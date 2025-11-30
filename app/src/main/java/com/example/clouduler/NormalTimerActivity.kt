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
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.StudyRecordEntity
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch
import java.time.LocalDate

/* NormalTimerActivity
 * - 일반 타이머 화면
 * - 사용자가 선택한 시간만큼 타이머 작동
 *
 * 기능:
 * 1) SeekBar를 통해 총 타이머 시간을 설정
 * - 단위는 1분 or 5분
 * 2) Start / Pause / Resume / Reset 버튼으로 타이머 제어
 * - 상황에 따라 버튼 요소 바뀜
 * 3) 알람 모드(소리 / 진동 / 무음) 전환
 * 4) 타이머 종료 -> 공부 시간 DB(StudyRecord)에 기록 저장
 * - 해당 내용은 공부할 과목을 선택한 경우에만
 * 5) TimerModeActivity에서 받은 subjectID 기준으로 공부 기록 연결
 */
class NormalTimerActivity : AppCompatActivity() {
    // 타이머 ui를 나타내는 enum
    enum class TimerState { READY, RUNNING, PAUSED }
    // 알람 방식
    // 소리, 진동, 무음
    enum class AlarmMode { SOUND, VIBRATE, SILENT }
    // seekbar 단위
    // 1분, 5분
    enum class UnitMode { ONE_MIN, FIVE_MIN }

    // ------ UI 요소 ----- //
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

    // ----- 상태 관련 변수 ----- //
    // 현재 타이머 UI 상태
    private var uiState = TimerState.READY
    // 현재 알람 모드
    // 초기값 : 소리
    private var alarmMode = AlarmMode.SOUND
    // seekbar 단위
    // 초기값 : 1분
    private var unitMode = UnitMode.ONE_MIN

    // ----- 사운드 관련 변수 ----- //
    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    // ----- 타이머 관련 변수 ----- //
    private var countDownTimer: CountDownTimer? = null // 실제 타이머
    private var millisRemaining: Long = 0L // 남은 시간
    private var totalSelectedTime: Long = 0L // 처음 설정된 전체 시간

    // ----- 과목 정보 ----- //
    // 타이머에 연결할 과목 ID
    private var subjectId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_timer)

        initViews() // View 참조
        initButtons() // 버튼 설정 -> 클릭관련
        initSoundPool() // 알람 사운드
        setupSeekBar() // seekbar 설정
        updateUnitButtons() // 단위 버튼/토클 초기 상태 설정
        updateUI() // 현재 IO 상태에 맞게 버튼 설정
        updateAlarmIcon() // 현재 알람 모드 아이콘 표시

        // TimerModeActivity에서 받은 subjectID 수신
        subjectId = intent.getIntExtra("subjectID", -1)
        if(subjectId==-1) subjectId = null // -1이면 과목이 없으므로 null
    }

    // ---------- SeekBar 설정 ---------- //

    /* setupSeekBar
     * 타이머 시간을 설정하는 SeekBar 설정
     * - max=120 → 최대 120분까지 설정 가능
     * - progress 값에 따라 분 텍스트 업데이트
     */
    private fun setupSeekBar() {
        timerSeekBar.max = 120    // 최대 120분 = 120칸

        timerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            // seekBar 값 변경 -> 호출
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // 사용자가 직접 드래그 -> 반영
                if (fromUser) updateSeekBarTimeText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    /* updateSeekBarTimeText
     * 현재 unitMode(1분/5분 단위)에 따라 시간 텍스트 갱신
     * - progress: SeekBar의 현재 값
     * - ONE_MIN: progress가 그대로 분으로 사용
     * - FIVE_MIN: progress를 5분 단위
     */
    private fun updateSeekBarTimeText(progress: Int) {
        val minutes =
            when (unitMode) {
                UnitMode.ONE_MIN -> progress // 1분 단위
                UnitMode.FIVE_MIN -> (progress / 5) * 5 // 5분 단위 반올림
            }

        // MM 형태로 텍스트 표시
        minText.text = String.format("%02d'", minutes)
        // 초는 항상 00
        secText.text = "00"
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

        btnUnit1 = findViewById(R.id.btnUnit1)
        btnUnit5 = findViewById(R.id.btnUnit5)

        unitToggleGroup = findViewById(R.id.unitToggleGroup)
    }

    // ---------- 버튼 리스너 ---------- //

    /* initButtons
     * 모든 버튼들의 클릭 리스너 정의
     * - 뒤로가기 / 시작 / 일시정지 / 재시작 / 리셋 / 알람모드 / 단위 설정
     */
    private fun initButtons() {
        // 뒤로가기 버튼 -> 모드 선택 화면
        btnBack.setOnClickListener {
            startActivity(Intent(this, TimerModeActivity::class.java))
            finish()
        }

        // ----- 타이머 조작 관련 버튼 ----- //
        // 타이머 시작 버튼
        btnStart.setOnClickListener {
            unitToggleGroup.isEnabled = false // 타이머 시작 후 -> 시간 단위 변경 비활성화

            // 현재 단위 모드에서 선택된 총 분 계산
            val selectedMinutes =
                when (unitMode) {
                    UnitMode.ONE_MIN -> timerSeekBar.progress
                    UnitMode.FIVE_MIN -> (timerSeekBar.progress / 5) * 5
                }

            // 선택된 분을 ms로 변환
            totalSelectedTime = selectedMinutes * 60000L
            millisRemaining = totalSelectedTime

            // 타이머 진행 -> seekbar 드래그 불가능
            timerSeekBar.isEnabled = false

            // 상태 변경
            // 대기 -> 실행중
            uiState = TimerState.RUNNING
            updateUI()

            // 타이머 시작
            startTimer(totalSelectedTime)
        }

        // 일시정지 버튼
        btnPause.setOnClickListener {
            uiState = TimerState.PAUSED
            updateUI()
            countDownTimer?.cancel() // 타이머 정지
        }

        // 재시작 버튼
        btnResume.setOnClickListener {
            uiState = TimerState.RUNNING
            updateUI()
            startTimer(millisRemaining) // 남은 시간부터 시작
        }

        // 리셋 버튼
        btnReset.setOnClickListener {
            unitToggleGroup.isEnabled = true // 단위 변경 다시 가능
            countDownTimer?.cancel() // 타이머 완전 중단

            // 다시 대기 상태로 전환
            uiState = TimerState.READY
            updateUI()

            // seekbar 다시 활성화 + 0으로 초기화
            timerSeekBar.isEnabled = true
            timerSeekBar.progress = 0

            // 남은 시간을 0으로 초기화 및 표시
            millisRemaining = 0
            updateTimeUI(0)
        }

        // ----- 알람 모드 전환 버튼 ----- //
        val alarmBtns = listOf(btnSound, btnVibrate, btnSilent)
        alarmBtns.forEach { btn ->
            btn.setOnClickListener {
                // 클릭
                // sound -> vibrate -> silent -> sound -> ...
                alarmMode = when (alarmMode) {
                    AlarmMode.SOUND -> AlarmMode.VIBRATE
                    AlarmMode.VIBRATE -> AlarmMode.SILENT
                    AlarmMode.SILENT -> AlarmMode.SOUND
                }
                updateAlarmIcon()
            }
        }

        // ----- 타이머 단위 설정 버튼 ----- //

        // 1분 단위 버튼
        btnUnit1.setOnClickListener {
            unitMode = UnitMode.ONE_MIN
            updateUnitButtons()
            updateSeekBarTimeText(timerSeekBar.progress)
        }

        // 5분 단위 버튼
        btnUnit5.setOnClickListener {
            unitMode = UnitMode.FIVE_MIN
            updateUnitButtons()
            updateSeekBarTimeText(timerSeekBar.progress)
        }
    }

    /* updateUnitButtons
     * 단위 토글 버튼 UI 설정
     * - MaterialButtonToggleGroup의 체크 상태에 따라 unitMode 반영
     */
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

                // 단위가 바뀌면 텍스트 다시 계산
                updateSeekBarTimeText(timerSeekBar.progress)
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

            override fun onTick(millisUntilFinished: Long) {
                millisRemaining = millisUntilFinished
                updateTimeUI(millisRemaining) // 남은 시간 텍스트 업데이트
                updateSeekBarProgress(millisRemaining)   // 1분마다 구름 이동
            }

            override fun onFinish() {
                millisRemaining = 0
                updateTimeUI(0)

                val alarmDuration = 4000L // 알람/진동 4초

                // 선택된 모드에 따라 동작
                when (alarmMode) {
                    AlarmMode.SOUND -> playSoundPattern()
                    AlarmMode.VIBRATE -> playVibrate()
                    AlarmMode.SILENT -> {}
                }

                // 알람이 끝나면 종료 다이얼로그 표시
                val handler = android.os.Handler(mainLooper)
                handler.postDelayed({
                    showFinishDialog()
                }, alarmDuration)
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
    private fun updateSeekBarProgress(millis: Long) {
        val elapsed = totalSelectedTime - millisRemaining

        val percent = ((elapsed.toFloat() / totalSelectedTime.toFloat()) * 100).toInt()
        timerSeekBar.progress = percent.coerceIn(0, 100)

        timerSeekBar.invalidate()  // 즉시 redraw
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
    private fun playVibrate() {
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
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitude, -1))
        } else {
            vibrator.vibrate(timings, -1)
        }
    }

    /* playSoundPattern
     * 알람 사운드 한 번 재생
     */
    private fun playSoundPattern() {
        Toast.makeText(this, "sound", Toast.LENGTH_SHORT).show()
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }

    // ---------- 종료 표시용 Dialog ---------- //

    /* showFinishDialog
     * 타이머 종료 후 나타나는 다이얼로그
     * - "메인 화면으로 돌아갈까요?" 질문
     * - 예(Yes): 공부 기록 저장 + MainActivity로 이동
     * - 아니오(No): 타이머 UI만 초기화 후 현재 화면 유지
     */
    private fun showFinishDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_pomodoro_mode, null)
        dialog.setContentView(view)

        val tvMessage = view.findViewById<TextView>(R.id.tv_message)
        val btnYes = view.findViewById<Button>(R.id.btn_yes)
        val btnNo = view.findViewById<Button>(R.id.btn_no)

        tvMessage.text = "타이머가 종료되었습니다.\n메인 화면으로 돌아가시겠습니까?"

        // 예
        // 공부 기록 저장 + 메인 화면 이동
        btnYes.setOnClickListener {
            saveStudyRecord() // 공부 기록 저장
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            dialog.dismiss() // 닫기
        }

        // 아니요
        // 타이머를 초기 상태로 리셋 + 해당 화면으로 유지
        btnNo.setOnClickListener {
            // 초기 상태
            uiState = TimerState.READY
            updateUI()

            timerSeekBar.isEnabled = true
            timerSeekBar.progress = 0
            updateTimeUI(0)

            dialog.dismiss() // 닫기
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

        // 총 선택시간에서 남은 시간을 빼서
        // 실제 공부 시간 계산
        val timeMillis = totalSelectedTime - millisRemaining

        // 0이하인 경우에 대해서 예외처리
        val finalTime =
            if(timeMillis>0){
                timeMillis
            }
            else{
                totalSelectedTime
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
