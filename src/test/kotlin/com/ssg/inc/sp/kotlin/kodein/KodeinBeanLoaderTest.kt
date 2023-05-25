package com.ssg.inc.sp.kotlin.kodein

import com.ssg.inc.sp.HelloTest
import com.ssg.inc.sp.HelloTestConfig
import com.ssg.inc.sp.KodeinDIAware
import com.ssg.inc.sp.KodeinDIAwareAuto
import com.ssg.inc.sp.kotlin.kodein.KodeinBeanLoader.loadConstantsBean
import com.ssg.inc.sp.kotlin.kodein.KodeinBeanLoader.loadKodeinBean
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.kodein.di.DI
import org.kodein.di.instance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmName

class KodeinBeanLoaderTest {


    @Test
    fun loadKodeinBean() {
        val module = DI.Module("test") {
            loadConstantsBean(HelloTestConfig::class)
            loadKodeinBean(HelloTest::class)
            loadKodeinBean(KodeinDIAware::class)
            loadKodeinBean(KodeinDIAwareAuto::class)
            loadKodeinBean(HelloTestConfig(), HelloTestConfig::testBean)
        }
        val di = DI {
            import(module)
        }
        val helloTest by di.instance<HelloTest> ( "test" )
        assertEquals(helloTest.text(), "test1")
        val kodeinDIAware by di.instance<KodeinDIAware> (tag = KodeinDIAware::class.jvmName)
        assertEquals(kodeinDIAware.getText(), "test1")

        val kodeinDIAwareAuto by di.instance<KodeinDIAwareAuto> (tag = KodeinDIAwareAuto::class.jvmName)
        assertEquals(kodeinDIAwareAuto.getText(), "test1")
        assertEquals(kodeinDIAwareAuto.test3, "test1")
        assertEquals(kodeinDIAwareAuto.test2, 10)

        val testBean by di.instance<HelloTestConfig.TestBean> ("test4")
        assertEquals(testBean.test2, 10)

    }

}