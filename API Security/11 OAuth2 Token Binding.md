# 11. OAuth 2.0 Token Binding

대부분의 OAuth 2.0 배포는 전달자 토큰에 의존합니다. 무기명 토큰은 "현금"과 같습니다. 당신에게서 10달러를 훔치면 스타벅스에서 커피 한 잔을 사는 데 사용할 수 있습니다. 10달러 지폐는 소유권을 증명할 필요가 없습니다. 현금과 달리 신용카드는 소지품을 증명해야 합니다. 나의 소유를 증명해야 합니다. 거래를 승인하려면 서명해야 하며 카드의 서명과 대조하여 확인됩니다. 무기명 토큰은 현금과 같습니다. 한 번 도난당한 공격자는 이를 사용하여 원래 소유자를 가장할 수 있습니다. 신용 카드는 소유 증명(PoP) 토큰과 같습니다.

OAuth 2.0은 클라이언트, 인증 서버 및 리소스 서버 간의 모든 상호 작용에 TLS(전송 계층 보안)를 사용할 것을 권장합니다. 이것은 복잡한 암호화가 포함되지 않은 OAuth 2.0 모델을 매우 단순하게 만들지만 동시에 전달자 토큰과 관련된 모든 위험을 수반합니다. 두 번째 수준의 방어는 없습니다. 또한 모든 사람이 OAuth 2.0 전달자 토큰을 사용하는 아이디어에 완전히 동의하는 것은 아닙니다. 단지 기본 TLS 통신을 신뢰하는 것뿐입니다. 나는 단지 무기명 토큰 때문에 OAuth 2.0을 사용하기를 꺼리는 여러 사람들(주로 금융 영역에서)을 만났습니다.

공격자는 다음 수단 중 하나를 사용하여 인증 서버에서 클라이언트로 전송되는 인증 코드/액세스 토큰/갱신 토큰(자세한 내용은 4장 참조)을 도청할 수 있습니다.

- 브라우저(공용 클라이언트)에 설치된 악성코드.
- 브라우저 기록(공용 클라이언트/URI 조각).
- 클라이언트와 권한 부여 서버 또는 리소스 서버 간의 TLS 통신을 가로채십시오(하트블리드 및 로그잼과 같은 TLS 계층의 취약점 악용).
- TLS는 지점 간(종단 간 아님)입니다. 프록시 서버에 액세스할 수 있는 공격자는 단순히 모든 토큰을 기록할 수 있습니다. 또한 많은 프로덕션 배포에서 TLS 연결이 에지에서 종료되고 그 이후부터는 새로운 TLS 연결 또는 일반 HTTP 연결이 됩니다. 두 경우 모두 토큰이 채널을 떠나는 즉시 더 이상 안전하지 않습니다.

## 토큰 바인딩 이해

OAuth 2.0 토큰 바인딩 제안은 보안 토큰을 TLS 레이어에 암호화하여 바인딩하여 토큰 내보내기 및 재생 공격을 방지합니다. TLS에 의존하며 토큰을 TLS 연결 자체에 바인딩하기 때문에 토큰을 훔치는 사람은 다른 채널을 통해 토큰을 사용할 수 없습니다.

토큰 바인딩 프로토콜을 세 가지 주요 단계로 나눌 수 있습니다(그림 11-1 참조).

 
Figure 11-1

Three main phases in the token binding protocol

토큰 바인딩 협상

협상 단계에서 클라이언트와 서버는 그들 사이의 토큰 바인딩에 사용할 매개변수 세트를 협상합니다. 이는 TLS 핸드셰이크 중에 발생하므로 앱 계층 프로토콜과 무관합니다(부록 C 참조). 다음 섹션에서 이에 대해 더 논의합니다. 토큰 바인딩 협상은 RFC 8472에 정의되어 있습니다. 이 단계에서는 키를 협상하지 않고 메타데이터만 협상한다는 점을 명심하십시오.

키 생성

키 생성 단계에서 클라이언트는 협상 단계에서 협상된 매개변수에 따라 키 쌍을 생성합니다. 클라이언트는 통신하는 각 호스트에 대한 키 쌍을 갖습니다(대부분의 경우).

소유 증명

소유 증명 단계에서 클라이언트는 키 생성 단계에서 생성된 키를 사용하여 소유를 증명합니다. 키에 동의하면 키 생성 단계에서 클라이언트는 TLS 연결에서 내보낸 키 자료(EKM)에 서명하여 키 소유를 증명합니다. RFC 5705를 사용하면 앱이 TLS 마스터 암호에서 파생된 추가 앱별 키 자료를 얻을 수 있습니다(부록 C 참조). RFC 8471은 서명 및 기타 주요 자료를 포함하는 토큰 바인딩 메시지의 구조를 정의하지만 클라이언트에서 서버로 토큰 바인딩 메시지를 전달하는 방법을 정의하지 않습니다. 그것을 정의하는 것은 상위 수준의 프로토콜에 달려 있습니다. RFC 8473은 HTTP 연결을 통해 토큰 바인딩 메시지를 전달하는 방법을 정의합니다(그림 11-2 참조).

 

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

### Key Generation

The Token Binding Protocol specification (RFC 8471) defines the parameters related to key generation. These are the ones agreed upon during the negotiation phase.

- If rsa2048_pkcs1.5 key parameter is used during the negotiation phase, then the signature is generated using the RSASSA-PKCS1-v1_5 signature scheme as defined in RFC 3447 with SHA256 as the hash function.

- If rsa2048_pss key parameter is used during the negotiation phase, then the signature is generated using the RSASSA-PSS signature scheme as defined in RFC 3447 with SHA256 as the hash function.

- If ecdsap256 key parameter is used during the negotiation phase, the signature is generated with ECDSA using Curve P-256 and SHA256 as defined in ANSI.X9–62.2005 and FIPS.186–4.2013.

In case a browser acts as the client, then the browser itself has to generate the keys and maintain them against the hostname of the server. You can find the status of this feature development for Chrome from here (www.chromestatus.com/feature/5097603234529280). Then again the token binding is not only for a browser, it’s useful in all the interactions between a client and a server—irrespective of the client being thin or thick.

### Proof of Possession

A token binding is established by a user agent (or the client) generating a private/public key pair (possibly, within a secure hardware module, such as trusted platform module (TPM)) per target server, providing the public key to the server, and proving the possession of the corresponding private key, on every TLS connection to the server. The generated public key is reflected in the token binding ID between the client and the server. At the server end, the verification happens in two steps.

First, the server receiving the token binding message needs to verify that the key parameters in the message match with the token binding parameters negotiated and then validate the signature contained in the token binding message. All the key parameters and the signature are embedded into the token binding message.

The structure of the token binding message is defined in the Token Binding Protocol specification (RFC 8471). A token binding message can have multiple token bindings (see Figure 11-3). A given token binding includes the token binding ID, the type of the token binding (provided or referred—we’ll talk about this later), extensions, and the signature over the concatenation of exported keying material (EKM) from the TLS layer, token binding type, and key parameters. The token binding ID reflects the derived public key along with the key parameters agreed upon the token binding negotiation.

Once the TLS connection is established between a client and a server, the EKM will be the same—both at the client end and at the server end. So, to verify the signature, the server can extract the EKM from the underneath TLS connection and use the token binding type and key parameters embedded into the token binding message itself. The signature is validated against the embedded public key (see Figure 11-3).

 

Figure 11-3

## The structure of the token binding message

How to carry the token binding message from the client to the server is not defined in the Token Binding Protocol specification, but in the Token Binding for HTTP specification or the RFC 8473. In other words, the core token binding specification lets the higher-level protocols make the decision on that. The Token Binding for HTTP specification introduces a new HTTP header called Sec-Token-Binding —and it carries the base64url-encoded value of the token binding message. The Sec-Token-Binding header field MUST NOT be included in HTTP responses—MUST include only once in an HTTP request.

Once the token binding message is accepted as valid, the next step is to make sure that the security tokens carried in the corresponding HTTP connection are bound to it. Different security tokens can be transported over HTTP—for example, cookies and OAuth 2.0 tokens. In the case of OAuth 2.0, how the authorization code, access token, and refresh token are bound to the HTTP connection is defined in the OAuth 2.0 Token Binding specification (https://tools.ietf.org/html/draft-ietf-oauth-token-binding-08).

Token Binding for OAuth 2.0 Refresh Token

Let’s see how the token binding works for OAuth 2.0 refresh tokens . A refresh token, unlike authorization code and access token, is only used between the client and the authorization server. Under the OAuth 2.0 authorization code grant type, the client first gets the authorization code and then exchanges it to an access token and a refresh token by talking to the token endpoint of the OAuth 2.0 authorization server (see Chapter 4 for details). The following flow assumes the client has already got the authorization code (see Figure 11-4).

 

Figure 11-4 OAuth 2.0 refresh grant type


1. 클라이언트와 권한 부여 서버 간의 연결은 TLS에 있어야 합니다.

2. OAuth 2.0 토큰 바인딩을 지원하는 클라이언트는 TLS 핸드셰이크 자체 동안 OAuth 2.0 토큰 바인딩을 지원하는 인증 서버와 필수 매개변수를 협상합니다.

3. TLS 핸드셰이크가 완료되면 OAuth 2.0 클라이언트는 개인 키와 공개 키를 생성하고 기본 TLS 연결에서 내보낸 키 자료(EKM)에 개인 키로 서명하고 토큰 바인딩 메시지를 작성합니다. (정확히 말하면 클라이언트는 EKM + 토큰 바인딩 유형 + 키 매개변수에 서명합니다.)

4. base64url로 인코딩된 토큰 바인딩 메시지는 클라이언트와 OAuth 2.0 인증 서버 간의 연결에 대한 Sec-Token-Binding HTTP 헤더 값으로 추가됩니다.

5. 클라이언트는 Sec-Token-Binding HTTP 헤더와 함께 토큰 끝점에 표준 OAuth 요청을 보냅니다.

6. 인증 서버는 서명을 포함한 Sec-Token-Binding 헤더의 값을 확인하고 발행된 갱신 토큰에 대해 토큰 바인딩 ID(토큰 바인딩 메시지에도 포함됨)를 기록합니다. 프로세스를 상태 비저장으로 만들기 위해 권한 부여 서버는 토큰 바인딩 ID의 해시를 새로 고침 토큰 자체에 포함할 수 있으므로 별도로 기억/저장할 필요가 없습니다.

7. 나중에 OAuth 2.0 클라이언트는 동일한 토큰 끝점에 대해 새로 고침 토큰을 사용하여 액세스 토큰을 새로 고치려고 합니다. 이제 클라이언트는 토큰 바인딩 메시지를 생성하기 위해 이전에 사용한 것과 동일한 개인 키와 공개 키 쌍을 사용해야 하며 다시 한 번 base64url로 인코딩된 값을 Sec-Token-Binding HTTP 헤더에 포함해야 합니다. 토큰 바인딩 메시지는 새로 고침 토큰이 원래 발급된 경우와 동일한 토큰 바인딩 ID를 전달해야 합니다.

8. OAuth 2.0 인증 서버는 이제 Sec-Token-Binding HTTP 헤더의 유효성을 검사한 다음 바인딩 메시지의 토큰 바인딩 ID가 동일한 메시지의 새로 고침 토큰에 첨부된 원래 토큰 바인딩 ID와 동일한지 확인해야 합니다. 요구. 이 검사는 새로 고침 토큰을 원래 토큰 바인딩 외부에서 사용할 수 없도록 합니다. 인증 서버가 토큰 바인딩 ID의 해시 값을 새로 고침 토큰 자체에 포함하기로 결정한 경우 이제 Sec-Token-Binding HTTP 헤더에서 토큰 바인딩 ID의 해시를 계산하고 포함된 것과 비교해야 합니다. 새로 고침 토큰에.

9. 누군가가 새로 고침 토큰을 훔쳐 원래 토큰 바인딩 외부에서 사용하기를 원하면 클라이언트와 서버 간의 연결에 해당하는 개인/공개 키 쌍도 훔쳐야 합니다.


토큰 바인딩에는 두 가지 유형이 있으며 새로 고침 토큰과 관련하여 논의한 내용을 제공된 토큰 바인딩이라고 합니다. 클라이언트와 서버 간에 직접 토큰 교환이 발생하는 경우에 사용합니다. 다른 유형은 참조 토큰 바인딩으로 알려져 있으며, 이는 액세스 토큰과 같은 다른 서버에 제공하기 위한 토큰을 요청할 때 사용됩니다. 액세스 토큰은 클라이언트와 권한 부여 서버 간의 연결에서 발급되지만 클라이언트와 리소스 서버 간의 연결에서 사용됩니다.

OAuth 2.0 인증 코드/액세스 토큰에 대한 토큰 바인딩

인증 코드 부여 유형에서 액세스 토큰에 대한 토큰 바인딩이 어떻게 작동하는지 봅시다. OAuth 2.0 인증 코드 부여 유형에서 클라이언트는 먼저 브라우저(사용자 에이전트)를 통해 인증 코드를 얻은 다음 OAuth 2.0 인증 서버의 토큰 끝점과 대화하여 액세스 토큰 및 새로 고침 토큰으로 교환합니다(그림 참조). 11-5).


Figure 11-5 OAuth 2.0 authorization code flow

1. 최종 사용자가 브라우저에서 OAuth 2.0 클라이언트 응용 프로그램의 로그인 링크를 클릭하면 브라우저는 클라이언트 응용 프로그램(웹 서버에서 실행됨)에 대해 HTTP GET을 수행해야 하고 브라우저는 TLS를 설정해야 합니다. 먼저 OAuth 2.0 클라이언트와 연결합니다. OAuth 2.0 토큰 바인딩을 지원하는 브라우저는 TLS 핸드셰이크 자체 중에 OAuth 2.0 토큰 바인딩도 지원하는 클라이언트 앱과 필수 매개변수를 협상합니다. TLS 핸드셰이크가 완료되면 브라우저는 개인 키와 공개 키(클라이언트 도메인용)를 생성하고 개인 키로 기본 TLS 연결에서 내보낸 키 자료(EKM)에 서명하고 토큰 바인딩 메시지를 작성합니다. base64url로 인코딩된 토큰 바인딩 메시지는 브라우저와 OAuth 2.0 클라이언트(HTTP GET) 간의 연결에 대한 Sec-Token-Binding HTTP 헤더의 값으로 추가됩니다.

2. 1단계(모든 토큰 바인딩 유효성 검사가 완료되었다고 가정)에 대한 응답으로 클라이언트는 사용자를 OAuth 2.0 인증 서버로 리디렉션하도록 요청하는 302 응답을 브라우저에 보냅니다. 또한 응답에서 클라이언트는 true로 설정된 HTTP 헤더 Include-Referred-Token-Binding-ID를 포함합니다. 이것은 브라우저가 권한 서버에 대한 요청에 브라우저와 클라이언트 사이에 설정된 토큰 바인딩 ID를 포함하도록 지시합니다. 또한 클라이언트 앱은 요청에 code_challenge 및 code_challenge_method의 두 가지 추가 매개변수를 포함합니다. 이러한 매개변수는 PKCE(Proof Key for Code Exchange) 또는 OAuth 2.0용 RFC 7636에 정의되어 있습니다. 토큰 바인딩에서 이 두 매개변수는 정적 값인 code_challenge=referred_tb 및 code_challenge_method=referred_tb를 전달합니다.

3. 브라우저는 TLS 핸드셰이크 자체 중에 권한 부여 서버와 필요한 매개변수를 협상합니다. TLS 핸드셰이크가 완료되면 브라우저는 개인 키와 공개 키(인증 서버 도메인용)를 생성하고 개인 키로 기본 TLS 연결에서 내보낸 키 자료(EKM)에 서명하고 토큰 바인딩 메시지를 작성합니다. . 클라이언트는 Sec-Token-Binding HTTP 헤더와 함께 인증 끝점에 표준 OAuth 요청을 보냅니다. 이 Sec-Token-Binding HTTP 헤더는 이제 두 개의 토큰 바인딩을 포함합니다(하나의 토큰 바인딩 메시지에서 - 그림 11-3 참조). 하나는 브라우저와 인증 서버 간의 연결을 위한 것이고 다른 하나는 브라우저와 클라이언트를 위한 것입니다. 앱(참조된 바인딩).

4. 인증 서버는 인증 코드와 함께 브라우저를 통해 사용자를 OAuth 클라이언트 앱으로 다시 리디렉션합니다. 인증 코드는 참조된 토큰 바인딩의 토큰 바인딩 ID에 대해 발급됩니다.

5. 브라우저는 클라이언트 앱에 POST를 수행하며 여기에는 인증 서버의 인증 코드도 포함됩니다. 브라우저는 자신과 클라이언트 앱 간에 설정된 동일한 토큰 바인딩 ID를 사용하고 Sec-Token-Binding HTTP 헤더를 추가합니다.

6. 클라이언트 응용 프로그램이 인증 코드를 받으면(Sec-Token-Binding 유효성 검사가 성공하면) 이제 인증 서버의 토큰 끝점과 통신합니다. 그 전에 클라이언트는 인증 서버와 토큰 바인딩을 설정해야 합니다. 토큰 요청에는 PKCE RFC에 정의된 code_verifier 매개변수도 포함됩니다. 이 매개변수는 인증 코드에 첨부된 토큰 바인딩 ID이기도 한 클라이언트와 브라우저 간에 제공된 토큰 바인딩 ID를 전달합니다. 인증 서버에서 발급할 액세스 토큰은 보호된 리소스에 대해 사용되기 때문에 클라이언트는 자신과 리소스 서버 간의 토큰 바인딩을 이 토큰 바인딩 메시지에 참조 바인딩으로 포함해야 합니다. 토큰 요청을 받으면 OAuth 2.0 인증 서버는 이제 Sec-Token-Binding HTTP 헤더의 유효성을 검사한 다음 code_verifier 매개변수의 토큰 바인딩 ID가 인증에 첨부된 원래 토큰 바인딩 ID와 동일한지 확인해야 합니다. 발급 시점의 코드입니다. 이 검사는 원래 토큰 바인딩 외부에서 코드를 사용할 수 없도록 합니다. 그런 다음 권한 부여 서버는 참조된 토큰 바인딩에 바인딩된 액세스 토큰과 클라이언트와 권한 부여 서버 간의 연결에 바인딩된 새로 고침 토큰을 발급합니다.

7. 이제 클라이언트 응용 프로그램이 액세스 토큰을 전달하는 리소스 서버에서 API를 호출합니다. 이것은 클라이언트와 리소스 서버 간에 토큰 바인딩을 수행합니다.

8. 리소스 서버는 이제 인증 서버의 자체 검사 끝점과 통신하고 액세스 토큰에 연결된 바인딩 ID를 반환하므로 리소스 서버는 자신과 클라이언트 앱 간에 사용된 동일한 바인딩 ID인지 확인할 수 있습니다. .

### TLS Termination

많은 프로덕션 배포에는 TLS 연결을 종료하는 역방향 프록시가 포함되어 있습니다. 이것은 클라이언트와 서버 사이에 있는 Apache 또는 Nginx 서버에 있을 수 있습니다. 역방향 프록시에서 연결이 종료되면 서버는 TLS 계층에서 무슨 일이 일어났는지 알 수 없습니다. 보안 토큰이 들어오는 TLS 연결에 바인딩되었는지 확인하려면 서버가 토큰 바인딩 ID를 알아야 합니다. 역방향 프록시를 종료하는 TLS를 사용한 HTTPS 토큰 바인딩, 사양 초안(https://tools.ietf.org/html/draft-ietf-tokbind-ttrp-09)은 바인딩 ID가 역방향 프록시에서 역방향 프록시로 전달되는 방식을 표준화합니다. 백엔드 서버, HTTP 헤더. Provided-Token-Binding-ID 및 Referred-Token-Binding-ID HTTP 헤더는 이 사양에 의해 도입되었습니다(그림 11-6 참조).


Figure 11-6 The reverse proxy passes the Provided-Token-Binding-ID and Referred-Token-Binding-ID HTTP headers to the backend server


## 요약

- OAuth 2.0 토큰 바인딩 제안은 보안 토큰을 TLS 레이어에 암호화하여 바인딩하여 토큰 내보내기 및 재생 공격을 방지합니다.

- 토큰 바인딩은 TLS에 의존하며 토큰을 TLS 연결 자체에 바인딩하기 때문에 토큰을 훔치는 사람은 다른 채널을 통해 토큰을 사용할 수 없습니다.

- 우리는 토큰 바인딩 프로토콜을 협상 단계, 키 생성 단계 및 소유 증명 단계의 세 가지 주요 단계로 나눌 수 있습니다.

- 협상 단계에서 클라이언트와 서버는 토큰 바인딩에 사용할 매개변수 집합을 협상합니다.

- 키 생성 단계에서 클라이언트는 협상 단계에서 협상된 매개변수에 따라 키 쌍을 생성합니다.

- 소유 증명 단계에서 클라이언트는 키 생성 단계에서 생성된 키를 사용하여 소유를 증명합니다.

 

