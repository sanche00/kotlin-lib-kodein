package com.ssg.inc.sp.kotlin.kodein

import org.kodein.di.*
import org.kodein.type.TypeToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


object KodeinBeanLoader {

    public val logger: Logger = LoggerFactory.getLogger(KodeinBeanLoader::class.java)

    private inline fun <reified T : Any> DI.Builder.loadConstantBean(kClass: KClass<T>, tag: String, value: Any) {
        bindConstant(tag) { value }
    }

    inline fun <T : Any> DI.Builder.loadConstantsBean(config: T) {
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
//                constant(if (meta.tag == "") it.name else meta.tag) with { value }
                val tag = if (meta.tag == "") it.name else meta.tag
                logger.info("bind constant $tag, $value")
//                this.loadConstantBean(kClass = meta.type, tag = tag, value = value)
                bindConstant(tag) { value }
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
            BindType.Singleton -> {
                val ref = when (beanMeta.ref) {
                    Reference.None -> null
                    Reference.SoftReference -> softReference
                    Reference.WeakReference -> weakReference
                    Reference.ThreadLocal -> threadLocal
                }
                bind<T>(getTag(beanMeta)) with singleton(ref = ref, sync = beanMeta.sync) {
                    value ?: createInstance(
                        kClass
                    )
                }
            }

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

    inline fun <reified T : Any> DIAware.inject(tag: String? = null): org.kodein.di.LazyDelegate<T> {
        val kClass = T::class
        if (!kClass.hasAnnotation<KodeinBean>()) {
            return instance(tag)
        }
        val meta = kClass.findAnnotation<KodeinBean>()!!
        return instance(getTag(meta, kClass))
    }

    inline fun <reified T : Any> DIAware.injectConst(tag: String): org.kodein.di.LazyDelegate<T> {
        val key = di.container.tree.let {
            it.bindings.keys.first { key -> key.tag == tag }
        }
        return Instance(key.type, tag) as LazyDelegate<T>
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