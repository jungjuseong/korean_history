# 11 실습: 책임 분리

이 장에서는 다음을 다룹니다.

- 토큰 구현 및 사용
- JSON 웹 토큰 작업
- 여러 앱에서 인증 및 권한 부여 책임 분리
- 다단계 인증 시나리오 구현
- 여러 사용자 정의 필터 및 여러 AuthenticationProvider 개체 사용
- 시나리오에 대한 다양한 가능한 구현 중에서 선택
  
우리는 먼 길을 왔습니다. 이제 이 책의 두 번째 실습 장 앞에 섰습니다. 이제 큰 그림을 보여주는 연습에서 배운 모든 것을 행동으로 옮길 때입니다. 안전 벨트를 착용하고 IDE를 열고 모험을 떠날 준비를 하세요!

이 장에서는 클라이언트, 인증 서버 및 비즈니스 논리 서버의 세 가지 행위자로 구성된 시스템을 설계합니다. 이 세 액터에서 인증 서버와 비즈니스 로직 서버의 백엔드 부분을 구현합니다. 보시다시피, 우리의 예는 더 복잡합니다. 이것은 우리가 실제 시나리오에 점점 더 가까워지고 있다는 신호입니다.

이 연습은 또한 이미 학습한 내용을 요약하고 적용하고 더 잘 이해하고 JSON 웹 토큰(JWT)과 같은 새로운 주제를 다룰 수 있는 좋은 기회입니다. 또한 시스템에서 인증 및 권한 부여 책임을 분리하는 첫 번째 데모도 볼 수 있습니다. 우리는 OAuth 2 프레임워크를 사용하여 12장에서 15장까지 이 논의를 확장할 것입니다. 다음 장에서 논의할 내용에 더 가까이 다가가는 것은 이 장의 실습을 위해 내가 디자인을 선택한 이유 중 하나입니다.

# 11.1 예제의 시나리오 및 요구 사항

이 장 전체에서 함께 개발하는 응용 프로그램의 요구 사항에 대해 설명합니다. 수행해야 할 작업을 이해하고 나면 11.2에서 시스템을 구현하는 방법과 최선의 옵션에 대해 논의합니다. 그런 다음 Spring Security로 11.3 및 11.4에서 머리부터 발끝까지 시나리오를 구현합니다. 시스템 아키텍처에는 세 가지 구성 요소가 있습니다. 그림 11.1에서 이러한 구성 요소를 찾을 수 있습니다. 세 가지 구성 요소는

- **클라이언트** -- 백엔드를 사용하는 애플리케이션입니다. Angular, ReactJS 또는 Vue.js와 같은 프레임워크를 사용하여 개발된 모바일 앱 또는 웹 애플리케이션의 프론트엔드일 수 있습니다. 우리는 시스템의 클라이언트 부분을 구현하지 않지만 실제 응용 프로그램에 존재한다는 것을 명심하십시오. 클라이언트를 사용하여 끝점을 호출하는 대신 cURL을 사용합니다.

- **인증 서버** --이것은 사용자 자격 증명 데이터베이스가 있는 응용 프로그램입니다. 이 응용 프로그램의 목적은 자격 증명(사용자 이름 및 암호)을 기반으로 사용자를 인증하고 SMS를 통해 일회용 암호(OTP)를 보내는 것입니다. 이 예에서는 실제로 SMS를 보내지 않기 때문에 데이터베이스에서 OTP 값을 직접 읽습니다.
  
이 장에서는 SMS를 보내지 않고 이 전체 응용 프로그램을 구현합니다. 나중에 AWS SNS(https://aws.amazon.com/sns/), Twillio(https://www .twilio.com/sms).

- **비즈니스 로직 서버** -- 클라이언트가 소비하는 엔드포인트를 노출하는 애플리케이션입니다. 우리는 이러한 끝점에 대한 액세스를 보호하려고 합니다. 엔드포인트를 호출하기 전에 사용자는 사용자 이름과 비밀번호로 인증한 다음 OTP를 보내야 합니다. 사용자는 SMS 메시지를 통해 OTP를 수신합니다. 이 애플리케이션은 대상 애플리케이션이므로 Spring Security로 보호합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH11_F01_Spilca.png)

그림 11.1 클라이언트는 비즈니스 로직 서버에 의해 노출된 끝점을 호출합니다. 사용자를 인증하기 위해 비즈니스 로직 서버는 인증 서버에 의해 구현된 책임을 사용합니다. 인증 서버는 데이터베이스에 사용자 자격 증명을 저장합니다.

비즈니스 로직 서버에서 엔드포인트를 호출하려면 클라이언트는 다음 세 단계를 따라야 합니다.

1. 무작위로 생성된 OTP를 얻기 위해 비즈니스 논리 서버에서 /login 끝점을 호출하여 사용자 이름과 암호를 인증합니다.

2. 사용자 이름과 OTP를 사용하여 /login 엔드포인트를 호출합니다.

3. 2단계에서 받은 토큰을 HTTP 요청의 Authorization 헤더에 추가하여 엔드포인트를 호출합니다.

클라이언트가 사용자 이름과 암호를 인증하면 비즈니스 로직 서버는 OTP에 대한 요청을 인증 서버로 보냅니다. 인증에 성공하면 인증 서버는 무작위로 생성된 OTP를 SMS를 통해 클라이언트에 보냅니다(그림 11.2). 이러한 사용자 식별 방식을 MFA(다단계 인증)라고 하며 오늘날에는 꽤 일반적입니다. 일반적으로 사용자는 자격 증명과 다른 식별 수단(예: 특정 모바일 장치 소유)을 사용하여 자신이 누구인지 증명해야 합니다.

두 번째 인증 단계에서 클라이언트가 수신된 SMS의 코드를 받으면 사용자는 다시 사용자 이름과 코드를 사용하여 /login 엔드포인트를 호출할 수 있습니다. 비즈니스 로직 서버는 인증 서버로 코드의 유효성을 검사합니다. 코드가 유효하면 클라이언트는 비즈니스 논리 서버의 모든 끝점을 호출하는 데 사용할 수 있는 토큰을 받습니다(그림 11.3). 11.2에서 이 토큰이 무엇인지, 어떻게 구현하는지, 왜 사용하는지 자세히 설명합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH11_F02_Spilca.png)

그림 11.2 첫 번째 인증 단계는 사용자 이름과 비밀번호로 사용자를 식별하는 것으로 구성됩니다. 사용자는 자격 증명을 보내고 인증 서버는 두 번째 인증 단계에서 OTP를 반환합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH11_F03_Spilca.png)

그림 11.3 두 번째 인증 단계. 클라이언트는 SMS 메시지를 통해 받은 코드를 사용자 이름과 함께 보냅니다. 비즈니스 로직 서버는 인증 서버를 호출하여 OTP를 검증합니다. OTP가 유효한 경우 비즈니스 논리 서버는 클라이언트에 토큰을 다시 발행합니다. 클라이언트는 이 토큰을 사용하여 비즈니스 논리 서버의 다른 끝점을 호출합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH11_F04_Spilca.png)

그림 11.4 세 번째 인증 단계. 비즈니스 로직 서버에 의해 노출된 엔드포인트를 호출하기 위해 클라이언트는 인증 HTTP 요청 헤더에 유효한 토큰을 추가합니다.

세 번째 인증 단계에서 클라이언트는 이제 2단계에서 수신한 토큰을 HTTP 요청의 Authorization 헤더에 추가하여 모든 엔드포인트를 호출할 수 있습니다. 그림 11.4는 이 단계를 보여줍니다.

> **참고** 이 예제를 사용하면 이전 장에서 논의한 개념을 더 많이 포함하는 더 큰 응용 프로그램에서 작업할 수 있습니다. 애플리케이션에 포함하려는 Spring Security 개념에 집중할 수 있도록 시스템 아키텍처를 단순화합니다. 누군가는 클라이언트가 인증 서버와만 암호를 공유해야 하고 비즈니스 논리 서버와 절대 공유해서는 안 되므로 이 아키텍처가 악의적인 접근 방식을 사용한다고 주장할 수 있습니다. 이것은 정확합니다! 우리의 경우에는 단순화일 뿐입니다. 실제 시나리오에서는 일반적으로 시스템의 가능한 적은 수의 구성 요소에서 알려진 자격 증명과 비밀을 유지하려고 노력합니다. 또한 누군가는 MFA 시나리오 자체가 Okta 또는 이와 유사한 것과 같은 타사 관리 시스템을 사용하여 더 쉽게 구현될 수 있다고 주장할 수 있습니다. 이 예제의 목적 중 일부는 사용자 정의 필터를 정의하는 방법을 가르치는 것입니다. 이러한 이유로 인증 아키텍처에서 이 부분을 구현하는 어려운 방법을 선택했습니다.

# 11.2 토큰 구현 및 사용

토큰은 액세스 카드와 유사합니다. 애플리케이션은 인증 프로세스의 결과로 토큰을 얻고 리소스에 액세스합니다. 끝점은 웹 애플리케이션의 리소스를 나타냅니다. 웹 응용 프로그램의 경우 토큰은 일반적으로 특정 끝점에 액세스하려는 클라이언트가 HTTP 헤더를 통해 보내는 문자열입니다. 이 문자열은 순수 UUID처럼 단순하거나 JWT(JSON Web Token)와 같이 더 복잡한 모양을 가질 수 있습니다.

오늘날 토큰은 인증 및 권한 부여 아키텍처에서 자주 사용합니다. OAuth 2 아키텍처에서 가장 중요한 요소 중 하나입니다. 그리고 이 장과 12장에서 15장에서 배우게 될 것처럼 토큰은 이점(인증 및 권한 부여 아키텍처의 책임 분리와 같은)을 제공하고 아키텍처를 상태 비저장 상태로 만드는 데 도움이 되며 요청을 검증할 수 있는 가능성을 제공합니다.

### 11.2.1 토큰이란 무엇입니까?

토큰은 응용 프로그램이 사용자를 인증했음을 증명하는 데 사용하는 방법을 제공하여 사용자가 응용 프로그램의 리소스에 액세스할 수 있도록 합니다. 11.2.2에서는 오늘날 사용되는 가장 일반적인 토큰 구현 중 하나인 JWT를 발견할 것입니다.

토큰이란 무엇입니까? 토큰은 이론적으로 액세스 카드일 뿐입니다. 사무실 건물을 방문하면 가장 먼저 접수처로 이동합니다. 그곳에서 본인을 식별(인증)하고 액세스 카드(토큰)를 받습니다. 출입 카드를 사용하여 일부 문을 열 수 있지만 반드시 모든 문을 열 수는 없습니다. 이렇게 하면 토큰이 액세스를 승인하고 특정 문을 여는 것과 같은 작업을 수행할 수 있는지 여부를 결정합니다. 그림 11.5는 이 개념을 나타냅니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH11_F05_Spilca.png)

**그림 11.5** 모선(비즈니스 논리 서버)에 액세스하려면 Zglorb에 액세스 카드(토큰)가 필요합니다. 식별 후 Zglorb는 액세스 카드를 받습니다. 이 액세스 카드(토큰)는 ​​방과 사무실(자원)에만 액세스할 수 있도록 합니다.

구현 수준에서 토큰은 일반 문자열일 수도 있습니다. 가장 중요한 것은 발급 후 이를 인식할 수 있어야 한다는 것입니다. UUID를 생성하여 메모리나 데이터베이스에 저장할 수 있습니다. 다음 시나리오를 가정해 보겠습니다.

1. 클라이언트는 자격 증명을 사용하여 서버에 자신의 ID를 증명합니다.

2. 서버는 클라이언트에게 UUID 형식의 토큰을 발급합니다. 이제 클라이언트와 연결된 이 토큰은 서버에 의해 메모리에 저장됩니다(그림 11.6).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH11_F06_Spilca.png)

**그림 11.6** 클라이언트가 인증되면 서버는 토큰을 생성하여 클라이언트에 반환합니다. 그런 다음 이 토큰은 클라이언트에서 서버의 리소스에 액세스하는 데 사용됩니다.

3. 클라이언트가 엔드포인트를 호출하면 클라이언트는 토큰을 제공하고 권한을 얻습니다. 그림 11.7은 이 단계를 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH11_F07_Spilca.png)

그림 11.7 클라이언트가 사용자 리소스에 액세스해야 하는 경우 요청에 유효한 토큰을 제공해야 합니다. 유효한 토큰은 사용자가 인증할 때 서버에서 이전에 발행한 토큰입니다.

이것은 인증 및 권한 부여 프로세스에서 토큰을 사용하는 것과 관련된 일반적인 흐름입니다. 단순한 로그인보다 복잡하지 않습니까? (어쨌든 사용자와 비밀번호에만 의존할 수 있다고 생각할 수 있습니다) 하지만 토큰은 더 많은 이점을 제공하므로 토큰을 열거한 다음 하나씩 논의해 보겠습니다.

- 토큰은 모든 요청에서 자격 증명 공유를 방지하는 데 도움이 됩니다.

- 수명이 짧은 토큰을 정의할 수 있습니다.

- 자격 증명을 무효화하지 않고 토큰을 무효화할 수 있습니다.

- 토큰은 또한 클라이언트가 요청에서 보내야 하는 사용자 권한과 같은 세부 정보를 저장할 수 있습니다.

- 토큰은 인증 책임을 시스템의 다른 구성 요소에 위임하는 데 도움이 됩니다.

토큰은 모든 요청에서 자격 증명 공유를 방지하는 데 도움이 됩니다. 2장에서 10장까지 모든 요청에 ​​대한 인증 방법으로 HTTP Basic을 사용했습니다. 그리고 이 방법은 배운 대로 각 요청에 대해 자격 증명을 보낸다고 가정합니다. 각 요청과 함께 자격 증명을 보내는 것은 종종 노출을 의미하기 때문에 좋지 않습니다. 자격 증명을 더 자주 노출할수록 누군가가 자격 증명을 가로챌 가능성이 커집니다. 토큰으로 우리는 전략을 바꿉니다. 

인증을 위한 첫 번째 요청에서만 자격 증명을 보냅니다. 일단 인증되면 토큰을 얻고 이를 사용하여 리소스 호출에 대한 권한을 얻을 수 있습니다. 이렇게 하면 토큰을 얻기 위해 자격 증명을 한 번만 보내면 됩니다.

수명이 짧은 토큰을 정의할 수 있습니다. 누군가 토큰을 훔치면 영원히 사용할 수 없습니다. 아마도 토큰을 사용하여 시스템에 침입하는 방법을 찾기 전에 토큰이 만료될 수 있고 무효화할 수도 있습니다. 토큰이 노출되었다는 사실을 알게 되면 이를 반박할 수 있습니다. 이렇게 하면 더 이상 사용할 수 없습니다.

토큰은 요청에 필요한 세부 정보를 저장할 수도 있습니다. 토큰을 사용하여 사용자의 권한 및 역할과 같은 세부 정보를 저장할 수 있습니다. 이런 식으로 서버 측 세션을 클라이언트 측 세션으로 교체할 수 있으며 수평 확장에 더 나은 유연성을 제공합니다. 이 접근 방식에 대한 자세한 내용은 OAuth 2 흐름에 대해 논의할 때 12~15장에서 볼 수 있습니다.

토큰은 인증 책임을 시스템의 다른 구성 요소로 분리하는 데 도움이 됩니다. 자체 사용자를 관리하지 않는 시스템을 구현하고 있는지도 모릅니다. 대신 사용자가 GitHub, Twitter 등과 같은 다른 플랫폼에 있는 계정의 자격 증명을 사용하여 인증할 수 있습니다. 인증을 수행하는 구성 요소를 구현하기로 선택하더라도 구현을 별도로 만들 수 있다는 것이 유리합니다. 확장성을 높이는 데 도움이 되며 시스템 아키텍처를 보다 자연스럽게 이해하고 개발할 수 있습니다. Neil Madden의 API Security in Action(Manning, 2020)의 5장과 6장도 이 주제와 관련된 좋은 읽을거리입니다. 다음은 이러한 리소스에 액세스할 수 있는 링크입니다.

https://livebook.manning.com/book/api-security-in-action/chapter-5/
https://livebook.manning.com/book/api-security-in-action/chapter-6/

### 11.2.2 JSON 웹 토큰이란 무엇입니까?

토큰 구체인 JSON 웹 토큰(JWT)에 대해 설명합니다. 이 토큰 구현에는 오늘날의 응용 프로그램에서 매우 일반적으로 사용되는 이점이 있습니다. 이것이 이 섹션에서 논의하는 이유이며 이 장의 실습 예제에 적용하기로 선택한 이유이기도 합니다. OAuth 2에 대해 논의할 12~15장에서도 찾을 수 있습니다.

섹션 11.2.1에서 토큰은 서버가 나중에 식별할 수 있는 모든 것(UUID, 액세스 카드 및 박물관에서 티켓을 구입할 때 받는 스티커)이라는 것을 이미 배웠습니다. JWT가 어떻게 생겼는지, 왜 JWT가 특별한지 알아봅시다. 구현 자체의 이름에서 JWT에 대해 많은 것을 이해하는 것은 쉽습니다.

- JSON -- JSON을 사용하여 포함된 데이터의 형식을 지정합니다.
- 웹--웹 요청에 사용하도록 설계되었습니다.
- 토큰--토큰 구현입니다.

JWT에는 세 부분이 있으며 각 부분은 점(마침표)으로 구분됩니다. 다음 코드 스니펫에서 예를 찾을 수 있습니다.
                    ↓                               ↓
```
eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImRhbmllbGxlIn0.wg6LFProg7s_KvFxvnYGiZF-Mj4rr-0nJA1tVGZNn8U
```
처음 두 부분은 헤더와 본문입니다. 헤더(토큰의 시작부터 첫 번째 점까지)와 본문(첫 번째 점과 두 번째 점 사이)은 JSON으로 형식이 지정된 다음 Base64로 인코딩됩니다. 헤더와 본문을 사용하여 토큰에 세부 정보를 저장합니다. 다음 코드 스니펫은 헤더와 본문이 Base64로 인코딩되기 전의 모습을 보여줍니다.
```
{
  "alg": "HS256" ❶
}
{
  "username": "danielle" ❷
}
```
❶ Base64로 인코딩된 헤더

❷ Base64로 인코딩된 바디

헤더에는 토큰과 관련된 메타데이터를 저장합니다. 이 경우 토큰에 서명하기로 선택했기 때문에(예제에서 곧 배우게 될 것임) 헤더에는 서명을 생성하는 알고리즘(HS256)의 이름이 포함됩니다. 본문에는 나중에 권한 부여에 필요한 세부 정보를 포함할 수 있습니다. 이 경우 사용자 이름만 있습니다. 토큰을 가능한 짧게 유지하고 본문에 많은 데이터를 추가하지 않는 것이 좋습니다. 기술적으로 제한이 없더라도

- 토큰이 길면 요청이 느려집니다.

- 토큰에 서명할 때 토큰이 길수록 암호화 알고리즘이 서명하는 데 더 많은 시간이 필요합니다.

토큰의 마지막 부분(두 번째 점부터 끝까지)은 디지털 서명이지만 이 부분은 누락될 수 있습니다. 일반적으로 헤더와 본문에 서명하는 것을 선호하기 때문에 토큰 내용에 서명할 때 나중에 서명을 사용하여 내용이 변경되지 않았는지 확인할 수 있습니다. 서명이 없으면 네트워크에서 토큰을 전송하고 내용을 변경할 때 누군가가 토큰을 가로채지 않았는지 확신할 수 없습니다.

요약하자면 JWT는 토큰 구현입니다. 이는 인증 중에 데이터를 쉽게 전송하고 무결성을 검증하기 위해 데이터에 서명하는 이점을 추가합니다(그림 11.8). Prabath Siriwardena와 Nuwan Dias(Manning, 2020)의 7장과 Microservices Security in Action의 부록 H에서 JWT에 대한 훌륭한 토론을 찾을 수 있습니다.

https://livebook.manning.com/book/microservices-security-in-action/chapter-7/
https://livebook.manning.com/book/microservices-security-in-action/h-json-web-token-jwt-/

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH11_F08_Spilca.png)

그림 11.8 JWT는 헤더, 본문 및 서명의 세 부분으로 구성됩니다. 헤더와 본문은 토큰에 저장된 데이터의 JSON 표현입니다. 요청 헤더에서 쉽게 보낼 수 있도록 Base64로 인코딩됩니다. 토큰의 마지막 부분은 서명입니다. 부품은 점으로 연결됩니다.

이 장에서는 JWT(Java JSON Web Token)를 라이브러리로 사용하여 JWT를 만들고 구문 분석합니다. 이것은 Java 애플리케이션에서 JWT 토큰을 생성하고 구문 분석하는 데 가장 자주 사용되는 라이브러리 중 하나입니다. 이 라이브러리를 사용하는 방법과 관련된 모든 필요한 세부 정보 외에도 JJWT의 GitHub 저장소에서 JWT에 대한 훌륭한 설명도 찾았습니다. 다음을 읽는 것도 유용할 수 있습니다.

https://github.com/jwtk/jjwt#overview

## 11.3 인증 서버 구현

이 섹션에서는 실습 예제의 구현을 시작합니다. 첫 번째 종속성은 인증 서버입니다. Spring Security 사용에 중점을 둔 애플리케이션이 아니더라도 최종 결과를 위해서는 필요합니다. 이 실습에서 필수적인 것에 집중할 수 있도록 구현의 일부를 가져옵니다. 나는 예제 전체에서 이것들을 언급하고 연습으로 구현하도록 남겨둡니다.
이 시나리오에서 인증 서버는 요청 인증 이벤트 중에 생성된 OTP와 사용자 자격 증명을 저장하는 데이터베이스에 연결합니다. 3개의 엔드포인트를 노출하려면 이 애플리케이션이 필요합니다(그림 11.9).
- /user/add--나중에 구현 테스트에 사용할 사용자를 추가합니다.
- /user/auth--자격 증명으로 사용자를 인증하고 OTP가 포함된 SMS를 보냅니다. 우리는 SMS를 보내는 부분을 제거하지만 이것은 연습으로 할 수 있습니다.
- /otp/check--OTP 값이 인증 서버가 특정 사용자에 대해 이전에 생성한 값인지 확인합니다.
REST 끝점을 만드는 방법에 대한 복습을 위해 Craig Walls의 Spring in Action, 6th ed.의 6장을 읽을 것을 권장합니다.
https://livebook.manning.com/book/spring-in-action-sixth-edition/chapter-6/

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH11_F09_Spilca.png)

그림 11.9 인증 서버를 위한 클래스 디자인. 컨트롤러는 서비스 클래스에 정의된 로직을 호출하는 REST 엔드포인트를 노출합니다. 두 개의 저장소는 데이터베이스에 대한 액세스 계층입니다. 또한 SMS를 통해 보낼 OTP를 생성하는 코드를 분리하는 유틸리티 클래스를 작성합니다.
새 프로젝트를 만들고 다음 코드 스니펫에 표시된 대로 필요한 종속성을 추가합니다. ssia-ch11-ex1-s1 프로젝트에서 구현된 이 앱을 찾을 수 있습니다.
```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
  <scope>runtime</scope>
</dependency>
```

We also need to make sure we create the database for the application. Because we store user credentials (username and password), we need a table for this. And we also need a second table to store the OTP values associated with authenticated users (figure 11.10).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH11_F10_Spilca.png)

Figure 11.10 The app database has two tables. In one of the tables, the app stores user credentials, while in the second one, the app stores the generated OTP codes.
I use a database named spring and add the scripts to create the two tables required in a schema.sql file. Remember to place the schema.sql file in the resources folder of your project as this is where Spring Boot picks it up to execute the scripts. In the next code snippet, you find the content of my schema.sql file. (If you don’t like the approach with the schema.sql file, you can create the database structure manually anytime or use any other method you prefer.)
```sql
CREATE TABLE IF NOT EXISTS `spring`.`user` (
    `username` VARCHAR(45) NULL,
    `password` TEXT NULL,
    PRIMARY KEY (`username`));

CREATE TABLE IF NOT EXISTS `spring`.`otp` (
    `username` VARCHAR(45) NOT NULL,
    `code` VARCHAR(45) NULL,
    PRIMARY KEY (`username`));
```
application.properties 파일에서 Spring Boot가 데이터 소스를 생성하는 데 필요한 매개변수를 제공합니다. 다음 코드 조각은 application.properties 파일의 내용을 보여줍니다.

```yml
spring.datasource.url=jdbc:mysql://localhost/spring
spring.datasource.username=root
spring.datasource.password=
spring.datasource.initialization-mode=always
```
이 애플리케이션에 대한 종속성에도 Spring Security를 추가했습니다. 인증 서버에 대해 이 작업을 수행한 유일한 이유는 데이터베이스에 저장할 때 사용자의 암호를 해시하는 데 사용하는 BCryptPasswordEncoder를 사용하기 위해서입니다. 예제를 짧고 목적에 맞게 유지하기 위해 비즈니스 로직 서버와 인증 서버 간에 인증을 구현하지 않습니다. 그러나 이것은 실습 예제를 마친 후 나중에 연습으로 남겨두고 싶습니다. 이 장에서 작업하는 구현의 경우 프로젝트의 구성 클래스는 목록 11.1에 있는 것과 같습니다.

> EXERCISE Change the applications from this hands-on chapter to validate the requests between the business logic server and the authentication server:

- 대칭키로
- 비대칭키 쌍으로

To solve the exercise, you might find it useful to review the example we worked on in section 9.2.

Listing 11.1 The configuration class for the authentication server
```java
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  @Bean                                                          ❶
  public PasswordEncoder passwordEncoder() {                     ❶
    return new BCryptPasswordEncoder();                          ❶
  }    

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable();                                       ❷
    http.authorizeRequests()                                     ❸
          .anyRequest().permitAll();                             ❸
  }
}
```
데이터베이스에 저장된 비밀번호를 해시하기 위한 비밀번호 인코더 정의

❷ 애플리케이션의 모든 엔드포인트를 직접 호출할 수 있도록 CSRF를 비활성화합니다.

❸ 인증 없이 모든 통화 허용

구성 클래스가 있으면 데이터베이스에 대한 연결을 계속 정의할 수 있습니다. Spring Data JPA를 사용하기 때문에 JPA 엔터티를 작성한 다음 저장소를 작성해야 하고 테이블이 2개 있으므로 JPA 엔터티 2개와 저장소 인터페이스 2개를 정의합니다. 다음 목록은 사용자 엔터티의 정의를 보여줍니다. 사용자 자격 증명을 저장하는 사용자 테이블을 나타냅니다.

Listing 11.2 The User entity
```java
@Entity
public class User {

  @Id
  private String username;
  private String password;

  // Omitted getters and setters
}
```
다음 목록은 두 번째 엔티티인 Otp를 나타냅니다. 이 엔터티는 애플리케이션이 인증된 사용자에 대해 생성된 OTP를 저장하는 otp 테이블을 나타냅니다.

Listing 11.3 The Otp entity
```java
@Entity
public class Otp {

  @Id
  private String username;
  private String code;

  // Omitted getters and setters
}
```
Listing 11.4 presents the Spring Data JPA repository for the User entity. In this interface, we define a method to retrieve a user by their username. We need this for the first step of authentication, where we validate the username and password.

Listing 11.4 The UserRepository interface
```java
public interface UserRepository extends JpaRepository<User, String> {

  Optional<User> findUserByUsername(String username);
}
```
Listing 11.5 presents the Spring Data JPA repository for the Otp entity. In this interface, we define a method to retrieve the OTP by username. We need this method for the second authentication step, where we validate the OTP for a user.

Listing 11.5 The OtpRepository interface
```java
public interface OtpRepository extends JpaRepository<Otp, String> {

  Optional<Otp> findOtpByUsername(String username);
}
```
With the repositories and entities in place, we can work on the logic of the application. For this, I create a service class that I call UserService. As shown in listing 11.6, the service has dependencies on the repositories and the password encoder. Because we use these objects to implement the application logic, we need to autowire them.
Listing 11.6 Autowiring the dependencies in the UserService class
```java
@Service
@Transactional
public class UserService {

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OtpRepository otpRepository;

}
```
Next, we need to define a method to add a user. You can find the definition of this method in the following listing.

Listing 11.7 Defining the addUser() method
```java
@Service
@Transactional
public class UserService {

  // Omitted code

  public void addUser(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepository.save(user);
  }
}
```
What does the business logic server need? It needs a way to send a username and password to be authenticated. After the user is authenticated, the authentication server generates an OTP for the user and sends it via SMS. The following listing shows the definition of the auth() method, which implements this logic.

Listing 11.8 Implementing the first authentication step
```java
@Service
@Transactional
public class UserService {

  // Omitted code

  public void auth(User user) {
    Optional<User> o =                                      ❶
      userRepository.findUserByUsername(user.getUsername());

    if(o.isPresent()) {                                     ❷
        User u = o.get();
        if (passwordEncoder.matches(
                user.getPassword(), 
                u.getPassword())) {
           renewOtp(u);                                     ❸
        } else {
           throw new BadCredentialsException
                      ("Bad credentials.");                 ❹
        }
    } else {
       throw new BadCredentialsException
                  ("Bad credentials.");                     ❹
    }
  }

  private void renewOtp(User u) {
    String code = GenerateCodeUtil
           .generateCode();                                 ❺

    Optional<Otp> userOtp =                                 ❻
      otpRepository.findOtpByUsername(u.getUsername());

    if (userOtp.isPresent()) {                              ❼
      Otp otp = userOtp.get();                              ❼
      otp.setCode(code);                                    ❼
    } else {                                                ❽
      Otp otp = new Otp();                                  ❽
      otp.setUsername(u.getUsername());                     ❽
      otp.setCode(code);                                    ❽
      otpRepository.save(otp);                              ❽
    }
  }

  // Omitted code

}
❶ Searches for the user in the database
❷ If the user exists, verifies its password
❸ If the password is correct, generates a new OTP
❹ If the password is not correct or username doesn’t exist, throws an exception
❺ Generates a random value for the OTP
❻ Searches the OTP by username
❼ If an OTP exists for this username, updates its value
❽ If an OTP doesn’t exist for this username, creates a new record with the generated value
The next listing presents the GenerateCodeUtil class. We used this class in listing 11.8 to generate the new OTP value.
Listing 11.9 Generating the OTP
public final class GenerateCodeUtil {

  private GenerateCodeUtil() {}

  public static String generateCode() {
    String code;

    try {
      SecureRandom random = 
        SecureRandom.getInstanceStrong();               ❶
        
      int c = random.nextInt(9000) + 1000;              ❷
        
      code = String.valueOf(c);                         ❸
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(
           "Problem when generating the random code.");
    }

   return code;
  }
}
❶ Creates an instance of SecureRandom that generates a random int value
❷ Generates a value between 0 and 8,999. We add 1,000 to each generated value. This way, we get values between 1,000 and 9,999 (4-digit random codes).
❸ Converts the int to a String and returns it
The last method we need to have in the UserService is one to validate the OTP for a user. You find this method in the following listing.
Listing 11.10 Validating an OTP
@Service
@Transactional
public class UserService {
  // Omitted code

  public boolean check(Otp otpToValidate) {
    Optional<Otp> userOtp =                                   ❶
      otpRepository.findOtpByUsername(
         otpToValidate.getUsername());

    if (userOtp.isPresent()) {                                ❷
      Otp otp = userOtp.get();                                ❷
      if (otpToValidate.getCode().equals(otp.getCode())) {    ❷
         return true;                                         ❷
      }
    }

     return false;                                            ❸
  }

  // Omitted code
}
❶ Searches the OTP by username
❷ If the OTP exists in the database, and it is the same as the one received from the business logic server, it returns true.
❸ Else, it returns false.
Finally, in this application, we expose the logic presented with a controller. The following listing defines this controller.
Listing 11.11 The definition of the AuthController class
@RestController
public class AuthController {

  @Autowired
  private UserService userService;

  @PostMapping("/user/add")
  public void addUser(@RequestBody User user) {
    userService.addUser(user);
  }

  @PostMapping("/user/auth")
  public void auth(@RequestBody User user) {
    userService.auth(user);
  }

  @PostMapping("/otp/check")
  public void check(@RequestBody Otp otp, HttpServletResponse response) {
    if (userService.check(otp)) {                                         ❶
      response.setStatus(HttpServletResponse.SC_OK);
    } else {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
  }
}
❶ If the OTP is valid, the HTTP response returns the status 200 OK; otherwise, the value of the status is 403 Forbidden.
With this setup, we now have the authentication server. Let’s start it and make sure that the endpoints work the way we expect. To test the functionality of the authentication server, we need to
1.	Add a new user to the database by calling the /user/add endpoint
2.	Validate that the user was correctly added by checking the users table in the database
3.	Call the /user/auth endpoint for the user added in step 1
4.	Validate that the application generates and stores an OTP in the otp table
5.	Use the OTP generated in step 3 to validate that the /otp/check endpoint works as desired
We begin by adding a user to the database of the authentication server. We need at least one user to use for authentication. We can add the user by calling the /user/add endpoint that we created in the authentication server. Because we didn’t configure a port in the authentication server application, we use the default one, which is 8080. Here’s the call:
curl -XPOST 
-H "content-type: application/json" 
-d "{\"username\":\"danielle\",\"password\":\"12345\"}" 
http://localhost:8080/user/add
After using the curl command presented by the previous code snippet to add a user, we check the database to validate that the record was added correctly. In my case, I can see the following details:
Username: danielle
Password: $2a$10$.bI9ix.Y0m70iZitP.RdSuwzSqgqPJKnKpRUBQPGhoRvHA.1INYmy
The application hashed the password before storing it in the database, which is the expected behavior. Remember, we used BCryptPasswordEncoder especially for this purpose in the authentication server.
NOTE Remember that in our discussion from chapter 4, BCryptPasswordEncoder uses bcrypt as the hashing algorithm. With bcrypt, the output is generated based on a salt value, which means that you obtain different outputs for the same input. For this example, the hash of the same password is a different one in your case. You can find more details and a great discussion on hash functions in chapter 2 of Real-World Cryptography by David Wong (Manning, 2020): http://mng.bz/oRmy.
We have a user, so let’s generate an OTP for the user by calling the /user/auth endpoint. The next code snippet provides the cURL command that you can use:
curl -XPOST 
-H "content-type: application/json" 
-d "{\"username\":\"danielle\",\"password\":\"12345\"}" 
http:/./localhost:8080/user/auth
In the otp table in our database, the application generates and stores a random four-digit code. In my case, its value is 8173.
The last step for testing our authentication server is to call the /otp/check endpoint and verify that it returns an HTTP 200 OK status code in the response when the OTP is correct and 403 Forbidden if the OTP is wrong. The following code snippets show you the test for the correct OTP value, as well as the test for a wrong OTP value. If the OTP value is correct:
curl -v -XPOST -H "content-type: application/json" -d "{\"username\":\"danielle\",\"code\":\"8173\"}" http:/./localhost:8080/otp/check
the response status is
...
< HTTP/1.1 200
...
If the OTP value is wrong:
curl -v -XPOST -H "content-type: application/json" -d "{\"username\":\"danielle\",\"code\":\"9999\"}" http:/./localhost:8080/otp/check
the response status is
...
< HTTP/1.1 403
...
We just proved that the authentication server components work! We can now dive into the next element for which we write most of the Spring Security configurations for our current hands-on example--the business logic server.
11.4 Implementing the business logic server
In this section, we implement the business logic server. With this application, you’ll recognize a lot of the things we discussed up to this point in the book. I’ll refer here and there to sections where you learned specific aspects in case you want to go back and review those. With this part of the system, you learn to implement and use JWTs for authentication and authorization. As well, we implement communication between the business logic server and the authentication server to establish the MFA in your application. To accomplish our task, at a high level, we need to
1.	Create an endpoint that represents the resource we want to secure.
2.	Implement the first authentication step in which the client sends the user credentials (username and password) to the business logic server to log in.
3.	Implement the second authentication step in which the client sends the OTP the user receives from the authentication server to the business logic server. Once authenticated by the OTP, the client gets back a JWT, which it can use to access a user’s resources.
4.	Implement authorization based on the JWT. The business logic server validates the JWT received from a client and, if valid, allows the client to access the resource.
Technically, to achieve these four high-level points, we need to
1.	Create the business logic server project. I name it ssia-ch11-ex1-s2.
2.	Implement the Authentication objects that have the role of representing the two authentication steps.
3.	Implement a proxy to establish communication between the authentication server and the business logic server.
4.	Define the AuthenticationProvider objects that implement the authentication logic for the two authentication steps using the Authentication objects defined in step 2.
5.	Define the custom filter objects that intercept the HTTP request and apply the authentication logic implemented by the AuthenticationProvider objects.
6.	Write the authorization configurations.
We start with the dependencies. The next listing shows the dependencies you need to add to the pom.xml file. You can find this application in the project ssia-ch11-ex1-s2.
Listing 11.12 The dependencies needed for the business logic server
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>                                      ❶
   <groupId>io.jsonwebtoken</groupId>             ❶
   <artifactId>jjwt-api</artifactId>              ❶
   <version>0.11.1</version>                      ❶
</dependency>                                     ❶
<dependency>                                      ❶
   <groupId>io.jsonwebtoken</groupId>             ❶
   <artifactId>jjwt-impl</artifactId>             ❶
   <version>0.11.1</version>                      ❶
   <scope>runtime</scope>                         ❶
</dependency>                                     ❶
<dependency>                                      ❶
   <groupId>io.jsonwebtoken</groupId>             ❶
   <artifactId>jjwt-jackson</artifactId>          ❶
   <version>0.11.1</version>                      ❶
   <scope>runtime</scope>                         ❶
</dependency>                                     ❶
<dependency>                                      ❷
   <groupId>jakarta.xml.bind</groupId>            ❷
   <artifactId>jakarta.xml.bind-api</artifactId>  ❷
   <version>2.3.2</version>                       ❷
</dependency>                                     ❷
<dependency>                                      ❷
   <groupId>org.glassfish.jaxb</groupId>          ❷
   <artifactId>jaxb-runtime</artifactId>          ❷
   <version>2.3.2</version>                       ❷
</dependency>                                     ❷
❶ Adds the jjwt dependency for generating and parsing JWTs
❷ You need this if you use Java 10 or above.
In this application, we only define a /test endpoint. Everything else we write in this project is to secure this endpoint. The /test endpoint is exposed by the TestController class, which is presented in the following listing.
Listing 11.13 The TestController class
@RestController
public class TestController {

  @GetMapping("/test")
  public String test() {
    return "Test";
  }
}
To secure the app now, we have to define the three authentication levels:
- Authentication with username and password to receive an OTP (figure 11.11)
 
Figure 11.11 The first authentication step. The user sends their credentials for authentication. The authentication server authenticates the user and sends an SMS message containing the OTP code.
- Authentication with OTP to receive a token (figure 11.12)
 
Figure 11.12 The second authentication step. The user sends the OTP code they received as a result of the first authentication step. The authentication server validates the OTP code and sends back a token to the client. The client uses the token to access the user’s resources.
- Authentication with the token to access the endpoint (figure 11.13).
 
Figure 11.13 The last authentication step. The client uses the token obtained in step 2 to access resources exposed by the business logic server.
With the given requirements for this example, which is more complex and assumes multiple authentication steps, HTTP Basic authentication can’t help us anymore. We need to implement special filters and authentication providers to customize the authentication logic for our scenario. Fortunately, you learned how to define custom filters in chapter 9, so let’s review the authentication architecture in Spring Security (figure 11.14).
 
Figure 11.14 The authentication architecture in Spring Security. The authentication filter, which is part of the filter chain, intercepts the request and delegates authentication responsibility to the authentication manager. The authentication manager uses an authentication provider to authenticate the request.
Often, when developing an application, there’s more than one good solution. When designing an architecture, you should always think about all possible implementations and choose the best fit for your scenario. If more than one option is applicable and you can’t decide which is the best to implement, you should write a proof-of-concept for each option to help you decide which solution to choose. For our scenario, I present two options, and then we continue the implementation with one of these. I leave the other choice as an exercise for you to implement.
The first option for us is to define three custom Authentication objects, three custom AuthenticationProvider objects, and a custom filter to delegate to these by making use of the AuthenticationManager (figure 11.15). You learned how to implement the Authentication and AuthenticationProvider interfaces in chapter 5.
 
Figure 11.15 The first option for implementing our application. The AuthenticationFilter intercepts the request. Depending on the authentication step, it creates a specific Authentication object and dispatches it to the AuthenticationManager. An Authentication object represents each authentication step. For each authentication step, an Authentication provider implements the logic. In the figure, I shaded the components that we need to implement.
The second option, which I chose to implement in this example, is to have two custom Authentication objects and two custom AuthenticationProvider objects. These objects can help us apply the logic related to the /login endpoint. These will
- Authenticate the user with a username and password
- Authenticate the user with an OTP
Then we implement the validation of the token with a second filter. Figure 11.16 presents this approach.
 
Figure 11.16 The second option for implementing our application. In this scenario, the authentication process separates responsibilities with two filters. The first treats requests on the /login path and takes care of the two initial authentication steps. The other takes care of the rest of the endpoints for which the JWT tokens need to be validated.
Both approaches are equally good. I describe both of these only to illustrate that you can find cases in which you have multiple ways to develop the same scenario, especially because Spring Security offers quite a flexible architecture. I chose the second one because it offers me the possibility to recap more things, like having multiple custom filters and using the shouldNotFilter() method of the OncePerRequestFilter class. We briefly discussed this class in section 9.5, but I didn’t have the chance to apply the shouldNotFilter() method with an example. We take this opportunity now.
EXERCISE Implement the business logic server with the first approach described in this section and presented by figure 11.15.
11.4.1 IMPLEMENTING THE AUTHENTICATION OBJECTS
In this section, we implement the two Authentication objects we need for our solution to develop the business logic server. At the beginning of section 11.4, we created the project and added the needed dependencies. We also created an endpoint that we want to secure and decided on how to implement the class design for our example. We need two types of Authentication objects, one to represent authentication by username and password and a second to represent authentication by OTP. As you learned in chapter 5, the Authentication contract represents the authentication process for a request. It can be a process in progress or after its completion. We need to implement the Authentication interface for both cases in which the application authenticates the user with their username and password, as well as for a OTP.
In listing 11.14, you find the UsernamePasswordAuthentication class, which implements authentication with username and password. To make the classes shorter, I extend the UsernamePasswordAuthenticationToken class and, indirectly, the Authentication interface. You saw the UsernamePasswordAuthenticationToken class in chapter 5, where we discussed applying custom authentication logic.
Listing 11.14 The UsernamePasswordAuthentication class
public class UsernamePasswordAuthentication 
  extends UsernamePasswordAuthenticationToken {

  public UsernamePasswordAuthentication(
    Object principal, 
    Object credentials, 
    Collection<? extends GrantedAuthority> authorities) {
    
    super(principal, credentials, authorities);
  }

  public UsernamePasswordAuthentication(
    Object principal, 
    Object credentials) {
   
    super(principal, credentials);
  }
}
Note that I define both constructors in this class. There’s a big difference between these: when you call the one with two parameters, the authentication instance remains unauthenticated, while the one with three parameters sets the Authentication object as authenticated. As you learned in chapter 5, when the Authentication instance is authenticated it means that the authentication process ends. If the Authentication object is not set as authenticated, and no exception is thrown during the process, the AuthenticationManager tries to find a proper AuthenticationProvider object to authenticate the request.
We used the constructor with two parameters when we initially build the Authentication object, and it’s not yet authenticated. When an AuthenticationProvider object authenticates the request, it creates an Authentication instance using the constructor with three parameters, which creates an authenticated object. The third parameter is the collection of granted authorities, which is mandatory for an authentication process that has ended.
Similarly to the UsernamePasswordAuthentication, we implement the second Authentication object for the second authentication step with OTP. I name this class OtpAuthentication. Listing 11.15 demonstrates that class extends the UsernamePasswordAuthenticationToken. We can use the same class because we treat the OTP as a password. Because it’s similar, we use the same approach to save some lines of code.
Listing 11.15 The OtpAuthentication class
public class OtpAuthentication 
  extends UsernamePasswordAuthenticationToken {

  public OtpAuthentication(Object principal, Object credentials) {
    super(principal, credentials);
  }

  public OtpAuthentication(
          Object principal, 
          Object credentials, 
          Collection<? extends GrantedAuthority> authorities) {
    super(principal, credentials, authorities);
  }
}
11.4.2 IMPLEMENTING THE PROXY TO THE AUTHENTICATION SERVER
In this section, we build a way to call the REST endpoint exposed by the authentication server. Immediately after defining the Authentication objects, we usually implement the AuthenticationProvider objects (figure 11.17). We know, however, that to complete authentication, we need a way to call the authentication server. I continue now with implementing a proxy for the authentication server before implementing the AuthenticationProvider objects.
 
Figure 11.17 The authentication logic implemented by the authentication providers uses the AuthenticationServerProxy to call the authentication server.
For this implementation, we need to
1.	Define a model class User, which we use to call the REST services exposed by the authentication server
2.	Declare a bean of type RestTemplate, which we use to call the REST endpoints exposed by the authentication server
3.	Implement the proxy class, which defines two methods: one for username/password authentication and the other for username/otp authentication
The following listing presents the User model class.
Listing 11.16 The User model class
public class User {

  private String username;
  private String password;
  private String code;

    // Omitted getters and setters
}
The next listing presents the application configuration class. I name this class ProjectConfig and define a RestTemplate bean for the proxy class that we develop next.
Listing 11.17 The ProjectConfig class
@Configuration
public class ProjectConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
We can now write the AuthenticationServerProxy class, which we use to call the two REST endpoints exposed by the authentication server application. The next listing presents this class.
Listing 11.18 The AuthenticationServerProxy class
@Component
public class AuthenticationServerProxy {

  @Autowired
  private RestTemplate rest;

  @Value("${auth.server.base.url}")           ❶
  private String baseUrl;

  public void sendAuth(String username, 
                         String password) {

    String url = baseUrl + "/user/auth";

    var body = new User();
    body.setUsername(username);               ❷
    body.setPassword(password);               ❷

    var request = new HttpEntity<>(body);

    rest.postForEntity(url, request, Void.class);
  }

  public boolean sendOTP(String username, 
                         String code) {

     String url = baseUrl + "/otp/check";

     var body = new User();                  ❸
     body.setUsername(username);             ❸
     body.setCode(code);                     ❸

     var request = new HttpEntity<>(body);

     var response = rest.postForEntity(url, request, Void.class);

     return response                         ❹
             .getStatusCode()                ❹
               .equals(HttpStatus.OK);       ❹
  }
}
❶ Takes the base URL from the application.properties file
❷ The HTTP request body needs the username and the password for this call.
❸ The HTTP request body needs the username and the code for this call.
❹ Returns true if the HTTP response status is 200 OK and false otherwise
These are just regular calls on REST endpoints with a RestTemplate. If you need a refresher on how this works, a great choice is chapter 7 of Spring in Action, 6th ed., by Craig Walls (Manning, 2018):
https://livebook.manning.com/book/spring-in-action-sixth-edition/chapter-7/
Remember to add the base URL for the authentication server to your application.properties file. I also change the port for the current application here because I expect to run the two server applications on the same system for my tests. I keep the authentication server on the default port, which is 8080, and I change the port for the current app (the business logic server) to 9090. The next code snippet shows the content for the application.properties file:
server.port=9090
auth.server.base.url=http://localhost:8080
11.4.3 IMPLEMENTING THE AUTHENTICATIONPROVIDER INTERFACE
In this section, we implement the AuthenticationProvider classes. Now we have everything we need to start working on the authentication providers. We need these because this is where we write the custom authentication logic.
We create a class named UsernamePasswordAuthenticationProvider to serve the UsernamePasswordAuthentication type of Authentication, as described by listing 11.19. Because we design our flow to have two authentication steps, and we have one filter that takes care of both steps, we know that authentication doesn’t finish with this provider. We use the constructor with two parameters to build the Authentication object: new UsernamePasswordAuthenticationToken (username, password). Remember, we discussed in section 11.4.1 that the constructor with two parameters doesn’t mark the object as being authenticated.
Listing 11.19 The UsernamePasswordAuthentication class
@Component
public class UsernamePasswordAuthenticationProvider 
  implements AuthenticationProvider {

  @Autowired
  private AuthenticationServerProxy proxy;

  @Override
  public Authentication authenticate
                    (Authentication authentication) 
                     throws AuthenticationException {

    String username = authentication.getName();
    String password = String.valueOf(authentication.getCredentials());

    proxy.sendAuth(username, password);                                  ❶

    return new UsernamePasswordAuthenticationToken(username, password);
  }

  @Override
  public boolean supports(Class<?> aClass) {                             ❷
    return UsernamePasswordAuthentication.class.isAssignableFrom(aClass);
  }
}
❶ Uses the proxy to call the authentication server. It sends the OTP to the client through SMS.
❷ Designs this AuthenticationProvider for the UsernamePasswordAuthentication type of Authentication
Listing 11.20 presents the authentication provider designed for the OtpAuthentication type of Authentication. The logic implemented by this AuthenticationProvider is simple. It calls the authentication server to find out if the OTP is valid. If the OTP is correct and valid, it returns an instance of Authentication. The filter sends back the token in the HTTP response. If the OTP isn’t correct, the authentication provider throws an exception.
Listing 11.20 The OtpAuthenticationProvider class
@Component
public class OtpAuthenticationProvider 
  implements AuthenticationProvider {

  @Autowired
  private AuthenticationServerProxy proxy;

  @Override
  public Authentication authenticate
                     (Authentication authentication) 
                      throws AuthenticationException {

    String username = authentication.getName();
    String code = String.valueOf(authentication.getCredentials());

    boolean result = proxy.sendOTP(username, code);

    if (result) {
      return new OtpAuthentication(username, code);
    } else {
      throw new BadCredentialsException("Bad credentials.");
    }
  }

  @Override
  public boolean supports(Class<?> aClass) {
    return OtpAuthentication.class.isAssignableFrom(aClass);
  }
}
11.4.4 IMPLEMENTING THE FILTERS
In this section, we implement the custom filters that we add to the filter chain. Their purpose is to intercept requests and apply authentication logic. We chose to implement one filter to deal with authentication done by the authentication server and another one for authentication based on the JWT. We implement an InitialAuthenticationFilter class, which deals with the first authentication steps that are done using the authentication server.
In the first step, the user authenticates with their username and password to receive an OTP (figure 11.18). You saw these graphics also in figures 11.11 and 11.12, but I add these again so that you don’t need to flip back through the pages and search for them.
 
Figure 11.18 First, the client needs to authenticate the user using their credentials. If successful, the authentication server sends an SMS message to the user with a code.
In the second step, the user sends the OTP to prove they really are who they claim to be, and after successful authentication, the app provides them with a token to call any endpoint exposed by the business logic server (figure 11.19).
 
Figure 11.19 The second authentication step. The user sends the OTP code they receive as a result of the first authentication step. The authentication server validates the OTP code and sends back a token to the client. The client uses the token to access user resources.
Listing 11.21 presents the definition of the InitialAuthenticationFilter class. We start by injecting the AuthenticationManager to which we delegate the authentication responsibility, override the doFilterInternal() method, which is called when the request reaches this filter in the filter chain, and override the shouldNotFilter() method. As we discussed in chapter 9, the shouldNotFilter() method is one of the reasons why we would choose to extend the OncePerRequestFilter class instead of implementing the Filter interface directly. When we override this method, we define a specific condition on when the filters execute. In our case, we want to execute any request only on the /login path and skip all others.
Listing 11.21 The InitialAuthenticationFilter class
@Component
public class InitialAuthenticationFilter
  extends OncePerRequestFilter {

  @Autowired                                     ❶
  private AuthenticationManager manager;

  @Override
  protected void doFilterInternal(               ❷
      HttpServletRequest request, 
      HttpServletResponse response, 
      FilterChain filterChain) 
         throws ServletException, IOException {
      // ...
  }

  @Override
  protected boolean shouldNotFilter(
    HttpServletRequest request) {

    return !request.getServletPath()
                      .equals("/login");         ❸
  }
}
❶ Autowires the AuthenticationManager, which applies the correct authentication logic
❷ Overrides doFilterInternal() to require the correct authentication based on the request
❸ Applies this filter only to the /login path
We continue writing the InitialAuthenticationFilter class with the first authentication step, the one in which the client sends the username and password to obtain the OTP. We assume that if the user doesn’t send an OTP (a code), we have to do authentication based on username and password. We take all the values from the HTTP request header where we expect them to be, and if a code wasn’t sent, we call the first authentication step by creating an instance of UsernamePasswordAuthentication (listing 11.22) and forwarding the responsibility to the AuthenticationManager.
We know (since chapter 2) that next, the AuthenticationManager tries to find a proper AuthenticationProvider. In our case, this is the UsernamePassword-AuthenticationProvider we wrote in listing 11.19. It’s the one triggered because its supports() method states that it accepts the UsernamePasswordAuthentication type.
Listing 11.22 Implementing the logic for UsernamePasswordAuthentication
@Component
public class InitialAuthenticationFilter 
  extends OncePerRequestFilter {

  // Omitted code

  @Override
  protected void doFilterInternal(
       HttpServletRequest request, 
       HttpServletResponse response, 
       FilterChain filterChain) 
         throws ServletException, IOException {

    String username = request.getHeader("username");
    String password = request.getHeader("password");
    String code = request.getHeader("code");

    if (code == null) {                                         ❶
      Authentication a = 
        new UsernamePasswordAuthentication(username, password);
      manager.authenticate(a);                                  ❷
    } 
  }

  // Omitted code
}
❶ If the HTTP request doesn’t contain an OTP, we assume we have to authenticate based on username and password.
❷ Calls the AuthenticationManager with an instance of UsernamePasswordAuthentication
If, however, a code is sent in the request, we assume it’s the second authentication step. In this case, we create an OtpAuthentication object to call the AuthenticationManager (listing 11.23). We know from our implementation of the OtpAuthenticationProvider class in listing 11.20 that if authentication fails, an exception is thrown. This means that the JWT token will be generated and attached to the HTTP response headers only if the OTP is valid.
Listing 11.23 Implementing the logic for OtpAuthentication
@Component
public class InitialAuthenticationFilter 
  extends OncePerRequestFilter {

  @Autowired
  private AuthenticationManager manager;

  @Value("${jwt.signing.key}")                                 ❶
  private String signingKey;                                   ❶

  @Override
  protected void doFilterInternal(
         HttpServletRequest request, 
         HttpServletResponse response, 
         FilterChain filterChain) 
    throws ServletException, IOException {

    String username = request.getHeader("username");
    String password = request.getHeader("password");
    String code = request.getHeader("code");

    if (code == null) {
      Authentication a = 
        new UsernamePasswordAuthentication(username, password);
      manager.authenticate(a);
    } else {                                                   ❷
      Authentication a = 
        new OtpAuthentication(username, code);                 ❸
                                                               ❸
      a = manager.authenticate(a);                             ❸

      SecretKey key = Keys.hmacShaKeyFor(
        signingKey.getBytes(
          StandardCharsets.UTF_8));
      
      String jwt = Jwts.builder()                              ❹
                    .setClaims(Map.of("username", username))
                    .signWith(key)
  
                    .compact();

        response.setHeader("Authorization", jwt);              ❺
    }
  }

  // Omitted code
}
❶ Takes the value of the key used to sign the JWT token from the properties file
❷ Adds the branch for the case in which the OTP code is not null. We consider, in this case, that the client sent an OTP for the second authentication step.
❸ For the second authentication step, creates an instance of type OtpAuthentication and sends it to the AuthenticationManager, which finds a proper provider for it
❹ Builds a JWT and stores the username of the authenticated user as one of its claims. We use the key to sign the token.
❺ Adds the token to the Authorization header of the HTTP response
NOTE I wrote a minimal implementation of our example, and I skipped some details like treating exceptions and logging the event. These aspects aren’t essential for our example now, where I only ask you to focus on Spring Security components and architecture. In a real-world application, you should also implement all these details.
The following code snippet builds the JWT. I use the setClaims() method to add a value in the JWT body and the signWith() method to attach a signature to the token. For our example, I use a symmetric key to generate the signature:
SecretKey key = Keys.hmacShaKeyFor(
    signingKey.getBytes(StandardCharsets.UTF_8));

String jwt = Jwts.builder()
                 .setClaims(Map.of("username", username))
                 .signWith(key)
                 .compact();
This key is known only by the business logic server. The business logic server signs the token and can use the same key to validate the token when the client calls an endpoint. For simplicity of the example, I use here one key for all users. In a real-world scenario, however, I would have a different key for each user, but as an exercise, you can change this application to use different keys. The advantage of using individual keys for users is that if you need to invalidate all the tokens for a user, you need only to change its key.
Because we inject the value of the key used to sign the JWT from the properties, we need to change the application.properties file to define this value. My application.properties file now looks like the one in the next code snippet. Remember, if you need to see the full content of the class, you can find the implementation in the project ssia-ch11-ex1-s2.
server.port=9090
auth.server.base.url=http://localhost:8080
jwt.signing.key=ymLTU8rq83...
We also need to add the filter that deals with the requests on all paths other than /login. I name this filter JwtAuthenticationFilter. This filter expects that a JWT exists in the authorization HTTP header of the request. This filter validates the JWT by checking the signature, creates an authenticated Authentication object, and adds it to the SecurityContext. The following listing presents the implementation of the JwtAuthenticationFilter.
Listing 11.24 The JwtAuthenticationFilter class
@Component
public class JwtAuthenticationFilter 
  extends OncePerRequestFilter {

  @Value("${jwt.signing.key}")
  private String signingKey;

  @Override
  protected void doFilterInternal(
       HttpServletRequest request, 
       HttpServletResponse response, 
       FilterChain filterChain) 
         throws ServletException, IOException {

    String jwt = request.getHeader("Authorization");

    SecretKey key = Keys.hmacShaKeyFor(
      signingKey.getBytes(StandardCharsets.UTF_8));
    
    Claims claims = Jwts.parserBuilder()                        ❶
                        .setSigningKey(key)                     ❶
                        .build()                                ❶
                        .parseClaimsJws(jwt)                    ❶
                        .getBody();                             ❶

    String username = String.valueOf(claims.get("username"));

    GrantedAuthority a = new SimpleGrantedAuthority("user");    ❷
    var auth = new UsernamePasswordAuthentication(              ❷
                          username,                             ❷
                          null,                                 ❷
                          List.of(a));                          ❷

    SecurityContextHolder.getContext()
            .setAuthentication(auth);                           ❸

    filterChain.doFilter(request, response);                    ❹
  }

  @Override
  protected boolean shouldNotFilter(
    HttpServletRequest request) {

    return request.getServletPath()
                     .equals("/login");                         ❺
  }
}
❶ Parses the token to obtain the claims and verifies the signature. An exception is thrown if the signature isn’t valid.
❷ Creates the Authentication instance that we add to the SecurityContext
❸ Adds the Authentication object in the SecurityContext
❹ Calls the next filter in the filter chain
❺ Configures this filter not to be triggered on requests for the /login path
NOTE A signed JWT is also called JWS (JSON Web Token Signed). This is why the name of the method we use is parseClaimsJws().
11.4.5 WRITING THE SECURITY CONFIGURATIONS
In this section, we finalize writing the application by defining the security configurations (listing 11.25). We have to do a few configurations so that our entire puzzle is coherent:
1.	Add the filters to the filter chain as you learned in chapter 9.
2.	Disable CSRF protection because, as you learned in chapter 10, this doesn’t apply when using different origins. Here, using a JWT replaces the validation that would be done with a CSRF token.
3.	Add the AuthenticationProvider objects so that the AuthenticationManager knows them.
4.	Use matcher methods to configure all the requests that need to be authenticated, as you learned in chapter 8.
5.	Add the AuthenticationManager bean in the Spring context so that we can inject it from the InitialAuthenticationFilter class, as you saw in listing 11.23.
Listing 11.25 The SecurityConfig class
@Configuration
public class SecurityConfig 
  extends WebSecurityConfigurerAdapter {                           ❶

  @Autowired                                                       ❷
  private InitialAuthenticationFilter initialAuthenticationFilter;

  @Autowired                                                       ❷
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired                                                       ❷
  private OtpAuthenticationProvider otpAuthenticationProvider;

  @Autowired                                                       ❷
  private UsernamePasswordAuthenticationProvider 
  ➥ usernamePasswordAuthenticationProvider;

  @Override
  protected void configure(
    AuthenticationManagerBuilder auth) {
 
    auth.authenticationProvider(                                   ❸
           otpAuthenticationProvider)                              ❸
        .authenticationProvider(                                   ❸
           usernamePasswordAuthenticationProvider);                ❸
  }

  @Override
  protected void configure(HttpSecurity http) 
    throws Exception { 

    http.csrf().disable();                                         ❹

    http.addFilterAt(                                              ❺
           initialAuthenticationFilter,                            ❺
            BasicAuthenticationFilter.class)                       ❺
        .addFilterAfter(                                           ❺
           jwtAuthenticationFilter,                                ❺
            BasicAuthenticationFilter.class                        ❺
        );    


    http.authorizeRequests()                                       ❻
           .anyRequest()                                           ❻
             .authenticated();                                     ❻
  }

  @Override
  @Bean                                                            ❼
  protected AuthenticationManager authenticationManager() 
    throws Exception {
      return super.authenticationManager();
  }
}
❶ Extends the WebSecurityConfigurerAdapter to override the configure() methods for the security configurations
❷ Autowires the filters and the authentication providers that we set up in the configuration
❸ Configures both authentication providers to the authentication manager
❹ Disables CSRF protection
❺ Adds both custom filters into the filter chain
❻ Ensures that all requests are authenticated
❼ Adds the AuthenticationManager to the Spring context so that we can autowire it from the filter class
11.4.6 TESTING THE WHOLE SYSTEM
In this section, we test the implementation of the business logic server. Now that everything is in place, it’s time to run the two components of our system, the authentication server and the business logic server, and examine our custom authentication and authorization to see if this works as desired.
For our example, we added a user and checked that the authentication server works properly in section 11.3. We can try the first step to authenticaton by accessing the endpoints exposed by the business logic server with the user we added in section 11.3. The authentication server opens port 8080, and the business logic server uses port 9090, which we configured earlier in the application.properties file of the business logic server. Here’s the cURL call:
curl -H "username:danielle" -H "password:12345" http://localhost:9090/login
Once we call the /login endpoint, providing the correct username and password, we check the database for the generated OTP value. This should be a record in the otp table where the value of the username field is danielle. In my case, I have the following record:
Username: danielle
Code: 6271
We assume this OTP was sent in an SMS message, and the user received it. We use it for the second authentication step. The cURL command in the next code snippet shows you how to call the /login endpoint for the second authentication step. I also add the -v option to see the response headers where I expect to find the JWT:
curl -v -H "username:danielle" -H "code:6271" http:/./localhost:9090/login
The (truncated) response is
. . .
< HTTP/1.1 200
< Authorization: eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImRhbmllbGxlIn0.wg6LFProg7s_KvFxvnYGiZF-Mj4rr-0nJA1tVGZNn8U
. . .
The JWT is right there where we expected it to be: in the authorization response header. Next, we use the token we obtained to call the /test endpoint:
curl -H "Authorization:eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImRhbmllbGxlIn0.wg6LFProg7s_KvFxvnYGiZF-Mj4rr-0nJA1tVGZNn8U" http:/./localhost:9090/test
The response body is
Test
Awesome! You finished the second hands-on chapter! You managed to write a whole backend system and secure its resources by writing custom authentication and authorization. And you even used JWTs for this, which takes you a significant step forward and prepares you for what’s coming in the next chapters--the OAuth 2 flow.
Summary
- When implementing custom authentication and authorization, always rely on the contracts offered by Spring Security. These are the AuthenticationProvider, AuthenticationManager, UserDetailsService, and so forth. This approach helps you implement an easier-to-understand architecture and makes your application less error prone.
- A token is an identifier for the user. It can have any implementation as long as the server recognizes it after it’s generated. Examples of tokens from real-world scenarios are an access card, a ticket, or the sticker you receive at the entrance of a museum.
- While an application can use a simple universally unique identifier (UUID) as a token implementation, you more often find tokens implemented as JSON Web Tokens (JWTs). JWTs have multiple benefits: they can store data exchanged on the request, and you can sign them to ensure they weren’t changed while transferred.
- A JWT token can be signed or might be completely encrypted. A signed JWT token is called a JSON Web Token Signed (JWS) and one that has its details encrypted is called a JSON Web Token Encrypted (JWE).
- Avoid storing too many details within your JWT. When signed or encrypted, the longer the token is, the more time is needed to sign or encrypt it. Also, remember that we send the token in the header of the HTTP request. The longer the token is, the more data you add to each request, which can affect the performance of your application.
- We prefer to decouple responsibilities in a system to make it easier to maintain and scale. For this reason, for the hands-on example, we separated the authentication in a different app, which we called the authentication server. The backend application serving the client, which we called the business logic server, uses the separate authentication server when it needs to authenticate a client.
- Multi-factor authentication (MFA) is an authentication strategy in which, to access a resource, the user is asked to authenticate multiple times and in different ways. In our example, the user has to use their username and password and then prove that they have access to a specific phone number by validating an OTP received through an SMS message. This way, the user’s resources are better protected against credentials theft.
- In many cases, you find more than one good solution for solving a problem. Always consider all possible solutions and, if time allows, implement proof-of-concepts for all options to understand which better fits your scenario.
- Copy
- Add Highlight
- Add Note