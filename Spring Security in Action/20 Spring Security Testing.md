# 20 스프링 시큐리티 테스팅

이 장에서는 다음을 다룹니다.

- 엔드포인트에 대한 Spring Security 구성과의 통합 테스트

- 테스트를 위한 모의 사용자 정의

- 메소드 레벨 보안을 위한 Spring Security와의 통합 테스트

- 반응형 Spring 구현 테스트
  
전설에 따르면 쓰기 단위 및 통합 테스트는 짧은 구절로 시작되었습니다.

"코드에 있는 99개의 작은 버그, 99개의 작은 버그.
하나를 추적하고 주변을 패치하고,
코드에는 113개의 작은 버그가 있습니다."

-- 익명

시간이 지남에 따라 소프트웨어는 더 복잡해지고 팀은 더 커졌습니다. 시간이 지나면서 다른 사람들이 구현한 모든 기능을 아는 것은 불가능해졌습니다. 개발자는 버그를 수정하거나 새로운 기능을 구현하는 동안 기존 기능이 손상되지 않도록 하는 방법이 필요했습니다.

애플리케이션을 개발하는 동안 우리는 구현한 기능이 원하는 대로 작동하는지 검증하기 위해 지속적으로 테스트를 작성합니다. 우리가 단위 및 통합 테스트를 작성하는 주된 이유는 버그를 수정하거나 새로운 기능을 구현하기 위해 코드를 변경할 때 기존 기능이 손상되지 않도록 하기 위해서입니다. 이를 회귀 테스트라고도 합니다.

요즘은 개발자가 변경 작업을 마치면 팀에서 코드 버전 관리를 관리하는 데 사용하는 서버에 변경 사항을 업로드합니다. 이 작업은 모든 기존 테스트를 실행하는 지속적 통합 도구를 자동으로 트리거합니다. 변경 사항이 기존 기능을 손상시키는 경우 테스트가 실패하고 지속적인 통합 도구가 팀에 알립니다(그림 20.1). 이렇게 하면 기존 기능에 영향을 주는 변경 사항을 전달할 가능성이 줄어듭니다.
 
그림 20.1 테스트는 개발 프로세스의 일부입니다. 개발자가 코드를 업로드할 때마다 테스트가 실행됩니다. 테스트가 실패하면 지속적 통합 도구가 개발자에게 알립니다.

> **참고** 이 그림에서 Jenkins를 사용하여 이것이 유일하게 사용된 지속적 통합 도구이거나 최고의 도구라고 말하지 않습니다. Bamboo, GitLab CI, CircleCI 등과 같이 선택할 수 있는 많은 대안이 있습니다.

애플리케이션을 테스트할 때 테스트해야 하는 것은 애플리케이션 코드만이 아니라는 점을 기억해야 합니다. 또한 다음과의 통합을 테스트해야 합니다.
사용하는 프레임워크 및 라이브러리도 포함됩니다(그림 20.2). 미래에 해당 프레임워크나 라이브러리를 새 버전으로 업그레이드할 수 있습니다. 종속성 버전을 변경할 때 앱이 해당 종속성의 새 버전과 여전히 잘 통합되는지 확인하고 싶습니다. 앱이 동일한 방식으로 통합되지 않는 경우 통합 문제를 수정하기 위해 변경해야 할 부분을 쉽게 찾고 싶습니다.
 
그림 20.2 응용 프로그램의 기능은 많은 종속성에 의존합니다. 종속성을 업그레이드하거나 변경하면 기존 기능에 영향을 미칠 수 있습니다. 종속성이 있는 통합 테스트를 사용하면 종속성 변경이 애플리케이션의 기존 기능에 영향을 미치는지 빠르게 발견하는 데 도움이 됩니다.

그렇기 때문에 이 장에서 다룰 내용, 즉 앱과 Spring Security의 통합을 테스트하는 방법을 알아야 합니다. 일반적으로 Spring 프레임워크 생태계와 마찬가지로 Spring Security는 빠르게 진화합니다. 앱을 새 버전으로 업그레이드하고 특정 버전으로 업그레이드하면 애플리케이션에 취약점, 오류 또는 비호환성이 발생하는지 확실히 알고 싶을 것입니다. 첫 번째 장에서 바로 논의한 내용을 기억하십시오. 앱의 첫 번째 디자인부터 보안을 고려해야 하며 심각하게 받아들여야 합니다. 보안 구성에 대한 테스트 구현은 필수 작업이어야 하며 "완료" 정의의 일부로 정의되어야 합니다. 보안 테스트가 준비되지 않은 경우 작업이 완료된 것으로 간주해서는 안 됩니다.

이 장에서는 앱과 Spring Security의 통합을 테스트하기 위한 몇 가지 사례를 논의할 것입니다. 이전 장에서 작업한 몇 가지 예제로 돌아가서 구현된 기능에 대한 통합 테스트를 작성하는 방법을 배우게 됩니다. 일반적으로 테스트는 장대한 이야기입니다. 그러나 이 주제를 자세히 배우면 많은 이점이 있습니다.
이 장에서는 애플리케이션과 Spring Security 간의 통합 테스트에 중점을 둘 것입니다. 예제를 시작하기 전에 이 주제를 깊이 이해하는 데 도움이 되는 몇 가지 리소스를 추천하고 싶습니다. 주제를 더 자세히 이해해야 하거나 복습을 위해 이 책을 읽을 수 있습니다. 나는 당신이 이것들을 훌륭하게 찾을 것이라고 확신합니다!

- JUnit in Action, 3rd ed. by Cătălin Tudose et al. (Manning, 2020)
- Unit Testing Principles, Practices, and Patterns by Vladimir Khorikov (Manning, 2020)
- Testing Java Microservices by Alex Soto Bueno et al. (Manning, 2018)

구현한 보안 코드의 테스트의 시작은 인증 설정 테스트로 시작한다. 20.1절에서는 인증을 스킵하고 엔드포인트 레벨에서 인증 설정을 테스트하기 위한 모형 사용자를 정의하는 방법을 배울 것이다. 20.2절은 여러분이 구현한 특정한 인증 객체를 사용할 필요가 있는 경우에서 보안 컨텍스트 전체를 설정하는 방법을 다룰 것이다. 마지막으로 20.4절에서는 앞 절에 배운 인증 설정 테스트를 메소드 보안에 적용해 볼 것이다.

인증 테스트를 다루었다면 20.5절은 인증 흐름도를 테스트하는 방법을 다룬다. 그리고 CSRF와 CORS와 같은 또 다른 보안 설정을 테스트하는 방법을 다룬다. 끝으로 스프링 시큐리티와 리액티브 앱의 통합 테스트를 다룬다.

## 20.1 Using mock users for tests

모의 사용자로 인증 설정을 테스트 한다. 이 방식이 가장 단순하며 자주 사용되는 방법이다. 모의 사용자를 사용할 때 테스트는 인증 절차를 생략한다. 모의 사용자는 테스트할때만 유효하며 이 사용자에 대해서 특정 시나리오를 검증하기 위해 필요한 특성을 설정할 수 있다. 예를 들어 사용자에게 ADMIN, MANAGER 등과 같은 역할을 부여하거나 다른 권한들을 사용하여 앱이 이러한 조건에서 예상대로 행동하는지를 검증한다.
 
그림 20.3 테스트를 실행할 때 Spring Security 인증 흐름에서 음영 처리된 구성 요소를 건너뜁니다. 테스트는 테스트된 기능을 호출하기 위해 정의한 모의 사용자가 포함된 모의 SecurityContext를 직접 사용합니다.

> **참고** 프레임워크의 어떤 구성 요소가 통합 테스트와 관련되어 있는지 아는 것이 중요합니다. 이렇게 하면 테스트에서 다루는 통합 부분을 알 수 있습니다. 예를 들어, 모의 사용자는 승인을 커버하는 데만 사용할 수 있습니다. (섹션 20.5에서 인증을 처리하는 방법을 배우게 될 것입니다.) 저는 때때로 개발자들이 이 측면에서 혼란스러워하는 것을 봅니다. 그들은 예를 들어 모의 사용자와 작업할 때 AuthenticationProvider의 사용자 정의 구현을 다루고 있다고 생각했지만 그렇지 않습니다. 테스트 중인 내용을 올바르게 이해했는지 확인하세요.

이러한 테스트를 작성하는 방법을 증명하기 위해 이 책에서 작업한 가장 간단한 예제인 ssia-ch2-ex1 프로젝트로 돌아가 보겠습니다. 이 프로젝트는 기본 Spring Security 구성만 사용하여 /hello 경로에 대한 엔드포인트를 노출합니다. 우리는 무슨 일이 일어날 것으로 예상합니까?

- 사용자 없이 엔드포인트를 호출할 때 HTTP 응답 상태는 401 Unauthorized여야 합니다.

- 인증된 사용자가 있는 엔드포인트를 호출할 때 HTTP 응답 상태는 200 OK여야 하고 응답 본문은 Hello!여야 합니다.
  
이 두 가지 시나리오를 테스트해 봅시다! 테스트를 작성하려면 pom.xml 파일에 몇 가지 종속성이 필요합니다. 다음 코드 조각은 이 장의 예제 전체에서 사용하는 클래스를 보여줍니다. 테스트 작성을 시작하기 전에 pom.xml 파일에 이러한 파일이 있는지 확인해야 합니다. 종속성은 다음과 같습니다.

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-test</artifactId>
   <scope>test</scope>
   <exclusions>
      <exclusion>
         <groupId>org.junit.vintage</groupId>
         <artifactId>junit-vintage-engine</artifactId>
      </exclusion>
   </exclusions>
</dependency>
<dependency>
   <groupId>org.springframework.security</groupId>
   <artifactId>spring-security-test</artifactId>
   <scope>test</scope>
</dependency>
```

> **참고** 이 장의 예제에서는 테스트를 작성하기 위해 JUnit 5를 사용합니다. Spring Security 통합의 관점에서 볼 때 학습할 주석과 나머지 클래스는 동일하게 작동합니다. Cătălin Tudose et al.의 JUnit 실행 중 4장. JUnit 4에서 JUnit 5로의 마이그레이션에 대한 전용 토론인 (Manning, 2020)에는 버전 4와 5의 클래스와 주석 간의 대응 관계를 보여주는 몇 가지 흥미로운 표가 포함되어 있습니다. 링크는 https://livebook.manning입니다. com/book/junit-in-action-third-edition/chapter-4.

Spring Boot Maven 프로젝트의 테스트 폴더에 MainTests라는 클래스를 추가합니다. 우리는 이 클래스를 응용 프로그램의 기본 패키지의 일부로 작성합니다. 기본 패키지의 이름은 com.laurentiuspilca.ssia입니다. 목록 20.1에서 테스트를 위한 빈 클래스의 정의를 찾을 수 있습니다. 테스트 스위트의 Spring 컨텍스트를 관리하는 편리한 방법을 나타내는 @SpringBootTest 주석을 사용합니다. 

Listing 20.1 A class for writing the tests
```java
@SpringBootTest          ❶
public class MainTests {
}
```
❶ Makes Spring Boot responsible for managing the Spring context for the tests

스프링의 MockMvc를 사용하면 엔드포인트의 행동의 테스트를 편리하게 구현할 수 있다. 

Listing 20.2 Adding MockMvc for implementing test scenarios
```java
@SpringBootTest
@AutoConfigureMockMvc       ❶
public class MainTests {
  @Autowired
  private MockMvc mvc;      ❷
}
```
❶ Enables Spring Boot to autoconfigure MockMvc. As a consequence, an object of type MockMvc is added to the Spring context.

❷ Injects the MockMvc object that we use to test the endpoint

이제 엔드포인트 행동을 테스트할 수 있는 수단이 생겼으므로 첫번째 시나리오를 시작해보자. 사용자 인증없이 /hello 엔드포인트를 부르면 HTTP 응답 상태는 401 Unauthorized이어야 한다. 

아래 그림에 이 테스트를 실행하는 컴포넌트들 간의 관계를 볼 수 있다. 테스트는 모의 SecurityContext를 사용하여 엔드포인트를 부른다. 우리는 이 SecurityContet에 뭔가를 추가하기로 했다. 이 테스트는 누군가 인증 없이 엔드포인트를 부르는 상황을 나타내는 사용자를 추가하지 않으면 앱이 401 Unauthorized 응답을 하는지를 확인해야한다. SecurityContext에 사용자를 추가하면, 앱이 호출을 수락하며 HTTP 응답은 200 OK이다.
 
Figure 20.4 테스트를 실행할 때 인증은 건너뛴다. 테스트는 모의 SecurityContext를 사용해서 /hello 엔드포인트를 부른다. SecurityContext에 모의 사용자를 추가하여 행동을 검증하는 것이 인증 규칙에 따라 정확하다. 모의 사용자를 정의하지 않으면 앱은 호출을 인증하지 않지만 사용자를 추가하면 호출이 성공한다.

다음 목록은 이 시나리오의 구현을 나타냅니다.

목록 20.3 인증된 사용자 없이 엔드포인트를 호출할 수 없는지 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void helloUnauthenticated() throws Exception {
    mvc.perform(get("/hello"))  ❶
         .andExpect(status().isUnauthorized()); 
  }
}
```
❶ /hello 경로에 대해 GET 요청을 수행할 때 Unauthorized 상태의 응답을 받을 것으로 예상합니다.

get() 및 status() 메서드를 정적으로 가져옵니다. 이 클래스의 이 장의 예제에서 사용하는 요청과 관련된 get() 메서드 및 유사한 메서드를 찾을 수 있습니다.

```java
org.springframework.test.web.servlet.request.MockMvcRequestBuilders
```
또한 이 클래스에서 이 장의 다음 예제에서 사용하는 호출 결과와 관련된 status() 메서드 및 유사한 메서드를 찾을 수 있습니다.
```java
org.springframework.test.web.servlet.result.MockMvcResultMatchers
```
이제 테스트를 실행하여 그 결과를 볼 수 있다. 성공하면 녹색으로 실패하면 빨강으로 표시할 것이다.

> **참고** 제공된 프로젝트에서 테스트를 구현하는 각 메서드 위에 `@DisplayName` 주석도 사용합니다. 이 주석을 사용하면 테스트 시나리오에 대한 더 길고 자세한 설명을 얻을 수 있습니다. 공간을 덜 차지하고 우리가 논의하는 테스트의 기능에 집중할 수 있도록 책의 목록에서 `@DisplayName` 주석을 제거했습니다.

두번째 시나리오는 모의 사용자가 필요하다. 승인된 사용자가 /hello를 부르는 것을 검증하기 위해서 @WithMockUser 주석을 사용한다. 테스트 메소드 위에 이 주석을 달면 SecurityContext에 UserDetails 인스턴스가 설정되도록 한다. 이것은 기본적으로 인증을 생략한다. 이제 엔드포인트를 부르면 @WithMockUser로 정의된 사용자가 인증된 것처럼 행동한다. 

이 예제에서는 모의 사용자의 이름, 역할 또는 권한과 같은 정보에 대해서는 관심이 없다. 따라서 @WithMockUser 주석을 추가하면 모의 사용자의 속성에 디폴트 값이 적용된다. 

Listing 20.4 Using @WithMockUser to define a mock authenticated user
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  // Omitted code

  @Test 
  @WithMockUser ❶
  public void helloAuthenticated() throws Exception {
    mvc.perform(get("/hello"))  ❷
         .andExpect(content().string("Hello!"))
         .andExpect(status().isOk());
  }

}
```
❶ Calls the method with a mock authenticated user

❷ In this case, when performing a GET request for the /hello path, we expect the response status to be OK.

어떤 상황에서는 특정한 사용자 이름이나 역할을 검증해야 하나. 이번에는 인증된 사용자 이름에 따라 응답하는 테스트를 작성한다.(ssia-ch5-ex2)

Listing 20.5 Configuring details for the mock user
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {
     
  // Omitted code

  @Test
  @WithMockUser(username = "mary")                     ❶
  public void helloAuthenticated() throws Exception {
    mvc.perform(get("/hello"))
         .andExpect(content().string("Hello, mary!"))
         .andExpect(status().isOk());
    }
}
```
❶ Sets up a username for the mock user
아래 그림은 주석을 사용한 테스트 환경 정의와 RequestPostProcessor를 사용할 때와의 차이를 보여준다. 프레임워크는 테스트 메소드를 수행하기 전에 @WithMockUser를 해석한다. RequestPostProcessor를 사용할 경우 프레임워크는 먼저 테스트 메소드를 호출한 다음 테스트 요청을 빌드한다.

사용자 이름을 설정하듯이 권한과 역할도 설정할 수 있다. RequestPostProcessor로도 모의 사용자를 만들 수 있다. RequestPostProcessor에 with() 메소드를 제공할 수 있다. SecurityMockMvcRequestPostProcessors클래스는 수많은 RequestPostProcessor 구현체를 제공한다. SecurityMockMvcRequestPostProcessors 클래스의 user() 메소드가 RequestPostProcessor를 리턴한다.
 
Figure 20.5 시큐리티 테스트 환경을 만들 때 주석을 사용할 때와 RequestPostProcessor를 사용할 때의 차이. 주석을 사용하면 프레임워크가 먼저 테스트 시큐리티 환경을 설정하다. RequestPostProcessor를 사용하면 테스트 요청을 만든 다음 테스트 환경과 같은 다른 제약을 정의하기 위해 변경된다. 프레임워크가 테스트 환경을 적용하는 부분을 음영 처리.

Listing 20.6 Using a RequestPostProcessor to define a mock user
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  // Omitted code

  @Test
  public void helloAuthenticatedWithUser() throws Exception {
    mvc.perform(
          get("/hello")
            .with(user("mary")))                 ❶
        .andExpect(content().string("Hello!"))
        .andExpect(status().isOk());
  }
}
```
❶ Calls the /hello endpoint using a mock user with the username Mary

앱의 기능과 스프링 시큐리티 통합을 위해 우리가 작성하는 대부분의 테스트는 인증 설정을 위한 것이다. 아마도 왜 권한은 테스트 하지 않는지 의아할 것이다. 20.5절에서 권한 테스트를 다룬다. 하지만 일반적으로 인증과 권한은 별도로 테스트하는 것이 좋다. 일반적으로 앱에는 사용자를 인증하는 한가지 방법이 있지만 권한이 다르게 구성된 수십개의 엔드포인트를 노출할 수 있다. 따라서 몇가지 테스트를 통해 개별적으로 인증을 테스트 한 다음에 엔드포인트의 각 인증 설정에 대해 구현하는 것이다. 로직이 바뀌지 않는다면 테스트된 각 엔트 포인트에 매번 인증을 반복하는 것은 시간 낭비이다.


# 20.2 Testing with users from a UserDetailsService

여기서는 UserDetailsService로부터 테스트를 위한 사용자 정보를 얻는 것을 다룬다. 가짜인 모의 사용자와는 달리 UserDetailsService에서 사용자를 가져온다. 앱이 사용자 정보를 로드하는 데이터 소스와 통합 테스트를 하려면 이 방법을 사용한다.
 
Figure 20.6 Instead of creating a mock user for the test when building the SecurityContext used by the test, we take the user details from a UserDetailsService. This way, you can test authorization using real users taken from a data source. During the test, the flow of execution skips the shaded components.

To demonstrate this approach, let’s open project ssia-ch2-ex2 and implement the tests for the endpoint exposed at the /hello path. We use the UserDetailsService bean that the project already adds to the context. Note that, with this approach, we need to have a UserDetailsService bean in the context. To specify the user we authenticate from this UserDetailsService, we annotate the test method with @WithUserDetails. With the @WithUserDetails annotation, to find the user, you specify the username. The following listing presents the implementation of the test for the /hello endpoint using the @WithUserDetails annotation to define the authenticated user.

Listing 20.7 Defining the authenticated user with the @WithUserDetails annotation
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  @WithUserDetails("john")                              ❶
  public void helloAuthenticated() throws Exception {
    mvc.perform(get("/hello"))
        .andExpect(status().isOk());
  }
        
}
```
❶ 테스트 시나리오를 실행하기 위해 UserDetailsService를 사용하여 사용자 John을 로드합니다.

## 20.3 테스트를 위해 사용자 정의 인증 개체 사용

일반적으로 테스트를 위해 모의 사용자를 사용할 때 프레임워크가 SecurityContext에서 인증 인스턴스를 생성하는 데 사용하는 클래스는 신경 쓰지 않습니다. 그러나 컨트롤러에 개체 유형에 따라 달라지는 일부 논리가 있다고 가정해 보겠습니다. 특정 유형을 사용하여 테스트용 인증 개체를 생성하도록 프레임워크에 지시할 수 있습니까? 대답은 예이며 이것이 이 섹션에서 논의하는 내용입니다.

이 접근 방식의 논리는 간단합니다. SecurityContext 구축을 담당하는 팩토리 클래스를 정의합니다. 이런 식으로 내부 내용을 포함하여 테스트용 SecurityContext가 구축되는 방식을 완전히 제어할 수 있습니다(그림 20.7). 예를 들어 사용자 지정 인증 개체를 갖도록 선택할 수 있습니다.
 
그림 20.7 테스트를 위한 SecurityContext가 어떻게 정의되는지에 대한 완전한 제어를 얻기 위해, 우리는 SecurityContext를 구축하는 방법에 대해 테스트에 지시하는 팩토리 클래스를 구축합니다. 이렇게 하면 유연성이 향상되고 인증 개체로 사용할 개체의 종류와 같은 세부 정보를 선택할 수 있습니다. 그림에서 테스트 중 흐름에서 건너뛴 구성 요소를 음영 처리했습니다.

ssia-ch2-ex5 프로젝트를 열고 모의 SecurityContext를 구성하고 프레임워크에 인증 개체를 생성하는 방법을 지시하는 테스트를 작성해 보겠습니다. 이 예제에서 기억해야 할 흥미로운 측면은 사용자 지정 AuthenticationProvider의 구현을 증명하는 데 사용한다는 것입니다. 우리가 구현한 사용자 정의 AuthenticationProvider는 John이라는 사용자만 인증합니다. 그러나 섹션 20.1 및 20.2에서 논의한 다른 두 가지 이전 접근 방식과 마찬가지로 현재 접근 방식은 인증을 건너뜁니다. 이러한 이유로 예제의 끝 부분에서 실제로 모의 사용자에게 어떤 이름이든 지정할 수 있음을 알 수 있습니다. 우리는 이 동작을 달성하기 위해 세 단계를 따릅니다(그림 20.8).

1.	Write an annotation to use over the test similarly to the way we use @WithMockUser or @WithUserDetails.
2.	Write a class that implements the WithSecurityContextFactory interface. This class implements the createSecurityContext() method that returns the mock SecurityContext the framework uses for the test.
3.	Link the custom annotation created in step 1 with the factory class created in step 2 via the @WithSecurityContext annotation.
 
Figure 20.8 To enable the test to use a custom SecurityContext, you need to follow the three steps illustrated in this figure.

#### STEP 1: DEFINING A CUSTOM ANNOTATION

In listing 20.8, you find the definition of the custom annotation we define for the test, named @WithCustomUser. As properties of the annotation, you can define whatever details you need to create the mock Authentication object. I added only the username here for my demonstration. Also, don’t forget to use the annotation @Retention (RetentionPolicy.RUNTIME) to set the retention policy to runtime. Spring needs to read this annotation using Java reflection at runtime. To allow Spring to read this annotation, you need to change its retention policy to RetentionPolicy.RUNTIME.

Listing 20.8 Defining the @WithCustomUser annotation
```java
@Retention(RetentionPolicy.RUNTIME)
public @interface WithCustomUser {

  String username();
}
```
2단계: 모의 보안 컨텍스트를 위한 팩토리 클래스 생성

두 번째 단계는 프레임워크가 테스트 실행에 사용하는 SecurityContext를 빌드하는 코드를 구현하는 것입니다. 여기에서 테스트에 사용할 인증 종류를 결정합니다. 다음 목록은 팩토리 클래스의 구현을 보여줍니다.

목록 20.9 SecurityContext에 대한 팩토리 구현
```java
public class CustomSecurityContextFactory                  ❶
  implements WithSecurityContextFactory<WithCustomUser> {

  @Override                                                ❷
  public SecurityContext createSecurityContext(
    WithCustomUser withCustomUser) {
      SecurityContext context =                            ❸
        SecurityContextHolder.createEmptyContext();

      var a = new UsernamePasswordAuthenticationToken(
        withCustomUser.username(), null, null);            ❹

      context.setAuthentication(a);                        ❺

      return context;
    }
}
```
❶ WithSecurityContextFactory 주석을 구현하고 테스트에 사용할 사용자 정의 주석을 지정합니다.

❷ 테스트를 위한 SecurityContext 생성 방법을 정의하기 위해 createSecurityContext() 구현

❸ 빈 보안 컨텍스트 구축

❺ SecurityContext에 모의 인증 추가

3단계: 공장 클래스에 사용자 지정 주석 연결

@WithSecurityContext 주석을 사용하여 이제 1단계에서 생성한 사용자 정의 주석을 2단계에서 구현한 SecurityContext의 팩토리 클래스에 연결합니다. 다음 목록은 이를 SecurityContext 팩토리 클래스에 연결하기 위한 @WithCustomUser 주석의 변경 사항을 보여줍니다.

목록 20.10 사용자 정의 주석을 SecurityContext 팩토리 클래스에 연결하기

```java
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomSecurityContextFactory.class)
public @interface WithCustomUser {

    String username();
}
```
With this setup complete, we can write a test to use the custom SecurityContext. The next listing defines the test.

Listing 20.11 Writing a test that uses the custom SecurityContext
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  @WithCustomUser(username = "mary")                   ❶
  public void helloAuthenticated() throws Exception {
    mvc.perform(get("/hello"))
         .andExpect(status().isOk());
  }
}
```
❶ 사용자 이름이 "mary"인 사용자로 테스트를 실행합니다.

테스트를 실행하면 성공적인 결과가 관찰됩니다. "잠깐! 이 예에서는 John이라는 사용자만 인증하는 사용자 지정 AuthenticationProvider를 구현했습니다. 사용자 이름 Mary로 어떻게 테스트에 성공할 수 있었습니까?” @WithMockUser 및 @WithUserDetails의 경우와 마찬가지로 이 방법을 사용하면 인증 논리를 건너뜁니다. 따라서 권한 부여와 관련된 내용을 테스트하는 데만 사용할 수 있습니다.

# 20.4 테스트 방법 보안

이 섹션에서는 테스트 메서드 보안에 대해 설명합니다. 지금까지 이 장에서 작성한 모든 테스트는 끝점을 참조합니다. 하지만 애플리케이션에 엔드포인트가 없다면 어떻게 될까요? 사실, 웹 앱이 아니라면 엔드포인트가 전혀 없습니다! 그러나 16장과 17장에서 논의한 것처럼 전역 메서드 보안과 함께 Spring Security를 ​​사용했을 수 있습니다. 이러한 시나리오에서는 여전히 보안 구성을 테스트해야 합니다.

다행히도 이전 섹션에서 논의한 것과 동일한 접근 방식을 사용하여 이 작업을 수행합니다. 여전히 @WithMockUser, @WithUserDetails 또는 사용자 정의 주석을 사용하여 고유한 SecurityContext를 정의할 수 있습니다. 그러나 MockMvc를 사용하는 대신 테스트해야 하는 메서드를 정의하는 빈 컨텍스트에서 직접 주입합니다.

ssia-ch16-ex1 프로젝트를 열고 NameService 클래스의 getName() 메서드에 대한 테스트를 구현해 보겠습니다. @PreAuthorize 주석을 사용하여 getName() 메서드를 보호했습니다. 목록 20.12에서 세 가지 테스트가 있는 테스트 클래스의 구현을 찾을 수 있으며 그림 20.9는 우리가 테스트하는 세 가지 시나리오를 그래픽으로 나타냅니다.

1. 인증된 사용자 없이 메서드를 호출하면 메서드에서 AuthenticationException을 throw해야 합니다.

2. 예상한 것과 다른 권한(쓰기)을 가진 인증된 사용자로 메서드를 호출하면 메서드에서 AccessDeniedException을 throw해야 합니다.

3. 예상 권한이 있는 인증된 사용자로 메서드를 호출하면 예상 결과가 반환됩니다.
 
그림 20.9 테스트된 시나리오. HTTP 요청이 인증되지 않은 경우 예상 결과는 AuthenticationException입니다. HTTP 요청이 인증되었지만 사용자에게 예상되는 권한이 없는 경우 예상되는 결과는 AccessDeniedException입니다. 인증된 사용자에게 예상되는 권한이 있으면 호출이 성공한 것입니다.

목록 20.12 세 가지 테스트의 구현
getName() 메서드에 대한 시나리오
```java
@SpringBootTest
class MainTests {

  @Autowired
  private NameService nameService;

  @Test
  void testNameServiceWithNoUser() {
    assertThrows(AuthenticationException.class,
            () -> nameService.getName());
  }

  @Test
  @WithMockUser(authorities = "read")
  void testNameServiceWithUserButWrongAuthority() {
    assertThrows(AccessDeniedException.class,
            () -> nameService.getName());
  }

  @Test
  @WithMockUser(authorities = "write")
  void testNameServiceWithUserButCorrectAuthority() {
    var result = nameService.getName();

    assertEquals("Fantastico", result);
  }
}
```
끝점을 호출할 필요가 없기 때문에 더 이상 MockMvc를 구성하지 않습니다. 대신 테스트된 메서드를 호출하기 위해 NameService 인스턴스를 직접 주입합니다. 섹션 20.1에서 논의한 것처럼 @WithMockUser 주석을 사용합니다. 유사하게, 섹션 20.2에서 논의한 것처럼 @WithUserDetails를 사용하거나 섹션 20.3에서 논의된 대로 SecurityContext를 빌드하는 사용자 정의 방법을 설계할 수 있습니다.

## 20.5 인증 테스트

이 섹션에서는 인증 테스트에 대해 설명합니다. 이전에 이 장에서 모의 ​​사용자를 정의하고 인증 구성을 테스트하는 방법을 배웠습니다. 그러나 인증은 어떻습니까? 인증 로직도 테스트할 수 있습니까? 예를 들어 인증을 위해 구현된 사용자 지정 논리가 있고 전체 흐름이 작동하는지 확인하려는 경우 이 작업을 수행해야 합니다. 인증을 테스트할 때 테스트 구현 요청은 그림 20.10과 같이 일반 클라이언트 요청처럼 작동합니다.
 
그림 20.10 인증을 테스트할 때 테스트는 클라이언트 역할을 하며 책 전체에서 논의된 전체 Spring Security 흐름을 거칩니다. 이 방법으로 예를 들어 사용자 지정 AuthenticationProvider 개체를 테스트할 수도 있습니다.
예를 들어 프로젝트 ssia-ch2-ex5로 돌아가서 구현한 사용자 지정 인증 공급자가 올바르게 작동하고 테스트를 통해 보안을 유지하는지 증명할 수 있습니까? 이 프로젝트에서 우리는 사용자 정의 AuthenticationProvider를 구현했으며 테스트와 함께 이 사용자 정의 인증 논리를 보호하고 싶습니다. 예, 인증 로직도 테스트할 수 있습니다.
우리가 구현하는 논리는 간단합니다. 사용자 이름 "john"과 암호 "12345"의 한 가지 자격 증명 세트만 허용됩니다. 유효한 자격 증명을 사용할 때 호출이 성공하는 반면 다른 자격 증명을 사용할 때는 HTTP 응답 상태가 401 Unauthorized임을 증명해야 합니다. 다시 ssia-ch2-ex5 프로젝트를 열고 인증이 올바르게 작동하는지 확인하기 위해 몇 가지 테스트를 구현해 보겠습니다.

Listing 20.13 httpBasic() RequestPostProcessor로 인증 테스트하기
```java
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void helloAuthenticatingWithValidUser() throws Exception {
    mvc.perform(
       get("/hello")
         .with(httpBasic("john","12345")))      ❶
         .andExpect(status().isOk());
  }

  @Test
  public void helloAuthenticatingWithInvalidUser() throws Exception {
    mvc.perform(
       get("/hello")
         .with(httpBasic("mary","12345")))      ❷
         .andExpect(status().isUnauthorized());
  }
}
```
❶ 올바른 자격 증명으로 인증

❷ 잘못된 자격 증명으로 인증

httpBasic() 요청 후처리기를 사용하여 테스트에 인증을 실행하도록 지시합니다. 이러한 방식으로 유효하거나 유효하지 않은 자격 증명을 사용하여 인증할 때 엔드포인트의 동작을 검증합니다. 동일한 접근 방식을 사용하여 양식 로그인으로 인증을 테스트할 수 있습니다. 인증을 위해 양식 로그인을 사용한 프로젝트 ssia-ch5-ex4를 열고 인증이 올바르게 작동하는지 확인하기 위한 몇 가지 테스트를 작성해 보겠습니다. 다음 시나리오에서 앱의 동작을 테스트합니다.

- 잘못된 자격 증명으로 인증하는 경우

- 유효한 자격 증명 집합으로 인증하지만 AuthenticationSuccessHandler에 작성한 구현에 따라 사용자에게 유효한 권한이 없는 경우

- 우리가 AuthenticationSuccessHandler에 작성한 구현에 따라 유효한 자격 증명과 유효한 권한을 가진 사용자로 인증할 때

목록 20.14에서 첫 번째 시나리오에 대한 구현을 찾을 수 있습니다. 잘못된 자격 증명을 사용하여 인증하는 경우 앱은 사용자를 인증하지 않고 HTTP 응답에 "실패" 헤더를 추가합니다. 5장에서 인증에 대해 논의할 때 앱을 사용자 정의하고 AuthenticationFailureHandler와 함께 "실패한" 헤더를 추가했습니다.

목록 20.14 테스트 양식 로그인 인증 실패
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void loggingInWithWrongUser() throws Exception {
    mvc.perform(formLogin()                               ❶
          .user("joey").password("12345"))
          .andExpect(header().exists("failed"))
          .andExpect(unauthenticated());
  }
}
```
❶ 잘못된 자격 증명 집합으로 양식 로그인을 사용하여 인증합니다.

5장으로 돌아가서 Authentication-SuccessHandler를 사용하여 인증 논리를 사용자 정의했습니다. 우리 구현에서 사용자에게 읽기 권한이 있는 경우 앱은 사용자를 /home 페이지로 리디렉션합니다. 그렇지 않으면 앱이 사용자를 /error 페이지로 리디렉션합니다. 다음 목록은 이 두 시나리오의 구현을 보여줍니다.

목록 20.15 사용자 인증 시 앱 동작 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  // Omitted code

  @Test
  public void loggingInWithWrongAuthority() throws Exception {
    mvc.perform(formLogin()
                .user("mary").password("12345")
            )
            .andExpect(redirectedUrl("/error"))     ❶
            .andExpect(status().isFound())
            .andExpect(authenticated());
    }

  @Test
  public void loggingInWithCorrectAuthority() throws Exception {
    mvc.perform(formLogin()
                 .user("bill").password("12345")
            )
            .andExpect(redirectedUrl("/home"))      ❷
            .andExpect(status().isFound())
            .andExpect(authenticated());
    }
}
```
❶ 읽기 권한이 없는 사용자로 인증 시 앱은 사용자를 경로/오류로 리디렉션합니다.

❷ 읽기 권한이 있는 사용자로 인증 시 앱은 사용자를 경로 /home으로 리디렉션합니다.

### 20.6 CSRF 구성 테스트

이 섹션에서는 애플리케이션에 대한 CSRF(교차 사이트 요청 위조) 보호 구성 테스트에 대해 설명합니다. 앱이 CSRF 취약점을 제시하면 공격자는 사용자가 애플리케이션에 로그인한 후 원하지 않는 조치를 취하도록 속일 수 있습니다. 10장에서 논의했듯이 Spring Security는 CSRF 토큰을 사용하여 이러한 취약점을 완화합니다. 이렇게 하면 모든 변경 작업(POST, PUT, DELETE)에 대해 요청의 헤더에 유효한 CSRF 토큰이 있어야 합니다. 물론 어떤 시점에서는 HTTP GET 요청 이상을 테스트해야 합니다. 10장에서 논의한 것처럼 애플리케이션을 구현하는 방법에 따라 CSRF 보호를 테스트해야 할 수도 있습니다. 예상대로 작동하고 변경 작업을 구현하는 끝점을 보호하는지 확인해야 합니다.

다행히 Spring Security는 RequestPostProcessor를 사용하여 CSRF 보호를 테스트하는 쉬운 접근 방식을 제공합니다. ssia-ch10-ex1 프로젝트를 열고 다음 시나리오에서 HTTP POST로 호출할 때 끝점 /hello에 대해 CSRF 보호가 활성화되었는지 테스트해 보겠습니다.

- CSRF 토큰을 사용하지 않는 경우 HTTP 응답 상태는 403 Forbidden입니다.

- CSRF 토큰을 보내면 HTTP 응답 상태는 200 OK입니다.

다음 목록은 이 두 시나리오의 구현을 보여줍니다. 단순히 csrf() RequestPostProcessor를 사용하여 응답에서 CSRF 토큰을 보내는 방법을 관찰하십시오.

목록 20.16 CSRF 보호 테스트 시나리오 구현
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void testHelloPOST() throws Exception {
    mvc.perform(post("/hello"))                           ❶
          .andExpect(status().isForbidden());
  }

  @Test
  public void testHelloPOSTWithCSRF() throws Exception {
    mvc.perform(post("/hello").with(csrf()))              ❷
          .andExpect(status().isOk());
  }
}
```
❶ CSRF 토큰 없이 엔드포인트 호출 시 HTTP 응답 상태는 403 Forbidden입니다.

❷ CSRF 토큰으로 엔드포인트를 호출할 때 HTTP 응답 상태는 200 OK입니다.

## 20.7 CORS 구성 테스트

이 섹션에서는 CORS(Cross-Origin Resource Sharing) 구성 테스트에 대해 설명합니다. 10장에서 배웠듯이 브라우저가 한 출처(예: example.com)에서 웹 앱을 로드하면 브라우저는 앱이 다른 출처(예: example.org)에서 오는 HTTP 응답을 사용하는 것을 허용하지 않습니다. ). 우리는 이러한 제한을 완화하기 위해 CORS 정책을 사용합니다. 이런 식으로 여러 출처에서 작동하도록 애플리케이션을 구성할 수 있습니다. 물론 다른 보안 구성과 마찬가지로 CORS 정책도 테스트해야 합니다. 10장에서 CORS는 HTTP 응답이 허용되는지 여부를 정의하는 값을 가진 응답의 특정 헤더에 관한 것임을 배웠습니다. CORS 사양과 관련된 이러한 헤더 중 두 가지는 Access-Control-Allow-Origin 및 Access-Control-Allow-Methods입니다. 10장에서 이러한 헤더를 사용하여 앱에 대한 다중 출처를 구성했습니다.

CORS 정책에 대한 테스트를 작성할 때 해야 할 일은 이러한 헤더(구성의 복잡성에 따라 다른 CORS 관련 헤더)가 존재하고 올바른 값을 갖는지 확인하는 것입니다. 이 검증을 위해 우리는 실행 전 요청을 할 때 브라우저가 하는 것처럼 정확하게 행동할 수 있습니다. CORS 헤더 값을 요청하는 HTTP OPTIONS 메서드를 사용하여 요청합니다. ssia-ch10-ex4 프로젝트를 열고 CORS 헤더 값을 검증하는 테스트를 작성해 보겠습니다. 다음 목록은 테스트의 정의를 보여줍니다.

목록 20.17 CORS 정책에 대한 테스트 구현
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void testCORSForTestEndpoint() throws Exception {
    mvc.perform(options("/test") ❶
            .header("Access-Control-Request-Method", "POST")
            .header("Origin", "http://www.example.com")
      ) ❷
      .andExpect(header().exists("Access-Control-Allow-Origin"))
      .andExpect(header().string("Access-Control-Allow-Origin", "*"))
      .andExpect(header().exists("Access-Control-Allow-Methods"))
      .andExpect(header().string("Access-Control-Allow-Methods", "POST"))
      .andExpect(status().isOk());
  }

}
```
❶ 끝점에서 CORS 헤더 값을 요청하는 HTTP OPTIONS 요청을 수행합니다.

❷ 앱에서 만든 구성에 따라 헤더 값의 유효성을 검사합니다.

## 20.8 반응형 Spring Security 구현 테스트

이 섹션에서는 반응형 앱 내에서 개발된 기능과 Spring Security의 통합 테스트에 대해 논의합니다. Spring Security가 반응형 앱에 대한 보안 구성 테스트도 지원한다는 사실에 놀라지 않을 것입니다.

비반응형 애플리케이션의 경우와 마찬가지로 사후반응형 애플리케이션의 보안은 중요한 측면입니다. 따라서 보안 구성을 테스트하는 것도 필수적입니다. 보안 구성에 대한 테스트를 구현하는 방법을 보여주기 위해 19장에서 작업한 예제로 돌아갑니다. 반응형 애플리케이션을 위한 Spring Security를 ​​사용하면 테스트를 작성하기 위한 두 가지 접근 방식을 알아야 합니다.

- @WithMockUser 주석으로 모의 사용자 사용

- WebTestClientConfigurer 사용

@WithMockUser 주석을 사용하는 것은 20.1절에서 논의한 것처럼 반응하지 않는 앱과 동일하게 작동하기 때문에 간단합니다. 테스트의 정의는 다르지만 반응형 앱이기 때문에 더 이상 MockMvc를 사용할 수 없습니다. 그러나 이 변경은 Spring Security와 관련이 없습니다. WebTestClient라는 도구인 반응형 앱을 테스트할 때 비슷한 것을 사용할 수 있습니다. 다음 목록에서는 모의 사용자를 사용하여 반응 엔드포인트의 동작을 확인하는 간단한 테스트 구현을 찾습니다.

목록 20.18 반응적 구현을 ​​테스트할 때 @WithMockUser 사용하기
```java
@SpringBootTest
@AutoConfigureWebTestClient            ❶
class MainTests {

  @Autowired                           ❷
  private WebTestClient client;

  @Test
  @WithMockUser                        ❸
  void testCallHelloWithValidUser() {
    client.get()                       ❹
            .uri("/hello")
            .exchange()
            .expectStatus().isOk();
  }
}
```
❶ 테스트에 사용하는 WebTestClient를 자동 구성하기 위해 Spring Boot를 요청합니다.

❷ Spring 컨텍스트에서 Spring Boot로 구성된 WebTestClient 인스턴스를 주입합니다.

❸ @WithMockUser 주석을 사용하여 테스트를 위한 모의 사용자 정의

❹ 교환 및 결과 검증

관찰한 바와 같이 @WithMockUser 주석을 사용하는 것은 반응이 없는 앱의 경우와 거의 동일합니다. 프레임워크는 모의 사용자와 함께 SecurityContext를 생성합니다. 애플리케이션은 인증 프로세스를 건너뛰고 테스트의 SecurityContext에서 모의 ​​사용자를 사용하여 권한 부여 규칙을 검증합니다.

사용할 수 있는 두 번째 접근 방식은 WebTestClientConfigurer입니다. 이 접근 방식은 반응하지 않는 앱의 경우 RequestPostProcessor를 사용하는 것과 유사합니다. 반응형 앱의 경우 우리가 사용하는 WebTestClient에 대해 테스트 컨텍스트를 변경하는 데 도움이 되는 WebTestClientConfigurer를 설정합니다. 예를 들어, 섹션 20.6에서 반응하지 않는 앱에 대해 했던 것처럼 모의 사용자를 정의하거나 CSRF 토큰을 보내 CSRF 보호를 테스트할 수 있습니다. 다음 목록은 WebTestClientConfigurer를 사용하는 방법을 보여줍니다.

Listing 20.19 WebTestClientConfigurer를 사용하여 모의 사용자 정의하기

```java
@SpringBootTest
@AutoConfigureWebTestClient
class MainTests {

  @Autowired
  private WebTestClient client;

  // Omitted code

  @Test
  void testCallHelloWithValidUserWithMockUser() {
    client.mutateWith(mockUser())                  ❶
           .get()
           .uri("/hello")
           .exchange()
           .expectStatus().isOk();
    }
}
```
❶ Before executing the GET request, mutates the call to use a mock user

Assuming you’re testing CSRF protection on a POST call, you write something similar to this:
```java
client.mutateWith(csrf())
         .post()
         .uri("/hello")
           .exchange()
           .expectStatus().isOk();
```

##### Mocking 종속성

종종 우리의 기능은 외부 의존성에 의존합니다. 보안 관련 구현은 때때로 외부 종속성에 의존합니다.

몇 가지 예는 사용자 자격 증명, 인증 키 또는 토큰을 저장하는 데 사용하는 데이터베이스입니다. 외부 응용 프로그램은 리소스 서버가 불투명 토큰에 대한 세부 정보를 얻기 위해 권한 부여 서버의 토큰 내부 검사 끝점을 호출해야 하는 OAuth 2 시스템의 경우와 같이 종속성을 나타냅니다. 이러한 경우를 처리할 때 일반적으로 종속성에 대한 모의 객체를 생성합니다. 예를 들어, 데이터베이스에서 사용자를 찾는 대신 저장소를 흉내내고 해당 메서드가 구현하는 테스트 시나리오에 적절하다고 생각하는 것을 반환하도록 합니다.

이 책에서 작업한 프로젝트에서 종속성을 mocking한 몇 가지 예를 찾을 수 있습니다. 이를 위해 다음을 살펴보는 데 관심이 있을 수 있습니다.

- 프로젝트 ssia-ch6-ex1에서 인증 흐름을 테스트할 수 있도록 저장소를 흉내냈습니다. 이렇게 하면 사용자를 확보하기 위해 실제 데이터베이스에 의존할 필요가 없지만 모든 구성 요소가 통합된 인증 흐름을 테스트할 수 있습니다.

- 프로젝트 ssia-ch11-ex1-s2에서 프록시를 조롱하여 프로젝트 ssia-ch11-ex1-s1에 구현된 애플리케이션에 의존할 필요 없이 두 가지 인증 단계를 테스트했습니다.

- ssia-ch14-ex1-rs 프로젝트에서 WireMockServer라는 도구를 사용하여 인증 서버의 토큰 내부 검사 끝점을 흉내냈습니다.
  
다양한 테스트 프레임워크는 우리의 기능이 의존하는 종속성을 가짜로 만들기 위해 모의 또는 스텁을 생성하기 위한 다양한 솔루션을 제공합니다. 스프링 시큐리티와 직접적인 관련이 없더라도 그 주제와 중요성을 알리고 싶었다. 다음은 이 주제를 계속 공부할 수 있는 몇 가지 리소스입니다.

- Chapter 8 of JUnit in Action by Cătălin Tudose et al. (Manning, 2020):
https://livebook.manning.com/book/junit-in-action-third-edition/chapter-8

- Chapters 5 and 9 of Unit Testing Principles, Practices, and Patterns by Vladimir Khorikov (Manning, 2020):
https://livebook.manning.com/book/unit-testing/chapter-5
https://livebook.manning.com/book/unit-testing/chapter-9

## 요약

- 필기 시험은 모범 사례입니다. 새로운 구현이나 수정 사항이 기존 기능을 손상시키지 않는지 확인하기 위해 테스트를 작성합니다.

- 코드를 테스트할 뿐만 아니라 사용하는 라이브러리 및 프레임워크와의 통합도 테스트해야 합니다.

- Spring Security는 보안 구성에 대한 테스트 구현을 위한 탁월한 지원을 제공합니다.

- 모의 사용자를 이용하여 직접 인증을 테스트할 수 있습니다. 일반적으로 권한 부여 테스트보다 인증 테스트가 더 적기 때문에 인증 없이 권한 부여에 대한 별도의 테스트를 작성합니다.

- 더 적은 수의 별도 테스트에서 인증을 테스트한 다음 엔드포인트 및 방법에 대한 권한 구성을 테스트하는 실행 시간을 절약합니다.

- 비반응형 앱에서 엔드포인트에 대한 보안 구성을 테스트하기 위해 Spring Security는 MockMvc로 테스트를 작성하기 위한 탁월한 지원을 제공합니다.

- 반응형 앱에서 엔드포인트에 대한 보안 구성을 테스트하기 위해 Spring Security는 WebTestClient를 사용하여 테스트를 작성하는 데 탁월한 지원을 제공합니다.

- 메소드 보안을 사용하여 보안 구성을 작성한 메소드에 대한 테스트를 직접 작성할 수 있습니다.
