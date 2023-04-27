package com.ssg.inc.sp.kotlin.kodein

import com.ssg.inc.sp.reflect.ReflectionUtils
import org.kodein.di.DI
import org.kodein.di.bindConstant
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.streams.toList

object KodeinBeanLoader {

    private fun MutableMap<String, MutableList<KodeinComponent>>.append(kodeinComponent: KodeinComponent) {
        val value = get(kodeinComponent.module) ?: mutableListOf()
        value.add(kodeinComponent)
        put(kodeinComponent.module, value)
    }


    private inline fun <reified T : Any> registerBean(di: DI.Builder, value: T, kodeinMeta: KodeinField) {
        di.bindConstant(tag = kodeinMeta.tag) { value }
    }

    private inline fun <reified T : Any> registerBean(di: DI.Builder, value: T, kodeinMeta: KodeinFunction) {
        di.bindConstant(tag = kodeinMeta.tag) { value }
    }

    private inline fun <reified T : Any> registerBean(di: DI.Builder, kClass: KClass<T>, kodeinMeta: KodeinBean) {
        when (kodeinMeta.bind) {
            BindType.Constant -> {
                di.bindConstant(tag = kodeinMeta.tag) {}
            }
            else -> {

            }
        }
    }

    fun loadPackage(basePackage: String) {
        val kodeinMap = mutableMapOf<String, MutableList<KodeinComponent>>()

        val classes = ReflectionUtils.findAllClasses(basePackage)
            .map { it.kotlin }
            .filter { it.simpleName != null }.toList();
        loadKodeinBeans(classes, kodeinMap)
        loadKodeinBeansByKodeinConfiguration(classes, kodeinMap)
    }

    private inline fun <reified T : Any> loadKodeinBeans (
        classes: Stream<KClass<Any>>,
        kodeinMap: MutableMap<String, MutableList<KodeinComponent>>
    ) {
        val configClasses = classes.filter { it.hasAnnotation<KodeinConfiguration>() }.toList()
        loadKodeinFields(configClasses, kodeinMap)
        loadKodeinFunctions(configClasses, kodeinMap)
    }


    private fun loadKodeinBeansByKodeinConfiguration (
        classes: List<KClass<Any>>,
        kodeinMap: MutableMap<String, MutableList<KodeinComponent>>
    ) {
        val configClasses = classes.filter { it.hasAnnotation<KodeinConfiguration>() }.toList()
        loadKodeinFields(configClasses, kodeinMap)
        loadKodeinFunctions(configClasses, kodeinMap)
    }


    private fun loadKodeinFunctions(
        configClasses: List<KClass<Any>>,
        kodeinMap: MutableMap<String, MutableList<KodeinComponent>>
    ) {
        configClasses.stream()
            .flatMap { it.declaredFunctions.stream() }
            .filter { it.hasAnnotation<KodeinFunction>() }
            .forEach {
                val meta = it.findAnnotation<KodeinFunction>()!!
                kodeinMap.append(KodeinComponent.createKodeinComponent(meta, it))
            }
    }

    private fun loadKodeinFields(
        configClasses: List<KClass<Any>>,
        kodeinMap: MutableMap<String, MutableList<KodeinComponent>>
    ) {
        configClasses.stream()
            .flatMap { it.declaredMemberProperties.stream() }
            .filter { it.hasAnnotation<KodeinField>() }
            .forEach {
                val meta = it.findAnnotation<KodeinField>()!!
                kodeinMap.append(KodeinComponent.createKodeinComponent(meta, it))
            }
    }

    private fun loadKodeinBeans(
        classes: List<KClass<Any>>,
        kodeinMap: MutableMap<String, MutableList<KodeinComponent>>
    ) {
        classes.filter {
            it.hasAnnotation<KodeinBean>()
        }.forEach {
            val meta = it.findAnnotation<KodeinBean>()!!
            kodeinMap.append(KodeinComponent.createKodeinComponent(meta, it))
        }
    }
}