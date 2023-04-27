package com.ssg.inc.sp.kotlin.di

import com.ssg.inc.sp.kotlin.kodein.KodeinConfiguration
import com.ssg.inc.sp.kotlin.kodein.KodeinField
import com.ssg.inc.sp.kotlin.kodein.KodeinFunction

@KodeinConfiguration
class KodeinConfig {

    @KodeinField(tag = "test")
    private val test:String = "test"


    private val test2:String = "test2"

    @KodeinFunction
    fun test():TypeParam {
        return TypeParam()
    }

}