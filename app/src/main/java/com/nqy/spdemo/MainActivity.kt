package com.nqy.spdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv.text = "name:${UserSP.name},age:${UserSP.age}"

        btn_name.setOnClickListener {
            val name = et_name.text.toString()
            UserSP.name = name

        }

        btn_age.setOnClickListener {
            val age = et_age.text.toString()
            UserSP.age = age.toInt()
        }

    }
}
