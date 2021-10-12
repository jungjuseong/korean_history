# 17 전역 메서드 보안: 사전 및 사후 필터링

이 장에서는 다음을 다룹니다.

- 사전 필터링으로 메소드가 매개변수 값으로 수신하는 것을 제한

- 사후 필터링으로 메서드 반환을 제한

- Spring Data와 필터링 통합

16장에서 전역 메서드 보안을 사용하여 권한 부여 규칙을 적용하는 방법을 배웠습니다. @PreAuthorize 및 @PostAuthorize 주석을 사용하여 예제를 작업했습니다. 이러한 주석을 사용하여 애플리케이션이 메서드 호출을 허용하거나 호출을 완전히 거부하는 접근 방식을 적용합니다. 메서드에 대한 호출을 금지하고 싶지 않지만 전송된 매개변수가 몇 가지 규칙을 따르는지 확인하고 싶다고 가정해 보겠습니다. 또는 다른 시나리오에서 누군가가 메서드를 호출한 후 메서드의 호출자가 반환된 값의 승인된 부분만 수신하도록 하려고 합니다. 이러한 기능 필터링의 이름을 지정하고 두 가지 범주로 분류합니다.

- 사전 필터링--프레임워크는 메서드를 호출하기 전에 매개변수 값을 필터링합니다.

- 사후 필터링--프레임워크는 메서드 호출 후 반환된 값을 필터링합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH17_F01_Spilca.png)

그림 17.1 클라이언트는 인증 규칙을 따르지 않는 값을 제공하는 엔드포인트를 호출합니다. 사전 승인을 사용하면 메서드가 전혀 호출되지 않고 호출자는 예외를 수신합니다. 사전 필터링을 사용하면 aspect가 메서드를 호출하지만 주어진 규칙을 따르는 값만 제공합니다.

필터링은 호출 승인과 다르게 작동합니다(그림 17.1). 필터링을 사용하면 프레임워크는 호출을 실행하고 매개변수 또는 반환된 값이 정의한 권한 부여 규칙을 따르지 않는 경우 예외를 throw하지 않습니다. 대신 지정한 조건을 따르지 않는 요소를 필터링합니다.

컬렉션과 배열에만 필터링을 적용할 수 있다는 점을 처음부터 언급하는 것이 중요합니다. 메서드가 배열 또는 개체 컬렉션을 매개 변수로 받는 경우에만 사전 필터링을 사용합니다. 프레임워크는 사용자가 정의한 규칙에 따라 이 컬렉션 또는 배열을 필터링합니다. 사후 필터링도 동일합니다. 메서드가 컬렉션이나 배열을 반환하는 경우에만 이 접근 방식을 적용할 수 있습니다. 프레임워크는 지정한 규칙에 따라 메서드가 반환하는 값을 필터링합니다.

## 17.1 메소드 승인을 위한 사전 필터링 적용

이 섹션에서는 사전 필터링의 메커니즘에 대해 논의한 다음 예제에서 사전 필터링을 구현합니다. 필터링을 사용하여 누군가가 메서드를 호출할 때 메서드 매개변수를 통해 전송된 값의 유효성을 검사하도록 프레임워크에 지시할 수 있습니다. 프레임워크는 주어진 기준과 일치하지 않는 값을 필터링하고 기준과 일치하는 값으로만 ​​메서드를 호출합니다. 이 기능을 사전 필터링이라고 합니다(그림 17.2).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH17_F02_Spilca.png)
그림 17.2 사전 필터링을 사용하여 aspect는 보호된 메서드에 대한 호출을 가로챕니다. aspect는 호출자가 매개변수로 제공하는 값을 필터링하고 사용자가 정의한 규칙을 따르는 값만 메소드에 보냅니다.

사전 필터링이 메서드가 구현하는 비즈니스 논리에서 권한 부여 규칙을 분리하기 때문에 사전 필터링이 잘 적용되는 실제 사례에서 요구 사항을 찾을 수 있습니다. 인증된 사용자가 소유한 특정 세부 정보만 처리하는 사용 사례를 구현한다고 가정해 보겠습니다. 이 사용 사례는 여러 곳에서 호출할 수 있습니다. 여전히 누가 유스 케이스를 호출하는지에 관계없이 인증된 사용자의 세부 정보만 처리할 수 있다는 책임이 있습니다. 사용 사례의 호출자가 권한 부여 규칙을 올바르게 적용하는지 확인하는 대신 사례가 자체 권한 부여 규칙을 적용하도록 합니다. 물론 메서드 내에서 이 작업을 수행할 수도 있습니다. 그러나 비즈니스 로직에서 권한 부여 로직을 분리하면 코드의 유지 관리 가능성이 향상되고 다른 사람들이 더 쉽게 읽고 이해할 수 있습니다.

16장에서 논의한 호출 승인의 경우와 마찬가지로 Spring Security도 aspect를 사용하여 필터링을 구현합니다. Aspect는 특정 메소드 호출을 가로채서 다른 명령어로 보강할 수 있습니다. 사전 필터링을 위해 aspect는 @PreFilter 주석으로 주석이 달린 메서드를 가로채고 정의한 기준에 따라 매개변수로 제공된 컬렉션의 값을 필터링합니다(그림 17.3).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH17_F03_Spilca.png)

그림 17.3 사전 필터링을 통해 비즈니스 구현에서 권한 부여 책임을 분리합니다. Spring Security에서 제공하는 aspect는 권한 부여 규칙만 처리하고 서비스 메소드는 구현하는 사용 사례의 비즈니스 로직만 처리합니다.

16장에서 논의한 @PreAuthorize 및 @PostAuthorize 주석과 유사하게 @PreFilter 주석의 값으로 권한 부여 규칙을 설정합니다. SpEL 표현식으로 제공하는 이러한 규칙에서 filterObject를 사용하여 메소드에 대한 매개변수로 제공하는 컬렉션 또는 배열 내의 모든 요소를 ​​참조합니다.

사전 필터링이 적용된 것을 보려면 프로젝트에서 작업해 보겠습니다. 이 프로젝트의 이름을 ssia-ch17-ex1로 지정했습니다. 제품 구매 및 판매를 위한 애플리케이션이 있고 백엔드가 /sell 엔드포인트를 구현한다고 가정합니다. 애플리케이션의 프런트엔드는 사용자가 제품을 판매할 때 이 끝점을 호출합니다. 그러나 로그인한 사용자는 자신이 소유한 제품만 판매할 수 있습니다. 매개변수로 받은 제품을 판매하기 위해 호출되는 서비스 메서드의 간단한 시나리오를 구현해 보겠습니다. 이 예제에서는 @PreFilter 주석을 적용하는 방법을 배웁니다. @PreFilter 주석은 메서드가 현재 로그인한 사용자가 소유한 제품만 수신하도록 하기 위해 사용하는 것입니다.

프로젝트를 생성한 후에는 구현을 테스트할 몇 명의 사용자가 있는지 확인하기 위해 구성 클래스를 작성합니다. 목록 17.1에서 구성 클래스의 직접적인 정의를 찾을 수 있습니다. 내가 ProjectConfig라고 부르는 구성 클래스는 UserDetailsService와 PasswordEncoder만 선언하고 @GlobalMethodSecurity(prePostEnabled=true)로 주석을 달았습니다. 필터링 주석의 경우 여전히 @GlobalMethodSecurity 주석을 사용하고 사전/사후 인증 주석을 활성화해야 합니다. 제공된 UserDetailsService는 테스트에 필요한 두 명의 사용자인 Nikolai와 Julien을 정의합니다.

Listing 17.1 Configuring users and enabling global method security
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ProjectConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    var uds = new InMemoryUserDetailsManager();

    var u1 = User.withUsername("nikolai")
            .password("12345")
            .authorities("read")
            .build();

    var u2 = User.withUsername("julien")
            .password("12345")
            .authorities("write")
            .build();

    uds.createUser(u1);
    uds.createUser(u2);

    return uds;
  }

  @Bean
    public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```
다음 목록에서 찾은 모델 클래스를 사용하여 제품을 설명합니다.

목록 17.2 Product 클래스 정의
```java
public class Product {

  private String name;
  private String owner;       ❶

  // Omitted constructor, getters, and setters
}
```
❶ 속성 소유자는 사용자 이름 값을 가집니다.

ProductService 클래스는 @PreFilter로 보호하는 서비스 메소드를 정의합니다. 목록 17.3에서 ProductService 클래스를 찾을 수 있습니다. 해당 목록에서 SellProducts() 메서드 이전에 @PreFilter 주석의 사용을 관찰할 수 있습니다. 주석과 함께 사용되는 SpEL(Spring Expression Language)은 filterObject .owner == authentication.name이며, 제품의 소유자 속성이 로그인한 사용자의 사용자 이름과 동일한 값만 허용합니다. SpEL 표현식에서 등호 연산자의 왼쪽에 있습니다. 우리는 filterObject를 사용합니다. filterObject를 사용하여 목록의 개체를 매개변수로 참조합니다. 제품 목록이 있으므로 이 경우 filterObject는 제품 유형입니다. 이러한 이유로 제품의 소유자 속성을 참조할 수 있습니다. 식에서 등호 연산자의 오른쪽에 있습니다. 우리는 인증 객체를 사용합니다. @PreFilter 및 @PostFilter 주석의 경우 인증 후 SecurityContext에서 사용할 수 있는 인증 개체를 직접 참조할 수 있습니다(그림 17.4).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH17_F04_Spilca.png)
그림 17.4 filterObject에 의한 사전 필터링을 사용할 때 호출자가 매개 변수로 제공하는 목록 내부의 개체를 참조합니다. 인증 객체는 보안 컨텍스트에서 인증 프로세스 이후에 저장되는 객체입니다.

서비스 메소드는 메소드가 수신한 대로 정확하게 목록을 리턴합니다. 이렇게 하면 HTTP 응답 본문에 반환된 목록을 확인하여 프레임워크가 목록을 예상대로 필터링했는지 테스트하고 확인할 수 있습니다.

목록 17.3 ProductService 클래스에서 @PreFilter 주석 사용하기
```java
@Service
public class ProductService {

  @PreFilter                                           ❶
  ➥ ("filterObject.owner == authentication.name")
  public List<Product> sellProducts(List<Product> products) {
    // sell products and return the sold products list
    return products;                                   ❷
  }
}
```
❶ 매개변수로 주어진 목록은 인증된 사용자가 소유한 제품만 허용합니다.

❷ 테스트 목적으로 제품을 반환

테스트를 더 쉽게 하기 위해 보호된 서비스 메서드를 호출하는 끝점을 정의합니다. 목록 17.4는 ProductController라는 컨트롤러 클래스에서 이 끝점을 정의합니다. 여기에서 엔드포인트 호출을 더 짧게 만들기 위해 목록을 만들어 서비스 메서드에 대한 매개변수로 직접 제공합니다. 실제 시나리오에서 이 목록은 요청 본문에서 클라이언트가 제공해야 합니다. 또한 비표준인 돌연변이를 제안하는 작업에 @GetMapping을 사용하는 것을 관찰할 수도 있습니다. 그러나 이 예에서 CSRF 보호를 다루지 않기 위해 이 작업을 수행한다는 것을 알고, 이를 통해 당면한 주제에 집중할 수 있습니다. 10장에서 CSRF 보호에 대해 배웠습니다.

목록 17.4 테스트에 사용하는 엔드포인트를 구현하는 컨트롤러 클래스
```java
@RestController
public class ProductController {

  @Autowired
  private ProductService productService;

  @GetMapping("/sell")
  public List<Product> sellProduct() {
    List<Product> products = new ArrayList<>();

    products.add(new Product("beer", "nikolai"));
    products.add(new Product("candy", "nikolai"));
    products.add(new Product("chocolate", "julien"));

    return productService.sellProducts(products);
  }
}
```
애플리케이션을 시작하고 /sell 엔드포인트를 호출할 때 어떤 일이 발생하는지 봅시다. 서비스 방법에 대한 매개변수로 제공한 목록에서 세 가지 제품을 관찰하십시오. 나는 두 개의 제품을 사용자 Nikolai에게 할당하고 다른 하나는 사용자 Julien에게 할당합니다. 엔드포인트를 호출하고 사용자 Nikolai로 인증할 때 응답에서 그녀와 연결된 두 제품만 볼 것으로 예상합니다. 엔드포인트를 호출하고 Julien으로 인증할 때 Julien과 관련된 하나의 제품만 응답에서 찾아야 합니다. 다음 코드 조각에서 테스트 호출과 그 결과를 찾을 수 있습니다. /sell 엔드포인트를 호출하고 사용자 Nikolai로 인증하려면 다음 명령을 사용하십시오.

```sh
curl -u nikolai:12345 http://localhost:8080/sell
```
The response body is
```json
[
  {"name":"beer","owner":"nikolai"},
  {"name":"candy","owner":"nikolai"}
]
```
To call the endpoint /sell and authenticate with user Julien, use this command:
```sh
curl -u julien:12345 http://localhost:8080/sell
```
The response body is
```
[
  {"name":"chocolate","owner":"julien"}
]
```
주의해야 할 점은 aspect가 주어진 컬렉션을 변경한다는 사실입니다. 우리의 경우 새 List 인스턴스를 반환할 것으로 기대하지 마십시오. 사실, aspect가 주어진 기준과 일치하지 않는 요소를 제거한 것과 동일한 인스턴스입니다. 이것은 고려하는 것이 중요합니다. 제공하는 컬렉션 인스턴스가 변경할 수 없는지 항상 확인해야 합니다. 처리할 불변 컬렉션을 제공하면 필터링 측면이 컬렉션의 내용을 변경할 수 없기 때문에 실행 시 예외가 발생합니다(그림 17.5).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH17_F05_Spilca.png)
그림 17.5 aspect는 매개변수로 주어진 컬렉션을 가로채서 변경한다. 관점에서 변경할 수 있도록 컬렉션의 변경 가능한 인스턴스를 제공해야 합니다.
Listing 17.5는 이 섹션의 앞부분에서 작업한 것과 동일한 프로젝트를 나타내지만, 이 상황에서 어떤 일이 발생하는지 테스트하기 위해 List.of() 메서드에서 반환된 불변 인스턴스로 List 정의를 변경했습니다.

목록 17.5 불변 컬렉션 사용하기
```java
@RestController
public class ProductController {

  @Autowired
  private ProductService productService;

  @GetMapping("/sell")
  public List<Product> sellProduct() {
    List<Product> products = List.of(            ❶
            new Product("beer", "nikolai"),
            new Product("candy", "nikolai"),
            new Product("chocolate", "julien"));

    return productService.sellProducts(products);
  }
}
```
❶ List.of()는 목록의 불변 인스턴스를 반환합니다.

이 예제를 프로젝트 ssia-ch17-ex2 폴더에 분리하여 직접 테스트할 수도 있습니다. 응용 프로그램을 실행하고 /sell 끝점을 호출하면 다음 코드 조각에 표시된 대로 상태 500 내부 서버 오류 및 콘솔 로그의 예외가 있는 HTTP 응답이 발생합니다.
```sh
curl -u julien:12345 http://localhost:8080/sell
```
The response body is:
```
{
  "status":500,
  "error":"Internal Server Error",
  "message":"No message available",
  "path":"/sell"
}
```
애플리케이션 콘솔에서 다음 코드 스니펫에 표시된 것과 유사한 예외를 찾을 수 있습니다.
```
java.lang.UnsupportedOperationException: null
        at java.base/java.util.ImmutableCollections.uoe(ImmutableCollections.java:73) ~[na:na]
...
```

## 17.2 메소드 승인을 위한 사후 필터링 적용

이 섹션에서는 사후 필터링을 구현합니다. 다음 시나리오가 있다고 가정합니다. Angular로 구현된 프론트엔드와 Spring 기반 백엔드가 있는 애플리케이션이 일부 제품을 관리합니다. 사용자는 제품을 소유하고 있으며 자신의 제품에 대한 세부 정보만 얻을 수 있습니다. 제품의 세부 정보를 얻기 위해 프론트엔드는 백엔드에 의해 노출된 엔드포인트를 호출합니다(그림 17.6).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH17_F06_Spilca.png)
그림 17.6 사후 필터링 시나리오. 클라이언트는 엔드포인트를 호출하여 프런트엔드에 표시해야 하는 데이터를 검색합니다. 사후 필터링 구현은 클라이언트가 현재 인증된 사용자가 소유한 데이터만 가져오도록 합니다.

서비스 클래스의 백엔드에서 개발자는 제품의 세부 정보를 검색하는 List<Product> findProducts() 메서드를 작성했습니다. 클라이언트 응용 프로그램은 이러한 세부 정보를 프런트엔드에 표시합니다. 개발자는 이 메서드를 호출하는 사람이 다른 사람이 소유한 제품이 아닌 자신이 소유한 제품만 수신하도록 하려면 어떻게 해야 할까요? 애플리케이션의 비즈니스 규칙과 분리된 권한 부여 규칙을 유지하여 이 기능을 구현하는 옵션을 사후 필터링이라고 합니다. 이 섹션에서는 사후 필터링이 작동하는 방식에 대해 논의하고 애플리케이션에서 구현을 시연합니다.

사전 필터링과 유사하게 사후 필터링도 측면에 의존합니다. 이 측면은 메서드에 대한 호출을 허용하지만 메서드가 반환되면 해당 측면은 반환된 값을 사용하여 정의한 규칙을 따르는지 확인합니다. 사전 필터링의 경우와 마찬가지로 사후 필터링은 메서드에서 반환된 컬렉션 또는 배열을 변경합니다. 반환된 컬렉션 내부의 요소가 따라야 하는 기준을 제공합니다. 사후 필터 측면은 반환된 컬렉션에서 필터링하거나 규칙을 따르지 않는 요소를 배열합니다.

사후 필터링을 적용하려면 @PostFilter 주석을 사용해야 합니다. @PostFilter 주석은 14장과 이 장에서 사용한 다른 모든 사전/사후 주석과 유사하게 작동합니다. 권한 부여 규칙을 주석 값에 대한 SpEL 표현식으로 제공하고 해당 규칙은 그림 17.7과 같이 필터링 측면에서 사용하는 규칙입니다. 또한 사전 필터링과 유사하게 사후 필터링은 배열 및 컬렉션에서만 작동합니다. 반환 유형이 배열 또는 컬렉션인 메서드에만 @PostFilter 주석을 적용해야 합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH17_F07_Spilca.png)
그림 17.7 사후 필터링. aspect는 protected 메소드에 의해 반환된 컬렉션을 가로채서 당신이 제공한 규칙을 따르지 않는 값을 필터링합니다. 사후 인증과 달리 사후 필터링은 반환된 값이 인증 규칙을 따르지 않을 때 호출자에게 예외를 throw하지 않습니다.

예에서 사후 필터링을 적용해 보겠습니다. 이 예를 위해 ssia-ch17-ex3이라는 프로젝트를 만들었습니다. 일관성을 위해 구성 클래스가 변경되지 않도록 이 장의 이전 예제와 동일한 사용자를 유지했습니다. 귀하의 편의를 위해 다음 목록에 제시된 구성을 반복합니다.

목록 17.6 구성 클래스
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ProjectConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    var uds = new InMemoryUserDetailsManager();

    var u1 = User.withUsername("nikolai")
            .password("12345")
            .authorities("read")
            .build();

    var u2 = User.withUsername("julien")
            .password("12345")
            .authorities("write")
            .build();

    uds.createUser(u1);
    uds.createUser(u2);

    return uds;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
```
The next code snippet shows that the Product class remains unchanged as well:
```java
public class Product {

  private String name;
  private String owner;

  // Omitted constructor, getters, and setters
}
```
I이제 ProductService 클래스에서 제품 목록을 반환하는 메서드를 구현합니다. 실제 시나리오에서는 애플리케이션이 데이터베이스 또는 기타 데이터 소스에서 제품을 읽는다고 가정합니다. 예제를 짧게 유지하고 논의하는 측면에 집중할 수 있도록 목록 17.7에 나와 있는 것처럼 간단한 컬렉션을 사용합니다.
제품 목록을 반환하는 findProducts() 메서드에 @PostFilter 주석을 추가합니다. 내가 주석 값으로 추가한 조건, filterObject.owner == authentication.name은 소유자가 인증된 사용자와 동일한 제품만 반환되도록 허용합니다(그림 17.8). equals 연산자의 왼쪽에서 filterObject를 사용하여 반환된 컬렉션 내부의 요소를 참조합니다. 연산자의 오른쪽에서 인증을 사용하여 SecurityContext에 저장된 인증 개체를 참조합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH17_F08_Spilca.png)

그림 17.8 권한 부여에 사용되는 SpEL 표현식에서 filterObject를 사용하여 반환된 컬렉션의 개체를 참조하고 인증을 사용하여 보안 컨텍스트에서 인증 인스턴스를 참조합니다.

목록 17.7 ProductService 클래스
```java
@Service
public class ProductService {

  @PostFilter ❶
  ➥ ("filterObject.owner == authentication.name")
  public List<Product> findProducts() {
    List<Product> products = new ArrayList<>();

    products.add(new Product("beer", "nikolai"));
    products.add(new Product("candy", "nikolai"));
    products.add(new Product("chocolate", "julien"));

    return products;
  }
}
```
❶ 메서드에서 반환된 컬렉션의 개체에 대한 필터링 조건을 추가합니다.

끝점을 통해 메서드에 액세스할 수 있도록 컨트롤러 클래스를 정의합니다. 다음 목록은 컨트롤러 클래스를 나타냅니다.

목록 17.8 ProductController 클래스
```java
@RestController
public class ProductController {

  @Autowired
  private ProductService productService;

  @GetMapping("/find")
  public List<Product> findProducts() {
    return productService.findProducts();
  }
}
```
이제 /find 엔드포인트를 호출하여 애플리케이션을 실행하고 동작을 테스트할 시간입니다. HTTP 응답 본문에는 인증된 사용자가 소유한 제품만 표시됩니다. 다음 코드 조각은 각 사용자 Nikolai 및 Julien과 함께 엔드포인트를 호출한 결과를 보여줍니다. 엔드포인트 /find를 호출하고 사용자 Julien으로 인증하려면 다음 cURL 명령을 사용하십시오.

```sh
curl -u julien:12345 http://localhost:8080/find
```
The response body is
```json
[
  {"name":"chocolate","owner":"julien"}
]
```
To call the endpoint /find and authenticate with user Nikolai, use this cURL command:
```
curl -u nikolai:12345 http://localhost:8080/find
```
The response body is
```json
[
  {"name":"beer","owner":"nikolai"},
  {"name":"candy","owner":"nikolai"}
]
```

# 17.3 스프링 데이터 저장소에서 필터링 사용하기

이 섹션에서는 Spring Data 리포지토리에 적용된 필터링에 대해 설명합니다. 애플리케이션의 데이터를 유지하기 위해 데이터베이스를 자주 사용하기 때문에 이 접근 방식을 이해하는 것이 중요합니다. SQL 또는 NoSQL과 같은 데이터베이스에 연결하기 위해 상위 계층으로 Spring Data를 사용하는 Spring Boot 애플리케이션을 구현하는 것은 매우 일반적입니다. Spring Data를 사용할 때 저장소 수준에서 필터링을 적용하는 두 가지 접근 방식을 논의하고 이를 예제로 구현합니다.

우리가 취하는 첫 번째 접근 방식은 @PreFilter 및 @PostFilter 주석을 사용하여 지금까지 이 장에서 배운 방법입니다. 우리가 논의하는 두 번째 접근 방식은 쿼리에 권한 부여 규칙을 직접 통합하는 것입니다. 이 섹션에서 배우게 될 것처럼 Spring Data 리포지토리에서 필터링을 적용하는 방법을 선택할 때 주의를 기울여야 합니다. 언급했듯이 두 가지 옵션이 있습니다.

- @PreFilter 및 @PostFilter 주석 사용
- 쿼리 내에서 직접 필터링 적용

리포지토리의 경우 @PreFilter 주석을 사용하는 것은 애플리케이션의 다른 계층에 이 주석을 적용하는 것과 동일합니다. 그러나 사후 필터링의 경우 상황이 바뀝니다. 저장소 메소드에 @PostFilter를 사용하는 것은 기술적으로 잘 작동하지만 성능 관점에서 거의 좋은 선택이 아닙니다.

회사 문서를 관리하는 애플리케이션이 있다고 가정해 보겠습니다. 개발자는 사용자가 로그인한 후 웹 페이지에 모든 문서가 나열되는 기능을 구현해야 합니다. 개발자는 Spring Data 저장소의 findAll() 메서드를 사용하기로 결정하고 Spring Security가 필터링할 수 있도록 @PostFilter로 주석을 추가합니다. 메서드가 현재 로그인한 사용자가 소유한 문서만 반환하도록 문서. 이 접근 방식은 응용 프로그램이 데이터베이스에서 모든 레코드를 검색한 다음 레코드 자체를 필터링할 수 있도록 하기 때문에 분명히 잘못된 것입니다. 많은 수의 문서가 있는 경우 페이지 매김 없이 findAll()을 호출하면 OutOfMemoryError가 직접 발생할 수 있습니다. 문서의 수가 힙을 채울 만큼 크지 않더라도 처음부터 데이터베이스에서 필요한 것만 검색하는 것보다 애플리케이션에서 레코드를 필터링하는 것은 여전히 ​​성능이 떨어집니다(그림 17.9).

서비스 수준에서는 앱의 레코드를 필터링하는 것 외에 다른 옵션이 없습니다. 그러나 로그인한 사용자가 소유한 레코드만 검색해야 한다는 것을 저장소 수준에서 알고 있다면 데이터베이스에서 필요한 문서만 추출하는 쿼리를 구현해야 합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH17_F09_Spilca.png)

그림 17.9 잘못된 설계의 구조 리포지토리 수준에서 필터링을 적용해야 하는 경우 먼저 필요한 데이터만 검색하는지 확인하는 것이 좋습니다. 그렇지 않으면 애플리케이션이 과도한 메모리 및 성능 문제에 직면할 수 있습니다.
참고 데이터베이스, 웹 서비스, 입력 스트림 등 데이터 소스에서 데이터를 검색하는 모든 상황에서 응용 프로그램이 필요한 데이터만 검색하는지 확인하십시오. 응용 프로그램 내부에서 데이터를 필터링할 필요를 최대한 피하십시오.

먼저 Spring Data 리포지토리 메서드에서 @PostFilter 주석을 사용하는 애플리케이션에서 작업한 다음 쿼리에 직접 조건을 작성하는 두 번째 접근 방식으로 변경하겠습니다. 이렇게 하면 두 가지 접근 방식을 모두 실험하고 비교할 수 있습니다.
ssia-ch17-ex4라는 새 프로젝트를 만들었습니다. 여기에서 이 장의 이전 예제와 동일한 구성 클래스를 사용합니다. 이전 예에서와 같이 제품을 관리하는 애플리케이션을 작성하지만 이번에는 데이터베이스의 테이블에서 제품 세부 정보를 검색합니다. 이 예에서는 제품에 대한 검색 기능을 구현합니다(그림 17.10). 문자열을 수신하고 이름에 주어진 문자열이 포함된 제품 목록을 반환하는 끝점을 작성합니다. 그러나 인증된 사용자와 연결된 제품만 반환해야 합니다.

Spring Data JPA를 사용하여 데이터베이스에 연결합니다. 이러한 이유로 우리는 또한 데이터베이스 관리 서버 기술에 따라 pom.xml 파일에 spring-boot-starter-data-jpa 종속성과 연결 드라이버를 추가해야 합니다. 다음 코드 조각은 pom.xml 파일에서 사용하는 종속성을 제공합니다.
```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
   <groupId>mysql</groupId>
   <artifactId>mysql-connector-java</artifactId>
   <scope>runtime</scope>
</dependency>
```
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH17_F10_Spilca.png)

그림 17.10 이 시나리오에서는 소유자를 기반으로 제품을 필터링하기 위해 @PostFilter를 사용하여 애플리케이션을 구현하는 것으로 시작합니다. 그런 다음 쿼리에 직접 조건을 추가하도록 구현을 변경합니다. 이렇게 하면 애플리케이션이 소스에서 필요한 레코드만 가져오도록 합니다.
application.properties 파일에서 Spring Boot가 데이터 소스를 생성하는 데 필요한 속성을 추가합니다. 다음 코드 조각에서 내 application.properties 파일에 추가한 속성을 찾을 수 있습니다.

```yml
spring.datasource.url=jdbc:mysql://localhost/spring
➥ ?useLegacyDatetimeCode=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.datasource.initialization-mode=always
```

또한 애플리케이션이 검색하는 제품 세부 정보를 저장하기 위해 데이터베이스에 테이블이 필요합니다. 테이블 생성을 위한 스크립트를 작성하는 schema.sql 파일과 테이블에 테스트 데이터를 삽입하기 위한 쿼리를 작성하는 data.sql 파일을 정의합니다. Spring Boot 프로젝트의 리소스 폴더에 두 파일(schema.sql 및 data.sql)을 모두 배치해야 애플리케이션 시작 시 파일을 찾고 실행할 수 있습니다. 다음 코드 조각은 schema.sql 파일에 작성해야 하는 테이블을 생성하는 데 사용된 쿼리를 보여줍니다.

```sql
CREATE TABLE IF NOT EXISTS `spring`.`product` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `owner` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));
In the data.sql file, I write three INSERT statements, which the next code snippet presents. These statements create the test data that we need later to prove the application’s behavior.
INSERT IGNORE INTO `spring`.`product` (`id`, `name`, `owner`) VALUES ('1', 'beer', 'nikolai');
INSERT IGNORE INTO `spring`.`product` (`id`, `name`, `owner`) VALUES ('2', 'candy', 'nikolai');
INSERT IGNORE INTO `spring`.`product` (`id`, `name`, `owner`) VALUES ('3', 'chocolate', 'julien');
```
> 참고 책 전체의 다른 예에서 동일한 이름을 테이블에 사용했음을 기억하십시오. 이전 예제에서 동일한 이름을 가진 테이블이 이미 있는 경우 이 프로젝트를 시작하기 전에 테이블을 삭제해야 합니다. 대안은 다른 스키마를 사용하는 것입니다.

애플리케이션에서 제품 테이블을 매핑하려면 엔터티 클래스를 작성해야 합니다. 다음 목록은 제품 엔터티를 정의합니다.

목록 17.9 Product 엔티티 클래스
```java
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String owner;

    // Omitted getters and setters
}
```
Product 엔터티의 경우 다음 목록에 정의된 Spring Data 저장소 인터페이스도 작성합니다. 이번에는 저장소 인터페이스에 의해 선언된 메소드에서 직접 @PostFilter 주석을 사용하는 것을 관찰하십시오.

목록 17.10 ProductRepository 인터페이스
```java
public interface ProductRepository
        extends JpaRepository<Product, Integer> {

    @PostFilter                                        ❶
    ➥ ("filterObject.owner == authentication.name")
    List<Product> findProductByNameContains(String text);
}
```
❶ Spring Data 리포지토리에서 선언한 메소드에 @PostFilter 어노테이션 사용

다음 목록은 동작 테스트에 사용하는 엔드포인트를 구현하는 컨트롤러 클래스를 정의하는 방법을 보여줍니다.

목록 17.11 ProductController 클래스
```java
@RestController
public class ProductController {

  @Autowired
  private ProductRepository productRepository;

  @GetMapping("/products/{text}")
  public List<Product> findProductsContaining(@PathVariable String text) {

    return productRepository.findProductByNameContains(text);
  }
}
```
애플리케이션을 시작하면 /products/{text} 엔드포인트를 호출할 때 어떤 일이 발생하는지 테스트할 수 있습니다. 사용자 Nikolai로 인증하는 동안 문자 c를 검색하면 HTTP 응답에 제품 캔디만 포함됩니다. 초콜릿에도 c가 포함되어 있어도 Julien이 소유하고 있으므로 응답에 초콜릿이 표시되지 않습니다. 다음 코드 조각에서 호출과 응답을 찾을 수 있습니다. 엔드포인트 /products를 호출하고 사용자 Nikolai로 인증하려면 다음 명령을 실행하십시오.
```
curl -u nikolai:12345 http://localhost:8080/products/c
```
The response body is
```json
[
  {"id":2,"name":"candy","owner":"nikolai"}
]
```
To call the endpoint /products and authenticate with user Julien, issue this command:
```sh
curl -u julien:12345 http://localhost:8080/products/c
```
The response body is
```json
[
  {"id":3,"name":"chocolate","owner":"julien"}
]
```
이 섹션의 앞부분에서 저장소에서 @PostFilter를 사용하는 것이 최선의 선택이 아니라고 논의했습니다. 대신 데이터베이스에서 필요하지 않은 것을 선택하지 않도록 해야 합니다. 그렇다면 선택 후 데이터를 필터링하는 대신 필요한 데이터만 선택하도록 예제를 어떻게 변경할 수 있습니까? 저장소 클래스에서 사용하는 쿼리에서 직접 SpEL 표현식을 제공할 수 있습니다. 이를 달성하기 위해 우리는 두 가지 간단한 단계를 따릅니다.

1. SecurityEvaluationContextExtension 유형의 객체를 Spring 컨텍스트에 추가합니다. 구성 클래스에서 간단한 @Bean 메소드를 사용하여 이를 수행할 수 있습니다.

2. 선택을 위한 적절한 절을 사용하여 저장소 클래스의 쿼리를 조정합니다.

프로젝트에서 컨텍스트에 SecurityEvaluationContextExtension 빈을 추가하려면 목록 17.12에 나온 대로 구성 클래스를 변경해야 합니다. 책의 예제와 관련된 모든 코드를 유지하기 위해 여기에서 ssia-ch17-ex5라는 다른 프로젝트를 사용합니다.

Listing 17.12 Adding the SecurityEvaluationContextExtension to the context
```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ProjectConfig {

  @Bean                                         ❶
  public SecurityEvaluationContextExtension 
    securityEvaluationContextExtension() {

    return new SecurityEvaluationContextExtension();
  }

    // Omitted declaration of the UserDetailsService and PasswordEncoder
}
```
❶ Spring 컨텍스트에 SecurityEvaluationContextExtension 추가
ProductRepository 인터페이스에서 메서드 앞에 쿼리를 추가하고 SpEL 표현식을 사용하여 적절한 조건으로 WHERE 절을 조정합니다. 다음 목록은 변경 사항을 나타냅니다.

목록 17.13 저장소 인터페이스의 쿼리에서 SpEL 사용
```java
public interface ProductRepository
        extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p 
    ➥ WHERE p.name LIKE %:text% AND                    ❶
          ➥ p.owner=?#{authentication.name}")
    List<Product> findProductByNameContains(String text);
}
```
❶ 쿼리에서 SpEL을 사용하여 레코드 소유자에 대한 조건 추가

이제 /products/{text} 엔드포인트를 호출하여 애플리케이션을 시작하고 테스트할 수 있습니다. 동작이 @PostFilter를 사용한 경우와 동일하게 유지되기를 기대합니다. 그러나 지금은 올바른 소유자의 레코드만 데이터베이스에서 검색되므로 기능이 더 빠르고 안정적입니다. 다음 코드 조각은 끝점에 대한 호출을 나타냅니다. 엔드포인트 /products를 호출하고 사용자 Nikolai로 인증하려면 다음 명령을 사용합니다.

```sh
curl -u nikolai:12345 http://localhost:8080/products/c
```
The response body is
```json
[
  {"id":2,"name":"candy","owner":"nikolai"}
]
```
엔드포인트 /products를 호출하고 사용자 Julien으로 인증하려면 다음 명령을 사용합니다.

```sh
curl -u julien:12345 http://localhost:8080/products/c
```
The response body is
```json
[
  {"id":3,"name":"chocolate","owner":"julien"}
]
```

## 요약

- 필터링은 프레임워크가 메서드의 입력 매개변수 또는 메서드에서 반환된 값의 유효성을 검사하고 사용자가 정의한 일부 기준을 충족하지 않는 요소를 제외하는 권한 부여 방식입니다. 권한 부여 접근 방식으로서 필터링은 메서드 실행 자체가 아니라 메서드의 입력 및 출력 값에 중점을 둡니다.

- 필터링을 사용하여 메서드가 처리하도록 승인된 값 이외의 다른 값을 가져오지 않고 메서드 호출자가 가져서는 안 되는 값을 반환할 수 없도록 합니다.

- 필터링을 사용할 때 메서드에 대한 액세스를 제한하지 않고 메서드의 매개 변수를 통해 보낼 수 있는 항목이나 메서드가 반환하는 항목을 제한합니다. 이 접근 방식을 사용하면 메서드의 입력 및 출력을 제어할 수 있습니다.

- 메서드의 매개변수를 통해 보낼 수 있는 값을 제한하려면 @PreFilter 주석을 사용합니다. @PreFilter 주석은 값이 메소드의 매개변수로 전송되도록 허용되는 조건을 수신합니다. 프레임워크는 매개변수로 제공된 컬렉션에서 주어진 규칙을 따르지 않는 모든 값을 필터링합니다.

- @PreFilter 주석을 사용하려면 메소드의 매개변수가 컬렉션 또는 배열이어야 합니다. 규칙을 정의하는 주석의 SpEL 표현식에서 filterObject를 사용하여 컬렉션 내부의 객체를 참조합니다.

- 메서드가 반환하는 값을 제한하려면 @PostFilter 주석을 사용합니다. @PostFilter 주석을 사용할 때 메서드의 반환 유형은 컬렉션 또는 배열이어야 합니다. 프레임워크는 @PostFilter 주석의 값으로 정의한 규칙에 따라 반환된 컬렉션의 값을 필터링합니다.

- Spring Data 리포지토리에서도 @PreFilter 및 @PostFilter 주석을 사용할 수 있습니다. 그러나 Spring Data 리포지토리 메서드에서 @PostFilter를 사용하는 것은 거의 좋은 선택이 아닙니다. 성능 문제를 방지하려면 이 경우 결과 필터링을 데이터베이스 수준에서 직접 수행해야 합니다.

- Spring Security는 Spring Data와 쉽게 통합되며 이를 사용하여 Spring Data 저장소의 메소드로 @PostFilter를 발행하는 것을 방지합니다.
