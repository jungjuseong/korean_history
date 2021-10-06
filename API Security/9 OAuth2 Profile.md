9. OAuth 2.0 Profiles

OAuth 2.0 is a framework for delegated authorization. It doesn’t address all specific enterprise API security use cases. The OAuth 2.0 profiles built on top of the core framework build a security ecosystem to make OAuth 2.0 ready for enterprise grade deployments. OAuth 2.0 introduced two extension points via grant types and token types. The profiles for OAuth 2.0 are built on top of this extensibility. This chapter talks about five key OAuth 2.0 profiles for token introspection, chained API invocation, dynamic client registration, and token revocation.

Token Introspection

OAuth 2.0 doesn’t define a standard API for communication between the resource server and the authorization server. As a result, vendor-specific, proprietary APIs have crept in to couple the resource server to the authorization server. The Token Introspection profile1 for OAuth 2.0 fills this gap by proposing a standard API to be exposed by the authorization server (Figure 9-1), allowing the resource server to talk to it and retrieve token metadata.

 

Figure 9-1

OAuth 2.0 Token Introspection

Any party in possession of the access token can generate a token introspection request. The introspection endpoint can be secured and the popular options are mutual Transport Layer Security (mTLS) and OAuth 2.0 client credentials.

POST /introspection HTTP/1.1

Accept: application/x-www-form-urlencoded

Host: authz.server.com

Authorization: Basic czZCaGRSa3F0Mzo3RmpmcDBaQnIxS3REUmJuZlZkbUl3

                     token=X3241Affw.423399JXJ&

                     token_type_hint=access_token&

Let’s examine the definition of each parameter:

- token: The value of the access_token or the refresh_token. This is the token where we need to get metadata about.

- token_type_hint: The type of the token (either the access_token or the refresh_token). This is optional and the value passed here could optimize the authorization server’s operations in generating the introspection response.

This request returns the following JSON response. The following response does not show all possible parameters that an introspection response could include:

HTTP/1.1 200 OK

Content-Type: application/json

Cache-Control: no-store

{

          "active": true,

          "client_id":"s6BhdRkqt3",

          "scope": "read write dolphin",

          "sub": "2309fj32kl",

          "aud": "http://my-resource/∗"

 }

Let’s examine the definition of the key parameters that you could expect in an introspection response:

- active: Indicates whether the token is active. To be active, the token should not be expired or revoked. The authorization server can define its own criteria for how to define active. This is the only required parameter the introspection response must include. All the others are optional.

- client_id: The identifier of the client to which the authorization server issued this token.

- scope: Approved scopes associated with the token. The resource server must validate that the scopes required to access the API are at least a subset of scopes attached to the token.

- sub: The subject identifier of the user who approved the authorization grant or in other words an identifier for the user who this token represents. This identifier is not necessarily a human-readable identifier, but it must carry a unique value all the time. The authorization server may produce a unique subject for each authorization server/resource server combination. This is implementation specific, and to support this, the authorization server must uniquely identify the resource server. In terms of privacy, it is essential that the authorization server maintains different subject identifiers by resource server, and this kind of an identifier is known as a persistence pseudonym. Since the authorization server issues different pseudonyms for different resource servers, for a given user, these resource servers together won’t be able to identify what other services this user accesses.

- username: Carries a human-readable identifier of the user who approved the authorization grant or in other words a human-readable identifier for the user who this token represents. If you are to persist anything at the resource server end, with respect to the user, username is not the right identifier. The value of the username can change time to time, based on how it is implemented at the authorization server end.

- aud: The allowed audience for the token. Ideally, this should carry an identifier that represents the corresponding resource server. If it does not match with your identifier, the resource server must immediately reject the token. This aud element can carry more than one identifier, and in that case you need to see whether your resource server’s one is part of it. Also in some implementations, rather than doing one-to-one string match, you can also match against a regular expression. For example, http://∗.my-resource.com will find a match for both the resource servers carrying the identifiers http://foo.my-resource.com and http://bar.my-resource.com.

> **Note**
>
 The audience (aud) parameter is defined in the OAuth 2.0: Audience Information Internet draft available at http://tools.ietf.org/html/draft-tschofenig-oauth-audience-00. This is a new parameter introduced into the OAuth token request flow and is independent of the token type.

- exp: Defines in seconds from January 1, 1970, in UTC, the expiration time of the token. This looks like redundant, as the active parameter is already there in the response. But resource server can utilize this parameter to optimize how frequently it wants to talk to the introspection endpoint of the authorization server. Since the call to the introspection endpoint is remote, there can be performance issues, and also it can be down due to some reason. In that case, the resource server can have a cache to carry the introspection responses, and when it gets the same token again and again, it can check the cache, and if the token has not expired, it can accept the token as valid. Also there should be a valid cache expiration time; otherwise, even if the token is revoked at the authorization server, the resource server will not know about it.

- iat: Defines in seconds from January 1, 1970, in UTC, the issued time of the token.

- nbf: Defines in seconds from January 1, 1970, in UTC, the time before the token should not be used.

- token_type: Indicates the type of the token. It can be a bearer token, a MAC token (see Appendix G), or any other type.

- iss: Carries an identifier that represents the issuer of the token. A resource server can accept tokens from multiple issuers (or authorization servers). If you store the subject of the token at the resource server end, it becomes unique only with the issuer. So you need to store it along with the issuer. There can be a case where the resource server connects to a multitenanted authorization server. In that case, your introspection endpoint will be the same, but it will be different issuers who issue tokens under different tenants.

- jti: This is a unique identifier for the token, issued by the authorization server. The jti is mostly used when the access token the authorization server issues is a JWT or a self-contained access token. This is useful to avoid replaying access tokens.

While validating the response from the introspection endpoint, the resource server should first check whether the value of active is set to true. Then it should check whether the value of aud in the response matches the aud URI associated with the resource server or the resource. Finally, it can validate the scope. The required scope to access the resource should be a subset of the scope values returned in the introspection response. If the resource server wants to do further access control based on the client or the resource owner, it can do so with respect to the values of sub and client_id.

Chain Grant Type

Once the audience restriction is enforced on OAuth tokens, they can only be used against the intended audience. You can access an API with an access token that has an audience restriction corresponding to that API. If this API wants to talk to another protected API to form the response to the client, the first API must authenticate to the second API. When it does so, the first API can’t just pass the access token it received initially from the client. That will fail the audience restriction validation at the second API. The Chain Grant Type OAuth 2.0 profile defines a standard way to address this concern.

According to the OAuth Chain Grant Type profile, the API hosted in the first resource server must talk to the authorization server and exchange the OAuth access token it received from the client for a new one that can be used to talk to the other API hosted in the second resource server.

> **Note**
>


The Chain Grant Type for OAuth 2.0 profile is available at https://datatracker.ietf.org/doc/draft-hunt-oauth-chain.

The chain grant type request must be generated from the first resource server to the authorization server. The value of the grant type must be set to http://oauth.net/grant_type/chain and should include the OAuth access token received from the client. The scope parameter should express the required scopes for the second resource in space-delimited strings. Ideally, the scope should be the same as or a subset of the scopes associated with the original access token. If there is any difference, then the authorization server can decide whether to issue an access token or not. This decision can be based on an out-of-band agreement with the resource owner:

POST /token HTTP/1.1

Host: authz.server.net

Content-Type: application/x-www-form-urlencoded

grant_type=http://oauth.net/grant_type/chain

oauth_token=dsddDLJkuiiuieqjhk238khjh

scope=read

This returns the following JSON response. The response includes an access token with a limited lifetime, but it should not have a refresh token. To get a new access token, the first resource server once again must present the original access token:

HTTP/1.1 200 OK

Content-Type: application/json;charset=UTF-8

Cache-Control: no-store

Pragma: no-cache

{

       "access_token":"2YotnFZFEjr1zCsicMWpAA",

       "token_type":"Bearer",

       "expires_in":1800,

}

The first resource server can use the access token from this response to talk to the second resource server. Then the second resource server talks to the authorization server to validate the access token (see Figure 9-2).

 

Figure 9-2

OAuth 2.0 Token Exchange

We talked about the chain grant type in the first edition of the book as well. But since then this specification didn’t make any progress. If you are using the chain grant type already, you should migrate to the OAuth 2.0 Token Exchange specification, which is still at the draft stage, but closer to being an RFC. In the next section, we talk about OAuth 2.0 Token Exchange draft RFC.

Token Exchange

The OAuth 2.0 Token Exchange is a draft proposal discussed under the IETF working group at the moment. It solves a similar problem, which was addressed by the Chain Grant Type proposal we discussed in the previous section, with some improvements. Like in the chain grant type, when the first resource server receives an access token from the client application, and when it wants to talk to another resource server, the first resource server generates the following request to talk to the authorization server—and exchanges the access token it got from the client application to a new one.

POST /token HTTP/1.1

Host: authz.server.net

Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:token-exchange

subject_token=dsddDLJkuiiuieqjhk238khjh

subject_token_type=urn:ietf:params:oauth:token-type:access_token

requested_token_type=urn:ietf:params:oauth:token-type:access_token

resource=https://bar.example.com

scope=read

The preceding sample request does not include all possible parameters. Let’s have a look at the key parameters that you could expect in a token exchange request:

- grant_type: Indicates to the token endpoint that, this is a request related to token exchange and must carry the value urn:ietf:params:oauth:grant-type:token-exchange. This is a required parameter.

- resource: The value of this parameter carries a reference to the target resource. For example, if the initial request comes to foo API, and it wants to talk to the bar API, then the value of the resource parameter carries the endpoint of the bar API. This is also quite useful in a microservices deployment, where one microservice has to authenticate to another microservice. The OAuth 2.0 authorization server can enforce access control policies against this request to check whether the foo API can access the bar API. This is an optional parameter.

- audience: The value of this parameter serves the same purpose as the resource parameter, but in this case the value of the audience parameter is a reference of the target resource, not an absolute URL. If you intend to use the same token against multiple target resources, you can include a list of audience values under the audience parameter. This is an optional parameter.

- scope: Indicates the scope values with respect to the new token. This parameter can carry a list of space-delimited, case-sensitive strings. This is an optional parameter.

- requested_token_type: Indicates the type of request token, which can be any of urn:ietf:params:oauth:token-type:access_token, urn:ietf:params:oauth:token-type:refresh_token, urn:ietf:params:oauth:token-type:id_token, urn:ietf:params:oauth:token-type:saml1, and urn:ietf:params:oauth:token-type:saml2. This is an optional parameter, and if it is missing, the token endpoint can decide the type of the token to return. If you use a different token type, which is not in the above list, then you can have your own URI as the requested_token_type.

- subject_token: Carries the initial token the first API receives. This carries the identity of the entity that initially invokes the first API. This is a required parameter.

- subject_token_type: Indicates the type of subject_token, which can be any of urn:ietf:params:oauth:token-type:access_token, urn:ietf:params:oauth:token-type:refresh_token, urn:ietf:params:oauth:token-type:id_token, urn:ietf:params:oauth:token-type:saml1, and urn:ietf:params:oauth:token-type:saml2. This is a required parameter. If you use a different token type, which is not in the above list, then you can have your own URI as the subject_token_type.

- actor_token: Carries a security token, which represents the identity of the entity that intends to use the requested token. In our case, when foo API wants to talk to the bar API, actor_token represents the foo API. This is an optional parameter.

- actor_token_type: Indicates the type of actor_token, which can be any of urn:ietf:params:oauth:token-type:access_token, urn:ietf:params:oauth:token-type:refresh_token, urn:ietf:params:oauth:token-type:id_token, urn:ietf:params:oauth:token-type:saml1, and urn:ietf:params:oauth:token-type:saml2. This is a required parameter when the actor_token is present in the request. If you use a different token type, which is not in the above list, then you can have your own URI as the actor_token_type.

The preceding request returns the following JSON response. The access_token parameter in the response carries the requested token, while the issued_token_type indicates its type. The other parameters in the response, token_type, expires_in, scope, and refresh_token, carry the same meaning as in a typical OAuth 2.0 token response, which we discussed in Chapter 4.

HTTP/1.1 200 OK

Content-Type: application/json

Cache-Control: no-cache, no-store

{

 "access_token":"eyJhbGciOiJFUzI1NiIsImtpZCI6IjllciJ9 ",

 "issued_token_type":

         "urn:ietf:params:oauth:token-type:access_token",

 "token_type":"Bearer",

 "expires_in":60

}

Dynamic Client Registration Profile

According to the OAuth 2.0 core specification, all OAuth clients must be registered with the OAuth authorization server and obtain a client identifier before any interactions. The aim of the Dynamic Client Registration OAuth 2.0 profile2 is to expose an endpoint for client registration in a standard manner to facilitate on-the-fly registrations.

The dynamic registration endpoint exposed by the authorization server can be secured or not. If it’s secured, it can be secured with OAuth, HTTP Basic authentication, Mutual Transport Layer Security (mTLS), or any other security protocol as desired by the authorization server. The Dynamic Client Registration profile doesn’t enforce any authentication protocols over the registration endpoint, but it must be secured with TLS. If the authorization server decides that it should allow the endpoint to be public and let anyone be registered, it can do so. For the registration, the client application must pass all its metadata to the registration endpoint:

POST /register HTTP/1.1

Content-Type: application/json

Accept: application/json

Host: authz.server.com

{

"redirect_uris":["https://client.org/callback","https://client.org/callback2"],

"token_endpoint_auth_method":"client_secret_basic","grant_types": ["authorization_code" , "implicit"],

"response_types": ["code" , "token"],

}

Let’s examine the definition of some of the important parameters in the client registration request:

- redirect_uris: An array of URIs under the control of the client. The user is redirected to one of these redirect_uris after the authorization grant. These redirect URIs must be over Transport Layer Security (TLS).

- token_endpoint_auth_method: The supported authentication scheme when talking to the token endpoint. If the value is client_secret_basic, the client sends its client ID and the client secret in the HTTP Basic Authorization header. If it’s client_secret_post, the client ID and the client secret are in the HTTP POST body. If the value is none, the client doesn’t want to authenticate, which means it’s a public client (as in the case of the OAuth implicit grant type or when you use authorization code grant type with a single-page application). Even though this RFC only supports three client authentication methods, the other OAuth profiles can introduce their own. For example, OAuth 2.0 Mutual-TLS Client Authentication and Certificate-Bound Access Tokens, a draft RFC which is being discussed under the IETF OAuth working group at the moment, introduces a new authentication method called tls_client_auth. This indicates that client authentication to the token endpoint happens with mutual TLS.

- grant_types: An array of grant types supported by the client. It is always better to limit your client application only to use the grant types it needs and no more. For example, if your client application is a single-page application, then you must only use authorization_code grant type.

- response_types: An array of expected response types from the authorization server. In most of the cases, there is a correlation between the grant_types and response_types—and if you pick something inconsistent, the authorization server will reject the registration request.

- client_name: A human-readable name that represents the client application. The authorization server will display the client name to the end users during the login flow. This must be informative enough so that the end users will be able to figure out the client application, during the login flow.

- client_uri: A URL that points to the client application. The authorization server will display this URL to the end users, during the login flow in a clickable manner.

- logo_uri: A URL pointing to the logo of the client application. The authorization server will display the logo to the end users, during the login flow.

- scope: A string containing a space-separated list of scope values where the client intends to request from the authorization server.

- contacts: A list of representatives from the client application end.

- tos_uri: A URL pointing to the terms of service document of the client application. The authorization server will display this link to the end users, during the login flow.

- policy_uri: A URL pointing to the privacy policy document of the client application. The authorization server will display this link to the end users, during the login flow.

- jwks_uri: Points to the endpoint, which carries the JSON Web Key (JWK) Set document with the client’s public key. Authorization server uses this public key to validate the signature of any of the requests signed by the client application. If the client application cannot host its public key via an endpoint, it can share the JWKS document under the parameter jwks instead of jwks_uri. Both the parameters must not be present in a single request.

- software_id: This is similar to client_id, but there is a major difference. The client_id is generated by the authorization server and mostly used to identify the application. But the client_id can change during the lifetime of an application. In contrast, the software_id is unique to the application across its lifecycle and uniquely represents all the metadata associated with it throughout the application lifecycle.

- software_version: The version of the client application, identified by the software_id.

- software_statement: This is a special parameter in the registration request, which carries a JSON Web Token (JWT). This JWT includes all the metadata defined earlier with respect to the client. In case the same parameter is defined in JWT and also in the request outside the software_statement parameter, then the parameter within the software_statement will take the precedence.

Based on the policies of the authorization server, it can decide whether it should proceed with the registration or not. Even if it decides to go ahead with the registration, the authorization server need not accept all the suggested parameters from the client. For example, the client may suggest using both authorization_code and implicit as grant types, but the authorization server can decide what to allow. The same is true for the token_endpoint_auth_method: the authorization server can decide what to support. The following is a sample response from the authorization server:

HTTP/1.1 200 OK

Content-Type: application/json

Cache-Control: no-store

Pragma: no-cache

{

"client_id":"iuyiSgfgfhffgfh",

"client_secret":"hkjhkiiu89hknhkjhuyjhk",

"client_id_issued_at":2343276600,

"client_secret_expires_at":2503286900,

"redirect_uris":["https://client.org/callback","https://client.org/callback2"],

"grant_types":"authorization_code",

"token_endpoint_auth_method":"client_secret_basic",

}

Let’s examine the definition of each parameter:

- client_id: The generated unique identifier for the client.

- client_secret: The generated client secret corresponding to the client_id. This is optional. For example, for public clients the client_secret isn’t required.

- client_id_issued_at: The number of seconds since January 1, 1970.

- client_secret_expires_at: The number of seconds since January 1, 1970 or 0 if it does not expire.

- redirect_uris: Accepted redirect_uris.

- token_endpoint_auth_method: The accepted authentication method for the token endpoint.

> **Note**
>


The Dynamic Client Registration OAuth 2.0 profile is quite useful in mobile applications. Mobile client applications secured with OAuth have the client ID and the client secret baked into the application. These are the same for all the installations of a given application. If a given client secret is compromised, that will affect all the installations, and rogue client applications can be developed using the stolen keys. These rogue client applications can generate more traffic on the server and exceed the legitimate throttling limit, hence causing a denial of service attack. With dynamic client registration, you need not set the same client ID and client secret for all the installations of a give application. During the installation process, the application can talk to the authorization server’s registration endpoint and generate a client ID and a client secret per installation.

Token Revocation Profile

Two parties can perform OAuth token revocation. The resource owner should be able to revoke an access token issued to a client, and the client should be able to revoke an access token or a refresh token it has acquired. The Token Revocation OAuth 2.0 profile3 addresses the latter. It introduces a standard token-revoke endpoint at the authorization server end. To revoke an access token or a refresh token, the client must notify the revoke endpoint.

> **Note**
>


In October 2013, there was an attack against Buffer (a social media management service that can be used to cross-post between Facebook, Twitter, etc.). Buffer was using OAuth to access user profiles in Facebook and Twitter. Once Buffer detected that it was under attack, it revoked all its access keys from Facebook, Twitter, and other social media sites, which prevented attackers from getting access to users’ Facebook and Twitter accounts.

The client must initiate the token revocation request. The client can authenticate to the authorization server via HTTP Basic authentication (with its client ID and client secret), with mutual TLS or with any other authentication mechanism proposed by the authorization server and then talk to the revoke endpoint. The request should consist of either the access token or the refresh token and then a token_type_hint that informs the authorization server about the type of the token (access_token or refresh_token). This parameter may not be required, but the authorization server can optimize its search criteria using it.

Here is a sample request:

POST /revoke HTTP/1.1

Host: server.example.com

Content-Type: application/x-www-form-urlencoded

Authorization: Basic czZCaGRSdadsdI9iuiaHk99kjkh

token=dsd0lkjkkljkkllkdsdds&token_type_hint=access_token

In response to this request, the authorization server first must validate the client credentials and then proceed with the token revocation. If the token is a refresh token, the authorization server must invalidate all the access tokens issued for the authorization grant associated with that refresh token. If it’s an access token, it’s up to the authorization server to decide whether to revoke the refresh token or not. In most cases, it’s ideal to revoke the refresh token, too. Once the token revocation is completed successfully, the authorization server must send an HTTP 200 status code back to the client.

## Summary

- The OAuth 2.0 profiles built on top of the core framework build a security ecosystem to make OAuth 2.0 ready for enterprise grade deployments.

- OAuth 2.0 introduced two extension points via grant types and token types.

- The Token Introspection profile for OAuth 2.0 introduces a standard API at the authorization server, allowing the resource server to talk to it and retrieve token metadata.

- According to the OAuth Chain Grant Type profile, the API hosted in the first resource server must talk to the authorization server and exchange the OAuth access token it received from the client for a new one that can be used to talk to another API hosted in a second resource server.

- The OAuth 2.0 Token Exchange is a draft proposal discussed under the IETF working group at the moment, which solves a similar problem as the Chain Grant Type proposal with some improvements.

- The aim of the Dynamic Client Registration OAuth 2.0 profile is to expose an endpoint for client registration in a standard manner to facilitate on-the-fly registrations.

- The Token Revocation OAuth 2.0 profile introduces a standard token-revoke endpoint at the authorization server to revoke an access token or a refresh token by the client.

 

