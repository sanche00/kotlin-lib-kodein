package com.ssg.inc.sp

import com.ssg.inc.sp.kotlin.kodein.KodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinInject

class HelloTestConfig {
    @KodeinBean(tag = "test1")
    val test: String = "test1"


    @KodeinBean(tag = "test2")
    val test2: Int = 10

    @KodeinBean(tag = "test3")
    val test3: String = "test1"

    @KodeinBean(tag = "test4")
    fun testBean(@KodeinInject(tag="test2") test1:Int): TestBean {
        return TestBean(test1)
    }
    data class TestBean(val test2:Int)
}
