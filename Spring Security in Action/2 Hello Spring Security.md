# 2 Hello Spring Security

이 장에서는 다음을 다룹니다.

- Spring Security로 첫 번째 프로젝트 생성
- 인증 및 권한 부여를 위한 기본 액터를 사용하여 간단한 기능 설계
- 이러한 행위자들이 서로 어떻게 관련되어 있는지 이해하기 위한 기본 계약 적용
- 주요 책임에 대한 구현 작성
- Spring Boot의 기본 구성 재정의

Spring Boot는 Spring Framework와 함께 애플리케이션 개발을 위한 진화 단계로 등장했습니다. 모든 구성을 작성할 필요 없이 Spring Boot는 미리 구성된 일부와 함께 제공되므로 구현과 일치하지 않는 구성만 재정의할 수 있습니다. 우리는 이 접근 방식을 구성을 초과하는 방식이라고 부르기도 합니다.

이러한 방식의 응용 프로그램 개발이 존재하기 전에 개발자는 만들어야 하는 모든 응용 프로그램에 대해 수십 줄의 코드를 계속해서 작성했습니다. 과거에는 대부분의 아키텍처를 모놀리식으로 개발했을 때 이러한 상황이 덜 눈에 띄었습니다. 모놀리식 아키텍처에서는 이러한 구성을 처음에 한 번만 작성하면 되었고 나중에는 거의 건드릴 필요가 없었습니다. 

SOA가 발전하면서 각 서비스를 구성하기 위해 작성해야 하는 상용구 코드의 고통을 느끼기 시작했습니다. 재미있다면 윌리 휠러(Willie Wheeler)와 조슈아 화이트(Manning, 2013)의 Spring in Practice에서 3장을 확인할 수 있습니다. 이전 책의 이 장에서는 Spring 3을 사용하여 웹 애플리케이션을 작성하는 방법에 대해 설명합니다. 이러한 방식으로 하나의 작은 한 페이지 웹 애플리케이션에 대해 얼마나 많은 구성을 작성해야 하는지 이해하게 될 것입니다. 다음은 해당 장의 링크입니다.

https://livebook.manning.com/book/spring-in-practice/chapter-3/

이러한 이유로 최근 앱, 특히 마이크로 서비스용 앱이 개발되면서 Spring Boot가 점점 더 대중화되었습니다. Spring Boot는 프로젝트에 대한 자동 구성을 제공하고 설정에 필요한 시간을 단축합니다. 오늘날의 소프트웨어 개발에 적합한 철학이 담겨 있다고 말하고 싶습니다.

이 장에서는 Spring Security를 ​​사용하는 첫 번째 애플리케이션으로 시작합니다. Spring Framework로 개발하는 앱의 경우 Spring Security는 애플리케이션 수준 보안을 구현하기 위한 탁월한 선택입니다. Spring Boot를 사용하고 자동 구성되는 기본값과 이러한 기본값을 재정의하는 방법에 대한 간략한 소개를 설명합니다. 기본 구성을 고려하면 인증 개념도 설명하는 Spring Security에 대한 훌륭한 소개를 제공합니다.

첫 번째 프로젝트를 시작하면 인증을 위한 다양한 옵션에 대해 더 자세히 설명하겠습니다. 3장에서 6장까지는 이 첫 번째 예에서 보게 될 다양한 책임 각각에 대한 보다 구체적인 구성을 계속할 것입니다. 또한 아키텍처 스타일에 따라 이러한 구성을 적용하는 다양한 방법을 볼 수 있습니다. 현재 장에서 접근할 단계는 다음과 같습니다.

1. Spring Security 및 웹 종속성만 있는 프로젝트를 생성하여 구성을 추가하지 않은 경우 어떻게 작동하는지 확인합니다. 이렇게 하면 인증 및 권한 부여를 위한 기본 구성에서 무엇을 기대해야 하는지 이해할 수 있습니다.

2. 사용자 지정 사용자 및 암호를 정의하는 기본값을 재정의하여 사용자 관리 기능을 추가하도록 프로젝트를 변경합니다.

3. 애플리케이션이 기본적으로 모든 엔드포인트를 인증하는 것을 관찰한 후, 이것도 사용자 정의할 수 있음을 배웁니다.

4. 모범 사례를 이해하기 위해 동일한 구성에 대해 다른 스타일을 적용합니다.

## 2.1 첫 번째 프로젝트 시작하기

첫 번째 프로젝트를 만들어 보겠습니다. 이 프로젝트는 REST 엔드포인트를 노출하는 작은 웹 애플리케이션입니다. Spring Security가 HTTP 기본 인증을 사용하여 이 엔드포인트를 보호하는 방법을 많이 하지 않고도 볼 수 있습니다. 프로젝트를 생성하고 올바른 종속성을 추가하기만 하면 Spring Boot는 애플리케이션을 시작할 때 사용자 이름과 비밀번호를 포함한 기본 구성을 적용합니다.

> 참고 Spring Boot 프로젝트를 생성하기 위한 다양한 대안이 있습니다. 일부 개발 환경에서는 프로젝트를 직접 생성할 수 있습니다. Spring Boot 프로젝트를 생성하는 데 도움이 필요한 경우 부록에 설명된 여러 방법을 찾을 수 있습니다. 더 자세한 내용은 Craig Walls의 Spring Boot in Action(Manning, 2016)을 추천합니다. Spring Boot in Action의 2장에서는 Spring Boot로 웹 앱을 만드는 방법을 정확하게 설명합니다(https://livebook.manning.com/book/spring-boot-in-action/chapter-2/).

이 책의 예제는 소스 코드를 참조합니다. 각 예에서 pom.xml 파일에 추가해야 하는 종속성도 지정합니다. https://www.manning.com/downloads/2105에서 책과 함께 제공되는 프로젝트와 사용 가능한 소스 코드를 다운로드할 수 있습니다. 문제가 발생하면 프로젝트가 도움이 될 것입니다. 또한 이를 사용하여 최종 솔루션을 검증할 수도 있습니다.

> 참고 이 책의 예제는 선택한 빌드 도구에 종속되지 않습니다. Maven 또는 Gradle을 사용할 수 있습니다. 그러나 일관성을 위해 모든 예제를 Maven으로 빌드했습니다.

첫 번째 프로젝트는 가장 작은 프로젝트이기도 합니다. 언급했듯이 이것은 그림 2.1에 설명된 대로 호출한 다음 응답을 수신할 수 있는 REST 끝점을 노출하는 간단한 응용 프로그램입니다.

이 프로젝트는 Spring Security로 애플리케이션을 개발할 때 첫 번째 단계를 배우기에 충분합니다. 인증 및 권한 부여를 위한 Spring Security 아키텍처의 기본 사항을 제공합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH02_F01_Spilca.png)

**그림 2.1** 첫 번째 애플리케이션은 HTTP Basic을 사용하여 엔드포인트에 대해 사용자를 인증하고 권한을 부여합니다. 애플리케이션은 정의된 경로(/hello)에서 REST 엔드포인트를 노출합니다. 성공적인 호출의 경우 응답은 HTTP 200 상태 메시지와 본문을 반환합니다. 이 예제는 기본적으로 Spring Security로 구성된 인증 및 권한 부여가 작동하는 방식을 보여줍니다.

빈 프로젝트를 만들고 이름을 ssia-ch2-ex1로 지정하여 Spring Security 학습을 시작합니다. (책과 함께 제공된 프로젝트에서 동일한 이름을 가진 이 예제도 찾을 수 있습니다.) 첫 번째 프로젝트에 대해 작성해야 하는 유일한 종속성은 spring-boot-starter-web 및 spring-boot-starter-security입니다. 목록 2.1에 나와 있습니다. 프로젝트를 생성한 후에는 이러한 종속성을 pom.xml 파일에 추가해야 합니다. 이 프로젝트에서 작업하는 주요 목적은 Spring Security로 구성된 기본 애플리케이션의 동작을 보는 것입니다. 또한 이 기본 구성의 일부인 구성 요소와 그 용도를 이해하고 싶습니다.

Listing 2.1 Spring Security dependencies for our first web app
```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

지금 바로 애플리케이션을 시작할 수 있습니다. Spring Boot는 프로젝트에 추가하는 종속성을 기반으로 Spring 컨텍스트의 기본 구성을 적용합니다. 그러나 보안된 엔드포인트가 하나 이상 없으면 보안에 대해 많은 것을 배울 수 없습니다. 간단한 끝점을 만들고 호출하여 무슨 일이 일어나는지 봅시다. 이를 위해 빈 프로젝트에 클래스를 추가하고 이 클래스의 이름을 `HelloController`로 지정합니다. 이를 위해 Spring Boot 프로젝트의 메인 네임스페이스 어딘가에 `controllers`라는 패키지에 클래스를 추가합니다.

> 참고 Spring Boot는 `@SpringBootApplication`으로 주석이 달린 클래스를 포함하는 패키지(및 해당 하위 패키지)의 구성 요소만 검색합니다. 기본 패키지 외부에 있는 Spring의 스테레오타입 구성 요소로 클래스에 주석을 추가하는 경우 `@ComponentScan` 주석을 사용하여 위치를 명시적으로 선언해야 합니다.

다음 목록에서 HelloController 클래스는 예제의 REST 컨트롤러와 REST 끝점을 정의합니다. 

Listing 2.2 The HelloController class and a REST endpoint
```
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
```
`@RestController` 주석은 컨텍스트에 빈을 등록하고 애플리케이션이 이 인스턴스를 웹 컨트롤러로 사용함을 Spring에 알립니다. 또한 주석은 애플리케이션이 HTTP 응답의 본문에서 반환된 값을 설정해야 함을 지정합니다. @GetMapping 주석은 /hello 경로를 구현된 메서드에 매핑합니다. 애플리케이션을 실행하면 콘솔의 다른 행 외에 다음과 유사한 내용이 표시되어야 합니다.

생성된 보안 암호 사용: `93a01cf0-794b-4b98-86ef-54860f36f7f3`

응용 프로그램을 실행할 때마다 새 암호가 생성되고 이전 코드 조각에 표시된 대로 콘솔에 이 암호가 인쇄됩니다. HTTP 기본 인증을 사용하여 애플리케이션의 끝점을 호출하려면 이 암호를 사용해야 합니다. 먼저 `Authorization` 헤더를 사용하지 않고 끝점을 호출해 보겠습니다. 
```
curl http://localhost:8080/hello
```
> 참고 이 책에서는 `cURL`을 사용하여 모든 예제에서 끝점을 호출합니다. `cURL`이 가장 읽기 쉬운 솔루션이라고 생각합니다. 그러나 원하는 경우 원하는 도구를 사용할 수 있습니다. 예를 들어, 보다 편안한 그래픽 인터페이스를 원할 수 있습니다. 이 경우 `Postman`이 탁월한 선택입니다. 사용하는 운영 체제에 이러한 도구가 설치되어 있지 않으면 직접 설치해야 합니다.
> 
And the response to the call:
```
{
  "status":401,
  "error":"Unauthorized",
  "message":"Unauthorized",
  "path":"/hello"
}
```

응답 상태는 `HTTP 401 Unauthorized`입니다. 인증에 적절한 자격 증명을 사용하지 않았기 때문에 이 결과를 예상했습니다. 기본적으로 Spring Security는 제공된 비밀번호(내 경우에는 `93a01`로 시작하는 비밀번호)가 있는 기본 `username(user)`을 예상합니다. 다시 시도하지만 이제 적절한 자격 증명을 사용하십시오.

```
curl -u user:93a01cf0-794b-4b98-86ef-54860f36f7f3 http://localhost:8080/hello
```
The response to the call now is
```
Hello!
```
> **참고** HTTP 401 Unauthorized 상태 코드는 약간 모호합니다. 일반적으로 권한 부여가 아닌 실패한 인증을 나타내는 데 사용됩니다. 개발자는 자격 증명이 누락되거나 잘못된 경우와 같은 경우 응용 프로그램 설계에 사용합니다. 실패한 승인의 경우 403 Forbidden 상태를 사용할 것입니다. 일반적으로 HTTP 403은 서버가 요청 호출자를 식별했지만 수행하려는 호출에 필요한 권한이 없음을 의미합니다.

올바른 자격 증명을 보내면 이전에 정의한 `HelloController` 메서드가 반환하는 내용을 응답 본문에서 정확하게 확인할 수 있습니다.

#### Calling the endpoint with HTTP Basic authentication

cURL을 사용하면 -u 플래그로 HTTP 기본 usernane과 password를 설정할 수 있습니다. 무대 뒤에서 cURL은 Base64에서 문자열 `<username>:<password>`를 인코딩하고 Basic 문자열이 접두사로 붙은 `Authorization` 헤더의 값으로 보냅니다. 그리고 `cURL을` 사용하면 `-u` 플래그를 사용하는 것이 더 쉬울 것입니다. 그러나 실제 요청이 어떻게 생겼는지 아는 것도 중요합니다. 따라서 시도해 보고 `Authorization` 헤더를 수동으로 생성해 보겠습니다.

첫 번째 단계에서 `<username>:<password>`문자열을 가져와 Base64로 인코딩합니다. 애플리케이션이 호출할 때 `Authorization` 헤더의 올바른 값을 구성하는 방법을 알아야 합니다. Linux 콘솔에서 Base64 도구를 사용하여 이 작업을 수행합니다. https://www.base64encode.org와 같이 Base64로 문자열을 인코딩하는 웹 페이지를 찾을 수도 있습니다. 이 스니펫은 Linux 또는 Git Bash 콘솔의 명령을 보여줍니다.

`echo -n user:93a01cf0-794b-4b98-86ef-54860f36f7f3 | base64`

Running this command returns this Base64-encoded string:

`dXNlcjo5M2EwMWNmMC03OTRiLTRiOTgtODZlZi01NDg2MGYzNmY3ZjM=`

You can now use the Base64-encoded value as the value of the Authorization header for the call. This call should generate the same result as the one using the -u option:
```
curl -H "Authorization: Basic dXNlcjo5M2EwMWNmMC03OTRiLTRiOTgtODZlZi01
➥ NDg2MGYzNmY3ZjM="        localhost:8080/hello
```
The result of the call is

`Hello!`

There’s no significant security configurations to discuss with a default project. We mainly use the default configurations to prove that the correct dependencies are in place. It does little for authentication and authorization. This implementation isn’t something we want to see in a production-ready application. But the default project is an excellent example that you can use for a start.

With this first example working, at least we know that Spring Security is in place. The next step is to change the configurations such that these apply to the requirements of our project. First, we’ll go deeper with what Spring Boot configures in terms of Spring Security, and then we see how we can override the configurations.

## 2.2 Which are the default configurations?

인증과 권한 부여 과정에 참여하는 전반적인 아키텍처에서의 주요 참가자를 다룬다. 그 이유는 미리 설정된 컴포넌트들을 여러분의 애플리케이션에 맞게 개정을 해줘야 하기 때문이다. 먼저 스프링 시큐리티의 인증과 권한 부여가 동작하는 방식을 설명하고 이 프로젝트에 적용한다. 이 모든 것을 한번에 다루기에는 너무 많으므로 각 컴포넌트에 대한 개략적인 설명을 할 것이다.

앞 절에서는 기본 사용자가 있으며 애플리케이션이 시작할 때 마다 랜덤으로 생성한 패스워드를 얻었다. 기본 사용자인 user와 생성된 암호로 엔드포인트를 호출한다. 하지만 이런 로직이 어디서 구현되었을까? 스프링 부트가 의존성을 보고 대신 이러한 컴포넌트를 설정한다.

아래 그림은 스프링 시큐리티 구조의 메인 컴포넌트들과 그들과의 관계를 보여준다. 이 컴포넌트들은 첫번째 프로젝트에서 미리 설정된 구현체를 가진다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH02_F02_Spilca.png)
그림 2.2 Spring Security의 인증 과정에서 작용하는 주요 구성 요소와 이들 간의 관계. 이 아키텍처는 Spring Security로 인증을 구현하는 백본을 나타냅니다. 인증 및 권한 부여에 대한 다양한 구현을 논의할 때 책 전체에서 이를 자주 참조합니다.

- 인증 필터: 인증 요청을 인증 매니저에게 위임하고 응답에 따라 시큐리티 context를 설정한다.
- 인증 매니저: 인증 제공자를 사용하여 인증을 처리.
- 인증 제공자: 인증 로직을 구현
- user details 서비스: 인증 제공자가 인증 로직에서 사용할 사용자 관리를 구현
- 패스워드 인코더: 인증 제공자가 인증 로직에서 사용할 암호 관리를 구현
- 시큐리티 컨텍스트: 인증 절차 후에 인증 데이터를 유지
In the following paragraphs, I’ll discuss these autoconfigured beans:
- UserDetailsService
- PasswordEncoder
- 
스프링 시큐리티와 `UserDetailsService` 계약을 구현하는 객체는 사용자 정보를 관리한다. 아직까지는 스프링 부트가 제공한 기본 구현체를 사용하였다. 이 구현체는 애플리케이션의 내부 메모리에 기본 자격 증명을 등록할 뿐이다. 이러한 기본 자격 증명은 user와 UUID 값인 패스워드이다. 이 패스워드는 랜덤으로 생성되어 컨솔에 표시해준다.  

이러한 기본 구현체는 단지 개념 증명으로서의 역할이며 의존성이 설치되었는지를 확인하는 것이다. 구현체는 메모리에 자격 증명을 저장할 뿐이다. 단지 예제일 뿐이므로 제품에는 적용해서는 안된다.

그런 다음 `PasswordEncoder`가 있습니다. `PasswordEncoder`는 두 가지 작업을 수행합니다.

- 암호를 인코딩
- 비밀번호가 기존 인코딩과 일치하는지 확인
- 
`UserDetailsService` 객체만큼 명확하지는 않지만 `PasswordEncoder`는 Basic 인증 흐름에서 필수이다. 가장 단순한 구현체는 패스워드를 암호화 없이 평문으로 관리한다. `UserDetailsService`의 기본 구현체를 바꿀 때 `PasswordEncoder`도 지정해야 한다.

또한 스프링 부트는 디폴트인 HTTP Basic 액세스 인증을 설정할 때 인증 메소드를 선택한다. 이것이 가장 단순한 액세스 인증 메소드이다. 기본 인증은 클라이언트가 HTTP 인증 헤더에 사용자 이름과 암호만 보내면 된다. 클라이언트는 헤더 값에는 Basic이 앞에 붙고 `username`과 `password`가 있는 문자열을 Base64 인코딩한 스트링이 따라온다.

> **참고** HTTP 기본 인증은 자격 증명의 기밀성을 제공하지 않습니다. Base64는 전송의 편의를 위한 인코딩 방법일 뿐입니다. 암호화나 해싱 방법이 아닙니다. 전송 중에 가로채면 누구나 자격 증명을 볼 수 있습니다. 일반적으로 기밀 유지를 위해 최소한 HTTPS가 없으면 HTTP 기본 인증을 사용하지 않습니다. RFC 7617(https://tools.ietf.org/html/rfc7617)에서 HTTP Basic에 대한 자세한 정의를 읽을 수 있습니다.
> 
`AuthenticationProvider`는 인증 로직을 정의하고 user와 password 관리를 위임한다. `AuthenticationProvider`의 기본 구현체는 `UserDetailsService`와 `PasswordEncoder`를 위해 제공된 기본 구현체를 사용한다. 기본적으로 여러분의 애플리케이션은 모든 엔드포인트를 보호한다. 따라서 우리의 예제에서 할 일은 엔드포인트를 추가하는 것 뿐이다. 또한 모든 엔드포인트에 접근할 사용자가 1명이므로 권한 부여에 대해 할일이 많지 않다. 

#### HTTP vs. HTTPS

예제에서는 HTTP만을 사용하지만 실제 환경에서는 HTTPS로 통신한다. HTTP 또는 HTTPS를 사용하든 스프링 시큐리티에 관련된 설정이 다르지 않다. 예제에서는 엔드포인트에 대한 HTTPS 설정을 하지 않으나 원한다면 이 사이드바에 있는 엔트포인트에 대해 HTTPS를 활성화할 수 있다.

시스템에서 HTTPS를 구성하는 몇 가지 패턴이 있습니다. 경우에 따라 개발자는 애플리케이션 수준에서 HTTPS를 구성합니다. 다른 곳에서는 서비스 메시를 사용하거나 인프라 수준에서 HTTPS를 설정하도록 선택할 수 있습니다. Spring Boot를 사용하면 이 사이드바의 다음 예제에서 배우게 될 것처럼 애플리케이션 수준에서 HTTPS를 쉽게 활성화할 수 있습니다.

이러한 구성 시나리오에서는 CA(인증 기관)에서 서명한 인증서가 필요합니다. 이 인증서를 사용하여 끝점을 호출하는 클라이언트는 응답이 인증 서버에서 오는지 그리고 아무도 통신을 가로채지 않았는지 여부를 압니다. 이러한 인증서를 구입할 수 있지만 갱신해야 합니다. 애플리케이션을 테스트하기 위해 HTTPS만 구성해야 하는 경우 `OpenSSL`과 같은 도구를 사용하여 자체 서명된 인증서를 생성할 수 있습니다. 자체 서명된 인증서를 생성한 다음 프로젝트에서 구성해 보겠습니다. 

```
openssl req -newkey rsa:2048 -x509 -keyout key.pem -out cert.pem -days 365
```
`openssl` 명령을 실행하면 암호와 CA에 대한 세부 정보를 묻는 메시지가 표시됩니다. 테스트를 위한 자체 서명된 인증서일 뿐이므로 모든 데이터를 입력할 수 있습니다. 비밀번호만 기억하면 됩니다. 이 명령은 `key.pem`(개인 키) 및 `cert.pem`(공개 인증서)의 두 파일을 출력합니다. 이 파일을 사용하여 HTTPS를 활성화하기 위한 자체 서명된 인증서를 생성합니다. 대부분의 경우 인증서는 PKCS12(공개 키 암호화 표준 #12)입니다. 덜 자주, 우리는 `JKS(Java KeyStore)` 형식을 사용합니다. `PKCS12` 형식으로 예제를 계속하겠습니다. 암호화에 대한 훌륭한 토론을 위해 David Wong의 Real-World Cryptography(Manning, 2020)를 추천합니다.

```
openssl pkcs12 -export -in cert.pem -inkey key.pem -out certificate.p12 -name "certificate"
```
우리가 사용하는 두 번째 명령은 첫 번째 명령에 의해 생성된 두 개의 파일을 입력으로 받아서 자체 서명된 인증서를 출력합니다. Windows 시스템의 Bash 셸에서 이러한 명령을 실행하는 경우 다음 코드 스니펫에 표시된 것처럼 앞에 `winpty`를 추가해야 할 수 있습니다.

```
winpty openssl req -newkey rsa:2048 -x509 -keyout key.pem -out cert.pem -days 365
winpty openssl pkcs12 -export -in cert.pem -inkey key.pem -out certificate.p12 -name "certificate"
```
마지막으로, 자체 서명된 인증서가 있으면 엔드포인트에 대해 HTTPS를 구성할 수 있습니다. `Certificate.p12` 파일을 Spring Boot 프로젝트의 리소스 폴더에 복사하고 `application.properties` 파일에 다음 줄을 추가합니다.

```yaml
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:certificate.p12
server.ssl.key-store-password=12345  ❶
```

❶ 비밀번호의 값은 `PKCS12` 인증서 파일을 생성하기 위한 두 번째 명령을 실행할 때 지정한 값입니다.

인증서 생성 명령을 실행한 후 프롬프트에서 비밀번호(제 경우 "12345")를 요청했습니다. 이것이 명령에 표시되지 않는 이유입니다. 이제 애플리케이션에 테스트 엔드포인트를 추가한 다음 HTTPS를 사용하여 호출해 보겠습니다.

```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello!";
    }
}
```

자체 서명된 인증서를 사용하는 경우 인증서의 신뢰성 테스트를 건너뛰도록 끝점을 호출하는 데 사용하는 도구를 구성해야 합니다. 도구가 인증서의 진위를 테스트하는 경우 인증서가 진위임을 인식하지 못하고 호출이 작동하지 않습니다. cURL에 `-k` 옵션을 사용하여 인증서의 신뢰성 테스트를 건너뛸 수 있습니다.

```bsh
curl -k https://localhost:8080/hello!
```

The response to the call is `Hello!`

HTTPS를 사용하더라도 시스템 구성 요소 간의 통신은 방탄이 아님을 기억하십시오. 여러 번 사람들이 "이제 암호화하지 않고 HTTPS를 사용합니다!"라고 말하는 것을 들었습니다. HTTPS는 통신을 보호하는 데 도움이 되지만 시스템 보안 벽의 벽돌 중 하나일 뿐입니다. 항상 책임을 가지고 시스템 보안을 처리하고 관련된 모든 계층을 관리하십시오.

## 2.3 Overriding default configurations

첫 번째 프로젝트의 기본값을 어떻게 바꿀 수 있는지 알아볼 차례입니다. 기본 구성 요소를 재정의하기 위한 옵션을 이해해야 합니다. 이것이 사용자 정의 구현을 연결하고 애플리케이션에 맞게 보안을 적용하는 방식이기 때문입니다. 그리고 개발 프로세스는 애플리케이션을 유지 관리하기 쉽게 유지하기 위해 구성을 작성하는 방법에 관한 것이기도 합니다. 우리가 작업할 프로젝트에서 구성을 재정의하는 여러 가지 방법을 종종 찾을 수 있습니다. 

이러한 유연성은 혼란을 야기할 수 있습니다. 동일한 애플리케이션에서 Spring Security의 다른 부분을 구성하는 다양한 스타일이 혼합된 것을 자주 볼 수 있는데 이는 바람직하지 않습니다. 따라서 이러한 유연성에는 주의가 필요합니다. 이들 중에서 선택하는 방법을 배워야 하므로 이 섹션은 또한 귀하의 옵션이 무엇인지 아는 것에 관한 것입니다.

어떤 경우에는 개발자가 구성을 위해 Spring 컨텍스트에서 빈을 사용하도록 선택합니다. 다른 경우에는 동일한 목적으로 다양한 메서드를 재정의합니다. Spring 생태계가 진화한 속도는 아마도 이러한 다중 접근 방식을 생성한 주요 요인 중 하나일 것입니다. 여러 스타일을 혼합하여 프로젝트를 구성하는 것은 코드를 이해하기 어렵게 만들고 애플리케이션의 유지 관리 가능성에 영향을 미치므로 바람직하지 않습니다. 옵션과 사용 방법을 아는 것은 귀중한 기술이며 프로젝트에서 애플리케이션 수준 보안을 구성하는 방법을 더 잘 이해하는 데 도움이 됩니다. 

이 섹션에서는 `UserDetailsService` 및 `PasswordEncoder`를 구성합니다. 이 요소는 인증에 참여하며 대부분의 응용 프로그램은 요구 사항에 따라 구성 요소를 사용자 지정합니다. 3장과 4장에서 사용자 지정에 대한 세부 정보를 논의할 것이지만 사용자 지정 구현을 연결하는 방법을 확인하는 것이 중요합니다. 이 장에서 사용하는 구현은 모두 Spring Security에서 제공합니다.

### 2.3.1 UserDetailsService 재정의

이 섹션에서는 `UserDetailsService` 유형의 사용자 정의 빈을 정의하는 방법을 배웁니다. Spring Security에서 제공하는 기본 설정을 재정의하기 위해 이 작업을 수행합니다. 3장에서 더 자세히 살펴보겠지만, 자신만의 구현을 생성하거나 Spring Security에서 제공하는 미리 정의된 구현을 사용하는 옵션이 있습니다. 이 장에서 우리는 Spring Security에서 제공하는 구현을 자세히 설명하거나 아직 우리 자신의 구현을 만들지 않을 것입니다. `InMemoryUserDetailsManager`라는 Spring Security에서 제공하는 구현을 사용하겠습니다. 이 예제를 통해 이러한 종류의 객체를 아키텍처에 연결하는 방법을 배우게 됩니다.

주 Java의 인터페이스는 객체 간의 계약을 정의합니다. 응용 프로그램의 클래스 디자인에서 인터페이스를 사용하여 서로를 사용하는 개체를 분리합니다. 이 책에서 설명할 때 이 인터페이스 특성을 적용하기 위해 주로 계약이라고 합니다.
우리가 선택한 구현으로 이 구성 요소를 재정의하는 방법을 보여주기 위해 첫 번째 예제에서 수행한 작업을 변경합니다. 그렇게 하면 인증을 위한 자체 관리 자격 증명을 가질 수 있습니다. 이 예제에서는 클래스를 구현하지 않지만 Spring Security에서 제공하는 구현을 사용합니다.

이 예에서는 `InMemoryUserDetailsManager` 구현을 사용합니다. 이 구현이 단순한 `UserDetailsService` 이상의 것이더라도 지금은 `UserDetailsService`의 관점에서만 참조합니다. 이 구현은 자격 증명을 메모리에 저장하고 Spring Security에서 요청을 인증하는 데 사용할 수 있습니다.

> 참고 `InMemoryUserDetailsManager` 구현은 예제 또는 개념 증명을 위한 훌륭한 도구입니다. 우리의 경우 기본 `UserDetailsService` 구현을 재정의하는 방법을 이해하는 데 사용합니다.

일반적으로 `config`라는 별도의 패키지에 구성 클래스를 선언합니다. 목록 2.3은 구성 클래스에 대한 정의를 보여줍니다. 프로젝트 `ssia-ch2-ex2`에서도 예제를 찾을 수 있습니다.

> **참고** 이 책의 예제는 Java 11용으로 설계되었습니다.

Listing 2.3 The configuration class for the UserDetailsService bean
```java
@Configuration                                      ❶
public class ProjectConfig {
  @Bean                                             ❷
  public UserDetailsService userDetailsService() {
    var userDetailsService = new InMemoryUserDetailsManager();      
    return userDetailsService;
  }
}
```
❶ The `@Configuration` annotation marks the class as a configuration class.

❷ The `@Bean` annotation instructs Spring to add the returned value as a bean in the Spring context.

`@Configuration`으로 클래스에 주석을 답니다. `@Bean` 주석은 Spring 컨텍스트에 메소드에 의해 반환된 인스턴스를 추가하도록 Spring에 지시합니다. 코드를 있는 그대로 실행하면 콘솔에 자동 생성된 비밀번호가 더 이상 표시되지 않습니다. 이제 애플리케이션은 기본 구성된 인스턴스 대신 컨텍스트에 추가한 `UserDetailsService` 유형의 인스턴스를 사용합니다. 그러나 동시에 다음 두 가지 이유로 끝점에 더 이상 액세스할 수 없습니다.

- You don’t have any users.
- You don’t have a PasswordEncoder.
  
그림 2.2에서 인증도 `PasswordEncoder`에 의존함을 알 수 있습니다. 이 두 가지 문제를 단계별로 해결해 보겠습니다. 

1. 자격 증명(사용자 이름 및 암호) 집합이 있는 사용자를 한 명 이상 만듭니다.
2. `UserDetailsService` 구현으로 관리할 사용자 추가
3. 애플리케이션이 `UserDetailsService`에 의해 관리되는 비밀번호로 주어진 비밀번호를 확인하는 데 사용할 수 있는 `PasswordEncoder` 유형의 빈을 정의합니다.

먼저 `InMemoryUserDetailsManager`의 인스턴스에 대한 인증에 사용할 수 있는 자격 증명 집합을 선언하고 추가합니다. 3장에서는 사용자와 사용자를 관리하는 방법에 대해 자세히 설명합니다. 지금은 미리 정의된 빌더를 사용하여 `UserDetails` 유형의 개체를 생성해 보겠습니다.

인스턴스를 빌드할 때 사용자 이름, 암호 및 하나 이상의 권한을 제공해야 합니다. 권한은 해당 사용자에게 허용되는 작업이며 이를 위해 모든 문자열을 사용할 수 있습니다. Listing 2.4에서 권한을 read라고 명명했지만 이 이름은 별로 중요하지 않다. 

Listing 2.4 Creating a user with the User builder class for `UserDetailsService`
```java
@Configuration
public class ProjectConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    var userDetailsService = new InMemoryUserDetailsManager();

    var user = User.withUsername("john")      ❶
            .password("12345")                ❶
            .authorities("read")              ❶
            .build();                         ❶
        
    userDetailsService.createUser(user);      ❷

    return userDetailsService;
  }
}
```
❶ 주어진 사용자 이름, 암호 및 권한 목록으로 사용자를 구성합니다.

❷ UserDetailsService에서 관리할 사용자 추가

> **참고** `org.springframework.security.core.userdetails` 패키지에서 `User` 클래스를 찾을 수 있습니다. 사용자를 나타내는 객체를 생성하는 데 사용하는 빌더 구현입니다. 또한 이 책에서는 일반적으로 코드 목록에 클래스를 작성하는 방법을 제시하지 않으면 Spring Security에서 제공한다는 의미입니다.

목록 2.4에 나와 있는 것처럼 사용자 이름에 하나, 암호에 하나, 권한에 하나 이상을 제공해야 합니다. 그러나 이것은 여전히 ​​끝점을 호출하기에 충분하지 않습니다. 또한 `PasswordEncoder`를 선언해야 합니다.

기본 `UserDetailsService`를 사용하는 경우 `PasswordEncoder`도 자동 구성됩니다. `UserDetailsService`를 재정의했기 때문에 `PasswordEncoder`도 선언해야 합니다. 지금 예제를 시도하면 끝점을 호출할 때 예외가 표시됩니다. 인증을 시도할 때 Spring Security는 비밀번호를 관리하는 방법을 알지 못함을 깨닫고 실패합니다. 예외는 다음 코드 스니펫과 같으며 애플리케이션 콘솔에서 볼 수 있어야 합니다. 클라이언트는 `HTTP 401 Unauthorized` 메시지와 빈 응답 본문을 반환합니다.

```bsh
curl -u john:12345 http://localhost:8080/hello
```

The result of the call in the app’s console is

```bsh
java.lang.IllegalArgumentException: There is no PasswordEncoder mapped for
➥ the id "null"
    at org.springframework.security.crypto.password
     ➥ .DelegatingPasswordEncoder$UnmappedIdPasswordEncoder
     ➥ .matches(DelegatingPasswordEncoder.java:244)
     ➥ ~[spring-security-core-5.1.6.RELEASE.jar:5.1.6.RELEASE] 
```

이 문제를 해결하기 위해 `UserDetailsService`와 마찬가지로 컨텍스트에 `PasswordEncoder` 빈을 추가할 수 있습니다. 이 빈의 경우 `PasswordEncoder`의 기존 구현을 사용합니다.

```java
@Bean
public PasswordEncoder passwordEncoder() {
  return NoOpPasswordEncoder.getInstance();
}
```

> **NOTE** `NoOpPasswordEncoder` 인스턴스는 암호를 일반 텍스트로 처리합니다. 암호화하거나 해시하지 않습니다. 일치를 위해 `NoOpPasswordEncoder`는 String 클래스의 기본 `equals(Object o)` 메서드를 사용하여 문자열만 비교합니다. 프로덕션 준비 앱에서 이러한 유형의 PasswordEncoder를 사용하면 안 됩니다. `NoOpPasswordEncoder`는 암호의 해싱 알고리즘에 집중하고 싶지 않은 예에 좋은 옵션입니다. 따라서 클래스 개발자는 이를 `@Deprecated`로 표시하고 개발 환경은 이름을 취소선으로 표시합니다.

다음 목록에서 구성 클래스의 전체 코드를 볼 수 있습니다.

Listing 2.5 The full definition of the configuration class
```java
@Configuration
public class ProjectConfig {
  @Bean
  public UserDetailsService userDetailsService() {
    var userDetailsService = new InMemoryUserDetailsManager();
    var user = User.withUsername("john")
            .password("12345")
            .authorities("read")
            .build();

    userDetailsService.createUser(user);
    return userDetailsService;
  }

  @Bean                                         ❶
  public PasswordEncoder passwordEncoder() {    ❶
    return NoOpPasswordEncoder.getInstance();   ❶
  }                                             ❶
}
```
❶ A new method annotated with `@Bean` to add a `PasswordEncoder` to the context

Let’s try the endpoint with the new user `John` and the password `12345`:

```bsh
curl -u john:12345 http://localhost:8080/hello
Hello!
```

> **참고** 단위 및 통합 테스트의 중요성을 알고 있는 일부 사용자는 이미 우리가 예제에 대한 테스트를 작성하지 않는 이유에 대해 궁금해할 것입니다. 실제로 이 책에서 제공하는 모든 예제와 관련된 Spring Security 통합 테스트를 찾을 수 있습니다. 그러나 각 장에 대해 제시된 주제에 집중할 수 있도록 Spring Security 통합 테스트에 대한 논의를 분리하고 20장에서 자세히 설명합니다.

### 2.3.2 엔드포인트 승인 구성 재정의

2.3.1에서 사용자를 위한 새로운 관리가 이루어지면 이제 끝점에 대한 인증 방법 및 구성에 대해 논의할 수 있습니다. 7, 8, 9장에서 권한 부여 구성과 관련하여 많은 것을 배우게 될 것입니다. 그러나 세부 사항으로 들어가기 전에 큰 그림을 이해해야 합니다. 이를 달성하는 가장 좋은 방법은 첫 번째 예를 사용하는 것입니다. 기본 구성에서 모든 엔드포인트는 애플리케이션에서 관리하는 유효한 사용자가 있다고 가정합니다. 또한 기본적으로 앱은 인증 방법으로 HTTP 기본 인증을 사용하지만 이 구성을 쉽게 재정의할 수 있습니다.

HTTP 기본 인증은 대부분의 애플리케이션 아키텍처에 적합하지 않습니다. 때때로 우리는 우리의 응용 프로그램과 일치하도록 변경하고 싶습니다. 마찬가지로 애플리케이션의 모든 엔드포인트에 보안이 필요한 것은 아니며 보안이 필요한 엔드포인트에 대해 다른 권한 부여 규칙을 선택해야 할 수도 있습니다. 이러한 변경을 수행하려면 `WebSecurityConfigurerAdapter` 클래스를 확장하는 것으로 시작합니다. 이 클래스를 확장하면 다음 목록에 표시된 대로 `configure(HttpSecurity http)` 메서드를 재정의할 수 있습니다. 이 예에서는 ssia-ch2-ex2 프로젝트에서 코드를 계속 작성하겠습니다.

Listing 2.6 Extending WebSecurityConfigurerAdapter

```java
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // ... 
  }
}
```
We can then alter the configuration using different methods of the `HttpSecurity` object as shown in the next listing.

Listing 2.7 Using the HttpSecurity parameter to alter the configuration
```java
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {
  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests()                 ❶
          .anyRequest().authenticated();     ❶
  }
}
```
❶ All the requests require authentication.

목록 2.7의 코드는 기본 동작과 동일한 동작으로 끝점 권한 부여를 구성합니다. 엔드포인트를 다시 호출하여 섹션 2.3.1의 이전 테스트와 동일하게 동작하는지 확인할 수 있습니다. 약간만 변경하면 자격 증명 없이 모든 엔드포인트에 액세스할 수 있습니다. 다음 목록에서 이 작업을 수행하는 방법을 볼 수 있습니다.

Listing 2.8 Using `permitAll()` to change the authorization configuration
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests()              ❶
           .anyRequest().permitAll();     ❶
  }
}
```
❶ None of the requests need to be authenticated.

Now, we can call the /hello endpoint without the need for credentials. The permitAll() call in the configuration, together with the anyRequest() method, makes all the endpoints accessible without the need for credentials:
```bsh
curl http://localhost:8080/hello
```

And the response body of the call is

`Hello!`

The purpose of this example is to give you a feeling for how to override default configurations. We’ll get into the details about authorization in chapters 7 and 8.

Spring Security로 구성을 생성할 때 혼란스러운 측면 중 하나는 동일한 것을 구성하는 여러 가지 방법이 있다는 것입니다. 이 섹션에서는 `UserDetailsService` 및 `PasswordEncoder를` 구성하기 위한 대안을 배웁니다. 이 책이나 블로그 및 기사와 같은 다른 출처에서 찾은 예에서 이러한 옵션을 인식할 수 있도록 옵션을 아는 것이 중요합니다. 또한 애플리케이션에서 이를 언제 어떻게 사용해야 하는지 이해하는 것도 중요합니다. 다음 장에서 이 섹션의 정보를 확장하는 다양한 예를 볼 수 있습니다.

첫 번째 프로젝트를 진행해 보겠습니다. 기본 애플리케이션을 만든 후 Spring 컨텍스트에서 새 구현을 빈으로 추가하여 `UserDetailsService` 및 `PasswordEncoder`를 재정의할 수 있었습니다. `UserDetailsService` 및 `PasswordEncoder`에 대해 동일한 구성을 수행하는 다른 방법을 찾아보겠습니다.

구성 클래스에서 이 두 객체를 빈으로 정의하는 대신 `configure(AuthenticationManagerBuilder auth)` 메서드를 통해 설정합니다. `WebSecurityConfigurerAdapter` 클래스에서 이 메서드를 재정의하고 `AuthenticationManagerBuilder` 유형의 매개 변수를 사용하여 다음 목록과 같이 `UserDetailsService`와 `PasswordEncoder`를 모두 설정합니다. 이 예제는 ssia-ch2-ex3 프로젝트에서 찾을 수 있습니다.

Listing 2.9 Setting UserDetailsService and PasswordEncoder in configure()
```java
@Configuration
public class ProjectConfig 
   extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(AuthenticationManagerBuilder auth) 
         throws  Exception {
    var userDetailsService = new InMemoryUserDetailsManager(); ❶

    var user = User.withUsername("john") ❷
                .password("12345") ❷
                .authorities("read") ❷
                .build(); ❷

    userDetailsService.createUser(user); ❸

    auth.userDetailsService(userDetailsService) ❹
        .passwordEncoder(NoOpPasswordEncoder.getInstance());
  }
}
```
❶ Declares a UserDetailsSevice to store the users in memory

❷ Defines a user with all its details

❸ Adds the user to be managed by our UserDetailsSevice

❹ The UserDetailsService and PasswordEncoder are now set up within the configure() method.

In listing 2.9, you can observe that we declar the UserDetailsService in the same way as in listing 2.5. The difference is that now this is done locally inside the second overridden method. We also call the userDetailsService() method from the AuthenticationManagerBuilder to register the UserDetailsService instance. Furthermore, we call the passwordEncoder() method to register the PasswordEncoder. Listing 2.10 shows the full contents of the configuration class.
NOTE The WebSecurityConfigurerAdapter class contains three different overloaded configure() methods. In listing 2.9, we overrode a different one than in listing 2.8. In the next chapters, we’ll discuss all three in more detail.

Listing 2.10 Full definition of the configuration class
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure
  ➥ (AuthenticationManagerBuilder auth) throws Exception {
    var userDetailsService =new InMemoryUserDetailsManager(); ❶

    var user = User.withUsername("john")           ❷
            .password("12345")                     ❷
            .authorities("read")                   ❷
            .build();                              ❷

        userDetailsService.createUser(user);       ❸

    auth.userDetailsService(userDetailsService) 
         .passwordEncoder(NoOpPasswordEncoder.getInstance());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests()                       ❺
          .anyRequest().authenticated();           ❺
  }
}
```
❶ Creates an instance of `InMemoryUserDetailsManager()`

❷ Creates a new user

❸ Adds the user to be managed by our `UserDetailsService`

❹ Configures `UserDetailsService` and `PasswordEncoder`

❺ Specifies that all the requests require authentication

이러한 구성 옵션은 모두 정확합니다. 컨텍스트에 빈을 추가하는 첫 번째 옵션을 사용하면 잠재적으로 필요할 수 있는 다른 클래스에 값을 주입할 수 있습니다. 그러나 귀하의 경우에 필요하지 않은 경우 두 번째 옵션도 똑같이 좋습니다. 그러나 혼동을 일으킬 수 있으므로 구성을 혼합하지 않는 것이 좋습니다. 예를 들어 다음 목록의 코드는 UserDetailsService와 PasswordEncoder 간의 링크가 어디에 있는지 궁금하게 만들 수 있습니다.

Listing 2.11 Mixing configuration styles
```java
@Configuration
public class Config extends WebSecurityConfigurerAdapter {

  @Bean
  public PasswordEncoder passwordEncoder() {        ❶
    return NoOpPasswordEncoder.getInstance();
  }

  @Override
  protected void configure
  ➥ (AuthenticationManagerBuilder auth) throws Exception {
    var userDetailsService = new InMemoryUserDetailsManager();

    var user = User.withUsername("john")
                .password("12345")
                .authorities("read")
                .build();

    userDetailsService.createUser(user);

    auth.userDetailsService(userDetailsService);    ❷
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests()
          .anyRequest().authenticated();
  }
}
```
❶ Designs the PasswordEncoder as a bean

❷ Configures the UserDetailsService directly in the configure() method

기능적으로 목록 2.11의 코드는 잘 작동하지만 코드를 깨끗하고 이해하기 쉽게 유지하려면 두 가지 접근 방식을 혼합하지 않는 것이 좋습니다. AuthenticationManagerBuilder를 사용하여 인증을 위해 사용자를 직접 구성할 수 있습니다. 이 경우 UserDetailsService를 생성합니다. 그러나 구문은 훨씬 더 복잡해지고 읽기 어려운 것으로 간주될 수 있습니다. 프로덕션 준비 시스템에서도 이 선택을 한 번 이상 보았습니다.

메모리 내 접근 방식을 사용하여 사용자를 구성하기 때문에 이 예가 괜찮아 보일 수 있습니다. 그러나 프로덕션 애플리케이션에서는 그렇지 않습니다. 거기에 사용자를 데이터베이스에 저장하거나 다른 시스템에서 액세스할 수 있습니다. 이 경우 구성이 상당히 길고 보기 흉해질 수 있습니다. Listing 2.12는 메모리 내 사용자를 위한 구성을 작성하는 방법을 보여줍니다. 이 예제는 `ssia-ch2-ex4` 프로젝트에 적용되었습니다.

Listing 2.12 Configuring in-memory user management
```java
@Override
protected void configure
➥ (AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
        .withUser("john")
        .password("12345")
        .authorities("read")
    .and()
        .passwordEncoder(NoOpPasswordEncoder.getInstance());
}
```
일반적으로 이 접근 방식은 권장하지 않습니다. 애플리케이션에서 책임을 최대한 분리하여 작성하는 것이 더 낫다고 생각하기 때문입니다.

### 2.3.4 인증 제공자 구현 재정의

이미 관찰한 것처럼 Spring Security 구성 요소는 많은 유연성을 제공하여 애플리케이션 아키텍처에 적용할 때 많은 옵션을 제공합니다. 지금까지 Spring Security 아키텍처에서 `UserDetailsService` 및 `PasswordEncoder`의 목적을 배웠습니다. 그리고 그것들을 구성하는 몇 가지 방법을 보았습니다. 이제 이들에게 위임하는 구성 요소인 `AuthenticationProvider`를 사용자 정의할 수도 있다는 것을 배울 시간입니다.

그림 2.3은 인증 논리를 구현하고 사용자 및 암호 관리를 위해 `UserDetailsService` 및 `PasswordEncoder`에 위임하는 `AuthenticationProvider`를 보여줍니다. 따라서 이 섹션에서는 인증 및 권한 부여 아키텍처에서 한 단계 더 깊이 들어가 `AuthenticationProvider`를 사용하여 사용자 지정 인증 논리를 구현하는 방법을 학습한다고 말할 수 있습니다.

이것은 첫 번째 예이므로 아키텍처의 구성 요소 간의 관계를 더 잘 이해할 수 있도록 간단한 그림만 보여줍니다. 그러나 우리는 3, 4, 5장에서 더 자세히 설명할 것입니다. 이 장에서 구현된 `AuthenticationProvider`와 더 중요한 연습인 책의 첫 번째 "실습" 섹션인 6장을 찾을 수 있습니다. .
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH02_F03_Spilca.png)
Figure 2.3 `AuthenticationProvider`는 인증 로직을 구현합니다. `AuthenticationManager`로부터 요청을 받고 `UserDetailsService`에 사용자 찾기를 위임하고 `PasswordEncoder`에 암호 확인을 위임합니다.

Spring Security 아키텍처에서 설계된 책임을 존중할 것을 권장합니다. 이 아키텍처는 세분화된 책임과 느슨하게 결합되어 있습니다. 이 디자인은 Spring Security를 유연하고 애플리케이션에 쉽게 통합할 수 있도록 하는 것 중 하나입니다. 하지만 유연성을 어떻게 활용하느냐에 따라 디자인도 바뀔 수 있다. 이러한 접근 방식은 솔루션을 복잡하게 만들 수 있으므로 주의해야 합니다. 예를 들어 더 이상 UserDetailsService 또는 PasswordEncoder가 필요하지 않은 방식으로 기본 AuthenticationProvider를 재정의하도록 선택할 수 있습니다. 이를 염두에 두고 다음 목록은 사용자 지정 인증 공급자를 만드는 방법을 보여줍니다. 이 예제는 ssia-ch2-ex5 프로젝트에서 찾을 수 있습니다.

Listing 2.13 Implementing the AuthenticationProvider interface
```java
@Component
public class CustomAuthenticationProvider 
➥ implements AuthenticationProvider {

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // authentication logic here
  }

  @Override
  public boolean supports(Class<?> authenticationType) {
    // type of the Authentication implementation here
  }
}
```

`authenticate(Authentication authentication)` 메소드는 인증을 위한 모든 논리를 나타내므로 목록 2.14에서 이와 같은 구현을 추가할 것입니다. `support()` 메서드의 사용법은 5장에서 자세히 설명하겠습니다. 당분간은 구현을 당연하게 여길 것을 권장합니다. 현재 예제에서는 필수가 아닙니다.

Listing 2.14 Implementing the authentication logic
```java
@Override
public Authentication authenticate(Authentication authentication) 
  throws AuthenticationException {

  String username = authentication.getName(); ❶
  String password = String.valueOf(authentication.getCredentials());

  if ("john".equals(username) && "12345".equals(password)) { ❷
      
    return new UsernamePasswordAuthenticationToken(username, password, Arrays.asList());
  } else {
    throw new AuthenticationCredentialsNotFoundException("Error in authentication!");
  }
}
```
❶ getName() 메서드는 Principal 인터페이스에서 Authentication에 의해 상속됩니다.

❷ 이 조건은 일반적으로 UserDetailsService 및 PasswordEncoder를 호출하여 사용자 이름과 암호를 테스트합니다.

보시다시피 여기서 if-else 절의 조건은 UserDetailsService 및 PasswordEncoder의 책임을 대체하는 것입니다. 두 개의 빈을 사용할 필요는 없지만 인증을 위해 사용자와 암호로 작업하는 경우 관리 논리를 분리하는 것이 좋습니다. 인증 구현을 재정의하는 경우에도 Spring Security 아키텍처가 설계한 대로 적용하십시오.

고유한 AuthenticationProvider를 구현하여 인증 논리를 대체하는 것이 유용할 수 있습니다. 기본 구현이 애플리케이션의 요구 사항에 완전히 맞지 않는 경우 사용자 지정 인증 논리를 구현하도록 결정할 수 있습니다. 전체 AuthenticationProvider 구현은 다음 목록에 있는 것과 같습니다.

Listing 2.15 The full implementation of the authentication provider
```java
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

  @Override
  public Authentication authenticate(Authentication authentication) 
      throws AuthenticationException {
      
      String username = authentication.getName();
      String password = String.valueOf(authentication.getCredentials());

      if ("john".equals(username) && "12345".equals(password)) {
        return new UsernamePasswordAuthenticationToken(username, password, Arrays.asList());
      } else {
        throw new AuthenticationCredentialsNotFoundException("Error!");
      }
    }

  @Override
  public boolean supports(Class<?> authenticationType) {
    return UsernamePasswordAuthenticationToken.class
                  .isAssignableFrom(authenticationType);
  }
}
```

In the configuration class, you can register the AuthenticationProvider in the configure(AuthenticationManagerBuilder auth) method shown in the following listing.

Listing 2.16 Registering the new implementation of AuthenticationProvider
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private CustomAuthenticationProvider authenticationProvider;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(authenticationProvider);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests().anyRequest().authenticated();
  }
}
```

You can now call the endpoint, which is accessible by the only user recognized, as defined by the authentication logic--John, with the password 12345:

```sh
curl -u john:12345 http://localhost:8080/hello
```
The response body is
```
`Hello!`
```
In chapter 5, you’ll learn more details about the AuthenticationProvider and how to override its behavior in the authentication process. In that chapter, we’ll also discuss the Authentication interface and its implementations, such as the UserPasswordAuthenticationToken.

### 2.3.5 USING MULTIPLE CONFIGURATION CLASSES IN YOUR PROJECT

이전에 구현된 예제에서는 구성 클래스만 사용했습니다. 그러나 구성 클래스에 대해서도 책임을 분리하는 것이 좋습니다. 구성이 더 복잡해지기 시작하기 때문에 이 분리가 필요합니다. 프로덕션 준비 응용 프로그램에서는 첫 번째 예제보다 더 많은 선언이 있을 수 있습니다. 프로젝트를 읽을 수 있도록 하기 위해 둘 이상의 구성 클래스를 갖는 것이 유용할 수도 있습니다.

각 책임당 하나의 클래스만 갖는 것은 항상 좋은 습관입니다. 이 예에서는 사용자 관리 구성을 권한 구성과 분리할 수 있습니다. `UserManagementConfig`(목록 2.17에 정의) 및 `WebAuthorizationConfig`(목록 2.18에 정의)의 두 가지 구성 클래스를 정의하여 이를 수행합니다. 이 예제는 `ssia-ch2-ex6` 프로젝트에서 찾을 수 있습니다.

Listing 2.17 Defining the configuration class for user and password management

```java
@Configuration
public class UserManagementConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    var userDetailsService = new InMemoryUserDetailsManager();

    var user = User.withUsername("john")
            .password("12345")
            .authorities("read")
            .build();

    userDetailsService.createUser(user);
    return userDetailsService;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```

이 경우 UserManagementConfig 클래스에는 사용자 관리를 담당하는 두 개의 빈(UserDetailsService 및 PasswordEncoder)만 포함됩니다. 이 클래스는 WebSecurityConfigurerAdapter를 확장할 수 없기 때문에 두 개체를 bean으로 구성합니다. 다음 목록은 이 정의를 보여줍니다.

Listing 2.18 권한 관리를 위한 구성 클래스 정의하기
```java
@Configuration
public class WebAuthorizationConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests().anyRequest().authenticated();
  }
}
```

여기서 `WebAuthorizationConfig` 클래스는 `WebSecurityConfigurerAdapter`를 확장하고 `configure(HttpSecurity http)` 메소드를 재정의해야 합니다.

> **참고** 이 경우 WebSecurityConfigurerAdapter를 확장하는 두 클래스를 모두 가질 수는 없습니다. 그렇게 하면 종속성 주입이 실패합니다. @Order 주석을 사용하여 주입 우선 순위를 설정하여 종속성 주입을 해결할 수 있습니다. 그러나 기능적으로는 구성이 병합하는 대신 서로를 제외하므로 작동하지 않습니다.

## Summary

- Spring Boot는 애플리케이션의 종속성에 Spring Security를 ​​추가할 때 몇 가지 기본 구성을 제공합니다.

- 인증 및 권한 부여를 위한 기본 구성 요소인 UserDetailsService, PasswordEncoder 및 AuthenticationProvider를 구현합니다.

- User 클래스로 사용자를 정의할 수 있습니다. 사용자는 최소한 사용자 이름, 암호 및 권한이 있어야 합니다. 권한은 사용자가 애플리케이션 컨텍스트에서 수행할 수 있도록 허용하는 작업입니다.

- Spring Security가 제공하는 UserDetailsService의 간단한 구현은 InMemoryUserDetailsManager입니다. 이러한 UserDetailsService 인스턴스에 사용자를 추가하여 애플리케이션의 메모리에서 사용자를 관리할 수 있습니다.

- NoOpPasswordEncoder는 암호를 일반 텍스트로 사용하는 PasswordEncoder 계약의 구현입니다. 이 구현은 학습 예제 및 (아마도) 개념 증명에 적합하지만 프로덕션 준비 응용 프로그램에는 적합하지 않습니다.

- AuthenticationProvider 계약을 사용하여 애플리케이션에서 사용자 정의 인증 로직을 구현할 수 있습니다.

- 구성을 작성하는 방법은 여러 가지가 있지만 단일 애플리케이션에서는 한 가지 방법을 선택하고 고수해야 합니다. 이것은 코드를 더 깔끔하고 이해하기 쉽게 만드는 데 도움이 됩니다.
