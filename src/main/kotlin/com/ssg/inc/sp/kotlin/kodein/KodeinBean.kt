package com.ssg.inc.sp.kotlin.kodein

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class KodeinBean (
    val module: String = DEFAULT_MODULE,
    val tag: String = "",
    val bind: BindType = BindType.Singleton
)

enum class BindType {
    Singleton, Provider, EagerSingleton, Factory, Multiton, Instance, Constant
}
