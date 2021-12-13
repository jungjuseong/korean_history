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

## Reactivity for everyone

In previous chapters, we have learned a lot of exciting things about reactive programming in Spring, as well as the role RxJava plays in its story. We also looked at the need to use reactive programming to implement the reactive system. We have also seen a brief overview of the reactive landscape and available alternatives to RxJava, which makes it possible to quickly start with reactive programming.

## API's 불일치 문제

On the one hand, the extensive list of competitive libraries, such as `RxJava` and features of the Java Core library, such as `CompletableStage`, give us a choice as to the way in which we write code. For example, we may rely on reaching the API of `RxJava` in order to write a flow of the items being processed. Consequently, to build an uncomplicated asynchronous request-response interaction, it is more than enough to rely on `CompletableStage`. Alternatively, we may use framework specific classes such as `org.springframework.util.concurrent.ListenableFuture` to build an asynchronous interaction between components and simplify the work with that framework.

On the other hand, the abundance of choices may easily over-complicate the system. For example, the presence of two libraries that rely on an asynchronous non-blocking communication concept but have a different API leads to providing an additional utility class in order to convert one callback into another and vice versa:

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
The numbered points in the preceding code are explained in the following:

1. This is the async Database client's interface declaration, which is the representative sample of the possible client interface for asynchronous database access.

2. This is the ListenableFuture to CompletionStage adaptor method implementation. At point (2.1), to provide manual control of CompletionStage, we create its direct implementation called CompletableFuture via the constructor with no arguments. To provide integration with ListenableFuture, we have to add callback (2.2), where we directly reuse the API of CompletableFuture.

3. This is the CompletionStage to ListenableFuture adapter method implementation. At point (3.1) we declare the specific implementation of ListenableFuture called SettableListenableFuture. This allows us to manually supply the result of the CompletionStage execution at point (3.2).

4. This is the RestController's class declaration. Here at point (4.1), we declare the request handler method, which acts asynchronously and returns ListenableFuture to handle the result of the execution in a non-blocking fashion. In turn, to store the result of the execution of AsyncRestTemplate, we have to adapt it to the CompletionStage (4.2). Finally, to satisfy the supported API, we have to adopt the result of storing for ListenableFuture again (4.3).

As may be noticed from the preceding example, there is no direct integration with Spring Framework 4.x ListenableFuture and CompletionStage. Moreover, that example is not an exclusion case from the common usage of reactive programming. Many libraries and frameworks provide their own interfaces and classes for asynchronous communication between components, which include plain request-response communication along with the streaming processing frameworks. In many cases, to solve that problem and make several independent libraries compatible, we have to provide our own adaptation and reuse it in a few places. Moreover, our own adaptation may contain bugs and require additional maintenance. 

> In Spring Framework 5.x, ListenableFuture's API was extended and an additional method called completable was provided to solve that incompatibility. Please see the following link to learn more about that: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html#completable--.

Here, the central problem lies in the fact that there is no single method that allows library vendors to build their aligned API. For example, as we might have seen in Chapter 2, Reactive Programming in Spring - Basic Concepts, RxJava was valued by many frameworks such as Vert.x, Ratpack, Retrofit, and so on.

In turn, all of them provided support for RxJava users and introduced additional modules, which allow integrating existing projects easily. At first glance, this was wondrous since the list of projects in which RxJava 1.x was introduced is extensive and includes frameworks for web, desktop, or mobile development. However, behind that support for developer needs, many hidden pitfalls affect library vendors. The first problem that usually happens when several RxJava 1.x compatible libraries meet in one place is rough version incompatibility. Since RxJava 1.x evolved very quickly over time, many library vendors did not get the chance to update their dependency to the new releases. From time to time, updates brought many internal changes that eventually made some versions incompatible. Consequently, having different libraries and frameworks that depend on the different versions of RxJava 1 may cause some unwanted issues. The second problem is similar to the first. Customizations of RxJava are non-standardized. Here, customization refers to the ability to provide an additional implementation of Observable or a specific transformation stage, which is common during the development of RxJava extensions. Due to non-standardized APIs and rapidly evolving internals, supporting the custom implementation was another challenge.

> An excellent example of significant changes in the version may be found at the following link: https://github.com/ReactiveX/RxJava/issues/802.