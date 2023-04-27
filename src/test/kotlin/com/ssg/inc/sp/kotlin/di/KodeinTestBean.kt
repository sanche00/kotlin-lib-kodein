package com.ssg.inc.sp.kotlin.di

import com.ssg.inc.sp.kotlin.kodein.KodeinBean
import com.ssg.inc.sp.kotlin.kodein.KodeinInject

@KodeinBean("test", "KodeinTestBean")
class KodeinTestBean(@KodeinInject val typeParam: TypeParam, @KodeinInject val value: String = "TEST") {
}

@KodeinBean("test", "TypeParam")
class TypeParam(val test: String = "TEST")