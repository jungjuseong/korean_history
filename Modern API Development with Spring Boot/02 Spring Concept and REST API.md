# Chapter 2: Spring Concepts and REST APIs
이전 장에서 REST 아키텍처 스타일에 대해 배웠습니다. Spring과 Spring Boot를 사용하여 RESTful 웹 서비스를 구현하기 전에 기본 Spring 개념에 대한 적절한 이해가 필요합니다. 이 장에서는 Spring Framework를 사용하여 RESTful 웹 서비스를 구현하는 데 필요한 Spring 기본 사항과 기능에 대해 학습합니다. 이것은 샘플 전자 상거래 앱을 개발하는 데 필요한 기술적 관점을 제공합니다. RESTful API 구현에 필요한 Spring 기본 사항을 이미 알고 있다면 다음 장으로 넘어갈 수 있습니다.

이 장의 일부로 다음 주제를 다룰 것입니다.

- 봄 소개
- Spring Framework의 기본 개념 학습
- 서블릿 디스패처로 작업하기

## 기술 요구 사항

이 장에서는 개념을 다루며 실제 프로그램이나 코드는 다루지 않습니다. 그러나 기본적인 Java 지식이 필요합니다.

Please visit the following link to download the code files: https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter02

## Spring 소개

Spring은 프레임워크이며 Java 언어로 작성되었습니다. Spring Data, Spring Security, Spring Cloud, Spring Web 등과 같은 많은 모듈을 제공합니다. 엔터프라이즈 애플리케이션을 구축하는 데 널리 사용됩니다. 처음에는 Java Enterprise Edition(EE) 대안으로 간주되었습니다. 그러나 수년에 걸쳐 Java EE보다 선호되었습니다. Spring은 Dependency Injection(Inversion of Control)과 Aspect-Oriented Programming을 핵심으로 지원합니다. Java 외에도 Spring은 Groovy 및 Kotlin과 같은 다른 JVM 언어도 지원합니다.

Spring Boot의 도입으로 웹 서비스 구축은 개발 소요 시간에 관한 한 머리에 못을 박았습니다. 당신은 즉시 땅을 쳤습니다. 이것은 거대하고 최근에 Spring이 인기를 얻게 된 이유 중 하나입니다.

Spring 기초 자체를 다루려면 전용 책이 필요합니다. 나는 간결하고 세부적인 방식으로 REST 구현 지식을 파악하고 진행하는 데 필요한 모든 기능을 다루려고 노력할 것입니다.

계속 진행하기 전에 IoC(Inversion of Control), DI(Dependency Injection) 및 AOP(Aspect-Oriented Programming)의 원리와 디자인 패턴을 살펴봐야 합니다.

### 제어 패턴의 역전
전통적인 CLI 프로그램은 흐름이 프로그래머에 의해 결정되고 코드가 하나씩 순차적으로 실행되는 절차적 프로그래밍 구현을 위한 일반적인 방법입니다. 그러나 UI 기반 OS 응용 프로그램은 사용자 입력 및 이벤트를 기반으로 프로그램의 흐름을 결정하며 이는 동적입니다.

오래 전에 대부분의 절차적 프로그래밍 방식이 인기를 끌었을 때 흐름 제어를 전통적인 절차적 방식(프로그래머가 흐름을 지시함)에서 결정하는 프레임워크 또는 구성 요소와 같은 외부 소스로 이동하는 방법을 찾아야 했습니다. 프로그램의 제어 흐름. 이것을 IoC라고 합니다. 이것은 매우 일반적인 원칙이자 대부분의 프레임워크의 일부입니다.

객체 지향 프로그래밍 접근 방식을 통해 곧 프레임워크는 의존성 주입을 지원하는 IoC 컨테이너 패턴 구현을 제시했습니다.

### 의존성 주입 패턴
데이터베이스에서 일부 데이터가 필요한 프로그램을 작성한다고 가정해 보겠습니다. 프로그램은 데이터베이스 연결이 필요합니다. JDBC 데이터베이스 연결 개체를 사용할 수 있습니다. 프로그램에서 즉시 데이터베이스 연결 개체를 인스턴스화하고 할당할 수 있습니다. 또는 연결 객체를 생성자나 setter/factory 메소드 매개변수로 사용할 수 있습니다. 그런 다음 프레임워크는 구성에 따라 연결 개체를 만들고 런타임에 해당 개체를 프로그램에 할당합니다. 여기서 프레임워크는 실제로 런타임에 연결 개체를 주입합니다. 이것을 DI라고 합니다. Spring은 클래스 구성을 위해 DI를 지원합니다.

노트:

의존성을 사용할 수 없거나 둘 이상의 객체 유형을 사용할 수 있을 때 적절한 객체 이름이 표시되지 않으면 Spring Framework는 런타임에 오류를 발생시킵니다. 이와 대조적으로 Dagger와 같이 컴파일 시간에 이러한 의존성을 확인하는 일부 프레임워크도 있습니다.

DI는 IoC의 한 유형입니다. IoC 컨테이너는 구현 개체를 구성하고 유지 관리합니다. 이러한 유형의 객체(다른 객체가 필요로 하는 객체 – 일종의 의존성)는 생성자, 설정자 또는 인터페이스에서 이를 필요로 하는 객체에 주입됩니다. 이것은 인스턴스화를 분리하고 런타임에 의존성 주입을 허용합니다. 의존성 주입은 Service Locator 패턴을 사용하여 달성할 수도 있습니다. 그러나 우리는 IoC 패턴 접근 방식을 고수할 것입니다.

다음 섹션에서 코드 예제를 통해 더 자세히 살펴보겠습니다.

### Aspect 지향 프로그래밍 패러다임
또 다른 프로그래밍 패러다임인 AOP가 등장합니다. AOP는 OOP와 함께 작동합니다. OOP에서는 특정 클래스에서 단일 책임만 처리하는 것이 좋습니다. 이 원칙을 단일 책임 원칙이라고 합니다(모듈/클래스/메서드에 적용 가능). 예를 들어 자동차 도메인 응용 프로그램에서 Gear 클래스를 작성하는 경우 Gear 클래스는 기어 개체와 관련된 기능만 허용하거나 제동과 같은 다른 기능을 수행하도록 허용해서는 안 됩니다. 

그러나 프로그래밍 모델에서는 종종 둘 이상의 클래스에 분산되는 기능이 필요합니다. 사실, 때때로 대부분의 클래스는 로깅 또는 메트릭과 같은 기능을 사용합니다.

로깅, 보안, 트랜잭션 관리 및 메트릭과 같은 기능은 여러 클래스/모듈에서 필요합니다. 이러한 기능의 코드는 여러 클래스에 흩어져 있습니다. OOP에서는 이러한 기능을 추상화하고 캡슐화할 방법이 없습니다. 이것이 AOP가 당신을 도와주는 곳입니다. 이러한 기능(읽기 측면)은 개체 모델의 여러 지점을 가로지르는 교차 절단 문제입니다. AOP는 여러 클래스/모듈에서 이러한 측면을 처리할 수 있는 방법을 제공합니다.

AOP allows you to do the following:

- To abstract and encapsulate cross-cutting concerns.
- To add aspects behavior around your code.
- To make code modular for cross-cutting concerns to easily maintain and extend it.
- To focus on your business logic inside code. This makes code clean. Cross-cutting concerns are encapsulated and maintained separately.
Without AOP, it is very difficult and complex to achieve all of the preceding points.

Please note that this section helps you to understand IoC, DI, and AOP conceptually. In the next sections, you'll take a deep dive into the code implementation of these patterns and paradigms.

Now, we will go through the fundamentals of the Spring Framework and its basic building blocks.



## Learning the basic concepts of the Spring Framework

The Spring Framework's backbone is the IoC container that is responsible for a bean's life cycle. In the Spring world, a Java object can be a bean if it is instantiated, assembled, and managed by the IoC container. You create n-number of beans, aka objects, for your application. A bean may have dependencies, that is, requiring other objects to work. The IoC container is responsible for injecting the object's dependencies when it creates that bean. In the Spring context, IoC is also known as DI.

In the following sections, we'll cover the following core Spring concepts:

- IoC containers
- Defining beans
- Configuring beans using Java
- How to code DI
- Writing code for AOP

*NOTE:*

You can refer to the Spring documentation (https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/) for more information about the Spring Framework.

Let's get started!

### IoC containers
Spring Framework의 IoC 컨테이너 코어는 org.springframework.beans 및 org.springframework.context의 두 패키지로 정의됩니다. BeanFactory(org.springframework.beans.factory.BeanFactory) 및 ApplicationContext(org.springframework.context.ApplicationContext)는 IoC 컨테이너의 기반을 제공하는 두 가지 중요한 인터페이스입니다. BeanFactory는 구성 프레임워크와 기본 기능을 제공하고 Bean 인스턴스화 및 배선을 처리합니다. ApplicationContext는 또한 빈 인스턴스화 및 연결을 처리할 수 있습니다. 

다음과 같이 더 많은 엔터프라이즈별 기능을 제공합니다.

- 통합 라이프 사이클 관리
- BeanPostProcessor 및 BeanFactoryPostProcessor 자동 등록
- MessageSource에 쉽게 접근할 수 있는 국제화(메시지 자원 처리)
- 내장된 ApplicationEvent를 이용한 이벤트 발행
- 웹 애플리케이션을 위한 애플리케이션 계층별 컨텍스트인 WebApplicationContext 제공

ApplicationContext is a sub-interface of BeanFactory. Let's look at its class signature:
```java
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory, MessageSource, ApplicationEventPublisher, ResourcePatternResolver
```
Here, ListableBeanFactory and HierarchicalBeanFactory are sub-interfaces of BeanFactory.

Spring recommends the use of ApplicationContext due to added features apart from state-of-the-art bean management.

### ApplicationContext
Now, you know that the ApplicationContext interface represents the IoC container and manages the beans, you must be wondering how it gets to know about what beans to instantiate, assemble, and configure. From where does it get its instruction? The answer is configuration metadata. Configuration metadata allows you to express your application objects and interdependencies among those objects. Configuration metadata can be represented in three ways: through XML configuration, Java annotations, and Java Code. You write the business objects and provide the configuration metadata, and the Spring container generates a ready-to-use fully configured system as shown:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/B16561_02_01.jpg)

Figure 2.1 – Spring container

Let's see how you can define a Spring bean.


## Defining beans

Bean은 IoC 컨테이너에서 관리하는 객체입니다. 개발자는 IoC 컨테이너에 구성 메타데이터를 제공하고, 이 메타데이터는 빈을 구성, 조합 및 관리하기 위해 컨테이너에서 사용됩니다. Bean은 컨테이너 내부에 고유한 식별자를 가져야 합니다.  Bean은 별칭을 사용하여 둘 이상의 ID를 가질 수 있습니다.

XML, Java 및 주석을 사용하여 빈을 정의할 수 있습니다. Java 기반 구성을 사용하여 간단한 빈을 선언해 보겠습니다.

```java
public class SampleBean {
    public void init() { // initialization logic }
    public void destroy() { // destruction logic }
    // bean code
}

public interface BeanInterface { 
   // interface code 
}

public class BeanInterfaceImpl implements BeanInterface {
    // bean code
}

@Configuration
public class AppConfig {
    @Bean(initMethod = "init", destroyMethod = "destroy", name = {"sampleBean", "sb"})
    @Description("Demonstrate a simple bean")
    public SampleBean sampleBean() {
        return new SampleBean();
    }

    @Bean
    public BeanInterface beanInterface() {
        return new BeanInterfaceImpl();
    }
}
```
여기서 Bean은 AppConfig 클래스를 사용하여 선언됩니다. @Configuration은 클래스에 구성 코드가 포함되어 있음을 보여주는 클래스 수준 주석입니다. @Bean은 Bean을 정의하는 데 사용되는 메소드 레벨 주석입니다. 이전 코드에서와 같이 @Bean 주석 속성을 사용하여 빈의 초기화 및 소멸 수명 주기 메서드를 전달할 수도 있습니다.

일반적으로 Bean의 이름은 이니셜이 소문자인 클래스 이름입니다. 예를 들어 BeanInterface의 빈 이름은 beanInterface입니다. 그러나 name 속성을 사용하여 빈 이름과 별칭을 정의할 수도 있습니다. SampleBean에는 sampleBean과 sb라는 두 개의 빈 이름이 있습니다.

*노트:*

파괴를 위한 기본 메소드는 컨테이너에 의해 자동으로 호출되는 close/shutdown 공용 메소드입니다. 그러나 다른 방법을 원할 경우 샘플 코드와 같이 수행할 수 있습니다. 컨테이너가 기본 파괴 메서드를 호출하지 않도록 하려면 destroyMethod 속성에 빈 문자열을 할당하면 됩니다(destroyMethod = "").

BeanInterface 빈에 대한 이전 코드에 표시된 인터페이스를 사용하여 빈을 생성할 수도 있습니다.

@Bean 주석은 @Component 주석 내부에 있어야 합니다. @Component 주석은 빈을 선언하는 일반적인 방법입니다. @Configuration으로 주석이 달린 클래스는 메소드가 @bean으로 주석이 달린 빈을 반환하도록 작동합니다. @Configuration은 @Component로 메타 주석이 달려 있으므로 @Bean 주석은 내부에서 작동합니다. @Controller, @Service 및 @Repository와 같은 다른 주석도 있으며 @Component로 주석이 지정됩니다.

@Description 주석은 빈을 설명하는 데 사용됩니다. 모니터링 도구를 사용할 때 이러한 설명은 런타임 시 빈을 이해하는 데 도움이 됩니다.

### @ComponentScan

@ComponentScan 주석은 빈의 자동 스캔을 허용합니다. 기본 패키지 및 해당 클래스와 같은 몇 가지 인수가 필요합니다. 그런 다음 Spring 컨테이너는 기본 패키지 내의 모든 클래스를 살펴보고 빈을 찾습니다. @Component로 주석 처리된 모든 클래스 또는 @Configuration, @Controller 등과 같이 @Component로 메타 주석 처리된 기타 주석을 스캔합니다.

기본적으로 Spring Boot는 @ComponentClass 주석이 있는 클래스에서 기본 기본 패키지를 가져옵니다. basePackageClasses 속성을 사용하여 스캔해야 하는 패키지를 식별할 수 있습니다.

두 개 이상의 패키지를 스캔하는 또 다른 방법은 basePackages 속성을 사용하는 것입니다. 하나 이상의 패키지를 스캔할 수 있습니다.

둘 이상의 @ComponentScan을 사용하려는 경우 다음과 같이 @ComponentScans 주석 내부에 래핑할 수 있습니다.
```java
@Configuration
@ComponentScans({
  @ComponentScan(basePackages = "com.packt.modern.api"),
  @ComponentScan(basePackageClasses = AppConfig.class)
})

class AppConfig { //code }
```

### The bean's scope

Spring 컨테이너는 빈의 인스턴스 생성을 담당합니다. 인스턴스를 생성하는 방법은 scope에 의해 정의됩니다. 기본 범위는 싱글톤입니다. 즉, IoC 컨테이너당 하나의 인스턴스만 생성되고 동일한 인스턴스가 주입됩니다. 요청될 때마다 새 인스턴스를 생성하려면 빈에 대한 프로토타입 범위를 정의할 수 있습니다.

모든 Spring 기반 애플리케이션에서 singleton 및 prototype scope를 사용할 수 있습니다. 웹 애플리케이션에 사용할 수 있는 scope는 request, session, application, websocket의 네 가지가 더 있습니다. 이후 scope의 경우 애플리케이션 컨텍스트는 웹을 인식해야 합니다. Spring Boot 기반 웹 애플리케이션은 웹을 인식합니다.

다음 표에는 모든 scope가 포함되어 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/B16561_02_Table_01.jpg)

코드에서 singleton 및 prototype 범위를 정의하는 방법을 살펴보겠습니다.

```java
@Configuration
public class AppConfig {
    // no scope is defined so default singleton scope is applied.
    // If you want to define it explicitly, you can do that using

    // @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    // OR
    // @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)

    // Here, ConfigurableBeanFactory.SCOPE_SINGLETON is string constant, which
    // value is "singleton". You can use the string also,better to avoid it.

    @Bean
    public SingletonBean singletonBean() {
        return new SingletonBean();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public PrototypeBean prototypeBean() {
        return new PrototypeBean();
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST,
           proxyMode = ScopedProxyMode.TARGET_CLASS)

    // You need a proxyMode attribute because when web-aware
    // context is instantiated, you don't have any HTTP request.

    // Therefore,
    // Spring injects the proxy as a dependency and
    // instantiate the bean when HTTP request is invoked.  
    // OR, in short you can write below which is a shortcut for
    // above

    @RequestScope
    public ReqScopedBean requestScopedBean() {
        return new ReqScopedBean();
    }
}
```
Similarly, you can create a web-aware context-related bean as follows:

```java
@Configuration
public class AppConfig {
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST,
           proxyMode = ScopedProxyMode.TARGET_CLASS)
    // You need a proxyMode attribute because when web-aware
    // context is instantiated, you don't have any HTTP request.

    // Therefore,
    // Spring injects the proxy as a dependency and
    // instantiate the bean when HTTP request is invoked.  
    // OR, in short you can write below which is a shortcut for
    // above

    @SessionScope
    public ReqScopedBean requestScopedBean() {
        return new ReqScopedBean();
    }

    @ApplicationScope
    public ReqScopedBean requestScopedBean() {
        return new ReqScopedBean();
    }

    // here "scopeName" is alias for value
    // interestingly, no shortcut. Also hard coded value for
    // websocket

    @Scope(scopeName = "websocket",
           proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ReqScopedBean requestScopedBean() {
        return new ReqScopedBean();
    }
}
```

공식 Spring 문서(https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/)에서 이에 대해 자세히 알아볼 수 있습니다.


## Java를 사용하여 빈 구성

Spring 3 이전에는 Java를 사용하여 빈을 정의할 수 있었습니다. Spring 3에서는 @Configuration, @Bean, @import 및 @DependsOn 주석을 도입하여 Java를 사용하여 Spring Bean을 구성하고 정의합니다.

빈 정의 섹션에서 @Configuration 및 @Bean 주석에 대해 이미 배웠습니다. 이제 @import 및 @DependsOn 주석을 사용하는 방법을 살펴보겠습니다.

@Import 주석은 자동 구성을 사용하지 않고 애플리케이션을 개발할 때 더 유용합니다.

### @Import 주석

둘 이상의 구성 클래스가 있는 경우 구성을 모듈화하는 데 사용됩니다. 다른 구성 클래스에서 빈의 정의를 가져올 수 있습니다. 컨텍스트를 수동으로 인스턴스화할 때 유용합니다. Spring Boot는 자동 설정을 사용하므로 @Import를 사용할 필요가 없습니다. 그러나 컨텍스트를 수동으로 인스턴스화하려면 @Import를 사용하여 구성을 모듈화해야 합니다.

구성 클래스 FooConfig에 FooBean이 포함되어 있고 구성 클래스 BarConfig에 BarBean이 포함되어 있다고 가정해 보겠습니다. BarConfig 클래스는 @Import를 사용하여 FooConfig도 가져옵니다.

```java
@Configuration
public class FooConfig {
    @Bean
    public FooBean fooBean() {
        return new FooBean();
    }
}
```

```java
@Configuration
@Import(FooConfig.class)
public class BarConfig {
    @Bean
    public BarBean barBean() {
        return new BarBean();
    }
}
```
Now, while instantiating the container (context), you can just supply BarConfig to load both FooBean and BarBean definitions in the Spring container as shown:

```java
public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(BarConfig.class);

    // now both FooBean and BarBean beans will be available...
    FooBean fooBean = ctx.getBean(FooBean.class);
    BarBean barBean = ctx.getBean(BarBean.class);
}
```

### The @DependsOn annotation

Spring 컨테이너는 Bean 초기화 순서를 관리합니다. 다른 빈에 의존하는 빈이 있다면? 종속 Bean이 필요한 Bean보다 먼저 초기화되었는지 확인하려고 합니다. @DependsOn은 XML이 아닌 Java를 사용하여 빈을 구성할 때 이를 달성하는 데 도움이 됩니다.

빈의 초기화 순서가 엉망이고 그로 인해 Spring 컨테이너가 의존성을 찾지 못하면 예외 NoSuchBeanDefinitionException이 발생합니다.

FooBean 및 BarBean 빈에 의존하는 BazBean이라는 이름의 빈이 있다고 가정해 보겠습니다. @DependsOn 주석을 사용하여 초기화 순서를 유지할 수 있습니다. Spring 컨테이너는 BazBean을 생성하기 전에 지침을 따르고 FooBean 및 BarBean 빈을 모두 초기화합니다. 코드는 다음과 같습니다.

```java
@Configuration
public class AppConfig {
    @Bean
    public FooBean fooBean() {
        return new FooBean();
    }    

    @Bean
    public BarBean barBean () {
        return new BarBean ();
    }

    @Bean
    @DependsOn({"fooBean","barBean"})
    public BazBean bazBean (){
        return new BazBean ();
    }
}
```

## How to code DI

다음 예를 살펴보십시오. CartService에는 CartRepository에 대한 의존성이 있습니다. CartRepository 인스턴스화는 CartService 생성자 내에서 수행되었습니다.

```java
public class CartService {
    private CartRepository repository;
    public CartService() {
        this.repository = new CartRepositoryImpl();

    }
}
```
We can decouple this dependency in the following way:
```java
public class CartService {
    private CartRepository repository;

    public CartService(CartRepository repository) {
        this.repository = repository;
    }
}  
```

CartRepository 구현의 Bean을 생성하면 구성 메타데이터를 사용하여 CartRepository Bean을 쉽게 주입할 수 있습니다. 이 장의 @Import 주석 하위 섹션에서 ApplicationContext를 초기화하는 방법을 보았습니다. 생성될 때 빈의 구성에서 모든 메타데이터를 가져옵니다. @Import를 사용하면 여러 구성을 가질 수 있습니다.

각 빈은 의존성을 가질 수 있습니다. 즉, 빈은 CartService 예제에서와 같이 작동(구성)하기 위해 다른 객체가 필요할 수 있습니다. 이러한 의존성은 생성자, 설정자 메서드 또는 속성을 사용하여 정의할 수 있습니다. 이러한 종속 객체(생성자의 일부, setter 메서드 인수 또는 클래스 속성)는 빈의 정의와 범위를 사용하여 Spring 컨테이너에 의해 주입됩니다. DI를 정의하는 이러한 각 방법을 살펴보겠습니다.

**노트**

DI는 클래스를 의존성과 독립적으로 만듭니다. 따라서 의존성 변경으로 인해 클래스를 변경할 필요가 없거나 드물게 발생할 수 있습니다. 즉, 클래스를 변경하지 않고 의존성을 변경할 수 있습니다. 또한 의존성을 흉내내거나 감시하여 단위 테스트를 단순화합니다.

### 생성자를 사용하여 의존성 정의

이제 CartRepository를 CartService 생성자에 삽입하는 방법을 볼 수 있습니다. 
생성자를 사용하여 의존성을 주입하는 방법은 다음과 같습니다.

```java
@Configuration
public class AppConfig {
    @Bean
    public CartRepository cartRepository() {
        return new CartRepositoryImpl();
    }   

    @Bean
    public CartService cartService() {
        return new CartService(cartRepository());
    }
}
```

### Using a setter method to define a dependency

이제 CartService 클래스를 변경해 보겠습니다. 생성자를 사용하는 대신 setter 메서드를 사용하여 의존성을 인스턴스화합니다.

```java
public class CartService {
    private CartRepository repository;
    public void setCartRepository(CartRepository repository) {
        this.repository = repository;
    }
}
```

이제 다음 설정으로 의존성을 주입할 수 있습니다:

```java
@Configuration
public class AppConfig {
    @Bean
    public CartRepository cartRepository() {
        return new CartRepositoryImpl();
    }
    
    @Bean
    public CartService cartService() {
        CartService service = new CartService();
        Service.setCartService(cartRepository());
        Return service;
    }
}
```

*노트*

Spring은 setter 메소드나 클래스 속성 기반 의존성 주입보다 **생성자 기반 의존성 주입을 사용할 것을 권장합니다**. 그러나 옵트인 의존성의 경우 setter 메서드 기반 의존성 주입을 신중하게 사용해야 합니다.

### 클래스 속성을 사용하여 의존성 정의

Spring은 또한 @Autowired 주석을 사용하여 의존성을 주입하기 위한 즉시 사용 가능한 솔루션을 제공합니다. 코드가 더 깔끔해 보입니다. 다음 예를 살펴보십시오.

```java
@Service
public class CartService {
    @Autowired
    private CartRepository repository;
}
```

Spring 컨테이너는 CartRepository 빈 주입을 처리합니다. 다음 섹션에서 @Autowired에 대해 자세히 알아볼 것입니다.


## 주석을 사용하여 빈의 메타데이터 구성

Spring에서는 빈에 대한 메타데이터를 구성하기 위해 많은 주석을 제공합니다. 그러나 가장 일반적으로 사용되는 주석인 @Autowired, @Qualifier, @Inject, @Resource, @Primary 및 @Value에 초점을 맞춥니다.

### @Autowired 사용법

@Autowired 주석을 사용하면 @Configuration 주석이 달린 별도의 구성 클래스를 작성하는 대신 빈의 클래스 자체에서 구성 부분을 정의할 수 있습니다. @Autowired 주석은 필드(클래스 속성 기반 의존성 주입 예제에서 보았듯이), 생성자, 세터 또는 모든 메서드에 적용할 수 있습니다.

Spring 컨테이너는 @Autowired로 주석이 달린 빈을 주입하기 위해 리플렉션을 사용합니다. 이것은 또한 다른 주입 접근법보다 비용이 많이 듭니다.

클래스 멤버에 @Autowired를 적용하는 것은 종속 빈을 주입할 생성자 또는 세터 메서드가 없는 경우에만 작동한다는 점에 유의하십시오.

다음은 의존성을 주입하는 @Autowired 방법의 코드 예입니다.

```java
@Component
public class CartService {
    private CartRepository repository;
    private ARepository aRepository;
    private BRepository bRepository;
    private CRepository cRepository;

    @Autowired // member(field) based auto wiring
    private AnyBean anyBean;

    @Autowired // constructor based autowired
    public CartService(CartRepository cartRepository) {
        this.repository = repository;
    }

    @Autowired // Setter based auto wiring
    public void setARepository(ARepository aRepository) {
        this.aRepository = aRepository;
    }

    @Autowired // method based auto wiring
    public void xMethod(BRepository bRepository, CRepository cRepository)
    {
        this.bRepository = bRepository;
        this.cRepository = cRepository;
    }
}
```
@Autowired는 리플렉션을 기반으로 작동합니다. 그러나 모호성을 제거하기 위해 일치하는 빈을 찾고 동일한 우선 순위로 유형 일치, 한정자 일치 또는 이름 일치를 사용하여 주입합니다. 필드 및 세터 메서드 주입 모두에 적용할 수 있습니다.

### type 일치

다음 예는 type 일치가 우선하므로 작동합니다. CartService 빈을 찾아 CartController에 주입합니다.

```java
@Configuration
public class AppConfig {
    @Bean
    public CartRepository cartRepository() {
        return new CartRepositoryImpl();
    }

    @Bean
    public CartService cartService() {
        CartService service = new CartService();
        Service.setCartService(cartRepository());
        Return service;
    }
}

@Controller
public class CartController {    
   @Autowired
   private CartService service;
}
```

#### Match by qualifier

주어진 유형의 빈이 두 개 이상 있다고 가정해 봅시다. 그러면 Spring 컨테이너는 유형별로 올바른 빈을 결정할 수 없습니다.

```java
@Configuration

public class AppConfig {    
   @Bean
    public CartService cartService1() {
        return new CartServiceImpl1();
    }    

    @Bean
    public CartService cartService2() {
        return new CartServiceImpl2();
    }
}

@Controller
public class CartController {    
   @Autowired
    private CartService service1;

    @Autowired
    private CartService service2;

}
```

이 예제는 실행될 때 NoUniqueBeanDefinitionException을 반환합니다. 이를 분류하기 위해 @Qualifier 주석을 사용할 수 있습니다.

자세히 살펴보면 구성 클래스에 해당 메소드 이름으로 식별되는 두 개의 빈(cartService1 및 cartService2)이 있음을 알 수 있습니다. 또는 @Bean 주석의 값 속성을 사용하여 이름/별칭을 지정할 수도 있습니다. 이제 다음과 같이 @Qualifier 주석을 사용하여 이러한 이름을 사용하여 이 두 개의 다른 빈을 동일한 유형으로 할당할 수 있습니다.

```java
@Controller
public class CartController {    
   @Autowired
   @Qualifier("cartService1")
   private CartService service1;

   @Autowired
   @Qualifier("cartService2")
   private CartService service2;
}

### Match by name

Let's define a service using the @Service annotation, which is a type of @Component. Let's assume we have a component scan in place:

```java
@Service(value="cartServc")
public class CartService {
    // code
}

@Controller
public class CartController {    
   @Autowired
    private CartService cartServc;
}
```
이 코드는 CartService용 CartController의 필드 이름이 @Service 어노테이션의 value 속성에 부여된 것과 동일하기 때문에 작동합니다. 필드 이름을 cartServc에서 다른 이름으로 변경하면 NoUniqueBeanDefinitionException과 함께 실패합니다.

@Inject 및 @Resource와 같은 다른 주석이 있습니다. @Inject에는 javax.inject 라이브러리도 필요합니다. @Resource 및 @Inject는 @Autowired와 유사하며 의존성을 주입하는 데 사용할 수 있습니다. @Autowired와 @Inject는 동일한 실행 경로 우선 순위를 갖습니다(유형별, 한정자별, 이름별 동일한 순서). 그러나 @Resource 실행 경로 기본 설정은 이름(첫 번째 기본 설정), 유형별, 한정자(마지막 기본 설정)별로 지정됩니다.

#### @Primary의 목적은 무엇입니까?

이전 하위 섹션에서 @Qualifier가 여러 빈을 주입할 수 있을 때 사용해야 하는 유형을 결정하는 데 도움이 되는 것을 보았습니다. @Primary 주석을 사용하면 type의 빈 중 하나를 기본값으로 설정할 수 있습니다. @Primary가 있는 Bean 주석은 autowired 필드에 주입됩니다.

```java
@Configuration
public class AppConfig {    
   @Bean
   @Primary
   public CartService cartService1() {
      return new CartServiceImpl1();
   }    

   @Bean
   public CartService cartService2() {
      return new CartServiceImpl2();
   }
}

@Controller

public class CartController {    
   @Autowired
    private CartService service;
}
```
In this example, the bean marked with @Primary will be used to inject the dependency into the CartController class for CartService.

### When we can use @Value?

Spring은 <xyz>.properties 또는 <xyz>.yml과 같은 외부 속성 파일의 사용을 지원합니다. 이제 코드에서 속성 값을 사용하려고 합니다. @Value 주석을 사용하여 이를 달성할 수 있습니다. 샘플 코드를 살펴보겠습니다.

```java
@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {}

@Controller
public class CartController {    
   @Value("${default.currency}")
   String defaultCurrency;
}
```
defaultCurrency 필드는 application.properties 파일에 정의된 default.currency 필드에서 값을 가져옵니다. Spring Boot를 사용하는 경우 @PropertySource를 사용할 필요가 없습니다. src/main/resources 디렉토리 아래에 application.yml 또는 속성 파일을 배치하기만 하면 됩니다.



## AOP용 코드 작성

우리는 이전에 Spring 소개 섹션에서 AOP에 대해 논의했습니다. 간단히 말해서 로깅, 트랜잭션, 보안 등과 같은 교차 문제를 해결하는 프로그래밍 패러다임입니다. 이러한 교차 문제는 AOP에서 측면으로 알려져 있습니다. 이를 통해 코드를 모듈화하고 교차 문제를 중앙에 배치할 수 있습니다.

다음 코드는 메서드가 실행하는 데 걸리는 시간을 캡처합니다.

```java
class Test
    public void performSomeTask() {
        long start = System.currentTimeMillis();
        // Business Logic
        long executionTime = System.currentTimeMillis() - start;
        System.out.println("Time taken: " + executionTime + " ms");        
    }
}
```
이 코드는 자체적으로 소요된 시간을 캡처합니다. 애플리케이션에 수백 개의 메서드가 있는 경우 시간을 모니터링하기 위해 각 메서드에 시간 캡처 코드를 추가해야 합니다. 게다가 코드를 수정하고 싶다면? 이는 모든 위치에서 코드를 수정해야 함을 의미합니다. 당신은 그렇게하고 싶지 않습니다. 이것이 AOP가 당신을 도와주는 곳입니다. 교차 절단 코드를 모듈화합니다.

메서드가 실행되는 데 걸리는 시간을 캡처하는 AOP 예제를 만들어 보겠습니다. 여기에서 로깅 모니터링 시간은 메서드 실행에 걸리는 시간을 캡처하는 측면이 될 것입니다.

첫 번째 단계로 메서드를 대상으로 하는 주석(TimeMonitor)을 정의합니다. @TimeMonitor 주석이 달린 메소드는 해당 메소드에 소요된 시간을 기록합니다. 이것은 포인트컷을 식별하는 데 도움이 됩니다. pointcut은 굵은 텍스트로 강조 표시된 Aspect 클래스의 코드 설명에 정의되어 있습니다.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeMonitor {}
```
실행하는 동안 추가 로직을 삽입합니다. 이 포인트를 조인 포인트라고 합니다. 조인 포인트는 수정 중인 필드, 호출 중인 메서드 또는 예외가 throw될 수 있습니다.

@Aspect 주석은 클래스를 Aspect로 표시하는 데 사용됩니다. Aspect-monitoring 시간은 다음 코드를 사용하여 정의되었습니다.
```java
@Aspect
@Component
public class TimeMonitorAspect {
    @Around("@annotation(com.packt.modern.api.TimeMonitor)")
    public Object logTime(ProceedingJoinPoint joinPoint) throws             Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        System.out.println(joinPoint.getSignature() + " takes:  " + executionTime + " ms");

        return proceed;
    }    

}
```
@Around는 Advice를 정의하는 메서드 주석입니다. Advice는 특정 시간(Joinpoint)에 Aspect가 취하는 조치입니다. 이 조언은 다음 중 하나일 수 있습니다.

- @Before: JoinPoint 전에 Advice가 실행됩니다.
이후: JoinPoint 이후에 실행된 어드바이스. 세 가지 하위 유형이 있습니다.

a. @After: Advice는 성공 여부와 상관없이 JoinPoint 이후에 실행됩니다.
b. @AfterReturning: JoinPoint가 성공적으로 실행된 후 Advice가 실행됩니다.
c. @AfterThrowing: JoinPoint가 예외를 throw한 후 Advice가 실행됩니다.

- @Around: Advice는 JoinPoint 전후에 실행됩니다.
MonitorTime Advice 대상이 메서드이기 때문에 TimeMonitorAspect는 메서드 수준에서 실행됩니다.

@Around는 표현식 인수 @annotation(com.packt.modern.api.TimeMonitor)도 사용합니다. 이 술어 표현식은 Advice를 실행해야 하는지 여부를 결정하는 Pointcut으로 알려져 있습니다. @TimeMonitor로 주석이 달린 모든 메소드에 대해 logTime 메소드가 실행됩니다. Spring은 AspectJ 표현식 구문을 지원합니다. 이러한 표현식은 본질적으로 동적이므로 Pointcut을 정의하는 동안 유연성을 허용합니다.

JoinPoint는 logTime() 메소드 매개변수로 추가됩니다. JoinPoint 개체를 사용하면 대상 및 프록시의 모든 정보를 캡처할 수 있습니다. JoinPoint 개체를 사용하여 메서드의 전체 서명, 클래스 이름, 메서드 이름, 인수 등을 캡처할 수 있습니다.

이것이 TimeMonitorAspect를 구현하는 데 필요한 전부입니다. 이제 @TimeMonitor 주석을 추가하여 다음과 같이 메서드에서 소요된 계산 시간을 기록할 수 있습니다.

```java
class Test
    @TimeMonitor
    public void performSomeTask() {
        // Business Logic
    }
}
```

JoinPoint를 사용하면 대상 개체와 프록시를 캡처할 수도 있습니다. 이것들이 무엇인지 궁금할 것입니다. 이것들은 Spring AOP 모듈에 의해 생성되며 AOP가 작동하는 데 중요합니다. Advice가 대상 개체에 적용됩니다. Spring AOP는 타겟 객체의 서브클래스를 생성하고 메소드를 오버라이드하고 어드바이스가 삽입된다. 반면 프록시는 CGLIB 또는 JDK 프록시 라이브러리를 사용하여 대상 객체에 어드바이스를 적용한 후 생성되는 객체입니다. 



## 스프링 부트를 사용하는 이유

현재 Spring Boot는 Spring에 특화된 최신 생산 준비 웹 애플리케이션을 개발하기 위한 확실한 선택입니다. 웹사이트(https://projects.spring.io/spring-boot/)에도 실제 장점이 나와 있습니다.

Spring Boot는 2014년 4월(GA)에 출시된 Pivotal에서 만든 놀라운 Spring 도구입니다. SPR-9888(https://jira.spring.io/browse/SPR-9888)의 요청에 따라 '컨테이너 없는' 웹 애플리케이션 아키텍처 지원 개선이라는 제목으로 개발되었습니다.

왜 컨테이너가 없는지 궁금할 것입니다. 오늘날의 클라우드 환경 또는 PaaS는 안정성, 관리 또는 확장성과 같은 컨테이너 기반 웹 아키텍처가 제공하는 대부분의 기능을 제공하기 때문입니다. 따라서 Spring Boot는 자체적으로 초경량 컨테이너를 만드는 데 중점을 둡니다.

Spring Boot에는 기본 구성이 있으며 프로덕션 준비 웹 애플리케이션을 간단하게 만들기 위해 자동 구성을 지원합니다. Spring Initializr(http://start.spring.io)은 그룹, 아티팩트 및 의존성과 같은 프로젝트 메타데이터와 함께 Maven 또는 Gradle과 같은 빌드 도구를 선택할 수 있는 웹 페이지입니다. 필수 필드를 채우고 나면 프로젝트 생성 버튼을 클릭하기만 하면 프로덕션 애플리케이션에 사용할 수 있는 Spring Boot 프로젝트가 제공됩니다.

이 페이지에서 기본 패키징 옵션은 이 책 전체에서 사용할 Jar입니다. WebLogic 또는 Tomcat과 같은 웹 서버에 애플리케이션을 배포하려는 경우 WAR 패키징을 사용할 수 있습니다.

간단히 말해서, 그것은 우리를 위해 모든 구성 부분을 수행합니다. 우리는 비즈 로직과 API 작성에 집중할 수 있습니다.

### 서블릿 디스패처의 목적
이전 장에서 RESTful 웹 서비스가 HTTP 프로토콜을 기반으로 개발된다는 것을 배웠습니다. Java에는 HTTP 프로토콜과 함께 작동하는 서블릿 기능이 있습니다. 서블릿을 사용하면 REST 끝점에서 작동할 수 있는 경로 매핑을 가질 수 있으며 식별을 위한 HTTP 메서드를 제공합니다. 또한 JSON 및 XML을 비롯한 다양한 유형의 응답 개체를 구성할 수 있습니다. 그러나 REST 끝점을 구현하는 조잡한 방법입니다. 요청 URI를 처리하고 매개변수를 구문 분석하고 JSON/XML을 변환하고 응답을 처리해야 합니다.

Spring MVC가 당신을 구해줍니다. Spring MVC는 MVC패턴을 기반으로 하며 첫 번째 릴리스부터 Spring Framework의 일부였습니다. MVC는 잘 알려진 디자인 패턴입니다.

- 모델: 모델은 애플리케이션 데이터를 포함하는 Java 객체(POJO)입니다. 또한 응용 프로그램의 상태를 나타냅니다.
- 뷰: 보기는 HTML/JSP/템플릿 파일로 구성된 프레젠테이션 계층입니다. 보기는 모델에서 데이터를 렌더링하고 HTML 출력을 생성합니다.
- 컨트롤러: 컨트롤러는 사용자 요청을 처리하고 모델을 빌드합니다.

DispatcherServlet은 Spring MVC의 일부입니다. 전면 컨트롤러로 작동합니다. 즉, 들어오는 모든 HTTP 요청을 처리합니다. Spring MVC는 UI 앱도 백엔드의 일부인 기존 웹 애플리케이션을 개발할 수 있게 해주는 웹 프레임워크입니다. 그러나 RESTful 웹 서비스를 개발하고 UI는 React JavaScript 라이브러리를 기반으로 하므로 @RestController를 사용하여 REST 끝점을 구현하는 것으로 제한된 서블릿 디스패처 역할을 유지합니다.

REST 컨트롤러에 대한 Spring MVC의 사용자 요청 흐름을 살펴보겠습니다.

1. 사용자는 DispatcherServlet에서 수신한 HTTP 요청을 보냅니다.

2. DispatcherServlet은 HandlerMapping에 배턴을 전달합니다. HandlerMapping은 요청된 URI에 대한 올바른 컨트롤러를 찾는 작업을 수행하고 이를 DispatcherServlet에 다시 전달합니다.

3. 그런 다음 DispatcherServlet은 HandlerAdaptor를 사용하여 컨트롤러를 처리합니다.

4. HandlerAdaptor는 Controller 내부에서 적절한 메소드를 호출합니다.

5. 그런 다음 컨트롤러는 연결된 비즈니스 로직을 실행하고 응답을 구성합니다.

6. Spring은 Java에서 JSON/XML 변환을 위해 요청 및 응답 객체의 마샬링/마샬링 해제를 사용하며 그 반대의 경우도 마찬가지입니다.

이 프로세스를 시각적으로 표현해 보겠습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/B16561_02_02.jpg)

Figure 2.2 – DispatcherServlet

Now, you will have understood the importance of DispatcherServlet, which is key for REST API implementation.

## 요약

이 장에서는 Spring의 핵심 개념인 빈, 의존성 주입 및 AOP에 대해 배우는 데 도움이 되었습니다. 또한 빈의 범위를 정의하고 프로그래밍 방식으로 ApplicationContext를 만들고 이를 사용하여 빈을 가져오는 방법도 배웠습니다. 이제 Java 및 주석을 사용하여 빈의 구성 메타데이터를 정의할 수 있으며 동일한 유형의 다른 빈을 사용하는 방법을 배웠습니다.

또한 모듈식 접근 방식을 사용하여 횡단 문제인 샘플 Aspect를 구현하고 AOP 프로그래밍 패러다임의 핵심 개념을 배웠습니다.

이 책에서는 REST API를 구현할 것이기 때문에 Servlet Dispatcher 개념을 이해하는 것이 중요합니다.

다음 장에서는 OpenAPI 사양을 사용하여 첫 번째 REST API 애플리케이션을 구현하고 Spring 컨트롤러를 사용하여 구현합니다.

## Questions

- 프로토타입 범위로 빈을 어떻게 정의합니까?
- 프로토타입 빈과 싱글톤 빈의 차이점은 무엇인가요?
- 세션 및 요청 범위가 작동하려면 무엇이 필요합니까?
- AOP 측면에서 Advice와 Pointcut의 관계는 무엇입니까?
- 메소드 실행 전에 메소드 이름과 인수 이름을 인쇄하고 메소드의 성공적인 실행 후에 리턴 유형이 있는 경우 메시지를 인쇄하는 로깅을 위한 Aspect를 작성합니다.

## Further reading

- Inversion of Control Containers and the Dependency Injection pattern (https://martinfowler.com/articles/injection.html)

- The Spring Framework documentation (5.2.9 was the latest at the time of writing this book) (https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/)
Spring Boot 2 Fundamentals (https://www.packtpub.com/product/spring-boot-2-fundamentals/9781838821975)

- Developing Java Applications with Spring and Spring Boot (https://www.packtpub.com/in/application-development/developing-java-applications-spring-and-spring-boot)