# 15. Patterns and Practices

지금까지 14개 장과 7개 부록이 넘는 이 책을 통해 우리는 API를 보호하는 다양한 방법과 그 이면의 이론적 배경에 대해 논의했습니다. 이 장에서는 가장 일반적인 엔터프라이즈 보안 문제를 해결하기 위한 API 보안 패턴 세트를 제시합니다.

## 신뢰할 수 있는 하위 시스템을 통한 직접 인증

중견 기업에 여러 API가 있다고 가정합니다. 회사 직원은 회사 방화벽 뒤에 있는 동안 웹 앱을 통해 이러한 API에 액세스할 수 있습니다. 모든 사용자 데이터는 Microsoft Active Directory(AD)에 저장되며 웹 앱은 Active Directory에 직접 연결되어 사용자를 인증합니다. 웹 앱은 로그인한 사용자의 식별자를 백엔드 API에 전달하여 사용자와 관련된 데이터를 검색합니다.

문제는 간단하며 그림 15-1은 해결책을 보여줍니다. 일종의 직접 인증 패턴을 사용해야 합니다. 사용자 인증은 프런트 엔드 웹 앱에서 이루어지며 사용자가 인증되면 웹 앱은 백엔드 API에 액세스해야 합니다. 여기서 안 것은 웹 앱이 로그인한 사용자의 식별자를 API에 전달한다는 것입니다. 이는 웹 앱이 사용자 인식 방식으로 API를 호출해야 함을 의미합니다.

웹 앱과 API 모두 동일한 신뢰 도메인에 있으므로 웹 앱에서 최종 사용자만 인증하고 백엔드 API는 웹 앱에서 전달된 데이터를 신뢰합니다. 이를 신뢰할 수 있는 하위 시스템 패턴이라고 합니다. 웹 응용 프로그램은 신뢰할 수 있는 하위 시스템 역할을 합니다. 이러한 경우 API를 보호하는 가장 좋은 방법은 `mTLS`(상호 전송 계층 보안)를 사용하는 것입니다. 웹 앱에서 생성된 모든 요청은 `mTLS`로 보호되며 웹 앱 외에는 누구도 API에 액세스할 수 없습니다(3장 참조).
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_15_Fig1_HTML.jpg)
Figure 15-1 Direct authentication with the trusted subsystem pattern

일부는 추가되는 오버헤드로 인해 TLS 사용을 거부하고 웹 앱과 API를 호스팅하는 컨테이너 간의 보안이 네트워크 수준에서 관리되는 제어된 환경 구축에 의존합니다. 네트워크 수준 보안은 웹 응용 프로그램 서버 이외의 구성 요소가 API를 호스팅하는 컨테이너와 통신할 수 없다는 보증을 제공해야 합니다. 

이것을 `네트워크 신뢰` 패턴이라고 하며, 시간이 지나면서 안티패턴이 되었습니다. `네트워크 신뢰` 패턴의 반대는 `제로 트러스트 네트워크`입니다. `제로 트러스트 네트워크` 패턴을 사용하면 네트워크를 신뢰하지 않습니다. 네트워크를 신뢰할 수 없는 경우 리소스(또는 우리의 경우 API)에 최대한 가깝게 보안 검사를 시행했는지 확인해야 합니다. `mTLS`를 사용하여 API를 보호하는 것이 여기에서 가장 이상적인 솔루션입니다.


## SSO with the Delegated Access Control

중견 기업에 여러 API가 있다고 가정합니다. 회사 직원은 회사 방화벽 뒤에 있는 동안 웹 앱을 통해 이러한 API에 액세스할 수 있습니다. 모든 사용자 데이터는 Microsoft Active Directory에 저장되고 모든 웹 앱은 사용자를 인증하기 위해 `SAML 2.0`을 지원하는 ID 공급자에 연결됩니다. 웹 앱은 로그인한 사용자를 대신하여 백엔드 API에 액세스해야 합니다.

여기에서 캐치는 마지막 문장입니다. "웹 앱은 로그인한 사용자를 대신하여 백엔드 API에 액세스해야 합니다." 이는 액세스 위임 프로토콜인 OAuth 2.0이 필요함을 나타냅니다. 그러나 사용자는 자신의 자격 증명을 웹 앱에 직접 제시하지 않고 SAML 2.0 자격 증명 공급자를 통해 인증합니다.

이 경우 웹 앱이` SAML 2.0` 웹 SSO 프로토콜을 통해 수신하는 SAML 토큰을 OAuth 2.0 사양에 대한 SAML 부여 유형에 정의된 OAuth 액세스 토큰에 대해 교환하는 방법을 찾아야 합니다(12장 참조). 그림 15-2의 3단계와 같이 웹 앱이 SAML 토큰을 수신하면 OAuth 2.0 인증 서버와 통신하여 SAML 토큰을 액세스 토큰으로 교환해야 합니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_15_Fig2_HTML.jpg)
Figure 15-2 Single sign-on with the Delegated Access Control pattern

권한 부여 서버는 `SAML 2.0` ID 제공자를 신뢰해야 합니다. 웹 앱이 액세스 토큰을 받으면 이를 사용하여 백엔드 API에 액세스할 수 있습니다. OAuth 2.0에 대한 SAML 부여 유형은 리프레시 토큰을 제공하지 않습니다. OAuth 2.0 권한 부여 서버에서 발급한 액세스 토큰의 수명은 권한 부여에 사용된 SAML 토큰의 수명과 일치해야 합니다.

사용자가 유효한 SAML 토큰으로 웹 앱에 로그인한 후 웹 앱은 그 이후로 사용자를 위한 세션을 생성하고 SAML 토큰의 수명에 대해 걱정하지 않습니다. 이로 인해 몇 가지 문제가 발생할 수 있습니다. 예를 들어, SAML 토큰이 만료되었지만 사용자는 웹 앱에서 여전히 유효한 브라우저 세션을 가지고 있습니다. SAML 토큰이 만료되었으므로 사용자 로그인 시 획득한 해당 OAuth 2.0 액세스 토큰도 만료되었음을 예상할 수 있습니다. 이제 웹 앱이 백엔드 API에 액세스하려고 하면 액세스 토큰이 만료되어 요청이 거부됩니다. 이러한 시나리오에서 웹 앱은 사용자를 다시 SAML 2.0 자격 증명 공급자로 리디렉션하고 새 SAML 토큰을 가져와 해당 토큰을 새 액세스 토큰으로 교환해야 합니다. SAML 2.0 ID 공급자의 세션이 아직 활성 상태인 경우 이 리디렉션을 최종 사용자에게 투명하게 만들 수 있습니다.

## 통합 Windows 인증을 통한 SSO

다수의 API가 있는 중견기업을 가정해 보겠습니다. 회사 직원은 회사 방화벽 뒤에 있는 동안 여러 웹 앱을 통해 이러한 API에 액세스할 수 있습니다. 모든 사용자 데이터는 Microsoft Active Directory에 저장되며 모든 웹 응용 프로그램은 사용자를 인증하기 위해 SAML 2.0 ID 공급자에 연결됩니다. 웹 앱은 로그인한 사용자를 대신하여 백엔드 API에 액세스해야 합니다. 모든 사용자는 Windows 도메인에 있으며 일단 워크스테이션에 로그인하면 다른 응용 프로그램에 대한 자격 증명을 제공하라는 요청을 받아서는 안 됩니다.

여기에서 캐치는 "모든 사용자는 Windows 도메인에 있으며 일단 워크스테이션에 로그인하면 다른 응용 프로그램에 대한 자격 증명을 제공하라는 요청을 받아서는 안 됩니다."라는 문구가 있습니다.

위임된 액세스 제어 패턴(두 번째 패턴)으로 SSO을 사용하여 제공한 솔루션을 확장해야 합니다. 이 경우 사용자는 Active Directory 사용자 이름과 암호를 사용하여 SAML 2.0 ID 공급자에 로그인합니다. 여기에서는 허용되지 않습니다. 대신 IWA(Windows 통합 인증)를 사용하여 SAML 2.0 ID 공급자를 보호할 수 있습니다. IWA를 사용하도록 SAML 2.0 ID 공급자를 구성하면 사용자가 인증을 위해 ID 공급자로 리디렉션되면 사용자가 자동으로 인증됩니다. 위임된 액세스 제어 패턴이 있는 SSO의 경우와 마찬가지로 SAML 응답이 웹 앱에 전달됩니다. 나머지 흐름은 변경되지 않습니다.

## 위임된 액세스 제어가 있는 ID 프록시

중견 기업에 여러 API가 있다고 가정합니다. 회사 직원과 신뢰할 수 있는 파트너의 직원은 웹 앱을 통해 이러한 API에 액세스할 수 있습니다. 모든 내부 사용자 데이터는 Microsoft Active Directory에 저장되며 모든 웹 응용 프로그램은 SAML 2.0 ID 공급자에 연결되어 사용자를 인증합니다. 웹 앱은 로그인한 사용자를 대신하여 백엔드 API에 액세스해야 합니다.

 ![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_15_Fig3_HTML.jpg)

그림 15-3 위임된 액세스 제어 패턴을 사용하는 ID 프록시

이 사례는 위임된 액세스 제어 패턴과 함께 SSO를 사용하는 확장입니다. 여기서 캐치는 "회사 직원뿐만 아니라 신뢰할 수 있는 파트너의 직원도 웹 응용 프로그램을 통해 이러한 API에 액세스할 수 있습니다."라는 문구입니다. 이제 회사 영역을 넘어서야 합니다. 그림 15-2의 모든 내용은 변경되지 않은 상태로 유지됩니다. SAML 2.0 ID 공급자에서 인증 메커니즘을 변경하기만 하면 됩니다(그림 15-3 참조).

최종 사용자의 도메인에 관계없이 클라이언트 웹 앱은 자체 도메인의 ID 공급자만 신뢰합니다. 내부 및 외부 사용자는 먼저 내부(또는 로컬) SAML ID 공급자로 리디렉션됩니다. 로컬 ID 공급자는 사용자에게 사용자 이름과 비밀번호(내부 사용자의 경우)로 인증할지 또는 해당 도메인을 선택할지 선택할 수 있는 옵션을 제공해야 합니다. 그런 다음 ID 제공자는 사용자를 외부 사용자의 홈 도메인에서 실행되는 해당 ID 제공자로 리디렉션할 수 있습니다. 이제 외부 ID 제공자가 내부 ID 제공자에게 SAML 응답을 반환합니다.

외부 ID 공급자가 이 SAML 토큰에 서명합니다. 서명이 유효하고 신뢰할 수 있는 외부 ID 공급자의 서명인 경우 내부 ID 공급자는 자체적으로 서명된 새 SAML 토큰을 호출 응용 프로그램에 발급합니다. 그런 다음 그림 15-2와 같이 흐름이 계속됩니다.

> **Note**
>
> 이 접근 방식의 한 가지 이점은 내부 앱이 자체 ID 공급자만 신뢰하면 된다는 것입니다. ID 공급자는 도메인 외부의 다른 ID 공급자 간의 신뢰 중개를 처리합니다. 이 시나리오에서 외부 ID 공급자도 SAML을 사용하지만 항상 예상할 수는 없습니다. 다른 프로토콜을 지원하는 ID 공급자도 있습니다. 이러한 시나리오에서 내부 ID 공급자는 서로 다른 프로토콜 간에 ID 어설션을 변환할 수 있어야 합니다.

### JSON 웹 토큰을 사용한 위임된 액세스 제어

다수의 API가 있는 중견기업을 가정해 보겠습니다. 회사 직원은 회사 방화벽 뒤에 있는 동안 웹 앱을 통해 이러한 API에 액세스할 수 있습니다. 모든 사용자 데이터는 Microsoft Active Directory에 저장되며 모든 웹 응용 프로그램은 OpenID Connect ID 공급자에 연결되어 사용자를 인증합니다. 웹 앱은 로그인한 사용자를 대신하여 백엔드 API에 액세스해야 합니다.

이 사용 사례는 위임된 액세스 제어 패턴을 사용하는 SSO의 확장이기도 합니다. 여기에서 캐치는 "모든 웹 응용 프로그램은 사용자를 인증하기 위해 `OpenID Connect ID 공급자`에 연결됩니다."라는 문구입니다. 그림 15-2에 표시된 SAML ID 제공자를 그림 15-4에 표시된 대로 OpenID Connect ID 제공자로 교체해야 합니다. 이는 또한 액세스 위임 프로토콜(OAuth)이 필요함을 시사합니다.

그러나 이 경우 사용자는 자신의 자격 증명을 웹 앱에 직접 제시하지 않습니다. 대신 `OpenID Connect ID 공급자`를 통해 인증합니다. 따라서 OpenID Connect 인증에서 받은 ID 토큰을 OAuth 2.0 사양(12장)에 대한 JWT 부여 유형에 정의된 OAuth 액세스 토큰으로 교환하는 방법을 찾아야 합니다. 웹 앱이 3단계에서 JWT이기도 한 ID 토큰을 수신하면 OAuth 2.0 인증 서버와 통신하여 이를 액세스 토큰으로 교환해야 합니다. 권한 부여 서버는 OpenID Connect ID 제공자를 신뢰해야 합니다. 웹 앱이 액세스 토큰을 받으면 이를 사용하여 백엔드 API에 액세스할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_15_Fig3_HTML.jpg)

Figure 15-4 Delegated Access Control with the JWT pattern

> **Note**
>
> Why would someone exchange the ID token obtained in OpenID Connect for an access token when it directly gets an access token along with the ID token? This is not required when both the OpenID Connect server and the OAuth authorization server are the same. If they aren’t, you have to use the JWT Bearer grant type for OAuth 2.0 and exchange the ID token for an access token. The access token issuer must trust the OpenID Connect identity provider.

### Nonrepudiation with the JSON Web Signature

그림 15-5와 같이 중견 금융 기업이 모바일 앱을 통해 API를 고객에게 노출해야 한다고 가정합니다. 한 가지 주요 요구 사항은 모든 API 호출이 부인 방지를 지원해야 한다는 것입니다.

여기에서 캐치는 "모든 API 호출은 부인 방지를 지원해야 합니다."라는 문장입니다. API를 통해 신원을 증명하여 비즈니스 거래를 할 때 나중에 거부하거나 거부할 수 없어야 합니다. 부인할 수 없음을 보장하는 속성을 부인 방지라고 합니다. 기본적으로 한 번만 하면 영원히 소유할 수 있습니다(자세한 내용은 2장 참조).

부인 방지는 제3자가 언제든지 확인할 수 있는 위조 불가능한 방식으로 데이터의 출처와 무결성에 대한 증거를 제공해야 합니다. 트랜잭션이 시작되면 트랜잭션 무결성을 유지하고 향후 검증을 허용하기 위해 사용자 ID, 날짜, 시간 및 트랜잭션 세부 정보를 포함한 그 어떤 내용도 전송 중에 변경되어서는 안 됩니다. 부인 방지는 트랜잭션이 커밋되고 확인된 후 변경되지 않고 기록되도록 해야 합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_15_Fig5_HTML.jpg)

Figure 15-5 Nonrepudiation with the JSON Web Signature pattern

무단 수정을 방지하려면 로그를 보관하고 적절하게 보호해야 합니다. 거부 분쟁이 있을 때마다 다른 로그 또는 데이터와 함께 트랜잭션 로그를 검색하여 개시자, 날짜, 시간, 트랜잭션 내역 등을 확인할 수 있습니다. 부인 방지를 달성하는 방법은 서명을 통한 것입니다. 최종 사용자에게만 알려진 키는 각 메시지에 서명해야 합니다.

이 경우 금융 기관은 각 고객에게 자신이 관리하는 인증 기관에서 서명한 키 쌍을 발급해야 합니다. 개인 키가 아닌 해당하는 공개 인증서만 저장해야 합니다. 고객은 자신의 모바일 장치에 개인 키를 설치하고 모바일 앱에서 사용할 수 있도록 할 수 있습니다. 모바일 앱에서 생성된 모든 API 호출은 사용자의 개인 키로 서명되고 금융 기관의 공개 키로 암호화되어야 합니다.

메시지에 서명하기 위해 모바일 앱은 JSON 웹 서명을 사용할 수 있습니다(7장 참조). 암호화를 위해 JSON 웹 암호화를 사용할 수 있습니다(8장 참조). 동일한 페이로드에서 서명과 암호화를 모두 사용하는 경우 메시지에 먼저 서명한 다음 서명된 페이로드를 암호화하여 법적 승인을 받아야 합니다.

## Chained Access Delegation

생수를 판매하는 중소기업에 등록된 사용자가 소비하는 물의 양을 업데이트하는 데 사용할 수 있는 API(Water API)가 있다고 가정합니다. 등록된 모든 사용자는 모든 클라이언트 앱을 통해 API에 액세스할 수 있습니다. Android 앱, iOS 앱 또는 웹 앱일 수도 있습니다.

회사는 API만 제공합니다. 누구나 이를 사용할 클라이언트 앱을 개발할 수 있습니다. Water API의 모든 사용자 데이터는 Microsoft Active Directory에 저장됩니다. 클라이언트 앱 사용자에 대한 정보를 찾기 위해 API에 직접 액세스할 수 없어야 합니다. Water API에 등록된 사용자만 액세스할 수 있습니다. 이러한 사용자는 자신의 정보만 볼 수 있어야 합니다. 동시에 사용자가 업데이트할 때마다 Water API는 `MyHealth.org`에서 유지 관리되는 사용자의 의료 기록을 업데이트해야 합니다. 사용자는 또한 `MyHealth.org`에 개인 기록이 있으며 API(MyHealth API)도 노출됩니다. Water API는 MyHealth API를 호출하여 사용자를 대신하여 사용자 기록을 업데이트해야 합니다.

요약하면, 모바일 앱은 최종 사용자를 대신하여 Water API에 액세스한 다음 Water API는 최종 사용자를 대신하여 MyHealth API에 액세스해야 합니다. Water API와 MyHealth API는 두 개의 독립적인 도메인에 있습니다. 이는 액세스 위임 프로토콜이 필요함을 시사합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_15_Fig6_HTML.jpg)

Figure 15-6 Chained Access Delegation pattern

다시 말하지만, 여기에서 "Water API는 MyHealth.org에서 유지 관리되는 사용자의 의료 기록도 업데이트해야 합니다"라는 문구가 있습니다. 여기에는 두 가지 솔루션이 있습니다.

- 첫 번째 솔루션에서 최종 사용자는 MyHealth.org에서 Water API에 대한 액세스 토큰을 가져와야 하고(Water API는 OAuth 클라이언트 역할을 함), Water API는 사용자 이름에 대해 내부적으로 토큰을 저장해야 합니다. 사용자가 모바일 앱을 통해 Water API에 업데이트를 보낼 때마다 Water API는 먼저 자체 기록을 업데이트한 다음 최종 사용자에 해당하는 MyHealth 액세스 토큰을 찾아 MyHealth API에 액세스하는 데 사용합니다. 이 접근 방식을 사용하면 Water API는 MyHealth API 액세스 토큰을 저장하는 오버헤드가 있으며 필요할 때마다 액세스 토큰을 새로 고쳐야 합니다.

- 두 번째 솔루션은 그림 15-6에 설명되어 있습니다. OAuth 2.0 토큰 위임 프로필을 기반으로 구축되었습니다(9장 참조). 모바일 앱은 최종 사용자를 대신하여 Water API에 액세스하기 위해 유효한 액세스 토큰을 가지고 있어야 합니다. 3단계에서 Water API는 액세스 토큰의 유효성을 검사하기 위해 자체 인증 서버와 통신합니다. 그런 다음 4단계에서 Water API는 모바일 앱에서 얻은 액세스 토큰을 JWT 액세스 토큰으로 교환합니다. JWT 액세스 토큰은 의미 있는 데이터를 전달하는 특수 액세스 토큰이며 Water API 도메인의 인증 서버에서 서명합니다. JWT에는 최종 사용자의 로컬 식별자(Water API에 해당)와 MyHealth 도메인의 매핑된 식별자가 포함됩니다. 최종 사용자는 Water API 도메인에서 이 작업을 허용해야 합니다.

6단계에서 Water API는 JWT 액세스 토큰을 사용하여 MyHealth API에 액세스합니다. MyHealth API는 자체 인증 서버와 통신하여 JWT 액세스 토큰의 유효성을 검사합니다. 서명을 확인합니다. 그리고 신뢰할 수 있는 엔터티가 서명한 경우 액세스 토큰은 유효한 것으로 처리됩니다.

JWT에는 MyHealth 도메인의 매핑된 사용자 이름이 포함되어 있으므로 해당 로컬 사용자 레코드를 식별할 수 있습니다. 그러나 이는 보안 문제를 야기합니다. 사용자가 매핑된 MyHealth 식별자를 사용하여 Water API 도메인에서 프로필을 업데이트하도록 허용하면 모든 사용자 식별자에 매핑할 수 있으며 이는 보안 허점으로 이어집니다. 이를 방지하려면 계정 매핑 단계를 OpenID Connect 인증으로 보호해야 합니다. 사용자가 자신의 MyHealth 계정 식별자를 추가하려는 경우 Water API 도메인은 OpenID Connect 인증 흐름을 시작하고 해당 ID 토큰을 받습니다. 그런 다음 ID 토큰의 사용자 식별자로 계정 매핑이 수행됩니다.

## Trusted Master Access Delegation

다수의 API가 있는 대규모 기업을 가정해 보겠습니다. API는 서로 다른 부서에서 호스팅되며, 각 부서는 서로 다른 배포에서 공급업체 비호환성으로 인해 자체 OAuth 2.0 인증 서버를 실행합니다. 회사 직원은 소속 부서에 관계없이 회사 방화벽 뒤에 있는 동안 웹 앱을 통해 이러한 API에 액세스할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_15_Fig7_HTML.jpg)
Figure 15-7 Trusted Master Access Delegation pattern

All user data are stored in a centralized Active Directory, and all the web applications are connected to a centralized OAuth 2.0 authorization server (which also supports OpenID Connect) to authenticate users. The web applications need to access back-end APIs on behalf of the logged-in user. These APIs may come from different departments, each of which has its own authorization server. The company also has a centralized OAuth 2.0 authorization server, and an employee having an access token from the centralized authorization server must be able to access any API hosted in any department.

Once again, this is an extended version of using SSO with the Delegated Access Control pattern. You have a master OAuth 2.0 authorization server and a set of secondary authorization servers. An access token issued from the master authorization server should be good enough to access any of the APIs under the control of the secondary authorization servers. In other words, the access token returned to the web application, as shown in step 3 of Figure 15-7, should be good enough to access any of the APIs.

To make this possible, you need to make the access token self-contained. Ideally, you should make the access token a JWT with the iss (issuer) field. In step 4, the web application accesses the API using the access token; and in step 5, the API talks to its own authorization server to validate the token. The authorization server can look at the JWT header and find out whether it issued this token or if a different server issued it. If the master authorization server issued it, then the secondary authorization server can talk to the master authorization server’s OAuth introspection endpoint to find out more about the token. The introspection response specifies whether the token is active and identifies the scopes associated with the access token. Using the introspection response, the secondary authorization server can build an eXtensible Access Control Markup Language (XACML) request and call a XACML policy decision point (PDP). If the XACML response is evaluated to permit, then the web application can access the API. Then again XACML is a little too complex in defining access control policies, irrespective of how powerful it is. You can also check the Open Policy Agent (OPA) project, which has become quite popular recently in building fine-grained access control policies.

## Resource Security Token Service (STS) with the Delegated Access Control

Suppose a global organization has APIs and API clients are distributed across different regions. Each region operates independently from the others. Currently, both clients and APIs are nonsecured. You need to secure the APIs without making any changes either at the API or the client end.

The solution is based on a simple theory in software engineering: introducing a layer of indirection can solve any problem. You need to introduce two interceptors. One sits in the client region, and all the nonsecured messages generated from the client are intercepted. The other interceptor sits in the API region, and all the API requests are intercepted. No other component except this interceptor can access the APIs in a nonsecured manner.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_15_Fig8_HTML.jpg)
Figure 15-8 Resource STS with the Delegated Access Control pattern

This restriction can be enforced at the network level. Any request generated from outside has no path to the API other than through the API interceptor. Probably you deploy both API interceptor and the API in the same physical machine. You can also call this component a policy enforcement point (PEP) or API gateway. The PEP validates the security of all incoming API requests. The interceptor’s responsibility, sitting in the client region, is to add the necessary security parameters to the nonsecured messages generated from the client and to send it to the API. In this way, you can secure the API without making changes at either the client or the API end.

Still, you have a challenge. How do you secure the API at the API gateway? This is a cross-domain scenario, and the obvious choice is to use JWT grant type for OAuth 2.0. Figure 15-8 explains how the solution is implemented. Nonsecured requests from the client application are captured by the interceptor component in step 1. Then it has to talk to its own security token service (STS). In step 2, the interceptor uses a default user account to access the STS using OAuth 2.0 client credentials grant type. The STS authenticates the request and issues a self-contained access token (a JWT), having the STS in the API region as the audience of the token.

In step 3, the client-side interceptor authenticates to the STS at the API region with the JWT token and gets a new JWT token, following OAuth 2.0 Token Delegation profile, which we discussed in Chapter 9. The audience of the new JWT is the OAuth 2.0 authorization server running in the API region. Before issuing the new JWT, the STS at the API region must validate its signature and check whether a trusted entity has signed it.

To make this scenario happen, the STS in the API region must trust the STS on the client side. The OAuth 2.0 authorization server only trusts its own STS. That is why step 4 is required. Step 4 initiates the JWT grant type for OAuth 2.0, and the client interceptor exchanges the JWT issued by the STS of the API region for an access token. Then it uses that access token to access the API in step 5.

The PEP in the API region intercepts the request and calls the authorization server to validate the access token. If the token is valid, the PEP lets the request hit the API (step 7).

## Delegated Access Control with No Credentials over the Wire

Suppose a company wants to expose an API to its employees. However, user credentials must never go over the wire. This is a straightforward problem with an equally straightforward solution. Both OAuth 2.0 bearer tokens and HTTP Basic authentication take user credentials over the wire. Even though both these approaches use TLS for protection, still some companies worry about passing user credentials over communication channels—or in other words passing bearer tokens over the wire.

You have few options: use either HTTP Digest authentication or OAuth 2.0 MAC tokens (Appendix G). Using OAuth 2.0 MAC tokens is the better approach because the access token is generated for each API, and the user can also revoke the token if needed without changing the password. However, the OAuth 2.0 MAC token profile is not matured yet. The other approach is to use OAuth 2.0 with Token Binding, which we discussed in Chapter 11. Even though we use bearer tokens there, with Token Binding, we bind the token to the underneath TLS channel—so no one can export the token and use it somewhere else.

There are few more draft proposals discussed under the IETF OAuth working group to address this concern. The OAuth 2.0 Mutual-TLS Client Authentication and Certificate-Bound Access Tokens is one of them, available at https://tools.ietf.org/html/draft-ietf-oauth-mtls-17.

## Summary

- API security is an ever-evolving subject.

- More and more standards and specifications are popping up, and most of them are built around the core OAuth 2.0 specification.

- Security around JSON is another evolving area, and the IETF JOSE working group is currently working on it.

- It’s highly recommended that if you wish to continue beyond this book, you should keep an eye on the IETF OAuth working group, the IETF JOSE working group, the OpenID Foundation, and the Kantara Initiative.

 
