# 18 실습: OAuth 2 애플리케이션

이 장에서는 다음을 다룹니다.

- Keycloak을 인증 서버로 구성
- 리소스 서버에서 전역 메서드 보안 사용

12~15장에서 OAuth 2 시스템이 작동하는 방식과 Spring Security로 구현하는 방법에 대해 자세히 설명했습니다. 그런 다음 주제를 변경하고 16장과 17장에서 전역 메서드 보안을 사용하여 애플리케이션의 모든 계층에서 권한 부여 규칙을 적용하는 방법을 배웠습니다. 이 장에서는 이 두 가지 필수 주제를 결합하고 OAuth 2 리소스 서버 내에서 전역 메서드 보안을 적용합니다.

리소스 서버 구현의 다른 계층에서 권한 부여 규칙을 정의하는 것 외에도 Keycloak이라는 도구를 시스템의 권한 부여 서버로 사용하는 방법도 배우게 됩니다. 이 장에서 다룰 예제는 다음과 같은 이유로 도움이 됩니다.

- 시스템은 실제 구현에서 Keycloak과 같은 타사 도구를 사용하여 인증을 위한 추상화 계층을 정의하는 경우가 많습니다. OAuth 2 구현에서 Keycloak 또는 유사한 타사 도구를 사용해야 할 가능성이 높습니다. Okta, Auth0 및 LoginRadius와 같은 Keycloak에 대한 많은 가능한 대안을 찾을 수 있습니다. 이 장에서는 개발하는 시스템에서 이러한 도구를 사용해야 하는 시나리오에 중점을 둡니다.

- 실제 시나리오에서는 엔드포인트뿐만 아니라 애플리케이션의 다른 계층에도 적용된 권한 부여를 사용합니다. 그리고 이것은 OAuth 2 시스템에서도 발생합니다.

- 우리가 논의하는 기술과 접근 방식의 큰 그림을 더 잘 이해할 수 있습니다. 이를 위해 12장에서 17장에서 배운 내용을 다시 한 번 예를 들어 설명합니다.

다음 섹션으로 들어가 이 실습 장에서 구현할 애플리케이션의 시나리오를 알아보겠습니다.

# 18.1 적용 시나리오

피트니스 애플리케이션을 위한 백엔드를 구축해야 한다고 가정해 보겠습니다. 다른 훌륭한 기능 외에도 앱은 사용자의 운동 기록도 저장합니다. 이 장에서는 운동 기록을 저장하는 응용 프로그램 부분에 중점을 둘 것입니다. 백엔드는 세 가지 사용 사례를 구현해야 한다고 가정합니다. 사용 사례에 의해 정의된 각 작업에 대해 특정 보안 제한이 있습니다(그림 18.1). 세 가지 사용 사례는 다음과 같습니다.

- 사용자에 대한 새 운동 기록을 추가합니다. 운동이라는 데이터베이스 테이블에서 사용자, 운동 시작 및 종료 시간, 운동 난이도를 1~5 사이의 정수를 사용하여 저장하는 새 레코드를 추가합니다.

이 사용 사례에 대한 권한 부여 제한은 인증된 사용자가 자신을 위한 운동 기록만 추가할 수 있다고 주장합니다. 클라이언트는 리소스 서버에 의해 노출된 끝점을 호출하여 새 운동 기록을 추가합니다.

- 사용자의 모든 운동을 찾습니다. 클라이언트는 사용자의 기록에 운동 목록을 표시해야 합니다. 클라이언트는 엔드포인트를 호출하여 해당 목록을 검색합니다.

이 경우 권한 제한은 사용자가 자신의 운동 기록만 가져올 수 있음을 나타냅니다.

- 운동을 삭제합니다. 관리자 역할이 있는 모든 사용자는 다른 사용자의 운동을 삭제할 수 있습니다. 클라이언트는 운동 기록을 삭제하기 위해 끝점을 호출합니다.

권한 제한에 따르면 관리자만 레코드를 삭제할 수 있습니다.

두 가지 역할이 있는 세 가지 사용 사례를 구현해야 합니다. 두 가지 역할은 표준 사용자인 fitnessuser와 관리자인 fitnessadmin입니다. 피트니스 사용자는 자신을 위한 운동을 추가하고 자신의 운동 기록을 볼 수 있습니다. Fitnessadmin은 모든 사용자의 운동 기록만 삭제할 수 있습니다. 물론 관리자도 사용자가 될 수 있으며, 이 경우 스스로 운동을 추가하거나 자신이 기록한 운동을 볼 수도 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F01_Spilca.png)

그림 18.1 운동 기록이든 은행 계좌이든 애플리케이션은 사용자 데이터를 도난이나 원치 않는 변경으로부터 보호하기 위해 적절한 승인 규칙을 구현해야 합니다.

이 세 가지 사용 사례로 구현하는 백엔드는 OAuth 2 리소스 서버입니다(그림 18.2). 인증 서버도 필요합니다. 이 예에서는 Keycloak이라는 도구를 사용하여 시스템에 대한 인증 서버를 구성합니다. Keycloak은 사용자를 로컬로 설정하거나 다른 사용자 관리 서비스와 통합하여 모든 가능성을 제공합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F02_Spilca.png)

그림 18.2 시스템의 행위자는 사용자, 클라이언트, 권한 부여 서버 및 리소스 서버입니다. Keycloak을 사용하여 인증 서버를 구성하고 Spring Security를 ​​사용하여 리소스 서버를 구현합니다.

로컬 Keycloak 인스턴스를 인증 서버로 구성하여 구현을 시작합니다. 그런 다음 리소스 서버를 구현하고 Spring Security를 ​​사용하여 권한 부여 규칙을 설정합니다. 작동하는 애플리케이션이 있으면 cURL로 엔드포인트를 호출하여 테스트합니다.

## 18.2 Keycloak을 인증 서버로 설정하기

Keycloak을 시스템의 인증 서버로 구성합니다(그림 18.3). Keycloak은 ID 및 액세스 관리를 위해 설계된 우수한 오픈 소스 도구입니다. keycloak.org에서 Keycloak을 다운로드할 수 있습니다. Keycloak은 단순 사용자를 로컬에서 관리할 수 있는 기능을 제공하며 사용자 연합과 같은 고급 기능도 제공합니다. 이를 LDAP 및 Active Directory 서비스 또는 다른 ID 공급자에 연결할 수 있습니다. 예를 들어, 12장에서 논의한 일반적인 OAuth 2 공급자 중 하나에 연결하여 Keycloak을 고급 인증 계층으로 사용할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F03_Spilca.png)

그림 18.3 이 장에서 구현하는 실습 응용 프로그램의 일부로 세 가지 주요 단계를 따릅니다. 이 섹션에서는 첫 번째 단계로 Keycloak을 시스템의 인증 서버로 구성합니다.

Keycloak의 구성은 유연하지만 달성하고자 하는 바에 따라 복잡해질 수 있습니다. 이 장에서는 예제에서 수행해야 하는 설정에 대해서만 설명합니다. 우리의 설정은 역할이 있는 소수의 사용자만 정의합니다. 그러나 Keycloak은 이보다 훨씬 더 많은 일을 할 수 있습니다. 실제 시나리오에서 Keycloak을 사용할 계획이라면 먼저 공식 웹사이트(https://www.keycloak.org/documentation)에서 자세한 문서를 읽는 것이 좋습니다. Ken Finnigan의 Enterprise Java Microservices 9장(Manning, 2018)에서 작성자가 사용자 관리를 위해 Keycloak을 사용하는 마이크로서비스 보안에 대한 좋은 논의도 찾을 수 있습니다. 링크는 다음과 같습니다.

https://livebook.manning.com/book/enterprise-java-microservices/chapter-9
(마이크로서비스에 대한 토론을 즐긴다면 Ken Finnigan의 책 전체를 읽는 것이 좋습니다. 저자는 Java로 마이크로서비스를 구현하는 모든 사람이 알아야 할 주제에 대한 훌륭한 통찰력을 제공합니다.)

Keycloak을 설치하려면 공식 웹사이트 https://www.keycloak.org/downloads에서 최신 버전이 포함된 아카이브를 다운로드하기만 하면 됩니다. 그런 다음 폴더에 아카이브의 압축을 풀고 bin 폴더에 있는 독립 실행형 실행 파일을 사용하여 Keycloak을 시작할 수 있습니다. Linux를 사용하는 경우 standalone.sh를 실행해야 합니다. Windows의 경우 standalone.bat를 실행합니다.

Keycloak 서버를 시작하면 브라우저에서 http://localhost:8080에 액세스합니다. Keycloak의 첫 번째 페이지에서 사용자 이름과 암호를 입력하여 관리자 계정을 구성합니다(그림 18.4).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F04_Spilca.png)

그림 18.4 Keycloak을 관리하려면 먼저 관리자 자격 증명을 설정해야 합니다. 처음 시작할 때 Keycloak에 액세스하여 이 작업을 수행합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F05_Spilca.png)

그림 18.5 관리자 계정을 설정하면 방금 설정한 자격 증명을 사용하여 Keycloak의 관리 콘솔에 로그인할 수 있습니다.

관리자 자격 증명을 성공적으로 설정했습니다. 그런 다음 그림 18.5와 같이 Keycloak을 관리하기 위해 자격 증명으로 로그인합니다.

관리 콘솔에서 인증 서버 구성을 시작할 수 있습니다. Keycloak이 노출하는 OAuth 2 관련 엔드포인트를 알아야 합니다. 관리 콘솔에 로그인한 후 처음 방문하는 페이지인 Realm Settings 페이지의 General 섹션에서 이러한 엔드포인트를 찾을 수 있습니다(그림 18.6).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F06_Spilca.png)

그림 18.6 OpenID 끝점 구성 링크를 클릭하여 권한 부여 서버와 관련된 끝점을 찾습니다. 액세스 토큰을 얻고 리소스 서버를 구성하려면 이러한 끝점이 필요합니다.

다음 코드에서는 OpenID 끝점 구성 링크를 클릭하여 찾은 OAuth 2 구성의 일부를 추출했습니다. 이 구성은 토큰 끝점, 권한 부여 끝점 및 지원되는 부여 유형 목록을 제공합니다. 이러한 세부 사항은 12장에서 15장에서 논의한 바와 같이 여러분에게 익숙할 것입니다.

```json
{
  "issuer":
    "http://localhost:8080/auth/realms/master",

  "authorization_endpoint":
    "http://localhost:8080/auth/realms/master/protocol/openid-connect/auth",

   "token_endpoint":
    "http://localhost:8080/auth/realms/master/protocol/openid-connect/token",
   
   "jwks_uri":
"http://localhost:8080/auth/realms/master/protocol/openid-connect/certs",
   
   "grant_types_supported":[
      "authorization_code",
      "implicit",
      "refresh_token",
      "password",
      "client_credentials"
   ],
...
}
```

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F07_Spilca.png)

그림 18.7 애플리케이션을 테스트하기 위해 엔드포인트를 호출하는 데 사용하는 액세스 토큰을 수동으로 생성합니다. 토큰의 수명을 짧게 정의하면 더 자주 생성해야 하고 토큰을 사용하기 전에 만료되면 짜증이 날 수 있습니다.

수명이 긴 액세스 토큰을 구성했다면 앱 테스트가 더 편할 것입니다(그림 18.7).

그러나 실제 시나리오에서는 토큰에 긴 수명을 제공하지 않는 것을 잊지 마십시오. 예를 들어 프로덕션 시스템에서 토큰은 몇 분 안에 만료되어야 합니다. 그러나 테스트를 위해 하루 동안 활성 상태로 둘 수 있습니다. 그림 18.8과 같이 토큰 탭에서 토큰의 수명을 변경할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F08_Spilca.png)

그림 18.8 발급된 액세스 토큰이 빨리 만료되지 않으면 테스트가 더 편안할 수 있습니다. 토큰 탭에서 수명을 변경할 수 있습니다.

이제 Keycloak을 설치하고 관리자 자격 증명을 설정하고 몇 가지 조정을 수행했으므로 인증 서버를 구성할 수 있습니다. 다음은 구성 단계 목록입니다.

1. 시스템에 클라이언트를 등록합니다. OAuth 2 시스템에는 인증 서버에서 인식하는 클라이언트가 하나 이상 필요합니다. 클라이언트는 사용자에 대한 인증 요청을 합니다. 섹션 18.2.1에서 새 클라이언트 등록을 추가하는 방법을 배웁니다.

2. 클라이언트 범위를 정의합니다. 클라이언트 범위는 시스템에서 클라이언트의 목적을 식별합니다. 클라이언트 범위 정의를 사용하여 권한 부여 서버에서 발급한 액세스 토큰을 사용자 지정합니다. 섹션 18.2.2에서 클라이언트 범위를 추가하는 방법을 배우고 섹션 18.2.4에서 액세스 토큰을 사용자 지정하도록 구성합니다.

3. 애플리케이션에 사용자를 추가합니다. 리소스 서버에서 엔드포인트를 호출하려면 애플리케이션에 대한 사용자가 필요합니다. 섹션 18.2.3에서 Keycloak에서 관리하는 사용자를 추가하는 방법을 배웁니다.

4. 사용자 역할 및 사용자 지정 액세스 토큰을 정의합니다. 사용자를 추가한 후 액세스 토큰을 발급할 수 있습니다. 액세스 토큰에는 시나리오를 수행하는 데 필요한 모든 세부 정보가 포함되어 있지 않습니다. 섹션 18.2.4에서 Spring Security를 ​​사용하여 구현할 리소스 서버에서 예상하는 세부 정보를 표시하기 위해 사용자의 역할을 구성하고 액세스 토큰을 사용자 정의하는 방법을 배웁니다.

### 18.2.1 우리 시스템에 클라이언트 등록

이 섹션에서는 Keycloak을 인증 서버로 사용할 때 클라이언트를 등록하는 방법에 대해 설명합니다. 다른 OAuth 2 시스템과 마찬가지로 인증 서버 수준에서 클라이언트 애플리케이션을 등록해야 합니다. 새 클라이언트를 추가하기 위해 Keycloak 관리 콘솔을 사용합니다. 그림 18.9에서 볼 수 있듯이 왼쪽 메뉴의 클라이언트 탭으로 이동하여 클라이언트 목록을 찾을 수 있습니다. 여기에서 새 클라이언트 등록을 추가할 수도 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F09_Spilca.png)

그림 18.9 새 클라이언트를 추가하려면 왼쪽 메뉴의 클라이언트 탭을 사용하여 클라이언트 목록으로 이동합니다. 여기에서 클라이언트 테이블의 오른쪽 상단 모서리에 있는 만들기 버튼을 클릭하여 새 클라이언트 등록을 추가할 수 있습니다.

나는 Fitnessapp이라는 새 클라이언트를 추가했습니다. 이 클라이언트는 섹션 18.3에서 구현할 리소스 서버에서 엔드포인트를 호출할 수 있는 애플리케이션을 나타냅니다. 그림 18.10은 클라이언트 추가 양식을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F10_Spilca.png)

그림 18.10 클라이언트를 추가할 때 고유한 클라이언트 ID(fitnessapp)를 할당한 다음 저장을 클릭하기만 하면 됩니다.

### 18.2.2 클라이언트 범위 지정

이 섹션에서는 섹션 18.2.1에서 등록한 클라이언트의 범위를 정의합니다. 클라이언트 범위는 클라이언트의 목적을 식별합니다. 또한 섹션 18.2.4의 클라이언트 범위를 사용하여 Keycloak에서 발급한 액세스 토큰을 사용자 지정합니다. 클라이언트에 범위를 추가하기 위해 다시 Keycloak 관리 콘솔을 사용합니다. 그림 18.11에서 볼 수 있듯이 왼쪽 메뉴에서 클라이언트 범위 탭으로 이동하면 클라이언트 범위 목록을 찾을 수 있습니다. 여기에서 목록에 새 클라이언트 범위를 추가할 수도 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F11_Spilca.png) 

그림 18.11 모든 클라이언트 범위 목록을 보려면 클라이언트 범위 탭으로 이동하십시오. 여기에서 클라이언트 범위 테이블의 오른쪽 상단 모서리에 있는 만들기 버튼을 클릭하여 새 클라이언트 범위를 추가합니다.

이 실습 예제에서 빌드하는 앱의 경우 Fitnessapp이라는 새 클라이언트 범위를 추가했습니다. 새 범위를 추가할 때 클라이언트 범위를 설정한 프로토콜도 openid-connect인지 확인하십시오(그림 18.12).

> 참고 선택할 수 있는 다른 프로토콜은 SAML 2.0입니다. Spring Security는 이전에 https://projects.spring.io/spring-security-saml/#quick-start에서 여전히 찾을 수 있는 이 프로토콜에 대한 확장을 제공했습니다. SAML 2.0은 더 이상 Spring Security용으로 개발되지 않았기 때문에 이 책에서 SAML 2.0 사용에 대해 논의하지 않습니다. 또한 SAML 2.0은 애플리케이션에서 OAuth 2보다 덜 자주 발생합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F12_Spilca.png)

그림 18.12 새 클라이언트 범위를 추가할 때 고유한 이름을 지정하고 원하는 프로토콜에 대해 정의했는지 확인하십시오. 우리의 경우 우리가 원하는 프로토콜은 openid-connect입니다.

새 역할을 만든 후에는 그림 18.13과 같이 클라이언트에 할당합니다. 클라이언트 메뉴로 이동한 다음 클라이언트 범위 탭을 선택하면 이 화면이 표시됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F13_Spilca.png)

그림 18.13 클라이언트 범위가 있으면 클라이언트에 할당합니다. 이 그림에서 필요한 범위를 할당된 기본 클라이언트 범위라는 오른쪽 상자로 이미 이동했습니다. 이렇게 하면 이제 특정 클라이언트에서 정의된 범위를 사용할 수 있습니다.

### 18.2.3 사용자 추가 및 액세스 토큰 얻기

이 섹션에서는 애플리케이션에 대한 사용자를 만들고 구성합니다. 이전에는 섹션 18.2.1 및 18.2.2에서 클라이언트와 해당 범위를 구성했습니다. 그러나 클라이언트 앱 외에도 사용자가 리소스 서버에서 제공하는 서비스를 인증하고 액세스해야 합니다. 애플리케이션을 테스트하는 데 사용할 세 명의 사용자를 구성합니다(그림 18.14). 나는 사용자 이름을 Mary, Bill, Rachel이라고 지었습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F14_Spilca.png)

그림 18.14 왼쪽 메뉴에서 사용자 탭으로 이동하면 앱의 모든 사용자 목록을 찾을 수 있습니다. 여기에서 사용자 테이블의 오른쪽 상단 모서리에 있는 사용자 추가를 클릭하여 새 사용자를 추가할 수도 있습니다.

사용자 추가 양식에서 새 사용자를 추가할 때 고유한 사용자 이름을 지정하고 이메일이 확인되었다는 상자를 선택하십시오(그림 18.15). 또한 사용자에게 필수 사용자 작업이 없는지 확인하십시오. 사용자에게 필요한 사용자 작업이 보류 중인 경우 이를 인증에 사용할 수 없습니다. 따라서 해당 사용자에 대한 액세스 토큰을 얻을 수 없습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F15_Spilca.png)

그림 18.15 새 사용자를 추가할 때 사용자에게 고유한 사용자 이름을 부여하고 사용자에게 필요한 사용자 작업이 없는지 확인하십시오.

사용자를 생성한 후에는 사용자 목록에서 모든 사용자를 찾아야 합니다. 그림 18.16은 사용자 목록을 나타냅니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F16_Spilca.png)

그림 18.16 이제 새로 생성된 사용자가 사용자 목록에 나타납니다. 여기에서 사용자를 선택하여 편집하거나 삭제할 수 있습니다.

물론 사용자는 로그인할 때에도 암호가 필요합니다. 일반적으로 사용자는 자신의 암호를 구성하며 관리자는 자신의 자격 증명을 몰라야 합니다. 우리의 경우 3명의 사용자에 대해 암호를 직접 구성할 수 밖에 없습니다(그림 18.17). 예제를 단순하게 유지하기 위해 모든 사용자에 대해 암호 "12345"를 구성했습니다. 또한 임시 확인란을 선택 취소하여 암호가 임시가 아님을 확인했습니다. 비밀번호를 임시로 설정하면 Keycloak은 사용자가 처음 로그인할 때 비밀번호를 변경하는 데 필요한 작업을 자동으로 추가합니다. 이 필수 작업 때문에 사용자를 인증할 수 없습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F17_Spilca.png)

그림 18.17 목록에서 사용자를 선택하여 자격 증명을 변경하거나 구성할 수 있습니다. 변경 사항을 저장하기 전에 임시 확인란을 OFF로 설정했는지 확인하십시오. 자격 증명이 임시인 경우 사용자에게 미리 인증할 수 없습니다.

사용자가 구성되면 이제 Keycloak으로 구현된 인증 서버에서 액세스 토큰을 얻을 수 있습니다. 다음 코드 조각은 예제를 단순하게 유지하기 위해 암호 부여 유형을 사용하여 토큰을 얻는 방법을 보여줍니다. 그러나 섹션 18.2.1에서 관찰했듯이 Keycloak은 12장에서 논의된 다른 승인 유형도 지원합니다. 그림 18.18은 여기서 논의한 암호 승인 유형에 대한 리프레셔입니다.

액세스 토큰을 얻으려면 권한 부여 서버의 /token 엔드포인트를 호출하십시오.

```sh
curl -XPOST "http://localhost:8080/auth/realms/master/protocol/openid-connect/token" \
-H "Content-Type: application/x-www-form-urlencoded" \
--data-urlencode "grant_type=password" \
--data-urlencode "username=rachel" \
--data-urlencode "password=12345" \
--data-urlencode "scope=fitnessapp" \
--data-urlencode "client_id=fitnessapp"
```

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F18_Spilca.png)

그림 18.18 암호 부여 유형을 사용할 때 사용자는 자신의 자격 증명을 클라이언트와 공유합니다. 클라이언트는 자격 증명을 사용하여 권한 부여 서버에서 액세스 토큰을 얻습니다. 토큰을 사용하여 클라이언트는 리소스 서버에 의해 노출된 사용자의 리소스에 액세스할 수 있습니다.

HTTP 응답 본문에서 액세스 토큰을 받습니다. 다음 코드 스니펫은 응답을 보여줍니다.

```json
{
  "access_token":"eyJhbGciOiJIUzI...",
  "expires_in":6000,
  "refresh_expires_in":1800,
  "refresh_token":"eyJhbGciOiJIUz... ",
  "token_type":"bearer",
  "not-before-policy":0,
  "session_state":"1f4ddae7-7fe0-407e-8314-a8e7fcd34d1b",
  "scope":"fitnessapp"
}
```
> **참고** HTTP 응답에서 JWT 토큰이 길기 때문에 잘렸습니다.

다음 코드는 JWT 액세스 토큰의 디코딩된 JSON 본문을 나타냅니다. 토큰에 애플리케이션이 작동하는 데 필요한 모든 세부 정보가 포함되어 있지 않다는 것을 알 수 있습니다. 역할과 사용자 이름이 누락되었습니다. 섹션 18.2.4에서는 사용자에게 역할을 할당하고 리소스 서버에 필요한 모든 데이터를 포함하도록 JWT를 사용자 지정하는 방법을 배웁니다.

```json
{
  "exp": 1585392296,
  "iat": 1585386296,
  "jti": "01117f5c-360c-40fa-936b-763d446c7873",
  "iss": "http://localhost:8080/auth/realms/master",
  "sub": "c42b534f-7f08-4505-8958-59ea65fb3b47",
  "typ": "Bearer",
  "azp": "fitnessapp",
  "session_state": "fce70fc0-e93c-42aa-8ebc-1aac9a0dba31",
  "acr": "1",
  "scope": "fitnessapp"
}
```
### 18.2.4 사용자 역할 정의

섹션 18.2.3에서 액세스 토큰을 얻을 수 있었습니다.
또한 클라이언트 등록을 추가하고 사용자가 토큰을 얻도록 구성했습니다. 그러나 여전히 토큰에는 리소스 서버가 권한 부여 규칙을 적용하는 데 필요한 모든 세부 정보가 없습니다. 시나리오에 대한 완전한 앱을 작성하려면 사용자에 대한 역할을 추가해야 합니다.

사용자에게 역할을 추가하는 것은 간단합니다. 왼쪽 메뉴의 역할 탭에서는 그림 18.19와 같이 모든 역할 목록을 찾고 새 역할을 추가할 수 있습니다. 저는 두 개의 새로운 역할인 fitnessuser와 fitnessadmin을 만들었습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F19_Spilca.png)

그림 18.19 왼쪽 메뉴의 역할 탭에 액세스하여 정의된 모든 역할을 찾고 새 역할을 만들 수 있습니다. 그런 다음 사용자에게 할당합니다.

이제 이러한 역할을 사용자에게 할당합니다. 저는 관리자인 Mary에게 Fitnessadmin 역할을 할당했고 일반 사용자인 Bill과 Rachel은 Fitnessuser 역할을 맡았습니다. 그림 18.20은 사용자에게 역할을 연결하는 방법을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F20_Spilca.png)

그림 18.20 선택한 사용자의 역할 매핑 섹션에서 역할을 할당합니다. 이러한 역할 매핑은 액세스 토큰에서 사용자의 권한으로 나타나며 이를 사용하여 권한 부여 구성을 구현합니다.

불행히도 기본적으로 이러한 새로운 세부 정보는 액세스 토큰에 표시되지 않습니다. 애플리케이션의 요구 사항에 따라 토큰을 사용자 지정해야 합니다. 섹션 18.2.2에서 생성하고 토큰에 할당한 클라이언트 범위를 구성하여 토큰을 사용자 지정합니다. 토큰에 세 가지 세부 정보를 더 추가해야 합니다.

- 역할--시나리오에 따라 엔드포인트 계층에서 권한 부여 규칙의 일부를 적용하는 데 사용됩니다.
- 사용자 이름--인증 규칙을 적용할 때 데이터를 필터링합니다.

- 대상 클레임(aud) -- 18.3절에서 배우게 될 요청을 확인하기 위해 리소스 서버에서 사용합니다.

다음 코드 조각은 설정을 마치면 토큰에 추가되는 필드를 나타냅니다. 그런 다음 그림 18.21과 같이 클라이언트 범위에서 매퍼를 정의하여 사용자 지정 클레임을 추가합니다.

```json
{
  // ...

  "authorities": [
    "fitnessuser"
  ],
  "aud": "fitnessapp",
  "user_name": "rachel",

  // ...
}
```

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F21_Spilca.png)

그림 18.21 액세스 토큰을 사용자 지정하기 위해 특정 클라이언트 범위에 대한 매퍼를 만듭니다. 이러한 방식으로 리소스 서버가 요청을 승인하는 데 필요한 모든 세부 정보를 제공합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F22_Spilca.png)

그림 18.22는 토큰에 역할을 추가하기 위해 매퍼를 생성하는 방법을 보여줍니다. 이것이 리소스 서버가 예상하는 방식이기 때문에 토큰에 권한 키가 있는 역할을 추가합니다.
 
그림 18.22 액세스 토큰에 역할을 추가하기 위해 매퍼를 정의합니다. 매퍼를 추가할 때 이름을 제공해야 합니다. 또한 토큰에 추가할 세부 정보와 할당된 세부 정보를 식별하는 클레임 ​​이름을 지정합니다.

그림 18.22에 제시된 것과 유사한 접근 방식을 사용하여 토큰에 사용자 이름을 추가하는 매퍼를 정의할 수도 있습니다. 그림 18.23은 사용자 이름에 대한 매퍼를 생성하는 방법을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F23_Spilca.png)

그림 18.23 액세스 토큰에 사용자 이름을 추가하는 매퍼를 생성합니다. 액세스 토큰에 사용자 이름을 추가할 때 클레임의 이름인 user_name을 선택합니다. 이 이름은 리소스 서버가 토큰에서 찾을 것으로 예상하는 방식입니다.

마지막으로 청중을 지정해야 합니다. 대상 클레임(aud)은 액세스 토큰의 의도된 수신자를 정의합니다. 이 클레임에 대한 값을 설정하고 섹션 18.3에서 배우게 될 리소스 서버에 대해 동일한 값을 구성합니다. 그림 18.24는 Keycloak이 JWT에 aud 클레임을 추가할 수 있도록 매퍼를 정의하는 방법을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F24_Spilca.png)

그림 18.24 매퍼 유형인 Audience를 나타내는 aud 클레임은 액세스 토큰의 수신자를 정의하며 이 경우에는 리소스 서버입니다. 리소스 서버가 토큰을 수락하도록 리소스 서버 측에서 동일한 값을 구성합니다.

액세스 토큰을 다시 획득하여 디코딩하면 토큰 본문에서 권한, user_name 및 aud 클레임을 찾아야 합니다. 이제 이 JWT를 사용하여 리소스 서버에 의해 노출된 엔드포인트를 인증하고 호출할 수 있습니다. 이제 완전히 구성된 인증 서버가 있으므로 섹션 18.3에서 섹션 18.1에 제시된 시나리오에 대한 리소스 서버를 구현합니다. 다음 코드는 토큰의 본문을 보여줍니다.

```json
{
  "exp": 1585395055,
  "iat": 1585389055,
  "jti": "305a8f99-3a83-4c32-b625-5f8fc8c2722c",
  "iss": "http://localhost:8080/auth/realms/master",
  "aud": "fitnessapp", ❶
  "sub": "c42b534f-7f08-4505-8958-59ea65fb3b47",
  "typ": "Bearer",
  "azp": "fitnessapp",
  "session_state": "f88a4f08-6cfa-42b6-9a8d-a2b3ed363bdd",
  "acr": "1",
  "scope": "fitnessapp",
  "user_name": "rachel", ❶
  "authorities": [ ❶
    "fitnessuser" ❶
  ] ❶
}
```
## 18.3 리소스 서버 구현

이 섹션에서는 Spring Security를 사용하여 시나리오에 대한 리소스 서버를 구현합니다. 섹션 18.2에서 Keycloak을 시스템의 인증 서버로 구성했습니다(그림 18.25).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F25_Spilca.png)

그림 18.25 이제 Keycloak 인증 서버를 설정했으므로 실습 예제의 다음 단계인 리소스 서버 구현을 시작합니다.

리소스 서버를 구축하기 위해 ssia-ch18-ex1이라는 새 프로젝트를 만들었습니다. 클래스 디자인은 간단하며(그림 18.26) 컨트롤러, 서비스 및 저장소의 세 가지 계층을 기반으로 합니다. 우리는 이러한 각 레이어에 대한 권한 부여 규칙을 구현합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617297731/files/OEBPS/Images/CH18_F26_Spilca.png)

그림 18.26 리소스 서버에 대한 클래스 디자인. 컨트롤러, 서비스 및 저장소의 세 가지 계층이 있습니다. 구현된 사용 사례에 따라 이러한 계층 중 하나에 대한 권한 부여 규칙을 구성합니다.

pom.xml 파일에 종속성을 추가합니다.

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-oauth2</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.security</groupId>
   <artifactId>spring-security-data</artifactId>
</dependency>
<dependency>
   <groupId>mysql</groupId>
   <artifactId>mysql-connector-java</artifactId>
   <scope>runtime</scope>
</dependency>
```

workout 세부 정보를 데이터베이스에 저장하기 때문에 schema.sql 및 data.sql 파일도 프로젝트에 추가합니다. 이 파일에 SQL 쿼리를 넣어 데이터베이스 구조와 나중에 애플리케이션을 테스트할 때 사용할 수 있는 일부 데이터를 생성합니다. 간단한 테이블만 필요하므로 schema.sql 파일은 이 테이블을 생성하기 위한 쿼리만 저장합니다.

```sql
CREATE TABLE IF NOT EXISTS `spring`.`workout` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user` VARCHAR(45) NULL,
  `start` DATETIME NULL,
  `end` DATETIME NULL,
  `difficulty` INT NULL,
  PRIMARY KEY (`id`));
```

또한 응용 프로그램을 테스트하기 위해 workout 테이블에 몇 가지 기록이 필요합니다. 이러한 레코드를 추가하려면 data.sql 파일에 몇 가지 INSERT 쿼리를 작성합니다.

```sql
INSERT IGNORE INTO `spring`.`workout` 
(`id`, `user`, `start`, `end`, `difficulty`) VALUES 
(1, 'bill', '2020-06-10 15:05:05', '2020-06-10 16:10:07', '3');

INSERT IGNORE INTO `spring`.`workout` 
(`id`, `user`, `start`, `end`, `difficulty`) VALUES 
(2, 'rachel', '2020-06-10 15:05:10', '2020-06-10 16:10:20', '3');

INSERT IGNORE INTO `spring`.`workout` 
(`id`, `user`, `start`, `end`, `difficulty`) VALUES 
(3, 'bill', '2020-06-12 12:00:10', '2020-06-12 13:01:10', '4');

INSERT IGNORE INTO `spring`.`workout` 
(`id`, `user`, `start`, `end`, `difficulty`) VALUES 
(4, 'rachel', '2020-06-12 12:00:05', '2020-06-12 12:00:11', '4');
```
이 네 개의 INSERT 문을 사용하여 이제 사용자 Bill에 대한 두 개의 운동 기록과 사용자 Rachel이 테스트에 사용할 다른 두 개의 운동 기록이 있습니다. 애플리케이션 로직 작성을 시작하기 전에 application.properties 파일을 정의해야 합니다. 이미 포트 8080에서 실행 중인 Keycloak 인증 서버가 있으므로 리소스 서버의 포트를 9090으로 변경합니다. 또한 application.properties 파일에 Spring Boot가 데이터 소스를 생성하는 데 필요한 속성을 작성합니다. 다음 코드는 application.properties 파일의 내용을 보여줍니다.

```yml
server.port=9090
        
spring.datasource.url=jdbc:mysql://localhost/spring ?useLegacyDatetimeCode=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.datasource.initialization-mode=always
```
이제 JPA 엔터티와 Spring Data JPA 저장소를 먼저 구현해 보겠습니다. 다음 목록은 Workout이라는 JPA 엔티티 클래스를 나타냅니다.

Listing 18.1 The Workout class
```java
@Entity
public class Workout {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private String user;
  private LocalDateTime start;
  private LocalDateTime end;
  private int difficulty;

  // Omitted getter and setters
}
```
목록 18.2에서 Workout 엔티티에 대한 Spring Data JPA 저장소 인터페이스를 찾을 수 있습니다. 여기 저장소 계층에서 데이터베이스에서 특정 사용자에 대한 모든 운동 기록을 검색하는 방법을 정의합니다. 17장에서 배운 것처럼 @PostFilter를 사용하는 대신 쿼리에서 직접 제약 조건을 적용하도록 선택합니다.

목록 18.2 WorkoutRepository 인터페이스
```java
public interface WorkoutRepository 
  extends JpaRepository<Workout, Integer> {
    @Query("SELECT w FROM Workout w WHERE w.user = ?#{authentication.name}") ❶
    List<Workout> findAllByUser();
}
```
❶ SpEL 표현식은 보안 컨텍스트에서 인증된 사용자 이름의 값을 검색합니다.

이제 저장소가 있으므로 WorkoutService라는 서비스 클래스를 계속 구현할 수 있습니다. 목록 18.3은 WorkoutService 클래스의 구현을 보여줍니다. 컨트롤러는 이 클래스의 메서드를 직접 호출합니다. 우리 시나리오에 따르면 세 가지 방법을 구현해야 합니다.

- saveWorkout()--데이터베이스에 새 운동 기록을 추가합니다.
  
- findWorkouts()--사용자의 운동 기록을 검색합니다.

- deleteWorkout()--주어진 ID에 대한 운동 기록을 삭제합니다.

목록 18.3 WorkoutService 클래스 
```java
@Service
public class WorkoutService {

  @Autowired
  private WorkoutRepository workoutRepository;

  @PreAuthorize("#workout.user == authentication.name") ❶
  public void saveWorkout(Workout workout) {
    workoutRepository.save(workout);
  }

  public List<Workout> findWorkouts() { ❷
    return workoutRepository.findAllByUser();
  }

  public void deleteWorkout(Integer id) { ❸
    workoutRepository.deleteById(id);
  }
}
```
❶ 사전 승인을 통해 사용자의 운동 기록이 아닌 경우 해당 메소드가 호출되지 않도록 합니다.

❷ 이 방법은 이미 리포지토리 레이어에서 필터링을 적용했습니다.

❸ 끝점 계층에서 이 방법에 대한 권한 부여를 적용합니다.

> **참고** 다른 방식이 아니라 예시에서 보는 것과 같이 인증 규칙을 정확히 구현하기로 선택한 이유가 궁금할 것입니다. deleteWorkout() 메서드의 경우 서비스 계층이 아닌 엔드포인트 수준에서 권한 부여 규칙을 작성한 이유는 무엇입니까? 이 사용 사례의 경우 인증을 구성하는 더 많은 방법을 다루기 위해 선택했습니다. 서비스 계층에서 운동 삭제에 대한 권한 부여 규칙을 설정했다면 이전 예와 동일할 것입니다. 그리고 실제 앱과 같이 더 복잡한 응용 프로그램에서는 특정 계층을 선택해야 하는 제한 사항이 있을 수 있습니다.

컨트롤러 클래스는 서비스 메서드를 추가로 호출하는 끝점만 정의합니다. 다음 목록은 컨트롤러 클래스의 구현을 나타냅니다.

목록 18.4 WorkoutController 클래스
```java
@RestController
@RequestMapping("/workout")
public class WorkoutController {

  @Autowired
  private WorkoutService workoutService;

  @PostMapping("/")
  public void add(@RequestBody Workout workout) {
    workoutService.saveWorkout(workout);
  }

  @GetMapping("/")
  public List<Workout> findAll() {
    return workoutService.findWorkouts();
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Integer id) {
    workoutService.deleteWorkout(id);
  }
}
```
완전한 애플리케이션을 갖기 위해 정의해야 하는 마지막 것은 구성 클래스입니다. 리소스 서버가 권한 부여 서버에서 발행한 토큰의 유효성을 검사하는 방법을 선택해야 합니다. 14장과 15장에서 세 가지 접근 방식을 논의했습니다.

- 인증 서버에 직접 호출
- 칠판 접근 방식 사용
- 암호화 서명 사용

인증 서버가 JWT를 발행한다는 것을 이미 알고 있기 때문에 가장 편안한 선택은 토큰의 암호화 서명에 의존하는 것입니다. 15장에서 알 수 있듯이 서명을 확인하기 위해 리소스 서버에 키를 제공해야 합니다. 다행히 Keycloak은 공개 키가 노출되는 엔드포인트를 제공합니다.

http://localhost:8080/auth/realms/master/protocol/openid-connect/certs

application.properties 파일의 토큰에 설정한 aud 클레임 값과 함께 이 URI를 추가합니다.

```yml
server.port=9090
spring.datasource.url=jdbc:mysql://localhost/spring
spring.datasource.username=root
spring.datasource.password=
spring.datasource.initialization-mode=always
claim.aud=fitnessapp
jwkSetUri=http://localhost:8080/auth/realms/master/protocol/openid-connect/certs
```
이제 구성 파일을 작성할 수 있습니다. 이를 위해 다음 목록은 구성 클래스를 보여줍니다.

목록 18.5 리소스 서버 구성 클래스
```java
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity ❶
  (prePostEnabled = true)
public class ResourceServerConfig ❷
  extends ResourceServerConfigurerAdapter {

  @Value("${claim.aud}") ❸
  private String claimAud;

  @Value("${jwkSetUri}") ❸
  private String urlJwk;

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    resources.tokenStore(tokenStore()); ❹
    resources.resourceId(claimAud); ❹
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwkTokenStore(urlJwk); ❺
  }

}
```
❶ 전역 메서드 보안 사전/사후 주석 가능

❷ ResourceServerConfigurerAdapter를 확장하여 리소스 서버 구성을 사용자 정의

❸ 컨텍스트에서 키의 URI와 aud 클레임 값을 주입합니다.

❹ aud 클레임에 대해 예상되는 토큰 저장소 및 값을 설정합니다.

❺ 제공된 URI에서 찾은 키를 기반으로 토큰을 확인하는 TokenStore 빈 생성

TokenStore의 인스턴스를 생성하기 위해 JwkTokenStore라는 구현을 사용합니다. 이 구현은 여러 키를 노출할 수 있는 끝점을 사용합니다. 토큰의 유효성을 검사하기 위해 JwkTokenStore는 제공된 JWT 토큰의 헤더에 ID가 있어야 하는 특정 키를 찾습니다(그림 18.27).
 
그림 18.27 인증 서버는 개인 키를 사용하여 토큰에 서명합니다. 토큰에 서명할 때 인증 서버는 토큰 헤더에 키 쌍의 ID도 추가합니다. 토큰의 유효성을 검사하기 위해 리소스 서버는 권한 부여 서버의 끝점을 호출하고 토큰 헤더에 있는 ID에 대한 공개 키를 가져옵니다. 리소스 서버는 이 공개 키를 사용하여 토큰 서명의 유효성을 검사합니다.

> **참고** 이 장의 시작 부분에서 Keycloak이 키를 노출한 Keycloak에서 끝점까지 /openid-connect/certs 경로를 사용했음을 기억하십시오. 이 끝점에 대해 다른 경로를 사용하는 다른 도구를 찾을 수 있습니다.

키 URI를 호출하면 다음 코드 스니펫과 유사한 것을 볼 수 있습니다. HTTP 응답 본문에는 여러 키가 있습니다. 이 키 모음을 키 집합이라고 합니다. 각 키에는 키 값과 각 키의 고유 ID를 포함하여 여러 속성이 있습니다. 속성 kid는 JSON 응답의 키 ID를 나타냅니다.

```json
{
  "keys":[
    {
     "kid":"LHOsOEQJbnNbUn8PmZXA9TUoP56hYOtc3VOk0kUvj5U", ❶
     "kty":"RSA",
     "alg":"RS256",
     "use":"sig",
      ...
    }
  ...
  ]
}
```
❶ 키의 ID

JWT는 토큰 서명에 사용되는 키 ID를 지정해야 합니다. 리소스 서버는 JWT 헤더에서 키 ID를 찾아야 합니다. 섹션 18.2에서 했던 것처럼 리소스 서버로 토큰을 생성하고 토큰의 헤더를 디코딩하면 예상대로 토큰에 키 ID가 포함된 것을 볼 수 있습니다. 다음 코드 스니펫에서 Keycloak 인증 서버로 생성된 토큰의 디코딩된 헤더를 찾을 수 있습니다.

```json
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "LHOsOEQJbnNbUn8PmZXA9TUoP56hYOtc3VOk0kUvj5U"
}
```
구성 클래스를 완료하기 위해 끝점 수준 및 SecurityEvaluationContextExtension에 대한 권한 부여 규칙을 추가하겠습니다. 우리 애플리케이션은 리포지토리 계층에서 사용한 SpEL 표현식을 평가하기 위해 이 확장이 필요합니다. 최종 구성 클래스는 다음 목록과 같습니다.

목록 18.6 구성 클래스
```java
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig 
  extends ResourceServerConfigurerAdapter {

  @Value("${claim.aud}")
  private String claimAud;

  @Value("${jwkSetUri}")
  private String urlJwk;

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    resources.tokenStore(tokenStore());
    resources.resourceId(claimAud);
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwkTokenStore(urlJwk);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests() ❶
        .mvcMatchers(HttpMethod.DELETE, "/**")
            .hasAuthority("fitnessadmin")
        .anyRequest().authenticated();
  }

  @Bean ❷
  public SecurityEvaluationContextExtension 
    securityEvaluationContextExtension() {

    return new SecurityEvaluationContextExtension();
  }
}
```
❶ 엔드포인트 수준에서 권한 부여 규칙 적용

❷ Spring 컨텍스트에 SecurityEvaluationContextExtension 빈 추가

#### OAuth 2 웹 보안 표현식 사용

대부분의 경우 일반 표현식을 사용하여 권한 부여 규칙을 정의하는 것으로 충분합니다. Spring Security를 사용하면 권한, 역할 및 사용자 이름을 쉽게 참조할 수 있습니다. 그러나 OAuth 2 리소스 서버를 사용하면 클라이언트 역할이나 범위와 같이 이 프로토콜과 관련된 다른 값을 참조해야 하는 경우가 있습니다. JWT 토큰에 이러한 세부 정보가 포함되어 있지만 SpEL 표현식으로 직접 액세스할 수 없으며 정의한 권한 부여 규칙에서 빠르게 사용할 수 없습니다.

다행히 Spring Security는 OAuth 2와 직접 관련된 조건을 추가하여 SpEL 표현식을 향상시킬 수 있는 가능성을 제공합니다. 이러한 SpEL 표현식을 사용하려면 SecurityExpressionHandler를 구성해야 합니다. OAuth 2 특정 요소로 인증 표현을 향상시킬 수 있는 SecurityExpression-Handler 구현은 OAuth2WebSecurityExpressionHandler입니다. 이를 구성하기 위해 다음 코드 스니펫에 표시된 대로 구성 클래스를 변경합니다.

```java
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig 
  extends ResourceServerConfigurerAdapter {

  // Omitted code

  public void configure(ResourceServerSecurityConfigurer resources) {
    resources.tokenStore(tokenStore());
    resources.resourceId(claimAud);
    resources.expressionHandler(handler());
  }

  @Bean
  public SecurityExpressionHandler<FilterInvocation> handler() {
    return new OAuth2WebSecurityExpressionHandler();
  }
}
```
With such an expression handler, you can write an expression like this:

```java
@PreAuthorize("#workout.user == authentication.name and
   #oauth2.hasScope('fitnessapp')")
public void saveWorkout(Workout workout) {
  workoutRepository.save(workout);
}
```
클라이언트 범위 #oauth2.hasScope('fitnessapp')를 확인하는 @PreAuthorize 주석에 추가한 조건을 관찰하십시오. 이제 구성에 추가한 OAuth2WebSecurityExpressionHandler에서 평가할 이러한 표현식을 추가할 수 있습니다. 또한 hasScope() 대신 표현식에서 clientHasRole() 메서드를 사용하여 클라이언트에 특정 역할이 있는지 테스트할 수 있습니다. 클라이언트 자격 증명 부여 유형과 함께 클라이언트 역할을 사용할 수 있습니다. 이 예제를 현재 실습 프로젝트와 혼합하지 않기 위해 ssia-ch18-ex2라는 프로젝트로 분리했습니다.

## 18.4 애플리케이션 테스트

이제 완전한 시스템이 있으므로 몇 가지 테스트를 실행하여 원하는 대로 작동하는지 확인할 수 있습니다(그림 18.28). 이 섹션에서는 인증 서버와 리소스 서버를 모두 실행하고 cURL을 사용하여 구현된 동작을 테스트합니다.
 
그림 18.28 정상에 올랐습니다! 이것은 이 장의 실습 응용 프로그램을 구현하는 마지막 단계입니다. 이제 시스템을 테스트하고 우리가 구성하고 구현한 것이 예상대로 작동하는지 증명할 수 있습니다.

테스트해야 하는 시나리오는 다음과 같습니다.

- 클라이언트는 인증된 사용자에 대해서만 운동을 추가할 수 있습니다.

- 클라이언트는 자신의 운동 기록만 검색할 수 있습니다.

- 관리자만 운동을 삭제할 수 있습니다.

필자의 경우 Keycloak 권한 부여 서버는 포트 8080에서 실행되고 application.properties 파일에서 구성한 리소스 서버는 포트 9090에서 실행됩니다. 구성한 포트를 사용하여 올바른 구성 요소를 호출하는지 확인해야 합니다. 세 가지 테스트 시나리오를 각각 수행하여 시스템이 올바르게 보호되는지 증명해 보겠습니다.

### 18.4.1 인증된 사용자가 자신에 대한 기록만 추가할 수 있음을 증명

시나리오에 따르면 사용자는 자신에 대한 레코드만 추가할 수 있습니다. 즉, 내가 Bill로 인증하면 Rachel에 대한 운동 기록을 추가할 수 없습니다. 이것이 앱의 동작임을 증명하기 위해 인증 서버를 호출하고 사용자 중 한 명(예: Bill)에 대한 토큰을 발행합니다. 그런 다음 Bill의 운동 기록과 Rachel의 운동 기록을 모두 추가하려고 합니다. Bill이 자신에 대한 레코드를 추가할 수 있음을 증명하지만 앱에서는 그가 Rachel에 대한 레코드를 추가하는 것을 허용하지 않습니다. 토큰을 발행하기 위해 다음 코드에 표시된 대로 인증 서버를 호출합니다.

```sh
curl -XPOST 'http://localhost:8080/auth/realms/master/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username=bill' \
--data-urlencode 'password=12345' \
--data-urlencode 'scope=fitnessapp' \
--data-urlencode 'client_id=fitnessapp'
```
무엇보다도 Bill에 대한 액세스 토큰도 받습니다. 다음 코드에서 토큰 값을 잘라서 더 짧게 만들었습니다. 액세스 토큰에는 이전에 섹션 18.1에서 Keycloak을 구성하여 추가한 사용자 이름 및 권한과 같이 승인에 필요한 모든 세부 정보가 포함되어 있습니다.

```json
{
    "access_token": "eyJhbGciOiJSUzI1NiIsInR...",
    "expires_in": 6000,
    "refresh_expires_in": 1800,
    "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
    "token_type": "bearer",
    "not-before-policy": 0,
    "session_state": "0630a3e4-c4fb-499c-946b-294176de57c5",
    "scope": "fitnessapp"
}
```
액세스 토큰이 있으면 엔드포인트를 호출하여 새 운동 기록을 추가할 수 있습니다. 먼저 Bill의 운동 기록을 추가하려고 합니다. Bill에 대한 액세스 토큰이 생성되었기 때문에 Bill에 대한 운동 기록을 추가하는 것이 유효할 것으로 예상합니다.

다음 코드는 Bill을 위한 새 운동을 추가하기 위해 실행하는 cURL 명령을 보여줍니다. 이 명령을 실행하면 HTTP 응답 상태가 200 OK이고 새 운동 기록이 데이터베이스에 추가됩니다. 물론 Authorization 헤더의 값으로 이전에 생성한 액세스 토큰을 추가해야 합니다. 명령을 더 짧고 읽기 쉽게 만들기 위해 다음 코드 스니펫에서 토큰 값을 자릅니다.

```sh
curl -v -XPOST 'localhost:9090/workout/' \
-H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOi...' \
-H 'Content-Type: application/json' \
--data-raw '{
        "user" : "bill",
        "start" : "2020-06-10T15:05:05",
        "end" : "2020-06-10T16:05:05",
        "difficulty" : 2
}'
```
엔드포인트를 호출하고 Rachel에 대한 레코드를 추가하려고 하면 HTTP 응답 상태 403 Forbidden이 반환됩니다.

```sh
curl -v -XPOST 'localhost:9090/workout/' \
-H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOi...' \
-H 'Content-Type: application/json' \
--data-raw '{
        "user" : "rachel",
        "start" : "2020-06-10T15:05:05",
        "end" : "2020-06-10T16:05:05",
        "difficulty" : 2
}'
```

응답 본문은

```json
{
    "error": "access_denied",
    "error_description": "Access is denied"
}
```
### 18.4.2 사용자가 자신의 기록만 검색할 수 있음을 증명

두 번째 테스트 시나리오를 증명합니다. 리소스 서버는 인증된 사용자의 운동 기록만 반환합니다. 이 동작을 보여주기 위해 Bill과 Rachel 모두에 대한 액세스 토큰을 생성하고 엔드포인트를 호출하여 운동 기록을 검색합니다. 어느 쪽도 다른 쪽의 레코드를 볼 수 없습니다. Bill에 대한 액세스 토큰을 생성하려면 다음 curl 명령을 사용하십시오.

```sh
curl -XPOST 'http://localhost:8080/auth/realms/master/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username=bill' \
--data-urlencode 'password=12345' \
--data-urlencode 'scope=fitnessapp' \
--data-urlencode 'client_id=fitnessapp'
```
Bill에 대해 생성된 액세스 토큰으로 운동 기록을 검색하기 위해 엔드포인트를 호출하면 애플리케이션은 Bill의 기록만 반환합니다.

```sh
curl 'localhost:9090/workout/' \
-H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSl...'
```
응답 본문은
```json
[
    {
        "id": 1,
        "user": "bill",
        "start": "2020-06-10T15:05:05",
        "end": "2020-06-10T16:10:07",
        "difficulty": 3
    },
    . . .
]
```
다음으로 Rachel에 대한 토큰을 생성하고 동일한 엔드포인트를 호출합니다. Rachel에 대한 액세스 토큰을 생성하려면 다음 curl 명령을 실행하십시오.
```
curl -XPOST 'http://localhost:8080/auth/realms/master/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username=rachel' \
--data-urlencode 'password=12345' \
--data-urlencode 'scope=fitnessapp' \
--data-urlencode 'client_id=fitnessapp'
```

Rachel의 액세스 토큰을 사용하여 운동 기록을 가져오면 애플리케이션은 Rachel이 소유한 레코드만 반환합니다.

```sh
curl 'localhost:9090/workout/' \
-H 'Authorization: Bearer eyJhaXciOiJSUzI1NiIsInR5cCIgOiAiSl...'
```
응답은
```json
[
    {
        "id": 2,
        "user": "rachel",
        "start": "2020-06-10T15:05:10",
        "end": "2020-06-10T16:10:20",
        "difficulty": 3
    },
    ...
]
```

### 18.4.3 관리자만 기록을 삭제할 수 있음을 증명

애플리케이션이 원하는 대로 작동하는지 증명하려는 세 번째이자 마지막 테스트 시나리오는 관리자만 운동 기록을 삭제할 수 있다는 것입니다. 이 동작을 시연하기 위해 관리자인 Mary에 대한 액세스 토큰과 관리자가 아닌 다른 사용자(예: Rachel)에 대한 액세스 토큰을 생성합니다. Mary에 대해 생성된 액세스 토큰을 사용하여 운동을 삭제할 수 있습니다. 그러나 애플리케이션은 Rachel에 대해 생성된 액세스 토큰을 사용하여 운동 기록을 삭제하기 위해 엔드포인트를 호출하는 것을 금지합니다. Rachel에 대한 토큰을 생성하려면 다음 curl 명령을 사용하십시오.
```sh
curl -XPOST 'http://localhost:8080/auth/realms/master/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username=rachel' \
--data-urlencode 'password=12345' \
--data-urlencode 'scope=fitnessapp' \
--data-urlencode 'client_id=fitnessapp'
```
Rachel의 토큰을 사용하여 기존 운동을 삭제하면 403 Forbidden HTTP 응답 상태로 돌아갑니다. 물론 레코드는 데이터베이스에서 삭제되지 않습니다. 호출 내용은 다음과 같습니다.
```sh
curl -XDELETE 'localhost:9090/workout/2' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsIn...'
```
Mary에 대한 토큰을 생성하고 새 액세스 토큰을 사용하여 엔드포인트에 대한 동일한 호출을 다시 실행합니다. Mary에 대한 토큰을 생성하려면 다음 curl 명령을 사용하십시오.
```sh
curl -XPOST 'http://localhost:8080/auth/realms/master/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'username=mary' \
--data-urlencode 'password=12345' \
--data-urlencode 'scope=fitnessapp' \
--data-urlencode 'client_id=fitnessapp'
```
Mary에 대한 액세스 토큰으로 workout 기록을 삭제하기 위해 엔드포인트를 호출하면 HTTP 상태 200 OK가 반환됩니다. workout 기록이 데이터베이스에서 제거됩니다. 호출 내용은 다음과 같습니다.

```sh
curl -XDELETE 'localhost:9090/workout/2' \
--header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsIn...'
```

## 요약

- 사용자 정의 인증 서버를 반드시 구현할 필요는 없습니다. 종종 실제 시나리오에서 Keycloak과 같은 도구를 사용하여 인증 서버를 구현합니다.

- Keycloak은 사용자 관리 및 권한 부여를 처리할 때 뛰어난 유연성을 제공하는 오픈 소스 ID 및 액세스 관리 솔루션입니다. 종종 사용자 지정 솔루션을 구현하는 것보다 이러한 도구를 사용하는 것을 선호할 수 있습니다.

- Keycloak과 같은 솔루션이 있다고 해서 권한 부여를 위한 맞춤형 솔루션을 구현하지 않는다는 의미는 아닙니다. 실제 시나리오에서는 빌드해야 하는 애플리케이션의 이해 관계자가 타사 구현을 신뢰할 수 있다고 생각하지 않는 상황을 찾을 수 있습니다. 발생할 수 있는 모든 경우에 대비할 수 있도록 준비해야 합니다.

- OAuth 2 프레임워크를 통해 구현된 시스템에서 전역 메서드 보안을 사용할 수 있습니다. 이러한 시스템에서는 리소스 서버 수준에서 전역 메서드 보안 제한을 구현하여 사용자 리소스를 보호합니다.

- 권한 부여를 위해 SpEL 표현식에서 특정 OAuth 2 요소를 사용할 수 있습니다. 이러한 SpEL 표현식을 작성하려면 표현식을 해석하도록 OAuth2WebSecurityExpressionHandler를 구성해야 합니다.
