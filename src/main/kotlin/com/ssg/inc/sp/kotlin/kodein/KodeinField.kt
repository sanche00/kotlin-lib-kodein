package com.ssg.inc.sp.kotlin.kodein

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class KodeinField(
    val module: String = "",
    val tag: String = "",
)

