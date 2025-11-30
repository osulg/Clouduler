package com.example.clouduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.clouduler.data.AppDatabase
import com.example.clouduler.data.SubjectEntity
import java.lang.Appendable

/* SubjectViewModel
 * - View와 RoomDB 사이 중간 역할 -> ViewModel
 * - 모든 과목 데이터를 LiveData 형태로 가짐 -> UI에서 관찰
 *
 * 사용 :
 * - ViewActivity
 *
 * 역할 :
 * - RoomDB에서 subject 목록을 불러와서 LiveData로 저장
 * - ViewActivity에서는 해당 LiveData를 observe -> 자동으로 리스트 업데이트
*/
class SubjectViewModel(application: Application) : AndroidViewModel(application) {
    // DAO 참조 : ROOM DB에 접근을 위한 객체
    private val dao = AppDatabase.getDatabase(application).subjectDao()

    /*
    * - 전체 과목 데이터를 RoomDB -> LiveData로 가져오기
    * - DB 변경? -> 자동으로 새로운 리스트 전달
    * - UI는 observe()만 하면 해결
    */
    val allSubjects : LiveData<List<SubjectEntity>> = dao.getAllSubjects()
}