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

/* PomodoroTimerActivity
 * - 25분 집중 + 5분 휴식의 뽀모도로 타이머 화면
 *
 * 기능 :
 * 1) 25분 집중 -> 5분 휴식
 * 2) 타이머 상태 관리: READY / RUNNING / PAUSED
 * 3) 알람 방식: SOUND / VIBRATE / SILENT
 * 4) SeekBar로 전체 사이클 진행 상황 시각화
 * 5) Cycle 종료 시, 과목에 대해 공부 기록 DB 저장
 */
class PomodoroTimerActivity : AppCompatActivity() {
    // 타이머 ui 상태를 나타내는 enum
    enum class TimerState { READY, RUNNING, PAUSED }
    // 현재 타이머 모드
    enum class TimerMode { FOCUS, BREAK }
    // 알람 방식 : 소리/진동/무음
    enum class AlarmMode{ SOUND, VIBRATE, SILENT }

    // ------ UI 요소 ----- //
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

    // ----- 상태 관련 변수 ----- //
    // 현재 타이머 UI 상태
    private var uiState = TimerState.READY
    // 현재 알람 모드
    // 초기값 : 소리
    private var alarmMode = AlarmMode.SOUND
    // 타이머 모드
    // 초기값 : 집중
    private var mode = TimerMode.FOCUS

    // ----- 타이머 관련 변수 ----- //
    private var countDownTimer: CountDownTimer? = null
    private val focusTime = 25 * 60 * 1000L
    private val breakTime = 5 * 60 * 1000L
    private var millisRemaining = focusTime

    // ----- 사운드 관련 변수 ----- //
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    // ----- 과목 정보 ----- //
    // 타이머에 연결할 과목 ID
    private var subjectId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro_timer)

        initViews() // View 참조
        initButton() // 버튼 설정 -> 클릭관련
        initSoundPool() // 알람 사운드
        updateUI() // 현재 IO 상태에 맞게 버튼 설정
        updateAlarmIcon() // 현재 알람 모드 아이콘 표시

        // TimerModeActivity에서 받은 subjectID 수신
        subjectId = intent.getIntExtra("subjectID", -1)
        if(subjectId==-1) subjectId = null // -1이면 과목이 없으므로 null
    }

    // ---------- 뷰 초기화 ---------- //

    /* initViews
     * 레이아웃의 View들을 findViewById로 초기화
     */
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

    // ---------- 버튼 리스너 ---------- //

    /* initButtons
     * 모든 버튼들의 클릭 리스너 정의
     * - 뒤로가기 / 시작 / 일시정지 / 재시작 / 리셋 / 알람모드 / 단위 설정
     */
    private fun initButton() {
        // 뒤로가기 버튼 -> 모드 선택 화면
        btnBack.setOnClickListener {
            startActivity(Intent(this, TimerModeActivity::class.java))
            finish()
        }

        // ----- 타이머 조작 관련 버튼 ----- //
        // 타이머 시작 버튼
        btnStart.setOnClickListener {
            mode = TimerMode.FOCUS
            millisRemaining = focusTime

            uiState = TimerState.RUNNING
            updateUI()
            startTimer(focusTime)
        }

        // 일시정지 버튼
        btnPause.setOnClickListener {
            uiState = TimerState.PAUSED
            updateUI()
            countDownTimer?.cancel()
        }

        // 재시작 버튼
        btnResume.setOnClickListener {
            uiState = TimerState.RUNNING
            updateUI()
            startTimer(millisRemaining)
        }

        // 리셋 버튼
        btnReset.setOnClickListener {
            countDownTimer?.cancel() // 타이머 완전 중단
            mode = TimerMode.FOCUS
            millisRemaining = focusTime
            timerSeekBar.progress = 0
            updateTimeUI(millisRemaining)

            uiState = TimerState.READY
            updateUI()
        }

        // ----- 알람 모드 전환 버튼 ----- //
        val alarmBtns = listOf(btnSound,btnVibrate,btnSilent)
        alarmBtns.forEach{ btn->
            btn.setOnClickListener {
                // 클릭
                // sound -> vibrate -> silent -> sound -> ...
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

    // ---------- UI 업데이트 ---------- //

    /* updateUI
     * READY/RUNNING/PAUSED에 따라
     * 어떤 버튼이 보이고, 어떤 버튼이 숨겨질지 결정
     */
    private fun updateUI() {
        when (uiState) {
            // 대기 중?
            // - 시작 버튼, 뒤로가기, 리셋 -> 보임
            // - 일시정지, 재생 -> 아직 시작 안했으므로 막기
            TimerState.READY -> {
                btnBack.visibility = View.VISIBLE
                btnStart.visibility = View.VISIBLE
                btnReset.visibility = View.VISIBLE
                btnPause.visibility = View.GONE
                btnResume.visibility = View.GONE
            }

            // 실행 중?
            // - 뒤로가기, 시작 -> 이미 시작했으므로 나갈 수 없음
            // - 리셋 버튼 -> 시작했어도 리셋은 가능
            // - 일시정지 버튼 -> 시작했으니 중간에 멈추기 가능
            // - 재시작 버튼 -> 정지 안했으므로 막기
            TimerState.RUNNING -> {
                btnBack.visibility = View.GONE
                btnStart.visibility = View.GONE
                btnReset.visibility = View.VISIBLE
                btnPause.visibility = View.VISIBLE
                btnResume.visibility = View.GONE
            }

            // 정지 중?
            // - 뒤로가기 -> 정지했으므로 타이머 나갈 수 있음
            // - 시작 버튼, 일시정지 버튼 -> 시작하고 정지를 누른 상태이므로 막기
            // - 리셋 버튼 -> 정지 중에도 리셋은 가능
            // - 재새 버튼 -> 정지했기 때문에 재생 가능
            TimerState.PAUSED -> {
                btnBack.visibility = View.VISIBLE
                btnStart.visibility = View.GONE
                btnReset.visibility = View.VISIBLE
                btnPause.visibility = View.GONE
                btnResume.visibility = View.VISIBLE
            }
        }
    }

    /* updateAlarmIcon
     * 현재 alarmMode에 따라 아이콘 결정
     * - SOUND: 소리 아이콘만 visible
     * - VIBRATE: 진동 아이콘만 visible
     * - SILENT: 무음 아이콘만 visible
     */
    private fun updateAlarmIcon() {
        btnSound.visibility = View.GONE
        btnVibrate.visibility = View.GONE
        btnSilent.visibility = View.GONE

        // 모드에 따라 아이콘 설정
        when (alarmMode) {
            AlarmMode.SOUND -> btnSound.visibility = View.VISIBLE
            AlarmMode.VIBRATE -> btnVibrate.visibility = View.VISIBLE
            AlarmMode.SILENT -> btnSilent.visibility = View.VISIBLE
        }
    }

    // ---------- Timer ---------- //

    /* startTimer
     * - 타이머 시작
     * - duration: ms 단위 타이머 전체 시간
     * - 1초마다 -> UI, SeekBar 업데이트
     * - 끝나면 -> 알람 + 종료 다이얼로그
     */
    private fun startTimer(duration: Long) {
        // 기존 타이머가 있다면 종료
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(duration, 1000) {

            // 1초마다 실행
            override fun onTick(millisUntilFinished: Long) {
                millisRemaining = millisUntilFinished
                updateTimeUI(millisRemaining) // 남은 시간 텍스트 업데이트

                // 현재 모드에 따라 seekbar 진행도 업데이트 방식 다르게 적용
                if (mode == TimerMode.FOCUS) {
                    updateSeekBarFocus(millisRemaining)
                } else {
                    updateSeekBarBreak(millisRemaining)
                }
            }

            // duration 종료 후 호출
            override fun onFinish() {
                millisRemaining = 0
                updateTimeUI(0)

                val alarmDelay = 4000L // 알람/진동 4초

                // 선택된 모드에 따라 동작
                when (alarmMode) {
                    AlarmMode.SOUND -> playSoundPattern()
                    AlarmMode.VIBRATE -> playVibrate()
                    AlarmMode.SILENT -> {}
                }

                // 알람 재생이 끝난 뒤 다이얼로그 실행
                val handler = android.os.Handler(mainLooper)
                handler.postDelayed({

                    // 모드에 따라 다른 다이얼로그 표시
                    if (mode == TimerMode.FOCUS) {
                        // 집중 시간 -> 휴식시간 여부 묻기
                        showPomodoroDialog(
                            "휴식시간을 시작하시겠습니까?",
                            // yes -> 휴식 모드
                            onYes = {
                                mode = TimerMode.BREAK
                                millisRemaining = breakTime

                                // seekbar를 집중 모드 끝까지 위치 이동
                                timerSeekBar.progress = (focusTime / 1000).toInt()
                                uiState = TimerState.RUNNING
                                updateUI()
                                startTimer(breakTime)
                            },
                            // no -> 집중까지만 하고 종료
                            onNo = {
                                saveStudyRecord()
                                startActivity(
                                    Intent(this@PomodoroTimerActivity, MainActivity::class.java)
                                )
                                finish()
                            }
                        )
                    } else {
                        showPomodoroDialog(
                            // 휴식시간 -> 다시 집중시간 여부 묻기
                            "다시 집중시간을 시작하시겠습니까?",
                            // yes -> 다시 집중 모드
                            onYes = {
                                mode = TimerMode.FOCUS
                                millisRemaining = focusTime

                                // seekbar 처음 위치로 이동
                                timerSeekBar.progress = 0
                                uiState = TimerState.RUNNING
                                updateUI()
                                startTimer(focusTime)
                            },
                            // no -> 여기까지 총 공부 시간 기록 -> 메인 이동
                            onNo = {
                                saveStudyRecord()
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

    /* updateTimeUI
     * 남은 시간(ms)을 분/초로 변환하여 TextView 업데이트
     */
    private fun updateTimeUI(millis: Long) {
        // 분, 초 변환 및 표시
        val totalSec = millis / 1000
        val min = totalSec / 60
        val sec = totalSec % 60

        minText.text = String.format("%02d'", min)
        secText.text = String.format("%02d", sec)
    }

    /* updateSeekBarProgress
     * 타이머 진행에 따라 SeekBar progress 갱신
     */
    private fun updateSeekBarFocus(millis: Long) {
        val totalSec = focusTime / 1000     // 1500
        val passedSec = totalSec - (millis / 1000)
        timerSeekBar.progress = passedSec.toInt()
    }

    private fun updateSeekBarBreak(millis: Long) {
        val totalSec = breakTime / 1000     // 300
        val passedSec = totalSec - (millis / 1000)
        val progress = 1500 + passedSec
        timerSeekBar.progress = progress.toInt()
    }

    // ---------- SoundPool ---------- //

    /* initSoundPool
     * 알람음을 재생하기 위한 SoundPool 초기화
     * - R.raw.alarm_sound 로딩
     */
    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // raw에서 alarm_sound.mp3 불러오기
        soundId = soundPool.load(this, R.raw.alarm_sound, 1)
    }

    // ---------- Alarm ---------- //

    /* playVibrate
     * 진동 패턴 재생
     */
    private fun playVibrate(){
        Toast.makeText(this, "vibrate", Toast.LENGTH_SHORT).show()

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // on/off 패턴
        val timings = longArrayOf(0, 500, 200, 500, 200, 500,
            200, 500)
        // 진동 세기
        val amplitude = intArrayOf(
            0,
            VibrationEffect.DEFAULT_AMPLITUDE, 0,
            VibrationEffect.DEFAULT_AMPLITUDE, 0,
            VibrationEffect.DEFAULT_AMPLITUDE, 0,
            VibrationEffect.DEFAULT_AMPLITUDE
        )

        // 진동 울리기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(timings,amplitude,-1)
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(timings,-1);
        }
    }

    /* playSoundPattern
     * 알람 사운드 한 번 재생
     */
    private fun playSoundPattern(){
        Toast.makeText(this, "sound", Toast.LENGTH_SHORT).show()
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    // ---------- 종료 표시용 Dialog ---------- //

    /* showPomodoroDialog
     * 타이머 종료 후 나타나는 다이얼로그
     * - "메인 화면으로 돌아갈까요?" 질문
     * - 예(Yes): 공부 기록 저장 + MainActivity로 이동
     * - 아니오(No): 타이머 UI만 초기화 후 현재 화면 유지
     */
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

        // 네 버튼
        btnYes.setOnClickListener {
            onYes()
            dialog.dismiss()
        }

        // 아니요 버튼
        btnNo.setOnClickListener {
            onNo()
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // ---------- 공부 시간 기록 ---------- //

    /* saveStudyRecord
     * - subjectId가 null이면 기록하지 않음
     * - studyTime = (전체 선택 시간 - 남은 시간) 또는 전체 선택 시간
     */
    private fun saveStudyRecord() {
        // 과목이 연결되어 있지 않다면 기록 안함
        if(subjectId == null) return

        // ROom DB 접근
        val dao = AppDatabase.getDatabase(this).studyRecordDao()

        // 오늘 날짜
        val date = java.time.LocalDate.now().toString()

        // 25분 전체에서 남은 시간을 뺴서 집중한 시간 계산
        val focusDuration = focusTime - millisRemaining

        // 0 이하가 되는 경우에 대한 예외처리
        val finalTime =
            if(focusDuration>0){
                focusDuration
            }
            else{
                focusTime
            }

        // 저장할 StudyRecordEntity 생성
        val record = StudyRecordEntity(
            subjectId = subjectId,
            date = date,
            studyTime = finalTime
        )

        // DB 저장
        // 같은 날짜, 과목이 있다면? 업데이트
        // 없으면? 삽입
        lifecycleScope.launch {
            dao.insertOrUpdate(record)
        }

    }
}
