# 5. Edge Security with an API Gateway

The API gateway is the most common pattern in securing APIs in a production deployment. In other words, it’s the entry point to your API deployment. There are many open source and proprietary products out there, which implement the API gateway pattern, which we commonly identify as API gateways. An API gateway is a policy enforcement point (PEP), which centrally enforces authentication, authorization, and throttling policies. Further we can use an API gateway to centrally gather all the analytics related to APIs and publish those to an analytics product for further analysis and presentation.

## Setting Up Zuul API Gateway

`Zuul` is an API gateway (see Figure 5-1) that provides dynamic routing, monitoring, resiliency, security, and more. It is acting as the front door to Netflix’s server infrastructure, handling traffic from all Netflix users around the world. It also routes requests, supports developers’ testing and debugging, provides deep insight into Netflix’s overall service health, protects the Netflix deployment from attacks, and channels traffic to other cloud regions when an AWS region is in trouble. In this section, we are going to set up Zuul as an API gateway to front the Order API, which we developed in Chapter 3.

All the samples used in this book are available in the `https://github.com/apisecurity/samples.git` git repository. Use the following git command to clone it. All the samples related to this chapter are inside the directory ch05. To run the samples in the book, we assumed you have installed Java (JDK 1.8+) and Apache Maven 3.2.0+.

```
\> git clone https://github.com/apisecurity/samples.git
\> cd samples/ch05
```

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_5_Fig1_HTML.jpg)

Figure 5-1 A typical Zuul API gateway deployment at Netflix. All the Netflix microservices are fronted by an API gateway

### Running the Order API

This is the simplest API implementation ever, which is developed with Java Spring Boot. In fact one can call it as a microservice as well. You can find the code inside the directory, `ch05/sample01`. To build the project with Maven, use the following command from the sample01 directory:
```
\> cd sample01
\> mvn clean install
```
Now, let’s see how to run our Spring Boot service and talk to it with a cURL client. Execute the following command from `ch05/sample01` directory to start the Spring Boot service with Maven.
```
\> mvn spring-boot:run
```
To test the API with a cURL client, use the following command from a different command console. It will print the output as shown in the following, after the initial command.
```
\> curl http://localhost:8080/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```
### Running the Zuul API Gateway

In this section, we are going to build the Zuul API gateway as a Spring Boot project and run it against the Order service. Or in other words, the Zuul gateway will proxy all the requests to the Order service. You can find the code inside `ch05/sample02` directory. To build the project with Maven, use the following commands:
```
\> cd sample02
\> mvn clean install
```
Before we delve deep into the code, let’s have a look at some of the notable Maven dependencies and plugins added into ch05/sample02/pom.xml. Spring Boot comes with different starter dependencies to integrate with different Spring modules. The `spring-cloud-starter-zuul` dependency (as shown in the following) brings in Zuul API gateway dependencies and does all the wiring between the components, making the developer’s work to a minimum.
```
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-zuul</artifactId>
</dependency>
```
It is important to have a look at the class file `ch05/sample02/GatewayApplication.java`. This is the class which spins up the Zuul API gateway. By default it starts on port 8080, and you can change the port by adding, say, for example, `server.port=9000` to the `application.properties` file. This will set the API gateway port to 9000. The following shows the code snippet from `GatewayApplication` class, which spins up the API gateway. The `@EnableZuulProxy` annotation instructs the Spring framework to start the Spring application as a Zuul proxy.
```
@EnableZuulProxy
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```
Now, let’s see how to start the API gateway and talk to it with a cURL client. The following command executed from `ch05/sample02` directory shows how to start the API gateway with Maven. Since the `Zuul` API gateway is also another Spring Boot application, the way you start it is the same as how we did before with the Order service.
```
\> mvn spring-boot:run
```
To test the Order API, which is now proxied through the Zuul API gateway, let’s use the following cURL. It will print the output as shown in the following. Also make sure that the Order service is still up and running on port 8080. Here we add a new context called retail (which we didn’t see in the direct API call) and talk to the port 9090, where the API gateway is running.
```
\> curl http://localhost:9090/retail/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```

### What Happens Underneath?

When the API gateway receives a request to the retail context, it routes the request to the back-end API. These routing instructions are set in the `application.properties` file, as shown in the following. If you want to use some other context, instead of retail, then you need to change the property key appropriately.
```
zuul.routes.retail.url=http://localhost:8080
```

## Enabling TLS for the Zuul API Gateway

In the previous section, the communication between the cURL client and the Zuul API gateway happened over HTTP, which is not secure. In this section, let’s see how to enable TLS at the `Zuul` API gateway. In Chapter 3, we discussed how to secure the Order service with TLS. There the Order service is a Java Spring Boot application, and we follow the same process here to secure the `Zuul` API gateway with TLS, as Zuul is also another Java Spring Boot application.

To enable TLS, first we need to create a public/private key pair. The following command uses keytool that comes with the default Java distribution to generate a key pair and stores it in keystore.jks file. If you are to use the `keystore.jks` file as it is, which is inside sample02 directory, you can possibly skip this step. Chapter 3 explains in detail what each parameter in the following command means.
```
\> keytool -genkey -alias spring -keyalg RSA -keysize 4096 -validity 3650 -dname "CN=zool,OU=bar,O=zee,L=sjc,S=ca,C=us" -keypass springboot -keystore keystore.jks -storeType jks -storepass springboot
```
To enable TLS for the Zuul API gateway, copy the keystore file (`keystore.jks`), which we created earlier, to the home directory of the gateway (e.g., `ch05/sample02/`) and add the following to the `application.properties` file. The samples that you download from the samples git repository already have these values (and you only need to uncomment them), and we are using springboot as the password for both the keystore and the private key.
```
server.ssl.key-store: keystore.jks
server.ssl.key-store-password: springboot
server.ssl.keyAlias: spring
```
To validate that everything works fine, use the following command from ch05/sample02/ directory to spin up the Zuul API gateway and notice the line, which prints the HTTPS port. If you already have the Zuul gateway running from the previous exercise, please shut it down first.
```
\> mvn spring-boot:run

Tomcat started on port(s): 9090 (https) with context path "
```
Assuming you already have the Order service still running from the previous section, run the following cURL command to access the Order service via the Zuul gateway, over HTTPS.
```
\> curl –k https://localhost:9090/retail/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```
We used the `-k` option in the preceding `cURL` command. Since we have self-signed (untrusted) certificates to secure our HTTPS endpoint, we need to pass the `–k` parameter to advise cURL to ignore the trust validation. In a production deployment with proper certificate authority–signed certificates, you do not need to do that. Also, if you have self-signed certificates, you can still avoid using `–k`, by pointing cURL to the corresponding public certificate.
```
\> curl --cacert ca.crt https://localhost:9090/retail/order/11
```
You can use the following keytool command from `ch05/sample02/` to export the public certificate of the Zuul gateway to `ca.crt` file in PEM (with the `-rfc` argument) format.
```
\> keytool -export -file ca.crt -alias spring –rfc -keystore keystore.jks -storePass springboot
```
The preceding command will result in the following error. This complains that the common name in certificate, which is zool, does not match with the hostname (`localhost`) in the cURL command.
```
curl: (51) SSL: certificate subject name 'zool' does not match target host name 'localhost'
```
Ideally, in a production deployment when you create a certificate, its common name should match the hostname. In this case, since we do not have DNS entry for the `zool` hostname, you can use the following workaround, with `cURL`.
```
\> curl --cacert ca.crt https://zool:9090/retail/order/11 --resolve zool:9090:127.0.0.1
```
## Enforcing OAuth 2.0 Token Validation at the Zuul API Gateway

In the previous section, we explained how to proxy requests to an API, via the Zuul API gateway. There we didn’t worry about enforcing security. In this section, we will discuss how to enforce OAuth 2.0 token validation at the `Zuul` API gateway. There are two parts in doing that. First we need to have an OAuth 2.0 authorization server (also we can call it a security token service) to issue tokens, and then we need to enforce OAuth token validation at the `Zuul` API gateway (see Figure 5-2).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_5_Fig2_HTML.jpg)

Figure 5-2 The Zuul API gateway intercepts all the requests going to the `Order` API and validates OAuth 2.0 access tokens against the authorization server (STS)

### Setting Up an OAuth 2.0 Security Token Service (STS)

The responsibility of the security token service (STS) is to issue tokens to its clients and respond to the validation requests from the API gateway. There are many open source OAuth 2.0 authorization servers out there: WSO2 Identity Server, Keycloak, Gluu, and many more. In a production deployment, you may use one of them, but for this example, we are setting up a simple OAuth 2.0 authorization server with Spring Boot. It is another microservice and quite useful in developer testing. The code corresponding to the authorization server is under ch05/sample03 directory.

Let’s have a look at `ch05/sample03/pom.xml` for notable Maven dependencies. These dependencies introduce a new set of annotations (`@EnableAuthorizationServer` annotation and `@EnableResourceServer` annotation), to turn a Spring Boot application to an OAuth 2.0 authorization server.
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
The class `ch05/sample03/TokenServiceApp.java` carries the `@EnableAuthorizationServer` annotation, which turns the project into an OAuth 2.0 authorization server. We’ve added `@EnableResourceServer` annotation to the same class, as it also has to act as a resource server, to validate access tokens and return back the user information. It’s understandable that the terminology here is a little confusing, but that’s the easiest way to implement the token validation endpoint (in fact the user info endpoint, which also indirectly does the token validation) in Spring Boot. When you use self-contained JWTs, this token validation endpoint is not required. If you are new to JWT, please check Chapter 7 for details.

The registration of clients with the Spring Boot authorization server can be done in multiple ways. This example registers clients in the code itself, in `ch05/sample03/config/AuthorizationServerConfig.java` file. The `AuthorizationServerConfig` class extends the `AuthorizationServerConfigurerAdapter` class to override its default behavior. Here we set the value of client id to `10101010`, client secret to `11110000`, available scope values to `foo` and/or `bar`, authorized grant types to `client_credentials`, password, and `refresh_token`, and the validity period of an access token to `6000` seconds. Most of the terms we use here are from OAuth 2.0 and explained in Chapter 4 .
```
@Override
public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
      clients.inMemory().withClient("10101010")
        .secret("11110000").scopes("foo", "bar")
        .authorizedGrantTypes("client_credentials", "password",
                            "refresh_token")
        .accessTokenValiditySeconds(6000);
}
```
To support password grant type, the authorization server has to connect to a user store. A user store can be a database or an LDAP server, which stores user credentials and attributes. Spring Boot supports integration with multiple user stores, but once again, the most convenient one, which is just good enough for this example, is an in-memory user store. The following code from sample03/src/main/java/com/apress/ch05/sample03/config/WebSecurityConfiguration.java file adds a user to the system, with the role USER.
```
@Override

public void configure(AuthenticationManagerBuilder auth) throws

Exception {

    auth.inMemoryAuthentication()

          .withUser("peter").password("peter123").roles("USER");

}
```
Once we define the in-memory user store in Spring Boot, we also need to engage that with the OAuth 2.0 authorization flow, as shown in the following, in the code sample03/src/main/java/com/apress/ch05/sample03/config/AuthorizationServerConfig.java.
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

To get an access token using the OAuth 2.0 client credentials grant type, use the following command. Make sure to replace the values of $CLIENTID and $CLIENTSECRET appropriately. The hard-coded values for client id and client secret used in our example are 10101010 and 11110000, respectively. Also you might have noticed already, the STS endpoint is protected with Transport Layer Security (TLS). To protect STS with TLS, we followed the same process we did before while protecting the Zuul API gateway with TLS.

\> curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=client_credentials&scope=foo" https://localhost:8443/oauth/token

{"access_token":"81aad8c4-b021-4742-93a9-e25920587c94","token_type":"bearer","expires_in":43199,"scope":"foo"}

> **Note**
>
> We use the –k option in the preceding cURL command. Since we have self-signed (untrusted) certificates to secure our HTTPS endpoint, we need to pass the –k parameter to advise cURL to ignore the trust validation. You can find more details regarding the parameters used here from the OAuth 2.0 6749 RFC: https://tools.ietf.org/html/rfc6749 and also explained in Chapter 4.

To get an access token using the password OAuth 2.0 grant type, use the following command. Make sure to replace the values of $CLIENTID, $CLIENTSECRET, $USERNAME, and $PASSWORD appropriately. The hard-coded values for client id and client secret used in our example are 10101010 and 11110000, respectively; and for username and password, we use peter and peter123, respectively.

```
\> curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=password&username=$USERNAME&password=$PASSWORD&scope=foo" https://localhost:8443/oauth/token

{"access_token":"69ff86a8-eaa2-4490-adda-6ce0f10b9f8b","token_type":"bearer","refresh_token":"ab3c797b-72e2-4a9a-a1c5-c550b2775f93","expires_in":43199,"scope":"foo"}
```
> **Note**
>
> If you carefully observe the two responses we got for the OAuth 2.0 client credentials grant type and the password grant type, you might have noticed that there is no refresh token in the client credentials grant type flow. In OAuth 2.0, the refresh token is used to obtain a new access token, when the access token has expired or is closer to expire. This is quite useful, when the user is offline and the client application has no access to his/her credentials to get a new access token and the only way is to use a refresh token. For the client credentials grant type, there is no user involved, and it always has access to its own credentials, so can be used any time it wants to get a new access token. Hence, a refresh token is not required.

Now let’s see how to validate an access token, by talking to the authorization server. The resource server usually does this. An interceptor running on the resource server intercepts the request, extracts out the access token, and then talks to the authorization server. In a typical API deployment, this validation happens over a standard endpoint exposed by the OAuth authorization server. This is called the introspection endpoint, and in Chapter 9, we discuss OAuth token introspection in detail. However, in this example, we have not implemented the standard introspection endpoint at the authorization server (or the STS), but rather use a custom endpoint for token validation.

The following command shows how to directly talk to the authorization server to validate the access token obtained in the previous command. Make sure to replace the value of $TOKEN with the corresponding access token appropriately.
```
\> curl -k -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json"   https://localhost:8443/user

{"details":{"remoteAddress":"0:0:0:0:0:0:0:1","sessionId":null,"tokenValue":"9f3319a1-c6c4-4487-ac3b-51e9e479b4ff","tokenType":"Bearer","decodedDetails":null},"authorities":[],"authenticated":true,"userAuthentication":null,"credentials":"","oauth2Request":{"clientId":"10101010","scope":["bar"],"requestParameters":{"grant_type":"client_credentials","scope":"bar"},"resourceIds":[],"authorities":[],"approved":true,"refresh":false,"redirectUri":null,"responseTypes":[],"extensions":{},"grantType":"client_credentials","refreshTokenRequest":null},"clientOnly":true,"principal":"10101010","name":"10101010"}
```
The preceding command returns back the metadata associated with the access token, if the token is valid. The response is built inside the `user()` method of `ch05/sample03/TokenServiceApp.java` class, as shown in the following code snippet. With the `@RequestMapping` annotation, we map the `/user` context (from the request) to the user() method.
```
@RequestMapping("/user")
user(Principal user) {
      return user;
}
```
> **Note**
>
> By default, with no extensions, Spring Boot stores issued tokens in memory. If you restart the server after issuing a token, and then validate it, it will result in an error response.

### Setting Up Zuul API Gateway for OAuth 2.0 Token Validation

To enforce token validation at the API gateway, we need to uncomment the following property in sample02/src/main/resources/application.properties file, as shown in the following. The value of the security.oauth2.resource.user-info-uri property carries the endpoint of the OAuth 2.0 security token service, which is used to validate tokens.
```
security.oauth2.resource.user-info-uri=https://localhost:8443/user
```
The preceding property points to an HTTPs endpoint on the authorization server. To support the HTTPS connection between the `Zuul` gateway and the authorization server, there is one more change we need to do at the `Zuul` gateway end. When we have a TLS connection between the Zuul gateway and the authorization server, the Zuul gateway has to trust the certificate authority associated with the public certificate of the authorization server. Since we are using self-signed certificate, we need to export authorization server’s public certificate and import it to `Zuul` gateway’s keystore. Let’s use the following keytool command from ch05/sample03 directory to export authorization server’s public certificate and copy it to ch05/sample02 directory. If you are using keystores from the samples git repo, then you may skip the following two keytool commands.
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
We also need to uncomment the following two dependencies in the sample02/pom.xml file. These dependencies do the autowiring between Spring Boot components to enforce OAuth 2.0 token validation at the Zuul gateway.
```
<dependency>
 <groupId>org.springframework.security</groupId>
 <artifactId>spring-security-jwt</artifactId>
</dependency>
<dependency>
 <groupId>org.springframework.security.oauth</groupId>
 <artifactId>spring-security-oauth2</artifactId>
</dependency>
```
Finally, we need to uncomment the @EnableResourceServer annotation and the corresponding package import on the GatewayApplication (`ch05/sample02/GatewayApplication.java`) class.

Let’s run the following command from the ch05/sample02 directory to start the Zuul API gateway. In case it is running already, you need to stop it first. Also, please make sure sample01 (Order service) and sample03 (STS) are still up and running.
```
\> mvn spring-boot:run
```
To test the API, which is now proxied through the Zuul API gateway and secured with OAuth 2.0, let’s use the following cURL. It should fail, because we do not pass an OAuth 2.0 token.
```
\> curl –k https://localhost:9090/retail/order/11
```
Now let’s see how to invoke the API properly with a valid access token. First we need to talk to the security token service and get an access token. Make sure to replace the values of $CLIENTID, $CLIENTSECRET, $USERNAME, and $PASSWORD appropriately in the following command. The hard-coded values for client id and client secret used in our example are 10101010 and 11110000, respectively; and for username and password, we used peter and peter123, respectively.
```
\> curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=password&username=$USERNAME&password=$PASSWORD&scope=foo" https://localhost:8443/oauth/token

{"access_token":"69ff86a8-eaa2-4490-adda-6ce0f10b9f8b","token_type":"bearer","refresh_token":"ab3c797b-72e2-4a9a-a1c5-c550b2775f93","expires_in":43199,"scope":"foo"}
```
Now let’s use the access token from the preceding response to invoke the Order API. Make sure to replace the value of $TOKEN with the corresponding access token appropriately.
```
\> curl -k -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json"   https://localhost:9090/retail/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items": [{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```

## Enabling Mutual TLS Between Zuul API Gateway and Order Service

So far in this chapter, we have protected the communication between the cURL client and STS, cURL client and Zuul API gateway, and Zuul API gateway and STS over TLS. Still we have a weak link in our deployment (see Figure 5-3). The communication between the Zuul gateway and Order service is neither protected with TLS nor authentication. In other words, if someone can bypass the gateway, they can reach the Order server with no authentication. To fix this, we need to secure the communication between the gateway and the Order service over mutual TLS. Then, no other request can reach the Order service without going through the gateway. Or in other words, the Order service only accepts requests generated from the gateway.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_5_Fig3_HTML.jpg)

Figure 5-3 The Zuul API gateway intercepts all the requests going to the Order API and validates OAuth 2.0 access tokens against the authorization server (STS)

To enable mutual TLS between the gateway and the Order service, first we need to create a public/private key pair. The following command uses keytool that comes with the default Java distribution to generate a key pair and stores it in keystore.jks file. Chapter 3 explains in detail what each parameter in the following command means. If you are using keystores from the samples git repo, then you may skip the following keytool commands.
```
\> keytool -genkey -alias spring -keyalg RSA -keysize 4096 -validity 3650 -dname "CN=order,OU=bar,O=zee,L=sjc,S=ca,C=us" -keypass springboot -keystore keystore.jks -storeType jks -storepass springboot
```
To enable mutual TLS for the Order service, copy the keystore file (keystore.jks), which we created earlier, to the home directory of the Order service (e.g., ch05/sample01/) and add the following to the [SAMPLE_HOME]/src/main/resources/application.properties file. The samples that you download from the samples git repository already have these values (and you only need to uncomment them), and we are using springboot as the password for both the keystore and the private key. The server.ssl.client-auth parameter is used to enforce mutual TLS at the Order service.
```
server.ssl.key-store: keystore.jks
server.ssl.key-store-password: springboot
server.ssl.keyAlias: spring
server.ssl.client-auth:need
```
There are two more changes we need to do at the Order service end. When we enforce mutual TLS at the Order service, the Zuul gateway (which acts as a client to the Order service) has to authenticate itself with an X.509 certificate—and the Order service must trust the certificate authority associated with Zuul gateway’s X.509 certificate. Since we are using self-signed certificate, we need to export Zuul gateway’s public certificate and import it to the Order service’s keystore. Let’s use the following keytool command from ch05/sample02 directory to export Zuul gateway’s public certificate and copy it to ch05/sample01 directory.
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
Finally, when we have a TLS connection between the Zuul gateway and the Order service, the Zuul gateway has to trust the certificate authority associated with the public certificate of the Order service. Even though we do not enable mutual TLS between these two parties, we still need to satisfy this requirement to enable just TLS. Since we are using self-signed certificate, we need to export Order service’s public certificate and import it to Zuul gateway’s keystore. Let’s use the following keytool command from ch05/sample01 directory to export Order service’s public certificate and copy it to ch05/sample02 directory.
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
To validate that TLS works fine with the Order service, use the following command from ch05/sample01/ directory to spin up the Order service and notice the line, which prints the HTTPS port. If you already have the Order service running from the previous exercise, please shut it down first.
```
\> mvn spring-boot:run
Tomcat started on port(s): 8080 (https) with context path "
```
Since we updated the Order service endpoint to use HTTPS instead of HTTP, we also need to update the Zuul gateway to use the new HTTPS endpoint. These routing instructions are set in the ch05/sample02/src/main/resources/application.properties file, as shown in the following. Just update it to use HTTPS instead of HTTP. Also we need to uncomment the zuul.sslHostnameValidationEnabled property in the same file and set it to false. This is to ask Spring Boot to ignore hostname verification. Or in other words, now Spring Boot won’t check whether the hostname of the Order service matches the common name of the corresponding public certificate.
```
zuul.routes.retail.url=https://localhost:8080
zuul.sslHostnameValidationEnabled=false
```
Restart the Zuul gateway with the following command from ch05/sample02.
```
\> mvn spring-boot:run
```
Assuming you have authorization server up and running, on HTTPS port 8443, run the following command to test the end-to-end flow. First we need to talk to the security token service and get an access token. Make sure to replace the values of $CLIENTID, $CLIENTSECRET, $USERNAME, and $PASSWORD appropriately in the following command. The hard-coded values for client id and client secret used in our example are 10101010 and 11110000, respectively; and for username and password, we used peter and peter123, respectively.
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

An OAuth 2.0 bearer token can be a reference token or self-contained token. A reference token is an arbitrary string. An attacker can carry out a brute-force attack to guess the token. The authorization server must pick the right length and use other possible measures to prevent brute forcing. A self-contained access token is a JSON Web Token (JWT), which we discuss in Chapter 7. When the resource server gets an access token, which is a reference token, then to validate the token, it has to talk to the authorization server (or the token issuer). When the access token is a JWT, the resource server can validate the token by itself, by verifying the signature of the JWT. In this section, we discuss how to obtain a JWT access token from the authorization server and use it to access the Order service through the Zuul API gateway.

### Setting Up an Authorization Server to Issue JWT

In this section, we’ll see how to extend the authorization server we used in the previous section (ch05/sample03/) to support self-contained access tokens or JWTs. The first step is to create a new key pair along with a keystore. This key is used to sign the JWTs issued from our authorization server. The following keytool command will create a new keystore with a key pair.
```
\> keytool -genkey -alias jwtkey -keyalg RSA -keysize 2048 -dname "CN=localhost" -keypass springboot -keystore jwt.jks -storepass springboot
```
The preceding command creates a keystore with the name jwt.jks, protected with the password springboot. We need to copy this keystore to sample03/src/main/resources/. Now to generate self-contained access tokens, we need to set the values of the following properties in sample03/src/main/resources/application.properties file.
```
spring.security.oauth.jwt: true
spring.security.oauth.jwt.keystore.password: springboot
spring.security.oauth.jwt.keystore.alias: jwtkey
spring.security.oauth.jwt.keystore.name: jwt.jks
```
The value of spring.security.oauth.jwt is set to false by default, and it has to be changed to true to issue JWTs. The other three properties are self-explanatory, and you need to set them appropriately based on the values you used in creating the keystore.

Let’s go through the notable changes in the source code to support JWTs. First, in the pom.xml, we need to add the following dependency, which takes care of building JWTs.
```
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-jwt</artifactId>
</dependency>
```
In sample03/src/main/java/com/apress/ch05/sample03/config/AuthorizationServerConfig.java class, we have added the following method, which takes care of injecting the details about how to retrieve the private key from the jwt.jks keystore, which we created earlier. This private key is used to sign the JWT.
```
@Bean
protected JwtAccessTokenConverter jwtConeverter() {
     String pwd = environment.getProperty("spring.security.oauth.jwt.keystore.password");
     String alias = environment.getProperty("spring.security.oauth.jwt.keystore.alias");
     String keystore = environment.getProperty("spring.security.oauth.jwt.keystore.name");
     String path = System.getProperty("user.dir");
      KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
             new FileSystemResource(new File(path + File.separator + keystore)), pwd.toCharArray());
     JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
     converter.setKeyPair(keyStoreKeyFactory.getKeyPair(alias));
     return converter;
}
```
In the same class file, we also set JwtTokenStore as the token store. The following function does it in a way, we only set the JwtTokenStore as the token store only if spring.security.oauth.jwt property is set to true in the application.properties file.
```
@Bean
public TokenStore tokenStore() {
   String useJwt = environment.getProperty("spring.security.oauth.jwt");
   if (useJwt != null && "true".equalsIgnoreCase(useJwt.trim())) {
       return new JwtTokenStore(jwtConeverter());
    } else {
       return new InMemoryTokenStore();
    }
}
```

Finally, we need to set the token store to AuthorizationServerEndpointsConfigurer, which is done in the following method, and once again, only if we want to use JWTs.

```
@Autowired
private AuthenticationManager authenticationManager;
@Override
public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
  String useJwt = environment.getProperty("spring.security.oauth.jwt");
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
To get an access token using the OAuth 2.0 client credentials grant type, use the following command. Make sure to replace the values of $CLIENTID and $CLIENTSECRET appropriately. The hard-coded values for client id and client secret used in our example are 10101010 and 11110000, respectively.
```
\> curl -v -X POST --basic -u $CLIENTID:$CLIENTSECRET -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -k -d "grant_type=client_credentials&scope=foo" https://localhost:8443/oauth/token
```
The preceding command will return back a base64-url-encoded JWT, and the following shows the decoded version.
```
{ "alg": "RS256", "typ": "JWT" }

{ "scope": [ "foo" ], "exp": 1524793284, "jti": "6e55840e-886c-46b2-bef7-1a14b813dd0a", "client_id": "10101010" }
```
Only the decoded header and the payload are shown in the output, skipping the signature (which is the third part of the JWT). Since we used client_credentials grant type, the JWT does not include a subject or username. It also includes the scope value(s) associated with the token.

### Protecting Zuul API Gateway with JWT

In this section, we’ll see how to enforce self-issued access token or JWT-based token validation at the Zuul API gateway. We only need to comment out security.oauth2.resource.user-info-uri property and uncomment security.oauth2.resource.jwt.keyUri property in sample02/src/main/resources/application.properties file. The updated application.properties file will look like the following.
```
#security.oauth2.resource.user-info-uri:https://localhost:8443/user
security.oauth2.resource.jwt.keyUri: https://localhost:8443/oauth/token_key
```
Here the value of security.oauth2.resource.jwt.keyUri points to the public key corresponding to the private key, which is used to sign the JWT by the authorization server. It’s an endpoint hosted under the authorization server. If you just type https://localhost:8443/oauth/token_key on the browser, you will find the public key, as shown in the following. This is the key the API gateway uses to verify the signature of the JWT included in the request.
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
Once we have a JWT access token obtained from the OAuth 2.0 authorization server, in the same way as we did before, with the following cURL command, we can access the protected resource. Make sure the value of $TOKEN is replaced appropriately with a valid JWT access token.
```
\> curl -k -H "Authorization: Bearer $TOKEN" https://localhost:9443/order/11

{"customer_id":"101021","order_id":"11","payment_method":{"card_type":"VISA","expiration":"01/22","name":"John Doe","billing_address":"201, 1st Street, San Jose, CA"},"items":[{"code":"101","qty":1},{"code":"103","qty":5}],"shipping_address":"201, 1st Street, San Jose, CA"}
```
## he Role of a Web Application Firewall (WAF)

As we discussed before, an API gateway is a policy enforcement point (PEP), which centrally enforces authentication, authorization, and throttling policies. In a public-facing API deployment, an API gateway is not just sufficient. We also need a web application firewall (WAF) sitting in front of the API gateway (see Figure 5-4). The primary role of a WAF is to protect your API deployment from distributed denial of service (DDoS) attacks—do threat detection and message validation against OpenAPI Specification (OAS) along with known threats identified by Open Web Application Security Project (OWASP). Gartner (one of the leading analyst firms) predicts that by 2020, more than 50% of public-facing web applications will be protected by cloud-based WAF service platforms such Akamai, Imperva, Cloudflare, Amazon Web Services, and so on, up from less than 20% in December 2018.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_5_Fig4_HTML.jpg)

Figure 5-4 A web application firewall (WAF) intercepts all the traffic coming into an API deployment

## Summary

- OAuth 2.0 is the de facto standard for securing APIs.

- The API gateway is the most common pattern in securing APIs in a production deployment. In other words, it’s the entry point to your API deployment.

- There are many open source and proprietary products out there, which implement the API gateway pattern, which we commonly identify as API gateways.

- An OAuth 2.0 bearer token can be a reference token or self-contained token. A reference token is an arbitrary string. An attacker can carry out a brute-force attack to guess the token. The authorization server must pick the right length and use other possible measures to prevent brute forcing.

- When the resource server gets an access token, which is a reference token, then to validate the token, it has to talk to the authorization server (or the token issuer). When the access token is a JWT, the resource server can validate the token by itself, by verifying the signature of the JWT.

- Zuul is an API gateway that provides dynamic routing, monitoring, resiliency, security, and more. It is acting as the front door to Netflix’s server infrastructure, handling traffic from all Netflix users around the world.

- In a public-facing API deployment, an API gateway is not just sufficient. We also need a web application firewall (WAF) sitting in front of the API gateway.

 