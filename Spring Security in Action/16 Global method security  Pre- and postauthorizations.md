# 16 글로벌 메소드 보안: 사전 및 사후 승인

이 장에서는 다음을 다룹니다.

- Spring 애플리케이션의 전역 메소드 보안
- 권한, 역할, 권한에 따른 메소드 사전 승인
- 권한, 역할, 권한에 따른 메소드 사후 승인

지금까지 인증을 구성하는 다양한 방법에 대해 논의했습니다. 2장에서 가장 간단한 접근 방식인 HTTP Basic으로 시작하여 5장에서 form 로그인을 설정하는 방법을 보여 드렸습니다. 12장에서 15장까지 OAuth 2를 다루었습니다. 그러나 권한 부여 측면에서는 구성에 대해서만 논의했습니다. 끝점 수준. 앱이 웹 애플리케이션이 아니라고 가정합니다. 

인증 및 권한 부여에도 Spring Security를 ​​사용할 수 없습니까? Spring Security는 앱이 HTTP 엔드포인트를 통해 사용되지 않는 시나리오에 적합합니다. 이 장에서는 메서드 수준에서 권한 부여를 구성하는 방법을 배웁니다. 이 접근 방식을 사용하여 웹 및 웹이 아닌 응용 프로그램 모두에서 권한 부여를 구성하고 전역 메서드 보안이라고 부를 것입니다(그림 16.1).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH16_F01_Spilca.png)

그림 16.1 전역 메서드 보안을 사용하면 애플리케이션의 모든 계층에서 권한 부여 규칙을 적용할 수 있습니다. 이 접근 방식을 사용하면 더 세분화되고 특별히 선택한 수준에서 권한 부여 규칙을 적용할 수 있습니다.

웹이 아닌 애플리케이션의 경우 전역 메서드 보안은 엔드포인트가 없는 경우에도 권한 부여 규칙을 구현할 수 있는 기회를 제공합니다. 웹 애플리케이션에서 이 접근 방식은 엔드포인트 수준뿐만 아니라 앱의 여러 계층에 권한 부여 규칙을 적용할 수 있는 유연성을 제공합니다. 이 장을 자세히 살펴보고 전역 메서드 보안을 사용하여 메서드 수준에서 권한 부여를 적용하는 방법을 알아보겠습니다.

## 16.1 전역 메서드 보안 활성화

메서드 수준에서 권한 부여를 활성화하는 방법과 다양한 권한 부여 규칙을 적용하기 위해 Spring Security가 제공하는 다양한 옵션을 배웁니다. 이 접근 방식은 승인을 적용할 때 더 큰 유연성을 제공합니다. 단순히 엔드포인트 수준에서 권한 부여를 구성할 수 없는 상황을 해결할 수 있는 필수 기술입니다.

기본적으로 전역 메서드 보안은 비활성화되어 있으므로 이 기능을 사용하려면 먼저 활성화해야 합니다. 또한 전역 메서드 보안은 권한 부여를 적용하기 위한 여러 접근 방식을 제공합니다. 우리는 이러한 접근 방식에 대해 논의한 다음 이 장의 다음 섹션과 17장의 예제에서 구현합니다. 간단히 말해서 전역 메서드 보안으로 두 가지 주요 작업을 수행할 수 있습니다.

- 호출 권한 부여--구현된 일부 권한 규칙에 따라 누군가가 메서드를 호출할 수 있는지(사전 권한 부여) 또는 누군가 메서드가 실행된 후 메서드가 반환하는 것에 액세스할 수 있는지 여부(사후 권한 부여)를 결정합니다.

- 필터링 - 메서드가 매개변수를 통해 수신할 수 있는 항목(사전 필터링)과 메서드가 실행된 후 호출자가 메서드에서 다시 수신할 수 있는 항목(사후 필터링)을 결정합니다. 필터링에 대해서는 17장에서 논의하고 구현할 것입니다.

### 16.1.1 호출 승인 이해

전역 메서드 보안과 함께 사용하는 권한 부여 규칙을 구성하기 위한 방식 중 하나는 호출 권한 부여입니다. 호출 권한 부여 방식은 메서드를 호출할 수 있는지 여부를 결정하거나 메서드가 호출되도록 한 다음 호출자가 메서드에서 반환된 값에 액세스할 수 있는지 여부를 결정하는 권한 부여 규칙을 적용하는 것을 말합니다. 종종 우리는 제공된 매개변수 또는 그 결과에 따라 누군가가 논리 조각에 액세스할 수 있는지 여부를 결정해야 합니다. 호출 승인에 대해 논의한 다음 몇 가지 예에 적용해 보겠습니다.

전역 메서드 보안은 어떻게 작동합니까? 승인 규칙을 적용하는 메커니즘은 무엇입니까? 애플리케이션에서 전역 메서드 보안을 활성화하면 실제로 Spring 측면을 활성화합니다. 이 측면은 권한 부여 규칙을 적용하는 메서드에 대한 호출을 가로채고 이러한 권한 부여 규칙을 기반으로 가로채는 메서드로 호출을 전달할지 여부를 결정합니다(그림 16.2).

Spring 프레임워크의 많은 구현은 AOP(Aspect Oriented Programming)에 의존합니다. 전역 메서드 보안은 측면에 의존하는 Spring 애플리케이션의 많은 구성 요소 중 하나일 뿐입니다. aspect와 AOP에 대한 복습이 필요하다면 Clarence Ho et al.(Apress, 2017)의 Pro Spring 5: An In-Depth Guide to Spring Framework and Its Tools의 5장을 읽는 것이 좋습니다. 간단히 말해서 호출 승인을 다음과 같이 분류합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH16_F02_Spilca.png)

**그림 16.2** 전역 메서드 보안을 활성화하면 Aspect가 보호된 메서드에 대한 호출을 가로챕니다. 주어진 권한 부여 규칙이 존중되지 않으면 aspect는 보호된 메소드에 호출을 위임하지 않습니다.

- 사전 승인--프레임워크는 메소드 호출 전에 승인 규칙을 확인합니다.

- 사후 권한 부여--프레임워크는 메서드가 실행된 후 권한 부여 규칙을 확인합니다.
두 가지 접근 방식을 모두 취하고 세부적으로 설명하고 몇 가지 예를 들어 구현해 보겠습니다.

> method에 대한 액세스를 보호하기 위해 사전 승인 사용

특정 사용자에 대한 호출자 문서로 반환하는 findDocumentsByUser(String username) 메서드가 있다고 가정해 보겠습니다. 호출자는 메서드의 매개변수를 통해 메서드가 문서를 검색하는 사용자 이름을 제공합니다. 인증된 사용자가 자신의 문서만 얻을 수 있도록 해야 한다고 가정합니다. 인증된 사용자의 사용자 이름을 매개 변수로 받는 메서드 호출만 허용되도록 이 메서드에 규칙을 적용할 수 있습니까? 예! 이것은 사전 승인으로 우리가 하는 일입니다.

특정 상황에서 누군가가 메서드를 호출하는 것을 완전히 금지하는 권한 부여 규칙을 적용할 때 이를 사전 권한 부여라고 합니다(그림 16.3). 이 접근 방식은 프레임워크가 메서드를 실행하기 전에 권한 부여 조건을 확인한다는 것을 의미합니다. 호출자에게 우리가 정의한 권한 부여 규칙에 따른 권한이 없으면 프레임워크는 호출을 메서드에 위임하지 않습니다. 대신 프레임워크에서 예외가 발생합니다. 이것은 지금까지 전역 메서드 보안에 가장 자주 사용되는 접근 방식입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH16_F03_Spilca.png)

그림 16.3 사전 승인을 사용하면 메소드 호출을 더 위임하기 전에 승인 규칙이 검증됩니다. 프레임워크는 권한 부여 규칙이 준수되지 않으면 호출을 위임하지 않고 대신 메서드 호출자에게 예외를 throw합니다.
일반적으로 일부 조건이 충족되지 않으면 기능이 전혀 실행되는 것을 원하지 않습니다. 인증된 사용자를 기준으로 조건을 적용할 수 있으며, 해당 매개변수를 통해 받은 메소드의 값을 참조할 수도 있습니다.

> 메서드 호출을 보호하기 위해 사후 승인 사용

누군가가 메서드를 호출하도록 허용하지만 반드시 메서드에서 반환된 결과를 얻을 필요는 없는 권한 부여 규칙을 적용할 때 사후 권한 부여를 사용하고 있습니다(그림 16.4). 사후 인증을 사용하면 Spring Security는 메소드가 실행된 후 인증 규칙을 확인합니다. 이러한 종류의 권한 부여를 사용하여 특정 조건에서 메서드 반환에 대한 액세스를 제한할 수 있습니다. 사후 승인은 메소드 실행 후에 발생하기 때문에 메소드에서 반환된 결과에 승인 규칙을 적용할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH16_F04_Spilca.png)

그림 16.4 사후 승인을 통해 aspect는 호출을 보호된 메서드에 위임합니다. 보호된 메서드가 실행을 완료한 후 aspect는 권한 부여 규칙을 확인합니다. 규칙이 준수되지 않으면 호출자에게 결과를 반환하는 대신 측면에서 예외가 발생합니다.

일반적으로 사후 승인을 사용하여 실행 후 메서드가 반환하는 내용을 기반으로 권한 부여 규칙을 적용합니다. 그러나 사후 승인에 주의하십시오! 메소드가 실행 중에 무언가를 변경하면 결국 권한 부여가 성공하는지 여부에 관계없이 변경이 발생합니다.

> **참고** @Transactional 주석을 사용하더라도 사후 승인에 실패하면 변경 사항이 롤백되지 않습니다. 사후 승인 기능에서 발생하는 예외는 트랜잭션 관리자가 트랜잭션을 커밋한 후에 발생합니다.

### 16.1.2 프로젝트에서 글로벌 메서드 보안 활성화

이 섹션에서는 글로벌 메서드 보안에서 제공하는 사전 승인 및 사후 승인 기능을 적용하는 프로젝트를 진행합니다. 전역 메서드 보안은 Spring Security 프로젝트에서 기본적으로 활성화되어 있지 않습니다. 사용하려면 먼저 활성화해야 합니다. 그러나 이 기능을 활성화하는 것은 간단합니다. 구성 클래스에서 @EnableGlobalMethodSecurity 주석을 사용하면 됩니다.

이 예를 위해 ssia-ch16-ex1이라는 새 프로젝트를 만들었습니다. 이 프로젝트의 경우 목록 16.1에 표시된 대로 ProjectConfig 구성 클래스를 작성했습니다. 구성 클래스에서 @EnableGobalMethodSecurity 주석을 추가합니다. 전역 메서드 보안은 이 장에서 논의하는 권한 부여 규칙을 정의하는 세 가지 접근 방식을 제공합니다.

- 사전/사후 승인 주석
- JSR 250 주석, @RolesAllowed
- @Secured 주석

거의 모든 경우에 사전/사후 승인 주석이 사용되는 유일한 접근 방식이기 때문에 이 장에서 이 접근 방식에 대해 설명합니다. 이 접근 방식을 활성화하기 위해 @EnableGlobalMethodSecurity 주석의 prePostEnabled 속성을 사용합니다. 이 장의 끝에서 이전에 언급한 다른 두 가지 옵션에 대한 간략한 개요를 제시합니다.

목록 16.1 전역 메서드 보안 활성화
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ProjectConfig {
}
```
HTTP 기본 인증에서 OAuth 2에 이르기까지 모든 인증 방식에서 전역 메서드 보안을 사용할 수 있습니다. 간단하게 유지하고 새로운 세부 정보에 집중할 수 있도록 HTTP 기본 인증을 통해 전역 메서드 보안을 제공합니다. 이러한 이유로 이 장의 프로젝트에 대한 pom.xml 파일은 다음 코드 조각이 제시하는 것처럼 웹 및 Spring Security 종속성만 필요합니다.

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## 16.2 권한 및 역할에 대한 사전 승인 적용

사전 승인의 예를 구현합니다. 이 예에서는 섹션 16.1에서 시작된 프로젝트 ssia-ch16-ex1을 계속 진행합니다. 섹션 16.1에서 논의했듯이 사전 승인은 특정 메소드를 호출하기 전에 Spring Security가 적용하는 승인 규칙을 정의하는 것을 의미합니다. 규칙이 준수되지 않으면 프레임워크는 메서드를 호출하지 않습니다.

이 섹션에서 구현하는 애플리케이션에는 간단한 시나리오가 있습니다. "Hello" 뒤에 이름이 오는 문자열을 반환하는 끝점인 /hello를 노출합니다. 이름을 얻기 위해 컨트롤러는 서비스 메서드를 호출합니다(그림 16.5). 이 방법은 사전 승인 규칙을 적용하여 사용자에게 쓰기 권한이 있는지 확인합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH16_F05_Spilca.png)

**그림 16.5** NameService의 getName() 메서드를 호출하려면 인증된 사용자에게 쓰기 권한이 있어야 합니다. 사용자에게 이 권한이 없으면 프레임워크에서 호출을 허용하지 않고 예외를 throw합니다.

인증할 사용자가 있는지 확인하기 위해 UserDetailsService 및 PasswordEncoder를 추가했습니다. 우리 솔루션을 검증하려면 쓰기 권한이 있는 사용자와 쓰기 권한이 없는 사용자의 두 명의 사용자가 필요합니다. 첫 번째 사용자가 성공적으로 엔드포인트를 호출할 수 있음을 증명하고 두 번째 사용자의 경우 메서드를 호출하려고 할 때 앱에서 권한 부여 예외를 throw합니다. 다음 목록은 UserDetailsService 및 PasswordEncoder를 정의하는 구성 클래스의 전체 정의를 보여줍니다.

```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true) ❶
public class ProjectConfig {

  @Bean ❷
  public UserDetailsService userDetailsService() {
    var service = new InMemoryUserDetailsManager();

    var u1 = User.withUsername("natalie")    
              .password("12345")
              .authorities("read")
              .build();

    var u2 = User.withUsername("emma")
              .password("12345")
              .authorities("write")
              .build();

    service.createUser(u1);
    service.createUser(u2);

    return service;
  }

  @Bean ❸
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```
❶ 사전/사후 인증을 위한 글로벌 메소드 보안 가능

❷ 테스트를 위해 두 명의 사용자가 있는 Spring 컨텍스트에 UserDetailsService 추가

❸ Spring 컨텍스트에 PasswordEncoder 추가

이 메서드에 대한 권한 부여 규칙을 정의하기 위해 @PreAuthorize 주석을 사용합니다. @PreAuthorize 주석은 권한 부여 규칙을 설명하는 SpEL(Spring Expression Language) 표현식을 값으로 받습니다. 이 예에서는 간단한 규칙을 적용합니다.

hasAuthority() 메서드를 사용하여 권한을 기반으로 사용자에 대한 제한을 정의할 수 있습니다. 7장에서 hasAuthority() 메서드에 대해 배웠습니다. 여기서 끝점 수준에서 권한 부여를 적용하는 방법에 대해 논의했습니다. 다음 목록은 이름 값을 제공하는 서비스 클래스를 정의합니다.

**목록 16.3** 서비스 클래스는 메소드에 대한 사전 승인 규칙을 정의합니다.
```java
@Service
public class NameService {

  @PreAuthorize("hasAuthority('write')")     ❶
  public String getName() {
    return "Fantastico";
  }
}
```
❶ 권한 부여 규칙을 정의합니다. 쓰기 권한이 있는 사용자만 메서드를 호출할 수 있습니다.

다음 목록에서 컨트롤러 클래스를 정의합니다. NameService를 종속성으로 사용합니다.

목록 16.4 끝점을 구현하고 서비스를 사용하는 컨트롤러 클래스
```java
@RestController
public class HelloController {

  @Autowired                                    ❶
  private NameService nameService;

  @GetMapping("/hello")
  public String hello() {
    return "Hello, " + nameService.getName();   ❷
  }
}
```
❶ 컨텍스트에서 서비스 주입

❷ 사전 승인 규칙을 적용하는 메서드를 호출합니다.

이제 애플리케이션을 시작하고 동작을 테스트할 수 있습니다. 쓰기 권한이 있기 때문에 사용자 Emma만 엔드포인트를 호출할 권한이 있을 것으로 예상합니다. 다음 코드는 Emma와 Natalie의 두 사용자와 함께 엔드포인트에 대한 호출을 보여줍니다. /hello 끝점을 호출하고 사용자 Emma로 인증하려면 다음 cURL 명령을 사용합니다.

```sh
curl -u emma:12345 http://localhost:8080/hello
```

응답은
```
Hello, Fantastico
```
/hello 엔드포인트를 호출하고 사용자 Natalie로 인증하려면 다음 cURL 명령을 사용하십시오.

```sh
curl -u natalie:12345 http://localhost:8080/hello
```
The response body is

```json
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/hello"
}
```
마찬가지로 끝점 인증을 위해 7장에서 논의한 다른 표현을 사용할 수 있습니다. 다음은 이에 대한 간략한 요약입니다.

- hasAnyAuthority() -- 여러 권한을 지정합니다. 메소드를 호출하려면 사용자에게 이러한 권한 중 하나 이상이 있어야 합니다.

- hasRole()--사용자가 메서드를 호출하기 위해 가져야 하는 역할을 지정합니다.

- hasAnyRole() -- 여러 역할을 지정합니다. 사용자는 메소드를 호출하기 위해 그들 중 적어도 하나가 있어야 합니다.
  
메서드 매개변수의 값을 사용하여 권한 부여 규칙을 정의하는 방법을 증명하기 위해 예제를 확장해 보겠습니다(그림 16.6). 이 예제는 ssia-ch16-ex2라는 프로젝트에서 찾을 수 있습니다.
 
그림 16.6 사전 승인을 구현할 때 승인 규칙에서 메소드 매개변수의 값을 사용할 수 있습니다. 이 예에서는 인증된 사용자만 자신의 비밀 이름에 대한 정보를 검색할 수 있습니다.

이 프로젝트의 경우 Emma 및 Natalie라는 두 사용자와 계속 작업할 수 있도록 첫 번째 예제와 동일한 ProjectConfig 클래스를 정의했습니다. 이제 끝점은 경로 변수를 통해 값을 취하고 서비스 클래스를 호출하여 주어진 사용자 이름에 대한 "비밀 이름"을 얻습니다. 물론, 이 경우에 비밀 이름은 사용자의 특성을 참고하여 제가 만든 것일 뿐이며, 이는 모두가 볼 수 있는 것은 아닙니다. 다음 목록에 나와 있는 대로 컨트롤러 클래스를 정의합니다.

목록 16.5 테스트를 위한 끝점을 정의하는 컨트롤러 클래스
```java
@RestController
public class HelloController {

  @Autowired                                             ❶
  private NameService nameService;

  @GetMapping("/secret/names/{name}")                    ❷
  public List<String> names(@PathVariable String name) {
      return nameService.getSecretNames(name);           ❸
  }
}
```
❶ 컨텍스트에서 보호된 메서드를 정의하는 서비스 클래스의 인스턴스를 주입합니다.

❷ 경로 변수에서 값을 가져오는 끝점을 정의합니다.

❸ 보호된 메서드를 호출하여 사용자의 비밀 이름을 가져옵니다.

이제 목록 16.6에서 NameService 클래스를 구현하는 방법을 살펴보겠습니다. 현재 인증에 사용하는 표현식은 #name == authentication.principal.username입니다. 이 표현식에서 #name을 사용하여 name이라는 getSecretNames() 메소드 매개변수의 값을 참조하고 현재 인증된 사용자를 참조하는 데 사용할 수 있는 인증 객체에 직접 액세스할 수 있습니다. 우리가 사용하는 표현식은 인증된 사용자의 사용자 이름이 메소드의 매개변수를 통해 전송된 값과 동일한 경우에만 메소드를 호출할 수 있음을 나타냅니다. 즉, 사용자는 자신의 비밀 이름만 검색할 수 있습니다.

목록 16.6 NameService 클래스는 보호된 메서드를 정의합니다.
```java
@Service
public class NameService {

  private Map<String, List<String>> secretNames = 
    Map.of(
     "natalie", List.of("Energico", "Perfecto"),
     "emma", List.of("Fantastico"));

  @PreAuthorize                                       ❶
    ("#name == authentication.principal.username")
  public List<String> getSecretNames(String name) {
    return secretNames.get(name);
  }
}
```
❶ #name을 사용하여 인증 표현식에서 메소드 매개변수의 값을 나타냅니다.+

애플리케이션을 시작하고 테스트하여 원하는 대로 작동하는지 확인합니다. 다음 코드 조각은 사용자 이름과 동일한 경로 변수 값을 제공하여 엔드포인트를 호출할 때 애플리케이션의 동작을 보여줍니다.
```sh
curl -u emma:12345 http://localhost:8080/secret/names/emma
```
응답 본문은
```
["Fantastico"]
```
사용자 Emma로 인증할 때 Natalie의 비밀 이름을 얻으려고 합니다. 호출이 작동하지 않음:

```sh
curl -u emma:12345 http://localhost:8080/secret/names/natalie
```

응답 본문은
```json
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/secret/names/natalie"
}
```

그러나 사용자 Natalie는 자신의 비밀 이름을 얻을 수 있습니다. 다음 코드는 이를 증명합니다.

```sh
curl -u natalie:12345 http://localhost:8080/secret/names/natalie
```
응답 본문은
```json
["Energico","Perfecto"]
```

> **참고** 응용 프로그램의 모든 계층에 전역 메서드 보안을 적용할 수 있음을 기억하십시오. 이 장에 제시된 예에서 서비스 클래스의 메소드에 적용된 권한 부여 규칙을 찾을 수 있습니다. 그러나 리포지토리, 관리자, 프록시 등 애플리케이션의 모든 부분에서 전역 메서드 보안과 함께 권한 부여 규칙을 적용할 수 있습니다.

## 16.3 사후 승인 적용

이제 메서드에 대한 호출을 허용하고 싶지만 특정 상황에서는 호출자가 반환된 값을 받지 않도록 하고 싶다고 가정해 보겠습니다. 메서드 호출 후 확인된 권한 부여 규칙을 적용하려면 사후 권한 부여를 사용합니다. 처음에는 다소 어색하게 들릴 수 있습니다. 누군가가 코드를 실행할 수 있지만 결과를 얻지 못하는 이유는 무엇입니까? 

메서드 자체에 관한 것은 아니지만 이 메서드가 웹 서비스나 데이터베이스와 같은 데이터 소스에서 일부 데이터를 검색한다고 상상해 보십시오. 당신은 당신의 메서드가 무엇을 하는지 확신할 수 있지만 당신의 메서드가 호출되는 제3자에게 베팅할 수는 없습니다.

따라서 메서드가 실행되도록 허용하지만 반환되는 내용을 확인하고 기준을 충족하지 않으면 호출자가 반환 값에 액세스하지 못하게 합니다.

Spring Security와 함께 사후 승인 규칙을 적용하기 위해 16.2에서 논의된 @PreAuthorize와 유사한 @PostAuthorize 주석을 사용합니다. 주석은 권한 부여 규칙을 정의하는 SpEL 값으로 수신합니다. @PostAuthorize 주석을 사용하고 메서드에 대한 사후 인증 규칙을 정의하는 방법을 배우는 예제를 계속 진행합니다(그림 16.7).

ssia-ch16-ex3이라는 프로젝트를 만든 이 예제의 시나리오는 Employee라는 개체를 정의합니다. 직원에게는 이름, 책 목록 및 권한 목록이 있습니다. 각 직원을 응용 프로그램의 사용자와 연결합니다. 이 장의 다른 예와 일관성을 유지하기 위해 동일한 사용자인 Emma와 Natalie를 정의합니다. 직원에게 읽기 권한이 있는 경우에만 메서드 호출자가 직원의 세부 정보를 얻도록 하고 싶습니다. 레코드를 검색할 때까지 직원 레코드와 관련된 권한을 모르기 때문에 메서드 실행 후에 권한 부여 규칙을 적용해야 합니다. 이러한 이유로 @PostAuthorize 주석을 사용합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH16_F07_Spilca.png)

그림 16.7 사후 권한 부여를 사용하면 메서드가 호출되는 것을 보호하지 않지만 정의된 권한 부여 규칙을 준수하지 않으면 반환된 값이 노출되지 않도록 보호합니다.
구성 클래스는 이전 예제에서 사용한 것과 동일합니다. 그러나 귀하의 편의를 위해 다음 목록에서 반복합니다.

**목록 16.7** 전역 메서드 보안 활성화 및 사용자 정의
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ProjectConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    var service = new InMemoryUserDetailsManager();

    var u1 = User.withUsername("natalie")
                 .password("12345")
                 .authorities("read")
                 .build();

    var u2 = User.withUsername("emma")
                 .password("12345")
                 .authorities("write")
                 .build();

    service.createUser(u1);
    service.createUser(u2);

    return service;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```
또한 이름, 책 목록 및 역할 목록과 함께 Employee 개체를 나타내는 클래스를 선언해야 합니다. 다음 목록은 Employee 클래스를 정의합니다.

**목록 16.8** Employee 클래스의 정의
```java
public class Employee {

  private String name;
  private List<String> books;
  private List<String> roles;

  // Omitted constructor, getters, and setters
}
```
아마도 데이터베이스에서 직원 세부 정보를 얻을 것입니다. 예제를 더 짧게 유지하기 위해 데이터 소스로 간주되는 몇 개의 레코드가 있는 Map을 사용합니다. 목록 16.9에서 BookService 클래스의 정의를 찾을 수 있습니다. BookService 클래스에는 인증 규칙을 적용하는 메서드도 포함되어 있습니다. @PostAuthorize 주석과 함께 사용하는 표현식이 returnObject 메소드에 의해 반환된 값을 참조하는 것을 관찰하십시오. 사후 인증 식은 메서드가 실행한 후에 사용할 수 있는 메서드에서 반환된 값을 사용할 수 있습니다.

**목록 16.9** 승인된 메서드를 정의하는 BookService 클래스
```java
@Service
public class BookService {

  private Map<String, Employee> records =
    Map.of("emma",
           new Employee("Emma Thompson",
               List.of("Karamazov Brothers"),
               List.of("accountant", "reader")),
           "natalie",
           new Employee("Natalie Parker",
               List.of("Beautiful Paris"),
               List.of("researcher"))
        );
  @PostAuthorize ❶
  ➥ ("returnObject.roles.contains('reader')")
  public Employee getBookDetails(String name) {
      return records.get(name);
  }
}
```
❶ 사후 승인에 대한 표현을 정의합니다.

또한 컨트롤러를 작성하고 권한 부여 규칙을 적용한 메서드를 호출하는 끝점을 구현해 보겠습니다. 다음 목록은 이 컨트롤러 클래스를 나타냅니다.

**목록 16.10** 끝점을 구현하는 컨트롤러 클래스
```java
@RestController
public class BookController {

  @Autowired
  private BookService bookService;

  @GetMapping("/book/details/{name}")
  public Employee getDetails(@PathVariable String name) {
    return bookService.getBookDetails(name);
  }
}
```
이제 애플리케이션을 시작하고 엔드포인트를 호출하여 앱의 동작을 관찰할 수 있습니다. 다음 코드에서 끝점 호출의 예를 찾을 수 있습니다. 반환된 역할 목록에 "reader" 문자열이 포함되어 있기 때문에 모든 사용자가 Emma의 세부 정보에 액세스할 수 있지만 사용자는 Natalie에 대한 세부 정보를 얻을 수 없습니다. 엔드포인트를 호출하여 Emma에 대한 세부 정보를 얻고 사용자 Emma로 인증하려면 다음 명령을 사용합니다.

```sh
curl -u emma:12345 http://localhost:8080/book/details/emma
```
The response body is
```json
{
  "name":"Emma Thompson",
  "books":["Karamazov Brothers"],
  "roles":["accountant","reader"]
}
```
엔드포인트를 호출하여 Emma에 대한 세부 정보를 얻고 사용자 Natalie로 인증할 때 다음 명령을 사용합니다.
```sh
curl -u natalie:12345 http://localhost:8080/book/details/emma
```
응답 본문은
```json
{
  "name":"Emma Thompson",
  "books":["Karamazov Brothers"],
  "roles":["accountant","reader"]
}
```
끝점을 호출하여 Natalie에 대한 세부 정보를 가져오고 사용자 Emma로 인증할 때 다음 명령을 사용합니다.

```sh
curl -u emma:12345 http://localhost:8080/book/details/natalie
```
응답 본문은
```json
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/book/details/natalie"
}
```
끝점을 호출하여 Natalie에 대한 세부 정보를 가져오고 사용자 Natalie로 인증할 때 다음 명령을 사용합니다.
```sh
curl -u natalie:12345 http://localhost:8080/book/details/natalie
```
The response body is
```json
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/book/details/natalie"
}
```

> 참고 요구 사항에 사전 승인과 사후 승인이 모두 필요한 경우 동일한 방법으로 @PreAuthorize 및 @PostAuthorize를 모두 사용할 수 있습니다.

## 16.4 메서드에 대한 권한 구현

지금까지 사전 승인 및 사후 승인에 대한 간단한 표현식으로 규칙을 정의하는 방법을 배웠습니다. 이제 권한 부여 논리가 더 복잡하고 한 줄로 작성할 수 없다고 가정해 보겠습니다. 거대한 SpEL 표현식을 작성하는 것은 확실히 편하지 않습니다. 권한 부여 규칙인지 여부에 관계없이 어떤 상황에서도 긴 SpEL 표현식을 사용하는 것을 권장하지 않습니다. 단순히 읽기 어려운 코드를 생성하고 이는 앱의 유지 관리 가능성에 영향을 미칩니다. 복잡한 권한 부여 규칙을 구현해야 하는 경우 긴 SpEL 표현식을 작성하는 대신 별도의 클래스에서 논리를 가져옵니다. Spring Security는 권한 개념을 제공하므로 별도의 클래스에서 권한 부여 규칙을 쉽게 작성하여 애플리케이션을 읽고 이해하기 쉽습니다.

이 섹션에서는 프로젝트 내 권한을 사용하여 권한 부여 규칙을 적용합니다. 이 프로젝트의 이름을 ssia-ch16-ex4로 지정했습니다. 이 시나리오에는 문서를 관리하는 애플리케이션이 있습니다. 모든 문서에는 문서를 만든 사용자인 소유자가 있습니다. 기존 문서의 세부 정보를 얻으려면 사용자가 관리자이거나 문서의 소유자여야 합니다. 우리는 이 요구 사항을 해결하기 위해 권한 평가기를 구현합니다. 다음 목록은 일반 Java 객체일 뿐인 문서를 정의합니다.

목록 16.11 Document 클래스
```java
public class Document {

  private String owner;

  // Omitted constructor, getters, and setters
}
```
데이터베이스를 흉내내고 편의를 위해 예제를 더 짧게 만들기 위해 Map에서 몇 가지 문서 인스턴스를 관리하는 리포지토리 클래스를 만들었습니다. 다음 목록에서 이 클래스를 찾을 수 있습니다.

목록 16.12 몇 가지 Document 인스턴스를 관리하는 DocumentRepository 클래스
```java
@Repository
public class DocumentRepository {

  private Map<String, Document> documents =      ❶
    Map.of("abc123", new Document("natalie"),
           "qwe123", new Document("natalie"),
           "asd555", new Document("emma"));

  public Document findDocument(String code) {
    return documents.get(code);                  ❷
  }
}
```
❶ 각 문서를 고유 코드로 식별하고 소유자 이름 지정

❷ 고유식별코드를 이용하여 문서 획득

서비스 클래스는 저장소를 사용하여 해당 코드로 문서를 얻는 메소드를 정의합니다. 서비스 클래스의 메서드는 권한 부여 규칙을 적용하는 메서드입니다. 클래스의 논리는 간단합니다. 고유 코드로 Document를 반환하는 메서드를 정의합니다. 이 메소드에 @PostAuthorize 주석을 달고 hasPermission() SpEL 표현식을 사용합니다. 이 방법을 사용하면 이 예제에서 추가로 구현하는 외부 인증 표현식을 참조할 수 있습니다. 한편, 우리가 hasPermission() 메서드에 제공하는 매개변수는 메서드에서 반환된 값을 나타내는 returnObject와 액세스를 허용하는 역할의 이름인 'ROLE_admin'입니다. 다음 목록에서 이 클래스의 정의를 찾을 수 있습니다.

목록 16.13 보호된 메서드를 구현하는 DocumentService 클래스
```java
@Service
public class DocumentService {

  @Autowired
  private DocumentRepository documentRepository;

  @PostAuthorize                                    ❶
  ("hasPermission(returnObject, 'ROLE_admin')")
  public Document getDocument(String code) {
    return documentRepository.findDocument(code);
  }
}
```
❶ hasPermission() 표현식을 사용하여 인증 표현식 참조

권한 로직을 구현하는 것은 우리의 의무입니다. 그리고 PermissionEvaluator 계약을 구현하는 객체를 작성하여 이를 수행합니다. PermissionEvaluator 계약은 권한 논리를 구현하는 두 가지 방법을 제공합니다.

- 개체 및 권한별 -- 현재 예에서 사용된 권한 평가자는 권한 부여 규칙이 적용되는 개체와 권한 논리를 구현하는 데 필요한 추가 세부 정보를 제공하는 개체의 두 가지 개체를 수신한다고 가정합니다.

- 개체 ID, 개체 유형 및 권한별--권한 평가자가 필요한 개체를 검색하는 데 사용할 수 있는 개체 ID를 수신한다고 가정합니다. 또한 동일한 권한 평가자가 여러 객체 유형에 적용되는 경우 사용할 수 있는 객체 유형을 수신하며 권한 평가를 위한 추가 세부 정보를 제공하는 객체가 필요합니다.

다음 목록에서 두 가지 방법이 있는 PermissionEvaluator 계약을 찾습니다.

**목록 16.14** PermissionEvaluator 계약 정의
```java
public interface PermissionEvaluator {

    boolean hasPermission(
              Authentication a, 
              Object subject,
              Object permission);

    boolean hasPermission(
              Authentication a, 
              Serializable id, 
              String type, 
              Object permission);
}
```
현재 예제에서는 첫 번째 방법을 사용하는 것으로 충분합니다. 우리는 이미 주제를 가지고 있으며, 우리의 경우 메소드에서 반환된 값입니다. 또한 예제 시나리오에 정의된 대로 모든 문서에 액세스할 수 있는 역할 이름 'ROLE_admin'을 보냅니다. 물론 이 예에서는 권한 평가자 클래스에서 역할 이름을 직접 사용하고 hasPermission() 객체의 값으로 보내는 것을 피할 수 있습니다. 여기서는 예제를 위해 전자만 수행합니다. 더 복잡할 수 있는 실제 시나리오에는 여러 가지 방법이 있으며 권한 부여 프로세스에 필요한 세부 정보는 각각 다를 수 있습니다. 이러한 이유로 메서드 수준에서 권한 부여 논리에 사용하기 위해 필요한 세부 정보를 보낼 수 있는 매개 변수가 있습니다.

당신의 인식과 혼란을 피하기 위해, 나는 또한 당신이 인증 객체를 전달할 필요가 없다는 것을 언급하고 싶습니다. Spring Security는 hasPermission() 메소드를 호출할 때 이 매개변수 값을 자동으로 제공합니다. 프레임워크는 이미 SecurityContext에 있기 때문에 인증 인스턴스의 값을 알고 있습니다. 목록 16.15에서 DocumentsPermissionEvaluator 클래스를 찾았습니다. 이 클래스는 이 예제에서 사용자 지정 권한 부여 규칙을 정의하기 위해 PermissionEvaluator 계약을 구현합니다.

목록 16.15 권한 부여 규칙 구현하기
```java
@Component
public class DocumentsPermissionEvaluator
  implements PermissionEvaluator { ❶

  @Override
  public boolean hasPermission(
    Authentication authentication,
    Object target,
    Object permission) {
    
    Document document = (Document) target; ❷
    String p = (String) permission; ❸

    boolean admin =                                  ❹
      authentication.getAuthorities()
        .stream()
        .anyMatch(a -> a.getAuthority().equals(p));

    return admin ||                                  ❺
      document.getOwner()
        .equals(authentication.getName());
      
  }

  @Override
  public boolean hasPermission(Authentication authentication,
                               Serializable targetId,
                               String targetType,
                               Object permission) {
    return false; ❻
  }
}
```
❶ PermissionEvaluator 계약 구현

❷ 대상 객체를 Document로 캐스트

❸ 우리의 경우 권한 객체는 역할 이름이므로 문자열로 변환합니다.

❹ 인증 사용자에게 매개변수로 받은 역할이 있는지 확인

❺ admin 또는 인증된 사용자가 문서의 소유자인 경우 권한을 부여합니다.

❻ 두 번째 방법은 사용하지 않기 때문에 구현할 필요가 없습니다.

Spring Security가 새로운 PermissionEvaluator 구현을 인식하도록 하려면 구성 클래스에서 MethodSecurityExpressionHandler를 정의해야 합니다. 다음 목록은 사용자 지정 PermissionEvaluator를 알리기 위해 MethodSecurityExpressionHandler를 정의하는 방법을 보여줍니다.

Listing 16.16 구성 클래스에서 PermissionEvaluator 구성하기
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ProjectConfig 
  extends GlobalMethodSecurityConfiguration {

  @Autowired
  private DocumentsPermissionEvaluator evaluator;

  @Override                                                             ❶
  protected MethodSecurityExpressionHandler createExpressionHandler() {
    var expressionHandler =                                             ❷
        new DefaultMethodSecurityExpressionHandler();

    expressionHandler.setPermissionEvaluator(
        evaluator);                                                     ❸

    return expressionHandler;                                           ❹
  }

  // Omitted definition of the UserDetailsService and PasswordEncoder beans
}
```
❶ Overrides the createExpressionHandler() method

❷ Defines a default security expression handler to set up the custom permission evaluator

❸ Sets up the custom permission evaluator

❹ Returns the custom expression handler

> **참고** 여기에서는 Spring Security가 제공하는 DefaultMethodSecurityExpressionHandler라는 MethodSecurityExpressionHandler에 대한 구현을 사용합니다. 사용자 정의 MethodSecurityExpressionHandler를 구현하여 권한 부여 규칙을 적용하는 데 사용하는 사용자 정의 SpEL 표현식을 정의할 수도 있습니다. 실제 시나리오에서는 이 작업을 거의 수행할 필요가 없으며 이러한 이유로 예제에서는 이러한 사용자 지정 개체를 구현하지 않습니다.

새 코드에만 집중할 수 있도록 UserDetailsService와 PasswordEncoder의 정의를 분리했습니다. 목록 16.17에서 나머지 구성 클래스를 찾을 수 있습니다. 사용자에 대해 주의해야 할 유일한 중요한 사항은 역할입니다. 사용자 Natalie는 관리자이며 모든 문서에 액세스할 수 있습니다. 사용자 Emma는 관리자이며 자신의 문서에만 액세스할 수 있습니다.

목록 16.7 구성 클래스의 전체 정의
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ProjectConfig 
  extends GlobalMethodSecurityConfiguration {

  @Autowired
  private DocumentsPermissionEvaluator evaluator;

  @Override
  protected MethodSecurityExpressionHandler createExpressionHandler() {
    var expressionHandler =
        new DefaultMethodSecurityExpressionHandler();

    expressionHandler.setPermissionEvaluator(evaluator);

    return expressionHandler;
  }

  @Bean
  public UserDetailsService userDetailsService() {
    var service = new InMemoryUserDetailsManager();

    var u1 = User.withUsername("natalie")
             .password("12345")
             .roles("admin")
             .build();

     var u2 = User.withUsername("emma")
              .password("12345")
              .roles("manager")
              .build();

     service.createUser(u1);
     service.createUser(u2);

     return service;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```
애플리케이션을 테스트하기 위해 엔드포인트를 정의합니다. 다음 목록은 이 정의를 나타냅니다.

목록 16.18 컨트롤러 클래스 정의 및 끝점 구현
```java
@RestController
public class DocumentController {

  @Autowired
  private DocumentService documentService;

  @GetMapping("/documents/{code}")
  public Document getDetails(@PathVariable String code) {
    return documentService.getDocument(code);
  }
}
```
애플리케이션을 실행하고 엔드포인트를 호출하여 동작을 관찰해 보겠습니다. 사용자 Natalie는 소유자에 관계없이 문서에 액세스할 수 있습니다. 사용자 Emma는 자신이 소유한 문서에만 액세스할 수 있습니다. Natalie에 속한 문서의 끝점을 호출하고 사용자 "natalie"로 인증하기 위해 다음 명령을 사용합니다.
```sh
curl -u natalie:12345 http://localhost:8080/documents/abc123
```
The response body is
```json
{
  "owner":"natalie"
}
```
Emma에 속한 문서의 끝점을 호출하고 사용자 "natalie"로 인증하기 위해 다음 명령을 사용합니다.
```sh
curl -u natalie:12345 http://localhost:8080/documents/asd555
```
The response body is
```json
{
  "owner":"emma"
}
```
Emma에 속한 문서의 끝점을 호출하고 사용자 "emma"로 인증하기 위해 다음 명령을 사용합니다.
```sh
curl -u emma:12345 http://localhost:8080/documents/asd555
```
The response body is
```json
{
  "owner":"emma"
}
```
Natalie에 속한 문서에 대한 끝점을 호출하고 사용자 "emma"로 인증하기 위해 다음 명령을 사용합니다.

```sh
curl -u emma:12345 http://localhost:8080/documents/abc123
```
The response body is
```json
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/documents/abc123"
}
```
비슷한 방식으로 두 번째 PermissionEvaluator 메서드를 사용하여 권한 부여 식을 작성할 수 있습니다. 두 번째 방법은 객체 자체가 아닌 식별자와 주체 유형을 사용하는 것입니다. 예를 들어 @PreAuthorize를 사용하여 메서드가 실행되기 전에 권한 부여 규칙을 적용하도록 현재 예제를 변경하고 싶다고 가정합니다. 이 경우 반환된 객체가 아직 없습니다. 그러나 개체 자체를 갖는 대신 고유 식별자인 문서의 코드가 있습니다. Listing 16.19는 이 시나리오를 구현하기 위해 권한 평가자 클래스를 변경하는 방법을 보여줍니다. 개별적으로 실행할 수 있는 ssia-ch16-ex5라는 프로젝트에서 예제를 분리했습니다.

Listing 16.19 Changes in the DocumentsPermissionEvaluator class
```java
@Component
public class DocumentsPermissionEvaluator
  implements PermissionEvaluator {

  @Autowired
  private DocumentRepository documentRepository;

  @Override
  public boolean hasPermission(Authentication authentication,
                               Object target,
                               Object permission) {
    return false;                                              ❶
  }

  @Override
  public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {

    String code = targetId.toString();                         ❷
    Document document = documentRepository.findDocument(code);

    String p = (String) permission;

    boolean admin =                                            ❸
           authentication.getAuthorities()
              .stream()
              .anyMatch(a -> a.getAuthority().equals(p));

     return admin ||                                           ❹
       document.getOwner().equals(
         authentication.getName());
  }
}
```
❶ 더 이상 첫 번째 방법을 통해 권한 부여 규칙을 정의하지 않습니다.

❷ 객체를 갖는 대신에 그 ID를 갖고, 그 ID를 사용하여 객체를 얻는다.

❸ 사용자가 관리자인지 확인

❹ 사용자가 문서의 관리자 또는 소유자인 경우 해당 사용자는 문서에 접근할 수 있습니다.

물론 @PreAuthorize 주석과 함께 권한 평가자에 대한 적절한 호출도 사용해야 합니다. 다음 목록에서 새 메서드에 권한 부여 규칙을 적용하기 위해 DocumentService 클래스에서 변경한 사항을 확인할 수 있습니다.

목록 16.20 DocumentService 클래스
```java
@Service
public class DocumentService {

  @Autowired
  private DocumentRepository documentRepository;

  @PreAuthorize                                         ❶
   ("hasPermission(#code, 'document', 'ROLE_admin')")
  public Document getDocument(String code) {
    return documentRepository.findDocument(code);
  }
}
```
❶ 권한 평가자의 두 번째 방법을 사용하여 사전 승인 규칙을 적용합니다.

응용 프로그램을 다시 실행하고 끝점의 동작을 확인할 수 있습니다. 권한 평가자의 첫 번째 방법을 사용하여 권한 부여 규칙을 구현한 경우와 동일한 결과를 볼 수 있습니다. 사용자 Natalie는 관리자이며 모든 문서의 세부 정보에 액세스할 수 있지만 사용자 Emma는 자신이 소유한 문서에만 액세스할 수 있습니다. Natalie에 속한 문서의 끝점을 호출하고 사용자 "natalie"로 인증하면 다음 명령이 실행됩니다.

```sh
curl -u natalie:12345 http://localhost:8080/documents/abc123
```
The response body is
```json
{
  "owner":"natalie"
}
```
Emma에 속한 문서의 끝점을 호출하고 사용자 "natalie"로 인증하면 다음 명령이 실행됩니다.
```sh
curl -u natalie:12345 http://localhost:8080/documents/asd555
```
The response body is
```json
{
  "owner":"emma"
}
```
Emma에 속한 문서의 끝점을 호출하고 사용자 "emma"로 인증하면 다음 명령을 실행합니다.
```sh
curl -u emma:12345 http://localhost:8080/documents/asd555
```
응답은
```json
{
  "owner":"emma"
}
```
123 / 5000
번역 결과
Natalie에 속한 문서의 끝점을 호출하고 사용자 "emma"로 인증하면 다음 명령이 실행됩니다. 
```sh
curl -u emma:12345 http://localhost:8080/documents/abc123
```
응답은
```json
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/documents/abc123"
}
```
@Secured 및 @RolesAllowed 주석 사용

이 장 전체에서 우리는 전역 메서드 보안과 함께 권한 부여 규칙을 적용하는 것에 대해 논의했습니다. 이 기능은 기본적으로 비활성화되어 있으며 구성 클래스에 대해 @EnableGlobalMethodSecurity 주석을 사용하여 활성화할 수 있다는 것을 학습하는 것으로 시작했습니다. 또한 @EnableGlobalMethodSecurity 주석의 속성을 사용하여 권한 부여 규칙을 적용하는 특정 방법을 지정해야 합니다. 다음과 같이 주석을 사용했습니다.
```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
```
prePostEnabled 속성을 사용하면 @PreAuthorize 및 @PostAuthorize 주석이 권한 부여 규칙을 지정할 수 있습니다. @EnableGlobalMethodSecurity 주석은 다른 주석을 활성화하는 데 사용할 수 있는 두 가지 다른 유사한 속성을 제공합니다. jsr250Enabled 속성을 사용하여 @RolesAllowed 주석을 활성화하고 secureEnabled 속성을 사용하여 @Secured 주석을 활성화합니다. @Secured 및 @RolesAllowed의 두 주석을 사용하는 것은 @PreAuthorize 및 @PostAuthorize를 사용하는 것보다 덜 강력하며 실제 시나리오에서 찾을 가능성이 적습니다. 그럼에도 불구하고 나는 당신에게 두 가지 모두를 알리고 싶지만 세부 사항에 너무 많은 시간을 할애하지 마십시오.

@EnableGlobalMethodSecurity의 속성을 true로 설정하여 사전 승인 및 사후 승인과 동일한 방식으로 이러한 주석을 사용할 수 있습니다. @Secure 또는 @RolesAllowed와 같은 한 종류의 주석 사용을 나타내는 속성을 활성화합니다. 다음 코드 스니펫에서 이를 수행하는 방법의 예를 찾을 수 있습니다.

```java
@EnableGlobalMethodSecurity(
        jsr250Enabled = true,
        securedEnabled = true
)
```
이러한 속성을 활성화하면 @RolesAllowed 또는 @Secured 주석을 사용하여 로그인한 사용자가 특정 메서드를 호출해야 하는 역할이나 권한을 지정할 수 있습니다. 다음 코드 조각은 @RolesAllowed 주석을 사용하여 ADMIN 역할을 가진 사용자만 getName() 메서드를 호출할 수 있도록 지정하는 방법을 보여줍니다.

```java
@Service
public class NameService {

  @RolesAllowed("ROLE_ADMIN")
  public String getName() {
      return "Fantastico";
  }
}
```
유사하게, 다음 코드 조각이 제시하는 것처럼 @RolesAllowed 주석 대신 @Secured 주석을 사용할 수 있습니다.

```java
@Service
public class NameService {
  @Secured("ROLE_ADMIN")
  public String getName() {
      return "Fantastico";
  }
}
```
이제 예제를 테스트할 수 있습니다. 다음 코드 스니펫은 이 작업을 수행하는 방법을 보여줍니다.

```sh
curl -u emma:12345 http://localhost:8080/hello
```
응답은

```
Hello, Fantastico
```
엔드포인트를 호출하고 사용자 Natalie로 인증하려면 다음 명령을 사용하십시오.

```sh
curl -u natalie:12345 http://localhost:8080/hello
```
응답은
```json
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/hello"
}
```
ssia-ch16-ex6 프로젝트에서 @RolesAllowed 및 @Secured 주석을 사용하여 전체 예제를 찾을 수 있습니다.

## 요약

- Spring Security를 ​​사용하면 엔드포인트 수준뿐만 아니라 애플리케이션의 모든 계층에 권한 부여 규칙을 적용할 수 있습니다. 이렇게 하려면 전역 메서드 보안 기능을 활성화합니다.

- 전역 메서드 보안 기능은 기본적으로 비활성화되어 있습니다. 이를 활성화하려면 애플리케이션의 구성 클래스에 @EnableGlobalMethodSecurity 주석을 사용합니다.

- 메소드를 호출하기 전에 애플리케이션이 확인하는 권한 부여 규칙을 적용할 수 있습니다. 이러한 권한 부여 규칙을 따르지 않으면 프레임워크에서 메서드 실행을 허용하지 않습니다. 메서드 호출 전에 권한 부여 규칙을 테스트할 때 사전 권한 부여를 사용합니다.

- 사전 승인을 구현하려면 승인 규칙을 정의하는 SpEL 표현식의 값과 함께 @PreAuthorize 주석을 사용합니다.

- 호출자가 반환된 값을 사용할 수 있는지 여부와 실행 흐름을 진행할 수 있는지 여부를 메서드 호출 후에만 결정하려는 경우 사후 승인을 사용합니다.

- 사후 인증을 구현하기 위해 인증 규칙을 나타내는 SpEL 표현식 값과 함께 @PostAuthorize 주석을 사용합니다.

- 복잡한 권한 부여 로직을 구현할 때 코드를 읽기 쉽게 하기 위해 이 로직을 다른 클래스로 분리해야 합니다. Spring Security에서 이를 수행하는 일반적인 방법은 PermissionEvaluator를 구현하는 것입니다.

- Spring Security는 @RolesAllowed 및 @Secured 주석과 같은 이전 사양과의 호환성을 제공합니다. 이것을 사용할 수 있지만 @PreAuthorize 및 @PostAuthorize보다 덜 강력하고 실제 시나리오에서 Spring과 함께 사용되는 이러한 주석을 찾을 가능성은 매우 낮습니다.
