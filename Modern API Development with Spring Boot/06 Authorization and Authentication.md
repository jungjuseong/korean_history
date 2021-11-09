
# 6장: 보안(권한 부여 및 인증)

이전 장에서 우리는 명령형 및 반응형 코딩 스타일을 사용하여 RESTful 웹 서비스(여기서 REST는 REpresentational State Transfer를 나타냄)를 개발했습니다. 이제 Spring Security를 ​​사용하여 이러한 REST 엔드포인트를 보호하는 방법을 배우게 됩니다. REST 끝점에 대한 토큰 기반 인증 및 권한 부여를 구현합니다. 

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

- Any IDE such as NetBeans, IntelliJ IDEA, or Eclipse

- Java Development Kit (JDK) 15

- An internet connection to clone the code and download the dependencies from Gradle

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
다양한 권한 부여 흐름을 사용하여 상태 비저장 HTTP(HyperText Transfer Protocol) 끝점 또는 리소스를 호출할 수 있습니다.

2장, Spring 개념과 REST API에서 DispatcherServlet에 대해 배웠습니다. 이것은 클라이언트 요청과 REST 컨트롤러 간의 인터페이스입니다. 따라서 토큰 기반 인증 및 권한 부여에 대한 논리를 배치하려면 요청이 DispatcherServlet에 도달하기 전에 이를 수행해야 합니다. Spring Security 라이브러리는 요청이 DispatcherServlet에 도달하기 전에 처리되는 서블릿 사전 필터(필터 체인의 일부로)를 제공합니다. 사전 필터는 실제 서블릿에 도달하기 전에 처리되는 서블릿 필터이며, Spring Security의 경우 DispatcherServlet입니다. 유사하게, 포스트 필터는 요청이 서블릿/컨트롤러에 의해 처리된 후에 처리됩니다.

토큰 기반(JWT) 인증을 구현할 수 있는 두 가지 방법이 있습니다. 

- spring-boot-starter-security
- spring-boot-starter-oauth2-resource-server
 
우리는 후자를 사용할 것입니다.

The former contains the following libraries:

- spring-security-core
- spring-security-config
- spring-security-web

`spring-boot-starter-oauth2-resource-server` provides the following, along with all three previously mentioned Java ARchive files (JARs):

- spring-security-oauth2-core
- spring-security-oauth2-jose
- spring-security-oauth2-resource-server

When you start this chapter's code, you can find the following log. You can see that, by default, DefaultSecurityFilterChain is auto-configured. The log statement lists down the configured filters in the DefaultSecurityFilterChain, as shown in the following code block:
```
INFO [Chapter06,,,]     [null] [null] [null]     [null] 24052 --- [main] o.s.s.web.DefaultSecurityFilterChain     : Will secure any request with [org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter@413e8246, org.springframework.security.web.context.SecurityContextPersistenceFilter@659565ed, org.springframework.security.web.header.HeaderWriterFilter@770c3ca2, org.springframework.web.filter.CorsFilter@4c7b4a31, org.springframework.security.web.csrf.CsrfFilter@1de6f29d, org.springframework.security.web.authentication.logout.LogoutFilter@5bb90b89, org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter@732fa176, org.springframework.security.web.savedrequest.RequestCacheAwareFilter@2ae0eb98, org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter@3f473daf, org.springframework.security.web.authentication.AnonymousAuthenticationFilter@2df7766b, org.springframework.security.web.session.SessionManagementFilter@711261c7, org.springframework.security.web.access.ExceptionTranslationFilter@1a7f2d34, org.springframework.security.web.access.intercept.FilterSecurityInterceptor@3390621a]
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

드디어 컨트롤러에 도착

이 필터 체인은 향후 릴리스에서 변경될 수 있습니다. 또한 spring-boot-starter-security를 사용했거나 구성을 변경한 경우 보안 필터 체인이 달라집니다. springSecurityFilterChain에서 사용 가능한 모든 필터는 https://docs.spring.io/spring-security/site/docs/current/reference/html5/#servlet-security-filters에서 찾을 수 있습니다. 

필터를 사용하여 인증을 수행하는 방법을 살펴보겠습니다.


## 필터를 사용하여 인증하는 방법 배우기

필터 기반 인증을 이미 알고 있는 경우 이 섹션을 건너뛰고 OAuth 2.0 리소스 서버를 사용한 인증 섹션으로 건너뛸 수 있습니다.

수동 필터 구성이 필요하지 않은 인증 구현을 위해 spring-boot-starter-oauth2-resource-server 종속성을 사용할 것입니다. oauth2-resource-server는 인증을 위해 `BearerTokenAuthenticationFilter`를 사용합니다. 그러나 필터 기반 인증 구현 및 구성을 이해하면 Spring Security 개념을 단순화할 수 있습니다. 필터 기반 인증 및 권한 부여를 위해 spring-boot-starter-spring-security를 ​​추가하면 됩니다.

적절한 사전 필터에 인증 논리를 추가할 수 있습니다. 요청이 인증에 실패하면 액세스 거부 예외(AccessDeniedException)와 함께 응답이 클라이언트에 전송되고 그 결과 HTTP 401 Unauthorized 오류 상태 응답 코드가 표시됩니다.

### 필터를 사용한 사용자 이름/비밀번호 인증 흐름

사용자 이름/암호를 사용한 인증은 다음 다이어그램과 같이 작동합니다. 사용자가 유효한 사용자 이름/암호 조합을 제출하면 호출이 성공하고 사용자는 200 OK 상태 코드(성공적인 응답)가 포함된 토큰을 받습니다. 잘못된 사용자 이름/비밀번호 조합으로 인해 통화가 실패하면 401 Unauthorized 상태 코드가 포함된 응답을 받게 됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_6.1_B16561.jpg)

Figure 6.1 – Username/password 인증 흐름

Now, let's have a look at a token authorization flow using filters.

### 필터를 사용한 토큰 인증 흐름

토큰을 사용한 권한 부여는 다음 다이어그램과 같이 작동합니다. 사용자가 Authorization 헤더와 함께 유효한 bearer 토큰을 제출하면 호출이 성공하고 FilterChain.doFilter(request,response)를 호출합니다. 따라서 호출은 DispatcherServlet을 통해 컨트롤러로 라우팅됩니다. 마지막에 클라이언트는 적절한 상태 코드가 포함된 응답을 받습니다.

잘못된 토큰으로 인해 호출이 실패하면 AccessDeniedException이 발생하고 401 Unauthorized 상태 코드와 함께 AuthenticationEntryPoint에서 응답이 전송됩니다. AuthenticationEntryPoint 인터페이스를 구현하고 그 begin() 메서드를 재정의하여 이 동작을 재정의할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_6.2_B16561.jpg)

그림 6.2 – BasicAuthenticationFilter를 사용한 토큰 인증 흐름

먼저 Gradle 빌드 파일에 필요한 종속성을 추가해 보겠습니다.

### 필요한 Gradle 종속성 추가

다음과 같이 build.gradle 파일에 다음 종속성을 추가해 보겠습니다.
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

클라이언트는 유효한 사용자 이름/암호 조합을 제공하여 성공적인 로그인을 수행한 후 JWT 토큰을 받습니다. Spring Security는 확장할 수 있는 UsernamePasswordAuthenticationFilter를 제공하고 attemptAuthentication() 및 successAuthentication() 메서드를 재정의할 수 있습니다. 먼저 다음과 같이 LoginFilter 클래스를 생성해 보겠습니다.

```java
public class LoginFilter extends  UsernamePasswordAuthenticationFilter {
  private final AuthenticationManager  authenticationManager;
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
public Authentication attemptAuthentication(
    HttpServletRequest req,HttpServletResponse res)
    throws AuthenticationException {
  if (!req.getMethod().equals(HttpMethod.POST.name())) {
    throw new MethodNotAllowedException(req.getMethod(),
        List.of(HttpMethod.POST));
  }
  try (InputStream is = req.getInputStream()) {
    SignInReq user = new ObjectMapper().readValue(          is, SignInReq.class);
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
org.springframework.security.authentication.AuthenticationManager is a component of Spring Security. We simply use it to authenticate by passing the username, password, and authorities. Authorities are passed as an empty list; however, you can pass the authorities too if they are either received from the request payload or by fetching them from the database/memory store. SignInReq is a Plain Old Java Object (POJO) that contains the username and password fields.
Once the login is successful, we need to return a JWT in response. The successfulAuthentication() method is overridden for the same purpose, as illustrated in the following code snippet:

```java
@Override
protected void successfulAuthentication(
    HttpServletRequest req,
    HttpServletResponse res,
    FilterChain chain,
    Authentication auth) throws IOException {
  User principal = (User) auth.getPrincipal();
  String token = tokenManager.create(principal);
  SignedInUser user = new SignedInUser()
            .username(principal.getUsername()).accessToken(
                      token);
  res.setContentType(MediaType.APPLICATION_JSON_VALUE);
  res.setCharacterEncoding("UTF-8");
  res.getWriter().print(mapper.writeValueAsString(user));
  res.getWriter().flush();
}
```

Here, a token is created using the tokenManager.create() method. SignedInUser is a POJO that contains the username and token fields.
The client receives the username and token as a response after successful authentication and can then use this token in the Authorization header by prefixing the token value with "Bearer". Let's see how you can configure and add these new filters in Spring Security filter chains.

### Configuring Spring Security

There is a final piece missing here about how AuthenticationManager.authenticate() works. AuthenticationManager uses the UserDetailsService bean internally— this has a single method, as shown next:
```java
UserDetails loadUserByUsername(String username) throws
                               UsernameNotFoundException
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
Here, @EnableWebSecurity annotation is applied to configure WebSecurityConfigurer or the class that extends WebSecurityConfigurerAdapter and customize the WebSecurity class. This class does the auto-configuration for security, with some override methods. The configure() method allows you to configure the HTTP security as a domain-specific language (DSL), using the methods shown here:
```java
@Override
protected void configure(HttpSecurity http) throws      Exception {
   http.authorizeRequests()
        .antMatchers(HttpMethod.POST, SIGN_UP_URL).permitAll()
        .anyRequest().authenticated()
        .and()
        .addFilter(new LoginFilter(             super.authenticationManager(), mapper))
        .addFilter(new JwtAuthenticationFilter(             super.authenticationManager()))
        .sessionManagement().sessionCreationPolicy(             SessionCreationPolicy.STATELESS);
}
```
The configure() method demonstrates how you can configure the HTTP security using DSL and override its default implementation. Let's understand the code, as follows:
You can see that the login URL is permitted without authentication by using the antMatchers() method. If you don't pass the HTTP method to antMatchers(), it will be applicable for all the HTTP methods. permitAll() removes the restriction on endpoints and their attached HTTP methods.
You can add custom filters using addFilter(). All other URLs need authentication.
Two extra filters have been added for sign-in operations and JWT token-based authentication.
At the end, the session policy is set to STATELESS because we are going to use REST endpoints.
We have coded the login filter in this section. Now, let's add the JwtAuthenticationFilter class for token verification.
Coding the filters for token verification
Let's explore how you can implement authentication using filters. If you use the spring-boot-starter-security dependency, then you can extend the BasicAuthenticationFilter class and override the doFilterInternal method for token verification, as shown in the code blocks that follow.
First, create a new class that extends the BasicAuthenticationFilter class, as follows:
public class JwtAuthenticationFilter extends
                          BasicAuthenticationFilter {
  public JwtAuthenticationFilter(AuthenticationManager
                                    authenticationManager) {
    super(authenticationManager);
  }
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/UNUSED/JwtAuthenticationFilter.java
Now, we can override the doFilterInternal method. Here, we check whether the request contains the Authorization header with a bearer token or not. If it has the Authorization header then it performs the authentication, adds the token to security context, and then passes the call to the next security filter. The code is illustrated in the following snippet:
@Override
protected void doFilterInternal(HttpServletRequest req,        HttpServletResponse res, FilterChain chain) throws                    IOException, ServletException {
  String header = req.getHeader("Authorization");
  if (Objects.isNull(header) || !header.startsWith("Bearer ")) {
    chain.doFilter(req, res);
    return;
  }
  Optional<UsernamePasswordAuthenticationToken>     authentication = getAuthentication(req);
  authentication.ifPresentOrElse(e ->
      SecurityContextHolder.getContext().setAuthentication(          e), SecurityContextHolder::clearContext);
  chain.doFilter(req, res);
}
The getAuthentication() method performs the token authentication logic, as shown in the following code snippet:
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
      return Optional.of(
              new UsernamePasswordAuthenticationToken(
          user, null, Objects.nonNull(authorities) ?
          authorities.stream().map(SimpleGrantedAuthority::
              new)
          .collect(Collectors.toList()) :
              Collections.emptyList()));
    }
  }
  return Optional.empty();
}
Here, JWT and DecodedJWT are part of the com.auth0:java-jwt library. Calling verify() performs the verification of the given token and returns a DecodedJWT instance. If verification fails, it returns JWTVerificationException. Once verification is done, we simply create and return the UsernamePasswordAuthenticationToken token that takes the principal, credentials, and collection of GrantedAuthority objects. GrantedAuthority is an interface that represents the authority associated with an authentication object. OAuth2 Resource Server lets you add scope authority by default. However, you can add custom authority such as roles.
So far, we have learned about the authentication and token authorization flow using a Spring filter chain. Next, we are going to implement the authentication using the spring-boot-starter-oauth2-resource-server dependency. In the next section, we'll explore the authentication and authorization flow using OAuth 2.0 Resource Server.
Authentication using OAuth 2.0 Resource Server
Spring Security OAuth 2.0 Resource Server allows you to implement authentication and authorization using BearerTokenAuthenticationFilter. This contains bearer token authentication logic. However, you still need to write the REST endpoint for generating the token. Let's explore how the authentication flow works in OAuth2.0 Resource Server. Have a look at the following diagram:
Figure 6.3 – Token authentication flow using OAuth 2.0 Resource Server
Figure 6.3 – Token authentication flow using OAuth 2.0 Resource Server
Let's understand the flow depicted in Figure 6.3 , as follows:
The client sends a GET HTTP request to /api/v1/addresses.
BearerTokenAuthenciationFilter comes into action. If the request doesn't contain the Authorization header then BearerTokenAuthenticationFilter does not authenticate the request since it did not find the bearer token. It passes the call to FilterSecurityInterceptor, which does the authorization. It throws an AccessDeniedException exception (marked as 2 in Figure 6.3). ExceptionTranslationFilter springs into action. Control is moved to BearerTokenAuthenticationEntryPoint, which responds with a 401 Unauthorized status and a WWW-Authenticate header with a Bearer value. If the client receives a WWW-Authenticate header with a Bearer value in response, it means it has to retry with the Authorization header that holds the valid bearer token. At this stage, the request cache is NullRequestCache (that is, empty) due to security reasons because the client can replay the request.
Let's assume the HTTP request contains an Authorization header. It extracts the Authorization header from the HTTP request and, apparently, the token from the Authorization header. It creates an instance of BearerTokenAuthenticationToken using the token value. BearerTokenAuthenticationToken is a type of AbstractAuthenticationToken class that implements an Authentication interface representing the token/principal for the authenticated request.
The HTTP request is passed to AuthenticationManagerResolver, which provides the AuthenticationManager based on the configuration. AuthenticationManager verifies the BearerTokenAuthenticationToken token.
If authentication is successful, then Authentication is set on the SecurityContext instance. This instance is then passed to SecurityContextHolder. setContext(). The request is passed to the remaining filters for processing and then routes to DispatcherServlet and then, finally, to AddressController.
If authentication fails, then SecurityContextHolder.clearContext() is called to clear the context value. ExceptionTranslationFilter springs into action. Control is moved to BearerTokenAuthenticationEntryPoint, which responds with a 401 Unauthorized status and a WWW-Authenticate header with a value that contains the appropriate error message, such as Bearer error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Jwt expired at 2020-12-14T17:23:30Z", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1".
Exploring the fundamentals of JWT
You need an authority in the form of permissions or rights to carry out any activity or access any information. This authority is known as a claim. A claim is represented as a key-value pair. The key contains the claim name and the value contains the claim that can be a valid JSON value. A claim can also be metadata about the JWT.
HOW IS JWT PRONOUNCED?
As per https://tools.ietf.org/html/rfc7519, the suggested pronunciation of JWT is the same as the English word jot.
A JWT is an encoded string that contains of set of claims. These claims are either digitally signed by a JSON Web Signature (JWS) or encrypted by JSON Web Encryption (JWE). JWT is a self-contained way to transmit claims securely between parties. The links for these Request for Comments (RFC) proposed standards are provided in the Further reading section of this chapter.
JWT structure
A JWT is an encoded string such as aaa.bbb.ccc, consisting of the following three parts separated by dots (.):
Header
Payload
Signature
A few websites such as https://jwt.io/ or https://www.jsonwebtoken.io/ allow you to view the content of a JWT and generate a JWT.
Let's have a look at the following sample JWT string. You can paste it into one of the previously mentioned websites to decode the content:
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzY290dCIsInJvbGVzIjpbIlVTRVIiXSwiaX NzIjoiTW9kZXJuIEFQSSBEZXZlbG9wbWVudCB3aXRoIFNw cmluZyBhbmQgU3ByaW5nIEJvb3QiLCJleHAiOjE2MTA1Mj A2MjksImlhdCI6MTYxMDE5ODIzNywianRpIjoiMjk3ZGY4 YTctNTE4Zi00ZWQ3LWJhNjYtOTJkYTQ5NGRkZDc2In0.MW-QOgAcNwLoEYINzqnDSm73-N86yf29-RUJsrApDyg
This sample token demonstrates how a JWT is formed and divided into three parts using dots.
Header
A header consists of a Base64URL-encoded JSON string, normally containing two key-value pairs: a type of token (with a typ key) and a signing algorithm (with an alg key).
A sample JWT string contains the following header:
{
  "typ": "JWT",
  "alg": "HS256"
}
The preceding header contains typ and alg fields, representing type and algorithm respectively.
Payload
A payload is a second part of the JWT and contains the claims. This is also a Base64URL-encoded JSON string. There are three types of claims—registered, public, and private. These are outlined as follows:
Registered claims: A few claims are registered in the Internet Assigned Numbers Authority (IANA) JSON Web Token Claims registry, therefore these claims are known as Registered claims. These are not mandatory but are recommended. Some Registered claims are listed here:
a. Issuer claim (iss key): This claim identifies the principal who issued a token.
b. Subject claim (sub key): This should be a unique value that represents the subject of the JWT.
c. Expiration Time claim (exp key): This is a numeric value representing the expiration time on or after which a JWT should be rejected.
d. Issued At claim (iat key): This claim identifies the time at which a JWT is issued.
e. JWT ID claim (jti key): This claim represents the unique identifier for a JWT.
f. Audience claim (aud key): This claims identifies the recipients, which JWT is intended for.
g. Not Before claim (nbf key): Represents the time before which a JWT must be rejected.
Public claims: These are defined by JWT issuers and must not collide with registered claims. Therefore, these should either be registered with the IANA JWT Claims Registry or defined as a Uniform Resource Identifier (URI) with a collision-resistant namespace.
Private claims: These are custom claims defined and used by the issuer and audience. They are neither registered nor public.
Here is a sample JWT string containing the following payload:
{
  "sub": "scott",
  "roles": [
    "USER"
  ],
  "iss": "Modern API Development with Spring and Spring
         Boot",
  "exp": 1610520629,
  "iat": 1610198237,
  "jti": "297df8a7-518f-4ed7-ba66-92da494ddd76"
}
The preceding payload contains sub (subject), iss (issuer), roles (custom claim roles), exp (expires), iat (issued at), and jti (JWT ID) fields.
Signature
A signature is also a Base64-encoded string—a third part. A signature is there to protect the content of the JWT. The content is visible, but cannot be modified if the token is signed. A Base64-encoded header and payload are passed to the signature's algorithm, along with either a secret or a public key to make the token a signed token. If you wish to include any sensitive or secret information in the payload, then it's better to encrypt it before assigning it to the payload.
A signature makes sure that the content is not modified once it is received back. The use of a public/private key enhances the security step by verifying the sender.
You can use a combination of both a JWT and JWE. The recommended way, however, is to first encrypt the payload using JWE and then sign it.
We'll use the public/private keys to sign the token in this chapter. Let's jump into the code.
Securing REST APIs with JWT
In this section, you'll secure the REST endpoints exposed in Chapter 4, Writing Business Logic for APIs. Therefore, we'll use the code from Chapter 4, Writing Business Logic for APIs and enhance it to secure the APIs.
The REST APIs should be protected with the following features:
No secure API should be accessed without JWT.
A JWT can be generated using sign-in/sign-up or a refresh token.
A JWT and a refresh token should only be provided for a valid user's username/password combination or a valid user sign-up.
The password should be stored in encoded format using a bcrypt strong hashing function.
The JWT should be signed with RSA (for Rivest, Shamir, Adleman) keys with a strong algorithm.
Claims in the payload should not store sensitive or secured information. If they do, then these should be encrypted.
You should be able to authorize API access for certain roles.
We need to include new APIs for the authorization flow. Let's add them first.
Learning new API definitions
You will enhance the existing APIs by adding four new APIs—sign-up, sign-in, sign-out, and a refresh token. Sign-up, sign-in, and sign-out operations are self-explanatory.
The refresh token provides a new access token (JWT) once the existing token expires. This is the reason why the sign-up/sign-in API provides two types of tokens—an access token and a refresh token—as a part of its response. The JWT access token self-expires, therefore a sign-out operation would only remove the refresh token.
Let's add these APIs into the openapi.yaml document.
Modifying the API specification
Apart from adding the new APIs, you also need to add a new user tag for these APIs that will expose all these APIs through the UserApi interface. Let's first add a sign-up endpoint.
Sign-up endpoint
Add the following specification for the sign-up endpoint in openapi.yaml:
/api/v1/users:
  post:
    tags:
      - user
    summary: Signup the a new customer (user)
    description: Creates a new customer (user), who can
                 login and do the shopping.
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
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/resources/api/openapi.yaml
Add the following new model, SignedInUser, to the schemas. This contains accessToken, refreshToken, username, and user ID fields. The code to add the model is shown in the following snippet:
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
Now, let's add the sign-in endpoint.
Sign-in endpoint definition
Add the following specification for the sign-in endpoint in openapi.yaml:
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
We need a new model, SignInReq, for the sign-in request payload. It just contains the username and password fields. Let's add it, as follows:
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
Sign-out endpoint
Add the following specification for the sign-out endpoint in openapi.yaml:
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
In an ideal scenario, you should remove the refresh token of a user received from the request. You can fetch the user ID from the token and then use that ID to remove it from the USER_TOKEN table. However, in that case, you should send a valid access token.
We have opted for an easy way to remove the token, which is for it to be sent by the user as a payload. Therefore, this endpoint needs the following new model, RefreshToken. Here is the code to add the model:
RefreshToken:
  description: Contains the refresh token
  type: object
  properties:
    refreshToken:
      description: Refresh Token
      type: string
Finally, let's add an endpoint for refreshing the access token.
Refresh token endpoint
Add the following specification for the refresh token endpoint in openapi.yaml:
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
Here, we have raised an exception by defining the refresh endpoint in terms of forming a URI that represents the refresh token resources. Instead, it generates a new access token.
In the existing code, we don't have a table for storing the refresh token. Therefore, let's add one.
Storing the refresh token using a database table
You can modify the Flyway database script to add a new table, as shown in the following code snippet:
create TABLE IF NOT EXISTS ecomm.user_token (
id uuid NOT NULL DEFAULT random_uuid(),
refresh_token varchar(128),
user_id uuid NOT NULL,
PRIMARY KEY(id),
FOREIGN KEY (user_id)
  REFERENCES ecomm.user(id)
);
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/resources/db/migration/V1.0.0__Init.sql
Now, you can start writing the implementation code for JWT.
Implementing the JWT manager
Let's add a constant class that contains all the constants related to the security functionality before we implement the JWT manager class, as shown in the following code snippet:
public class Constants {
  public static final String ENCODER_ID = "bcrypt";
  public static final String API_URL_PREFIX = "/api/v1/**";
  public static final String H2_URL_PREFIX = "/h2-
    console/**";
  public static final String SIGNUP_URL = "/api/v1/users";
  public static final String TOKEN_URL =
    "/api/v1/auth/token";
  public static final String REFRESH_URL =
                              "/api/v1/auth/token/refresh";
  public static final String AUTHORIZATION =
    "Authorization";
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String SECRET_KEY = "SECRET_KEY";
  public static final long EXPIRATION_TIME = 900_000;
  public static final String ROLE_CLAIM = "roles";
  public static final String AUTHORITY_PREFIX = "ROLE_";
}
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/Constants.java
These constants are self-explanatory, except the EXPIRATION_TIME long value, which represents 15 minutes as a time unit.
Now, we can define the JWT manager class—JwtManager. JwtManager is a custom class that is responsible for generating a new JWT. It uses the java-jwt library from auth0.com. You are going to use public/private keys for signing the token. Let's define this class, as follows:
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
        .withClaim(ROLE_CLAIM,
               principal.getAuthorities().stream()
           .map(GrantedAuthority::getAuthority).collect(               toList()))
        .withIssuedAt(new Date(now))
        .withExpiresAt(new Date(now + EXPIRATION_TIME))
        .sign(Algorithm.RSA256(pubKey, privateKey));
  }
}
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/JwtManager.java
Here, JWT is a class from the java-jwt library that provides a fluent API for generating the token. It adds issuer ("iss"), subject ("sub"), issued at ("iat"), and expired at ("exp") claims.
It also adds a custom claim, ROLE_CLAIMS ("roles"), which is populated using authorities from UserDetails. UserDetails is an interface provided by Spring Security. You can use the org.springframework.security.core.userdetails.User.builder() method to create a UserBuilder class. UserBuilder is a final builder class that allows you to build an instance of UserDetails.
At the end, it signs the JWT using a SHA256withRSA algorithm, using the provided public and private RSA keys. The JWT header specifies a HS256 value for the algorithm ("alg") claim.
RSA
RSA is an algorithm approved by the Federal Information Processing Standards (FIPS) (FIPS 186) for digital signatures and in Special Publication (SP) (SP800-56B) for key establishment.
Signing is done using the public and private RSA keys. Let's add the code for RSA key management in our sample e-commerce application.
Generating the public/private keys
You can use JDK's keytool to create a key store and generate public/private keys, as shown in the following code snippet:
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
Is CN=Modern API Development, OU=Org Unit, O=Packt, L=City,                               ST=State, C=IN correct?
  [no]:  yes
Generating 4,096 bit RSA key pair and self-signed certificate (SHA384withRSA) with a validity of 90 days
        for: CN=Modern API Development, OU=Org Unit, O=Packt,              L=City, ST=State, C=IN
The generated key store should be placed under the src/main/resources directory. These keys are valid only for 90 days from the time they got generated. Therefore, make sure that you create a new set of public/private keys when you use this chapter's code before running it.
Required values used in the keytool command should also be configured in the application.properties file, as shown here:
app.security.jwt.keystore-location=jwt-keystore.jks
app.security.jwt.keystore-password=password
app.security.jwt.key-alias=jwt-sign-key
app.security.jwt.private-key-passphrase=password
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/resources/application.properties
Now, we can configure the key store and public/private keys in the security configuration class.
Configuring the key store and keys
Let's add a SecurityConfig configuration class to configure the security relation configurations. This class extends the WebSecurityConfigurerAdaptor class. Here's the code to do this:
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
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/SecurityConfig.java
We have added all the properties defined in application.properties here.
Now, we can make use of the properties defined in application.properties for configuring the KeyStore, RSAPrivateKey, and RSAPublicKey beans in the security configuration class, as shown in the next few sections.
KeyStore bean
You can create a new bean for KeyStore by adding the following method and annotating it with @Bean:
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
This creates a KeyStore instance using the KeyStore class from the java.security package. It loads the key store from the src/main/resources package and uses the password configuration in the application.properties file.
RSAPrivateKey bean
You can create a new bean for RSAPrivateKey by adding the following method and annotating it with @Bean:
@Bean
public RSAPrivateKey jwtSigningKey(KeyStore keyStore) {
  try {
    Key key = keyStore.getKey(keyAlias,                 privateKeyPassphrase.toCharArray());
    if (key instanceof RSAPrivateKey) {
      return (RSAPrivateKey) key;
    }
  } catch (UnrecoverableKeyException |               NoSuchAlgorithmException |               KeyStoreException e) {
    LOG.error("Unable to load private key from keystore:               {}", keyStorePath, e);
  }
  throw new IllegalArgumentException("Unable to load         private key");
}
This method uses a key alias and a private key password to retrieve the private key, which is being used to return the RSAPrivateKey bean.
RSAPublicKey bean
You can create a new bean for RSAPublicKey by adding the following method and annotating it with @Bean:
@Bean
public RSAPublicKey jwtValidationKey(KeyStore keyStore) {
  try {
    Certificate certificate =
        keyStore.getCertificate(keyAlias);
    PublicKey publicKey = certificate.getPublicKey();
    if (publicKey instanceof RSAPublicKey) {
      return (RSAPublicKey) publicKey;
    }
  } catch (KeyStoreException e) {
    LOG.error("Unable to load private key from keystore:
              {}", keyStorePath, e);
  }
  throw new IllegalArgumentException("Unable to load public
        key");
}
Again, a key alias is used to retrieve the certificate from the key store. Then, the public key is retrieved from the certificate and returned.
As you know, JwtManager uses these public and private RSA keys to sign the JWT; therefore, a JWT decoder should use the same public key to decode the token. OAuth 2.0 Resource Server uses the org.springframework.security.oauth2.jwt.JwtDecoder interface to decode the token. Therefore, we need to create an instance of the JwtDecoder implementation and set the same public key in it to decode the token.
Spring OAuth 2.0 Resource Server provides a NimbusJwtDecoder implementation class of JwtDecoder. Let's now create a bean of it with the public key.
JwtDecoder Bean
You can create a new bean for JwtDecoder by adding the following method and annotating it with @Bean:
@Bean
public JwtDecoder jwtDecoder(RSAPublicKey rsaPublicKey) {
  return NimbusJwtDecoder.withPublicKey(
    rsaPublicKey).build();
}
Now, we can implement the newly added REST APIs.
Implementing new APIs
Let's implement the APIs exposed using UserApi. UserApi is part of code that has been autogenerated using OpenAPI Codegen. First, we need to add a new entity to the user_token table.
Coding user token functionality
You can create a UserTokenEntity based on the user_token table, as shown in the following code snippet:
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
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/entity/UserTokenEntity.java
Similarly, we can expose the following create, read, update, and delete (CRUD) repository for UserTokenEntity with the following two methods: deleteByUserId(), which will remove the UserToken table record based on a given user ID, and findByRefreshToken(), which will find the UserToken table record based on a given refresh token. The code is illustrated in the following code snippet:
public interface UserTokenRepository extends                    CrudRepository<UserTokenEntity, UUID> {
  Optional<UserTokenEntity> findByRefreshToken(    String refreshToken);
  Optional<UserTokenEntity> deleteByUserId(UUID userId);
}
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/repository/UserTokenRepository.java
Now, let add the new operations into UserService.
Enhancing the UserService class
We also need to add new methods into UserService for the UserApi interface. Let's add new methods into the service, as follows:
UserEntity findUserByUsername(String username);
Optional<SignedInUser> createUser(User user);
SignedInUser getSignedInUser(UserEntity userEntity);
Optional<SignedInUser> getAccessToken(RefreshToken
        refreshToken);
void removeRefreshToken(RefreshToken refreshToken);
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/service/UserService.java
Here, each method performs a specific operation, as outlined here:
findUserByUsername(): Finds and returns a user based on a given username.
createUser(): Adds a new signed-up user to the database.
getSignedInUser(): Creates a new model instance of SignedInUser that holds the refresh token, access token (JWT), user ID, and username.
getAcceessToken(): Generates and returns a new access token (JWT) for a given valid refresh token.
removeRefreshToken(): Removes the refresh token from the database. It is called when the user wants to sign out.
Let's implement each of these methods in the UserServiceImpl class.
findUserByUsername() implementation
First, you can add the implementation for findUserByUsername(), as follows:
@Override
public UserEntity findUserByUsername(String username) {
  if (Strings.isBlank(username)) {
    throw new UsernameNotFoundException("Invalid user.");
  }
  final String uname = username.trim();
  Optional<UserEntity> oUserEntity =               repository.findByUsername(uname);
  UserEntity userEntity = oUserEntity.orElseThrow(() ->
              new UsernameNotFoundException(
                String.format("Given user(%s) not found.",                               uname)));
  return userEntity;
}
This is a straightforward operation. You query the database based on a given username. If found, then it returns the user, else it throws a UsernameNotFoundException exception.
createUser() implementation
Next, you can add the implementation for the createUser() method, as shown in the following code snippet:
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
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/service/UserServiceImpl.java
Here, we first check whether there is any existing user that was assigned the same username or email in the sign-up request. If there is, then it simply raises an exception, else it creates a new user in the database and returns a SignedInUser instance with refresh and access tokens using the createSignedUserWithRefreshToken() method.
First, we can add a private createSignedUserWithRefreshToken() method in UserServiceImpl, as follows:
private SignedInUser createSignedUserWithRefreshToken(
                                  UserEntity userEntity) {
  return createSignedInUser(userEntity)
         .refreshToken(createRefreshToken(userEntity));
}
This also uses another private method, createSignedInUser(), which returns SignedInUser; then, it adds the refresh token by calling the createRefreshToken() method.
Let's define these two createSignedIn() and createSignedInUser() private methods, as follows:
private SignedInUser createSignedInUser(
    UserEntity userEntity) {
  String token = tokenManager.create(
       org.springframework.security.core.userdetails.User.
          builder()
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
Here, tokenManager is used in the createSignedIn() method for creating the JWT. tokenManager is an instance of JwtManager. The User.builder() method is used to create a UserBuilder class. UserBuilder, which is a final builder class, is used to create an instance of UserDetails. The JwtManager.create() method uses this UserDetails instance to create a token.
The createRefreshToken() method uses RandomHolder private static class to generate a refresh token. This token is not a JWT; however, we can use a longer duration valid token, such as one valid for a day for a refresh token. Saving a JWT as a refresh token in the database removes the sole purpose of using the JWT. Therefore, we should think carefully about using the JWT as a refresh token and then saving it in the database.
Let's add the RandomHolder private static class, as follows:
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
This class uses a SecureRandom instance to generate a random BigInteger instance. Then, this random BigInteger value is converted to a string with radix size 32. At the end, the space is replaced with 0 if found in a converted string.
You can also use the org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric() method to generate a refresh token, or use any other secured random key generator.
We also need to modify the UserRepository class to add a new method that returns the count of users having a given username or email.
getSignedInUser() implementation
Implementation of the getSignedInUser() method is straightforward, as shown in following code snippet:
@Override
@Transactional
public SignedInUser getSignedInUser(UserEntity userEntity) {
  userTokenRepository.deleteByUserId(userEntity.getId());
  return createSignedUserWithRefreshToken(userEntity);
}
It first removes the existing token from the database associated with the given user, and then returns the new instance of SignedInUser created using createSignedUserWithRefreshToken(), defined previously in the createUser() implementation subsection.
getAccessToken() implementation
Implementation of the getAccessToken() method is again straightforward, as shown in the following code snippet:
@Override
public Optional<SignedInUser> getAccessToken(                               RefreshToken refreshToken) {
  return userTokenRepository
      .findByRefreshToken(refreshToken.getRefreshToken())
      .map(ut -> Optional.of(createSignedInUser(             ut.getUser())
          .refreshToken(refreshToken.getRefreshToken())))
      .orElseThrow(() -> new InvalidRefreshTokenException(                                          "Invalid token."));
}
First, it finds the user's token entity using the UserTokenRepository instance. Then, it populate the SignedInUser POJO using the retrieved UserToken entity. The createSignedInUser() method does not populate the refresh token, therefore we assign the same refresh token back. If it does find the user token entry in the database based on the refresh token, it throws an exception.
You can also add a time validation logic for the refresh token—for example, store the refresh token creation time in the database and use the configured valid time for refresh token validation: a kind of expiration logic of JWT.
removeRefreshToken() implementation
Implementation of the removeRefreshToken() method is shown in the following code snippet:
@Override
public void removeRefreshToken(RefreshToken refreshToken) {
  userTokenRepository
    .findByRefreshToken(refreshToken.getRefreshToken())
      .ifPresentOrElse(userTokenRepository::delete, () -> {
        throw new InvalidRefreshTokenException(
                                         "Invalid token.");
      });
}
First, it finds the given refresh token in the database. If this is not found, then it throws an exception. If the given refresh token is found in the database, then it deletes it.
Enhancing the UserRepository class
Let's add findByUsername() and findByUsernameOrEmail() methods to UserRepository, as follows:
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
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/repository/UserRepository.java
This returns a count of records matching the given username or email.
Now, we can implement the UserApi interface to write REST controllers.
Implementing the REST controllers
You have already developed and enhanced services and repositories required to implement the APIs defined in the UserApi interface generated by OpenAPI Codegen in the previous section. The only pending dependency is PasswordEncoder. PasswordEncoder is required for encoding the password before storing and matching the password given as part of the sign-in request.
Adding a bean for PasswordEncoder
You should expose the PasswordEncoder bean because Spring Security needs to know which encoding you want to use for password encoding, as well as for decoding the passwords. Let's add a PasswordEncoder bean in AppConfig, as follows:
@Bean
public PasswordEncoder passwordEncoder() {
  Map<String, PasswordEncoder> encoders = Map.of(
      "bcrypt", new BCryptPasswordEncoder(),
      "pbkdf2", new Pbkdf2PasswordEncoder(),
      "scrypt", new SCryptPasswordEncoder());
  return new DelegatingPasswordEncoder("bcrypt", encoders);
}
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/AppConfig.java
You can directly create a new instance of BCryptPasswordEncoder and return it for bcrypt encoding. However, use of DelegatingPasswordEncoder not only allows you to support existing passwords but also facilitates migration to a new, better encoder if one is available in the future. This code uses Bcrypt as a default password encoder, which is the best among the current available encoders.
For DelegatingPasswordEncoder to work, you need to add a hashing algorithm prefix such as {bcrypt} to encoded passwords—for example, add {bcrypt}$2a$10$neR0EcYY5./tLVp4litNyuBy/kfrTsqEv8hiyqEKX0TXIQQwC/5Rm in the persistent store if you already have a hashed password in the database or if you're adding any seed/test users in the database script. The new password would store the password with a prefix anyway, as configured in the DelegatingPasswordEncoder constructor. You have passed bcrypt into the constructor, therefore all new passwords will be stored with a {bcrypt} prefix.
PasswordEncoder reads the password from the persistence store and removes the prefix before matching. It uses the same prefix to find out which encoder it needs to use for matching. Now, you can start implementing the new APIs based on UserApi.
Implementing the Controller class
First, create a new AuthController class, as shown in the following code snippet:
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
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/controller/AuthController.java
The AuthController class is annotated with @RestController to mark it as a REST controller. Then, it uses two beans, UserService and PasswordEncoder, which will be injected at the time of the AuthController construction.
Let's first add the sign-in operation, as follows:
@Override
public ResponseEntity<SignedInUser> signIn(                              @Valid SignInReq signInReq) {
  UserEntity userEntity = service               .findUserByUsername(signInReq.getUsername());
  if (passwordEncoder.matches(
          signInReq.getPassword(),
              userEntity.getPassword())) {
    return ok(service.getSignedInUser(userEntity));
  }
  throw new InsufficientAuthenticationException(        "Unauthorized.");
}
It first finds the user and matches the password using the PasswordEncoder instance. If everything goes through successfully, it returns the SignedInUser instance with refresh and access tokens; else, it throws an exception.
Let's add other operations to AuthController, as follows:
@Override
public ResponseEntity<Void> signOut(
                        @Valid RefreshToken refreshToken) {
  service.removeRefreshToken(refreshToken);
  return accepted().build();
}
@Override
public ResponseEntity<SignedInUser> signUp(
                              @Valid User user) {
  return status(HttpStatus.CREATED)
        .body(service.createUser(user).get());
}
@Override
public ResponseEntity<SignedInUser> getAccessToken(
                              @Valid RefreshToken refreshToken) {
  return ok(service.getAccessToken(refreshToken)
        .orElseThrow(InvalidRefreshTokenException::new));
}
All operations such as signOut(), signUp(), and getAccessToken() are straightforward, as outlined here:
signOut() uses the user service to remove the given refresh token.
signUp() creates a valid new user and returns the SignedInUser instance as a response.
getAccessToken() returns the SignedInUser with a new access token if the given refresh token is valid.
We are done with coding the controllers. Let's configure security in the next subsection.
Configuring web-based security
You have already learned about how to configure Spring Security in one of the previous subsections, Configuring Spring Security. In that subsection, we defined a new class, SecurityConfig, which extends WebSecurityConfigurerAdaptor and is also annotated with @EnableWebSecurity.
Let's modify its overridden configure() method, which will allow us to configure HttpSecurity using DSL (fluent methods). Let's make the following configurations:
Disable HTTP Basic authentication (BA) using httpBasic().disable().
Disable the form login using formLogin().disable().
Restrict access based on URL patterns using authorizeRequests().
Configure URL patterns and respective HTTP methods using antMatchers(), which allows you to use ant (build tool) pattern matching styles. You can also use mvcMatchers(), which uses the same pattern matching style as Spring Model-View-Controller (MVC).
All URLs except those configured explicitly by authorizeRequests() should be allowed by any authenticated user (by using anyRequest().authenticated()).
Enable JWT bearer token support for OAuth 2.0 Resource Server (oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt).
Enable the STATELESS session creation policy (that is, it won't create any HTTPSession).
Let's add these to HttpSecurity, as follows:
@Override
protected void configure(HttpSecurity http) throws
    Exception {
  http.httpBasic().disable().formLogin().disable()
      .and()
      .headers().frameOptions().sameOrigin() // for H2
                                             // Console
      .and()
      .authorizeRequests()
      .antMatchers(HttpMethod.POST, TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.DELETE,
          TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.POST, SIGNUP_URL).permitAll()
      .antMatchers(HttpMethod.POST,
          REFRESH_URL).permitAll()
      .antMatchers(H2_URL_PREFIX).permitAll()      
      .anyRequest().authenticated()
      .and()
      .oauth2ResourceServer(OAuth2ResourceServerConfigurer:
         :jwt)
      .sessionManagement().sessionCreationPolicy(
             SessionCreationPolicy.STATELESS);
}
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/SecurityConfig.java
Here, we have added one more important configure for the H2 console app. The H2 console user interface (UI) is based on HTML frames. The H2 console UI won't display in browsers because by default, the security header (X-Frame-Options) is not sent with the permission to allow frames with the same origin. Therefore, you need to configure headers().frameOptions().sameOrigin().
Now, you can configure CORS and CSRF.
Configuring CORS and CSRF
Browsers restrict cross-origin requests from scripts for security reasons. For example, a call from http://mydomain.com to http://mydomain-2.com can't be made using a script. Also, an origin not only indicates a domain—in fact, it includes a scheme and a port too.
Before hitting to any endpoint, the browser sends a preflight request using the HTTP method option to check whether the server would permit the actual request. This request contains the following headers:
Actual request's headers (Access-Control-Request-Headers)
A header containing the actual request's HTTP method (Access-Control-Request-Method)
A Origin header that contains the requesting origin (scheme, domain, and port)
If the response from the server is successful, then only the browser allows the actual request to fire. The server responds with other headers, such as Access-Control-Allow-Origin, which contains the allowed origins (an asterisk * value means any origin), Access-Control-Allow-Methods (allowed methods), Access-Control-Allow-Headers (allowed headers), and Access-Control-Max-Age (allowed time in seconds).
You can configure CORS to take care of cross-origin requests. For that, you need to make the following two changes:
Add a CorsConfigurationSource bean that takes care of the CORS configuration using a CorsConfiguration instance.
Add the cors() method into HTTPSecurity in the configure() method. It uses CorsFilter if a corsFilter bean is added, else it uses CorsConfigurationSource. If neither is configured, then it uses the Spring MVC pattern inspector handler.
Let's now add the CorsConfigurationSource bean to the SecurityConfig class.
The default permitted values (new CorsConfiguraton().applyPermitDefaultValues()) configure CORS for any origin (*), all headers, and simple methods (GET, HEAD, POST) with allowed max age is 30 minutes.
You need to allow mostly all of the HTTP methods, including the DELETE method, and need more custom configuration; therefore, we will use the following bean definition:
@Bean
CorsConfigurationSource corsConfigurationSource() {
  CorsConfiguration configuration = new
      CorsConfiguration();
  configuration.setAllowedOrigins(Arrays.asList("*"));
  configuration.setAllowedMethods(Arrays.asList("HEAD",
      "GET", "PUT", "POST", "DELETE", "PATCH"));
   // For CORS response headers
  configuration.addAllowedOrigin("*");
  configuration.addAllowedHeader("*");
  configuration.addAllowedMethod("*");
  UrlBasedCorsConfigurationSource source = new
      UrlBasedCorsConfigurationSource();
  source.registerCorsConfiguration("/**", configuration);
  return source;
}
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/SecurityConfig.java
Here, you are creating a CorsConfiguration instance using the default constructor, and then setting the allowed origins, allowed methods, and response headers. Finally, you are passing it as an argument while registering it to the UrlBasedCorsConfigurationSource instance and returning it.
Let's add cors() to HttpSecurity, as follows:
@Override
protected void configure(HttpSecurity http) throws Exception {
  http.httpBasic().disable().formLogin().disable()
      .csrf().ignoringAntMatchers(API_URL_PREFIX,
          H2_URL_PREFIX)
      .and()
      .headers().frameOptions().sameOrigin() // for H2 Console
      .and()
      .cors()
      .and()
      .authorizeRequests()
      .antMatchers(HttpMethod.POST, TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.DELETE,
          TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.POST, SIGNUP_URL).permitAll()
      .antMatchers(HttpMethod.POST,
          REFRESH_URL).permitAll()
      …
      …
}
Here, we have also configured CSRF using csrf() DSL. We have applied CSRF protection to all URLs except URLs starting with /api/v1 and the /h2-console H2 database console URL. You can change the configuration based on your requirement.
Let's first understand what CSRF/XSRF is. CSRF or XSRF stands for Cross-Origin Request Forgery, which is a web security vulnerability. Let's assume you are a bank customer and are currently signed in to the bank. You may get an email and you click on a link in the email, or click on any malicious website's link that may contain a malicious script. This script then sends a request to your bank for a fund transfer. The bank then transfers the funds to a perpetrator's account because the bank thinks that the request has been sent by you as you are signed in. This is just an example.
To prevent such attacks, the application sends new unique CSRF tokens associated with the signed-in user for each new request. These tokens are stored in hidden form fields. When a user submits a form, the same token should be sent back with the request. The application then verifies the CSRF token and only processes the request if the verification is successful. This works because malicious scripts can't read the token due to the same origin policy,
However, if a perpetuator also tricks you into revealing the CSRF token, then it is very difficult to prevent such attacks. You can disable CSRF protection for this web service by using csrf().disable() because it only provides REST endpoints.
Now, let's move on to the final section, where you will configure the authorization based on the user's role.
Understanding authorization
Your valid username/password or access token for authentication allows you access to secure resources such as URLs, web resources, or secure web pages. Authorization is one step ahead; it allows you to configure access security further with scopes such as read, write, or roles such as Admin, User, Manager, and so on. Spring Security allows you to configure any custom authority.
We will configure three types of roles for our sample e-commerce app—namely, Customer (user), Admin, and Customer Support Representative (CSR). Obviously, each user would have their own specific authority. For example, a user can place an order and buy stuff online, but should not be able to access the CSR or Admin resources. Similarly, a CSR should not be able to have access to Admin-only resources. A security configuration that allows authority or role-based access to resources is known authorization. A failed authentication should return HTTP (status 401 unauthorized), and a failed authorization should return HTTP status 403 (forbidden), which means the user is authenticated but does not have the required authority/role to access the resource.
Let's introduce these three roles in a sample e-commerce app, as shown in the following code snippet:
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
Here, we have declared an enum that implements Spring Security's GrantedAuthority interface to override the getAuthority() method. GrantedAuthority is an authority granted to an Authentication (interface) object. As you know, BearerTokenAuthenticationToken is a type of AbstractAuthenticationToken class that implements the Authentication interface, which represents the token/principal for an authenticated request. We have used the String constants for user's roles in this enum as we need these when we configure the role-based restriction at a method level.
Let's discuss role and authority in detail.
Role and authority
You can take authorities for finer-grained control, whereas roles should be applied to large sets of permissions. A role is an authority that has the ROLE_ prefix. This prefix is configurable in Spring Security.
Spring Security provides hasRole() and hasAuthority() methods for applying role- and authority-based restrictions. hasRole() and hasAuthority() are almost identical, but the hasRole() method maps with Authority without the ROLE_ prefix. If you use hasRole('ADMIN'), your Admin enum must be ROLE_ADMIN instead of ADMIN because a role is an authority and should have a ROLE_ prefix, whereas if you use hasAuthority('ADMIN'), your ADMIN enum must be only ADMIN.
OAuth 2.0 Resource Server by default populates the authorities based on the scope (scp) claim. If you provide access to user's resources such as order history and so on for integration to other application, then you can limit an application's access to a user's account before granting access to other applications for third-party integration. Third- party applications can request one or more scopes; this information is then presented to the user in the consent screen, and the access token issued to the application will be limited to the scopes granted. However, in this chapter, we are not providing OAuth 2.0 authorization flows and are limiting security access to REST endpoints.
If the JWT contains a claim with the name "scope" (scp), then Spring Security will use the value in that claim to construct the authorities by prefixing each value with "SCOPE_". For example, if a payload contains a scp=["READ","WRITE"] claim, this means that a list of Authority will consist of SCOPE_READ and SCOPE_WRITE.
We need to change the default authority mapping behavior because a scope (scp) claim is the default authority for OAuth2.0 Resource Server in Spring. We can do that by adding a custom authentication converter to JwtConfigurer in OAuth2ResourceServer in your security configuration. Let's add a method that returns the converter, as follows:
private Converter<Jwt, AbstractAuthenticationToken>                                getJwtAuthenticationConverter() {
  JwtGrantedAuthoritiesConverter authorityConverter =                    new JwtGrantedAuthoritiesConverter();
  authorityConverter.setAuthorityPrefix(AUTHORITY_PREFIX);
  authorityConverter.setAuthoritiesClaimName(ROLE_CLAIM);
  JwtAuthenticationConverter converter =                         new JwtAuthenticationConverter();
  converter.setJwtGrantedAuthoritiesConverter(      authorityConverter);
  return converter;
}
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/src/main/java/com/packt/modern/api/security/SecurityConfig.java
Here, first create a new instance of JwtGrantedAuthorityConverter and then assign an authority prefix (ROLE_) and authority claim name (key of the claim in JWT) as roles.
Now, we can use this private method for configuring the OAuth 2.0 resource server. You can now modify the existing configuration with the following code. We can also add configuration for adding role-based restriction on the POST /api/v1/addresses API call in the following code snippet:
@Override
protected void configure(HttpSecurity http) throws
    Exception {
  http.httpBasic().disable().formLogin().disable()
      .csrf().ignoringAntMatchers(API_URL_PREFIX,
        H2_URL_PREFIX)
      .and()
      .headers().frameOptions().sameOrigin() // for H2
                                             // Console
      .and()
      .cors()
      .and()
      .authorizeRequests()
      .antMatchers(HttpMethod.POST, TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.DELETE,
        TOKEN_URL).permitAll()
      .antMatchers(HttpMethod.POST, SIGNUP_URL).permitAll()
      .antMatchers(HttpMethod.POST,
         REFRESH_URL).permitAll()
      .antMatchers(H2_URL_PREFIX).permitAll()
      .mvcMatchers(HttpMethod.POST, "/api/v1/addresses/**")
      .hasAuthority(RoleEnum.ADMIN.getAuthority())
      .anyRequest().authenticated()
      .and()
      .oauth2ResourceServer(oauth2ResourceServer ->
         oauth2ResourceServer.jwt(jwt ->
            jwt.jwtAuthenticationConverter(
               getJwtAuthenticationConverter())))
      .sessionManagement().sessionCreationPolicy(
         SessionCreationPolicy.STATELESS);
}
After setting this configuration to add an address (POST /api/v1/addresses), it now requires both authentication and authorization. This means the logged-in user must have the ADMIN role to call this endpoint successfully. Also, we have changed the default claim from scope to role.
Now, we can proceed further with method-level role-based restrictions. Spring Security provides a feature that allows you to place authority-/role-based restrictions on public methods of Spring beans using a set of annotations such as @PreAuthorize, @Secured, and @RolesAllowed. By default, these are disabled, therefore you need to enable them explicitly.
Let's enable these by adding @EnableGlobalMethodSecurity(prePostEnabled = true) annotation to the Spring Security configuration class, as follows:
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
Now, you can use @PreAuthorize (the given access-control expression would be evaluated before the method invocation) and @PostAuthorize (the given access-control expression would be evaluated after the method invocation) annotations to place restrictions on public methods of Spring beans because you have set the prePostEnabled property to true when enabling the global method-level security.
@EnableGlobalMethodSecurity also supports the following properties:
securedEnabled: This allows you to use @Secured annotation on public methods.
jsr250Enabled: This allows you to use JSR-250 annotations such as @RolesAllowed. @RolesAllowed can be applied to both public classes and methods. As the name suggests, you can use a list of roles for access restrictions.
@PreAuthorize/@PostAuthorize are more powerful than the other security annotations because these can not only be configured for authorities/roles, but you can also use any valid Spring Expression Language (SpEL) expression.
For demonstration purposes, let's add @PreAuthorize annotation to the deleteAddressesById() method, which is associated with DELETE /v1/auth/addresses/{id} in AddressController, as shown in the following code snippet:
@PreAuthorize("hasRole('" + Const.ADMIN + "')")
@Override
public ResponseEntity<Void> deleteAddressesById(String id) {
  service.deleteAddressesById(id);
  return accepted().build();
}
Here, hasRole() is a built-in SpEL expression. We need to pass a valid SpEL expression, and it should be a String. Any variable used to form this SpEL expression should be final. Therefore, we have declared final string constants in the RoleEnum enum (for example, Const.ADMIN).
Now, the DELETE /api/v1/addresses/{id} REST API can only be invoked if the user has the ADMIN role.
Spring Security provides various built-in SpEL expressions, such as hasRole(). Here are some others:
a. hasAnyRole(String… roles): Returns true if principal's role matches any of the given roles.
b. hasAuthority(String authority): Returns true if principal has given authority. Similarly, you can also use hasAnyAuthority(String… authorities).
c. permitAll: Returns true.
d. denyAll: Returns false.
isAnonymous(): Returns true if current user is anonymous.
isAuthenticated(): Returns true if current user is not anonymous.
A full list of these expressions is available at https://docs.spring.io/spring-security/site/docs/current/reference/html5/#el-access.
Similarly, you can apply access restrictions for other APIs. Let's test security in the next section.
Testing security
You can clone the code and build it using the following command:
Run it from project home
$ gradlew clean build
This code is tested with Java 15.
IMPORTANT
Make sure to generate the keys again, as keys generated by the JDK keytool are only valid for 90 days.
Then, you can run the application from your project home, as shown in the following code snippet:
$ java -jar build/libs/Chapter06-0.0.1-SNAPSHOT.jar
Now, you must be looking forward to testing. Let's test our first use case.
Let's hit the GET /api/vi/addresses API without the Authorization header, as shown in the following code snippet:
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
This returns HTTP Status 401 (unauthorized) and a WWW-Authenticate: Bearer response header, which suggests the request should be sent with an Authorization header.
Let's send the request again with an invalid token, as follows:
$  curl -v 'http://localhost:8080/api/v1/addresses' -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9… rest of the JWT string removed for brevity'
< HTTP/1.1 401
< Vary: Origin
< Vary: Access-Control-Request-Method
< Vary: Access-Control-Request-Headers
< WWW-Authenticate: Bearer error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Jwt expired at 2021-01-09T14:19:49Z", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"
< Other information is removed for brevity
The server again responds with 401 (unauthorized), but this time with an error message and description that tells us that a given token has expired. It can also send an invalid_token error with HTTP status 401 based on a given bearer token, as outlined here:
If the token is not a well-formatted JWT, then it shows An error occurred while attempting to decode the Jwt: Invalid JWT serialization: Missing dot delimiter(s).
If the token doesn't have a valid signature, then it shows An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature.
We have created two users using a Flyway database migration script: scott/tiger and scott2/tiger. Now, let's perform a sign-in with the username scott to get the valid JWT, as follows:
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
It returns with both a refresh and an access token. Let's use this access token to call the GET /api/v1/addresses API again (please note that the Bearer token value in the Authorization header is taken from the response of the previous GET /api/v1/auth/token API call). The code is shown in the following snippet:
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
This time, the call is successful. Now, let's use the refresh token to get a new access token, as follows:
$ curl -X POST 'http://localhost:8080/api/v1/auth/token/refresh'
-H 'Content-Type: application/json' -H 'Accept: application/json'
-d '{
    "refreshToken": "3i2tlrmdqnp60drl6i9c2kdm36s48qg5vm2ucgtflsk0cjo4dth hjan9aj1ck83det8m8hkl461cqkfl57puk81ct6j09ilporanf1j j414ht4ob7dkcakq6lk92cnct"
}'
< Response
{
    "refreshToken": "3i2tlrmdqnp60drl6i9c2kdm36s48qg5vm2ucgtflsk0cjo4dthhjan9 aj1ck83det8m8hkl461cqkfl57puk81ct6j09ilporanf1jj4 14ht4ob7dkcakq6lk92cnct",
    "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.
                    Rest of token truncated for brevity",
    "username": "scott",
    "userId": "a1b9b31d-e73c-4112-af7c-b68530f38222"
}
This time, it returns a new access token with the same refresh token given in the payload.
If you pass an invalid refresh token while calling the refresh token API, it would provide the following response:
{"errorCode":"PACKT-0010",
"message":"Requested resource not found. Invalid token.", "status":404,"url":"http://localhost:8080/api/v1/auth/token/refresh","reqMethod":"POST","timestamp":"2021-01-18T07:20:35.846649200Z"}
After testing the authentication using JWT, we can now test the authorization. Let's create an address using a token created by user SCOTT. SCOTT has a USER role. Here is the code to do this:
$ curl -v -X POST 'http://localhost:8080/api/v1/addresses' -H 'Content-Type: application/json' -H 'Accept: application/json' -H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.ey
Rest of the token is truncated for brevity'
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
The API responded with 403 (forbidden) because SCOTT has a USER role and we have configured this API to only be allowed to be accessed by a user with an ADMIN role.
Let's create a token again, using the SCOTT2 user who has an ADMIN role, with the following code:
$ curl -X POST 'http://localhost:8080/api/v1/auth/token' -H 'Content-Type: application/json' -H 'Accept: application/json' -d '{
    "username": "scott2",
    "password": "tiger"
}'
Now, let's call the create address API again using the access token received from the SCOTT2 sign-in, as shown in the following code snippet:
$ curl -X POST 'http://localhost:8080/api/v1/addresses'
-H 'Content-Type: application/json' -H 'Accept: application/json'
-H 'Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.                          eyJzd
Rest of the token is truncated for brevity' -d '{
    "number": "9I-999",
    "residency": "Fraser Suites Le Claridge",
    "street": "Champs-Elysees",
    "city": "Paris",
    "state": "Ile-de-France",
    "country": "France",
    "pincode": "75008"
}'
< Response
{"_links":{"self":[{"href":"http://localhost:8080/b78d485e-16a0-4b11-98d2-6e4dadbc60e7"},{"href":"http://localhost:8080/api/v1/addresses/b78d485e-16a0-4b11-98d2-6e4dadbc60e7"}]},"id":"b78d48
5e-16a0-4b11-98d2-6e4dadbc60e7","number":"9I-999","residency":"Fraser Suites Le Claridge","street":"Champs-Elysees","city":"Paris","state":"Ile-de-France","country":"France","pincode":"75008
"}
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter06/Chapter06.postman_collection.json
Similarly, you can try to delete address operations using the REST API. This would only allow an ADMIN role to perform the operation.
You can find the Postman (API client) collection of this chapter APIs at the following location, which is based on Postman Collection version 2.1. You can import it and then test the APIs.

## Summary

In this chapter, you have learned about JWTs, Spring Security, authentication using filters, and JWT token validation using filter and authentication with Spring OAuth 2.0 Resource Server. You have also learned how you can add CORS and CSRF protection and why these are necessary.

You have also learned about access protection based on roles and authorities. You have now got the skills to implement JWTs, Spring Security, and Spring Security OAuth 2.0 Resource Server to protect your web resources.

In the next chapter, you will develop a sample e-commerce app's UI using the Spring Security framework and APIs used in this chapter. This integration will allow you to understand the UI flows and how to consume REST APIs using JavaScript.

## Questions

- What is a security context and principal?

- Which is the preferred way to secure a JWT—signing or encrypting a token?

- What are the best practices for using a JWT?
  
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
