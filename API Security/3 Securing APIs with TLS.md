

# 3. Securing APIs with TLS

TLS가 API 배포시 가장 흔하게 보는 API 보안이다. 

In this chapter, we discuss how to deploy an API implemented in Java Spring Boot, enable TLS, and protect an API with mutual TLS.

## Setting Up the Environment

이 섹션에서는 처음부터 `Spring Boot`를 사용하여 API를 개발하는 방법을 살펴보겠습니다. 이 책에서는 Spring Boot를 사용하여 API를 구현합니다. 

To run the samples, you will need Java 8 or latest, Maven 3.2 or latest, and a git client. Once you are successfully done with the installation, run the following two commands in the command line to make sure everything is working fine. If you’d like some help in setting up Java or Maven, there are plenty of online resources out there.

All the samples used in this book are available in the https://github.com/apisecurity/samples.git git repository. Use the following git command to clone it. All the samples related to this chapter are inside the directory ch03.
```
\> git clone https://github.com/apisecurity/samples.git
\> cd samples/ch03
```

> **Note**
>
> If you find any issues in building or running the samples given in this book, please refer to the README file under the corresponding chapter in the git repository: https://github.com/apisecurity/samples.git. We will update the samples and the corresponding README files in the git repository, to reflect any changes happening, related to the tools, libraries, and frameworks used in this book.

### Deploying Order API

This is the simplest API ever. You can find the code inside the directory `ch03/sample01`. To build the project with Maven, use the following command:
```
\> cd sample01
\> mvn clean install
```

`OrderProcessing` 클래스의 `checkOrderStatus` 메서드는 주문 ID를 수락하고 주문 상태를 반환합니다.
```
@RestController
@GetMapping(value = "/order")
public class OrderProcessing {
  @GetMapping(value="/{id}")
  public String checkOrderStatus(@PathVariable("id") String orderId)
  {
    return ResponseEntity.ok("{'status' : 'shipped'}");
  }
}
```
There is another important class file at `ch03/sample01/OrderProcessing/App.java` worth having a look at. This is the class which spins up our API in its own application server, in this case the embedded Tomcat. By default the API starts on port `8080`, and you can change the port by adding, say, for example, `server.port=9000` to the `application.properties` file. This will set the server port to `9000`. The following shows the code snippet from OrderProcessingApp class, which spins up our API. The `@SpringBootApplication` annotation, which is defined at the class level, is being used as a shortcut for four other annotations defined in Spring: 
```
@SpringBootApplication
public class OrderProcessing {
  public static void main(String[] args) {
    SpringApplication.run(OrderProcessing.class, args);
  }
}
```
Now, let’s see how to run our API and talk to it with a cURL client. The following command executed from ch03/sample01 directory shows how to start our Spring Boot application with Maven.
```
\> mvn spring-boot:run
```
To test the API with a `cURL` client, use the following command from a different command console. It will print the output as shown in the following, after the initial command.
```
\> curl http://localhost:8080/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```

## Securing Order API with TLS

`TLS`를 활성화하려면 먼저 공개/개인 키 쌍을 생성해야 합니다. `keytool`을 사용하여 키 쌍을 생성하고 `keystore.jks` 파일에 저장합니다. 이 파일은 키 저장소이며 다른 형식일 수 있습니다. 가장 많이 사용되는 두 가지 형식은 `JKS`와 `PKCS#12`입니다. `JKS`는 Java에만 해당하는 반면, `PKCS#12`는 PKCS에 정의된 표준 제품군에 속하는 표준입니다. 다음 명령에서 `JKS`로 설정된 `storetype` 인수로 키 저장소 유형을 지정합니다.

```
\> keytool -genkey -alias spring -keyalg RSA -keysize 4096 -validity 3650 -dname "CN=foo,OU=bar,O=zee,L=sjc,S=ca,C=us" -keypass springboot -keystore keystore.jks -storeType jks -storepass springboot
```

- `alias`은 키 저장소에 저장된 생성된 키를 식별하는 방법을 지정합니다. 주어진 키 저장소에 여러 개의 키가 저장될 수 있으며 해당 별칭의 값은 고유해야 합니다. 여기서 우리는 별칭으로 `spring`을 사용합니다.

- `validity`는 생성된 키가 10년 동안만 유효함을 지정합니다.

- `keysize` 및 `keystore` 인수는 생성된 키의 길이와 키가 저장되는 키 저장소의 이름을 지정합니다.

- `genkey`는 `keytool`이 새 키를 생성하도록 지시하는 옵션입니다. `genkey` 대신 `genkeypair` 옵션을 사용할 수도 있습니다. 위의 명령이 실행되면 `keystore.jks`라는 키 저장소 파일이 생성되며 이 파일은 `springboot` 암호로 보호됩니다.

이 예에서 생성된 인증서를 `자체 서명된 인증서`라고 한다. 즉, 외부 CA가 없다. 일반적으로 제품 배포에서는 퍼블릭 인증 기관 또는 기업 수준 인증 기관을 사용하여 퍼블릭 인증서에 서명하므로 인증 기관을 신뢰하는 모든 클라이언트가 이를 확인할 수 있다. 마이크로 서비스 배포 또는 내부 API 배포에서 서비스 간 통신을 보호하기 위해 인증서를 사용하는 경우 퍼블릭 인증 기관을 갖는 것에 대해 걱정할 필요가 없다. 자체 인증 기관을 가질 수 있으나 외부 클라이언트 앱에 노출하는 API의 경우 공용 인증 기관에서 서명한 인증서를 받아야 한다.

`Spring Boot` API에 대해 TLS를 활성화하려면 키 저장소 파일(`keystore.jks`)을 샘플의 홈 디렉토리(예: `ch03/sample01/`)에 복사하고 `application.properties` 파일에 다음을 추가한다.

```
server.ssl.key-store: keystore.jks
server.ssl.key-store-password: springboot
server.ssl.keyAlias: spring
```

모든 것이 제대로 작동하는지 확인하려면 `ch03/sample01/` 디렉토리에서 다음 명령을 사용하여 `Order` API를 실행하고 `HTTPS` 포트를 확인하십시오.

```
\> mvn spring-boot:run
Tomcat started on port(s): 8080 (https) with context path "
```
To test the API with a `cURL` client, use the following command from a different command console. It will print the output as shown in the following, after the initial command. Instead of HTTP, we are using HTTPS here.
```
\> curl –k https://localhost:8080/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```

이전 `cURL` 명령에서 `-k` 옵션을 사용한다. HTTPS 끝점을 보호하기 위해 `자체 서명된 인증서`가 있으므로 `cURL` 신뢰 유효성 검사를 무시하도록 조언하는 `–k` 매개변수를 전달해야 한다. 적절한 인증 기관 서명 인증서가 있는 프로덕션 배포에서는 그렇게 할 필요가 없다. 또한 자체 서명된 인증서가 있는 경우 `cURL`을 해당 퍼블릭 인증서로 지정하여 `–k`를 사용하지 않아도 된다.

```
curl --cacert ca.crt https://localhost:8080/order/11
```
You can use the following command from `ch03/sample01/` to export the public certificate of the `Order` API to `ca.crt` file in PEM (with the `-rfc` argument) format.

```
keytool -export -file ca.crt -alias spring –rfc -keystore keystore.jks -storePass springboot
```

위애서의 `ca.crt`가 있는 `curl`은 다음 오류를 발생시킵니다. `foo`인 `Order` API의 공개 인증서에 있는 일반 이름이 `cURL` 명령의 호스트이름(`localhost`)과 일치하지 않는다고 불평합니다.

```
curl: (51) SSL: certificate subject name 'foo' does not match target host name 'localhost'
```

이상적으로는 프로덕션 배포에서 인증서를 생성할 때 인증서의 일반 이름이 `hostname`과 일치해야 합니다. 이 경우 `foo` 호스트이름에 대한 DNS 항목이 없으므로 `cURL`과 함께 다음 해결 방법을 사용할 수 있습니다.

```
\> curl --cacert ca.crt https://foo:8080/order/11 --resolve foo:8080:127.0.0.1
```

## Protecting Order API with Mutual TLS

이 절에서는 `Order` API와 `cURL` 클라이언트 간에 `TLS` 상호 인증을 활성화하는 방법을 살펴본다. 대부분의 경우 `TLS` 상호 인증을 사용하여 시스템 간 인증을 활성화한다. 먼저 `sample01/keystore.jks`에 키 저장소가 있는지 확인한 다음 `TLS` 상호 인증을 활성화하려면 `application.properties` 파일에서 다음 속성의 주석 처리를 제거한다.

```
server.ssl.client-auth:need
```

이제 `cURL`을 사용하여 `Order` API를 호출하여 흐름을 테스트할 수 있습니다. 먼저 `ch03/sample01/` 디렉토리에서 다음 명령을 사용하여 `Order` API를 실행하고 `HTTPS` 포트를  확인합니다

```
\> mvn spring-boot:run
Tomcat started on port(s): 8080 (https) with context path ''
```
To test the API with a `cURL` client, use the following command from a different command console.
```
\> curl –k https://localhost:8080/order/11
```
Since we have protected the API with TLS mutual authentication, the preceding command will result in the following error message, which means the API (or the server) has refused to connect with the cURL client, because it didn’t present a valid client certificate.

API를 TLS 상호 인증으로 보호하므로 앞의 명령은 다음 오류 메시지를 표시한다. 이는 API(또는 서버)가 유효한 클라이언트 인증서를 제시하지 않았기 때문에 `cURL` 클라이언트와의 연결을 거부했음을 의미한다.

```
curl: (35) error:1401E412:SSL routines:CONNECT_CR_FINISHED:sslv3 alert bad certificate
```

이 문제를 해결하려면 `cURL` 클라이언트에 대한 키 쌍(공개 키 및 개인 키)을 생성하고 공개 키를 신뢰하도록 `Order` API를 구성해야 한다. 그런 다음 생성한 키 쌍을 `cURL` 명령과 함께 사용하여 상호 TLS로 보호되는 API에 액세스할 수 있다.

`cURL` 클라이언트에 대한 개인 키와 공개 키를 생성하기 위해 다음 `OpenSSL` 명령을 사용한다. `OpenSSL`은 TLS용 상용급 툴킷 및 암호화 라이브러리이며 여러 플랫폼에서 사용할 수 있다. `www.openssl.org/source`에서 플랫폼에 맞는 배포판을 다운로드하고 설정할 수 있다. 또는 가장 쉬운 방법으로 `OpenSSL` Docker 이미지를 사용하는 것입니다. 다음 절에서는 `OpenSSL`을 Docker 컨테이너로 실행하는 방법에 대해 설명한다.

```
\> openssl genrsa -out privkey.pem 4096
```
Now, to generate a self-signed certificate, corresponding to the preceding private key (`privkey.pem`), use the following `OpenSSL` command.
```
\> openssl req -key privkey.pem -new -x509 -sha256 -nodes -out client.crt -subj "/C=us/ST=ca/L=sjc/O=zee/OU=bar/CN=client"
```
Let’s take down the Order API, if it is still running, and import the public certificate (`client.crt`) we created in the preceding step to `sample01/keystore.jks`, using the following command.
```
\> keytool -import -file client.crt -alias client -keystore keystore.jks -storepass springboot
```
Now we can test the flow by invoking the `Order` API using `cURL`. First, use the following command from `ch03/sample01/` directory to spin up the `Order` API.
```
\> mvn spring-boot:run
Tomcat started on port(s): 8080 (https) with context path ''
```
To test the API with a cURL client, use the following command from a different command console.
```
\> curl -k --key privkey.pem --cert client.crt https://localhost:8080/order/11
```
In case we use a key pair, which is not known to the Order API, or in other words not imported into the sample01/keystore.jks file, you will see the following error, when you execute the preceding cURL command.
```
curl: (35) error:1401E416:SSL routines:CONNECT_CR_FINISHED:sslv3 alert certificate unknown
```

## Running OpenSSL on Docker

In the last few years, Docker revolutionized the way we distribute software. Docker provides a containerized environment to run software in self-contained manner. A complete overview of Docker is out of the scope of this book—and if you are interested in learning more, we recommend you check out the book Docker in Action (Manning Publications, 2019) by Jeff Nickoloff and Stephen Kuenzli.

Setting up Docker in your local machine is quite straightforward, following the steps in Docker documentation available at https://docs.docker.com/install/. Once you get Docker installed, run the following command to verify the installation, and it will show the version of Docker engine client and server.
```
\> docker version
```
To start OpenSSL as a Docker container, use the following command from the `ch03/sample01` directory.
```
\> docker run -it -v $(pwd):/export prabath/openssl
```
앞의 명령을 처음 실행할 때 실행하는 데 몇 분이 걸리고 이전 섹션의 끝에서 사용한 키를 생성하기 위해 `OpenSSL` 명령을 실행할 수 있는 명령 프롬프트로 끝납니다. 앞의 docker run 명령은 호스트 파일의 `ch03/sample01`(또는 이전 명령에서 `$(pwd)`로 표시된 현재 디렉토리) 디렉토리를 매핑하는 볼륨 마운트와 함께 Docker 컨테이너에서 `OpenSSL`을 시작합니다. 시스템을 컨테이너 파일 시스템의 `/export` 디렉토리로 이동합니다. 이 볼륨 마운트는 호스트 파일 시스템의 일부를 컨테이너 파일 시스템과 공유하는 데 도움이 됩니다. OpenSSL 컨테이너가 인증서를 생성하면 컨테이너 파일 시스템의 `/export` 디렉토리에 기록됩니다. 볼륨 마운트가 있기 때문에 컨테이너 파일 시스템의 `/export` 디렉토리 내의 모든 항목은 호스트 파일 시스템의 `ch03/sample01` 디렉토리에서도 액세스할 수 있습니다.

To generate a private key and a public key for the cURL client, we use the following `OpenSSL` command.

```
# openssl genrsa -out /export/privkey.pem 4096
```
Now, to generate a self-signed certificate, corresponding to the preceding private key (`privkey.pem`), use the following `OpenSSL` command.
```
# openssl req -key /export/privkey.pem -new -x509 -sha256 -nodes -out client.crt -subj "/C=us/ST=ca/L=sjc/O=zee/OU=bar/CN=client"
```
## Summary


- `TLS`는 모든 API를 보호하는 기본입니다.

- `TLS`로 API를 보호하는 것은 모든 API 배포에서 볼 수 있는 가장 일반적인 보호 형태입니다.

- `TLS`는 기밀성과 무결성을 위해 전송 중인 데이터를 보호하고, 상호 `TLS`는 클라이언트 인증을 적용하여 침입자로부터 API를 보호합니다.

- `OpenSSL`은 TLS용 상용 등급 툴킷 및 암호화 라이브러리이며 여러 플랫폼에서 사용할 수 있습니다.
