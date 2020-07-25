package com.nqy.nqy_anno

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Entity(
    val Sp: String = "\"you must Provide a code to get a SharedPreferences instance\"",
    val name: String = ""
)