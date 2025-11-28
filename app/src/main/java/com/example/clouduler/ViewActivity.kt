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


class ViewActivity : AppCompatActivity() {
    private val viewModel: SubjectViewModel by viewModels()
    private lateinit var adapter: SubjectAdapter
    private var subjectID: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        recyclerView.layoutManager= LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        adapter = SubjectAdapter(
            mutableListOf(),
            onUpdateClick = { subject ->
                val intent = Intent(this, AddActivity::class.java)
                intent.putExtra("subjectID", subject.id)
                startActivity(intent)
            },
            onDeleteClick = { subject ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getDatabase(this@ViewActivity).subjectDao()
                    dao.delete(subject)
                }
            },
            onItemClick = { subject ->
                val intent = Intent(this, SubjectDetailActivity::class.java)
                intent.putExtra("subjectID", subject.id)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        viewModel.allSubjects.observe(this, Observer { subjects ->
            adapter.updateData(subjects)
        })
    }
}