package com.example.clouduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.SubjectEntity
import java.lang.Appendable

class SubjectViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).subjectDao()
    val allSubjects : LiveData<List<SubjectEntity>> = dao.getAllSubjects()
}