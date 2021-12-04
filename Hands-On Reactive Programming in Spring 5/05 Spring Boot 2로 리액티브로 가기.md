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

Spring MVC 및 Spring Data 모듈의 블로킹 특성 때문에 프로그래밍 패러다임을 반응형으로 변경하는 것만으로는 이점이 없으므로 Spring 팀은 해당 모듈 내부의 전체 패러다임도 변경하기로 결정했습니다. 이를 위해 Spring 생태계는 반응 모듈 목록을 제공합니다. 이 섹션에서는 이러한 모든 모듈을 매우 간략하게 다룰 것입니다.

## Reactive in Spring Core

Spring 생태계의 중심 모듈은 `Spring Core` 모듈입니다. Spring Framework 5.x와 함께 도입된 눈에 띄는 개선 사항 중 하나는 `RxJava 1/2` 및 `Project Reactor 3`과 같은 `Reactive Streams` 및 Reactive 라이브러리에 대한 기본 지원이었습니다.

### 반응형 타입 변환 지원

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
         ReactiveTypeDescriptor.singleOptionalValue(Maybe.class, Maybe::empty), // (3)
         rawMaybe -> ((Maybe<?>)rawMaybe).toFlowable(), // (4)
         publisher -> Flowable.fromPublisher(publisher).singleElement() // (5)
      );
   }
}
```
(1) 기본 `ReactiveAdapter`를 확장하고 사용자 정의 구현을 제공합니다. 
(2) 기본 생성자를 제공하고 그 뒤에 구현의 세부 사항을 숨깁니다
(3) 부모 생성자의 첫 번째 매개변수는 `ReactiveTypeDescriptor` 인스턴스의 정의입니다.

`ReactiveTypeDescriptor`는 `ReactiveAdapte`r에서 사용되는 반응형 타입에 대한 정보를 제공합니다. 마지막으로 부모 생성자는 원시 객체(May로 가정됨)를 게시자(4)로 변환하고 모든 게시자를 다시 Maybe로 변환하는 변환 함수(이 경우 람다)의 정의가 필요합니다.

ReactiveAdapter는 객체를 toPublisher() 메서드에 전달하기 전에 `ReactiveAdapter#getReactiveType` 메서드를 사용하여 객체의 타입 호환성을 확인한다고 가정합니다.

상호 작용을 단순화하기 위해 `ReactiveAdapter`의 인스턴스를 한 곳에 보관하고 액세스를 일반화할 수 있는 `ReactiveAdapterRegistry`가 있습니다.

```java
ReactiveAdapterRegistry
   .getSharedInstance() // (1)
   .registerReactiveType( // (2)
      ReactiveTypeDescriptor.singleOptionalValue(Maybe.class, Maybe::empty),
      rawMaybe -> ((Maybe<?>)rawMaybe).toFlowable(),
      publisher -> Flowable.fromPublisher(publisher).singleElement()
   );
...

ReactiveAdapter maybeAdapter = ReactiveAdapterRegistry
   .getSharedInstance() // (1)
   .getAdapter(Maybe.class); // (3)
```

ReactiveAdapterRegistry는 다양한 반응형 타입에 대한 ReactiveAdapter 인스턴스의 공통 풀을 나타냅니다. 

(1) 결과적으로 ReactiveAdapterRegistry는 프레임워크 내의 여러 위치에서 사용되거나 개발된 애플리케이션에 사용될 수 있는 싱글톤 인스턴스를 제공합니다. 

(2) 레지스트리를 통해 이전 예와 동일한 매개변수 목록을 제공하여 어댑터를 등록할 수 있습니다. 

(3) 마지막으로 변환을 수행해야 하는 Java 클래스를 제공하여 기존 어댑터를 얻을 수 있습니다.


### Reactive I/O

반응형 지원과 관련된 또 다른 극적인 개선 사항은 코어 I/O 패키지의 강화입니다. 우선, Spring Core 모듈은 **DataBuffer**라는 바이트 인스턴스의 버퍼에 대한 추가 추상화를 도입했습니다. `java.nio.ByteBuffer`를 피하는 주된 이유는 서로 다른 바이트 버퍼를 추가로 변환할 필요 없이 서로 다른 바이트 버퍼를 지원할 수 있는 추상화를 제공하는 것입니다. 

예를 들어, `io.netty.buffer.ByteBuf`를 `ByteBuffer`로 변환하려면 힙이 아닌 공간에서 힙으로 가져와야 할 수 있는 저장된 바이트에 액세스해야 합니다. 이것은 Netty에서 제공하는 효율적인 메모리 사용 및 버퍼 재활용(동일한 바이트 버퍼 재사용)을 중단할 수 있습니다. 또는 Spring DataBuffer는 특정 구현의 추상화를 제공하고 일반적인 방식으로 기본 구현을 사용할 수 있도록 합니다. **PooledDataBuffer**라고 하는 추가 하위 인터페이스도 참조 카운팅 기능을 활성화하고 기본적으로 효율적인 메모리 관리를 허용합니다.

또한 Reactive Streams의 형태로 I/O와 상호 작용이 가능한 추가 **DataBufferUtils** 클래스를 도입합니다. 
예를 들어 셰익스피어의 햄릿을 반응형으로 배압 지원하면서 읽을 수 있습니다.

```java
Flux<DataBuffer> reactiveHamlet = DataBufferUtils
   .read(
      new DefaultResourceLoader().getResource("hamlet.txt"),
      new DefaultDataBufferFactory(),
      1024
   );
```
`DataBufferUtils.read`는 DataBuffer 인스턴스의 Flux를 반환합니다. 따라서 우리는 Hamlet을 읽기 위해 Reactor의 모든 기능을 사용할 수 있습니다.

마지막으로 Spring Core에서 Reactive와 관련된 중요 기능은 반응형 코덱입니다. 반응형 코덱은 DataBuffer 인스턴스의 스트림을 객체의 스트림으로 변환하고 그 반대로 변환하는 편리 기능을 제공합니다. 

이를 위해 데이터 스트림을 인코딩/디코딩하기 위한 다음 API를 제공하는 인코더 및 디코더 인터페이스가 있습니다.

```java
interface Encoder<T> {
   ...
   Flux<DataBuffer> encode(Publisher<? extends T> inputStream, ...);
}

interface Decoder<T> {
   ...
   Flux<T> decode(Publisher<DataBuffer> inputStream, ...);
   Mono<T> decodeToMono(Publisher<DataBuffer> inputStream, ...);

}
```

앞의 예에서 두 인터페이스 모두 Reactive Streams의 게시자와 함께 작동하며 DataBuffer 인스턴스 스트림을 객체에 인코딩/디코딩할 수 있습니다. 주요 이점은 직렬화된 데이터를 Java 객체로 또는 그 반대로 변환하는 넌블로킹 방식을 제공한다는 것입니다. 또한, 이러한 방식은 Reactive Streams의 특성이 독립적인 요소 처리를 허용하므로 마지막 바이트가 전체 데이터 세트 디코딩을 시작할 때까지 기다릴 필요가 없기 때문에 처리 대기 시간을 줄일 수 있습니다. 반대로 인코딩을 시작하고 I/O 채널로 보내기 위해 전체 개체 목록을 가질 필요가 없으므로 양방향이 향상될 수 있습니다.

Spring Core의 Reactive I/O에 대해 자세히 알아보려면 https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#databuffers를 방문하세요.

요약하자면, Spring5 Core는 반응형 프로그래밍을 위한 훌륭한 기반입니다. Spring Boot는 해당 기반을 모든 애플리케이션에 백본 구성 요소로 제공합니다. 또한 반응형 애플리케이션을 작성하는 동시에 반응형 타입을 변환하고, 반응형 스타일로 I/O로 작업하고, 즉석에서 데이터를 인코딩/디코딩하는 방법을 개발하는 데 더 적은 노력을 기울일 수 있습니다.

### Reactive in web

또 다른 중요한 점은 웹 모듈의 불가피한 변경입니다. 우선 Spring Boot 2는 **WebFlux**라는 새로운 웹 스타터를 도입하여 처리량이 높고 대기 시간이 짧은 애플리케이션에 새로운 기회를 제공합니다. `Spring WebFlux` 모듈은 Reactive Streams 어댑터 위에 구축되었으며 일반 `Servlet-API-3.1` 기반 서버의 지원과 함께 Netty 및 Undertow와 같은 서버 엔진과의 통합을 제공합니다. 이러한 방식으로 **Spring WebFlux**는 넌블로킹 기반을 제공하고 비즈니스 로직 코드와 서버 엔진 간의 상호 작용을 위한 중심적인 추상화로서 Reactive Streams에 대한 새로운 가능성을 열어줍니다.

Servlet API 3.1용 어댑터는 WebMVC 어댑터와 다른 순수 비동기 및 넌블로킹 통합을 제공합니다. 물론 Spring WebMVC 모듈은 HTTP/2 지원을 가능하게 하는 Servlet API 4.0도 지원합니다.

Spring WebFlux는 `Reactor3`을 일등급 객체로 광범위하게 사용합니다. 이 때문에 추가 노력 없이 기본적으로 반응형 프로그래밍을 사용할 수 있으며 Netty와 Project Reactor의 내장 통합 위에서 웹 애플리케이션을 실행할 수도 있습니다. 마지막으로 WebFlux 모듈은 내장된 배압 지원을 제공하므로 I/O가 과부하되지 않도록 할 수 있습니다. 서버 측 상호 작용의 변경 사항 외에도 차단되지 않는 클라이언트 측 상호 작용을 가능하게 하는 새로운 `WebClient` 클래스를 제공합니다.

또한 WebFlux 모듈의 도입과 함께 기존의 좋은 WebMVC 모듈도 Reactive Streams에 대한 일부 지원을 얻었습니다. 스프링5 버전부터 Servlet API 3.1도 WebMVC 모듈의 기준이 되었습니다. 이것은 WebMVC가 이제 서블릿 사양에서 제안된 형식으로 넌블로킹 I/O를 지원한다는 것을 의미해야 합니다. 그러나 WebMVC 모듈의 디자인은 적절한 수준의 넌블로킹 I/O에 대해 많이 변경되지 않았습니다. 그럼에도 불구하고 Servlet 3.0의 비동기 동작은 한동안 제대로 구현되었습니다. 반응형 지원의 격차를 메우기 위해 Spring WebMVC는 `ResponseBodyEmitterReturnValueHandler` 클래스에 대한 업그레이드를 제공합니다. `Publisher` 클래스는 이벤트의 무한 스트림으로 간주될 수 있으므로 `Emitter` 핸들러는 WebMVC 모듈의 전체 인프라를 손상시키지 않고 반응형 처리를 위한 논리를 배치하기에 적절한 위치입니다. 이를 위해 WebMVC 모듈은 Flux 및 Mono와 같은 반응형 타입을 적절하게 처리하는 `ReactiveTypeHandler` 클래스를 도입합니다.

서버 측에서 반응형을 지원하는 변경 사항뿐만 아니라 클라이언트 측에서 비차단 동작을 얻기 위해 WebFlux 모듈에서 가져온 동일한 WebClient를 사용할 수 있습니다. 언뜻보기에 이것은 두 모듈 사이에 충돌을 일으키는 것처럼 보일 수 있습니다. 다행스럽게도 Spring Boot가 구출되어 클래스 경로에서 사용 가능한 클래스를 기반으로 정교한 환경 관리 동작을 제공합니다. 결과적으로 WebFlux와 함께 WebMVC(spring-boot-starter-web) 모듈을 제공함으로써 WebFlux 모듈에서 WebMVC 환경과 비차단 반응 WebClient를 얻습니다.

마지막으로 두 모듈을 반응형 파이프로 비교하면 다음 다이어그램에 표시된 종류의 배치을 얻습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/0d1fcbed-198b-464f-9618-c4919e1cfa17.png)

Diagram 5.1. 파이프 형태의 반응형 WebFlux와 부분적으로 반응형인 WebMVC 모듈

WebMVC 또는 WebFlux 모두 거의 동일한 Reactive Streams 기반 프로그래밍 모델을 얻습니다. 
주목할만한 차이점 중 하나는 WebMVC가 이전 모듈의 설계에서 비롯된 Servlet API와의 통합 지점에서 쓰기/읽기를 차단해야 한다는 것입니다. 이 결함은 반응 스트림 내의 상호 작용 모델을 저하시키고 일반 PULL 모델로 다운그레이드합니다. WebMVC는 이제 내부적으로 모든 차단 읽기/쓰기 전용 스레드 풀을 사용합니다. 따라서 예기치 않은 동작을 방지하기 위해 적절하게 구성해야 합니다.

반면에 WebFlux 통신 모델은 네트워크 처리량과 자체 제어 흐름을 정의할 수 있는 기본 전송 프로토콜에 따라 다릅니다.

요약하자면, Spring 5는 Reactive Streams 사양과 Project Reactor를 사용하여 반응형 비차단 애플리케이션을 구축하기 위한 강력한 도구를 소개합니다. 또한 Spring Boot는 강력한 의존성 관리 및 자동 구성을 가능하게 합니다. 새로운 Reactive Web의 기능에 대해 자세히 설명하지는 않겠지만 6장 WebFlux Async Non-Blocking Communication에서 WebFlux 모듈을 광범위하게 다룰 것입니다.


## Reactive in Spring Data

웹 계층의 변경 사항과 마찬가지로 대부분의 애플리케이션에서 또 다른 중요한 부분은 스토리지와 상호 작용하는 데이터 계층입니다. 수년 동안 일상적인 개발을 단순화한 강력한 솔루션은 저장소 패턴을 통해 데이터 액세스를 위한 편리한 추상화를 제공하는 Spring Data 프로젝트입니다. 초기부터 Spring Data는 기본 저장 영역에 대한 동기 차단 액세스를 제공했습니다. 다행히 5세대 Spring Data 프레임워크는 데이터베이스 계층에 대한 반응 및 비차단 액세스에 대한 새로운 가능성을 제공합니다. 새로운 세대에서 Spring Data는 `ReactiveCrudRepository` 인터페이스를 제공합니다. 이 인터페이스는 반응 워크플로와의 원활한 통합을 위해 Project Reactor의 반응 유형을 노출합니다. 결과적으로 데이터베이스 커넥터가 완전히 반응하는 응용 프로그램의 효율적인 부분이 될 수 있습니다.

Reactive Repository와 마찬가지로 Spring Data는 `ReactiveCrudRepository` 인터페이스를 확장하여 저장 방법과 통합하는 몇 가지 모듈을 제공합니다. 다음은 이제 Spring Data에 반응적 통합이 있는 저장 방법 목록입니다.

- `Spring Data Mongo Reactive`: NoSQL 데이터베이스와 완전히 반응하고 차단되지 않는 상호 작용이며 적절한 배압 제어이기도 합니다.

- `Spring Data Cassandra Reactive`: TCP 흐름 제어에 대한 백프레셔를 지원하는 Cassandra 데이터 저장소와의 비동기 및 비차단 상호작용입니다.

- `Spring Data Redis Reactive`: Lettuce Java 클라이언트를 통한 Redis와의 반응 통합입니다.

- `Spring Data Couchbase Reactive`: RxJava 기반 드라이버를 통해 Couchbase 데이터베이스와 반응하는 Spring Data 통합입니다.

또한 이러한 모든 모듈은 선택한 저장 방법과의 원활한 통합을 제공하는 추가 스타터 모듈을 제공하는 Spring Boot에서 지원됩니다.

게다가 Spring Data는 NoSQL 데이터베이스와 마찬가지로 곧 Reactive JDBC 연결을 제공할 수 있는 JDBC와의 경량 통합인 Spring Data JDBC도 도입합니다. 리액티브 데이터 액세스는 7장, 리액티브 데이터베이스 액세스에서 다룹니다.

요약하자면, 5세대 Spring Data는 웹 끝점에서 반응형 데이터베이스 통합까지 종단 간 반응형 데이터 스트림을 완성하며, 이는 대부분의 애플리케이션의 요구 사항을 충족합니다. 다음 섹션에서 볼 수 있듯이 다른 Spring Framework 모듈의 대부분의 개선 사항은 WebFlux 또는 Reactive Spring Data 모듈의 반응형 기능을 기반으로 합니다.


### 스프링 세션에서 반응형

Spring 웹 모듈과 관련된 또 다른 중요 업데이트는 Spring Session 모듈의 반응 지원입니다.

이제 세션 관리를 위한 효율적인 추상화를 사용할 수 있도록 WebFlux 모듈에 대한 지원을 받습니다. 이를 위해 Spring Session은 Reactor의 Mono 유형으로 저장된 세션에 대한 비동기식 비차단 액세스를 가능하게 하는 **ReactiveSessionRepository**를 도입합니다.

그 외에도 Spring Session은 반응형 Spring 데이터를 통한 세션 저장소로 Redis와의 반응형 통합을 제공합니다. 이러한 방식으로 다음 종속성을 포함하는 것만으로 분산 WebSession을 얻을 수 있습니다.

```gradle
org.springframework.session:spring-session-data-redis
org.springframework.boot:spring-boot-starter-webflux
org.springframework.boot:spring-boot-starter-data-redis-reactive
```

앞의 gradle 종속성 예제에서 볼 수 있듯이 반응형 Redis WebSession 관리를 달성하려면 이 세 가지 종속성을 한 곳에서 결합해야 합니다. 차례로 Spring Boot는 웹 애플리케이션을 원활하게 실행하기 위해 빈의 정확한 조합을 제공하고 적합한 자동 구성을 생성합니다.


### 스프링 시큐리티의 반응형

WebFlux 모듈을 적용하기 위해 Spring 5는 Spring Security 모듈에서 향상된 반응형 지원을 제공합니다. 여기서 핵심 개선 사항은 Project Reactor를 통한 반응형 프로그래밍 모델에 대한 지원입니다. 기억할 수 있듯이 이전 Spring Security는 ThreadLocal을 SecurityContext 인스턴스의 저장 방법으로 사용했습니다. 이 기술은 하나의 스레드 내에서 실행되는 경우에 잘 작동합니다. 언제든지 ThreadLocal 저장소에 저장된 SecurityContext에 액세스할 수 있습니다. 

그러나 비동기 통신이 시행되면 문제가 발생합니다. 여기에서 ThreadLocal 콘텐츠를 다른 Thread로 전송하기 위한 추가 노력을 제공해야 하며, Thread 인스턴스 간에 전환하는 각 인스턴스에 대해 이 작업을 수행합니다. Spring Framework가 추가 ThreadLocal 확장을 사용하여 스레드 간의 SecurityContext 전송을 단순화하더라도 Project Reactor 또는 유사한 반응 라이브러리를 사용하여 반응 프로그래밍 패러다임을 적용할 때 여전히 문제가 발생할 수 있습니다.

다행히도 새로운 세대의 Spring Security는 Flux 또는 Mono 스트림 내에서 보안 컨텍스트를 전송하기 위해 **리액터 컨텍스트** 기능을 사용합니다. 이런 식으로 다른 실행 스레드에서 작동할 수 있는 복잡한 반응 스트림에서도 보안 컨텍스트에 안전하게 액세스할 수 있습니다. 이러한 기능이 반응 스택 내에서 구현되는 방법에 대한 자세한 내용은 6장, WebFlux 비동기 비차단 통신에서 다룹니다.