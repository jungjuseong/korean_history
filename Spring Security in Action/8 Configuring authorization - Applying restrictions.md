
# 8 Configuring authorization: Applying restrictions
This chapter covers
- Selecting requests to apply restrictions using matcher methods
- Learning best-case scenarios for each matcher method
In chapter 7, you learned how to configure access based on authorities and roles. But we only applied the configurations for all of the endpoints. In this chapter, you’ll learn how to apply authorization constraints to a specific group of requests. In production applications, it’s less probable that you’ll apply the same rules for all requests. You have endpoints that only some specific users can call, while other endpoints might be accessible to everyone. Each application, depending on the business requirements, has its own custom authorization configuration. Let’s discuss the options you have to refer to different requests when you write access configurations.
Even though we didn’t call attention to it, the first matcher method you used was the anyRequest() method. As you used it in the previous chapters, you know now that it refers to all requests, regardless of the path or HTTP method. It is the way you say “any request” or, sometimes, “any other request.”
First, let’s talk about selecting requests by path; then we can also add the HTTP method to the scenario. To choose the requests to which we apply authorization configuration, we use matcher methods. Spring Security offers you three types of matcher methods:
- MVC matchers--You use MVC expressions for paths to select endpoints.
- Ant matchers--You use Ant expressions for paths to select endpoints.
- regex matchers--You use regular expressions (regex) for paths to select endpoints.
8.1 Using matcher methods to select endpoints
In this section, you learn how to use matcher methods in general so that in sections 8.2 through 8.4, we can continue describing each of the three options you have: MVC, Ant, and regex. By the end of this chapter, you’ll be able to apply matcher methods in any authorization configurations you might need to write for your application’s requirements. Let’s start with a straightforward example.
We create an application that exposes two endpoints: /hello and /ciao. We want to make sure that only users having the ADMIN role can call the /hello endpoint. Similarly, we want to make sure that only users having the MANAGER role can call the /ciao endpoint. You can find this example in the project ssia-ch8-ex1. The following listing provides the definition of the controller class.
Listing 8.1 The definition of the controller class
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }

  @GetMapping("/ciao")
  public String ciao() {
    return "Ciao!";
  }
}
In the configuration class, we declare an InMemoryUserDetailsManager as our UserDetailsService instance and add two users with different roles. The user John has the ADMIN role, while Jane has the MANAGER role. To specify that only users having the ADMIN role can call the endpoint /hello when authorizing requests, we use the mvcMatchers() method. The next listing presents the definition of the configuration class.
Listing 8.2 The definition of the configuration class
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public UserDetailsService userDetailsService() {
    var manager = new InMemoryUserDetailsManager();

    var user1 = User.withUsername("john")
            .password("12345")
            .roles("ADMIN")
            .build();

    var user2 = User.withUsername("jane")
            .password("12345")
            .roles("MANAGER")
            .build();

    manager.createUser(user1);
    manager.createUser(user2);

    return manager;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
         .mvcMatchers("/hello").hasRole("ADMIN")      ❶
         .mvcMatchers("/ciao").hasRole("MANAGER");    ❷
  }

}
❶ Only calls the path /hello if the user has the ADMIN role
❷ Only calls the path /ciao if the user has the Manager role
You can run and test this application. When you call the endpoint /hello with user John, you get a successful response. But if you call the same endpoint with user Jane, the response status returns an HTTP 403 Forbidden. Similarly, for the endpoint /ciao, you can only use Jane to get a successful result. For user John, the response status returns an HTTP 403 Forbidden. You can see the example calls using cURL in the next code snippets. To call the endpoint /hello for user John, use
curl -u john:12345 http://localhost:8080/hello
The response body is
Hello!
To call the endpoint /hello for user Jane, use
curl -u jane:12345 http://localhost:8080/hello
The response body is
{ 
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/hello"
}
To call the endpoint /ciao for user Jane, use
curl -u jane:12345 http://localhost:8080/ciao
The response body is
Hello!
To call the endpoint /ciao for user John, use
curl -u john:12345 http://localhost:8080/ciao
The response body is
{
    "status":403,
    "error":"Forbidden",
    "message":"Forbidden",
    "path":"/ciao"
}
If you now add any other endpoint to your application, it is accessible by default to anyone, even unauthenticated users. Let’s assume you add a new endpoint /hola as presented in the next listing.
Listing 8.3 Adding a new endpoint for path /hola to the application
@RestController
public class HelloController {

  // Omitted code

  @GetMapping("/hola")
  public String hola() {
    return "Hola!";
  }
}
Now when you access this new endpoint, you see that it is accessible with or without having a valid user. The next code snippets display this behavior. To call the endpoint /hola without authenticating, use
curl http://localhost:8080/hola
The response body is
Hola!
To call the endpoint /hola for user John, use
curl -u john:12345 http://localhost:8080/hola
The response body is
Hola!
You can make this behavior more visible if you like by using the permitAll() method. You do this by using the anyRequest() matcher method at the end of the configuration chain for the request authorization, as presented in listing 8.4.
NOTE It is good practice to make all your rules explicit. Listing 8.4 clearly and unambiguously indicates the intention to permit requests to endpoints for everyone, except for the endpoints /hello and /ciao.
Listing 8.4 Marking additional requests explicitly as accessible without authentication
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
           .mvcMatchers("/hello").hasRole("ADMIN")
           .mvcMatchers("/ciao").hasRole("MANAGER")
           .anyRequest().permitAll();                     ❶
  }
}
❶ The permitAll() method states that all other requests are allowed without authentication.
NOTE When you use matchers to refer to requests, the order of the rules should be from particular to general. This is why the anyRequest() method cannot be called before a more specific matcher method like mvcMatchers().
Unauthenticated vs. failed authentication
If you have designed an endpoint to be accessible to anyone, you can call it without providing a username and a password for authentication. In this case, Spring Security won’t do the authentication. If you, however, provide a username and a password, Spring Security evaluates them in the authentication process. If they are wrong (not known by the system), authentication fails, and the response status will be 401 Unauthorized. To be more precise, if you call the /hola endpoint for the configuration presented in listing 8.4, the app returns the body “Hola!” as expected, and the response status is 200 OK. For example,
curl http://localhost:8080/hola
The response body is
Hola!
But if you call the endpoint with invalid credentials, the status of the response is 401 Unauthorized. In the next call, I use an invalid password:
curl -u bill:abcde http://localhost:8080/hola
The response body is
{
    "status":401,
    "error":"Unauthorized",
    "message":"Unauthorized",
    "path":"/hola"
}
This behavior of the framework might look strange, but it makes sense as the framework evaluates any username and password if you provide them in the request. As you learned in chapter 7, the application always does authentication before authorization, as this figure shows.
 
The authorization filter allows any request to the /hola path. But because the application first executes the authentication logic, the request is never forwarded to the authorization filter. Instead, the authentication filter replies with an HTTP 401 Unauthorized.
In conclusion, any situation in which authentication fails will generate a response with the status 401 Unauthorized, and the application won’t forward the call to the endpoint. The permitAll() method refers to authorization configuration only, and if authentication fails, the call will not be allowed further.
You could decide, of course, to make all the other endpoints accessible only for authenticated users. To do this, you would change the permitAll() method with authenticated() as presented in the following listing. Similarly, you could even deny all other requests by using the denyAll() method.
Listing 8.5 Making other requests accessible for all authenticated users
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) 
    throws Exception {
    http.httpBasic();

     http.authorizeRequests()
           .mvcMatchers("/hello").hasRole("ADMIN")
           .mvcMatchers("/ciao").hasRole("MANAGER")
           .anyRequest().authenticated();               ❶
  }
}
❶ All other requests are accessible only by authenticated users.
Here, at the end of this section, you’ve become familiar with how you should use matcher methods to refer to requests for which you want to configure authorization restrictions. Now we must go more in depth with the syntaxes you can use.
In most practical scenarios, multiple endpoints can have the same authorization rules, so you don’t have to set them up endpoint by endpoint. As well, you sometimes need to specify the HTTP method, not only the path, as we’ve done until now. Sometimes, you only need to configure rules for an endpoint when its path is called with HTTP GET. In this case, you’d need to define different rules for HTTP POST and HTTP DELETE. In the next sections, we take each type of matcher method and discuss these aspects in detail.
8.2 Selecting requests for authorization using MVC matchers
In this section, we discuss MVC matchers. Using MVC expressions is a common approach to refer to requests for applying authorization configuration. So I expect you to have many opportunities to use this method to refer to requests in the applications you develop.
This matcher uses the standard MVC syntax for referring to paths. This syntax is the same one you use when writing endpoint mappings with annotations like @RequestMapping, @GetMapping, @PostMapping, and so forth. The two methods you can use to declare MVC matchers are as follows:
- mvcMatchers(HttpMethod method, String... patterns)--Lets you specify both the HTTP method to which the restrictions apply and the paths. This method is useful if you want to apply different restrictions for different HTTP methods for the same path.
- mvcMatchers(String... patterns)--Simpler and easier to use if you only need to apply authorization restrictions based on paths. The restrictions can automatically apply to any HTTP method used with the path.
In this section, we approach multiple ways of using mvcMatchers() methods. To demonstrate this, we start by writing an application that exposes multiple endpoints.
For the first time, we write endpoints that can be called with other HTTP methods besides GET. You might have observed that until now, I’ve avoided using other HTTP methods. The reason for this is that Spring Security applies, by default, protection against cross-site request forgery (CSRF). In chapter 1, I described CSRF, which is one of the most common vulnerabilities for web applications. For a long time, CSRF was present in the OWASP Top 10 vulnerabilities. In chapter 10, we’ll discuss how Spring Security mitigates this vulnerability by using CSRF tokens. But to make things simpler for the current example and to be able to call all endpoints, including those exposed with POST, PUT, or DELETE, we need to disable CSRF protection in our configure() method:
http.csrf().disable();
NOTE We disable CSRF protection now only to allow you to focus for the moment on the discussed topic: matcher methods. But don’t rush to consider this a good approach. In chapter 10, we’ll discuss in detail the CSRF protection provided by Spring Security.
We start by defining four endpoints to use in our tests:
- /a using the HTTP method GET
- /a using the HTTP method POST
- /a/b using the HTTP method GET
- /a/b/c using the HTTP method GET
With these endpoints, we can consider different scenarios for authorization configuration. In listing 8.6, you can see the definitions of these endpoints. You can find this example in the project ssia-ch8-ex2.
Listing 8.6 Definition of the four endpoints for which we configure authorization
@RestController
public class TestController {

  @PostMapping("/a")
  public String postEndpointA() {
    return "Works!";
  }

  @GetMapping("/a")
  public String getEndpointA() {
    return "Works!";
  }

  @GetMapping("/a/b")
  public String getEnpointB() {
    return "Works!";
  }

  @GetMapping("/a/b/c")
  public String getEnpointC() {
    return "Works!";
  }
}
We also need a couple of users with different roles. To keep things simple, we continue using an InMemoryUserDetailsManager. In the next listing, you can see the definition of the UserDetailsService in the configuration class.
Listing 8.7 The definition of the UserDetailsService
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public UserDetailsService userDetailsService() {
    var manager = new InMemoryUserDetailsManager();     ❶

    var user1 = User.withUsername("john")
            .password("12345")
            .roles("ADMIN")                             ❷
            .build();

    var user2 = User.withUsername("jane")
            .password("12345")
            .roles("MANAGER")                           ❸
            .build();

    manager.createUser(user1);
    manager.createUser(user2);

    return manager;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();          ❹
  }
}
❶ Defines an InMemoryUserDetailsManager to store users
❷ User john has the ADMIN role
❸ User jane has the MANAGER role
❹ Don’t forget you also need to add a PasswordEncoder.
Let’s start with the first scenario. For requests done with an HTTP GET method for the /a path, the application needs to authenticate the user. For the same path, requests using an HTTP POST method don’t require authentication. The application denies all other requests. The following listing shows the configurations that you need to write to achieve this setup.
Listing 8.8 Authorization configuration for the first scenario, /a
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {
    
  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
            .mvcMatchers(HttpMethod.GET, "/a")
               .authenticated()                    ❶
            .mvcMatchers(HttpMethod.POST, "/a")
               .permitAll()                        ❷
            .anyRequest()
               .denyAll();                         ❸

    http.csrf().disable();                         ❹
  }
}
❶ For path /a requests called with an HTTP GET method, the app needs to authenticate the user.
❷ Permits path /a requests called with an HTTP POST method for anyone
❸ Denies any other request to any other path
❹ Disables CSRF to enable a call to the /a path using the HTTP POST method
In the next code snippets, we analyze the results on the calls to the endpoints for the configuration presented in listing 8.8. For the call to path /a using the HTTP method POST without authenticating, use this cURL command:
curl -XPOST http://localhost:8080/a   
The response body is
Works!
When calling path /a using HTTP GET without authenticating, use
curl -XGET http://localhost:8080/a   
The response is
{
  "status":401,
  "error":"Unauthorized",
  "message":"Unauthorized",
  "path":"/a"
}
If you want to change the response to a successful one, you need to authenticate with a valid user. For the following call
curl -u john:12345 -XGET http://localhost:8080/a   
the response body is
Works!
But user John isn’t allowed to call path /a/b, so authenticating with his credentials for this call generates a 403 Forbidden:
curl -u john:12345 -XGET http://localhost:8080/a/b  
The response is
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/a/b"
}
With this example, you now know how to differentiate requests based on the HTTP method. But, what if multiple paths have the same authorization rules? Of course, we can enumerate all the paths for which we apply authorization rules, but if we have too many paths, this makes reading code uncomfortable. As well, we might know from the beginning that a group of paths with the same prefix always has the same authorization rules. We want to make sure that if a developer adds a new path to the same group, it doesn’t also change the authorization configuration. To manage these cases, we use path expressions. Let’s prove these in an example.
For the current project, we want to ensure that the same rules apply for all requests for paths starting with /a/b. These paths in our case are /a/b and /a/b/c. To achieve this, we use the ** operator. (Spring MVC borrows the path-matching syntaxes from Ant.) You can find this example in the project ssia-ch8-ex3.
Listing 8.9 Changes in the configuration class for multiple paths
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter { 

  // Omitted code

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
          .mvcMatchers( "/a/b/**")       ❶
             .authenticated()    
          .anyRequest()
             .permitAll();

    http.csrf().disable();
  }
}
❶ The /a/b/** expression refers to all paths prefixed with /a/b.
With the configuration given in listing 8.9, you can call path /a without being authenticated, but for all paths prefixed with /a/b, the application needs to authenticate the user. The next code snippets present the results of calling the /a, /a/b, and /a/b/c endpoints. First, to call the /a path without authenticating, use
curl http://localhost:8080/a
The response body is
Works!
To call the /a/b path without authenticating, use
curl http://localhost:8080/a/b
The response is
{ 
  "status":401,
  "error":"Unauthorized",
  "message":"Unauthorized",
  "path":"/a/b"
}
To call the /a/b/c path without authenticating, use
curl http://localhost:8080/a/b/c
The response is
{
  "status":401,
  "error":"Unauthorized",
  "message":"Unauthorized",
  "path":"/a/b/c"
}
As presented in the previous examples, the ** operator refers to any number of pathnames. You can use it as we have done in the last example so that you can match requests with paths having a known prefix. You can also use it in the middle of a path to refer to any number of pathnames or to refer to paths ending in a specific pattern like /a/**/c. Therefore, /a/**/c would not only match /a/b/c but also /a/b/d/c and a/b/c/d/e/c and so on. If you only want to match one pathname, then you can use a single *. For example, a/*/c would match a/b/c and a/d/c but not a/b/d/c.
Because you generally use path variables, you can find it useful to apply authorization rules for such requests. You can even apply rules referring to the path variable value. Do you remember the discussion from section 8.1 about the denyAll() method and restricting all requests?
Let’s turn now to a more suitable example of what you have learned in this section. We have an endpoint with a path variable, and we want to deny all requests that use a value for the path variable that has anything else other than digits. You can find this example in the project ssia-ch8-ex4. The following listing presents the controller.
Listing 8.10 The definition of an endpoint with a path variable in a controller class
@RestController
public class ProductController {

  @GetMapping("/product/{code}")
  public String productCode(@PathVariable String code) {
    return code;
  }
}
The next listing shows you how to configure authorization such that only calls that have a value containing only digits are always permitted, while all other calls are denied.
Listing 8.11 Configuring the authorization to permit only specific digits
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
         .mvcMatchers
          ➥ ("/product/{code:^[0-9]*$}")          ❶
              .permitAll()
         .anyRequest()
              .denyAll();
    }
}
❶ The regex refers to strings of any length, containing any digit.
NOTE When using parameter expressions with a regex, make sure to not have a space between the name of the parameter, the colon (:), and the regex, as displayed in the listing.
Running this example, you can see the result as presented in the following code snippets. The application only accepts the call when the path variable value has only digits. To call the endpoint using the value 1234a, use
curl http://localhost:8080/product/1234a
The response is
{
  "status":401,
  "error":"Unauthorized",
  "message":"Unauthorized",
  "path":"/product/1234a"
}
To call the endpoint using the value 12345, use
curl http://localhost:8080/product/12345
The response is
12345
We discussed a lot and included plenty of examples of how to refer to requests using MVC matchers. Table 8.1 is a refresher for the MVC expressions you used in this section. You can simply refer to it later when you want to remember any of them.
Table 8.1 Common expressions used for path matching with MVC matchers
Expression	Description
/a	Only path /a.
/a/*	The * operator replaces one pathname. In this case, it matches /a/b or /a/c, but not /a/b/c.
/a/**	The ** operator replaces multiple pathnames. In this case, /a as well as /a/b and /a/b/c are a match for this expression.
/a/{param}	This expression applies to the path /a with a given path parameter.
/a/{param:regex}	This expression applies to the path /a with a given path parameter only when the value of the parameter matches the given regular expression.
8.3 Selecting requests for authorization using Ant matchers
In this section, we discuss Ant matchers for selecting requests for which the application applies authorization rules. Because Spring borrows the MVC expressions to match paths to endpoints from Ant, the syntaxes that you can use with Ant matchers are the same as those that you saw in section 8.2. But there’s a trick I’ll show you in this section--a significant difference you should be aware of. Because of this, I recommend that you use MVC matchers rather than Ant matchers. However, in the past, I’ve seen Ant matchers used a lot in applications. For this reason as well, I want to make you aware of this difference. You can still find Ant matchers in production applications today, which makes them important. The three methods when using Ant matchers are
- antMatchers(HttpMethod method, String patterns)--Allows you to specify both the HTTP method to which the restrictions apply and the Ant patterns that refer to the paths. This method is useful if you want to apply different restrictions for different HTTP methods for the same group of paths.
- antMatchers(String patterns)--Simpler and easier to use if you only need to apply authorization restrictions based on paths. The restrictions automatically apply for any HTTP method.
- antMatchers(HttpMethod method), which is the equivalent of ant Matchers(httpMethod, “/**”)--Allows you to refer to a specific HTTP method disregarding the paths.
The way that you apply these is similar to the MVC matchers in the previous section. Also, the syntaxes we use for referring to paths are the same. So what is different then? The MVC matchers refer exactly to how your Spring application understands matching requests to controller actions. And, sometimes, multiple paths could be interpreted by Spring to match the same action. My favorite example that’s simple but makes a significant impact in terms of security is the following: any path (let’s take, for example, /hello) to the same action can be interpreted by Spring if you append another / after the path. In this case, /hello and /hello/ call the same method. If you use an MVC matcher and configure security for the /hello path, it automatically secures the /hello/ path with the same rules. This is huge! A developer not knowing this and using Ant matchers could leave a path unprotected without noticing it. And this, as you can imagine, creates a major security breach for the application.
Let’s test this behavior with an example. You can find this example in the project ssia-ch8-ex5. The following listing shows you how to define the controller.
Listing 8.12 Definition of the /hello endpoint in the controller class
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
Listing 8.13 describes the configuration class. In this case, I use an MVC matcher to define the authorization configuration for the /hello path. (We compare this to an Ant matcher next.) Any request to this endpoint requires authentication. I omit the definition of the UserDetailsService and PasswordEncoder from the example, as these are the same as in listing 8.7.
Listing 8.13 The configuration class using an MVC matcher
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
         .mvcMatchers( "/hello")
           .authenticated();
  }
}
If you start the application and test it, you’ll observe that authentication is required for both the /hello and /hello/ paths. This is probably what you would expect to happen. The next code snippets show the requests made with cURL for these paths. Calling the /hello endpoint unauthenticated looks like this:
curl http://localhost:8080/hello
The response is
{
  "status":401,
  "error":"Unauthorized",
  "message":"Unauthorized",
  "path":"/hello"
}
Calling the /hello endpoint using the /hello/ path (with one more / at the end), unauthenticated looks like this:
curl http://localhost:8080/hello/
The response is
{
  "status":401,
  "error":"Unauthorized",
  "message":"Unauthorized",
  "path":"/hello"
}
Calling the /hello endpoint authenticating as Jane looks like this:
curl -u jane:12345 http://localhost:8080/hello 
The response body is
Hello!
And calling the /hello endpoint using the /hello/ path (with one more / at the end) and authenticating as Jane looks like this:
curl -u jane:12345 http://localhost:8080/hello/
The response body is
Hello!
All of these responses are what you probably expected. But let’s see what happens if we change the implementation to use Ant matchers. If you just change the configuration class to use an Ant matcher for the same expression, the result changes. As stated, the app doesn’t apply the authorization configurations for the /hello/ path. In fact, the Ant matchers apply exactly the given Ant expressions for patterns but know nothing about subtle Spring MVC functionality. In this case, /hello doesn’t apply as an Ant expression to the /hello/ path. If you also want to secure the /hello/ path, you have to individually add it or write an Ant expression that matches it as well. The following listing shows the change made in the configuration class using an Ant matcher instead of the MVC matcher.
Listing 8.14 The configuration class using an Ant matcher
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()
          .antMatchers( "/hello").authenticated();
  }
}
The next code snippets provide the results for calling the endpoint with the /hello and /hello/ paths. To call the /hello endpoint unauthenticated, use
curl http://localhost:8080/hello
The response is
{
  "status":401,
  "error":"Unauthorized",
  "message":"Unauthorized",
  "path":"/hello"
}
To call the /hello endpoint unauthenticated but using the path /hello/ (with one more / at the end), use
curl http://localhost:8080/hello/
The response is
Hello!
IMPORTANT To say it again: I recommend and prefer MVC matchers. Using MVC matchers, you avoid some of the risks involved with the way Spring maps paths to actions. This is because you know that the way paths are interpreted for the authorization rules are the same as Spring itself interprets these for mapping the paths to endpoints. When you use Ant matchers, exercise caution and make sure your expressions indeed match everything for which you need to apply authorization rules.
Effects of communication and knowledge sharing
I always encourage sharing knowledge in all possible ways: books, articles, conferences, videos, and so on. Sometimes even a short discussion can raise questions that drive dramatic improvements and changes. I’ll illustrate what I mean through a story from a course about Spring I delivered a couple of years ago.
The training was designed for a group of intermediate developers who were working for a specific project. It wasn’t directly related to Spring Security, but at some point, we started using matcher methods for one of the examples we were working as part of the training.
I started configuring the endpoint authorization rules with MVC matchers without first teaching the participants about MVC matchers. I thought that they would have already used these in their projects; I didn’t think it mandatory to explain them first. While I was working on the configuration and teaching about what I was doing, one of the attendees asked a question. I still remember the shy voice of the lady saying, “Could you introduce these MVC methods you’re using? We’re configuring our endpoint security with some Ant-something methods.”
I realized then that the attendees might not be aware of what they were using. And I was right. They were indeed working with Ant matchers, but didn’t understand these
(continued)
configurations and were most probably using them mechanically. Copy-and-paste programming is a risky approach, one that’s unfortunately used too often, especially by junior developers. You should never use something without understanding what it does!
While we were discussing the new subject, the same lady found in their implementations a situation in which Ant matchers were wrongly applied. The training ended with their team scheduling a full sprint to verify and correct such mistakes, which could have led to very dangerous vulnerabilities in their app.
8.4 Selecting requests for authorization using regex matchers
In this section, we discuss regular expression (regex). You should already be aware of what regular expressions are, but you don’t need to be an expert in the subject. Any of the books recommended at https://www.regular-expressions.info/books.html are excellent resources from which you can learn about the subject in more depth. For writing regex, I also often use online generators like https://regexr.com/ (figure 8.1).
 
Figure 8.1 Letting your cat play over the keyboard is not the best solution for generating regular expressions (regex). To learn how to generate regexes youcan use an online generator like https://regexr.com/.
You learned in sections 8.2 and 8.3 that in most cases, you can use MVC and Ant syntaxes to refer to requests to which you apply authorization configurations. In some cases, however, you might have requirements that are more particular, and you cannot solve those with Ant and MVC expressions. An example of such a requirement could be this: “Deny all requests when paths contain specific symbols or characters.” For these scenarios, you need to use a more powerful expression like a regex.
You can use regexes to represent any format of a string, so they offer limitless possibilities for this matter. But they have the disadvantage of being difficult to read, even when applied to simple scenarios. For this reason, you might prefer to use MVC or Ant matchers and fall back to regexes only when you have no other option. The two methods that you can use to implement regex matchers are as follows:
- regexMatchers(HttpMethod method, String regex)--Specifies both the HTTP method to which restrictions apply and the regexes that refer to the paths. This method is useful if you want to apply different restrictions for different HTTP methods for the same group of paths.
- regexMatchers(String regex)--Simpler and easier to use if you only need to apply authorization restrictions based on paths. The restrictions automatically apply for any HTTP method.
To prove how regex matchers work, let’s put them into action with an example: building an application that provides video content to its users. The application that presents the video gets its content by calling the endpoint /video/{country}/{language}. For the sake of the example, the application receives the country and language in two path variables from where the user makes the request. We consider that any authenticated user can see the video content if the request comes from the US, Canada, or the UK, or if they use English.
You can find this example implemented in the project ssia-ch8-ex6. The endpoint we need to secure has two path variables, as shown in the following listing. This makes the requirement complicated to implement with Ant or MVC matchers.
Listing 8.15 The definition of the endpoint for the controller class
@RestController
public class VideoController {

  @GetMapping("/video/{country}/{language}")
  public String video(@PathVariable String country,
                      @PathVariable String language) {
    return "Video allowed for " + country + " " + language;
  }
}
For a condition on a single path variable, we can write a regex directly in the Ant or MVC expressions. We referred to such an example in section 8.3, but I didn’t go in depth about it at that time because we weren’t discussing regexes.
Let’s assume you have the endpoint /email/{email}. You want to apply a rule using a matcher only to the requests that send as a value of the email parameter an address ending in .com. In that case, you write an MVC matcher as presented by the next code snippet. You can find the complete example of this in the project ssia-ch8-ex7.
http.authorizeRequests()
    .mvcMatchers("/email/{email:.*(.+@.+\\.com)}")
       .permitAll()
    .anyRequest()
       .denyAll();
If you test such a restriction, you find that the application only accepts emails ending in .com. For example, to call the endpoint to jane@example.com, you can use this command:
curl http://localhost:8080/email/jane@example.com
The response body is
Allowed for email jane@example.com
And to call the endpoint to jane@example.net, you use this command:
curl http://localhost:8080/email/jane@example.net
The response body is
{
  "status":401,
  "error":"Unauthorized",
  "message":"Unauthorized",
  "path":/email/jane@example.net
}
It is fairly easy and makes it even clearer why we encounter regex matchers less frequently. But, as I said earlier, requirements are complex sometimes. You’ll find it handier to use regex matchers when you find something like the following:
- Specific configurations for all paths containing phone numbers or email addresses
- Specific configurations for all paths having a certain format, including what is sent through all the path variables
Back to our regex matchers example (ssia-ch8-ex6): when you need to write a more complex rule, eventually referring to more path patterns and multiple path variable values, it’s easier to write a regex matcher. In listing 8.16, you find the definition for the configuration class that uses a regex matcher to solve the requirement given for the /video/{country}/{language} path. We also add two users with different authorities to test the implementation.
Listing 8.16 The configuration class using a regex matcher
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public UserDetailsService userDetailsService() {
    var uds = new InMemoryUserDetailsManager();

    var u1 = User.withUsername("john")
                 .password("12345")
                 .authorities("read")
                 .build();

    var u2 = User.withUsername("jane")
                .password("12345")
                .authorities("read", "premium")
                .build();

    uds.createUser(u1);
    uds.createUser(u2);

    return uds;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic();

    http.authorizeRequests()                        ❶
        .regexMatchers(".*/(us|uk|ca)+/(en|fr).*")
            .authenticated()
        .anyRequest()
            .hasAuthority("premium");               ❷

  }
}
❶ We use a regex to match the paths for which the user only needs to be authenticated.
❷ Configures the other paths for which the user needs to have premium access
Running and testing the endpoints confirm that the application applied the authorization configurations correctly. The user John can call the endpoint with the country code US and language en, but he can’t call the endpoint for the country code FR and language fr due to the restrictions we configured. Calling the /video endpoint and authenticating user John for the US region and the English language looks like this:
curl -u john:12345 http://localhost:8080/video/us/en
The response body is
Video allowed for us en
Calling the /video endpoint and authenticating user John for the FR region and the French language looks like this:
curl -u john:12345 http://localhost:8080/video/fr/fr
The response body is
{
  "status":403,
  "error":"Forbidden",
  "message":"Forbidden",
  "path":"/video/fr/fr"
}
Having premium authority, user Jane makes both calls with success. For the first call,
curl -u jane:12345 http://localhost:8080/video/us/en
the response body is
Video allowed for us en
And for the second call,
curl -u jane:12345 http://localhost:8080/video/fr/fr
the response body is
Video allowed for fr fr
Regexes are powerful tools. You can use them to refer to paths for any given requirement. But because regexes are hard to read and can become quite long, they should remain your last choice. Use these only if MVC and Ant expressions don’t offer you a solution to your problem.
In this section, I used the most simple example I could imagine so that the needed regex is short. But with more complex scenarios, the regex can become much longer. Of course, you’ll find experts who say any regex is easy to read. For example, a regex used to match an email address might look like the one in the next code snippet. Can you easily read and understand it?
(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])
Summary
- In real-world scenarios, you often apply different authorization rules for different requests.
- You specify the requests for which authorization rules are configured based on path and HTTP method. To do this, you use matcher methods, which come in three flavors: MVC, Ant, and regex.
- The MVC and Ant matchers are similar, and generally, you can choose one of these options to refer to requests for which you apply authorization restrictions.
- When requirements are too complex to be solved with Ant or MVC expressions, you can implement them with the more powerful regexes.
- Copy
- Add Highlight
- Add Note