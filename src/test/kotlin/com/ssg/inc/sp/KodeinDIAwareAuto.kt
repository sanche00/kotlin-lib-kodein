package com.ssg.inc.sp

import com.ssg.inc.sp.kotlin.kodein.KodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinBeanLoader.inject
import com.ssg.inc.sp.kotlin.kodein.KodeinInject
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import kotlin.test.assertEquals

@KodeinBean
class KodeinDIAwareAuto(override val di: DI) : DIAware {
//    val test : String by di.inject("test1")
    private val helloTest:HelloTest by di.inject()


    fun getText(): String {
        return helloTest.text()
    }

}
