8. Message-Level Security with JSON Web Encryption

7장에서는 JWT(JSON Web Token) 및 JWS(JSON Web Signature) 사양에 대해 자세히 설명했습니다. 이 두 사양은 모두 IETF JOSE 작업 그룹에서 개발되었습니다. 이 장에서는 메시지 암호화(JSON 페이로드일 필요는 없음)를 위해 동일한 IETF 작업 그룹에서 개발한 또 다른 중요한 표준인 JSON 웹 암호화(JWE)에 중점을 둡니다.

JWS와 마찬가지로 JWT는 JWE의 기반입니다. JWE 사양은 암호화된 콘텐츠를 JSON 기반 데이터 구조로 표현하는 방법을 표준화합니다. JWE1 사양은 암호화된 페이로드를 나타내는 두 가지 직렬화된 형식(JWE 압축 직렬화 및 JWE JSON 직렬화)을 정의합니다. 이 두 직렬화 기술은 모두 다음 섹션에서 자세히 설명합니다. 

JWS와 마찬가지로 JWE 표준을 사용하여 암호화할 메시지는 JSON 페이로드일 필요가 없으며 모든 콘텐츠가 될 수 있습니다. JWE 토큰이라는 용어는 JWE 사양에 정의된 직렬화 기술에 따라 직렬화된 형식의 암호화된 메시지(JSON뿐 아니라 모든 메시지)를 참조하는 데 사용됩니다.

## JWE Compact Serialization

With the JWE compact serialization, a JWE token is built with five key components, each separated by periods (`.`): JOSE header, JWE Encrypted Key, JWE Initialization Vector, JWE Ciphertext, and JWE Authentication Tag. Figure 8-1 shows the structure of a JWE token formed by JWE compact serialization.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_8_Fig1_HTML.jpg)
Figure 8-1 A JWE token with compact serialization

### JOSE Header

JOSE 헤더는 압축 직렬화에서 생성된 JWE 토큰의 첫 번째 요소입니다. JOSE 헤더의 구조는 몇 가지 예외를 제외하고 7장에서 논의한 것과 동일합니다. JWE 사양은 JWS(JSON 웹 서명) 사양에 의해 도입된 매개변수 외에 JWE 토큰의 JOSE 헤더에 포함된 두 개의 새로운 매개변수(enc 및 zip)를 도입합니다. 다음은 JWE 사양에 정의된 모든 JOSE 헤더 매개변수를 나열합니다.

- alg(알고리즘): 콘텐츠 암호화 키(CEK)를 암호화하는 데 사용되는 알고리즘의 이름. CEK는 일반 텍스트 JSON 페이로드를 암호화하는 대칭 키입니다. 일반 텍스트가 CEK로 암호화되면 CEK 자체는 ag 매개변수 값으로 식별되는 알고리즘에 따라 다른 키로 암호화됩니다. 그러면 암호화된 CEK가 JWE 토큰의 JWE 암호화 키 섹션에 포함됩니다. 이것은 JOSE 헤더의 필수 속성입니다. 헤더에 이것을 포함하지 않으면 토큰 구문 분석 오류가 발생합니다. alg 매개변수의 값은 JWA(JSON Web Algorithms2) 사양에 정의된 JSON 웹 서명 및 암호화 알고리즘 레지스트리에서 선택되는 문자열입니다. ag 매개변수의 값이 이전 레지스트리에서 선택되지 않은 경우 충돌 방지 방식으로 정의되어야 하지만 특정 알고리즘이 모든 JWE 구현에서 식별된다는 보장은 없습니다. JWA 사양에 정의된 알고리즘을 고수하는 것이 항상 더 좋습니다.

- enc: JOSE 헤더의 enc 매개변수는 콘텐츠 암호화에 사용되는 알고리즘의 이름을 나타냅니다. 이 알고리즘은 AEAD(연결된 데이터로 인증된 대칭 암호화) 알고리즘이어야 합니다. 이것은 JOSE 헤더의 필수 속성입니다. 헤더에 이것을 포함하지 않으면 토큰 구문 분석 오류가 발생합니다. enc 매개변수의 값은 JSON 웹 알고리즘(JWA) 사양에 의해 정의된 JSON 웹 서명 및 암호화 알고리즘 레지스트리에서 선택되는 문자열입니다. enc 매개변수의 값이 이전 레지스트리에서 선택되지 않은 경우 충돌 방지 방식으로 정의되어야 하지만 특정 알고리즘이 모든 JWE 구현에서 식별된다는 보장은 없습니다. JWA 사양에 정의된 알고리즘을 고수하는 것이 항상 더 좋습니다.

- zip: JOSE 헤더의 zip 매개변수는 압축 알고리즘의 이름을 정의합니다. 토큰 발급자가 압축을 사용하기로 결정한 경우 일반 텍스트 JSON 페이로드는 암호화 전에 압축됩니다. 압축은 필수가 아닙니다. JWE 사양은 DEF를 압축 알고리즘으로 정의하지만 반드시 사용해야 하는 것은 아닙니다. 토큰 발행자는 자체 압축 알고리즘을 정의할 수 있습니다. 압축 알고리즘의 기본값은 JSON 웹 알고리즘(JWA) 사양의 JSON 웹 암호화 압축 알고리즘 레지스트리에 정의되어 있습니다. 이것은 선택적 매개변수입니다.

- jku: JOSE 헤더의 jku 매개변수는 JSON 웹 키(JWK)3 세트를 가리키는 URL을 전달합니다. 이 JWK 세트는 JSON으로 인코딩된 공개 키 모음을 나타내며, 여기서 키 중 하나는 콘텐츠 암호화 키(CEK)를 암호화하는 데 사용됩니다. 키 세트를 검색하는 데 사용되는 프로토콜이 무엇이든 무결성 보호를 제공해야 합니다. HTTP를 통해 키를 검색하는 경우 일반 HTTP 대신 HTTPS(또는 TLS를 통한 HTTP)를 사용해야 합니다. 부록 C에서 TLS(전송 계층 보안)에 대해 자세히 설명합니다. jku는 선택적 매개변수입니다.


- jwk: JOSE 헤더의 jwk 매개변수는 콘텐츠 암호화 키(CEK)를 암호화하는 데 사용되는 키에 해당하는 공개 키를 나타냅니다. 키는 JWK(JSON 웹 키) 사양에 따라 인코딩됩니다.3 앞에서 논의한 jku 매개변수는 JWK 세트를 보유하는 링크를 가리키고 jwk 매개변수는 키를 JOSE 헤더 자체에 포함합니다. jwk는 선택적 매개변수입니다.

- kid: JOSE 헤더의 kid 매개변수는 콘텐츠 암호화 키(CEK)를 암호화하는 데 사용되는 키의 식별자를 나타냅니다. 이 식별자를 사용하여 JWE의 수신자는 키를 찾을 수 있어야 합니다. 토큰 발행자가 JOSE 헤더의 kid 매개변수를 사용하여 수신자에게 서명 키에 대해 알린다면 해당 키가 토큰 발행자와 수신자 간에 "어쨌든" 교환되어야 합니다. 이 키 교환이 발생하는 방법은 JWE 사양의 범위를 벗어납니다. kid 매개변수의 값이 JWK를 참조하는 경우 이 매개변수의 값은 JWK의 kid 매개변수 값과 일치해야 합니다. kid는 JOSE 헤더의 선택적 매개변수입니다.

- x5u: JOSE 헤더의 x5u 매개변수는 이전에 논의한 jku 매개변수와 매우 유사합니다. 여기에서 URL은 JWK 세트를 가리키는 대신 X.509 인증서 또는 X.509 인증서 체인을 가리킵니다. URL이 가리키는 리소스는 PEM 인코딩 형식의 인증서 또는 인증서 체인을 보유해야 합니다. 체인의 각 인증서는 구분자4 사이에 나타나야 합니다. -----BEGIN CERTIFICATE----- 및 -----END CERTIFICATE-----. 콘텐츠 암호화 키(CEK)를 암호화하는 데 사용되는 키에 해당하는 공개 키는 인증서 체인의 맨 처음 항목이어야 하며 나머지는 중간 CA(인증 기관)와 루트 CA의 인증서입니다. x5u는 JOSE 헤더의 선택적 매개변수입니다.

- x5c: The x5c parameter in the JOSE header represents the X.509 certificate (or the certificate chain), which corresponds to the public key, which is used to encrypt the Content Encryption Key (CEK). This is similar to the jwk parameter we discussed before, but in this case instead of a JWK, it’s an X.509 certificate (or a chain of certificates). The certificate or the certificate chain is represented in a JSON array of certificate value strings. Each element in the array should be a base64-encoded DER PKIX certificate value. The public key corresponding to the key used to encrypt the Content Encryption Key (CEK) should be the very first entry in the JSON array, and the rest is the certificates of intermediate CAs (certificate authority) and the root CA. The x5c is an optional parameter in the JOSE header.

- x5t: JOSE 헤더의 x5t 매개변수는 콘텐츠 암호화 키(CEK)를 암호화하는 데 사용되는 키에 해당하는 X.509 인증서의 base64url로 인코딩된 SHA-1 지문을 나타냅니다. 이것은 우리가 이전에 논의한 kid 매개변수와 유사합니다. 이 두 매개변수는 키를 찾는 데 사용됩니다. 토큰 발행자가 JOSE 헤더의 x5t 매개변수를 사용하여 수신자에게 서명 키에 대해 알린다면 해당 키는 사전에 토큰 발행자와 수신자 간에 "어떻게든" 교환되어야 합니다. 이 키 교환이 발생하는 방법은 JWE 사양의 범위를 벗어납니다. x5t는 JOSE 헤더의 선택적 매개변수입니다.

- x5t#s256: JOSE 헤더의 x5t#s256 매개변수는 콘텐츠 암호화 키(CEK)를 암호화하는 데 사용되는 키에 해당하는 X.509 인증서의 base64url로 인코딩된 SHA256 지문을 나타냅니다. x5t#s256과 x5t의 유일한 차이점은 해싱 알고리즘입니다. x5t#s256은 JOSE 헤더의 선택적 매개변수입니다.

- typ: JOSE 헤더의 typ 매개변수는 완전한 JWE의 미디어 유형을 정의하는 데 사용됩니다. JWE를 처리하는 구성 요소에는 JWE 구현과 JWE 응용 프로그램의 두 가지 유형이 있습니다. Nimbus5는 Java의 JWE 구현입니다. Nimbus 라이브러리는 JWE를 빌드하고 구문 분석하는 방법을 알고 있습니다. JWE 응용 프로그램은 내부적으로 JWE를 사용하는 모든 것이 될 수 있습니다. JWE 애플리케이션은 JWE 구현을 사용하여 JWE를 빌드하거나 구문 분석합니다. 이 경우 typ 매개변수는 JWE 구현을 위한 또 다른 매개변수일 뿐입니다. 값을 해석하려고 시도하지 않지만 JWE 응용 프로그램은 해석합니다. typ 매개변수는 여러 유형의 객체가 있는 경우 JWE 애플리케이션이 내용을 구별하는 데 도움이 됩니다. JWS 압축 직렬화를 사용하는 JWS 토큰 및 JWE 압축 직렬화를 사용하는 JWE 토큰의 경우 typ 매개변수의 값은 JOSE이고 JWS JSON 직렬화를 사용하는 JWS 토큰 및 JWE JSON 직렬화를 사용하는 JWE 토큰의 경우 값은 JOSE입니다. +제이슨. (JWS 직렬화는 7장에서 논의되었고 JWE 직렬화는 이 장의 뒷부분에서 논의되었습니다). 유형은 JOSE 헤더의 선택적 매개변수입니다.

- cty: JOSE 헤더의 cty 매개변수는 JWE에서 보안 콘텐츠의 미디어 유형을 나타내는 데 사용됩니다. 중첩된 JWT의 경우에만 이 매개변수를 사용하는 것이 좋습니다. 중첩된 JWT는 이 장의 뒷부분에서 설명하고 cty 매개변수의 정의는 거기에서 더 자세히 설명합니다. cty는 JOSE 헤더의 선택적 매개변수입니다.

- crit: JOSE 헤더의 crit 매개변수는 JWE 또는 JWA 사양에 의해 정의되지 않은 사용자 정의 매개변수가 JOSE 헤더에 있음을 JWE 수신자에게 표시하는 데 사용됩니다. 수신자가 이러한 사용자 정의 매개변수를 이해하지 못하는 경우 JWE 토큰은 유효하지 않은 것으로 처리됩니다. crit 매개변수의 값은 이름의 JSON 배열이며, 여기서 각 항목은 사용자 정의 매개변수를 나타냅니다. crit는 JOSE 헤더의 선택적 매개변수입니다.

앞서 정의한 13개의 모든 매개변수 중 7개는 콘텐츠 암호화 키(CEK)를 암호화하는 데 사용되는 공개 키를 참조하는 방법에 대해 설명합니다. 키를 참조하는 방법에는 외부 참조, 내장 및 키 식별자의 세 가지가 있습니다. jku 및 x5u 매개변수는 외부 참조 범주에 속합니다. 둘 다 URI를 통해 키를 참조합니다. jwk 및 x5c 매개변수는 포함된 참조 범주에 속합니다. 각각은 JOSE 헤더 자체에 키를 포함하는 방법을 정의합니다. kid, x5t 및 x5t#s256 매개변수는 키 식별자 참조 범주에 속합니다. 세 가지 모두 식별자를 사용하여 키를 찾는 방법을 정의합니다. 그런 다음 다시 모든 7개의 매개변수는 키 표현에 따라 JSON 웹 키(JWK) 및 X.509의 두 가지 범주로 더 나눌 수 있습니다. jku, jwk 및 kid는 JWK 범주에 속하고 x5u, x5c, x5t 및 x5t#s256은 X.509 범주에 속합니다. 주어진 JWE 토큰의 JOSE 헤더에서 주어진 시간에 앞의 매개변수 중 하나만 있으면 됩니다.

> **Note**
>
> The JSON payload, which is subject to encryption, could contain whitespaces and/or line breaks before or after any JSON value.

JWE 사양은 이전에 정의된 13개의 헤더 매개변수만 사용하도록 애플리케이션을 제한하지 않습니다. 새 헤더 매개변수를 도입하는 방법에는 공개 헤더 이름과 개인 헤더 이름의 두 가지가 있습니다. 공용 공간에서 사용하려는 모든 헤더 매개변수는 충돌 방지 방식으로 도입되어야 합니다. IANA JSON 웹 서명 및 암호화 헤더 매개변수 레지스트리에 이러한 공개 헤더 매개변수를 등록하는 것이 좋습니다. 개인 헤더 매개변수는 주로 토큰 발행자와 수신자가 서로를 잘 알고 있는 제한된 환경에서 사용됩니다. 이러한 매개변수는 충돌 가능성이 있으므로 주의해서 사용해야 합니다. 주어진 수신자가 여러 토큰 발행자의 토큰을 수락하면 동일한 매개변수의 의미가 개인 헤더인 경우 발행자마다 다를 수 있습니다. 공용 헤더 매개변수이든 개인 헤더 매개변수이든, JWE 또는 JWA 사양에 정의되어 있지 않은 경우 헤더 이름은 앞서 논의한 crit 헤더 매개변수에 포함되어야 합니다.

### JWE Encrypted Key

JWE의 JWE 암호화 키 섹션을 이해하려면 먼저 JSON 페이로드가 암호화되는 방식을 이해해야 합니다. JOSE 헤더의 enc 매개변수는 콘텐츠 암호화 알고리즘을 정의하며, 대칭 `AEAD(Authenticated Encryption with Associated Data)` 알고리즘이어야 합니다. JOSE 헤더의 `lg` 매개변수는 콘텐츠 암호화 키(CEK)를 암호화하는 암호화 알고리즘을 정의합니다. CEK를 래핑하므로 이 알고리즘을 키 래핑 알고리즘이라고 부를 수도 있습니다.

> 인증된 암호화
>
> 암호화만으로는 데이터 기밀성만 제공합니다. 의도된 수신자만 암호화된 데이터를 해독하고 볼 수 있습니다. 모든 사람이 데이터를 볼 수는 없지만 암호화된 데이터에 액세스할 수 있는 사람은 누구나 데이터의 비트 스트림을 변경하여 다른 메시지를 반영할 수 있습니다. 예를 들어 Alice가 그녀의 은행 계좌에서 Bob의 계좌로 미화 100달러를 이체하고 그 메시지가 암호화된 경우 중간에 있는 Eve는 그 안에 무엇이 들어 있는지 볼 수 없습니다. 그러나 Eve는 암호화된 데이터의 비트 스트림을 수정하여 메시지를 변경할 수 있습니다(예: US $100에서 US $150). 거래를 통제하는 은행은 Eve가 중간에 수행한 이러한 변경을 감지하지 못하고 합법적인 거래로 취급합니다. 그렇기 때문에 암호화 자체가 항상 안전한 것은 아니며, 1970년대에는 이것이 은행 업계의 문제로 확인되었습니다.
>
> `Authenticated Encryption`은 단순한 암호화와 달리 데이터에 대한 기밀성, 무결성 및 신뢰성을 동시에 보장합니다. ISO/IEC 19772:2009는 GCM, OCB 2.0, CCM, Key Wrap, EAX 및 Encrypt-then-MAC의 6가지 인증된 암호화 모드를 표준화했습니다. AEAD(Authenticated Encryption with Associated Data)는 이 모델을 확장하여 암호화되지 않은 추가 인증 데이터(AAD)의 무결성과 신뢰성을 보존하는 기능을 추가합니다. AAD는 관련 데이터(AD)라고도 합니다. AEAD 알고리즘은 암호화할 일반 텍스트와 AAD(추가 인증 데이터)의 두 가지 입력을 사용하여 암호문과 인증 태그의 두 가지 출력을 생성합니다. AAD는 인증되지만 암호화되지 않은 데이터를 나타냅니다. 인증 태그는 암호문과 AAD의 무결성을 보장합니다.

Let’s look at the following JOSE header. For content encryption, it uses A256GCM algorithm, and for key wrapping, RSA-OAEP:
```
{"alg":"RSA-OAEP","enc":"A256GCM"}
```
A256GCM은 JWA 사양에 정의되어 있습니다. `Galois/Counter Mode(GCM)` 알고리즘의 `AES(Advanced Encryption Standard)`를 256비트 길이의 키와 함께 사용하며 `AEAD`에 사용되는 대칭 키 알고리즘입니다. 대칭 키는 주로 콘텐츠 암호화에 사용됩니다. 대칭 키 암호화는 비대칭 키 암호화보다 훨씬 빠릅니다. 동시에 비대칭 키 암호화는 대용량 메시지를 암호화하는 데 사용할 수 없습니다. `RSA-OAEP`는 JWA 사양에 너무 정의되어 있습니다. 암호화 과정에서 토큰 발행자는 256비트 크기의 임의 키를 생성하고 AES GCM 알고리즘에 따라 해당 키를 사용하여 메시지를 암호화합니다. 다음으로, 메시지 암호화에 사용되는 키는 비대칭 암호화 방식인 RSA-OAEP6를 사용하여 암호화됩니다. RSA-OAEP 암호화 체계는 `OAEP(Optimal Asymmetric Encryption Padding)` 방법과 함께 RSA 알고리즘을 사용합니다. 마지막으로 암호화된 대칭 키는 JWE의 JWE 암호화된 헤더 섹션에 배치됩니다.

### Key Management Modes

키 관리 모드는 콘텐츠 암호화 키(CEK)에 대한 값을 도출하거나 계산하는 방법을 정의합니다. JWE 사양은 다음과 같이 5가지 키 관리 모드를 사용하며 적절한 키 관리 모드는 JOSE 헤더에 정의된 alg 매개변수에 따라 결정됩니다.

1. 키 암호화: 키 암호화 모드에서 CEK 값은 비대칭 암호화 알고리즘을 사용하여 암호화됩니다. 예를 들어, JOSE 헤더의 alg 매개변수 값이 RSA-OAEP이면 해당 키 관리 알고리즘은 기본 매개변수를 사용하는 RSAES OAEP입니다. ag 매개변수와 키 관리 알고리즘 간의 이러한 관계는 JWA 사양에 정의되어 있습니다. RSAES OAEP 알고리즘은 키 암호화를 키 관리 모드로 차지하여 CEK 값을 도출합니다.

2. 키 래핑: 키 래핑 모드에서 CEK 값은 대칭 키 래핑 알고리즘을 사용하여 암호화됩니다. 예를 들어, JOSE 헤더의 alg 매개변수 값이 A128KW인 경우 해당 키 관리 알고리즘은 128비트 키를 사용하는 기본 초기 값이 있는 AES 키 랩입니다. AES Key Wrap 알고리즘은 키 관리 모드로 키 래핑을 차지하여 CEK 값을 도출합니다.

3. 직접 키 동의: 직접 키 동의 모드에서 CEK 값은 키 동의 알고리즘에 따라 결정됩니다. 예를 들어 JOSE 헤더의 alg 매개변수 값이 ECDH-ES인 경우 해당 키 관리 알고리즘은 Concat KDF를 사용하는 Elliptic Curve Diffie-Hellman Ephemeral Static 키 계약입니다. 이 알고리즘은 직접 키 동의를 키 관리 모드로 사용하여 CEK 값을 도출합니다.

4. 키 랩핑을 통한 키 동의: 키 랩핑 모드를 사용한 직접 키 동의에서 CEK 값은 키 계약 알고리즘에 따라 결정되며 대칭 키 랩핑 알고리즘을 사용하여 암호화됩니다. 예를 들어 JOSE 헤더의 alg 매개변수 값이 ECDH-ES+A128KW인 경우 해당 키 관리 알고리즘은 A128KW로 랩핑된 Concat KDF 및 CEK를 사용하는 ECDH-ES입니다. 이 알고리즘은 CEK 값을 도출하기 위한 키 관리 모드로 키 래핑과 직접 키 동의를 차지합니다.

5. Direct encryption: With the direct encryption mode, the value of the CEK is the same as the symmetric key value, which is already shared between the token issuer and the recipient. For example, if the value of the alg parameter in the JOSE header is dir, then the direct encryption is occupied as the key management mode to derive the value of the CEK.

#### JWE Initialization Vector

Some encryption algorithms, which are used for content encryption, require an initialization vector, during the encryption process. Initialization vector is a randomly generated number, which is used along with a secret key to encrypt data. This will add randomness to the encrypted data, which will prevent repetition even if the same data gets encrypted using the same secret key again and again. To decrypt the message at the token recipient end, it has to know the initialization vector, hence included in the JWE token, under the JWE Initialization Vector element. If the content encryption algorithm does not require an initialization vector, then the value of this element should be kept empty.

#### JWE Ciphertext

The fourth element of the JWE token is the base64url-encoded value of the JWE ciphertext. The JWE ciphertext is computed by encrypting the plaintext JSON payload using the CEK, the JWE Initialization Vector, and the Additional Authentication Data (AAD) value, with the encryption algorithm defined by the header parameter enc. The algorithm defined by the enc header parameter should be a symmetric Authenticated Encryption with Associated Data (AEAD) algorithm. The AEAD algorithm, which is used to encrypt the plaintext payload, also allows specifying Additional Authenticated Data (AAD).

#### JWE Authentication Tag

The base64url-encoded value of the JWE Authentication Tag is the final element of the JWE token. The value of the authentication tag is produced during the AEAD encryption process, along with the ciphertext. The authentication tag ensures the integrity of the ciphertext and the Additional Authenticated Data (AAD).

### The Process of Encryption (Compact Serialization)

We have discussed about all the ingredients that are required to build a JWE token under compact serialization. The following discusses the steps involved in building the JWE token. There are five elements in a JWE token; the first element is produced by step 6, the second element is produced by step 3, the third element is produced by step 4, the fourth element is produced by step 10, and the fifth element is produced by step 11.

1. Figure out the key management mode by the algorithm used to determine the Content Encryption Key (CEK) value. This algorithm is defined by the alg parameter in the JOSE header. There is only one alg parameter per JWE token.

2. Compute the CEK and calculate the JWE Encrypted Key based on the key management mode, picked in step 1. The CEK is later used to encrypt the JSON payload. There is only one JWE Encrypted Key element in the JWE token.

3. Compute the base64url-encoded value of the JWE Encrypted Key, which is produced by step 2. This is the second element of the JWE token.

4. Generate a random value for the JWE Initialization Vector. Irrespective of the serialization technique, the JWE token carries the value of the base64url-encoded value of the JWE Initialization Vector. This is the third element of the JWE token.

5. If token compression is needed, the JSON payload in plaintext must be compressed following the compression algorithm defined under the zip header parameter.
   
6. Construct the JSON representation of the JOSE header and find the base64url-encoded value of the JOSE header with UTF-8 encoding. This is the first element of the JWE token.

7. To encrypt the JSON payload, we need the CEK (which we already have), the JWE Initialization Vector (which we already have), and the Additional Authenticated Data (AAD). Compute ASCII value of the encoded JOSE header (step 6) and use it as the AAD.

8. Encrypt the compressed JSON payload (from step 5) using the CEK, the JWE Initialization Vector, and the Additional Authenticated Data (AAD), following the content encryption algorithm defined by the enc header parameter.

9. The algorithm defined by the enc header parameter is an AEAD algorithm, and after the encryption process, it produces the ciphertext and the Authentication Tag.

10. Compute the base64url-encoded value of the ciphertext, which is produced by step 9. This is the fourth element of the JWE token.

11. Compute the base64url-encoded value of the Authentication Tag, which is produced by step 9. This is the fifth element of the JWE token.

12. Now we have all the elements to build the JWE token in the following manner. The line breaks are introduced only for clarity.

`BASE64URL-ENCODE(UTF8(JWE Protected Header)).
BASE64URL-ENCODE(JWE Encrypted Key).
BASE64URL-ENCODE(JWE Initialization Vector).
BASE64URL-ENCODE(JWE Ciphertext).
BASE64URL-ENCODE(JWE Authentication Tag)`
 
## JWE JSON Serialization

JWE 압축 직렬화와 달리 JWE JSON 직렬화는 동일한 JSON 페이로드를 통해 여러 수신자를 대상으로 하는 암호화된 데이터를 생성할 수 있습니다. JWE JSON 직렬화에서 궁극적으로 직렬화된 형식은 암호화된 JSON 페이로드를 JSON 객체로 나타냅니다. 이 JSON 객체는 6개의 최상위 요소인 protected, unprotected, recipients, iv, ciphertext, tag를 포함합니다. 다음은 JWE JSON 직렬화로 직렬화되는 JWE 토큰의 예입니다.

```
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
```

### JWE 보호 헤더

JWE 보호 헤더는 AEAD 알고리즘으로 무결성을 보호해야 하는 헤더 매개변수를 포함하는 JSON 객체입니다. JWE 보호 헤더 내부의 매개변수는 JWE 토큰의 모든 수신자에게 적용됩니다. 직렬화된 JSON 형식의 protected 매개변수는 JWE 보호 헤더의 base64url 인코딩 값을 나타냅니다. 루트 수준의 JWE 토큰에는 보호된 요소가 하나만 있으며 이전에 JOSE 헤더에서 논의한 모든 헤더 매개변수는 JWE 보호된 헤더에서도 사용할 수 있습니다.

### JWE 공유 비보호 헤더

JWE 공유 비보호 헤더는 무결성이 보호되지 않는 헤더 매개변수를 포함하는 JSON 객체입니다. 직렬화된 JSON 형식의 unprotected 매개변수는 JWE 공유 비보호 헤더를 나타냅니다. 루트 수준의 JWE 토큰에는 보호되지 않은 요소가 하나만 있으며 이전에 JOSE 헤더에서 논의한 모든 헤더 매개변수는 JWE 공유 비보호 헤더에서도 사용할 수 있습니다.

### JWE 수신자별 비보호 헤더

JWE 수신자별 비보호 헤더는 무결성이 보호되지 않는 헤더 매개변수를 포함하는 JSON 객체입니다. JWE 수신자별 비보호 헤더 내부의 매개변수는 JWE 토큰의 특정 수신자에게만 적용됩니다. JWE 토큰에서 이러한 헤더 매개변수는 매개변수 수신자 아래에 그룹화됩니다. recipients 매개변수는 JWE 토큰의 수신자 배열을 나타냅니다. 각 멤버는 헤더 매개변수와 암호화된 키 매개변수로 구성됩니다.

- header: The header parameter, which is inside the recipients parameter, represents the value of the JWE header elements that aren’t protected for integrity by authenticated encryption for each recipient.

- encryptedkey: The encryptedkey parameter represents the base64url-encoded value of the encrypted key. This is the key used to encrypt the message payload. The key can be encrypted in different ways for each recipient.

Any header parameter that we discussed before under the JOSE header can also be used under the JWE Per-Recipient Unprotected Header.

### JWE Initialization Vector

This carries the same meaning as explained under JWE compact serialization previously in this chapter. The iv parameter in the JWE token represents the value of the initialization vector used for encryption.

### JWE Ciphertext

This carries the same meaning as explained under JWE compact serialization previously in this chapter. The ciphertext parameter in the JWE token carries the base64url-encoded value of the JWE ciphertext.

### JWE 인증 태그

이것은 이 장의 앞부분에서 JWE 압축 직렬화에서 설명한 것과 같은 의미를 갖습니다. JWE 토큰의 태그 매개변수는 AEAD 알고리즘을 사용한 암호화 프로세스의 결과인 JWE 인증 태그의 base64url 인코딩 값을 전달합니다.

## 암호화 과정(JSON 직렬화)

우리는 JSON 직렬화에서 JWE 토큰을 빌드하는 데 필요한 모든 구성 요소에 대해 논의했습니다. 다음은 JWE 토큰 빌드와 관련된 단계에 대해 설명합니다.

1. 콘텐츠 암호화 키(CEK) 값을 결정하는 데 사용되는 알고리즘으로 키 관리 모드를 파악합니다. 이 알고리즘은 JOSE 헤더의 ag 매개변수에 의해 정의됩니다. JWE JSON 직렬화에서 JOSE 헤더는 JWE 보호 헤더, JWE 공유 비보호 헤더 및 수신자별 비보호 헤더에 정의된 모든 매개변수의 통합으로 구축됩니다. 수신자별 비보호 헤더에 포함되면 수신자별로 ag 매개변수를 정의할 수 있습니다.

2. CEK를 계산하고 1단계에서 선택한 키 관리 모드를 기반으로 JWE 암호화 키를 계산합니다. CEK는 나중에 JSON 페이로드를 암호화하는 데 사용됩니다.

3. 2단계에서 생성된 JWE 암호화 키의 base64url 인코딩 값을 계산합니다. 다시 한 번 이는 수신자별로 계산되며 결과 값은 수신자별 비보호 헤더 매개변수인 암호화 키에 포함됩니다.


4. JWE 토큰의 각 수신자에 대해 1-3단계를 수행합니다. 각 반복은 JWE 토큰의 수신자 JSON 배열에 요소를 생성합니다.

5. JWE 초기화 벡터에 대한 임의 값을 생성합니다. 직렬화 기술에 관계없이 JWE 토큰은 JWE 초기화 벡터의 base64url 인코딩 값을 전달합니다.

6. 토큰 압축이 필요한 경우 zip 헤더 매개변수에 정의된 압축 알고리즘에 따라 일반 텍스트의 JSON 페이로드를 압축해야 합니다. zip 헤더 매개변수의 값은 JWE 보호 헤더 또는 JWE 공유 비보호 헤더에서 정의할 수 있습니다.

. JWE 보호 헤더, JWE 공유 비보호 헤더 및 수신자별 비보호 헤더의 JSON 표현을 구성합니다.

8. UTF-8 인코딩을 사용하여 JWE 보호 헤더의 base64url 인코딩 값을 계산합니다. 이 값은 직렬화된 JWE 토큰의 보호된 요소로 표시됩니다. JWE 보호 헤더는 선택 사항이며 존재하는 경우 하나의 헤더만 있을 수 있습니다. JWE 헤더가 없으면 보호된 요소의 값은 비어 있습니다.

9. AAD(추가 인증 데이터)에 대한 값을 생성하고 이 값의 base64url 인코딩 값을 계산합니다. 이것은 선택적 단계이며, 있는 경우 10단계에서와 같이 base64url로 인코딩된 AAD 값이 JSON 페이로드를 암호화하기 위한 입력 매개변수로 사용됩니다.

10. JSON 페이로드를 암호화하려면 CEK(이미 보유), JWE 초기화 벡터(이미 보유) 및 추가 인증 데이터(AAD)가 필요합니다. 인코딩된 JWE 보호 헤더의 ASCII 값을 계산하고(8단계) AAD로 사용합니다. 9단계가 완료되면 AAD의 값이 ASCII(인코딩된 JWE 보호 헤더. BASE64URL-ENCODE(AAD))로 계산됩니다.

11. enc 헤더 매개변수에 의해 정의된 콘텐츠 암호화 알고리즘에 따라 CEK, JWE 초기화 벡터 및 추가 인증 데이터(10단계의 AAD)를 사용하여 압축된 JSON 페이로드(6단계에서)를 암호화합니다.

12. enc 헤더 매개변수에 의해 정의된 알고리즘은 AEAD 알고리즘으로 암호화 과정을 거쳐 암호문과 인증 태그를 생성한다.

13. 12단계에서 생성된 암호문의 base64url 인코딩 값을 계산합니다.

14. 12단계에서 생성된 인증 태그의 base64url 인코딩 값을 계산합니다.

Now we have all the elements to build the JWE token under JSON serialization.

> **Note**
>
> The XML Encryption specification by W3C only talks about encrypting an XML payload. If you have to encrypt any content, then first you need to embed that within an XML payload and then encrypt. In contrast, the JWE specification is not just limited to JSON. You can encrypt any content with JWE without wrapping it inside a JSON payload.

### 중첩된 JWT

JWS 토큰과 JWE 토큰 모두에서 페이로드는 모든 콘텐츠가 될 수 있습니다. JSON, XML 또는 무엇이든 될 수 있습니다. 중첩 JWT에서 페이로드는 JWT 자체여야 합니다. 즉, 다른 JWS나 JWE 토큰으로 묶인 JWT가 Nested JWT를 빌드합니다. 중첩 JWT는 중첩 서명 및 암호화를 수행하는 데 사용됩니다. 중첩된 JWT의 경우 cty 헤더 매개변수가 존재하고 값 JWT로 설정되어야 합니다. 다음은 JWS를 사용하여 페이로드에 먼저 서명한 다음 JWE를 사용하여 JWS 토큰을 암호화하는 중첩 JWT를 빌드하는 단계를 나열합니다.

1. 선택한 페이로드 또는 콘텐츠로 JWS 토큰을 빌드합니다.

2. 사용하는 JWS 직렬화 기술을 기반으로 1단계에서는 JSON 직렬화를 사용하는 JSON 객체 또는 각 요소가 마침표(.)로 구분되는 3개 요소 문자열을 생성하며 컴팩트 직렬화를 사용합니다.

3. 2단계의 출력을 Base64url로 인코딩하고 이를 JWE 토큰에 대해 암호화할 페이로드로 사용합니다.

4. JWE JOSE 헤더의 cty 헤더 매개변수 값을 JWT로 설정합니다.

5. JWE 사양에 정의된 두 가지 직렬화 기술 중 하나에 따라 JWE를 빌드합니다.

> **Note**
>
> 서명 후 암호화는 중첩 JWT를 빌드할 때 서명 후 암호화 대신 선호되는 접근 방식입니다. 서명은 콘텐츠의 소유권을 서명자 또는 토큰 발행자에게 바인딩합니다. 암호화된 콘텐츠가 아닌 원본 콘텐츠에 서명하는 것이 업계에서 인정하는 모범 사례입니다. 또한 먼저 서명하고 서명된 페이로드를 암호화하면 서명 자체도 암호화되어 중간에 있는 공격자가 서명을 벗겨내는 것을 방지합니다. 서명 및 모든 관련 메타데이터가 암호화되기 때문에 공격자는 메시지를 보고 있는 토큰 발급자에 대한 세부 정보를 얻을 수 없습니다. 먼저 암호화하고 암호화된 페이로드에 서명하면 모든 사람이 서명을 볼 수 있으며 공격자가 메시지에서 이를 제거할 수도 있습니다.

> JWE 대 JWS
>
> 애플리케이션 개발자의 입장에서는 주어진 메시지가 JWE 토큰인지 JWS 토큰인지 식별하고 이를 기반으로 처리를 시작하는 것이 상당히 중요할 수 있습니다. 다음은 JWE 토큰과 JWS 토큰을 구별하는 데 사용할 수 있는 몇 가지 기술을 나열합니다.

1. 압축 직렬화가 사용되는 경우 JWS 토큰에는 마침표(.)로 구분된 3개의 base64url 인코딩 요소가 있는 반면 JWE 토큰에는 마침표(.)로 구분된 5개의 base64url 인코딩 요소가 있습니다.

2. JSON 직렬화를 사용하는 경우 생성되는 JSON 객체의 요소는 JWS 토큰과 JWE 토큰이 다릅니다. 예를 들어 JWS 토큰에는 JWE 토큰에 없는 페이로드라는 최상위 요소가 있고 JWE 토큰에는 JWS 토큰에 없는 암호문이라는 최상위 요소가 있습니다.

3. JWE 토큰의 JOSE 헤더에는 enc 헤더 매개변수가 있지만 JWS 토큰의 JOSE 헤더에는 없습니다.

4. JWS 토큰의 JOSE 헤더에 있는 alg 매개변수의 값은 디지털 서명 또는 MAC 알고리즘을 전달하거나 전혀 전달하지 않는 반면 JWE 토큰의 JOSE 헤더에 있는 동일한 매개변수는 키 암호화, 키 래핑, 직접 키 동의를 전달합니다. , 키 랩핑을 통한 키 계약 또는 직접 암호화 알고리즘.


### Generating a JWE Token with RSA-OAEP and AES with a JSON Payload

The following Java code generates a JWE token with RSA-OAEP and AES. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch08/sample01—and it runs on Java 8+. First you need to invoke the method generateKeyPair() and pass the PublicKey(generateKeyPair().getPublicKey()) into the method buildEncryptedJWT():
```
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
```
The following Java code shows how to invoke the previous two methods:
```
KeyPair keyPair = generateKeyPair();
buildEncryptedJWT(keyPair.getPublic());
```
To build and run the program, execute the following Maven command from the ch08/sample01 directory.
```
\> mvn test -Psample01
```
Let’s see how to decrypt a JWT encrypted by RSA-OAEP. You need to know the PrivateKey corresponding to the PublicKey used to encrypt the message:
```
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
```
The preceding code produces something similar to the following output:
```
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
```

### Generating a JWE Token with RSA-OAEP and AES with a Non-JSON Payload

The following Java code generates a JWE token with RSA-OAEP and AES for a non-JSON payload. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch08/sample02—and it runs on Java 8+. First you need to invoke the method generateKeyPair() and pass the PublicKey(generateKeyPair().getPublicKey()) into the method buildEncryptedJWT():

// this method generates a key pair and the corresponding public key is used 
// to encrypt the message.

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

To build and run the program, execute the following Maven command from the `ch08/sample02` directory.
```
\> mvn test -Psample02
```
### Generating a Nested JWT

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
```
\> mvn test -Psample03
```


## 요약

- JWE 사양은 암호화된 콘텐츠를 암호학적으로 안전한 방식으로 표현하는 방법을 표준화합니다.

- JWE는 암호화된 페이로드를 나타내기 위해 JWE 압축 직렬화와 JWE JSON 직렬화라는 두 가지 직렬화된 형식을 정의합니다.

- JWE 압축 직렬화에서 JWE 토큰은 JOSE 헤더, JWE 암호화 키, JWE 초기화 벡터, JWE 암호문 및 JWE 인증 태그와 같이 각각 마침표(.)로 구분된 5개의 구성 요소로 구성됩니다.

- JWE JSON 직렬화는 동일한 페이로드를 통해 여러 수신자를 대상으로 하는 암호화된 데이터를 생성할 수 있습니다.

- 중첩 JWT에서 페이로드는 JWT 자체여야 합니다. 즉, 다른 JWS나 JWE 토큰으로 묶인 JWT가 Nested JWT를 빌드합니다.

- 중첩된 JWT는 중첩된 서명 및 암호화를 수행하는 데 사용됩니다