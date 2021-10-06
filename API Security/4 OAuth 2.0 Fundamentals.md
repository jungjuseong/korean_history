
# 4. OAuth 2.0 Fundamentals

OAuth 2.0은 ID 위임의 주요 혁신입니다. OAuth 1.0(부록 B 참조)에 뿌리를 두고 있지만 OAuth 웹 리소스 인증 프로필(부록 B 참조)이 주로 영향을 미쳤습니다. OAuth 1.0과 2.0의 주요 차이점은 OAuth 1.0이 ID 위임을 위한 표준 프로토콜인 반면 OAuth 2.0은 확장성이 뛰어난 인증 프레임워크라는 점입니다. OAuth 2.0은 이미 API 보안을 위한 사실상의 표준이며 Facebook, Google, LinkedIn, Microsoft, PayPal, Instagram, Foursquare, GitHub, Yammer, Meetup 등에서 널리 사용됩니다. 한 가지 일반적인 예외가 있습니다. Twitter는 여전히 OAuth 1.0을 사용합니다.

## Understanding OAuth 2.0

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

OAuth 2.0의 권한 부여 유형은 클라이언트가 리소스 소유자를 대신하여 리소스에 액세스하기 위해 리소스 소유자로부터 권한 부여를 얻는 방법을 정의한다. `Grant`라는 단어의 어원은 지원에 대한 동의를 의미하는 `Granter`에서 유래한다. 즉, 권한 부여 유형은 잘 정의된 목적을 위해 리소스 소유자를 대신하여 리소스에 접근하기 위해 리소스 소유자의 동의를 얻는 잘 정의된 프로세스를 정의한다.

OAuth 2.0에서는 이 잘 정의된 목적을 **범위**라고도 한다. 또한 범위를 권한으로 해석할 수 있다. 즉, 범위는 클라이언트 앱이 지정된 리소스에서 수행할 수 있는 작업을 정의한다. 그림 4-1에서 `Facebook` 인증 서버에서 발급된 토큰은 범위에 바인딩되어 있으며 클라이언트 앱은 해당 사용자의 `Facebook` 담벼락에 메시지를 게시하는 데에만 토큰을 사용할 수 있다.

OAuth 2.0의 권한 부여 유형은 WRAP의 OAuth 프로필과 매우 유사하다(부록 B 참조). OAuth 2.0 핵심 사양은 `인증 코드` 부여 유형, `암시적` 부여 유형, `리소스 소유자 암호 자격 증명` 부여 유형 및 `클라이언트 자격 증명` 부여 유형의 4 가지 핵심 부여 유형을 도입한다. 표 4-1은 OAuth 2.0 부여 유형이 WRAP 프로필과 어떻게 일치하는지 보여준다.

Table 4-1 OAuth 2.0 부여 타입 vs. OAuth WRAP 프로파일 

- OAuth 2.0 | OAuth WRAP
- Authorization code 부여 방식| Web App Profile/Rich App
- 암시적 부여 방식 | -
- 리소스 소유자 패스워드 자격증명 부여 방식| Username and Password
- 클라이언트 자격증명 부여 방식| Client Account and Password

### Authorization Code 부여 타입

OAuth 2.0의 **인증 코드** 부여 유형은 WRAP의 앱 프로필과 매우 유사하다. 웹 브라우저를 실행할 수 있는 기능이 있는 앱에 주로 권장된다(그림 4-2 참조). 클라이언트 앱을 방문하는 리소스 소유자는 **인증 코드** 부여 유형을 시작한다. 그림 4-2의 1단계와 같이 권한 서버에 등록된 앱이어야 하는 클라이언트 앱은 승인을 받기 위해 리소스 소유자를 권한 서버로 리디렉션한다. 

다음은 사용자를 인증 서버의 인증 끝점으로 리디렉션하는 동안 클라이언트 앱이 생성하는 HTTP 요청을 보여준다.

```
https://authz.example.com/oauth2/authorize?
    response_type=code&
    client_id=0rhQErXIX49svVYoXJGt0DWBuFca&
    redirect_uri=https%3A%2F%2Fmycallback
```

인증코드 부여 끝점은 OAuth 2.0 권한 부여 서버의 잘 알려진 게시 끝점입니다. `response_type` 매개변수의 값은 `code`여야 한다.

이것은 요청이 인증 코드에 대한 것임을 인증 서버에 표시한다(인증 코드 부여 유형 아래). `client_id` 는 클라이언트 앱의 식별자이다. 클라이언트 앱이 인증 서버에 등록되면 클라이언트는 `client_id`와 `client_secret`을 얻습니다. 

클라이언트 등록 단계에서 클라이언트 앱은 자신이 제어하는 ​​URL을 `redirect_uri`로 제공해야 하며 초기 요청에서 `redirect_uri`의 값이 인증 서버에 등록된 것과 일치해야 한다. 또한 `redirect_uri` 콜백 URL을 호출한다. URL로 인코딩된 콜백 URL 값은 `redirect_uri` 로 요청에 추가된다. 

이러한 매개변수 외에도 클라이언트 앱에는 `scope` 도 포함될 수 있다. `scope`의 값은 승인 화면에서 리소스 소유자에게 표시된다. 이는 대상 리소스 API에 대해 클라이언트가 필요로 하는 액세스 수준을 인증 서버에 나타낸다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_4_Fig2_HTML.jpg)

Figure 4-2 Authorization code 부여 타입

그림 4-2의 5단계에서 인증 서버는 클라이언트 앱의 등록된 콜백 URL(`redirect_uri`)에 요청한 코드를 반환한다. 이 코드를 인증 코드라고 한다. 각 인증 코드에는 수명이 있어야 하며 1분보다 긴 수명은 권장되지 않는다.

https://callback.example.com/?code=9142d4cad58c66d0a5edfad8952192

인증 코드의 값은 HTTP 리디렉션을 통해 클라이언트 앱에 전달되고 리소스 소유자에게 표시된다. 다음 단계(6단계)에서 클라이언트는 인증 서버에 의해 노출된 OAuth 토큰 끝점과 통신하여 인증 코드를 OAuth 액세스 토큰으로 교환해야 한다

> **Note**
>
> 모든 OAuth 2.0 부여 유형의 궁극적인 목표는 클라이언트 앱에 액세스 토큰을 제공하는 것이다. 클라이언트 앱은 이 토큰을 사용하여 리소스에 액세스할 수 있다. 액세스 토큰은 리소스 소유자, 클라이언트 앱 및 하나 이상의 범위에 바인딩된다. 액세스 토큰이 주어지면 권한 부여 서버는 해당 리소스 소유자 및 클라이언트 응용 프로그램과 연결된 범위가 누구인지 알고 있다.

대부분의 경우 토큰 끝점은 보안 끝점이다. 클라이언트 앱은 `HTTP Authorization` 헤더에 들어갈 해당 `client_id(0rhQErXIX49svVYoXJGt0DWBuFca)` 및 `client_secret(eYOFkL756W8usQaVNgCNkz9C2D0a)`와 함께 토큰 요청을 생성할 수 있다.

대부분의 경우 토큰 끝점은 HTTP 기본 인증으로 보호되지만 필수는 아니다. 더 강력한 보안을 위해 상호 TLS를 사용할 수도 있으며, 단일 페이지 앱 또는 모바일 앱에서 인증 코드 부여 유형을 사용하는 경우 자격 증명을 전혀 사용하지 않을 수 있다. 다음은 토큰 엔드포인트에 대한 샘플 요청(6단계)을 보여준다. 거기에 있는 `grant_type`의 값에는 `authorization_code`가 있어야 하고, 코드 값은 이전 단계(5단계)에서 반환된 값이어야 한다. 클라이언트 앱이 이전 요청(1단계)에서 `redirect_uri` 매개변수의 값을 보낸 경우 토큰 요청에도 동일한 값을 포함해야 한다. 클라이언트 앱이 토큰 끝점에 대해 인증하지 않는 경우 해당 `client_id`를 HTTP 본문의 매개 변수로 보내야 한다.

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

앞의 `cURL` 명령은 권한 부여 서버에서 다음 응답을 반환한다(7단계). 응답의 `token_type`은 토큰의 유형을 나타낸다. ("OAuth 2.0 토큰 유형" 섹션에서 토큰 유형에 대해 자세히 설명) 액세스 토큰 외에도 권한 부여 서버는 선택 사항인 리프레시 토큰도 반환합니다. 리프레시 토큰은 토큰이 만료되기 전에 클라이언트 앱에서 새 액세스 토큰을 얻는 데 사용할 수 있다. `expires_in` 매개변수는 액세스 토큰의 수명을 초 단위로 나타낸다.

```
{
    "token_type":"bearer",
    "expires_in":3600,
    "refresh_token":"22b157546b26c2d6c0165c4ef6b3f736",
    "access_token":"cac93e1d29e45bf6d84073dbfb460"
}
```
> **참고**
>
> 각 새로 고침 토큰에는 고유한 수명이 있습니다. 액세스 토큰의 수명과 비교하여 새로 고침 토큰의 수명이 더 깁니다. 액세스 토큰의 수명은 분 단위인 반면 새로 고침 토큰의 수명은 일 단위입니다.

### 암시적 부여 유형

액세스 토큰을 얻기 위한 암시적 부여 유형은 대부분 웹 브라우저에서 실행되는 JavaScript 클라이언트에서 사용됩니다(그림 4-3 참조). 현재 JavaScript 클라이언트의 경우에도 암시적 부여 유형을 사용하지 않는 것이 좋습니다. 클라이언트 인증 없이 권한 부여 코드 부여 유형을 사용하십시오. 이는 주로 14장에서 논의하는 암시적 부여 유형의 고유한 보안 문제 때문입니다. 암시적 부여 유형에 대한 다음 논의는 작동 방식을 이해하는 데 도움이 되지만 프로덕션 배포에서는 사용하지 마십시오.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_4_Fig3_HTML.jpg)

그림 4-3 암시적 부여 유형

권한 부여 코드 부여 유형과 달리 암시적 부여 유형에는 OAuth WRAP에 해당하는 프로필이 없습니다. JavaScript 클라이언트는 사용자를 인증 서버로 리디렉션하여 암시적 승인 흐름을 시작합니다. 요청의 response_type 매개변수는 클라이언트가 코드가 아닌 토큰을 기대한다는 것을 인증 서버에 나타냅니다. 암시적 승인 유형에서는 권한 부여 서버가 JavaScript 클라이언트를 인증할 필요가 없습니다. 요청에 'client_id'만 보내면 됩니다. 이는 로깅 및 감사 목적과 해당 'redirect_uri'를 찾기 위한 것입니다. 요청의 'redirect_uri'는 선택 사항입니다. 존재하는 경우 클라이언트 등록 시 제공된 것과 일치해야 합니다.

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

### 리소스 소유자 암호 자격 증명 부여 유형

`리소스 소유자 암호 자격 증명` 부여 유형에서 리소스 소유자는 클라이언트 앱을 신뢰해야 합니다. 이것은 OAuth WRAP의 사용자 이름 및 비밀번호 프로필과 동일합니다. 리소스 소유자는 자신의 자격 증명을 클라이언트 앱에 직접 제공해야 합니다(그림 4-4 참조).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_4_Fig4_HTML.jpg)

그림 4-4 리소스 소유자 암호 자격 증명 부여 유형

다음 `cURL` 명령은 권한 부여 서버의 토큰 끝점과 통신하여 리소스 소유자의 사용자 이름과 비밀번호를 매개변수로 전달합니다. 또한 클라이언트 응용 프로그램은 해당 ID를 증명합니다. 대부분의 경우 토큰 끝점은 HTTP 기본 인증으로 보호되며 클라이언트 앱은 `HTTP Authorization` 헤더에서 `client_id` 및 `client_secret`를 전달합니다. `grant_type`의 값은 `password로` 설정되어야 합니다.

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

> **참고**
>
> 권한 부여 코드 사용 권한 부여 유형이 옵션인 경우 리소스 소유자 암호 자격 증명 부여 유형 위에 사용해야 합니다. 리소스 소유자 비밀번호 자격 증명 부여 유형은 HTTP 기본 인증 및 다이제스트 인증에서 OAuth 2.0으로의 마이그레이션을 지원하기 위해 도입되었습니다.

### 클라이언트 자격 증명 부여 유형

클라이언트 자격 증명 부여 유형은 OAuth WRAP의 클라이언트 계정 및 비밀번호 프로필 및 OAuth 1.0의 two-legged OAuth와 동일합니다. 이 유형을 사용하면 클라이언트 자체가 리소스 소유자가 됩니다(그림 4-5 참조). 다음 cURL 명령은 인증 서버의 토큰 엔드포인트와 통신하여 클라이언트 앱의 `client_id` 및 `client_secret`를 전달합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_4_Fig5_HTML.jpg)

Figure 4-5 Client credentials grant type

```
\> curl –v –k -X POST --basic
     -u 0rhQErXIX49svVYoXJGt0DWBuFca:eYOFkL756W8usQaVNgCNkz9C2D0a
     -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8"
     -d "grant_type=client_credentials"
     https://authz.example.com/oauth2/token
```

그러면 액세스 토큰이 포함된 응답이 반환됩니다. 리소스 소유자 암호 자격 증명 부여 유형과 달리 클라이언트 자격 증명 부여 유형은 리프레시 토큰을 반환하지 않습니다

```
{     "token_type":"bearer",
      "expires_in":3600,
      "access_token":"4c9a9ae7463ff9bb93ae7f169bd6a"
}
```
클라이언트 자격 증명 부여 유형은 최종 사용자가 없는 시스템 간 상호 작용에 주로 사용됩니다. 예를 들어, 웹 앱은 일부 메타데이터를 얻기 위해 OAuth 보안 API에 액세스해야 합니다.

### Refresh Grant Type

암시적 부여 유형 및 클라이언트 자격 증명 부여 유형의 경우는 아니지만 다른 두 부여 유형의 경우 OAuth `액세스 토큰`이 `리프레시 토큰`과 함께 제공됩니다. 이 리프레시 토큰은 리소스 소유자의 개입 없이 액세스 토큰의 유효성을 확장하는 데 사용할 수 있습니다. 다음 cURL 명령은 리프레시 토큰에서 새 액세스 토큰을 가져오는 방법을 보여줍니다.
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

> **참고**
>
> 리프레시 토큰은 액세스 토큰보다 수명이 훨씬 깁니다. 리프레시 토큰의 수명이 만료되면 클라이언트는 처음부터 OAuth 토큰 흐름을 시작하고 새 액세스 토큰과 리프레시 토큰을 가져와야 합니다. 인증 서버에는 클라이언트가 액세스 토큰을 새로 고칠 때마다 새 리프레시 토큰을 반환하는 옵션도 있습니다. 이러한 경우 클라이언트는 이전에 얻은 리프레시 토큰을 버리고 새 토큰을 사용하기 시작해야 합니다.

### 올바른 부여 유형을 선택하는 방법은 무엇입니까?

프레임워크의 특성은 여러 옵션을 제공하는 것이며 사용 사례에 따라 이러한 옵션 중에서 가장 좋은 것을 선택하는 것은 앱 개발자의 몫입니다. OAuth는 모든 종류의 앱에서 사용할 수 있습니다. 웹 앱, 단일 페이지 앱, 데스크톱 앱 또는 기본 모바일 앱이 될 수 있습니다.

이러한 애플리케이션에 적합한 권한 부여 유형을 선택하려면 먼저 클라이언트 앱이 OAuth 보안 API를 호출하는 방법을 생각해야 합니다. 앱이 그 자체로 API에 액세스하려는 경우 `클라이언트 자격 증명 부여 유형`을 사용해야 하고 그렇지 않은 경우 `인증 코드 부여 유형`을 사용해야 합니다.

> Note: Both the implicit and password grant types are now obsolete.

## OAuth 2.0 토큰 유형

OAuth 1.0도 WRAP도 사용자 정의 토큰 유형을 지원할 수 없습니다. OAuth 1.0은 항상 서명 기반 토큰을 사용했고 OAuth WRAP은 항상 TLS를 통해 전달자 토큰을 사용했습니다. OAuth 2.0은 어떤 토큰 유형에도 연결되지 않습니다. OAuth 2.0에서는 필요한 경우 고유한 토큰 유형을 도입할 수 있습니다. 인증 서버의 OAuth 토큰 응답에서 반환된 `token_type`에 관계없이 클라이언트는 사용하기 전에 이를 이해해야 합니다. `token_type`을 기반으로 인증 서버는 응답에 추가 속성/매개변수를 추가할 수 있습니다.

OAuth 2.0에는 `OAuth 2.0 Bearer 토큰 프로필`과 OAuth 2.0 MAC 토큰 프로필의 두 가지 주요 토큰 프로필이 있습니다. 가장 인기 있는 OAuth 토큰 프로필은 `Bearer`입니다. 오늘날 거의 모든 OAuth 2.0 배포는 `OAuth 2.0 Bearer 토큰` 프로필을 기반으로 합니다. 다음 섹션에서는 Bearer 토큰 프로필에 대해 자세히 설명하고 부록 G에서는 MAC 토큰 프로필에 대해 설명합니다.

### OAuth 2.0 무기명 토큰 프로필

OAuth 2.0 `Bearer Token Profile`은 Bearer 토큰만 지원하는 OAuth WRAP의 영향을 받았습니다. 이름에서 알 수 있듯이 토큰을 소지한 사람은 누구나 사용할 수 있습니다. 잃어버리지 마세요! 전달자 토큰은 전송 중에 손실되지 않도록 항상 TLS(전송 계층 보안)를 통해 사용해야 합니다. 승인 서버에서 전달자 액세스 토큰을 받으면 클라이언트는 이를 세 가지 방법으로 사용하여 리소스 서버와 통신할 수 있습니다. 이 세 가지 방법은 RFC 6750에 정의되어 있습니다. 가장 널리 사용되는 방법은 HTTP Authorization 헤더에 액세스 토큰을 포함하는 것입니다.

> **참고**
>
> OAuth 2.0 전달자 토큰은 참조 토큰 또는 자체 포함 토큰일 수 있습니다. 참조 토큰은 임의의 문자열입니다. 공격자는 무차별 대입 공격을 수행하여 토큰을 추측할 수 있습니다. 권한 부여 서버는 올바른 길이를 선택하고 무차별 대입을 방지하기 위해 다른 가능한 조치를 사용해야 합니다. 자체 포함된 액세스 토큰은 7장에서 설명하는 `JSON 웹 토큰(JWT)`입니다. 리소스 서버가 참조 토큰인 액세스 토큰을 받으면 토큰을 확인하기 위해 권한 부여와 대화해야 합니다. 서버(또는 토큰 발행자). 액세스 토큰이 JWT인 경우 리소스 서버는 JWT의 서명을 확인하여 자체적으로 토큰의 유효성을 검사할 수 있습니다.

```
GET /resource HTTP/1.1
Host: rs.example.com
Authorization: Bearer JGjhgyuyibGGjgjkjdlsjkjdsd
```

액세스 토큰은 쿼리 매개변수로 포함될 수도 있습니다. 이 접근 방식은 주로 JavaScript로 개발된 클라이언트 애플리케이션에서 사용됩니다.

```
GET /resource?access_token=JGjhgyuyibGGjgjkjdlsjkjdsd
Host: rs.example.com
```

> **참고**
>
> OAuth 액세스 토큰 값을 쿼리 파라미터로 보낼 때 파라미터 이름은 `access_token`이어야 합니다. Facebook과 Google 모두 올바른 이름을 사용하지만 LinkedIn은 `oauth2_access_token`을 사용하고 Salesforce는 `oauth_token`을 사용합니다.

액세스 토큰을 형식 인코딩된 본문 매개변수로 보낼 수도 있습니다. Bearer 토큰 프로필을 지원하는 인증 서버는 다음 패턴 중 하나를 처리할 수 있어야 합니다.

```
POST /resource HTTP/1.1
Host: server.example.com
Content-Type: application/x-www-form-urlencoded
access_token=JGjhgyuyibGGjgjkjdlsjkjdsd
```
> **참고**
>
> OAuth 전달자 토큰의 값은 인증 서버에만 의미가 있습니다. 클라이언트 앱은 내용을 해석하려고 해서는 안 됩니다. 처리 논리를 효율적으로 만들기 위해 권한 부여 서버는 액세스 토큰에 의미 있지만 기밀이 아닌 데이터를 포함할 수 있습니다. 예를 들어, 권한 부여 서버가 다중 테넌트를 지원하는 여러 도메인을 지원하는 경우 액세스 토큰에 테넌트 도메인을 포함시킨 다음 base64로 인코딩하거나 단순히 JWT를 사용할 수 있습니다.


## OAuth 2.0 Client Types

OAuth 2.0은 기밀 클라이언트와 공개 클라이언트의 두 가지 유형의 클라이언트를 식별한다. 기밀 클라이언트는 자신의 자격 증명(클라이언트 키 및 암호)을 보호할 수 있지만 공개 클라이언트는 보호할 수 없다. OAuth 2.0 사양은 웹 앱, 사용자 에이전트 기반 앱 및 기본 앱의 세 가지 유형의 클라이언트 프로필을 중심으로 구축된다. 웹 앱은 웹 서버에서 실행되는 기밀 클라이언트로 간주됩니다. 최종 사용자 또는 리소스 소유자는 웹 브라우저를 통해 이러한 앱에 액세스합니다. 사용자 에이전트 기반 앱은 공개 클라이언트로 간주된다. 

웹 서버에서 코드를 다운로드하여 브라우저에서 실행되는 JavaScript와 같은 사용자 에이전트에서 실행합니다. 이러한 클라이언트는 자격 증명을 보호할 수 없습니다. 최종 사용자는 JavaScript에서 무엇이든 볼 수 있습니다. 기본 앱도 공용 클라이언트로 간주됩니다. 이러한 클라이언트는 최종 사용자의 제어 하에 있으며 해당 앱에 저장된 모든 기밀 데이터를 추출할 수 있습니다. Android 및 iOS 기본 앱이 예이다.


> **참고**
>
> OAuth 2.0 핵심 사양에 정의된 4가지 권한 유형 모두 클라이언트는 인증 서버에 사전 등록해야 하며 그 대가로 클라이언트 식별자를 받습니다. 암시적 부여 유형에서 클라이언트는 클라이언트 비밀을 얻지 못합니다. 동시에 다른 권한 부여 유형에서도 클라이언트 암호를 사용할지 여부는 옵션입니다.

Table 4-2 OAuth 2.0 Bearer Token Profile

- An authorization framework for access delegation
- Nonsignature-based, Bearer Token Profile
- Highly extensible via grant types and token types
- More developer-friendly
- Bearer Token Profile mandates using TLS during the entire flow
- Secret key goes on the wire (Bearer Token Profile)


> **참고**
>
> OAuth 2.0은 클라이언트, 리소스 소유자, 인증 서버 및 리소스 서버를 명확하게 구분합니다. 그러나 핵심 OAuth 2.0 사양은 리소스 서버가 액세스 토큰의 유효성을 검사하는 방법에 대해 이야기하지 않습니다. 대부분의 OAuth 구현은 권한 부여 서버에 의해 노출된 독점 API와 통신하여 이 작업을 시작했습니다. OAuth 2.0 Token Introspection 프로필은 이를 어느 정도 표준화했으며 9장에서 이에 대해 자세히 설명합니다.

### JWT Secured Authorization Request (JAR)

권한 부여 서버의 권한 부여 엔드포인트에 대한 OAuth 2.0 요청에서 모든 요청 매개변수는 브라우저를 통해 쿼리 매개변수로 흐른다. 다음은 OAuth 2.0 인증 코드 부여 요청의 예이다.

```
https://authz.example.com/oauth2/authorize?
    response_type=token&
    client_id=0rhQErXIX49svVYoXJGt0DWBuFca&
    redirect_uri=https%3A%2F%2Fmycallback
```

이 접근 방식에는 몇 가지 문제가 있습니다. 이러한 매개변수는 브라우저를 통해 흐르기 때문에 최종 사용자 또는 브라우저의 모든 사용자는 인증 서버에서 예상치 못한 결과를 초래할 수 있는 입력 매개변수를 변경할 수 있습니다. 동시에 요청이 무결성으로 보호되지 않기 때문에 권한 부여 서버는 요청을 시작한 사람을 확인할 수단이 없습니다. JWT 보안 인증 요청을 사용하면 이 두 가지 문제를 해결할 수 있습니다.

JWT가 처음이라면 7장과 8장을 확인하십시오. JWT는 암호화된 방식으로 이해 당사자 간에 데이터를 전송하는 컨테이너를 정의합니다. IETF JOSE 작업 그룹에서 개발된 JWS 사양은 메시지 또는 페이로드를 나타내며 디지털 서명 또는 MAC(해싱 알고리즘이 HMAC와 함께 사용되는 경우)되는 반면 JWE 사양은 암호화된 페이로드를 나타내는 방법을 표준화합니다.

IETF OAuth 작업 그룹에 대한 초안 제안 중 하나는 JWT에서 요청 매개변수를 보내는 기능을 도입할 것을 제안합니다. 이 기능을 사용하면 요청이 JWS로 서명되고 JWE로 암호화되어 승인 요청이 보존됩니다. 작성 당시 이 제안은 매우 초기 단계에 있으며 `SAML` Single Sign-On에 익숙하다면 이는 SAML의 서명된 인증 요청과 매우 유사합니다. 다음은 JWT 내에 이상적으로 들어가는 샘플 승인 요청의 디코딩된 페이로드를 보여줍니다.

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

클라이언트 앱이 JWT(7장과 8장 참조)를 구성하면 두 가지 방법으로 OAuth 권한 부여 서버에 권한 부여 요청을 보낼 수 있습니다. 한 가지 방법은 값으로 전달하는 것이고 다른 하나는 참조로 전달하는 것입니다. 다음은 클라이언트 앱이 `request`라는 쿼리 매개변수에서 JWT를 보내는 값으로 전달하는 예를 보여줍니다. 다음 요청의 `[jwt_assertion]`은 실제 JWS 또는 JWE를 나타냅니다.
```
https://server.example.com/authorize?request=[jwt_assertion]
```
JWT 승인 요청에 대한 제안 초안은 다음과 같이 값에 의한 전달 방법의 몇 가지 제한 사항을 극복하기 위해 참조에 의한 전달 방법을 도입합니다.

- 이 글을 쓰는 현재 시장에 나와 있는 많은 휴대전화는 여전히 대용량 페이로드를 허용하지 않습니다. 페이로드 제한은 일반적으로 512 또는 1024 ASCII 문자입니다.

- 이전 버전의 Internet Explorer에서 지원하는 최대 URL 길이는 2083 ASCII 문자입니다.

- 2G 모바일 연결과 같이 느린 연결에서 URL이 크면 응답이 느릴 수 있습니다. 따라서 이러한 사용은 사용자 경험의 관점에서 바람직하지 않습니다.

다음은 참조에 의한 전달의 예를 보여줍니다. 여기서 클라이언트 애플리케이션은 요청에 링크를 전송합니다. 링크는 인증 서버에서 JWT를 가져오는 데 사용할 수 있습니다. 이것은 새로운 request_uri 쿼리 매개변수와 함께 일반적인 OAuth 2.0 인증 코드 요청입니다. `request_uri` 매개변수의 값은 해당 JWS 또는 JWE를 가리키는 링크를 전달합니다.

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

- OAuth 2.0은 공개 클라이언트와 기밀 클라이언트의 두 가지 유형의 클라이언트 앱을 지원한다. SPA와 기본 모바일 앱은 공개 클라이언트에 속하는 반면 웹 앱은 기밀 클라이언트에 속한다.

- OAuth 2.0 인증 프레임워크: JWT 보안 인증 요청(JAR) 제안은 JWT에서 요청 매개변수를 보내는 기능을 도입할 것을 제안한다.

- PAR(Pushed Authorization Requests) 제안은 인증 서버 끝에 푸시 엔드포인트를 도입하여 클라이언트 앱이 모든 인증 요청 매개변수를 안전하게 푸시한 다음 브라우저 기반 로그인 흐름을 시작할 수 있도록 제안한다.