package com.ssg.inc.sp.kotlin.kodein

@Target(
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class KodeinBean (
    val module: String = "",
    val tag: String = "",
    val bind: BindType = BindType.Singleton
)

enum class BindType {
    Singleton, Provider, EagerSingleton, Factory, Multiton, Instance, Constant
}
