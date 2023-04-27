package com.ssg.inc.sp.kotlin.kodein

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1

class KodeinComponent private constructor(
    val kodeinMeta: Annotation,
    val value: Any,
    val module: String,
    val tag: String
) {

    companion object {
        fun createKodeinComponent(kodeinField: KodeinField, property: KProperty1<*, *>): KodeinComponent {
            return KodeinComponent(kodeinField, property, "", "");
        }

        fun <T : Any> createKodeinComponent(kodeinBean: KodeinBean, kClass: KClass<T>): KodeinComponent {
            return KodeinComponent(kodeinBean, kClass, "", "");
        }

        fun createKodeinComponent(kodeinFunction: KodeinFunction, function: KFunction<*>): KodeinComponent {
            return KodeinComponent(kodeinFunction, function, "", "");
        }
    }
}

