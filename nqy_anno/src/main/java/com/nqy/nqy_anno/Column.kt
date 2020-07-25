package com.nqy.nqy_anno

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Column(val defValue: String = "", val clear: Boolean = true)