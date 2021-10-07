# 10. Accessing APIs via Native Mobile Apps

우리는 기본 모바일 앱을 신뢰할 수 없거나 공개 클라이언트로 취급합니다. 자체 키 또는 자격 증명을 보호할 수 없는 클라이언트 앱은 OAuth 용어에서 공개 클라이언트로 식별됩니다. 기본 모바일 앱은 사용자가 소유한 장치에서 실행되기 때문에 모바일 장치에 대한 완전한 액세스 권한을 가진 사용자는 앱이 숨기고 있는 모든 키를 파악할 수 있습니다. 이것은 기본 모바일 앱에서 보안 API에 액세스할 때 직면하는 어려운 과제입니다.

이 장에서는 네이티브 앱에 OAuth 2.0을 사용하는 모범 사례인 `PKCE(Proof Key for Code Exchange)`를 설명합니다. 이는 코드 가로채기 공격으로부터 네이티브 앱을 보호하고 브라우저가 없는 환경에서 네이티브 앱을 보호하기 위한 접근 방식입니다.

## Mobile SSO

사용자가 앱에 로그인하는 데 평균 20초가 걸립니다. 사용자가 리소스에 액세스해야 할 때마다 암호를 입력하지 않아도 되므로 시간이 절약되고 사용자의 생산성이 향상되며 여러 로그인 이벤트와 잊어버린 암호로 인한 좌절감도 줄어듭니다. Single Sign-On이 있으면 사용자는 기억하고 업데이트할 암호가 하나만 있고 기억할 암호 규칙 집합은 하나만 있습니다. 초기 로그인을 통해 일반적으로 하루 또는 일주일 동안 모든 리소스에 액세스할 수 있습니다.

회사 직원이 모바일 장치에서 액세스할 수 있도록 여러 모바일 앱을 제공하는 경우 각 앱에 개별적으로 다시 로그인하도록 요청하는 것은 힘든 일입니다. 그들 모두가 동일한 자격 증명 저장소를 공유하고 있을 수 있습니다. 이는 Facebook 사용자가 Facebook 자격 증명을 사용하여 여러 타사 모바일 앱에 로그인하는 경우와 유사합니다. Facebook 로그인을 사용하면 Facebook에 한 번만 로그인하면 Facebook 로그인에 의존하는 다른 앱에 자동으로 로그인됩니다.

모바일에서 기본 앱 로그인은 사용자 자격 증명을 직접 요청하는 방법, `WebView`를 사용하는 방법, 시스템 브라우저를 사용하는 방법의 세 가지 방법으로 수행됩니다.

#### Login with Direct Credentials

이 방식을 사용하면 사용자가 기본 앱 자체에 자격 증명을 직접 제공합니다(그림 10-1 참조). 그리고 앱은 API(또는 OAuth 2.0 비밀번호 부여 유형)를 사용하여 사용자를 인증합니다. 이 접근 방식은 기본 앱을 신뢰할 수 있다고 가정합니다. 

기본 앱이 로그인을 위해 타사 ID 공급자를 사용하는 경우 이를 사용해서는 안 됩니다. 타사 ID 공급자가 로그인 API를 제공하거나 OAuth 2.0 암호 부여 유형을 지원하지 않는 한 이 접근 방식조차 불가능할 수 있습니다. 또한 이 접근 방식은 사용자를 피싱 공격에 취약하게 만들 수 있습니다. 공격자는 사용자가 원래 앱과 모양과 느낌이 같은 기본 앱을 설치하도록 속이고 사용자가 자신의 자격 증명을 공유하도록 속여서 피싱 공격을 가할 수 있습니다. 

이러한 위험 외에도 직접 자격 증명을 사용한 로그인은 기본 앱이 여러 개인 경우 SSO 환경을 구축하는 데 도움이 되지 않습니다. 각 개별 앱에 로그인하려면 자격 증명을 사용해야 합니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_10_Fig1_HTML.jpg)
Figure 10-1 The Chase bank’s app, which users directly provide credentials for login

### Login with WebView

기본 앱 개발자는 기본 앱에서 WebView를 사용하여 브라우저를 포함하므로 앱이 HTML, JavaScript 및 CSS와 같은 웹 기술을 사용할 수 있습니다. 로그인 흐름 동안 기본 앱은 시스템 브라우저를 WebView로 로드하고 HTTP 리디렉션을 사용하여 사용자를 해당 ID 공급자로 가져옵니다. 예를 들어 Facebook으로 사용자를 기본 앱에 인증하려면 먼저 시스템 브라우저를 WebView에 로드한 다음 사용자를 Facebook으로 리디렉션합니다. WebView에 로드된 브라우저에서 일어나는 일은 브라우저를 사용하여 Facebook을 통해 웹 앱에 로그인할 때 표시되는 흐름과 다르지 않습니다.

WebView 기반 접근 방식은 더 나은 사용자 경험을 제공하기 때문에 하이브리드 네이티브 앱을 구축하는 데 널리 사용되었습니다. 사용자는 브라우저가 WebView에 로드되는 것을 눈치채지 못할 것입니다. 모든 것이 동일한 기본 앱에서 일어나는 것처럼 보입니다.

또한 몇 가지 주요 단점이 있습니다. 기본 앱의 WebView에 로드된 브라우저 아래의 웹 세션은 여러 기본 앱 간에 공유되지 않습니다. 예를 들어, WebView에 로드된 브라우저를 통해 사용자를 facebook.com으로 리디렉션하여 하나의 기본 앱에 Facebook으로 로그인하는 경우 여러 기본 앱이 동일한 접근 방식을 따를 때 사용자는 Facebook에 계속해서 로그인해야 합니다. 한 WebView에서 facebook.com 아래에 생성된 웹 세션은 다른 기본 앱의 다른 WebView와 공유되지 않기 때문입니다. 따라서 기본 앱 간의 SSO는 WebView 접근 방식에서 작동하지 않습니다.

WebView 기반 기본 앱은 또한 사용자를 피싱 공격에 더 취약하게 만듭니다. 이전에 논의한 것과 동일한 예에서 사용자가 WebView에 로드된 시스템 브라우저를 통해 facebook.com으로 리디렉션되면 사용자는 기본 앱 외부에 있는 항목을 방문하는지 여부를 파악할 수 없습니다. 따라서 기본 앱 개발자는 facebook.com과 매우 유사한 것을 제공하여 사용자를 속이고 사용자의 Facebook 자격 증명을 훔칠 수 있습니다. 이러한 이유로 대부분의 개발자는 이제 로그인을 위해 WebView를 사용하지 않고 있습니다.

### 시스템 브라우저로 로그인

기본 앱에 로그인하기 위한 이 접근 방식은 이전 섹션에서 논의한 것과 유사하지만 WebView 대신 기본 앱이 시스템 브라우저를 실행합니다(그림 10-2 참조). 시스템 브라우저 자체는 또 다른 기본 앱입니다. 이 접근 방식의 사용자 경험은 사용자가 로그인 프로세스 동안 두 개의 기본 앱 사이를 전환해야 하므로 WebView 접근 방식만큼 원활하지 않지만 보안 측면에서는 이것이 최선의 접근 방식입니다. 또한 모바일 환경에서 싱글 사인온을 경험할 수 있는 유일한 방법입니다. WebView 접근 방식과 달리 시스템 브라우저를 사용할 때 사용자에 대한 단일 웹 세션을 관리합니다. 예를 들어 동일한 시스템 브라우저를 통해 Facebook 로그인을 사용하는 여러 기본 앱이 있는 경우 사용자는 Facebook에 한 번만 로그인하면 됩니다. 시스템 브라우저를 사용하여 facebook.com 도메인 아래에 웹 세션이 생성되면 다른 기본 앱의 후속 로그인 요청에 대해 사용자가 자동으로 로그인됩니다. 다음 섹션에서는 OAuth 2.0을 안전하게 사용하여 이 사용 사례를 구축하는 방법을 살펴봅니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_10_Fig2_HTML.jpg)
Figure 10-2 Login to Foursquare native app using Facebook

## Using OAuth 2.0 in Native Mobile Apps

OAuth 2.0은 모바일 앱 인증을 위한 사실상의 표준이 되었습니다. 우리의 보안 설계에서는 기본 앱을 멍청한 앱으로 취급해야 합니다. 단일 페이지 앱과 매우 유사합니다. 다음은 OAuth 2.0을 사용하여 기본 모바일 앱에 로그인할 때 발생하는 일련의 이벤트 목록입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_10_Fig3_HTML.jpg)

Figure 10-3 A typical login flow for a native mobile app with OAuth 2.0

1. 모바일 앱 개발자는 해당 ID 제공자 또는 OAuth 2.0 인증 서버에 앱을 등록하고 `client_id`를 얻어야 합니다. 클라이언트 암호 없이 OAuth 2.0 인증 코드 부여 유형을 사용하는 것이 좋습니다. 네이티브 앱은 신뢰할 수 없는 클라이언트이므로 클라이언트 비밀을 가질 필요가 없습니다. 일부는 기본 앱에 대한 암시적 부여 유형을 사용했지만 고유한 보안 문제가 있어 더 이상 권장되지 않습니다.

2. WebView 대신 iOS9+에서 `SFSafariViewController`를 사용하거나 Android용 Chrome 사용자 지정 탭을 사용합니다. 이 웹 컨트롤러는 앱 내에 배치할 수 있는 컨트롤에서 기본 시스템 브라우저의 모든 이점을 제공합니다. 그런 다음 1단계에서 얻은 `client_id`를 앱에 포함할 수 있습니다. `client_id`를 앱에 포함하면 해당 기본 앱의 모든 인스턴스에 대해 동일합니다. 앱의 각 인스턴스(다른 장치에 설치됨)를 구별하려는 경우 OAuth 2.0 동적 클라이언트 등록 프로필에 정의된 프로토콜에 따라 앱 시작 시 각 인스턴스에 대한 `client_id`를 동적으로 생성할 수 있습니다. 9장에서 자세히 설명합니다.

3. 앱을 설치하는 동안 모바일 운영 체제에 앱별 사용자 지정 URL 체계를 등록해야 합니다. 이 URL 체계는 앱 등록 시 1단계에서 사용한 콜백 URL 또는 리디렉션 URI와 일치해야 합니다. 사용자 지정 URL 체계를 사용하면 모바일 운영 체제가 시스템 브라우저와 같은 다른 외부 앱에서 앱으로 컨트롤을 다시 전달할 수 있습니다. 일부 매개변수를 브라우저의 앱별 사용자 정의 URI 체계에 보내면 모바일 운영 체제가 이를 추적하고 해당 매개변수를 사용하여 해당하는 기본 앱을 호출합니다.

4. 사용자가 로그인을 클릭하면 기본 앱에서 시스템 브라우저를 실행하고 4장에서 자세히 설명한 OAuth 2.0 인증 코드 부여 유형(그림 10-3 참조)에 정의된 프로토콜을 따라야 합니다.

5. 사용자가 `ID 공급자`에 대해 인증한 후 브라우저는 사용자를 등록된 리디렉션 URI로 다시 리디렉션합니다. 이 URI는 실제로 모바일 운영 체제에 등록된 사용자 지정 URL 체계입니다.

6. 시스템 브라우저에서 사용자 정의 URL 체계에 대한 인증 코드를 수신하면 모바일 운영 체제는 해당 기본 앱을 실행하고 제어를 전달합니다.

7. 네이티브 앱은 인증 서버의 토큰 엔드포인트와 통신하고 인증 코드를 액세스 토큰으로 교환합니다.

8. 기본 앱은 액세스 토큰을 사용하여 API에 액세스합니다.

### Inter-app Communication

시스템 브라우저 자체는 또 다른 기본 앱입니다. 인증 서버에서 인증 코드를 수신하기 위해 앱 간 통신 방법으로 사용자 지정 URL 체계를 사용했습니다. 모바일 환경에서 사용할 수 있는 앱 간 통신에는 개인용 URI 체계(사용자 지정 URL 체계라고도 함), 요청된 HTTPS URL 체계 및 루프백 URI 체계와 같은 여러 가지 방법이 있습니다.

#### 개인 URI 체계

이전 섹션에서 개인 URI 체계가 작동하는 방식에 대해 이미 논의했습니다. 브라우저가 개인 URI 체계를 사용하면 해당 URI 체계에 등록된 해당 기본 앱을 호출하고 제어를 넘겨줍니다. RFC 75951은 URI 스킴에 대한 가이드라인과 등록 절차를 정의하고 있으며 이에 따라 개인 URI 스킴과 역순으로 귀하가 제어하는 ​​도메인 이름을 사용하는 것이 좋습니다. 예를 들어 `app.foo.com`을 소유하고 있다면 개인 URI 체계는 `com.foo.app`이어야 합니다. 완전한 개인 URI 체계는 `com.foo.app:/oauth2/redirect`와 같을 수 있으며 체계 구성 요소 바로 뒤에 나타나는 슬래시가 하나만 있습니다.

동일한 모바일 환경에서 private URI 방식은 서로 충돌할 수 있습니다. 예를 들어 동일한 URI 체계에 대해 두 개의 앱이 등록될 수 있습니다. 이상적으로는 식별자를 선택하는 동안 이전에 논의한 규칙을 따른다면 이런 일이 발생하지 않아야 합니다. 그러나 여전히 공격자가 이 기술을 사용하여 코드 가로채기 공격을 수행할 수 있는 기회가 있습니다. 이러한 공격을 방지하려면 개인 URI 체계와 함께 `PKCE(Proof Key for Code Exchange)`를 사용해야 합니다. 이후 섹션에서 `PKCE`에 대해 설명합니다.

#### 요청된 HTTPS URI 체계

이전 섹션에서 논의한 개인 URI 체계와 마찬가지로 브라우저는 요청된 HTTPS URI 체계를 볼 때 해당 페이지를 로드하는 대신 해당 기본 앱에 제어를 넘깁니다. 지원되는 모바일 운영 체제에서는 제어할 수 있는 HTTPS 도메인을 요청할 수 있습니다. 요청된 전체 HTTPS URI 체계는 https://app.foo.com/oauth2/redirect와 같습니다. private URI 방식과 달리 브라우저는 리디렉션 전에 요청된 HTTPS URI의 ID를 확인하고 같은 이유로 가능한 경우 다른 것보다 요청된 HTTPS URI 스키마를 사용하는 것이 좋습니다.

#### Loopback Interface

With this approach, your native app will listen on a given port in the device itself. In other words, your native app acts as a simple web server. For example, your redirect URI will look like http://127.0.0.1:5000/oauth2/redirect. Since we are using the loopback interface (127.0.0.1), when the browser sees this URL, it will hand over the control to the service listening on the mobile device on port 5000. The challenge with this approach is that your app may not be able to run on the same port on all the devices, if there are any other apps on the mobile device already using the same port.

#### Proof Key for Code Exchange (PKCE)

`PKCE(Proof Key for Code Exchange)`는 모바일 환경에서 코드 가로채기 공격(자세한 내용은 14장 참조)을 완화하기 위한 방법으로 RFC 7636에 정의되어 있습니다. 이전 섹션에서 논의한 바와 같이 사용자 정의 URL 체계를 사용하여 OAuth 인증 서버에서 인증 코드를 검색할 때 다른 앱으로 이동하는 경우가 있을 수 있습니다. 사용자 정의 URL 체계를 원래 앱으로 사용합니다. 공격자는 코드를 훔칠 의도로 이 작업을 수행할 수 있습니다.

인증 코드가 잘못된 앱에 도달하면 액세스 토큰으로 교환한 다음 해당 API에 액세스할 수 있습니다. 모바일 환경에서는 클라이언트 시크릿이 없는 인증 코드를 사용하고, 원본 앱의 클라이언트 ID는 공개되어 있으므로 공격자는 인증 서버의 토큰 엔드포인트와 대화하여 액세스 토큰으로 코드를 교환하는 데 문제가 없습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_10_Fig4_HTML.jpg)

Figure 10-4 A typical login flow for a native mobile app with OAuth 2.0 and PKCE

PKCE가 코드 가로채기 공격을 어떻게 해결하는지 봅시다(그림 10-4 참조):

1. 기본 모바일 앱은 사용자를 인증 서버로 리디렉션하기 전에 `code_verifier`라고 하는 임의의 값을 생성합니다. code_verifier 값의 최소 길이는 43자, 최대 128자여야 합니다.

2. 다음으로 앱은 `code_verifie`r의 SHA256을 계산하고 패딩 없이 `base64-url-encoded`(부록 E 참조) 표현을 찾아야 합니다. SHA256 해싱 알고리즘은 항상 256비트의 해시를 생성하므로 `base64-url-encode`할 때 `=` 기호로 표시되는 항상 패딩이 있습니다. PKCE RFC에 따르면 해당 패딩을 제거해야 하며 SHA256 해시, `base64-url` 인코딩, 패딩되지 않은 `code_verifier`인 해당 값을 `code_challenge`라고 합니다.

3. 이제 네이티브 앱이 인증 코드 요청을 시작하고 사용자를 인증 서버로 리디렉션할 때 `code_challenge` 및 `code_challenge_method` 쿼리 매개변수와 함께 다음과 같은 방식으로 요청 URL을 구성해야 합니다. code_challenge_method는 해싱 알고리즘의 이름을 전달합니다.

https://idp.foo.com/authorization?client_id=FFGFGOIPI7898778&scopeopenid&redirect_uri=com.foo.app:/oauth2/redirect&response_type=code&code_challenge=YzfcdAoRg7rAfj9_Fllh7XZ6BBl4PIHC-xoMrfqvWUc&code_challenge_method=S256"

4. 인증 서버는 인증 코드 발급 시 발급된 인증 코드에 대해 제공된 `code_challenge`를 기록해야 합니다. 일부 인증 서버는 `code_challenge`를 코드 자체에 포함할 수 있습니다.

5. 네이티브 앱이 인증 코드를 받으면 인증 서버의 토큰 끝점과 통신하여 코드를 액세스 토큰으로 교환할 수 있습니다. 그러나 PKCE를 따를 때는 토큰 요청과 함께 `code_verifier`(`code_challenge`에 해당)를 보내야 합니다.
 
```
curl -k --user "XDFHKKJURJSHJD" -d "code=XDFHKKJURJSHJD&grant_type=authorization_code&client_id=FFGFGOIPI7898778 &redirect_uri=com.foo.app:/oauth2/redirect&code_verifier=ewewewoiuojslkdjsd9sadoidjalskdjsdsdewewewoiuojslkdjsd9sadoidjalskdjsdsd" https://idp.foo.com/token
```

6. 공격자의 앱이 인증 코드를 받으면 원래 앱만 code_verifier를 알고 있기 때문에 여전히 액세스 토큰으로 교환할 수 없습니다.

7. 인증 서버가 토큰 요청과 함께 code_verifier를 수신하면 SHA256 해시, base64 URL 인코딩, 패딩되지 않은 값을 찾아 기록된 code_challenge와 비교합니다. 이 두 가지가 일치하면 액세스 토큰을 발급합니다


## Browser-less Apps

지금까지 이 장에서는 웹 브라우저를 실행할 수 있는 모바일 장치에 대해서만 논의했습니다. 스마트 TV, 스마트 스피커, 프린터 등과 같이 입력 제약이 있고 웹 브라우저가 없는 장치에서 실행되는 앱에서 OAuth 보안 API를 사용해야 하는 또 다른 요구 사항이 증가하고 있습니다. 이 섹션에서는 OAuth 2.0 장치 권한 부여를 사용하여 브라우저 없는 앱에서 OAuth 2.0 보호 API에 액세스하는 방법에 대해 설명합니다. 어떤 경우에도 장치 권한 부여는 지원되는 모바일 장치에서 실행되는 기본 앱과 관련하여 앞서 논의한 접근 방식을 대체하지 않습니다.

### OAuth 2.0 Device Authorization Grant

OAuth 2.0 장치 권한 부여 grant2는 IETF OAuth 작업 그룹에서 게시한 RFC 8628입니다. 이 RFC에 따르면 장치 권한 부여 유형을 사용하는 장치는 다음 요구 사항을 충족해야 합니다.

- 장치가 이미 인터넷 또는 네트워크에 연결되어 있으며 인증 서버에 액세스할 수 있습니다.
- 장치가 아웃바운드 HTTPS 요청을 할 수 있습니다.
- 장치는 URI 및 코드 시퀀스를 사용자에게 표시하거나 달리 통신할 수 있습니다.
- 사용자는 요청을 처리할 수 있는 보조 장치(예: 개인용 컴퓨터 또는 스마트폰)를 가지고 있습니다.

예를 들어 장치 권한 부여가 작동하는 방식을 살펴보겠습니다. 스마트 TV에서 실행 중인 YouTube 앱이 있고 우리를 대신하여 YouTube 계정에 액세스하려면 스마트 TV가 필요하다고 가정해 보겠습니다. 이 경우 YouTube는 OAuth 인증 서버와 리소스 서버 역할을 모두 하며 스마트 TV에서 실행되는 YouTube 앱은 OAuth 클라이언트 앱입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_10_Fig5_HTML.jpg)

Figure 10-5 A typical login flow for a browser-less app with OAuth 2.0

1. 1The user takes the TV remote and clicks the YouTube app to associate his/her YouTube account with the app.

2. The YouTube app running on the smart TV has an embedded client ID and sends a direct HTTP request over HTTPS to the authorization server.
```
POST /device_authorization HTTP/1.1
Host: idp.youtube.com
Content-Type: application/x-www-form-urlencoded
client_id=XDFHKKJURJSHJD
```

3. 이전 요청에 대한 응답으로 인증 서버는 device_code, user_code 및 확인 URI를 다시 반환합니다. device_code와 user_code에는 모두 만료 시간이 관련되어 있으며, 이 만료 시간은 expires_in 매개변수(초 단위)를 통해 클라이언트 앱에 전달됩니다.
```
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
```

4. YouTube 클라이언트 앱은 제공된 확인 URI(이전 응답에서)를 방문하고 제공된 사용자 코드(이전 응답에서)로 승인 요청을 확인하도록 사용자에게 지시합니다.

5. 이제 사용자는 인증 URI를 방문하기 위해 보조 장치(노트북 또는 휴대폰)를 사용해야 합니다. 해당 작업이 진행되는 동안 YouTube 앱은 인증 서버를 계속 폴링하여 사용자가 인증 요청을 확인했는지 확인합니다. 클라이언트가 폴링 전에 대기해야 하는 최소 시간 또는 폴링 사이의 시간은 간격 매개변수 아래의 이전 응답에서 권한 부여 서버에 의해 지정됩니다. 권한 부여 서버의 토큰 끝점에 대한 폴 요청에는 세 개의 매개변수가 포함됩니다. grant_type 매개변수는 urn:ietf:params:oauth:grant-type:device_code 값을 전달해야 하므로 권한 부여 서버가 이 요청을 처리하는 방법을 알 수 있습니다. device_code 매개변수는 첫 번째 응답에서 인증 서버에서 발행한 기기 코드를 전달하고 client_id 매개변수는 YouTube 앱의 클라이언트 식별자를 전달합니다.

```
POST /token HTTP/1.1
Host: idp.youtube.com
Content-Type: application/x-www-form-urlencoded
grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code
&device_code=GmRhmhcxhwAzkoEqiMEg_DnyEysNkuNhszIySk9eS
&client_id=459691054427
```

6. 사용자는 제공된 검증 URI를 방문하여 사용자 코드를 입력하고 승인 요청을 확인합니다.

7. 사용자가 권한 부여 요청을 확인하면 권한 부여 서버는 5단계의 요청에 대해 다음 응답을 발행합니다. 이것은 OAuth 2.0 권한 부여 서버 토큰 끝점의 표준 응답입니다.

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
 
8. Now the YouTube app can use this access token to access the YouTube API on behalf of the user.


## 요약

- OAuth 2.0에는 여러 권한 부여 유형이 있습니다. 그러나 OAuth 2.0을 사용하여 네이티브 모바일 앱에서 API에 액세스하는 동안 PKCE(Proof Key for Code Exchange)와 함께 인증 코드 부여 유형을 사용하는 것이 좋습니다.

- PKCE는 코드 가로채기 공격으로부터 네이티브 앱을 보호합니다.

- 스마트 TV, 스마트 스피커, 프린터 등 브라우저가 없는 기기의 사용이 대중화되고 있습니다.

- OAuth 2.0 장치 권한 부여는 브라우저가 없는 장치에서 OAuth 2.0을 사용하고 API에 액세스하기 위한 표준 흐름을 정의합니다.