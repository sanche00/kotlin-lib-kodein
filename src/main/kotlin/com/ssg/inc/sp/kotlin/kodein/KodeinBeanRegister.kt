package com.ssg.inc.sp.kotlin.kodein

import org.kodein.di.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.beans.BeanProperty
import java.util.stream.Collectors
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

internal class KodeinBeanRegister {

    val logger: Logger = LoggerFactory.getLogger(KodeinBeanLoader::class.java)

    fun registerBean(di: DI.Builder, kodeinComponent: KodeinComponent) {
        when (kodeinComponent.kReflect) {
            is KFunction<*> -> registerBeanByFunction(di, kodeinComponent, kodeinComponent.kReflect)
            is KProperty1<*, *> -> registerBeanByProperty(di, kodeinComponent, kodeinComponent.kReflect)
            is KClass<*> -> registerBeanByClass(di, kodeinComponent, kodeinComponent.kReflect)
            else -> throw Exception("not supported auto bind bean ${kodeinComponent.kReflect}")
        }

    }

    fun registerBeanByClass(di: DI.Builder, kodeinComponent: KodeinComponent, kReflect: KClass<*>) {
        val meta = kodeinComponent.kodeinMeta
        when(meta.bind){
            BindType.Singleton -> TODO()
            BindType.Provider -> TODO()
            BindType.EagerSingleton -> TODO()
            BindType.Factory -> TODO()
            BindType.Multiton -> TODO()
            BindType.Instance -> TODO()
            BindType.Constant -> TODO()
        }
    }

    private inline fun <reified T : Any>  bindSingleton(di: DI.Builder, kodeinComponent: KodeinComponent, kReflect: KClass<T>) {
//        di.bind<T>(tag = kodeinComponent.tag) with di.singleton{ kReflect.constructors.first().callBy(kReflect.constructors.first().parameters.stream().map { it to Any()}.collect(Collectors.groupingBy { Pair::first })) }
    }

    fun registerBeanByFunction(di: DI.Builder, kodeinComponent: KodeinComponent, function: KFunction<*>) {
        throw Exception("not yet supported function beans")
    }

    fun registerBeanByProperty(di: DI.Builder, kodeinComponent: KodeinComponent, property: KProperty1<*, *>) {
        val meta = kodeinComponent.kodeinMeta
        if (meta.bind != BindType.Constant) {
            logger.warn("Property Bean is Only Constant")
        }
        val value = if (!property.isAccessible) {
            property.javaField!!.trySetAccessible()
            property.get(kodeinComponent.configObj as Nothing)
        } else {
            property.get(kodeinComponent.configObj as Nothing)
        }
        if (value != null) {
            registerBeanByProperty(di, kodeinComponent, value)
        } else {
            throw Exception("Property bean is null : ${property}!")
        }
    }

    private inline fun <reified T : Any> registerBeanByProperty(
        di: DI.Builder,
        kodeinComponent: KodeinComponent,
        type: T
    ) {
        di.bindConstant<T>(kodeinComponent.tag) { type }
    }
}