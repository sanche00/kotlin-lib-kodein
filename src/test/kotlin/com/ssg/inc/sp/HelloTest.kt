package com.ssg.inc.sp

import com.ssg.inc.sp.kotlin.kodein.KodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinInject
import org.junit.Test
import kotlin.test.assertEquals

@KodeinBean(module = "test")
class HelloTest (@KodeinInject(tag = "test1") private val test : String ="TEST"){
    fun text(): String {
        return test;
    }
}
