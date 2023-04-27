package com.ssg.inc.sp.kotlin.kodein

@Target(
    AnnotationTarget.FUNCTION,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class KodeinFunction (
    val module: String = "",
    val tag: String = "",
    val bind: BindType = BindType.Singleton
)
