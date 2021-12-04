# Project Reactor - 반응형 앱의 토대

In the previous chapter, we looked at an overview of the Reactive Streams specification and the way it augments reactive libraries by offering common interfaces and a new pull-push model for data exchange.

In this chapter, we are going to dive into Project Reactor, the most famous library in the reactive landscape, which has already become a vital part of the Spring Framework ecosystem. We will explore the most essential, frequently used Project Reactor APIs. The library is so versatile and feature-rich that it deserves a separate book of its own, it being impossible to cover its entire API in one chapter. We will look under the hood of the Reactor library and use its power to build a reactive application.

In this chapter, we are going to cover the following topics:

- The history and motivation behind Project Reactor
- Project Reactor's terminology and API
- Advanced features of Project Reactor
- The most crucial implementation details of Project Reactor
- A comparison of the most frequently used reactive types
- A business case implemented with the Reactor library


## 프로젝트 리액터의 간략한 역사

`Reactive Streams` 명세는 반응형 라이브러리를 서로 호환 가능하게 만들고 pull-push 데이터 교환 모델을 도입하여 백프레셔 문제를 해결했습니다. Reactive Streams 명세에 의해 도입된 상당한 개선에도 불구하고 여전히 API와 규칙만 정의하고 일상적인 사용을 위한 라이브러리는 제공하지 않습니다. 이 장에서는 Reactive Streams 사양의 가장 널리 사용되는 구현 중 하나인 **Project Reactor**(또는 줄여서 Reactor)를 다룹니다. 그러나 Reactor 라이브러리는 초기 버전 이후 많이 발전하여 현재 가장 최신의 반응형 라이브러리가 되었습니다. Reactive Streams 사양이 API 및 라이브러리의 구현 세부 사항을 어떻게 형성했는지 알아보기 위해 이력을 살펴보겠습니다.


## Project Reactor version 1.x

Reactive Streams 사양에서 작업할 때 Spring Framework 팀의 개발자는 특히 Spring XD 프로젝트의 경우 대용량 데이터 처리 프레임워크가 필요했습니다. 그 목표는 빅 데이터 애플리케이션의 개발을 단순화하는 것이었습니다. 이러한 요구를 충족하기 위해 Spring 팀은 새로운 프로젝트를 시작했습니다. 처음부터 비동기식 비차단 처리를 지원하도록 설계되었습니다. 팀은 그것을 `Project Rector`라고 불렀습니다. 기본적으로 Reactor 버전 1.x는 Reactor Pattern과 같은 메시지 처리 모범 사례와 기능 및 반응형 프로그래밍 스타일을 통합했습니다.

Reactor Pattern은 비동기 이벤트 처리 및 동기 처리를 돕는 행동 패턴입니다. 이는 모든 이벤트가 대기열에 추가되고 이벤트의 실제 처리가 나중에 별도의 스레드에서 발생함을 의미합니다. 이벤트는 모든 이해 당사자(이벤트 핸들러)에게 전달되고 동기적으로 처리됩니다. Reactor Pattern에 대해 자세히 알아보려면 다음 링크를 방문하십시오. http://www.dre.vanderbilt.edu/~schmidt/PDF/reactor-siemens.pdf.

이러한 기술을 수용함으로써 Project Reactor 버전 1.x는 다음과 같은 간결한 코드를 작성할 수 있는 기능을 제공합니다.

```java
Environment env = new Environment(); // (1)
Reactor reactor = Reactors.reactor() // (2)
                          .env(env)
                          .dispatcher(Environment.RING_BUFFER)     // (2.1)
                          .get(); 

reactor.on($("channel"), // (3)
           event -> System.out.println(event.getData())); // 

Executors.newSingleThreadScheduledExecutor() // (4)
         .scheduleAtFixedRate(
             () -> reactor.notify("channel", Event.wrap("test")),
             0, 100, TimeUnit.MILLISECONDS
         );
```

In the preceding code, there are a couple of conceptual points:

(1) Environment 인스턴스를 만듭니다. 이 인스턴스는 특정 디스패처 생성을 담당하는 실행 컨텍스트입니다. 이것은 잠재적으로 프로세스 간 디스패처에서 분산 디스패처에 이르기까지 다양한 종류의 디스패처를 제공할 수 있습니다.

(2) `Reactor` 패턴을 직접 구현한 Reactor의 인스턴스가 생성됩니다. 앞의 예제 코드에서는 구체적인 Reactor 인스턴스를 위한 유창한 빌더인 `Reactors` 클래스를 사용합니다. (2.1) 지점에서 RingBuffer 구조를 기반으로 하는 미리 정의된 Dispatcher 구현을 사용합니다. RingBuffer 기반 Dispatcher의 내부 및 전체 디자인에 대해 자세히 알아보려면 https://martinfowler.com/articles/lmax.html 링크를 방문하세요.

(3) 채널 선택기 및 이벤트 소비자의 선언이 있습니다. 이 시점에서 이벤트 핸들러(이 경우 수신된 모든 이벤트를 System.out에 출력하는 람다)를 등록합니다. 이벤트 필터링은 이벤트 채널의 이름을 나타내는 문자열 선택기를 사용하여 발생합니다. Selectors.$는 더 광범위한 기준 선택을 제공하므로 이벤트 선택에 대한 최종 표현식이 더 복잡할 수 있습니다.

(4) 여기에서는 예약된 작업의 형태로 Event의 생산자를 구성합니다. 그 시점에서 Java의 ScheduledExecutorService 가능성을 사용하여 이전에 인스턴스화된 Reactor 인스턴스의 특정 채널에 Event를 보내는 주기적 작업을 예약합니다.

내부적으로 이벤트는 Dispatcher에 의해 처리된 다음 대상 지점으로 전송됩니다. Dispatcher 구현에 따라 이벤트는 동기식 또는 비동기식으로 처리될 수 있습니다. 이것은 기능적 분해를 제공하고 일반적으로 Spring Framework 이벤트 처리 접근 방식과 유사한 방식으로 작동합니다. 또한 Reactor 1.x는 명확한 흐름으로 이벤트 처리를 구성할 수 있는 유용한 래퍼를 제공합니다.

```java
... // (1)
Stream<String> stream = Streams.on(reactor, $("channel")); // (2)
stream.map(s -> "Hello world " + s) // (3)
      .distinct()
      .filter((Predicate<String>) s -> s.length() > 2)
      .consume(System.out::println); // (3.1)

Deferred<String, Stream<String>> input = Streams.defer(env); // (4)

Stream<String> compose = input.compose() // (5)
compose.map(m -> m + " Hello World") // (6)
       .filter(m -> m.contains("1"))
       .map(Event::wrap)
       .consume(reactor.prepare("channel")); // (6.1)

for (int i = 0; i < 1000; i++) { // (7)
   input.accept(UUID.randomUUID().toString());
}
```

Let's break down the preceding code:

(1) 이전 예에서와 같이 환경과 Reactor 생성이 있습니다.

(2) Stream을 생성합니다. Stream을 사용하면 기능적 변환 체인을 구축할 수 있습니다. 지정된 Selector를 사용하여 Reactor에 Streams.on 메서드를 적용하면 지정된 Reactor 인스턴스의 지정된 채널에 연결된 Stream 객체를 받습니다.

(3) 여기에서 처리 흐름이 생성됩니다. map, filter, consume과 같은 몇 가지 중간 작업을 적용합니다. 마지막은 터미널 연산자(3.1)입니다.

(4) Deferred Stream을 생성합니다. Deferred 클래스는 스트림에 수동 이벤트를 제공할 수 있도록 하는 특수 래퍼입니다. Stream.defer 메소드는 Reactor 클래스의 추가 인스턴스를 생성합니다.

(5) Stream 인스턴스를 생성합니다. 여기에서 compose 메서드를 사용하여 Deferred 인스턴스에서 Stream을 검색합니다.

(6) 반응적 처리 흐름 생성이 있습니다. 파이프라인 구성의 이 부분은 (3)에서와 유사합니다. (6.1) 지점에서 `e -> reactor.notify("channel", e)`와 같이 코드에 Reactor API 단축키를 사용합니다.

(7) Deferred 인스턴스에 임의의 요소를 제공합니다.

앞의 예에서는 채널을 구독한 다음 들어오는 모든 이벤트를 단계별로 처리합니다. 대조적으로, 그 예에서 우리는 선언적 처리 흐름을 구축하기 위해 반응형 프로그래밍 기술을 사용합니다. 여기에서는 두 가지 별도의 처리 단계를 제공합니다. 또한 코드는 잘 알려진 RxJava API처럼 보이기 때문에 RxJava 사용자에게 더 친숙합니다. 어느 시점에서 Reactor 1.x는 Spring Framework와 잘 통합되었습니다. 메시지 처리 라이브러리와 함께 Reactor 1.x는 Netty용 추가 기능과 같은 많은 추가 기능을 제공합니다.

요약하자면 당시 Reactor 1.x는 이벤트를 고속으로 처리하기에 충분했습니다. Spring Framework와의 뛰어난 통합 및 Netty와의 구성으로 비동기 및 비차단 메시지 처리를 제공하는 고성능 시스템 개발을 가능하게 했습니다.

그러나 Reactor 1.x에도 단점이 있습니다. 우선, 라이브러리에는 배압 제어가 없습니다. 불행히도 Reactor 1.x의 이벤트 기반 구현은 생산자 스레드를 차단하거나 이벤트를 건너뛰는 것 외에는 역압을 제어하는 ​​방법을 제공하지 않았습니다. 또한 오류 처리가 상당히 복잡했습니다. Reactor 1.x는 오류 및 실패를 처리하는 여러 방법을 제공합니다. Reactor 1.x는 가장자리가 거칠었지만 인기 있는 Grails 웹 프레임워크에서 사용되었습니다. 물론 이것은 반응형 라이브러리의 다음 반복에 큰 영향을 미쳤습니다.

## `Project Reactor` 2.x

Reactor 1.x의 첫 공식 출시 후 얼마 지나지 않아 Stephane Maldini는 Reactive Streams Special Interest Group에 고성능 메시지 처리 시스템 전문가이자 프로젝트 Reactor 공동 리더로 초대되었습니다. 이 그룹은 추측할 수 있듯이 Reactive Streams 사양에서 작업했습니다. Reactive Streams의 특성을 더 잘 이해하고 Rector 팀에 새로운 지식을 소개한 후 Stephane Maldini와 Jon Brisbin은 2015년 초에 Reactor 2.x를 발표했습니다. Stephane Maldini를 인용하여: "Reactor 2는 Reactive Streams의 첫 번째 장면이었습니다."

Reactor 디자인의 가장 중요한 변경 사항은 EventBus 및 Stream 기능을 별도의 모듈로 추출한 것입니다. 또한 전면적인 재설계를 통해 새로운 Reactor Streams 라이브러리가 Reactive Streams 사양을 완전히 준수할 수 있었습니다. Reactor 팀은 Reactor의 API를 획기적으로 개선했습니다. 예를 들어, 새로운 Reactor API는 Java Collections API와 더 잘 통합되었습니다.

두 번째 버전에서는 Reactor의 Streams API가 RxJava API와 훨씬 더 유사해졌습니다. 스트림을 만들고 사용하기 위한 간단한 추가 기능과 함께 다음과 같이 백프레셔 관리, 스레드 디스패치 및 복원력 지원을 위한 유용한 추가 기능이 많이 추가되었습니다.

```java
stream
   .retry() // (1)
   .onOverflowBuffer() // (2)
   .onOverflowDrop() //
   .dispatchOn(new RingBufferDispatcher("test")) // (3)
```

이전 예에서는 세 가지 간단한 기술을 보여줍니다.

1. 한줄 연산자 retry()를 통해 흐름에 탄력성을 도입하고 오류가 발생할 경우 업스트림 작업을 다시 실행해야 합니다.

2. `onOverflowBuffer()` 및 `onOverflowDrop()` 메소드로 게시자가 푸시 모델만 지원하고 소비자 요구에 의해 제어할 수 없는 경우에 대한 역압 제어를 추가합니다.

3. 동시에 `dispatchOn()` 연산자를 적용하여 해당 스트림에서 작업할 새 `Dispatcher`를 전용했습니다. 이렇게 하면 메시지를 비동기적으로 처리하는 기능이 유지됩니다.

Reactor EventBus도 개선되었습니다. 먼저 메시지 전송을 담당하는 Reactor 객체의 이름이 EventBus로 변경되었습니다. 모듈은 또한 Reactive Streams 사양을 지원하도록 재설계되었습니다.

그 무렵 스테판 말디니는 그의 논문 "고해상도 및 투명한 생산 인포매틱스" 작업을 하고 있던 데이비드 카녹을 만났습니다. 이 논문은 Reactive Streams, Reactive Programming 및 RxJava 분야에 대한 심층 연구를 보고합니다. Maldini와 Karnok은 긴밀한 협력을 통해 RxJava 및 Project Reactor에 대한 아이디어와 경험을 `react-stream-commons`라는 라이브러리에 압축했습니다. 잠시 후 이 라이브러리는 Reactor 2.5, 그리고 마침내 Reactor 3.x의 기반이 되었습니다.

react-streams-commons 라이브러리의 소스는 다음 GitHub 프로젝트 페이지(https://github.com/reactor/reactive-streams-commons)에서 탐색할 수 있습니다.

1년의 노력 끝에 Reactor 3.0이 출시되었습니다. 동시에 거의 동일한 RxJava 2.0이 등장했습니다. 후자는 이전 버전인 RxJava 1.x보다 Reactor 3.x와 더 유사합니다. 이러한 라이브러리 간의 가장 눈에 띄는 차이점은 RxJava가 Java 6(Android 지원 포함)을 대상으로 하는 반면 Reactor 3은 Java 8을 기준선으로 선택했다는 사실입니다. 한편 Reactor 3.x는 Spring Framework 5의 반응적 변형을 형성했습니다. 이것이 이 책의 나머지 장에서 Project Reactor를 광범위하게 사용하는 이유입니다. 이제 Project Reactor 3.x의 API와 이를 효과적으로 사용하는 방법에 대해 알아보겠습니다.

## 프로젝트 리액터 필수품

Reactor 라이브러리는 처음부터 비동기 파이프라인을 구축할 때 콜백 지옥과 깊이 중첩된 코드를 생략하는 것을 목표로 설계되었습니다. 이러한 현상과 이로 인한 난해함을 1장, 왜 Reactive Spring인가에서 설명했습니다. 선형 코드를 찾는 과정에서 라이브러리 작성자는 조립 라인을 사용하여 비유를 공식화했습니다.

라이브러리의 주요 목표는 코드의 가독성을 개선하고 Reactor 라이브러리로 정의된 워크플로에 조립 가능성을 도입하는 것입니다. Public API는 높은 수준이지만 매우 다재다능함과 동시에 성능을 희생하지 않도록 설계되었습니다. API는 "네이키드" Reactive Streams 사양보다 가장 큰 부가 가치를 제공하는 풍부한 연산자 세트(어셈블리 비유의 "워크스테이션")를 제공합니다.

Reactor API는 복잡하고 잠재적으로 재사용 가능한 실행 그래프를 구축할 수 있도록 하는 연산자 연결을 권장합니다. 이러한 실행 그래프는 실행 흐름만 정의하지만 구독자가 실제로 구독을 생성할 때까지는 아무 일도 일어나지 않으므로 구독만 실제 데이터 흐름을 트리거한다는 점에 유의하는 것이 중요합니다.

라이브러리는 잠재적으로 결함이 있는 I/O가 있는 비동기 요청의 결과로 로컬 및 검색된 데이터를 효율적으로 조작할 수 있도록 설계되었습니다. 이것이 Project Reactor의 오류 처리 연산자가 매우 다재다능한 이유이며 나중에 보게 되겠지만 탄력적인 코드 작성을 권장합니다.

backpressure는 Reactive Streams 사양이 리액티브 라이브러리를 갖도록 권장하는 필수 속성이며 Reactor가 사양을 구현하기 때문에 backpressure는 Reactor 자체의 중심 테마입니다. 따라서 Reactor로 구축된 Reactive Streams에 대해 이야기할 때 데이터는 게시자에서 구독자로 다운스트림으로 이동합니다. 동시에 구독 및 수요 제어 신호는 구독자에서 게시자로 업스트림으로 전파됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/6b5be4bb-a857-4469-8cbe-e6c167d010d7.png)

다이어그램 4.1 반응형 스트림을 통한 데이터 흐름 및 구독/요구 신호 전파

라이브러리는 다음과 같이 백프레셔 전파의 모든 공통 모드를 지원합니다.

- push 전용: 구독자가 `subscription.request(Long.MAX_VALUE)`를 사용하여 사실상 무한한 수의 요소를 요청할 때
- pull 전용: 구독자가 이전 요소를 수신한 후에만 다음 요소를 요청할 때: subscription.request
- pull-push(혼합): 구독자가 수요를 실시간으로 제어하고 게시자가 발표된 데이터 소비 속도에 적응할 수 있는 경우.

또한 pull-push 작업 모델을 지원하지 않는 이전 API를 적용할 때 Reactor는 버퍼링, 윈도우, 메시지 삭제, 예외 시작 등과 같은 많은 구식 백프레셔 메커니즘을 제공합니다. 이 모든 기술은 이 장의 뒷부분에서 다룹니다. 경우에 따라 앞서 언급한 전략을 사용하면 실제 수요가 나타나기 전에도 데이터를 미리 가져올 수 있어 시스템의 응답성이 향상됩니다. 또한 Reactor API는 사용자 활동의 짧은 피크를 완화하고 시스템 과부하를 방지하기에 충분한 도구를 제공합니다.

`Project Reactor`는 동시성에 구애받지 않도록 설계되었으므로 동시성 모델을 적용하지 않습니다. 동시에 거의 모든 방식으로 실행 스레드를 관리할 수 있는 유용한 스케줄러 세트를 제공하며, 제안된 스케줄러 중 어느 것도 요구 사항에 맞지 않는 경우 개발자는 완전한 저수준 제어로 자체 스케줄러를 작성할 수 있습니다. 

Reactor 라이브러리의 스레드 관리도 이 장의 뒷부분에서 다룹니다.
이제 Reactor 라이브러리에 대한 간략한 개요를 살펴본 후 프로젝트에 추가하고 도달 API 조사를 시작하겠습니다.

## 프로젝트에 Reactor 추가

여기에서는 독자가 이미 Reactive Streams 사양에 익숙하다고 가정합니다. 그렇지 않은 경우 이전 장에서 간략하게 설명합니다. Reactive Streams 사양은 현재 컨텍스트에서 필수적입니다. Project Reactor가 그 위에 구축되고 `org.reactivestreams:reactive-streams`가 Project Reactor의 유일한 필수 종속성이기 때문입니다.

프로젝트 리액터를 애플리케이션에 종속성으로 추가하는 것은 `build.gradle` 파일에 다음 종속성을 추가하는 것만큼 간단합니다.
```
compile("io.projectreactor:reactor-core:3.2.0.RELEASE")
```

작성 당시 라이브러리의 최신 버전은 3.2.0.RELEASE입니다. 이 버전은 Spring Framework 5.1에서도 사용됩니다.

Maven 프로젝트에 Project Reactor 라이브러리를 추가하는 절차는 https://projectreactor.io/docs/core/3.2.0.RELEASE/reference/#_maven_installation 문서에 설명되어 있습니다.

또한 다음 종속성을 추가하는 것이 종종 가치가 있습니다. 이는 반응형 코드를 테스트하는 데 필요한 도구 집합을 제공하기 때문에 분명히 단위 테스트로 다루어야 합니다.
```
testCompile("io.projectreactor:reactor-test:3.2.0.RELEASE")
```
이 장에서는 Reactive Streams에 대한 몇 가지 간단한 테스트 기술을 사용할 것입니다. 또한 9장, 반응형 애플리케이션 테스트에서는 반응형 코드 테스트와 관련된 주제를 더 자세히 다룹니다.

이제 애플리케이션 클래스 경로에 Reactor가 있으므로 Reactor의 반응 유형 및 연산자를 실험할 준비가 되었습니다.

## Reactive types – Flux and Mono

As we already know, the Reactive Streams specification defines only four interfaces: Publisher<T>, Subscriber<T>, Subscription, and Processor<T, R>. More or less, we are going to follow this list and look at the interface implementations provided by the library.

First of all, Project Reactor provides two implementations of the Publisher<T> interface: Flux<T> and Mono<T>. Such an approach adds additional contextual meaning to reactive types.  Here, to investigate the behavior of reactive types (Flux and Mono), we are going to use some reactive operators without a detailed explanation of how these operators work. Operators are covered later in this chapter.

### Flux

Let's describe how data flows through the Flux class with the following marble diagram:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/c43203e0-01f9-4d13-8c66-23ed4c9ce7cd.png)

Diagram 4.2 An example of the Flux stream transformed into another Flux stream

Flux defines a usual reactive stream that can produce zero, one, or many elements; even potentially an infinite amount of elements. It has the following formula:

```
onNext x 0..N [onError | onComplete]
```
It is not very common to work with infinite data containers in the imperative world, but it is pretty common with functional programming. The following code may produce a simple endless Reactive Stream:

```java
Flux.range(1, 5).repeat()
```
This stream repeatedly produces numbers from 1 to 5 (the sequence would look like — 1, 2, 3, 4, 5, 1, 2,...). This is not a problem and it will not blow up the memory as each element can be transformed and consumed without the need to finish creating the whole stream. Furthermore, the subscriber can cancel the subscription at any time and effectively transform an endless stream into a finite stream.

Beware: an attempt to collect all elements emitted by an endless stream may cause an OutOfMemoryException. It is not recommended to do so in production applications, but the simplest way to reproduce such behavior may be with the following code:

```java
Flux.range(1, 100)                                                  // (1)
   .repeat()                                                        // (2)
   .collectList()                                                   // (3)
   .block();                                                        // (4)
```
In the preceding code, we do the following:

1. The range operator creates a sequence of integers starting from 1 up to 100 (inclusive).

2. The repeat operator subscribes to the source reactive stream again and again after the source stream finishes. So, the repeat operator subscribes to the results of the stream operator, receives elements 1 to 100 and the onComplete signal, and then subscribes again, receives elements 1 to 100, and so on, without stopping.

3. With the collectList operator, we are trying to gather all produced elements into a single list. Of course, because the repeat operator generates an endless stream, elements arrive and increase the size of the list so it consumes all the memory and causes the application to fail with the following error—java.lang.OutOfMemoryError: Java heap space. Our application has just run out of free heap memory.

4. The block operator triggers an actual subscription and blocks the running thread until the final result arrives, which, in the current case, cannot happen as the reactive stream is endless.

### Mono
Now, let's look at how the Mono type is different from the Flux type:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/cc4aed1c-8812-46a2-9433-5d6626297018.png)
Diagram 4.3 An example of the Mono stream transformed into another Mono stream

In contrast with Flux, the Mono type defines a stream that can produce at most one element and can be described by the following formula:
```java
onNext x 0..1 [onError | onComplete]
```
The distinction between Flux and Mono allows us to not only introduce additional meaning to the method signatures, but also enables more efficient internal implementation of Mono due to skipping redundant buffers and costly synchronizations.

Mono<T> may be useful in cases when an application API returns one element at most. Consequently, it can easily replace CompletableFuture<T>, giving a pretty similar semantic. Of course, these two types have some small semantic differences—CompletableFuture, unlike Mono, cannot complete normally without emitting a value. Also, CompletableFuture starts processing immediately, while Mono does nothing until a subscriber appears. The benefit of the Mono type lies in providing plenty of reactive operators and the ability to be flawlessly incorporated into a bigger reactive workflow.

Also, Mono can be used when it is required to notify a client about a finished action. In such cases, we may return the Mono<Void>  type and signal onComplete() when processing is done or onError() in the event of failure. In such a scenario, we don't return any data but signal a notification, which in turn may be used as a trigger for further computation.

Mono and Flux are not detached types and can easily be "transformed" into each other. For example, Flux<T>.collectList() returns Mono<List<T>> and Mono<T>.flux() returns Flux<T>. In addition, the library is smart enough to optimize some transformations that do not change the semantic. For example, let's consider the following transformation (Mono -> Flux -> Mono):
```java
Mono.from(Flux.from(mono))
```
When calling the preceding code, it returns the original mono instance, as this is conceptually a no-ops conversion.