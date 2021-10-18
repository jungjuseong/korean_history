# 19 반응형 앱을 위한 스프링 시큐리티

이 장에서는 다음을 다룹니다.

- 반응형 애플리케이션과 함께 Spring Security 사용
- OAuth 2로 설계된 시스템에서 반응형 앱 사용

Reactive는 애플리케이션을 개발할 때 다른 사고 방식을 적용하는 프로그래밍 패러다임입니다. 반응형 프로그래밍은 널리 받아들여진 웹 앱을 개발하는 강력한 방법입니다. 몇 년 전만 해도 어떤 중요한 회의에서 반응형 앱에 대해 논의하는 프레젠테이션이 최소한 몇 개 있을 때 유행이 되었다고 말할 수도 있습니다. 그러나 소프트웨어 개발의 다른 기술과 마찬가지로 반응형 프로그래밍은 모든 상황에 적용할 수 있는 솔루션을 나타내지 않습니다.

어떤 경우에는 반응적 접근 방식이 매우 적합합니다. 다른 경우에는 삶이 복잡해질 수 있습니다. 그러나 결국, 반응적 접근 방식은 명령형 프로그래밍의 몇 가지 제한 사항을 해결하고 이러한 제한 사항을 피하기 위해 사용되기 때문에 존재합니다. 이러한 제한 사항 중 하나는 어떻게든 조각화될 수 있는 대규모 작업을 실행하는 것과 관련이 있습니다. 

명령형 접근 방식을 사용하면 응용 프로그램에 실행할 작업을 부여하고 응용 프로그램은 이를 해결할 책임이 있습니다. 작업이 큰 경우 응용 프로그램에서 해결하는 데 상당한 시간이 걸릴 수 있습니다. 작업을 할당한 클라이언트는 응답을 받기 전에 작업이 완전히 해결될 때까지 기다려야 합니다. 반응형 프로그래밍을 사용하면 앱이 일부 하위 작업에 동시에 접근할 수 있도록 작업을 나눌 수 있습니다. 이러한 방식으로 클라이언트는 처리된 데이터를 더 빨리 수신합니다.

이 장에서는 Spring Security를 ​​사용하여 반응형 애플리케이션에서 애플리케이션 수준 보안을 구현하는 방법에 대해 설명합니다. 다른 애플리케이션과 마찬가지로 보안은 반응형 앱의 중요한 측면입니다. 그러나 반응형 앱은 다르게 설계되었기 때문에 Spring Security는 이 책에서 이전에 논의한 기능을 구현하는 방식을 조정했습니다.

19.1에서 Spring 프레임워크로 반응형 앱을 구현하는 간단한 개요로 시작하겠습니다. 그런 다음 이 책에서 배운 보안 기능을 보안 앱에 적용합니다. 

19.2에서 리액티브 앱의 사용자 관리에 대해 논의하고 섹션 19.3에서 권한 부여 규칙 적용을 계속할 것입니다. 

마지막으로 섹션 19.4에서 OAuth 2를 통해 설계된 시스템에서 반응형 애플리케이션을 구현하는 방법을 배우게 됩니다. 

반응형 애플리케이션과 관련하여 Spring Security 관점에서 변경되는 사항을 배우고 물론 이를 예제와 함께 적용합니다.

## 19.1 반응형 앱이란 무엇입니까?

반응형 앱에 대해 간략하게 설명합니다. 이 장은 반응형 앱에 보안을 적용하는 것에 관한 것이므로 이 섹션에서는 Spring Security 구성에 대해 더 깊이 들어가기 전에 반응형 앱의 필수 사항을 이해했는지 확인하고 싶습니다. 리액티브 애플리케이션의 주제는 크기 때문에 여기서는 리액티브 앱의 주요 측면만 복습으로 검토합니다. 반응형 앱이 어떻게 작동하는지 아직 알지 못하거나 더 자세히 이해해야 하는 경우 Craig Walls의 Spring in Action(Manning, 2020)의 10장을 읽는 것이 좋습니다.

https://livebook.manning.com/book/spring-in-action-sixth-edition/chapter-10/

반응형 앱을 구현할 때 두 가지 방식을 사용하여 기능을 구현합니다. 다음 목록은 이러한 접근 방식에 대해 자세히 설명합니다.

- **명령형 접근 방식**을 사용하면 앱에서 대량의 데이터를 한 번에 처리합니다. 예를 들어 클라이언트 앱은 서버에 의해 노출된 끝점을 호출하고 처리해야 하는 모든 데이터를 백엔드로 보냅니다. 사용자가 파일을 업로드하는 기능을 구현한다고 가정해 보겠습니다. 사용자가 여러 파일을 선택하고 백엔드 앱에서 이 모든 파일을 수신하여 한 번에 처리하는 경우 명령적 접근 방식으로 작업하고 있는 것입니다.

- **반응적 접근 방식**을 사용하면 앱이 데이터를 조각으로 수신하고 처리합니다. 모든 데이터를 처음부터 완전히 사용할 수 있어야 처리할 수 있는 것은 아닙니다. 백엔드는 데이터를 받는 대로 데이터를 수신하고 처리합니다. 사용자가 일부 파일을 선택하고 백엔드에서 파일을 업로드하고 처리해야 한다고 가정해 보겠습니다. 백엔드는 처리하기 전에 모든 파일을 한 번에 수신할 때까지 기다리지 않습니다. 백엔드는 파일을 하나씩 수신하고 더 많은 파일이 올 때까지 기다리는 동안 각각을 처리할 수 있습니다.

그림 19.1은 두 가지 프로그래밍 접근 방식에 대한 비유를 보여줍니다. 우유를 병에 넣는 공장을 상상해보십시오. 공장에서 아침에 모든 우유를 받고 병입이 완료되면 우유를 배달하면 명령형(필수)이라고 합니다. 공장에서 하루 종일 우유를 받고 주문에 필요한 충분한 우유를 병에 채우고 나면 주문을 배달하고 반응적이라고 합니다. 분명히 우유 공장의 경우 비반응적 접근보다 사후적 접근 방식을 사용하는 것이 더 유리합니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH19_F01_Spilca.png)

그림 19.1 무반응 대 반응성. 무반응 접근 방식에서 우유 공장은 아침에 포장할 모든 우유를 가져오고 저녁에 모든 상자를 배달합니다. 반응적 접근 방식에서는 우유가 공장으로 운반될 때 포장됩니다.
```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
다음으로 데모 끝점의 정의를 유지하기 위해 간단한 HelloController를 정의합니다. 목록 19.1은 HelloController 클래스의 정의를 보여줍니다. 끝점의 정의에서 반환 유형으로 Mono를 사용했음을 관찰할 수 있습니다. Mono는 Reactor 구현에 의해 정의된 필수 개념 중 하나입니다. Reactor로 작업할 때 게시자(데이터 소스)를 정의하는 Mono 및 Flux를 자주 사용합니다. Reactive Streams 사양에서 게시자는 게시자 인터페이스로 설명됩니다. 이 인터페이스는 Reactive Streams와 함께 사용되는 필수 계약 중 하나를 설명합니다. 다른 계약은 가입자입니다. 이 계약은 데이터를 사용하는 구성 요소를 설명합니다.

무언가를 반환하는 끝점을 디자인할 때 끝점은 게시자가 되므로 게시자 구현을 반환해야 합니다. Project Reactor를 사용하는 경우 Mono 또는 Flux가 됩니다. Mono는 단일 값의 게시자이고 Flux는 여러 값의 게시자입니다. 그림 19.2는 이러한 구성 요소와 이들 간의 관계를 설명합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH19_F02_Spilca.png)

그림 19.2 반응 스트림에서 게시자는 값을 생성하고 구독자는 해당 값을 소비합니다. Reactive Streams 사양에 정의된 계약은 게시자와 구독자를 설명합니다. Project Reactor는 Reactive Streams 사양을 구현하고 게시자 및 구독자 계약을 구현합니다. 그림에서 이 장의 예제에서 사용하는 구성 요소는 음영 처리되어 있습니다.

이 설명을 더 정확하게 하기 위해 우유 공장 비유로 돌아가 보겠습니다. 우유 공장은 처리할 우유를 수신하는 엔드포인트를 노출하는 반응형 백엔드 구현입니다. 이 끝점은 무언가(병에 담긴 우유)를 생산하므로 게시자를 반환해야 합니다. 두 병 이상의 우유가 요청되면 우유 공장은 0개 이상의 생산된 값을 처리하는 Project Reactor의 게시자 구현인 Flux를 반환해야 합니다.

목록 19.1 HelloController 클래스의 정의
@RestController
public class HelloController {

  @GetMapping("/hello")
  public Mono<String> hello() {
    return Mono.just("Hello!");      ❶
  }
}
❶ 스트림에 하나의 값으로 Mono 스트림 소스를 생성하고 반환합니다.

이제 응용 프로그램을 시작하고 테스트할 수 있습니다. 앱의 터미널에서 가장 먼저 관찰한 것은 Spring Boot가 더 이상 Tomcat 서버를 구성하지 않는다는 것입니다. Spring Boot는 기본적으로 웹 애플리케이션용 Tomcat을 구성하는 데 사용되었으며 이 책에서 이전에 개발한 예제에서 이 측면을 관찰했을 수 있습니다. 대신 이제 Spring Boot는 Netty를 Spring Boot 프로젝트의 기본 반응 웹 서버로 자동 구성합니다.

끝점을 호출할 때 관찰할 수 있는 두 번째 사항은 비반응적 접근 방식으로 개발된 끝점과 다르게 동작하지 않는다는 것입니다. 여전히 HTTP 응답 본문에서 Hello!를 찾을 수 있습니다. 엔드포인트가 정의된 Mono 스트림에서 반환하는 메시지입니다. 다음 코드 스니펫은 엔드포인트를 호출할 때 앱의 동작을 나타냅니다.

```sh
curl http://localhost:8080/hello
```
응답 본문은
```sh
Hello!
```
그러나 Spring Security 측면에서 반응적 접근 방식이 다른 이유는 무엇입니까? 뒤에서 반응 구현은 스트림의 작업을 해결하기 위해 여러 스레드를 사용합니다. 즉, 명령형 접근 방식으로 설계된 웹 앱에 사용하는 요청당 스레드 1개라는 철학을 변경합니다(그림 19.3). 그리고 여기에서 더 많은 차이점이 있습니다.

- SecurityContext 구현은 반응형 애플리케이션에서 동일한 방식으로 작동하지 않습니다. SecurityContext는 ThreadLocal을 기반으로 하며 이제 요청당 하나 이상의 스레드가 있음을 기억하십시오. (이 구성요소는 5장에서 논의했습니다.)

- SecurityContext로 인해 이제 모든 권한 부여 구성이 영향을 받습니다. 권한 부여 규칙은 일반적으로 SecurityContext에 저장된 인증 인스턴스에 의존한다는 것을 기억하십시오. 따라서 이제 엔드포인트 계층에 적용된 보안 구성과 전역 메서드 보안 기능이 영향을 받습니다.

- 사용자 세부 정보 검색을 담당하는 구성 요소인 UserDetailsService는 데이터 소스입니다. 이 때문에 사용자 세부 정보 서비스도 반응적 접근 방식을 지원해야 합니다. (이 계약에 대해서는 2장에서 배웠습니다.)

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH19_F03_Spilca.png)
그림 19.3 그림에서 각 화살표는 다른 스레드의 타임라인을 나타내고 사각형은 요청 A, B 및 C에서 처리된 작업을 나타냅니다. 리액티브 앱에서는 한 요청의 작업이 여러 스레드에서 처리될 수 있기 때문에 인증 세부 정보는 더 이상 스레드 수준에 저장할 수 없습니다.

다행히 Spring Security는 반응형 앱에 대한 지원을 제공하고 더 이상 명령형 앱에 대한 구현을 사용할 수 없는 모든 경우를 다룹니다. 반응형 앱을 위한 Spring Security로 보안 구성을 구현하는 방법을 논의하여 이 장에서 계속할 것입니다. 섹션 19.2에서 사용자 관리 구현으로 시작하여 섹션 19.3에서 엔드포인트 권한 부여 규칙 적용으로 계속하여 보안 컨텍스트가 반응형 앱에서 작동하는 방식을 알아봅니다. 그런 다음 명령형 앱의 전역 메서드 보안을 대체하는 사후 메서드 보안에 대한 논의를 계속할 것입니다.

## 19.2 반응형 앱의 사용자 관리

종종 응용 프로그램에서 사용자 인증 방식은 사용자 이름과 암호 자격 증명 쌍을 기반으로 합니다. 이 접근 방식은 기본이며 2장에서 구현한 가장 간단한 응용 프로그램부터 시작하여 논의했습니다. 그러나 반응형 응용 프로그램에서는 사용자 관리를 처리하는 구성 요소의 구현도 변경됩니다. 이 섹션에서는 반응형 앱에서 사용자 관리를 구현하는 방법에 대해 설명합니다.

애플리케이션 컨텍스트에 ReactiveUserDetailsService를 추가하여 섹션 19.1에서 시작한 ssia-ch19-ex1 애플리케이션의 구현을 계속합니다. 인증된 사용자만 /hello 엔드포인트를 호출할 수 있도록 하고 싶습니다. 이름에서 알 수 있듯이 ReactiveUserDetailsService 계약은 반응형 앱에 대한 사용자 세부 정보 서비스를 정의합니다.

계약의 정의는 UserDetailsService에 대한 정의만큼 간단합니다. ReactiveUserDetailsService는 사용자 이름으로 사용자를 검색하기 위해 Spring Security에서 사용하는 메소드를 정의합니다. 차이점은 ReactiveUserDetailsService에서 설명하는 메서드가 UserDetailsService에서 발생하는 것처럼 UserDetails가 아니라 Mono<UserDetails>를 직접 반환한다는 것입니다. 다음 코드 조각은 ReactiveUserDetailsService 인터페이스의 정의를 보여줍니다.

```java
public interface ReactiveUserDetailsService {
  Mono<UserDetails> findByUsername(String username);
}
```
UserDetailsService의 경우처럼 Spring Security에 사용자 세부 정보를 얻을 수 있는 방법을 제공하기 위해 ReactiveUserDetailsService의 사용자 정의 구현을 작성할 수 있습니다. 이 데모를 단순화하기 위해 Spring Security에서 제공하는 구현을 사용합니다. 

MapReactiveUserDetailsService 구현은 사용자 세부 정보를 메모리에 저장합니다(2장에서 배운 InMemoryUserDetailsManager와 동일). 다음 코드 조각이 제시하는 것처럼 ssia-ch19-ex1 프로젝트의 pom.xml 파일을 변경하고 Spring Security 종속성을 추가합니다. 
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
그런 다음 구성 클래스를 만들고 ReactiveUserDetailsService 및 PasswordEncoder를 Spring Security 컨텍스트에 추가합니다. 구성 클래스의 이름을 ProjectConfig로 지정했습니다. 이 클래스의 정의는 목록 19.2에서 찾을 수 있습니다. ReactiveUserDetailsService를 사용하여 사용자 이름 john, 암호 12345, 권한 읽기를 사용하여 사용자 한 명을 정의합니다. 

관찰할 수 있듯이 UserDetailsService로 작업하는 것과 유사합니다. ReactiveUserDetailsService 구현의 주요 차이점은 메서드가 UserDetails 인스턴스 자체 대신 UserDetails를 포함하는 반응 게시자 개체를 반환한다는 것입니다. Spring Security는 통합에 대한 나머지 작업을 수행합니다.

Listing 19.2 The ProjectConfig class
```java
@Configuration
public class ProjectConfig {

  @Bean ❶
  public ReactiveUserDetailsService userDetailsService() {
    var  u = User.withUsername("john") ❷
              .password("12345")
              .authorities("read")
              .build();

    var uds = new MapReactiveUserDetailsService(u); ❸

    return uds;
  }

  @Bean ❹
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```
❶ Spring 컨텍스트에 ReactiveUserDetailsService 추가

❷ 사용자 이름, 암호 및 권한을 사용하여 새 사용자를 만듭니다.

❸ UserDetails 인스턴스를 관리하기 위해 MapReactiveUserDetailsService 생성

❹ Spring 컨텍스트에 PasswordEncoder 추가

지금 애플리케이션을 시작하고 테스트하면 적절한 자격 증명을 사용하여 인증할 때만 엔드포인트를 호출할 수 있음을 알 수 있습니다. 우리가 추가한 유일한 사용자 레코드이기 때문에 비밀번호 12345로만 john을 사용할 수 있습니다. 다음 코드는 유효한 자격 증명으로 엔드포인트를 호출할 때 앱의 동작을 보여줍니다.
```
curl -u john:12345 http://localhost:8080/hello
```
The response body is
```
Hello!
```
그림 19.4는 이 애플리케이션에서 사용하는 아키텍처를 설명합니다. 뒤에서 AuthenticationWebFilter가 HTTP 요청을 가로챕니다. 이 필터는 인증 책임을 인증 관리자에게 위임합니다. 인증 관리자는 ReactiveAuthenticationManager 계약을 구현합니다. 명령형 앱과 달리 인증 공급자가 없습니다. ReactiveAuthenticationManager는 인증 로직을 직접 구현합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH19_F04_Spilca.png)

그림 19.4 AuthenticationWebFilter는 요청을 가로채고 인증 책임을 ReactiveAuthenticationManager에 위임합니다. 인증 논리에 사용자 및 암호가 포함된 경우 ReactiveAuthenticationManager는 ReactiveUserDetailsService를 사용하여 사용자 세부 정보를 찾고 PasswordEncoder를 사용하여 암호를 확인합니다.

사용자 정의 인증 로직을 생성하려면 ReactiveAuthenticationManager 인터페이스를 구현하십시오. 반응형 앱의 아키텍처는 이 책 전체에서 명령형 응용 프로그램에 대해 이미 논의한 것과 크게 다르지 않습니다. 그림 19.4에 나와 있는 것처럼 인증에 사용자 자격 증명이 포함된 경우 ReactiveUserDetailsService를 사용하여 사용자 세부 정보를 얻고 PasswordEncoder를 사용하여 암호를 확인합니다.

또한 프레임워크는 사용자가 요청할 때 인증 인스턴스를 주입하는 것을 여전히 알고 있습니다. Mono<Authentication>을 컨트롤러 클래스의 메서드에 매개 변수로 추가하여 인증 세부 정보를 요청합니다. Listing 19.3은 컨트롤러 클래스에 대한 변경 사항을 보여줍니다. 다시 말하지만 중요한 변경 사항은 반응 게시자를 사용한다는 것입니다. 반응이 없는 앱에서 사용한 일반 인증 대신 Mono<Authentication>을 사용해야 합니다.

목록 19.3 HelloController 클래스
```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public Mono<String> hello(
    Mono<Authentication> auth) {             ❶

    Mono<String> message =                   ❷
      auth.map(a -> "Hello " + a.getName());

    return message;
  }
}
```
❶ 프레임워크에 인증 대상 제공 요청

❷ 응답에서 교장 이름을 반환합니다.
응용 프로그램을 다시 실행하고 끝점을 호출하면 다음 코드 조각에 표시된 대로 동작이 관찰됩니다.

```sh
curl -u john:12345 http://localhost:8080/hello
```
The response body is
```
Hello john
```
그리고 이제 아마도 귀하의 질문은 인증 개체가 어디에서 왔습니까? 이것이 반응형 앱이기 때문에 프레임워크가 SecurityContext를 관리하도록 설계되었기 때문에 더 이상 ThreadLocal을 사용할 여유가 없습니다. 그러나 Spring Security는 ReactiveSecurityContextHolder 반응형 앱에 대한 컨텍스트 홀더의 다른 구현을 제공합니다. 이것을 사용하여 반응형 앱에서 SecurityContext와 함께 작업합니다. 따라서 여전히 SecurityContext가 있지만 이제는 다르게 관리됩니다. 그림 19.5는 ReactiveAuthenticationManager가 요청을 성공적으로 인증한 후 인증 프로세스의 끝을 설명합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH19_F05_Spilca.png)

그림 19.5 ReactiveAuthenticationManager가 요청을 성공적으로 인증하면 인증 개체를 필터에 반환합니다. 필터는 SecurityContext에 인증 인스턴스를 저장합니다.

Listing 19.4는 보안 컨텍스트에서 직접 인증 세부 정보를 얻으려는 경우 컨트롤러 클래스를 다시 작성하는 방법을 보여줍니다. 이 접근 방식은 프레임워크가 메서드의 매개변수를 통해 주입하도록 하는 대안입니다. 이 변경 사항은 프로젝트 ssia-ch19-ex2에서 구현되었습니다.

목록 19.4 ReactiveSecurityContextHolder로 작업하기
@RestController
public class HelloController {

    @GetMapping("/hello")
    public Mono<String> hello() {
      Mono<String> message =
        ReactiveSecurityContextHolder.getContext()    ❶

          .map(ctx -> ctx.getAuthentication())        ❷

          .map(auth -> "Hello " + auth.getName());    ❸

      return message;
    }
}
```
❶ ReactiveSecurityContextHolder에서 Mono<SecurityContext>

❷ SecurityContext를 Authentication 객체에 매핑

❸ 인증 개체를 반환된 메시지에 매핑합니다.

애플리케이션을 다시 실행하고 엔드포인트를 다시 테스트하면 이 섹션의 이전 예제와 동일하게 동작하는 것을 관찰할 수 있습니다. 다음은 명령입니다.

```sh
curl -u john:12345 http://localhost:8080/hello

```
The response body is
```
Hello john
```
이제 Spring Security가 반응 환경에서 SecurityContext를 적절하게 관리하기 위한 구현을 제공한다는 것을 알았으므로 이것이 앱이 권한 부여 규칙을 적용하는 방법이라는 것을 알게 되었습니다. 그리고 방금 배운 이러한 세부 정보는 19.3 섹션에서 논의할 권한 부여 규칙을 구성하는 방법을 제공합니다.

# 19.3 반응형 앱에서 권한 부여 규칙 구성

이 섹션에서는 권한 부여 규칙 구성에 대해 설명합니다. 이전 장에서 이미 알고 있듯이 권한 부여는 인증을 따릅니다. 19.1절과 19.2절에서 Spring Security가 반응형 앱에서 사용자와 SecurityContext를 관리하는 방법에 대해 논의했습니다. 그러나 앱이 인증을 완료하고 인증된 요청의 세부 정보를 SecurityContext에 저장하면 인증할 시간입니다.
다른 애플리케이션과 마찬가지로 반응형 앱도 개발할 때 권한 부여 규칙을 구성해야 합니다. 반응형 앱에서 권한 부여 규칙을 설정하는 방법을 가르치기 위해 먼저 섹션 19.3.1에서 엔드포인트 계층에서 구성을 만드는 방법을 설명합니다. 엔드포인트 계층에서 권한 부여 구성에 대한 논의를 마치면 19.3.2절에서 메서드 보안을 사용하여 애플리케이션의 다른 계층에 적용하는 방법을 배우게 됩니다.

### 19.3.1 반응형 앱의 엔드포인트 계층에서 승인 적용

이 섹션에서는 반응형 앱의 엔드포인트 계층에서 권한 부여를 구성하는 방법에 대해 설명합니다. 엔드포인트 계층에서 권한 부여 규칙을 설정하는 것은 웹 앱에서 권한 부여를 구성하기 위한 가장 일반적인 접근 방식입니다. 이 책의 이전 예제를 작업하는 동안 이미 이것을 발견했습니다. 엔드포인트 계층의 권한 부여 구성은 필수적입니다. 거의 모든 앱에서 이를 사용합니다. 따라서 반응형 앱에도 적용하는 방법을 알아야 합니다.

이전 장에서 WebSecurityConfigurerAdapter 클래스의 configure(HttpSecurity http) 메서드를 재정의하여 권한 부여 규칙을 설정하는 방법을 배웠습니다. 이 접근 방식은 반응형 앱에서는 작동하지 않습니다. 반응형 앱에 대해 엔드포인트 계층에 대한 권한 부여 규칙을 적절하게 구성하는 방법을 가르치기 위해 먼저 ssia-ch19-ex3이라는 새 프로젝트 작업을 시작합니다.

반응형 앱에서 Spring Security는 SecurityWebFilterChain이라는 계약을 사용하여 이전 장에서 설명한 대로 WebSecurityConfigurerAdapter 클래스의 configure() 메서드 중 하나를 재정의하여 수행했던 구성을 적용합니다. 반응형 앱을 사용하여 Spring 컨텍스트에 SecurityWebFilterChain 유형의 빈을 추가합니다. 이를 수행하는 방법을 가르치기 위해 독립적으로 보호하는 두 개의 엔드포인트가 있는 기본 애플리케이션을 구현해 보겠습니다. 새로 생성된 ssia-ch19-ex3 프로젝트의 pom.xml 파일에서 반응형 웹 앱과 Spring Security에 대한 종속성을 추가합니다.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```
권한 부여 규칙을 구성하는 두 끝점을 정의하는 컨트롤러 클래스를 만듭니다. 이러한 끝점은 /hello 및 /ciao 경로에서 액세스할 수 있습니다. /hello 엔드포인트를 호출하려면 사용자가 인증해야 하지만 인증 없이 /ciao 엔드포인트를 호출할 수 있습니다. 다음 목록은 컨트롤러의 정의를 나타냅니다.

Listing 19.5 보안을 위해 엔드포인트를 정의하는 HelloController 클래스

```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public Mono<String> hello(Mono<Authentication> auth) {
    Mono<String> message = auth.map(a -> "Hello " + a.getName());
    return message;
  }

  @GetMapping("/ciao")
  public Mono<String> ciao() {
    return Mono.just("Ciao!");
  }
}
```
구성 클래스에서 섹션 19.2에서 배운 것처럼 사용자를 정의하기 위해 ReactiveUserDetailsService 및 PasswordEncoder를 선언해야 합니다. 다음 목록은 이러한 선언을 정의합니다.

목록 19.6 사용자 관리를 위한 구성 요소를 선언하는 구성 클래스
```java
@Configuration
public class ProjectConfig {

  @Bean
  public ReactiveUserDetailsService userDetailsService() {
    var  u = User.withUsername("john")
            .password("12345")
            .authorities("read")
            .build();

    var uds = new MapReactiveUserDetailsService(u);

    return uds;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  // ...
}
```

목록 19.7에서 우리는 목록 19.6에서 선언한 것과 동일한 구성 클래스에서 작업하지만, 우리가 논의하는 인증 구성에 집중할 수 있도록 ReactiveUserDetailsService 및 PasswordEncoder의 선언을 생략합니다. 목록 19.7에서 SecurityWebFilterChain 유형의 빈을 Spring 컨텍스트에 추가한 것을 알 수 있습니다. 메소드는 Spring에 의해 주입되는 ServerHttpSecurity 유형의 객체를 매개변수로 받습니다. ServerHttpSecurity를 사용하면 SecurityWebFilterChain의 인스턴스를 구축할 수 있습니다. ServerHttpSecurity는 명령형 앱에 대한 권한 부여를 구성할 때 사용한 것과 유사한 구성 방법을 제공합니다.

목록 19.7 리액티브 앱에 대한 엔드포인트 승인 구성 
```java
@Configuration
public class ProjectConfig {

  // Omitted code

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(
    ServerHttpSecurity http) {
    
    return http.authorizeExchange() ❶
               .pathMatchers(HttpMethod.GET, "/hello")
                   .authenticated() ❷
               .anyExchange() ❸
                   .permitAll() ❹
               .and().httpBasic() ❺
               .and().build(); ❻
    }
}
```
❶ 엔드포인트 인증 설정 시작

❷ 승인 규칙을 적용할 요청을 선택합니다.

❸ 인증된 경우에만 액세스할 수 있도록 선택한 요청 구성

❹ 기타 요청사항 참조

❺ 인증 없이 요청을 호출할 수 있습니다.

❻ 반환할 SecurityWebFilterChain 객체를 빌드합니다.

AuthorizeExchange() 메서드를 사용하여 권한 부여 구성을 시작합니다. 명령형 앱에 대한 엔드포인트 권한 부여를 구성할 때 authorizeRequests() 메서드를 호출하는 방식과 유사하게 이 메서드를 호출합니다. 그런 다음 pathMatchers() 메서드를 사용하여 계속합니다. 이 방법을 명령형 앱에 대한 엔드포인트 권한 부여를 구성할 때 mvcMatchers()를 사용하는 것과 동일하다고 생각할 수 있습니다.

명령형 앱의 경우, 승인 규칙을 적용하는 요청을 그룹화하기 위해 매처 메소드를 사용하고 나면 승인 규칙이 무엇인지 지정합니다. 이 예에서는 인증된 요청만 수락된다는 인증된() 메서드를 호출했습니다. 명령형 앱에 대한 엔드포인트 권한 부여를 구성할 때도 authenticated()라는 메서드를 사용했습니다. 반응형 앱의 메서드 이름은 더 직관적으로 만들기 위해 동일하게 지정됩니다. 인증된() 메서드와 유사하게 다음 메서드를 호출할 수도 있습니다.

- permitAll()--인증 없이 요청을 허용하도록 앱을 구성합니다.

- denyAll() -- 모든 요청을 거부합니다.

- hasRole() 및 hasAnyRole() -- 역할을 기반으로 규칙 적용

- hasAuthority() 및 hasAnyAuthority() -- 권한을 기반으로 규칙 적용

뭔가 빠진 것 같죠? 명령형 앱에서 권한 부여 규칙을 구성할 때와 마찬가지로 access() 메서드도 있습니까? 예. 그러나 그것은 약간 다르기 때문에 우리는 그것을 증명하기 위해 별도의 예제를 연구할 것입니다. 이름 지정의 또 다른 유사점은 명령형 앱에서 이전에 anyRequest() 역할을 하는 anyExchange() 메서드입니다.

> **참고** 왜 이것을 anyExchange()라고 하며, 개발자들은 왜 anyRequest() 메소드에 대해 같은 이름을 유지하지 않았습니까? 왜 AuthorizeExchange()이고 왜 authorizeRequests()가 아닌가요? 이것은 단순히 반응 앱과 함께 사용되는 용어에서 비롯된 것입니다. 우리는 일반적으로 반응적인 방식으로 두 구성 요소 간의 통신을 데이터 교환이라고 합니다. 이는 하나의 요청에서 큰 묶음이 아닌 연속 스트림으로 분할되어 전송되는 데이터의 이미지를 강화합니다.

또한 다른 관련 구성과 마찬가지로 인증 방법을 지정해야 합니다. 동일한 ServerHttpSecurity 인스턴스로 동일한 이름의 메소드를 사용하고 명령형 앱에 대해 배운 것과 동일한 방식으로 이 작업을 수행합니다: httpBasic(), formLogin(), csrf(), cors(), 필터 추가 및 사용자 정의 필터 체인 등. 결국, 우리는 SecurityWebFilterChain의 인스턴스를 생성하기 위해 build() 메소드를 호출하고, 마침내 Spring 컨텍스트에 추가하기 위해 반환합니다.

이 섹션의 앞부분에서 명령형 앱의 경우와 마찬가지로 반응형 앱의 엔드포인트 권한 부여 구성에서 access() 메서드를 사용할 수도 있다고 말했습니다. 하지만 7장과 8장에서 non-reactive 앱의 설정에 대해 이야기할 때 말했듯이, access() 메서드는 설정을 적용할 수 없을 때만 사용하세요. access() 메서드는 뛰어난 유연성을 제공하지만 앱 구성을 읽기 더 어렵게 만듭니다. 항상 복잡한 솔루션보다 간단한 솔루션을 선호합니다. 그러나 이러한 유연성이 필요한 상황을 찾을 수 있습니다. 예를 들어 더 복잡한 권한 부여 규칙을 적용해야 하고 hasAuthority() 또는 hasRole() 및 관련 메서드를 사용하는 것만으로는 충분하지 않다고 가정합니다. 이러한 이유로 access() 메서드를 사용하는 방법도 알려 드리겠습니다. 

이 예제에서는 ssia-ch19-ex4라는 새 프로젝트를 만들었습니다. 목록 19.8에서 사용자에게 관리자 역할이 있는 경우에만 /hello 경로에 대한 액세스를 허용하도록 SecurityWebFilterChain 객체를 빌드한 방법을 볼 수 있습니다. 또한 정오 이전에만 액세스가 가능합니다. 다른 모든 엔드포인트에 대해서는 액세스를 완전히 제한합니다.

목록 19.8 구성 규칙을 구현할 때 access() 메서드 사용
```java
@Configuration
public class ProjectConfig {

  // Omitted code

  @Bean
  public SecurityWebFilterChain 
    securityWebFilterChain(ServerHttpSecurity http) {
    

    return http.authorizeExchange()
        .anyExchange() ❶
           .access(this::getAuthorizationDecisionMono)
        .and().httpBasic()
        .and().build();
   }

  private Mono<AuthorizationDecision> 
    getAuthorizationDecisionMono( ❷
            Mono<Authentication> a,
            AuthorizationContext c) {

    String path = getRequestPath(c); ❸

    boolean restrictedTime = LocalTime.now().isAfter(LocalTime.NOON); ❹

    if(path.equals("/hello")) { ❺
      return  a.map(isAdmin())
               .map(auth -> auth && !restrictedTime)
               .map(AuthorizationDecision::new);
    }
    return Mono.just(new AuthorizationDecision(false));
  }

  // Omitted code
}
```
❶ 모든 요청에 ​​대해 사용자 지정 권한 부여 규칙 적용

❷ 사용자 정의 권한 부여 규칙을 정의하는 메소드는 인증 및 요청 컨텍스트를 매개변수로 수신합니다.

❸ 컨텍스트에서 요청 경로를 얻습니다.

❹ 제한시간 정의

❺ /hello 경로의 경우 사용자 지정 권한 부여 규칙을 적용합니다.

어려워 보일 수 있지만 그렇게 복잡하지 않습니다. access() 메서드를 사용할 때 요청에 대해 가능한 모든 세부 정보를 수신하는 함수를 제공합니다. 이는 인증 개체와 AuthorizationContext입니다. 인증 개체를 사용하면 인증된 사용자의 세부 정보(사용자 이름, 역할 또는 권한 및 인증 논리를 구현하는 방법에 따라 다른 사용자 지정 세부 정보)가 있습니다. AuthorizationContext는 요청에 대한 정보(경로, 헤더, 쿼리 매개변수, 쿠키 등)를 제공합니다.

access() 메서드에 매개 변수로 제공하는 함수는 AuthorizationDecision 유형의 개체를 반환해야 합니다. 짐작했듯이 AuthorizationDecision은 요청이 허용되는지 여부를 앱에 알려주는 답변입니다. new AuthorizationDecision(true)으로 인스턴스를 생성하면 요청을 허용한다는 의미입니다. new AuthorizationDecision(false)로 생성하면 요청을 허용하지 않는다는 의미입니다.

목록 19.9에서 편의를 위해 목록 19.8에서 생략한 getRequestPath() 및 isAdmin()의 두 가지 메소드를 찾을 수 있습니다. 이를 생략하여 access() 메서드에서 사용하는 논리에 집중할 수 있습니다. 보시다시피 방법은 간단합니다. isAdmin() 메서드는 ROLE_ADMIN 속성이 있는 인증 인스턴스에 대해 true를 반환하는 함수를 반환합니다. getRequestPath() 메서드는 단순히 요청 경로를 반환합니다.

목록 19.9 getRequestPath() 및 isAdmin() 메소드의 정의
```java
@Configuration
public class ProjectConfig {

  // Omitted code

  private String getRequestPath(AuthorizationContext c) {
    return c.getExchange()
            .getRequest()
            .getPath()
            .toString();
  }

  private Function<Authentication, Boolean> isAdmin() {
    return p ->
      p.getAuthorities().stream()
       .anyMatch(e -> e.getAuthority().equals("ROLE_ADMIN"));
  }
}
```
애플리케이션을 실행하고 엔드포인트를 호출하면 우리가 적용한 인증 규칙 중 하나라도 충족되지 않은 경우 응답 상태 403 Forbidden이 발생하거나 단순히 HTTP 응답 본문에 메시지를 표시합니다.
```
curl -u john:12345 http://localhost:8080/hello
```
응답은
```
Hello john
```
이 섹션의 예에서 배후에서 무슨 일이 일어났습니까? 인증이 종료되면 다른 필터가 요청을 가로챕니다. AuthorizationWebFilter는 권한 부여 책임을 ReactiveAuthorizationManager에 위임합니다(그림 19.6).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH19_F06_Spilca.png)

그림 19.6 인증 프로세스가 성공적으로 종료되면 AuthorizationWebFilter라는 다른 필터가 요청을 가로챕니다. 이 필터는 권한 부여 책임을 ReactiveAuthorizationManager에 위임합니다.
기다리다! 이것은 ReactiveAuthorizationManager만 있다는 것을 의미합니까? 이 구성 요소는 우리가 만든 구성을 기반으로 요청을 승인하는 방법을 어떻게 압니까? 첫 번째 질문에 대해 아니오, 실제로 ReactiveAuthorizationManager의 여러 구현이 있습니다. 
AuthorizationWebFilter는 Spring 컨텍스트에 추가한 SecurityWebFilterChain 빈을 사용합니다. 이 빈을 사용하여 필터는 권한 부여 책임을 위임할 ReactiveAuthorizationManager 구현을 결정합니다(그림 19.7).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH19_F07_Spilca.png)

그림 19.7 AuthorizationFilter는 사용할 ReactiveAuthorizationManager를 알기 위해 컨텍스트에 추가한 SecurityWebFilterChain 빈(음영 처리)을 사용합니다.

### 19.3.2 반응형 앱에서 메서드 보안 사용

이 섹션에서는 반응형 앱의 모든 계층에 권한 부여 규칙을 적용하는 방법에 대해 설명합니다. 명령형 앱의 경우 전역 메서드 보안을 사용했으며 16장과 17장에서 메서드 수준에서 권한 부여 규칙을 적용하는 다양한 접근 방식을 배웠습니다. 엔드포인트 계층 이외의 계층에서 권한 부여 규칙을 적용할 수 있으므로 유연성이 뛰어나고 웹 응용 프로그램이 아닌 응용 프로그램에 권한 부여를 적용할 수 있습니다. 반응형 앱에 메서드 보안을 사용하는 방법을 가르치기 위해 ssia-ch19-ex5라는 별도의 예제를 작업합니다.

전역 메서드 보안 대신 명령형 앱으로 작업할 때 접근 방식 반응형 메서드 보안이라고 하며, 여기서 메서드 수준에서 직접 권한 부여 규칙을 적용합니다. 불행히도 반응적 메서드 보안은 아직 성숙한 구현이 아니며 @PreAuthorize 및 @PostAuthorize 주석만 사용할 수 있습니다. @PreFilter 및 @PostFilter 주석을 사용할 때 2018년에 Spring Security 팀에 문제가 추가되었지만 아직 구현되지 않았습니다. 

자세한 내용은
https://github.com/spring-projects/spring-security/issues/5249
이 예에서는 @PreAuthorize를 사용하여 사용자에게 테스트 끝점을 호출할 특정 역할이 있는지 확인합니다. 예제를 단순하게 유지하기 위해 끝점을 정의하는 메서드에 직접 @PreAuthorize 주석을 사용합니다. 하지만 명령형 앱에 대해 16장에서 논의한 것과 같은 방식으로 사용할 수 있습니다. 목록 19.10은 컨트롤러 클래스의 정의를 보여줍니다. 16장에서 배운 것과 유사하게 @PreAuthorize를 사용하는 것을 관찰하십시오. SpEL 표현식을 사용하여 우리는 관리자만 주석이 달린 메소드를 호출할 수 있다고 선언합니다.

목록 19.10 컨트롤러 클래스의 정의
```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  @PreAuthorize("hasRole('ADMIN')")     ❶
  public Mono<String> hello() {
    return Mono.just("Hello");
  }
}
```
❶ @PreAuthorize를 사용하여 메서드에 대한 액세스를 제한합니다.

여기에서 @EnableReactiveMethodSecurity 주석을 사용하여 반응적 메서드 보안 기능을 활성화하는 구성 클래스를 찾을 수 있습니다. 전역 메서드 보안과 유사하게 이를 활성화하려면 주석을 명시적으로 사용해야 합니다. 이 주석 외에도 구성 클래스에서 일반적인 사용자 관리 정의도 찾을 수 있습니다.

목록 19.11 구성 클래스
```java
@Configuration
@EnableReactiveMethodSecurity       ❶
public class ProjectConfig {

  @Bean
  public ReactiveUserDetailsService userDetailsService() {
    var  u1 = User.withUsername("john")
            .password("12345")
            .roles("ADMIN")
            .build();

    var  u2 = User.withUsername("bill")
            .password("12345")
            .roles("REGULAR_USER")
            .build();

    var uds = new MapReactiveUserDetailsService(u1, u2);

    return uds;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```
❶ 사후 대응 방식 보안 기능 활성화

이제 애플리케이션을 시작하고 각 사용자에 대해 엔드포인트를 호출하여 엔드포인트의 동작을 테스트할 수 있습니다. John을 관리자로 정의했기 때문에 John만 엔드포인트를 호출할 수 있음을 관찰해야 합니다. Bill은 일반 사용자이므로 Bill로 인증하는 엔드포인트를 호출하려고 하면 HTTP 403 Forbidden 상태의 응답이 반환됩니다. 사용자 John으로 인증하는 /hello 엔드포인트를 호출하는 것은 다음과 같습니다.

```sh
curl -u john:12345 http://localhost:8080/hello
``
응답은
```
Hello
```
Calling the /hello endpoint authenticating with user Bill looks like this:

```sh
curl -u bill:12345 http://localhost:8080/hello
```
응답은
```
Denied
```
이면에서 이 기능은 반응하지 않는 앱과 동일하게 작동합니다. 16장과 17장에서 aspect가 메서드 호출을 가로채고 권한 부여를 구현한다는 것을 배웠다. 호출이 지정된 사전 승인 규칙을 충족하지 않으면 aspect는 호출을 메서드에 위임하지 않습니다(그림 19.8).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH19_F08_Spilca.png)

그림 19.8 메서드 보안을 사용할 때 관점은 보호된 메서드에 대한 호출을 가로챕니다. 호출이 사전 승인 규칙을 충족하지 않으면 aspect는 호출을 메소드에 위임하지 않습니다.

## 19.4 반응형 앱과 OAuth 2

지금쯤이면 OAuth 2 프레임워크를 통해 설계된 시스템에서 반응형 애플리케이션을 사용할 수 있는지 궁금할 것입니다. 이 섹션에서는 리소스 서버를 반응형 앱으로 구현하는 방법에 대해 설명합니다. OAuth 2를 통해 구현된 인증 접근 방식에 의존하도록 반응 애플리케이션을 구성하는 방법을 배웁니다. 

오늘날 OAuth 2를 사용하는 것이 매우 일반적이기 때문에 리소스 서버 애플리케이션을 반응 서버로 설계해야 하는 요구 사항이 발생할 수 있습니다. ssia-ch19-ex6이라는 새 프로젝트를 만들었으며 반응형 리소스 서버 응용 프로그램을 구현할 것입니다. pom.xml에 종속성을 추가합니다.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```
애플리케이션을 테스트하려면 끝점이 필요하므로 컨트롤러 클래스를 추가합니다. 다음은 컨트롤러 클래스를 나타냅니다.

```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public Mono<String> hello() {
    return Mono.just("Hello!");
  }
}
```
이제 예제에서 가장 중요한 부분인 보안 구성입니다. 이 예에서는 토큰 서명 유효성 검사를 위해 권한 부여 서버에서 공개한 공개 키를 사용하도록 리소스 서버를 구성합니다. 이 접근 방식은 18장에서 Keycloak을 인증 서버로 사용했을 때와 동일합니다. 이 예에서는 실제로 동일한 구성된 서버를 사용했습니다. 동일한 작업을 선택하거나 13장에서 논의한 것처럼 사용자 지정 권한 부여 서버를 구현할 수 있습니다.

인증 방법을 구성하기 위해 섹션 19.3에서 배운 것처럼 SecurityWebFilterChain을 사용합니다. 그러나 httpBasic() 메서드를 사용하는 대신 oauth2ResourceServer() 메서드를 호출합니다. 그런 다음 jwt() 메서드를 호출하여 사용하는 토큰의 종류를 정의하고 Customizer 개체를 사용하여 토큰 서명의 유효성을 검사하는 방법을 지정합니다. 목록 19.12에서 구성 클래스의 정의를 찾을 수 있습니다.

목록 19.12 구성 클래스
```java
@Configuration
public class ProjectConfig {

  @Value("${jwk.endpoint}")
  private String jwkEndpoint;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(
    ServerHttpSecurity http) {
   
    return http.authorizeExchange()
                  .anyExchange().authenticated()
               .and().oauth2ResourceServer() ❶
                  .jwt(jwtSpec -> { ❷
                    jwtSpec.jwkSetUri(jwkEndpoint);
                  })
               .and().build();
    }
}

```
❶ 리소스 서버 인증 방식 설정

❷ 토큰의 유효성을 검사하는 방법을 지정합니다.

같은 방식으로 공개 키가 노출되는 URI를 지정하는 대신 공개 키를 구성할 수 있습니다. 유일한 변경 사항은 jwtSpec 인스턴스의 publicKey() 메서드를 호출하고 유효한 공개 키를 매개 변수로 제공하는 것입니다. 14장과 15장에서 논의한 접근 방식 중 하나를 사용할 수 있습니다. 여기에서 리소스 서버가 액세스 토큰의 유효성을 검사하는 방식을 자세히 분석했습니다.

다음으로 application.properties 파일을 변경하여 키 세트가 노출된 URI 값을 추가하고 서버 포트를 9090으로 변경합니다. 이렇게 하면 Keycloak이 8080에서 실행될 수 있습니다. 다음 코드 스니펫에서, application.properties 파일의 내용을 찾습니다. 

```yml
server.port=9090
jwk.endpoint=http://localhost:8080/auth/realms/master/protocol/openid-connect/certs
```

앱을 실행하고 우리가 원하는 예상 동작이 있음을 증명해 봅시다. 로컬에 설치된 Keycloak 서버를 사용하여 액세스 토큰을 생성합니다.

```
curl -XPOST 'http://localhost:8080/auth/realms/master/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username=bill' \
--data-urlencode 'password=12345' \
--data-urlencode 'client_id=fitnessapp' \
--data-urlencode 'scope=fitnessapp'
```
HTTP 응답 본문에서 다음과 같이 액세스 토큰을 받습니다.

```json
{
    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
    "expires_in": 6000,
    "refresh_expires_in": 1800,
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5c... ",
    "token_type": "bearer",
    "not-before-policy": 0,
    "session_state": "610f49d7-78d2-4532-8b13-285f64642caa",
    "scope": "fitnessapp"
}
```
액세스 토큰을 사용하여 다음과 같이 애플리케이션의 /hello 엔드포인트를 호출합니다.

```sh
curl -H 'Authorization: Bearer
eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJMSE9zT0VRSmJuTmJVbjhQb
VpYQTlUVW9QNTZoWU90YzNWT2swa1V2ajVVIn...' \
'http://localhost:9090/hello'
```
응답은
```
Hello!
```
## 요약

- 반응형 응용 프로그램은 데이터를 처리하고 다른 구성 요소와 메시지를 교환하는 스타일이 다릅니다. 반응형 앱은 처리 및 교환을 위해 데이터를 별도의 작은 세그먼트로 분할할 수 있는 경우와 같은 일부 상황에서 더 나은 선택일 수 있습니다.

- 다른 애플리케이션과 마찬가지로 보안 구성을 사용하여 반응형 앱도 보호해야 합니다. Spring Security는 반응형 앱과 명령형 앱에 보안 구성을 적용하는 데 사용할 수 있는 훌륭한 도구 세트를 제공합니다.

- Spring Security를 ​​사용하여 반응형 앱에서 사용자 관리를 구현하기 위해 ReactiveUserDetailsService 계약을 사용합니다. 이 구성 요소는 명령형 앱에 대한 UserDetailsService와 동일한 목적을 가지고 있습니다. 앱에 사용자 세부 정보를 가져오는 방법을 알려줍니다.

- 반응형 웹 애플리케이션에 대한 엔드포인트 권한 부여 규칙을 구현하려면 SecurityWebFilterChain 유형의 인스턴스를 생성하고 이를 Spring 컨텍스트에 추가해야 합니다. ServerHttpSecurity 빌더를 사용하여 SecurityWebFilterChain 인스턴스를 작성합니다.

- 일반적으로 권한 구성을 정의하는 데 사용하는 메서드의 이름은 명령형 앱에 사용하는 메서드와 동일합니다. 그러나 반응 용어와 관련된 사소한 명명 차이점을 찾을 수 있습니다. 예를 들어, AuthorizeRequests()를 사용하는 대신 반응형 앱에 대한 해당 이름은 authorizeExchange()입니다.

- Spring Security는 또한 Reactive 메서드 보안이라는 메서드 수준에서 권한 부여 규칙을 정의하는 방법을 제공하며 반응 앱의 모든 계층에서 권한 부여 규칙을 적용하는 데 큰 유연성을 제공합니다. 명령형 앱에 대한 전역 메서드 보안이라고 하는 것과 유사합니다.

- 그러나 반응형 메서드 보안은 명령형 앱에 대한 고발 메서드 보안만큼 성숙한 구현이 아닙니다. @PreAuthorize 및 @PostAuthorize 주석을 이미 사용할 수 있지만 @PreFilter 및 @PostFilter에 대한 기능은 여전히 ​​개발을 기다리고 있습니다.