# Chapter 3: API 명세와 구현

이전 장에서는 REST API의 디자인 측면과 RESTful 웹 서비스 개발에 필요한 Spring 기본 사항에 대해 배웠습니다. 이 장에서는 이 두 영역을 사용하여 REST API를 구현합니다. 구현을 위해 디자인 우선 접근 방식을 선택했습니다. 먼저 API를 설계하고 나중에 구현하기 위해 OpenAPI 명세를 사용합니다. 또한 요청을 처리하는 동안 발생하는 오류를 처리하는 방법도 배우게 됩니다. 여기서는 참고용으로 샘플 전자상거래 앱의 API를 설계하고 구현합니다.

이 장의 일부로 다음 주제를 다룰 것입니다.

- OpenAPI 명세로 API 설계
- OpenAPI 명세를 Spring 코드로 변환
- OpenAPI 명세 코드 인터페이스 구현
- 전역 예외 처리기 추가

## Technical requirements

You need the following to execute the instructions in this chapter:

- IDE
- JDK 16
- 의존성과 Gradle을 다운로드

GitHub(https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter03)에서 이 장의 코드 파일을 찾을 수 있습니다.

## OAS로 API 설계하기

API 코딩을 직접 시작할 수 있습니다. 그러나 이러한 접근 방식은 잦은 수정, API 관리의 어려움, 특히 비기술적 도메인 팀이 주도하는 검토의 어려움과 같은 많은 문제를 야기합니다. 따라서 디자인 우선 접근 방식을 사용해야 합니다.

마음에 떠오르는 첫 번째 질문은 REST API를 어떻게 설계할 수 있습니까? 
"1장, RESTful 웹 서비스 기본 사항"에서 REST API 구현을 제어하는 ​​기존 표준이 없다는 것을 배웠습니다. OpenAPI 명세는 적어도 REST API의 명세 및 설명 측면을 해결하기 위해 도입되었습니다. 
이를 통해 YAML 또는 JSON 마크업 언어로 REST API를 작성할 수 있습니다.

전자 상거래 앱 REST API를 구현하기 위해 OAS 버전 3.0(https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md)을 사용합니다.

OAS는 이전에는 `Swagger 명세`라고 알려졌습니다. 그러나 OAS 지원 도구는 여전히 `Swagger`로 알려져 있습니다. `Swagger`는 REST API의 전체 개발 수명 주기를 돕는 오픈 소스 프로젝트입니다. 

이 장에서는 다음 `Swagger`를 사용할 것입니다.

- Swagger Editor: 전자상거래 앱 REST API를 설계하고 설명. 이를 통해 REST API의 디자인 및 설명을 작성하고 동시에 미리 볼 수 있습니다. OAS 3.0을 사용하고 있는지 확인하십시오. 이 책을 쓰는 시점에서 기본값은 OpenAPI 버전 2.0입니다. 편집 | OpenAPI 3으로 변환합니다. 취소 및 변환 옵션과 함께 포함된 메시지가 나타납니다. 변환 버튼을 클릭하여 OAS를 버전 2.0에서 3.0으로 변환합니다.

- Swagger Codegen: Spring 기반 API 인터페이스를 생성. Swagger Codegen 위에서 작동하는 코드를 생성하기 위해 Gradle 플러그인(https://github.com/int128/gradle-swagger-generator-plugin)을 사용합니다. OpenAPI 도구 Gradle 플러그인 – OpenAPI 생성기(https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin)도 있습니다.

- REST API 문서 생성을 위한 Swagger UI(https://swagger.io/swagger-ui/). API 문서를 생성하는 데 동일한 Gradle 플러그인이 사용됩니다.

다음으로 OAS 개요에 대해 논의해 보겠습니다.


## OAS 기본 구조

OpenAPI 정의 구조는 다음과 같은 섹션으로 나눌 수 있습니다:

- openapi (version)
- info
- externalDocs

- servers
- tags
- paths
- components

처음 세 섹션(openapi, info 및 externalDocs)은 API의 메타데이터를 정의하는 데 사용됩니다.

API 정의는 한 파일에 있거나 여러 파일로 나눌 수 있습니다. OAS는 둘 다 지원합니다. 샘플 전자 상거래 API를 정의하는 데 한 파일을 사용합니다.

이 모든 섹션을 이론적으로 다루고 전자 상거래 API 정의를 작성하는 대신 두 가지를 함께 논의합니다. 
먼저 전자 상거래 API의 각 섹션 정의를 다룬 다음 사용 이유와 의미에 대해 논의합니다.

### 메타 데이터 섹션

Let's have a look at the metadata sections of the e-commerce API definitions:

```yaml
openapi: 3.0.3
info:
  title: Sample Ecommerce App
  description:
    'This is a ***sample ecommerce app API***.  You can find out more about Swagger at [swagger.io](http://swagger.io).
    Description supports markdown markup. For example, you can use the `inline code` using back ticks.'
  termsOfService: https://github.com/PacktPublishing/Modern- API-Development-with-Spring-and-Spring- Boot/blob/master/LICENSE
  contact:
    email: support@packtpub.com
  license:
    name: MIT
    url: https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/master/LICENSE
  version: 1.0.0
externalDocs:
  description: Document link you want to generate along with API.
  url: http://swagger.io
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter03/src/main/resources/api/openapi.yaml

Now, we have written the metadata definitions of our API. Let's discuss each in detail.

### openapi

openapi 섹션은 API 정의를 작성하는 데 사용되는 OAS를 알려줍니다. OpenAPI는 시맨틱 버전 관리(https://semver.org/)를 사용합니다. 즉, 버전이 Major:minor:patch 형식이 됩니다. openapi 메타데이터 값을 보면 3.0.3을 사용하고 있습니다. 이것은 우리가 패치 3과 함께 메이저 버전 3을 사용하고 있음을 보여줍니다.

### info

정보 섹션에는 API에 대한 메타데이터가 포함됩니다. 이 정보는 문서 생성에 사용되며 클라이언트가 사용할 수 있습니다. 여기에는 제목과 버전만 필수 필드이고 나머지는 선택 필드인 다음 필드가 포함됩니다.

- title: API의 제목입니다.

- description: API 세부 정보를 설명하기 위해 사용합니다. >(각괄호) 기호는 여러 줄 값을 추가하는 데 사용됩니다.

- termsOfService: 서비스 약관으로 연결되는 URL입니다. 올바른 URL 형식을 따르는지 확인하십시오.

- contact: API 제공자의 연락처 정보입니다. 이메일 속성은 담당자/조직의 이메일 주소여야 합니다. 우리가 사용하지 않은 다른 속성은 이름과 URL입니다. name 속성은 담당자 또는 조직의 이름을 나타냅니다. URL 속성은 연락처 페이지에 대한 링크를 제공합니다. 이 필드는 선택 사항이며 모든 속성도 선택 사항입니다.

- license: 라이선스 정보입니다. name 속성은 MIT와 같은 올바른 라이선스 이름을 나타내는 필수 필드입니다. url은 선택 사항이며 라이센스 문서에 대한 링크를 제공합니다.

- version: API 버전을 문자열 형식으로 노출합니다.

#### externalDocs

externalDocs는 노출된 API의 확장 문서를 가리키는 선택적 필드입니다. 설명과 URL의 두 가지 속성이 있습니다. 설명은 외부 문서의 요약을 정의하는 선택적 필드입니다. 설명에 Markdown 구문을 사용할 수 있습니다. url 속성은 필수이며 외부 문서에 대한 링크입니다.

## servers와 tags

```yaml
servers:
  - url: https://ecommerce.swagger.io/v2
tags:
  - name: cart
    description: Everything about cart
    externalDocs:
      description: Find out more (extra document link)
      url: http://swagger.io
  - name: order
    description: Operation about orders
  - name: user
    description: Operations about users
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter03/src/main/resources/api/openapi.yaml

#### servers

서버는 API를 호스팅하는 서버 목록이 포함된 선택적 섹션입니다. 호스팅된 API 문서가 대화형인 경우 Swagger UI에서 이를 사용하여 API를 직접 호출하고 응답을 표시할 수 있습니다. 제공되지 않으면 호스트된 문서 서버의 루트(/)를 가리킵니다. 서버 URL은 url 속성을 사용하여 표시됩니다.

#### tag

루트 수준에서 정의된 태그 섹션에는 태그 및 해당 메타데이터 컬렉션이 포함됩니다. 태그는 리소스에서 수행되는 작업을 그룹화하는 데 사용됩니다. 태그 메타데이터에는 필수 필드인 이름과 두 개의 추가 선택적 속성인 description 및 externalDocs가 포함됩니다.

name 속성은 태그 이름을 포함합니다. 메타데이터에 대한 이전 섹션에서 설명 및 externalDocs 필드에 대해 이미 논의했습니다.

## components

순차적으로 진행했다면 path를 먼저 논의했을 것입니다. 그러나 개념적으로는 path 섹션에서 모델을 사용하기 전에 먼저 모델을 작성하고 싶습니다. 따라서 먼저 components 섹션에 대해 설명합니다.

다음은 샘플 전자상거래 앱의 components 섹션에 있는 코드입니다.

```yaml
components:
  schemas:
    Cart:
      description: Shopping Cart of the user
      type: object
      properties:
        customerId:
          description: Id of the customer who possesses the cart
          type: string
        items:
          description: Collection of items in cart.
          type: array
          items:
            $ref: '#/components/schemas/Item'
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter03/src/main/resources/api/openapi.yaml

여기에서 Cart 모델은 object이며 Id(string) 및 items(array)의 두 필드를 포함합니다.

*THE OBJECT DATA TYPE*

모든 모델 또는 필드를 개체로 정의할 수 있습니다. `type`을 `object`로 표시하면 다음 속성은 모든 객체의 필드로 구성된 속성입니다. 예를 들어, 이전 코드의 장바구니 모델은 다음 구문을 가집니다.

```yaml
type: object
properties:
    <field name>:
        type: <data type>
```

OAS는 다음과 같은 6가지 기본 데이터 유형을 지원합니다(모두 소문자).

- string
- number
- integer
- boolean
- object
- array

string, object 및 array 데이터 타입을 사용한 Cart 모델에 대해 논의해 보겠습니다. 다른 데이터   타입은 number, integer 및 boolean입니다. 이제 date-time 및 float 타입 등을 정의하는 방법이 궁금할 것입니다. object 타입과 함께 사용할 수 있는 형식 속성으로 이를 수행할 수 있습니다. 

예를 들어 다음 코드를 살펴보십시오.

```yaml
orderDate:
    type: string
    format: date-time
```

이전 코드에서 orderDate는 string으로 정의되지만 format은 문자열 값을 결정합니다. format은 date-time으로 표시되므로 orderDate 필드는 예를 들어 RFC 3339, 섹션 5.6에 정의된 형식의 날짜와 시간을 포함합니다.

```
2020-10-22T19:31:58Z.
```
There are some other common formats you can use along with types, as follows:

- type: number with format: float
- type: number with format: double
- type: integer with format: int32
- type: integer with format: int64
- type: string with format: date: for example, 2020-10-22.
- type: string with format: byte: Base64-encoded values.
- type: string with format: binary:

Cart 모델의 items 필드는 사용자 정의 항목 타입의 배열입니다. 

여기서 Items은 또 다른 모델이며 $ref를 사용하여 참조됩니다. 모든 사용자 정의 유형은 $ref를 사용하는 참조입니다. Item 모델은 구성 요소/스키마 섹션의 일부이기도 합니다. 따라서 $ref 값에는 `#/component/schemas/{type}`이 있는 사용자 정의 타입에 대한 앵커가 포함됩니다.

$ref는 참조 객체를 나타냅니다. JSON 참조(https://tools.ietf.org/html/draft-pbryan-zyp-json-ref-03)를 기반으로 하며 YAML에서 동일한 의미를 따릅니다. 동일한 문서 또는 외부 문서의 개체를 참조할 수 있습니다. 따라서 API 정의가 여러 파일로 나누어져 있을 때 사용합니다. 
이전 코드에서 이미 한 가지 사용법을 보았습니다.

```
# Relative Schema Document
$ref: Cart.yaml
# Relative Document with embedded Schema
$ref: definitions.yaml#/Cart
```
이전 코드에 대한 또 다른 주의 사항이 있습니다. 자세히 보면 두 항목을 찾을 수 있습니다. 하나는 Cart 객체 타입의 속성이고 다른 하나는 배열 타입의 속성입니다. 전자는 Cart 객체의 필드인 단순합니다. 그러나 후자는 배열에 속하며 배열 구문의 일부입니다.

> **ARRAY SYNTAX**

```yaml
type: array
items:
    type: <type of object>

```
i. 객체 타입을 배열로 배치하면 중첩 배열을 가질 수 있습니다.

ii. 코드와 같이 $ref를 이용하여 사용자 정의 타입을 참조할 수도 있습니다. (그럼 Items에 type 속성은 필요하지 않습니다.)

item 모델은 다음과 같습니다.

```yaml
Item:
  description: Items in shopping cart
  type: object
  properties:
    id:
      description: Item Identifier
      type: string
    quantity:
      description: The item quantity
      type: integer
      format: int32
    unitPrice:
      description: The item's price per unit
      type: number
      format: double
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter03/src/main/resources/api/openapi.yaml

Item 모델은 `components/schema` 섹션의 일부이기도 합니다. 
이제 `components/schema` 섹션에서 모델을 정의하는 방법을 배웠습니다. 
다음으로 OAS의 path 섹션에서 API의 끝점을 정의하는 방법을 논의할 것입니다.

> **IMPORTANT NOTE**

스키마와 마찬가지로 구성 요소 섹션에서 requestBodies(요청 페이로드) 및 응답을 정의할 수도 있습니다. 이것은 일반적인 요청 본문과 응답이 있을 때 유용합니다..

### paths

여기서 끝점을 정의합니다. 이것은 URI를 형성하고 HTTP 메소드를 첨부하는 곳입니다.

`POST /api/v1/carts/{customerId}/items`에 대한 정의를 작성해 보겠습니다. 이 API는 지정된 고객 식별자와 연결된 cart에 항목을 추가합니다.

```yaml
paths:
  /api/v1/carts/{customerId}/items:
    post:
      tags:
        - cart
      summary: Adds an item in shopping cart
      description: Adds an item to the shopping cart
      operationId: addCartItemsByCustomerId
      parameters:
        - name: customerId
          in: path
          description: Customer Identifier
          required: true
          schema:
            type: string
      requestBody:
        description: Item object
        content:
          application/xml:
            schema:
              $ref: '#/components/schemas/Item'
          application/json:
            schema:
              $ref: '#/components/schemas/Item'
      responses:
        201:
          description: Item added successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/Item'
        404:
          description: Given customer ID doesn't exist
          content: {}
```

여기서 v1은 API의 버전을 나타냅니다. 각 엔드포인트 경로(예: `/api/v1/carts/{customerId}/items`)에는 연결된 HTTP 메소드(예: post)가 있습니다. 끝점 경로는 항상 /로 시작합니다.

그러면 각 메서드에는 태그, 요약, 설명, operationId, 매개 변수, 응답 및 requestBody의 7개 필드가 있을 수 있습니다. 다음 하위 섹션에서 각각에 대해 설명합니다.

### tags
Tags are used for grouping APIs, as shown in the following screenshot for APIs tagged with cart. Tags can also be used by Swagger Codegen to generate the code:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_3.1_B16561.jpg)

Figure 3.1 — Cart APIs

보시다시피 각 태그에 대해 별도의 API 인터페이스를 생성합니다.

### summary and description

summary 및 설명 섹션은 앞서 OAS 섹션의 메타데이터 섹션에서 논의한 것과 동일합니다. 여기에는 각각 주어진 API의 작업 요약과 자세한 설명이 포함되어 있습니다. 평소와 같이 동일한 스키마를 참조하므로 설명 필드에 Markdown을 사용할 수 있습니다.

### operationId

이것은 작업의 이름을 나타냅니다. 이전 코드에서 볼 수 있듯이 addCartItemsByCustomerId 값을 할당했습니다. 이 동일한 작업 이름은 Swagger Codegen에서 생성된 API 인터페이스의 메서드 이름으로 사용됩니다.

#### parameters

자세히 보면 이름 필드 앞에 -(하이픈)이 있습니다. 이것은 배열 요소로 선언하는 데 사용됩니다. 매개변수 필드는 여러 매개변수를 포함할 수 있으며 실제로는 경로 및 쿼리 매개변수의 조합이므로 배열로 선언됩니다.

경로 매개변수의 경우 매개변수 아래의 이름 값이 중괄호 안의 경로에 지정된 값과 동일한지 확인해야 합니다.

매개변수 필드에는 API 쿼리, 경로, 헤더 및 쿠키 매개변수가 포함됩니다. 이전 코드에서는 경로 매개변수(in 필드의 값)를 사용했습니다. 쿼리 매개변수로 선언하려는 경우 값을 쿼리로 변경할 수 있습니다.

description은 평소와 같이 정의된 매개변수를 설명합니다.

부울 매개변수인 매개변수 섹션 내의 필수 필드를 사용하여 필드를 필수 또는 선택으로 표시할 수 있습니다.

마지막으로 스키마 필드가 사용되는 매개변수의 데이터 유형을 선언해야 합니다.

#### responses

response는 모든 API 작업의 필수 필드입니다. 요청 시 API 작업에서 응답할 수 있는 응답 타입을 정의합니다. 기본 필드로 HTTP 상태 코드가 포함되어 있습니다. 기본 응답 또는 200과 같은 성공적인 HTTP 상태 코드가 될 수 있는 응답이 하나 이상 있어야 합니다. API 작업에서 다른 응답이 정의되거나 사용 가능하지 않을 때 기본 응답이 사용됩니다.

response 타입(예: 200 또는 기본값) 필드에는 세 가지 유형의 필드가 있습니다.

- `description`: 응답을 설명

- `headers`: 헤더와 값을 정의 

A headers example is shown as follows:
  
```yaml
responses:
   200:
      description: operation successful
      headers:
         X-RateLimit-Limit:
            schema:
            type: integer
```

- content: 콘텐츠 타입을 정의합니다. 여기서는 `application/json`을 사용했습니다 마찬가지로 `application/xml`과 같은 타입을 정의할 수 있습니다. 콘텐츠 타입 필드에는 schema 필드를 사용하여 정의할 수 있는 실제 응답 객체가 포함되어 있습니다. 그 안에 항목 모델의 배열을 정의했기 때문입니다.

앞서 언급했듯이 구성 요소 섹션에서 재사용 가능한 응답을 생성하고 $ref를 사용하여 직접 사용할 수 있습니다.

#### requestBody

requestBody는 요청 페이로드 객체를 정의하는 데 사용됩니다. 응답 객체와 마찬가지로 requestBody에는 설명 및 콘텐츠 필드도 포함됩니다. 콘텐츠는 응답 개체에 대해 정의된 방식과 유사한 방식으로 정의할 수 있습니다. 예를 들어 `POST /carts/{customerId}/items`의 이전 코드를 참조할 수 있습니다. 응답으로 구성 요소 섹션 아래에 재사용 가능한 요청 본문을 만들고 $ref를 사용하여 직접 사용할 수도 있습니다.

이제 OAS를 사용하여 API 사양을 정의하는 방법을 배웠습니다. 여기에서는 샘플 전자 상거래 앱 API의 일부를 설명했습니다. 마찬가지로 다른 API를 설명할 수 있습니다. openapi.yaml을 참조할 수 있습니다. 전자상거래 API 정의의 전체 코드입니다.

openapi.yaml에서 코드를 복사하여 https://editor.swagger.io 편집기에 붙여넣어 멋진 사용자 인터페이스에서 API를 보고 사용하는 것이 좋습니다. 기본 버전이 3.0으로 설정되지 않은 경우 편집 메뉴를 사용하여 API를 OpenAPI 버전 3으로 변환해야 합니다.

API 설계를 마쳤으므로 이제 openapi.yaml을 사용하여 코드를 생성합니다.



## Converting OAS to Spring code

지금까지 RESTful 웹 서비스 이론과 개념, Spring 기초에 대해 배웠고 샘플 전자 상거래 애플리케이션에 대한 첫 번째 API 사양을 설계했습니다.

- Gradle
- Java
- Spring Boot: 2.3.5.RELEASE
- Project metadata with your preferred values
- Packaging: JarAdded
- Java: 15 (You can change it to 14 in the build.gradle file later.)
- Dependencies: 'org.springframework.boot:spring-boot-starter-web' (Spring Web in Spring Initializer)

Once you open the project in your favorite IDE (IntelliJ, Eclipse, or NetBeans), you can add the following extra dependencies required for OpenAPI support under dependencies in the build.gradle file:
```
swaggerCodegen 'org.openapitools:openapi-generator-cli:4.3.1'
compileOnly 'io.swagger:swagger-annotations:1.6.2'
compileOnly 'org.springframework.boot:spring-boot-starter-validation'
compileOnly 'org.openapitools:jackson-databind-nullable:0.2.1'
implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml'
implementation 'org.springframework.boot:spring-boot-starter-hateoas'
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter03/build.gradle

As mentioned earlier, we are going to use a Swagger plugin for code generation from the API definitions we have just written. You can follow the next seven steps to generate the code.

### Step 1 – Gradle 플러그인 추가
To make use of the OpenAPI Generator CLI tool, you can add the Swagger Gradle plugin under `plugins {}` in `build.gradle` as shown:

```gradle
plugins {
  …
  …
  id 'org.hidetake.swagger.generator' version '2.18.2'
}
```

#### Step 2 – 코드 생성을 위한 OpenAPI config 정의
OpenAPI Generator의 CLI가 사용해야 하는 모델 및 API 패키지 이름이나 REST 인터페이스 또는 날짜/시간 관련 개체를 생성하는 데 사용해야 하는 라이브러리와 같은 특정 구성이 필요합니다. 
이러한 모든 구성 및 기타 구성은 `config.json`에서 정의할 수 있습니다.

```json
{
  "library": "spring-mvc",
  "dateLibrary": "java8",
  "hideGenerationTimestamp": true,
  "modelPackage": "com.packt.modern.api.model",
  "apiPackage": "com.packt.modern.api",
  "invokerPackage": "com.packt.modern.api",
  "serializableModel": true,
  "useTags": true,
  "useGzipFeature" : true,
  "hateoas": true,
  "withXml": true,
  "importMappings": {
      "Link": "org.springframework.hateoas.Link"
  }
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter03/src/main/resources/api/config.json

`importMappings`를 제외한 모든 속성은 자명합니다. 여기에는 YAML 파일에서 Java 또는 외부 라이브러리에 있는 타입으로의 매핑이 포함됩니다. 따라서 importMapping 객체에 대한 코드가 생성되면 생성된 코드에서 매핑된 클래스를 사용합니다. 모델에서 Link를 사용하는 경우 생성된 모델은 YAML 파일에 정의된 모델 대신 매핑된 'org.springframework.hateoas.Link' 클래스를 사용합니다.

- hatoas: Spring HATEOAS 라이브러리를 사용하고 HATEOAS 링크를 추가할 수 있습니다.

- withXML: XML 주석이 있는 모델을 생성하고 `application/xml` 타입을 지원할 수 있습니다.

You can find more information about the configuration at https://github.com/swagger-api/swagger-codegen#customizing-the-generator.

### Step 3 – defining the OpenAPI Generator ignore file

.gitignore와 비슷한 파일을 추가하여 생성하지 않으려는 특정 코드를 무시할 수도 있습니다. 파일(/src/main/resources/api/.openapi-generator-ignore)에 다음 코드 줄을 추가합니다.
```
**/*controller.java
```
우리는 컨트롤러를 생성하고 싶지 않습니다. 추가 후에는 API 인터페이스와 모델만 생성됩니다. 컨트롤러를 수동으로 추가하겠습니다.

### Step 4 – defining a swaggerSources task in the Gradle build file

Now, let's add the logic to the `swaggerSources` task in the `build.gradle` file:

```gradle
swaggerSources {
  def typeMappings = 'URI=URI'
  def importMappings = 'URI=java.net.URI'
  eStore {
    def apiYaml = "${rootDir}/src/main/resources/api/openapi.yaml"
    def configJson = "${rootDir}/src/main/resources/api/config.json"
    inputFile = file(apiYaml)
    def ignoreFile = file("${rootDir}/src/main/resources/api/.openapi-generator-ignore")
    code {
      language = 'spring'
      configFile = file(configJson)
      rawOptions = ['--ignore-file-override', ignoreFile, '--type-mappings',
          typeMappings, '--import-mappings', importMappings] as List<String>
      components = [models: true, apis: true, supportingFiles: 'ApiUtil.java']
      //depends On validation // Should be uncommented once
      //plugin starts supporting OA 3 validation
    }
}
}
```
여기에서는 openapi.yaml 파일의 위치를 ​​가리키는 inputFile을 포함하는 eStore(사용자 정의 이름)를 정의했습니다. 
입력을 정의한 후 생성기는 코드에서 구성된 출력을 생성해야 합니다.

언어(다양한 언어 지원), config.json을 가리키는 configFile, rawOptions(타입 및 가져오기 매핑 포함) 및 코드 블록의 구성 요소를 정의했습니다. 언어를 제외하고는 모두 선택 사항입니다.

우리는 단지 모델과 API를 생성하기를 원합니다. 클라이언트 또는 테스트 파일과 같은 다른 파일도 생성할 수 있습니다. 
ApiUtil.java는 생성된 API 인터페이스에 필요합니다. 그렇지 않으면 빌드 시간 동안 컴파일 오류가 발생하므로 구성 요소에 추가됩니다.

### 5단계 – compileJava 작업 의존성에 swaggerSources 추가

다음으로 swaggerSources를 compileJava 작업에 의존성 작업으로 추가해야 합니다. 
eStore에 정의된 코드 블록을 가리킵니다.
```
compileJava.dependsOn swaggerSources.eStore.code
```

### 6단계 – 생성된 소스 코드를 Gradle 소스 세트에 추가

또한 생성된 소스 코드와 리소스를 sourceSet에 추가해야 합니다. 이렇게 하면 생성된 소스 코드와 리소스를 개발 및 빌드에 사용할 수 있습니다.

```gradle
sourceSets.main.java.srcDir "${swaggerSources.eStore.code.outputDir}/src/main/java"
sourceSets.main.resources.srcDir "${swaggerSources.eStore.code.outputDir}/src/main/resources"
```

The source code will be generated in the `/build` directory of the project, such as `Chapter03\build\swagger-code-eStore`. This will append the generated source code and resources to Gradle sourceSets.

### Step 7 – 코드 생성, 컴파일 및 빌드를 위한 빌드 실행

마지막 단계는 빌드를 실행하는 것입니다. 빌드 경로에 실행 가능한 Java 코드가 있는지 확인하십시오. Java 버전은 `build.gradle` 속성(sourceCompatibility = '1.14') 또는 IDE 설정에 정의된 버전과 일치해야 합니다.
```sh
$ gradlew clean build
```
Once the build is executed successfully, you can find the generated code in the build directory, as shown in the following screenshot:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_3.2_B16561.jpg)

Figure 3.2 — OpenAPI generated code

In the next section, you'll implement the API interfaces generated by OpenAPI Codegen.


## OAS 코드 인터페이스 구현

지금까지 전자상거래 앱 모델과 API 인터페이스로 구성된 코드를 생성했습니다. 이러한 생성된 인터페이스에는 당사에서 제공한 YAML 설명에 따라 모든 주석이 포함됩니다. 예를 들어 CartApi.java에서 @RequestMapping, @PathVariable 및 @RequestBody에는 끝점 경로(/api/v1/carts/{customerId}/items), 경로 변수의 값(예: 경로의 {customerId})이 포함됩니다. 마찬가지로 생성된 모델에는 JSON 및 XML 콘텐츠 타입을 지원하는 데 필요한 모든 매핑이 포함됩니다.

`Swagger Codegen`은 우리를 위해 Spring 코드를 생성합니다. 
인터페이스를 구현하고 그 안에 비즈니스 로직을 작성하기만 하면 됩니다. `Swagger Codegen`은 제공된 각 태그에 대한 API 인터페이스를 생성합니다. 예를 들어 cart 및 결제 태그에 대해 각각 CartApi 및 PaymentAPI Java 인터페이스를 생성합니다. 모든 경로는 주어진 태그를 기반으로 하는 단일 Java 인터페이스로 함께 묶입니다. 
예를 들어 cart 태그가 있는 모든 API는 단일 Java 인터페이스인 CartApi로 함께 묶입니다.

이제 각 인터페이스에 대한 클래스를 만들고 구현하기만 하면 됩니다. 
`com.packt.modern.api.controllers` 패키지에 CartController.java를 만들고 CartApi를 구현합니다.

```java
@RestController
public class CartsController implements CartApi {
  private static final Logger log = LoggerFactory.getLogger(CartsController.class);

  @Override
  public ResponseEntity<List<Item>> addCartItemsByCustomerId (String customerId, @Valid Item item) {
    log.info("Request for customer ID: {}\nItem: {}", customerId, item);
    return ok(Collections.EMPTY_LIST);
  }

  @Override
  public ResponseEntity<List<Cart>> getCartByCustomerId(String customerId) {
    throw new RuntimeException("Manual Exception thrown");
  }
  // Other method implementations (omitted)
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter03/src/main/java/com/packt/modern/api/controllers/CartsController.java

여기서는 데모용으로 두 가지 방법을 구현했습니다. 다음 장에서 실제 비즈니스 로직을 구현할 것입니다.

항목(`POST /api/v1/carts/{customerId}/items`) 요청을 추가하기 위해 `addCartItemsByCustomerId()` 메소드 내부에 들어오는 요청 페이로드와 고객 ID를 기록하기만 하면 됩니다. 또 다른 메소드인 `getCartByCustomerId()`는 단순히 예외를 발생시킵니다. 이를 통해 다음 섹션에서 전역 예외 처리기를 시연할 수 있습니다.

## Adding a Global Exception Handler

여러 메서드로 구성된 여러 컨트롤러가 있습니다. 각 메서드에는 확인된 예외가 있거나 런타임 예외가 발생할 수 있습니다. 더 나은 유지 관리와 모듈화 그리고 깔끔한 코드를 위해 이러한 모든 오류를 처리할 수 있는 중앙 집중식 장소가 있어야 합니다.

Spring은 이를 위해 AOP 기능을 제공합니다. `@ControllerAdvice`로 주석이 달린 단일 클래스를 작성하기만 하면 됩니다. 그런 다음 각 예외에 대해 `@ExceptionHandler`를 추가하기만 하면 됩니다. 이 예외 처리기 메서드는 다른 관련 정보와 함께 사용자에게 친숙한 오류 메시지를 생성합니다.

해당 조직에서 타사 라이브러리 사용을 승인한 경우 `lombok` 라이브러리를 사용할 수 있습니다. 
이렇게 하면 getter, setter, 생성자 등에 대한 코드의 장황함이 제거됩니다.

먼저 모든 오류 정보를 포함하는 `exceptions` 패키지에 Error 클래스를 작성해 보겠습니다.

```java
public class Error {
  private static final long serialVersionUID = 1L;
  /**
   * App error code, which is different from HTTP error code.
   */
  private String errorCode;
  /**
   * Short, human-readable summary of the problem.
   */
  private String message;
  /**
   * HTTP status code.
   */
  private Integer status;
  /**
   * Url of request that produced the error.
   */
  private String url = "Not available";
  /**
   * Method of request that produced the error.
   */
  private String reqMethod = "Not available";
  // getters and setters (omitted)
}
```

You can add other fields here if required. The exceptions package will contain all the code for user-defined exceptions and global exception handling.

그런 다음 ErrorCode라는 enum을 작성하여 사용자 정의 에러와 에러 코드를 포함한 모든 예외 키를 넣는다.

```java
public enum ErrorCode {
  // Internal Errors: 1 to 0999
  GENERIC_ERROR("PACKT-0001", "The system is unable to complete the request. Contact system support."),
  HTTP_MEDIATYPE_NOT_SUPPORTED("PACKT-0002", "Requested media type is not supported. Please use application/json or   application/xml as 'Content-Type' header value"),
  HTTP_MESSAGE_NOT_WRITABLE("PACKT-0003", "Missing 'Accept' header. Please add 'Accept' header."),
  HTTP_MEDIA_TYPE_NOT_ACCEPTABLE("PACKT-0004", "Requested 'Accept' header value is not supported. Please use   application/json or application/xml as 'Accept' value"),
  JSON_PARSE_ERROR("PACKT-0005", "Make sure request payload should be a valid JSON object."),
  HTTP_MESSAGE_NOT_READABLE("PACKT-0006", "Make sure request payload should be a valid JSON or XML object according to 'Content-Type'.");
  private String errCode;
  private String errMsgKey;
  ErrorCode(final String errCode, final String errMsgKey) {
    this.errCode = errCode;
    this.errMsgKey = errMsgKey;
  }
  /**
   * @return the errCode
   */
  public String getErrCode() {
    return errCode;
  }
  /**
   * @return the errMsgKey
   */
  public String getErrMsgKey() {
    return errMsgKey;
  }
}
```

Here, we have just added actual error messages instead of message keys. You can add message keys and add the resource file to `src/main/resources` for internationalization.

다음에는 Error 객체를 만드는 유틸리티를 추가합니다.

```java
public class ErrorUtils {
  private ErrorUtils() {}
  /**
   * Creates and return an error object
   *
   * @param errMsgKey
   * @param errorCode
   * @param httpStatusCode
   * @param url
   * @return error
   */
  public static Error createError(final String errMsgKey, final String errorCode, 
          final Integer httpStatusCode) {
    Error error = new Error();
    error.setMessage(errMsgKey);
    error.setErrorCode(errorCode);
    error.setStatus(httpStatusCode);

    return error;
  }
}
```

끝으로 전역 예외 처리를 구현하는 클래스를 만듭니다.
```java
@ControllerAdvice
public class RestApiErrorHandler {
  private static final Logger log = LoggerFactory.getLogger(RestApiErrorHandler.class);
  private final MessageSource messageSource;

  @Autowired
  public RestApiErrorHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> handleException(HttpServletRequest request, Exception ex, Locale locale) {
    Error error = ErrorUtils.createError(ErrorCode.GENERIC_ERROR.getErrMsgKey(), 
          ErrorCode.GENERIC_ERROR.getErrCode(), 
          HttpStatus.INTERNAL_SERVER_ERROR.value())
      .setUrl(request.getRequestURL().toString()).setReqMethod(request.getMethod());
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Error> handleHttpMediaTypeNotSupportedException(HttpServletRequest request, HttpMediaTypeNotSupportedException ex, Locale locale) {
    Error error = ErrorUtils.createError(ErrorCode.HTTP_MEDIATYPE_NOT_SUPPORTED.getErrMsgKey(),
            ErrorCode.HTTP_MEDIATYPE_NOT_SUPPORTED.getErrCode(),
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
        .setUrl(request.getRequestURL().toString())
        .setReqMethod(request.getMethod());
    log.info("HttpMediaTypeNotSupportedException :: request.getMethod(): " + request.getMethod());
    return new ResponseEntity<>(error, HttpStatus. UNSUPPORTED_MEDIA_TYPE);
  }
```

보시다시피 클래스를 `@ControllerAdvice`로 표시하여 이 클래스가 REST 컨트롤러의 모든 요청 및 응답 처리를 추적할 수 있도록 하고 `@ExceptionHandler`를 사용하여 예외를 처리할 수 있도록 합니다.

이전 코드에서는 일반 내부 서버 오류 예외와 `HttpMediaTypeNotSupportException`이라는 두 가지 예외를 처리하고 있습니다. 처리 방법은 ErrorCode, HttpServletRequest 및 HttpStatus를 사용하여 Error 객체를 채웁니다. 
마지막에 적절한 HTTP 상태로 ResponseEntity 내부에 래핑된 오류를 반환합니다.

여기에서 사용자 정의 예외도 추가할 수 있습니다. 국제화 메시지를 지원하기 위해 Locale 인스턴스(메서드 매개변수)와 messageSource 클래스 멤버를 사용할 수도 있습니다. 

## Testing

Once the code is ready to run, you can compile and build the artifact using the following command from the root folder of the project:
```sh
gradlew clean build
```
The previous command removes the build folder and generates the artifact (compiled classes and JAR). After the successful build, you can run the application using the following command:

```sh
java -jar build\libs\Chapter03-0.0.1-SNAPSHOT.jar
```
Now, we can perform the tests using the curl command:

```sh
$ curl --request GET 'http://localhost:8080/api/v1/carts/1' --header 'Accept: application/xml'
```
This command calls the GET request for `/carts` with ID 1. Here, we demand the XML response using the Accept header, and we get the following response:

```xml
<Error>
    <errorCode>PACKT-0001</errorCode>
    <message>The system is unable to complete the request. Contact system support.</message>
    <status>500</status>
    <url>http://localhost:8080/api/v1/carts/1</url>
    <reqMethod>GET</reqMethod>
</Error>
```
If you changed the Accept header from application/xml to application/json, you would get the following JSON response:

```json
{
    "errorCode": "PACKT-0001",
    "message": "The system is unable to complete the request. Contact system support.",
    "status": 500,
    "url": "http://localhost:8080/api/v1/carts/1",
    "reqMethod": "GET"
}
```
Similarly, we can also call the add item to cart call, as shown:

```sh
$ curl --request POST 'http://localhost:8080/api/v1/carts/1/items' \
> --header 'Content-Type: application/json' \
> --header 'Accept: application/json' \
> --data-raw '{
>     "id": "1",
>     "quantity": 1,
>     "unitPrice": 2.5
> }'
```
구현에서는 단지 빈 컬렉션을 반환하기 때문에 응답으로 [](빈 배열)을 얻습니다. 요청과 함께 페이로드(항목 개체)를 보내기 때문에 이 요청에 Content-Type 헤더를 제공해야 합니다. 페이로드가 XML로 작성된 경우 Content-Type을 `application/xml`로 변경할 수 있습니다. Accept 헤더 값이 application/xml이면 <List/> 값을 반환합니다. Content-Type 및 Accept 헤더를 제거/변경하거나 잘못된 형식의 JSON 또는 XML을 사용하여 다른 오류 응답을 테스트할 수 있습니다.

이런 식으로 OpenAPI를 사용하여 API 설명을 생성한 다음 생성된 모델 및 API 인터페이스를 사용하여 API를 구현할 수 있습니다.

## 요약

이 장에서는 RESTful 웹 서비스를 작성하기 위해 디자인 우선 접근 방식을 선택했습니다. OAS를 사용하여 API 설명을 작성하는 방법과 Swagger Codegen 도구(Gradle 플러그인 사용)를 사용하여 모델 및 API 인터페이스를 생성하는 방법을 배웠습니다. 또한 모든 예외 처리를 중앙 집중화하기 위해 전역 예외 처리기를 구현했습니다. API Java 인터페이스가 있으면 비즈니스 로직에 대한 구현을 작성할 수 있습니다. 이제 RESTful API를 작성하기 위해 OAS 및 Swagger Codegen을 사용하는 방법을 알게 되었습니다. 또한 전역적으로 예외를 처리하는 방법도 배웠습니다.

다음 장에서는 데이터베이스 지속성이 있는 비즈니스 로직으로 완전한 API 인터페이스를 구현합니다.

## Questions

- What is OpenAPI and how does it help?
- How can you define a nested array in a model in a YAML OAS-based file?
- What annotations do we need to implement a Global Exception Handler?
- How you can use models or classes written in Java code in your OpenAPI description?
- Why do we only generate models and API interfaces using Swagger Codegen?
