package com.ssg.inc.sp

import com.ssg.inc.sp.kotlin.kodein.KodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinInject
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import kotlin.test.assertEquals

@KodeinBean
class KodeinDIAware(override val di: DI) : DIAware {

    val helloTest:HelloTest by di.instance("test")

    fun getText(): String {
        return helloTest.text()
    }
}
