package com.anwesh.uiprojects.kotlinlinkedordertrackerview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linkedordertrackerview.LinkedOrderTrackerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinkedOrderTrackerView.create(this)
    }
}
