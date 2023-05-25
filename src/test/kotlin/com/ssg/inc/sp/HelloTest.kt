package com.ssg.inc.sp

import com.ssg.inc.sp.kotlin.kodein.KodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinInject
import org.junit.Test
import kotlin.test.assertEquals

@KodeinBean(tag = "test")
class HelloTest (@KodeinInject(tag = "test1") val test : String ="TEST"){

    fun text(): String {
        return test;
    }
}
