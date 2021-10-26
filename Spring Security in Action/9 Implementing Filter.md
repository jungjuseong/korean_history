# 9 필터 구현

이 장에서는 다음을 다룹니다.
- 필터 체인 작업
- 사용자 정의 필터 정의
- Filter 인터페이스를 구현하는 Spring Security 클래스 사용

Spring Security에서 HTTP 필터는 HTTP 요청에 적용되는 다양한 책임을 위임합니다. HTTP 기본 인증 및 권한 부여 아키텍처에 대해 논의한 3~5장에서 저는 종종 필터를 언급했습니다. 인증 관리자에게 인증 책임을 위임하는 인증 필터라는 구성 요소에 대해 배웠습니다. 인증에 성공하면 특정 필터가 인증 구성을 처리한다는 것도 배웠습니다. Spring Security에서 일반적으로 HTTP 필터는 요청에 적용되어야 하는 각 책임을 관리합니다. 필터는 책임 체인을 형성합니다. 필터는 요청을 수신하고 해당 논리를 실행하며 결국 체인의 다음 필터에 요청을 위임합니다(그림 9.1).
 
그림 9.1 필터 체인이 요청을 수신합니다. 각 필터는 관리자를 사용하여 요청에 특정 논리를 적용하고 결국에는 체인을 따라 다음 필터에 요청을 위임합니다.

아이디어는 간단합니다. 공항에 갈 때 터미널 입장부터 항공기 탑승까지 여러 가지 필터를 거칩니다(그림 9.2). 먼저 티켓을 제시하고 여권을 확인한 후 보안 검색대를 통과합니다. 공항 게이트에서 더 많은 "필터"가 적용될 수 있습니다. 

예를 들어 탑승 직전에 여권과 비자를 한 번 더 확인하는 경우가 있습니다. 이것은 Spring Security의 필터 체인과 매우 유사합니다. 같은 방식으로 HTTP 요청에 대해 작동하는 Spring Security를 ​​사용하여 필터 체인에서 필터를 사용자 정의합니다. Spring Security는 사용자 정의를 통해 필터 체인에 추가하는 필터 구현을 제공하지만 사용자 정의 필터를 정의할 수도 있습니다.
 
그림 9.2 공항에서는 필터 체인을 거쳐 결국 항공기에 탑승합니다. 같은 방식으로 Spring Security에는 애플리케이션이 수신한 HTTP 요청에 대해 작동하는 필터 체인이 있습니다.

이 장에서는 Spring Security에서 인증 및 권한 부여 아키텍처의 일부인 필터를 사용자 정의하는 방법에 대해 설명합니다. 예를 들어 이메일 주소를 확인하거나 1회용 비밀번호를 사용하는 것과 같이 사용자를 위한 단계를 하나 더 추가하여 인증을 강화할 수 있습니다. 또한 인증 이벤트 감사를 참조하는 기능을 추가할 수도 있습니다. 디버깅 목적에서 사용자 행동 식별에 이르기까지 애플리케이션이 감사 인증을 사용하는 다양한 시나리오를 찾을 수 있습니다. 오늘날의 기술과 기계 학습 알고리즘을 사용하면 예를 들어 사용자의 행동을 학습하고 누군가가 계정을 해킹했거나 사용자를 사칭했는지 여부를 파악하여 애플리케이션을 개선할 수 있습니다.

HTTP 필터 책임 체인을 사용자 정의하는 방법을 아는 것은 귀중한 기술입니다. 실제로 응용 프로그램에는 기본 구성을 사용하는 것이 더 이상 작동하지 않는 다양한 요구 사항이 있습니다. 체인의 기존 구성 요소를 추가하거나 교체해야 합니다. 기본 구현에서는 사용자 이름과 암호에 의존할 수 있는 HTTP 기본 인증 방법을 사용합니다. 그러나 실제 시나리오에서는 이보다 더 필요한 상황이 많이 있습니다. 인증을 위해 다른 전략을 구현하거나, 승인 이벤트에 대해 외부 시스템에 알리거나, 나중에 추적 및 감사에 사용되는 인증 성공 또는 실패를 간단히 기록해야 할 수도 있습니다(그림 9.3). 시나리오가 무엇이든 Spring Security는 필요에 따라 정확하게 필터 체인을 모델링할 수 있는 유연성을 제공합니다.
 
그림 9.3 기존 필터의 이전, 이후 또는 위치에 새 필터를 추가하여 필터 체인을 사용자 정의할 수 있습니다. 이러한 방식으로 인증은 물론 요청 및 응답에 적용되는 전체 프로세스를 사용자 정의할 수 있습니다.

## 9.1 스프링 시큐리티 아키텍처에서 필터 구현하기

이 섹션에서는 Spring Security 아키텍처에서 필터와 필터 체인이 작동하는 방식에 대해 논의합니다. 이 장의 다음 섹션에서 작업할 구현 예를 이해하려면 먼저 이 일반 개요가 필요합니다. 이전 장에서 인증 필터가 요청을 가로채고 인증 책임을 권한 부여 관리자에게 더 위임한다는 것을 배웠습니다. 인증 전에 특정 논리를 실행하려면 인증 필터 앞에 필터를 삽입하여 수행합니다.

Spring Security 아키텍처의 필터는 일반적인 HTTP 필터입니다. javax.servlet 패키지에서 Filter 인터페이스를 구현하여 필터를 생성할 수 있습니다. 다른 모든 HTTP 필터와 마찬가지로 해당 논리를 구현하려면 doFilter() 메서드를 재정의해야 합니다. 이 메소드는 ServletRequest, ServletResponse 및 FilterChain을 매개변수로 수신합니다.
- ServletRequest--HTTP 요청을 나타냅니다. ServletRequest 객체를 사용하여 요청에 대한 세부 정보를 검색합니다.

- ServletResponse--HTTP 응답을 나타냅니다. ServletResponse 객체를 사용하여 응답을 클라이언트로 다시 보내기 전에 또는 더 나아가 필터 체인을 따라 응답을 변경합니다.

- FilterChain--필터 체인을 나타냅니다. FilterChain 객체를 사용하여 체인의 다음 필터로 요청을 전달합니다.
필터 체인은 필터가 작동하는 순서가 정의된 필터 모음을 나타냅니다. Spring Security는 우리를 위해 몇 가지 필터 구현과 그 순서를 제공합니다. 제공되는 필터 중

- BasicAuthenticationFilter는 존재하는 경우 HTTP 기본 인증을 처리합니다.

- CsrfFilter는 10장에서 논의할 CSRF(교차 사이트 요청 위조) 보호를 처리합니다.
- CorsFilter는 CORS(Cross-Origin Resource Sharing) 권한 부여 규칙을 처리합니다. 이에 대해서도 10장에서 논의합니다.
코드에서 직접 만지지 않을 것이기 때문에 모든 필터를 알 필요는 없지만 필터 체인이 작동하는 방식을 이해하고 몇 가지 구현을 알고 있어야 합니다. 이 책에서는 우리가 논의하는 다양한 주제에 필수적인 필터만 설명합니다.

애플리케이션이 체인에 이러한 모든 필터의 인스턴스를 반드시 가질 필요는 없다는 점을 이해하는 것이 중요합니다. 응용 프로그램을 구성하는 방법에 따라 체인이 더 길거나 짧습니다. 예를 들어, 2장과 3장에서 HTTP 기본 인증 방법을 사용하려면 HttpSecurity 클래스의 httpBasic() 메서드를 호출해야 한다는 것을 배웠습니다. httpBasic() 메서드를 호출하면 BasicAuthenticationFilter의 인스턴스가 체인에 추가됩니다. 마찬가지로 작성하는 구성에 따라 필터 체인의 정의가 영향을 받습니다.
 
그림 9.4 각 필터에는 주문 번호가 있습니다. 필터가 요청에 적용되는 순서를 결정합니다. Spring Security에서 제공하는 필터와 함께 사용자 정의 필터를 추가할 수 있습니다.

다른 필터를 기준으로 체인에 새 필터를 추가합니다(그림 9.4). 또는 알려진 필터의 이전, 이후 또는 위치에 필터를 추가할 수 있습니다. 실제로 각 위치는 인덱스(숫자)이며 "주문"이라고도 합니다.

같은 위치에 두 개 이상의 필터를 추가할 수 있습니다(그림 9.5). 섹션 9.4에서 이런 일이 발생할 수 있는 일반적인 경우를 접하게 되며 일반적으로 개발자들 사이에서 혼란을 야기합니다.

> **참고** 여러 필터의 위치가 같으면 필터가 호출되는 순서가 정의되지 않습니다.
 
그림 9.5 체인에 동일한 순서 값을 가진 여러 필터가 있을 수 있습니다. 이 경우 Spring Security는 호출되는 순서를 보장하지 않습니다.

## 9.2 체인의 기존 필터 앞에 필터 추가

이 섹션에서는 필터 체인의 기존 필터보다 먼저 사용자 정의 HTTP 필터를 적용하는 방법에 대해 설명합니다. 이것이 유용한 시나리오를 찾을 수 있습니다. 실용적인 방식으로 접근하기 위해 우리는 우리의 예를 위한 프로젝트에서 작업할 것입니다. 이 예제를 통해 사용자 정의 필터를 구현하고 필터 체인의 기존 필터보다 먼저 적용하는 방법을 쉽게 배울 수 있습니다. 그런 다음 프로덕션 애플리케이션에서 찾을 수 있는 유사한 요구 사항에 이 예제를 적용할 수 있습니다.
첫 번째 사용자 정의 필터 구현을 위해 간단한 시나리오를 고려해 보겠습니다. 모든 요청에 ​​Request-Id라는 헤더가 있는지 확인하고 싶습니다(프로젝트 ssia-ch9-ex1 참조). 우리의 애플리케이션은 요청을 추적하기 위해 이 헤더를 사용하고 이 헤더는 필수라고 가정합니다. 동시에 애플리케이션이 인증을 수행하기 전에 이러한 가정을 검증하고자 합니다. 인증 프로세스에는 요청 형식이 유효하지 않은 경우 애플리케이션이 실행하지 않기를 바라는 데이터베이스 또는 기타 리소스 소비 작업을 쿼리하는 작업이 포함될 수 있습니다. 어떻게 해야 하나요? 현재 요구 사항을 해결하려면 두 단계만 수행하면 되며 결국 필터 체인은 그림 9.6과 같습니다.

1. 필터를 구현합니다. 요청에 필요한 헤더가 있는지 확인하는 RequestValidationFilter 클래스를 만듭니다.
2. 필터 체인에 필터를 추가합니다. configure() 메서드를 재정의하여 구성 클래스에서 이 작업을 수행합니다.
 
그림 9.6 이 예에서는 인증 필터보다 먼저 작동하는 RequestValidationFilter를 추가합니다. RequestValidationFilter는 요청의 유효성 검사가 실패할 경우 인증이 발생하지 않도록 합니다. 이 경우 요청에는 Request-Id라는 필수 헤더가 있어야 합니다.
필터를 구현하는 1단계를 수행하기 위해 사용자 정의 필터를 정의합니다. 다음 목록은 구현을 보여줍니다.

목록 9.1 사용자 정의 필터 구현
```java
public class RequestValidationFilter 
  implements Filter {                     ❶

  @Override
  public void doFilter(
     ServletRequest servletRequest, 
     ServletResponse servletResponse, 
     FilterChain filterChain) 
     throws IOException, ServletException {
     // ...
  }
}
```
❶ 이 클래스는 필터를 정의하기 위해 Filter 인터페이스를 구현하고 doFilter() 메서드를 재정의합니다.

doFilter() 메서드 내에서 필터의 논리를 작성합니다. 이 예에서는 Request-Id 헤더가 있는지 확인합니다. 그렇다면 doFilter() 메서드를 호출하여 체인의 다음 필터로 요청을 전달합니다. 헤더가 존재하지 않으면 체인의 다음 필터로 전달하지 않고 응답에 대해 HTTP 상태 400 Bad Request를 설정합니다(그림 9.7). 목록 9.2는 논리를 보여줍니다.
 
그림 9.7 인증 전에 추가한 사용자 정의 필터가 Request-Id
헤더가 존재합니다. 헤더가 요청에 있는 경우 애플리케이션은 인증을 위해 요청을 전달합니다. 헤더가 존재하지 않으면 애플리케이션은 HTTP 상태를 400 Bad Request로 설정하고 클라이언트로 반환합니다.

Listing 9.2 Implementing the logic in the doFilter() method
```java
@Override
public void doFilter(
  ServletRequest request, 
  ServletResponse response, 
  FilterChain filterChain) 
    throws IOException, 
           ServletException {

  var httpRequest = (HttpServletRequest) request;
  var httpResponse = (HttpServletResponse) response;

  String requestId = httpRequest.getHeader("Request-Id");

  if (requestId == null || requestId.isBlank()) {
      httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return; ❶
  }

  filterChain.doFilter(request, response); ❷

}
```
❶ If the header is missing, the HTTP status changes to 400 Bad Request, and the request is not forwarded to the next filter in the chain.

❷ If the header exists, the request is forwarded to the next filter in the chain.

To implement step 2, applying the filter within the configuration class, we use the addFilterBefore() method of the HttpSecurity object because we want the application to execute this custom filter before authentication. This method receives two parameters:

- An instance of the custom filter we want to add to the chain--In our example, this is an instance of the RequestValidationFilter class presented in listing 9.1.

- The type of filter before which we add the new instance--For this example, because the requirement is to execute the filter logic before authentication, we need to add our custom filter instance before the authentication filter. The class Basic-AuthenticationFilter defines the default type of the authentication filter.

Until now, we have referred to the filter dealing with authentication generally as the authentication filter. You’ll find out in the next chapter that Spring Security also configures other filters. In chapter 10, we’ll discuss cross-site request forgery (CSRF) protection and cross-origin resource sharing (CORS), which also rely on filters.

Listing 9.3 shows how to add the custom filter before the authentication filter in the configuration class. To make the example simpler, I use the permitAll() method to allow all unauthenticated requests.

Listing 9.3 Configuring the custom filter before authentication
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.addFilterBefore(                                         ❶
            new RequestValidationFilter(),
            BasicAuthenticationFilter.class)
        .authorizeRequests()
            .anyRequest().permitAll();
  }
}
```
❶ Adds an instance of the custom filter before the authentication filter in the filter chain

We also need a controller class and an endpoint to test the functionality. The next listing defines the controller class.

Listing 9.4 The controller class
```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
```
You can now run and test the application. Calling the endpoint without the header generates a response with HTTP status 400 Bad Request. If you add the header to the request, the response status becomes HTTP 200 OK, and you’ll also see the response body, Hello! To call the endpoint without the Request-Id header, we use this cURL command:
```sh
curl -v http://localhost:8080/hello
This call generates the following (truncated) response:
...
< HTTP/1.1 400
...
```
To call the endpoint and provide the Request-Id header, we use this cURL command:
```sh
curl -H "Request-Id:12345" http://localhost:8080/hello
```
This call generates the following (truncated) response:
```
Hello!
```
## 9.3 Adding a filter after an existing one in the chain

In this section, we discuss adding a filter after an existing one in the filter chain. You use this approach when you want to execute some logic after something already existing in the filter chain. Let’s assume that you have to execute some logic after the authentication process. Examples for this could be notifying a different system after certain authentication events or simply for logging and tracing purposes (figure 9.8). As in section 9.1, we implement an example to show you how to do this. You can adapt it to your needs for a real-world scenario.

For our example, we log all successful authentication events by adding a filter after the authentication filter (figure 9.8). We consider that what bypasses the authentication filter represents a successfully authenticated event and we want to log it. Continuing the example from section 9.1, we also log the request ID received through the HTTP header.
 
Figure 9.8 We add the AuthenticationLoggingFilter after the BasicAuthenticationFilter to log the requests that the application authenticates.

The following listing presents the definition of a filter that logs requests that pass the authentication filter.

Listing 9.5 Defining a filter to log requests
```java
public class AuthenticationLoggingFilter implements Filter {

  private final Logger logger =
          Logger.getLogger(
          AuthenticationLoggingFilter.class.getName());

  @Override
  public void doFilter(
    ServletRequest request, 
    ServletResponse response, 
    FilterChain filterChain) 
      throws IOException, ServletException {

      var httpRequest = (HttpServletRequest) request;

      var requestId = 
        httpRequest.getHeader("Request-Id");         ❶

      logger.info("Successfully authenticated        ❷
                   request with id " +  requestId);  ❷

      filterChain.doFilter(request, response);       ❸
  }
}
```
❶ Gets the request ID from the request headers

❷ Logs the event with the value of the request ID

❸ Forwards the request to the next filter in the chain

To add the custom filter in the chain after the authentication filter, you call the addFilterAfter() method of HttpSecurity. The next listing shows the implementation.
Listing 9.6 Adding a custom filter after an existing one in the filter chain

```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.addFilterBefore(
            new RequestValidationFilter(),
            BasicAuthenticationFilter.class)
        .addFilterAfter(                          ❶
            new AuthenticationLoggingFilter(),
            BasicAuthenticationFilter.class)
        .authorizeRequests()
            .anyRequest().permitAll();
    }
}
```
❶ Adds an instance of AuthenticationLoggingFilter to the filter chain after the authentication filter

Running the application and calling the endpoint, we observe that for every successful call to the endpoint, the application prints a log line in the console. For the call
curl -H "Request-Id:12345" http://localhost:8080/hello
the response body is
```
Hello!
```
In the console, you can see a line similar to this:
```
INFO 5876 --- [nio-8080-exec-2] c.l.s.f.AuthenticationLoggingFilter: Successfully authenticated request with id 12345
```

## 9.4 Adding a filter at the location of another in the chain

In this section, we discuss adding a filter at the location of another one in the filter chain. You use this approach especially when providing a different implementation for a responsibility that is already assumed by one of the filters known by Spring Security. A typical scenario is authentication.

Let’s assume that instead of the HTTP Basic authentication flow, you want to implement something different. Instead of using a username and a password as input credentials based on which the application authenticates the user, you need to apply another approach. Some examples of scenarios that you could encounter are

- Identification based on a static header value for authentication

- Using a symmetric key to sign the request for authentication

- Using a one-time password (OTP) in the authentication process

In our first scenario, identification based on a static key for authentication, the client sends a string to the app in the header of HTTP request, which is always the same. The application stores these values somewhere, most probably in a database or a secrets vault. Based on this static value, the application identifies the client.

This approach (figure 9.9) offers weak security related to authentication, but architects and developers often choose it in calls between backend applications for its simplicity. The implementations also execute fast because these don’t need to do complex calculations, as in the case of applying a cryptographic signature. This way, static keys used for authentication represent a compromise where developers rely more on the infrastructure level in terms of security and also don’t leave the endpoints wholly unprotected.
 
Figure 9.9 The request contains a header with the value of the static key. If this value matches the one known by the application, it accepts the request.

In our second scenario, using symmetric keys to sign and validate requests, both client and server know the value of a key (client and server share the key). The client uses this key to sign a part of the request (for example, to sign the value of specific headers), and the server checks if the signature is valid using the same key (figure 9.10). The server can store individual keys for each client in a database or a secrets vault. Similarly, you can use a pair of asymmetric keys.
 
Figure 9.10 The Authorization header contains a value signed with a key known by both client and server (or a private key for which the server has the public pair). The application checks the signature and, if correct, allows the request.
And finally, for our third scenario, using an OTP in the authentication process, the user receives the OTP via a message or by using an authentication provider app like Google Authenticator (figure 9.11).
 
Figure 9.11 To access the resource, the client has to use a one-time password (OTP). The client obtains the OTP from a third-party authentication server. Generally, applications use this approach during login when multifactor authentication is required.

Let’s implement an example to demonstrate how to apply a custom filter. To keep the case relevant but straightforward, we focus on configuration and consider a simple logic for authentication. In our scenario, we have the value of a static key, which is the same for all requests. To be authenticated, the user must add the correct value of the static key in the Authorization header as presented in figure 9.12. You can find the code for this example in the project ssia-ch9-ex2.
 
Figure 9.12 The client adds a static key in the Authorization header of the HTTP request. The server checks if it knows the key before authorizing the requests.

We start with implementing the filter class, named StaticKeyAuthenticationFilter. This class reads the value of the static key from the properties file and verifies if the value of the Authorization header is equal to it. If the values are the same, the filter forwards the request to the next component in the filter chain. If not, the filter sets the value 401 Unauthorized to the HTTP status of the response without forwarding the request in the filter chain. Listing 9.7 defines the StaticKeyAuthenticationFilter class. In chapter 11, which is the next hands-on exercise, we’ll examine and implement a solution in which we apply cryptographic signatures for authentication as well.

Listing 9.7 The definition of the StaticKeyAuthenticationFilter class
```java
@Component                                           ❶
public class StaticKeyAuthenticationFilter 
  implements Filter {                                ❷

  @Value("${authorization.key}")                     ❸
  private String authorizationKey;

  @Override
  public void doFilter(ServletRequest request, 
                       ServletResponse response, 
                       FilterChain filterChain) 
    throws IOException, ServletException {

    var httpRequest = (HttpServletRequest) request;
    var httpResponse = (HttpServletResponse) response;

    String authentication =                          ❹
           httpRequest.getHeader("Authorization");

    if (authorizationKey.equals(authentication)) {
        filterChain.doFilter(request, response);
    } else {
        httpResponse.setStatus(
                         HttpServletResponse.SC_UNAUTHORIZED);
    }
  }
}
```
❶ To allow us to inject values from the properties file, adds an instance of the class in the Spring context
❷ Defines the authentication logic by implementing the Filter interface and overriding the doFilter() method
❸ Takes the value of the static key from the properties file using the @Value annotation
❹ Takes the value of the Authorization header from the request to compare it with the static key
Once we define the filter, we add it to the filter chain at the position of the class Basic-AuthenticationFilter by using the addFilterAt() method (figure 9.13).
 
Figure 9.13 We add our custom authentication filter at the location where the class BasicAuthenticationFilter would have been if we were using HTTP Basic as an authentication method. This means our custom filter has the same ordering value.
But remember what we discussed in section 9.1. When adding a filter at a specific position, Spring Security does not assume it is the only one at that position. You might add more filters at the same location in the chain. In this case, Spring Security doesn’t guarantee in which order these will act. I tell you this again because I’ve seen many people confused by how this works. Some developers think that when you apply a filter at a position of a known one, it will be replaced. This is not the case! We must make sure not to add filters that we don’t need to the chain.
NOTE I do advise you not to add multiple filters at the same position in the chain. When you add more filters in the same location, the order in which they are used is not defined. It makes sense to have a definite order in which filters are called. Having a known order makes your application easier to understand and maintain.
In listing 9.8, you can find the definition of the configuration class that adds the filter. Observe that we don’t call the httpBasic() method from the HttpSecurity class here because we don’t want the BasicAuthenticationFilter instance to be added to the filter chain.
Listing 9.8 Adding the filter in the configuration class
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Autowired                                       ❶
  private StaticKeyAuthenticationFilter filter;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.addFilterAt(filter,                       ❷
           BasicAuthenticationFilter.class)
        .authorizeRequests()
           .anyRequest().permitAll();
  }
}
```
❶ Injects the instance of the filter from the Spring context

❷ Adds the filter at the position of the basic authentication filter in the filter chain

To test the application, we also need an endpoint. For that, we define a controller, as given in listing 9.4. You should add a value for the static key on the server in the application.properties file, as shown in this code snippet:
```yaml
authorization.key=SD9cICjl1e
```

> **NOTE** Storing passwords, keys, or any other data that is not meant to be seen by everybody in the properties file is never a good idea for a production application. In our examples, we use this approach for simplicity and to allow you to focus on the Spring Security configurations we make. But in real-world scenarios, make sure to use a secrets vault to store such kinds of details.

We can now test the application. We expect that the app allows requests having the correct value for the Authorization header and rejects others, returning an HTTP 401 Unauthorized status on the response. The next code snippets present the curl calls used to test the application. If you use the same value you set on the server side for the Authorization header, the call is successful, and you’ll see the response body, Hello! The call
```sh
curl -H "Authorization:SD9cICjl1e" http:/ /localhost:8080/hello
```
returns this response body:
```
Hello!
```
With the following call, if the Authorization header is missing or is incorrect, the response status is HTTP 401 Unauthorized:
```sh
curl -v http://localhost:8080/hello
The response status is
...
< HTTP/1.1 401
...
```
In this case, because we don’t configure a UserDetailsService, Spring Boot automatically configures one, as you learned in chapter 2. But in our scenario, you don’t need a UserDetailsService at all because the concept of the user doesn’t exist. We only validate that the user requesting to call an endpoint on the server knows a given value. Application scenarios are not usually this simple and often require a UserDetailsService. But, if you anticipate or have such a case where this component is not needed, you can disable autoconfiguration. To disable the configuration of the default UserDetailsService, you can use the exclude attribute of the @SpringBootApplication annotation on the main class like this:

```java
@SpringBootApplication(exclude = 
  {UserDetailsServiceAutoConfiguration.class })
```

## 9.5 Filter implementations provided by Spring Security

In this section, we discuss classes provided by Spring Security, which implement the Filter interface. In the examples in this chapter, we define the filter by implementing this interface directly.

Spring Security offers a few abstract classes that implement the Filter interface and for which you can extend your filter definitions. These classes also add functionality your implementations could benefit from when you extend them. For example, you could extend the GenericFilterBean class, which allows you to use initialization parameters that you would define in a web.xml descriptor file where applicable. A more useful class that extends the GenericFilterBean is OncePerRequestFilter. When adding a filter to the chain, the framework doesn’t guarantee it will be called only once per request. OncePerRequestFilter, as the name suggests, implements logic to make sure that the filter’s doFilter() method is executed only one time per request.

If you need such functionality in your application, use the classes that Spring provides. But if you don’t need them, I’d always recommend you to go as simple as possible with your implementations. Too often, I’ve seen developers extending the GenericFilterBean class instead of implementing the Filter interface in functionalities that don’t require the custom logic added by the GenericFilterBean class. When asked why, it seems they don’t know. They probably copied the implementation as they found it in examples on the web.

To make it crystal clear how to use such a class, let’s write an example. The logging functionality we implemented in section 9.3 makes a great candidate for using OncePerRequestFilter. We want to avoid logging the same requests multiple times. Spring Security doesn’t guarantee the filter won’t be called more than once, so we have to take care of this ourselves. The easiest way is to implement the filter using the OncePerRequestFilter class. I wrote this in a separate project called ssia-ch9-ex3.

In listing 9.9, you find the change I made for the AuthenticationLoggingFilter class. Instead of implementing the Filter interface directly, as was the case in the example in section 9.3, now it extends the OncePerRequestFilter class. The method we override here is doFilterInternal().

Listing 9.9 Extending the OncePerRequestFilter class
```java
public class AuthenticationLoggingFilter 
  extends OncePerRequestFilter {             ❶

  private final Logger logger =
          Logger.getLogger(
            AuthenticationLoggingFilter.class.getName());

  @Override
  protected void doFilterInternal(           ❷
    HttpServletRequest request,              ❸
    HttpServletResponse response,            ❸
    FilterChain filterChain) throws     
      ServletException, IOException {

      String requestId = request.getHeader("Request-Id");

      logger.info("Successfully authenticated request with id " +
                   requestId);

      filterChain.doFilter(request, response);
  }
}
```
❶ Filter 인터페이스를 구현하는 대신 OncePerRequestFilter 클래스를 확장

❷ Filter 인터페이스의 doFilter() 메서드의 목적을 대체하는 doFilterInternal()을 재정의합니다.

❸ OncePerRequestFilter는 HTTP 필터만 지원합니다. 이것이 매개변수가 HttpServletRequest와 HttpServletResponse로 직접 주어지는 이유입니다.

유용할 수 있는 OncePerRequestFilter 클래스에 대한 몇 가지 짧은 관찰:

- HTTP 요청만 지원하지만 실제로 우리가 항상 사용하는 것입니다. 장점은 유형을 캐스팅하고 HttpServlet-Request 및 HttpServletResponse로 요청을 직접 수신한다는 것입니다. Filter 인터페이스를 사용하여 요청과 응답을 캐스트해야 함을 기억하십시오.

- 필터 적용 여부를 결정하는 로직을 구현할 수 있습니다. 필터를 체인에 추가하더라도 특정 요청에는 적용되지 않는다고 결정할 수 있습니다. shouldNotFilter(HttpServletRequest) 메서드를 재정의하여 이를 설정합니다. 기본적으로 필터는 모든 요청에 ​​적용됩니다.

- 기본적으로 OncePerRequestFilter는 비동기 요청 또는 오류 디스패치 요청에 적용되지 않습니다. shouldNotFilterAsyncDispatch() 및 shouldNotFilterErrorDispatch() 메서드를 재정의하여 이 동작을 변경할 수 있습니다.

구현에서 이러한 OncePerRequestFilter 특성 중 하나라도 유용하다고 생각되면 이 클래스를 사용하여 필터를 정의하는 것이 좋습니다.

## 요약

- HTTP 요청을 가로채는 웹 애플리케이션 아키텍처의 첫 번째 계층은 필터 체인입니다. Spring Security 아키텍처의 다른 구성 요소의 경우 요구 사항에 맞게 사용자 지정할 수 있습니다.

- 기존 필터 이전, 기존 필터 이후 또는 기존 필터 위치에 새 필터를 추가하여 필터 체인을 사용자 정의할 수 있습니다.

- 기존 필터의 동일한 위치에 여러 개의 필터를 가질 수 있습니다. 이 경우 필터가 실행되는 순서는 정의되지 않습니다.

- 필터 체인을 변경하면 애플리케이션의 요구 사항에 정확하게 일치하도록 인증 및 권한을 사용자 지정하는 데 도움이 됩니다.