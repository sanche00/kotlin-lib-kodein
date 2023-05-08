package com.ssg.inc.sp.kotlin.reflection

import com.ssg.inc.sp.kotlin.kodein.KodeinInject
import org.kodein.di.DI
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.test.Test
import kotlin.test.assertEquals

class KClassReflection {

    @Test
    fun constructorTest() {
        assertEquals(Test1::class.constructors.size , 1)
        val const = Test1::class.primaryConstructor!!
        const.parameters.forEach{
            println(it)
            println(it.isOptional)
            println(it.name)
            println(it.kind)
            println(it.type)
            println(it.isVararg)
            if(it.hasAnnotation<KodeinInject>()) {
                println(it.findAnnotation<KodeinInject>())
            }
            val kClass = it.type.classifier as KClass<*>
            println(kClass.constructors)
        }
    }

    class Test1(val test:String, val test2:String = "test2" , @KodeinInject val test3:Test2)

    class Test2(val test:String, val test2:String = "test2")



    @Test
    fun newInstanceTest() {
        val const =  Test2::class.primaryConstructor!!
        val test2 = const.callBy(const.parameters.map {
            it to createArgument(it)
        }.filter { it.second != null }.toMap())
        assertEquals(test2.test,"test1")
        assertEquals(test2.test2,"test2")
    }

    private fun createArgument(parameter: KParameter): Any? {
        if(parameter.isOptional) {
            return null
        }
        return "test1"
    }

}