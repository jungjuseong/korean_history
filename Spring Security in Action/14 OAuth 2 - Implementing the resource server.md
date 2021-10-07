
 
14 OAuth 2: Implementing the resource server
This chapter covers
- Implementing an OAuth 2 resource server
- Implementing token validation
- Customizing token management
In this chapter, we’ll discuss implementing a resource server with Spring Security. The resource server is the component that manages user resources. The name resource server might not be suggestive to begin with, but in terms of OAuth 2, it represents the backend you secure just like any other app we secured in the previous chapters. Remember, for example, the business logic server we implemented in chapter 11? To allow a client to access the resources, resource server requires a valid access token. A client obtains an access token from the authorization server and uses it to call for resources on the resource server by adding the token to the HTTP request headers. Figure 14.1 provides a refresher from chapter 12, showing the place of the resource server in the OAuth 2 authentication architecture.
 
Figure 14.1 The resource server is one of the components acting in the OAuth 2 architecture. The resource server manages user data. To call an endpoint on the resource server, a client needs to prove with a valid access token that the user approves it to work with their data.
In chapters 12 and 13, we discussed implementing a client and an authorization server. In this chapter, you’ll learn how to implement the resource server. But what’s more important when discussing the resource server implementation is to choose how the resource server validates tokens. We have multiple options for implementing token validation at the resource server level. I’ll briefly describe the three options and then detail them one by one. The first option allows the resource server to directly call the authorization server to verify an issued token. Figure 14.2 shows this option.
 
Figure 14.2 To validate the token, the resource server calls the authorization server directly. The authorization server knows whether it issued a specific token or not.
The second option uses a common database where the authorization server stores tokens, and then the resource server can access and validate the tokens (figure 14.3). This approach is also called blackboarding.
 
Figure 14.3 Blackboarding. Both the authorization server and the resource server access a shared database. The authorization server stores the tokens in this database after it issues them. The resource server can then access them to validate the tokens it receives.
Finally, the third option uses cryptographic signatures (figure 14.4). The authorization server signs the token when issuing it, and the resource server validates the signature. Here’s where we generally use JSON Web Tokens (JWTs). We discuss this approach in chapter 15.
 
Figure 14.4 When issuing an access token, the authorization server uses a private key to sign it. To verify a token, the resource server only needs to check if the signature is valid.
14.1 Implementing a resource server
We start with the implementation of our first resource server application, the last piece of the OAuth 2 puzzle. The reason why we have an authorization server that issues tokens is to allow clients to access a user’s resources. The resource server manages and protects the user’s resources. For this reason, you need to know how to implement a resource server. We use the default implementation provided by Spring Boot, which allows the resource server to directly call the authorization server to find out if a token is valid (figure 14.5).
NOTE As in the case of the authorization server, the implementation of the resource server suffered changes in the Spring community. These changes affect us because now, in practice, you find different ways in which developers implement the resource server. I provide examples in which you can configure the resource server in two ways, such that when you encounter these in real-world scenarios, you will understand and be able to use both.
 
 
Figure 14.5 When the resource server needs to validate a token, it directly calls the authorization server. If the authorization server confirms it issued the token, then the resource server considers the token valid.
To implement a resource server, we create a new project and add the dependencies as in the next code snippet. I named this project ssia-ch14-ex1-rs.
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
Besides the dependencies, you also add the dependencyManagement tag for the spring-cloud-dependencies artifact. The next code snippet shows how to do this:
<dependencyManagement>
   <dependencies>
      <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-dependencies</artifactId>
         <version>Hoxton.SR1</version>
         <type>pom</type>
         <scope>import</scope>
      </dependency>
  </dependencies>
</dependencyManagement>
The purpose of the resource server is to manage and protect a user’s resources. So to prove how it works, we need a resource that we want to access. We create a /hello endpoint for our tests by defining the usual controller as presented in the following listing.
Listing 14.1 The controller class defining the test endpoint
@RestController
public class HelloController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello!";
  }
}
The other thing we need is a configuration class in which we use the @Enable-ResourceServer annotation to allow Spring Boot to configure what’s needed for our app to become a resource server. The following listing presents the configuration class.
Listing 14.2 The configuration class
@Configuration
@EnableResourceServer
public class ResourceServerConfig {
}
We have a resource server now. But it’s not useful if you can’t access the endpoint, as is our case because we didn’t configure any way in which the resource server can check tokens. You know that requests made for resources need to also provide a valid access token. Even if it does provide a valid access token, a request still won’t work. Our resource server cannot verify that these are valid tokens, that the authorization server indeed issued them. This is because we didn’t implement any of the options the resource server has to validate access tokens. Let’s take this approach and discuss our options in the next two sections; chapter 15 presents an additional option.
NOTE As I mentioned in an earlier note, the resource server implementation changed as well. The @EnableResourceServer annotation, which is part of the Spring Security OAuth project, was recently marked as deprecated. In the Spring Security migration guide (https://github.com/spring-projects/spring-security/wiki/OAuth-2.0-Migration-Guide), the Spring Security team invites us to use configuration methods directly from Spring Security. Currently, I still encounter the use of Spring Security OAuth projects in most of the apps I see. For this reason, I consider it important that you understand both approaches that we present as examples in this chapter.
14.2 Checking the token remotely
In this section, we implement token validation by allowing the resource server to call the authorization server directly. This approach is the simplest you can implement to enable access to the resource server with a valid access token. You choose this approach if the tokens in your system are plain (for example, simple UUIDs as in the default implementation of the authorization server with Spring Security). We start by discussing this approach and then we implement it with an example. This mechanism for validating tokens is simple (figure 14.6):
1.	The authorization server exposes an endpoint. For a valid token, it returns the granted authorities of the user to whom it was issued earlier. Let’s call this endpoint the check_token endpoint.
2.	The resource server calls the check_token endpoint for each request. This way, it validates the token received from the client and also obtains the client-granted authorities.
 
Figure 14.6 To validate a token and obtain details about it, the resource server calls the endpoint /oauth/check_token of the authorization server. The resource server uses the details retrieved about the token to authorize the call.
The advantage of this approach is its simplicity. You can apply it to any kind of token implementation. The disadvantage of this approach is that for each request on the resource server having a new, as yet unknown token, the resource server calls the authorization server to validate the token. These calls can put an unnecessary load on the authorization server. Also, remember the rule of thumb: the network is not 100% reliable. You need to keep this in mind every time you design a new remote call in your architecture. You might also need to apply some alternative solutions for what happens if the call fails because of some network instability (figure 14.7).
 
Figure 14.7 The network is not 100% reliable. If the connection between the resource server and the authorization server is down, tokens cannot be validated. This implies that the resource server refuses the client access to the user’s resources even if it has a valid token.
Let’s continue our resource server implementation in project ssia-ch14-ex1-rs. What we want is to allow a client to access the /hello endpoint if it provides an access token issued by an authorization server. We already developed authorization servers in chapter 13. We could use, for example, the project ssia-ch13-ex1 as our authorization server. But to avoid changing the project we discussed in the previous section, I created a separate project for this discussion, ssia-ch14-ex1-as. Mind that it now has the same structure as the project ssia-ch13-ex1, and what I present to you in this section is only the changes I made with regard to our current discussion. You can choose to continue our discussion using the authorization server we implemented in either ssia-ch13-ex2, ssia-ch13-ex3, or ssia-ch13-ex4 if you’d like.
NOTE You can use the configuration we discuss here with any other grant type that I described in chapter 12. Grant types are the flows implemented by the OAuth 2 framework in which the client gets a token issued by the authorization server. So you can choose to continue our discussion using the authorization server we implemented in ssia-ch13-ex2, ssia-ch13-ex3, or ssia-ch13-ex4 projects if you’d like.
By default, the authorization server implements the endpoint /oauth/check_token that the resource server can use to validate a token. However, at present the authorization server implicitly denies all requests to that endpoint. Before using the /oauth/check_token endpoint, you need to make sure the resource server can call it.
To allow authenticated requests to call the /oauth/check_token endpoint, we override the configure(AuthorizationServerSecurityConfigurer c) method in the AuthServerConfig class of the authorization server. Overriding the configure() method allows us to set the condition in which we can call the /oauth/check_token endpoint. The following listing shows you how to do this.
Listing 14.3 Enabling authenticated access to the check_token endpoint
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) 
      throws Exception {

      clients.inMemory()
             .withClient("client")
             .secret("secret")
             .authorizedGrantTypes("password", "refresh_token")
             .scopes("read");
  }

  @Override
  public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints.authenticationManager(authenticationManager);
  }
    
  public void configure(
    AuthorizationServerSecurityConfigurer security) {
      security.checkTokenAccess
                ("isAuthenticated()");        ❶
  }
}
❶ Specifies the condition for which we can call the check_token endpoint
NOTE You can even make this endpoint accessible without authentication by using permitAll() instead of isAuthenticated(). But it’s not recommended to leave endpoints unprotected. Preferably, in a real-world scenario, you would use authentication for this endpoint.
Besides making this endpoint accessible, if we decide to allow only authenticated access, then we need a client registration for the resource server itself. For the authorization server, the resource server is also a client and requires its own credentials. We add these as for any other client. For the resource server, you don’t need any grant type or scope, but only a set of credentials that the resource server uses to call the check_token endpoint. The next listing presents the change in configuration to add the credentials for the resource server in our example.
Listing 14.4 Adding credentials for the resource server
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
        extends AuthorizationServerConfigurerAdapter {

  // Omitted code

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) 
      throws Exception {

      clients.inMemory()
             .withClient("client")
             .secret("secret")
             .authorizedGrantTypes("password", "refresh_token")
             .scopes("read")
               .and()                           ❶
             .withClient("resourceserver")      ❶
             .secret("resourceserversecret");   ❶
    }
  }
❶ Adds a set of credentials for the resource server to use when calling the /oauth/check_token endpoint
You can now start the authorization server and obtain a token like you learned in chapter 13. Here’s the cURL call:
curl -v -XPOST -u client:secret "http://localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read"
The response body is
{
  "access_token":"4f2b7a6d-ced2-43dc-86d7-cbe844d3e16b",
  "token_type":"bearer",
  "refresh_token":"a4bd4660-9bb3-450e-aa28-2e031877cb36",
  "expires_in":43199,"scope":"read"
}
Next, we call the check_token endpoint to find the details about the access token we obtained in the previous code snippet. Here’s that call:
curl -XPOST -u resourceserver:resourceserversecret "http://localhost:8080/oauth/check_token?token=4f2b7a6d-ced2-43dc-86d7-cbe844d3e16b"
The response body is
{
  "active":true,
  "exp":1581307166,
  "user_name":"john",
  "authorities":["read"],
  "client_id":"client",
  "scope":["read"]
}
Observe the response we get back from the check_token endpoint. It tells us all the details needed about the access token:
- Whether the token is still active and when it expires
- The user the token was issued for
- The authorities that represent the privileges
- The client the token was issued for
Now, if we call the endpoint using cURL, the resource server should be able to use it to validate tokens. We need to configure the endpoint of the authorization server and the credentials the resource server uses to access endpoint. We can do all this in the application.properties file. The next code snippet presents the details:
server.port=9090

security.oauth2.resource.token-info-uri=http:/./localhost:8080/oauth/check_token

security.oauth2.client.client-id=resourceserver
security.oauth2.client.client-secret=resourceserversecret
NOTE When we use authentication for the /oauth/check_token (token introspection) endpoint, the resource server acts as a client for the authorization server. For this reason, it needs to have some credentials registered, which it uses to authenticate using HTTP Basic authentication when calling the introspection endpoint.
By the way, if you plan to run both applications on the same system as I do, don’t forget to set a different port using the server.port property. I use port 8080 (the default one) for running the authorization server and port 9090 for the resource server.
You can run both applications and test the whole setup by calling the /hello endpoint. You need to set the access token in the Authorization header of the request, and you need to prefix its value with the word bearer. For this word, the case is insensitive. That means that you can also write “Bearer” or “BEARER.”
curl -H "Authorization: bearer 4f2b7a6d-ced2-43dc-86d7-cbe844d3e16b" "http:/./localhost:9090/hello"
The response body is
Hello!
If you had called the endpoint without a token or with the wrong one, the result would have been a 401 Unauthorized status on the HTTP response. The next code snippet presents the response:
curl -v "http:/./localhost:9090/hello"
The (truncated) response is
...
< HTTP/1.1 401
...
{
  "error":"unauthorized",
  "error_description":"Full authentication is 
    required to access this resource"
}
Using token introspection without Spring Security OAuth
A common concern nowadays is how to implement a resource server as in the previous example without Spring Security OAuth. Although it’s said that Spring Security OAuth is deprecated, in my opinion you should still understand it because there’s a good chance you’ll find these classes in existing projects. To clarify this aspect, I add a comparison where relevant with a way to implement the same thing without Spring Security OAuth. In this sidebar, we discuss the implementation of a resource server using token introspection without using Spring Security OAuth but directly with Spring Security configurations. Fortunately, it’s easier than you might imagine.
If you remember, we discussed httpBasic(), formLogin(), and other authentication methods in the previous chapters. You learned that when calling such a method, you simply add a new filter to the filter chain, which enables a different authentication mechanism in your app. Guess what? In its latest versions, Spring Security also offers an oauth2ResourceServer() method that enables a resource server authentication method. You can use it like any other method you’ve used until now to set up authentication method, and you no longer need the Spring Security OAuth project in your dependencies. However, mind that this functionality isn’t mature yet, and to use it, you need to add other dependencies that are not automatically figured out by Spring Boot. The following code snippet presents the required dependencies for implementing a resource server using token introspection:
<dependency>
   <groupId>org.springframework.security</groupId>
   <artifactId>spring-security-oauth2-resource-server</artifactId>
   <version>5.2.1.RELEASE</version>
</dependency>

<dependency>
   <groupId>com.nimbusds</groupId>
   <artifactId>oauth2-oidc-sdk</artifactId>
   <version>8.4</version>
   <scope>runtime</scope>
</dependency>
Once you add the needed dependencies to your pom.xml file, you can configure the authentication method as shown in the next code snippet:
@Configuration
public class ResourceServerConfig
  extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
           .anyRequest().authenticated()
        .and()
           .oauth2ResourceServer(
              c -> c.opaqueToken(
                 o -> {
                   o.introspectionUri("...");
                   o.introspectionClientCredentials("client", "secret");
              })
           );
  }
}
To make the code snippet easier to be read, I omitted the parameter value of the introspectionUri() method, which is the check_token URI, also known as the introspection token URI. As a parameter to the oauth2ResourceServer() method, I added a Customizer instance. Using the Customizer instance, you specify the parameters needed for the resource server depending on the approach you choose. For direct token introspection, you need to specify the URI the resource server calls to validate the token, and the credentials the resource server needs to authenticate when calling this URI. You’ll find this example implemented in the project ssia-ch14-ex1-rs-migration folder.
14.3 Implementing blackboarding with a JdbcTokenStore
In this section, we implement an application where the authorization server and the resource server use a shared database. We call this architectural style blackboarding. Why blackboarding? You can think of this as the authorization server and the resource server using a blackboard to manage tokens. This approach for issuing and validating tokens has the advantage of eliminating direct communication between the resource server and the authorization server. However, it implies adding a shared database, which might become a bottleneck. Like any architectural style, you can find it applicable to various situations. For example, if you already have your services sharing a database, it might make sense to use this approach for your access tokens as well. For this reason, I consider it important for you to know how to implement this approach.
Like the previous implementations, we work on an application to demonstrate how you use such an architecture. You’ll find this application in the projects as ssia-ch14-ex2-as for the authorization server and ssia-ch14-ex2-rs for the resource server. This architecture implies that when the authorization server issues a token, it also stores the token in the database shared with the resource server (figure 14.8).
 
Figure 14.8 When the authorization server issues a token, it also stores the token in a shared database. This way, the resource server can get the token and validate it later.
It also implies that the resource server accesses the database when it needs to validate the token (figure 14.9).
 
Figure 14.9 The resource server searches for the token in the shared database. If the token exists, the resource server finds the details related to it in the database, including the username and its authorities. With these details, the resource server can then authorize the request.
The contract representing the object that manages tokens in Spring Security, both on the authorization server as well as for the resource server, is the TokenStore. For the authorization server, you can visualize its place in the authentication architecture where we previously used SecurityContext. Once authentication finishes, the authorization server uses the TokenStore to generate a token (figure 14.10).
 
Figure 14.10 The authorization server uses a token store to generate tokens at the end of the authentication process. The client uses these tokens to access resources managed by the resource server.
For the resource server, the authentication filter uses TokenStore to validate the token and find the user details that it later uses for authorization. The resource server then stores the user’s details in the security context (figure 14.11).
 
Figure 14.11 The resource server uses the token store to validate the token and retrieve details needed for authorization. These details are then stored in the security context.
NOTE The authorization server and the resource server implement two different responsibilities, but these don’t necessarily have to be carried out by two separate applications. In most real-world implementations, you develop them in different applications, and this is why we do the same in our examples in this book. But, you can choose to implement both in the same application. In this case, you don’t need to establish any call or have a shared database. If, however, you implement the two responsibilities in the same app, then both the authorization server and resource server can access the same beans. As such, these can use the same token store without needing to do network calls or to access a database.
Spring Security offers various implementations for the TokenStore contract, and in most cases, you won’t need to write your own implementation. For example, for all the previous authorization server implementations, we did not specify a TokenStore implementation. Spring Security provided a default token store of type InMemoryTokenStore. As you can imagine, in all these cases, the tokens were stored in the application’s memory. They did not persist! If you restart the authorization server, the tokens issued before the restart won’t be valid anymore.
To implement token management with blackboarding, Spring Security offers the JdbcTokenStore implementation. As the name suggests, this token store works with a database directly via JDBC. It works similarly to the JdbcUserDetailsManager we discussed in chapter 3, but instead of managing users, the JdbcTokenStore manages tokens.
NOTE In this example, we use the JdbcTokenStore to implement blackboarding. But you could choose to use TokenStore just to persist tokens and continue using the /oauth/check_token endpoint. You would choose to do so if you don’t want to use a shared database, but you need to persist tokens such that if the authorization server restarts, you can still use the previously issued tokens.
JdbcTokenStore expects you to have two tables in the database. It uses one table to store access tokens (the name for this table should be oauth_access _token) and one table to store refresh tokens (the name for this table should be oauth_refresh_token). The table used to store tokens persists the refresh tokens.
NOTE As in the case of the JdbcUserDetailsManager component, which we discussed in chapter 3, you can customize JdbcTokenStore to use other names for tables or columns. JdbcTokenStore methods must override any of the SQL queries it uses to retrieve or store details of the tokens. To keep it short, in our example we use the default names.
We need to change our pom.xml file to declare the necessary dependencies to connect to our database. The next code snippet presents the dependencies I use in my pom.xml file:
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
</dependency>
In the authorization server project ssia-ch14-ex2-as, I define the schema.sql file with the queries needed to create the structure for these tables. Don’t forget that this file needs to be in the resources folder to be picked up by Spring Boot when the application starts. The next code snippet presents the definition of the two tables as presented in the schema.sql file:
CREATE TABLE IF NOT EXISTS `oauth_access_token` (
    `token_id` varchar(255) NOT NULL,
    `token` blob,
    `authentication_id` varchar(255) DEFAULT NULL,
    `user_name` varchar(255) DEFAULT NULL,
    `client_id` varchar(255) DEFAULT NULL,
    `authentication` blob,
    `refresh_token` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`token_id`));

CREATE TABLE IF NOT EXISTS `oauth_refresh_token` (
    `token_id` varchar(255) NOT NULL,
    `token` blob,
    `authentication` blob,
    PRIMARY KEY (`token_id`));
In the application.properties file, you need to add the definition of the data source. The next code snippet provides the definition:
spring.datasource.url=jdbc:mysql://localhost/
➥ spring?useLegacyDatetimeCode=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.datasource.initialization-mode=always
The following listing presents the AuthServerConfig class the way we used it in the first example.
Listing 14.5 The AuthServerConfig class
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) 
    throws Exception {

    clients.inMemory()
           .withClient("client")
           .secret("secret")
           .authorizedGrantTypes("password", "refresh_token")
           .scopes("read");
   }

   @Override
   public void configure(
     AuthorizationServerEndpointsConfigurer endpoints) {
       endpoints.authenticationManager(authenticationManager);
   }
}
We change this class to inject the data source and then define and configure the token store. The next listing shows this change.
Listing 14.6 Defining and configuring JdbcTokenStore
@Configuration
@EnableAuthorizationServer
public class AuthServerConfig
  extends AuthorizationServerConfigurerAdapter {

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private DataSource dataSource;                       ❶

  @Override
  public void configure(
    ClientDetailsServiceConfigurer clients) 
    throws Exception {

      clients.inMemory()
             .withClient("client")
             .secret("secret")
             .authorizedGrantTypes("password", "refresh_token")
             .scopes("read");
  }

  @Override
  public void configure(
    AuthorizationServerEndpointsConfigurer endpoints) {
      endpoints
        .authenticationManager(authenticationManager)
        .tokenStore(tokenStore());                    ❷
  }

  @Bean
  public TokenStore tokenStore() {                    ❸
      return new JdbcTokenStore(dataSource);          ❸
  }                                                   ❸
}
❶ Injects the data source we configured in the application.properties file
❷ Configures the token store
❸ Creates an instance of JdbcTokenStore, providing access to the database through the data source configured in the application.properties file
We can now start our authorization server and issue tokens. We issue tokens in the same way we did in chapter 13 and earlier in this chapter. From this perspective, nothing’s changed. But now, we can see our tokens stored in the database as well. The next code snippet shows the cURL command you use to issue a token:
curl -v -XPOST -u client:secret "http://localhost:8080/oauth/token?grant_type=password&username=john&password=12345&scope=read"
The response body is
{
  "access_token":"009549ee-fd3e-40b0-a56c-6d28836c4384",
  "token_type":"bearer",
  "refresh_token":"fd44d772-18b3-4668-9981-86373017e12d",
  "expires_in":43199,
  "scope":"read"
}
The access token returned in the response can also be found as a record in the oauth_access_token table. Because I configure the refresh token grant type, I receive a refresh token. For this reason, I also find a record for the refresh token in the oauth_refresh_token table. Because the database persists tokens, the resource server can validate the issued tokens even if the authorization server is down or after its restart.
It’s time now to configure the resource server so that it also uses the same database. For this purpose, I work in the project ssia-ch14-ex2-rs. I start with the implementation we worked on in section 14.1. As for the authorization server, we need to add the necessary dependencies in the pom.xml file. Because the resource server needs to connect to the database, we also need to add the spring-boot-starter-jdbc dependency and the JDBC driver. The next code snippet shows the dependencies in the pom.xml file:
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
</dependency>
In the application.properties file, I configure the data source so the resource server can connect to the same database as the authorization server. The next code snippet shows the content of the application.properties file for the resource server:
server.port=9090

spring.datasource.url=jdbc:mysql://localhost/spring
spring.datasource.username=root
spring.datasource.password=
In the configuration class of the resource server, we inject the data source and configure JdbcTokenStore. The following listing shows the changes to the resource server’s configuration class.
Listing 14.7 The configuration class for the resource server
@Configuration
@EnableResourceServer
public class ResourceServerConfig 
  extends ResourceServerConfigurerAdapter {

  @Autowired
  private DataSource dataSource;                ❶

  @Override
  public void configure(
    ResourceServerSecurityConfigurer resources) {
    
    resources.tokenStore(tokenStore());         ❷
  }

  @Bean
  public TokenStore tokenStore() {
    return new JdbcTokenStore(dataSource);      ❸
  }
}
❶ Injects the data source we configured in the application.properties file
❷ Configures the token store
❸ Creates a JdbcTokenStore based on the injected data source
You can now start your resource server as well and call the /hello endpoint with the access token you previously issued. The next code snippet shows you how to call the endpoint using cURL:
curl -H "Authorization:Bearer 009549ee-fd3e-40b0-a56c-6d28836c4384" "http://localhost:9090/hello"
The response body is
Hello!
Fantastic! In this section, we implemented a blackboarding approach for communication between the resource server and the authorization server. We used an implementation of TokenStore called JdbcTokenStore. Now we can persist tokens in a data-base, and we can avoid direct calls between the resource server and the authorization server for validating tokens. But having both the authorization server and the resource server depend on the same database presents a disadvantage. In the case of a large number of requests, this dependency might become a bottleneck and slow down the system. To avoid using a shared database, do we have another implementation option? Yes; in chapter 15, we’ll discuss the alternative to the approaches presented in this chapter--using signed tokens with JWT.
NOTE Writing the configuration of the resource server without Spring Security OAuth makes it impossible to use the blackboarding approach.
14.4 A short comparison of approaches
In this chapter, you learned to implement two approaches for allowing the resource server to validate tokens it receives from the client:
- Directly calling the authorization server. When the resource server needs to validate a token, it directly calls the authorization server that issues that token.
- Using a shared database (blackboarding). Both the authorization server and the resource server work with the same database. The authorization server stores the issued tokens in the database, and the resource server reads those for validation.
Let’s briefly sum this up. In table 14.1, you find the advantages and disadvantages of the two approaches discussed in this chapter.
Table 14.1 Advantages and disadvantages of implementing the presented approaches for the resource server to validate tokens
Approach	Advantages	Disadvantages
Directly calling the authorization server	Easy to implement.
It can be applied to any token implementation.	It implies direct dependency between the authorization server and the resource server.
It might cause unnecessary stress on the authorization server.
Using a shared database (blackboarding)	Eliminates the need for direct communication between the authorization server and the resource server.
It can be applied to any token implementation.
Persisting tokens allows authorization to work after an authorization server restart or if the authorization server is down.	It’s more difficult to implement than directly calling the authorization server.
Requires one more component in the system, the shared database.
The shared database can become a bottleneck and affect system performance.
Summary
- The resource server is a Spring component that manages user resources.
- The resource server needs a way to validate tokens issued to the client by the authorization server.
- One option for verifying tokens for the resource server is to call the authorization server directly. This approach can cause too much stress on the authorization server. I generally avoid using this approach.
- So that the resource server can validate tokens, we can choose to implement a blackboarding architecture. In this implementation, the authorization server and the resource server access the same database where they manage tokens.
- Blackboarding has the advantage of eliminating direct dependencies between the resource server and the authorization server. But it implies adding a database to persist tokens, which could become a bottleneck and affect system performance in the case of a large number of requests.
- To implement token management, we need to use an object of type TokenStore. We can write our own implementation of TokenStore, but in most cases, we use an implementation provided by Spring Security.
- JdbcTokenStore is a TokenStore implementation that you can use to persist the access and refresh tokens in a database.
- Copy
- Add Highlight
- Add Note
 