package com.ssg.inc.sp

import com.ssg.inc.sp.kotlin.kodein.KodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinBeanLoader.inject
import com.ssg.inc.sp.kotlin.kodein.KodeinBeanLoader.injectConst
import com.ssg.inc.sp.kotlin.kodein.KodeinInject
import org.kodein.di.DI
import org.kodein.di.DIAware

@KodeinBean
class KodeinDIAwareAuto(override val di: DI) : DIAware {

    val test3 : String by di.injectConst(KodeinInject("test3"))
    val test2 : Int by di.injectConst("test2")
    private val helloTest:HelloTest by di.inject()

    fun getText(): String {
        return helloTest.text()
    }

}
