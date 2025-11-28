package com.example.clouduler

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.SubjectEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.clouduler.data.ParentColorAdapter
import com.example.clouduler.data.ToneColorAdapter

class AddActivity : AppCompatActivity() {

    private var difficultyLevel = 0f
    private var importanceLevel = 0f
    private var selectedDate = ""

    private var curSelectedColor: Int? = null

    private val allColors = listOf(
        0xFFFAE3E2.toInt(),
        0xFFF6F3EE.toInt(),
        0xFFEDECEB.toInt(),
        0xFFEAE8EB.toInt(),
        0xFFEDDFDE.toInt(),
        0xFFFBEFE3.toInt(),
        0xFFFCE4E2.toInt(),
        0xFFFAE8E8.toInt(),
        0xFFF8E6E6.toInt(),
        0xFFCE9C9D.toInt(),
        0xFFD5A4A3.toInt(),
        0xFFE9BDBE.toInt(),
        0xFFFAF3EB.toInt(),
        0xFFEAEEE0.toInt(),
        0xFFB0B0B2.toInt(),
        0xFFE1E2E4.toInt(),
        0xFFDADADC.toInt(),
        0xFFB5A28A.toInt(),
        0xFFEBF6FA.toInt(),
        0xFFD9E6EC.toInt(),
        0xFFA4C8D5.toInt(),
        0xFF80ADBC.toInt(),
        0xFFAED9EA.toInt(),
        0xFFC0D3D8.toInt(),
        0xFFEFEFEF.toInt(),
        0xFFECDCDC.toInt(),
        0xFFEEE8E8.toInt(),
        0xFFE9CCC4.toInt(),
        0xFFE7E7DB.toInt(),
        0xFFDFD8AB.toInt(),
        0xFFEFDFD5.toInt(),
        0xFFE3E2E1.toInt(),
        0xFFDEE9EB.toInt(),
        0xFFF0EDE8.toInt(),
        0xFFC2B7B1.toInt(),
        0xFFE1D7CD.toInt(),
        0xFFD7E0E5.toInt(),
        0xFFADB5BE.toInt(),
        0xFFD4C8B6.toInt(),
        0xFFD9BD9C.toInt(),
        0xFFCAB08B.toInt(),
        0xFFD7DBDA.toInt(),
        0xFFE6EDF3.toInt(),
        0xFFD4E2EF.toInt(),
        0xFFB4C6DC.toInt(),
        0xFFDEC8BD.toInt(),
        0xFFF1D5BD.toInt(),
        0xFFE0B394.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val editSubject = findViewById<EditText>(R.id.edit_subject)
        val calendarView = findViewById<CalendarView>(R.id.calendarView2)
        val btnSave = findViewById<ImageButton>(R.id.btn_save)
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val btnDiff = findViewById<ImageButton>(R.id.btn_difficulty)
        val btnImp = findViewById<ImageButton>(R.id.btn_importance)
        val btnColor = findViewById<ImageButton>(R.id.btn_colorPicker)

        // Room DB 인스턴스 생성
        val db = AppDatabase.getDatabase(this)
        val dao = db.subjectDao()

        // 날짜 선택
        // yyyy-mm-dd 형식 저장
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()

            calendar.set(year, month, dayOfMonth)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedDate = format.format(calendar.time)
        }

        // 뒤로가기 버튼 → 메인으로
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 난이도 버튼 클릭 → 별점 다이얼로그
        btnDiff.setOnClickListener {
            showRatingDialog("난이도 선택") { rating ->
                difficultyLevel = rating
            }
        }

        // 중요도 버튼 클릭 → 별점 다이얼로그
        btnImp.setOnClickListener {
            showRatingDialog("중요도 선택") { rating ->
                importanceLevel = rating
            }
        }

        btnColor.setOnClickListener {
            showColorPicker(btnColor)
        }

        btnSave.setOnClickListener {
            val subjectName = editSubject.text.toString().trim()

            // 1) 기본색을 parentColors 첫 번째 색으로 지정
            val fallbackColor = allColors.first()

            // 2) 실제 저장할 색
            val finalColor = curSelectedColor ?: fallbackColor
            Log.d("COLOR_CHECK", "최종 저장 색상: $finalColor")

            val subject = SubjectEntity(
                name = subjectName,
                examDate = selectedDate,
                difficulty = difficultyLevel,
                importance = importanceLevel,
                color = finalColor
                )

            if (subjectName.isEmpty() || selectedDate.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력하세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                dao.insertSubject(subject)
                Log.d("DB_DEBUG", "과목 저장 완료: ${subject.name}, ${subject.examDate}")

                launch(Dispatchers.Main) {
                    Toast.makeText(this@AddActivity, "저장 완료!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    // 다이얼로그 생성 함수
    private fun showRatingDialog(title: String, onRated: (Float) -> Unit) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null)
        dialog.setContentView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)

        tvTitle.text = title

        btnConfirm.setOnClickListener {
            val selectedRating = ratingBar.rating
            onRated(selectedRating)
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showColorPicker(btnColor: ImageButton) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.color_sheet, null)

        val rv = view.findViewById<RecyclerView>(R.id.rvParentColors)
        rv.layoutManager = GridLayoutManager(this, 5)

        rv.adapter = ParentColorAdapter(allColors) { selected ->
            curSelectedColor = selected
            btnColor.setBackgroundColor(selected)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }




}
