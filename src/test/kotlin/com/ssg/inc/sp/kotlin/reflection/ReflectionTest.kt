package com.ssg.inc.sp.kotlin.reflection

import com.ssg.inc.sp.HelloTest
import com.ssg.inc.sp.kotlin.kodein.KodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinInject
import com.ssg.inc.sp.reflect.ReflectionUtils
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.test.assertEquals

@KodeinBean(module = "test")
class ReflectionTest {

    private val log = LoggerFactory.getLogger(this::class.java)
    private val h: HelloTest = HelloTest("TEST")


    @Test
    fun loadPackageTest() {
        val kodeinBeanClass = ReflectionUtils.findAllClassesUsingClassLoader("com.ssg.inc.sp")
            .map { it.kotlin }
            .filter { it.hasAnnotation<KodeinBean>() }
            .findAny().get()
        val kodeinBeanMeta = kodeinBeanClass.findAnnotation<KodeinBean>()
        println(kodeinBeanClass)
        assertEquals(kodeinBeanMeta!!.module, "test")

        kodeinBeanClass.constructors.forEach {
            println(it)
            it.parameters.forEach { x ->
                println(x)
                println(x.isOptional)
                val kodeinInject = x.findAnnotation<KodeinInject>()

                println(kodeinInject!!.tag)
                assertEquals(kodeinInject!!.tag, "test1")
            };
        }
    }
}