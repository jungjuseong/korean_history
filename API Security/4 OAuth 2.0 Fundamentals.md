
# 4. OAuth 2.0 Fundamentals

OAuth 2.0 is a major breakthrough in identity delegation. It has its roots in OAuth 1.0 (see Appendix B), but OAuth Web Resource Authorization Profiles (see Appendix B) primarily influenced it. The main difference between OAuth 1.0 and 2.0 is that OAuth 1.0 is a standard protocol for identity delegation, whereas OAuth 2.0 is a highly extensible authorization framework. OAuth 2.0 is already the de facto standard for securing APIs and is widely used by Facebook, Google, LinkedIn, Microsoft, PayPal, Instagram, Foursquare, GitHub, Yammer, Meetup, and many more. There is one popular exception: Twitter still uses OAuth 1.0.

## Understanding OAuth 2.0

OAuth 2.0은 ID 위임의 주요 혁신니다. OAuth 1.0(부록 B 참조)에 뿌리를 두고 있지만 OAuth 웹 리소스 인증 프로필(부록 B 참조)이 주로 영향을 주었다. OAuth 1.0과 2.0의 주요 차이점은 OAuth 1.0이 ID 위임을 위한 표준 프로토콜인 반면 OAuth 2.0은 확장성이 뛰어난 인증 프레임워크라는 점이다. OAuth 2.0은 이미 API 보안을 위한 사실상의 표준이며 `Facebook`, `Google`, LinkedIn, `Microsoft`, `PayPal`, `Instagram`, Foursquare, `GitHub`, Yammer, Meetup 등에서 널리 사용됩니다. 한 가지 일반적인 예외가 있습니다. Twitter는 여전히 OAuth 1.0을 사용한다

1. 사용자가 타사 앱을 방문하고 앱이 자신의 Facebook 담벼락에 메시지를 게시하도록 하려고 한다. 이를 위해 앱은 Facebook의 토큰이 필요하고 토큰을 얻기 위해 사용자를 Facebook으로 리디렉션한다.

2. Facebook은 사용자에게 인증을 요청하고 메시지를 Facebook 담벼락에 게시할 수 있는 권한을 타사 앱에 부여하기 위해 사용자의 동의를 요청한다.

3. 사용자는 Facebook을 인증하고 Facebook에 동의를 제공하여 Facebook이 앱과 토큰을 공유할 수 있도록 한다. 이 토큰은 제한 기간 동안 Facebook 담벼락에 메시지를 게시하기에 충분하며 다른 작업은 할 수 없다. 사용자가 타사 앱을 방문하고 앱이 자신의 Facebook 담벼락에 메시지를 게시하도록 한다. 이를 위해 앱은 Facebook의 토큰이 필요하고 토큰을 얻기 위해 사용자를 Facebook으로 리디렉션한다.

2. Facebook은 사용자에게 인증을 요청하고(아직 인증되지 않은 경우) 메시지를 Facebook 담벼락에 게시할 수 있는 권한을 타사 앱에 부여하기 위해 사용자의 동의를 요청한다.

3. 사용자는 Facebook을 인증하고 Facebook에 동의를 제공하여 Facebook이 타사 앱과 토큰을 공유할 수 있도록 한다. 이 토큰은 제한된 기간 동안 Facebook 담벼락에 메시지를 게시할 수 있으나 다른 작업은 할 수 없다. 예를 들어, 타사 앱은 친구 요청을 보내고, 상태 메시지를 삭제하고, 사진을 업로드하는 등의 작업을 토큰으로 할 수 없다.

4. 타사 앱은 Facebook에서 토큰을 받는다. 이 단계에서 정확히 어떤 일이 발생하는지 설명하려면 먼저 OAuth 2.0 부여 유형이 작동하는 방식을 이해해야 하며 이 장의 뒷부분에서 이를 다룬다.

5. 타사 앱은 4단계에서 Facebook에서 제공한 토큰을 사용하여 Facebook API에 액세스한다. Facebook API는 유효한 토큰과 함께 제공되는 요청만 액세스할 수 있는지 확인한다. 그런 다음 이 장의 뒷부분에서 이 단계에서 어떤 일이 발생하는지 자세히 설명한다. 

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_4_Fig1_HTML.jpg)

Figure 4-1 OAuth 2.0 solves the access delegation problem by issuing a temporary time-bound token to a third-party web application that is only good enough for a well-defined purpose

## OAuth 2.0 Actors

OAuth 2.0 introduces four actors in a typical OAuth flow. The following explains the role of each of them with respect to Figure 4-1:

1. 리소스 소유자: 앞의 예에서 타사 앱은 Facebook API를 통해 Facebook 사용자의 Facebook 담벼락에 액세스하고 사용자를 대신하여 메시지를 게시하려고 한다. 이 경우 Facebook 담벼락을 소유한 Facebook 사용자가 리소스 소유자이다.

2. 리소스 서버: This is the place which hosts protected resources. In the preceding scenario, the server that hosts the Facebook API is the resource server, where Facebook API is the resource.

3. 클라이언트: 리소스 소유자를 대신하여 리소스에 액세스하려는 앱이다. 앞의 사용 사례에서 타사 앱은 클라이언트이다.

4. 인증 서버: 클라이언트에 OAuth 2.0 액세스 토큰을 발급하는 보안 토큰 서비스 역할을 하는 엔터티이다. 앞의 예제에서는 Facebook 자체가 권한 부여 서버 역할을 한다

## 권한부여 타입

OAuth 2.0의 권한 부여 유형은 클라이언트가 리소스 소유자를 대신하여 리소스에 액세스하기 위해 리소스 소유자로부터 권한 부여를 얻는 방법을 정의한다. 그랜트(Grant)라는 단어의 어원은 지원에 대한 동의를 의미하는 프랑스어 부여자(Granter)에서 유래한다. 즉, 권한 부여 유형은 잘 정의된 목적을 위해 자원 소유자를 대신하여 자원에 액세스하기 위해 자원 소유자의 동의를 얻는 잘 정의된 프로세스를 정의한다.

OAuth 2.0에서는 이 잘 정의된 목적을 **범위**라고도 한다. 또한 범위를 권한으로 해석할 수 있다. 즉, 범위는 클라이언트 앱이 지정된 리소스에서 수행할 수 있는 작업을 정의한다. 그림 4-1에서 `Facebook` 인증 서버에서 발급된 토큰은 범위에 바인딩되어 있으며 클라이언트 앱은 해당 사용자의 `Facebook` 담벼락에 메시지를 게시하는 데에만 토큰을 사용할 수 있다.

OAuth 2.0의 권한 부여 유형은 WRAP의 OAuth 프로필과 매우 유사하다(부록 B 참조). OAuth 2.0 핵심 사양은 인증 코드 부여 유형, 암시적 부여 유형, 리소스 소유자 암호 자격 증명 부여 유형 및 클라이언트 자격 증명 부여 유형의 4 가지 핵심 부여 유형을 도입한다. 표 4-1은 OAuth 2.0 부여 유형이 WRAP 프로필과 어떻게 일치하는지 보여준다.

Table 4-1 OAuth 2.0 부여 타입 vs. OAuth WRAP 프로파일 

- OAuth 2.0 | OAuth WRAP
- Authorization code |  Web App Profile/Rich App
- 묵시적 | -
- 리소스 소유자 패스워드 자격증명 | Username and Password
- 클라이언트 자격증명 | Client Account and Password

### Authorization Code 부여 타입

OAuth 2.0의 **권한 부여 코드** 부여 유형은 WRAP의 앱 프로필과 매우 유사하다. 웹 브라우저를 실행할 수 있는 기능이 있는 앱에 주로 권장된다(그림 4-2 참조). 클라이언트 앱을 방문하는 리소스 소유자는 **인증 코드** 부여 유형을 시작한다. 그림 4-2의 1단계와 같이 권한 서버에 등록된 앱이어야 하는 클라이언트 앱은 승인을 받기 위해 리소스 소유자를 권한 서버로 리디렉션한다. 다음은 사용자를 인증 서버의 인증 끝점으로 리디렉션하는 동안 클라이언트 앱이 생성하는 HTTP 요청을 보여준다.

```
https://authz.example.com/oauth2/authorize?
    response_type=code&
    client_id=0rhQErXIX49svVYoXJGt0DWBuFca&
    redirect_uri=https%3A%2F%2Fmycallback
```

권한 부여 끝점은 OAuth 2.0 권한 부여 서버의 잘 알려진 게시 끝점입니다. `response_type` 매개변수의 값은 `code`여야 한다.

이것은 요청이 인증 코드에 대한 것임을 인증 서버에 표시한다(인증 코드 부여 유형 아래). `client_id` 는 클라이언트 앱의 식별자이다. 클라이언트 앱이 인증 서버에 등록되면 클라이언트는 `client_id`와 `client_secret`을 얻습니다. 클라이언트 등록 단계에서 클라이언트 앱은 자신이 제어하는 ​​URL을 `redirect_uri`로 제공해야 하며 초기 요청에서 `redirect_uri`의 값이 인증 서버에 등록된 것과 일치해야 한다. 또한 `redirect_uri` 콜백 URL을 호출한다. URL로 인코딩된 콜백 URL 값은 `redirect_uri` 로 요청에 추가된다. 이러한 매개변수 외에도 클라이언트 앱에는 `scope` 도 포함될 수 있다. `scope`의 값은 승인 화면에서 리소스 소유자에게 표시된다. 이는 대상 리소스 API에 대해 클라이언트가 필요로 하는 액세스 수준을 인증 서버에 나타낸다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_4_Fig2_HTML.jpg)

Figure 4-2 Authorization code 부여 타입

그림 4-2의 5단계에서 인증 서버는 클라이언트 앱의 등록된 콜백 URL('redirect_uri'라고도 함)에 요청한 코드를 반환한다. 이 코드를 인증 코드라고 한다. 각 인증 코드에는 수명이 있어야 하며 1분보다 긴 수명은 권장되지 않는다.

https://callback.example.com/?code=9142d4cad58c66d0a5edfad8952192

인증 코드의 값은 HTTP 리디렉션을 통해 클라이언트 앱에 전달되고 리소스 소유자에게 표시된다. 다음 단계(6단계)에서 클라이언트는 인증 서버에 의해 노출된 OAuth 토큰 끝점과 통신하여 인증 코드를 OAuth 액세스 토큰으로 교환해야 한다

> **Note**
>
> 모든 OAuth 2.0 부여 유형의 궁극적인 목표는 클라이언트 앱에 액세스 토큰을 제공하는 것이다. 클라이언트 앱은 이 토큰을 사용하여 리소스에 액세스할 수 있다. 액세스 토큰은 리소스 소유자, 클라이언트 앱 및 하나 이상의 범위에 바인딩된다. 액세스 토큰이 주어지면 권한 부여 서버는 해당 리소스 소유자 및 클라이언트 응용 프로그램과 연결된 범위가 누구인지 알고 있다.

대부분의 경우 토큰 끝점은 보안 끝점이다. 클라이언트 앱은 HTTP Authorization 헤더에 들어갈 해당 `client_id(0rhQErXIX49svVYoXJGt0DWBuFca)` 및 `client_secret(eYOFkL756W8usQaVNgCNkz9C2D0a)`와 함께 토큰 요청을 생성할 수 있다.

대부분의 경우 토큰 끝점은 HTTP 기본 인증으로 보호되지만 필수는 아니다. 더 강력한 보안을 위해 상호 TLS를 사용할 수도 있으며, 단일 페이지 앱 또는 모바일 앱에서 인증 코드 부여 유형을 사용하는 경우 자격 증명을 전혀 사용하지 않을 수 있다. 다음은 토큰 엔드포인트에 대한 샘플 요청(6단계)을 보여준다. 거기에 있는 `grant_type` 매개변수의 값에는 `authorization_code`가 있어야 하고, 코드 값은 이전 단계(5단계)에서 반환된 값이어야 한다. 클라이언트 앱이 이전 요청(1단계)에서 `redirect_uri` 매개변수의 값을 보낸 경우 토큰 요청에도 동일한 값을 포함해야 한다. 클라이언트 앱이 토큰 끝점에 대해 인증하지 않는 경우 해당 client_id를 HTTP 본문의 매개 변수로 보내야 한다.

> **Note**
>
> 인증 서버에서 반환된 인증 코드는 중간 코드 역할을 한다. 이 코드는 최종 사용자 또는 리소스 소유자를 OAuth 클라이언트에 매핑하는 데 사용된다. OAuth 클라이언트는 인증 서버의 토큰 끝점에 대해 자신을 인증할 수 있다. 인증 서버는 코드를 액세스 토큰으로 교환하기 전에 인증된 OAuth 클라이언트에 코드가 발급되었는지 확인해야 한다.

```
\> curl -v –k -X POST --basic -u 0rhQErXIX49svVYoXJGt0DWBuFca:eYOFkL756W8usQaVNgCNkz9C2D0a
     -H "Content-Type:application/x-www-form-urlencoded;charset=UTF-8"
     -d "grant_type=authorization_code&
         code=9142d4cad58c66d0a5edfad8952192&
         redirect_uri=https://mycallback"
         https://authz.example.com/oauth2/token
```
> **Note**
>
> 인증 코드는 클라이언트가 한 번만 사용해야 한다. 인증 서버가 두 번 이상 사용되었음을 감지하면 해당 인증 코드에 대해 발급된 모든 토큰을 취소해야 한다.

앞의 `cURL` 명령은 권한 부여 서버에서 다음 응답을 반환한다(7단계). 응답의 `token_type` 매개변수는 토큰의 유형을 나타낸다. ("OAuth 2.0 토큰 유형" 섹션에서 토큰 유형에 대해 자세히 설명) 액세스 토큰 외에도 권한 부여 서버는 선택 사항인 리프레시 토큰도 반환합니다. 리프레시 토큰은 토큰이 만료되기 전에 클라이언트 앱에서 새 액세스 토큰을 얻는 데 사용할 수 있다. `expires_in` 매개변수는 액세스 토큰의 수명을 초 단위로 나타낸다.
```
{
    "token_type":"bearer",
    "expires_in":3600,
    "refresh_token":"22b157546b26c2d6c0165c4ef6b3f736",
    "access_token":"cac93e1d29e45bf6d84073dbfb460"
}
```
> **Note**
>

Each refresh token has its own lifetime. Compared to the lifetime of the access token, the refresh token’s is longer: the lifetime of an access token is in minutes, whereas the lifetime of a refresh token is in days.

### Implicit Grant Type

The implicit grant type to acquire an access token is mostly used by JavaScript clients running in the web browser (see Figure 4-3). Even for JavaScript clients now, we do not recommend using implicit grant type, rather use authorization code grant type with no client authentication. This is mostly due to the inherent security issues in the implicit grant type, which we discuss in Chapter 14. The following discussion on implicit grant type will help you understand how it works, but never use it in a production deployment.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_4_Fig3_HTML.jpg)

Figure 4-3 Implicit grant type

Unlike the authorization code grant type, the implicit grant type doesn’t have any equivalent profiles in OAuth WRAP. The JavaScript client initiates the implicit grant flow by redirecting the user to the authorization server. The response_type parameter in the request indicates to the authorization server that the client expects a token, not a code. The implicit grant type doesn’t require the authorization server to authenticate the JavaScript client; it only has to send the `client_id` in the request. This is for logging and auditing purposes and also to find out the corresponding `redirect_uri`. The `redirect_uri` in the request is optional; if it’s present, it must match what is provided at the client registration:
```
https://authz.example.com/oauth2/authorize?
    response_type=token&
    client_id=0rhQErXIX49svVYoXJGt0DWBuFca&
    redirect_uri=https%3A%2F%2Fmycallback
```
This returns the following response. The implicit grant type sends the access token as a URI fragment and doesn’t provide any refreshing mechanism:

https://callback.example.com/#access_token=cac93e1d29e45bf6d84073dbfb460&expires_in=3600

권한 부여 코드 유형과 달리 암시적 부여 유형 클라이언트는 부여 요청에 대한 응답으로 액세스 토큰을 받는다. URL의 URI 조각에 무언가가 있을 때 브라우저는 결코 그것을 백엔드로 보내지 않고 브라우저에만 남아 있다. 따라서 권한 부여 서버가 클라이언트 앱의 콜백 URL로 리디렉션을 보낼 때 요청은 먼저 브라우저에 도달하고 브라우저는 클라이언트 앱을 호스팅하는 웹 서버에 HTTP 'GET'을 수행한다. 그러나 해당 HTTP 'GET'에서는 URI 조각을 찾을 수 없으며 웹 서버에서도 이를 볼 수 없다. URI 조각에 있는 액세스 토큰을 처리하기 위해 브라우저에서 HTTP `GET`에 대한 응답으로 클라이언트 앱의 웹 서버는 JavaScript가 포함된 HTML 페이지를 반환한다. 브라우저 주소 표시줄에 여전히 남아 있는 URI는 단일 페이지 앱이 작동하는 방식이다.

> **Note**
>
> The authorization server must treat the authorization code, access token, refresh token, and client secret key as sensitive data. They should never be sent over HTTP—the authorization server must use Transport Layer Security (TLS). These tokens should be stored securely, possibly by encrypting or hashing them.

### Resource Owner Password Credentials Grant Type

Under the **resource owner password credentials** grant type, the resource owner must trust the client application. This is equivalent to the Username and Password Profile in OAuth WRAP. The resource owner has to give his/her credentials directly to the client application (see Figure 4-4).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_4_Fig4_HTML.jpg)

Figure 4-4 Resource owner password credentials grant type

The following `cURL` command talks to the token endpoint of the authorization server, passing the resource owner’s username and password as parameters. In addition, the client application proves its identity. In most of the cases, the token endpoint is secured with HTTP Basic authentication (but not a must), and the client application passes its `client_id (0rhQErXIX49svVYoXJGt0DWBuFca)` and `client_secret (eYOFkL756W8usQaVNgCNkz9C2D0a`) in the HTTP Authorization header. The value of the grant_type parameter must be set to password:
```
\> curl -v -k -X POST --basic
     -u 0rhQErXIX49svVYoXJGt0DWBuFca:eYOFkL756W8usQaVNgCNkz9C2D0a
     -H "Content-Type:application/x-www-form-urlencoded;charset=UTF-8"
     -d "grant_type=password&
         username=admin&password=admin"
         https://authz.example.com/oauth2/token
```
This returns the following response, which includes an access token along with a refresh token:
```
{
    "token_type":"bearer",
    "expires_in":685,"
    "refresh_token":"22b157546b26c2d6c0165c4ef6b3f736",
    "access_token":"cac93e1d29e45bf6d84073dbfb460"
}
```
> **Note**
>
> If using the authorization code grant type is an option, it should be used over the resource owner password credentials grant type. The resource owner password credentials grant type was introduced to aid migration from HTTP Basic authentication and Digest authentication to OAuth 2.0.

### Client Credentials Grant Type

The client credentials grant type is equivalent to the Client Account and Password Profile in OAuth WRAP and to two-legged OAuth in OAuth 1.0 (see Appendix B). With this grant type, the client itself becomes the resource owner (see Figure 4-5). The following cURL command talks to the token endpoint of the authorization server, passing the client application’s client_id (0rhQErXIX49svVYoXJGt0DWBuFca) and client_secret (eYOFkL756W8usQaVNgCNkz9C2D0a).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_4_Fig5_HTML.jpg)

Figure 4-5 Client credentials grant type

```
\> curl –v –k -X POST --basic
     -u 0rhQErXIX49svVYoXJGt0DWBuFca:eYOFkL756W8usQaVNgCNkz9C2D0a
     -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8"
     -d "grant_type=client_credentials"
     https://authz.example.com/oauth2/token
```
This returns the following response, which includes an access token. Unlike the resource owner password credentials grant type, the client credentials grant type doesn’t return a refresh token:
```
{     "token_type":"bearer",
      "expires_in":3600,
      "access_token":"4c9a9ae7463ff9bb93ae7f169bd6a"

}
```
This client credential grant type is mostly used for system-to-system interactions with no end user. For example, a web application needs to access an OAuth secured API to get some metadata.

### Refresh Grant Type

Although it’s not the case with the implicit grant type and the client credentials grant type, with the other two grant types, the OAuth access token comes with a refresh token. This refresh token can be used to extend the validity of the access token without the involvement of the resource owner. The following cURL command shows how to get a new access token from the refresh token:
```
\> curl -v -X POST --basic
    -u 0rhQErXIX49svVYoXJGt0DWBuFca:eYOFkL756W8usQaVNgCNkz9C2D0a
    -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8"
    -k -d "grant_type=refresh_token&
           refresh_token=22b157546b26c2d6c0165c4ef6b3f736"
    https://authz.example.com/oauth2/token
```
This returns the following response:
```
{
    "token_type":"bearer",
    "expires_in":3600,
    "refresh_token":"9ecc381836fa5e3baf5a9e86081",
    "access_token":"b574d1ba554c26148f5fca3cceb05e2"
}
```

> **Note**
>
> The refresh token has a much longer lifetime than the access token. If the lifetime of the refresh token expires, then the client must initiate the OAuth token flow from the start and get a new access token and refresh token. The authorization server also has the option to return a new refresh token each time the client refreshes the access token. In such cases, the client has to discard the previously obtained refresh token and begin using the new one.

### How to Pick the Right Grant Type?

The nature of a framework is to provide multiple options, and it’s up to the application developers to pick the best out of those options, based on their use cases. OAuth can be used with any kind of application. It can be a web application, single-page application, desktop application, or a native mobile application.

To pick the right grant type for those applications, first we need to think how the client application is going to invoke the OAuth secured API: whether it is going to access the API by itself or on behalf of an end user. If the application wants to access the API just being itself, then we should use client credentials grant type and, if not, should use authorization code grant type. 

> Note: Both the implicit and password grant types are now obsolete.


## OAuth 2.0 Token Types

Neither OAuth 1.0 nor WRAP could support custom token types. OAuth 1.0 always used signature-based tokens, and OAuth WRAP always used bearer tokens over TLS. OAuth 2.0 isn’t coupled into any token type. In OAuth 2.0, you can introduce your own token type if needed. Regardless of the token_type returned in the OAuth token response from the authorization server, the client must understand it before using it. Based on the token_type, the authorization server can add additional attributes/parameters to the response.

OAuth 2.0 has two main token profiles: OAuth 2.0 Bearer Token Profile and OAuth 2.0 MAC Token Profile. The most popular OAuth token profile is Bearer; almost all OAuth 2.0 deployments today are based on the OAuth 2.0 Bearer Token Profile. The next section talks about the Bearer Token Profile in detail, and Appendix G discusses the MAC Token Profile.

### OAuth 2.0 Bearer Token Profile

The OAuth 2.0 Bearer Token Profile was influenced by OAuth WRAP, which only supported bearer tokens. As its name implies, anyone who bears the token can use it—don’t lose it! Bearer tokens must always be used over Transport Layer Security (TLS) to avoid losing them in transit. Once the bearer access token is obtained from the authorization server, the client can use it in three ways to talk to the resource server. These three ways are defined in the RFC 6750. The most popular way is to include the access token in the HTTP Authorization header:

> **Note**
>
> An OAuth 2.0 bearer token can be a reference token or self-contained token. A reference token is an arbitrary string. An attacker can carry out a brute-force attack to guess the token. The authorization server must pick the right length and use other possible measures to prevent brute forcing. A self-contained access token is a JSON Web Token (JWT), which we discuss in Chapter 7. When the resource server gets an access token, which is a reference token, then to validate the token, it has to talk to the authorization server (or the token issuer). When the access token is a JWT, the resource server can validate the token by itself, by verifying the signature of the JWT.
```
GET /resource HTTP/1.1
Host: rs.example.com
Authorization: Bearer JGjhgyuyibGGjgjkjdlsjkjdsd
```
The access token can also be included as a query parameter. This approach is mostly used by the client applications developed in JavaScript:
```
GET /resource?access_token=JGjhgyuyibGGjgjkjdlsjkjdsd
Host: rs.example.com
```
> **Note**
>
> When the value of the OAuth access token is sent as a query parameter, the name of the parameter must be access_token. Both Facebook and Google use the correct parameter name, but LinkedIn uses oauth2_access_token and Salesforce uses oauth_token.

It’s also possible to send the access token as a form-encoded body parameter. An authorization server supporting the Bearer Token Profile should be able to handle any of these patterns:
```
POST /resource HTTP/1.1
Host: server.example.com
Content-Type: application/x-www-form-urlencoded
access_token=JGjhgyuyibGGjgjkjdlsjkjdsd
```
> **Note**
>
>The value of the OAuth bearer token is only meaningful to the authorization server. The client application should not try to interpret what it says. To make the processing logic efficient, the authorization server may include some meaningful but nonconfidential data in the access token. For example, if the authorization server supports multiple domains with multitenancy, it may include the tenant domain in the access token and then base64-encode (see Appendix E) it or simply use a JSON Web Token (JWT).

### OAuth 2.0 Client Types

OAuth 2.0은 기밀 클라이언트와 공개 클라이언트의 두 가지 유형의 클라이언트를 식별합니다. 기밀 클라이언트는 자신의 자격 증명(클라이언트 키 및 클라이언트 암호)을 보호할 수 있지만 공개 클라이언트는 보호할 수 없습니다. OAuth 2.0 사양은 웹 애플리케이션, 사용자 에이전트 기반 애플리케이션 및 기본 애플리케이션의 세 가지 유형의 클라이언트 프로필을 중심으로 구축됩니다. 웹 응용 프로그램은 웹 서버에서 실행되는 기밀 클라이언트로 간주됩니다. 최종 사용자 또는 리소스 소유자는 웹 브라우저를 통해 이러한 응용 프로그램에 액세스합니다. 사용자 에이전트 기반 응용 프로그램은 공개 클라이언트로 간주된다. 

웹 서버에서 코드를 다운로드하여 브라우저에서 실행되는 JavaScript와 같은 사용자 에이전트에서 실행합니다. 이러한 클라이언트는 자격 증명을 보호할 수 없습니다. 최종 사용자는 JavaScript에서 무엇이든 볼 수 있습니다. 기본 응용 프로그램도 공용 클라이언트로 간주됩니다. 이러한 클라이언트는 최종 사용자의 제어 하에 있으며 해당 응용 프로그램에 저장된 모든 기밀 데이터를 추출할 수 있습니다. Android 및 iOS 기본 애플리케이션이 몇 가지 예이다.

> **Note**
>
> All four grant types defined in the OAuth 2.0 core specification require the client to preregister with the authorization server, and in return it gets a client identifier. Under the implicit grant type, the client doesn’t get a client secret. At the same time, even under other grant types, it’s an option whether to use the client secret or not.

Table 4-2 OAuth 2.0 Bearer Token Profile

- An authorization framework for access delegation
- Nonsignature-based, Bearer Token Profile
- Highly extensible via grant types and token types
- More developer-friendly
- Bearer Token Profile mandates using TLS during the entire flow
- Secret key goes on the wire (Bearer Token Profile)

> **Note**
>
OAuth 2.0 introduces a clear separation between the client, the resource owner, the authorization server, and the resource server. But the core OAuth 2.0 specification doesn’t talk about how the resource server validates an access token. Most OAuth implementations started doing this by talking to a proprietary API exposed by the authorization server. The OAuth 2.0 Token Introspection profile standardized this to some extent, and in Chapter 9, we talk more about it.

### JWT Secured Authorization Request (JAR)

권한 부여 서버의 권한 부여 엔드포인트에 대한 OAuth 2.0 요청에서 모든 요청 매개변수는 브라우저를 통해 쿼리 매개변수로 흐른다. 다음은 OAuth 2.0 인증 코드 부여 요청의 예이다.

```
https://authz.example.com/oauth2/authorize?
    response_type=token&
    client_id=0rhQErXIX49svVYoXJGt0DWBuFca&
    redirect_uri=https%3A%2F%2Fmycallback
```
There are a couple of issues with this approach. Since these parameters flow via the browser, the end user or anyone on the browser can change the input parameters that could result in some unexpected outcomes at the authorization server. At the same time, since the request is not integrity protected, the authorization server has no means to validate who initiated the request. With JWT secured authorization requests, we can overcome these two issues. 

If you are new to JWT, please check Chapters 7 and 8. JWT defines a container to transport data between interested parties in a cryptographically safe manner. JWS specification developed under the IETF JOSE working group, represents a message or a payload, which is digitally signed or MACed (when a hashing algorithm is used with HMAC), while the JWE specification standardizes a way to represent an encrypted payload.

One of the draft proposals1 to the IETF OAuth working group suggests to introduce the ability to send request parameters in a JWT, which allows the request to be signed with JWS and encrypted with JWE so that the integrity, source authentication, and confidentiality properties of the authorization request are preserved. At the time of writing, this proposal is in its very early stage—and if you are familiar with Security Assertion Markup Language (SAML) Single Sign-On, this is quite analogous to the signed authentication requests in SAML. The following shows the decoded payload of a sample authorization request, which ideally goes within a JWT:
```
{
  "iss": "s6BhdRkqt3",
  "aud": "https://server.example.com",
  "response_type": "code id_token",
  "client_id": "s6BhdRkqt3",
  "redirect_uri": "https://client.example.org/cb",
  "scope": "openid",
  "state": "af0ifjsldkj",
  "nonce": "n-0S6_WzA2Mj",
  "max_age": 86400
}
```
Once the client application constructs the JWT (a JWS or a JWE—please see Chapters 7 and 8 for the details), it can send the authorization request to the OAuth authorization server in two ways. One way is called passing by value, and the other is passing by reference. The following shows an example of passing by value, where the client application sends the JWT in a query parameter called request. The `[jwt_assertion]` in the following request represents either the actual JWS or JWE.
```
https://server.example.com/authorize?request=[jwt_assertion]
```
The draft proposal for JWT authorization request introduces the pass by reference method to overcome some of the limitations in the pass by value method, as listed here:

- Many mobile phones in the market as of this writing still do not accept large payloads. The payload restriction is typically either 512 or 1024 ASCII characters.

- The maximum URL length supported by older versions of the Internet Explorer is 2083 ASCII characters.

- On a slow connection such as a 2G mobile connection, a large URL would cause a slow response. Therefore the use of such is not advisable from the user experience point of view.

The following shows an example of pass by reference, where the client application sends a link in the request, which can be used by the authorization server to fetch the JWT. This is a typical OAuth 2.0 authorization code request, along with the new request_uri query parameter. The value of the request_uri parameter carries a link pointing to the corresponding JWS or JWE.
```
https://server.example.com/authorize?
        response_type=code&
        client_id=s6BhdRkqt3&
        request_uri=https://tfp.example.org/request.jwt/Schjwew&
        state=af0ifjsldkj
```

## Pushed Authorization Requests (PAR)

이것은 현재 IETF OAuth 작업 그룹에서 논의 중인 또 다른 제안 초안으로, 이전 섹션에서 논의한 JAR(JWT Secured Authorization Request) 접근 방식을 보완합니다. JAR의 한 가지 문제는 각 클라이언트가 엔드포인트를 인증 서버에 직접 노출해야 한다는 것입니다. 이는 권한 부여 서버에서 사용하는 해당 JWT를 호스팅하는 엔드포인트이다. PAR(Pushed Authorization Requests) 초안 제안을 사용하면 이 요구 사항이 해결된다. PAR은 권한 부여 서버 측에서 엔드포인트를 정의합니다. 

여기서 각 클라이언트는 일반적인 OAuth 2.0 권한 부여 요청의 모든 매개변수를 직접 푸시(브라우저를 통하지 않고)한 다음 브라우저를 통해 일반 권한 부여 흐름을 사용하여 푸시된 항목에 대한 참조를 전달할 수 있습니다. 요구. 다음은 클라이언트 앱이 권한 부여 서버에서 호스팅되는 엔드포인트에 권한 부여 요청 매개변수를 푸시하는 예입니다. 권한 부여 서버의 이 푸시 끝점은 상호 TLS(전송 계층 보안) 또는 OAuth 2.0 자체(클라이언트 자격 증명) 또는 클라이언트 앱과 권한 부여 서버 간에 합의된 다른 수단으로 보호될 수 있다.

```
POST /as/par HTTP/1.1
Host: server.example.com
Content-Type: application/x-www-form-urlencoded
Authorization: Basic czZCaGRSa3F0Mzo3RmpmcDBaQnIxS3REUmJuZlZkbUl3
response_type=code&
state=af0ifjsldkj&
client_id=s6BhdRkqt3&
redirect_uri=https%3A%2F%2Fclient.example.org%2Fcb&
scope=ais
```
If the client follows the JAR specification which, we discussed in the previous section, it can also send a JWS or a JWE to the push endpoint in the following way.
```
POST /as/par HTTP/1.1
Host: server.example.com
Content-Type: application/x-www-form-urlencoded
Authorization: Basic czZCaGRSa3F0Mzo3RmpmcDBaQnIxS3REUmJuZlZkbUl3
request=[jwt_assertion]
```
Once the push endpoint at the authorization server receives the preceding request, it has to carry out all the validation checks against the request that it usually performs against a typical authorization request. If it all looks good, the authorization server responds with the following. The value of the request_uri parameter in the response is bound to the client_id in the request and acts as a reference to the authorization request.
```
HTTP/1.1 201 Created
Date: Tue, 2 Oct 2019 15:22:31 GMT
Content-Type: application/json
{
  "request_uri": "urn:example:bwc4JK-ESC0w8acc191e-Y1LTC2",
  "expires_in": 3600
}
```
Upon receiving the push response from the authorization server, the client application can construct the following request with the request_uri parameter from the response to redirect the user to the authorization server.
```
https://server.example.com/authorize?
        request_uri=urn:example:bwc4JK-ESC0w8acc191e-Y1LTC2
```

## Summary

- OAuth 2.0은 API 보안을 위한 사실상의 표준으로 접근 위임 문제를 주로 해결한다.

- OAuth 2.0의 부여 유형은 클라이언트가 리소스 소유자를 대신하여 리소스에 액세스하기 위해 리소스 소유자로부터 권한 부여를 얻을 수 있는 방법을 정의한다.

- OAuth 2.0 핵심 사양은 권한 부여 코드, 암시적, 암호, 클라이언트 자격 증명 및 리프레시의 5가지 유형을 정의한다.

- 리프레시 부여 유형은 OAuth 2.0 클라이언트 앱에서 만료되거나 만료에 가까운 액세스 토큰을 갱신하는 데 사용되는 특수 부여 유형이다.

- 암시적 부여 유형 및 클라이언트 자격 증명 부여 유형은 라프레시 토큰을 반환하지 않는다.

- 암시적 부여 유형은 더 이상 사용되지 않으며 고유한 보안 문제로 인해 사용하지 않는 것이 좋다.

- OAuth 2.0은 공개 클라이언트와 기밀 클라이언트의 두 가지 유형의 클라이언트 앱을 지원한다. 단일 페이지 앱과 기본 모바일 앱은 공개 클라이언트에 속하는 반면 웹 앱은 기밀 클라이언트에 속한다.

- OAuth 2.0 인증 프레임워크: JWT 보안 인증 요청(JAR) 제안은 JWT에서 요청 매개변수를 보내는 기능을 도입할 것을 제안한다.

- PAR(Pushed Authorization Requests) 제안은 인증 서버 끝에 푸시 엔드포인트를 도입하여 클라이언트 앱이 모든 인증 요청 매개변수를 안전하게 푸시한 다음 브라우저 기반 로그인 흐름을 시작할 수 있도록 제안한다.