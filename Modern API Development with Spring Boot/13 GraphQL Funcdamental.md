# Chapter 13: GraphQL Fundamentals

In this chapter, you will learn about the fundamentals of GraphQL, including its Schema Definition Language (SDL), queries, mutations, and subscriptions. This knowledge will help you in the next chapter, when you will implement an API based on GraphQL.

We will cover the following topics in this chapter:

- Introducing GraphQL
- Learning about the fundamentals of GraphQL
- Designing a GraphQL schema
- Testing GraphQL query and mutation
- Solving the N+1 problem

After completing this chapter, you will know about the basics of GraphQL, including its semantics, schema design, and everything you need to develop a GraphQL-based API using Spring and Spring Boot.

## Technical requirements
This chapter covers the theory surrounding GraphQL. However, you need the following for developing and testing the GraphQL-based service code presented in the next chapter:

- IntelliJ, or Eclipse
- JDK 15

## Introducing GraphQL

You might have heard of or be aware of GraphQL, which has become more popular and is the preferred way of implementing APIs for handheld devices and the web.

GraphQL is a declarative query and manipulation language, and a server-side runtime for APIs. GraphQL empowers the client to query exactly the data they want – no more, no less.

We'll discuss its brief history in the next subsection.

### GraphQL의 간략한 역사

2011년 Facebook은 모바일 브라우저에서 웹사이트의 성능을 개선하는 문제에 직면했습니다. 그들은 모바일 네이티브 기술로 자체 모바일 앱을 구축하기 시작했습니다. 그러나 API는 계층적 및 재귀적 데이터로 인해 표시에 미치지 못했습니다. 그들은 네트워크 호출을 최적화하기를 원했습니다. 그 당시 모바일 네트워크 속도는 세계의 일부 지역에서 Kb/s였습니다. 소비자가 모바일 장치로 이동하기 시작했기 때문에 고품질의 빠른 모바일 앱을 보유하는 것이 성공의 열쇠였습니다.

2012년에는 Facebook의 몇 명의 엔지니어가 협력하여 GraphQL을 만들었습니다. 처음에는 Facebook의 뉴스피드 기능을 설계하고 개발하는 데 사용되었지만 나중에는 인프라 전반에 걸쳐 사용되었습니다. 2015년에 오픈 소스로 공개되기 전에 Facebook에서 내부적으로 사용 중이었습니다. 이 때 GraphQL 사양과 JavaScript 구현을 대중에게 공개했습니다. 곧 Java를 포함하여 GraphQL 사양의 다른 언어 구현이 롤아웃되기 시작했습니다.

https://www.youtube.com/watch?v=783ccP__No8에서 이 GraphQL 다큐멘터리를 보는 것을 좋아할 거라고 생각합니다. 이 다큐멘터리는 내부 Facebook 도구에서 현재의 성공까지 GraphQL의 여정을 안내합니다.

알고 계셨나요?

Netflix와 Coursera도 효율적이고 성능이 뛰어난 API를 구축하기 위해 유사한 아이디어를 연구하고 있었습니다. Coursera는 앞으로 나아가지 않았지만 Netflix는 Falcor를 오픈 소스로 제공했습니다.

### Comparing GraphQL with REST

이 책의 섹션 1에서 REST를 사용하여 API를 개발했습니다. 실제로 샘플 전자 상거래 UI 앱도 REST API를 사용하여 전자 상거래 기능을 구현했습니다. 필요한 GraphQL 개념이 어디에 적용되는지 이해할 수 있도록 이 장에서 REST를 계속 참조할 것입니다. 이 상관 관계는 GraphQL 개념을 쉽게 이해하는 데 도움이 됩니다.

**GraphQL은 REST보다 강력하고 유연하며 효율적입니다**. 이유를 이해합시다.

사용자가 샘플 전자 상거래 UI 앱에 로그인하고 자동으로 제품 목록 페이지로 이동하면 UI 앱은 다음과 같이 세 가지 다른 끝점을 사용합니다.

- 사용자 엔드포인트, 사용자 정보 가져오기
- 제품 엔드포인트, 제품 목록 가져오기
- 장바구니 엔드포인트, 사용자 장바구니에서 장바구니 항목 가져오기

따라서 기본적으로 백엔드에서 고정된 구조로 필요한 정보를 가져오려면 세 번 호출해야 합니다(응답으로 전송되는 필드는 변경할 수 없음).

반면에 GraphQL은 사용자 정보, 사용자 장바구니 데이터 및 제품 목록을 단일 호출로 가져올 수 있습니다. 이것은 네트워크 호출을 3개에서 1개로 줄입니다. GraphQL은 각 사용 사례에 대해 끝점을 정의해야 하는 REST와 달리 단일 끝점만 노출합니다. 이를 수행하는 새로운 REST 엔드포인트를 작성할 수 있다고 말할 수 있습니다. 예, 이렇게 하면 이 특정 사용 사례를 해결할 수 있지만 유연하지 않습니다. 빠른 변경 반복을 허용하지 않습니다.

또한 GraphQL을 사용하면 요청의 백엔드에서 가져오려는 필드를 표현할 수 있습니다. 서버는 요청된 필드에 따라 응답을 제공합니다. 더도 말고 덜도 말고요.

새 필드 집합이 필요한 경우 새 REST 끝점을 만들 필요가 없습니다. 예를 들어 제품에 사용자 리뷰를 추가할 수 있습니다. 이를 위해 GraphQL 쿼리에 리뷰 필드를 추가하기만 하면 됩니다. 마찬가지로 추가 필드를 사용할 필요가 없습니다. GraphQL 쿼리에 필요한 필드를 추가하기만 하면 됩니다. 대신 REST의 응답에는 응답 개체에 특정 필드가 필요한지 여부에 관계없이 미리 정의된 필드가 포함됩니다. 그런 다음 클라이언트 측에서 필수 필드를 필터링해야 합니다. 따라서 GraphQL은 over/underfetching 문제를 피함으로써 네트워크 대역폭을 효과적으로 사용한다고 말할 수 있습니다.

GraphQL API는 REST처럼 지속적인 변경이 필요하지 않습니다. 요구 사항 변경을 위해 API를 변경하거나 새 API를 추가해야 할 수 있습니다. 이것은 개발 속도와 반복을 향상시킵니다. 새 필드를 쉽게 추가하거나 더 이상 사용되지 않는 기존 필드를 표시할 수 있습니다(클라이언트에서 더 이상 사용하지 않는 필드). 따라서 백엔드에 영향을 주지 않고 클라이언트에서 변경할 수 있습니다. 간단히 말해서 버전 관리 및 주요 변경 사항 없이 진화하는 API를 작성할 수 있습니다.

REST는 기본 제공 HTTP 사양을 사용하여 캐싱을 제공합니다. 그러나 GraphQL은 HTTP 사양을 따르지 않습니다. 대신 캐싱을 위해 Apollo/Relay와 같은 라이브러리를 사용합니다. 그러나 REST는 HTTP를 기반으로 하며 구현 사양을 따르지 않으므로 REST와 gRPC를 비교하면서 논의한 것처럼 구현이 일관되지 않을 수 있습니다. HTTP GET 메서드를 사용하여 리소스를 삭제할 수 있습니다.

GraphQL은 모바일 클라이언트에서의 사용 측면에서 REST API보다 우수합니다. GraphQL API의 기능은 또한 강력한 유형을 사용하여 정의됩니다. 이러한 유형은 API 정의가 포함된 스키마의 일부입니다. 이러한 유형은 SDL을 사용하여 스키마에 작성됩니다.

GraphQL은 서버와 클라이언트 간의 계약 역할을 합니다. GraphQL 스키마를 gRPC IDL(인터페이스 정의 언어) 파일 및 OpenAPI 사양 파일과 연관시킬 수 있습니다.

다음 섹션에서 GraphQL의 기본 사항에 대해 논의할 것입니다.

## GraphQL의 기초에 대해 배우기

GraphQL API에는 쿼리, 변형 및 구독의 세 가지 중요한 루트 유형이 있습니다. 이들은 모두 특별한 SDL 구문을 사용하여 GraphQL 스키마에 정의되어 있습니다.

GraphQL은 쿼리, 변형 또는 구독이 될 수 있는 요청을 기반으로 JSON 응답을 반환하는 단일 엔드포인트를 제공합니다.

먼저 쿼리를 이해합시다.

### 쿼리 유형 탐색

쿼리 유형은 서버에서 정보를 가져오는 작업을 읽는 데 사용됩니다. 단일 쿼리 유형에는 많은 쿼리가 포함될 수 있습니다. 다음 GraphQL 스키마와 같이 로그인한 사용자를 검색하기 위해 SDL을 사용하여 쿼리를 작성해 보겠습니다.

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

1. 실행할 수 있는 쿼리가 포함된 GraphQL 인터페이스의 쿼리 루트를 정의했습니다. 여기에는 LoggedInUser 유형의 인스턴스를 반환하는 단일 쿼리 유형인 me만 포함됩니다.

2. 네 개의 필드가 포함된 사용자 정의 LoggedInUser 개체 유형을 정의했습니다. 이러한 필드 뒤에는 해당 유형이 옵니다. 여기에서 ID 및 String이라고 하는 GraphQL의 내장 스칼라 유형을 사용하여 필드 유형을 정의했습니다. 내장 스칼라 유형에 대해 자세히 논의할 때 이 장의 뒷부분에서 이러한 유형에 대해 논의할 것입니다.

서버에서 이 스키마를 구현하고 다음 GraphQL 쿼리를 실행하면 요청한 필드만 값과 함께 응답으로 JSON 객체로 가져옵니다.

다음 코드 블록에서 me 쿼리와 해당 JSON 응답을 찾을 수 있습니다.
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
여기서 GraphQL의 요청 입력은 기본값이기 때문에 query로 시작하지 않습니다. 이를 익명 쿼리라고 합니다. 그러나 원하는 경우 다음과 같이 요청 입력에 쿼리 접두사를 붙일 수도 있습니다.

```graphql
query {
  me {
    id
    username
  }
}
```
보시다시피, 필요한 필드만 쿼리할 수 있습니다. 여기서는 LoggedInUser 유형에서 id 및 사용자 이름 필드만 요청했고 서버는 이 두 필드만 사용하여 응답했습니다. 요청 페이로드는 중괄호 {}로 묶여 있습니다. 스키마에서 주석을 달 때 #을 사용할 수 있습니다.

이제 GraphQL 스키마에서 쿼리 및 개체 유형을 정의하는 방법을 알게 되었습니다. 또한 쿼리 유형과 예상 JSON 응답에 따라 GraphQL 요청 페이로드를 구성하는 방법을 배웠습니다.

다음에서 GraphQL 변형에 대해 알아볼 것입니다.

### Mutation 유형 탐색

Mutation 유형은 서버에서 수행되는 모든 추가, 업데이트 또는 삭제 작업에 대한 GraphQL 요청에 사용됩니다. 단일 Mutation 유형에는 많은 Mutation이 포함될 수 있습니다. 장바구니에 새 항목을 추가하는 addItemInCart를 정의해 보겠습니다.

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

여기에서 Mutation 유형과 Item이라는 새 객체 유형을 정의했습니다. Mutation이 추가되고 addItemInCart라고 합니다. Query, Mutation 및 Subscription 유형은 인수를 전달할 수 있습니다. 필요한 매개변수를 정의하기 위해 명명된 인수를 () 대괄호로 묶을 수 있습니다. 인수는 쉼표로 구분됩니다. addItemInCart의 서명은 두 개의 인수를 포함하고 장바구니 항목 목록을 반환합니다. 목록은 [] 괄호를 사용하여 표시됩니다.

OPTIONAL AND REQUIRED ARGUMENTS

Let's say you declare an argument with a default value, such as the following mutation:
```
pay(amount: Float, currency: String = "USD"): Payment
```
여기서 currency는 선택적 인수입니다. 여기에는 기본값이 포함되어 있지만 amount에는 기본값이 포함되어 있지 않으므로 필수 필드입니다.

Int는 부호 있는 32비트 정수에 대한 내장 스칼라 유형입니다. GraphQL에서 기본값은 null입니다. 필드에 대해 nullable이 아닌 값을 강제 적용하려면 해당 유형에 느낌표(!)를 표시해야 합니다. 스키마의 필드에 적용되면 GraphQL 서버는 클라이언트가 요청 페이로드에 배치할 때 해당 필드에 대해 null 대신 항상 값을 제공합니다. 느낌표가 있는 목록을 선언할 수도 있습니다. 예를 들어 항목: [항목]! 및 항목: [항목!]!. 두 선언 모두 목록에 0개 이상의 항목을 제공합니다. 그러나 후자는 유효한 Item 객체를 제공합니다.

서버에 이 스키마 구현이 있으면 다음 GraphQL 쿼리를 사용할 수 있습니다. 요청한 필드만 해당 값과 함께 JSON 객체로 가져옵니다.
```graphql
# Request input
mutation {
  addItemInCart(productId: "qwer90asdkqwe09kl", qty: 2) {
    id
    productId
  }
}
```
이번에는 GraphQL 요청 입력이 mutation 키워드로 시작하는 것을 볼 수 있습니다. mutation 키워드로 mutation을 시작하지 않으면 오류가 발생할 수 있으며 필드 ' addItemInCart'가 'Query' 유형에 존재하지 않습니다. 이는 서버가 요청 페이로드를 쿼리로 처리하기 때문입니다.

여기에서 addItemInCart 변형에 필요한 인수를 추가한 다음 응답으로 검색하려는 필드(id, productId)를 추가해야 합니다. 요청이 성공적으로 처리되면 다음과 유사한 JSON 출력이 표시됩니다.

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
Here, the value of the id field is generated by the server. Similarly, you can write other mutations, such as delete and update, in the schema. Then, you can use the payload in the GraphQL request to process the mutation accordingly.

We'll explore the GraphQL Subscription type in the next subsection.

### Exploring the Subscription type

REST에만 익숙하다면 구독 개념이 생소할 것입니다. GraphQL이 없으면 폴링 또는 WebSocket을 사용하여 유사한 기능을 구현할 수 있습니다. 다음을 포함하여 구독 기능이 필요한 많은 사용 사례가 있습니다.

- 라이브 스코어 업데이트 또는 선거 결과
- 일괄 처리 업데이트

이벤트를 즉시 업데이트해야 하는 경우가 많이 있습니다. GraphQL은 이 사용 사례에 대한 구독 기능을 제공합니다. 이러한 경우 클라이언트는 안정적인 연결을 시작하고 유지하여 이벤트를 구독합니다. 구독 이벤트가 발생하면 서버는 결과 이벤트 데이터를 클라이언트에 푸시합니다. 이 결과 데이터는 요청/응답 종류의 통신(쿼리/변이의 경우에 발생)이 아닌 시작된 연결을 통해 스트림으로 전송됩니다.

RECOMMENDED APPROACH

큰 개체에 대해 소규모 업데이트(예: 일괄 처리)가 발생하거나 라이브 점수 업데이트와 같이 대기 시간이 짧은 라이브 업데이트가 있는 경우에만 구독을 사용하는 것이 좋습니다. 그렇지 않으면 폴링(지정된 간격으로 주기적으로 쿼리 실행)을 사용해야 합니다.

다음과 같이 스키마에서 구독을 생성해 보겠습니다.

```graphql
type Subscription {
  orderShipped(customerID: ID!): Order
  # You can add other subscription here
}

# Order type contains order information and another object Shipping
# Shipping contains id and estDeliveryDate and carrier fields

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
여기에서 고객 ID를 인수로 받아들이고 Order를 반환하는 orderShipped 구독을 정의했습니다. 클라이언트가 이 이벤트를 구독하면 지정된 customerId에 대한 주문이 배송될 때마다 서버가 스트림을 사용하여 요청된 주문 세부 정보를 클라이언트에 푸시합니다.

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

이 섹션에서는 GraphQL 스키마에서 쿼리, 변이 및 구독 유형을 선언하는 방법을 배웠습니다.

스키마에 스칼라 유형과 사용자 정의 개체 유형을 정의했습니다. 또한 쿼리/변이 또는 구독에 대한 GraphQL 요청 입력을 작성하는 방법을 살펴보았습니다.

이제 루트 유형에서 작업 매개변수를 정의하고 GraphQL 요청을 보내는 동안 인수를 전달하는 방법을 알게 되었습니다. 스키마의 nullable이 아닌 필드는 느낌표(!)로 표시할 수 있습니다. 배열 또는 개체 목록의 경우 대괄호([])를 사용해야 합니다.

다음 섹션에서는 GraphQL 스키마에 대해 자세히 알아보겠습니다.

## Designing a GraphQL schema

스키마는 DSL 구문을 사용하여 작성된 GraphQL 파일입니다. 기본적으로 루트 유형(쿼리, 변형 및 구독)과 객체 유형, 스칼라 유형, 인터페이스, 통합 유형, 입력 유형 및 조각과 같은 루트 유형에 사용되는 각 유형이 포함됩니다.

먼저 이러한 유형에 대해 논의해 보겠습니다. 이전 섹션에서 루트 유형(쿼리, 변형 및 구독)과 객체 유형에 대해 배웠습니다. 이제 스칼라 유형에 대해 자세히 알아보겠습니다.

### 스칼라 유형 이해

스칼라 유형은 구체적인 데이터를 확인합니다. 스칼라 유형에는 내장 스칼라 유형, 사용자 정의 스칼라 유형 및 열거 유형의 세 가지 유형이 있습니다. 먼저 내장 스칼라 유형에 대해 논의합시다. GraphQL은 다음 5가지 종류의 내장 스칼라 유형을 제공합니다.

- Int: 부호 있는 32비트 정수
- Float: 부호 있는 배정밀도 부동 소수점 값
- String: UTF-8 문자 시퀀스
- Bool: true 또는 false
- ID: 개체 식별자 문자열을 정의. 이것은 문자열로만 직렬화할 수 있으며 사람이 읽을 수 없습니다.
- 사용자 정의 스칼라 유형이라고 하는 고유한 스칼라 유형을 정의할 수도 있습니다. 여기에는 날짜와 같은 유형이 포함됩니다.
- Date 사용자 정의 스칼라 유형은 다음과 같이 정의할 수 있습니다.

scala Date

이러한 사용자 지정 스칼라 유형의 직렬화, 역직렬화 및 유효성 검사를 결정하는 구현을 작성해야 합니다. 예를 들어 날짜는 Unix 타임스탬프 또는 사용자 지정 스칼라 Date 유형 케이스의 특정 데이터 형식을 가진 문자열로 처리될 수 있습니다.

또 다른 특별한 스칼라 유형은 허용되는 값의 특정 세트를 정의하는 데 사용되는 열거 유형(enum)입니다. 다음과 같이 주문 상태 열거를 정의해 보겠습니다.
```graphql
enum OrderStatus {
  CREATED
  CONFIRMED
  SHIPPED
  DELIVERED
  CANCELLED
}
```
여기서 OrderStatus 열거형은 주어진 시점의 주문 상태를 나타냅니다. 다른 유형을 살펴보기 전에 다음 하위 섹션에서 GraphQL 조각을 이해할 것입니다.

### 조각 이해하기

클라이언트 측에서 쿼리하는 동안 충돌하는 시나리오가 발생할 수 있습니다. 동일한 결과(동일한 개체 또는 필드 집합)를 반환하는 두 개 이상의 쿼리가 있을 수 있습니다. 이 충돌을 피하기 위해 쿼리 결과에 이름을 지정할 수 있습니다. 이 이름을 별칭이라고 합니다.

다음 쿼리에서 별칭을 사용하겠습니다.

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

여기서 HomeAndBillingAddress는 getAddress 쿼리 작업을 포함하는 명명된 쿼리입니다. getAddress가 두 번 사용되어 동일한 필드 집합을 반환합니다. 따라서 결과 개체를 구별하기 위해 home 및 billing 별칭이 사용됩니다.

getAddress 쿼리는 Address 개체를 반환할 수 있습니다. 주소 개체에는 유형, 주, 국가 및 연락처와 같은 추가 필드가 있을 수 있습니다. 따라서 동일한 필드 집합을 사용할 수 있는 쿼리가 있는 경우 조각을 만들어 쿼리에서 사용할 수 있습니다.

조각을 만들고 이전 코드 블록의 공통 필드를 교체해 보겠습니다. 
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
Here, the addressFragment fragment has been created and used in the query.

You can also create an inline fragment in the query. Inline fragments can be used when a querying field returns an Interface or Union type. We will explore inline fragments in more detail later.

We'll look at GraphQL interfaces in the next subsection.

### Understanding interfaces

GraphQL interfaces are abstract. You may have a few fields that are common across multiple objects. You can create an interface type for such a common set of fields. For example, a product may have some common attributes, such as ID, name, and description. The product can also have other attributes based on its type. For example, a book may have several pages, an author, and a publisher, while a bookcase may have material, width, height, and depth attributes.

Let's define these three objects (Product, Book, and Bookcase) using interfaces:

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

Here, an abstract type called Product has been created using the interface keyword. This interface can be implemented when we wish to create new the object types – Book and Bookcase.

Now, you can simply write the following query, which returns all the products (books and bookcases):

type query {

  allProducts: [Product]

}

Now, you can use the following query on the client side to retrieve all the products:

query getProducts {

  allProducts {

    id

    name

    description

  }

}

You might have noticed that the preceding code only contains attributes from the Product interface. If you want to retrieve attributes from Book and Bookcase, then you have to use inline fragments, as shown here:

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

Here, an operation (…) is being used to create the inline fragments. This way, you can fetch the fields from the type that implements the interface.

We'll understand Union types in the next subsection.

Understanding Union types
Let's say there are two object types – Book and Author. Here, you want to write a GraphQL query that can return both books and authors. Note that the interface is not there; so, how can we combine both objects in the query result? In such cases, you can use a Union type, which is a combination of two or more objects.

Consider the following before creating a Union type:

You don't need to have a common field.
Union members should be of a concrete type. Therefore, you can't use union, interface, input, or scalar types.
Let's create a Union type that can return any object included in the union type – books and bookcases – as shown in the following code block:

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

Here, the union keyword is being used to create a union type for the Book and Author objects. A pipe symbol (|) is being used to separate the included objects. At the end, a query has been defined, which returns the collection of books or authors that contains the given text.

Now, let's write this query for the client, as shown here:

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

Response JSON

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

As you can see, an inline fragment is being used in the query. Another important point is the extra field, called __typename, which refers to the object it belongs to and helps you differentiate between different objects in the client.

We'll look at input types in the next subsection.

Understanding input types
So far, you have used scalar types as arguments. GraphQL also allows you to pass object types as arguments in mutations. The only difference is that you have to declare them with input instead of using the type keyword.

Let's create a mutation that accepts an input type as an argument:

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

Here, the addProduct mutation accepts ProductInput as an argument and returns a Product.

Now, let's use the GraphQL request to add a product to the client, as shown here:

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

# JSON Output

{

  "data": {

    addProduct {

      "name": "Blink"

    }

  }

}

Here, you are running a mutation that uses an input type. You might have observed that Variable is being used here to pass ProductInput. The named mutation is being used for the variable. If variables are defined in the mutation, along with their types, then they should be used in the mutation.

Variable values should be assigned in the variable section (or beforehand in the client). The value of a variable's input is assigned using a JSON object that should map to ProductInput.

We'll look at the tools we can use while designing a GraphQL schema in the next subsection.

Tools that help with designing a schema
You can use the following tools for design and work with GraphQL. Each has its own offerings:

GraphiQL: It is pronounced graphical. It is an official GraphQL Foundation project that provides the web-based GraphQL Integrated Development Environment (IDE). It makes use of Language Server Protocol (LSP), which uses the JSON-RPC-based protocol between the source code editor and the IDE. It is available at https://github.com/graphql/graphiql.
GraphQL Playground: This is also a GraphQL IDE that provides better features than GraphiQL. It is available at https://github.com/graphql/graphql-playground.
GraphQL Faker: This provides the mock data for your GraphQL APIs. It is available at https://github.com/APIs-guru/graphql-faker.
GraphQL Editor: This allows you to design your schema visually and then transform it into code. It is available at https://github.com/graphql-editor/graphql-editor.
GraphQL Voyager: This converts your schema into interactive graphs, such as entity diagrams and all its relationships. It is available at https://github.com/APIs-guru/graphql-voyager.
In the next section, you'll test the knowledge that you have acquired throughout this chapter.

Testing GraphQL queries and mutations
Let's write queries and mutations in a real GraphQL schema to test the skill you have learned throughout this chapter.

You are going to use GitHub's GraphQL API explorer in this section. Let's perform the following steps:

First, go to https://docs.github.com/en/graphql/overview/explorer.
You might have to authorize it using your GitHub account, so that you can execute GraphQL queries.
GitHub Explorer is based on GraphiQL. It is divided into three vertical sections (from left to right):
a. There are two two subsections – an upper section for writing a query and a bottom section for defining variables.

b. The middle vertical section shows the response.

c. Normally, the rightmost section is hidden. Click on the Docs link to display it. It shows the respective documentation and schema, along with the root types that you can explore.

Let's fire this query to find out the ID of the repository you wish to mark as star:
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

Here, you are querying this book's repository by providing two arguments – the repository's name and its owner. You are fetching a few of the fields from here. One of the most important ones is stargazerCount because we are going to perform an addStar mutation. This count will tell us whether the mutation was successful or not.

Click on the Execute Query button on the top bar, or press Ctrl + Enter to execute the query. You might get the following output once this query executes successfully:
{

  "data": {

    "repository": {

      "id": "MDEwOlJlcG9zaXRvcnkyOTMyOTU5NDA=",

      "owner": {

        "id": "MDEyOk9yZ2FuaXphdGlvbjEwOTc0OTA2",

        "login": "PacktPublishing"

      },

      "name": "Modern-API-Development-with-Spring-and-

               Spring-Boot",

      "description": "Modern API Development with   

       Spring and Spring Boot, published by Packt",

      "viewerHasStarred": false,

      "stargazerCount": 1

    }

  }

}

Here, you need to copy the value of id (highlighted) from the response because you need it to mark the start.

Execute the following query to perform the addStar mutation:
mutation {

  addStar(input: {

    starrableId: "MDEwOlJlcG9zaXRvcnkyOTMyOTU5NDA="

  }) {

    clientMutationId

  }

}

This performs the addStar mutation for the given repository ID.

Once the previous query has executed successfully, you must reexecute the query from step 4 to find out about the change. If you get an access issue, then you can choose your own GitHub repository to perform these steps.
You can also explore other queries and mutations to deep dive into GraphQL.

Finally, let's understand the N+1 problem in GraphQL queries before we jump into the implementation in the next chapter.

Solving the N+1 problem
The N+1 problem is not new to Java developers. You might have encountered this problem in hibernation, which occurs if you don't optimize your queries or write entities properly.

Let's understand what the N+1 problem is.

Understanding the N+1 problem
The N+1 problem normally occurs when associations are involved. There are one-to-many relationships between the customer and the order. One customer can have many orders. If you need to find all the customers and their orders, you may do the following:

Find all the users.
Find all the user's orders based on the user's ID, which was received in the first step by setting the relation.
So, here, you fire two queries. If you optimize the implementation any further, you can place a joint between these two entities and receive all the records in a single query.

If this is so simple, then why does GraphQL encounter the N+1 problem? You need to understand the resolver function to answer this question.

If you go by the database schema we created in Chapter 4, Writing Business Logic for APIs, you can say that the getUsersOrders query will lead to the following SQL statements being executed:

SELECT * FROM ecomm.user;

SELECT * FROM ecomm.orders WHERE customer_id in (1);

SELECT * FROM ecomm.orders WHERE customer_id in (2);

...

...

SELECT * FROM ecomm.orders WHERE customer_id in (n);

Here, it's executing a query on the user to fetch all the users. Then, it executes N queries on orders. This is why it is called the N+1 problem. This is not efficient because ideally, it should execute a single query or in the worst case, two queries.

GraphQL can only respond with the values of fields that have been requested in the query due to resolvers. Each field has its own resolver function in the GraphQL server implementation that fetches the data for its corresponding field. Let's assume we have the following schema:

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

Here, we have a mutation that returns a collection of users. Each user may have a collection of orders. You might use the following query in the client:

{

  getUsersOrders {

    name

    orders {

      id

      status

    }

  }

}

Let's understand how this query will be processed by the server. In the server, each field will have its own resolver function that fetches the corresponding data.

The first resolver will be for the user and will fetch all the users from the data store. Next, the resolver will be ordered for each user. It will fetch the orders from the data store based on the given user ID. Therefore, the orders resolver would execute n times, where n is the number of users that have been fetched from the data store.

We'll learn how to resolve the N+1 problem in the next subsection.

### Solution for the N+1 problem

You need to have a solution that waits until all the orders have been loaded. Once all the user IDs have been retrieved, a database call should be made to fetch all the orders in a single data store call. You can use the batch if the size of the database is huge. Then, it can resolve the individual order resolvers. However, this is easier said than done. GraphQL provides a library called DataLoader (https://github.com/graphql/dataloader) that does this job for you.

Java provides a similar library called java-dataloader (https://github.com/graphql-java/java-dataloader) that can help you solve this problem. You can find out more about it at https://www.graphql-java.com/documentation/v16/batching/.

## Summary

In this chapter, you learned about GraphQL, its advantages, and how it compares to REST. You learned how GraphQL solves overfetching and underfetching problems. You then learned about GraphQL's root types – queries, mutations, and subscriptions – and how different blocks can help you design the GraphQL schema. Finally, you understood how resolvers work, how they can lead to the N+1 problem, and the solution to this problem.

Now that you know about the fundamentals of GraphQL, you can start designing GraphQL schemas. You also learned about GraphQL's client-side queries and how to make use of aliases, fragments, and variables to resolve common problems.

In the next chapter, you will use the GraphQL skills you acquired in this chapter to implement a GraphQL server.

Questions
Is GraphQL better than REST? If yes, then in what way?
When should you use fragments?
How can you use variables in a GraphQL query?

## Further reading

- GraphQL specifications: https://spec.graphql.org/
- GraphQL documentation: https://graphql.org/learn/
- GraphQL and Apollo with Android from Novice to Expert (video): https://www.packtpub.com/product/graphql-and-apollo-with-android-from-novice-to-expert-video/9781800564626