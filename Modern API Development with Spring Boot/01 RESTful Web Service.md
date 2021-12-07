# Chapter 1: RESTful Web Service Fundamentals

In this chapter, you will go through the fundamentals of RESTful APIs, or REST APIs for short, and their design paradigms. We will take a brief look at the history of REST, learn how resources are formed, and understand methods and status codes before we move on to explore Hypermedia As The Engine Of Application State (HATEOAS). These basics should provide a solid platform for you to develop a RESTful web service. You will also learn the best practices for designing Application Programming Interfaces (APIs).

This chapter will also introduce a sample e-commerce app, which will be used throughout the book as you learn about the different aspects of API development. In this chapter, we will cover the following topics:

- Introducing REST APIs
- Handling resources and URIs
- Exploring HTTP methods and status codes
- Learning HATEOAS
- Best practices for designing REST APIs
- Overview of an e-commerce app (our sample app)

## Technical requirements

- knowledge of HTTP

## REST API 소개

API는 코드 조각이 다른 코드 조각과 통신하는 수단입니다. 이미 코드용 API를 작성했거나 프로그램에서 사용했을 수 있습니다. 예를 들어, 특정 작업을 수행하기 위한 다양한 API를 제공하는 수집, 입력/출력 또는 스트림을 위한 Java 라이브러리에서.

Java의 SDK API를 사용하면 프로그램의 한 부분이 프로그램의 다른 부분과 통신할 수 있습니다. 함수를 작성한 다음 다른 클래스에서 사용할 수 있도록 공용 액세스 한정자로 이를 노출할 수 있습니다. 해당 함수 서명은 해당 클래스에 대한 API입니다. 그러나 이러한 클래스나 라이브러리를 사용하여 노출되는 API는 단일 애플리케이션 또는 개별 서비스 내부에서만 내부 통신을 허용합니다. 그렇다면 둘 이상의 애플리케이션(또는 서비스)이 서로 통신하기를 원하면 어떻게 될까요? 즉, 둘 이상의 서비스를 통합하려고 합니다. 여기에서 시스템 전반의 API가 도움이 됩니다.

역사적으로 RPC, SOAP(Simple Object Access Protocol) 기반 서비스 등 한 응용 프로그램을 다른 응용 프로그램과 통합하는 다양한 방법이 있었습니다. 앱 통합은 특히 클라우드와 휴대폰의 붐 이후 소프트웨어 아키텍처의 필수적인 부분이 되었습니다. 이제 Facebook, Google 및 GitHub와 같은 소셜 로그인이 있습니다. 즉, 독립적인 로그인 모듈을 작성하지 않고도 애플리케이션을 개발할 수 있고 안전한 방법으로 비밀번호를 저장하는 것과 같은 보안 문제를 해결할 수 있습니다.

이러한 소셜 로그인은 REST 및 GraphQL을 사용하는 API를 제공합니다. 현재 REST가 가장 널리 사용되고 있으며, 통합 및 웹앱 소비를 위한 API 작성의 표준이 되었습니다. 또한 이 책의 마지막 장(13장, GraphQL 기초 및 14장, GraphQL 개발 및 테스트)에서 GraphQL에 대해 자세히 설명합니다.

REST는 소프트웨어 아키텍처 스타일인 REpresentational State Transfer의 약자입니다. REST 스타일을 따르는 웹 서비스를 RESTful 웹 서비스라고 합니다. 다음 섹션에서는 REST의 기초를 이해하기 위해 REST의 역사를 간단히 살펴보겠습니다.

### REST 기록

REST가 채택되기 전에는 인터넷이 막 널리 알려지기 시작했고 Yahoo와 Hotmail이 인기 있는 메일 및 소셜 메시징 앱이었을 때 웹 애플리케이션과 통합하는 동종 방식을 제공하는 표준 소프트웨어 아키텍처가 없었습니다. 사람들은 아이러니하게도 전혀 단순하지 않은 SOAP 기반 웹 서비스를 사용하고 있었습니다.

그때 빛이 왔다. Roy Fielding은 박사 학위 연구인 Architectural Styles and Design of Network-Based Software Architectures(https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm)에서 2000년에 REST를 제시했습니다. REST의 아키텍처 스타일은 모든 서버가 네트워크를 통해 다른 서버와 통신할 수 있도록 했습니다. 커뮤니케이션을 단순화하고 통합을 더 쉽게 만들었습니다. REST는 HTTP 위에서 작동하도록 만들어졌으며 웹 전체와 내부 네트워크에서 사용할 수 있습니다.

eBay는 REST 기반 API를 최초로 활용했습니다. 2000년 11월에 선택된 파트너와 함께 REST API를 도입했습니다. 이후 Amazon, Delicious(사이트 북마크 웹 앱) 및 Flickr(사진 공유 앱)에서 REST 기반 API를 제공하기 시작했습니다. 실제로 Amazon Web Services(AWS)는 Web 2.0(REST의 발명과 함께)을 활용하고 2006년 AWS 클라우드 사용을 위해 개발자에게 REST API를 제공했습니다.

나중에 Facebook, Twitter, Google 및 기타 회사에서 사용하기 시작했습니다. 요즘(2021년)에는 REST API 없이 개발된 웹 애플리케이션을 거의 찾을 수 없습니다. 그러나 모바일 앱용 GraphQL 기반 API는 인기 면에서 꽤 가까워지고 있습니다.

### REST 기초

REST는 HTTP 프로토콜 위에서 작동합니다. 각 URI는 API 리소스로 작동합니다. 따라서 동사 대신 명사를 끝점으로 사용해야 합니다. RPC 스타일 끝점은 동사를 사용합니다(예: api/v1/getPersons). 이에 비해 REST에서는 이 끝점을 api/v1/persons로 간단하게 작성할 수 있습니다. 그러면 REST 리소스에서 수행되는 다양한 작업을 어떻게 구별할 수 있는지 궁금하실 것입니다. 이것이 HTTP 메소드가 우리를 도와주는 곳입니다. 예를 들어 GET, DELETE, POST(생성용), PUT(수정용) 및 PATCH(부분 업데이트용)와 같이 HTTP 메서드가 동사로 작동하도록 할 수 있습니다. 이에 대해서는 나중에 더 자세히 다루겠습니다. 지금은 getPerson RPC 스타일 엔드포인트가 REST에서 GET api/v1/persons로 변환됩니다.

*노트*

REST 끝점은 REST 리소스를 나타내는 고유한 URI입니다. 예를 들어 https://demo.app/api/v1/persons는 REST 엔드포인트입니다. 또한 /api/v1/persons는 끝점 경로이고 people은 REST 리소스입니다.

여기에 클라이언트와 서버 통신이 있습니다. 따라서 REST는 클라이언트-서버 개념을 기반으로 합니다. 클라이언트는 REST API를 호출하고 서버는 응답으로 응답합니다. REST를 사용하면 클라이언트(즉, 프로그램, 웹 서비스 또는 UI 앱)가 HTTP 요청 및 응답을 사용하여 원격(또는 로컬로) 실행 중인 서버(또는 웹 서비스)와 통신할 수 있습니다. 클라이언트는 웹에 대한 HTTP 요청에 래핑된 API 명령을 사용하여 웹 서비스에 보냅니다. 이 HTTP 요청에는 쿼리 매개변수, 헤더 또는 요청 본문 형식의 페이로드(또는 입력)가 포함될 수 있습니다. 호출된 웹 서비스는 성공/실패 표시기와 HTTP 응답 내부에 래핑된 응답 데이터로 응답합니다. HTTP 상태 코드는 일반적으로 상태를 나타내며 응답 본문에는 응답 데이터가 포함됩니다. 예를 들어 HTTP 상태 코드 200 OK는 일반적으로 성공을 나타냅니다.

REST 관점에서 HTTP 요청은 자체 설명적이며 서버에서 처리할 수 있는 충분한 컨텍스트가 있습니다. 따라서 REST 호출은 상태 비저장입니다. 상태는 클라이언트 측이나 서버 측에서 관리됩니다. REST API는 상태를 유지하지 않습니다. 서버에서 클라이언트로 또는 그 반대로 상태만 전송합니다. 따라서 이를 REpresentation State Transfer 또는 줄여서 REST라고 합니다.

또한 HTTP 캐시 제어를 사용하여 REST API를 캐시 가능하게 만듭니다. 따라서 클라이언트는 모든 표현이 자체 설명적이기 때문에 표현(즉, HTTP 응답)을 캐시할 수도 있습니다.

다음은 REST의 주요 개념 목록입니다.

- Resources and URIs
- HTTP methods

A sample REST call in plain text looks similar to the following:
```
GET /licenses HTTP/1.1
Host: api.github.com
```
여기서 /licenses 경로는 라이선스 리소스를 나타냅니다. GET은 HTTP 메서드입니다. 첫 번째 줄 끝에 있는 1.1은 HTTP 프로토콜 버전을 나타냅니다. 두 번째 회선은 호출할 호스트를 공유합니다.

GitHub는 JSON 객체로 응답합니다. 상태는 200 OK이고 JSON 객체는 다음과 같이 응답 본문에 래핑됩니다.

```
HTTP/1.1 200 OK
date: Sun, 22 Sep 2020 18:01:22 GMT
content-type: application/json; charset=utf-8
server: GitHub.com
status: 200 OK
cache-control: public, max-age=60, s-maxage=60
vary: Accept, Accept-Encoding, Accept, X-Requested-With, Accept-Encoding
etag: W/"3cbb5a2e38ac6fc92b3d798667e828c7e3584af278aa314f6eb1857bbf2593ba"

… <bunch of other headers>
Accept-Ranges: bytes
Content-Length: 2507
X-GitHub-Request-Id: 1C03:5C22:640347:81F9C5:5F70D372
[
    {
         "key": "agpl-3.0",
         "name": "GNU Affero General Public License v3.0",
         "spdx_id": "AGPL-3.0",
        "url": "https://api.github.com/licenses/agpl-3.0",
        "node_id": "MDc6TGljZW5zZTE="
    },
    {
        "key": "apache-2.0",
        "name": "Apache License 2.0",
        "spdx_id": "Apache-2.0",
        "url": "https://api.github.com/licenses/apache-2.0",
        "node_id": "MDc6TGljZW5zZTI="
    },
    …
]
```
이 응답의 세 번째 줄을 메모하면 콘텐츠 유형의 값을 알려줍니다. 요청과 응답 모두에 대한 콘텐츠 유형으로 JSON을 사용하는 것이 좋습니다.


## 리소스 및 URI 처리

WWW의 모든 문서는 HTTP 측면에서 리소스로 표시됩니다. 이 리소스는 서버의 고유 리소스를 나타내는 끝점인 URI로 표시됩니다.

Roy Fielding은 URI가 WWW 주소, UDI(Universal Document Identifier), URI, URL(Uniform Resource Locator) 및 URN(Uniform Resource Name)과 같은 많은 이름으로 알려져 있다고 말합니다.

그렇다면 URI란 무엇인가? URI는 WWW 세계에서 위치, 이름 또는 둘 다로 리소스를 식별하는 문자열(즉, 일련의 문자)입니다. URI에는 다음과 같이 URL과 URN의 두 가지 유형이 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_1.1_B16561.jpg)

그림 1.1 – URI 계층 구조

URL은 널리 사용되며 개발자가 아닌 사용자에게도 알려져 있습니다. URL은 HTTP에만 국한되지 않습니다. 실제로 FTP, JDBC 및 MAILTO와 같은 다른 많은 프로토콜에도 사용됩니다. 따라서 URL은 리소스의 네트워크 위치를 식별하는 식별자입니다. 이후 섹션에서 더 자세히 다루겠습니다.

### The URI syntax

The URI syntax is as follows:
```
scheme:[//authority]path[?query][#fragment]
```
구문에 따라 다음은 URI의 구성 요소 목록입니다.

- Scheme: 콜론(:)이 뒤에 오는 비어 있지 않은 문자 시퀀스를 나타냅니다. 체계는 문자로 시작하고 숫자, 문자, 마침표(.), 하이픈(-) 또는 더하기 문자(+)의 조합이 옵니다.
예에는 HTTP, HTTPS, MAILTO, FILE, FTP 등이 포함됩니다. URI 체계는 IANA(Internet Assigned Numbers Authority)에 등록해야 합니다.

- Authority: 선택적 필드이며 //가 앞에 옵니다. 다음과 같은 선택적 하위 필드로 구성됩니다.
a. UserInfo: 이것은 선택 사항인 사용자 이름과 암호를 포함할 수 있는 하위 구성 요소입니다.

b. Host: IP 주소 또는 등록된 호스트 또는 도메인 이름을 포함하는 하위 구성 요소입니다.

c. Port: 콜론(:)이 뒤에 오는 선택적 하위 구성요소입니다.

- Path: 경로에는 슬래시 문자(/)로 구분된 일련의 세그먼트가 포함됩니다. 앞의 GitHub REST API 예제에서 /licenses는 경로입니다.

- Query: 선택적 구성 요소이며 앞에 물음표(?)가 표시됩니다. 쿼리 구성 요소에는 비계층적 데이터의 쿼리 문자열이 포함되어 있습니다. 쿼리 구성 요소에서 각 매개 변수는 앰퍼샌드(&)로 구분되며 매개 변수 값은 등호(=) 연산자를 사용하여 할당됩니다.

- Fragment: 선택적 필드이며 해시(#)가 앞에 옵니다. 프래그먼트 구성 요소에는 2차 리소스에 대한 방향을 제공하는 프래그먼트 식별자가 포함됩니다.
  
The following list contains examples of URIs:

- www.packt.com: This doesn't contain the scheme. It just contains the domain name. There is no port either, which means it points to the default port.

- index.html: This contains no scheme nor authority. It only contains the path.
https://www.packt.com/index.html: This contains the scheme, authority, and path.

Here are some examples of different scheme URIs:

- mailto:support@packt.com
- telnet://192.168.0.1:23/
- ldap://[2020:ab9::9]/c=AB?objectClass?obj

From a REST perspective, the path component of a URI is very important because it represents the resource path and your API endpoint paths are formed based on it. For example, take a look at the following:
```
GET https://www.domain.com/api/v1/order/1
```
Here, /api/v1/order/1 represents the path, and GET represents the HTTP method.

### URLs
자세히 보면 앞에서 언급한 대부분의 URI 예제를 URL이라고도 할 수 있습니다. URI는 식별자입니다. 반면에 URL은 식별자일 뿐만 아니라 URL에 도달하는 방법도 알려줍니다.

URI에 대한 RFC(Request for Comments)-3986(https://xml2rfc.tools.ietf.org/public/rfc/html/rfc3986.html)에 따라 URL이라는 용어는 리소스를 식별하고 기본 액세스 메커니즘(예: 네트워크 "위치")을 설명하여 리소스를 찾는 수단을 제공합니다.

URL은 프로토콜 이름(체계), 호스트 이름 포트(HTTP 포트가 80이 아닌 경우, HTTPS의 경우 기본 포트는 443)를 포함하는 리소스의 전체 웹 주소, 권한 구성 요소의 일부, 경로, 선택적 쿼리 및 조각 하위 구성 요소.

### URN
URN은 일반적으로 사용되지 않습니다. 또한 urn이라는 체계로 시작하는 URI 유형입니다. 다음 URN 예제는 URI에 대한 RFC-3986(https://xml2rfc.tools.ietf.org/public/rfc/html/rfc3986.html)에서 직접 가져온 것입니다.

항아리:오아시스:이름:사양:docbook:dtd:xml:4.1.2

이 예는 "urn:" <NID> ":" <NSS> 구문을 따릅니다. 여기서 <NID>는 NAMESPACE IDENTIFIER이고 <NSS>는 네임스페이스별 문자열입니다. 우리는 REST 구현에서 URN을 사용하지 않을 것입니다. 그러나 RFC-2141(https://tools.ietf.org/html/rfc2141)에서 이에 대한 자세한 내용을 읽을 수 있습니다.

URI에 대한 RFC-3986에 따라(https://xml2rfc.tools.ietf.org/public/rfc/html/rfc3986.html): URN이라는 용어는 역사적으로 "urn" 체계 RFC에서 두 URI를 모두 나타내는 데 사용되었습니다. -2141, 리소스가 더 이상 존재하지 않거나 사용할 수 없게 되는 경우에도 전역적으로 고유하고 지속적으로 유지되어야 하며 이름의 속성을 가진 다른 URI에 대해 요구됩니다.



## HTTP 메서드 및 상태 코드 탐색

HTTP는 다양한 HTTP 메소드를 제공합니다. 그러나 주로 5개만 사용할 것입니다. 시작하려면 HTTP 메서드와 연결된 CRUD(만들기, 읽기, 업데이트 및 삭제) 작업이 필요합니다.

- POST: Create or search
- GET: Read
- PUT: Update
- DELETE: Delete
- PATCH: Partial update

일부 조직은 REST 끝점에서 헤더 응답을 검색하려는 시나리오에 대해 HEAD 메서드도 제공합니다. HEAD 작업으로 GitHub API를 실행하여 헤더만 검색할 수 있습니다. 예를 들어 curl --head https://api.github.com/users입니다.

*노트*

REST에는 어떤 작업에 어떤 방법을 사용해야 하는지 지정하는 요구 사항이 없습니다. 그러나 널리 사용되는 업계 지침 및 관행에서는 특정 규칙을 따르는 것이 좋습니다.

다음 섹션에서 각 방법에 대해 자세히 살펴보겠습니다.

### post
HTTP POST 메서드는 일반적으로 리소스 작업 생성과 연결하려는 것입니다. 그러나 읽기 작업에 POST 메서드를 사용하려는 경우 특정 예외가 있습니다. 그러나 충분히 숙고한 과정을 거쳐 실행에 옮겨야 합니다. 이러한 예외 중 하나는 필터 기준에 GET 호출의 길이 제한을 초과할 수 있는 매개변수가 너무 많은 검색 작업입니다.

GET 쿼리 문자열은 256자로 제한됩니다. 또한 GET HTTP 메서드는 최대 2,048자에서 실제 경로의 문자 수를 뺀 것으로 제한됩니다. 반면에 POST 방식은 이름과 값 쌍을 제출하기 위한 URL의 크기에 제한을 받지 않습니다.

제출된 입력 매개변수에 개인 정보나 보안 정보가 포함된 경우 읽기 호출에 HTTPS와 함께 POST 메서드를 사용할 수도 있습니다.

성공적인 생성 작업의 경우 201 Created 상태로 응답할 수 있으며 성공적인 검색 또는 읽기 작업의 경우 POST HTTP 메서드를 사용하여 호출이 이루어지더라도 200 OK 또는 204 No Content 상태 코드를 사용해야 합니다.

실패한 작업의 경우 REST 응답은 오류 유형에 따라 다른 오류 상태 코드를 가질 수 있습니다. 이에 대해서는 이 섹션의 뒷부분에서 살펴보겠습니다.

### GET
HTTP GET 메서드는 일반적으로 리소스 읽기 작업과 연결하려는 것입니다. 마찬가지로 GitHub 시스템에서 사용 가능한 라이선스를 반환하는 GitHub GET /licenses 호출을 관찰해야 합니다. 또한 성공적인 GET 작업은 응답에 데이터가 포함된 경우 200 OK 상태 코드와 연결되어야 하고 응답에 데이터가 포함되지 않은 경우 204 No Content와 연결되어야 합니다.

### put
HTTP PUT 메서드는 일반적으로 리소스 업데이트 작업과 연결하려는 것입니다. 또한 성공적인 업데이트 작업은 응답에 데이터가 포함된 경우 200 OK 상태 코드와 연결되어야 하고 응답에 데이터가 포함되지 않은 경우 204 No Content와 연결되어야 합니다. 일부 개발자는 PUT HTTP 메서드를 사용하여 기존 리소스를 대체합니다. 예를 들어 GitHub API v3은 PUT을 사용하여 기존 리소스를 대체합니다.

### delete
HTTP DELETE 메서드는 리소스 삭제 작업과 연결하려는 것입니다. GitHub는 라이선스 리소스에 대한 DELETE 작업을 제공하지 않습니다. 그러나 존재한다고 가정하면 DELETE /licenses/agpl-3.0과 매우 유사하게 보일 것입니다. 성공적인 삭제 호출은 agpl-3.0 키와 연결된 리소스를 삭제해야 합니다. 또한 성공적인 DELETE 작업은 204 No Content 상태 코드와 연결되어야 합니다.

### patch
HTTP PATCH 메서드는 부분 업데이트 리소스 작업과 연결하려는 것입니다. 또한 성공적인 PATCH 작업은 200 OK 상태 코드와 연결되어야 합니다. PATCH는 다른 HTTP 작업에 비해 비교적 새롭습니다. 사실, 몇 년 전 Spring은 오래된 Java HTTP 라이브러리로 인해 REST 구현을 위한 이 방법에 대한 최신 지원을 제공하지 않았습니다. 그러나 현재 Spring은 REST 구현에서 PATCH 메소드에 대한 내장 지원을 제공합니다.

### HTTP status codes

There are five categories of HTTP status codes, as follows:

- 정보 응답(100–199)
- 성공 응답(200–299)
- Redirects (300–399)
- 클라이언트 에러(400–499)
- 서버 에러(500–599)

You can view a complete list of status codes at MDN Web Docs (https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) or RFC-7231 (https://tools.ietf.org/html/rfc7231). However, you can find the most commonly used REST response status codes in the following table:


## Learning HATEOAS

HATEOAS를 통해 RESTful 웹 서비스는 하이퍼미디어를 통해 동적으로 정보를 제공합니다. Hypermedia는 REST 호출 응답에서 수신하는 콘텐츠의 일부입니다. 이 하이퍼미디어 콘텐츠에는 텍스트, 이미지 및 비디오와 같은 다양한 유형의 미디어에 대한 링크가 포함되어 있습니다.

하이퍼미디어 링크는 HTTP 헤더나 응답 본문에 포함될 수 있습니다. GitHub API를 살펴보면 GitHub API가 헤더와 응답 본문 모두에서 하이퍼미디어 링크를 제공한다는 것을 알 수 있습니다. GitHub는 "Link"라는 헤더를 사용하여 페이징 관련 링크를 포함합니다. 또한 GitHub API의 응답을 보면 접미사가 "url"인 키가 있는 다른 리소스 관련 링크도 찾을 수 있습니다. 예를 들어 보겠습니다. GET /users 리소스를 누르고 응답을 분석합니다.

```sh
$ curl -v https://api.github.com/users
```

This will give you the following output:

```
HTTP/1.1 200 OK
date: Mon, 28 Sep 2020 05:49:56 GMT
content-type: application/json; charset=utf-8
server: GitHub.com
status: 200 OK
cache-control: public, max-age=60, s-maxage=60
vary: Accept, Accept-Encoding, Accept, X-Requested-With,
      Accept-Encoding
etag: W/"6308a6b7274db1f1ffa377aeeb5359a015f69fa6733298938
      9453c7f20336753"
x-github-media-type: github.v3; format=json
link: <https://api.github.com/users?since=46>; rel="next", <https://api.github.com/users{?since}>; rel="first"

… <Some other headers>
…
[
  {
    "login": "mojombo",
    "id": 1,
    "node_id": "MDQ6VXNlcjE=",
    "avatar_url": "https://avatars0.githubusercontent.com/u/1?v=4",
    "gravatar_id": "",
    "url": "https://api.github.com/users/mojombo",
    "html_url": "https://github.com/mojombo",
    "followers_url": "https://api.github.com/users/mojombo/followers",
    "following_url": "https://api.github.com/users/mojombo/ following{/other_user}",

    "gists_url": "https://api.github.com/users/mojombo/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/mojombo/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/mojombo/subscriptions",
    "organizations_url": "https://api.github.com/users/mojombo/orgs",
    "repos_url": "https://api.github.com/users/mojombo/repos",
    "events_url": "https://api.github.com/users/mojombo/events{/privacy}",
    "received_events_url": "https://api.github.com/users/mojombo/received_events",
    "type": "User",
    "site_admin": false
  },
  {
    "login": "defunkt",
    "id": 2,
    "node_id": "MDQ6VXNlcjI=",
…
… <some more data>
]
```

이 코드 블록에서 "Link" 헤더에 페이지 매김 정보가 포함되어 있음을 알 수 있습니다. "다음" 페이지 및 "첫 번째" 페이지에 대한 링크가 응답의 일부로 제공됩니다. 또한 다른 하이퍼미디어에 대한 링크를 제공하는 "avatar_url" 또는 "followers_url"과 같은 응답 본문에서 많은 URL을 찾을 수 있습니다.

REST 클라이언트는 하이퍼미디어에 대한 일반적인 이해가 있어야 합니다. 그런 다음 REST 클라이언트는 서버와 상호 작용하는 방법에 대한 특정 지식 없이도 RESTful 웹 서비스와 상호 작용할 수 있습니다. 정적 REST API 끝점을 호출하기만 하면 추가 상호 작용을 위한 응답의 일부로 동적 링크가 수신됩니다. REST를 사용하면 클라이언트가 링크를 탐색하여 적절한 리소스를 동적으로 탐색할 수 있습니다. REST 클라이언트는 사람이 웹 페이지를 보고 링크를 클릭하는 것과 유사한 방식으로 다른 리소스를 탐색할 수 있기 때문에 머신에 권한을 부여합니다. 간단히 말해서 REST 클라이언트는 이러한 링크를 사용하여 탐색합니다.

HATEOAS는 REST의 매우 중요한 개념입니다. REST와 RPC를 구분하는 개념 중 하나입니다. Roy Fielding조차도 특정 REST API 구현에 매우 관심을 갖고 2008년에 자신의 웹사이트에 다음 블로그를 게시했습니다. REST API는 하이퍼텍스트 기반이어야 합니다.

하이퍼텍스트와 하이퍼미디어의 차이점이 무엇인지 궁금할 것입니다. 본질적으로 하이퍼미디어는 하이퍼텍스트의 확장된 버전일 뿐입니다. 

Roy Fielding은 다음과 같이 말합니다.

"내가 하이퍼텍스트라고 할 때, 정보가 사용자(또는 자동 장치)가 선택을 얻고 행동을 선택하는 어포던스가 되도록 정보와 제어를 동시에 표시하는 것을 의미합니다. 하이퍼미디어는 텍스트가 시간적 앵커를 포함하는 것을 의미하는 확장일 뿐입니다. 미디어 스트림, 대부분의 연구자들은 구분을 포기했습니다.

하이퍼텍스트는 브라우저에서 HTML일 필요가 없습니다. 기계는 데이터 형식과 관계 유형을 이해하면 링크를 따라갈 수 있습니다."


## REST API 설계 모범 사례

API 구현을 위한 모범 사례에 대해 이야기하기에는 너무 이릅니다. API는 먼저 설계되고 나중에 구현됩니다. 따라서 다음 섹션에서 언급되는 디자인 관련 모범 사례를 찾을 수 있습니다. 또한 REST API 구현 과정에서 앞으로 나아가기 위한 모범 사례를 찾을 수 있습니다.

### 1. 엔드포인트 경로에서 리소스 이름을 지정할 때 동사가 아닌 **명사를 사용**하십시오.
우리는 이전에 HTTP 메소드에 대해 논의했습니다. HTTP 메서드는 동사를 사용합니다. 따라서 동사를 직접 사용하는 것은 불필요하며 호출이 GET /getlicenses와 같은 RPC 종점처럼 보이게 됩니다. REST에서는 REST에 따르면 지침이 아닌 상태를 전송하기 때문에 항상 리소스 이름을 사용해야 합니다.

예를 들어 사용 가능한 라이선스를 검색하는 GitHub 라이선스 API를 다시 살펴보겠습니다. GET/licenses입니다. 완벽 해. 이 끝점에 동사를 사용하는 경우 GET/getlicenses가 된다고 가정해 보겠습니다. 여전히 작동하지만 의미상 REST를 따르지 않습니다. 상태 전송이 아닌 처리 명령을 전달하기 때문입니다. 따라서 리소스 이름만 사용하십시오.

그러나 GitHub의 공개 API는 모든 CRUD 작업 중 라이선스 리소스에 대한 읽기 작업만 제공합니다. 나머지 작업을 설계해야 하는 경우 해당 경로는 다음과 같아야 합니다.

- POST /licenses: 새 라이선스를 생성하기 위한 것입니다.
- PATCH /licenses/{license_key}: 부분 업데이트용입니다. 여기서 경로에는 경로를 동적으로 만드는 매개변수(즉, 식별자)가 있습니다. 여기서 라이선스 키는 라이선스 컬렉션에서 고유한 값으로 식별자로 사용됩니다. 각 라이선스에는 고유한 키가 있습니다. 이 호출은 주어진 라이센스에서 업데이트를 수행해야 합니다. GitHub는 리소스 교체를 위해 PUT을 사용한다는 것을 기억하십시오.
- DELETE /licenses/{license_key}: 라이선스 정보를 조회하기 위한 것입니다. GET/licenses 호출에 대한 응답으로 받은 모든 라이선스로 이 작업을 시도할 수 있습니다. 한 가지 예는 GET /licenses/agpl-3.0입니다.

HTTP 메서드를 사용하여 리소스 경로에 명사가 있으면 모호성을 어떻게 분류하는지 알 수 있습니다.

### 2. 끝점 경로에서 컬렉션 리소스의 이름을 지정할 때 복수형을 사용합니다.
GitHub 라이선스 API를 살펴보면 리소스 이름이 복수형으로 제공되는 것을 확인할 수 있습니다. 리소스가 컬렉션을 나타내는 경우 복수형을 사용하는 것이 좋습니다. 따라서 /license 대신 /license를 사용할 수 있습니다. GET 호출은 라이선스 컬렉션을 반환합니다. 스타일 호출은 기존 라이선스 컬렉션에 새 라이선스를 생성합니다. 삭제 및 패치 호출의 경우 라이센스 키를 사용하여 특정 라이센스를 식별합니다.

### 3. 하이퍼미디어 사용(HATEOAS)
Hypermedia(즉, 다른 리소스에 대한 링크)는 REST 클라이언트의 작업을 더 쉽게 만듭니다. 응답에 명시적 URL 링크를 제공하면 두 가지 이점이 있습니다. 첫째, REST 클라이언트는 자체적으로 REST URL을 구성할 필요가 없습니다. 둘째, 엔드포인트 경로의 모든 업그레이드가 자동으로 처리되므로 클라이언트와 개발자가 업그레이드를 더 쉽게 수행할 수 있습니다.

### 4. 항상 API 버전 관리
API 버전 관리는 향후 업그레이드의 핵심입니다. 시간이 지남에 따라 API는 계속 변경되며 여전히 이전 버전을 사용하는 고객이 있을 수 있습니다. 따라서 여러 버전의 API를 지원해야 합니다.

다음과 같이 여러 가지 방법으로 API 버전을 지정할 수 있습니다.

- **헤더 사용**: GitHub API는 이 접근 방식을 사용합니다. 요청을 처리해야 하는 API 버전을 알려주는 Accept 헤더를 추가할 수 있습니다. 예를 들어 다음을 고려하십시오.
수락: application/vnd.github.v3+json

이 접근 방식은 기본 버전을 설정하는 이점을 제공합니다. Accept 헤더가 없으면 기본 버전으로 연결되어야 합니다. 그러나 최근 API 업그레이드 후 버전 관리 헤더를 사용하는 REST 클라이언트가 변경되지 않으면 기능이 중단될 수 있습니다. 따라서 버전 관리 헤더를 사용하는 것이 좋습니다.

- **엔드포인트 경로 사용**: 이 접근 방식에서는 엔드포인트 경로 자체에 버전을 추가합니다. 예: https://demo.app/api/v1/persons. 여기서 v1은 버전 1이 경로 자체에 추가되고 있음을 나타냅니다.

기본 버전 관리를 즉시 설정할 수 없습니다. 그러나 요청 전달과 같은 다른 방법을 사용하여 이 제한을 극복할 수 있습니다. 클라이언트는 이 접근 방식에서 항상 API의 의도된 버전을 사용합니다.

기본 설정 및 보기에 따라 이전 버전 관리 방법 중 하나를 선택할 수 있습니다. 그러나 중요한 점은 항상 버전 관리를 사용해야 한다는 것입니다.

### 5. Nested resources
다음과 같은 매우 흥미로운 질문을 고려하십시오. 중첩되거나 특정 관계가 있는 리소스에 대한 끝점을 어떻게 구성할 것입니까? 전자 상거래 관점에서 고객 리소스의 몇 가지 예를 살펴보겠습니다.

- GET /customers/1/addresses: 고객 1의 주소 모음을 반환합니다.
- GET /customers/1/addresses/2: 고객 1의 두 번째 주소를 반환합니다.
- POST /customers/1/addresses: 고객 1의 주소에 새 주소를 추가합니다.
- PUT /customers/1/address/2: 이것은 고객 1의 두 번째 주소를 대체합니다.
- PATCH /customers/1/address/2: 고객 1의 두 번째 주소를 부분적으로 업데이트합니다.
- DELETE /customers/1/address/2: 고객 1의 두 번째 주소를 삭제합니다.

여태까지는 그런대로 잘됐다. 이제 완전히 별도의 주소 리소스 엔드포인트(GET /addresses/2)를 가질 수 있습니까? 그것은 의미가 있으며 그것을 요구하는 관계가 있는 경우 그렇게 할 수 있습니다. 예를 들어 주문 및 지불. /orders/1/payments/1 대신에 별도의 /payments/1 엔드포인트를 선호할 수 있습니다. 마이크로서비스 세계에서는 이것이 더 합리적입니다. 예를 들어 주문과 결제 모두에 대해 두 개의 별도 RESTful 웹 서비스가 있습니다.

이제 이 접근 방식을 하이퍼미디어와 결합하면 작업이 더 쉬워집니다. 고객 1에게 REST API 요청을 하면 고객 1 데이터와 주소 링크를 하이퍼미디어(즉, 링크)로 제공합니다. 주문에도 동일하게 적용됩니다. 주문의 경우 결제 링크를 하이퍼미디어로 사용할 수 있습니다.

그러나 어떤 경우에는 하이퍼미디어 제공 URL을 사용하여 관련 리소스를 가져오는 대신 단일 요청으로 완전한 응답을 원할 수 있습니다. 이렇게 하면 웹 조회수가 줄어듭니다. 그러나 엄지 손가락 규칙은 없습니다. 플래그 작업의 경우 중첩 엔드포인트 접근 방식을 사용하는 것이 좋습니다. 예를 들어 GitHub API의 경우 PUT /gist/2/star(별을 추가함) 및 DELETE /gist/2/star(별을 취소함)입니다.

또한 일부 시나리오에서는 검색 작업과 같이 여러 리소스가 관련된 경우 적절한 리소스 이름을 찾지 못할 수 있습니다. 이 경우 직접 /search 엔드포인트를 사용해야 합니다. 이것은 예외입니다.

### 6. 보안 API
API 보안은 세심한 주의가 필요한 또 다른 기대 사항입니다. 다음은 몇 가지 권장 사항입니다.

- 암호화된 통신에는 항상 HTTPS를 사용하십시오.
- 항상 OWASP의 주요 API 보안 위협 및 취약점을 찾으십시오. 이들은 웹사이트(https://owasp.org/www-project-api-security/) 또는 GitHub 저장소(https://github.com/OWASP/API-Security)에서 찾을 수 있습니다.
- 보안 REST API에는 인증이 있어야 합니다. REST API는 상태 비저장입니다. 따라서 REST API는 쿠키나 세션을 사용하지 않아야 합니다. 대신 JWT 또는 OAuth 2.0 기반 토큰을 사용하여 보안을 유지해야 합니다.

### 7. 문서
문서는 쉽게 액세스할 수 있어야 하며 해당 버전이 있는 최신 구현으로 최신 상태여야 합니다. 항상 샘플 코드와 예제를 제공하는 것이 좋습니다. 개발자의 통합 작업을 더 쉽게 만듭니다.

변경 로그 또는 릴리스 로그에는 영향을 받는 모든 라이브러리가 나열되어야 하며, 일부 API가 더 이상 사용되지 않는 경우 교체 API 또는 해결 방법이 설명서 내에서 자세히 설명되어야 합니다.

### 8. **상태 코드**
HTTP 메서드 및 상태 코드 탐색 섹션에서 상태 코드에 대해 이미 배웠을 수 있습니다. 거기에서 논의된 것과 동일한 지침을 따르십시오.

### 9. **Caching**

HTTP는 이미 캐싱 메커니즘을 제공합니다. REST API 응답에 추가 헤더를 제공하기만 하면 됩니다. 그런 다음 REST 클라이언트는 유효성 검사를 사용하여 호출할지 아니면 캐시된 응답을 사용할지 확인합니다. 두 가지 방법이 있습니다.

- ETag: ETag는 리소스 표현(즉, 응답 객체)의 해시 또는 체크섬 값을 포함하는 특수 헤더 값입니다. 이 값은 응답 표현과 관련하여 변경되어야 합니다. 리소스 응답이 변경되지 않으면 동일하게 유지됩니다. 
이제 클라이언트는 ETag 값을 포함하는 If-None-Match라는 다른 헤더 필드와 함께 요청을 보낼 수 있습니다. 서버가 이 요청을 수신하고 리소스 표현 값의 해시 또는 체크섬 값이 If-None-Match와 다르다는 것을 발견한 경우에만 ETag 헤더에서 새로운 표현과 이 해시 값으로 응답을 반환해야 합니다. 동일하다고 판단되면 서버는 단순히 304(수정되지 않음) 상태 코드로 응답해야 합니다.

- Last-Modified: 이 접근 방식은 ETag 방식과 동일합니다. 해시 또는 체크섬을 사용하는 대신 RFC-1123 형식(Last-Modified: Wed, 21 Oct 2015 07:28:00 GMT)의 타임스탬프 값을 사용합니다. ETag보다 정확도가 떨어지며 낙하 메커니즘에만 사용해야 합니다.

여기서 클라이언트는 Last-Modified 응답 헤더에서 받은 값과 함께 If-Modified-Since 헤더를 보냅니다. 서버는 리소스 수정 타임스탬프 값을 If-Modified-Since 헤더 값과 비교하고 일치하는 경우 304 상태를 보냅니다. 그렇지 않으면 새로운 Last-Modified 헤더와 함께 응답을 보냅니다.

### 10. 요금 제한
    
이는 API의 남용을 방지하려는 경우에 중요합니다. HTTP 상태 코드 429 Too Many Requests는 속도 제한을 초과할 때 사용됩니다. 현재, 속도 제한이 초과되기 전에 클라이언트에게 경고를 전달하는 표준은 없습니다. 그러나 응답 헤더를 사용하여 이에 대해 통신하는 일반적인 방법이 있습니다. 여기에는 다음이 포함됩니다.

- X-Ratelimit-Limit: 현재 기간에 허용된 요청 수
- X-Ratelimit-Remaining: 현재 기간의 남은 요청 수
- X-Ratelimit-Reset: 현재 기간의 남은 시간(초)
- X-Ratelimit-Used: 현재 기간에 사용된 요청 수

GitHub API에서 보낸 헤더를 확인할 수 있습니다. 예를 들어 다음과 유사할 수 있습니다.

- X-Ratelimit-Limit: 60
- X-Ratelimit-Remaining: 55
- X-Ratelimit-Reset: 1601299930
- X-Ratelimit-Used: 5

지금까지 REST와 관련된 다양한 개념에 대해 논의했습니다. 다음으로 샘플 앱에 대해 논의하겠습니다.



## 전자상거래 앱 개요

전자상거래 앱은 간단한 온라인 쇼핑 애플리케이션입니다. 다음과 같은 기능을 제공합니다.

- 사용자는 제품을 탐색할 수 있습니다.
- 사용자는 장바구니에 제품을 추가/제거/업데이트할 수 있습니다.
- 사용자는 주문을 할 수 있습니다.
- 사용자는 배송 주소를 수정할 수 있습니다.
- 애플리케이션은 단일 통화만 지원할 수 있습니다.

전자 상거래는 매우 인기 있는 도메인입니다. 기능을 살펴보면 경계 컨텍스트를 사용하여 애플리케이션을 다음 하위 도메인으로 나눌 수 있습니다.

- Users: 이 하위 도메인은 사용자와 관련이 있습니다. 사용자 관리를 위한 REST API를 제공하는 사용자 RESTful 웹 서비스를 추가합니다.

- Carts: 이 하위 도메인은 장바구니와 관련이 있습니다. 장바구니 관리를 위한 REST API를 제공하는 장바구니 RESTful 웹 서비스를 추가합니다. 사용자는 장바구니 항목에 대해 CRUD 작업을 수행할 수 있습니다.
- Product: 이 하위 도메인은 제품 카탈로그와 관련이 있습니다. 제품 검색 및 검색을 위한 REST API를 제공하는 제품 RESTful 웹 서비스를 추가합니다.

- Order: 이 하위 도메인은 주문과 관련이 있습니다. 사용자가 주문할 수 있도록 REST API를 제공하는 주문 RESTful 웹 서비스를 추가합니다.

- Payment: 이 하위 도메인은 결제와 관련이 있습니다. 결제 처리를 위한 REST API를 제공하는 결제 RESTful 웹 서비스를 추가합니다.

- Shipping: 이 하위 도메인은 배송과 관련이 있습니다. 주문 추적 및 배송을 위한 REST API를 제공하는 배송 RESTful 웹 서비스를 추가합니다.
  
다음은 앱 아키텍처를 시각적으로 나타낸 것입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_1.2_B16561.jpg)

Figure 1.2 – The e-commerce app architecture

We'll implement a RESTful web service for each of the subdomains. We'll keep the implementation simple, and we will focus on learning these concepts throughout this book.

## Summary

이 장에서는 REST 아키텍처 스타일의 기본 개념을 배웠습니다. 이제 HTTP를 기반으로 하는 REST가 통합을 단순화하고 쉽게 만드는 방법을 알게 되었습니다. 또한 의미 있는 방식으로 REST API를 작성할 수 있는 다양한 HTTP 개념을 살펴보았습니다. 또한 HATEOAS가 REST 구현의 필수적인 부분인 이유도 배웠습니다. 또한 REST API를 설계하기 위한 모범 사례를 배웠습니다. 또한 전자 상거래 앱에 대한 개요도 살펴보았습니다. 이 샘플 앱은 책 전체에서 사용됩니다.

다음 장에서는 Spring Framework와 그 기초에 대해 배울 것입니다.

## Questions

- Why have RESTful web services became so popular and, arguably, the industry standard?
- What is the difference between RPC and REST?
- How would you explain HATEOAS?
- What error codes should be used for server-related issues?
- Should verbs be used to form REST endpoints, and why?

## Further reading

- Architectural Styles and the Design of Network-based Software Architectures can be found at https://www.ics.uci.edu/~fielding/pubs/dissertation/top.htm.
- The URI Generic Syntax (RFC-3986) can be found at https://tools.ietf.org/html/rfc3986.
- The URN Syntax (RFC-2141) can be found at https://tools.ietf.org/html/rfc2141.
- HTTP Response Status Codes – RFC 7231 can be found at https://tools.ietf.org/html/rfc7231.
- HTTP Response Status Codes – Mozilla Developer - - Network can be found at https://developer.mozilla.org/en-US/docs/Web/HTTP/Status.
- REST APIs must be hypertext-driven can be found at https://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven.
- RFC for the URI template can be found at https://tools.ietf.org/html/rfc6570.
- The OWASP API security project can be found at https://owasp.org/www-project-api-security/ and https://github.com/OWASP/API-Security.