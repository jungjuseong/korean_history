# 12. Federating Access to APIs

Quocirca(분석가 및 연구 회사)가 수행한 연구 중 하나에 따르면 많은 기업에는 이제 내부 사용자보다 엔터프라이즈 애플리케이션과 상호 작용하는 외부 사용자가 더 많습니다. 유럽에서는 기업의 58%가 다른 회사 및/또는 소비자의 사용자와 직접 거래합니다. 영국에서만 그 수치가 65%입니다.

최근의 역사를 살펴보면 오늘날 대부분의 기업은 인수, 합병 및 파트너십을 통해 성장합니다. 딜로직(Dealogic)에 따르면 미국에서만 2013년 첫 9개월 동안 인수합병 규모는 총 8,651억 달러에 달했다. 이는 전년 동기 대비 39% 증가한 수치이며 2008년 이후 9개월 중 가장 높은 수치입니다. 이것이 API 보안에 의미하는 바는 무엇입니까? 국경을 넘어 여러 이기종 보안 시스템을 처리할 수 있는 능력이 필요합니다.

## Enabling Federation

API 보안의 맥락에서 `연합`은 고유한 ID 관리 시스템 또는 고유한 기업 전체에 사용자 ID를 전파하는 것입니다. 파트너에게 공개된 API가 있는 간단한 사용 사례부터 시작하겠습니다. 다른 파트너의 이 API에 대해 사용자를 어떻게 인증하시겠습니까? 이러한 사용자는 외부 파트너에 속하며 외부 파트너가 관리합니다. HTTP 기본 인증이 작동하지 않습니다. 외부 사용자의 자격 증명에 액세스할 수 없으며 동시에 파트너는 방화벽 외부의 LDAP 또는 데이터베이스 연결을 외부 당사자에게 노출하지 않습니다. 사용자 이름과 암호를 묻는 것은 연합 시나리오에서 작동하지 않습니다. OAuth 2.0이 작동합니까? OAuth로 보호되는 API에 액세스하려면 클라이언트가 API 소유자가 발급하거나 API가 신뢰하는 엔터티에서 발급한 액세스 토큰을 제시해야 합니다. 외부 당사자의 사용자는 먼저 API가 신뢰하는 OAuth 권한 부여 서버로 인증한 다음 액세스 토큰을 얻어야 합니다. 이상적으로는 API가 신뢰하는 권한 부여 서버가 API와 동일한 도메인에 있는 것입니다.

권한 부여 코드 부여 유형이나 암시적 부여 유형은 권한 부여 서버에서 사용자를 인증하는 방법을 요구하지 않습니다. 결정하는 것은 권한 부여 서버에 달려 있습니다. 사용자가 권한 부여 서버에 로컬인 경우 사용자 이름과 암호 또는 기타 직접 인증 프로토콜을 사용할 수 있습니다. 사용자가 외부 엔터티에서 온 경우 일종의 중개 인증을 사용해야 합니다

## Brokered Authentication

중개 인증을 사용하면 인증 시 로컬 인증 서버(API와 동일한 도메인에서 실행)가 외부 당사자의 모든 개별 사용자를 신뢰할 필요가 없습니다. 대신 지정된 파트너 도메인의 브로커를 신뢰할 수 있습니다(그림 12-1 참조). 각 파트너는 자신의 사용자를 인증(직접 인증을 통해 가능)한 다음 신뢰할 수 있고 신뢰할 수 있는 방식으로 인증 결정을 다시 로컬 OAuth 권한 부여 서버에 전달하는 역할을 하는 신뢰 브로커가 있어야 합니다. 실제로는 사용자(이 경우 파트너 직원)의 홈 도메인에서 실행되는 ID 공급자가 신뢰 중개자 역할을 합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_12_Fig1_HTML.jpg)
그림 12-1 OAuth 클라이언트 애플리케이션을 위한 중개 인증

파트너의 브로커와 로컬 OAuth 권한 부여 서버(또는 두 페더레이션 도메인 사이) 간의 신뢰 관계는 대역 외에서 설정해야 합니다. 즉, 사전에 두 당사자 간의 합의가 있어야 성립됩니다. 대부분의 시나리오에서 서로 다른 엔터티 간의 신뢰는 X.509 인증서를 통해 설정됩니다. 샘플 중개 인증 사용 사례를 살펴보겠습니다.

OAuth 원칙으로 돌아가서 연합 시나리오에서 리소스 소유자, 리소스 서버, 권한 부여 서버 및 클라이언트 애플리케이션의 네 가지 엔터티를 처리해야 합니다. 이러한 모든 엔터티는 동일한 도메인 또는 다른 도메인에 상주할 수 있습니다.

가장 간단한 시나리오부터 시작하겠습니다. 리소스 소유자(사용자), 리소스 서버(API 게이트웨이) 및 권한 부여 서버는 단일 도메인에 있고 클라이언트 애플리케이션(웹 앱)은 다른 도메인에 있습니다. 예를 들어, Foo Inc.의 직원이고 Bar Inc.에서 호스팅하는 웹 애플리케이션에 액세스하려고 합니다(그림 12-1 참조). Bar Inc.에서 웹 애플리케이션에 로그인하면 사용자를 대신하여 Foo Inc.에서 호스팅되는 API에 액세스해야 합니다. OAuth 용어를 사용하면 리소스 소유자이며 API는 리소스 서버에서 호스팅됩니다. 귀하와 API 모두 Foo 도메인에서 왔습니다. Bar Inc.에서 호스팅하는 웹 애플리케이션은 OAuth 클라이언트 애플리케이션입니다.

그림 12-1은 OAuth 클라이언트 애플리케이션에 대해 중개 인증이 작동하는 방식을 보여줍니다.

- Foo Inc.의 리소스 소유자(사용자)가 Bar Inc.의 웹 애플리케이션을 방문합니다(1단계).

- 사용자를 인증하기 위해 웹 애플리케이션은 리소스 소유자의 홈 도메인이기도 한 Foo Inc.의 OAuth 인증 서버로 사용자를 리디렉션합니다(2단계). OAuth 인증 코드 부여 유형을 사용하려면 웹 애플리케이션도 리디렉션 중에 인증 코드 부여 요청과 함께 클라이언트 ID를 전달해야 합니다. 이때 인증 서버는 클라이언트 애플리케이션을 인증하지 않고 존재 여부만 확인합니다. 연합 시나리오에서 권한 부여 서버는 모든 개별 애플리케이션(또는 OAuth 클라이언트)을 신뢰할 필요가 없습니다. 오히려 해당 도메인을 신뢰합니다. 권한 부여 서버는 신뢰할 수 있는 도메인에 속한 모든 클라이언트의 권한 부여 요청을 수락합니다. 이것은 또한 클라이언트 등록 비용을 피합니다. Bar Inc.의 각 클라이언트 애플리케이션을 등록할 필요가 없습니다. 대신 Foo Inc.의 인증 서버와 Bar Inc.의 트러스트 브로커 간에 신뢰 관계를 구축할 수 있습니다. 인증 코드 부여 단계에서 인증 서버만 클라이언트 ID를 기록해야 합니다. 클라이언트의 존재를 확인할 필요가 없습니다.

> **Note**
> The OAuth client identifier (ID) isn’t treated as a secret. It’s publicly visible to anyone.

- 클라이언트 애플리케이션이 인증 서버로부터 인증 코드를 받으면(3단계), 다음 단계는 이를 유효한 액세스 토큰으로 교환하는 것입니다. 이 단계에서는 클라이언트 인증이 필요합니다.

- 권한 부여 서버는 개별 응용 프로그램을 신뢰하지 않기 때문에 웹 응용 프로그램은 먼저 자체 도메인의 자체 신뢰 브로커에 인증하고(4단계) 서명된 주장을 받아야 합니다(5단계). 이 서명된 주장은 Foo Inc의 인증 서버에 대한 증거 토큰으로 사용할 수 있습니다.

- 인증 서버는 어설션의 서명을 확인하고, 신뢰하는 엔터티가 서명한 경우 해당 액세스 토큰을 클라이언트 애플리케이션에 반환합니다(단계 6 및 7).

- 클라이언트 애플리케이션은 액세스 토큰을 사용하여 리소스 소유자를 대신하여 Foo Inc.의 API에 액세스하거나(8단계) Foo Inc.의 사용자 엔드포인트와 통신하여 사용자에 대한 추가 정보를 얻을 수 있습니다.

> **참고**
>
> 옥스포드 영어 사전에 따르면 주장의 정의는 "사실이나 신념에 대한 자신감 있고 강력한 진술"입니다. 여기서 사실 또는 믿음은 이 주장을 가져오는 엔터티가 신뢰 브로커에서 인증된 엔터티라는 것입니다. 주장이 서명되지 않은 경우 중간에 누구든지 이를 변경할 수 있습니다. 신뢰 브로커(또는 주장 당사자)가 개인 키로 주장에 서명하면 중간에 아무도 이를 변경할 수 없습니다. 변경된 경우 서명 유효성 검사 중에 권한 부여 서버에서 변경 사항을 감지할 수 있습니다. 서명은 신뢰 브로커의 해당 공개 키를 사용하여 검증됩니다.

### SAML

SAML은 XML 기반 데이터 형식으로 이해 당사자 간에 인증, 권한 부여 및 ID 관련 데이터를 교환하기 위한 OASIS 표준입니다. SAML 1.0은 2002년 OASIS 표준으로 채택되었으며, 2003년 SAML 1.1은 OASIS 표준으로 승인되었습니다. 동시에 Liberty Alliance는 ID 연합 프레임워크를 OASIS에 기증했습니다. SAML 2.0은 SAML 1.1, Liberty Alliance의 Identity Federation Framework 및 Shibboleth 1.3을 통합하여 2005년 OASIS 표준이 되었습니다. SAML 2.0에는 네 가지 기본 요소가 있습니다.

- Assertions: Authentication, Authorization, and Attribute assertions.

- Protocol: Request and Response elements to package SAML assertions.

- Bindings: How to transfer SAML messages between interested parties. HTTP binding and SOAP binding are two examples. If the trust broker uses a SOAP message to transfer a SAML assertion, then it has to use the SOAP binding for SAML.

- Profiles: How to aggregate the assertions, protocol, and bindings to address a specific use case. A SAML 2.0 Web Single Sign-On (SSO) profile defines a standard way to establish SSO between different service providers via SAML.

> **Note**
> The blog post at http://blog.facilelogin.com/2011/11/depth-of-saml-saml-summary.html provides a high-level overview of SAML.

### SAML 2.0 Client Authentication

To achieve client authentication with the SAML 2.0 profile for OAuth 2.0, you can use the parameter client_assertion_type with the value urn:ietf:params:oauth:client-assertion-type:saml2-bearer in the access token request (see step 6 in Figure 12-1). The OAuth flow starts from step 2.

Now let’s dig into each step. The following shows a sample authorization code grant request initiated by the web application at Bar Inc.:
```
GET /authorize?response_type=code
               &client_id=wiuo879hkjhkjhk3232
               &state=xyz
               &redirect_uri=https://bar.com/cb
HTTP/1.1
Host: auth.foo.com
``
This results in the following response, which includes the requested authorization code:
```
HTTP/1.1 302 Found
Location: https://bar.com/cb?code=SplwqeZQwqwKJjklje&state=xyz
```
So far it’s the normal OAuth authorization code flow. Now the web application has to talk to the trust broker in its own domain to obtain a SAML assertion. This step is outside the scope of OAuth. Because this is machine-to-machine authentication (from the web application to the trust broker), you can use a SOAP-based WS-Trust protocol to obtain the SAML assertion or any other protocol like OAuth 2.0 Token Delegation profile, which we discussed in Chapter 9. The web application does not need to do this each time a user logs in; it can be one-time operation that is governed by the lifetime of the SAML assertion. The following is a sample SAML assertion obtained from the trust broker:

`<saml:Assertion>
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
</saml:Assertion>`

To use this SAML assertion in an OAuth flow to authenticate the client, it must adhere to the following rules:

- The assertion must have a unique identifier for the Issuer element, which identifies the token-issuing entity. In this case, the broker of the Bar Inc.

- The assertion must have a NameID element inside the Subject element that uniquely identifies the client application (web app). This is treated as the client ID of the client application at the authorization server.

- The SubjectConfirmation method must be set to urn:oasis:names:tc:SAML:2.0:cm:bearer.

- If the assertion issuer authenticates the client, then the assertion must have a single AuthnStatement.

> **Note**
> `WS-Trust` is an OASIS standard for SOAP message security. WS-Trust, which is built on top of the WS-Security standard, defines a protocol to exchange identity information that is wrapped in a token (SAML), between two trust domains. The blog post at http://blog.facilelogin.com/2010/05/ws-trust-with-fresh-banana-service.html explains WS-Trust at a high level. The latest WS-Trust specification is available at http://docs.oasis-open.org/ws-sx/ws-trust/v1.4/errata01/ws-trust-1.4-errata01-complete.html.

Once the client web application gets the SAML assertion from the trust broker, it has to base64url-encode the assertion and send it to the authorization server along with the access token request. In the following sample HTTP POST message, client_assertion_type is set to `urn:ietf:params:oauth:client-assertion-type:saml2-bearer`, and the `base64url-encoded` (see Appendix E) SAML assertion is set to the `client_assertion` parameter :
```
POST /token HTTP/1.1
Host: auth.foo.com
Content-Type: application/x-www-form-urlencoded
grant_type=authorization_code&code=SplwqeZQwqwKJjklje
&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:saml2-bearer
&client_assertion=HdsjkkbKLew...[omitted for brevity]...OT
``
Once the authorization server receives the access token request, it validates the SAML assertion. If it’s valid (signed by a trusted party), an access token is issued, along with a refresh token.


## OAuth 2.0용 SAML 권한 부여 유형

이전 섹션에서는 SAML 어설션을 사용하여 클라이언트 애플리케이션을 인증하는 방법을 설명했습니다. 이것은 OAuth 컨텍스트에 속하는 하나의 연합 사용 사례입니다. 그곳에서는 클라이언트 애플리케이션이 실행되고 있는 Bar Inc. 내부에서 신뢰 브로커가 실행되고 있었습니다. 리소스 서버(API), 인증 서버 및 클라이언트 애플리케이션이 동일한 도메인(Bar Inc.)에서 실행되고 사용자가 외부 도메인(Foo Inc.)에서 실행되는 사용 사례를 생각해 보겠습니다. 여기에서 최종 사용자는 SAML 어설션을 사용하여 웹 애플리케이션에 인증합니다(그림 12-2 참조). 사용자 도메인의 트러스트 브로커(SAML ID 공급자)가 이 어설션을 발행합니다. 클라이언트 애플리케이션은 이 어설션을 사용하여 로그인한 사용자를 대신하여 API에 액세스하기 위한 액세스 토큰을 얻기 위해 로컬 권한 부여 서버와 통신합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_12_Fig2_HTML.jpg)
Figure 12-2 Brokered authentication with the SAML grant type for OAuth 2.0

Figure 12-2 illustrates how brokered authentication with a SAML grant type for OAuth 2.0 works.

- The first three steps are outside the scope of OAuth. The resource owner first logs in to the web application owned by Bar Inc. via SAML 2.0 Web SSO.

- The SAML 2.0 Web SSO flow is initiated by the web application by redirecting the user to the SAML identity provider at Foo Inc. (step 2).

- Once the user authenticates to the SAML identity provider, the SAML identity provider creates a SAML response (which wraps the assertion) and sends it back to the web application (step 3). The web application validates the signature in the SAML assertion and, if a trusted identity provider signs it, allows the user to log in to the web application.

- Once the user logs in to the web application, the web application has to exchange the SAML assertion for an access token by talking to its own internal authorization server (steps 4 and 5). The way to do this is defined in the SAML 2.0 Profile for OAuth 2.0 Client Authentication and Authorization Grants specification (RFC 7522).

다음은 웹 애플리케이션에서 인증 서버로 보내는 샘플 POST 메시지입니다. 여기서 `grant_type`의 값은 `urn:ietf:params:oauth:grant-type:saml2-bearer`여야 하고 `base64url`로 인코딩된 SAML 주장은 주장 매개변수의 값으로 설정됩니다.

> **참고**
> SAML 전달자 부여 유형에서는 새로 고침 토큰이 발급되지 않습니다. 액세스 토큰의 수명은 SAML 전달자 어설션의 수명을 상당히 초과해서는 안 됩니다.

```
POST /token HTTP/1.1
Host: auth.bar.com
Content-Type: application/x-www-form-urlencoded
grant_type=urn:ietf:params:oauth:grant-type:saml2-bearer
&assertion=QBNhbWxwOl...[omitted for brevity]...OT4
```

이 요청은 권한 부여 서버에서 검증됩니다. SAML 주장은 서명을 통해 다시 한 번 검증됩니다. 신뢰할 수 있는 ID 제공자가 서명하면 권한 부여 서버가 유효한 액세스 토큰을 발급합니다.

SAML 전달자 부여 유형에서 발급된 액세스 토큰의 범위는 리소스 소유자가 대역 외로 설정해야 합니다. 여기서 대역 외는 SAML 부여 유형이 사용 중일 때 리소스 소유자가 지정된 리소스와 연결된 범위와 관련하여 리소스 서버/권한 부여 서버와 사전 계약을 맺는 것을 나타냅니다. 클라이언트 애플리케이션은 권한 부여 요청에 범위 매개변수를 포함할 수 있지만 범위 매개변수의 값은 자원 소유자가 대역 외에서 정의한 범위의 하위 집합이어야 합니다. 권한 부여 요청에 범위 매개변수가 포함되지 않은 경우 액세스 토큰은 대역 외 설정 범위를 상속합니다.

논의된 두 페더레이션 사용 사례는 리소스 서버와 권한 부여 서버가 동일한 도메인에서 실행되고 있다고 가정합니다. 그렇지 않은 경우 리소스 서버는 클라이언트가 리소스에 액세스하려고 할 때 액세스 토큰의 유효성을 검사하기 위해 권한 부여 서버에 의해 노출된 API를 호출해야 합니다. Authorization Server가 OAuth Introspection 사양(9장에서 설명)을 지원하는 경우 리소스 서버는 Introspection 끝점과 통신하고 토큰이 활성 상태인지 여부와 토큰과 연결된 범위를 확인할 수 있습니다. 그런 다음 리소스 서버는 토큰에 리소스에 액세스하는 데 필요한 범위 집합이 있는지 확인할 수 있습니다.


## OAuth 2.0용 JWT 부여 유형

RFC 7523에 정의된 OAuth 2.0용 JWT(JSON Web Token) 프로필은 고유한 권한 부여 유형과 클라이언트 인증 메커니즘을 정의하여 OAuth 2.0 핵심 사양을 확장합니다. OAuth 2.0의 권한 부여는 리소스 소유자가 리소스에 액세스하기 위해 OAuth 2.0 클라이언트에 부여한 임시 자격 증명의 추상 표현입니다. OAuth 2.0 핵심 사양은 권한 부여 코드, 암시적, 리소스 소유자 암호 및 클라이언트 자격 증명의 네 가지 권한 부여 유형을 정의합니다. 이러한 각 부여 유형은 리소스 소유자가 자신이 소유한 리소스에 대한 위임된 액세스 권한을 OAuth 2.0 클라이언트에 부여하는 방법을 고유한 방식으로 정의합니다. 이 장에서 설명하는 JWT 부여 유형은 JWT를 OAuth 2.0 액세스 토큰으로 교환하는 방법을 정의합니다. JWT 부여 유형 외에도 RFC 7523은 OAuth 2.0 인증 서버와의 상호 작용에서 OAuth 2.0 클라이언트를 인증하는 방법도 정의합니다. OAuth 2.0은 대부분의 경우 클라이언트 ID와 클라이언트 암호를 사용하는 HTTP 기본 인증이지만 클라이언트 인증을 위한 구체적인 방법을 정의하지 않습니다. RFC 7523은 JWT를 사용하여 OAuth 2.0 클라이언트를 인증하는 방법을 정의합니다.

JWT 권한 부여 유형은 클라이언트가 JWT를 소유하고 있다고 가정합니다. 이 JWT는 자체 발급 JWT 또는 ID 제공자로부터 얻은 JWT일 수 있습니다. JWT에 서명한 사람에 따라 자체 발급 JWT와 ID 제공자 발급 JWT를 구별할 수 있습니다. 클라이언트 자체가 자체 발행 JWT에 서명하는 반면 ID 제공자는 ID 제공자 발행 JWT에 서명합니다. 두 경우 모두 OAuth 권한 부여 서버는 JWT의 발급자를 신뢰해야 합니다. 다음은 샘플 JWT 권한 부여 요청을 보여줍니다. 여기서 grant_type 매개변수의 값은 urn:ietf:params:oauth:grant-type:jwt-bearer로 설정됩니다.

```
POST /token HTTP/1.1
Host: auth.bar.com
Content-Type: application/x-www-form-urlencoded
grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=eyJhbGciOiJFUzI1NiIsImtpZCI6IjE2In0.
eyJpc3Mi[...omitted for brevity...].
J9l-ZhwP[...omitted for brevity...]
```
The Assertion Framework for OAuth 2.0 Client Authentication and Authorization Grants specification, which is the RFC 7521, defines the parameters in the JWT authorization grant request, as listed out in the following:

- grant_type: This is a required parameter, which defines the format of the assertion, as understood by the authorization server. The value of grant_type is an absolute URI, and it must be urn:ietf:params:oauth:grant-type:jwt-bearer.

- assertion: This is a required parameter, which carries the token. For example, in the case of JWT authorization grant type, the assertion parameter will carry the base64url-encoded JWT, and it must only contain a single JWT. If there are multiple JWTs in the assertion, then the authorization server will reject the grant request.

- scope: This is an optional parameter. Unlike in authorization code and implicit grant types, the JWT grant type does not have a way to get the resource owner’s consent for a requested scope. In such case, the authorization server will establish the resource owner’s consent via an out-of-band mechanism. If the authorization grant request carries a value for the scope parameter, then either it should exactly match the out-of-band established scope or less than that.

> **참고**
> OAuth 권한 부여 서버는 JWT 부여 유형에서 refresh_token을 발행하지 않습니다. access_token이 만료되면 OAuth 클라이언트는 새 JWT를 가져오거나(JWT가 만료된 경우) 동일한 유효한 JWT를 사용하여 새 access_token을 가져와야 합니다. access_token의 수명은 해당 JWT의 수명과 일치해야 합니다.

### JWT 부여 유형의 적용

JWT 권한 부여 유형의 여러 애플리케이션이 있습니다. 최종 사용자 또는 리소스 소유자가 OpenID Connect를 통해 웹 애플리케이션에 로그인한 다음(6장) 웹 애플리케이션이 로그인한 사용자를 대신하여 API에 액세스해야 하는 일반적인 사용 사례를 살펴보겠습니다. OAuth 2.0으로 보호됩니다. 그림 12-3은 이 사용 사례와 관련된 주요 상호 작용을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_12_Fig3_HTML.jpg)
Figure 12-3 JWT grant type, a real-world example

The following lists out all the interactions as illustrated in Figure 12-3 by the number:

- The end user visits the web application (step 1).

- In step 2, the user gets redirected to the OpenID Connect server and authenticates against the Active Directory connected to it. After the authentication, the user gets redirected back to the web application, with an authorization code (assuming that we are using OAuth 2.0 authorization code grant type).

- The web application talks directly to the OpenID Connect server and exchanges the authorization code from the previous step to an ID token and an access token. The ID token itself is a JWT, which is signed by the OpenID Connect server (step 3).

- Now the web application needs to invoke an API on behalf of the logged-in user. It talks to the OAuth authorization server, trusted by the API, and using the JWT grant type, exchanges the JWT from step 3 to an OAuth access token. The OAuth authorization server validates the JWT and makes sure that it’s being signed by a trusted identity provider. In this case, the OAuth authorization server trusts the OpenID Connect identity provider (step 4).

- In step 5, the web application invokes the API with the access token from step 4.

- The application server, which hosts the API, validates the access token by talking to the OAuth authorization server, which issued the access token (step 6).


## JWT 클라이언트 인증

OAuth 2.0 핵심 사양은 OAuth 인증 서버에 대해 OAuth 클라이언트를 인증하는 구체적인 방법을 정의하지 않습니다. 대부분 client_id 및 client_secret을 사용한 HTTP 기본 인증입니다. RFC 7523은 JWT로 OAuth 클라이언트를 인증하는 방법을 정의합니다. JWT 클라이언트 인증은 특정 승인 유형에만 국한되지 않습니다. 모든 OAuth 권한 부여 유형과 함께 사용할 수 있습니다. OAuth 2.0의 또 다른 장점은 OAuth 부여 유형이 클라이언트 인증과 분리된다는 점입니다. 다음은 JWT 클라이언트 인증을 사용하는 권한 부여 코드 부여 유형에서 OAuth 권한 부여 서버에 대한 샘플 요청을 보여줍니다.

```
POST /token HTTP/1.1
Host: auth.bar.com
Content-Type: application/x-www-form-urlencoded
grant_type=authorization_code&
code=n0esc3NRze7LTCu7iYzS6a5acc3f0ogp4&      client_assertion_type=urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer&
client_assertion=eyJhbGciOiJSUzI1NiIsImtpZCI6IjIyIn0.
eyJpc3Mi[...omitted for brevity...].
cC4hiUPo[...omitted for brevity...]
```

The RFC 7523 uses three additional parameters in the OAuth request to the token endpoint to do the client authentication: client_assertion_type, client_assertion, and client_id (optional). The Assertion Framework for OAuth 2.0 Client Authentication and Authorization Grants specification, which is the RFC 7521, defines these parameters. The following lists them out along with their definitions:

- client_assertion_type: This is a required parameter, which defines the format of the assertion, as understood by the OAuth authorization server. The value of client_assertion_type is an absolute URI. For JWT client authentication, this parameter must carry the value urn:ietf:params:oauth:client-assertion-type:jwt-bearer.

- client_assertion: This is a required parameter, which carries the token. For example, in the case of JWT client authentication, the client_assertion parameter will carry the base64url-encoded JWT, and it must only contain a single JWT. If there are multiple JWTs in the assertion, then the authorization server will reject the grant request.

- client_id: This is an optional parameter. Ideally, the client_id must be present inside the client_assertion itself. If this parameter carries a value, it must match the value of the client_id inside the client_assertion. Having the client_id parameter in the request itself could be useful, as the authorization server does not need to parse the assertion first to identify the client.

## Applications of JWT Client Authentication

JWT 클라이언트 인증은 `client_id` 및 `client_secret`과 함께 HTTP 기본 인증을 사용하는 대신 JWT로 OAuth 권한 부여 서버에 대해 클라이언트를 인증하는 데 사용됩니다. 누군가가 HTTP 기본 인증보다 JWT 클라이언트 인증을 선택하는 이유는 무엇입니까?

예를 들어 보겠습니다. foo와 bar라는 두 회사가 있다고 가정해 보겠습니다. foo 회사는 API 세트를 호스팅하고, 바 회사에는 해당 API에 대한 애플리케이션을 개발하는 개발자 세트가 있습니다. 이 책에서 논의한 대부분의 OAuth 예제와 마찬가지로, 변호사 회사는 API에 액세스하기 위해 foo 회사에 등록하여 client_id 및 `client_secret`을 얻어야 합니다. 바 회사는 여러 애플리케이션(웹 앱, 모바일 앱, 리치 클라이언트 앱)을 개발하기 때문에 foo 회사에서 얻은 동일한 `client_id`와 `client_secret`을 여러 개발자가 공유해야 합니다. 개발자 중 한 명이 비밀 키를 다른 사람에게 넘기거나 오용할 수 있기 때문에 이것은 약간 위험합니다. 이 문제를 해결하기 위해 JWT 클라이언트 인증을 사용할 수 있습니다. client_id 및 `client_secret`을 개발자와 공유하는 대신 Bar 회사는 키 쌍(공개 키 및 개인 키)을 생성하고 회사의 인증 기관(CA) 키로 공개 키에 서명하고 전달합니다. 개발자에게. 이제 공유 `client_id` 및 `client_secret` 대신 각 개발자는 회사 CA에서 서명한 고유한 공개 키와 개인 키를 갖게 됩니다. foo 회사의 OAuth 인증 서버와 통신할 때 애플리케이션은 자체 개인 키가 JWT에 서명하는 JWT 클라이언트 인증을 사용하고 토큰은 해당 공개 키를 전달합니다. 다음 코드 조각은 이전 기준과 일치하는 샘플 디코딩된 JWS 헤더 및 페이로드를 보여줍니다. 7장에서는 JWS에 대해 자세히 설명하고 JWT와 어떻게 관련되는지 설명합니다.
```
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
```

foo 회사의 인증 서버는 먼저 첨부된 공개 키(이전 코드 스니펫의 x5c 매개변수 값)로 JWT를 확인한 다음 해당 공개 키가 바 회사의 인증서로 서명되었는지 확인해야 합니다. 권한. 이 경우 유효한 JWT이며 클라이언트 인증을 성공적으로 완료합니다. 또한 바 회사에 대해 생성된 원래 client_id의 값이 JWT의 주제로 설정된다는 점에 유의하십시오.

여전히 우리에게는 도전이 있습니다. 해당 개발자가 사임하거나 인증서가 오용된 것으로 판명된 경우 해당 개발자에게 속한 인증서를 어떻게 취소합니까? 이를 용이하게 하기 위해 인증 서버는 `client_id`로 인증서 해지 목록(CRL)을 유지 관리해야 합니다. 즉, 각 `client_id`는 자체 인증서 해지 목록을 유지할 수 있습니다. 인증서를 취소하려면 클라이언트(이 경우 바 회사)가 인증 서버에서 호스팅되는 CRL API와 통신해야 합니다. CRL API는 이 모델을 지원하기 위해 OAuth 권한 부여 서버에서 호스팅되어야 하는 사용자 지정 API입니다. 이 API는 OAuth 2.0 클라이언트 자격 증명 부여 유형으로 보호되어야 합니다. CRL 업데이트 요청을 받으면 API를 호출한 클라이언트에 해당하는 CRL을 업데이트하고 클라이언트 인증이 발생할 때마다 권한 부여 서버는 CRL에 대해 JWT의 공용 인증서를 확인해야 합니다. 일치하는 항목을 찾으면 요청을 즉시 거부해야 합니다. 또한 특정 클라이언트의 CRL이 갱신될 때 해지된 공인인증서에 대해 발급된 모든 접근 토큰과 갱신 토큰도 해지해야 합니다. CRL을 지원하는 데 걸리는 오버헤드가 걱정되는 경우 단기 인증서를 사용하고 해지를 잊어버릴 수 있습니다. 그림 12-4는 foo와 바 회사 간의 상호 작용을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_12_Fig4_HTML.jpg)
Figure 12-4 JWT client authentication, a real-world example

## Parsing and Validating JWT

The OAuth authorization server must parse and validate the JWT, both in the JWT grant type and in the client authentication. The following lists out the criteria for token validation:

- The JWT must have the iss parameter in it. The iss parameter represents the issuer of the JWT. This is treated as a case-sensitive string value. Ideally, this represents the asserting party of the claims set. If Google issues the JWT, then the value of iss would be accounts.google.com. This is an indication to the receiving party who the issuer of the JWT is.

- The JWT must have the sub parameter in it. The token issuer or the asserting party issues the JWT for a particular entity, and the claims set embedded into the JWT normally represents this entity, which is identified by the sub parameter. The value of the sub parameter is a case-sensitive string value. For the JWT client authentication, the value of the sub parameter must carry the corresponding client_id, while for the authorization grant, it will be the authorized accessor or the resource server for which the access token is being requested.

- The JWT must have the aud parameter . The token issuer issues the JWT to an intended recipient or a list of recipients, which is represented by the aud parameter. The recipient or the recipient list should know how to parse the JWT and validate it. Prior to any validation check, the recipient of the token must first see whether the particular JWT is issued for its use and if not should reject immediately. The value of the aud parameter can be a case-sensitive string value or an array of strings. The token issuer should know, prior to issuing the token, who the intended recipient (or the recipients) of the token is, and the value of the aud parameter must be a pre-agreed value between the token issuer and the recipient. In practice, one can also use a regular expression to validate the audience of the token. For example, the value of the aud in the token can be ∗.apress.com, while each recipient under the apress.com domain can have its own aud values: foo.apress.com, bar.apress.com likewise. Instead of finding an exact match for the aud value, each recipient can just check whether the aud value in the token matches a regular expression: (?:[a-zA-Z0-9]∗|\∗).apress.com. This will make sure that any recipient can use a JWT, which is having any subdomain of apress.com.

- The JWT must have the exp parameter. Each JWT will carry an expiration time. The recipient of the JWT token must reject it, if that token has expired. The issuer can decide the value of the expiration time. The JWT specification does not recommend or provide any guidelines on how to decide the best token expiration time. It’s a responsibility of the other specifications, which use JWT internally, to provide such recommendations. The value of the exp parameter is calculated by adding the expiration time (from the token issued time) in seconds to the time elapsed from 1970-01-01T00:00:00Z UTC to the current time. If the token issuer’s clock is out of sync with the recipient’s clock (irrespective of their time zone), then the expiration time validation could fail. To fix that, each recipient can add a couple of minutes as the clock skew.

- The JWT may have the nbf parameter . In other words, this is not a must. The recipient of the token should reject it, if the value of the nbf parameter is greater than the current time. The JWT is not good enough to use prior to the value indicated in the nbf parameter. The value of the nbf parameter is calculated by adding the not before time (from the token issued time) in seconds to the time elapsed from 1970-01-01T00:00:00Z UTC to the current time.

- The JWT may have the iat parameter. The iat parameter in the JWT indicates the issued time of the JWT as calculated by the token issuer. The value of the iat parameter is the number of seconds elapsed from 1970-01-01T00:00:00Z UTC to the current time, when the token is issued.

- The JWT must be digitally signed or carry a Message Authentication Code (MAC) defined by its issuer.


## 요약

- ID 연합은 경계를 넘어 사용자 ID를 전파하는 것입니다. 이러한 경계는 별개의 기업 또는 동일한 기업 내의 별개의 ID 관리 시스템 사이에 있을 수 있습니다.

- 두 개의 OAuth 2.0 프로필(SAML 2.0 부여 유형 및 JWT 부여 유형)은 API 보안을 위한 연합 시나리오 구축에 중점을 둡니다.

- RFC 7522에 정의된 OAuth 2.0용 SAML 프로필은 OAuth 2.0 핵심 사양의 기능을 확장합니다. SAML 어설션을 기반으로 OAuth 2.0 클라이언트를 인증하는 방법과 함께 새로운 권한 부여 유형을 소개합니다.

- RFC 7523에 정의된 OAuth 2.0용 JSON 웹 토큰(JWT) 프로필은 OAuth 2.0 핵심 사양의 기능을 확장합니다. JWT를 기반으로 OAuth 2.0 클라이언트를 인증하는 방법과 새로운 권한 부여 유형을 소개합니다.

 
