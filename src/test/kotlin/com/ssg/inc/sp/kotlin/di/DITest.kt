package com.ssg.inc.sp.kotlin.di

import com.ssg.inc.sp.HelloTest
import org.junit.jupiter.api.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.eagerSingleton
import org.kodein.di.instance
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
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

}