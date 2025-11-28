package com.example.clouduler

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.StudyRecordEntity
import kotlinx.coroutines.launch

class PomodoroTimerActivity : AppCompatActivity() {
    enum class TimerState { READY, RUNNING, PAUSED }
    enum class TimerMode { FOCUS, BREAK }
    enum class AlarmMode{ SOUND, VIBRATE, SILENT }

    private lateinit var btnStart: ImageButton
    private lateinit var btnReset: ImageButton
    private lateinit var btnResume: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var minText: TextView
    private lateinit var secText: TextView
    private lateinit var timerSeekBar: SeekBar

    private var uiState = TimerState.READY

    private var countDownTimer: CountDownTimer? = null
    private val focusTime = 20 * 1000L
    private val breakTime = 10 * 1000L
    private var millisRemaining = focusTime
    private var mode = TimerMode.FOCUS

    private var alarmMode = AlarmMode.SOUND
    private lateinit var btnSound: ImageButton
    private lateinit var btnVibrate: ImageButton
    private lateinit var btnSilent: ImageButton

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    private var subjectId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro_timer)

        initViews()
        initButtonEvents()
        initSoundPool()

        updateUI()
        updateAlarmIcon()

        subjectId = intent.getIntExtra("subjectID", -1)
        if(subjectId==-1) subjectId = null
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnStart = findViewById(R.id.btnStart)
        btnReset = findViewById(R.id.btnReset)
        btnPause = findViewById(R.id.btnPause)
        btnResume = findViewById(R.id.btnResume)

        minText = findViewById(R.id.minText)
        secText = findViewById(R.id.secText)
        timerSeekBar = findViewById(R.id.timerSeekBar)

        btnSound = findViewById(R.id.btn_sound)
        btnVibrate = findViewById(R.id.btn_vibrate)
        btnSilent = findViewById(R.id.btn_silent)
    }

    private fun initButtonEvents() {
        btnBack.setOnClickListener {
            startActivity(Intent(this, TimerModeActivity::class.java))
            finish()
        }

        btnStart.setOnClickListener {
            mode = TimerMode.FOCUS
            millisRemaining = focusTime

            uiState = TimerState.RUNNING
            updateUI()
            startTimer(focusTime)
        }

        btnPause.setOnClickListener {
            uiState = TimerState.PAUSED
            updateUI()
            countDownTimer?.cancel()
        }

        btnResume.setOnClickListener {
            uiState = TimerState.RUNNING
            updateUI()
            startTimer(millisRemaining)
        }

        btnReset.setOnClickListener {
            countDownTimer?.cancel()
            mode = TimerMode.FOCUS
            millisRemaining = focusTime
            timerSeekBar.progress = 0
            updateTimeUI(millisRemaining)

            uiState = TimerState.READY
            updateUI()
        }

        val alarmBtns = listOf(btnSound,btnVibrate,btnSilent)

        alarmBtns.forEach{ btn->
            btn.setOnClickListener {
                alarmMode=
                    when(alarmMode){
                        AlarmMode.SOUND -> AlarmMode.VIBRATE
                        AlarmMode.VIBRATE -> AlarmMode.SILENT
                        AlarmMode.SILENT -> AlarmMode.SOUND
                    }
                updateAlarmIcon()
            }
        }
    }

    private fun updateUI() {
        when (uiState) {
            TimerState.READY -> {
                btnBack.visibility = View.VISIBLE
                btnStart.visibility = View.VISIBLE
                btnReset.visibility = View.VISIBLE
                btnPause.visibility = View.GONE
                btnResume.visibility = View.GONE
            }

            TimerState.RUNNING -> {
                btnBack.visibility = View.GONE
                btnStart.visibility = View.GONE
                btnReset.visibility = View.VISIBLE
                btnPause.visibility = View.VISIBLE
                btnResume.visibility = View.GONE
            }

            TimerState.PAUSED -> {
                btnBack.visibility = View.VISIBLE
                btnStart.visibility = View.GONE
                btnReset.visibility = View.VISIBLE
                btnPause.visibility = View.GONE
                btnResume.visibility = View.VISIBLE
            }
        }
    }

    private fun updateAlarmIcon() {
        btnSound.visibility = View.GONE
        btnVibrate.visibility = View.GONE
        btnSilent.visibility = View.GONE

        when (alarmMode) {
            AlarmMode.SOUND -> btnSound.visibility = View.VISIBLE
            AlarmMode.VIBRATE -> btnVibrate.visibility = View.VISIBLE
            AlarmMode.SILENT -> btnSilent.visibility = View.VISIBLE
        }

    }

    private fun startTimer(duration: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(duration, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                millisRemaining = millisUntilFinished
                updateTimeUI(millisRemaining)

                if (mode == TimerMode.FOCUS) {
                    updateSeekBarFocus(millisRemaining)
                } else {
                    updateSeekBarBreak(millisRemaining)
                }
            }

            override fun onFinish() {
                millisRemaining = 0
                updateTimeUI(0)

                val alarmDelay = 4000L

                when (alarmMode) {
                    AlarmMode.SOUND -> playSoundPattern()
                    AlarmMode.VIBRATE -> playVibrate()
                    AlarmMode.SILENT -> {}
                }

                val handler = android.os.Handler(mainLooper)
                handler.postDelayed({

                    if (mode == TimerMode.FOCUS) {
                        showPomodoroDialog(
                            "휴식시간을 시작하시겠습니까?",
                            onYes = {
                                mode = TimerMode.BREAK
                                millisRemaining = breakTime
                                timerSeekBar.progress = (focusTime / 1000).toInt()
                                uiState = TimerState.RUNNING
                                updateUI()
                                startTimer(breakTime)
                            },
                            onNo = {
                                saveStudyRecord()
                                startActivity(
                                    Intent(this@PomodoroTimerActivity, TimerModeActivity::class.java)
                                )
                                finish()
                            }
                        )
                    } else {
                        showPomodoroDialog(
                            "다시 집중시간을 시작하시겠습니까?",
                            onYes = {
                                mode = TimerMode.FOCUS
                                millisRemaining = focusTime
                                timerSeekBar.progress = 0
                                uiState = TimerState.RUNNING
                                updateUI()
                                startTimer(focusTime)
                            },
                            onNo = {
                                saveStudyRecord()   // ← 반드시 여기에도 넣어야 함
                                startActivity(
                                    Intent(this@PomodoroTimerActivity, MainActivity::class.java)
                                )
                                finish()
                            }
                        )
                    }
                }, alarmDelay)
            }
        }

        countDownTimer?.start()
    }


    private fun updateTimeUI(millis: Long) {
        val totalSec = millis / 1000
        val min = totalSec / 60
        val sec = totalSec % 60

        minText.text = String.format("%02d'", min)
        secText.text = String.format("%02d", sec)
    }

    private fun updateSeekBarFocus(millis: Long) {
        val totalSec = focusTime / 1000     // 1500
        val passedSec = totalSec - (millis / 1000)
        timerSeekBar.progress = passedSec.toInt()
    }

    private fun updateSeekBarBreak(millis: Long) {
        val totalSec = breakTime / 1000     // 300
        val passedSec = totalSec - (millis / 1000)
        timerSeekBar.progress = 20 + passedSec.toInt()
    }

    private fun playVibrate(){
        Toast.makeText(this, "vibrate", Toast.LENGTH_SHORT).show()

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val timing = longArrayOf(
            0,      // 바로 시작
            500, 200,   // 0.5 ON + 0.2 OFF
            500, 200,   // 반복
            500, 200,
            500        // 마지막 진동 → 총 4초
        )

        val amplitude = intArrayOf(
            0,
            VibrationEffect.DEFAULT_AMPLITUDE, 0,
            VibrationEffect.DEFAULT_AMPLITUDE, 0,
            VibrationEffect.DEFAULT_AMPLITUDE, 0,
            VibrationEffect.DEFAULT_AMPLITUDE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(timing,amplitude,-1)
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(timing,-1);
        }
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        soundId = soundPool.load(this, R.raw.alarm_sound, 1)
    }

    private fun playSoundPattern(){
        Toast.makeText(this, "sound", Toast.LENGTH_SHORT).show()
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    private fun showPomodoroDialog(
        message: String,
        onYes: ()-> Unit,
        onNo: () -> Unit
    ) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_pomodoro_mode, null)
        dialog.setContentView(view)

        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val btnYes = view.findViewById<Button>(R.id.btn_yes)
        val btnNo = view.findViewById<Button>(R.id.btn_no)

        tvMessage.text = message

        btnYes.setOnClickListener {
            onYes()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            onNo()
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun saveStudyRecord() {
        if(subjectId == null) return

        val dao = AppDatabase.getDatabase(this).studyRecordDao()
        val date = java.time.LocalDate.now().toString()
        val focusDuration = focusTime - millisRemaining
        val finalTime =
            if(focusDuration>0){
                focusDuration
            }
            else{
                focusTime
            }

        val record = StudyRecordEntity(
            subjectId = subjectId,
            date = date,
            studyTime = finalTime
        )

        lifecycleScope.launch {
            dao.insertRecord(record)
        }

    }
}
