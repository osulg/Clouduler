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
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.StudyRecordEntity
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch
import java.time.LocalDate

class NormalTimerActivity : AppCompatActivity() {

    enum class TimerState { READY, RUNNING, PAUSED }
    enum class AlarmMode { SOUND, VIBRATE, SILENT }
    enum class UnitMode { ONE_MIN, FIVE_MIN }

    private lateinit var unitToggleGroup: MaterialButtonToggleGroup
    private lateinit var btnStart: ImageButton
    private lateinit var btnReset: ImageButton
    private lateinit var btnResume: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnBack: ImageButton
    private lateinit var minText: TextView
    private lateinit var secText: TextView
    private lateinit var timerSeekBar: SeekBar

    private lateinit var btnSound: ImageButton
    private lateinit var btnVibrate: ImageButton
    private lateinit var btnSilent: ImageButton

    private lateinit var btnUnit1: Button
    private lateinit var btnUnit5: Button

    private var uiState = TimerState.READY
    private var alarmMode = AlarmMode.SOUND
    private var unitMode = UnitMode.ONE_MIN

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    private var countDownTimer: CountDownTimer? = null
    private var millisRemaining: Long = 0L
    private var totalSelectedTime: Long = 0L

    private var subjectId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_timer)

        initViews()
        initButtons()
        initSoundPool()
        setupSeekBar()

        updateUnitButtons()
        updateUI()
        updateAlarmIcon()

        subjectId = intent.getIntExtra("subjectID", -1)
        if(subjectId==-1) subjectId = null
    }

    // ---------------------- SeekBar 설정 ----------------------

    private fun setupSeekBar() {
        timerSeekBar.max = 120    // 최대 120분 = 120칸

        timerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) updateSeekBarTimeText(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // 단위에 따라 텍스트 표시
    private fun updateSeekBarTimeText(progress: Int) {
        val minutes =
            when (unitMode) {
                UnitMode.ONE_MIN -> progress                   // 1분 단위
                UnitMode.FIVE_MIN -> (progress / 5) * 5       // 5분 단위 반올림
            }

        minText.text = String.format("%02d'", minutes)
        secText.text = "00"
    }

    // ---------------------- 뷰 초기화 ----------------------

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

        // ★ 단위 버튼
        btnUnit1 = findViewById(R.id.btnUnit1)
        btnUnit5 = findViewById(R.id.btnUnit5)

        unitToggleGroup = findViewById(R.id.unitToggleGroup)
    }

    // ---------------------- 버튼 리스너 ----------------------

    private fun initButtons() {

        btnBack.setOnClickListener {
            startActivity(Intent(this, TimerModeActivity::class.java))
            finish()
        }

        btnStart.setOnClickListener {
            unitToggleGroup.isEnabled = false
            val selectedMinutes =
                when (unitMode) {
                    UnitMode.ONE_MIN -> timerSeekBar.progress
                    UnitMode.FIVE_MIN -> (timerSeekBar.progress / 5) * 5
                }

            totalSelectedTime = selectedMinutes * 60000L
            millisRemaining = totalSelectedTime

            timerSeekBar.isEnabled = false

            uiState = TimerState.RUNNING
            updateUI()

            startTimer(totalSelectedTime)
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
            unitToggleGroup.isEnabled = true
            countDownTimer?.cancel()

            uiState = TimerState.READY
            updateUI()

            timerSeekBar.isEnabled = true
            timerSeekBar.progress = 0

            millisRemaining = 0
            updateTimeUI(0)
        }

        // ---------------- Alarm Mode ----------------
        val alarmBtns = listOf(btnSound, btnVibrate, btnSilent)
        alarmBtns.forEach { btn ->
            btn.setOnClickListener {
                alarmMode = when (alarmMode) {
                    AlarmMode.SOUND -> AlarmMode.VIBRATE
                    AlarmMode.VIBRATE -> AlarmMode.SILENT
                    AlarmMode.SILENT -> AlarmMode.SOUND
                }
                updateAlarmIcon()
            }
        }

        // ---------------- Unit Toggle Buttons ----------------

        btnUnit1.setOnClickListener {
            unitMode = UnitMode.ONE_MIN
            updateUnitButtons()
            updateSeekBarTimeText(timerSeekBar.progress)
        }

        btnUnit5.setOnClickListener {
            unitMode = UnitMode.FIVE_MIN
            updateUnitButtons()
            updateSeekBarTimeText(timerSeekBar.progress)
        }
    }

    // 단위 버튼 UI 업데이트
    private fun updateUnitButtons() {
        val unitGroup = findViewById<MaterialButtonToggleGroup>(R.id.unitToggleGroup)

        unitGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnUnit1 -> {
                        unitMode = UnitMode.ONE_MIN
                    }
                    R.id.btnUnit5 -> {
                        unitMode = UnitMode.FIVE_MIN
                    }
                }
                updateSeekBarTimeText(timerSeekBar.progress)
            }
        }
    }

    // ---------------------- UI 업데이트 ----------------------

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

    // ---------------------- SoundPool ----------------------

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

    // ---------------------- Timer ----------------------

    private fun startTimer(duration: Long) {

        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(duration, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                millisRemaining = millisUntilFinished
                updateTimeUI(millisRemaining)
                updateSeekBarProgress(millisRemaining)   // 1분마다 구름 이동
            }

            override fun onFinish() {
                millisRemaining = 0
                updateTimeUI(0)

                val alarmDuration = 4000L

                when (alarmMode) {
                    AlarmMode.SOUND -> playSoundPattern()
                    AlarmMode.VIBRATE -> playVibrate()
                    AlarmMode.SILENT -> {}
                }

                val handler = android.os.Handler(mainLooper)
                handler.postDelayed({
                    showFinishDialog()
                }, alarmDuration)
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

    // ★ 타이머 진행 중 1분마다 SeekBar 이동
    private fun updateSeekBarProgress(millis: Long) {
        val totalMinutes = totalSelectedTime / 60000
        val remainingMinutes = millis / 60000
        val passedMinutes = totalMinutes - remainingMinutes

        timerSeekBar.progress = passedMinutes.toInt()
    }

    // ---------------------- Alarm ----------------------

    private fun playVibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val timings = longArrayOf(0, 500, 200, 500, 200, 500,
            200, 500)
        val amplitudes = intArrayOf(
            0,
            VibrationEffect.DEFAULT_AMPLITUDE,
            0,
            VibrationEffect.DEFAULT_AMPLITUDE,
            0,
            VibrationEffect.DEFAULT_AMPLITUDE,
            0,
            VibrationEffect.DEFAULT_AMPLITUDE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            vibrator.vibrate(timings, -1)
        }
    }

    private fun playSoundPattern() {
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    // ---------------------- Finish Dialog ----------------------

    private fun showFinishDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_pomodoro_mode, null)
        dialog.setContentView(view)

        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val btnYes = view.findViewById<Button>(R.id.btn_yes)
        val btnNo = view.findViewById<Button>(R.id.btn_no)

        tvMessage.text = "타이머가 종료되었습니다.\n메인 화면으로 돌아가시겠습니까?"

        btnYes.setOnClickListener {
            saveStudyRecord()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            uiState = TimerState.READY
            updateUI()

            timerSeekBar.isEnabled = true
            timerSeekBar.progress = 0
            updateTimeUI(0)

            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun saveStudyRecord(subjectId: Int?, time: Long) {
        val dao = AppDatabase.getDatabase(this).studyRecordDao()
        val date = LocalDate.now().toString()

        val record = StudyRecordEntity(
            subjectId = subjectId,
            date = date,
            studyTime = time
        )

        lifecycleScope.launch {
            dao.insertRecord(record)
        }
    }

    private fun saveStudyRecord() {
        if(subjectId == null) return

        val dao = AppDatabase.getDatabase(this).studyRecordDao()
        val date = java.time.LocalDate.now().toString()
        val timeMillis = totalSelectedTime - millisRemaining
        val finalTime =
            if(timeMillis>0){
                timeMillis
            }
            else{
                totalSelectedTime
            }

        val record = StudyRecordEntity(
            subjectId = subjectId,
            date = date,
            studyTime = finalTime
        )

        lifecycleScope.launch {
            dao.insertOrUpdate(record)
        }
    }
}
