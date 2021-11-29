
# Chapter 10: gRPC Fundamentals

gRPC is an open source framework for general-purpose Remote Procedure Calls (RPCs) across a network. RPC allows a remote procedure (hosted on a different machine) to call as if it is calling a local procedure in connected systems without coding the remote interaction details. RPC has a constant meaning in gRPC abbreviation. It seems logical that the g in gRPC would refer to Google because it was initially developed there. But the meaning of the g has changed with every release. For its first release, version 1.0, the g in gRPC stood for gRPC itself. That is, in version 1, it stands for gRPC Remote Procedure Call. You are going to use gRPC version 1.37, in which the g stands for gilded. Therefore, you can refer to gRPC as gilded Remote Procedure Call (for version 1.37). You can find out all the meanings of the g for different versions at https://github.com/grpc/grpc/blob/master/doc/g_stands_for.md.

In this chapter, you'll learn the fundamentals of gRPC such as gRPC architecture, gRPC service definitions, its lifecycle, gRPC server, and client. This chapter will provide you with a foundation that you can use to implement gRPC-based APIs. These fundamentals will help you to implement inter-service communication in a sample e-commerce app.

You will use gRPC-based APIs to develop a basic payment gateway for processing the payments in an e-commerce app in the next chapter.

You will explore the following topics in this chapter:

- Introduction and gRPC architecture
- Understanding service definitions
- Exploring the RPC life cycle
- Understanding the gRPC server and gRPC stub
- Handling errors

After completing this chapter, you will have learned the gRPC basics that will help you to implement a gRPC-based web service in the next chapter.

## Technical requirements
This chapter contains the theory of gRPC. However, you need the following for the development and testing of gRPC-based web services:

- Any Java IDE, such as NetBeans, IntelliJ, or Eclipse
- Java Development Kit (JDK) 15
- An internet connection to clone the code and download the dependencies and Gradle
- Postman/cURL (for API testing)

So, let's begin!


## Introduction and gRPC architecture

gRPC is an open source framework for general-purpose RPC across a network. gRPC supports full-duplex streaming and is also mostly aligned with HTTP/2 semantics. It supports different media formats, such as Protobuf (default), JSON, XML, and Thrift. The use of Protocol Buffer (Protobuf) aces the others because of higher performance.

gRPC brings the best of REST and RPC to the table and is well suited for distributed network communication through APIs. It offers some prolific features, as follows:

- It is designed for a highly scalable distributed system and offers low latency.
- It offers load balancing and failover.
- It can be integrated easily at the application layer for interaction with flow control because of its layered design.
- It supports cascade call cancellation.
- It offers wide communication—mobile app to server, web app to server, and any gRPC client app to the gRPC server app on different machines.

You're already well aware of REST and its implementation. Let's find out the differences between REST and gRPC in the next sub-section, which gives you a different perspective and allows you to choose between REST or gRPC based on requirements and use cases.

### REST versus gRPC

gRPC는 클라이언트-서버 아키텍처를 기반으로 하지만 REST에서는 그렇지 않습니다.

gRPC와 REST는 모두 HTTP 프로토콜을 활용합니다. gRPC는 음성 또는 화상 통화와 같은 다양한 시나리오에 적합한 REST와 달리 HTTP/2 사양 및 양방향 스트리밍 통신을 지원합니다.

쿼리 매개변수, 경로 매개변수 및 REST의 요청 본문을 사용하여 페이로드를 전달할 수 있습니다. 이는 요청 페이로드/데이터가 다른 소스를 사용하여 전달될 수 있음을 의미하며, 이로 인해 다른 소스의 페이로드/데이터를 구문 분석하여 지연 시간과 복잡성이 추가됩니다. 반면에 gRPC는 정적 경로와 요청 페이로드의 단일 소스를 사용하기 때문에 REST보다 성능이 우수합니다.

아시다시피 REST 응답 오류는 HTTP 상태 코드에 따라 달라지지만 gRPC는 오류 집합을 공식화하여 API와 잘 정렬되도록 했습니다.

REST API는 순수하게 HTTP에 의존하기 때문에 구현이 더 유연합니다. 이는 유연성을 제공하지만 엄격한 검증 및 검증을 위한 표준과 규칙이 필요합니다. 그러나 이러한 엄격한 검증과 검증이 왜 필요한지 아십니까? 다양한 방식으로 API를 구현할 수 있기 때문입니다. 예를 들어, HTTP DELETE 메서드를 사용하는 대신 HTTP 메서드를 사용하여 리소스를 삭제할 수 있으며 이는 단순히 끔찍하게 들립니다.

언급된 모든 것 외에도 gRPC는 호출 취소, 부하 분산 및 장애 조치를 지원하고 처리하기 위해 구축되었습니다.

REST는 성숙하고 널리 채택되었지만 gRPC는 이점을 제공합니다. 따라서 장단점에 따라 선택할 수 있습니다. (참고로, 자체 제품을 제공하는 GraphQL에 대해서는 아직 논의하지 않았습니다. GraphQL에 대해서는 13장, 그래프 QL 기초 및 14장, 그래프 QL 개발 및 테스트에서 배우게 됩니다.)

다음 섹션에서 REST와 같은 웹 통신에 gRPC를 사용할 수 있는지 알아보겠습니다.

### 웹 브라우저와 모바일 앱에서 gRPC 서버를 호출할 수 있나요?

물론 가능합니다. gRPC 프레임워크는 분산 시스템의 통신을 위해 설계되었으며 대부분 HTTP/2 의미 체계와 일치합니다. 로컬 개체를 호출하는 것처럼 모바일 앱에서 gRPC API를 호출할 수 있습니다. 이것이 gRPC의 아름다움입니다! 인트라넷과 인터넷을 통한 서비스 간 통신과 모바일 앱 및 웹 브라우저에서 gRPC 서버로의 호출을 지원합니다. 따라서 각종 통신에 활용할 수 있습니다.

gRPC for web(즉, gRPC-web)은 2018년에 상당히 새로운 것이었으나, 현재(2021년)에는 더 많은 인지도를 얻고 있으며 특히 사물 인터넷(IoT) 응용 프로그램에 사용되고 있습니다. 이상적으로는 내부 서비스 간 통신에 먼저 채택한 다음 웹/모바일 서버 통신에 채택해야 합니다.

다음 하위 섹션에서 아키텍처에 대해 자세히 알아보겠습니다.

### gRPC 아키텍처 개요
gRPC는 범용 RPC 기반 프레임워크입니다. 다음 단계를 포함하는 RPC 스타일에서 매우 잘 작동합니다.

1. 우선, 매개변수 및 반환 유형과 함께 메서드 서명을 포함하는 서비스 인터페이스를 정의합니다.
2. 그런 다음 정의된 서비스 인터페이스를 gRPC 서버의 일부로 구현합니다. 이제 원격 호출을 처리할 준비가 되었습니다.
3. 다음으로 서비스 인터페이스를 사용하여 생성할 수 있는 클라이언트용 스텁이 필요합니다. 클라이언트 애플리케이션은 로컬 호출인 스텁을 호출합니다. 차례로 스텁은 gRPC 서버와 통신하고 반환된 값은 gRPC 클라이언트에 전달됩니다. 이것은 다음 다이어그램에 나와 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/B16561_Figure_10.1.jpg)

Figure 10.1 – gRPC client-server architecture

클라이언트 애플리케이션의 경우 응답을 얻기 위해 스텁에 대한 로컬 호출일 뿐입니다. 동일한 시스템이나 다른 시스템에 서버를 둘 수 있습니다. 이렇게 하면 분산 서비스를 더 쉽게 작성할 수 있습니다. 마이크로서비스를 작성하기 위한 이상적인 도구입니다. gRPC는 언어 독립적입니다. 다른 언어로 서버와 클라이언트를 작성할 수 있습니다. 이것은 개발에 많은 유연성을 제공합니다.

gRPC는 원격 호출을 가능하게 하기 위해 다음과 같은 계층이 있는 계층화된 아키텍처입니다.

- 스텁: 클라이언트가 스텁을 통해 서버를 호출한다는 것을 알고 있습니다. 스텁은 최상위 레이어입니다. 스텁은 서비스 인터페이스, 메소드 및 메시지를 포함하는 IDL(인터페이스 정의 언어) 파일에서 생성됩니다. IDL 파일은 인터페이스가 Protobuf를 사용하여 정의된 경우 .proto 확장자를 갖습니다.

- 채널: 스텁은 서버와 통신하기 위해 ABI(응용 프로그램 바이너리 인터페이스)를 사용합니다. 채널은 이러한 ABI를 제공하는 중간 계층입니다. 일반적으로 채널은 특정 호스트 및 포트의 서버에 대한 연결을 제공합니다. 이것이 채널이 연결됨 또는 유휴 상태와 같은 상태인 이유입니다.

- 전송: 가장 낮은 계층이며 HTTP/2를 프로토콜로 사용합니다. 따라서 gRPC는 동일한 네트워크 연결을 통해 전이중 통신 및 다중 병렬 호출을 제공합니다.

다음 단계에 따라 gRPC 기반 서비스를 개발할 수 있습니다.

1. .proto 파일(Protobuf)을 사용하여 서비스 인터페이스를 정의합니다.
2. 1단계에서 정의한 서비스 인터페이스의 구현을 작성하십시오.
3. gRPC 서버를 생성하고 여기에 서비스를 등록합니다.
4. 서비스 스텁을 생성하고 gRPC 클라이언트와 함께 사용합니다.

다음 장인 11장, gRPC 기반 API 개발 및 테스트에서 실제 gRPC 서비스를 구현합니다.

> GRPC STUB
>
> A stub is an object that exposes service interfaces. The gRPC client calls the stub method, hooks the call to the server, and gets the response back.

You need to understand Protobuf for defining the service interfaces. Let's explore it in the next sub-section.

### Protocol Buffer

Protobuf was created in 2001 and was publicly made available in 2008. It was also used by Google's microservice-based system, Stubby.

gRPC also works well with JSON and other media types. However, you'll define the service interfaces using Protobuf because it is known for its performance. It allows formal contracts, better bandwidth optimization, and code generation. Protobuf is also the default format for gRPC. gRPC makes use of Protobuf not only for data serialization but also for code generation. Protobuf serializes data and unlike JSON, YAML is not human-readable. Let's see how it is built.

Protobuf messages contain a series of key-value pairs. The key specifies the message field and its type. Let's examine the following Employee message:

```
message Employee 
  int64 id = 1;
  string firstName = 2;
}
```
Let's represent this message using Protobuf (with an id value of 299 and firstName value of Scott), as shown in the following diagram:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/B16561_Figure_10.2.jpg)
Figure 10.2 – Employee message representation using Protobuf

The `Id` and `firstName` fields are tagged with numbers sequenced 1 and 2, respectively, which is required for serialization. The wire type is another aspect that provides information to find the length of the value.

The following table contains the wire types and their respective meanings:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/B16561_Table_10.1.jpg)

A Protobuf file is created with the .proto extension. You define service interfaces in the form of method signatures and messages (objects) that are referred to in method signatures. These messages can be method parameters or returned types. You can compile a defined service interface with the protoc compiler, which generates the classes for interfaces and given messages. Similarly, you can also generate the stubs for the gRPC client.

Let's have a look at the following sample .proto file:

Sample Service Interface of Employee

```
syntax = "proto3";
package com.packtpub;
option java_package = «com.packt.modern.api.proto»;
option java_multiple_files = true;
message Employee {
  int64 id = 1;
  string firstName = 2;
  string lastName = 3;
  int64 deptId = 4;
  double salary = 5;
  message Address {
    string houseNo = 1;
    string street1 = 2;
    string street2 = 3;
    string city = 4;
    string state = 5;
    string country = 6;
    string pincode = 7;
  }
}
message EmployeeCreateResponse {
  int64 id = 1;
}
service EmployeeService {
  rpc Create(Employee) returns (EmployeeCreateResponse);
}
```

Let's understand this code line by line:

1. 첫 번째 줄은 구문 키워드로 표시된 Protobuf 버전을 나타냅니다. 구문 값(proto3)은 컴파일러에게 Protobuf 버전 3이 사용되었음을 알려줍니다. 기본 버전은 proto2입니다. Protobuf 버전 3은 더 많은 기능과 단순화된 구문을 제공하며 더 많은 언어를 지원합니다. gRPC는 Protobuf 버전 3을 사용할 것을 권장합니다.
2. 다음으로 package 키워드와 패키지 이름을 사용하여 proto 패키지 이름을 정의합니다. 메시지 유형 간의 이름 충돌을 방지합니다.
3. 다음으로 java_package 매개변수를 사용하여 Java 패키지 이름을 정의하는 옵션 키워드를 사용합니다.
4. 그런 다음 java_multiple_files 매개변수를 사용하여 각 루트 수준 메시지 유형에 대해 별도의 파일을 생성하기 위해 option 키워드를 다시 사용합니다.
5. 그런 다음 객체일 뿐인 메시지 키워드를 사용하여 메시지를 정의합니다. 메시지 및 해당 필드는 정확한 사양으로 개체를 정의하는 강력한 유형을 사용하여 정의됩니다. Java의 중첩 클래스처럼 중첩 메시지를 정의할 수 있습니다. 마지막 포인트에는 메시지 필드 유형을 정의하는 데 사용할 수 있는 Protobuf 유형 테이블이 포함되어 있습니다.
6. Employee.Address를 사용하여 다른 메시지의 주소 필드를 정의할 수 있습니다.
7. 일련 번호가 표시된 필드의 태깅은 바이너리 메시지의 직렬화 및 구문 분석에 사용되기 때문에 필요합니다.
8. 일단 직렬화되면 메시지 구조를 변경할 수 없습니다.
9. 서비스 정의는 서비스 키워드를 사용하여 정의됩니다. 서비스 정의에는 메소드가 포함됩니다. rpc 키워드를 사용하여 메서드를 정의할 수 있습니다. 참조는 EmployeeService 서비스 정의를 참조하십시오. 다음 하위 섹션에서 서비스 정의에 대해 자세히 알아볼 것입니다.
10. Protobuf에는 미리 정의된 유형(스칼라 유형)이 있습니다. 메시지 필드는 Protobuf 스칼라 유형 중 하나를 가질 수 있습니다. .proto 파일을 컴파일하면 메시지 필드가 해당 언어 유형으로 변환됩니다. 다음 표는 Protobuf 유형과 Java 유형 간의 매핑을 정의합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/B16561_Table_10.2.jpg)

Protobuf also allows you to define the enumeration types using the enum keyword and maps using the map<keytype, valuetype> keyword. Please refer to the following code for examples of enumeration and map types:
```
...omitted
message Employee {
  ..omitted
  enum Grade {
    I_GRADE = 1;
    II_GRADE = 2;
    III_GRADE = 3;
    IV_GRADE = 4;
  }
  map<string, int32> nominees = 1;
  ..omitted
}
```

앞의 샘플 코드는 I_GRADE와 같은 값이 있는 Grade 열거 필드가 있는 Employee 메시지를 만듭니다. 후보자 필드는 유형이 string인 키와 유형이 int32인 값이 있는 맵입니다.

다음 섹션에서 서비스 정의를 자세히 살펴보겠습니다.


## 서비스 정의 이해

각각의 매개변수 및 반환 유형으로 해당 메서드를 지정하여 서비스를 정의합니다. 이러한 메서드는 원격으로 호출할 수 있는 서버에 의해 노출됩니다. 다음 코드 블록과 같이 이전 하위 섹션에서 EmployeeService 정의를 정의했습니다.

```
서비스 직원 서비스 {
  rpc Create(Employee)는 (EmployeeCreateResponse)를 반환합니다.
}
```

여기서 Create는 EmployeeService 서비스 정의에 의해 노출되는 메소드이다. Create 서비스에 사용되는 메시지도 서비스 정의의 일부로 정의되어야 합니다. Create 서비스 메소드는 클라이언트가 단일 요청 객체를 보내고 서버로부터 단일 응답 객체를 수신하기 때문에 단항 서비스 메소드입니다.

gRPC에서 제공하는 서비스 방법 유형에 대해 자세히 알아보겠습니다.

- **unary**: 이전 예제에서 단항 서비스 방법에 대해 이미 논의했습니다. 단일 요청에 대한 단방향 응답이 있습니다.

- **서버 스트리밍**: 이러한 유형의 서비스 방법에서 클라이언트는 단일 개체를 서버에 보내고 그 대가로 스트림 응답을 받습니다. 이 스트림에는 일련의 메시지가 포함됩니다. 스트림은 클라이언트가 모든 메시지를 수신할 때까지 열린 상태로 유지됩니다. 메시지 시퀀스 순서는 gRPC에 의해 보장됩니다. 다음 예에서 클라이언트는 경기가 끝날 때까지 라이브 스코어 메시지를 계속 수신합니다.
```
rpc LiveMatchScore(MatchId) returns (stream MatchScore);
```
- **클라이언트 스트리밍**: 이러한 유형의 서비스 방법에서 클라이언트는 일련의 메시지를 서버에 보내고 그 대가로 응답 개체를 받습니다. 스트림은 클라이언트가 모든 메시지를 보낼 때까지 열린 상태로 유지됩니다. 메시지 시퀀스 순서는 gRPC에 의해 보장됩니다. 클라이언트가 모든 메시지를 보내면 서버 응답을 기다립니다. 다음 예에서 클라이언트는 모든 데이터 레코드가 전송될 때까지 서버에 데이터 메시지를 보낸 다음 보고서를 기다립니다.
```
rpc AnalyzeData(스트림 DataInput) returns (Report);
```

- **양방향 스트리밍**: 클라이언트와 서버 스트리밍을 동시에 실행하는 것입니다. 이는 서버와 클라이언트 모두 읽기-쓰기 스트림을 사용하여 일련의 메시지를 보낸다는 것을 의미합니다. 여기에서 시퀀스의 순서가 유지됩니다. 그러나 이 두 스트림은 독립적으로 작동합니다. 따라서 각자 원하는 순서대로 읽고 쓸 수 있습니다. 서버는 하나씩 또는 한 번에 메시지를 읽고 회신하거나 임의의 조합을 가질 수 있습니다. 다음 예에서 처리된 레코드는 즉시 하나씩 보내거나 나중에 다른 배치로 보낼 수 있습니다.
```
rpc BatchProcessing(스트림 InputRecords) returns
    (stream Response);
```
이제 gRPC 서비스 정의에 대해 배웠으므로 다음 섹션에서 RPC 수명 주기를 살펴보겠습니다.


## RPC 수명 주기 살펴보기

이전 섹션에서는 네 가지 유형의 서비스 정의에 대해 배웠습니다. 각 유형의 서비스 정의에는 고유한 수명 주기가 있습니다. 이 섹션에서는 각 서비스 정의의 수명 주기에 대해 자세히 알아보겠습니다.

### 단항 RPC의 수명 주기

단항 RPC는 서비스 방법의 가장 간단한 형태입니다. 클라이언트와 서버 모두 단일 개체를 보냅니다. 어떻게 작동하는지 알아봅시다. 단항 RPC는 클라이언트에 의해 시작됩니다. 클라이언트가 스텁 메서드를 호출합니다. 스텁은 RPC 호출이 호출되었음을 서버에 알립니다. 스텁은 또한 알림과 함께 서버 클라이언트의 메타데이터, 메서드 이름 및 지정된 기한(해당되는 경우)을 제공합니다.

*메타데이터는 시간 초과 및 인증 세부 정보와 같은 키-값 쌍 형태의 RPC 호출에 대한 데이터입니다.*

그런 다음 응답으로 서버는 초기 메타데이터를 다시 보냅니다. 서버가 초기 메타데이터를 즉시 보낼지 아니면 클라이언트의 요청 메시지를 받은 후에 보낼지 여부는 애플리케이션에 따라 다릅니다. 그러나 서버는 응답 전에 이를 보내야 합니다.

서버는 요청에 대해 작업하고 클라이언트의 요청 메시지를 받은 후 응답을 준비합니다. 서버는 성공적인 호출에 대한 상태(코드 및 선택적 메시지) 및 선택적 후행 메타데이터와 함께 응답을 다시 보냅니다.

클라이언트는 응답을 수신하고 호출을 완료합니다(상태 OK의 경우 HTTP 상태 200과 유사).

다음으로 서버 스트리밍 RPC의 수명 주기에 대해 알아보겠습니다.

### 서버 스트리밍 RPC의 수명 주기
서버 스트리밍 RPC의 수명 주기는 단항 RPC와 거의 동일합니다. 동일한 단계를 따릅니다. 유일한 차이점은 스트림 응답으로 인해 응답이 전송되는 방식입니다. 서버는 모든 메시지가 전송될 때까지 메시지를 스트림으로 전송합니다. 결국 서버는 상태(코드 및 선택적 메시지)와 선택적 후행 메타데이터가 포함된 응답을 다시 보내고 서버 측 처리를 완료합니다. 클라이언트는 서버의 모든 메시지를 받으면 수명 주기를 완료합니다.

### 클라이언트 스트리밍 RPC의 수명 주기
클라이언트 스트리밍 RPC의 수명 주기는 단항 RPC와 거의 동일합니다. 동일한 단계를 따릅니다. 유일한 차이점은 스트림 요청으로 인해 요청이 전송되는 방식입니다. 클라이언트는 모든 메시지가 서버로 보내질 때까지 메시지를 스트림으로 보냅니다. 서버는 성공적인 호출에 대한 상태(코드 및 선택적 메시지) 및 선택적 후행 메타데이터와 함께 단일 메시지 응답을 다시 보냅니다. 서버는 유휴 시나리오에서 모든 클라이언트의 메시지를 수신한 후 응답을 보냅니다. 클라이언트는 서버 메시지를 받으면 수명 주기를 완료합니다.

### 양방향 스트리밍 RPC의 수명 주기

양방향 스트리밍 RPC 수명 주기의 처음 두 단계는 단항 RPC와 동일합니다. 양쪽의 스트리밍 처리는 애플리케이션에 따라 다릅니다. 두 스트림이 서로 독립적이기 때문에 서버와 클라이언트 모두 순서에 관계없이 메시지를 읽고 쓸 수 있습니다.

서버는 클라이언트가 보낸 요청 메시지 스트림을 임의의 순서로 처리할 수 있습니다. 예를 들어, 서버와 클라이언트는 탁구를 할 수 있고 클라이언트는 요청 메시지를 보내고 서버는 그것을 처리합니다. 다시 말하지만, 클라이언트는 요청 메시지를 보내고 서버는 이를 처리하고 아시다시피 프로세스가 계속됩니다. 또는 서버는 메시지를 쓰기 전에 모든 클라이언트의 메시지를 수신할 때까지 기다립니다.

클라이언트는 모든 서버 메시지를 받으면 수명 주기를 완료합니다.

### Events that impact the life cycle

다음 이벤트는 RPC의 수명 주기에 영향을 줄 수 있습니다.

- 기한/시간 초과: gRPC는 기한/시간 초과를 지원합니다. 따라서 클라이언트는 서버로부터 응답을 받기 위해 정의된 기한/시간 초과를 기다립니다. 대기 시간이 정의된 기한/시간 초과를 초과하면 DEADLINE_EXCEEDED 오류가 발생합니다. 마찬가지로 서버는 특정 RPC가 시간 초과되었는지 여부 또는 RPC를 완료하는 데 남은 시간을 확인하기 위해 쿼리할 수 있습니다.
    시간 초과 구성은 언어별로 다릅니다. 일부 언어 API는 시간 초과(시간 지속)를 지원하고 일부는 마감 시간(고정 시점)을 지원합니다. API의 기본값은 기한/시간 초과이며 일부는 그렇지 않을 수 있습니다.

- RPC 종료: 클라이언트와 서버가 호출 성공에 대해 독립적이고 로컬에서 결정을 내리기 때문에 RPC가 종료되는 시나리오는 거의 없으며 결론이 일치하지 않을 수 있습니다. 예를 들어, 서버는 모든 메시지를 전송하여 해당 부분을 완료할 수 있지만 시간 초과 후에 응답이 도착했기 때문에 클라이언트 측에서는 실패할 수 있습니다. 또 다른 시나리오는 클라이언트가 모든 메시지를 보내기 전에 서버가 RPC를 완료하기로 결정하는 경우입니다.

- RPC 취소: gRPC에는 서버나 클라이언트가 언제든지 RPC를 취소할 수 있는 조항이 있습니다. 그러면 RPC가 즉시 종료됩니다. 그러나 취소하기 전에 변경된 사항은 롤백되지 않습니다.

Let's explore the gRPC server and stub a bit more in the next section.



## Understanding the gRPC server and gRPC stub

그림 10.1을 자세히 살펴보면 gRPC가 클라이언트-서버 아키텍처를 기반으로 하기 때문에 gRPC 서버와 gRPC 스텁이 구현의 핵심 부분입니다. 서비스를 정의하고 나면 gRPC Java 플러그인과 함께 Protobuf 컴파일러인 protoc를 사용하여 서비스 인터페이스와 스텁을 모두 생성할 수 있습니다. 다음 장인 11장, gRPC 기반 API 개발 및 테스트에서 실용적인 예를 찾을 수 있습니다.

다음 유형의 파일이 컴파일러에서 생성됩니다.

- **Models**: 요청 및 응답 메시지의 종류를 직렬화, 역직렬화, 가져오기 위한 Protobuf 코드가 포함된 서비스 정의 파일에 정의된 모든 메시지(즉, 모델)를 생성합니다.

- **gRPC Java 파일**: 서비스 기반 인터페이스와 스텁을 포함합니다. 기본 인터페이스가 구현된 다음 gRPC 서버의 일부로 사용됩니다. 스텁은 클라이언트가 서버와 통신하는 데 사용합니다.

먼저 EmployeeService에 대해 다음 코드와 같이 인터페이스를 구현해야 합니다.

```java
public class EmployeeService extends
    EmployeeServiceImplBase {  
  // some code
  @Override
  public void create(Employee request,
        io.grpc.stub.StreamObserver<Response>responseObserver) {
   
    // implementation
  }
}
```
Once you implement the interface, you can run the gRPC server to serve the requests from gRPC clients:
```java
public class GrpcServer {
  public static void main(String[] arg) {
    try {
      Server server = ServerBuilder.forPort(8080)
        .addService(new EmployeeService())
        .build();
      System.out.println("Starting gRPC Server Service...");
      server.start();
      System.out.println("Server has started at port:8080");
      System.out.println("Following services are available:");
      server.getServices().stream()
        .forEach(
          s -> System.out.println("Service Name: " +
          s.getServiceDescriptor().getName())
        );
      server.awaitTermination();
    } catch (Exception e) {
      // error handling
    }
  }
}
```
For clients, first you need to create the channel using `ChannelBuilder`, and then you can use the created channels for creating stubs, as shown in the following code:
```java
public EmployeeServiceClient(ManagedChannelBuilder<?>channelBuilder) {
  channel = channelBuilder.build();
  blockingStub = EmployeeServiceGrpc.newBlockingStub(
    channel);
  asyncStub = EmployeeServiceGrpc.newStub(channel);
}
```

Here, both blocking and asynchronous stubs have been created using the channel built using the ManageChannelBuilder class.

Let's explore error handling in the next section.

## 오류 처리

HTTP 상태 코드를 사용하는 REST와 달리 gRPC는 오류 코드와 선택적 오류 메시지(문자열)가 포함된 상태 모델을 사용합니다.

HTTP 오류 코드에는 제한된 정보가 포함되어 있기 때문에 오류 세부 정보를 포함하기 위해 Error라는 특수 클래스를 사용했다는 것을 기억한다면. 마찬가지로 gRPC 오류 상태 모델은 코드 및 선택적 메시지(문자열)로 제한됩니다. 클라이언트가 오류를 처리하거나 재시도하는 데 사용할 수 있는 오류 세부 정보가 충분하지 않습니다. https://cloud.google.com/apis/design/errors#error_model에 설명된 대로 더 풍부한 오류 모델을 사용하여 자세한 오류 정보를 클라이언트에 다시 전달할 수 있습니다. 빠른 참조를 위해 다음 코드 블록에서 오류 모델을 찾을 수도 있습니다.

```java
package google.rpc;
message Status {
  // actual error code is defined by `google.rpc.Code`.
  int32 code = 1;
  // A developer-facing human-readable error message
  string message = 2;
  // Additional error information that the client code can
  // use
  // to handle the error, such as retry info or a help
  // link.
  repeated google.protobuf.Any details = 3;
}
```
The details field contains extra information, and you can use it to pass relevant information, such as RetryInfo, DebugInfo, QuotaFailure, ErrorInfo, PreconditionFailure, BadRequest, RequestInfo, ResourceInfo, Help, and LocalizedMethod. All these message types are available at https://github.com/googleapis/googleapis/blob/master/google/rpc/error_details.proto.

These richer error models are described using Protobuf. If you would like to use richer error models, you have to make sure that support libraries are aligned with the practical use of APIs as is described for Protobuf.

### Error status codes
Similar to REST, errors can be raised by the RPC for various reasons, such as network failure or data validation. Let's have a look at the following REST error codes and their respective gRPC counterparts:


gRPC error codes are more readable as you don't need a mapping to understand the number codes.

## Summary

In this chapter, you have explored Protobuf, an IDL, and the serialization utility. You have also explored gRPC fundamentals such as service definitions, messages, server interfaces, and methods. You have compared gRPC with REST. I hope this has given you enough perspective to understand gRPC.

You have also learned about the gRPC life cycles, servers, and clients with stubs. You have covered the following in this chapter – Protobuf, gRPC architecture, and gRPC fundamentals – which will allow you to develop gRPC-based APIs and services.

You will make use of the fundamentals you have learned in this chapter in the next chapter for implementing the gRPC server and client.

## Questions

- What is RPC?
- How is gRPC different in comparison to REST and which one should be used?
- Which type of service method is useful when you want to view the latest tweets or do similar types of work?

## Further reading

- gRPC documentation: https://grpc.io/
- Practical gRPC: https://www.packtpub.com/in/web-development/practical-grpc
