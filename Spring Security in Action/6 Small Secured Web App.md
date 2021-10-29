# 6 실습: 작은 보안 웹 애플리케이션

이 장에서는 다음을 다룹니다.

- 실습 예제에서 인증 적용
- UserDetails 인터페이스로 사용자 정의
- 사용자 정의 UserDetailsService 정의
- 제공된 PasswordEncoder 구현 사용
- AuthenticationProvider를 구현하여 인증 논리 정의
- 폼 로그인 인증 방식 설정

우리는 이 첫 번째 장에서 먼 길을 왔고 이미 인증에 대한 많은 세부 사항을 논의했습니다. 그러나 우리는 이러한 새로운 세부 사항을 각각 개별적으로 적용했습니다. 더 복잡한 프로젝트에서 배운 내용을 정리할 때입니다. 이 실습 예제는 지금까지 논의한 모든 구성 요소가 실제 응용 프로그램에서 함께 작동하는 방식에 대한 더 나은 개요를 갖는 데 도움이 됩니다.

## 6.1 프로젝트 요구 사항 및 설정

사용자가 인증에 성공한 후 기본 페이지에서 제품 목록을 볼 수 있는 작은 웹 애플리케이션을 구현합니다. ssia-ch6-ex1에서 제공된 프로젝트로 완전한 구현을 찾을 수 있습니다.

우리 프로젝트의 경우 데이터베이스는 이 애플리케이션의 제품과 사용자를 저장합니다. 각 사용자의 암호는 bcrypt 또는 scrypt로 해시됩니다. 예제에서 인증 로직을 사용자 지정해야 하는 이유를 제공하기 위해 두 가지 해싱 알고리즘을 선택했습니다. users 테이블의 열은 암호화 유형을 저장합니다. 세 번째 테이블은 사용자의 권한을 저장합니다.

그림 6.1은 이 애플리케이션의 인증 흐름을 설명합니다. 사용자 정의할 구성 요소를 음영 처리했습니다. 나머지는 Spring Security에서 제공하는 기본값을 사용합니다. 요청은 2장에서 5장에서 논의한 표준 인증 흐름을 따릅니다. 연속선이 있는 화살표로 다이어그램에서 요청을 나타냅니다. AuthenticationFilter는 요청을 가로채고 인증 책임을 AuthenticationManager에 위임합니다. 인증 관리자는 요청을 인증하기 위해 AuthenticationProvider를 사용합니다. AuthenticationFilter가 이를 SecurityContext에 저장할 수 있도록 성공적으로 인증된 호출의 세부 정보를 반환합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH06_F01_Spilca.png)

그림 6.1 실습 웹 애플리케이션의 인증 흐름. 사용자 지정 인증 공급자는 인증 로직을 구현합니다. 이를 위해 인증 공급자는 UserDetailsService 구현과 두 개의 PasswordEncoder 구현(요청된 각 해싱 알고리즘에 대해 하나씩)을 사용합니다. JpaUserDetailsService라고 하는 UserDetailsService 구현은 Spring Data와 JPA를 사용하여 작업하고 사용자의 세부 정보를 얻습니다.

이 예제에서 구현하는 것은 인증 공급자와 인증 로직입니다. 그림 6.1에 나와 있는 것처럼 우리는 인증 공급자를 구현하는 AuthenticationProviderService 클래스를 생성합니다. 이 구현은 데이터베이스에서 사용자 세부 정보를 찾기 위해 UserDetailsService를 호출하고 암호가 올바른지 확인하기 위해 PasswordEncoder를 호출해야 하는 인증 로직을 정의합니다. 이 애플리케이션의 경우 Spring Data JPA를 사용하여 데이터베이스와 작업하는 JpaUserDetailsService를 만듭니다. 이러한 이유로 Spring Data JpaRepository에 의존하며, UserRepository라고 명명했습니다. 응용 프로그램이 bcrypt로 해시된 암호와 scrypt로 해시된 암호의 유효성을 검사하기 때문에 두 개의 암호 인코더가 필요합니다. 간단한 웹 응용 프로그램이므로 사용자 인증을 허용하려면 표준 로그인 양식이 필요합니다. 이를 위해 formLogin을 인증 방법으로 구성합니다.

> **참고** 일부 예제에서는 Spring Data JPA를 사용합니다. 이 접근 방식을 사용하면 Spring Security로 작업할 때 찾을 수 있는 애플리케이션에 더 가까이 다가갈 수 있습니다. 예제를 이해하기 위해 JPA 전문가가 될 필요는 없습니다. Spring Data 및 JPA 관점에서 사용 사례를 간단한 구문으로 제한하고 Spring Security에 중점을 둡니다. 그러나 Hibernate와 같은 JPA 및 JPA 구현에 대해 더 자세히 알고 싶다면 Christian Bauer et al.이 작성한 Java Persistence with Hibernate, 2nd ed.를 읽는 것이 좋습니다. (매닝, 2015). Spring Data에 대한 훌륭한 토론을 위해 Craig Walls의 Spring in Action, 5th ed를 읽을 수 있습니다. (매닝, 2018).

응용 프로그램에는 사용자가 로그인에 성공한 후 액세스할 수 있는 기본 페이지도 있습니다. 이 페이지는 데이터베이스에 저장된 제품에 대한 세부 정보를 표시합니다. 그림 6.2에서 우리가 만드는 구성 요소를 음영 처리했습니다. 메인 페이지에 대한 요청에 따라 애플리케이션이 실행하는 작업을 정의하는 MainPageController가 필요합니다. MainPageController는 메인 페이지에 사용자의 이름을 표시하므로 이것이 SecurityContext에 의존하는 이유입니다. 보안 컨텍스트에서 사용자 이름을 가져오고 ProductService에서 표시할 제품 목록을 가져옵니다. ProductService는 표준 Spring Data JPA 저장소인 ProductRepository를 사용하여 데이터베이스에서 제품 목록을 가져옵니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH06_F02_Spilca.png)

그림 6.2 MainPageController는 애플리케이션의 메인 페이지에 대한 요청을 처리합니다. 표시하려면

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH06_F03_Spilca.png)

그림 6.3 데이터베이스의 엔터티 관계 다이어그램(ERD). USER 테이블에는 사용자 이름, 암호 및 암호를 해시하는 데 사용되는 알고리즘이 저장됩니다. 또한 사용자는 AUTHORITY 테이블에 저장된 하나 이상의 권한을 가지고 있습니다. PRODUCT 테이블은 제품 레코드의 세부 정보(이름, 가격, 통화)를 저장합니다. 기본 페이지에는 이 테이블에 저장된 모든 제품의 세부 정보가 표시됩니다.

이 프로젝트를 구현하기 위해 취하는 주요 단계는 다음과 같습니다.

1. 데이터베이스 설정
2. 사용자 관리 정의
3. 인증 로직 구현
4. 메인 페이지 구현
5. 애플리케이션 실행 및 테스트

먼저 테이블을 생성해야 합니다. 내가 사용하는 데이터베이스의 이름은 spring입니다. 먼저 명령줄 도구나 클라이언트를 사용하여 데이터베이스를 만들어야 합니다. 이 책의 예제와 같이 MySQL을 사용하는 경우 MySQL Workbench를 사용하여 데이터베이스를 만들고 결국에는 스크립트를 실행할 수 있습니다. 그러나 나는 Spring Boot가 데이터베이스 구조를 생성하고 거기에 데이터를 추가하는 스크립트를 실행하도록 하는 것을 선호합니다. 이렇게 하려면 프로젝트의 리소스 폴더에 schema.sql 및 data.sql 파일을 생성해야 합니다. schema.sql 파일에는 데이터베이스 구조를 생성하거나 변경하는 모든 쿼리가 포함되어 있고 data.sql 파일에는 데이터와 함께 작동하는 모든 쿼리가 저장되어 있습니다. 목록 6.1, 6.2 및 6.3은 애플리케이션에서 사용하는 세 개의 테이블을 정의합니다.

user 테이블의 필드는 다음과 같습니다.

- id--자동 증분으로 정의된 테이블의 기본 키를 나타냅니다.
- 사용자 이름--사용자 이름을 저장합니다.
- 암호 -- 암호 해시를 저장합니다(bcrypt 또는 scrypt).
- algorithm -- BCRYPT 또는 SCRYPT 값을 저장하고 현재 레코드에 대한 암호의 해싱 방법을 결정합니다.

목록 6.1은 사용자 테이블의 정의를 제공합니다. 이 스크립트를 수동으로 실행하거나 schema.sql 파일에 추가하여 프로젝트가 시작될 때 Spring Boot가 실행하도록 할 수 있습니다.

Listing 6.1 Script for creating the user table
```sql
CREATE TABLE IF NOT EXISTS `spring`.`user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(45) NOT NULL,
  `password` TEXT NOT NULL,
  `algorithm` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`));
  ```
The fields of the authority table are

- id--Represents the primary key of the table that’s defined as auto-increment
- name--Represents the name of the authority
- user--Represents the foreign key to the user table

Listing 6.2 provides the definition of the authority table. You can run this script manually or add it to the schema.sql file to let Spring Boot run it when the project starts.

Listing 6.2 Script for creating the authority table
```sql
CREATE TABLE IF NOT EXISTS `spring`.`authority` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `user` INT NOT NULL,
  PRIMARY KEY (`id`));
```
The third table is named product. It stores the data that’s displayed after the user successfully logs in. The fields of this table are

- id--Represents the primary key of the table that’s defined as auto-increment
- name--Represents the name of the product, which is a string
- price--Represents the price of the product, which is a double
- currency--Represents the currency (for example, USD, EUR, and so on), which is a string

Listing 6.3 provides the definition of the product table. You can run this script manually or add it to the schema.sql file to let Spring Boot run it when the project starts.

Listing 6.3 Script for creating the product table
```sql
CREATE TABLE IF NOT EXISTS `spring`.`product` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `price` VARCHAR(45) NOT NULL,
  `currency` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`));
```
> NOTE It is advisable to have a many-to-many relationship between the authorities and the users. To keep the example simpler from the point of view of the persistence layer and focus on the essential aspects of Spring Security, I decided to make this one-to-many.

Let’s add some data that we can use to test our application. You can run these INSERT queries manually or add them to the data.sql file in the resources folder of your project to allow Spring Boot to run them when you start the application:
```sql
INSERT IGNORE INTO `spring`.`user` (`id`, `username`, `password`, `algorithm`) VALUES ('1', 'john', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'BCRYPT');
  
INSERT IGNORE INTO `spring`.`authority` (`id`, `name`, `user`) VALUES ('1', 'READ', '1');
INSERT IGNORE INTO `spring`.`authority` (`id`, `name`, `user`) VALUES ('2', 'WRITE', '1');

INSERT IGNORE INTO `spring`.`product` (`id`, `name`, `price`, `currency`) VALUES ('1', 'Chocolate', '10', 'USD');
```
In this code snippet, for user John, the password is hashed using bcrypt. The raw password is 12345.

> NOTE It’s common to use the schema.sql and data.sql files in examples. In a real application, you can choose a solution that allows you to also version the SQL scripts. You’ll find this often done using a dependency like Flyway (https://flywaydb.org/) or Liquibase (https://www.liquibase.org/).

Now that we have a database and some test data, let’s start with the implementation. We create a new project, and add the following dependencies, which are presented in listing 6.4:

- spring-boot-starter-data-jpa--Connects to the database using Spring Data
- spring-boot-starter-security--Lists the Spring Security dependencies
- spring-boot-starter-thymeleaf--Adds Thymeleaf as a template engine to simplify the definition of the web page
- spring-boot-starter-web--Lists the standard web dependencies
- mysql-connector-java--Implements the MySQL JDBC driver

Listing 6.4 Dependencies needed for the development of the example project
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
   <artifactId>spring-boot-starter-thymeleaf</artifactId>
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
The application.properties file needs to declare the database connectivity parameters like so:
```yaml
spring.datasource.url=jdbc:mysql://localhost/➥ spring?useLegacyDatetimeCode=false&serverTimezone=UTC
spring.datasource.username=<your_username>
spring.datasource.password=<your_password>
spring.datasource.initialization-mode=always
```
> 참고 이 주제에 대해 반복할 수 있지만 절대 비밀번호를 노출하지 마십시오! 이 예에서는 문제가 없지만 실제 시나리오에서는 민감한 데이터를 application.properties 파일에 자격 증명이나 개인 키로 쓰지 않아야 합니다. 대신 이 용도로 비밀 금고를 사용하십시오.

## 6.2 사용자 관리 구현

이 섹션에서는 응용 프로그램의 사용자 관리 부분을 구현하는 방법에 대해 설명합니다. Spring Security와 관련된 사용자 관리의 대표적인 컴포넌트는 UserDetailsService이다. Spring Security에 사용자의 세부 정보를 검색하는 방법을 지시하려면 최소한 이 계약을 구현해야 합니다.

이제 프로젝트가 준비되고 데이터베이스 연결이 구성되었으므로 애플리케이션 보안과 관련된 구현에 대해 생각할 때입니다. 사용자 관리를 처리하는 애플리케이션의 이 부분을 빌드하기 위해 수행해야 하는 단계는 다음과 같습니다.

1. 두 해싱 알고리즘에 대한 암호 인코더 개체를 정의합니다.
2. 인증 프로세스에 필요한 세부 정보를 저장하는 사용자 및 권한 테이블을 나타내도록 JPA 엔터티를 정의합니다.
3. Spring Data에 대한 JpaRepository 계약을 선언합니다. 이 예에서는 사용자를 직접 참조하기만 하면 되므로 UserRepository라는 리포지토리를 선언합니다.
4. 사용자 JPA 엔터티에 대해 UserDetails 계약을 구현하는 데코레이터를 만듭니다. 여기에서는 섹션 3.2.5에서 논의된 책임을 분리하는 접근 방식을 사용합니다.
5. UserDetailsService 계약을 구현합니다. 이를 위해 JpaUserDetailsService라는 클래스를 생성합니다. 이 클래스는 3단계에서 만든 UserRepository를 사용하여 데이터베이스에서 사용자에 대한 세부 정보를 얻습니다. JpaUserDetailsService가 사용자를 찾으면 4단계에서 정의한 데코레이터의 구현으로 사용자를 반환합니다.

먼저 사용자와 비밀번호 관리를 고려합니다. 우리는 앱이 비밀번호를 해시하기 위해 사용하는 알고리즘이 bcrypt와 scrypt라는 것을 예제의 요구 사항에서 알고 있습니다. 다음 목록과 같이 구성 클래스를 생성하고 이 두 암호 인코더를 빈으로 선언하는 것으로 시작할 수 있습니다.

Listing 6.5 Registering a bean for each PasswordEncoder
```java
@Configuration
public class ProjectConfig {

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SCryptPasswordEncoder sCryptPasswordEncoder() {
    return new SCryptPasswordEncoder();
  }
}
```
UserDetailsService 구현을 선언해야 합니다. UserDetails 인터페이스의 구현으로 사용자를 반환해야 하며 인증을 위해 두 개의 JPA 엔터티인 User 및 Authority를 구현해야 합니다. 목록 6.6은 사용자를 정의하는 방법을 보여줍니다. 기관 엔터티와 일대다 관계가 있습니다.

Listing 6.6 The User entity class
```java
@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String username;
  private String password;

  @Enumerated(EnumType.STRING)
  private EncryptionAlgorithm algorithm;

  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
  private List<Authority> authorities;

  // Omitted getters and setters
}
```
The EncryptionAlgorithm is an enum defining the two supported hashing algorithms as specified in the request:
```java
public enum EncryptionAlgorithm {
    BCRYPT, SCRYPT
}
```
The following listing shows how to implement the Authority entity. It has a many-to-one relationship with the User entity.

Listing 6.7 The Authority entity class
```java
@Entity
public class Authority {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;

  @JoinColumn(name = "user")
  @ManyToOne
  private User user;

  // Omitted getters and setters
}
```
A repository must be declared to retrieve users by their names from the database. The following listing shows how to do this.
Listing 6.8 The definition of the Spring Data repository for the User entity
```java
public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findUserByUsername(String u);       ❶
}
```
❶ 쿼리 작성은 필수가 아닙니다. Spring Data는 필요한 쿼리에서 메소드의 이름을 번역합니다.

여기에서 Spring Data JPA 저장소를 사용합니다. 그러면 Spring Data는 인터페이스에 선언된 메소드를 구현하고 그 이름을 기반으로 쿼리를 실행합니다. 이 메서드는 이름이 매개변수로 제공된 User 엔터티를 포함하는 Optional 인스턴스를 반환합니다. 그러한 사용자가 데이터베이스에 없으면 메서드는 빈 Optional 인스턴스를 반환합니다.
UserDetailsService에서 사용자를 반환하려면 사용자가 UserDetails임을 나타내야 합니다. 다음 목록에서 CustomUserDetails 클래스는 UserDetails 인터페이스를 구현하고 User 엔터티를 래핑합니다.

목록 6.9 UserDetails 계약의 구현
```java
public class CustomUserDetails implements UserDetails {

  private final User user;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  // Omitted code

  public final User getUser() {
    return user;
  }
}
```
The CustomUserDetails class implements the methods of the UserDetails interface. The following listings shows how this is done.

Listing 6.10 Implementing the remaining methods of the UserDetails interface
```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
  return user.getAuthorities().stream()
    .map(a -> new SimpleGrantedAuthority(
                     a.getName()))          ❶
    .collect(Collectors.toList());          ❷
}

@Override
public String getPassword() {
  return user.getPassword();
}

@Override

public String getUsername() {
  return user.getUsername();
}

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
```
❶ 사용자의 데이터베이스에서 찾은 각 권한 이름을 SimpleGrantedAuthority에 매핑합니다.

❷ SimpleGrantedAuthority의 모든 인스턴스를 수집하여 목록으로 반환

> 참고 목록 6.10에서 저는 GrantedAuthority 인터페이스의 직접적인 구현인 SimpleGrantedAuthority를 사용합니다. Spring Security는 이 구현을 제공합니다.

이제 Listing 6.11처럼 보이도록 UserDetailsService를 구현할 수 있습니다. 애플리케이션이 사용자 이름으로 사용자를 찾으면 CustomUserDetails 인스턴스에서 User 유형의 인스턴스를 래핑하고 반환합니다. 사용자가 존재하지 않는 경우 서비스는 UsernameNotFoundException 유형의 예외를 발생시켜야 합니다.

Listing 6.11 The implementation of the UserDetailsService contract
```java
@Service
public class JpaUserDetailsService implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  public CustomUserDetails loadUserByUsername(String username) {
    Supplier<UsernameNotFoundException> s = ❶
            () -> new UsernameNotFoundException(
                  "Problem during authentication!");

    User u = userRepository
               .findUserByUsername(username) ❷
               .orElseThrow(s); ❸

    return new CustomUserDetails(u); ❹
  }
}
```
❶ 예외 인스턴스를 생성하기 위해 공급자를 선언합니다.

❷ 사용자가 포함된 Optional 인스턴스를 반환하거나 사용자가 없는 경우 빈 Optional을 반환합니다.

❸ Optional 인스턴스가 비어 있으면 정의된 Supplier가 생성한 예외를 던집니다. 그렇지 않으면 User 인스턴스를 반환합니다.

❹ 사용자 인스턴스를 CustomUserDetails 데코레이터로 래핑하고 반환합니다.

## 6.3 사용자 정의 인증 로직 구현

사용자 및 비밀번호 관리가 완료되면 사용자 정의 인증 로직 작성을 시작할 수 있습니다. 이렇게 하려면 AuthenticationProvider(목록 6.12)를 구현하고 이를 Spring Security 인증 아키텍처에 등록해야 합니다. 인증 논리를 작성하는 데 필요한 종속성은 UserDetailsService 구현과 두 개의 암호 인코더입니다. 이것들을 자동 연결하는 것 외에, authenticate() 및 support() 메서드를 재정의합니다. 지원되는 인증 구현 유형이 UsernamePasswordAuthenticationToken임을 지정하기 위해 support() 메서드를 구현합니다.

Listing 6.12 Implementing the AuthenticationProvider
```java
@Service
public class AuthenticationProviderService 
  implements AuthenticationProvider {

  @Autowired                                               ❶
  private JpaUserDetailsService userDetailsService;

  @Autowired
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  private SCryptPasswordEncoder sCryptPasswordEncoder;

  @Override
  public Authentication authenticate(
    Authentication authentication) 
      throws AuthenticationException {
        // ...
  }

  @Override
  public boolean supports(Class<?> aClass) {
    return return UsernamePasswordAuthenticationToken.class
        .isAssignableFrom(aClass);
  }
}
```
❶ UserDetailsService 및 두 개의 PasswordEncoder 구현인 필요한 종속성을 주입합니다.

authenticate() 메서드는 먼저 사용자 이름으로 사용자를 로드한 다음 암호가 데이터베이스에 저장된 해시와 일치하는지 확인합니다(목록 6.13). 확인은 사용자의 비밀번호를 해시하는 데 사용되는 알고리즘에 따라 다릅니다.

Listing 6.13 authenticate()를 재정의하여 인증 로직 정의하기
```java
@Override
public Authentication authenticate(Authentication authentication) 
    throws AuthenticationException {

  String username = authentication.getName();
  String password = authentication
                     .getCredentials()
                     .toString();

  CustomUserDetails user = ❶
        userDetailsService.loadUserByUsername(username);

  switch (user.getUser().getAlgorithm()) { ❷
    case BCRYPT: ❸
        return checkPassword(user, password, bCryptPasswordEncoder);
    case SCRYPT: ❹
        return checkPassword(user, password, sCryptPasswordEncoder);
  }

  throw new BadCredentialsException("Bad credentials");
}
```
❶ UserDetailsService를 사용하여 데이터베이스에서 사용자 세부 정보를 찾습니다.

❷ 사용자 고유의 해싱 알고리즘에 따라 비밀번호 유효성 검사

❸ bcrypt가 사용자의 비밀번호를 해시하면 BCryptPasswordEncoder를 사용합니다.

❹ 그렇지 않으면 SCryptPasswordEncoder를 사용합니다.

목록 6.13에서 사용자의 algorithm 속성 값을 기반으로 암호의 유효성을 검사하는 데 사용할 PasswordEncoder를 선택합니다. 목록 6.14에서 checkPassword() 메서드의 정의를 찾을 수 있습니다. 이 방법은 매개변수로 전송된 암호 인코더를 사용하여 사용자 입력에서 받은 원시 암호가 데이터베이스의 인코딩과 일치하는지 확인합니다. 암호가 유효하면 인증 계약 구현의 인스턴스를 반환합니다. UsernamePasswordAuthenticationToken 클래스는 인증 인터페이스의 구현입니다. 목록 6.14에서 호출한 생성자는 인증된 값도 true로 설정합니다. 이 세부 정보는 AuthenticationProvider의 authenticate() 메서드가 인증된 인스턴스를 반환해야 한다는 것을 알고 있기 때문에 중요합니다.

Listing 6.14 인증 로직에 사용되는 checkPassword() 메소드
```java
private Authentication checkPassword(CustomUserDetails user, 
                                     String rawPassword, 
                                     PasswordEncoder encoder) {

  if (encoder.matches(rawPassword, user.getPassword())) {
    return new UsernamePasswordAuthenticationToken(
                      user.getUsername(), 
                      user.getPassword(), 
                      user.getAuthorities());
  } else {
    throw new BadCredentialsException("Bad credentials");
  }
}
```
Now we need to register the AuthenticationProvider within the configuration class. The next listing shows how to do this.

Listing 6.15 Registering the AuthenticationProvider in the configuration class
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Autowired ❶
  private AuthenticationProviderService authenticationProvider;

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SCryptPasswordEncoder sCryptPasswordEncoder() {
    return new SCryptPasswordEncoder();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(
           authenticationProvider); ❷
  }
}
```
❶ 컨텍스트에서 AuthenticationProviderService의 인스턴스를 가져옵니다.

❷ configure() 메소드를 재정의하여 Spring Security용 인증 제공자를 등록

구성 클래스에서 다음 목록과 같이 formLogin 메서드에 대한 인증 구현과 /main 경로를 기본 성공 URL로 설정하려고 합니다. 이 경로를 웹 애플리케이션의 기본 페이지로 구현하려고 합니다.

Listing 6.16 인증 방법으로 formLogin 구성하기
```java
@Override
protected void configure(HttpSecurity http) 
  throws Exception {

  http.formLogin()
      .defaultSuccessUrl("/main", true);

  http.authorizeRequests()
      .anyRequest().authenticated();
}
```

## 6.4 메인 페이지 구현

마지막으로 보안 부분이 준비되었으므로 앱의 기본 페이지를 구현할 수 있습니다. product 테이블의 모든 기록을 표시하는 간단한 페이지입니다. 이 페이지는 사용자가 로그인한 후에만 액세스할 수 있습니다. 데이터베이스에서 product 레코드를 가져오려면 프로젝트에 Product 엔터티 클래스와 ProductRepository 인터페이스를 추가해야 합니다. 다음 목록은 Product 클래스를 정의합니다.

Listing 6.17 Definining the Product JPA entity
```java
@Entity
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;
  private double price;

  @Enumerated(EnumType.STRING)
  private Currency currency;

  // Omitted code
}
```
The Currency enumeration declares the types allowed as currencies in the application. For example;
```java
public enum Currency {
  USD, GBP, EUR
}
```
The ProductRepository interface only has to inherit from JpaRepository. Because the application scenario asks to display all the products, we need to use the findAll() method that we inherit from the JpaRepository interface, as shown in the next listing.

Listing 6.18 Definition of the ProductRepository interface
```java
public interface ProductRepository 
  extends JpaRepository<Product, Integer> {
} ❶
```
❶ The interface doesn’t need to declare any methods. We only use the methods inherited from the JpaRepository interface implemented by Spring Data.
The ProductService class uses the ProductRepository to retrieve all the products from the database.

Listing 6.19 Implementation of the ProductService class
```java
@Service
public class ProductService {

  @Autowired
  private ProductRepository productRepository;

  public List<Product> findAll() {
    return productRepository.findAll();
  }
}
```
In the end, a MainPageController defines the path for the page and fills the Model object with what the page will display.
Listing 6.20 The definition of the controller class
```java
@Controller
public class MainPageController {

  @Autowired
  private ProductService productService;

  @GetMapping("/main")
  public String main(Authentication a, Model model) {
    model.addAttribute("username", a.getName());
    model.addAttribute("products", productService.findAll());
    return "main.html";
  }
}
```
The main.html page is stored in the resources/templates folder and displays the products and the name of the logged-in user.

Listing 6.21 The definition of the main page
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">       ❶
  <head>
    <meta charset="UTF-8">
    <title>Products</title>
  </head>
  <body>
    <h2 th:text="'Hello, ' + ${username} + '!'" />         ❷
    <p><a href="/logout">Sign out here</a></p>

    <h2>These are all the products:</h2>
    <table>
      <thead>
        <tr>
          <th> Name </th>
          <th> Price </th>
        </tr>
      </thead>
      <tbody>
        <tr th:if="${products.empty}">                    ❸
          <td colspan="2"> No Products Available </td>
        </tr>
        <tr th:each="book : ${products}">                 ❹
          <td><span th:text="${book.name}"> Name </span></td>
          <td><span th:text="${book.price}"> Price </span></td>
        </tr>
      </tbody>
    </table>
  </body>
</html>
```
❶ 페이지에서 Thymeleaf 구성 요소를 사용할 수 있도록 접두사 th를 선언합니다.

❷ 페이지에 이 메시지를 표시합니다. 컨트롤러 액션 실행 후 ${username}은 모델에서 페이지로 주입되는 변수입니다.

❸ 모델 목록에 제품이 없을 경우 메시지를 표시합니다.
❹ 모델 목록에서 찾은 각 제품에 대해 테이블에 행 생성

## 6.5 애플리케이션 실행 및 테스트

이 책의 첫 번째 실습 프로젝트에 대한 코드 작성을 완료했습니다. 이제 사양에 따라 작동하는지 확인할 때입니다. 이제 애플리케이션을 실행하고 로그인을 시도해 보겠습니다. 애플리케이션을 실행한 후 http://localhost :8080 주소를 입력하여 브라우저에서 액세스할 수 있습니다. 표준 로그인 양식은 그림 6.4와 같이 나타납니다. 내가 데이터베이스에 저장한 사용자(그리고 이 장의 시작 부분에 제공된 스크립트에 있는 사용자)는 bcrypt를 사용하여 해시된 암호 12345를 가진 John입니다. 이 자격 증명을 사용하여 로그인할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH06_F04_Spilca.png)

그림 6.4 애플리케이션의 로그인 양식

> **참고** 실제 응용 프로그램에서는 사용자가 "12345"와 같은 간단한 암호를 정의하도록 허용해서는 안 됩니다. 너무 단순한 암호는 추측하기 쉽고 보안 위험을 나타냅니다. Wikipedia는 https://en.wikipedia.org/wiki/Password_strength에서 암호에 대한 유익한 기사를 제공합니다. 강력한 암호를 설정하는 규칙뿐만 아니라 암호 강도를 계산하는 방법도 설명합니다.

로그인하면 애플리케이션이 기본 페이지로 리디렉션합니다(그림 6.5). 여기에서 보안 컨텍스트에서 가져온 사용자 이름이 데이터베이스의 제품 목록과 함께 페이지에 나타납니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH06_F05_Spilca.png)

그림 6.5 애플리케이션의 메인 페이지

여기에서 로그아웃 링크를 클릭하면 애플리케이션이 표준 로그아웃 확인 페이지로 리디렉션합니다(그림 6.6). 이것은 formLogin 인증 방법을 사용하기 때문에 Spring Security에서 미리 정의합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH06_F06_Spilca.png)

그림 6.6 표준 로그아웃 확인 페이지

로그아웃을 클릭한 후 초콜릿을 더 주문하려면 로그인 페이지로 다시 리디렉션됩니다(그림 6.7).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH06_F07_Spilca.png)

그림 6.7 애플리케이션에서 로그아웃하면 로그인 페이지가 나타납니다.

축하합니다! 첫 번째 실습 예제를 구현했고 이 책에서 이미 논의한 몇 가지 필수 사항을 정리했습니다. 이 예제에서는 Spring Security로 인증을 관리하는 작은 웹 애플리케이션을 개발했습니다. 폼 로그인 인증 방식을 사용하고 사용자 정보를 데이터베이스에 저장했습니다. 또한 사용자 지정 인증 논리를 구현했습니다.
이 장을 닫기 전에 한 가지 더 관찰하고 싶습니다. 다른 소프트웨어 요구 사항과 마찬가지로 동일한 응용 프로그램을 다른 방식으로 구현할 수 있습니다. 앞에서 논의한 가능한 한 많은 것들을 다루기 위해 이 구현을 선택했습니다. 주로 사용자 정의 AuthenticationProvider를 구현해야 하는 이유를 갖고 싶었습니다. 연습으로 4장에서 논의한 것처럼 DelegatingPasswordEncoder를 사용하여 구현을 단순화합니다.

## 요약

- 실제 응용 프로그램에서는 동일한 개념의 다른 구현이 필요한 종속성을 갖는 것이 일반적입니다. 이것은 우리의 경우와 같이 Spring Security의 UserDetails 및 JPA 구현의 User 엔티티일 수 있습니다. 이에 대한 좋은 권장 사항은 가독성을 높이기 위해 다른 클래스의 책임을 분리하는 것입니다.

- 대부분의 경우 동일한 기능을 구현하는 여러 가지 방법이 있습니다. 가장 간단한 솔루션을 선택해야 합니다. 코드를 더 쉽게 이해할 수 있도록 하면 오류 및 보안 침해의 여지가 줄어듭니다.