
# 6장: 보안(권한 부여 및 인증)

이전 장에서 명령형 및 반응형 코딩 스타일을 사용하여 RESTful 웹 서비스를 개발했습니다. 이제 Spring Security를 ​​사용하여 이러한 REST 엔드포인트를 보호하는 방법을 배우게 됩니다. REST 끝점에 대한 토큰 기반 인증 및 권한 부여를 구현합니다. 

인증이 성공하면 액세스 토큰으로 웹 토큰(JWT)과 응답으로 리프레시 토큰이라는 두 가지 유형의 토큰을 제공합니다. 이 JWT 기반 액세스 토큰은 보안 URL에 액세스하는 데 사용됩니다. 

리프레시 토큰은 기존 JWT가 만료된 경우 새 JWT를 요청하는 데 사용되며 유효한 요청 토큰은 사용할 새 JWT를 제공합니다.

사용자를 관리자, 사용자 등과 같은 역할과 연결합니다. 이러한 역할은 사용자가 특정 역할을 보유하는 경우에만 REST 끝점에 액세스할 수 있도록 하는 권한으로 사용됩니다. 또한 CSRF(Cross-Site Request Forgery) 및 CORS(Cross-Origin Resource Sharing)에 대해서도 간략하게 설명합니다. 

이 장의 주제는 다음 섹션으로 나뉩니다.

- Spring Security와 JWT를 이용한 인증 구현
- JWT로 REST API(응용 프로그래밍 인터페이스) 보안
- CORS 및 CSRF 구성
- 권한 부여 이해
- 보안 테스트

By the end of the chapter, you will have learned how to implement authentication and authorization by using Spring Security.


## Technical requirements

You need the following to develop and execute the code featured in this chapter:

- IntelliJ IDEA, or Eclipse
- JDK 15
- Gradle
- Postman/cURL (for API testing)

Please visit the following link to download the code files:

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter06


## Implementing authentication using Spring Security and JWT

Spring Security는 상용구 코드 작성에 대해 걱정하지 않고 엔터프라이즈 애플리케이션 보안을 구현할 수 있도록 하는 라이브러리 모음으로 구성된 프레임워크입니다. 이 장에서는 Spring Security 프레임워크를 사용하여 토큰 기반(JWT) 인증 및 권한 부여를 구현합니다. 이 장의 과정을 통해 CORS 및 CSRF 구성에 대해서도 배웁니다.

Spring Security는 JWT와 유사한 불투명 토큰도 지원한다는 것을 아는 것이 유용합니다. 이들의 주요 차이점은 토큰에서 정보를 읽는 방법입니다. JWT에서 할 수 있는 방식으로 불투명 토큰에서 정보를 읽을 수 없습니다. 발급자만 이를 수행하는 방법을 알고 있습니다.

> NOTE

A token is a string of characters such as 
```
5rm1tc1obfshrm2354lu9dlt5reqm1ddjchqh81 7rbk37q95b768bib0jf44df6suk1638sf78cef7 hfolg4ap3bkighbnk7inr68ke780744fpej0gtd 9qflm999o8q.` 
```
다양한 권한 부여 흐름을 사용하여 상태 비저장 HTTP 끝점 또는 리소스를 호출할 수 있습니다.

"2장, Spring 개념과 REST API"에서 DispatcherServlet에 대해 배웠습니다. 이것은 클라이언트 요청과 REST 컨트롤러 간의 인터페이스입니다. 따라서 토큰 기반 인증 및 권한 부여에 대한 논리를 배치하려면 요청이 DispatcherServlet에 도달하기 전에 이를 수행해야 합니다. Spring Security 라이브러리는 요청이 DispatcherServlet에 도달하기 전에 처리되는 서블릿 pre 필터(필터 체인의 일부로)를 제공합니다. pre 필터는 실제 서블릿에 도달하기 전에 처리되는 서블릿 필터이며, Spring Security의 경우 DispatcherServlet입니다. 유사하게, post 필터는 요청이 서블릿/컨트롤러에 의해 처리된 후에 처리됩니다.

토큰 기반(JWT) 인증을 구현할 수 있는 두 가지 방법이 있습니다. 

- spring-boot-starter-security
-** spring-boot-starter-oauth2-resource-server**
 
우리는 후자를 사용할 것입니다.

전자는 다음 라이브러리가 들어 있습니다.

- spring-security-core
- spring-security-config
- spring-security-web

`spring-boot-starter-oauth2-resource-server`는 위의 jar에 다음 을 추기로 제공합니다:

- spring-security-oauth2-core
- spring-security-oauth2-jose
- spring-security-oauth2-resource-server

When you start this chapter's code, you can find the following log. You can see that, by default, DefaultSecurityFilterChain is auto-configured. The log statement lists down the configured filters in the DefaultSecurityFilterChain, as shown in the following code block:
```
INFO [Chapter06,,,]     [null] [null] [null]     [null] 24052 --- 
[main] o.s.s.web.DefaultSecurityFilterChain     : Will secure any request with [org.springframework.security.web.context.request.async.
WebAsyncManagerIntegrationFilter@413e8246, org.springframework.security.web.context.
SecurityContextPersistenceFilter@659565ed, org.springframework.security.web.header.
HeaderWriterFilter@770c3ca2, org.springframework.web.filter.
CorsFilter@4c7b4a31, org.springframework.security.web.csrf.
CsrfFilter@1de6f29d, org.springframework.security.web.authentication.logout.
LogoutFilter@5bb90b89, org.springframework.security.oauth2.server.resource.web.
BearerTokenAuthenticationFilter@732fa176, org.springframework.security.web.savedrequest.
RequestCacheAwareFilter@2ae0eb98, org.springframework.security.web.servletapi.
SecurityContextHolderAwareRequestFilter@3f473daf, org.springframework.security.web.authentication.
AnonymousAuthenticationFilter@2df7766b, org.springframework.security.web.session.
SessionManagementFilter@711261c7, org.springframework.security.web.access.
ExceptionTranslationFilter@1a7f2d34, org.springframework.security.web.access.intercept.
FilterSecurityInterceptor@3390621a]
```
따라서 클라이언트가 HTTP 요청을 실행하면 REST 컨트롤러에 도달하기 전에 다음 보안 필터를 모두 거칩니다(순서는 인증 결과에 따라 다를 수 있음).

- WebAsyncManagerIntegrationFilter
- SecurityContextPersistenceFilter
- HeaderWriterFilter
- CorsFilter
- CsrfFilter
- LogoutFilter
- BearerTokenAuthenticationFilter
- RequestCacheAwareFilter
- SecurityContextHolderAwareRequestFilter
- AnonymousAuthenticationFilter
- SessionManagementFilter
- ExceptionTranslationFilter
- FilterSecurityInterceptor
- 드디어 컨트롤러에 도착

이 필터 체인은 향후 릴리스에서 변경될 수 있습니다. 또한 spring-boot-starter-security를 사용했거나 구성을 변경한 경우 보안 필터 체인이 달라집니다. 

springSecurityFilterChain에서 사용 가능한 모든 필터는 https://docs.spring.io/spring-security/site/docs/current/reference/html5/#servlet-security-filters에서 찾을 수 있습니다. 

필터를 사용하여 인증을 수행하는 방법을 살펴보겠습니다.


## 필터를 사용하여 인증하는 방법

필터 기반 인증을 이미 알고 있는 경우 이 섹션을 건너뛰고 OAuth 2.0 리소스 서버를 사용한 인증 섹션으로 건너뛸 수 있습니다.

수동 필터 구성이 필요하지 않은 인증 구현을 위해 oauth2-resource-server 의존성을 사용할 것입니다. oauth2-resource-server는 인증을 위해 `BearerTokenAuthenticationFilter`를 사용합니다. 그러나 필터 기반 인증 구현 및 구성을 이해하면 Spring Security 개념을 단순화할 수 있습니다. 필터 기반 인증 및 권한 부여를 위해spring-security를 ​​추가하면 됩니다.

적절한 사전 필터에 인증 로직을 추가할 수 있습니다. 요청이 인증에 실패하면 액세스 거부 예외(AccessDeniedException)와 함께 응답이 클라이언트에 전송되고 그 결과 HTTP 401 Unauthorized 오류 상태 응답 코드가 표시됩니다.

### 필터를 사용한 사용자 username/password 인증 흐름

username/password를 사용한 인증은 다음 다이어그램과 같이 작동합니다. 사용자가 유효한 사용자 username/password 조합을 제출하면 호출이 성공하고 사용자는 200 OK 상태 코드(성공적인 응답)가 포함된 토큰을 받습니다. 잘못된 username/password 조합으로 인해 통화가 실패하면 401 Unauthorized 상태 코드가 포함된 응답을 받게 됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_6.1_B16561.jpg)

Figure 6.1 – Username/password 인증 흐름

Now, let's have a look at a token authorization flow using filters.

### 필터를 사용한 토큰 인증 플로우

토큰을 사용한 권한 부여는 다음 다이어그램과 같이 작동합니다. 사용자가 Authorization 헤더와 함께 유효한 bearer 토큰을 제출하면 호출이 성공하고 `FilterChain.doFilter(request,response)`를 호출합니다. 따라서 호출은 DispatcherServlet을 통해 컨트롤러로 라우팅됩니다. 마지막에 클라이언트는 적절한 상태 코드가 포함된 응답을 받습니다.

잘못된 토큰으로 인해 호출이 실패하면 AccessDeniedException이 발생하고 401 Unauthorized 상태 코드와 함께 `AuthenticationEntryPoint`에서 응답이 전송됩니다. `AuthenticationEntryPoint` 인터페이스를 구현하고 그 begin() 메서드를 재정의하여 이 동작을 재정의할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_6.2_B16561.jpg)

그림 6.2 – BasicAuthenticationFilter를 사용한 토큰 인증 흐름

먼저 Gradle 빌드 파일에 필요한 의존성을 추가해 보겠습니다.

### 필요한 Gradle 의존성 추가

다음과 같이 build.gradle 파일에 다음 의존성을 추가해 보겠습니다.
```
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
implementation 'com.auth0:java-jwt:3.12.0'
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/build.gradle

The Spring Boot Starter OAuth 2.0 resource server dependency would add the following JARs:

- spring-security-core
- spring-security-config
- spring-security-web
- spring-security-oauth2-core
- spring-security-oauth2-jose
- spring-security-oauth2-resource-server

For JWT implementation, we are going to use the java-jwt library from auth0.com.

You can now explore how to code these two filters—login- and token-based authentication.

### 로그인 기능을 위한 필터 코딩

클라이언트는 유효한 username/password 조합을 제공하여 성공적인 로그인을 수행한 후 JWT 토큰을 받습니다. Spring Security는 확장할 수 있는 `UsernamePasswordAuthenticationFilter`를 제공하고 attemptAuthentication() 및 successAuthentication() 메서드를 재정의할 수 있습니다. 먼저 다음과 같이 LoginFilter 클래스를 생성해 보겠습니다.

```java
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
  private final AuthenticationManager authenticationManager;
  private final JwtManager tokenManager;
  private final ObjectMapper mapper;

  public LoginFilter(AuthenticationManager authenticationManager, JwtManager tokenManager, ObjectMapper mapper) {
        this.authenticationManager = authenticationManager;
        this.tokenManager = tokenManager;
        this.mapper = mapper;
        super.setFilterProcessesUrl("/api/v1/auth/token");
  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/UNUSED/LoginFilter.java

여기서 JwtManager는 주어진 org.springframework.security.core.userdetails.User를 기반으로 JWT를 생성할 수 있는 클래스입니다. REST API에 JWT 추가 섹션에서 이에 대해 자세히 살펴보겠습니다. 이 생성자를 사용하면 setFilterProcessesUrl()을 사용하여 로그인 URL을 구성할 수도 있습니다. 이것이 표시되지 않으면 기본적으로 /login URL을 사용합니다.

Let's first override the attemptAuthentication() method, as follows:

```java
@Override
public Authentication attemptAuthentication(HttpServletRequest req,HttpServletResponse res)
    throws AuthenticationException {
  if (!req.getMethod().equals(HttpMethod.POST.name())) {
    throw new MethodNotAllowedException(req.getMethod(),
        List.of(HttpMethod.POST));
  }
  try (InputStream is = req.getInputStream()) {
    SignInReq user = new ObjectMapper().readValue(is, SignInReq.class);
    return authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            user.getUsername(),
            user.getPassword(),
            Collections.emptyList())
    );
  } catch (IOException e) {
    throw new RuntimeException(e);
  }
}
```
security.authentication.AuthenticationManager는 Spring Security의 구성요소이다. 우리는 단순히 username, password 및 권한을 전달하여 인증하는 데 사용합니다. 권한은 빈 목록으로 전달됩니다. 그러나 요청 페이로드에서 받거나 데이터베이스/메모리 저장소에서 가져오는 경우 권한을 전달할 수도 있습니다. SignInReq는 username, password를 포함하는 POJO입니다.

로그인이 성공하면 응답으로 JWT를 반환해야 합니다. 다음 코드처럼 successAuthentication() 메서드는 동일한 목적으로 재정의됩니다.

```java
@Override
protected void successfulAuthentication(
    HttpServletRequest req,
    HttpServletResponse res,
    FilterChain chain,
    Authentication auth) throws IOException {

  User principal = (User) auth.getPrincipal();
  String token = tokenManager.create(principal);
  SignedInUser user = new SignedInUser().username(principal.getUsername()).accessToken(token);

  res.setContentType(MediaType.APPLICATION_JSON_VALUE);
  res.setCharacterEncoding("UTF-8");
  res.getWriter().print(mapper.writeValueAsString(user));
  res.getWriter().flush();
}
```
여기에서 tokenManager.create() 메서드를 사용하여 토큰을 생성합니다. SignedInUser는 사용자 이름 및 토큰 필드를 포함하는 POJO입니다.

클라이언트는 성공적인 인증 후 응답으로 사용자 이름과 토큰을 수신하고 토큰 값에 "Bearer"를 접두사로 붙여 Authorization 헤더에서 이 토큰을 사용할 수 있습니다. Spring Security 필터 체인에서 이러한 새 필터를 구성하고 추가하는 방법을 살펴보겠습니다.

### 스프링 시큐리티 설정하기

AuthenticationManager.authenticate() 작동 방식에 대한 마지막 부분이 누락되었습니다. AuthenticationManager는 내부적으로 UserDetailsService 빈을 사용합니다. 다음과 같이 단일 메서드가 있습니다.
```java
UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
```
You just have to create an implementation of the UserDetailsService interface and expose it as a bean, as shown in the following code snippet:

```java
@Bean
@Override
protected UserDetailsService userDetailsService() {
  return userService;
}
```
This bean is exposed in the SecurityConfig class, which extends WebSecurityConfigurerAdapter, as shown in the following code snippet:
```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
```
여기서 @EnableWebSecurity 주석은 WebSecurityConfigurer 또는 WebSecurityConfigurerAdapter를 확장하는 클래스를 설정하고 WebSecurity 클래스를 커스터마이징하기 위해 적용된다. 이 클래스는 일부 재정의 메서드를 사용하여 보안을 위한 자동 구성을 수행합니다. configure() 메서드를 사용하면 여기에 표시된 메서드를 사용하여 HTTP 보안을 DSL(도메인별 언어)로 구성할 수 있습니다.

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
   http.authorizeRequests()
    .antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
    .anyRequest().authenticated()
    .and()
    .addFilter(new LoginFilter(super.authenticationManager(), mapper))
    .addFilter(new JwtAuthenticationFilter(super.authenticationManager()))
    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
}
```
configure() 메서드는 DSL을 사용하여 HTTP 보안을 구성하고 기본 구현을 재정의하는 방법입니다. 

다음과 같이 코드를 이해합시다.

- antMatchers(): 인증 없이 로그인 URL이 허용된 것을 확인할 수 있습니다. antMatchers()에 HTTP 메소드를 전달하지 않으면 모든 HTTP 메소드에 적용됩니다. permitAll()은 끝점 및 연결된 HTTP 메서드에 대한 제한을 제거합니다.

- addFilter(): 사용자 정의 필터를 추가할 수 있습니다. 다른 모든 URL에는 인증이 필요합니다.
- 로그인 작업 및 JWT 토큰 기반 인증을 위해 두 개의 추가 필터가 추가되었습니다.
- 세션 정책은 REST 끝점을 사용할 것이기 때문에 STATELESS로 설정됩니다.

이 섹션에서는 로그인 필터를 코딩했습니다. 이제 토큰 확인을 위한 JwtAuthenticationFilter 클래스를 추가해 보겠습니다.

### 토큰 확인을 위한 필터 코딩

필터를 사용하여 인증을 구현하는 방법을 살펴보겠습니다. spring-boot-starter-security 종속성을 사용하는 경우 다음 코드 블록에 표시된 대로 BasicAuthenticationFilter 클래스를 확장하고 토큰 확인을 위해 doFilterInternal 메서드를 재정의할 수 있습니다.

먼저 다음과 같이 BasicAuthenticationFilter 클래스를 확장하는 새 클래스를 만듭니다.

```java
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {
  public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
    super(authenticationManager);
  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/UNUSED/JwtAuthenticationFilter.java

이제 doFilterInternal 메소드를 재정의할 수 있습니다. 여기에서 요청에 bearer 토큰이 있는 Authorization 헤더가 포함되어 있는지 확인합니다. Authorization 헤더가 있으면 인증을 수행하고 보안 컨텍스트에 토큰을 추가한 다음 호출을 다음 보안 필터로 전달합니다.

```java
@Override
protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
  String header = req.getHeader("Authorization");
  if (Objects.isNull(header) || !header.startsWith("Bearer ")) {
    chain.doFilter(req, res);
    return;
  }
  Optional<UsernamePasswordAuthenticationToken> authentication = getAuthentication(req);
  authentication.ifPresentOrElse(e ->
      SecurityContextHolder.getContext().setAuthentication(e), SecurityContextHolder::clearContext);
  chain.doFilter(req, res);
}
```
The getAuthentication() method performs the token authentication logic, as shown in the following code snippet:

```java
private Optional<UsernamePasswordAuthenticationToken>
         getAuthentication(HttpServletRequest request) {
  String token = request.getHeader("Authorization");
  if (Objects.nonNull(token)) {
    DecodedJWT jwt = JWT.require(Algorithm.HMAC512(
         SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
        .build()
        .verify(token.replace(TOKEN_PREFIX, ""));
    String user = jwt.getSubject();
    @SuppressWarnings("unchecked")
    List<String> authorities = (List)
        jwt.getClaim("roles");
    if (Objects.nonNull(user)) {
      return Optional.of(new UsernamePasswordAuthenticationToken(
          user, null, Objects.nonNull(authorities) ?
          authorities.stream().map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList()) : Collections.emptyList()));
    }
  }
  return Optional.empty();
}
```
여기서 JWT 및 DecodedJWT는 com.auth0:java-jwt 라이브러리의 일부입니다. verify()를 호출하면 지정된 토큰의 확인이 수행되고 DecodedJWT 인스턴스가 반환됩니다. 확인에 실패하면 JWTVerificationException을 반환합니다. 확인이 완료되면 GrantedAuthority 개체의 주체, 자격 증명 및 컬렉션을 사용하는 UsernamePasswordAuthenticationToken 토큰을 만들고 반환하기만 하면 됩니다. GrantedAuthority는 인증 객체와 관련된 권한을 나타내는 인터페이스입니다. OAuth2 Resource Server를 사용하면 기본적으로 범위 권한을 추가할 수 있습니다. 그러나 역할과 같은 사용자 지정 권한을 추가할 수 있습니다.

지금까지 Spring 필터 체인을 사용하여 인증 및 토큰 권한 부여 흐름에 대해 배웠습니다. 다음으로 spring-boot-starter-oauth2-resource-server 종속성을 사용하여 인증을 구현합니다. 
다음 섹션에서는 OAuth 2.0 리소스 서버를 사용한 인증 및 권한 부여 흐름을 살펴보겠습니다.


## OAuth 2.0 리소스 서버를 이용한 인증

Spring Security OAuth 2.0 Resource Server를 사용하면 BearerTokenAuthenticationFilter를 사용하여 인증 및 권한 부여를 구현할 수 있습니다. 여기에는 무기명 토큰 인증 로직이 포함됩니다. 그러나 토큰 생성을 위한 REST 엔드포인트를 작성해야 합니다. 

OAuth2.0 리소스 서버에서 인증 흐름이 어떻게 작동하는지 살펴보겠습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_6.3_B16561.jpg)

그림 6.3 – OAuth 2.0 리소스 서버를 사용한 토큰 인증 흐름

다음과 같이 그림 6.3에 묘사된 흐름을 이해합시다.

1. 클라이언트는` /api/v1/addresses`에 GET HTTP 요청을 보냅니다.

2. BearerTokenAuthenciationFilter가 작동합니다. 요청에 Authorization 헤더가 포함되어 있지 않으면 BearerTokenAuthenticationFilter가 전달자 토큰을 찾지 못했기 때문에 요청을 인증하지 않습니다. 권한 부여를 수행하는 FilterSecurityInterceptor에 대한 호출을 전달합니다. AccessDeniedException 예외가 발생합니다(그림 6.3에서 2로 표시됨). ExceptionTranslationFilter가 작동합니다. 제어는 401 Unauthorized 상태와 Bearer 값이 있는 WWW-Authenticate 헤더로 응답하는 BearerTokenAuthenticationEntryPoint로 이동됩니다. 클라이언트가 응답으로 Bearer 값이 포함된 WWW-Authenticate 헤더를 수신하면 유효한 Bearer 토큰을 보유한 Authorization 헤더로 재시도해야 함을 의미합니다. 이 단계에서는 클라이언트가 요청을 재생할 수 있기 때문에 보안상의 이유로 요청 캐시가 NullRequestCache(즉, 비어 있음)입니다.

3. HTTP 요청에 Authorization 헤더가 포함되어 있다고 가정해 보겠습니다. HTTP 요청에서 Authorization 헤더를 추출하고 Authorization 헤더에서 토큰을 추출합니다. 토큰 값을 사용하여 BearerTokenAuthenticationToken의 인스턴스를 생성합니다. BearerTokenAuthenticationToken은 인증된 요청에 대한 토큰/principal을 나타내는 Authentication 인터페이스를 구현하는 AbstractAuthenticationToken 클래스 유형입니다.

4. HTTP 요청은 구성에 따라 AuthenticationManager를 제공하는 AuthenticationManagerResolver로 전달됩니다. AuthenticationManager는 BearerTokenAuthenticationToken 토큰을 확인합니다.

5. 인증에 성공하면 SecurityContext 인스턴스에 Authentication이 설정됩니다. 그런 다음 이 인스턴스는 SecurityContextHolder에 전달됩니다. 요청은 처리를 위해 나머지 필터로 전달된 다음 DispatcherServlet으로 라우팅되고 마지막으로 AddressController로 라우팅됩니다.

6. 인증에 실패하면 SecurityContextHolder.clearContext()가 호출되어 컨텍스트 값을 지웁니다. ExceptionTranslationFilter가 작동합니다. 제어가 BearerTokenAuthenticationEntryPoint로 이동되어 401 Unauthorized 상태로 응답하고 WWW-Authenticate header에는 다음과 같은 에러 메시지가 들어 있습니다.
   
```json
{
  ...
  Bearer error="invalid_token", 
  error_description="An error occurred while attempting to decode the Jwt: Jwt expired at 2020-12-14T17:23:30Z", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"
}
```


## JWT의 기초 살펴보기

액션을 수행하거나 정보에 액세스하려면 허가 또는 권리 형태의 권한이 필요합니다. 이 권한을 클레임이라고 합니다. 클레임은 키-값 쌍으로 표시됩니다. 키에는 클레임 이름이 포함되고 값에는 유효한 JSON 값이 될 수 있는 클레임이 포함됩니다. 클레임은 JWT에 대한 메타데이터일 수도 있습니다.

JWT는 어떻게 발음됩니까?

https://tools.ietf.org/html/rfc7519에 따르면 JWT의 제안 발음은 영어 단어 jot와 동일합니다.

JWT는 일련의 클레임이 포함된 인코딩된 문자열입니다. 이러한 클레임은 JWS(JSON 웹 서명)로 디지털 서명되거나 JWE(JSON 웹 암호화)로 암호화됩니다. JWT는 당사자 간에 안전하게 클레임을 전송하는 자체 포함된 방법입니다. 이러한 RFC(Request for Comments) 제안 표준에 대한 링크는 이 장의 추가 읽기 섹션에서 제공됩니다.

### JWT structure

A JWT is an encoded string such as `aaa.bbb.ccc`, consisting of the following three parts separated by dots (.):

- Header
- Payload
- Signature

A few websites such as https://jwt.io/ or https://www.jsonwebtoken.io/ allow you to view the content of a JWT and generate a JWT.

Let's have a look at the following sample JWT string. You can paste it into one of the previously mentioned websites to decode the content:
```
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzY290dCIsInJvbGVzIjpbIlVTRVIiXSwiaX NzIjoiTW9kZXJuIEFQSSBEZXZlbG9wbWVudCB3aXRoIFNw cmluZyBhbmQgU3ByaW5nIEJvb3QiLCJleHAiOjE2MTA1Mj A2MjksImlhdCI6MTYxMDE5ODIzNywianRpIjoiMjk3ZGY4 YTctNTE4Zi00ZWQ3LWJhNjYtOTJkYTQ5NGRkZDc2In0.MW-QOgAcNwLoEYINzqnDSm73-N86yf29-RUJsrApDyg
```
This sample token demonstrates how a JWT is formed and divided into three parts using dots.

#### 헤더

헤더는 일반적으로 두 개의 키-값 쌍을 포함하는 Base64URL 인코딩 JSON 문자열로 구성됩니다.
토큰 유형(일반 키 포함) 및 서명 알고리즘(alg 키 포함).

A sample JWT string contains the following header:
```json
{
  "typ": "JWT",
  "alg": "HS256"
}
```
The preceding header contains typ and alg fields, representing type and algorithm respectively.

#### Payload
페이로드는 JWT의 두 번째 부분이며 클레임을 포함합니다. 이것은 Base64URL로 인코딩된 JSON 문자열이기도 합니다. 클레임에는 등록, 공개 및 비공개의 세 가지 유형이 있습니다. 이를 요약하면 다음과 같습니다.

- 등록된 클레임: 일부 클레임은 IANA JSON 웹 토큰 클레임 레지스트리에 등록되어 있으므로 이러한 클레임을 등록된 클레임이라고 합니다. 필수 사항은 아니지만 권장됩니다. 일부 등록된 클레임은 다음과 같습니다.

  a. 발급자 클레임(iss 키): 이 클레임은 토큰을 발급한 주체를 식별합니다.

  b. 주제 클레임(하위 키): JWT의 주제를 나타내는 고유한 값이어야 합니다.
  
  c. 만료 시간 클레임(exp 키): JWT가 거부되어야 하는 만료 시간을 나타내는 숫자 값입니다.
  
  d. 클레임 발행(iat 키): 이 클레임은 JWT가 발행된 시간을 식별합니다.
  이자형. JWT ID 클레임(jti 키): 이 클레임은 JWT의 고유 식별자를 나타냅니다.
  
  e. 대상 클레임(aud 키): 이 클레임은 JWT가 의도한 수신자를 식별합니다.
  
  f. Not Before claim(nbf 키): JWT가 거부되어야 하는 이전 시간을 나타냅니다.

- 공개 클레임: JWT 발급자가 정의하며 등록된 클레임과 충돌하지 않아야 합니다. 따라서 IANA JWT 클레임 레지스트리에 등록하거나 충돌 방지 네임스페이스가 있는 URI로 정의해야 합니다.

- 비공개 클레임: 발급자와 청중이 정의하고 사용하는 맞춤 클레임입니다. 등록된 것도 아니고 공개된 것도 아닙니다.

Here is a sample JWT string containing the following payload:

```json
{
    "sub": "scott",
    "roles": [
        "USER"
    ],
    "iss": "Modern API Development with Spring and Spring Boot",
    "exp": 1610520629,
    "iat": 1610198237,
    "jti": "297df8a7-518f-4ed7-ba66-92da494ddd76"
}
```
앞의 페이로드에는 sub(제목), iss(발급자), 역할(사용자 지정 클레임 역할), exp(만료), iat(발급 시간) 및 jti(JWT ID) 필드가 포함됩니다.

#### 서명

서명은 세 번째 부분인 Base64로 인코딩된 문자열이기도 합니다. 서명은 JWT의 콘텐츠를 보호하기 위한 것입니다. 콘텐츠는 표시되지만 토큰에 서명한 경우 수정할 수 없습니다. Base64로 인코딩된 헤더 및 페이로드는 토큰을 서명된 토큰으로 만들기 위한 비밀 또는 공개 키와 함께 서명 알고리즘으로 전달됩니다. 페이로드에 민감한 정보나 비밀 정보를 포함하려면 페이로드에 할당하기 전에 암호화하는 것이 좋습니다.
서명은 콘텐츠가 다시 수신된 후 수정되지 않도록 합니다. 공개/개인 키를 사용하면 보낸 사람을 확인하여 보안 단계를 강화합니다.
JWT와 JWE의 조합을 사용할 수 있습니다. 그러나 권장되는 방법은 먼저 JWE를 사용하여 페이로드를 암호화한 다음 서명하는 것입니다.
이 장에서는 공개/개인 키를 사용하여 토큰에 서명합니다. 코드로 넘어가 봅시다.



## JWT로 REST API 보안

이 섹션에서는 "4장, API를 위한 비즈니스 로직 작성"에서 노출된 REST 엔드포인트를 보호합니다. 따라서 우리는 4장 코드를 사용하고 API를 보호하기 위해 이를 개선할 것입니다.

REST API는 다음 기능으로 보호해야 합니다.

- JWT 없이는 보안 API에 액세스할 수 없습니다.
- JWT는 로그인/가입 또는 리프레시 토큰을 사용하여 생성할 수 있습니다.
- JWT 및 리프레시 토큰은 유효한 사용자의 username/password 조합 또는 유효 사용자에 대해서만 제공되어야 합니다.
- 비밀번호는 강력한 해싱 기능 bcrypt을 사용하여 인코딩된 형식으로 저장해야 합니다.
- JWT는 강력한 알고리즘이 있는 RSA(Rivest, Shamir, Adleman용) 키로 서명해야 합니다.
- 페이로드의 클레임은 민감한 정보나 보안 정보를 저장해서는 안 됩니다. 그렇다면 암호화해야 합니다.
- 특정 역할에 대한 API 액세스 권한을 부여할 수 있어야 합니다.

권한 부여 흐름을 위한 새 API를 포함해야 합니다. 먼저 추가해 보겠습니다.

### 새로운 API 정의 배우기

등록, 로그인, 로그아웃 및 리프레시 토큰의 4가지 새로운 API를 추가하여 기존 API를 향상시킵니다. 
가입, 로그인 및 로그아웃 작업은 자명합니다.

리프레시 토큰은 기존 토큰이 만료되면 새 액세스 토큰(JWT)을 제공합니다. 이것이 가입/로그인 API가 응답의 일부로 액세스 토큰과 리프레시 토큰이라는 두 가지 유형의 토큰을 제공하는 이유입니다. 
JWT 액세스 토큰은 자체 만료되므로 로그아웃 작업은 리프레시 토큰만 제거합니다.

이러한 API를 openapi.yaml 문서에 추가해 보겠습니다.

### API 사양 수정

새 API를 추가하는 것 외에도 UserApi 인터페이스를 통해 이러한 모든 API를 노출하는 이러한 API에 대한 새 사용자 태그도 추가해야 합니다. 먼저 가입 끝점을 추가해 보겠습니다.

#### 가입 끝점

Add the following specification for the sign-up endpoint in openapi.yaml:

```yaml
/api/v1/users:
  post:
    tags:
      - user
    summary: Signup the a new customer (user)
    description: Creates a new customer (user), who can  login and do the shopping.
    operationId: signUp
    requestBody:
      content:
        application/xml:
          schema:
            $ref: '#/components/schemas/User'
        application/json:
          schema:
            $ref: '#/components/schemas/User'
    responses:
      201:
        description: For successful user creation.
        content:
          application/xml:
            schema:
              $ref: '#/components/schemas/SignedInUser'
          application/json:
            schema:
              $ref: '#/components/schemas/SignedInUser'
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/resources/api/openapi.yaml

Add the following new model, SignedInUser, to the schemas. This contains accessToken, refreshToken, username, and user ID fields. The code to add the model is shown in the following snippet:

```yaml
SignedInUser:
  description: Signed-in user information
  type: object
  properties:
    refreshToken:
      description: Refresh Token, a unique secure string
      type: string
    accessToken:
      description: JWT Token aka access token
      type: string
    username:
      description: User Name
      type: string
    userId:
      description: User Identifier
      type: string
```
Now, let's add the sign-in endpoint.
Sign-in endpoint definition
Add the following specification for the sign-in endpoint in openapi.yaml:

```yaml
/api/v1/auth/token:
  post:
    tags:
      - user
    summary: Signin the customer (user)
    description: Signin the customer (user) that generates
                 the JWT (access token) and refresh token,
                 which can be used for accessing APIs.
    operationId: signIn
    requestBody:
      content:
        application/xml:
          schema:
            $ref: '#/components/schemas/SignInReq'
        application/json:
          schema:
            $ref: '#/components/schemas/SignInReq'
    responses:
      200:
        description: For user sign-in. Once successful,
                     user
                     receives the access and refresh token.
        content:
          application/xml:
            schema:
              $ref: '#/components/schemas/SignedInUser'
          application/json:
            schema:
              $ref: '#/components/schemas/SignedInUser'
```
We need a new model, SignInReq, for the sign-in request payload. It just contains the username and password fields. Let's add it, as follows:

```yaml
SignInReq:
  description: Request body for Sign-in
  type: object
  properties:
    username:
      description: username of the User
      type: string
    password:
      description: password of the User
      type: string
```

#### Sign-out endpoint

Add the following specification for the sign-out endpoint in openapi.yaml:
```yaml
# Under the /api/v1/auth/token
delete:
  tags:
    - user
  summary: Signouts the customer (user)
  description: Signouts the customer (user). It removes the
               refresh
               token from DB. Last issued JWT should be
               removed from
               client end that if not removed last for
               given
               expiration time.
  operationId: signOut
  requestBody:
    content:
      application/xml:
        schema:
          $ref: '#/components/schemas/RefreshToken'
      application/json:
        schema:
          $ref: '#/components/schemas/RefreshToken'
  responses:
    202:
      description: Accepts the request for logout.
```
In an ideal scenario, you should remove the refresh token of a user received from the request. You can fetch the user ID from the token and then use that ID to remove it from the USER_TOKEN table. However, in that case, you should send a valid access token.

We have opted for an easy way to remove the token, which is for it to be sent by the user as a payload. Therefore, this endpoint needs the following new model, RefreshToken. Here is the code to add the model:

```yaml
RefreshToken:
  description: Contains the refresh token
  type: object
  properties:
    refreshToken:
      description: Refresh Token
      type: string
```
Finally, let's add an endpoint for refreshing the access token.

#### Refresh token endpoint

Add the following specification for the refresh token endpoint in openapi.yaml:

```yaml
/api/v1/auth/token/refresh:
  post:
    tags:
      - user
    summary: Provides new JWT based on valid refresh token.
    description: Provides new JWT based on valid refresh
                 token.
    operationId: getAccessToken
    requestBody:
      content:
        application/xml:
          schema:
            $ref: '#/components/schemas/RefreshToken'
        application/json:
          schema:
            $ref: '#/components/schemas/RefreshToken'
    responses:
      200:
        description: For successful operation.
        content:
          application/xml:
            schema:
              $ref: '#/components/schemas/SignedInUser'
          application/json:
            schema:
              $ref: '#/components/schemas/SignedInUser'
```
Here, we have raised an exception by defining the refresh endpoint in terms of forming a URI that represents the refresh token resources. Instead, it generates a new access token.

In the existing code, we don't have a table for storing the refresh token. Therefore, let's add one.

### Storing the refresh token using a database table

You can modify the Flyway database script to add a new table, as shown in the following code snippet:
```sql
create TABLE IF NOT EXISTS ecomm.user_token (
id uuid NOT NULL DEFAULT random_uuid(),
refresh_token varchar(128),
user_id uuid NOT NULL,
PRIMARY KEY(id),
FOREIGN KEY (user_id)
  REFERENCES ecomm.user(id)
);
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/resources/db/migration/V1.0.0__Init.sql

Now, you can start writing the implementation code for JWT.
Implementing the JWT manager

Let's add a constant class that contains all the constants related to the security functionality before we implement the JWT manager class, as shown in the following code snippet:

```java
public class Constants {
  public static final String ENCODER_ID = "bcrypt";
  public static final String API_URL_PREFIX = "/api/v1/**";
  public static final String H2_URL_PREFIX = "/h2-console/**";
  public static final String SIGNUP_URL = "/api/v1/users";
  public static final String TOKEN_URL = "/api/v1/auth/token";
  public static final String REFRESH_URL = "/api/v1/auth/token/refresh";
  public static final String AUTHORIZATION = "Authorization";
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String SECRET_KEY = "SECRET_KEY";
  public static final long EXPIRATION_TIME = 900_000;
  public static final String ROLE_CLAIM = "roles";
  public static final String AUTHORITY_PREFIX = "ROLE_";
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/Constants.java

이러한 상수는 시간 단위로 15분을 나타내는 EXPIRATION_TIME 긴 값을 제외하고는 자명합니다.

이제 JWT 관리자 클래스인 JwtManager를 정의할 수 있습니다. JwtManager는 새 JWT 생성을 담당하는 사용자 정의 클래스입니다. auth0.com의 java-jwt 라이브러리를 사용합니다. 토큰 서명을 위해 공개/개인 키를 사용할 것입니다. 이 클래스를 다음과 같이 정의합시다.

```java
@Component
public class JwtManager {
  private final RSAPrivateKey privateKey;
  private final RSAPublicKey pubKey;
  public JwtManager(RSAPrivateKey privateKey, RSAPublicKey         pubKey) {
    this.privateKey = privateKey;
    this.pubKey = pubKey;
  }
  public String create(UserDetails principal) {
    final long now = System.currentTimeMillis();
    return JWT.create()
        .withIssuer("Modern API Development with Spring…")
        .withSubject(principal.getUsername())
        .withClaim(ROLE_CLAIM, principal.getAuthorities().stream()
           .map(GrantedAuthority::getAuthority).collect(toList()))
        .withIssuedAt(new Date(now))
        .withExpiresAt(new Date(now + EXPIRATION_TIME))
        .sign(Algorithm.RSA256(pubKey, privateKey));
  }
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/JwtManager.java

여기서 JWT는 토큰 생성을 위한 유창한 API를 제공하는 java-jwt 라이브러리의 클래스입니다.
 발행자("iss"), 제목("sub"), 발행일("iat") 및 만료일("exp") 클레임을 추가합니다.

또한 UserDetails의 권한을 사용하여 채워지는 사용자 지정 클레임 ROLE_CLAIMS("역할")를 추가합니다. UserDetails는 Spring Security에서 제공하는 인터페이스입니다. org.springframework.security.core.userdetails.User.builder() 메소드를 사용하여 UserBuilder 클래스를 생성할 수 있습니다. UserBuilder는 UserDetails의 인스턴스를 빌드할 수 있는 최종 빌더 클래스입니다.

마지막에는 제공된 공개 및 개인 RSA 키를 사용하여 SHA256withRSA 알고리즘을 사용하여 JWT에 서명합니다. JWT 헤더는 알고리즘("alg") 클레임에 대한 HS256 값을 지정합니다.

*RSA*

RSA는 디지털 서명에 대해 FIPS(연방 정보 처리 표준)(FIPS 186)에서, 키 설정에 대해 SP(특별 간행물)(SP800-56B)에서 승인한 알고리즘입니다.

서명은 공개 및 개인 RSA 키를 사용하여 수행됩니다. 샘플 전자 상거래 애플리케이션에 RSA 키 관리를 위한 코드를 추가해 보겠습니다.

### 공개/개인 키 생성

다음 코드 조각과 같이 JDK의 keytool을 사용하여 키 저장소를 만들고 공개/개인 키를 생성할 수 있습니다. 

```sh
$ keytool -genkey -alias "jwt-sign-key" -keyalg RSA -keystore jwt-keystore.jks -keysize 4096
Enter keystore password:
Re-enter new password:
What is your first and last name?
  [Unknown]:  Modern API Development
What is the name of your organizational unit?
  [Unknown]:  Org Unit
What is the name of your organization?
  [Unknown]:  Packt
What is the name of your City or Locality?
  [Unknown]:  City
What is the name of your State or Province?
  [Unknown]:  State
What is the two-letter country code for this unit?
  [Unknown]:  IN
Is CN=Modern API Development, OU=Org Unit, O=Packt, L=City, ST=State, C=IN correct?
  [no]:  yes
Generating 4,096 bit RSA key pair and self-signed certificate (SHA384withRSA) with a validity of 90 days for: CN=Modern API Development, OU=Org Unit, O=Packt, L=City, ST=State, C=IN
```
생성된 키 저장소는 src/main/resources 디렉토리 아래에 있어야 합니다. 이 키는 생성된 후 90일 동안만 유효합니다. 따라서 실행하기 전에 이 장의 코드를 사용할 때 새 공개/개인 키 세트를 생성했는지 확인하십시오.

keytool 명령에 사용되는 필수 값은 다음과 같이 application.properties 파일에서도 구성해야 합니다.

```yaml
app.security.jwt.keystore-location=jwt-keystore.jks
app.security.jwt.keystore-password=password
app.security.jwt.key-alias=jwt-sign-key
app.security.jwt.private-key-passphrase=password
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/resources/application.properties
Now, we can configure the key store and public/private keys in the security configuration class.

### Configuring the key store and keys

Let's add a SecurityConfig configuration class to configure the security relation configurations. This class extends the WebSecurityConfigurerAdaptor class. Here's the code to do this:

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${app.security.jwt.keystore-location}")
    private String keyStorePath;
    
    @Value("${app.security.jwt.keystore-password}")
    private String keyStorePassword;

    @Value("${app.security.jwt.key-alias}")
    private String keyAlias;

    @Value("${app.security.jwt.private-key-passphrase}")
    private String privateKeyPassphrase;
  …
  …
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/SecurityConfig.java

We have added all the properties defined in application.properties here.

Now, we can make use of the properties defined in application.properties for configuring the KeyStore, RSAPrivateKey, and RSAPublicKey beans in the security configuration class, as shown in the next few sections.

#### KeyStore bean

You can create a new bean for KeyStore by adding the following method and annotating it with @Bean:

```java
@Bean
public KeyStore keyStore() {
  try {
    KeyStore keyStore =              KeyStore.getInstance(KeyStore.getDefaultType());
    InputStream resourceAsStream =              Thread.currentThread().getContextClassLoader()                 .getResourceAsStream(keyStorePath);
    keyStore.load(resourceAsStream,         keyStorePassword.toCharArray());
    return keyStore;
  } catch (IOException | CertificateException |
                NoSuchAlgorithmException |
                KeyStoreException e) {
    LOG.error("Unable to load keystore: {}", keyStorePath, e);
  }
  throw new IllegalArgumentException("Unable to load
    keystore");
}
```
This creates a KeyStore instance using the KeyStore class from the java.security package. It loads the key store from the src/main/resources package and uses the password configuration in the application.properties file.

#### RSAPrivateKey bean

You can create a new bean for RSAPrivateKey by adding the following method and annotating it with @Bean:

```java
@Bean
public RSAPrivateKey jwtSigningKey(KeyStore keyStore) {
  try {
    Key key = keyStore.getKey(keyAlias, privateKeyPassphrase.toCharArray());
    if (key instanceof RSAPrivateKey) {
      return (RSAPrivateKey) key;
    }
  } 
  catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
    LOG.error("Unable to load private key from keystore: {}", keyStorePath, e);
  }
  throw new IllegalArgumentException("Unable to load private key");
}
```
This method uses a key alias and a private key password to retrieve the private key, which is being used to return the RSAPrivateKey bean.

#### RSAPublicKey bean

You can create a new bean for RSAPublicKey by adding the following method and annotating it with @Bean:

```java
@Bean
public RSAPublicKey jwtValidationKey(KeyStore keyStore) {
  try {
    Certificate certificate = keyStore.getCertificate(keyAlias);
    PublicKey publicKey = certificate.getPublicKey();
    if (publicKey instanceof RSAPublicKey) {
      return (RSAPublicKey) publicKey;
    }
  } catch (KeyStoreException e) {
    LOG.error("Unable to load private key from keystore: {}", keyStorePath, e);
  }
  throw new IllegalArgumentException("Unable to load public key");
}
```
Again, a key alias is used to retrieve the certificate from the key store. Then, the public key is retrieved from the certificate and returned.

As you know, JwtManager uses these public and private RSA keys to sign the JWT; therefore, a JWT decoder should use the same public key to decode the token. OAuth 2.0 Resource Server uses the org.springframework.security.oauth2.jwt.JwtDecoder interface to decode the token. Therefore, we need to create an instance of the JwtDecoder implementation and set the same public key in it to decode the token.
Spring OAuth 2.0 Resource Server provides a NimbusJwtDecoder implementation class of JwtDecoder. Let's now create a bean of it with the public key.

#### JwtDecoder Bean

You can create a new bean for JwtDecoder by adding the following method and annotating it with @Bean:
```java
@Bean
public JwtDecoder jwtDecoder(RSAPublicKey rsaPublicKey) {
  return NimbusJwtDecoder.withPublicKey(
    rsaPublicKey).build();
}
```
Now, we can implement the newly added REST APIs.


## Implementing new APIs

Let's implement the APIs exposed using UserApi. UserApi is part of code that has been autogenerated using OpenAPI Codegen. First, we need to add a new entity to the user_token table.

### Coding user token functionality

You can create a UserTokenEntity based on the user_token table, as shown in the following code snippet:

```java
@Entity
@Table(name = "user_token")
public class UserTokenEntity {
  @Id
  @GeneratedValue
  @Column(name = "ID", updatable = false, nullable = false)
  private UUID id;
  @NotNull(message = "Refresh token is required.")
  @Basic(optional = false)
  @Column(name = "refresh_token")
  private String refreshToken;
  @ManyToOne(fetch = FetchType.LAZY)
  private UserEntity user;
  …
  …
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/entity/UserTokenEntity.java

Similarly, we can expose the following create, read, update, and delete (CRUD) repository for UserTokenEntity with the following two methods: deleteByUserId(), which will remove the UserToken table record based on a given user ID, and findByRefreshToken(), which will find the UserToken table record based on a given refresh token. The code is illustrated in the following code snippet:

```java
public interface UserTokenRepository extends                    CrudRepository<UserTokenEntity, UUID> {
  Optional<UserTokenEntity> findByRefreshToken(    String refreshToken);
  Optional<UserTokenEntity> deleteByUserId(UUID userId);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/repository/UserTokenRepository.java

Now, let add the new operations into UserService.

### Enhancing the UserService class

We also need to add new methods into UserService for the UserApi interface. Let's add new methods into the service, as follows:

```java
UserEntity findUserByUsername(String username);
Optional<SignedInUser> createUser(User user);
SignedInUser getSignedInUser(UserEntity userEntity);
Optional<SignedInUser> getAccessToken(RefreshToken refreshToken);
void removeRefreshToken(RefreshToken refreshToken);
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/service/UserService.java

Here, each method performs a specific operation, as outlined here:

- findUserByUsername(): Finds and returns a user based on a given username.
- createUser(): Adds a new signed-up user to the database.
- getSignedInUser(): Creates a new model instance of SignedInUser that holds the refresh token, access token (JWT), user ID, and username.
- getAcceessToken(): Generates and returns a new access token (JWT) for a given valid refresh token.
- removeRefreshToken(): Removes the refresh token from the database. It is called when the user wants to sign out.

Let's implement each of these methods in the UserServiceImpl class.

### findUserByUsername() implementation

First, you can add the implementation for findUserByUsername(), as follows:
```java
@Override
public UserEntity findUserByUsername(String username) {
  if (Strings.isBlank(username)) {
    throw new UsernameNotFoundException("Invalid user.");
  }
  final String uname = username.trim();
  Optional<UserEntity> oUserEntity = repository.findByUsername(uname);
  UserEntity userEntity = oUserEntity.orElseThrow(() ->
              new UsernameNotFoundException(
                String.format("Given user(%s) not found.", uname)));
  return userEntity;
}
```
This is a straightforward operation. You query the database based on a given username. If found, then it returns the user, else it throws a UsernameNotFoundException exception.

### createUser() implementation

Next, you can add the implementation for the createUser() method, as shown in the following code snippet:

```java
@Override
@Transactional
public Optional<SignedInUser> createUser(User user) {
  Integer count = repository.findByUsernameOrEmail(
                         user.getUsername(), user.getEmail());
  if (count > 0) {
    throw new GenericAlreadyExistsException("
        Use different username and email.");
  }
  UserEntity userEntity = repository.save(toEntity(user));
  return Optional.of(createSignedUserWithRefreshToken(
    userEntity));
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/service/UserServiceImpl.java

Here, we first check whether there is any existing user that was assigned the same username or email in the sign-up request. If there is, then it simply raises an exception, else it creates a new user in the database and returns a SignedInUser instance with refresh and access tokens using the createSignedUserWithRefreshToken() method.

First, we can add a private createSignedUserWithRefreshToken() method in UserServiceImpl, as follows:

```java
private SignedInUser createSignedUserWithRefreshToken(
                                  UserEntity userEntity) {
  return createSignedInUser(userEntity)
         .refreshToken(createRefreshToken(userEntity));
}
```
This also uses another private method, createSignedInUser(), which returns SignedInUser; then, it adds the refresh token by calling the createRefreshToken() method.

Let's define these two createSignedIn() and createSignedInUser() private methods, as follows:
```java
private SignedInUser createSignedInUser(UserEntity userEntity) {
  String token = tokenManager.create(
       org.springframework.security.core.userdetails.User.builder()
      .username(userEntity.getUsername())
      .password(userEntity.getPassword())
      .authorities(Objects.nonNull(userEntity.getRole()) ?
          userEntity.getRole().name() : "")
      .build());
  return new SignedInUser().username(
          userEntity.getUsername())
      .accessToken(token)
      .userId(userEntity.getId().toString());
}
private String createRefreshToken(UserEntity user) {
  String token = RandomHolder.randomKey(128);
  userTokenRepository.save(new UserTokenEntity()
                 .setRefreshToken(token).setUser(user));
  return token;
}
```
Here, tokenManager is used in the createSignedIn() method for creating the JWT. tokenManager is an instance of JwtManager. The User.builder() method is used to create a UserBuilder class. UserBuilder, which is a final builder class, is used to create an instance of UserDetails. The JwtManager.create() method uses this UserDetails instance to create a token.

The createRefreshToken() method uses RandomHolder private static class to generate a refresh token. This token is not a JWT; however, we can use a longer duration valid token, such as one valid for a day for a refresh token. Saving a JWT as a refresh token in the database removes the sole purpose of using the JWT. Therefore, we should think carefully about using the JWT as a refresh token and then saving it in the database.

Let's add the RandomHolder private static class, as follows:

```java
// https://stackoverflow.com/a/31214709/109354
private static class RandomHolder {
  static final Random random = new SecureRandom();
  public static String randomKey(int length) {
    return String.format("%"+length+"s",
                  new BigInteger(
                      length*5/*base 32,2^5*/, random)
        .toString(32)).replace('\u0020', '0');
  }
}
```
This class uses a SecureRandom instance to generate a random BigInteger instance. Then, this random BigInteger value is converted to a string with radix size 32. At the end, the space is replaced with 0 if found in a converted string.
You can also use the org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric() method to generate a refresh token, or use any other secured random key generator.
We also need to modify the UserRepository class to add a new method that returns the count of users having a given username or email.

#### getSignedInUser() implementation

Implementation of the getSignedInUser() method is straightforward, as shown in following code snippet:
```java
@Override
@Transactional
public SignedInUser getSignedInUser(UserEntity userEntity) {
  userTokenRepository.deleteByUserId(userEntity.getId());
  return createSignedUserWithRefreshToken(userEntity);
}
```
It first removes the existing token from the database associated with the given user, and then returns the new instance of SignedInUser created using createSignedUserWithRefreshToken(), defined previously in the createUser() implementation subsection.

#### getAccessToken() implementation

Implementation of the getAccessToken() method is again straightforward, as shown in the following code snippet:
```java
@Override
public Optional<SignedInUser> getAccessToken(                               RefreshToken refreshToken) {
  return userTokenRepository
      .findByRefreshToken(refreshToken.getRefreshToken())
      .map(ut -> Optional.of(createSignedInUser(             ut.getUser())
          .refreshToken(refreshToken.getRefreshToken())))
      .orElseThrow(() -> new InvalidRefreshTokenException(                                          "Invalid token."));
}
```
First, it finds the user's token entity using the UserTokenRepository instance. Then, it populate the SignedInUser POJO using the retrieved UserToken entity. The createSignedInUser() method does not populate the refresh token, therefore we assign the same refresh token back. If it does find the user token entry in the database based on the refresh token, it throws an exception.

You can also add a time validation logic for the refresh token—for example, store the refresh token creation time in the database and use the configured valid time for refresh token validation: a kind of expiration logic of JWT.

#### removeRefreshToken() implementation

Implementation of the removeRefreshToken() method is shown in the following code snippet:
```java
@Override
public void removeRefreshToken(RefreshToken refreshToken) {
  userTokenRepository
    .findByRefreshToken(refreshToken.getRefreshToken())
      .ifPresentOrElse(userTokenRepository::delete, () -> {
        throw new InvalidRefreshTokenException(
                                         "Invalid token.");
      });
}
```
First, it finds the given refresh token in the database. If this is not found, then it throws an exception. If the given refresh token is found in the database, then it deletes it.

### Enhancing the UserRepository class

Let's add findByUsername() and findByUsernameOrEmail() methods to UserRepository, as follows:
```java
public interface UserRepository
                 extends CrudRepository<UserEntity, UUID> {
  Optional<UserEntity> findByUsername(String username);
  @Query(value = "select count(u.*) from ecomm.user u
                 where u.username = :username or u.email =
                 :email",
                 nativeQuery = true
  Integer findByUsernameOrEmail(String username, String
                                email);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/repository/UserRepository.java

This returns a count of records matching the given username or email.
Now, we can implement the UserApi interface to write REST controllers.


### Implementing the REST controllers

You have already developed and enhanced services and repositories required to implement the APIs defined in the UserApi interface generated by OpenAPI Codegen in the previous section. The only pending dependency is PasswordEncoder. PasswordEncoder is required for encoding the password before storing and matching the password given as part of the sign-in request.

#### Adding a bean for PasswordEncoder
You should expose the PasswordEncoder bean because Spring Security needs to know which encoding you want to use for password encoding, as well as for decoding the passwords. Let's add a PasswordEncoder bean in AppConfig, as follows:
```java
@Bean
public PasswordEncoder passwordEncoder() {
  Map<String, PasswordEncoder> encoders = Map.of(
      "bcrypt", new BCryptPasswordEncoder(),
      "pbkdf2", new Pbkdf2PasswordEncoder(),
      "scrypt", new SCryptPasswordEncoder());
  return new DelegatingPasswordEncoder("bcrypt", encoders);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/AppConfig.java

You can directly create a new instance of BCryptPasswordEncoder and return it for bcrypt encoding. However, use of DelegatingPasswordEncoder not only allows you to support existing passwords but also facilitates migration to a new, better encoder if one is available in the future. This code uses Bcrypt as a default password encoder, which is the best among the current available encoders.

For DelegatingPasswordEncoder to work, you need to add a hashing algorithm prefix such as {bcrypt} to encoded passwords—for example, add {bcrypt}$2a$10$neR0EcYY5./tLVp4litNyuBy/kfrTsqEv8hiyqEKX0TXIQQwC/5Rm in the persistent store if you already have a hashed password in the database or if you're adding any seed/test users in the database script. The new password would store the password with a prefix anyway, as configured in the DelegatingPasswordEncoder constructor. You have passed bcrypt into the constructor, therefore all new passwords will be stored with a {bcrypt} prefix.

PasswordEncoder reads the password from the persistence store and removes the prefix before matching. It uses the same prefix to find out which encoder it needs to use for matching. Now, you can start implementing the new APIs based on UserApi.
Implementing the Controller class

First, create a new AuthController class, as shown in the following code snippet:
```java
@RestController
public class AuthController implements UserApi {
  private final UserService service;
  private final PasswordEncoder passwordEncoder;
  public AuthController(UserService service,
                        PasswordEncoder passwordEncoder, ) {
    this.service = service;
    this.passwordEncoder = passwordEncoder;
  }
  …
  …
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/controller/AuthController.java

The AuthController class is annotated with @RestController to mark it as a REST controller. Then, it uses two beans, UserService and PasswordEncoder, which will be injected at the time of the AuthController construction.

Let's first add the sign-in operation, as follows:

```java
@Override
public ResponseEntity<SignedInUser> signIn(@Valid SignInReq signInReq) {
  UserEntity userEntity = service.findUserByUsername(signInReq.getUsername());
  if (passwordEncoder.matches(
          signInReq.getPassword(),
              userEntity.getPassword())) {
    return ok(service.getSignedInUser(userEntity));
  }
  throw new InsufficientAuthenticationException("Unauthorized.");
}
```

It first finds the user and matches the password using the PasswordEncoder instance. If everything goes through successfully, it returns the SignedInUser instance with refresh and access tokens; else, it throws an exception.
Let's add other operations to AuthController, as follows:
```java
@Override
public ResponseEntity<Void> signOut(@Valid RefreshToken refreshToken) {
  service.removeRefreshToken(refreshToken);
  return accepted().build();
}
@Override
public ResponseEntity<SignedInUser> signUp(@Valid User user) {
  return status(HttpStatus.CREATED) .body(service.createUser(user).get());
}
@Override
public ResponseEntity<SignedInUser> getAccessToken(@Valid RefreshToken refreshToken) {
  return ok(service.getAccessToken(refreshToken)
        .orElseThrow(InvalidRefreshTokenException::new));
}
```
All operations such as signOut(), signUp(), and getAccessToken() are straightforward, as outlined here:

- signOut() uses the user service to remove the given refresh token.
- signUp() creates a valid new user and returns the SignedInUser instance as a response.
- getAccessToken() returns the SignedInUser with a new access token if the given refresh token is valid.

We are done with coding the controllers. Let's configure security in the next subsection.


## Configuring web-based security

이전 하위 섹션 중 하나인 Spring Security 구성에서 Spring Security를 ​​구성하는 방법에 대해 이미 배웠습니다. 해당 하위 섹션에서 WebSecurityConfigurerAdaptor를 확장하고 @EnableWebSecurity로 주석이 추가된 SecurityConfig라는 새 클래스를 정의했습니다.

재정의된 configure() 메서드를 수정하여 DSL(유창한 메서드)을 사용하여 HttpSecurity를 ​​구성할 수 있습니다. 다음 구성을 만들어 보겠습니다.

- httpBasic().disable()을 사용하여 HTTP 기본 인증(BA)을 비활성화합니다.

- formLogin().disable()을 사용하여 양식 로그인을 비활성화합니다.

-authorizeRequests()를 사용하여 URL 패턴에 따라 액세스를 제한합니다.

- ant(빌드 도구) 패턴 일치 스타일을 사용할 수 있는 antMatchers()를 사용하여 URL 패턴 및 해당 HTTP 메서드를 구성합니다. Spring MVC와 동일한 패턴 매칭 스타일을 사용하는 mvcMatchers()를 사용할 수도 있다.

- authorizeRequests()에 의해 명시적으로 구성된 URL을 제외한 모든 URL은 인증된 모든 사용자가 허용해야 합니다(anyRequest().authenticated()를 사용하여).

- OAuth 2.0 리소스 서버(oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)에 대한 JWT 전달자 토큰 지원을 활성화합니다.

- STATELESS 세션 생성 정책을 활성화합니다(즉, HTTPSession을 생성하지 않음).

다음과 같이 HttpSecurity에 추가해 보겠습니다.

```java
@Override
protected void configure(HttpSecurity http) throws  Exception {
  http.httpBasic().disable().formLogin().disable()
      .and()
      .headers().frameOptions().sameOrigin() // for H2 Console
      .and()
      .authorizeRequests()
      .antMatchers(HttpMethod.POST, TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.DELETE, TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.POST, SIGNUP_URL).permitAll()
      .antMatchers(HttpMethod.POST, REFRESH_URL).permitAll()
      .antMatchers(H2_URL_PREFIX).permitAll()      
      .anyRequest().authenticated()
      .and()
      .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
      .sessionManagement().sessionCreationPolicy(
             SessionCreationPolicy.STATELESS);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/SecurityConfig.java

여기에 H2 콘솔 앱에 대한 중요한 구성이 하나 더 추가되었습니다. H2 콘솔 사용자 인터페이스(UI)는 HTML 프레임을 기반으로 합니다. 기본적으로 보안 헤더(X-Frame-Options)는 동일한 출처의 프레임을 허용하는 권한과 함께 전송되지 않기 때문에 H2 콘솔 UI는 브라우저에 표시되지 않습니다. 따라서 headers().frameOptions().sameOrigin()을 설정해야 합니다.

이제 CORS 및 CSRF를 구성할 수 있습니다.


## Configuring CORS and CSRF

브라우저는 보안상의 이유로 스크립트의 크로스 오리진 요청을 제한합니다. 예를 들어 http://mydomain.com에서 http://mydomain-2.com으로의 호출은 스크립트를 사용하여 만들 수 없습니다. 또한 오리진은 도메인을 나타낼 뿐만 아니라 실제로 scheme와 port도 포함합니다.

끝점에 도달하기 전에 브라우저는 HTTP 메서드 옵션을 사용하여 실행 전 요청을 보내 서버가 실제 요청을 허용할지 여부를 확인합니다. 이 요청에는 다음 헤더가 포함되어 있습니다.

- 실제 요청의 헤더(Access-Control-Request-Headers)

- 실제 요청의 HTTP 메소드를 포함하는 헤더(Access-Control-Request-Method)

- 요청한 출처(scheme, domain, port)를 포함하는 Origin 헤더

- 서버의 응답이 성공하면 브라우저만 실제 요청을 실행하도록 허용합니다. 서버는 다음과 같은 다른 헤더로 응답합니다.

- 허용된 출처를 포함하는 Access-Control-Allow-Origin(별표 * 값은 모든 출처를 의미), Access-Control-Allow-Methods(허용되는 방법),
Access-Control-Allow-Headers(허용된 헤더) 및 Access-Control-Max-Age(허용된 시간(초)).

크로스-오리진 요청을 처리하도록 CORS를 구성할 수 있습니다. 이를 위해 다음 두 가지를 변경해야 합니다.

- CorsConfiguration 인스턴스를 사용하여 CORS 구성을 처리하는 CorsConfigurationSource 빈을 추가합니다.

- configure() 메서드의 HTTPSecurity에 cors() 메서드를 추가합니다. corsFilter 빈이 추가되면 CorsFilter를 사용하고, 그렇지 않으면 CorsConfigurationSource를 사용합니다. 둘 다 구성되지 않은 경우 Spring MVC 패턴 검사기 핸들러를 사용합니다.

이제 CorsConfigurationSource 빈을 SecurityConfig 클래스에 추가합시다.

기본 허용 값(new CorsConfiguraton().applyPermitDefaultValues())은 허용되는 최대 기간이 30분인 모든 원본(*), 모든 헤더 및 간단한 메서드(GET, HEAD, POST)에 대한 CORS를 구성합니다.

DELETE 메소드를 포함하여 대부분의 모든 HTTP 메소드를 허용해야 하며 더 많은 사용자 정의 구성이 필요합니다. 따라서 다음 빈 정의를 사용합니다.

```java
@Bean
CorsConfigurationSource corsConfigurationSource() {
  CorsConfiguration configuration = new CorsConfiguration();
  configuration.setAllowedOrigins(Arrays.asList("*"));
  configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH"));
   // For CORS response headers
  configuration.addAllowedOrigin("*");
  configuration.addAllowedHeader("*");
  configuration.addAllowedMethod("*");
  UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
  source.registerCorsConfiguration("/**", configuration);
  return source;
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/SecurityConfig.java

여기에서는 기본 생성자를 사용하여 CorsConfiguration 인스턴스를 만든 다음 허용된 출처, 허용된 메서드 및 응답 헤더를 설정합니다. 마지막으로 UrlBasedCorsConfigurationSource 인스턴스에 등록하고 반환하는 동안 인수로 전달합니다

Let's add cors() to HttpSecurity, as follows:

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
  http.httpBasic().disable().formLogin().disable()
      .csrf().ignoringAntMatchers(API_URL_PREFIX, H2_URL_PREFIX)
      .and()
      .headers().frameOptions().sameOrigin() // for H2 Console
      .and()
      .cors()
      .and()
      .authorizeRequests()
      .antMatchers(HttpMethod.POST, TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.DELETE, TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.POST, SIGNUP_URL).permitAll()
      .antMatchers(HttpMethod.POST, REFRESH_URL).permitAll()
      …
      …
}
```
여기에서는 csrf() DSL을 사용하여 CSRF도 구성했습니다. `/api/v1`로 시작하는 URL 및 `/h2-console` H2 데이터베이스 콘솔 URL을 제외한 모든 URL에 CSRF 보호를 적용했습니다. 요구 사항에 따라 구성을 변경할 수 있습니다.

먼저 CSRF/XSRF가 무엇인지 이해합시다.
 CSRF 또는 XSRF는 Cross-Origin Request Forgery의 약자로 웹 보안 취약점입니다. 귀하가 은행 고객이고 현재 은행에 로그인되어 있다고 가정해 보겠습니다. 이메일을 받고 이메일의 링크를 클릭하거나 악성 스크립트가 포함될 수 있는 악성 웹사이트의 링크를 클릭할 수 있습니다. 그런 다음 이 스크립트는 은행에 자금 이체 요청을 보냅니다.

그런 다음 은행은 귀하가 로그인한 상태에서 요청을 보낸 것으로 간주하기 때문에 가해자의 계좌로 자금을 이체합니다. 이것은 단지 예일 뿐입니다. 이러한 공격을 방지하기 위해 애플리케이션은 각각의 새 요청에 대해 로그인한 사용자와 연결된 고유한 CSRF 토큰을 새로 보냅니다. 이러한 토큰은 숨겨진 양식 필드에 저장됩니다. 

사용자가 양식을 제출할 때 동일한 토큰이 요청과 함께 다시 보내져야 합니다. 그런 다음 애플리케이션은 CSRF 토큰을 확인하고 확인이 성공한 경우에만 요청을 처리합니다. 이것은 악성 스크립트가 동일한 출처 정책으로 인해 토큰을 읽을 수 없기 때문에 작동합니다.

그러나 침입자가 CSRF 토큰을 공개하도록 속인다면 그러한 공격을 방지하기가 매우 어렵습니다. REST 끝점만 제공하기 때문에 csrf().disable()을 사용하여 이 웹 서비스에 대한 CSRF 보호를 비활성화할 수 있습니다.

이제 사용자의 역할에 따라 권한을 구성하는 마지막 섹션으로 이동하겠습니다.



## 권한 부여

인증을 위한 유효한 사용자 이름/비밀번호 또는 액세스 토큰을 사용하면 URL, 웹 리소스 또는 보안 웹 페이지와 같은 보안 리소스에 액세스할 수 있습니다. 승인은 한 단계 앞서 있습니다. 이를 통해 읽기, 쓰기와 같은 범위 또는 관리자, 사용자, 관리자 등과 같은 역할로 액세스 보안을 추가로 구성할 수 있습니다. Spring Security를 ​​사용하면 사용자 정의 권한을 구성할 수 있습니다.

샘플 전자 상거래 앱에 대해 세 가지 유형의 역할, 즉 고객(사용자), 관리자 및 고객 지원 담당자를 구성합니다. 분명히 각 사용자는 고유한 특정 권한을 갖습니다. 예를 들어 사용자는 온라인으로 주문하고 물건을 구매할 수 있지만 CSR 또는 관리 리소스에 액세스할 수 없어야 합니다. 마찬가지로 고객 지원 담당자은 관리자 전용 리소스에 액세스할 수 없어야 합니다. 리소스에 대한 권한 또는 역할 기반 액세스를 허용하는 보안 구성을 알려진 권한 부여라고 합니다. 실패한 인증은 HTTP(상태 401 인증되지 않음)를 반환해야 하고 실패한 권한 부여는 HTTP 상태 403(금지됨)을 반환해야 합니다. 이는 사용자가 인증되었지만 리소스에 액세스하는 데 필요한 권한/역할이 없음을 의미합니다.

다음 코드 같이 샘플 전자상거래 앱에서 이 세 가지 역할을 소개하겠습니다.

```java
public enum RoleEnum implements GrantedAuthority {
  USER(Const.USER), ADMIN(Const.ADMIN), CSR(Const.CSR);
  private String authority;

  RoleEnum(String authority) { this.authority = authority; }
  @Override
  @JsonValue
  public String getAuthority() { return authority; }

  @JsonCreator
  public static RoleEnum fromAuthority(String authority) {
    for (RoleEnum b : RoleEnum.values()) {
      if (b.authority.equals(authority)) { return b; }
    }
    throw new IllegalArgumentException("Unexpected value");
  }

  @Override
  public String toString() { return String.valueOf(authority); }
  public class Const {
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String USER = "ROLE_USER";
    public static final String CSR = "ROLE_CSR";
  }
}
```
여기에서 getAuthority() 메서드를 재정의하기 위해 Spring Security의 GrantedAuthority 인터페이스를 구현하는 열거형을 선언했습니다. GrantedAuthority는 Authentication 객체에 부여된 권한입니다. 아시다시피 BearerTokenAuthenticationToken은 인증된 요청에 대한 token/principal 를 나타내는 Authentication  인터페이스를 구현하는 AbstractAuthenticationToken 클래스 유형입니다. 메서드 수준에서 역할 기반 제한을 구성할 때 필요하므로 이 열거형에서 사용자 역할에 대해 문자열 상수를 사용했습니다.

역할과 권한에 대해 자세히 살펴보겠습니다.

### 역할 및 권한

더 세분화된 제어를 위해 권한을 가질 수 있지만 역할은 많은 권한 집합에 적용되어야 합니다. 역할은 ROLE_ 접두사가 있는 권한입니다. 이 접두사는 Spring Security에서 구성할 수 있습니다.

Spring Security는 역할 및 권한 기반 제한을 적용하기 위해 hasRole() 및 hasAuthority() 메서드를 제공합니다. hasRole() 및 hasAuthority()는 거의 동일하지만 hasRole() 메서드는 ROLE_ 접두사 없이 Authority와 매핑됩니다. hasRole('ADMIN')을 사용하는 경우 역할이 권한이고 ROLE_ 접두사가 있어야 하기 때문에 Admin 열거형은 ADMIN 대신 ROLE_ADMIN이어야 합니다. 반면에 hasAuthority('ADMIN')를 사용하는 경우 ADMIN 열거형은 ADMIN이어야 합니다. .

OAuth 2.0 리소스 서버는 기본적으로 스코프(scp) 클레임을 기반으로 권한을 채웁니다. 다른 애플리케이션과의 통합을 위해 주문 내역 등과 같은 사용자 리소스에 대한 액세스를 제공하는 경우 타사 통합을 위해 다른 애플리케이션에 대한 액세스 권한을 부여하기 전에 사용자 계정에 대한 애플리케이션의 액세스를 제한할 수 있습니다. 타사 응용 프로그램은 하나 이상의 스코프를 요청할 수 있습니다. 그런 다음 이 정보는 동의 화면에서 사용자에게 제공되며 애플리케이션에 발급된 액세스 토큰은 부여된 범위로 제한됩니다. 그러나 이 장에서는 OAuth 2.0 권한 부여 흐름을 제공하지 않으며 REST 끝점에 대한 보안 액세스를 제한합니다.

JWT에 "scope"(scp)라는 이름의 클레임이 포함된 경우 Spring Security는 해당 클레임의 값을 사용하여 각 값에 "SCOPE_" 접두사를 붙여 권한을 구성합니다. 예를 들어 페이로드에 scp=["READ","WRITE"] 클레임이 포함된 경우 이는 권한 목록이 SCOPE_READ 및 SCOPE_WRITE로 구성됨을 의미합니다.

스코프(scp) 클레임이 Spring의 OAuth2.0 리소스 서버에 대한 기본 권한이기 때문에 기본 권한 매핑 동작을 변경해야 합니다. 보안 구성에서 OAuth2ResourceServer의 JwtConfigurer에 사용자 지정 인증 변환기를 추가하여 이를 수행할 수 있습니다. 

다음과 같이 변환기를 반환하는 메서드를 추가해 보겠습니다.

```java
private Converter<Jwt, AbstractAuthenticationToken>                                getJwtAuthenticationConverter() {
  JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
  converter.setAuthorityPrefix(AUTHORITY_PREFIX);
  converter.setAuthoritiesClaimName(ROLE_CLAIM);
  JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
  converter.setJwtGrantedAuthoritiesConverter(converter);
  return converter;
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/SecurityConfig.java

여기서 먼저 JwtGrantedAuthorityConverter의 새 인스턴스를 만든 다음 권한 접두사(ROLE_)와 권한 클레임 이름(JWT의 클레임 키)을 역할로 할당합니다.

이제 OAuth 2.0 리소스 서버를 구성하기 위해 이 비공개 방법을 사용할 수 있습니다. 이제 다음 코드를 사용하여 기존 구성을 수정할 수 있습니다. 다음 코드 조각에서 POST `/api/v1/addresses` API 호출에 역할 기반 제한을 추가하기 위한 구성을 추가할 수도 있습니다. 

```java
@Override
protected void configure(HttpSecurity http) throws
    Exception {
  http.httpBasic().disable().formLogin().disable()
      .csrf().ignoringAntMatchers(API_URL_PREFIX, H2_URL_PREFIX)
      .and()
      .headers().frameOptions().sameOrigin() // for H2 Console
      .and()
      .cors()
      .and()
      .authorizeRequests()
      .antMatchers(HttpMethod.POST, TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.DELETE, TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.POST, SIGNUP_URL).permitAll()
      .antMatchers(HttpMethod.POST, REFRESH_URL).permitAll()
      .antMatchers(H2_URL_PREFIX).permitAll()
      .mvcMatchers(HttpMethod.POST, "/api/v1/addresses/**")
      .hasAuthority(RoleEnum.ADMIN.getAuthority())
      .anyRequest().authenticated()
      .and()
      .oauth2ResourceServer(oauth2ResourceServer ->
         oauth2ResourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(getJwtAuthenticationConverter())))
      .sessionManagement().sessionCreationPolicy(
         SessionCreationPolicy.STATELESS);
}
```
이 구성을 설정하여 주소(POST /api/v1/addresses)를 추가하면 이제 인증과 권한 부여가 모두 필요합니다. 이는 로그인한 사용자가 이 끝점을 성공적으로 호출하려면 ADMIN 역할이 있어야 함을 의미합니다. 또한 기본 클레임을 범위에서 역할로 변경했습니다.

이제 메서드 수준 역할 기반 제한을 더 진행할 수 있습니다. Spring Security는 @PreAuthorize, @Secured, @RolesAllowed와 같은 어노테이션 세트를 사용하여 Spring Bean의 공개 메소드에 권한/역할 기반 제한을 둘 수 있는 기능을 제공합니다. 기본적으로 비활성화되어 있으므로 명시적으로 활성화해야 합니다.

다음과 같이 Spring Security 구성 클래스에 @EnableGlobalMethodSecurity(prePostEnabled = true) 주석을 추가하여 이를 활성화합시다.

```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
```

이제 @PreAuthorize(주어진 액세스 제어 표현식이 메소드 호출 전에 평가됨) 및 @PostAuthorize(주어진 액세스 제어 표현식이 메소드 호출 후에 평가됨) 주석을 사용하여 Spring Bean의 공용 메소드에 제한을 둘 수 있습니다. 전역 메서드 수준 보안을 활성화할 때 prePostEnabled 속성을 true로 설정했기 때문입니다.
@EnableGlobalMethodSecurity는 다음 속성도 지원합니다.

- secureEnabled: 공개 메소드에서 @Secured 주석을 사용할 수 있습니다.
- jsr250Enabled: @RolesAllowed와 같은 JSR-250 주석을 사용할 수 있습니다. @RolesAllowed는 공용 클래스와 메서드 모두에 적용할 수 있습니다. 이름에서 알 수 있듯이 액세스 제한에 역할 목록을 사용할 수 있습니다.

@PreAuthorize/@PostAuthorize는 권한/역할에 대해 구성할 수 있을 뿐만 아니라 유효한 SpEL(Spring Expression Language) 표현식을 사용할 수도 있기 때문에 다른 보안 주석보다 강력합니다.

데모 목적으로 @PreAuthorize 주석을 다음 코드 같이 AddressController의 DELETE /v1/auth/addresses/{id}와 연결된 deleteAddressesById() 메서드에 추가해 보겠습니다.

```java
@PreAuthorize("hasRole('" + Const.ADMIN + "')")
@Override
public ResponseEntity<Void> deleteAddressesById(String id) {
  service.deleteAddressesById(id);
  return accepted().build();
}
```
- Here, hasRole() is a built-in SpEL expression. We need to pass a valid SpEL expression, and it should be a String. Any variable used to form this SpEL expression should be final. Therefore, we have declared final string constants in the RoleEnum enum (for example, Const.ADMIN).

- Now, the DELETE /api/v1/addresses/{id} REST API can only be invoked if the user has the ADMIN role.

- Spring Security provides various built-in SpEL expressions, such as hasRole(). Here are some others:

  a. hasAnyRole(String… roles): Returns true if principal's role matches any of the given roles.
  
  b. hasAuthority(String authority): Returns true if principal has given authority. Similarly, you can also use hasAnyAuthority(String… authorities).
  
  c. permitAll: Returns true.
  
  d. denyAll: Returns false.

- isAnonymous(): Returns true if current user is anonymous.

- isAuthenticated(): Returns true if current user is not anonymous.

A full list of these expressions is available at https://docs.spring.io/spring-security/site/docs/current/reference/html5/#el-access.

Similarly, you can apply access restrictions for other APIs. Let's test security in the next section.


## Testing security

You can clone the code and build it using the following command:

Run it from project home
```sh
$ gradlew clean build
```
This code is tested with Java 15.

> IMPORTANT
Make sure to generate the keys again, as keys generated by the JDK keytool are only valid for 90 days.

Then, you can run the application from your project home, as shown in the following code snippet:
```sh
$ java -jar build/libs/Chapter06-0.0.1-SNAPSHOT.jar
```
Now, you must be looking forward to testing. Let's test our first use case.

Let's hit the GET /api/vi/addresses API without the Authorization header, as shown in the following code snippet:
```sh
$ curl -v 'http://localhost:8080/api/v1/addresses' -H 'Content-Type: application/json' -H 'Accept: application/json'
< HTTP/1.1 401
< Vary: Origin
< Vary: Access-Control-Request-Method
< Vary: Access-Control-Request-Headers
< WWW-Authenticate: Bearer
< X-Content-Type-Options: nosniff
< X-XSS-Protection: 1; mode=block
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Other information is removed for brevity
```
This returns HTTP Status 401 (unauthorized) and a WWW-Authenticate: Bearer response header, which suggests the request should be sent with an Authorization header.

Let's send the request again with an invalid token, as follows:
```sh
$  curl -v 'http://localhost:8080/api/v1/addresses' -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9… rest of the JWT string removed for brevity'
< HTTP/1.1 401
< Vary: Origin
< Vary: Access-Control-Request-Method
< Vary: Access-Control-Request-Headers
< WWW-Authenticate: Bearer error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Jwt expired at 2021-01-09T14:19:49Z", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"
< Other information is removed for brevity
```
서버는 다시 401(승인되지 않음)로 응답하지만 이번에는 지정된 토큰이 만료되었음을 알려주는 오류 메시지와 설명이 표시됩니다. 또한 여기에 설명된 대로 지정된 전달자 토큰을 기반으로 HTTP 상태 401과 함께 invalid_token 오류를 보낼 수도 있습니다.

- 토큰이 올바른 형식의 JWT가 아닌 경우 Jwt를 디코딩하는 동안 오류가 발생했습니다: 잘못된 JWT 직렬화: 점 구분 기호 누락이 표시됩니다.

- 토큰에 유효한 서명이 없으면 Jwt 디코딩을 시도하는 동안 오류가 발생했습니다: 서명된 JWT 거부됨: 잘못된 서명이 표시됩니다.

Flyway 데이터베이스 마이그레이션 스크립트를 사용하여 scott/tiger 및 scott2/tiger라는 두 명의 사용자를 만들었습니다. 이제 다음과 같이 유효한 JWT를 가져오기 위해 사용자 이름 scott로 로그인을 수행해 보겠습니다. 

```sh
$ curl -X POST 'http://localhost:8080/api/v1/auth/token' -H 'Content-Type: application/json' -H 'Accept: application/json' -d '{
    "username": "scott",
    "password": "tiger"
}'
{
    "refreshToken": "3i2tlrmdqnp60drl6i9c2kdm36s48qg5vm2ucgt flsk0cjo4dthhjan9aj1ck83det8m8hkl461cqkfl57puk81ct6j09ilpo ranf1jj414ht4ob7dkcakq6lk92cnct",
    "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzY290dCIsInJvbGVzIjpbIlVTRVI iXSwiaXNzIjoiTW9kZXJuIEFQSSBEZXZlbG9wbW VudCB3aXRoIFNwcmluZyBhbmQgU3ByaW5nIEJvb 3QiLCJleHAiOjE2MTA5NTQ3NzMsImlhdCI6MTYxMDk1Mzg3M30.GOE2WwgN-1s82KqU2U-hd7rcrhdblrfV59HXTL9B7BL2eAgshjxtJGVhj9CtR_LQwA54fZo0yVwYFyMUrQBBFkn_2fDRU_8j1LD91N HDO1wqpiVx9kRzB9nUIR0OcpT2OdMaPD_HpmRiQOchwZSxsi7_c5dO59-URJn17ahXeBDJoAFrYQGmetjvuZtGwd9nLAvdSq9KKOL_gLle1wqsjJOYqJ9l_djLzeaO3Xgg-Kva5rmyZP0tWws7A95H2Si2tIqRGESZUCAQ3GbezpZB2OO_YgyCkQSuJkFTuQWc1MFbqtgeRcRiklX53BBngcHCfAeOAsBtKL17yXnd-IQSPn1GBLmCJh1-nMgrwAKS-lbx1k55FI93qGVoXDFFnVRUgjf_mA5aKNx9VECDtaXLDR7TA7LgjXiDXJ3ZPRNsF3-8fagHNKq42BjPdGH62XtWBve_Ide00DXNtSffHAlo2ukjGpN_zdmuZu7-UNeObg3g_dD6vvSnfupylJbVJooVDOQctR0u-ausMNKvh32NBG4-IQS2pW5Xo3i3l0GEtTP8AIy0vtafnFWBJI_OwTKVM8s966cgliswmeahLxIpLPeSo4Q0NxdE7MDnGg8wUbnzxoiq-ExcUjm_e7M2N7LMgdlsk0asQQYOJDe50EwMr2oE9ZDQepTtqwfSjcpKdKQ",
    "username": "scott",
    "userId": "a1b9b31d-e73c-4112-af7c-b68530f38222"
}
```
리프레시 및 액세스 토큰과 함께 반환됩니다. 이 액세스 토큰을 사용하여 GET /api/v1/addresses API를 다시 호출해 보겠습니다(Authorization 헤더의 Bearer 토큰 값은 이전 GET /api/v1/auth/token API 호출의 응답에서 가져옴). 코드는 다음에 나와 있습니다.

```
$  curl -v 'http://localhost:8080/api/v1/addresses' -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.ey… rest of the JWT string removed for brevity'
< Response
[
    {
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/api/v1/                addresses/a731fda1-aaad-42ea-bdbc-a27eeebe2cc0"
            }
        ],
        "id": "a731fda1-aaad-42ea-bdbc-a27eeebe2cc0",
        "number": "9I-999",
        "residency": "Fraser Suites Le Claridge",
        "street": "Champs-Elysees",
        "city": "Paris",
        "state": "Île-de-France",
        "country": "France",
        "pincode": "75008"
    }
]
```
This time, the call is successful. Now, let's use the refresh token to get a new access token, as follows:

```sh
$ curl -X POST 'http://localhost:8080/api/v1/auth/token/refresh'
-H 'Content-Type: application/json' -H 'Accept: application/json'
-d '{
    "refreshToken": "3i2tlrmdqnp60drl6i9c2kdm36s48qg5vm2ucgtflsk0cjo4dth hjan9aj1ck83det8m8hkl461cqkfl57puk81ct6j09ilporanf1j j414ht4ob7dkcakq6lk92cnct"
}'
```

```
< Response
{
    "refreshToken": "3i2tlrmdqnp60drl6i9c2kdm36s48qg5vm2ucgtflsk0cjo4dthhjan9 aj1ck83det8m8hkl461cqkfl57puk81ct6j09ilporanf1jj4 14ht4ob7dkcakq6lk92cnct",
    "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.
                    Rest of token truncated for brevity",
    "username": "scott",
    "userId": "a1b9b31d-e73c-4112-af7c-b68530f38222"
}
```

이번에는 페이로드에 제공된 것과 동일한 리프레시 토큰을 사용하여 새 액세스 토큰을 반환합니다.

리프레시 토큰 API를 호출하는 동안 잘못된 리프레시 토큰을 전달하면 다음 응답이 제공됩니다.

```json
{"errorCode":"PACKT-0010",
"message":"Requested resource not found. Invalid token.", "status":404,"url":"http://localhost:8080/api/v1/auth/token/refresh","reqMethod":"POST","timestamp":"2021-01-18T07:20:35.846649200Z"}
```

JWT를 사용하여 인증을 테스트한 후 이제 인증을 테스트할 수 있습니다. 사용자 SCOTT가 생성한 토큰을 사용하여 주소를 생성해 보겠습니다. SCOTT에는 USER 역할이 있습니다. 이를 수행하는 코드는 다음과 같습니다. 

```sh
$ curl -v -X POST 'http://localhost:8080/api/v1/addresses' -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.ey
```
Rest of the token is truncated for brevity'
```
-d '{
    "number": "9I-999",
    "residency": "Fraser Suites Le Claridge",
    "street": "Champs-Elysees",
    "city": "Paris",
    "state": "Ile-de-France",
    "country": "France",
    "pincode": "75008"
}'
< HTTP/1.1 403
< Vary: Origin
< Vary: Access-Control-Request-Method
< Vary: Access-Control-Request-Headers
< WWW-Authenticate: Bearer error="insufficient_scope", error_description="The request requires higher privileges than provided by the access token.", error_uri="https://tools.ietf.org/html/r
fc6750#section-3.1"
< output truncated for brevity
```
API는 SCOTT에 USER 역할이 있고 ADMIN 역할을 가진 사용자만 액세스할 수 있도록 이 API를 구성했기 때문에 403(forbidden)으로 응답했습니다.

다음 코드와 함께 ADMIN 역할이 있는 SCOTT2 사용자를 사용하여 토큰을 다시 생성해 보겠습니다. 

```sh
$ curl -X POST 'http://localhost:8080/api/v1/auth/token' -H 'Content-Type: application/json' -H 'Accept: application/json' -d '{
    "username": "scott2",
    "password": "tiger"
}'
```
Now, let's call the create address API again using the access token received from the SCOTT2 sign-in, as shown in the following code snippet:

```sh
$ curl -X POST 'http://localhost:8080/api/v1/addresses'
-H 'Content-Type: application/json' -H 'Accept: application/json'
-H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzd
-d '{
    "number": "9I-999",
    "residency": "Fraser Suites Le Claridge",
    "street": "Champs-Elysees",
    "city": "Paris",
    "state": "Ile-de-France",
    "country": "France",
    "pincode": "75008"
}'
```
< Response
```json
{"_links":{"self":[{"href":"http://localhost:8080/b78d485e-16a0-4b11-98d2-6e4dadbc60e7"},{"href":"http://localhost:8080/api/v1/addresses/b78d485e-16a0-4b11-98d2-6e4dadbc60e7"}]},"id":"b78d48
5e-16a0-4b11-98d2-6e4dadbc60e7","number":"9I-999","residency":"Fraser Suites Le Claridge","street":"Champs-Elysees","city":"Paris","state":"Ile-de-France","country":"France","pincode":"75008
"}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/Chapter06.postman_collection.json

마찬가지로 REST API를 사용하여 주소 작업 삭제를 시도할 수 있습니다. 이렇게 하면 ADMIN 역할만 작업을 수행할 수 있습니다.

Postman Collection 버전 2.1을 기반으로 하는 이 장의 API의 Postman(API 클라이언트) 컬렉션은 다음 위치에서 찾을 수 있습니다. 가져온 다음 API를 테스트할 수 있습니다. 

## 요약

이 장에서는 JWT, Spring Security, 필터를 사용한 인증, Spring OAuth 2.0 Resource Server를 사용한 인증 및 필터를 사용한 JWT 토큰 유효성 검사에 대해 배웠습니다. 또한 CORS 및 CSRF 보호를 추가하는 방법과 이것이 필요한 이유를 배웠습니다.

역할 및 권한을 기반으로 한 액세스 보호에 대해서도 배웠습니다. 이제 웹 리소스를 보호하기 위해 JWT, Spring Security 및 Spring Security OAuth 2.0 리소스 서버를 구현하는 기술을 갖추었습니다.

다음 장에서는 이 장에서 사용되는 Spring Security 프레임워크와 API를 사용하여 샘플 전자상거래 앱의 UI를 개발할 것입니다. 이 통합을 통해 UI 흐름과 JavaScript를 사용하여 REST API를 사용하는 방법을 이해할 수 있습니다.

## 질문

- 보안 컨텍스트 및 보안 주체란 무엇입니까?

- JWT를 보호하는 데 선호되는 방법(토큰 서명 또는 암호화)은 무엇입니까?

- JWT를 사용하기 위한 모범 사례는 무엇입니까?
  
## Further reading

Hands-On Spring Security 5.x (video course):
https://www.packtpub.com/product/hands-on-spring-security-5-x-video/9781789802931

Spring Security documentation:
https://docs.spring.io/spring-security/site/docs/current/reference/html5/

List of filters available in Spring Security:
https://docs.spring.io/spring-security/site/docs/current/reference/html5/#servlet-security-filters

JWT:
https://tools.ietf.org/html/rfc7519

JWS:
https://www.rfc-editor.org/info/rfc7515

JWE:
https://www.rfc-editor.org/info/rfc7516

Spring Security in-built SpEL expressions:
https://docs.spring.io/spring-security/site/docs/current/reference/html5/#el-access
