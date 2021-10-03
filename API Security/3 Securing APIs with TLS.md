

# 3. Securing APIs with TLS

TLS가 API 배포시 가장 흔하게 보는 API 보안이다. 

In this chapter, we discuss how to deploy an API implemented in Java Spring Boot, enable TLS, and protect an API with mutual TLS.

## Setting Up the Environment

In this section, we’ll see how we can develop an API using Spring Boot from scratch. Spring Boot (https://projects.spring.io/spring-boot/) is the most popular microservices development framework for Java developers. To be precise, Spring Boot offers an opinionated1 runtime for Spring, which takes out a lot of complexities. Even though Spring Boot is opinionated, it also gives developers to override many of its default picks. Due to the fact that many Java developers are familiar with Spring, and the ease of development is a key success factor in the microservices world, many adopted Spring Boot. Even for Java developers who are not using Spring, still it is a household name. If you have worked on Spring, you surely would have worried how painful it was to deal with large, chunky XML configuration files. Unlike Spring, Spring Boot believes in convention over configuration—no more XML hell! In this book, we use Spring Boot to implement our APIs. Even if you are not familiar with Java, you will be able to get started with no major learning curve, as we provide all the code examples.

To run the samples, you will need Java 8 or latest, Maven 3.2 or latest, and a git client. Once you are successfully done with the installation, run the following two commands in the command line to make sure everything is working fine. If you’d like some help in setting up Java or Maven, there are plenty of online resources out there.

All the samples used in this book are available in the https://github.com/apisecurity/samples.git git repository. Use the following git command to clone it. All the samples related to this chapter are inside the directory ch03.
```
\> git clone https://github.com/apisecurity/samples.git
\> cd samples/ch03
```
To anyone who loves Maven, the best way to get started with a Spring Boot project would be with a Maven archetype. Unfortunately, it is no more supported. One option we have is to create a template project via https://start.spring.io/ –which is known as the Spring Initializer. There you can pick which type of project you want to create, project dependencies, give a name, and download a maven project as a zip file. The other option is to use the Spring Tool Suite (STS).2 It’s an IDE (integrated development environment) built on top of the Eclipse platform, with many useful plugins to create Spring projects. However, in this book, we provide you all the fully coded samples in the preceding git repository.

> **Note**
>
> If you find any issues in building or running the samples given in this book, please refer to the README file under the corresponding chapter in the git repository: https://github.com/apisecurity/samples.git. We will update the samples and the corresponding README files in the git repository, to reflect any changes happening, related to the tools, libraries, and frameworks used in this book.

### Deploying Order API

This is the simplest API ever. You can find the code inside the directory ch03/sample01. To build the project with Maven, use the following command:
```
\> cd sample01
\> mvn clean install
```
Before we delve deep into the code, let’s have a look at some of the notable Maven dependencies and plugins added into ch03/sample01/pom.xml.

Spring Boot comes with different starter dependencies to integrate with different Spring modules. The spring-boot-starter-web dependency brings in Tomcat and Spring MVC and, does all the wiring between the components, making the developer’s work to a minimum. The spring-boot-starter-actuator dependency helps you monitor and manage your application.
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
In the pom.xml file, we also have the spring-boot-maven-plugin plugin, which lets you start the Spring Boot API from Maven itself.
```
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```
Now let’s have a look at the checkOrderStatus method in the class file service/OrderProcessing.java. This method accepts an order id and returns back the status of the order. There are three notable annotations used in the following code. The `@RestController` is a class-level annotation that marks the corresponding class as a REST endpoint, which accepts and produces JSON payloads. The `@GetMapping` annotation can be defined both at the class level and the method level. The value attribute at the class-level annotation defines the path under which the corresponding endpoint is registered. The same at the method level appends to the class-level path. Anything defined within curly braces is a placeholder for any variable value in the path. For example, a GET request on `/order/101` and `/order/102` (where 101 and 102 are the order ids), both hit the method checkOrderStatus. In fact, the value of the value attribute is a URI template.3 The annotation @PathVariable extracts the provided variable from the URI template defined under the value attribute of the `@GetMapping` annotation and binds it to the variable defined in the method signature.
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
There is another important class file at `ch03/sample01/OrderProcessing/App.java` worth having a look at. This is the class which spins up our API in its own application server, in this case the embedded Tomcat. By default the API starts on port 8080, and you can change the port by adding, say, for example, server.port=9000 to the `application.properties` file. This will set the server port to 9000. The following shows the code snippet from OrderProcessingApp class, which spins up our API. The `@SpringBootApplication` annotation, which is defined at the class level, is being used as a shortcut for four other annotations defined in Spring: 
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

To enable TLS, first we need to create a public/private key pair. The following command uses `keytool` to generate a key pair and stores it in `keystore.jks` file. This file is a keystore, and it can be in different formats. Two most popular formats are `JKS` and `PKCS#12`. `JKS` is specific to Java, while `PKCS#12` is a standard, which belongs to the family of standards defined under PKCS. In the following command, we specify the keystore type with the storetype argument, which is set to `JKS`.

```
\> keytool -genkey -alias spring -keyalg RSA -keysize 4096 -validity 3650 -dname "CN=foo,OU=bar,O=zee,L=sjc,S=ca,C=us" -keypass springboot -keystore keystore.jks -storeType jks -storepass springboot
```
- The `alias` specifies how to identify the generated keys stored in the keystore. There can be multiple keys stored in a given keystore, and the value of the corresponding alias must be unique. Here we use spring as the alias. 

- The `validity` argument specifies that the generated keys are only valid for 10 years. 

- The `keysize` and `keystore` arguments specify the length of the generated key and the name of the keystore, where the keys are stored. 

- The `genkey` is the option, which instructs the keytool to generate new keys; instead of `genkey`, you can also use `genkeypair` option. Once the preceding command is executed, it will create a keystore file called `keystore.jks`, which is protected with the password springboot.


이 예에서 생성된 인증서를 자체 서명된 인증서라고 한다. 즉, 외부 CA가 없다. 일반적으로 제품 배포에서는 퍼블릭 인증 기관 또는 기업 수준 인증 기관을 사용하여 퍼블릭 인증서에 서명하므로 인증 기관을 신뢰하는 모든 클라이언트가 이를 확인할 수 있다. 마이크로 서비스 배포 또는 내부 API 배포에서 서비스 간 통신을 보호하기 위해 인증서를 사용하는 경우 퍼블릭 인증 기관을 갖는 것에 대해 걱정할 필요가 없다. 자체 인증 기관을 가질 수 있으나 외부 클라이언트 애플리케이션에 노출하는 API의 경우 공용 인증 기관에서 서명한 인증서를 받아야 한다.

Spring Boot API에 대해 TLS를 활성화하려면 키 저장소 파일(`keystore.jks`)을 샘플의 홈 디렉토리(예: `ch03/sample01/`)에 복사하고 `application.properties` 파일에 다음을 추가한다. git에서 다운로드한 샘플에는 이미 이러한 값이 있으며(해당 값을 주석 해제하기만 하면 됨) 키 저장소와 개인 키 모두에 대한 암호로 springboot를 사용한다.

```
server.ssl.key-store: keystore.jks
server.ssl.key-store-password: springboot
server.ssl.keyAlias: spring
```
To validate that everything works fine, use the following command from `ch03/sample01/` directory to spin up the Order API and notice the line which prints the HTTPS port.
```
\> mvn spring-boot:run
Tomcat started on port(s): 8080 (https) with context path "
```
To test the API with a `cURL` client, use the following command from a different command console. It will print the output as shown in the following, after the initial command. Instead of HTTP, we are using HTTPS here.
```
\> curl –k https://localhost:8080/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```

이전 `cURL` 명령에서 `-k` 옵션을 사용한다. HTTPS 끝점을 보호하기 위해 자체 서명된(신뢰할 수 없는) 인증서가 있으므로 `cURL` 신뢰 유효성 검사를 무시하도록 조언하는 `–k` 매개변수를 전달해야 한다. 적절한 인증 기관 서명 인증서가 있는 프로덕션 배포에서는 그렇게 할 필요가 없다. 또한 자체 서명된 인증서가 있는 경우 `cURL`을 해당 퍼블릭 인증서로 지정하여 `–k`를 사용하지 않아도 된다.

```
curl --cacert ca.crt https://localhost:8080/order/11
```
You can use the following `keytool` command from `ch03/sample01/` to export the public certificate of the `Order` API to `ca.crt` file in PEM (with the `-rfc` argument) format.

```
keytool -export -file ca.crt -alias spring –rfc -keystore keystore.jks -storePass springboot
```
The preceding `curl` command with the `ca.crt` will result in the following error. It complains that the common name in the public certificate of the Order API, which is `foo`, does not match with the `hostname` (localhost) in the `cURL` command.
```
curl: (51) SSL: certificate subject name 'foo' does not match target host name 'localhost'
```
Ideally in a production deployment when you create a certificate, its common name should match the `hostname`. In this case, since we do not have a DNS entry for the `foo` hostname, you can use the following workaround, with cURL.
```
\> curl --cacert ca.crt https://foo:8080/order/11 --resolve foo:8080:127.0.0.1
```

## Protecting Order API with Mutual TLS

이 절에서는 `Order` API와 `cURL` 클라이언트 간에 TLS 상호 인증을 활성화하는 방법을 살펴본다. 대부분의 경우 TLS 상호 인증을 사용하여 시스템 간 인증을 활성화한다. 먼저 `sample01/keystore.jks`에 키 저장소가 있는지 확인한 다음 TLS 상호 인증을 활성화하려면 `application.properties` 파일에서 다음 속성의 주석 처리를 제거한다.

```
server.ssl.client-auth:need
```
Now we can test the flow by invoking the `Order` API using `cURL`. First, use the following command from `ch03/sample01/` directory to spin up the Order API and notice the line which prints the HTTPS port.
```
\> mvn spring-boot:run
Tomcat started on port(s): 8080 (https) with context path ''
```
To test the API with a cURL client, use the following command from a different command console.
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
When you run the preceding command for the first time, it will take a couple of minutes to execute and ends with a command prompt, where you can execute your OpenSSL commands to create the keys, which we used toward the end of the previous sections. The preceding docker run command starts OpenSSL in a Docker container, with a volume mount, which maps `ch03/sample01` (or the current directory, which is indicated by `$(pwd)` in the preceding command) directory from the host file system to the /export directory of the container file system. This volume mount helps you to share part of the host file system with the container file system. When the OpenSSL container generates certificates, those are written to the `/export` directory of the container file system. Since we have a volume mount, everything inside the `/export` directory of the container file system is also accessible from the `ch03/sample01` directory of the host file system.

To generate a private key and a public key for the cURL client, we use the following `OpenSSL` command.

```
# openssl genrsa -out /export/privkey.pem 4096
```
Now, to generate a self-signed certificate, corresponding to the preceding private key (`privkey.pem`), use the following `OpenSSL` command.
```
# openssl req -key /export/privkey.pem -new -x509 -sha256 -nodes -out client.crt -subj "/C=us/ST=ca/L=sjc/O=zee/OU=bar/CN=client"
```
## Summary

- TLS is fundamental in securing any API.

- Securing APIs with TLS is the most common form of protection we see in any API deployment.

- TLS protects data in transit for confidentiality and integrity, and mutual TLS protects your APIs from intruders by enforcing client authentication.

- OpenSSL is a commercial-grade toolkit and cryptographic library for TLS and available for multiple platforms.
