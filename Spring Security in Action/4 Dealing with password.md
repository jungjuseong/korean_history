# 4 비밀번호 다루기

이 장에서는 다음을 다룹니다.

- PasswordEncoder 구현 및 작업
- Spring Security Crypto 모듈에서 제공하는 도구 사용

3장에서는 Spring Security로 구현된 애플리케이션에서 사용자 관리에 대해 논의했습니다. 그러나 암호는 어떻습니까? 그들은 확실히 인증 흐름에서 필수적인 부분입니다. 이 장에서는 Spring Security로 구현된 애플리케이션에서 암호와 비밀을 관리하는 방법을 배웁니다. PasswordEncoder 계약과 비밀번호 관리를 위해 SSCM(Spring Security Crypto module)에서 제공하는 도구에 대해 논의합니다.

# 4.1 PasswordEncoder 계약 이해

3장에서 이제 UserDetails 인터페이스가 무엇인지, 구현을 사용하는 다양한 방법에 대한 명확한 이미지를 갖게 되었습니다. 그러나 2장에서 배웠듯이 인증 및 권한 부여 프로세스 동안 다양한 행위자가 사용자 표현을 관리합니다. 또한 이들 중 일부에는 UserDetailsService 및 PasswordEncoder와 같은 기본값이 있다는 것도 배웠습니다. 이제 기본값을 재정의할 수 있다는 것을 알게 되었습니다. 우리는 이러한 빈과 이를 구현하는 방법에 대한 깊은 이해를 계속하므로 이 섹션에서는 PasswordEncoder를 분석합니다. 그림 4.1은 PasswordEncoder가 인증 프로세스에 적합한 위치를 보여줍니다.
 
그림 4.1 Spring Security 인증 프로세스. AuthenticationProvider는 PasswordEncoder를 사용하여 인증 프로세스에서 사용자의 암호를 확인합니다.

일반적으로 시스템은 암호를 일반 텍스트로 관리하지 않기 때문에 암호를 읽고 훔치기가 더 어려워지는 일종의 변형을 겪습니다. 이 책임을 위해 Spring Security는 별도의 계약을 정의합니다. 이 섹션에서 쉽게 설명하기 위해 PasswordEncoder 구현과 관련된 많은 코드 예제를 제공합니다. 계약을 이해하는 것으로 시작한 다음 프로젝트 내에서 구현을 작성합니다. 그런 다음 섹션 4.1.3에서 Spring Security에서 제공하는 가장 잘 알려지고 널리 사용되는 PasswordEncoder 구현 목록을 제공합니다.

### 4.1.1 THE DEFINITION OF THE PASSWORDENCODER CONTRACT

이 섹션에서는 PasswordEncoder 계약의 정의에 대해 설명합니다. 이 계약을 구현하여 Spring Security에 사용자 비밀번호의 유효성을 검사하는 방법을 알려줍니다. 인증 프로세스에서 PasswordEncoder는 암호가 유효한지 여부를 결정합니다. 모든 시스템은 어떤 방식으로든 인코딩된 암호를 저장합니다. 누군가가 암호를 읽을 수 있는 기회가 없도록 해시를 저장하는 것이 좋습니다. PasswordEncoder는 암호를 인코딩할 수도 있습니다. 계약에서 선언하는 메서드인 encode() 및 matching()은 실제로 그 책임의 정의입니다. 이 둘은 서로 강력하게 연결되어 있기 때문에 동일한 계약의 일부입니다. 애플리케이션이 암호를 인코딩하는 방식은 암호의 유효성이 검사되는 방식과 관련이 있습니다. 먼저 PasswordEncoder 인터페이스의 내용을 검토해 보겠습니다.
```java
public interface PasswordEncoder {

  String encode(CharSequence rawPassword);
  boolean matches(CharSequence rawPassword, String encodedPassword);

  default boolean upgradeEncoding(String encodedPassword) { 
    return false; 
  }
}
```
인터페이스는 두 개의 추상 메소드와 기본 구현이 있는 하나를 정의합니다. 추상 encode() 및 match() 메서드는 PasswordEncoder 구현을 다룰 때 가장 자주 듣는 메서드이기도 합니다.

encode(CharSequence rawPassword) 메서드의 목적은 제공된 문자열의 변환을 반환하는 것입니다. Spring Security 기능 측면에서 주어진 암호에 대한 암호화 또는 해시를 제공하는 데 사용됩니다. 이후에 match(CharSequence rawPassword, StringcodedPassword) 메서드를 사용하여 인코딩된 문자열이 원시 암호와 일치하는지 확인할 수 있습니다. 인증 프로세스에서 match() 메서드를 사용하여 알려진 자격 증명 집합에 대해 제공된 암호를 테스트합니다. upgradeEncoding(CharSequencecodedPassword)이라고 하는 세 번째 방법은 계약에서 기본적으로 false로 설정됩니다. true를 반환하도록 재정의하면 더 나은 보안을 위해 인코딩된 암호가 다시 인코딩됩니다.

어떤 경우에는 인코딩된 암호를 인코딩하면 결과에서 일반 텍스트 암호를 얻기가 더 어려울 수 있습니다. 일반적으로 이것은 개인적으로 좋아하지 않는 일종의 모호함입니다. 그러나 프레임워크는 귀하의 사례에 적용된다고 생각하는 경우 이러한 가능성을 제공합니다.

### 4.1.2 IMPLEMENTING THE PASSWORDENCODER CONTRACT

두 메서드 match()와 encode()는 강한 관계를 가지고 있습니다. 그것들을 재정의하는 경우 기능 면에서 항상 일치해야 합니다. encode() 메서드에서 반환된 문자열은 항상 동일한 PasswordEncoder의 match() 메서드로 확인할 수 있어야 합니다. 이 섹션에서는 PasswordEncoder 계약을 구현하고 인터페이스에서 선언한 두 개의 추상 메서드를 정의합니다. PasswordEncoder를 구현하는 방법을 알면 애플리케이션이 인증 프로세스에 대한 암호를 관리하는 방법을 선택할 수 있습니다. 가장 간단한 구현은 암호를 일반 텍스트로 간주하는 암호 인코더입니다. 즉, 암호에 대해 인코딩을 수행하지 않습니다.

일반 텍스트로 암호를 관리하는 것은 NoOpPasswordEncoder의 인스턴스가 정확하게 수행하는 작업입니다. 2장의 첫 번째 예제에서 이 클래스를 사용했습니다. 직접 작성한다면 다음 목록과 같을 것입니다.

Listing 4.1 The simplest implementation of a PasswordEncoder
```java
public class PlainTextPasswordEncoder 
  implements PasswordEncoder {

  @Override
  public String encode(CharSequence rawPassword) {
    return rawPassword.toString();                       ❶
  }

  @Override
  public boolean matches(
    CharSequence rawPassword, String encodedPassword) {
      return rawPassword.equals(encodedPassword);        ❷
  }
}
```
❶ We don’t change the password, just return it as is.

❷ Checks if the two strings are equal

The result of the encoding is always the same as the password. So to check if these match, you only need to compare the strings with equals(). A simple implementation of PasswordEncoder that uses the hashing algorithm SHA-512 looks like the next listing.

Listing 4.2 Implementing a PasswordEncoder that uses SHA-512
```java
public class Sha512PasswordEncoder 
  implements PasswordEncoder {

  @Override
  public String encode(CharSequence rawPassword) {
    return hashWithSHA512(rawPassword.toString());
  }

  @Override
  public boolean matches(
    CharSequence rawPassword, String encodedPassword) {
    String hashedPassword = encode(rawPassword);
    return encodedPassword.equals(hashedPassword);
  }

  // Omitted code

}
```
In listing 4.2, we use a method to hash the string value provided with SHA-512. I omit the implementation of this method in listing 4.2, but you can find it in listing 4.3. We call this method from the encode() method, which now returns the hash value for its input. To validate a hash against an input, the matches() method hashes the raw password in its input and compares it for equality with the hash against which it does the validation.

Listing 4.3 The implementation of the method to hash the input with SHA-512
```java
private String hashWithSHA512(String input) {
  StringBuilder result = new StringBuilder();
  try {
    MessageDigest md = MessageDigest.getInstance("SHA-512");
    byte [] digested = md.digest(input.getBytes());
    for (int i = 0; i < digested.length; i++) {
       result.append(Integer.toHexString(0xFF & digested[i]));
    }
  } catch (NoSuchAlgorithmException e) {
    throw new RuntimeException("Bad algorithm");
  }
  return result.toString();
}
```
You’ll learn better options to do this in the next section, so don’t bother too much with this code for now.

### 4.1.3 CHOOSING FROM THE PROVIDED IMPLEMENTATIONS OF PASSWORDENCODER

While knowing how to implement your PasswordEncoder is powerful, you also have to be aware that Spring Security already provides you with some advantageous implementations. If one of these matches your application, you don’t need to rewrite it. In this section, we discuss the PasswordEncoder implementation options that Spring Security provides. These are

- NoOpPasswordEncoder--Doesn’t encode the password but keeps it in cleartext. We use this implementation only for examples. Because it doesn’t hash the password, you should never use it in a real-world scenario.

- StandardPasswordEncoder--Uses SHA-256 to hash the password. This implementation is now deprecated, and you shouldn’t use it for your new implementations. The reason why it’s deprecated is that it uses a hashing algorithm that we don’t consider strong enough anymore, but you might still find this implementation used in existing applications.

- Pb

- kdf2PasswordEncoder--Uses the password-based key derivation function 2 (PBKDF2).

- BCryptPasswordEncoder--Uses a bcrypt strong hashing function to encode the password.

- SCryptPasswordEncoder--Uses an scrypt hashing function to encode the password.

해싱 및 이러한 알고리즘에 대한 자세한 내용은 David Wong의 Real-World Cryptography(Manning, 2020)의 2장에서 좋은 토론을 찾을 수 있습니다. 링크는 다음과 같습니다.
https://livebook.manning.com/book/real-world-cryptography/chapter-2/

이러한 유형의 PasswordEncoder 구현 인스턴스를 만드는 방법에 대한 몇 가지 예를 살펴보겠습니다. NoOpPasswordEncoder는 암호를 인코딩하지 않습니다. 목록 4.1의 예제에서 PlainTextPasswordEncoder와 유사한 구현이 있습니다. 이러한 이유로 이 암호 인코더는 이론적인 예만 사용합니다. 또한 NoOpPasswordEncoder 클래스는 싱글톤으로 설계되었습니다. 클래스 외부에서 직접 생성자를 호출할 수는 없지만 NoOpPasswordEncoder.getInstance() 메서드를 사용하여 다음과 같이 클래스의 인스턴스를 얻을 수 있습니다.

```java
PasswordEncoder p = NoOpPasswordEncoder.getInstance();
```
Spring Security에서 제공하는 StandardPasswordEncoder 구현은 SHA-256을 사용하여 비밀번호를 해시합니다. StandardPasswordEncoder의 경우 해싱 프로세스에 사용되는 비밀을 제공할 수 있습니다. 생성자의 매개변수로 이 비밀 값을 설정합니다. 인수가 없는 생성자를 호출하도록 선택하면 구현에서 빈 문자열을 키 값으로 사용합니다. 그러나 StandardPasswordEncoder는 이제 더 이상 사용되지 않으며 새 구현과 함께 사용하지 않는 것이 좋습니다. 여전히 사용하는 오래된 애플리케이션이나 레거시 코드를 찾을 수 있으므로 이를 알고 있어야 합니다. 다음 코드 조각은 이 암호 인코더의 인스턴스를 만드는 방법을 보여줍니다.

```java
PasswordEncoder p = new StandardPasswordEncoder();
PasswordEncoder p = new StandardPasswordEncoder("secret");
```

Spring Security에서 제공하는 또 다른 옵션은 비밀번호 인코딩을 위해 PBKDF2를 사용하는 Pbkdf2PasswordEncoder 구현입니다. Pbkdf2PasswordEncoder의 인스턴스를 생성하려면 다음 옵션이 있습니다.

```java
PasswordEncoder p = new Pbkdf2PasswordEncoder();
PasswordEncoder p = new Pbkdf2PasswordEncoder("secret");
PasswordEncoder p = new Pbkdf2PasswordEncoder("secret", 185000, 256);
```
PBKDF2는 iterations 인수로 지정된 횟수만큼 HMAC를 수행하는 매우 쉽고 느린 해싱 함수입니다. 마지막 호출에서 수신한 세 가지 매개변수는 인코딩 프로세스에 사용된 키 값, 비밀번호 인코딩에 사용된 반복 횟수, 해시 크기입니다. 두 번째 및 세 번째 매개변수는 결과의 강도에 영향을 줄 수 있습니다. 결과 길이뿐만 아니라 더 많거나 더 적은 반복을 선택할 수 있습니다. 해시가 길수록 암호가 더 강력해집니다.

그러나 성능은 이러한 값의 영향을 받습니다. 반복이 많을수록 애플리케이션이 더 많은 리소스를 소비합니다. 해시 생성에 사용되는 리소스와 필요한 인코딩 강도 사이에서 현명한 타협을 해야 합니다.

> 참고 이 책에서는 여러분이 더 알고 싶어할 몇 가지 암호화 개념을 언급합니다. HMAC 및 기타 암호화 세부 정보에 대한 관련 정보는 David Wong의 Real-World Cryptography(Manning, 2020)를 권장합니다. 이 책의 3장은 HMAC에 대한 자세한 정보를 제공합니다. https://livebook.manning.com/book/real-world-cryptography/chapter-3/에서 책을 찾을 수 있습니다.

Pbkdf2PasswordEncoder 구현에 대해 두 번째 또는 세 번째 값 중 하나를 지정하지 않으면 기본값은 반복 횟수에 대해 185000이고 결과 길이에 대해 256입니다. 매개변수가 없는 생성자 Pbkdf2PasswordEncoder() 또는 비밀 값만 매개변수로 받는 Pbkdf2PasswordEncoder( "비밀").

Spring Security에서 제공하는 또 다른 훌륭한 옵션은 bcrypt 강력한 해싱 기능을 사용하여 암호를 인코딩하는 BCryptPasswordEncoder입니다. 인수가 없는 생성자를 호출하여 BCryptPasswordEncoder를 인스턴스화할 수 있습니다. 그러나 인코딩 프로세스에 사용된 로그 라운드(로그 라운드)를 나타내는 강도 계수를 지정할 수도 있습니다. 또한 인코딩에 사용되는 SecureRandom 인스턴스를 변경할 수도 있습니다.

```java
PasswordEncoder p = new BCryptPasswordEncoder();
PasswordEncoder p = new BCryptPasswordEncoder(4);

SecureRandom s = SecureRandom.getInstanceStrong();
PasswordEncoder p = new BCryptPasswordEncoder(4, s);
```
제공하는 로그 반올림 값은 해싱 작업에서 사용하는 반복 횟수에 영향을 줍니다. 사용된 반복 횟수는 2log 라운드입니다. 반복 횟수 계산의 경우 로그 라운드의 값은 4에서 31 사이일 수 있습니다. 이전 코드 조각에 표시된 것처럼 두 번째 또는 세 번째 오버로드된 생성자 중 하나를 호출하여 이를 지정할 수 있습니다.

내가 제시하는 마지막 옵션은 SCryptPasswordEncoder입니다(그림 4.2). 이 암호 인코더는 scrypt 해싱 기능을 사용합니다. ScryptPasswordEncoder의 경우 인스턴스를 만드는 두 가지 옵션이 있습니다.

```java
PasswordEncoder p = new SCryptPasswordEncoder();
PasswordEncoder p = new SCryptPasswordEncoder(16384, 8, 1, 32, 64);
```
이전 예제의 값은 인수가 없는 생성자를 호출하여 인스턴스를 생성한 경우에 사용된 값입니다.
 
그림 4.2 SCryptPasswordEncoder 생성자는 5개의 매개변수를 사용하며 CPU 비용, 메모리 비용, 키 길이 및 솔트 길이를 구성할 수 있습니다.

### 4.1.4 PASSWORDENCODER 위임을 통한 다중 인코딩 전략

이 섹션에서는 인증 흐름이 암호 일치를 위해 다양한 구현을 적용해야 하는 경우에 대해 설명합니다. 또한 응용 프로그램에서 PasswordEncoder 역할을 하는 유용한 도구를 적용하는 방법도 배우게 됩니다. 자체 구현 대신 이 도구는 PasswordEncoder 인터페이스를 구현하는 다른 개체에 위임합니다.

일부 응용 프로그램에서는 다양한 암호 인코더가 있고 특정 구성에 따라 선택하는 것이 유용할 수 있습니다. 프로덕션 응용 프로그램에서 DelegatingPasswordEncoder를 찾는 일반적인 시나리오는 특정 버전의 응용 프로그램에서 시작하여 인코딩 알고리즘이 변경되는 경우입니다. 누군가 현재 사용되는 알고리즘에서 취약점을 발견하고 새로 등록된 사용자에 대해 변경하고 싶지만 기존 자격 증명에 대해서는 변경하고 싶지 않다고 상상해 보십시오. 따라서 여러 종류의 해시가 생깁니다. 이 사건을 어떻게 처리합니까? 이 시나리오의 유일한 접근 방식은 아니지만 DelegatingPasswordEncoder 개체를 사용하는 것이 좋습니다.

DelegatingPasswordEncoder는 인코딩 알고리즘을 구현하는 대신 동일한 계약 구현의 다른 인스턴스에 위임하는 PasswordEncoder 인터페이스의 구현입니다. 해시는 해당 해시를 정의하는 데 사용되는 알고리즘의 이름을 지정하는 접두사로 시작합니다. DelegatingPasswordEncoder는 암호의 접두사를 기반으로 PasswordEncoder의 올바른 구현을 위임합니다.

복잡해 보이지만 예를 들어보면 꽤 쉽다는 것을 알 수 있습니다. 그림 4.3은 PasswordEncoder 인스턴스 간의 관계를 나타냅니다. DelegatingPasswordEncoder에는 위임하는 PasswordEncoder 구현 목록이 있습니다. DelegatingPasswordEncoder는 맵에 각 인스턴스를 저장합니다. NoOpPasswordEncoder는 키 noop에 할당되고 BCryptPasswordEncoder 구현에는 키 bcrypt가 할당됩니다. 비밀번호에 {noop} 접두사가 있는 경우 DelegatingPasswordEncoder는 작업을 NoOpPasswordEncoder 구현에 위임합니다. 접두사가 {bcrypt}이면 그림 4.4에 표시된 대로 작업이 BCryptPasswordEncoder 구현에 위임됩니다.
 
그림 4.3 이 경우 DelegatingPasswordEncoder는 접두사 {noop}에 대해 NoOpPasswordEncoder, 접두사 {bcrypt}에 대해 BCryptPasswordEncoder, 접두사 {scrypt}에 대해 SCrypt-PasswordEncoder를 등록합니다. 비밀번호에 {noop} 접두사가 있는 경우 DelegatingPasswordEncoder는 작업을 NoOpPasswordEncoder 구현으로 전달합니다.
 
그림 4.4 이 경우 DelegatingPasswordEncoder는 접두사 {noop}에 대해 NoOpPasswordEncoder, 접두사 {bcrypt}에 대해 BCryptPasswordEncoder, 접두사 {scrypt}에 대해 SCrypt-PasswordEncoder를 등록합니다. 비밀번호에 {bcrypt} 접두사가 있는 경우 DelegatingPasswordEncoder는 작업을 BCryptPasswordEncoder 구현으로 전달합니다.
다음으로 DelegatingPasswordEncoder를 정의하는 방법을 알아보겠습니다. 원하는 PasswordEncoder 구현의 인스턴스 모음을 만드는 것으로 시작하고 다음 목록과 같이 DelegatingPasswordEncoder에 함께 넣습니다.

목록 4.4 DelegatingPasswordEncoder의 인스턴스 만들기
```java
@Configuration
public class ProjectConfig {

  // Omitted code

  @Bean
  public PasswordEncoder passwordEncoder() {
    Map<String, PasswordEncoder> encoders = new HashMap<>();

    encoders.put("noop", NoOpPasswordEncoder.getInstance());
    encoders.put("bcrypt", new BCryptPasswordEncoder());
    encoders.put("scrypt", new SCryptPasswordEncoder());

    return new DelegatingPasswordEncoder("bcrypt", encoders);
  }
}
```
DelegatingPasswordEncoder는 구현 모음에서 선택해야 할 때 사용할 수 있도록 PasswordEncoder 역할을 하는 도구일 뿐입니다. 목록 4.4에서 선언된 DelegatingPasswordEncoder 인스턴스는 NoOpPasswordEncoder, BCryptPasswordEncoder 및 SCryptPasswordEncoder에 대한 참조를 포함하고 기본값을 BCryptPasswordEncoder 구현에 위임합니다. 해시의 접두사를 기반으로 DelegatingPasswordEncoder는 비밀번호 일치를 위해 올바른 PasswordEncoder 구현을 사용합니다. 이 접두사에는 인코더 맵에서 사용할 암호 인코더를 식별하는 키가 있습니다. 접두사가 없으면 DelegatingPasswordEncoder는 기본 인코더를 사용합니다. 기본 PasswordEncoder는 DelegatingPasswordEncoder 인스턴스를 구성할 때 첫 번째 매개변수로 제공되는 것입니다. 목록 4.4의 코드에서 기본 PasswordEncoder는 bcrypt입니다.

> 참고 중괄호는 해시 접두사의 일부이며 키 이름을 둘러싸야 합니다. 예를 들어 제공된 해시가 {noop}12345이면 DelegatingPasswordEncoder는 접두사 noop에 대해 등록한 NoOpPasswordEncoder에 위임합니다. 다시 말하지만, 중괄호는 접두사에서 필수라는 것을 잊지 마십시오.

해시가 다음 코드 조각처럼 보이는 경우 비밀번호 인코더는 BCryptPasswordEncoder인 접두사 {bcrypt}에 할당한 것입니다. 기본 구현으로 정의했기 때문에 접두사가 전혀 없는 경우 애플리케이션이 위임할 대상이기도 합니다.
```
{bcrypt}$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG
```
편의를 위해 Spring Security는 모든 표준 제공 PasswordEncoder 구현에 대한 맵이 있는 DelegatingPasswordEncoder를 생성하는 방법을 제공합니다. PasswordEncoderFactories 클래스는 bcrypt를 기본 인코더로 사용하여 DelegatingPasswordEncoder의 구현을 반환하는 createDelegating-PasswordEncoder() 정적 메서드를 제공합니다.
```java
PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
```

> 인코딩 vs. 암호화 vs. 해싱

이전 섹션에서는 인코딩, 암호화 및 해싱이라는 용어를 자주 사용했습니다. 나는 이 용어들과 우리가 책 전체에서 사용하는 방식을 간략하게 설명하고자 합니다. 인코딩은 주어진 입력의 모든 변환을 나타냅니다. 예를 들어 문자열을 뒤집는 함수 x가 있는 경우 함수 x -> y를 ABCD에 적용하면 DCBA가 생성됩니다. 암호화는 출력을 얻기 위해 입력 값과 키를 모두 제공하는 특정 유형의 인코딩입니다. 키를 사용하면 나중에 누가 기능을 되돌릴 수 있는지 선택할 수 있습니다(출력에서 입력을 얻음). 암호화를 함수로 나타내는 가장 간단한 형식은 다음과 같습니다.
```
(x, k) -> y
```
여기서 x는 입력, k는 키, y는 암호화 결과입니다. 

이런 식으로 개인은 키가 알려진 기능을 사용하여 출력(y, k) -> x에서 입력을 얻을 수 있다는 것을 알고 있습니다. 우리는 이것을 역함수 복호화라고 부릅니다. 암호화에 사용되는 키가 복호화에 사용되는 키와 동일한 경우 일반적으로 대칭 키라고 합니다.

암호화((x, k1) -> y) 및 복호화((y, k2) -> x)를 위한 두 개의 다른 키가 있는 경우 암호화가 비대칭 키로 수행된다고 말합니다. 그런 다음 (k1, k2)를 키 쌍이라고 합니다. 암호화에 사용되는 키 k1은 공개 키라고도 하고 k2는 개인 키라고도 합니다. 이렇게 하면 개인 키의 소유자만 데이터를 해독할 수 있습니다.

해싱은 함수가 단방향이라는 점을 제외하고는 특정 유형의 인코딩입니다. 즉, 해싱 함수의 출력 y에서 입력 x를 되돌릴 수 없습니다. 그러나 출력 y가 입력 x에 해당하는지 확인할 수 있는 방법이 항상 있어야 하므로 해싱을 인코딩 및 일치를 위한 한 쌍의 함수로 이해할 수 있습니다. 해싱이 x -> y이면 일치하는 함수(x,y) -> 부울도 있어야 합니다.

때때로 해싱 함수는 입력에 추가된 임의의 값((x, k) -> y)을 사용할 수도 있습니다. 이 값을 소금이라고 합니다. 솔트는 함수를 더 강하게 만들어 결과에서 입력을 얻기 위해 역함수를 적용하는 것이 어렵습니다.
이 책에서 지금까지 논의하고 적용한 계약을 요약하기 위해 표 4.1은 각 구성 요소에 대해 간략하게 설명합니다.

**표 4.1** Spring Security에서 인증 흐름을 위한 주요 계약을 나타내는 인터페이스

- UserDetails: Spring Security에서 볼 수 있는 사용자를 나타냅니다.

- GrantedAuthority: 애플리케이션의 목적 내에서 사용자에게 허용되는 작업(예: 읽기, 쓰기, 삭제 등)을 정의합니다.

- UserDetailsService: 사용자 이름별로 사용자 세부 정보를 검색하는 데 사용되는 개체를 나타냅니다.

- UserDetailsManager: UserDetailsService에 대한 보다 구체적인 계약입니다. 사용자 이름으로 사용자를 검색하는 것 외에도 사용자 모음 또는 특정 사용자를 변경하는 데 사용할 수도 있습니다.

- PasswordEncoder: 암호가 암호화되거나 해시되는 방법과 주어진 인코딩된 문자열이 일반 텍스트 암호와 일치하는지 확인하는 방법을 지정합니다.

## 4.2 Spring Security Crypto 모듈에 대한 추가 정보

암호화를 다루는 Spring Security Crypto 모듈(SSCM)에 대해 설명합니다. 암호화 및 암호 해독 기능을 사용하고 키를 생성하는 것은 Java 언어와 함께 즉시 제공되지 않습니다. 이는 개발자가 이러한 기능에 더 쉽게 접근할 수 있는 접근 방식을 제공하는 종속성을 추가할 때 제약을 줍니다.

Spring Security는 프로젝트의 종속성을 줄일 수 있는 자체 솔루션을 제공합니다. 암호 인코더는 이전 섹션에서 별도로 처리한 경우에도 SSCM의 일부입니다. 이 섹션에서는 암호화와 관련하여 SSCM이 제공하는 다른 옵션에 대해 설명합니다. SSCM에서 두 가지 필수 기능을 사용하는 방법의 예를 볼 수 있습니다.

- 키 생성기: 해싱 및 암호화 알고리즘을 위한 키를 생성하는 데 사용되는 개체
- 암호화기: 데이터를 암호화하고 해독하는 데 사용되는 개체

### 4.2.1 키 생성기 사용

키 생성기는 일반적으로 암호화 또는 해싱 알고리즘에 필요한 특정 종류의 키를 생성하는 데 사용되는 개체입니다. Spring Security가 제공하는 키 생성기는 훌륭한 유틸리티 도구입니다. 애플리케이션에 다른 종속성을 추가하는 것보다 이러한 구현을 사용하는 것을 선호할 것이므로 이러한 구현을 숙지하는 것이 좋습니다. 키 생성기를 만들고 적용하는 방법에 대한 몇 가지 코드 예제를 살펴보겠습니다.

두 개의 인터페이스는 두 가지 주요 유형의 키 생성기인 BytesKeyGenerator 및 StringKeyGenerator를 나타냅니다. 팩토리 클래스 KeyGenerators를 사용하여 직접 빌드할 수 있습니다. StringKeyGenerator 계약으로 표시되는 문자열 키 생성기를 사용하여 키를 문자열로 가져올 수 있습니다. 일반적으로 이 키를 해싱 또는 암호화 알고리즘의 솔트 값으로 사용합니다. 이 코드 조각에서 StringKeyGenerator 계약의 정의를 찾을 수 있습니다.
```java
public interface StringKeyGenerator {
  String generateKey();
}
```
생성기에는 키 값을 나타내는 문자열을 반환하는 generateKey() 메서드만 있습니다. 다음 코드는 StringKeyGenerator 인스턴스를 얻는 방법과 이를 사용하여 솔트 값을 얻는 방법에 대한 예를 보여줍니다.
```java
StringKeyGenerator keyGenerator = KeyGenerators.string();
String salt = keyGenerator.generateKey();
```

생성기는 8바이트 키를 생성하고 이를 16진수 문자열로 인코딩합니다. 이 메서드는 이러한 작업의 결과를 문자열로 반환합니다. 키 생성기를 설명하는 두 번째 인터페이스는 다음과 같이 정의되는 BytesKeyGenerator입니다.
```java
public interface BytesKeyGenerator {
  int getKeyLength();
  byte[] generateKey();
}
```
키를 byte[]로 반환하는 generateKey() 메서드 외에도 인터페이스는 키 길이를 바이트 수로 반환하는 다른 메서드를 정의합니다. 기본 ByteKeyGenerator는 8바이트 길이의 키를 생성합니다.

```java
BytesKeyGenerator keyGenerator = KeyGenerators.secureRandom();
byte [] key = keyGenerator.generateKey();
int keyLength = keyGenerator.getKeyLength();
```

이전 코드에서 키 생성기는 8바이트 길이의 키를 생성합니다. 다른 키 길이를 지정하려는 경우 KeyGenerators.secureRandom() 메서드에 원하는 값을 제공하여 키 생성기 인스턴스를 가져올 때 이를 수행할 수 있습니다.

```java
BytesKeyGenerator keyGenerator = KeyGenerators.secureRandom(16);
```
KeyGenerators.secureRandom() 메소드로 생성된 BytesKeyGenerator에 의해 생성된 키는 generateKey() 메소드의 각 호출에 대해 고유합니다. 어떤 경우에는 동일한 키 생성기의 각 호출에 대해 동일한 키 값을 반환하는 구현을 선호합니다. 이 경우 KeyGenerators.shared(int length) 메서드를 사용하여 BytesKeyGenerator를 만들 수 있습니다. 이 코드 조각에서 key1과 key2는 동일한 값을 갖습니다.

```java
BytesKeyGenerator keyGenerator = KeyGenerators.shared(16);
byte [] key1 = keyGenerator.generateKey();
byte [] key2 = keyGenerator.generateKey();
```

### 4.2.2 암호화 및 복호화 작업에 암호화 사용

이 섹션에서는 Spring Security가 코드 예제와 함께 제공하는 암호화기의 구현을 적용합니다. 암호화기는 암호화 알고리즘을 구현하는 객체입니다. 보안에 대해 이야기할 때 암호화 및 암호 해독은 일반적인 작업이므로 애플리케이션 내에서 이러한 작업이 필요할 것으로 예상합니다.

시스템 구성 요소 간에 데이터를 보내거나 유지할 때 데이터를 암호화해야 하는 경우가 많습니다. 암호화기가 제공하는 작업은 암호화 및 암호 해독입니다. SSCM에서 정의하는 두 가지 유형의 암호화기(BytesEncryptor 및 TextEncryptor)가 있습니다. 그들은 비슷한 책임을 가지고 있지만 다른 데이터 유형을 취급합니다. TextEncryptor는 데이터를 문자열로 관리합니다. 해당 메소드는 인터페이스 정의에서 볼 수 있듯이 문자열을 입력으로 수신하고 문자열을 출력으로 반환합니다.
```java
public interface TextEncryptor {

  String encrypt(String text);
  String decrypt(String encryptedText);

}
```
BytesEncryptor가 좀 더 일반적입니다. 입력 데이터는 바이트 배열로 제공합니다:
```java
public interface BytesEncryptor {

  byte[] encrypt(byte[] byteArray);
  byte[] decrypt(byte[] encryptedByteArray);

}
```
암호화기를 구축하고 사용하기 위해 어떤 옵션이 필요한지 알아보겠습니다. 팩토리 클래스 Encryptors는 다양한 가능성을 제공합니다. BytesEncryptor의 경우 다음과 같이 Encryptors.standard() 또는 Encryptors.stronger() 메서드를 사용할 수 있습니다. 
```java
String salt = KeyGenerators.string().generateKey();
String password = "secret";
String valueToEncrypt = "HELLO";

BytesEncryptor e = Encryptors.standard(password, salt);
byte [] encrypted = e.encrypt(valueToEncrypt.getBytes());
byte [] decrypted = e.decrypt(encrypted);
```
표준 바이트 암호화기는 `256바이트 AES 암호화`를 사용하여 암호화합니다. 바이트 암호화기의 더 강력한 인스턴스를 빌드하려면 Encryptors.stronger() 메서드를 호출할 수 있습니다.
```java
BytesEncryptor e = Encryptors.stronger(password, salt);
```
그 차이는 작으며 256비트의 AES 암호화가 작동 모드로 Galois/Counter Mode(GCM)를 사용하는 배후에서 발생합니다. 표준 모드는 약한 방법으로 간주되는 CBC(암호 블록 체인)를 사용합니다.

TextEncryptors는 세 가지 주요 유형으로 제공됩니다. Encryptors.text(), Encryptors.delux() 또는 Encryptors.queryableText() 메서드를 호출하여 이 세 가지 유형을 만듭니다. 이러한 암호기를 만드는 방법 외에도 값을 암호화하지 않는 더미 TextEncryptor를 반환하는 방법도 있습니다. 암호화에 시간을 소비하지 않고 애플리케이션의 성능을 테스트하려는 경우 또는 데모 예제에 더미 TextEncryptor를 사용할 수 있습니다. 이 무작동 암호화기를 반환하는 메서드는 Encryptors.noOpText()입니다. 다음 코드에서 TextEncryptor를 사용하는 예를 찾을 수 있습니다. 암호화기에 대한 호출인 경우에도 이 예에서 암호화된 것과 valueToEncrypt는 동일합니다.

```java
String valueToEncrypt = "HELLO";
TextEncryptor e = Encryptors.noOpText();
String encrypted = e.encrypt(valueToEncrypt);
```

Encryptors.text() 암호기는 Encryptors.standard() 메서드를 사용하여 암호화 작업을 관리하는 반면 Encryptors.delux() 메서드는 다음과 같이 Encryptors.stronger() 인스턴스를 사용합니다.

```java
String salt = KeyGenerators.string().generateKey();
String password = "secret";
String valueToEncrypt = "HELLO";

TextEncryptor e = Encryptors.text(password, salt); ❶
String encrypted = e.encrypt(valueToEncrypt);
String decrypted = e.decrypt(encrypted);
```
❶ 솔트와 비밀번호를 사용하는 TextEncryptor 객체 생성

Encryptors.text() 및 Encryptors.delux()의 경우 동일한 입력에서 호출된 encrypt() 메서드가 반복적으로 다른 출력을 생성합니다. 암호화 프로세스에서 사용되는 무작위로 생성된 초기화 벡터 때문에 다른 출력이 발생합니다. 실제 세계에서는 예를 들어 OAuth API 키의 경우와 같이 이러한 일이 발생하지 않기를 원하는 경우를 찾을 수 있습니다. OAuth 2에 대해서는 12~15장에서 더 논의할 것입니다. 이러한 종류의 입력을 쿼리 가능한 텍스트라고 하며 이 경우 Encryptors.queryableText() 인스턴스를 사용합니다. 이 암호화기는 순차적 암호화 작업이 동일한 입력에 대해 동일한 출력을 생성하도록 보장합니다. 다음 예에서 encrypted1 변수의 값은 encrypted2 변수의 값과 같습니다.

```java
String salt = KeyGenerators.string().generateKey();
String password = "secret";
String valueToEncrypt = "HELLO";

TextEncryptor e = Encryptors.queryableText(password, salt); ❶

String encrypted1 = e.encrypt(valueToEncrypt);
String encrypted2 = e.encrypt(valueToEncrypt);
```

❶ Creates a queryable text encryptor

## 요약

- PasswordEncoder는 인증 논리에서 가장 중요한 책임 중 하나인 암호를 처리합니다.
- Spring Security는 해싱 알고리즘 측면에서 여러 대안을 제공하므로 구현은 선택 사항일 뿐입니다.
- Spring Security Crypto 모듈(SSCM)은 키 생성기 및 암호화기 구현을 위한 다양한 대안을 제공합니다.
- 키 생성기는 암호화 알고리즘과 함께 사용되는 키를 생성하는 데 도움이 되는 유틸리티 개체입니다.
- 암호화기는 데이터의 암호화 및 암호 해독을 적용하는 데 도움이 되는 유틸리티 개체입니다.
