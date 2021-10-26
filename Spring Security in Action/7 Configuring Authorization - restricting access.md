# 7 권한 설정: 접근 제한

이 장에서는 다음을 다룹니다.

- 권한 및 역할 정의
- 엔드포인트에 권한 부여 규칙 적용

몇 년 전 아름다운 카르파티아 산맥에서 스키를 타던 중 이 재미있는 장면을 목격했습니다. 약 10명, 아마도 15명의 사람들이 스키 슬로프 정상에 오르기 위해 캐빈에 들어가기 위해 줄을 서고 있었습니다. 두 명의 경호원과 함께 유명한 팝 아티스트가 나타났습니다. 그는 유명하기 때문에 대기열을 건너 뛰기를 기대하면서 자신있게 걸어 올라갔습니다. 라인의 선두에 도달, 그는 놀라움을 얻었다. “티켓 주세요!” 탑승을 관리하는 사람은 "글쎄요, 먼저 표가 필요하고, 두 번째로 이 탑승에는 우선 순위가 없습니다. 죄송합니다. 대기열은 거기서 끝납니다.” 그는 줄의 끝을 가리켰다. 삶의 대부분의 경우와 마찬가지로, 당신이 누구인지는 중요하지 않습니다. 소프트웨어 응용 프로그램에 대해서도 마찬가지입니다. 특정 기능이나 데이터에 액세스하려고 할 때 당신이 누구인지는 중요하지 않습니다!

지금까지 우리는 인증, 즉 배웠듯이 애플리케이션이 리소스의 호출자를 식별하는 프로세스에 대해서만 논의했습니다. 이전 장에서 작업한 예제에서는 요청 승인 여부를 결정하는 규칙을 구현하지 않았습니다. 우리는 시스템이 사용자를 알고 있는지 여부에만 신경을 썼습니다. 대부분의 응용 프로그램에서 시스템에서 식별한 모든 사용자가 시스템의 모든 리소스에 액세스할 수 있는 것은 아닙니다. 이 장에서는 권한 부여에 대해 설명합니다. 인증은 식별된 클라이언트가 요청된 리소스에 액세스할 수 있는 권한이 있는지 시스템이 결정하는 프로세스입니다(그림 7.1).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH07_F01_Spilca.png)

그림 7.1 권한 부여는 인증된 엔터티가 리소스에 액세스할 수 있는지 여부를 애플리케이션이 결정하는 프로세스입니다. 권한 부여는 항상 인증 후에 발생합니다.

Spring Security에서 애플리케이션은 인증 흐름을 종료하면 요청을 인증 필터에 위임합니다. 필터는 구성된 권한 부여 규칙에 따라 요청을 허용하거나 거부합니다(그림 7.2).

권한 부여에 대한 모든 필수 세부 정보를 다루기 위해 이 장에서 다음 단계를 따릅니다.

1. 권한이 무엇인지 이해하고 사용자 권한을 기반으로 모든 엔드포인트에 액세스 규칙을 적용합니다.

2. 권한을 역할별로 그룹화하는 방법과 사용자의 역할에 따라 권한 부여 규칙을 적용하는 방법을 배웁니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH07_F02_Spilca.png)

그림 7.2 클라이언트가 요청하면 인증 필터가 사용자를 인증합니다. 인증에 성공하면 인증 필터가 사용자 세부 정보를 보안 컨텍스트에 저장하고 요청을 권한 부여 필터로 전달합니다. 권한 부여 필터는 호출이 허용되는지 여부를 결정합니다. 요청을 승인할지 여부를 결정하기 위해 권한 부여 필터는 보안 컨텍스트의 세부 정보를 사용합니다.

8장에서는 계속해서 권한 부여 규칙을 적용할 끝점을 선택합니다. 지금은 권한과 역할을 살펴보고 이것이 애플리케이션에 대한 액세스를 제한하는 방법을 살펴보겠습니다.

## 7.1 권한 및 역할에 따른 접근 제한

권한 부여 및 역할의 개념에 대해 알아봅니다. 이를 사용하여 애플리케이션의 모든 엔드포인트를 보호합니다. 사용자마다 권한이 다른 실제 시나리오에 적용하려면 먼저 이러한 개념을 이해해야 합니다. 사용자가 가진 권한에 따라 특정 작업만 실행할 수 있습니다. 애플리케이션은 권한 및 역할로 권한을 제공합니다.

3장에서 GrantedAuthority 인터페이스를 구현했습니다. 또 다른 필수 구성요소인 UserDetails 인터페이스를 논의할 때 이 계약을 소개했습니다. 이 인터페이스는 주로 권한 부여 프로세스와 관련이 있기 때문에 우리는 GrantedAuthority와 함께 작업하지 않았습니다. 

이제 GrantedAuthority로 돌아가 목적을 조사할 수 있습니다. 그림 7.3은 UserDetails 계약과 GrantedAuthority 인터페이스 간의 관계를 나타냅니다. 이 계약에 대한 논의를 마치면 이러한 규칙을 개별적으로 또는 특정 요청에 사용하는 방법을 배우게 됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH07_F03_Spilca.png)

그림 7.3 사용자에게는 하나 이상의 권한이 있습니다(사용자가 수행할 수 있는 작업). 인증 프로세스 동안 UserDetailsService는 권한을 포함하여 사용자에 대한 모든 세부 정보를 얻습니다. 애플리케이션은 사용자를 성공적으로 인증한 후 권한 부여를 위해 GrantedAuthority 인터페이스가 나타내는 권한을 사용합니다.

목록 7.1은 GrantedAuthority 계약의 정의를 보여줍니다. 권한은 사용자가 시스템 리소스로 수행할 수 있는 작업입니다. 권한에는 객체의 getAuthority() 동작이 문자열로 반환하는 이름이 있습니다. 사용자 지정 권한 부여 규칙을 정의할 때 권한 이름을 사용합니다. 종종 권한 부여 규칙은 "Jane은 제품 레코드를 삭제할 수 있습니다." 또는 "John은 문서 레코드를 읽을 수 있습니다."와 같이 보일 수 있습니다. 이러한 경우 삭제 및 읽기가 부여된 권한입니다. 애플리케이션을 통해 사용자 Jane과 John은 읽기, 쓰기 또는 삭제와 같은 이름을 갖는 이러한 작업을 수행할 수 있습니다.

Listing 7.1 The GrantedAuthority contract
```java
public interface GrantedAuthority extends Serializable {
  String getAuthority();
}
```
Spring Security에서 사용자를 설명하는 계약인 UserDetails에는 그림 7.3과 같이 GrantedAuthority 인스턴스 모음이 있습니다. 사용자에게 하나 이상의 권한을 허용할 수 있습니다. getAuthorities() 메서드는 GrantedAuthority의 컬렉션을 반환합니다. 목록 7.2에서 UserDetails 계약에서 이 메소드를 검토할 수 있습니다. 사용자에게 부여된 모든 권한을 반환하도록 이 메서드를 구현합니다. 인증이 종료된 후 권한은 로그인한 사용자에 대한 세부 정보의 일부이며 애플리케이션이 권한을 부여하는 데 사용할 수 있습니다.

Listing 7.2 The getAuthorities() method from the UserDetails contract
```java
public interface UserDetails extends Serializable {
  Collection<? extends GrantedAuthority> getAuthorities();

  // Omitted code
}
```

### 7.1.1 사용자 권한에 따라 모든 엔드포인트에 대한 액세스 제한

특정 사용자의 엔드포인트에 대한 액세스 제한에 대해 설명합니다. 지금까지 예제에서 인증된 사용자는 애플리케이션의 모든 엔드포인트를 호출할 수 있습니다. 이제부터 이 액세스를 사용자 지정하는 방법을 배웁니다. 프로덕션 환경에서 찾은 앱에서는 인증되지 않은 경우에도 애플리케이션의 일부 엔드포인트를 호출할 수 있지만 다른 앱에는 특별한 권한이 필요합니다(그림 7.4). 이러한 제한을 적용할 수 있는 다양한 방법을 배울 수 있도록 몇 가지 예제를 작성할 것입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH07_F04_Spilca.png)

그림 7.4 권한은 사용자가 애플리케이션에서 수행할 수 있는 작업입니다. 이러한 작업을 기반으로 권한 부여 규칙을 구현합니다. 특정 권한이 있는 사용자만 엔드포인트에 특정 요청을 할 수 있습니다. 예를 들어 Jane은 끝점을 읽고 쓸 수만 있는 반면 John은 끝점을 읽고, 쓰고, 삭제하고, 업데이트할 수 있습니다.

이제 UserDetails 및 GrantedAuthority 계약과 이들 간의 관계를 기억했으므로 권한 부여 규칙을 적용하는 작은 앱을 작성할 차례입니다. 이 예에서는 사용자의 권한을 기반으로 끝점에 대한 액세스를 구성하는 몇 가지 대안을 배웁니다. ssia-ch7-ex1이라는 새 프로젝트를 시작합니다. 다음 방법을 사용하여 언급한 대로 액세스를 구성할 수 있는 세 가지 방법을 보여 드리겠습니다.

- hasAuthority()--응용 프로그램이 제한을 구성하는 하나의 권한만 매개변수로 수신합니다. 해당 권한이 있는 사용자만 엔드포인트를 호출할 수 있습니다.

- hasAnyAuthority()--응용 프로그램이 제한을 구성하는 둘 이상의 권한을 받을 수 있습니다. 나는 이 방법이 "주어진 권한을 가지고 있다"고 기억합니다. 사용자가 요청하려면 지정된 권한 중 하나 이상이 있어야 합니다.

사용자에게 할당한 권한의 수에 따라 단순성을 위해 이 메서드 또는 hasAuthority() 메서드를 사용하는 것이 좋습니다. 구성에서 읽기 쉽고 코드를 더 쉽게 이해할 수 있습니다.

- access()--응용 프로그램이 SpEL(Spring Expression Language)을 기반으로 하는 권한 부여 규칙을 구축하기 때문에 액세스를 구성할 수 있는 무한한 가능성을 제공합니다. 그러나 코드를 읽고 디버그하기가 더 어려워집니다. 이러한 이유로 hasAnyAuthority() 또는 hasAuthority() 메서드를 적용할 수 없는 경우에만 덜 솔루션으로 권장합니다.

pom.xml 파일에 필요한 유일한 종속성은 spring-boot-starter-web 및 spring-boot-starter-security입니다. 이러한 종속성은 이전에 열거한 세 가지 솔루션 모두에 접근하기에 충분합니다. 이 예는 ssia-ch7-ex1 프로젝트에서 찾을 수 있습니다.

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
We also add an endpoint in the application to test our authorization configuration:
```java
@RestController
public class HelloController {
    
  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
```
구성 클래스에서 InMemoryUserDetailsManager를 UserDetailsService로 선언하고 이 인스턴스에서 관리할 두 명의 사용자 John과 Jane을 추가합니다. 사용자마다 권한이 다릅니다. 다음 목록에서 이 작업을 수행하는 방법을 볼 수 있습니다.

Listing 7.3 Declaring the UserDetailsService and assigning users
```java
@Configuration
public class ProjectConfig {

  @Bean ❶
  public UserDetailsService userDetailsService() {
    var manager = new InMemoryUserDetailsManager(); ❷

    var user1 = User.withUsername("john") ❸
                    .password("12345")
                    .authorities("READ")
                    .build();

    var user2 = User.withUsername("jane") ❹
                    .password("12345")
                    .authorities("WRITE")
                    .build();

    manager.createUser(user1); ❺
    manager.createUser(user2);

    return manager;
  }

  @Bean ❻
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance(); 
  }
}
```
❶ 메서드에서 반환된 UserDetailsService가 SpringContext에 추가됩니다.

❷ 몇 명의 사용자를 저장하는 InMemoryUserDetailsManager를 선언합니다.

❸ 첫 번째 사용자 john은 READ 권한을 가지고 있습니다.

❹ 두 번째 사용자 jane은 WRITE 권한을 가지고 있습니다.

❺ 사용자는 UserDetailsService에 의해 추가 및 관리됩니다.

❻ PasswordEncoder도 필요하다는 것을 잊지 마십시오.

다음으로 할 일은 권한 부여 구성을 추가하는 것입니다. 2장에서 첫 번째 예제에서 작업할 때 모든 엔드포인트에 모든 사람이 액세스할 수 있도록 하는 방법을 보았습니다. 이를 위해 다음 목록에서 볼 수 있는 것과 유사하게 WebSecurityConfigurerAdapter 클래스를 확장하고 configure() 메서드를 재정의했습니다.

목록 7.4 인증 없이 모든 사람이 모든 엔드포인트에 액세스할 수 있도록 만들기
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
        
    http.authorizeRequests()
          .anyRequest().permitAll(); ❶
    }
}
```
❶ 모든 요청에 대한 접근 허용

AuthorizeRequests() 메서드를 사용하면 끝점에 대한 권한 부여 규칙을 계속 지정할 수 있습니다. anyRequest() 메서드는 사용된 URL 또는 HTTP 메서드에 관계없이 규칙이 모든 요청에 적용됨을 나타냅니다. permitAll() 메서드는 인증 여부에 관계없이 모든 요청에 대한 액세스를 허용합니다.

WRITE 권한이 있는 사용자만 모든 엔드포인트에 액세스할 수 있도록 하고 싶다고 가정해 보겠습니다. 이 예에서는 Jane만 의미합니다. 목표를 달성하고 이번에는 사용자 권한에 따라 액세스를 제한할 수 있습니다. 다음 목록의 코드를 살펴보십시오.

목록 7.5 WRITE 권한이 있는 사용자만 액세스 제한
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
         .anyRequest()
          .hasAuthority("WRITE"); ❶
  }
}
```
❶ 사용자가 엔드포인트에 액세스할 수 있는 조건을 지정합니다.

permitAll() 메서드를 has-Authority() 메서드로 대체한 것을 볼 수 있습니다. hasAuthority() 메소드의 매개변수로 사용자에게 허용된 권한의 이름을 제공합니다. 애플리케이션은 먼저 요청을 인증한 다음 사용자의 권한에 따라 호출 허용 여부를 결정해야 합니다.

이제 두 사용자 각각과 함께 엔드포인트를 호출하여 애플리케이션 테스트를 시작할 수 있습니다. 사용자 Jane으로 엔드포인트를 호출하면 HTTP 응답 상태는 200 OK이고 응답 본문은 "Hello!"입니다. 사용자 John과 함께 호출할 때 HTTP 응답 상태는 403 Forbidden이고 빈 응답 본문을 다시 받습니다. 예를 들어 사용자 Jane과 함께 이 끝점을 호출하면
```sh
curl -u jane:12345 http://localhost:8080/hello
```
we get this response:
```
Hello!
```
Calling the endpoint with user John,

```sh
curl -u john:12345 http://localhost:8080/hello
```
we get this response:
```json
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/hello"
}
```
비슷한 방법으로 hasAnyAuthority() 메서드를 사용할 수 있습니다. 이 메소드에는 varargs 매개변수가 있습니다. 이런 식으로 여러 기관 이름을 받을 수 있습니다. 사용자에게 메소드에 대한 매개변수로 제공된 권한 중 하나 이상이 있는 경우 애플리케이션은 요청을 허용합니다. 이전 목록의 hasAuthority()를 hasAnyAuthority("WRITE")로 바꿀 수 있습니다. 이 경우 애플리케이션은 정확히 같은 방식으로 작동합니다. 그러나 hasAuthority()를 hasAnyAuthority("WRITE", "READ")로 바꾸면 두 권한 중 하나를 가진 사용자의 요청이 수락됩니다. 우리의 경우 애플리케이션은 John과 Jane 모두의 요청을 허용합니다. 다음 목록에서 hasAnyAuthority() 메서드를 적용하는 방법을 볼 수 있습니다.

목록 7.6 hasAnyAuthority() 메서드 적용하기
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
          .anyRequest()
            .hasAnyAuthority("WRITE", "READ"); ❶
  }
}
```
❶ Permits requests from users with both WRITE and READ authorities

You can successfully call the endpoint now with any of our two users. Here’s the call for John:
```sh
curl -u john:12345 http://localhost:8080/hello
```
The response body is
```
Hello!
```
And the call for Jane:
```sh
curl -u jane:12345 http://localhost:8080/hello
```
The response body is
```
Hello!
```
사용자 권한을 기반으로 액세스를 지정하는 세 번째 방법은 실제로 access() 메서드입니다. 그러나 access() 메서드가 더 일반적입니다. 권한 부여 조건을 지정하는 Spring 표현식(SpEL)을 매개변수로 수신합니다. 이 방법은 강력하며 당국에만 국한되지 않습니다. 그러나 이 방법은 또한 코드를 읽고 이해하기 어렵게 만듭니다. 이러한 이유로 이 섹션의 앞부분에서 설명한 hasAuthority() 또는 hasAnyAuthority() 메서드 중 하나를 적용할 수 없는 경우에만 마지막 옵션으로 권장합니다.

이 메서드를 더 쉽게 이해할 수 있도록 먼저 hasAuthority() 및 hasAnyAuthority() 메서드를 사용하여 권한을 지정하는 대신 이 메서드를 제시합니다. 이 예제에서 배운 것처럼 메서드에 대한 매개변수로 Spring 표현식을 제공해야 합니다. 우리가 정의한 권한 부여 규칙은 읽기가 더 어려워지므로 간단한 규칙에는 이 접근 방식을 권장하지 않습니다. 그러나 access() 메소드는 매개변수로 제공하는 표현식을 통해 규칙을 사용자 정의할 수 있다는 장점이 있습니다. 그리고 이것은 정말 강력합니다! SpEL 표현식과 마찬가지로 기본적으로 모든 조건을 정의할 수 있습니다.

> 참고 대부분의 경우 hasAuthority() 및 hasAnyAuthority() 메서드를 사용하여 필요한 제한을 구현할 수 있으며 이를 사용하는 것이 좋습니다. 다른 두 옵션이 적합하지 않고 보다 일반적인 권한 부여 규칙을 구현하려는 경우에만 access() 메서드를 사용하십시오.

이전의 경우와 동일한 요구 사항을 일치시키기 위해 간단한 예부터 시작합니다. 사용자에게 특정 권한이 있는지 테스트해야 하는 경우 access() 메서드와 함께 사용해야 하는 표현식은 다음 중 하나일 수 있습니다.

- hasAuthority('WRITE')--사용자가 엔드포인트를 호출하기 위해 WRITE 권한이 필요하다고 규정합니다.
- hasAnyAuthority('READ', 'WRITE')--사용자에게 READ 또는 WRITE 권한 중 하나가 필요하도록 지정합니다. 이 표현식을 사용하여 액세스를 허용하려는 모든 권한을 열거할 수 있습니다.

이러한 식의 이름은 이 섹션의 앞부분에서 설명한 메서드와 동일합니다. 다음 목록은 access() 메서드를 사용하는 방법을 보여줍니다.

목록 7.7 access() 메서드를 사용하여 끝점에 대한 액세스 구성
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
          .anyRequest()
            .access("hasAuthority('WRITE')"); ❶
  }
}
```
❶ WRITE 권한을 가진 사용자의 요청 승인

목록 7.7에 나와 있는 예제는 간단한 요구 사항에 사용하는 경우 access() 메서드가 구문을 복잡하게 만드는 방법을 보여줍니다. 이러한 경우 대신 hasAuthority() 또는 hasAnyAuthority() 메서드를 직접 사용해야 합니다. 그러나 access() 메서드가 모두 나쁜 것은 아닙니다. 앞서 언급했듯이 유연성을 제공합니다. 응용 프로그램이 액세스 권한을 부여하는 것을 기반으로 더 복잡한 표현식을 작성하는 데 사용할 수 있는 실제 시나리오의 상황을 찾을 수 있습니다. access() 메서드가 없으면 이러한 시나리오를 구현할 수 없습니다.

목록 7.8에서 다른 식으로 작성하기 쉽지 않은 표현식이 적용된 access() 메서드를 찾을 수 있습니다. 정확히는 목록 7.8에 제시된 구성은 다른 권한을 가진 두 명의 사용자 John과 Jane을 정의합니다. 사용자 John에게는 읽기 권한만 있고 Jane에게는 읽기, 쓰기 및 삭제 권한이 있습니다. 엔드포인트는 읽기 권한이 있는 사용자가 액세스할 수 있어야 하지만 삭제 권한이 있는 사용자는 액세스할 수 없습니다.

> **참고** Spring 앱에서는 명명 권한에 대한 다양한 스타일과 규칙을 찾을 수 있습니다. 일부 개발자는 모두 대문자를 사용하고 다른 개발자는 모두 소문자를 사용합니다. 제 생각에는 이러한 모든 선택이 앱에서 일관성을 유지하는 한 괜찮습니다. 이 책에서는 실제 시나리오에서 접할 수 있는 더 많은 접근 방식을 관찰할 수 있도록 예제에서 다양한 스타일을 사용합니다.

물론 가상의 예이지만 이해하기 쉬울 만큼 간단하고 access() 메서드가 더 강력한 이유를 증명할 만큼 복잡합니다. access() 메서드를 사용하여 이를 구현하려면 요구 사항을 반영하는 표현식을 사용할 수 있습니다. 예를 들어:
```java
"hasAuthority('read') and !hasAuthority('delete')"
```

The next listing illustrates how to apply the access() method with a more complex expression. You can find this example in the project named ssia-ch7-ex2.

Listing 7.8 pplying the access() method with a more complex expression
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public UserDetailsService userDetailsService() {
    var manager = new InMemoryUserDetailsManager();

    var user1 = User.withUsername("john")
            .password("12345")
            .authorities("read")
            .build();

    var user2 = User.withUsername("jane")
            .password("12345")
            .authorities("read", "write", "delete")
            .build();

    manager.createUser(user1);
    manager.createUser(user2);

    return manager;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Override
  protected void configure(HttpSecurity http) 
    throws Exception {

    http.httpBasic();

    String expression = 
           "hasAuthority('read') and ❶
           ➥ !hasAuthority('delete')"; ❶

    http.authorizeRequests()
         .anyRequest()
         .access(expression);
  }
}
```
❶ States that the user must have the authority read but not the authority delete

Let’s test our application now by calling the /hello endpoint for user John:
```json
curl -u john:12345 http://localhost:8080/hello
```
The body of the response is
```
Hello!
```
And calling the endpoint with user Jane:
```sh
curl -u jane:12345 http://localhost:8080/hello
```
The body of the response is
```json
{
    "status":403,
    "error":"Forbidden",
    "message":"Forbidden",
    "path":"/hello"
}
```
사용자 John은 읽기 권한만 갖고 엔드포인트를 성공적으로 호출할 수 있습니다. 그러나 Jane에게도 삭제 권한이 있으며 엔드포인트를 호출할 권한이 없습니다. Jane의 호출에 대한 HTTP 상태는 403 Forbidden입니다.

이러한 예를 통해 사용자가 지정된 일부 엔드포인트에 액세스하는 데 필요한 권한과 관련된 제약 조건을 설정하는 방법을 확인할 수 있습니다. 물론 경로나 HTTP 메서드를 기반으로 보안할 요청을 선택하는 것에 대해서는 아직 논의하지 않았습니다. 대신 우리는 애플리케이션에 의해 노출된 끝점에 관계없이 모든 요청에 ​​대해 규칙을 적용했습니다. 사용자 역할에 대해 동일한 구성을 마치면 권한 부여 구성을 적용할 끝점을 선택하는 방법을 논의합니다.

### 7.1.2 사용자 역할에 따라 모든 엔드포인트에 대한 액세스 제한

이 섹션에서는 역할을 기반으로 끝점에 대한 액세스를 제한하는 방법에 대해 설명합니다. 역할은 사용자가 할 수 있는 일을 나타내는 또 다른 방법입니다(그림 7.5). 실제 응용 프로그램에서도 이러한 항목을 찾을 수 있으므로 역할과 역할과 권한의 차이점을 이해하는 것이 중요합니다. 이 섹션에서는 애플리케이션이 역할을 사용하는 모든 실제 시나리오와 이러한 경우에 대한 구성을 작성하는 방법을 알 수 있도록 역할을 사용하는 몇 가지 예를 적용합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH07_F05_Spilca.png)

그림 7.5 역할은 세분화되어 있습니다. 특정 역할을 가진 각 사용자는 해당 역할이 부여한 작업만 수행할 수 있습니다. 이 철학을 권한 부여에 적용할 때 시스템에서 사용자의 목적에 따라 요청이 허용됩니다. 특정 역할을 가진 사용자만 특정 엔드포인트를 호출할 수 있습니다.

Spring Security는 권한을 우리가 제한을 적용하는 세분화된 권한으로 이해합니다. 역할은 사용자에게 배지와 같습니다. 이는 사용자에게 작업 그룹에 대한 권한을 부여합니다. 일부 응용 프로그램은 항상 특정 사용자에게 동일한 권한 그룹을 제공합니다. 애플리케이션에서 사용자가 읽기 권한만 가질 수 있거나 읽기, 쓰기 및 삭제 권한을 모두 가질 수 있다고 상상해 보십시오. 이 경우 읽기만 가능한 사용자에게는 READER라는 역할이 있고 다른 사용자에게는 ADMIN이라는 역할이 있다고 생각하는 것이 더 편할 수 있습니다. ADMIN 역할이 있다는 것은 애플리케이션이 읽기, 쓰기, 업데이트 및 삭제 권한을 부여한다는 의미입니다. 당신은 잠재적으로 더 많은 역할을 가질 수 있습니다. 예를 들어, 요청에서 읽기 및 쓰기만 허용된 사용자도 필요하다고 지정하는 경우 애플리케이션에 대해 MANAGER라는 세 번째 역할을 생성할 수 있습니다.

> **참고** 애플리케이션에서 역할이 있는 접근 방식을 사용할 때 더 이상 권한을 정의할 필요가 없습니다. 권한은 이 경우 개념으로 존재하며 구현 요구 사항에 나타날 수 있습니다. 그러나 응용 프로그램에서는 사용자에게 권한이 부여된 하나 이상의 작업을 처리하는 역할만 정의하면 됩니다.

역할에 부여하는 이름은 권한을 부여하는 이름과 같습니다. 우리는 권위와 비교할 때 역할이 거칠다고 말할 수 있습니다. 어쨌든 배후에서 역할은 Spring Security, GrantedAuthority에서 동일한 계약을 사용하여 표현됩니다. 역할을 정의할 때 이름은 ROLE_ 접두사로 시작해야 합니다. 구현 수준에서 이 접두사는 역할과 권한 간의 차이를 지정합니다. 프로젝트 ssia-ch7-ex3의 이 섹션에서 작업하는 예제를 찾을 수 있습니다. 다음 목록에서 이전 예제에 대한 변경 사항을 살펴보십시오.

Listing 7.9 Setting roles for users
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public UserDetailsService userDetailsService() {
    var manager = new InMemoryUserDetailsManager();

    var user1 = User.withUsername("john")
                    .password("12345")
                    .authorities("ROLE_ADMIN") ❶
                    .build();

    var user2 = User.withUsername("jane")
                    .password("12345")
                    .authorities("ROLE_MANAGER")
                    .build();

    manager.createUser(user1);
    manager.createUser(user2);

    return manager;
  }

  // Omitted code

}
```
❶ ROLE_ 접두어를 갖는 GrantedAuthority는 이제 역할을 나타냅니다.

사용자 역할에 대한 제약 조건을 설정하려면 다음 방법 중 하나를 사용할 수 있습니다.

- hasRole() - 응용 프로그램이 요청을 승인하는 역할 이름을 매개변수로 받습니다.

- hasAnyRole() - 응용 프로그램이 요청을 승인하는 역할 이름을 매개변수로 받습니다.

- access() - 애플리케이션이 요청을 승인하는 역할을 지정하기 위해 Spring 표현식을 사용합니다. 역할 측면에서 hasRole() 또는 hasAnyRole()을 SpEL 표현식으로 사용할 수 있습니다.

관찰한 바와 같이 이름은 섹션 7.1.1에 제시된 방법과 유사합니다. 우리는 이것을 같은 방식으로 사용하지만 권한 대신 역할에 대한 구성을 적용합니다. 내 권장 사항도 비슷합니다. 첫 번째 옵션으로 hasRole() 또는 hasAnyRole() 메서드를 사용하고 앞의 두 가지가 적용되지 않는 경우에만 access()를 사용하는 것으로 대체합니다. 다음 목록에서 현재 configure() 메서드가 어떻게 생겼는지 확인할 수 있습니다.

목록 7.10 관리자의 요청만 수락하도록 앱 구성
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {
  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
         .anyRequest().hasRole("ADMIN"); ❶
  }
}
```
❶ 이제 hasRole() 메서드는 엔드포인트에 대한 액세스가 허용되는 역할을 지정합니다. ROLE_ 접두사는 여기에 나타나지 않습니다.

> **참고** 중요한 점은 ROLE_ 접두사를 역할 선언에만 사용한다는 것입니다. 그러나 역할을 사용할 때는 이름으로만 수행합니다.

애플리케이션을 테스트할 때 사용자 John이 엔드포인트에 액세스할 수 있는 반면 Jane은 HTTP 403 Forbidden을 수신하는 것을 관찰해야 합니다. 사용자 John과 함께 엔드포인트를 호출하려면 다음을 사용하십시오.
```sh
curl -u john:12345 http://localhost:8080/hello
```
The response body is
```
Hello!
```
And to call the endpoint with user Jane, use
```sh
curl -u jane:12345 http://localhost:8080/hello
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
이 섹션의 예제에서와 같이 User 빌더 클래스로 사용자를 빌드할 때 roles() 메소드를 사용하여 역할을 지정합니다. 이 메서드는 GrantedAuthority 개체를 만들고 사용자가 제공한 이름에 ROLE_ 접두사를 자동으로 추가합니다.

> **참고** roles() 메서드에 제공하는 매개변수에 ROLE_ 접두사가 포함되지 않는지 확인하십시오. 해당 접두사가 실수로 role() 매개변수에 포함된 경우 메서드에서 예외가 발생합니다. 간단히 말해,authorities() 메서드를 사용할 때 ROLE_ 접두사를 포함하십시오. roles() 메서드를 사용할 때 ROLE_ 접두사를 포함하지 마십시오.

다음 목록에서 역할 기반 액세스를 설계할 때 authority() 메서드 대신 roles() 메서드를 사용하는 올바른 방법을 볼 수 있습니다.

Listing 7.11 roles() 메소드로 역할 설정하기
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public UserDetailsService userDetailsService() {
    var manager = new InMemoryUserDetailsManager();

    var user1 = User.withUsername("john")
                    .password("12345")
                    .roles("ADMIN") ❶
                    .build();

    var user2 = User.withUsername("jane")
                    .password("12345")
                    .roles("MANAGER")
                    .build();

    manager.createUser(user1);
    manager.createUser(user2);

        return manager;
  }

  // Omitted code
}
```
❶ roles() 메소드는 사용자의 역할을 지정합니다.

access() 메서드에 대한 추가 정보

섹션 7.1.1 및 7.1.2에서 access() 메서드를 사용하여 권한 및 역할을 참조하는 권한 부여 규칙을 적용하는 방법을 배웠습니다. 일반적으로 애플리케이션에서 권한 부여 제한은 권한 및 역할과 관련됩니다. 그러나 access() 메서드가 일반적이라는 것을 기억하는 것이 중요합니다. 내가 제시하는 예를 통해 권한과 역할에 적용하는 방법을 가르치는 데 중점을 두지만 실제로는 SpEL 표현식을 받습니다. 권한 및 역할과 관련될 필요는 없습니다.

간단한 예는 오후 12시 이후에만 허용되도록 끝점에 대한 액세스를 구성하는 것입니다. 이와 같은 문제를 해결하기 위해 다음 SpEL 표현식을 사용할 수 있습니다.
```자바
T(java.time.LocalTime).now().isAfter(T(java.time.LocalTime).of(12, 0))
```
SpEL 표현식에 대한 자세한 내용은 Spring Framework 문서를 참조하십시오.

https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#expressions

access() 메서드를 사용하면 기본적으로 모든 종류의 규칙을 구현할 수 있습니다. 가능성은 무한합니다. 응용 프로그램에서 우리는 항상 구문을 가능한 한 단순하게 유지하기 위해 노력한다는 것을 잊지 마십시오. 다른 선택이 없을 때만 구성을 복잡하게 만드십시오. 이 예제는 ssia-ch7-ex4 프로젝트에 적용되었습니다.

### 7.1.3 모든 엔드포인트에 대한 액세스 제한

이 섹션에서는 모든 요청에 ​​대한 액세스 제한에 대해 설명합니다. 5장에서 permitAll() 메서드를 사용하여 모든 요청에 ​​대한 액세스를 허용할 수 있음을 배웠습니다. 권한과 역할에 따라 액세스 규칙을 적용할 수 있다는 것도 배웠습니다. 하지만 당신이 할 수 있는 일은 모든 요청을 거부하는 것입니다. denyAll() 메서드는 permitAll() 메서드의 반대입니다. 다음 목록에서 denyAll() 메소드를 사용하는 방법을 볼 수 있습니다.

목록 7.12 denyAll() 메서드를 사용하여 끝점에 대한 액세스 제한
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {
  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
         .anyRequest().denyAll(); ❶
  }
}
```
❶ 모든 사람의 액세스를 제한하기 위해 denyAll()을 사용합니다.

그렇다면 그러한 제한을 어디에 사용할 수 있습니까? 다른 방법만큼 많이 사용되지는 않지만 요구 사항에 따라 필요한 경우가 있습니다. 이 점을 명확히 하기 위해 몇 가지 사례를 보여드리겠습니다.

이메일 주소를 경로 변수로 수신하는 엔드포인트가 있다고 가정해 보겠습니다. 원하는 것은 .com으로 끝나는 변수 주소 값을 가진 요청을 허용하는 것입니다. 

애플리케이션이 이메일 주소에 대해 다른 형식을 허용하는 것을 원하지 않습니다. (다음 섹션에서는 경로 및 HTTP 메서드를 기반으로 하는 요청 그룹과 경로 변수에 대해 제한을 적용하는 방법을 배우게 됩니다.) 이 요구 사항의 경우 정규식을 사용하여 규칙과 일치하는 요청을 그룹화한 다음 denyAll() 메소드를 사용하여 애플리케이션이 이러한 모든 요청을 거부하도록 지시하십시오(그림 7.6).
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH07_F06_Spilca.png)

그림 7.6 사용자가 .com으로 끝나는 매개변수 값으로 끝점을 호출하면 응용 프로그램이 요청을 수락합니다. 사용자가 엔드포인트에 전화를 걸어 .net으로 끝나는 이메일 주소를 제공하면 애플리케이션이 호출을 거부합니다. 이러한 동작을 수행하려면 매개 변수 값이 .com으로 끝나지 않는 모든 끝점에 대해 denyAll() 메서드를 사용할 수 있습니다.

그림 7.7과 같이 설계된 응용 프로그램을 상상할 수도 있습니다. 몇몇 서비스는 다른 경로에서 사용 가능한 엔드포인트를 호출하여 액세스할 수 있는 애플리케이션의 사용 사례를 구현합니다. 그러나 엔드포인트를 호출하기 위해 클라이언트는 게이트웨이를 호출할 수 있는 다른 서비스를 요청합니다. 이 아키텍처에는 이 유형의 두 가지 개별 서비스가 있습니다. 그림 7.7에서 게이트웨이 A와 게이트웨이 B를 호출했습니다. 클라이언트는 /products 경로에 액세스하려는 경우 게이트웨이 A를 요청합니다. 

그러나 /articles 경로의 경우 클라이언트는 게이트웨이 B를 요청해야 합니다. 각 게이트웨이 서비스는 서비스를 제공하지 않는 다른 경로에 대한 모든 요청을 거부하도록 설계되었습니다. 이 단순화된 시나리오는 denyAll() 메서드를 쉽게 이해하는 데 도움이 될 수 있습니다. 프로덕션 애플리케이션에서는 더 복잡한 아키텍처에서 유사한 사례를 찾을 수 있습니다.

프로덕션의 애플리케이션은 때때로 이상하게 보일 수 있는 다양한 아키텍처 요구 사항에 직면합니다. 프레임워크는 발생할 수 있는 모든 상황에 대해 필요한 유연성을 허용해야 합니다. 이러한 이유로, denyAll() 메서드는 이 장에서 배운 다른 모든 옵션만큼 중요합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH07_F07_Spilca.png)

그림 7.7 게이트웨이 A와 게이트웨이 B를 통한 접근. 각 게이트웨이는 특정 경로에 대한 요청만 전달하고 나머지는 거부한다.

## 요약

- 승인은 인증된 요청이 허용되는지 여부를 애플리케이션이 결정하는 프로세스입니다. 권한 부여는 항상 인증 후에 발생합니다.

- 인증된 사용자의 권한 및 역할을 기반으로 애플리케이션이 요청을 승인하는 방법을 구성합니다.

- 애플리케이션에서 인증되지 않은 사용자에 대해 특정 요청이 가능하도록 지정할 수도 있습니다.

- denyAll() 메서드를 사용하여 모든 요청을 거부하거나 permitAll() 메서드를 사용하여 모든 요청을 허용하도록 앱을 구성할 수 있습니다.