package com.ssg.inc.sp.kotlin.kodein

import com.ssg.inc.sp.HelloTest
import com.ssg.inc.sp.HelloTestConfig
import com.ssg.inc.sp.kotlin.di.KodeinConfig
import com.ssg.inc.sp.kotlin.di.KodeinTestBean
import com.ssg.inc.sp.kotlin.di.TypeParam
import com.ssg.inc.sp.kotlin.kodein.KodeinBeanLoader.loadConstantsBean
import com.ssg.inc.sp.kotlin.kodein.KodeinBeanLoader.loadKodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinBeanLoader.append
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.kodein.di.*
import kotlin.reflect.KClass

class KodeinBeanLoaderTest {

    private val kClasses: List<KClass<*>> = listOf(KodeinConfig::class, KodeinTestBean::class, TypeParam::class)
    private val map = mutableMapOf<String, MutableList<KodeinComponent>>()

    @Test
    fun loadBeanTest() {
        val module = DI.Module("test") {
            loadKodeinBean<HelloTest>(HelloTest::class)
        }

        DI {
            import(module)
        }
    }

    @Test
    fun mutableMapAppendTest() {

        val value = KodeinComponent.createKodeinComponent(
            KodeinBean(
                module = DEFAULT_MODULE, tag = "", bind = BindType.Constant
            ), Any::class
        )
        map.append(value)
        assertTrue(map.containsKey(DEFAULT_MODULE))
        val values = map[DEFAULT_MODULE]
        assertEquals(values?.size, 1)
        assertEquals(values?.first(), value)
    }

    @Test
    fun loadKodeinBeansByKodeinConfigurationsTest() {
        KodeinBeanLoader.loadKodeinBeansByKodeinConfigurations(kClasses, map);
        assertEquals(map.size, 1)
        assertTrue(map.containsKey(DEFAULT_MODULE))
        val values = map[DEFAULT_MODULE]!!
        assertEquals(values.size, 2)
        values.forEach {
            assertEquals(it.tag, "test")
        }
    }

    @Test
    fun createKodeinComponentTest() {
        KodeinBeanLoader.loadKodeinBeans(kClasses, map);
        assertEquals(map.size, 1)
        assertTrue(map.containsKey("test"))
        val values = map["test"]!!
        assertEquals(values.size, 2)
        values.forEach {
            println(it)
        }
    }

    @Test
    fun loadKodeinBean() {
        val module = DI.Module("test11111") {
            loadConstantsBean(HelloTestConfig::class)
            loadKodeinBean(HelloTest::class)
        }
        val di = DI {
            import(module)
        }
        val ret by di.instance<HelloTest> ( "test" )

        assertEquals(ret.text(), "test1")
    }
}