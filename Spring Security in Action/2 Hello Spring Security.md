
2 Hello Spring Security
This chapter covers
- Creating your first project with Spring Security
- Designing simple functionalities using the basic actors for authentication and authorization
- Applying the basic contracts to understand how these actors relate to each other
- Writing your implementations for the primary responsibilities
- Overriding Spring Boot’s default configurations
Spring Boot appeared as an evolutionary stage for application development with the Spring Framework. Instead of you needing to write all the configurations, Spring Boot comes with some preconfigured, so you can override only the configurations that don’t match your implementations. We also call this approach convention-over-configuration.
Before this way of developing applications existed, developers wrote dozens of lines of code again and again for all the apps they had to create. This situation was less visible in the past when we developed most architectures monolithically. With a monolithic architecture, you only had to write these configurations once at the beginning, and you rarely needed to touch them afterward. When service-oriented software architectures evolved, we started to feel the pain of boilerplate code that we needed to write for configuring each service. If you find it amusing, you can check out chapter 3 from Spring in Practice by Willie Wheeler with Joshua White (Manning, 2013). This chapter of an older book describes writing a web application with Spring 3. In this way, you’ll understand how many configurations you had to write for one small one-page web application. Here’s the link for the chapter:
https://livebook.manning.com/book/spring-in-practice/chapter-3/
For this reason, with the development of recent apps and especially those for microservices, Spring Boot became more and more popular. Spring Boot provides autoconfiguration for your project and shortens the time needed for the setup. I would say it comes with the appropriate philosophy for today’s software development.
In this chapter, we’ll start with our first application that uses Spring Security. For the apps that you develop with the Spring Framework, Spring Security is an excellent choice for implementing application-level security. We’ll use Spring Boot and discuss the defaults that are autoconfigured, as well as a brief introduction to overriding these defaults. Considering the default configurations provides an excellent introduction to Spring Security, one that also illustrates the concept of authentication.
Once we get started with the first project, we’ll discuss various options for authentication in more detail. In chapters 3 through 6, we’ll continue with more specific configurations for each of the different responsibilities that you’ll see in this first example. You’ll also see different ways to apply those configurations, depending on architectural styles. The steps we’ll approach in the current chapter follow:
1.	Create a project with only Spring Security and web dependencies to see how it behaves if you don’t add any configuration. This way, you’ll understand what you should expect from the default configuration for authentication and authorization.
2.	Change the project to add functionality for user management by overriding the defaults to define custom users and passwords.
3.	After observing that the application authenticates all the endpoints by default, learn that this can be customized as well.
4.	Apply different styles for the same configurations to understand best practices.
2.1 Starting with the first project
Let’s create the first project so that we have something to work on for the first example. This project is a small web application, exposing a REST endpoint. You’ll see how, without doing much, Spring Security secures this endpoint using HTTP Basic authentication. Just by creating the project and adding the correct dependencies, Spring Boot applies default configurations, including a username and a password when you start the application.
NOTE You have various alternatives to create Spring Boot projects. Some development environments offer the possibility of creating the project directly. If you need help with creating your Spring Boot projects, you can find several ways described in the appendix. For even more details, I recommend Craig Walls’ Spring Boot in Action (Manning, 2016). Chapter 2 from Spring Boot in Action accurately describes creating a web app with Spring Boot (https://livebook.manning.com/book/spring-boot-in-action/chapter-2/).
The examples in this book refer to the source code. With each example, I also specify the dependencies that you need to add to your pom.xml file. You can, and I recommend that you do, download the projects provided with the book and the available source code at https://www.manning.com/downloads/2105. The projects will help you if you get stuck with something. You can also use these to validate your final solutions.
NOTE The examples in this book are not dependent on the build tool you choose. You can use either Maven or Gradle. But to be consistent, I built all the examples with Maven.
The first project is also the smallest one. As mentioned, it’s a simple application exposing a REST endpoint that you can call and then receive a response as described in figure 2.1. This project is enough to learn the first steps when developing an application with Spring Security. It presents the basics of the Spring Security architecture for authentication and authorization.
 
Figure 2.1 Our first application uses HTTP Basic to authenticate and authorize the user against an endpoint. The application exposes a REST endpoint at a defined path (/hello). For a successful call, the response returns an HTTP 200 status message and a body. This example demonstrates how the authentication and authorization configured by default with Spring Security works.
We begin learning Spring Security by creating an empty project and naming it ssia-ch2-ex1. (You’ll also find this example with the same name in the projects provided with the book.) The only dependencies you need to write for our first project are spring-boot-starter-web and spring-boot-starter-security, as shown in listing 2.1. After creating the project, make sure that you add these dependencies to your pom.xml file. The primary purpose of working on this project is to see the behavior of a default configured application with Spring Security. We also want to understand which components are part of this default configuration, as well as their purpose.
Listing 2.1 Spring Security dependencies for our first web app
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-security</artifactId>
</dependency>
We could directly start the application now. Spring Boot applies the default configuration of the Spring context for us based on which dependencies we add to the project. But we wouldn’t be able to learn much about security if we don’t have at least one endpoint that’s secured. Let’s create a simple endpoint and call it to see what happens. For this, we add a class to the empty project, and we name this class HelloController. To do that, we add the class in a package called controllers somewhere in the main namespace of the Spring Boot project.
NOTE Spring Boot scans for components only in the package (and its subpackages) that contains the class annotated with @SpringBootApplication. If you annotate classes with any of the stereotype components in Spring outside of the main package, you must explicitly declare the location using the @ComponentScan annotation.
In the following listing, the HelloController class defines a REST controller and a REST endpoint for our example.
Listing 2.2 The HelloController class and a REST endpoint
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
The @RestController annotation registers the bean in the context and tells Spring that the application uses this instance as a web controller. Also, the annotation specifies that the application has to set the returned value from the body of the HTTP response. The @GetMapping annotation maps the /hello path to the implemented method. Once you run the application, besides the other lines in the console, you should see something that looks similar to this:
Using generated security password: 93a01cf0-794b-4b98-86ef-54860f36f7f3
Each time you run the application, it generates a new password and prints this password in the console as presented in the previous code snippet. You must use this password to call any of the application’s endpoints with HTTP Basic authentication. First, let’s try to call the endpoint without using the Authorization header:
curl http://localhost:8080/hello
NOTE In this book, we use cURL to call the endpoints in all the examples. I consider cURL to be the most readable solution. But if you prefer, you can use a tool of your choice. For example, you might want to have a more comfortable graphical interface. In this case, Postman is an excellent choice. If the operating system you use does not have any of these tools installed, you probably need to install them yourself.
And the response to the call:
{
  "status":401,
  "error":"Unauthorized",
  "message":"Unauthorized",
  "path":"/hello"
}
The response status is HTTP 401 Unauthorized. We expected this result as we didn’t use the proper credentials for authentication. By default, Spring Security expects the default username (user) with the provided password (in my case, the one starting with 93a01). Let’s try it again but now with the proper credentials:
curl -u user:93a01cf0-794b-4b98-86ef-54860f36f7f3 http://localhost:8080/hello
The response to the call now is
Hello!
NOTE The HTTP 401 Unauthorized status code is a bit ambiguous. Usually, it’s used to represent a failed authentication rather than authorization. Developers use it in the design of the application for cases like missing or incorrect credentials. For a failed authorization, we’d probably use the 403 Forbidden status. Generally, an HTTP 403 means that the server identified the caller of the request, but they don’t have the needed privileges for the call that they are trying to make.
Once we send the correct credentials, you can see in the body of the response precisely what the HelloController method we defined earlier returns.
Calling the endpoint with HTTP Basic authentication
With cURL, you can set the HTTP basic username and password with the -u flag. Behind the scenes, cURL encodes the string <username>:<password> in Base64 and sends it as the value of the Authorization header prefixed with the string Basic. And with cURL, it’s probably easier for you to use the -u flag. But it’s also essential to know what the real request looks like. So, let’s give it a try and manually create the Authorization header.
In the first step, take the <username>:<password> string and encode it with Base64. When our application makes the call, we need to know how to form the correct value for the Authorization header. You do this using the Base64 tool in a Linux console. You could also find a web page that encodes strings in Base64, like https://www.base64encode.org. This snippet shows the command in a Linux or a Git Bash console:
echo -n user:93a01cf0-794b-4b98-86ef-54860f36f7f3 | base64
Running this command returns this Base64-encoded string:
dXNlcjo5M2EwMWNmMC03OTRiLTRiOTgtODZlZi01NDg2MGYzNmY3ZjM=
You can now use the Base64-encoded value as the value of the Authorization header for the call. This call should generate the same result as the one using the -u option:
curl -H "Authorization: Basic dXNlcjo5M2EwMWNmMC03OTRiLTRiOTgtODZlZi01
➥ NDg2MGYzNmY3ZjM="        localhost:8080/hello
The result of the call is
Hello!
There’s no significant security configurations to discuss with a default project. We mainly use the default configurations to prove that the correct dependencies are in place. It does little for authentication and authorization. This implementation isn’t something we want to see in a production-ready application. But the default project is an excellent example that you can use for a start.
With this first example working, at least we know that Spring Security is in place. The next step is to change the configurations such that these apply to the requirements of our project. First, we’ll go deeper with what Spring Boot configures in terms of Spring Security, and then we see how we can override the configurations.
2.2 Which are the default configurations?
인증과 권한 부여 과정에 참여하는 전반적인 아키텍처에서의 주요 참가자를 다룬다. 그 이유는 미리 설정된 컴포넌트들을 여러분의 애플리케이션에 맞게 개정을 해줘야 하기 때문이다. 먼저 스프링 시큐리티의 인증과 권한 부여가 동작하는 방식을 설명하고 이 프로젝트에 적용한다. 이 모든 것을 한번에 다루기에는 너무 많으므로 각 컴포넌트에 대한 개략적인 설명을 할 것이다.
앞 절에서는 기본 사용자가 있으며 애플리케이션이 시작할 때 마다 랜덤으로 생성한 패스워드를 얻었다. 기본 사용자인 user와 생성된 암호로 엔드포인트를 호출한다. 하지만 이런 로직이 어디서 구현되었을까? 스프링 부트가 의존성을 보고 대신 이러한 컴포넌트를 설정한다.
아래 그림은 스프링 시큐리티 구조의 메인 컴포넌트들과 그들과의 관계를 보여준다. 이 컴포넌트들은 첫번째 프로젝트에서 미리 설정된 구현체를 가진다.
 
Figure 2.2 The main components acting in the authentication process for Spring Security and the relationships among these. This architecture represents the backbone of implementing authentication with Spring Security. We’ll refer to it often throughout the book when discussing different implementations for authentication and authorization.
In figure 2.2, you can see that
- 인증 필터: 인증 요청을 인증 매니저에게 위임하고 응답에 따라 시큐리티 context를 설정한다.
- 인증 매니저: authentication provider를 사용하여 인증을 처리.
- 인증 제공자: 인증 로직을 구현
- user details 서비스: 인증 제공자가 인증 로직에서 사용할 사용자 관리를 구현
- 패스워드 인코더: 인증 제공자가 인증 로직에서 사용할 암호 관리를 구현
- 시큐리티 컨텍스트: 인증 절차 후에 인증 데이터를 유지
In the following paragraphs, I’ll discuss these autoconfigured beans:
- UserDetailsService
- PasswordEncoder
스프링 시큐리티와 UserDetailsService 계약을 구현하는 객체는 사용자 정보를 관리한다. 아직까지는 스프링 부트가 제공한 기본 구현체를 사용하였다. 이 구현체는 애플리케이션의 내부 메모리에 기본 자격 증명을 등록할 뿐이다. 이러한 기본 자격 증명은 user와 UUID 값인 패스워드이다. 이 패스워드는 랜덤으로 생성되어 컨솔에 표시해준다.   
이러한 기본 구현체는 단지 개념 증명으로서의 역할이며 의존성이 설치되었는지를 확인하는 것이다. 구현체는 메모리에 자격 증명을 저장할 뿐이다. 단지 예제일 뿐이므로 제품에는 적용해서는 안된다.
And then we have the PasswordEncoder. The PasswordEncoder does two things:
- Encodes a password
- Verifies if the password matches an existing encoding
UserDetailsService 객체만큼 명확하지는 않지만 PasswordEncoder는 Basic 인증 흐름에서 필수이다. 가장 단순한 구현체는 패스워드를 암호화 없이 평문으로 관리한다. UserDetailsService의 기본 구현체를 바꿀 때 PasswordEncoder도 지정해야 한다.
또한 스프링 부트는 디폴트인 HTTP Basic 액세스 인증을 설정할 때 인증 메소드를 선택한다. 이것이 가장 단순한 액세스 인증 메소드이다. 기본 인증은 클라이언트가 HTTP 인증 헤더에 사용자 이름과 암호만 보내면 된다. 클라이언트는 헤더 값에는 Basic이 앞에 붙고 username과 password가 있는 문자열을 Base64 인코딩한 스트링이 따라온다.
NOTE HTTP Basic authentication doesn’t offer confidentiality of the credentials. Base64 is only an encoding method for the convenience of the transfer; it’s not an encryption or hashing method. While in transit, if intercepted, anyone can see the credentials. Generally, we don’t use HTTP Basic authentication without at least HTTPS for confidentiality. You can read the detailed definition of HTTP Basic in RFC 7617 (https://tools.ietf.org/html/rfc7617).
AuthenticationProvider는 인증 로직을 정의하고 user와 password 관리를 위임한다. uthenticationProvider의 기본 구현체는 UserDetailsService와 PasswordEncoder를 위해 제공된 기본 구현체를 사용한다. 기본적으로 여러분의 애플리케이션은 모든 엔드포인트를 보호한다. 따라서 우리의 예제에서 할 일은 엔드포인트를 추가하는 것 뿐이다. 또한 모든 엔드포인트에 접근할 사용자가 1명이므로 권한 부여에 대해 할일이 많지 않다. 

HTTP vs. HTTPS
You might have observed that in the examples I presented, I only use HTTP. In practice, however, your applications communicate only over HTTPS. For the examples we discuss in this book, the configurations related to Spring Security aren’t different, whether we use HTTP or HTTPS. So that you can focus on the examples related to Spring Security, I won’t configure HTTPS for the endpoints in the examples. But, if you want, you can enable HTTPS for any of the endpoints as presented in this sidebar.
예제에서는 HTTP만을 사용하지만 실제 환경에서는 HTTPS로 통신한다. HTTP 또는 HTTPS를 사용하든 스프링 시큐리티에 관련된 설정을 다르지 않다. 예제에서는 엔드포인트에 대한 HTTPS 설정을 하지 않으나 원한다면 이 사이드바에 있는 엔트포인트
There are several patterns to configure HTTPS in a system. In some cases, developers configure HTTPS at the application level; in others, they might use a service mesh or they could choose to set HTTPS at the infrastructure level. With Spring Boot, you can easily enable HTTPS at the application level, as you’ll learn in the next example in this sidebar.
In any of these configuration scenarios, you need a certificate signed by a certification authority (CA). Using this certificate, the client that calls the endpoint knows whether the response comes from the authentication server and that nobody intercepted the communication. You can buy such a certificate, but you have to renew it. If you only need to configure HTTPS to test your application, you can generate a self-signed certificate using a tool like OpenSSL. Let’s generate our self-signed certificate and then configure it in the project:
openssl req -newkey rsa:2048 -x509 -keyout key.pem -out cert.pem -days 365
After running the openssl command in a terminal, you’ll be asked for a password and details about your CA. Because it is only a self-signed certificate for a test, you can input any data there; just make sure to remember the password. The command outputs two files: key.pem (the private key) and cert.pem (a public certificate). We’ll use these files further to generate our self-signed certificate for enabling HTTPS. In most cases, the certificate is the Public Key Cryptography Standards #12 (PKCS12). Less frequently, we use a Java KeyStore (JKS) format. Let’s continue our example with a PKCS12 format. For an excellent discussion on cryptography, I recommend Real-World Cryptography by David Wong (Manning, 2020).
openssl pkcs12 -export -in cert.pem -inkey key.pem -out certificate.p12 -name "certificate"
The second command we use receives as input the two files generated by the first command and outputs the self-signed certificate. Mind that if you run these commands in a Bash shell on a Windows system, you might need to add winpty before it, as shown in the next code snippet:
winpty openssl req -newkey rsa:2048 -x509 -keyout key.pem -out cert.pem -days 365
winpty openssl pkcs12 -export -in cert.pem -inkey key.pem -out certificate.p12 -name "certificate"
Finally, having the self-signed certificate, you can configure HTTPS for your endpoints. Copy the certificate.p12 file into the resources folder of the Spring Boot project and add the following lines to your application.properties file:
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:certificate.p12
server.ssl.key-store-password=12345              ❶
❶ The value of the password is the one you specified when running the second command to generate the PKCS12 certificate file.
The password (in my case, “12345”) was requested in the prompt after running the command for generating the certificate. This is the reason why you don’t see it in the command. Now, let’s add a test endpoint to our application and then call it using HTTPS:
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello!";
    }
}
If you use a self-signed certificate, you should configure the tool you use to make the endpoint call so that it skips testing the authenticity of the certificate. If the tool tests the authenticity of the certificate, it won’t recognize it as being authentic, and the call won’t work. With cURL, you can use the -k option to skip testing the authenticity of the certificate:
curl -k https://localhost:8080/hello!
The response to the call is
Hello!
Remember that even if you use HTTPS, the communication between components of your system isn’t bulletproof. Many times, I’ve heard people say, “I’m not encrypting this anymore, I use HTTPS!” While helpful in protecting communication, HTTPS is just one of the bricks of the security wall of a system. Always treat the security of your system with responsibility and take care of all the layers involved in it.
2.3 Overriding default configurations
Now that you know the defaults of your first project, it’s time to see how you can replace these. You need to understand the options you have for overriding the default components because this is the way you plug in your custom implementations and apply security as it fits your application. And, as you’ll learn in this section, the development process is also about how you write configurations to keep your applications highly maintainable. With the projects we’ll work on, you’ll often find multiple ways to override a configuration. This flexibility can create confusion. I frequently see a mix of different styles of configuring different parts of Spring Security in the same application, which is undesirable. So this flexibility comes with a caution. You need to learn how to choose from these, so this section is also about knowing what your options are.
In some cases, developers choose to use beans in the Spring context for the configuration. In other cases, they override various methods for the same purpose. The speed with which the Spring ecosystem evolved is probably one of the main factors that generated these multiple approaches. Configuring a project with a mix of styles is not desirable as it makes the code difficult to understand and affects the maintainability of the application. Knowing your options and how to use them is a valuable skill, and it helps you better understand how you should configure application-level security in a project.
In this section, you’ll learn how to configure a UserDetailsService and a PasswordEncoder. These two components take part in authentication, and most applications customize them depending on their requirements. While we’ll discuss details about customizing them in chapters 3 and 4, it’s essential to see how to plug in a custom implementation. The implementations we use in this chapter are all provided by Spring Security.
2.3.1 OVERRIDING THE USERDETAILSSERVICE COMPONENT
The first component we talked about in this chapter was UserDetailsService. As you saw, the application uses this component in the process of authentication. In this section, you’ll learn to define a custom bean of type UserDetailsService. We’ll do this to override the default one provided by Spring Security. As you’ll see in more detail in chapter 3, you have the option to create your own implementation or to use a predefined one provided by Spring Security. In this chapter, we aren’t going to detail the implementations provided by Spring Security or create our own implementation just yet. I’ll use an implementation provided by Spring Security, named InMemoryUserDetailsManager. With this example, you’ll learn how to plug this kind of object into your architecture.
NOTE Interfaces in Java define contracts between objects. In the class design of the application, we use interfaces to decouple objects that use one another. To enforce this interface characteristic when discussing those in this book, I mainly refer to them as contracts.
To show you the way to override this component with an implementation that we choose, we’ll change what we did in the first example. Doing so allows us to have our own managed credentials for authentication. For this example, we don’t implement our class, but we use an implementation provided by Spring Security.
In this example, we use the InMemoryUserDetailsManager implementation. Even if this implementation is a bit more than just a UserDetailsService, for now, we only refer to it from the perspective of a UserDetailsService. This implementation stores credentials in memory, which can then be used by Spring Security to authenticate a request.
NOTE An InMemoryUserDetailsManager implementation isn’t meant for production-ready applications, but it’s an excellent tool for examples or proof of concepts. In some cases, all you need is users. You don’t need to spend the time implementing this part of the functionality. In our case, we use it to understand how to override the default UserDetailsService implementation.
We start by defining a configuration class. Generally, we declare configuration classes in a separate package named config. Listing 2.3 shows the definition for the configuration class. You can also find the example in the project ssia-ch2-ex2.
NOTE The examples in this book are designed for Java 11, which is the latest long-term supported Java version. For this reason, I expect more and more applications in production to use Java 11. So it makes a lot of sense to use this version for the examples in this book.
You’ll sometimes see that I use var in the code. Java 10 introduced the reserved type name var, and you can only use it for local declarations. In this book, I use it to make the syntax shorter, as well as to hide the variable type. We’ll discuss the types hidden by var in later chapters, so you don’t have to worry about that type until it’s time to analyze it properly.
Listing 2.3 The configuration class for the UserDetailsService bean
@Configuration                                      ❶
public class ProjectConfig {

  @Bean                                             ❷
  public UserDetailsService userDetailsService() {
    var userDetailsService = 
        new InMemoryUserDetailsManager();           ❸
        
    return userDetailsService;
  }
}
❶ The @Configuration annotation marks the class as a configuration class.
❷ The @Bean annotation instructs Spring to add the returned value as a bean in the Spring context.
❸ The var word makes the syntax shorter and hides some details.
We annotate the class with @Configuration. The @Bean annotation instructs Spring to add the instance returned by the method to the Spring context. If you execute the code exactly as it is now, you’ll no longer see the autogenerated password in the console. The application now uses the instance of type UserDetailsService you added to the context instead of the default autoconfigured one. But, at the same time, you won’t be able to access the endpoint anymore for two reasons:
- You don’t have any users.
- You don’t have a PasswordEncoder.
In figure 2.2, you can see that authentication depends on a PasswordEncoder as well. Let’s solve these two issues step by step. We need to
1.	Create at least one user who has a set of credentials (username and password)
2.	Add the user to be managed by our implementation of UserDetailsService
3.	Define a bean of the type PasswordEncoder that our application can use to verify a given password with the one stored and managed by UserDetailsService
First, we declare and add a set of credentials that we can use for authentication to the instance of InMemoryUserDetailsManager. In chapter 3, we’ll discuss more about users and how to manage them. For the moment, let’s use a predefined builder to create an object of the type UserDetails.
When building the instance, we have to provide the username, the password, and at least one authority. The authority is an action allowed for that user, and we can use any string for this. In listing 2.4, I name the authority read, but because we won’t use this authority for the moment, this name doesn’t really matter.
Listing 2.4 Creating a user with the User builder class for UserDetailsService
@Configuration
public class ProjectConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    var userDetailsService = 
        new InMemoryUserDetailsManager();

    var user = User.withUsername("john")      ❶
            .password("12345")                ❶
            .authorities("read")              ❶
            .build();                         ❶
        
    userDetailsService.createUser(user);      ❷

    return userDetailsService;
  }
}
❶ Builds the user with a given username, password, and authorities list
❷ Adds the user to be managed by UserDetailsService
NOTE You’ll find the class User in the org.springframework.security.core .userdetails package. It’s the builder implementation we use to create the object to represent the user. Also, as a general rule in this book, if I don’t present how to write a class in a code listing, it means Spring Security provides it.
As presented in listing 2.4, we have to provide a value for the username, one for the password, and at least one for the authority. But this is still not enough to allow us to call the endpoint. We also need to declare a PasswordEncoder.
When using the default UserDetailsService, a PasswordEncoder is also auto-configured. Because we overrode UserDetailsService, we also have to declare a PasswordEncoder. Trying the example now, you’ll see an exception when you call the endpoint. When trying to do the authentication, Spring Security realizes it doesn’t know how to manage the password and fails. The exception looks like that in the next code snippet, and you should see it in your application’s console. The client gets back an HTTP 401 Unauthorized message and an empty response body:
curl -u john:12345 http://localhost:8080/hello
The result of the call in the app’s console is
java.lang.IllegalArgumentException: There is no PasswordEncoder mapped for
➥ the id "null"
    at org.springframework.security.crypto.password
     ➥ .DelegatingPasswordEncoder$UnmappedIdPasswordEncoder
     ➥ .matches(DelegatingPasswordEncoder.java:244)
     ➥ ~[spring-security-core-5.1.6.RELEASE.jar:5.1.6.RELEASE] 
To solve this problem, we can add a PasswordEncoder bean in the context, the same as we did with the UserDetailsService. For this bean, we use an existing implementation of PasswordEncoder:
@Bean
public PasswordEncoder passwordEncoder() {
  return NoOpPasswordEncoder.getInstance();
}
NOTE The NoOpPasswordEncoder instance treats passwords as plain text. It doesn’t encrypt or hash them. For matching, NoOpPasswordEncoder only compares the strings using the underlying equals(Object o) method of the String class. You shouldn’t use this type of PasswordEncoder in a production-ready app. NoOpPasswordEncoder is a good option for examples where you don’t want to focus on the hashing algorithm of the password. Therefore, the developers of the class marked it as @Deprecated, and your development environment will show its name with a strikethrough.
You can see the full code of the configuration class in the following listing.
Listing 2.5 The full definition of the configuration class
@Configuration
public class ProjectConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    var userDetailsService = new InMemoryUserDetailsManager();

    var user = User.withUsername("john")
            .password("12345")
            .authorities("read")
            .build();

    userDetailsService.createUser(user);

    return userDetailsService;
  }

  @Bean                                         ❶
  public PasswordEncoder passwordEncoder() {    ❶
    return NoOpPasswordEncoder.getInstance();   ❶
  }                                             ❶
}
❶ A new method annotated with @Bean to add a PasswordEncoder to the context
Let’s try the endpoint with the new user having the username John and the password 12345:
curl -u john:12345 http://localhost:8080/hello
Hello!
NOTE Knowing the importance of unit and integration tests, some of you might already wonder why we don’t also write tests for our examples. You will actually find the related Spring Security integration tests with all the examples provided with this book. However, to help you focus on the presented topics for each chapter, I have separated the discussion about testing Spring Security integrations and detail this in chapter 20.
2.3.2 OVERRIDING THE ENDPOINT AUTHORIZATION CONFIGURATION
With new management for the users in place, as described in section 2.3.1, we can now discuss the authentication method and configuration for endpoints. You’ll learn plenty of things regarding authorization configuration in chapters 7, 8, and 9. But before diving into details, you must understand the big picture. And the best way to achieve this is with our first example. With default configuration, all the endpoints assume you have a valid user managed by the application. Also, by default, your app uses HTTP Basic authentication as the authorization method, but you can easily override this configuration.
As you’ll learn in the next chapters, HTTP Basic authentication doesn’t fit into most application architectures. Sometimes we’d like to change it to match our application. Similarly, not all endpoints of an application need to be secured, and for those that do, we might need to choose different authorization rules. To make such changes, we start by extending the WebSecurityConfigurerAdapter class. Extending this class allows us to override the configure(HttpSecurity http) method as presented in the next listing. For this example, I’ll continue writing the code in the project ssia-ch2-ex2.
Listing 2.6 Extending WebSecurityConfigurerAdapter
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // ... 
  }
}
We can then alter the configuration using different methods of the HttpSecurity object as shown in the next listing.
Listing 2.7 Using the HttpSecurity parameter to alter the configuration
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {
  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests()                 ❶
          .anyRequest().authenticated();     ❶
  }
}
❶ All the requests require authentication.
The code in listing 2.7 configures endpoint authorization with the same behavior as the default one. You can call the endpoint again to see that it behaves the same as in the previous test from section 2.3.1. With a slight change, you can make all the endpoints accessible without the need for credentials. You’ll see how to do this in the following listing.
Listing 2.8 Using permitAll() to change the authorization configuration
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests()              ❶
           .anyRequest().permitAll();     ❶
  }
}
❶ None of the requests need to be authenticated.
Now, we can call the /hello endpoint without the need for credentials. The permitAll() call in the configuration, together with the anyRequest() method, makes all the endpoints accessible without the need for credentials:
curl http://localhost:8080/hello
And the response body of the call is
Hello!
The purpose of this example is to give you a feeling for how to override default configurations. We’ll get into the details about authorization in chapters 7 and 8.
2.3.3 SETTING THE CONFIGURATION IN DIFFERENT WAYS
One of the confusing aspects of creating configurations with Spring Security is having multiple ways to configure the same thing. In this section, you’ll learn alternatives for configuring UserDetailsService and PasswordEncoder. It’s essential to know the options you have so that you can recognize these in the examples that you find in this book or other sources like blogs and articles. It’s also important that you understand how and when to use these in your application. In further chapters, you’ll see different examples that extend the information in this section.
Let’s take the first project. After we created a default application, we managed to override UserDetailsService and PasswordEncoder by adding new implementations as beans in the Spring context. Let’s find another way of doing the same configurations for UserDetailsService and PasswordEncoder.
In the configuration class, instead of defining these two objects as beans, we set them up through the configure(AuthenticationManagerBuilder auth) method. We override this method from the WebSecurityConfigurerAdapter class and use its parameter of type AuthenticationManagerBuilder to set both the UserDetailsService and the PasswordEncoder as shown in the following listing. You can find this example in the project ssia-ch2-ex3.
Listing 2.9 Setting UserDetailsService and PasswordEncoder in configure()
@Configuration
public class ProjectConfig 
   extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(
      AuthenticationManagerBuilder auth) 
         throws  Exception {
    var userDetailsService = 
        new InMemoryUserDetailsManager();                ❶

    var user = User.withUsername("john")                 ❷
                .password("12345")                       ❷
                .authorities("read")                     ❷
                .build();                                ❷

    userDetailsService.createUser(user);                 ❸

    auth.userDetailsService(userDetailsService)          ❹
        .passwordEncoder(NoOpPasswordEncoder.getInstance());
  }
}
❶ Declares a UserDetailsSevice to store the users in memory
❷ Defines a user with all its details
❸ Adds the user to be managed by our UserDetailsSevice
❹ The UserDetailsService and PasswordEncoder are now set up within the configure() method.
In listing 2.9, you can observe that we declar the UserDetailsService in the same way as in listing 2.5. The difference is that now this is done locally inside the second overridden method. We also call the userDetailsService() method from the AuthenticationManagerBuilder to register the UserDetailsService instance. Furthermore, we call the passwordEncoder() method to register the PasswordEncoder. Listing 2.10 shows the full contents of the configuration class.
NOTE The WebSecurityConfigurerAdapter class contains three different overloaded configure() methods. In listing 2.9, we overrode a different one than in listing 2.8. In the next chapters, we’ll discuss all three in more detail.
Listing 2.10 Full definition of the configuration class
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure
  ➥ (AuthenticationManagerBuilder auth) throws Exception {
    var userDetailsService =
            new InMemoryUserDetailsManager();      ❶

    var user = User.withUsername("john")           ❷
            .password("12345")                     ❷
            .authorities("read")                   ❷
            .build();                              ❷

        userDetailsService.createUser(user);       ❸

    auth.userDetailsService(userDetailsService)    ❹
         .passwordEncoder(                         ❹
            NoOpPasswordEncoder.getInstance());    ❹
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests()                       ❺
          .anyRequest().authenticated();           ❺
  }
}
❶ Creates an instance of InMemoryUserDetailsManager()
❷ Creates a new user
❸ Adds the user to be managed by our UserDetailsService
❹ Configures UserDetailsService and PasswordEncoder
❺ Specifies that all the requests require authentication
Any of these configuration options are correct. The first option, where we add the beans to the context, lets you inject the values in another class where you might potentially need them. But if you don’t need that for your case, the second option would be equally good. However, I recommend you avoid mixing configurations because it might create confusion. For example, the code in the following listing could make you wonder about where the link between the UserDetailsService and PasswordEncoder is.
Listing 2.11 Mixing configuration styles
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  @Bean
  public PasswordEncoder passwordEncoder() {        ❶
    return NoOpPasswordEncoder.getInstance();
  }

  @Override
  protected void configure
  ➥ (AuthenticationManagerBuilder auth) throws Exception {
    var userDetailsService = new InMemoryUserDetailsManager();

    var user = User.withUsername("john")
                .password("12345")
                .authorities("read")
                .build();

    userDetailsService.createUser(user);

    auth.userDetailsService(userDetailsService);    ❷
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests()
          .anyRequest().authenticated();
  }
}
❶ Designs the PasswordEncoder as a bean
❷ Configures the UserDetailsService directly in the configure() method
Functionally, the code in listing 2.11 works just fine, but again, I recommend you avoid mixing the two approaches to keep the code clean and easier to understand. Using the AuthenticationManagerBuilder, you can configure users for authentication directly. It creates the UserDetailsService for you in this case. The syntax, however, becomes even more complex and could be considered difficult to read. I’ve seen this choice more than once, even with production-ready systems.
It could be that this example looks fine because we use an in-memory approach to configure users. But in a production application, this isn’t the case. There, you probably store your users in a database or access them from another system. As in this case, the configuration could become pretty long and ugly. Listing 2.12 shows the way you can write the configuration for in-memory users. You’ll find this example applied in the project ssia-ch2-ex4.
Listing 2.12 Configuring in-memory user management
@Override
protected void configure
➥ (AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
        .withUser("john")
        .password("12345")
        .authorities("read")
    .and()
        .passwordEncoder(NoOpPasswordEncoder.getInstance());
}
Generally, I don’t recommend this approach, as I find it better to separate and write responsibilities as decoupled as possible in an application.
2.3.4 OVERRIDING THE AUTHENTICATIONPROVIDER IMPLEMENTATION
As you’ve already observed, Spring Security components provide a lot of flexibility, which offers us a lot of options when adapting these to the architecture of our applications. Up to now, you’ve learned the purpose of UserDetailsService and PasswordEncoder in the Spring Security architecture. And you saw a few ways to configure them. It’s time to learn that you can also customize the component that delegates to these, the AuthenticationProvider.
Figure 2.3 shows the AuthenticationProvider, which implements the authentication logic and delegates to the UserDetailsService and PasswordEncoder for user and password management. So we could say that with this section, we go one step deeper in the authentication and authorization architecture to learn how to implement custom authentication logic with AuthenticationProvider.
Because this is a first example, I only show you the brief picture so that you better understand the relationship between the components in the architecture. But we’ll detail more in chapters 3, 4, and 5. In those chapters, you’ll find the AuthenticationProvider implemented, as well as in a more significant exercise, the first “Hands-On” section of the book, chapter 6.
 
Figure 2.3 The AuthenticationProvider implements the authentication logic. It receives the request from the AuthenticationManager and delegates finding the user to a UserDetails-Service, and verifying the password to a PasswordEncoder.
I recommend that you respect the responsibilities as designed in the Spring Security architecture. This architecture is loosely coupled with fine-grained responsibilities. That design is one of the things that makes Spring Security flexible and easy to integrate in your applications. But depending on how you make use of its flexibility, you could change the design as well. You have to be careful with these approaches as they can complicate your solution. For example, you could choose to override the default AuthenticationProvider in a way in which you no longer need a UserDetailsService or PasswordEncoder. With that in mind, the following listing shows how to create a custom authentication provider. You can find this example in the project ssia-ch2-ex5.
Listing 2.13 Implementing the AuthenticationProvider interface
@Component
public class CustomAuthenticationProvider 
➥ implements AuthenticationProvider {

  @Override
  public Authentication authenticate
  ➥ (Authentication authentication) throws AuthenticationException {

        // authentication logic here
  }

  @Override
  public boolean supports(Class<?> authenticationType) {

    // type of the Authentication implementation here
  }
}
The authenticate(Authentication authentication) method represents all the logic for authentication, so we’ll add an implementation like that in listing 2.14. I’ll explain the usage of the supports() method in detail in chapter 5. For the moment, I recommend you take its implementation for granted. It’s not essential for the current example.
Listing 2.14 Implementing the authentication logic
@Override
public Authentication authenticate
➥ (Authentication authentication) 
  throws AuthenticationException {

  String username = authentication.getName();             ❶
  String password = String.valueOf(authentication.getCredentials());

  if ("john".equals(username) && 
      "12345".equals(password)) {                         ❷
      
    return new UsernamePasswordAuthenticationToken
    ➥ (username, password, Arrays.asList());
  } else {
    throw new AuthenticationCredentialsNotFoundException
    ➥ ("Error in authentication!");
  }
}
❶ The getName() method is inherited by Authentication from the Principal interface.
❷ This condition generally calls UserDetailsService and PasswordEncoder to test the username and password.
As you can see, here the condition of the if-else clause is replacing the responsibilities of UserDetailsService and PasswordEncoder. Your are not required to use the two beans, but if you work with users and passwords for authentication, I strongly suggest you separate the logic of their management. Apply it as the Spring Security architecture designed it, even when you override the authentication implementation.
You might find it useful to replace the authentication logic by implementing your own AuthenticationProvider. If the default implementation doesn’t fit entirely into your application’s requirements, you can decide to implement custom authentication logic. The full AuthenticationProvider implementation looks like the one in the next listing.
Listing 2.15 The full implementation of the authentication provider
@Component
public class CustomAuthenticationProvider 
➥ implements AuthenticationProvider {

  @Override
  public Authentication authenticate
  ➥ (Authentication authentication) 
      throws AuthenticationException {
      
      String username = authentication.getName();
      String password = String.valueOf(authentication.getCredentials());

      if ("john".equals(username) && 
          "12345".equals(password)) {
        return new UsernamePasswordAuthenticationToken
        ➥ (username, password, Arrays.asList());
      } else {
        throw new AuthenticationCredentialsNotFoundException("Error!");
      }
    }

  @Override
  public boolean supports(Class<?> authenticationType) {
    return UsernamePasswordAuthenticationToken.class
                  .isAssignableFrom(authenticationType);
  }
}
In the configuration class, you can register the AuthenticationProvider in the configure(AuthenticationManagerBuilder auth) method shown in the following listing.
Listing 2.16 Registering the new implementation of AuthenticationProvider
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private CustomAuthenticationProvider authenticationProvider;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(authenticationProvider);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests().anyRequest().authenticated();
  }
}
You can now call the endpoint, which is accessible by the only user recognized, as defined by the authentication logic--John, with the password 12345:
curl -u john:12345 http://localhost:8080/hello
The response body is
Hello!
In chapter 5, you’ll learn more details about the AuthenticationProvider and how to override its behavior in the authentication process. In that chapter, we’ll also discuss the Authentication interface and its implementations, such as the UserPasswordAuthenticationToken.
2.3.5 USING MULTIPLE CONFIGURATION CLASSES IN YOUR PROJECT
In the previously implemented examples, we only used a configuration class. It is, however, good practice to separate the responsibilities even for the configuration classes. We need this separation because the configuration starts to become more complex. In a production-ready application, you probably have more declarations than in our first examples. You also might find it useful to have more than one configuration class to make the project readable.
It’s always a good practice to have only one class per each responsibility. For this example, we can separate user management configuration from authorization configuration. We do that by defining two configuration classes: UserManagementConfig (defined in listing 2.17) and WebAuthorizationConfig (defined in listing 2.18). You can find this example in the project ssia-ch2-ex6.
Listing 2.17 Defining the configuration class for user and password management
@Configuration
public class UserManagementConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    var userDetailsService = new InMemoryUserDetailsManager();

    var user = User.withUsername("john")
            .password("12345")
            .authorities("read")
            .build();

    userDetailsService.createUser(user);
    return userDetailsService;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
In this case, the UserManagementConfig class only contains the two beans that are responsible for user management: UserDetailsService and PasswordEncoder. We’ll configure the two objects as beans because this class can’t extend WebSecurityConfigurerAdapter. The next listing shows this definition.
Listing 2.18 Defining the configuration class for authorization management
@Configuration
public class WebAuthorizationConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();
    http.authorizeRequests().anyRequest().authenticated();
  }
}
Here the WebAuthorizationConfig class needs to extend WebSecurityConfigurerAdapter and override the configure(HttpSecurity http) method.
NOTE You can’t have both classes extending WebSecurityConfigurerAdapter in this case. If you do so, the dependency injection fails. You might solve the dependency injection by setting the priority for injection using the @Order annotation. But, functionally, this won’t work, as the configurations exclude each other instead of merging.
Summary
- Spring Boot provides some default configurations when you add Spring Security to the dependencies of the application.
- You implement the basic components for authentication and authorization: UserDetailsService, PasswordEncoder, and AuthenticationProvider.
- You can define users with the User class. A user should at least have a username, a password, and an authority. Authorities are actions that you allow a user to do in the context of the application.
- A simple implementation of a UserDetailsService that Spring Security provides is InMemoryUserDetailsManager. You can add users to such an instance of UserDetailsService to manage the user in the application’s memory.
- The NoOpPasswordEncoder is an implementation of the PasswordEncoder contract that uses passwords in cleartext. This implementation is good for learning examples and (maybe) proof of concepts, but not for production-ready applications.
- You can use the AuthenticationProvider contract to implement custom authentication logic in the application.
- There are multiple ways to write configurations, but in a single application, you should choose and stick to one approach. This helps to make your code cleaner and easier to understand.
- Copy
- Add Highlight
- Add Note