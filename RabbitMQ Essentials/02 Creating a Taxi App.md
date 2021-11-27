# 02 Creating a Taxi Application

일상 대화에서 사람들은 서로 인사하고 농담을 주고받으며 결국 대화를 끝내고 길을 갑니다. 저수준 TCP 연결은 RabbitMQ의 경량 채널에서 동일한 방식으로 작동합니다. RabbitMQ를 통해 메시지를 교환하려는 애플리케이션은 메시지 브로커에 영구적인 연결을 설정해야 합니다. 이 연결이 설정되면 메시지 게시 및 소비와 같은 메시지 중심 상호 작용을 수행할 수 있도록 채널을 만들어야 합니다.

이러한 기본 사항을 설명한 후 이 장에서는 브로커가 교환을 사용하여 각 메시지가 전달되어야 하는 위치를 결정하는 방법에 대해 설명합니다. 교환은 우편 배달부와 같습니다. 나중에 소비자가 찾을 수 있도록 적절한 대기열(사서함)에 메시지를 전달합니다.

기본 RabbitMQ 개념은 다음 다이어그램에 나와 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/18e8d28a-a508-4004-8e74-5c3d14f7e03b.png)

Fig 2.1: Basic RabbitMQ concepts

By the end of this chapter, you will have a solid understanding of the application architecture behind the Complete Car (CC) platform and how they sent the first message through RabbitMQ. This requires an introduction to two different types of exchanges: direct exchange, which delivers messages to a single queue, and topic exchange, which delivers messages to multiple queues based on pattern-matching routing keys.

To get the best start possible, following topics are covered:

- The application architecture behind CC
- Establishing a connection to RabbitMQ
- Sending the first messages
- Adding topic messages

Let's get started!


## Technical requirements

이 장의 코드 파일은 GitHub(https://github.com/PacktPublishing/RabbitMQ-Essentials-Second-Edition/tree/master/Chapter02)에서 찾을 수 있습니다.


## CC 이면의 애플리케이션 아키텍처

CC는 택시 기사가 사용하는 애플리케이션과 고객이 사용하는 애플리케이션이 필요합니다. 고객은 애플리케이션을 통해 택시를 요청할 수 있어야 하고 택시 기사는 요청을 수락할 수 있어야 합니다(탑승):

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/9ef350c8-bc43-4d03-b857-8019b22db9a2.png)

그림 2.2: 고객이 CC 애플리케이션을 통해 택시를 요청합니다.

고객은 여행의 시작점과 끝점에 대한 정보를 입력할 수 있어야 합니다. 활성 드라이버는 요청을 수신하고 이를 수락할 수 있습니다. 고객은 결국 여행 중 택시의 위치를 ​​따라갈 수 있어야합니다.

다음 다이어그램은 CC가 달성하고자 하는 메시징 아키텍처를 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/74212d1d-1a7a-4c38-9195-fa1a97e55bd1.png)

그림 2.3: CC의 주요 애플리케이션 아키텍처

이 흐름은 앞의 다이어그램에서 강조 표시된 것처럼 10단계로 설명할 수 있습니다.

1. 고객이 CC의 모바일 애플리케이션을 사용하여 택시를 예약합니다. 이제 모바일 애플리케이션에서 2. 애플리케이션 서비스로 요청이 전송됩니다. 이 요청에는 고객이 예약하려는 여행에 대한 정보가 포함됩니다. Application Service는 요청을 데이터베이스에 저장합니다.
3. Application Service는 RabbitMQ의 큐로의 이동에 대한 정보가 포함된 메시지를 추가합니다.
4. 연결된 택시 차량은 메시지(예약요청)를 구독합니다.
5. 택시는 RabbitMQ에 다시 메시지를 보내 고객에게 응답합니다.
6. 애플리케이션 서비스가 메시지를 구독합니다.
7. 다시 말하지만, 응용 프로그램 서비스는 정보를 데이터베이스에 저장합니다.
8. 애플리케이션 서비스는 고객에게 정보를 전달합니다.
9. 택시 앱은 지정된 간격으로 택시의 지리적 위치를 RabbitMQ로 자동 전송하기 시작합니다.
10. 그런 다음 택시의 위치가 WebSocket을 통해 고객의 모바일 애플리케이션에 직접 전달되어 택시가 언제 도착하는지 알 수 있습니다.

고객이 택시를 요청하고(래빗MQ에 메시지가 게시됨) 택시 기사가 요청을 받는(메시지가 RabbitMQ에서 소비).


## RabbitMQ에 견고한 연결 설정

1장에서 언급했듯이 애플리케이션 서버와 RabbitMQ 간에 물리적 네트워크 연결이 설정되어야 합니다. AMQP 연결은 초기 인증, IP 확인 및 네트워킹을 포함하여 기본 네트워킹 작업을 수행하는 클라이언트와 브로커 간의 링크입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/e959981b-8e51-459f-adb4-f27147bf43aa.png)

그림 2.4: 애플리케이션과 RabbitMQ 간의 AMQP 연결

각 AMQP 연결은 기본 채널 세트를 유지 관리합니다. 채널은 새로운 TCP 스트림을 재인증하고 열 필요가 없으므로 연결을 재사용하여 리소스 효율성을 높입니다.

다음 다이어그램은 애플리케이션과 RabbitMQ 간의 연결 내의 채널을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/43ce057b-af5f-4164-95ff-ee965b9a21b7.png)

그림 2.5: 채널을 통해 리소스를 보다 효율적으로 사용할 수 있습니다.

채널 생성과 달리 연결 생성은 데이터베이스 연결과 마찬가지로 비용이 많이 드는 작업입니다. 일반적으로 데이터베이스 연결은 풀링되어 풀의 각 인스턴스가 단일 실행 스레드에서 사용됩니다. AMQP는 다중화된 채널을 통해 여러 스레드에서 단일 연결을 사용할 수 있다는 점에서 다릅니다.

AMQP 연결을 위한 핸드셰이크 프로세스에는 최소 7개의 TCP 패킷이 필요하며 TLS를 사용할 때는 그 이상이 필요합니다. 필요한 경우 채널을 더 자주 열고 닫을 수 있습니다.

- AMQP 커넥션: 7 TCP 패키지
- AMQP 채널: 2 TCP 패키지
- AMQP 게시: 1 TCP 패키지(더 큰 메시지의 경우)
- AMQP 닫기 채널: 2 TCP 패키지
- AMQP 닫기 컨넥션: 2 TCP 패키지
- 총 14-19개 패키지(Acks 포함)

다음 다이어그램은 연결 및 채널에 전송되는 정보의 개요를 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/f23a471f-89ff-43ca-a643-e779f4492462.png)

Fig 2.6: The handshake process for an AMQP connection

Application Service와 RabbitMQ 간에 단일 장기 연결을 설정하는 것이 좋은 시작입니다.

사용할 프로그래밍 언어와 클라이언트 라이브러리를 결정해야 합니다. 이 책의 처음 몇 가지 예제는 Ruby로 작성되었으며 클라이언트 라이브러리 Bunny(https://github.com/ruby-amqp/bunny)를 사용하여 메시지를 게시하고 사용합니다. Ruby는 익숙하지 않더라도 읽고 이해하기 쉬운 언어입니다.

종종 연결 문자열이라고 하는 특정 연결 끝점을 사용하도록 응용 프로그램을 구성해야 합니다. 예를 들어, 호스트와 포트. 연결 문자열에는 연결을 설정하는 데 필요한 정보가 포함되어 있습니다. AMQP에 할당된 포트 번호는 5672입니다. TLS/SSL로 암호화된 AMQP는 AMQPS를 통해 사용할 수 있습니다. 포트 5671이 할당된 AMQP 프로토콜의 보안 버전입니다.

라이브러리는 대상 IP 주소 및 포트에 대한 TCP 연결을 여는 요소입니다. 연결 매개변수는 RABBITMQ_URI라는 코드에 환경 변수에 URI 문자열로 추가되었습니다. AMQP URI에 대한 URI 표준은 없지만 이 형식이 널리 사용됩니다.
```
 RABBITMQ_URI="amqp://user:password@host/vhost"
```
According to the Ruby (Bunny) documentation, connecting to RabbitMQ is simple. The code for this is divided into code blocks, and can be found later in this chapter:

1. Add the username, the password, and the vhost that were set up in Chapter 1, A Rabbit Springs to Life, and then add the string to an environment variable on the machine:
```
RABBITMQ_URI="amqp://cc-dev:taxi123@localhost/cc-dev-vhost"
```
2. Require the bunny client library:
```
# Require client library
require "bunny"
```

3. Read the connection URI from the environment variable and start a connection:
```
connection = Bunny.new ENV['RABBITMQ_URI']
# Start a session with RabbitMQ 
connection.start
```

This seems straightforward so far, but CC requires production-grade code that can gracefully handle failures. What if RabbitMQ is not running? Clearly, it is bad if the whole application is down. What if RabbitMQ needs to be restarted? CC wants its application to recover gracefully if any issues occur. In fact, CC wants its application to keep functioning, regardless of whether the whole messaging subsystem is working or not. The user experience must be smooth and easy to understand, as well as reliable.

In summary, the behavior CC wishes to achieve is as follows:

- If the connection to RabbitMQ is lost, it should reconnect by itself.
- If the connection is down, sending or fetching messages should fail gracefully.

When the application connects to the broker, it needs to handle connection failures. No network is reliable all the time and misconfigurations and mistakes happen; the broker might be down, and so on. While not automatic, in this case, error detection should happen early in the process.

To handle TCP connection failures in Bunny, it is necessary to catch the exception:
```
begin
  connection = Bunny.new ENV['RABBITMQ_URI']
  connection.start
rescue Bunny::TCPConnectionFailed => e
  puts "Connection to server failed"
end
```

Detecting network connection failures is nearly useless if an application cannot recover from them. Recovery is an important part of error handling.

Some client libraries offer automatic connection recovery features that include consumer recovery. Any operation that's attempted on a closed channel will fail with an exception. If Bunny detects a TCP connection failure, it will try to reconnect every 5 seconds with no limit regarding the number of reconnection attempts. It is possible to disable automatic connection recovery by adding automatic_recovery => false to Bunny.new. This setting should only be used if you're reconnecting in some other way, or when testing the connection string.

Messages can be sent across languages, platforms, and operating systems. You can choose from a number of different client libraries for different languages. There are lots of client libraries out there, but here are some that are recommended:
```
Python: Pika
Node.js: amqplib
PHP: php-amqplib
Java: amqp-client
Clojure: Langohr
```
This section has shown how CC manages to establish a connection to RabbitMQ. We demonstrated why a long-lived connection is recommended and how to handle some common errors. Now, it's time to create a channel inside the connection.

### Working with channels

Every AMQP protocol-related operation occurs over a channel. The channel instances are created by the connection instance. As described, a channel is a virtual (AMQP) connection inside the (TCP) connection. All operations performed by a client happen on a channel, queues are declared on channels, and messages are sent over channels.

A channel never exists on its own; it's always in the context of a connection:
```
# Declare a channel
channel = connection.create_channel
```

Channels in a connection are closed once the connection is closed or when a channel error occurs. Client libraries allow us to observe and react to channel exceptions.

More exceptions are usually thrown at a channel level than at the connection level. Channel-level exceptions often indicate errors the application can recover from, such as, when it has no permissions, or when attempting to consume from a deleted queue. Any attempted operation on a closed channel will also fail with an exception.

Even though channel instances are technically thread-safe, it is strongly recommended to avoid having several threads that are using the same channel concurrently.

CC is now able to connect to a RabbitMQ broker, open a channel, and issue a series of commands, all in a thread-safe and exception-safe manner. It's now time to build on this foundation!

### 택시 요청 도구 구축

이제 메시지 흐름을 구축할 차례입니다.

먼저 고객은 모바일 애플리케이션에서 애플리케이션 서비스로 간단한 HTTP 요청을 보냅니다. 이 메시지에는 타임스탬프, 발신자 및 수신자 ID, 목적지 및 요청된 택시 ID와 같은 메타 정보가 포함됩니다.

메시지 흐름은 다음과 같습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/c4c3407a-a481-4a0c-9d1c-6f0b2adb7b4c.png)

그림 2.7: CC 메인 애플리케이션의 프론트엔드/백엔드 상호작용

Application Service는 모든 데이터가 이후 상태의 데이터 분석 스크립트에서 볼 수 있도록 정보를 데이터베이스에 저장합니다.

데이터가 데이터베이스에 저장되는 방법은 이 장의 주요 사례가 아니기 때문에 이 예제에서 다루지 않습니다. 가장 쉬운 방법은 Application Service가 데이터베이스에 정보를 추가하도록 허용하는 것입니다. 또 다른 옵션은 애플리케이션 서비스를 오프로드하고 새 메시지를 데이터베이스와 애플리케이션 서비스 사이의 메시지 대기열에 넣고 다른 서비스가 해당 메시지를 구독하고 처리하도록 하는 것입니다. 즉, 데이터베이스에 저장합니다.
모바일 장치, Application Service 및 RabbitMQ 간의 흐름은 다음 다이어그램에 나와 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/09a3d121-718e-4fbc-86de-3f58c18094fa.png)

그림 2.8: 모바일 장치, 애플리케이션 서비스 및 RabbitMQ 간의 흐름

우리의 주요 흐름과 관련하여 1장, A Rabbit Springs to Life에서 AMQP에 대한 논의는 메시지가 소비될 대기열로 라우팅된 후 교환에 게시되는 방법에 대해 자세히 설명했습니다.

라우팅 전략은 메시지가 라우팅될 대기열을 결정합니다. 라우팅 전략은 라우팅 키(자유 형식 문자열)와 잠재적으로 메시지 메타 정보를 기반으로 결정합니다. 라우팅 키는 교환이 메시지를 라우팅하는 방법을 결정하는 데 사용하는 주소로 생각하십시오. 또한 전자에서 후자로 메시지가 흐를 수 있도록 교환과 대기열 간의 바인딩이 필요합니다.

이제 직접 교환을 살펴보겠습니다.

### 직접 교환

직접 교환은 메시지 라우팅 키를 기반으로 큐에 메시지를 전달합니다. 메시지는 바인딩 루틴 키가 메시지의 라우팅 키와 일치하는 큐로 이동합니다.

CC는 차량이 2대뿐이므로 한 명의 고객이 한 명의 기사에게 택시를 요청할 수 있는 간단한 통신 시스템으로 시작합니다. 이 경우 하나의 메시지가 해당 드라이버의 받은 편지함 역할을 하는 대기열로 라우팅되어야 합니다. 따라서 사용할 교환 라우팅 전략은 다음 다이어그램과 같이 메시지가 생성될 때 사용되는 라우팅 키와 대상 큐 이름을 일치시키는 직접 전략입니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/40c44f43-019d-4d0d-8880-d292a31a6a19.png)

그림 2.9: 특정 큐에 대한 직접 교환 경로 메시지

직접 교환의 사용 사례는 다음과 같습니다.

1. 고객이 택시라는 이름의 택시를 주문합니다.1. HTTP 요청은 고객의 모바일 애플리케이션에서 애플리케이션 서비스로 전송됩니다.
2. 애플리케이션 서비스는 라우팅 키 택시.1을 사용하여 RabbitMQ에 메시지를 보냅니다. 메시지 라우팅 키는 대기열의 이름과 일치하므로 메시지는 택시 1 대기열에서 끝납니다.

다음 다이어그램은 직접 교환 메시지 라우팅이 발생하는 방법을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/71ddd244-042c-4d10-a2a0-af0476d1a2cd.png)

그림 2.10: 라우팅 키를 기반으로 특정 큐로 라우팅 메시지를 직접 교환

이것은 가장 효율적인 확장 방법이 아닐 수 있습니다. 사실 CC에 차가 더 많아지면 바로 검토하겠지만, 애플리케이션을 빠르게 시작하고 실행하는 가장 쉬운 방법입니다.

CC가 초기 애플리케이션으로 생성한 첫 번째 코드를 따라하면서 동시에 다양한 개념에 대해 알아보겠습니다. 코드 블록의 시작 부분에 있는 코드는 연결 및 채널 섹션에서 가져왔습니다.

1. 토끼 클라이언트 라이브러리가 필요합니다.
2. 환경 변수에서 URI 연결을 읽고 연결을 시작합니다.
3. RabbitMQ와 통신 세션을 시작합니다.
4. Taxi.1 대기열을 선언합니다.
5. Taxi.1 직접 교환을 선언합니다.
6. Taxi.1 라우팅 키를 사용하여 Taxi.1 대기열을 Taxi-Direct 교환기에 바인딩합니다.

```ruby
# 1. Require client library
require "bunny"

# 2. Read RABBITMQ_URI from ENV
connection = Bunny.new ENV["'RABBITMQ_URI"]

# 3. Start a communication session with RabbitMQ
connection.start
channel = connection.create_channel

def on_start(channel)
 # 4. Declare a queue for a given taxi
 queue = channel.queue("taxi.1", durable: true)
 # 5. Declare a direct exchange, taxi-direct
 exchange = channel.direct("taxi-direct", durable: true, auto_delete: true)

 # 6. Bind the queue to the exchange
 queue.bind(exchange, routing_key: "taxi.1")

 # 7. Return the exchange
 exchange
end

exchange = on_start(channel)
```

전송되는 모든 메시지에 대해 대기열 및 교환을 선언하는 것은 약간 과도하고 불필요하므로 애플리케이션 설정을 처리하는 메서드를 만드는 것이 좋습니다. 이것은 연결을 생성하고 대기열, 교환 등을 선언하는 메서드여야 합니다. 이 예제의 메서드는 단순히 on_start라고 하며 큐를 선언하고 교환을 큐에 바인딩합니다.

거래소에 게시될 때 해당 거래소가 존재하지 않으면 예외가 발생합니다. 교환이 이미 존재하는 경우 아무 것도 하지 않습니다. 그렇지 않으면 실제로 생성됩니다. 이것이 애플리케이션이 시작될 때마다 또는 메시지를 게시하기 전에 큐를 선언하는 것이 안전한 이유입니다.

채널은 예외에 의해 종료됩니다. CC의 경우 존재하지 않는 교환기로 전송하면 예외가 발생할 뿐만 아니라 오류가 발생한 채널도 종료됩니다. 종료된 채널을 사용하려는 모든 후속 코드도 실패합니다.
직접 유형을 사용하는 것 외에도 CC는 지속 유형, autoDelete 및 교환의 인수 속성을 구성했습니다. 이 교환은 RabbitMQ를 다시 시작한 후나 구성에 사용된 값을 설명하는 사용하지 않을 때 사라지지 않아야 합니다.

교환 선언은 교환 속성이 ​​동일한 경우에만 멱등원입니다. 다른 속성을 가진 이미 존재하는 교환을 선언하려는 시도는 실패합니다. 항상 교환 선언에서 일관된 속성을 사용하십시오. 속성을 변경하는 경우 새 속성으로 선언하기 전에 교환을 삭제하십시오. 동일한 규칙이 대기열 선언에 적용됩니다.
거래소를 생성한 후 택시 대기열이 생성되어 이에 바인딩됩니다.

대기열은 교환과 유사한 접근 방식으로 선언되지만 다음과 같이 속성이 약간 다릅니다.

- durable: True – the queue must stay declared, even after a broker restart.
- autoDelete: False – keep the queue, even if it's not being consumed anymore.
- exclusive: False – this queue should be able to be consumed by other connections (several application servers can be connected to RabbitMQ and accessed from different connections).
- arguments: Null – no need to custom configure the queue.

큐는 자체 이름을 라우팅 키로 사용하여 Exchange에 바인딩되므로 직접 라우팅 전략이 메시지를 라우팅할 수 있습니다. 이 작업이 완료되면 택시 직접 교환기에 메시지를 게시하면 게시된 라우팅 키와 이름이 일치하는 택시 대기열에 실제로 메시지가 배달됩니다.

교환에 바인딩된 큐가 없거나 라우팅 전략이 일치하는 대상 큐를 찾을 수 없는 경우 교환에 게시된 메시지는 자동으로 삭제됩니다. 옵션으로 다음 장에서 볼 수 있듯이 라우팅할 수 없는 메시지가 삭제될 때 알림을 받을 수 있습니다.

다시 말하지만, 동일한 속성을 사용하는 경우 이러한 작업은 멱등적이므로 대기열을 안전하게 선언하고 교환에 바인딩할 수 있습니다.

이 장에서 직접 교환을 다루었지만 AMQP 0-9-1 브로커는 네 가지 유형의 교환을 제공합니다. 대기열과 매개변수 간의 바인딩 설정에 따라 이러한 교환은 메시지를 다르게 라우팅합니다. 다음 장에서는 다른 유형의 교환에 대해 자세히 살펴봅니다. 지금은 각각에 대한 간단한 설명이 있습니다.

- Fanout: Messages are routed to all queues bound to the fanout exchange.
- Topic: Wildcards must form a match between the routing key and the binding's specific routing pattern.
- Headers: Use the message header attributes for routing.

Now, it's time to send our first message to RabbitMQ!


## Sending the first messages

The basic concept and initial setup has already been covered, so let's jump in and send the first messages!

First, let's take a look at the order_taxi method, which is in charge of sending messages for the initial car request:

```ruby
def order_taxi(taxi, exchange)
  payload = "example-message"
  message_id = rand
 exchange.publish(payload,
    routing_key: taxi,
    content_type: "application/json",
    content_encoding: "UTF-8",
    persistent: true,
    message_id: message_id)
end

exchange = on_start(channel)
order_taxi("taxi.1", exchange)
```

order_taxi는 사용자가 택시를 주문할 때마다 호출됩니다. 받는 사람이 시스템에 로그인했다는 보장이 없으므로 보낸 사람에 관한 한 대상 대기열이 있는지 확인하는 것은 불가능합니다. 가장 안전한 경로는 전송된 모든 메시지에 대해 대기열을 선언하는 것입니다. 이 선언 작업은 멱등적이므로 대기열이 이미 존재하는 경우 아무 작업도 수행하지 않습니다. 처음에는 이상하게 보일 수 있지만 메시지가 손실되지 않도록 하려면 받는 사람의 대기열이 있는지 확인하는 것은 보낸 사람의 책임입니다.

이는 이벤트 간에 강력한 발생 이전 관계가 없을 때 AMQP에서 일반적인 패턴입니다. 재선언이 갈 길입니다. 반대로 확인 후 행동 패턴은 권장되지 않습니다. AMQP가 사용되는 일반적인 분산 환경에서 교환 또는 대기열의 존재 여부를 확인하는 것은 성공을 보장하지 않습니다.

메시지를 게시하는 방법은 매우 간단합니다. 거래소를 향해 publish를 호출합니다. 그런 다음 큐 이름을 라우팅 키로 사용하고(직접 라우팅에 따라) 실제 메시지 페이로드를 나타내는 바이트 배열을 사용합니다. 다음을 포함할 수 있는 몇 가지 선택적 메시지 속성을 추가할 수 있습니다.

- content_type (string): A message is published and consumed as a byte array, but nothing really says what these bytes represent. In the current situation, both publishers and consumers are in the same system, so it could be assumed that the content type is expected. That being said, always specify the content type so that messages are self-contained; whichever system ends up receiving or introspecting a message will know for sure what the byte array it contains represents.

- content_encoding (string): A specific encoding (UTF-8) is used when serializing string messages into byte arrays so that they can be published. Again, in order for the messages to be self-explicit, provide all the necessary meta-information to allow them to be read.

- message_id (string): As demonstrated later in this book, message identifiers are an important aspect of traceability in messaging and distributed applications. In the example is a random message id generated.

- persistent (boolean): Specifies if the message should be persisted to disk or not.

교환 및 대기열 지속성을 메시지 지속성과 혼동하지 마십시오. 지속성 대기열에 저장된 비지속적 메시지는 브로커가 다시 시작된 후 사라지고 빈 대기열만 남게 됩니다.

또한 비지속 대기열의 지속 메시지는 브로커가 다시 시작된 후 사라지고 빈 대기열이 남게 됩니다.

대기열을 지속성으로 선언하고 메시지 전달 모드를 지속성으로 설정하여 메시지가 손실되지 않도록 합니다.
그러나 RabbitMQ와의 연결이 끊어진 경우와 같이 메시지 전송이 실패하면 어떻게 될까요?

비영구 전달 모드를 사용하는 이유는 무엇입니까? RabbitMQ와 같은 메시지 브로커의 요점은 메시지가 손실되지 않도록 보장하는 것 아닌가요? 이것은 사실이지만 이 보증이 완화될 수 있는 상황이 있습니다. 게시자가 중요하지 않은 많은 메시지를 브로커에 폭격하는 시나리오를 고려하십시오. 여기서 비영구적 전달을 사용하면 RabbitMQ가 디스크에 지속적으로 액세스할 필요가 없으므로 이 경우 더 나은 성능을 제공할 수 있습니다.
더 진행하기 전에 AMQP 메시지의 구조를 살펴보겠습니다.

## AMQP 메시지 구조

다음 스크린샷은 AMQP 메시지의 구조를 보여주며 방금 사용한 네 가지 AMQP 메시지 속성과 몇 가지 새로운 속성을 포함합니다. 이 다이어그램은 필드의 사양 이름을 사용하며 각 언어 구현은 유효한 이름이 될 수 있도록 약간 이름을 바꿉니다. 예를 들어, content-type은 Java에서는 contentType이 되고 Ruby에서는 content_type이 됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/9b3fe337-567a-459c-97b3-1226398fa7dd.png)

Fig 2.11: Properties of an AMQP message

예약된 속성을 제외하고 이러한 모든 속성은 자유롭게 사용할 수 있으며 달리 지정하지 않는 한 AMQP 브로커에서 무시합니다. RabbitMQ의 경우 브로커가 지원하는 유일한 필드는 연결을 설정한 브로커 사용자의 이름과 일치하는지 확인하는 사용자 ID 필드입니다. 표준 속성이 요구 사항에 맞지 않는 경우 headers 속성을 사용하여 추가 키-값 쌍을 추가할 수 있는 방법에 주목하세요.

다음 섹션에서는 메시지가 사용되는 방식을 설명합니다.

### 메시지 소비
이제 CC 이면의 애플리케이션 아키텍처 섹션에서 볼 수 있는 CC의 메인 아키텍처의 4단계인 메시지 검색을 담당하는 방법에 주목해 보자.

여기에서 택시 애플리케이션은 정기적인 간격으로 새 메시지에 대한 대기열을 확인할 수 있습니다. 이것은 소위 동기식 접근 방식입니다. 이는 다음 다이어그램과 같이 대기 중인 모든 메시지가 큐에서 제거될 때까지 폴 요청 처리를 담당하는 애플리케이션 스레드를 유지하는 것을 의미합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/2c2137d3-3784-4d43-90d1-4ce304c5d344.png)

그림 2.12: 브로커에서 새 메시지를 요청하는 클라이언트

메시지에 대해 백엔드를 정기적으로 폴링하는 프론트엔드는 곧 부하 측면에서 부담을 느끼기 시작하며, 이는 전체 솔루션이 성능 저하를 겪기 시작한다는 것을 의미합니다.

대신 CC는 서버 푸시 방식을 선호하는 솔루션을 구축하기로 현명하게 결정합니다. 아이디어는 브로커에서 클라이언트로 메시지를 서버 푸시하는 것입니다. 다행히 RabbitMQ는 메시지를 수신하는 두 가지 방법을 제공합니다. 폴링 기반 basic.get 메서드와 푸시 기반 basic.consume 메서드가 있습니다. 다음 다이어그램에 나와 있는 것처럼 메시지는 소비자에게 푸시됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/152412b4-01d6-4e4a-ac95-e9fc397b1f52.png)

그림 2.13: 브로커의 소비자 구독 메시지

subscribe 메서드는 큐에 소비자를 추가한 다음 메시지 배달을 수신하도록 구독합니다.

소비자가 기본 GET 작업을 사용하는 대신 대기열에서 메시지를 사용하는지 확인합니다. basic.get 명령은 리소스와 관련하여 비교적 비용이 많이 듭니다.

구독을 사용하면 새 메시지가 준비되고 클라이언트에 가용성이 있을 때 메시지가 브로커에서 클라이언트로 전달됩니다. 이것은 일반적으로 메시지의 원활한 처리를 허용합니다. 또한 구독을 사용한다는 것은 소비자가 선언된 채널이 사용 가능한 한 또는 클라이언트가 취소할 때까지 소비자가 연결되어 있음을 의미합니다.

메시지 프로세스는 마치 아무 일도 일어나지 않는 것처럼 매끄럽고 쉽게 실행되고 있습니다! 물론 프로세스의 일부가 예상대로 실행되었는지 또는 계획대로 실행되지 않았는지 여부를 확인 및/또는 부정적으로 확인하기 위해 경고가 실행될 때까지입니다.

### 승인 및 부정적인 승인

RabbitMQ는 메시지가 예상대로 소비자에게 전송된다는 측면에서 메시지가 성공한 것으로 간주될 수 있는 시점을 알아야 합니다. 그런 다음 브로커가 응답을 받으면 브로커는 대기열에서 메시지를 삭제해야 합니다. 그렇지 않으면 대기열이 오버플로됩니다. 클라이언트는 메시지를 수신하거나 소비자가 메시지를 완전히 처리했을 때 메시지를 확인(확인)하여 브로커에 응답할 수 있습니다. 두 경우 모두 메시지가 승인되면 대기열에서 제거됩니다.

따라서 처리가 완료된 경우 또는 메시지가 비동기식으로 처리되는 경우 메시지 손실 위험이 없는 경우에만 메시지를 확인하는 것은 소비자의 몫입니다.

메시지가 영구적으로 손실될 수 있는 상황(예: 작업자 충돌, 예외 등)을 피하기 위해 소비 애플리케이션은 메시지가 완전히 끝날 때까지 메시지를 확인하지 않아야 합니다.

응용 프로그램이 처리에 실패했거나 현재 완료할 수 없음을 브로커에 표시하면 응용 프로그램에서 메시지를 거부합니다. Nack 또는 부정 승인은 RabbitMQ에 메시지가 지시된 대로 처리되지 않았음을 알려줍니다. 기본적으로 Nack된 메시지는 다른 시도를 위해 대기열로 다시 전송됩니다.

확인은 3장, 여러 택시 기사에게 메시지 보내기에서 자세히 다룹니다.
Ready? Set? Time to RUN, Rabbit!

### Running the code

Now, it's time to set up some code for the consumer. You'll be able to recognize most of this code from the previous section, Sending the first messages:

Require client libraries.
Read RABBITMQ_URI from ENV.
Start a communication session with RabbitMQ.
Declare a queue for a given taxi.
Declare a direct exchange, taxi-direct.
Bind the queue to the exchange.
Subscribe to the queue.
What follows is the code that's required for the initial consumer setup:
```rb
# example_consumer.rb
# 1. Require client library
require "bunny"

# 2. Read RABBITMQ_URI from ENV
connection = Bunny.new ENV["RABBITMQ_URI"]

# 3. Start a communication session with RabbitMQ
connection.start
channel = connection.create_channel

# Method for the processing
def process_order(info)

  puts "Handling taxi order"
  puts info
  sleep 5.0
  puts "Processing done"
end

def taxi_subscribe(channel, taxi)
  # 4. Declare a queue for a given taxi
  queue = channel.queue(taxi, durable: true)

  # 5. Declare a direct exchange, taxi-direct
  exchange = channel.direct("taxi-direct", durable: true, auto_delete: true)

  # 6. Bind the queue to the exchange
  queue.bind(exchange, routing_key: taxi)

  # 7. Subscribe from the queue
  queue.subscribe(block: true, manual_ack: false) do |delivery_info, properties, payload|
    process_order(payload)
  end
end

taxi = "taxi.1"
taxi_subscribe(channel, taxi)    
```

Here, two flags were added to the subscribe method that need to be explained. Let's look at them in detail:

- **block** (Boolean, default false): Should the call block the calling thread? This option can be useful for keeping the main thread of a script alive. It is incompatible with automatic connection recovery and is not generally recommended.
- **manual_ack** (Boolean, default false): In CC's case, since the risk of losing a message is acceptable during this phase, the system does not manually acknowledge messages. Instead, it informs the broker to consider them as acknowledged as soon as it fetches them (more on manual acknowledgment later in this book).

And that's it! CC now has a working order request inbox ready to be tested. Next, we'll look at the management console when activated taxis are running.

### Running the application

With the application running and a server connected to RabbitMQ, the following established connections can be seen from the management console:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/90a38a53-a625-4f9f-a15f-b3a8c4ff7c1a.png)

Fig 2.14: The management console provides connection information

Notice how the upstream and downstream network throughputs are clearly represented, and that the channels that get opened and closed very quickly are hard to see from the management console. So, let's look at the following exchanges:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/91175a73-136f-44c6-b528-217c23549537.png)

Fig 2.15: The taxi-direct direct exchange showing up in the management console

사용자 교환 및 들어오고 나가는 메시지 비율이 관리 콘솔에 표시됩니다. 들어오는 속도만큼 빠르게 소비되고 있다는 사실은 현재 아키텍처가 CC의 요구 사항에 충분하고 메시지가 쌓이지 않는다는 좋은 신호입니다. 그러나 코드로 생성되지 않은 다른 모든 교환은 무엇이며 어디에서 오는 것입니까? (AMQP 기본값)으로 표시되는 이름 없는 교환 및 amq로 시작하는 이름을 가진 모든 교환. AMQP 사양에 의해 정의되므로 기본적으로 RabbitMQ에서 제공해야 합니다. 이제 대기열은 어떻습니까? 살펴보겠습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/9e8a32d8-41e2-46fc-a8fb-79fd4cb2acb1.png)

Fig 2.16: Each client-to-taxi inbox queue is visible in the management console

예상대로 택시당 하나의 대기열과 멋진 사용 통계가 있습니다. 메시지 승인이 작동하는 방식을 고려할 때 ack 열이 비어 있는 방법에 주목하세요. 이는 놀라운 일이 아닙니다. 큐는 메시지를 수신하면서 RabbitMQ에게 메시지를 승인하지 않을 것임을 알리므로 메시지 승인과 관련된 활동이 없습니다.


RAM이 충분하면 RabbitMQ는 수백 개의 대기열과 바인딩을 문제 없이 처리할 수 있으므로 여러 대기열이 문제가 되지 않습니다.
아키텍처와 구현에 대해 확신을 갖고 있는 CC는 클라이언트-택시 주문 하위 시스템을 출시합니다. 클라이언트는 요청을 보낼 수 있고 택시는 요청을 처리할 수 있습니다.

CC는 두 대의 새로운 친환경 자동차로 회사를 빠르게 확장합니다. 이전 솔루션과 마찬가지로 클라이언트는 특정 드라이버에 주문 요청 메시지를 보내야 합니다. 이제 새로운 기능이 요청되었습니다. 바로 택시 차량 그룹에 메시지를 보낼 수 있는 기능입니다. 고객이 일반택시와 친환경택시를 선택할 수 있어야 합니다. CC가 RabbitMQ의 힘을 통해 이 새로운 기능을 구현하는 방법을 살펴보겠습니다.


## Adding topic messages

CC의 애플리케이션을 사용하면 택시가 관심 주제를 등록하여 그룹으로 구성할 수 있습니다. 롤아웃할 새로운 기능을 통해 클라이언트는 특정 주제 내의 모든 택시에 주문 요청을 보낼 수 있습니다. 이 기능이 특정 교환 라우팅 규칙과 일치하는 것으로 나타났습니다. 놀랍게도 topic이라고 불리는 것은 아닙니다! 이러한 유형의 교환을 통해 메시지의 라우팅 키와 일치하는 라우팅 키로 바인딩된 모든 대기열로 메시지를 라우팅할 수 있습니다. 따라서 메시지를 최대 하나의 큐로 라우팅하는 직접 교환과 달리 토픽 교환은 메시지를 여러 큐로 라우팅할 수 있습니다. 주제 기반 라우팅이 적용될 수 있는 다른 두 가지 예는 교통 경고 방송과 같은 위치별 데이터 또는 여행 가격 업데이트입니다.

라우팅 패턴은 점으로 구분된 여러 단어로 구성됩니다. 따라야 할 모범 사례는 가장 일반적인 요소에서 가장 구체적인 요소(예: news.economy.usa 또는 europe.sweden.stockholm)로 라우팅 키를 구성하는 것입니다.
토픽 교환은 엄격한 라우팅 키 일치를 지원하며 각각 정확히 한 단어와 0개 이상의 단어에 대한 자리 표시자로 * 및 #을 사용하여 와일드카드 일치를 수행합니다.

다음 다이어그램은 주제 교환이 CC의 애플리케이션에서 사용되는 방법을 보여줍니다. 단일 받은 편지함 대기열이 변경되지 않고 사용자의 관심을 반영하는 추가 바인딩을 통해 단순히 주제 교환에 연결되는 방법에 주목하세요.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781789131666/files/assets/3f964e6f-517b-466a-a0eb-9965c3fb05a2.png)

Fig 2.17: The topic exchange sending thematic messages to eco queues

Because the same inbox is used for everything, the code that's already in place for fetching messages doesn't need to be changed. In fact, this whole feature can be implemented with only a few additions. The first of these additions takes care of declaring the topic exchange in the existing on_start method, as follows:

```ruby
def on_start(channel)
  # Declare and return the topic exchange, taxi-topic
  channel.topic("taxi-topic", durable: true, auto_delete: true)
end
```
There's nothing really new or fancy here; the main difference is that this exchange is called taxi-topic and is a topic type of exchange. Sending a message is even simpler than with the client-to-taxi feature because there is no attempt to create the addressee's queue. It wouldn't make sense for the sender to iterate through all the users to create and bind their queues, as only users already subscribed to the target topic at the time of sending will get the message, which is exactly the expected functionality. The order_taxi method is listed here:

```ruby
# Publishing an order to the exchange
def order_taxi(type, exchange)
  payload = "example-message"
  message_id = rand
  exchange.publish(payload,
                   routing_key: type,
                   content_type: "application/json",
                   content_encoding: "UTF-8",
                   persistent: true,
                   message_id: message_id)
end

exchange = on_start(channel)
# Order will go to any eco taxi
order_taxi('taxi.eco', exchange) 
# Order will go to any eco taxi
order_taxi('taxi.eco', exchange) 
# Order will go to any taxi
order_taxi('taxi', exchange) 
# Order will go to any taxi
order_taxi('taxi', exchange) 
```

The difference is that, now, messages are published to the taxi-topic exchange. The rest of the code that creates and publishes the message is exactly the same as the client-to-taxi messaging. Lastly, information needs to be added when a new taxi subscribes or unsubscribes from certain topics:

```ruby
# example_consumer.rb

def taxi_topic_subscribe(channel, taxi, type)
  # Declare a queue for a given taxi
  queue = channel.queue(taxi, durable: true)

  # Declare a topic exchange
  exchange = channel.topic('taxi-topic', durable: true, auto_delete: true)

  # Bind the queue to the exchange
  queue.bind(exchange, routing_key: type)

  # Bind the queue to the exchange to make sure the taxi will get any order
  queue.bind(exchange, routing_key: 'taxi')

  # Subscribe from the queue
  queue.subscribe(block:true,manual_ack: false) do |delivery_info, properties, payload|
    process_order(payload)
  end
end

taxi = "taxi.3"
taxi_topic_subscribe(channel, taxi, "taxi.eco.3")
```

taxi.3 is the new environmentally friendly taxi, now ready to receive orders from clients that want an environmentally friendly car.

AMQP 사양은 대기열의 현재 바인딩을 검사하는 수단을 제공하지 않으므로 택시의 관심 주제 변경을 반영하기 위해 더 이상 필요하지 않은 바인딩을 제거하고 반복할 수 없습니다. 어쨌든 이 상태를 유지하려면 애플리케이션이 필요하기 때문에 이것은 심각한 문제가 아닙니다.

RabbitMQ 관리 콘솔은 AMQP 사양에서 다루지 않는 다른 많은 기능 중에서 대기열 바인딩 내부 검사를 수행하는 데 사용할 수 있는 REST API를 제공합니다. 이에 대한 자세한 내용은 다음 장에서 설명합니다.
이 새 코드를 사용하면 모든 것이 예상대로 작동합니다. 새로운 클라이언트-택시 주문은 이전 메시지와 동일한 받은 편지함 대기열에 도착하기 때문에 코드를 변경할 필요가 없습니다. 택시 차량은 주제별 메시지를 올바르게 보내고 받으며 이 모든 것이 최소한의 변경으로 발생하며 대기열 수는 증가하지 않습니다. 관리 콘솔에 연결되면 Exchanges 탭을 클릭합니다. 눈에 보이는 유일한 차이점은 새로운 교환 주제입니다. 즉, 택시 주제입니다.

## 요약

이 장에서는 RabbitMQ에 연결하는 방법과 주문 메시지를 보내고 받는 방법에 대해 설명했습니다. 자동차 주문 시스템이 성공적으로 만들어졌고 CC의 클라이언트-택시 및 클라이언트-택시 기능의 맥락에서 직접 및 주제 교환이 시작되었습니다.

Complete Car가 성장함에 따라 택시 애플리케이션의 새로운 기능에 대한 수요도 증가합니다. 사용자 요구를 충족하는 CC의 다음 단계는 무엇입니까? 다음 장에서는 채널 및 대기열을 사용하여 앱의 기능을 확장하는 방법을 설명합니다.

