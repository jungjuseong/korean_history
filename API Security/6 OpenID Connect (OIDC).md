# 6. OpenID Connect (OIDC)

OpenID Connect는 RESTful 방식으로 식별 인터랙션하는 경량 프레임워크를 제공한다. 

## From OpenID to OIDC

2005년 `SAML`(Security Assertion Markup Language)의 뒤를 이은 OpenID는 웹 인증에 혁명을 일으켰습니다. LiveJournal의 설립자인 Brad Fitzpatrick은 OpenID의 초기 아이디어를 생각해 냈습니다. OpenID와 SAML(12장에서 설명)의 기본 원칙은 동일합니다. 둘 다 웹 SSO(Single Sign-On) 및 도메인 간 ID 연합을 용이하게 하는 데 사용할 수 있습니다. OpenID는 커뮤니티 친화적이며 사용자 중심적이며 분산되어 있습니다. 야후! 2008년 1월에 OpenID 지원을 추가했고, 같은 해 7월에 MySpace가 OpenID에 대한 지원을 발표했으며, Google은 10월에 파티에 합류했습니다. 2009년 12월까지 OpenID 사용 계정이 10억 개 이상이었습니다. 웹 SSO 프로토콜로서 큰 성공을 거두었습니다.

OpenID와 OAuth 1.0은 두 가지 다른 문제를 해결합니다. OpenID는 인증에 관한 것이고 OAuth 1.0은 위임된 권한에 관한 것입니다. 이 두 표준이 각각의 영역에서 인기를 얻으면서 사용자를 인증하고 단일 단계로 사용자를 대신하여 리소스에 액세스할 수 있는 토큰을 얻을 수 있도록 결합하는 데 관심이 있었습니다.
Google Step 2 프로젝트는 이러한 방향에서 처음으로 진지한 노력을 기울였습니다. 기본적으로 OpenID 요청/응답에서 OAuth 관련 매개변수를 사용하는 OAuth용 OpenID 확장을 도입했습니다. Google Step 2 프로젝트를 시작한 동일한 사람들이 나중에 이를 OpenID Foundation으로 가져왔습니다.

OpenID는 현재까지 3세대를 거쳤습니다. OpenID 1.0/1.1/2.0이 1세대이고 OAuth용 OpenID 확장이 2세대입니다. OpenID Connect(OIDC)는 OpenID의 3세대입니다. Yahoo!, Google 및 기타 많은 OpenID 제공업체는 2015년 중반경 OpenID에 대한 지원을 중단하고 OpenID Connect로 마이그레이션했습니다.

> `OpenID Connect`는 `OpenID`가 아니다. `OpenID`가 작동하는 방식이다.
>
> 관리할 프로파일이 많을 것이다. 전화번호나 집 주소가 바뀔때 모든 프로파일을 수정하거나 그냥 둘 것이다. `OpenID`는 다른 웹사이트에 흩어져 있는 프로파일 문제를 해결한다. `OpenID`는 프로파일을 `OpenID` 제공자에서만 관리하고 다른 사이트들은 `OpenID`에 의존되도록 한다. 의존하는 사이트에서는 여러분의 정보를 `OpenID` 제공자로부터 얻어온다.

웹 사이트에 로그인하려고 할 때마다 `OpenID` 제공자에게 페이지 전환이 된다. `OpenID` 제공자에서 여러분은 인증을 하고 여러분의 속성에 대한 요청을 승인 한다. 승인이 되면 요청된 속성을 갖고 돌아온다.  This goes beyond simple attribute sharing to facilitate decentralized SSO.

`SSO`로는 `OpenID` 제공자에서 한번 로그인 할뿐이다. 즉, 사이트가 처음에 `OpenID` 제공자에게 여러분을 보낼 때이다. 그 후부터는 다른 사이트에서 `OpenID` 제공자에게 보내도 자격증명을 요구하지 않고 그 전에 만들어진  인증된 세션을 사용한다. 이 인증 세션은 브라우저가 닫힐 때까지 쿠키에 또는 영속 쿠키에 저장된다. 

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_6_Fig1_HTML.jpg)

Figure 6-1 OpenID protocol 플로우

> 사용자는 사이트 상에 그의 `OpenID`를 입력하여 `OpenID` 플로우를 시작한다. OpenID는 고유 URL이다. For example, http://prabath.myopenid.com is an `OpenID` 
> 사용자가 그의 `OpenID`를 입력하면, 의존 사이트는 해당하는 `OpenID` 제공자를 찾아야 한다.
> 신뢰 당사자는 `OpenID(URL)`에 대해 `HTTP GET`을 수행하여 뒤에 있는 `HTML` 텍스트를 다시 가져옵니다.
> 예를 들어 http://prabath.myopenid.com 뒤에 있는 소스를 보면 다음 태그가 표시됩니다(MyOpenID는 몇 년 전에 삭제되었습니다). 이것이 바로 신뢰 당사자가 검색 단계에서 보는 것입니다. 이 태그는 제공된 OpenID 뒤에 있는 OpenID 공급자를 나타냅니다.
> `<link rel="openid2.provider" href="http://www.myopenid.com/server" />`
>
> `OpenID`는 사용자에게 `OpenID`를 요청하는 것 외에 `OpenID `공급자를 식별하는 또 다른 방법이 있습니다. 이를 `직접 ID`라고 하며 Yahoo!, Google 및 기타 여러 `OpenID` 제공업체에서 사용했습니다. 신뢰 당사자가 직접 ID를 사용하는 경우 `OpenID` 공급자가 누구인지 이미 알고 있으므로 검색 단계가 필요하지 않습니다. 신뢰 당사자는 지원하는 `OpenID` 공급자 집합을 나열하고 사용자는 인증할 공급자를 선택해야 합니다.
>
> `OpenID` 공급자가 검색되면 다음 단계는 신뢰 당사자의 유형에 따라 다릅니다. 스마트 신뢰 당사자인 경우 그림 6-1의 3단계를 실행하여 `OpenID` 공급자와의 연결을 생성합니다. 연결하는 동안 `OpenID` 공급자와 신뢰 당사자 간에 공유 비밀 키가 설정됩니다. 두 당사자 간에 키가 이미 설정되어 있으면 스마트 신뢰 당사자라도 이 단계를 건너뜁니다. 멍청한 신뢰 당사자는 항상 3단계를 무시합니다.
>
> 5단계에서 사용자는 검색된 `OpenID` 공급자로 리디렉션됩니다. 6단계에서 사용자는 신뢰 당사자의 속성 요청을 인증하고 승인해야 합니다(6, 7단계). 승인 시 사용자는 신뢰 당사자에게 다시 리디렉션됩니다(9단계). `OpenID` 공급자와 해당 신뢰 당사자만 알고 있는 키는 `OpenID` 공급자의 이 응답에 서명합니다. 신뢰 당사자가 응답을 받으면 스마트 신뢰 당사자인 경우 서명 자체의 유효성을 검사합니다. 연결 단계에서 공유된 키는 메시지에 서명해야 합니다. 벙어리 신뢰 당사자인 경우 10단계(브라우저 리디렉션이 아님)에서 `OpenID` 공급자와 직접 대화하고 서명의 유효성을 검사하도록 요청합니다. 결정은 11단계에서 신뢰 당사자에게 다시 전달되고 이로써 `OpenID` 프로토콜 흐름이 종료됩니다.

### Amazon은 여전히 ​​OpenID 2.0을 사용합니다.

Amazon이 사용자 인증에 여전히 OpenID를 사용한다는 사실을 아는 사람은 거의 없습니다. 직접 확인하십시오: `www.amazon.com`으로 이동하여 로그인 버튼을 클릭하십시오. 그런 다음 브라우저 주소 표시줄을 관찰합니다. `OpenID` 인증 요청인 다음과 유사한 내용이 표시됩니다.
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

## OpenID Connect 이해하기

`OpenID Connect`는 OAuth2 위에 `식별 레이어`를 도입한다. 이 `식별 레이어`는 ID 토큰으로 추상화되며 7장에서 자세히 다룬다. OAuth2 인증 서버는 액세스 토큰과 함께 ID 토큰을 리턴한다.

`OpenID Connect`는 OAuth2 위에 구축된 프로파일이다. OAuth는 액세스 위임에 관한 것이라면 `OpenID Connect`는 인가에 대한 것이다. 즉, `OIDC`는 OAuth2 위에 식별 레이어를 구축한다.

인증Authentication은 데이터 또는 엔티티의 속성이 참인가를 확인하는 행동이다. 내가 나를 `Peter`라고 말한다면 나는 이를 증명할 필요가 있다. 내가 아는 무엇 또는 내가 가진 무엇인가로 증명할 수 있다. 내가 나임을 증명하였다면 시스템이 나를 신뢰할 수 있다. 때때로 시스템은 이름만으로 사용자를 식별하기를 원하지 않는다. 이름이 고유하게 식별하는데 도움이 될 수도 있지만 다른 속성은 어떨까? 국경을 통과하려면 이름으로, 사진이나 지문 홍채로 식별하기도 한다. VISA 오피스에도 실시간으로 식별에 사용한다. VISA를 가진 사람이 동일한 인물임을 확인하고 입국을 승인한다.

이것이 여러분의 신분을 제공한다. 신분을 증명하는 것이 인증이다. 

인가authorization는 여러분이 할 수 있는 권한 또는 능력에 관한 것이다. 
국경에서 여러분의 신분을 증명하는데 사진, 지문, 홍채등을 사용하지만 여러분이 할 수 있는 일은 VISA가 결정한다. 입국을 하려면 비자의 유효기간이 남아 있어야 한다. 유효한 비자는 여러분을 식별하는 부분이 아니고 여러분이 무엇을 할 수 있는지를 결정한다. 입국한 나라에서 가능한 일은 비자 타입이 결정한다. B1/B2 비자와 L1/L2 비자는 다른다. 이것이 인가이다.

OAuth2는 인증이 아니라 인가에 관한 것이다. OAuth2로는 클라이언트가 사용자에 관해 모른다. 사용자 대신 리소스에 접근하기 위한 액세스 토큰을 얻는다. `OpenID Connect`로는 클라이언트는 액세스 토큰과 함께 ID 토큰을 얻는다. ID 토큰은 사용자의 신분을 나타낸다. 

`OpenID Connect`로 API를 보호한다는 것이 어떤 의미인가? 또는 완전히 의미없는가? `OpenID Connect`는 API 레벨이나 리소스 서버 레벨이 아니라 앱 또는 클라이언트 레벨이다. `OpenID Connect`는 클라이언트가 사용자가 누군지를 발견하는데 도움을 주지만 API에게는 의미가 없다. API가 기대하는 유일한 것은 액세스 토큰이다. 리소스 소유자 또는 API가 사용자에 대해 알고 싶다면 인가 서버에게 질의를 하거나 JWT와 같은 자체 정보가 들어있는 액세스 토큰을 참조한다.

## Anatomy of the ID Token

ID 토큰은 OAuth2가 `OpenID Connect`를 지원하기 위한 주요 애드온이다. 이것은 인증 서버로부터 클라이언트에게 인증된 사용자 정보를 운반하는 JWT이다.
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

- iss: '철저한 서버'의 식별자. 실제로 대부분의 OpenID 공급자 구현 또는 제품을 사용하면 원하는 발급자를 구성할 수 있습니다. 또한 이는 대부분 URL이 아닌 식별자로 사용됩니다. ID 토큰의 필수 속성입니다.

- sub: 토큰 발행자 또는 어설션 당사자가 특정 엔터티에 대한 ID 토큰을 발급하고 ID 토큰에 포함된 클레임 집합은 일반적으로 `sub` 매개 변수로 식별되는 이 엔터티를 나타냅니다. `sub` 매개변수의 값은 대소문자를 구분하는 문자열 값이며 ID 토큰의 필수 속성입니다.

- ud: 토큰의 청중입니다. 이것은 식별자의 배열일 수 있지만 그 안에 OAuth 클라이언트 ID가 있어야 합니다. 그렇지 않으면 이 섹션의 뒷부분에서 설명하는 `azp` 매개 변수에 클라이언트 ID를 추가해야 합니다. 유효성 검사를 하기 전에 OpenID 클라이언트는 먼저 특정 ID 토큰이 사용을 위해 발급되었는지 확인하고 그렇지 않은 경우 즉시 거부해야 합니다. 즉, `aud` 속성의 값이 `OpenID` 클라이언트의 식별자와 일치하는지 확인해야 합니다. `aud` 매개변수의 값은 대소문자를 구분하는 문자열 값 또는 문자열 배열일 수 있습니다. ID 토큰의 필수 속성입니다.

- nonce: 초기 권한 부여 요청에 대해 `OpenID Connect` 사양에 의해 도입된 새 매개변수입니다. OAuth 2.0에 정의된 매개변수 외에도 클라이언트 애플리케이션은 선택적으로 nonce 매개변수를 포함할 수 있습니다. 이 매개변수는 재생 공격을 완화하기 위해 도입되었습니다. 인증 서버는 동일한 `nonce` 값을 가진 두 개의 요청을 찾으면 모든 요청을 거부해야 합니다. 권한 부여 요청에 `nonce가` 있는 경우 권한 부여 서버는 ID 토큰에 동일한 값을 포함해야 합니다. 클라이언트 응용 프로그램은 권한 부여 서버에서 ID 토큰을 받으면 nonce 값을 확인해야 합니다.

- exp: 각 ID 토큰에는 만료 시간이 있습니다. 해당 토큰이 만료된 경우 ID 토큰의 수신자는 이를 거부해야 합니다. 발행자는 만료 시간의 값을 결정할 수 있습니다. `exp`의 값은 `1970-01-01T00:00:00Z UTC`부터 현재 시간까지 경과된 시간에 초 단위의 만료 시간(토큰 발행 시간)을 더하여 계산됩니다. 토큰 발행자의 시계가 수신자의 시계와 동기화되지 않으면(시간대에 관계없이) 만료 시간 유효성 검사가 실패할 수 있습니다. 이 문제를 해결하기 위해 각 수신자는 유효성 검사 프로세스 동안 시계 왜곡으로 몇 분을 추가할 수 있습니다. ID 토큰의 필수 속성입니다.

- iat: ID 토큰의 `iat` 매개변수는 토큰 발행자가 계산한 ID 토큰의 발행 시간을 나타냅니다. `iat` 매개변수의 값은 `1970-01-01T00:00:00Z UTC`부터 토큰이 발행된 현재 시간까지 경과된 시간(초)입니다. ID 토큰의 필수 속성입니다.

- auth_time: 최종 사용자가 인가 서버로 인증하는 시간. 사용자가 이미 인증된 경우 인증 서버는 사용자에게 다시 인증을 요청하지 않습니다. 주어진 인증 서버가 사용자를 인증하는 방법과 인증된 세션을 관리하는 방법은 `OpenID Connect`의 범위를 벗어납니다. 사용자는 `OpenID` 클라이언트 앱아닌 다른 앱에서 첫 번째 로그인 시도에서 권한 부여 서버와 인증된 세션을 만들 수 있습니다. 이러한 경우 권한 부여 서버는 인증된 시간을 유지하고 이를 `auth_time`에 포함해야 합니다. 이것은 선택적입니다.

- acr: 인증 컨텍스트 클래스 참조를 나타냅니다. 이 매개변수의 값은 권한 부여 서버와 클라이언트 응용 프로그램 모두에서 이해해야 합니다. 인증 수준을 나타냅니다. 예를 들어 사용자가 수명이 긴 브라우저 쿠키로 인증하는 경우 수준 0으로 간주됩니다. `OpenID Connect` 사양에서는 금전적 가치가 있는 리소스에 액세스하는 데 인증 수준 0을 사용하지 않는 것이 좋습니다. 이것은 선택적 매개변수입니다.

- amr: 인증 방법 참조를 나타냅니다. 권한 부여 서버가 사용자를 인증하는 방법을 나타냅니다. 값 배열로 구성될 수 있습니다. 권한 부여 서버와 클라이언트 애플리케이션 모두 이 매개변수의 값을 이해해야 합니다. 예를 들어, 사용자가 사용자 이름/암호와 SMS를 통한 일회성 암호를 사용하여 권한 부여 서버에서 인증하는 경우 `amr` 값은 이를 나타내야 합니다. 이것은 선택적 매개변수입니다.

- azp: Stands for authorized party. It’s needed when there is one audience (aud) and its value is different from the OAuth client ID. The value of azp must be set to the OAuth client ID. This is an optional parameter.

> **Note**
>
> 인증 서버는 `JWS` 사양에 정의된 대로 `ID 토큰`에 서명해야 합니다. 선택적으로 암호화할 수도 있습니다. 토큰 암호화는 `JWE(JSON Web Encryption)` 사양에 정의된 규칙을 따라야 합니다. `ID 토큰`이 암호화된 경우 먼저 서명한 다음 암호화해야 합니다. 많은 법인에서 암호화된 텍스트에 서명하는 것이 의심스럽기 때문입니다. 7장과 8장에서는 JWT, JWS 및 JWE에 대해 설명합니다.


### WSO2 ID 서버와 OpenID 연결

이 연습에서는 OAuth 2.0 액세스 토큰과 함께 `OpenID Connect` ID 토큰을 얻는 방법을 봅니다. 여기에서 `WSO2 Identity Server`를 OAuth 2.0 인증 서버로 실행합니다.

> **참고**
>
> `WSO2 Identity Server`는 Apache 2.0 라이선스에 따라 출시된 무료 오픈 소스 ID 및 자격 관리 서버입니다. 이 글을 쓰는 시점에서 최신 릴리스 버전은 5.9.0이며 Java 8에서 실행됩니다.

다음 단계에 따라 응용 프로그램을 `WSO2 Identity Server`에 서비스 공급자로 등록한 다음 `OpenID Connect`를 통해 응용 프로그램에 로그인합니다.

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

`ID 토큰`은 `OpenID Connect`의 핵심이지만 OAuth 2.0에서 벗어난 유일한 곳은 아닙니다. `OpenID Connect`는 OAuth 2.0 권한 부여 요청에 몇 가지 선택적 매개변수를 도입했습니다. 이전 연습에서는 이러한 매개변수를 사용하지 않았습니다. 모든 선택적 매개변수가 포함된 샘플 권한 부여 요청을 살펴보겠습니다.

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
> All `OpenID Connect` authentication requests must have a `scope` parameter with the value openid.

### Requesting User Attributes

`OpenID Connect`는 사용자 속성을 요청하는 두 가지 방법을 정의합니다. 클라이언트 앱은 초기 `OpenID Connect` 인증 요청을 사용하여 속성을 요청하거나 나중에 권한 부여 서버에서 호스팅하는 `UserInfo` 끝점과 통신할 수 있습니다. 초기 인증 요청을 사용하는 경우 클라이언트 앱은 요청 매개변수에 `JSON` 메시지로 요청된 클레임을 포함해야 합니다. 다음 권한 부여 요청은 ID 토큰에 사용자의 이메일 주소와 이름을 포함하도록 요청합니다.

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
> `OpenID Connect` 핵심 사양은 20개의 표준 사용자 클레임을 정의합니다. 이러한 식별자는 `OpenID Connect`를 지원하는 모든 권한 부여 서버 및 클라이언트 앱에서 이해해야 합니다. OpenID Connect 표준 주장의 전체 집합은 http://openid.net/specs/openid-connect-core-1_0.html에서 사용할 수 있는 `OpenID Connect` 핵심 사양의 섹션 5.1에 정의되어 있습니다.

사용자 속성을 요청하는 다른 접근 방식은 `UserInfo` 끝점을 사용하는 것입니다. `UserInfo` 끝점은 권한 부여 서버의 OAuth 2.0 보호 리소스입니다. 이 끝점에 대한 모든 요청에는 유효한 OAuth 2.0 토큰이 있어야 합니다. 다시 한 번, `UserInfo` 끝점에서 사용자 특성을 가져오는 두 가지 방법이 있습니다. 첫 번째 접근 방식은 OAuth 액세스 토큰을 사용하는 것입니다. 이 접근 방식을 사용하면 클라이언트는 권한 부여 요청에 해당 속성 범위를 지정해야 합니다. `OpenID Connect` 사양은 속성을 요청하기 위해 프로필, 이메일, 주소 및 전화의 네 가지 범위 값을 정의합니다. 범위 값이 프로필로 설정되면 클라이언트가 name, family_name, given_name, middle_name, 별명, preferred_username, 프로필, 사진, 웹사이트, 성별, 생년월일, zoneinfo, locale, 및 updated_at.

다음 권한 부여 요청은 사용자의 이메일 주소 및 전화 번호에 대한 액세스 권한을 요청합니다.

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

UserInfo 끝점에서 사용자 특성을 검색하는 다른 방법은 `claim` 매개 변수를 사용하는 것입니다. 다음 예는 OAuth로 보호되는 `UserInfo` 엔드포인트와 통신하여 사용자의 이메일 주소를 검색하는 방법을 보여줍니다.
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

### OpenID 연결 흐름

지금까지 이 장의 모든 예제는 인증 코드 부여 유형을 사용하여 ID 토큰을 요청했지만 요구 사항은 아닙니다. 실제로 `OpenID Connec`t는 OAuth 2.0 권한 부여 유형과 관계없이 코드 흐름, 암시적 흐름 및 하이브리드 흐름과 같은 일련의 흐름을 정의했습니다. 각 흐름은 `response_type` 값을 정의합니다. `response_type`는 항상 권한 부여 끝점에 대한 요청과 함께 이동하며(반대로 `grant_type` 매개변수는 항상 토큰 끝점으로 이동) 권한 부여 끝점에서 예상되는 응답 유형을 정의합니다. `code`로 설정하면 인증 서버의 인증 엔드포인트가 코드를 반환해야 하며 이 흐름은 `OpenID Connect`에서 인증 코드 흐름으로 식별됩니다.

`OpenID Connect` 컨텍스트에서 암시적 흐름의 경우 `response_type`의 값은 `id_token` 또는 `id_token` 토큰(공백으로 구분)일 수 있습니다. `id_token`인 경우 권한 부여 서버는 권한 부여 끝점에서 ID 토큰을 반환합니다. 둘 다 포함하는 경우 ID 토큰과 액세스 토큰이 모두 응답에 포함됩니다.

하이브리드 흐름은 다른 조합을 사용할 수 있습니다. response_type의 값이 코드 id_token(공백으로 구분)으로 설정되면 인증 끝점의 응답에는 인증 코드와 id_token이 포함됩니다. 코드 토큰(공백으로 구분)인 경우 액세스 토큰(UserInfo 끝점용)과 함께 인증 코드를 반환합니다. response_type이 세 가지(코드 토큰 id_token)를 모두 포함하는 경우 응답에는 id_token, 액세스 토큰 및 인증 코드가 포함됩니다. 표 6-1은 이 논의를 요약한 것입니다.

Table 6-1 OpenID Connect Flows

Type of Flow: response_type, Tokens Returned

- Authorization code: code,  Authorization code
- Implicit: id_token, ID token
- Implicit: id_token token, ID token and access token
- Hybrid: code id_token, ID token and authorization code
- Hybrid: code id_token token, ID token, authorization code, and access token
- Hybrid: code token, Access token and authorization code

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

## OpenID Connect Discovery

이 장의 시작 부분에서 OpenID 신뢰 당사자가 사용자 제공 OpenID(URL)를 통해 OpenID 공급자를 찾는 방법에 대해 논의했습니다. `OpenID Connect Discovery`는 동일한 문제를 해결하지만 다른 방식으로 해결합니다(그림 6-2 참조). OpenID Connect를 통해 사용자를 인증하려면 OpenID Connect 신뢰 당사자는 먼저 최종 사용자 뒤에 있는 인증 서버를 파악해야 합니다. OpenID Connect는 이 검색을 위해 WebFinger(RFC 7033) 프로토콜을 사용합니다.

> **참고**
>
> OpenID Connect Discovery 사양은 http://openid.net/specs/openid-connect-discovery-1_0.html에서 확인할 수 있습니다. 주어진 OpenID Connect 신뢰 당사자가 이미 인증 서버가 누구인지 알고 있는 경우 검색 단계를 무시할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_6_Fig2_HTML.jpg)

Figure 6-2 OpenID Connect Discovery

`Peter`라는 사용자가 `OpenID Connect` 신뢰 당사자를 방문하여 로그인하려고 한다고 가정해 보겠습니다(그림 6-2 참조). `Peter`를 인증하려면 `OpenID Connect` 신뢰 당사자가 `Peter`에 해당하는 권한 부여 서버를 알아야 합니다. 이를 발견하기 위해 `Peter`는 신뢰 당사자에게 자신과 관련된 고유 식별자를 제공해야 합니다. 이 식별자를 사용하여 신뢰 당사자는 `Peter`에 해당하는 `WebFinger` 끝점을 찾을 수 있어야 합니다.

`Peter`가 제공한 식별자가 그의 이메일인 `peter@apress.com`이라고 가정해 보겠습니다(1단계). 신뢰 당사자는 Peter의 이메일 주소를 사용하여 WebFinger 엔드포인트에 대한 충분한 세부 정보를 찾을 수 있어야 합니다. 사실, 신뢰 당사자는 이메일 주소에서 `WebFinger` 끝점을 파생할 수 있어야 합니다. 그런 다음 신뢰 당사자는 `WebFinger` 끝점에 쿼리를 보내어 인증 서버(또는 ID 제공자)가 `Peter`에 해당하는지 확인할 수 있습니다(2단계 및 3단계). 이 쿼리는 `WebFinger` 사양에 따라 수행됩니다. 다음은 `peter@apress.com`에 대한 샘플 `WebFinger` 요청을 보여줍니다.

```
GET /.well-known/webfinger?resource=acct:peter@apress.com
&rel=http://openid.net/specs/connect/1.0/issuer HTTP/1.1
Host: apress.com
```
`WebFinger` 요청에는 리소스와 rel의 두 가지 주요 매개변수가 있습니다. `resource` 매개변수는 최종 사용자를 고유하게 식별해야 하지만 rel 값은 `OpenID Connect`에 대해 고정되어 있고 http://openid.net/specs/connect/1.0/issuer와 같아야 합니다. rel(relation-type) 매개변수는 주어진 리소스에 해당하는 `OpenID Connect` 발급자를 결정하는 필터 역할을 합니다.

`WebFinger` 끝점은 다른 서비스에 대한 다른 많은 검색 요청을 수락할 수 있습니다. 일치하는 항목을 찾으면 다음 응답이 `OpenID Connect` 신뢰 당사자에게 반환됩니다. OpenID ID 제공자 또는 권한 부여 서버 엔드포인트의 값이 응답에 포함됩니다.

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

The WebFinger specification has the well-known endpoint /.well-known/webfinger. The OpenID Connect Discovery specification has the well-known endpoint for OpenID provider configuration metadata, `/.well-known/openid-configuration`.

### OpenID Connect Identity Provider Metadata

메타데이터 검색을 지원하는 `OpenID Connect ID` 공급자는 `/.well-known/openid-configuration` 끝점에서 구성을 호스팅해야 합니다. 대부분의 경우 이것은 누구나 액세스할 수 있는 보안되지 않은 끝점입니다. `OpenID Connect` 신뢰 당사자는 HTTP GET을 메타데이터 끝점으로 보내 다음과 같이 OpenID 공급자 구성 세부 정보를 검색할 수 있습니다.
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


### 동적 클라이언트 등록

OpenID 제공자 엔드포인트가 `WebFinger`(및 `OpenID Connect` Discovery를 통해 관련된 모든 메타데이터)를 통해 검색되면 `OpenID Connect` 신뢰 당사자는 여전히 클라이언트 ID와 클라이언트 비밀(암시적 부여 유형이 아님)을 등록해야 합니다. OpenID 제공자에서 권한 부여 요청 또는 `OpenID Connect` 인증 요청을 시작합니다. `OpenID Connect` 동적 클라이언트 등록 사양2은 OpenID 공급자에서 `OpenID Connect` 신뢰 당사자를 동적으로 등록하는 메커니즘을 용이하게 합니다.

OpenID 공급자 메타데이터 끝점의 응답에는 `registration_endpoint` 매개변수 아래에 클라이언트 등록을 위한 끝점이 포함됩니다. 동적 클라이언트 등록을 지원하려면 이 끝점이 인증 요구 사항 없이 공개 등록 요청을 수락해야 합니다.

서비스 거부(DoS) 공격에 맞서기 위해 속도 제한 또는 WAF(웹 애플리케이션 방화벽)로 엔드포인트를 보호할 수 있습니다. 클라이언트 등록을 시작하기 위해 OpenID 신뢰 당사자는 자체 메타데이터와 함께 HTTP POST 메시지를 등록 끝점에 보냅니다.

다음은 샘플 클라이언트 등록 요청입니다.

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

### API 보안을 위한 OpenID Connect

지금까지 OpenID Connect에 대한 자세한 설명을 보았습니다. 그러나 실제로 API 보안에 어떤 도움이 될까요? 최종 사용자는 `OpenID Connect`를 사용하여 웹 앱, 모바일 앱 등을 인증할 수 있습니다. 그럼에도 불구하고 헤드리스 API를 보호하기 위해 `OpenID Connect`가 필요한 이유는 무엇입니까? 하루가 끝나면 모든 API는 OAuth 2.0으로 보호되며 API와 대화하려면 액세스 토큰을 제시해야 합니다. API(또는 정책 시행 구성 요소)는 권한 부여 서버와 통신하여 액세스 토큰의 유효성을 검사합니다. API에 ID 토큰을 전달해야 하는 이유는 무엇입니까?

OAuth는 위임된 권한 부여에 관한 것이고 `OpenID Connect`는 인증에 관한 것입니다. ID 토큰은 귀하의 신원에 대한 주장, 즉 귀하의 신원을 증명하는 것입니다. API로 인증하는 데 사용할 수 있습니다. 이 글을 쓰는 시점에서 JWT에 대한 HTTP 바인딩은 정의되어 있지 않습니다.

다음 예에서는 JWT 어설션(또는 ID 토큰)을 HTTP Authorization 헤더의 액세스 토큰으로 보호된 API에 전달할 것을 제안합니다. ID 토큰 또는 서명된 JWT는 세 부분으로 base64-url로 인코딩됩니다. 각 부분은 점(.)으로 구분됩니다. 첫 번째 점까지의 첫 번째 부분은 JWT 헤더입니다. 두 번째 부분은 JWT 본문입니다. 세 번째 부분은 서명입니다. 클라이언트 애플리케이션에서 JWT를 얻으면 여기에 표시된 방식으로 HTTP Authorization 헤더에 배치할 수 있습니다.

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

## 요약

- `OpenID Connect`는 OAuth 2.0을 기반으로 구축되었습니다. OAuth 2.0 위에 ID 계층을 도입합니다. 이 ID 계층은 `JWT(JSON Web Token)`인 ID 토큰으로 추상화됩니다.

- `OpenID Connec`t는 OpenID에서 OAuth 2.0 프로필로 발전했습니다.

- `OpenID Connect` 동적 클라이언트 등록 사양은 OpenID 공급자에서 `OpenID Connect` 신뢰 당사자를 동적으로 등록하는 메커니즘을 용이하게 합니다.

- `OpenID Connect`는 사용자 속성을 요청하는 두 가지 방법을 정의합니다. 클라이언트 응용 프로그램은 초기 `OpenID Connect` 인증 요청을 사용하여 속성을 요청하거나 나중에 권한 부여 서버에서 호스팅하는 `UserInfo` 끝점과 통신할 수 있습니다.

- `OpenID Connect`는 `OpenID Connect ` 동적 클라이언트 등록 및 ID 제공자 메타데이터 구성과 함께 검색 프로세스에서 `WebFinger` 프로토콜을 활용합니다.

- 메타데이터 검색을 지원하는 `OpenID Connect` ID 공급자는 `/.well-known/openid-configuration` 끝점에서 구성을 호스팅해야 합니다.