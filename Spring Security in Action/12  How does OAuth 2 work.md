
# 12 OAuth 2는 어떻게 작동합니까?

이 장에서는 다음을 다룹니다.

- OAuth 2 개요
- OAuth 2 사양 구현 소개
- 싱글 사인온을 사용하는 OAuth 2 앱 빌드

OAuth 2 프레임워크는 책 한 권을 다 읽어야 하는 방대한 주제입니다. 4개의 ​​장에서 Spring Security와 함께 OAuth 2를 적용하는 데 필요한 모든 것을 배우게 될 것입니다. OAuth 2 프레임워크의 주요 행위자가 사용자, 클라이언트, 리소스 서버 및 인증 서버입니다. Spring Security를 ​​사용하여 클라이언트를 구현하는 방법을 배운 다음, 13장에서 15장까지 마지막 두 구성 요소인 리소스 서버와 권한 부여 서버 구현에 대해 논의합니다. 실제 시나리오에 적용할 수 있는 예제와 앱을 다룹니다.

이 장에서는 OAuth 2가 무엇인지 논의한 다음 SSO(Single Sign-On) 인증에 중점을 둔 애플리케이션에 OAuth 2를 적용합니다. 이 주제를 SSO의 예와 함께 다루는 이유는 매우 간단하면서도 유용하기 때문입니다. OAuth 2에 대한 개요를 제공하며 너무 많은 코드를 작성하지 않고도 완벽하게 작동하는 애플리케이션을 구현하는 만족감을 줍니다.

13~15장에서는 이 책의 이전 장에서 이미 익숙한 코드 예제에서 이 장에서 다루는 내용을 적용합니다. 이 4개의 챕터를 마치면 애플리케이션에서 Spring Security로 OAuth 2를 구현하는 데 필요한 훌륭한 개요를 갖게 될 것입니다.

OAuth 2는 매우 큰 주제이므로 적절한 경우 내가 필수라고 생각하는 다른 리소스를 참조하겠습니다. Spring Security를 ​​사용하면 OAuth 2로 애플리케이션을 쉽게 개발할 수 있습니다. 시작하는 데 필요한 유일한 전제 조건은 이 책의 2장에서 11장까지이며, 여기에서 Spring Security에서 인증 및 권한 부여의 일반적인 아키텍처를 배웠습니다. OAuth 2에 대해 논의할 내용은 Spring Security의 표준 인증 및 인증 아키텍처와 동일한 기반을 기반으로 합니다.

## 12.1 OAuth 2 프레임워크

OAuth 2는 웹 애플리케이션 보안에 일반적으로 사용되므로 이미 들어봤을 것입니다. 애플리케이션에 OAuth 2를 적용해야 할 가능성이 있습니다. 이것이 우리가 Spring Security와 함께 Spring 애플리케이션에 OAuth 2를 적용하는 것에 대해 논의해야 하는 이유입니다. 약간의 이론으로 시작한 다음 계속해서 SSO를 사용하는 응용 프로그램에 적용합니다.

OAuth 2는 리소스에 대한 타사 웹사이트 또는 앱 액세스를 허용하는 것이 주 목적인 `권한 부여 프레임워크`(또는 사양 프레임워크)라고 합니다. 때때로 사람들은 OAuth 2를 `위임 프로토콜`이라고 합니다. 무엇을 부르기로 선택하든 OAuth 2는 특정 구현이나 라이브러리가 아니라는 점을 기억하는 것이 중요합니다. 다른 플랫폼, 도구 또는 언어와 함께 OAuth 2 흐름 정의를 적용할 수도 있습니다. 이 책에서는 Spring Boot와 Spring Security를 ​​사용하여 OAuth 2를 구현하는 방법을 알아봅니다.

OAuth 2가 무엇이고 그 유용성을 이해하는 가장 좋은 방법은 이 책에서 이미 분석한 예제로 토론을 시작하는 것입니다. 지금까지 많은 예제에서 본 가장 간단한 인증 방법은 HTTP 기본 인증 방법입니다. 복잡성을 추가할 필요가 없는 시스템에서는 이것으로 충분하지 않습니까? 아니요. HTTP 기본 인증을 사용하는 경우 고려해야 할 두 가지 문제가 있습니다.

- 각각의 모든 요청에 ​​대한 자격 증명 보내기(그림 12.1)
- 사용자의 자격 증명을 별도의 시스템에서 관리

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F01_Spilca.png)

**그림 12.1** HTTP 기본 인증을 사용할 때 모든 요청에 ​​대해 자격 증명을 보내고 인증 논리를 반복해야 합니다. 이 접근 방식은 네트워크를 통해 자격 증명을 자주 공유함을 의미합니다.

각각의 모든 요청에 ​​대한 자격 증명을 보내는 것은 격리된 경우에 작동할 수 있지만 일반적으로 다음을 의미하기 때문에 바람직하지 않습니다.

- 네트워크를 통해 자주 자격 증명 공유
- 클라이언트(웹 애플리케이션의 경우 브라우저)가 자격 증명을 저장하도록 하여 클라이언트가 인증 및 권한 부여 요청과 함께 해당 자격 증명을 서버에 보낼 수 있도록 합니다.

자격 증명을 취약하게 만들어 보안을 약화시키기 때문에 애플리케이션 아키텍처에서 이 두 가지 포인트를 제거하고 싶습니다. 대부분의 경우 별도의 시스템에서 사용자 자격 증명을 관리하기를 원합니다. 조직에서 작업하는 모든 응용 프로그램에 대해 별도의 자격 증명을 구성하고 사용해야 한다고 상상해 보십시오(그림 12.2).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F02_Spilca.png)

**그림 12.2** 조직에서는 여러 응용 프로그램을 사용합니다. 이들 중 대부분을 사용하려면 인증이 필요합니다. 여러 암호를 알고 조직에서 여러 자격 증명 집합을 관리하는 것은 어려울 것입니다.

자격 증명 관리 책임을 시스템의 한 구성 요소로 분리하는 것이 좋습니다. 지금은 인증 서버라고 합시다(그림 12.3).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F03_Spilca.png)
그림 12.3 유지 관리가 더 쉬운 아키텍처는 자격 증명을 별도로 유지하고 모든 응용 프로그램이 사용자에 대해 동일한 자격 증명 집합을 사용할 수 있도록 합니다.

이 접근 방식은 동일한 개인을 나타내는 자격 증명의 중복을 제거합니다. 이러한 방식으로 아키텍처가 더 간단해지고 유지 관리가 쉬워집니다.

## 12.2 인증 아키텍처의 구성 요소

인증 구현에서 작동하는 구성 요소에 대해 설명합니다. 다음 섹션에서 참조하므로 이러한 구성 요소와 해당 구성 요소가 수행하는 역할을 알아야 합니다. 또한 책의 나머지 부분에서 OAuth 2와 관련된 구현을 작성할 때마다 참조합니다. 그러나 이 섹션에서는 이러한 구성 요소가 무엇인지, 목적에 대해서만 논의합니다(그림 12.4). 12.3에서 배우게 될 것처럼 이러한 구성 요소가 서로 "대화"하는 더 많은 방법이 있습니다. 그리고 이 섹션에서는 이러한 구성 요소 간에 서로 다른 상호 작용을 일으키는 다양한 흐름에 대해서도 배웁니다.

언급했듯이 OAuth 2 구성 요소에는 다음이 포함됩니다.

- **리소스 서버** -- 사용자가 소유한 리소스를 호스팅하는 응용 프로그램입니다. 리소스는 사용자의 데이터 또는 승인된 작업일 수 있습니다.

- **사용자**(리소스 소유자) -- 리소스 서버에 의해 노출된 리소스를 소유한 개인입니다. 사용자는 일반적으로 자신을 식별하는 데 사용하는 사용자 이름과 암호를 가지고 있습니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F04_Spilca.png)

그림 12.4 주요 구성 요소는 리소스 소유자, 클라이언트, 권한 부여 서버 및 리소스 서버입니다. 이들 각각은 인증 및 권한 부여 프로세스에 필수적인 자체 책임이 있습니다.

- **클라이언트** -- 사용자를 대신하여 사용자가 소유한 리소스에 액세스하는 응용 프로그램입니다. 클라이언트는 client id와 password를 사용하여 자신을 식별합니다. 이러한 자격 증명은 사용자 자격 증명과 동일하지 않으므로 주의하십시오. 클라이언트는 요청할 때 자신을 식별하기 위해 자체 자격 증명이 필요합니다.
  
- **권한 부여 서버** -- 클라이언트가 리소스 서버에 의해 노출된 사용자의 리소스에 액세스할 수 있도록 권한을 부여하는 응용 프로그램입니다. 권한 부여 서버는 클라이언트가 사용자를 대신하여 리소스에 액세스할 수 있는 권한이 있다고 결정하면 토큰을 발급합니다. 클라이언트는 이 토큰을 사용하여 권한 부여 서버에 의해 권한이 부여되었음을 리소스 서버에 증명합니다. 리소스 서버는 유효한 토큰이 있는 경우 클라이언트가 요청한 리소스에 액세스할 수 있도록 합니다.

## 12.3 부여 유형 선택하기

애플리케이션 아키텍처에 따라 OAuth 2를 적용하는 방법에 대해 설명합니다. 앞으로 배우겠지만 OAuth 2는 여러 가지 가능한 인증 흐름을 의미하며 어떤 인증 흐름이 귀하의 사례에 적용되는지 알아야 합니다. 이 섹션에서는 가장 일반적인 경우를 선택하여 평가합니다. 첫 번째 구현을 시작하기 전에 이 작업을 수행하여 구현 중인 내용을 아는 것이 중요합니다.

그렇다면 OAuth 2는 어떻게 작동합니까? OAuth 2 인증 및 권한 부여를 구현한다는 것은 무엇을 의미합니까? 주로 OAuth 2는 권한 부여를 위해 토큰을 사용하는 것을 말합니다. 토큰은 액세스 카드와 같으며 토큰으로 특정 리소스에 액세스할 수 있습니다. 그러나 OAuth 2는 부여grant라고 하는 토큰을 얻을 수 있는 여러 가능성을 제공합니다. 다음은 선택할 수 있는 가장 일반적인 OAuth 2 권한입니다.

- 인증 코드

- Password

- 리프레시 토큰

- 클라이언트 자격증명
  
구현을 시작할 때 부여 유형을 선택해야 하므로 각 부여 유형에 대해 토큰이 생성되는 방식을 알아야 합니다. 그런 다음 애플리케이션 요구 사항에 따라 그 중 하나를 선택합니다. Justin Richer와 Antonio Sanso의 OAuth 2 In Action(Manning, 2017)의 섹션 6.1에서 부여 유형에 대한 훌륭한 토론을 찾을 수도 있습니다.

https://livebook.manning.com/book/oauth-2-in-action/chapter-6/6

### 12.3.1 인증 코드 부여 유형

인증 코드 부여 유형에 대해 논의합니다(그림 12.5). 섹션 12.5에서 구현할 애플리케이션에서도 사용할 것입니다. 이 권한 부여 유형은 가장 일반적으로 사용되는 OAuth 2 흐름 중 하나이므로 작동 방식과 적용 방법을 이해하는 것이 매우 중요합니다. 개발 중인 애플리케이션에서 사용할 가능성이 높습니다.

![그림 12.5](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F05_Spilca.png)

**그림 12.5** 인증 코드 부여 유형. 클라이언트는 사용자에게 권한 부여 서버와 직접 상호 작용하여 사용자의 요청에 대한 권한을 부여하도록 요청합니다. 권한이 부여되면 권한 부여 서버는 클라이언트가 사용자의 리소스에 액세스하는 데 사용하는 토큰을 발행합니다.

> **참고** 그림 12.5의 화살표가 반드시 HTTP 요청 및 응답을 나타내는 것은 아닙니다. 이는 OAuth 2의 액터 간에 교환되는 메시지를 나타냅니다. 예를 들어 클라이언트가 사용자(다이어그램 상단에서 두 번째 화살표)에게 "이 작업을 수행할 수 있는 권한 부여 서버에 알려주세요"라고 말하면 클라이언트는 사용자를 인증 서버 로그인 페이지로 이동합니다. 인증 서버가 클라이언트에게 액세스 토큰을 제공할 때 인증 서버는 실제로 리디렉션 URI라고 하는 클라이언트를 호출합니다. 이러한 시퀀스 다이어그램이 단지 HTTP 요청과 응답을 나타내는 것이 아닙니다. 다음은 OAuth 2 액터 간의 통신에 대한 간략한 설명입니다.

인증 코드 부여 유형이 작동하는 방식은 다음과 같습니다. 다음으로 각 단계에 대한 세부 정보를 살펴봅니다.

1. 인증 요청하기
2. 액세스 토큰 획득
3. 보호된 리소스 호출

#### 1단계: 인증 코드 부여 유형으로 인증 요청하기

클라이언트는 인증이 필요한 권한 서버의 끝점으로 사용자를 리디렉션합니다. OOO 앱을 사용 중이고 보호된 리소스에 액세스해야 한다고 상상할 수 있습니다. 해당 리소스에 액세스하려면 OOO앱에서 인증해야 합니다. 자격 증명으로 채워야 하는 인증 서버의 로그인 양식이 있는 페이지가 열립니다.

> **참고** 여기에서 정말 중요한 것은 사용자가 인증 서버와 직접 상호 작용한다는 것입니다. 사용자는 자격 증명을 클라이언트 앱에 보내지 않습니다.

여기서 일어나는 일은 클라이언트가 사용자를 권한 부여 서버로 리디렉션할 때 클라이언트가 요청 쿼리에서 다음 세부 정보를 사용하여 권한 부여 끝점을 호출한다는 것입니다.

- `response_type`: 클라이언트가 코드를 기대한다는 것을 인증 서버에 알리는 값 code가 있음 . 두 번째 단계에서 볼 수 있듯이 클라이언트는 액세스 토큰을 얻기 위해 code가 필요합니다.

- `client_id`: 응용 프로그램 자체를 식별하는 클라이언트 ID 값입니다.

- `redirect_uri`: 인증 성공 후 사용자를 리디렉션할 위치를 인증 서버에 알려줍니다. 때때로 권한 부여 서버는 각 클라이언트에 대한 기본 리디렉션 URI를 이미 알고 있습니다.

- `scope`: 5장에서 논의한 부여된 권한과 유사

- `status`: 10장에서 논의한 CSRF 보호에 사용되는 `CSRF` 토큰을 정의
  
인증에 성공하면 인증 서버는 리디렉션 URI에서 클라이언트를 다시 호출하고 코드와 상태 값을 제공합니다. 클라이언트는 상태 값이 리디렉션 URI를 호출하려는 다른 사람이 아닌지 확인하기 위해 요청에서 보낸 값과 동일한지 확인합니다. 클라이언트는 코드를 사용하여 2단계에 제공된 액세스 토큰을 얻습니다.

#### 2단계: 승인 코드 부여 유형으로 액세스 토큰 얻기

사용자가 리소스에 액세스할 수 있도록 하기 위해 1단계에서 생성된 `code`는 사용자가 인증했다는 클라이언트의 증거이며 인증 코드 부여 유형이라고 불리는 이유입니다. 이제 클라이언트는 토큰을 가져오기 위해 `code`를 사용하여 권한 부여 서버를 호출합니다.

> **참고** 첫 번째 단계에서 사용자와 인증 서버 간의 상호 작용이 있었습니다. 이 단계에서 상호 작용은 클라이언트와 권한 부여 서버 간의 상호 작용입니다(그림 12.6).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F06_Spilca.png)

**그림 12.6** 첫 번째 단계는 사용자와 권한 부여 서버 간의 직접적인 상호 작용을 의미합니다. 두 번째 단계에서 클라이언트는 인증 서버에 액세스 토큰을 요청하고 1단계에서 획득한 인증 코드를 제공합니다.

일반적으로 흐름에 인증 서버에 대한 두 번의 호출과 두 개의 다른 토큰(인증 코드 및 액세스 토큰)이 필요한 이유에 대해 다음 사항을 이해하십시오.

- 인증 서버는 사용자가 직접 상호 작용했다는 증거로 첫 번째 `code`를 생성합니다. 클라이언트는 이 code를 수신하고 `액세스 토큰`을 얻기 위해 이 `code`와 해당 자격 증명을 사용하여 다시 인증해야 합니다.

- 클라이언트는 액세스 토큰을 사용하여 리소스 서버의 리소스에 액세스합니다.

그렇다면 인증 서버가 액세스 토큰을 직접 반환하지 않은 이유는 무엇입니까? OAuth 2는 권한 부여 서버가 직접 액세스 토큰을 반환하는 `암시적 부여 유형`이라는 흐름을 정의합니다. `암시적 부여 유형`은 사용이 권장되지 않고 오늘날 대부분의 인증 서버에서 허용하지 않기 때문에 이 섹션에서 열거하지 않습니다. 

권한 부여 서버가 해당 토큰을 받는 것이 실제로 올바른 클라이언트인지 확인하지 않고 액세스 토큰을 사용하여 리디렉션 URI를 직접 호출한다는 단순한 사실로 인해 흐름이 덜 안전합니다. 먼저 인증 코드를 전송함으로써 클라이언트는 액세스 토큰을 얻기 위해 자격 증명을 사용하여 자신이 누구인지 다시 증명해야 합니다. 클라이언트는 액세스 토큰을 얻기 위해 최종 호출을 하고 다음을 보냅니다.

- 사용자가 권한을 부여했음을 증명하는 `인증 코드`

- 인증 코드를 가로챈 다른 사람이 아니라 실제로 동일한 클라이언트임을 증명하는 `자격 증명`
- 
2단계로 돌아가기 위해 클라이언트는 이제 권한 부여 서버에 요청합니다. 이 요청에는 다음 정보가 포함되어 있습니다.

- `code`, 1단계에서 받은 인증코드. 이것은 사용자가 인증되었음을 증명합니다.
- `client_id` 및 `client_secret`: 클라이언트의 자격 증명.
- `redirect_uri`: 확인을 위해 1단계에서 사용한 것과 동일.
- `grant_type`: 부여 유형을 식별하는 값 `authorization_code`. 

서버는 여러 흐름을 지원할 수 있으므로 항상 현재 실행되는 인증 흐름을 지정하는 것이 중요합니다.

응답으로 서버는 `access_token`을 다시 보냅니다. 이 토큰은 클라이언트가 리소스 서버에서 노출된 리소스를 호출하는 데 사용할 수 있는 값입니다.

#### 3단계: 승인 코드 부여 유형으로 보호된 리소스 호출

인증 서버에서 액세스 토큰을 성공적으로 얻은 후 클라이언트는 이제 보호된 리소스를 호출할 수 있습니다. 클라이언트는 리소스 서버의 끝점을 호출할 때 권한 부여 요청 헤더에서 액세스 토큰을 사용합니다.

> 권한부여 유형 승인 코드에 대한 비유

나는 때때로 오랫동안 알고 지낸 작은 가게에서 책을 산다. 나는 책을 미리 주문하고 며칠 후에 책을 가져와야 합니다. 하지만 그 가게는 내가 매일 가는 길에 있지 않기 때문에 가끔 내가 책을 가지러 가지 못할 때가 있다. 나는 보통 내 근처에 사는 친구에게 그곳에 가서 나를 위해 수집해 달라고 부탁합니다. 내 친구가 내 주문을 요청할 때, 가게의 아주머니가 전화를 걸어 책을 가져오도록 누군가를 보냈다는 것을 확인합니다. 내 확인 후, 내 친구가 소포를 수거하여 그날 나중에 나에게 가져다 줍니다.

이 비유에서 책은 자원입니다. 나는 그것들을 소유하고 있으므로 사용자(리소스 소유자)입니다. 나를 위해 그들을 데리러 내 친구는 클라이언트입니다. 책을 파는 아주머니가 인증 서버다. (우리는 그녀나 서점을 리소스 서버로 간주할 수도 있습니다.) 내 친구(클라이언트)에게 책(리소스)을 수집하는 권한을 부여하기 위해 책을 판매하는 여성(권한 서버)이 저(사용자)에게 전화를 거는 것을 관찰합니다. 곧장. 이 비유는 인증 코드 및 암시적 부여 유형의 프로세스를 설명합니다. 물론 이야기에 토큰이 없기 때문에 비유는 부분적이며 두 경우 모두를 설명합니다.

> **참고**
인증 코드 부여 유형은 사용자가 자신의 자격 증명을 클라이언트와 공유할 필요 없이 클라이언트가 특정 작업을 실행할 수 있도록 하는 큰 이점이 있습니다. 그러나 이 부여 유형에는 약점이 있습니다. 누군가가 인증 코드를 가로채면 어떻게 될까요? 물론 클라이언트는 앞에서 설명한 것처럼 자격 증명으로 인증해야 합니다. 그러나 클라이언트 자격 증명도 어떻게든 도난당하면 어떻게 될까요? 이 시나리오가 달성하기 쉽지 않더라도 이 부여 유형의 취약점으로 간주할 수 있습니다. 이 취약점을 완화하려면 PKCE(Proof Key for Code Exchange) 인증 코드 부여 유형이 제시하는 보다 복잡한 시나리오에 의존해야 합니다. RFC 7636: https://tools.ietf.org/html/rfc7636에서 직접 PKCE 인증 코드 부여 유형에 대한 훌륭한 설명을 찾을 수 있습니다. 

이 주제에 대한 훌륭한 토론을 위해 Neil Madden의 API Security in Action(Manning, 2020) 섹션 7.3.2(http://mng.bz/nzvV)도 읽을 것을 권장합니다.

### 12.3.2 패스워드 부여 유형 구현

이 섹션에서는 암호 부여 유형에 대해 논의합니다(그림 12.7). 이 부여 유형은 `리소스 소유자 자격 증명 부여 유형`이라고도 합니다. 이 흐름을 사용하는 애플리케이션은 클라이언트가 사용자 자격 증명을 사용하여 인증 서버에서 액세스 토큰을 인증하고 획득한다고 가정합니다.

11장의 실습 예제의 아키텍처는 암호 부여 유형에서 발생하는 것과 매우 유사합니다. 또한 13~15장에서 Spring Security를 ​​사용하여 실제 OAuth 2 암호 부여 유형 아키텍처를 구현합니다.

> **참고** 이 시점에서 리소스 서버가 토큰이 유효한지 여부를 어떻게 알 수 있는지 궁금할 것입니다. 13장과 14장에서 리소스 서버가 토큰의 유효성을 검사하는 데 사용하는 접근 방식에 대해 설명합니다. 권한 부여 서버가 액세스 토큰을 발행하는 방법만 언급하기 때문에 당분간은 부여 유형 논의에 집중해야 합니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F07_Spilca.png)

**그림 12.7** 암호 부여 유형은 사용자가 자신의 자격 증명을 클라이언트와 공유한다고 가정합니다. 클라이언트는 이를 사용하여 권한 부여 서버에서 토큰을 얻습니다. 그런 다음 사용자를 대신하여 리소스 서버에서 리소스에 액세스합니다.

클라이언트와 권한 부여 서버가 동일한 조직에서 구축 및 유지 관리되는 경우에만 이 인증 흐름을 사용합니다. 마이크로서비스 시스템을 구축하고 확장성을 향상하고 각 서비스에 대한 책임을 분리하기 위해 인증 책임을 다른 마이크로서비스로 분리하기로 결정했다고 가정해 보겠습니다.

또한 시스템 사용자가 Angular, ReactJS 와 같은 프론트엔드 프레임워크로 개발된 클라이언트 웹 애플리케이션을 사용하거나 모바일 앱을 사용한다고 가정해 보겠습니다. 이 경우 사용자는 인증을 위해 시스템에서 동일한 시스템으로 리디렉션되었다가 다시 돌아오는 것이 이상하다고 생각할 수 있습니다. 이것은 권한 부여 코드 부여 유형과 같은 흐름에서 발생하는 일입니다. 암호 부여 유형을 사용하면 대신 응용 프로그램이 사용자에게 로그인 양식을 제시하고 클라이언트가 인증을 위해 서버에 자격 증명을 보내도록 합니다. 사용자는 애플리케이션에서 인증 책임을 어떻게 설계했는지 알 필요가 없습니다. 암호 부여 유형을 사용할 때 어떤 일이 발생하는지 봅시다. 두 가지 작업은 다음과 같습니다.

1. 액세스 토큰을 요청합니다.
2. 액세스 토큰을 사용하여 리소스를 호출합니다.

#### 1단계: 패스워드 부여 유형 사용 시 액세스 토큰 요청

암호 부여 유형을 사용하면 흐름이 훨씬 간단합니다. 클라이언트는 사용자 자격 증명을 수집하고 권한 부여 서버를 호출하여 액세스 토큰을 얻습니다. 액세스 토큰을 요청할 때 클라이언트는 요청에서 다음 세부 정보도 보냅니다.

- `grant_type`: password 값.
- `client_id` 및 `client_secret`: 클라이언트 자격 증명입니다.
- `scope`: 부여된 권한.
- `username` 및 `password`: 사용자 자격 증명. 이들은 요청 헤더의 값으로 일반 텍스트로 전송됩니다.

클라이언트는 응답에서 액세스 토큰을 다시 받습니다. 클라이언트는 이제 액세스 토큰을 사용하여 리소스 서버의 끝점을 호출할 수 있습니다.

#### 2단계: 비밀번호 부여 유형을 사용할 때 액세스 토큰을 사용하여 리소스 호출

클라이언트에 액세스 토큰이 있으면 토큰을 사용하여 권한 부여 코드 부여 유형과 정확히 동일한 리소스 서버의 끝점을 호출합니다. 클라이언트는 권한 부여 요청 헤더의 요청에 액세스 토큰을 추가합니다.

> 비밀번호 부여 유형에 대한 비유

12.3.1에서 만든 비유를 다시 참조하기 위해 책을 파는 여자가 친구에게 책을 사주기를 원한다는 확인 전화를 하지 않는다고 상상해 보십시오. 대신에 나는 내 친구에게 책을 데리러 가도록 위임했다는 것을 증명하기 위해 내 ID를 내 친구에게 줄 것입니다. 차이점이 보이시나요? 이 흐름에서는 내 ID(자격 증명)를 클라이언트와 공유해야 합니다. 이러한 이유로 리소스 소유자가 클라이언트를 "신뢰"하는 경우에만 이 권한 부여 유형이 적용된다고 말합니다.

> **참고** 암호 부여 유형은 주로 사용자 자격 증명을 클라이언트 앱과 공유한다고 가정하기 때문에 인증 코드 부여 유형보다 덜 안전합니다. 승인 코드 부여 유형보다 더 간단하고 이것이 이론적인 예에서 많이 사용되는 주된 이유인 것은 사실이지만 실제 시나리오에서는 이 부여 유형을 피하십시오. 권한 부여 서버와 클라이언트가 모두 같은 조직에서 구축되어 있더라도 먼저 권한 부여 코드 부여 유형을 사용하는 것에 대해 생각해야 합니다. 두 번째 옵션으로 비밀번호 부여 유형을 선택하십시오.

### 12.3.3 클라이언트 자격 증명 부여 유형 구현

클라이언트 자격 증명 부여 유형은 가장 간단한 권한 부여 유형이며 사용자가 관여하지 않을 때 사용할 수 있습니다. 즉, 두 응용 프로그램 간의 인증을 구현할 때입니다. 클라이언트 자격 증명 부여 유형을 암호 부여 유형과 API 키 인증 흐름의 조합으로 생각하고 싶습니다. OAuth 2로 인증을 구현하는 시스템이 있다고 가정합니다. 이제 외부 서버가 인증하고 서버가 노출하는 특정 리소스를 호출하도록 허용해야 합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F08_Spilca.png)

그림 12.8 클라이언트 자격 증명 부여 유형. 클라이언트가 리소스 소유자를 대신하지 않고 리소스에 액세스해야 하는 경우 이 흐름을 사용합니다. 이 리소스는 사용자가 소유하지 않은 끝점일 수 있습니다.

9장에서 필터 구현에 대해 논의했습니다. API 키를 사용한 인증으로 구현을 보강하기 위해 사용자 지정 필터를 만드는 방법을 배웠습니다. OAuth 2 흐름을 사용하여 이 접근 방식을 계속 적용할 수 있습니다. 그리고 구현에서 OAuth 2를 사용하는 경우 OAuth 2 프레임워크 외부에 있는 사용자 정의 필터로 확장하는 것보다 모든 경우에 OAuth 2 프레임워크를 사용하는 것이 의심할 여지 없이 더 깔끔합니다.

클라이언트 자격 증명 부여 유형에 대한 단계는 암호 부여 유형과 유사합니다. 유일한 예외는 액세스 토큰 요청에 사용자 자격 증명이 필요하지 않다는 것입니다. 이 부여 유형을 구현하는 단계는 다음과 같습니다.

1. 액세스 토큰 요청
2. 액세스 토큰을 사용하여 리소스 호출

#### 1단계: 클라이언트 자격 증명 부여 유형으로 액세스 토큰 요청

액세스 토큰을 얻기 위해 클라이언트는 다음 세부 정보와 함께 권한 부여 서버에 요청을 보냅니다.

- `grant_type`: 값이 client_credentials
- `client_id` 및 `client_secret`: 클라이언트 자격 증명
- `scope`: 부여된 권한

이에 대한 응답으로 클라이언트는 `액세스 토큰`을 받습니다. 클라이언트는 이제 액세스 토큰을 사용하여 리소스 서버의 끝점을 호출할 수 있습니다.

#### 2단계: 액세스 토큰을 사용하여 리소스 호출

클라이언트에 액세스 토큰이 있으면 해당 토큰을 사용하여 리소스 서버의 끝점을 호출합니다. 이는 인증 코드 부여 유형 및 암호 부여 유형과 정확히 같습니다. 클라이언트는 권한 부여 요청 헤더의 요청에 액세스 토큰을 추가합니다.

### 12.3.4 리프레시 토큰을 사용하여 새로운 액세스 토큰 얻기

액세스 토큰이 어떻게 구현되든 만료될 수 있다는 것입니다. 필수 사항은 아닙니다. 수명이 무한한 토큰을 만들 수 있지만 일반적으로 가능한 한 짧게 만들어야 합니다. 리프레시 토큰은 새 액세스 토큰을 얻기 위해 자격 증명을 사용하는 것에 대한 대안을 나타냅니다. 리프레시 토큰이 OAuth 2에서 어떻게 작동하는지 보여주고 13장에서 애플리케이션으로 구현된 것을 볼 수도 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F09_Spilca.png)

**그림 12.9** 리프레시 토큰. 클라이언트에 만료된 액세스 토큰이 있습니다. 사용자가 강제로 다시 로그인하지 않도록 하기 위해 클라이언트는 리프레시 토큰을 사용하여 새 액세스 토큰을 발급합니다.

앱에서 만료되지 않는 토큰을 구현한다고 가정해 보겠습니다. 이는 클라이언트가 동일한 토큰을 반복해서 사용하여 리소스 서버의 리소스를 호출할 수 있음을 의미합니다. 토큰을 도난당하면 어떻게 됩니까? 결국 토큰이 모든 요청에 ​​간단한 HTTP 헤더로 첨부된다는 것을 잊지 마십시오. 토큰이 만료되지 않으면 토큰을 손에 넣은 사람이 토큰을 사용하여 리소스에 액세스할 수 있습니다. 만료되지 않는 토큰은 너무 강력합니다. 사용자 자격 증명만큼 강력해집니다. 우리는 이것을 피하고 토큰의 수명을 짧게 만드는 것을 선호합니다. 이렇게 하면 어느 시점에서 만료된 토큰을 더 이상 사용할 수 없습니다. 클라이언트는 다른 액세스 토큰을 얻어야 합니다.

새 액세스 토큰을 얻기 위해 클라이언트는 사용된 권한 부여 유형에 따라 흐름을 다시 실행할 수 있습니다. 예를 들어 부여 유형이 인증 코드인 경우 클라이언트는 사용자를 인증 서버 로그인 끝점으로 리디렉션하고 사용자는 사용자 이름과 암호를 다시 입력해야 합니다. 정말 사용자 친화적이지 않습니까? 토큰의 수명이 20분이고 온라인 앱으로 몇 시간 동안 작업한다고 상상해 보세요. 그 시간 동안 앱은 다시 로그인하기 위해 약 6번 리디렉션합니다. (오 안돼! 그 앱은 나를 다시 로그아웃시켰다!) 재인증의 필요성을 피하기 위해 인증 서버는 액세스 토큰과 다른 값과 목적을 가진 리프레시 토큰을 발급할 수 있습니다. 앱은 재인증하는 대신 리프레시 토큰을 사용하여 새 액세스 토큰을 얻습니다.

리프레시 토큰은 또한 암호 부여 유형에서 재인증보다 이점이 있습니다. 암호 부여 유형을 사용하더라도 리프레시 토큰을 사용하지 않으면 사용자에게 다시 인증을 요청하거나 자격 증명을 저장해야 합니다. 암호 부여 유형을 사용할 때 사용자 자격 증명을 저장하는 것은 가장 큰 실수 중 하나입니다! 

그리고 이 접근 방식이 실제 응용 프로그램에서 사용되는 것을 보았습니다! 안됩니다! 사용자 이름과 암호를 저장하면(그리고 이를 재사용할 수 있어야 하기 때문에 일반 텍스트 또는 되돌릴 수 있는 것으로 저장한다고 가정) 해당 자격 증명을 노출합니다. 리프레시 토큰을 사용하면 이 문제를 쉽고 안전하게 해결할 수 있습니다. 자격 증명을 안전하지 않게 저장하고 매번 사용자를 리디렉션할 필요 없이 리프레시 토큰을 저장하고 필요할 때 새 액세스 토큰을 얻는 데 사용할 수 있습니다. 

리프레시 토큰을 저장하는 것은 노출된 것을 발견하면 취소할 수 있기 때문에 더 안전합니다. 또한 사람들은 여러 앱에 대해 동일한 자격 증명을 사용하는 경향이 있음을 잊지 마십시오. 따라서 자격 증명을 잃는 것은 특정 응용 프로그램에서 사용할 수 있는 토큰을 잃는 것보다 더 나쁩니다.

마지막으로 리프레시 토큰을 사용하는 방법을 살펴보겠습니다. 리프레시 토큰은 어디서 얻나요? 인증 서버는 인증 코드 또는 암호 부여 유형과 같은 흐름을 사용할 때 액세스 토큰과 함께 리프레시 토큰을 반환합니다. 클라이언트 자격 증명 부여를 사용하면 이 흐름에 사용자 자격 증명이 필요하지 않기 때문에 리프레시 토큰이 없습니다. 클라이언트에 리프레시 토큰이 있으면 액세스 토큰이 만료될 때 다음 세부 정보가 포함된 요청을 발행해야 합니다.

- 값이 refresh_token인 `grant_type`.
- 리프레시 토큰 값이 있는 `refresh_token`.
- 클라이언트 자격 증명이 있는 `client_id` 및 `client_secret`.
- `scope`, 부여된 권한과 같거나 그 이하를 정의합니다. 더 많은 권한이 부여된 권한을 부여해야 하는 경우 재인증이 필요합니다.
- 
이 요청에 대한 응답으로 권한 부여 서버는 새 액세스 토큰과 새 리프레시 토큰을 발급합니다.

## 12.4 OAuth 2의 취약점

 OAuth 2 인증 및 권한 부여의 가능한 취약점에 대해 설명합니다. 이러한 시나리오를 피할 수 있도록 OAuth 2를 사용할 때 무엇이 ​​잘못될 수 있는지 이해하는 것이 중요합니다. 물론 소프트웨어 개발의 다른 모든 것과 마찬가지로 OAuth 2는 방탄이 아닙니다. 여기에는 우리가 인식해야 하고 애플리케이션을 구축할 때 고려해야 하는 취약점이 있습니다. 여기에 가장 일반적인 몇 가지를 열거합니다.

- 클라이언트에서 CSRF(교차 사이트 요청 위조) 사용 -- 사용자가 로그인한 상태에서 애플리케이션이 CSRF 보호 메커니즘을 적용하지 않는 경우 CSRF가 가능합니다. 우리는 10장에서 Spring Security에 의해 구현된 CSRF 보호에 대해 훌륭한 토론을 했습니다.

- 클라이언트 자격 증명 도용 -- 보호되지 않은 자격 증명을 저장하거나 전송하면 공격자가 도용하여 사용할 수 있는 위반이 발생할 수 있습니다.

- 토큰 재생 -- 13장과 14장에서 배우게 될 것처럼 토큰은 리소스에 액세스하기 위해 OAuth 2 인증 및 권한 부여 아키텍처 내에서 사용하는 키입니다. 네트워크를 통해 전송하지만 때때로 가로챌 수 있습니다. 가로채면 도난당하고 재사용할 수 있습니다. 집 현관문에서 열쇠를 잃어버렸다고 상상해 보십시오. 무슨 일이 일어날 수 있습니까? 다른 사람이 원하는 만큼 문을 여는 데 사용할 수 있습니다(재생). 토큰과 토큰 재생을 피하는 방법에 대해 14장에서 더 배울 것입니다.
  
- 토큰 하이재킹 -- 누군가 인증 프로세스를 방해하고 리소스에 액세스하는 데 사용할 수 있는 토큰을 훔치는 것을 암시합니다. 이것은 또한 리프레시 토큰을 사용하는 잠재적인 취약점이기도 합니다. 이러한 토큰도 가로채서 새 액세스 토큰을 얻는 데 사용할 수 있기 때문입니다. 이 유용한 기사를 추천합니다.
http://blog.intothesymmetry.com/2015/06/on-oauth-token-hijacks-for-fun-and.html

OAuth 2는 프레임워크임을 기억하십시오. 취약점은 그 위에 기능을 잘못 구현한 결과입니다. Spring Security를 ​​사용하면 이미 애플리케이션에서 이러한 취약점 대부분을 완화하는 데 도움이 됩니다. Spring Security로 애플리케이션을 구현할 때 이 장에서 볼 수 있듯이 구성을 설정해야 하지만 Spring Security에 의해 구현된 흐름에 의존합니다.

OAuth 2 프레임워크와 관련된 취약성과 기만적인 개인이 이를 악용할 수 있는 방법에 대한 자세한 내용은 Justin Richer와 Antonio Sanso의 OAuth 2 In Action(Manning, 2017)의 3부에서 훌륭한 토론을 찾을 수 있습니다. 링크는 다음과 같습니다.

https://livebook.manning.com/book/oauth-2-in-action/part-3


## 12.5 간단한 SSO 애플리케이션 구현

Spring Security와 함께 OAuth 2 프레임워크를 사용하는 책의 첫 번째 애플리케이션을 구현합니다. 이 예제에서는 Spring Security와 함께 OAuth 2를 적용하는 방법에 대한 일반적인 개요를 보여주고 알아야 할 첫 번째 계약 중 일부를 알려줍니다. `Single Sign-On 응용 프로그램은 이름에서 알 수 있듯이 권한 부여 서버를 통해 인증한 다음 리프레시 토큰을 사용하여 앱에서 로그인 상태를 유지하는 응용 프로그램입니다.` 우리의 경우 OAuth 2 아키텍처의 클라이언트만 나타냅니다.

이 애플리케이션(그림 12.10)에서는 GitHub를 인증 및 리소스 서버로 사용하고 인증 코드 부여 유형이 있는 구성 요소 간의 통신에 중점을 둡니다. 13장과 14장에서는 OAuth 2 아키텍처에서 인증 서버와 리소스 서버를 모두 구현할 것입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F10_Spilca.png)

**그림 12.10** 우리 애플리케이션은 OAuth 2 아키텍처에서 클라이언트 역할을 합니다. GitHub를 인증 서버로 사용하지만 리소스 서버의 역할도 수행하므로 사용자의 세부 정보를 검색할 수 있습니다.

### 12.5.1 인증 서버 관리

이 장에서는 자체 인증 서버를 구현하지 않고 대신 기존 서버인 GitHub를 사용합니다. 13장에서는 자신의 인증 서버를 구현하는 방법을 배웁니다.

그렇다면 GitHub와 같은 타사를 인증 서버로 사용하려면 어떻게 해야 할까요? 즉, 결국 우리 애플리케이션은 사용자를 관리하지 않으며 누구나 GitHub 계정을 사용하여 애플리케이션에 로그인할 수 있습니다. 다른 인증 서버와 마찬가지로 GitHub는 토큰을 발급하는 클라이언트 애플리케이션을 알아야 합니다. 

OAuth 2 권한 부여에 대해 논의한 섹션 12.3에서 요청에 클라이언트 ID와 클라이언트 암호가 사용되었음을 기억하십시오. 클라이언트는 이러한 자격 증명을 사용하여 권한 부여 서버에서 자신을 인증하므로 OAuth 애플리케이션을 GitHub 권한 부여 서버에 등록해야 합니다. 이를 위해 다음 링크를 사용하여 짧은 양식(그림 12.11)을 완성합니다.

https://github.com/settings/applications/new

새 OAuth 애플리케이션을 추가할 때 애플리케이션의 이름, 홈페이지 및 GitHub가 애플리케이션을 다시 호출할 링크를 지정해야 합니다. 이것이 작동하는 OAuth 2 부여 유형은 인증 코드 부여 유형입니다. 이 승인 유형은 클라이언트가 로그인을 위해 사용자를 인증 서버(이 경우 GitHub)로 리디렉션 한 다음, 12.3.1에서 논의한 대로 인증 서버가 정의된 URL에서 클라이언트를 다시 호출한다고 가정합니다. 이것이 여기에서 콜백 URL을 식별해야 하는 이유입니다. 내 시스템에서 예제를 실행하기 때문에 두 경우 모두 localhost를 사용합니다. 그리고 포트(기본값은 8080)를 변경하지 않았기 때문에 http://localhost:8080이 내 홈페이지 URL이 됩니다. 콜백에 동일한 URL을 사용합니다.

> **참고** GitHub의 클라이언트 측(브라우저)은 localhost를 호출합니다. 이것이 애플리케이션을 로컬에서 테스트할 수 있는 방법입니다.
 
![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F11_Spilca.png)

**그림 12.11** GitHub를 인증 서버로 사용하여 애플리케이션을 OAuth 2 클라이언트로 사용하려면 먼저 등록해야 합니다. GitHub에 새 OAuth 애플리케이션을 추가하기 위한 양식을 작성하면 됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F12_Spilca.png)

**그림 12.12** GitHub에 OAuth 애플리케이션을 등록하면 클라이언트에 대한 자격 증명을 받습니다. 애플리케이션 구성에서 이를 사용합니다.
양식을 작성하고 애플리케이션 등록을 선택하면 GitHub에서 클라이언트 ID와 클라이언트 암호를 제공합니다(그림 12.12).

> **참고** 이미지에 보이는 응용 프로그램을 삭제했습니다. 이러한 자격 증명은 기밀 정보에 대한 액세스를 제공하므로 이러한 자격 증명을 그대로 둘 수 없습니다. 이러한 이유로 자격 증명을 재사용할 수 없습니다. 이 섹션에 나와 있는 대로 직접 생성해야 합니다. 또한 이러한 자격 증명을 사용하여 응용 프로그램을 작성할 때 특히 공용 Git 저장소를 사용하여 저장하는 경우 주의하십시오.

이 구성은 인증 서버에 대해 수행해야 하는 모든 것입니다. 이제 클라이언트 자격 증명이 있으므로 애플리케이션 작업을 시작할 수 있습니다.

### 12.5.2 STARTING THE IMPLEMENTATION

SSO 애플리케이션 구현을 시작합니다. 이 예제는 ssia-ch12-ex1 프로젝트에서 찾을 수 있습니다.

먼저 웹 페이지를 보호해야 합니다. 이를 위해 컨트롤러 클래스와 애플리케이션을 나타내는 간단한 HTML 페이지를 만듭니다. 다음 목록은 앱의 단일 끝점을 정의하는 `MainController` 클래스를 나타냅니다.

Listing 12.1 The controller class
```java
@Controller
public class MainController {

  @GetMapping("/")
  public String main() {
    return "main.html";
  }
}
```
또한 내 Spring Boot 프로젝트의 resources/static 폴더에 main.html 페이지를 정의합니다. 페이지에 액세스할 때 다음을 관찰할 수 있도록 제목 텍스트만 포함합니다.

```html
<h1>안녕하세요!</h1>
```
그리고 이제 진짜 직업! 애플리케이션이 GitHub에서 로그인을 사용할 수 있도록 보안 구성을 설정해 보겠습니다. 익숙한 대로 구성 클래스를 작성하는 것으로 시작합니다. `WebSecurityConfigurerAdapter`를 확장하고 `configure(HttpSecurity http)` 메서드를 재정의합니다. 

그리고 이제 차이점: 4장에서 배운 것처럼 `httpBasic()` 또는 `formLogin()`을 사용하는 대신 `oauth2Login()`이라는 다른 메서드를 호출합니다. 이 코드는 다음 목록에 나와 있습니다.

Listing 12.2 설정 클래스
```java
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.oauth2Login();          ❶

    http.authorizeRequests()
          .anyRequest()          
          .authenticated();    ❷
  }
}
```
❶ 인증 방법 설정

❷ 사용자 인증이 필요

목록 12.2에서 HttpSecurity 개체에 대한 새 메서드인 `oauth2Login()`을 호출합니다. 하지만 무슨 일이 일어나고 있는지 알고 있습니다. httpBasic() 또는 formLogin()과 마찬가지로 oauth2Login()은 단순히 필터 체인에 새 인증 필터를 추가합니다. 우리는 9장에서 필터에 대해 논의했습니다. 여기서 Spring Security에는 일부 필터 구현이 있으며 필터 체인에 사용자 정의를 추가할 수도 있습니다. 이 경우 프레임워크가 `oauth2Login()` 메서드를 호출할 때 필터 체인에 추가하는 필터는 `OAuth2LoginAuthenticationFilter`입니다(그림 12.13). 이 필터는 요청을 가로채고 OAuth 2 인증에 필요한 로직을 적용합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F13_Spilca.png)

**그림 12.13** HttpSecurity 개체에서 oauth2Login() 메서드를 호출하여 필터 체인에 OAuth2LoginAuthenticationFilter를 추가합니다. 요청을 가로채 OAuth 2 인증 로직을 적용합니다.

### 12.5.3 ClientRegistration 구현하기

클라이언트와 인증 서버 간의 링크 구현에 대해 설명합니다. 애플리케이션이 실제로 어떤 작업을 수행하도록 하려면 이것은 매우 중요합니다. 지금 그대로 시작하면 메인 페이지에 접근할 수 없습니다. 페이지에 액세스할 수 없는 이유는 모든 요청에 ​​대해 사용자가 인증해야 한다고 지정했지만 인증 방법을 제공하지 않았기 때문입니다. GitHub가 인증 서버임을 설정해야 합니다. 이를 위해 Spring Security는 `ClientRegistration` 계약을 정의합니다.

`ClientRegistration` 인터페이스는 OAuth 2 아키텍처에서 클라이언트를 나타냅니다. 클라이언트의 경우 필요한 모든 세부 정보를 정의해야 합니다.

- 클라이언트 id 및 secret
- 인증에 사용되는 부여 유형
- 리디렉션 URI
- scope

12.3에서 애플리케이션이 인증 프로세스를 위해 이러한 모든 세부 정보를 필요로 한다는 것을 볼 수 있습니다. Spring Security는 또한 2장부터 `UserDetails` 인스턴스를 빌드하는 데 이미 사용한 것과 유사한 빌더의 인스턴스를 생성하는 쉬운 방법을 제공합니다. Listing 12.3은 Spring Security를 ​​사용하여 클라이언트 구현을 나타내는 그러한 인스턴스를 빌드하는 방법을 보여줍니다. 다음 목록에서 모든 세부 정보를 제공하는 방법을 보여주지만 일부 공급자의 경우 이 섹션의 뒷부분에서 이보다 훨씬 쉽다는 것을 알게 될 것입니다.

**Listing 12.3** Creating a ClientRegistration instance
```java
ClientRegistration cr = 
   ClientRegistration.withRegistrationId("github")
     .clientId("a7553955a0c534ec5e6b")
     .clientSecret("1795b30b425ebb79e424afa51913f1c724da0dbb")
     .scope(new String[]{"read:user"})
     .authorizationUri("https://github.com/login/oauth/authorize")
     .tokenUri("https://github.com/login/oauth/access_token")
     .userInfoUri("https://api.github.com/user")
     .userNameAttributeName("id")
     .clientName("GitHub")
     .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
     .redirectUriTemplate("{baseUrl}/{action}/oauth2/code/{registrationId}")
     .build();
```

오! 그 모든 세부 사항은 어디에서 왔습니까? 목록 12.3이 언뜻 보기에는 무섭게 보일 수 있다는 것을 알고 있지만 클라이언트 ID와 secret을 설정하는 것 이상은 아닙니다. 또한 목록 12.3에서 내가 선택한 범위(부여된 권한), 클라이언트 이름 및 등록 ID를 정의합니다. 이러한 세부 정보 외에도 인증 서버의 URL을 제공해야 했습니다.

- 권한 부여 URI--클라이언트가 인증을 위해 사용자를 리디렉션하는 URI입니다.

- 토큰 URI--섹션 12.3에서 설명한 대로 클라이언트가 액세스 토큰과 새로 고침 토큰을 얻기 위해 호출하는 URI입니다.

- 사용자 정보 URI--사용자에 대한 자세한 정보를 얻기 위해 액세스 토큰을 얻은 후 클라이언트가 호출할 수 있는 URI입니다.

그 많은 URI는 어디에서 얻었습니까? 아마도 우리의 경우와 같이 권한 부여 서버가 개발되지 않은 경우 문서에서 가져와야합니다. 예를 들어 GitHub의 경우 여기에서 찾을 수 있습니다.

https://developer.github.com/apps/building-oauth-apps/authorizing-oauth-apps/

Spring Security는 이보다 훨씬 더 똑똑합니다. 프레임워크는 `CommonOAuth2Provider`라는 클래스를 정의합니다. 이 클래스는 다음을 포함하여 인증에 사용할 수 있는 가장 일반적인 공급자에 대한 `ClientRegistration` 인스턴스를 부분적으로 정의합니다.

- Google
- GitHub
- Facebook
- Okta

If you use one of these providers, you can define your `ClientRegistration` as presented in the next listing.

Listing 12.4 Using the CommonOAuth2Provider class
```java
ClientRegistration cr = 
   CommonOAuth2Provider.GITHUB ❶
     .getBuilder("github") ❷
       .clientId("a7553955a0c534ec5e6b") ❸
       .clientSecret("1795b30b42. . .") ❸
       .build(); ❹
```
❶ 해당 URI를 이미 설정하려면 GitHub 공급자를 선택합니다.

❷ 클라이언트 등록을 위한 아이디 제공

❸ 클라이언트 자격 증명 설정

❹ `ClientRegistration` 인스턴스를 빌드합니다.

보시다시피 훨씬 깔끔하며 인증 서버의 URL을 수동으로 찾아 설정할 필요가 없습니다. 물론 이것은 일반 공급자에게만 적용됩니다. 인증 서버가 공통 제공자에 속하지 않는 경우 목록 12.3에 표시된 대로 ClientRegistration을 완전히 정의하는 것 외에는 다른 옵션이 없습니다.

> 참고 `CommonOAuth2Provider` 클래스의 값을 사용한다는 것은 사용하는 공급자가 URL 및 기타 관련 값을 변경하지 않는다는 사실에 의존한다는 의미이기도 합니다. 그럴 가능성은 없지만 이 상황을 피하려면 목록 12.3에 표시된 대로 ClientRegistration을 구현하는 것이 좋습니다. 이를 통해 구성 파일에서 URL 및 관련 공급자 값을 구성할 수 있습니다.

다음 목록에 표시된 대로 `ClientRegistration` 개체를 반환하는 개인 메서드를 구성 클래스에 추가하여 이 섹션을 끝냅니다. 섹션 12.5.4에서 Spring Security가 인증에 사용하도록 이 클라이언트 등록 객체를 등록하는 방법을 배웁니다.

**Listing 12.5** Building the `ClientRegistration` object in the configuration class
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  private ClientRegistration clientRegistration() { ❶
    return CommonOAuth2Provider.GITHUB  ❷
            .getBuilder("github")
            .clientId("a7553955a0c534ec5e6b") ❸
            .clientSecret("1795b30b425ebb79e424afa51913f1c724da0dbb") ❸
            .build();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.oauth2Login();

    http.authorizeRequests()
           .anyRequest()
             .authenticated();
  }
}
```

❶ 나중에 이 메서드를 호출하여 반환된 ClientRegistration을 얻습니다.

❷ Spring Security가 GitHub common provider에 제공하는 설정부터 시작

❸ 고객 자격 증명 제공

> **참고** 클라이언트 ID와 클라이언트 secret은 자격 증명이므로 민감한 데이터가 됩니다. 실제 응용 프로그램에서는 비밀 저장소에서 가져와야 하며 소스 코드에 자격 증명을 직접 작성해서는 안 됩니다.

### 12.5.4 ClientRegistrationRepository 구현

인증에 사용할 `ClientRegistration` 인스턴스를 등록하는 방법을 배웁니다. 12.5.3에서 `ClientRepository` 계약을 구현하여 클라이언트를 표현하는 방법을 배웠습니다. 이것을 인증에 사용하도록 설정해야 합니다. 이를 위해 `ClientRegistrationRepository` 객체를 사용합니다(그림 12.14).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F14_Spilca.png)

**그림 12.14** ClientRegistrationRepository는 ClientRegistration 세부 정보(클라이언트 ID, 클라이언트 암호, URL, scope 등)를 검색합니다. 인증 필터에는 인증 흐름에 대한 이러한 세부 정보가 필요합니다.

`ClientRegistrationRepository` 인터페이스는 `UserDetailsService`와 유사합니다. `UserDetailsService`가 사용자 이름으로 `UserDetails`를 찾는 것과 같은 방식으로 `ClientRegistrationRepository`는 등록 ID로 `ClientRegistration`을 찾습니다.

`ClientRegistrationRepository` 인터페이스를 구현하여 `ClientRegistration` 인스턴스를 찾을 위치를 프레임워크에 알릴 수 있습니다.

`InMemoryClientRegistrationRepository`의 인스턴스를 메모리에 저장하는 `ClientRegistrationRepository`에 대한 구현을 제공합니다. 이것은 `InMemoryUserDetailsManager`가 `UserDetails` 인스턴스에 대해 작동하는 방식과 유사하게 작동합니다.

끝으로 `InMemoryClientRegistrationRepository` 구현을 사용하여 `ClientRegistrationRepository`를 정의하고 이를 Spring 컨텍스트에서 빈으로 등록합니다. 

12.5.3에서 구축한 ClientRegistration 인스턴스를 `InMemoryClientRegistrationRepository` 생성자에 매개변수로 제공하여 `InMemoryClientRegistrationRepository`에 추가합니다.

**Listing 12.6** ClientRegistration 객체 등록
```java
@Configuration
public class ProjectConfig extends WebSecurityConfigurerAdapter {

  @Bean ❶
  public ClientRegistrationRepository clientRepository() {
    var client = clientRegistration();
    return new InMemoryClientRegistrationRepository(client);
  }

  private ClientRegistration clientRegistration() {
    return CommonOAuth2Provider.GITHUB.getBuilder("github")
            .clientId("a7553955a0c534ec5e6b")
            .clientSecret("1795b30b425ebb79e424afa51913f1c724da0dbb")
            .build();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.oauth2Login();

    http.authorizeRequests()
            .anyRequest().authenticated();
  }
}
```
❶ Spring 컨텍스트에 `ClientRegistrationRepository` 유형의 빈을 추가합니다. Bean에는 `ClientRegistration`에 대한 참조가 포함되어 있습니다.

`ClientRegistrationRepository`를 등록하는 방법의 대안으로 HttpSecurity 개체의 `oauth2Login()` 메서드 매개 변수로 Customizer 개체를 사용할 수 있습니다. 7장과 8장에서 `httpBasic()` 및 `formLogin()` 메서드로 유사한 작업을 수행한 다음 10장에서 cors() 및 csrf() 메서드로 유사한 작업을 수행하는 방법을 배웠습니다. 동일한 원칙이 여기에서도 적용됩니다. 이것을 `ssia-ch12-ex2`라는 프로젝트로 분리했습니다.

Listing 12.7 Configuring ClientRegistrationRepository with a Customizer
```java
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.oauth2Login(c -> { ❶
        c.clientRegistrationRepository(clientRepository());
    });

    http.authorizeRequests()
           .anyRequest()
             .authenticated();
  }

  private ClientRegistrationRepository clientRepository() {
    var c = clientRegistration();
    return new InMemoryClientRegistrationRepository(c);
  }

  private ClientRegistration clientRegistration() {
    return CommonOAuth2Provider.GITHUB.getBuilder("github")
            .clientId("a7553955a0c534ec5e6b")
            .clientSecret("1795b30b425ebb79e424afa51913f1c724da0dbb")
            .build();
  }
}
```
❶ Uses a Customizer to set the `ClientRegistrationRepository` instance

> **NOTE** 하나의 구성 옵션은 다른 것만큼 좋지만 코드를 이해하기 쉽게 유지하려면 구성 접근 방식을 혼합하지 마십시오. 컨텍스트에서 빈으로 모든 것을 설정하는 접근 방식을 사용하거나 코드 인라인 구성 스타일을 사용하십시오.


### 12.5.5 스프링 부트 구성의 마법

이 장의 앞부분에서 구축한 응용 프로그램을 구성하는 세 번째 접근 방식을 보여줍니다. Spring Boot는 마법을 사용하고 속성 파일에서 직접 ClientRegistration 및 `ClientRegistrationRepository` 객체를 빌드하도록 설계되었습니다. 이 접근 방식은 Spring Boot 프로젝트에서 드문 일이 아닙니다. 우리는 다른 객체에서도 이런 일이 일어나는 것을 봅니다. 예를 들어 속성 ​​파일을 기반으로 구성된 데이터 소스를 종종 볼 수 있습니다. 다음 코드는 `application.properties` 파일에서 예제에 대한 클라이언트 등록을 설정하는 방법을 보여줍니다.

```yaml
spring.security.oauth2.client.registration.github.client-id=a7553955a0c534ec5e6b
spring.security.oauth2.client.registration.github.client-secret=1795b30b425ebb79e424afa51913f1c724da0dbb
```
여기에서는 클라이언트 id와 secret 만 지정하면 됩니다. 공급자의 이름이 github이기 때문에 Spring Boot는 `CommonOAuth2Provider` 클래스에서 URI와 관련된 모든 세부 정보를 가져오는 것을 알고 있습니다. 이제 내 구성 클래스는 다음 목록에 표시된 것과 같습니다. 이 예제는 `ssia-ch12-ex3`이라는 별도의 프로젝트에서도 찾을 수 있습니다.

Listing 12.8 The configuration class
```java
@Configuration
public class ProjectConfig 
  extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.oauth2Login();

    http.authorizeRequests()
          .anyRequest()
          .authenticated();
  }
}
```
`ClientRegistration` 및 `ClientRegistrationRepository`는 속성 파일을 기반으로 Spring Boot에 의해 자동으로 생성되기 때문에 세부 사항을 지정할 필요가 없습니다. Spring Security에서 알려진 일반적인 공급자 이외의 공급자를 사용하는 경우 `spring.security.oauth2.client.provider`로 시작하는 속성 그룹을 사용하여 권한 부여 서버에 대한 세부 정보도 지정해야 합니다. 다음은 예제를 제공합니다.

```yaml
spring.security.oauth2.client.provider.myprovider.authorization-uri=<some uri>
spring.security.oauth2.client.provider.myprovider.token-uri=<some uri>
```
예제에서와 같이 메모리에 하나 이상의 인증 공급자를 보유하는 데 필요한 모든 것을 가지고 이 섹션에서 설명한 대로 구성하는 것을 선호합니다. 더 깨끗하고 관리하기 쉽습니다. 그러나 클라이언트 등록 세부 정보를 데이터베이스에 저장하거나 웹 서비스에서 가져오는 것과 같이 다른 것이 필요한 경우 ClientRegistrationRepository의 사용자 지정 구현을 만들어야 합니다. 이 경우 12.5.5에서 배운 대로 설정해야 합니다.

> **EXERCISE** Change the current application to store the authorization server details in a database.

### 12.5.6 인증된 사용자에 대한 세부 정보 얻기

인증된 사용자의 세부 정보를 가져오고 사용하는 방법에 대해 설명합니다. Spring Security 아키텍처에서 인증된 사용자의 세부 정보를 저장하는 것은 SecurityContext라는 것을 이미 알고 있습니다. 인증 프로세스가 끝나면 담당 필터는 SecurityContext에 인증 개체를 저장합니다. 응용 프로그램은 거기에서 사용자 세부 정보를 가져와 필요할 때 사용할 수 있습니다. OAuth 2 인증에서도 마찬가지입니다.

이 경우 프레임워크에서 사용하는 인증 객체의 이름은 `OAuth2AuthenticationToken`입니다. 6장에서 배운 것처럼 SecurityContext에서 직접 가져오거나 Spring Boot가 엔드포인트의 매개변수에 삽입하도록 할 수 있습니다. 다음 목록은 콘솔에서 사용자에 대한 세부 정보를 수신하고 인쇄하도록 컨트롤러를 변경한 방법을 보여줍니다. .

**Listing 12.9** Using details of a logged in user
```java
@Controller
public class MainController {

  private Logger logger = Logger.getLogger(MainController.class.getName());

  @GetMapping("/")
  public String main(OAuth2AuthenticationToken token) { ❶
    logger.info(String.valueOf(token.getPrincipal()));
    return "main.html";
  }
}
```
❶ Spring Boot automatically injects the Authentication object representing the user in the method’s parameter.

### 12.5.7 TESTING THE APPLICATION

이 장에서 작업한 앱을 테스트합니다. 기능 확인과 함께 OAuth 2 인증 코드 부여 유형(그림 12.15)의 단계를 따라 올바르게 이해했는지 확인하고 Spring Security가 우리가 만든 구성으로 이를 적용하는 방법을 관찰합니다. 이 장에서 작성한 세 가지 프로젝트 중 하나를 사용할 수 있습니다. 이들은 구성을 작성하는 다른 방법으로 동일한 기능을 정의하지만 결과는 동일합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F15_Spilca.png)

**그림 12.15** 애플리케이션은 GitHub를 인증 서버와 리소스 서버로 사용합니다. 사용자가 로그인을 원할 때 클라이언트는 사용자를 GitHub 로그인 페이지로 리디렉션합니다. 사용자가 성공적으로 로그인하면 GitHub는 인증 코드를 사용하여 애플리케이션을 다시 호출합니다. 우리 애플리케이션은 인증 코드를 사용하여 액세스 토큰을 요청합니다. 그런 다음 애플리케이션은 액세스 토큰을 제공하여 리소스 서버(GitHub)에서 사용자 세부 정보에 액세스할 수 있습니다. 리소스 서버의 응답은 기본 페이지의 URL과 함께 사용자 세부 정보를 제공합니다.

먼저 GitHub에 로그인하지 않았는지 확인합니다. 또한 브라우저 콘솔을 열어 요청 탐색 기록을 확인합니다. 이 기록은 섹션 12.3.1에서 논의한 단계인 OAuth 2 흐름에서 발생하는 단계에 대한 개요를 제공합니다. 내가 인증되면 애플리케이션에서 나를 직접 기록합니다. 그런 다음 앱을 시작하고 브라우저에서 애플리케이션의 기본 페이지에 액세스합니다.

http://localhost:8080/ 응용 프로그램은 다음 코드의 URL로 리디렉션합니다(그림 12.16 참조). 이 URL은 GitHub의 `CommonOauth2Provider` 클래스에서 인증 URL로 구성됩니다.

https://github.com/login/oauth/authorize?response_type=code&client_id=a7553955a0c534ec5e6b&scope=read:user&state=fWwg5r9sKal4BMubg1oXBRrNn5y7VDW1A_rQ4UITbJk%3D&redirect_uri=http://localhost:8080/login/oauth2/code/github

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F16_Spilca.png)

**그림 12.16** 기본 페이지에 액세스한 후 브라우저는 GitHub 로그인으로 리디렉션합니다. Chrome 콘솔 도구에서 localhost에 대한 호출과 GitHub의 인증 엔드포인트에 대한 호출을 볼 수 있습니다.

섹션 12.3.1에서 논의한 대로 애플리케이션은 필요한 쿼리 매개변수를 URL에 첨부합니다. 이것들은

- 값 코드가 있는 `response_type`
- `client_id`
- `scope`(값 read:user도 `CommonOauth2Provider` 클래스에서 정의됨)
- CSRF 토큰으로 상태

GitHub 자격 증명을 사용하고 GitHub로 애플리케이션에 로그인합니다. 그림 12.17에서 볼 수 있듯이 인증되고 다시 리디렉션됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH12_F17_Spilca.png)

그림 12.17 자격 증명을 입력한 후 GitHub는 우리를 애플리케이션으로 다시 리디렉션합니다. 이제 메인 페이지를 볼 수 있으며 애플리케이션은 액세스 토큰을 사용하여 GitHub에서 사용자 세부 정보에 액세스할 수 있습니다.

다음 코드는 GitHub에서 우리를 다시 호출하는 URL을 보여줍니다. GitHub에서 애플리케이션이 액세스 토큰을 요청하는 데 사용하는 인증 코드를 제공하는 것을 볼 수 있습니다.

http://localhost:8080/login/oauth2/code/github?code=a3f20502c182164a4086&state=fWwg5r9sKal4BMubg1oXBRrNn5y7VDW1A_rQ4UITbJk%3D

애플리케이션에서 직접 발생하므로 브라우저에서 토큰 끝점에 대한 호출을 볼 수 없습니다. 그러나 콘솔에 인쇄된 사용자 세부 정보를 볼 수 있기 때문에 애플리케이션이 토큰을 가져왔다고 믿을 수 있습니다. 이는 앱이 엔드포인트를 호출하여 사용자 세부 정보를 검색할 수 있음을 의미합니다. 다음 코드는 이 출력의 일부를 보여줍니다.

```bsh
Name: [43921235], 
Granted Authorities: [[ROLE_USER, SCOPE_read:user]], User Attributes: [{login=lspil, id=43921235, node_id=MDQ6VXNlcjQzOTIxMjM1, avatar_url=https://avatars3.githubusercontent.com/u/43921235?v=4, gravatar_id=, url=https://api.github.com/users/lspil, html_url=https://github.com/lspil, followers_url=https://api.github.com/users/lspil/followers, following_url=https://api.github.com/users/lspil/following{/other_user}, ...
```

## Summary

- OAuth 2 프레임워크는 엔터티가 다른 사람을 대신하여 리소스에 액세스할 수 있도록 하는 방법을 설명합니다. 인증 및 권한 부여 논리를 구현하기 위해 애플리케이션에서 사용합니다.


- 애플리케이션이 액세스 토큰을 얻기 위해 사용할 수 있는 다양한 흐름을 권한 부여라고 합니다. 시스템 아키텍처에 따라 적합한 부여 유형을 선택해야 합니다.

- 인증 코드 부여 유형은 사용자가 권한 서버에서 직접 인증할 수 있도록 하여 클라이언트가 액세스 토큰을 얻을 수 있도록 하는 방식입니다. 사용자가 클라이언트를 신뢰하지 않고 자격 증명을 공유하고 싶지 않을 때 이 부여 유형을 선택합니다.

- 암호 부여 유형은 사용자가 자신의 자격 증명을 클라이언트와 공유함을 의미합니다. 클라이언트를 신뢰할 수 있는 경우에만 적용해야 합니다.

- 클라이언트 자격 증명 부여 유형은 클라이언트가 자격 증명으로만 인증하여 토큰을 얻음을 의미합니다. 클라이언트가 사용자의 리소스가 아닌 리소스 서버의 끝점을 호출해야 할 때 이 부여 유형을 선택합니다.

- Spring Security는 OAuth 2 프레임워크를 구현하므로 몇 줄의 코드로 애플리케이션에서 구성할 수 있습니다.

- Spring Security에서는 ClientRegistration의 인스턴스를 사용하여 인증 서버에서 클라이언트의 등록을 나타냅니다.

- 특정 클라이언트 등록을 찾는 역할을 하는 Spring Security OAuth 2 구현의 구성 요소를 ClientRegistrationRepository라고 합니다. Spring Security로 OAuth 2 클라이언트를 구현할 때 사용 가능한 ClientRegistration이 하나 이상 있는 ClientRegistrationRepository 객체를 정의해야 합니다.
