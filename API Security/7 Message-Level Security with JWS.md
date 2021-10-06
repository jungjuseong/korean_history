# 7. Message-Level Security with JSON Web Signature

JSON은 언어 중립적이고 텍스트 기반이며 가벼운 방식으로 데이터를 교환하는 방법을 제공합니다. JSON 및 XML은 API에 가장 일반적으로 사용되는 데이터 교환 형식입니다. 지난 몇 년 동안의 추세를 살펴보면 JSON이 XML을 대체하고 있음이 매우 분명합니다. 대부분의 API는 JSON을 지원하고 일부는 JSON과 XML을 모두 지원합니다. XML 전용 API는 매우 드뭅니다.

## JSON 웹 토큰(JWT) 이해

JWT는 JSON으로 이해 당사자 간에 데이터를 전송하기 위한 컨테이너를 정의합니다. 2015년 5월 RFC 7519와 함께 IETF 표준이 되었습니다. 6장에서 논의한 OpenID Connect 사양은 JWT를 사용하여 ID 토큰을 나타냅니다. 예를 들어 Google API에서 반환된 OpenID Connect ID 토큰을 살펴보겠습니다(JWT를 이해하기 위해 OpenID Connect에 대해 알 필요는 없습니다).

eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc4YjRjZjIzNjU2ZGMzOTUzNjRmMWI2YzAyOTA3
NjkxZjJjZGZmZTEifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMT
EwNTAyMjUxMTU4OTIwMTQ3NzMyIiwiYXpwIjoiODI1MjQ5ODM1NjU5LXRlOHF
nbDcwMWtnb25ub21ucDRzcXY3ZXJodTEyMTFzLmFwcHMuZ29vZ2xldXNlcmNvb
nRlbnQuY29tIiwiZW1haWwiOiJwcmFiYXRoQHdzbzIuY29tIiwiYXRfaGFzaCI6InpmO
DZ2TnVsc0xCOGdGYXFSd2R6WWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkI
joiODI1MjQ5ODM1NjU5LXRlOHFnbDcwMWtnb25ub21ucDRzcXY3ZXJodTEyMTFz
LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaGQiOiJ3c28yLmNvbSIsImlhdCI6
MTQwMTkwODI3MSwiZXhwIjoxNDAxOTEyMTcxfQ.TVKv-pdyvk2gW8sGsCbsnkq
srS0T-H00xnY6ETkIfgIxfotvFn5IwKm3xyBMpy0FFe0Rb5Ht8AEJV6PdWyxz8rMgX
2HROWqSo_RfEfUpBb4iOsq4W28KftW5H0IA44VmNZ6zU4YTqPSt4TPhyFC9fP2D
_Hg7JQozpQRUfbWTJI

> **Note**
>
> JWT보다 훨씬 이전인 2009년에 Microsoft는 `SWT(Simple Web Token)`를 도입했습니다.1 이것은 JSON도 XML도 아닙니다. HTML 형식으로 인코딩된 이름/값 쌍 세트를 수행하기 위해 자체 토큰 형식을 정의했습니다. JWT와 SWT는 모두 애플리케이션 간에 클레임을 전달하는 방법을 정의합니다. SWT에서는 클레임 ​​이름과 클레임 값이 모두 문자열인 반면 JWT에서는 클레임 ​​이름이 문자열이지만 클레임 값은 모든 JSON 유형이 될 수 있습니다. 이러한 토큰 유형은 모두 `HMAC SHA256`이 포함된 `SWT` 및 서명, `MAC` 및 암호화 알고리즘을 포함한 알고리즘을 선택할 수 있는 JWT와 같은 콘텐츠에 대한 암호화 보호 기능을 제공합니다. `SWT`는 `IETF`에 대한 제안으로 개발되었지만 `IETF`가 제안한 표준이 된 적은 없습니다. Dick Hardt는 SWT 사양의 편집자였으며, 나중에 부록 B에서 논의하는 `OAuth WRAP` 사양을 구축하는 데에도 중요한 역할을 했습니다.

### JOSE Header

앞의 JWT에는 세 가지 주요 요소가 있습니다. 각 요소는 `base64url`로 인코딩되고 마침표(`.`)로 구분됩니다. 부록 E에서는 `base64url` 인코딩이 어떻게 작동하는지 자세히 설명합니다. JWT의 각 개별 요소를 식별해 보겠습니다. JWT의 첫 번째 요소는 `JOSE(JavaScript Object Signing and Encryption)` 헤더라고 합니다. JOSE 헤더는 JWT 클레임 세트에 적용된 암호화 작업과 관련된 속성을 나열합니다(이 장의 뒷부분에서 설명함). 다음은 이전 JWT의 base64url로 인코딩된 JOSE 헤더입니다.

```
eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc4YjRjZjIzNjU2ZGMzOTUzNjRmMWI2YzAyOTA3
NjkxZjJjZGZmZTEifQ
```

JOSE 헤더를 읽을 수 있도록 하려면 `base64url-decode`해야 합니다. 다음은 알고리즘(al`g)과 키 식별자(`kid`)라는 두 가지 속성을 정의하는 `base64url`로 디코딩된 JOSE 헤더를 보여줍니다.

```
{"alg":"RS256","kid":"78b4cf23656dc395364f1b6c02907691f2cdffe1"}
```

`alg` 및 `kid` 매개변수는 모두 JWT 사양에 정의되어 있지 않지만 `JWS(JSON Web Signature)` 사양에 정의되어 있습니다. 여기서 이러한 매개변수가 의미하는 바를 간단히 파악하고 JWS를 설명할 때 자세히 설명하겠습니다. JWT 사양은 특정 알고리즘에 구속되지 않습니다. 적용 가능한 모든 알고리즘은 RFC 7518인 JSON 웹 알고리즘(JWA) 사양에 따라 정의됩니다. RFC 7518의 섹션 3.1은 JWS 토큰에 대해 가능한 모든 `alg` 값을 정의합니다. `kid` 값은 메시지에 서명하는 데 사용되는 키에 대한 표시 또는 힌트를 제공합니다. 아이를 보면 메시지 수신자는 키를 찾고 찾을 위치를 알아야 합니다. JWT 사양은 JOSE 헤더에 두 개의 매개변수만 정의합니다. 다음은 그 목록입니다.

- `typ`: `typ`는 완전한 JWT의 미디어 유형을 정의하는 데 사용됩니다. 미디어 유형은 인터넷을 통해 전송되는 콘텐츠의 형식을 정의하는 식별자입니다. JWT를 처리하는 구성 요소에는 JWT 구현과 JWT 애플리케이션의 두 가지 유형이 있습니다. `Nimbus2`는 Java의 JWT 구현입니다. `Nimbus` 라이브러리는 JWT를 빌드하고 구문 분석하는 방법을 알고 있습니다. JWT 애플리케이션은 내부적으로 JWT를 사용하는 모든 것이 될 수 있습니다. JWT 애플리케이션은 JWT 구현을 사용하여 JWT를 빌드하거나 구문 분석합니다. typ 매개변수는 JWT 구현을 위한 또 다른 매개변수일 뿐입니다. 값을 해석하려고 시도하지 않지만 JWT 애플리케이션은 해석합니다. `typ`는 JWT가 아닌 값이 JWT 객체와 함께 애플리케이션 데이터 구조에도 존재할 수 있는 경우 JWT 애플리케이션이 JWT의 내용을 구별하는 데 도움이 됩니다. 이것은 선택적 매개변수이며 JWT에 대해 존재하는 경우 JWT를 미디어 유형으로 사용하는 것이 좋습니다.

- `cty`: `cty`는 JWT에 대한 구조적 정보를 정의하는 데 사용됩니다. 중첩된 JWT의 경우에만 이 매개변수를 사용하는 것이 좋습니다. 중첩된 JWT는 8장에서 설명하고 cty 매개변수의 정의는 8장에서 자세히 설명합니다.

### JWT Claims Set

JWT의 두 번째 요소는 `JWT 페이로드` 또는 `JWT 클레임 세트`로 알려져 있습니다. 비즈니스 데이터를 전달하는 JSON 객체입니다. 다음은 이전 JWT(Google API에서 반환됨)의 `base64url`로 인코딩된 JWT 클레임 집합입니다. 여기에는 인증된 사용자에 대한 정보가 포함됩니다.

```
eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTEwNTAyMjUxMTU4OT
IwMTQ3NzMyIiwiYXpwIjoiODI1MjQ5ODM1NjU5LXRlOHFnbDcwMWtnb25ub21uc
DRzcXY3ZXJodTEyMTFzLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1ha
WwiOiJwcmFiYXRoQHdzbzIuY29tIiwiYXRfaGFzaCI6InpmODZ2TnVsc0xCOGdGYX
FSd2R6WWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkIjoiODI1MjQ5ODM1NjU
```

JWT 클레임을 읽을 수 있도록 설정하려면 base64url 디코딩해야 합니다. 다음은 base64url로 디코딩된 JWT 클레임 집합을 보여줍니다. JWT 클레임 세트를 빌드하는 동안 공백을 명시적으로 유지할 수 있습니다. `base64url` 인코딩 전에 정규화가 필요하지 않습니다. 정규화는 다양한 형식의 메시지를 단일 표준 형식으로 변환하는 프로세스입니다. 이것은 주로 XML 메시지에 서명하기 전에 사용됩니다. XML에서 동일한 메시지는 동일한 의미를 전달하기 위해 다른 형식으로 표시될 수 있습니다. 예를 들어, <vehicles><car></car></vehicles> 및 <vehicles><car/></vehicles>는 의미가 동일하지만 두 가지 다른 표준 형식이 있습니다. XML 메시지에 서명하기 전에 정규화 알고리즘을 따라 표준 양식을 작성해야 합니다.
```
{
     "iss":"accounts.google.com",
     "sub":"110502251158920147732",
     "azp":"825249835659-te8qgl701kgonnomnp4sqv7erhu1211s.apps.googleusercontent.com",
     "email":"prabath@wso2.com",
     "at_hash":"zf86vNulsLB8gFaqRwdzYg",
     "email_verified":true,
     "aud":"825249835659-te8qgl701kgonnomnp4sqv7erhu1211s.apps.googleusercontent.com",
     "hd":"wso2.com",
     "iat":1401908271,
     "exp":1401912171
}
```

JWT 클레임 집합은 JWT 발급자가 주장한 클레임이 구성원인 JSON 개체를 나타냅니다. JWT 내의 각 클레임 이름은 고유해야 합니다. 중복 클레임 이름이 있는 경우 JWT 파서는 구문 분석 오류를 반환하거나 가장 마지막 중복 클레임으로 설정된 클레임을 반환할 수 있습니다. JWT 사양은 필수 클레임과 선택 클레임을 명시적으로 정의하지 않습니다. 필수 및 선택적 클레임을 정의하는 것은 JWT의 각 애플리케이션에 달려 있습니다. 예를 들어, 6장에서 자세히 논의한 OpenID Connect 사양은 필수 및 선택적 클레임을 정의합니다.

JWT 사양은 등록된 클레임, 공개 클레임 및 비공개 클레임의 세 가지 클레임 클래스를 정의합니다. 등록된 클레임은 IANA(Internet Assigned Numbers Authority) JSON 웹 토큰 클레임 레지스트리에 등록됩니다. 이러한 클레임은 등록된 클레임으로 처리되지만 JWT 사양에서는 사용을 의무화하지 않습니다. 필수 항목과 그렇지 않은 항목을 결정하는 것은 JWT 위에 구축된 다른 사양에 달려 있습니다. 예를 들어, OpenID Connect 사양에서 iss는 필수 클레임입니다. 다음은 JWT 사양에 정의된 등록된 클레임 집합을 나열합니다.

- iss(발급자): JWT의 발행자. 이것은 대소문자를 구분하는 문자열 값으로 처리됩니다. 이상적으로는 클레임 ​​집합의 주장 당사자를 나타냅니다. Google에서 JWT를 발행하는 경우 iss의 값은 accounts.google.com이 됩니다. 이것은 JWT의 발행자가 누구인지를 받는 당사자에게 표시됩니다.

- sub(subject): 토큰 발행자 또는 주장 당사자가 특정 엔터티에 대해 JWT를 발급하고 JWT에 포함된 클레임 집합은 일반적으로 sub 매개변수로 식별되는 이 엔터티를 나타냅니다. sub 매개변수의 값은 대소문자를 구분하는 문자열 값입니다.

- aud: 토큰 발행자는 JWT를 의도된 수신자 또는 aud 매개변수로 표시되는 수신자 목록에 발행합니다. 수신자 또는 수신자 목록은 JWT를 구문 분석하고 유효성을 검사하는 방법을 알고 있어야 합니다. 유효성 검사를 하기 전에 먼저 특정 JWT가 사용을 위해 발급되었는지 확인하고 그렇지 않은 경우 즉시 거부해야 합니다. aud 매개변수의 값은 대소문자를 구분하는 문자열 값 또는 문자열 배열일 수 있습니다. 토큰 발행자는 토큰을 발행하기 전에 토큰의 의도된 수신자(또는 수신자)가 누구인지 알아야 하며 aud 매개변수의 값은 토큰 발행자와 수신자 간에 사전 합의된 값이어야 합니다. 실제로는 정규식을 사용하여 토큰 대상의 유효성을 검사할 수도 있습니다. 예를 들어, 토큰의 aud 값은 \*.apress.com일 수 있는 반면 apress.com 도메인 아래의 각 수신자는 foo.apress.com, bar.apress.com과 같은 고유한 aud 값을 가질 수 있습니다. aud 값과 정확히 일치하는 항목을 찾는 대신 각 수신자는 aud 값이 정규식과 일치하는지 확인할 수 있습니다. (?:[a-zA-Z0-9]*|\*).apress.com. 이렇게 하면 모든 수신자가 apress.com의 하위 도메인이 있는 JWT를 사용할 수 있습니다.

- exp (expiration time): Each JWT carries an expiration time. The recipient of the JWT token must reject it, if that token has expired. The issuer can decide the value of the expiration time. The JWT specification does not recommend or provide any guidelines on how to decide the best token expiration time. It’s a responsibility of the other specifications, which use JWT internally to provide such recommendations. The value of the exp parameter is calculated by adding the expiration time (from the token issued time) in seconds to the time elapsed from 1970-01-01T00:00:00Z UTC to the current time. If the token issuer’s clock is out of sync with the recipient’s clock (irrespective of their time zone), then the expiration time validation could fail. To fix that, each recipient can add a couple of minutes as the clock skew during the validation process.

- nbf(이전 아님): nbf 매개변수의 값이 현재 시간보다 큰 경우 토큰 수신자는 토큰을 거부해야 합니다. JWT는 nbf 매개변수에 표시된 값 이전에 사용하기에 충분하지 않습니다. nbf 매개변수의 값은 1970-01-01T00:00:00Z UTC에서 이전 시간까지 경과된 시간(초)입니다.

- iat(발급 시점): JWT의 iat 매개변수는 토큰 발행자가 계산한 JWT 발행 시간을 나타냅니다. iat 매개변수의 값은 1970-01-01T00:00:00Z UTC부터 토큰이 발행된 현재 시간까지 경과된 시간(초)입니다.

- jti(JWT ID): JWT의 jti 매개변수는 토큰 발행자가 생성한 고유한 토큰 식별자입니다. 토큰 수신자가 여러 토큰 발행자의 JWT를 수락하는 경우 이 값은 모든 발행자에서 고유하지 않을 수 있습니다. 이 경우 토큰 수신자는 토큰 발행자 아래에 토큰을 유지하여 토큰 고유성을 유지할 수 있습니다. 토큰 발행자 식별자 + jti의 조합은 고유한 토큰 식별자를 생성해야 합니다.

공개 클레임은 JWT를 기반으로 구축된 다른 사양에 의해 정의됩니다. 이러한 경우 충돌을 방지하려면 이름을 IANA JSON 웹 토큰 청구 레지스트리에 등록하거나 적절한 네임스페이스를 사용하여 충돌 방지 방식으로 정의해야 합니다. 예를 들어, OpenID Connect 사양은 ID 토큰(ID 토큰 자체가 JWT임) 내에 포함된 자체 클레임 집합을 정의하고 이러한 클레임은 IANA JSON 웹 토큰 클레임 레지스트리에 등록됩니다.

비공개 클레임은 실제로 비공개여야 하며 주어진 토큰 발행자와 선택된 수신자 사이에서만 공유되어야 합니다. 이러한 주장은 충돌 가능성이 있으므로 주의해서 사용해야 합니다. 주어진 수신자가 여러 토큰 발행자의 토큰을 수락하는 경우 개인 청구인 경우 동일한 청구의 의미 체계가 발행자마다 다를 수 있습니다.

### JWT Signature

JWT의 세 번째 부분은 `base64url`로 인코딩된 서명입니다. 서명과 관련된 암호화 매개변수는 JOSE 헤더에 정의됩니다. 이 특정 예에서 Google은 JOSE 헤더의 `alg` 매개변수 값인 `RS256`으로 표현되는 `SHA256` 해싱 알고리즘과 함께 `RSASSA-PKCS1-V1_53`을 사용합니다. 다음은 Google에서 반환된 JWT의 서명 요소를 보여줍니다. 서명 자체는 사람이 읽을 수 없으므로 다음을 `base64url` 디코딩 하려고 시도할 필요가 없습니다.

`TVKv-pdyvk2gW8sGsCbsnkqsrS0TH00xnY6ETkIfgIxfotvFn5IwKm3xyBMpy0
FFe0Rb5Ht8AEJV6PdWyxz8rMgX2HROWqSo_RfEfUpBb4iOsq4W28KftW5
H0IA44VmNZ6zU4YTqPSt4TPhyFC-9fP2D_Hg7JQozpQRUfbWTJI`

### Generating a Plaintext JWT

The plaintext JWT doesn’t have a signature. It has only two parts. The value of the alg parameter in the JOSE header must be set to none. The following Java code generates a plaintext JWT. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample01.
```
public static String buildPlainJWT() {
    // build audience restriction list.
    List<String> aud = new ArrayList<String>();
    aud.add("https://app1.foo.com");
    aud.add("https://app2.foo.com");
    Date currentTime = new Date();
    // create a claims set.
    JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().
        // set the value of the issuer.
        issuer("https://apress.com").
        // set the subject value - JWT belongs to // this subject.
        subject("john").
        // set values for audience restriction.
        audience(aud).
        // expiration time set to 10 minutes.
        expirationTime(new Date(new Date().getTime() + 1000 - 60 - 10)).
        // set the valid from time to current time.
        notBeforeTime(currentTime).
        // set issued time to current time.
        issueTime(currentTime).
        // set a generated UUID as the JWT // identifier.
        jwtID(UUID.randomUUID().toString()).
        build();

    // create plaintext JWT with the JWT claims.
    PlainJWT plainJwt = new PlainJWT(jwtClaims);

    // serialize into string.
    String jwtInText = plainJwt.serialize();

    // print the value of the JWT.
    System.out.println(jwtInText);
    return jwtInText;
}
```
To build and run the program, execute the following Maven command from the ch07/sample01 directory.

\> mvn test -Psample01

앞의 코드는 JWT인 다음 출력을 생성합니다. 코드를 반복해서 실행하면 프로그램을 실행할 때마다 currentTime 변수의 값이 변경되므로 동일한 출력을 얻지 못할 수 있습니다.

`eyJhbGciOiJub25lIn0.eyJleHAiOjE0MDIwMzcxNDEsInN1YiI6ImpvaG4iLCJuYm
YiOjE0MDIwMzY1NDEsImF1ZCI6WyJodHRwczpcL1wvYXBwMS5mb28uY29tIi
wiaHR0cHM6XC9cL2FwcDIuZm9vLmNvbSJdLCJpc3MiOiJodHRwczpcL1wvYX
ByZXNzLmNvbSIsImp0aSI6IjVmMmQzM2RmLTEyNDktNGIwMS04MmYxLWJl
MjliM2NhOTY4OSIsImlhdCI6MTQwMjAzNjU0MX0.`

The following Java code shows how to parse a base64url-encoded JWT. This code would ideally run at the JWT recipient end:

```
public static PlainJWT parsePlainJWT() throws ParseException {
        // get JWT in base64url-encoded text.
        String jwtInText = buildPlainJWT();
        // build a plain JWT from the bade64url-encoded text.
        PlainJWT plainJwt  = PlainJWT.parse(jwtInText);
        // print the JOSE header in JSON.
        System.out.println(plainJwt.getHeader().toString());
        // print JWT body in JSON.
        System.out.println(plainJwt.getPayload().toString());

        return plainJwt;
}
```

This code produces the following output, which includes the parsed JOSE header and the payload:
```
{"alg":"none"}
{
   "exp":1402038339,
   "sub":"john",
   "nbf":1402037739,
   "aud":["https:\/\/app1.foo.com","https:\/\/app2.foo.com"],
   "iss":"https:\/\/apress.com",
   "jti":"1e41881f-7472-4030-8132-856ccf4cbb25",
   "iat":1402037739
}
```

> Jose Working Group
>
> OAuth 작업 그룹과 SCIM(System for Cross-domain Identity Management) 작업 그룹을 포함하여 IETF 내의 많은 작업 그룹이 JSON으로 직접 작업합니다. SCIM 작업 그룹은 JSON을 기반으로 프로비저닝 표준을 구축하고 있습니다. IETF 외부에서 OASIS XACML 작업 그룹은 XACML 3.0용 JSON 프로필을 구축하기 위해 노력하고 있습니다.
>
>OpenID Foundation에서 개발한 OpenID Connect 사양도 JSON을 기반으로 합니다. JSON을 중심으로 구축된 표준의 등장과 API에서 데이터 교환을 위해 JSON을 많이 사용함에 따라 메시지 수준에서 JSON 메시지를 보호하는 방법을 정의하는 것이 절대적으로 필요하게 되었습니다. TLS(전송 계층 보안)를 사용하면 전송 계층에서만 기밀성과 무결성을 제공합니다. IETF 아래에 형성된 JOSE 작업 그룹은 무결성 보호 및 기밀성, 키 및 알고리즘 식별자 형식을 표준화하여 JSON을 사용하는 프로토콜에 대한 보안 서비스의 상호 운용성을 지원하는 것을 목표로 합니다. JSON 웹 서명(RFC 7515), JSON 웹 암호화(RFC 7516), JSON 웹 키(RFC 7517) 및 JSON 웹 알고리즘(RFC 7518)은 JOSE 작업 그룹에서 개발된 4가지 IETF 제안 표준입니다.

### JSON Web Signature (JWS)

IETF JOSE 작업 그룹에서 개발된 JSON 웹 서명(JWS) 사양은 디지털 서명 또는 MAC(해싱 알고리즘이 HMAC와 함께 사용되는 경우)된 메시지 또는 페이로드를 나타냅니다. 서명된 메시지는 JWS 사양(JWS 압축 직렬화 및 JWS JSON 직렬화)에 따라 두 가지 방법으로 직렬화할 수 있습니다. 이 장의 시작 부분에서 설명하는 Google OpenID Connect 예제에서는 JWS 압축 직렬화를 사용합니다. 사실, OpenID Connect 사양은 필요할 때마다 JWS 압축 직렬화와 JWE 압축 직렬화를 사용하도록 규정하고 있습니다(JWE는 8장에서 논의합니다). JWS 토큰이라는 용어는 JWS 사양에 정의된 직렬화 기술에 따라 직렬화된 페이로드 형식을 나타내는 데 사용됩니다.

> **Note**
>
> JSON Web Tokens (JWTs) are always serialized with the JWS compact serialization or the JWE compact serialization. We discuss JWE (JSON Web Encryption) in Chapter 8.

#### JWS Compact Serialization

JWS compact serialization represents a signed JSON payload as a compact URL-safe string. This compact string has three main elements separated by periods (.): the JOSE header, the JWS payload, and the JWS signature (see Figure 7-1). If you use compact serialization against a JSON payload, then you can have only a single signature, which is computed over the complete JOSE header and JWS payload.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_7_Fig1_HTML.jpg)

Figure 7-1 A JWS token with compact serialization

#### JOSE Header

JWS 사양은 JOSE 헤더에 11개의 매개변수를 도입합니다. 다음은 메시지 서명과 관련된 JOSE 헤더에 포함된 매개변수를 나열합니다. 이러한 모든 매개변수 중에서 JWT 사양은 typ 및 cty 매개변수만 정의합니다(이전에 논의한 대로). 나머지는 JWS 사양에 의해 정의됩니다. JWS 토큰의 JOSE 헤더는 JWS 토큰 수신자가 서명을 적절하게 검증하는 데 필요한 모든 매개변수를 전달합니다.

- alg(알고리즘): JSON 페이로드에 서명하는 데 사용되는 알고리즘의 이름입니다. 이것은 JOSE 헤더의 필수 속성입니다. 헤더에 이것을 포함하지 않으면 토큰 구문 분석 오류가 발생합니다. alg 매개변수의 값은 JSON 웹 알고리즘(JWA) 사양에 정의된 JSON 웹 서명 및 암호화 알고리즘 레지스트리에서 선택되는 문자열입니다. lg 매개변수의 값이 이전 레지스트리에서 선택되지 않은 경우 충돌 방지 방식으로 정의되어야 하지만 특정 알고리즘이 모든 JWS 구현에서 식별된다는 보장은 없습니다. JWA 사양에 정의된 알고리즘을 고수하는 것이 항상 더 좋습니다.

- jku: JOSE 헤더의 jku 매개변수는 JWK(JSON Web Key) 세트를 가리키는 URL을 전달합니다. 이 JWK 세트는 JSON으로 인코딩된 공개 키 모음을 나타내며, 여기서 키 중 하나는 JSON 페이로드에 서명하는 데 사용됩니다. 키 세트를 검색하는 데 사용되는 프로토콜이 무엇이든 무결성 보호를 제공해야 합니다. HTTP를 통해 키를 검색하는 경우 일반 HTTP 대신 HTTPS(또는 TLS를 통한 HTTP)를 사용해야 합니다. 부록 C에서 TLS(전송 계층 보안)에 대해 자세히 설명합니다. jku는 선택적 매개변수입니다.

- jwk: JOSE 헤더의 jwk 매개변수는 JSON 페이로드에 서명하는 데 사용되는 키에 해당하는 공개 키를 나타냅니다. 키는 JWK(JSON Web Key) 사양에 따라 인코딩됩니다. 이전에 논의한 jku 매개변수는 JWK 세트를 보유하는 링크를 가리키는 반면 jwk 매개변수는 JOSE 헤더 자체에 키를 포함합니다. jwk는 선택적 매개변수입니다.

- kid: JOSE 헤더의 kid 매개변수는 JSON 페이로드에 서명하는 데 사용되는 키의 식별자를 나타냅니다. 이 식별자를 사용하여 JWS의 수신자는 키를 찾을 수 있어야 합니다. 토큰 발행자가 JOSE 헤더의 kid 매개변수를 사용하여 수신자에게 서명 키에 대해 알린다면 해당 키가 토큰 발행자와 수신자 간에 "어쨌든" 교환되어야 합니다. 이 키 교환이 발생하는 방법은 JWS 사양의 범위를 벗어납니다. kid 매개변수의 값이 JWK를 참조하는 경우 이 매개변수의 값은 JWK의 kid 매개변수 값과 일치해야 합니다. kid는 JOSE 헤더의 선택적 매개변수입니다.

- x5u: JOSE 헤더의 x5u 매개변수는 이전에 논의한 jku 매개변수와 매우 유사합니다. 여기에서 URL은 JWK 세트를 가리키는 대신 X.509 인증서 또는 X.509 인증서 체인을 가리킵니다. URL이 가리키는 리소스는 PEM 인코딩 형식의 인증서 또는 인증서 체인을 보유해야 합니다. 체인의 각 인증서는 구분자4 사이에 나타나야 합니다. -----BEGIN CERTIFICATE----- 및 -----END CERTIFICATE-----. JSON 페이로드 서명에 사용된 키에 해당하는 공개 키는 인증서 체인의 맨 처음 항목이어야 하며 나머지는 중간 CA(인증 기관)와 루트 CA의 인증서입니다. x5u는 JOSE 헤더의 선택적 매개변수입니다.

- x5c: JOSE 헤더의 x5c 매개변수는 JSON 페이로드에 서명하는 데 사용되는 개인 키에 해당하는 X.509 인증서(또는 인증서 체인)를 나타냅니다. 이것은 이전에 논의한 jwk 매개변수와 유사하지만 이 경우 JWK 대신 X.509 인증서(또는 인증서 체인)입니다. 인증서 또는 인증서 체인은 인증서 값 문자열의 JSON 배열로 표시됩니다. 배열의 각 요소는 base64로 인코딩된 DER PKIX 인증서 값이어야 합니다. JSON 페이로드 서명에 사용된 키에 해당하는 공개 키는 JSON 배열의 맨 처음 항목이어야 하며 나머지는 중간 CA(인증 기관)와 루트 CA의 인증서입니다. x5c는 JOSE 헤더의 선택적 매개변수입니다.

- x5t: JOSE 헤더의 x5t 매개변수는 JSON 페이로드에 서명하는 데 사용된 키에 해당하는 X.509 인증서의 base64url로 인코딩된 SHA-1 지문을 나타냅니다. 이것은 우리가 이전에 논의한 kid 매개변수와 유사합니다. 이 두 매개변수는 키를 찾는 데 사용됩니다. 토큰 발행자가 JOSE 헤더의 x5t 매개변수를 사용하여 수신자에게 서명 키에 대해 알린다면 해당 키는 사전에 토큰 발행자와 수신자 간에 "어떻게든" 교환되어야 합니다. 이 키 교환이 발생하는 방법은 JWS 사양의 범위를 벗어납니다. x5t는 JOSE 헤더의 선택적 매개변수입니다.

- x5t#s256: JOSE 헤더의 x5t#s256 매개변수는 JSON 페이로드 서명에 사용된 키에 해당하는 X.509 인증서의 base64url로 인코딩된 SHA256 지문을 나타냅니다. x5t#s256과 x5t의 유일한 차이점은 해싱 알고리즘입니다. x5t#s256은 JOSE 헤더의 선택적 매개변수입니다.


- typ: The typ parameter in the JOSE header is used to define the media type of the complete JWS. There are two types of components that process a JWS: JWS implementations and JWS applications. Nimbus5 is a JWS implementation in Java. The Nimbus library knows how to build and parse a JWS. A JWS application can be anything, which uses JWS internally. A JWS application uses a JWS implementation to build or parse a JWS. In this case, the typ parameter is just another parameter for the JWS implementation. It will not try to interpret the value of it, but the JWS application would. The typ parameter will help JWS applications to differentiate the content when multiple types of objects are present. For a JWS token using JWS compact serialization and for a JWE token using JWE compact serialization, the value of the typ parameter is JOSE, and for a JWS token using JWS JSON serialization and for a JWE token using JWE JSON serialization, the value is JOSE+JSON. (JWS serialization is discussed later in this chapter, and JWE serialization is discussed in Chapter 8). The typ is an optional parameter in the JOSE header.

- cty: cty는 JWS에서 보안 콘텐츠의 미디어 유형을 나타내는 데 사용됩니다. 중첩된 JWT의 경우에만 이 매개변수를 사용하는 것이 좋습니다. 중첩된 JWT는 8장 뒷부분에서 설명하고 cty 매개변수의 정의는 거기에서 더 자세히 설명합니다. cty는 JOSE 헤더의 선택적 매개변수입니다.

- crit: crit는 JWS 또는 JWA 사양에 의해 정의되지 않은 사용자 정의 매개변수가 JOSE 헤더에 있음을 JWS 수신자에게 표시하는 데 사용됩니다. 수신자가 이러한 사용자 정의 매개변수를 이해하지 못하는 경우 JWS 토큰은 유효하지 않은 것으로 처리됩니다. crit 매개변수의 값은 이름의 JSON 배열이며, 여기서 각 항목은 사용자 정의 매개변수를 나타냅니다. crit는 JOSE 헤더의 선택적 매개변수입니다.

앞서 정의한 11개의 모든 매개변수 중 7개는 JSON 페이로드에 서명하는 데 사용되는 키에 해당하는 공개 키를 참조하는 방법에 대해 이야기합니다. 키를 참조하는 방법에는 외부 참조, 내장 및 키 식별자의 세 가지가 있습니다. jku 및 x5u 매개변수는 외부 참조 범주에 속합니다. 둘 다 URI를 통해 키를 참조합니다. jwk 및 x5c 매개변수는 포함된 참조 범주에 속합니다. 각각은 JOSE 헤더 자체에 키를 포함하는 방법을 정의합니다. kid, x5t 및 x5t#s256 매개변수는 키 식별자 참조 범주에 속합니다. 세 가지 모두 식별자를 사용하여 키를 찾는 방법을 정의합니다. 그런 다음 다시 모든 7개의 매개변수는 키 표현에 따라 JSON 웹 키(JWK) 및 X.509의 두 가지 범주로 더 나눌 수 있습니다. jku, jwk 및 kid는 JWK 범주에 속하고 x5u, x5c, x5t 및 x5t#s256은 X.509 범주에 속합니다. 주어진 JWS 토큰의 JOSE 헤더에서 주어진 시간에 앞의 매개변수 중 하나만 있으면 됩니다.

> **Note**
>
> JOSE 헤더에 jku, jwk, kid, x5u, x5c, x5t, x5t#s256 중 하나라도 있으면 무결성을 보호해야 합니다. 그렇게 하지 않으면 공격자가 메시지에 서명하는 데 사용되는 키를 수정하고 메시지 페이로드의 내용을 변경할 수 있습니다. JWS 토큰의 서명을 검증한 후 수신자 애플리케이션은 서명과 연관된 키가 신뢰할 수 있는지 여부를 확인해야 합니다. 받는 사람이 해당 키를 알고 있는지 확인하면 신뢰 유효성 검사를 수행할 수 있습니다.

JWS 사양은 이전에 정의된 11개의 헤더 매개변수만 사용하도록 애플리케이션을 제한하지 않습니다. 새 헤더 매개변수를 도입하는 방법에는 공개 헤더 이름과 개인 헤더 이름의 두 가지가 있습니다. 공용 공간에서 사용하려는 모든 헤더 매개변수는 충돌 방지 방식으로 도입되어야 합니다. IANA JSON 웹 서명 및 암호화 헤더 매개변수 레지스트리에 이러한 공개 헤더 매개변수를 등록하는 것이 좋습니다. 개인 헤더 매개변수는 주로 토큰 발행자와 수신자가 서로를 잘 알고 있는 제한된 환경에서 사용됩니다. 이러한 매개변수는 충돌 가능성이 있으므로 주의해서 사용해야 합니다. 주어진 수신자가 여러 토큰 발행자의 토큰을 수락하면 동일한 매개변수의 의미가 개인 헤더인 경우 발행자마다 다를 수 있습니다. 공개 또는 비공개 헤더 매개변수이든 JWS 또는 JWA 사양에 정의되어 있지 않은 경우 이전에 논의한 crit 헤더 매개변수에 헤더 이름이 포함되어야 합니다.

#### JWS Payload

The JWS payload is the message that needs to be signed. The message can be anything—need not be a JSON payload. If it is a JSON payload, then it could contain whitespaces and/or line breaks before or after any JSON value. The second element of the serialized JWS token carries the base64url-encoded value of the JWS payload.

#### JWS Signature

The JWS signature is the digital signature or the MAC, which is calculated over the JWS payload and the JOSE header. The third element of the serialized JWS token carries the base64url-encoded value of the JWS signature.

#### The Process of Signing (Compact Serialization)

압축 직렬화에서 JWS 토큰을 빌드하는 데 필요한 모든 구성 요소에 대해 논의했습니다. 다음은 JWS 토큰 빌드와 관련된 단계에 대해 설명합니다. JWS 토큰에는 세 가지 요소가 있습니다. 첫 번째 요소는 단계 2에서 생산되고 두 번째 요소는 단계 4에서 생산되며 세 번째 요소는 단계 7에서 생산됩니다.

1. JWS 토큰의 암호화 속성을 표현하는 모든 헤더 매개변수를 포함하는 JSON 객체를 작성합니다. 이를 JOSE 헤더라고 합니다. 이 장의 앞부분에서 논의한 바와 같이 "JOSE 헤더" 섹션에서 토큰 발행자는 메시지 서명에 사용된 키에 해당하는 공개 키를 JOSE 헤더에 광고해야 합니다. 이는 jku, jwk, kid, x5u, x5c, x5t 및 x5t#s256과 같은 헤더 매개변수를 통해 표현할 수 있습니다.

2. 1단계의 UTF-8 인코딩 JOSE 헤더에 대해 base64url 인코딩 값을 계산하여 JWS 토큰의 첫 번째 요소를 생성합니다.

3. 페이로드 또는 서명할 콘텐츠를 구성합니다. 이를 JWS 페이로드라고 합니다. 페이로드는 JSON일 필요는 없으며 모든 콘텐츠가 될 수 있습니다.

4. 3단계에서 JWS 페이로드의 base64url 인코딩 값을 계산하여 JWS 토큰의 두 번째 요소를 생성합니다.

5. 디지털 서명 또는 MAC을 계산하는 메시지를 작성합니다. 메시지는 ASCII(BASE64URL-ENCODE(UTF8(JOSE Header)) .BASE64URL-ENCODE(JWS Payload))로 구성됩니다.

6. JOSE 헤더 매개변수 alg에 의해 정의된 서명 알고리즘에 따라 5단계에서 구성된 메시지에 대한 서명을 계산합니다. 메시지는 JOSE 헤더에 알려진 공개 키에 해당하는 개인 키를 사용하여 서명됩니다.

7. 직렬화된 JWS 토큰의 세 번째 요소인 6단계에서 생성된 JWS 서명의 base64url 인코딩 값을 계산합니다.

8. 이제 다음과 같은 방식으로 JWS 토큰을 빌드하기 위한 모든 요소가 있습니다. 줄 바꿈은 명확성을 위해서만 도입되었습니다.

BASE64URL(UTF8(JWS Protected Header)).
BASE64URL(JWS Payload).
BASE64URL(JWS Signature)

#### JWS JSON Serialization

JWS 압축 직렬화와 달리 JWS JSON 직렬화는 다른 JOSE 헤더 매개변수와 함께 동일한 JWS 페이로드에 대해 여러 서명을 생성할 수 있습니다. JWS JSON 직렬화에서 궁극적으로 직렬화된 형식은 모든 관련 메타데이터와 함께 서명된 페이로드를 JSON 객체로 래핑합니다. 이 JSON 개체에는 두 개의 최상위 요소인 페이로드 및 서명과 서명 요소 아래에 있는 세 개의 하위 요소인 보호, 헤더 및 서명이 포함됩니다. 다음은 JWS JSON 직렬화로 직렬화한 JWS 토큰의 예이다. 이것은 URL에 안전하지도 않고 압축에 최적화되어 있지도 않습니다. 동일한 페이로드에 대해 두 개의 서명을 전달하고 각 서명과 그 주변의 메타데이터는 서명 최상위 요소 아래에 있는 JSON 배열의 요소로 저장됩니다. 각 서명은 해당하는 kid 헤더 매개변수로 표시되는 다른 키를 사용하여 서명합니다. JSON 직렬화는 JOSE 헤더 매개변수에 선택적으로 서명하는 데에도 유용합니다. 대조적으로 JWS 압축 직렬화는 완전한 JOSE 헤더에 서명합니다.

{
"payload":"eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzOD",
    "signatures":[
        {
            "protected":"eyJhbGciOiJSUzI1NiJ9",
            "header":{"kid":"2014-06-29"},
            "signature":"cC4hiUPoj9Eetdgtv3hF80EGrhuB"
        },
        {
            "protected":"eyJhbGciOiJFUzI1NiJ9",
            "header":{"kid":"e909097a-ce81-4036-9562-d21d2992db0d"},
            "signature":"DtEhU3ljbEg8L38VWAfUAqOyKAM"
        }
    ]
}

#### JWS Payload

The payload top-level element of the JSON object includes the base64url-encoded value of the complete JWS payload. The JWS payload necessarily need not be a JSON payload, it can be of any content type. The payload is a required element in the serialized JWS token.

#### JWS 보호 헤더

JWS 보호 헤더는 서명 또는 MAC 알고리즘으로 무결성을 보호해야 하는 헤더 매개변수를 포함하는 JSON 객체입니다. 직렬화된 JSON 형식의 protected 매개변수는 JWS 보호 헤더의 base64url 인코딩 값을 나타냅니다. protected는 직렬화된 JWS 토큰의 최상위 요소가 아닙니다. 서명 JSON 배열의 요소를 정의하는 데 사용되며 서명해야 하는 base64url 인코딩 헤더 요소를 포함합니다. 앞의 코드 조각에서 첫 번째 보호된 요소의 값을 base64url-decode하면 {"alg":"RS256"}이 표시됩니다. 보호된 헤더 매개변수가 있는 경우 보호된 매개변수가 있어야 합니다. 서명 JSON 배열의 각 항목에 대해 하나의 보호된 요소가 있습니다.

#### JWS 비보호 헤더

JWS 비보호 헤더는 서명 또는 MAC 알고리즘에 의해 무결성이 보호되지 않는 헤더 매개변수를 포함하는 JSON 객체입니다. 직렬화된 JSON 형식의 헤더 매개변수는 JWS 비보호 헤더의 base64url 인코딩 값을 나타냅니다. 헤더는 JSON 객체의 최상위 매개변수가 아닙니다. 서명 JSON 배열의 요소를 정의하는 데 사용됩니다. header 매개변수에는 해당 서명과 관련된 보호되지 않은 헤더 요소가 포함되며 이러한 요소는 서명되지 않습니다. 보호된 헤더와 보호되지 않은 헤더를 결합하면 궁극적으로 서명에 해당하는 JOSE 헤더가 파생됩니다. 앞의 코드 조각에서 서명 JSON 배열의 첫 번째 항목에 해당하는 완전한 JOSE 헤더는 {"alg":"RS256", "kid":"2010-12-29"}입니다. 헤더 요소는 JSON 객체로 표시되며 보호되지 않은 헤더 매개변수가 있는 경우 반드시 존재해야 합니다. 서명 JSON 배열의 각 항목에 대해 하나의 헤더 요소가 있습니다.

#### JWS Signature

The signatures parameter of the JSON object includes an array of JSON objects, where each element includes a signature or MAC (over the JWS payload and JWS protected header) and the associated metadata. This is a required parameter. The signature subelement, which is inside each entry of the signatures array, carries the base64url-encoded value of the signature computed over the protected header elements (represented by the protected parameter) and the JWS payload. Both the signatures and signature are required parameters.

> **Note**
>
> Even though JSON serialization provides a way to selectively sign JOSE header parameters, it does not provide a direct way to selectively sign the parameters in the JWS payload. Both forms of serialization mentioned in the JWS specification sign the complete JWS payload. There is a workaround for this using JSON serialization. You can replicate the payload parameters that need to be signed selectively in the JOSE header. Then with JSON serialization, header parameters can be selectively signed.

#### The Process of Signing (JSON Serialization)

We discussed about all the ingredients that are required to build a JWS token under JSON serialization. The following discusses the steps involved in building the JWS token.

1. Construct the payload or the content to be signed—this is known as the JWS payload. The payload is not necessarily JSON—it can be any content. The payload element in the serialized JWS token carries the base64url-encoded value of the content.

2. Decide how many signatures you would need against the payload and for each case which header parameters must be signed and which are not.

3. Build a JSON object including all the header parameters that are to be integrity protected or to be signed. In other words, construct the JWS Protected Header for each signature. The base64url-encoded value of the UTF-8 encoded JWS Protected Header will produce the value of the protected subelement inside the signatures top-level element of the serialized JWS token.

4. Build a JSON object including all the header parameters that need not be integrity protected or not be signed. In other words, construct the JWS Unprotected Header for each signature. This will produce the header subelement inside the signatures top-level element of the serialized JWS token.

5. Both the JWS Protected Header and the JWS Unprotected Header express the cryptographic properties of the corresponding signature (there can be more than one signature element)—this is known as the JOSE header. As discussed before in this chapter, under the section “JOSE Header,” the token issuer should advertise in the JOSE header the public key corresponding to the key used to sign the message. This can be expressed via any of these header parameters: jku, jwk, kid, x5u, x5c, x5t, and x5t#s256.

6. Build the message to compute the digital signature or the MAC against each entry in the signatures JSON array of the serialized JWS token. The message is constructed as `ASCII(BASE64URL-ENCODE(UTF8(JWS Protected Header)). BASE64URL-ENCODE(JWS Payload))`.

7. Compute the signature over the message constructed in step 6, following the signature algorithm defined by the header parameter alg. This parameter can be either inside the JWS Protected Header or the JWS Unprotected Header. The message is signed using the private key corresponding to the public key advertised in the header.

8. Compute the base64url-encoded value of the JWS signature produced in step 7, which will produce the value of the signature subelement inside the signatures top-level element of the serialized JWS token.

9. Once all the signatures are computed, the signatures top-level element can be constructed and will complete the JWS JSON serialization.


> Signature Types
>
> The XML Signature specification, which was developed under W3C, proposes three types of signatures: enveloping, enveloped, and detached. These three kinds of signatures are only discussed under the context of XML.
> With the enveloping signature, the XML content to be signed is inside the signature itself. That is, inside the `<ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#">` element.
> 
> With the enveloped signature, the signature is inside the XML content to be signed. In other words, the `<ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#">` element is inside the parent element of the XML payload to be signed.
> 
> With the detached signature, there is no parent-child relationship between the XML content to be signed and the corresponding signature. They are detached from each other.
> For anyone who is familiar with XML Signature, all the signatures defined in the JWS specification can be treated as detached signatures.


> **Note**
>
> The XML Signature specification by W3C only talks about signing an XML payload. If you have to sign any content, then first you need to embed that within an XML payload and then sign. In contrast, the JWS specification is not just limited to JSON. You can sign any content with JWS without wrapping it inside a JSON payload.

#### Generating a JWS Token with HMAC-SHA256 with a JSON Payload

The following Java code generates a JWS token with HMAC-SHA256. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample02.

The method `buildHmacSha256SignedJWT()` in the code should be invoked by passing a secret value that is used as the shared key to sign. The length of the secret value must be at least 256 bits:
```
public static String buildHmacSha256SignedJSON(String secret) throws JOSEException {
    // build audience restriction list.
    List<String> aud = new ArrayList<String>();
    aud.add("https://app1.foo.com");
    aud.add("https://app2.foo.com");

    Date currentTime = new Date();

    // create a claims set.
    JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().
        // set the value of the issuer.
        issuer("https://apress.com").
        // set the subject value - JWT belongs to // this subject.
        subject("john").
        // set values for audience restriction.
        audience(aud).
        // expiration time set to 10 minutes.
        expirationTime(new Date(new Date().getTime() + 1000 - 60 - 10)).
        // set the valid from time to current time.
        notBeforeTime(currentTime).
        // set issued time to current time.
        issueTime(currentTime).
        // set a generated UUID as the JWT // identifier.
        jwtID(UUID.randomUUID().toString()).
        build();

    // create JWS header with HMAC-SHA256 algorithm.
    JWSHeader jswHeader = new JWSHeader(JWSAlgorithm.HS256);

    // create signer with the provider shared secret.
    JWSSigner signer = new MACSigner(secret);

    // create the signed JWT with the JWS header and the JWT body.
    SignedJWT signedJWT = new SignedJWT(jswHeader, jwtClaims);

    // sign the JWT with HMAC-SHA256.
    signedJWT.sign(signer);

    // serialize into base64url-encoded text.
    String jwtInText = signedJWT.serialize();

    // print the value of the JWT.
    System.out.println(jwtInText);

    return jwtInText;
}
```
To build and run the program, execute the following Maven command from the `ch07/sample02` directory.
```
\> mvn test -Psample02
```
The preceding code produces the following output, which is a signed JSON payload (a JWS). If you run the code again and again, you may not get the same output as the value of the currentTime variable changes every time you run the program:

`eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0MDIwMzkyOTIsInN1YiI6ImpvaG4iLCJuYm
YiOjE0MDIwMzg2OTIsImF1ZCI6WyJodHRwczpcL1wvYXBwMS5mb28uY29tIiw
iaHR0cHM6XC9cL2FwcDIuZm9vLmNvbSJdLCJpc3MiOiJodHRwczpcL1wvYXBy
ZXNzLmNvbSIsImp0aSI6ImVkNjkwN2YwLWRlOGEtNDMyNi1hZDU2LWE5ZmE
5NjA2YTVhOCIsImlhdCI6MTQwMjAzODY5Mn0.3v_pa-QFCRwoKU0RaP7pLOox
T57okVuZMe_A0UcqQ8`

The following Java code shows how to validate the signature of a signed JSON message with HMAC-SHA256. To do that, you need to know the shared secret used to sign the JSON payload:
```
public boolean isValidHmacSha256Signature()
    throws JOSEException, ParseException {

    String sharedSecretString = "ea9566bd-590d-4fe2-a441-d5f240050dbc";
    // get signed JWT in base64url-encoded text.
    String jwtInText = buildHmacSha256SignedJWT(sharedSecretString);

    // create verifier with the provider shared secret.
    JWSVerifier verifier = new MACVerifier(sharedSecretString);

    // create the signed JWS token with the base64url-encoded text.
    SignedJWT signedJWT = SignedJWT.parse(jwtInText);

    // verify the signature of the JWS token.
    boolean isValid = signedJWT.verify(verifier);

    if (isValid) {
        System.out.println("valid JWT signature");
    } else {
        System.out.println("invalid JWT signature");
    }

    return isValid;
}
```

#### Generating a JWS Token with RSA-SHA256 with a JSON Payload

The following Java code generates a JWS token with RSA-SHA256. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample03. First you need to invoke the method `generateKeyPair()` and pass the `PrivateKey(generateKeyPair()`.`getPrivateKey())` into the method `buildRsaSha256SignedJSON()`:

```
public static KeyPair generateKeyPair()
    throws NoSuchAlgorithmException {

        // instantiate KeyPairGenerate with RSA algorithm.
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

        // set the key size to 1024 bits.
        keyGenerator.initialize(1024);

        // generate and return private/public key pair.
        return keyGenerator.genKeyPair();
 }

 public String buildRsaSha256SignedJSON(PrivateKey privateKey)
                throws JOSEException {

    // build audience restriction list.
    List<String> aud = new ArrayList<String>();

    aud.add("https://app1.foo.com");
    aud.add("https://app2.foo.com");
    Date currentTime = new Date();

    // create a claims set.
    JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().
        // set the value of the issuer.
        issuer("https://apress.com").
        // set the subject value - JWT belongs to // this subject.
        subject("john").
        // set values for audience restriction.
        audience(aud).
        // expiration time set to 10 minutes.
        expirationTime(new Date(new Date().getTime() + 1000 - 60 - 10)).
        // set the valid from time to current time.
        notBeforeTime(currentTime).
        // set issued time to current time.
        issueTime(currentTime).
        // set a generated UUID as the JWT identifier.
        jwtID(UUID.randomUUID().toString()).build();

        // create JWS header with RSA-SHA256 algorithm.
        JWSHeader jswHeader = new JWSHeader(JWSAlgorithm.RS256);

        // create signer with the RSA private key..
        JWSSigner signer = new RSASSASigner((RSAPrivateKey)privateKey);

        // create the signed JWT with the JWS header and the JWT body.
        SignedJWT signedJWT = new SignedJWT(jswHeader, jwtClaims);

        // sign the JWT with HMAC-SHA256.
        signedJWT.sign(signer);

        // serialize into base64-encoded text.
        String jwtInText = signedJWT.serialize();

        // print the value of the JWT.
        System.out.println(jwtInText);

        return jwtInText;
}
```

The following Java code shows how to invoke the previous two methods:
```
KeyPair keyPair = generateKeyPair();
buildRsaSha256SignedJSON(keyPair.getPrivate());
```
To build and run the program, execute the following Maven command from the `ch07/sample03` directory .
```
\> mvn test -Psample03
```
Let’s examine how to validate a JWS token signed by RSA-SHA256. You need to know the PublicKey corresponding to the PrivateKey used to sign the message:
```
public static boolean isValidRsaSha256Signature()
        throws NoSuchAlgorithmException, JOSEException, ParseException {

    // generate private/public key pair.
    KeyPair keyPair = generateKeyPair();

    // get the private key - used to sign the message.
    PrivateKey privateKey = keyPair.getPrivate();

    // get public key - used to verify the message signature.
    PublicKey publicKey = keyPair.getPublic();

    // get signed JWT in base64url-encoded text.
    String jwtInText = buildRsaSha256SignedJWT(privateKey);

    // create verifier with the provider shared secret.
    JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);

    // create the signed JWT with the base64url-encoded text.
    SignedJWT signedJWT = SignedJWT.parse(jwtInText);

    // verify the signature of the JWT.
    boolean isValid = signedJWT.verify(verifier);

    if (isValid) {
        System.out.println("valid JWT signature");
    } else {
        System.out.println("invalid JWT signature");
    }
    return isValid;
}
```

#### Generating a JWS Token with HMAC-SHA256 with a Non-JSON Payload

The following Java code generates a JWS token with HMAC-SHA256. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample04. The method buildHmacSha256SignedNonJSON() in the code should be invoked by passing a secret value that is used as the shared key to sign. The length of the secret value must be at least 256 bits:
```
public static String buildHmacSha256SignedJWT(String sharedSecretString)
                                                    throws JOSEException {
    // create an HMAC-protected JWS object with a non-JSON payload
    JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256),
                    new Payload("Hello world!"));

    // create JWS header with HMAC-SHA256 algorithm.
    jwsObject.sign(new MACSigner(sharedSecretString));

    // serialize into base64-encoded text.
    String jwtInText = jwsObject.serialize();

    // print the value of the serialzied JWS token.
    System.out.println(jwtInText);

    return jwtInText;
}
```
To build and run the program, execute the following Maven command from the ch07/sample04 directory.
```
\> mvn test -Psample04
```
The preceding code uses the JWS compact serialization and will produce the following output:

`eyJhbGciOiJIUzI1NiJ9.SGVsbG8gd29ybGQh.zub7JG0FOh7EIKAgWMzx95w-nFpJdRMvUh_pMwd6wnA`

## Summary

- JSON은 이미 API에 대한 사실상의 메시지 교환 형식이 되었습니다.

- JSON 보안에 대한 이해는 API 보안에 중요한 역할을 합니다.

- JWT(JSON Web Token)는 암호학적으로 안전한 방식으로 이해 당사자 간에 데이터를 전송하기 위한 컨테이너를 정의합니다. 2015년 5월 RFC 7519와 함께 IETF 표준이 되었습니다.

- JWS(JSON 웹 서명) 및 JWE(JSON 웹 암호화) 표준은 모두 JWT를 기반으로 구축되었습니다.

- JWS 사양에 정의된 직렬화 기술에는 컴팩트 직렬화와 JSON 직렬화의 두 가지 유형이 있습니다.

- JWS 사양은 JSON에만 국한되지 않습니다. JSON 페이로드 내부에 래핑하지 않고 JWS로 모든 콘텐츠에 서명할 수 있습니다.

 



 



