package com.ssg.inc.sp.kotlin.kodein

@Target(
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class KodeinConfiguration(
    val basePackages:Array<String> = []
)

