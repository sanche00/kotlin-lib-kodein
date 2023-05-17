package com.ssg.inc.sp.kotlin.kodein

import kotlin.reflect.KClass

@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE_PARAMETER,
)
@Retention(AnnotationRetention.SOURCE)
annotation class KodeinInject(
    val tag: String = "",
//    val type: KClass<*>
)
