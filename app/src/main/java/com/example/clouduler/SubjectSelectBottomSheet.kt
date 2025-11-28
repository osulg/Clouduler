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

class SubjectSelectBottomSheet(
    private val onSubjectSelected: (Int?) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnNoSubject: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.bottom_subject_select, container, false)

        recyclerView = v.findViewById(R.id.subjectRecycler)
        btnNoSubject = v.findViewById(R.id.btnNoSubject)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadSubjectList()

        btnNoSubject.setOnClickListener {
            onSubjectSelected(null)
            dismiss()
        }

        return v
    }

    private fun loadSubjectList() {
        val dao = AppDatabase.getDatabase(requireContext()).subjectDao()

        dao.getAllSubjects().observe(viewLifecycleOwner) { list ->
            recyclerView.adapter = SubjectSelectAdapter(list) { subjectId ->
                onSubjectSelected(subjectId)
                dismiss()
            }
        }
    }
}
