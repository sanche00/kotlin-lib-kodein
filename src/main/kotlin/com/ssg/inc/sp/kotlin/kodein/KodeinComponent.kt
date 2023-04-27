package com.ssg.inc.sp.kotlin.kodein

import kotlin.reflect.KCallable
import kotlin.reflect.KClass

const val DEFAULT_MODULE = "MAIN_KODEIN_BEANS"

class KodeinComponent private constructor(
    val kodeinMeta: KodeinBean,
    val kReflect: Any,
    val configObj: Any = Any(),
    val module: String,
    val tag: String
) {

    companion object {
        fun <T : KCallable<*>> createKodeinComponent(
            kodeinBean: KodeinBean,
            property: T,
            config: Any
        ): KodeinComponent {
            return KodeinComponent(
                kodeinBean,
                property,
                config,
                kodeinBean.module,
                kodeinBean.tag.ifBlank { property.name }
            );
        }

        fun createKodeinComponent(
            kodeinBean: KodeinBean,
            kClass: KClass<*>
        ): KodeinComponent {
            return KodeinComponent(
                kodeinMeta = kodeinBean,
                kReflect = kClass,
                module = kodeinBean.module,
                tag = kodeinBean.tag.ifBlank { kClass.simpleName!! }
            );
        }

    }
}

