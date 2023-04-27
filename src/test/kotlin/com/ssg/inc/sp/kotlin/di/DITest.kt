package com.ssg.inc.sp.kotlin.di

import com.ssg.inc.sp.HelloTest
import com.ssg.inc.sp.kotlin.kodein.KodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinConfiguration
import com.ssg.inc.sp.kotlin.kodein.KodeinField
import com.ssg.inc.sp.reflect.ReflectionUtils
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.eagerSingleton
import org.kodein.di.instance
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.streams.toList
import kotlin.test.assertEquals

class DITest {

    private inline fun <reified T : Any> registerBean(di: DI.Builder, kclass: KClass<T>) {
        val a: T = kclass.createInstance()
        di.bind<T>("test1") with di.eagerSingleton { kclass.createInstance() }
    }

    fun DI.MainBuilder.injection(module: DI.Module) {
        import(module, allowOverride = true)
    }

    @Test
    fun dynamicBeanTest() {
        val test = DI.Module("TEST") {
            registerBean(this, HelloTest::class)
        }


        val kodein = DI {
            injection(test)
        }

        val ret by kodein.instance<HelloTest>("test1")
        assertEquals(ret.text(), "TEST")
    }

    @Test
    fun loadBeanTest() {
        val kodeinBeanClasses = ReflectionUtils.findAllClassesUsingClassLoader("com.ssg.inc.sp.kotlin.di")
            .map { it.kotlin }
            .filter { it.simpleName != null }
            .filter {
                it.hasAnnotation<KodeinBean>()
            }
            .toList()
        assertEquals(kodeinBeanClasses.size, 2)
        kodeinBeanClasses.forEach {
            println(it)
        }

        for (kodeinBean in kodeinBeanClasses) {
            val kodeinMeta = kodeinBean.findAnnotation<KodeinBean>()
            assertEquals(kodeinMeta!!.module, "test")
            assertEquals(kodeinBean.constructors.size, 1)
            kodeinBean.constructors.first().parameters.forEach { println(it) }
        }
    }

    @Test
    fun loadPropertyTest() {
        val kodeinBeanClasses = ReflectionUtils.findAllClassesUsingClassLoader("com.ssg.inc.sp.kotlin.di")
            .map { it.kotlin }
            .filter { it.simpleName != null }
            .filter {
                it.hasAnnotation<KodeinConfiguration>()
            }
            .toList()
        assertEquals(kodeinBeanClasses.size, 1)
        val temp = kodeinBeanClasses.first().createInstance();
        kodeinBeanClasses.first().declaredMemberProperties
            .map{
                it
            }
            .filter { it.hasAnnotation<KodeinField>() }.forEach {
                println(it)
                val meta = it.findAnnotation<KodeinField>()!!
                if(meta.tag == "test") {
                    if(!it.isAccessible) {
                        it.javaField?.trySetAccessible()
                    }
                    assertEquals("test", it.get(temp))
                }
            }
    }
}