
 

 
10 Applying CSRF protection and CORS
This chapter covers
- Implementing cross-site request forgery protection
- Customizing CSRF protection
- Applying cross-origin resource sharing configurations
You have learned about the filter chain and its purpose in the Spring Security architecture. We worked on several examples in chapter 9, where we customized the filter chain. But Spring Security also adds its own filters to the chain. In this chapter, we’ll discuss the filter that applies CSRF protection and the one related to CORS configurations. You’ll learn to customize these filters to make a perfect fit for your scenarios.
10.1 Applying cross-site request forgery (CSRF) protection in applications
You have probably observed that in most of the examples up to now, we only implemented our endpoints with HTTP GET. Moreover, when we needed to configure HTTP POST, we also had to add a supplementary instruction to the configuration to disable CSRF protection. The reason why you can’t directly call an endpoint with HTTP POST is because of CSRF protection, which is enabled by default in Spring Security.
In this section, we discuss CSRF protection and when to use it in your applications. CSRF is a widespread type of attack, and applications vulnerable to CSRF can force users to execute unwanted actions on a web application after authentication. You don’t want the applications you develop to be CSRF vulnerable and allow attackers to trick your users into making unwanted actions.
Because it’s essential to understand how to mitigate these vulnerabilities, we start by reviewing what CSRF is and how it works. We then discuss the CSRF token mechanism that Spring Security uses to mitigate CSRF vulnerabilities. We continue with obtaining a token and use it to call an endpoint with the HTTP POST method. We prove this with a small application using REST endpoints. Once you learn how Spring Security implements its CSRF token mechanism, we discuss how to use it in a real-world application scenario. Finally, you learn possible customizations of the CSRF token mechanism in Spring Security.
10.1.1 HOW CSRF PROTECTION WORKS IN SPRING SECURITY
In this section, we discuss how Spring Security implements CSRF protection. It is important to first understand the underlying mechanism of CSRF protection. I encounter many situations in which misunderstanding the way CSRF protection works leads developers to misuse it, either disabling it in scenarios where it should be enabled or the other way around. Like any other feature in a framework, you have to use it correctly to bring value to your applications.
As an example, consider this scenario (figure 10.1): you are at work, where you use a web tool to store and manage your files. With this tool, in a web interface you can add new files, add new versions for your records, and even delete them. You receive an email asking you to open a page for a specific reason. You open the page, but the page is blank or it redirects you to a known website. You go back to your work but observe that all your files are gone!
 
Figure 10.1 After the user logs into their account, they access a page containing forgery code. This code impersonates the user and can execute unwanted actions on behalf of the user.
What happened? You were logged into the application so you could manage your files. When you add, change, or delete a file, the web page you interact with calls some endpoints from the server to execute these operations. When you opened the foreign page by clicking the unknown link in the email, that page called the server and executed actions on your behalf (it deleted your files). It could do that because you logged in previously, so the server trusted that the actions came from you. You might think that someone couldn’t trick you so easily into clicking a link from a foreign email or message, but trust me, this happens to a lot of people. Most web app users aren’t aware of security risks. So it’s wiser if you, who know all the tricks to protect your users, build secure apps rather than rely on your apps’ users to protect themselves.
CSRF attacks assume that a user is logged into a web application. They’re tricked by the attacker into opening a page that contains scripts that execute actions in the same application the user was working on. Because the user has already logged in (as we’ve assumed from the beginning), the forgery code can now impersonate the user and do actions on their behalf.
How do we protect our users from such scenarios? What CSRF protection wants to ensure is that only the frontend of web applications can perform mutating operations (by convention, HTTP methods other than GET, HEAD, TRACE, or OPTIONS). Then, a foreign page, like the one in our example, can’t act on behalf of the user.
How can we achieve this? What you know for sure is that before being able to do any action that could change data, a user must send a request using HTTP GET to see the web page at least once. When this happens, the application generates a unique token. The application now accepts only requests for mutating operations (POST, PUT, DELETE, and so forth) that contain this unique value in the header. The application considers that knowing the value of the token is proof that it is the app itself making the mutating request and not another system. Any page containing mutating calls, like POST, PUT, DELETE, and so on, should receive through the response the CSRF token, and the page must use this token when making mutating calls.
The starting point of CSRF protection is a filter in the filter chain called CsrfFilter. The CsrfFilter intercepts requests and allows all those that use these HTTP methods: GET, HEAD, TRACE, and OPTIONS. For all other requests, the filter expects to receive a header containing a token. If this header does not exist or contains an incorrect token value, the application rejects the request and sets the status of the response to HTTP 403 Forbidden.
What is this token, and where does it come from? These tokens are nothing more than string values. You have to add the token in the header of the request when you use any method other than GET, HEAD, TRACE, or OPTIONS. If you don’t add the header containing the token, the application doesn’t accept the request, as presented in figure 10.2.
 
Figure 10.2 To make a POST request, the client needs to add a header containing the CSRF token. The application generates a CSRF token when the page is loaded (via a GET request), and the token is added to all requests that can be made from the loaded page. This way, only the loaded page can make mutable requests.
The CsrfFilter (figure 10.3) uses a component named CsrfTokenRepository to manage the CSRF token values that generate new tokens, store tokens, and eventually invalidate these. By default, the CsrfTokenRepository stores the token on the HTTP session and generates the tokens as random universally unique identifiers (UUIDs). In most cases, this is enough, but as you’ll learn in section 10.1.3, you can use your own implementation of CsrfTokenRepository if the default one doesn’t apply to the requirements you need to implement.
 
Figure 10.3 The CsrfFilter is one of the filters in the filter chain. It receives the request and eventually forwards it to the next filter in the chain. To manage CSRF tokens, CsrfFilter uses a CsrfTokenRepository.
In this section, I explained how CSRF protection works in Spring Security with plenty of text and figures. But I want to enforce your understanding with a small code example as well. You’ll find this code as part of the project named ssia-ch10-ex1. Let’s create an application that exposes two endpoints. We can call one of these with HTTP GET and the other with HTTP POST. As you know by now, you are not able to call endpoints with POST directly without disabling CSRF protection. In this example, you learn how to call the POST endpoint without disabling CSRF protection. You need to obtain the CSRF token so that you can use it in the header of the call, which you do with HTTP POST.
As you learn with this example, the CsrfFilter adds the generated CSRF token to the attribute of the HTTP request named _csrf (figure 10.4). If we know this, we know that after the CsrfFilter, we can find this attribute and take the value of the token from it. For this small application, we choose to add a custom filter after the CsrfFilter, as you learned in chapter 9. You use this custom filter to print in the console of the application the CSRF token that the app generates when we call the endpoint using HTTP GET. We can then copy the value of the token from the console and use it to make the mutating call with HTTP POST. In the following listing, you can find the definition of the controller class with the two endpoints that we use for a test.
 
Figure 10.4 Adding the CsrfTokenLogger (shaded) after the CsrfFilter. This way, the CsrfTokenLogger can obtain the value of the token from the _csrf attribute of the request where the CsrfFilter stores it. The CsrfTokenLogger prints the CSRF token in the application console, where we can access it and use it to call an endpoint with the HTTP POST method.
Listing 10.1 The controller class with two endpoints
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String getHello() {
    return "Get Hello!";
  }

  @PostMapping("/hello")
  public String postHello() {
    return "Post Hello!";
  }
}
Listing 10.2 defines the custom filter we use to print the value of the CSRF token in the console. I named the custom filter CsrfTokenLogger. When called, the filter obtains the value of the CSRF token from the _csrf request attribute and prints it in the console. The name of the request attribute, _csrf, is where the CsrfFilter sets the value of the generated CSRF token as an instance of the class CsrfToken. This instance of CsrfToken contains the string value of the CSRF token. You can obtain it by calling the getToken() method.
Listing 10.2 The definition of the custom filter class
public class CsrfTokenLogger implements Filter {

  private Logger logger =
          Logger.getLogger(CsrfTokenLogger.class.getName());

  @Override
  public void doFilter(
    ServletRequest request, 
    ServletResponse response, 
    FilterChain filterChain) 
      throws IOException, ServletException {

      Object o = request.getAttribute("_csrf");      ❶
      CsrfToken token = (CsrfToken) o;

      logger.info("CSRF token " + token.getToken());

      filterChain.doFilter(request, response);
  }
}
❶ Takes the value of the token from the _csrf request attribute and prints it in the console
In the configuration class, we add the custom filter. The next listing presents the configuration class. Observe that I don’t disable CSRF protection in the listing.
Listing 10.3 Adding the custom filter in the configuration class
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) 
    throws Exception {

    http.addFilterAfter(
            new CsrfTokenLogger(), CsrfFilter.class)
        .authorizeRequests()
            .anyRequest().permitAll();
  }
}
We can now test the endpoints. We begin by calling the endpoint with HTTP GET. Because the default implementation of the CsrfTokenRepository interface uses the HTTP session to store the token value on the server side, we also need to remember the session ID. For this reason, I add the -v flag to the call so that I can see more details from the response, including the session ID. Calling the endpoint
curl -v http://localhost:8080/hello
returns this (truncated) response:
...
< Set-Cookie: JSESSIONID=21ADA55E10D70BA81C338FFBB06B0206;
...
Get Hello!
Following the request in the application console, you can find a log line that contains the CSRF token:
INFO 21412 --- [nio-8080-exec-1] c.l.ssia.filters.CsrfTokenLogger : CSRF token c5f0b3fa-2cae-4ca8-b1e6-6d09894603df
NOTE You might ask yourself, how do clients get the CSRF token? They can neither guess it nor read it in the server logs. I designed this example such that it’s easier for you to understand how CSRF protection implementation works. As you’ll find in section 10.1.2, the backend application has the responsibility to add the value of the CSRF token in the HTTP response to be used by the client.
If you call the endpoint using the HTTP POST method without providing the CSRF token, the response status is 403 Forbidden, as this command line shows:
curl -XPOST http://localhost:8080/hello
The response body is
{
    "status":403,
    "error":"Forbidden",
    "message":"Forbidden",
    "path":"/hello"
}
But if you provide the correct value for the CSRF token, the call is successful. You also need to specify the session ID (JSESSIONID) because the default implementation of CsrfTokenRepository stores the value of the CSRF token on the session:
curl -X POST   http://localhost:8080/hello 
-H 'Cookie: JSESSIONID=21ADA55E10D70BA81C338FFBB06B0206'   
-H 'X-CSRF-TOKEN: 1127bfda-57b1-43f0-bce5-bacd7d94694e'
The response body is
Post Hello!
10.1.2 USING CSRF PROTECTION IN PRACTICAL SCENARIOS
In this section, we discuss applying CSRF protection in practical situations. Now that you know how CSRF protection works in Spring Security, you need to know where you should use it in the real world. Which kinds of applications need to use CSRF protection?
You use CSRF protection for web apps running in a browser, where you should expect that mutating operations can be done by the browser that loads the displayed content of the app. The most basic example I can provide here is a simple web application developed on the standard Spring MVC flow. We already made such an application when discussing form login in chapter 5, and that web app actually used CSRF protection. Did you notice that the login operation in that application used HTTP POST? Then why didn’t we need to do anything explicitly about CSRF in that case? The reason why we didn’t observe this was because we didn’t develop any mutating operation within it ourselves.
For the default login, Spring Security correctly applies CSRF protection for us. The framework takes care of adding the CSRF token to the login request. Let’s now develop a similar application to look closer at how CSRF protection works. As figure 10.5 shows, in this section we
- Build an example of a web application with the login form
- Look at how the default implementation of the login uses CSRF tokens
- Implement an HTTP POST call from the main page
 
Figure 10.5 The plan. In this section, we start by building and analyzing a simple app to understand how Spring Security applies CSRF protection, and then we write our own POST call.
In this example application, you’ll notice that the HTTP POST call won’t work until we correctly use the CSRF tokens, and you’ll learn how to apply the CSRF tokens in a form on such a web page. To implement this application, we start by creating a new Spring Boot project. You can find this example in the project ssia-ch10-ex2. The next code snippet presents the needed dependencies:
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
Then we need, of course, to configure the form login and at least one user. The following listing presents the configuration class, which defines the UserDetailsService, adds a user, and configures the formLogin method.
Listing 10.4 The definition of the configuration class
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  @Bean                                                ❶
  public UserDetailsService uds() {
    var uds = new InMemoryUserDetailsManager();

    var u1 = User.withUsername("mary")
                 .password("12345")
                 .authorities("READ")
                 .build();

    uds.createUser(u1);
    return uds;
  }

  @Bean                                               ❷
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Override                                           ❸
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
          .anyRequest().authenticated();

    http.formLogin()
        .defaultSuccessUrl("/main", true);
  }
}
❶ Adds a UserDetailsService bean managing one user to test the application
❷ Adds a PasswordEncoder
❸ Overrides configure() to set the form login authentication method and specifies that only authenticated users can access any of the endpoints
We add a controller class for the main page in a package named controllers and in a main.html file in the resources/templates folder of the Maven project. The main.html file can remain empty for the moment because on first execution of the application, we only focus on how the login page uses the CSRF tokens. The following listing presents the MainController class, which serves the main page.
Listing 10.5 The definition of the MainController class
@Controller
public class MainController {

  @GetMapping("/main")
  public String main() {
    return "main.html";
  }
}
After running the application, you can access the default login page. If you inspect the form using the inspect element function of your browser, you can observe that the default implementation of the login form sends the CSRF token. This is why your login works with CSRF protection enabled even if it uses an HTTP POST request! Figure 10.6 shows how the login form sends the CSRF token through hidden input.
 
Figure 10.6 The default form login uses a hidden input to send the CSRF token in the request. This is why the login request that uses an HTTP POST method works with CSRF protection enabled.
But what about developing our own endpoints that use POST, PUT, or DELETE as HTTP methods? For these, we have to take care of sending the value of the CSRF token if CSRF protection is enabled. To test this, let’s add an endpoint using HTTP POST to our application. We call this endpoint from the main page, and we create a second controller for this, called ProductController. Within this controller, we define an endpoint, /product/add, that uses HTTP POST. Further, we use a form on the main page to call this endpoint. The next listing defines the ProductController class.
Listing 10.6 The definition of the ProductController class
@Controller
@RequestMapping("/product")
public class ProductController {

  private Logger logger =
          Logger.getLogger(ProductController.class.getName());

  @PostMapping("/add")
  public String add(@RequestParam String name) {
    logger.info("Adding product " + name);
    return "main.html";
  }
}
The endpoint receives a request parameter and prints it in the application console. The following listing shows the definition of the form defined in the main.html file.
Listing 10.7 The definition of the form in the main.html page
<form action="/product/add" method="post">
   <span>Name:</span>
   <span><input type="text" name="name" /></span>
   <span><button type="submit">Add</button></span>
</form>
Now you can rerun the application and test the form. What you’ll observe is that when submitting the request, a default error page is displayed, which confirms an HTTP 403 Forbidden status on the response from the server (figure 10.7). The reason for the HTTP 403 Forbidden status is the absence of the CSRF token.
 
Figure 10.7 Without sending the CSRF token, the server won’t accept the request done with the HTTP POST method. The application redirects the user to a default error page, which confirms that the status on the response is HTTP 403 Forbidden.
To solve this problem and make the server allow the request, we need to add the CSRF token in the request done through the form. An easy way to do this is to use a hidden input component, as you saw in the default form login. You can implement this as presented in the following listing.
Listing 10.8 Adding the CSRF token to the request done through the form
<form action="/product/add" method="post">
   <span>Name:</span>
   <span><input type="text" name="name" /></span>
   <span><button type="submit">Add</button></span>

   <input type="hidden"                       ❶
          th:name="${_csrf.parameterName}"    ❷
          th:value="${_csrf.token}" />        ❷
</form>
❶ Uses hidden input to add the request to the CSRF token
❷ The “th” prefix enables Thymeleaf to print the token value.
NOTE In the example, we use Thymeleaf because it provides a straightforward way to obtain the request attribute value in the view. In our case, we need to print the CSRF token. Remember that the CsrfFilter adds the value of the token in the _csrf attribute of the request. It’s not mandatory to do this with Thymeleaf. You can use any alternative of your choice to print the token value to the response.
After rerunning the application, you can test the form again. This time the server accepts the request, and the application prints the log line in the console, proving that the execution succeeds. Also, if you inspect the form, you can find the hidden input with the value of the CSRF token (figure 10.8).
 
Figure 10.8 The form defined on the main page now sends the value for the CSRF token in the request. This way, the server allows the request and executes the controller action. In the source code for the page, you can now find the hidden input used by the form to send the CSRF token in the request.
After submitting the form, you should find in the application console a line similar to this one:
INFO 20892 --- [nio-8080-exec-7] c.l.s.controllers.ProductController    : Adding product Chocolate
Of course, for any action or asynchronous JavaScript request your page uses to call a mutable action, you need to send a valid CSRF token. This is the most common way used by an application to make sure the request doesn’t come from a third party. A third-party request could try to impersonate the user to execute actions on their behalf.
CSRF tokens work well in an architecture where the same server is responsible for both the frontend and the backend, mainly for its simplicity. But CSRF tokens don’t work well when the client is independent of the backend solution it consumes. This scenario happens when you have a mobile application as a client or a web frontend developed independently. A web client developed with a framework like Angular, ReactJS, or Vue.js is ubiquitous in web application architectures, and this is why you need to know how to implement the security approach for these cases as well. We’ll discuss these kinds of designs in chapters 11 to 15:
- In chapter 11, we’ll work on a hands-on application where we’ll solve the requirement of implementing a web application with separate web servers independently supporting the frontend and backend solutions. For that example, we’ll analyze the applicability of CSRF protection with tokens again.
- In chapters 12 through 15, you’ll learn to implement the OAuth 2 specification, which has excellent advantages in decoupling the component. This makes the authentication from the resources for which the application authorizes the client.
NOTE It might look like a trivial mistake, but in my experience, I see it too many times in applications--never use HTTP GET with mutating operations! Do not implement behavior that changes data and allows it to be called with an HTTP GET endpoint. Remember that calls to HTTP GET endpoints don’t require a CSRF token.
10.1.3 CUSTOMIZING CSRF PROTECTION
In this section, you learn how to customize the CSRF protection solution that Spring Security offers. Because applications have various requirements, any implementation provided by a framework needs to be flexible enough to be easily adapted to different scenarios. The CSRF protection mechanism in Spring Security is no exception. In this section, the examples let you apply the most frequently encountered needs in customization of the CSRF protection mechanism. These are
- Configuring paths for which CSRF applies
- Managing CSRF tokens
We use CSRF protection only when the page that consumes resources produced by the server is itself generated by the same server. It can be a web application where the consumed endpoints are exposed by a different origin, as we discussed in section 10.2, or a mobile application. In the case of mobile applications, you can use the OAuth 2 flow, which we’ll discuss in chapters 12 through 15.
By default, CSRF protection applies to any path for endpoints called with HTTP methods other than GET, HEAD, TRACE, or OPTIONS. You already know from chapter 7 how to completely disable CSRF protection. But what if you want to disable it only for some of your application paths? You can do this configuration quickly with a Customizer object, similar to the way we customized HTTP Basic for form-login methods in chapter 3. Let’s try this with an example.
In our example, we create a new project and add only the web and security dependencies as presented in the next code snippet. You can find this example in the project ssia-ch10-ex3. Here are the dependencies:
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
In this application, we add two endpoints called with HTTP POST, but we want to exclude one of these from using CSRF protection (figure 10.9). Listing 10.9 defines the controller class for this, which I name HelloController.
 
Figure 10.9 The application requires a CSRF token for the /hello endpoint called with HTTP POST but allows HTTP POST requests to the /ciao endpoint without a CSRF token.
Listing 10.9 The definition of the HelloController class
@RestController
public class HelloController {

  @PostMapping("/hello")       ❶
  public String postHello() {
    return "Post Hello!";
  }

  @PostMapping("/ciao")        ❷
  public String postCiao() {
    return "Post Ciao";
  }
}
❶ The /hello path remains under CSRF protection. You can’t call the endpoint without a valid CSRF token.
❷ The /ciao path can be called without a CSRF token.
To make customizations on CSRF protection, you can use the csrf() method of the HttpSecurity object in the configuration() method with a Customizer object. The next listing presents this approach.
Listing 10.10 A Customizer object for the configuration of CSRF protection
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) 
    throws Exception {

    http.csrf(c -> {                        ❶
        c.ignoringAntMatchers("/ciao");
    });

    http.authorizeRequests()
         .anyRequest().permitAll();
    }
}
❶ The parameter of the lambda expression is a CsrfConfigurer. By calling its methods, you can configure CSRF protection in various ways.
Calling the ignoringAntMatchers(String paths) method, you can specify the Ant expressions representing the paths that you want to exclude from the CSRF protection mechanism. A more general approach is to use a RequestMatcher. Using this allows you to apply the exclusion rules with regular MVC expressions as well as with regexes (regular expressions). When using the ignoringRequestMatchers() method of the CsrfCustomizer object, you can provide any RequestMatcher as a parameter. The next code snippet shows how to use the ignoringRequestMatchers() method with an MvcRequestMatcher instead of using ignoringAntMatchers():
HandlerMappingIntrospector i = new HandlerMappingIntrospector();
MvcRequestMatcher r = new MvcRequestMatcher(i, "/ciao");
c.ignoringRequestMatchers(r);
Or, you can similarly use a regex matcher as in the next code snippet:
String pattern = ".*[0-9].*";
String httpMethod = HttpMethod.POST.name();
RegexRequestMatcher r = new RegexRequestMatcher(pattern, httpMethod);
c.ignoringRequestMatchers(r);
Another need often found in the requirements of the application is customizing the management of CSRF tokens. As you learned, by default, the application stores CSRF tokens in the HTTP session on the server side. This simple approach is suitable for small applications, but it’s not great for applications that serve a large number of requests and that require horizontal scaling. The HTTP session is stateful and reduces the scalability of the application.
Let’s suppose you want to change the way the application manages tokens and store them somewhere in a database rather than in the HTTP session. Spring Security offers two contracts that you need to implement to do this:
- CsrfToken--Describes the CSRF token itself
- CsrfTokenRepository--Describes the object that creates, stores, and loads CSRF tokens
The CsrfToken object has three main characteristics that you need to specify when implementing the contract (listing 10.11 defines the CsrfToken contract):
- The name of the header in the request that contains the value of the CSRF token (default named X-CSRF-TOKEN)
- The name of the attribute of the request that stores the value of the token (default named _csrf)
- The value of the token
Listing 10.11 The definition of the CsrfToken interface
public interface CsrfToken extends Serializable {

  String getHeaderName();
  String getParameterName();
  String getToken();
}
Generally, you only need the instance of the CsrfToken type to store the three details in the attributes of the instance. For this functionality, Spring Security offers an implementation called DefaultCsrfToken that we also use in our example. DefaultCsrfToken implements the CsrfToken contract and creates immutable instances containing the required values: the name of the request attribute and header, and the token itself.
CsrfTokenRepository is responsible for managing CSRF tokens in Spring Security. The interface CsrfTokenRepository is the contract that represents the component that manages CSRF tokens. To change the way the application manages the tokens, you need to implement the CsrfTokenRepository interface, which allows you to plug your custom implementation into the framework. Let’s change the current application we use in this section to add a new implementation for CsrfTokenRepository, which stores the tokens in a database. Figure 10.10 presents the components we implement for this example and the link between them.
 
Figure 10.10 The CsrfToken uses a custom implementation of CsrfTokenRepository. This custom implementation uses a JpaRepository to manage CSRF tokens in a database.
In our example, we use a table in a database to store CSRF tokens. We assume the client has an ID to identify themselves uniquely. The application needs this identifier to obtain the CSRF token and validate it. Generally, this unique ID would be obtained during login and should be different each time the user logs in. This strategy of managing tokens is similar to storing them in memory. In this case, you use a session ID. So the new identifier for this example merely replaces the session ID.
An alternative to this approach would be to use CSRF tokens with a defined lifetime. With such an approach, tokens expire after a time you define. You can store tokens in the database without linking them to a specific user ID. You only need to check if a token provided via an HTTP request exists and is not expired to decide whether you allow that request.
EXERCISE Once you finish with this example where we use an identifier to which we assign the CSRF token, implement the second approach where you use CSRF tokens that expire.
To make our example shorter, we only focus on the implementation of the Csrf-TokenRepository, and we need to consider that the client already has a generated identifier. To work with the database, we need to add a couple more dependencies to the pom.xml file:
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
  <version>8.0.18</version>
</dependency>
In the application.properties file, we need to add the properties for the database connection:
spring.datasource.url=jdbc:mysql://localhost/spring
spring.datasource.username=root
spring.datasource.password=
spring.datasource.initialization-mode=always
To allow the application to create the needed table in the database at the start, you can add the schema.xml file in the resources folder of the project. This file should contain the query for creating the table, as presented by this code snippet:
CREATE TABLE IF NOT EXISTS `spring`.`token` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `identifier` VARCHAR(45) NULL,
    `token` TEXT NULL,
PRIMARY KEY (`id`));
We use Spring Data with a JPA implementation to connect to the database, so we need to define the entity class and the JpaRepository class. In a package named entities, we define the JPA entity as presented in the following listing.
Listing 10.12 The definition of the JPA entity class
@Entity
public class Token {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String identifier;     ❶
  private String token;          ❷

  // Omitted code

}
❶ The identifier of the client
❷ The CSRF token generated by the application for the client
The JpaTokenRepository, which is our JpaRepository contract, can be defined as shown in the following listing. The only method you need is findTokenByIdentifier(), which gets the CSRF token from the database for a specific client.
Listing 10.13 The definition of the JpaTokenRepository interface
public interface JpaTokenRepository 
  extends JpaRepository<Token, Integer> {

  Optional<Token> findTokenByIdentifier(String identifier);
}
With access to the implemented database, we can now start to write the CsrfToken-Repository implementation, which I call CustomCsrfTokenRepository. The next listing defines this class, which overrides the three methods of CsrfTokenRepository.
Listing 10.14 The implementation of the CsrfTokenRepository contract
public class CustomCsrfTokenRepository implements CsrfTokenRepository {

  @Autowired
  private JpaTokenRepository jpaTokenRepository;

  @Override
  public CsrfToken generateToken(
         HttpServletRequest httpServletRequest) {
    // ...
  }
  @Override
  public void saveToken(
       CsrfToken csrfToken, 
       HttpServletRequest httpServletRequest, 
       HttpServletResponse httpServletResponse) {
    // ...
  }

  @Override
  public CsrfToken loadToken(
         HttpServletRequest httpServletRequest) {
    // ...
  }
}
CustomCsrfTokenRepository injects an instance of JpaTokenRepository from the Spring context to gain access to the database. CustomCsrfTokenRepository uses this instance to retrieve or to save the CSRF tokens in the database. The CSRF protection mechanism calls the generateToken() method when the application needs to generate a new token. In listing 10.15, you find the implementation of this method for our exercise. We use the UUID class to generate a new random UUID value, and we keep the same names for the request header and attribute, X-CSRF-TOKEN and _csrf, as in the default implementation offered by Spring Security.
Listing 10.15 The implementation of the generateToken() method
@Override
public CsrfToken generateToken(HttpServletRequest httpServletRequest) {
  String uuid = UUID.randomUUID().toString();
  return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", uuid);
}
The saveToken() method saves a generated token for a specific client. In the case of the default CSRF protection implementation, the application uses the HTTP session to identify the CSRF token. In our case, we assume that the client has a unique identifier. The client sends the value of its unique ID in the request with the header named X-IDENTIFIER. In the method logic, we check whether the value exists in the database. If it exists, we update the database with the new value of the token. If not, we create a new record for this ID with the new value of the CSRF token. The following listing presents the implementation of the saveToken() method.
Listing 10.16 The implementation of the saveToken() method
@Override
public void saveToken(
   CsrfToken csrfToken, 
   HttpServletRequest httpServletRequest, 
   HttpServletResponse httpServletResponse) {
    String identifier = 
        httpServletRequest.getHeader("X-IDENTIFIER");

    Optional<Token> existingToken =                       ❶
       jpaTokenRepository.findTokenByIdentifier(identifier);

    if (existingToken.isPresent()) {                      ❷
       Token token = existingToken.get();
       token.setToken(csrfToken.getToken());
    } else {                                              ❸
       Token token = new Token();
       token.setToken(csrfToken.getToken());
       token.setIdentifier(identifier);
       jpaTokenRepository.save(token);
    }
}
❶ Obtains the token from the database by client ID
❷ If the ID exists, updates the value of the token with a newly generated value
❸ If the ID doesn’t exist, creates a new record for the ID with a generated value for the CSRF token
The loadToken() method implementation loads the token details, if these exist, or returns null, otherwise. The following listing shows this implementation.
Listing 10.17 The implementation of the loadToken() method
@Override
public CsrfToken loadToken(
  HttpServletRequest httpServletRequest) {

  String identifier = httpServletRequest.getHeader("X-IDENTIFIER");

  Optional<Token> existingToken = 
              jpaTokenRepository
                .findTokenByIdentifier(identifier);

  if (existingToken.isPresent()) {
    Token token = existingToken.get();
    return new DefaultCsrfToken(
                  "X-CSRF-TOKEN", 
                  "_csrf", 
                  token.getToken());
  }
        
  return null;
}
We use a custom implementation of the CsrfTokenRepository to declare a bean in the configuration class. We then plug the bean into the CSRF protection mechanism with the csrfTokenRepository() method of CsrfConfigurer. The next listing defines this configuration class.
Listing 10.18 The configuration class for the custom CsrfTokenRepository
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Bean                                               ❶
  public CsrfTokenRepository customTokenRepository() {
    return new CustomCsrfTokenRepository();
  }

  @Override
  protected void configure(HttpSecurity http) 
    throws Exception {

    http.csrf(c -> {                                  ❷
        c.csrfTokenRepository(customTokenRepository());
        c.ignoringAntMatchers("/ciao");
    });

    http.authorizeRequests()
         .anyRequest().permitAll();
  }
}
❶ Defines CsrfTokenRepository as a bean in the context
❷ Uses the Customizer<CsrfConfigurer<HttpSecurity>> object to plug the new CsrfTokenRepository implementation into the CSRF protection mechanism
In the definition of the controller class presented in listing 10.9, we also add an endpoint that uses the HTTP GET method. We need this method to obtain the CSRF token when testing our implementation:
@GetMapping("/hello")
public String getHello() {
  return "Get Hello!";
}
You can now start the application and test the new implementation for managing the token. We call the endpoint using HTTP GET to obtain a value for the CSRF token. When making the call, we have to use the client’s ID within the X-IDENTIFIER header, as assumed from the requirement. A new value of the CSRF token is generated and stored in the database. Here’s the call:
curl -H "X-IDENTIFIER:12345" http://localhost:8080/hello
Get Hello!
If you search the token table in the database, you find that the application added a new record for the client with identifier 12345. In my case, the generated value for the CSRF token, which I can see in the database, is 2bc652f5-258b-4a26-b456-928e9bad71f8. We use this value to call the /hello endpoint with the HTTP POST method, as the next code snippet presents. Of course, we also have to provide the client ID that’s used by the application to retrieve the token from the database to compare with the one we provide in the request. Figure 10.11 describes the flow.
curl -XPOST -H "X-IDENTIFIER:12345" -H "X-CSRF-TOKEN:2bc652f5-258b-4a26-b456-928e9bad71f8" http://localhost:8080/hello
Post Hello!
 
Figure 10.11 First, the GET request generates the CSRF token and stores its value in the database. Any following POST request must send this value. Then, the CsrfFilter checks if the value in the request corresponds with the one in the database. Based on this, the request is accepted or rejected.
If we try to call the /hello endpoint with POST without providing the needed headers, we get a response back with the HTTP status 403 Forbidden. To confirm this, call the endpoint with
curl -XPOST http://localhost:8080/hello
The response body is
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/hello"
}
10.2 Using cross-origin resource sharing
In this section, we discuss cross-origin resource sharing (CORS) and how to apply it with Spring Security. First, what is CORS and why should you care? The necessity for CORS came from web applications. By default, browsers don’t allow requests made for any domain other than the one from which the site is loaded. For example, if you access the site from example.com, the browser won’t let the site make requests to api.example.com. Figure 10.12 shows this concept.
 
Figure 10.12 Cross-origin resource sharing (CORS). When accessed from example.com, the website cannot make requests to api.example.com because they would be cross-domain requests.
We can briefly say that a browser uses the CORS mechanism to relax this strict policy and allow requests made between different origins in some conditions. You need to know this because it’s likely you will have to apply it to your applications, especially nowadays where the frontend and backend are separate applications. It is common that a frontend application is developed using a framework like Angular, ReactJS, or Vue and hosted at a domain like example.com, but it calls endpoints on the backend hosted at another domain like api.example.com. For this section, we develop some examples from which you can learn how to apply CORS policies for your web applications. We also describe some details that you need to know such that you avoid leaving security breaches in your applications.
10.2.1 HOW DOES CORS WORK?
In this section, we discuss how CORS applies to web applications. If you are the owner of example.com, for example, and for some reason the developers from example.org decide to call your REST endpoints from their website, they won’t be able to. The same situation can happen if a domain loads your application using an iframe, for example (see figure 10.13).
 
Figure 10.13 Even if the example.org page is loaded in an iframe from the example.com domain, the calls from the content loaded in example.org won’t load. Even if the application makes a request, the browser won’t accept the response.
NOTE An iframe is an HTML element that you use to embed content generated by a web page into another web page (for example, to integrate the content from example.org inside a page from example.com).
Any situation in which an application makes calls between two different domains is prohibited. But, of course, you can find cases in which you need to make such calls. In these situations, CORS allows you to specify from which domain your application allows requests and what details can be shared. The CORS mechanism works based on HTTP headers (figure 10.14). The most important are
- Access-Control-Allow-Origin--Specifies the foreign domains (origins) that can access resources on your domain.
- Access-Control-Allow-Methods--Lets us refer only to some HTTP methods in situations in which we want to allow access to a different domain, but only to specific HTTP methods. You use this if you’re going to enable example.com to call some endpoint, but only with HTTP GET, for example.
- Access-Control-Allow-Headers--Adds limitations to which headers you can use in a specific request.
 
Figure 10.14 Enabling cross-origin requests. The example.org server adds the Access-Control-Allow-Origin header to specify the origins of the request for which the browser should accept the response. If the domain from where the call was made is enumerated in the origins, the browser accepts the response.
With Spring Security, by default, none of these headers are added to the response. So let’s start at the beginning: what happens when you make a cross-origin call if you don’t configure CORS in your application. When the application makes the request, it expects that the response has an Access-Control-Allow-Origin header containing the origins accepted by the server. If this doesn’t happen, as in the case of default Spring Security behavior, the browser won’t accept the response. Let’s demonstrate this with a small web application. We create a new project using the dependencies presented by the next code snippet. You can find this example in the project ssia-ch10-ex4.
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
  <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
We define a controller class having an action for the main page and a REST endpoint. Because the class is a normal Spring MVC @Controller class, we also have to add the @ResponseBody annotation explicitly to the endpoint. The following listing defines the controller.
Listing 10.19 The definition of the controller class
@Controller
public class MainController {

  private Logger logger =              ❶
          Logger.getLogger(MainController.class.getName());

  @GetMapping("/")                     ❷
  public String main() {
    return "main.html";
  }

  @PostMapping("/test")
  @ResponseBody
  public String test() {               ❸
    logger.info("Test method called");
    return "HELLO";
  }
}
❶ Uses a logger to observe when the test() method is called
❷ Defines a main.html page that makes the request to the /test endpoint
❸ Defines an endpoint that we call from a different origin to prove how CORS works
Further, we need to define the configuration class where we disable CSRF protection to make the example simpler and allow you to focus only on the CORS mechanism. Also, we allow unauthenticated access to all endpoints. The next listing defines this configuration class.
Listing 10.20 The definition of the configuration class
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable();

    http.authorizeRequests()
          .anyRequest().permitAll();
  }
}
Of course, we also need to define the main.html file in the resources/templates folder of the project. The main.html file contains the JavaScript code that calls the /test endpoint. To simulate the cross-origin call, we can access the page in a browser using the domain localhost. From the JavaScript code, we make the call using the IP address 127.0.0.1. Even if localhost and 127.0.0.1 refer to the same host, the browser sees these as different strings and considers these different domains. The next listing defines the main.html page.
Listing 10.21 The main.html page
<!DOCTYPE HTML>
<html lang="en">
  <head>
    <script>
      const http = new XMLHttpRequest();
      const url='http://127.0.0.1:8080/test';     ❶
      http.open("POST", url);
      http.send();

      http.onreadystatechange = (e) => {
        document                                  ❷
          .getElementById("output")
          .innerHTML = http.responseText;
      }
    </script>
  </head>
  <body>
    <div id="output"></div>
  </body>
</html>
❶ Calls the endpoint using 127.0.0.1 as host to simulate the cross-origin call
❷ Sets the response body to the output div in the page body
Starting the application and opening the page in a browser with localhost:8080, we can observe that the page doesn’t display anything. We expected to see HELLO on the page because this is what the /test endpoint returns. When we check the browser console, what we see is an error printed by the JavaScript call. The error looks like this:
Access to XMLHttpRequest at 'http://127.0.0.1:8080/test' from origin 'http://localhost:8080' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
The error message tells us that the response wasn’t accepted because the Access-Control-Allow-Origin HTTP header doesn’t exist. This behavior happens because we didn’t configure anything regarding CORS in our Spring Boot application, and by default, it doesn’t set any header related to CORS. So the browser’s behavior of not displaying the response is correct. I would like you, however, to notice that in the application console, the log proves the method was called. The next code snippet shows what you find in the application console:
INFO 25020 --- [nio-8080-exec-2] c.l.s.controllers.MainController : Test method called
This aspect is important! I meet many developers who understand CORS as a restriction similar to authorization or CSRF protection. Instead of being a restriction, CORS helps to relax a rigid constraint for cross-domain calls. And even with restrictions applied, in some situations, the endpoint can be called. This behavior doesn’t always happen. Sometimes, the browser first makes a call using the HTTP OPTIONS method to test whether the request should be allowed. We call this test request a preflight request. If the preflight request fails, the browser won’t attempt to honor the original request.
The preflight request and the decision to make it or not are the responsibility of the browser. You don’t have to implement this logic. But it is important to understand it, so you won’t be surprised to see cross-origin calls to the backend even if you did not specify any CORS policies for specific domains. This could happen, as well, when you have a client-side app developed with a framework like Angular or ReactJS. Figure 10.15 presents this request flow.
When the browser omits to make the preflight request if the HTTP method is GET, POST, or OPTIONS, it only has some basic headers as described in the official documentation at https://www.w3.org/TR/cors/#simple-cross-origin-request-0
In our example, the browser makes the request, but we don’t accept the response if the origin is not specified in the response, as shown in figures 10.9 and 10.10. The CORS mechanism is, in the end, related to the browser and not a way to secure endpoints. The only thing it guarantees is that only origin domains that you allow can make requests from specific pages in the browser.
 
Figure 10.15 For simple requests, the browser sends the original request directly to the server. The browser rejects the response if the server doesn’t allow the origin. In some cases, the browser sends a preflight request to test if the server accepts the origin. If the preflight request succeeds, the browser sends the original request.
10.2.2 APPLYING CORS POLICIES WITH THE @CROSSORIGIN ANNOTATION
In this section, we discuss how to configure CORS to allow requests from different domains using the @CrossOrigin annotation. You can place the @CrossOrigin annotation directly above the method that defines the endpoint and configure it using the allowed origins and methods. As you learn in this section, the advantage of using the @CrossOrigin annotation is that it makes it easy to configure CORS for each endpoint.
We use the application we created in section 10.2.1 to demonstrate how @CrossOrigin works. To make the cross-origin call work in the application, the only thing you need to do is to add the @CrossOrigin annotation over the test() method in the controller class. The following listing shows how to use the annotation to make the localhost an allowed origin.
Listing 10.22 Making localhost an allowed origin
@PostMapping("/test")
@ResponseBody
@CrossOrigin("http://localhost:8080")      ❶
public String test() {
  logger.info("Test method called");
  return "HELLO";
}
❶ Allows the localhost origin for cross-origin requests
You can rerun and test the application. This should now display on the page the string returned by the /test endpoint: HELLO.
The value parameter of @CrossOrigin receives an array to let you define multiple origins; for example, @CrossOrigin({"example.com", "example.org"}). You can also set the allowed headers and methods using the allowedHeaders attribute and the methods attribute of the annotation. For both origins and headers, you can use the asterisk (*) to represent all headers or all origins. But I recommend you exercise caution with this approach. It’s always better to filter the origins and headers that you want to allow and never allow any domain to implement code that accesses your applications’ resources.
By allowing all origins, you expose the application to cross-site scripting (XSS) requests, which eventually can lead to DDoS attacks, as we discussed in chapter 1. I personally avoid allowing all origins even in test environments. I know that applications sometimes happen to run on wrongly defined infrastructures that use the same data centers for both test and production. It is wiser to treat all layers on which security applies independently, as we discussed in chapter 1, and to avoid assuming that the application doesn’t have particular vulnerabilities because the infrastructure doesn’t allow it.
The advantage of using @CrossOrigin to specify the rules directly where the endpoints are defined is that it creates good transparency of the rules. The disadvantage is that it might become verbose, forcing you to repeat a lot of code. It also imposes the risk that the developer might forget to add the annotation for newly implemented endpoints. In section 10.2.3, we discuss applying the CORS configuration centralized within the configuration class.
10.2.3 APPLYING CORS USING A CORSCONFIGURER
Although using the @CrossOrigin annotation is easy, as you learned in section 10.2.2, you might find it more comfortable in a lot of cases to define CORS configuration in one place. In this section, we change the example we worked on in sections 10.2.1 and 10.2.2 to apply CORS configuration in the configuration class using a Customizer. In the next listing, you can find the changes we need to make in the configuration class to define the origins we want to allow.
Listing 10.23 Defining CORS configurations centralized in the configuration class
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors(c -> {                                              ❶
      CorsConfigurationSource source = request -> {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(
            List.of("example.com", "example.org"));
        config.setAllowedMethods(
            List.of("GET", "POST", "PUT", "DELETE"));
        return config;
      };
      c.configurationSource(source);
    });

    http.csrf().disable();

    http.authorizeRequests()
         .anyRequest().permitAll();
  }
}
❶ Calls cors() to define the CORS configuration. Within it, we create a CorsConfiguration object where we set the allowed origins and methods.
The cors() method that we call from the HttpSecurity object receives as a parameter a Customizer<CorsConfigurer> object. For this object, we set a CorsConfigurationSource, which returns CorsConfiguration for an HTTP request. CorsConfiguration is the object that states which are the allowed origins, methods, and headers. If you use this approach, you have to specify at least which are the origins and the methods. If you only specify the origins, your application won’t allow the requests. This behavior happens because a CorsConfiguration object doesn’t define any methods by default.
In this example, to make the explanation straightforward, I provide the implementation for CorsConfigurationSource as a lambda expression in the configure() method directly. I strongly recommend to separate this code in a different class in your applications. In real-world applications, you could have much longer code, so it becomes difficult to read if not separated by the configuration class.
Summary
- A cross-site request forgery (CSRF) is a type of attack where the user is tricked into accessing a page containing a forgery script. This script can impersonate a user logged into an application and execute actions on their behalf.
- CSRF protection is by default enabled in Spring Security.
- The entry point of CSRF protection logic in the Spring Security architecture is an HTTP filter.
- Cross-over resource sharing (CORS) refers to the situation in which a web application hosted on a specific domain tries to access content from another domain. By default, the browser doesn’t allow this to happen. CORS configuration enables you to allow a part of your resources to be called from a different domain in a web application run in the browser.
- You can configure CORS both for an endpoint using the @CrossOrigin annotation or centralized in the configuration class using the cors() method of the HttpSecurity object.
- Copy
- Add Highlight
- Add Note