package com.ssg.inc.sp

import com.ssg.inc.sp.kotlin.kodein.KodeinBean

class HelloTestConfig {
    @KodeinBean(tag = "test1")
    val test: String = "test1"


    @KodeinBean(tag = "test2")
    val test2: Int = 10
}