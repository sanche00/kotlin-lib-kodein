package com.ssg.inc.sp.kotlin.kodein

@Target(
    AnnotationTarget.PROPERTY
)
@Retention(AnnotationRetention.RUNTIME)
annotation class KodeinField(
    val module: String = "",
    val tag: String = "",
)

