# 15 OAuth 2: JWT 및 암호화 서명 사용

이 장에서는 다음을 다룹니다.

- 암호화 서명을 사용한 토큰 검증
- OAuth 2 아키텍처에서 JSON 웹 토큰 사용
- 대칭 및 비대칭 키로 토큰 서명
- JWT에 사용자 정의 세부 정보 추가
  
토큰 구현을 위해 JSON 웹 토큰(JWT)을 사용하는 방법에 대해 설명합니다. 14장에서 리소스 서버가 권한 부여 서버에서 발행한 토큰의 유효성을 검사해야 한다는 것과 세 가지 방법을 다루었습니다.

- 14.2에서 구현한 리소스 서버와 권한 부여 서버 간의 직접 호출 사용

- 14.3에서 구현한 토큰 저장을 위한 공유 데이터베이스 사용

- 이 장에서 논의할 암호화 서명 사용

토큰의 유효성을 검사하기 위해 암호화 서명을 사용하면 권한 서버를 직접 호출하거나 공유 데이터베이스가 필요하지 않고 리소스 서버가 토큰의 유효성을 검사할 수 있다는 이점이 있습니다. 토큰 유효성 검사를 구현하는 이 접근 방식은 OAuth 2로 인증 및 권한 부여를 구현하는 시스템에서 일반적으로 사용됩니다. 

이러한 이유로 토큰 유효성 검사를 구현하는 이 방법을 알아야 합니다. 14장에서 다른 두 가지 방법에 대해 했던 것처럼 이 방법에 대한 예를 작성할 것입니다.

## 15.1 JWT를 사용하여 대칭 키로 서명된 토큰 사용

토큰 서명에 대한 가장 간단한 접근 방식은 대칭 키를 사용하는 것입니다. 이 접근 방식을 사용하면 동일한 키를 사용하여 토큰에 서명하고 서명의 유효성을 검사할 수 있습니다. 토큰 서명에 대칭 키를 사용하는 것은 이 장의 뒷부분에서 논의할 다른 접근 방식보다 간단하고 빠릅니다. 그러나 토큰 서명에 사용되는 키를 인증 프로세스와 관련된 모든 애플리케이션과 항상 공유할 수는 없습니다. 15.2에서 대칭 키와 비대칭 키 쌍을 비교할 때 이러한 장단점에 대해 논의할 것입니다.

지금은 대칭 키로 서명된 JWT를 사용하는 시스템을 구현하는 새 프로젝트를 시작하겠습니다. 이 구현을 위해 프로젝트 이름을 인증 서버로 ssia-ch15-ex1-as로, 리소스 서버로 ssia-ch15-ex1-rs로 이름을 지정했습니다. 11장에서 자세히 설명한 JWT에 대한 간략한 요약으로 시작합니다. 그런 다음 예제에서 이를 구현합니다.

## 15.1.1 JWTS 사용

11장에서 JWT에 대해 자세히 논의했지만 JWT의 작동 방식에 대한 복습으로 시작하는 것이 가장 좋습니다. 그런 다음 인증 서버와 리소스 서버를 계속 구현합니다. 이 장에서 논의하는 모든 것은 JWT에 의존하므로 첫 번째 예를 더 진행하기 전에 이 복습부터 시작하는 것이 중요하다고 생각합니다.

JWT는 토큰 구현입니다. 토큰은 헤더, 본문 및 서명의 세 부분으로 구성됩니다. 헤더 및 본문의 세부 정보는 JSON으로 표시되며 Base64로 인코딩됩니다. 세 번째 부분은 헤더와 본문을 입력으로 사용하는 암호화 알고리즘을 사용하여 생성된 서명입니다(그림 15.1). 암호화 알고리즘은 또한 키가 필요함을 의미합니다. 키는 암호와 같습니다. 적절한 키를 가진 사람은 토큰에 서명하거나 서명이 인증되었는지 확인할 수 있습니다. 토큰의 서명이 인증된 경우 서명된 후 아무도 토큰을 변경하지 않았음을 보장합니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH15_F01_Spilca.png)

**그림 15.1** JWT는 헤더, 본문 및 서명의 세 부분으로 구성됩니다. 헤더와 본문에는 JSON으로 표시되는 세부 정보가 포함됩니다. 이러한 부분은 Base64로 인코딩된 다음 서명됩니다. 토큰은 점으로 구분된 이 세 부분으로 구성된 문자열입니다.

JWT가 서명되면 JWS(JSON Web Token Signed)라고도 합니다. 일반적으로 토큰 서명에 암호화 알고리즘을 적용하면 충분하지만 때로는 암호화하도록 선택할 수도 있습니다. 토큰이 서명되면 키나 암호 없이도 내용을 볼 수 있습니다. 그러나 해커는 토큰의 내용을 보았더라도 토큰의 내용을 변경할 수 없습니다. 그렇게 하면 서명이 무효화되기 때문입니다(그림 15.2). 서명이 유효하려면

- 올바른 키로 생성
- 서명된 내용과 일치

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH15_F02_Spilca.png)

**그림 15.2** 해커가 토큰을 가로채 내용을 변경합니다. 토큰의 서명이 더 이상 콘텐츠와 일치하지 않기 때문에 리소스 서버가 호출을 거부합니다.

토큰이 암호화되면 JWE(JSON Web Token Encrypted)라고도 합니다. 유효한 키가 없으면 암호화된 토큰의 내용을 볼 수 없습니다.

### 15.1.2 JWTS 발행을 위한 인증 서버 구현

인증을 위해 JWT를 클라이언트에 발행하는 인증 서버를 구현합니다. 14장에서 토큰을 관리하는 구성 요소가 TokenStore라는 것을 배웠습니다. 여기서는 Spring Security에서 제공하는 TokenStore의 또 다른 구현인 JwtTokenStore를 사용하여 JWT를 관리합니다. 

또한 인증 서버도 테스트합니다. 나중에 15.1.3에서 리소스 서버를 구현하고 JWT를 사용하는 완전한 시스템을 갖게 됩니다. 다음 두 가지 방법으로 JWT를 사용하여 토큰 유효성 검사를 구현할 수 있습니다.

- 토큰 서명과 서명 확인에 동일한 키를 사용하면 키가 대칭이라고 합니다.

- 하나의 키로 서명하고 다른 키로 서명을 확인하는 경우 비대칭 키 쌍을 사용한다고 합니다.

이 예에서는 대칭 키로 서명을 구현합니다. 이 방식은 권한 부여 서버와 리소스 서버가 모두 동일한 키를 알고 사용함을 의미합니다. 인증 서버는 키로 토큰에 서명하고 리소스 서버는 동일한 키를 사용하여 서명을 확인합니다(그림 15.3).
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH15_F03_Spilca.png)
 
그림 15.3 대칭 키 사용. 인증 서버와 리소스 서버는 모두 동일한 키를 공유합니다. 인증 서버는 키를 사용하여 토큰에 서명하고 리소스 서버는 키를 사용하여 서명을 확인합니다.

프로젝트 이름은 ssia-ch15-ex1-as입니다. 다음 코드는 추가해야 하는 종속성을 나타냅니다. 이것은 13장과 14장에서 인증 서버에 사용한 것과 동일합니다.
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
```
14장에서 JdbcTokenStore에 대해 했던 것과 같은 방식으로 JwtTokenStore를 구성합니다. 또한 JwtAccessTokenConverter 유형의 개체를 정의해야 합니다. JwtAccessTokenConverter를 사용하여 인증 서버가 토큰의 유효성을 검사하는 방법을 구성합니다. 여기서는 대칭 키를 사용합니다. 다음 목록은 구성 클래스에서 JwtTokenStore를 구성하는 방법을 보여줍니다.

**Listing 15.1** JwtTokenStore 설정
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  @Value("${jwt.key}")
  private String jwtKey; ❶

  @Autowired
  private AuthenticationManager authenticationManager;

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
        .tokenStore(tokenStore()) ❷
        .accessTokenConverter(jwtAccessTokenConverter()); ❷
  }
  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(jwtAccessTokenConverter()); ❸
  }
  @Bean
  public JwtAccessTokenConverter jwtAccessTokenConverter() {
    var converter = new JwtAccessTokenConverter();
    converter.setSigningKey(jwtKey); ❹
    return converter;
  }
}
```
❶ application.properties 파일에서 대칭 키 값을 가져옵니다.

❷ 토큰 저장소 및 액세스 토큰 변환기 개체 구성

❸ 연결된 액세스 토큰 변환기를 사용하여 토큰 저장소를 만듭니다.

❹ 액세스 토큰 변환기 개체의 대칭 키 값을 설정합니다.

다음 코드에서 볼 수 있듯이 이 예제의 대칭 키 값을 application.properties 파일에 저장했습니다. 그러나 서명 키는 민감한 데이터라는 사실을 잊지 마십시오. 실제 시나리오에서는 이를 비밀 금고에 저장해야 합니다.

```yaml
jwt.key=MjWP5L7CiD
```

13장과 14장의 인증 서버에 대한 이전 예제에서 모든 인증 서버에 대해 UserDetailsServer 및 PasswordEncoder도 정의한다는 것을 기억하십시오. 목록 15.2는 권한 부여 서버에 대해 이러한 구성요소를 구성합니다. 

**Listing 15.2** 인증 서버를 위한 사용자 관리 설정
```java
@Configuration
public class WebSecurityConfig 
  extends WebSecurityConfigurerAdapter {

  @Bean
  public UserDetailsService uds() {
    var uds = new InMemoryUserDetailsManager(); 
    var u = User.withUsername("john")
                .password("12345")
                .authorities("read")
                .build();
    uds.createUser(u);
    return uds;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Bean
  public AuthenticationManager authenticationManagerBean() 
    throws Exception {
      return super.authenticationManagerBean();
  }
}
```
이제 권한 부여 서버를 시작하고 /oauth/token 엔드포인트를 호출하여 액세스 토큰을 얻을 수 있습니다. 다음은 /oauth/token 엔드포인트를 호출하는 cURL 명령을 보여줍니다.

```bsh
curl -v -XPOST -u client:secret "http://localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read"
```
응답 

```json
{
  "access_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXV...",
  "token_type":"bearer",
  "refresh_token":"eyJhbGciOiJIUzI1NiIsInR5cCI6Ikp...",
  "expires_in":43199,
  "scope":"read",
  "jti":"7774532f-b74b-4e6b-ab16-208c46a19560"
}
```
응답에서 액세스 토큰과 리프레시 토큰이 모두 이제 JWT임을 확인할 수 있습니다. 코드 에서 더 읽기 쉽게 만들기 위해 토큰을 줄였습니다. 콘솔의 응답에서 토큰이 훨씬 더 긴 것을 볼 수 있습니다. 다음 코드에서 토큰 본문의 디코딩된(JSON) 형식을 찾습니다.

```json
{
  "user_name": "john",
  "scope": [
    "read"
  ],

  "generatedInZone": "Europe/Bucharest",
  "exp": 1583874061,
  "authorities": [
    "read"
  ],

  "jti": "38d03577-b6c8-47f5-8c06-d2e3a713d986",
  "client_id": "client"
}
```
인증 서버를 설정했으면 이제 리소스 서버를 구현할 수 있습니다.

### 15.1.3 JWT를 사용하는 리소스 서버 구현

대칭 키를 사용하여 15.1.2에서 설정한 권한 부여 서버에서 발행한 토큰의 유효성을 검사하는 리소스 서버를 구현합니다. 이 섹션의 끝에서 대칭 키를 사용하여 서명된 JWT를 사용하는 완전한 OAuth 2 시스템을 작성하는 방법을 알게 될 것입니다. 새 프로젝트를 만들고 필요한 종속성을 pom.xml에 추가합니다. 이 프로젝트의 이름을 ssia-ch15-ex1-rs로 지정했습니다.

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
```
13장과 14장에서 이미 사용한 것에 새로운 종속성을 추가하지 않았습니다. 보안을 위해 하나의 끝점이 필요하기 때문에 컨트롤러와 메서드를 정의하여 리소스 서버를 테스트하는 데 사용하는 간단한 끝점을 노출합니다. 다음 목록은 컨트롤러를 정의합니다.

**Listing 15.3** The HelloController class
```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
```
이제 보호할 엔드포인트가 있으므로 TokenStore를 구성하는 구성 클래스를 선언할 수 있습니다. 인증 서버에 대해 수행하는 것처럼 리소스 서버에 대해 TokenStore를 구성합니다. 가장 중요한 측면은 키에 대해 동일한 값을 사용하는지 확인하는 것입니다. 리소스 서버는 토큰의 서명을 확인하기 위해 키가 필요합니다. 다음 목록은 리소스 서버 구성 클래스를 정의합니다.

**Listing 15.4** 리소스 서버 설정 클래스
```java
@Configuration
@EnableResourceServer
public class ResourceServerConfig 
  extends ResourceServerConfigurerAdapter {

  @Value("${jwt.key}") ❶
  private String jwtKey;

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    resources.tokenStore(tokenStore()); ❷
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(jwtAccessTokenConverter()); ❸
  }

  @Bean
  public JwtAccessTokenConverter jwtAccessTokenConverter() {
    var converter = new JwtAccessTokenConverter(); ❹
    converter.setSigningKey(jwtKey); ❹
    return converter; ❹
  }
}
```
❶ application.properties 파일에서 키 값 삽입

❷ TokenStore 설정

❸ TokenStore 선언 및 Spring 컨텍스트에 추가

❹ 액세스 토큰 변환기를 만들고 토큰 서명을 확인하는 데 사용되는 대칭 키를 설정합니다.

> **참고** application.properties 파일에서 키 값을 설정하는 것을 잊지 마십시오.

대칭 암호화 또는 서명에 사용되는 키는 임의의 바이트 문자열입니다. 무작위성을 위한 알고리즘을 사용하여 생성합니다. 이 예에서는 "abcde"와 같이 모든 문자열 값을 사용할 수 있습니다. 실제 시나리오에서는 길이가 바람직하게는 258바이트보다 긴 무작위로 생성된 값을 사용하는 것이 좋습니다. 자세한 내용은 David Wong의 Real-World Cryptography(Manning, 2020)를 권장합니다. David Wong의 책 8장에서 무작위성과 비밀에 대한 자세한 논의를 볼 수 있습니다.

https://livebook.manning.com/book/real-world-cryptography/chapter-8/

인증 서버와 리소스 서버를 모두 동일한 시스템에서 로컬로 실행하기 때문에 이 애플리케이션에 대해 다른 포트를 구성해야 합니다. 다음은 application.properties 파일의 내용을 나타냅니다.

```yaml
server.port=9090
jwt.key=MjWP5L7CiD
```

이제 리소스 서버를 시작하고 이전에 인증 서버에서 얻은 유효한 JWT를 사용하여 /hello 끝점을 호출할 수 있습니다. 이 예에서 "Bearer"라는 접두사가 붙은 요청의 Authorization HTTP 헤더에 토큰을 추가해야 합니다. 다음 코드 조각은 cURL을 사용하여 끝점을 호출하는 방법을 보여줍니다.

```bsh
curl -H "Authorization:Bearer eyJhbGciOiJIUzI1NiIs..." http://localhost:9090/hello
```
 
응답 본문은
```
Hello!
```

토큰 구현으로 JWT와 함께 OAuth 2를 사용하는 시스템 구현을 마쳤습니다. 알다시피 Spring Security는 이 구현을 쉽게 만듭니다. 이 섹션에서는 대칭 키를 사용하여 토큰에 서명하고 유효성을 검사하는 방법을 배웠습니다. 

그러나 인증 서버와 리소스 서버 모두에서 동일한 키를 갖는 것이 불가능한 실제 시나리오에서 요구 사항을 찾을 수 있습니다. 15.2에서는 이러한 시나리오의 토큰 유효성 검사를 위해 비대칭 키를 사용하는 유사한 시스템을 구현하는 방법을 배웁니다.

### Spring Security OAuth 프로젝트 없이 대칭 키 사용

14장에서 논의한 것처럼 oauth2ResourceServer()와 함께 JWT를 사용하도록 리소스 서버를 구성할 수도 있습니다. 언급했듯이 이 접근 방식은 향후 프로젝트에 더 적합하지만 기존 앱에서 찾을 수 있습니다. 따라서 향후 구현 및 물론 기존 프로젝트를 마이그레이션하려는 경우 이 접근 방식을 알아야 합니다. 다음 코드는 Spring Security OAuth 프로젝트의 클래스 없이 대칭 키를 사용하여 JWT 인증을 구성하는 방법을 보여줍니다.

```java
@Configuration
public class ResourceServerConfig 
  extends WebSecurityConfigurerAdapter {

  @Value("${jwt.key}")
  private String jwtKey;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
           .anyRequest().authenticated()
        .and()
           .oauth2ResourceServer(
              c -> c.jwt( 
                    j -> j.decoder(jwtDecoder());
            ));
  }

  // Omitted code
}
(continued)
```

보시다시피 이번에는 oauth2ResourceServer()에 매개변수로 전송된 Customizer 객체의 jwt() 메서드를 사용합니다. jwt() 메서드를 사용하여 앱에서 토큰을 확인하는 데 필요한 세부 정보를 구성합니다. 이 경우 대칭 키를 사용한 유효성 검사에 대해 논의하기 때문에 동일한 클래스에 JwtDecoder를 만들어 대칭 키 값을 제공합니다. 다음 코드 조각은 디코더() 메서드를 사용하여 이 디코더를 설정하는 방법을 보여줍니다. 

```java
@Bean
public JwtDecoder jwtDecoder() {
  byte [] key = jwtKey.getBytes();
  SecretKey originalKey = new SecretKeySpec(key, 0, key.length, "AES");

  NimbusJwtDecoder jwtDecoder =
    NimbusJwtDecoder.withSecretKey(originalKey)
                    .build();

    return jwtDecoder;
}
```
우리가 구성한 요소는 동일합니다! 이 접근 방식을 사용하여 리소스 서버를 설정하기로 선택한 경우 구문만 다릅니다. 이 예제는 ssia-ch15-ex1-rs-migration 프로젝트에서 구현되었습니다.

## 15.2 JWT에서 비대칭 키로 서명된 토큰 사용하기

인증 서버와 리소스 서버가 비대칭 키 쌍을 사용하여 토큰에 서명하고 유효성을 검사하는 OAuth 2 인증의 예를 구현합니다. 15.1처럼 인증 서버와 리소스 서버가 공유하는 키만 갖는 것은 불가능한 경우가 대부분입니다. 이 시나리오는 권한 부여 서버와 리소스 서버가 동일한 조직에서 개발되지 않은 경우에 발생합니다. 이 경우 권한 부여 서버가 리소스 서버를 "신뢰"하지 않으므로 권한 부여 서버가 리소스 서버와 키를 공유하는 것을 원하지 않는다고 말합니다. 그리고 대칭 키를 사용하면 리소스 서버가 너무 많은 권한을 갖게 됩니다. 즉, 토큰의 유효성을 검사할 뿐만 아니라 서명할 수도 있습니다(그림 15.4).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH15_F04_Spilca.png)

그림 15.4 해커가 어떻게든 대칭 키를 얻을 수 있다면 토큰을 변경하고 서명할 수 있습니다. 그렇게 하면 사용자의 리소스에 액세스할 수 있습니다.

> **참고** 다른 프로젝트에서 컨설턴트로 일하면서 메일 또는 기타 보안되지 않은 채널을 통해 대칭 키를 교환하는 경우를 봅니다. 대칭 키는 개인 키입니다. 그러한 키가 있는 사람은 이를 사용하여 시스템에 액세스할 수 있습니다. 제 경험 법칙은 시스템 외부에서 키를 공유해야 하는 경우 대칭이 아니어야 한다는 것입니다.

인증 서버와 리소스 서버 간의 신뢰할 수 있는 관계를 가정할 수 없을 때 비대칭 키 쌍을 사용합니다. 이러한 이유로 이러한 시스템을 구현하는 방법을 알아야 합니다. 이 섹션에서는 이 목표를 달성하는 방법에 필요한 모든 측면을 보여주는 예제를 작업합니다.

비대칭 키 쌍이란 무엇이며 어떻게 작동합니까? 개념은 아주 간단합니다. 비대칭 키 쌍에는 두 개의 키가 있습니다. 하나는 개인 키라고 하고 다른 하나는 공개 키라고 합니다. 인증 서버는 개인 키를 사용하여 토큰에 서명하고 누군가는 개인 키를 사용해야만 토큰에 서명할 수 있습니다(그림 15.5).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH15_F05_Spilca.png)

그림 15.5 토큰에 서명하려면 누군가 개인 키를 사용해야 합니다. 그런 다음 키 쌍의 공개 키는 서명자의 신원을 확인하기 위해 누구나 사용할 수 있습니다.

공개 키는 개인 키와 연결되어 있으므로 쌍이라고 합니다. 그러나 공개 키는 서명을 확인하는 데만 사용할 수 있습니다. 아무도 공개 키를 사용하여 토큰에 서명할 수 없습니다(그림 15.6).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH15_F06_Spilca.png)

**그림 15.6** 해커가 공개 키를 획득하면 토큰 서명에 사용할 수 없습니다. 공개 키는 서명을 확인하는 데만 사용할 수 있습니다.

### 15.2.1 키 쌍 생성

비대칭 키 쌍을 생성하는 방법을 알려드립니다. 15.2.2 및 15.2.3에서 구현한 인증 서버와 리소스 서버를 구성하려면 키 쌍이 필요합니다. 이것은 비대칭 키 쌍입니다(즉, 권한 부여 서버가 토큰에 서명하는 데 사용하는 개인 부분과 서명을 확인하기 위해 리소스 서버가 사용하는 공개 부분이 있음을 의미합니다). 키 쌍을 생성하기 위해 사용하기 쉬운 두 가지 명령줄 도구인 keytool과 OpenSSL을 사용합니다. JDK는 keytool을 설치하므로 컴퓨터에 이미 있을 수 있습니다. OpenSSL의 경우 https://www.openssl.org/에서 다운로드해야 합니다. OpenSSL과 함께 제공되는 Git Bash를 사용하면 별도로 설치할 필요가 없습니다. 이러한 도구를 별도로 설치할 필요가 없기 때문에 항상 이러한 작업에 Git Bash를 사용하는 것을 선호합니다. 도구가 있으면 두 가지 명령을 실행하여

- 개인 키 생성

- 이전에 생성된 개인 키에 대한 공개 키를 얻습니다.
  
#### 개인 키 생성

개인 키를 생성하려면 다음 코드 스니펫에서 keytool 명령을 실행하십시오. ssia.jks라는 파일에 개인 키를 생성합니다. 또한 개인 키를 보호하기 위해 암호 "ssia123"을 사용하고 키 이름을 지정하기 위해 별칭 "ssia"를 사용합니다. 다음 명령에서 키 RSA를 생성하는 데 사용된 알고리즘을 볼 수 있습니다.
```bsh
keytool -genkeypair -alias ssia -keyalg RSA -keypass ssia123 -keystore ssia.jks -storepass ssia123
```

#### 공개키 생성

방금 생성한 개인키에 대한 공개키는 다음과 같이 만든다.

```bsh
keytool -list -rfc --keystore ssia.jks | openssl x509 -inform pem -pubkey
```

공개 키를 생성할 때 사용한 암호를 입력하라는 메시지가 표시됩니다. 제 경우에는 ssia123입니다. 그런 다음 출력에서 공개 키와 인증서를 찾아야 합니다. (이 예에서는 키 값만 필요합니다.) 이 키는 다음 코드과 유사해야 합니다.
```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAijLqDcBHwtnsBw+WFSzG
VkjtCbO6NwKlYjS2PxE114XWf9H2j0dWmBu7NK+lV/JqpiOi0GzaLYYf4XtCJxTQ
DD2CeDUKczcd+fpnppripN5jRzhASJpr+ndj8431iAG/rvXrmZt3jLD3v6nwLDxz
pJGmVWzcV/OBXQZkd1LHOK5LEG0YCQ0jAU3ON7OZAnFn/DMJyDCky994UtaAYyAJ
7mr7IO1uHQxsBg7SiQGpApgDEK3Ty8gaFuafnExsYD+aqua1Ese+pluYnQxuxkk2
Ycsp48qtUv1TWp+TH3kooTM6eKcnpSweaYDvHd/ucNg8UDNpIqynM1eS7KpffKQm
DwIDAQAB
-----END PUBLIC KEY-----
```

JWT에 서명하는 데 사용할 수 있는 개인 키와 서명을 확인하는 데 사용할 수 있는 공개 키가 있습니다. 이제 권한 부여 및 리소스 서버에서 구성하기만 하면 됩니다.

### 15.2.2 개인 키를 사용하는 인증 서버 구현

JWT 서명에 개인 키를 사용하도록 권한 부여 서버를 구성합니다. 15.2.1에서 개인 키와 공개 키를 생성하는 방법을 배웠습니다. 여기서는 ssia-ch15-ex2-as라는 별도의 프로젝트를 생성하지만 15.1에서 구현한 인증 서버와 동일한 종속성을 pom.xml 파일에서 사용합니다.

resource 폴더에 개인 키 파일인 ssia.jks를 복사합니다. 클래스 경로에서 직접 읽기 쉽기 때문에 resource 폴더에 키를 추가합니다. 그러나 클래스 경로에 반드시 있어야 하는 것은 아닙니다. application.properties 파일에는 파일 이름, 키 별칭, 암호를 생성할 때 개인 키를 보호하는 데 사용한 암호를 저장합니다. JwtTokenStore를 구성하려면 이러한 세부 정보가 필요합니다. 다음은 application.properties 파일의 내용을 보여줍니다.

```yaml
password=ssia123
privateKey=ssia.jks
alias=ssia
```
인증 서버가 대칭 키를 사용하기 위해 수행한 구성과 비교할 때 변경되는 유일한 것은 JwtAccessTokenConverter 객체의 정의입니다. 여전히 JwtTokenStore를 사용합니다. 15.1절에서 대칭 키를 구성하기 위해 JwtAccessTokenConverter를 사용했습니다. 동일한 JwtAccessTokenConverter 객체를 사용하여 개인 키를 설정합니다. 다음 목록은 권한 부여 서버의 구성 클래스를 보여줍니다.

목록 15.5 인증 서버 및 개인 키에 대한 구성 클래스

```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  @Value("${password}") ❶
  private String password; 
 
  @Value("${privateKey}")
  private String privateKey;
                                                   
  @Value("${alias}")
  private String alias;

  @Autowired
  private AuthenticationManager authenticationManager;

  // Omitted code

  @Bean
  public JwtAccessTokenConverter jwtAccessTokenConverter() {
    var converter = new JwtAccessTokenConverter();

    KeyStoreKeyFactory keyStoreKeyFactory = 
       new KeyStoreKeyFactory(
           new ClassPathResource(privateKey),
                     password.toCharArray()
       ); ❷ 

    converter.setKeyPair( ❸
       keyStoreKeyFactory.getKeyPair(alias));

    return converter;
  }
}
```
❶ application.properties 파일에서 private key 파일명, alias, 패스워드 삽입

❷ 클래스 경로에서 개인 키 파일을 읽을 KeyStoreKeyFactory 객체 생성

❸ KeyStoreKeyFactory 객체를 사용하여 키 쌍을 검색하고 키 쌍을 JwtAccessTokenConverter 객체로 설정합니다.

이제 권한 부여 서버를 시작하고 `/oauth/token` 엔드포인트를 호출하여 새 액세스 토큰을 생성할 수 있습니다. 물론 생성된 일반 JWT만 볼 수 있지만 서명을 확인하려면 쌍에서 공개 키를 사용해야 한다는 차이점이 있습니다. 토큰은 암호화되지 않고 서명만 된다는 것을 잊지 마십시오. 다음 코드는 `/oauth/token` 엔드포인트를 호출하는 방법을 보여줍니다.

```bsh
curl -v -XPOST -u client:secret "http://localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read"
```
The response body is

```json
{
  "access_token":"eyJhbGciOiJSUzI1NiIsInR5...",
  "token_type":"bearer",
  "refresh_token":"eyJhbGciOiJSUzI1NiIsInR...",
  "expires_in":43199,
  "scope":"read",
  "jti":"8e74dd92-07e3-438a-881a-da06d6cbbe06"
}
```

### 15.2.3 공개 키를 사용하는 리소스 서버 구현

리소스 서버는 공개 키로 토큰의 서명을 확인합니다. OAuth 2를 통한 인증을 구현하고 공개-개인 키 쌍을 사용하여 토큰을 보호하는 전체 시스템을 만듭니다. 인증 서버는 개인 키를 사용하여 토큰에 서명하고 리소스 서버는 공개 키를 사용하여 서명을 확인합니다. 토큰에 서명할 때만 키를 사용하고 암호화하지 않습니다. 이 리소스 서버를 구현하기 위해 작업하는 프로젝트의 이름을 ssia-ch15-ex2-rs로 지정했습니다. 이전 섹션에 있는 예제와 동일한 종속성을 pom.xml에서 사용합니다.

리소스 서버는 토큰 서명의 유효성을 검사하기 위해 쌍의 공개 키가 있어야 하므로 이 키를 application.properties 파일에 추가해 보겠습니다. 섹션 15.2.1에서 공개 키를 생성하는 방법을 배웠습니다. 다음 코드은 내 application.properites 파일의 내용을 보여줍니다.

```yaml
server.port=9090
publicKey=-----BEGIN PUBLIC KEY-----MIIBIjANBghk...-----END PUBLIC KEY-----
```
다음 목록은 리소스 서버의 구성 클래스에서 이 키를 구성하는 방법을 보여줍니다.

**목록 15.6** 리소스 서버 및 공개 키에 대한 구성 클래스
```java
@Configuration
@EnableResourceServer
public class ResourceServerConfig 
  extends ResourceServerConfigurerAdapter {

  @Value("${publicKey}") ❶
  private String publicKey;

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    resources.tokenStore(tokenStore());
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore( ❷
       jwtAccessTokenConverter()); ❷
  }

  @Bean
  public JwtAccessTokenConverter jwtAccessTokenConverter() {
    var converter = new JwtAccessTokenConverter();
    converter.setVerifierKey(publicKey); ❸
    return converter;
  }
}
```
❶ application.properties 파일에서 키 삽입

❷ Spring 컨텍스트에서 JwtTokenStore 생성 및 추가

❸ 토큰 저장소가 토큰의 유효성을 검사하는 데 사용하는 공개 키를 설정합니다.

물론 엔드포인트가 있으려면 컨트롤러도 추가해야 합니다. 다음 코드는 컨트롤러를 정의합니다.

```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
```
Let’s run and call the endpoint to test the resource server. Here’s the command:
```bsh
curl -H "Authorization:Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6I..." http://localhost:9090/hello
```
The response body is
```
Hello!
```
### Spring Security OAuth 프로젝트 없이 비대칭 키 사용

이 사이드바에서는 앱이 토큰 유효성 검사에 비대칭 키를 사용하는 경우 Spring Security OAuth 프로젝트를 사용하여 리소스 서버를 간단한 Spring Security 프로젝트로 마이그레이션하기 위해 수행해야 하는 변경 사항에 대해 설명합니다. 실제로 비대칭 키를 사용하는 것은 대칭 키가 있는 프로젝트를 사용하는 것과 크게 다르지 않습니다. 유일한 변경 사항은 사용해야 하는 JwtDecoder입니다. 이 경우 토큰 유효성 검사를 위해 대칭 키를 구성하는 대신 키 쌍의 공개 부분을 구성해야 합니다. 다음 코드는 이 작업을 수행하는 방법을 보여줍니다.

```java
public JwtDecoder jwtDecoder() {
  try {   KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    var key = Base64.getDecoder().decode(publicKey);

    var x509 = new X509EncodedKeySpec(key);
    var rsaKey = (RSAPublicKey) keyFactory.generatePublic(x509);
    return NimbusJwtDecoder.withPublicKey(rsaKey).build();
  } catch (Exception e) {
    throw new RuntimeException("Wrong public key");
  }
}
```
토큰의 유효성을 검사하기 위해 공개 키를 사용하는 JwtDecoder가 있으면 oauth2ResourceServer() 메서드를 사용하여 디코더를 설정해야 합니다. 대칭 키처럼 이 작업을 수행합니다. 다음 코드는 이 작업을 수행하는 방법을 보여줍니다. 이 예제는 ssia-ch15-ex2-rs-migration 프로젝트에서 구현되었습니다.

```java
@Configuration
public class ResourceServerConfig 
extends WebSecurityConfigurerAdapter {
  @Value("${publicKey}")
  private String publicKey;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.oauth2ResourceServer(
      c -> c.jwt(
          j -> j.decoder(jwtDecoder())
      )
    );

    http.authorizeRequests()
          .anyRequest().authenticated();
  }

  // Omitted code
}
```

### 15.2.4 엔드포인트를 사용하여 공개 키 노출

공개 키를 리소스 서버에 알리는 방법에 대해 설명합니다. 권한 부여 서버는 공개 키를 노출합니다. 15.2에서 구현한 시스템에서 개인-공개 키 쌍을 사용하여 토큰에 서명하고 유효성을 검사합니다. 리소스 서버 측에서 공개 키를 구성했습니다. 리소스 서버는 공개 키를 사용하여 JWT의 유효성을 검사합니다. 그러나 키 쌍을 변경하려는 경우 어떻게 됩니까? 동일한 키 쌍을 영원히 유지하지 않는 것이 좋은 방법이며 이것이 이 섹션에서 구현하는 방법입니다. 시간이 지남에 따라 키를 회전해야 합니다! 이렇게 하면 시스템이 키 도난에 덜 취약해집니다(그림 15.7).
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH15_F07_Spilca.png)

그림 15.7 키가 주기적으로 변경되면 시스템은 키 도난에 덜 취약합니다. 그러나 키가 두 애플리케이션 모두에 구성되어 있으면 키를 회전하기가 더 어렵습니다.

지금까지 인증 서버 측에 개인 키를 구성하고 리소스 서버 측에 공개 키를 구성했습니다(그림 15.7). 두 위치에 설정하면 키를 관리하기가 더 어려워집니다. 그러나 한 쪽에서만 구성하면 키를 더 쉽게 관리할 수 있습니다. 솔루션은 전체 키 쌍을 인증 서버 측으로 이동하고 인증 서버가 엔드포인트와 함께 공개 키를 노출하도록 허용합니다(그림 15.8).
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH15_F08_Spilca.png)

그림 15.8 두 키 모두 인증 서버에서 구성됩니다. 공개 키를 가져오기 위해 리소스 서버는 권한 부여 서버에서 끝점을 호출합니다. 이 접근 방식을 사용하면 키를 한 곳에서만 구성하면 되므로 키를 더 쉽게 회전할 수 있습니다.

프로젝트 ssia-ch15-ex3-as에서 이 예제에 대한 권한 부여 서버를, 프로젝트 ssia-ch15-ex3-rs에서 이 예제의 리소스 서버를 찾을 수 있습니다.

인증 서버의 경우 15.2.3에서 개발한 프로젝트와 동일한 설정을 유지합니다. 공개 키를 노출하는 엔드포인트에 액세스할 수 있도록 하기만 하면 됩니다. 예, Spring Boot는 이미 그러한 끝점을 구성하지만 단지 그 뿐입니다. 기본적으로 모든 요청이 거부됩니다. 끝점의 구성을 재정의하고 클라이언트 자격 증명을 가진 모든 사람이 액세스할 수 있도록 해야 합니다. 목록 15.7에서 인증 서버의 구성 클래스에 대해 변경해야 할 사항을 찾을 수 있습니다. 이러한 구성을 통해 유효한 클라이언트 자격 증명을 가진 사람은 누구나 엔드포인트를 호출하여 공개 키를 얻을 수 있습니다.

Listing 15.7 The configuration class for the authorization server exposing public keys
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
               .and() ❶
             .withClient("resourceserver") ❶
             .secret("resourceserversecret"); ❶
    }

    @Override
    public void configure(
      AuthorizationServerSecurityConfigurer security) {
        security.tokenKeyAccess("isAuthenticated()"); ❷
    }
}
```
❶ 리소스 서버가 엔드포인트를 호출하는 데 사용하는 클라이언트 자격 증명을 추가하여 공개 키를 노출합니다.

❷ 유효한 클라이언트 자격 증명으로 인증된 모든 요청에 대해 공개 키에 대한 끝점을 노출하도록 권한 부여 서버를 구성합니다.

권한 부여 서버를 시작하고 /oauth/token_key 엔드포인트를 호출하여 구성을 올바르게 구현했는지 확인할 수 있습니다.

```json
curl -u resourceserver:resourceserversecret http://localhost:8080/oauth/token_key
```
The response body is
```json
{
  "alg":"SHA256withRSA",
  "value":"-----BEGIN PUBLIC KEY----- nMIIBIjANBgkq... -----END PUBLIC KEY-----"
}
```
리소스 서버가 이 끝점을 사용하고 공개 키를 얻으려면 속성 파일에서 끝점과 자격 증명만 구성하면 됩니다. 다음 코드는 리소스 서버의 application.properties 파일을 정의합니다.

```yaml
server.port=9090
security.oauth2.resource.jwt.key-uri=http://localhost:8080/oauth/token_key

security.oauth2.client.client-id=resourceserver
security.oauth2.client.client-secret=resourceserversecret
```
리소스 서버는 이제 인증 서버의 /oauth/token_key 끝점에서 공개 키를 가져오기 때문에 리소스 서버 구성 클래스에서 구성할 필요가 없습니다. 다음 코드에서 볼 수 있듯이 리소스 서버의 구성 클래스는 비어 있을 수 있습니다.

```java
@Configuration
@EnableResourceServer
public class ResourceServerConfig 
  extends ResourceServerConfigurerAdapter {
}
```
이제 리소스 서버를 시작하고 노출되는 /hello 끝점을 호출하여 전체 설정이 예상대로 작동하는지 확인할 수 있습니다. 다음 코드 조각은 cURL을 사용하여 /hello 엔드포인트를 호출하는 방법을 보여줍니다. 여기에서 섹션에서 했던 것처럼 토큰을 얻습니다.

15.2.3을 사용하여 리소스 서버의 테스트 끝점을 호출합니다.

```sh
curl -H "Authorization:Bearer eyJhbGciOiJSUzI1NiIsInR5cCI..." http://localhost:9090/hello
```

응답은
```
Hello!
```

## 15.3 JWT에 사용자 지정 세부 정보 추가

JWT 토큰에 사용자 지정 세부 정보를 추가하는 방법에 대해 설명합니다. 대부분의 경우 Spring Security가 이미 토큰에 추가한 것 이상은 필요하지 않습니다. 그러나 실제 시나리오에서는 토큰에 사용자 지정 세부 정보를 추가해야 하는 요구 사항을 찾을 수 있습니다. 이 섹션에서는 JWT에 사용자 지정 세부 정보를 추가하도록 권한 부여 서버를 변경하는 방법과 이러한 세부 정보를 읽도록 리소스 서버를 변경하는 방법을 배우는 예제를 구현합니다. 이전 예제에서 생성한 토큰 중 하나를 가져와 디코딩하면 Spring Security가 토큰에 추가하는 기본값을 볼 수 있습니다. 다음 목록은 이러한 기본값을 나타냅니다.

**목록 15.8** 권한 부여 서버에서 발행한 JWT 본문의 기본 세부 정보
```json
{
  "exp": 1582581543, ❶
  "user_name": "john", ❷
  "authorities": [ ❸
    "read"
  ],
  "jti": "8e208653-79cf-45dd-a702-f6b694b417e7", ❹
  "client_id": "client", ❺
  "scope": [ ❻
    "read"
  ]
}
```
❶ 토큰이 만료되는 타임스탬프

❷ 클라이언트가 자신의 리소스에 액세스할 수 있도록 인증한 사용자

❸ 사용자에게 부여된 권한

❹ 토큰의 고유 식별자

❺ 토큰을 요청한 클라이언트

❻ 클라이언트에게 부여된 권한

목록 15.8에서 볼 수 있듯이 기본적으로 토큰은 일반적으로 기본 인증에 필요한 모든 세부 정보를 저장합니다. 그러나 실제 시나리오의 요구 사항이 더 많은 것을 요구한다면 어떻게 될까요? 몇 가지 예는 다음과 같습니다.

- 독자가 책을 검토하는 응용 프로그램에서 권한 부여 서버를 사용합니다. 일부 엔드포인트는 특정 수 이상의 리뷰를 제공한 사용자만 액세스할 수 있어야 합니다.

- 사용자가 특정 시간대에서 인증된 경우에만 통화를 허용해야 합니다.

- 인증 서버는 소셜 네트워크이며 일부 엔드포인트는 최소 연결 수를 가진 사용자만 액세스할 수 있어야 합니다.

첫 번째 예에서는 토큰에 리뷰 수를 추가해야 합니다. 두 번째로 클라이언트가 연결된 시간대를 추가합니다. 세 번째 예의 경우 사용자에 대한 연결 수를 추가해야 합니다. 어떤 경우이든 JWT를 사용자 정의하는 방법을 알아야 합니다.

### 15.3.1 토큰에 사용자 지정 세부 정보를 추가하도록 인증 서버 구성

토큰에 사용자 지정 세부 정보를 추가하기 위해 인증 서버에 적용해야 하는 변경 사항에 대해 설명합니다. 예제를 간단하게 하기 위해 인증 서버 자체의 시간대를 추가하는 것이 요구 사항이라고 가정합니다. 이 예제에서 내가 작업하는 프로젝트는 ssia-ch15-ex4-as입니다. 토큰에 세부 정보를 추가하려면 TokenEnhancer 유형의 개체를 생성해야 합니다. 다음 목록은 이 예제를 위해 만든 TokenEnhancer 개체를 정의합니다.

Listing 15.9 커스텀 토큰 인핸서
```java
public class CustomTokenEnhancer 
  implements TokenEnhancer { ❶

  @Override
  public OAuth2AccessToken enhance( ❷
    OAuth2AccessToken oAuth2AccessToken,
    OAuth2Authentication oAuth2Authentication) {

    var token = ❸
      new DefaultOAuth2AccessToken(oAuth2AccessToken);

      Map<String, Object> info = ❹
         Map.of("generatedInZone", 
                ZoneId.systemDefault().toString());

      token.setAdditionalInformation(info); ❺

      return token; ❻
  }
}
```
❶ TokenEnhancer 계약 이행

❷ 현재 토큰을 받고 향상된 토큰을 반환하는 향상() 메서드를 재정의합니다.

❸ 수신한 토큰을 기반으로 새 토큰 개체를 만듭니다.

❹ 토큰에 추가하려는 세부 정보를 Map으로 정의합니다.

❺ 토큰에 추가 세부 정보 추가

❻ 추가 세부 정보가 포함된 토큰을 반환합니다.

TokenEnhancer 객체의 enhanced() 메서드는 우리가 강화한 토큰을 매개변수로 수신하고 추가 세부 정보가 포함된 "향상된" 토큰을 반환합니다. 이 예에서는 섹션 15.2에서 개발한 것과 동일한 애플리케이션을 사용하고 토큰 인핸서를 적용하기 위해 configure() 메서드만 변경합니다. 다음 목록은 이러한 변경 사항을 나타냅니다.

목록 15.10 TokenEnhancer 객체 구성하기
```java
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

// Omitted code

  @Override
     public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
  
    TokenEnhancerChain tokenEnhancerChain 
      = new TokenEnhancerChain(); ❶

    var tokenEnhancers = ❷
      List.of(new CustomTokenEnhancer(),
              jwtAccessTokenConverter());

    tokenEnhancerChain ❸
      .setTokenEnhancers(tokenEnhancers);

    endpoints
      .authenticationManager(authenticationManager)
      .tokenStore(tokenStore())
      .tokenEnhancer(tokenEnhancerChain); ❹

   }
}
```
❶ TokenEnhancerChain 정의

❷ 두 개의 토큰 강화 개체를 목록에 추가합니다.

❸ 토큰 강화기의 목록을 체인에 추가합니다.

❹ 토큰 인핸서 개체를 구성합니다.

커스텀 토큰 인핸서를 구성하는 것은 조금 더 복잡합니다. 액세스 토큰 변환기 개체도 토큰 향상기이기 때문에 토큰 향상기 체인을 만들고 하나의 개체 대신 전체 체인을 설정해야 합니다. 사용자 지정 토큰 향상기만 구성하면 액세스 토큰 변환기의 동작을 재정의합니다. 대신 책임 체인에 두 가지를 모두 추가하고 두 개체를 모두 포함하는 체인을 구성합니다.

인증 서버를 시작하고 새 액세스 토큰을 생성하고 어떻게 보이는지 검사해 보겠습니다. 다음 코드 조각은 /oauth/token 엔드포인트를 호출하여 액세스 토큰을 얻는 방법을 보여줍니다.

```sh
curl -v -XPOST -u client:secret "http://localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read"
```

응답은

```json
{
  "access_token":"eyJhbGciOiJSUzI...",
  "token_type":"bearer",
  "refresh_token":"eyJhbGciOiJSUzI1...",
  "expires_in":43199,
  "scope":"read",
  "generatedInZone":"Europe/Bucharest",
  "jti":"0c39ace4-4991-40a2-80ad-e9fdeb14f9ec"
}
```
토큰을 디코딩하면 본문이 목록 15.11에 표시된 것과 같은 것을 볼 수 있습니다. 프레임워크가 기본적으로 응답에서도 사용자 지정 세부 정보를 추가하는 것을 추가로 관찰할 수 있습니다. 그러나 항상 토큰의 모든 정보를 참조하는 것이 좋습니다. 토큰에 서명함으로써 누군가가 토큰의 내용을 변경하더라도 서명의 유효성이 검사되지 않는다는 것을 기억하십시오. 이렇게 하면 서명이 정확하면 아무도 토큰의 내용을 변경하지 않았음을 알 수 있습니다. 응답 자체에 대해 동일한 보장이 없습니다.

Listing 15.11 The body of the enhanced JWT
```json
{
  "user_name": "john",
  "scope": [
    "read"
  ],
  "generatedInZone": "Europe/Bucharest",     ❶
  "exp": 1582591525,
  "authorities": [
    "read"
  ],
  "jti": "0c39ace4-4991-40a2-80ad-e9fdeb14f9ec",
  "client_id": "client"
}
```
❶ 추가한 사용자 정의 세부 정보는 토큰 본문에 나타납니다.

### 15.3.2 JWT의 사용자 지정 세부 정보를 읽도록 리소스 서버 구성

JWT에 추가한 추가 세부 정보를 읽기 위해 리소스 서버에 수행해야 하는 변경 사항에 대해 설명합니다. JWT에 사용자 지정 세부 정보를 추가하도록 권한 부여 서버를 변경하면 리소스 서버가 이러한 세부 정보를 읽을 수 있기를 원합니다. 사용자 지정 세부 정보에 액세스하기 위해 리소스 서버에서 수행해야 하는 변경 사항은 간단합니다. ssia-ch15-ex4-rs 프로젝트의 이 섹션에서 작업하는 예제를 찾을 수 있습니다.

15.1절에서 AccessTokenConverter가 토큰을 인증으로 변환하는 객체라는 것을 논의했습니다. 이것은 토큰의 사용자 지정 세부 정보도 고려하도록 변경해야 하는 개체입니다. 이전에는 다음 코드 스니펫에 표시된 것처럼 JwtAccessTokenConverter 유형의 빈을 생성했습니다. 

```java
@Bean
public JwtAccessTokenConverter jwtAccessTokenConverter() {
  var converter = new JwtAccessTokenConverter();
  converter.setSigningKey(jwtKey);   
  return converter;
}
```
이 토큰을 사용하여 토큰 유효성 검사를 위해 리소스 서버에서 사용하는 키를 설정했습니다. 토큰에 대한 새로운 세부 정보도 고려하는 JwtAccessTokenConverter의 사용자 지정 구현을 만듭니다. 가장 간단한 방법은 이 클래스를 확장하고 extractAuthentication() 메서드를 재정의하는 것입니다. 이 메서드는 인증 개체의 토큰을 변환합니다. 다음 목록은 사용자 지정 AcessTokenConverter를 구현하는 방법을 보여줍니다.

목록 15.12 사용자 정의 AccessTokenConverter 만들기
```java
public class AdditionalClaimsAccessTokenConverter
  extends JwtAccessTokenConverter {

  @Override
  public OAuth2Authentication 
         extractAuthentication(Map<String, ?> map) {

    var authentication = ❶
      super.extractAuthentication(map);

    authentication.setDetails(map); ❷

    return authentication; ❸

  }
}
```
❶ JwtAccessTokenConverter 클래스에서 구현한 로직을 적용하여 초기 인증 객체를 얻는다.

❷ 인증에 사용자 지정 세부 정보 추가

❸ 인증 대상 반환
리소스 서버의 구성 클래스에서 이제 사용자 지정 액세스 토큰 변환기를 사용할 수 있습니다. 다음 목록은 구성 클래스에서 AccessTokenConverter 빈을 정의합니다.

목록 15.13 새로운 AccessTokenConverter 빈 정의하기
```java
@Configuration
@EnableResourceServer
public class ResourceServerConfig 
  extends ResourceServerConfigurerAdapter {

  // Omitted code

  @Bean
  public JwtAccessTokenConverter jwtAccessTokenConverter() {
    var converter = 
      new AdditionalClaimsAccessTokenConverter();      ❶
    converter.setVerifierKey(publicKey);
    return converter;
  }
}
```
❶ 새로운 AccessTokenConverter 객체의 인스턴스 생성
변경 사항을 테스트하는 쉬운 방법은 변경 사항을 컨트롤러 클래스에 주입하고 HTTP 응답으로 반환하는 것입니다. 목록 15.14는 컨트롤러 클래스를 정의하는 방법을 보여줍니다.

**목록 15.1** 컨트롤러 클래스

```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello(OAuth2Authentication authentication) {
    OAuth2AuthenticationDetails details =                        ❶
      (OAuth2AuthenticationDetails) authentication.getDetails();

    return "Hello! " + details.getDecodedDetails();              ❷
  }
}
```
❶ 인증 개체에 추가된 추가 세부 정보를 가져옵니다.

❷ HTTP 응답의 세부 정보를 반환합니다.

이제 리소스 서버를 시작하고 사용자 지정 세부 정보가 포함된 JWT로 끝점을 테스트할 수 있습니다. 다음 코드 조각은 /hello 엔드포인트를 호출하는 방법과 호출 결과를 보여줍니다. getDecodedDetails() 메서드는 토큰의 세부 정보가 포함된 Map을 반환합니다. 이 예제에서는 간단하게 유지하기 위해 getDecodedDetails()에서 반환된 전체 값을 직접 인쇄했습니다. 특정 값만 사용해야 하는 경우 반환된 Map을 검사하고 해당 키를 사용하여 원하는 값을 얻을 수 있습니다.

```sh
curl -H "Authorization:Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6Ikp... " http://localhost:9090/hello
```
응답은
```
Hello! {user_name=john, scope=[read], generatedInZone=Europe/Bucharest, exp=1582595692, authorities=[read], jti=982b02be-d185-48de-a4d3-9b27337d1a46, client_id=client}
```

응답에서 `generatedInZone=Europe/Bucharest` 속성을 확인할 수 있습니다.

## 요약

- 오늘날 애플리케이션이 OAuth 2 인증 아키텍처에서 토큰을 검증하는 방식은 암호화 서명을 사용하는 경우가 많습니다.

- 암호화 서명과 함께 토큰 유효성 검사를 사용할 때 JWT(JSON Web Token)가 가장 널리 사용되는 토큰 구현입니다.

- 대칭 키를 사용하여 토큰에 서명하고 유효성을 검사할 수 있습니다. 대칭 키를 사용하는 것은 간단한 접근 방식이지만 권한 부여 서버가 리소스 서버를 신뢰하지 않는 경우 사용할 수 없습니다.

- 구현에서 대칭 키를 사용할 수 없는 경우 비대칭 키 쌍을 사용하여 토큰 서명 및 유효성 검사를 구현할 수 있습니다.

- 시스템이 키 도난에 덜 취약하도록 키를 정기적으로 변경하는 것이 좋습니다. 주기적으로 키를 변경하는 것을 키 순환이라고 합니다.

- 리소스 서버 측에서 직접 공개 키를 구성할 수 있습니다. 이 접근 방식은 간단하지만 키 회전을 더 어렵게 만듭니다.

- 키 순환을 단순화하기 위해 권한 부여 서버 측에서 키를 구성하고 리소스 서버가 특정 끝점에서 키를 읽도록 허용할 수 있습니다.

- 구현 요구 사항에 따라 본문에 세부 정보를 추가하여 JWT를 사용자 정의할 수 있습니다. 권한 부여 서버는 토큰 본문에 사용자 지정 세부 정보를 추가하고 리소스 서버는 이러한 세부 정보를 권한 부여에 사용합니다.
