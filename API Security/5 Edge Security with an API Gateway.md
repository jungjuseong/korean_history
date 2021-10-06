# 5. Edge Security with an API Gateway

`API 게이트웨이`는 프로덕션 배포에서 API를 보호하는 가장 일반적인 패턴이다. 즉, API 배포의 진입점이다. 우리가 일반적으로 API 게이트웨이로 식별하는 API 게이트웨이 패턴을 구현하는 많은 오픈 소스 및 독점 제품이 있다. API 게이트웨이는 인증, 권한 부여 및 조절 정책을 중앙에서 시행하는 정책 시행 지점(PEP)이다. 또한 API 게이트웨이를 사용하여 API와 관련된 모든 분석을 중앙에서 수집하고 추가 분석 및 프레젠테이션을 위해 분석 제품에 게시할 수 있다

## Setting Up Zuul API Gateway

`Zuul`은 동적 라우팅, 모니터링, 탄력성, 보안 등을 제공하는 API 게이트웨이(그림 5-1 참조)입니다. Netflix 서버 인프라의 정문 역할을 하며 전 세계 모든 Netflix 사용자의 트래픽을 처리한다. 또한 요청을 라우팅하고, 개발자의 테스트 및 디버깅을 지원하고, Netflix의 전반적인 서비스 상태에 대한 깊은 통찰력을 제공하고, 공격으로부터 Netflix 배포를 보호하고, AWS 리전에 문제가 있을 때 트래픽을 다른 클라우드 리전으로 보낸다. 이 섹션에서는 3장에서 개발한 `Order` API 앞에 `Zuul`을 API 게이트웨이로 설정한다.

이 책에 사용된 모든 샘플은 `https://github.com/apisecurity/samples.git` 저장소에서 사용할 수 있다.

```
\> git clone https://github.com/apisecurity/samples.git
\> cd samples/ch05
```

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_5_Fig1_HTML.jpg)

Figure 5-1 A typical `Zuul` API gateway deployment at Netflix. All the Netflix microservices are fronted by an API gateway

### Running the Order API

This is the simplest API implementation ever, which is developed with `Spring Boot`. In fact one can call it as a microservice as well. You can find the code inside the directory, `ch05/sample01`. To build the project with Maven, use the following command from the sample01 directory:
```
\> cd sample01
\> mvn clean install
```
Now, let’s see how to run our `Spring Boot` service and talk to it with a cURL client. Execute the followings from `ch05/sample01` directory to start the `Spring Boot` service with Maven.
```
\> mvn spring-boot:run
```
To test the API with a cURL client, use the following command from a different command console. It will print the output as shown in the following, after the initial command.
```
\> curl http://localhost:8080/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```
### Running the Zuul API Gateway

이 섹션에서는 `Zuul API 게이트웨이`를 `Spring Boot` 프로젝트로 빌드하고 `Order` 서비스에 대해 실행할 것이다. 즉, `Zuul 게이트웨이`는 Order 서비스에 대한 모든 요청을 프록시한다. 코드는 `ch05/sample02` 디렉토리에서 찾을 수 있습니다. Maven으로 프로젝트를 빌드하려면 다음 명령을 사용한다.

```
\> cd sample02
\> mvn clean install
```

코드를 자세히 살펴보기 전에 `ch05/sample02/pom.xml`에 추가된 몇 가지 주목할만한 `maven` 종속성과 플러그인을 살펴보겠습니다.

```
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-zuul</artifactId>
</dependency>
```

`GatewayApplication`는 `Zuul API 게이트웨이`를 구동하는 클래스입니다. `server.port=9000`을 `application.properties` 파일에 추가하여 API 게이트웨이 포트가 `9000`으로 설정됩니다. 다음은 API 게이트웨이를 스핀업하는 `GatewayApplication` 클래스의 코드를 보여줍니다. `@EnableZuulProxy` 주석은 `Spring` 프레임워크가 앱을 `Zuul 프록시`로 시작하도록 지시합니다.

```
@EnableZuulProxy
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

이제 API 게이트웨이를 시작하고 `cURL` 클라이언트와 통신하는 방법을 살펴보겠습니다. `ch05/sample02` 디렉토리에서 실행되는 다음 명령어는 Maven으로 API 게이트웨이를 시작하는 방법을 보여준다. `Zuul' API 게이트웨이`도 또 다른 `Spring Boot` 애플리케이션이기 때문에 시작하는 방법은 이전에 `Order` 서비스에서 했던 방법과 동일합니다.

```
\> mvn spring-boot:run
```

이제 `Zuul API 게이트웨이`를 통해 프록시되는 `Order` API를 테스트하기 위해 다음을 사용합시다. 또한 주문 서비스가 여전히 포트 `8080`에서 실행 중인지 확인한다. 여기에 `Retail`이라는 새 컨텍스트를 추가하고(직접 API 호출에서는 볼 수 없음) API 게이트웨이가 실행 중인 포트 `9090`과 통신한다.

```
\> curl http://localhost:9090/retail/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```

### What Happens Underneath?

API 게이트웨이는 `retail` 컨텍스트에 대한 요청을 수신하면 요청을 백엔드 API로 라우팅한다. 이러한 라우팅 지침은 다음과 같이 `application.properties` 파일에 설정됩니다. `retail` 대신 다른 컨텍스트를 사용하려면 속성 키를 적절하게 변경해야 한다.

```
zuul.routes.retail.url=http://localhost:8080
```

## Enabling TLS for the Zuul API Gateway

 이 섹션에서는 `Zuul` API 게이트웨이에서 TLS를 활성화하는 방법을 살펴보겠습니다. 3장에서 TLS로 Order 서비스를 보호하는 방법에 대해 논의했다. Order 서비스는 `Spring Boot` 앱이며 `Zuul`도 `Spring Boot` 앱이기 때문에 TLS로 `Zuul` API 게이트웨이를 보호하기 위해 동일한 프로세스를 따릅니다.

`TLS`를 활성화하려면 먼저 공개/개인 키 쌍을 생성해야 한다. 다음 명령은 기본 Java 배포와 함께 제공되는 `keytool`을 사용하여 키 쌍을 생성하고 `keystore.jks` 파일에 저장한다. `sample02` 디렉토리에 있는 `keystore.jks` 파일을 그대로 사용하려면 이 단계를 건너뛸 수 있다. 3장에서는 다음 명령의 각 매개변수가 의미하는 바를 자세히 설명한다.

```
\> keytool -genkey -alias spring -keyalg RSA -keysize 4096 -validity 3650 -dname "CN=zool,OU=bar,O=zee,L=sjc,S=ca,C=us" -keypass springboot -keystore keystore.jks -storeType jks -storepass springboot
```

`Zuul` API 게이트웨이에 대해 `TLS`를 활성화하려면 앞서 만든 키 저장소 파일(`keystore.jks`)을 게이트웨이의 홈 디렉토리(예: `ch05/sample02/`)에 복사한다.

```
server.ssl.key-store: keystore.jks
server.ssl.key-store-password: springboot
server.ssl.keyAlias: spring
```

모든 것이 제대로 작동하는지 확인하려면 `ch05/sample02/` 디렉토리에서 다음 명령을 사용하여 `Zuul API` 게이트웨이를 가동하고 `HTTPS` 포트를 프린트하는 행을 확인하십시오. 이전 연습에서 실행 중인 `Zuul` 게이트웨이가 이미 있는 경우 먼저 종료하십시오.

```
\> mvn spring-boot:run

Tomcat started on port(s): 9090 (https) with context path "
```

이미 이전 섹션에서 실행 중인 `Order` 서비스가 있다고 가정하고 다음 `cURL` 명령을 실행하여 `HTTPS`를 통해 `Zuul` 게이트웨이를 통해 `Order` 서비스에 액세스한다.

```
\> curl –k https://localhost:9090/retail/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```

이전 `cURL` 명령에서 `-k` 옵션을 사용했습니다. `HTTPS` 끝점을 보호하기 위해 자체 서명된(신뢰할 수 없는) 인증서가 있으므로 `cURL`이 신뢰 유효성 검사를 무시하도록 조언하는 `–k` 매개변수를 전달해야 한다. 적절한 인증 기관 서명 인증서가 있는 프로덕션 배포에서는 그렇게 할 필요가 없습니다. 또한 자체 서명된 인증서가 있는 경우 cURL이 해당 공용 인증서를 가리키도록 하여 `-k`를 사용하는 것을 피할 수 있다.

```
\> curl --cacert ca.crt https://localhost:9090/retail/order/11
```

`ch05/sample02/`에서 `keytool` 명령을 사용하여 `Zuul` 게이트웨이의 공개 인증서를 PEM(`-rfc` 인수 포함) 형식의 `ca.crt` 파일로 내보낼 수 있다.

```
\> keytool -export -file ca.crt -alias spring –rfc -keystore keystore.jks -storePass springboot
```

앞의 명령은 다음 오류를 발생시킵니다. 이것은 인증서의 공통 이름(`zool`)이 `cURL` 명령의 호스트 이름(`localhost`)과 일치하지 않는다고 불평한다

```
curl: (51) SSL: certificate subject name 'zool' does not match target host name 'localhost'
```

이상적으로는 프로덕션 배포에서 인증서를 생성할 때 일반 이름이 호스트 이름과 일치해야 한다. 이 경우 `zool` 호스트 이름에 대한 DNS 항목이 없으므로 `cURL`과 함께 다음 해결 방법을 사용할 수 있다.

```
\> curl --cacert ca.crt https://zool:9090/retail/order/11 --resolve zool:9090:127.0.0.1
```

## Zuul API 게이트웨이에서 OAuth 2.0 토큰 유효성 검사 시행

이전 섹션에서 `Zuul API 게이트웨이`를 통해 API에 요청을 프록시하는 방법을 설명하였다. 거기에서 우리는 보안 강화에 대해 걱정하지 않았습니다. 이 섹션에서는 `Zuul API 게이트웨이`에서 OAuth 2.0 토큰 유효성 검사를 적용하는 방법에 대해 설명한다. 그 이유는 토큰을 발행하기 위해 OAuth 2.0 인증 서버(보안 토큰 서비스라고도 부를 수 있음)가 있어야 하고, `Zuul API 게이트웨이`에서 OAuth 토큰 유효성 검사를 시행해야 한다(그림 5-2 참조).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_5_Fig2_HTML.jpg)

Figure 5-2 The Zuul API gateway intercepts all the requests going to the `Order` API and validates OAuth 2.0 access tokens against the authorization server (STS)

### OAuth 2.0 보안 토큰 서비스(STS) 설정

`보안 토큰 서비스`의 책임은 클라이언트에 토큰을 발급하고 `API 게이트웨이`의 유효성 검사 요청에 응답하는 것입니다. `WSO2 Identity Server`, `Keycloak`, `Gluu` 등 많은 오픈 소스 OAuth 2.0 인증 서버가 있다. 프로덕션 배포에서는 그 중 하나를 사용할 수 있지만 이 예에서는 `Spring Boot`를 사용하여 간단한 OAuth 2.0 인증 서버를 설정한다. 또 다른 마이크로 서비스이며 개발자 테스트에 매우 유용한다. 인증 서버에 해당하는 코드는 `ch05/sample03` 디렉토리에 있다.

주목할만한 Maven 종속성에 대해 `ch05/sample03/pom.xml`을 살펴보겠습니다. 이러한 종속성은 새로운 주석 세트(`@EnableAuthorizationServer` 주석 및 `@EnableResourceServer` 주석)를 도입하여 `Spring Boot` 앱을 OAuth 2.0 인증 서버로 전환한다.

```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.security.oauth</groupId>
  <artifactId>spring-security-oauth2</artifactId>
</dependency>
```
`Spring Boot` 인증 서버에 클라이언트 등록은 여러 가지 방법으로 수행할 수 있습니다. 이 예제는 `config/AuthorizationServerConfig` 클래스의 코드 자체에 클라이언트를 등록합니다. `AuthorizationServerConfig` 클래스는 `AuthorizationServerConfigurerAdapter` 클래스를 확장하여 기본 동작을 재정의합니다. 여기에서 클라이언트 ID의 값을 `10101010`으로, 클라이언트 비밀을 `11110000`으로, 사용 가능한 범위 값을 `foo` 및/또는 `bar`로, 승인된 부여 유형을 `client_credentials`, 비밀번호 및 `refresh_token`으로 설정하고, 액세스 토큰의 유효 기간은 6,000초입니다. 여기서 사용하는 대부분의 용어는 OAuth 2.0에서 가져온 것이며 4장에서 설명합니다.

`TokenServiceApp` 클래스는 프로젝트를 OAuth 2.0 인증 서버로 바꾸는 `@EnableAuthorizationServer` 주석을 전달한다. 액세스 토큰의 유효성을 검사하고 사용자 정보를 반환하기 위해 리소스 서버 역할도 해야 하므로 `@EnableResourceServer` 주석을 동일한 클래스에 추가했습니다. 여기서 용어가 약간 혼란스럽다는 것은 이해할 수 있지만 이것이 `Spring Boot`에서 토큰 유효성 검사 끝점(사실 간접적으로 토큰 유효성 검사도 수행하는 사용자 정보 끝점)을 구현하는 가장 쉬운 방법입니다. 자체 포함 JWT를 사용하는 경우 이 토큰 유효성 검사 끝점이 필요하지 않습니다. JWT를 처음 사용하는 경우 자세한 내용은 7장을 확인하십시오.

`Spring Boot` 인증 서버에 클라이언트를 등록하는 것은 여러 가지 방법으로 수행할 수 있다. 이 예제는 `ch05/sample03/config/AuthorizationServerConfig.java` 파일의 코드 자체에 클라이언트를 등록한다. `AuthorizationServerConfig` 클래스는 `AuthorizationServerConfigurerAdapter` 클래스를 확장하여 기본 동작을 재정의한다. 

여기에서 클라이언트 ID의 값을 `10101010`으로, 클라이언트 비밀을 `11110000`으로, 사용 가능한 범위 값을 `foo` 및/또는 `bar`로, 승인된 부여 유형을 `client_credentials`, 비밀번호 및 `refresh_token`으로 설정하고, 액세스 토큰의 유효 기간은 `6000`초입니다. 여기서 사용하는 대부분의 용어는 OAuth 2.0에서 가져온 것이다.

```
@Override
public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
  clients.inMemory().withClient("10101010")
    .secret("11110000")
    .scopes("foo", "bar")
    .authorizedGrantTypes("client_credentials", "password","refresh_token")
    .accessTokenValiditySeconds(6000);
}
```

패스워드 부여 유형을 지원하려면 권한 부여 서버가 사용자 저장소에 연결되어야 한다. 사용자 저장소는 사용자 자격 증명 및 속성을 저장하는 데이터베이스 또는 LDAP 서버일 수 있다.` Spring Boot`는 여러 사용자 저장소와의 통합을 지원하지만 다시 한 번 이 예제에서 가장 편리한 것은 메모리 내 사용자 저장소입니다.`config/WebSecurityConfiguration.java` 파일의 다음 코드는 `USER` 역할로 사용자를 시스템에 추가한다.

```
@Override
public void configure(AuthenticationManagerBuilder auth) throws
Exception {
    auth.inMemoryAuthentication()
      .withUser("peter").password("peter123").roles("USER");
}
```
`Spring Boot`에서 메모리 내 사용자 저장소를 정의하고 나면 다음과 같이 `config/AuthorizationServerConfig` 코드에서 OAuth 2.0 인증 흐름과 연결해야 한다. .
```
@Autowired
private AuthenticationManager authenticationManager;
@Override
public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints.authenticationManager(authenticationManager);
}
```
To start the authorization server, use the following command from `ch05/sample03/` directory to spin up the TokenService microservice, and it starts running on HTTPS port 8443.

\> mvn spring-boot:run

### Testing OAuth 2.0 Security Token Service (STS)

OAuth 2.0 `클라이언트 자격 증명 부여` 유형을 사용하여 액세스 토큰을 얻으려면 다음 명령을 사용한다. `$CLIENTID` 및 `$CLIENTSECRET` 값을 적절하게 바꾸십시오. 이 예에서 사용된 `client ID` 및 `client password`에 대한 하드 코딩된 값은 각각 `10101010` 및 `11110000`입니다. 또한 STS 엔드포인트가 TLS로 보호된다는 사실을 이미 눈치채셨을 수도 있다. TLS로 STS를 보호하기 위해 TLS로 `Zuul API 게이트웨이`를 보호하면서 이전과 동일한 프로세스를 따랐습니다.

```
\> curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=client_credentials&scope=foo" https://localhost:8443/oauth/token

{"access_token":"81aad8c4-b021-4742-93a9-e25920587c94","token_type":"bearer","expires_in":43199,"scope":"foo"}
```

> **Note**
>
> 앞의 `cURL` 명령에서 `-k` 옵션을 사용한다. `HTTPS` 끝점을 보호하기 위해 자체 서명된(신뢰할 수 없는) 인증서가 있으므로 `-k` 매개 변수를 전달하여 cURL이 신뢰 유효성 검사를 무시하도록 조언해야 한다. OAuth 2.0 6749 RFC: https://tools.ietf.org/html/rfc6749 에서 사용된 매개변수에 대한 자세한 내용을 찾을 수 있으며 4장에서도 설명한다.

`패스워드 부여` 유형을 사용하여 액세스 토큰을 얻으려면 다음 명령을 사용하십시오. `$CLIENTID`, `$CLIENTSECRET`, `$USERNAME` 및 `$PASSWORD` 값을 적절하게 바꾸십시오. 이 예에서 사용된 클라이언트 ID 및 클라이언트 암호에 대한 하드 코딩된 값은 각각 `10101010` 및 `1111000`0입니다. 사용자 이름과 비밀번호는 각각 `Peter`와 ` Peter123`을 사용한다.
```
\> curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=password&username=$USERNAME&password=$PASSWORD&scope=foo" https://localhost:8443/oauth/token

{"access_token":"69ff86a8-eaa2-4490-adda-6ce0f10b9f8b","token_type":"bearer","refresh_token":"ab3c797b-72e2-4a9a-a1c5-c550b2775f93","expires_in":43199,"scope":"foo"}
```
> **Note**
>
> OAuth 2.0 `클라이언트 자격 증명 부여` 유형과 `패스워드 부여` 유형에 대해 얻은 두 가지 응답을 주의 깊게 관찰하면 `클라이언트 자격 증명 부여` 유형 흐름에 리프레시 토큰이 없음을 알 수 있다. OAuth 2.0에서는 액세스 토큰이 만료되었거나 만료가 임박한 경우 `리프레시 토큰`을 사용하여 새 액세스 토큰을 얻습니다. 이것은 사용자가 오프라인이고 클라이언트 앱이 새 액세스 토큰을 얻기 위해 자신의 자격 증명에 액세스할 수 없고 유일한 방법은 `리프레시 토큰`을 사용하는 경우에 매우 유용하다. `클라이언트 자격 증명 부여 유형`의 경우 관련 사용자가 없으며 항상 자체 자격 증명에 액세스할 수 있으므로 새 액세스 토큰을 얻고자 할 때 언제든지 사용할 수 있다. 따라서 리프레시 토큰이 필요하지 않습니다.

이제 인증 서버와 대화하여 액세스 토큰을 확인하는 방법을 살펴보겠습니다. 리소스 서버는 일반적으로 이 작업을 수행한다. 리소스 서버에서 실행 중인 인터셉터는 요청을 가로채서 액세스 토큰을 추출한 다음 권한 부여 서버와 통신한다. 일반적인 API 배포에서 이 유효성 검사는 OAuth 권한 부여 서버에 의해 노출된 표준 끝점을 통해 발생한다. 이를 `인트로스펙션 엔드포인트`라고 하며 9장에서 OAuth 토큰 인트로스펙션에 대해 자세히 설명한다. 그러나 이 예에서는 인증 서버(또는 STS)에서 표준 자체 검사 끝점을 구현하지 않고 토큰 유효성 검사를 위해 사용자 지정 끝점을 사용한다.

다음 명령은 이전 명령에서 얻은 액세스 토큰의 유효성을 검사하기 위해 권한 부여 서버와 직접 통신하는 방법을 보여줍니다. `$TOKEN` 값을 해당 액세스 토큰으로 적절하게 대체해야 한다.

```
\> curl -k -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json"   https://localhost:8443/user

{"details":{"remoteAddress":"0:0:0:0:0:0:0:1","sessionId":null,"tokenValue":"9f3319a1-c6c4-4487-ac3b-51e9e479b4ff","tokenType":"Bearer","decodedDetails":null},"authorities":[],"authenticated":true,"userAuthentication":null,"credentials":"","oauth2Request":{"clientId":"10101010","scope":["bar"],"requestParameters":{"grant_type":"client_credentials","scope":"bar"},"resourceIds":[],"authorities":[],"approved":true,"refresh":false,"redirectUri":null,"responseTypes":[],"extensions":{},"grantType":"client_credentials","refreshTokenRequest":null},"clientOnly":true,"principal":"10101010","name":"10101010"}
```

토큰이 유효한 경우 앞의 명령은 액세스 토큰과 연결된 메타데이터를 반환한다. 응답은 다음 코드와 같이 `TokenServiceApp` 클래스의 `user()` 메소드 내부에 빌드됩니다. `@RequestMapping` 주석을 사용하여 `/user` 컨텍스트(요청에서)를 `user()` 메서드에 매핑한다.

```
@RequestMapping("/user")
user(Principal user) {
      return user;
}
```
> **Note**
>
> 기본적으로 `Spring Boot`는 확장 없이 발행된 토큰을 메모리에 저장한다. 토큰을 발급한 후 서버를 다시 시작한 다음 유효성을 검사하면 오류 응답이 발생한다.

### OAuth 2.0 토큰 유효성 검사를 위한 `Zuul API 게이트웨이` 설정

API 게이트웨이에서 토큰 유효성 검사를 시행하려면 `security.oauth2.resource.user-info-uri` 속성의 값으로 토큰의 유효성을 검사하는 데 사용되는 OAuth 2.0 보안 토큰 서비스의 끝점을 전달한다.
```
security.oauth2.resource.user-info-uri=https://localhost:8443/user
```
앞의 속성은 권한 부여 서버의 `HTTP` 엔드포인트를 가리킵니다. `Zuul 게이트웨이`와 인증 서버 간의 `HTTPS` 연결을 지원하려면 `Zuul 게이트웨이` 끝에서 한 가지 더 변경해야 한다. `Zuul 게이트웨이`와 `인증 서버` 간에 TLS 연결이 있는 경우 `Zuul 게이트웨이`는 인증 서버의 공용 인증서와 연결된 인증 기관을 신뢰해야 한다. 자체 서명된 인증서를 사용하기 때문에 인증 서버의 공인 인증서를 내보내고 `Zuul 게이트웨이`의 키 저장소로 가져와야 한다. ch05/sample03 디렉토리에서 다음 `keytool` 명령어를 사용하여 `Authorization Server`의 공인인증서를 내보내고 `ch05/sample02` 디렉토리에 복사해 봅시다. 샘플 git repo에서 키 저장소를 사용하는 경우 다음 두 `keytool` 명령을 건너뛸 수 있다.

```
\> keytool -export -alias spring -keystore keystore.jks -storePass springboot -file sts.crt
Certificate stored in file <sts.crt>
\> cp sts.crt ../sample02
```
Let’s use the following keytool command from ch05/sample02 directory to import security token service’s public certificate to Zuul gateway’s keystore.
```
\> keytool -import -alias sts -keystore keystore.jks -storePass springboot -file sts.crt
Trust this certificate? [no]:yes
Certificate was added to keystore
```

마지막으로 `@EnableResourceServer` 주석과 `GatewayApplication` 클래스에서 해당 패키지 가져오기의 주석을 제거해야 합니다. `ch05/sample02` 디렉터리에서 다음 명령을 실행하여 Zuul API 게이트웨이를 시작하겠습니다.

```
\> mvn spring-boot:run
```
To test the API, which is now proxied through the Zuul API gateway and secured with OAuth 2.0, let’s use the following cURL. It should fail, because we do not pass an OAuth 2.0 token.
```
\> curl –k https://localhost:9090/retail/order/11
```

이제 유효한 액세스 토큰으로 API를 올바르게 호출하는 방법을 살펴보겠습니다. 먼저 보안 토큰 서비스와 대화하고 액세스 토큰을 가져와야 한다. 다음 명령에서 `$CLIENTID`, `$CLIENTSECRET`, `$USERNAME` 및 `$PASSWORD` 값을 적절하게 교체해야 한다. 이 예에서 사용된 클라이언트 ID 및 클라이언트 암호에 대한 하드 코딩된 값은 각각 `10101010` 및 `11110000`입니다. 사용자 이름과 비밀번호는 각각 `Peter`와 `Peter123`을 사용했습니다.

```
\> curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=password&username=$USERNAME&password=$PASSWORD&scope=foo" https://localhost:8443/oauth/token

{"access_token":"69ff86a8-eaa2-4490-adda-6ce0f10b9f8b","token_type":"bearer","refresh_token":"ab3c797b-72e2-4a9a-a1c5-c550b2775f93","expires_in":43199,"scope":"foo"}
```
Now let’s use the access token from the preceding response to invoke the Order API. Make sure to replace the value of $TOKEN with the corresponding access token appropriately.
```
\> curl -k -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json"   https://localhost:9090/retail/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```

## Enabling Mutual TLS Between `Zuul API Gateway` and `Order Service`

지금까지 이 장에서는 `cURL` 클라이언트와 STS, `cURL` 클라이언트와` Zuul API 게이트웨이`, `Zuul API 게이트웨이`와 `STS over TLS` 간의 통신을 보호했습니다. 여전히 우리는 배포에 약한 링크가 있다(그림 5-3 참조). `Zuul 게이트웨이`와 `Order 서비스` 간의 통신은 TLS나 인증으로 보호되지 않습니다. 즉, 누군가가 게이트웨이를 우회할 수 있다면 인증 없이 `Order` 서버에 도달할 수 있다. 이 문제를 해결하려면 상호 `TLS`를 통해 `게이트웨이`와 `Order` 서비스 간의 통신을 보호해야 한다. 그러면 `게이트웨이`를 거치지 않고 다른 요청이 `Order` 서비스에 도달할 수 없습니다. 즉, `Order` 서비스는 게이트웨이에서 생성된 요청만 수락한다.


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_5_Fig3_HTML.jpg)

그림 5-3 `Zuul API 게이트웨이`는 `Order` API로 가는 모든 요청을 가로채고 인증 서버(STS)에 대해 OAuth 2.0 액세스 토큰의 유효성을 검사한다.

게이트웨이와 `Order` 서비스 간에 상호 TLS를 활성화하려면 먼저 공개/개인 키 쌍을 만들어야 한다. ` keytool`을 사용하여 키 쌍을 생성하고 `keystore.jks` 파일에 저장한다. 3장에서는 다음 명령의 각 매개변수가 의미하는 바를 자세히 설명한다. 샘플 git repo의 키 저장소를 사용하는 경우 다음 `keytool` 명령을 건너뛸 수 있다.

```
\> keytool -genkey -alias spring -keyalg RSA -keysize 4096 -validity 3650 -dname "CN=order,OU=bar,O=zee,L=sjc,S=ca,C=us" -keypass springboot -keystore keystore.jks -storeType jks -storepass springboot
```

Order 서비스에 대해 상호 TLS를 활성화하려면 앞서 생성한 키 저장소 파일(`keystore.jks`)을 `Order` 서비스의 홈 디렉터리에 복사하고 `application.properties`에 다음을 추가한다. 우리는 키 저장소와 개인 키 모두에 대한 암호로 `springboot`를 사용하고 있다. `server.ssl.client-auth`는 주문 서비스에서 상호 TLS를 적용하는 데 사용됩니다.

```
server.ssl.key-store: keystore.jks
server.ssl.key-store-password: springboot
server.ssl.keyAlias: spring
server.ssl.client-auth:need
```
`Order` 서비스 끝에서 수행해야 하는 두 가지 변경 사항이 더 있다. `Order` 서비스에서 상호 TLS를 시행할 때 `Zuul 게이트웨이`(Order 서비스에 대한 클라이언트 역할)는 `X.509` 인증서로 자체 인증해야 하며 Order 서비스는 `Zuul 게이트웨이`의` X.509`와 연결된 인증 기관을 신뢰해야 한다. 자체 서명된 인증서를 사용하기 때문에 `Zuul 게이트웨이`의 공용 인증서를 내보내고 Order 서비스의 키 저장소로 가져와야 한다. ch05/sample02 디렉터리에서 다음 `keytool` 명령을 사용하여 `Zuul 게이트웨이`의 공인 인증서를 내보내고 `ch05/sample01` 디렉터리에 복사해 보겠습니다.

```
\> keytool -export -alias spring -keystore keystore.jks -storePass springboot -file zuul.crt
Certificate stored in file <zuul.crt>
\> cp zuul.crt ../sample01
```
Let’s use the following keytool command from ch05/sample01 directory to import Zuul gateway’s public certificate to Order service’s keystore.
```
\> keytool -import -alias zuul -keystore keystore.jks -storePass springboot -file zuul.crt

Trust this certificate? [no]:yes
Certificate was added to keystore
```
마지막으로 `Zuul 게이트웨이`와 Order 서비스 간에 TLS 연결이 있는 경우 `Zuul 게이트웨이`는 `Order` 서비스의 공용 인증서와 연결된 인증 기관을 신뢰해야 한다. 이 두 당사자 간에 상호 TLS를 활성화하지 않더라도 TLS만 활성화하려면 이 요구 사항을 충족해야 한다. 자체 서명된 인증서를 사용하기 때문에 `Order` 서비스의 공인 인증서를 내보내고 Zuul 게이트웨이의 키 저장소로 가져와야 한다. `ch05/sample01` 디렉터리에서 다음 `keytool` 명령을 사용하여 `Order` 서비스의 공인 인증서를 내보내고 `ch05/sample02` 디렉터리에 복사해 보겠습니다.

```
\> keytool -export -alias spring -keystore keystore.jks -storePass springboot -file order.crt
Certificate stored in file <order.crt>
\> cp order.crt ../sample02
```
Let’s use the following keytool command from ch05/sample02 directory to import Order service’s public certificate to Zuul gateway’s keystore.
```
\> keytool -import -alias order -keystore keystore.jks -storePass springboot -file order.crt
Trust this certificate? [no]:yes
Certificate was added to keystore
```

TLS가 `Order` 서비스에서 제대로 작동하는지 확인하려면 `ch05/sample01/` 디렉토리에서 다음 명령을 사용하여 `Order` 서비스를 실행하고 `HTTPS` 포트를 인쇄하는 행을 확인하십시오.

```
\> mvn spring-boot:run
Tomcat started on port(s): 8080 (https) with context path "
```
`HTTP` 대신 `HTTPS`를 사용하도록 `Order` 서비스 끝점을 업데이트했으므로 새 HTTPS 끝점을 사용하도록 `Zuul 게이트웨이`도 업데이트해야 한다. 이러한 라우팅 지침은 다음과 같이 `application.properties` 파일에 설정됩니다. `HTTP` 대신 `HTTPS`를 사용하도록 업데이트하기만 하면 됩니다. 또한 동일한 파일에서 `zuul.sslHostnameValidationEnabled` 속성의 주석 처리를 제거하고 `false`로 설정해야 한다. 이것은 `Spring Boot`에 호스트 이름 확인을 무시하도록 요청하는 것입니다. 즉, 이제 `Spring Boot`는 Order 서비스의 호스트 이름이 해당 공용 인증서의 일반 이름과 일치하는지 여부를 확인하지 않습니다.
```
zuul.routes.retail.url=https://localhost:8080
zuul.sslHostnameValidationEnabled=false
```
Restart the Zuul gateway with the following command from ch05/sample02.
```
\> mvn spring-boot:run
```

인증 서버가 실행 중이라고 가정하고 `HTTPS` 포트 `8443`에서 다음 명령을 실행하여 종단 간 흐름을 테스트한다. 먼저 보안 토큰 서비스와 대화하고 액세스 토큰을 가져와야 한다. 다음 명령에서 `$CLIENTID`, `$CLIENTSECRET`, `$USERNAME` 및 `$PASSWORD` 값을 적절하게 교체해야 한다.

```
\> curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=password&username=$USERNAME&password=$PASSWORD&scope=foo" https://localhost:8443/oauth/token

{"access_token":"69ff86a8-eaa2-4490-adda-6ce0f10b9f8b","token_type":"bearer","refresh_token":"ab3c797b-72e2-4a9a-a1c5-c550b2775f93","expires_in":43199,"scope":"foo"}
```
Now let’s use the access token from the preceding response to invoke the Order API. Make sure to replace the value of $TOKEN with the corresponding access token appropriately.
```
\> curl -k -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json"   https://localhost:9090/retail/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```
### Securing Order API with Self-Contained Access Tokens

OAuth 2.0 전달자 토큰은 참조 토큰 또는 자체 포함 토큰일 수 있습니다. 참조 토큰은 임의의 문자열입니다. 공격자는 무차별 대입 공격을 수행하여 토큰을 추측할 수 있습니다. 권한 부여 서버는 올바른 길이를 선택하고 무차별 대입을 방지하기 위해 다른 가능한 조치를 사용해야 합니다. 자체 포함된 액세스 토큰은 7장에서 설명하는 JSON 웹 토큰(JWT)입니다. 리소스 서버가 참조 토큰인 액세스 토큰을 받으면 토큰을 확인하기 위해 권한 부여와 대화해야 합니다. 서버(또는 토큰 발행자). 액세스 토큰이 JWT인 경우 리소스 서버는 JWT의 서명을 확인하여 자체적으로 토큰의 유효성을 검사할 수 있습니다. 이 섹션에서는 인증 서버에서 JWT 액세스 토큰을 얻고 이를 사용하여 Zuul API 게이트웨이를 통해 주문 서비스에 액세스하는 방법에 대해 설명합니다.


### 자체 포함 액세스 토큰으로 주문 API 보안

OAuth 2.0 `bearer` 토큰은 `참조 토큰` 또는 `자체 포함 토큰`일 수 있다. `참조 토큰`은 임의의 문자열입니다. 공격자는 무차별 대입 공격을 수행하여 토큰을 추측할 수 있다. 권한 부여 서버는 올바른 길이를 선택하고 무차별 대입을 방지하기 위해 다른 가능한 조치를 사용해야 한다. 자체 포함된 액세스 토큰은 7장에서 설명하는 JWT입니다. 리소스 서버가 `참조 토큰`인 액세스 토큰을 받으면 토큰을 확인하기 위해 권한 부여와 대화해야 한다. 서버(또는 토큰 발행자). 액세스 토큰이 JWT인 경우 리소스 서버는 JWT의 서명을 확인하여 자체적으로 토큰의 유효성을 검사할 수 있다. 이 섹션에서는 인증 서버에서 JWT 액세스 토큰을 얻고 이를 사용하여 `Zuul API 게이트웨이`를 통해 Order 서비스에 액세스하는 방법에 대해 설명한다.

### JWT를 발행하는 인증 서버 설정

이 섹션에서는 자체 포함 액세스 토큰 또는 JWT를 지원하기 위해 이전 섹션(`ch05/sample03/`)에서 사용한 인증 서버를 확장하는 방법을 살펴보겠습니다. 첫 번째 단계는 키 저장소와 함께 새 키 쌍을 만드는 것입니다. 이 키는 인증 서버에서 발행된 JWT에 서명하는 데 사용됩니다. 다음 `keytool` 명령은 키 쌍으로 새 키 저장소를 생성한다.

```
\> keytool -genkey -alias jwtkey -keyalg RSA -keysize 2048 -dname "CN=localhost" -keypass springboot -keystore jwt.jks -storepass springboot
```

앞의 명령은 `jwt.jks`라는 이름의 키 저장소를 생성하며, 비밀번호 `springboot로` 보호됩니다. 이 키 저장소를 `resources/`에 복사해야 한다. 이제 자체 포함된 액세스 토큰을 생성하려면 `application.properties` 파일에서 다음 속성 값을 설정해야 한다.

```
spring.security.oauth.jwt: true
spring.security.oauth.jwt.keystore.password: springboot
spring.security.oauth.jwt.keystore.alias: jwtkey
spring.security.oauth.jwt.keystore.name: jwt.jks
```

`spring.security.oauth.jwt`의 값은 기본적으로 `false`로 설정되어 있으며 JWT를 발행하려면 true로 변경해야 합니다. 다른 세 가지 속성은 설명이 필요 없으며 키 저장소를 생성할 때 사용한 값에 따라 적절하게 설정해야 합니다.

`config/AuthorizationServerConfig` 클래스에 다음 메소드를 추가했습니다. 이 메소드는 이전에 생성한 `jwt.jks` 키 저장소에서 개인 키를 검색하는 방법에 대한 세부 정보를 주입한다. 이 개인 키는 JWT에 서명하는 데 사용됩니다.

```
@Bean
protected JwtAccessTokenConverter jwtConeverter() {
     String pwd = env.getProperty("spring.security.oauth.jwt.keystore.password");
     String alias = env.getProperty("spring.security.oauth.jwt.keystore.alias");
     String keystore = env.getProperty("spring.security.oauth.jwt.keystore.name");
     String path = System.getProperty("user.dir");
      KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
             new FileSystemResource(new File(path + File.separator + keystore)), pwd.toCharArray());
     JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
     converter.setKeyPair(keyStoreKeyFactory.getKeyPair(alias));
     return converter;
}
```

동일한 클래스 파일에서 `JwtTokenStore`도 토큰 저장소로 설정한다. 다음 함수는 이를 어떤 방식으로 수행한다. `application.properties` 파일에서 `spring.security.oauth.jwt` 속성이 `true`로 설정된 경우에만 `JwtTokenStore`를 토큰 저장소로 설정한다.

```
@Bean
public TokenStore tokenStore() {
   String useJwt = env.getProperty("spring.security.oauth.jwt");
   if (useJwt != null && "true".equalsIgnoreCase(useJwt.trim())) {
       return new JwtTokenStore(jwtConeverter());
    } else {
       return new InMemoryTokenStore();
    }
}
```

마지막으로 토큰 저장소를 `AuthorizationServerEndpointsConfigurer`로 설정해야 한다. 이 작업은 JWT를 사용하려는 경우에만 다음 방법으로 수행됩니다.

```
@Autowired
private AuthenticationManager authenticationManager;
@Override
public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
  String useJwt = env.getProperty("spring.security.oauth.jwt");
  if (useJwt != null && "true".equalsIgnoreCase(useJwt.trim())) {
      endpoints.tokenStore(tokenStore()).tokenEnhancer(jwtConeverter())
                         .authenticationManager(authenticationManager);
  } else {
      endpoints.authenticationManager(authenticationManager);
  }

}
```
To start the authorization server, use the following command from ch05/sample03/ directory, which now issues self-contained access tokens (JWTs).
```
\> mvn spring-boot:run
```

OAuth 2.0 `클라이언트 자격 증명 부여` 유형을 사용하여 액세스 토큰을 얻으려면 다음 명령을 사용한다. `$CLIENTID` 및 `$CLIENTSECRET` 값을 적절하게 바꾸십시오.

```
\> curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=client_credentials&scope=foo" https://localhost:8443/oauth/token
```
The preceding command will return back a base64-url-encoded JWT, and the following shows the decoded version.
```
{ "alg": "RS256", "typ": "JWT" }

{ "scope": [ "foo" ], "exp": 1524793284, "jti": "6e55840e-886c-46b2-bef7-1a14b813dd0a", "client_id": "10101010" }
```
디코딩된 헤더와 페이로드만 출력에 표시되고 서명(JWT의 세 번째 부분)은 건너뜁니다. `client 자격증명` 부여 유형을 사용했기 때문에 `JWT`에는 제목이나 사용자 이름이 포함되지 않습니다. 여기에는 토큰과 연결된 범위 값도 포함됩니다.

### JWT로 Zuul API 게이트웨이 보호

이 섹션에서는 `Zuul API 게이트웨이`에서 자체 발급 액세스 토큰 또는 JWT 기반 토큰 유효성 검사를 시행하는 방법을 살펴보겠습니다. `application.properties` 파일에서 `user-info-uri` 속성을 주석 처리하고 `jwt.keyUri` 속성의 주석을 제거하면 됩니다.
```
#security.oauth2.resource.user-info-uri:https://localhost:8443/user
security.oauth2.resource.jwt.keyUri: https://localhost:8443/oauth/token_key
```
여기에서 `jwt.keyUri` 값은 인증 서버에서 JWT에 서명하는 데 사용되는 개인 키에 해당하는 공개 키를 가리킵니다. 권한 부여 서버에서 호스팅되는 끝점입니다. 브라우저에 https://localhost:8443/oauth/token_key를 입력하면 다음과 같이 공개 키를 찾을 수 있다. API 게이트웨이가 요청에 포함된 JWT의 서명을 확인하는 데 사용하는 키입니다.

```
{
   "alg":"SHA256withRSA",
   "value":"-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA+WcBjPsrFvGOwqVJd8vpV+gNx5onTyLjYx864mtIvUxO8D4mwAaYpjXJgsre2dcXjQ03BOLJdcjY5Nc9Kclea09nhFIEJDG3obwxm9gQw5Op1TShCP30Xqf8b7I738EHDFT6qABul7itIxSrz+AqUvj9LSUKEw/cdXrJeu6b71qHd/YiElUIA0fjVwlFctbw7REbi3Sy3nWdm9yk7M3GIKka77jxw1MwIBg2klfDJgnE72fPkPi3FmaJTJA4+9sKgfniFqdMNfkyLVbOi9E3DlaoGxEit6fKTI9GR1SWX40FhhgLdTyWdu2z9RS2BOp+3d9WFMTddab8+fd4L2mYCQIDAQAB\n-----END PUBLIC KEY-----"
}
```
Once the changes are made as highlighted earlier, let’s restart the Zuul gateway with the following command from the sample02 directory .
```
\> mvn spring-boot:run
```
OAuth 2.0 인증 서버에서 JWT 액세스 토큰을 얻으면 이전과 같은 방식으로 다음 cURL 명령을 사용하여 보호된 리소스에 액세스할 수 있습니다. `$TOKEN `값이 유효한 JWT 액세스 토큰으로 적절하게 대체되었는지 확인하십시오.

```
\> curl -k -H "Authorization: Bearer $TOKEN" https://localhost:9443/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items":[{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```

## 웹 애플리케이션 방화벽(WAF)의 역할

API 게이트웨이는 인증, 권한 부여 및 조절 정책을 중앙에서 시행하는 정책 시행 지점(PEP)입니다. 공개 API 배포에서는 API 게이트웨이만으로는 충분하지 않습니다. 또한 API 게이트웨이 앞에 있는 방화벽(WAF)이 필요한다(그림 5-4 참조). `WAF`의 주요 역할은 `DDoS` 공격으로부터 API 배포를 보호하는 것입니다. `OWASP`에서 식별한 알려진 위협과 함께 OpenAPI 사양에 대한 위협 탐지 및 메시지 유효성 검사를 수행한다. 가트너는 2020년까지 공개 웹 애플리케이션의 50% 이상이 `Akamai`, `Imperva`, `Cloudflare`, `AWS` 등과 같은 클라우드 기반 WAF 서비스 플랫폼으로 보호될 것으로 예측한다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_5_Fig4_HTML.jpg)

Figure 5-4 A web application firewall (WAF) intercepts all the traffic coming into an API deployment

## Summary


- OAuth 2.0은 API 보안을 위한 사실상의 표준입니다.

- API 게이트웨이는 프로덕션 배포에서 API를 보호하는 가장 일반적인 패턴입니다. 즉, API 배포의 진입점입니다.

- 우리가 일반적으로 API 게이트웨이로 식별하는 API 게이트웨이 패턴을 구현하는 많은 오픈 소스 및 독점 제품이 있다.

- OAuth 2.0 전달자 토큰은 참조 토큰 또는 자체 포함 토큰일 수 있다. 참조 토큰은 임의의 문자열입니다. 공격자는 무차별 대입 공격을 수행하여 토큰을 추측할 수 있다. 권한 부여 서버는 올바른 길이를 선택하고 무차별 대입을 방지하기 위해 다른 가능한 조치를 사용해야 한다.

- 리소스 서버가 참조 토큰인 액세스 토큰을 받으면 토큰의 유효성을 검사하기 위해 인증 서버(또는 토큰 발급자)와 대화해야 한다. 액세스 토큰이 JWT인 경우 리소스 서버는 JWT의 서명을 확인하여 자체적으로 토큰의 유효성을 검사할 수 있다.

- Zuul은 동적 라우팅, 모니터링, 복원력, 보안 등을 제공하는 API 게이트웨이입니다. Netflix 서버 인프라의 정문 역할을 하며 전 세계 모든 Netflix 사용자의 트래픽을 처리한다.

- 공개 API 배포에서는 API 게이트웨이만으로는 충분하지 않습니다. 또한 API 게이트웨이 앞에 있는 WAF(웹 애플리케이션 방화벽)도 필요한다
 