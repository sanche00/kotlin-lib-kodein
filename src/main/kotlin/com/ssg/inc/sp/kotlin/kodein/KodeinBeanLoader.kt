package com.ssg.inc.sp.kotlin.kodein

import com.ssg.inc.sp.kotlin.kodein.KodeinBeanLoader.createInstance
import com.ssg.inc.sp.reflect.ReflectionUtils
import org.kodein.di.*
import org.kodein.di.bindings.NoArgBindingDI
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.stream.Stream
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.streams.toList


object KodeinBeanLoader {

    public val logger: Logger = LoggerFactory.getLogger(KodeinBeanLoader::class.java)
    private val cache = mutableMapOf<KClass<*>, Any>()

    private val kodeinBeanRegister: KodeinBeanRegister = KodeinBeanRegister()

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

    inline fun <reified T : Any> DI.Builder.loadConstantsBean(config: T) {
        val kClass = config::class
        kClass.declaredMemberProperties
            .filter { it.hasAnnotation<KodeinBean>() }
            .forEach {
                val meta = it.findAnnotation<KodeinBean>()!!
                val value = if (!it.isAccessible) {
                    it.javaField!!.trySetAccessible()
                    it.javaField!!.get(config)
                } else {
                    it.get(config as Nothing)
                    it.javaField!!.get(config)
                }
                if (value == null) {
                    throw Exception("constant bean 의 값은 null 일 수 없습니다.")
                }
                bindConstant(if (meta.tag == "") it.name else meta.tag) { value }
            }
    }

    inline fun <reified T : Any> DI.Builder.loadConstantsBean(kClass: KClass<T>) {
        logger.info("load kodein constant bean for KClass : $kClass")
        val instance = kClass.createInstance()!!
        loadConstantsBean(instance)
    }

    inline fun <reified T : Any> DI.Builder.loadKodeinBean(kClass: KClass<T>, value: T? = null) {
        logger.info("load kodein bean for KClass : $kClass")
        if (!kClass.hasAnnotation<KodeinBean>()) {
            throw Exception("not defined KodeinBean")
        }

        val beanMeta = kClass.findAnnotation<KodeinBean>()!!
        logger.info("load kodein bean meta $beanMeta")

        when (beanMeta.bind) {
            BindType.Singleton -> bind<T>(getTag(beanMeta)) with singleton { value ?: createInstance(kClass) }
            BindType.Provider -> bind<T>(getTag(beanMeta)) with provider { value ?: createInstance(kClass) }
            BindType.EagerSingleton -> bind<T>(getTag(beanMeta)) with eagerSingleton { value ?: createInstance(kClass) }
            BindType.Factory -> bind<T>(getTag(beanMeta)) with factory { value ?: createInstance(kClass) }
            BindType.Multiton -> bind<T>(getTag(beanMeta)) with multiton { value ?: createInstance(kClass) }
//            BindType.Instance -> bind<T>(getTag(beanMeta)) with instance { _instance }
            BindType.Constant -> {
                bindConstant<T>(getTag(beanMeta, kClass)!!) { value ?: kClass.createInstance() }
            }
            else -> throw Exception("not yet")
        }

    }

    fun <T : Any> org.kodein.di.DirectDI.createInstance(kClass: KClass<T>): T {
        if (kClass.constructors.size > 1) {
            throw Exception("too many constructor : ${kClass.constructors.size}")
        }
        val constructor = kClass.primaryConstructor!!
        if (constructor.parameters.isEmpty()) {
            return kClass.createInstance()
        }

//        return constructor.callBy(constructor.parameters.associateWith { createArgument(it) })
        return constructor.callBy(constructor.parameters
            .map {
                it to createArgument(it)
            }.filter { it.second != null }.toMap()
        )
    }

    private fun org.kodein.di.DirectDI.createArgument(parameter: KParameter): Any? {
        if (!parameter.hasAnnotation<KodeinInject>()) {
            return notDefineInject(parameter)
        }
        val inject = parameter.findAnnotation<KodeinInject>()!!

        return find(if (inject.tag != "") inject.tag else null, parameter.type.classifier as KClass<*>)
    }

    private fun org.kodein.di.DirectDI.notDefineInject(parameter: KParameter): Any? {
        if (parameter.type.classifier == org.kodein.di.DI::class) {
            return di
        }
        val ret = find(parameter.name, parameter.type.classifier as KClass<*>)
        if (ret != null) {
            return ret
        }
        if (parameter.isOptional) {
            return null
        }
        throw Exception("not find instance !!")
    }

    private fun <T : Any> org.kodein.di.DirectDI.find(tag: String?, type: KClass<T>): Any? {
        var ret: Any? = instanceOrNull(tag)
        if (ret == null) {
            ret = instanceOrNull()
        }
        return ret
    }

    fun getTag(beanMeta: KodeinBean, kClass: KClass<*>? = null): String? {
        if (beanMeta.tag == "") {
            return kClass?.simpleName
        }
        return beanMeta.tag
    }


}