package com.ssg.inc.sp.kotlin.di

import com.ssg.inc.sp.kotlin.kodein.KodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinConfiguration

@KodeinConfiguration
class KodeinConfig {

    @KodeinBean(tag = "test")
    private val test:String = "test"

    private val test2:String = "test2"

    @KodeinBean
    fun test():TypeParam {
        return TypeParam()
    }

}
