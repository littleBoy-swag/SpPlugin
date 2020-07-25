package com.nqy.spdemo

import com.nqy.nqy_anno.Column
import com.nqy.nqy_anno.Entity


@Entity(Sp = "com.nqy.spdemo.SpUtil.sp")
data class User(
    @Column(defValue = "小明")
    val name: String,
    @Column(defValue = "12")
    val age: Int
)