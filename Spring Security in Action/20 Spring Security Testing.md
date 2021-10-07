
 
20 Spring Security testing
This chapter covers
- Testing integration with Spring Security configurations for endpoints
- Defining mock users for tests
- Testing integration with Spring Security for method-level security
- Testing reactive Spring implementations
The legend says that writing unit and integration tests started with a short verse:
“99 little bugs in the code,
99 little bugs.
Track one down, patch it around,
There’s 113 little bugs in the code.”
--Anonymous
With time, software became more complex, and teams became larger. Knowing all the functionalities implemented over time by others became impossible. Developers needed a way to make sure they didn’t break existing functionalities while correcting bugs or implementing new features.
While developing applications, we continuously write tests to validate that the functionalities we implement work as desired. The main reason why we write unit and integration tests is to make sure we don’t break existing functionalities when changing code for fixing a bug or for implementing new features. This is also called regression testing.
Nowadays, when a developer finishes making a change, they upload the changes to a server used by the team to manage code versioning. This action automatically triggers a continuous integration tool that runs all existing tests. If any of the changes break an existing functionality, the tests fail, and the continuous integration tool notifies the team (figure 20.1). This way, it’s less likely to deliver changes that affect existing features.
 
Figure 20.1 Testing is part of the development process. Anytime a developer uploads code, the tests run. If any test fails, a continuous integration tool notifies the developer.
NOTE By using Jenkins in this figure, I say neither that this is the only continuous integration tool used or that it’s the best one. You have many alternatives to choose from like Bamboo, GitLab CI, CircleCI, and so on.
When testing applications, you need to remember it’s not only your application code that you need to test. You need to also make sure you test the integrations with the
frameworks and libraries you use, as well (figure 20.2). Sometime in the future, you may upgrade that framework or library to a new version. When changing the version of a dependency, you want to make sure your app still integrates well with the new version of that dependency. If your app doesn’t integrate in the same way, you want to easily find where you need to make changes to correct the integration problems.
 
Figure 20.2 The functionality of an application relies on many dependencies. When you upgrade or change a dependency, you might affect existing functionality. Having integration tests with dependencies helps you to discover quickly if a change in a dependency affects the existing functionality of your application.
So that’s why you need to know what we’ll cover in this chapter--how to test your app’s integration with Spring Security. Spring Security, like the Spring framework ecosystem in general, evolves quickly. You probably upgrade your app to new versions, and you certainly want to be aware if upgrading to a specific version develops vulnerabilities, errors, or incompatibilities in your application. Remember what we discussed right from the first chapter: you need to consider security from the first design for the app, and you need to take it seriously. Implementing tests for any of your security configurations should be a mandatory task and should be defined as part of your definition of “done.” You shouldn’t consider a task finished if security tests aren’t ready.
In this chapter, we’ll discuss several practices for testing an app’s integration with Spring Security. We’ll go back to some of the examples we worked on in previous chapters, and you’ll learn how to write integration tests for implemented functionality. Testing, in general, is an epic story. But learning this subject in detail brings many benefits.
In this chapter, we’ll focus on testing integration between an application and Spring Security. Before starting our examples, I’d like to recommend a few resources that helped me understand this subject deeply. If you need to understand the subject more in detail, or even as a refresher, you can read these books. I am positive you’ll find these great!
- JUnit in Action, 3rd ed. by Cătălin Tudose et al. (Manning, 2020)
- Unit Testing Principles, Practices, and Patterns by Vladimir Khorikov (Manning, 2020)
- Testing Java Microservices by Alex Soto Bueno et al. (Manning, 2018)
구현한 보안 코드의 테스트의 시작은 인증 설정 테스트로 시작한다. 20.1절에서는 인증을 스킵하고 엔드포인트 레벨에서 인증 설정을 테스트하기 위한 모형 사용자를 정의하는 방법을 배울 것이다. 20.2절은 여러분이 구현한 특정한 인증 객체를 사용할 필요가 있는 경우에서 보안 컨텍스트 전체를 설정하는 방법을 다룰 것이다. 마지막으로 20.4절에서는 앞 절에 배운 인증 설정 테스트를 메소드 보안에 적용해 볼 것이다.
인증 테스트를 다루었다면 20.5절은 인증 흐름도를 테스트하는 방법을 다룬다. 그리고 CSRF와 CORS와 같은 또 다른 보안 설정을 테스트하는 방법을 다룬다. 끝으로 스프링 시큐리티와 리액티브 앱의 통합 테스트를 다룬다.
20.1 Using mock users for tests
모의 사용자로 인증 설정을 테스트 한다. 이 방식이가장 단순하며 자주 사용되는 방법이다. 모의 사용자를 사용할 때 테스트는 인증 절차를 생략한다. 모의 사용자는 테스트할때만 유효하며 이 사용자에 대해서 특정 시나리오를 검증하기 위해 필요한 특성을 설정할 수 있다. 예를 들어 사용자에게 ADMIN, MANAGER 등과 같은 역할을 부여하거나 다른 권한들을 사용하여 앱이 이러한 조건에서 예상대로 행동하는지를 검증한다.
 
Figure 20.3 We skip the shaded components in the Spring Security authentication flow when executing a test. The test directly uses a mock SecurityContext, which contains the mock user you define to call the tested functionality.
NOTE It’s important to know which components from the framework are involved in an integration test. This way, you know which part of the integration you cover with the test. For example, a mock user can only be used to cover authorization. (In section 20.5, you’ll learn how to deal with authentication.) I sometimes see developers getting confused on this aspect. They thought they were also covering, for example, a custom implementation of an AuthenticationProvider when working with a mock user, which is not the case. Make sure you correctly understand what you’re testing.
To prove how to write such a test, let’s go back to the simplest example we worked on in this book, the project ssia-ch2-ex1. This project exposes an endpoint for the path /hello with only the default Spring Security configuration. What do we expect to happen?
- When calling the endpoint without a user, the HTTP response status should be 401 Unauthorized.
- When calling the endpoint having an authenticated user, the HTTP response status should be 200 OK, and the response body should be Hello!.
Let’s test these two scenarios! We need a couple of dependencies in the pom.xml file to write the tests. The next code snippet shows you the classes we use throughout the examples in this chapter. You should make sure you have these in your pom.xml file before starting to write the tests. Here are the dependencies:
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-test</artifactId>
   <scope>test</scope>
   <exclusions>
      <exclusion>
         <groupId>org.junit.vintage</groupId>
         <artifactId>junit-vintage-engine</artifactId>
      </exclusion>
   </exclusions>
</dependency>
<dependency>
   <groupId>org.springframework.security</groupId>
   <artifactId>spring-security-test</artifactId>
   <scope>test</scope>
</dependency>
NOTE For the examples in this chapter, we use JUnit 5 for writing tests. But don’t be discouraged if you still work with JUnit 4. From the Spring Security integration point of view, the annotations and the rest of the classes you’ll learn work the same. Chapter 4 of JUnit in Action by Cătălin Tudose et al. (Manning, 2020), which is a dedicated discussion about migrating from JUnit 4 to JUnit 5, contains some interesting tables that show the correspondence between classes and annotations of versions 4 and 5. Here’s the link: https://livebook.manning.com/book/junit-in-action-third-edition/chapter-4.
In the test folder of the Spring Boot Maven project, we add a class named MainTests. We write this class as part of the main package of the application. The name of the main package is com.laurentiuspilca.ssia. In listing 20.1, you can find the definition of the empty class for the tests. We use the @SpringBootTest annotation, which represents a convenient way to manage the Spring context for our test suite.
Listing 20.1 A class for writing the tests
@SpringBootTest          ❶
public class MainTests {
}
❶ Makes Spring Boot responsible for managing the Spring context for the tests
스프링의 MockMvc를 사용하면 엔드포인트의 행동의 테스트를 편리하게 구현할 수 있다. 
Listing 20.2 Adding MockMvc for implementing test scenarios
@SpringBootTest
@AutoConfigureMockMvc       ❶
public class MainTests {
  @Autowired
  private MockMvc mvc;      ❷
}
❶ Enables Spring Boot to autoconfigure MockMvc. As a consequence, an object of type MockMvc is added to the Spring context.
❷ Injects the MockMvc object that we use to test the endpoint
이제 엔드포인트 행동을 테스트할 수 있는 수단이 생겼으므로 첫번째 시나리오를 시작해보자. 사용자 인증없이 /hello 엔드포인트를 부르면 HTTP 응답 상태는 401 Unauthorized이어야 한다. 
아래 그림에 이 테스트를 실행하는 컴포넌트들 간의 관계를 볼 수 있다. 테스트는 모의 SecurityContext를 사용하여 엔드포인트를 부른다. 우리는 이 SecurityContet에 뭔가를 추가하기로 했다. 이 테스트는 누군가 인증 없이 엔드포인트를 부르는 상황을 나타내는 사용자를 추가하지 않으면 앱이 401 Unauthorized 응답을 하는지를 확인해야한다. SecurityContext에 사용자를 추가하면, 앱이 호출을 수락하며 HTTP 응답은 200 OK이다.
 
Figure 20.4 테스트를 실행할 때 인증은 건너뛴다. 테스트는 모의 SecurityContext를 사용해서 /hello 엔드포인트를 부른다. SecurityContext에 모의 사용자를 추가하여 행동을 검증하는 것이 인증 규칙에 따라 정확하다. 모의 사용자를 정의하지 않으면 앱은 호출을 인증하지 않지만 사용자를 추가하면 호출이 성공한다.
The following listing presents this scenario’s implementation.
Listing 20.3 Testing that you can’t call the endpoint without an authenticated user
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void helloUnauthenticated() throws Exception {
    mvc.perform(get("/hello"))  ❶
         .andExpect(status().isUnauthorized()); 
  }

}
❶ When performing a GET request for the /hello path, we expect to get back a response with the status Unauthorized.
Mind that we statically import the methods get() and status(). You find the method get() and similar methods related to the requests we use in the examples of this chapter in this class:
org.springframework.test.web.servlet.request.MockMvcRequestBuilders
Also, you find the method status() and similar methods related to the result of the calls that we use in the next examples of this chapter in this class:
org.springframework.test.web.servlet.result.MockMvcResultMatchers
이제 테스트를 실행하여 그 결과를 볼 수 있다. 성공하면 녹색으로 실패하면 빨강으로 표시할 것이다.
NOTE In the projects provided with the book, above each method implementing a test, I also use the @DisplayName annotation. This annotation allows us to have a longer, more detailed description of the test scenario. To occupy less space and allow you to focus on the functionality of the tests we discuss, I took the @DisplayName annotation out of the listings in the book.
두번째 시나리오는 모의 사용자가 필요하다. 승인된 사용자가 /hello를 부르는 것을 검증하기 위해서 @WithMockUser 주석을 사용한다. 테스트 메소드 위에 이 주석을 달면 SecurityContext에 UserDetails 인스턴스가 설정되도록 한다. 이것은 기본적으로 인증을 생략한다. 이제 엔드포인트를 부르면 @WithMockUser로 정의된 사용자가 인증된 것처럼 행동한다. 
이 예제에서는 모의 사용자의 이름, 역할 또는 권한과 같은 정보에 대해서는 관심이 없다. 따라서 @WithMockUser 주석을 추가하면 모의 사용자의 속성에 디폴트 값이 적용된다. 

Listing 20.4 Using @WithMockUser to define a mock authenticated user
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  // Omitted code

  @Test 
  @WithMockUser ❶
  public void helloAuthenticated() throws Exception {
    mvc.perform(get("/hello"))  ❷
         .andExpect(content().string("Hello!"))
         .andExpect(status().isOk());
  }

}
❶ Calls the method with a mock authenticated user
❷ In this case, when performing a GET request for the /hello path, we expect the response status to be OK.
어떤 상황에서는 특정한 사용자 이름이나 역할을 검증해야 하나. 이번에는 인증된 사용자 이름에 따라 응답하는 테스트를 작성한다.(ssia-ch5-ex2)
Listing 20.5 Configuring details for the mock user
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {
     
  // Omitted code

  @Test
  @WithMockUser(username = "mary")                     ❶
  public void helloAuthenticated() throws Exception {
    mvc.perform(get("/hello"))
         .andExpect(content().string("Hello, mary!"))
         .andExpect(status().isOk());
    }
}
❶ Sets up a username for the mock user
아래 그림은 주석을 사용한 테스트 환경 정의와 RequestPostProcessor를 사용할 때와의 차이를 보여준다. 프레임워크는 테스트 메소드를 수행하기 전에 @WithMockUser를 해석한다. RequestPostProcessor를 사용할 경우 프레임워크는 먼저 테스트 메소드를 호출한 다음 테스트 요청을 빌드한다.
사용자 이름을 설정하듯이 권한과 역할도 설정할 수 있다. RequestPostProcessor로도 모의 사용자를 만들 수 있다. RequestPostProcessor에 with() 메소드를 제공할 수 있다. SecurityMockMvcRequestPostProcessors클래스는 수많은 RequestPostProcessor 구현체를 제공한다. SecurityMockMvcRequestPostProcessors 클래스의 user() 메소드가 RequestPostProcessor를 리턴한다.
 
Figure 20.5 시큐리티 테스트 환경을 만들 때 주석을 사용할 때와 RequestPostProcessor를 사용할 때의 차이. 주석을 사용하면 프레임워크가 먼저 테스트 시큐리티 환경을 설정하다. RequestPostProcessor를 사용하면 테스트 요청을 만든 다음 테스트 환경과 같은 다른 제약을 정의하기 위해 변경된다. 프레임워크가 테스트 환경을 적용하는 부분을 음영 처리.
Listing 20.6 Using a RequestPostProcessor to define a mock user
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  // Omitted code

  @Test
  public void helloAuthenticatedWithUser() throws Exception {
    mvc.perform(
          get("/hello")
            .with(user("mary")))                 ❶
        .andExpect(content().string("Hello!"))
        .andExpect(status().isOk());
  }
}
❶ Calls the /hello endpoint using a mock user with the username Mary
앱의 기능과 스프링 시큐리티 통합을 위해 우리가 작성하는 대부분의 테스트는 인증 설정을 위한 것이다. 아마도 왜 권한은 테스트 하지 않는지 의아할 것이다. 20.5절에서 권한 테스트를 다룬다. 하지만 일반적으로 인증과 권한은 별도로 테스트하는 것이 좋다. 일반적으로 앱에는 사용자를 인증하는 한가지 방법이 있지만 권한이 다르게 구성된 수십개의 엔드포인트를 노출할 수 있다. 따라서 몇가지 테스트를 통해 개별적으로 인증을 테스트 한 다음에 엔드포인트의 각 인증 설정에 대해 구현하는 것이다. 로직이 바뀌지 않는다면 테스트된 각 엔트 포인트에 매번 인증을 반복하는 것은 시간 낭비이다.
20.2 Testing with users from a UserDetailsService
여기서는 UserDetailsService로부터 테스트를 위한 사용자 정보를 얻는 것을 다룬다. 가짜인 모의 사용자와는 달리 UserDetailsService에서 사용자를 가져온다. 앱이 사용자 정보를 로드하는 데이터 소스와 통합 테스트를 하려면 이 방법을 사용한다.
 
Figure 20.6 Instead of creating a mock user for the test when building the SecurityContext used by the test, we take the user details from a UserDetailsService. This way, you can test authorization using real users taken from a data source. During the test, the flow of execution skips the shaded components.
To demonstrate this approach, let’s open project ssia-ch2-ex2 and implement the tests for the endpoint exposed at the /hello path. We use the UserDetailsService bean that the project already adds to the context. Note that, with this approach, we need to have a UserDetailsService bean in the context. To specify the user we authenticate from this UserDetailsService, we annotate the test method with @WithUserDetails. With the @WithUserDetails annotation, to find the user, you specify the username. The following listing presents the implementation of the test for the /hello endpoint using the @WithUserDetails annotation to define the authenticated user.
Listing 20.7 Defining the authenticated user with the @WithUserDetails annotation
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  @WithUserDetails("john")                              ❶
  public void helloAuthenticated() throws Exception {
    mvc.perform(get("/hello"))
        .andExpect(status().isOk());
  }
        
}
❶ Loads the user John using the UserDetailsService for running the test scenario
20.3 Using custom Authentication objects for testing
Generally, when using a mock user for a test, you don’t care which class the frame-work uses to create the Authentication instances in the SecurityContext. But say you have some logic in the controller that depends on the type of the object. Can you somehow instruct the framework to create the Authentication object for the test using a specific type? The answer is yes, and this is what we discuss in this section.
The logic behind this approach is simple. We define a factory class responsible for building the SecurityContext. This way, we have full control over how the SecurityContext for the test is built, including what’s inside it (figure 20.7). For example, we can choose to have a custom Authentication object.
 
Figure 20.7 To obtain full control of how the SecurityContext for the test is defined, we build a factory class that instructs the test on how to build the SecurityContext. This way, we gain greater flexibility, and we can choose details like the kind of object to use as an Authentication object. In the figure, I shaded the components skipped from the flow during the test.
Let’s open project ssia-ch2-ex5 and write a test in which we configure the mock SecurityContext and instruct the framework on how to create the Authentication object. An interesting aspect to remember about this example is that we use it to prove the implementation of a custom AuthenticationProvider. The custom AuthenticationProvider we implement in our case only authenticates a user named John. However, as in the other two previous approaches we discussed in sections 20.1 and 20.2, the current approach skips authentication. For this reason, you see at the end of the example that we can actually give any name to our mock user. We follow three steps to achieve this behavior (figure 20.8):
1.	Write an annotation to use over the test similarly to the way we use @WithMockUser or @WithUserDetails.
2.	Write a class that implements the WithSecurityContextFactory interface. This class implements the createSecurityContext() method that returns the mock SecurityContext the framework uses for the test.
3.	Link the custom annotation created in step 1 with the factory class created in step 2 via the @WithSecurityContext annotation.
 
Figure 20.8 To enable the test to use a custom SecurityContext, you need to follow the three steps illustrated in this figure.
STEP 1: DEFINING A CUSTOM ANNOTATION
In listing 20.8, you find the definition of the custom annotation we define for the test, named @WithCustomUser. As properties of the annotation, you can define whatever details you need to create the mock Authentication object. I added only the username here for my demonstration. Also, don’t forget to use the annotation @Retention (RetentionPolicy.RUNTIME) to set the retention policy to runtime. Spring needs to read this annotation using Java reflection at runtime. To allow Spring to read this annotation, you need to change its retention policy to RetentionPolicy.RUNTIME.
Listing 20.8 Defining the @WithCustomUser annotation
@Retention(RetentionPolicy.RUNTIME)
public @interface WithCustomUser {

  String username();
}
STEP 2: CREATING A FACTORY CLASS FOR THE MOCK SECURITYCONTEXT
The second step consists in implementing the code that builds the SecurityContext that the framework uses for the test’s execution. Here’s where we decide what kind of Authentication to use for the test. The following listing demonstrates the implementation of the factory class.
Listing 20.9 The implementation of a factory for the SecurityContext
public class CustomSecurityContextFactory                  ❶
  implements WithSecurityContextFactory<WithCustomUser> {

  @Override                                                ❷
  public SecurityContext createSecurityContext(
    WithCustomUser withCustomUser) {
      SecurityContext context =                            ❸
        SecurityContextHolder.createEmptyContext();

      var a = new UsernamePasswordAuthenticationToken(
        withCustomUser.username(), null, null);            ❹

      context.setAuthentication(a);                        ❺

      return context;
    }
}
❶ Implements the WithSecurityContextFactory annotation and specifies the custom annotation we use for the tests
❷ Implements createSecurityContext() to define how to create the SecurityContext for the test
❸ Builds an empty security context
❹ Creates an Authentication instance
❺ Adds the mock Authentication to the SecurityContext
STEP 3: LINKING THE CUSTOM ANNOTATION TO THE FACTORY CLASS
Using the @WithSecurityContext annotation, we now link the custom annotation we created in step 1 to the factory class for the SecurityContext we implemented in step 2. The following listing presents the change to our @WithCustomUser annotation to link it to the SecurityContext factory class.
Listing 20.10 Linking the custom annotation to the SecurityContext factory class
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomSecurityContextFactory.class)
public @interface WithCustomUser {

    String username();
}
With this setup complete, we can write a test to use the custom SecurityContext. The next listing defines the test.
Listing 20.11 Writing a test that uses the custom SecurityContext
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  @WithCustomUser(username = "mary")                   ❶
  public void helloAuthenticated() throws Exception {
    mvc.perform(get("/hello"))
         .andExpect(status().isOk());
  }
}
❶ Executes the test with a user having the username “mary”
Running the test, you observe a successful result. You might think, “Wait! In this example, we implemented a custom AuthenticationProvider that only authenticates a user named John. How could the test be successful with the username Mary?” As in the case of @WithMockUser and @WithUserDetails, with this method we skip the authentication logic. So you can use it only to test what’s related to authorization and onward.
20.4 Testing method security
In this section, we discuss testing method security. All the tests we wrote until now in this chapter refer to endpoints. But what if your application doesn’t have endpoints? In fact, if it’s not a web app, it doesn’t have endpoints at all! But you might have used Spring Security with global method security as we discussed in chapters 16 and 17. You still need to test your security configurations in such scenarios.
Fortunately, you do this by using the same approaches we discussed in the previous sections. You can still use @WithMockUser, @WithUserDetails, or a custom annotation to define your own SecurityContext. But instead of using MockMvc, you directly inject from the context the bean defining the method you need to test.
Let’s open project ssia-ch16-ex1 and implement the tests for the getName() method in the NameService class. We protected the getName() method using the @PreAuthorize annotation. In listing 20.12, you find the implementation of the test class with its three tests, and figure 20.9 represents graphically the three scenarios we test:
1.	Calling the method without an authenticated user, the method should throw AuthenticationException.
2.	Calling the method with an authenticated user that has an authority different than the expected one (write), the method should throw AccessDeniedException.
3.	Calling the method with an authenticated user that has the expected authority returns the expected result.
 
Figure 20.9 The tested scenarios. If the HTTP request is not authenticated, the expected result is an AuthenticationException. If the HTTP request is authenticated but the user doesn’t have the expected authority, the expected result is an AccessDeniedException. If the authenticated user has the expected authority, the call is successful.
Listing 20.12 Implementation of the three test scenarios for the getName() method
@SpringBootTest
class MainTests {

  @Autowired
  private NameService nameService;

  @Test
  void testNameServiceWithNoUser() {
    assertThrows(AuthenticationException.class,
            () -> nameService.getName());
  }

  @Test
  @WithMockUser(authorities = "read")
  void testNameServiceWithUserButWrongAuthority() {
    assertThrows(AccessDeniedException.class,
            () -> nameService.getName());
  }

  @Test
  @WithMockUser(authorities = "write")
  void testNameServiceWithUserButCorrectAuthority() {
    var result = nameService.getName();

    assertEquals("Fantastico", result);
  }
}
We don’t configure MockMvc anymore because we don’t need to call an endpoint. Instead, we directly inject the NameService instance to call the tested method. We use the @WithMockUser annotation as we discussed in section 20.1. Similarly, you could have used the @WithUserDetails as we discussed in section 20.2 or designed a custom way to build the SecurityContext as discussed in section 20.3.
20.5 Testing authentication
In this section, we discuss testing authentication. Previously, in this chapter, you learned how to define mock users and test authorization configurations. But what about authentication? Can we also test the authentication logic? You need to do this if, for example, you have custom logic implemented for your authentication, and you want to make sure the entire flow works. When testing authentication, the test implementation requests work like normal client requests, as presented in figure 20.10.
 
Figure 20.10 When testing authentication, the test acts as a client and goes through the full Spring Security flow discussed throughout the book. This way, you can also test, for example, your custom AuthenticationProvider objects.
For example, going back to project ssia-ch2-ex5, can we prove that the custom authentication provider we implemented works correctly and secure it with tests? In this project, we implemented a custom AuthenticationProvider, and we want to make sure that we secure this custom authentication logic as well with tests. Yes, we can test the authentication logic as well.
The logic we implement is straightforward. Only one set of credentials is accepted: the username "john" and the password "12345". We need to prove that, when using valid credentials, the call is successful, whereas when using some other credentials, the HTTP response status is 401 Unauthorized. Let’s again open project ssia-ch2-ex5 and implement a couple of tests to validate that authentication behaves correctly.
Listing 20.13 Testing authentication with httpBasic() RequestPostProcessor
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void helloAuthenticatingWithValidUser() throws Exception {
    mvc.perform(
       get("/hello")
         .with(httpBasic("john","12345")))      ❶
         .andExpect(status().isOk());
  }

  @Test
  public void helloAuthenticatingWithInvalidUser() throws Exception {
    mvc.perform(
       get("/hello")
         .with(httpBasic("mary","12345")))      ❷
         .andExpect(status().isUnauthorized());
  }
}
❶ Authenticates with the correct credentials
❷ Authenticates with the wrong credentials
Using the httpBasic() request postprocessor, we instruct the test to execute the authentication. This way, we validate the behavior of the endpoint when authenticating using either valid or invalid credentials. You can use the same approach to test the authentication with a form login. Let’s open project ssia-ch5-ex4, where we used form login for authentication, and write some tests to prove authentication works correctly. We test the app’s behavior in the following scenarios:
- When authenticating with a wrong set of credentials
- When authenticating with a valid set of credentials, but the user doesn’t have a valid authority according to the implementation we wrote in the AuthenticationSuccessHandler
- When authenticating with a valid set of credentials and a user that has a valid authority according to the implementation we wrote in the AuthenticationSuccessHandler
In listing 20.14, you find the implementation for the first scenario. If we authenticate using invalid credentials, the app doesn’t authenticate the user and adds the header “failed” to the HTTP response. We customized an app and added the “failed” header with an AuthenticationFailureHandler when discussing authentication back in chapter 5.
Listing 20.14 Testing form login failed authentication
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void loggingInWithWrongUser() throws Exception {
    mvc.perform(formLogin()                               ❶
          .user("joey").password("12345"))
          .andExpect(header().exists("failed"))
          .andExpect(unauthenticated());
  }
}
❶ Authenticates using form login with an invalid set of credentials
Back in chapter 5, we customized authentication logic using an Authentication-SuccessHandler. In our implementation, if the user has read authority, the app redirects them to the /home page. Otherwise, the app redirects the user to the /error page. The following listing presents the implementation of these two scenarios.
Listing 20.15 Testing app behavior when authenticating users
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  // Omitted code

  @Test
  public void loggingInWithWrongAuthority() throws Exception {
    mvc.perform(formLogin()
                .user("mary").password("12345")
            )
            .andExpect(redirectedUrl("/error"))     ❶
            .andExpect(status().isFound())
            .andExpect(authenticated());
    }

  @Test
  public void loggingInWithCorrectAuthority() throws Exception {
    mvc.perform(formLogin()
                 .user("bill").password("12345")
            )
            .andExpect(redirectedUrl("/home"))      ❷
            .andExpect(status().isFound())
            .andExpect(authenticated());
    }
}
❶ When authenticating with a user that doesn’t have read authority, the app redirects the user to path /error.
❷ When authenticating with a user that has read authority, the app redirects the user to path /home.
20.6 Testing CSRF configurations
In this section, we discuss testing the cross-site request forgery (CSRF) protection configuration for your application. When an app presents a CSRF vulnerability, an attacker can fool the user into taking actions they don’t want to take once they’re logged into the application. As we discussed in chapter 10, Spring Security uses CSRF tokens to mitigate these vulnerabilities. This way, for any mutating operation (POST, PUT, DELETE), the request needs to have a valid CSRF token in its headers. Of course, at some point, you need to test more than HTTP GET requests. Depending on how you implement your application, as we discussed in chapter 10, you might need to test CSRF protection. You need to make sure it works as expected and protects the endpoint that implements mutating actions.
Fortunately, Spring Security provides an easy approach to test CSRF protection using a RequestPostProcessor. Let’s open the project ssia-ch10-ex1 and test that CSRF protection is enabled for an endpoint /hello when called with HTTP POST in the following scenarios:
- If we don’t use a CSRF token, the HTTP response status is 403 Forbidden.
- If we send a CSRF token, the HTTP response status is 200 OK.
The following listing shows you the implementation of these two scenarios. Observe how we can send a CSRF token in the response simply by using the csrf() RequestPostProcessor.
Listing 20.16 Implementing the CSRF protection test scenarios
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void testHelloPOST() throws Exception {
    mvc.perform(post("/hello"))                           ❶
          .andExpect(status().isForbidden());
  }

  @Test
  public void testHelloPOSTWithCSRF() throws Exception {
    mvc.perform(post("/hello").with(csrf()))              ❷
          .andExpect(status().isOk());
  }
}
❶ When calling the endpoint without a CSRF token, the HTTP response status is 403 Forbidden.
❷ When calling the endpoint with a CSRF token, the HTTP response status is 200 OK.
20.7 Testing CORS configurations
In this section, we discuss testing cross-origin resource sharing (CORS) configurations. As you learned in chapter 10, if a browser loads a web app from one origin (say, example.com), the browser won’t allow the app to use an HTTP response that comes from a different origin (say, example.org). We use CORS policies to relax these restrictions. This way, we can configure our application to work with multiple origins. Of course, as for any other security configurations, you need to also test the CORS policies. In chapter 10, you learned that CORS is about specific headers on the response whose values define whether the HTTP response is accepted. Two of these headers related to CORS specifications are Access-Control-Allow-Origin and Access-Control-Allow-Methods. We used these headers in chapter 10 to configure multiple origins for our app.
All we need to do when writing tests for the CORS policies is to make sure that these headers (and maybe other CORS-related headers, depending on the complexity of your configurations) exist and have the correct values. For this validation, we can act precisely as the browser does when making a preflight request. We make a request using the HTTP OPTIONS method, requesting the value for the CORS headers. Let’s open project ssia-ch10-ex4 and write a test to validate the values for the CORS headers. The following listing shows the definition of the test.
Listing 20.17 Test implementation for CORS policies
@SpringBootTest
@AutoConfigureMockMvc
public class MainTests {

  @Autowired
  private MockMvc mvc;

  @Test
  public void testCORSForTestEndpoint() throws Exception {
    mvc.perform(options("/test")                                 ❶
            .header("Access-Control-Request-Method", "POST")
            .header("Origin", "http://www.example.com")
      )                                                          ❷
      .andExpect(header().exists("Access-Control-Allow-Origin"))
      .andExpect(header().string("Access-Control-Allow-Origin", "*"))
      .andExpect(header().exists("Access-Control-Allow-Methods"))
      .andExpect(header().string("Access-Control-Allow-Methods", "POST"))
      .andExpect(status().isOk());
  }

}
❶ Performs an HTTP OPTIONS request on the endpoint requesting the value for the CORS headers
❷ Validates the values for the headers according to the configuration we made in the app
20.8 Testing reactive Spring Security implementations
In this section, we discuss testing the integration of Spring Security with functionalities developed within a reactive app. You won’t be surprised to find out that Spring Security provides support for testing security configurations also for reactive apps. As in the case of non-reactive applications, security for reactive apps is a crucial aspect. So testing their security configurations is also essential. To show you how to implement tests for your security configurations, we go back to the examples we worked on in chapter 19. With Spring Security for reactive applications, you need to know two approaches for writing your tests:
- Using mock users with @WithMockUser annotations
- Using a WebTestClientConfigurer
Using the @WithMockUser annotation is straightforward because it works the same as for non-reactive apps, as we discussed in section 20.1. The definition of the test is different, however, because being a reactive app, we can’t use MockMvc anymore. But this change isn’t related to Spring Security. We can use something similar when testing reactive apps, a tool named WebTestClient. In the next listing, you find the implementation of a simple test making use of a mock user to verify the behavior of a reactive endpoint.
Listing 20.18 Using the @WithMockUser when testing reactive implementations
@SpringBootTest
@AutoConfigureWebTestClient            ❶
class MainTests {

  @Autowired                           ❷
  private WebTestClient client;

  @Test
  @WithMockUser                        ❸
  void testCallHelloWithValidUser() {
    client.get()                       ❹
            .uri("/hello")
            .exchange()
            .expectStatus().isOk();
  }
}
❶ Requests Spring Boot to autoconfigure the WebTestClient we use for the tests
❷ Injects the WebTestClient instance configured by Spring Boot from the Spring context
❸ Uses the @WithMockUser annotation to define a mock user for the test
❹ Makes the exchange and validates the result
As you observe, using the @WithMockUser annotation is pretty much the same as for non-reactive apps. The framework creates a SecurityContext with the mock user. The application skips the authentication process and uses the mock user from the test’s SecurityContext to validate the authorization rules.
The second approach you can use is a WebTestClientConfigurer. This approach is similar to using the RequestPostProcessor in the case of a non-reactive app. In the case of a reactive app, for the WebTestClient we use, we set a WebTestClientConfigurer, which helps mutate the test context. For example, we can define the mock user or send a CSRF token to test CSRF protection as we did for non-reactive apps in section 20.6. The following listing shows you how to use a WebTestClientConfigurer.
Listing 20.19 Using a WebTestClientConfigurer to define a mock user
@SpringBootTest
@AutoConfigureWebTestClient
class MainTests {

  @Autowired
  private WebTestClient client;

  // Omitted code

  @Test
  void testCallHelloWithValidUserWithMockUser() {
    client.mutateWith(mockUser())                  ❶
           .get()
           .uri("/hello")
           .exchange()
           .expectStatus().isOk();
    }
}
❶ Before executing the GET request, mutates the call to use a mock user
Assuming you’re testing CSRF protection on a POST call, you write something similar to this:
client.mutateWith(csrf())
         .post()
         .uri("/hello")
           .exchange()
           .expectStatus().isOk();
Mocking dependencies
Often our functionalities rely on external dependencies. Security-related implementations also rely sometimes on external dependencies. Some examples are databases we use to store user credentials, authentication keys, or tokens. External applications also represent dependencies as in the case of an OAuth 2 system where the resource server needs to call the token introspection endpoint of an authorization server to get details about an opaque token. When we deal with such cases, we usually create mocks for dependencies. For example, instead of finding the user from a database, you mock the repository and make its methods return what you consider appropriate for the test scenarios you implement.
In the projects we worked on in this book, you find some examples where we mocked dependencies. For this, you might be interested in taking a look at the following:
- In project ssia-ch6-ex1, we mocked the repository to enable testing the authentication flow. This way, we don’t need to rely on a real database to get the users, but we can still manage to test the authentication flow with all its components integrated.
- In project ssia-ch11-ex1-s2, we mocked the proxy to test the two authentication steps without needing to rely on the application implemented in project ssia-ch11-ex1-s1.
- In project ssia-ch14-ex1-rs, we used a tool named WireMockServer to mock the authorization server’s token introspection endpoints.
Different testing frameworks offer us different solutions for creating mocks or stubs to fake the dependencies on which our functionalities rely. Even if this is not directly related to Spring Security, I wanted to make you aware of the subject and its importance. Here are a few resources where you can continue studying this subject:
- Chapter 8 of JUnit in Action by Cătălin Tudose et al. (Manning, 2020):
https://livebook.manning.com/book/junit-in-action-third-edition/chapter-8
- Chapters 5 and 9 of Unit Testing Principles, Practices, and Patterns by Vladimir Khorikov (Manning, 2020):
https://livebook.manning.com/book/unit-testing/chapter-5
https://livebook.manning.com/book/unit-testing/chapter-9
Summary
- Writing tests is a best practice. You write tests to make sure your new implementations or fixes don’t break existing functionalities.
- You need to not only test your code, but also test integration with libraries and frameworks you use.
- Spring Security offers excellent support for implementing tests for your security configurations.
- You can test authorization directly by using mock users. You write separate tests for authorization without authentication because, generally, you need fewer authentication tests than authorization tests.
- It saves execution time to test authentication in separate tests, which are fewer in number, and then test the authorization configuration for your endpoints and methods.
- To test security configurations for endpoints in non-reactive apps, Spring Security offers excellent support for writing your tests with MockMvc.
- To test security configurations for endpoints in reactive apps, Spring Security offers excellent support for writing your tests with WebTestClient.
- You can write tests directly for methods for which you wrote security configurations using method security.
