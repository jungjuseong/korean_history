# 6. OpenID Connect (OIDC)

OpenID Connect는 RESTful 방식으로 식별 인터랙션하는 경량 프레임워크를 제공한다. 

## From OpenID to OIDC

OpenID는 SAML의 후속으로 웹 인증을 혁신하였다. SAML과 OpenID의 기본 원칙은 동일한다. 둘 다 웹 SSO와 크로스도메인 연대를 지원하기 위해 사용한다. OpenID는 더 커뮤니티 친화적이고 사용자 중심이며 탈중앙화되어 있다. 

OpenID가 인증에 관한 것이라면 OAuth 1.0은 위임된 인가에 관한 것이다. 둘을 합하면 사용자를 인증하는 것과 액세스 토큰을 얻는 것은 단일 스텝으로 가능하다.

구글 Step2 프로젝트는 이러한 방향의 첫번째 중요한 시도로서 OAuth를 위한 OpenID 확장을 도입한다. 나중에 이 사람들이 OpenID 파운데이션을 설립한다.

OpenID는 1.0/1.1/2.0 세 단계를 거친 1세대이고 OAuth를 위한 OpenID 확장이 2세대이다. OpenID Connect는 3세대이다.

OpenID Connect는 OpenID가 아니다. OpenID가 작동하는 방식이다.

관리할 프로파일이 많을 것이다. 전화번호나 집 주소가 바뀔때 모든 프로파일을 수정하거나 그냥 둘 것이다. OpenID는 다른 웹사이트에 흩어져 있는 프로파일 문제를 해결한다. OpenID는 프로파일을 OpenID 제공자에서만 관리하고 다른 사이트들은 OpenID에 의존되도록 한다. 의존하는 사이트에서는 여러분의 정보를 OpenID 제공자로부터 얻어온다.

웹 사이트에 로그인하려고 할 때마다 OpenID 제공자에게 페이지 전환이 된다. OpenID 제공자에서 여러분은 인증을 하고 여러분의 속성에 대한 요청을 승인 한다. 승인이 되면 요청된 속성을 갖고 돌아온다.  This goes beyond simple attribute sharing to facilitate decentralized SSO.

SSO로는 OpenID 제공자에서 한번 로그인 할뿐이다. 즉, 사이트가 처음에 OpenID 제공자에게 여러분을 보낼 때이다. 그 후부터는 다른 사이트에서 OpenID 제공자에게 보내도 자격증명을 요구하지 않고 그 전에 만들어진  인증된 세션을 사용한다. 이 인증 세션은 브라우저가 닫힐 때까지 쿠키에 또는 영속 쿠키에 저장된다. 
 
Figure 6-1

### OpenID protocol flow

사용자는 사이트 상에 그의 OpenID를 입력하여 OpenID 플로우를 시작한다. OpenID는 고유 URL이다. For example, http://prabath.myopenid.com is an OpenID. 

사용자가 그의 OpenID를 입력하면, 의존 사이트는 해당하는 OpenID 제공자를 찾는 디스커버리를 해야 한다.

The relying party performs an HTTP GET on the OpenID (which is a URL) to get back the HTML text behind it. 

For example, if you view the source that is behind http://prabath.myopenid.com, you’ll see the following tag (MyOpenID was taken down some years back). This is exactly what the relying party sees during the discovery phase. This tag indicates which OpenID provider is behind the provided OpenID:

```
<link rel="openid2.provider" href="http://www.myopenid.com/server" />
```
`OpenID` has another way of identifying the OpenID provider, other than asking for an OpenID from the end user. This is known as directed identity, and Yahoo!, Google, and many other OpenID providers used it. If a relying party uses directed identity, it already knows who the OpenID provider is, so a discovery phase isn’t needed. The relying party lists the set of OpenID providers it supports, and the user has to pick which one it wants to authenticate against.

Once the OpenID provider is discovered, the next step depends on the type of the relying party. If it’s a smart relying party, then it executes step 3 in Figure 6-1 to create an association with the OpenID provider. During the association, a shared secret key is established between the OpenID provider and the relying party. If a key is already established between the two parties, this step is skipped, even for a smart relying party. A dumb relying party always ignores step 3.

In step 5, the user is redirected to the discovered OpenID provider. In step 6, the user has to authenticate and approve the attribute request from the relying party (steps 6 and 7). Upon approval, the user is redirected back to the relying party (step 9). A key only known to the OpenID provider and the corresponding relying party signs this response from the OpenID provider. Once the relying party receives the response, if it’s a smart relying party, it validates the signature itself. The key shared during the association phase should sign the message. If it’s a dumb relying party, it directly talks to the OpenID provider in step 10 (not a browser redirect) and asks to validate the signature. The decision is passed back to the relying party in step 11, and that concludes the OpenID protocol flow.

### Amazon Still Uses OpenID 2.0

Few have noticed that Amazon still uses (at the time of this writing) OpenID for user authentication. Check it out yourself: go to www.amazon.com, and click the Sign In button. Then observe the browser address bar. You see something similar to the following, which is an OpenID authentication request:
```
https://www.amazon.com/ap/signin?_encoding=UTF8
  &openid.assoc_handle=usflex
  &openid.claimed_id=
          http://specs.openid.net/auth/2.0/identifier_select
  &openid.identity=
          http://specs.openid.net/auth/2.0/identifier_select
  &openid.mode=checkid_setup
  &openid.ns=http://specs.openid.net/auth/2.0
  &openid.ns.pape=
          http://specs.openid.net/extensions/pape/1.0
  &openid.pape.max_auth_age=0
  &openid.return_to=https://www.amazon.com/gp/yourstore/home
```

### Understanding OpenID Connect

OpenID Connect was built on top of OAuth 2.0. It introduces an identity layer on top of OAuth 2.0. This identity layer is abstracted into an ID token, which is JSON Web Token (JWT), and we talk about JWT in detail in Chapter 7. An OAuth 2.0 authorization server that supports OpenID Connect returns an ID token along with the access token.

OpenID Connect is a profile built on top of OAuth 2.0. OAuth talks about access delegation, while OpenID Connect talks about authentication. In other words, OpenID Connect builds an identity layer on top of OAuth 2.0.

Authentication is the act of confirming the truth of an attribute of a datum or entity. If I say I am Peter, I need to prove that. I can prove that with something I know, something I have, or with something I am. Once proven who I claim I am, then the system can trust me. Sometimes systems do not just want to identify end users just by the name. Name could help to identify uniquely—but how about other attributes? Before you get through the border control, you need to identify yourself—by name, by picture, and also by fingerprints and eye retina. Those are validated in real time against the data from the VISA office, which issued the VISA for you. That check will make sure it’s the same person who claimed to have the VISA that enters into the country.

That is proving your identity. Proving your identity is authentication. Authorization is about what you can do or your capabilities.

You could prove your identity at the border control by name, by picture, and also by fingerprints and eye retina—but it's your visa that decides what you can do. To enter into the country, you need to have a valid visa that has not expired. A valid visa is not a part of your identity, but a part of what you can do. What you can do inside the country depends on the visa type. What you do with a B1 or B2 visa differs from what you can do with an L1 or L2 visa. That is authorization.

OAuth 2.0 is about authorization—not about authentication. With OAuth 2.0, the client does not know about the end user (only exception is resource owner password credentials grant type, which we discussed in Chapter 4). It simply gets an access token to access a resource on behalf of the user. With OpenID Connect, the client will get an ID token along with the access token. ID token is a representation of the end user’s identity. What does it mean by securing an API with OpenID Connect? Or is it totally meaningless? OpenID Connect is at the application level or at the client level—not at the API level or at the resource server level. OpenID Connect helps client or the application to find out who the end user is, but for the API that is meaningless. The only thing API expects is the access token. If the resource owner or the API wants to find who the end user is, it has to query the authorization server or rely on a self-contained access token (which is a JWT).

### Anatomy of the ID Token

The ID token is the primary add-on to OAuth 2.0 to support OpenID Connect. It’s a JSON Web Token (JWT) that transports authenticated user information from the authorization server to the client application. Chapter 7 delves deeper into JWT. The structure of the ID token is defined by the OpenID Connect specification. The following shows a sample ID token:

``` 
{
   "iss":"https://auth.server.com",
   "sub":"prabath@apache.org",
   "aud":"67jjuyuy7JHk12",
   "nonce":"88797jgjg32332",
   "exp":1416283970,
   "iat":1416281970,
   "auth_time":1311280969,
   "acr":"urn:mace:incommon:iap:silver",
   "amr":"password",
   "azp":"67jjuyuy7JHk12"
  }
```

Let’s examine the definition of each attribute:

- iss: The token issuer’s (authorization server or identity provider) identifier in the format of an HTTPS URL with no query parameters or URL fragments. In practice, most of the OpenID Provider implementations or products let you configure an issuer you want—and also this is mostly being used as an identifier, rather than a URL. This is a required attribute in the ID token.

- sub: The token issuer or the asserting party issues the ID token for a particular entity, and the claims set embedded into the ID token normally represents this entity, which is identified by the sub parameter. The value of the sub parameter is a case-sensitive string value and is a required attribute in the ID token.

- aud: The audience of the token. This can be an array of identifiers, but it must have the OAuth client ID in it; otherwise, the client ID should be added to the azp parameter, which we discuss later in this section. Prior to any validation check, the OpenID client must first see whether the particular ID token is issued for its use and if not should reject immediately. In other words, you need to check whether the value of the aud attribute matches with the OpenID client’s identifier. The value of the aud parameter can be a case-sensitive string value or an array of strings. This is a required attribute in the ID token.

- nonce: A new parameter introduced by the OpenID Connect specification to the initial authorization grant request. In addition to the parameters defined in OAuth 2.0, the client application can optionally include the nonce parameter. This parameter was introduced to mitigate replay attacks. The authorization server must reject any request if it finds two requests with the same nonce value. If a nonce is present in the authorization grant request, then the authorization server must include the same value in the ID token. The client application must validate the value of the nonce once it receives the ID token from the authorization server.

- exp: Each ID token carries an expiration time. The recipient of the ID token must reject it, if that token has expired. The issuer can decide the value of the expiration time. The value of the exp parameter is calculated by adding the expiration time (from the token issued time) in seconds to the time elapsed from 1970-01-01T00:00:00Z UTC to the current time. If the token issuer’s clock is out of sync with the recipient’s clock (irrespective of their time zone), then the expiration time validation could fail. To fix that, each recipient can add a couple of minutes as the clock skew during the validation process. This is a required attribute in the ID token.

- iat: The iat parameter in the ID token indicates the issued time of the ID token as calculated by the token issuer. The value of the iat parameter is the number of seconds elapsed from 1970-01-01T00:00:00Z UTC to the current time, when the token is issued. This is a required attribute in the ID token.

- auth_time: The time at which the end user authenticates with the authorization server. If the user is already authenticated, then the authorization server won’t ask the user to authenticate back. How a given authorization server authenticates the user, and how it manages the authenticated session, is outside the scope of OpenID Connect. A user can create an authenticated session with the authorization server in the first login attempt from a different application, other than the OpenID client application. In such cases, the authorization server must maintain the authenticated time and include it in the parameter auth_time. This is an optional parameter.

- acr: Stands for authentication context class reference. The value of this parameter must be understood by both the authorization server and the client application. It gives an indication of the level of authentication. For example, if the user authenticates with a long-lived browser cookie, it is considered as level 0. OpenID Connect specification does not recommend using an authentication level of 0 to access any resource of any monetary value. This is an optional parameter.

- amr: Stands for authentication method references. It indicates how the authorization server authenticates the user. It may consist of an array of values. Both the authorization server and the client application must understand the value of this parameter. For example, if the user authenticates at the authorization server with username/password and with one-time passcode over SMS, the value of amr parameter must indicate that. This is an optional parameter.

- azp: Stands for authorized party. It’s needed when there is one audience (aud) and its value is different from the OAuth client ID. The value of azp must be set to the OAuth client ID. This is an optional parameter.

> **Note**
>
The authorization server must sign the ID token, as defined in JSON Web Signature (JWS) specification. Optionally, it can also be encrypted. Token encryption should follow the rules defined in the JSON Web Encryption (JWE) specification. If the ID token is encrypted, it must be signed first and then encrypted. This is because signing the encrypted text is questionable in many legal entities. Chapters 7 and 8 talk about JWT, JWS, and JWE.

### OpenID Connect with WSO2 Identity Server

In this exercise, you see how to obtain an OpenID Connect ID token along with an OAuth 2.0 access token. Here we run the WSO2 Identity Server as the OAuth 2.0 authorization server.

> **Note**
>
WSO2 Identity Server is a free, open source identity and entitlement management server, released under the Apache 2.0 license. At the time of this writing, the latest released version is 5.9.0 and runs on Java 8.

Follow these steps to register your application as a service provider in WSO2 Identity Server and then log in to your application via OpenID Connect:

1. Download WSO2 Identity Server 5.9.0 from http://wso2.com/products/identity-server/, set up the JAVA_HOME environment variable, and start the server from the wso2server.sh/wso2server.bat file in the WSO2_IS_HOME/bin directory. If the WSO2 Identity Server 5.9.0 isn’t available from the main download page, you can find it at http://wso2.com/more-downloads/identity-server/.

2. By default, the WSO2 Identity Server starts on HTTPS port 9443.

3. Log in to the Identity Server running at https://localhost:9443 with its default username and password (admin/admin).

4. To get an OAuth 2.0 client ID and a client secret for a client application, you need to register it as a service provider on the OAuth 2.0 authorization server. Choose Main ➤ Service Providers ➤ Add. Enter a name, say, oidc-app, and click Register.

5. Choose Inbound Authentication Configuration ➤ OAuth and OpenID Connect Configuration ➤ Configure.

6. Uncheck all the grant types except Code. Make sure the OAuth version is set to 2.0.

7. Provide a value for the Callback Url text box—say, https://localhost/callback—and click Add.

8. Copy the values of OAuth Client Key and the OAuth Client Secret.

9. You use cURL here instead of a full-blown web application. First you need to get an authorization code. Copy the following URL, and paste it into a browser. Replace the values of client_id and redirect_uri appropriately. Note that here we are passing the openid as the value of the scope parameter in the request. This is a must to use OpenID Connect. You’re directed to a login page where you can authenticate with admin/admin and then approve the request by the client:
```
https://localhost:9443/oauth2/authorize?
        response_type=code&scope=openid&
        client_id=NJ0LXcfdOW20EvD6DU0l0p01u_Ya&
        redirect_uri=https://localhost/callback
 
```
10. Once approved, you’re redirected back to the redirect_uri with the authorization code, as shown here. Copy the value of the authorization code:

https://localhost/callback?code=577fc84a51c2aceac2a9e2f723f0f47f

11. Now you can exchange the authorization code from the previous step for an ID token and an access token. Replace the value of client_id, client_secret, code, and redirect_uri appropriately. The value of –u is constructed as client_id:client_secret:
```
curl -v -X POST --basic
     -u NJ0LXcfdOW2...:EsSP5GfYliU96MQ6...
     -H "Content-Type: application/x-www-form-urlencoded; charset=UTF-8" -k
     -d "client_id=NJ0LXcfdOW20EvD6DU0l0p01u_Ya&
         grant_type=authorization_code&
         code=577fc84a51c2aceac2a9e2f723f0f47f&
         redirect_uri=https://localhost/callback"
         https://localhost:9443/oauth2/token
```
This results in the following JSON response:
```
{
    "scope":"openid",
    "token_type":"bearer",
    "expires_in":3299,
    "refresh_token":"1caf88a1351d2d74093f6b84b8751bb",
    "id_token":"eyJhbGciOiJub25......",
    "access_token":"6cc611211a941cc95c0c5caf1385295"
}
```
 
12. The value of id_token is base64url-encoded. Once it’s base64url-decoded, it looks like the following. Also you can use an online tool like https://jwt.io to decode the ID token:
```
{
    "alg":"none",
    "typ":"JWT"
}.
{
    "exp":1667236118,
    "azp":"NJ0LXcfdOW20EvD6DU0l0p01u_Ya",
    "sub":"admin@carbon.super",
    "aud":"NJ0LXcfdOW20EvD6DU0l0p01u_Ya",
    "iss":"https://localhost:9443/oauth2endpoints/token",
    "iat":1663636118
}
```
### OpenID Connect Request

The ID token is the heart of OpenID Connect, but that isn’t the only place where it deviates from OAuth 2.0. OpenID Connect introduced some optional parameters to the OAuth 2.0 authorization grant request. The previous exercise didn’t use any of those parameters. Let’s examine a sample authorization grant request with all the optional parameters:
```
https://localhost:9443/oauth2/authorize?response_type=code&
     scope=openid&
     client_id=NJ0LXcfdOW20EvD6DU0l0p01u_Ya&
     redirect_uri= https://localhost/callback&
     response_mode=.....&
     nonce=.....&
     display=....&
     prompt=....&
     max_age=.....&
     ui_locales=.....&
     id_token_hint=.....&
     login_hint=.....&
     acr_value=.....
```
Let’s review the definition of each attribute:

- response_mode: Determines how the authorization server sends back the parameters in the response. This is different from the response_type parameter, defined in the OAuth 2.0 core specification. With the response_type parameter in the request, the client indicates whether it expects a code or a token. In the case of an authorization code grant type, the value of response_type is set to code, whereas with an implicit grant type, the value of response_type is set to token. The response_mode parameter addresses a different concern. If the value of response_mode is set to query, the response parameters are sent back to the client as query parameters appended to the redirect_uri; and if the value is set to fragment, then the response parameters are appended to the redirect_uri as a URI fragment.

- nonce: Mitigates replay attacks. The authorization server must reject any request if it finds two requests with the same nonce value. If a nonce is present in the authorization grant request, then the authorization server must include the same value in the ID token. The client application must validate the value of the nonce once it receives the ID token from the authorization server.

- display: Indicates how the client application expects the authorization server to display the login page and the user consent page. Possible values are page, popup, touch, and wap.

- prompt: Indicates whether to display the login or the user consent page at the authorization server. If the value is none, then neither the login page nor the user consent page should be presented to the user. In other words, it expects the user to have an authenticated session at the authorization server and a preconfigured user consent. If the value is login, the authorization server must reauthenticate the user. If the value is consent, the authorization server must display the user consent page to the end user. The select_account option can be used if the user has multiple accounts on the authorization server. The authorization server must then give the user an option to select from which account he or she requires attributes.

- max_age: In the ID token there is a parameter that indicates the time of user authentication (auth_time). The max_age parameter asks the authorization server to compare that value with max_age. If it’s less than the gap between the current time and max_age (current time-max_age), the authorization server must reauthenticate the user. When the client includes the max_age parameter in the request, the authorization server must include the auth_time parameter in the ID token.

- ui_locales: Expresses the end user’s preferred language for the user interface.

- id_token_hint: An ID token itself. This could be an ID token previously obtained by the client application. If the token is encrypted, it has to be decrypted first and then encrypted back by the public key of the authorization server and then placed into the authentication request. If the value of the parameter prompt is set to none, then the id_token_hint could be present in the request, but it isn’t a requirement.

- login_hint: This is an indication of the login identifier that the end user may use at the authorization server. For example, if the client application already knows the email address or phone number of the end user, this could be set as the value of the login_hint. This helps provide a better user experience.

- acr_values: Stands for authentication context reference values. It includes a space-separated set of values that indicates the level of authentication required at the authorization server. The authorization server may or may not respect these values.

> **Note**
>
> All OpenID Connect authentication requests must have a scope parameter with the value openid.

### Requesting User Attributes

OpenID Connect defines two ways to request user attributes. The client application can either use the initial OpenID Connect authentication request to request attributes or else later talk to a UserInfo endpoint hosted by the authorization server. If it uses the initial authentication request, then the client application must include the requested claims in the claims parameter as a JSON message. The following authorization grant request asks to include the user’s email address and the given name in the ID token:
```
https://localhost:9443/oauth2/authorize?
        response_type=code&
        scope=openid&
        client_id=NJ0LXcfdOW20EvD6DU0l0p01u_Ya&
        redirect_uri=https://localhost/callback&
        claims={ "id_token":
                  {
                     "email": {"essential": true},
                     "given_name": {"essential": true},

                  }
        }
```

> **Note**
>
> The OpenID Connect core specification defines 20 standard user claims. These identifiers should be understood by all of the authorization servers and client applications that support OpenID Connect. The complete set of OpenID Connect standard claims is defined in Section 5.1 of the OpenID Connect core specification, available at http://openid.net/specs/openid-connect-core-1_0.html.

The other approach to request user attributes is via the UserInfo endpoint. The UserInfo endpoint is an OAuth 2.0-protected resource on the authorization server. Any request to this endpoint must carry a valid OAuth 2.0 token. Once again, there are two ways to get user attributes from the UserInfo endpoint. The first approach is to use the OAuth access token. With this approach, the client must specify the corresponding attribute scope in the authorization grant request. The OpenID Connect specification defines four scope values to request attributes: profile, email, address, and phone. If the scope value is set to profile, that implies that the client requests access to a set of attributes, which includes name, family_name, given_name, middle_name, nickname, preferred_username, profile, picture, website, gender, birthdate, zoneinfo, locale, and updated_at.

The following authorization grant request asks permission to access a user’s email address and phone number:

> **Note**
>
The UserInfo endpoint must support both HTTP GET and POST. All communication with the UserInfo endpoint must be over Transport Layer Security (TLS).
```
https://localhost:9443/oauth2/authorize?
        response_type=code
        &scope=openid phone email
        &client_id=NJ0LXcfdOW20EvD6DU0l0p01u_Ya
        &redirect_uri=https://localhost/callback
```
This results in an authorization code response. Once the client application has exchanged the authorization code for an access token, by talking to the token endpoint of the authorization server, it can use the access token it received to talk to the UserInfo endpoint and get the user attributes corresponding to the access token:
```
GET /userinfo HTTP/1.1
Host: auth.server.com
Authorization: Bearer SJHkhew870hooi90
```
The preceding request to the UserInfo endpoint results in the following JSON message, which includes the user’s email address and phone number:
```
HTTP/1.1 200 OK
Content-Type: application/json
  {
   "phone": "94712841302",
   "email": "joe@authserver.com",
  }
```

The other way to retrieve user attributes from the UserInfo endpoint is through the claims parameter. The following example shows how to retrieve the email address of the user by talking to the OAuth-protected UserInfo endpoint:
```
POST /userinfo HTTP/1.1
Host: auth.server.com
Authorization: Bearer SJHkhew870hooi90
claims={ "userinfo":
                {
                    "email": {"essential": true}
                }
        }
```
> **Note**
>
Signing or encrypting the response message from the UserInfo endpoint isn’t a requirement. If it’s signed or encrypted, then the response should be wrapped in a JWT, and the Content-Type of the response should be set to application/jwt.

## OpenID Connect Flows

All the examples in this chapter so far have used an authorization code grant type to request an ID token—but it isn’t a requirement. In fact OpenID Connect, independent of OAuth 2.0 grant types, defined a set of flows: code flow, implicit flow, and hybrid flow. Each of the flows defines the value of the response_type parameter. The response_type parameter always goes with the request to the authorize endpoint (in contrast the grant_type parameter always goes to the token endpoint), and it defines the expected type of response from the authorize endpoint. If it is set to code, the authorize endpoint of the authorization server must return a code, and this flow is identified as the authorization code flow in OpenID Connect.

For implicit flow under the context of OpenID Connect, the value of response_type can be either id_token or id_token token (separated by a space). If it’s just id_token, then the authorization server returns an ID token from the authorize endpoint; if it includes both, then both the ID token and the access token are included in the response.

The hybrid flow can use different combinations. If the value of response_type is set to code id_token (separated by a space), then the response from the authorize endpoint includes the authorization code as well as the id_token. If it’s code token (separated by a space), then it returns the authorization code along with an access token (for the UserInfo endpoint). If response_type includes all three (code token id_token), then the response includes an id_token, an access token, and the authorization code. Table 6-1 summarizes this discussion.

Table 6-1

OpenID Connect Flows

Type of Flow response_type Tokens Returned

Authorization code code Authorization code

Implicit id_token ID token

Implicit id_token token ID token and access token

Hybrid code id_token ID token and authorization code

Hybrid code id_token token ID token, authorization code, and access token

Hybrid code token Access token and authorization code

> **Note**
>


When id_token is being used as the response_type in an OpenID Connect flow, the client application never has access to an access token. In such a scenario, the client application can use the scope parameter to request attributes, and those are added to the id_token.

Requesting Custom User Attributes

As discussed before, OpenID Connect defines 20 standard claims. These claims can be requested via the scope parameter or through the claims parameter. The only way to request custom-defined claims is through the claims parameter. The following is a sample OpenID Connect request that asks for custom-defined claims:

https://localhost:9443/oauth2/authorize?response_type=code

        &scope=openid

        &client_id=NJ0LXcfdOW20EvD6DU0l0p01u_Ya

        &redirect_uri=https://localhost/callback

        &claims=

          { "id_token":

           {

            "http://apress.com/claims/email": {"essential": true},

            "http://apress.com/claims/phone": {"essential": true},

           }

         }

OpenID Connect Discovery

At the beginning of the chapter, we discussed how OpenID relying parties discover OpenID providers through the user-provided OpenID (which is a URL). OpenID Connect Discovery addresses the same concern, but in a different way (see Figure 6-2). In order to authenticate users via OpenID Connect, the OpenID Connect relying party first needs to figure out what authorization server is behind the end user. OpenID Connect utilizes the WebFinger (RFC 7033) protocol for this discovery.

> **Note**
>


The OpenID Connect Discovery specification is available at http://openid.net/specs/openid-connect-discovery-1_0.html. If a given OpenID Connect relying party already knows who the authorization server is, it can simply ignore the discovery phase.

 

Figure 6-2

OpenID Connect Discovery

Let’s assume a user called Peter visits an OpenID Connect relying party and wants to log in (see Figure 6-2). To authenticate Peter, the OpenID Connect relying party should know the authorization server corresponding to Peter. To discover this, Peter has to provide to the relying party some unique identifier that relates to him. Using this identifier, the relying party should be able to find the WebFinger endpoint corresponding to Peter.

Let’s say that the identifier Peter provides is his email address, peter@apress.com (step 1). The relying party should be able to find enough detail about the WebFinger endpoint using Peter’s email address. In fact, the relying party should be able to derive the WebFinger endpoint from the email address. The relying party can then send a query to the WebFinger endpoint to find out which authorization server (or the identity provider) corresponds to Peter (steps 2 and 3). This query is made according to the WebFinger specification. The following shows a sample WebFinger request for peter@apress.com:

GET /.well-known/webfinger?resource=acct:peter@apress.com

&rel=http://openid.net/specs/connect/1.0/issuer HTTP/1.1

Host: apress.com

The WebFinger request has two key parameters: resource and rel. The resource parameter should uniquely identify the end user, whereas the value of rel is fixed for OpenID Connect and must be equal to http://openid.net/specs/connect/1.0/issuer. The rel (relation-type) parameter acts as a filter to determine the OpenID Connect issuer corresponding to the given resource.

A WebFinger endpoint can accept many other discovery requests for different services. If it finds a matching entry, the following response is returned to the OpenID Connect relying party. The value of the OpenID identity provider or the authorization server endpoint is included in the response:
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Content-Type: application/jrd+json
{
    "subject":"acct:peter@apress.com",
    "links":[
              {
                 "rel":"http://openid.net/specs/connect/1.0/issuer",
                 "href":"https://auth.apress.com"
              }
            ]
}
```
> **Note**
>


Neither the WebFinger nor the OpenID Connect Discovery specification mandates the use of the email address as the resource or the end user identifier. It must be a URI that conforms to the URI definition in RFC 3986, which can be used to derive the WebFinger endpoint. If the resource identifier is an email address, then it must be prefixed with acct.

The acct is a URI scheme as defined in http://tools.ietf.org/html/draft-ietf-appsawg-acct-uri-07. When the acct URI scheme is being used, everything after the @ sign is treated as the hostname. The WebFinger hostname is derived from an email address as per the acct URI scheme, which is the part after the @ sign.

If a URL is being used as the resource identifier, the hostname (and port number) of the URL is treated as the WebFinger hostname. If the resource identifier is https://auth.server.com:9443/prabath, then the WebFinger hostname is auth.server.com:9443.

Once the endpoint of the identity provider is discovered, that concludes the role of WebFinger. Yet you don’t have enough data to initiate an OpenID Connect authentication request with the corresponding identity provider. You can find more information about the identity provider by talking to its metadata endpoint, which must be a well-known endpoint (steps 4 and 5 in Figure 6-2). After that, for the client application to talk to the authorization server, it must be a registered client application. The client application can talk to the client registration endpoint of the authorization server (steps 6 and 7) to register itself—and then can access the authorize and token endpoints (steps 8 and 9).

> **Note**
>


Both the WebFinger and OpenID Connect Discovery specifications use the Defining Well-Known URIs (http://tools.ietf.org/html/rfc5785) specification to define endpoint locations. The RFC 5785 specification introduces a path prefix called /.well-known/ to identify well-known locations. Most of the time, these locations are metadata endpoints or policy endpoints.

The WebFinger specification has the well-known endpoint /.well-known/webfinger. The OpenID Connect Discovery specification has the well-known endpoint for OpenID provider configuration metadata, /.well-known/openid-configuration.

OpenID Connect Identity Provider Metadata

An OpenID Connect identity provider, which supports metadata discovery, should host its configuration at the endpoint /.well-known/openid-configuration. In most cases, this is a nonsecured endpoint, which can be accessed by anyone. An OpenID Connect relying party can send an HTTP GET to the metadata endpoint to retrieve the OpenID provider configuration details as follows:

GET /.well-known/openid-configuration HTTP/1.1

Host: auth.server.com

This results in the following JSON response, which includes everything an OpenID Connect relying party needs to know to talk to the OpenID provider or the OAuth authorization server:

HTTP/1.1 200 OK

Content-Type: application/json

{

  "issuer":"https://auth.server.com",

  "authorization_endpoint":"https://auth.server.com/connect/authorize",

  "token_endpoint":"https://auth.server.com/connect/token",

  "token_endpoint_auth_methods_supported":["client_secret_basic", "private_key_jwt"],

  "token_endpoint_auth_signing_alg_values_supported":["RS256", "ES256"],

  "userinfo_endpoint":"https://auth.sever.com/connect/userinfo",

  "check_session_iframe":"https://auth.server.com/connect/check_session",

  "end_session_endpoint":"https://auth.server.com/connect/end_session",

  "jwks_uri":"https://auth.server.com/jwks.json",

  "registration_endpoint":"https://auth.server.com/connect/register",

  "scopes_supported":["openid", "profile", "email", "address", "phone", "offline_access"],

  "response_types_supported":["code", "code id_token", "id_token", "token id_token"],

  "acr_values_supported":["urn:mace:incommon:iap:silver", "urn:mace:incommon:iap:bronze"],

  "subject_types_supported":["public", "pairwise"],

  "userinfo_signing_alg_values_supported":["RS256", "ES256", "HS256"],

  "userinfo_encryption_alg_values_supported":["RSA1_5", "A128KW"],

  "userinfo_encryption_enc_values_supported":["A128CBC-HS256", "A128GCM"],

  "id_token_signing_alg_values_supported":["RS256", "ES256", "HS256"],

  "id_token_encryption_alg_values_supported":["RSA1_5", "A128KW"],

  "id_token_encryption_enc_values_supported":["A128CBC-HS256", "A128GCM"],

  "request_object_signing_alg_values_supported":["none", "RS256", "ES256"],

  "display_values_supported":["page", "popup"],

  "claim_types_supported":["normal", "distributed"],

  "claims_supported":["sub", "iss", "auth_time", "acr",

                       "name", "given_name", "family_name", "nickname",

                       "profile", "picture", "website","email",

                       "email_verified",

                       "locale", "zoneinfo",

                       "http://example.info/claims/groups"],

  "claims_parameter_supported":true,  "service_documentation":"http://auth.server.com/connect/service_documentation.html",

  "ui_locales_supported":["en-US", "fr-CA"]

}

> **Note**
>


If the endpoint of the discovered identity provider is https://auth.server.com, then the OpenID provider metadata should be available at https://auth.server.com/.well-known/openid-configuration. If the endpoint is https://auth.server.com/openid, then the metadata endpoint is https://auth.server.com/openid/.well-known/openid-configuration.

Dynamic Client Registration

Once the OpenID provider endpoint is discovered via WebFinger (and all the metadata related to it through OpenID Connect Discovery), the OpenID Connect relying party still needs to have a client ID and a client secret (not under the implicit grant type) registered at the OpenID provider to initiate the authorization grant request or the OpenID Connect authentication request. The OpenID Connect Dynamic Client Registration specification2 facilitates a mechanism to register dynamically OpenID Connect relying parties at the OpenID provider.

The response from the OpenID provider metadata endpoint includes the endpoint for client registration under the parameter registration_endpoint. To support dynamic client registrations, this endpoint should accept open registration requests, with no authentication requirements.

To fight against denial of service (DoS) attacks, the endpoint can be protected with rate limits or with a web application firewall (WAF). To initiate client registration, the OpenID relying party sends an HTTP POST message to the registration endpoint with its own metadata.

The following is a sample client registration request:
```
POST /connect/register HTTP/1.1
Content-Type: application/json
Accept: application/json
Host: auth.server.com
{
"application_type":"web","redirect_uris":["https://app.client.org/callback","https://app.client.org/callback2"],
"client_name":"Foo",
"logo_uri":"https://app.client.org/logo.png",
"subject_type":"pairwise",
"sector_identifier_uri":"https://other.client.org /file_of_redirect_uris.json",
"token_endpoint_auth_method":"client_secret_basic",
"jwks_uri":"https://app.client.org/public_keys.jwks",
"userinfo_encrypted_response_alg":"RSA1_5",
"userinfo_encrypted_response_enc":"A128CBC-HS256",
"contacts":["prabath@wso2.com", "prabath@apache.org"],
"request_uris":["https://app.client.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"]
}
```
In response, the OpenID Connect provider or the authorization server sends back the following JSON message. It includes a client_id and a client_secret:

HTTP/1.1 201 Created

Content-Type: application/json

Cache-Control: no-store

Pragma: no-cache

{

"client_id":"Gjjhj678jhkh89789ew",

"client_secret":"IUi989jkjo_989klkjuk89080kjkuoikjkUIl",

"client_secret_expires_at":2590858900,

"registration_access_token":"this.is.an.access.token.value.ffx83",

"registration_client_uri":"https://auth.server.com/connect/register?client_id=Gjjhj678jhkh89789ew ",

"token_endpoint_auth_method":"client_secret_basic",

"application_type": "web",

"redirect_uris":["https://app.client.org/callback","https://app.client.org/callback2"],

"client_name":"Foo",

"logo_uri":"https://client.example.org/logo.png",

"subject_type":"pairwise",

"sector_identifier_uri":"https://other.client.org/file_of_redirect_uris.json",

"jwks_uri":"https://app.client.org/public_keys.jwks",

"userinfo_encrypted_response_alg":"RSA1_5",

"userinfo_encrypted_response_enc":"A128CBC-HS256",

"contacts":["prabath@wso2.com", "prabath@apache.org"],

"request_uris":["https://app.client.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA"]

}

Once the OpenID Connect relying party obtains a client ID and a client secret, it concludes the OpenID Connect Discovery phase. The relying party can now initiate the OpenID Connect authentication request.

> **Note**
>


Section 2.0 of the OpenID Connect Dynamic Client Registration specification lists all the attributes that can be included in an OpenID Connect client registration request: http://openid.net/specs/openid-connect-registration-1_0.html.

OpenID Connect for Securing APIs

So far, you have seen a detailed discussion about OpenID Connect. But in reality, how will it help you in securing APIs? The end users can use OpenID Connect to authenticate into web applications, mobile applications, and much more. Nonetheless, why would you need OpenID Connect to secure a headless API? At the end of the day, all the APIs are secured with OAuth 2.0, and you need to present an access token to talk to the API. The API (or the policy enforcement component) validates the access token by talking to the authorization server. Why would you need to pass an ID token to an API?

OAuth is about delegated authorization, whereas OpenID Connect is about authentication. An ID token is an assertion about your identity, that is, a proof of your identity. It can be used to authenticate into an API. As of this writing, no HTTP binding is defined for JWT.

The following example suggests passing the JWT assertion (or the ID token) to a protected API as an access token in the HTTP Authorization header. The ID token, or the signed JWT, is base64-url-encoded in three parts. Each part is separated by a dot (.). The first part up to the first dot is the JWT header. The second part is the JWT body. The third part is the signature. Once the JWT is obtained by the client application, it can place it in the HTTP Authorization header in the manner shown here:

POST /employee HTTP/1.1

Content-Type: application/json

Accept: application/json

Host: resource.server.com

Authorization: Bearer eyJhbGciOiljiuo98kljlk2KJl.IUojlkoiaos298jkkdksdosiduIUiopo.oioYJ21sajds

{

   "empl_no":"109082",

   "emp_name":"Peter John",

   "emp_address":“Mountain View, CA, USA”

}

To validate the JWT, the API (or the policy enforcement component) has to extract the JWT assertion from the HTTP Authorization header, base64-url-decode it, and validate the signature to see whether it’s signed by a trusted issuer. In addition, the claims in the JWT can be used for authentication and authorization.

> **Note**
>


When an OpenID Connect identity provider issues an ID token, it adds the aud parameter to the token to indicate the audience of the token. This can be an array of identifiers.

When using ID tokens to access APIs, a URI known to the API should also be added to the aud parameter. Currently this can’t be requested in the OpenID Connect authentication request, so it must be set out of band at the OpenID Connect identity provider.

## Summary

- OpenID Connect was built on top of OAuth 2.0. It introduces an identity layer on top of OAuth 2.0. This identity layer is abstracted into an ID token, which is a JSON Web Token (JWT).

- OpenID Connect evolved from OpenID to an OAuth 2.0 profile.

- The OpenID Connect Dynamic Client Registration specification facilitates a mechanism to register dynamically OpenID Connect relying parties at the OpenID provider.

- OpenID Connect defines two ways to request user attributes. The client application can either use the initial OpenID Connect authentication request to request attributes or else later talk to the UserInfo endpoint hosted by the authorization server.

- OpenID Connect utilizes the WebFinger protocol in its discovery process along with OpenID Connect dynamic client registration and identity provider metadata configuration.

- An OpenID Connect identity provider, which supports metadata discovery, should host its configuration at the endpoint /.well-known/openid-configuration.

 

7. Message-Level Security with JSON Web Signature

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

JavaScript Object Notation (JSON) provides a way of exchanging data in a language-neutral, text-based, and lightweight manner. It was originally derived from the ECMAScript programming language. JSON and XML are the most commonly used data exchange formats for APIs. Observing the trend over the last few years, it’s quite obvious that JSON is replacing XML. Most of the APIs out there have support for JSON, and some support both JSON and XML. XML-only APIs are quite rare.

Understanding JSON Web Token (JWT)

JSON Web Token (JWT) defines a container to transport data between interested parties in JSON. It became an IETF standard in May 2015 with the RFC 7519. The OpenID Connect specification, which we discussed in Chapter 6, uses a JWT to represent the ID token. Let’s examine an OpenID Connect ID token returned from the Google API, as an example (to understand JWT, you do not need to know about OpenID Connect):

eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc4YjRjZjIzNjU2ZGMzOTUzNjRmMWI2YzAyOTA3

NjkxZjJjZGZmZTEifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMT

EwNTAyMjUxMTU4OTIwMTQ3NzMyIiwiYXpwIjoiODI1MjQ5ODM1NjU5LXRlOHF

nbDcwMWtnb25ub21ucDRzcXY3ZXJodTEyMTFzLmFwcHMuZ29vZ2xldXNlcmNvb

nRlbnQuY29tIiwiZW1haWwiOiJwcmFiYXRoQHdzbzIuY29tIiwiYXRfaGFzaCI6InpmO

DZ2TnVsc0xCOGdGYXFSd2R6WWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkI

joiODI1MjQ5ODM1NjU5LXRlOHFnbDcwMWtnb25ub21ucDRzcXY3ZXJodTEyMTFz

LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaGQiOiJ3c28yLmNvbSIsImlhdCI6

MTQwMTkwODI3MSwiZXhwIjoxNDAxOTEyMTcxfQ.TVKv-pdyvk2gW8sGsCbsnkq

srS0T-H00xnY6ETkIfgIxfotvFn5IwKm3xyBMpy0FFe0Rb5Ht8AEJV6PdWyxz8rMgX

2HROWqSo_RfEfUpBb4iOsq4W28KftW5H0IA44VmNZ6zU4YTqPSt4TPhyFC9fP2D

_Hg7JQozpQRUfbWTJI

> **Note**
>


Way before JWT, in 2009, Microsoft introduced Simple Web Token (SWT).1 It is neither JSON nor XML. It defined its own token format to carry out a set of HTML form–encoded name/value pairs. Both JWTs and SWTs define a way to carry claims between applications. In SWT, both the claim names and claim values are strings, while in JWT claim names are strings, but claim values can be any JSON type. Both of these token types offer cryptographic protection for their content: SWTs with HMAC SHA256 and JWTs with a choice of algorithms, including signature, MAC, and encryption algorithms. Even though SWT was developed as a proposal for IETF, it never became an IETF proposed standard. Dick Hardt was the editor of the SWT specification, who also played a major role later in building the OAuth WRAP specification, which we discuss in Appendix B.

JOSE Header

The preceding JWT has three main elements. Each element is base64url-encoded and separated by a period (.). Appendix E explains how base64url encoding works in detail. Let’s identify each individual element in the JWT. The first element of the JWT is called the JavaScript Object Signing and Encryption (JOSE) header. The JOSE header lists out the properties related to the cryptographic operations applied on the JWT claims set (which we explain later in this chapter). The following is the base64url-encoded JOSE header of the preceding JWT:

eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc4YjRjZjIzNjU2ZGMzOTUzNjRmMWI2YzAyOTA3

NjkxZjJjZGZmZTEifQ

To make the JOSE header readable, we need to base64url-decode it. The following shows the base64url-decoded JOSE header, which defines two attributes, the algorithm (alg) and key identifier (kid).

{"alg":"RS256","kid":"78b4cf23656dc395364f1b6c02907691f2cdffe1"}

Both the alg and kid parameters are not defined in the JWT specification, but in the JSON Web Signature (JWS) specification. Let’s briefly identify here what these parameters mean and will discuss in detail when we explain JWS. The JWT specification is not bound to any specific algorithm. All applicable algorithms are defined under the JSON Web Algorithms (JWA) specification, which is the RFC 7518. Section 3.1 of RFC 7518 defines all possible alg parameter values for a JWS token. The value of the kid parameter provides an indication or a hint about the key, which is used to sign the message. Looking at the kid, the recipient of the message should know where to look up for the key and find it. The JWT specification only defines two parameters in the JOSE header; the following lists out those:

- typ (type): The typ parameter is used to define the media type of the complete JWT. A media type is an identifier, which defines the format of the content, transmitted over the Internet. There are two types of components that process a JWT: the JWT implementations and JWT applications. Nimbus2 is a JWT implementation in Java. The Nimbus library knows how to build and parse a JWT. A JWT application can be anything, which uses JWTs internally. A JWT application uses a JWT implementation to build or parse a JWT. The typ parameter is just another parameter for the JWT implementation. It will not try to interpret the value of it, but the JWT application would. The typ parameter helps JWT applications to differentiate the content of the JWT when the values that are not JWTs could also be present in an application data structure along with a JWT object. This is an optional parameter, and if present for a JWT, it is recommended to use JWT as the media type.

- cty (content type): The cty parameter is used to define the structural information about the JWT. It is only recommended to use this parameter in the case of a nested JWT. The nested JWTs are discussed in Chapter 8, and the definition of the cty parameter is further explained there.

JWT Claims Set

The second element of the JWT is known as either the JWT payload or the JWT claims set. It is a JSON object, which carries the business data. The following is the base64url-encoded JWT claims set of the preceding JWT (which is returned from the Google API); it includes information about the authenticated user:

eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTEwNTAyMjUxMTU4OT

IwMTQ3NzMyIiwiYXpwIjoiODI1MjQ5ODM1NjU5LXRlOHFnbDcwMWtnb25ub21uc

DRzcXY3ZXJodTEyMTFzLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1ha

WwiOiJwcmFiYXRoQHdzbzIuY29tIiwiYXRfaGFzaCI6InpmODZ2TnVsc0xCOGdGYX

FSd2R6WWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkIjoiODI1MjQ5ODM1NjU

5LXRlOHFnbDcwMWtnb25ub21ucDRzcXY3ZXJodTEyMTFzLmFwcHMuZ29vZ2xld

XNlcmNvbnRlbnQuY29tIiwiaGQiOiJ3c28yLmNvbSIsImlhdCI6MTQwMTkwODI3MS

wiZXhwIjoxNDAxOTEyMTcxfQ

To make the JWT claims set readable, we need to base64url-decode it. The following shows the base64url-decoded JWT claims set. Whitespaces can be explicitly retained while building the JWT claims set—no canonicalization is required before base64url-encoding. Canonicalization is the process of converting different forms of a message into a single standard form. This is used mostly before signing XML messages. In XML, the same message can be represented in different forms to carry the same meaning. For example, <vehicles><car></car></vehicles> and <vehicles><car/></vehicles> are equivalent in meaning, but have two different canonical forms. Before signing an XML message, you should follow a canonicalization algorithm to build a standard form.

{

     "iss":"accounts.google.com",

     "sub":"110502251158920147732",

     "azp":"825249835659-te8qgl701kgonnomnp4sqv7erhu1211s.apps.googleusercontent.com",

     "email":"prabath@wso2.com",

     "at_hash":"zf86vNulsLB8gFaqRwdzYg",

     "email_verified":true,

     "aud":"825249835659-te8qgl701kgonnomnp4sqv7erhu1211s.apps.googleusercontent.com",

     "hd":"wso2.com",

     "iat":1401908271,

     "exp":1401912171

}

The JWT claims set represents a JSON object whose members are the claims asserted by the JWT issuer. Each claim name within a JWT must be unique. If there are duplicate claim names, then the JWT parser could either return a parsing error or just return back the claims set with the very last duplicate claim. JWT specification does not explicitly define what claims are mandatory and what are optional. It’s up to each application of JWT to define mandatory and optional claims. For example, the OpenID Connect specification, which we discussed in detail in Chapter 6, defines the mandatory and optional claims.

The JWT specification defines three classes of claims: registered claims, public claims, and private claims. The registered claims are registered in the Internet Assigned Numbers Authority (IANA) JSON Web Token Claims registry. Even though these claims are treated as registered claims, the JWT specification doesn’t mandate their usage. It’s totally up to the other specifications which are built on top of JWT to decide which are mandatory and which aren’t. For example, in OpenID Connect specification, iss is a mandatory claim. The following lists out the registered claims set as defined by the JWT specification:

- iss (issuer): The issuer of the JWT. This is treated as a case-sensitive string value. Ideally, this represents the asserting party of the claims set. If Google issues the JWT, then the value of iss would be accounts.google.com. This is an indication to the receiving party who the issuer of the JWT is.

- sub (subject): The token issuer or the asserting party issues the JWT for a particular entity, and the claims set embedded into the JWT normally represents this entity, which is identified by the sub parameter. The value of the sub parameter is a case-sensitive string value.

- aud (audience): The token issuer issues the JWT to an intended recipient or a list of recipients, which is represented by the aud parameter. The recipient or the recipient list should know how to parse the JWT and validate it. Prior to any validation check, it must first see whether the particular JWT is issued for its use and if not should reject immediately. The value of the aud parameter can be a case-sensitive string value or an array of strings. The token issuer should know, prior to issuing the token, who the intended recipient (or the recipients) of the token is, and the value of the aud parameter must be a pre-agreed value between the token issuer and the recipient. In practice, one can also use a regular expression to validate the audience of the token. For example, the value of the aud in the token can be *.apress.com, while each recipient under the apress.com domain can have its own aud values: foo.apress.com, bar.apress.com likewise. Instead of finding an exact match for the aud value, each recipient can just check whether the aud value matches the regular expression: (?:[a-zA-Z0-9]*|\*).apress.com. This will make sure that any recipient can use a JWT, which is having any subdomain of apress.com.

- exp (expiration time): Each JWT carries an expiration time. The recipient of the JWT token must reject it, if that token has expired. The issuer can decide the value of the expiration time. The JWT specification does not recommend or provide any guidelines on how to decide the best token expiration time. It’s a responsibility of the other specifications, which use JWT internally to provide such recommendations. The value of the exp parameter is calculated by adding the expiration time (from the token issued time) in seconds to the time elapsed from 1970-01-01T00:00:00Z UTC to the current time. If the token issuer’s clock is out of sync with the recipient’s clock (irrespective of their time zone), then the expiration time validation could fail. To fix that, each recipient can add a couple of minutes as the clock skew during the validation process.

- nbf (not before): The recipient of the token should reject it, if the value of the nbf parameter is greater than the current time. The JWT is not good enough to use prior to the value indicated in the nbf parameter. The value of the nbf parameter is the number of seconds elapsed from 1970-01-01T00:00:00Z UTC to the not before time.

- iat (issued at): The iat parameter in the JWT indicates the issued time of the JWT as calculated by the token issuer. The value of the iat parameter is the number of seconds elapsed from 1970-01-01T00:00:00Z UTC to the current time, when the token is issued.

- jti (JWT ID): The jti parameter in the JWT is a unique token identifier generated by the token issuer. If the token recipient accepts JWTs from multiple token issuers, then this value may not be unique across all the issuers. In that case, the token recipient can maintain the token uniqueness by maintaining the tokens under the token issuer. The combination of the token issuer identifier + the jti should produce a unique token identifier.

The public claims are defined by the other specifications, which are built on top of JWT. To avoid any collisions in such cases, names should either be registered in the IANA JSON Web Token Claims registry or defined in a collision-resistant manner with a proper namespace. For example, the OpenID Connect specification defines its own set of claims, which are included inside the ID token (the ID token itself is a JWT), and those claims are registered in the IANA JSON Web Token Claims registry.

The private claims should indeed be private and shared only between a given token issuer and a selected set of recipients. These claims should be used with caution, because there is a chance for collision. If a given recipient accepts tokens from multiple token issuers, then the semantics of the same claim may be different from one issuer to another, if it is a private claim.

JWT Signature

The third part of the JWT is the signature, which is also base64url-encoded. The cryptographic parameters related to the signature are defined in the JOSE header. In this particular example, Google uses RSASSA-PKCS1-V1_53 with the SHA256 hashing algorithm, which is expressed by value of the alg parameter in the JOSE header: RS256. The following shows the signature element of the JWT returned back from Google. The signature itself is not human readable—so there is no point of trying to base64url-decode the following:

TVKv-pdyvk2gW8sGsCbsnkqsrS0TH00xnY6ETkIfgIxfotvFn5IwKm3xyBMpy0

FFe0Rb5Ht8AEJV6PdWyxz8rMgX2HROWqSo_RfEfUpBb4iOsq4W28KftW5

H0IA44VmNZ6zU4YTqPSt4TPhyFC-9fP2D_Hg7JQozpQRUfbWTJI

Generating a Plaintext JWT

The plaintext JWT doesn’t have a signature. It has only two parts. The value of the alg parameter in the JOSE header must be set to none. The following Java code generates a plaintext JWT. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample01.

public static String buildPlainJWT() {

// build audience restriction list.

List<String> aud = new ArrayList<String>();

aud.add("https://app1.foo.com");

aud.add("https://app2.foo.com");

Date currentTime = new Date();

// create a claims set.

JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().

                                // set the value of the issuer.

                                issuer("https://apress.com").

                                // set the subject value - JWT belongs to // this subject.

                                subject("john").

                                // set values for audience restriction.

                                audience(aud).

                                // expiration time set to 10 minutes.

                                expirationTime(new Date(new Date().getTime() + 1000 - 60 - 10)).

                                // set the valid from time to current time.

                                notBeforeTime(currentTime).

                                // set issued time to current time.

                                issueTime(currentTime).

                                // set a generated UUID as the JWT // identifier.

                                jwtID(UUID.randomUUID().toString()).

                                build();

// create plaintext JWT with the JWT claims.

PlainJWT plainJwt = new PlainJWT(jwtClaims);

// serialize into string.

String jwtInText = plainJwt.serialize();

// print the value of the JWT.

System.out.println(jwtInText);

return jwtInText;

}

To build and run the program, execute the following Maven command from the ch07/sample01 directory.

\> mvn test -Psample01

The preceding code produces the following output, which is a JWT. If you run the code again and again, you may not get the same output as the value of the currentTime variable changes every time you run the program:

eyJhbGciOiJub25lIn0.eyJleHAiOjE0MDIwMzcxNDEsInN1YiI6ImpvaG4iLCJuYm

YiOjE0MDIwMzY1NDEsImF1ZCI6WyJodHRwczpcL1wvYXBwMS5mb28uY29tIi

wiaHR0cHM6XC9cL2FwcDIuZm9vLmNvbSJdLCJpc3MiOiJodHRwczpcL1wvYX

ByZXNzLmNvbSIsImp0aSI6IjVmMmQzM2RmLTEyNDktNGIwMS04MmYxLWJl

MjliM2NhOTY4OSIsImlhdCI6MTQwMjAzNjU0MX0.

The following Java code shows how to parse a base64url-encoded JWT. This code would ideally run at the JWT recipient end:

public static PlainJWT parsePlainJWT() throws ParseException {

        // get JWT in base64url-encoded text.

        String jwtInText = buildPlainJWT();

        // build a plain JWT from the bade64url-encoded text.

        PlainJWT plainJwt  = PlainJWT.parse(jwtInText);

        // print the JOSE header in JSON.

        System.out.println(plainJwt.getHeader().toString());

        // print JWT body in JSON.

        System.out.println(plainJwt.getPayload().toString());

        return plainJwt;

}

This code produces the following output, which includes the parsed JOSE header and the payload:

{"alg":"none"}

{

   "exp":1402038339,

   "sub":"john",

   "nbf":1402037739,

   "aud":["https:\/\/app1.foo.com","https:\/\/app2.foo.com"],

   "iss":"https:\/\/apress.com",

   "jti":"1e41881f-7472-4030-8132-856ccf4cbb25",

   "iat":1402037739

}

Jose Working Group

Many working groups within the IETF work directly with JSON, including the OAuth working group and the System for Cross-domain Identity Management (SCIM) working group. The SCIM working group is building a provisioning standard based on JSON. Outside the IETF, the OASIS XACML working group is working on building a JSON profile for XACML 3.0.

The OpenID Connect specification, which is developed under the OpenID Foundation, is also heavily based on JSON. Due to the rise of standards built around JSON and the heavy usage of JSON for data exchange in APIs, it has become absolutely necessary to define how to secure JSON messages at the message level. The use of Transport Layer Security (TLS) only provides confidentiality and integrity at the transport layer. The JOSE working group, formed under the IETF, has the goal of standardizing integrity protection and confidentiality as well as the format for keys and algorithm identifiers to support interoperability of security services for protocols that use JSON. JSON Web Signature (RFC 7515), JSON Web Encryption (RFC 7516), JSON Web Key (RFC 7517), and JSON Web Algorithms (RFC 7518) are four IETF proposed standards, which were developed under the JOSE working group.

JSON Web Signature (JWS)

The JSON Web Signature (JWS) specification, developed under the IETF JOSE working group, represents a message or a payload, which is digitally signed or MACed (when a hashing algorithm is used with HMAC). A signed message can be serialized in two ways by following the JWS specification: the JWS compact serialization and the JWS JSON serialization. The Google OpenID Connect example discussed at the beginning of this chapter uses JWS compact serialization. In fact, the OpenID Connect specification mandates to use JWS compact serialization and JWE compact serialization whenever necessary (we discuss JWE in Chapter 8). The term JWS token is used to refer to the serialized form of a payload, following any of the serialization techniques defined in the JWS specification.

> **Note**
>


JSON Web Tokens (JWTs) are always serialized with the JWS compact serialization or the JWE compact serialization. We discuss JWE (JSON Web Encryption) in Chapter 8.

JWS Compact Serialization

JWS compact serialization represents a signed JSON payload as a compact URL-safe string. This compact string has three main elements separated by periods (.): the JOSE header, the JWS payload, and the JWS signature (see Figure 7-1). If you use compact serialization against a JSON payload, then you can have only a single signature, which is computed over the complete JOSE header and JWS payload.

 

Figure 7-1

A JWS token with compact serialization

JOSE Header

The JWS specification introduces 11 parameters to the JOSE header. The following lists out the parameters carried in a JOSE header, which are related to the message signature. Out of all those parameters, the JWT specification only defines the typ and cty parameters (as we discussed before); the rest is defined by the JWS specification. The JOSE header in a JWS token carries all the parameters required by the JWS token recipient to properly validate its signature:

- alg (algorithm): The name of the algorithm, which is used to sign the JSON payload. This is a required attribute in the JOSE header. Failure to include this in the header will result in a token parsing error. The value of the alg parameter is a string, which is picked from the JSON Web Signature and Encryption Algorithms registry defined by the JSON Web Algorithms (JWA) specification. If the value of the alg parameter is not picked from the preceding registry, then it should be defined in a collision-resistant manner, but that won’t give any guarantee that the particular algorithm is identified by all JWS implementations. It’s always better to stick to the algorithms defined in the JWA specification.

- jku: The jku parameter in the JOSE header carries a URL, which points to a JSON Web Key (JWK) set. This JWK set represents a collection of JSON-encoded public keys, where one of the keys is used to sign the JSON payload. Whatever the protocol used to retrieve the key set should provide the integrity protection. If keys are retrieved over HTTP, then instead of plain HTTP, HTTPS (or HTTP over TLS) should be used. We discuss Transport Layer Security (TLS) in detail in Appendix C. The jku is an optional parameter.

- jwk: The jwk parameter in JOSE header represents the public key corresponding to the key that is used to sign the JSON payload. The key is encoded as per the JSON Web Key (JWK) specification. The jku parameter, which we discussed before, points to a link that holds a set of JWKs, while the jwk parameter embeds the key into the JOSE header itself. The jwk is an optional parameter.

- kid: The kid parameter of the JOSE header represents an identifier for the key that is used to sign the JSON payload. Using this identifier, the recipient of the JWS should be able locate the key. If the token issuer uses the kid parameter in the JOSE header to let the recipient know about the signing key, then the corresponding key should be exchanged “somehow” between the token issuer and the recipient beforehand. How this key exchange happens is out of the scope of the JWS specification. If the value of the kid parameter refers to a JWK, then the value of this parameter should match the value of the kid parameter in the JWK. The kid is an optional parameter in the JOSE header.

- x5u: The x5u parameter in the JOSE header is very much similar to the jku parameter, which we discussed before. Instead of pointing to a JWK set, the URL here points to an X.509 certificate or a chain of X.509 certificates. The resource pointed by the URL must hold the certificate or the chain of certificates in the PEM-encoded form. Each certificate in the chain must appear between the delimiters4: -----BEGIN CERTIFICATE----- and -----END CERTIFICATE-----. The public key corresponding to the key used to sign the JSON payload should be the very first entry in the certificate chain, and the rest is the certificates of intermediate CAs (certificate authority) and the root CA. The x5u is an optional parameter in the JOSE header.

- x5c: The x5c parameter in the JOSE header represents the X.509 certificate (or the certificate chain), which corresponds to the private key, which is used to sign the JSON payload. This is similar to the jwk parameter we discussed before, but in this case, instead of a JWK, it’s an X.509 certificate (or a chain of certificates). The certificate or the certificate chain is represented in a JSON array of certificate value strings. Each element in the array should be a base64-encoded DER PKIX certificate value. The public key corresponding to the key used to sign the JSON payload should be the very first entry in the JSON array, and the rest is the certificates of intermediate CAs (certificate authority) and the root CA. The x5c is an optional parameter in the JOSE header.

- x5t: The x5t parameter in the JOSE header represents the base64url-encoded SHA-1 thumbprint of the X.509 certificate corresponding to the key used to sign the JSON payload. This is similar to the kid parameter we discussed before. Both these parameters are used to locate the key. If the token issuer uses the x5t parameter in the JOSE header to let the recipient know about the signing key, then the corresponding key should be exchanged “somehow” between the token issuer and the recipient beforehand. How this key exchange happens is out of the scope of the JWS specification. The x5t is an optional parameter in the JOSE header.

- x5t#s256: The x5t#s256 parameter in the JOSE header represents the base64url-encoded SHA256 thumbprint of the X.509 certificate corresponding to the key used to sign the JSON payload. The only difference between x5t#s256 and the x5t is the hashing algorithm. The x5t#s256 is an optional parameter in the JOSE header.

- typ: The typ parameter in the JOSE header is used to define the media type of the complete JWS. There are two types of components that process a JWS: JWS implementations and JWS applications. Nimbus5 is a JWS implementation in Java. The Nimbus library knows how to build and parse a JWS. A JWS application can be anything, which uses JWS internally. A JWS application uses a JWS implementation to build or parse a JWS. In this case, the typ parameter is just another parameter for the JWS implementation. It will not try to interpret the value of it, but the JWS application would. The typ parameter will help JWS applications to differentiate the content when multiple types of objects are present. For a JWS token using JWS compact serialization and for a JWE token using JWE compact serialization, the value of the typ parameter is JOSE, and for a JWS token using JWS JSON serialization and for a JWE token using JWE JSON serialization, the value is JOSE+JSON. (JWS serialization is discussed later in this chapter, and JWE serialization is discussed in Chapter 8). The typ is an optional parameter in the JOSE header.

- cty: The cty parameter in the JOSE header is used to represent the media type of the secured content in the JWS. It is only recommended to use this parameter in the case of a nested JWT. The nested JWT is discussed later in Chapter 8, and the definition of the cty parameter is further explained there. The cty is an optional parameter in the JOSE header.

- crit: The crit parameter in the JOSE header is used to indicate the recipient of the JWS that the presence of custom parameters, which neither defined by the JWS or JWA specifications, in the JOSE header. If these custom parameters are not understood by the recipient, then the JWS token will be treated as invalid. The value of the crit parameter is a JSON array of names, where each entry represents a custom parameter. The crit is an optional parameter in the JOSE header.

Out of all the 11 parameters defined earlier, 7 talk about how to reference the public key corresponding to the key, which is used to sign the JSON payload. There are three ways of referencing a key: external reference, embedded, and key identifier. The jku and x5u parameters fall under the external reference category. Both of them reference the key through a URI. The jwk and x5c parameters fall under embedded reference category. Each one of them defines how to embed the key to the JOSE header itself. The kid, x5t, and x5t#s256 parameters fall under the key identifier reference category. All three of them define how to locate the key using an identifier. Then again all the seven parameters can further divide into two categories based on the representation of the key: JSON Web Key (JWK) and X.509. The jku, jwk, and kid fall under the JWK category, while x5u, x5c, x5t, and x5t#s256 fall under the X.509 category. In the JOSE header of a given JWS token, at a given time, we only need to have one from the preceding parameters.

> **Note**
>


If any of the jku, jwk, kid, x5u, x5c, x5t, and x5t#s256 are present in the JOSE header, those must be integrity protected. Failure to do so will let an attacker modify the key used to sign the message and change the content of the message payload. After validating the signature of a JWS token, the recipient application must check whether the key associated with the signature is trusted. Checking whether the recipient knows the corresponding key can do the trust validation.

The JWS specification does not restrict applications only to use 11 header parameters defined earlier. There are two ways to introduce new header parameters: public header names and private header names. Any header parameter that is intended to use in the public space should be introduced in a collision-resistant manner. It is recommended to register such public header parameters in the IANA JSON Web Signature and Encryption Header Parameters registry. The private header parameters are mostly used in a restricted environment, where both the token issuer and the recipients are well aware of each other. These parameters should be used with caution, because there is a chance for collision. If a given recipient accepts tokens from multiple token issuers, then the semantics of the same parameter may be different from one issuer to another, if it is a private header. In either case, whether it’s a public or a private header parameter, if it is not defined in the JWS or the JWA specification, the header name should be included in the crit header parameter, which we discussed before.

JWS Payload

The JWS payload is the message that needs to be signed. The message can be anything—need not be a JSON payload. If it is a JSON payload, then it could contain whitespaces and/or line breaks before or after any JSON value. The second element of the serialized JWS token carries the base64url-encoded value of the JWS payload.

JWS Signature

The JWS signature is the digital signature or the MAC, which is calculated over the JWS payload and the JOSE header. The third element of the serialized JWS token carries the base64url-encoded value of the JWS signature.

The Process of Signing (Compact Serialization)

We discussed about all the ingredients that are required to build a JWS token under compact serialization. The following discusses the steps involved in building a JWS token. There are three elements in a JWS token; the first element is produced by step 2, the second element is produced by step 4, and the third element is produced by step 7.

1. 1.

Build a JSON object including all the header parameters, which express the cryptographic properties of the JWS token—this is known as the JOSE header. As discussed before in this chapter, under the section “JOSE Header,” the token issuer should advertise in the JOSE header the public key corresponding to the key used to sign the message. This can be expressed via any of these header parameters: jku, jwk, kid, x5u, x5c, x5t, and x5t#s256.

 

2. 2.

Compute the base64url-encoded value against the UTF-8 encoded JOSE header from step 1 to produce the first element of the JWS token.

 

3. 3.

Construct the payload or the content to be signed—this is known as the JWS payload. The payload is not necessarily JSON—it can be any content.

 

4. 4.

Compute the base64url-encoded value of the JWS payload from step 3 to produce the second element of the JWS token.

 

5. 5.

Build the message to compute the digital signature or the MAC. The message is constructed as ASCII(BASE64URL-ENCODE(UTF8(JOSE Header)) . BASE64URL-ENCODE(JWS Payload)).

 

6. 6.

Compute the signature over the message constructed in step 5, following the signature algorithm defined by the JOSE header parameter alg. The message is signed using the private key corresponding to the public key advertised in the JOSE header.

 

7. 7.

Compute the base64url-encoded value of the JWS signature produced in step 6, which is the third element of the serialized JWS token.

 

8. 8.

Now we have all the elements to build the JWS token in the following manner. The line breaks are introduced only for clarity.

BASE64URL(UTF8(JWS Protected Header)).

BASE64URL(JWS Payload).

BASE64URL(JWS Signature)

 

JWS JSON Serialization

In contrast to the JWS compact serialization , the JWS JSON serialization can produce multiple signatures over the same JWS payload along with different JOSE header parameters. The ultimate serialized form under JWS JSON serialization wraps the signed payload in a JSON object, with all related metadata. This JSON object includes two top-level elements, payload and signatures, and three subelements under the signatures element: protected, header, and signature. The following is an example of a JWS token, which is serialized with JWS JSON serialization. This is neither URL safe nor optimized for compactness. It carries two signatures over the same payload, and each signature and the metadata around it are stored as an element in the JSON array, under the signatures top-level element. Each signature uses a different key to sign, represented by the corresponding kid header parameter. The JSON serialization is also useful in selectively signing JOSE header parameters. In contrast, JWS compact serialization signs the complete JOSE header:

{

"payload":"eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzOD",

"signatures":[

               {

                  "protected":"eyJhbGciOiJSUzI1NiJ9",

                  "header":{"kid":"2014-06-29"},

                  "signature":"cC4hiUPoj9Eetdgtv3hF80EGrhuB"

               },

               {

                  "protected":"eyJhbGciOiJFUzI1NiJ9",

                  "header":{"kid":"e909097a-ce81-4036-9562-d21d2992db0d"},

                  "signature":"DtEhU3ljbEg8L38VWAfUAqOyKAM"

               }

             ]

}

JWS Payload

The payload top-level element of the JSON object includes the base64url-encoded value of the complete JWS payload. The JWS payload necessarily need not be a JSON payload, it can be of any content type. The payload is a required element in the serialized JWS token.

JWS Protected Header

The JWS Protected Header is a JSON object that includes the header parameters that have to be integrity protected by the signing or MAC algorithm. The protected parameter in the serialized JSON form represents the base64url-encoded value of the JWS Protected Header. The protected is not a top-level element of the serialized JWS token. It is used to define elements in the signatures JSON array and includes the base64url-encoded header elements, which should be signed. If you base64url-decode the value of the first protected element in the preceding code snippet, you will see {"alg":"RS256"}. The protected parameter must be present, if there are any protected header parameters. There is one protected element for each entry of the signatures JSON array.

JWS Unprotected Header

The JWS Unprotected Header is a JSON object that includes the header parameters that are not integrity protected by the signing or MAC algorithm. The header parameter in the serialized JSON form represents the base64url-encoded value of the JWS Unprotected Header. The header is not a top-level parameter of the JSON object. It is used to define elements in the signatures JSON array. The header parameter includes unprotected header elements related to the corresponding signature, and these elements are not signed. Combining both the protected headers and unprotected headers ultimately derives the JOSE header corresponding to the signature. In the preceding code snippet, the complete JOSE header corresponding to the first entry in the signatures JSON array would be {"alg":"RS256", "kid":"2010-12-29"}. The header element is represented as a JSON object and must be present if there are any unprotected header parameters. There is one header element for each entry of the signatures JSON array.

JWS Signature

The signatures parameter of the JSON object includes an array of JSON objects, where each element includes a signature or MAC (over the JWS payload and JWS protected header) and the associated metadata. This is a required parameter. The signature subelement, which is inside each entry of the signatures array, carries the base64url-encoded value of the signature computed over the protected header elements (represented by the protected parameter) and the JWS payload. Both the signatures and signature are required parameters.

> **Note**
>


Even though JSON serialization provides a way to selectively sign JOSE header parameters, it does not provide a direct way to selectively sign the parameters in the JWS payload. Both forms of serialization mentioned in the JWS specification sign the complete JWS payload. There is a workaround for this using JSON serialization. You can replicate the payload parameters that need to be signed selectively in the JOSE header. Then with JSON serialization, header parameters can be selectively signed.

The Process of Signing (JSON Serialization)

We discussed about all the ingredients that are required to build a JWS token under JSON serialization. The following discusses the steps involved in building the JWS token.

1. 1.

Construct the payload or the content to be signed—this is known as the JWS payload. The payload is not necessarily JSON—it can be any content. The payload element in the serialized JWS token carries the base64url-encoded value of the content.

 

2. 2.

Decide how many signatures you would need against the payload and for each case which header parameters must be signed and which are not.

 

3. 3.

Build a JSON object including all the header parameters that are to be integrity protected or to be signed. In other words, construct the JWS Protected Header for each signature. The base64url-encoded value of the UTF-8 encoded JWS Protected Header will produce the value of the protected subelement inside the signatures top-level element of the serialized JWS token.

 

4. 4.

Build a JSON object including all the header parameters that need not be integrity protected or not be signed. In other words, construct the JWS Unprotected Header for each signature. This will produce the header subelement inside the signatures top-level element of the serialized JWS token.

 

5. 5.

Both the JWS Protected Header and the JWS Unprotected Header express the cryptographic properties of the corresponding signature (there can be more than one signature element)—this is known as the JOSE header. As discussed before in this chapter, under the section “JOSE Header,” the token issuer should advertise in the JOSE header the public key corresponding to the key used to sign the message. This can be expressed via any of these header parameters: jku, jwk, kid, x5u, x5c, x5t, and x5t#s256.

 

6. 6.

Build the message to compute the digital signature or the MAC against each entry in the signatures JSON array of the serialized JWS token. The message is constructed as ASCII(BASE64URL-ENCODE(UTF8(JWS Protected Header)). BASE64URL-ENCODE(JWS Payload)).

 

7. 7.

Compute the signature over the message constructed in step 6, following the signature algorithm defined by the header parameter alg. This parameter can be either inside the JWS Protected Header or the JWS Unprotected Header. The message is signed using the private key corresponding to the public key advertised in the header.

 

8. 8.

Compute the base64url-encoded value of the JWS signature produced in step 7, which will produce the value of the signature subelement inside the signatures top-level element of the serialized JWS token.

 

9. 9.

Once all the signatures are computed, the signatures top-level element can be constructed and will complete the JWS JSON serialization.

 

Signature Types

The XML Signature specification, which was developed under W3C, proposes three types of signatures: enveloping, enveloped, and detached. These three kinds of signatures are only discussed under the context of XML.

With the enveloping signature, the XML content to be signed is inside the signature itself. That is, inside the <ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#"> element.

With the enveloped signature, the signature is inside the XML content to be signed. In other words, the <ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#"> element is inside the parent element of the XML payload to be signed.

With the detached signature, there is no parent-child relationship between the XML content to be signed and the corresponding signature. They are detached from each other.

For anyone who is familiar with XML Signature, all the signatures defined in the JWS specification can be treated as detached signatures.

> **Note**
>


The XML Signature specification by W3C only talks about signing an XML payload. If you have to sign any content, then first you need to embed that within an XML payload and then sign. In contrast, the JWS specification is not just limited to JSON. You can sign any content with JWS without wrapping it inside a JSON payload.

Generating a JWS Token with HMAC-SHA256 with a JSON Payload

The following Java code generates a JWS token with HMAC-SHA256. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample02.

The method buildHmacSha256SignedJWT() in the code should be invoked by passing a secret value that is used as the shared key to sign. The length of the secret value must be at least 256 bits:

public static String buildHmacSha256SignedJSON(String sharedSecretString) throws JOSEException {

// build audience restriction list.

List<String> aud = new ArrayList<String>();

aud.add("https://app1.foo.com");

aud.add("https://app2.foo.com");

Date currentTime = new Date();

// create a claims set.

JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().

                                // set the value of the issuer.

                                issuer("https://apress.com").

                                // set the subject value - JWT belongs to // this subject.

                                subject("john").

                                // set values for audience restriction.

                                audience(aud).

                                // expiration time set to 10 minutes.

                                expirationTime(new Date(new Date().getTime() + 1000 - 60 - 10)).

                                // set the valid from time to current time.

                                notBeforeTime(currentTime).

                                // set issued time to current time.

                                issueTime(currentTime).

                                // set a generated UUID as the JWT // identifier.

                                jwtID(UUID.randomUUID().toString()).

                                build();

// create JWS header with HMAC-SHA256 algorithm.

JWSHeader jswHeader = new JWSHeader(JWSAlgorithm.HS256);

// create signer with the provider shared secret.

JWSSigner signer = new MACSigner(sharedSecretString);

// create the signed JWT with the JWS header and the JWT body.

SignedJWT signedJWT = new SignedJWT(jswHeader, jwtClaims);

// sign the JWT with HMAC-SHA256.

signedJWT.sign(signer);

// serialize into base64url-encoded text.

String jwtInText = signedJWT.serialize();

// print the value of the JWT.

System.out.println(jwtInText);

return jwtInText;

}

To build and run the program, execute the following Maven command from the ch07/sample02 directory.

\> mvn test -Psample02

The preceding code produces the following output, which is a signed JSON payload (a JWS). If you run the code again and again, you may not get the same output as the value of the currentTime variable changes every time you run the program:

eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0MDIwMzkyOTIsInN1YiI6ImpvaG4iLCJuYm

YiOjE0MDIwMzg2OTIsImF1ZCI6WyJodHRwczpcL1wvYXBwMS5mb28uY29tIiw

iaHR0cHM6XC9cL2FwcDIuZm9vLmNvbSJdLCJpc3MiOiJodHRwczpcL1wvYXBy

ZXNzLmNvbSIsImp0aSI6ImVkNjkwN2YwLWRlOGEtNDMyNi1hZDU2LWE5ZmE

5NjA2YTVhOCIsImlhdCI6MTQwMjAzODY5Mn0.3v_pa-QFCRwoKU0RaP7pLOox

T57okVuZMe_A0UcqQ8

The following Java code shows how to validate the signature of a signed JSON message with HMAC-SHA256. To do that, you need to know the shared secret used to sign the JSON payload:

public static boolean isValidHmacSha256Signature()

                                       throws JOSEException, ParseException {

        String sharedSecretString = "ea9566bd-590d-4fe2-a441-d5f240050dbc";

        // get signed JWT in base64url-encoded text.

        String jwtInText = buildHmacSha256SignedJWT(sharedSecretString);

        // create verifier with the provider shared secret.

        JWSVerifier verifier = new MACVerifier(sharedSecretString);

        // create the signed JWS token with the base64url-encoded text.

        SignedJWT signedJWT = SignedJWT.parse(jwtInText);

        // verify the signature of the JWS token.

        boolean isValid = signedJWT.verify(verifier);

        if (isValid) {

            System.out.println("valid JWT signature");

        } else {

            System.out.println("invalid JWT signature");

        }

        return isValid;

}

Generating a JWS Token with RSA-SHA256 with a JSON Payload

The following Java code generates a JWS token with RSA-SHA256. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample03. First you need to invoke the method generateKeyPair() and pass the PrivateKey(generateKeyPair().getPrivateKey()) into the method buildRsaSha256SignedJSON():

public static KeyPair generateKeyPair()

                                  throws NoSuchAlgorithmException {

        // instantiate KeyPairGenerate with RSA algorithm.

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

        // set the key size to 1024 bits.

        keyGenerator.initialize(1024);

        // generate and return private/public key pair.

        return keyGenerator.genKeyPair();

 }

 public static String buildRsaSha256SignedJSON(PrivateKey privateKey)

                                                throws JOSEException {

    // build audience restriction list.

    List<String> aud = new ArrayList<String>();

    aud.add("https://app1.foo.com");

    aud.add("https://app2.foo.com");

    Date currentTime = new Date();

    // create a claims set.

    JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().

                                // set the value of the issuer.

                                issuer("https://apress.com").

                                // set the subject value - JWT belongs to // this subject.

                                subject("john").

                                // set values for audience restriction.

                                audience(aud).

                                // expiration time set to 10 minutes.

                                expirationTime(new Date(new Date().getTime() + 1000 - 60 - 10)).

                                // set the valid from time to current time.

                                notBeforeTime(currentTime).

                                // set issued time to current time.

                                issueTime(currentTime).

                                // set a generated UUID as the JWT identifier.

                                jwtID(UUID.randomUUID().toString()).

                                build();

        // create JWS header with RSA-SHA256 algorithm.

        JWSHeader jswHeader = new JWSHeader(JWSAlgorithm.RS256);

        // create signer with the RSA private key..

        JWSSigner signer = new RSASSASigner((RSAPrivateKey)privateKey);

        // create the signed JWT with the JWS header and the JWT body.

        SignedJWT signedJWT = new SignedJWT(jswHeader, jwtClaims);

        // sign the JWT with HMAC-SHA256.

        signedJWT.sign(signer);

        // serialize into base64-encoded text.

        String jwtInText = signedJWT.serialize();

        // print the value of the JWT.

        System.out.println(jwtInText);

        return jwtInText;

}

The following Java code shows how to invoke the previous two methods:

KeyPair keyPair = generateKeyPair();

buildRsaSha256SignedJSON(keyPair.getPrivate());

To build and run the program, execute the following Maven command from the ch07/sample03 directory .

\> mvn test -Psample03

Let’s examine how to validate a JWS token signed by RSA-SHA256. You need to know the PublicKey corresponding to the PrivateKey used to sign the message:

public static boolean isValidRsaSha256Signature()

                                           throws NoSuchAlgorithmException,

                                                       JOSEException, ParseException {

        // generate private/public key pair.

        KeyPair keyPair = generateKeyPair();

        // get the private key - used to sign the message.

        PrivateKey privateKey = keyPair.getPrivate();

        // get public key - used to verify the message signature.

        PublicKey publicKey = keyPair.getPublic();

        // get signed JWT in base64url-encoded text.

        String jwtInText = buildRsaSha256SignedJWT(privateKey);

        // create verifier with the provider shared secret.

        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);

        // create the signed JWT with the base64url-encoded text.

        SignedJWT signedJWT = SignedJWT.parse(jwtInText);

        // verify the signature of the JWT.

        boolean isValid = signedJWT.verify(verifier);

        if (isValid) {

            System.out.println("valid JWT signature");

        } else {

            System.out.println("invalid JWT signature");

        }

        return isValid;

}

Generating a JWS Token with HMAC-SHA256 with a Non-JSON Payload

The following Java code generates a JWS token with HMAC-SHA256. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample04. The method buildHmacSha256SignedNonJSON() in the code should be invoked by passing a secret value that is used as the shared key to sign. The length of the secret value must be at least 256 bits:

public static String buildHmacSha256SignedJWT(String sharedSecretString)

                                                    throws JOSEException {

// create an HMAC-protected JWS object with a non-JSON payload

JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256),

                                    new Payload("Hello world!"));

// create JWS header with HMAC-SHA256 algorithm.

jwsObject.sign(new MACSigner(sharedSecretString));

// serialize into base64-encoded text.

String jwtInText = jwsObject.serialize();

// print the value of the serialzied JWS token.

System.out.println(jwtInText);

return jwtInText;

}

To build and run the program, execute the following Maven command from the ch07/sample04 directory.

\> mvn test -Psample04

The preceding code uses the JWS compact serialization and will produce the following output:

eyJhbGciOiJIUzI1NiJ9.SGVsbG8gd29ybGQh.zub7JG0FOh7EIKAgWMzx95w-nFpJdRMvUh_pMwd6wnA

## Summary

- JSON has already become the de facto message exchange format for APIs.

- Understanding JSON security plays a key role in securing APIs.

- JSON Web Token (JWT) defines a container to transport data between interested parties in a cryptographically safe manner. It became an IETF standard in May 2015 with the RFC 7519.

- Both JWS (JSON Web Signature) and JWE (JSON Web Encryption) standards are built on top of JWT.

- There are two types of serialization techniques defined by the JWS specification: compact serialization and JSON serialization.

- The JWS specification is not just limited to JSON. You can sign any content with JWS without wrapping it inside a JSON payload.

 

8. Message-Level Security with JSON Web Encryption

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

In Chapter 7, we discussed in detail the JWT (JSON Web Token) and JWS (JSON Web Signature) specifications. Both of these specifications are developed under the IETF JOSE working group. This chapter focuses on another prominent standard developed by the same IETF working group for encrypting messages (not necessarily JSON payloads): JSON Web Encryption (JWE). Like in JWS, JWT is the foundation for JWE. The JWE specification standardizes the way to represent an encrypted content in a JSON-based data structure. The JWE1 specification defines two serialized forms to represent the encrypted payload: the JWE compact serialization and JWE JSON serialization. Both of these two serialization techniques are discussed in detail in the sections to follow. Like in JWS, the message to be encrypted using JWE standard need not be a JSON payload, it can be any content. The term JWE token is used to refer to the serialized form of an encrypted message (any message, not just JSON), following any of the serialization techniques defined in the JWE specification.

JWE Compact Serialization

With the JWE compact serialization, a JWE token is built with five key components, each separated by periods (.): JOSE header, JWE Encrypted Key, JWE Initialization Vector, JWE Ciphertext, and JWE Authentication Tag. Figure 8-1 shows the structure of a JWE token formed by JWE compact serialization.

 

Figure 8-1

A JWE token with compact serialization

JOSE Header

The JOSE header is the very first element of the JWE token produced under compact serialization. The structure of the JOSE header is the same, as we discussed in Chapter 7, other than few exceptions. The JWE specification introduces two new parameters (enc and zip), which are included in the JOSE header of a JWE token, in addition to those introduced by the JSON Web Signature (JWS) specification. The following lists out all the JOSE header parameters, which are defined by the JWE specification:

- alg (algorithm): The name of the algorithm, which is used to encrypt the Content Encryption Key (CEK). The CEK is a symmetric key, which encrypts the plaintext JSON payload. Once the plaintext is encrypted with the CEK, the CEK itself will be encrypted with another key following the algorithm identified by the value of the alg parameter. The encrypted CEK will then be included in the JWE Encrypted Key section of the JWE token. This is a required attribute in the JOSE header. Failure to include this in the header will result in a token parsing error. The value of the alg parameter is a string, which is picked from the JSON Web Signature and Encryption Algorithms registry defined by the JSON Web Algorithms2 (JWA) specification. If the value of the alg parameter is not picked from the preceding registry, then it should be defined in a collision-resistant manner, but that won’t give any guarantee that the particular algorithm is identified by all JWE implementations. It’s always better to stick to the algorithms defined in the JWA specification.

- enc: The enc parameter in the JOSE header represents the name of the algorithm, which is used for content encryption. This algorithm should be a symmetric Authenticated Encryption with Associated Data (AEAD) algorithm. This is a required attribute in the JOSE header. Failure to include this in the header will result in a token parsing error. The value of the enc parameter is a string, which is picked from the JSON Web Signature and Encryption Algorithms registry defined by the JSON Web Algorithms (JWA) specification. If the value of the enc parameter is not picked from the preceding registry, then it should be defined in a collision-resistant manner, but that won’t give any guarantee that the particular algorithm is identified by all JWE implementations. It’s always better to stick to the algorithms defined in the JWA specification.

- zip: The zip parameter in the JOSE header defines the name of the compression algorithm. The plaintext JSON payload gets compressed before the encryption, if the token issuer decides to use compression. The compression is not a must. The JWE specification defines DEF as the compression algorithm, but it’s not a must to use it. The token issuers can define their own compression algorithms. The default value of the compression algorithm is defined in the JSON Web Encryption Compression Algorithms registry under the JSON Web Algorithms (JWA) specification. This is an optional parameter.

- jku: The jku parameter in the JOSE header carries a URL, which points to a JSON Web Key (JWK)3 set. This JWK set represents a collection of JSON-encoded public keys, where one of the keys is used to encrypt the Content Encryption Key (CEK). Whatever the protocol used to retrieve the key set should provide the integrity protection. If keys are retrieved over HTTP, then instead of plain HTTP, HTTPS (or HTTP over TLS) should be used. We discuss Transport Layer Security (TLS) in detail in Appendix C. The jku is an optional parameter.

- jwk: The jwk parameter in JOSE header represents the public key corresponding to the key that is used to encrypt the Content Encryption Key (CEK). The key is encoded as per the JSON Web Key (JWK) specification.3 The jku parameter, which we discussed before, points to a link that holds a set of JWKs, while the jwk parameter embeds the key into the JOSE header itself. The jwk is an optional parameter.

- kid: The kid parameter of the JOSE header represents an identifier for the key that is used to encrypt the Content Encryption Key (CEK). Using this identifier, the recipient of the JWE should be able to locate the key. If the token issuer uses the kid parameter in the JOSE header to let the recipient know about the signing key, then the corresponding key should be exchanged “somehow” between the token issuer and the recipient beforehand. How this key exchange happens is out of the scope of the JWE specification. If the value of the kid parameter refers to a JWK, then the value of this parameter should match the value of the kid parameter in the JWK. The kid is an optional parameter in the JOSE header.

- x5u: The x5u parameter in the JOSE header is very much similar to the jku parameter, which we discussed before. Instead of pointing to a JWK set, the URL here points to an X.509 certificate or a chain of X.509 certificates. The resource pointed by the URL must hold the certificate or the chain of certificates in the PEM-encoded form. Each certificate in the chain must appear between the delimiters4: -----BEGIN CERTIFICATE----- and -----END CERTIFICATE-----. The public key corresponding to the key used to encrypt the Content Encryption Key (CEK) should be the very first entry in the certificate chain, and the rest is the certificates of intermediate CAs (certificate authority) and the root CA. The x5u is an optional parameter in the JOSE header.

- x5c: The x5c parameter in the JOSE header represents the X.509 certificate (or the certificate chain), which corresponds to the public key, which is used to encrypt the Content Encryption Key (CEK). This is similar to the jwk parameter we discussed before, but in this case instead of a JWK, it’s an X.509 certificate (or a chain of certificates). The certificate or the certificate chain is represented in a JSON array of certificate value strings. Each element in the array should be a base64-encoded DER PKIX certificate value. The public key corresponding to the key used to encrypt the Content Encryption Key (CEK) should be the very first entry in the JSON array, and the rest is the certificates of intermediate CAs (certificate authority) and the root CA. The x5c is an optional parameter in the JOSE header.

- x5t: The x5t parameter in the JOSE header represents the base64url-encoded SHA-1 thumbprint of the X.509 certificate corresponding to the key used to encrypt the Content Encryption Key (CEK). This is similar to the kid parameter we discussed before. Both these parameters are used to locate the key. If the token issuer uses the x5t parameter in the JOSE header to let the recipient know about the signing key, then the corresponding key should be exchanged “somehow” between the token issuer and the recipient beforehand. How this key exchange happens is out of the scope of the JWE specification. The x5t is an optional parameter in the JOSE header.

- x5t#s256: The x5t#s256 parameter in the JOSE header represents the base64url-encoded SHA256 thumbprint of the X.509 certificate corresponding to the key used to encrypt the Content Encryption Key (CEK). The only difference between x5t#s256 and the x5t is the hashing algorithm. The x5t#s256 is an optional parameter in the JOSE header.

- typ: The typ parameter in the JOSE header is used to define the media type of the complete JWE. There are two types of components that process a JWE: JWE implementations and JWE applications. Nimbus5 is a JWE implementation in Java. The Nimbus library knows how to build and parse a JWE. A JWE application can be anything, which uses JWE internally. A JWE application uses a JWE implementation to build or parse a JWE. In this case, the typ parameter is just another parameter for the JWE implementation. It will not try to interpret the value of it, but the JWE application would. The typ parameter will help JWE applications to differentiate the content when multiple types of objects are present. For a JWS token using JWS compact serialization and for a JWE token using JWE compact serialization, the value of the typ parameter is JOSE, and for a JWS token using JWS JSON serialization and for a JWE token using JWE JSON serialization, the value is JOSE+JSON. (JWS serialization was discussed in Chapter 7 and JWE serialization is discussed later in this chapter). The typ is an optional parameter in the JOSE header.

- cty: The cty parameter in the JOSE header is used to represent the media type of the secured content in the JWE. It is only recommended to use this parameter in the case of a nested JWT. The nested JWT is discussed later in this chapter, and the definition of the cty parameter is further explained there. The cty is an optional parameter in the JOSE header.

- crit: The crit parameter in the JOSE header is used to indicate to the recipient of the JWE that the presence of custom parameters, which neither defined by the JWE or JWA specifications, in the JOSE header. If these custom parameters are not understood by the recipient, then the JWE token will be treated as invalid. The value of the crit parameter is a JSON array of names, where each entry represents a custom parameter. The crit is an optional parameter in the JOSE header.

Out of all the 13 parameters defined earlier, 7 talk about how to reference the public key, which is used to encrypt the Content Encryption Key (CEK). There are three ways of referencing a key: external reference, embedded, and key identifier. The jku and x5u parameters fall under the external reference category. Both of them reference the key through a URI. The jwk and x5c parameters fall under embedded reference category. Each one of them defines how to embed the key to the JOSE header itself. The kid, x5t, and x5t#s256 parameters fall under the key identifier reference category. All three of them define how to locate the key using an identifier. Then again all the seven parameters can further divide into two categories based on the representation of the key: JSON Web Key (JWK) and X.509. The jku, jwk, and kid fall under the JWK category, while x5u, x5c, x5t, and x5t#s256 fall under the X.509 category. In the JOSE header of a given JWE token, at a given time, we only need to have one from the preceding parameters.

> **Note**
>


The JSON payload, which is subject to encryption, could contain whitespaces and/or line breaks before or after any JSON value.

The JWE specification does not restrict applications only to use 13 header parameters defined earlier. There are two ways to introduce new header parameters: public header names and private header names. Any header parameter that is intended to use in the public space should be introduced in a collision-resistant manner. It is recommended to register such public header parameters in the IANA JSON Web Signature and Encryption Header Parameters registry. The private header parameters are mostly used in a restricted environment, where both the token issuer and the recipients are well aware of each other. These parameters should be used with caution, because there is a chance for collision. If a given recipient accepts tokens from multiple token issuers, then the semantics of the same parameter may be different from one issuer to another, if it is a private header. In either case, whether it’s a public or a private header parameter, if it is not defined in the JWE or the JWA specification, the header name should be included in the crit header parameter, which we discussed before.

JWE Encrypted Key

To understand JWE Encrypted Key section of the JWE, we first need to understand how a JSON payload gets encrypted. The enc parameter of the JOSE header defines the content encryption algorithm, and it should be a symmetric Authenticated Encryption with Associated Data (AEAD) algorithm. The alg parameter of the JOSE header defines the encryption algorithm to encrypt the Content Encryption Key (CEK). We can also call this algorithm a key wrapping algorithm, as it wraps the CEK.

Authenticated Encryption

Encryption alone only provides the data confidentiality. Only the intended recipient can decrypt and view the encrypted data. Even though data is not visible to everyone, anyone having access to the encrypted data can change the bit stream of it to reflect a different message. For example, if Alice transfers US $100 from her bank account to Bob’s account and if that message is encrypted, then Eve in the middle can’t see what’s inside it. But, Eve can modify the bit stream of the encrypted data to change the message, let’s say from US $100 to US $150. The bank which controls the transaction would not detect this change done by Eve in the middle and will treat it as a legitimate transaction. This is why encryption itself is not always safe, and in the 1970s, this was identified as an issue in the banking industry.

Unlike just encryption, the Authenticated Encryption simultaneously provides a confidentiality, integrity, and authenticity guarantee for data. ISO/IEC 19772:2009 has standardized six different authenticated encryption modes: GCM, OCB 2.0, CCM, Key Wrap, EAX, and Encrypt-then-MAC. Authenticated Encryption with Associated Data (AEAD) extends this model to add the ability to preserve the integrity and authenticity of Additional Authenticated Data (AAD) that isn’t encrypted. AAD is also known as Associated Data (AD). AEAD algorithms take two inputs, plaintext to be encrypted and the Additional Authentication Data (AAD), and result in two outputs: the ciphertext and the authentication tag. The AAD represents the data to be authenticated, but not encrypted. The authentication tag ensures the integrity of the ciphertext and the AAD.

Let’s look at the following JOSE header. For content encryption, it uses A256GCM algorithm, and for key wrapping, RSA-OAEP:

{"alg":"RSA-OAEP","enc":"A256GCM"}

A256GCM is defined in the JWA specification. It uses the Advanced Encryption Standard (AES) in Galois/Counter Mode (GCM) algorithm with a 256-bit long key, and it’s a symmetric key algorithm used for AEAD. Symmetric keys are mostly used for content encryption. Symmetric key encryption is much faster than asymmetric key encryption. At the same time, asymmetric key encryption can’t be used to encrypt large messages. RSA-OAEP is too defined in the JWA specification. During the encryption process, the token issuer generates a random key, which is 256 bits in size, and encrypts the message using that key following the AES GCM algorithm. Next, the key used to encrypt the message is encrypted using RSA-OAEP,6 which is an asymmetric encryption scheme. The RSA-OAEP encryption scheme uses RSA algorithm with the Optimal Asymmetric Encryption Padding (OAEP) method. Finally, the encrypted symmetric key is placed in the JWE Encrypted Header section of the JWE.

Key Management Modes

The key management mode defines the method to derive or compute a value to the Content Encryption Key (CEK). The JWE specification employs five key management modes, as listed in the following, and the appropriate key management mode is decided based on the alg parameter, which is defined in the JOSE header:

1. 1.

Key encryption: With the key encryption mode, the value of the CEK is encrypted using an asymmetric encryption algorithm. For example, if the value of the alg parameter in the JOSE header is RSA-OAEP, then the corresponding key management algorithm is the RSAES OAEP using the default parameters. This relationship between the alg parameter and the key management algorithm is defined in the JWA specification. The RSAES OAEP algorithm occupies the key encryption as the key management mode to derive the value of the CEK.

 

2. 2.

Key wrapping: With the key wrapping mode, the value of the CEK is encrypted using a symmetric key wrapping algorithm. For example, if the value of the alg parameter in the JOSE header is A128KW, then the corresponding key management algorithm is the AES Key Wrap with the default initial value, which uses a 128-bit key. The AES Key Wrap algorithm occupies the key wrapping as the key management mode to derive the value of the CEK.

 

3. 3.

Direct key agreement: With the direct key agreement mode, the value of the CEK is decided based upon a key agreement algorithm. For example, if the value of the alg parameter in the JOSE header is ECDH-ES, then the corresponding key management algorithm is the Elliptic Curve Diffie-Hellman Ephemeral Static key agreement using Concat KDF. This algorithm occupies the direct key agreement as the key management mode to derive the value of the CEK.

 

4. 4.

Key agreement with key wrapping: With the direct key agreement with key wrapping mode, the value of the CEK is decided based upon a key agreement algorithm, and it is encrypted using a symmetric key wrapping algorithm. For example, if the value of the alg parameter in the JOSE header is ECDH-ES+A128KW, then the corresponding key management algorithm is the ECDH-ES using Concat KDF and CEK rapped with A128KW. This algorithm occupies the direct key agreement with key wrapping as the key management mode to derive the value of the CEK.

 

5. 5.

Direct encryption: With the direct encryption mode, the value of the CEK is the same as the symmetric key value, which is already shared between the token issuer and the recipient. For example, if the value of the alg parameter in the JOSE header is dir, then the direct encryption is occupied as the key management mode to derive the value of the CEK.

 

JWE Initialization Vector

Some encryption algorithms, which are used for content encryption, require an initialization vector, during the encryption process. Initialization vector is a randomly generated number, which is used along with a secret key to encrypt data. This will add randomness to the encrypted data, which will prevent repetition even if the same data gets encrypted using the same secret key again and again. To decrypt the message at the token recipient end, it has to know the initialization vector, hence included in the JWE token, under the JWE Initialization Vector element. If the content encryption algorithm does not require an initialization vector, then the value of this element should be kept empty.

JWE Ciphertext

The fourth element of the JWE token is the base64url-encoded value of the JWE ciphertext. The JWE ciphertext is computed by encrypting the plaintext JSON payload using the CEK, the JWE Initialization Vector, and the Additional Authentication Data (AAD) value, with the encryption algorithm defined by the header parameter enc. The algorithm defined by the enc header parameter should be a symmetric Authenticated Encryption with Associated Data (AEAD) algorithm. The AEAD algorithm, which is used to encrypt the plaintext payload, also allows specifying Additional Authenticated Data (AAD).

JWE Authentication Tag

The base64url-encoded value of the JWE Authentication Tag is the final element of the JWE token. The value of the authentication tag is produced during the AEAD encryption process, along with the ciphertext. The authentication tag ensures the integrity of the ciphertext and the Additional Authenticated Data (AAD).

The Process of Encryption (Compact Serialization)

We have discussed about all the ingredients that are required to build a JWE token under compact serialization. The following discusses the steps involved in building the JWE token. There are five elements in a JWE token; the first element is produced by step 6, the second element is produced by step 3, the third element is produced by step 4, the fourth element is produced by step 10, and the fifth element is produced by step 11.

1. 1.

Figure out the key management mode by the algorithm used to determine the Content Encryption Key (CEK) value. This algorithm is defined by the alg parameter in the JOSE header. There is only one alg parameter per JWE token.

 

2. 2.

Compute the CEK and calculate the JWE Encrypted Key based on the key management mode, picked in step 1. The CEK is later used to encrypt the JSON payload. There is only one JWE Encrypted Key element in the JWE token.

 

3. 3.

Compute the base64url-encoded value of the JWE Encrypted Key, which is produced by step 2. This is the second element of the JWE token.

 

4. 4.

Generate a random value for the JWE Initialization Vector. Irrespective of the serialization technique, the JWE token carries the value of the base64url-encoded value of the JWE Initialization Vector. This is the third element of the JWE token.

 

5. 5.

If token compression is needed, the JSON payload in plaintext must be compressed following the compression algorithm defined under the zip header parameter.

 

6. 6.

Construct the JSON representation of the JOSE header and find the base64url-encoded value of the JOSE header with UTF-8 encoding. This is the first element of the JWE token.

 

7. 7.

To encrypt the JSON payload, we need the CEK (which we already have), the JWE Initialization Vector (which we already have), and the Additional Authenticated Data (AAD). Compute ASCII value of the encoded JOSE header (step 6) and use it as the AAD.

 

8. 8.

Encrypt the compressed JSON payload (from step 5) using the CEK, the JWE Initialization Vector, and the Additional Authenticated Data (AAD), following the content encryption algorithm defined by the enc header parameter.

 

9. 9.

The algorithm defined by the enc header parameter is an AEAD algorithm, and after the encryption process, it produces the ciphertext and the Authentication Tag.

 

10. 10.

Compute the base64url-encoded value of the ciphertext, which is produced by step 9. This is the fourth element of the JWE token.

 

11. 11.

Compute the base64url-encoded value of the Authentication Tag, which is produced by step 9. This is the fifth element of the JWE token.

 

12. 12.

Now we have all the elements to build the JWE token in the following manner. The line breaks are introduced only for clarity.

BASE64URL-ENCODE(UTF8(JWE Protected Header)).

BASE64URL-ENCODE(JWE Encrypted Key).

BASE64URL-ENCODE(JWE Initialization Vector).

BASE64URL-ENCODE(JWE Ciphertext).

BASE64URL-ENCODE(JWE Authentication Tag)

 

JWE JSON Serialization

Unlike the JWE compact serialization, the JWE JSON serialization can produce encrypted data targeting at multiple recipients over the same JSON payload. The ultimate serialized form under JWE JSON serialization represents an encrypted JSON payload as a JSON object. This JSON object includes six top-level elements: protected, unprotected, recipients, iv, ciphertext, and tag. The following is an example of a JWE token, which is serialized with JWE JSON serialization:

{

    "protected":"eyJlbmMiOiJBMTI4Q0JDLUhTMjU2In0",

    "unprotected":{"jku":"https://server.example.com/keys.jwks"},

    "recipients":[

       {

        "header":{"alg":"RSA1_5","kid":"2011-04-29"},

        "encrypted_key":"UGhIOguC7IuEvf_NPVaXsGMoLOmwvc1GyqlIK..."

       },

       {

        "header":{"alg":"A128KW","kid":"7"},

        "encrypted_key":"6KB707dM9YTIgHtLvtgWQ8mKwb..."

       }

    ],

    "iv":"AxY8DCtDaGlsbGljb3RoZQ",

    "ciphertext":"KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY",

    "tag":"Mz-VPPyU4RlcuYv1IwIvzw"

}

JWE Protected Header

The JWE Protected Header is a JSON object that includes the header parameters that have to be integrity protected by the AEAD algorithm. The parameters inside the JWE Protected Header are applicable to all the recipients of the JWE token. The protected parameter in the serialized JSON form represents the base64url-encoded value of the JWE Protected Header. There is only one protected element in a JWE token at the root level, and any header parameter that we discussed before under the JOSE header can also be used under the JWE Protected Header.

JWE Shared Unprotected Header

The JWE Shared Unprotected Header is a JSON object that includes the header parameters that are not integrity protected. The unprotected parameter in the serialized JSON form represents the JWE Shared Unprotected Header. There is only one unprotected element in a JWE token at the root level, and any header parameter that we discussed before under the JOSE header can also be used under the JWE Shared Unprotected Header.

JWE Per-Recipient Unprotected Header

The JWE Per-Recipient Unprotected Header is a JSON object that includes the header parameters that are not integrity protected. The parameters inside the JWE Per-Recipient Unprotected Header are applicable only to a particular recipient of the JWE token. In the JWE token, these header parameters are grouped under the parameter recipients. The recipients parameter represents an array of recipients of the JWE token. Each member consists of a header parameter and an encryptedkey parameter .

- header: The header parameter, which is inside the recipients parameter, represents the value of the JWE header elements that aren’t protected for integrity by authenticated encryption for each recipient.

- encryptedkey: The encryptedkey parameter represents the base64url-encoded value of the encrypted key. This is the key used to encrypt the message payload. The key can be encrypted in different ways for each recipient.

Any header parameter that we discussed before under the JOSE header can also be used under the JWE Per-Recipient Unprotected Header.

JWE Initialization Vector

This carries the same meaning as explained under JWE compact serialization previously in this chapter. The iv parameter in the JWE token represents the value of the initialization vector used for encryption.

JWE Ciphertext

This carries the same meaning as explained under JWE compact serialization previously in this chapter. The ciphertext parameter in the JWE token carries the base64url-encoded value of the JWE ciphertext.

JWE Authentication Tag

This carries the same meaning as explained under JWE compact serialization previously in this chapter. The tag parameter in the JWE token carries the base64url-encoded value of the JWE Authentication Tag, which is an outcome of the encryption process using an AEAD algorithm.

The Process of Encryption (JSON Serialization)

We have discussed about all the ingredients that are required to build a JWE token under JSON serialization. The following discusses the steps involved in building the JWE token.

1. 1.

Figure out the key management mode by the algorithm used to determine the Content Encryption Key (CEK) value. This algorithm is defined by the alg parameter in the JOSE header. Under JWE JSON serialization, the JOSE header is built by the union of all the parameters defined under the JWE Protected Header, JWE Shared Unprotected Header, and Per-Recipient Unprotected Header. Once included in the Per-Recipient Unprotected Header, the alg parameter can be defined per recipient.

 

2. 2.

Compute the CEK and calculate the JWE Encrypted Key based on the key management mode, picked in step 1. The CEK is later used to encrypt the JSON payload.

 

3. 3.

Compute the base64url-encoded value of the JWE Encrypted Key, which is produced by step 2. Once again, this is computed per recipient, and the resultant value is included in the Per-Recipient Unprotected Header parameter, encryptedkey.

 

4. 4.

Perform steps 1–3 for each recipient of the JWE token. Each iteration will produce an element in the recipients JSON array of the JWE token.

 

5. 5.

Generate a random value for the JWE Initialization Vector. Irrespective of the serialization technique, the JWE token carries the value of the base64url-encoded value of the JWE Initialization Vector.

 

6. 6.

If token compression is needed, the JSON payload in plaintext must be compressed following the compression algorithm defined under the zip header parameter. The value of the zip header parameter can be defined either in the JWE Protected Header or JWE Shared Unprotected Header.

 

7. 7.

Construct the JSON representation of the JWE Protected Header, JWE Shared Unprotected Header, and Per-Recipient Unprotected Headers.

 

8. 8.

Compute the base64url-encoded value of the JWE Protected Header with UTF-8 encoding. This value is represented by the protected element in the serialized JWE token. The JWE Protected Header is optional, and if present there can be only one header. If no JWE header is present, then the value of the protected element will be empty.

 

9. 9.

Generate a value for the Additional Authenticated Data (AAD) and compute the base64url-encoded value of it. This is an optional step, and if it’s there, then the base64url-encoded AAD value will be used as an input parameter to encrypt the JSON payload, as in step 10.

 

10. 10.

To encrypt the JSON payload, we need the CEK (which we already have), the JWE Initialization Vector (which we already have), and the Additional Authenticated Data (AAD). Compute ASCII value of the encoded JWE Protected Header (step 8) and use it as the AAD. In case step 9 is done and then the value of AAD is computed as ASCII(encoded JWE Protected Header. BASE64URL-ENCODE(AAD)).

 

11. 11.

Encrypt the compressed JSON payload (from step 6) using the CEK, the JWE Initialization Vector, and the Additional Authenticated Data (AAD from step 10), following the content encryption algorithm defined by the enc header parameter.

 

12. 12.

The algorithm defined by the enc header parameter is an AEAD algorithm, and after the encryption process, it produces the ciphertext and the Authentication Tag.

 

13. 13.

Compute the base64url-encoded value of the ciphertext, which is produced by step 12.

 

14. 14.

Compute the base64url-encoded value of the Authentication Tag, which is produced by step 12.

 

Now we have all the elements to build the JWE token under JSON serialization.

> **Note**
>


The XML Encryption specification by W3C only talks about encrypting an XML payload. If you have to encrypt any content, then first you need to embed that within an XML payload and then encrypt. In contrast, the JWE specification is not just limited to JSON. You can encrypt any content with JWE without wrapping it inside a JSON payload.

Nested JWTs

Both in a JWS token and a JWE token, the payload can be of any content. It can be JSON, XML, or anything. In a Nested JWT, the payload must be a JWT itself. In other words, a JWT, which is enclosed in another JWS or JWE token, builds a Nested JWT. A Nested JWT is used to perform nested signing and encryption. The cty header parameter must be present and set to the value JWT, in the case of a Nested JWT. The following lists out the steps in building a Nested JWT, which signs a payload first using JWS and then encrypts the JWS token using JWE:

1. 1.

Build the JWS token with the payload or the content of your choice.

 

2. 2.

Based on the JWS serialization technique you use, step 1 will produce either a JSON object with JSON serialization or a three-element string where each element is separated out by a period (.)—with compact serialization.

 

3. 3.

Base64url-encode the output from step 2 and use it as the payload to be encrypted for the JWE token.

 

4. 4.

Set the value of the cty header parameter of the JWE JOSE header to JWT.

 

5. 5.

Build the JWE following any of the two serialization techniques defined in the JWE specification.

 

> **Note**
>


Sign first and then encrypt is the preferred approach in building a nested JWT, instead of sign and then encrypt. The signature binds the ownership of the content to the signer or the token issuer. It is an industry accepted best practice to sign the original content, rather than the encrypted content. Also, when sign first and encrypt the signed payload, the signature itself gets encrypted too, preventing an attacker in the middle stripping off the signature. Since the signature and all its related metadata are encrypted, an attacker cannot derive any details about the token issuer looking at the message. When encrypt first and sign the encrypted payload, then the signature is visible to anyone and also an attacker can strip it off from the message.

JWE vs. JWS

From an application developer’s point of view, it may be quite important to identify whether a given message is a JWE token or a JWS token and start processing based on that. The following lists out a few techniques that can be used to differentiate a JWS token from a JWE token:

1. 1.

When compact serialization is used, a JWS token has three base64url-encoded elements separated by periods (.), while a JWE token has five base64url-encoded elements separated by periods (.).

 

2. 2.

When JSON serialization is used, the elements of the JSON object produced are different in JWS token and JWE token. For example, the JWS token has a top-level element called payload, which is not in the JWE token, and the JWE token has a top-level element called ciphertext, which is not in the JWS token.

 

3. 3.

The JOSE header of a JWE token has the enc header parameter, while it is not present in the JOSE header of a JWS token.

 

4. 4.

The value of the alg parameter in the JOSE header of a JWS token carries a digital signature or a MAC algorithm or none, while the same parameter in the JOSE header of a JWE token carries a key encryption, key wrapping, direct key agreement, key agreement with key wrapping, or direct encryption algorithm.

 

Generating a JWE Token with RSA-OAEP and AES with a JSON Payload

The following Java code generates a JWE token with RSA-OAEP and AES. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch08/sample01—and it runs on Java 8+. First you need to invoke the method generateKeyPair() and pass the PublicKey(generateKeyPair().getPublicKey()) into the method buildEncryptedJWT():

// this method generates a key pair and the corresponding public key is used // to encrypt the message.

public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {

    // instantiate KeyPairGenerate with RSA algorithm.

    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

    // set the key size to 1024 bits.

    keyGenerator.initialize(1024);

    // generate and return private/public key pair.

    return keyGenerator.genKeyPair();

}

// this method is used to encrypt a JWT claims set using the provided public // key.

public static String buildEncryptedJWT(PublicKey publicKey) throws JOSEException {

    // build audience restriction list.

    List<String> aud = new ArrayList<String>();

    aud.add("https://app1.foo.com");

    aud.add("https://app2.foo.com");

    Date currentTime = new Date();

    // create a claims set.

    JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().

                  // set the value of the issuer.

                  issuer("https://apress.com").

                  // set the subject value - JWT belongs to this subject.

                  subject("john").

                  // set values for audience restriction.

                  audience(aud).

                  // expiration time set to 10 minutes.

                  expirationTime(new Date(new Date().getTime() + 1000 ∗ 60 ∗ 10)).

                  // set the valid from time to current time.

                  notBeforeTime(currentTime).

                  // set issued time to current time.

                  issueTime(currentTime).

                  // set a generated UUID as the JWT identifier.

                  jwtID(UUID.randomUUID().toString()).build();

    // create JWE header with RSA-OAEP and AES/GCM.

    JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

    // create encrypter with the RSA public key.

    JWEEncrypter encrypter = new RSAEncrypter((RSAPublicKey) publicKey);

    // create the encrypted JWT with the JWE header and the JWT payload.

    EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, jwtClaims);

    // encrypt the JWT.

    encryptedJWT.encrypt(encrypter);

    // serialize into base64-encoded text.

    String jwtInText = encryptedJWT.serialize();

    // print the value of the JWT.

    System.out.println(jwtInText);

    return jwtInText;

}

The following Java code shows how to invoke the previous two methods:

KeyPair keyPair = generateKeyPair();

buildEncryptedJWT(keyPair.getPublic());

To build and run the program, execute the following Maven command from the ch08/sample01 directory.

\> mvn test -Psample01

Let’s see how to decrypt a JWT encrypted by RSA-OAEP. You need to know the PrivateKey corresponding to the PublicKey used to encrypt the message:

    public static void decryptJWT() throws NoSuchAlgorithmException,

                                JOSEException, ParseException {

    // generate private/public key pair.

    KeyPair keyPair = generateKeyPair();

    // get the private key - used to decrypt the message.

    PrivateKey privateKey = keyPair.getPrivate();

    // get the public key - used to encrypt the message.

    PublicKey publicKey = keyPair.getPublic();

    // get encrypted JWT in base64-encoded text.

    String jwtInText = buildEncryptedJWT(publicKey);

    // create a decrypter.

    JWEDecrypter decrypter = new RSADecrypter((RSAPrivateKey) privateKey);

    // create the encrypted JWT with the base64-encoded text.

    EncryptedJWT encryptedJWT = EncryptedJWT.parse(jwtInText);

    // decrypt the JWT.

    encryptedJWT.decrypt(decrypter);

    // print the value of JOSE header.

    System.out.println("JWE Header:" + encryptedJWT.getHeader());

    // JWE content encryption key.

    System.out.println("JWE Content Encryption Key: " + encryptedJWT.getEncryptedKey());

    // initialization vector.

    System.out.println("Initialization Vector: " + encryptedJWT.getIV());

    // ciphertext.

    System.out.println("Ciphertext : " + encryptedJWT.getCipherText());

    // authentication tag.

    System.out.println("Authentication Tag: " + encryptedJWT.getAuthTag());

    // print the value of JWT body

    System.out.println("Decrypted Payload: " + encryptedJWT.getPayload());

}

The preceding code produces something similar to the following output:

JWE Header: {"alg":"RSA-OAEP","enc":"A128GCM"}

JWE Content Encryption Key: NbIuAjnNBwmwlbKiIpEzffU1duaQfxJpJaodkxDj

SC2s3tO76ZdUZ6YfPrwSZ6DU8F51pbEw2f2MK_C7kLpgWUl8hMHP7g2_Eh3y

Th5iK6Agx72o8IPwpD4woY7CVvIB_iJqz-cngZgNAikHjHzOC6JF748MwtgSiiyrI

9BsmU

Initialization Vector: JPPFsk6yimrkohJf

Ciphertext: XF2kAcBrAX_4LSOGejsegoxEfb8kV58yFJSQ0_WOONP5wQ07HG

mMLTyR713ufXwannitR6d2eTDMFe1xkTFfF9ZskYj5qJ36rOvhGGhNqNdGEpsB

YK5wmPiRlk3tbUtd_DulQWEUKHqPc_VszWKFOlLQW5UgMeHndVi3JOZgiwN

gy9bvzacWazK8lTpxSQVf-NrD_zu_qPYJRisvbKI8dudv7ayKoE4mnQW_fUY-U10

AMy-7Bg4WQE4j6dfxMlQGoPOo

Authentication Tag: pZWfYyt2kO-VpHSW7btznA

Decrypted Payload:

{

   "exp":1402116034,

   "sub":"john",

   "nbf":1402115434,

   "aud":["https:\/\/app1.foo.com "," https:\/\/app2.foo.com"],

   "iss":"https:\/\/apress.com",

   "jti":"a1b41dd4-ba4a-4584-b06d-8988e8f995bf",

   "iat":1402115434

}

Generating a JWE Token with RSA-OAEP and AES with a Non-JSON Payload

The following Java code generates a JWE token with RSA-OAEP and AES for a non-JSON payload. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch08/sample02—and it runs on Java 8+. First you need to invoke the method generateKeyPair() and pass the PublicKey(generateKeyPair().getPublicKey()) into the method buildEncryptedJWT():

// this method generates a key pair and the corresponding public key is used // to encrypt the message.

public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, JOSEException {

    // instantiate KeyPairGenerate with RSA algorithm.

    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

    // set the key size to 1024 bits.

    keyGenerator.initialize(1024);

    // generate and return private/public key pair.

    return keyGenerator.genKeyPair();

}

// this method is used to encrypt a non-JSON payload using the provided // public key.

public static String buildEncryptedJWT(PublicKey publicKey) throws JOSEException {

    // create JWE header with RSA-OAEP and AES/GCM.

    JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

    // create encrypter with the RSA public key.

    JWEEncrypter encrypter = new RSAEncrypter((RSAPublicKey) publicKey);

    // create a JWE object with a non-JSON payload

    JWEObject jweObject = new JWEObject(jweHeader, new Payload("Hello world!"));

    // encrypt the JWT.

    jweObject.encrypt(encrypter);

    // serialize into base64-encoded text.

    String jwtInText = jweObject.serialize();

    // print the value of the JWT.

    System.out.println(jwtInText);

    return jwtInText;

}

To build and run the program, execute the following Maven command from the ch08/sample02 directory.

\> mvn test -Psample02

Generating a Nested JWT

The following Java code generates a nested JWT with RSA-OAEP and AES for encryption and HMAC-SHA256 for signing. The nested JWT is constructed by encrypting the signed JWT. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch08/sample03—and it runs on Java 8+. First you need to invoke the method buildHmacSha256SignedJWT() with a shared secret and pass its output along with the generateKeyPair().getPublicKey() into the method buildNestedJWT():

// this method generates a key pair and the corresponding public key is used // to encrypt the message.

public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {

    // instantiate KeyPairGenerate with RSA algorithm.

    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

    // set the key size to 1024 bits.

    keyGenerator.initialize(1024);

    // generate and return private/public key pair.

    return keyGenerator.genKeyPair();

}

// this method is used to sign a JWT claims set using the provided shared // secret.

public static SignedJWT buildHmacSha256SignedJWT(String sharedSecretString) throws JOSEException {

    // build audience restriction list.

    List<String> aud = new ArrayList<String>();

    aud.add("https://app1.foo.com");

    aud.add("https://app2.foo.com");

    Date currentTime = new Date();

    // create a claims set.

    JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().

    // set the value of the issuer.

    issuer("https://apress.com").

    // set the subject value - JWT belongs to this subject.

    subject("john").

    // set values for audience restriction.

    audience(aud).

    // expiration time set to 10 minutes.

    expirationTime(new Date(new Date().getTime() + 1000 ∗ 60 ∗ 10)).

    // set the valid from time to current time.

    notBeforeTime(currentTime).

    // set issued time to current time.

    issueTime(currentTime).

    // set a generated UUID as the JWT identifier.

    jwtID(UUID.randomUUID().toString()).build();

    // create JWS header with HMAC-SHA256 algorithm.

    JWSHeader jswHeader = new JWSHeader(JWSAlgorithm.HS256);

    // create signer with the provider shared secret.

    JWSSigner signer = new MACSigner(sharedSecretString);

    // create the signed JWT with the JWS header and the JWT body.

    SignedJWT signedJWT = new SignedJWT(jswHeader, jwtClaims);

    // sign the JWT with HMAC-SHA256.

    signedJWT.sign(signer);

    // serialize into base64-encoded text.

    String jwtInText = signedJWT.serialize();

    // print the value of the JWT.

    System.out.println(jwtInText);

    return signedJWT;

}

// this method is used to encrypt the provided signed JWT or the JWS using // the provided public key.

public static String buildNestedJWT(PublicKey publicKey, SignedJWT signedJwt) throws JOSEException {

    // create JWE header with RSA-OAEP and AES/GCM.

    JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

    // create encrypter with the RSA public key.

    JWEEncrypter encrypter = new RSAEncrypter((RSAPublicKey) publicKey);

    // create a JWE object with the passed SignedJWT as the payload.

    JWEObject jweObject = new JWEObject(jweHeader, new Payload(signedJwt));

    // encrypt the JWT.

    jweObject.encrypt(encrypter);

    // serialize into base64-encoded text.

    String jwtInText = jweObject.serialize();

    // print the value of the JWT.

    System.out.println(jwtInText);

    return jwtInText;

}

To build and run the program, execute the following Maven command from the ch08/sample03 directory.

\> mvn test -Psample03

## Summary

- The JWE specification standardizes the way to represent encrypted content in a cryptographically safe manner.

- JWE defines two serialized forms to represent the encrypted payload: the JWE compact serialization and JWE JSON serialization.

- In the JWE compact serialization, a JWE token is built with five components, each separated by a period (.): JOSE header, JWE Encrypted Key, JWE Initialization Vector, JWE Ciphertext, and JWE Authentication Tag.

- The JWE JSON serialization can produce encrypted data targeting at multiple recipients over the same payload.

- In a Nested JWT, the payload must be a JWT itself. In other words, a JWT, which is enclosed in another JWS or JWE token, builds a Nested JWT.

- A Nested JWT is used to perform nested signing and encryption.

 

9. OAuth 2.0 Profiles

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

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

 

10. Accessing APIs via Native Mobile Apps

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

The adoption of native mobile apps has increased heavily in the last few years. Within the first decade of the 21st century, the Internet users worldwide increased from 350 million to more than 2 billion and mobile phone subscribers from 750 million to 5 billion—and today it hits 6 billion, where the world population is around 7 billion. Most of the mobile devices out there–even the cheapest ones—could be used to access the Internet.

We treat a native mobile application as an untrusted or a public client. A client application, which is not capable of protecting its own keys or credentials, is identified as a public client under OAuth terminology. Since the native mobile apps run on a device owned by the user, the user who is having complete access to the mobile device can figure out any keys the application hides. This is a hard challenge we face in accessing secured APIs from a native mobile application.

In this chapter, we discuss the best practices in using OAuth 2.0 for native apps, Proof Key for Code Exchange (PKCE), which is an approach for protecting native apps from code interception attack and protecting native apps in a browser-less environment.

Mobile Single Sign-On (SSO)

It takes an average of 20 seconds for a user to log in to an application. Not having to enter a password each time a user needs to access a resource saves time and makes users more productive and also reduces the frustration of multiple login events and forgotten passwords. When we have single sign-on, the users will only have one password to remember and update and only one set of password rules to remember. Their initial login provides them with access to all the resources, typically for the entire day or the week.

If you provide multiple mobile applications for your corporate employees to access from their mobile devices, it’s a pain to ask them to re-login to each application independently. Possibly all of them may be sharing the same credential store. This is analogous to a case where Facebook users log in to multiple third-party mobile applications with their Facebook credentials. With Facebook login, you only login once to Facebook and will automatically log into the other applications rely on Facebook login.

In mobile world, login to native apps is done in three different ways: directly asking for user credentials, using a WebView, and using the system browser.

Login with Direct Credentials

With this approach, the user directly provides the credentials to the native app itself (see Figure 10-1). And the app will use an API (or OAuth 2.0 password grant type) to authenticate the user. This approach assumes the native app is trusted. In case your native app uses a third-party identity provider for login, we must not use this. Even this approach may not be possible, unless the third-party identity provider provides a login API or supports OAuth 2.0 password grant type. Also this approach can make the users vulnerable for phishing attacks. An attacker can plant a phishing attack by fooling the user to install a native app with the same look and feel as the original app and then mislead the user to share his or her credentials with it. In addition to this risk, login with direct credentials does not help in building a single sign-on experience, when you have multiple native apps. You need to use your credentials to log in to each individual application.

 

Figure 10-1

The Chase bank’s mobile app, which users directly provide credentials for login

Login with WebView

The native app developers use a WebView in a native app to embed the browser, so that the app can use web technologies such as HTML, JavaScript, and CSS. During the login flow, the native app loads the system browser into a WebView and uses HTTP redirects to get the user to the corresponding identity provider. For example, if you want to authenticate users with Facebook, to your native app, you load the system browser into a WebView first and then redirect the user to Facebook. What’s happening in the browser loaded into the WebView is no different from the flow you see when you log in to a web app via Facebook using a browser.

The WebView-based approach was popular in building hybrid native apps, because it provides better user experience. The users won’t notice the browser being loaded into the WebView. It looks like everything happens in the same native app.

It also has some major disadvantages. The web session under the browser loaded into a WebView of a native app is not shared between multiple native apps. For example, if you do login with Facebook to one native app, by redirecting the user to facebook.com via a browser loaded into a WebView, the user has to log in to Facebook again and again, when multiple native apps follow the same approach. That is because the web session created under facebook.com in one WebView is not shared with another WebView of a different native app. So the single sign-on (SSO) between native apps will not work with the WebView approach.

WebView-based native apps also make the users more vulnerable to phishing attacks. In the same example we discussed before, when a user gets redirected to facebook.com via the system browser loaded into a WebView, he or she won’t be able to figure out whether they are visiting something outside the native app. So, the native app developer can trick the user by presenting something very similar to facebook.com and steal user’s Facebook credentials. Due to this reason, most of the developers are now moving away from using a WebView for login.

Login with a System Browser

This approach for login into a native app is similar to what we discussed in the previous section, but instead of the WebView, the native app spins up the system browser (see Figure 10-2). System browser itself is another native app. User experience in this approach is not as smooth as with the WebView approach, as the user has to switch between two native apps during the login process, but in terms of security, this is the best approach. Also, this is the only approach we can have single sign-on experience in a mobile environment. Unlike WebView approach, when you use the system browser, it manages a single web session for the user. Say, for example, when there are multiple native apps using Facebook login via the same system browser, the users only need to log in to Facebook once. Once a web session is created under facebook.com domain with the system browser, for the subsequent login requests from other native apps, users will be logged in automatically. In the next section, we see how we can use OAuth 2.0 securely to build this use case.

 

Figure 10-2

Login to Foursquare native app using Facebook

Using OAuth 2.0 in Native Mobile Apps

OAuth 2.0 has become the de facto standard for mobile application authentication. In our security design, we need to treat a native app a dumb application. It is very much similar to a single-page application. The following lists out the sequence of events that happen in using OAuth 2.0 to log in to a native mobile app.

 

Figure 10-3

A typical login flow for a native mobile app with OAuth 2.0

1. 1.

Mobile app developer has to register the application with the corresponding identity provider or the OAuth 2.0 authorization server and obtain a client_id. The recommendation is to use OAuth 2.0 authorization code grant type, without a client secret. Since the native app is an untrusted client, there is no point of having a client secret. Some were using implicit grant type for native apps, but it has its own inherent security issues and not recommended any more.

 

2. 2.

Instead of WebView, use SFSafariViewController with iOS9+ or Chrome Custom Tabs for Android. This web controller provides all the benefits of the native system browser in a control that can be placed within an application. Then you can embed the client_id obtained from step 1 into the application. When you embed a client_id into an app, it will be the same for all the instances of that native app. If you want to differentiate each instance of the app (installed in different devices), then we can dynamically generate a client_id for each instance at the start of the app, following the protocol defined in OAuth 2.0 Dynamic Client Registration profile, which we explained in detail in Chapter 9.

 

3. 3.

During the installation of the app, we need to register an app-specific custom URL scheme with the mobile operating system. This URL scheme must match the callback URL or redirect URI you used in step 1, at the time of app registration. A custom URL scheme lets the mobile operating system to pass back the control to your app from another external application, for example from the system browser. If you send some parameters to the app-specific custom URI scheme on the browser, the mobile operating system will track that and invoke the corresponding native app with those parameters.

 

4. 4.

Once the user clicks login, on the native app, we need to spin up the system browser and follow the protocol defined in OAuth 2.0 authorization code grant type (see Figure 10-3), which we discussed in detail in Chapter 4.

 

5. 5.

After the user authenticates to the identity provider, the browser redirects the user back to the registered redirect URI, which is in fact a custom URL scheme registered with the mobile operating system.

 

6. 6.

Upon receiving the authorization code to the custom URL scheme on the system browser, the mobile operating system spins up the corresponding native app and passes over the control.

 

7. 7.

The native app will talk to the token endpoint of the authorization server and exchange the authorization code to an access token.

 

8. 8.

The native app uses the access token to access APIs.

 

Inter-app Communication

The system browser itself is another native app. We used a custom URL scheme as a way of inter-app communication to receive the authorization code from the authorization server. There are multiple ways for inter-app communication available in a mobile environment: private-use URI scheme (also known as custom URL scheme), claimed HTTPS URL scheme, and loopback URI scheme.

Private URI Schemes

In the previous section, we already discussed how a private URI scheme works. When the browser hits with a private URI scheme, it invokes the corresponding native app, registered for that URI scheme, and hands over the control. The RFC 75951 defines guidelines and registration procedures for URI schemes, and according to that, it is recommended to use a domain name that is under your control, in its reverse order as the private URI scheme. For example, if you own app.foo.com, then the private URI scheme should be com.foo.app. The complete private URI scheme may look like com.foo.app:/oauth2/redirect, and there is only one slash that appears right after the scheme component.

In the same mobile environment, the private URI schemes can collide with each other. For example, there can be two apps registered for the same URI scheme. Ideally, this should not happen if you follow the convention we discussed before while choosing an identifier. But still there is an opportunity that an attacker can use this technique to carry out a code interception attack. To prevent such attacks, we must use Proof Key for Code Exchange (PKCE) along with private URI schemes. We discuss PKCE in a later section.

Claimed HTTPS URI Scheme

Just like the private URI scheme, which we discussed in the previous section, when a browser sees a claimed HTTPS URI scheme, instead of loading the corresponding page, it hands over the control to the corresponding native app. In supported mobile operating systems, you can claim an HTTPS domain, which you have control. The complete claimed HTTPS URI scheme may look like https://app.foo.com/oauth2/redirect. Unlike in private URI scheme, the browser verifies the identity of the claimed HTTPS URI before redirection, and for the same reason, it is recommended to use claimed HTTPS URI scheme over others where possible.

Loopback Interface

With this approach, your native app will listen on a given port in the device itself. In other words, your native app acts as a simple web server. For example, your redirect URI will look like http://127.0.0.1:5000/oauth2/redirect. Since we are using the loopback interface (127.0.0.1), when the browser sees this URL, it will hand over the control to the service listening on the mobile device on port 5000. The challenge with this approach is that your app may not be able to run on the same port on all the devices, if there are any other apps on the mobile device already using the same port.

Proof Key for Code Exchange (PKCE)

Proof Key for Code Exchange (PKCE) is defined in the RFC 7636 as a way to mitigate code interception attack (more details in Chapter 14) in a mobile environment. As we discussed in the previous section, when you use a custom URL scheme to retrieve the authorization code from the OAuth authorization server, there can be a case where it goes to a different app, which is also registered with the mobile device for the same custom URL scheme as the original app. An attacker can possibly do this with the intention of stealing the code.

When the authorization code gets to the wrong app, it can exchange it to an access token and then gets access to the corresponding APIs. Since we use authorization code with no client secret in mobile environments, and the client id of the original app is public, the attacker has no issue in exchanging the code to an access token by talking to the token endpoint of the authorization server.

 

Figure 10-4

A typical login flow for a native mobile app with OAuth 2.0 and PKCE

Let’s see how PKCE solves the code interception attack (see Figure 10-4):

1. 1.

The native mobile app, before redirecting the user to the authorization server, generates a random value, which is called the code_verifier. The value of the code_verifier must have a minimum length of 43 characters and a maximum of 128 characters.

 

2. 2.

Next the app has to calculate the SHA256 of the code_verifier and find its base64-url-encoded (see Appendix E) representation, with no padding. Since SHA256 hashing algorithm always results in a hash of 256 bits, when you base64-url-encode it, there will be a padding all the time, which is represented by the = sign. According to the PKCE RFC, we need to remove that padding—and that value, which is the SHA256-hashed, base64-url-encoded, unpadded code_verifier, is known as the code_challenge.

 

3. 3.

Now, when the native app initiates the authorization code request and redirects the user to the authorization server, it has to construct the request URL in the following manner, along with the code_challenge and the code_challenge_method query parameters. The code_challenge_method carries the name of the hashing algorithm.

https://idp.foo.com/authorization?client_id=FFGFGOIPI7898778&scopeopenid&redirect_uri=com.foo.app:/oauth2/redirect&response_type=code&code_challenge=YzfcdAoRg7rAfj9_Fllh7XZ6BBl4PIHC-xoMrfqvWUc&code_challenge_method=S256"

 

4. 4.

At the time of issuing the authorization code, the authorization server must record the provided code_challenge against the issued authorization code. Some authorization servers may embed the code_challenge into the code itself.

 

5. 5.

Once the native app gets the authorization code, it can exchange the code to an access token by talking to the authorization server’s token endpoint. But, when you follow PKCE, you must send the code_verifier (which is corresponding to the code_challenge) along with the token request.

curl -k --user "XDFHKKJURJSHJD" -d "code=XDFHKKJURJSHJD&grant_type=authorization_code&client_id=FFGFGOIPI7898778 &redirect_uri=com.foo.app:/oauth2/redirect&code_verifier=ewewewoiuojslkdjsd9sadoidjalskdjsdsdewewewoiuojslkdjsd9sadoidjalskdjsdsd" https://idp.foo.com/token

 

6. 6.

If the attacker’s app gets the authorization code, it still cannot exchange it to an access token, because only the original app knows the code_verifier.

 

7. 7.

Once the authorization server receives the code_verifier along with the token request, it will find the SHA256-hashed, base64-url-encoded, unpadded value of it and compare it with the recorded code_challenge. If those two match, then it will issue the access token.

 

Browser-less Apps

So far in this chapter, we only discussed about mobile devices, which are capable of spinning up a web browser. There is another growing requirement to use OAuth secured APIs from applications running on devices with input constraints and no web browser, such as smart TVs, smart speakers, printers, and so on. In this section, we discuss how to access OAuth 2.0 protected APIs from browser-less apps using the OAuth 2.0 device authorization grant. In any case, the device authorization grant does not replace any of the approaches we discussed earlier with respect to native apps running on capable mobile devices.

OAuth 2.0 Device Authorization Grant

The OAuth 2.0 device authorization grant2 is the RFC 8628, which is published by the IETF OAuth working group. According to this RFC, a device to use the device authorization grant type must satisfy the following requirements:

- The device is already connected to the Internet or to the network, which has access to the authorization server.

- The device is able to make outbound HTTPS requests.

- The device is able to display or otherwise communicate a URI and code sequence to the user.

- The user has a secondary device (e.g., personal computer or smartphone) from which they can process a request.

Let’s see how device authorization grant works, with an example. Say we have a YouTube app running on a smart TV, and we need the smart TV to access our YouTube account on behalf of us. In this case, YouTube acts as both the OAuth authorization server and the resource server, and the YouTube app running on the smart TV is the OAuth client application.

 

Figure 10-5

A typical login flow for a browser-less app with OAuth 2.0

1. 1.

The user takes the TV remote and clicks the YouTube app to associate his/her YouTube account with the app.

 

2. 2.

The YouTube app running on the smart TV has an embedded client ID and sends a direct HTTP request over HTTPS to the authorization server.

POST /device_authorization HTTP/1.1

Host: idp.youtube.com

Content-Type: application/x-www-form-urlencoded

client_id=XDFHKKJURJSHJD

 

3. 3.

In response to the preceding request, the authorization server returns back a device_code, a user_code, and a verification URI. Both the device_code and the user_code have an expiration time associated with them, which is communicated to the client app via expires_in parameter (in seconds).

HTTP/1.1 200 OK

Content-Type: application/json

Cache-Control: no-store

{

  "device_code": "GmRhmhcxhwAzkoEqiMEg_DnyEysNkuNhszIySk9eS",

  "user_code": "WDJB-MJHT",

  "verification_uri": "https://youtube.com/device",

  "verification_uri_complete":

            "https://youtube.com/device?user_code=WDJB-MJHT",

  "expires_in": 1800,

  "interval": 5

}

 

4. 4.

The YouTube client app instructs the user to visit the provided verification URI (from the preceding response) and confirm the authorization request with the provided user code (from the preceding response).

 

5. 5.

Now the user has to use a secondary device (a laptop or mobile phone) to visit the verification URI. While that action is in progress, the YouTube app will keep polling the authorization server to see whether the user has confirmed the authorization request. The minimum amount of time the client should wait before polling or the time between polling is specified by the authorization server in the preceding response under the interval parameter. The poll request to the token endpoint of the authorization server includes three parameters. The grant_type parameter must carry the value urn:ietf:params:oauth:grant-type:device_code, so the authorization server knows how to process this request. The device_code parameter carries the device code issued by the authorization server in its first response, and the client_id parameter carries the client identifier of the YouTube app.

POST /token HTTP/1.1

Host: idp.youtube.com

Content-Type: application/x-www-form-urlencoded

grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code

&device_code=GmRhmhcxhwAzkoEqiMEg_DnyEysNkuNhszIySk9eS

&client_id=459691054427

 

6. 6.

The user visits the provided verification URI, enters the user code, and confirms the authorization request.

 

7. 7.

Once the user confirms the authorization request, the authorization server issues the following response to the request in step 5. This is the standard response from an OAuth 2.0 authorization server token endpoint.

HTTP/1.1 200 OK

Content-Type: application/json;charset=UTF-8

Cache-Control: no-store

Pragma: no-cache

{

       "access_token":"2YotnFZFEjr1zCsicMWpAA",

       "token_type":"Bearer",

       "expires_in":3600,

       "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",

 }

 

8. 8.

Now the YouTube app can use this access token to access the YouTube API on behalf of the user.

 

## Summary

- There are multiple grant types in OAuth 2.0; however, while using OAuth 2.0 to access APIs from a native mobile app, it is recommended to use authorization code grant type, along with Proof Key for Code Exchange (PKCE).

- PKCE protects the native apps from code interception attack.

- The use of browser-less devices such as smart TVs, smart speakers, printers, and so on is gaining popularity.

- The OAuth 2.0 device authorization grant defines a standard flow to use OAuth 2.0 from a browser-less device and gain access to APIs.

Footnotes

 

11. OAuth 2.0 Token Binding

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

Most of the OAuth 2.0 deployments do rely upon bearer tokens. A bearer token is like “cash.” If I steal 10 bucks from you, I can use it at a Starbucks to buy a cup of coffee—no questions asked. I do not need to prove that I own the ten-dollar note. Unlike cash, if I use my credit card, I need to prove the possession. I need to prove I own it. I need to sign to authorize the transaction, and it’s validated against the signature on the card. The bearer tokens are like cash—once stolen, an attacker can use it to impersonate the original owner. Credit cards are like proof of possession (PoP) tokens.

OAuth 2.0 recommends using Transport Layer Security (TLS) for all the interactions between the client, authorization server, and resource server. This makes the OAuth 2.0 model quite simple with no complex cryptography involved—but at the same time, it carries all the risks associated with a bearer token. There is no second level of defense. Also not everyone is fully bought into the idea of using OAuth 2.0 bearer tokens—just trusting the underlying TLS communication. I’ve met several people—mostly from the financial domain—who are reluctant to use OAuth 2.0, just because of the bearer tokens.

An attacker may attempt to eavesdrop authorization code/access token/refresh token (see Chapter 4 for details) in transit from the authorization server to the client, using any of the following means:

- Malware installed in the browser (public clients).

- Browser history (public clients/URI fragments).

- Intercept the TLS communication between the client and the authorization server or the resource server (exploiting the vulnerabilities in the TLS layer like Heartbleed and Logjam).

- TLS is point to point (not end to end)—an attacker having access to a proxy server could simply log all the tokens. Also, in many production deployments, the TLS connection is terminated at the edge, and from there onward, it’s either a new TLS connection or a plain HTTP connection. In either case, as soon as a token leaves the channel, it’s no more secure.

Understanding Token Binding

OAuth 2.0 token binding proposal cryptographically binds security tokens to the TLS layer, preventing token export and replay attacks. It relies on TLS—and since it binds the tokens to the TLS connection itself, anyone who steals a token cannot use it over a different channel.

We can break down the token binding protocol into three main phases (see Figure 11-1).

 

Figure 11-1

Three main phases in the token binding protocol

Token Binding Negotiation

During the negotiation phase, the client and the server negotiate a set of parameters to use for token binding between them. This is independent of the application layer protocols—as it happens during the TLS handshake (see Appendix C). We discuss more about this in the next section. The token binding negotiation is defined in the RFC 8472. Keep in mind we do not negotiate any keys in this phase, only the metadata.

Key Generation

During the key generation phase, the client generates a key pair according to the parameters negotiated in the negotiation phase. The client will have a key pair for each host it talks to (in most of the cases).

Proof of Possession

During the proof of possession phase, the client uses the keys generated in the key generation phase to prove the possession. Once the keys are agreed upon, in the key generation phase, the client proves the possession of the key by signing the exported keying material (EKM) from the TLS connection. The RFC 5705 allows an application to get additional application-specific keying material derived from the TLS master secret (see Appendix C). The RFC 8471 defines the structure of the token binding message, which includes the signature and other key materials, but it does not define how to carry the token binding message from the client to the server. It’s up to the higher-level protocols to define it. The RFC 8473 defines how to carry the token binding message over an HTTP connection (see Figure 11-2).

 

Figure 11-2

The responsibilities of each layer in a token binding flow

TLS Extension for Token Binding Protocol Negotiation

To bind security tokens to the TLS connection, the client and the server need to first agree upon the token binding protocol (we’ll discuss about this later) version and the parameters (signature algorithm, length) related to the token binding key. This is accomplished by a new TLS extension without introducing additional network roundtrips in TLS 1.2 and earlier versions.

The token binding protocol version reflects the protocol version defined by the Token Binding Protocol (RFC 8471)—and the key parameters are defined by the same specification itself.

The client uses the Token Binding TLS extension to indicate the highest supported token binding protocol version and key parameters. This happens with the Client Hello message in the TLS handshake. To support the token binding specification, both the client and the server should support the token binding protocol negotiation extension.

The server uses the Token Binding TLS extension to indicate the support for the token binding protocol and to select the protocol version and key parameters. The server that supports token binding and receives a Client Hello message containing the Token Binding extension will include the Token Binding extension in the Server Hello if the required conditions are satisfied.

If the Token Binding extension is included in the Server Hello and the client supports the token binding protocol version selected by the server, it means that the version and key parameters have been negotiated between the client and the server and shall be definitive for the TLS connection. If the client does not support the token binding protocol version selected by the server, then the connection proceeds without token binding.

Every time a new TLS connection is negotiated (TLS handshake) between the client and the server, a token binding negotiation happens too. Even though the negotiation happens repeatedly by the TLS connection, the token bindings (you will learn more about this later) are long-lived; they encompass multiple TLS connections and TLS sessions between a given client and server.

In practice, Nginx (https://github.com/google/ngx_token_binding) and Apache (https://github.com/zmartzone/mod_token_binding) have support for token binding. An implementation of Token Binding Protocol Negotiation TLS Extension in Java is available here: https://github.com/pingidentity/java10-token-binding-negotiation.

Key Generation

The Token Binding Protocol specification (RFC 8471) defines the parameters related to key generation. These are the ones agreed upon during the negotiation phase.

- If rsa2048_pkcs1.5 key parameter is used during the negotiation phase, then the signature is generated using the RSASSA-PKCS1-v1_5 signature scheme as defined in RFC 3447 with SHA256 as the hash function.

- If rsa2048_pss key parameter is used during the negotiation phase, then the signature is generated using the RSASSA-PSS signature scheme as defined in RFC 3447 with SHA256 as the hash function.

- If ecdsap256 key parameter is used during the negotiation phase, the signature is generated with ECDSA using Curve P-256 and SHA256 as defined in ANSI.X9–62.2005 and FIPS.186–4.2013.

In case a browser acts as the client, then the browser itself has to generate the keys and maintain them against the hostname of the server. You can find the status of this feature development for Chrome from here (www.chromestatus.com/feature/5097603234529280). Then again the token binding is not only for a browser, it’s useful in all the interactions between a client and a server—irrespective of the client being thin or thick.

Proof of Possession

A token binding is established by a user agent (or the client) generating a private/public key pair (possibly, within a secure hardware module, such as trusted platform module (TPM)) per target server, providing the public key to the server, and proving the possession of the corresponding private key, on every TLS connection to the server. The generated public key is reflected in the token binding ID between the client and the server. At the server end, the verification happens in two steps.

First, the server receiving the token binding message needs to verify that the key parameters in the message match with the token binding parameters negotiated and then validate the signature contained in the token binding message. All the key parameters and the signature are embedded into the token binding message.

The structure of the token binding message is defined in the Token Binding Protocol specification (RFC 8471). A token binding message can have multiple token bindings (see Figure 11-3). A given token binding includes the token binding ID, the type of the token binding (provided or referred—we’ll talk about this later), extensions, and the signature over the concatenation of exported keying material (EKM) from the TLS layer, token binding type, and key parameters. The token binding ID reflects the derived public key along with the key parameters agreed upon the token binding negotiation.

Once the TLS connection is established between a client and a server, the EKM will be the same—both at the client end and at the server end. So, to verify the signature, the server can extract the EKM from the underneath TLS connection and use the token binding type and key parameters embedded into the token binding message itself. The signature is validated against the embedded public key (see Figure 11-3).

 

Figure 11-3

The structure of the token binding message

How to carry the token binding message from the client to the server is not defined in the Token Binding Protocol specification, but in the Token Binding for HTTP specification or the RFC 8473. In other words, the core token binding specification lets the higher-level protocols make the decision on that. The Token Binding for HTTP specification introduces a new HTTP header called Sec-Token-Binding —and it carries the base64url-encoded value of the token binding message. The Sec-Token-Binding header field MUST NOT be included in HTTP responses—MUST include only once in an HTTP request.

Once the token binding message is accepted as valid, the next step is to make sure that the security tokens carried in the corresponding HTTP connection are bound to it. Different security tokens can be transported over HTTP—for example, cookies and OAuth 2.0 tokens. In the case of OAuth 2.0, how the authorization code, access token, and refresh token are bound to the HTTP connection is defined in the OAuth 2.0 Token Binding specification (https://tools.ietf.org/html/draft-ietf-oauth-token-binding-08).

Token Binding for OAuth 2.0 Refresh Token

Let’s see how the token binding works for OAuth 2.0 refresh tokens . A refresh token, unlike authorization code and access token, is only used between the client and the authorization server. Under the OAuth 2.0 authorization code grant type, the client first gets the authorization code and then exchanges it to an access token and a refresh token by talking to the token endpoint of the OAuth 2.0 authorization server (see Chapter 4 for details). The following flow assumes the client has already got the authorization code (see Figure 11-4).

 

Figure 11-4

OAuth 2.0 refresh grant type

1. 1.

The connection between the client and the authorization server must be on TLS.

 

2. 2.

The client which supports OAuth 2.0 token binding, during the TLS handshake itself, negotiates the required parameters with the authorization server, which too supports OAuth 2.0 token binding.

 

3. 3.

Once the TLS handshake is completed, the OAuth 2.0 client will generate a private key and a public key and will sign the exported keying material (EKM) from the underlying TLS connection with the private key—and builds the token binding message. (To be precise, the client will sign EKM + token binding type + key parameters.)

 

4. 4.

The base64url-encoded token binding message will be added as the value to the Sec-Token-Binding HTTP header to the connection between the client and the OAuth 2.0 authorization server.

 

5. 5.

The client will send a standard OAuth request to the token endpoint along with the Sec-Token-Binding HTTP header.

 

6. 6.

The authorization server validates the value of Sec-Token-Binding header, including the signature, and records the token binding ID (which is also included in the token binding message) against the issued refresh token. To make the process stateless, the authorization server can include the hash of the token binding ID into the refresh token itself—so it does not need to remember/store it separately.

 

7. 7.

Later, the OAuth 2.0 client tries to use the refresh token against the same token endpoint to refresh the access token. Now, the client has to use the same private key and public key pair used before to generate the token binding message and, once again, includes the base64url-encoded value of it to the Sec-Token-Binding HTTP header. The token binding message has to carry the same token binding ID as in the case where the refresh token was originally issued.

 

8. 8.

The OAuth 2.0 authorization server now must validate the Sec-Token-Binding HTTP header and then needs to make sure that the token binding ID in the binding message is the same as the original token binding ID attached to the refresh token in the same request. This check will make sure that the refresh token cannot be used outside the original token binding. In case the authorization server decides to embed the hashed value of the token binding ID to the refresh token itself, now it has to calculate the hash of the token binding ID in the Sec-Token-Binding HTTP header and compare it with what is embedded into the refresh token.

 

9. 9.

If someone steals the refresh token and is desperate to use it outside the original token binding, then he/she also has to steal the private/public key pair corresponding to the connection between the client and the server.

 

There are two types of token bindings—and what we discussed with respect to the refresh token is known as provided token binding. This is used when the token exchange happens directly between the client and the server. The other type is known as referred token binding, which is used when requesting tokens, which are intended to present to a different server—for example, the access token. The access token is issued in a connection between the client and the authorization server—but used in a connection between the client and the resource server.

Token Binding for OAuth 2.0 Authorization Code/Access Token

Let’s see how the token binding works for access tokens, under the authorization code grant type. Under the OAuth 2.0 authorization code grant type, the client first gets the authorization code via the browser (user agent) and then exchanges it to an access token and a refresh token by talking to the token endpoint of the OAuth 2.0 authorization server (see Figure 11-5).

 

Figure 11-5

OAuth 2.0 authorization code flow

1. 1.

When the end user clicks the login link on the OAuth 2.0 client application on the browser, the browser has to do an HTTP GET to the client application (which is running on a web server), and the browser has to establish a TLS connection with the OAuth 2.0 client first. The browser, which supports OAuth 2.0 token binding, during the TLS handshake itself, negotiates the required parameters with the client application, which too supports OAuth 2.0 token binding. Once the TLS handshake is completed, the browser will generate a private key and public key (for the client domain) and will sign the exported keying material (EKM) from the underlying TLS connection with the private key—and builds the token binding message. The base64url-encoded token binding message will be added as the value to the Sec-Token-Binding HTTP header to the connection between the browser and the OAuth 2.0 client—which is the HTTP GET.

 

2. 2.

In response to step 1 (assuming all the token binding validations are done), the client will send a 302 response to the browser, asking to redirect the user to the OAuth 2.0 authorization server. Also in the response, the client will include the HTTP header Include-Referred-Token-Binding-ID, which is set to true. This instructs the browser to include the token binding ID established between the browser and the client in the request to the authorization server. Also, the client application will include two additional parameters in the request: code_challenge and code_challenge_method. These parameters are defined in the Proof Key for Code Exchange (PKCE) or RFC 7636 for OAuth 2.0. Under token binding, these two parameters will carry static values, code_challenge=referred_tb and code_challenge_method=referred_tb.

 

3. 3.

The browser, during the TLS handshake itself, negotiates the required parameters with the authorization server. Once the TLS handshake is completed, the browser will generate a private key and public key (for the authorization server domain) and will sign the exported keying material (EKM) from the underlying TLS connection with the private key—and builds the token binding message. The client will send the standard OAuth request to the authorization endpoint along with the Sec-Token-Binding HTTP header. This Sec-Token-Binding HTTP header now includes two token bindings (in one token binding message—see Figure 11-3), one for the connection between the browser and the authorization server, and the other one is for the browser and the client application (referred binding).

 

4. 4.

The authorization server redirects the user back to the OAuth client application via browser—along with the authorization code. The authorization code is issued against the token binding ID in the referred token binding.

 

5. 5.

The browser will do a POST to the client application, which also includes the authorization code from the authorization server. The browser will use the same token binding ID established between itself and the client application—and adds the Sec-Token-Binding HTTP header.

 

6. 6.

Once the client application gets the authorization code (and given that the Sec-Token-Binding validation is successful), it will now talk to the authorization server’s token endpoint. Prior to that, the client has to establish a token binding with the authorization server. The token request will also include the code_verifier parameter (defined in the PKCE RFC), which will carry the provided token binding ID between the client and the browser—which is also the token binding ID attached to the authorization code. Since the access token, which will be issued by the authorization server, is going to be used against a protected resource, the client has to include the token binding between itself and the resource server into this token binding message as a referred binding. Upon receiving the token request, the OAuth 2.0 authorization server now must validate the Sec-Token-Binding HTTP header and then needs to make sure that the token binding ID in the code_verifier parameter is the same as the original token binding ID attached to the authorization code at the point of issuing it. This check will make sure that the code cannot be used outside the original token binding. Then the authorization server will issue an access token, which is bound to the referred token binding, and a refresh token, which is bound to the connection between the client and the authorization server.

 

7. 7.

The client application now invokes an API in the resource server passing the access token. This will carry the token binding between the client and the resource server.

 

8. 8.

The resource server will now talk to the introspection endpoint of the authorization server—and it will return back the binding ID attached to the access token, so the resource server can check whether it’s the same binding ID used between itself and the client application.

 

TLS Termination

Many production deployments do include a reverse proxy—which terminates the TLS connection. This can be at an Apache or Nginx server sitting between the client and the server. Once the connection is terminated at the reverse proxy, the server has no clue what happened at the TLS layer. To make sure the security tokens are bound to the incoming TLS connection, the server has to know the token binding ID. The HTTPS Token Binding with TLS Terminating Reverse Proxies, the draft specification (https://tools.ietf.org/html/draft-ietf-tokbind-ttrp-09), standardizes how the binding IDs are passed from the reverse proxy to the back-end server, as HTTP headers. The Provided-Token-Binding-ID and Referred-Token-Binding-ID HTTP headers are introduced by this specification (see Figure 11-6).

 

Figure 11-6

The reverse proxy passes the Provided-Token-Binding-ID and Referred-Token-Binding-ID HTTP headers to the backend server

## Summary

- OAuth 2.0 token binding proposal cryptographically binds security tokens to the TLS layer, preventing token export and replay attacks.

- Token binding relies on TLS—and since it binds the tokens to the TLS connection itself, anyone who steals a token cannot use it over a different channel.

- We can break down the token binding protocol into three main phases: negotiation phase, key generation phase, and proof of possession phase.

- During the negotiation phase, the client and the server negotiate a set of parameters to use for token binding between them.

- During the key generation phase, the client generates a key pair according to the parameters negotiated in the negotiation phase.

- During the proof of possession phase, the client uses the keys generated in the key generation phase to prove the possession.

 

12. Federating Access to APIs

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

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

 

13. User-Managed Access

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

OAuth 2.0 introduced an authorization framework for access delegation. It lets Bob delegate read access to his Facebook wall to a third-party application, without sharing Facebook credentials. User-Managed Access (UMA, pronounced “OOH-mah”) extends this model to another level, where Bob can not only delegate access to a third-party application but also to Peter who uses the same third-party application.

UMA is an OAuth 2.0 profile. OAuth 2.0 decouples the resource server from the authorization server. UMA takes one step further: it lets you control a distributed set of resource servers from a centralized authorization server. Also the resource owner can define a set of policies at the authorization server, which can be evaluated at the time a client is granted access to a protected resource. This eliminates the need of having the presence of the resource owner to approve access requests from arbitrary clients or requesting parties. The authorization server can make the decision based on the policies defined by the resource owner.

The latest version of UMA, which we discuss in this chapter, is UMA 2.0. If you are interested in learning more about UMA evolution, please check Appendix D: UMA Evolution.

Use Cases

Let’s say you have multiple bank accounts with Chase Bank, Bank of America, and Wells Fargo. You have hired a financial manager called Peter, who manages all your bank accounts through a personal financial management (PFM) application, which helps to budget better and understand the overall financial position, by often pulling information from multiple bank accounts. Here, you need to give limited access to Peter, to use the PFM to access your bank accounts. We assume all the banks expose their functionality over APIs and PFM uses banking APIs to retrieve data.

At a very high level, let’s see how UMA solves this problem (see Figure 13-1). First you need to define an access control policy at the authorization server, which all your banks trust. This authorization policy would say Peter should be given read access via the PFM app to Wells Fargo, Chase, and Bank of America bank accounts. Then you also need to introduce each bank to the authorization server, so whenever Peter tries to access your bank accounts, each bank talks to the authorization server and asks whether Peter is allowed to do that. For Peter to access a bank account via PFM app, the PFM app first needs to talk to the authorization server and gets a token on behalf of Peter. During this process, before issuing the token, the authorization server evaluates the access control policy you defined.

 

Figure 13-1

An account owner delegates the administration of his/her accounts to a Financial Manager via a Personal Financial Management App

Let’s take another example. Say you have a Google Doc. You do not want to share this with everyone, but with anyone from the management team of foo and bar companies (see Figure 13-2). Let’s see how this works with UMA.

First you have an authorization server, which Google trusts, so whenever someone wants to access your Google Doc, Google talks to the authorization server to see whether that person has the rights to do so. You also define a policy at the authorization server, which says only the managers from foo and bar companies can access your Google Doc.

 

Figure 13-2

A Google Doc owner delegates access to a Google Doc to a third party from a different company with specific roles

When a person (say Peter) tries to access your Google Doc, Google will redirect you to the authorization server. Then the authorization server will redirect Peter to Foo identity provider (or the home identity provider of Peter). Foo identity provider will authenticate Peter and send back Peter’s role as a claim to the authorization server. Now, since authorization server knows Peter’s role, and also the company Peter belongs to, if Peter belongs to a manager role, it will issue a token to Google Docs app, which it can use to retrieve the corresponding Google Doc via the Google Docs API.

UMA 2.0 Roles

UMA introduces one more role in addition to the four roles (resource owner, resource server, client, and authorization server) we discussed under OAuth 2.0, in Chapter 4. The following lists out all five roles involved in UMA:

1. 1.

Resource owner: In the preceding two use cases, you are the resource owner. In the first case, you owned the bank account, and in the second use case, you owned the Google Doc.

 

2. 2.

Resource server: This is the place which hosts protected resources. In the preceding first use case, each bank is a resource server—and in the second use case, the server, which hosts Google Docs API, is the resource server.

 

3. 3.

Client: This is the application, which wants to access a resource on behalf of the resource owner. In the preceding first use case, the personal financial management (PFM) application is the client, and in the second use case, it is the Google Docs web application.

 

4. 4.

Authorization server: This is the entity, which acts as the security token service (STS) to issue OAuth 2.0 access tokens to client applications.

 

5. 5.

Requesting party: This is something new in UMA. In the preceding first use case, Peter, the financial manager, is the requesting party, and in the second use case, Peter who is a manager at Foo company is the requesting party. The requesting party accesses a resource via a client application, on behalf of the resource owner.

 

UMA Protocol

There are two specifications developed under Kantara Initiative, which define UMA protocol. The core specification is called UMA 2.0 Grant for OAuth 2.0 Authorization. The other one is the Federated Authorization for UMA 2.0, which is optional.

A grant type is an extension point in OAuth 2.0 architecture. UMA 2.0 grant type extends the OAuth 2.0 to support the requesting party role and defines the flow the client application should follow to obtain an access token on behalf of the requesting party from the authorization server.

Let’s see in step by step how UMA 2.0 works, with the first use case we discussed earlier:

1. 1.

First, the account owner has to introduce each of his banks to the UMA authorization server. Here we possibly follow OAuth 2.0 authorization code grant type and provision an access token to the Chase Bank. UMA gives a special name to this token: Protection API Access Token (PAT).

 

2. 2.

The Chase Bank uses the provisioned access token or the PAT to register its resources with the authorization server. Following is a sample cURL command for resource registration. $PAT in the following command is a placeholder for the Protection API Access Token. Here we register the account of the account owner as a resource.

\> curl -v -X POST -H "Authorization:Bearer $PAT" -H "Content-Type: application/json" -d '{"resource_scopes":["view"], "description":"bank account details", "name":"accounts/1112019209", "type":"/accounts"}' https://as.uma.example.com/uma/resourceregistration

 

3. 3.

Peter via the personal financial management (PFM) application tries to access the Chase Bank account with no token.

\> curl –X GET https://chase.com/apis/accounts/1112019209

 

4. 4.

Since there is no token in the request from PFM, the bank API responds back with a 401 HTTP error code, along with the endpoint of the authorization server and a permission ticket. This permission ticket represents the level of permissions PFM needs to do a GET to /accounts API of the Chase Bank. In other words, PFM should get an access token from the provided authorization server, with the provided permissions in the given permission ticket.

 

5. 5.

To generate the permission ticket, the Chase Bank has to talk to the authorization server. As per the following cURL command, Chase Bank also passes resource_id and the resource_scope. The permission API is protected via OAuth 2.0, so the Chase Bank has to pass a valid access token to access it. UMA gives a special name to this token: Protection API Access Token (PAT), which we provisioned to Chase Bank in step 1.

\> curl -v -X POST -H "Authorization:Bearer $PAT" -H "Content-Type: application/json" -d '[{"resource_id":" accounts/1112019209","resource_scopes":["view"]}]' https://as.uma.example.com/uma/permission

{"ticket":"1qw32s-2q1e2s-1rt32g-r4wf2e"}

 

6. 6.

Now the Chase Bank will send the following 401 response to the PFM application.

HTTP/1.1 401 Unauthorized

WWW-Authenticate: UMA realm="chase" as_uri="https://as.uma.example.com" ticket="1qw32s-2q1e2s-1rt32g-r4wf2e "

 

7. 7.

The client application or the PFM now has to talk to the authorization server. By this time, we can assume that Peter, or the requesting party, has already logged in to the client app. If that login happens over OpenID Connect, then PFM has an ID token, which represents Peter. PFM passes both the ID token (as claim_token) and the permission ticket (as ticket) it got from Chase Bank to the authorization server, in the following cURL command. The claim_token is an optional parameter in the request, and if it is present, then there must be claim_token_format parameter as well, which defines the format of the claim_token. In the following cURL command, use a claim_token of the ID token format, and it can be even a SAML token. Here the $APP_CLIENTID and $APP_CLIENTSECRET are the OAuth 2.0 client id and client secret, respectively, you get at the time you register your application (PFM) with the OAuth 2.0 authorization server. The $IDTOKEN is a placeholder for the OpenID Connect ID token, while $TICKET is a placeholder for the permission ticket. The value of the grant_type parameter must be set to urn:ietf:params:oauth:grant-type:uma-ticket. The following cURL command is only an example, and it does not carry all the optional parameters.

\> curl -v -X POST --basic -u $APP_CLIENTID:$APP_CLIENTSECRET

   -H "Content-Type: application/x-www-form-urlencoded;

   charset=UTF-8" -k -d

   "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket&

    claim_token=$IDTOKEN&

    claim_token_format=http://openid.net/specs/openid-connect-core-1_0.html#IDToken&

    ticket=$TICKET"

    https://as.uma.example.com/uma/token

 

8. 8.

As the response to the preceding request, the client application gets an access token, which UMA calls a requesting party token (RPT) , and before authorization server returns back the access token, it internally evaluates any authorization policies defined by the account owner (or the resource owner) to see whether Peter has access to the corresponding bank account.

{

  "token_type":"bearer",

  "expires_in":3600,

  "refresh_token":"22b157546b26c2d6c0165c4ef6b3f736",

  "access_token":"cac93e1d29e45bf6d84073dbfb460"

}

 

9. 9.

Now the application (PFM) tries to access the Chase Bank account with the RPT from the preceding step.

\> curl –X GET –H "Authorization: Bearer cac93e1d29e45bf6d84073dbfb460" https://chase.com/apis/accounts/1112019209

 

10. 10.

The Chase Bank API will now talk to the introspection (see Chapter 9) endpoint to validate the provided RPT and, if the token is valid, will respond back with the corresponding data. If the introspection endpoint is secured, then the Chase Bank API has to pass the PAT in the HTTP authorization header to authenticate.

\> curl -H "Authorization:Bearer $PAT" -H 'Content-Type: application/x-www-form-urlencoded' -X POST --data "token=cac93e1d29e45bf6d84073dbfb460" https://as.uma.example.com/uma/introspection

HTTP/1.1 200 OK

Content-Type: application/json

Cache-Control: no-store

{

  "active": true,

  "client_id":"s6BhdRkqt3",

  "scope": "view",

  "sub": "peter",

  "aud": "accounts/1112019209"

 }

 

11. 11.

Once the Chase Bank finds the token is valid and carries all required scopes, it will respond back to the client application (PFM) with the requested data.

 

> **Note**
>


A recording of a UMA 2.0 demo done by the author of the book to the UMA working group with the open source WSO2 Identity Server is available here: www.youtube.com/watch?v=66aGc5AV7P4.

Interactive Claims Gathering

In the previous section, in step 7, we assumed that the requesting party is already logged in to the client application and the client application knows about the requesting party’s claims, say, for example, in the format of an ID token or a SAML token. The client application passes these claims in the claim_token parameter along with the permission ticket to the token endpoint of the authorization server. This request from the client application to the authorization server is a direct request. In case the client application finds that it does not have enough claims that are required by the authorization server to make an authorization decision based on its policies, the client application can decide to use interactive claim gathering. During the interactive claim gathering, the client application redirects the requesting party to the UMA authorization server. This is what we discussed under the second use case at the very beginning of the chapter, with respect to sharing Google Docs with external companies. The following is a sample request the client application generates to redirect the requesting party to the authorization server.

Host: as.uma.example.com

GET /uma/rqp_claims?client_id=$APP_CLIENTID

&ticket=$TICKET

&claims_redirect_uri=https://client.example.com/redirect_claims

&state=abc

The preceding sample request is an HTTP redirect, which flows through the browser. Here the $APP_CLIENTID is the OAuth 2.0 client id you get at the time you register your application with the UMA authorization server, and $TICKET is a placeholder for the permission ticket the client application gets from the resource server (see step 6 in the previous section). The value of claim_redirect_uri indicates the authorization server, where to send the response back, which points to an endpoint hosted in the client application.

How the authorization server does claim gathering is out of the scope of the UMA specification. Ideally, it can be by redirecting the requesting party again to his/her own home identity provider and getting back the requested claims (see Figure 13-2). Once the claim gathering is completed, the authorization server redirects the user back to the claim_redirect_uri endpoint with a permission ticket, as shown in the following. The authorization server tracks all the claims it gathered against this permission ticket.

HTTP/1.1 302 Found

Location: https://client.example.com/redirect_claims?

ticket=cHJpdmFjeSBpcyBjb250ZXh0LCBjb250cm9s&state=abc

The client application will now talk to the token endpoint of the authorization server with the preceding permission ticket to get a requesting party token (RPT) . This is similar to what we discussed under step 7 in the previous section, but here we do not send a claim_token.

\> curl -v -X POST --basic -u $APP_CLIENTID:$APP_CLIENTSECRET

   -H "Content-Type: application/x-www-form-urlencoded;

   charset=UTF-8" -k -d

   "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket&

    ticket=$TICKET"

    https://as.uma.example.com/uma/token

As the response to the preceding request, the client application gets an access token, which UMA calls a requesting party token (RPT), and before authorization server returns back the access token, it internally evaluates any authorization policies defined by the account owner (or the resource owner) to see whether Peter has access to the corresponding bank account.

{

  "token_type":"bearer",

  "expires_in":3600,

  "refresh_token":"22b157546b26c2d6c0165c4ef6b3f736",

  "access_token":"cac93e1d29e45bf6d84073dbfb460"

}

## Summary

- User-Managed Access (UMA) is an emerging standard built on top of the OAuth 2.0 core specification as a profile.

- UMA still has very few vendor implementations, but it promises to be a highly recognized standard in the near future.

- There are two specifications developed under Kantara Initiative, which define the UMA protocol. The core specification is called the UMA 2.0 Grant for OAuth 2.0 Authorization. The other one is the Federated Authorization for UMA 2.0, which is optional.

- UMA introduces a new role called, requesting party, in addition to the four roles used in OAuth 2.0: the authorization server, the resource server, the resource owner and the client application.

 

14. OAuth 2.0 Security

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

OAuth 2.0 is an authorization framework, as you know already. Being a framework, it gives multiple options for application developers. It is up to the application developers to pick the right options based on their use cases and how they want to use OAuth 2.0. There are few guideline documents to help you use OAuth 2.0 in a secure way. OAuth 2.0 Threat Model and Security Considerations (RFC 6819) produced by OAuth IETF working group defines additional security considerations for OAuth 2.0, beyond those in the OAuth 2.0 specification, based on a comprehensive threat model. The OAuth 2.0 Security Best Current Practice document, which is a draft proposal at the time of writing, talks about new threats related to OAuth 2.0, since the RFC 6819 was published. Also, the Financial-grade API (FAPI) working group under the OpenID foundation has published a set of guidelines on how to use OAuth 2.0 in a secure way to build financial grade applications. In this chapter, we go through a set of possible attacks against OAuth 2.0 and discuss how to mitigate those.

Identity Provider Mix-Up

Even though OAuth 2.0 is about access delegation, still people work around it to make it work for login. That’s how login with Facebook works. Then again, the OpenID Connect (see Chapter 6), which is built on top of OAuth 2.0, is the right way of using OAuth 2.0 for authentication. A recent research done by one of the leading vendors in the Identity and Access Management domain confirmed that most of the new development happened over the past few years at the enterprise level picked OAuth 2.0/OpenID Connect over SAML 2.0. All in all, OAuth 2.0 security is a hot topic. In 2016, Daniel Fett, Ralf Küsters, and Guido Schmitz did a research on OAuth 2.0 security and published a paper.1 Identity provider mix-up is one of the attacks highlighted in their paper. Identity provider is in fact the entity that issues OAuth 2.0 tokens or the OAuth 2.0 authorization server, which we discussed in Chapter 4.

Let’s try to understand how identity provider mix-up works (see Figure 14-1):

1. 1.

This attack happens with an OAuth 2.0 client application, which provides multiple identity provider (IdP) options for login. Let’s say foo.idp and evil.idp. We assume that the client application does not know that evil.idp is evil. Also it can be a case where evil.idp is a genuine identity provider, which could possibly be under an attack itself.

 

2. 2.

The victim picks foo.idp from the browser and the attacker intercepts the request and changes the selection to evil.idp. Here we assume the communication between the browser and the client application is not protected with Transport Layer Security (TLS). The OAuth 2.0 specification does not talk about it, and it’s purely up to the web application developers. Since there is no confidential data passed in this flow, most of the time the web application developers may not worry about using TLS. At the same time, there were few vulnerabilities discovered over the past on TLS implementations (mostly openssl). So, the attacker could possibly use such vulnerabilities to intercept the communication between the browser and the client application (web server), even if TLS is used.

 

Figure 14-1

Identity provider mix-up attack

 

3. 3.

Since the attacker changed the identity provider selection of the user, the client application thinks it’s evil.idp (even though the user picked foo.idp) and redirects the user to evil.idp. The client application only gets the modified request from the attacker, who intercepted the communication.

 

4. 4.

The attacker intercepts the redirection and modifies the redirection to go to the foo.idp. The way redirection works is the web server (in this case, the client application) sends back a response to the browser with a 302 status code—and with an HTTP Location header. If the communication between the browser and the client application is not on TLS, then this response is not protected, even if the HTTP Location header contains an HTTPS URL. Since we assumed already, the communication between the browser and the client application can be intercepted by the attacker, then the attacker can modify the Location header in the response to go to the foo.idp—which is the original selection—and no surprise to the user.

 

5. 5.

The client application gets either the code or the token (based on the grant type) and now will talk to the evil.idp to validate it. The authorization server (or the identity provider) will send back the authorization code (if the code grant type is used) to the callback URL, which is under the client application. Just looking at the authorization code, the client application cannot decide to which identity provider the code belongs to. So we assume it tracks the identity provider by some session variable—so as per step 3, the client application thinks it’s the evil.idp and talks to the evil.idp to validate the token.

 

6. 6.

The evil.idp gets hold of the user’s access token or the authorization code from the foo.idp. If it’s the implicit grant type, then it would be the access token, otherwise the authorization code. In mobile apps, most of the time, people used to embed the same client id and the client secret into all the instances—so an attacker having root access to his own phone can figure it out what the keys are and then, with the authorization code, can get the access token.

 

There is no record that the preceding attack is being carried out in practice—but at the same time, we cannot totally rule it out. There are a couple of options to prevent such attacks, and our recommendation is to use the option 1 as it is quite straightforward and solves the problem without much hassle.

1. 1.

Have separate callback URLs by each identity provider. With this the client application knows to which identity provider the response belongs to. The legitimate identity provider will always respect the callback URL associated with the client application and will use that. The client application will also attach the value of the callback URL to the browser session and, once the user got redirected back, will see whether it’s on the right place (or the right callback URL) by matching with the value of the callback URL from the browser session.

 

2. 2.

Follow the mitigation steps defined in the IETF draft specification: OAuth 2.0 IdP Mix-Up Mitigation (https://tools.ietf.org/html/draft-ietf-oauth-mix-up-mitigation-01). This specification proposes to send a set of mitigation data from the authorization server back to the client, along with the authorization response. The mitigation data provided by the authorization server to the client includes an issuer identifier, which is used to identify the authorization server, and a client id, which is used to verify that the response is from the correct authorization server and is intended for the given client. This way the OAuth 2.0 client can verify from which authorization server it got the response back and based on that identify the token endpoint or the endpoint to validate the token.

 

Cross-Site Request Forgery (CSRF)

In general, Cross-Site Request Forgery (CSRF) attack forces a logged-in victim’s browser to send a forged HTTP request, including the victim’s session cookie and any other automatically included authentication information to a vulnerable web application. Such an attack allows the attacker to force a victim’s browser to generate requests, where the vulnerable application thinks are legitimate requests from the victim. OWASP (Open Web Application Security Project) identifies this as one of the key security risks in web applications in its 2017 report.2

Let’s see how CSRF can be used with OAuth 2.0 to exploit a vulnerable web application (see Figure 14-2):

1. 1.

The attacker tries to log in to the target web site (OAuth 2.0 client) with his account at the corresponding identity provider. Here we assume the attacker has a valid account at the identity provider, trusted by the corresponding OAuth 2.0 client application.

 

2. 2.

The attacker blocks the redirection to the target web site and captures the authorization code. The target web site never sees the code. In OAuth 2.0, the authorization code is only good enough for one-time use. In case the OAuth 2.0 client application sees it and then exchanges it to an access token, then it’s no more valid—so the attacker has to make sure that the authorization code never reaches the client application. Since the authorization code flows through the attacker’s browser to the client, it can be easily blocked.

 

3. 3.

The attacker constructs the callback URL for the target site—and makes the victim clicks on it. In fact, it would be the same callback URL the attacker can copy from step 2. Here the attacker can send the link to the victim’s email or somehow fool him to click on the link.

 

Figure 14-2

Cross-Site Request Forgery (CSRF) attack in the OAuth 2.0 code flow

 

4. 4.

The victim clicks on the link and logs in to the target web site, with the account attached to the attacker—and adds his/her credit card information. Since the authorization code belongs to the attacker, the victim logs in to the target web site with the attacker’s account. This is a pattern many web sites follow to authenticate users with OAuth 2.0. Login with Facebook works in the same way. Once the web site gets the authorization code, it will talk to the authorization server and exchanges it to an access token. Then using that access token, the web site talks to another endpoint in the authorization server to find user information. In this case, since the code belongs to the attacker, the user information returned back from the authorization server will be related to him—so the victim now logs in to the target web site with the attacker’s account.

 

5. 5.

The attacker too logs in to the target web site with his/her valid credentials and uses victim’s credit card to purchase goods.

 

The preceding attack can be mitigated by following these best practices:

- Use a short-lived authorization code. Making the authorization code expires soon gives very little time for the attacker to plant an attack. For example, the authorization code issued by LinkedIn expires in 30 seconds. Ideally, the lifetime of the authorization code should be in seconds.

- Use the state parameter as defined in the OAuth 2.0 specification. This is one of the key parameters to use to mitigate CSRF attacks in general. The client application has to generate a random number (or a string) and passes it to the authorization server along with the grant request. Further, the client application has to add the generated value of the state to the current user session (browser session) before redirecting the user to the authorization server. According to the OAuth 2.0 specification, the authorization server has to return back the same state value with the authorization code to the redirect_uri (to the client application). The client must validate the state value returned from the authorization server with the value stored in the user’s current session—if it mismatches, it rejects moving forward. Going back to the attack, when the user clicks the crafted link sent to the victim by the attacker, it won’t carry the same state value generated before and attached to the victim’s session (or most probably victim’s session has no state value), or the attacker does not know how to generate the exact same state value. So, the attack won’t be successful, and the client application will reject the request.

- Use PKCE (Proof Key for Code Exchange). PKCE (RFC 7636) was introduced to protect OAuth 2.0 client applications from the authorization code interception attack, mostly targeting native mobile apps. The use of PKCE will also protect users from CSRF attacks, once the code_verifier is attached to the user’s browser session. We talked about PKCE in detail in Chapter 10.

Token Reuse

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

Token Leakage/Export

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

Open Redirector

An open redirector is an endpoint hosted on the resource server (or the OAuth 2.0 client application) end, which accepts a URL as a query parameter in a request—and then redirects the user to that URL. An attacker can modify the redirect_uri in the authorization grant request from the resource server to the authorization server to include an open redirector URL pointing to an endpoint owned by him. To do this, the attacker has to intercept the communication channel between the victim’s browser and the authorization server—or the victim’s browser and the resource server (see Figure 14-3).

Once the request hits the authorization server and after the authentication, the user will be redirected to the provided redirect_uri, which also carries the open redirector query parameter pointing to the attacker’s endpoint. To detect any modifications to the redirect_uri, the authorization server can carry out a check against a preregistered URL. But then again, some authorization server implementations will only worry about the domain part of the URL and will ignore doing an exact one-to-one match. So, any changes to the query parameters will be unnoticed.

 

Figure 14-3

Open Redirector attack

Once the user got redirected to the open redirector endpoint, it will again redirect the user to the value (URL) defined in the open redirector query parameter—which will take him/her to the attacker’s endpoint. In this request to the attacker’s endpoint, the HTTP Referer header could carry some confidential data, including the authorization code (which is sent to the client application by the authorization server as a query parameter).

How to prevent an open redirector attack:

- Enforce strict validations at the authorization server against the redirect_uri. It can be an exact one-to-one match or regex match.

- Validate the redirecting URL at open redirector and make sure you only redirect to the domains you own.

- Use JWT Secured Authorization Request (JAR) or Pushed Authorization Requests (PAR) as discussed in Chapter 4 to protect the integrity of the authorization request, so the attacker won’t be able to modify the request to include the open redirector query parameter to the redirect_uri.

Code Interception Attack

Code interception attack could possibly happen in a native mobile app. OAuth 2.0 authorization requests from native apps should only be made through external user agents, primarily the user’s browser. The OAuth 2.0 for Native Apps specification (which we discussed in Chapter 10) explains in detail the security and usability reasons why this is the case and how native apps and authorization servers can implement this best practice.

The way you do single sign-on in a mobile environment is by spinning up the system browser from your app and then initiate OAuth 2.0 flow from there. Once the authorization code is returned back to the redirect_uri (from the authorization server) on the browser, there should be a way to pass it over to the native app. This is taken care by the mobile OS—and each app has to register for a URL scheme with the mobile OS. When the request comes to that particular URL, the mobile OS will pass its control to the corresponding native app. But, the danger here is, there can be multiple apps that get registered for the same URL scheme, and there is a chance a malicious app could get hold of the authorization code. Since many mobile apps embed the same client id and client secret for all the instances of that particular app, the attacker can also find out what they are. By knowing the client id and client secret, and then having access to the authorization code, the malicious app can now get an access token on behalf of the end user.

PKCE (Proof Key for Code Exchange), which we discussed in detail in Chapter 10, was introduced to mitigate such attacks. Let’s see how it works:

1. 1.

The OAuth 2.0 client app generates a random number (code_verifier) and finds the SHA256 hash of it—which is called the code_challenge.

 

2. 2.

The OAuth 2.0 client app sends the code_challenge along with the hashing method in the authorization grant request to the authorization server.

 

3. 3.

Authorization server records the code_challenge (against the issued authorization code) and replies back with the code.

 

4. 4.

The client sends the code_verifier along with the authorization code to the token endpoint.

 

5. 5.

The authorization server finds the hash of the provided code_verifier and matches it against the stored code_challenge. If it does not match, rejects the request.

 

With this approach, a malicious app just having access to the authorization code cannot exchange it to an access token without knowing the value of the code_verifier.

Security Flaws in Implicit Grant Type

The OAuth 2.0 implicit grant type (see Figure 14-4) is now obsolete. This was mostly used by single-page applications and native mobile apps—but no more. In both the cases, the recommendation is to use the authorization code grant type. There are few security flaws, as listed in the following, identified in the implicit grant type, and the IETF OAuth working group officially announced that the applications should not use implicit grant type any more:

- With implicit grant type, the access token comes as a URI fragment and remains in the web browser location bar (step 5 in Figure 14-4). Since anything the web browser has in the location bar persevered as browser history, anyone having access to the browser history can steal the tokens.

- Since the access token remains in the web browser location bar, the API calls initiated from the corresponding web page will carry the entire URL in the location bar, along with the access token, in the HTTP Referer header. This will let external API endpoints to figure out (looking at the HTTP Referer header) what the access token is and possibly misuse it.

 

Figure 14-4

OAuth 2.0 implicit grant flow.

Google Docs Phishing Attack

An attacker used a fake OAuth 2.0 app called Google Docs as a medium to launch a massive phishing attack targeting Google users in May 2017. The first target was the media companies and public relations (PR) agencies. They do have a large amount of contacts—and the attacker used the email addresses from their contact lists to spread the attack. It went viral for an hour—before the app was removed by Google.

Is this a flaw in the OAuth 2.0 protocol exploited by the attacker or a flaw in how Google implemented it? Is there something we could have done better to prevent such attacks?

 

Figure 14-5

OAuth 2.0 authorization grant flow.

Almost all the applications you see on the Web today use the authorization code grant flow in OAuth 2.0. The attacker exploited step 3 in Figure 14-5 by tricking the user with an application name (Google Docs) known to them. Also, the attacker used an email template which is close to what Google uses in sharing docs, to make the user click on the link. Anyone who carefully looked at the email or even the consent screen could have caught up something fishy happening—but unfortunately, very few do care.

It’s neither a flaw of OAuth 2.0 nor how Google implemented it. Phishing is a prominent threat in cybersecurity. Does that mean there is no way to prevent such attacks other than proper user education? There are basic things Google could do to prevent such attacks in the future. Looking at the consent screen, “Google Docs” is the key phrase used there to win user’s trust. When creating an OAuth 2.0 app in Google, you can pick any name you want. This helps an attacker to misguide users. Google could easily filter out the known names and prevent app developers from picking names to trick the users.

Another key issue is Google does not show the domain name of the application (but just the application name) on the consent page. Having domain name prominently displayed on the consent page will provide some hint to the user where he is heading to. Also the image of the application on the consent page misleads the user. The attacker has intentionally picked the Google Drive image there. If all these OAuth applications can go through an approval process, before launching into public, such mishaps can be prevented. Facebook already follows such a process. When you create a Facebook app, first, only the owner of the application can log in—to launch it to the public, it has to go through an approval process.

G Suite is widely used in the enterprise. Google can give the domain admins more control to whitelist, which applications the domain users can access from corporate credentials. This prevents users under phishing attacks, unknowingly sharing access to important company docs with third-party apps.

The phishing attack on Google is a good wake-up call to evaluate and think about how phishing resistance techniques can be occupied in different OAuth flows. For example, Google Chrome security team has put so much effort when they designed the Chrome warning page for invalid certificates. They did tons of research even to pick the color, the alignment of text, and what images to be displayed. Surely, Google will bring up more bright ideas to the table to fight against phishing.

## Summary

- OAuth 2.0 is the de facto standard for access delegation to cater real production use cases. There is a huge ecosystem building around it—with a massive adoption rate.

- Whenever you use OAuth, you should make sure that you follow and adhere to security best practices—and always use proven libraries and products, which already take care of enforcing the best practices.

- OAuth 2.0 Threat Model and Security Considerations (RFC 6819) produced by OAuth IETF working group defines additional security considerations for OAuth 2.0, beyond those in the OAuth 2.0 specification, based on a comprehensive threat model.

- The OAuth 2.0 Security Best Current Practice document, which is a draft proposal at the time of writing, talks about new threats related to OAuth 2.0, since the RFC 6819 was published.

- The Financial-grade API (FAPI) working group under OpenID Foundation has published a set of guidelines on how to use OAuth 2.0 in a secure way to build financial-grade applications.

 

15. Patterns and Practices

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

Throughout the book so far over 14 chapters and 7 appendices, we discussed different ways of securing APIs and the theoretical background behind those. In this chapter, we present a set of API security patterns to address some of the most common enterprise security problems.

Direct Authentication with the Trusted Subsystem

Suppose a medium-scale enterprise has a number of APIs. Company employees are allowed to access these APIs via a web application while they’re behind the company firewall. All user data are stored in Microsoft Active Directory (AD), and the web application is connected directly to the Active Directory to authenticate users. The web application passes the logged-in user’s identifier to the back-end APIs to retrieve data related to the user.

The problem is straightforward, and Figure 15-1 illustrates the solution. You need to use some kind of direct authentication pattern. User authentication happens at the front-end web application, and once the user is authenticated, the web application needs to access the back-end APIs. The catch here is that the web application passes the logged-in user’s identifier to the APIs. That implies the web application needs to invoke APIs in a user-aware manner.

Since both the web application and the APIs are in the same trust domain, we only authenticate the end user at the web application, and the back-end APIs trust whatever data passed on to those from the web application. This is called the trusted subsystem pattern. The web application acts as a trusted subsystem. In such case, the best way to secure APIs is through mutual Transport Layer Security (mTLS). All the requests generated from the web application are secured with mTLS, and no one but the web application can access the APIs (see Chapter 3).

 

Figure 15-1

Direct authentication with the trusted subsystem pattern

Some do resist using TLS due to the overhead it adds and rely on building a controlled environment, where security between the web application and the container that hosts APIs is governed at the network level. Network-level security must provide the assurance that no component other than the web application server can talk to the container that hosts the APIs. This is called the trust-the-network pattern, and over the time, this has become an antipattern. The opposite of the trust-the-network pattern is zero-trust network. With the zero-trust network pattern, we do not trust the network. When we do not trust the network, we need to make sure we have enforced security checks as much as closer to the resource (or in our case, the APIs). The use of mTLS to secure the APIs is the most ideal solution here.

Single Sign-On with the Delegated Access Control

Suppose a medium-scale enterprise has a number of APIs. Company employees are allowed to access these APIs via web applications while they’re behind the company firewall. All user data are stored in Microsoft Active Directory, and all the web applications are connected to an identity provider, which supports Security Assertion Markup Language (SAML) 2.0 to authenticate users. The web applications need to access back-end APIs on behalf of the logged-in user.

The catch here is the last statement: “The web applications need to access back-end APIs on behalf of the logged-in user.” This suggests the need for an access delegation protocol: OAuth 2.0. However, users don’t present their credentials directly to the web application—they authenticate through a SAML 2.0 identity provider.

In this case, you need to find a way to exchange the SAML token a web application receives via the SAML 2.0 Web SSO protocol for an OAuth access token, which is defined in the SAML grant type for the OAuth 2.0 specification (see Chapter 12). Once the web application receives the SAML token, as shown in step 3 of Figure 15-2, it has to exchange the SAML token to an access token by talking to the OAuth 2.0 authorization server.

 

Figure 15-2

Single sign-on with the Delegated Access Control pattern

The authorization server must trust the SAML 2.0 identity provider. Once the web application gets the access token, it can use it to access back-end APIs. The SAML grant type for OAuth 2.0 doesn’t provide a refresh token. The lifetime of the access token issued by the OAuth 2.0 authorization server must match the lifetime of the SAML token used in the authorization grant.

After the user logs in to the web application with a valid SAML token, the web application creates a session for the user from then onward, and it doesn’t worry about the lifetime of the SAML token. This can lead to some issues. Say, for example, the SAML token expires, but the user still has a valid browser session in the web application. Because the SAML token has expired, you can expect that the corresponding OAuth 2.0 access token obtained at the time of user login has expired as well. Now, if the web application tries to access a back-end API, the request will be rejected because the access token is expired. In such a scenario, the web application has to redirect the user back to the SAML 2.0 identity provider, get a new SAML token, and exchange that token for a new access token. If the session at the SAML 2.0 identity provider is still live, then this redirection can be made transparent to the end user.

Single Sign-On with the Integrated Windows Authentication

Suppose a medium-scale enterprise that has a number of APIs. Company employees are allowed to access these APIs via multiple web applications while they’re behind the company firewall. All user data are stored in Microsoft Active Directory, and all the web applications are connected to a SAML 2.0 identity provider to authenticate users. The web applications need to access back-end APIs on behalf of the logged-in user. All the users are in a Windows domain, and once they’re logged in to their workstations, they shouldn’t be asked to provide credentials at any point for any other application.

The catch here is the statement, “All the users are in a Windows domain, and once they’re logged in to their workstations, they shouldn’t be asked to provide credentials at any point for any other application.”

You need to extend the solution we provided using single sign-on (SSO) with the Delegated Access Control pattern (the second pattern). In that case, the user logs in to the SAML 2.0 identity provider with their Active Directory username and password. Here, this isn’t acceptable. Instead, you can use Integrated Windows Authentication (IWA) to secure the SAML 2.0 identity provider. When you configure the SAML 2.0 identity provider to use IWA, then once the user is redirected to the identity provider for authentication, the user is automatically authenticated; as in the case of SSO with the Delegated Access Control pattern, a SAML response is passed to the web application. The rest of the flow remains unchanged.

Identity Proxy with the Delegated Access Control

Suppose a medium-scale enterprise has a number of APIs. Company employees, as well as employees from trusted partners, are allowed to access these APIs via web applications. All the internal user data are stored in Microsoft Active Directory, and all the web applications are connected to a SAML 2.0 identity provider to authenticate users. The web applications need to access back-end APIs on behalf of logged-in users.

 

Figure 15-3

Identity proxy with the Delegated Access Control pattern

This use case is an extension of using SSO with the Delegated Access Control pattern. The catch here is the statement, “company employees, as well as employees from trusted partners, are allowed to access these APIs via web applications.” You now have to go beyond the company domain. Everything in Figure 15-2 remains unchanged. The only thing you need to do is to change the authentication mechanism at the SAML 2.0 identity provider (see Figure 15-3).

Regardless of the end user’s domain, the client web application only trusts the identity provider in its own domain. Internal as well as external users are first redirected to the internal (or local) SAML identity provider. The local identity provider should offer the user the option to pick whether to authenticate with their username and password (for internal users) or to pick their corresponding domain. Then the identity provider can redirect the user to the corresponding identity provider running in the external user’s home domain. Now the external identity provider returns a SAML response to the internal identity provider.

The external identity provider signs this SAML token. If the signature is valid, and if it’s from a trusted external identity provider, the internal identity provider issues a new SAML token signed by itself to the calling application. The flow then continues as shown in Figure 15-2.

> **Note**
>


One benefit of this approach is that the internal applications only need to trust their own identity provider. The identity provider handles the brokering of trust between other identity providers outside its domain. In this scenario, the external identity provider also talks SAML, but that can’t be expected all the time. There are also identity providers that support other protocols. In such scenarios, the internal identity provider must be able to transform identity assertions between different protocols.

Delegated Access Control with the JSON Web Token

Suppose a medium-scale enterprise that has a number of APIs. Company employees are allowed to access these APIs via web applications while they’re behind the company firewall. All user data are stored in Microsoft Active Directory, and all the web applications are connected to an OpenID Connect identity provider to authenticate users. The web applications need to access back-end APIs on behalf of the logged-in user.

This use case is also an extension of the SSO with the Delegated Access Control pattern. The catch here is the statement, “all the web applications are connected to an OpenID Connect identity provider to authenticate users.” You need to replace the SAML identity provider shown in Figure 15-2 with an OpenID Connect identity provider, as illustrated in Figure 15-4. This also suggests the need for an access delegation protocol (OAuth).

In this case, however, users don’t present their credentials directly to the web application; rather, they authenticate through an OpenID Connect identity provider. Thus, you need to find a way to exchange the ID token received in OpenID Connect authentication for an OAuth access token, which is defined in the JWT grant type for OAuth 2.0 specification (Chapter 12). Once the web application receives the ID token in step 3, which is also a JWT, it has to exchange it for an access token by talking to the OAuth 2.0 authorization server. The authorization server must trust the OpenID Connect identity provider. When the web application gets the access token, it can use it to access back-end APIs.

 

Figure 15-4

Delegated Access Control with the JWT pattern

> **Note**
>


Why would someone exchange the ID token obtained in OpenID Connect for an access token when it directly gets an access token along with the ID token? This is not required when both the OpenID Connect server and the OAuth authorization server are the same. If they aren’t, you have to use the JWT Bearer grant type for OAuth 2.0 and exchange the ID token for an access token. The access token issuer must trust the OpenID Connect identity provider.

Nonrepudiation with the JSON Web Signature

Suppose a medium-scale enterprise in the finance industry needs to expose an API to its customers through a mobile application, as illustrated in Figure 15-5. One major requirement is that all the API calls should support nonrepudiation.

The catch here is the statement, “all the API calls should support nonrepudiation.” When you do a business transaction via an API by proving your identity, you shouldn’t be able to reject it later or repudiate it. The property that ensures the inability to repudiate is known as nonrepudiation . Basically, you do it once, and you own it forever (see Chapter 2 for details).

Nonrepudiation should provide proof of the origin and the integrity of data in an unforgeable manner, which a third party can verify at any time. Once a transaction is initiated, none of its content, including the user identity, date, time, and transaction details, should be altered while in transit, in order to maintain transaction integrity and to allow for future verifications. Nonrepudiation has to ensure that the transaction is unaltered and logged after it’s committed and confirmed.

 

Figure 15-5

Nonrepudiation with the JSON Web Signature pattern

Logs must be archived and properly secured to prevent unauthorized modifications. Whenever there is a repudiation dispute, transaction logs, along with other logs or data, can be retrieved to verify the initiator, date, time, transaction history, and so on. The way to achieve nonrepudiation is via signature. A key known only to the end user should sign each message.

In this case, the financial institution must issue a key pair to each of its customers, signed by a certificate authority under its control. It should only store the corresponding public certificate, not the private key. The customer can install the private key in his or her mobile device and make it available to the mobile application. All API calls generated from the mobile application must be signed by the private key of the user and encrypted by the public key of the financial institution.

To sign the message, the mobile application can use JSON Web Signature (see Chapter 7); and for encryption, it can use JSON Web Encryption (see Chapter 8). When using both the signature and encryption on the same payload, the message must be signed first, and then the signed payload must be encrypted for legal acceptance.

Chained Access Delegation

Suppose a medium-scale enterprise that sells bottled water has an API (Water API) that can be used to update the amount of water consumed by a registered user. Any registered user can access the API via any client application. It could be an Android app, an iOS app, or even a web application.

The company only provides the API—anyone can develop client applications to consume it. All the user data of the Water API are stored in Microsoft Active Directory. The client applications shouldn’t be able to access the API directly to find out information about users. Only the registered users of the Water API can access it. These users should only be able to see their own information. At the same time, for each update made by a user, the Water API must update the user’s healthcare record maintained at MyHealth.org. The user also has a personal record at MyHealth.org, and it too exposes an API (MyHealth API). The Water API has to invoke the MyHealth API to update the user record on the user’s behalf.

In summary, a mobile application accesses the Water API on behalf of the end user, and then the Water API has to access the MyHealth API on behalf of the end user. The Water API and the MyHealth API are in two independent domains. This suggests the need for an access delegation protocol.

 

Figure 15-6

Chained Access Delegation pattern

Again, the catch here is the statement, “the Water API must also update the user’s healthcare record maintained at MyHealth.org.” This has two solutions. In the first solution, the end user must get an access token from MyHealth.org for the Water API (the Water API acts as the OAuth client), and then the Water API must store the token internally against the user’s name. Whenever the user sends an update through a mobile application to the Water API, the Water API first updates its own record and then finds the MyHealth access token corresponding to the end user and uses it to access the MyHealth API. With this approach, the Water API has the overhead of storing the MyHealth API access token, and it should refresh the access token whenever needed.

The second solution is explained in Figure 15-6. It’s built around the OAuth 2.0 Token Delegation profile (see Chapter 9). The mobile application must carry a valid access token to access the Water API on behalf of the end user. In step 3, the Water API talks to its own authorization server to validate the access token. Then, in step 4, the Water API exchanges the access token it got from the mobile application for a JWT access token. The JWT access token is a special access token that carries some meaningful data, and the authorization server in the Water API’s domain signs it. The JWT includes the end user’s local identifier (corresponding to the Water API) as well as its mapped identifier in the MyHealth domain. The end user must permit this action at the Water API domain.

In step 6, the Water API accesses the MyHealth API using the JWT access token. The MyHealth API validates the JWT access token by talking to its own authorization server. It verifies the signature; and, if it’s signed by a trusted entity, the access token is treated as valid.

Because the JWT includes the mapped username from the MyHealth domain, it can identify the corresponding local user record. However, this raises a security concern. If you let users update their profiles in the Water API domain with the mapped MyHealth identifier, they can map it to any user identifier, and this leads to a security hole. To avoid this, the account mapping step must be secured with OpenID Connect authentication. When the user wants to add his or her MyHealth account identifier, the Water API domain initiates the OpenID Connect authentication flow and receives the corresponding ID token. Then the account mapping is done with the user identifier in the ID token.

Trusted Master Access Delegation

Suppose a large-scale enterprise that has a number of APIs. The APIs are hosted in different departments, and each department runs its own OAuth 2.0 authorization server due to vendor incompatibilities in different deployments. Company employees are allowed to access these APIs via web applications while they’re behind the company firewall, regardless of the department which they belong to.

 

Figure 15-7

Trusted Master Access Delegation pattern

All user data are stored in a centralized Active Directory, and all the web applications are connected to a centralized OAuth 2.0 authorization server (which also supports OpenID Connect) to authenticate users. The web applications need to access back-end APIs on behalf of the logged-in user. These APIs may come from different departments, each of which has its own authorization server. The company also has a centralized OAuth 2.0 authorization server, and an employee having an access token from the centralized authorization server must be able to access any API hosted in any department.

Once again, this is an extended version of using SSO with the Delegated Access Control pattern. You have a master OAuth 2.0 authorization server and a set of secondary authorization servers. An access token issued from the master authorization server should be good enough to access any of the APIs under the control of the secondary authorization servers. In other words, the access token returned to the web application, as shown in step 3 of Figure 15-7, should be good enough to access any of the APIs.

To make this possible, you need to make the access token self-contained. Ideally, you should make the access token a JWT with the iss (issuer) field. In step 4, the web application accesses the API using the access token; and in step 5, the API talks to its own authorization server to validate the token. The authorization server can look at the JWT header and find out whether it issued this token or if a different server issued it. If the master authorization server issued it, then the secondary authorization server can talk to the master authorization server’s OAuth introspection endpoint to find out more about the token. The introspection response specifies whether the token is active and identifies the scopes associated with the access token. Using the introspection response, the secondary authorization server can build an eXtensible Access Control Markup Language (XACML) request and call a XACML policy decision point (PDP). If the XACML response is evaluated to permit, then the web application can access the API. Then again XACML is a little too complex in defining access control policies, irrespective of how powerful it is. You can also check the Open Policy Agent (OPA) project, which has become quite popular recently in building fine-grained access control policies.

Resource Security Token Service (STS) with the Delegated Access Control

Suppose a global organization has APIs and API clients are distributed across different regions. Each region operates independently from the others. Currently, both clients and APIs are nonsecured. You need to secure the APIs without making any changes either at the API or the client end.

The solution is based on a simple theory in software engineering: introducing a layer of indirection can solve any problem. You need to introduce two interceptors. One sits in the client region, and all the nonsecured messages generated from the client are intercepted. The other interceptor sits in the API region, and all the API requests are intercepted. No other component except this interceptor can access the APIs in a nonsecured manner.

 

Figure 15-8

Resource STS with the Delegated Access Control pattern

This restriction can be enforced at the network level. Any request generated from outside has no path to the API other than through the API interceptor. Probably you deploy both API interceptor and the API in the same physical machine. You can also call this component a policy enforcement point (PEP) or API gateway. The PEP validates the security of all incoming API requests. The interceptor’s responsibility, sitting in the client region, is to add the necessary security parameters to the nonsecured messages generated from the client and to send it to the API. In this way, you can secure the API without making changes at either the client or the API end.

Still, you have a challenge. How do you secure the API at the API gateway? This is a cross-domain scenario, and the obvious choice is to use JWT grant type for OAuth 2.0. Figure 15-8 explains how the solution is implemented. Nonsecured requests from the client application are captured by the interceptor component in step 1. Then it has to talk to its own security token service (STS). In step 2, the interceptor uses a default user account to access the STS using OAuth 2.0 client credentials grant type. The STS authenticates the request and issues a self-contained access token (a JWT), having the STS in the API region as the audience of the token.

In step 3, the client-side interceptor authenticates to the STS at the API region with the JWT token and gets a new JWT token, following OAuth 2.0 Token Delegation profile, which we discussed in Chapter 9. The audience of the new JWT is the OAuth 2.0 authorization server running in the API region. Before issuing the new JWT, the STS at the API region must validate its signature and check whether a trusted entity has signed it.

To make this scenario happen, the STS in the API region must trust the STS on the client side. The OAuth 2.0 authorization server only trusts its own STS. That is why step 4 is required. Step 4 initiates the JWT grant type for OAuth 2.0, and the client interceptor exchanges the JWT issued by the STS of the API region for an access token. Then it uses that access token to access the API in step 5.

The PEP in the API region intercepts the request and calls the authorization server to validate the access token. If the token is valid, the PEP lets the request hit the API (step 7).

Delegated Access Control with No Credentials over the Wire

Suppose a company wants to expose an API to its employees. However, user credentials must never go over the wire. This is a straightforward problem with an equally straightforward solution. Both OAuth 2.0 bearer tokens and HTTP Basic authentication take user credentials over the wire. Even though both these approaches use TLS for protection, still some companies worry about passing user credentials over communication channels—or in other words passing bearer tokens over the wire.

You have few options: use either HTTP Digest authentication or OAuth 2.0 MAC tokens (Appendix G). Using OAuth 2.0 MAC tokens is the better approach because the access token is generated for each API, and the user can also revoke the token if needed without changing the password. However, the OAuth 2.0 MAC token profile is not matured yet. The other approach is to use OAuth 2.0 with Token Binding, which we discussed in Chapter 11. Even though we use bearer tokens there, with Token Binding, we bind the token to the underneath TLS channel—so no one can export the token and use it somewhere else.

There are few more draft proposals discussed under the IETF OAuth working group to address this concern. The OAuth 2.0 Mutual-TLS Client Authentication and Certificate-Bound Access Tokens is one of them, available at https://tools.ietf.org/html/draft-ietf-oauth-mtls-17.

## Summary

- API security is an ever-evolving subject.

- More and more standards and specifications are popping up, and most of them are built around the core OAuth 2.0 specification.

- Security around JSON is another evolving area, and the IETF JOSE working group is currently working on it.

- It’s highly recommended that if you wish to continue beyond this book, you should keep an eye on the IETF OAuth working group, the IETF JOSE working group, the OpenID Foundation, and the Kantara Initiative.

 

The Evolution of Identity Delegation

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

Identity delegation plays a key role in securing APIs. Most of the resources on the Web today are exposed over APIs. The Facebook API exposes your Facebook wall, the Twitter API exposes your Twitter feed, Flickr API exposes your Flickr photos, Google Calendar API exposes your Google Calendar, and so on. You could be the owner of a certain resource (Facebook wall, Twitter feed, etc.) but not the direct consumer of an API. There may be a third party who wants to access an API on your behalf. For example, a Facebook app may want to import your Flickr photos on behalf of you. Sharing credentials with a third party who wants to access a resource you own on your behalf is an antipattern. Most web-based applications and APIs developed prior to 2006 utilized credential sharing to facilitate identity delegation. Post 2006, many vendors started developing their own proprietary ways to address this concern without credential sharing. Yahoo! BBAuth, Google AuthSub, and Flickr Authentication are some of the implementations that became popular.

A typical identity delegation model has three main roles: delegator, delegate, and service provider. The delegator owns the resource and is also known as the resource owner. The delegate wants to access a service on behalf of the delegator. The delegator delegates a limited set of privileges to the delegate to access the service. The service provider hosts the protected service and validates the legitimacy of the delegate. The service provider is also known as the resource server.

Direct Delegation vs. Brokered Delegation

Let’s take a step back and look at a real-world example (see Figure A-1). Flickr is a popular cloud-based service for storing and sharing photos. Photos stored in Flickr are the resources, and Flickr is the resource server or the service provider. Say you have a Flickr account: you’re the resource owner (or the delegator) of the photos under your account. You also have a Snapfish account. Snapfish is a web-based photo-sharing and photo-printing service that is owned by Hewlett-Packard. How can you print your Flickr photos from Snapfish? To do so, Snapfish has to first import those photos from Flickr and should have the privilege to do so, which should be delegated to Snapfish by you. You’re the delegator, and Snapfish is the delegate. Other than the privilege to import photos, Snapfish won’t be able to do any of the following with your Flickr photos:

- Access your Flickr account (including private content)

- Upload, edit, and replace photos and videos in the account

- Interact with other members’ photos and videos (comment, add notes, favorite)

 

Figure A-1

Direct delegation. The resource owner delegates privileges to the client application

Snapfish can now access your Flickr account on your behalf with the delegated privileges. This model is called direct delegation : the delegator directly delegates a subset of his or her privileges to a delegate. The other model is called indirect delegation : the delegator first delegates to an intermediate delegate, and that delegate delegates to another delegate. This is also known as brokered delegation (see Figure A-2).

 

Figure A-2

Brokered delegation. The resource owner delegates privileges to an intermediate application and that application delegates privileges to another application

Let’s say you have a Lucidchart account. Lucidchart is a cloud-based design tool that you can use to draw a wide variety of diagrams. It also integrates with Google Drive. From your Lucidchart account, you have the option to publish completed diagrams to your Google Drive. To do that, Lucidchart needs privileges to access the Google Drive API on your behalf, and you need to delegate the relevant permissions to Lucidchart. If you want to print something from Lucidchart, it invokes the Snapfish printing API. Snapfish needs to access the diagrams stored in your Google Drive. Lucidchart has to delegate a subset of the permissions you delegated to it to Snapfish. Even though you granted read/write permissions to Lucidchart, it only has to delegate read permission to Snapfish to access your Google Drive and print the selected drawings.

The Evolution

The modern history of identity delegation can be divided into two eras: pre-2006 and post-2006. Credential sharing mostly drove identity delegation prior to 2006. Twitter, SlideShare, and almost all the web applications used credential sharing to access third-party APIs. As shown in Figure A-3, when you created a Twitter account prior to 2006, Twitter asked for your email account credentials so it could access your email address book and invite your friends to join Twitter. Interestingly, it displayed the message “We don’t store your login, your password is submitted securely, and we don’t email without your permission” to win user confidence. But who knows—if Twitter wanted to read all your emails or do whatever it wanted to your email account, it could have done so quite easily.

 

Figure A-3

Twitter, pre-2006

SlideShare did the same thing. SlideShare is a cloud-based service for hosting and sharing slides. Prior to 2006, if you wanted to publish a slide deck from SlideShare to a Blogger blog, you had to give your Blogger username and password to SlideShare, as shown in Figure A-4. SlideShare used Blogger credentials to access its API to post the selected slide deck to your blog. If SlideShare had wanted to, it could have modified published blog posts, removed them, and so on.

 

Figure A-4

SlideShare, pre-2006

These are just two examples. The pre-2006 era was full of such applications. Google Calendar, introduced in April 2006, followed a similar approach. Any third-party application that wanted to create an event in your Google Calendar first had to request your Google credentials and use them to access the Google Calendar API. This wasn’t tolerable in the Internet community, and Google was pushed to invent a new and, of course, better way of securing its APIs. Google AuthSub was introduced toward the end of 2006 as a result. This was the start of the post-2006 era of identity delegation.

Google ClientLogin

In the very early stages of its deployment, the Google Data API was secured with two nonstandard security protocols: ClientLogin and AuthSub. ClientLogin was intended to be used by installed applications. An installed application can vary from a simple desktop application to a mobile application—but it can’t be a web application. For web applications, the recommended way was to use AuthSub.

> **Note**
>


The complete Google ClientLogin documentation is available at https://developers.google.com/accounts/docs/AuthForInstalledApps. The ClientLogin API was deprecated as of April 20, 2012. According to the Google deprecation policy, it operated the same until April 20, 2015.

As shown in Figure A-5, Google ClientLogin uses identity delegation with password sharing. The user has to share his Google credentials with the installed application in the first step. Then the installed application creates a request token out of the credentials, and it calls the Google Accounts Authorization service. After the validation, a CAPTCHA challenge is sent back as the response. The user must respond to the CAPTCHA and is validated again against the Google Accounts Authorization service. Once the user is validated successfully, a token is issued to the application. Then the application can use the token to access Google services.

 

Figure A-5

Google ClientLogin

Google AuthSub

Google AuthSub was the recommended authentication protocol to access Google APIs via web applications in the post-2006 era. Unlike ClientLogin, AuthSub doesn’t require credential sharing. Users don’t need to provide credentials for a third-party web application—instead, they provide credentials directly to Google, and Google shares a temporary token with a limited set of privileges with the third-party web application. The third-party application uses the temporary token to access Google APIs. Figure A-6 explains the protocol flow in detail.

 

Figure A-6

Google AuthSub

The end user initiates the protocol flow by visiting the web application. The web application redirects the user to the Google Accounts Authorization service with an AuthSub request. Google notifies the user of the access rights (or the privileges) requested by the application, and the user can approve the request by login. Once approved by the user, Google Accounts Authorization service provides a temporary token to the web application. Now the web application can use that temporary token to access Google APIs.

> **Note**
>


The complete Google AuthSub documentation is available at https://developers.google.com/accounts/docs/AuthSub. How to use AuthSub with the Google Data API is explained at https://developers.google.com/gdata/docs/auth/authsub. The AuthSub API was deprecated as of April 20, 2012. According to the Google deprecation policy, it operated the same until April 20, 2015.

Flickr Authentication API

Flickr is a popular image/video hosting service owned by Yahoo!. Flickr was launched in 2004 (before the acquisition by Yahoo! in 2005), and toward 2005 it exposed its services via a public API. It was one of the very few companies at that time that had a public API; this was even before the Google Calendar API. Flickr was one of the very few applications that followed an identity delegation model without credential sharing prior to 2006. Most of the implementations that came after that were highly influenced by the Flickr Authentication API. Unlike in Google AuthSub or ClientLogin, the Flickr model was signature based. Each request should be signed by the application from its application secret.

Yahoo! Browser–Based Authentication (BBAuth)

Yahoo! BBAuth was launched in September 2006 as a generic way of granting third-party applications access to Yahoo! data with a limited set of privileges. Yahoo! Photos and Yahoo! Mail were the first two services to support BBAuth. BBAuth, like Google AuthSub, borrowed the same concept used in Flickr (see Figure A-7).

 

Figure A-7

Yahoo! BBAuth

The user first initiates the flow by visiting the third-party web application. The web application redirects the user to Yahoo!, where the user has to log in and approve the access request from the third-party application. Once approved by the user, Yahoo! redirects the user to the web application with a temporary token. Now the third-party web application can use the temporary token to access user’s data in Yahoo! with limited privileges.

> **Note**
>


The complete guide to Yahoo! BBAuth is available at http://developer.yahoo.com/bbauth/.

OAuth

Google AuthSub, Yahoo! BBAuth, and Flickr Authentication all made considerable contributions to initiate a dialog to build a common standardized delegation model. OAuth 1.0 was the first step toward identity delegation standardization. The roots of OAuth go back to November 2006, when Blaine Cook started developing an OpenID implementation for Twitter. In parallel, Larry Halff of Magnolia (a social bookmarking site) was thinking about integrating an authorization model with OpenID (around this time, OpenID began gaining more traction in the Web 2.0 community). Larry started discussing the use of OpenID for Magnolia with Twitter and found out there is no way to delegate access to Twitter APIs through OpenID. Blaine and Larry, together with Chris Messina, DeWitt Clinton, and Eran Hammer, started a discussion group in April 2007 to build a standardized access delegation protocol—which later became OAuth. The access delegation model proposed in OAuth 1.0 wasn’t drastically different from what Google, Yahoo!, and Flickr already had.

> **Note**
>


OpenID is a standard developed by the OpenID Foundation for decentralized single sign-on. The OpenID 2.0 final specification is available at http://openid.net/specs/openid-authentication-2_0.html.

The OAuth 1.0 core specification was released in December 2007. Later, in 2008, during the 73rd Internet Engineering Task Force (IETF) meeting, a decision was made to develop OAuth under the IETF. It took some time to be established in the IETF, and OAuth 1.0a was released as a community specification in June 2009 to fix a security issue related to a session fixation attack.1 In April 2010, OAuth 1.0 was released as RFC 5849 under the IETF.

> **Note**
>


The OAuth 1.0 community specification is available at http://oauth.net/core/1.0/, and OAuth 1.0a is at http://oauth.net/core/1.0a/. Appendix B explains OAuth 1.0 in detail.

In November 2009, during the Internet Identity Workshop (IIW), Dick Hardt of Microsoft, Brian Eaton of Google, and Allen Tom of Yahoo! presented a new draft specification for access delegation. It was called Web Resource Authorization Profiles (WRAP), and it was built on top of the OAuth 1.0 model to address some of its limitations. In December 2009, WRAP was deprecated in favor of OAuth 2.0.

> **Note**
>


The WRAP specification contributed to the IETF OAuth working group is available at http://tools.ietf.org/html/draft-hardt-oauth-01.

While OAuth was being developed under the OAuth community and the IETF working group, the OpenID community also began to discuss a model to integrate OAuth with OpenID. This effort, initiated in 2009, was called OpenID/OAuth hybrid extension (see Figure A-8). This extension describes how to embed an OAuth approval request into an OpenID authentication request to allow combined user approval. For security reasons, the OAuth access token isn’t returned in the OpenID authentication response. Instead, a mechanism to obtain the access token is provided.

> **Note**
>


The finalized specification for OpenID/OAuth extension is available at http://step2.googlecode.com/svn/spec/openid_oauth_extension/latest/openid_oauth_extension.html.

 

Figure A-8 The evolution of identity protocols from OpenID to OpenID Connect

OAuth 1.0 provided a good foundation for access delegation. However, criticism arose against OAuth 1.0, mainly targeting its usability and extensibility. As a result, OAuth 2.0 was developed as an authorization framework, rather than a standard protocol. OAuth 2.0 became the RFC 6749 in October 2012 under the IETF.

