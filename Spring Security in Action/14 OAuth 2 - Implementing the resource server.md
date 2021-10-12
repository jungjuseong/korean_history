# 14 OAuth 2: 리소스 서버 구현하기

이 장에서는 다음을 다룹니다.

- 리소스 서버 구현
- 토큰 검증 구현
- 토큰 관리 커스터마이징

이 장에서는 리소스 서버를 구현하는 방법에 대해 설명합니다. 리소스 서버는 사용자 리소스를 관리하는 구성 요소입니다. 이름 리소스 서버는 처음에는 암시적이지 않을 수 있지만 OAuth 2의 경우 이전 장에서 보호한 다른 앱과 마찬가지로 보안을 유지하는 백엔드를 나타냅니다. 예를 들어, 11장에서 구현한 비즈니스 로직 서버를 클라이언트가 액세스할 수 있도록 하려면 리소스 서버에 유효한 액세스 토큰이 필요합니다. 클라이언트는 인증 서버에서 액세스 토큰을 얻고 이를 사용하여 HTTP 요청 헤더에 토큰을 추가하여 리소스 서버의 리소스를 호출합니다. 그림 14.1은 OAuth 2 인증 아키텍처에서 리소스 서버의 위치를 ​​보여주는 12장의 환기를 제공합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F01_Spilca.png)

**그림 14.1** 리소스 서버는 사용자 데이터를 관리합니다. 리소스 서버에서 끝점을 호출하려면 클라이언트가 유효한 액세스 토큰으로 사용자가 데이터 작업을 승인했음을 증명해야 합니다.

리소스 서버 구현에서 중요한 것은 리소스 서버가 토큰의 유효성을 검사하는 방법을 선택하는 것입니다. 

리소스 서버 수준에서 토큰 유효성 검사를 구현하기 위한 여러 옵션이 있습니다. 첫 번째 옵션은 리소스 서버가 인증 서버를 직접 호출하여 발급된 토큰을 확인할 수 있습니다. 그림 14.2는 이 옵션을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F02_Spilca.png)

**그림 14.2** 토큰을 확인하기 위해 리소스 서버는 인증 서버를 직접 호출합니다. 인증 서버는 특정 토큰을 발급했는지 여부를 알고 있습니다.

두 번째 옵션은 인증 서버가 토큰을 저장하는 공통 데이터베이스를 사용하고 리소스 서버가 토큰에 액세스하고 유효성을 검사할 수 있습니다(그림 14.3). 이 접근 방식을 `블랙보드`라고도 합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F03_Spilca.png)

**그림 14.3** 블랙보드. 인증 서버와 리소스 서버 모두 공유 데이터베이스에 액세스합니다. 인증 서버는 토큰을 발행한 후 이 데이터베이스에 토큰을 저장합니다. 그런 다음 리소스 서버는 리소스 서버에 액세스하여 수신한 토큰의 유효성을 검사할 수 있습니다.

세 번째 옵션은 `암호화 서명`을 사용합니다(그림 14.4). 인증 서버는 토큰을 발급할 때 토큰에 서명하고 리소스 서버는 서명의 유효성을 검사합니다. 여기에서 JSON 웹 토큰(JWT)을 사용합니다. 이 접근 방식은 15장에서 논의합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F04_Spilca.png)

**그림 14.4** 액세스 토큰을 발행할 때 인증 서버는 개인 키를 사용하여 서명합니다. 토큰을 확인하기 위해 리소스 서버는 서명이 유효한지 확인하기만 하면 됩니다.

## 14.1 리소스 서버 구현

토큰을 발행하는 인증 서버가 있는 이유는 클라이언트가 사용자의 리소스에 액세스할 수 있도록 하기 위해서입니다. 리소스 서버는 사용자의 리소스를 관리하고 보호하므로 리소스 서버를 구현하는 방법을 알아야 합니다. 토큰이 유효한지 알아보기 위해 리소스 서버가 직접 인증 서버를 호출할 수 있도록 하는 Spring Boot에서 제공하는 기본 구현을 사용합니다(그림 14.5).

> **NOTE** Authorization 서버의 경우와 마찬가지로 Spring 커뮤니티에서 리소스 서버의 구현이 변경되었습니다. 이러한 변경 사항은 이제 실제로 개발자가 리소스 서버를 구현하는 다양한 방법을 찾을 수 있기 때문에 우리에게 영향을 줍니다. 리소스 서버를 두 가지 방법으로 구성할 수 있는 예제를 제공하여 실제 시나리오에서 두 가지를 모두 이해하고 사용할 수 있습니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F05_Spilca.png)

**그림 14.5** 리소스 서버가 토큰의 유효성을 검사해야 할 때 인증 서버를 직접 호출합니다. 권한 부여 서버가 토큰을 발행했음을 확인하면 리소스 서버는 토큰이 유효한 것으로 간주합니다.

리소스 서버를 구현하기 위해 새 프로젝트를 만들고 다음 코드 조각에서와 같이 종속성을 추가합니다. 이 프로젝트의 이름을 ssia-ch14-ex1-rs로 지정했습니다.

종속성 외에도 spring-cloud-dependencies 아티팩트에 대한 `dependencyManagement` 태그도 추가합니다. 다음은 이 작업을 수행하는 방법을 보여줍니다.
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
리소스 서버의 목적은 사용자의 리소스를 관리하고 보호하는 것이므로 작동 방식을 증명하려면 액세스하려는 리소스가 필요합니다. 다음 목록에 표시된 대로 일반 컨트롤러를 정의하여 테스트에 대한 /hello 끝점을 만듭니다.

**Listing 14.1** The controller class defining the test endpoint
```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
```
추가로 필요한 것은 `@EnableResourceServer` 주석을 사용하여 Spring Boot가 앱이 리소스 서버가 되는 데 필요한 것을 구성할 수 있도록 하는 구성 클래스입니다. 다음 목록은 구성 클래스를 나타냅니다.

**Listing 14.2** The configuration class
```java
@Configuration
@EnableResourceServer
public class ResourceServerConfig {
}
```
이제 리소스 서버가 있으나 토큰을 확인할 수 있는 방법을 구성하지 않았기 때문에 엔드포인트에 액세스할 수 없습니다. 리소스에 대한 요청은 유효한 액세스 토큰도 제공해야 한다는 것을 알고 있습니다. 

유효한 액세스 토큰을 제공하더라도 요청은 여전히 ​​작동하지 않습니다. 리소스 서버는 이것이 유효한 토큰인지, 인증 서버가 실제로 토큰을 발행했는지 확인할 수 없습니다. 리소스 서버가 액세스 토큰의 유효성을 검사하는 데 필요한 옵션을 구현하지 않았기 때문입니다. 이 접근 방식을 취하고 다음 두 섹션에서 옵션에 대해 논의하겠습니다. 15장에서는 추가 옵션을 제공합니다.

> **참고** 이전 참고에서 언급했듯이 리소스 서버 구현도 변경되었습니다. Spring Security OAuth 프로젝트의 일부인 @EnableResourceServer 주석은 최근에 더 이상 사용되지 않는 것으로 표시되었습니다. Spring Security 마이그레이션 가이드(https://github.com/spring-projects/spring-security/wiki/OAuth-2.0-Migration-Guide)에서 Spring Security 팀은 Spring Security에서 직접 구성 방법을 사용하도록 초대합니다. 현재 내가 보는 대부분의 앱에서 여전히 Spring Security OAuth 프로젝트를 사용하고 있습니다. 이러한 이유로 이 장에서 예제로 제시하는 두 가지 접근 방식을 모두 이해하는 것이 중요하다고 생각합니다.

## 14.2 원격으로 토큰 확인하기

리소스 서버가 인증 서버를 직접 호출하도록 허용하여 토큰 유효성 검사를 구현합니다. 이 접근 방식은 유효한 액세스 토큰을 사용하여 리소스 서버에 액세스할 수 있도록 구현할 수 있는 가장 간단한 방법입니다. 시스템의 토큰이 일반인 경우 이 접근 방식을 선택합니다(예: Spring Security가 있는 인증 서버의 기본 구현에서와 같이 단순한 UUID). 먼저 이 접근 방식을 논의한 다음 예제를 통해 구현합니다. 토큰을 검증하는 이 메커니즘은 간단합니다(그림 14.6).

1. 권한 부여 서버가 끝점을 노출합니다. 유효한 토큰의 경우 이전에 발행된 사용자에게 부여된 권한을 반환합니다. 이 끝점을 `check_token` 끝점이라고 합시다.

2. 리소스 서버는 각 요청에 대해 `check_token` 끝점을 호출합니다.
이런 식으로 클라이언트로부터 받은 토큰의 유효성을 검사하고 클라이언트가 부여한 권한도 얻습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F06_Spilca.png)

**그림 14.6** 토큰의 유효성을 검사하고 토큰에 대한 세부 정보를 얻기 위해 리소스 서버는 인증 서버의 엔드포인트 `/oauth/check_token`을 호출합니다. 리소스 서버는 토큰에 대해 검색된 세부 정보를 사용하여 호출을 승인합니다.

이 접근 방식의 장점은 단순성입니다. 모든 종류의 토큰 구현에 적용할 수 있습니다. 이 접근 방식의 단점은 아직 알려지지 않은 새로운 토큰이 있는 리소스 서버에 대한 각 요청에 대해 리소스 서버가 권한 부여 서버를 호출하여 토큰의 유효성을 검사한다는 것입니다. 이러한 호출은 권한 부여 서버에 불필요한 부하를 줄 수 있습니다. 

또한 네트워크는 100% 신뢰할 수 없습니다. 아키텍처에서 새로운 원격 호출을 설계할 때마다 이 점을 염두에 두어야 합니다. 또한 일부 네트워크 불안정으로 인해 호출이 실패할 경우 발생하는 상황에 대해 몇 가지 대체 솔루션을 적용해야 할 수도 있습니다(그림 14.7).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F07_Spilca.png)

**그림 14.7** 네트워크는 100% 신뢰할 수 없습니다. 리소스 서버와 권한 부여 서버 간의 연결이 끊어지면 토큰의 유효성을 검사할 수 없습니다. 이것은 리소스 서버가 유효한 토큰을 가지고 있더라도 사용자의 리소스에 대한 클라이언트 액세스를 거부한다는 것을 의미합니다.

ssia-ch14-ex1-rs 프로젝트에서 리소스 서버 구현을 계속합시다. 목표는 인증 서버에서 발급한 액세스 토큰을 제공하는 경우 클라이언트가 /hello 엔드포인트에 액세스할 수 있도록 하는 것입니다. 이미 13장에서 인증 서버를 개발했습니다. 예를 들어 프로젝트 ssia-ch13-ex1을 인증 서버로 사용할 수 있습니다. 그러나 이전 섹션에서 논의한 프로젝트 변경을 피하기 위해 이 토론을 위한 별도의 프로젝트인 ssia-ch14-ex1-as를 만들었습니다. 

이제 ssia-ch13-ex1 프로젝트와 동일한 구조를 가지며 이 섹션에서 여러분에게 제시하는 것은 현재 논의와 관련하여 제가 변경한 사항일 뿐입니다. 원하는 경우 ssia-ch13-ex2, ssia-ch13-ex3 또는 ssia-ch13-ex4에서 구현한 인증 서버를 사용하여 토론을 계속할 수 있습니다.

> **참고** 여기에서 설명하는 구성을 12장에서 설명한 다른 승인 유형과 함께 사용할 수 있습니다. 승인 유형은 OAuth 2 프레임워크에 의해 구현된 흐름으로 클라이언트가 인증 서버에서 발행한 토큰을 가져옵니다. 따라서 원하는 경우 ssia-ch13-ex2, ssia-ch13-ex3 또는 ssia-ch13-ex4 프로젝트에서 구현한 인증 서버를 사용하여 토론을 계속할 수 있습니다.

기본적으로 권한 부여 서버는 리소스 서버가 토큰의 유효성을 검사하는 데 사용할 수 있는 엔드포인트 `/oauth/check_token`을 구현합니다. 그러나 현재 권한 부여 서버는 해당 끝점에 대한 모든 요청을 암시적으로 거부합니다. /oauth/check_token 엔드포인트를 사용하기 전에 리소스 서버가 이를 호출할 수 있는지 확인해야 합니다.

인증된 요청이 `/oauth/check_token` 끝점을 호출할 수 있도록 하려면 권한 부여 서버의 AuthServerConfig 클래스에서 `configure(AuthorizationServerSecurityConfigurer c)` 메서드를 재정의합니다. configure() 메서드를 재정의하면 `/oauth/check_token` 끝점을 호출할 수 있는 조건을 설정할 수 있습니다. 다음 목록은 이 작업을 수행하는 방법을 보여줍니다.

**Listing 14.3** Enabling authenticated access to the check_token endpoint
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) 
      throws Exception {

      clients.inMemory()
             .withClient("client")
             .secret("secret")
             .authorizedGrantTypes("password", "refresh_token")
             .scopes("read");
  }

  @Override
  public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints.authenticationManager(authenticationManager);
  }
    
  public void configure(
    AuthorizationServerSecurityConfigurer security) {
      security.checkTokenAccess
                ("isAuthenticated()");        ❶
  }
}
```
❶ check_token 끝점을 호출할 수 있는 조건을 지정합니다.

> **참고** isAuthenticated() 대신에 permitAll()을 사용하여 인증 없이 이 끝점에 액세스할 수 있도록 만들 수도 있습니다. 그러나 엔드포인트를 보호되지 않은 상태로 두는 것은 권장되지 않습니다. 실제 시나리오에서는 이 끝점에 대한 인증을 사용하는 것이 좋습니다.

이 끝점을 액세스 가능하게 만드는 것 외에도 인증된 액세스만 허용하기로 결정했다면 리소스 서버 자체에 대한 클라이언트 등록이 필요합니다. 권한 부여 서버의 경우 리소스 서버도 클라이언트이며 자체 자격 증명이 필요합니다. 우리는 다른 클라이언트와 마찬가지로 이것을 추가합니다. 리소스 서버의 경우 권한 부여 유형이나 범위가 필요하지 않으며 리소스 서버가 check_token 끝점을 호출하는 데 사용하는 자격 증명 집합만 있으면 됩니다. 다음 목록은 이 예에서 리소스 서버에 대한 자격 증명을 추가하기 위한 구성 변경 사항을 보여줍니다.

**Listing 14.4** 리소스 서버에 자격증명 추가
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
             .authorizedGrantTypes("password", "refresh_token")
             .scopes("read")
               .and()                           ❶
             .withClient("resourceserver")      ❶
             .secret("resourceserversecret");   ❶
    }
  }
```
❶ Adds a set of credentials for the resource server to use when calling the /oauth/check_token endpoint

You can now start the authorization server and obtain a token like you learned in chapter 13. Here’s the cURL call:
```
curl -v -XPOST -u client:secret "http://localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read"
```

The response body is

```json
{
  "access_token":"4f2b7a6d-ced2-43dc-86d7-cbe844d3e16b",
  "token_type":"bearer",
  "refresh_token":"a4bd4660-9bb3-450e-aa28-2e031877cb36",
  "expires_in":43199,"scope":"read"
}
```
Next, we call the check_token endpoint to find the details about the access token we obtained in the previous code snippet. Here’s that call:
```
curl -XPOST -u resourceserver:resourceserversecret "http://localhost:8080/oauth/check_token?token=4f2b7a6d-ced2-43dc-86d7-cbe844d3e16b"
```

The response body is

```json
{
  "active":true,
  "exp":1581307166,
  "user_name":"john",
  "authorities":["read"],
  "client_id":"client",
  "scope":["read"]
}
```
check_token 끝점에서 반환되는 응답을 관찰합니다. 액세스 토큰에 대해 필요한 모든 세부 정보를 알려줍니다.
- 토큰이 아직 활성 상태인지 여부와 만료 시기
- 토큰이 발행된 사용자
- 특권을 대표하는 권한
- 토큰이 발행된 클라이언트
이제 cURL을 사용하여 엔드포인트를 호출하면 리소스 서버가 이를 사용하여 토큰을 확인할 수 있어야 합니다. 인증 서버의 끝점과 리소스 서버가 끝점에 액세스하는 데 사용하는 자격 증명을 구성해야 합니다. application.properties 파일에서 이 모든 작업을 수행할 수 있습니다. 다음 코드 조각은 세부 정보를 제공합니다.
```
server.port=9090
security.oauth2.resource.token-info-uri=http:/./localhost:8080/oauth/check_token
security.oauth2.client.client-id=resourceserver
security.oauth2.client.client-secret=resourceserversecret
```

그런데 저와 같은 시스템에서 두 응용 프로그램을 모두 실행할 계획이라면 server.port 속성을 사용하여 다른 포트를 설정하는 것을 잊지 마십시오. 인증 서버를 실행하기 위해 포트 8080(기본값)을 사용하고 리소스 서버를 위해 포트 9090을 사용합니다.

/hello 엔드포인트를 호출하여 두 애플리케이션을 모두 실행하고 전체 설정을 테스트할 수 있습니다. 요청의 Authorization 헤더에 액세스 토큰을 설정해야 하고 해당 값에 단어 bearer를 접두사로 붙여야 합니다. 이 단어의 경우 대소문자를 구분하지 않습니다. 즉, "Bearer" 또는 "BEARER"도 쓸 수 있습니다.

```
curl -H "Authorization: bearer 4f2b7a6d-ced2-43dc-86d7-cbe844d3e16b" "http:/./localhost:9090/hello"
```
The response body is

`Hello!`

If you had called the endpoint without a token or with the wrong one, the result would have been a 401 Unauthorized status on the HTTP response. The next code snippet presents the response:

```bsh
curl -v "http:/./localhost:9090/hello"
```

The (truncated) response is

```
...
< HTTP/1.1 401
...
{
  "error":"unauthorized",
  "error_description":"Full authentication is 
    required to access this resource"
}
```
### Spring Security OAuth 없이 토큰 자체 검사 사용

요즘 공통적인 관심사는 Spring Security OAuth 없이 이전 예제와 같이 리소스 서버를 구현하는 방법입니다. Spring Security OAuth가 더 이상 사용되지 않는다고 말하지만 기존 프로젝트에서 이러한 클래스를 찾을 수 있는 좋은 기회가 있기 때문에 여전히 이해해야 한다고 생각합니다. 이 측면을 명확히 하기 위해 Spring Security OAuth 없이 동일한 것을 구현하는 방법과 관련된 비교를 추가합니다. 이 사이드바에서는 Spring Security OAuth를 사용하지 않고 Spring Security 구성으로 직접 토큰 검사를 사용하여 리소스 서버를 구현하는 방법에 대해 설명합니다. 다행히 생각보다 쉽습니다.

기억한다면 이전 장에서 httpBasic(), formLogin() 및 기타 인증 방법에 대해 논의했습니다. 이러한 메서드를 호출할 때 필터 체인에 새 필터를 추가하기만 하면 앱에서 다른 인증 메커니즘을 사용할 수 있다는 것을 배웠습니다. 뭔지 맞춰봐? 최신 버전에서 Spring Security는 리소스 서버 인증 방법을 활성화하는 oauth2ResourceServer() 메서드도 제공합니다. 인증 방법을 설정하기 위해 지금까지 사용했던 다른 방법처럼 사용할 수 있으며 더 이상 종속성에 Spring Security OAuth 프로젝트가 필요하지 않습니다. 그러나 이 기능은 아직 완성되지 않았으며 이를 사용하려면 Spring Boot에서 자동으로 파악하지 못하는 다른 종속성을 추가해야 합니다. 다음 코드 조각은 토큰 자체 검사를 사용하여 리소스 서버를 구현하는 데 필요한 종속성을 나타냅니다.

```xml
<dependency>
   <groupId>org.springframework.security</groupId>
   <artifactId>spring-security-oauth2-resource-server</artifactId>
   <version>5.2.1.RELEASE</version>
</dependency>

<dependency>
   <groupId>com.nimbusds</groupId>
   <artifactId>oauth2-oidc-sdk</artifactId>
   <version>8.4</version>
   <scope>runtime</scope>
</dependency>
```
Once you add the needed dependencies to your pom.xml file, you can configure the authentication method as shown in the next code snippet:

```java
@Configuration
public class ResourceServerConfig
  extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
           .anyRequest().authenticated()
        .and()
           .oauth2ResourceServer(
              c -> c.opaqueToken(
                 o -> {
                   o.introspectionUri("...");
                   o.introspectionClientCredentials("client", "secret");
              })
           );
  }
}
```
코드를 더 쉽게 읽을 수 있도록 introspectionUri() 메서드의 매개 변수 값을 생략했습니다. 이 매개 변수 값은 introspection 토큰 URI라고도 하는 check_token URI입니다. oauth2ResourceServer() 메서드의 매개변수로 Customizer 인스턴스를 추가했습니다. Customizer 인스턴스를 사용하여 선택한 접근 방식에 따라 리소스 서버에 필요한 매개변수를 지정합니다. 직접 토큰 검사의 경우 리소스 서버가 토큰의 유효성을 검사하기 위해 호출하는 URI와 이 URI를 호출할 때 리소스 서버가 인증해야 하는 자격 증명을 지정해야 합니다. 이 예제는 프로젝트 ssia-ch14-ex1-rs-migration 폴더에 구현되어 있습니다.

## 14.3 JdbcTokenStore로 블랙보드 구현하기

인증 서버와 리소스 서버가 공유 데이터베이스를 사용하는 애플리케이션을 구현합니다. 우리는 이것을 건축 스타일의 칠판이라고 부릅니다. 왜 칠판을 쓰나요? 이를 인증 서버와 칠판을 사용하여 토큰을 관리하는 리소스 서버로 생각할 수 있습니다. 토큰을 발급하고 검증하기 위한 이러한 접근 방식은 리소스 서버와 권한 부여 서버 간의 직접 통신을 제거하는 이점이 있습니다. 그러나 이는 병목 현상이 발생할 수 있는 공유 데이터베이스를 추가함을 의미합니다. 모든 건축 스타일과 마찬가지로 다양한 상황에 적용할 수 있습니다. 예를 들어 데이터베이스를 공유하는 서비스가 이미 있는 경우 액세스 토큰에도 이 접근 방식을 사용하는 것이 합리적일 수 있습니다. 이러한 이유로 이 접근 방식을 구현하는 방법을 아는 것이 중요하다고 생각합니다.

이전 구현과 마찬가지로 우리는 애플리케이션에서 이러한 아키텍처를 사용하는 방법을 보여줍니다. 프로젝트에서 이 애플리케이션을 인증 서버의 경우 ssia-ch14-ex2-as, 리소스 서버의 경우 ssia-ch14-ex2-rs로 찾을 수 있습니다. 이 아키텍처는 인증 서버가 토큰을 발행할 때 리소스 서버와 공유되는 데이터베이스에도 토큰을 저장함을 의미합니다(그림 14.8).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F08_Spilca.png)

그림 14.8 권한 부여 서버는 토큰을 발행할 때 공유 데이터베이스에도 토큰을 저장합니다. 이렇게 하면 리소스 서버가 토큰을 가져와 나중에 확인할 수 있습니다.
또한 리소스 서버가 토큰의 유효성을 검사해야 할 때 데이터베이스에 액세스함을 의미합니다(그림 14.9).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F09_Spilca.png)

그림 14.9 리소스 서버는 공유 데이터베이스에서 토큰을 검색합니다. 토큰이 존재하는 경우 리소스 서버는 사용자 이름 및 권한을 포함하여 데이터베이스에서 토큰과 관련된 세부 정보를 찾습니다. 이러한 세부 정보를 사용하여 리소스 서버는 요청을 승인할 수 있습니다.
인증 서버와 리소스 서버 모두에서 Spring Security에서 토큰을 관리하는 객체를 나타내는 계약은 TokenStore입니다. 권한 부여 서버의 경우 이전에 SecurityContext를 사용한 인증 아키텍처에서의 위치를 ​​시각화할 수 있습니다. 인증이 완료되면 인증 서버는 TokenStore를 사용하여 토큰을 생성합니다(그림 14.10).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F11_Spilca.png)

그림 14.10 인증 서버는 토큰 저장소를 사용하여 인증 프로세스가 끝날 때 토큰을 생성합니다. 클라이언트는 이러한 토큰을 사용하여 리소스 서버에서 관리하는 리소스에 액세스합니다.

리소스 서버의 경우 인증 필터는 TokenStore를 사용하여 토큰의 유효성을 검사하고 나중에 인증에 사용하는 사용자 세부 정보를 찾습니다. 그런 다음 리소스 서버는 보안 컨텍스트에 사용자의 세부 정보를 저장합니다(그림 14.11).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH14_F11_Spilca.png)

그림 14.11 리소스 서버는 토큰 저장소를 사용하여 토큰을 확인하고 권한 부여에 필요한 세부 정보를 검색합니다. 이러한 세부 정보는 보안 컨텍스트에 저장됩니다.

> **참고** 권한 부여 서버와 리소스 서버는 두 가지 다른 책임을 구현하지만 반드시 두 개의 별도 응용 프로그램에서 수행할 필요는 없습니다. 대부분의 실제 구현에서는 다른 응용 프로그램에서 이를 개발하므로 이 책의 예제에서도 동일한 작업을 수행합니다. 그러나 동일한 응용 프로그램에서 둘 다 구현하도록 선택할 수 있습니다. 이 경우 호출을 설정하거나 공유 데이터베이스를 가질 필요가 없습니다. 그러나 동일한 앱에서 두 가지 책임을 구현하면 권한 부여 서버와 리소스 서버 모두 동일한 빈에 액세스할 수 있습니다. 따라서 이들은 동일한 것을 사용할 수 있습니다.token store without needing to do network calls or to access a database.

Spring Security는 TokenStore 계약에 대한 다양한 구현을 제공하며 대부분의 경우 자체 구현을 작성할 필요가 없습니다. 예를 들어, 이전의 모든 인증 서버 구현에 대해 TokenStore 구현을 지정하지 않았습니다. Spring Security는 InMemoryTokenStore 유형의 기본 토큰 저장소를 제공했습니다. 상상할 수 있듯이 이 모든 경우에 토큰은 애플리케이션의 메모리에 저장되었습니다. 그들은 지속하지 않았다! 권한 부여 서버를 다시 시작하면 다시 시작하기 전에 발급된 토큰은 더 이상 유효하지 않습니다.

블랙보드로 토큰 관리를 구현하기 위해 Spring Security는 JdbcTokenStore 구현을 제공합니다. 이름에서 알 수 있듯이 이 토큰 저장소는 JDBC를 통해 직접 데이터베이스와 함께 작동합니다. 3장에서 논의한 JdbcUserDetailsManager와 유사하게 작동하지만 사용자를 관리하는 대신 JdbcTokenStore가 토큰을 관리합니다.

> 참고 이 예제에서는 JdbcTokenStore를 사용하여 블랙보드를 구현합니다. 그러나 토큰을 유지하고 /oauth/check_token 끝점을 계속 사용하기 위해 TokenStore를 사용하도록 선택할 수 있습니다. 공유 데이터베이스를 사용하고 싶지 않지만 인증 서버가 다시 시작되더라도 이전에 발행된 토큰을 계속 사용할 수 있도록 토큰을 유지해야 하는 경우 그렇게 하도록 선택합니다.

JdbcTokenStore는 데이터베이스에 두 개의 테이블이 있을 것으로 예상합니다. 하나의 테이블을 사용하여 액세스 토큰(이 테이블의 이름은 oauth_access _token이어야 함)을 저장하고 하나의 테이블을 사용하여 새로 고침 토큰을 저장합니다(이 테이블의 이름은 oauth_refresh_token이어야 함). 토큰을 저장하는 데 사용되는 테이블은 새로 고침 토큰을 유지합니다.

> **참고** 3장에서 논의한 JdbcUserDetailsManager 구성 요소의 경우와 같이 테이블이나 열에 다른 이름을 사용하도록 JdbcTokenStore를 사용자 정의할 수 있습니다. JdbcTokenStore 메소드는 토큰의 세부 정보를 검색하거나 저장하는 데 사용하는 SQL 쿼리를 재정의해야 합니다. 짧게 유지하기 위해 이 예에서는 기본 이름을 사용합니다.

pom.xml 파일을 변경하여 데이터베이스에 연결하는 데 필요한 종속성을 선언해야 합니다. 다음 코드 조각은 pom.xml 파일에서 사용하는 종속성을 나타냅니다.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
</dependency>
```

권한 부여 서버 프로젝트 `ssia-ch14-ex2-as`에서 이러한 테이블의 구조를 생성하는 데 필요한 쿼리로 `schema.sql` 파일을 정의합니다. 이 파일은 애플리케이션이 시작될 때 Spring Boot에 의해 선택될 리소스 폴더에 있어야 한다는 것을 잊지 마십시오. 다음 코드 조각은 `schema.sql` 파일에 표시된 두 테이블의 정의를 나타냅니다.

```sql
CREATE TABLE IF NOT EXISTS `oauth_access_token` (
    `token_id` varchar(255) NOT NULL,
    `token` blob,
    `authentication_id` varchar(255) DEFAULT NULL,
    `user_name` varchar(255) DEFAULT NULL,
    `client_id` varchar(255) DEFAULT NULL,
    `authentication` blob,
    `refresh_token` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`token_id`));

CREATE TABLE IF NOT EXISTS `oauth_refresh_token` (
    `token_id` varchar(255) NOT NULL,
    `token` blob,
    `authentication` blob,
    PRIMARY KEY (`token_id`));
```
In the application.properties file, you need to add the definition of the data source. The next code snippet provides the definition:

```yml
spring.datasource.url=jdbc:mysql://localhost/spring?useLegacyDatetimeCode=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.datasource.initialization-mode=always
```
The following listing presents the AuthServerConfig class the way we used it in the first example.

Listing 14.5 The AuthServerConfig class
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) 
    throws Exception {

    clients.inMemory()
           .withClient("client")
           .secret("secret")
           .authorizedGrantTypes("password", "refresh_token")
           .scopes("read");
   }

   @Override
   public void configure(
     AuthorizationServerEndpointsConfigurer endpoints) {
       endpoints.authenticationManager(authenticationManager);
   }
}
```
We change this class to inject the data source and then define and configure the token store. The next listing shows this change.

Listing 14.6 Defining and configuring JdbcTokenStore
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private DataSource dataSource; ❶

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) 
    throws Exception {

      clients.inMemory()
             .withClient("client")
             .secret("secret")
             .authorizedGrantTypes("password", "refresh_token")
             .scopes("read");
  }

  @Override
  public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints
        .authenticationManager(authenticationManager)
        .tokenStore(tokenStore());                    ❷
  }

  @Bean
  public TokenStore tokenStore() {                    ❸
      return new JdbcTokenStore(dataSource);          ❸
  }                                                   ❸
}
```
❶ application.properties 파일에서 구성한 데이터 소스를 삽입합니다.

❷ 토큰 저장소 구성

❸ application.properties 파일에 구성된 데이터 소스를 통해 데이터베이스에 대한 액세스를 제공하는 JdbcTokenStore의 인스턴스를 생성합니다.

이제 인증 서버를 시작하고 토큰을 발행할 수 있습니다. 우리는 13장과 이 장의 앞부분에서 했던 것과 같은 방식으로 토큰을 발행합니다. 이 관점에서 보면 아무것도 바뀌지 않았습니다. 그러나 이제 데이터베이스에 저장된 토큰도 볼 수 있습니다. 다음은 토큰을 발행하는 데 사용하는 cURL 명령을 보여줍니다.

```
curl -v -XPOST -u client:secret "http://localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read"
```

The response body is

```json
{
  "access_token":"009549ee-fd3e-40b0-a56c-6d28836c4384",
  "token_type":"bearer",
  "refresh_token":"fd44d772-18b3-4668-9981-86373017e12d",
  "expires_in":43199,
  "scope":"read"
}
```
응답에서 반환된 액세스 토큰은 `oauth_access_token` 테이블의 레코드로도 찾을 수 있습니다. 새로 고침 토큰 부여 유형을 구성하기 때문에 새로 고침 토큰을 받습니다. 이러한 이유로 `oauth_refresh_token` 테이블에서 새로 고침 토큰에 대한 레코드도 찾습니다. 데이터베이스가 토큰을 유지하기 때문에 리소스 서버는 권한 부여 서버가 다운되거나 다시 시작된 후에도 발행된 토큰의 유효성을 검사할 수 있습니다.

이제 동일한 데이터베이스를 사용하도록 리소스 서버를 구성할 시간입니다. 이를 위해 `ssia-ch14-ex2-rs` 프로젝트에서 일합니다. 섹션 14.1에서 작업한 구현으로 시작합니다. 인증 서버의 경우 pom.xml 파일에 필요한 종속성을 추가해야 합니다. 리소스 서버는 데이터베이스에 연결해야 하므로 spring-boot-starter-jdbc 종속성과 JDBC 드라이버도 추가해야 합니다. 다음 코드 조각은 pom.xml 파일의 종속성을 보여줍니다.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
</dependency>
```
`application.properties` 파일에서 리소스 서버가 권한 부여 서버와 동일한 데이터베이스에 연결할 수 있도록 데이터 소스를 구성합니다. 다음 코드 조각은 리소스 서버에 대한 application.properties 파일의 내용을 보여줍니다.

```yml
server.port=9090

spring.datasource.url=jdbc:mysql://localhost/spring
spring.datasource.username=root
spring.datasource.password=
```
리소스 서버의 구성 클래스에서 데이터 소스를 주입하고 JdbcTokenStore를 구성합니다. 다음 목록은 리소스 서버의 구성 클래스에 대한 변경 사항을 보여줍니다.

Listing 14.7 The configuration class for the resource server
```java
@Configuration
@EnableResourceServer
public class ResourceServerConfig 
  extends ResourceServerConfigurerAdapter {

  @Autowired
  private DataSource dataSource;                ❶

  @Override
  public void configure(
    ResourceServerSecurityConfigurer resources) {
    
    resources.tokenStore(tokenStore());         ❷
  }

  @Bean
  public TokenStore tokenStore() {
    return new JdbcTokenStore(dataSource);      ❸
  }
}
```
❶ `application.properties` 파일에서 구성한 데이터 소스를 삽입합니다.

❷ 토큰 저장소 구성

❸ 주입된 데이터 소스를 기반으로 JdbcTokenStore 생성

이제 리소스 서버도 시작하고 이전에 발급한 액세스 토큰으로 /hello 엔드포인트를 호출할 수 있습니다. 다음 코드 조각은 cURL을 사용하여 끝점을 호출하는 방법을 보여줍니다.

```bsh
curl -H "Authorization:Bearer 009549ee-fd3e-40b0-a56c-6d28836c4384" "http://localhost:9090/hello"
```
The response body is
```
Hello!
```
환상적이야! 이 섹션에서는 리소스 서버와 권한 부여 서버 간의 통신을 위한 블랙보드 접근 방식을 구현했습니다. JdbcTokenStore라는 TokenStore 구현을 사용했습니다. 이제 데이터베이스에서 토큰을 유지할 수 있으며 토큰 유효성 검사를 위해 리소스 서버와 권한 부여 서버 간의 직접 호출을 피할 수 있습니다. 그러나 인증 서버와 리소스 서버가 모두 동일한 데이터베이스에 의존하는 것은 단점이 있습니다. 요청이 많은 경우 이 종속성이 병목 현상이 되어 시스템 속도를 저하시킬 수 있습니다. 공유 데이터베이스 사용을 피하기 위해 다른 구현 옵션이 있습니까? 예; 15장에서 JWT와 함께 서명된 토큰을 사용하는 이 장에서 제시한 접근 방식에 대한 대안을 논의할 것입니다.

> 참고 Spring Security OAuth 없이 리소스 서버의 설정을 작성하면 블랙보드 접근 방식을 사용할 수 없습니다.

## 14.4 접근 방식의 짧은 비교

이 장에서는 리소스 서버가 클라이언트로부터 받는 토큰의 유효성을 검사할 수 있도록 하는 두 가지 접근 방식을 구현하는 방법을 배웠습니다.

- 인증 서버를 직접 호출합니다. 리소스 서버가 토큰의 유효성을 검사해야 하는 경우 해당 토큰을 발급하는 권한 부여 서버를 직접 호출합니다.

- 공유 데이터베이스 사용(블랙보드). 권한 부여 서버와 리소스 서버는 모두 동일한 데이터베이스에서 작동합니다. 인증 서버는 발급된 토큰을 데이터베이스에 저장하고 리소스 서버는 유효성 검사를 위해 토큰을 읽습니다.

간단히 요약해 보겠습니다. 표 14.1에서 이 장에서 논의된 두 가지 접근 방식의 장점과 단점을 찾을 수 있습니다.

표 14.1 리소스 서버가 토큰을 검증하기 위해 제시된 접근 방식을 구현할 때의 장단점
접근 장점 단점

인증 서버 직접 호출 구현하기 쉽습니다.
모든 토큰 구현에 적용할 수 있습니다. 이는 권한 부여 서버와 리소스 서버 간의 직접적인 종속성을 의미합니다.

권한 부여 서버에 불필요한 스트레스를 유발할 수 있습니다.
공유 데이터베이스 사용(블랙보드) 권한 부여 서버와 리소스 서버 간의 직접 통신이 필요하지 않습니다.

모든 토큰 구현에 적용할 수 있습니다.
토큰을 유지하면 권한 부여 서버가 다시 시작된 후 또는 권한 부여 서버가 다운된 경우 권한 부여가 작동할 수 있습니다. 인증 서버를 직접 호출하는 것보다 구현하기가 더 어렵습니다.

시스템에 하나 이상의 구성요소인 공유 데이터베이스가 필요합니다.
공유 데이터베이스는 병목 현상이 되어 시스템 성능에 영향을 줄 수 있습니다.

## 요약

- 리소스 서버는 사용자 리소스를 관리하는 Spring 컴포넌트입니다.

- 리소스 서버는 인증 서버가 클라이언트에게 발급한 토큰을 검증하는 방법이 필요합니다.

- 리소스 서버에 대한 토큰을 확인하는 한 가지 옵션은 인증 서버를 직접 호출하는 것입니다. 이 접근 방식은 권한 부여 서버에 너무 많은 스트레스를 유발할 수 있습니다. 나는 일반적으로 이 접근 방식을 사용하지 않습니다.

- 리소스 서버가 토큰의 유효성을 검사할 수 있도록 블랙보드 아키텍처를 구현하도록 선택할 수 있습니다. 이 구현에서 권한 부여 서버와 리소스 서버는 토큰을 관리하는 동일한 데이터베이스에 액세스합니다.

- 블랙보드는 리소스 서버와 권한 서버 간의 직접적인 종속성을 제거할 수 있는 장점이 있습니다. 그러나 이는 토큰을 유지하기 위해 데이터베이스를 추가하는 것을 의미하며, 이는 많은 요청의 경우 병목 현상이 되고 시스템 성능에 영향을 미칠 수 있습니다.

- 토큰 관리를 구현하려면 TokenStore 유형의 개체를 사용해야 합니다. TokenStore의 자체 구현을 작성할 수 있지만 대부분의 경우 Spring Security에서 제공하는 구현을 사용합니다.

- JdbcTokenStore는 데이터베이스에서 액세스 및 새로 고침 토큰을 유지하는 데 사용할 수 있는 TokenStore 구현입니다.

 