# Reactive Streams - the New Streams' Standard

In this chapter, we are going to cover some of the problems mentioned in the previous chapter, along with those that arise when several reactive libraries meet in one project. We will also dig deeper into backpressure control in reactive systems. Here, we are going to oversee solutions proposed by RxJava as well as its limitations. We will explore how the Reactive Streams specification solves those problems, learning the essentials of this specification. We will also cover the reactive landscape changes that come with a new specification. Finally, to reinforce our knowledge, we are going to build a simple application and combine several reactive libraries within it.

In this chapter, the following topics are covered:

- 공통 API 문제
- Backpressure 제어
- Reactive 스트림 예제
- 기술간의 호환성
- Reactive Streams inside JDK 9
- Reactive Streams 고급 개념
- Reinforcement of reactive landscape
- Reactive Streams in action

## 모두를 위한 반응성

In previous chapters, we have learned a lot of exciting things about reactive programming in Spring, as well as the role `RxJava` plays in its story. We also looked at the need to use reactive programming to implement the reactive system. We have also seen a brief overview of the reactive landscape and available alternatives to `RxJava`, which makes it possible to quickly start with reactive programming.

## API's 불일치 문제

한편으로 `RxJava`와 같은 경쟁 라이브러리의 광범위한 목록과 `CompletableStage`와 같은 `Java Core` 라이브러리의 기능은 우리가 코드를 작성하는 방식에 대한 선택권을 줍니다. 예를 들어, 처리 중인 항목의 흐름을 작성하기 위해 RxJava의 API에 도달하는 데 의존할 수 있습니다. 따라서 복잡하지 않은 비동기 요청-응답 상호작용을 구축하려면 `CompletableStage`에 의존하는 것으로 충분합니다. 또는 `org.springframework.util.concurrent.ListenableFuture`와 같은 프레임워크 특정 클래스를 사용하여 구성 요소 간의 비동기 상호 작용을 구축하고 해당 프레임워크로 작업을 단순화할 수 있습니다.

반면에 선택의 폭이 넓으면 시스템이 너무 복잡해지기 쉽습니다. 예를 들어, 비동기 비차단 통신 개념에 의존하지만 다른 API를 가진 두 개의 라이브러리가 있으면 한 콜백을 다른 콜백으로 또는 그 반대로 변환하기 위해 추가 유틸리티 클래스를 제공하게 됩니다.

```java
interface AsyncDatabaseClient { // (1)
   <T> CompletionStage<T> store(CompletionStage<T> stage);
}

final class AsyncAdapters {
   public static <T> CompletionStage<T> toCompletion( // (2)
                     ListenableFuture<T> future) {

      CompletableFuture<T> completableFuture = // (2.1)
         new CompletableFuture<>();

      future.addCallback( // (2.2)
         completableFuture::complete,
         completableFuture::completeExceptionally
      );

      return completableFuture;
   }

   public static <T> ListenableFuture<T> toListenable( // (3)
                     CompletionStage<T> stage) {
      SettableListenableFuture<T> future = // (3.1)
         new SettableListenableFuture<>();

      stage.whenComplete((v, t) -> { // (3.2)
         if (t == null) {
            future.set(v);
         }
         else {
            future.setException(t);
         }
      });

      return future;
   }
}

@RestController // (4)
public class MyController {
   ...
   @RequestMapping
   public ListenableFuture<?> requestData() { // (4.1)
      AsyncRestTemplate httpClient = ...;
      AsyncDatabaseClient databaseClient = ...;

      CompletionStage<String> completionStage = toCompletion( // (4.2)
         httpClient.execute(...)
      );

      return toListenable( // (4.3)
         databaseClient.store(completionStage)
      );
   }
}
```

1. 비동기 데이터베이스 접근을 위한 가능한 클라이언트 인터페이스의 샘플인 비동기 데이터베이스 클라이언트의 인터페이스 선언입니다.

2. `CompletionStage` 어댑터 메소드 구현에 대한 `ListenableFuture`입니다.

3. (2.1) 지점에서 CompletionStage를 수동으로 제어하기 위해 인수가 없는 생성자를 통해 CompletableFuture라는 직접 구현을 만듭니다. ListenableFuture와의 통합을 제공하려면 CompletableFuture의 API를 직접 재사용하는 콜백(2.2)을 추가해야 합니다.

4. 이것은 ListenableFuture 어댑터 메소드 구현에 대한 CompletionStage입니다. (3.1) 지점에서 SettableListenableFuture라는 ListenableFuture의 특정 구현을 선언합니다. 이를 통해 (3.2) 지점에서 CompletionStage 실행 결과를 수동으로 제공할 수 있습니다.

5. RestController의 클래스 선언입니다. 여기 (4.1) 지점에서 비동기적으로 작동하고 비차단 방식으로 실행 결과를 처리하기 위해 ListenableFuture를 반환하는 요청 처리기 메서드를 선언합니다. 차례로 AsyncRestTemplate 실행 결과를 저장하려면 CompletionStage(4.2)에 맞게 조정해야 합니다. 마지막으로 지원되는 API를 만족시키기 위해서는 ListenableFuture에 대한 저장 결과를 다시 채택해야 합니다(4.3).

앞의 예에서 알 수 있듯이 Spring Framework 4.x ListenableFuture 및 CompletionStage와의 직접적인 통합은 없습니다. 게다가, 그 예는 반응 프로그래밍의 일반적인 사용에서 제외되는 경우가 아닙니다. 많은 라이브러리와 프레임워크는 스트리밍 처리 프레임워크와 함께 일반 요청-응답 통신을 포함하는 구성 요소 간의 비동기 통신을 위한 자체 인터페이스와 클래스를 제공합니다. 많은 경우에, 그 문제를 해결하고 여러 독립 라이브러리를 호환 가능하게 만들기 위해 우리는 우리 자신의 적응을 제공하고 몇몇 장소에서 그것을 재사용해야 합니다. 또한 자체 적응에 버그가 포함될 수 있으며 추가 유지 관리가 필요할 수 있습니다.

> Spring 5에서는 L`istenableFuture`의 API가 확장되었고 이러한 비호환성을 해결하기 위해 `completable`이라는 추가 메소드가 제공되었습니다. 이에 대해 자세히 알아보려면 다음 링크를 참조하세요. https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html#completable--.

여기서 핵심 문제는 라이브러리 공급업체가 정렬된 API를 빌드할 수 있는 단일 방법이 없다는 사실에 있습니다. 예를 들어, "2장, Spring의 반응형 프로그래밍 - 기본 개념"에서 보았듯이 RxJava는 Vert.x, Ratpack, Retrofit 등과 같은 많은 프레임워크에서 평가되었습니다.

그들 모두는 RxJava 사용자를 지원하고 기존 프로젝트를 쉽게 통합할 수 있는 추가 모듈을 도입했습니다. RxJava 1.x가 도입된 프로젝트 목록이 광범위하고 웹, 데스크톱 또는 모바일 개발을 위한 프레임워크를 포함하고 있기 때문에 언뜻 보기에 이는 놀라운 일이었습니다. 그러나 개발자 요구 사항에 대한 지원 뒤에 숨겨진 많은 함정이 라이브러리 공급업체에 영향을 미칩니다.

여러 RxJava 1.x 호환 라이브러리가 한 곳에서 만날 때 일반적으로 발생하는 첫 번째 문제는 대략적인 버전 비호환성입니다. RxJava 1.x는 시간이 지남에 따라 매우 빠르게 발전했기 때문에 많은 라이브러리 공급업체는 새 릴리스에 대한 의존성을 업데이트할 기회를 얻지 못했습니다. 때때로 업데이트로 인해 일부 버전이 호환되지 않는 많은 내부 변경 사항이 발생했습니다. 결과적으로 다른 버전의 RxJava 1에 의존하는 다른 라이브러리와 프레임워크를 사용하면 원치 않는 문제가 발생할 수 있습니다.

두 번째 문제는 첫 번째 문제와 유사합니다. RxJava의 사용자 정의는 표준화되지 않았습니다. 여기서 커스터마이징이란 RxJava 확장을 개발하는 동안 흔히 볼 수 있는 Observable 또는 특정 변환 단계의 추가 구현을 제공하는 기능을 말합니다. 표준화되지 않은 API와 빠르게 진화하는 내부로 인해 사용자 지정 구현을 지원하는 것이 또 다른 과제였습니다.

> An excellent example of significant changes in the version may be found at the following link: https://github.com/ReactiveX/RxJava/issues/802.

## Pull versus push

반응형 환경의 초기에는 모든 라이브러리는 데이터가 소스에서 구독자로 푸시된다는 생각으로 설계되었습니다. 이러한 결정은 어떤 경우에는 순수 풀 모델이 충분히 효율적이지 않기 때문에 내려진 것입니다. 예를 들어 네트워크를 통한 통신이 네트워크 경계가 있는 시스템에 나타날 때입니다. 방대한 데이터 목록을 필터링하지만 그 중에서 처음 10개 요소만 취한다고 가정합니다. 이러한 문제를 해결하기 위해 PULL 모델을 수용하면 다음 코드가 남습니다.

```java
final AsyncDatabaseClient dbClient = ... // (1)

public CompletionStage<Queue<Item>> list(int count) { // (2)
   BlockingQueue<Item> storage = new ArrayBlockingQueue<>(count);
   CompletableFuture<Queue<Item>> result = new CompletableFuture<>();
   pull("1", storage, result, count); // (2.1)
   return result;
}

void pull( // (3)
   String elementId,
   Queue<Item> queue,
   CompletableFuture resultFuture,
   int count
) {
   dbClient.getNextAfterId(elementId)
           .thenAccept(item -> {
              if (isValid(item)) { // (3.1)
                 queue.offer(item);

                 if (queue.size() == count) { // (3.2)
                    resultFuture.complete(queue);
                    return;
                 }
              }

              pull(item.getId(), // (3.3)
                   queue,
                   resultFuture,
                   count);
           });
}
```
주석이 달린 코드는 다음과 같이 다시 설명됩니다.

(1) 이것은 `AsyncDatabaseClient` 필드 선언입니다. 여기에서 해당 클라이언트를 사용하여 외부 데이터베이스와 비동기식 비차단 통신을 연결합니다.

이것은 목록 메소드 선언입니다. 여기에서 list 메소드 호출의 결과로 CompletionStage를 반환하여 비동기식 계약을 선언합니다. 차례로 pull 결과를 집계하고 호출자에게 비동기적으로 전송하기 위해 Queue 및 CompletableFuture를 선언하여 수신된 값을 저장한 다음 수집된 Queue를 나중에 수동으로 보냅니다. 여기서 (2.1) 지점에서 pull 메서드의 첫 번째 호출을 시작합니다.

이것은 pull 메소드 선언입니다. 해당 메서드 내에서 `AsyncDatabaseClient#getNextAfterId`를 호출하여 쿼리를 실행하고 결과를 비동기적으로 수신합니다. 그런 다음 결과가 수신되면 (3.1) 지점에서 필터링합니다. 유효한 항목의 경우 대기열에 집계합니다. 또한 (3.2) 지점에서 요소를 충분히 수집했는지 확인하고 호출자에게 보내고 풀링을 종료합니다. 언급된 if 분기 중 하나가 우회된 경우 pull 메서드를 재귀적으로 다시 호출합니다(3.3).

앞의 코드에서 알 수 있듯이 서비스와 데이터베이스 간에 비동기식 비차단 상호작용을 사용합니다. 언뜻보기에 여기에는 아무 문제가 없습니다. 그러나 다음 다이어그램을 보면 격차가 있음을 알 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/de9778d3-6082-4153-a9c5-64f055825f29.png)
도표 3.1. 풀링 처리 흐름의 예

다음 요소를 하나씩 요청하면 서비스에서 데이터베이스로 요청을 전달하는 데 추가 시간이 소요됩니다. 서비스 관점에서 볼 때 전체 처리 시간의 대부분은 유휴 상태에서 낭비됩니다. 리소스를 사용하지 않더라도 추가 네트워크 활동으로 인해 전체 처리 시간이 두 배 또는 세 배까지 늘어납니다. 또한 데이터베이스는 향후 요청 수를 인식하지 못하므로 데이터베이스가 미리 데이터를 생성할 수 없으므로 유휴 상태입니다. 이는 데이터베이스가 새 요청을 기다리고 있으며 응답이 서비스에 전달되고 서비스가 수신 응답을 처리한 다음 데이터의 새 부분을 요청하는 동안 비효율적임을 의미합니다.

전체 실행을 최적화하고 풀링 모델을 1급 시민으로 유지하기 위해 중앙 예제의 다음 수정과 같이 풀링과 일괄 처리를 결합할 수 있습니다.

```java
void pull( // (1)
   String elementId,
   Queue<Item> queue,
   CompletableFuture resultFuture,
   int count
) {

   dbClient.getNextBatchAfterId(elementId, count) // (2)
           .thenAccept(items -> {
              for(Item item : items) { // (2.1)
                 if (isValid(item)) {
                    queue.offer(item);

                    if (queue.size() == count) {
                       resultFuture.complete(queue);
                       return;
                    }
                 }
              }

              pull(items.get(items.size() - 1) // (3)
                        .getId(),
                   queue,
                   resultFuture,
                   count);
           });
}
```

Again, the following key explains the code:

이것은 이전 예에서와 동일한 pull 메소드 선언입니다.
이것은 getNextBatchAfterId 실행입니다. 알 수 있듯이 AsyncDatabaseClient 메서드를 사용하면 List<Item>으로 반환되는 특정 수의 요소를 요청할 수 있습니다. 차례로, 데이터를 사용할 수 있게 되면 일괄 처리의 각 요소를 개별적으로 처리하기 위해 추가 for 루프가 생성된다는 점을 제외하고는 거의 동일한 방식으로 처리됩니다(2.1).

이것은 재귀적 pull 메서드 실행으로, 이전 pull에서 항목이 부족한 경우 항목의 추가 배치를 검색하도록 설계되었습니다.
한편으로 요소 배치를 요청함으로써 목록 메소드 실행의 성능을 크게 향상시키고 전체 처리 시간을 줄일 수 있습니다. 반면에 상호 작용 모델에는 여전히 약간의 차이가 있으며 다음 다이어그램을 분석하여 감지할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/2f32aa40-5d9f-41e7-9765-2e16eba492de.png)

Diagram 3.2. Example of batch-based pulling processing flow

처리 시간이 여전히 약간 비효율적입니다. 예를 들어, 데이터베이스가 데이터를 쿼리하는 동안 클라이언트는 여전히 유휴 상태입니다. 차례로 요소 배치를 보내는 것은 하나만 보내는 것보다 시간이 조금 더 걸립니다. 마지막으로 전체 요소 배치에 대한 추가 요청은 사실상 중복될 수 있습니다. 예를 들어, 처리를 완료하기 위해 하나의 요소만 남아 있고 다음 배치의 첫 번째 요소가 유효성 검사를 충족하면 나머지 항목은 건너뛰고 완전히 중복됩니다.

최종 최적화를 제공하기 위해 데이터를 한 번 요청할 수 있으며 소스는 데이터가 사용 가능해지면 비동기식으로 푸시합니다. 다음 코드 수정은 달성 방법을 보여줍니다.

```java
public Observable<Item> list(int count) { // (1)
   return dbClient.getStreamOfItems() // (2)
                  .filter(item -> isValid(item)) // (2.1)
                  .take(count) // (2.2)
}
```
The annotations are as follows:

(1) list() 메소드는 Observable<Item>리턴 타입이 항목이 푸시되고 있음을 식별합니다.

(2) 스트림 단계를 쿼리합니다. `AsyncDatabaseClient#getStreamOfItems` 메서드를 호출하여 데이터베이스를 한 번 구독합니다.

(2.1) 요소를 필터링하고 연산자를 사용하여 `.take()` 호출자가 요청한 대로 특정 양의 데이터를 가져옵니다.

여기에서 `RxJava 1.x` 클래스를 일급 시민으로 사용하여 푸시된 요소를 수신합니다. 차례로 모든 요구 사항이 충족되면 취소 신호가 전송되고 데이터베이스 연결이 닫힙니다. 현재 상호 작용 흐름은 다음 다이어그램에 나와 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/a4628d56-5aa4-4e98-b980-a5a2877cca58.png)
Diagram 3.3. Example of Push processing flow

앞의 다이어그램에서 전체 처리 시간이 다시 최적화되었습니다. 상호 작용 중에 서비스가 첫 번째 응답을 기다릴 때 큰 유휴 상태가 하나만 있습니다. 첫 번째 요소가 도착한 후 데이터베이스는 후속 요소가 오는 대로 보내기 시작합니다. 결과적으로 처리가 다음 요소를 쿼리하는 것보다 약간 더 빠르더라도 서비스의 전체 유휴 시간은 짧습니다. 그러나 데이터베이스는 필요한 수의 요소가 수집되면 서비스에서 무시하는 초과 요소를 계속 생성할 수 있습니다.

### 흐름 제어 문제

한편으로 앞의 설명은 PUSH 모델을 수용하는 핵심 이유가 요청의 양을 최소화함으로써 전체 처리 시간을 최적화하는 것이라고 의미일 수 있습니다. 이것이 RxJava 1.x 및 유사한 라이브러리가 데이터 푸시용으로 설계된 이유이며 스트리밍이 분산 시스템 내 구성 요소 간의 통신에 유용한 기술이 된 이유입니다.

반면에 PUSH 모델과만 결합하면 그 기술에는 한계가 있습니다. "1장, 왜 Reactive Spring인가?"에서 기억할 수 있듯이 메시지 기반 통신의 특성은 요청에 대한 응답으로 서비스가 잠재적으로 무한한 메시지 스트림을 비동기식으로 수신할 수 있다고 가정합니다. 생산자가 소비자의 처리량 가능성을 무시하면 다음 두 섹션에서 설명하는 방식으로 전체 시스템 안정성에 영향을 미칠 수 있기 때문에 이는 까다로운 부분입니다.

## 느린 생산자와 빠른 소비자

가장 간단한 것부터 시작하겠습니다. 느린 생산자와 매우 빠른 소비자가 있다고 가정합니다. 이 상황은 알려지지 않은 소비자에 대한 생산자 측의 일부 희박한 가정 때문에 발생할 수 있습니다.

한편으로 이러한 구성은 특정 비즈니스 가정입니다. 반면 실제 런타임은 다를 수 있으며 소비자의 가능성은 동적으로 변경될 수 있습니다. 예를 들어, 우리는 항상 생산자를 확장하여 생산자의 수를 늘림으로써 소비자의 부담을 증가시킬 수 있습니다.

이러한 문제를 해결하기 위해 가장 필요한 것은 실제 수요입니다. 불행히도, 순수 푸시 모델은 그러한 메트릭을 제공할 수 없으므로 시스템의 처리량을 동적으로 증가시키는 것은 불가능합니다.

## 빠른 생산자와 느린 소비자

두 번째 문제는 훨씬 더 복잡합니다. 빠른 생산자와 느린 소비자가 있다고 가정합니다.
여기서 문제는 생산자가 소비자가 처리할 수 있는 것보다 훨씬 더 많은 데이터를 보낼 수 있다는 것입니다. 이로 인해 스트레스를 받는 구성 요소에 치명적인 오류가 발생할 수 있습니다.

이러한 경우에 대한 직관적인 솔루션 중 하나는 처리되지 않은 요소를 대기열로 수집하는 것입니다.
대기열은 생산자와 소비자 사이에 있거나 소비자 측에 있을 수도 있습니다. 소비자가 바쁘더라도 이러한 기술을 사용하면 데이터의 이전 요소 또는 일부를 처리하여 새로운 데이터를 처리할 수 있습니다.

큐를 사용하여 푸시된 데이터를 처리하는 데 중요한 요소 중 하나는 적절한 특성을 가진 큐를 선택하는 것입니다.
일반적으로 다음 하위 섹션에서 고려되는 세 가지 일반적인 유형의 대기열이 있습니다.

### 무제한 대기열

가장 확실한 첫 번째 솔루션은 크기가 무제한인 대기열 또는 단순히 무제한 대기열을 제공하는 것입니다. 이 경우 생성된 모든 요소는 먼저 큐 내부에 저장된 다음 실제 구독자에 의해 비워집니다. 다음 놀라운 다이어그램은 언급된 상호 작용을 나타냅니다(다이어그램 3.4).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/d6c0477e-fb74-4c56-9b5a-22dcf3d6de2e.png)

Diagram 3.4.무제한 큐의 예

한편으로 무제한 대기열을 사용하여 메시지를 처리할 때 얻을 수 있는 핵심 이점은 `메시지 전달 가능성`입니다.
즉, 소비자는 특정 시점에 저장된 모든 요소를 처리하게 됩니다.

반면 메시지 전달에 성공하면 무한한 자원이 없기 때문에 애플리케이션의 탄력성이 떨어진다.
예를 들어, 메모리 제한에 도달하면 전체 시스템이 쉽게 손상될 수 있습니다.

### 제한된 드롭 큐

또는 메모리 오버플로를 피하기 위해 수신 메시지가 가득 차면 무시할 수 있는 대기열을 사용할 수 있습니다. 다음 대리석 다이어그램은 요소 크기가 2이고 오버플로 시 요소 삭제가 특징인 대기열을 나타냅니다(그림 3.5).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/32ce4f32-2312-4bee-86fb-9b26e43d1349.png)

도표 3.5. 2개 항목의 용량이 있는 삭제 대기열의 예

일반적으로 이 기술은 리소스의 한계를 존중하고 리소스의 용량을 기반으로 대기열의 용량을 구성하는 것을 가능하게 합니다. 결과적으로 이러한 종류의 대기열을 수용하는 것은 메시지의 중요성이 낮을 때 일반적인 관행입니다. 비즈니스 사례의 예는 데이터 세트 변경 이벤트의 스트림일 수 있습니다. 차례로, 각 이벤트는 전체 데이터 세트 집계를 사용하고 들어오는 이벤트 수량과 비교하여 상당한 시간이 걸리는 일부 통계 재계산을 트리거합니다. 이 경우 데이터 세트가 변경되었다는 사실만 중요합니다. 어떤 데이터가 영향을 받았는지 아는 것은 중요하지 않습니다.

### 제한된 차단 대기열

한편, 이 기법은 각 메시지가 유의미한 경우에는 적합하지 않을 수 있다. 예를 들어, 결제 시스템에서 각 사용자가 제출한 결제는 처리되어야 하며 일부를 삭제하는 것은 허용되지 않습니다. 결과적으로 푸시된 데이터를 처리하는 방법으로 메시지를 삭제하고 제한된 대기열을 유지하는 대신 한도에 도달하면 생산자를 차단할 수 있습니다. 생산자를 차단하는 기능이 특징인 대기열을 일반적으로 차단 대기열이라고 합니다. 세 가지 요소의 용량을 가진 차단 대기열을 사용하는 상호 작용의 예는 다음 대리석 다이어그램에 나와 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/2ec4e385-d26f-4540-ba93-0691fea7cce0.png)

Diagram 3.6. 3개의 아이템을 수용할 수 있는 Blocking 큐

불행히도 이 기술은 시스템의 모든 비동기 동작을 무효화합니다. 일반적으로 생산자는 대기열의 한계에 도달하면 차단되기 시작하고 소비자가 요소를 비우고 대기열의 여유 공간을 사용할 수 있게 될 때까지 해당 상태에 있게 됩니다. 그런 다음 가장 느린 소비자의 처리량이 시스템의 전체 처리량을 제한한다는 결론을 내릴 수 있습니다. 결과적으로 해당 기술은 비동기 동작을 부정하는 것과 함께 효율적인 리소스 활용도 거부합니다. 결과적으로 탄력성, 탄력성 및 반응성의 세 가지를 모두 달성하려면 어떤 경우도 허용되지 않습니다.

더욱이, 대기열의 존재는 시스템의 전체 설계를 복잡하게 할 수 있고 언급된 솔루션 사이의 절충점을 찾는 추가 책임을 추가할 수 있으며, 이는 또 다른 과제입니다.

일반적으로 순수 푸시 모델의 제어되지 않은 의미는 많은 원치 않는 상황을 유발할 수 있습니다. 이것이 Reactive Manifesto가 시스템이 부하에 우아하게 응답할 수 있도록 하는 메커니즘의 중요성, 즉 배압 제어 메커니즘의 필요성을 언급하는 이유입니다.

불행히도 반응형 라이브러리는 RxJava 1.x와 유사하며 이러한 표준화된 기능을 제공하지 않습니다. 기본적으로 역압을 제어할 수 있는 명시적 API는 없습니다.

---
## 해결책

2013년 말, Lightbend, Netflix 및 Pivotal의 천재 엔지니어들이 모여 설명된 문제를 해결하고 JVM 커뮤니티에 표준을 제공했습니다. 오랜 노력 끝에 세계는 Reactive Streams 사양의 첫 번째 초안을 보았습니다. 이 제안 뒤에는 특별한 것이 없었습니다. 아이디어는 이전 장에서 본 친숙한 **반응형 프로그래밍 패턴의 표준화**에 있었습니다.

### Reactive Streams 사양의 기본 사항

Reactive Streams 사양은 게시자, 구독자, 구독 및 프로세서의 네 가지 기본 인터페이스를 정의합니다.
이 이니셔티브는 어떤 조직과도 독립적으로 성장했기 때문에 모든 인터페이스가 `org.reactivestreams` 패키지 내에 있는 별도의 JAR 파일로 사용할 수 있게 되었습니다.

일반적으로 지정된 인터페이스는 이전에 사용했던 것과 유사합니다(예: RxJava 1.x에서).
어떤 면에서 이들은 RxJava의 잘 알려진 클래스를 반영합니다. 이러한 인터페이스 중 처음 두 개는 고전적인 게시자-구독자 모델과 유사한 Observable-Observer와 유사합니다. 결과적으로 처음 두 개의 이름은 게시자와 구독자로 지정되었습니다. 이 두 인터페이스가 Observable 및 Observer와 유사한지 확인하기 위해 다음 선언을 살펴보겠습니다.

```java
package org.reactivestreams;

public interface Publisher<T> {
    void subscribe(Subscriber<? super T> s);
}
```
앞의 코드는 `Publisher` 인터페이스의 내부를 보여줍니다. 구독자를 등록할 수 있는 방법은 하나뿐입니다. 유용한 DSL을 제공하기 위해 설계된 Observable과 비교하여 Publisher는 간단한 `Publisher` 및 `Subscriber` 연결을 위한 표준화된 진입점을 나타냅니다. 게시자와 달리 구독자 쪽은 RxJava의 `Observer` 인터페이스에 있는 것과 거의 동일한 장황한 API에 가깝습니다.

```java
package org.reactivestreams;

public interface Subscriber<T> {
    void onSubscribe(Subscription s);
    void onNext(T t);
    void onError(Throwable t);
    void onComplete();
}
```
RxJava Observer의 메서드와 동일한 세 가지 메서드와 함께 `onSubscribe`라는 새로운 추가 메서드를 제공합니다.

`onSubscribe` 메서드는 구독자에게 성공적인 구독을 알리는 표준화된 방법을 제공하는 개념적으로 새로운 API 메서드입니다. 차례로 해당 메서드의 들어오는 매개 변수는 `Subscription`이라는 새 계약을 소개합니다. 아이디어를 이해하기 위해 인터페이스를 자세히 살펴보겠습니다.

```java
package org.reactivestreams;

public interface Subscription {
    void request(long n);
    void cancel();
}
```
`Subscription`은 요소의 생산을 제어하기 위한 기본 사항을 제공합니다. 
RxJava 1.x의 `Subscription#unsubscribe()`와 유사하게 `cancel()` 메서드가 있어서 스트림 구독을 취소하거나 게시를 완전히 취소할 수도 있습니다. 

그러나 취소 기능과 함께 제공되는 가장 중요한 개선 사항은 새로운 요청 방법에 있습니다. 
Reactive Stream 사양은 게시자와 구독자 간의 상호 작용 기능을 확장하기 위해 `request()` 메서드를 도입했습니다. 이제 게시자에게 푸시해야 하는 데이터의 양을 알리기 위해 구독자는 `request` 방법을 통해 요청 크기를 알려야 하며 들어오는 요소의 수가 제한을 초과하지 않는지 확인할 수 있습니다. 

기본 메커니즘을 이해하기 위해 다음 다이어그램을 살펴보겠습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/d9a2fbce-24af-463d-b946-c142f469e69a.png)
Diagram 3.7. Backpressure mechanism

앞의 다이어그램에서 알 수 있듯이 게시자는 이제 구독자가 요청한 경우에만 요소의 새 부분이 전송되도록 보장합니다. 게시자의 전체 구현은 게시자에 달려 있으며, 이는 순전히 차단 대기에서 구독자의 요청에 대해서만 데이터를 생성하는 정교한 메커니즘에 이르기까지 다양합니다. 그러나 언급된 보장이 있으므로 이제 추가 대기열 비용을 지불할 필요가 없습니다.

게다가, 이 사양은 순수한 푸시 모델과 달리 배압을 적절하게 제어할 수 있는 하이브리드 푸시-풀 모델을 제공합니다.

하이브리드 모델의 힘을 이해하기 위해 데이터베이스에서 스트리밍하는 이전 예를 다시 방문하여 이러한 기술이 이전만큼 효율적인지 확인하겠습니다.

```java
public Publisher<Item> list(int count) {                           // (1)

   Publisher<Item> source = dbClient.getStreamOfItems();           // (2)
   TakeFilterOperator<Item> takeFilter = new TakeFilterOperator<>( // (2.1)
      source,
      count,
      item -> isValid(item)
   );

   return takeFilter;                                              // (3)
}
```
The key is as follows:

이것은 `list()` 메소드 선언입니다. 여기서 우리는 Reactive Streams 사양을 따르고 `Publisher<>` 인터페이스를 통신을 위한 일급 시민으로 반환합니다.

`AsyncDatabaseClient#getStreamOfItems` 메서드 실행입니다. 여기서는 Publisher<>를 반환하는 업데이트된 메서드를 사용합니다. (2.1) 지점에서 가져와야 하는 요소 수를 허용하는 Take 및 Filter 연산자의 사용자 지정 구현을 인스턴스화합니다. 또한 스트림에서 들어오는 항목의 유효성을 검사할 수 있도록 하는 사용자 지정 Predicate 구현을 전달합니다.

이 시점에서 이전에 생성된 TakeFilterOperator 인스턴스를 반환합니다. 연산자의 유형이 다르더라도 여전히 Publisher 인터페이스를 확장한다는 점을 기억하십시오.
차례로 사용자 지정 TakeFilterOperator의 내부를 명확하게 이해하는 것이 중요합니다. 다음 코드는 해당 연산자의 내부를 확장합니다.

```java
public class TakeFilterOperator<T> implements Publisher<T> {       // (1)
   ...

   public void subscribe(Subscriber s) {                           // (2)
      source.subscribe(new TakeFilterInner<>(s, take, predicate));
   }

   static final class TakeFilterInner<T> implements Subscriber<T>, // (3)
                                                    Subscription {
      final Subscriber<T> actual;
      final int take;
      final Predicate<T> predicate;
      final Queue<T> queue;
      Subscription current;
      int remaining;
      int filtered;
      volatile long requested;
      ...

      TakeFilterInner(                                             // (4)
         Subscriber<T> actual,
         int take,
         Predicate<T> predicate
      ) { ... }

      public void onSubscribe(Subscription current) {              // (5)
         ...
         current.request(take);                                    // (5.1)
         ...
      }

      public void onNext(T element) {                              // (6)
         ...
         long r = requested;
         Subscriber<T> a = actual;
         Subscription s = current;

         if (remaining > 0) {                                      // (7)
            boolean isValid = predicate.test(element);
            boolean isEmpty = queue.isEmpty();
            if (isValid && r > 0 && isEmpty) {
               a.onNext(element);                                  // (7.1)
               remaining--;
               ...
            }
            else if (isValid && (r == 0 || !isEmpty)) {
               queue.offer(element);                               // (7.2)
               remaining--;
               ...
            }
            else if (!isValid) {
               filtered++;                                         // (7.3)
            }
         }
         else {                                                    // (7.4)
            s.cancel();
            onComplete();
         }

         if (filtered > 0 && remaining / filtered < 2) {           // (8)
            s.request(take);
            filtered = 0;
         }
      }
      ...                                                          // (9)
   }
}
```

The key points of the preceding code are explained in the following list:

이것은 TakeFilterOperator 클래스 선언입니다. 이 클래스는 Publisher<>를 확장합니다. 또한 ... 뒤에는 클래스의 생성자 및 관련 필드가 숨겨져 있습니다.

이것은 Subscriber#subscribe 메소드 구현입니다. 구현을 고려하여 스트림에 추가 논리를 제공하려면 실제 구독자를 동일한 인터페이스를 확장하는 어댑터 클래스로 래핑해야 한다는 결론을 내릴 수 있습니다.

이것은 TakeFilterOperator.TakeFilterInner 클래스 선언입니다. 이 클래스는 Subscriber 인터페이스를 구현하며 메인 소스에 실제 Subscriber로 전달되기 때문에 가장 중요한 역할을 한다. 요소가 onNext에서 수신되면 필터링되어 다운스트림 구독자로 전송됩니다. 다음으로, 구독자 인터페이스와 함께 TakeFilterInner 클래스는 구독 인터페이스를 구현하여 다운스트림 구독자로 전송되어 모든 다운스트림 요구를 제어할 수 있습니다. 여기서 Queue는 크기가 동일한 ArrayBlockingQueue의 인스턴스입니다. 구독자 및 구독 인터페이스를 확장하는 내부 클래스를 만드는 기술은 중간 변환 단계를 구현하는 고전적인 방법입니다.

이것은 생성자 선언입니다. 여기에서 알 수 있듯이 take 및 predicate 매개변수와 함께 subscribe() 메서드를 호출하여 TakeFilterOperator에 구독된 실제 구독자 인스턴스가 있습니다.

이것은 Subscriber#onSubscribe 메소드 구현입니다. 여기서 가장 흥미로운 요소는 (5.1) 지점에서 찾을 수 있습니다. 여기에서 원격 데이터베이스에 대한 첫 번째 Subscription#request의 실행이 있습니다. 이는 일반적으로 첫 번째 onSubscribe 메소드 호출 중에 발생합니다.

이것은 요소 처리 선언에 필요한 유용한 매개변수 목록이 있는 Subscriber#onNext 호출입니다.

이것은 요소 선언의 처리 흐름입니다. 여기 그 처리에 4가지 핵심 사항이 있습니다. 취해야 하는 요소의 나머지 수가 0보다 높으면 실제 구독자가 데이터를 요청했고 요소가 유효하며 대기열에 요소가 없으면 해당 요소를 다운스트림으로 직접 보낼 수 있습니다(7.1). . 수요가 아직 표시되지 않았거나 대기열에 무언가가 있는 경우 해당 요소를 대기열에 넣어(주문을 보존하기 위해) 나중에 전달해야 합니다(7.2). 요소가 유효하지 않은 경우 필터링된 요소의 수를 늘려야 합니다(7.3). 마지막으로 나머지 요소 수가 0이면 (7.4) 구독을 취소하고 스트림을 완료해야 합니다.

이것은 선언을 요청하는 추가 데이터의 메커니즘입니다. 여기에서 필터링된 요소의 수가 한도에 도달하면 전체 프로세스를 차단하지 않고 데이터베이스에서 추가 데이터 부분을 요청합니다.

이것은 나머지 Subscriber 및 Subscriptions 메서드 구현입니다.
일반적으로 데이터베이스와의 연결이 유선이고 TakeFilterOperator 인스턴스가 구독을 수신하면 지정된 수의 요소가 있는 첫 번째 요청이 데이터베이스로 전송됩니다. 그 직후에 데이터베이스는 지정된 양의 요소를 생성하고 들어오는 대로 푸시하기 시작합니다. 차례로, TakeFilterOperator의 논리는 데이터의 추가 부분을 요청해야 하는 경우를 지정합니다. 그런 일이 발생하면 데이터의 다음 부분에 대한 새로운 비차단 요청이 서비스에서 데이터베이스로 전송됩니다. 여기서 Reactive Streams 사양은 Subscription#request의 호출이 방해적이지 않은 실행이어야 한다고 직접 지정한다는 점에 유의하는 것이 중요합니다. 권장하지 않습니다.

To get more information about the mentioned behavior, please see the following link: https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.2/README.md#3.4.
Finally, the following diagram depicts the overall interaction between the service and the database:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/aa9c530f-6bb6-421c-a309-3acd2c6cd68d.png)

Diagram 3.8. The hybrid Push-Pull processing flow

앞의 다이어그램에서 알 수 있듯이 데이터베이스의 첫 번째 요소는 게시자와 구독자 간의 상호 작용에 대한 Reactive Streams 사양의 계약으로 인해 조금 늦게 도착할 수 있습니다. 데이터의 새 부분을 요청하는 데 진행 중인 요소 처리를 중단하거나 차단할 필요가 없습니다. 결과적으로 전체 처리 시간은 거의 영향을 받지 않습니다.

반면에 순수 푸시 모델이 선호되는 경우도 있습니다. 다행히도 Reactive Streams는 충분히 유연합니다. 동적 푸시-풀 모델과 함께 사양은 별도의 푸시 및 풀 모델도 제공합니다. 문서에 따르면 순수한 푸시 모델을 달성하기 위해 263-1(java.lang.Long.MAX_VALUE)과 동일한 요구 사항을 요청할 수 있습니다.

이 숫자는 현재 또는 예상되는 하드웨어로 합리적인 시간 내에 263-1의 수요를 충족하는 것이 실현 가능하지 않기 때문에 무한한 것으로 간주될 수 있습니다(나노초당 1요소는 292년이 걸립니다). 따라서 게시자는 https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.2/README.md#3.17 시점 이후에 수요 추적을 중지할 수 있습니다.

대조적으로 순수 풀 모델로 전환하기 위해 `Subscriber#onNext`가 호출될 때마다 하나의 새 요소를 요청할 수 있습니다.

