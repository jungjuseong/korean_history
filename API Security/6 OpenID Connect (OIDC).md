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

OpenID Connect는 OAuth2 위에 식별 레이어를 도입한다. 이 식별 레이어는 ID 토큰으로 추상화되며 7장에서 자세히 다룬다. OAuth2 인증 서버는 액세스 토큰과 함께 ID 토큰을 리턴한다.

OpenID Connect는 OAuth2 위에 구축된 프로파일이다. OAuth는 액세스 위임에 관한 것이라면 OpenID Connect는 인가에 대한 것이다. 즉, OIDC는 OAuth2 위에 식별 레이어를 구축한다.

인증은 데이터 또는 엔티티의 속성이 참인가를 확인하는 행동이다. 내가 나를 피터라고 말한다면 나는 이를 증명할 필요가 있다. 내가 아는 무엇 또는 내가 가진 무엇인가로 증명할 수 있다. 내가 나임을 증명하였다면 시스템이 나를 신뢰할 수 있다. 때때로 시스템은 이름만으로 사용자를 식별하기를 원하지 않는다. 이름이 고유하게 식별하는데 도움이 될 수도 있지만 다른 속성은 어떨까? 국경을 통과하려면 이름으로, 사진이나 지문 홍채로 식별하기도 한다. VISA 오피스에도 실시간으로 식별에 사용한다. VISA를 가진 사람이 동일한 인물임을 확인하고 입국을 승인한다.    

이것이 여러분의 신분을 제공한다. 신분을 증명하는 것이 인증이다. 인가authorization는 여러분이 할 수 있는 권한 또는 능력에 관한 것이다. 

국경에서 여러분의 신분을 증명하는데 사진, 지문, 홍채등을 사용하지만 여러분이 할 수 있는 일은 VISA가 결정한다. 입국을 하려면 유효기간이 남은 비자가 있어야 한다. 유효한 비자는 여러분을 식별하는 부분이 아니고 여러분이 무엇을 할 수 있는지를 결정한다. 입국한 나라에서 가능한 일은 비자 타입이 결정한다. B1/B2 비자와 L1/L2 비자는 다른다. 이것이 인가이다.

OAuth2는 인증이 아니라 인가에 관한 것이다. OAuth2로는 클라이언트가 사용자에 관해 모른다. 사용자 대신 리소스에 접근하기 위한 액세스 토큰을 얻는다. OpenID Connect로는 클라이언트는 액세스 토큰과 함께 ID 토큰을 얻는다. ID 토큰은 사용자의 신분을 나타낸다. 

OpenID Connect로 API를 보호한다는 것이 어떤 의미인가? 또는 완전히 의미없는가? OpenID Connect는 API 레벨이나 리소스 서버 레벨이 아니라 애플리케이션 레벨 또는 클라이언트 레벨에 있다. OpenID Connect는 클라이언트가 사용자가 누군지를 발견하는데 도움을 주지만 API에게는 의미가 없다. API가 기대하는 유일한 것은 액세스 토큰이다. 리소스 소유자 또는 API가 사용자에 대해 알고 싶다면 인가 서버에게 질의를 하거나 JWT와 같은 자체 정보가 들어있는 액세스 토큰을 참조한다.

### Anatomy of the ID Token

ID 토큰은 OAuth2가 OpenID Connect를 지원하기 위한 주요 애드온이다. 이것은 인증 서버로부터 클라이언트에게 인증된 사용자 정보를 운반하는 JWT이다. 
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

- iss: 토큰 발행자(인증서버 또는 식별제공자)의 식별자. In practice, most of the OpenID Provider implementations or products let you configure an issuer you want—and also this is mostly being used as an identifier, rather than a URL. This is a required attribute in the ID token.

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
> The authorization server must sign the ID token, as defined in JSON Web Signature (JWS) specification. Optionally, it can also be encrypted. Token encryption should follow the rules defined in the JSON Web Encryption (JWE) specification. If the ID token is encrypted, it must be signed first and then encrypted. This is because signing the encrypted text is questionable in many legal entities. Chapters 7 and 8 talk about JWT, JWS, and JWE.

### OpenID Connect with WSO2 Identity Server

In this exercise, you see how to obtain an OpenID Connect ID token along with an OAuth 2.0 access token. Here we run the WSO2 Identity Server as the OAuth 2.0 authorization server.

> **Note**
>
> WSO2 Identity Server is a free, open source identity and entitlement management server, released under the Apache 2.0 license. At the time of this writing, the latest released version is 5.9.0 and runs on Java 8.

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
> Signing or encrypting the response message from the UserInfo endpoint isn’t a requirement. If it’s signed or encrypted, then the response should be wrapped in a JWT, and the Content-Type of the response should be set to application/jwt.

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
> When id_token is being used as the response_type in an OpenID Connect flow, the client application never has access to an access token. In such a scenario, the client application can use the scope parameter to request attributes, and those are added to the id_token.

### Requesting Custom User Attributes

As discussed before, OpenID Connect defines 20 standard claims. These claims can be requested via the scope parameter or through the claims parameter. The only way to request custom-defined claims is through the claims parameter. The following is a sample OpenID Connect request that asks for custom-defined claims:
```
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
```

### OpenID Connect Discovery

At the beginning of the chapter, we discussed how OpenID relying parties discover OpenID providers through the user-provided OpenID (which is a URL). OpenID Connect Discovery addresses the same concern, but in a different way (see Figure 6-2). In order to authenticate users via OpenID Connect, the OpenID Connect relying party first needs to figure out what authorization server is behind the end user. OpenID Connect utilizes the WebFinger (RFC 7033) protocol for this discovery.

> **Note**
>
> The OpenID Connect Discovery specification is available at http://openid.net/specs/openid-connect-discovery-1_0.html. If a given OpenID Connect relying party already knows who the authorization server is, it can simply ignore the discovery phase.


Figure 6-2

### OpenID Connect Discovery

Let’s assume a user called Peter visits an OpenID Connect relying party and wants to log in (see Figure 6-2). To authenticate Peter, the OpenID Connect relying party should know the authorization server corresponding to Peter. To discover this, Peter has to provide to the relying party some unique identifier that relates to him. Using this identifier, the relying party should be able to find the WebFinger endpoint corresponding to Peter.

Let’s say that the identifier Peter provides is his email address, peter@apress.com (step 1). The relying party should be able to find enough detail about the WebFinger endpoint using Peter’s email address. In fact, the relying party should be able to derive the WebFinger endpoint from the email address. The relying party can then send a query to the WebFinger endpoint to find out which authorization server (or the identity provider) corresponds to Peter (steps 2 and 3). This query is made according to the WebFinger specification. The following shows a sample WebFinger request for peter@apress.com:
```
GET /.well-known/webfinger?resource=acct:peter@apress.com
&rel=http://openid.net/specs/connect/1.0/issuer HTTP/1.1
Host: apress.com
```
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
> Neither the WebFinger nor the OpenID Connect Discovery specification mandates the use of the email address as the resource or the end user identifier. It must be a URI that conforms to the URI definition in RFC 3986, which can be used to derive the WebFinger endpoint. If the resource identifier is an email address, then it must be prefixed with acct.

The acct is a URI scheme as defined in http://tools.ietf.org/html/draft-ietf-appsawg-acct-uri-07. When the acct URI scheme is being used, everything after the @ sign is treated as the hostname. The WebFinger hostname is derived from an email address as per the acct URI scheme, which is the part after the @ sign.

If a URL is being used as the resource identifier, the hostname (and port number) of the URL is treated as the WebFinger hostname. If the resource identifier is https://auth.server.com:9443/prabath, then the WebFinger hostname is auth.server.com:9443.

Once the endpoint of the identity provider is discovered, that concludes the role of WebFinger. Yet you don’t have enough data to initiate an OpenID Connect authentication request with the corresponding identity provider. You can find more information about the identity provider by talking to its metadata endpoint, which must be a well-known endpoint (steps 4 and 5 in Figure 6-2). After that, for the client application to talk to the authorization server, it must be a registered client application. The client application can talk to the client registration endpoint of the authorization server (steps 6 and 7) to register itself—and then can access the authorize and token endpoints (steps 8 and 9).

> **Note**
>
> Both the WebFinger and OpenID Connect Discovery specifications use the Defining Well-Known URIs (http://tools.ietf.org/html/rfc5785) specification to define endpoint locations. The RFC 5785 specification introduces a path prefix called /.well-known/ to identify well-known locations. Most of the time, these locations are metadata endpoints or policy endpoints.

The WebFinger specification has the well-known endpoint /.well-known/webfinger. The OpenID Connect Discovery specification has the well-known endpoint for OpenID provider configuration metadata, /.well-known/openid-configuration.

### OpenID Connect Identity Provider Metadata

An OpenID Connect identity provider, which supports metadata discovery, should host its configuration at the endpoint /.well-known/openid-configuration. In most cases, this is a nonsecured endpoint, which can be accessed by anyone. An OpenID Connect relying party can send an HTTP GET to the metadata endpoint to retrieve the OpenID provider configuration details as follows:
```
GET /.well-known/openid-configuration HTTP/1.1
Host: auth.server.com
```
This results in the following JSON response, which includes everything an OpenID Connect relying party needs to know to talk to the OpenID provider or the OAuth authorization server:

```
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
```

> **Note**
>
> If the endpoint of the discovered identity provider is https://auth.server.com, then the OpenID provider metadata should be available at https://auth.server.com/.well-known/openid-configuration. If the endpoint is https://auth.server.com/openid, then the metadata endpoint is https://auth.server.com/openid/.well-known/openid-configuration.

### Dynamic Client Registration

Once the OpenID provider endpoint is discovered via `WebFinger` (and all the metadata related to it through OpenID Connect Discovery), the OpenID Connect relying party still needs to have a client ID and a client secret (not under the implicit grant type) registered at the OpenID provider to initiate the authorization grant request or the OpenID Connect authentication request. The OpenID Connect Dynamic Client Registration specification2 facilitates a mechanism to register dynamically OpenID Connect relying parties at the OpenID provider.

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
```
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
```
Once the OpenID Connect relying party obtains a client ID and a client secret, it concludes the OpenID Connect Discovery phase. The relying party can now initiate the OpenID Connect authentication request.

> **Note**
>
> Section 2.0 of the OpenID Connect Dynamic Client Registration specification lists all the attributes that can be included in an OpenID Connect client registration request: http://openid.net/specs/openid-connect-registration-1_0.html.

### OpenID Connect for Securing APIs

So far, you have seen a detailed discussion about OpenID Connect. But in reality, how will it help you in securing APIs? The end users can use OpenID Connect to authenticate into web applications, mobile applications, and much more. Nonetheless, why would you need OpenID Connect to secure a headless API? At the end of the day, all the APIs are secured with OAuth 2.0, and you need to present an access token to talk to the API. The API (or the policy enforcement component) validates the access token by talking to the authorization server. Why would you need to pass an ID token to an API?

OAuth is about delegated authorization, whereas OpenID Connect is about authentication. An ID token is an assertion about your identity, that is, a proof of your identity. It can be used to authenticate into an API. As of this writing, no HTTP binding is defined for JWT.

The following example suggests passing the JWT assertion (or the ID token) to a protected API as an access token in the HTTP Authorization header. The ID token, or the signed JWT, is base64-url-encoded in three parts. Each part is separated by a dot (.). The first part up to the first dot is the JWT header. The second part is the JWT body. The third part is the signature. Once the JWT is obtained by the client application, it can place it in the HTTP Authorization header in the manner shown here:
```
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
```
To validate the JWT, the API (or the policy enforcement component) has to extract the JWT assertion from the HTTP Authorization header, base64-url-decode it, and validate the signature to see whether it’s signed by a trusted issuer. In addition, the claims in the JWT can be used for authentication and authorization.

> **Note**
>
> When an OpenID Connect identity provider issues an ID token, it adds the aud parameter to the token to indicate the audience of the token. This can be an array of identifiers.

When using ID tokens to access APIs, a URI known to the API should also be added to the aud parameter. Currently this can’t be requested in the OpenID Connect authentication request, so it must be set out of band at the OpenID Connect identity provider.

## Summary

- OpenID Connect was built on top of OAuth 2.0. It introduces an identity layer on top of OAuth 2.0. This identity layer is abstracted into an ID token, which is a JSON Web Token (JWT).

- OpenID Connect evolved from OpenID to an OAuth 2.0 profile.

- The OpenID Connect Dynamic Client Registration specification facilitates a mechanism to register dynamically OpenID Connect relying parties at the OpenID provider.

- OpenID Connect defines two ways to request user attributes. The client application can either use the initial OpenID Connect authentication request to request attributes or else later talk to the UserInfo endpoint hosted by the authorization server.

- OpenID Connect utilizes the WebFinger protocol in its discovery process along with OpenID Connect dynamic client registration and identity provider metadata configuration.

- An OpenID Connect identity provider, which supports metadata discovery, should host its configuration at the endpoint /.well-known/openid-configuration.