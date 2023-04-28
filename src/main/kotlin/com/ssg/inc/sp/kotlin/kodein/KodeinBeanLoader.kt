package com.ssg.inc.sp.kotlin.kodein

import com.ssg.inc.sp.reflect.ReflectionUtils
import org.kodein.di.DI
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.stream.Stream
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.streams.toList

object KodeinBeanLoader {

    private val logger: Logger = LoggerFactory.getLogger(KodeinBeanLoader::class.java)
    private val cache = mutableMapOf<KClass<*>, Any>()

    private val kodeinBeanRegister : KodeinBeanRegister = KodeinBeanRegister()

    private fun getCache(kClass: KClass<*>) {
        if (cache.containsKey(kClass)) {
            cache[kClass]
        } else {
            cache[kClass] = kClass.createInstance()
            cache[kClass]
        }
    }

    fun createKodeinModules(kodeinMap: MutableMap<String, MutableList<KodeinComponent>>): List<DI.Module> {
        sortedMapOfBeanReference(kodeinMap)
        return emptyList()
    }

    fun sortedMapOfBeanReference(kodeinMap: MutableMap<String, MutableList<KodeinComponent>>): Stream<Pair<String, List<KodeinComponent>>> {
        return Stream.of()
    }

    fun MutableMap<String, MutableList<KodeinComponent>>.append(kodeinComponent: KodeinComponent) {
        val value = get(kodeinComponent.module) ?: mutableListOf()
        value.add(kodeinComponent)
        put(kodeinComponent.module, value)
    }



    fun loadKodeinMapByPackage(basePackage: String): MutableMap<String, MutableList<KodeinComponent>> {

        val classes = ReflectionUtils.findAllClasses(basePackage)
            .map { it.kotlin }
            .filter { it.simpleName != null }.toList();

        return loadKodeinMapByClasses(classes)
    }

    fun loadKodeinMapByClasses(classes: List<KClass<Any>>): MutableMap<String, MutableList<KodeinComponent>> {

        val kodeinMap = mutableMapOf<String, MutableList<KodeinComponent>>()
        loadKodeinBeans(classes, kodeinMap)
        loadKodeinBeansByKodeinConfigurations(classes, kodeinMap)
        return kodeinMap
    }

    fun loadKodeinBeansByKodeinConfigurations(
        classes: List<KClass<*>>,
        kodeinMap: MutableMap<String, MutableList<KodeinComponent>>
    ) {
        val configClasses = classes.filter { it.hasAnnotation<KodeinConfiguration>() }.toList()
        configClasses.stream()
            .forEach { loadKodeinBeansByKodeinConfiguration(it, it.declaredFunctions.stream(), kodeinMap) }
        configClasses.stream()
            .forEach { loadKodeinBeansByKodeinConfiguration(it, it.declaredMemberProperties.stream(), kodeinMap) }
    }

    private inline fun <reified T : KCallable<*>> loadKodeinBeansByKodeinConfiguration(
        configClass: KClass<*>,
        stream: Stream<T>,
        kodeinMap: MutableMap<String, MutableList<KodeinComponent>>
    ) {
        stream.filter { it.hasAnnotation<KodeinBean>() }
            .forEach {
                kodeinMap.append(
                    KodeinComponent.createKodeinComponent(
                        it.findAnnotation<KodeinBean>()!!,
                        it,
                        getCache(configClass)
                    )
                )
            }
    }

    fun loadKodeinBeans(
        classes: List<KClass<*>>,
        kodeinMap: MutableMap<String, MutableList<KodeinComponent>>
    ) {
        classes.filter {
            it.hasAnnotation<KodeinBean>()
        }.forEach {
            kodeinMap.append(KodeinComponent.createKodeinComponent(it.findAnnotation<KodeinBean>()!!, it))
        }
    }
}