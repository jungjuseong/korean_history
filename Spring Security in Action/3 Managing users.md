# 2부. 구현

1부에서는 보안의 중요성과 Spring Security를 ​​종속성으로 사용하여 Spring Boot 프로젝트를 생성하는 방법에 대해 논의했습니다. 또한 인증을 위한 필수 구성 요소를 살펴보았습니다. 이제 시작점이 있습니다.

2부가 이 책의 대부분을 차지합니다. 이 부분에서는 애플리케이션 개발에서 Spring Security를 ​​사용하는 방법에 대해 알아보겠습니다. 각 Spring Security 구성 요소에 대해 자세히 설명하고 실제 앱을 개발할 때 알아야 할 다양한 접근 방식에 대해 논의합니다. 2부에서는 많은 예제 프로젝트와 두 가지 실습을 통해 Spring Security를 ​​사용하여 앱에서 보안 기능을 개발하는 데 필요한 모든 것을 찾을 수 있습니다. 기본부터 OAuth 2 사용에 이르기까지, 그리고 명령형 프로그래밍을 사용하는 앱 보안에서 반응형 애플리케이션에 보안 적용에 이르기까지 다양한 주제에 대한 지식 경로를 안내해 드리겠습니다. 그리고 우리가 논의하는 내용이 Spring Security 사용 경험에서 배운 교훈을 잘 활용하도록 할 것입니다.

3장과 4장에서는 사용자 관리를 사용자 정의하고 암호를 처리하는 방법을 배웁니다. 대부분의 경우 응용 프로그램은 자격 증명을 사용하여 사용자를 인증합니다. 이러한 이유로 사용자 자격 증명 관리에 대해 논의하면 인증 및 권한 부여에 대해 더 자세히 논의할 수 있습니다. 5장에서 인증 로직을 사용자 정의하는 작업을 계속할 것입니다. 6장에서 11장에서 권한 부여와 관련된 구성 요소에 대해 설명합니다. 이 모든 장을 통해 사용자 세부 정보 관리자, 암호 인코더, 인증 공급자 및 필터와 같은 기본 요소를 처리하는 방법을 배웁니다. 이러한 구성 요소를 적용하는 방법을 알고 올바르게 이해하면 실제 시나리오에서 직면하게 될 보안 요구 사항을 해결할 수 있습니다.

오늘날 많은 앱, 특히 클라우드에 배포된 시스템은 OAuth 2 사양을 통해 인증 및 권한 부여를 구현합니다. 12~15장에서 Spring Security를 ​​사용하여 OAuth 2 앱에서 인증 및 권한 부여를 구현하는 방법을 배웁니다. 16장과 17장에서 메서드 수준에서 권한 부여 규칙을 적용하는 방법에 대해 설명합니다. 이 접근 방식을 사용하면 웹이 아닌 앱에서 Spring Security에 대해 배운 것을 사용할 수 있습니다. 또한 웹 앱에서 제한을 적용할 때 더 많은 유연성을 제공합니다. 19장에서는 반응형 앱에 Spring Security를 ​​적용하는 방법을 배웁니다. 그리고 테스트 없이는 개발 프로세스가 없기 때문에 20장에서 보안 구현을 위한 통합 테스트를 작성하는 방법을 배우게 됩니다.

2부 전체에서 당면한 주제를 다루기 위해 다른 방법을 사용할 챕터를 찾을 수 있습니다. 이 각 장에서 우리는 학습한 내용을 새로고침하는 데 도움이 되는 요구 사항에 대해 작업하고, 우리가 논의한 주제가 얼마나 더 잘 맞는지 이해하고, 새로운 것에 대한 응용 프로그램도 배울 것입니다. 저는 이것을 "실습" 장이라고 부릅니다.


# 3 사용자 관리

이 장에서는 다음을 다룹니다.

- UserDetails 인터페이스로 사용자 설명

- 인증 흐름에서 UserDetailsService 사용

- UserDetailsService의 사용자 정의 구현 만들기

- UserDetailsManager의 사용자 정의 구현 만들기

- 인증 흐름에서 JdbcUserDetailsManager 사용

대학 동료 중 한 명이 요리를 꽤 잘합니다. 화려한 레스토랑의 셰프는 아니지만 요리에 대한 열정은 대단하다. 어느 날, 토론에서 생각을 공유할 때 나는 그에게 어떻게 그 많은 요리법을 기억하는지 물었다. 그는 그것이 쉽다고 말했습니다. “레시피를 다 기억할 필요는 없지만 기본 재료가 어떻게 궁합이 맞는지 기억하세요. 혼합할 수 있는 것과 혼합하지 말아야 할 것을 알려주는 실제 계약과 같습니다. 그런 다음 각 레시피에 대해 몇 가지 트릭만 기억하게 됩니다.”

이 비유는 아키텍처가 작동하는 방식과 유사합니다. 모든 강력한 프레임워크에서 계약을 사용하여 프레임워크 구현을 프레임워크에 구축된 애플리케이션과 분리합니다. Java에서는 인터페이스를 사용하여 계약을 정의합니다. 프로그래머는 올바른 "구현"을 선택하기 위해 재료가 함께 "작동"하는 방식을 알고 있는 요리사와 비슷합니다. 프로그래머는 프레임워크의 추상화를 알고 이를 사용하여 통합합니다.

이 장은 2장에서 작업한 첫 번째 예인 UserDetailsService에서 만난 기본 역할 중 하나를 자세히 이해하는 것에 관한 것입니다. UserDetailsService와 함께 논의할 것입니다.

- `UserDetails`: Spring Security에 대한 사용자를 나타냄

- `GrantedAuthority`: 사용자가 실행할 수 있는 작업을 정의

- UserDetailsService 계약을 확장하는 `UserDetailsManager`. 상속된 동작 외에도 사용자 생성 및 사용자 암호 수정 또는 삭제와 같은 작업에 대해서도 설명합니다.
-
2장에서 이미 인증 프로세스에서 UserDetailsService 및 PasswordEncoder의 역할에 대한 아이디어를 얻었습니다. 그러나 우리는 Spring Boot에서 구성한 기본 인스턴스를 사용하는 대신 사용자가 정의한 인스턴스를 연결하는 방법에 대해서만 논의했습니다. 더 자세히 논의할 사항이 있습니다.

- Spring Security에서 제공하는 구현 및 사용 방법

- 계약에 대한 사용자 정의 구현을 정의하는 방법 및 수행 시기

- 실제 애플리케이션에서 볼 수 있는 인터페이스를 구현하는 방법

- 이러한 인터페이스를 사용하기 위한 모범 사례

Spring Security가 사용자 정의를 이해하는 방법으로 시작하는 것입니다. 이를 위해 UserDetails 및 GrantedAuthority 계약에 대해 논의합니다. 그런 다음 UserDetailsService와 UserDetailsManager가 이 계약을 확장하는 방법을 자세히 설명합니다. 이러한 인터페이스에 대한 구현을 적용합니다(예: InMemoryUserDetailsManager, JdbcUserDetailsManager 및 LdapUserDetailsManager). 이러한 구현이 시스템에 적합하지 않은 경우 사용자 정의 구현을 작성합니다.

## 3.1 스프링 시큐리티에서 인증 구현하기

이전 장에서 우리는 Spring Security를 ​​시작했습니다. 첫 번째 예에서 우리는 Spring Boot가 새 애플리케이션이 처음에 어떻게 작동하는지 정의하는 몇 가지 기본값을 정의하는 방법에 대해 논의했습니다. 또한 앱에서 자주 볼 수 있는 다양한 대안을 사용하여 이러한 기본값을 재정의하는 방법도 배웠습니다. 그러나 우리는 당신이 우리가 무엇을 할 것인지에 대한 아이디어를 제공하기 위해 이것들의 표면만을 고려했습니다. 이 장과 4장과 5장에서 우리는 이러한 인터페이스를 다양한 구현과 실제 응용 프로그램에서 찾을 수 있는 위치와 함께 더 자세히 논의할 것입니다.

그림 3.1은 Spring Security의 인증 흐름을 나타낸다. 이 아키텍처는 Spring Security에 의해 구현된 인증 프로세스의 백본입니다. Spring Security 구현에서 의존할 것이기 때문에 그것을 이해하는 것이 정말 중요합니다. 이 책의 거의 모든 장에서 이 아키텍처의 일부를 논의한다는 것을 알게 될 것입니다. 당신은 그것을 너무 자주 보게 될 것이고 아마도 당신은 그것을 마음으로 배울 것입니다. 그것은 좋은 것입니다. 이 구조를 알면 재료를 알고 어떤 레시피라도 만들 수 있는 요리사와 같습니다.

그림 3.1에서 음영 처리된 상자는 시작하는 구성 요소인 UserDetailsService와 PasswordEncoder를 나타냅니다. 이 두 구성 요소는 내가 종종 "사용자 관리 부분"이라고 부르는 흐름 부분에 중점을 둡니다. 이 장에서 UserDetailsService 및 PasswordEncoder는 사용자 세부 정보 및 자격 증명을 직접 처리하는 구성 요소입니다. 우리는 4장에서 PasswordEncoder에 대해 자세히 논의할 것입니다. 또한 이 책의 인증 흐름에서 사용자 정의할 수 있는 다른 구성 요소에 대해 자세히 설명하겠습니다. 5장에서는 AuthenticationProvider와 SecurityContext를, 9장에서는 , 필터.
 
그림 3.1 Spring Security의 인증 흐름. AuthenticationFilter는 요청을 가로채고 인증 책임을 AuthenticationManager에 위임합니다. 인증 논리를 구현하기 위해 AuthenticationManager는 인증 공급자를 사용합니다. 사용자 이름과 암호를 확인하기 위해 AuthenticationProvider는 UserDetailsService와 PasswordEncoder를 사용합니다.

사용자 관리의 일부로 UserDetailsService 및 UserDetailsManager 인터페이스를 사용합니다. UserDetailsService는 사용자 이름으로 사용자를 검색하는 역할만 합니다. 이 작업은 프레임워크에서 인증을 완료하는 데 필요한 유일한 작업입니다. UserDetailsManager는 사용자를 추가, 수정 또는 삭제하는 동작을 추가합니다. 이는 대부분의 애플리케이션에서 필수 기능입니다. 두 계약 간의 분리는 인터페이스 분리 원칙의 훌륭한 예입니다. 인터페이스를 분리하면 앱에 필요하지 않은 경우 프레임워크에서 강제로 동작을 구현하지 않기 때문에 유연성이 향상됩니다. 앱이 사용자를 인증하기만 하면 되는 경우 UserDetailsService 계약을 구현하면 원하는 기능을 충분히 다룰 수 있습니다. 사용자를 관리하려면 UserDetailsService 및 UserDetailsManager 구성 요소에 사용자를 나타내는 방법이 필요합니다.

Spring Security는 프레임워크가 이해하는 방식으로 사용자를 설명하기 위해 구현해야 하는 UserDetails 계약을 제공합니다. 이 장에서 배우게 될 Spring Security에서 사용자는 사용자가 수행할 수 있는 일련의 권한을 가지고 있습니다. 권한 부여에 대해 논의할 때 7장과 8장에서 이러한 권한으로 많은 작업을 할 것입니다. 그러나 현재로서는 Spring Security는 사용자가 GrantedAuthority 인터페이스로 수행할 수 있는 작업을 나타냅니다. 우리는 종종 이러한 권한을 호출하며 사용자에게는 하나 이상의 권한이 있습니다. 그림 3.2에서 인증 흐름의 사용자 관리 부분 구성 요소 간의 관계 표현을 찾을 수 있습니다.
 
그림 3.2 사용자 관리와 관련된 구성 요소 간의 종속성. UserDetailsService는 사용자의 세부 정보를 반환하고 이름으로 사용자를 찾습니다. UserDetails 계약은 사용자를 설명합니다. 사용자에게는 GrantedAuthority 인터페이스로 표시되는 하나 이상의 권한이 있습니다. 사용자에게 암호 생성, 삭제 또는 변경과 같은 작업을 추가하기 위해 UserDetailsManager 계약은 UserDetailsService를 확장하여 작업을 추가합니다.

Spring Security 아키텍처에서 이러한 객체 간의 링크와 이를 구현하는 방법을 이해하면 애플리케이션에서 작업할 때 선택할 수 있는 광범위한 옵션을 제공합니다. 이러한 옵션 중 하나는 작업 중인 앱에서 올바른 퍼즐 조각이 될 수 있으며 현명하게 선택해야 합니다. 그러나 선택할 수 있으려면 먼저 무엇을 선택할 수 있는지 알아야 합니다.

## 3.2 사용자 설명

Spring Security가 이해할 수 있도록 애플리케이션 사용자를 설명하는 방법을 배웁니다. 사용자를 표현하고 프레임워크가 사용자를 인식하도록 하는 방법을 배우는 것은 인증 흐름을 구축하는 데 필수적인 단계입니다. 사용자를 기반으로 애플리케이션은 특정 기능에 대한 호출이 허용되거나 허용되지 않는 결정을 내립니다. 사용자와 작업하려면 먼저 애플리케이션에서 사용자의 프로토타입을 정의하는 방법을 이해해야 합니다. 이 섹션에서는 Spring Security 애플리케이션에서 사용자를 위한 청사진을 설정하는 방법을 예제로 설명합니다.

Spring Security의 경우 사용자 정의는 UserDetails 계약을 준수해야 합니다. UserDetails 계약은 Spring Security가 이해하는 사용자를 나타냅니다. 사용자를 설명하는 응용 프로그램의 클래스는 이 인터페이스를 구현해야 하며 이러한 방식으로 프레임워크는 이를 이해합니다.

### 3.2.1 사용자 세부 정보 계약의 정의 이해하기

애플리케이션의 사용자를 설명하기 위해 UserDetails 인터페이스를 구현하는 방법을 배웁니다. 우리는 각각의 구현 방법과 이유를 이해하기 위해 UserDetails 계약에 의해 선언된 메서드에 대해 논의할 것입니다. 먼저 다음 목록에 표시된 인터페이스를 살펴보겠습니다.

목록 3.1 UserDetails 인터페이스
```java
public interface UserDetails extends Serializable {
    String getUsername(); ❶
    String getPassword();
    Collection<? extends GrantedAuthority> getAuthorities() ❷
    boolean isAccountNonExpired(); ❸
    boolean isAccountNonLocked();
    boolean isCredentialsNonExpired();
    boolean isEnabled();
}
```
❶ 이 메서드는 사용자 자격 증명을 반환합니다.

❷ 앱이 사용자에게 허용한 작업을 GrantedAuthority 인스턴스의 컬렉션으로 반환합니다.

❸ 이 네 가지 방법은 서로 다른 이유로 계정을 활성화하거나 비활성화합니다.

getUsername() 및 getPassword() 메서드는 예상대로 사용자 이름과 암호를 반환합니다. 앱은 인증 과정에서 이러한 값을 사용하며 본 계약의 인증과 관련된 유일한 세부 정보입니다. 다른 다섯 가지 방법은 모두 사용자가 응용 프로그램의 리소스에 액세스할 수 있도록 권한을 부여하는 것과 관련이 있습니다.

일반적으로 앱은 사용자가 애플리케이션 컨텍스트에서 의미 있는 몇 가지 작업을 수행할 수 있도록 허용해야 합니다. 예를 들어, 사용자는 데이터를 읽거나, 데이터를 쓰거나, 데이터를 삭제할 수 있어야 합니다. 사용자가 작업을 수행할 수 있는 권한이 있거나 없는 경우 권한은 사용자가 가진 권한을 나타냅니다. 사용자에게 부여된 권한 그룹을 반환하기 위해 getAuthorities() 메서드를 구현합니다.

> 참고 7장에서 배우겠지만 Spring Security는 권한을 사용하여 세분화된 권한 또는 권한 그룹인 역할을 참조합니다. 여러분의 읽기를 더 쉽게 하기 위해 이 책에서 나는 권한으로 세분화된 특권을 언급합니다.

또한 UserDetails 계약에서 볼 수 있듯이 사용자는

- 계정이 만료되도록 둡니다.

- 계정 잠금

- 자격 증명이 만료되도록 둡니다.

- 계정 비활성화

애플리케이션 로직에서 이러한 사용자 제한을 구현하기로 선택한 경우 isAccountNonExpired(), isAccountNonLocked(), isCredentialsNonExpired(), isEnabled() 메서드를 재정의해야 합니다. 활성화해야 하는 메서드는 true를 반환합니다. 모든 애플리케이션에 특정 조건으로 만료되거나 잠기는 계정이 있는 것은 아닙니다. 애플리케이션에서 이러한 기능을 구현할 필요가 없다면 이 네 가지 메서드가 true를 반환하도록 하면 됩니다.

참고 UserDetails 인터페이스의 마지막 4개 메서드 이름이 이상하게 들릴 수 있습니다. 깨끗한 코딩과 유지 관리 측면에서 이것들이 현명하게 선택되지 않았다고 주장할 수 있습니다. 예를 들어 isAccountNonExpired()라는 이름은 이중 부정처럼 보이며 처음에는 혼동을 일으킬 수 있습니다. 그러나 주의를 기울여 네 가지 메서드 이름을 모두 분석합니다. 권한 부여가 실패할 경우 모두 false를 반환하고 그렇지 않으면 true를 반환하도록 이름이 지정됩니다. 인간의 마음은 "거짓"이라는 단어를 부정과 연관시키고 "참"이라는 단어를 긍정적인 시나리오와 연관시키는 경향이 있기 때문에 이것이 올바른 접근 방식입니다.

### 3.2.2 권한 부여 계약에 대한 세부 정보

섹션 3.2.1의 UserDetails 인터페이스 정의에서 관찰했듯이 사용자에게 부여된 작업을 권한이라고 합니다. 7장과 8장에서는 이러한 사용자 권한을 기반으로 권한 구성을 작성합니다. 따라서 그것들을 정의하는 방법을 아는 것이 중요합니다.

권한은 사용자가 애플리케이션에서 수행할 수 있는 작업을 나타냅니다. 권한이 없으면 모든 사용자가 평등합니다. 사용자가 동일한 간단한 응용 프로그램이 있지만 대부분의 실제 시나리오에서 응용 프로그램은 여러 종류의 사용자를 정의합니다. 응용 프로그램에는 특정 정보만 읽을 수 있는 사용자가 있고 다른 사용자는 데이터를 수정할 수도 있습니다. 그리고 사용자가 필요로 하는 권한인 응용 프로그램의 기능 요구 사항에 따라 응용 프로그램을 구분해야 합니다. Spring Security에서 권한을 설명하기 위해 GrantedAuthority 인터페이스를 사용합니다.

UserDetails 구현에 대해 논의하기 전에 GrantedAuthority 인터페이스를 이해합시다. 우리는 사용자 세부 정보의 정의에서 이 인터페이스를 사용합니다. 사용자에게 부여된 권한을 나타냅니다. 사용자는 여러 권한을 가질 수 없으며 일반적으로 최소한 하나는 가지고 있습니다. 다음은 GrantedAuthority 정의의 구현입니다.

```java
public interface GrantedAuthority extends Serializable {
    String getAuthority();
}
```
권한을 생성하려면 나중에 권한 부여 규칙을 작성할 때 참조할 수 있도록 해당 권한에 대한 이름만 찾으면 됩니다. 예를 들어, 사용자는 애플리케이션에서 관리하는 레코드를 읽거나 삭제할 수 있습니다. 이러한 작업에 부여한 이름을 기반으로 권한 부여 규칙을 작성합니다. 7장과 8장에서는 사용자의 권한을 기반으로 하는 권한 부여 규칙을 작성하는 방법을 배웁니다.

이 장에서는 권한 이름을 문자열로 반환하는 getAuthority() 메서드를 구현합니다. GrantedAuthority 인터페이스에는 추상 메서드가 하나만 있으며 이 책에서는 구현에 람다 식을 사용하는 예를 자주 볼 수 있습니다. 또 다른 가능성은 SimpleGrantedAuthority 클래스를 사용하여 권한 인스턴스를 만드는 것입니다.

SimpleGrantedAuthority 클래스는 GrantedAuthority 유형의 변경할 수 없는 인스턴스를 만드는 방법을 제공합니다. 인스턴스를 빌드할 때 권한 이름을 제공합니다. 다음 코드 스니펫에서는 GrantedAuthority를 ​​구현하는 두 가지 예를 찾을 수 있습니다. 여기에서는 람다 식을 사용한 다음 SimpleGrantedAuthority 클래스를 사용합니다.

```java
GrantedAuthority g1 = () -> "read";
GrantedAuthority g2 = new SimpleGrantedAuthority("read");
```
> 참고 인터페이스를 람다 식으로 구현하기 전에 @FunctionalInterface 주석으로 인터페이스가 기능적인 것으로 표시되었는지 확인하는 것이 좋습니다. 이 방법을 사용하는 이유는 인터페이스가 기능으로 표시되지 않으면 개발자가 향후 버전에서 인터페이스에 추상 메서드를 더 추가할 수 있는 권한을 보유할 수 있음을 의미할 수 있기 때문입니다. Spring Security에서 GrantedAuthority 인터페이스는 기능적으로 표시되지 않습니다. 그러나 이 책에서 람다 식을 사용하여 코드를 더 짧고 읽기 쉽게 만들기 위해 해당 인터페이스를 구현할 것입니다. 실제 프로젝트에서는 권장하지 않는 작업일지라도 말입니다.

### 3.2.3 UserDetails 정보의 최소한의 구현 작성

UserDetails 계약의 첫 번째 구현을 작성합니다. 각 메서드가 정적 값을 반환하는 기본 구현으로 시작합니다. 그런 다음 실제 시나리오에서 찾을 가능성이 더 높은 버전과 여러 사용자 인스턴스를 가질 수 있는 버전으로 변경합니다. 이제 UserDetails 및 GrantedAuthority 인터페이스를 구현하는 방법을 알았으므로 애플리케이션에 대한 가장 간단한 사용자 정의를 작성할 수 있습니다.

DummyUser라는 클래스를 사용하여 목록 3.2에서와 같이 사용자에 대한 최소한의 설명을 구현해 보겠습니다. 저는 주로 이 클래스를 사용하여 UserDetails 계약에 대한 메서드 구현을 보여줍니다. 이 클래스의 인스턴스는 항상 암호 "12345"와 권한이 "READ"인 사용자 "bill" 하나만 참조합니다.

목록 3.2 DummyUser 클래스
```java
public class DummyUser implements UserDetails {

  @Override
  public String getUsername() {
    return "bill";
  }
    
  @Override
  public String getPassword() {
    return "12345";
  }

  // Omitted code
}
```
The class in the listing 3.2 implements the UserDetails interface and needs to implement all its methods. You find here the implementation of getUsername() and getPassword(). In this example, these methods only return a fixed value for each of the properties.

Next, we add a definition for the list of authorities. Listing 3.3 shows the implementation of the getAuthorities() method. This method returns a collection with only one implementation of the GrantedAuthority interface.

Listing 3.3 Implementation of the getAuthorities() method
```java
public class DummyUser implements UserDetails {

  // Omitted code

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(() -> "READ");
  }

  // Omitted code

}
```
Finally, you have to add an implementation for the last four methods of the UserDetails interface. For the DummyUser class, these always return true, which means that the user is forever active and usable. You find the examples in the following listing.

Listing 3.4 Implementation of the last four UserDetails interface methods
```java
public class DummyUser implements UserDetails {

  // Omitted code

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  // Omitted code

}
```
물론 이 최소한의 구현은 클래스의 모든 인스턴스가 동일한 사용자를 나타냄을 의미합니다. 실제 응용 프로그램의 경우 다른 사용자를 나타낼 수 있는 인스턴스를 생성하는 데 사용할 수 있는 클래스를 만들어야 합니다. 이 경우 다음 목록과 같이 정의에 최소한 사용자 이름과 비밀번호가 클래스의 속성으로 포함됩니다.

Listing 3.5 A more practical implementation of the UserDetails interface
```java
public class SimpleUser implements UserDetails {
    
  private final String username;
  private final String password;
    
  public SimpleUser(String username, String password) {
    this.username = username;
    this.password = password;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  @Override
  public String getPassword() {
    return this.password;
  }
    
  // Omitted code

}
```

### 3.2.4 USING A BUILDER TO CREATE INSTANCES OF THE USERDETAILS TYPE

일부 애플리케이션은 단순하며 UserDetails 인터페이스의 사용자 정의 구현이 필요하지 않습니다. 이 섹션에서는 Spring Security에서 제공하는 빌더 클래스를 사용하여 간단한 사용자 인스턴스를 만드는 방법을 살펴봅니다. 애플리케이션에서 클래스를 하나 더 선언하는 대신 사용자 빌더 클래스를 사용하여 사용자를 나타내는 인스턴스를 빠르게 얻을 수 있습니다.

org.springframework.security.core.userdetails 패키지의 User 클래스는 UserDetails 유형의 인스턴스를 빌드하는 간단한 방법입니다. 이 클래스를 사용하여 변경할 수 없는 UserDetails 인스턴스를 만들 수 있습니다. 최소한 사용자 이름과 암호를 제공해야 하며 사용자 이름은 빈 문자열이 아니어야 합니다. 목록 3.6은 이 빌더를 사용하는 방법을 보여줍니다. 이러한 방식으로 사용자를 구축하면 UserDetails 계약을 구현할 필요가 없습니다.

Listing 3.6 Constructing a user with the User builder class
`java
UserDetails u = User.withUsername("bill")
                .password("12345")
                .authorities("read", "write")
                .accountExpired(false)
                .disabled(true)
                .build();
```
이전 목록을 예로 들어 사용자 빌더 클래스의 구조를 더 자세히 살펴보겠습니다. User.withUsername(String username) 메서드는 User 클래스에 중첩된 빌더 클래스 UserBuilder의 인스턴스를 반환합니다. 빌더를 만드는 또 다른 방법은 UserDetails의 다른 인스턴스에서 시작하는 것입니다. 목록 3.7에서 첫 번째 줄은 문자열로 제공된 사용자 이름으로 시작하여 UserBuilder를 구성합니다. 그런 다음 이미 존재하는 UserDetails 인스턴스로 시작하는 빌더를 만드는 방법을 보여줍니다.

Listing 3.7 Creating the User.UserBuilder instance
```java
User.UserBuilder builder1 = User.withUsername("bill"); ❶

UserDetails u1 = builder1
                 .password("12345")
                 .authorities("read", "write")
                 .passwordEncoder(p -> encode(p)) ❷
                 .accountExpired(false)
                 .disabled(true)
                 .build(); ❸

User.UserBuilder builder2 = User.withUserDetails(u);    ❹

UserDetails u2 = builder2.build();
```
❶ 사용자 이름으로 사용자 구축

❷ 비밀번호 인코더는 인코딩만 하는 기능입니다.

❸ 빌드 파이프라인의 끝에서 build() 메서드를 호출합니다.

❹ 기존 UserDetails 인스턴스에서 사용자를 빌드할 수도 있습니다.

Listing 3.7에 정의된 모든 빌더를 사용하여 UserDetails 계약이 나타내는 사용자를 얻기 위해 빌더를 사용할 수 있음을 볼 수 있습니다. 빌드 파이프라인이 끝나면 build() 메서드를 호출합니다. 암호를 제공하는 경우 암호를 인코딩하도록 정의된 함수를 적용하고 UserDetails의 인스턴스를 구성하고 반환합니다.

> **참고** 암호 인코더는 2장에서 논의한 bean과 동일하지 않습니다. 이름이 혼동될 수 있지만 여기서는 <String, String> 함수만 있습니다. 이 함수의 유일한 책임은 주어진 인코딩으로 암호를 변환하는 것입니다. 다음 섹션에서는 2장에서 사용한 Spring Security의 PasswordEncoder 계약에 대해 자세히 설명합니다.

### 3.2.5 사용자와 관련된 여러 책임 결합

이전 섹션에서 UserDetails 인터페이스를 구현하는 방법을 배웠습니다. 실제 시나리오에서는 종종 더 복잡합니다. 대부분의 경우 사용자와 관련된 여러 책임을 찾습니다. 그리고 데이터베이스에 사용자를 저장한 다음 애플리케이션에 저장하는 경우 지속성 엔터티를 나타내는 클래스도 필요합니다. 또는 다른 시스템에서 웹 서비스를 통해 사용자를 검색하는 경우 사용자 인스턴스를 나타내는 데이터 전송 개체가 필요할 수 있습니다. 첫 번째는 단순하지만 일반적인 경우라고 가정하고 SQL 데이터베이스에 사용자를 저장하는 테이블이 있다고 가정해 보겠습니다. 예를 더 짧게 만들기 위해 각 사용자에게 하나의 권한만 부여합니다. 다음 목록은 테이블을 매핑하는 엔터티 클래스를 보여줍니다.

Listing 3.8 Defining the JPA User entity class

```java
@Entity
public class User {
    
  @Id
  private Long id;
  private String username;
  private String password;
  private String authority;
    
  // Omitted getters and setters

}
```
동일한 클래스에서 사용자 세부 정보에 대한 Spring Security 계약도 구현하도록 하면 클래스가 더 복잡해집니다. 다음 목록에서 코드가 어떻게 보입니까? 제 입장에서는 엉망입니다.

Listing 3.9 The User class has two responsibilities
```java
@Entity
public class User implements UserDetails {

  @Id
  private int id;
  private String username;
  private String password;
  private String authority;

  @Override
  public String getUsername() {
    return this.username;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  public String getAuthority() {
    return this.authority;
  }
    
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(() -> this.authority);
  }

  // Omitted code

}
```
이 클래스에는 JPA 주석, getter 및 setter가 포함되어 있으며 이 중 getUsername() 및 getPassword() 모두 UserDetails 계약의 메서드를 재정의합니다. String을 반환하는 getAuthority() 메서드와 Collection을 반환하는 getAuthorities() 메서드가 있습니다. getAuthority() 메서드는 클래스의 getter일 뿐이며 getAuthorities()는 UserDetails 인터페이스의 메서드를 구현합니다. 그리고 다른 엔터티에 관계를 추가하면 상황이 훨씬 더 복잡해집니다. 다시 말하지만, 이 코드는 전혀 친숙하지 않습니다!

이 코드를 더 깔끔하게 작성하려면 어떻게 해야 합니까? 이전 코드 예제의 모호한 측면의 근원은 두 가지 책임이 혼합되어 있다는 것입니다. 응용 프로그램에서 둘 다 필요한 것은 사실이지만, 이 경우 아무도 이것들을 같은 클래스에 넣어야 한다고 말하지 않습니다. User 클래스를 장식하는 SecurityUser라는 별도의 클래스를 정의하여 이들을 분리해 보겠습니다. 목록 3.10에서 볼 수 있듯이 SecurityUser 클래스는 UserDetails 계약을 구현하고 이를 사용하여 사용자를 Spring Security 아키텍처에 연결합니다. User 클래스에는 JPA 엔터티 책임만 남아 있습니다.

Listing 3.10 Implementing the User class only as a JPA entity
```java
@Entity
public class User {

  @Id
  private int id;
  private String username;
  private String password;
  private String authority;

  // Omitted getters and setters

}
```
목록 3.10의 User 클래스에는 JPA 엔터티 책임만 남아 있으므로 더 읽기 쉬워집니다. 이 코드를 읽으면 이제 Spring Security 관점에서 중요하지 않은 지속성과 관련된 세부 사항에만 집중할 수 있습니다. 다음 목록에서는 SecurityUser 클래스를 구현하여 User 엔터티를 래핑합니다.

목록 3.11 SecurityUser 클래스는 UserDetails 계약을 구현합니다.
```java
public class SecurityUser implements UserDetails {
    
  private final User user;
    
  public SecurityUser(User user) {
    this.user = user;
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(() -> user.getAuthority());
  }

  // Omitted code

}
```
보시다시피 SecurityUser 클래스는 시스템의 사용자 세부 정보를 Spring Security가 이해하는 UserDetails 계약에 매핑하기 위해서만 사용합니다. SecurityUser가 User 엔터티 없이는 의미가 없다는 사실을 표시하기 위해 필드를 final로 만듭니다. 생성자를 통해 사용자에게 제공해야 합니다. SecurityUser 클래스는 User 엔터티 클래스를 장식하고 JPA 엔터티에 코드를 혼합하지 않고 Spring Security 계약과 관련된 필요한 코드를 추가하여 여러 다른 작업을 구현합니다.

> 참고 두 가지 책임을 분리하는 다른 접근 방식을 찾을 수 있습니다. 이 섹션에서 제시하는 접근 방식이 최고이거나 유일한 방법이라고 말하고 싶지 않습니다. 일반적으로 클래스 디자인을 구현하기 위해 선택하는 방법은 경우에 따라 많이 다릅니다. 그러나 주요 아이디어는 동일합니다. 책임 혼합을 피하고 가능한 한 분리된 코드를 작성하여 앱의 유지 관리 가능성을 높이십시오.

## 3.3 Spring Security에 사용자 관리 방법 지시하기

이전 섹션에서 Spring Security가 사용자를 이해할 수 있도록 사용자를 설명하기 위해 UserDetails 계약을 구현했습니다. 그러나 Spring Security는 사용자를 어떻게 관리합니까? 자격 증명을 비교할 때 어디에서 가져오고 새 사용자를 추가하거나 기존 사용자를 변경합니까? 2장에서 프레임워크가 인증 프로세스가 사용자 관리를 위임하는 특정 구성 요소인 UserDetailsService 인스턴스를 정의한다는 것을 배웠습니다. Spring Boot에서 제공하는 기본 구현을 재정의하기 위해 UserDetailsService도 정의했습니다.

이 섹션에서는 UserDetailsService 클래스를 구현하는 다양한 방법을 실험합니다. 이 예에서 UserDetailsService 계약에 설명된 책임을 구현하여 사용자 관리가 작동하는 방식을 이해할 수 있습니다. 그런 다음 UserDetailsManager가 UserDetailsService에 의해 정의된 계약에 더 많은 동작을 추가하는 방법을 알게 될 것입니다. 이 섹션의 끝에서 UserDetailsManager의 제공된 구현을 사용할 것입니다. 

Spring Security에서 제공하는 가장 잘 알려진 구현 중 하나인 JdbcUserDetailsManager를 사용할 예제 프로젝트를 작성할 것입니다. 이것을 배우면 인증 흐름에서 필수적인 사용자를 찾을 위치를 Spring Security에 알리는 방법을 알게 될 것입니다.

## 3.3.1 UNDERSTANDING THE USERDETAILSSERVICE CONTRACT

이 섹션에서는 UserDetailsService 인터페이스 정의에 대해 알아봅니다. 구현 방법과 이유를 이해하기 전에 먼저 계약을 이해해야 합니다. 이제 UserDetailsService 및 이 구성 요소의 구현 작업 방법에 대해 자세히 설명할 때입니다. UserDetailsService 인터페이스에는 다음과 같이 하나의 메서드만 포함됩니다.

```java
public interface UserDetailsService {

  UserDetails loadUserByUsername(String username) 
      throws UsernameNotFoundException;
}
```
인증 구현은 loadUserByUsername(String username) 메서드를 호출하여 주어진 사용자 이름을 가진 사용자의 세부 정보를 얻습니다(그림 3.3). 물론 사용자 이름은 고유한 것으로 간주됩니다. 이 메서드에서 반환된 User는 UserDetails 계약의 구현입니다. username이 존재하지 않으면 메서드에서 UsernameNotFoundException이 발생합니다.
 
그림 3.3 AuthenticationProvider는 인증 논리를 구현하고 UserDetailsService를 사용하여 사용자에 대한 세부 정보를 로드하는 구성 요소입니다. 사용자 이름으로 사용자를 찾으려면 loadUserByUsername(String 사용자 이름) 메서드를 호출합니다.

> **참고** UsernameNotFoundException은 RuntimeException입니다. UserDetailsService 인터페이스의 throws 절은 문서화 목적으로만 사용됩니다. UsernameNotFoundException은 인증 프로세스와 관련된 모든 예외의 상위인 AuthenticationException 유형에서 직접 상속됩니다. AuthenticationException은 RuntimeException 클래스를 추가로 상속합니다.

### 3.3.2 IMPLEMENTING THE USERDETAILSSERVICE CONTRACT

이 섹션에서는 UserDetailsService의 구현을 보여주기 위한 실제 예제를 작업합니다. 애플리케이션은 자격 증명 및 기타 사용자 측면에 대한 세부 정보를 관리합니다. 이것들은 데이터베이스에 저장되거나 웹 서비스나 다른 수단을 통해 액세스하는 다른 시스템에 의해 처리될 수 있습니다(그림 3.3). 이것이 시스템에서 어떻게 발생하는지에 관계없이 Spring Security가 User에게 필요한 유일한 것은 username으로 사용자를 검색하는 구현입니다.

다음 예에서는 메모리 내 사용자 목록이 있는 UserDetailsService를 작성합니다. 2장에서 동일한 작업을 수행하는 제공된 구현인 InMemoryUserDetailsManager를 사용했습니다. 이 구현이 작동하는 방식에 이미 익숙하기 때문에 비슷한 기능을 선택했지만 이번에는 자체적으로 구현합니다. UserDetailsService 클래스의 인스턴스를 만들 때 사용자 목록을 제공합니다. 이 예제는 ssia-ch3-ex1 프로젝트에서 찾을 수 있습니다. model이라는 패키지에서 다음 목록과 같이 UserDetails를 정의합니다.

목록 3.12 UserDetails 인터페이스의 구현
```java
public class User implements UserDetails {

  private final String username; ❶
  private final String password;
  private final String authority; ❷

  public User(String username, String password, String authority) {
    this.username = username;
    this.password = password;
    this.authority = authority;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(() -> authority);  ❸
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() { ❹
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
```
❶ User 클래스는 변경할 수 없습니다. 인스턴스를 빌드할 때 세 가지 속성에 대한 값을 제공하고 이 값은 나중에 변경할 수 없습니다.

❷ 예시를 간단하게 하기 위해 사용자는 하나의 권한만 가지고 있습니다.

❸ 인스턴스를 빌드할 때 제공된 이름을 가진 GrantedAuthority 개체만 포함하는 목록을 반환합니다.

❹ 계정은 만료되거나 잠기지 않습니다.

services 패키지에서 InMemoryUserDetailsService라는 클래스를 만듭니다. 다음 목록은 이 클래스를 구현하는 방법을 보여줍니다.

Listing 3.13 The implementation of the UserDetailsService interface
```java
public class InMemoryUserDetailsService implements UserDetailsService {

  private final List<UserDetails> users; ❶

  public InMemoryUserDetailsService(List<UserDetails> users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) 
    throws UsernameNotFoundException {
    
    return users.stream()
      .filter( ❷
         u -> u.getUsername().equals(username)
      )    
      .findFirst() ❸
      .orElseThrow( ❹
        () -> new UsernameNotFoundException("User not found")
      );    
   }

}
```

❶ UserDetailsService는 메모리 내 사용자 목록을 관리합니다.

❷ 사용자 목록에서 요청한 사용자 이름이 있는 사용자를 필터링합니다.

❸ 해당 이용자가 있는 경우에는 반품

❹ 이 사용자 이름을 가진 사용자가 없으면 예외가 발생합니다.

loadUserByUsername(String username) 메서드는 주어진 사용자 이름에 대한 사용자 목록을 검색하고 원하는 UserDetails 인스턴스를 반환합니다. 해당 사용자 이름을 가진 인스턴스가 없으면 UsernameNotFoundException이 발생합니다. 이제 이 구현을 UserDetailsService로 사용할 수 있습니다. 다음 목록은 구성 클래스에 빈으로 추가하고 그 안에 한 명의 사용자를 등록하는 방법을 보여줍니다.

Listing 3.14 구성 클래스에 빈으로 등록된 UserDetailsService 
```java
@Configuration
public class ProjectConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails john = new User("john", "12345", "read");
    List<UserDetails> users = List.of(john);
    return new InMemoryUserDetailsService(users);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```
Finally, we create a simple endpoint and test the implementation. The following listing defines the endpoint.

Listing 3.15 The definition of the endpoint used for testing the implementation
```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
```
When calling the endpoint using cURL, we observe that for user John with password 12345, we get back an HTTP 200 OK. If we use something else, the application returns 401 Unauthorized.
```sh
curl -u john:12345 http://localhost:8080/hello
```
The response body is
```
Hello!
```

### 3.3.3 IMPLEMENTING THE USERDETAILSMANAGER CONTRACT

UserDetailsManager 인터페이스 사용 및 구현에 대해 설명합니다. 이 인터페이스는 UserDetailsService 계약에 더 많은 메서드를 확장하고 추가합니다. Spring Security는 인증을 수행하기 위해 UserDetailsService 계약이 필요합니다. 그러나 일반적으로 응용 프로그램에는 사용자 관리도 필요합니다. 대부분의 경우 앱은 새 사용자를 추가하거나 기존 사용자를 삭제할 수 있어야 합니다. 이 경우 Spring Security에서 정의한 보다 특별한 인터페이스인 UserDetailsManager를 구현합니다. UserDetailsService를 확장하고 구현해야 하는 작업을 더 추가합니다.

```java
public interface UserDetailsManager extends UserDetailsService {
  void createUser(UserDetails user);
  void updateUser(UserDetails user);
  void deleteUser(String username);
  void changePassword(String oldPassword, String newPassword);
  boolean userExists(String username);
}
```
2장에서 사용한 InMemoryUserDetailsManager 개체는 실제로 UserDetailsManager입니다. 그 당시에는 UserDetailsService 특성만 고려했지만 이제 인스턴스에서 createUser() 메서드를 호출할 수 있었던 이유를 더 잘 이해하게 되었습니다.

### 사용자 관리를 위해 JDBCUSERDETAILSMANAGER 사용

InMemoryUserDetailsManager 외에 다른 UserDetailManager인 JdbcUserDetailsManager를 자주 사용합니다. JdbcUserDetailsManager는 SQL 데이터베이스의 사용자를 관리합니다. JDBC를 통해 직접 데이터베이스에 연결합니다. 이런 식으로 JdbcUserDetailsManager는 데이터베이스 연결과 관련된 다른 프레임워크나 사양과 무관합니다.

JdbcUserDetailsManager가 어떻게 작동하는지 이해하려면 예제와 함께 실행하는 것이 가장 좋습니다. 다음 예제에서는 JdbcUserDetailsManager를 사용하여 MySQL 데이터베이스의 사용자를 관리하는 애플리케이션을 구현합니다. 그림 3.4는 JdbcUserDetailsManager 구현이 인증 흐름에서 차지하는 위치에 대한 개요를 제공합니다.
 
그림 3.4 Spring Security 인증 흐름. 여기서는 JDBCUserDetailsManager를 UserDetailsService 구성 요소로 사용합니다. JdbcUserDetailsManager는 데이터베이스를 사용하여 사용자를 관리합니다.

데이터베이스와 두 개의 테이블을 생성하여 JdbcUserDetailsManager를 사용하는 방법에 대한 데모 애플리케이션 작업을 시작합니다. 우리의 경우 데이터베이스 이름을 spring으로 지정하고 테이블 중 하나의 이름을 사용자와 다른 권한으로 지정합니다. 이러한 이름은 JdbcUserDetailsManager에 의해 알려진 기본 테이블 이름입니다. 이 섹션의 끝에서 배우게 될 JdbcUserDetailsManager 구현은 유연하며 원하는 경우 이러한 기본 이름을 재정의할 수 있습니다. users 테이블의 목적은 사용자 레코드를 유지하는 것입니다. JdbcUserDetails Manager 구현은 사용자 테이블에 세 개의 열(사용자 이름, 암호 및 활성화됨)을 예상하며 사용자를 비활성화하는 데 사용할 수 있습니다.

데이터베이스 관리 시스템(DBMS) 또는 클라이언트 응용 프로그램용 명령줄 도구를 사용하여 데이터베이스 및 해당 구조를 직접 만들도록 선택할 수 있습니다. 예를 들어 MySQL의 경우 MySQL Workbench를 사용하여 이를 수행할 수 있습니다. 그러나 가장 쉬운 방법은 Spring Boot 자체가 스크립트를 실행하도록 하는 것입니다. 이렇게 하려면 리소스 폴더의 프로젝트에 schema.sql과 data.sql이라는 두 개의 파일을 추가하기만 하면 됩니다. schema.sql 파일에서 테이블 생성, 변경 또는 삭제와 같은 데이터베이스 구조와 관련된 쿼리를 추가합니다. data.sql 파일에서 INSERT, UPDATE 또는 DELETE와 같이 테이블 내부의 데이터와 함께 작동하는 쿼리를 추가합니다. Spring Boot는 애플리케이션을 시작할 때 자동으로 이러한 파일을 실행합니다. 데이터베이스가 필요한 예제를 구축하기 위한 더 간단한 솔루션은 H2 인메모리 데이터베이스를 사용하는 것입니다. 이렇게 하면 별도의 DBMS 솔루션을 설치할 필요가 없습니다.

> **참고** 원하는 경우 이 책에서 제공하는 응용 프로그램을 개발할 때 H2를 사용할 수도 있습니다. 나는 그것이 시스템의 외부 구성 요소임을 분명히 하고 이러한 방식으로 혼동을 피하기 위해 외부 DBMS로 예제를 구현하기로 결정했습니다.

다음 목록의 코드를 사용하여 MySQL 서버가 있는 사용자 테이블을 생성합니다. 이 스크립트를 Spring Boot 프로젝트의 schema.sql 파일에 추가할 수 있습니다.

Listing 3.16 users 테이블을 생성하기 위한 SQL 쿼리
```sql
CREATE TABLE IF NOT EXISTS `spring`.`users` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(45) NOT NULL,
  `password` VARCHAR(45) NOT NULL,
  `enabled` INT NOT NULL,
  PRIMARY KEY (`id`));
```
The authorities table stores authorities per user. Each record stores a username and an authority granted for the user with that username.

Listing 3.17 The SQL query for creating the authorities table
```sql
CREATE TABLE IF NOT EXISTS `spring`.`authorities` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(45) NOT NULL,
  `authority` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`));
```
> NOTE For simplicity, in the examples provided with this book, I skip the definitions of indexes or foreign keys.

To make sure you have a user for testing, insert a record in each of the tables. You can add these queries in the data.sql file in the resources folder of the Spring Boot project:

```sql
INSERT IGNORE INTO `spring`.`authorities` VALUES (NULL, 'john', 'write');
INSERT IGNORE INTO `spring`.`users` VALUES (NULL, 'john', '12345', '1');
```
For your project, you need to add at least the dependencies stated in the following listing. Check your pom.xml file to make sure you added these dependencies.

Listing 3.18 Dependencies needed to develop the example project
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
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
   <groupId>mysql</groupId>
   <artifactId>mysql-connector-java</artifactId>
   <scope>runtime</scope>
</dependency>
```
> **참고** 예제에서 올바른 JDBC 드라이버를 종속성에 추가하는 한 모든 SQL 데이터베이스 기술을 사용할 수 있습니다.

프로젝트의 application.properties 파일에서 또는 별도의 빈으로 데이터 소스를 구성할 수 있습니다. application.properties 파일을 사용하도록 선택한 경우 해당 파일에 다음 행을 추가해야 합니다.

```yaml
spring.datasource.url=jdbc:mysql://localhost/spring
spring.datasource.username=<your user>
spring.datasource.password=<your password>
spring.datasource.initialization-mode=always
```
프로젝트의 구성 클래스에서 UserDetailsService 및 PasswordEncoder를 정의합니다. JdbcUserDetailsManager는 데이터베이스에 연결하기 위해 DataSource가 필요합니다. 데이터 소스는 메소드의 매개변수(다음 목록에 표시됨) 또는 클래스의 속성을 통해 자동 연결될 수 있습니다.

Listing 3.19 구성 클래스에 JdbcUserDetailsManager 등록하기
Configuration
```java
public class ProjectConfig {

  @Bean
  public UserDetailsService userDetailsService(DataSource dataSource) {
    return new JdbcUserDetailsManager(dataSource);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```
애플리케이션의 끝점에 액세스하려면 이제 데이터베이스에 저장된 사용자 중 한 명과 HTTP 기본 인증을 사용해야 합니다. 이를 증명하기 위해 다음 목록과 같이 새 끝점을 만든 다음 cURL로 호출합니다.

목록 3.20 구현을 확인하기 위한 테스트 엔드포인트
```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
```
In the next code snippet, you find the result when calling the endpoint with the correct username and password:

```sh
curl -u john:12345 http://localhost:8080/hello
```
The response to the call is
```
Hello!
```
JdbcUserDetailsManager를 사용하면 사용된 쿼리를 구성할 수도 있습니다. 이전 예에서 JdbcUserDetailsManager 구현이 예상하는 대로 테이블과 열에 정확한 이름을 사용했는지 확인했습니다. 그러나 응용 프로그램의 경우 이러한 이름이 최선의 선택이 아닐 수 있습니다. 다음 목록은 JdbcUserDetailsManager에 대한 쿼리를 재정의하는 방법을 보여줍니다.

Listing 3.21 JdbcUserDetailsManager의 쿼리를 변경하여 사용자 찾기
```java
@Bean
public UserDetailsService userDetailsService(DataSource dataSource) {
  String usersByUsernameQuery = 
     "select username, password, enabled from users where username = ?";
  String authsByUserQuery =
     "select username, authority from spring.authorities where username = ?";
      
      var userDetailsManager = new JdbcUserDetailsManager(dataSource);
      userDetailsManager.setUsersByUsernameQuery(usersByUsernameQuery);
      userDetailsManager.setAuthoritiesByUsernameQuery(authsByUserQuery);
      return userDetailsManager;
}
```
같은 방법으로 JdbcUserDetailsManager 구현에서 사용하는 모든 쿼리를 변경할 수 있습니다.

> **연습**: 데이터베이스에서 테이블과 열의 이름을 다르게 지정하는 유사한 응용 프로그램을 작성합니다. JdbcUserDetailsManager 구현에 대한 쿼리를 재정의합니다(예: 새 테이블 구조에서 인증이 작동함). 프로젝트 ssia-ch3-ex2는 가능한 솔루션을 제공합니다.


### 사용자 관리를 위해 LDAPUSERDETAILSMANAGER 사용

Spring Security는 또한 LDAP용 UserDetailsManager의 구현을 제공합니다. JdbcUserDetailsManager보다 덜 유명하더라도 사용자 관리를 위해 LDAP 시스템과 통합해야 하는 경우 신뢰할 수 있습니다. ssia-ch3-ex3 프로젝트에서 LdapUserDetailsManager를 사용하는 간단한 데모를 찾을 수 있습니다. 이 데모에서는 실제 LDAP 서버를 사용할 수 없기 때문에 내 Spring Boot 애플리케이션에 임베디드 서버를 설정했습니다. 내장형 LDAP 서버를 설정하기 위해 간단한 LDIF(LDAP Data Interchange Format) 파일을 정의했습니다. 다음 목록은 내 LDIF 파일의 내용을 보여줍니다.

Listing 3.22 The definition of the LDIF file
```yaml
dn: dc=springframework,dc=org ❶
objectclass: top
objectclass: domain
objectclass: extensibleObject
dc: springframework

dn: ou=groups,dc=springframework,dc=org ❷
objectclass: top
objectclass: organizationalUnit
ou: groups

dn: uid=john,ou=groups,dc=springframework,dc=org   ❸
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: John
sn: John
uid: john
userPassword: 12345
```
❶ 기본 엔터티 정의

❷ 그룹 엔터티 정의

❸ 사용자 정의

LDIF 파일에서 이 예제의 끝에서 앱의 동작을 테스트해야 하는 사용자를 한 명만 추가합니다. LDIF 파일을 리소스 폴더에 직접 추가할 수 있습니다. 이렇게 하면 클래스 경로에 자동으로 포함되므로 나중에 쉽게 참조할 수 있습니다. LDIF 파일 이름을 server.ldif로 지정했습니다. LDAP로 작업하고 Spring Boot가 임베디드 LDAP 서버를 시작하도록 허용하려면 다음 코드 스니펫에서와 같이 종속성에 pom.xml을 추가해야 합니다.

```xml
<dependency>
   <groupId>org.springframework.security</groupId>
   <artifactId>spring-security-ldap</artifactId>
</dependency>
<dependency>
   <groupId>com.unboundid</groupId>
   <artifactId>unboundid-ldapsdk</artifactId>
</dependency>
```
application.properties 파일에서 다음 코드 스니펫에 표시된 대로 임베디드 LDAP 서버에 대한 구성도 추가해야 합니다. 앱이 내장된 LDAP 서버를 부팅하는 데 필요한 값에는 LDIF 파일의 위치, LDAP 서버용 포트, 기본 DN(도메인 구성 요소) 레이블 값이 포함됩니다.

```yaml
spring.ldap.embedded.ldif=classpath:server.ldif
spring.ldap.embedded.base-dn=dc=springframework,dc=org
spring.ldap.embedded.port=33389
```

application.properties 파일에서 다음 코드에 표시된 대로 임베디드 LDAP 서버에 대한 구성도 추가해야 합니다. 앱이 내장된 LDAP 서버를 부팅하는 데 필요한 값에는 LDIF 파일의 위치, LDAP 서버용 포트, 기본 DN(도메인 구성 요소) 레이블 값이 포함됩니다. 인증을 위한 LDAP 서버가 있으면 다음을 구성할 수 있습니다. 그것을 사용하는 응용 프로그램. 다음 목록은 앱이 LDAP 서버를 통해 사용자를 인증할 수 있도록 LdapUserDetailsManager를 구성하는 방법을 보여줍니다.

Listing 3.23 구성 파일의 LdapUserDetailsManager 정의 
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Bean ❶
  public UserDetailsService userDetailsService() {
    var cs = new DefaultSpringSecurityContextSource( ❷
      "ldap://127.0.0.1:33389/dc=springframework,dc=org");
    cs.afterPropertiesSet();

    var manager = new LdapUserDetailsManager(cs); ❸

    manager.setUsernameMapper( ❹
      new DefaultLdapUsernameToDnMapper("ou=groups", "uid"));

    manager.setGroupSearchBase("ou=groups"); ❺
    
    return manager;    
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```
❶ Spring 컨텍스트에 UserDetailsService 구현 추가

❷ LDAP 서버의 주소를 지정하기 위한 컨텍스트 소스 생성

❸ LdapUserDetailsManager 인스턴스 생성

❹ 사용자 이름 매퍼를 설정하여 LdapUserDetailsManager에게 사용자 검색 방법을 지시합니다.

❺ 앱이 사용자를 검색하는 데 필요한 그룹 검색 기반을 설정합니다.

보안 구성을 테스트하기 위해 간단한 끝점도 만들어 보겠습니다. 다음 코드 에 표시된 대로 컨트롤러 클래스를 추가했습니다.

```java
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
```
이제 앱을 시작하고 /hello 엔드포인트를 호출합니다. 앱에서 엔드포인트를 호출할 수 있도록 하려면 사용자 John으로 인증해야 합니다. 다음 코드는 cURL을 사용하여 엔드포인트를 호출한 결과를 보여줍니다.
```sh
curl -u john:12345 http://localhost:8080/hello
```
The response to the call is
```
Hello!
```
## Summary

- UserDetails 인터페이스는 Spring Security에서 사용자를 설명하는 데 사용하는 계약입니다.

- UserDetailsService 인터페이스는 애플리케이션이 사용자 세부 정보를 얻는 방법을 설명하기 위해 인증 아키텍처에서 구현하기를 Spring Security에서 기대하는 계약입니다.

- UserDetailsManager 인터페이스는 UserDetailsService를 확장하고 사용자 생성, 변경 또는 삭제와 관련된 동작을 추가합니다.

- Spring Security는 UserDetailsManager 계약의 몇 가지 구현을 제공합니다. 이 중에는 InMemoryUserDetailsManager, JdbcUser-DetailsManager 및 LdapUserDetailsManager가 있습니다.

- JdbcUserDetailsManager는 JDBC를 직접 사용하는 장점이 있으며 다른 프레임워크에 응용 프로그램을 종속되게 하지 않습니다.
