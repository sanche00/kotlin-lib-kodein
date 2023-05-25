# Kotlin Kodein 활용 라이브러리
kotlin 학습용으로 만든 소스입니다.

kodein DI anntation을 사용하여 관리 합니다.

매우 심플한 라이브러리입니다.

### 사용방법
#### 1) Kodein bean 선언
@KodeinBean 어노테이션을 사용하여 Kodein Bean을 선언합니다.

@KodeinBean 선언 시 tag, bindType, sync, Reference 를 설정할수 있습니다.

tag 미 입력시 KClass의 java name이 tag로 들어값니다.

@KodeinBean CLASS, PROPERTY, FUNCTION에 선언가능합니다.
- Class Type
```
@KodeinBean(tag = "classBean")
class HelloTest (val test : String ="TEST")

```
- Function Type
```
@KodeinBean(tag = "functionBean")
fun testBean(test : String ="TEST2"): TestBean {
    return HelloTest(test)
}
```
- Property Type

Property Bean은 항상 Constant 로 binding 됩니다.
```
@KodeinBean(tag = "propertyBean")
val test: String = "test1"
```
#### 2) 선언한 Bean등록

@KodeinBean 선언된 Target 을 KodeinBeanLoader 클래스의 loadKodeinBean 함수를 사용하여 등록합니다.
- Class Type
```
KodeinBeanLoader.loadKodeinBean(HelloTest::class)

```
- Function Type
```
@KodeinBean(tag = "functionBean")
fun testBean(test : String ="TEST2"): TestBean {
    return TestBean(test)
}
```
- Property Type
  
Property Bean은 Bean이 선언된 클래스를 Load 시킵니다.
```
KodeinBeanLoader.loadConstantsBean(HelloTestConfig::class)
```
#### 3) 등록한 Bean 주입

등록된 Bean은 @KodeinInject을 사용하여 Parameter에 주입 시킬 수 있습니다.
```

@KodeinBean(tag = "classBean")
class HelloTest (@KodeinInject(tag="functionBean")  val testBean : TestBean) {
    fun getTest = testBean.test
}

```
변수에 사용시 KodeinBeanLoader 의 inject, directInject를 사용하여 주입을 제공합니다.
```
private val helloTest:HelloTest by di.inject("classBean")
or
private val helloTest:HelloTest by lazy {di.directInject("classBean")}
...

val helloTest:HelloTest = di.directInject("classBean")

```
