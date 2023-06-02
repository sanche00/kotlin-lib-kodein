package com.ssg.inc.sp.kotlin.kodein

import org.kodein.di.*
import org.kodein.type.TypeToken
import org.kodein.type.jvmType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType.Primitive
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmName


object KodeinBeanLoader {

    val logger: Logger = LoggerFactory.getLogger(KodeinBeanLoader::class.java)

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
                when (value) {
                    is String -> bindConstant<String>(tag) { value }
                    is Int -> bindConstant<Int>(tag) { value }
                    is Long -> bindConstant<Long>(tag) { value }
                    is Double -> bindConstant<Double>(tag) { value }
                    is Float -> bindConstant<Float>(tag) { value }
                    else -> bindConstant(tag) { value }
                }

            }
    }

    inline fun <reified T : Any> DI.Builder.loadConstantsBean(kClass: KClass<T>) {
        logger.info("load kodein constant bean for KClass : $kClass")
        val instance = kClass.createInstance()!!
        loadConstantsBean(instance)
    }

    inline fun <reified T : Any> DI.Builder.loadKodeinBean(parent: Any, kFunction: KFunction<T>, value: T? = null) {
        logger.info("load kodein bean for kFunction : $kFunction")
        if (!kFunction.hasAnnotation<KodeinBean>()) {
            throw Exception("not defined KodeinBean")
        }

        val beanMeta = kFunction.findAnnotation<KodeinBean>()!!
        logger.info("load kodein bean meta $beanMeta")

        when (beanMeta.bind) {
            BindType.Singleton -> {
                val ref = when (beanMeta.ref) {
                    Reference.None -> null
                    Reference.SoftReference -> softReference
                    Reference.WeakReference -> weakReference
                    Reference.ThreadLocal -> threadLocal
                }
                bind<T>(getTag(beanMeta, kFunction.returnType.classifier as KClass<*>?)) with singleton(
                    ref = ref,
                    sync = beanMeta.sync
                ) {
                    value ?: callFunction(
                        parent, kFunction
                    )
                }
            }

            BindType.Provider -> bind<T>(
                getTag(
                    beanMeta, kFunction.returnType.classifier as KClass<*>?
                )
            ) with provider { value ?: callFunction(parent, kFunction) }

            BindType.EagerSingleton -> bind<T>(
                getTag(
                    beanMeta, kFunction.returnType.classifier as KClass<*>?
                )
            ) with eagerSingleton { value ?: callFunction(parent, kFunction) }

            BindType.Factory -> bind<T>(
                getTag(
                    beanMeta, kFunction.returnType.classifier as KClass<*>?
                )
            ) with factory { value ?: callFunction(parent, kFunction) }

            BindType.Multiton -> bind<T>(
                getTag(
                    beanMeta, kFunction.returnType.classifier as KClass<*>?
                )
            ) with multiton { value ?: callFunction(parent, kFunction) }
//            BindType.Instance -> bind<T>(getTag(beanMeta)) with instance { _instance }
//            BindType.Constant -> {
//                bindConstant<T>(getTag(beanMeta, T::class)!!) { value ?: callFuntion(parent, kFunction) }
//            }
            else -> throw Exception("not yet")
        }
    }

    fun <T> DirectDI.callFunction(parent: Any, kFunction: KFunction<T>): T {

        if (kFunction.parameters.isEmpty()) {
            return kFunction.call(parent)
        }

        return kFunction.callBy(kFunction.parameters
            .map {
                it to if (it.name == null) parent else createArgument(it)
            }.filter { it.second != null }.toMap()
        )
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
                bind<T>(getTag(beanMeta, kClass)) with singleton(ref = ref, sync = beanMeta.sync) {
                    value ?: createInstance(
                        kClass
                    )
                }
            }

            BindType.Provider -> bind<T>(getTag(beanMeta, kClass)) with provider { value ?: createInstance(kClass) }
            BindType.EagerSingleton -> bind<T>(getTag(beanMeta, kClass)) with eagerSingleton {
                value ?: createInstance(
                    kClass
                )
            }

            BindType.Factory -> bind<T>(getTag(beanMeta, kClass)) with factory { value ?: createInstance(kClass) }
            BindType.Multiton -> bind<T>(getTag(beanMeta, kClass)) with multiton { (value ?: createInstance(kClass)) }
//            BindType.Instance -> bind<T>(getTag(beanMeta)) with instance { _instance }
            BindType.Constant -> {
                bindConstant<T>(getTag(beanMeta, kClass)!!) { value ?: kClass.createInstance() }
            }

            else -> throw Exception("not yet")
        }
    }

    inline fun <reified T : Any> DIAware.findKeyPair(tag: String? = null): Pair<Any?, TypeToken<out Any>> {
        val kClass = T::class
        var keyPair = if (!kClass.hasAnnotation<KodeinBean>()) {
            findKey { it.tag == tag ?: kClass.jvmName }
        } else {
            findKey { it.tag == getTag(kClass.findAnnotation<KodeinBean>()!!, kClass) }
        }
        return keyPair ?: findKey { it.type.jvmType.typeName.startsWith(kClass.jvmName) }
        ?: throw Exception("Bean 을 찾을 수 없습니다. $kClass)")
    }

    inline fun <reified T : Any> DIAware.inject(tag: String? = null) =
        findKeyPair<T>(tag).let {
            Instance(it.second, it.first) as LazyDelegate<T>
        }

    inline fun <reified T : Any> DIAware.directInject(tag: String? = null) =
        findKeyPair<T>(tag).let {
            direct.Instance(it.second, it.first) as T
        }

    inline fun DIAware.findKey(predicate: (DI.Key<*, *, *>) -> Boolean) =
        di.container.tree.let {
            it.bindings.keys.firstOrNull { key ->
                predicate(key)
            }?.let { key -> key.tag to key.type }
        }


    inline fun <reified T : Any> DIAware.injectConst(kodeinInject: KodeinInject): LazyDelegate<T> {
        return injectConst(kodeinInject.tag)
    }

    inline fun <reified T : Any> DIAware.injectConst(tag: String): LazyDelegate<T> {

        val key = di.container.tree.let {
            it.bindings.keys.first { key -> key.tag == tag }
        }
        return Instance(key.type, tag) as LazyDelegate<T>
    }

    fun <T : Any> DirectDI.createInstance(kClass: KClass<T>): T {
        if (kClass.constructors.size > 1) {
            throw Exception("too many constructor : ${kClass.constructors.size}")
        }

        try {
            val constructor = kClass.primaryConstructor!!
            if (constructor.parameters.isEmpty()) {
                return kClass.createInstance()
            }
            logger.info(constructor.returnType.toString())
            return constructor.callBy(constructor.parameters
                .map {
                    it to createArgument(it)
                }.filter { it.second != null }.toMap()
            )
        } catch (e: Exception) {
            throw Exception("createInstance Error : ${kClass}", e)
        }

    }

    private fun DirectDI.createArgument(parameter: KParameter): Any? {
        var ret : Any? = null
        if (!parameter.hasAnnotation<KodeinInject>()) {
            ret = notDefineInject(parameter)
        }else {
            val inject = parameter.findAnnotation<KodeinInject>()!!
            ret = find(if (inject.tag != "") inject.tag else null, parameter.type.classifier as KClass<*>)
        }

        if (ret != null) {
            return ret
        }
        if (parameter.isOptional) {
            return null
        }

        throw Exception("not find instance !! $parameter")
    }

    private fun DirectDI.notDefineInject(parameter: KParameter): Any? {
        if (parameter.type.classifier == org.kodein.di.DI::class) {
            return di
        }
        return find(parameter.name, parameter.type.classifier as KClass<*>)

    }

    fun <T : Any> DirectDI.find(tag: String?, type: KClass<T>): Any? {
        var ret: Any? = tag?.let { instanceOrNull(it) } ?: type?.jvmName?.let { instanceOrNull(it) }
        return ret ?: di.findKey { !type.isBaseType() && it.type.jvmType.typeName.startsWith(type.jvmName) }?.let {
            return Instance(it.second, it.first)
        } ?: throw Exception("Bean을 찾을수 없습니다. ${type.jvmName}")
    }

    fun getTag(beanMeta: KodeinBean, kClass: KClass<*>? = null): String? {
        if (beanMeta.tag == "") {
            return kClass?.jvmName
        }
        return beanMeta.tag
    }

}

val PrimitiveTypes = setOf(Int::class
    , Char::class
    , Byte::class
    , String::class
    , Double::class
    , Any::class
    , Float::class
    , Long::class
    , Short::class
)
private fun <T : Any> KClass<T>.isBaseType() = PrimitiveTypes.contains(this)