
# 14. OAuth 2.0 Security

OAuth 2.0 is an authorization framework, as you know already. Being a framework, it gives multiple options for application developers. It is up to the application developers to pick the right options based on their use cases and how they want to use OAuth 2.0. There are few guideline documents to help you use OAuth 2.0 in a secure way. OAuth 2.0 Threat Model and Security Considerations (RFC 6819) produced by OAuth IETF working group defines additional security considerations for OAuth 2.0, beyond those in the OAuth 2.0 specification, based on a comprehensive threat model. The OAuth 2.0 Security Best Current Practice document, which is a draft proposal at the time of writing, talks about new threats related to OAuth 2.0, since the RFC 6819 was published. Also, the Financial-grade API (FAPI) working group under the OpenID foundation has published a set of guidelines on how to use OAuth 2.0 in a secure way to build financial grade applications. In this chapter, we go through a set of possible attacks against OAuth 2.0 and discuss how to mitigate those.

## Identity Provider Mix-Up

Even though OAuth 2.0 is about access delegation, still people work around it to make it work for login. That’s how login with Facebook works. Then again, the OpenID Connect (see Chapter 6), which is built on top of OAuth 2.0, is the right way of using OAuth 2.0 for authentication. A recent research done by one of the leading vendors in the Identity and Access Management domain confirmed that most of the new development happened over the past few years at the enterprise level picked OAuth 2.0/OpenID Connect over SAML 2.0. All in all, OAuth 2.0 security is a hot topic. In 2016, Daniel Fett, Ralf Küsters, and Guido Schmitz did a research on OAuth 2.0 security and published a paper.1 Identity provider mix-up is one of the attacks highlighted in their paper. Identity provider is in fact the entity that issues OAuth 2.0 tokens or the OAuth 2.0 authorization server, which we discussed in Chapter 4.

Let’s try to understand how identity provider mix-up works (see Figure 14-1):

1. This attack happens with an OAuth 2.0 client application, which provides multiple identity provider (IdP) options for login. Let’s say foo.idp and evil.idp. We assume that the client application does not know that evil.idp is evil. Also it can be a case where evil.idp is a genuine identity provider, which could possibly be under an attack itself.


2. The victim picks foo.idp from the browser and the attacker intercepts the request and changes the selection to evil.idp. Here we assume the communication between the browser and the client application is not protected with Transport Layer Security (TLS). The OAuth 2.0 specification does not talk about it, and it’s purely up to the web application developers. Since there is no confidential data passed in this flow, most of the time the web application developers may not worry about using TLS. At the same time, there were few vulnerabilities discovered over the past on TLS implementations (mostly openssl). So, the attacker could possibly use such vulnerabilities to intercept the communication between the browser and the client application (web server), even if TLS is used.

 
Figure 14-1 Identity provider mix-up attack

3. Since the attacker changed the identity provider selection of the user, the client application thinks it’s evil.idp (even though the user picked foo.idp) and redirects the user to evil.idp. The client application only gets the modified request from the attacker, who intercepted the communication.


4. The attacker intercepts the redirection and modifies the redirection to go to the foo.idp. The way redirection works is the web server (in this case, the client application) sends back a response to the browser with a 302 status code—and with an HTTP Location header. If the communication between the browser and the client application is not on TLS, then this response is not protected, even if the HTTP Location header contains an HTTPS URL. Since we assumed already, the communication between the browser and the client application can be intercepted by the attacker, then the attacker can modify the Location header in the response to go to the foo.idp—which is the original selection—and no surprise to the user.

5. The client application gets either the code or the token (based on the grant type) and now will talk to the evil.idp to validate it. The authorization server (or the identity provider) will send back the authorization code (if the code grant type is used) to the callback URL, which is under the client application. Just looking at the authorization code, the client application cannot decide to which identity provider the code belongs to. So we assume it tracks the identity provider by some session variable—so as per step 3, the client application thinks it’s the evil.idp and talks to the evil.idp to validate the token.

6. The evil.idp gets hold of the user’s access token or the authorization code from the foo.idp. If it’s the implicit grant type, then it would be the access token, otherwise the authorization code. In mobile apps, most of the time, people used to embed the same client id and the client secret into all the instances—so an attacker having root access to his own phone can figure it out what the keys are and then, with the authorization code, can get the access token.

There is no record that the preceding attack is being carried out in practice—but at the same time, we cannot totally rule it out. There are a couple of options to prevent such attacks, and our recommendation is to use the option 1 as it is quite straightforward and solves the problem without much hassle.

1. Have separate callback URLs by each identity provider. With this the client application knows to which identity provider the response belongs to. The legitimate identity provider will always respect the callback URL associated with the client application and will use that. The client application will also attach the value of the callback URL to the browser session and, once the user got redirected back, will see whether it’s on the right place (or the right callback URL) by matching with the value of the callback URL from the browser session.

2. Follow the mitigation steps defined in the IETF draft specification: OAuth 2.0 IdP Mix-Up Mitigation (https://tools.ietf.org/html/draft-ietf-oauth-mix-up-mitigation-01). This specification proposes to send a set of mitigation data from the authorization server back to the client, along with the authorization response. The mitigation data provided by the authorization server to the client includes an issuer identifier, which is used to identify the authorization server, and a client id, which is used to verify that the response is from the correct authorization server and is intended for the given client. This way the OAuth 2.0 client can verify from which authorization server it got the response back and based on that identify the token endpoint or the endpoint to validate the token.


## Cross-Site Request Forgery (CSRF)

In general, Cross-Site Request Forgery (CSRF) attack forces a logged-in victim’s browser to send a forged HTTP request, including the victim’s session cookie and any other automatically included authentication information to a vulnerable web application. Such an attack allows the attacker to force a victim’s browser to generate requests, where the vulnerable application thinks are legitimate requests from the victim. OWASP (Open Web Application Security Project) identifies this as one of the key security risks in web applications in its 2017 report.2

Let’s see how CSRF can be used with OAuth 2.0 to exploit a vulnerable web application (see Figure 14-2):

1. The attacker tries to log in to the target web site (OAuth 2.0 client) with his account at the corresponding identity provider. Here we assume the attacker has a valid account at the identity provider, trusted by the corresponding OAuth 2.0 client application.

2. The attacker blocks the redirection to the target web site and captures the authorization code. The target web site never sees the code. In OAuth 2.0, the authorization code is only good enough for one-time use. In case the OAuth 2.0 client application sees it and then exchanges it to an access token, then it’s no more valid—so the attacker has to make sure that the authorization code never reaches the client application. Since the authorization code flows through the attacker’s browser to the client, it can be easily blocked.

3. The attacker constructs the callback URL for the target site—and makes the victim clicks on it. In fact, it would be the same callback URL the attacker can copy from step 2. Here the attacker can send the link to the victim’s email or somehow fool him to click on the link.

Figure 14-2 Cross-Site Request Forgery (CSRF) attack in the OAuth 2.0 code flow

4. The victim clicks on the link and logs in to the target web site, with the account attached to the attacker—and adds his/her credit card information. Since the authorization code belongs to the attacker, the victim logs in to the target web site with the attacker’s account. This is a pattern many web sites follow to authenticate users with OAuth 2.0. Login with Facebook works in the same way. Once the web site gets the authorization code, it will talk to the authorization server and exchanges it to an access token. Then using that access token, the web site talks to another endpoint in the authorization server to find user information. In this case, since the code belongs to the attacker, the user information returned back from the authorization server will be related to him—so the victim now logs in to the target web site with the attacker’s account.

5. The attacker too logs in to the target web site with his/her valid credentials and uses victim’s credit card to purchase goods.

The preceding attack can be mitigated by following these best practices:

- Use a short-lived authorization code. Making the authorization code expires soon gives very little time for the attacker to plant an attack. For example, the authorization code issued by LinkedIn expires in 30 seconds. Ideally, the lifetime of the authorization code should be in seconds.

- Use the state parameter as defined in the OAuth 2.0 specification. This is one of the key parameters to use to mitigate CSRF attacks in general. The client application has to generate a random number (or a string) and passes it to the authorization server along with the grant request. Further, the client application has to add the generated value of the state to the current user session (browser session) before redirecting the user to the authorization server. According to the OAuth 2.0 specification, the authorization server has to return back the same state value with the authorization code to the redirect_uri (to the client application). The client must validate the state value returned from the authorization server with the value stored in the user’s current session—if it mismatches, it rejects moving forward. Going back to the attack, when the user clicks the crafted link sent to the victim by the attacker, it won’t carry the same state value generated before and attached to the victim’s session (or most probably victim’s session has no state value), or the attacker does not know how to generate the exact same state value. So, the attack won’t be successful, and the client application will reject the request.

- Use PKCE (Proof Key for Code Exchange). PKCE (RFC 7636) was introduced to protect OAuth 2.0 client applications from the authorization code interception attack, mostly targeting native mobile apps. The use of PKCE will also protect users from CSRF attacks, once the code_verifier is attached to the user’s browser session. We talked about PKCE in detail in Chapter 10.

## Token Reuse

OAuth 2.0 tokens are issued by the authorization server to a client application to access a resource on behalf of the resource owner. This token is to be used by the client—and the resource server will make sure it’s a valid one. What if the resource server is under the control of an attacker and wants to reuse the token sent to it to access another resource, impersonating the original client? Here the basic assumption is there are multiple resource servers, which trust the same authorization server. For example, in a microservices deployment, there can be multiple microservices protected with OAuth 2.0, which trust the same authorization server.

How do we make sure at the resource server side that the provided token is only good enough to access it? One approach is to have properly scoped access tokens. The scopes are defined by the resource server—and update the authorization server. If we qualify each scope with a Uniform Resource Name (URN) specific to the corresponding resource server, then there cannot be any overlapping scopes across all the resource servers—and each resource server knows how to uniquely identify a scope corresponding to it. Before accepting a token, it should check whether the token is issued with a scope known to it.

This does not completely solve the problem. If the client decides to get a single access token (with all the scopes) to access all the resources, then still a malicious client can use that access token to access another resource by impersonating the original client. To overcome this, the client can first get an access token with all the scopes, then it can exchange the access token to get multiple access tokens with different scopes, following the OAuth 2.0 Token Exchange specification (which we discussed in Chapter 9). A given resource server will only see an access token having scopes only related to that particular resource server.

Let’s see another example of token reuse. Here assume that you log in to an OAuth 2.0 client application with Facebook. Now the client has an access token, which is good enough to access the user info endpoint (https://graph.facebook.com/me) of Facebook and find who the user is. This client application is under an attacker, and now the attacker tries to access another client application, which uses the implicit grant type, with the same access token, as shown in the following.

https://target-app/callback?access_token=<access_token>

The preceding URL will let the attacker log in to the client application as the original user unless the target client application has proper security checks in place. How do we overcome this?

There are multiple options:

- Avoid using OAuth 2.0 for authentication—instead use OpenID Connect. The ID token issued by the authorization server (via OpenID Connect) has an element called aud (audience)—and its value is the client id corresponding to the client application. Each application should make sure that the value of the aud is known to it before accepting the user. If the attacker tries to replay the ID token, it will not work since the audience validation will fail at the second client application (as the second application expects a different aud value).

- Facebook login is not using OpenID Connect—and the preceding attack can be carried out against a Facebook application which does not have the proper implementation. There are few options introduced by Facebook to overcome the preceding threat. One way is to use the undocumented API, https://graph.facebook.com/app?access_token=<access_token>, to get access token metadata. This will return back in a JSON message the details of the application which the corresponding access token is issued to. If it’s not yours, reject the request.

- Use the standard token introspection endpoint of the authorization server to find the token metadata. The response will have the client_id corresponding to the OAuth 2.0 application—and if it does not belong to you, reject the login request.

There is another flavor of token reuse—rather we call it token misuse. When implicit grant type is used with a single-page application (SPA) , the access token is visible to the end user—as it’s on the browser. It’s the legitimate user—so the user seeing the access token is no big deal. But the issue is the user would probably take the access token out of the browser (or the app) and automate or script some API calls, which would generate more load on the server that would not expect in a normal scenario. Also, there is a cost of making API calls. Most of the client applications are given a throttle limit—meaning a given application can only do n number of calls during a minute or some fixed time period. If one user tries to invoke APIs with a script, that could possibly eat out the complete throttle limit of the application—making an undesirable impact on the other users of the same application. To overcome such scenarios, the recommended approach is to introduce throttle limits by user by application—not just by the application. In that way, if a user wants to eat out his own throttle limit, go out and do it! The other solution is to use Token Binding, which we discussed in Chapter 11. With token binding, the access token is bound to the underlying Transport Layer Security (TLS) connection, and the user won’t be able to export it and use it from somewhere else.

## Token Leakage/Export

More than 90% of the OAuth 2.0 deployments are based on bearer tokens—not just the public/Internet scale ones but also at the enterprise level. The use of a bearer token is just like using cash. When you buy a cup of coffee from Starbucks, paying by cash, no one will bother how you got that ten-dollar note—or if you’re the real owner of it. OAuth 2.0 bearer tokens are similar to that. If someone takes the token out of the wire (just like stealing a ten-dollar note from your pocket), he/she can use it just as the original owner of it—no questions asked!

Whenever you use OAuth 2.0, it’s not just recommended but a must to use TLS. Even though TLS is used, still a man-in-the-middle attack can be carried out with various techniques. Most of the time, the vulnerabilities in TLS implementations are used to intercept the TLS-protected communication channels. The Logjam attack discovered in May 2015 allowed a man-in-the-middle attacker to downgrade vulnerable TLS connections to 512-bit export-grade cryptography. This allowed the attacker to read and modify any data passed over the connection.

There are few things we need to worry about as precautions to keep the attacker away from having access to the tokens:

- Always be on TLS (use TLS 1.2 or later).

- Address all the TLS-level vulnerabilities at the client, authorization server, and the resource server.

- The token value should be >=128 bits long and constructed from a cryptographically strong random or pseudorandom number sequence.

- Never store tokens in cleartext—but the salted hash.

- Never write access/refresh tokens into logs.

- Use TLS tunneling over TLS bridging.

- Decide the lifetime of each token based on the risk associated with token leakage, duration of the underlying access grant (SAML grant (RFC 7522) or JWT grant (RFC 7523)), and the time required for an attacker to guess or produce a valid token.

- Prevent reuse of the authorization code—just once.

- Use one-time access tokens. Under the OAuth 2.0 implicit grant type, access token comes as a URI fragment—which will be in the browser history. In such cases, it can be immediately invalidated by exchanging it to a new access token from the client application (which is an SPA).

- Use strong client credentials. Most of the applications just use client id and client secret to authenticate the client application to the authorization server. Rather than passing credentials over the wire, client can use either the SAML or JWT assertion to authenticate.

In addition to the preceding measures, we can also cryptographically bind the OAuth 2.0 access/refresh tokens and authorization codes to a given TLS channel—so those cannot be exported and used elsewhere. There are few specifications developed under the IETF Token Binding working group to address this aspect.

The Token Binding Protocol, which we discussed in Chapter 11, allows client/server applications to create long-lived, uniquely identifiable TLS bindings spanning multiple TLS sessions and connections. Applications are then enabled to cryptographically bind security tokens to the TLS layer, preventing token export and replay attacks. To protect privacy, the Token Binding identifiers are only conveyed over TLS and can be reset by the user at any time.

The OAuth 2.0 Token Binding specification (which we discussed in Chapter 11) defines how to apply Token Binding to access tokens, authorization codes, and refresh tokens. This cryptographically binds OAuth tokens to a client’s Token Binding key pair, the possession of which is proven on the TLS connections over which the tokens are intended to be used. The use of Token Binding protects OAuth tokens from man-in-the-middle, token export, and replay attacks.

## Open Redirector

An open redirector is an endpoint hosted on the resource server (or the OAuth 2.0 client application) end, which accepts a URL as a query parameter in a request—and then redirects the user to that URL. An attacker can modify the redirect_uri in the authorization grant request from the resource server to the authorization server to include an open redirector URL pointing to an endpoint owned by him. To do this, the attacker has to intercept the communication channel between the victim’s browser and the authorization server—or the victim’s browser and the resource server (see Figure 14-3).

Once the request hits the authorization server and after the authentication, the user will be redirected to the provided redirect_uri, which also carries the open redirector query parameter pointing to the attacker’s endpoint. To detect any modifications to the redirect_uri, the authorization server can carry out a check against a preregistered URL. But then again, some authorization server implementations will only worry about the domain part of the URL and will ignore doing an exact one-to-one match. So, any changes to the query parameters will be unnoticed.

 
Figure 14-3 Open Redirector attack

Once the user got redirected to the open redirector endpoint, it will again redirect the user to the value (URL) defined in the open redirector query parameter—which will take him/her to the attacker’s endpoint. In this request to the attacker’s endpoint, the HTTP Referer header could carry some confidential data, including the authorization code (which is sent to the client application by the authorization server as a query parameter).

How to prevent an open redirector attack:

- Enforce strict validations at the authorization server against the redirect_uri. It can be an exact one-to-one match or regex match.

- Validate the redirecting URL at open redirector and make sure you only redirect to the domains you own.

- Use JWT Secured Authorization Request (JAR) or Pushed Authorization Requests (PAR) as discussed in Chapter 4 to protect the integrity of the authorization request, so the attacker won’t be able to modify the request to include the open redirector query parameter to the redirect_uri.

## Code Interception Attack

Code interception attack could possibly happen in a native mobile app. OAuth 2.0 authorization requests from native apps should only be made through external user agents, primarily the user’s browser. The OAuth 2.0 for Native Apps specification (which we discussed in Chapter 10) explains in detail the security and usability reasons why this is the case and how native apps and authorization servers can implement this best practice.

The way you do single sign-on in a mobile environment is by spinning up the system browser from your app and then initiate OAuth 2.0 flow from there. Once the authorization code is returned back to the redirect_uri (from the authorization server) on the browser, there should be a way to pass it over to the native app. This is taken care by the mobile OS—and each app has to register for a URL scheme with the mobile OS. When the request comes to that particular URL, the mobile OS will pass its control to the corresponding native app. But, the danger here is, there can be multiple apps that get registered for the same URL scheme, and there is a chance a malicious app could get hold of the authorization code. Since many mobile apps embed the same client id and client secret for all the instances of that particular app, the attacker can also find out what they are. By knowing the client id and client secret, and then having access to the authorization code, the malicious app can now get an access token on behalf of the end user.

PKCE (Proof Key for Code Exchange), which we discussed in detail in Chapter 10, was introduced to mitigate such attacks. Let’s see how it works:

1. The OAuth 2.0 client app generates a random number (code_verifier) and finds the SHA256 hash of it—which is called the code_challenge.

2. The OAuth 2.0 client app sends the code_challenge along with the hashing method in the authorization grant request to the authorization server.

3. Authorization server records the code_challenge (against the issued authorization code) and replies back with the code.

4. The client sends the code_verifier along with the authorization code to the token endpoint.

5. The authorization server finds the hash of the provided code_verifier and matches it against the stored code_challenge. If it does not match, rejects the request.


With this approach, a malicious app just having access to the authorization code cannot exchange it to an access token without knowing the value of the code_verifier.

## Security Flaws in Implicit Grant Type

The OAuth 2.0 implicit grant type (see Figure 14-4) is now obsolete. This was mostly used by single-page applications and native mobile apps—but no more. In both the cases, the recommendation is to use the authorization code grant type. There are few security flaws, as listed in the following, identified in the implicit grant type, and the IETF OAuth working group officially announced that the applications should not use implicit grant type any more:

- With implicit grant type, the access token comes as a URI fragment and remains in the web browser location bar (step 5 in Figure 14-4). Since anything the web browser has in the location bar persevered as browser history, anyone having access to the browser history can steal the tokens.

- Since the access token remains in the web browser location bar, the API calls initiated from the corresponding web page will carry the entire URL in the location bar, along with the access token, in the HTTP Referer header. This will let external API endpoints to figure out (looking at the HTTP Referer header) what the access token is and possibly misuse it.

 
Figure 14-4 OAuth 2.0 implicit grant flow.

## Google Docs Phishing Attack

An attacker used a fake OAuth 2.0 app called Google Docs as a medium to launch a massive phishing attack targeting Google users in May 2017. The first target was the media companies and public relations (PR) agencies. They do have a large amount of contacts—and the attacker used the email addresses from their contact lists to spread the attack. It went viral for an hour—before the app was removed by Google.

Is this a flaw in the OAuth 2.0 protocol exploited by the attacker or a flaw in how Google implemented it? Is there something we could have done better to prevent such attacks?


Figure 14-5 OAuth 2.0 authorization grant flow.

Almost all the applications you see on the Web today use the authorization code grant flow in OAuth 2.0. The attacker exploited step 3 in Figure 14-5 by tricking the user with an application name (Google Docs) known to them. Also, the attacker used an email template which is close to what Google uses in sharing docs, to make the user click on the link. Anyone who carefully looked at the email or even the consent screen could have caught up something fishy happening—but unfortunately, very few do care.

It’s neither a flaw of OAuth 2.0 nor how Google implemented it. Phishing is a prominent threat in cybersecurity. Does that mean there is no way to prevent such attacks other than proper user education? There are basic things Google could do to prevent such attacks in the future. Looking at the consent screen, “Google Docs” is the key phrase used there to win user’s trust. When creating an OAuth 2.0 app in Google, you can pick any name you want. This helps an attacker to misguide users. Google could easily filter out the known names and prevent app developers from picking names to trick the users.

Another key issue is Google does not show the domain name of the application (but just the application name) on the consent page. Having domain name prominently displayed on the consent page will provide some hint to the user where he is heading to. Also the image of the application on the consent page misleads the user. The attacker has intentionally picked the Google Drive image there. If all these OAuth applications can go through an approval process, before launching into public, such mishaps can be prevented. Facebook already follows such a process. When you create a Facebook app, first, only the owner of the application can log in—to launch it to the public, it has to go through an approval process.

G Suite is widely used in the enterprise. Google can give the domain admins more control to whitelist, which applications the domain users can access from corporate credentials. This prevents users under phishing attacks, unknowingly sharing access to important company docs with third-party apps.

The phishing attack on Google is a good wake-up call to evaluate and think about how phishing resistance techniques can be occupied in different OAuth flows. For example, Google Chrome security team has put so much effort when they designed the Chrome warning page for invalid certificates. They did tons of research even to pick the color, the alignment of text, and what images to be displayed. Surely, Google will bring up more bright ideas to the table to fight against phishing.


## 요약

- OAuth 2.0은 실제 프로덕션 사용 사례를 수용하기 위한 액세스 위임을 위한 사실상의 표준입니다. 그 주변에는 엄청난 채택률을 보이는 거대한 생태계가 구축되어 있습니다.

- OAuth를 사용할 때마다 보안 모범 사례를 따르고 준수해야 하며 이미 모범 사례 시행을 처리하는 입증된 라이브러리와 제품을 항상 사용해야 합니다.

- OAuth IETF 워킹 그룹에서 생성한 OAuth 2.0 위협 모델 및 보안 고려 사항(RFC 6819)은 포괄적인 위협 모델을 기반으로 OAuth 2.0 사양에 있는 것 외에 OAuth 2.0에 대한 추가 보안 고려 사항을 정의합니다.

- 작성 당시 제안서 초안인 OAuth 2.0 Security Best Current Practice 문서에서는 RFC 6819가 게시된 이후 OAuth 2.0과 관련된 새로운 위협에 대해 이야기하고 있습니다.

- `OpenID Foundation` 산하 금융 등급 API(FAPI) 작업 그룹은 금융 등급 앱을 구축하기 위해 안전한 방법으로 OAuth 2.0을 사용하는 방법에 대한 일련의 지침을 게시했습니다.