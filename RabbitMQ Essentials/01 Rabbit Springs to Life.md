# 01 A Rabbit Springs to Life

메시징 또는 메시지 대기열은 응용 프로그램이나 구성 요소 간의 통신 방법입니다. 메시지 대기열 덕분에 이러한 애플리케이션은 개별 작업을 처리할 때 완전히 분리된 상태를 유지할 수 있습니다. 메시지는 일반적으로 작은 요청, 응답, 상태 업데이트 또는 단순한 정보입니다. 메시지 대기열은 이러한 메시지를 보관할 임시 장소를 제공하여 애플리케이션이 필요에 따라 메시지를 보내고 받을 수 있도록 합니다.

RabbitMQ는 독립적인 애플리케이션에 대한 중개자 또는 중개자 역할을 하는 오픈 소스 **메시지 브로커**로, 이들에게 통신을 위한 공통 플랫폼을 제공합니다. RabbitMQ는 주로 클러스터링 및 복잡한 메시지 라우팅과 같은 고급 기능을 지원하는 AMQP(Advanced Message Queuing Protocol)의 Erlang 기반 구현을 사용합니다.

이 장에는 RabbitMQ를 시작하는 방법과 이것이 아키텍처에 도움이 되는 이유에 대한 정보가 포함되어 있습니다. 이 책은 가상의 택시 회사인 Complete Car(CC)를 따라 RabbitMQ를 아키텍처에 구현한 방법을 보여줍니다. 이 장에서는 모든 것을 쉽게 시작하고 실행할 수 있도록 RabbitMQ를 설치하고 구성하는 방법을 보여줍니다.

This chapter will cover the following topics:

- Explaining message queues
- Discovering AMQP and RabbitMQ
- Using RabbitMQ in real life
- Exploring the benefits of message queuing
- A RabbitMQ scenario
- Getting ready for RabbitMQ

Let's get started!

## Technical requirements

The code files of this chapter can be found on GitHub at https://github.com/PacktPublishing/RabbitMQ-Essentials-Second-Edition/tree/master/Chapter01.

## 메시지 큐 설명

연기 신호, 택배, 운반비둘기, 세마포어: 이것이 수수께끼라면 메시지라는 단어가 즉시 떠오를 것입니다. 인류는 항상 소통해야 하는 다른 그룹의 사람들 사이의 거리로 인해 제기되는 도전을 무시하고 새로운 방법을 찾기 위해 연결해야 할 필요가 있었습니다. 인류는 현대 기술로 먼 길을 왔지만 본질적으로 기본은 남아 있습니다. 발신자, 수신자 및 메시지는 당사의 모든 통신 인프라의 핵심입니다.

소프트웨어 응용 프로그램에는 동일한 요구 사항이 있습니다. 시스템은 서로 통신하고 메시지를 보내야 합니다. 보낸 메시지가 목적지에 도달했는지 확인해야 하는 경우도 있고 즉각적인 응답을 받아야 하는 경우도 있습니다. 어떤 경우에는 둘 이상의 응답을 받아야 할 수도 있습니다. 이러한 서로 다른 요구 사항에 따라 시스템 간에 서로 다른 스타일의 통신이 등장했습니다.

RabbitMQ의 기본 프로토콜인 AMQP는 다음 섹션에서 설명합니다.


## AMQP 및 RabbitMQ 발견

메시지 큐는 시스템 간의 비동기 상호 작용을 제공하는 단방향 통신 스타일입니다. 이 장에서 메시지 대기열의 작동 방식을 계속 설명하면 이점이 명확해질 것입니다. 요청-응답 메시지 교환 패턴에 대한 몇 가지 배경 지식은 RabbitMQ의 작동 방식을 설명합니다.

### 요청-응답 메시지 교환 패턴
메시지 교환 패턴에는 여러 유형이 있지만 요청-응답 스타일이 가장 일반적입니다. 클라이언트 역할을 하는 시스템은 서버 역할을 하는 다른 원격 시스템과 상호 작용합니다. 클라이언트는 데이터 요청을 보내고 서버는 다음 다이어그램과 같이 요청에 응답합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/55761e1c-afc1-4fe2-8ffa-338ca2cfc21b.png)

그림 1.1: 클라이언트와 서버 간의 요청-응답 상호 작용

요청-응답 스타일은 클라이언트가 즉각적인 응답을 받아야 하거나 테이블을 예약하기 위해 레스토랑에 전화를 걸 때 보류되는 것과 같이 서비스가 작업을 지연 없이 완료하기를 원할 때 사용됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/71dd3f7d-9555-4fe8-a6fd-a21aca382a9d.png)

그림 1.2: 클라이언트와 레스토랑 간의 요청-응답

원격 프로시저 호출, 웹 서비스 호출 또는 리소스 소비의 형태를 취하든 모델은 동일합니다. 한 시스템이 다른 시스템에 메시지를 보내고 원격 당사자가 응답하기를 기다립니다. 시스템은 이벤트와 프로세스가 동시에 발생하거나 시간과 관련된 종속성 또는 이벤트가 있는 지점 간 방식으로 서로 통신합니다. 클라이언트와 서버 간의 상호 작용은 동기식입니다.

한편, 이 요청-응답 스타일은 모든 것이 절차적으로 발생하므로 개발자에게 간단한 프로그래밍 모델을 제공합니다. 반면에 양 당사자 간의 긴밀한 결합은 발전하기 어렵고 확장하기 어렵고 독립적인 릴리스로 제공하기 어렵기 때문에 전체 시스템의 아키텍처에 깊은 영향을 미칩니다.

### 메시지 큐 교환 패턴
메시지 큐잉은 일반적으로 메시지 브로커를 통해 메시지를 통해 한 시스템이 다른 시스템과 비동기적으로 상호 작용하는 단방향 상호 작용 스타일입니다. 비동기 통신 모드의 요청 시스템은 응답을 기다리거나 반환 정보를 요구하지 않습니다. 상관없이 계속 처리됩니다. 이러한 상호 작용의 가장 일반적인 예는 이메일입니다. 요점은 비동기 통신은 처리를 계속하기 위해 응답을 기다리는 것을 포함하지 않는다는 것입니다. 실제로 응답이 없거나 응답이 전송되는 데 시간이 걸릴 수 있습니다. 어떤 경우이든 시스템은 프로세스를 계속하기 위해 응답에 의존하지 않습니다.

메시지는 게시자에서 브로커로, 마지막으로 소비자로 한 방향으로 흐릅니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/e10308b6-e487-4927-8405-bcfbf5587839.png)

Fig 1.3: 단방향 메시지큐

시스템과 애플리케이션은 메시지 발행자(제작자)와 메시지 소비자(가입자)의 역할을 모두 수행합니다. 게시자는 데이터를 의도한 소비자에게 전달하기 위해 의존하는 메시지를 브로커에 게시합니다. 응답이 필요한 경우 동일한 메커니즘을 통해 특정 시점에 도달하지만 반대입니다(소비자와 생산자 역할이 바뀝니다).

### 느슨하게 결합된 아키텍처
메시징 대기열 방식의 한 가지 큰 장점은 시스템이 서로 느슨하게 결합된다는 것입니다. 그들은 네트워크에서 다른 노드의 위치를 ​​알 필요가 없습니다. 이름만 들어도 충분합니다. 따라서 메시지 전달의 신뢰성을 브로커에게 맡기기 때문에 서로에게 영향을 주지 않고 독립적으로 시스템을 발전시킬 수 있습니다.

다음 다이어그램은 게시자와 소비자 간의 느슨한 결합을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/09a9fe21-6b50-4db2-8496-81930a935750.png)
그림 1.4: 느슨하게 결합된 아키텍처를 가능하게 하는 메시지 큐

어떤 이유에서든 한 시스템이 다운되더라도 시스템의 다른 부분은 계속 작동할 수 있으며 두 시스템 간에 전송되어야 하는 메시지는 대기열에서 대기합니다.

메시지 큐잉을 통해 표현되는 아키텍처는 다음을 허용합니다.

- 게시자 또는 소비자는 서로 영향을 주지 않고 하나씩 업데이트할 수 있습니다.
- 각 측면의 성능은 다른 측면에 영향을 주지 않습니다.
- 퍼블리셔와 컨슈머는 서로에게 영향을 주지 않고 실패할 수 있습니다.
- 완전히 독립적으로 워크로드를 확장하고 수용할 게시자 및 소비자 인스턴스의 수.
- 소비자와 게시자 간의 기술 혼합.

이 접근 방식의 주요 단점은 프로그래머가 이벤트가 차례로 발생하는 절차적 프로그래밍 모델에 의존할 수 없다는 것입니다. 메시징에서는 시간이 지남에 따라 일이 발생합니다. 이를 처리하도록 시스템을 프로그래밍해야 합니다.

이 모든 것이 약간 흐릿하다면 잘 알려진 프로토콜인 SMTP의 예를 사용하십시오. 이 프로토콜에서 전자 메일은 SMTP 서버에 게시(전송)됩니다. 그런 다음 이 초기 서버는 이메일을 저장하고 다음 SMTP 서버로 전달하는 방식으로 수신자 이메일 서버에 도달할 때까지 계속됩니다. 이 시점에서 메시지는 받은 편지함에 대기하고 소비자가 선택하기를 기다립니다(일반적으로 POP3 또는 IMAP을 통해). SMTP를 사용하면 게시자는 이메일이 언제 배달될지 또는 결국에는 배달될지 전혀 모릅니다. 배달 실패의 경우 게시자는 나중에 문제에 대해 알립니다.

유일한 확실한 사실은 브로커가 처음에 보낸 메시지를 성공적으로 수락했다는 것입니다. 이 전체 프로세스는 다음 다이어그램에서 볼 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/3bd7cc21-3873-4dd2-9881-85803726d75f.png)

그림 1.5: 메시지 큐잉에 대한 비유로서의 이메일 인프라

또한 응답이 필요한 경우 동일한 전달 메커니즘을 사용하여 비동기식으로 도착하지만 게시자와 소비자 역할은 반대입니다.

이러한 기본 개념이 확립되면 이 책에서 사용할 메시징 프로토콜인 AMQP에 대해 자세히 알아볼 때입니다.


## AMQP를 만나보세요

AMQP는 시스템이 메시지를 교환할 수 있는 방법을 정의하는 개방형 표준 프로토콜입니다. 프로토콜은 서로 통신할 시스템이 따라야 하는 일련의 규칙을 정의합니다. 소비자/생산자와 브로커 간에 발생하는 상호 작용을 정의하는 것 외에도 교환되는 메시지 및 명령의 표현도 정의합니다. AMQP는 메시지의 유선 형식을 지정하므로 상호 운용이 가능하므로 특정 공급업체나 호스팅 플랫폼에서 해석할 여지가 없습니다. 오픈 소스이기 때문에 AMQP 커뮤니티는 다양하며 광범위한 언어로 브로커 및 클라이언트 구현을 보유하고 있습니다.

RabbitMQ는 AMQP 0-9-1 사양을 기반으로 구축되었지만 AMQP 1.0을 지원하는 플러그인을 사용할 수 있습니다.

AMQP 0-9-1 사양은 http://www.rabbitmq.com/resources/specs/amqp0-9-1.pdf에서 다운로드할 수 있습니다.

다음은 AMQP의 핵심 개념 목록이며 다음 장에서 자세히 설명합니다.

- **메시지 브로커**: 브로커는 한 애플리케이션 또는 서비스에서 메시지를 수신하고 다른 애플리케이션, 서비스 또는 브로커에 전달하는 소프트웨어입니다.

- **가상 호스트**: 가상 호스트가 브로커 내에 존재합니다. 브로커 내부의 논리적 컨테이너와 유사하게 동일한 RabbitMQ 인스턴스를 사용하는 애플리케이션을 분리하는 방법입니다. 예를 들어 작업 환경을 하나의 가상 호스트에서 개발로 분리하고 다른 가상 호스트에서 스테이징하여 여러 브로커를 설정하는 대신 동일한 브로커 내에 유지합니다. 사용자, 교환, 대기열 등은 하나의 특정 가상 호스트에 격리됩니다. 특정 가상호스트에 접속한 사용자는 다른 가상호스트의 자원(대기열, 교환 등)에 접근할 수 없다. 사용자는 서로 다른 가상 호스트에 대해 서로 다른 액세스 권한을 가질 수 있습니다.

- **연결** 애플리케이션(게시자/소비자)과 브로커 간의 물리적 네트워크(TCP) 연결입니다. 클라이언트 연결이 끊기거나 시스템 오류가 발생하면 연결이 닫힙니다.

- **채널** 채널은 연결 내부의 가상 연결입니다. 새 TCP 스트림을 다시 인증하고 열 필요가 없으므로 연결을 재사용합니다. 메시지가 게시되거나 사용되면 채널을 통해 수행됩니다. 단일 연결 내에서 여러 채널을 설정할 수 있습니다.

- **교환** 교환 엔티티는 메시지에 대한 라우팅 규칙을 적용하여 메시지가 최종 목적지에 도달하는지 확인합니다. 즉, 교환은 수신된 메시지가 올바른 대기열에서 끝나는지 확인합니다. 메시지가 끝나는 대기열은 교환 유형에 의해 정의된 규칙에 따라 다릅니다. 큐는 메시지를 수신할 수 있으려면 적어도 하나의 교환에 바인딩되어야 합니다. 라우팅 규칙에는 직접(지점 간), 주제(게시-구독), 팬아웃(멀티캐스트) 및 헤더 교환이 포함됩니다.

- **대기열** 대기열은 일련의 항목입니다. 이 경우 메시지. 대기열이 브로커 내에 존재합니다.

- **바인딩** 바인딩은 교환기와 브로커 내의 대기열 간의 가상 링크입니다. 메시지가 교환에서 큐로 흐를 수 있도록 합니다.

다음 다이어그램은 AMQP의 일부 개념에 대한 개요를 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/eb85d456-ba34-460a-ba06-4a916b6296f3.png)

Fig 1.6: Overview of some of the concepts defined by the AMQP specification

이 책에 자세히 나와 있는 오픈 소스 브로커는 처음부터 AMQP를 지원하도록 구축되었지만 MQTT, HTTP 및 STOMP와 같은 다른 많은 프로토콜도 RabbitMQ에서 지원됩니다.


## The RabbitMQ broker

RabbitMQ는 AMQP 브로커의 Erlang 구현입니다. 프로토콜에서 허용하는 대로 사용자 지정 확장을 사용하여 AMQP 버전 0-9-1을 구현합니다. Erlang은 매우 안정적이고 분산된 애플리케이션 구축을 위한 본질적인 지원 때문에 선택되었습니다. 실제로 Erlang은 여러 대규모 통신 시스템에서 통신 스위치를 실행하는 데 사용되며 전체 시스템의 가용성이 99.9999999999999로 보고되었습니다(1년에 단 32밀리초의 다운타임). Erlang은 모든 운영 체제에서 실행할 수 있습니다.

데이터 지속성을 위해 RabbitMQ는 Erlang의 메모리/파일 지속 내장 데이터베이스인 Mnesia에 의존합니다. Mnesia는 사용자, 교환, 대기열, 바인딩 등에 대한 정보를 저장합니다. 큐 인덱스는 메시지 위치와 메시지 전달 여부에 대한 정보를 저장합니다. 메시지는 큐 인덱스 또는 모든 큐 간에 공유되는 키-값 저장소인 메시지 저장소에 저장됩니다.

클러스터링의 경우 주로 Erlang의 뿌리 깊은 클러스터링 기능에 의존합니다. RabbitMQ는 플러그인을 추가하여 쉽게 확장할 수 있습니다. 예를 들어, 이 메커니즘 덕분에 웹 기반 관리 콘솔을 RabbitMQ에 배포할 수 있습니다.

*플러그인을 사용하여 핵심 브로커 기능을 확장할 수 있습니다. RabbitMQ에 사용할 수 있는 많은 플러그인이 있으며 필요한 경우 플러그인을 개발할 수도 있습니다. https://www.rabbitmq.com/plugins.html.

RabbitMQ는 단일 독립 실행형 인스턴스 또는 여러 서버의 클러스터로 설정할 수 있습니다.*

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/af1f86a8-0764-4632-afdd-1b618669a3e2.png)

그림 1.7: 독립 실행형 인스턴스 또는 여러 서버의 클러스터

RabbitMQ 브로커는 브로커 간의 스마트 메시지 라우팅과 여러 데이터 센터에 걸친 용량을 사용하여 메시징 토폴로지를 형성하기 위해 연합 및 삽과 같은 다양한 기술을 사용하여 함께 연결할 수 있습니다.

다음 스크린샷은 전 세계 여러 곳에 위치한 RabbitMQ 브로커 간의 연합을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/d81c5d31-c38f-43fd-884c-797e97efe07e.png)

그림 1.8: 다양한 토폴로지에 참여하는 RabbitMQ 브로커
RabbitMQ는 플러그인을 통해 AMQP 1.0을 지원합니다.

AMQP 1.0은 AMQP의 개발 및 유지 관리가 OASIS로 이전된 후 2011년 말에 게시되었습니다. AMQP는 0-9-1과 1.0 사이에서 대폭 수정되었습니다. 이것은 너무 과감하여 교환과 같은 일부 핵심 개념이 더 이상 존재하지 않습니다. 따라서 AMQP 1.0은 0-9-1과 다른 프로토콜이지만 채택해야 할 진정으로 강력한 이유는 없습니다. 그것은 0-9-1보다 더 유능하지 않으며 일부는 또한 처음부터 매력적으로 만든 주요 측면 중 일부를 잃어 버렸다고 주장합니다.

그렇다면 RabbitMQ는 언제 또는 어디에 사용됩니까? 다음 섹션에서는 RabbitMQ의 몇 가지 일반적인 사용 사례를 설명합니다.


## RabbitMQ의 적용 사례

RabbitMQ의 가장 일반적인 사용 사례는 단일 생산자, 단일 소비자 대기열입니다. 한 응용 프로그램이 파이프의 한쪽 끝에 메시지를 넣고 다른 응용 프로그램이 다른 쪽 끝에서 나오는 메시지를 읽는 파이프로 생각하십시오. 메시지는 선입선출 순서로 전달됩니다. 이러한 메시지는 명령이거나 중요한 데이터를 포함할 수 있습니다. 이것은 쉬워 보이지만 이러한 유형의 아키텍처를 어디에 적용할 수 있습니까? 메시지 큐잉이 언제 그리고 왜 빛을 발하는지 이해할 때입니다!

### 마이크로서비스 간의 메시지 큐

메시지 대기열은 종종 마이크로서비스 사이에 사용되지만 이것이 의미하는 바는 무엇입니까?

마이크로서비스 아키텍처 스타일은 애플리케이션을 소규모 서비스로 나누고 완성된 애플리케이션은 마이크로서비스의 합이 됩니다. 서비스는 서로 엄격하게 연결되어 있지 않습니다. 대신, 예를 들어 메시지 대기열을 사용하여 연락을 유지합니다. 한 서비스는 메시지를 대기열에 비동기식으로 푸시하고 해당 메시지는 소비자가 준비되면 올바른 대상으로 배달됩니다.

마이크로서비스 아키텍처는 종종 전체 시스템이 하나의 소프트웨어로 함께 번들링되는 모놀리식 아키텍처와 비교되고 대조됩니다. 하나의 응용 프로그램은 특정 작업만 담당하는 것이 아닙니다. 실제로 특정 기능을 완료하는 데 필요한 모든 단계를 수행합니다. 모놀리식은 모든 부품이 동일한 프로세스에서 실행되기 때문에 시스템 내에서 통신합니다. 이 시스템은 모든 기능이 다른 기능에 의존하기 때문에 고도로 결합되어 있습니다.

모놀리스 아키텍처 스타일을 기반으로 구축된 웹 상점의 예에서 다음 다이어그램과 같이 하나의 시스템에서 인벤토리, 지불, 리뷰 및 평가 등을 포함한 모든 기능을 처리합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/2b1c0208-cfbf-47aa-ae97-71be171caf27.png)

그림 1.9: 모놀리스 아키텍처 스타일로 구축된 웹샵

반면에 마이크로서비스 아키텍처를 기반으로 구축된 웹샵은 시스템의 각 부분이 개별 활동임을 의미합니다. 하나의 마이크로서비스가 리뷰와 평가를 처리합니다. 그런 다음 다음 다이어그램에 표시된 것처럼 또 다른 인벤토리와 결제를 위한 또 다른 인벤토리가 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/f23a3835-2ccf-4bfe-836a-69137a57861f.png)

그림 1.10: 각 부분이 단일 비즈니스 기능에 초점을 맞춘 마이크로서비스 아키텍처 스타일

각 요청 및 응답 쌍은 독립적으로 통신합니다. 이를 상태 비저장 통신이라고 합니다. 많은 마이크로서비스가 관련되어 있지만 서로 직접적으로 의존하지 않습니다.

RabbitMQ의 또 다른 일반적인 사용 사례는 다음 섹션에서 다룰 작업 대기열입니다.

### 이벤트 및 작업

이벤트는 어떤 일이 발생했을 때 애플리케이션에 알리는 알림입니다. 한 응용 프로그램은 다른 응용 프로그램의 이벤트를 구독하고 스스로 작업을 만들고 처리하여 응답할 수 있습니다. 일반적인 사용 사례는 RabbitMQ가 느린 작업을 처리하는 작업 대기열로 작동하는 경우입니다.

이에 대한 두 가지 예를 살펴보겠습니다.

 -Instagram과 같은 소셜 미디어 애플리케이션을 상상해 보십시오. 누군가가 새 게시물을 게시할 때마다 네트워크(팔로워)는 새 게시물에 대한 정보를 받아야 합니다. 이것은 매우 시간 소모적인 작업일 수 있습니다. 수백만 명의 사람들이 동시에 같은 작업을 수행하려고 할 수 있습니다. 애플리케이션은 메시지 대기열을 사용하여 도착하는 각 게시물의 대기열에 작업을 대기열에 넣을 수 있습니다. 작업자가 요청을 받으면 보낸 사람의 팔로어 목록을 검색하고 각각을 업데이트합니다.

- 수천 명의 사용자에게 수천 개의 이메일을 보내는 이메일 뉴스레터 캠페인 도구를 생각해 보십시오. 많은 사용자가 동시에 대량 메시지를 트리거하는 시나리오가 있습니다. 이메일 뉴스레터 캠페인 도구는 이 양의 메시지를 처리할 수 있어야 합니다. 이 모든 이메일은 무엇을 누구에게 보낼지 작업자에게 지시하는 푸시 대기열에 추가할 수 있습니다. 모든 단일 이메일은 모든 이메일이 전송될 때까지 하나씩 처리됩니다.

다음 다이어그램은 메시지가 먼저 대기열에 들어간 다음 처리되는 작업 대기열을 보여줍니다. 그런 다음 새 작업이 다른 대기열에 추가됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/768cecf3-08a9-46c0-a5b5-d41491b3b207.png)

Fig 1.11: Event and task queue

이를 통해 두 가지 일반적인 사용 사례를 살펴보고 검토했습니다. RabbitMQ의 이점은 각각에서 분명했습니다. 다음 섹션에서 메시지 큐잉의 이점을 살펴봄으로써 이것을 더욱 분명하게 만들 것입니다.


## 메시지 큐잉의 이점 살펴보기

다양한 응용 프로그램 간의 통신은 분산 시스템에서 중요한 역할을 합니다. 메시지 대기열을 사용할 수 있는 경우에 대한 많은 예가 있으므로 마이크로서비스 아키텍처에서 메시지 대기열의 몇 가지 기능과 이점을 강조하겠습니다.

- **더 쉬워진 개발 및 유지 관리**: 여러 서비스에 애플리케이션을 분할하면 별도의 책임이 허용되고 개발자는 선택한 언어로 특정 서비스에 대한 코드를 자유롭게 작성할 수 있습니다. 작성된 코드를 유지 관리하고 시스템을 변경하는 것이 더 쉬울 것입니다. 단일 인증 체계를 업데이트할 때 다른 기능을 방해하지 않고 인증 모듈에만 테스트용 코드가 추가되어야 합니다.

- **장애 격리**: 장애는 단일 모듈로 격리될 수 있으므로 다른 서비스에 영향을 미치지 않습니다. 예를 들어, 보고 서비스가 일시적으로 작동하지 않는 애플리케이션은 인증 또는 지불 서비스에 영향을 미치지 않습니다. 또 다른 예로, 보고 서비스를 변경하면 고객이 보고서를 볼 수 없는 경우에도 여전히 필수 트랜잭션을 수행할 수 있습니다.

- **향상된 속도 및 생산성 수준**: 다른 개발자가 동시에 다른 모듈에서 작업할 수 있습니다. 개발 주기의 속도를 높이는 것 외에도 테스트 단계는 마이크로서비스 및 메시지 대기열의 사용에 의해 영향을 받습니다. 이는 각 서비스를 자체적으로 테스트하여 전체 시스템의 준비 상태를 확인할 수 있기 때문입니다.

- **향상된 확장성**: 마이크로서비스를 통해 원하는 대로 손쉽게 확장할 수 있습니다. 메시지 대기열이 증가하는 경우 더 많은 소비자를 추가할 수 있습니다. 하나의 서비스에 새 구성 요소를 추가하는 것은 다른 서비스를 변경하지 않고도 쉽게 수행할 수 있습니다.

- **이해하기 쉬움**: 마이크로 서비스 아키텍처의 각 모듈은 단일 기능을 나타내므로 작업에 대한 관련 세부 정보를 쉽게 알 수 있습니다. 예를 들어, 단일 서비스에 대해 컨설턴트를 고용한다고 해서 전체 시스템을 이해할 필요는 없습니다.

이제 위험할 만큼 지식이 충분하므로 이 책의 나머지 부분에 대한 장면을 설정하는 RabbitMQ 시나리오 회사에 뛰어들 시간입니다.


## A RabbitMQ scenario

CC는 무한한 가능성을 가진 새로운 택시 회사입니다. 현재 회사에는 두 명의 택시 기사와 두 명의 개발자만 있지만 내년에는 많이 확장되기를 원합니다. CC는 이미 Ruby로 웹사이트를 구축했으며 CC 여행을 데이터베이스에 저장하는 Ruby로 작성된 백엔드로 시작했습니다. CC에는 경로 보고서를 생성하는 Python으로 작성된 일부 스크립트도 있습니다.

현재까지 CC의 시스템은 다음과 같이 실행됩니다.

- 회사의 웹사이트와 블로그는 Ruby에서 실행됩니다.
- 여행의 출발지, 종점 등의 경로 데이터를 저장하는 앱은 Ruby로 작성되었습니다.
- 드라이버에게 경로 업데이트를 보내고 Ruby로 작성된 백오피스가 있습니다.
- 다중 임시 Python 스크립트는 경로 보고서를 생성하기 위해 데이터를 추출하고 메시지를 보내는 데 사용됩니다.
- 택시 앱은 Python으로 작성되었습니다.

이전 아키텍처는 다음과 같이 설명됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/31b397de-241d-45ae-af51-4b191c803e71.png)

Fig 1.12: CC software landscape

CC가 이미 바쁜 환경에 RabbitMQ를 추가하려는 이유는 무엇입니까? 주된 이유는 CC가 고객에게 제공하고자 하는 새로운 기능 때문입니다. 고객은 이동 중에 예약을 처리하는 택시 앱을 구축하기를 원합니다. CC는 또한 고통 없이 확장할 수 있기를 원합니다. 스마트폰으로 차량을 예약하고, 예약 확인을 받고, 출발 지점에 가까워지는 차량을 볼 수 있는 앱을 구축할 계획이다.

CC는 이미 다른 언어로 된 일부 서비스를 가지고 있고 CC는 쉽게 확장할 수 있기를 원하기 때문에 앱, 클라이언트 및 백엔드 간의 비동기 통신을 위해 RabbitMQ와 같은 메시지 기반 미들웨어를 사용하기로 결정했습니다.

RabbitMQ에 대한 CC의 지식과 사용이 증가함에 따라 환경에서 이를 활용할 수 있는 새로운 기회를 발견하게 될 것입니다. 지금은 RabbitMQ 작업의 첫 단계를 시작하는 CC를 따라가 보겠습니다.


## Getting ready for RabbitMQ

To get started, the following three installation and configuration steps need to be completed:

- RabbitMQ 브로커 설치
- 관리 플러그인 설치(웹 UI)
- 가상 호스트 및 사용자 구성

브로커 설치부터 시작하겠습니다!

### 브로커 설치

CC는 Ubuntu Linux에서 프로덕션 서버를 실행합니다. 한 개발자는 macOS와 Linux를 사용하고 다른 개발자는 모두 Windows를 사용합니다. 이러한 이질성은 이러한 모든 운영 체제에서 기본적으로 실행될 수 있는 RabbitMQ의 문제가 아닙니다.

RabbitMQ는 지원되는 모든 운영 체제에 대한 완전한 온라인 설치 가이드를 제공하며 http://www.rabbitmq.com/download.html에서 찾을 수 있습니다. 이 책에는 apt 저장소에서 RabbitMQ가 설치된 Debian/Ubuntu에 대한 지침이 포함되어 있습니다. 또한 이 장의 아래에 있는 Docker에 대한 지침도 포함되어 있습니다.

### Ubuntu에 RabbitMQ 설치

RabbitMQ를 설치하는 데 필요한 단계는 비교적 적습니다. 그것들은 다음과 같습니다:

1. 우분투를 업데이트합니다.
2. 저장소 키를 다운로드하여 설치합니다.
3. 키가 리포지토리에 있는지 확인합니다.
4. 패키지 저장소에서 RabbitMQ를 설치합니다.

다운로드 프로세스를 시작하기 전에 Ubuntu가 최신 버전인지 확인하십시오. 오래된 종속성은 보안 취약성을 생성하므로 운영 체제가 모든 소프트웨어의 최신 버전을 사용하고 있는지 확인하십시오.

apt update 명령을 실행하여 설치된 소프트웨어의 최신 릴리스를 다운로드합니다.

```sh
apt upgrade
```

RabbitMQ requires several software packages. Verify that curl, apt-transport-https, and GnuPG are on the system by running the following command:

```sh
sudo apt install curl gnupg -y
sudo apt install apt-transport-https
```

The -y option accepts any licenses for these dependencies. Ubuntu installs all required sub-packages.

Discover the name of the operating system by running any of the following commands:

```sh
- cat /etc/os-release
- lsb_release -a
- hostnamectl
```

The release name is non-technical. Previous names include focal and bionic. Ubuntu does not include RabbitMQ by default, so it must be added to the repository key before you proceed. Execute the following set of commands in a Terminal:

```sh
curl -fsSL https://github.com/rabbitmq/signing-keys/releases/download/2.0/rabbitmq-release-signing-key.asc 
sudo apt-key add -
sudo tee /etc/apt/sources.list.d/bintray.rabbitmq.list <<EOF
deb https://dl.bintray.com/rabbitmq-erlang/debian [os release name] erlang
deb https://dl.bintray.com/rabbitmq/debian [os release name] main
EOF
```
이러한 명령은 브로커 및 Erlang에 적절한 운영 체제 패키지를 추가하기 전에 키를 다운로드하고 저장소 목록에 추가합니다.

RabbitMQ는 분산 네트워크 생성을 위한 강력한 내장 지원 기능이 있는 기능적 언어인 Erlang으로 작성되었습니다. 개발자는 지원되는 브로커의 최신 릴리스에 대한 언어의 최소 버전 목록(https://www.rabbitmq.com/which-erlang.html)을 유지 관리합니다. 작성 당시 RabbitMQ 3.8은 Erlang 21.3부터 23까지 지원합니다.

이제 RabbitMQ를 올바르게 설치할 수 있습니다.

*RabbitMQ를 사용하는 데 반드시 필요한 것은 아니지만 이 강력한 언어와 플랫폼을 발견하는 것이 좋습니다. http://www.erlang.org/에서 Erlang에 대해 자세히 알아볼 수 있습니다. 또는 Elixir를 Erlang 가상 머신(VM)의 선택적 언어로 고려할 수 있습니다. 이에 대한 자세한 내용은 http://elixir-lang.org에서 확인할 수 있습니다.

Run the following commands to install RabbitMQ:*

``` sh
sudo apt install -y rabbitmq-server
sudo apt install librabbitmq-dev 
```

The librabbitmq-dev library includes a client for interacting with the broker. However, the server may be the only requirement.

### RabbitMQ installation on Docker

Docker containers allow the separation and control of resources without risking corrupting the operating system. Instructions for installing Docker are available from the official website: https://docs.docker.com/get-docker/. With Docker installed, pull the RabbitMQ image:

```sh
docker pull rabbitmq
```

Run the broker with reasonable defaults:

```sh
docker run -d --hostname my-rabbit --name my-rabbit -p 5672:5672 -p 15672:15672 -e RABBITMQ_ERLANG_COOKIE='cookie_for_clustering' -e RABBITMQ_DEFAULT_USER=user -e RABBITMQ_DEFAULT_PASS=password  --name some-rabbit rabbitmq:3-management
```
A Docker container needs to be created so that it's accessible from the localhost with the management console enabled. This will be discovered shortly.

### Starting RabbitMQ
Installing the RabbitMQ server from the repository also installs a suite of command-line tools used to start the server for the first time. This is done by executing the following command:

```sh
rabbitmq-server start 
```

The server starts in the foreground. To run the broker as a service, use the following commands:

```sh
sudo systemctl enable rabbitmq-server
sudo systemctl start rabbitmq-server
sudo systemctl status rabbitmq-server 
```

The systemctl command can also be used to manage services in Ubuntu. The output of the final command should show that the broker is running. Consult the RabbitMQ documentation (https://www.rabbitmq.com/troubleshooting.html) if not.

### Downloading the example code
Download all the example code files for this book. They can be purchased from http://www.packtpub.com. If you purchased this book elsewhere, visit http://www.packtpub.com/support and register to have the files emailed to you directly.

### Verifying that the RabbitMQ broker is running
Now, verify that the RabbitMQ broker is actually working by using the status service command.

Write the following line in the Terminal:
```sh
$ sudo service rabbitmq-server status
  rabbitmq-server.service - RabbitMQ broker
   Loaded: loaded (/lib/systemd/system/rabbitmq-server.service; enabled; vendor preset: enabled)
  Drop-In: /etc/systemd/system/rabbitmq-server.service.d
           └─10-limits.conf, 90-env.conf
   Active: active (running) since Mon 2019-04-29 13:28:43 UTC; 1h 43min ago
  Process: 27474 ExecStop=/usr/lib/rabbitmq/bin/rabbitmqctl shutdown (code=exited, status=0/SUCCESS)
 Main PID: 27583 (beam.smp)
   Status: "Initialized"
    Tasks: 87 (limit: 1121)
   CGroup: /system.slice/rabbitmq-server.service
           ├─27583 /usr/lib/erlang/erts-10.2.2/bin/beam.smp -W w -A 64 -MBas ageffcbf -MHas ageffcbf -MBlmbcs 512 -MHlmbcs 512 -MMmcs 30 -P 1048576 -t 5000000 
           ├─27698 /usr/lib/erlang/erts-10.2.2/bin/epmd -daemon
           ├─27854 erl_child_setup 1000000
           ├─27882 inet_gethost 4
           └─27883 inet_gethost 4

Apr 29 13:28:42 test-young-mouse-01 rabbitmq-server[27583]:   ##  ##
Apr 29 13:28:42 test-young-mouse-01 rabbitmq-server[27583]:   ##  ##      RabbitMQ 3.7.14. Copyright (C) 2007-2019 Pivotal Software, Inc.
Apr 29 13:28:42 test-young-mouse-01 rabbitmq-server[27583]:   ##########  Licensed under the MPL.  See https://www.rabbitmq.com/
Apr 29 13:28:42 test-young-mouse-01 rabbitmq-server[27583]:   ######  ##
Apr 29 13:28:42 test-young-mouse-01 rabbitmq-server[27583]:   ##########  Logs: /var/log/rabbitmq/rabbit@test-young-mouse-01.log
Apr 29 13:28:42 test-young-mouse-01 rabbitmq-server[27583]:                     /var/log/rabbitmq/rabbit@test-young-mouse-01_upgrade.log
Apr 29 13:28:42 test-young-mouse-01 rabbitmq-server[27583]:               Starting broker...
Apr 29 13:28:43 test-young-mouse-01 rabbitmq-server[27583]: systemd unit for activation check: "rabbitmq-server.service"
Apr 29 13:28:43 test-young-mouse-01 systemd[1]: Started RabbitMQ broker.
Apr 29 13:28:43 test-young-mouse-01 rabbitmq-server[27583]:  completed with 9 plugins.
```

The default folders where the package has installed files are /etc/rabbitmq for configuration files, /usr/lib/rabbitmq for application files, and /var/lib/rabbitmq for data files (mnesia).
Look at the running processes for RabbitMQ and find both the service wrapper and the Erlang VM (also known as BEAM) that's running, as follows:

```sh
$ pgrep -fl rabbitmq
27583 beam.smp

$ ps aux | grep rabbitmq
ubuntu   10260  0.0  0.1  14856  1004 pts/0    S+   15:13   0:00 grep --color=auto rabbitmq
rabbitmq 27583  0.5  8.5 2186988 83484 ?       Ssl  13:28   0:36 /usr/lib/erlang/erts-10.2.2/bin/beam.smp -W w -A 64 -MBas ageffcbf -MHas ageffcbf -MBlmbcs 512 -MHlmbcs 512 -MMmcs 30 -P 1048576 -t 5000000 -stbt db -zdbbl 128000 -K true -- -root /usr/lib/erlang -progname erl -- -home /var/lib/rabbitmq -- -pa /usr/librabbitmq/lib/rabbitmq_server-3.7.14/ebin  -noshell -noinput -s rabbit boot -sname rabbit@test-young-mouse-01 -boot start_sasl -config /etc/rabbitmq/rabbitmq -kernel inet_default_connect_options [{nodelay,true}] -sasl errlog_type error -sasl sasl_error_logger false -rabbit lager_log_root "/var/log/rabbitmq" -rabbit lager_default_file "/var/log/rabbitmq/rabbit@test-young-mouse-01.log" -rabbit lager_upgrade_file "/var/log/rabbitmq/rabbit@test-young-mouse-01_upgrade.log" -rabbit enabled_plugins_file "/etc/rabbitmq/enabled_plugins" -rabbit plugins_dir "/usr/lib/rabbitmq/plugins:/usr/lib/rabbitmq/lib/rabbitmq_server-3.7.14/plugins" -rabbit plugins_expand_dir "/var/lib/rabbitmq/mnesia/rabbit@test-young-mouse-01-plugins-expand" -os_mon start_cpu_sup false -os_mon start_disksup false -os_mon start_memsup false -mnesia dir "/var/lib/rabbitmq/mnesia/rabbit@test-young-mouse-01" -kernel inet_dist_listen_min 25672 -kernel inet_dist_listen_max 25672
rabbitmq 27698  0.0  0.1   8532  1528 ?        S    13:28   0:00 /usr/lib/erlang/erts-10.2.2/bin/epmd -daemon
rabbitmq 27854  0.0  0.1   4520  1576 ?        Ss   13:28   0:00 erl_child_setup 1000000
rabbitmq 27882  0.0  0.1   8264  1076 ?        Ss   13:28   0:00 inet_gethost 4
rabbitmq 27883  0.0  0.1  14616  1808 ?        S    13:28   0:00 inet_gethost 4
```

It is possible that, when RabbitMQ runs, a process named epmd is also running. This is the Erlang port mapper daemon, which is in charge of coordinating Erlang nodes in a cluster. It is expected to start even if the clustered RabbitMQ application is not running.

Note that by default, the broker service is configured to auto-start when the Linux host starts.

Skip the hassle of the installation and configuration of RabbitMQ and use a hosted RabbitMQ solution. CloudAMQP is the largest provider of hosted RabbitMQ clusters: www.cloudamqp.com.

Installing the management plugin (Web UI)
RabbitMQ does not install a management console by default, but the optional web-based plugin used in this example makes it easy to peek into a running RabbitMQ instance.

The Debian package installs several scripts. One of them is rabbitmq-plugins. Its purpose is to allow us to install and remove plugins. Use it to install the management plugin, as follows:

```sh
$ sudo rabbitmq-plugins enable rabbitmq_management 
Enabling plugins on node rabbit@host:
rabbitmq_management
The following plugins have been configured:
 rabbitmq_consistent_hash_exchange
 rabbitmq_event_exchange
 rabbitmq_federation
 rabbitmq_management
 rabbitmq_management_agent
 rabbitmq_shovel
 rabbitmq_web_dispatch
Applying plugin configuration to rabbit@host...
The following plugins have been enabled:
  rabbitmq_management
  rabbitmq_management_agent
  rabbitmq_web_dispatch
```

Yes, it is that easy!

Use a web browser to reach the home page of the management console by navigating to http://<hostname>:15672, as shown in the following screenshot:


Fig 1.13: The login screen of the management console
Stay tuned for the next episode – creating and configuring users!

### Configuring users

One of the scripts that's installed by the Debian package is rabbitmqctl, which is a tool for managing RabbitMQ nodes and used to configure all aspects of the broker. Use it to configure an administration user in the broker, as follows:

```sh
$ sudo rabbitmqctl add_user cc-admin taxi123
Adding user "cc-admin" ...

$ sudo rabbitmqctl set_user_tags cc-admin administrator
Setting tags for user "cc-admin" to [administrator] ...
```

By default, RabbitMQ comes with a guest user that's authenticated with the guest password. Change this password to something else, as follows:

```sh
$ sudo rabbitmqctl change_password guest guest123
```

Navigating back to the management console login screen allows us to log in with the username cc-admin and the password taxi123.

The welcome screen provides an overview of the broker's internals, as shown in the following screenshot:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/ee346036-1498-4c86-83e8-dbb9b3627f28.png)

Fig 1.14: The main dashboard of the management console

Note that at this point, the cc-admin user is not able to examine any exchange or queue in any vhost. For now, another user must be created for development purposes so that applications can connect to RabbitMQ.

Create the cc-dev user, as follows:
```sh
$ sudo rabbitmqctl add_user cc-dev taxi123
Adding user "cc-dev" ...
```

As discussed earlier in this chapter, RabbitMQ supports the notion of vhosts, which is where different users can have different access privileges. The CC development environment will have a vhost, also known as vhost. Anything that happens in the vhost happens in isolation from any other environment created in the future (such as a QA environment). It is possible to set per-vhost limits on a number of queues and concurrent client connections in later versions of RabbitMQ (3.7+).

Create a vhost called cc-dev-vhost, as follows:

```sh
$ sudo rabbitmqctl add_vhost cc-dev-vhost
Adding vhost "cc-dev-vhost" ...
```

This creates a user and a vhost for development.

### Configuring dedicated vhosts

RabbitMQ는 / 라는 기본 가상 호스트와 함께 제공되며, 이 호스트는 게스트 사용자에게 모든 권한이 있습니다. 이것은 빠른 테스트에 편리하지만 예기치 않은 영향 없이 가상 호스트를 완전히 삭제하고 처음부터 다시 시작할 수 있도록 전용 가상 호스트를 만들어 우려 사항을 분리하는 것이 좋습니다.

그대로 cc-admin이나 cc-dev 사용자는 cc-dev-vhost에서 아무 것도 할 수 있는 권한이 없습니다. 다음과 같이 가상 호스트에 모든 권한을 부여하여 이 문제를 해결할 수 있습니다.

```sh
$ sudo rabbitmqctl set_permissions -p cc-dev-vhost cc-admin ".*" ".*" ".*"
```
가상 호스트 "cc-dev-vhost"에서 사용자 "cc-admin"에 대한 권한 설정 ... $ sudo rabbitmqctl set_permissions -p cc-dev-vhost cc-dev ".*" ".*" ".*"
가상 호스트 "cc-dev-vhost"에서 사용자 "cc-dev"에 대한 권한 설정 중...
방금 수행한 작업을 요약하면 대부분의 명령은 간단하지만 ".*" ".*" ".*" 부분은 약간 신비해 보이므로 분석해 보겠습니다.

이는 고려 대상 사용자 및 가상 호스트에 대해 지정된 리소스에 대한 구성, 쓰기 및 읽기 권한을 부여하는 고려 대상 가상 호스트에 대한 세 가지 권한입니다. 교환과 대기열로 구성된 리소스는 이름과 일치하는 정규식으로 지정됩니다. 이 경우 .* 정규식을 통해 요청된 모든 리소스가 허용됩니다.

부여되는 실제 명령은 리소스 유형 및 부여된 권한에 따라 다릅니다. RabbitMQ에서 지원하는 액세스 제어 정책의 전체 목록은 http://www.rabbitmq.com/access-control.html을 참조하십시오.

모든 명령줄에 대한 대안으로 관리 콘솔의 사용자 관리 기능으로 전환합니다. 콘솔의 관리 탭을 클릭한 다음 사용자 탭에 나열된 cc-dev 사용자를 클릭하여 다음 스크린샷에 표시된 것과 유사한 정보를 봅니다. 명령줄에서 설정된 전체 사용자 구성이 표시되고 관리 콘솔에서 편집할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/d898f837-ce12-420c-9644-f4af2a4d27e7.png)

Fig 1.15: User management from the RabbitMQ management console
The details of an individual user can be found by clicking on a given user's name in the management console:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/75629c10-48d3-4fc3-b069-7cabe27614fa.png)

Fig 1.16: Details of an individual user in the management console
The RabbitMQ broker and the management plugin (Web UI) have been installed and the vhost and the users have been configured.

## 요약

이 장에서는 AMQP와 RabbitMQ가 이러한 약속을 이행하는 방법을 포함하여 메시징의 아키텍처 및 디자인 약속을 살펴보았습니다. 또한 택시회사 Complete Car가 자사의 소프트웨어 환경에 RabbitMQ를 도입하기로 결정한 이유도 밝혀졌습니다. 마지막으로 RabbitMQ 브로커가 설치되었고 이를 위해 사용자와 다양한 가상 호스트가 구성되었습니다. 메시지 대기열과 RabbitMQ에 대한 기본적인 이해를 바탕으로 다음 장에서는 이러한 개념을 기반으로 Complete Car 택시 애플리케이션의 아키텍처를 살펴봅니다.

이제 실행을 시작하고 코드를 작성할 때입니다. RabbitMQ 기반 애플리케이션 구축을 시작하려면 다음 장으로 넘어가십시오!