# 06 WebFlux Async Non-Blocking Communication

In the previous chapter, we started to take a look at Spring Boot 2.x. We saw that a lot of useful updates and modules have arrived with the fifth version of the Spring Framework, and we also looked at the Spring WebFlux module.

In this chapter, we are going to take a look at that module in detail. We will compare the internal design of WebFlux with good old Web MVC and try to understand the strengths and weaknesses of both. We are also going to build a simple web application with WebFlux.

This chapter covers the following topics:

A bird's-eye view of Spring WebFlux
Spring WebFlux versus Spring Web MVC
A comprehensive design overview of Spring WebFlux

## WebFlux as a central reactive server foundation

1장, 왜 Reactive Spring인가? 4장, Project Reactor - Reactive Apps의 기초에서 보았듯이 애플리케이션 서버의 새로운 시대는 개발자를 위한 새로운 기술을 가져왔습니다. 웹 애플리케이션 분야에서 Spring Framework의 진화 초기부터 Spring 웹 모듈을 Java EE의 Servlet API와 통합하기로 결정했습니다. Spring Framework의 전체 인프라는 Servlet API를 중심으로 구축되며 밀접하게 결합되어 있습니다. 예를 들어 Spring Web MVC 전체는 Front Controller 패턴을 기반으로 합니다. 그 패턴은 javax.servlet.http.HttpServlet 클래스를 간접적으로 확장하는 org.springframework.web.servlet.DispatcherServlet 클래스에 의해 Spring Web MVC에서 구현된다.

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
> Spring MVC는 매핑, 바인딩 및 유효성 검사 기능과 함께 `HttpServletRequest` 및 `HttpServletResponse`와의 직접적인 상호 작용을 지원합니다. 그러나 이러한 클래스를 사용할 때 Servlet API에 대한 추가 직접적인 종속성이 있습니다. 이것은 Web MVC에서 WebFlux 또는 Spring용 다른 웹 확장으로의 마이그레이션 프로세스를 복잡하게 만들 수 있기 때문에 나쁜 습관으로 간주될 수 있습니다. 대신 `org.springframework.http.RequestEntity` 및 `org.springframework.http.ResponseEntity`를 사용하는 것이 좋습니다. 이러한 클래스는 웹 서버 구현에서 요청 및 응답 개체를 분리합니다.

Spring Web MVC 접근 방식은 수년간 편리한 프로그래밍 모델이었습니다. 웹 애플리케이션 개발을 위한 견고하고 안정적인 골격임이 입증되었습니다. 이것이 2003년에 Spring Framework가 Servlet API 위에 웹 애플리케이션을 구축하기 위한 가장 인기 있는 솔루션 중 하나가 되기 시작한 이유입니다. 그러나 과거의 방법론과 기술은 현대의 데이터 집약적 시스템의 요구 사항에 잘 맞지 않습니다.

Servlet API가 비동기식 비차단 통신(버전 3.1부터)을 지원한다는 사실에도 불구하고 Spring MVC 모듈의 구현에는 많은 간격이 있고 요청 수명 주기 전체에 걸쳐 비차단 작업을 허용하지 않습니다. 예를 들어 즉시 사용 가능한 비차단 HTTP 클라이언트가 없으므로 외부 상호 작용으로 인해 차단 IO 호출이 발생할 가능성이 큽니다. 5장, Spring Boot 2로 반응하기에서 언급했듯이 Web MVC 추상화는 비차단 Servlet API 3.1의 모든 기능을 지원하지 않습니다. 그렇게 되기 전까지는 Spring Web MVC는 고부하 프로젝트를 위한 프레임워크로 간주될 수 없습니다. 이전 Spring에서 웹 추상화의 또 다른 단점은 Spring 웹 기능을 재사용하거나 Netty와 같은 비서블릿 서버에 대한 프로그래밍 모델을 유연하게 사용할 수 없다는 것입니다.

그렇기 때문에 지난 몇 년 동안 Spring Framework 팀의 핵심 과제는 동일한 주석 기반 프로그래밍 모델을 허용하고 비동기식 논블로킹 서버의 모든 이점을 동시에 제공하는 새로운 솔루션을 구축하는 것이었습니다.