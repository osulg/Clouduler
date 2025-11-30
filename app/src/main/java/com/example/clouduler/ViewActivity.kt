package com.example.clouduler

import com.example.clouduler.viewmodel.SubjectViewModel
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.SubjectAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.Observer
import androidx.activity.viewModels

/* ViewActivity
 * - 전체 과목 목록을 리스트 형식으로 보여주는 화면
 * - 수정 버튼 -> AddActivity로 이동 + 기존 데이터 수정
 * - 삭제 버튼 -> RoomDB에서 해당 과목 삭제
 * - 리스트 항목 클릭 -> SubjectDetailActivity 이동 (상세 정보)
 * - ViewModel을 사용해 LiveData 기반 UI 업데이트
*/
class ViewActivity : AppCompatActivity() {
    private val viewModel: SubjectViewModel by viewModels() // LiveData를 이용한 ViewModel
    private lateinit var adapter: SubjectAdapter // RecyclerView Adapter
    private var subjectID: Int = -1 // 클릭된 subject ID 저장용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // 뒤로가기 버튼 클릭 -> MainActivity
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ----- RecyclerView ----- 구성
        recyclerView.layoutManager= LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // ----- Adapter 설정 ----- //
        adapter = SubjectAdapter(
            mutableListOf(),
            // 수정버튼 클릭
            onUpdateClick = { subject ->
                // 과목 수정 화면으로 이동 (기존 ID 전달)
                val intent = Intent(this, AddActivity::class.java)
                intent.putExtra("subjectID", subject.id)
                startActivity(intent)
            },
            // 삭제버튼 클릭
            onDeleteClick = { subject ->
                // DB에서 해당 항목 삭제
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getDatabase(this@ViewActivity).subjectDao()
                    dao.delete(subject)
                }
            },
            // 리스트 항목 클릭
            onItemClick = { subject ->
                // 상세 정보 화면으로 이동
                val intent = Intent(this, SubjectDetailActivity::class.java)
                intent.putExtra("subjectID", subject.id)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        // ----- ViewModel에서 LiveData 관찰 ----- //
        // - allSubjects가 변경? -> UI 자동 갱신
        // - 삭제/추가/수정 후에도 반영
        viewModel.allSubjects.observe(this, Observer { subjects ->
            adapter.updateData(subjects)
        })
    }
}