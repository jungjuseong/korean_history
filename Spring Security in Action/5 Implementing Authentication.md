# 5 인증 구현

4장에서 PasswordEncoder가 암호를 관리하는 방법과 암호를 사용하는 방법, 그리고 암호화기와 키 생성기가 있는 SSCM(Spring Security 암호 모듈)을 배웠습니다.

AuthenticationProvider는 인증 로직을 담당한다. 이곳에서 요청을 승인할지를 결정하는 조건이나 지시를 찾는다. AuthenticatonManage는 HTTP 필터로부터 요청을 받으며 이러한 책임을 AuthenticationProvider에게 위임한다. 
이 장에서는 다음 두가지 가능한 결과만 갖는 인증 프로세스를 조사한다.

- 요청한 엔터티가 승인되지 않음. 사용자가 식별되지 않으며 앱은 요청을 거부한다. 대부분의 경우는 HTTP 404로 응답한다.
- 요청한 엔터티가 승인됨. 인증에 필요한 요청자 정보가 저장될 수 있다. SecurityContext는 현재 승인된 요청의 상세 정보가 저장된 인스턴스이다.

액터와 이들 사이의 링크를 상기시키기 위해 그림 5.1은 2장에서도 본 다이어그램을 제공합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F01_Spilca.png)

Figure 5.1 스프링 시큐리티의 인증 흐름도. 앱이 누군가의 요청을 파악하는 방법을 정의한다. 이 장에서 다루는 객체는 그림자 처리된 박스이다. AuthenticationProvider는 인증 로직을, SecurityContext는 인증된 요청에 관한 정보를 저장한다

이 장에서는 인증 흐름의 나머지 부분을 다룹니다(그림 5.1에서 음영 처리된 상자). 그런 다음 7장과 8장에서 HTTP 요청에서 인증을 따르는 프로세스인 인증이 작동하는 방식을 배우게 됩니다. 먼저 AuthenticationProvider를 구현하는 방법을 논의 합니다. Spring Security가 인증 프로세스에서 요청을 이해하는 방법을 알아야 합니다. 

## 5.1 Understanding the AuthenticationProvider

기업용 앱에서는 사용자 이름과 암호를 기반으로 하는 기본 인증구현이 적용되지 않는 상황에 처할 수 있다. 또한 인증과 관련하여 앱에 여러가지 시나리오의 구현이 필요할 수 있다. 예를 들어 SMS 메시지로 받서나 특정 앱에 표시된 코드로 자신을 증명할 수 있기를 원할 수 있다. 또는 사용자가 파일에 저장된 어떤 유형의 키를 제공하여야 하는 인증 시나리오를 구현하기도 한다. 심지어 사용자의 지문으로 인증 로직을 구현할 필요가 있다. 프레임워크의 목적은 어떤 시나리오도 구현이 가능할 정도로 유연해야 한다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F02_Spilca.png)

그림 5.2 응용 프로그램의 경우 다른 방법으로 인증을 구현해야 할 수도 있습니다. 대부분의 경우 사용자 이름과 암호로 충분하지만 경우에 따라 사용자 인증 시나리오가 더 복잡할 수 있습니다. 

프레임워크는 가장 일반적인 구현체들을 제공하지만 물론 모든 가능한 옵션들을 커버하지는 못한다. 스프링 시큐리티에서의 용어로는 AuthenticationProder를 사용하여 인증 로직의 변경을 정의할 수 있다. 이 절에서는 Authentication 인터페이스를 구현하여 인증 이벤트를 나타내고 AuthenticaionProvider로 개인화된 인증 로직을 만드는 것을 배운다. 그러기 위해서

- 인증 이벤트
- 인증 로직을 담당하는 AuthenticationProvider
- 인증 로직을 구현

### 5.1.1 REPRESENTING THE REQUEST DURING AUTHENTICATION

인증 과정에서 요청을 나타내는 방법을 다룬다. 개인화된 인증 로직을 구현하기 전에 이 부분을 다루는 것이 중요하다. 개인회된 AuthenticaionProvicer를 구현하려면 인증 이벤트 자체를 표현할 수 있어야 한다.

Authentication은 인증 절차에 관련된 필수 인터페이스 중 하나이다. 이 인터페이스는 인증 요청 이벤트를 나타내며 앱에 대한 접근 요청을 한 엔터티 정보를 가진다. 여러분은 인증 절차 동안 또는 이후에 인증 요청 이벤트에 관련된 정보를 사용할 수 있다. 사용자의 앱에 대한 접근 요청을 Principal이라고 부른다. 스프링 시큐리티는 이 것을 확장한다

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F03_Spilca.png)

Figure 5.3 Authentication은 Principal을 상속한다. Authentication은 암호 요구 또는 인증 요청에 대한 정보와 같은 요구 사항을 추가한다. 권한 목록은 스프링 시큐리티에만 있는 것이다.

Spring Security의 인증 계약은 주체를 나타낼 뿐만 아니라 인증 프로세스의 완료 여부에 대한 정보와 권한 집합을 추가합니다. 이 계약이 Java Security API에서 Principal 계약을 확장하도록 설계되었다는 사실은 다른 프레임워크 및 애플리케이션 구현과의 호환성 측면에서 플러스입니다. 이러한 유연성 덕분에 다른 방식으로 인증을 구현하는 애플리케이션에서 Spring Security로 더 쉽게 마이그레이션할 수 있습니다.

다음 목록에서 인증 인터페이스 디자인에 대해 자세히 알아보겠습니다. 

목록 5.1 Authentication 인터페이스
```java
public interface Authentication extends Principal, Serializable {

  Collection<? extends GrantedAuthority> getAuthorities();
  Object getCredentials();
  Object getDetails();
  Object getPrincipal();
  boolean isAuthenticated();
  void setAuthenticated(boolean isAuthenticated) 
     throws IllegalArgumentException;
}
```
현재로서는 이 계약에서 배워야 할 유일한 메서드는 다음과 같습니다.

- isAuthenticated() - 인증 프로세스가 종료되면 true를 반환하고 인증 프로세스가 아직 진행 중인 경우 false를 반환합니다.

- getCredentials() - 인증 프로세스에 사용된 암호 또는 비밀을 반환합니다.

- getAuthorities() - 인증된 요청에 대해 부여된 권한 모음을 반환합니다.

이후 장에서 인증 계약을 위한 다른 방법에 대해 논의할 것입니다.

### 5.1.2 사용자 지정 인증 로직 구현

사용자 지정 인증 논리 구현에 대해 설명합니다. 이 책임과 관련된 Spring Security 계약을 분석하여 정의를 이해합니다. 이러한 세부 정보를 사용하여 5.1.3의 코드 예제를 사용하여 사용자 지정 인증 논리를 구현합니다.

Spring Security의 AuthenticationProvider는 인증 로직을 처리하며 기본 구현에서는 시스템 사용자를 찾는 책임을 UserDetailsService에 위임합니다. 인증 과정에서 비밀번호 관리를 위해서도 PasswordEncoder를 사용합니다. 

다음 목록은 애플리케이션에 대한 사용자 지정 인증 공급자를 정의하기 위해 구현해야 하는 AuthenticationProvider의 정의를 제공합니다.

Listing 5.2 The AuthenticationProvider interface
```java
public interface AuthenticationProvider {

  Authentication authenticate(Authentication authentication) 
    throws AuthenticationException;

  boolean supports(Class<?> authentication);
}
```
AuthenticationProvider 책임은 인증 계약과 강력하게 연결되어 있습니다. authenticate() 메소드는 매개변수로 인증 객체를 수신하고 인증 객체를 반환합니다. 인증 로직은 authenticate() 메서드에서 구현합니다. 
다음은 authenticate() 메서드를 구현해야 하는 방법의 요약입니다.

- 인증이 실패하면 AuthenticationException을 던져야 합니다.

- 메서드가 AuthenticationProvider 구현에서 지원하지 않는 인증 개체를 수신하는 경우 메서드는 null을 반환해야 합니다. 이런 식으로 HTTP 필터 수준에서 분리된 여러 인증 유형을 사용할 수 있습니다. 이 측면에 대해서는 9장에서 더 논의할 것입니다. 11장에서 여러 AuthorizationProvider 클래스가 있는 예제를 찾을 수 있습니다.

- 메서드는 완전히 인증된 개체를 나타내는 인증 인스턴스를 반환해야 합니다. 이 경우 isAuthenticated() 메서드는 true를 반환하고 인증된 엔터티에 대한 모든 필요한 세부 정보를 포함합니다. 일반적으로 애플리케이션은 이 인스턴스에서 비밀번호와 같은 민감한 데이터도 제거합니다. 구현 후에는 암호가 더 이상 필요하지 않으며 이러한 세부 정보를 유지하면 잠재적으로 원치 않는 눈에 노출될 수 있습니다.

AuthenticationProvider의 두 번째 메서드는 support(Class<?> 인증)입니다. 현재 AuthenticationProvider가 인증 객체로 제공된 유형을 지원하는 경우 이 메서드를 구현하여 true를 반환할 수 있습니다. 이 메서드가 객체에 대해 true를 반환하더라도 authenticate() 메서드가 null을 반환하여 요청을 거부할 가능성이 여전히 있음을 주목하세요. Spring Security는 더 유연하게 설계되었으며 유형뿐만 아니라 요청의 세부 정보를 기반으로 인증 요청을 거부할 수 있는 AuthenticationProvider를 구현할 수 있습니다.

인증 관리자와 인증 공급자가 인증 요청을 확인하거나 무효화하기 위해 함께 작동하는 방식에 대한 비유는 출입문에 대한 잠금이 더 복잡하다는 것입니다. 이 자물쇠는 카드나 구식 물리적 키를 사용하여 열 수 있습니다(그림 5.4). 자물쇠 자체가 문을 열지 여부를 결정하는 인증 관리자입니다. 결정을 내리기 위해 두 인증 제공자에게 위임합니다. 하나는 카드의 유효성을 검사하는 방법을 알고 다른 하나는 물리적 키를 확인하는 방법을 알고 있습니다. 문을 열기 위해 카드를 제시하면 물리적 키로만 작동하는 인증 업체는 이런 인증을 모른다고 하소연합니다. 그러나 다른 공급자는 이러한 종류의 인증을 지원하고 카드가 문에 유효한지 확인합니다. 이것이 실제로 support() 메서드의 목적입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F04_Spilca.png)

그림 5.4 AuthenticationManager는 사용 가능한 인증 공급자 중 하나에 위임합니다. AuthenticationProvider는 제공된 인증 유형을 지원하지 않을 수 있습니다. 반면에 개체 유형을 지원하는 경우 해당 특정 개체를 인증하는 방법을 모를 수 있습니다. 인증이 평가되고 요청이 올바른지 여부를 말할 수 있는 AuthenticationProvider가 AuthenticationManager에 응답합니다.

인증 유형을 테스트하는 것 외에도 Spring Security는 유연성을 위해 레이어를 하나 더 추가합니다. 도어록은 여러 종류의 카드를 인식할 수 있습니다. 이 경우 카드를 제시할 때 인증 제공자 중 한 명이 "이것은 카드로 이해합니다. 하지만 내가 확인할 수 있는 카드 유형이 아닙니다!" 이는 support()가 true를 반환하지만 authenticate()가 null을 반환할 때 발생합니다.

### 5.1.3 사용자 지정 인증 논리 적용

사용자 지정 인증 논리를 구현합니다.(ssia-ch5-ex1). 이 예제에서는 5.1.1 및 5.1.2에서 Authentication 및 AuthenticationProvider에 대해 배운 내용을 적용합니다. 목록 5.3과 5.4에서는 사용자 정의 AuthenticationProvider를 구현하는 방법의 예를 단계별로 작성합니다. 그림 5.5에도 나와 있는 이러한 단계는 다음과 같습니다.

1. 인증 공급자를 구현하는 클래스를 선언합니다.
2. 새 인증 공급자가 지원하는 인증 개체의 종류를 결정합니다.
3. 인증 공급자가 지원하는 인증 유형을 지정하기 위해 support(Class<?> c)를 재정의합니다.
4. authenticate(Authentication a)에서 인증 논리를 구현합니다.
5. Spring Security에 새로운 인증 공급자를 등록하십시오.

**Listing 5.3** Overriding the supports() method of the AuthenticationProvider
```java
@Component
public class CustomAuthenticationProvider 
  implements AuthenticationProvider {

  // Omitted code

  @Override
  public boolean supports(Class<?> authenticationType) {
    return authenticationType
            .equals(UsernamePasswordAuthenticationToken.class);
  }
}
```
목록 5.3에서 AuthenticationProvider를 구현하는 새 클래스를 정의합니다. Spring 컨텍스트에서 해당 유형의 인스턴스를 갖도록 클래스를 @Component로 표시합니다. 그런 다음 이 AuthenticationProvider가 지원하는 인증 인터페이스 구현의 종류를 결정해야 합니다. 이는 authenticate()에 대한 매개변수로 제공되는 유형에 따라 다릅니다. 인증 필터 수준에서 아무 것도 사용자 지정하지 않으면(9장에서는 그렇게 할 것입니다) UsernamePasswordAuthenticationToken 클래스가 유형을 정의합니다. 이 클래스는 인증 인터페이스의 구현이며 사용자 이름과 암호가 있는 표준 인증 요청을 나타냅니다.

이 정의를 통해 우리는 AuthenticationProvider가 특정 종류의 키를 지원하도록 만들었습니다. AuthenticationProvider의 범위를 지정했으면 다음 목록과 같이 authenticate() 메서드를 재정의하여 인증 논리를 구현합니다.

Listing 5.4 인증 로직 구현
```java
@Component
public class CustomAuthenticationProvider 
  implements AuthenticationProvider {

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public Authentication authenticate(Authentication authentication) {
    String username = authentication.getName();
    String password = authentication.getCredentials().toString();

    UserDetails u = userDetailsService.loadUserByUsername(username);

    if (passwordEncoder.matches(password, u.getPassword())) {
      return new UsernamePasswordAuthenticationToken(
            username, 
            password, 
            u.getAuthorities());                ❶
    } else {
      throw new BadCredentialsException
                  ("Something went wrong!");    ❷
    }
  }

  // Omitted code
}
```
❶ 비밀번호가 일치하면 필요한 세부 정보와 함께 인증 계약의 구현을 반환합니다.

❷ 비밀번호가 일치하지 않으면 AuthenticationException 유형의 예외를 던집니다. BadCredentialsException은 AuthenticationException에서 상속됩니다.

목록 5.4의 논리는 간단하며 그림 5.5는 이 논리를 시각적으로 보여줍니다. UserDetailsService 구현을 사용하여 UserDetails를 가져옵니다. 사용자가 존재하지 않는 경우 loadUserByUsername()는 AuthenticationException을 던져야 합니다. 이 경우 인증 프로세스가 중지되고 HTTP 필터가 응답 상태를 HTTP 401 Unauthorized로 설정합니다. username이 존재하는 경우 컨텍스트에서 PasswordEncoder의 match() 메서드를 사용하여 사용자의 암호를 추가로 확인할 수 있습니다. 비밀번호가 일치하지 않으면 다시 AuthenticationException이 발생해야 합니다. 암호가 정확하면 인증 공급자는 요청에 대한 세부 정보가 포함된 "인증됨"으로 표시된 인증 인스턴스를 반환합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F05_Spilca.png)

**그림 5.5** 인증 공급자에 의해 구현된 사용자 정의 인증 흐름. 인증 요청을 검증하기 위해 인증 공급자는 제공된 UserDetailsService 구현으로 사용자 세부 정보를 로드하고 암호가 일치하는 경우 PasswordEncoder로 암호를 검증합니다. 사용자가 없거나 암호가 잘못된 경우 예외를 발생시킵니다.

인증 공급자의 새 구현을 연결하려면 ProjectConfig 클래스에서 WebSecurityConfigurerAdapter의 configure(AuthenticationManagerBuilder auth) 를 재정의합니다.

Listing 5.5 인증 공급자 등록
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private AuthenticationProvider authenticationProvider;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
      auth.authenticationProvider(authenticationProvider);
  }

  // Omitted code
}
```
❶ 비밀번호가 일치하면 필요한 세부 정보와 함께 인증 계약의 구현을 반환합니다.

❷ 비밀번호가 일치하지 않으면 AuthenticationException 유형의 예외를 throw합니다. BadCredentialsException은 AuthenticationException에서 상속됩니다.

목록 5.4의 논리는 간단하며 그림 5.5는 이 논리를 시각적으로 보여줍니다. UserDetailsService 구현을 사용하여 UserDetails를 가져옵니다. 사용자가 존재하지 않는 경우 loadUserByUsername()는 인증 예외를 던져야 합니다. 이 경우 인증 프로세스가 중지되고 HTTP 필터가 응답 상태를 HTTP 401 Unauthorized로 설정합니다. 사용자 이름이 존재하는 경우 컨텍스트에서 PasswordEncoder의 match() 메서드를 사용하여 사용자의 암호를 추가로 확인할 수 있습니다. 비밀번호가 일치하지 않으면 다시 AuthenticationException이 발생해야 합니다. 암호가 정확하면 AuthenticationProvider는 요청에 대한 세부 정보가 포함된 "인증됨"으로 표시된 인증 인스턴스를 반환합니다.
 
AuthenticationProvider의 새 구현을 연결하려면 프로젝트의 구성 클래스에서 WebSecurityConfigurerAdapter 클래스의 configure(AuthenticationManagerBuilder auth) 메서드를 재정의합니다. 이것은 다음 목록에 나와 있습니다.

이 이야기에서 얻을 수 있는 몇 가지 교훈:

- 프레임워크, 특히 응용 프로그램에서 널리 사용되는 프레임워크는 많은 똑똑한 개인의 참여로 작성됩니다. 그렇더라도 잘못 구현될 수 있다고 보기는 어렵습니다. 문제가 프레임워크의 잘못이라는 결론을 내리기 전에 항상 애플리케이션을 분석하십시오.

- 프레임워크를 사용하기로 결정할 때 최소한 기본 사항을 잘 이해하고 있는지 확인하십시오.

- 프레임워크에 대해 배우기 위해 사용하는 리소스에 유의하십시오. 때때로 웹에서 찾은 기사는 빠른 해결 방법을 보여주지만 반드시 클래스 디자인을 올바르게 구현하는 방법은 아닙니다.
- 연구에 여러 소스를 사용합니다. 당신의 오해를 명확히 하기 위해, 어떤 것을 사용하는 방법이 확실하지 않을 때 개념 증명을 작성하십시오.

- 프레임워크를 사용하기로 결정했다면 최대한 의도한 목적에 맞게 사용하세요. 예를 들어, Spring Security를 ​​사용하고 보안 구현을 위해 프레임워크가 제공하는 것에 의존하는 대신 더 많은 사용자 정의 코드를 작성하는 경향이 있음을 관찰했다고 가정해 보겠습니다. 왜 이런 일이 발생하는지에 대한 질문을 제기해야 합니다.
- 
프레임워크에 의해 구현된 기능에 의존할 때 몇 가지 이점을 누릴 수 있습니다. 우리는 그들이 테스트되었으며 취약점을 포함하는 변경 사항이 적다는 것을 알고 있습니다. 또한 좋은 프레임워크는 유지 관리 가능한 응용 프로그램을 만드는 데 도움이 되는 추상화에 의존합니다. 자체 구현을 작성할 때 취약성을 포함할 가능성이 더 높다는 점을 기억하십시오.

## 5.2 SecurityContext 사용하기

SecurityContext의 작동 방식, 데이터 액세스 방법 및 다양한 스레드 관련 시나리오에서 애플리케이션이 데이터를 관리하는 방법을 분석합니다. 이 섹션을 마치면 다양한 상황에 대한 SecurityContext를 구성하는 방법을 알게 될 것입니다. 이런 식으로 7장과 8장에서 권한 부여를 구성할 때 보안 컨텍스트에 의해 저장된 인증된 사용자에 대한 세부 정보를 사용할 수 있습니다.

인증 프로세스가 끝난 후 인증된 엔터티에 대한 세부 정보가 필요할 수 있습니다. 예를 들어, 현재 인증된 사용자의 사용자 이름이나 권한을 참조해야 할 수 있습니다. 인증 매니저가 인증 프로세스를 성공적으로 완료하면 나머지 요청에 대한 인증 인스턴스를 저장합니다. 인증 객체를 저장하는 인스턴스를 SeccurityContext라고 합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F06_Spilca.png)

그림 5.6 인증 성공 후 인증 필터는 인증된 엔터티의 세부 정보를 SecuirtyContext에 저장합니다. 거기에서 요청에 매핑된 작업을 구현하는 컨트롤러는 필요할 때 이러한 세부 정보에 액세스할 수 있습니다.

Listing 5.6 The SecurityContext interface
```java
public interface SecurityContext extends Serializable {

  Authentication getAuthentication();
  void setAuthentication(Authentication authentication);
}
```
계약 정의에서 알 수 있듯이 SecurityContext의 주요 책임은 인증 객체를 저장하는 것입니다. 그러나 SecurityContext 자체는 어떻게 관리됩니까? Spring Security는 관리자 역할의 객체로 SecurityContext를 관리하는 세 가지 전략을 제공합니다. 이름은 SecurityContextHolder입니다.

- MODE_THREADLOCAL--각 스레드가 컨텍스트에 고유한 세부 정보를 저장할 수 있도록 합니다. 요청당 스레드 웹 애플리케이션에서 이는 각 요청에 개별 스레드가 있기 때문에 일반적인 접근 방식입니다.

- MODE_INHERITABLETHREADLOCAL--MODE_THREADLOCAL과 유사하지만 비동기 메서드의 경우 컨텍스트를 다음 스레드로 복사하도록 Spring Security에 지시합니다. 이렇게 하면 @Async 메서드를 실행하는 새 스레드가 컨텍스트를 상속한다고 말할 수 있습니다.

- MODE_GLOBAL--응용 프로그램의 모든 스레드가 동일한 보안 컨텍스트 인스턴스를 보도록 합니다.

Spring Security에서 제공하는 컨텍스트를 관리하기 위한 이 세 가지 전략 외에도 Spring에서 알지 못하는 자신의 스레드를 정의할 때 어떤 일이 발생하는지 논의합니다. 이러한 경우 컨텍스트에서 새 스레드로 세부 정보를 명시적으로 복사해야 합니다. Spring Security는 Spring의 컨텍스트에 있지 않은 객체를 자동으로 관리할 수 없지만 이를 위한 몇 가지 훌륭한 유틸리티 클래스를 제공합니다..

### 5.2.1 보안 컨텍스트에 대한 보유 전략 사용

시큐리티 컨텍스트를 관리하기 위한 첫 번째 전략은 MODE_THREADLOCAL 전략입니다. 이 전략은 Spring Security에서 사용하는 시큐리티 컨텍스트를 관리하기 위한 기본값이기도 합니다. 

이 전략을 사용하면 Spring Security는 ThreadLocal을 사용하여 컨텍스트를 관리합니다. ThreadLocal은 JDK에서 제공하는 구현입니다. 이 구현은 데이터 컬렉션으로 작동하지만 애플리케이션의 각 스레드가 컬렉션에 저장된 데이터만 볼 수 있도록 합니다. 이런 식으로 각 요청은 시큐리티 컨텍스트에 액세스할 수 있습니다. 어떤 스레드도 다른 스레드의 ThreadLocal에 액세스할 수 없습니다. 이는 웹 애플리케이션에서 각 요청이 자체 시큐리티 컨텍스트만 볼 수 있음을 의미합니다. 이것은 백엔드 웹 애플리케이션에 대해 일반적으로 원하는 것이기도 합니다.

그림 5.7은 이 기능에 대한 개요를 제공합니다. 각 요청(A, B 및 C)에는 할당된 스레드(T1, T2 및 T3)가 있습니다. 이렇게 하면 각 요청은 보안 컨텍스트에 저장된 세부 정보만 볼 수 있습니다. 그러나 이것은 또한 새 스레드가 생성되면(예: 비동기식 메서드가 호출될 때) 새 스레드에도 자체 보안 컨텍스트가 있음을 의미합니다. 부모 스레드(요청의 원래 스레드)의 세부 정보는 새 스레드의 보안 컨텍스트에 복사되지 않습니다.

> 참고 여기에서는 각 요청이 스레드에 연결되는 전통적인 서블릿 응용 프로그램에 대해 설명합니다. 이 아키텍처는 각 요청에 고유한 스레드가 할당된 기존 서블릿 애플리케이션에만 적용됩니다. 반응형 애플리케이션에는 적용되지 않습니다. 반응적 접근 방식의 보안에 대해서는 19장에서 자세히 설명합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F07_Spilca.png)

그림 5.7 각 요청에는 화살표로 표시된 자체 스레드가 있습니다. 각 스레드는 자체 시큐리티 컨텍스트 세부 정보에만 액세스할 수 있습니다. 새 스레드가 생성되면(예: @Async 메서드로) 상위 스레드의 세부 정보는 복사되지 않습니다.

시큐리티 컨텍스트를 관리하기 위한 기본 전략이므로 이 프로세스를 명시적으로 구성할 필요가 없습니다. 인증 프로세스가 끝난 후 필요할 때마다 정적 getContext() 메서드를 사용하여 보유자에게 보안 컨텍스트를 요청하기만 하면 됩니다. 목록 5.7에서 애플리케이션의 끝점 중 하나에서 보안 컨텍스트를 얻는 예를 찾을 수 있습니다. 보안 컨텍스트에서 인증된 엔터티에 대한 세부 정보를 저장하는 인증 개체를 추가로 가져올 수 있습니다. 이 섹션에서 ssia-ch5-ex2 프로젝트의 일부로 논의하는 예제를 찾을 수 있습니다.

Listing 5.7 Obtaining the SecurityContext from the SecurityContextHolder
```java
@GetMapping("/hello")
public String hello() {
  SecurityContext context = SecurityContextHolder.getContext();
  Authentication a = context.getAuthentication();

  return "Hello, " + a.getName() + "!";
}
```
컨텍스트에서 인증을 얻는 것이 엔드포인트 수준에서 훨씬 더 편안합니다. Spring은 이를 메서드 매개변수에 직접 주입하는 것을 알고 있기 때문입니다. 매번 SecurityContextHolder 클래스를 명시적으로 참조할 필요는 없습니다. 다음 목록에 나와 있는 이 접근 방식이 더 좋습니다.

목록 5.8 Spring은 메소드의 매개변수에 인증 값을 삽입합니다.
```java
@GetMapping("/hello")
public String hello(Authentication a) {        ❶
  return "Hello, " + a.getName() + "!";
}
```
❶ Spring Boot는 메소드 매개변수에 현재 인증을 삽입합니다.

올바른 사용자로 엔드포인트를 호출하면 응답 본문에 사용자 이름이 포함됩니다. 예를 들어,
```sh
curl -u user:99ff79e3-8ca0-401c-a396-0a8625ab3bad http://localhost:8080/hello
```
```
Hello, user!
```

778 / 5000
번역 결과
### 5.2.2 비동기식 호출에 대한 보유 전략 사용

시큐리티 컨텍스트를 관리하기 위한 기본 전략을 고수하는 것은 쉽습니다. 그리고 많은 경우에 필요한 유일한 것입니다. MODE_THREADLOCAL은 각 스레드에 대한 보안 컨텍스트를 격리하는 기능을 제공하며 보안 컨텍스트를 보다 자연스럽게 이해하고 관리할 수 있도록 합니다. 그러나 이것이 적용되지 않는 경우도 있습니다.

요청당 여러 스레드를 처리해야 하는 경우 상황이 더 복잡해집니다. 엔드포인트를 비동기식으로 만들면 어떻게 되는지 살펴보세요. 메소드를 실행하는 스레드는 더 이상 요청을 처리하는 동일한 스레드가 아닙니다. 다음 목록에 나와 있는 것과 같은 끝점을 생각해 보십시오.

목록 5.9 다른 스레드에서 제공하는 @Async 메서드 
```java
@GetMapping("/bye")
@Async                     ❶
public void goodbye() {
  SecurityContext context = SecurityContextHolder.getContext();
  String username = context.getAuthentication().getName();

  // do something with the username
}
```
❶ Being @Async, the method is executed on a separate thread.

To enable the functionality of the @Async annotation, I have also created a configuration class and annotated it with @EnableAsync, as shown here:
```java
@Configuration
@EnableAsync
public class ProjectConfig {
}
```
> **참고** 기사나 포럼에서 구성 주석이 기본 클래스 위에 놓이는 경우가 있습니다. 예를 들어, 특정 예제에서는 기본 클래스에서 직접 @EnableAsync 주석을 사용한다는 것을 알 수 있습니다. 이 접근 방식은 @Configuration 특성을 포함하는 @SpringBootApplication 주석으로 Spring Boot 애플리케이션의 기본 클래스에 주석을 달기 때문에 기술적으로 정확합니다. 그러나 실제 응용 프로그램에서는 책임을 별도로 유지하는 것을 선호하며 기본 클래스를 구성 클래스로 사용하지 않습니다. 이 책의 예제에 대해 가능한 한 명확하게 하기 위해 실제 시나리오에서 주석을 찾는 방법과 유사하게 @Configuration 클래스보다 이러한 주석을 유지하는 것을 선호합니다.

코드를 있는 그대로 시도하면 인증에서 이름을 가져오는 줄에 NullPointerException이 발생합니다.
```java
String username = context.getAuthentication().getName()
```
시큐리티 컨텍스트를 상속하지 않는 다른 스레드에서 메서드가 실행되기 때문입니다. 이러한 이유로 Authorization 개체는 null이고 제시된 코드의 컨텍스트에서 NullPointerException을 발생시킵니다. 이 경우 MODE_INHERITABLETHREADLOCAL 전략을 사용하여 문제를 해결할 수 있습니다. 

이것은 SecurityContextHolder.setStrategyName() 메서드를 호출하거나 시스템 속성 spring.security.strategy를 사용하여 설정할 수 있습니다. 이 전략을 설정함으로써 프레임워크는 요청의 원래 스레드 세부 정보를 비동기 메서드의 새로 생성된 스레드에 복사한다는 것을 알게 됩니다(그림 5.8).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F08_Spilca.png)

그림 5.8 MODE_INHERITABLETHREADLOCAL을 사용할 때 프레임워크는 요청의 원래 스레드에서 새 스레드의 시큐리티 컨텍스트로 시큐리티 컨텍스트 세부 정보를 복사합니다.

다음 목록은 setStrategyName() 메서드를 호출하여 보안 컨텍스트 관리 전략을 설정하는 방법을 보여줍니다.

Listing 5.10 Using InitializingBean to set SecurityContextHolder mode
```java
@Configuration
@EnableAsync
public class ProjectConfig {

  @Bean
  public InitializingBean initializingBean() {
    return () -> SecurityContextHolder.setStrategyName(
      SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
  }
}
```
엔드포인트를 호출하면 이제 보안 컨텍스트가 Spring에 의해 다음 스레드로 올바르게 전파되는 것을 관찰할 수 있습니다. 또한 인증은 더 이상 null이 아닙니다.

> 참고 그러나 이것은 프레임워크 자체가 스레드를 생성할 때만 작동합니다(예: @Async 메서드의 경우). 코드가 스레드를 생성하면 MODE _INHERITABLETHREADLOCAL 전략을 사용해도 동일한 문제가 발생합니다. 이것은 이 경우 프레임워크가 코드가 생성하는 스레드에 대해 알지 못하기 때문에 발생합니다. 섹션 5.2.4 및 5.2.5에서 이러한 경우의 문제를 해결하는 방법에 대해 논의할 것입니다.

### 5.2.3 독립형 애플리케이션에 대한 보유 전략 사용

애플리케이션의 모든 스레드가 공유하는 보안 컨텍스트가 필요한 경우 전략을 MODE_GLOBAL로 변경합니다(그림 5.9). 응용 프로그램의 일반적인 그림에 맞지 않으므로 웹 서버에 이 전략을 사용하지 않습니다. 백엔드 웹 애플리케이션은 수신하는 요청을 독립적으로 관리하므로 모든 요청에 ​​대해 하나의 컨텍스트가 아닌 요청별로 보안 컨텍스트를 분리하는 것이 더 합리적입니다. 그러나 이것은 독립 실행형 응용 프로그램에 유용할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F09_Spilca.png)

그림 5.9 보안 컨텍스트 관리 전략으로 MODE_GLOBAL을 사용하면 모든 스레드가 동일한 보안 컨텍스트에 액세스합니다. 이는 이들 모두가 동일한 데이터에 액세스할 수 있고 해당 정보를 변경할 수 있음을 의미합니다. 이 때문에 경쟁 조건이 발생할 수 있으며 동기화에 주의해야 합니다.

다음 코드 스니펫에서 볼 수 있듯이 MODE_INHERITABLETHREADLOCAL과 동일한 방식으로 전략을 변경할 수 있습니다. SecurityContextHolder.setStrategyName() 메서드나 spring.security .strategy 시스템 속성을 사용할 수 있습니다.
```java
@Bean
public InitializingBean initializingBean() {
  return () -> SecurityContextHolder.setStrategyName(
    SecurityContextHolder.MODE_GLOBAL);
}
```
또한 SecurityContext는 스레드로부터 안전하지 않습니다. 따라서 애플리케이션의 모든 스레드가 SecurityContext 개체에 액세스할 수 있는 이 전략에서는 동시 액세스를 처리해야 합니다.

### 5.2.4 DELEGATINGSECURITYCONTEXTRUNNABLE로 시큐리티 컨텍스트 전달

세 가지 모드(MODE_THREADLOCAL, MODE_INHERITEDTHREADLOCAL 및 MODE_GLOBAL)로 시큐리티 컨텍스트를 관리할 수 있다는 것을 배웠습니다. 기본적으로 프레임워크는 요청 스레드에 대한 시큐리티 컨텍스트만 제공하도록 하고 이 시큐리티 컨텍스트는 해당 스레드에서만 액세스할 수 있습니다. 

그러나 프레임워크는 새로 생성된 스레드를 처리하지 않습니다(예: 비동기 메서드의 경우). 그리고 이 상황에서 시큐리티 컨텍스트 관리를 위해 다른 모드를 명시적으로 설정해야 한다는 것을 배웠습니다. 그러나 우리는 여전히 특이점이 있습니다. 프레임워크가 모르는 상태에서 코드가 새 스레드를 시작하면 어떻게 될까요? 때때로 프레임워크가 아니라 우리가 관리하기 때문에 이러한 자체 관리 스레드의 이름을 지정합니다. 이 섹션에서는 새로 생성된 스레드에 시큐리티 컨텍스트를 전파하는 데 도움이 되는 몇 가지 유틸리티 도구를 적용합니다.

SecurityContextHolder의 특정 전략은 자체 관리 스레드에 대한 솔루션을 제공하지 않습니다. 이 경우 보안 컨텍스트 전파를 처리해야 합니다. 이에 대한 한 가지 솔루션은 DelegatingSecurityContextRunnable을 사용하여 별도의 스레드에서 실행하려는 작업을 장식하는 것입니다. 

DelegatingSecurityContextRunnable은 Runnable을 확장합니다. 예상되는 값이 없을 때 작업 실행 후에 사용할 수 있습니다. 반환 값이 있는 경우 DelegatingSecurityContextCallable<T>인 Callable<T> 대안을 사용할 수 있습니다. 두 클래스 모두 다른 Runnable 또는 Callable과 마찬가지로 비동기적으로 실행되는 작업을 나타냅니다. 또한 작업을 실행하는 스레드에 대한 현재 보안 컨텍스트를 복사해야 합니다. 그림 5.10에서 볼 수 있듯이 이러한 개체는 원래 작업을 장식하고 보안 컨텍스트를 새 스레드에 복사합니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F10_Spilca.png)

그림 5.10 DelegatingSecurityContextCallable은 Callable 객체의 데코레이터로 설계되었습니다. 이러한 개체를 빌드할 때 응용 프로그램이 비동기적으로 실행하는 호출 가능한 작업을 제공합니다. DelegatingSecurityContextCallable은 보안 컨텍스트에서 새 스레드로 세부 정보를 복사한 다음 작업을 실행합니다.

목록 5.11은 DelegatingSecurityContextCallable의 사용을 보여줍니다. Callable 개체를 선언하는 간단한 끝점 메서드를 정의하는 것으로 시작하겠습니다. 호출 가능 작업은 현재 보안 컨텍스트에서 사용자 이름을 반환합니다.

목록 5.11 호출 가능한 객체 정의 및 별도의 스레드에서 작업으로 실행
```java
@GetMapping("/ciao")
public String ciao() throws Exception {
  Callable<String> task = () -> {
     SecurityContext context = SecurityContextHolder.getContext();
     return context.getAuthentication().getName();
  };
        
  ...
}
```
We continue the example by submitting the task to an ExecutorService. The response of the execution is retrieved and returned as a response body by the endpoint.

Listing 5.12 Defining an ExecutorService and submitting the task
```java
@GetMapping("/ciao")
public String ciao() throws Exception {
  Callable<String> task = () -> {
      SecurityContext context = SecurityContextHolder.getContext();
      return context.getAuthentication().getName();
  };

  ExecutorService e = Executors.newCachedThreadPool();
  try {
     return "Ciao, " + e.submit(task).get() + "!";
  } finally {
     e.shutdown();
  }
}
```
응용 프로그램을 있는 그대로 실행하면 NullPointerException만 발생합니다. 호출 가능한 작업을 실행하기 위해 새로 생성된 스레드 내부에는 인증이 더 이상 존재하지 않으며 보안 컨텍스트가 비어 있습니다. 이 문제를 해결하기 위해 이 목록에서 제공하는 것처럼 새 스레드에 현재 컨텍스트를 제공하는 DelegatingSecurityContextCallable로 작업을 장식합니다.

목록 5.13 DelegatingSecurityContextCallable에 의해 데코레이팅된 작업 실행
```java
@GetMapping("/ciao")
public String ciao() throws Exception {
  Callable<String> task = () -> {
    SecurityContext context = SecurityContextHolder.getContext();
    return context.getAuthentication().getName();
  };

  ExecutorService e = Executors.newCachedThreadPool();
  try {
    var contextTask = new DelegatingSecurityContextCallable<>(task);
    return "Ciao, " + e.submit(contextTask).get() + "!";
  } finally {
    e.shutdown();
  }
}
```
Calling the endpoint now, you can observe that Spring propagated the security context to the thread in which the tasks execute:
```sh
curl -u user:2eb3f2e8-debd-420c-9680-48159b2ff905
➥ http://localhost:8080/ciao
The response body for this call is
Ciao, user!
```

### 5.2.5 DELEGATINGSECURITYCONTEXTEXECUTORSERVICE로 시큐리티 컨텍스트 전달

프레임워크에 알리지 않고 코드가 시작하는 스레드를 처리할 때 보안 컨텍스트에서 다음 스레드로의 세부 정보 전파를 관리해야 합니다. 섹션 5.2.4에서 작업 자체를 사용하여 보안 컨텍스트에서 세부 정보를 복사하는 기술을 적용했습니다. Spring Security는 DelegatingSecurityContextRunnable 및 DelegatingSecurityContextCallable과 같은 훌륭한 유틸리티 클래스를 제공합니다. 이러한 클래스는 비동기적으로 실행하는 작업을 장식하고 구현이 새로 생성된 스레드의 작업에 액세스할 수 있도록 보안 컨텍스트에서 세부 정보를 복사하는 책임도 집니다. 그러나 새 스레드로의 시큐리티 컨텍스트 전파를 처리하는 두 번째 옵션이 있으며 이는 작업 자체가 아닌 스레드 풀에서 전파를 관리하는 것입니다. 이 섹션에서는 Spring Security에서 제공하는 더 뛰어난 유틸리티 클래스를 사용하여 이 기술을 적용하는 방법을 배웁니다.

태스크 데코레이팅의 대안은 특정 유형의 Executor를 사용하는 것입니다. 다음 예제에서는 작업이 단순한 Callable<T>로 남아 있지만 스레드가 여전히 보안 컨텍스트를 관리하는 것을 관찰할 수 있습니다. DelegatingSecurityContextExecutorService라는 구현이 ExecutorService를 장식하기 때문에 시큐리티 컨텍스트의 전파가 발생합니다. DelegatingSecurityContext-ExecutorService는 그림 5.11과 같이 보안 컨텍스트 전파도 처리합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F11_Spilca.png)

그림 5.11 DelegatingSecurityContextExecutorService는 작업을 제출하기 전에 ExecutorService를 장식하고 보안 컨텍스트 세부 정보를 다음 스레드로 전파합니다.

목록 5.14의 코드는 DelegatingSecurityContext-ExecutorService를 사용하여 작업을 제출할 때 보안 컨텍스트의 세부 사항을 전파하도록 주의하도록 ExecutorService를 장식하는 방법을 보여줍니다.

Listing 5.14 Propagating the SecurityContext
```java
@GetMapping("/hola")
public String hola() throws Exception {
  Callable<String> task = () -> {
    SecurityContext context = SecurityContextHolder.getContext();
    return context.getAuthentication().getName();
  };

  ExecutorService e = Executors.newCachedThreadPool();
  e = new DelegatingSecurityContextExecutorService(e);
  try {
    return "Hola, " + e.submit(task).get() + "!";
  } finally {
    e.shutdown();
  }
}
```
Call the endpoint to test that the DelegatingSecurityContextExecutorService correctly delegated the security context:
```sh
curl -u user:5a5124cc-060d-40b1-8aad-753d3da28dca http://localhost:8080/hola
```
The response body for this call is
```
Hola, user!
```
> 참고 보안 컨텍스트에 대한 동시성 지원과 관련된 클래스 중 표 5.1에 나와 있는 클래스를 알고 있는 것이 좋습니다.

Spring은 자신의 스레드를 생성할 때 보안 컨텍스트를 관리하기 위해 애플리케이션에서 사용할 수 있는 유틸리티 클래스의 다양한 구현을 제공합니다. 섹션 5.2.4에서 DelegatingSecurityContextCallable을 구현했습니다. 이 섹션에서는 DelegatingSecurityContextExecutorService를 사용합니다. 예약된 작업에 대한 보안 컨텍스트 전파를 구현해야 하는 경우 Spring Security가 DelegatingSecurityContextScheduledExecutorService라는 데코레이터도 제공한다는 소식을 듣게 되어 기쁩니다. 이 메커니즘은 이 섹션에서 제시한 DelegatingSecurityContextExecutorService와 유사하지만 ScheduledExecutorService를 장식하여 예약된 작업으로 작업할 수 있다는 차이점이 있습니다.

또한 유연성을 높이기 위해 Spring Security는 DelegatingSecurityContextExecutor라는 데코레이터의 보다 추상적인 버전을 제공합니다. 이 클래스는 스레드 풀 계층 구조의 가장 추상적인 계약인 Executor를 직접 장식합니다. 스레드 풀의 구현을 언어가 제공하는 선택 사항으로 대체할 수 있기를 원할 때 응용 프로그램 디자인을 위해 선택할 수 있습니다.

표 5.1 보안 컨텍스트를 별도의 스레드에 위임하는 객체 클래스 설명

- DelegatingSecurity-ContextExecutor Executor 인터페이스를 구현하고 풀에서 생성된 스레드에 보안 컨텍스트를 전달할 수 있는 기능으로 Executor 개체를 장식하도록 설계되었습니다.

- DelegatingSecurityContext-ExecutorService ExecutorService 인터페이스를 구현하고 풀에 의해 생성된 스레드에 보안 컨텍스트를 전달하는 기능으로 ExecutorService 개체를 장식하도록 설계되었습니다.

- DelegatingSecurityContext-ScheduledExecutorService ScheduledExecutorService 인터페이스를 구현하고 해당 풀에서 생성된 스레드에 보안 컨텍스트를 전달하는 기능으로 ScheduledExecutorService 개체를 장식하도록 설계되었습니다.

- DelegatingSecurityContext-Runnable Runnable 인터페이스를 구현하고 응답을 반환하지 않고 다른 스레드에서 실행되는 작업을 나타냅니다. 일반 Runnable 위에는 새 스레드에서 사용할 보안 컨텍스트를 전파할 수도 있습니다.

- DelegatingSecurityContext-Callable Callable 인터페이스를 구현하고 다른 스레드에서 실행되고 결국 응답을 반환하는 작업을 나타냅니다. 일반 Callable 위에 새 스레드에서 사용할 보안 컨텍스트를 전파할 수도 있습니다.

## 5.3 HTTP 기본 및 폼 기반 로그인 인증 이해

지금까지는 HTTP Basic만 인증 방법으로 사용했지만 이 책을 통해 다른 가능성도 있다는 것을 알게 될 것입니다. HTTP 기본 인증 방법은 간단하므로 예제 및 데모 목적 또는 개념 증명에 좋지만 실제 시나리오에 맞지 않을 수 있습니다.

이 섹션에서는 HTTP 기본과 관련된 구성에 대해 자세히 알아봅니다. 또한 formLogin이라는 새로운 인증 방법을 발견했습니다. 이 책의 나머지 부분에서는 다른 종류의 아키텍처와 잘 어울리는 다른 인증 방법에 대해 논의할 것입니다. 모범 사례와 인증 방지 패턴을 이해할 수 있도록 이를 비교합니다.

### 5.3.1 HTTP 기본 사용 및 구성

HTTP Basic이 기본 인증 방법이라는 것을 알고 있으며 3장의 다양한 예제에서 작동 방식을 관찰했습니다. 이 섹션에서는 이 인증 방법의 구성에 대한 자세한 내용을 추가합니다.

이론적 시나리오의 경우 HTTP 기본 인증과 함께 제공되는 기본값이 좋습니다. 그러나 더 복잡한 응용 프로그램에서는 이러한 설정 중 일부를 사용자 지정해야 할 수도 있습니다. 예를 들어 인증 프로세스가 실패한 경우에 대해 특정 논리를 구현하려고 할 수 있습니다. 이 경우 클라이언트로 다시 전송된 응답에 일부 값을 설정해야 할 수도 있습니다. 따라서 이를 구현하는 방법을 이해하기 위해 실제 사례와 함께 이러한 경우를 살펴보겠습니다. 다음 목록과 같이 이 메서드를 명시적으로 설정할 수 있는 방법을 다시 한 번 지적하고 싶습니다. 이 예제는 ssia-ch5-ex3 프로젝트에서 찾을 수 있습니다.

Listing 5.15 HTTP 기본 인증 방법 설정하기
```java
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) 
    throws Exception {
    http.httpBasic();
  }
}
```
또한 Customizer 유형의 매개 변수를 사용하여 HttpSecurity 인스턴스의 httpBasic() 메서드를 호출할 수도 있습니다. 이 매개변수를 사용하면 인증 방법과 관련된 일부 구성(예: 목록 5.16에 표시된 것처럼 영역 이름)을 설정할 수 있습니다. 영역을 특정 인증 방법을 사용하는 보호 공간으로 생각할 수 있습니다. 전체 설명은 https://tools.ietf.org/html/rfc2617에서 RFC 2617을 참조하십시오.

목록 5.16 인증 실패에 대한 응답을 위한 영역 이름 구성
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
  http.httpBasic(c -> {
    c.realmName("OTHER");
  });

  http.authorizeRequests().anyRequest().authenticated();
}
```
목록 5.16은 영역 이름을 변경하는 예를 보여줍니다. 사용된 람다 식은 실제로 Customizer<HttpBasicConfigurer-<HttpSecurity>> 유형의 개체입니다. HttpBasicConfigurer<HttpSecurity> 유형의 매개변수를 사용하면 realmName() 메서드를 호출하여 영역 이름을 변경할 수 있습니다. -v 플래그와 함께 cURL을 사용하여 영역 이름이 실제로 변경된 자세한 HTTP 응답을 얻을 수 있습니다. 그러나 HTTP 응답 상태가 401 Unauthorized일 때만 응답에서 WWW-Authenticate 헤더를 찾을 수 있으며 HTTP 응답 상태가 200 OK일 때는 그렇지 않습니다. 다음은 cURL에 대한 호출입니다.

```sh
curl -v http://localhost:8080/hello
```
The response of the call is
```
/
...
< WWW-Authenticate: Basic realm="OTHER"
...
```
또한 Customizer를 사용하여 실패한 인증에 대한 응답을 사용자 지정할 수 있습니다. 시스템의 클라이언트가 인증 실패의 경우 응답에서 특정 항목을 기대하는 경우 이 작업을 수행해야 합니다. 하나 이상의 헤더를 추가하거나 제거해야 할 수 있습니다. 또는 애플리케이션이 민감한 데이터를 클라이언트에 노출하지 않도록 본문을 필터링하는 로직을 가질 수 있습니다.

> **참고** 시스템 외부에 노출되는 데이터에 대해 항상 주의하십시오. OWASP 상위 10개 취약점의 일부인 가장 일반적인 실수 중 하나는 민감한 데이터를 노출하는 것입니다. 인증 실패를 위해 응용 프로그램이 클라이언트에 보내는 세부 정보로 작업하면 항상 기밀 정보가 노출될 위험이 있습니다.

실패한 인증에 대한 응답을 사용자 지정하기 위해 AuthenticationEntryPoint를 구현할 수 있습니다. 그것의 begin() 메소드는 인증 실패의 원인이 되는 HttpServlet-Request, HttpServletResponse 및 AuthenticationException을 수신합니다. 목록 5.17은 응답에 헤더를 추가하고 HTTP 상태를 401 Unauthorized로 설정하는 AuthenticationEntryPoint를 구현하는 방법을 보여줍니다.

> **참고** AuthenticationEntryPoint 인터페이스의 이름이 인증 실패 시 사용법을 반영하지 않는다는 점이 약간 모호합니다. Spring Security 아키텍처에서 이것은 ExceptionTranslationManager라는 컴포넌트에 의해 직접 사용되며, 필터 체인 내에서 발생한 모든 AccessDeniedException 및 AuthenticationException을 처리합니다. ExceptionTranslationManager를 Java 예외와 HTTP 응답 간의 브리지로 볼 수 있습니다.

목록 5.17 AuthenticationEntryPoint 구현하기
```java
public class CustomEntryPoint 
  implements AuthenticationEntryPoint {

  @Override
  public void commence(
    HttpServletRequest httpServletRequest, 
    HttpServletResponse httpServletResponse, 
    AuthenticationException e) 
      throws IOException, ServletException {

      httpServletResponse
        .addHeader("message", "Luke, I am your father!");
      httpServletResponse
        .sendError(HttpStatus.UNAUTHORIZED.value());

    }
}
```
그런 다음 구성 클래스의 HTTP 기본 메서드를 사용하여 CustomEntryPoint를 등록할 수 있습니다. 다음 목록은 사용자 정의 진입점에 대한 구성 클래스를 나타냅니다.

목록 5.18 사용자 정의 AuthenticationEntryPoint 설정하기
```java
@Override
protected void configure(HttpSecurity http) 
  throws Exception {

  http.httpBasic(c -> {
     c.realmName("OTHER");
     c.authenticationEntryPoint(new CustomEntryPoint());
  });
    
  http.authorizeRequests()
       .anyRequest()
        .authenticated();
}
```
If you now make a call to an endpoint such that the authentication fails, you should find in the response the newly added header:
```sh
curl -v http://localhost:8080/hello
```
The response of the call is
```sh
...
< HTTP/1.1 401
< Set-Cookie: JSESSIONID=459BAFA7E0E6246A463AD19B07569C7B; Path=/; HttpOnly
< message: Luke, I am your father!
...
```

### 5.3.2 양식 기반 로그인으로 인증 구현

웹 애플리케이션을 개발할 때 사용자가 자격 증명을 입력할 수 있는 사용자 친화적인 로그인 양식을 제시하고 싶을 것입니다. 또한 인증된 사용자가 로그인한 후 웹 페이지를 탐색하고 로그아웃할 수 있기를 원할 수 있습니다. 소규모 웹 응용 프로그램의 경우 양식 기반 로그인 방법을 활용할 수 있습니다. 이 섹션에서는 애플리케이션에 이 인증 방법을 적용하고 구성하는 방법을 배웁니다. 이를 달성하기 위해 양식 기반 로그인을 사용하는 작은 웹 애플리케이션을 작성합니다. 그림 5.12는 우리가 구현할 흐름을 설명합니다. 이 섹션의 예제는 ssia-ch5-ex4 프로젝트의 일부입니다.

> **참고** 이 방법을 작은 웹 응용 프로그램에 연결합니다. 이 방법으로 시큐리티 컨텍스트를 관리하기 위해 서버 측 세션을 사용하기 때문입니다. 수평적 확장성이 필요한 대규모 응용 프로그램의 경우 시큐리티 컨텍스트를 관리하기 위해 서버 측 세션을 사용하는 것은 바람직하지 않습니다. 이러한 측면은 OAuth 2를 다룰 때 12~15장에서 더 자세히 논의할 것입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F12_Spilca.png)

그림 5.12 폼 기반 로그인 사용. 인증되지 않은 사용자는 자격 증명을 사용하여 인증할 수 있는 양식으로 리디렉션됩니다. 애플리케이션이 인증하면 애플리케이션의 홈페이지로 리디렉션됩니다.

인증 방식을 폼 기반 로그인으로 변경하려면 구성 클래스의 configure(HttpSecurity http) 메서드에서 httpBasic() 대신 HttpSecurity 파라미터의 formLogin() 메서드를 호출하면 된다. 다음 목록은 이 변경 사항을 나타냅니다.

목록 5.19 인증 방법을 양식 기반 로그인으로 변경하기
```java
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) 
    throws Exception {
    http.formLogin();
    http.authorizeRequests().anyRequest().authenticated();
  }
}
```
이 최소한의 구성으로도 Spring Security는 이미 로그인 양식과 프로젝트에 대한 로그아웃 페이지를 구성했습니다. 응용 프로그램을 시작하고 브라우저로 응용 프로그램에 액세스하면 로그인 페이지로 리디렉션됩니다(그림 5.13).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F13_Spilca.png)

그림 5.13 formLogin() 메서드를 사용할 때 Spring Security가 자동으로 설정하는 기본 로그인 페이지.

UserDetailsService를 등록하지 않는 한 기본 제공된 자격 증명을 사용하여 로그인할 수 있습니다. 2장에서 배운 것처럼 사용자 이름 "user"와 응용 프로그램이 시작될 때 콘솔에 인쇄되는 UUID 암호입니다. 로그인에 성공하면 정의된 다른 페이지가 없기 때문에 기본 오류 페이지로 리디렉션됩니다. 애플리케이션은 이전 예에서 발생한 인증을 위해 동일한 아키텍처에 의존합니다. 따라서 그림 5.14와 같이 응용 프로그램의 홈페이지에 대한 컨트롤러를 구현해야 합니다. 차이점은 간단한 JSON 형식의 응답을 갖는 대신 엔드포인트가 브라우저에서 웹 페이지로 해석할 수 있는 HTML을 반환하기를 원한다는 것입니다. 이 때문에 우리는 Spring MVC 흐름을 고수하고 컨트롤러에 정의된 작업을 실행한 후 파일에서 뷰를 렌더링하도록 선택합니다. 그림 5.14는 애플리케이션의 홈페이지를 렌더링하기 위한 Spring MVC 흐름을 보여준다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH05_F14_Spilca.png)

그림 5.14 Spring MVC 흐름의 간단한 표현. 이 경우 디스패처는 지정된 경로인 /home과 관련된 컨트롤러 작업을 찾습니다. 컨트롤러 작업을 실행한 후 뷰가 렌더링되고 응답이 클라이언트로 다시 전송됩니다.

애플리케이션에 간단한 페이지를 추가하려면 먼저 프로젝트의 resources/static 폴더에 HTML 파일을 생성해야 합니다. 이 파일을 home.html이라고 부릅니다. 그 안에 나중에 브라우저에서 찾을 수 있는 텍스트를 입력하십시오. 제목만 추가할 수 있습니다(예: <h1>환영합니다</h1>). HTML 페이지를 생성한 후 컨트롤러는 경로에서 보기로의 매핑을 정의해야 합니다. 다음 목록은 컨트롤러 클래스의 home.html 페이지에 대한 작업 메서드의 정의를 나타냅니다.

Listing 5.20 home.html 페이지에 대한 컨트롤러의 액션 메소드 정의하기
```java
@Controller
public class HelloController {

  @GetMapping("/home")
  public String home() {
    return "home.html";
  }
}
```
@RestController가 아니라 간단한 @Controller라는 점을 염두에 두십시오. 이 때문에 Spring은 HTTP 응답에서 메소드가 반환한 값을 보내지 않습니다. 대신 home.html이라는 이름의 뷰를 찾아서 렌더링합니다.

지금 /home 경로에 액세스하려고 하면 먼저 로그인할지 묻는 메시지가 표시됩니다. 로그인에 성공하면 환영 메시지가 표시되는 홈페이지로 리디렉션됩니다. 이제 /logout 경로에 액세스할 수 있으며 로그아웃 페이지로 리디렉션됩니다(그림 5.15).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH15_F11_Spilca.png)

그림 5.15 스프링 시큐리티가 폼 기반 로그인 인증 방식으로 설정한 로그아웃 페이지.
로그인하지 않고 경로에 접근을 시도하면 사용자는 자동으로 로그인 페이지로 리디렉션됩니다. 로그인에 성공하면 애플리케이션은 사용자를 원래 액세스하려고 시도한 경로로 다시 리디렉션합니다. 해당 경로가 없으면 애플리케이션은 기본 오류 페이지를 표시합니다. formLogin() 메서드는 사용자 지정 작업을 수행할 수 있는 FormLoginConfigurer<HttpSecurity> 유형의 개체를 반환합니다. 예를 들어 다음 목록과 같이 defaultSuccessUrl() 메서드를 호출하여 이 작업을 수행할 수 있습니다.

Listing 5.21 Setting a default success URL for the login form
```java
@Override
protected void configure(HttpSecurity http) 
  throws Exception {
    http.formLogin()
        .defaultSuccessUrl("/home", true);

    http.authorizeRequests()
        .anyRequest().authenticated();
}
```
이것에 대해 더 깊이 들어가야 하는 경우 AuthenticationSuccessHandler 및 AuthenticationFailureHandler 개체를 사용하면 더 자세한 사용자 지정 접근 방식을 제공합니다. 이러한 인터페이스를 사용하면 인증을 위해 실행된 논리를 적용할 수 있는 개체를 구현할 수 있습니다. 성공적인 인증을 위한 논리를 사용자 지정하려면 AuthenticationSuccessHandler를 정의할 수 있습니다. 

onAuthenticationSuccess() 메소드는 서블릿 요청, 서블릿 응답 및 인증 객체를 매개변수로 수신합니다. 목록 5.22에서 로그인한 사용자에게 부여된 권한에 따라 다른 리디렉션을 만들기 위해 onAuthenticationSuccess() 메서드를 구현하는 예를 찾을 수 있습니다.

Listing 5.22 Implementing an AuthenticationSuccessHandler
```java
@Component
public class CustomAuthenticationSuccessHandler 
  implements AuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(
    HttpServletRequest httpServletRequest, 
    HttpServletResponse httpServletResponse, 
    Authentication authentication) 
      throws IOException {
        
      var authorities = authentication.getAuthorities();

      var auth = authorities.stream()
                .filter(a -> a.getAuthority().equals("read"))
                .findFirst(); ❶

      if (auth.isPresent()) { ❷
        httpServletResponse.sendRedirect("/home");
      } else {
        httpServletResponse.sendRedirect("/error");
      }
   }
}
```
❶ "읽기" 권한이 없으면 빈 Optional 객체를 반환합니다.

❷ "읽기" 권한이 있는 경우 /home으로 리디렉션

실제 시나리오에서는 인증에 실패한 경우 클라이언트가 특정 형식의 응답을 기대하는 상황이 있습니다. 응답 본문에서 401 Unauthorized 또는 추가 정보와 다른 HTTP 상태 코드를 기대할 수 있습니다. 응용 프로그램에서 찾은 가장 일반적인 경우는 요청 식별자를 보내는 것입니다. 이 요청 식별자는 여러 시스템 간의 요청을 추적하는 데 사용되는 고유한 값을 가지며 인증 실패 시 애플리케이션이 응답 본문에 이를 보낼 수 있습니다. 또 다른 상황은 애플리케이션이 시스템 외부에 민감한 데이터를 노출하지 않도록 응답을 삭제하려는 경우입니다. 추가 조사를 위해 이벤트를 기록하여 인증 실패에 대한 사용자 정의 논리를 정의할 수 있습니다.

인증 실패 시 애플리케이션이 실행하는 논리를 사용자 지정하려면 AuthenticationFailureHandler 구현과 유사하게 이를 수행할 수 있습니다. 예를 들어, 실패한 인증에 대한 특정 헤더를 추가하려는 경우 목록 5.23에 표시된 것과 같은 작업을 수행할 수 있습니다. 물론 여기에도 어떤 논리라도 구현할 수 있습니다. AuthenticationFailureHandler의 경우 onAuthenticationFailure()는 요청, 응답 및 인증 객체를 수신합니다.

목록 5.23 AuthenticationFailureHandler 구현하기
```java
@Component
public class CustomAuthenticationFailureHandler 
  implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(
    HttpServletRequest httpServletRequest, 
    HttpServletResponse httpServletResponse, 
    AuthenticationException e)  {
     httpServletResponse
       .setHeader("failed", LocalDateTime.now().toString());
    }
}
```
두 객체를 사용하려면 formLogin() 메서드에서 반환된 FormLoginConfigurer 객체의 configure() 메서드에 등록해야 합니다. 다음 목록은 이를 수행하는 방법을 보여줍니다.

Listing 5.24 구성 클래스에 핸들러 객체 등록하기
```java
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  @Autowired
  private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

  @Autowired
  private CustomAuthenticationFailureHandler authenticationFailureHandler;

  @Override
  protected void configure(HttpSecurity http) 
    throws Exception {

    http.formLogin()
        .successHandler(authenticationSuccessHandler)
        .failureHandler(authenticationFailureHandler);

    http.authorizeRequests()
        .anyRequest().authenticated();
    }
}
```
현재로서는 적절한 사용자 이름과 암호로 HTTP Basic을 사용하여 /home 경로에 액세스하려고 하면 HTTP 302 Found 상태의 응답이 반환됩니다. 이 응답 상태 코드는 애플리케이션이 리디렉션을 시도하고 있음을 알려주는 방식입니다. 올바른 사용자 이름과 비밀번호를 제공한 경우에도 이를 고려하지 않고 대신 formLogin 메소드에서 요청한 로그인 양식으로 보내려고 시도합니다. 그러나 다음 목록과 같이 HTTP 기본 및 양식 기반 로그인 방법을 모두 지원하도록 구성을 변경할 수 있습니다.

목록 5.25 양식 기반 로그인과 HTTP 기본을 함께 사용하기
```java
@Override
protected void configure(HttpSecurity http) 
  throws Exception {

  http.formLogin()
      .successHandler(authenticationSuccessHandler)
      .failureHandler(authenticationFailureHandler)
  .and()
      .httpBasic();

  http.authorizeRequests()
      .anyRequest().authenticated();
}
```

/home 경로에 액세스하면 이제 양식 기반 로그인 및 HTTP 기본 인증 방법 모두에서 작동합니다. 
```sh
curl -u user:cdd430f6-8ebc-49a6-9769-b0f3ce571d19 
➥ http://localhost:8080/home
```
The response of the call is
```
<h1>Welcome</h1>
```

## 요약

- AuthenticationProvider는 사용자 정의 인증 논리를 구현할 수 있는 구성 요소입니다.
- 사용자 지정 인증 논리를 구현할 때 책임을 분리된 상태로 유지하는 것이 좋습니다. 사용자 관리를 위해 인증 제공자는 UserDetailsService에 위임하고, 비밀번호 유효성 검사를 위해 AuthenticationProvider는 PasswordEncoder에 위임합니다.
- SecurityContext는 인증 성공 후 인증된 엔터티에 대한 세부 정보를 유지합니다.
- MODE _THREADLOCAL, MODE_INHERITABLETHREADLOCAL 및 MODE_GLOBAL의 세 가지 전략을 사용하여 보안 컨텍스트를 관리할 수 있습니다. 다른 스레드에서 보안 컨텍스트 세부 정보로의 액세스는 선택한 모드에 따라 다르게 작동합니다.
- 공유 스레드 로컬 모드를 사용할 때 Spring에서 관리하는 스레드에만 적용된다는 점을 기억하십시오. 프레임워크는 관리되지 않는 스레드에 대한 보안 컨텍스트를 복사하지 않습니다.
- Spring Security는 현재 프레임워크가 인식하고 있는 코드에 의해 생성된 스레드를 관리하기 위한 훌륭한 유틸리티 클래스를 제공합니다. 생성한 스레드에 대한 SecurityContext를 관리하려면 다음을 사용할 수 있습니다.
- DelegatingSecurityContextRunnable
- DelegatingSecurityContextCallable
- SecurityContextExecutor 위임
- Spring Security는 로그인을 위한 폼과 폼 기반 로그인 인증 방식인 formLogin()으로 로그아웃하는 옵션을 자동으로 설정한다. 작은 웹 애플리케이션을 개발할 때 사용하기 쉽습니다.
- formLogin 인증 방법은 사용자 정의가 가능합니다. 또한 이러한 유형의 인증을 HTTP 기본 방법과 함께 사용할 수 있습니다.
