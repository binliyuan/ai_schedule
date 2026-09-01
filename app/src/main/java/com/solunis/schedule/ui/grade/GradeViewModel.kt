package com.solunis.schedule.ui.grade

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class GradeViewModel(application: Application) : AndroidViewModel(application) {

    private val _grades = MutableLiveData<List<GradeItem>>(emptyList())
    val grades: LiveData<List<GradeItem>> = _grades

    data class GradeItem(
        val courseName: String,
        val score: String,
        val credit: Float,
        val semester: String
    )
}
