# Chapter 13: GraphQL Fundamentals

이 장에서는 SDL, 쿼리, 변형 및 구독을 포함하여 GraphQL의 기본 사항에 대해 배웁니다. 이 지식은 다음 장에서 GraphQL 기반 API를 구현할 때 도움이 될 것입니다.

이 장에서는 다음 주제를 다룰 것입니다.

- GraphQL 소개
- GraphQL의 기초 학습
- GraphQL 스키마 설계
- GraphQL 쿼리 및 변형 테스트
- N+1 문제 풀기

이 장을 마치면 의미론, 스키마 디자인, 그리고 Spring과 Spring Boot를 사용하여 GraphQL 기반 API를 개발하는 데 필요한 모든 것을 포함하여 GraphQL의 기본 사항에 대해 알게 될 것입니다.

## 기술 요구 사항

이 장에서는 GraphQL을 둘러싼 이론을 다룹니다. 그러나 다음 장에서 제시하는 GraphQL 기반 서비스 코드를 개발하고 테스트하려면 다음이 필요합니다.

- IntelliJ 또는 Eclipse
- JDK 15

## GraphQL 소개

GraphQL은 모바일 및 웹 API를 구현하는 데 선호되는 방법입니다. GraphQL은 선언적 쿼리 및 조작 언어이며 API용 서버 측 런타임입니다. GraphQL은 클라이언트가 더도 말고 덜도 말고 원하는 데이터를 정확하게 쿼리할 수 있도록 합니다.

### GraphQL의 간략한 역사

2011년 Facebook은 모바일 브라우저에서 웹사이트의 성능을 개선하는 문제에 직면했습니다. 그들은 모바일 네이티브 기술로 자체 모바일 앱을 구축하기 시작했습니다. 그러나 API는 계층적 및 재귀적 데이터로 인해 표시에 미치지 못했습니다. 그들은 네트워크 호출을 최적화하기를 원했습니다. 그 당시 모바일 네트워크 속도는 세계의 일부 지역에서 Kb/s였습니다. 소비자가 모바일 장치로 이동하기 시작했기 때문에 고품질의 빠른 모바일 앱을 보유하는 것이 성공의 열쇠였습니다.

2012년에는 Facebook의 몇 명의 엔지니어가 협력하여 GraphQL을 만들었습니다. 처음에는 Facebook의 뉴스피드 기능을 설계하고 개발하는 데 사용되었지만 나중에는 인프라 전반에 걸쳐 사용되었습니다. 2015년에 오픈 소스로 공개되기 전에 Facebook에서 내부적으로 사용 중이었습니다. 이 때 GraphQL 사양과 JavaScript 구현을 대중에게 공개했습니다. 곧 Java를 포함하여 GraphQL 사양의 다른 언어 구현이 발표되기 시작했습니다.

Netflix와 Coursera도 효율적이고 성능이 뛰어난 API를 구축하기 위해 유사한 아이디어를 연구하고 있었습니다. Coursera는 앞으로 나아가지 않았지만 Netflix는 Falcor를 오픈 소스로 제공했습니다.

### GraphQL과 REST 비교

앞에서 REST를 사용하여 API를 개발했습니다. 실제로 전자 상거래 앱도 REST API를 사용하여 전자 상거래 기능을 구현했습니다. 필요한 GraphQL 개념이 어디에 적용되는지 이해할 수 있도록 이 장에서 REST를 계속 참조할 것입니다. 이 상관 관계는 GraphQL 개념을 쉽게 이해하는 데 도움이 됩니다.

**GraphQL은 REST보다 강력하고 유연하며 효율적입니다**.

사용자가 샘플 전자 상거래 UI 앱에 로그인하고 자동으로 제품 목록 페이지로 이동하면 UI 앱은 다음과 같이 세 가지 다른 엔드포인트을 사용합니다.

- 사용자 정보 가져오기
- 제품 목록 가져오기
- 사용자 장바구니 항목 가져오기

따라서, 기본적으로 백엔드에서 고정된 구조로 필요한 정보를 가져오려면 세 번 호출해야 합니다(응답으로 전송되는 필드는 변경할 수 없음).

반면에 GraphQL은 사용자 정보, 사용자 장바구니 데이터 및 제품 목록을 한번의 호출로 가져올 수 있습니다. GraphQL은 각 사용 사례에 대해 엔드포인트를 정의해야 하는 REST와 달리 단일 엔드포인트만 노출합니다. 이를 수행하는 새로운 REST 엔드포인트를 작성할 수 있다고 말할 수 있습니다. 이렇게 하면 이 특정 사용 사례를 해결할 수 있지만 유연하지 않습니다. 빠른 변경 반복을 허용하지 않습니다.

또한 GraphQL을 사용하면 요청의 백엔드에서 가져오려는 필드를 표현할 수 있습니다. 서버는 요청된 필드에 따라 응답을 제공합니다. 더도 말고 덜도 말고요.

새 필드 집합이 필요한 경우 새 REST 엔드포인트를 만들 필요가 없습니다. 예를 들어 제품에 사용자 리뷰를 추가할 수 있습니다. 이를 위해 GraphQL 쿼리에 리뷰 필드를 추가하기만 하면 됩니다. 마찬가지로 추가 필드를 사용할 필요가 없습니다. GraphQL 쿼리에 필요한 필드를 추가하기만 하면 됩니다. 

대신 REST의 응답에는 응답 객체에 특정 필드가 필요한지 여부에 관계없이 미리 정의된 필드가 포함됩니다. 그런 다음 클라이언트 측에서 필수 필드를 필터링해야 합니다. 따라서 GraphQL은 오버 페칭/언더 페칭 문제를 피함으로써 네트워크 대역폭을 효과적으로 사용합니다.

GraphQL API는 REST처럼 지속적인 변경이 필요하지 않습니다. 요구 사항 변경을 위해 API를 변경하거나 새 API를 추가해야 할 수 있습니다. 이것은 개발 속도와 반복을 향상시킵니다. 새 필드를 쉽게 추가하거나 더 이상 사용되지 않는 기존 필드를 표시할 수 있습니다(클라이언트에서 더 이상 사용하지 않는 필드). 

따라서, 백엔드에 영향을 주지 않고 클라이언트에서 변경할 수 있습니다. 간단히 말해서 버전 관리 및 주요 변경 사항 없이 진화하는 API를 작성할 수 있습니다.

REST는 기본 제공 HTTP 사양을 사용하여 캐싱을 제공합니다. 그러나 GraphQL은 HTTP 사양을 따르지 않으며 캐싱을 위해 `Apollo/Relay`와 같은 실시간러리를 사용합니다. 그러나 REST는 HTTP를 기반으로 하며 구현 사양을 따르지 않으므로 REST와 gRPC를 비교하면서 논의한 것처럼 구현이 일관되지 않을 수 있습니다. HTTP GET 메서드를 사용하여 리소스를 삭제할 수 있습니다.

GraphQL은 모바일 클라이언트에서의 사용 측면에서 REST API보다 우수합니다. GraphQL API의 기능은 또한 강력한 타입을 사용하여 정의됩니다. 이러한 타입은 API 정의가 포함된 스키마의 일부입니다. 이러한 타입은 SDL을 사용하여 스키마에 작성됩니다.

GraphQL은 서버와 클라이언트 간의 계약 역할을 합니다. GraphQL 스키마를 gRPC IDL(인터페이스 정의 언어) 파일 및 OpenAPI 사양 파일과 연관시킬 수 있습니다.

## GraphQL 기초 배우기

GraphQL API에는 query, mutation 및 subscription과 같은 중요 타입이 있습니다. 이들은 모두 특별한 SDL 구문을 사용하여 GraphQL 스키마에 정의되어 있습니다.

GraphQL은 query, mutation 또는 subscription이 될 수 있는 요청을 기반으로 JSON 응답을 리턴하는 단일 엔드포인트를 제공합니다.


### query형

쿼리 타입은 서버에서 정보를 가져오는 작업을 읽는 데 사용됩니다. 단일 쿼리 타입에는 많은 쿼리가 포함될 수 있습니다. 
다음 스키마와 같이 로그인한 사용자를 검색하기 위해 SDL을 사용하여 쿼리를 작성해 보겠습니다.

```graphql
type Query {
   me: LogginInUser
  # You can add other queries here
}

type LoggedInUser {
  id: ID
  accessToken: String
  refreshToken: String
  username: String
}
```
여기에서 두 가지 작업을 수행했습니다.

1. 실행할 쿼리가 포함된 루트 query를 정의했습니다. 여기에는 LoggedInUser 타입의 인스턴스를 리턴하는 me 쿼리만 포함됩니다.

2. LoggedInUser 타입은 사용자 정의 타입이며 ID와 String 타입을 사용하여 필드를 정의했습니다. 
서버에서 이 스키마를 구현하고 다음 쿼리를 실행하면 응답으로 요청한 필드 값만 들어있는 JSON 객체를 리턴합니다.

```graphql
# Request input
{
  me {
    id
    username
  }
}
```

```json
#JSON response
{
  "data": {
    "me": {
      "id": "asdf90asdkqwe09kl",
      "username": "scott"
    }
  }
}
```
여기서 request 입력이 디폴트이므로 query로 시작하지 않습니다. 이를 익명 쿼리라고 합니다. 
그러나 원하는 경우 다음과 같이 request 입력에 query 접두사를 붙일 수도 있습니다.

```graphql
query {
  me {
    id
    username
  }
}
```
보시다시피, 필요한 필드만 쿼리할 수 있습니다. 여기서는 LoggedInUser 타입에서 id, username 필드만 요청했고 서버는 이 두 필드만으로 응답했습니다. 
요청 페이로드는 중괄호 {}로 묶여 있습니다.

### Mutation형

Mutation형은 추가, 업데이트 또는 삭제 요청에 사용됩니다. 한 Mutation 타입에 또 다른 Mutation이 포함될 수 있습니다. 

장바구니에 새 항목을 추가하는 addItemInCart를 정의합니다.

```graphql
type Mutation {
  addItemInCart(productId: ID, qty: Int): [Item]
  # You can add other mutation here
}

type Item {
  id: ID!
  productId: ID
  qty: Int
}
```

여기에서 Mutation 타입과 Item 타입을 정의했습니다. addItemInCart라는 Mutation을 정의 합니다. 
Query, Mutation 및 Subscription 타입은 매개변수를 전달할 수 있습니다. 
addItemInCart의 시그니처는 두 개의 매개변수와 카트 Item 목록을 리턴합니다. 

*OPTIONAL AND REQUIRED ARGUMENTS*

다음과 같이 기본값을 사용하여 매개변수를 선언한다고 가정해 보겠습니다.

```graphql
pay(amount: Float, currency: String = "USD"): Payment
```

여기서 `currency`는 선택적 매개변수입니다. 여기에는 기본값이 포함되어 있지만 `amount`에는 기본값이 포함되어 있지 않으므로 필수 필드입니다.

Int는 정수를 나타내는 스칼라 타입입니다. 기본값은 null입니다. 필드에 대해 nullable이 아닌 값을 강제 적용하려면 해당 타입에 느낌표(!)를 표시해야 합니다. 스키마의 필드에 적용되면 GraphQL 서버는 클라이언트가 요청 페이로드에 배치할 때 해당 필드에 대해 null 대신 항상 값을 제공합니다. 느낌표가 있는 목록을 선언할 수도 있습니다. 예를 들어 항목: `[item]!` 및 `item: [item!]!`. 두 선언 모두 목록에 0개 이상의 항목을 제공합니다. 그러나 후자는 유효한 Item 객체를 제공합니다.

서버에 이 스키마 구현이 있으면 다음의 query를 사용할 수 있습니다. 요청한 필드만 해당 값과 함께 JSON 객체로 가져옵니다.

```graphql
# Request input
mutation {
  addItemInCart(productId: "qwer90asdkqwe09kl", qty: 2) {
    id
    productId
  }
}
```
이번에는 request 입력이 mutation 키워드로 시작하는 것을 볼 수 있습니다. mutation 키워드로 mutation을 시작하지 않으면 오류가 발생할 수 있으며 필드 'addItemInCart'가 'Query' 타입에 존재하지 않습니다. 이는 서버가 요청 페이로드를 쿼리로 처리하기 때문입니다.

여기에서 addItemInCart 변형에 필요한 매개변수를 추가한 다음 응답으로 검색하려는 필드(id, productId)를 추가해야 합니다. 요청이 성공적으로 처리되면 다음과 유사한 JSON 출력이 표시됩니다.

```json
#JSON response
{
  "data": {
    addItemInCart: [
      {
        "id": "zxcv90asdkqwe09kl",
        "productId": "qwer90asdkqwe09kl"
      }
    ]
  }
}
```
여기서 id 필드의 값은 서버에서 생성됩니다. 마찬가지로 스키마에서 삭제 및 업데이트와 같은 mutation을 작성할 수 있습니다. 
그런 다음 request의 페이로드를 사용하여 그에 따라 mutation을 처리할 수 있습니다.

### Subscription 타입

REST에만 익숙하다면 구독 개념이 생소할 것입니다. 
GraphQL이 없으면 폴링 또는 WebSocket을 사용하여 유사한 기능을 구현할 수 있습니다. 
다음을 포함하여 구독 기능이 필요한 많은 사용 사례가 있습니다.

- 실시간 점수 또는 선거 결과 업데이트
- 일괄 처리 업데이트

이벤트를 즉시 업데이트해야 하는 경우에 구독 기능을 사용합니다. 
이러한 경우 클라이언트는 안정적인 연결을 시작하고 유지하여 이벤트를 구독합니다. 
구독 이벤트가 발생하면 서버는 결과 이벤트 데이터를 클라이언트에 푸시합니다. 
이 결과 데이터는 요청/응답 통신이 아닌 시작된 연결을 통해 스트림으로 전송됩니다.

*추천하는 방식*

큰 객체에 대해 소규모 업데이트(예: 일괄 처리)가 발생하거나 실시간 점수 업데이트와 같이 대기 시간이 짧은 실시간 업데이트가 있는 경우에만 구독을 사용하는 것이 좋습니다. 
그렇지 않으면 폴링(지정된 간격으로 주기적으로 쿼리 실행)을 사용해야 합니다.

다음과 같이 스키마에서 구독을 생성해 보겠습니다.

```graphql
type Subscription {
  orderShipped(customerID: ID!): Order
  # You can add other subscription here
}

# Order 타입은 주문 정보와 Shipping 객체가 들어있다
# Shipping은 id, estDeliveryDate, carrier 필드가 있다

type Order {
  # other fields omitted for brevity
  shipping: Shipping
}

type Shipping {
  Id: ID!
  estDeliveryDate: String
  carrier: String
}
```
여기에서 고객 ID를 매개변수로 받아들이고 Order를 리턴하는 orderShipped 구독을 정의했습니다. 
클라이언트가 이 이벤트를 구독하면 지정된 customerId에 대한 주문이 선적될 때마다 서버가 스트림을 사용하여 요청된 주문 세부 정보를 클라이언트에 푸시합니다.

다음 GraphQL 요청을 사용하여 GraphQL 구독을 구독할 수 있습니다.

```graphql
# Request Input
subscription {
  orderShipped(customerID: "customer90asdkqwe09kl") {
    shipping {
      estDeliveryDate
      trackingId
    }
  }
}
```

```json
# JSON Output
{
  "data": {
    "orderShipped": {
      "estDeliveryDate": "13-Aug-2022",
      "trackingId": "tracking90asdkqwe09kl"
    }
  }
}
```
클라이언트는 지정된 고객에 속하는 주문이 배송될 때마다 JSON 응답을 요청합니다. 서버는 이 GraphQL 구독을 구독한 모든 클라이언트에게 이러한 업데이트를 푸시합니다.

이제 루트 타입에서 작업 매개변수를 정의하고 request를 보내는 동안 매개변수를 전달하는 방법을 알게 되었습니다. 
스키마의 nullable이 아닌 필드는 느낌표(!)로 표시할 수 있습니다. 배열 또는 개체 목록의 경우 대괄호([])를 사용해야 합니다.



## GraphQL 스키마 디자인

스키마는 DSL 구문을 사용하여 작성된 GraphQL 파일입니다. 
기본적으로 루트 타입(query, mutation 및 subscription)과 객체 타입, 스칼라 타입, 인터페이스, 통합 타입, 입력 타입 및 프래그먼트와 같은 루트 타입에 사용되는 각 타입이 포함됩니다.

### 스칼라 타입 이해

스칼라 타입은 구체적인 데이터를 확인합니다. 스칼라 타입에는 내장 타입, 사용자 정의 타입 및 열거 타입의 세 가지 타입이 있습니다. 

다음은 내장 스칼라 타입입니다.

- Int: 32비트 정수
- Float: 부동 소수점 값
- String: UTF-8 문자열
- Bool: true/false
- ID: 개체 식별자 문자열. 문자열로만 직렬화할 수 있으며 사람이 읽을 수 없습니다.

사용자 정의 스칼라 타입이라고 하는 고유한 스칼라 타입을 정의할 수도 있습니다. 
여기에는 날짜와 같은 타입이 포함됩니다.

Date 사용자 정의 스칼라 타입은 다음과 같이 정의할 수 있습니다.
```
scala Date
```
이러한 사용자 지정 스칼라 타입의 직렬화, 역직렬화 및 유효성 검사를 결정하는 구현을 작성해야 합니다. 
예를 들어 날짜는 Unix 타임스탬프 또는 사용자 지정 스칼라 Date 타입 케이스의 특정 데이터 형식을 가진 문자열로 처리될 수 있습니다.

열거형 스칼라 타입은 허용되는 값의 특정 세트를 정의하는 데 사용됩니다. 
다음과 같이 주문 상태 열거를 정의해 보겠습니다.

```graphql
enum OrderStatus {
  CREATED
  CONFIRMED
  SHIPPED
  DELIVERED
  CANCELLED
}
```
여기서 OrderStatus 열거형은 주어진 시점의 주문 상태를 나타냅니다. 

### 프래그먼트 이해하기

클라이언트 측에서 쿼리하는 동안 충돌하는 시나리오가 발생할 수 있습니다. 
동일한 결과(동일한 개체 또는 필드 집합)를 리턴하는 두 개 이상의 쿼리가 있을 수 있습니다. 
이 충돌을 피하기 위해 쿼리 결과에 별칭을 지정할 수 있습니다.

```graphql
query HomeAndBillingAddress {
  home: getAddress(type: "home") {
    number
    residency
    street
    city
    pincode
  }

  billing: getAddress(type: "home") {
    number
    residency
    street
    city
    pincode
  }
}
```

여기서 `HomeAndBillingAddress`는 `getAddress` 쿼리 작업을 포함하는 명명된 쿼리입니다. 
`getAddress`가 두 번 사용되어 동일한 필드 집합을 리턴합니다. 
따라서 결과 개체를 구별하기 위해 `home` 및 `billing`이라는 별칭이 사용됩니다.

`getAddress` 쿼리는 `Address` 개체를 리턴할 수 있습니다. 
주소 객체에는 타입, 주, 국가 및 연락처와 같은 추가 필드가 있을 수 있습니다. 
동일한 필드 집합을 사용하는 쿼리가 있는 경우 프래그먼트를 만들어 쿼리에서 사용할 수 있습니다.

```graphql
query HomeAndBillingAddress {
  home: getAddress(type: "home") {
    ...addressFragment
  }
  billing: getAddress(type: "home") {
    ...addressFragment
  }
}

fragment addressFragment on Address {
    number
    residency
    street
    city
    pincode
}
```
여기에서 addressFragment가 생성되어 쿼리에 사용되었습니다.

쿼리에서 인라인 프래그먼트를 만들 수도 있습니다. 
쿼리 필드가 인터페이스 또는 union 타입을 리턴할 때 인라인 프래그먼트을 사용할 수 있습니다. 
인라인 프래그먼트에 대해서는 나중에 더 자세히 살펴보겠습니다.

### interface 이해하기

GraphQL 인터페이스는 추상적입니다. 여러 개체에서 공통적인 몇 가지 필드가 있을 수 있습니다. 
이러한 공통 필드 세트에 대한 인터페이스 타입을 작성할 수 있습니다. 
예를 들어 제품에는 ID, name, description과 같은 공통 속성이 있을 수 있습니다. 
제품은 타입에 따라 다른 속성을 가질 수도 있습니다. 
예를 들어 책에는 여러 페이지, 저자 및 출판사가 있을 수 있지만 책장은 재질, 너비, 높이 및 깊이 속성이 있을 수 있습니다.

인터페이스를 사용하여 이 세 가지를 정의해 보겠습니다.

```graphql
interface Product {
  id: ID!
  name: String!
  description: string
}

type Book implements Product {
  id: ID!
  name: String!
  description: string
  author: String!
  publisher: String
  noOfPages: Int
}

type Bookcase implements Product {
  id: ID!
  name: String!
  description: string
  material: [String!]!
  width: Int
  height: Int
  depth: Int
}
```
여기서는 interface 키워드를 사용하여 Product라는 추상 타입을 생성했습니다. 
이 인터페이스는 Book과 Bookcase라는 객체 타입을 생성하고자 할 때 구현할 수 있습니다.

이제 모든 제품(Book과 Bookcase)을 리턴하는 쿼리를 간단하게 작성할 수 있습니다.

```graphql
type query {
  allProducts: [Product]
}
```
이제 클라이언트 측에서 다음 쿼리를 사용하여 모든 제품을 검색할 수 있습니다.

```graphql
query getProducts {
  allProducts {
    id
    name
    description
  }
}
```

앞의 코드에는 Product 인터페이스의 속성만 포함되어 있다는 것을 눈치채셨을 것입니다. 
Book 및 Bookcase에서 속성을 검색하려면 다음과 같이 인라인 프래그먼트를 사용해야 합니다.

```graphql
query getProducts {
  allProducts {
    id
    name
    description
    ... on Book {
      author
      publisher
    }

    ... on BookCase {
      material
      height
    }
  }
}
```

여기서`…` 연산은 인라인 프래그먼트를 만드는 데 사용됩니다. 인터페이스를 구현하는 타입에서 필드를 가져올 수 있습니다.

### Union 타입

Book과 Author의 두 가지 객체 타입이 있다고 가정해 보겠습니다. 
여기에서 책과 저자를 모두 리턴할 수 있는 쿼리를 작성하려고 합니다. 
인터페이스가 없다는 점에 유의하십시오. 그렇다면 쿼리 결과에서 둘 이상의 개체를 조합한 Union 타입을 사용할 수 있습니다.

Union 타입을 생성하기 전에 다음 사항을 고려하십시오.

- 공통 필드가 필요하지 않습니다.
- Union 구성원은 구체적인 타입이어야 합니다. 따라서 공용체, 인터페이스, 입력 또는 스칼라 타입을 사용할 수 없습니다.

다음 코드 같이 Book과 Boocase와 같이 Union 타입에 포함된 모든 개체를 리턴할 수 있는 타입을 만들어 보겠습니다.

```graphql
union SearchResult = Book | Author

type Book {
  id: ID!
  name: String!
  publisher: String
}

type Author {
  id: ID!
  name: String!
}

type Query {
  search(text: String): [SearchResult]
}
```
여기에서 Union 키워드는 Book 및 Author 개체에 대한 공용체 타입을 만드는 데 사용됩니다. 
파이프 기호(|)는 포함된 개체를 구분하는 데 사용됩니다. 마지막에 쿼리가 정의되어 주어진 텍스트가 포함된 Book 또는 Author 컬렉션을 리턴합니다.

이제 다음과 같이 클라이언트에 대해 이 쿼리를 작성해 보겠습니다.

```graphql
# Request Input
{
  search(text: "Malcolm Gladwell") {
    __typename
    ... on Book {
      name
      publisher
    }
    ... on Author {
      name
    }
  }
}
```

JSON 응답

```json
{
  "data": {
    "search": [
      {
        "__typename": "Book",
        "name": "Blink",
        "publisher": "Back Bay Books"
      },
      {
        "__typename": "Author",
        "name": " Malcolm Gladwell ",
      }
    ]
  }
}
```
보시다시피 쿼리에서 인라인 프래그먼트가 사용되고 있습니다. 
__typename 필드는 해당 객체가 속한 객체를 참조하고 클라이언트의 여러 개체를 구별하는 데 도움이 됩니다.


### input 타입

지금까지 스칼라 타입을 매개변수로 사용했습니다. mutation의 매개변수로 객체 타입을 전달할 수도 있습니다. 
유일한 차이점은 `type` 대신 `input`으로 선언해야 한다는 것입니다.

`input` 타입을 매개변수로 받아들이는 `mutation`을 만들어 보겠습니다.

```graphql
type Mutation {
  addProduct(prodInput: ProductInput): Product
}

input ProductInput {
  name: String!
  description: String
  price: Float!
  # other fields…
}

type Product {
  # Product Input fields. Truncated for brevity.
}
```
여기에서 addProduct는 ProductInput을 매개변수로 받아들이고 Product를 리턴합니다.

이제 다음 요청을 사용하여 클라이언트에 제품을 추가합니다.

```graphql
# Request Input

mutation AddProduct ($input: ProductInput) {
  addProduct(prodInput: $input) {
    name
  }
}

#---- Variable Section ----
{
  "input": {
    name: "Blink",
    description: "a book",
    "price": 10.00
  }
}
```

```json
# JSON Output
{
  "data": {
    addProduct {
      "name": "Blink"
    }
  }
}
```
여기에서는 input 타입을 사용하는 mutation을 실행하고 있습니다. 여기에서 ProductInput을 전달하기 위해 Variable이 사용됩니다. 
이름이 부여된 mutation이 변수에 사용 중입니다. 변수가 해당 타입과 함께 mutation에 정의된 경우 해당 변수를 mutation에 사용해야 합니다.

변수 값은 변수 절에서(또는 클라이언트에서 미리) 할당되어야 합니다. 변수의 입력 값은 ProductInput에 매핑되어야 하는 JSON 개체를 사용하여 할당됩니다.

다음 하위 절에서 GraphQL 스키마를 설계하는 동안 사용할 수 있는 도구를 살펴보겠습니다.

### 스키마 설계에 도움이 되는 도구

다음 도구를 사용하여 GraphQL을 디자인하고 작업할 수 있습니다. 각각 고유한 제품이 있습니다.

- GraphiQL: 웹 기반 GraphQL 통합 개발 환경(IDE)을 제공하는 공식 GraphQL Foundation 프로젝트입니다. 
소스 코드 편집기와 IDE 간에 JSON-RPC 기반 프로토콜을 사용하는 LSP(Language Server Protocol)를 사용합니다. https://github.com/graphql/graphiql에서 사용할 수 있습니다.

- GraphQL Playground: GraphiQL보다 더 나은 기능을 제공하는 GraphQL IDE이기도 합니다. https://github.com/graphql/graphql-playground에서 사용할 수 있습니다.

- GraphQL Faker: GraphQL API에 대한 모의 데이터를 제공합니다. https://github.com/APIs-guru/graphql-faker에서 사용할 수 있습니다.

- GraphQL 편집기: 이를 통해 스키마를 시각적으로 디자인한 다음 코드로 변환할 수 있습니다. https://github.com/graphql-editor/graphql-editor에서 사용할 수 있습니다.

- GraphQL Voyager: 스키마를 엔터티 다이어그램 및 모든 관계와 같은 대화형 그래프로 변환합니다. https://github.com/APIs-guru/graphql-voyager에서 사용할 수 있습니다.

In the next section, you'll test the knowledge that you have acquired throughout this chapter.


## GraphQL 쿼리 및 mutation 테스트

실제 GraphQL 스키마에서 query와 mutation을 작성해 보겠습니다.
이 절에서는 GitHub의 GraphQL API 탐색기를 사용할 것입니다.

1. 먼저 https://docs.github.com/en/graphql/overview/explorer 로 이동합니다.

2. GitHub 계정을 사용하여 권한을 부여해야 GraphQL 쿼리를 실행할 수 있습니다.

3. GitHub Explorer는 GraphiQL을 기반으로 합니다. 세 개의 수직 절(왼쪽에서 오른쪽으로)으로 나뉩니다.

    a. 쿼리 작성을 위한 상단 절과 변수 정의를 위한 하단 절의 두 가지 하위 절이 있습니다.

    b.. 중간 수직 절은 응답을 보여줍니다.

    c. 일반적으로 맨 오른쪽 절은 숨겨져 있습니다. 문서 링크를 클릭하여 표시합니다. 탐색할 수 있는 루트 타입과 함께 해당 문서 및 스키마가 표시됩니다.

4. 별표로 표시하려는 저장소의 ID를 찾기 위해 다음 쿼리를 실행해 보겠습니다.

```graphql
  query {
    repository (name:
        "Modern-API-Development-with-Spring-and-Spring-Boot",
        owner: "PacktPublishing") {
      id
      owner {
        id
        login
      }
      name
      description
      viewerHasStarred
      stargazerCount
    }
  }
```
    여기에서 repository 이름과 owner라는 두 가지 매개변수를 제공하여 이 책의 저장소를 쿼리합니다. 여기에서 몇 개의 필드를 가져오고 있습니다. addStar mutation를 수행할 것이기 때문에 가장 중요한 것 중 하나는 stargazerCount입니다. 이 개수는 mutation의 성공 여부를 알려줍니다.

5. 상단 표시줄에서 쿼리 실행 버튼을 클릭하거나 Ctrl + Enter를 눌러 쿼리를 실행합니다. 이 쿼리가 성공적으로 실행되면 다음 출력을 얻을 수 있습니다.

```json
{
  "data": {
    "repository": {
      "id": "MDEwOlJlcG9zaXRvcnkyOTMyOTU5NDA=",
      "owner": {
        "id": "MDEyOk9yZ2FuaXphdGlvbjEwOTc0OTA2",
        "login": "PacktPublishing"
      },

      "name": "Modern-API-Development-with-Spring-and-Spring-Boot",
      "description": "Modern API Development with Spring and Spring Boot, published by Packt",
      "viewerHasStarred": false,
      "stargazerCount": 1
    }
  }
}
```
    여기에서 시작을 표시하는 데 필요하기 때문에 응답에서 id 값을 복사해야 합니다.

6. 다음 쿼리를 실행하여 addStar 변형을 수행합니다.

```graphql 
  mutation {
    addStar(input: {
      starrableId: "MDEwOlJlcG9zaXRvcnkyOTMyOTU5NDA="
    }) {
      clientMutationId
    }
  }
```

  이것은 주어진 저장소 ID에 대해 addStar 변형을 수행합니다.

7. 이전 쿼리가 성공적으로 실행되면 4단계의 쿼리를 다시 실행하여 변경 사항을 확인해야 합니다. 액세스 문제가 발생하면 고유한 GitHub 리포지토리를 선택하여 이러한 단계를 수행할 수 있습니다. 다른 쿼리 및 변형을 탐색하여 GraphQL에 대해 자세히 알아볼 수도 있습니다.


## Solving the N+1 problem

N+1 문제는 Java 개발자에게 새로운 것이 아닙니다. 
쿼리를 최적화하지 않거나 엔터티를 올바르게 작성하지 않으면 최대 절전 모드에서 이 문제가 발생했을 수 있습니다.


### N+1 문제 이해하기

N+1 문제는 일반적으로 연관이 관련될 때 발생합니다. 
고객과 주문 사이에는 일대다 관계가 있습니다. 한 고객이 여러 주문을 할 수 있습니다. 
모든 고객과 주문을 찾아야 하는 경우 다음을 수행할 수 있습니다.

1. 모든 사용자를 찾습니다.

2. 관계를 설정하여 첫 번째 단계에서 수신한 사용자 ID를 기반으로 사용자의 모든 주문을 찾습니다.

여기에서 두 개의 쿼리를 실행합니다. 구현을 더 최적화하면 이 두 엔터티 사이에 조인트를 배치하고 단일 쿼리에서 모든 레코드를 수신할 수 있습니다.
이것이 그렇게 간단하다면 왜 GraphQL에서 N+1 문제가 발생합니까? 이 질문에 답하려면 리졸버 기능을 이해해야 합니다.

4장, API를 위한 비즈니스 로직 작성에서 생성한 데이터베이스 스키마를 살펴보면 getUsersOrders 쿼리가 다음 SQL 문이 실행되도록 할 것이라고 말할 수 있습니다.

```sql
SELECT * FROM ecomm.user;
SELECT * FROM ecomm.orders WHERE customer_id in (1);
SELECT * FROM ecomm.orders WHERE customer_id in (2);
...
...
SELECT * FROM ecomm.orders WHERE customer_id in (n);
```
여기에서는 모든 사용자를 가져오기 위해 사용자에 대한 쿼리를 실행하고 있습니다. 그런 다음 주문에 대해 N개의 쿼리를 실행합니다. 이것이 N+1 문제라고 불리는 이유입니다. 이상적으로는 단일 쿼리를 실행하거나 최악의 경우 두 개의 쿼리를 실행해야 하므로 효율적이지 않습니다.

GraphQL은 리졸버로 인해 쿼리에서 요청한 필드 값으로만 응답할 수 있습니다. 각 필드에는 해당 필드에 대한 데이터를 가져오는 GraphQL 서버 구현의 자체 리졸버 함수가 있습니다. 

다음 스키마가 있다고 가정해 보겠습니다.

```graphql
type Mutation {
  getUsersOrders: [User]
}

type User {
  name: String
  orders: [Order]
}

type Order {
  id: Int
  status: Status
}
```
여기에 사용자 컬렉션을 리턴하는 mutation이 있습니다. 각 사용자는 주문 모음을 가질 수 있습니다. 클라이언트에서 다음 쿼리를 사용할 수 있습니다.

```graphql
{
  getUsersOrders {
    name
    orders {
      id
      status
    }
  }
}
```
이 쿼리가 서버에서 어떻게 처리되는지 이해합시다. 서버에서 각 필드에는 해당 데이터를 가져오는 자체 해석기 기능이 있습니다.

첫 번째 확인자는 사용자를 위한 것이며 데이터 저장소에서 모든 사용자를 가져옵니다. 다음으로 리졸버는 각 사용자에 대해 주문됩니다. 주어진 사용자 ID를 기반으로 데이터 저장소에서 주문을 가져옵니다. 따라서 주문 해석기는 n번 실행됩니다. 여기서 n은 데이터 저장소에서 가져온 사용자 수입니다.

### N+1 문제에 대한 솔루션

모든 주문이 로드될 때까지 기다리는 솔루션이 필요합니다. 모든 사용자 ID가 검색되면 단일 데이터 저장소 호출에서 모든 주문을 가져오기 위해 데이터베이스 호출이 이루어져야 합니다. 데이터베이스의 크기가 큰 경우 배치를 사용할 수 있습니다. 그런 다음 개별 주문 확인자를 해결할 수 있습니다. 그러나 이것은 말보다 쉽습니다. GraphQL은 이 작업을 수행하는 DataLoader(https://github.com/graphql/dataloader)라는 라이브러리를 제공합니다.

Java는 이 문제를 해결하는 데 도움이 되는 java-dataloader(https://github.com/graphql-java/java-dataloader)라는 유사한 라이브러리를 제공합니다. 자세한 내용은 https://www.graphql-java.com/documentation/v16/batching/에서 확인할 수 있습니다.

## 요약

- 이 장에서는 GraphQL의 장점과 REST와 비교하는 방법에 대해 배웠습니다. GraphQL이 오버페칭 및 언더페칭 문제를 해결하는 방법을 배웠습니다. 그런 다음 GraphQL의 루트 형(쿼리, 변형 및 구독)과 다양한 블록이 GraphQL 스키마를 설계하는 데 어떻게 도움이 되는지 배웠습니다. 마지막으로 리졸버가 어떻게 작동하는지, 어떻게 N+1 문제로 이어질 수 있는지, 이 문제에 대한 솔루션을 이해했습니다.

- 이제 GraphQL의 기본 사항을 알았으므로 GraphQL 스키마 설계를 시작할 수 있습니다. 또한 GraphQL의 클라이언트 측 쿼리와 별칭, 조각 및 변수를 사용하여 일반적인 문제를 해결하는 방법에 대해서도 배웠습니다.

다음 장에서는 이 장에서 습득한 GraphQL 기술을 사용하여 GraphQL 서버를 구현합니다.

### Questions

- Is GraphQL better than REST? If yes, then in what way?
- When should you use fragments?
- How can you use variables in a GraphQL query?

## Further reading

- GraphQL specifications: https://spec.graphql.org/
- GraphQL documentation: https://graphql.org/learn/
- GraphQL and Apollo with Android from Novice to Expert (video): https://www.packtpub.com/product/graphql-and-apollo-with-android-from-novice-to-expert-video/9781800564626