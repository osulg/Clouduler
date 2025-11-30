package com.example.clouduler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.SubjectSelectAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/* SubjectSelectBottomSheet
 * 타이머 실행 전, 공부 시간을 어떤 과목을 학습할지 선택하는 BottomSheet
 *
 * 위치:
 * TimerModeActivity → btnTimerSubject 클릭하면 나옴
 */
class SubjectSelectBottomSheet(
    // 사용자가 어떤 과목을 선택했는지 알려줌
    private val onSubjectSelected: (Int?) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView // 과목 목록 리스트
    private lateinit var btnNoSubject: Button // 과목 없음 버튼

    // Bottimsheet가 처음 표시될 때 레이아웃을 inflate 
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // bottom_subject_select.xml을 화면에 가져옴
        val v = inflater.inflate(R.layout.bottom_subject_select, container, false)

        // View 초기화
        recyclerView = v.findViewById(R.id.subjectRecycler)
        btnNoSubject = v.findViewById(R.id.btnNoSubject)

        // RecyclerView 세팅
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // DB에서 과목 목록 로딩
        loadSubjectList()

        // 과목 없음 버튼
        // null 전달 후 닫기
        btnNoSubject.setOnClickListener {
            onSubjectSelected(null)
            dismiss()
        }

        return v
    }

    // Room DB에서 모든 과목 불러와 목록 표기
    // LiveData로 observe ->DB 변경? 자동 반영
    private fun loadSubjectList() {
        val dao = AppDatabase.getDatabase(requireContext()).subjectDao()

        // LiveData 관찰
        dao.getAllSubjects().observe(viewLifecycleOwner) { list ->
            recyclerView.adapter = SubjectSelectAdapter(list) { subjectId ->
                onSubjectSelected(subjectId) // 특정 과목 클릭 -> 전달
                dismiss() // 닫기
            }
        }
    }
}
