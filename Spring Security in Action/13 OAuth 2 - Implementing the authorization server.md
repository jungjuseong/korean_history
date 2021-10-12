
 
# 13 OAuth2 인증 서버 구현

이 장에서는 다음을 다룹니다.
- 인증 서버 구현
- 클라이언트 관리
- 부여grant 유형 사용

이 장에서는 인증 서버를 구현하는 방법에 대해 설명합니다. 권한 부여 서버의 역할은 사용자를 인증하고 클라이언트에 토큰을 제공하는 것입니다. 클라이언트는 이 토큰을 사용하여 사용자를 대신하여 리소스 서버에서 노출된 리소스에 액세스합니다. 

OAuth 2 프레임워크는 토큰을 얻기 위한 여러 흐름을 정의합니다. 이러한 흐름을 권한 부여라고 합니다. 시나리오에 따라 다양한 부여 유형 중 하나를 선택합니다. 권한 부여 서버의 동작은 선택한 권한에 따라 다릅니다. 이 장에서는 가장 일반적인 승인 유형에 대해 인증 서버를 구성하는 방법을 배웁니다.

- 인증코드 부여 유형
- 비밀번호 부여 유형
- 클라이언트 자격 증명 부여 유형

리프레시 토큰을 발행하도록 인증 서버를 구성하는 방법도 배우게 됩니다. 클라이언트는 리프레시 토큰을 사용하여 새 액세스 토큰을 얻습니다. 액세스 토큰이 만료되면 클라이언트는 새 토큰을 받아야 합니다. 그렇게 하기 위해 클라이언트는 사용자 자격 증명을 사용하여 재인증하거나 리프레시 토큰을 사용하는 두 가지 선택이 있습니다. 섹션 12.3.4에서 사용자 자격 증명보다 리프레시 토큰을 사용할 때의 이점에 대해 논의했습니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F01_Spilca.png)

그림 13.1 인증 서버는 리소스 소유자를 식별하고 클라이언트에 액세스 토큰을 제공합니다. 클라이언트는 사용자를 대신하여 리소스에 액세스하려면 액세스 토큰이 필요합니다.

몇 달 동안 Spring Security를 ​​사용한 인증 서버 개발이 더 이상 지원되지 않는다는 소문이 있었습니다(http://mng.bz/v9lm). 마지막으로 Spring Security OAuth 2 종속성은 더 이상 사용되지 않습니다. 이 작업을 통해 클라이언트와 리소스 서버를 구현하기 위한 대안(이 책에서 배운 것)이 있지만 권한 부여 서버는 없습니다. 운 좋게도 Spring Security 팀은 http://mng.bz/4Be5에서 새로운 인증 서버가 개발 중이라고 발표했습니다. 또한 http://mng.bz/Qx01 링크를 사용하여 다양한 Spring Security 프로젝트에서 구현된 기능을 계속 알고 있는 것이 좋습니다.

새로운 Spring Security 인증 서버가 성숙하는 데는 시간이 걸립니다. 그때까지 사용자 정의 인증 서버를 개발하는 유일한 방법은 이 장에서 서버를 구현하는 방법입니다. 사용자 지정 권한 부여 서버를 구현하면 이 구성 요소가 작동하는 방식을 더 잘 이해할 수 있습니다.

개발자가 프로젝트에서 이 접근 방식을 적용한 것을 봅니다. 이러한 방식으로 인증 서버를 구현하는 프로젝트를 처리해야 하는 경우 새 구현을 사용하기 전에 이를 이해하는 것이 여전히 중요합니다. 그리고 새로운 권한 부여 서버 구현을 시작하고 싶다고 가정해 봅시다. 다른 선택이 없기 때문에 여전히 Spring Security를 ​​사용하는 유일한 방법입니다.

사용자 지정 인증 서버를 구현하는 대신 Keycloak 또는 Okta와 같은 타사 도구를 사용할 수 있습니다. 18장에서는 실습 예제에서 Keycloak을 사용할 것입니다. 그러나 내 경험상 이해 관계자가 이러한 솔루션을 사용하는 것을 수락하지 않는 경우가 있으므로 사용자 지정 코드를 구현해야 합니다.

## 13.1 자체 인증 서버 구현 작성

OAuth 2는 주로 액세스 토큰을 얻는 것입니다. 따라서 먼저 구현 방법을 알아야 합니다. 그런 다음 14장과 15장에서 클라이언트가 인증 서버에서 얻은 액세스 토큰을 기반으로 리소스 서버가 요청을 인증하는 방법을 배웁니다.

시작하려면 새 Spring Boot 프로젝트를 만들고 다음 코드에 종속성을 추가해야 합니다. 이 프로젝트의 이름을 ssia-ch13-ex1로 지정했습니다.

Inside the project tag, you also need to add the dependencyManagement tag for the spring-cloud-dependencies artifact ID:
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-dependencies</artifactId>
      <version>Hoxton.SR1</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
이제 AuthServerConfig라고 하는 구성 클래스를 정의할 수 있습니다. @Configuration 주석 외에 @EnableAuthorizationServer로 이 클래스에 주석을 달아야 합니다. 권한 부여 서버에 특정한 구성을 활성화하도록 Spring Boot에 지시합니다. `AuthorizationServerConfigurerAdapter` 클래스를 확장하고 특정 메서드를 재정의하여 이 구성을 사용자 지정할 수 있습니다.

**Listing 13.1** The AuthServerConfig class
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig 
   extends AuthorizationServerConfigurerAdapter {
}
```
인증 서버에 대한 최소 구성이 이미 있지만 사용 가능하게 하려면 사용자 관리를 구현하고 적어도 하나의 클라이언트를 등록하고 지원할 부여 유형을 결정해야 합니다.

## 13.2 사용자 관리 정의

인증 서버는 OAuth 2 프레임워크에서 사용자 인증을 처리하는 구성 요소이므로 사용자를 관리해야 합니다. 사용자 관리 구현은 3장과 4장에서 배운 것과 다르지 않으므로 `UserDetails`, `UserDetailsService` 및 `UserDetailsManager`를 사용하여 자격 증명을 관리합니다. 비밀번호는 `PasswordEncoder` 계약을 계속 사용합니다.

여기에서 이들은 3장과 4장에서 배운 것과 동일한 역할을 하고 동일하게 작동합니다. 이면에는 이전 장에서 논의한 표준 인증 아키텍처가 있습니다.

그림 13.2는 인증 프로세스에서 작동하는 주요 구성 요소를 상기시킵니다. 지금까지 인증 아키텍처를 설명한 방식과 다른 점은 더 이상 SecurityContext가 없다는 것입니다. 인증 결과가 SecurityContext에 저장되지 않았기 때문입니다. 인증은 대신 TokenStore의 토큰으로 관리됩니다. 리소스 서버에 대해 논의하는 14장에서 TokenStore에 대해 자세히 알아볼 것입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F02_Spilca.png)

**그림 13.2** 인증 프로세스. 필터는 사용자 요청을 가로채고 인증 책임을 인증 관리자에게 위임합니다. 또한 인증 관리자는 인증 논리를 구현하는 인증 공급자를 사용합니다. 인증 제공자는 사용자를 찾기 위해 `UserDetailsService`를 사용하고 인증 제공자는 암호를 확인하기 위해 `PasswordEncoder`를 사용합니다.

인증 서버에서 사용자 관리를 구현하는 방법을 알아보겠습니다. 항상 구성 클래스의 책임을 분리하는 것이 좋으므로 사용자 관리에 필요한 구성만 작성하는 두 번째 구성 클래스로 `WebSecurityConfig`로 정의했습니다.

**Listing 13.2** Configurations for user management in the WebSecurityConfig class
```java
@Configuration
public class WebSecurityConfig {

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
목록 13.2에서 보았듯이 InMemoryUserDetailsManager를 User-DetailsService로 선언하고 NoOpPasswordEncoder를 PasswordEncoder로 사용합니다. 이러한 구성 요소에 대해 원하는 구현을 사용할 수 있지만 OAuth 2 측면에 집중하도록 이러한 구성 요소를 최대한 단순하게 유지합니다.

이제 사용자가 있으므로 사용자 관리를 인증 서버 구성에 연결하기만 하면 됩니다. 이를 위해 Spring 컨텍스트에서 AuthenticationManager를 Bean으로 노출한 다음 AuthServerConfig 클래스에서 사용합니다. 다음 목록은 Spring 컨텍스트에서 AuthenticationManager를 빈으로 추가하는 방법을 보여줍니다.

**Listing 13.3** Adding the AuthenticationManager instance in the Spring context
```java
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter { ❶

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

  @Bean ❷
  public AuthenticationManager authenticationManagerBean() 
    throws Exception {
    return super.authenticationManagerBean();
  }
}
```
❶ WebSecurityConfigurerAdapter를 확장하여 AuthenticationManager 인스턴스에 액세스

❷ Spring 컨텍스트에서 AuthenticationManager 인스턴스를 빈으로 추가

이제 AuthServerConfig 클래스를 변경하여 인증 서버에 AuthenticationManager를 등록할 수 있습니다. 다음 목록은 AuthServerConfig 클래스에서 변경해야 하는 사항을 보여줍니다.

**Listing 13.4** AuthenticationManager 등록하기
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

  @Autowired ❶
  private AuthenticationManager authenticationManager;

  @Override ❷
  public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints.authenticationManager(authenticationManager);
  }
}
```
❶ 컨텍스트에서 AuthenticationManager 인스턴스를 주입합니다.

❷ AuthenticationManager를 설정하기 위해 configure() 메서드를 재정의합니다.

이러한 구성을 통해 이제 인증 서버에서 인증할 수 있는 사용자가 있습니다. 그러나 OAuth 2 아키텍처는 사용자가 클라이언트에 권한을 부여함을 의미합니다. 사용자를 대신하여 리소스를 사용하는 것은 클라이언트입니다. 13.3에서 인증 서버에 대해 클라이언트를 구성하는 방법을 배웁니다.

## 13.3 인증 서버에 클라이언트 등록

클라이언트를 인증 서버에 알리는 방법을 배웁니다. 인증 서버를 호출하려면 OAuth 2 아키텍처에서 클라이언트 역할을 하는 앱에 자체 자격 증명이 필요합니다. 인증 서버는 또한 이러한 자격 증명을 관리하고 알려진 클라이언트의 요청만 허용합니다(그림 13.3).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F03_Spilca.png)

**그림 13.3** 인증 서버는 사용자와 클라이언트의 자격 증명을 저장합니다. 클라이언트 자격 증명을 사용하여 알려진 응용 프로그램만 인증되도록 허용합니다.

12장에서 개발한 클라이언트 응용 프로그램은 GitHub를 인증 서버로 사용했습니다. GitHub는 클라이언트 앱에 대해 알아야 했기 때문에 가장 먼저 한 일은 GitHub에 애플리케이션을 등록하는 것이었습니다. 그런 다음 클라이언트 ID와 클라이언트 암호인 클라이언트 자격 증명을 받았습니다. 

우리는 이러한 자격 증명을 구성했으며 앱은 이를 사용하여 권한 부여 서버(GitHub)로 인증했습니다. 

이 경우에도 동일하게 적용됩니다. 인증 서버는 클라이언트의 요청을 수락하기 때문에 클라이언트를 알아야 합니다. 여기에서 프로세스가 익숙해져야 합니다. 권한 부여 서버에 대한 클라이언트를 정의하는 계약은 `ClientDetails`입니다. 해당 ID로 `ClientDetails`를 검색하는 개체를 정의하는 계약은 `ClientDetailsService`입니다.

이러한 인터페이스는 UserDetails 및 UserDetailsService 인터페이스처럼 작동하지만 클라이언트를 나타냅니다. 3장에서 논의한 많은 것들이 ClientDetails 및 ClientDetailsService에 대해 유사하게 작동한다는 것을 알게 될 것입니다.

예를 들어 InMemoryClientDetailsService는 메모리에서 ClientDetails를 관리하는 ClientDetailsService 인터페이스의 구현입니다. UserDetails에 대한 InMemoryUserDetailsManager 클래스와 유사하게 작동합니다. 마찬가지로 JdbcClientDetailsService는 JdbcUserDetailsManager와 유사합니다. 그림 13.4는 이러한 클래스와 인터페이스, 그리고 이들 간의 관계를 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F04_Spilca.png)

**Figure 13.4** 인증 서버에 대한 클라이언트 관리를 정의하는 데 사용하는 클래스와 인터페이스 간의 종속성

목록 13.5는 클라이언트 구성을 정의하고 InMemoryClientDetailsService를 사용하여 설정하는 방법을 보여줍니다. 목록에서 사용하는 BaseClientDetails 클래스는 Spring Security에서 제공하는 ClientDetails 인터페이스의 구현입니다. 목록 13.6에서 동일한 구성을 작성하는 더 짧은 방법을 찾을 수 있습니다.

**Listing 13.5** 클라이언트 설정
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
   extends AuthorizationServerConfigurerAdapter {

   // Omitted code

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) 
    throws Exception {
      
    var service = new InMemoryClientDetailsService();   ❷

    var cd = new BaseClientDetails(); 
    cd.setClientId("client"); 
    cd.setClientSecret("secret"); 
    cd.setScope(List.of("read")); 
    cd.setAuthorizedGrantTypes(List.of("password"));    ❸

    service.setClientDetailsStore(Map.of("client", cd)); ❹

    clients.withClientDetails(service); ❺
  }
}
```
❷ ClientDetailsService 구현을 사용하여 인스턴스 생성

❸ ClientDetails의 인스턴스를 생성하고 클라이언트에 대해 필요한 세부 정보를 설정합니다.

❹ InMemoryClientDetailsService에 ClientDetails 인스턴스 추가

❺ 인증 서버에서 사용할 ClientDetailsService를 구성합니다.

목록 13.6은 동일한 구성을 작성하는 더 짧은 방법을 보여줍니다. 이를 통해 반복을 피하고 더 깨끗한 코드를 작성할 수 있습니다.

**Listing 13.6** Configuring ClientDetails in memory
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  // Omitted code

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) 
      throws Exception {

      clients.inMemory() ❶
             .withClient("client") 
             .secret("secret") 
             .authorizedGrantTypes("password")  
             .scopes("read");  ❷
    }
}
```
❶ `ClientDetailsService` 구현을 사용하여 메모리에 저장된 `ClientDetails` 관리

❷ `ClientDetails`의 인스턴스를 빌드하고 추가합니다.

더 적은 코드를 작성하려면 목록 13.5에서 더 자세한 것보다 더 짧은 버전을 사용하는 것을 선호하지만 실제 시나리오의 경우인 데이터베이스에 클라이언트 세부 정보를 저장하는 구현을 작성하는 경우 목록 13.5의 계약을 사용하는 것이 가장 좋습니다.

> **EXERCISE** 데이터베이스에서 클라이언트 세부 정보를 관리하는 구현을 작성합니다. 3.3에서 작업한 UserDetailsService와 유사한 구현을 사용할 수 있습니다.

> **참고** UserDetailsService에 대해 했던 것처럼 이 예제에서는 메모리의 세부 정보를 관리하는 구현을 사용합니다. 실제 시나리오에서는 일반적으로 데이터베이스에서 이러한 세부 정보를 유지하는 구현을 사용합니다.

## 13.4 암호 부여 유형 사용하기

OAuth 2 암호 부여와 함께 권한 부여 서버를 사용합니다. 글쎄, 우리는 주로 그것이 작동하는지 테스트합니다. 섹션 13.2 및 13.3에서 수행한 구현으로 이미 암호 부여 유형을 사용하는 작동 중인 인증 서버가 있기 때문입니다. 그림 13.5는 이 흐름 내에서 암호 부여 유형과 권한 부여 서버의 위치를 ​​알려줍니다.

이제 응용 프로그램을 시작하고 테스트해 보겠습니다. /oauth/token 엔드포인트에서 토큰을 요청할 수 있습니다. Spring Security는 우리를 위해 이 끝점을 자동으로 구성합니다. HTTP Basic과 함께 클라이언트 자격 증명을 사용하여 끝점에 액세스하고 필요한 세부 정보를 쿼리 매개 변수로 보냅니다. 12장에서 알다시피 이 요청에서 보내야 하는 매개변수는 다음과 같습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F05_Spilca.png)

**그림 13.5** 암호 부여 유형. 권한 부여 서버는 사용자 자격 증명을 수신하고 사용자를 인증합니다. 자격 증명이 정확하면 인증 서버는 클라이언트가 인증된 사용자에 속한 리소스를 호출하는 데 사용할 수 있는 액세스 토큰을 발급합니다.

- 값 암호가 있는 `grant_type`
- 사용자 자격 증명인 사용자 이름 및 암호
- 권한이 부여된 scope

In the next code snippet, you see the cURL command:
```bsh
curl -v -XPOST -u client:secret http://localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read
```

Running this command, you get this response:
```json
{
  "access_token":"693e11d3-bd65-431b-95ff-a1c5f73aca8c",
  "token_type":"bearer",
  "expires_in":42637,
  "scope":"read"
}
```
응답에서 액세스 토큰은 Spring Security의 기본 구성에서 단순한 UUID입니다. 클라이언트는 이제 이 토큰을 사용하여 리소스 서버에 의해 노출된 리소스를 호출할 수 있습니다. 13.2에서 리소스 서버를 구현하는 방법과 토큰 사용자 지정에 대해 자세히 배웠습니다.

## 13.5 인증 코드 부여 유형 사용

인증 코드 부여 유형에 대한 인증 서버 구성에 대해 설명합니다. 12장에서 개발한 클라이언트 애플리케이션과 함께 이 부여 유형을 사용했으며 가장 일반적으로 사용되는 OAuth 2 부여 유형 중 하나라는 것을 알고 있습니다. 실제 시스템에서 이 요구 사항을 찾을 가능성이 매우 높기 때문에 이 권한 부여 유형과 함께 작동하도록 권한 부여 서버를 구성하는 방법을 이해하는 것이 중요합니다. 

따라서 Spring Security와 함께 작동하도록 하는 방법을 증명하는 몇 가지 코드를 작성합니다. ssia-ch13-ex2라는 다른 프로젝트를 만들었습니다. 그림 13.6에서 인증 코드 부여 유형이 작동하는 방식과 인증 서버가 이 흐름의 다른 구성 요소와 상호 작용하는 방식을 상기할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F06_Spilca.png)

**그림 13.6** 인증 코드 부여 유형에서 클라이언트는 인증을 위해 사용자를 인증 서버로 리디렉션합니다. 사용자는 권한 부여 서버와 직접 상호 작용하고 일단 인증되면 권한 부여 서버는 리디렉션 URI를 클라이언트에 반환합니다. 클라이언트를 다시 호출할 때 인증 코드도 제공합니다. 클라이언트는 인증 코드를 사용하여 액세스 토큰을 얻습니다.

13.3에서 배웠듯이 클라이언트를 등록하는 방법에 관한 모든 것입니다. 따라서 다른 승인 유형을 사용하려면 목록 13.7에 표시된 대로 클라이언트 등록에서 설정하기만 하면 됩니다. 인증 코드 부여 유형의 경우 리디렉션 URI도 제공해야 합니다. 인증 서버가 인증을 완료한 후 사용자를 리디렉션하는 URI입니다. 리디렉션 URI를 호출할 때 권한 부여 서버는 액세스 코드도 제공합니다.

**Listing 13.7** Setting the authorization code grant type
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

    // Omitted code

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) 
      throws Exception {

      clients.inMemory()
         .withClient("client")
         .secret("secret")
         .authorizedGrantTypes("authorization_code")
         .scopes("read")
         .redirectUris("http://localhost:9090/home");
  }

  @Override
  public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints.authenticationManager(authenticationManager);
  }
}
```
여러 클라이언트를 가질 수 있으며 각각 다른 권한을 사용할 수 있습니다. 그러나 한 클라이언트에 대해 여러 권한을 설정할 수도 있습니다. 인증 서버는 클라이언트의 요청에 따라 작동합니다. 다른 클라이언트에 대해 다른 권한을 구성하는 방법을 보려면 다음 목록을 살펴보십시오.

**Listing 13.8** 다른 부여 유형으로 클라이언트 설정
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

    // Omitted code

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) 
      throws Exception {

       clients.inMemory()
         .withClient("client1")
         .secret("secret1")
         .authorizedGrantTypes("authorization_code")
         .scopes("read")
         .redirectUris("http://localhost:9090/home")
           .and()

         .withClient("client2")
         .secret("secret2")
         .authorizedGrantTypes( ❷
           "authorization_code", "password", "refresh_token")
         .scopes("read")
         .redirectUris("http://localhost:9090/home");
    }

    @Override
    public void configure(
      AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager);
    }
}
```
❶ ID가 client1인 클라이언트는 authorization_code 부여만 사용할 수 있습니다.

❷ ID가 client2인 클라이언트는 authorization_code, 비밀번호, 리프레시 토큰 중 하나를 사용할 수 있습니다.

### 클라이언트에 여러 부여 유형 사용

한 클라이언트에 대해 여러 부여 유형을 허용할 수 있지만 보안 관점에서 아키텍처에서 잘못된 관행을 사용하고 있음을 드러낼 수 있으므로 주의해야 합니다. 권한 부여 유형은 클라이언트(응용 프로그램)가 특정 리소스에 액세스할 수 있도록 액세스 토큰을 얻는 흐름입니다. 그러한 시스템에서 클라이언트를 구현할 때(12장에서 했던 것처럼) 사용하는 부여 유형에 따라 논리를 작성합니다.

그렇다면 인증 서버 측에서 동일한 클라이언트에 여러 승인 유형을 할당하는 이유는 무엇입니까? 가장 피하고 싶은 것은 클라이언트 자격 증명을 공유하는 것입니다. 클라이언트 자격 증명을 공유한다는 것은 다른 클라이언트 응용 프로그램이 동일한 클라이언트 자격 증명을 공유한다는 것을 의미합니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F07_Spilca.png)

클라이언트 자격 증명을 공유할 때 여러 클라이언트가 동일한 자격 증명을 사용하여 권한 부여 서버에서 액세스 토큰을 얻습니다.

OAuth 2 흐름에서 클라이언트는 애플리케이션이라 할지라도 자신을 식별하는 데 사용하는 자체 자격 증명이 있는 독립 구성 요소로 작동합니다. 사용자 자격 증명을 공유하지 않기 때문에 클라이언트 자격 증명도 공유해서는 안 됩니다. 클라이언트를 정의하는 모든 응용 프로그램이 동일한 시스템의 일부인 경우에도 권한 서버 수준에서 이러한 응용 프로그램을 별도의 클라이언트로 등록하는 것을 막을 수는 없습니다. 

인증 서버에 클라이언트를 개별적으로 등록하면 다음과 같은 이점이 있습니다.

- 각 애플리케이션에서 개별적으로 이벤트를 감사할 수 있습니다. 이벤트를 기록할 때 어떤 클라이언트가 이벤트를 생성했는지 알 수 있습니다.

- 더 강력한 격리를 허용합니다. 한 쌍의 자격 증명이 손실되면 하나의 클라이언트만 영향을 받습니다.

- scope를 분리할 수 있습니다. 특정 방식으로 토큰을 얻는 클라이언트에 다른 scope(부여된 권한)를 할당할 수 있습니다.
 
범위 분리는 기본이며 잘못 관리하면 이상한 시나리오가 발생할 수 있습니다. 다음 코드에 표시된 대로 클라이언트를 정의했다고 가정해 보겠습니다.

```java
clients.inMemory()
       .withClient("client")
       .secret("secret")
       .authorizedGrantTypes(  
         "authorization_code",
         "client_credentials")
       .scopes("read")
```
이 클라이언트는 인증 코드 및 클라이언트 자격 증명 부여 유형에 대해 구성됩니다. 이 중 하나를 사용하여 클라이언트는 읽기 권한을 제공하는 액세스 토큰을 얻습니다. 

여기서 이상한 점은 클라이언트가 사용자를 인증하거나 자체 자격 증명만 사용하여 동일한 토큰을 얻을 수 있다는 것입니다. 이것은 말이 되지 않으며, 이것이 보안 침해라고 주장할 수도 있습니다. 이상하게 들릴지 모르지만 감사를 요청받은 시스템에서 실제로 이것을 보았습니다. 왜 그 시스템을 위해 코드가 그렇게 설계되었습니까? 아마도 개발자는 부여 유형의 목적을 이해하지 못하고 웹 어딘가에서 찾은 일부 코드를 사용했을 것입니다. 

시스템의 모든 클라이언트가 가능한 모든 부여 유형(이 중 일부는 부여 유형으로 존재하지 않는 문자열이기도 함)을 포함하는 동일한 목록으로 구성된 것을 보았을 때 상상할 수 있었던 유일한 것입니다. 그러한 실수를 피하십시오. 조심하세요. 권한 부여 유형을 지정하려면 열거형 값이 아닌 문자열을 사용하며 이 설계는 실수를 유발할 수 있습니다. 예를들어, 이 코드에 표시된 것과 같은 구성을 작성할 수 있습니다.

```java
clients.inMemory()
       .withClient("client")
       .secret("secret")
       .authorizedGrantTypes("password", "hocus_pocus")
       .scopes("read")
```

"hocus_pocus" 부여 유형을 사용하지 않는 한 애플리케이션은 실제로 작동합니다.
목록 13.9에 제시된 구성을 사용하여 애플리케이션을 시작합시다. 인증 코드 부여 유형을 수락하려는 경우 서버는 클라이언트가 로그인을 위해 사용자를 리디렉션하는 페이지도 제공해야 합니다. 5장에서 배운 양식 로그인 구성을 사용하여 이 페이지를 구현합니다. 다음 목록에 표시된 대로 configure() 메서드를 재정의해야 합니다.

**Listing 13.9** Configuring form-login authentication for the authorization server
```java
@Configuration
public class WebSecurityConfig 
  extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) 
    throws Exception {
      http.formLogin();
  }
}
```
이제 다음 코드에 표시된 대로 애플리케이션을 시작하고 브라우저에서 링크에 액세스할 수 있습니다. 그런 다음 그림 13.7과 같이 로그인 페이지로 리디렉션됩니다.

http://localhost:8080/oauth/authorize?response_type=code&client_id=client&scope=read
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F08_Spilca.png)

**그림 13.7** 인증 서버가 로그인 페이지로 리디렉션합니다. 사용자를 인증한 후 제공된 리디렉션 URI로 리디렉션합니다.

로그인 후 인증 서버는 요청된 범위를 허용하거나 거부하도록 명시적으로 요청합니다. 그림 13.8은 이 형식을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F09_Spilca.png)

그림 13.8 인증 후 인증 서버는 인증하려는 범위를 확인하도록 요청합니다.

범위를 부여하면 권한 부여 서버가 리디렉션 URI로 리디렉션하고 액세스 토큰을 제공합니다. 다음 코드 조각에서 인증 서버가 나를 리디렉션한 URL을 찾습니다. 클라이언트가 요청의 쿼리 매개변수를 통해 얻은 액세스 코드를 관찰합니다.

```
http://localhost:9090/home?code=qeSLSt    ❶
```
❶ This is the authorization code.

애플리케이션은 이제 인증 코드를 사용하여 `/oauth/token` 엔드포인트를 호출하는 토큰을 얻을 수 있습니다.

```bsh
curl -v -XPOST -u client:secret "http://localhost:8080/oauth/token?grant_type=authorization_code&scope=read&code=qeSLSt"
```

The response body is

```json
{
  "access_token":"0fa3b7d3-e2d7-4c53-8121-bd531a870635",
  "token_type":"bearer",
  "expires_in":43052,
  "scope":"read"
}
```
인증 코드는 한 번만 사용할 수 있습니다. 동일한 코드를 다시 사용하여` /oauth/token` 엔드포인트를 호출하려고 하면 다음 코드에 표시된 것과 같은 오류가 수신됩니다. 사용자에게 다시 로그인하도록 요청해야만 다른 유효한 인증 코드를 얻을 수 있습니다.

```json
{
  "error":"invalid_grant",
  "error_description":"Invalid authorization code: qeSLSt"
}
```
## 13.6 클라이언트 자격 증명 부여 유형 사용

클라이언트 자격 증명 부여 유형 구현에 대해 설명합니다. 12장에서 백엔드 간 인증에 이 승인 유형을 사용합니다. 이 경우 필수는 아니지만 이 유형을 8장에서 논의한 API 키 인증 방법의 대안으로 볼 수 있습니다. 특정 사용자와 관련이 없는 엔드포인트를 보호할 때도 클라이언트 자격 증명 부여 유형을 사용할 수 있습니다. 클라이언트에 액세스가 필요한 항목입니다.

서버의 상태를 반환하는 끝점을 구현하려고 한다고 가정해 보겠습니다. 클라이언트는 이 끝점을 호출하여 연결을 확인하고 결국 사용자에게 연결 상태 또는 오류 메시지를 표시합니다. 이 끝점은 클라이언트와 리소스 서버 간의 거래만을 나타내고 사용자별 리소스와 관련이 없기 때문에 클라이언트는 사용자가 인증할 필요 없이 호출할 수 있어야 합니다. 이러한 시나리오의 경우 클라이언트 자격 증명 부여 유형을 사용합니다. 그림 13.9는 클라이언트 자격 증명 부여 유형이 작동하는 방식과 권한 부여 서버가 이 흐름의 다른 구성 요소와 상호 작용하는 방식을 상기시킵니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F10_Spilca.png)

그림 13.9 클라이언트 자격 증명 부여 유형에는 사용자가 포함되지 않습니다. 일반적으로 두 백엔드 솔루션 간의 인증에 이 권한 부여 유형을 사용합니다. 클라이언트는 액세스 토큰을 인증하고 얻기 위해 자격 증명만 필요합니다.

> **참고** 리소스 서버가 토큰을 확인하는 방법에 대해 가능한 모든 시나리오는 14장과 15장에서 자세히 논의할 것입니다.

예상대로 클라이언트 자격 증명 부여 유형을 사용하려면 이 부여에 클라이언트를 등록해야 합니다. 이 보조금 유형을 증명하기 위해 ssia-ch13-ex3이라는 별도의 프로젝트를 정의했습니다. 다음 목록에서 이 권한 부여 유형을 사용하는 클라이언트의 구성을 찾을 수 있습니다.

Listing 13.10 The client registration for the client credentials grant type
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  // Omitted code

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) 
        throws Exception {

    clients.inMemory()
              .withClient("client")
              .secret("secret")
              .authorizedGrantTypes("client_credentials")
              .scopes("info");
  }
}
```
You can start the application now and call the /oauth/token endpoint to get an access token. The next code snippet shows you how to obtain this:

"curl -v -XPOST -u client:secret "http://localhost:8080/oauth/token?grant_type=client_credentials&scope=info""

The response body is
```json
{
  "access_token":"431eb294-bca4-4164-a82c-e08f56055f3f",
  "token_type":"bearer",
  "expires_in":4300,
  "scope":"info"
}
```
클라이언트 자격 증명 부여 유형에 주의하십시오. 이 권한 부여 유형은 클라이언트가 자격 증명을 사용하기만 하면 됩니다. 사용자 자격 증명이 필요한 흐름과 동일한 범위에 대한 액세스를 제공하지 않는지 확인하십시오. 그렇지 않으면 사용자의 권한 없이 클라이언트가 사용자의 리소스에 액세스하도록 허용할 수 있습니다. 그림 13.10은 개발자가 사용자가 먼저 인증할 필요 없이 클라이언트가 사용자의 리소스 끝점을 호출할 수 있도록 하여 보안 위반을 만든 설계를 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH13_F11_Spilca.png)

그림 13.10 시스템의 악의적인 설계. 개발자는 사용자의 허가 없이 /info 엔드포인트를 호출할 수 있는 가능성을 클라이언트에 제공하기를 원했습니다. 그러나 이들은 동일한 범위를 사용했기 때문에 이제 클라이언트가 사용자의 리소스인 /transactions 엔드포인트를 호출할 수도 있습니다.

## 13.7 리프레시 토큰 부여 유형 사용

이 섹션에서는 Spring Security로 개발된 인증 서버에서 리프레시 토큰을 사용하는 방법에 대해 설명합니다. 12장에서 기억할 수 있듯이 리프레시 토큰은 다른 부여 유형과 함께 사용할 때 몇 가지 이점을 제공합니다. 인증 코드 부여 유형 및 암호 부여 유형(그림 13.11)과 함께 갱신 토큰을 사용할 수 있습니다.
 
그림 13.11 사용자가 인증하면 클라이언트는 액세스 토큰 외에 리프레시 토큰도 받습니다. 클라이언트는 리프레시 토큰을 사용하여 새 액세스 토큰을 얻습니다.

인증 서버가 리프레시 토큰을 지원하도록 하려면 클라이언트의 권한 부여 목록에 리프레시 토큰 권한을 추가해야 합니다. 예를 들어, 리프레시 토큰 부여를 증명하기 위해 섹션 13.4에서 만든 프로젝트를 변경하려면 다음 목록에 표시된 대로 클라이언트를 변경합니다. 이 변경 사항은 ssia-ch13-ex4 프로젝트에서 구현됩니다.

Listing 13.11 Adding the refresh token
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  // Omitted code

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) throws Exception {
      clients.inMemory()
        .withClient("client")
        .secret("secret")
        .authorizedGrantTypes(
           "password", 
           "refresh_token")      ❶
        .scopes("read");
  }
}
```
❶ 클라이언트의 권한 부여 유형 목록에 refresh_token 추가

이제 섹션 13.4에서 사용한 것과 동일한 cURL 명령을 시도하십시오. 응답이 비슷하지만 이제 새로 고침 토큰이 포함된 것을 볼 수 있습니다.
```
curl -v -XPOST -u client:secret http://localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read
```
다음 코드 조각은 이전 명령의 응답을 나타냅니다.
```json
{
  "access_token":"da2a4837-20a4-447d-917b-a22b4c0e9517",
  "token_type":"bearer",
  "refresh_token":"221f5635-086e-4b11-808c-d88099a76213",       ❶
  "expires_in":43199,
  "scope":"read"
}
```
❶ The app added the refresh token to the response.

## Summary

- ClientRegistration 인터페이스는 OAuth 2 클라이언트 등록을 정의합니다. ClientRegistrationRepository 인터페이스는 클라이언트 등록 관리를 담당하는 객체를 설명합니다. 이 두 계약을 통해 권한 부여 서버가 클라이언트 등록을 관리하는 방법을 사용자 정의할 수 있습니다.

- 구현된 인증 서버의 경우 클라이언트 등록에 따라 부여 유형이 결정됩니다. 동일한 권한 부여 서버가 다른 클라이언트에 다른 승인 유형을 제공할 수 있습니다. 이는 여러 승인 유형을 정의하기 위해 권한 부여 서버에서 특정 항목을 구현할 필요가 없음을 의미합니다.

- 권한 부여 코드 부여 유형의 경우 권한 부여 서버는 사용자에게 로그인 가능성을 제공해야 합니다. 이 요구 사항은 권한 부여 코드 흐름에서 사용자(자원 소유자)가 권한 부여 시 자신을 직접 인증한다는 사실의 결과입니다. 클라이언트에 대한 액세스 권한을 부여하는 서버.

- ClientRegistration은 여러 승인 유형을 요청할 수 있습니다. 이는 예를 들어 클라이언트가 서로 다른 상황에서 암호 및 인증 코드 부여 유형을 모두 사용할 수 있음을 의미합니다.

- 백엔드 간 인증을 위해 클라이언트 자격 증명 부여 유형을 사용합니다. 기술적으로 가능하지만 클라이언트가 다른 승인 유형과 함께 클라이언트 자격 증명 승인 유형을 요청하는 것은 드문 일입니다.

- 인증코드 부여형, 비밀번호 부여형과 함께 리프레시 토큰을 사용할 수 있습니다. 클라이언트 등록에 리프레시 토큰을 추가하여 액세스 토큰 외에 리프레시 토큰도 발급하도록 권한 부여 서버에 지시합니다. 클라이언트는 리프레시 토큰을 사용하여 사용자를 다시 인증할 필요 없이 새 액세스 토큰을 얻습니다.