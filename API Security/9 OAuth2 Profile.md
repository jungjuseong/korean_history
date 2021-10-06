# 9. OAuth 2.0 Profiles

OAuth 2.0은 위임된 권한 부여를 위한 프레임워크입니다. 모든 특정 엔터프라이즈 API 보안 사용 사례를 다루지는 않습니다. 핵심 프레임워크 위에 구축된 OAuth 2.0 프로필은 OAuth 2.0을 엔터프라이즈급 배포에 사용할 수 있도록 보안 에코시스템을 구축합니다. OAuth 2.0은 부여 유형과 토큰 유형을 통해 두 가지 확장 지점을 도입했습니다. OAuth 2.0용 프로필은 이러한 확장성을 기반으로 구축됩니다. 이 장에서는 토큰 자체 검사, 연결된 API 호출, 동적 클라이언트 등록 및 토큰 취소를 위한 5가지 주요 OAuth 2.0 프로필에 대해 설명합니다.

## Token Introspection

OAuth 2.0은 리소스 서버와 인증 서버 간의 통신을 위한 표준 API를 정의하지 않습니다. 그 결과, 공급업체별, 독점 API가 자원 서버를 인증 서버에 연결하기 위해 몰래 들어왔습니다. OAuth 2.0용 Token Introspection 프로파일1은 인증 서버(그림 9-1)에 의해 노출될 표준 API를 제안함으로써 이 격차를 메우고 리소스 서버가 이에 대해 말하고 토큰 메타데이터를 검색할 수 있도록 합니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_9_Fig1_HTML.jpg)

Figure 9-1 OAuth 2.0 Token Introspection

액세스 토큰을 소유한 모든 당사자는 토큰 자체 검사 요청을 생성할 수 있습니다. 인트로스펙션 엔드포인트를 보호할 수 있으며 널리 사용되는 옵션은 `mTLS` 및 OAuth 2.0 클라이언트 자격 증명입니다.
```
POST /introspection HTTP/1.1
Accept: application/x-www-form-urlencoded
Host: authz.server.com
Authorization: Basic czZCaGRSa3F0Mzo3RmpmcDBaQnIxS3REUmJuZlZkbUl3
                     token=X3241Affw.423399JXJ&
                     token_type_hint=access_token&
```

각 매개변수의 정의를 살펴보겠습니다.

- `token`: `access_token` 또는 `refresh_token`의 값입니다. 이것은 메타데이터를 가져와야 하는 토큰입니다.

- `token_type_hint`: 토큰의 유형(`access_token` 또는 `refresh_token`). 이것은 선택 사항이며 여기에 전달된 값은 자체 검사 응답을 생성할 때 권한 부여 서버의 작업을 최적화할 수 있습니다.

이 요청은 다음 JSON 응답을 반환합니다. 다음 응답은 자체 검사 응답에 포함될 수 있는 모든 가능한 매개변수를 표시하지 않습니다.

```
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
```

인트로스펙션 응답에서 기대할 수 있는 주요 매개변수의 정의를 살펴보겠습니다.

- 활성: 토큰이 활성 상태인지 여부를 나타냅니다. 활성화하려면 토큰이 만료되거나 취소되어서는 안 됩니다. 권한 부여 서버는 활성을 정의하는 방법에 대한 자체 기준을 정의할 수 있습니다. 이것은 인트로스펙션 응답이 포함해야 하는 유일한 필수 매개변수입니다. 나머지는 모두 선택 사항입니다.

- `client_id`: 인증 서버가 이 토큰을 발급한 클라이언트의 식별자입니다.

- `scope`: 토큰과 연결된 승인된 범위입니다. 리소스 서버는 API에 액세스하는 데 필요한 범위가 토큰에 연결된 범위의 하위 집합 이상인지 확인해야 합니다.

- `sub`: 권한 부여를 승인한 사용자의 주체 식별자 또는 이 토큰이 나타내는 사용자의 식별자입니다. 이 식별자는 반드시 사람이 읽을 수 있는 식별자는 아니지만 항상 고유한 값을 전달해야 합니다. 인증 서버는 각 인증 서버/리소스 서버 조합에 대해 고유한 주제를 생성할 수 있습니다. 이것은 구현에 따라 다르며 이를 지원하기 위해 권한 부여 서버는 리소스 서버를 고유하게 식별해야 합니다. 프라이버시 측면에서 권한 부여 서버는 리소스 서버별로 서로 다른 주체 식별자를 유지 관리하는 것이 필수적이며, 이러한 종류의 식별자를 영속 가명이라고 합니다. 권한 부여 서버는 다른 리소스 서버에 대해 다른 가명을 발행하기 때문에 주어진 사용자에 대해 이러한 리소스 서버는 함께 이 사용자가 액세스하는 다른 서비스를 식별할 수 없습니다

- `username`: 권한 부여를 승인한 사용자의 사람이 읽을 수 있는 식별자 또는 이 토큰이 나타내는 사용자에 대한 사람이 읽을 수 있는 식별자를 전달합니다. 리소스 서버 측에서 무엇이든 유지하려면 사용자와 관련하여 사용자 이름이 올바른 식별자가 아닙니다. 사용자 이름의 값은 권한 부여 서버 측에서 구현되는 방식에 따라 수시로 변경될 수 있습니다.

- `aud`: 토큰에 대해 허용된 청중입니다. 이상적으로는 해당 리소스 서버를 나타내는 식별자가 있어야 합니다. 식별자와 일치하지 않으면 리소스 서버는 즉시 토큰을 거부해야 합니다. 이 aud 요소는 둘 이상의 식별자를 포함할 수 있으며, 이 경우 리소스 서버의 식별자가 식별자의 일부인지 확인해야 합니다. 또한 일부 구현에서는 일대일 문자열 일치를 수행하는 대신 정규식과 일치시킬 수도 있습니다. 예를 들어 http://*.my-resource.com은 http://foo.my-resource.com 및 http://bar.my-resource.com 식별자를 포함하는 리소스 서버 모두에 대해 일치하는 항목을 찾습니다.

> **참고**
>
`aud` 매개변수는 http://tools.ietf.org/html/draft-tschofenig-oauth-audience-00에 있는 OAuth 2.0: 대상 정보 인터넷 초안에 정의되어 있습니다. 이것은 OAuth 토큰 요청 흐름에 도입된 새 매개변수이며 토큰 유형과 무관합니다.

- `exp`: 1970년 1월 1일부터 토큰의 만료 시간을 UTC로 초 단위로 정의합니다. 활성 매개변수가 이미 응답에 있으므로 중복된 것처럼 보입니다. 그러나 리소스 서버는 이 매개변수를 활용하여 권한 부여 서버의 자체 검사 엔드포인트와 통신하려는 빈도를 최적화할 수 있습니다. 인트로스펙션 엔드포인트에 대한 호출은 원격이므로 성능 문제가 있을 수 있으며 어떤 이유로 인해 다운될 수도 있습니다. 이 경우 리소스 서버는 인트로스펙션 응답을 전달할 캐시를 가질 수 있으며 동일한 토큰을 몇 번이고 다시 받았을 때 캐시를 확인할 수 있으며 토큰이 만료되지 않은 경우 토큰을 유효한 것으로 받아들일 수 있습니다. 또한 유효한 캐시 만료 시간이 있어야 합니다. 그렇지 않으면 권한 서버에서 토큰이 취소되더라도 리소스 서버는 이에 대해 알지 못합니다.

- `iat`: 1970년 1월 1일 UTC 기준으로 토큰이 발행된 시간을 초 단위로 정의합니다.

- `nbf`: 1970년 1월 1일 UTC 기준으로 토큰을 사용하지 않아야 하는 시간을 초 단위로 정의합니다.

- `token_type`: 토큰의 종류를 나타냅니다. 무기명 토큰, MAC 토큰(부록 G 참조) 또는 기타 유형이 될 수 있습니다.

- `iss`: 토큰 발행자를 나타내는 식별자를 전달합니다. 리소스 서버는 여러 발급자(또는 권한 부여 서버)의 토큰을 수락할 수 있습니다. 토큰의 주체를 리소스 서버단에 저장하면 발급자에게만 고유하게 됩니다. 따라서 발급자와 함께 보관해야 합니다. 리소스 서버가 다중 테넌트 인증 서버에 연결되는 경우가 있을 수 있습니다. 이 경우 내부 검사 끝점은 동일하지만 다른 테넌트에서 토큰을 발행하는 다른 발급자가 됩니다.

- `jti`: 인증 서버에서 발급한 토큰의 고유 식별자입니다. jti는 인증 서버가 발급한 액세스 토큰이 JWT 또는 자체 포함된 액세스 토큰인 경우에 주로 사용됩니다. 이는 액세스 토큰 재생을 방지하는 데 유용합니다.

인트로스펙션 엔드포인트의 응답을 확인하는 동안 리소스 서버는 먼저 `active` 값이 true로 설정되어 있는지 확인해야 합니다. 그런 다음 응답의 `ud` 값이 리소스 서버 또는 리소스와 연결된 `aud` URI와 일치하는지 확인해야 합니다. 마지막으로 범위를 확인할 수 있습니다. 리소스에 액세스하는 데 필요한 범위는 자체 검사 응답에서 반환된 범위 값의 하위 집합이어야 합니다. 리소스 서버가 클라이언트 또는 리소스 소유자를 기반으로 추가 액세스 제어를 수행하려는 경우 sub 및 `client_id` 값과 관련하여 수행할 수 있습니다.

## Chain Grant Type

Once the audience restriction is enforced on OAuth tokens, they can only be used against the intended audience. You can access an API with an access token that has an audience restriction corresponding to that API. If this API wants to talk to another protected API to form the response to the client, the first API must authenticate to the second API. When it does so, the first API can’t just pass the access token it received initially from the client. That will fail the audience restriction validation at the second API. The Chain Grant Type OAuth 2.0 profile defines a standard way to address this concern.

According to the OAuth Chain Grant Type profile, the API hosted in the first resource server must talk to the authorization server and exchange the OAuth access token it received from the client for a new one that can be used to talk to the other API hosted in the second resource server.

대상 제한이 OAuth 토큰에 적용되면 의도한 대상에 대해서만 사용할 수 있습니다. 해당 API에 해당하는 대상 제한이 있는 액세스 토큰으로 API에 액세스할 수 있습니다. 이 API가 다른 보호된 API와 통신하여 클라이언트에 대한 응답을 형성하려는 경우 첫 번째 API는 두 번째 API에 대해 인증해야 합니다. 그렇게 하면 첫 번째 API는 클라이언트에서 처음에 받은 액세스 토큰을 그냥 전달할 수 없습니다. 두 번째 API에서 대상 제한 유효성 검사에 실패합니다. Chain Grant Type OAuth 2.0 프로필은 이 문제를 해결하는 표준 방법을 정의합니다.

`OAuth Chain Grant Type` 프로파일에 따르면 첫 번째 리소스 서버에서 호스팅되는 API는 인증 서버와 통신해야 하며 클라이언트로부터 받은 OAuth 액세스 토큰을 두 번째 리소스 서버에서 호스팅되는 다른 API와 통신하는 데 사용할 수 있는 새 것으로 교환해야 합니다. 

> **Note**
>
> The Chain Grant Type for OAuth 2.0 profile is available at https://datatracker.ietf.org/doc/draft-hunt-oauth-chain.

체인 부여 유형 요청은 첫 번째 리소스 서버에서 권한 부여 서버로 생성되어야 합니다. 부여 유형의 값은 http://oauth.net/grant_type/chain으로 설정되어야 하며 클라이언트에서 수신한 OAuth 액세스 토큰을 포함해야 합니다. scope 매개변수는 공백으로 구분된 문자열로 두 번째 리소스에 필요한 범위를 표현해야 합니다. 이상적으로 범위는 원래 액세스 토큰과 연결된 범위와 같거나 하위 집합이어야 합니다. 차이가 있는 경우 권한 부여 서버에서 액세스 토큰을 발급할지 여부를 결정할 수 있습니다. 이 결정은 리소스 소유자와의 대역 외 계약을 기반으로 할 수 있습니다.

```
POST /token HTTP/1.1
Host: authz.server.net
Content-Type: application/x-www-form-urlencoded
grant_type=http://oauth.net/grant_type/chain
oauth_token=dsddDLJkuiiuieqjhk238khjh
scope=read
```

This returns the following JSON response. The response includes an access token with a limited lifetime, but it should not have a refresh token. To get a new access token, the first resource server once again must present the original access token:
```
HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Cache-Control: no-store
Pragma: no-cache
{
       "access_token":"2YotnFZFEjr1zCsicMWpAA",
       "token_type":"Bearer",
       "expires_in":1800,
}
```
The first resource server can use the access token from this response to talk to the second resource server. Then the second resource server talks to the authorization server to validate the access token (see Figure 9-2).

 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_9_Fig2_HTML.jpg)
Figure 9-2 OAuth 2.0 Token Exchange

We talked about the chain grant type in the first edition of the book as well. But since then this specification didn’t make any progress. If you are using the chain grant type already, you should migrate to the OAuth 2.0 Token Exchange specification, which is still at the draft stage, but closer to being an RFC. In the next section, we talk about OAuth 2.0 Token Exchange draft RFC.

## Token Exchange

OAuth 2.0 Token Exchange는 현재 IETF 작업 그룹에서 논의된 제안 초안입니다. 이전 섹션에서 논의한 Chain Grant Type 제안에서 해결한 유사한 문제를 몇 가지 개선과 함께 해결합니다. 체인 그랜트 유형과 마찬가지로 첫 번째 리소스 서버가 클라이언트 응용 프로그램에서 액세스 토큰을 수신하고 다른 리소스 서버와 통신하려고 할 때 첫 번째 리소스 서버는 권한 부여 서버와 대화하기 위해 다음 요청을 생성하고 클라이언트 애플리케이션에서 새 애플리케이션으로 가져온 액세스 토큰입니다.

```
POST /token HTTP/1.1
Host: authz.server.net
Content-Type: application/x-www-form-urlencoded
grant_type=urn:ietf:params:oauth:grant-type:token-exchange
subject_token=dsddDLJkuiiuieqjhk238khjh
subject_token_type=urn:ietf:params:oauth:token-type:access_token
requested_token_type=urn:ietf:params:oauth:token-type:access_token
resource=https://bar.example.com
scope=read
```

앞의 샘플 요청에는 가능한 모든 매개변수가 포함되어 있지 않습니다. 토큰 교환 요청에서 기대할 수 있는 주요 매개변수를 살펴보겠습니다.

- `grant_type`: 토큰 교환과 관련된 요청이며 urn:ietf:params:oauth:grant-type:token-exchange 값을 전달해야 함을 토큰 끝점에 나타냅니다. 이것은 필수 매개변수입니다.

- `resource`: 이 매개변수의 값은 대상 자원에 대한 참조를 전달합니다. 예를 들어, 초기 요청이 foo API에 와서 bar API와 통신하려는 경우 resource 매개변수의 값은 bar API의 끝점을 전달합니다. 이는 한 마이크로 서비스가 다른 마이크로 서비스에 인증해야 하는 마이크로 서비스 배포에서도 매우 유용합니다. OAuth 2.0 권한 부여 서버는 foo API가 bar API에 액세스할 수 있는지 여부를 확인하기 위해 이 요청에 대해 액세스 제어 정책을 시행할 수 있습니다. 이것은 선택적 매개변수입니다.

- `aud`: 이 매개변수의 값은 자원 매개변수와 동일한 용도로 사용되지만 이 경우 대상 매개변수의 값은 절대 URL이 아닌 대상 자원의 참조입니다. 여러 대상 리소스에 대해 동일한 토큰을 사용하려는 경우 대상 매개변수 아래에 대상 값 목록을 포함할 수 있습니다. 이것은 선택적 매개변수입니다.

- `scope`: 새 토큰에 대한 범위 값을 나타냅니다. 이 매개변수는 공백으로 구분된 대소문자 구분 문자열 목록을 포함할 수 있습니다. 이것은 선택적 매개변수입니다.

- `requested_token_type`: Indicates the type of request token, which can be any of urn:ietf:params:oauth:token-type:access_token, urn:ietf:params:oauth:token-type:refresh_token, urn:ietf:params:oauth:token-type:id_token, urn:ietf:params:oauth:token-type:saml1, and urn:ietf:params:oauth:token-type:saml2. This is an optional parameter, and if it is missing, the token endpoint can decide the type of the token to return. If you use a different token type, which is not in the above list, then you can have your own URI as the requested_token_type.

- `subject_token`: Carries the initial token the first API receives. This carries the identity of the entity that initially invokes the first API. This is a required parameter.

- `subject_token_type`: Indicates the type of subject_token, which can be any of urn:ietf:params:oauth:token-type:access_token, urn:ietf:params:oauth:token-type:refresh_token, urn:ietf:params:oauth:token-type:id_token, urn:ietf:params:oauth:token-type:saml1, and urn:ietf:params:oauth:token-type:saml2. This is a required parameter. If you use a different token type, which is not in the above list, then you can have your own URI as the subject_token_type.

- `actor_toke`n: Carries a security token, which represents the identity of the entity that intends to use the requested token. In our case, when foo API wants to talk to the bar API, actor_token represents the foo API. This is an optional parameter.

- `actor_token_type`: Indicates the type of actor_token, which can be any of urn:ietf:params:oauth:token-type:access_token, urn:ietf:params:oauth:token-type:refresh_token, urn:ietf:params:oauth:token-type:id_token, urn:ietf:params:oauth:token-type:saml1, and urn:ietf:params:oauth:token-type:saml2. This is a required parameter when the actor_token is present in the request. If you use a different token type, which is not in the above list, then you can have your own URI as the actor_token_type.

The preceding request returns the following JSON response. The access_token parameter in the response carries the requested token, while the issued_token_type indicates its type. The other parameters in the response, token_type, expires_in, scope, and refresh_token, carry the same meaning as in a typical OAuth 2.0 token response, which we discussed in Chapter 4.
```
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
```

## Dynamic Client Registration Profile

According to the OAuth 2.0 core specification, all OAuth clients must be registered with the OAuth authorization server and obtain a client identifier before any interactions. The aim of the Dynamic Client Registration OAuth 2.0 profile2 is to expose an endpoint for client registration in a standard manner to facilitate on-the-fly registrations.

The dynamic registration endpoint exposed by the authorization server can be secured or not. If it’s secured, it can be secured with OAuth, HTTP Basic authentication, Mutual Transport Layer Security (mTLS), or any other security protocol as desired by the authorization server. The Dynamic Client Registration profile doesn’t enforce any authentication protocols over the registration endpoint, but it must be secured with TLS. If the authorization server decides that it should allow the endpoint to be public and let anyone be registered, it can do so. For the registration, the client application must pass all its metadata to the registration endpoint:
```
POST /register HTTP/1.1
Content-Type: application/json
Accept: application/json
Host: authz.server.com
{
"redirect_uris":["https://client.org/callback","https://client.org/callback2"],
"token_endpoint_auth_method":"client_secret_basic","grant_types": ["authorization_code" , "implicit"],
"response_types": ["code" , "token"],
}
```
Let’s examine the definition of some of the important parameters in the client registration request:

- redirect_uris: An array of URIs under the control of the client. The user is redirected to one of these redirect_uris after the authorization grant. These redirect URIs must be over Transport Layer Security (TLS).

- `token_endpoint_auth_method`: The supported authentication scheme when talking to the token endpoint. If the value is client_secret_basic, the client sends its client ID and the client secret in the HTTP Basic Authorization header. If it’s client_secret_post, the client ID and the client secret are in the HTTP POST body. If the value is none, the client doesn’t want to authenticate, which means it’s a public client (as in the case of the OAuth implicit grant type or when you use authorization code grant type with a single-page application). Even though this RFC only supports three client authentication methods, the other OAuth profiles can introduce their own. For example, OAuth 2.0 Mutual-TLS Client Authentication and Certificate-Bound Access Tokens, a draft RFC which is being discussed under the IETF OAuth working group at the moment, introduces a new authentication method called tls_client_auth. This indicates that client authentication to the token endpoint happens with mutual TLS.

- `grant_types`: An array of grant types supported by the client. It is always better to limit your client application only to use the grant types it needs and no more. For example, if your client application is a single-page application, then you must only use authorization_code grant type.

- `response_types`: An array of expected response types from the authorization server. In most of the cases, there is a correlation between the grant_types and response_types—and if you pick something inconsistent, the authorization server will reject the registration request.

- `client_name`: A human-readable name that represents the client application. The authorization server will display the client name to the end users during the login flow. This must be informative enough so that the end users will be able to figure out the client application, during the login flow.

- `client_uri`: A URL that points to the client application. The authorization server will display this URL to the end users, during the login flow in a clickable manner.

- `logo_uri`: A URL pointing to the logo of the client application. The authorization server will display the logo to the end users, during the login flow.

- `scope`: A string containing a space-separated list of scope values where the client intends to request from the authorization server.

- `contacts`: A list of representatives from the client application end.

- `tos_uri`: A URL pointing to the terms of service document of the client application. The authorization server will display this link to the end users, during the login flow.

- policy_uri: A URL pointing to the privacy policy document of the client application. The authorization server will display this link to the end users, during the login flow.

- `jwks_uri`: Points to the endpoint, which carries the JSON Web Key (JWK) Set document with the client’s public key. Authorization server uses this public key to validate the signature of any of the requests signed by the client application. If the client application cannot host its public key via an endpoint, it can share the JWKS document under the parameter jwks instead of jwks_uri. Both the parameters must not be present in a single request.

- `software_id`: This is similar to client_id, but there is a major difference. The client_id is generated by the authorization server and mostly used to identify the application. But the client_id can change during the lifetime of an application. In contrast, the software_id is unique to the application across its lifecycle and uniquely represents all the metadata associated with it throughout the application lifecycle.

- `software_version`: The version of the client application, identified by the software_id.

- `software_statement`: This is a special parameter in the registration request, which carries a JSON Web Token (JWT). This JWT includes all the metadata defined earlier with respect to the client. In case the same parameter is defined in JWT and also in the request outside the software_statement parameter, then the parameter within the software_statement will take the precedence.

Based on the policies of the authorization server, it can decide whether it should proceed with the registration or not. Even if it decides to go ahead with the registration, the authorization server need not accept all the suggested parameters from the client. For example, the client may suggest using both authorization_code and implicit as grant types, but the authorization server can decide what to allow. The same is true for the token_endpoint_auth_method: the authorization server can decide what to support. The following is a sample response from the authorization server:
```
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
```
Let’s examine the definition of each parameter:

- client_id: The generated unique identifier for the client.

- client_secret: The generated client secret corresponding to the client_id. This is optional. For example, for public clients the client_secret isn’t required.

- client_id_issued_at: The number of seconds since January 1, 1970.

- client_secret_expires_at: The number of seconds since January 1, 1970 or 0 if it does not expire.

- redirect_uris: Accepted redirect_uris.

- token_endpoint_auth_method: The accepted authentication method for the token endpoint.

> **Note**
>
> The Dynamic Client Registration OAuth 2.0 profile is quite useful in mobile applications. Mobile client applications secured with OAuth have the client ID and the client secret baked into the application. These are the same for all the installations of a given application. If a given client secret is compromised, that will affect all the installations, and rogue client applications can be developed using the stolen keys. These rogue client applications can generate more traffic on the server and exceed the legitimate throttling limit, hence causing a denial of service attack. With dynamic client registration, you need not set the same client ID and client secret for all the installations of a give application. During the installation process, the application can talk to the authorization server’s registration endpoint and generate a client ID and a client secret per installation.

### Token Revocation Profile

Two parties can perform OAuth token revocation. The resource owner should be able to revoke an access token issued to a client, and the client should be able to revoke an access token or a refresh token it has acquired. The Token Revocation OAuth 2.0 profile3 addresses the latter. It introduces a standard token-revoke endpoint at the authorization server end. To revoke an access token or a refresh token, the client must notify the revoke endpoint.

> **Note**
>
> In October 2013, there was an attack against Buffer (a social media management service that can be used to cross-post between Facebook, Twitter, etc.). Buffer was using OAuth to access user profiles in Facebook and Twitter. Once Buffer detected that it was under attack, it revoked all its access keys from Facebook, Twitter, and other social media sites, which prevented attackers from getting access to users’ Facebook and Twitter accounts.

The client must initiate the token revocation request. The client can authenticate to the authorization server via HTTP Basic authentication (with its client ID and client secret), with mutual TLS or with any other authentication mechanism proposed by the authorization server and then talk to the revoke endpoint. The request should consist of either the access token or the refresh token and then a token_type_hint that informs the authorization server about the type of the token (access_token or refresh_token). This parameter may not be required, but the authorization server can optimize its search criteria using it.

Here is a sample request:
```
POST /revoke HTTP/1.1
Host: server.example.com
Content-Type: application/x-www-form-urlencoded
Authorization: Basic czZCaGRSdadsdI9iuiaHk99kjkh
token=dsd0lkjkkljkkllkdsdds&token_type_hint=access_token
```

이 요청에 대한 응답으로 권한 부여 서버는 먼저 클라이언트 자격 증명을 확인한 다음 토큰 취소를 진행해야 합니다. 토큰이 새로 고침 토큰인 경우 권한 부여 서버는 해당 새로 고침 토큰과 관련된 권한 부여에 대해 발급된 모든 액세스 토큰을 무효화해야 합니다. 액세스 토큰인 경우 새로 고침 토큰을 취소할지 여부를 결정하는 것은 권한 부여 서버에 달려 있습니다. 대부분의 경우 새로 고침 토큰도 취소하는 것이 이상적입니다. 토큰 취소가 성공적으로 완료되면 인증 서버는 HTTP 200 상태 코드를 클라이언트에 다시 보내야 합니다.

## 요약

- 핵심 프레임워크 위에 구축된 OAuth 2.0 프로필은 OAuth 2.0을 엔터프라이즈급 배포에 사용할 수 있도록 보안 에코시스템을 구축합니다.

- OAuth 2.0은 부여 유형과 토큰 유형을 통해 두 가지 확장 지점을 도입했습니다.

- OAuth 2.0용 Token Introspection 프로필은 인증 서버에 표준 API를 도입하여 리소스 서버가 이에 대해 말하고 토큰 메타데이터를 검색할 수 있도록 합니다.

- OAuth Chain Grant Type 프로파일에 따르면, 첫 번째 리소스 서버에서 호스팅되는 API는 인증 서버와 통신해야 하며 클라이언트로부터 받은 OAuth 액세스 토큰을 에서 호스팅되는 다른 API와 통신하는 데 사용할 수 있는 새 것으로 교환해야 합니다. 두 번째 리소스 서버.

- OAuth 2.0 Token Exchange는 현재 IETF 워킹 그룹에서 논의된 초안 제안으로, 일부 개선 사항을 포함하여 Chain Grant Type 제안과 유사한 문제를 해결합니다.

- 동적 클라이언트 등록 OAuth 2.0 프로필의 목표는 즉시 등록을 용이하게 하기 위해 표준 방식으로 클라이언트 등록을 위한 끝점을 노출하는 것입니다.

- 토큰 해지 OAuth 2.0 프로필은 인증 서버에서 표준 토큰 해지 끝점을 도입하여 클라이언트에서 액세스 토큰 또는 새로 고침 토큰을 해지합니다.