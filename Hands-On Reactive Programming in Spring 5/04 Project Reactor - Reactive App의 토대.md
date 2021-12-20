# Project Reactor - 반응형 앱의 토대

In the previous chapter, we looked at an overview of the Reactive Streams specification and the way it augments reactive libraries by offering common interfaces and a new pull-push model for data exchange.

In this chapter, we are going to dive into Project Reactor, the most famous library in the reactive landscape, which has already become a vital part of the Spring Framework ecosystem. We will explore the most essential, frequently used Project Reactor APIs. The library is so versatile and feature-rich that it deserves a separate book of its own, it being impossible to cover its entire API in one chapter. We will look under the hood of the Reactor library and use its power to build a reactive application.

In this chapter, we are going to cover the following topics:

- Project Reactor의 역사와 동기
- Project Reactor의 용어와 API
- Project Reactor의 고급 기능
- Project Reactor의 가장 핵심적 구현 상세
- 일반적으로 사용되는 리액티 타입의 비교
- 비즈니스 구현 사례


## 프로젝트 리액터의 간략한 역사

`Reactive Streams` 명세는 반응형 라이브러리를 서로 호환이 가능하도록 pull-push 데이터 교환 모델을 도입하여 백프레셔 문제를 해결했습니다. 이 명세로 도입된 상당한 개선에도 불구하고 여전히 API와 규칙만 정의하고 일상적인 사용을 위한 라이브러리는 제공하지 않습니다. 

이 장에서는 `Reactive Streams` 사양의 가장 일반적인 구현 중 하나인 **Project Reactor**(또는 줄여서 Reactor)를 다룹니다. 그러나 Reactor 라이브러리는 초기 버전 이후 많이 발전하여 현재 가장 최신의 반응형 라이브러리가 되었습니다. `Reactive Streams` 사양이 API 및 라이브러리의 구현 세부 사항을 어떻게 형성했는지 알아보기 위해 이력을 살펴보겠습니다.

### Project Reactor version 1.x

`Reactive Streams` 사양에서 작업할 때 Spring Framework 팀의 개발자는 특히 Spring XD 프로젝트의 경우 대용량 데이터 처리 프레임워크가 필요했습니다. 그 목표는 빅 데이터 애플리케이션의 개발을 단순화하는 것이었습니다. 이러한 요구를 충족하기 위해 Spring 팀은 새로운 프로젝트를 시작했습니다. 처음부터 비동기식 비차단 처리를 지원하도록 설계되었습니다. 팀은 그것을 `Project Rector`라고 불렀습니다. 기본적으로 Reactor 버전 1.x는 Reactor 패턴과 같은 메시지 처리 모범 사례와 기능 및 반응형 프로그래밍 스타일을 통합했습니다.

Reactor 패턴은 비동기 이벤트 처리 및 동기 처리를 돕는 행동 패턴입니다. 이는 모든 이벤트가 대기열에 추가되고 이벤트의 실제 처리가 나중에 별도의 스레드에서 발생함을 의미합니다. 이벤트는 모든 이해 당사자(이벤트 핸들러)에게 전달되고 동기적으로 처리됩니다. Reactor 패턴에 대해 자세히 알아보려면 다음 링크를 방문하십시오. http://www.dre.vanderbilt.edu/~schmidt/PDF/reactor-siemens.pdf.

이러한 기술을 수용함으로써 `Project Reactor 버전 1.x`는 다음 코드를 작성할 수 있는 기능을 제공합니다.

```java
Environment env = new Environment(); // (1)
Reactor reactor = Reactors.reactor() // (2)
            .env(env)
            .dispatcher(Environment.RING_BUFFER) // (2.1)
            .get(); 

reactor.on($("channel"), // (3)
           event -> System.out.println(event.getData())); // 

Executors.newSingleThreadScheduledExecutor() // (4)
         .scheduleAtFixedRate(
             () -> reactor.notify("channel", Event.wrap("test")),
             0, 100, TimeUnit.MILLISECONDS
         );
```

(1) Environment는 특정 디스패처 생성을 담당하는 실행 컨텍스트입니다. 이것은 잠재적으로 프로세스 간 디스패처에서 분산 디스패처에 이르기까지 다양한 종류의 디스패처를 제공할 수 있습니다.

(2) `Reactor 패턴`을 직접 구현한 Reactor의 인스턴스가 생성됩니다. (2.1) 지점에서 RingBuffer 구조를 기반으로 하는 Dispatcher 구현을 사용합니다. RingBuffer 기반 Dispatcher의 내부 및 전체 디자인에 대해 자세히 알아보려면 https://martinfowler.com/articles/lmax.html 링크를 방문하세요.

(3) 채널 선택기 및 이벤트 컨슈머를 선언합니다. 이 시점에서 이벤트 핸들러를 등록합니다. 이벤트 필터링은 이벤트 채널의 이름을 나타내는 문자열 선택기를 사용하여 발생합니다. Selectors.$는 더 광범위한 기준 선택을 제공하므로 이벤트 선택에 대한 최종 표현식이 더 복잡할 수 있습니다.

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

(1) 이전 예에서와 같이 Environment와 Reactor 생성합니다.

(2) Stream을 사용하면 함수형 변환 체인을 구축할 수 있습니다. 지정된 Selector를 사용하여 Reactor에 Streams.on 메서드를 적용하면 지정된 Reactor 인스턴스의 지정된 채널에 연결된 Stream 객체를 받습니다.

(3) 여기에서 처리 흐름이 생성됩니다. map, filter, consume과 같은 몇 가지 중간 작업을 적용합니다. 마지막은 터미널 연산자(3.1)입니다.

(4) Deferred Stream을 생성합니다. Deferred 클래스는 스트림에 수동 이벤트를 제공할 수 있도록 하는 특수 래퍼입니다. Stream.defer 메소드는 Reactor 클래스의 추가 인스턴스를 생성합니다.

(5) Stream 인스턴스를 생성합니다. 여기에서 compose 메서드를 사용하여 Deferred 인스턴스에서 Stream을 검색합니다.

(6) 반응적 처리 흐름 생성이 있습니다. 파이프라인 구성의 이 부분은 (3)에서와 유사합니다. (6.1) 지점에서 `e -> reactor.notify("channel", e)`와 같이 코드에 Reactor API 단축키를 사용합니다.

(7) Deferred 인스턴스에 임의의 요소를 제공합니다.

앞의 예에서는 채널을 구독한 다음 들어오는 모든 이벤트를 단계별로 처리합니다. 대조적으로, 그 예에서 우리는 선언적 처리 흐름을 구축하기 위해 반응형 프로그래밍 기술을 사용합니다. 여기에서는 두 가지 별도의 처리 단계를 제공합니다. 또한 코드는 잘 알려진 RxJava API처럼 보이기 때문에 RxJava 사용자에게 더 친숙합니다. 어느 시점에서 Reactor 1.x는 Spring Framework와 잘 통합되었습니다. 메시지 처리 라이브러리와 함께 Reactor 1.x는 Netty용 추가 기능과 같은 많은 추가 기능을 제공합니다.

요약하자면 당시 Reactor 1.x는 이벤트를 고속으로 처리하기에 충분했습니다. Spring Framework와의 뛰어난 통합 및 Netty와의 구성으로 비동기 및 비차단 메시지 처리를 제공하는 고성능 시스템 개발이 가능합니다.

그러나 Reactor 1.x에도 단점이 있습니다. 우선, 라이브러리에는 백프레셔 제어가 없습니다. 불행히도 Reactor 1.x의 이벤트 기반 구현은 생산자 스레드를 차단하거나 이벤트를 건너뛰는 것 외에는 백프레셔를 제어하는 ​​방법을 제공하지 않았습니다. 또한 오류 처리가 상당히 복잡했습니다. Reactor 1.x는 오류 및 실패를 처리하는 여러 방법을 제공합니다. Reactor 1.x는 가장자리가 거칠었지만 인기 있는 Grails 웹 프레임워크에서 사용되었습니다. 물론 이것은 반응형 라이브러리의 다음 이터레이션에 큰 영향을 미쳤습니다.

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

(1) retry()는 흐름에 탄력성을 도입하고 오류가 발생할 경우 업스트림 작업을 다시 실행해야 합니다.

(2) `onOverflowBuffer()` 및 `onOverflowDrop()` 메소드로 게시자가 푸시 모델만 지원하고 소비자 요구에 의해 제어할 수 없는 경우에 대한 백프레셔 제어를 추가합니다.

(3) 동시에 `dispatchOn()` 연산자를 적용하여 해당 스트림에서 작업할 새 `Dispatcher`를 전용했습니다. 이렇게 하면 메시지를 비동기적으로 처리하는 기능이 유지됩니다.

Reactor EventBus도 개선되었습니다. 먼저 메시지 전송을 담당하는 Reactor 객체의 이름이 EventBus로 변경되었습니다. 모듈은 또한 Reactive Streams 사양을 지원하도록 재설계되었습니다.

그 무렵 스테판 말디니는 그의 논문 "고해상도 및 투명한 생산 인포매틱스" 작업을 하고 있던 데이비드 카녹을 만났습니다. 이 논문은 Reactive Streams, Reactive Programming 및 RxJava 분야에 대한 심층 연구를 보고합니다. Maldini와 Karnok은 긴밀한 협력을 통해 RxJava 및 Project Reactor에 대한 아이디어와 경험을 `react-stream-commons`라는 라이브러리에 압축했습니다. 잠시 후 이 라이브러리는 Reactor 2.5, 그리고 마침내 Reactor 3.x의 기반이 되었습니다.

react-streams-commons 라이브러리의 소스는 다음 GitHub 프로젝트 페이지(https://github.com/reactor/reactive-streams-commons)에서 탐색할 수 있습니다.

1년의 노력 끝에 `Reactor 3.0`이 출시되었습니다. 동시에 거의 동일한 RxJava 2.0이 등장했습니다. 후자는 이전 버전인 RxJava 1.x보다 Reactor 3.x와 더 유사합니다. 이러한 라이브러리 간의 가장 눈에 띄는 차이점은 RxJava가 Java 6(Android 지원 포함)을 대상으로 하는 반면 Reactor 3은 Java 8을 기준선으로 선택했다는 사실입니다. 한편 Reactor 3.x는 Spring Framework 5의 반응적 변형을 형성했습니다. 이것이 이 책의 나머지 장에서 Project Reactor를 광범위하게 사용하는 이유입니다. 이제 `Project Reactor 3.x`의 API와 이를 효과적으로 사용하는 방법에 대해 알아보겠습니다.

## Project Reactor 핵심

Reactor 라이브러리는 처음부터 비동기 파이프라인을 구축할 때 콜백 지옥과 깊이 중첩된 코드를 생략하는 것을 목표로 설계되었습니다. 이러한 현상과 이로 인한 난해함을 1장, 왜 Reactive Spring인가에서 설명했습니다. 선형 코드를 찾는 과정에서 라이브러리 작성자는 조립 라인을 사용하여 비유를 공식화했습니다.

라이브러리의 주요 목표는 코드의 가독성을 개선하고 Reactor 라이브러리로 정의된 워크플로에 조립 가능성을 도입하는 것입니다. Public API는 높은 수준이지만 매우 다재다능함과 동시에 성능을 희생하지 않도록 설계되었습니다. API는 "네이키드" Reactive Streams 사양보다 가장 큰 부가 가치를 제공하는 풍부한 연산자 세트(어셈블리 비유의 "워크스테이션")를 제공합니다.

Reactor API는 복잡하고 잠재적으로 재사용 가능한 실행 그래프를 구축할 수 있도록 하는 연산자 연결을 권장합니다. 이러한 실행 그래프는 실행 흐름만 정의하지만 구독자가 실제로 구독을 생성할 때까지는 아무 일도 일어나지 않으므로 구독만 실제 데이터 흐름을 트리거한다는 점에 유의하는 것이 중요합니다.

라이브러리는 잠재적으로 결함이 있는 I/O가 있는 비동기 요청의 결과로 로컬 및 검색된 데이터를 효율적으로 조작할 수 있도록 설계되었습니다. 이것이 Project Reactor의 오류 처리 연산자가 매우 다재다능한 이유이며 나중에 보게 되겠지만 탄력적인 코드 작성을 권장합니다.

`백프레셔`는 Reactive Streams 사양이 리액티브 라이브러리를 갖도록 권장하는 필수 속성이며 Reactor가 사양을 구현하기 때문에 **백프레셔**는 Reactor 자체의 중심 테마입니다. 따라서 Reactor로 구축된 Reactive Streams에 대해 이야기할 때 데이터는 게시자에서 구독자로 다운스트림으로 이동합니다. 동시에 구독 및 수요 제어 신호는 구독자에서 게시자로 업스트림으로 전파됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/6b5be4bb-a857-4469-8cbe-e6c167d010d7.png)

다이어그램 4.1 반응형 스트림을 통한 데이터 흐름 및 구독/요구 신호 전파

라이브러리는 다음과 같이 백프레셔 전파의 모든 공통 모드를 지원합니다.

- 푸시 전용: 구독자가 `subscription.request(Long.MAX_VALUE)`를 사용하여 사실상 무한한 수의 요소를 요청할 때
- 풀 전용: 구독자가 이전 요소를 수신한 후에만 다음 요소를 요청할 때: subscription.request
- 푸시-풀(혼합): 구독자가 수요를 실시간으로 제어하고 게시자가 발표된 데이터 소비 속도에 적응할 수 있는 경우.

또한 `풀-푸시` 모델을 지원하지 않는 이전 API를 적용할 때 Reactor는 버퍼링, 윈도우, 메시지 삭제, 예외 시작 등과 같은 많은 구식 백프레셔 메커니즘을 제공합니다. 이 모든 기술은 이 장의 뒷부분에서 다룹니다. 경우에 따라 앞서 언급한 전략을 사용하면 실제 수요가 나타나기 전에도 데이터를 미리 가져올 수 있어 시스템의 응답성이 향상됩니다. 또한 Reactor API는 사용자 활동의 짧은 피크를 완화하고 시스템 과부하를 방지하기에 충분한 도구를 제공합니다.

`Project Reactor`는 동시성에 구애받지 않도록 설계되었으므로 동시성 모델을 적용하지 않습니다. 동시에 거의 모든 방식으로 실행 스레드를 관리할 수 있는 유용한 스케줄러 세트를 제공하며, 제안된 스케줄러 중 어느 것도 요구 사항에 맞지 않는 경우 개발자는 완전한 저수준 제어로 자체 스케줄러를 작성할 수 있습니다. 

## 프로젝트에 Reactor 추가

여기에서는 독자가 이미 Reactive Streams 사양에 익숙하다고 가정합니다. Reactive Streams 사양은 현재 컨텍스트에서 필수입니다. Project Reactor가 그 위에 구축되고 `org.reactivestreams:reactive-streams`가 Project Reactor의 유일한 필수 의존성입니다.

`Project Reactor`를 애플리케이션에 의존성으로 추가하는 것은 `build.gradle` 파일에 다음 의존성을 추가하는 것만큼 간단합니다.
```
compile("io.projectreactor:reactor-core:3.2.0.RELEASE")
```

이 버전은 3.2.0.RELEASE이며 Spring Framework 5.1에서도 사용됩니다.

Maven 프로젝트에 Project Reactor 라이브러리를 추가하는 절차는 https://projectreactor.io/docs/core/3.2.0.RELEASE/reference/#_maven_installation 문서에 설명되어 있습니다.

또한 다음 의존성을 추가하는 것이 종종 가치가 있습니다. 이는 반응형 코드를 테스트하는 데 필요한 도구 집합을 제공하기 때문에 분명히 단위 테스트로 다루어야 합니다.
```
testCompile("io.projectreactor:reactor-test:3.2.0.RELEASE")
```
이 장에서는 Reactive Streams에 대한 몇 가지 간단한 테스트 기술을 사용할 것입니다. 또한 9장, 반응형 앱 테스트에서는 반응형 코드 테스트와 관련된 주제를 더 자세히 다룹니다.

이제 애플리케이션 클래스 경로에 Reactor가 있으므로 Reactor의 반응형 타입 및 연산자를 실험할 준비가 되었습니다.

## Reactive types – Flux and Mono

Reactive Streams 사양은 Publisher<T>, Subscriber<T>, Subscription 및 Processor<T, R>의 네 가지 인터페이스만 정의합니다. 다소간 이 목록을 따라 라이브러리에서 제공하는 인터페이스 구현을 살펴보겠습니다.

우선 `Project Reactor`는 Publisher<T> 인터페이스의 두 가지 구현인 Flux<T> 및 Mono<T>를 제공합니다. Flux 및 Mono의 동작을 조사하기 위해 이러한 연산자의 작동 방식에 대한 자세한 설명 없이 일부 반응형 연산자를 사용할 것입니다.

### Flux

Let's describe how data flows through the Flux class with the following marble diagram:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/c43203e0-01f9-4d13-8c66-23ed4c9ce7cd.png)

다이어그램 4.2 Flux 스트림이 다른 Flux 스트림으로 변환된 예

Flux는 0, 1 또는 무한의 항목을 생성할 수 있는 일반적인 반응형 스트림을 정의합니다. 다음 공식이 있습니다.

```
onNext x 0..N [onError | onComplete]
```
명령형 세계에서 무한 데이터 컨테이너로 작업하는 것은 그리 일반적이지 않지만 함수형 프로그래밍에서는 꽤 일반적입니다. 다음 코드는 간단한 끝없는 반응형 스트림을 생성할 수 있습니다.

```java
Flux.range(1, 5).repeat()
```
이 스트림은 1에서 5까지의 숫자를 반복적으로 생성합니다(시퀀스는 1, 2, 3, 4, 5, 1, 2,...). 이것은 문제가 되지 않으며 전체 스트림 생성을 마칠 필요 없이 각 요소를 변환하고 사용할 수 있으므로 메모리를 폭파하지 않습니다. 또한 가입자는 언제든지 구독을 취소하고 무한 스트림을 유한 스트림으로 효과적으로 변환할 수 있습니다.

> **주의** 무한 스트림에서 방출되는 모든 요소를 수집하려고 하면 `OutOfMemoryException`이 발생할 수 있습니다. 프로덕션 응용 프로그램에서는 그렇게 하지 않는 것이 좋지만 이러한 동작을 재현하는 가장 간단한 방법은 다음 코드를 사용하는 것입니다. 

```java
Flux.range(1, 100) // (1)
   .repeat() // (2)
   .collectList() // (3)
   .block(); // (4)
```
In the preceding code, we do the following:

(1) `range()`는 1에서 100(포함)까지의 정수 시퀀스를 생성합니다.

(2) `repeat()`는 소스 스트림이 완료된 후 소스 리액티브 스트림을 계속해서 구독합니다. 따라서 스트림 연산자의 결과를 구독하고 요소 1~100과 `onComplete` 신호를 수신한 다음 다시 구독하고 요소 1~100 등을 멈추지 않고 수신합니다.

(3) `collectList()`는 생성된 모든 요소를 ​​단일 목록으로 수집하려고 합니다. 물론 repeat 연산자는 무한 스트림을 생성하기 때문에 요소가 도착하고 목록의 크기를 늘려 모든 메모리를 소비하고 애플리케이션이 다음 오류와 함께 실패하게 합니다. `java.lang.OutOfMemoryError: Java heap space`.

(4) 블록 연산자는 실제 구독을 트리거하고 최종 결과가 도착할 때까지 실행 중인 스레드를 차단합니다. 현재의 경우에는 반응 스트림이 무한하므로 발생할 수 없습니다.

### Mono

Now, let's look at how the Mono type is different from the Flux type:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/cc4aed1c-8812-46a2-9433-5d6626297018.png)
Diagram 4.3 An example of the Mono stream transformed into another Mono stream

Flux와 달리 Mono 유형은 최대 하나의 요소를 생성할 수 있는 스트림을 정의하고 다음 공식으로 설명할 수 있습니다.

```java
onNext x 0..1 [onError | onComplete]
```

Flux와 Mono를 구분하면 메서드 서명에 추가 의미를 도입할 수 있을 뿐만 아니라 중복 버퍼를 건너뛰고 비용이 많이 드는 동기화로 인해 Mono의 내부 구현을 보다 효율적으로 구현할 수 있습니다.

Mono<T>는 애플리케이션 API가 최대 하나의 요소를 반환하는 경우에 유용합니다. 결과적으로 CompletableFuture<T>를 쉽게 대체하여 매우 유사한 의미를 제공합니다. 물론 이 두 유형에는 약간의 의미론적 차이가 있습니다. Mono와 달리 CompletableFuture는 값을 내보내지 않고는 정상적으로 완료될 수 없습니다. 또한 CompletableFuture는 즉시 처리를 시작하지만 Mono는 구독자가 나타날 때까지 아무 작업도 수행하지 않습니다. Mono 유형의 이점은 많은 리액티브 오퍼레이터를 제공하고 더 큰 리액티브 워크플로에 완벽하게 통합할 수 있는 능력에 있습니다.

또한 Mono는 완료된 작업을 클라이언트에 알려야 할 때 사용할 수 있습니다. 이러한 경우 처리가 완료되면 `Mono<Void>` 유형을 반환하고 onComplete() 또는 실패 시 onError() 신호를 보낼 수 있습니다. 이러한 시나리오에서는 데이터를 반환하지 않고 알림을 보내 추가 계산을 위한 트리거로 사용할 수 있습니다.

Mono와 Flux는 분리형이 아니며 쉽게 서로 "변환"될 수 있습니다. 예를 들어, Flux<T>.collectList()는 Mono<List<T>>를 반환하고 Mono<T>.flux()는 Flux<T>를 반환합니다. 또한 라이브러리는 의미 체계를 변경하지 않는 일부 변환을 최적화할 만큼 충분히 똑똑합니다. 

예를 들어 다음 변환(Mono -> Flux -> Mono)을 고려해 보겠습니다.

```java
Mono.from(Flux.from(mono))
```
이전 코드를 호출하면 원래 Mono 인스턴스를 반환합니다.

### RxJava 2의 리액티브 타입

RxJava 2.x 라이브러리와 Project Reactor는 같은 기반을 가지고 있지만, RxJava 2는 다른 리액티브 Publisher 세트를 가지고 있습니다. 이 두 라이브러리가 동일한 아이디어를 구현하므로 RxJava 2가 최소한 반응형 유형과 관련하여 어떻게 다른지 설명할 가치가 있습니다. 

반응 연산자, 스레드 관리 및 오류 처리를 포함한 다른 모든 측면은 매우 유사합니다. 
따라서 라이브러리 중 하나에 다소 익숙하다는 것은 두 라이브러리에 모두 익숙하다는 것을 의미합니다.

"2장, Spring의 반응형 프로그래밍 - 기본 개념"에서 설명한 것처럼 RxJava 1.x에는 원래 하나의 반응형 유형인 Observable만 있었습니다. 나중에 Single 및 Completable 유형이 추가되었습니다. 버전 2에서 라이브러리에는 Observable, Flowable, Single, Maybe 및 Completable과 같은 반응 유형이 있습니다. 이들의 차이점을 간략하게 설명하고 Flux/Mono 탠덤과 비교하겠습니다.


### Observable

RxJava 2의 Observable 유형은 RxJava 1.x와 거의 동일한 의미 체계를 제공하지만 더 이상 null 값을 허용하지 않습니다. 또한 Observable은 역압을 지원하지 않으며 Publisher 인터페이스를 구현하지 않습니다. 따라서 Reactive Streams 사양과 직접 호환되지 않습니다. 따라서 많은 요소(수천 개 이상)가 있는 스트림에 사용할 때는 주의해야 합니다. 반면 Observable 유형은 Flowable 유형보다 오버헤드가 적습니다. 사용자가 선택한 배압 전략을 적용하여 스트림을 Flowable로 변환하는 toFlowable 메서드가 있습니다.

### Flowable

The Flowable type is a direct counterpart of Reactor's Flux type. It implements Reactive Streams' Publisher. Consequently, it may be easily used in reactive workflows implemented with Project Reactor, as a well-designed API would consume arguments of the Publisher type instead of a more library-specific Flux. 

### Single
The Single type represents streams that produce precisely one element. It does not inherit the Publisher interface. It also has the toFlowable method, which, in this case, does not require a backpressure strategy. Single better represents the semantics of CompletableFuture than Reactor's Mono type. However, it still does not start processing before subscription happens. 

### Maybe
To achieve the same semantics as Reactor's Mono type, RxJava 2.x provides the Maybe type. However, it is not Reactive Streams compliant, as Maybe does not implement the Publisher interface. It has the toFlowable method for that purpose.

### Completable

또한 RxJava 2.x에는 onError 또는 onComplete 신호만 트리거할 수 있지만 onNext 신호는 생성할 수 없는 Completable 유형이 있습니다. 게시자 인터페이스도 구현하지 않으며 toFlowable 메서드가 있습니다. 의미적으로는 onNext 신호도 생성할 수 없는 Mono<Void> 유형에 해당합니다.

요약하자면, RxJava 2는 반응 유형 간에 더 세분화된 의미론적 구별을 가지고 있습니다. Flowable 유형만 Reactive Streams 불만 사항입니다. Observable은 동일한 작업을 수행하지만 배압 지원이 없습니다. Maybe<T> 유형은 Reactor의 Mono<T>에 해당하고 RxJava의 Completable은 Reactor의 Mono<Void>에 해당합니다. Single 유형의 의미는 프로젝트 반응기 측면에서 직접 표현할 수 없습니다. 해당 유형 중 어느 것도 생성된 이벤트의 최소 수에 대해 보장하지 않기 때문입니다. 다른 Reactive Streams 불만 코드와 통합하려면 RxJava 유형을 Flowable 유형으로 변환해야 합니다.




## Flux and Mono 시퀀스 만들기

Flux and Mono provide many factory methods to create Reactive Streams based on data that is already available. For example, we may create Flux with object references or from a collection, or we may even create our own lazy range of numbers:

```java
Flux<String>  stream1 = Flux.just("Hello", "world");
Flux<Integer> stream2 = Flux.fromArray(new Integer[]{1, 2, 3});
Flux<Integer> stream3 = Flux.fromIterable(Arrays.asList(9, 8, 7));
```

It is easy to generate a stream of integers with the range method, where 2010 is a starting point, and 9 is the number of elements in the sequence:

```java
Flux<Integer> stream4 = Flux.range(2010, 9);
```
This is a handy way to generate a stream of recent years, so the preceding code generates the following stream of integers:
```
2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018
```
Mono provides similar factory methods, but mainly targets one element. It is also often used in conjunction with nullable and Optional types:

```java
Mono<String> stream5 = Mono.just("One");
Mono<String> stream6 = Mono.justOrEmpty(null);
Mono<String> stream7 = Mono.justOrEmpty(Optional.empty());
```

Mono는 HTTP 요청 또는 DB 쿼리와 같은 비동기 작업을 래핑하는 데 매우 유용할 수 있습니다. 이를 위해 Mono는 `fromCallable(Callable)`, `fromRunnable(Runnable)`, `fromSupplier(Supplier)`, `fromFuture(CompletableFuture)`, `fromCompletionStage(CompletionStage)` 등의 메서드를 제공합니다. 다음 코드 줄을 사용하여 Mono에서 긴 HTTP 요청을 래핑할 수 있습니다.


```java
Mono<String> stream8 = Mono.fromCallable(() -> httpRequest());
```

또는 자바8의 `메소드 참조 문법`으로 더 짧은 코드로 작성할 수 있습니다.
```java
Mono<String> stream8 = Mono.fromCallable(this::httpRequest);
```

앞의 코드는 HTTP 요청을 비동기식으로 만들 뿐만 아니라(적절한 스케줄러와 함께 제공됨) onError 시그널로 전파될 수 있는 오류도 처리합니다.

Flux와 Mono는 모두 `from(Publisher<T> p)` 팩토리 메서드를 사용하여 다른 `Publisher` 인스턴스에 적응할 수 있습니다.

두 타입 모두 편리하고 일반적으로 사용되는 빈 스트림과 오류만 포함하는 스트림을 만드는 방법이 있습니다.

```java
Flux<String> empty = Flux.empty();
Flux<String> never = Flux.never();
Mono<String> error = Mono.error(new RuntimeException("Unknown id"));
```

Flux와 Mono에는 각각 Flux 또는 Mono의 빈 인스턴스를 생성하는 `empty()`라는 팩토리 메서드가 있습니다. 마찬가지로 `never()` 메서드는 완료, 데이터 또는 오류 신호를 보내지 않는 스트림을 만듭니다.

`error(Throwable)` 팩토리 메서드는 구독할 때 각 구독자의 `onError(...)` 메서드를 통해 항상 오류를 전파하는 시퀀스를 만듭니다. Flux 또는 Mono 선언 중에 오류가 생성되고 결과적으로 각 구독자는 동일한 `Throwable` 인스턴스를 받습니다.

`defer` 팩토리 메소드는 구독 시점에 동작을 결정하는 시퀀스를 생성하고 결과적으로 다른 구독자에 대해 다른 데이터를 생성할 수 있습니다.

```java
Mono<User> requestUserData(String sessionId) {
   return Mono.defer(() ->
      isValidSession(sessionId)
         ? Mono.fromCallable(() -> requestUser(sessionId))
         : Mono.error(new RuntimeException("Invalid user session")));
}
```

이 코드는 실제 구독이 발생할 때까지 `sessionId` 유효성 검사를 연기합니다. 대조적으로 다음 코드는 실제 구독보다 훨씬 이전일 수 있는 `requestUserData(...)` 메서드가 호출될 때 유효성 검사를 수행합니다(또한 구독이 전혀 발생하지 않을 수 있음).

```java
Mono<User> requestUserData(String sessionId) {
   return isValidSession(sessionId) 
      ? Mono.fromCallable(() -> requestUser(sessionId))
      : Mono.error(new RuntimeException("Invalid user session"));
}
```
첫 번째 예는 누군가가 반환된 Mono<User>를 구독할 때마다 세션의 유효성을 검사합니다. 
두 번째 예는 세션 유효성 검사를 수행하지만 `requestUserData` 메서드가 호출된 경우에만 수행합니다. 
그러나 구독이 수행되면 유효성 검사가 발생하지 않습니다.

요약하자면, Project Reactor는 `just()` 메소드로 요소를 열거하는 것만으로 Flux 및 Mono 시퀀스를 생성할 수 있습니다. 
`justOrEmpty()`를 사용하여 Optional을 Mono로 쉽게 래핑하거나 fromSupplier 메서드를 사용하여 Supplier를 Mono로 래핑할 수 있습니다. 
`fromFuture()` 메서드로 Future를 매핑하거나 fromRunnable 팩토리 메서드로 Runnable을 매핑할 수 있습니다. 또한 fromArray 또는 fromIterable 메서드를 사용하여 배열 또는 Iterable 컬렉션을 Flux 스트림으로 변환할 수 있습니다. 뿐만 아니라 Project Reactor를 사용하면 이 장의 뒷부분에서 다룰 보다 복잡한 반응 시퀀스를 생성할 수 있습니다. 

이제 리액티브 스트림에서 생성된 요소를 소비하는 방법을 알아보겠습니다.


##  Reactive Streams을 구독하기

Flux와 Mono는 `subscribe()` 메서드의 람다 기반 오버로드를 제공하여 구독 루틴을 많이 단순화합니다.

```java
subscribe(); // (1)

subscribe(Consumer<T> dataConsumer); // (2)

subscribe(Consumer<T> dataConsumer, // (3)
          Consumer<Throwable> errorConsumer);

subscribe(Consumer<T> dataConsumer, // (4)
          Consumer<Throwable> errorConsumer,
          Runnable completeConsumer);

subscribe(Consumer<T> dataConsumer, // (5)
          Consumer<Throwable> errorConsumer,
          Runnable completeConsumer,
          Consumer<Subscription> subscriptionConsumer);

subscribe(Subscriber<T> subscriber); // (6)
```
구독자를 만들기 위한 옵션을 살펴보겠습니다. 
우선, `subscribe()` 메서드의 모든 재정의는 `Disposable` 인터페이스의 인스턴스를 반환합니다. 기본 구독을 취소하는 데 사용할 수 있습니다. (1)~(4)의 경우 구독은 무제한 수요(Long.MAX_VALUE)를 요청합니다. 

이제 차이점을 살펴보겠습니다.

이 방법은 모든 신호를 무시하므로 스트림을 구독하는 가장 간단한 방법입니다. 일반적으로 다른 변형이 선호되어야 합니다. 그러나 때로는 부작용이 있는 스트림 처리를 트리거하는 것이 유용할 수 있습니다.
dataConsumer는 각 값(onNext 신호)에서 호출됩니다. onError 및 onComplete 신호를 처리하지 않습니다.
옵션 (2)와 동일합니다. 그러나 이를 통해 onError 신호를 처리할 수 있습니다. onComplete 신호는 무시됩니다.
옵션 (3)과 동일합니다. 그러나 이것은 또한 onComplete 신호의 처리를 허용합니다.

핸들 오류 및 완료 신호를 포함하여 반응 스트림의 모든 요소를 ​​사용할 수 있습니다. 중요한 것은 이 재정의를 통해 적절한 양의 데이터를 요청하여 구독을 제어할 수 있다는 점입니다. 물론 Long.MAX_VALUE를 요청할 수도 있습니다.
시퀀스를 구독하는 가장 일반적인 방법입니다. 여기에서 원하는 동작으로 구독자 구현을 제공할 수 있습니다. 이 옵션은 매우 다양하지만 거의 필요하지 않습니다.

간단한 Reactive Stream을 만들고 구독해 보겠습니다.
```java
Flux.just("A", "B", "C")
   .subscribe(
      data -> log.info("onNext: {}", data),
      err -> { /* ignored  */ },
      () -> log.info("onComplete"));
```
The preceding code produces the following console output:
```
onNext: A
onNext: B
onNext: C
onComplete
```
단순한 구독 요청 무제한 수요 옵션(Long.MAX_VALUE)은 때때로 생산자가 수요를 충족시키기 위해 상당한 양의 작업을 수행하도록 강제할 수 있다는 점에 다시 한 번 주목할 가치가 있습니다. 따라서 생산자가 제한된 수요를 처리하는 데 더 적합하다면 구독 객체로 수요를 제어하거나 이 장의 뒷부분에서 다룰 요청 제한 연산자를 적용하여 수요를 제어하는 것이 좋습니다.

수동 구독 제어로 Reactive Stream을 구독해 보겠습니다.

```java
Flux.range(1, 100) // (1)
    .subscribe( // (2)
        data -> log.info("onNext: {}", data),
        err -> { /* ignore */ },
        () -> log.info("onComplete"),
        subscription -> { // (3)
           subscription.request(4); // (3.1)
           subscription.cancel(); // (3.2)
        }
    );
```
앞의 코드는 다음을 수행합니다.

먼저 범위 연산자를 사용하여 100개의 값을 생성합니다. 이전 예제와 같은 방식으로 스트림을 구독합니다.
그러나 이제 우리는 구독을 제어합니다. 처음에는 4개 항목(3.1)을 요청한 후 즉시 구독을 취소(3.2)하므로 다른 요소가 전혀 생성되지 않아야 합니다.

앞의 코드를 실행하면 다음 출력이 수신됩니다.
```
onNext: 1
onNext: 2
onNext: 3
onNext: 4
```
스트림이 완료되기 전에 구독자가 구독을 취소했기 때문에 `onComplete` 신호를 수신하지 않습니다. Reactive Stream은 생산자(onError 또는 onComplete 신호 사용)에 의해 완료되거나 구독 인스턴스를 통해 구독자가 취소할 수 있음을 기억하는 것도 중요합니다. 또한 Disposable 인스턴스는 취소 목적으로 사용될 수도 있습니다. 

일반적으로 가입자가 사용하지 않고 위의 추상화 한 수준 코드에서 사용합니다. 

예를 들어 Disposable을 호출하여 스트림 처리를 취소해 보겠습니다.

```java
Disposable disposable = Flux.interval(Duration.ofMillis(50)) // (1)
    .subscribe( // (2)
        data -> log.info("onNext: {}", data)
    );
Thread.sleep(200); // (3)
disposable.dispose(); // (4)
```

앞의 코드는 다음을 수행합니다.

간격 팩토리 방법을 사용하면 정의된 기간(50밀리초마다)으로 이벤트를 생성할 수 있습니다. 생성된 스트림은 끝이 없습니다. onNext 신호에 대한 핸들러만 제공하여 구독합니다. 우리는 몇 가지 이벤트를 수신할 시간을 기다립니다(200/50은 약 4개의 이벤트를 전달할 수 있어야 함). 구독을 내부적으로 취소하는 dispose 메서드를 호출합니다.


### Implementing custom subscribers

기본 subscribe(...) 메서드가 필요한 다용성을 제공하지 않는 경우 자체 구독자를 구현할 수 있습니다. 다음과 같이 항상 Reactive Streams 사양에서 구독자 인터페이스를 직접 구현하고 이를 스트림에 구독할 수 있습니다.

```java
Subscriber<String> subscriber = new Subscriber<String>() {
   volatile Subscription subscription; // (1)

   public void onSubscribe(Subscription s) { // (2)
      subscription = s; // (2.1)
      log.info("initial request for 1 element"); //
      subscription.request(1); // (2.2)
   }

   public void onNext(String s) { // (3)
      log.info("onNext: {}", s); //
      log.info("requesting 1 more element"); //
      subscription.request(1); // (3.1)
   }

   public void onComplete() {
      log.info("onComplete");
   }

   public void onError(Throwable t) {
      log.warn("onError: {}", t.getMessage());
   }
};

Flux<String> stream = Flux.just("Hello", "world", "!");            // (4)
stream.subscribe(subscriber);                                      // (5)
```
사용자 정의 `Subscriber` 구현에서 다음을 수행합니다.

구독자는 게시자와 구독자를 묶는 구독에 대한 참조를 보유해야 합니다. 구독 및 데이터 처리가 다른 스레드에서 발생할 수 있으므로 volatile 키워드를 사용하여 모든 스레드가 구독 인스턴스에 대한 올바른 참조를 갖도록 합니다.
구독이 도착하면 구독자는 onSubscribe 콜백으로 알림을 받습니다. 여기에서 구독(2.1)을 저장하고 초기 수요(2.2)를 요청합니다. 해당 요청이 없으면 TCK 불만 사항 제공자는 데이터를 보낼 수 없으며 요소 처리가 전혀 시작되지 않습니다.

onNext 콜백에서 수신된 데이터를 기록하고 다음 요소를 요청합니다. 이 경우 역압 관리를 위해 간단한 풀 모델(subscription.request(1))을 사용합니다.
여기에서는 just factory 메소드를 사용하여 간단한 스트림을 생성합니다.
여기에서 우리는 (4)단계에서 정의한 Reactive Stream에 대한 우리의 커스텀 구독자를 구독합니다.
앞의 코드는 다음 콘솔 출력을 생성해야 합니다.
```
initial request for 1 element
onNext: Hello
requesting 1 more element
onNext: world
requesting 1 more element
onNext: !
requesting 1 more element
onComplete
```
그러나 설명된 구독 정의 접근 방식은 옳지 않습니다. 선형 코드 흐름을 깨고 오류가 발생하기 쉽습니다. 가장 어려운 부분은 역압을 자체적으로 관리하고 가입자에 대한 모든 TCK 요구 사항을 올바르게 구현해야 한다는 것입니다. 또한 이전 예에서 구독 유효성 검사 및 취소와 관련된 몇 가지 TCK 요구 사항을 위반했습니다.

대신 Project Reactor에서 제공하는 BaseSubscriber 클래스를 확장하는 것이 좋습니다. 이 경우 구독자는 다음과 같이 보일 수 있습니다.

```java
class MySubscriber<T> extends BaseSubscriber<T> {
   public void hookOnSubscribe(Subscription subscription) {
      log.info("initial request for 1 element");
      request(1);
   }

   public void hookOnNext(T value) {
      log.info("onNext: {}", value);
      log.info("requesting 1 more element");
      request(1);
   }
}
```
hookOnSubscribe(Subscription) 및 hookOnNext(T) 메서드와 함께 hookOnError(Throwable), hookOnCancel(), hookOnComplete() 및 기타 소수와 같은 메서드를 재정의할 수 있습니다. BaseSubscriber 클래스는 이러한 메서드(request(long) 및 requestUnbounded())를 사용하여 Reactive Stream 요구를 세부적으로 제어하기 위한 메서드를 제공합니다. 또한 BaseSubscriber 클래스를 사용하면 TCK 호환 구독자를 훨씬 쉽게 구현할 수 있습니다. 이러한 접근 방식은 가입자 자신이 세심한 수명 주기 관리로 귀중한 리소스를 보유하고 있는 경우에 바람직할 수 있습니다. 예를 들어 구독자는 파일 처리기 또는 WebSocket 연결을 타사 서비스로 래핑할 수 있습니다.


### 연산자로 반응형 시퀀스 변환

반응형 시퀀스로 작업할 때 스트림을 만들고 사용하는 것 외에도 이를 완벽하게 변환하고 조작할 수 있는 능력을 갖는 것이 중요합니다. 그래야만 반응 프로그래밍이 유용한 기술이 됩니다. Project Reactor는 거의 모든 필수 반응 변환을 위한 도구(메서드 및 팩토리 메서드)를 제공하며 일반적으로 라이브러리의 기능을 다음과 같이 분류할 수 있습니다.

- 기존 시퀀스 변환
- 시퀀스 처리 엿보기 메소드
- Flux 시퀀스의 분할 및 결합
- 시간 다루기
- 데이터를 동기적으로 리턴하기

여기에서 Reactor의 모든 연산자와 팩토리 메서드를 설명할 수는 없습니다. 너무 많은 페이지가 필요하고 모든 것을 기억하는 것이 거의 불가능하기 때문입니다. Project Reactor가 적절한 연산자 선택에 대한 가이드를 포함하여 훌륭한 문서를 제공한다는 점을 고려할 때 또한 불필요합니다: http://projectreactor.io/docs/core/release/reference/#which-operator. 그래도 이 섹션에서는 몇 가지 코드 샘플과 함께 가장 많이 사용되는 연산자를 살펴보겠습니다.

대부분의 연산자에는 기본 동작을 보강하기 위해 다양한 옵션이 있는 재정의가 많이 있습니다. 또한 각 버전에서 Project Reactor는 점점 더 유용한 연산자를 받습니다. 따라서 연산자에 대한 최신 업데이트는 Reactor의 설명서를 참조하십시오.


### 반응 시퀀스의 매핑 요소

시퀀스를 변환하는 가장 자연스러운 방법은 모든 요소를 ​​새로운 값으로 매핑하는 것입니다. Flux와 Mono는 Java Stream API의 map() 연산자와 유사하게 동작하는 map 연산자를 제공합니다. `map(Function<T, R>)` 서명이 있는 함수를 사용하면 요소를 하나씩 처리할 수 있습니다. 물론 T에서 R로 요소의 타입을 변경함에 따라 전체 시퀀스의 타입이 변경되므로 맵 연산자 Flux<T>가 Flux<R>이 된 후 Mono<T>가 Mono<R>이 됩니다. Flux.map()의 다이어그램은 다음과 같습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/e7c4bd93-1df8-472c-9a93-ab1ed33972af.png)

도표 4.4 map 연산자

물론 Mono 클래스의 map 연산자도 유사하게 동작합니다. `cast(Class c)` 연산자는 스트림의 요소를 대상 클래스로 캐스팅합니다. cast(Class c) 연산자를 구현하는 가장 쉬운 방법은 map() 연산자를 사용하는 것입니다. Flux 클래스의 소스를 살펴보고 우리의 가정을 증명하는 다음 코드를 찾을 수 있습니다.

```java
public final <E> Flux<E> cast(Class<E> clazz) {
   return map(clazz::cast);
}
```
인덱스 연산자를 사용하면 시퀀스의 요소를 열거할 수 있습니다. 이 메서드에는 Flux<Tuple2<Long, T>> index() 시그니처가 있습니다. 이제 Tuple2 클래스로 작업해야 합니다. 이것은 표준 Java 라이브러리에 없는 Tuple 데이터 구조를 나타냅니다. 라이브러리는 라이브러리 운영자가 자주 사용하는 Tuple2 ~ Tuple8 클래스를 제공합니다. 타임스탬프 연산자는 인덱스 연산자와 유사하게 작동하지만 인덱스 대신 현재 타임스탬프를 추가합니다. 따라서 다음 코드는 요소를 열거하고 시퀀스의 모든 요소에 타임스탬프를 첨부해야 합니다.

```java
Flux.range(2018, 5)                                                // (1)
    .timestamp()                                                   // (2)
    .index()                                                       // (3)
    .subscribe(e -> log.info("index: {}, ts: {}, value: {}",       // (4)
        e.getT1(),                                                 // (4.1)
        Instant.ofEpochMilli(e.getT2().getT1()),                   // (4.2)
        e.getT2().getT2())); 
```
앞의 코드는 다음을 수행합니다.

여기에서 범위 연산자(2018~2022)를 사용하여 일부 데이터를 생성합니다. 이 연산자는 Flux<Integer> 유형의 시퀀스를 반환합니다.
타임스탬프 연산자를 사용하여 현재 타임스탬프를 첨부합니다. 이제 시퀀스에는 Flux<Tuple2<Long, Integer>> 유형이 있습니다.
여기서는 인덱스 연산자로 열거를 적용합니다. 이제 시퀀스에는 Flux<Tuple2<Long, Tuple2<Long, Integer>>> 유형이 있습니다.
여기에서 시퀀스 및 로그 요소를 구독합니다. e.getT1() 호출은 인덱스(4.1)를 반환하고 e.getT2().getT1() 호출은 타임스탬프를 반환합니다. 이 타임스탬프는 Instant 클래스(4.2)를 사용하여 사람이 읽을 수 있는 방식으로 출력하는 반면 e. .getT2().getT2() 호출은 실제 값(4.3)을 반환합니다.
이전 코드 조각을 실행한 후 다음 출력을 받아야 합니다.

```
index: 0, ts: 2018-09-24T03:00:52.041Z, value: 2018
index: 1, ts: 2018-09-24T03:00:52.061Z, value: 2019
index: 2, ts: 2018-09-24T03:00:52.061Z, value: 2020
index: 3, ts: 2018-09-24T03:00:52.061Z, value: 2021
index: 4, ts: 2018-09-24T03:00:52.062Z, value: 2022
```

### Filtering reactive sequences
Of course, Project Reactor contains all kinds of operators for filtering elements, such as:

- `filter` 연산자는 조건을 만족하는 요소만 전달합니다.
- `ignoreElements` 연산자는 Mono<T>를 반환하고 모든 요소를 필터링합니다. 결과 시퀀스는 원본이 끝난 후에만 끝납니다.
- 라이브러리는 첫 번째 n을 제외한 모든 요소를 무시하는 take(n) 메서드를 사용하여 가져온 요소를 제한할 수 있습니다.
- takeLast는 스트림의 마지막 요소만 반환합니다.
- takeUntil(Predicate)은 어떤 조건이 충족될 때까지 요소를 전달합니다.
- elementAt(n)은 시퀀스의 n번째 요소만 사용하도록 허용합니다.
- 단일 연산자는 소스에서 단일 항목을 내보내고 빈 소스의 경우 NoSuchElementException 오류 또는 둘 이상의 요소가 있는 소스의 경우 IndexOutOfBoundsException에 신호를 보냅니다.
- skip(Duration) 또는 take(Duration) 연산자를 사용하면 양 뿐만 아니라 Duration으로도 요소를 가져오거나 건너뛸 수 있습니다.
- 또한 다른 스트림(takeUntilOther(Publisher) 또는 skipUntilOther(Publisher))에서 메시지가 도착할 때까지 요소를 건너뛰거나 취할 수 있습니다.

Let's consider a workflow where we have to start and then stop stream processing as a reaction to some events originating from other streams. The code may look like the following:

```java
Mono<?> startCommand = ...
Mono<?> stopCommand = ...
Flux<UserEvent> streamOfData = ...

streamOfData
    .skipUntilOther(startCommand)
    .takeUntilOther(stopCommand)
    .subscribe(System.out::println);
```
In this case, we may start and then stop elements processing, but only once. The marble diagram for this use case would be as follows:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/c229f8bb-f720-4d97-b68d-0f83a1d395a3.png)

Diagram 4.5 Peeking elements between start-stop commands

### reactive 시퀀스 컬렉션
It is possible to collect all elements in the list and process the resulting collection as a Mono stream with Flux.collectList() and Flux.collectSortedList(). The last one not only collects elements but also sorts them. Consider the following code:

```java
Flux.just(1, 6, 2, 8, 3, 1, 5, 1)
    .collectSortedList(Comparator.reverseOrder())
    .subscribe(System.out::println);
```
This produces the following output with one collection containing sorted numbers:
```
[8, 6, 5, 3, 2, 1, 1, 1]
```
Note that collecting sequence elements in the collection may be resource hungry, especially when a sequence has many elements. Also, it is possible to consume all the available memory when trying to collect on an endless stream.
Project Reactor allows the collection of Flux elements not only to List, but also to the following:

- Map ( Map<K, T>) with the collectMap operator
- Multi-map (Map<K, Collection<T>>) with the collectMultimap operator
- Any data structure with a custom java.util.stream.Collector and the Flux.collect(Collector) operator

Both Flux and Mono have the repeat() and repeat(times) methods, which allow for the looping of incoming sequences. We have already used these in the previous section.

One more handy method, called defaultIfEmpty(T), allows the provision of default values for an empty Flux or Mono.

Flux.distinct() passes only the element that has not been encountered in a stream before. However, this method keeps track of all unique elements, so use it carefully, especially with high-cardinality data streams. The distinct method has overrides that allow the provision of custom algorithms for duplicate tracking. So, it is sometimes possible to optimize resource usage of the distinct operator manually.

High-cardinality refers to data with elements that are very uncommon or unique. For example, identification numbers or usernames are typically highly-cardinal. At the same time, enum values or values from a small fixed dictionary are not. 
The Flux.distinctUntilChanged() operator has no such limitation and can be used for endless streams to remove duplicates that appear in an uninterrupted row. The following marble-diagram shows its behavior:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/9bc7400f-2c72-44b4-875b-5d444102e56e.png)

Diagram 4.6. Operator—distinct until changed

### Reducing stream elements

Project Reactor를 사용하면 스트림의 요소 수를 count()하거나 Flux.all(Predicate)을 사용하여 모든 요소에 필수 속성이 있는지 확인할 수 있습니다. Flux.any(Predicate) 연산자를 사용하여 하나 이상의 요소에 원하는 속성이 있는지 확인하는 것도 쉽습니다.

hasElements 연산자를 사용하여 스트림에 요소가 있는지 또는 hasElement 연산자를 사용하여 스트림에 원하는 요소가 포함되어 있는지 확인할 수 있습니다. 후자는 단락 논리를 구현하고 요소가 값과 일치하는 즉시 true로 완료됩니다. 또한 any 연산자를 사용하면 사용자 정의 Predicate 인스턴스를 제공하여 요소의 동등성뿐만 아니라 다른 속성도 확인할 수 있습니다. 시퀀스에 짝수가 있는지 확인합시다.

```java
Flux.just(3, 5, 7, 9, 11, 15, 16, 17)
    .any(e -> e % 2 == 0)
    .subscribe(hasEvens -> log.info("Has evens: {}", hasEvens));
```

sort() 연산자를 사용하면 백그라운드에서 요소를 정렬한 다음 원래 시퀀스가 완료되면 정렬된 시퀀스를 내보냅니다.

Flux 클래스를 사용하면 사용자 정의 논리로 시퀀스를 줄일 수 있습니다(때때로 절차를 폴딩이라고 함). 감소 연산자에는 일반적으로 초기 값과 이전 단계의 결과를 현재 단계의 요소와 결합하는 함수가 필요합니다. 1과 10 사이의 정수를 합해 봅시다. 

```java
Flux.range(1, 5)
    .reduce(0, (acc, elem) -> acc + elem)
    .subscribe(result -> log.info("Result: {}", result));
```
The result would be 15. The reduce operator produces only one element with the final result. However, when doing aggregations, it's sometimes handy to send downstream intermediate results. The Flux.scan() operator does that. Let's sum integer numbers between 1 and 10 with the scan operator:

```java
Flux.range(1, 5)
    .scan(0, (acc, elem) -> acc + elem)
    .subscribe(result -> log.info("Result: {}", result));
```
The preceding code produces the following output:
```
Result: 0
Result: 1
Result: 3
Result: 6
Result: 10
Result: 15
```
As we can see, the final result is the same (15). However, we also received all intermediate results. With that said, the scan operator may be useful for many applications that need some information about ongoing events. For example, we can calculate the moving average on the stream:

```java
int bucketSize = 5;                                                // (1)
Flux.range(1, 500)                                                 // (2)
    .index()                                                       // (3)
    .scan(                                                         // (4)
        new int[bucketSize],                                       // (4.1)
        (acc, elem) -> {                                           //
           acc[(int)(elem.getT1() % bucketSize)] = elem.getT2();   // (4.2)
           return acc;                                             // (4.3)
        })
    .skip(bucketSize)                                              // (5)
    .map(array -> Arrays.stream(array).sum() * 1.0 / bucketSize)   // (6)
    .subscribe(av -> log.info("Running average: {}", av));         // (7)
```

Let's describe this code:

(1) 여기에서 이동 평균 창의 크기를 정의합니다(가장 최근 5개 이벤트에 관심이 있다고 가정해 보겠습니다).

(2) 범위 연산자를 사용하여 데이터를 생성해 보겠습니다.

(3) 인덱스 연산자를 사용하여 각 요소에 인덱스를 첨부할 수 있습니다.

(4) 스캔 연산자를 사용하여 최신 5개 요소를 컨테이너(4.1)에 수집합니다. 여기서 요소의 인덱스는 컨테이너(4.2)의 위치를 ​​계산하는 데 사용됩니다. 모든 단계에서 업데이트된 콘텐츠가 포함된 동일한 컨테이너를 반환합니다.

(5) 여기에서 이동 평균에 대한 충분한 데이터를 수집하기 위해 스트림 시작 부분의 일부 요소를 건너뜁니다.

(6) 이동 평균값을 계산하기 위해 컨테이너 내용물의 합을 크기로 나눕니다.

(7) 물론 값을 받기 위해서는 데이터를 구독해야 합니다.

Mono 및 Flux 스트림에는 상위 스트림이 완료될 때 완료되는 then, thenMany 및 thenEmpty 연산자가 있습니다. 연산자는 들어오는 요소를 무시하고 완료 또는 오류 신호만 재생합니다. 이러한 연산자는 상위 스트림이 처리를 완료하는 즉시 새 스트림을 트리거하는 데 유용할 수 있습니다.

```java
Flux.just(1, 2, 3)
    .thenMany(Flux.just(4, 5))
    .subscribe(e -> log.info("onNext: {}", e));
```   
The lambda in the subscribe method receives only 4 and 5, even though 1, 2, and 3 are generated and processed by the stream.

### Combining Reactive Streams

Of course, Project Reactor allows the combining of many incoming streams into one outgoing stream. The named operators have many overrides but perform the following transformations:

- The concat operator concatenates all sources by forwarding received elements downstream. When the operator concatenates two streams, at first, it consumes and resends all elements of the first stream, then does the same for the second.

- The merge operator merges data from upstream sequences into one downstream sequence. Unlike  the concat operator, upstream sources are subscribed to eagerly (at the same time).

- The zip operator subscribes to all upstreams, waits for all sources to emit one element and then combines received elements into an output element. In Chapter 2, Reactive Programming in Spring - Basic Concepts, we described how zip works in detail. In Reactor, the zip operator may operate not only with reactive publishers but also with an Iterable container. For that purpose, we can use the zipWithIterable operator.

- The combineLatest operator works similarly to the zip operator. However, it generates a new value as soon as at least one upstream source emits a value.

Let's concatenate a couple of streams:
```java
Flux.concat(
    Flux.range(1, 3),
    Flux.range(4, 2),
    Flux.range(6, 5)
).subscribe(e -> log.info("onNext: {}", e));
```
Obviously, the preceding code in the result generates values from 1 to 10 ([1, 2 , 3] + [4, 5] + [6, 7, 8, 9, 10]).

### Batching stream elements

Project Reactor는 다음과 같은 몇 가지 방법으로 스트림 요소(Flux<T>)의 일괄 처리를 지원합니다.

- List와 같은 컨테이너에 요소를 버퍼링하면 결과 스트림은 Flux<List<T>> 유형을 갖습니다.

- Flux<Flux<T>>와 같은 스트림 스트림으로 요소를 윈도잉합니다. 이제 스트림은 값이 아니라 우리가 처리할 수 있는 하위 스트림을 나타냅니다.

- Flux<GroupedFlux<K, T>> 유형의 스트림으로 일부 키로 요소를 그룹화합니다. 각각의 새 키는 새 GroupedFlux 인스턴스를 트리거하고 해당 키가 있는 모든 요소는 GroupFlux 클래스의 해당 인스턴스를 통해 푸시됩니다.

버퍼링 및 윈도우잉은 다음에 따라 발생할 수 있습니다.

- 처리된 요소의 수; 10개의 요소마다
- 일정 기간; 5분마다 말하자
- 일부 술어를 기반으로 합니다. 각각의 새로운 짝수 전에 절단한다고 가정 해 봅시다.
- 실행을 제어하는 다른 Flux의 이벤트 도착을 기반으로 합니다.

Let's buffer integer elements in lists of size 4:

```java
Flux.range(1, 13)
    .buffer(4)
    .subscribe(e -> log.info("onNext: {}", e));
```
The preceding code generates the following output:

```
onNext: [1, 2, 3, 4]
onNext: [5, 6, 7, 8]
onNext: [9, 10, 11, 12]
onNext: [13]
```
프로그램의 출력에서 마지막 요소를 제외한 모든 요소가 크기 4의 목록임을 알 수 있습니다. 마지막 요소는 13의 계수를 4로 나눈 값이기 때문에 크기 1의 컬렉션입니다. 버퍼 연산자는 많은 이벤트를 컬렉션으로 수집합니다. 이벤트. 해당 컬렉션 자체는 다운스트림 운영자의 이벤트가 됩니다. 버퍼 연산자는 하나의 요소만 있는 많은 작은 요청 대신 요소 모음으로 소수의 요청을 만드는 것이 바람직할 때 일괄 처리에 편리합니다. 예를 들어, 데이터베이스에 요소를 하나씩 삽입하는 대신 몇 초 동안 항목을 버퍼링하고 일괄 삽입을 수행할 수 있습니다. 물론 이는 일관성 요구 사항이 허용하는 경우에만 해당됩니다.

 `window()` 연산자를 연습하기 위해 요소가 소수일 때마다 숫자 시퀀스를 창으로 분할해 보겠습니다. 이를 위해 창 연산자의 windowUntil 변형을 사용할 수 있습니다. 조건자를 사용하여 새 슬라이스를 만드는 시기를 결정합니다. 코드는 다음과 같을 수 있습니다.

```java
Flux<Flux<Integer>> windowedFlux = Flux.range(101, 20)              // (1)
    .windowUntil(this::isPrime, true);                              // (2)

windowedFlux.subscribe(window -> window                             // (3)
        .collectList()                                              // (4)
        .subscribe(e -> log.info("window: {}", e)));                // (5)
```
Let's look at the preceding code:

At first, we generate 20 integers starting with 101.

Here, we slice a new window with elements each time a number is a prime number. The second argument of the windowUntil operator defines whether we cut a new slice before or after satisfying the predicate. In the preceding code, we slice the way that a new prime number begins its window. The resulting stream has the Flux<Flux<Integer>> type.
Now, we may subscribe to the windowedFlux stream. However, each element of the windowedFlux stream is itself a Reactive Stream. So, for each window, we make another reactive transformation.

In our case, for each window, we collect elements with the collectList operator so that each window is now reduced to the Mono<List<Integer>> type.

For each internal Mono element, we make a separate subscription and log received events.
The preceding code generates the following output:

```java
window: []
window: [101, 102]
window: [103, 104, 105, 106]
window: [107, 108]
window: [109, 110, 111, 112]
window: [113, 114, 115, 116, 117, 118, 119, 120]
```
Note that the first window is empty. This happens because, as soon as we start the original stream, we generate an initial window. Then, the first element arrives (number 101), which is a prime number, which triggers a new window and, consequently, the already-opened window is closed (with the onComplete signal) without any elements.

Of course, we could resolve the exercise with the buffer operator. Both operators behave pretty similarly. However, buffer emits a collection only when a buffer closes, while the window operator propagates events as soon as they arrive, making it possible to react sooner and implement more sophisticated workflows.

Also, we may group elements in a Reactive Stream by some criteria with the groupBy operator. Let's divide the integer sequence by odd and even numbers and track only the last two elements in each group. The code may look like the following:

```java
Flux.range(1, 7)                                                   // (1)
    .groupBy(e -> e % 2 == 0 ? "Even" : "Odd")                     // (2)
    .subscribe(groupFlux -> groupFlux                              // (3)
        .scan(                                                     // (4)
            new LinkedList<>(),                                    // (4.1)
            (list, elem) -> {
                list.add(elem);                                    // (4.2)
                if (list.size() > 2) {
                    list.remove(0);                                // (4.3)
                }
                return list;
            })
        .filter(arr -> !arr.isEmpty())                             // (5)
        .subscribe(data ->                                         // (6)
            log.info("{}: {}", groupFlux.key(), data)));
```
Let's look at the preceding code:

Here, we generate a small sequence of numbers.

With the groupBy operator, we split the sequence between odd and even numbers based on the division module. The operator returns a stream of type Flux<GroupedFlux<String, Integer>>.

Here, we subscribe to the main Flux and for each of the grouped fluxes, we apply the scan operator.

The scan operator is a seed with the empty list (4.1). Each element in the grouped flux is added to the list (4.2), and if the list is larger than two elements, the oldest element is removed (4.3).

The scan operator, first of all, propagates the seed and then recalculated values. In that case, the filter operator allows us to remove empty data containers from the scan's seed.

Finally we subscribe separately for each grouped flux and display what the scan operator sends.

As we expect, the preceding code displays the following output:
```
Odd: [1]
Even: [2]
Odd: [1, 3]
Even: [2, 4]
Odd: [3, 5]
Even: [4, 6]
Odd: [5, 7]
```
Also, the Project Reactor library supports some advanced techniques such as grouping emitted elements over distinct time windows. For that functionality, please refer to the documentation of the groupJoin operator.
