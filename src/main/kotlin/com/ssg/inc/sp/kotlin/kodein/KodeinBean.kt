package com.ssg.inc.sp.kotlin.kodein

import kotlin.reflect.KClass

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class KodeinBean (
    val tag: String = "",
    val bind: BindType = BindType.Singleton,
    val sync:Boolean = true,
    val ref:Reference = Reference.None,
    val type:KClass<*> = Any::class
)

enum class Reference {
    None, SoftReference, WeakReference, ThreadLocal
}
enum class BindType {
    Singleton, Provider, EagerSingleton, Factory, Multiton, Instance, Constant
}
