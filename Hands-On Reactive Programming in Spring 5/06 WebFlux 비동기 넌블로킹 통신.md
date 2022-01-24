# 06 WebFlux 비동시 넌블로킹 통신

In the previous chapter, we started to take a look at Spring Boot 2.x. We saw that a lot of useful updates and modules have arrived with the fifth version of the Spring Framework, and we also looked at the Spring WebFlux module.

In this chapter, we are going to take a look at that module in detail. We will compare the internal design of WebFlux with good old Web MVC and try to understand the strengths and weaknesses of both. We are also going to build a simple web application with WebFlux.

This chapter covers the following topics:

- A bird's-eye view of Spring WebFlux
- Spring WebFlux versus Spring Web MVC
- A comprehensive design overview of Spring WebFlux

## WebFlux는 반응형 서버 토대의 중심

1장, 왜 Reactive Spring인가? 4장, Project Reactor - Reactive Apps의 기초에서 보았듯이 애플리케이션 서버의 새로운 시대는 개발자를 위한 새로운 기술을 가져왔습니다. 웹 애플리케이션 분야에서 Spring Framework의 진화 초기부터 Spring 웹 모듈을 Java EE의 Servlet API와 통합하기로 결정했습니다. Spring Framework의 전체 인프라는 Servlet API를 중심으로 구축되며 밀접하게 결합되어 있습니다. 예를 들어 Spring Web MVC 전체는 Front Controller 패턴을 기반으로 합니다. 그 패턴은 javax.servlet.http.HttpServlet 클래스를 간접적으로 확장하는 `org.springframework.web.servlet.DispatcherServlet` 클래스에 의해 Spring Web MVC에서 구현된다.

반면에 Spring Framework는 주석 기반 컨트롤러와 같은 많은 기능을 위한 빌딩 블록인 Spring 웹 모듈에서 더 나은 수준의 추상화를 제공합니다. 이 모듈은 공통 인터페이스를 구현과 부분적으로 분리하지만 Spring Web의 초기 디자인도 동기식 상호 작용 모델을 기반으로 하므로 IO를 차단합니다. 그럼에도 불구하고 이러한 분리는 좋은 기초이므로 반응형 웹을 살펴보기 전에 웹 모듈의 디자인을 요약하고 여기에서 무슨 일이 일어나고 있는지 이해해 보겠습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/7b3705f3-111f-45e8-a623-2b9f02cbbcec.png)

Diagram 6.1. The implementation of the web stack in the Spring WebMVC module

다음은 앞의 다이어그램에 대한 설명입니다.

1. 들어오는 요청은 기본 서블릿 컨테이너에 의해 처리됩니다. 여기에서 서블릿 컨테이너는 들어오는 본문을 서블릿 API의 ServletRequest 인터페이스로 변환하고 ServletResponse 인터페이스의 형태로 출력을 준비하는 역할을 합니다.

2. FilterChain에는 필터를 통해 ServletRequest를 필터링하는 단계가 결합되어 있습니다.

3. 다음 단계는 DispatcherServlet 처리 단계입니다. DispatcherServlet은 Servlet 클래스를 확장한다는 것을 기억하십시오. 또한 HandlerMappings(4), HandlerAdapters(5) 및 ViewResolvers(스키마에 표시되지 않음) 목록을 보유하고 있습니다. 현재 실행 흐름의 컨텍스트에서 DispatcherServlet 클래스는 HandlerMapping 인스턴스를 검색하고 적절한 HandlerAdapter를 사용하여 이를 적용합니다. 그런 다음 DispatcherServlet이 HandlerMapping 및 HandlerAdapter 실행 결과의 렌더링을 시작하도록 View를 확인할 수 있는 ViewResolver를 검색합니다.

4. 그 다음에는 HandlerMapping 단계가 있습니다. DispatcherServlet(3)은 애플리케이션 컨텍스트에서 모든 HandlerMapping 빈을 검색합니다. 매핑 초기화 과정에서 스캐닝 과정에서 발견된 모든 인스턴스는 순서대로 정렬됩니다. 주문 번호는 @Order 주석에 의해 지정되거나 HandlerMapping이 Ordered 인터페이스를 구현하는 경우에 지정됩니다. 이 때문에 적절한 HandlerMapping 인스턴스에 대한 조회는 이전에 설정된 순서에 따라 달라집니다. 앞의 다이어그램에는 몇 가지 일반적인 HandlerMapping 인스턴스가 나와 있습니다. 가장 친숙한 것은 주석 기반 프로그래밍 모델을 가능하게 하는 RequestMappingHandlerMapping입니다.

5. 마지막으로 들어오는 ServletRequest를 @Controller 주석이 달린 객체에 적절하게 바인딩하는 RequestMappingHandlerAdapter 단계가 있습니다. RequestMappingHandlerAdapter는 또한 요청 유효성 검사, 응답 변환 및 Spring Web MVC 프레임워크를 일상적인 웹 개발에 유용하게 만드는 기타 많은 유용한 기능을 제공합니다.

전체 디자인은 컨테이너 내부의 모든 매핑된 서블릿을 처리하는 책임이 있는 기본 서블릿 컨테이너에 의존합니다. DispatchServlet은 유연하고 고도로 구성 가능한 Spring 웹 인프라와 무겁고 복잡한 Servlet API 간의 통합 지점 역할을 합니다. HandlerMapping의 구성 가능한 추상화는 Servlet API에서 컨트롤러 및 빈과 같은 최종 비즈니스 로직을 분리하는 데 도움이 됩니다.

> **Tip**
> 
> Spring MVC는 매핑, 바인딩 및 유효성 검사 기능과 함께 `HttpServletRequest` 및 `HttpServletResponse`와의 직접적인 상호 작용을 지원합니다. 그러나 이러한 클래스를 사용할 때 Servlet API에 대한 추가 직접적인 의존성이 있습니다. 이것은 Web MVC에서 WebFlux 또는 Spring용 다른 웹 확장으로의 마이그레이션 프로세스를 복잡하게 만들 수 있습니다. 대신 `org.springframework.http.RequestEntity` 및 `org.springframework.http.ResponseEntity`를 사용하는 것이 좋습니다. 이러한 클래스는 웹 서버 구현에서 요청 및 응답 개체를 분리합니다.

Spring Web MVC 접근 방식은 수년간 편리한 프로그래밍 모델이었습니다. 웹 애플리케이션 개발을 위한 견고하고 안정적인 골격임이 입증되었습니다. 이것이 2003년에 Spring Framework가 Servlet API 위에 웹 애플리케이션을 구축하기 위한 가장 인기 있는 솔루션 중 하나가 되기 시작한 이유입니다. 그러나 과거의 방법론과 기술은 현대의 데이터 집약적 시스템의 요구 사항에 잘 맞지 않습니다.

Servlet API가 비동기식 넌블로킹 통신(버전 3.1부터)을 지원한다는 사실에도 불구하고 Spring MVC 모듈의 구현에는 많은 간격이 있고 요청 수명 주기 전체에 걸쳐 넌블로킹 작업을 허용하지 않습니다. 예를 들어 즉시 사용 가능한 넌블로킹 HTTP 클라이언트가 없으므로 외부 상호 작용으로 인해 블로킹 IO 호출이 발생할 가능성이 큽니다. "5장, Spring Boot 2로 반응하기"에서 언급했듯이 Web MVC 추상화는 넌블로킹 Servlet API 3.1의 모든 기능을 지원하지 않습니다. 그렇게 되기 전까지는 Spring Web MVC는 고부하 프로젝트를 위한 프레임워크로 간주될 수 없습니다. 이전 Spring에서 웹 추상화의 또 다른 단점은 Spring 웹 기능을 재사용하거나 Netty와 같은 서블릿이 아닌 서버에 대한 프로그래밍 모델을 유연하게 사용할 수 없다는 것입니다.

그렇기 때문에 지난 몇 년 동안 Spring Framework 팀의 핵심 과제는 동일한 주석 기반 프로그래밍 모델을 허용하고 비동기식 논블로킹 서버의 모든 이점을 동시에 제공하는 새로운 솔루션을 구축하는 것이었습니다.


## 반응형 웹 코어

새로운 Spring 에코시스템을 위한 새로운 비동기식 넌블로킹 웹 모듈에 대해 작업하고 있다고 상상해 봅시다. 새로운 반응형 웹 스택은 어떤 모습이어야 합니까? 먼저 기존 솔루션을 분석하고 개선하거나 제거해야 할 부분을 강조해 보겠습니다.

Spring MVC의 내부 API는 잘 설계되어 있으므로 API에 추가할 것은 Servlet API에 대한 직접적인 의존성입니다. 따라서 최종 솔루션은 Servlet API와 유사한 인터페이스를 가져야 합니다. 
반응형 스택을 설계하기 위한 첫 번째 단계는 `javax.servlet.Servlet#service`를 들어오는 요청에 반응하는 유사한 인터페이스 및 메서드로 바꾸는 것입니다. 서버의 응답에 대한 클라이언트의 요청을 교환하는 Servlet API의 방식도 개선되고 사용자 정의되어야 합니다.

자체 API를 도입하면 서버 엔진과 구체적인 API에서 분리할 수 있지만 반응형 통신을 설정하는 데 도움이 되지는 않습니다. 따라서 모든 새 인터페이스는 요청 본문 및 반응 형식의 세션과 같은 모든 데이터에 대한 액세스를 제공해야 합니다.  Reactive Streams 모델을 사용하면 가용성과 수요에 따라 데이터와 상호 작용하고 데이터를 처리할 수 있습니다. Project Reactor는 Reactive Streams 표준을 따르고 함수형 관점에서 광범위한 API를 제공하므로 그 위에 모든 반응 웹 API를 구축하는 데 적절한 도구가 될 수 있습니다.

마지막으로 실제 구현에서 이러한 것들을 결합하면 다음 코드가 나옵니다.

```java
interface ServerHttpRequest { // (1) 
   ...                                                             
   Flux<DataBuffer> getBody(); // (1.1)
   ...                                                             
}                                                                  

interface ServerHttpResponse { // (2)
   ...                                                             
   Mono<Void> writeWith(Publisher<? extends DataBuffer> body); // (2.1)
   ...                                                             
}                                                                  

interface ServerWebExchange { // (3)
   ...                                                             
   ServerHttpRequest getRequest(); // (3.1)
   ServerHttpResponse getResponse(); // (3.2)
   ...                                                             
   Mono<WebSession> getSession(); // (3.3)
   ...                                                             
}                                                                  
```
The preceding code can be explained as follows:

(1) 요청 메시지를 나타내는 인터페이스 입니다. 입력 바이트에 대한 액세스의 추상화가 Flux이며, 이는 정의상 반응형 액세스 권한이 있습니다. DataBuffer에 대한 유용한 추상화로는 특정 서버 구현과 데이터를 교환하는 방법입니다. 요청 본문과 함께 모든 HTTP 요청에는 일반적으로 들어오는 헤더, 경로, 쿠키 및 쿼리 매개변수에 대한 정보가 포함되어 있으므로 정보가 해당 인터페이스나 하위 인터페이스에서 별도의 메서드로 표현될 수 있습니다.

(2) 응답 인터페이스입니다. `ServerHttpResponse#writeWith` 메서드는 `Publisher<? DataBuffer>` 클래스를 확장합니다. 이 경우 게시자 반응 유형은 더 많은 유연성을 제공하고 특정 반응 라이브러리에서 분리됩니다. 따라서 인터페이스의 구현을 사용하고 프레임워크에서 비즈니스 로직을 분리할 수 있습니다. 이 메서드는 네트워크에 데이터를 보내는 비동기식 프로세스를 나타내는 `Mono<Void>`를 리턴합니다. 여기서 중요한 점은 주어진 Mono를 구독할 때만 데이터를 보내는 프로세스가 실행된다는 것입니다. 또한 수신 서버는 전송 프로토콜의 제어 흐름에 따라 배압을 제어할 수 있습니다.

(3) `ServerWebExchange` 인터페이스는 HTTP 요청-응답 인스턴스(3.1 및 3.2)의 컨테이너 역할을 합니다. 인터페이스는 인프라 구조이며 HTTP 상호 작용뿐 아니라 프레임워크와 관련된 정보를 보유할 수 있습니다. 예를 들어, 여기에는 (3.3) 지점에 표시된 것처럼 들어오는 요청에서 복원된 WebSession에 대한 정보가 포함될 수 있습니다. 또는 요청 및 응답 인터페이스 위에 추가 인프라 메서드를 제공할 수 있습니다.

앞의 예에서 반응형 웹 스택에 대한 잠재적인 인터페이스를 작성했습니다. 일반적으로 이 세 가지 인터페이스는 Servlet API에 있는 것과 유사합니다. 예를 들어, ServerHttpRequest와 ServerHttpResponse는 ServletRequest와 ServletResponse를 생각나게 합니다. 

기본적으로 반응형 대응물은 상호 작용 모델의 관점에서 거의 동일한 방법을 제공하는 것을 목표로 합니다. 그러나 Reactive Streams의 비동기 및 넌블로킹 특성으로 인해 즉시 사용 가능한 스트리밍 기반과 복잡한 콜백 기반 API로부터 보호합니다. 이것은 또한 콜백 지옥으로부터 우리를 보호합니다.

중앙 인터페이스를 제외하고 전체 상호 작용 흐름을 수행하려면 다음과 같은 요청-응답 핸들러 및 필터 API를 정의해야 합니다.

```java
interface WebHandler { // (1)
   Mono<Void> handle(ServerWebExchange exchange);                  
}                                                                  

interface WebFilterChain { // (2)
   Mono<Void> filter(ServerWebExchange exchange);                  
}                                                                  

interface WebFilter { // (3)
   Mono<Void> filter(ServerWebExchange exch, WebFilterChain chain);
}                                                                  
```
The numbered sections in the preceding code can be described as follows:

(1) **이것은 WebHandler라고 하는 모든 HTTP 상호 작용의 중앙 진입점입니다.** 이 인터페이스는 추상 DispatcherServlet의 역할을 하므로 그 위에 모든 구현을 빌드할 수 있습니다. 인터페이스의 책임은 요청의 핸들러를 찾은 다음 실행 결과를 ServerHttpResponse에 쓰는 뷰의 렌더러로 구성하는 것이기 때문에 DispatcheServlet#handle 메서드는 결과를 리턴할 필요가 없습니다. 그러나 처리가 완료되면 알림을 받는 것이 유용할 수 있습니다. 이와 같은 알림 신호에 의존하여 처리 시간 초과를 적용하여 지정된 기간 내에 신호가 나타나지 않으면 실행을 취소할 수 있습니다. 이러한 이유로 메서드는 Void에서 Mono를 리턴하므로 반드시 결과를 처리하지 않고도 비동기 처리가 완료될 때까지 기다릴 수 있습니다.

(3) Servlet API와 유사하게 몇 개의 WebFilter 인스턴스를 체인으로 연결할 수 있는 인터페이스입니다. 이것은 반응형 필터 표현입니다.

앞의 인터페이스는 프레임워크의 나머지 부분에 대한 비즈니스 로직을 구축하기 시작할 수 있는 토대를 제공합니다.

반응형 웹 인프라의 추상화 계층을 완성하려면 반응형 HTTP 요청 처리를 위한 가장 낮은 수준의 계약이 필요합니다. 이전에 데이터 전송 및 처리를 담당하는 인터페이스만 정의했기 때문에 정의된 인프라에 서버 엔진을 적용하는 역할을 담당하는 인터페이스를 정의해야 합니다. 이를 위해서는 `ServerHttpRequest` 및 `ServerHttpResponse`와의 직접적인 상호 작용을 담당하는 추가 수준의 추상화가 필요합니다.

또한 이 계층은 `ServerWebExchange` 구축을 담당해야 합니다. 특정 세션 저장소, 로케일 확인자 및 유사한 인프라가 여기에 보관됩니다.

```java
public interface HttpHandler {
    Mono<Void> handle(
      ServerHttpRequest request,
      ServerHttpResponse response);
}
```
마지막으로 각 서버 엔진에 대해 미들웨어의 HttpHandler를 호출하는 적응이 있을 수 있습니다. 그러면 미들웨어는 주어진 `ServerHttpResponse` 및 `ServerHttpRequest`를 구성하여 `ServerWebExchange`에 보내고 이를 `WebFilterChain` 및 `WebHandler`에 전달합니다. 이러한 디자인에서는 Spring WebFlux 사용자에게 특정 서버 엔진이 어떻게 작동하는지 중요하지 않습니다. 이제 우리는 서버 엔진의 세부 사항을 숨기는 적절한 수준의 추상화를 갖게 되었기 때문입니다. 

이제 다음 단계로 이동하여 높은 수준의 반응 추상화를 구축할 수 있습니다.


## 반응형 웹 및 MVC 프레임워크

Spring Web MVC 모듈의 주요 기능은 주석 기반 프로그래밍 모델입니다. 따라서 핵심 과제는 반응형 웹 스택에 대해 동일한 개념을 제공하는 것입니다. 현재 Spring Web MVC 모듈을 보면 일반적으로 모듈이 제대로 설계되었음을 알 수 있다. 새로운 반응형 MVC 인프라를 구축하는 대신 기존 인프라를 재사용하고 동기 통신을 Flux, Mono 및 Publisher와 같은 반응형 유형으로 교체할 수 있습니다. 예를 들어, 요청을 매핑하고 컨텍스트 정보(예: 헤더, 쿼리 매개변수, 속성 및 세션)를 발견된 핸들러에 바인딩하기 위한 두 개의 중앙 인터페이스는 HandlerMapping 및 HandlerAdapter입니다. 

일반적으로 Spring Web MVC에서와 동일한 `HandlerMapping` 및 `HandlerAdapter` 체인을 유지할 수 있지만 Reactor 유형을 사용하여 즉시 명령형을 반응형으로 대체할 수 있습니다.

```java
interface HandlerMapping { // (1)
/* HandlerExecutionChain getHandler(HttpServletRequest request) */ // (1.1)
   Mono<Object> getHandler(ServerWebExchange exchange); // (1.2)
}                                                                  

interface HandlerAdapter { // (2)
   boolean supports(Object handler); 
                                                                   
/* ModelAndView handle( // (2.1)
      HttpServletRequest request, HttpServletResponse response,    
      Object handler                                               
   ) */                                                            
   Mono<HandlerResult> handle( // (2.2)
      ServerWebExchange exchange,                                  
      Object handler                                               
   );                                                              
}                                                                  
```
The preceding code is explained in the following numbered list:

(1) 이것은 반응형 `HandlerMapping` 인터페이스의 선언입니다. 여기에서 이전 웹 MVC 구현과 개선된 구현 간의 차이점을 강조하기 위해 코드에 두 메서드의 선언이 모두 포함되어 있습니다. 방법은 매우 유사하며 차이점은 Mono 유형을 리턴하므로 반응형 동작을 활성화한다는 것입니다.

(2) 반응형 `HandlerAdapter` 인터페이스 버전입니다. 핸들 메서드의 반응형 버전은 `ServerWebExchange` 클래스가 요청과 응답 인스턴스를 동시에 결합하기 때문에 좀 더 간결합니다. 지점(2.2)에서 메서드는 `ModelAndView`(2.1에 있음) 대신 `HandlerResult`의 Mono를 리턴합니다. 기억할 수 있듯이 `ModelAndView`는 상태 코드, 모델 및 보기와 같은 정보를 제공하는 역할을 합니다. HandlerResult 클래스는 상태 코드를 제외하고 동일한 정보를 포함합니다. `HandlerResult`는 직접 실행의 결과를 제공하기 때문에 더 좋기 때문에 DispatcherHandler가 핸들러를 찾기가 더 쉽습니다. Web MVC에서 View는 템플릿과 개체를 렌더링하는 역할을 합니다. 또한 결과를 렌더링하므로 Web MVC에서의 목적이 약간 불분명할 수 있습니다. 불행히도 이러한 다중 책임은 비동기식 결과 처리에 쉽게 적응할 수 없습니다. 이러한 경우 결과가 일반 Java 객체일 때 View 조회는 해당 클래스의 직접적인 책임이 아닌 HandlerAdapter에서 수행됩니다. 이 때문에 책임을 명확히 하는 것이 좋으므로 앞의 코드에서 구현한 변경은 개선이다.

이러한 단계를 따르면 전체 실행 계층을 손상시키지 않고 반응형 상호 작용 모델을 제공하므로 기존 디자인을 유지하고 최소한의 변경으로 기존 코드를 잠재적으로 재사용할 수 있습니다.

마지막으로, 실제 구현을 고려하여 반응형 웹 스택을 달성하고 요청의 처리 흐름을 수정하기 위해 지금까지 취한 모든 단계를 수집하여 다음과 같은 디자인을 제시합니다.


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/17476bf8-33e8-44d0-bfe2-2badff02676a.png)

Diagram 6.2.재설계된 반응형 웹 및 MVC 스택

앞의 다이어그램은 다음과 같이 설명할 수 있습니다.

(1) 기본 서버 엔진에서 처리되는 들어오는 요청입니다.(서버 엔진에는 Netty 및 Undertow와 같은 엔진을 포함) 여기에서 각 서버 엔진에는 HTTP 요청 및 HTTP 응답의 내부 표현을 ServerHttpRequest 및 ServerHttpResponse에 매핑하는 자체의 반응형 어댑터가 있습니다.

(2) 주어진 `ServerHttpRequest`, `ServerHttpResponse`, 사용자의 Session 및 관련 정보를 `ServerWebExchage` 인스턴스로 구성하는 `HttpHandler` 단계입니다.

(3) 이 시점에서 정의된 WebFilter를 체인으로 구성하는 `WebFilterChain` 단계가 있습니다. 그런 다음 WebFilterChain은 들어오는 ServerWebExchange를 필터링하기 위해 이 체인에 있는 각 WebFilter 인스턴스의 `WebFilter#filter` 메서드를 실행합니다.

(4) 모든 필터 조건이 충족되면 WebFilterChain은 WebHandler 인스턴스를 호출합니다.

(5) 다음 단계는 HandlerMapping의 인스턴스를 조회하고 첫 번째 적절한 인스턴스를 호출하는 것입니다. 이 예에서는 `RouterFunctionMapping`, 잘 알려진 `RequestMappingHandlerMapping` 및 HandlerMa`pping 리소스와 같은 몇 가지 `HandlerMapping` 인스턴스를 설명했습니다. 여기에서 새로운 `HandlerMapping` 인스턴스는 WebFlux 모듈에 도입된 `RouterFunctionMapping`이며 순수한 함수형 요청 처리를 넘어선 것입니다. 여기서는 해당 기능에 대해 자세히 설명하지 않습니다. 다음 섹션에서 이에 대해 다룰 것입니다.

(6) 이것은 `RequestMappingHandlerAdapter` 단계로 이전과 동일한 기능을 갖지만 이제 반응형 상호 작용 흐름을 구축하기 위해 반응형 스트림을 사용합니다.

앞의 다이어그램은 WebFlux 모듈의 기본 상호 작용 흐름에 대한 단순화된 보기만 보여줍니다. WebFlux 모듈에서 기본 서버 엔진은 Netty입니다. Netty 서버는 반응형 공간에서 널리 사용되기 때문에 적절한 기본값입니다. 또한 해당 서버 엔진은 클라이언트와 서버 모두 비동기식 넌블로킹 상호작용을 제공합니다. 이것은 Spring WebFlux가 제공하는 반응형 프로그래밍 패러다임에 더 낫다는 것을 의미합니다. Netty가 훌륭한 기본 서버 엔진이라는 사실에도 불구하고 WebFlux를 사용하면 서버 엔진을 선택할 수 있는 유연성이 있습니다. 즉, Undertow, Tomcat, Jetty 또는 기타 Servlet API 기반과 같은 다양한 최신 서버 엔진 간에 쉽게 전환할 수 있습니다. 보시다시피 WebFlux 모듈은 Spring Web MVC 모듈의 아키텍처를 반영하므로 이전 웹 프레임워크에 대한 경험이 있는 사람들이 쉽게 이해할 수 있습니다. 또한 Spring Web Flux 모듈에는 다음 섹션에서 다룰 숨겨진 보석이 많이 있습니다.

## WebFlux를 사용한 순수 함수형 웹
앞의 다이어그램에서 알 수 있듯이 Web MVC와 많은 유사점이 있지만 WebFlux는 많은 새로운 기능도 제공합니다. 소규모 마이크로서비스, Amazon Lambda 및 유사한 클라우드 서비스의 시대에는 개발자가 거의 동일한 프레임워크 기능을 갖춘 경량 애플리케이션을 만들 수 있도록 하는 기능을 제공하는 것이 중요합니다. Vert.x 또는 Ratpack과 같은 경쟁자 프레임워크를 더욱 매력적으로 만든 기능 중 하나는 함수형 경로 매핑과 복잡한 요청 라우팅 논리를 작성할 수 있는 내장 API를 통해 달성된 경량 애플리케이션을 생성하는 기능이었습니다. 이것이 Spring Framework 팀이 이 기능을 WebFlux 모듈에 통합하기로 결정한 이유입니다. 더욱이, 순수 함수형 라우팅의 조합은 새로운 반응 프로그래밍 접근 방식에 충분히 적합합니다. 예를 들어, 새로운 함수형 접근 방식을 사용하여 복잡한 라우팅을 구축하는 방법을 살펴보겠습니다.

```java
import static ...RouterFunctions.nest; // (1)
import static ...RouterFunctions.nest;                             
import static ...RouterFunctions.route;                            
...
import static ...RequestPredicates.GET; // (2)
import static ...RequestPredicates.POST;                           
import static ...RequestPredicates.accept;                         
import static ...RequestPredicates.contentType;                    
import static ...RequestPredicates.method;                         
import static ...RequestPredicates.path;                           

@SpringBootApplication
public class DemoApplication {                                     
   ...                                                             
   @Bean                                                           
   public RouterFunction<ServerResponse> routes(OrderHandler handler) {//(4)                                                      
      return 
         nest(path("/orders"), // (5)
            nest(accept(APPLICATION_JSON),                         
               route(GET("/{id}"), handler::get)                   
               .andRoute(method(HttpMethod.GET), handler::list)    
            )                                                      
            .andNest(contentType(APPLICATION_JSON),                
               route(POST("/"), handler::create)                   
            )                                                      
         );
   }
}
```
(1) `RouterFunctions` 클래스는 다양한 동작으로 RouterFunction 인터페이스를 리턴하는 광범위한 팩토리 메소드를 제공합니다.

(2) `RequestPredicates` 클래스를 사용하면 들어오는 요청을 다른 관점에서 확인할 수 있습니다. 일반적으로 RequestPredicates는 함수형 인터페이스인 RequestPredicate 인터페이스의 다른 구현에 대한 액세스를 제공하며 사용자 지정 방식으로 들어오는 요청을 확인하기 위해 쉽게 확장될 수 있습니다.

(4) RouterFunction<ServerResponse> 빈을 초기화하는 메소드 선언입니다.

(5) RouterFunction 및 RequestPredicates API의 지원으로 표현된 RouterFunction의 선언입니다.

앞의 예에서는 애플리케이션의 웹 API를 선언하는 다른 방법을 사용했습니다. 이 기술은 핸들러 선언을 위한 함수형 메소드를 제공하고 명시적으로 정의된 모든 경로를 한 곳에서 유지할 수 있도록 합니다. 또한 이전에 사용된 것과 같은 API를 사용하면 자체 요청 술어를 쉽게 작성할 수 있습니다. 

다음 코드는 사용자 지정 `RequestPredicate`를 구현하고 라우팅 로직에 적용하는 방법을 보여줍니다.
`Redirect-Traffic` 쿠키가 있는 경우 트래픽을 다른 서버로 리디렉션하는 RouterFunction을 만들었습니다.

```java
nest((serverRequest) -> serverRequest.cookies()
      .containsKey("Redirect-Traffic"),
      route(all(), serverRedirectHandler)
)
```

새로운 함수형 웹은 또한 요청 및 응답을 처리하는 새로운 방법을 도입했습니다. 
예를 들어 다음 코드 샘플은 `OrderHandler` 구현의 일부를 보여줍니다.

```java
class OrderHandler {
   final OrderRepository orderRepository;                          
   ...                                                             
   public Mono<ServerResponse> create(ServerRequest request) { // (2)
      return request
         .bodyToMono(Order.class) // (2.1)
         .flatMap(orderRepository::save)                           
         .flatMap(o ->                                             
            ServerResponse.created(URI.create("/orders/" + o.id))  // (2.2)
                          .build()                                 
         );                                                        
    }                                                              
    ...                                                            
}                                                                  
```
앞의 코드는 다음과 같이 설명할 수 있습니다.

(2) `create()` 메서드는 함수형 경로 요청 유형에 특정한 ServerRequest를 수락합니다. ServerRequest는 API를 노출하여 요청 본문을 Mono 또는 Flux에 수동으로 매핑할 수 있습니다. 또한 API를 사용하면 요청 본문이 매핑되어야 하는 클래스를 지정할 수 있습니다. 마지막으로 WebFlux의 기능 추가는 ServerResponse 클래스의 유창한 API를 사용하여 응답을 구성할 수 있는 API를 제공합니다.

함수형 경로 선언을 위한 API 외에도 요청 및 응답 처리를 위한 함수형 API가 있습니다.

새 API가 핸들러 및 매핑을 선언하는 함수형 접근 방식을 제공하지만 완전히 가벼운 웹 애플리케이션을 제공하지는 않습니다. Spring 생태계의 전체 기능이 중복되어 애플리케이션의 전체 시작 시간이 감소하는 경우가 있습니다. 

예를 들어 사용자 암호 일치를 담당하는 서비스를 빌드해야 한다고 가정합니다. 일반적으로 이러한 서비스는 입력된 암호를 해시한 다음 저장된 암호와 비교하여 CPU를 많이 소모합니다. 우리에게 필요한 유일한 기능은 `PasswordEncoder#matchs` 메소드를 사용하여 인코딩된 비밀번호를 원시 비밀번호와 비교할 수 있게 해주는 Spring Security 모듈의 `PasswordEncoder` 인터페이스입니다. 따라서 IoC, 주석 처리 및 자동 구성이 포함된 전체 Spring 인프라는 중복되고 애플리케이션을 로딩을 느리게 만듭니다.

다행히도 새로운 기능의 웹 프레임워크를 사용하면 전체 Spring 인프라를 시작하지 않고도 웹 애플리케이션을 구축할 수 있습니다. 이를 달성할 수 있는 방법을 이해하기 위해 다음 예를 살펴보겠습니다.

```java
class StandaloneApplication { // (1)

   public static void main(String[] args) { // (2)
      HttpHandler httpHandler = RouterFunctions.toHttpHandler( // (2.1)
         routes(new BCryptPasswordEncoder(18)) // (2.2)
      );                                                           
      ReactorHttpHandlerAdapter reactorHttpHandler = // (2.3)
         new ReactorHttpHandlerAdapter(httpHandler);               

      HttpServer.create() // (3)
                .port(8080) // (3.1)
                .handle(reactorHttpHandler) // (3.2)
                .bind() // (3.3)
                .flatMap(DisposableChannel::onDispose) // (3.4)
                .block();
   }                                                               

   static RouterFunction<ServerResponse> routes( // (4)
      PasswordEncoder passwordEncoder                              
   ) {                                                             
      return
         route(POST("/check"), // (5)
            request -> request                                     
               .bodyToMono(PasswordDTO.class) // (5.1)
               .map(p -> passwordEncoder
                  .matches(p.getRaw(), p.getSecured())) // (5.2)
               .flatMap(isMatched -> isMatched // (5.3)
                  ? ServerResponse                                 
                       .ok()                                       
                       .build()                                    
                  : ServerResponse                                 
                       .status(HttpStatus.EXPECTATION_FAILED)      
                       .build()                                    
               )                                                   
         );                                                        
   }
}
```
다음 번호 목록은 앞의 코드 샘플을 설명합니다.

(1) 이것은 주요 응용 프로그램 클래스의 선언입니다. 보시다시피 Spring Boot에는 추가 주석이 없습니다.

(2) 여기에 필요한 변수의 초기화와 함께 메인 메소드의 선언이 있습니다. 지점(2.2)에서 route 메소드를 호출한 다음 RouterFunction을 HttpHandler로 변환합니다. 그런 다음 (2.3) 지점에서 `ReactorHttpHandlerAdapter`라는 내장 HttpHandler 어댑터를 사용합니다.

(3) Reactor-Netty API의 일부인 HttpServer 인스턴스를 생성합니다. 여기서는 서버를 설정하기 위해 HttpServer 클래스의 유창한 API를 사용합니다. 지점(3.1)에서 포트를 선언하고 생성된 ReactorHttpHandlerAdapter 인스턴스(3.2 지점)를 넣고 지점(3.3)에서 bind를 호출하여 서버 엔진을 시작합니다. 마지막으로 애플리케이션을 활성 상태로 유지하기 위해 메인 스레드를 차단하고 지점(3.4)에서 생성된 서버의 폐기 이벤트를 수신합니다.

(4) 이 지점은 route 메소드의 선언을 보여줍니다.

(5) /check 경로가 있는 POST 메서드에 대한 요청을 처리하는 경로 매핑 논리입니다. 여기에서는 `bodyToMono` 메서드를 지원하여 들어오는 요청을 매핑하는 것으로 시작합니다. 그런 다음 본문이 변환되면 인코딩된 암호와 비교하여 원시 암호를 확인하기 위해 PasswordEncoder 인스턴스를 사용합니다(우리의 경우 18번의 해싱이 포함된 강력한 BCrypt 알고리즘을 사용합니다. 이 알고리즘은 인코딩/ 매칭) (5.2). 마지막으로 비밀번호가 저장된 비밀번호와 일치하면 ServerResponse는 비밀번호가 저장된 비밀번호와 일치하지 않으면 OK(200) 또는 EXPECTATION_FAILED(417) 상태를 리턴합니다.

앞의 예제는 전체 Spring Framework 인프라를 실행할 필요 없이 웹 애플리케이션을 얼마나 쉽게 설정할 수 있는지 보여줍니다. 이러한 웹 응용 프로그램의 이점은 시작 시간이 훨씬 짧다는 것입니다. 애플리케이션의 시작 시간은 약 0.7초인 반면, Spring Framework 및 Spring Boot 인프라가 있는 동일한 애플리케이션의 시작 프로세스는 최대 2초가 소요되며 이는 약 3배 더 느립니다.

시작 시간은 다를 수 있지만 전체 비율은 동일해야 합니다.

함수형 경로 선언으로 전환하여 라우팅 선언 기술을 요약하기 위해 모든 라우팅 구성을 한 곳에서 유지하고 들어오는 요청 처리에 대해 반응적 접근 방식을 사용합니다. 동시에 이러한 기술은 들어오는 요청 매개변수, 경로 변수 및 요청의 기타 중요한 구성 요소에 액세스하는 측면에서 일반적인 주석 기반 접근 방식과 거의 동일한 유연성을 제공합니다. 또한 전체 Spring Framework 인프라가 실행되는 것을 피할 수 있는 기능을 제공하고 경로 설정 측면에서 동일한 유연성을 가지므로 애플리케이션의 부트스트래핑 시간을 최대 3배까지 줄일 수 있습니다.

## WebClient와의 넌블로킹 서비스 간 통신

이전 섹션에서는 새 Spring WebFlux 모듈의 기본 설계 및 변경 사항에 대한 개요를 살펴보고 `RoutesFunction`을 사용한 새로운 함수형 접근 방식에 대해 배웠습니다. 그러나 Spring WebFlux에는 다른 새로운 가능성도 포함되어 있습니다. 가장 중요한 소개 중 하나는 `WebClient`라고 하는 새로운 넌블로킹 HTTP 클라이언트입니다.

본질적으로 `WebClient`는 이전 `RestTemplate`에 대한 반응형 대체입니다. 그러나 WebClient에는 반응형 접근 방식에 더 잘 맞는 함수형 API가 있으며 Flux 또는 Mono와 같은 Project Reactor 유형에 대한 기본 제공 매핑을 제공합니다. WebClient에 대해 자세히 알아보기 위해 다음 예제를 살펴보겠습니다.

```java
WebClient.create("http://localhost/api") // (1)
         .get() // (2)
         .uri("/users/{id}", userId) // (3)
         .retrieve() // (4)
         .bodyToMono(User.class) // (5)
         .map(...) // (6)
         .subscribe();
```
(1) 앞의 예에서는 1번 지점에 표시된 create라는 팩토리 메서드를 사용하여 WebClient 인스턴스를 만듭니다. 여기에서 create 메서드를 사용하면 향후 모든 HTTP 호출에 내부적으로 사용되는 기본 URI를 지정할 수 있습니다. 그런 다음 원격 서버에 대한 호출 구축을 시작하기 위해 HTTP 메서드처럼 들리는 WebClient 메서드 중 하나를 실행할 수 있습니다. 

(2) 이전 예제에서는 WebClient#get을 사용했습니다. WebClient#get 메서드를 호출하면 요청 빌더 인스턴스에서 작업하고 (3)에 표시된 대로 uri 메서드에서 상대 경로를 지정할 수 있습니다. 상대 경로 외에도 헤더, 쿠키 및 요청 본문을 지정할 수 있습니다. 

(4) 그러나 단순화를 위해 이 경우 이러한 설정을 생략하고 검색 또는 교환 메서드를 호출하여 요청을 작성하는 것으로 넘어갔습니다. 이 예에서는 지점 (4)에 표시된 검색 방법을 사용합니다. 이 옵션은 몸체를 검색하고 추가 처리를 수행하는 데만 관심이 있을 때 유용합니다. 

(5) 요청이 설정되면 응답 본문을 변환하는 데 도움이 되는 방법 중 하나를 사용할 수 있습니다. 여기서는 사용자의 수신 페이로드를 Mono로 변환하는 bodyToMono 메서드를 사용합니다. 

마지막으로 Reactor API를 사용하여 들어오는 응답의 처리 흐름을 만들고 subscribe 메서드를 호출하여 원격 호출을 실행할 수 있습니다.

WebClient는 `subscribe()` 메소드를 호출해야만 컨넥션을 연결하고 원격 서버로 데이터를 보내기 시작합니다.

대부분의 경우 가장 일반적인 응답 처리는 본문 처리이지만 응답 상태, 헤더 또는 쿠키를 처리해야 하는 경우도 있습니다. 예를 들어 비밀번호 확인 서비스에 대한 호출을 작성하고 WebClient API를 사용하여 사용자 지정 방식으로 응답 상태를 처리해 보겠습니다.

```java
class DefaultPasswordVerificationService // (1)
   implements PasswordVerificationService {                        

   final WebClient webClient; // (2)
                                                                   
   public DefaultPasswordVerificationService(                      
      WebClient.Builder webClientBuilder                           
   ) {                                                             
      this.webClient = webClientBuilder // (2.1)
         .baseUrl("http://localhost:8080")                         
         .build();                                                 
   }                                                               

   @Override // (3)
   public Mono<Void> check(String raw, String encoded) {           
      return webClient                                             
         .post() // (3.1)
         .uri("/check")                                            
         .body(BodyInserters.fromPublisher( // (3.2)
            Mono.just(new PasswordDTO(raw, encoded)),              
            PasswordDTO.class                                      
         ))                                                        
         .exchange() // (3.3)
         .flatMap(response -> { // (3.4)
            if (response.statusCode().is2xxSuccessful()) { // (3.5)
               return Mono.empty();                                
            }                                                      
            else if(resposne.statusCode() == EXPECTATION_FAILD) {  
               return Mono.error( // (3.6)
                  new BadCredentialsException(...)                 
               );                                                  
            }                                                      
            return Mono.error(new IllegalStateException());        
         });                                                       
   }                                                               
}   
                                                               
```

(1) `PasswordVerificationService` 인터페이스의 구현입니다.

(2) WebClient 인스턴스의 초기화입니다. 여기서는 클래스별로 WebClient 인스턴스를 사용하므로 check 메서드를 실행할 때마다 새 인스턴스를 초기화할 필요가 없습니다. 이러한 기술은 WebClient의 새 인스턴스를 초기화할 필요성을 줄이고 메서드의 실행 시간을 줄입니다. 그러나 WebClient의 기본 구현은 기본 구성에서 모든 HttpClient 인스턴스 간에 공통 리소스 풀을 공유하는 `Reactor-Netty HttpClient`를 사용합니다. 따라서 새 HttpClient 인스턴스를 만드는 데 많은 비용이 들지 않습니다. `DefaultPasswordVerificationService`의 생성자가 호출되면 webClient 초기화를 시작하고 클라이언트를 설정하기 위해 (2.1) 지점에 표시된 빌더를 사용합니다.

(3) 이것은 check 메소드의 구현입니다. 여기에서 (3.1) 지점에 표시된 것처럼 게시 요청을 실행하기 위해 webClient 인스턴스를 사용합니다. 그리고 (3.2)와 같이 body 메소드를 사용하여 body를 전송하고 BodyInserters#fromPublisher 팩토리 메소드를 사용하여 삽입을 준비합니다. 그런 다음 (3.3) 지점에서 교환 메서드를 실행하여 `Mono<ClientResponse>`를 리턴합니다. 따라서 우리는 (3.4)에 표시된 flatMap 연산자를 사용하여 응답을 처리할 수 있습니다. 암호가 성공적으로 확인되면 (3.5) 지점에서 볼 수 있듯이 check 메서드는 Mono.empty를 리턴합니다. 또는 EXPECTATION_FAILED(417) 상태 코드의 경우 포인트 (3.6)에서와 같이 BadCredentialsExeception의 모노를 리턴할 수 있습니다.

앞의 예에서 알 수 있듯이 공통 HTTP 응답의 상태 코드, 헤더, 쿠키 및 기타 내부 처리가 필요한 경우 가장 적절한 방법은 ClientResponse를 리턴하는 exchange 방법입니다.

`DefaultWebClient`는 원격 서버와의 비동기 및 넌블로킹 상호작용을 제공하기 위해 Reactor-Netty HttpClient를 사용합니다. 그러나 DefaultWebClient는 기본 HTTP 클라이언트를 쉽게 변경할 수 있도록 설계되었습니다. 이를 위해 `org.springframework.http.client.reactive.ClientHttpConnector`라고 하는 HTTP 연결 주변에 낮은 수준의 반응형 추상화가 있습니다. 기본적으로 `DefaultWebClient`는 ClientHttpConnector 인터페이스의 구현인 `ReactorClientHttpConnector`를 사용하도록 미리 구성되어 있습니다. Spring WebFlux 5.1부터 Jetty의 반응형 HttpClient를 사용하는 `JettyClientHttpConnector` 구현이 있습니다. 기본 HTTP 클라이언트 엔진을 변경하기 위해 `WebClient.Builder#clientConnector` 메소드를 사용하고 원하는 인스턴스를 전달할 수 있습니다. 이는 사용자 정의 구현이거나 기존 구현일 수 있습니다.

유용한 추상 계층 외에도 ClientHttpConnector를 원시 형식으로 사용할 수 있습니다. 예를 들어 대용량 파일 다운로드, 즉석 처리 또는 단순한 바이트 스캔에 사용할 수 있습니다.



## 반응형 웹소켓 API

현대 웹의 중요한 부분 중 하나는 클라이언트와 서버가 서로 메시지를 스트리밍할 수 있는 스트리밍 상호 작용 모델입니다. 이 섹션에서는 WebSocket이라고 하는 이중 클라이언트-서버 통신을 위한 가장 잘 알려진 이중 프로토콜 중 하나를 살펴보겠습니다.

WebSocket 프로토콜을 통한 통신이 2013년 초에 Spring Framework에 도입되었고 비동기 메시지 전송을 위해 설계되었다는 사실에도 불구하고 실제 구현에는 여전히 일부 차단 작업이 있습니다. 예를 들어, I/O에 데이터 쓰기 또는 I/O에서 데이터 읽기 모두 여전히 차단 작업이므로 둘 다 애플리케이션 성능에 영향을 미칩니다. 따라서 WebFlux 모듈은 WebSocket용 인프라의 개선된 버전을 도입했습니다.

WebFlux는 클라이언트와 서버 인프라를 모두 제공합니다. 먼저 서버 측 WebSocket을 분석한 다음 클라이언트 측 가능성을 다룰 것입니다.

### Server-side WebSocket API 

WebFlux는 WebSocket 연결을 처리하기 위한 WebSocketHandler 인터페이스를 제공합니다. 이 인터페이스에는 WebSocketSession을 허용하는 `handle()` 메서드가 있습니다. WebSocketSession 클래스는 클라이언트와 서버 간의 성공적인 핸드셰이크를 나타내며 핸드셰이크, 세션 속성 및 들어오는 데이터 스트림에 대한 정보를 포함한 정보에 대한 액세스를 제공합니다. 이 정보를 처리하는 방법을 배우기 위해 발신자에게 에코 메시지로 응답하는 다음 예를 살펴보겠습니다.

```java
class EchoWebSocketHandler implements WebSocketHandler { // (1)
   @Override
   public Mono<Void> handle(WebSocketSession session) { // (2)
      return session // (3)
         .receive() // (4)
         .map(WebSocketMessage::getPayloadAsText) // (5)
         .map(tm -> "Echo: " + tm) // (6)
         .map(session::textMessage) // (7)
         .as(session::send); // (8)
    }
}
```
새로운 WebSocket API는 Project Reactor의 반응형 타입 위에 빌드됩니다. 

(1) 지점에서 WebSocketHandler 인터페이스의 구현을 제공하고 지점 (2)에서 핸들 메서드를 재정의합니다. 그런 다음 Flux API를 사용하여 들어오는 WebSocketMessage의 처리 흐름을 빌드하기 위해 지점 (3)에서 `WebSocketSession#receive` 메서드를 사용합니다. 

(5) WebSocketMessage는 DataBuffer 주변의 래퍼이며 바이트로 표시된 페이로드를 포인트 (5)의 텍스트로 변환하는 것과 같은 추가 기능을 제공합니다. 

(6) 입력 메시지가 추출되면 지점 (6)에 표시된 "Echo:" 접미사를 해당 텍스트 앞에 추가하고 WebSocketMessage에 새 텍스트 메시지를 래핑한 다음 WebSocketSession#send 메서드를 사용하여 클라이언트로 다시 보냅니다. 여기에서 send 메서드는 `Publisher<WebSocketMessage>`를 수락하고 결과로 `Mono<Void>`를 리턴합니다. 

(8) 따라서 Reactor API의 as 연산자를 사용하여 Flux를 `Mono<Void>`로 취급하고 `session::send`를 변환 함수로 사용할 수 있습니다.

`WebSocketHandler` 인터페이스 구현과 별도로 서버 측 WebSocket API를 설정하려면 추가 HandlerMapping 및 `WebSocketHandlerAdapter` 인스턴스를 구성해야 합니다. 이러한 구성의 예로 다음 코드를 고려하십시오.

```java
@Configuration                                                     // (1)
public class WebSocketConfiguration {                              

   @Bean                                                           // (2)
   public HandlerMapping handlerMapping() {                        
      SimpleUrlHandlerMapping mapping =                            
         new SimpleUrlHandlerMapping();                            // (2.1)
      mapping.setUrlMap(Collections.singletonMap(                  // (2.2)
         "/ws/echo",                                               
         new EchoWebSocketHandler()                                
      ));                                                          
      mapping.setOrder(-1);                                        // (2.3)
      return mapping;                                              
   }                                                               

   @Bean                                                           // (3)
   public HandlerAdapter handlerAdapter() {                        
      return new WebSocketHandlerAdapter();                        
   }                                                               
}
```
The preceding example can be described as follows: 

(1) @Configuration으로 주석이 달린 클래스입니다.
여기에 HandlerMapping 빈의 선언과 설정이 있습니다. 

(2.1) 지점에서 WebSocketHandler에 대한 (2.2) 지점에 표시된 설정 경로 기반 매핑을 허용하는 SimpleUrlHandlerMapping을 만듭니다. SimpleUrlHandlerMapping이 다른 HandlerMapping 인스턴스보다 먼저 처리되도록 하려면 우선 순위가 더 높아야 합니다.

(3) WebSocketHandlerAdapter인 HandlerAdapter 빈의 선언입니다. 여기서 WebSocketHandlerAdapter는 HTTP 연결을 WebSocket으로 업그레이드한 후 WebSocketHandler#handle 메소드를 호출하기 때문에 가장 중요한 역할을 합니다.

### Client-side WebSocket API

WebSocket 모듈(WebMVC 기반)과 달리 WebFlux는 클라이언트 측 지원도 제공합니다. WebSocket 연결 요청을 보내기 위해 WebSocketClient 클래스가 있습니다. WebSocketClient에는 다음 코드 샘플과 같이 WebSocket 연결을 실행하는 두 가지 중앙 메서드가 있습니다.

```java
public interface WebSocketClient {
   Mono<Void> execute(
      URI url,
      WebSocketHandler handler
   );
   Mono<Void> execute(
      URI url,
      HttpHeaders headers, 
      WebSocketHandler handler
   );
}
```
WebSocketClient는 동일한 WebSockeHandler 인터페이스를 사용하여 서버의 메시지를 처리하고 메시지를 다시 보냅니다. TomcatWebSocketClient 구현 또는 JettyWebSocketClient 구현과 같이 서버 엔진과 관련된 몇 가지 WebSocketClient 구현이 있습니다. 

다음 예제에서는 `ReactorNettyWebSocketClient`를 살펴보겠습니다.

```java
WebSocketClient client = new ReactorNettyWebSocketClient();

client.execute(
   URI.create("http://localhost:8080/ws/echo"),
   session -> Flux
      .interval(Duration.ofMillis(100))
      .map(String::valueOf)
      .map(session::textMessage)
      .as(session::send)
);
```
The preceding example shows how we can use ReactorNettyWebSocketClient to wire a WebSocket connection and start sending periodic messages to the server.

### WebFlux WebSocket 대 Spring WebSocket 모듈

서블릿 기반 WebSocket 모듈에 익숙한 독자는 두 모듈의 설계에 많은 유사점이 있음을 알 수 있습니다. 그러나 차이점도 많이 있습니다. Spring WebSocket 모듈의 주요 단점은 IO와의 차단 상호 작용인 반면 Spring WebFlux는 완전한 넌블로킹 쓰기 및 읽기를 제공합니다. 또한 WebFlux 모듈은 Project Reactor를 사용하여 더 나은 스트리밍 추상화를 제공합니다. 이전 WebSocket 모듈의 WebSocketHandler 인터페이스는 한 번에 하나의 메시지만 처리할 수 있습니다. 또한 `WebSocketSession#sendMessage` 메서드는 동기 방식으로만 메시지를 보낼 수 있습니다.

그러나 새로운 Spring WebFlux와 WebSocket의 통합에는 약간의 격차가 있습니다. 이전 Spring WebSocket 모듈의 중요한 기능 중 하나는 WebSocket 끝점을 선언하기 위해 `@MessageMapping` 주석을 사용할 수 있는 Spring Messaging 모듈과의 통합이었습니다. 

다음 코드는 Spring Messaging의 주석을 사용하는 이전 Web MVC 기반 WebSocket API의 예입니다.

```java
@Controller
public class GreetingController {

   @MessageMapping("/hello")
   @SendTo("/topic/greetings")
   public Greeting greeting(HelloMessage message) {
      return new Greeting("Hello, " + message.getName() + "!");
   }
}
```
앞의 코드는 Spring Messaging 모듈을 사용하여 WebSocket 끝점을 선언하는 방법을 보여줍니다. 
불행히도 WebFlux 모듈에서 WebSocket 통합에 대한 이러한 지원이 누락되었으며 복잡한 핸들러를 선언하려면 자체 인프라를 제공해야 합니다.

8장, Cloud Streams로 확장하기에서는 단순한 브라우저-서버 상호작용에 앞서 사용될 수 있는 클라이언트와 서버 간의 이중 메시징에 대한 또 다른 강력한 추상화를 다룰 것입니다.


### WebSockets의 경량 대체품으로서 `반응형 SSE`

무거운 WebSocket과 함께 HTML 5는 서버가 이벤트를 푸시할 수 있는 정적(이 경우 반이중) 연결을 생성하는 새로운 방법을 도입했습니다. 이 기술은 WebSocket과 유사한 문제를 해결합니다. 예를 들어 동일한 주석 기반 프로그래밍 모델을 사용하여 서버 전송 이벤트(SSE) 스트림을 선언할 수 있지만 다음 예제와 같이 대신 `ServerSentEvent` 객체의 무한 스트림을 리턴합니다.

```java
@RestController // (1)
@RequestMapping("/sse/stocks")                                     
class StocksController {                                           
   final Map<String, StocksService> stocksServiceMap;              
   ...                                                                
   @GetMapping // (2)
   public Flux<ServerSentEvent<?>> streamStocks() { // (2.1)
      return Flux                                                  
         .fromIterable(stocksServiceMap.values())                  
         .flatMap(StocksService::stream) // (2.2)
         .<ServerSentEvent<?>>map(item ->                          
            ServerSentEvent // (2.3)
               .builder(item) // (2.4)
               .event("StockItem") // (2.5)
               .id(item.getId()) // (2.6)
               .build()                                            
         )                                                         
         .startWith( // (2.7)
            ServerSentEvent                                        
              .builder()                                           
              .event("Stocks") // (2.8)
              .data(stocksServiceMap.keySet()) // (2.9)
              .build()                                             
         );                                                        
   }
}
```
이전 코드의 숫자는 다음과 같이 설명할 수 있습니다.

(1) @RestController 클래스의 선언입니다. 코드를 단순화하기 위해 생성자와 필드 초기화 부분을 건너뛰었습니다.

(2) 여기에 친숙한 @GetMapping으로 주석이 달린 처리기 메서드 선언이 있습니다. (2.1) 지점에서 볼 수 있듯이 streamStocks 메서드는 `ServerSentEvent`의 Flux를 리턴합니다. 즉, 현재 핸들러는 이벤트 스트리밍을 활성화합니다. 그런 다음 (2.2) 지점에서 볼 수 있듯이 사용 가능한 모든 주식 소스를 병합하고 변경 사항을 클라이언트에 스트리밍합니다. 그런 다음 (2.3)에서와 같이 (2.4)에서 정적 빌더 메서드를 사용하여 각 StockItem을 ServerSentEvent에 매핑하는 매핑을 적용합니다. `ServerSentEvent` 인스턴스를 올바르게 설정하기 위해 빌더 매개변수에 이벤트 ID(2.6)와 이벤트 이름(2.5)을 제공하여 클라이언트 측에서 메시지를 구별할 수 있도록 합니다. 또한 (2.7) 지점에서 (2.8) 지점에 표시된 특정 `ServerSentEvent` 인스턴스로 Flux를 시작합니다. 이 인스턴스는 클라이언트(2.9)에 사용 가능한 스톡 채널을 선언합니다.

앞의 예에서 볼 수 있듯이 Spring WebFlux는 Flux 반응 유형의 스트리밍 특성을 매핑하고 stock 이벤트의 무한 스트림을 클라이언트에 보낼 수 있습니다. 또한 SSE 스트리밍에서는 API를 변경하거나 추가 추상화를 사용할 필요가 없습니다. 프레임워크가 응답을 처리하는 방법을 파악하는 데 도움이 되도록 특정 리턴 유형을 선언하기만 하면 됩니다. `ServerSentEvent`의 Flux도 선언할 필요가 없습니다. 대신 다음 예와 같이 콘텐츠 유형을 직접 제공할 수 있습니다.

```java
@GetMapping(produces = "text/event-stream")
public Flux<StockItem> streamStocks() {
   ...
}
```
이 경우 WebFlux 프레임워크는 스트림의 각 요소를 내부적으로 ServerSentEvent로 래핑합니다.

보시다시피 ServerSentEvent 기술의 핵심 이점은 이러한 스트리밍 모델의 구성에 추가 상용구 코드가 필요하지 않다는 것입니다. 이는 SSE가 프로토콜 전환이 필요하지 않고 특정 서버 구성이 필요하지 않은 HTTP를 통한 간단한 추상화이기 때문입니다. 앞의 예에서 볼 수 있듯이 SSE는 @RestController 및 @XXXMapping 주석의 전통적인 조합을 사용하여 구성할 수 있습니다. 그러나 WebSocket의 경우 특정 메시징 프로토콜을 수동으로 선택하는 것과 같은 사용자 지정 메시지 변환 구성이 필요합니다. 대조적으로, SSE의 경우 Spring WebFlux는 일반적인 REST 컨트롤러와 동일한 메시지 변환기 구성을 제공합니다.

반면 SSE는 바이너리 인코딩을 지원하지 않으며 이벤트를 UTF-8 인코딩으로 제한합니다. 이것은 WebSocket이 더 작은 메시지 크기에 유용할 수 있고 클라이언트와 서버 사이에 더 적은 트래픽을 전송하는 데 유용할 수 있으므로 더 낮은 대기 시간을 가질 수 있음을 의미합니다.

요약하자면 SSE는 일반적으로 WebSocket에 대한 좋은 대안입니다. SSE는 HTTP 프로토콜을 통한 추상화이므로 WebFlux는 일반적인 REST 컨트롤러와 동일한 선언적 및 기능적 엔드포인트 구성 및 메시지 변환을 지원합니다.



## 반응형 웹 보안

Spring Web은 초기부터 Spring Security 모듈이라는 동반 모듈과 함께 제공되었습니다. 이를 통해 컨트롤러 및 웹 핸들러 호출 전에 필터를 제공하여 보안 웹 애플리케이션을 설정하고 기존 Spring 웹 인프라에 자연스럽게 맞출 수 있습니다. 수년 동안 Spring Security 모듈은 Web MVC 인프라와 결합되었으며 Servlet API의 필터 추상화만 사용했습니다.

다행히 Reactive WebFlux 모듈이 도입되면서 모든 것이 바뀌었습니다. 구성 요소 간의 반응 및 넌블로킹 상호 작용을 지원하고 반응형 방식으로 액세스를 제공하기 위해 Spring Security는 새로운 WebFilter 인프라를 사용하고 Project Reactor의 컨텍스트 기능에 크게 의존하는 완전히 새로운 반응 스택의 구현을 제공합니다.

### Reactive access to SecurityContext

새로운 반응형 Spring Security 모듈에서 SecurityContext에 액세스하기 위해 `ReactiveSecurityContextHolder`라는 새로운 클래스가 있습니다.

이 클래스는 `Mono<SecurityContext>`를 리턴하는 `getContext()` 메서드를 통해 반응형 방식으로 현재 SecurityContext에 대한 액세스를 제공합니다. 

애플리케이션에서 SecurityContext에 액세스하기 위해 다음 코드를 작성할 수 있습니다.

```java
@RestController // (1)
@RequestMapping("/api/v1")                                         
public class SecuredProfileController {                            
    
   @GetMapping("/profiles") // (2)
   @PreAuthorize("hasRole(USER)") // (2.1)
   public Mono<Profile> getProfile() { // (2.2)
      return ReactiveSecurityContextHolder // (2.3)
         .getContext() // (2.4)
         .map(SecurityContext::getAuthentication)                  
         .flatMap(auth ->                                          
            profileService.getByUser(auth.getName()) // (2.5)
         );                                                        
   }                                                               
}
```

(1) 요청 매핑이 `/api/v1`인 REST 컨트롤러 클래스의 선언입니다.

(2) `getProfile` 메서드는 (2.2) 지점에서 볼 수 있는 것처럼 데이터에 대한 Mono 타입을 리턴합니다. 그런 다음 현재 SecurityContext에 액세스하기 위해 ReactiveSecurityContextHolder.getContext()를 호출합니다. 

마지막으로 SecurityContext가 있는 경우 flatMap이 처리되고 (2.5) 항목에 표시된 것처럼 사용자 프로필에 액세스할 수 있습니다. 또한 이 메서드는 `@PreAuthorize`로 주석이 달려 있으며 이 경우 사용 가능한 인증에 필요한 역할이 있는지 확인합니다. 반응형 리턴 타입이 있는 경우 필요한 인증이 해결되고 필요한 권한이 있을 때까지 메서드 호출이 지연됩니다.

새로운 반응형 컨텍스트 홀더의 API는 API의 동기식 대응물에 있는 것과 다소 유사합니다. 또한 차세대 Spring Security에서는 필요한 권한을 확인하기 위해 동일한 주석을 사용할 수 있습니다.

내부적으로 `ReactiveSecurityContextHolder`는 Reactor Context API에 의존합니다. 로그인한 사용자에 대한 현재 정보는 Context 인터페이스의 인스턴스 내에 보관됩니다. 

다음 예는 ReactiveSecurityContextHolder가 내부에서 어떻게 작동하는지 보여줍니다.

```java
static final Class<?> SECURITY_CONTEXT_KEY = SecurityContext.class;
...
public static Mono<SecurityContext> getContext() {
   return Mono.subscriberContext()
      .filter(c -> c.hasKey(SECURITY_CONTEXT_KEY))
      .flatMap(c -> c.<Mono<SecurityContext>>get(SECURITY_CONTEXT_KEY));
}
```
"4장, 프로젝트 리액터 - 리액티브 앱의 기초"에서 기억할 수 있듯이 내부 리액터 컨텍스트에 액세스하기 위해 `subscriberContext`라는 Mono 타입의 전용 연산자를 사용할 수 있습니다. 그런 다음 컨텍스트에 액세스하면 현재 컨텍스트를 필터링하고 특정 키가 포함되어 있는지 확인합니다. 해당 키에 숨겨진 값은 SecurityContext의 Mono입니다. 즉, 반응형 방식으로 현재 SecurityContext에 액세스할 수 있습니다. 

실행은 예를 들어 누군가가 주어진 Mono를 구독할 때만 실행되는 데이터베이스에서 저장된 SecurityContext를 검색하는 것과 관련이 있습니다.

`ReactiveSecurityContextHolder`의 API는 친숙해 보이지만 많은 함정을 숨깁니다. 예를 들어 실수로 SecurityContextHolder로 작업할 때 익숙해진 방식을 따를 수 있습니다. 따라서 다음 코드 샘플에 설명된 일반적인 상호 작용을 맹목적으로 구현할 수 있습니다.

```java
ReactiveSecurityContextHolder
   .getContext()
   .map(SecurityContext::getAuthentication)
   .block();
```
ThreadLocal에서 SecurityContext를 검색하는 데 사용한 것처럼 이전 예제에서 볼 수 있듯이 `ReactiveSecurityContextHolde`r에서도 동일한 작업을 시도하고 싶을 수 있습니다. 
불행히도 getContext를 호출하고 block 메서드를 사용하여 스트림을 구독하면 스트림에 빈 컨텍스트가 구성됩니다. 따라서 ReactiveSecurityContextHodler 클래스가 내부 컨텍스트에 액세스하려고 시도하면 사용 가능한 SecurityContext를 찾을 수 없습니다.

따라서 문제는 섹션 시작 부분에 표시된 것처럼 스트림을 올바르게 연결할 때 컨텍스트를 설정하고 액세스할 수 있도록 하는 것입니다. 해답은 Spring5 Security에서 새로 나온 ReactorContextWebFilter에 있습니다. 호출하는 동안 이 필터는 subscriberContext 메서드를 사용하여 Reactor Context를 제공합니다. 또한 SecurityContext의 해결은 ServerSecurityContextRepository를 사용하여 수행됩니다. ServerSecurityContextRepository에는 저장 및 로드라는 두 가지 메서드가 있습니다.

```java
interface ServerSecurityContextRepository {
   Mono<Void> save(ServerWebExchange exchange, SecurityContext context);
   Mono<SecurityContext> load(ServerWebExchange exchange);
}
```

`save()` 메서드를 사용하면 SecurityContext를 특정 ServerWebExchange와 연결한 다음 ServerWebExchange에 연결된 수신 사용자 요청에서 load 메서드를 사용하여 복원할 수 있습니다.

보시다시피, Spring Security의 새로운 세대의 주요 이점은 SecurityContext에 대한 반응적 액세스에 대한 완전한 지원입니다. 여기서 반응적 접근은 실제 SecurityContext가 데이터베이스에 저장될 수 있음을 의미하므로 저장된 SecurityContext의 해결은 차단 작업이 필요하지 않습니다. 컨텍스트 분석 전략은 지연되므로 기본 저장소에 대한 실제 호출은 `ReactiveSecurityContextHolder.getContext()`를 구독할 때만 실행됩니다. 마지막으로 SecurityContext 전송 메커니즘을 사용하면 Thread 인스턴스 간의 일반적인 ThreadLocal 전파 문제에 주의를 기울이지 않고 복잡한 스트리밍 프로세스를 쉽게 구축할 수 있습니다. 

### Enabling reactive security

The last part that we haven't yet covered is how complex it is to enable security in a reactive web application. Fortunately, the configuration of security in a modern WebFlux-based application requires the declaration of few beans. The following is a reference example of how we may do this:

```java
@SpringBootConfiguration                                           // (1)
@EnableReactiveMethodSecurity                                      // (1.1)
public class SecurityConfiguration {                               

   @Bean                                                           // (2)
   public SecurityWebFilterChain securityWebFilterChain(           
      ServerHttpSecurity http                                      // (2.1)
   ) {                                                             
      return http                                                  // (2.2)
         .formLogin()                                              
         .and()                                                    
         .authorizeExchange()                                      
            .anyExchange().authenticated()                         
         .and()                                                    
         .build();                                                 // (2.3)
   }                                                               

   @Bean                                                           // (3)
   public ReactiveUserDetailsService userDetailsService() {        
      UserDetails user =                                           
         User.withUsername("user")                                 // (3.1)
             .withDefaultPasswordEncoder()                         
             .password("password")                                 
             .roles("USER", "ADMIN")                               
             .build();                                             
      return new MapReactiveUserDetailsService(user);              // (3.2)
   }                                                               
}
```

(1) 이것은 구성 클래스의 선언입니다. 여기서 특정 MethodInterceptor 어노테이션을 활성화하려면 (1.1)과 같이 이에 필요한 구성을 가져오는 @EnableReactiveMethodSecurity 어노테이션을 추가해야 합니다.

(2) 여기에 SecurityWebFilterChain 빈의 구성이 있습니다. 필요한 빈을 구성하기 위해 Spring Security는 유창한 API(2.2에 표시)가 있는 빌더(2.3에 표시)인 ServerHttpSecurity를 ​​제공합니다.

(3) ReactiveUserDetailsService 빈의 설정입니다. 기본 Spring Security 설정에서 사용자를 인증하려면 ReactiveUserDetailsService의 구현을 제공해야 합니다. 데모를 위해 (3.2) 지점에서 볼 수 있는 것처럼 인터페이스의 메모리 내 구현을 제공하고 시스템에 로그인하기 위해 테스트 사용자(3.1에서)를 구성합니다.

앞의 코드에서 알 수 있듯이 Spring Security의 전체 구성은 이전에 본 것과 유사합니다. 즉, 이러한 구성으로 마이그레이션하는 데 많은 시간이 걸리지 않습니다.

차세대 Spring Security에서 반응형 지원을 통해 인프라 설정에 드는 노력을 최소화하면서 고도로 보호된 웹 애플리케이션을 구축할 수 있습니다

### Interaction with other reactive libraries

Despite the fact that WebFlux uses Project Reactor 3 as the central building block, WebFlux allows the use of other reactive libraries as well. To enable cross-library interoperability, most operations in WebFlux are based on interfaces from the Reactive Streams specification. In this way, we can easily replace code written in Reactor 3 with RxJava 2 or Akka Streams:

```java
import io.reactivex.Observable;                                    // (1)
...                                                                

@RestController                                                    // (2)
class AlbomsController {                                           // 
   final ReactiveAdapterRegistry adapterRegistry;                  // (2.1)
   ...                                                             

   @GetMapping("/songs")                                           // (3)
   public Observable<Song> findAlbomByArtists(                     // (3.1)
      Flux<Artist> artistsFlux                                     // (3.2)
   ) {                                                             
      Observable<Artist> observable = adapterRegistry              // (4)
         .getAdapter(Observable.class)                             
         .fromPublisher(artistsFlux);                              
      Observable<Song> albomsObservable = ...;                     // (4.1) 
                                                                   
      return albomsObservable;                                     // (4.2)
   }
}
```
(1) 이것은 RxJava 2에서 Observable을 가져오는 것을 보여주는 가져오기 선언입니다.

(2) @RestController 주석으로 주석이 달린 AlbomsController 클래스입니다. 또한 이 예제의 뒷부분에서 사용되는 ReactiveAdapterRegistry 유형의 필드를 선언합니다.

(3) 여기에 findAlbumByArtists라는 요청 처리기 메서드가 선언되어 있습니다. 우리가 볼 수 있듯이 findAlbumByArtists는 지점 (3.2)에 표시된 것처럼 Flux<Artist> 유형의 게시자를 허용하고 지점 (3.1)에 표시된 것처럼 Observable<Song>을 리턴합니다.

(4) 여기에 ArtistFlux를 Observable<Artist>에 매핑하고, 비즈니스 로직(4.1에서)을 실행하고, 호출자에게 결과를 리턴하는 선언이 있습니다.

앞의 예제는 RxJava의 반응 유형과 Project Reactor 반응 유형을 사용하여 반응 통신을 다시 작성하는 방법을 보여줍니다. 5장, Spring Boot 2로 반응하기에서 기억할 수 있듯이 반응형 변환은 Spring Core 모듈의 일부이며 org.springframework.core.ReactiveAdapterRegistry 및 org.springframework.core.ReactiveAdapter에서 지원합니다. 이러한 클래스를 사용하면 Reactive Streams Publisher 클래스로의 변환과 Reactive Streams Publisher 클래스에서의 변환이 모두 가능합니다. 따라서 해당 지원 라이브러리를 사용하면 프로젝트 리액터와 긴밀하게 연결하지 않고도 거의 모든 리액티브 라이브러리를 사용할 수 있습니다.




## WebFlux versus Web MVC

이전 섹션에서는 새로운 Spring WebFlux에 포함된 주요 구성 요소에 대한 간략한 개요를 제공했습니다. 또한 Spring WebFlux 모듈에 도입된 새로운 기능과 사용 방법을 살펴보았습니다.

그러나 이제 새로운 API를 사용하여 웹 애플리케이션을 구축하는 방법을 이해했음에도 불구하고 새로운 WebFlux가 Web MVC보다 나은 이유는 여전히 불분명합니다. WebFlux의 주요 이점을 이해하는 것이 도움이 될 것입니다.

이를 위해서는 웹 서버 구축 방법에 대한 이론적 토대를 파고, 빠른 웹 서버의 중요한 특성이 무엇인지 이해하고, 웹 서버의 성능을 변경할 수 있는 요소를 고려해야 합니다. 다음 섹션에서는 최신 웹 서버의 중요한 특성을 분석하고 성능 저하를 일으킬 수 있는 원인을 배우고 이를 방지하는 방법에 대해 생각할 것입니다.


### 프레임워크를 비교할 때 법칙이 중요

계속 진행하기 전에 비교에 사용할 시스템의 특성을 이해하려고 합니다. 대부분의 웹 애플리케이션에 대한 중심 지표는 처리량, 대기 시간, CPU 및 메모리 사용량입니다. 웹은 이제 시작했을 때와 완전히 다른 요구 사항을 갖습니다. 이전에는 컴퓨터가 순차적이었습니다. 이전에는 사용자가 단순하고 정적인 콘텐츠를 관찰하는 데 만족했으며 시스템의 전체 부하가 낮았습니다. 기본 작업에는 HTML 생성 또는 간단한 계산이 포함되었습니다. 하나의 프로세서에 계산이 들어맞았고 웹 응용 프로그램에 대해 둘 이상의 서버가 필요하지 않았습니다.

시간이 지남에 따라 게임의 규칙이 변경되었습니다. 웹은 사용자를 10억 ​​단위로 계산하기 시작했고 콘텐츠는 동적이고 심지어 실시간으로 변하기 시작했습니다. 처리량 및 대기 시간에 대한 요구 사항이 크게 변경되었습니다. 우리의 웹 애플리케이션은 코어와 클러스터에 고도로 분산되기 시작했습니다. 웹 애플리케이션을 확장하는 방법을 이해하는 것이 중요해졌습니다. 중요한 질문은 병렬 작업자의 수가 대기 시간이나 처리량을 어떻게 변화시키는가입니다.

### 리틀의 법칙

이 질문에 답하기 위해 리틀의 법칙이 구출됩니다. 이 법칙은 특정 대기 시간 수준에서 미리 정의된 처리량을 처리하기 위해 동시에 처리되는 요청 수(또는 단순히 병렬 작업자 수)를 계산하는 방법을 설명합니다. 즉, 이 공식을 사용하여 안정적인 응답 시간으로 필요한 초당 사용자 수를 처리하기 위해 필요한 시스템 용량 또는 병렬로 실행되는 컴퓨터, 노드 또는 웹 애플리케이션 인스턴스의 수를 계산할 수 있습니다.

 N = X * R

앞의 공식은 다음과 같이 설명될 수 있습니다. 시스템 또는 대기열에 있는 평균 요청 수(또는 동시에 처리되는 요청 수)(N)는 처리량(또는 초당 사용자 수)(X)에 평균 응답을 곱한 것과 같습니다. 시간 또는 대기 시간(R).

즉, 시스템의 평균 응답 시간 R이 0.2초이고 처리량 X가 초당 100개 요청인 경우 20개의 요청을 동시에 처리하거나 20명의 사용자를 병렬로 처리할 수 있어야 합니다. 한 기계에 20명의 작업자가 필요하거나 작업자 한 명이 있는 기계 20개가 필요합니다. 작업자 또는 동시 요청 간에 교차가 없는 이상적인 경우입니다. 이것은 다음 다이어그램에 나와 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/c972d8e4-608d-42d0-b2ca-7b4a77e5920d.png)

도표 6.3. 이상적인 동시 처리

앞의 다이어그램에서 볼 수 있듯이 시스템에는 3개의 작업자가 있으며 초당 6개의 액터 또는 요청을 처리할 수 있습니다. 이 경우 모든 행위자는 작업자 사이에서 균형을 이루므로 작업자를 선택하기 위해 작업자 간에 조정이 필요하지 않습니다.

그러나 웹 애플리케이션과 같은 모든 시스템은 CPU 또는 메모리와 같은 공유 리소스에 대한 동시 액세스가 필요하기 때문에 앞의 경우는 실제로 그다지 현실적이지 않습니다. 따라서 Amdahl의 법칙과 그 확장인 Universal Scalability Law에 설명된 전체 처리량에 대한 수정 사항 목록이 늘어나고 있습니다.

### Amdahl's Law

이러한 법칙 중 첫 번째는 직렬화된 액세스가 평균 응답 시간(또는 대기 시간)과 처리량에 미치는 영향에 관한 것입니다. 우리는 항상 작업을 병렬화하기를 원하지만 병렬화할 수 없고 대신 작업을 직렬화해야 하는 시점이 올 수 있습니다. 이는 조정자 작업자가 있거나 반응 흐름에 집계 또는 축소 연산자가 있는 경우일 수 있습니다. 즉, 모든 실행을 조인해야 합니다. 또는 직렬 모드에서만 작동하는 코드 조각일 수 있으므로 병렬로 실행할 수 없습니다. 대규모 마이크로서비스 시스템에서 이것은 로드 밸런서 또는 오케스트레이션 시스템일 수 있습니다. 따라서 다음 공식을 사용하여 처리량 변화를 계산하기 위해 `Amdahl의 법칙`을 참조할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/0bd905d1-d0bb-4e86-af88-ba7af1cae65c.png)

이 공식에서 X(1)은 초기 처리량, N은 병렬화 또는 작업자 수, σ는 경합 계수(직렬화 계수라고도 함), 즉 전체 시간의 백분율입니다. 병렬로 처리할 수 없는 코드를 실행하는 데 소비됩니다.

간단한 계산을 수행하고 임의의 경합 계수, σ = 0,03 및 초기 처리량 X(1) = 병렬화 범위 N = 0에서 초당 50개 요청을 사용하여 병렬화 처리량의 종속성 그래프를 작성하면.. 500이면 다음 곡선을 얻습니다.


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/453856fa-e179-4983-a1f9-99a05293ca86.png)
Diagram 6.4. Throughput changes depending on parallelization

앞의 다이어그램에서 알 수 있듯이 병렬화가 증가함에 따라 시스템의 처리량이 점점 느려지기 시작합니다. 마지막으로 처리량의 전반적인 증가가 끝나고 대신 점근적 동작을 따릅니다. Amdahl의 법칙에 따르면 전체 작업 병렬화는 처리량의 선형 증가를 제공하지 않습니다. 왜냐하면 우리는 코드의 직렬화된 부분보다 더 빠르게 결과를 처리할 수 없기 때문입니다. 일반적인 웹 응용 프로그램을 확장하는 관점에서 이 진술은 더 빠르게 작동할 수 없는 단일 조정 지점 또는 처리가 있는 경우 시스템의 코어 또는 노드 수를 늘려도 아무런 이점이 없음을 의미합니다. 게다가, 우리는 중복 머신을 지원함으로써 돈을 잃고, 처리량의 전반적인 증가는 비용 가치가 없습니다.

앞의 차트에서 처리량의 변화가 병렬화에 의존함을 알 수 있습니다. 그러나 많은 경우 병렬화에 대한 종속성이 증가함에 따라 지연 시간이 어떻게 변하는지 이해해야 합니다. 이를 위해 리틀의 법칙과 암달의 법칙의 방정식을 결합할 수 있습니다. 기억할 수 있듯이 두 방정식 모두 처리량(X)을 포함합니다. 따라서 두 공식을 결합하려면 리틀의 법칙을 다시 작성해야 합니다.


After the preceding transformation, we may replace X(N) in Amdahl's Law, and derive the following:


Finally, in order to derive the latency (R), we have to do the following transformation:



From the preceding formula, we may conclude that the overall growth is linear. The following diagram shows the latency growth curve depending on the parallelization:


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/1d19755c-9b46-4308-920a-e3bb7e171b90.png)

Diagram 6.5. Linear growth of latency depending on parallelization

That means that with an increase in parallelization, the response time decreases.

To conclude, as described by Amdahl's Law, a system that has parallel execution always has points of serialization, which causes additional overhead and does not allow us to reach higher throughput just by increasing the level of parallelization. The following diagram shows this system:


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/b9f04f87-b206-4f93-8bfd-e483282736fa.png)

Diagram 6.6. Simultaneous processing example using Amdahl's Law

이전 다이어그램은 다음과 같이 설명할 수 있습니다.

(1) worker 표현입니다. 여기에서도 코드는 독립적으로 실행할 수 있는 더 작은 하위 작업으로 분할될 수 없으며 직렬화 지점으로도 간주되어야 합니다.

(2) 이것은 큐 또는 사용자 요청에 있는 액터의 표현입니다.

(3) 다음은 액터 또는 사용자 요청이 전용 작업자에게 할당되기 전의 대기열입니다. 직렬화 포인트는 작업자에게 액터를 할당하고 조정하는 것입니다.

(4) 배우 조정이 양방향으로 필요할 수 있습니다. 이 시점에서 코디네이터는 사용자에게 응답을 다시 보내기 위해 몇 가지 작업을 수행할 수 있습니다.

요약하자면, Amdahl의 법칙은 시스템에 병목 현상이 있으며 이로 인해 더 많은 사용자에게 서비스를 제공할 수 없거나 대기 시간이 짧다고 명시되어 있습니다.

### The Universal Scalability Law

Amdahl의 법칙은 모든 시스템의 확장성을 설명하지만 실제 응용 프로그램은 상당히 다른 확장성 결과를 보여줍니다. Neil Gunther는 이 분야에 대한 몇 가지 연구를 통해 직렬화에도 불구하고 일관성 없는 또 다른 중요한 점이 있음을 발견했습니다.

Neil Gunther는 오픈 소스 성능 모델링 소프트웨어 Pretty Damn Quick을 개발하고 컴퓨터 용량 계획 및 성능 분석에 대한 게릴라 접근 방식을 개발한 것으로 국제적으로 가장 잘 알려진 컴퓨터 정보 시스템 연구원입니다. 자세한 내용은 http://www.perfdynamics.com/Bio/njg.html을 참조하십시오.

비일관성은 공유 리소스가 있는 동시 시스템에서 일반적인 현상입니다. 예를 들어, 표준 Java 웹 애플리케이션 관점에서 볼 때 이러한 일관성은 CPU와 같은 리소스에 대한 혼란스러운 스레드 액세스에서 노출됩니다. 전체 Java 스레딩 모델은 이상적이지 않습니다. 실제 프로세서보다 스레드 인스턴스가 더 많은 경우 CPU에 액세스하고 계산 주기를 달성하기 위해 서로 다른 스레드 인스턴스 간에 직접적인 충돌이 있습니다. 이를 위해서는 중복 조정 및 일관성을 해결하기 위한 추가 노력이 필요합니다. 스레드에서 공유 메모리에 액세스할 때마다 추가 동기화가 필요하고 애플리케이션의 처리량과 대기 시간이 감소할 수 있습니다.

시스템에서 이러한 동작을 설명하기 위해 Amdahl의 법칙의 USL(Universal Scalability Law) 확장은 병렬화에 따른 처리량 변화를 계산하는 다음 공식을 제공합니다.


앞의 공식은 일관성 계수(k)를 소개합니다. 여기서 가장 주목할만한 점은 이제부터 병렬화 N에 대해 2차 역방향 처리량 X(N) 관계가 있다는 것입니다.

이 연결의 치명적인 영향을 이해하기 위해 이전과 동일한 초기 처리량 X(1) = 5가 있는 다음 다이어그램을 살펴보겠습니다.0, the coefficient of contention σ  = 0,03, and the coefficient of coherence k = 0,00007:


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/85cbbf94-591e-4d5a-bfae-b95f6fe883a6.png)
Diagram 6.7 병렬화에 따른 처리량. Amdahl의 법칙(점선)과 USL(실선)의 비교.

앞의 플롯에서 처리량이 저하되기 시작하는 위기 지점이 있음을 관찰할 수 있습니다. 더욱이, 실제 시스템 확장성을 더 잘 나타내기 위해 그래프는 USL에 의해 모델링된 시스템 확장성과 Amdahl의 법칙에 의해 모델링된 시스템 확장성을 모두 보여줍니다. 평균 응답 시간 저하 곡선도 동작을 변경했습니다. 다음 다이어그램은 병렬화에 따른 지연 시간 변화를 보여줍니다.


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/7b5281d6-b93d-490d-9dd1-b7859e4705fc.png)
Diagram 6.8 병렬화에 따른 처리량. Amdahl 법칙(점선)과 USL(실선)의 비교

유사하게, 대조를 보여주기 위해 USL에 의해 모델링된 지연 변화 곡선은 Amdahl의 법칙에 의해 모델링된 동일한 곡선과 비교된다. 이전 플롯에서 볼 수 있듯이 공유 액세스 지점이 있는 경우 시스템이 다르게 작동하며, 이는 일관성이 없고 추가 동기화가 필요할 수 있습니다. 이러한 시스템의 개략적인 예는 다음 다이어그램에 나와 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/974a5fd3-444a-4cf0-ae86-28e168866d9a.png)
Diagram 6.9. Simultaneous processing example by the USL

우리가 볼 수 있듯이 시스템의 전체 그림은 리틀의 법칙에 의해 처음 도입된 것보다 훨씬 더 복잡할 수 있습니다. 시스템의 확장성에 직접적인 영향을 줄 수 있는 숨겨진 함정이 많이 있습니다.

이 세 섹션을 결론짓기 위해 이러한 법칙에 대한 전반적인 이해는 확장 가능한 시스템을 모델링하고 시스템 용량을 계획하는 데 중요한 역할을 합니다. 이러한 법칙은 복잡한 고부하 분산 시스템과 Spring Framework로 구축된 웹 애플리케이션이 있는 다중 프로세서 노드에 적용될 수 있습니다. 또한 시스템 확장성에 영향을 미치는 요소를 이해하면 시스템을 적절하게 설계하고 불일치 및 경합과 같은 함정을 피할 수 있습니다. 또한 법칙의 관점에서 WebFlux 및 Web MVC 모듈을 적절하게 분석하고 어떤 스케일이 가장 잘 수행될지 예측할 수 있습니다.




## 철저한 분석과 비교

확장성에 대한 지식을 통해 우리는 프레임워크의 동작, 아키텍처 및 리소스 사용 모델을 이해하는 것이 필수적이라는 것을 알고 있습니다. 또한 특정 문제를 해결하기 위해 적절한 프레임워크를 선택하는 것이 중요합니다. 다음 몇 개의 하위 섹션에서는 Web MVC와 WebFlux를 다양한 관점에서 비교하고 마지막으로 각각이 더 적합한 문제 영역에 대해 알아볼 것입니다.

### WebFlux 및 Web MVC의 처리 모델 이해

우선, 시스템 처리량 및 대기 시간에 대한 다양한 처리 모델의 영향을 이해하기 위해 Web MVC 및 WebFlux에서 들어오는 요청이 처리되는 방식을 요약합니다.

앞서 언급했듯이 Web MVC는 I/O 차단 위에 구축됩니다. 즉, 들어오는 각 요청을 처리하는 스레드는 I/O에서 들어오는 본문을 읽음으로써 차단될 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/e907d9dc-60e3-4bb8-885a-e9b1d678b2a8.png)

도표 6.10. 요청 및 응답 처리 차단

앞의 예에서 모든 요청은 큐에 대기되고 하나의 스레드에 의해 순차적으로 처리됩니다. 검은색 막대는 I/O에서 읽기/쓰기 작업을 차단하고 있음을 나타냅니다. 또한 알 수 있듯이 실제 처리 시간(흰색 막대)은 차단 작업에 소요된 시간보다 훨씬 짧습니다. 이 간단한 다이어그램에서 우리는 스레드가 비효율적이며 대기열에서 요청을 수락하고 처리할 때 대기 시간이 공유될 수 있다는 결론을 내릴 수 있습니다.

대조적으로 WebFlux는 넌블로킹 API 위에 구축되었습니다. 즉, I/O 블록 스레드와의 상호 작용이 필요하지 않은 작업이 없습니다. 요청을 수락하고 처리하는 이 효율적인 기술은 다음 다이어그램에 나와 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/3ad867bf-dbc4-4e01-88cb-a82fe2f958b7.png)
도표 6.11. 비동기식 넌블로킹 요청 처리

이전 I/O 차단 사례와 동일한 사례가 있습니다. 다이어그램의 왼쪽에는 요청 대기열이 있고 가운데에는 처리 타임라인이 있습니다. 이 경우 처리 타임라인에 검은색 막대가 없으므로 요청 처리를 계속하기 위해 네트워크에서 오는 바이트가 충분하지 않더라도 차단하지 않고 항상 다른 요청 처리로 전환할 수 있습니다. 앞의 비동기 넌블로킹 요청 처리를 차단 예제와 비교하면 요청 본문이 수집되는 동안 기다리는 대신 스레드가 새 연결을 수락하는 데 효율적으로 사용된다는 것을 알 수 있습니다. 그런 다음 기본 운영 체제는 예를 들어 요청 본문이 수집되었으며 프로세서가 차단 없이 처리할 수 있음을 알릴 수 있습니다. 이 경우 최적의 CPU 사용률을 갖게 됩니다. 마찬가지로 응답을 작성하는 데 차단이 필요하지 않으며 비 차단 방식으로 I/O에 쓸 수 있습니다. 유일한 차이점은 데이터의 일부를 차단 없이 I/O에 쓸 준비가 되었을 때 시스템이 알려준다는 것입니다.

이전 예는 WebFlux가 Web MVC보다 훨씬 더 효율적으로 하나의 스레드를 활용하므로 같은 시간에 더 많은 요청을 처리할 수 있음을 보여줍니다. 그러나 적절한 수의 스레드 인스턴스를 배치하여 실제 프로세서를 활용할 수 있도록 Java에 여전히 다중 스레딩이 있다고 주장하는 것은 여전히 ​​가능합니다. 따라서 웹 MVC를 차단하여 요청을 더 빠르게 처리하고 동일한 CPU 사용률을 달성하기 위해 하나의 스레드 대신 여러 작업자 스레드 또는 연결 모델당 하나의 스레드를 사용할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/b344d810-59fb-4598-a3a1-07d673737e80.png)
도표 6.12. 연결당 스레드 웹 MVC 모델

앞의 다이어그램에서 볼 수 있듯이 멀티스레딩 모델은 대기 중인 요청을 더 빠르게 처리할 수 있으며 시스템이 거의 동일한 수의 요청을 수락, 처리 및 응답하는 것처럼 보입니다.

그러나 이 디자인에는 단점이 있습니다. 범용 확장성 법칙에서 배웠듯이 시스템이 CPU나 메모리와 같은 리소스를 공유할 때 병렬 작업자의 수를 확장하면 시스템 성능이 저하될 수 있습니다. 이 경우 사용자 요청을 처리할 때 너무 많은 Thread 인스턴스를 포함하게 되면 이들 간의 비일관성(incoherence)으로 인해 성능이 저하된다.

### 처리량 및 대기 시간에 대한 처리 모델의 영향

간단한 부하 테스트를 하기 위해 Web MVC 또는 WebFlux로 간단한 Spring Boot 2.x 애플리케이션을 사용할 것입니다. 또한 제3자 서비스에 대한 몇 가지 네트워크 호출을 수행하여 미들웨어에서 I/O 활동을 시뮬레이션할 것입니다. 그러면 평균 대기 시간이 200밀리초가 보장된 빈 성공적인 응답이 리턴됩니다. 통신 흐름은 다음과 같이 표시됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/ff1c5fb9-9302-4fb0-a263-18c01099f8b7.png)

도표 6.13. 벤치마크에 대한 통신 흐름

미들웨어를 시작하고 클라이언트 활동을 시뮬레이션하기 위해 각 컴퓨터에 Ubuntu Server 16.04가 설치된 Microsoft Azure 인프라를 사용할 것입니다. 미들웨어의 경우 D12 v2 VM(4개의 가상 CPU 및 28GB RAM)을 사용할 것입니다. 클라이언트의 경우 F4 v2 VM(가상 CPU 4개 및 RAM 8GB)을 사용할 것입니다. 사용자 활동은 작은 단계로 순차적으로 증가합니다. 4명의 동시 사용자로 부하 테스트를 시작하고 20,000명의 동시 사용자로 끝낼 것입니다. 이것은 우리에게 부드러운 대기 시간 곡선과 처리량 변화를 제공하고 이해하기 쉬운 그래픽을 만들 수 있게 해줍니다. 미들웨어에 적절한 로드를 생성하고 통계 및 측정 특성을 올바르게 수집하기 위해 wrk(https://github.com/wg/wrk)라는 최신 HTTP 벤치마킹 도구를 사용할 것입니다.

이 벤치마크는 시간 경과에 따른 시스템 안정성보다는 경향을 보여주고 WebFlux 프레임워크의 현재 구현이 얼마나 적절한지를 측정하기 위한 것입니다. 다음 측정은 Web MVC에서 동기 및 스레드 기반 통신을 차단하는 것에 비해 WebFlux에서 넌블로킹 및 비동기 통신의 이점을 보여줍니다.

다음은 측정에 사용되는 Web MVC 미들웨어 코드의 예입니다.

```java
@RestController                                                    // (1)
@SpringBootApplication                                             // 
public class BlockingDemoApplication                               
   implements InitializingBean {                                   
   ...                                                             // (1.1)
   @GetMapping("/")                                                // (2)
   public void get() {                                             
      restTemplate.getForObject(someUri, String.class);            // (2.1)
      restTemplate.getForObject(someUri, String.class);            // (2.2)
   }                                                               
   ...                                                             
}
                                                            
```
The preceding code can be described as follows:

(1) @SpringBootApplication으로 주석 처리된 클래스의 선언입니다. 동시에 이 클래스는 @RestController로 주석이 달린 컨트롤러입니다. 이 예제를 가능한 한 단순하게 유지하기 위해 (1.1) 지점에서 볼 수 있듯이 초기화 프로세스를 건너뛰고 이 클래스에서 필드를 선언했습니다.

(2) 여기에 @GetMapping 선언이 있는 get 메서드가 있습니다. 중복 네트워크 트래픽을 줄이고 프레임워크 성능에만 집중하기 위해 응답 본문에 내용을 반환하지 않습니다. 앞의 다이어그램에서 언급한 흐름에 따라 (2.1) 및 (2.2) 지점에 표시된 것처럼 원격 서버에 대한 두 가지 HTTP 요청을 수행합니다.
앞의 예제와 스키마에서 알 수 있듯이 미들웨어의 평균 응답 시간은 약 400밀리초여야 합니다.

이 테스트에서는 Web MVC의 기본값인 Tomcat 웹 서버를 사용할 것입니다. 또한 Web MVC에서 성능이 어떻게 변하는지 확인하기 위해 동시 사용자 수만큼 Thread 인스턴스를 설정합니다. 다음 sh 스크립트는 Tomcat에 대한 설정을 보여줍니다.

```java
java -Xss512K -Xmx24G -Xms24G 
   -Dserver.tomcat.prestartmin-spare-threads=true
   -Dserver.tomcat.prestart-min-spare-threads=true 
   -Dserver.tomcat.max-threads=$1
   -Dserver.tomcat.min-spare-threads=$1
   -Dserver.tomcat.max-connections=100000
   -Dserver.tomcat.accept-count=100000
   -jar ...
```
max-threads 및 min-spare-threads 매개변수의 값은 동적이며 테스트의 병렬 사용자 수에 의해 정의됩니다.

앞의 설정은 프로덕션 준비가 되지 않았으며 Spring Web MVC에서 사용되는 스레딩 모델, 특히 연결당 스레드 모델의 단점을 보여주기 위한 목적으로만 사용됩니다.

서비스에 대한 테스트 제품군을 시작하면 다음과 같은 결과 곡선을 얻을 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/0e0a2a87-c883-4e6b-94fa-6a6655b761e3.png)
도표 6.14. Web MVC 처리량 측정 결과

앞의 다이어그램은 어느 시점에서 처리량 손실이 시작됨을 보여줍니다. 이는 애플리케이션에 경합 또는 불일치가 있음을 의미합니다.

Web MVC 프레임워크의 성능 결과를 비교하려면 WebFlux에서도 동일한 테스트를 실행해야 합니다. 
다음은 WebFlux 기반 애플리케이션 성능을 측정하기 위해 사용하는 코드입니다.

```java
@RestController                                                   
@SpringBootApplication
public class ReactiveDemoApplication 
   implements InitializingBean {
   ...
   @GetMapping("/")
   public Mono<Void> get() {                                       // (1)
      return                                                       // 
         webClient                                                 
               .get()                                              // (2)
               .uri(someUri)                                       // 
               .retrieve()                                         // 
               .bodyToMono(DataBuffer.class)                       
               .doOnNext(DataBufferUtils::release)                 
         .then(                                                    // (3)
            webClient                                              
               .get()                                              // (4)
               .uri(someUri)                                       
               .retrieve()                                         
               .bodyToMono(DataBuffer.class)                       
               .doOnNext(DataBufferUtils::release)                 
               .then()                                             
         )                                                         
         .then();                                                  // (5)
    }
    ...
}
```
앞의 코드는 비동기 및 넌블로킹 요청 및 응답 처리를 달성하기 위해 현재 Spring WebFlux 및 Project Reactor 기능을 적극적으로 사용하고 있음을 보여줍니다. 웹 MVC의 경우와 마찬가지로 (1) 지점에서 Void 결과를 반환하지만 이제는 반응 유형인 Mono로 래핑됩니다. 그런 다음 WebClient API를 사용하여 원격 호출을 실행하고 지점 (3)에서 동일한 순차적 방식으로 지점 (4)에 표시된 두 번째 원격 호출을 수행합니다. 마지막으로 두 호출 실행 결과를 건너뛰고 구독자에게 두 실행 완료를 알리는 `Mono<Void>` 결과를 ​​반환합니다.

Reactor 기술을 사용하면 두 요청을 병렬로 수행하지 않고도 실행 시간을 개선할 수 있습니다. 두 실행 모두 넌블로킹 및 비동기이므로 이를 위해 추가 스레드 인스턴스를 할당할 필요가 없습니다. 그러나 다이어그램 6.13에 언급된 시스템의 동작을 유지하기 위해 실행을 순차적으로 유지하므로 결과 대기 시간은 평균 ~400밀리초가 되어야 합니다.
WebFlux 기반 미들웨어에 대한 테스트 제품군을 시작하면 다음과 같은 결과 곡선을 얻을 수 있습니다.


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/82a6a04b-07dd-42e7-acb0-299f374f20b1.png)
도표 6.15. WebFlux 처리량 측정 결과

앞의 차트에서 볼 수 있듯이 WebFlux 곡선의 경향은 WebMVC 곡선과 다소 유사합니다.
두 곡선을 비교하기 위해 동일한 플롯에 두도록 합시다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/2cba6a14-e26d-43f0-ae6e-12a392f725c3.png)

도표 6.16. WebFlux 대 Web MVC 처리량 측정 결과 비교

앞의 다이어그램에서 +(더하기) 기호 행은 Web MVC용이고 -(대시) 기호 행은 WebFlux용입니다. 이 경우 높을수록 더 좋습니다. 보시다시피 WebFlux의 처리량은 거의 두 배입니다.

또한 12,000명의 병렬 사용자 이후에는 Web MVC에 대한 측정값이 없다는 점에 유의해야 합니다. 문제는 Tomcat의 스레드 풀이 너무 많은 메모리를 사용하고 주어진 28GB에 맞지 않는다는 것입니다. 따라서 Tomcat이 12,000개 이상의 스레드 인스턴스를 전용으로 시도할 때마다 Linux 커널은 해당 프로세스를 종료합니다. 이 점은 약 10,000명 이상의 사용자를 처리해야 하는 경우 연결당 스레드 모델이 적합하지 않다는 것을 강조합니다.

앞의 비교는 연결당 스레드 모델과 넌블로킹 비동기 처리 모델을 비교한 것입니다. 첫 번째 경우 대기 시간에 큰 영향을 주지 않고 요청을 처리하는 유일한 방법은 각 사용자에 대해 별도의 스레드를 지정하는 것입니다. 이러한 방식으로 사용자가 사용 가능한 스레드를 기다리는 대기열에서 보내는 시간을 최소화합니다. 반면에 WebFlux의 설정은 non-blocking I/O를 사용하기 때문에 사용자별로 별도의 Thread를 할당할 필요가 없습니다. 실제 시나리오에서 Tomcat 서버의 일반적인 구성은 스레드 풀에 대해 제한된 크기를 갖습니다.

그럼에도 불구하고, 두 곡선은 유사한 경향을 나타내고 임계점이 있으며 그 이후에는 처리량이 저하되기 시작합니다. 이것은 많은 시스템이 개방형 클라이언트 연결 측면에서 한계가 있다는 사실로 설명될 수 있습니다. 또한 구성이 다른 HTTP 클라이언트의 다른 구현을 사용하기 때문에 비교가 약간 불공평할 수 있습니다. 예를 들어 RestTemplate의 기본 연결 전략은 새 호출마다 새 HTTP 연결을 할당하는 것입니다. 대조적으로 기본 Netty 기반 WebClient 구현은 내부적으로 연결 풀을 사용합니다. 이 경우 연결을 재사용할 수 있습니다. 열린 연결을 재사용하도록 시스템을 조정할 수 있지만 이러한 비교는 잘못된 표현일 수 있습니다.

따라서 더 나은 비교를 위해 0.4초 지연을 제공하여 네트워크 활동을 시뮬레이션할 것입니다. 두 경우 모두 다음 코드가 사용됩니다.

```java
Mono.empty()
    .delaySubscription(Duration.ofMillis(200))
    .then(Mono.empty()
              .delaySubscription(Duration.ofMillis(200)))
    .then()
```

WebFlux의 경우 반환 유형이 `Mono<Void>`이고 Web MVC의 경우 .block() 작업을 호출하여 실행 흐름이 종료되므로 지정된 지연 시간 동안 Thread가 차단됩니다. 여기서는 지연 스케줄링에 대해 동일한 동작을 얻기 위해 동일한 코드를 사용합니다.

우리는 또한 유사한 클라우드 설정을 사용할 것입니다. 미들웨어의 경우 E4S V3 VM(4개의 가상 CPU 및 32GB RAM)을 사용하고 클라이언트의 경우 B4MS VM(4개의 가상 CPU 및 16GB RAM)을 사용합니다.

서비스에 대해 테스트 제품군을 실행하면 다음 결과를 관찰할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/67281547-c023-42c0-bd70-7cd7963cea29.png)

도표 6.17. 추가 I/O 없이 WebFlux 대 Web MVC 처리량 측정 결과 비교

앞의 다이어그램에서 +(더하기) 기호 행은 Web MVC용이고 -(대시) 기호 행은 WebFlux용입니다. 보시다시피 전체 결과는 실제 외부 호출보다 높습니다. 즉, 애플리케이션 내의 연결 풀이나 운영 체제 내의 연결 정책이 시스템 성능에 큰 영향을 미칩니다.

그럼에도 불구하고 WebFlux는 여전히 Web MVC의 두 배의 처리량을 보여주고 있으며, 이는 마침내 연결당 스레드 모델의 비효율성에 대한 우리의 가정을 증명합니다. WebFlux는 여전히 Amdahl의 법칙이 제안한 대로 작동합니다. 그러나 응용 프로그램 제한과 함께 시스템 제한이 있으므로 최종 결과에 대한 해석이 변경될 수 있음을 기억해야 합니다.

또한 각각 다이어그램 6.18 및 6.19에 표시된 대기 시간 및 CPU 사용량과 관련하여 두 모듈을 비교할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/985c9b41-75d3-4e02-bfdb-bdfb78ecf04a.png)

도표 6.18. 추가 I/O가 없는 WebFlux와 Web MVC의 대기 시간 비교

앞의 다이어그램에서 `+` 기호 행은 Web MVC용이고 `-` 기호 행은 WebFlux용입니다. 이 경우 결과는 낮을수록 좋습니다. 앞의 다이어그램은 Web MVC의 대기 시간이 크게 저하되는 것을 보여줍니다. 12,000명의 동시 사용자의 병렬화 수준에서 WebFlux는 약 2.1배 더 나은 응답 시간을 보여줍니다.

CPU 사용량의 관점에서 볼 때 다음과 같은 경향이 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/3e62f5a4-1115-4fa4-a977-c98e800ed39c.png)

도표 6.19. 추가 I/O가 없는 WebFlux와 Web MVC의 CPU 사용량 비교

앞의 다이어그램에서 실선은 Web MVC용이고 점선은 WebFlux용입니다. 이 경우에도 결과가 낮을수록 좋습니다. WebFlux가 처리량, 대기 시간 및 CPU 사용량과 관련하여 훨씬 더 효율적이라는 결론을 내릴 수 있습니다. CPU 사용량의 차이는 서로 다른 스레드 인스턴스 간의 중복 작업 컨텍스트 전환으로 설명될 수 있습니다.


### WebFlux 처리 모델의 과제

WebFlux는 Web MVC와 크게 다릅니다. 시스템에 차단 I/O가 없기 때문에 모든 요청을 처리하기 위해 소수의 스레드 인스턴스만 사용할 수 있습니다. 이벤트를 동시에 처리하는 데 시스템의 프로세서/코어보다 많은 수의 스레드 인스턴스가 필요하지 않습니다.
WebFlux가 Netty 위에 구축되었기 때문입니다. 여기서 Thread 인스턴스의 기본 수는 `Runtime.getRuntime().availableProcessors()`에 2를 곱한 것입니다.

넌블로킹 작업을 사용하면 결과를 비동기식으로 처리할 수 있지만(다이어그램 6.11 참조) 확장성을 높이고 CPU를 더 효율적으로 활용하며 실제 처리에 CPU 주기를 사용하고 컨텍스트 전환에 대한 낭비를 줄일 수 있습니다. 처리 모델에는 고유한 함정이 있습니다. **우선 CPU 집약적인 작업은 별도의 Thread 또는 ThreadPool 인스턴스에 예약해야 한다는 점을 이해하는 것이 중요합니다.** 이 문제는 연결당 스레드 모델 또는 스레드 풀에 작업자 수가 많은 유사한 모델에는 적용되지 않습니다. 이 경우 각 연결에 이미 전용 작업자가 있기 때문입니다. 일반적으로 이러한 모델에 대한 집중적인 경험이 있는 대부분의 개발자는 이를 잊어버리고 메인 스레드에서 CPU 집약적인 작업을 실행합니다. 이와 같은 실수는 비용이 많이 들고 전체 성능에 영향을 줄 수 있습니다. 이 경우 주 스레드는 처리 중이며 새 연결을 수락하거나 처리할 시간이 없습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/623b24ee-7f6c-4e93-95ca-862ad45cafd7.png)

도표 6.20. 단일 프로세서 환경에서 CPU를 많이 사용하는 작업

앞의 다이어그램에서 볼 수 있듯이 전체 요청 처리 라인이 흰색 막대로 구성되어 있어도(즉, 차단 I/O가 없음을 의미함) 다른 요청에서 처리 시간을 훔치는 하드 계산을 실행하여 처리를 스택할 수 있습니다.

이 문제를 해결하려면 장기 실행 작업을 별도의 프로세서 풀에 위임하거나 단일 프로세스 노드의 경우 다른 노드에 작업을 위임해야 합니다. 예를 들어, 하나의 스레드가 연결을 수락한 다음 실제 처리를 다른 작업자/노드 풀에 위임하는 효율적인 이벤트 루프(https://en.wikipedia.org/wiki/Event_loop)를 구성할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/90e92bb7-1058-4245-9b1a-76fd0792909e.png)

도표 6.21. Netty와 같은 넌블로킹 서버 아키텍처

비동기식 넌블로킹 프로그래밍의 또 다른 일반적인 실수는 worker 사용을 차단하는 것입니다. 웹 애플리케이션 개발의 까다로운 부분 중 하나는 고유한 UUID를 생성하는 것입니다.
```
UUID requestUniqueId = java.util.UUID.randomUUID();
```
여기서 문제는 `#randomUUID()`가 `SecureRandom`을 사용한다는 것입니다. 일반적인 암호화 강도 난수 생성기는 애플리케이션 외부에 있는 엔트로피 소스를 사용합니다. 하드웨어 난수 생성기일 수 있지만 일반적으로 정상 작동 시 운영 체제에서 수집하는 누적된 난수입니다.

이 컨텍스트에서 임의성 개념은 마우스 움직임, 전력 변경 및 런타임 시 시스템에서 수집할 수 있는 기타 임의 이벤트와 같은 이벤트를 의미합니다.

문제는 엔트로피의 소스에는 속도 제한이 있다는 것입니다. 일정 시간 동안 이 값을 초과하면 일부 시스템의 경우 엔트로피를 읽기 위한 시스템 호출이 충분한 엔트로피를 사용할 수 있게 될 때까지 중단됩니다. 또한 스레드의 수는 UUID 생성 성능에 큰 영향을 미칩니다. UUID.randomUUID()에 대한 난수를 생성하는 SecureRandom#nextBytes(byte[] bytes)의 구현을 보면 설명할 수 있습니다.

```java
synchronized public void nextBytes(byte[] bytes) {
   secureRandomSpi.engineNextBytes(bytes);
}
```
보시다시피 #nextBytes는 동기화되어 다른 스레드에서 액세스할 때 상당한 성능 손실을 초래합니다.

SecureRandom의 해결 방법에 대해 자세히 알아보려면 다음 스택 오버플로 답변을 참조하세요. https://stackoverflow.com/questions/137212/how-to-solve-slow-java-securerandom.

우리가 배웠듯이 WebFlux는 몇 개의 스레드를 사용하여 엄청난 양의 요청을 넌블로킹 방식으로 비동기식으로 처리합니다. 언뜻 보기에는 I/O 작업이 없는 것처럼 보이지만 실제로는 OS와의 특정 상호 작용을 숨기는 방법을 사용할 때 주의해야 합니다. 이러한 방법에 대한 적절한 주의가 없으면 전체 시스템의 성능이 크게 저하될 수 있습니다. 따라서 WebFlux에서 넌블로킹 작업만 사용하는 것이 중요합니다. 그러나 이러한 요구 사항은 반응형 시스템 개발에 많은 제한을 가합니다. 예를 들어, 전체 JDK는 Java 에코시스템의 구성요소 사이의 명령적이고 동기적인 상호작용을 위해 설계되었습니다. 따라서 많은 차단 작업에는 넌블로킹, 비동기 아날로그가 없으므로 많은 넌블로킹, 반응형 시스템 개발이 복잡합니다. WebFlux는 더 높은 처리량과 더 낮은 대기 시간을 제공하지만 우리가 작업하는 모든 작업과 라이브러리에 많은 주의를 기울여야 합니다.

또한 복잡한 계산이 서비스의 중심 작업인 경우 복잡한 스레딩 기반 처리 모델이 넌블로킹, 비동기 처리 모델보다 선호됩니다. 또한 I/O와 상호 작용하는 모든 작업이 차단되는 경우 넌블로킹 I/O를 사용할 때만큼 많은 이점이 없습니다. 게다가 이벤트 처리를 위한 넌블로킹 및 비동기 알고리즘의 복잡성은 중복될 수 있으므로 Web MVC의 간단한 스레딩 모델은 WebFlux 모델보다 더 효율적입니다.

그럼에도 불구하고 이러한 제한 사항이나 특정 사용 사례가 없고 I/O 상호 작용이 많은 경우 넌블로킹 및 비동기 WebFlux가 밝게 빛날 것입니다.


### 메모리 소비에 대한 다양한 처리 모델의 영향

프레임워크 분석의 또 다른 중요한 구성 요소는 메모리 사용량을 비교하는 것입니다. 1장, 왜 Reactive Spring인가?에서 연결당 스레드 모델에 대한 논의를 다시 생각해 보면, 작은 이벤트의 객체에 메모리를 할당하는 대신 각각의 새로운 연결에 대해 거대한 전용 스레드를 할당한다는 것을 알고 있습니다. 우리가 염두에 두어야 할 첫 번째 사항은 스레드가 스택을 위한 여유 공간을 유지한다는 것입니다. 실제 스택 크기는 OS 및 JVM 구성에 따라 다릅니다. 기본적으로 64비트에서 실행되는 대부분의 일반 서버의 경우 VM 스택 크기는 1MB입니다.

이벤트는 열린 연결 또는 데이터 가용성과 같은 시스템 상태의 변경에 대한 신호를 의미합니다.
고부하 시나리오의 경우 이 기술을 사용하면 메모리 사용량이 높아집니다. 기껏해야 요청 및 응답 본문과 함께 전체 1MB 스택을 유지하기 위한 부당한 오버헤드가 있습니다. 전용 스레드 풀이 제한되면 처리량과 평균 대기 시간이 감소합니다. 따라서 Web MVC에서는 메모리 사용량과 시스템 처리량의 균형을 맞춰야 합니다. 대조적으로, 이전 섹션에서 배웠듯이 WebFlux는 고정된 수의 Thread 인스턴스를 사용하여 훨씬 더 많은 요청을 처리하는 동시에 예측 가능한 양의 메모리를 사용할 수 있습니다. 이전 측정에서 메모리가 어떻게 사용되었는지 완전히 이해하려면 메모리 사용 비교를 살펴보십시오.


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/6bfa888e-8efd-489e-8df7-893a88755a6b.png)
도표 6.22. WebFlux와 Web MVC의 메모리 사용량 비교

앞의 다이어그램에서 실선은 Web MVC용이고 점선은 WebFlux용입니다. 이 경우 낮을수록 좋습니다. 두 애플리케이션 모두 추가 JVM 매개변수(Xms26GB 및 Xmx26GB)가 제공된다는 점에 유의해야 합니다. 이는 두 응용 프로그램 모두 동일한 양의 전용 메모리에 액세스할 수 있음을 의미합니다. 그러나 Web MVC의 경우 병렬화가 증가함에 따라 메모리 사용량이 증가합니다. 이 섹션의 시작 부분에서 언급했듯이 일반적인 스레드 스택 크기는 1MB입니다. 우리의 경우 스레드 스택 크기는 -Xss512K로 설정되어 있으므로 각각의 새 스레드는 추가로 ~512KB의 메모리를 사용합니다. 따라서 연결당 스레드 모델의 경우 메모리 사용이 비효율적입니다.

반면 WebFlux의 경우 병렬화에도 불구하고 메모리 사용량이 안정적입니다. 이것은 WebFlux가 메모리를 보다 최적으로 소비한다는 것을 의미합니다. 즉, WebFlux를 사용하면 더 저렴한 서버를 사용할 수 있다는 의미입니다.

이것이 올바른 가정인지 확인하기 위해 메모리 사용량의 예측 가능성과 예측할 수 없는 상황에서 메모리 사용량이 어떻게 도움이 될 수 있는지에 대한 작은 실험을 다시 실행해 보겠습니다. 이 테스트를 위해 Web MVC 및 WebFlux를 사용하여 클라우드 인프라에 얼마나 많은 비용을 지출할 것인지 분석하려고 합니다.

시스템의 상한선을 측정하기 위해 스트레스 테스트를 수행하고 시스템이 처리할 수 있는 요청 수를 확인합니다. 웹 애플리케이션을 실행할 때 가상 CPU 1개와 RAM 2GB가 있는 Amazon EC2 t2.small 인스턴스를 시작합니다. 운영 체제는 JDK 1.8.0_144 및 VM 25.144-b01이 있는 Amazon Linux입니다. 첫 번째 측정에서는 Tomcat과 함께 Spring Boot 2.0.x 및 Web MVC를 사용합니다. 또한 최신 시스템의 일반적인 구성 요소인 네트워크 호출 및 기타 I/O 활동을 시뮬레이션하기 위해 다음과 같은 순진한 코드를 사용합니다.

```java
@RestController
@SpringBootApplication
public class BlockingDemoApplication {
   ...
   @GetMapping("/endpoint")
   public String get() throws InterruptedException {
      Thread.sleep(1000);
      return "Hello";
   }
}
```
To run our application, we will use the following command:
```sh
java -Xmx2g 
     -Xms1g
     -Dserver.tomcat.max-threads=20000 
     -Dserver.tomcat.max-connections=20000 
     -Dserver.tomcat.accept-count=20000 
     -jar blocking-demo-0.0.1-SNAPSHOT.jar
```
So, with the preceding configuration, we will check whether our system can handle up to 20,000 users without failures. If we run our load test, we will get the following results:
```
Number of simultaneous requests	Average latency (milliseconds)
100	1,271
1,000	1,429
10,000	OutOfMemoryError/Killed
```
이러한 결과는 시간이 지남에 따라 다를 수 있지만 평균적으로는 동일합니다. 보시다시피 2GB의 메모리는 연결당 10,000개의 독립 스레드를 처리하기에 충분하지 않습니다. 물론 JVM과 Tomcat의 특정 구성을 조정하고 놀아보면 결과가 약간 향상될 수 있지만 이것이 부당한 메모리 낭비 문제를 해결하지는 못합니다. 동일한 애플리케이션 서버를 유지하고 Servlet 3.1을 통해 WebFlux로 전환하면 상당한 개선을 볼 수 있습니다. 

새 웹 응용 프로그램은 다음과 같습니다.

```java
@RestController
@SpringBootApplication
public class TomcatNonBlockingDemoApplication {
   ...
   @GetMapping("/endpoint")
   public Mono<String> get() {
      return Mono.just("Hello")
                 .delaySubscription(Duration.ofSeconds(1));
   }
}
```
이 경우 I/O를 사용한 상호 작용 시뮬레이션은 비동기 및 넌블로킹이 되며 이는 유창한 Reactor 3 API에서 쉽게 사용할 수 있습니다.

WebFlux의 기본 서버 엔진은 Reactor-Netty입니다. 따라서 Tomcat 웹 서버로 전환하려면 WebFlux에서 spring-boot-starter-reactor-netty를 제외하고 spring-boot-starter-tomcat 모듈에 대한 종속성을 제공해야 합니다.

새 스택을 실행하려면 다음 명령을 사용합니다. 

```sh
java -Xmx2g 
     -Xms1g
     -Dserver.tomcat.accept-count=20000 
     -jar non-blocking-demo-tomcat-0.0.1-SNAPSHOT.jar
```
Similarly, we allocate all RAM for our Java application, but in this case, we use the default thread pool size, which is 200 threads. By running the same tests, we will get the following results:
```
Number of simultaneous requests	Average latency (milliseconds)
100	1,203
1,000	1,407
10,000	9,661
```
이 경우 애플리케이션이 훨씬 더 나은 결과를 보여줍니다. 부하가 높은 일부 사용자는 꽤 오랜 시간을 기다려야 하기 때문에 결과는 여전히 이상적이지 않습니다. 결과를 개선하기 위해 Reactor-Netty라는 진정한 반응형 서버의 처리량과 대기 시간을 확인합시다.

새 웹 애플리케이션을 실행하기 위한 코드와 명령은 동일하므로 벤치마크 결과만 다루겠습니다.
```
Number of simultaneous requests	Average latency (milliseconds)
1,000	1,370
10,000	2,699
20,000	6,310
```

보시다시피 결과가 훨씬 좋습니다. 우선 Netty의 경우 한 번에 1,000개 연결의 최소 처리량을 선택했습니다. 상한선은 20,000으로 설정되었습니다. 이것은 Netty가 서버로서 동일한 구성으로 Tomcat의 두 배 성능을 제공한다는 것을 보여주기에 충분합니다. 이 비교만으로도 WebFlux 기반 솔루션이 인프라 비용을 줄일 수 있음을 알 수 있습니다. 이제 우리 애플리케이션이 더 저렴한 서버에 적합하고 훨씬 더 효율적인 방식으로 리소스를 소비하기 때문입니다.

WebFlux 모듈과 함께 제공되는 또 다른 보너스는 들어오는 요청 본문을 더 적은 메모리 소비로 더 빠르게 처리할 수 있다는 것입니다. 이 기능은 들어오는 본문이 요소 모음이고 시스템이 각 항목을 개별적으로 처리할 수 있을 때 켜집니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/1ff7c3ef-8d27-45d5-a759-f22223770b7b.png)

도표 6.23. WebFlux는 작은 청크로 큰 데이터 배열을 처리합니다.

반응형 메시지 인코딩 및 디코딩에 대해 자세히 알아보려면 https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-codecs 링크를 참조하세요.
앞의 다이어그램에서 볼 수 있듯이 시스템은 데이터 처리를 시작하기 위해 요청 본문의 작은 부분만 필요합니다. 응답 본문을 클라이언트에 보낼 때도 마찬가지입니다. 전체 응답 본문을 기다릴 필요가 없으며 대신 오는 대로 각 요소를 네트워크에 쓰기 시작할 수 있습니다. 다음은 WebFlux를 사용하여 이를 달성하는 방법을 보여줍니다.

```java
@RestController
@RequestMapping("/api/json")
class BigJSONProcessorController {

   @GetMapping(
      value = "/process-json",
      produces = MediaType.APPLICATION_STREAM_JSON_VALUE
   )
   public Flux<ProcessedItem> processOneByOne(Flux<Item> bodyFlux) {
      return bodyFlux
         .map(item -> processItem(item))
         .filter(processedItem -> filterItem(processedItem));
   }
}
```
앞의 코드에서 알 수 있듯이 이러한 놀라운 기능은 Spring WebFlux 모듈의 내부를 해킹하지 않고도 사용할 수 있으며 사용 가능한 API를 사용하여 달성할 수 있습니다. 또한 이러한 처리 모델을 사용하면 네트워크에 첫 번째 항목을 업로드하고 응답을 수신하는 시간이 다음과 같기 때문에 첫 번째 응답을 훨씬 빠르게 반환할 수 있습니다.


스트리밍 데이터 처리 기술에서는 응답 본문의 콘텐츠 길이를 예측할 수 없으므로 단점으로 간주될 수 있습니다.
이에 비해 Web MVC는 전체 요청을 메모리에 업로드해야 합니다. 그 후에야 들어오는 본문을 처리할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/aba0ee36-432d-445a-8661-ef9f433cfcfe.png)

도표 6.24. 한 번에 많은 양의 데이터를 처리하는 웹 MVC

`@Controller`의 일반적인 선언은 다음과 같기 때문에 WebFlux에서와 같이 데이터를 반응형으로 처리하는 것은 불가능합니다.

```java
@RestController
@RequestMapping("/api/json")
class BigJSONProcessorController {

   @GetMapping("/process-json") 
   public List<ProcessedItem> processOneByOne(
      List<Item> bodyList
   ) {
      return bodyList
         .stream()
         .map(item -> processItem(item))
         .filter(processedItem -> filterItem(processedItem))
         .collect(toList());
   }
}
```
여기에서 메서드 선언은 전체 요청 본문이 특정 항목의 컬렉션으로 변환되도록 명시적으로 요구합니다. 수학적 관점에서 평균 처리 시간은 다음과 같습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/b8b1ae74-33fe-4e94-9842-414c4bc99229.png)

다시 말하지만, 첫 번째 결과를 사용자에게 반환하려면 전체 요청 본문을 처리하고 결과를 컬렉션에 집계해야 합니다. 그 후에야 시스템이 클라이언트에 응답을 보낼 수 있습니다. 이것은 WebFlux가 Web MVC보다 훨씬 적은 메모리를 사용한다는 것을 의미합니다. WebFlux는 Web MVC보다 훨씬 빠르게 첫 번째 응답을 반환할 수 있으며 무한한 데이터 스트림을 처리할 수 있습니다.


### 사용성에 대한 처리 모델의 영향

Web MVC와 WebFlux 간의 비교에는 양적 지표뿐만 아니라 질적 지표도 포함되어야 합니다. 측정되는 가장 일반적인 질적 지표 중 하나는 학습 곡선입니다. Web MVC는 엔터프라이즈 영역에서 10년 이상 적극적으로 사용된 잘 알려진 프레임워크입니다. 명령형 프로그래밍 패러다임인 가장 단순한 프로그래밍 패러다임에 의존합니다. 비즈니스의 경우 이는 일반 Spring 5 및 Web MVC를 기반으로 하는 새 프로젝트를 시작하면 숙련된 개발자를 찾는 것이 훨씬 쉽고 새 개발자를 가르치는 비용이 훨씬 저렴하다는 것을 의미합니다. 대조적으로 WebFlux의 경우 상황이 크게 다릅니다. 우선 WebFlux는 아직 충분히 입증되지 않은 신기술이며 잠재적으로 많은 버그와 취약점이 있을 수 있습니다. 기본 비동기 넌블로킹 프로그래밍 패러다임도 문제가 될 수 있습니다. 우선 Zuul을 새로운 프로그래밍 모델로 마이그레이션한 Netflix의 경험에서 알 수 있듯이 비동기식 넌블로킹 코드를 디버그하기가 어렵습니다.

비동기 프로그래밍은 콜백 기반이며 이벤트 루프에 의해 구동됩니다. 요청을 따르려고 할 때 이벤트 루프의 스택 추적은 의미가 없습니다. 이벤트와 콜백이 처리되고 디버깅에 도움이 되는 도구가 거의 없기 때문입니다. 엣지 케이스, 처리되지 않은 예외 및 잘못 처리된 상태 변경은 매달린 리소스를 생성하여 ByteBuf 누출, 파일 설명자 누출, 응답 손실 등을 초래합니다. 이러한 유형의 문제는 어떤 이벤트가 제대로 처리되지 않았거나 적절하게 정리되지 않았는지 알기 어렵기 때문에 디버그하기가 상당히 어려운 것으로 입증되었습니다. 

자세한 내용은 https://medium.com/netflix-techblog/zuul-2-the-netflix-journey-to-asynchronous-non-blocking-systems-45947377fb5c를 참조하세요.

또한 비즈니스 관점에서, 특히 Netty 스택을 사용하여 비동기 및 넌블로킹 프로그래밍에 대한 심층 지식을 갖춘 고도로 숙련된 엔지니어를 찾는 것은 비합리적일 수 있습니다. 새로운 개발자를 처음부터 가르치는 것은 많은 시간과 비용이 소요되며 완전히 이해한다는 보장도 없습니다. 운 좋게도 이 문제의 일부는 의미 있는 변환 흐름을 더 간단하게 만들고 비동기 프로그래밍의 가장 어려운 부분을 숨길 수 있는 Reactor 3을 사용하여 해결됩니다. 불행히도 Reactor는 모든 문제를 해결하지 않으며 기업의 경우 사람과 위험한 기술에 대한 예측할 수 없는 재정적 투자는 가치가 없을 수 있습니다.

정성적 분석의 또 다른 중요한 점은 기존 솔루션을 새로운 반응형 스택으로 마이그레이션하는 것입니다. Spring 팀은 프레임워크 개발 초기부터 원활한 마이그레이션을 위해 최선을 다하고 있음에도 불구하고 모든 마이그레이션 사례를 예측하기는 여전히 어렵습니다. 예를 들어, JSP, Apache Velocity 또는 유사한 서버 측 렌더링 기술에 의존하는 사람들은 전체 UI 관련 코드를 마이그레이션해야 합니다. 또한 많은 최신 프레임워크가 ThreadLocal에 의존하므로 비동기식 넌블로킹 프로그래밍으로의 원활한 이동이 어렵습니다. 이와 함께 데이터베이스와 관련된 많은 문제가 있으며 이는 7장, Reactive Database Access에서 다룹니다.




## WebFlux의 응용

이전 섹션에서 WebFlux 디자인의 기본 사항과 새로운 기능에 대해 배웠습니다. 또한 WebFlux와 Web MVC를 세밀하게 비교했습니다. 우리는 다양한 관점에서 그들의 장점과 단점을 이해했습니다. 마지막으로 이 섹션에서는 WebFlux의 응용 프로그램을 명확하게 이해하려고 합니다.


### 마이크로서비스 기반 시스템

WebFlux의 첫 번째 응용은 마이크로서비스 시스템에 있습니다. 모놀리스와 비교하여 일반적인 마이크로 서비스 시스템의 가장 두드러진 특징은 풍부한 I/O 통신입니다. I/O, 특히 블로킹 I/O가 있으면 전체 시스템 대기 시간과 처리량이 감소합니다. 연결당 스레드 모델의 경합 및 일관성은 시스템 성능을 크게 향상시키지 않습니다. 이는 서비스 간 호출이 중요한 시스템 또는 특정 서비스의 경우 WebFlux가 가장 효율적인 솔루션 중 하나가 될 것임을 의미합니다. 

이러한 서비스의 예는 지불 흐름 조정 서비스입니다.
일반적으로 계정 간 송금과 같은 간단한 작업 뒤에는 일련의 검색, 확인 및 실제 송금 실행 작업을 포함하는 숨겨진 복잡한 메커니즘이 있습니다. 예를 들어 PayPal을 사용하여 돈을 보낼 때 첫 번째 단계는 보낸 사람과 받는 사람의 계정을 검색하는 것일 수 있습니다. 그런 다음 PayPal은 모든 국가에서 모든 국가로 송금할 수 있으므로 송금이 해당 국가의 법률을 위반하지 않는지 확인하는 것이 중요합니다. 각 계정에는 고유한 제한 사항이 있을 수 있습니다. 마지막으로 수취인은 내부 PayPal 계정이나 외부 신용 카드 또는 직불 카드를 가지고 있을 수 있으므로 계정 유형에 따라 외부 시스템에 추가 호출을 해야 할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/d0878562-033f-4a3d-bb4c-208353035d32.png)

도표 6.25. PayPal 결제 흐름의 구현 예

이러한 복잡한 흐름에서 넌블로킹, 비동기 통신을 구성함으로써 다른 요청을 효율적으로 처리하고 컴퓨터 리소스를 효율적으로 활용할 수 있습니다.

### 연결이 느린 클라이언트를 처리하는 시스템

WebFlux의 두 번째 응용은 네트워크 연결이 느리거나 불안정한 클라이언트의 모바일 장치와 함께 작동하도록 설계된 시스템을 구축하는 것입니다. WebFlux가 이 영역에서 유용한 이유를 이해하기 위해 느린 연결을 처리할 때 무슨 일이 일어나고 있는지 상기해 보겠습니다. 문제는 클라이언트에서 서버로 데이터를 전송하는 데 상당한 시간이 걸릴 수 있고 해당 응답에도 많은 시간이 걸릴 수 있다는 것입니다. 연결당 스레드 모델을 사용하면 연결된 클라이언트 수가 증가하면 시스템이 충돌할 가능성이 높아집니다. 예를 들어, 서비스 거부(DoS) 공격을 사용하여 해커는 쉽게 우리 서버를 사용할 수 없게 만들 수 있습니다.

대조적으로 WebFlux를 사용하면 작업 스레드를 차단하지 않고 연결을 수락할 수 있습니다. 이런 식으로 느린 연결은 문제를 일으키지 않습니다. WebFlux는 들어오는 요청 본문을 기다리는 동안 차단하지 않고 다른 연결을 계속 수신합니다. Reactive Streams 추상화를 통해 필요할 때 데이터를 사용할 수 있습니다. 이는 서버가 네트워크의 준비 상태에 따라 이벤트 소비를 제어할 수 있음을 의미합니다.


### 스트리밍 또는 실시간 시스템

WebFlux의 또 다른 유용한 응용 프로그램은 실시간 및 스트리밍 시스템입니다. WebFlux가 여기에서 도움이 되는 이유를 이해하기 위해 실시간 및 스트리밍 시스템이 무엇인지 상기해 보겠습니다.

우선, 이러한 시스템은 낮은 대기 시간과 높은 처리량을 특징으로 합니다. 스트리밍 시스템의 경우 대부분의 데이터가 서버 측에서 나가므로 클라이언트 측이 소비자 역할을 합니다. 서버 측보다 클라이언트 측의 이벤트가 더 적은 것이 일반적입니다. 그러나 온라인 게임과 같은 실시간 시스템의 경우 들어오는 데이터의 양은 나가는 데이터의 양과 같습니다.

넌블로킹 통신을 사용하여 짧은 대기 시간과 높은 처리량을 달성할 수 있습니다. 이전 섹션에서 배운 것처럼 넌블로킹, 비동기 통신은 효율적인 리소스 활용을 가능하게 합니다. Netty 또는 유사한 프레임워크를 기반으로 하는 시스템에서 가장 높은 처리량과 가장 낮은 대기 시간이 나타났습니다. 그러나 이러한 리액티브 프레임워크에는 채널과 콜백을 사용하는 복잡한 상호 작용 모델이라는 고유한 단점이 있습니다.

그럼에도 불구하고 반응형 프로그래밍은 이 두 가지 문제에 대한 우아한 솔루션입니다. 4장, 프로젝트 리액터 - 리액티브 앱의 기초에서 배웠듯이 리액티브 프로그래밍, 특히 리액터 3과 같은 리액티브 라이브러리를 사용하면 기본 코드 기반 복잡성과 허용 가능한 학습 곡선. 두 솔루션 모두 WebFlux에 통합되어 있습니다. Spring Framework를 사용하면 이러한 시스템을 쉽게 구축할 수 있습니다.




## WebFlux in action

실제 시나리오에서 WebFlux를 사용하는 방법을 배우기 위해 WebClient를 사용하여 원격 Gitter Streams API에 연결하고 Project Reactor API를 사용하여 데이터를 변환한 다음 변환된 메시지를 SSE를 사용하여 브로드캐스트하는 간단한 웹 애플리케이션을 구축할 것입니다. 다음 다이어그램은 시스템의 개략도를 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781787284951/files/assets/bb8ea4a9-02df-413f-bc32-40bbab6c7dbf.png)

도표 6.26. 스트리밍 애플리케이션 설계

앞의 다이어그램은 다음과 같이 설명할 수 있습니다.

(1) Gitter API와의 통합 지점입니다. 앞의 다이어그램에서 볼 수 있듯이 서버와 Gitter 간의 통신은 스트리밍입니다. 따라서 반응형 프로그래밍은 자연스럽게 거기에 맞습니다.

(2) 들어오는 메시지를 처리하고 다른 보기로 변환해야 하는 시스템의 지점입니다.

(3) 수신된 메시지를 캐싱하고 연결된 각 클라이언트에 브로드캐스트하는 지점입니다.

(4) 연결된 브라우저를 나타냅니다.

우리가 볼 수 있듯이 시스템에는 네 가지 핵심 구성 요소가 있습니다. 이 시스템을 구축하기 위해 다음 클래스와 인터페이스를 만들 것입니다.

- ChatService: 원격 서버와의 통신을 연결하는 인터페이스입니다. 해당 서버에서 메시지를 수신할 수 있는 기능을 제공합니다.
- GitterService: 새 메시지를 수신하기 위해 Gitter 스트리밍 API에 연결하는 ChatService 인터페이스의 구현입니다.
- InfoResource: 사용자 요청을 처리하고 메시지 스트림으로 응답하는 핸들러 클래스입니다.

시스템 구현을 위한 첫 번째 단계는 ChatService 인터페이스를 분석하는 것입니다. 
다음 샘플은 필요한 방법을 보여줍니다.

```java
interface ChatService<T> {
   Flux<T> getMessagesStream();
   Mono<List<T>> getMessagesAfter(String messageId);
}
```
앞의 예제 인터페이스는 메시지 읽기 및 수신과 관련된 최소한의 필수 기능을 다룹니다. 여기서 getMessagesStream 메서드는 채팅에서 무한한 새 메시지 스트림을 반환하는 반면 getMessagesAfter는 특정 메시지 ID를 가진 메시지 목록을 검색할 수 있습니다.

두 경우 모두 Gitter는 HTTP를 통해 메시지에 대한 액세스를 제공합니다. 즉, 일반 WebClient를 사용할 수 있습니다. 다음은 getMessagesAfter를 구현하고 원격 서버에 액세스하는 방법의 예입니다.

```java
Mono<List<MessageResponse>> getMessagesAfter(                      
   String messageId                                                
) {                                                                
   ...                                                             
   return webClient                                                // (1)
      .get()                                                       // (2)
      .uri(...)                                                    // (3)
      .retrieve()                                                  // (4)
      .bodyToMono(                                                 // (5)
         new ParameterizedTypeReference<List<MessageResponse>>() {}
      )                                                            
      .timeout(Duration.ofSeconds(1))                              // (6)
      .retryBackoff(Long.MAX_VALUE, Duration.ofMillis(500));       // (7)
}
```
앞의 코드 샘플은 Gitter 서비스와의 일반 요청-응답 상호 작용을 구성하는 방법을 보여줍니다. 여기서 (1) 지점에서 원격 Gitter 서버(3)에 대한 GET HTTP 메서드 호출(2)을 실행하기 위해 WebClient 인스턴스를 사용합니다. 그런 다음 (4) 지점에서 정보를 검색하고 WebClient DSL을 사용하여 지점 (5)에서 MessageResponse 목록의 Mono로 변환합니다. 그런 다음 외부 서비스와의 통신에 대한 복원력을 제공하기 위해 지점 (6)에서 호출에 대한 제한 시간을 제공하고 오류가 있는 경우 지점 (7)에서 호출을 재시도합니다.

스트리밍 Gitter API와 통신하는 것은 그만큼 간단합니다. 
다음은 Gitter 서버의 JSON 스트리밍(application/stream+json) 엔드포인트에 연결하는 방법을 보여줍니다.

```java
public Flux<MessageResponse> getMessagesStream() {                 
   return webClient                                                
      .get()                                                       // (1)
      .uri(...)                                                    
      .retrieve()                                                  
      .bodyToFlux(MessageResponse.class)                           // (2)
      .retryBackoff(Long.MAX_VALUE, Duration.ofMillis(500));       
}
                                                                  
```
앞의 코드에서 볼 수 있듯이 (1)에서 볼 수 있듯이 이전과 동일한 API를 사용합니다. 우리가 만든 유일한 변경 사항은 숨겨진 URI와 지점 (2)에 표시된 것처럼 Mono 대신 Flux에 매핑한다는 사실뿐입니다. 내부적으로 WebClient는 컨테이너에서 사용 가능한 디코더를 사용합니다. 무한 스트림이 있는 경우 스트림의 끝을 기다리지 않고 즉석에서 요소를 변환할 수 있습니다.

마지막으로 두 스트림을 하나로 결합하고 캐시하기 위해 InfoResource 핸들러에 대한 구현을 제공하는 다음 코드를 구현할 수 있습니다.

```java
@RestController                                                    // (1)
@RequestMapping("/api/v1/info")                                    
public class InfoResource {                                        

   final ReplayProcessor<MessageVM> messagesStream                 // (2)
      = ReplayProcessor.create(50);                                
 
   public InfoResource(                                            // (3)
      ChatService<MessageResponse> chatService                     
   ) {                                                             
     Flux.mergeSequential(                                         // (3.1)
            chatService.getMessageAfter(null)                      // (3.2)
                       .flatMapIterable(Function.identity())       
            chatService.getMessagesStream()                        // (3.3)
         )                                                         
         .map(...)                                                 // (3.4)
         .subscribe(messagesStream);                               // (3.5)
   }

   @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)       // (4)
   public Flux<MessageResponse> stream() {                         
      return messagesStream;                                       // (4.1)
   }                                                               
}                                                                  
```

The preceding code can be explained as follows:

(2) 이것은 ReplayProcessor 필드 선언입니다. 4장, 프로젝트 리액터 - 리액티브 앱의 기초에서 기억할 수 있듯이 ReplayProcessor를 사용하면 미리 정의된 수의 요소를 캐시하고 각 새 구독자에게 최신 요소를 재생할 수 있습니다.

(3) 여기에 InfoResource 클래스의 생성자 선언이 있습니다. 생성자 내에서 Gitter의 최신 메시지 스트림을 병합하는 처리 흐름을 구축합니다(3.1 및 3.2 지점 참조). null ID의 경우 Gitter는 30개의 최신 메시지를 반환합니다. 처리 흐름은 또한 지점(3.3)에 표시된 것처럼 거의 실시간으로 새 메시지 스트림을 수신합니다. 그런 다음, 모든 메시지는 (3.4) 지점에 표시된 것처럼 보기 모델에 매핑되고 스트림은 ReplayProcessor에 의해 즉시 구독됩니다. 즉, InfoResource 빈이 구성되면 Gitter 서비스에 연결하고 최신 메시지를 캐시하고 업데이트 수신을 시작합니다. mergeSequential은 두 스트림을 동시에 구독하지만 두 번째 스트림에서 메시지 전송을 시작하지만 첫 번째 스트림이 완료된 경우에만 시작됩니다. 첫 번째 스트림은 유한한 스트림이므로 최신 메시지를 수신하고 getMessagesStream Flux에서 대기 중인 메시지를 보내기 시작합니다.

(4) 이것은 지정된 끝점에 대한 각각의 새 연결에서 호출되는 처리기 메서드 선언입니다. 여기에서 포인트 (4.1)에 표시된 ReplayProcessor 인스턴스를 반환하면 최신 캐시된 메시지를 공유하고 사용 가능한 새 메시지를 보낼 수 있습니다.

앞의 예에서 볼 수 있듯이 스트림을 적절한 순서로 병합하거나 최신 50개 메시지를 캐싱하여 모든 구독자에게 동적으로 브로드캐스트하는 것과 같은 복잡한 기능을 제공하는 데 많은 노력이나 작성된 코드가 필요하지 않습니다. Reactor와 WebFlux는 가장 어려운 부분을 다루며 우리가 비즈니스 로직을 작성할 수 있도록 합니다. 이것은 I/O와의 효율적인 넌블로킹 상호작용을 가능하게 합니다. 따라서 이 강력한 툴킷을 사용하여 높은 처리량과 낮은 대기 시간 시스템을 달성할 수 있습니다.

## 요약

이 장에서 우리는 WebFlux가 오래된 Web MVC 프레임워크를 효율적으로 대체한다는 것을 배웠습니다. 우리는 또한 WebFlux가 요청 핸들러 선언에 대해 동일한 기술을 사용한다는 것을 배웠습니다(잘 알려진 @RestController 및 @Controller 사용). 표준 핸들러 선언 외에도 WebFlux는 RouterFunction을 사용하여 가볍고 기능적인 끝점 선언을 도입합니다. 오랫동안 Spring Framework 사용자는 Netty와 같은 최신 반응형 웹 서버와 비차단 Undertow 기능을 사용할 수 없었습니다. WebFlux 웹 프레임워크를 통해 이러한 기술은 동일하고 친숙한 API를 사용하여 사용할 수 있게 되었습니다. WebFlux는 비동기식 non-blocking 통신을 기반으로 하기 때문에 이 프레임워크는 모듈의 핵심 구성 요소인 Reactor 3에 의존합니다.

또한 새로운 WebFlux 모듈에 도입된 변경 사항을 살펴보았습니다. 여기에는 Reactor 3 Reactive Types를 기반으로 한 사용자와 서버 간의 통신 변경 사항이 포함됩니다. 특히 새로운 WebClient 기술을 사용하여 서버와 외부 서비스 간의 통신 변경, WebSocket을 통한 클라이언트-서버 통신을 허용하는 새로운 WebSocketClient. 또한 WebFlux는 라이브러리 간 프레임워크입니다. 즉, 모든 Reactive Streams 기반 라이브러리가 여기에서 지원되며 기본 Reactor 3 라이브러리 또는 기타 선호하는 라이브러리를 대체할 수 있습니다.

그 후 이 장에서는 WebFlux와 Web MVC를 다양한 관점에서 자세히 비교하는 방법을 소개했습니다. 요약하자면, 대부분의 경우 WebFlux는 고부하 웹 서버에 적합한 솔루션이며 모든 성능 결과에서 Web MVC보다 2배의 성능을 보입니다. WebFlux 모듈을 사용하여 얻을 수 있는 비즈니스 이점을 살펴보고 WebFlux가 작업을 단순화하는 방법을 고려했습니다. 우리는 또한 이 기술의 함정을 살펴보았습니다.

마지막으로 WebFlux가 가장 적절한 솔루션인 몇 가지 사용 사례에 대해 배웠습니다. 이러한 사례는 마이크로서비스 시스템, 실시간 스트리밍 시스템, 온라인 게임 및 기타 유사한 응용 분야로, 낮은 지연 시간, 높은 처리량, 낮은 메모리 공간, 효율적인 CPU 활용 등의 중요한 특성이 있습니다.

웹 애플리케이션의 핵심 측면에 대해 배웠지만 데이터베이스와의 상호 작용이라는 훨씬 더 중요한 부분이 있습니다. 다음 장에서는 반응형 통신을 지원하는 데이터베이스와 반응형 통신의 주요 기능과 반응형 지원이 없을 때 해야 할 일에 대해 알아보겠습니다.