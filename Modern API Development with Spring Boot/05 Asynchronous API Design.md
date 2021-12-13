# 5장: 비동기 API 설계

지금까지 호출이 동기식인 기존 모델을 기반으로 RESTful 웹 서비스를 개발했습니다. 
코드를 비동기 및 넌블로킹으로 만들고 싶다면 어떻게 해야 할까요? 
호출이 비동기 및 넌블로킹인 이 장에서 비동기 API 설계에 대해 배울 것입니다. 
자체적으로 Project Reactor(https://projectreactor.io)를 기반으로 하는 Spring WebFlux를 사용하여 이러한 API를 개발할 것입니다.

먼저 Reactive 프로그래밍 기본 사항을 살펴본 다음 기존 전자 상거래 REST API(4장)를 비동기식(Reactive) API로 마이그레이션하여 기존 방식과 Reactive 방식을 비교합니다.

이 장에서는 다음 항목에 대해 설명합니다.

- reactive 스트림 이해
- Spring WebFlux 탐색
- DispatcherHandler 이해
- 컨트롤러
- 함수형 엔드포인트
- 전자상거래 앱을 위한 Reactive API 구현

이 장의 끝에서 반응형 API를 개발 및 구현하고 비동기식 API 개발을 탐색하는 방법을 배우게 됩니다.

## Technical requirements

The code present in this chapter is found at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter05.

## 리액티브 스트림 이해

일반 Java 코드는 스레드 풀을 사용하여 비동기성을 구현합니다. 웹 서버는 요청을 처리하기 위해 스레드 풀을 사용합니다. 각 수신 요청에 스레드를 할당합니다. 응용 프로그램은 DB 연결에 스레드 풀을 사용합니다. 각 DB 호출은 별도의 스레드를 사용하고 결과를 기다립니다. 따라서 각 웹 요청 및 DB 호출은 자체 스레드를 사용합니다. 그러나 이와 관련된 wait이 있으므로 블로킹 호출입니다. 스레드는 DB에서 응답이 다시 수신되거나 응답 개체가 작성될 때까지 대기하고 리소스를 활용합니다. 이것은 JVM에서 사용할 수 있는 리소스만 사용할 수 있으므로 확장할 때 일종의 제한 사항입니다. 수평적 확장의 한 유형인 서비스의 다른 인스턴스와 함께 로드 밸런서를 사용하여 이 제한을 극복합니다.

지난 10년 동안 클라이언트-서버 아키텍처가 증가했습니다. 많은 IoT 지원 장치, 기본 앱이 있는 스마트폰, 일류 웹 앱 및 기존 웹 응용 프로그램이 등장했습니다. 응용 프로그램에는 타사 서비스가 있을 뿐만 아니라 다양한 데이터 소스가 있으므로 더 큰 응용 프로그램으로 연결됩니다. 또한 마이크로 서비스 기반 아키텍처는 서비스 자체 간의 통신을 증가시켰습니다. 이러한 상위 네트워크 통신 수요를 처리하려면 많은 리소스가 필요하므로 스케일링이 필요합니다. 

스레드는 비싸고 무한하지 않지만 효과적인 활용을 위해 차단하고 싶지 않습니다. 이것이 비동기성이 도움이 되는 곳입니다. 비동기식 호출에서 스레드는 호출이 완료되는 즉시 해제되고 JavaScript와 같은 콜백 유틸리티를 사용합니다. 소스에서 데이터를 사용할 수 있는 경우 데이터를 푸시합니다. 

Reactive 스트림은 데이터 소스인 게시자가 데이터를 구독자에게 푸시하는 게시자-구독자 모델을 사용합니다. 반면에 `node.js`는 단일 스레드를 사용하여 대부분의 리소스를 사용한다는 것을 알고 있을 것입니다. 이벤트 루프라고 하는 비동기식 넌블로킹 설계를 기반으로 합니다.

Reactive API는 이벤트 루프 디자인을 기반으로 하며 푸시 스타일 알림을 사용합니다.
`Reactive Streams`는 map, flatMap, filter와 같은 Java Streams 작업도 지원합니다. 내부적으로 `Reactive Streams`는 푸시 스타일을 사용하는 반면 Java Stream은 풀 모델에서 작동합니다. 즉, 컬렉션과 같은 소스에서 항목을 가져옵니다. Reactive에서 소스(게시자)는 데이터를 푸시합니다.

`Reactive Streams`에서 데이터 스트림은 비동기적이고 넌블록킹이며 백프레셔를 지원합니다. (백프레셔에 대한 설명은 이 장의 구독자 섹션을 참조)

`Reactive Streams` 사양에 따라 네 가지 기본 유형이 있습니다.

- Publisher
- Subscriber
- Subscription
- Processor

Let's have a look at each.

#### Publisher

게시자는 한 명 이상의 구독자에게 데이터 스트림을 제공합니다. 구독자는 `subscriber()` 메서드를 사용하여 게시자를 구독합니다. 
각 구독자는 게시자에게 한 번만 구독해야 합니다. **가장 중요한 것은 게시자가 구독자의 요청에 따라 데이터를 푸시한다는 것입니다**. 
reactive 스트림은 lazy합니다. 따라서 게시자는 구독자가 있는 경우에만 항목을 푸시합니다.

A publisher is defined as follows:

```java
// T – type of element Publisher sends
public interface Publisher<T> {
    public void subscribe(Subscriber<? super T> s);
}
```

#### Subscriber

구독자는 게시자가 푸시한 데이터를 사용합니다. 게시자-구독자 통신은 다음과 같이 작동합니다.

1. 구독자 인스턴스가 `subscribe()` 메서드에 전달되면 `onSubscribe()` 메서드를 트리거합니다. 여기에는 백프레셔, 즉 구독자가 게시자에게 요구하는 데이터의 양을 제어하는 ​​구독 매개변수가 포함되어 있습니다.

2. 첫 번째 단계 후 게시자는 `request(long)` 호출을 기다립니다. `request()` 호출이 이루어진 후에만 데이터를 구독자로 푸시합니다. 이 방법은 Publisher의 엘리먼트 수를 요구합니다.

    일반적으로 게시자는 구독자가 데이터를 안전하게 처리할 수 있는지 여부에 관계없이 구독자에게 데이터를 푸시합니다. 그러나 구독자는 안전하게 처리할 수 있는 데이터의 양을 가장 잘 알고 있습니다. 따라서 reactive 스트림에서 구독자는 구독 인스턴스를 사용하여 엘리먼트 수에 대한 수요를 게시자에게 전달합니다. 이것은 **back-pressure** 또는 **흐름 제어**로 알려져 있습니다.

    게시자가 구독자에게 속도를 줄이도록 요청했지만 속도를 늦출 수 없다면 어떻게 될까요? 이 경우 게시자는 실패, 삭제 또는 버퍼링 여부를 결정해야 합니다.

3. 2단계를 사용하여 요청이 이루어지면 게시자는 데이터 알림을 보내고 onNext() 메서드를 사용하여 데이터를 사용합니다. `request()`에 의해 전달된 수요에 따라 게시자가 데이터 알림을 푸시할 때까지 트리거됩니다.

4. 마지막에 `onError()` 또는 `onCompletion()`이 종료 상태로 트리거됩니다. `request()`를 호출하더라도 이러한 호출 중 하나가 트리거된 후에는 알림이 전송되지 않습니다. 다음은 종료 방법입니다.
   
    a. onError()는 오류가 발생하면 호출됩니다.

    b. 모든 엘리먼트가 푸시되면 onCompletion()이 호출됩니다.

```java
// T – type of element Publisher sends

public interface Subscriber<T> {
    public void onSubscribe(Subscription s);
    public void onNext(T t);
    public void onError(Throwable t);
    public void onComplete();
}
```

#### Subscription

Subscription은 게시자와 구독자 사이의 중재자입니다. `subscriber()` 메서드를 호출하고 게시자에게 수요를 알리는 것은 구독자의 책임입니다. 
구독자가 필요할 때 호출할 수 있습니다. `cancel()`은 게시자에게 데이터 알림 전송을 중지하고 리소스를 정리하도록 요청합니다.

```java
public interface Subscription {
    public void request(long n);
    public void cancel();
}
```

#### Processor

Processor는 게시자와 구독자 사이의 브릿지 역할을 하며 처리 단계를 나타냅니다. 게시자와 구독자 모두로 작동하며 둘 다에서 정의한 계약을 따릅니다.

```java
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
}
```

Flux는 `Project Reactor`의 게시자 유형입니다.
다음 코드는 Flux.just() 팩토리 메서드를 사용하여 Flux를 생성합니다.  
이 게시자는 4개의 정수 항목에 대해 reduce 연산자를 사용하여 합계 연산을 수행합니다.

```java
Flux<Integer> fluxInt = Flux.just(1, 10, 100, 1000).log();
fluxInt.reduce(Integer::sum)
    .subscribe(sum -> System.out.printf("Sum is: %d", sum));
```

코드를 실행하면 다음과 같이 로그를 출력한다.

```sh
11:00:38.074 [main] INFO reactor.Flux.Array.1 - | onSubscribe([Synchronous Fuseable] FluxArray.ArraySubscription)
11:00:38.074 [main] INFO reactor.Flux.Array.1 - | request(unbounded)
11:00:38.084 [main] INFO reactor.Flux.Array.1 - | onNext(1)
11:00:38.084 [main] INFO reactor.Flux.Array.1 - | onNext(10)
11:00:38.084 [main] INFO reactor.Flux.Array.1 - | onNext(100)
11:00:38.084 [main] INFO reactor.Flux.Array.1 - | onNext(1000)
11:00:38.084 [main] INFO reactor.Flux.Array.1 - | onComplete()
Sum is: 1111

Process finished with exit code 0
```

게시자가 구독할 때 구독자가 무제한 `Subscription.request()`를 보냅니다. 
첫 번째 항목이 알림을 받으면 onNext()가 호출되는 식입니다. 
마지막으로 게시자가 푸시 엘리먼트를 완료하면 onComplete() 이벤트가 호출됩니다. 
이것이 Reactive 스트림이 작동하는 방식입니다.

Spring이 Spring WebFlux 모듈에서 이러한 Reactive 스트림을 사용하는 방법과 이유를 살펴보겠습니다.


## Spring WebFlux 살펴보기

기존 서블릿 API는 블로킹 API입니다. 이들은 API를 블로킹 I/O 스트림을 사용합니다. 
Servlet 3.0 컨테이너는 기본 이벤트 루프를 발전시키고 사용합니다. 
비동기 요청은 비동기적으로 처리되지만 I/O 작업은 여전히 ​​블로킹 I/O 스트림을 사용합니다.

**Servlet 3.1 컨테이너**는 더 발전하고 비동기성을 지원하며 넌블로킹 I/O 스트림 API를 가지고 있습니다. 
그러나 `request.getParameters()`와 같은 특정 Servlet API는 블로킹 요청 본문을 구문 분석하고 Filter와 같은 동기 계약을 제공합니다. 
Spring MVC 프레임워크는 Servlet API 및 Servlet 컨테이너를 기반으로 합니다.

따라서, **Spring은 완전히 넌블로킹이고 백프레셔 기능을 제공하는 `Spring WebFlux`를 제공합니다**. 
적은 수의 스레드와 동시성을 제공하고 더 적은 수의 하드웨어 리소스로 확장됩니다. 
WebFlux는 비동기 로직의 선언적 구성을 지원하기 위해 유창하고 함수형이며 연속적인 스타일의 API를 제공합니다. 
비동기 기능 코드를 작성하는 것은 명령형 코드를 작성하는 것보다 더 복잡합니다. 
그러나 일단 사용하게 되면 정확하고 읽기 쉬운 코드를 작성할 수 있기 때문에 좋아하게 될 것입니다.

Spring WebFlux와 Spring MVC는 공존할 수 있습니다. 
**그러나 Reactive 프로그래밍 모델을 효과적으로 사용하려면 Reactive 흐름과 블로킹 호출을 혼합해서는 안 됩니다.**

Spring WebFlux는 다음 기능과 프로토타입을 지원합니다.

- 이벤트 루프 동시성 모델
- 주석이 달린 컨트롤러와 함수형 엔드포인트
- Reactive 클라이언트
- Tomcat, Jetty 등 Netty 및 Servlet 3.1 컨테이너 기반 웹 서버

Reactive API와 Reactor Core를 이해함으로써 WebFlux가 어떻게 작동하는지 깊이 파헤쳐 봅시다.

### Reactive APIs

`Spring WebFlux` API는 Reactive API이며 게시자를 입력으로 허용합니다. 
그런 다음 WebFlux는 `Reactor Core` 또는 `RxJava`와 같은 Reactive 라이브러리에서 지원하는 유형에 맞게 조정합니다. 
그런 다음 지원되는 Reactive 라이브러리 유형에 따라 입력을 처리하고 출력을 반환합니다. 
이를 통해 WebFlux API는 다른 Reactive 라이브러리와 상호 운용할 수 있습니다.

기본적으로 `Spring WebFlux`는 핵심 의존성으로 Reactor(https://projectreactor.io)를 사용합니다. 
`Project Reactor`는 `Reactive Streams` 라이브러리를 제공합니다. 
WebFlux는 게시자로 입력을 받아 Reactor 유형에 적용한 다음 Mono 또는 Flux 출력으로 반환합니다.

`Reactive Streams`의 게시자는 수요에 따라 구독자에게 데이터를 푸시합니다. 
Project Reactor는 더 나아가 Mono와 Flux라는 두 가지 Publisher 구현체를 제공합니다. 
Mono는 구독자에게 0 또는 1개를 반환할 수 있지만 Flux는 0 ~ N의 엘리먼트를 반환합니다. 
둘 다 Publisher를 확장한 CorePugblisher 인터페이스를 구현하는 추상 클래스입니다.

레포지토리에 다음과 같은 메소드가 있습니다

```java
public Product findById(UUID id);
public List<Product> getAll();
```
Mono와 Flux로 치환할 수 있습니다:

```java
public Mono<Product> findById(UUID id);
public Flux<Product> getAll();
```
Hot 스트림과 Cold 스트림의 개념이 있습니다. 
Cold 스트림의 경우 여러 구독자가 있으면 소스를 새로 시작하지만 핫 스트림은 여러 구독자에 대해 동일한 소스를 사용합니다. 

`Project Reactor` 스트림은 기본적으로 `cold`이므로 스트림을 사용하면 다시 시작할 때까지 재사용할 수 없습니다. 그러나 `Project Reactor`를 사용하면 cache() 메서드를 사용하여 콜드 스트림을 `hot` 스트림으로 전환할 수 있습니다. 

Let's understand the cold and hot stream concepts with some examples:

```java
Flux<Integer> flux = Flux.just(1, 10, 100).log();

flux.reduce(Integer::sum)
    .subscribe(sum -> System.out.printf("Sum is: %d\n", sum));

flux.reduce(Integer::max)
    .subscribe(max -> System.out.printf("Maximum is: %d", max));
```

숫자 3개의 Flux를 만든 후 sum, max 연산을 별도로 수행합니다. 거기에 2개의 구독자가 있습니다. 기본적으로 Project Reactor는 콜드이므로 두 개의 구독자가 등록될 때 다음과 같은 출력으로 보여주면서 다시 시작합니다.
```sh
11:23:35.060 [main] INFO reactor.Flux.Array.1 - | onSubscribe([Synchronous Fuseable] FluxArray.ArraySubscription)
11:23:35.060 [main] INFO reactor.Flux.Array.1 - | request(unbounded)
11:23:35.060 [main] INFO reactor.Flux.Array.1 - | onNext(1)
11:23:35.060 [main] INFO reactor.Flux.Array.1 - | onNext(10)
11:23:35.060 [main] INFO reactor.Flux.Array.1 - | onNext(100)
11:23:35.060 [main] INFO reactor.Flux.Array.1 - | onComplete()

Sum is: 111

11:23:35.076 [main] INFO reactor.Flux.Array.1 - | onSubscribe([Synchronous Fuseable] FluxArray.ArraySubscription)

11:23:35.076 [main] INFO reactor.Flux.Array.1 - | request(unbounded)
11:23:35.076 [main] INFO reactor.Flux.Array.1 - | onNext(1)
11:23:35.076 [main] INFO reactor.Flux.Array.1 - | onNext(10)
11:23:35.076 [main] INFO reactor.Flux.Array.1 - | onNext(100)
11:23:35.076 [main] INFO reactor.Flux.Array.1 - | onComplete()

Maximum is: 100
```
소스는 같은 프로그램에서 만들었지만 소스가 HTTP 요청과 같이 다른 어딘가라든가 또는 소스를 재시작하지 않으려면 어떻게 할까요? 이러한 경우 `cache()`를 사용하여 콛드 스트림을 핫 스트림으로 바꿀 수 있습니다. 이 둘간의 유일한 차이는 `Flux.just()`에 `cache()` 호출을 추가한 것입니다.

```java
Flux<Integer> fluxInt = Flux.just(1, 10, 100).log().cache();

fluxInt.reduce(Integer::sum)
    .subscribe(sum -> System.out.printf("Sum is: %d\n", sum));

fluxInt.reduce(Integer::max)
    .subscribe(max -> System.out.printf("Maximum is: %d", max));
```

출력을 보면 소스는 재시작하지 않으며 대신 같은 소스를 다시 사용합니다.
```
11:29:25.665 [main] INFO reactor.Flux.Array.1 - | onSubscribe([Synchronous Fuseable] FluxArray.ArraySubscription)
11:29:25.665 [main] INFO reactor.Flux.Array.1 - | request(unbounded)
11:29:25.665 [main] INFO reactor.Flux.Array.1 - | onNext(1)
11:29:25.665 [main] INFO reactor.Flux.Array.1 - | onNext(10)
11:29:25.665 [main] INFO reactor.Flux.Array.1 - | onNext(100)
11:29:25.665 [main] INFO reactor.Flux.Array.1 - | onComplete()
Sum is: 111

Maximum is: 100
```

Now we have got to the crux of Reactive APIs, let's see what Spring WebFlux's Reactive Core consists of.


## Reactive Core

Reactive Core는 스프링을 Reactive 앱으로 개발하는 토대를 제공합니다. 
웹 앱이 HTTP 웹 요청을 서비스하려면 3가지 수준의 지원이 필요합니다.

- 서버에 의해 웹 요청을 처리:
  
  a. `HttpHandler`: Netty 또는 Tomcat 같은 다른 HTTP 서버 상에서 요청/응답을 추상화하는 인터페이스

  b. `WebHandler`: 사용자 세션, 요청과 세션 속성, 로케일과 요청 principal, 폼 데이터 등의 지원을 제공

```java
  public interface HttpHandler {
      Mono<Void> handle(ServerHttpRequest request,ServerHttpResponse response);
  }
```
 
- WebClient를 사용하여 클라이언트에 의해 웹 요청 호출의 처리
- 콘텐츠 직렬화를 위한 코덱(인코더, 디코더, HttpMessageWriter, HttpMessageReader, DataBuffer)

이 컴포넌트들이 스프링 WebFlux의 코어이며 앱 설정에 다음 빈들을 포함한다 

- WebHandler(DispatcherHandler)
- WebFilter
- WebExceptionHandler
- HandlerMapping
- HandlerAdapter
- HandlerResultHandler.

REST 서비스 구현을 위해 Tomcat, Jetty, Netty 및 Undertow 웹 서버에 대한 특정 HandlerAdapter 인스턴스가 있습니다. 
`Reactive Streams`를 지원하는 Netty와 같은 웹 서버는 가입자의 요구를 처리합니다.

그러나 서버 핸들러가 `Reactive Streams`를 지원하지 않으면 org.springframework.http.server.reactive.ServletHttpHandlerAdapter HTTP HandlerAdapter가 사용됩니다. 

`Reactive Streams`와 Servlet 3.1 컨테이너 비동기 I/O 간의 적응을 처리하고 Subscriber 클래스를 구현합니다. 이것은 OS TCP 버퍼를 사용합니다. OS TCP는 자체 백프레셔(제어 흐름)을 사용합니다. 즉, 버퍼가 가득 차면 OS는 TCP 백 프레셔를 사용하여 들어오는 요소를 중지합니다.

모든 HTTP 클라이언트는 HTTP 프로토콜을 사용하여 REST API를 사용합니다. 웹 서버에서 요청을 받으면 이를 Spring WebFlux 애플리케이션으로 전달합니다. 그런 다음 WebFlux는 컨트롤러로 가는 Reactive 파이프라인을 빌드합니다. 

HttpHandler는 WebFlux와 HTTP 프로토콜을 사용하여 통신하는 웹 서버 간의 인터페이스입니다. 

기본 서버가 `Reactive Streams`를 지원하는 경우 구독은 기본적으로 서버에서 수행됩니다. 그렇지 않으면 WebFlux는 Servlet 3.1 컨테이너 기반 서버에 ServletHttpHandlerAdapter를 사용합니다. 그런 다음 ServletHttpHandlerAdapter는 스트림을 비동기 I/O 서블릿 API에 맞게 조정하고 그 반대의 경우도 마찬가지입니다. 그런 다음 `Reactive Streams`의 구독은 ServletHttpHandlerAdapter로 발생합니다.

요약하면, Mono/Flux 스트림은 WebFlux 내부 클래스에 의해 구독되고 컨트롤러가 Mono/Flux 스트림을 보낼 때 이러한 클래스는 이를 HTTP 패킷으로 변환합니다. HTTP 프로토콜은 이벤트 스트림을 지원합니다. 

그러나 JSON과 같은 다른 미디어 유형의 경우 Spring WebFlux는 Mono/Flux 스트림을 구독하고 onComplete() 또는 onError()가 트리거될 때까지 기다립니다. 그런 다음 하나의 HTTP 응답에서 전체 요소 목록 또는 Mono의 경우 단일 요소를 직렬화합니다.

Spring WebFlux는 전면 컨트롤러인 Spring MVC의 DispatcherServlet과 유사한 컴포넌트가 필요합니다. 

다음 섹션에서 이에 대해 논의해 보겠습니다.

## DispatcherHandler 이해하기

Spring WebFlux의 전면 컨트롤러인 DispatcherHandler는 Spring MVC 프레임워크의 DispatcherServlet입니다. 

DispatcherHandler에는 

- HandlerMapping(요청을 핸들러에 매핑), 
- HandlerAdapter(요청에 매핑된 핸들러를 호출하는 DispatcherHandler 도우미)
- HandlerResultHandler(결과를 처리하고 결과를 형성하기 위한 단어 리턴)와 같은 특수 구성 요소를 사용하는 알고리즘이 포함되어 있습니다)
 
DispatcherHandler 구성 요소는 `webHandler`라는 빈으로 식별됩니다.

다음과 같은 방식으로 요청을 처리합니다.

1. DispatcherHandler에서 웹 요청을 받습니다.

2. DispatcherHandler는 HandlerMapping을 사용하여 요청에 대해 일치하는 핸들러를 찾고 첫 번째 일치를 사용합니다.

3. 그런 다음 각 HandlerAdapter를 사용하여 요청을 처리하고 HandlerResult(처리 후 반환 값)를 노출합니다. 반환 값은 ResponseEntity, ServerResponse, @RestController에서 반환된 값 또는 뷰 해석기가 반환한 값(CharSequence, view, map 등) 중 하나일 수 있습니다.

4. 각 HandlerResultHandler를 사용하여 2단계에서 받은 HandlerResult 유형을 기반으로 응답을 작성하거나 뷰를 렌더링합니다. ResponseEntityResultHandler는 ResponseEntity에, ServerResponseResultHandler는 ServerResponse에, ResponseBodyResultHandler는 @RestController가 반환한 값에 사용 또는 @ResponseBody 어노테이션이 있는 메서드이고 ViewResolutionResultHandler는 뷰 리졸버에서 반환된 값에 사용됩니다.

5. 요청이 완료되었습니다.

Spring MVC와 같은 주석이 달린 컨트롤러 또는 함수형 끝점을 사용하여 Spring WebFlux에서 REST 끝점을 만들 수 있습니다.

### 컨트롤러

Spring 팀은 Spring MVC 및 Spring WebFlux에게 이 주석이 넌블로킹이도록 동일한 주석을 유지했습니다. 따라서 이전 장에서 REST 컨트롤러를 생성하기 위해 사용한 것과 동일한 주석을 사용할 수 있습니다. 거기에서 주석은 Reactive Core에서 실행되고 넌블로킹 플로우를 제공합니다. 

그러나 개발자는 완전히 넌블로킹 흐름을 유지하고 Reactive 체인(파이프라인)을 유지 관리할 책임이 있습니다. Reactive 체인의 모든 블로킹 호출은 Reactive 체인을 블로킹 호출로 바꿉니다.

넌블로킹 및 리액티브 호출을 지원하는 간단한 REST 컨트롤러를 만들어 보겠습니다.

```java
@RestController
public class OrderController {
  @PostMapping(value = "/api/v1/orders")
  public ResponseEntity<Order> addOrder(@RequestBody NewOrder newOrder){
     // …
  }

  @GetMapping(value = "/api/v1/orders/{id}")
  public ResponseEntity<Order> getOrderById(@PathVariable("id") String id){
    // …
  }
}

```
Spring MVC에서 사용한 모든 주석을 사용하는 것을 볼 수 있습니다.

- @RestController는 클래스를 REST 컨트롤러로 표시하는 데 사용됩니다. 이것이 없으면 엔드포인트가 등록되지 않고 요청이 NOT FOUND 404로 반환됩니다.

- @GetMapping은 경로와 HTTP 메소드를 정의하는데 사용된다.

- @RequestBody 주석은 매개변수를 요청 본문으로 표시하고 적절한 코덱이 변환에 사용됩니다. 마찬가지로 경로 매개변수 및 쿼리 매개변수에 대해 각각 - @PathVariable 및 @RequestParam이 있습니다.

REST 끝점을 작성하기 위해 주석 기반 모델을 사용할 것입니다. WebFlux를 사용하여 전자 상거래 앱 컨트롤러를 구현할 때 자세히 살펴보겠습니다. Spring WebFlux는 또한 다음 섹션에서 탐색할 함수형 프로그래밍 스타일을 사용하여 REST 끝점을 작성하는 방법을 제공합니다.


## 함수형 엔드포인트

Spring MVC를 사용하여 코딩한 REST 컨트롤러는 명령형 프로그래밍으로 작성하지만 Reactive는 함수형 프로그래밍입니다. 따라서 Spring WebFlux는 엔드포인트를 사용하여 REST 엔드포인트를 정의하는 대체 방법도 허용합니다. 이들은 또한 동일한 Reactive Core 기반을 사용합니다.

함수형 엔드포인트를 사용하여 동일한 순서 REST 엔드포인트를 작성하는 방법을 살펴보겠습니다.

```java
OrderRepository repository = ...
OrderHandler handler = new OrderHandler(repository);

RouterFunction<ServerResponse> route = route()
    .GET("/v1/api/orders/{id}", accept(APPLICATION_JSON), handler::getOrderById)
    .POST("/v1/api/orders", handler::addOrder)
    .build();
```

```java
public class OrderHandler {
    public Mono<ServerResponse> addOrder(ServerRequest req){
        // ...
    }
    public Mono<ServerResponse> getOrderById(ServerRequest req) {
        // ...
    }
}
```
You can see that the RouterFunctions.route() builder allows you to write all the REST routes in a single statement using the functional programming style. 

다음에는 핸들러 클래스의 메소드 참조를 사용하여 요청을 처리한다. 이것은 주석 기반 모델의 @RequestMapping 본문과 똑같다.

Let's add the following code in the OrderHandler methods:

```java
public class OrderHandler {
    public Mono<ServerResponse> addOrder(ServerRequest req) {
       Mono<NewOrder> order = req.bodyToMono(NewOrder.class);
      return ok().build(repository.save(toEntity(order)));
    }

    public Mono<ServerResponse> getOrderById(ServerRequest req) {
      String orderId = req.pathVariable("id");
      return repository.getOrderById(UUID.fromString(orderId))
        .flatMap(order -> ok()
          .contentType(APPLICATION_JSON).bodyValue(toModel(order)))
        .switchIfEmpty(ServerResponse.notFound().build());
    }
}
```

REST 컨트롤러의 @RequestMapping() 매핑 메소드와 달리 핸들러 메소드는 body, path, query와 같은 복수의 파라미터를 갖지 않습니다. 단지 이와 똑같이 사용할 수 있는 `ServerRequest` 파라미터가 있습니다. addOrder 메소드에서 Order 객체는 request.bodyToMono()로 추출되며 요청 본문을 해석한 다음 Order 객체로 변환합니다. 마찬가지로 getOrderById() 핸들러 메소드에서 request.pathVariable()을 사용하여 요청으로부터 ID를 추출합니다. 

이제 응답에 대해 논의해 보겠습니다. 핸들러 메소드는 Spring MVC의 ResponseEntity와 비교하여 ServerResponse 객체를 사용합니다. 따라서 ok() 메서드는 ResponseEntity에서 가져온 것처럼 보이지만 org.springframework.web.reactive.function.server.ServerResponse.ok에서 가져온 것입니다. Spring 팀은 API를 Spring MVC와 최대한 유사하게 유지하려고 노력했습니다. 그러나 기본 구현은 다르며 넌블로킹 Reactive 인터페이스를 제공합니다.

이러한 핸들러 메소드에 대한 마지막 요점은 응답이 작성되는 방식입니다. 명령형 스타일이 아닌 함수형 스타일을 사용하며 Reactive 체인이 끊어지지 않도록 합니다. 리포지토리는 두 경우 모두 Mono 개체(게시자)를 반환하고 ServerResponse 내부에 래핑된 응답으로 반환합니다.

getOrderById() 메소드에서 흥미로운 코드를 찾을 수 있습니다. 레포지토리에서 받은 Mono 객체에 대해 flatMap 작업을 수행합니다. 엔터티에서 모델로 변환한 다음 ServerResponse 개체로 래핑하고 응답을 반환합니다. 저장소가 null을 반환하면 어떻게 되는지 궁금할 것입니다. 레포지토리는 계약에 따라 Mono를 반환하며, 이는 본질적으로 Java Optional 클래스와 유사합니다. 따라서 Mono 객체는 비어 있을 수 있지만 계약에 따라 null은 아닙니다. 레포지토리가 빈 Mono를 반환하면 switchIfEmpty() 연산자가 사용되며 NOT FOUND 404 응답이 전송됩니다.

오류의 경우 doOnError() 또는 onErrorReturn()과 같이 사용할 수 있는 다양한 오류 연산자가 있습니다.

Mono 유형을 사용하는 로직 흐름에 대해 논의했습니다. Mono 대신 Flux 유형을 사용하는 경우에도 동일합니다.

Spring 컨텍스트에서 Reactive, 비동기 및 넌블로킹 프로그래밍과 관련된 많은 이론을 논의했습니다. 코딩으로 넘어가 4장, API를 위한 비즈니스 로직 작성에서 개발한 전자상거래 API를 리액티브 API로 마이그레이션합니다.


## 전자 상거래 앱용 Reactive API 구현

이제 `Reactive Streams`가 작동하는 방식에 대한 아이디어를 얻었으므로 비동기식 및 넌블로킹 REST API를 구현할 수 있습니다.

설계 우선 접근 방식을 따르고 있으므로 API 사양이 먼저 필요하다는 것을 기억할 것입니다. 그러나 이전에 3장, API 사양 및 구현에서 만든 전자 상거래 API 사양을 재사용할 수 있습니다.

OpenAPI Codegen은 Spring MVC 호환 API Java 인터페이스를 생성하는 API 인터페이스/계약을 생성하는 데 사용됩니다. Reactive API 인터페이스를 생성하기 위해 어떤 변경이 필요한지 살펴보겠습니다.

### Reactive API용 OpenAPI Codegen 변경

다음과 같이 Spring WebFlux 호환 Java 인터페이스를 생성하려면 몇 가지 OpenAPI Codegen 구성을 조정해야 합니다.

```json
{
  "library": "spring-boot",
  "dateLibrary": "java8",
  "hideGenerationTimestamp": true,
  "modelPackage": "com.packt.modern.api.model",
  "apiPackage": "com.packt.modern.api",
  "invokerPackage": "com.packt.modern.api",
  "serializableModel": true,
  "useTags": true,
  "useGzipFeature" : true,
  "reactive": true,
  "interfaceOnly": true,
   …
   …
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/resources/api/config.json

리액티브 API 지원은 스프링 부트를 라이브러리로 선택한 경우에만 제공됩니다. 또한 `reactive` 플래그를 true로 설정해야 합니다. 기본적으로 `reactive` 플래그는 false입니다.

Now, you can run the following command:

```sh
$ gradlew clean generateSwaggerCode
```

그러면 주석 기반 REST 컨트롤러 인터페이스인 `Reactive Streams` 호환 Java 인터페이스가 생성됩니다. API 인터페이스를 열면 OrderAPI 인터페이스에 대한 다음 코드 블록과 같이 Mono/Flux 리액터 유형을 찾을 수 있습니다.

```java
@ApiOperation(value = "Creates a new order for the given order request", nickname = "addOrder", notes = "Creates a new order for the given order request.", response = Order.class, tags = {"order",})

@ApiResponses(value = {
    @ApiResponse(code = 201, message = "Order added                   successfully", response = Order.class),
    @ApiResponse(code = 406, message = "If payment is not                  authorized.")})

@RequestMapping(value = "/api/v1/orders",
    produces = {"application/xml", "application/json"},
    consumes = {"application/xml", "application/json"},
    method = RequestMethod.POST)

Mono<ResponseEntity<Order>> addOrder(
    @ApiParam(value = "New Order Request object") @Valid @RequestBody(required = false) Mono<NewOrder> newOrder, ServerWebExchange exchange);
```
또 다른 변경 사항을 관찰했을 것입니다. 추가 매개 변수인 ServerWebExchange도 Reactive 컨트롤러에 필요합니다.

이제 코드를 컴파일할 때 Reactive 지원에 필요한 종속성을 아직 추가하지 않았기 때문에 컴파일 오류를 찾을 수 있습니다. 다음 섹션에서 추가하는 방법을 알아보겠습니다.

### build.xml에 반응성 종속성 추가

먼저 지금은 Spring MVC가 필요하지 않으므로 spring-boot-starter-web을 제거합니다. 둘째, Spring WebFlux 및 Reactor 지원 테스트에 각각 spring-boot-starter-webflux 및 reactor-test를 추가합니다. 이러한 의존성이 성공적으로 추가되면 OpenAPI 생성 코드에 컴파일 오류가 표시되지 않습니다.

다음과 같이 build.gradle에 필요한 반응성 종속성을 추가할 수 있습니다.

```
implementation 'org.springframework.boot:spring-boot-starter-webflux'
// implementation 'org.springframework.boot:spring-boot-starter-web'
testImplementation('org.springframework.boot:spring-boot-starter-test')
testImplementation 'io.projectreactor:reactor-test'
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/build.gradle

REST 컨트롤러에서 데이터베이스까지 완전한 reactive 파이프라인이 필요합니다. 그러나 기존 JDBC 및 Hibernate 종속성은 블로킹 호출만 지원합니다. JDBC는 완전 블로킹 API입니다. 하이버네이트 역시 블로킹입니다. 따라서 데이터베이스에 대한 리액티브 의존성이 필요합니다.

Hibernate Reactive(https://github.com/hibernate/hibernate-reactive)는 이 장을 작성하는 시점에서 베타 버전입니다. Hibernate Reactive 베타는 PostgresSQL, MySQL 및 Db2만 지원합니다. Hibernate Reactive는 작성 당시 H2를 지원하지 않습니다. 따라서 'Reactive Streams' 작업을 위한 spring-data-r2dbc 라이브러리를 제공하는 Spring 프레임워크인 Spring Data를 사용하기만 하면 됩니다.

MongoDB와 같은 많은 NoSQL 데이터베이스는 이미 Reactive 데이터베이스 드라이버를 제공합니다. R2DBC 기반 드라이버는 완전한 넌블로킹/반응 API 호출을 위해 JDBC 대신 관계형 데이터베이스에 사용해야 합니다. R2DBC는 Reactive Relational Database Connectivity의 약자입니다. R2DBC는 데이터베이스 드라이버용 SPI(서비스 공급자 인터페이스)를 설정하는 Reactive API 개방형 사양입니다. 거의 모든 인기 있는 관계 데이터베이스는 H2, MySQL, MariaDB, SQL Server, PostgresSQL 및 Proxy와 같은 R2DBC 드라이버를 지원합니다. 작성 당시 Oracle DB는 Reactor, RxJava 및 Akka Streams와 통합되는 흐름 기반의 Reactive JDBC 확장(DB 20c)을 제공합니다. 그러나 Oracle R2DBC 드라이버가 곧 출시될 예정입니다(2020년 현재 아직 출시되지 않음).

Let's add the R2DBC dependencies for Spring Data and H2 in the build.gradle file:

```gradle
// DB Starts

implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
implementation 'com.h2database:h2'
runtimeOnly 'io.r2dbc:r2dbc-h2'

// DB Ends
```
이제 컴파일 오류 없이 종단 간(컨트롤러에서 저장소로) 코드를 작성할 수 있습니다. API 인터페이스에 대한 구현을 작성하기 전에 전역 예외 처리를 추가해 보겠습니다.

### 예외 처리

3장, API 사양 및 구현에서 Spring MVC에 추가된 방식으로 전역 예외 처리기를 추가합니다. 그 전에 Reactive 파이프라인에서 예외를 처리하는 방법이 궁금할 것입니다. 반응형 파이프라인은 스트림의 흐름이며 명령형 코드에서 수행하는 방식으로 예외 처리를 추가할 수 없습니다. 파이프라인 흐름에서만 오류를 발생시켜야 합니다.

Check out the following code:

```java
.flatMap(card -> {
  if (Objects.isNull(card.getId())) {
    return service.registerCard(mono)
        .map(ce -> status(HttpStatus.CREATED)
        .body(assembler.entityToModel(ce, exchange)));
  } else {
    return Mono.error(
        () -> new CardAlreadyExistsException(" for user with ID - " + d.getId()));
  }
})
```

여기에서 flatMap 작업이 수행됩니다. 카드가 유효하지 않은 경우, 즉 카드에 요청된 ID가 없는 경우 오류가 발생해야 합니다. 여기서 Mono.error()는 파이프라인이 반환되는 객체로 Mono를 기대하기 때문에 사용됩니다. 마찬가지로 Flux가 반환된 유형으로 예상되는 경우 Flux.error()를 사용할 수 있습니다.

서비스 또는 리포지토리 호출에서 개체를 기대하고 있지만 대신 빈 개체를 수신한다고 가정해 보겠습니다. 그런 다음 다음 코드와 같이 switchIfEmpty() 연산자를 사용할 수 있습니다.여기에서 flatMap 작업이 수행됩니다. 카드가 유효하지 않은 경우, 즉 카드에 요청된 ID가 없는 경우 오류가 발생해야 합니다. 여기서 Mono.error()는 파이프라인이 반환되는 객체로 Mono를 기대하기 때문에 사용됩니다. 마찬가지로 Flux가 반환된 유형으로 예상되는 경우 Flux.error()를 사용할 수 있습니다.

서비스 또는 리포지토리 호출에서 개체를 기대하고 있지만 대신 빈 개체를 수신한다고 가정해 보겠습니다. 그런 다음 다음 코드와 같이 switchIfEmpty() 연산자를 사용할 수 있습니다.

```java
Mono<List<String>> monoIds = itemRepo.findByCustomerId(customerId)
    .switchIfEmpty(Mono.error(new ResourceNotFoundException(
        ". No items found in Cart of customer with Id - " + customerId)))
    .map(i -> i.getId().toString())
    .collectList().cache();
```
Here, the code expects a Mono object of List from the item repository. However, if the returned object is empty, then it simply throws ResourceNotFoundException. switchIfEmpty() accepts the alternate Mono instance.

By now, you might have a question about the type of exception. It throws a runtime exception. See the ResourceNotFoundException class declaration here:
```java
public class ResourceNotFoundException extends RuntimeException
```
Similarly, you can also use onErrorReturn(), onErrorResume(), or similar error operators from `Reactive Streams`. Look at the use of onErrorReturn() in the next code block:

```java
return service.getCartByCustomerId(customerId)
    .map(cart ->
        assembler.itemfromEntities(cart.getItems().stream()
            .filter(i -> i.getProductId().toString().equals(itemId.trim())).collect(toList()))
            .get(0)).map(ResponseEntity::ok)
    .onErrorReturn(notFound().build())
```

모든 예외를 처리하고 오류 응답을 사용자에게 보내야 합니다. 이것이 우리가 먼저 전역 예외 핸들러를 살펴보는 이유입니다.

### 컨트롤러에 대한 전역 예외 처리

Spring MVC에서 @ControllerAdvice를 사용하여 전역 예외 핸들러를 생성했습니다. Spring WebFlux에서 오류를 처리하기 위해 약간 다른 경로를 취할 것입니다. 먼저 Spring MVC에서도 사용할 수 있는 ApiErrorAttributes 클래스를 생성합니다. 이 클래스는 ErrorAttributes 인터페이스의 기본 구현인 DefaultErrorAttributes를 확장합니다. ErrorAttributes 인터페이스는 맵, 오류 필드 맵 및 해당 값을 처리하는 방법을 제공합니다. 그런 다음 이러한 오류 속성을 사용하여 사용자에게 오류를 표시하거나 로깅할 수 있습니다.

DefaultErrorAttributes 클래스는 다음 속성을 제공합니다.

- timestamp: The time that the error was captured
- status: The status code
- error: Error description
- exception: The class name of the root exception (if configured)
- message: The exception message (if configured)
- errors: Any ObjectErrors from a BindingResult exception (if configured)
- trace: The exception stack trace (if configured)
- path: The URL path when the exception was raised
- requestId: The unique ID associated with the current request

We have added two default values to the status and message – an internal server error and a generic error message (The system is unable to complete the request. Contact system support.), respectively – in ApiErrorAttributes, as shown next:

```java
@Component
public class ApiErrorAttributes extends DefaultErrorAttributes {

  private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
  private String message = ErrorCode.GENERIC_ERROR.getErrMsgKey();

  @Override
  public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {

    var attributes = super.getErrorAttributes(request, options);
    attributes.put("status", status);
    attributes.put("message", message);
    attributes.put("code", ErrorCode.GENERIC_ERROR.getErrCode());

    return attributes;
  }

// Getter/Setters
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/exception/ApiErrorAttributes.java

이제 사용자 지정 전역 예외 처리기 클래스에서 이 ApiErrorAttributes 클래스를 사용할 수 있습니다. AbstractErrorWebExceptionHandler 추상 클래스를 확장하는 ApiErrorWebExceptionHandler 클래스를 생성합니다.

AbstractErrorWebExceptionHandler 클래스는 ErrorWebExceptionHandler 및 InitializingBean 인터페이스를 구현합니다. ErrorWebExceptionHandler는 WebExceptionHandler 인터페이스를 확장하는 기능 인터페이스로, WebExceptionHandler가 예외를 렌더링하는 데 사용됨을 나타냅니다. WebExceptionHandler는 서버 교환 처리가 발생할 때 예외를 처리하기 위한 계약입니다.

InitializingBean 인터페이스는 Spring 핵심 프레임워크의 일부입니다. 모든 속성이 채워질 때 반응하는 구성 요소에서 사용됩니다. 또한 모든 필수 속성이 설정되었는지 확인하는 데 사용할 수도 있습니다.

기본 사항을 공부했으므로 이제 ApiErrorAttributes 클래스를 작성해 보겠습니다.

```java
@Component
@Order(-2)
public class ApiErrorWebExceptionHandler extends       AbstractErrorWebExceptionHandler {

  public ApiErrorWebExceptionHandler(ApiErrorAttributes errorAttributes,
      ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {

    super(errorAttributes, new WebProperties().getResources(), applicationContext);

    super.setMessageWriters(serverCodecConfigurer.getWriters());
    super.setMessageReaders(serverCodecConfigurer.getReaders());
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {

    return RouterFunctions.route(
        RequestPredicates.all(), this::renderErrorResponse);
  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/exception/ApiErrorWebExceptionHandler.java

이 코드에 대한 첫 번째 중요한 관찰은 실행 선호도를 알려주는 @Order 주석을 추가했다는 것입니다. ResponseStatusExceptionHandler는 Spring Framework에 의해 0으로 정렬되고 DefaultErrorWebExceptionHandler는 -1로 정렬됩니다. 둘 다 우리가 만든 것과 같은 예외 처리기입니다. 이 두 가지보다 ApiErrorWebExceptionHandler에 우선 순위를 지정하지 않으면 실행되지 않습니다. 따라서 순서는 -2로 설정됩니다.

다음으로, 이 클래스는 개인적으로 정의된 renderErrorResponse() 메서드를 호출하는 getRoutingFunction() 메서드를 재정의합니다. 여기서 다음과 같이 오류 처리를 위한 사용자 정의 구현이 있습니다.

```java
private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

  Map<String, Object> errorPropertiesMap = getErrorAttributes(request,      ErrorAttributeOptions.defaults());

  Throwable throwable = (Throwable) request
      .attribute("org.springframework.boot.web.reactive.error.                 DefaultErrorAttributes.ERROR")
      .orElseThrow(
          () -> new IllegalStateException("Missing exception attribute in ServerWebExchange"));

  ErrorCode errorCode = ErrorCode.GENERIC_ERROR;

  if (throwable instanceof IllegalArgumentException
      || throwable instanceof DataIntegrityViolationException
      || throwable instanceof ServerWebInputException) {

    errorCode = ILLEGAL_ARGUMENT_EXCEPTION;
  } else if (throwable instanceof CustomerNotFoundException) {
    errorCode = CUSTOMER_NOT_FOUND;
  } else if (throwable instanceof ResourceNotFoundException) {
    errorCode = RESOURCE_NOT_FOUND;
  } // other else-if
    …
    …
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/exception/ApiErrorWebExceptionHandler.java

Here, first we extract the error attributes in errorPropertiesMap. This will be used when we form the error response. Next, we capture the occurred exception using throwable. Then, we check the type of exception and assign appropriate code to it. We keep the default as GenericError, which is nothing more than InternalServerError.

Next, we use a switch statement to form an error response based on the raised exception, as shown here:
```java
switch (errorCode) {
    case ILLEGAL_ARGUMENT_EXCEPTION -> {

      errorPropertiesMap.put("status", HttpStatus.BAD_REQUEST);
      errorPropertiesMap.put("code", ILLEGAL_ARGUMENT_EXCEPTION.getErrCode());
      errorPropertiesMap.put("error", ILLEGAL_ARGUMENT_EXCEPTION);
      errorPropertiesMap.put("message", String
          .format("%s %s", ILLEGAL_ARGUMENT_EXCEPTION.                  getErrMsgKey(), throwable.getMessage()));

      return ServerResponse.status(HttpStatus.BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(errorPropertiesMap));
    }

    case CUSTOMER_NOT_FOUND -> {
      errorPropertiesMap.put("status", HttpStatus.NOT_FOUND);
      errorPropertiesMap.put("code", CUSTOMER_NOT_FOUND.getErrCode());
      errorPropertiesMap.put("error", CUSTOMER_NOT_FOUND);
      errorPropertiesMap.put("message", String .format("%s %s", CUSTOMER_NOT_FOUND.getErrMsgKey(),throwable.getMessage()));

      return ServerResponse.status(HttpStatus.NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(errorPropertiesMap));
    }

    case RESOURCE_NOT_FOUND -> {
                  // Rest of the code
                  …
                  …
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/exception/ApiErrorWebExceptionHandler.java

아마도 다음 버전의 Java에서는 if-else와 switch 블록을 결합하여 코드를 더 간결하게 만들 수 있을 것입니다. errorPropertiesMap을 인수로 사용하고 이를 기반으로 형성된 서버 응답을 반환하는 별도의 메서드를 만들 수도 있습니다. 그런 다음 스위치를 사용할 수 있습니다.

CustomerNotFoundException과 같은 사용자 정의 애플리케이션 예외 클래스와 ErrorCode 및 Error와 같은 기타 예외 처리 지원 클래스는 기존 코드에서 사용됩니다(4장, API용 비즈니스 로직 작성).

예외 처리에 대해 공부했으므로 이제 HATEOAS에 집중할 수 있습니다.

### API 응답에 하이퍼미디어 링크 추가

Reactive API에 대한 HATEOAS 지원이 있으며 이전 장에서 Spring MVC를 사용하여 수행한 것과 약간 유사합니다. HATEOAS 지원을 위해 이러한 어셈블러를 다시 만듭니다. 우리는 또한 모델에서 엔터티로 또는 그 반대로 변환하기 위해 HATEOAS 어셈블러 클래스를 사용합니다.

Spring WebFlux는 하이퍼미디어 링크를 형성하기 위한 ReactiveRepresentationModelAssembler 인터페이스를 제공합니다. 응답 모델에 대한 링크를 추가하기 위해 toModel() 메서드를 재정의합니다.

여기에서는 링크를 채우기 위한 몇 가지 기초 작업을 수행합니다. 다음과 같이 단일 기본 방법으로 HateoasSupport 인터페이스를 생성합니다.

```java
public interface HateoasSupport {

    default UriComponentsBuilder getUriComponentBuilder(@Nullable ServerWebExchange exchange) {
      if (exchange == null) {
        return UriComponentsBuilder.fromPath("/");
      }
      ServerHttpRequest request = exchange.getRequest();
      PathContainer contextPath = request.getPath().contextPath();

      return UriComponentsBuilder.fromHttpRequest(request)
          .replacePath(contextPath.toString())
          .replaceQuery("");
    }
}
```
이 클래스에는 ServerWebExchange를 인수로 받아들이고 UriComponentsBuilder 인스턴스를 반환하는 단일 기본 메서드인 getUriCompononentBuilder()가 포함되어 있습니다. 그런 다음 이 인스턴스를 사용하여 프로토콜, 호스트 및 포트가 있는 링크를 추가하는 데 사용할 서버 URI를 추출할 수 있습니다. 기억한다면 ServerWebExchange 인수가 컨트롤러 메서드에 추가되었습니다. 이 인터페이스는 HTTP 요청, 응답 및 기타 속성을 가져오는 데 사용됩니다.

이제 표현 모델 어셈블러를 정의하기 위해 이 두 가지 인터페이스인 HateoasSupport 및 ReactiveRepresentationModelAssembler를 사용할 수 있습니다.

```java
@Component
public class AddressRepresentationModelAssembler implements     ReactiveRepresentationModelAssembler<AddressEntity, Address>, HateoasSupport {

    private static String serverUri = null;
    private String getServerUri(@Nullable ServerWebExchange exchange) {
        if (Strings.isBlank(serverUri)) {
          serverUri = getUriComponentBuilder(exchange).toUriString();
        }
        return serverUri;
  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/hateoas/AddressRepresentationModelAssembler.java

Here, we have defined another private method, getServerUri(), which extracts the server URI from UriComponentBuilder, which itself is returned from the default getUriComponentBuilder() method of the HateoasSupport interface.

Now, we can override the toModel() method as shown in the following code block:

```java
@Override
public Mono<Address> toModel(AddressEntity entity, ServerWebExchange exchange) {
  return Mono.just(entityToModel(entity, exchange));
}

public Address entityToModel(AddressEntity entity, ServerWebExchange exchange) {

  Address resource = new Address();
  if(Objects.isNull(entity)) {
    return resource;
  }

  BeanUtils.copyProperties(entity, resource);
  resource.setId(entity.getId().toString());
  String serverUri = getServerUri(exchange);
  resource.add(Link.of(String.format("%s/api/v1/addresses",serverUri)).withRel("addresses"));

  resource.add(
      Link.of(String.format("%s/api/v1/addresses/%s",serverUri, entity.getId())).withSelfRel());

  return resource;
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/hateoas/AddressRepresentationModelAssembler.java

toModel() 메서드는 entityToModel() 메서드를 사용하여 AddressEntity 인스턴스에서 형성된 하이퍼미디어 링크가 있는 Mono<Address> 개체를 반환합니다.

entityToModel()은 엔티티 인스턴스에서 모델 인스턴스로 속성을 복사합니다. 가장 중요한 것은 resource.add() 메서드를 사용하여 모델에 하이퍼미디어 링크를 추가한다는 것입니다. add() 메서드는 org.springframework.hateoas.Link 인스턴스를 인수로 사용합니다. 그런 다음 Link 클래스의 of() 정적 팩토리 메서드를 사용하여 링크를 형성합니다. 여기에서 서버 URI를 사용하여 링크에 추가하는 것을 볼 수 있습니다. 원하는 만큼 링크를 만들고 add() 메서드를 사용하여 리소스에 추가할 수 있습니다.

ReactiveRepresentationModelAssembler 인터페이스는 Mono<CollectionModel<D>> 컬렉션 모델을 반환하는 기본 구현과 함께 toCollectionModel() 메서드를 제공합니다. 그러나 여기에 표시된 대로 주소의 Flux 인스턴스를 반환하는 toListModel() 메서드를 추가할 수도 있습니다.

```java
public Flux<Address> toListModel(Flux<AddressEntity> entities,ServerWebExchange exchange) {
  if (Objects.isNull(entities)) {
    return Flux.empty();
  }
  return Flux.from(entities.map(e -> entityToModel(e, exchange)));
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/hateoas/AddressRepresentationModelAssembler.java

This method internally uses the entityToModel() method. Similarly, you can create a representation model assembler for other API models. You can find all these models at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/hateoas.

Now that we are done with the basic code infrastructure, we can develop the API implementation based on the interfaces generated by OpenAPI Codegen. Here, we'll first develop the repositories that will be consumed by the services. At the end, we'll write the controller implementation. Let's start with the repositories.

### Defining an entity

Entities are defined in more or less the same way as we defined and used them in Chapter 4, Writing Business Logic for APIs. However, instead of using Hibernate mappings and JPA, we'll use Spring Data annotations as shown here:

```java
@Table("ecomm.orders")
public class OrderEntity {
  @Id
  @Column("id")
  private UUID id;

  @Column("customer_id")
  private UUID customerId;

  @Column("address_id")
  private UUID addressId;

  @Column("card_id")
  private UUID cardId;

  @Column("order_date")
  private Timestamp orderDate;

  // other fields mapped to table columns

  private UUID cartId;
  private UserEntity userEntity;
  private AddressEntity addressEntity;
  private PaymentEntity paymentEntity;

  private List<ShipmentEntity> shipments = Collections.emptyList();

  // other entities fields and getter/setters
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/entity/OrderEntity.java

@Table을 사용하여 엔터티 클래스를 테이블 이름에 연결하고 @Column을 사용하여 필드를 테이블의 열에 매핑합니다. 명백한 바와 같이 @Id는 식별자 열로 사용됩니다. 마찬가지로 다른 엔티티를 정의할 수 있습니다.

### 레포지토리 추가

레포지토리는 애플리케이션 코드와 데이터베이스 간의 인터페이스입니다. Spring MVC에 있던 것과 동일합니다. 그러나 우리는 Reactive 패러다임을 사용하여 코드를 작성하고 있습니다. 따라서 R2DBC/Reactive 기반 드라이버를 사용하는 리포지토리와 'Reactive Streams' 위에 Reactive 유형의 인스턴스를 반환하는 것이 필요합니다. 이것이 우리가 JDBC를 사용할 수 없는 이유입니다.

Spring Data R2DBC는 ReactiveCrudRepository, ReactiveSortingRepository, RxJava2CrudRepository 및 RxJava3CrudRepository와 같은 Reactor 및 RxJava를 위한 다양한 레포지토리를 제공합니다. 또한 사용자 정의 구현을 작성할 수 있습니다.

ReactiveCrudRepository를 사용하고 사용자 정의 구현도 작성할 것입니다.

We'll write repositories for the Order entity. For other entities, you can find the repositories at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter05/src/main/java/com/packt/modern/api/repository.

First, let's write the CRUD repository for the Order entity as shown next:

```java
@Repository
public interface OrderRepository extends                ReactiveCrudRepository<OrderEntity, UUID>, OrderRepositoryExt {

  @Query("select o.* from ecomm.orders o join ecomm.user u on o.customer_id = u.id where u.id = :custId")
  Flux<OrderEntity> findByCustomerId(String custId);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/repository/OrderRepository.java

This is as simple as shown. The OrderRepository interface extends ReactiveCrudRepository and our own custom repository interface, OrderRepositoryExt.

We'll discuss OrderRepositoryExt a bit later; let's discuss OrderRepository first. We have added one extra method, findByCustomerId(), in the OrderRepository interface, which finds the order by the given customer ID. The ReactiveCrudRepository interface and the Query() annotation are part of the Spring Data R2DBC library. Query() consumes native SQL queries unlike in Spring MVC.

> CAUTION

The Spring Data R2DBC library, at the time of writing, does not support nested entities.

We can also write our own custom repository. Let's write a simple contract for it as shown next:

```java
public interface OrderRepositoryExt {
  Mono<OrderEntity> insert(Mono<NewOrder> m);
  Mono<OrderEntity> updateMapping(OrderEntity orderEntity);
}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/repository/OrderRepositoryExt.java

Here, we have written two method signatures – the first one inserts a new order record in the database and the second one updates the order item and cart item mapping. The idea is that once an order is placed, items should be removed from the cart and added to the order. If you want, you can also combine both operations together.

Let's first define the OrderRepositoryExtImpl class that extends the OrderRepositoryExt interface as shown in the following code block:

```java
@Repository

public class OrderRepositoryExtImpl implements OrderRepositoryExt {
  private ConnectionFactory connectionFactory;
  private DatabaseClient dbClient;
  private ItemRepository itemRepo;
  private CartRepository cartRepo;
  private OrderItemRepository oiRepo;

  public OrderRepositoryExtImpl(ConnectionFactory connectionFactory, ItemRepository itemRepo, OrderItemRepository oiRepo, CartRepository cartRepo, DatabaseClient dbClient) {
    this.itemRepo = itemRepo;
    this.connectionFactory = connectionFactory;
    this.oiRepo = oiRepo;
    this.cartRepo = cartRepo;
    this.dbClient = dbClient;  
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/repository/OrderRepositoryExtImpl.java

We have just defined a few class properties and added these properties in the constructor as an argument for constructor-based dependency injection.

As per the contract, it receives Mono<NewOrder>. Therefore, we need to add a method that converts a model to an entity in the OrderRepositoryExtImpl class. We also need an extra argument as CartEntity contains the cart items. Here it is:

```java
private OrderEntity toEntity(NewOrder order, CartEntity c) {

  OrderEntity orderEntity = new OrderEntity();
  BeanUtils.copyProperties(order, orderEntity);
  orderEntity.setUserEntity(c.getUser());
  orderEntity.setCartId(c.getId());
  orderEntity.setItems(c.getItems())
      .setCustomerId(UUID.fromString(order.getCustomerId()))
      .setAddressId(UUID.fromString(order.getAddress().getId()))
      .setOrderDate(Timestamp.from(Instant.now()))
      .setTotal(c.getItems().stream().collect(Collectors.toMap(k -> k.getProductId(),
          v -> BigDecimal.valueOf(v.getQuantity()).multiply(v.getPrice())))
          .values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO));

  return orderEntity;

}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/repository/OrderRepositoryExtImpl.java

이 방법은 합계가 설정되는 코드를 제외하고 간단합니다. 합계는 스트림을 사용하여 계산됩니다. 
이해를 돕기 위해 다음과 같이 분해해 보겠습니다.

1. 먼저 cart 엔터티에서 항목을 가져옵니다.
2. 그런 다음 items에서 스트림을 만듭니다.
3. 키를 제품 ID로, 값을 수량과 가격의 곱으로 하여 맵을 생성합니다.
4. 맵에서 값을 가져와 스트림으로 변환합니다.
5. BigDecimal에 메소드를 추가하여 reduce 연산을 수행한다. 그런 다음 총 금액을 제공합니다.
6. 값이 없으면 단순히 0을 반환합니다.

toEntity() 메서드 다음에 데이터베이스에서 행을 읽고 이를 OrderEntity로 변환하는 또 다른 매퍼가 필요합니다. 이를 위해 java.util.function 패키지의 일부인 BiFunction을 작성할 것입니다.

```java
class OrderMapper implements BiFunction<Row, Object,OrderEntity> {

  @Override
  public OrderEntity apply(Row row, Object o) {

    OrderEntity oe = new OrderEntity();

    return oe.setId(row.get("id", UUID.class))
        .setCustomerId(row.get("customer_id", UUID.class))
        .setAddressId(row.get("address_id", UUID.class))
        .setCardId(row.get("card_id", UUID.class))
        .setOrderDate(Timestamp.from(
           ZonedDateTime.of((LocalDateTime)
           row.get("order_date"), ZoneId.of("Z")).toInstant()))
        .setTotal(row.get("total", BigDecimal.class))
        .setPaymentId(row.get("payment_id", UUID.class))
        .setShipmentId(row.get("shipment_id", UUID.class))
        .setStatus(StatusEnum.fromValue(row.get("status",
           String.class)));
  }
}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/repository/OrderRepositoryExtImpl.java

We have overridden the apply() method, which returns OrderEntity, by mapping properties from the row to OrderEntity. The second parameter of the apply() method is not used because it contains metadata that we don't need.

Let's first implement the updateMapping() method from the OrderRepositoryExt interface:

```java
@Override

public Mono<OrderEntity> updateMapping(OrderEntity orderEntity) {

  return oiRepo.saveAll(orderEntity.getItems().stream()
      .map(i -> new OrderItemEntity()
        .setOrderId(orderEntity.getId()).setItemId(i.getId()))
        .collect(toList()))
      .then(
        itemRepo.deleteCartItemJoinById(orderEntity.getItems()
           .stream().map(i -> i.getId().toString())
           .collect(toList()),
        orderEntity.getCartId().toString())
           .then(Mono.just(orderEntity))
      );
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/repository/OrderRepositoryExtImpl.java

Here, we have created a pipeline of `Reactive Streams` and performed two back-to-back database operations. First, it creates the order item mapping using OrderItemRepository, and then it removes the cart item mapping using ItemRepository.

Java Streams is used for creating an input list of OrderItemEntity instances in the first operation, and a list of item IDs in the second operation.

So far, we have made use of ReactiveCrudRepository methods. Let's implement a custom method using an entity template as shown next:

```java
@Override

public Mono<OrderEntity> insert(Mono<NewOrder> mdl) {

  AtomicReference<UUID> orderId = new AtomicReference<>();

  Mono<List<ItemEntity>> itemEntities = mdl
      .flatMap(m -> itemRepo.findByCustomerId(m.getCustomerId())
      .collectList().cache());

  Mono<CartEntity> cartEntity = mdl
      .flatMap(m -> cartRepo.findByCustomerId(m.getCustomerId()))
      .cache();

  cartEntity = Mono.zip(cartEntity, itemEntities, (c, i) -> {
    if (i.size() < 1) {
      throw new ResourceNotFoundException(String.format("No item found in customer's (ID:%s) cart.", c.getUser().getId()));
    }
    return c.setItems(i);
  }).cache();
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/repository/OrderRepositoryExtImpl.java

여기서는 OrderRepositoryExt 인터페이스의 insert() 메서드를 재정의합니다. insert() 메서드는 유창하고 기능적이며 반응적인 API로 채워져 있습니다. insert() 메서드는 NewOrder 모델 Mono 인스턴스를 새 주문 생성을 위한 페이로드를 포함하는 인수로 받습니다. Spring Data R2DBC는 중첩 엔티티를 가져오는 것을 허용하지 않습니다. 그러나 Cart와 해당 항목을 함께 가져올 수 있는 Order와 유사한 Cart용 사용자 지정 저장소를 작성할 수 있습니다.

Cart 및 Item 엔터티에 대해 ReactiveCrudRepository를 사용하고 있습니다. 따라서 하나씩 가져오고 있습니다. 먼저 항목 저장소를 사용하여 주어진 고객 ID를 기반으로 장바구니 항목을 가져옵니다. 고객이 장바구니와 일대일 매핑을 합니다. 그런 다음 고객 ID를 사용하여 장바구니 저장소를 사용하여 장바구니 엔터티를 가져옵니다.

Mono<List<ItemEntity>> 및 Mono<CartEntity>라는 두 개의 개별 Mono 개체를 얻습니다. 이제 그것들을 결합해야 합니다. Mono에는 두 개의 Mono 개체를 가져온 다음 Java BiFunction을 사용하여 병합할 수 있는 zip() 연산자가 있습니다. zip()은 주어진 Mono 객체가 모두 항목을 생성할 때만 새로운 Mono 객체를 반환합니다. zip()은 다형성이므로 다른 형식도 사용할 수 있습니다.

카트와 해당 품목, NewOrder 페이로드가 있습니다. 다음 코드 블록과 같이 이러한 항목을 사용하여 데이터베이스에 삽입해 보겠습니다.

```java
  R2dbcEntityTemplate template = new R2dbcEntityTemplate(connectionFactory);
  Mono<OrderEntity> orderEntity = Mono.zip(mdl, cartEntity, (m, c) -> toEntity(m, c)).cache();
  return orderEntity.flatMap(oe -> dbClient.sql("""
      INSERT INTO ecomm.orders (address_id, card_id, customer_id, order_date, total, status) VALUES($1, $2, $3, $4, $5, $6)""")

      .bind("$1", Parameter.fromOrEmpty(oe.getAddressId(),UUID.class))
      .bind("$2", Parameter.fromOrEmpty(oe.getCardId(), UUID.class))
      .bind("$3", Parameter.fromOrEmpty(oe.getCustomerId(),UUID.class))

      .bind("$4", OffsetDateTime.ofInstant(oe.getOrderDate().                  toInstant(),
              ZoneId.of("Z")).truncatedTo(ChronoUnit.MICROS))
      .bind("$5", oe.getTotal())
      .bind("$6", StatusEnum.CREATED.getValue())
                     .map(new OrderMapper()::apply)
      .one())
      .then(orderEntity.flatMap(x -> template.selectOne(
          query(where("customer_id").is(x.getCustomerId())
              .and("order_date")
            .greaterThanOrEquals(OffsetDateTime
              .ofInstant(x.getOrderDate().toInstant(),
                              ZoneId.of("Z"))
                .truncatedTo(ChronoUnit.MICROS))),
           OrderEntity.class).map(t -> x.setId(t.getId())
             .setStatus(t.getStatus()))
      ));
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/repository/OrderRepositoryExtImpl.java

We again use Mono.zip() to create an OrderEntity instance. Now, we can use values from this instance to insert into the orders table.

There are two ways to interact with the database for running SQL queries – by using either DatabaseClient or R2dbcEntityTemplate. Now, DatabaseClient is a lightweight implementation that uses the sql() method to deal with SQL directly, whereas R2dbcEntityTemplate provides a fluent API for CRUD operations. We have used both classes to demonstrate their usage.

First, we use DatabaseClient.sql() to insert the new order in the orders table. We use OrderMapper to map the row returned from the database to the entity. Then, we use the then() reactive operator to select the newly inserted record and then map it back to orderEntity using the R2dbcEntityTemplate.selectOne() method.

Similarly, you can create repositories for other entities. Now, we can use these repositories in services. Let's define them in the next sub-section.

## Adding services

Let's add a service for Order. There is no change in the server interface, as shown:

```java
public interface OrderService {
  Mono<OrderEntity> addOrder(@Valid Mono<NewOrder>newOrder);
  Mono<OrderEntity> updateMapping(@Valid OrderEntity orderEntity);
  Flux<OrderEntity> getOrdersByCustomerId(@NotNull @Valid String customerId);
  Mono<OrderEntity> getByOrderId(String id);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/service/OrderService.java

You just need to make sure that interface method signatures have Reactive types as returned types to keep the non-blocking flow in place.

Now, we can implement it in the following way:

```java
@Override
public Mono<OrderEntity> addOrder(@Valid Mono<NewOrder>     newOrder) {
  return repository.insert(newOrder);
}

@Override
public Mono<OrderEntity> updateMapping(@Valid OrderEntity     orderEntity) {
  return repository.updateMapping(orderEntity);
}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/service/OrderServiceImpl.java

The first two are straightforward; we just use the OrderRepository instance to call the respective methods. The third one is a bit tricky, as shown next:

```java
private BiFunction<OrderEntity, List<ItemEntity>, OrderEntity>
            biOrderItems = (o, fi) -> o.setItems(fi);

@Override
public Flux<OrderEntity> getOrdersByCustomerId(String customerId) {

  // will use the dummy Card Id that doesn't exist
  // if it is null

  return repository.findByCustomerId(customerId).flatMap(order   ->
      Mono.just(order)
          .zipWith(userRepo.findById(order.getCustomerId()))
          .map(t -> t.getT1().setUserEntity(t.getT2()))
          .zipWith(addRepo.findById(order.getAddressId()))
          .map(t -> t.getT1().setAddressEntity(t.getT2()))
          .zipWith(cardRepo.findById(
              order.getCardId() != null ? order.getCardId()
              : UUID.fromString(
                  "0a59ba9f-629e-4445-8129-b9bce1985d6a"))
              .defaultIfEmpty(new CardEntity()))
          .map(t -> t.getT1().setCardEntity(t.getT2()))
          .zipWith(itemRepo.findByCustomerId(
              order.getCustomerId().toString()).collectList(),
              biOrderItems)
  );
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/service/OrderServiceImpl.java

이 방법은 복잡해 보이지만 그렇지 않습니다. 여기서 수행하는 작업은 기본적으로 여러 저장소에서 데이터를 가져온 다음 OrderEntity 내부에 중첩된 엔터티를 채우는 것입니다. 이것은 map() 연산자와 함께 사용하거나 BiFunction을 별도의 인수로 사용하여 zipWith() 연산자를 사용하여 수행됩니다.

먼저 고객 ID를 사용하여 주문을 가져온 다음 주문을 평면 매핑하여 중첩된 엔터티를 채웁니다. 따라서 flatMap() 연산자 내부에서 zipWith()를 사용하고 있습니다. 첫 번째 zipWith()를 관찰하면 사용자 엔터티를 가져온 다음 map() 연산자를 사용하여 중첩된 사용자 엔터티의 속성을 설정합니다. 마찬가지로 다른 중첩 엔터티가 채워집니다.

마지막 zipWith() 연산자에서 BiFunction biOrderItems를 사용하여 OrderEntity 인스턴스의 항목 엔터티를 설정합니다.

다음 코드와 같이 OrderService 인터페이스의 마지막 메서드를 구현하는 데 동일한 알고리즘이 사용됩니다.

```java
@Override
public Mono<OrderEntity> getByOrderId(String id) {
  return repository.findById(UUID.fromString(id)).flatMap(order ->

      Mono.just(order)
          .zipWith(userRepo.findById(order.getCustomerId()))
          .map(t -> t.getT1().setUserEntity(t.getT2()))
          .zipWith(addRepo.findById(order.getAddressId()))
          .map(t -> t.getT1().setAddressEntity(t.getT2()))
          .zipWith(cardRepo.findById(order.getCardId()))
          .map(t -> t.getT1().setCardEntity(t.getT2()))

          .zipWith(itemRepo.findByCustomerId(
              order.getCustomerId().toString()).collectList(),
              biOrderItems)
  );
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/service/OrderServiceImpl.java

There is another way to merge two Mono instances using the Mono.zip() operator, as shown next:

```java
private BiFunction<CartEntity, List<ItemEntity>, CartEntity>         cartItemBiFun = (c, i) -> c
      .setItems(i);

@Override
public Mono<CartEntity> getCartByCustomerId(String customerId) {
  Mono<CartEntity> cart = repository.findByCustomerId(customerId)
      .subscribeOn(Schedulers.boundedElastic());

  Flux<ItemEntity> items = itemRepo.findByCustomerId(customerId)
      .subscribeOn(Schedulers.boundedElastic());
  return Mono.zip(cart, items.collectList(), cartItemBiFun);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/service/CartServiceImpl.java

이 예제는 CartServiceImpl 클래스에서 가져온 것입니다. 여기서 우리는 두 개의 개별 호출을 수행합니다. 하나는 장바구니 저장소를 사용하고 다른 하나는 항목 저장소를 사용합니다. 결과적으로 이 두 호출은 두 개의 Mono 인스턴스를 생성하고 Mono.zip() 연산자를 사용하여 병합합니다. 이것은 Mono를 사용하여 직접 호출합니다. 이전 예제는 zipWith() 연산자가 있는 Mono/Flux 인스턴스에서 사용되었습니다.

유사한 기술을 사용하여 나머지 서비스가 생성되었습니다. https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter05/src/main/java/com/packt/modern/api에서 찾을 수 있습니다. /서비스.

Reactive API 구현의 마지막 하위 섹션으로 초점을 옮겨 보겠습니다.

## 컨트롤러 구현 추가

REST 컨트롤러 인터페이스는 이미 OpenAPI Codegen 도구에 의해 생성됩니다. 이제 이러한 인터페이스의 구현을 만들 수 있습니다. 이번에 유일하게 다른 점은 서비스와 어셈블러를 호출하는 리액티브 파이프라인이 있다는 것입니다. 또한 생성된 계약을 기반으로 Mono 또는 Flux로 래핑된 ResponseEntity 개체만 반환해야 합니다.

Orders REST API의 컨트롤러 인터페이스인 OrderApi를 구현해 보겠습니다.

```java
@RestController
public class OrderController implements OrderApi {
  private final OrderRepresentationModelAssembler assembler;
  private OrderService service;
  public OrderController(OrderService service,
             OrderRepresentationModelAssembler assembler) {
    this.service = service;
    this.assembler = assembler;
  }
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/controller/OrderController.java

Here, @RestController is a trick that combines @Controller and @ResponseBody. These are the same annotations we used in Chapter 4, Writing Business Logic for APIs, for creating the REST controller. However, the methods have different signatures now to apply the Reactive pipelines. Make sure you don't break the Reactive chain of calls or add any blocking calls. If you do, either the REST call will not be fully non-blocking or you may see undesired results.

We use constructor-based dependency injection to inject the order service and assembler. Let's add the method implementations:

```java
@Override
public Mono<ResponseEntity<Order>> addOrder(@Valid Mono<NewOrder> newOrder,
    ServerWebExchange exchange) {

  return service.addOrder(newOrder.cache())
      .zipWhen(x -> service.updateMapping(x))
      .map(t -> status(HttpStatus.CREATED)
        .body(assembler.entityToModel(t.getT2(), exchange)))
      .defaultIfEmpty(notFound().build());
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/controller/OrderController.java

Both the method argument and return type are Reactive types (Mono), used as a wrapper. Reactive controllers also have an extra parameter, ServerWebExchange, which we discussed earlier.

In this method, we simply pass the newOrder instance to the service. We have used cache() because we need to subscribe to it more than once. We get the newly created EntityOrder by the addOrder() call. Then, we use the zipWhen() operator, which performs the updateMapping operation using the newly created order entity. At the end, we send it by wrapping it inside ResponseEntity. Also, it returns NOT FOUND 404 when an empty instance is returned.

Let's have a look at other API interface implementations:

```java
@Override
public Mono<ResponseEntity<Flux<Order>>>     getOrdersByCustomerId(@NotNull @Valid String customerId,    ServerWebExchange exchange) {
  return Mono.just(ok(assembler.toListModel(
    service.getOrdersByCustomerId(customerId), exchange)));
}

@Override
public Mono<ResponseEntity<Order>> getByOrderId(String id,     ServerWebExchange exchange) {
  return service.getByOrderId(id).map(o ->                           assembler.entityToModel(o, exchange))
      .map(ResponseEntity::ok)
      .defaultIfEmpty(notFound().build());
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/controller/OrderController.java

Both are kind of similar in nature; the service returns OrderEntity based on the given customer ID and order ID. It then gets converted into a model and is wrapped inside ResponseEntity and Mono.

Similarly, other REST controllers are implemented using the same approach. You can find the rest of them at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter05/src/main/java/com/packt/modern/api/controller.

We are almost done with Reactive API implementation. Let's look into some other minor changes.

### Adding H2 Console to an application

The H2 Console app is not available by default in Spring WebFlux the way it is available in Spring MVC. However, you can add it by defining the bean on your own, as shown:

```java
@Component
public class H2ConsoleComponent {
    private final static Logger log = LoggerFactory.getLogger(H2ConsoleComponent.class);
    private Server webServer;

    @Value("${modern.api.h2.console.port:8081}")
    Integer h2ConsolePort;

    @EventListener(ContextRefreshedEvent.class)
    public void start() throws java.sql.SQLException {
      log.info("starting h2 console at port "+h2ConsolePort);

      this.webServer = org.h2.tools.Server.createWebServer("-webPort", h2ConsolePort.toString(), "-tcpAllowOthers").start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
      log.info("stopping h2 console at port " + h2ConsolePort);
      this.webServer.stop();
    }
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/H2ConsoleComponent.java

이것은 간단합니다. 각각 ContextRefreshEvent 및 ContextStopEvent에서 실행되는 start() 및 stop() 메서드를 추가했습니다. ContextRefreshEvent는 ApplicationContext가 새로 고쳐지거나 초기화될 때 발생하는 애플리케이션 이벤트입니다. ContextStopEvent는 ApplicationContext가 닫힐 때 시작되는 애플리케이션 이벤트이기도 합니다.

start() 메서드는 H2 라이브러리를 사용하여 웹 서버를 만들고 지정된 포트에서 시작합니다. stop() 메서드는 H2 웹 서버, 즉 H2 콘솔 앱을 중지합니다.

H2 콘솔을 실행하려면 다른 포트가 필요하며 application.properties 파일에 modern.api.h2.console.port=8081 속성을 추가하여 구성할 수 있습니다. h2ConsolePort 속성은 @Value("${modern.api.h2.console.port:8081}")로 주석 처리되므로 H2ConsoleComponent 빈이 초기화될 때 application.properties에 구성된 값이 선택되어 h2ConsolePort에 할당됩니다. 스프링 프레임워크. 속성이 application.properties 파일에 정의되지 않은 경우 값 8081이 할당됩니다.

application.properties에 대해 논의 중이므로 다른 변경 사항을 살펴보겠습니다.

### Adding application configuration

We are going to use Flyway for database migration. Let's add the configuration required for it:

```yaml
spring.flyway.url=jdbc:h2:file:./data/ecomm;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE;DATABASE_TO_UPPER=FALSE;DB_CLOSE_ON_EXIT=FALSE

spring.flyway.schemas=ecomm
spring.flyway.user=
spring.flyway.password=
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/resources/application.properties

You must be wondering why we are using JDBC here, instead of R2DBC. Because Flyway hasn't yet started supporting R2DBC (at the time of writing). You can change it to R2DBC once support is added.

We have specified the ecomm schema and set a blank username and password.

Let's see the Spring Data configuration:

```yaml
spring.r2dbc.url=r2dbc:h2:file://././data/ecomm?options=AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE;DATABASE_TO_UPPER=FALSE;DB_CLOSE_ON_EXIT=FALSE
spring.r2dbc.driver=io.r2dbc:r2dbc-h2
spring.r2dbc.name=
spring.r2dbc.password=
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/resources/application.properties

Spring Data supports R2DBC, therefore we are using an R2DBC-based URL. We have set io.r2dbc:r2dbc-h2 for the driver to H2 and set a blank username and password.

Similarly, we have added the following logging properties in logback-spring.xml for adding debug statements in the console for Spring R2DBC and H2, as shown next:

```xml
<logger name="org.springframework.r2dbc" level="debug" additivity="false">
  <appender-ref ref="STDOUT"/>
</logger>
<logger name="reactor.core" level="debug" additivity="false">
  <appender-ref ref="STDOUT"/>
</logger>
<logger name="io.r2dbc.h2" level="debug" additivity="false">
  <appender-ref ref="STDOUT"/>
</logger>
```

This concludes our implementation of Reactive RESTful APIs. Now, you can test them.

## Testing Reactive APIs

Now, you must be looking forward to testing. You can find the Postman (API client) collection at the following location, which is based on Postman Collection version 2.1. You can import it and then test the APIs:

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/Chapter05.postman_collection.json

## 요약

비동기, 넌블로킹 및 기능적 패러다임을 사용한 Reactive API 개발에 대해 즐겁게 배웠기를 바랍니다. 유창하고 기능적인 패러다임에 익숙하지 않은 경우 언뜻 보기에 복잡할 수 있지만 연습을 통해 기능적 스타일의 코드만 작성하기 시작할 것입니다. 확실히 Java Streams 및 기능에 익숙하면 개념을 쉽게 이해할 수 있습니다.

이제 이 장의 끝에 도달했으므로 기능적 및 반응적 코드를 작성할 수 있는 기술을 갖추게 되었습니다. 이제 반응성, 비동기 및 넌블로킹 코드와 REST API를 작성할 수 있습니다. 또한 Reactive 프로그래밍이 존재하는 한 앞으로 더욱 견고해지고 향상될 R2DBC에 대해서도 배웠습니다.

다음 장에서는 RESTful 서비스 개발의 보안 측면을 살펴보겠습니다.

## 질문

- 애플리케이션 개발을 위해 Reactive 패러다임이 정말로 필요한가?
- Reactive 패러다임을 사용할 때의 단점이 있나요?
- Spring WebFlux에서 HTTP 요청 시 가입자 역할은 누가 하나요?

## Further reading

Project Reactor:
https://projectreactor.io

Spring Reactive documentation:
https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html

Spring Data R2DBC – reference documentation
https://docs.spring.io/spring-data/r2dbc/docs/current/reference/html/#preface

Hands-On Reactive Programming in Spring 5 (book)
https://www.packtpub.com/product/hands-on-reactive-programming-in-spring-5/9781787284951

Hands-On Reactive Programming with Java 12 (video)
https://www.packtpub.com/product/hands-on-reactive-programming-with-java-12-video/9781789808773
