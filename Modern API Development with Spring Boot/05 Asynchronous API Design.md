# 5장: 비동기 API 설계

지금까지 호출이 동기식인 기존 모델을 기반으로 RESTful 웹 서비스를 개발했습니다. 코드를 비동기 및 비차단으로 만들고 싶다면 어떻게 해야 할까요? 이것이 우리가 이 장에서 할 일입니다. 호출이 비동기 및 비차단인 이 장에서 비동기 API 설계에 대해 배울 것입니다. 자체적으로 Project Reactor(https://projectreactor.io)를 기반으로 하는 Spring WebFlux를 사용하여 이러한 API를 개발할 것입니다.

먼저 Reactive 프로그래밍 기본 사항을 살펴본 다음 기존 전자 상거래 REST API(4장, API용 비즈니스 로직 작성에서 배웠음)를 비동기식(Reactive) API로 마이그레이션하여 기존의 (필수적인) 방식과 Reactive 방식의 프로그래밍을 상호 연관시키고 비교함으로써 더 쉽습니다.

이 장에서는 다음 항목에 대해 설명합니다.

- reactive 스트림 이해
- Spring WebFlux 탐색
- DispatcherHandler 이해
- 컨트롤러
- 함수 엔드포인트
- 전자상거래 앱을 위한 Reactive API 구현

이 장의 끝에서 반응형 API를 개발 및 구현하고 비동기식 API 개발을 탐색하는 방법을 배우게 됩니다.

## Technical requirements

The code present in this chapter is found at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter05.

## Understanding Reactive Streams

일반 Java 코드는 스레드 풀을 사용하여 비동기성을 구현합니다. 웹 서버는 요청을 처리하기 위해 스레드 풀을 사용합니다. 각 수신 요청에 스레드를 할당합니다. 응용 프로그램은 데이터베이스 연결에 스레드 풀을 사용합니다. 각 데이터베이스 호출은 별도의 스레드를 사용하고 결과를 기다립니다. 따라서 각 웹 요청 및 데이터베이스 호출은 자체 스레드를 사용합니다. 그러나 이와 관련된 대기가 있으므로 블로킹 호출입니다. 스레드는 데이터베이스에서 응답이 다시 수신되거나 응답 개체가 작성될 때까지 대기하고 리소스를 활용합니다. 이것은 JVM에서 사용할 수 있는 리소스만 사용할 수 있으므로 확장할 때 일종의 제한 사항입니다. 수평적 확장의 한 유형인 서비스의 다른 인스턴스와 함께 로드 밸런서를 사용하여 이 제한을 극복합니다.

지난 10년 동안 클라이언트-서버 아키텍처가 증가했습니다. 많은 IoT 지원 장치, 기본 앱이 있는 스마트폰, 일류 웹 앱 및 기존 웹 응용 프로그램이 등장했습니다. 응용 프로그램에는 타사 서비스가 있을 뿐만 아니라 다양한 데이터 소스가 있으므로 더 큰 응용 프로그램으로 연결됩니다. 또한 마이크로 서비스 기반 아키텍처는 서비스 자체 간의 통신을 증가시켰습니다. 이러한 상위 네트워크 통신 수요를 처리하려면 많은 리소스가 필요합니다. 따라서 스케일링이 필요합니다. 스레드는 비싸고 무한하지 않습니다. 효과적인 활용을 위해 차단하고 싶지 않습니다. 이것이 비동기성이 도움이 되는 곳입니다. 비동기식 호출에서 스레드는 호출이 완료되는 즉시 해제되고 JavaScript와 같은 콜백 유틸리티를 사용합니다. 소스에서 데이터를 사용할 수 있는 경우 데이터를 푸시합니다. 

Reactive Streams는 데이터 소스인 게시자가 데이터를 구독자에게 푸시하는 게시자-구독자 모델을 사용합니다. 반면에 Node.js는 단일 스레드를 사용하여 대부분의 리소스를 사용한다는 것을 알고 있을 것입니다. 이벤트 루프라고 하는 비동기식 비차단 설계를 기반으로 합니다.

Reactive API는 이벤트 루프 디자인을 기반으로 하며 푸시 스타일 알림을 사용합니다. 자세히 보면 Reactive Streams는 map, flatMap, filter와 같은 Java Streams 작업도 지원합니다. 내부적으로 Reactive Streams는 푸시 스타일을 사용하는 반면 Java Stream은 풀 모델에서 작동합니다. 즉, 컬렉션과 같은 소스에서 항목을 가져옵니다. Reactive에서 소스(게시자)는 데이터를 푸시합니다.

Reactive Streams에서 데이터 스트림은 비동기적이고 넌블록킹이며 백프레셔를 지원합니다. (역압에 대한 설명은 이 장의 구독자 섹션을 참조하십시오.)

Reactive Streams 사양에 따라 네 가지 기본 유형이 있습니다.

- Publisher
- Subscriber
- Subscription
- Processor

Let's have a look at each.

#### 게시자

게시자는 한 명 이상의 구독자에게 데이터 스트림을 제공합니다. 구독자는 subscriber() 메서드를 사용하여 게시자를 구독합니다. 각 구독자는 게시자에게 한 번만 구독해야 합니다. 가장 중요한 것은 게시자가 구독자의 요청에 따라 데이터를 푸시한다는 것입니다. reactive 스트림은 게으릅니다. 따라서 게시자는 구독자가 있는 경우에만 요소를 푸시합니다.

A publisher is defined as follows:

```java
package org.reactivestreams;
// T – type of element Publisher sends
public interface Publisher<T> {
    public void subscribe(Subscriber<? super T> s);
}
```

### 구독자

구독자는 게시자가 푸시한 데이터를 사용합니다. 게시자-구독자 통신은 다음과 같이 작동합니다.

1. 구독자 인스턴스가 Publisher.subscribe() 메서드에 전달되면 onSubscribe() 메서드를 트리거합니다. 여기에는 백프레셔, 즉 구독자가 게시자에게 요구하는 데이터의 양을 제어하는 ​​구독 매개변수가 포함되어 있습니다.

2. 첫 번째 단계 후 게시자는 Subscription.request(long) 호출을 기다립니다. Subscription.request() 호출이 이루어진 후에만 데이터를 구독자로 푸시합니다. 이 방법은 Publisher의 요소 수를 요구합니다.

    일반적으로 게시자는 구독자가 데이터를 안전하게 처리할 수 있는지 여부에 관계없이 구독자에게 데이터를 푸시합니다. 그러나 가입자는 안전하게 처리할 수 있는 데이터의 양을 가장 잘 알고 있습니다. 따라서 reactive 스트림에서 구독자는 구독 인스턴스를 사용하여 요소 수에 대한 수요를 게시자에게 전달합니다. 이것은 **back-pressure** 또는 흐름 제어로 알려져 있습니다.

    게시자가 구독자에게 속도를 줄이도록 요청했지만 속도를 늦출 수 없다면 어떻게 될까요? 이 경우 게시자는 실패, 삭제 또는 버퍼링 여부를 결정해야 합니다.

1. 2단계를 사용하여 요청이 이루어지면 게시자는 데이터 알림을 보내고 onNext() 메서드를 사용하여 데이터를 사용합니다. Subscription.request()에 의해 전달된 수요에 따라 게시자가 데이터 알림을 푸시할 때까지 트리거됩니다.

2. 마지막에 onError() 또는 onCompletion()이 터미널 상태로 트리거됩니다. Subscription.request()를 호출하더라도 이러한 호출 중 하나가 트리거된 후에는 알림이 전송되지 않습니다. 다음은 터미널 방법입니다.
   
    a. onError()는 오류가 발생하는 순간 호출됩니다.

    b. 모든 요소가 푸시되면 onCompletion()이 호출됩니다.

The Subscriber interface is defined as follows:

```java
package org.reactivestreams;

// T – type of element Publisher sends

public interface Subscriber<T> {
    public void onSubscribe(Subscription s);
    public void onNext(T t);
    public void onError(Throwable t);
    public void onComplete();
}
```

### 구독
구독은 게시자와 구독자 사이의 중재자입니다. Subscription.subscriber() 메서드를 호출하고 게시자에게 수요를 알리는 것은 구독자의 책임입니다. 구독자가 필요할 때 호출할 수 있습니다. cancel() 메서드는 게시자에게 데이터 알림 전송을 중지하고 리소스를 정리하도록 요청합니다.

A subscription is defined as follows:

```java
package org.reactivestreams;
public interface Subscription {
    public void request(long n);
    public void cancel();
}
```

### Processor
프로세서는 게시자와 구독자 사이의 다리 역할을 하며 처리 단계를 나타냅니다. 게시자와 구독자 모두로 작동하며 둘 다에서 정의한 계약을 따릅니다. 다음과 같이 정의됩니다.

```java
package org.reactivestreams;
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {
}
```

다음 예를 살펴보겠습니다. 여기서는 Flux.just() 정적 팩토리 메서드를 사용하여 Flux를 생성합니다. Flux는 Project Reactor의 게시자 유형입니다. 이 게시자는 4개의 정수 요소를 포함합니다. 그런 다음 감소 연산자(Java Streams와 유사)를 사용하여 합계 연산을 수행합니다.


```java
Flux<Integer> fluxInt = Flux.just(1, 10, 100, 1000).log();
fluxInt.reduce(Integer::sum)
    .subscribe(sum -> System.out.printf("Sum is: %d", sum));
```

When you run this code, it prints the following output:
```
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

출력을 보면 게시자가 구독할 때 구독자가 무제한 Subscription.request()를 보냅니다. 첫 번째 요소가 알림을 받으면 onNext()가 호출되는 식입니다. 마지막으로 게시자가 푸시 요소를 완료하면 onComplete() 이벤트가 호출됩니다. 이것이 반응 스트림이 작동하는 방식입니다.

이제 Reactive 스트림이 작동하는 방식에 대한 아이디어를 얻었으므로 Spring이 Spring WebFlux 모듈에서 이러한 Reactive 스트림을 사용하는 방법과 이유를 살펴보겠습니다.


## Spring WebFlux 살펴보기

기존 서블릿 API는 블로킹 API입니다. 이들은 API를 차단하는 입력 및 출력 스트림을 사용합니다. Servlet 3.0 컨테이너는 기본 이벤트 루프를 발전시키고 사용합니다. 비동기 요청은 비동기적으로 처리되지만 읽기 및 쓰기 작업은 여전히 ​​차단되는 입력/출력 스트림을 사용합니다. Servlet 3.1 컨테이너는 더 발전하고 비동기성을 지원하며 넌블로킹 I/O 스트림 API를 가지고 있습니다. 그러나 request.getParameters()와 같은 특정 Servlet API는 차단 중인 요청 본문을 구문 분석하고 Filter와 같은 동기 계약을 제공합니다. Spring MVC 프레임워크는 Servlet API 및 Servlet 컨테이너를 기반으로 합니다.

따라서 Spring은 완전히 non-blocking이고 back-pressure 기능을 제공하는 Spring WebFlux를 제공합니다. 적은 수의 스레드와 동시성을 제공하고 더 적은 수의 하드웨어 리소스로 확장됩니다. WebFlux는 비동기 로직의 선언적 구성을 지원하기 위해 유창하고 기능적이며 연속적인 스타일의 API를 제공합니다. 비동기 기능 코드를 작성하는 것은 명령형 코드를 작성하는 것보다 더 복잡합니다. 그러나 일단 사용하게 되면 정확하고 읽기 쉬운 코드를 작성할 수 있기 때문에 좋아하게 될 것입니다.

Spring WebFlux와 Spring MVC는 모두 공존할 수 있습니다. **그러나 Reactive 프로그래밍 모델을 효과적으로 사용하려면 Reactive 흐름과 블로킹 호출을 혼합해서는 안 됩니다.**

Spring WebFlux는 다음 기능과 원형을 지원합니다.

- 이벤트 루프 동시성 모델
- 주석이 달린 컨트롤러와 기능적 엔드포인트 모두
- 반응성 클라이언트
- Tomcat, Undertow, Jetty 등 Netty 및 Servlet 3.1 컨테이너 기반 웹 서버

Reactive API와 Reactor Core를 이해함으로써 WebFlux가 어떻게 작동하는지 깊이 파헤쳐 봅시다.

### Reactive APIs

Spring WebFlux API는 Reactive API이며 게시자를 입력으로 허용합니다. 그런 다음 WebFlux는 Reactor Core 또는 RxJava와 같은 Reactive 라이브러리에서 지원하는 유형에 맞게 조정합니다. 그런 다음 지원되는 Reactive 라이브러리 유형에 따라 입력을 처리하고 출력을 반환합니다. 이를 통해 WebFlux API는 다른 Reactive 라이브러리와 상호 운용할 수 있습니다.

기본적으로 Spring WebFlux는 핵심 의존성으로 Reactor(https://projectreactor.io)를 사용합니다. Project Reactor는 Reactive Streams 라이브러리를 제공합니다. WebFlux는 게시자로 입력을 받아 Reactor 유형에 적용한 다음 Mono 또는 Flux 출력으로 반환합니다.

Reactive Streams의 게시자는 수요에 따라 구독자에게 데이터를 푸시한다는 것을 알고 있습니다. 하나 이상의(무한) 요소를 푸시할 수 있습니다. Project Reactor는 더 나아가 Mono와 Flux라는 두 가지 Publisher 구현을 제공합니다. Mono는 구독자에게 0 또는 1개를 반환할 수 있지만 Flux는 0에서 N 개의 요소를 반환합니다. 둘 다 CorePublisher 인터페이스를 구현하는 추상 클래스입니다. CorePublisher 인터페이스는 게시자를 확장합니다.

Normally, we have the following methods in the repository:

```java
public Product findById(UUID id);
public List<Product> getAll();
```
These can be replaced with Mono and Flux:

```java
Public Mono<Product> findById(UUID id);
public Flux<Product> getAll();
```
핫 스트림과 콜드 스트림의 개념이 있습니다. 콜드 스트림의 경우 여러 구독자가 있으면 소스를 새로 시작하지만 핫 스트림은 여러 구독자에 대해 동일한 소스를 사용합니다. Project Reactor 스트림은 기본적으로 콜드입니다. 따라서 스트림을 사용하면 다시 시작할 때까지 재사용할 수 없습니다. 그러나 Project Reactor를 사용하면 cache() 메서드를 사용하여 콜드 스트림을 핫 스트림으로 전환할 수 있습니다. 이 두 가지 방법은 Mono 및 Flux 추상 클래스에서 모두 사용할 수 있습니다.

Let's understand the cold and hot stream concepts with some examples:
```java
Flux<Integer> fluxInt = Flux.just(1, 10, 100).log();

fluxInt.reduce(Integer::sum)
    .subscribe(sum -> System.out.printf("Sum is: %d\n", sum));

fluxInt.reduce(Integer::max)
    .subscribe(max -> System.out.printf("Maximum is: %d", max));
```
Here, we are creating Flux of three numbers. Then, we are performing two operations separately – sum and max. You can see that there are two subscribers. By default, Project Reactor streams are cold; therefore, when a second subscriber registers, it restarts, as shown in the following output:
```
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
The source is created in the same program, but what if the source is somewhere else, such as in an HTTP request, or you don't want to restart the source? In these cases, you can turn the cold stream into a hot stream by using cache(), as shown in the next code block. The only difference between this one and the previous code is that we have added a cache() call to Flux.just():

```java
Flux<Integer> fluxInt = Flux.just(1, 10, 100).log().cache();

fluxInt.reduce(Integer::sum)
    .subscribe(sum -> System.out.printf("Sum is: %d\n", sum));

fluxInt.reduce(Integer::max)
    .subscribe(max -> System.out.printf("Maximum is: %d", max));
```
Now, look at the output. The source has not restarted; instead, the same source is used again:

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

Reactive Core
This provides a foundation for developing a Reactive web application with Spring. A web application needs three levels of support for serving HTTP web requests:

Handling of web requests by the server:
a. HttpHandler: An interface that is an abstraction of a request/response handler over different HTTP server APIs, such as Netty or Tomcat:

public interface HttpHandler {

  Mono<Void> handle(ServerHttpRequest request, ServerHttpResponse response);

}

b. WebHandler: Provides support for user sessions, request and session attributes, a locale and principal for the request, form data, and so on

Handling of a web request call by the client using WebClient
Codecs (Encoder, Decoder, HttpMessageWriter, HttpMessageReader, and DataBuffer) for the serialization and deserialization of content at both the server and client level for the request and response
These components are at the core of Spring WebFlux. WebFlux application configuration also contains the following beans – webHandler (DispatcherHandler), WebFilter, WebExceptionHandler, HandlerMapping, HandlerAdapter, and HandlerResultHandler.

For REST service implementation, there are specific HandlerAdapter instances for the following web servers – Tomcat, Jetty, Netty, and Undertow. A web server such as Netty, which supports Reactive Streams, handles the subscriber's demands. However, if the server handler does not support Reactive Streams, then the org.springframework.http.server.reactive.ServletHttpHandlerAdapter HTTP HandlerAdapter is used. It handles the adaptation between Reactive Streams and Servlet 3.1 container async I/O and implements a Subscriber class. This uses the OS TCP buffers. OS TCP uses its own back-pressure (control flow); that is, when the buffer is full, the OS uses the TCP back-pressure to stop incoming elements.

The browser, or any HTTP client, consumes REST APIs using the HTTP protocol. When a request is received by the web server, it forwards it to the Spring WebFlux application. Then, WebFlux builds the Reactive pipeline that goes to the controller. HttpHandler is an interface between WebFlux and the web server that communicates using the HTTP protocol. If the underlying server supports Reactive Streams, such as Netty, then the subscription is done by the server natively. Else, WebFlux uses ServletHttpHandlerAdapter for Servlet 3.1 container-based servers. ServletHttpHandlerAdapter then adapts the streams to async I/O Servlet APIs and vice versa. Then, the subscription of Reactive Streams happens with ServletHttpHandlerAdapter.

Therefore, in summary, Mono/Flux streams are subscribed by WebFlux internal classes, and when the controller sends a Mono/Flux stream, these classes convert it into HTTP packets. The HTTP protocol does support event streams. However, for other media types, such as JSON, Spring WebFlux subscribes the Mono/Flux streams and waits until onComplete() or onError() is triggered. Then, it serializes the whole list of elements, or a single element in the case of Mono, in one HTTP response.

Spring WebFlux needs a component similar to DispatcherServlet in Spring MVC – a front controller. Let's discuss this in the next section.

### Understanding DispatcherHandler
DispatcherHandler, a front controller in Spring WebFlux, is what DispatcherServlet is in the Spring MVC framework. DispatcherHandler contains an algorithm that makes use of special components – HandlerMapping (maps requests to the handler), HandlerAdapter (a DispatcherHandler helper to invoke a handler mapped to a request), and HandlerResultHandler (a palindrome of words, for processing the result and forming results) – for processing requests. The DispatcherHandler component is identified by a bean named webHandler.

It processes requests in the following way:

1. A web request is received by DispatcherHandler.

2. DispatcherHandler uses HandlerMapping to find a matching handler for the request and uses the first match.

3. It then uses the respective HandlerAdapter to process the request, which exposes the HandlerResult (return value after processing). The return value could be one of the following – ResponseEntity, ServerResponse, values returned from @RestController, or values (CharSequence, view, map, and so on) returned by a view resolver.

4. Then, it makes use of the respective HandlerResultHandler to write the response or render a view based on the HandlerResult type received from step 2. ResponseEntityResultHandler is used for ResponseEntity, ServerResponseResultHandler is used for ServerResponse, ResponseBodyResultHandler is used for values returned by @RestController or @ResponseBody-annotated methods, and ViewResolutionResultHandler is used for values returned by the view resolver.

5. The request is completed.

You can create REST endpoints in Spring WebFlux using either an annotated controller such as Spring MVC or functional endpoints. Let's explore these in the next sections.

## Controllers

The Spring team has kept the same annotations for both Spring MVC and Spring WebFlux as these annotations are non-blocking. Therefore, you can use the same annotations we have used in previous chapters for creating REST controllers. There, the annotation runs on Reactive Core and provides a non-blocking flow. However, you, as the developer, have the responsibility of maintaining a fully non-blocking flow and maintaining the Reactive chain (pipeline). Any blocking calls in a Reactive chain would convert the Reactive chain into a blocking call.

Let's create a simple REST controller that supports non-blocking and Reactive calls:

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
You can see that it uses all the annotations that we have used in Spring MVC:

- @RestController is used for marking a class as a REST controller. Without this, the endpoint won't register and the request will be returned as NOT FOUND 404.

- @RequestMapping is used for defining the path and HTTP method. Here, you can also use @PostMapping with just the path. Similarly, for each of the HTTP methods, a respective mapping is there, such as @GetMapping.

- The @RequestBody annotation marks a parameter as a request body, and an appropriate codec would be used for conversion. Similarly, there is - @PathVariable and @RequestParam for the path parameter and query parameter, respectively.

We are going to use an annotation-based model for writing the REST endpoints. You'll get a closer look when we implement the e-commerce app controllers using WebFlux. Spring WebFlux also provides a way to write a REST endpoint using a functional programming style that you'll explore in the next section.


## Functional endpoints

The REST controllers we coded using Spring MVC were written in imperative-style programming. Reactive programming, on the other hand, is functional-style programming. Therefore, Spring WebFlux also allows an alternative way to define REST endpoints, using functional endpoints. These also use the same Reactive Core foundation.

Let's see how we can write the same order REST endpoint using a functional endpoint:

```java
OrderRepository repository = ...
OrderHandler handler = new OrderHandler(repository);

RouterFunction<ServerResponse> route = route()
    .GET("/v1/api/orders/{id}", accept(APPLICATION_JSON),                handler::getOrderById)
    .POST("/v1/api/orders", handler::addOrder)
    .build();

public class OrderHandler {
    public Mono<ServerResponse> addOrder(ServerRequest req){
        // ...
    }
    public Mono<ServerResponse> getOrderById(ServerRequest req) {
        // ...
    }
}
```
You can see that the RouterFunctions.route() builder allows you to write all the REST routes in a single statement using the functional programming style. Then, it uses the method reference of the handler class to process the request, which is exactly the same as the @RequestMapping body of an annotation-based model.

Let's add the following code in the OrderHandler methods:

```java
public class OrderHandler {
    public Mono<ServerResponse> addOrder(ServerRequest req){
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
Unlike the @RequestMapping() mapping methods in the REST controller, handler methods don't have multiple parameters such as body, path, or query parameters. They just have a ServerRequest parameter, which can be used to extract the body, path, and query parameters. In the addOrder method, the Order object is extracted using request.bodyToMono(), which parses the request body and then converts it into an Order object. Similarly, the ID is extract from a request using request.pathVariable() in the getOrderById() handler method.

Now, let's discuss the response. The handler method uses the ServerResponse object in comparison to ResponseEntity in Spring MVC. Therefore, the ok() static method looks like it's from ResponseEntity, but it is from org.springframework.web.reactive.function.server.ServerResponse.ok. The Spring team has tried to keep the API as similar as possible to Spring MVC; however, the underlying implementation differs and provides a non-blocking Reactive interface.

The last point about these handler methods is the way a response is written. It uses a functional style instead of an imperative style and makes sure that the Reactive chain does not break. The repository returns the Mono object (a publisher) in both cases and returns it as a response wrapped inside ServerResponse.

You can find interesting code in the getOrderById() handler method. It performs a flatMap operation on the received Mono object from the repository. It converts it from an entity into a model, then wraps it in a ServerResponse object and returns the response. You must be wondering what happens if the repository returns null. The repository returns Mono as per the contract, which is similar in nature to the Java Optional class. Therefore, the Mono object can be empty but not null, as per the contract. If the repository returns an empty Mono, then the switchIfEmpty() operator will be used and a NOT FOUND 404 response will be sent.

In the case of an error, there are different error operators that can be used, such as doOnError() or onErrorReturn().

We have discussed the logic flow using the Mono type; the same explanation will apply if you use the Flux type in place of the Mono type.

We have discussed a lot of theory relating to Reactive, asynchronous, and non-blocking programming in a Spring context. Let's jump into coding and migrate the e-commerce API developed in Chapter 4, Writing Business Logic for APIs, to a Reactive API.

Implementing Reactive APIs for our e-commerce app
Now that you have an idea of how Reactive streams work, we can go ahead and implement REST APIs that are asynchronous and non-blocking.

You'll recall that we are following the design-first approach, so we need the API design specification first. However, we can reuse the e-commerce API specification we created previously in Chapter 3, API Specifications and Implementation.

OpenAPI Codegen is used for generating the API interface/contract that generates the Spring MVC-compliant API Java interfaces. Let's see what changes we need to do to generate the Reactive API interfaces.

### Changing OpenAPI Codegen for Reactive APIs

You need to tweak few OpenAPI Codegen configurations to generate Spring WebFlux-compliant Java interfaces, as shown next:

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

Reactive API support is only there if you opt for spring-boot as library. Also, you need to set the reactive flag to true. By default, the reactive flag is false.

Now, you can run the following command:
```sh
$ gradlew clean generateSwaggerCode
```
This will generate Reactive Streams-compliant Java interfaces, which are annotation-based REST controller interfaces. When you open any API interface, you'll find Mono/Flux reactor types in it, as shown in the following code block for the OrderAPI interface:

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
You would have observed another change: an additional parameter, ServerWebExchange, is also required for Reactive controllers.

Now, when you compile your code, you may find compilation errors, because we haven't yet added the dependencies required for Reactive support. Let's learn how to add them in the next section.

###  Adding Reactive dependencies in build.xml

First, we'll remove spring-boot-starter-web as we don't need Spring MVC now. Second, we'll add spring-boot-starter-webflux and reactor-test for Spring WebFlux and Reactor support tests, respectively. Once these dependencies are added successfully, you should not see any compilation errors in the OpenAPI-generated code.

You can add the required Reactive dependencies in build.gradle as shown next:

```
implementation 'org.springframework.boot:spring-boot-starter-webflux'

// implementation 'org.springframework.boot:spring-boot-

// starter-web'

testImplementation('org.springframework.boot:spring-boot-                    starter-test')

testImplementation 'io.projectreactor:reactor-test'
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/build.gradle

We need to have a complete Reactive pipeline from the REST controller to the database. However, existing JDBC and Hibernate dependencies only support blocking calls. JDBC is a fully blocking API. Hibernate is also blocking. Therefore, we need to have Reactive dependencies for the database.

Hibernate Reactive (https://github.com/hibernate/hibernate-reactive) is in beta version at the time of writing this chapter. Hibernate Reactive beta only supports PostgresSQL, MySQL, and Db2. Hibernate Reactive does not support H2 at the time of writing. Therefore, we would simply use Spring Data, a Spring framework that provides the spring-data-r2dbc library for working with Reactive Streams.

Many NoSQL databases, such as MongoDB, already provide a Reactive database driver. An R2DBC-based driver should be used for relational databases in place of JDBC for fully non-blocking/Reactive API calls. R2DBC stands for Reactive Relational Database Connectivity. R2DBC is a Reactive API open specification that establishes a Service Provider Interface (SPI) for database drivers. Almost all the popular relation databases support R2DBC drivers – H2, MySQL, MariaDB, SQL Server, PostgresSQL, and Proxy. Oracle DB, at the time of writing, provides flow-based Reactive JDBC extensions (DB 20c) that integrate with Reactor, RxJava, and Akka Streams. However, an Oracle R2DBC driver is soon to be launched (it hasn't yet been launched as of 2020).

Let's add the R2DBC dependencies for Spring Data and H2 in the build.gradle file:
```
// DB Starts

implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
implementation 'com.h2database:h2'
runtimeOnly 'io.r2dbc:r2dbc-h2'

// DB Ends
```
Now, we can write end-to-end (from the controller to the repository) code without any compilation errors. Let's add global exception handling before we jump into writing an implementation for API interfaces.


## Handling exceptions

We'll add the global exception handler the way it was added in Spring MVC in Chapter 3, API Specifications and Implementation. Before that, you must be wondering how to handle exceptions in a Reactive pipeline. Reactive pipelines are a flow of streams and you can't add exception handling the way you do in imperative code. You need to raise the error in a pipeline flow only.

Check out the following code:

```java
.flatMap(card -> {

  if (Objects.isNull(card.getId())) {

    return service.registerCard(mono)

        .map(ce -> status(HttpStatus.CREATED)

            .body(assembler.entityToModel(ce, exchange)));

  } else {

    return Mono.error(

        () -> new CardAlreadyExistsException(" for user with ID         - " + d.getId()));

  }

})
```

Here, a flatMap operation is performed. An error should be thrown if card is not valid, that is, if card does not have the requested ID. Here, Mono.error() is used because the pipeline expects Mono as a returned object. Similarly, you can use Flux.error() if Flux is expected as the returned type.

Let's assume you are expecting an object from a service or repository call, but instead you receive an empty object. Then you can use the switchIfEmpty() operator as shown in the next code:

```java
Mono<List<String>> monoIds = itemRepo.findByCustomerId(customerId)
    .switchIfEmpty(Mono.error(new ResourceNotFoundException(
        ". No items found in Cart of customer with Id - " +            customerId)))
    .map(i -> i.getId().toString())
    .collectList().cache();
```
Here, the code expects a Mono object of List from the item repository. However, if the returned object is empty, then it simply throws ResourceNotFoundException. switchIfEmpty() accepts the alternate Mono instance.

By now, you might have a question about the type of exception. It throws a runtime exception. See the ResourceNotFoundException class declaration here:

public class ResourceNotFoundException extends RuntimeException

Similarly, you can also use onErrorReturn(), onErrorResume(), or similar error operators from Reactive Streams. Look at the use of onErrorReturn() in the next code block:

```java
return service.getCartByCustomerId(customerId)

    .map(cart ->

        assembler.itemfromEntities(cart.getItems().stream()

            .filter(i -> i.getProductId().toString().                    equals(itemId.trim())).collect(toList()))

            .get(0)).map(ResponseEntity::ok)

    .onErrorReturn(notFound().build())
```

All exceptions should be handled and error response should be sent to the user. This is why we'll have a look at the global exception handler first.

### Handling global exceptions for controllers

You created a global exception handler using @ControllerAdvice in Spring MVC. We'll take a slightly different route for handling errors in Spring WebFlux. First, we'll create the ApiErrorAttributes class, which can also be used in Spring MVC. This class extends DefaultErrorAttributes, which is a default implementation of the ErrorAttributes interface. The ErrorAttributes interface provides a way to handle maps, a map of error fields, and their values. These error attributes can then be used for displaying an error to the user or for logging.

The following attributes are provided by the DefaultErrorAttributes class:

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
  public Map<String, Object> getErrorAttributes(ServerRequest       request, ErrorAttributeOptions options) {

    var attributes = super.getErrorAttributes(request, options);

    attributes.put("status", status);
    attributes.put("message", message);
    attributes.put("code", ErrorCode.GENERIC_ERROR.                   getErrCode());

    return attributes;

  }

// Getter/Setters
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/exception/ApiErrorAttributes.java

Now, we can use this ApiErrorAttributes class in a custom global exception handler class. We'll create the ApiErrorWebExceptionHandler class, which extends the AbstractErrorWebExceptionHandler abstract class.

The AbstractErrorWebExceptionHandler class implements the ErrorWebExceptionHandler and InitializingBean interfaces. ErrorWebExceptionHandler is a functional interface that extends the WebExceptionHandler interface, which indicates that WebExceptionHandler is used for rendering exceptions. WebExceptionHandler is a contract for handling exceptions when server exchange processing takes place.

The InitializingBean interface is a part of the Spring core framework. It is used by components that react when all properties are populated. It can also be used to check whether all the mandatory properties are set.

Now that we have studied the basics, let's jump into writing the ApiErrorAttributes class:

```java
@Component
@Order(-2)
public class ApiErrorWebExceptionHandler extends       AbstractErrorWebExceptionHandler {

  public ApiErrorWebExceptionHandler(ApiErrorAttributes       errorAttributes,

      ApplicationContext applicationContext,      ServerCodecConfigurer serverCodecConfigurer) {

    super(errorAttributes, new WebProperties().getResources(),           applicationContext);

    super.setMessageWriters(          serverCodecConfigurer.getWriters());

    super.setMessageReaders(          serverCodecConfigurer.getReaders());

  }

  @Override

  protected RouterFunction<ServerResponse>       getRoutingFunction(ErrorAttributes errorAttributes) {

    return RouterFunctions.route(

        RequestPredicates.all(), this::renderErrorResponse);

  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/exception/ApiErrorWebExceptionHandler.java

The first important observation about this code is that we have added the @Order annotation, which tells us the preference of execution. ResponseStatusExceptionHandler is ordered at 0 by the Spring Framework and DefaultErrorWebExceptionHandler is ordered at -1. Both are exception handlers like the one we have created. If you don't give a precedence order to ApiErrorWebExceptionHandler over both of these, then it won't ever execute. Therefore, the order is set at -2.

Next, this class overrides the getRoutingFunction() method, which calls the privately defined renderErrorResponse() method, where we have our own custom implementation for error handling, as shown next:

```java
private Mono<ServerResponse> renderErrorResponse(    ServerRequest request) {

  Map<String, Object> errorPropertiesMap =       getErrorAttributes(request,      ErrorAttributeOptions.defaults());

  Throwable throwable = (Throwable) request

      .attribute("org.springframework.boot.web.reactive.error.                 DefaultErrorAttributes.ERROR")

      .orElseThrow(

          () -> new IllegalStateException("Missing exception           attribute in ServerWebExchange"));

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

      errorPropertiesMap.put(        "status", HttpStatus.BAD_REQUEST);

      errorPropertiesMap.put("code", ILLEGAL_ARGUMENT_        EXCEPTION.getErrCode());

      errorPropertiesMap.put("error",         ILLEGAL_ARGUMENT_EXCEPTION);

      errorPropertiesMap.put("message", String

          .format("%s %s", ILLEGAL_ARGUMENT_EXCEPTION.                  getErrMsgKey(), throwable.getMessage()));

      return ServerResponse.status(HttpStatus.BAD_REQUEST)

          .contentType(MediaType.APPLICATION_JSON)

          .body(BodyInserters.fromValue(errorPropertiesMap));

    }

    case CUSTOMER_NOT_FOUND -> {

      errorPropertiesMap.put("status", HttpStatus.NOT_FOUND);

      errorPropertiesMap.put("code",         CUSTOMER_NOT_FOUND.getErrCode());

      errorPropertiesMap.put("error", CUSTOMER_NOT_FOUND);

      errorPropertiesMap.put("message", String           .format("%s %s", CUSTOMER_NOT_FOUND.getErrMsgKey(),              throwable.getMessage()));

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

Probably in the next version of Java, we will be able to combine the if-else and switch blocks to make the code more concise. You can also create a separate method that takes errorPropertiesMap as an argument and returns the formed server response based on it. Then, you can use switch.

Custom application exception classes, such as CustomerNotFoundException, and other exception handling-supported classes, such as ErrorCode and Error, are being used from the existing code (from Chapter 4, Writing Business Logic for APIs).

Now that we have studied exception handling, we can concentrate on HATEOAS.

Adding hypermedia links to an API response
HATEOAS support for Reactive APIs is there and is a bit similar to what we did in the previous chapter using Spring MVC. We create these assemblers again for HATEOAS support. We also use the HATEOAS assembler classes for conversion from a model to an entity and vice versa.

Spring WebFlux provides the ReactiveRepresentationModelAssembler interface for forming hypermedia links. We would override its toModel() method for adding the links to response models.

Here, we will do some groundwork for populating the links. We will create an HateoasSupport interface with a single default method as shown next:

```java
public interface HateoasSupport {

    default UriComponentsBuilder getUriComponentBuilder(      @Nullable ServerWebExchange exchange) {

      if (exchange == null) {

        return UriComponentsBuilder.fromPath("/");

      }

      ServerHttpRequest request = exchange.getRequest();

      PathContainer contextPath = request.getPath().                                  contextPath();

      return UriComponentsBuilder.fromHttpRequest(request)

          .replacePath(contextPath.toString())

          .replaceQuery("");

    }
}
```
This class contains a single default method, getUriCompononentBuilder(), which accepts ServerWebExchange as an argument and returns the UriComponentsBuilder instance. This instance can then be used to extract the server URI that would be used for adding the links with a protocol, host, and port. If you remember, the ServerWebExchange argument was added to controller methods. This interface is used for getting the HTTP request, response, and other attributes.

Now, we can use these two interfaces – HateoasSupport and ReactiveRepresentationModelAssembler – for defining the representation model assemblers.

Let's define the address's representational model assembler as shown next:

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

  resource.add(Link.of(String.format("%s/api/v1/addresses",                serverUri)).withRel("addresses"));

  resource.add(
      Link.of(String.format("%s/api/v1/addresses/%s",               serverUri, entity.getId())).withSelfRel());

  return resource;
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/hateoas/AddressRepresentationModelAssembler.java

The toModel() method returns the Mono<Address> object with hypermedia links formed from the AddressEntity instance using the entityToModel() method.

entityToModel() copies the properties from the entity instance to the model instance. Most importantly, it adds hypermedia links to the model using the resource.add() method. The add() method takes the org.springframework.hateoas.Link instance as an argument. Then, we use the Link class's of() static factory method to form the link. You can see that a server URI is used here to add it to the link. You can form as many links as you want and add these to the resource using the add() method.

The ReactiveRepresentationModelAssembler interface provides the toCollectionModel() method with a default implementation that returns the Mono<CollectionModel<D>> collection model. However, we can also add the toListModel() method as shown here, which returns the Flux instance of addresses:

```java
public Flux<Address> toListModel(Flux<AddressEntity> entities,       ServerWebExchange exchange) {

  if (Objects.isNull(entities)) {

    return Flux.empty();

  }

  return Flux.from(entities.map(e -> entityToModel(                   e, exchange)));

}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/hateoas/AddressRepresentationModelAssembler.java

This method internally uses the entityToModel() method. Similarly, you can create a representation model assembler for other API models. You can find all these models at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/hateoas.

Now that we are done with the basic code infrastructure, we can develop the API implementation based on the interfaces generated by OpenAPI Codegen. Here, we'll first develop the repositories that will be consumed by the services. At the end, we'll write the controller implementation. Let's start with the repositories.

Defining an entity
Entities are defined in more or less the same way as we defined and used them in Chapter 4, Writing Business Logic for APIs. However, instead of using Hibernate mappings and JPA, we'll use Spring Data annotations as shown here:

```java
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

// other imports

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

We use @Table to associate an entity class to a table name and @Column for mapping a field to a column of the table. As is obvious, @Id is used as the identifier column. Similarly, you can define the other entities.

Adding repositories
A repository is an interface between our application code and database. It is the same as what was there in Spring MVC. However, we are writing the code using the Reactive paradigm. Therefore, it is necessary to have repositories that use an R2DBC-/Reactive-based driver and return instances of Reactive types on top of Reactive streams. This is the reason why we can't use JDBC.

Spring Data R2DBC provides different repositories for Reactor and RxJava, such as ReactiveCrudRepository, ReactiveSortingRepository, RxJava2CrudRepository, and RxJava3CrudRepository. Also, you can write your own custom implementation.

We are going to use ReactiveCrudRepository and write a custom implementation also.

We'll write repositories for the Order entity. For other entities, you can find the repositories at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter05/src/main/java/com/packt/modern/api/repository.

First, let's write the CRUD (Create, Read, Update, and Delete) repository for the Order entity as shown next:

```java
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

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

public class OrderRepositoryExtImpl implements     OrderRepositoryExt {

  private ConnectionFactory connectionFactory;

  private DatabaseClient dbClient;

  private ItemRepository itemRepo;

  private CartRepository cartRepo;

  private OrderItemRepository oiRepo;

  public OrderRepositoryExtImpl(ConnectionFactory       connectionFactory, ItemRepository itemRepo,      OrderItemRepository oiRepo, CartRepository cartRepo,       DatabaseClient dbClient) {

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

      .setAddressId(UUID.fromString(          order.getAddress().getId()))

      .setOrderDate(Timestamp.from(Instant.now()))

      .setTotal(c.getItems().stream().collect(Collectors.            toMap(k -> k.getProductId(),

          v -> BigDecimal.valueOf(v.getQuantity()).multiply(v.            getPrice())))

          .values().stream().reduce(BigDecimal::add).            orElse(BigDecimal.ZERO));

  return orderEntity;

}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/repository/OrderRepositoryExtImpl.java

This method is straightforward except for the code where the total is set. The total is calculated using the stream. Let's break it down as shown next to understand it:

First, it takes the items from the cart entity.
Then, it creates streams from items.
It creates a map with the key as the product ID and the value as the product of the quantity and price.
It takes the value from the map and converts it into a stream.
It performs the reduce operation by adding a method to BigDecimal. It then gives the total amount.
If values are not present, then it simply returns 0.
After the toEntity() method, we also need another mapper that reads rows from the database and converts them to OrderEntity. For this purpose, we'll write BiFunction, which is a part of the java.util.function package:

```java
class OrderMapper implements BiFunction<Row, Object,     OrderEntity> {

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

Here, we have created a pipeline of Reactive streams and performed two back-to-back database operations. First, it creates the order item mapping using OrderItemRepository, and then it removes the cart item mapping using ItemRepository.

Java Streams is used for creating an input list of OrderItemEntity instances in the first operation, and a list of item IDs in the second operation.

So far, we have made use of ReactiveCrudRepository methods. Let's implement a custom method using an entity template as shown next:

```java
@Override

public Mono<OrderEntity> insert(Mono<NewOrder> mdl) {

  AtomicReference<UUID> orderId = new AtomicReference<>();

  Mono<List<ItemEntity>> itemEntities = mdl

      .flatMap(m -> itemRepo.findByCustomerId(m.           getCustomerId())

      .collectList().cache());

  

  Mono<CartEntity> cartEntity = mdl

      .flatMap(m -> cartRepo.findByCustomerId(m.           getCustomerId()))

      .cache();

  cartEntity = Mono.zip(cartEntity, itemEntities, (c, i) -> {

    if (i.size() < 1) {

      throw new ResourceNotFoundException(String

        .format("No item found in customer's (ID:%s) cart.",                  c.getUser().getId()));

    }

    return c.setItems(i);

  }).cache();
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/repository/OrderRepositoryExtImpl.java

Here, we override the insert() method from the OrderRepositoryExt interface. The insert() method is filled with fluent, functional, and Reactive APIs. The insert() method receives a NewOrder model Mono instance as an argument that contains the payload for creating a new order. Spring Data R2DBC does not allow fetching nested entities. However, you can write a custom repository for Cart similar to Order that can fetch Cart and its items together.

We are using ReactiveCrudRepository for Cart and Item entities. Therefore, we are fetching them one by one. First, we use the item repository to fetch the cart items based on the given customer ID. Customer has a one-to-one mapping with Cart. Then, we fetch the Cart entity using the cart repository by using the customer ID.

We get the two separate Mono objects – Mono<List<ItemEntity>> and Mono<CartEntity>. Now, we need to combine them. Mono has a zip() operator that allows you to take two Mono objects and then use the Java BiFunction to merge them. zip() returns a new Mono object only when both the given Mono objects produce the item. zip() is polymorphic and therefore other forms are also available.

We have the cart and its items, plus the NewOrder payload. Let's use these items to insert them into a database as shown in the next code block:
```java
  R2dbcEntityTemplate template = new               R2dbcEntityTemplate(connectionFactory);

  Mono<OrderEntity> orderEntity = Mono.zip(mdl, cartEntity,                              (m, c) -> toEntity(m, c)).cache();

  return orderEntity.flatMap(oe -> dbClient.sql("""

      INSERT INTO ecomm.orders (address_id, card_id, customer_      id, order_date, total, status)       VALUES($1, $2, $3, $4, $5, $6)""")

      .bind("$1", Parameter.fromOrEmpty(oe.getAddressId(),                                           UUID.class))

      .bind("$2", Parameter.fromOrEmpty(oe.getCardId(), UUID.                                          class))

      .bind("$3", Parameter.fromOrEmpty(oe.getCustomerId(),

                                          UUID.class))

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
public Flux<OrderEntity> getOrdersByCustomerId(String     customerId) {

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/service/OrderServiceImpl.java

This method looks complicated but it's not. What you are doing here is basically fetching data from multiple repositories and then populating the nested entities inside OrderEntity. This is done by using the zipWith() operator by using either the map() operator alongside it or BiFunction as a separate argument.

It first fetches the orders by using the customer ID, then flat maps the orders to populate its nested entities. Therefore, we are using zipWith() inside the flatMap() operator. If you observer the first zipWith(), it fetches the user entity and then sets the nested user entity's property using the map() operator. Similarly, other nested entities are populated.

In the last zipWith() operator, we are using the BiFunction biOrderItems to set the item entities in the OrderEntity instance.

The same algorithm is used for implementing the last method of the OrderService interface, as shown in the following code:

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
  Mono<CartEntity> cart = repository.        findByCustomerId(customerId)
      .subscribeOn(Schedulers.boundedElastic());

  Flux<ItemEntity> items = itemRepo.        findByCustomerId(customerId)
      .subscribeOn(Schedulers.boundedElastic());
  return Mono.zip(cart, items.collectList(), cartItemBiFun);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/service/CartServiceImpl.java

This example is taken from the CartServiceImpl class. Here, we make two separate calls – one using the cart repository and another one from the item repository. As a result, these two calls produce two Mono instances, and merge them using the Mono.zip() operator. This we call directly using Mono; the previous example was used on Mono/Flux instances with the zipWith() operator.

Using similar techniques, the remaining services have been created. Those you can find at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter05/src/main/java/com/packt/modern/api/service.

Let's move our focus on to the last sub-section of our Reactive API implementation.

## Adding controller implementations

REST controller interfaces are already generated by the OpenAPI Codegen tool. We can now create an implementation of those interfaces. The only different thing this time is having the Reactive pipelines to call the services and assemblers. You should also only return ResponseEntity objects wrapped in either Mono or Flux based on the generated contract.

Let's implement OrderApi, which is the controller interface for the Orders REST API:

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

Adding H2 Console to an application
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

      this.webServer = org.h2.tools.Server.createWebServer(          "-webPort", h2ConsolePort.toString(), "-tcpAllowOthers").start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
      log.info("stopping h2 console at port "+h2ConsolePort);
      this.webServer.stop();
    }
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter05/src/main/java/com/packt/modern/api/H2ConsoleComponent.java

This is straightforward; we have added the start() and stop() methods, which are executed on ContextRefreshEvent and ContextStopEvent, respectively. ContextRefreshEvent is an application event that gets fired when ApplicationContext is refreshed or initialized. ContextStopEvent is also an application event that gets fired when ApplicationContext is closed.

The start() method creates the web server using the H2 library and starts it on a given port. The stop() method stops the H2 web server, that is, the H2 Console app.

You need a different port to execute H2 Console, which can be configured by adding the modern.api.h2.console.port=8081 property in the application.properties file. The h2ConsolePort property is annotated with @Value("${modern.api.h2.console.port:8081}"), therefore the value configured in application.properties will be picked and assigned to h2ConsolePort when the H2ConsoleComponent bean is initialized by the Spring Framework. The value 8081 will be assigned if the property is not defined in the application.properties file.

Since we are discussing application.properties, let's have a look at some of the other changes.

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
<logger name="org.springframework.r2dbc" level="debug"     additivity="false">
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

## Summary

I hope you enjoyed learning about Reactive API development with an asynchronous, non-blocking, and functional paradigm. At first glance, you may find it complicated if you are not very familiar with the fluent and functional paradigm, but with practice, you'll start writing only functional-style code. Definitely, familiarity with Java Streams and functions gives you an edge to grasp the concepts easily.

Now that you have reached the end of the chapter, you have the skills to write functional and Reactive code. Now you can write Reactive, asynchronous, and non-blocking code and REST APIs. You also learned about R2DBC, which will become more solid and enhanced in the future as long as Reactive programming is there.

In the next chapter, we'll explore the security aspect of RESTful service development.

## Questions

- Do you really need the Reactive paradigm for application development?
- Are there any disadvantages to using the Reactive paradigm?
- Who plays the role of the subscriber in the case of an HTTP request in Spring WebFlux?

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
