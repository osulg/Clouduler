package com.example.clouduler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper


/* SplashActivity
 * - 앱 실행 시 가장 먼저 보여지는 로고 / 스플래시 화면
 * - 2초 동안 보여준 뒤 자동으로 MainActivity로 이동
 *
 * 동작 :
 * onCreate() -> 스플래시 레이아웃 표시 
 * -> Handler.postDelayed() 2초 타이머
 * -> MainActivity로 이동 + SplashActivity 종료
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // splash 화면 UI 적용

        // 메인 스레드에서 2초 지연 후 코드 실행
        Handler(Looper.getMainLooper()).postDelayed({
            // 2초 뒤 MainActivity 이동
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}