package com.ssg.inc.sp.kotlin.kodein


@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE_PARAMETER,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class KodeinInject(
    val tag: String = "",
//    val type: KClass<*>
)
