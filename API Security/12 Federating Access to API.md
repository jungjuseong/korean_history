# 12. Federating Access to APIs

One of the research performed by Quocirca (analyst and research company) confirms that many businesses now have more external users who interact with enterprise applications than internal ones. In Europe, 58% of businesses transact directly with users from other firms and/or consumers. In the United Kingdom alone, the figure is 65%.

If you look at recent history, most enterprises today grow via acquisitions, mergers, and partnerships. In the United States alone, the volume of mergers and acquisitions totaled $865.1 billion in the first nine months of 2013, according to Dealogic. That’s a 39% increase over the same period of the previous year and the highest nine-month total since 2008. What does this mean for securing APIs? You need to have the ability to deal with multiple heterogeneous security systems across borders.

Enabling Federation

Federation, in the context of API security, is about propagating user identities across distinct identity management systems or distinct enterprises. Let’s start with a simple use case where you have an API exposed to your partners. How would you authenticate users for this API from different partners? These users belong to the external partners and are managed by them. HTTP Basic authentication won’t work. You don’t have access to the external users’ credentials, and, at the same time, your partners won’t expose an LDAP or a database connection outside their firewall to external parties. Asking for usernames and passwords simply doesn’t work in a federation scenario. Would OAuth 2.0 work? To access an API secured with OAuth, the client must present an access token issued by the owner of the API or issued by an entity that your API trusts. Users from external parties have to authenticate first with the OAuth authorization server that the API trusts and then obtain an access token. Ideally, the authorization server the API trusts is from the same domain as the API.

Neither the authorization code grant type nor the implicit grant type mandates how to authenticate users at the authorization server. It’s up to the authorization server to decide. If the user is local to the authorization server, then it can use a username and password or any other direct authentication protocol. If the user is from an external entity, then you have to use some kind of brokered authentication.

Brokered Authentication

With brokered authentication, at the time of authentication, the local authorization server (running in the same domain as the API) does not need to trust each and every individual user from external parties. Instead, it can trust a broker from a given partner domain (see Figure 12-1). Each partner should have a trust broker whose responsibility is to authenticate its own users (possibly through direct authentication) and then pass the authentication decision back to the local OAuth authorization server in a reliable and trusted manner. In practice, an identity provider running in the user’s (in our case, the partner employees’) home domain plays the role of a trust broker.

 

Figure 12-1

Brokered authentication for OAuth client applications

The trust relationship between the brokers from partners and the local OAuth authorization server (or between two federation domains) must be established out of band. In other words, it has to be established with a prior agreement between two parties. In most scenarios, trust between different entities is established through X.509 certificates. Let’s walk through a sample brokered authentication use case.

Going back to OAuth principles, you need to deal with four entities in a federation scenario: the resource owner, the resource server, the authorization server, and the client application. All these entities can reside in the same domain or in different ones.

Let’s start with the simplest scenario first. The resource owner (user), resource server (API gateway), and authorization server are in a single domain, and the client application (web app) is in a different domain. For example, you’re an employee of Foo Inc. and want to access a web application hosted by Bar Inc. (see Figure 12-1). Once you log in to a web application at Bar Inc., it needs to access an API hosted in Foo Inc. on your behalf. Using OAuth terminology, you’re the resource owner, and the API is hosted in the resource server. Both you and API are from the Foo domain. The web application hosted by Bar Inc. is the OAuth client application.

Figure 12-1 illustrates how brokered authentication works for an OAuth client application.

- The resource owner (user) from Foo Inc. visits the web application at Bar Inc. (step 1).

- To authenticate the user, the web application redirects the user to the OAuth authorization server at Foo Inc., which is also the home domain of the resource owner (step 2). To use the OAuth authorization code grant type, the web application also needs to pass its client ID along with the authorization code grant request during the redirection. At this time, the authorization server won’t authenticate the client application but only validates its existence. In a federation scenario, the authorization server does not need to trust each and every individual application (or OAuth client); rather, it trusts the corresponding domain. The authorization server accepts authorization grant requests from any client that belongs to a trusted domain. This also avoids the cost of client registration. You don’t need to register each client application from Bar Inc.—instead, you can build a trust relationship between the authorization server from Foo Inc. and the trust broker from Bar Inc. During the authorization code grant phase, the authorization server only needs to record the client ID. It doesn’t need to validate the client’s existence.

> **Note**
>
 The OAuth client identifier (ID) isn’t treated as a secret. It’s publicly visible to anyone.

- Once the client application gets the authorization code from the authorization server (step 3), the next step is to exchange it for a valid access token. This step requires client authentication.

- Because the authorization server doesn’t trust each individual application, the web application must first authenticate to its own trust broker in its own domain (step 4) and get a signed assertion (step 5). This signed assertion can be used as a token of proof against the authorization server in Foo Inc.

- The authorization server validates the signature of the assertion and, if it’s signed by an entity it trusts, returns the corresponding access token to the client application (steps 6 and 7).

- The client application can use the access token to access the APIs in Foo Inc. on behalf of the resource owner (step 8), or it can talk to a user endpoint at Foo Inc. to get more information about the user.

> **Note**
>


The definition of assertion, according to the Oxford English Dictionary, is “a confident and forceful statement of fact or belief.” The fact or belief here is that the entity that brings this assertion is an authenticated entity at the trust broker. If the assertion isn’t signed, anyone in the middle can alter it. Once the trust broker (or the asserting party) signs the assertion with its private key, no one in the middle can alter it. If it’s altered, any alterations can be detected at the authorization server during signature validation. The signature is validated using the corresponding public key of the trust broker.

Security Assertion Markup Language (SAML)

Security Assertion Markup Language (SAML) is an OASIS standard for exchanging authentication, authorization, and identity-related data between interested parties in an XML-based data format. SAML 1.0 was adopted as an OASIS standard in 2002, and in 2003 SAML 1.1 was ratified as an OASIS standard. At the same time, the Liberty Alliance donated its Identity Federation Framework to OASIS. SAML 2.0 became an OASIS standard in 2005 by converging SAML 1.1, Liberty Alliance’s Identity Federation Framework, and Shibboleth 1.3. SAML 2.0 has four basic elements:

- Assertions: Authentication, Authorization, and Attribute assertions.

- Protocol: Request and Response elements to package SAML assertions.

- Bindings: How to transfer SAML messages between interested parties. HTTP binding and SOAP binding are two examples. If the trust broker uses a SOAP message to transfer a SAML assertion, then it has to use the SOAP binding for SAML.

- Profiles: How to aggregate the assertions, protocol, and bindings to address a specific use case. A SAML 2.0 Web Single Sign-On (SSO) profile defines a standard way to establish SSO between different service providers via SAML.

> **Note**
>


The blog post at http://blog.facilelogin.com/2011/11/depth-of-saml-saml-summary.html provides a high-level overview of SAML.

SAML 2.0 Client Authentication

To achieve client authentication with the SAML 2.0 profile for OAuth 2.0, you can use the parameter client_assertion_type with the value urn:ietf:params:oauth:client-assertion-type:saml2-bearer in the access token request (see step 6 in Figure 12-1). The OAuth flow starts from step 2.

Now let’s dig into each step. The following shows a sample authorization code grant request initiated by the web application at Bar Inc.:

GET /authorize?response_type=code

               &client_id=wiuo879hkjhkjhk3232

               &state=xyz

               &redirect_uri=https://bar.com/cb

HTTP/1.1

Host: auth.foo.com

This results in the following response, which includes the requested authorization code:

HTTP/1.1 302 Found

Location: https://bar.com/cb?code=SplwqeZQwqwKJjklje&state=xyz

So far it’s the normal OAuth authorization code flow. Now the web application has to talk to the trust broker in its own domain to obtain a SAML assertion. This step is outside the scope of OAuth. Because this is machine-to-machine authentication (from the web application to the trust broker), you can use a SOAP-based WS-Trust protocol to obtain the SAML assertion or any other protocol like OAuth 2.0 Token Delegation profile, which we discussed in Chapter 9. The web application does not need to do this each time a user logs in; it can be one-time operation that is governed by the lifetime of the SAML assertion. The following is a sample SAML assertion obtained from the trust broker:

<saml:Assertion >

       <saml:Issuer>bar.com</saml:Issuer>

       <ds:Signature>

         <ds:SignedInfo></ds:SignedInfo>

         <ds:SignatureValue></ds:SignatureValue>

         <ds:KeyInfo></ds:KeyInfo>

       </ds:Signature>

       <saml:Subject>

              <saml:NameID>18982198kjk2121</saml:NameID>

              <saml:SubjectConfirmation>

              <saml:SubjectConfirmationData

                        NotOnOrAfter="2019-10-05T19:30:14.654Z"

                        Recipient="https://foo.com/oauth2/token"/>

              </saml:SubjectConfirmation>

       </saml:Subject>

       <saml:Conditions

             NotBefore="2019-10-05T19:25:14.654Z"

             NotOnOrAfter="2019-10-05T19:30:14.654Z">

               <saml:AudienceRestriction>

                  <saml:Audience>

                      https://foo.com/oauth2/token

                  </saml:Audience>

               </saml:AudienceRestriction>

       </saml:Conditions>

       <saml:AuthnStatement AuthnInstant="2019-10-05T19:25:14.655Z">

              <saml:AuthnContext>

                 <saml:AuthnContextClassRef>

                    urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified

                 </saml:AuthnContextClassRef>

             </saml:AuthnContext>

       </saml:AuthnStatement>

</saml:Assertion>

To use this SAML assertion in an OAuth flow to authenticate the client, it must adhere to the following rules:

- The assertion must have a unique identifier for the Issuer element, which identifies the token-issuing entity. In this case, the broker of the Bar Inc.

- The assertion must have a NameID element inside the Subject element that uniquely identifies the client application (web app). This is treated as the client ID of the client application at the authorization server.

- The SubjectConfirmation method must be set to urn:oasis:names:tc:SAML:2.0:cm:bearer.

- If the assertion issuer authenticates the client, then the assertion must have a single AuthnStatement.

> **Note**
>


WS-Trust is an OASIS standard for SOAP message security. WS-Trust, which is built on top of the WS-Security standard, defines a protocol to exchange identity information that is wrapped in a token (SAML), between two trust domains. The blog post at http://blog.facilelogin.com/2010/05/ws-trust-with-fresh-banana-service.html explains WS-Trust at a high level. The latest WS-Trust specification is available at http://docs.oasis-open.org/ws-sx/ws-trust/v1.4/errata01/ws-trust-1.4-errata01-complete.html.

Once the client web application gets the SAML assertion from the trust broker, it has to base64url-encode the assertion and send it to the authorization server along with the access token request. In the following sample HTTP POST message, client_assertion_type is set to urn:ietf:params:oauth:client-assertion-type:saml2-bearer, and the base64url-encoded (see Appendix E) SAML assertion is set to the client_assertion parameter :

POST /token HTTP/1.1

Host: auth.foo.com

Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&code=SplwqeZQwqwKJjklje

&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:saml2-bearer

&client_assertion=HdsjkkbKLew...[omitted for brevity]...OT

Once the authorization server receives the access token request, it validates the SAML assertion. If it’s valid (signed by a trusted party), an access token is issued, along with a refresh token.

SAML Grant Type for OAuth 2.0

The previous section explained how to use a SAML assertion to authenticate a client application. That is one federation use case that falls under the context of OAuth. There the trust broker was running inside Bar Inc., where the client application was running. Let’s consider a use case where the resource server (API), the authorization server, and the client application run in the same domain (Bar Inc.), while the user is from an outside domain (Foo Inc.). Here the end user authenticates to the web application with a SAML assertion (see Figure 12-2). A trust broker (a SAML identity provider) in the user’s domain issues this assertion. The client application uses this assertion to talk to the local authorization server to obtain an access token to access an API on behalf of the logged-in user.

 

Figure 12-2

Brokered authentication with the SAML grant type for OAuth 2.0

Figure 12-2 illustrates how brokered authentication with a SAML grant type for OAuth 2.0 works.

- The first three steps are outside the scope of OAuth. The resource owner first logs in to the web application owned by Bar Inc. via SAML 2.0 Web SSO.

- The SAML 2.0 Web SSO flow is initiated by the web application by redirecting the user to the SAML identity provider at Foo Inc. (step 2).

- Once the user authenticates to the SAML identity provider, the SAML identity provider creates a SAML response (which wraps the assertion) and sends it back to the web application (step 3). The web application validates the signature in the SAML assertion and, if a trusted identity provider signs it, allows the user to log in to the web application.

- Once the user logs in to the web application, the web application has to exchange the SAML assertion for an access token by talking to its own internal authorization server (steps 4 and 5). The way to do this is defined in the SAML 2.0 Profile for OAuth 2.0 Client Authentication and Authorization Grants specification (RFC 7522).

The following is a sample POST message from the web application to the authorization server. There the value of grant_type must be urn:ietf:params:oauth:grant-type:saml2-bearer, and the base64url-encoded SAML assertion is set as the value of the assertion parameter:

> **Note**
>


No refresh tokens are issued under the SAML Bearer grant type. The lifetime of the access token should not exceed the lifetime of the SAML bearer assertion by a significant amount.

POST /token HTTP/1.1

Host: auth.bar.com

Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:saml2-bearer

&assertion=QBNhbWxwOl...[omitted for brevity]...OT4

This request is validated at the authorization server. The SAML assertion is once again validated via its signature; and, if a trusted identity provider signs it, the authorization server issues a valid access token.

The scope of the access token issued under the SAML Bearer grant type should be set out of band by the resource owner. Out of band here indicates that the resource owner makes a pre-agreement with the resource server/authorization server with respect to the scope associated with a given resource when the SAML grant type is being used. The client application can include a scope parameter in the authorization grant request, but the value of the scope parameter must be a subset of the scope defined out of band by the resource owner. If no scope parameter is included in the authorization grant request, then the access token inherits the scope set out of band.

Both federation use cases discussed assume that the resource server and the authorization server are running in the same domain. If that isn’t the case, the resource server must invoke an API exposed by the authorization server to validate the access token at the time the client tries to access a resource. If the authorization server supports the OAuth Introspection specification (discussed in Chapter 9), the resource server can talk to the introspection endpoint and find out whether the token is active or not and also what scopes are associated with the token. The resource server can then check whether the token has the required set of scopes to access the resource.

JWT Grant Type for OAuth 2.0

The JSON Web Token (JWT) profile for OAuth 2.0, which is defined in the RFC 7523, extends the OAuth 2.0 core specification by defining its own authorization grant type and a client authentication mechanism. An authorization grant in OAuth 2.0 is an abstract representation of the temporary credentials granted to the OAuth 2.0 client by the resource owner to access a resource. The OAuth 2.0 core specification defines four grant types: authorization code, implicit, resource owner password, and client credentials. Each of these grant types defines in a unique way how the resource owner can grant delegated access to a resource he/she owns to an OAuth 2.0 client. The JWT grant type, which we discuss in this chapter, defines how to exchange a JWT for an OAuth 2.0 access token. In addition to the JWT grant type, the RFC 7523 also defines a way to authenticate an OAuth 2.0 client in its interactions with an OAuth 2.0 authorization server. OAuth 2.0 does not define a concrete way for client authentication, even though in most of the cases it’s the HTTP Basic authentication with client id and the client secret. The RFC 7523 defines a way to authenticate an OAuth 2.0 client using a JWT.

The JWT authorization grant type assumes that the client is in possession with a JWT. This JWT can be a self-issued JWT or a JWT obtained from an identity provider. Based on who signs the JWT, one can differentiate a self-issued JWT from an identity provider–issued JWT. The client itself signs a self-issued JWT, while an identity provider signs the identity provider–issued JWT. In either case, the OAuth authorization server must trust the issuer of the JWT. The following shows a sample JWT authorization grant request, where the value of the grant_type parameter is set to urn:ietf:params:oauth:grant-type:jwt-bearer.

POST /token HTTP/1.1

Host: auth.bar.com

Content-Type: application/x-www-form-urlencoded

grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=eyJhbGciOiJFUzI1NiIsImtpZCI6IjE2In0.

eyJpc3Mi[...omitted for brevity...].

J9l-ZhwP[...omitted for brevity...]

The Assertion Framework for OAuth 2.0 Client Authentication and Authorization Grants specification, which is the RFC 7521, defines the parameters in the JWT authorization grant request, as listed out in the following:

- grant_type: This is a required parameter, which defines the format of the assertion, as understood by the authorization server. The value of grant_type is an absolute URI, and it must be urn:ietf:params:oauth:grant-type:jwt-bearer.

- assertion: This is a required parameter, which carries the token. For example, in the case of JWT authorization grant type, the assertion parameter will carry the base64url-encoded JWT, and it must only contain a single JWT. If there are multiple JWTs in the assertion, then the authorization server will reject the grant request.

- scope: This is an optional parameter. Unlike in authorization code and implicit grant types, the JWT grant type does not have a way to get the resource owner’s consent for a requested scope. In such case, the authorization server will establish the resource owner’s consent via an out-of-band mechanism. If the authorization grant request carries a value for the scope parameter, then either it should exactly match the out-of-band established scope or less than that.

> **Note**
>


The OAuth authorization server will not issue a refresh_token under the JWT grant type. If the access_token expires, then the OAuth client has to get a new JWT (if the JWT has expired) or use the same valid JWT to get a new access_token. The lifetime of the access_token should match the lifetime of the corresponding JWT.

Applications of JWT Grant Type

There are multiple applications of the JWT authorization grant type. Let’s have a look at one common use case, where the end user or the resource owner logs in to a web application via OpenID Connect (Chapter 6), then the web application needs to access an API on behalf of the logged-in user, which is secured with OAuth 2.0. Figure 12-3 shows the key interactions related to this use case.

 

Figure 12-3

JWT grant type, a real-world example

The following lists out all the interactions as illustrated in Figure 12-3 by the number:

- The end user visits the web application (step 1).

- In step 2, the user gets redirected to the OpenID Connect server and authenticates against the Active Directory connected to it. After the authentication, the user gets redirected back to the web application, with an authorization code (assuming that we are using OAuth 2.0 authorization code grant type).

- The web application talks directly to the OpenID Connect server and exchanges the authorization code from the previous step to an ID token and an access token. The ID token itself is a JWT, which is signed by the OpenID Connect server (step 3).

- Now the web application needs to invoke an API on behalf of the logged-in user. It talks to the OAuth authorization server, trusted by the API, and using the JWT grant type, exchanges the JWT from step 3 to an OAuth access token. The OAuth authorization server validates the JWT and makes sure that it’s being signed by a trusted identity provider. In this case, the OAuth authorization server trusts the OpenID Connect identity provider (step 4).

- In step 5, the web application invokes the API with the access token from step 4.

- The application server, which hosts the API, validates the access token by talking to the OAuth authorization server, which issued the access token (step 6).

JWT Client Authentication

The OAuth 2.0 core specification does not define a concrete way to authenticate OAuth clients to the OAuth authorization server. Mostly it’s the HTTP Basic authentication with client_id and the client_secret. The RFC 7523 defines a way to authenticate OAuth clients with a JWT. The JWT client authentication is not just limited to a particular grant type; it can be used with any OAuth grant types. That’s another beauty in OAuth 2.0—the OAuth grant types are decoupled from the client authentication. The following shows a sample request to the OAuth authorization server under the authorization code grant type, which uses JWT client authentication.

POST /token HTTP/1.1

Host: auth.bar.com

Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&

code=n0esc3NRze7LTCu7iYzS6a5acc3f0ogp4&      client_assertion_type=urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer&

client_assertion=eyJhbGciOiJSUzI1NiIsImtpZCI6IjIyIn0.

eyJpc3Mi[...omitted for brevity...].

cC4hiUPo[...omitted for brevity...]

The RFC 7523 uses three additional parameters in the OAuth request to the token endpoint to do the client authentication: client_assertion_type, client_assertion, and client_id (optional). The Assertion Framework for OAuth 2.0 Client Authentication and Authorization Grants specification, which is the RFC 7521, defines these parameters. The following lists them out along with their definitions:

- client_assertion_type: This is a required parameter, which defines the format of the assertion, as understood by the OAuth authorization server. The value of client_assertion_type is an absolute URI. For JWT client authentication, this parameter must carry the value urn:ietf:params:oauth:client-assertion-type:jwt-bearer.

- client_assertion: This is a required parameter, which carries the token. For example, in the case of JWT client authentication, the client_assertion parameter will carry the base64url-encoded JWT, and it must only contain a single JWT. If there are multiple JWTs in the assertion, then the authorization server will reject the grant request.

- client_id: This is an optional parameter. Ideally, the client_id must be present inside the client_assertion itself. If this parameter carries a value, it must match the value of the client_id inside the client_assertion. Having the client_id parameter in the request itself could be useful, as the authorization server does not need to parse the assertion first to identify the client.

Applications of JWT Client Authentication

The JWT client authentication is used to authenticate a client to an OAuth authorization server with a JWT, instead of using HTTP Basic authentication with client_id and client_secret. Why would someone select JWT client authentication over HTTP Basic authentication?

Let’s take an example. Say we have two companies called foo and bar. The foo company hosts a set of APIs, and the bar company has a set of developers who are developing applications against those APIs. Like in most of the OAuth examples we discussed in this book, the bar company has to register with the foo company to obtain a client_id and client_secret, in order to access its APIs. Since the bar company develops multiple applications (a web app, a mobile app, a rich client app), the same client_id and client_secret obtained from the foo company need to be shared between multiple developers. This is a bit risky as any one of those developers can pass over the secret keys to anyone else—or even misuse them. To fix this, we can use JWT client authentication. Instead of sharing the client_id and the client_secret with its developers, the bar company can create a key pair (a public key and a private key), sign the public key by the key of the company’s certificate authority (CA), and hand them over to its developers. Now, instead of the shared client_id and client_secret, each developer will have its own public key and private key, signed by the company CA. When talking to the foo company’s OAuth authorization server, the applications will use the JWT client authentication, where its own private key signs the JWT—and the token will carry the corresponding public key. The following code snippet shows a sample decoded JWS header and the payload, which matches the preceding criteria. Chapter 7 explains JWS in detail and how it relates to JWT.

{

  "alg": "RS256"

  "x5c": [

          "MIIE3jCCA8agAwIBAgICAwEwDQYJKoZIhvcNAQEFBQ......",

          "MIIE3jewlJJMddds9AgICAwEwDQYJKoZIhvUjEcNAQ......",

         ]

}

{

  "sub": "3MVG9uudbyLbNPZN8rZTCj6IwpJpGBv49",

  "aud": "https://login.foo.com",

  "nbf": 1457330111,

  "iss": "bar.com",

  "exp": 1457330711,

  "iat": 1457330111,

  "jti": "44688e78-2d30-4e88-8b86-a6e25cd411fd"

}

The authorization server at the foo company first needs to verify the JWT with the attached public key (which is the value of the x5c parameter in the preceding code snippet) and then needs to check whether the corresponding public key is signed by the bar company’s certificate authority. If that is the case, then it’s a valid JWT and would successfully complete the client authentication. Also note that the value of the original client_id created for the bar company is set as the subject of the JWT.

Still we have a challenge. How do we revoke a certificate that belongs to a given developer, in case he/she resigns or it is found that the certificate is misused? To facilitate this, the authorization server has to maintain a certificate revocation list (CRL) by the client_id. In other words, each client_id can maintain its own certificate revocation list. To revoke a certificate, the client (in this case, the bar company) has to talk to the CRL API hosted in the authorization server. The CRL API is a custom API that must be hosted at the OAuth authorization server to support this model. This API must be secured with OAuth 2.0 client credentials grant type. Once it receives a request to update the CRL, it will update the CRL corresponding to the client who invokes the API, and each time the client authentication happens, the authorization server must check the public certificate in the JWT against the CRL. If it finds a match, then the request should be turned down immediately. Also, at the time the CRL of a particular client is updated, all the access tokens and refresh tokens issued against a revoked public certificate must be revoked too. In case you worry about the overhead it takes to support a CRL, you probably can use short-lived certificates and forget about revocation. Figure 12-4 shows the interactions between the foo and the bar companies.

 

Figure 12-4

JWT client authentication, a real-world example

Parsing and Validating JWT

The OAuth authorization server must parse and validate the JWT, both in the JWT grant type and in the client authentication. The following lists out the criteria for token validation:

- The JWT must have the iss parameter in it. The iss parameter represents the issuer of the JWT. This is treated as a case-sensitive string value. Ideally, this represents the asserting party of the claims set. If Google issues the JWT, then the value of iss would be accounts.google.com. This is an indication to the receiving party who the issuer of the JWT is.

- The JWT must have the sub parameter in it. The token issuer or the asserting party issues the JWT for a particular entity, and the claims set embedded into the JWT normally represents this entity, which is identified by the sub parameter. The value of the sub parameter is a case-sensitive string value. For the JWT client authentication, the value of the sub parameter must carry the corresponding client_id, while for the authorization grant, it will be the authorized accessor or the resource server for which the access token is being requested.

- The JWT must have the aud parameter . The token issuer issues the JWT to an intended recipient or a list of recipients, which is represented by the aud parameter. The recipient or the recipient list should know how to parse the JWT and validate it. Prior to any validation check, the recipient of the token must first see whether the particular JWT is issued for its use and if not should reject immediately. The value of the aud parameter can be a case-sensitive string value or an array of strings. The token issuer should know, prior to issuing the token, who the intended recipient (or the recipients) of the token is, and the value of the aud parameter must be a pre-agreed value between the token issuer and the recipient. In practice, one can also use a regular expression to validate the audience of the token. For example, the value of the aud in the token can be ∗.apress.com, while each recipient under the apress.com domain can have its own aud values: foo.apress.com, bar.apress.com likewise. Instead of finding an exact match for the aud value, each recipient can just check whether the aud value in the token matches a regular expression: (?:[a-zA-Z0-9]∗|\∗).apress.com. This will make sure that any recipient can use a JWT, which is having any subdomain of apress.com.

- The JWT must have the exp parameter. Each JWT will carry an expiration time. The recipient of the JWT token must reject it, if that token has expired. The issuer can decide the value of the expiration time. The JWT specification does not recommend or provide any guidelines on how to decide the best token expiration time. It’s a responsibility of the other specifications, which use JWT internally, to provide such recommendations. The value of the exp parameter is calculated by adding the expiration time (from the token issued time) in seconds to the time elapsed from 1970-01-01T00:00:00Z UTC to the current time. If the token issuer’s clock is out of sync with the recipient’s clock (irrespective of their time zone), then the expiration time validation could fail. To fix that, each recipient can add a couple of minutes as the clock skew.

- The JWT may have the nbf parameter . In other words, this is not a must. The recipient of the token should reject it, if the value of the nbf parameter is greater than the current time. The JWT is not good enough to use prior to the value indicated in the nbf parameter. The value of the nbf parameter is calculated by adding the not before time (from the token issued time) in seconds to the time elapsed from 1970-01-01T00:00:00Z UTC to the current time.

- The JWT may have the iat parameter. The iat parameter in the JWT indicates the issued time of the JWT as calculated by the token issuer. The value of the iat parameter is the number of seconds elapsed from 1970-01-01T00:00:00Z UTC to the current time, when the token is issued.

- The JWT must be digitally signed or carry a Message Authentication Code (MAC) defined by its issuer.

## Summary

- Identity federation is about propagating user identities across boundaries. These boundaries can be between distinct enterprises or even distinct identity management systems within the same enterprise.

- Two OAuth 2.0 profiles—SAML 2.0 grant type and JWT grant type—focus on building federation scenarios for API security.

- The SAML profile for OAuth 2.0, which is defined in the RFC 7522, extends the capabilities of the OAuth 2.0 core specification. It introduces a new authorization grant type as well as a way of authenticating OAuth 2.0 clients, based on a SAML assertion.

- The JSON Web Token (JWT) profile for OAuth 2.0, which is defined in the RFC 7523, extends the capabilities of the OAuth 2.0 core specification. It introduces a new authorization grant type as well as a way of authenticating OAuth 2.0 clients, based on a JWT.

 
