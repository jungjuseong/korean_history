# Reactive with Spring Boot2

이전 장에서 우리는 프로젝트 리액터의 필수 요소와 리액티브 유형 및 연산자의 동작, 다양한 비즈니스 문제를 해결하는 데 도움이 되는 방법을 배웠습니다. 간단한 API와 함께 우리는 내부에 동시, 비동기, 비차단 메시지 처리를 허용하는 숨겨진 복잡한 메커니즘이 있음을 확인했습니다. 

이 외에도 사용 가능한 배압 제어 및 관련 전략을 탐색했습니다. 이전 장에서 보았듯이 `Project Reactor`는 단순한 반응형 프로그래밍 라이브러리 그 이상입니다. 또한 Spring Framework 없이도 반응형 시스템을 구축할 수 있도록 하는 애드온 및 어댑터를 제공합니다. 이를 통해 우리는 `Apache Kafka` 및 `Netty`와 `Project Reactor`의 통합을 보았습니다.

`Project Reactor`는 Spring Framework 없이도 잘 작동할 수 있지만 일반적으로 기능이 풍부한 애플리케이션을 빌드하는 것만으로는 충분하지 않습니다. 여기서 누락된 요소 중 하나는 잘 알려진 DI로 구성 요소 간의 분리를 제공합니다. 게다가 우리가 강력하고 커스터마이징 가능한 애플리케이션을 구축하는 시기는 Spring Framework가 더욱 빛날 때입니다. Spring Boot로 더 나은 애플리케이션을 구축할 수 있습니다.

따라서 이 장에서는 Spring Boot의 중요성과 Spring Boot가 제공하는 기능에 대해 설명합니다. 또한 Spring 생태계가 반응형 프로그래밍 접근 방식을 채택하는 방법을 살펴보고 Spring Framework 5 및 Spring Boot 2와 함께 제공되는 변경 사항을 볼 것입니다.

이 장에서는 다음 항목을 다룹니다.

- 스프링 부트가 해결하는 문제와 해결 방법
- 스프링 부트의 필수 요소
- Spring Boot 2.0과 Spring Framework에서의 반응성


## A fast start as the key to success

인간은 일상적인 작업과 목표와 관련이 없는 작업에 많은 시간을 보내는 것을 결코 좋아하지 않습니다. 비즈니스에서 원하는 결과를 얻으려면 빨리 배우고 빨리 실험해야 합니다. 반응성은 실생활에서도 적용됩니다. 시장의 변화에 ​​빠르게 대응하고 전략을 빠르게 변경하며 가능한 한 빨리 새로운 목표를 달성하는 것이 중요합니다. 개념을 더 빨리 증명할수록 비즈니스에 더 많은 가치를 제공하므로 연구에 지출되는 비용이 줄어듭니다.

이러한 이유로 인간은 일상적인 작업을 단순화하기 위해 항상 노력해 왔습니다. 개발자는 해당 규칙에서 제외되지 않습니다. 우리는 특히 Spring과 같은 복잡한 프레임워크에 대해 이야기할 때 즉시 사용할 수 있는 모든 것을 좋아합니다. Spring Framework가 도입한 많은 이점과 유리한 기능에도 불구하고 작업 방법에 대한 깊은 이해가 필요하며 초보 개발자는 경험이 없는 영역에 참여하면 쉽게 실패할 수 있습니다. 가능한 함정의 좋은 예는 최소한 5개의 가능한 구성 방법을 계산하는 간단한 IoC(제어 반전) 컨테이너 구성입니다. 문제를 이해하기 위해 다음 코드 샘플을 살펴보겠습니다.

```java
public class SpringApp {
   public static void main(String[] args) { 
      GenericApplicationContext context = new GenericApplicationContext();

      new XmlBeanDefinitionReader(context).loadBeanDefinitions("services.xml");

      new GroovyBeanDefinitionReader(context).loadBeanDefinitions("services.groovy");

      new PropertiesBeanDefinitionReader(context).loadBeanDefinitions("services.properties");

      context.refresh(); 
   } 
}
```

앞의 코드에서 볼 수 있듯이 원시 Spring Framework에는 Spring 컨텍스트에서 빈을 등록하는 세 가지 다른 방법이 있습니다.

한편으로 Spring Framework는 빈의 소스를 구성하는 데 있어 유연성을 제공합니다. 반면에, 이렇게 하기 위한 광범위한 옵션 목록을 갖는 데는 몇 가지 문제가 있습니다. 예를 들어 문제 중 하나는 쉽게 디버깅할 수 없는 XML 구성입니다. 이러한 구성으로 작업하기 더 어렵게 만드는 또 다른 문제는 IntelliJ IDEA 또는 Spring Tool Suite와 같은 추가 도구 없이 해당 구성의 정확성을 검증할 수 없다는 것입니다. 마지막으로, 코딩 스타일 및 개발 규칙에 대한 적절한 규율의 부족은 대규모 프로젝트의 복잡성을 크게 증가시키고 명확성을 감소시킬 수 있습니다. 예를 들어, 빈 정의에 대한 접근 방식에 적절한 규율이 ​​없으면 팀의 한 개별 개발자가 XML로 빈을 정의하고 다른 개발자가 속성에서 이를 수행할 수 있기 때문에 향후 프로젝트를 복잡하게 만들 수 있습니다. 다른 사람은 Java 구성에서 동일한 작업을 수행할 수 있습니다. 결과적으로 새로운 개발자는 이러한 불일치에 쉽게 혼동을 느끼고 필요한 것보다 훨씬 더 오랫동안 프로젝트에 몰두할 수 있습니다.

간단한 IoC와 함께 Spring Frameworks는 Spring Web 모듈이나 Spring Data 모듈과 같은 훨씬 더 복잡한 기능을 제공합니다. 두 모듈 모두 응용 프로그램을 실행하는 데만 많은 구성이 필요합니다. 문제는 일반적으로 개발된 응용 프로그램이 플랫폼 독립적이어야 하는 경우 발생합니다. 즉, 구성 및 상용구 코드의 수가 증가하고 비즈니스 관련 코드가 줄어듭니다.

여기서 플랫폼 독립적이란 Servlet API와 같은 특정 서버 API에 독립적이라는 것을 의미합니다. 또는 특정 환경과 해당 구성 및 기타 기능을 인식하지 못하는 것을 말합니다.
예를 들어, 간단한 웹 애플리케이션만 구성하려면 다음 코드와 같이 최소 7줄의 상용구가 필요합니다.

```java
public class MyWebApplicationInitializer 
 implements WebApplicationInitializer {      
   @Override   
   public void onStartup(ServletContext servletCxt) { 
      AnnotationConfigWebApplicationContext cxt =
            new AnnotationConfigWebApplicationContext();
      cxt.register(AppConfig.class);     
      cxt.refresh();
      DispatcherServlet servlet = new DispatcherServlet(cxt);     
      ServletRegistration.Dynamic registration = servletCxt
            .addServlet("app", servlet);
      registration.setLoadOnStartup(1);
      registration.addMapping("/app/*");
   }
}
```

앞의 코드에는 보안 구성이나 콘텐츠 렌더링과 같은 기타 필수 기능이 포함되어 있지 않습니다. 어느 시점에서 각 Spring 기반 애플리케이션에는 최적화되지 않은 유사한 코드 조각이 있었고 개발자의 추가 주의가 필요하여 결과적으로 정당한 이유 없이 돈을 낭비했습니다.

## Using Spring Roo to try to develop applications faster

다행히 Spring 팀은 빠른 프로젝트 시작의 중요성을 이해했습니다. 2009년 초에 Spring Roo라는 새로운 프로젝트가 발표되었습니다(자세한 내용은 https://projects.spring.io/spring-roo 참조). 이 프로젝트는 신속한 애플리케이션 개발을 목표로 했습니다. Spring Roo의 주요 아이디어는 구성을 초과하는 방식을 사용하는 것입니다. 이를 위해 Spring Roo는 인프라 및 도메인 모델을 초기화하고 몇 가지 명령으로 REST API를 생성할 수 있는 명령줄 사용자 인터페이스를 제공합니다. Spring Roo는 애플리케이션 개발 프로세스를 단순화합니다. 그러나 이러한 도구를 대규모 응용 프로그램 개발에 사용하는 것은 실제로 작동하지 않는 것 같았습니다. 여기서 문제는 프로젝트의 구조가 복잡해지거나 사용된 기술이 Spring Framework의 범위를 벗어날 때 발생합니다. 마지막으로 Spring Roo도 일상적으로 사용하기에는 그다지 인기가 없었습니다. 결과적으로, 신속한 애플리케이션 개발에 대한 질문은 아직 풀리지 않은 상태로 남아 있습니다.


## Spring Boot as a key to fast-growing applications

2012년 말 Mike Youngstrom은 Spring Framework의 미래에 영향을 미치는 문제를 제기했습니다. 그가 제안한 요점은 Spring Architecture 전체를 변경하고 Spring Framework의 사용을 단순화하여 개발자가 비즈니스 로직을 더 빨리 구축할 수 있도록 하는 것이었습니다. 비록 그 제안이 거절되었지만, 그것은 Spring 팀이 Spring Framework 사용을 극적으로 단순화하는 새로운 프로젝트를 만들도록 동기를 부여했습니다. 

2013년 중반에 Spring 팀은 Spring Boot라는 이름으로 프로젝트의 첫 번째 프리릴리즈를 발표했습니다(자세한 내용은 https://spring.io/projects/spring-boot 참조). Spring Boot의 주요 아이디어는 애플리케이션 개발 프로세스를 단순화하고 사용자가 추가 인프라 구성 없이 새 프로젝트를 시작할 수 있도록 하는 것이었습니다.

이와 함께 `Spring Boot`는 컨테이너 없는 웹 애플리케이션 아이디어와 실행 가능한 팻 JAR 기술을 채택합니다. 이 접근 방식을 사용하면 Spring 애플리케이션을 한 줄에 작성하고 하나의 추가 명령줄로 실행할 수 있습니다. 다음 코드는 완전한 `Spring Boot` 웹 애플리케이션을 보여줍니다.

```java
@SpringBootApplication 
public class MyApplication {
   public static void main(String[] args) {
      SpringApplication.run(MyApplication.class, args);
   }
}
```
앞의 예에서 가장 중요한 부분은 IoC 컨테이너를 실행하는 데 필요한 `@SpringBootApplication`이라는 주석이 있다는 것입니다. MVC 서버와 다른 응용 프로그램 구성 요소도 있습니다. 이에 대해 조금 더 깊이 파헤쳐 보겠습니다. 우선, Spring Boot는 Gradle 또는 Maven과 같은 최신 빌드 도구에 대한 추가 모듈 및 추가 사항입니다. 

일반적으로 Spring Boot에는 의존하는 두 개의 중앙 모듈이 있습니다. 

- 첫 번째는 Spring IoC 컨테이너와 관련된 모든 가능한 기본 구성과 함께 제공되는 spring-boot 모듈입니다. 
- 
- 두 번째는 Spring Data, Spring MVC, Spring WebFlux 등과 같은 기존의 모든 Spring 프로젝트에 대해 가능한 모든 구성을 가져오는 spring-boot-autoconfigure입니다. 언뜻 보기에는 정의된 모든 구성이 필요하지 않더라도 한 번에 활성화되는 것처럼 보입니다. 그러나 이것은 사실이 아니며 특정 종속성이 도입될 때까지 모든 구성이 비활성화됩니다. 

Spring Boot는 일반적으로 이름에 `-starter-`라는 단어가 포함된 모듈에 대한 새로운 개념을 정의합니다. 기본적으로 스타터에는 Java 코드가 포함되어 있지 않지만 `spring-boot-autoconfigure`에서 특정 구성을 활성화하기 위해 모든 관련 종속성을 가져옵니다. Spring Boot를 사용하면 이제 `-starter-web` 및 `-starter-data-jpa` 모듈이 있어 추가 번거로움 없이 필요한 모든 인프라 부품을 구성할 수 있습니다. Spring Roo 프로젝트에서 가장 눈에 띄는 차이점은 더 큰 유연성입니다. 쉽게 확장할 수 있는 기본 구성과 함께 Spring Boot는 자체 starter를 구축하기 위한 유창한 API를 제공합니다. 이 API는 기본 구성을 대체하고 특정 모듈에 대한 자체 구성을 제공합니다.


## Reactive in Spring Boot 2.0

이 책은 반응형 프로그래밍에 관한 것이기 때문에 Spring Boot에 대해 너무 자세히 다루지는 않을 것입니다. 그러나 이전 섹션에서 언급했듯이 애플리케이션을 빠르게 부트스트랩하는 기능은 성공적인 프레임워크의 핵심 요소입니다. Spring 생태계에서 반응성이 어떻게 반영되는지 알아봅시다. Spring MVC 및 Spring Data 모듈의 차단 특성 때문에 프로그래밍 패러다임을 반응형 프로그래밍으로 변경하는 것만으로는 이점이 없으므로 Spring 팀은 해당 모듈 내부의 전체 패러다임도 변경하기로 결정했습니다. 이를 위해 Spring 생태계는 반응 모듈 목록을 제공합니다. 이 섹션에서는 이러한 모든 모듈을 매우 간략하게 다룰 것입니다. 그러나 대부분은 책 뒷부분의 전용 장에서 다룹니다.


## Reactive in Spring Core

Spring 생태계의 중심 모듈은 `Spring Core` 모듈입니다. Spring Framework 5.x와 함께 도입된 눈에 띄는 개선 사항 중 하나는 `RxJava 1/2` 및 `Project Reactor 3`과 같은 `Reactive Streams` 및 Reactive 라이브러리에 대한 기본 지원이었습니다.

## 반응형 타입 변환 지원

`Reactive Streams` 사양을 지원하기 위한 가장 포괄적인 개선 사항 중 하나는 `ReactiveAdapter` 및 `ReactiveAdapterRegistry`의 도입입니다. `ReactiveAdapter` 클래스는 다음 코드와 같이 반응형 변환을 위한 두 가지 기본 메서드를 제공합니다.

```java
class ReactiveAdapter {
   ...
   <T> Publisher<T> toPublisher(@Nullable Object source) { ... }   // (1)
   Object fromPublisher(Publisher<?> publisher) { ... }            // (2)
}
```
(1) `ReactiveAdapter`는 모든 형식을 `Publisher<T>`로 변환하고 다시 Object로 변환하기 위한 두 가지 기본 메서드를 소개합니다. 예를 들어, `RxJava 2`에서 `Maybe` 리액티브 타입 변환을 제공하기 위해 다음과 같은 방식으로 자체 `ReactiveAdapter`를 만들 수 있습니다.

```java
public class MaybeReactiveAdapter extends ReactiveAdapter { // (1)

   public MaybeReactiveAdapter() { // (2)
      super(
         ReactiveTypeDescriptor // (3)
            .singleOptionalValue(Maybe.class, Maybe::empty), //
         rawMaybe -> ((Maybe<?>)rawMaybe).toFlowable(), // (4)
         publisher -> Flowable.fromPublisher(publisher) // (5)
                              .singleElement() //
      );
   }
}
```
(1) 기본 `ReactiveAdapter`를 확장하고 사용자 정의 구현을 제공합니다. 
(2) 기본 생성자를 제공하고 그 뒤에 구현의 세부 사항을 숨깁니다
(3) 부모 생성자의 첫 번째 매개변수는 `ReactiveTypeDescriptor` 인스턴스의 정의입니다.

`ReactiveTypeDescriptor`는 `ReactiveAdapte`r에서 사용되는 반응 유형에 대한 정보를 제공합니다. 마지막으로 부모 생성자는 원시 개체(May로 가정됨)를 게시자(4)로 변환하고 모든 게시자를 다시 Maybe로 변환하는 변환 함수(이 경우 람다)의 정의가 필요합니다.

ReactiveAdapter는 개체를 toPublisher 메서드에 전달하기 전에 `ReactiveAdapter#getReactiveType` 메서드를 사용하여 개체의 유형 호환성을 확인한다고 가정합니다.

상호 작용을 단순화하기 위해 다음 코드와 같이 ReactiveAdapter의 인스턴스를 한 곳에 보관하고 액세스를 일반화할 수 있는 ReactiveAdapterRegistry가 있습니다.

```java
ReactiveAdapterRegistry
   .getSharedInstance()                                            // (1)
   .registerReactiveType(                                          // (2)
      ReactiveTypeDescriptor
         .singleOptionalValue(Maybe.class, Maybe::empty),
      rawMaybe -> ((Maybe<?>)rawMaybe).toFlowable(),
      publisher -> Flowable.fromPublisher(publisher)
                           .singleElement()
   );

...

ReactiveAdapter maybeAdapter = ReactiveAdapterRegistry
   .getSharedInstance()                                            // (1)
   .getAdapter(Maybe.class);                                       // (3)
```
As we can see, the ReactiveAdapterRegistry represents a common pool of ReactiveAdapter instances for different reactive types. In turn, ReactiveAdapterRegistry provides a singleton instance (1) that might be used in many places within the framework or that may be employed in the developed application. Along with that, the registry makes it possible to register an adapter by providing the same list of parameters as in the previous example (2). Finally, we may get an existing adapter by providing the Java class to which the conversion should be done (3).