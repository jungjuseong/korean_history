# 3 Spring Boot로 마이크로서비스 구축

이 장에서는 다음을 다룹니다.

- 마이크로서비스가 클라우드 아키텍처에 어떻게 적용되는지 이해
- 비즈니스 도메인을 마이크로서비스 세트로 분해
- 마이크로서비스 앱 구축에 대한 관점 이해
- 마이크로서비스를 사용하지 말아야 할 때 배우기
- 마이크로서비스 구현

마이크로서비스를 성공적으로 설계하고 구축하려면 범죄 목격자를 인터뷰하는 경찰 형사처럼 마이크로서비스에 접근해야 합니다. 각 증인은 동일한 사건을 목격하지만 범죄에 대한 해석은 배경, 그들에게 중요한 것(예: 동기 부여), 사건을 목격한 순간에 어떤 환경적 압력을 받았는지에 따라 결정됩니다. 증인은 각자 자신이 중요하다고 생각하는 것에 대해 나름대로의 관점(편견)을 가지고 있습니다.

진실을 밝히기 위해 노력하는 성공적인 경찰과 마찬가지로 성공적인 마이크로서비스 아키텍처를 구축하기 위한 여정에는 소프트웨어 개발 조직 내 여러 개인의 관점을 통합하는 것이 포함됩니다. 전체 애플리케이션을 제공하려면 기술 인력보다 더 많은 인력이 필요하기 때문에 성공적인 마이크로서비스 개발의 기반은 다음 세 가지 중요한 역할의 관점에서 시작된다고 믿습니다.

- 아키텍트 — 큰 그림을 보고 애플리케이션을 개별 마이크로서비스로 분해한 다음 마이크로서비스가 솔루션을 제공하기 위해 상호 작용하는 방식을 이해합니다.

- 소프트웨어 개발자 — 코드를 작성하고 언어 및 개발 프레임워크를 사용하여 마이크로서비스를 제공하는 방법을 이해합니다.

- DevOps 엔지니어 —프로덕션 및 비프로덕션 환경 전반에 걸쳐 서비스를 배포하고 관리하는 방법을 결정합니다. DevOps 엔지니어의 표어 는 모든 환경에서의 일관성 과 반복성 입니다.

이 장에서는 이러한 각 역할의 관점에서 마이크로서비스 세트를 설계하고 구축하는 방법을 보여줍니다. 이 장에서는 자체 비즈니스 애플리케이션 내에서 잠재적인 마이크로서비스를 식별하고 마이크로서비스를 배포하기 위해 배치해야 하는 운영 속성을 이해하는 데 필요한 기초를 제공합니다. 이 장이 끝나면 2장에서 만든 스켈레톤 프로젝트를 사용하여 클라우드에 패키징하고 배포할 수 있는 서비스를 갖게 됩니다.

## 3.1 아키텍트의 이야기: 마이크로서비스 아키텍처 설계

프로젝트에서 아키텍트의 역할은 해결해야 할 문제의 작업 모델을 제공하는 것입니다. 아키텍트는 응용 프로그램의 모든 부분이 서로 맞도록 개발자가 코드를 빌드할 스캐폴딩을 제공합니다. 마이크로서비스를 구축할 때 프로젝트 아키텍트는 세 가지 주요 작업에 중점을 둡니다.

- 비즈니스 문제 분해

- 서비스 세분화 설정

- 서비스 인터페이스 정의

### 3.1.1 비즈니스 문제 분해

에 복잡성에 직면하여 대부분의 사람들은 작업 중인 문제를 관리 가능한 덩어리로 분해하려고 합니다. 그들은 머리 속에 문제의 모든 세부 사항을 맞출 필요가 없도록 이렇게 합니다. 그들은 문제를 몇 가지 필수 부분으로 나눈 다음 이러한 부분 사이에 존재하는 관계를 찾을 수 있습니다.

마이크로서비스 아키텍처에서 프로세스는 거의 동일합니다. 아키텍트는 비즈니스 문제를 개별 활동 영역을 나타내는 청크로 나눕니다. 이러한 청크는 비즈니스 도메인의 특정 부분과 관련된 비즈니스 규칙 및 데이터 논리를 캡슐화합니다. 예를 들어, 아키텍트는 코드로 수행해야 하는 비즈니스 흐름을 보고 고객 및 제품 정보가 모두 필요하다는 것을 인식할 수 있습니다.

> **TIP** 두 개의 개별 데이터 도메인이 있다는 것은 여러 마이크로서비스가 작동 중임을 나타내는 좋은 표시입니다. 비즈니스 트랜잭션의 서로 다른 두 부분이 상호 작용하는 방식은 일반적으로 마이크로서비스의 서비스 인터페이스가 됩니다.

비즈니스 영역을 세분화하는 것은 흑백 과학이 아닌 예술 형식입니다. 비즈니스 문제를 식별하고 마이크로서비스 후보로 분해하기 위해 다음 지침을 사용할 수 있습니다.

- 비즈니스 문제를 설명하고 그것을 설명하기 위해 사용하는 명사에 주목하십시오. 문제를 설명할 때 동일한 명사를 반복해서 사용하는 것은 일반적으로 핵심 비즈니스 영역과 마이크로서비스의 기회를 나타내는 좋은 표시입니다. O-stock 애플리케이션에 대한 대상 명사의 예는 계약, 라이선스 및 자산과 같은 것일 수 있습니다.

- 동사에 주의하십시오. 동사는 행동을 강조하고 종종 문제 영역의 자연스러운 윤곽을 나타냅니다. "트랜잭션 X 는 사물 A 와 사물 B 에서 데이터를 가져와야 합니다. "라고 말하는 경우 일반적으로 여러 서비스가 작동 중임을 나타냅니다.

이 접근 방식을 O-stock 애플리케이션에 적용하면 "데스크톱 서비스의 Mike가 새 PC를 설정할 때 소프트웨어 X에 사용할 수 있는 라이선스 수를 조회하고 라이선스가 있는 경우 설치합니다. 소프트웨어. 그런 다음 추적 스프레드시트에 사용된 라이선스 수를 업데이트합니다.” 여기서 핵심 동사는 외모와 업데이트입니다.

- 데이터 응집력을 찾으십시오. 비즈니스 문제를 개별 조각으로 나눌 때 서로 관련성이 높은 데이터 조각을 찾으십시오. 대화 중에 갑자기 논의한 내용과 근본적으로 다른 데이터를 읽거나 업데이트하는 경우 다른 서비스 후보가 있을 수 있습니다. 마이크로서비스는 데이터를 완전히 소유해야 합니다.

이 지침을 받아 소프트웨어 자산 관리에 사용되는 O-stock 소프트웨어와 같은 실제 문제에 적용해 보겠습니다. (이 응용 프로그램은 2장에서 처음 언급했습니다.)

다시 말하지만, O-stock은 고객 데이터 센터 내에 있는 JEE 애플리케이션 서버에 배포되는 Optima Growth의 모놀리식 웹 애플리케이션입니다. 목표는 기존 모놀리식 애플리케이션을 서비스 세트로 분해하는 것입니다. 이를 달성하기 위해 O-stock 애플리케이션의 사용자 및 일부 비즈니스 이해 관계자를 인터뷰하고 애플리케이션과 상호 작용하고 애플리케이션을 사용하는 방법에 대해 논의하는 것으로 시작합니다. 그림 3.1은 다양한 비즈니스 고객과의 대화 중 몇 가지 명사와 동사를 요약하고 강조 표시합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F01_Huaylupo.png)

그림 3.1 일상 업무를 수행하고 애플리케이션과 상호 작용하는 방법을 이해하기 위해 O-stock 사용자를 인터뷰한 결과 요약

O-stock 사용자가 응용 프로그램과 상호 작용하는 방식을 살펴보고 다음 질문에 답하면 응용 프로그램의 데이터 모델을 식별할 수 있습니다. 이를 통해 O-stock 문제 영역을 마이크로서비스 후보로 분해할 수 있습니다.

- Emma가 관리하는 계약 정보를 어디에 저장할까요?

- 라이선스 정보(비용, 라이선스 유형, 라이선스 소유자, 라이선스 계약)를 어디에 저장하고 어떻게 관리할 것인가?

- Jenny는 PC에 라이선스를 설정합니다. 자산을 어디에 저장할 것인가?

- 앞서 언급한 모든 개념을 고려하면 라이선스가 여러 자산을 보유한 조직에 속한다는 것을 알 수 있습니다. 그렇다면 조직 정보를 어디에 저장할 것인가?

그림 3.2는 Optima Growth의 고객과의 대화를 기반으로 한 단순화된 데이터 모델을 보여줍니다. 비즈니스 인터뷰 및 데이터 모델을 기반으로 마이크로서비스 후보는 조직 , 라이선스 , 계약 및 자산 .

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F02_Huaylupo.png)

그림 3.2 단순화된 O-stock 데이터 모델. 조직에는 여러 라이선스가 있을 수 있으며 라이선스는 하나 이상의 자산에 적용될 수 있으며 각 라이선스에는 계약이 있습니다.

### 3.1.2 서비스 세분성 설정

한 번 단순화된 데이터 모델이 있으므로 애플리케이션에 필요한 마이크로서비스를 정의하는 프로세스를 시작할 수 있습니다. 그림 3.2의 데이터 모델에서 볼 수 있듯이 다음 요소를 기반으로 하는 네 가지 잠재적인 마이크로서비스를 볼 수 있습니다.

- 자산

- 특허

- 계약

- 조직

목표는 이러한 주요 기능을 가져와 서로 독립적으로 구축 및 배포할 수 있는 완전히 독립적인 단위로 추출하는 것입니다. 이러한 장치는 선택적으로 개별 데이터베이스를 공유하거나 가질 수 있습니다. 그러나 데이터 모델에서 서비스를 추출하려면 코드를 별도의 프로젝트로 다시 패키징하는 것 이상이 필요합니다. 또한 서비스가 액세스할 실제 데이터베이스 테이블을 추출하고 각 서비스가 특정 도메인의 테이블에만 액세스할 수 있도록 허용합니다. 그림 3.3은 애플리케이션 코드와 데이터 모델이 개별 조각으로 "청크"되는 방법을 보여줍니다.

> NOTE 각 서비스에 대해 개별 데이터베이스를 만들었지만 서비스 간에 데이터베이스를 공유할 수도 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F03_Huaylupo.png)

그림 3.3 모놀리식 애플리케이션에서 서로 독립적으로 배포되는 더 작은 개별 서비스로 분류된 O-stock 애플리케이션

문제 영역을 개별 조각으로 나눈 후 서비스에 대해 적절한 수준의 세분화를 달성했는지 여부를 결정하는 데 어려움을 겪는 경우가 많습니다. 너무 조악하거나 세분화된 마이크로서비스에는 몇 가지 특징이 있습니다. 이에 대해서는 곧 논의하겠습니다.

마이크로서비스 아키텍처를 구축할 때 세분성의 문제는 필수적입니다. 이것이 올바른 세분성 수준이 무엇인지에 대한 정답을 결정하기 위해 다음 개념을 설명하려는 이유입니다.

- 마이크로서비스로 광범위하게 시작하고 더 작은 서비스로 리팩토링하는 것이 좋습니다. 마이크로서비스 여정을 시작하고 모든 것을 마이크로서비스로 만들 때 과도하게 넘어가기 쉽습니다. 그러나 문제 영역을 작은 서비스로 분해하면 마이크로서비스가 세분화된 데이터 서비스에 불과하기 때문에 조기 복잡성을 초래하는 경우가 많습니다.

- 먼저 우리 서비스가 서로 어떻게 상호 작용하는지에 초점을 맞춥니다. 이것은 문제 영역의 거친 인터페이스를 설정하는 데 도움이 됩니다. 너무 세세한 것보다 너무 거친 것에서 리팩토링하는 것이 더 쉽습니다.

- 서비스 책임은 문제 영역에 대한 이해가 증가함에 따라 시간이 지남에 따라 변경됩니다. 종종 마이크로서비스는 새로운 애플리케이션 기능이 요청될 때 책임을 집니다. 단일 마이크로서비스로 시작하는 것이 여러 서비스로 성장할 수 있습니다. 원래 마이크로서비스는 이러한 새로운 서비스에 대한 오케스트레이션 계층 역할을 하고 애플리케이션의 다른 부분에서 기능을 캡슐화합니다.

나쁜 마이크로서비스의 냄새는 무엇입니까? 마이크로서비스가 적절한 규모인지 어떻게 알 수 있습니까? 마이크로서비스가 너무 세분화된 경우 다음이 표시될 수 있습니다.

- 너무 많은 책임이 있는 서비스. 서비스에서 비즈니스 로직의 일반적인 흐름은 복잡하고 지나치게 다양한 규칙을 적용하는 것 같습니다.

- 많은 수의 테이블에서 데이터를 관리하는 서비스입니다. 마이크로서비스는 관리하는 데이터에 대한 기록입니다. 여러 테이블에 데이터를 유지하거나 서비스 데이터베이스 외부의 테이블에 도달하는 경우 서비스가 너무 크다는 단서입니다. 우리는 마이크로서비스가 3~5개 이하의 테이블을 소유해야 한다는 지침을 사용하고 싶습니다. 더 이상, 귀하의 서비스는 너무 많은 책임을 져야 할 것입니다.

- 테스트 케이스가 너무 많은 서비스. 서비스는 시간이 지남에 따라 규모와 책임이 커질 수 있습니다. 적은 수의 테스트 케이스로 시작하여 수백 개의 단위 및 통합 테스트로 끝나는 서비스가 있는 경우 리팩토링이 필요할 수 있습니다.

너무 세분화된 마이크로서비스는 어떻습니까?

- 문제 영역의 한 부분에 있는 마이크로서비스는 토끼처럼 번식합니다. 모든 것이 마이크로서비스가 되면 서비스에서 비즈니스 로직을 구성하는 것이 복잡하고 어려워집니다. 작업을 완료하는 데 필요한 서비스의 수가 엄청나게 증가하기 때문입니다. 일반적인 냄새는 애플리케이션에 수십 개의 마이크로서비스가 있고 각 서비스가 단일 데이터베이스 테이블과만 상호 작용할 때입니다.

- 마이크로서비스는 서로 크게 상호 의존적입니다. 문제 도메인의 한 부분에 있는 마이크로서비스가 단일 사용자 요청을 완료하기 위해 서로 간에 계속 호출하고 있음을 발견했습니다.

- 마이크로서비스는 간단한 CRUD 서비스 모음이 됩니다. 마이크로서비스는 데이터 소스에 대한 추상화 레이어가 아니라 비즈니스 로직의 표현입니다. 마이크로서비스가 CRUD 관련 논리만 수행한다면 너무 세분화된 것일 수 있습니다.

마이크로서비스 아키텍처는 처음부터 올바른 디자인을 얻을 수 없다는 것을 알고 있는 진화적 사고 프로세스로 개발되어야 합니다. 그렇기 때문에 첫 번째 서비스 세트는 세분화된 것보다 더 거친 것부터 시작하는 것이 좋습니다.

디자인에 독단적이지 않는 것도 중요합니다. 서비스에 물리적 제약이 있을 수 있습니다. 예를 들어, 두 개의 개별 서비스가 너무 복잡하거나 서비스의 도메인 라인 사이에 명확한 경계가 없기 때문에 데이터를 결합하는 집계 서비스를 만들어야 합니다. 결국, 디자인을 완벽하게 만들려고 시간을 낭비하고 당신의 노력에 대해 아무것도 보여줄 것이 없기보다는 실용적인 접근 방식을 취하고 전달하십시오.

### 3.1.3 서비스 인터페이스 정의

아키텍트 입력의 마지막 부분은 애플리케이션의 마이크로서비스가 서로 통신하는 방법을 정의하는 것입니다. 마이크로서비스로 비즈니스 로직을 구축할 때 서비스에 대한 인터페이스는 직관적이어야 하며 개발자는 애플리케이션의 서비스 중 하나 또는 두 개를 완전히 이해함으로써 애플리케이션에서 모든 서비스가 어떻게 작동하는지 리듬을 얻어야 합니다. 일반적으로 서비스 인터페이스 디자인을 구현하기 위해 다음 지침을 사용할 수 있습니다.

- REST 철학을 수용하십시오. 이것은 Richardson 성숙도 모델(다음 사이드바 참조)과 함께 모범 사례(부록 A 참조) 중 하나입니다. 서비스에 대한 REST 접근 방식은 기본적으로 표준 HTTP(GET, PUT, POST 및 DELETE)를 사용하여 서비스에 대한 호출 프로토콜로 HTTP를 수용하는 것입니다. 이러한 HTTP 동사를 중심으로 기본 동작을 모델링하십시오.

- URI를 사용하여 의도를 전달합니다. 서비스의 끝점으로 사용하는 URI는 문제 도메인의 다양한 리소스를 설명하고 그 안의 리소스 관계에 대한 기본 메커니즘을 제공해야 합니다.

- 요청 및 응답에 JSON을 사용합니다. JSON은 매우 가벼운 데이터 직렬화 프로토콜이며 XML보다 훨씬 사용하기 쉽습니다.

- HTTP 상태 코드를 사용하여 결과를 전달합니다. HTTP 프로토콜에는 서비스의 성공 또는 실패를 나타내는 다양한 표준 응답 코드 본문이 있습니다. 이러한 상태 코드를 배우고 가장 중요한 것은 모든 서비스에서 이를 일관되게 사용하는 것입니다.

모든 기본 지침은 서비스 인터페이스를 이해하고 소비하기 쉽게 만드는 한 가지를 가리킵니다. 개발자가 앉아서 서비스 인터페이스를 보고 사용을 시작하기를 원합니다. 마이크로서비스를 사용하기 쉽지 않은 경우 개발자는 마이크로서비스를 우회하여 마이크로서비스의 의도를 전복시킬 것입니다.


## 3.2 마이크로서비스를 사용하지 않는 경우

마이크로서비스가 애플리케이션 구축을 위한 강력한 아키텍처 패턴인 이유에 대해 설명했습니다. 그러나 마이크로서비스를 사용하여 애플리케이션을 빌드 해서는 안 되는 경우 는 다루지 않았습니다. 다음과 같은 "해서는 안 되는 사항"을 살펴보겠습니다.

- 분산 시스템 구축 시 복잡성

- 가상 서버 또는 컨테이너 sprawl

- 신청 유형

- 데이터 트랜잭션 및 일관성

### 3.2.1 분산 시스템 구축 시 복잡성

마이크로서비스는 소규모로 분산되고 세분화되어, 이는 더 많은 모놀리식 애플리케이션에서 볼 수 없는 수준의 복잡성을 애플리케이션에 도입합니다. 마이크로서비스 아키텍처에는 높은 수준의 운영 성숙도가 필요합니다. 조직이 고도로 분산된 애플리케이션에 필요한 자동화 및 운영 작업(모니터링, 확장 등)에 기꺼이 투자하지 않는 한 마이크로서비스 사용을 고려하지 마십시오.

### 3.2.2 서버 또는 컨테이너 스프롤

마이크로서비스에 대한 가장 일반적인 배포 모델 중 하나는 하나의 컨테이너에 하나의 마이크로서비스 인스턴스를 배포하는 것입니다. 대규모 마이크로서비스 기반 애플리케이션에서는 프로덕션에서만 구축 및 유지 관리해야 하는 50~100개의 서버 또는 컨테이너(일반적으로 가상)로 끝날 수 있습니다. 클라우드에서 이러한 서비스를 실행하는 비용이 저렴하더라도 이러한 서비스를 관리하고 모니터링하는 운영 복잡성은 엄청납니다

> **참고** 마이크로서비스의 유연성은 이러한 모든 서버를 실행하는 비용과 비교해야 합니다. 람다와 같은 기능 개발을 고려하거나 동일한 서버에 더 많은 마이크로서비스 인스턴스를 추가하는 것과 같은 다른 대안을 가질 수도 있습니다.

### 3.2.3 애플리케이션 유형

마이크로서비스 재사용성에 중점을 두고 있으며 복원력과 확장성이 필요한 대규모 애플리케이션을 구축하는 데 매우 유용합니다. 이것이 많은 클라우드 기반 회사가 마이크로서비스를 채택한 이유 중 하나입니다. 

소규모 부서 수준의 애플리케이션을 구축하거나 사용자 기반이 적은 애플리케이션을 구축하는 경우 마이크로서비스와 같은 분산 모델 구축과 관련된 복잡성으로 인해 기존보다 더 많은 비용이 발생할 수 있습니다.

### 3.2.4 데이터 트랜잭션 및 일관성

마이크로서비스를 보기 시작하면 서비스와 서비스 소비자의 데이터 사용 패턴을 생각해야 합니다. 마이크로서비스는 적은 수의 테이블을 둘러싸고 추상화하며 데이터 저장소에 대해 간단한(복잡하지 않은) 쿼리 생성, 추가 및 수행과 같은 "운영" 작업을 수행하기 위한 메커니즘으로 잘 작동합니다.

애플리케이션이 여러 데이터 원본에서 복잡한 데이터 집계 또는 변환을 수행해야 하는 경우 마이크로서비스의 분산 특성으로 인해 이 작업이 어려워집니다. 마이크로서비스는 항상 너무 많은 책임을 지게 되며 또한 성능 문제가 생깁니다.



## 3.3 개발자 이야기: Spring Boot로 마이크로서비스 구축하기

이 섹션에서는 O-stock 도메인 모델에서 라이선스 마이크로서비스를 구축할 때 개발자의 우선 순위를 살펴보겠습니다.

> **참고** 이전 장에서 라이선스 서비스의 골격을 만들었습니다. 해당 장의 코드 목록을 따르지 않은 경우 https://github.com/ihuaylupo/manning-smia/tree/master/chapter2 에서 소스 코드를 다운로드할 수 있습니다 .

다음 여러 섹션에서

1. 라이선스 서비스 엔드포인트를 노출하도록 엔드포인트 매핑을 위한 Spring Boot 컨트롤러 클래스 구현

2. 메시지를 다른 언어에 적용할 수 있도록 국제화 구현

3. 사용자가 서버와 상호 작용할 수 있도록 충분한 정보를 제공하기 위해 Spring HATEOAS 구현

### 3.3.1 마이크로서비스로의 출입구 구축: 스프링 부트 컨트롤러

지금 빌드 스크립트를 제거하고(2장 참조) 간단한 Spring Boot 부트스트랩 클래스를 구현했기 때문에 어떤 작업을 수행할 첫 번째 코드를 작성할 수 있습니다. 이 코드는 컨트롤러 클래스가 됩니다. Spring Boot 애플리케이션에서 컨트롤러 클래스는 서비스 끝점을 노출하고 들어오는 HTTP 요청의 데이터를 요청을 처리하는 Java 메서드에 매핑합니다.

REST를 부여하세요

이 책의 모든 마이크로서비스는 Richardson Maturity Model(http://mng.bz/JD5Z)을 따릅니다. 구축하는 모든 서비스에는 다음과 같은 특성이 있습니다.

- HTTP/HTTPS를 서비스에 대한 호출 프로토콜로 사용 - HTTP 끝점은 서비스를 노출하고 HTTP 프로토콜은 서비스와 데이터를 주고 받습니다.

- 서비스 동작을 표준 HTTP 동사에 매핑 - REST는 동작을 HTTP 동사 POST, GET, PUT 및 DELETE에 매핑하는 서비스를 갖는 것을 강조합니다. 이러한 동사는 대부분의 서비스에서 발견되는 CRUD 기능에 매핑됩니다.

- 서비스로 들어오고 나가는 모든 데이터에 대한 직렬화 형식으로 JSON 사용 — 이것은 REST 기반 마이크로서비스에 대한 어렵고 빠른 원칙은 아니지만, JSON은 마이크로서비스. XML을 사용할 수 있지만 많은 REST 기반 응용 프로그램에서 JavaScript와 JSON을 사용합니다. JSON은 JavaScript 기반 웹 프런트 엔드 및 서비스에서 사용하는 데이터를 직렬화 및 역직렬화하기 위한 기본 형식입니다.
  
- HTTP 상태 코드를 사용하여 서비스 호출 상태 전달 - HTTP 프로토콜은 다양한 상태 코드 세트를 사용하여 서비스의 성공 또는 실패를 나타냅니다. REST 기반 서비스는 이러한 HTTP 상태 코드와 역방향 프록시 및 캐시와 같은 기타 웹 기반 인프라를 활용합니다. 이들은 비교적 쉽게 마이크로서비스와 통합할 수 있습니다.
HTTP는 웹의 언어입니다. HTTP를 서비스 구축을 위한 철학적 프레임워크로 사용하는 것은 클라우드에서 서비스 구축의 핵심입니다.

src/main/java/com/optimagrowth/license/controller/LicenseController.java에서 첫 번째 컨트롤러 클래스를 찾을 수 있습니다. LicenseController클래스 POST, GET, PUT, DELETE 동사에 매핑되는 4개의 HTTP 끝점을 노출합니다.

이 컨트롤러 클래스를 살펴보고 Spring Boot가 서비스 엔드포인트를 최소한으로 노출하는 데 필요한 노력을 유지하고 서비스에 대한 비즈니스 로직을 구축하는 데 집중할 수 있도록 하는 일련의 주석을 제공하는 방법을 살펴보겠습니다. 아직 클래스 메서드가 없는 기본 컨트롤러 클래스 정의부터 살펴보겠습니다. 다음 목록은 O-stock의 라이선스 서비스에 대한 컨트롤러 클래스를 보여줍니다.

목록 3.1 LicenseServiceController스프링으로 표시하기RestController

```java
@RestController ❶
@RequestMapping(value="v1/organization/{organizationId}/license") ❷
public class LicenseController {
}
```
❶ Spring Boot에 이것이 REST 기반 서비스이며 JSON을 통해 서비스 요청/응답을 자동으로 직렬화/역직렬화함을 알려줍니다.

❷ 이 클래스의 모든 HTTP 끝점을 /v1/organization/{organizationId}/license로 노출합니다.

@RestController 주석 을 보고 탐색을 시작하겠습니다. @RestController는 이 Java 클래스가 REST 기반 서비스에 사용될 것임을 Spring 컨테이너에 알리는 클래스 수준 Java 주석입니다. 이 주석은 JSON 또는 XML로 서비스에 전달된 데이터의 직렬화를 자동으로 처리합니다(기본적으로 이 클래스는 반환된 데이터를 JSON으로 직렬화함). 기존의 Spring @Controller 주석과 달리 @RestController는 컨트롤러 클래스의 메소드에서 ResponseBody 클래스를 반환할 것을 요구하지 않습니다. 이것은 모두 @ResponseBody 주석을 포함하는 @RestController 주석의 존재에 의해 처리됩니다.

왜 마이크로서비스용 JSON인가?

여러 프로토콜을 사용하여 HTTP 기반 마이크로서비스 간에 데이터를 주고받을 수 있습니다. 그러나 JSON은 다음과 같은 몇 가지 이유로 사실상의 표준으로 부상했습니다.

- XML 기반 SOAP 같은 다른 프로토콜과 비교할 때 JSON은 매우 가볍습니다. 많은 텍스트 오버헤드 없이 데이터를 표현할 수 있습니다.

- JSON은 사람이 쉽게 읽고 사용할 수 있습니다. 이것은 직렬화 프로토콜을 선택하기 위한 과소평가된 품질입니다. 문제가 발생하면 개발자가 JSON 청크를 살펴보고 그 안에 있는 내용을 신속하게 처리하는 것이 중요합니다. 프로토콜의 단순성으로 인해 이 작업을 매우 쉽게 수행할 수 있습니다.

- JSON은 JavaScript에서 사용되는 기본 직렬화 프로토콜입니다. 프로그래밍 언어로서 JavaScript의 극적인 부상과 JavaScript에 크게 의존하는 SPA의 극적인 상승 이후 JSON은 REST 기반 응용 프로그램을 구축하는 데 자연스럽게 적합하게 되었습니다. 클라이언트가 서비스를 호출하는 데 사용합니다.

- 그러나 다른 메커니즘과 프로토콜은 서비스 간 통신에 JSON보다 더 효율적입니다. Apache Thrift( http://thrift.apache.org ) 프레임워크를 사용하면 바이너리 프로토콜을 사용하여 서로 통신할 수 있는 다국어 서비스를 구축할 수 있습니다. Apache Avro 프로토콜( http://avro.apache.org )은 클라이언트와 서버 호출 간에 데이터를 바이너리 형식으로 앞뒤로 변환하는 데이터 직렬화 프로토콜입니다. 유선을 통해 보내는 데이터의 크기를 최소화해야 하는 경우 이러한 프로토콜을 살펴보는 것이 좋습니다. 그러나 마이크로서비스에서 직접 JSON을 사용하는 것이 효과적으로 작동하고 서비스 소비자와 서비스 클라이언트 간의 디버그를 위해 다른 통신 계층을 방해하지 않는다는 것이 우리의 경험이었습니다.

@RequestMapping을 사용할 수 있습니다(목록 3.1의 두 번째 주석)을 클래스 수준 및 메서드 수준 주석으로 사용하고 Spring 컨테이너에 서비스가 사용자에게 노출할 HTTP 끝점을 알려줍니다. @RequestMapping클래스 수준 주석으로 사용할 때 컨트롤러에 의해 노출되는 다른 모든 끝점에 대한 URL의 루트를 설정하는 것입니다. 속성을 @RequestMapping(value="v1/organization/ {organizationId}/ license")사용valueController클래스에 노출된 모든 끝점에 대한 URL의 루트를 설정합니다.. 이 컨트롤러에 노출된 모든 서비스 끝점은 다음으로 시작합니다.

```java
v1/organization/{organizationId}/license 
```

컨트롤러에 첫 번째 메서드를 추가하기 전에 만들려는 서비스에서 사용할 모델과 서비스 클래스를 살펴보겠습니다. Listing 3.2는 라이센스 데이터를 캡슐화하는 POJO 클래스를 보여준다.

> **참고** 캡슐화는 객체 지향 프로그래밍의 주요 원칙 중 하나이며 Java에서 캡슐화를 달성하려면 클래스의 변수를 private로 선언한 다음 해당 변수의 값을 읽고 쓸 수 있는 공용 getter 및 setter를 제공해야 합니다.

목록 3.2 라이센스 모델
```java
@Getter 
@Setter 
@ToString
public class License { ❶
    private int id;
    private String licenseId;
    private String description;
    private String organizationId;
    private String productName;
    private String licenseType;
}
```
❶ 라이선스 정보가 포함된 POJO

다음 목록은 컨트롤러 클래스에서 만들 다양한 서비스의 논리를 개발하는 데 사용할 서비스 클래스를 보여줍니다.

목록 3.3 LicenseService클래스 탐색

```java

@Service
public class LicenseService {

   public License getLicense(String licenseId, String organizationId){
      License license = new License();
      license.setId(new Random().nextInt(1000));
      license.setLicenseId(licenseId);
      license.setOrganizationId(organizationId);
      license.setDescription("Software product");
      license.setProductName("Ostock");
      license.setLicenseType("full");

      return license;
   }

   public String createLicense(License license, String organizationId){
      String responseMessage = null;
      if(license != null) {
         license.setOrganizationId(organizationId);
            responseMessage = String.format("This is the post and the
                                  object is: %s", license.toString());
      }

      return responseMessage;
   }

   public String updateLicense(License license, String organizationId){
      String responseMessage = null;
      if (license != null) {
         license.setOrganizationId(organizationId);
         responseMessage = String.format("This is the put and
                      the object is: %s", license.toString());
      }

      return responseMessage;
   }

   public String deleteLicense(String licenseId, String organizationId){
      String responseMessage = null;
      responseMessage = String.format("Deleting license with id %s for the organization %s",licenseId, organizationId);
      return responseMessage;

   }
}
```
이 서비스 클래스에는 하드코딩된 데이터를 반환하는 더미 서비스 세트가 포함되어 있어 마이크로서비스의 골격이 어떻게 보여야 하는지 알 수 있습니다. 계속 읽으면서 이 서비스에 대한 작업을 계속하게 되며 서비스를 구성하는 방법에 대해 더 자세히 알아볼 것입니다. 지금은 컨트롤러에 첫 번째 메서드를 추가해 보겠습니다. 이 메서드는 REST 호출에 사용된 GET 동사를 구현하고 단일 License클래스를 반환합니다. 다음 코드 목록에 표시된 대로 인스턴스.

목록 3.4 개별 GET HTTP 엔드포인트 노출
```java
@RestController
@RequestMapping(value="v1/organization/{organizationId}/license")
public class LicenseController {

@Autowired
  private LicenseService licenseService;

  @GetMapping(value="/{licenseId}") ❶
  public ResponseEntity<License> getLicense(
     @PathVariable("organizationId") String organizationId,
     @PathVariable("licenseId") String licenseId) { ❷

     License license = licenseService
         .getLicense(licenseId,organizationId); ❸
       return ResponseEntity.ok(license);
  }
}
```
❶ 라이선스 데이터를 가져오는 방법 가져오기

❷ URL에서 @GetMapping의 매개변수로 두 개의 매개변수(organizationId 및 licenseId)를 매핑합니다.

❸ ResponseEntity는 전체 HTTP 응답을 나타냅니다.

가장 먼저 하는 일은 목록은 주석을 달기 위한 것이었습니다. 메서드 수준 주석을 사용하여 메서드에 대해 다음 끝점을 빌드할 수 있습니다.

```java
v1/organization/{organizationId}/license/{licenseId}
```
클래스의 맨 위를 살펴보면 컨트롤러에 들어오는 모든 HTTP 요청과 일치하도록 루트 수준 주석을 지정했습니다. 먼저 루트 수준 주석 값을 추가한 다음 메서드 수준 값. 주석의 두 번째 매개변수인 method은 메서드를 일치시키는 데 사용되는 HTTP 동사를 지정합니다. 에 getLicense()의해 제시된 방법, 우리는 GET 방식에 일치 RequestMethod.GET된 열거입니다.

목록 3.4에서 주목해야 할 두 번째 사항은 @PathVariable을 사용한다는 것입니다.getLicense()메소드 의 매개변수 본문에서 이 주석은 들어오는 URL에 전달된 매개변수 값( {parameterName}구문 으로 표시됨 )을 메서드의 매개변수에 매핑합니다 . 목록 3.4의 GET 서비스의 경우 URL(organizationId 그리고 licenseId)를 다음과 같이 메서드의 두 매개변수 수준 변수에 추가합니다.

```java
@PathVariable("organizationId") String organizationId
@PathVariable("licenseId") String licenseId
```

마지막으로, ResponseEntity 객체를 살펴봅니다. ResponseEntity는 상태 코드, 헤더와 본문을 포함한 전체 HTTP 응답을 나타냅니다. 이전 목록에서는 License 객체 를 반환할 수 있습니다.

이제 HTTP GET을 사용하여 끝점을 만드는 방법을 이해했으므로 계속해서 POST, PUT 및 DELETE 메서드를 추가하여 License클래스 를 만들고 업데이트하고 삭제하겠습니다.인스턴스. 다음 목록은 이를 수행하는 방법을 보여줍니다.

목록 3.5 개별 HTTP 엔드포인트 노출

```java
@RestController
@RequestMapping(value="v1/organization/{organizationId}/license")
public class LicenseController {

   @Autowired
   private LicenseService licenseService;

   @RequestMapping(value="/{licenseId}",method = RequestMethod.GET)
   public ResponseEntity<License> getLicense(    
         @PathVariable("organizationId") String organizationId,
         @PathVariable("licenseId") String licenseId) {

      License license = licenseService.getLicense(licenseId, 
                                             organizationId);
      return ResponseEntity.ok(license);
   }

   @PutMapping ❶
   public ResponseEntity<String> updateLicense(
            @PathVariable("organizationId") 
            String organizationId,
            @RequestBody License request) { ❷
      return ResponseEntity.ok(licenseService.updateLicense(request, organizationId));
   }

   @PostMapping ❸
   public ResponseEntity<String> createLicense(
      @PathVariable("organizationId") String organizationId,
      @RequestBody License request) {
      return ResponseEntity.ok(licenseService.createLicense(request, organizationId));
   }

   @DeleteMapping(value="/{licenseId}") ❹
   public ResponseEntity<String> deleteLicense(
      @PathVariable("organizationId") String organizationId,
      @PathVariable("licenseId") String licenseId) {
      return ResponseEntity.ok(licenseService.deleteLicense(licenseId, organizationId));
   }
}
```
❶ 라이선스 업데이트를 위한 Put 메소드

❷ HTTP 요청 본문을 License 객체에 매핑

❸ 라이선스를 삽입하는 포스트 방식

❹ 라이선스 삭제 삭제 방법

목록 3.5에서 먼저 updateLicense() 메서드에 @PutMapping 주석을 추가합니다.

다음으로 주목해야 할 점은 updateLicense() 메서드의 매개변수 본문에서 @PathVariable 및 @RequestBody 주석을 사용한다는 것입니다. @RequestBody는 HTTPRequest 본문을 전송 객체(이 경우 라이선스 객체)에 매핑합니다. updateLicense() 메서드에서 두 개의 매개변수(하나는 URL에서, 다른 하나는 HTTPRequest 본문에서)에서 다음 두 개의 매개변수 수준 변수로 createLicense 메서드에 매핑합니다.

```java
@PathVariable("organizationId") String organizationId
@RequestBody License request
```
마지막으로 목록 3.5에서 우리는 @PostMapping그리고 @DeleteMapping주석. @PostMapping주석에 대한 바로 가기 역할을하는 메소드 레벨의 주석입니다
```java
@RequestMapping(method = RequestMethod.POST) 
```
@DeleteMapping(value="/{licenseId}") 또한 메서드 수준 주석이며
```java
@RequestMapping(value="/{licenseId}",method = RequestMethod.DELETE)
```

엔드포인트 이름이 중요

마이크로서비스를 작성하는 경로를 너무 멀리 가기 전에 서비스를 통해 노출하려는 엔드포인트에 대한 표준을 설정했는지 확인하십시오. 마이크로서비스의 URL은 서비스의 의도, 서비스가 관리하는 리소스, 서비스 내에서 관리되는 리소스 간의 관계를 명확하게 전달하는 데 사용해야 합니다. 서비스 엔드포인트 이름 지정에 유용한 다음 지침을 찾았습니다.

- 서비스가 나타내는 리소스를 설정하는 명확한 URL 이름을 사용합니다. URL 정의를 위한 표준 형식을 사용하면 API가 보다 직관적이고 사용하기 쉽게 느껴지고 이름 지정 규칙에서 일관성을 유지하는 데 도움이 됩니다.

- URL을 사용하여 리소스 간의 관계를 설정합니다. 종종 부모 컨텍스트 외부에 자식이 존재하지 않는 마이크로서비스 내의 리소스 간에 부모-자식 관계가 있습니다. 따라서 자녀를 위한 별도의 마이크로서비스가 없을 수 있습니다. URL을 사용하여 이러한 관계를 표현하십시오. URL이 길고 중첩되는 경향이 있다면 마이크로서비스가 너무 많은 일을 하고 있는 것일 수 있습니다.

- URL에 대한 버전 관리 체계를 조기에 설정하십시오. URL 및 해당 끝점은 서비스 소유자와 서비스 소비자 간의 계약을 나타냅니다. 한 가지 일반적인 패턴은 모든 끝점 앞에 버전 번호를 추가하는 것입니다. 초기에 버전 관리 체계를 수립하고 고수하십시오. 이미 여러 소비자가 사용하고 있는 경우 URL에 버전 관리를 개조하는 것은 매우 어렵습니다(예: URL 매핑에서 /v1/ 사용).
이 시점에서 여러 서비스가 있습니다. 명령줄 창에서 pom.xml이 있는 프로젝트 루트 디렉터리로 이동하고 다음 Maven 명령을 실행합니다. (그림 3.4는 이 명령의 예상 출력을 보여줍니다.)
```sh
mvn spring-boot:run
```

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F04_Huaylupo.png)

그림 3.4 라이선스 서비스가 성공적으로 시작되었음을 보여주는 출력

서비스가 시작되면 노출된 엔드포인트를 직접 선택할 수 있습니다. 서비스 호출을 위해 Postman 또는 cURL과 같은 Chrome 기반 도구를 사용하는 것이 좋습니다. 그림 3.5는 엔드포인트에서 GET 및 DELETE 서비스를 호출하는 방법을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F05_Huaylupo.png)

그림 3.5 Postman으로 호출되는 GET 및 DELETE 서비스 라이선스 부여

그림 3.6은 http://local-host:8080/v1/organization/optimaGrowth/license엔드포인트를 사용하여 POST 및 PUT 서비스를 호출하는 방법을 보여줍니다 .

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F06_Huaylupo.png)

그림 3.6 Postman으로 호출되는 GET 및 DELETE 서비스 라이선스 부여

PUT, DELETE, POST 및 GET HTTP 동사에 대한 메서드를 구현했으면 다음으로 넘어갈 수 있습니다. 내면화.

### 3.3.2 라이선싱 서비스에 국제화 추가

국제화 응용 프로그램이 다른 언어에 적응할 수 있도록 하는 필수 요구 사항입니다. 여기서 주요 목표는 다양한 형식과 언어로 콘텐츠를 제공하는 애플리케이션을 개발하는 것입니다. 이 섹션에서는 이전에 생성한 라이선스 서비스에 내부화를 추가하는 방법을 설명합니다.

먼저 부트스트랩 클래스를 업데이트합니다. LicenseServiceApplication.java 만들기 LocaleResolver 그리고 ResourceBundleMessageSource우리의 라이센스 서비스를 위해. 다음 목록은 부트스트랩 클래스의 모양을 보여줍니다.

목록 3.6 부트스트랩 클래스용 빈 생성

```java
@SpringBootApplication
public class LicenseServiceApplication {

   public static void main(String[] args) {
     SpringApplication.run(LicenseServiceApplication.class, args);
   }

   @Bean
   public LocaleResolver localeResolver() {
     SessionLocaleResolver localeResolver = new SessionLocaleResolver();
     localeResolver.setDefaultLocale(Locale.US); ❶
     return localeResolver;
   }
   @Bean
   public ResourceBundleMessageSource messageSource() {
     ResourceBundleMessageSource messageSource = 
                                 new ResourceBundleMessageSource();
     messageSource.setUseCodeAsDefaultMessage(true); ❷

     messageSource.setBasenames("messages"); ❸
     return messageSource;
   }
}
```
❶ 미국을 기본 로케일로 설정합니다.

❷ 메시지가 발견되지 않으면 오류가 발생하지 않고 대신 메시지 코드를 반환합니다.

❸ 언어 속성 파일의 기본 이름 설정

목록 3.6에서 주목해야 할 첫 번째 사항은 Locale.US를 기본 로케일로 설정했다는 것입니다. 메시지를 검색할 때 로케일을 설정하지 않으면 messageSource는 LocaleResolver로 설정된 기본 로케일을 사용합니다. 다음으로 다음 호출에 유의하십시오.
```java
messageSource.setUseCodeAsDefaultMessage(true)
```
메시지를 찾을 수 없는 경우 이 옵션은 다음 'license.creates .message'과 같은 오류 대신 메시지 코드 를 반환합니다 .

"로케일 'es'에 대한 'license.creates.message' 코드에서 메시지를 찾을 수 없습니다.
마지막으로 messageSource.setBasenames("messages") 호출은 메시지를 메시지 소스 파일의 기본 이름으로 설정합니다. 예를 들어 이탈리아에 있다면 Locale.IT를 사용하고 messages_it.properties라는 파일을 갖게 됩니다. 특정 언어로 된 메시지를 찾을 수 없는 경우 메시지 소스는 messages.properties라는 기본 메시지 파일에서 검색합니다.

이제 메시지를 구성해 보겠습니다. 이 예에서는 영어와 스페인어 메시지를 사용합니다. 이를 달성하려면 /src/main/resources 소스 폴더 아래에 다음 파일을 생성해야 합니다.

- messages_en.properties

- messages_es.properties

- messages.properties

The following two listings show how the messages_en.properties and the messages_ es.properties files should look.

목록 3.7 messages_en.properties 파일 탐색
```yaml
license.create.message = License created %s
license.update.message = License %s updated
license.delete.message = Deleting license with 
                 id %s for the organization %s
```                 
목록 3.8 messages_es.properties 파일 탐색

```yaml
license.create.message = Licencia creada %s
license.update.message = Licencia %s creada
license.delete.message = Eliminando licencia con 
                  id %s para la organization %s license
```
이제 메시지와 @Beans주석을 구현했으므로, 메시지 리소스를 호출하도록 컨트롤러 또는 서비스의 코드를 업데이트할 수 있습니다. 다음 목록은 이 작업을 수행하는 방법을 보여줍니다.

Listing 3.9 에서 메시지를 찾기 위해 서비스 업데이트하기 MessageSource

```java
@Autowired
MessageSource messages;

public String createLicense(License license, 
                            String organizationId,
                            Locale locale){                                ❶
      String responseMessage = null;
      if (license != null) {
         license.setOrganizationId(organizationId);
         responseMessage = String.format(messages.getMessage(
                           "license.create.message", null,locale),
                            license.toString());                           ❷
      }

      return responseMessage;
}

public String updateLicense(License license, String organizationId){
      String responseMessage = null;
      if (license != null) {
         license.setOrganizationId(organizationId);
         responseMessage = String.format(messages.getMessage(
                           "license.update.message", null, null),
                            license.toString());                           ❸
      }

      return responseMessage;
}
```
❶ 메서드 매개변수로 Locale을 받습니다.

❷ 특정 메시지를 검색할 수신 로케일 설정

❸ 특정 메시지를 검색하기 위해 null 로케일을 보냅니다.

목록 3.9의 코드에서 강조해야 할 세 가지 중요한 사항이 있습니다. 첫 번째는 컨트롤러 자체에서 로케일을 수신할 수 있다는 것입니다. 두 번째는 매개변수로 수신한 로케일을 사용하여 messages.getMessage("license.create.message",null,locale)를 호출할 수 있다는 것이고, 세 번째로 주목해야 할 것은 messages.getMessage("license .update.message", null, null) 로케일을 보내지 않고. 이 특정 시나리오에서 애플리케이션은 이전에 부트스트랩 클래스에서 정의한 기본 로케일을 사용합니다. 이제 다음 코드로 요청 Accept-Language 헤더에서 언어를 수신하도록 컨트롤러의 createLicense() 메서드를 업데이트하겠습니다.

```java
   @PostMapping
   public ResponseEntity<String> createLicense(
        @PathVariable("organizationId") String organizationId, 
        @RequestBody License request,
        @RequestHeader(value = "Accept-Language",required = false) 
                       Locale locale){
     return ResponseEntity.ok(licenseService.createLicense(
         request, organizationId, locale));
   }
```
이 코드에서 주의해야 할 몇 가지 사항은 여기에서 @RequestHeader 주석을 사용한다는 것입니다. @RequestHeader 주석은 메서드 매개변수를 요청 헤더 값과 매핑합니다. createLicense() 메소드에서 요청 헤더 Accept-Language에서 로케일을 검색합니다. 이 서비스 매개변수는 필수가 아니므로 지정하지 않으면 기본 로케일을 사용합니다. 그림 3.7은 Postman에서 Accept-Language 요청 헤더를 보내는 방법을 보여줍니다.

> **참고** 로케일을 사용하는 방법에 대한 잘 정의된 규칙은 없습니다. 아키텍처를 분석하고 가장 적합한 옵션을 선택하는 것이 좋습니다. 예를 들어 프런트 엔드 애플리케이션이 로케일을 처리하는 경우 컨트롤러 메소드에서 로케일을 매개변수로 수신하는 것이 가장 좋은 옵션입니다. 그러나 백엔드에서 로케일을 관리하는 경우 기본 로케일을 사용할 수 있습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F07_Huaylupo.png)

그림 3.7 Accept-LanguagePOST 생성 라이선스 서비스 의 헤더 설정

### 3.3.3 관련 링크를 표시하기 위한 Spring HATEOAS 구현

Hypermedia를 응용 프로그램 상태의 엔진으로 나타냅니다. Spring HATEOAS는 주어진 리소스에 대한 관련 링크를 표시하는 HATEOAS 원칙을 따르는 API를 생성할 수 있는 작은 프로젝트입니다. HATEOAS 원칙에 따르면 API는 각 서비스 응답과 함께 가능한 다음 단계에 대한 정보를 반환하여 클라이언트에 가이드를 제공해야 합니다. 이 프로젝트는 핵심 기능이나 필수 기능은 아니지만 주어진 리소스의 모든 API 서비스에 대한 완전한 가이드를 갖고 싶다면 훌륭한 옵션입니다.

Spring HATEOAS를 사용하면 리소스 표현 모델에 대한 링크를 위한 모델 클래스를 빠르게 생성할 수 있습니다. 또한 Spring MVC 컨트롤러 메소드를 가리키는 특정 링크를 생성하는 링크 빌더 API를 제공합니다. 다음 코드 조각은 HATEOAS가 라이선스 서비스를 찾는 방법의 예를 보여줍니다.

```json
"_links": {
    "self" : {
        "href" : "http://localhost:8080/v1/organization/
                   optimaGrowth/license/0235431845"
    },
    "createLicense" : {
        "href" : "http://localhost:8080/v1/organization/
                  optimaGrowth/license"
    },
    "updateLicense" : {
        "href" : "http://localhost:8080/v1/organization/
                   optimaGrowth/license"
    },
    "deleteLicense" : {
        "href" : "http://localhost:8080/v1/organization/
                  optimaGrowth/license/0235431845"
    }
}
```
이 섹션에서는 라이선스 서비스에서 Spring HATEOAS를 구현하는 방법을 보여줍니다. 응답에서 리소스와 관련된 링크를 보내기 위해 가장 먼저 해야 할 일은 다음과 같이 HATEOAS 종속성을 pom.xml 파일에 추가하는 것입니다.

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```
Once we have the dependency, we need to update the License class in order to extend from RepresentationModel<License>. The following listing shows how to do this.

Listing 3.10 Extending from RepresentationModel

```java
@Getter @Setter @ToString
public class License extends RepresentationModel<License> {

   private int id;
   private String licenseId;
   private String description;
   private String organizationId;
   private String productName;
   private String licenseType;

}
```
RepresentationModel<License>는 라이선스 모델 클래스에 대한 링크를 추가할 수 있는 기능을 제공합니다. 이제 모든 설정이 완료되었으므로 LicenseController 클래스에 대한 링크를 검색하기 위해 HATEOS 구성을 생성해 보겠습니다. 다음 목록은 이것이 어떻게 수행되는지 보여줍니다. 이 예에서는 LicenseController 클래스의 getLicense() 메서드만 변경할 것입니다.

Listing 3.11 Adding links to the LicenseController
```java
@RequestMapping(value="/{licenseId}",method = RequestMethod.GET)
public ResponseEntity<License> getLicense( 
           @PathVariable("organizationId") String organizationId,
           @PathVariable("licenseId") String licenseId) {

    License license = licenseService.getLicense(licenseId,
                                            organizationId);
    license.add(linkTo(methodOn(LicenseController.class)
           .getLicense(organizationId, license.getLicenseId()))
           .withSelfRel(),
           linkTo(methodOn(LicenseController.class)
           .createLicense(organizationId, license, null))
           .withRel("createLicense"),
           linkTo(methodOn(LicenseController.class)
           .updateLicense(organizationId, license))
           .withRel("updateLicense"),
           linkTo(methodOn(LicenseController.class)
           .deleteLicense(organizationId, license.getLicenseId()))
           .withRel("deleteLicense"));        
    return ResponseEntity.ok(license);
}
```
add() 메서드는 RepresentationModel의 메서드입니다. linkTo 메서드는 License 컨트롤러 클래스를 검사하여 루트 매핑을 얻고, methodOn 메서드는 대상 메서드의 더미 호출을 수행하여 메서드 매핑을 가져옵니다. 두 메소드 모두 org.springframework.hateoas.server .mvc.WebMvcLinkBuilder의 정적 메소드입니다. WebMvcLinkBuilder는 컨트롤러 클래스에 대한 링크를 생성하기 위한 유틸리티 클래스입니다. 그림 3.8은 getLicense() 서비스의 응답 본문에 대한 링크를 보여줍니다. 이를 검색하려면 GET HTTP 메소드를 호출해야 합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F08_Huaylupo.png)

그림 3.8 HTTP GET 라이선스 서비스의 응답 본문에 있는 HATEOAS 링크

이 시점에서 서비스의 골격이 실행되고 있습니다. 그러나 개발 관점에서 이 서비스는 완전하지 않습니다. 좋은 마이크로서비스 설계는 서비스를 잘 정의된 비즈니스 로직과 데이터 액세스 계층으로 분리하는 것을 피하지 않습니다. 이후 장에서 진행하면서 이 서비스를 계속 반복하고 구조화하는 방법에 대해 자세히 알아볼 것입니다. 지금은 최종 관점으로 전환해 보겠습니다. DevOps 엔지니어가 서비스를 운영하고 클라우드에 배포하기 위해 패키징하는 방법을 살펴봅니다.

## 3.4 DevOps 스토리: 엄격한 런타임을 위한 빌드

DevOps는 새롭게 떠오르는 IT 분야이지만 DevOps 엔지니어에게 마이크로서비스 설계는 서비스가 생산에 들어간 후 관리하는 것에 관한 것입니다. 코드 작성은 종종 쉬운 부분입니다. 계속 실행하는 것은 어려운 부분입니다. 네 가지 원칙으로 마이크로서비스 개발 노력을 시작하고 이 책의 뒷부분에서 이를 기반으로 할 것입니다.

1. 마이크로서비스는 독립적이어야 합니다. 또한 단일 소프트웨어 아티팩트로 시작 및 종료되는 서비스의 여러 인스턴스와 함께 독립적으로 배포할 수 있어야 합니다.

2. 마이크로서비스를 구성할 수 있어야 합니다. 서비스 인스턴스가 시작되면 중앙 위치에서 자체 구성하는 데 필요한 데이터를 읽거나 구성 정보를 환경 변수로 전달해야 합니다. 서비스를 구성하는 데 사람이 개입할 필요가 없습니다.

3. 마이크로서비스 인스턴스는 클라이언트에 투명해야 합니다. 클라이언트는 서비스의 정확한 위치를 절대 알 수 없습니다. 대신 마이크로서비스 클라이언트는 서비스 검색 에이전트와 통신해야 합니다. 이를 통해 애플리케이션은 물리적 위치를 알 필요 없이 마이크로서비스의 인스턴스를 찾을 수 있습니다.

4. 마이크로서비스는 상태를 전달해야 합니다. 이것은 클라우드 아키텍처의 중요한 부분입니다. 마이크로서비스 인스턴스는 실패하고 검색 에이전트는 잘못된 서비스 인스턴스를 우회해야 합니다. 이 책에서는 Spring Boot Actuator를 사용하여 각 마이크로서비스의 상태를 표시합니다.

이 네 가지 원칙은 마이크로서비스 개발에 존재할 수 있는 역설을 드러냅니다. 마이크로서비스는 크기와 범위가 더 작지만 마이크로서비스가 자체 컨테이너에서 서로 독립적으로 배포되고 실행되기 때문에 마이크로서비스를 사용하면 애플리케이션에서 더 많은 움직이는 부분이 도입됩니다. 이것은 응용 프로그램의 실패 지점에 대한 높은 수준의 조정과 더 많은 기회를 제공합니다.

DevOps 관점에서 마이크로서비스의 운영 요구 사항을 미리 해결하고 이 네 가지 원칙을 마이크로서비스가 구축되어 환경에 배포될 때마다 발생하는 표준 수명 주기 이벤트 집합으로 변환해야 합니다. 4가지 원칙은 다음 운영 수명 주기에 매핑될 수 있습니다. 그림 3.9는 이 4가지 단계가 어떻게 함께 들어맞는지 보여줍니다.

- 서비스 어셈블리 - 동일한 서비스 코드와 런타임이 정확히 동일한 방식으로 배포되도록 반복성과 일관성을 보장하기 위해 서비스를 패키징하고 배포하는 방법입니다.

- 서비스 부트스트랩 - 애플리케이션 및 환경별 구성 코드를 런타임 코드에서 분리하여 사람의 개입 없이 모든 환경에서 마이크로서비스 인스턴스를 신속하게 시작하고 배포할 수 있도록 하는 방법입니다.

- 서비스 등록/검색 - 새 마이크로서비스 인스턴스가 배포될 때 다른 애플리케이션 클라이언트가 새 서비스 인스턴스를 검색할 수 있도록 만드는 방법입니다.

- 서비스 모니터링 - 마이크로서비스 환경에서는 고가용성 요구로 인해 동일한 서비스의 여러 인스턴스가 실행되는 것이 일반적입니다. DevOps 관점에서 마이크로서비스 인스턴스를 모니터링하고 장애가 발생한 서비스 인스턴스를 중심으로 오류가 라우팅되고 이러한 인스턴스가 중단되었는지 확인해야 합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F09_Huaylupo.png)

그림 3.9 마이크로서비스는 수명 주기에서 여러 단계를 거칩니다.

### 3.4.1 서비스 어셈블리: 마이크로서비스 패키징 및 배포

DevOps 관점에서 마이크로서비스 아키텍처의 핵심 개념 중 하나는 변화하는 애플리케이션 환경(예: 갑작스러운 사용자 요청 유입, 인프라 내 문제 등)에 대응하여 마이크로서비스의 여러 인스턴스를 신속하게 배포할 수 있다는 것입니다). 이를 수행하려면 마이크로서비스가 모든 종속성이 정의된 단일 아티팩트로 패키징되고 설치 가능해야 합니다. 이러한 종속성은 마이크로서비스를 호스팅하는 런타임 엔진(예: HTTP 서버 또는 애플리케이션 컨테이너)도 포함해야 합니다.

일관되게 구축, 패키징 및 배포하는 프로세스가 서비스 어셈블리입니다(그림 3.9의 1단계). 그림 3.10은 이 단계에 대한 추가 세부 정보를 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F10_Huaylupo.png)

그림 3.10 서비스 어셈블리 단계에서 소스 코드는 런타임 엔진과 함께 컴파일되고 패키지됩니다.

다행히도 거의 모든 Java 마이크로서비스 프레임워크에는 코드와 함께 패키징하고 배포할 수 있는 런타임 엔진이 포함됩니다. 예를 들어 그림 3.10의 Spring Boot 예제에서 Maven과 Spring Boot는 JAR에 내장된 Tomcat 엔진이 있는 실행 가능한 Java JAR 파일을 빌드합니다. 다음 명령줄 예제에서는 라이선스 서비스를 실행 가능한 JAR로 빌드한 다음 명령줄에서 JAR을 시작합니다.
```sh
mvn clean package && java -jar target/licensing-service-0.0.1-SNAPSHOT.jar
```
특정 운영 팀의 경우 JAR 파일에 런타임 환경을 포함한다는 개념은 애플리케이션 배포에 대한 생각의 주요 변화입니다. 기존 Java 웹 기반 응용 프로그램에서 응용 프로그램은 응용 프로그램 서버에 배포됩니다. 이 모델은 응용 프로그램 서버가 그 자체로 하나의 엔터티이며 배포되는 응용 프로그램과 독립적으로 서버의 구성을 감독하는 시스템 관리자 팀이 관리하는 경우가 많다는 것을 의미합니다.

응용 프로그램에서 응용 프로그램 서버 구성을 분리하면 배포 프로세스에서 실패 지점이 발생합니다. 이는 많은 조직에서 애플리케이션 서버의 구성이 소스 제어 하에 유지되지 않고 사용자 인터페이스와 자체 개발 관리 스크립트의 조합을 통해 관리되기 때문입니다. 구성 드리프트가 애플리케이션 서버 환경에 침투하여 갑자기 표면에 무작위로 나타나는 현상이 발생하기 쉽습니다.정전.

### 3.4.2 서비스 부트스트랩: 마이크로서비스 구성 관리

서비스 부트스트래핑(그림 3.9의 2단계)은 마이크로서비스가 처음 시작되고 애플리케이션 구성 정보를 로드해야 할 때 발생합니다. 그림 3.11은 부트스트랩 프로세스에 대한 추가 컨텍스트를 제공합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F11_Huaylupo.png)

그림 3.11 서비스가 시작되면(부트스트랩) 중앙 저장소에서 구성을 읽습니다.

모든 응용 프로그램 개발자가 알고 있듯이 응용 프로그램의 런타임 동작을 구성 가능하게 만들어야 하는 경우가 있습니다. 일반적으로 여기에는 애플리케이션과 함께 배포된 속성 파일에서 애플리케이션 구성 데이터를 읽거나 관계형 데이터베이스와 같은 데이터 저장소에서 데이터를 읽는 것이 포함됩니다.

마이크로서비스는 종종 동일한 유형의 구성 요구 사항을 실행합니다. 차이점은 클라우드에서 실행되는 마이크로서비스 애플리케이션에서는 수백 또는 수천 개의 마이크로서비스 인스턴스가 실행될 수 있다는 것입니다. 이를 더욱 복잡하게 만드는 것은 서비스가 전 세계에 퍼질 수 있다는 것입니다. 지리적으로 분산된 서비스가 많기 때문에 서비스를 재배포하여 새 구성 데이터를 가져오는 것이 불가능해집니다. 서비스 외부의 데이터 저장소에 데이터를 저장하면 이 문제가 해결됩니다. 그러나 클라우드의 마이크로서비스는 다음과 같은 고유한 과제를 제공합니다.

- 구성 데이터는 구조가 단순한 경향이 있으며 일반적으로 자주 읽고 드물게 작성됩니다. 관계형 데이터베이스는 단순한 키-값 쌍 집합보다 훨씬 더 복잡한 데이터 모델을 관리하도록 설계되었기 때문에 이 상황에서 과도합니다.

- 데이터는 정기적으로 액세스되지만 자주 변경되지 않기 때문에 낮은 수준의 대기 시간으로 데이터를 읽을 수 있어야 합니다.

- 데이터 저장소는 가용성이 높고 데이터를 읽는 서비스와 가까워야 합니다. 구성 데이터 저장소는 애플리케이션의 단일 실패 지점이 되기 때문에 완전히 다운될 수 없습니다.

5장에서는 간단한 키-값 데이터와 같은 것을 사용하여 마이크로서비스 애플리케이션 구성 데이터를 관리하는 방법을 보여줍니다.

### 3.4.3 서비스 등록 및 검색: 클라이언트가 마이크로서비스와 통신하는 방법

마이크로서비스 소비자 관점에서 마이크로서비스는 위치가 투명해야 합니다. 클라우드 기반 환경에서 서버는 일시적이기 때문입니다. 임시는 서비스가 호스팅되는 서버가 일반적으로 기업 데이터 센터에서 실행되는 서비스보다 수명이 짧음을 의미합니다. 클라우드 기반 서비스는 서비스가 실행되는 서버에 할당된 완전히 새로운 IP 주소로 신속하게 시작 및 종료할 수 있습니다.

서비스가 수명이 짧은 일회용 개체로 취급된다고 주장함으로써 마이크로서비스 아키텍처는 서비스의 여러 인스턴스를 실행하여 높은 수준의 확장성과 가용성을 달성할 수 있습니다. 서비스 수요와 탄력성은 상황이 허락하는 한 신속하게 관리할 수 있습니다. 각 서비스에는 고유하고 영구적인 IP 주소가 할당되어 있습니다. 임시 서비스의 단점은 서비스가 지속적으로 발생 및 중단되기 때문에 이러한 서비스의 큰 풀을 수동으로 또는 수동으로 관리하면 서비스 중단이 발생할 수 있다는 것입니다.

마이크로서비스 인스턴스는 타사 에이전트에 자체 등록해야 합니다. 이 등록 프로세스를 서비스 디스커버리라고 합니다. (그림 3.9의 3단계를 참조한 다음 이 프로세스에 대한 자세한 내용은 그림 3.12를 참조하십시오). 마이크로서비스 인스턴스가 서비스 디스커버리 에이전트에 등록되면 물리적 IP 주소(또는 서비스 인스턴스의 도메인 주소)와 애플리케이션이 서비스를 조회하는 데 사용할 수 있는 논리적 이름이라는 두 가지 정보를 디스커버리 에이전트에 알려줍니다. 특정 서비스 디스커버리 에이전트에는 등록 서비스로 다시 전송되는 URL도 필요합니다. 이 URL은 서비스 디스커버리 에이전트에서 상태 확인을 수행하는 데 사용할 수 있습니다. 그런 다음 서비스 클라이언트는 디스커버리 에이전트와 통신하여 서비스의 위치를 찾습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F12_Huaylupo.png)

Figure 3.12 A 서비스 디스커버리 에이전트는 서비스의 물리적 위치를 추상화한다.

### 3.4.4 Communicating a microservice’s health

A 서비스 디스커버리 에이전트는 클라이언트를 서비스 위치로 안내하는 교통 경찰 역할만 하는 것은 아닙니다. 클라우드 기반 마이크로서비스 애플리케이션에서는 실행 중인 서비스의 여러 인스턴스가 있는 경우가 많습니다. 조만간 이러한 서비스 인스턴스 중 하나가 실패합니다. 서비스 검색 에이전트는 등록된 각 서비스 인스턴스의 상태를 모니터링하고 라우팅 테이블에서 실패한 서비스 인스턴스를 제거하여 클라이언트가 실패한 서비스 인스턴스를 보내지 않도록 합니다.

마이크로서비스가 시작된 후 서비스 검색 에이전트는 상태 확인 인터페이스를 계속 모니터링하고 ping하여 해당 서비스를 사용할 수 있는지 확인합니다. 이것은 그림 3.9의 4단계입니다. 그림 3.13은 이 단계에 대한 컨텍스트를 제공합니다.


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F13_Huaylupo.png)

그림 3.13 서비스 디스커버리 에이전트는 노출된 상태 URL을 사용하여 마이크로서비스 상태를 확인합니다.

일관된 상태 확인 인터페이스를 구축하면 클라우드 기반 모니터링 도구를 사용하여 문제를 감지하고 적절하게 대응할 수 있습니다. 서비스 검색 에이전트가 서비스 인스턴스의 문제를 발견하면 병든 인스턴스를 종료하거나 추가 서비스 인스턴스를 가동하는 등의 수정 조치를 취할 수 있습니다.

REST를 사용하는 마이크로서비스 환경에서 상태 확인 인터페이스를 구축하는 가장 간단한 방법은 JSON 페이로드 및 HTTP 상태 코드를 반환할 수 있는 HTTP 끝점을 노출하는 것입니다. Spring Boot 기반이 아닌 마이크로서비스에서 서비스 상태를 반환하는 끝점을 작성하는 것은 개발자의 책임인 경우가 많습니다.

Spring Boot에서 엔드포인트를 노출하는 것은 간단하며 Spring Actuator 모듈을 포함하도록 Maven 빌드 파일을 수정하는 것 외에는 아무것도 포함되지 않습니다. Spring Actuator는 서비스 상태를 이해하고 관리하는 데 도움이 되는 즉시 사용 가능한 운영 엔드포인트를 제공합니다. Spring Actuator를 사용하려면 Maven 빌드 파일에 다음 종속성을 포함해야 합니다.
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
http://localhost:8080/actuator/health라이선스 서비스 의 끝점에 도달하면 반환된 상태 데이터가 표시되어야 합니다. 그림 3.14는 반환된 데이터의 예를 제공합니다.


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH03_F14_Huaylupo.png)

그림 3.14 각 서비스 인스턴스의 상태 확인을 통해 모니터링 도구에서 서비스 인스턴스가 실행 중인지 확인할 수 있습니다.

그림 3.14에서 볼 수 있듯이 상태 확인은 상승 및 하락을 나타내는 지표 이상일 수 있습니다. 또한 마이크로서비스 인스턴스가 실행 중인 서버의 상태에 대한 정보를 제공할 수 있습니다. Spring Actuator를 사용하면 애플리케이션 속성 파일을 통해 기본 구성을 변경할 수 있습니다. 예를 들어:

```yaml
management.endpoints.web.base-path=/
management.endpoints.enabled-by-default=false
management.endpoint.health.enabled=true
management.endpoint.health.show-details=always
management.health.db.enabled=false
management.health.diskspace.enabled=true
```
첫 번째 줄을 사용하면 모든 Actuator 서비스에 대한 기본 경로를 설정할 수 있습니다(예: 이제 상태 엔드포인트가 http://localhost:8080/healthURL에 노출됨 ). 나머지 줄을 사용하면 기본 서비스를 비활성화하고 원하는 서비스를 활성화할 수 있습니다.

> **참고** Spring Actuator에 의해 노출되는 모든 서비스를 알고 싶다면 다음 Spring 문서를 읽는 것이 좋습니다.

https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html .



## 3.5 관점을 하나로 모으기

클라우드의 마이크로서비스는 믿을 수 없을 정도로 단순해 보입니다. 그러나 이들과 함께 성공하려면 아키텍트, 개발자 및 DevOps 엔지니어의 관점을 응집력 있는 비전으로 끌어들이는 통합된 뷰가 필요합니다. 이러한 각 관점에 대한 주요 내용은 다음과 같습니다.

- 아키텍트 — 비즈니스 문제의 자연스러운 윤곽에 초점을 맞춥니다. 당신의 비즈니스 문제 영역을 설명하고 당신이 말하는 이야기를 들어보세요. 대상 마이크로서비스 후보가 나타날 것입니다. 또한 큰 규모의 소규모 서비스 그룹으로 시작하는 것보다 거친 마이크로서비스로 시작하여 작은 서비스로 다시 리팩토링하는 것이 더 낫다는 점을 기억하십시오. 대부분의 우수한 아키텍처와 마찬가지로 마이크로서비스 아키텍처는 창발적이며 순간에 미리 계획되지 않습니다.

- 소프트웨어 엔지니어(개발자라고도 함) — 서비스가 작다고 해서 좋은 디자인 원칙이 사라진 것은 아닙니다. 서비스의 각 계층이 개별적인 책임을 지는 계층화된 서비스 구축에 중점을 둡니다. 코드에서 프레임워크를 구축하려는 유혹을 피하고 각 마이크로서비스를 완전히 독립적으로 만드십시오. 성급한 프레임워크 설계 및 채택은 애플리케이션 수명 주기 후반에 막대한 유지 관리 비용을 초래할 수 있습니다.

- DevOps 엔지니어 — 서비스는 진공 상태에 존재하지 않습니다. 서비스의 수명 주기를 조기에 설정하십시오. DevOps 관점은 서비스 구축 및 배포를 자동화하는 방법뿐만 아니라 서비스 상태를 모니터링하고 문제가 발생했을 때 대응하는 방법에 초점을 맞춰야 합니다. 서비스 운영에는 비즈니스 로직을 작성하는 것보다 더 많은 작업과 사전 고려가 필요한 경우가 많습니다. 부록 C에서 Prometheus와 Grafana를 사용하여 이를 달성하는 방법을 설명합니다.

## 요약

- 마이크로서비스로 성공하려면 아키텍트, 개발자, DevOps라는 세 가지 팀 관점을 통합해야 합니다.

- 마이크로서비스는 강력한 아키텍처 패러다임이지만 장단점이 있습니다. 모든 애플리케이션이 마이크로서비스 애플리케이션이어야 하는 것은 아닙니다.

- 아키텍트의 관점에서 마이크로서비스는 작고 독립적이며 분산되어 있습니다. 마이크로서비스는 경계가 좁고 소량의 데이터를 관리해야 합니다.

- 개발자의 관점에서 마이크로서비스는 일반적으로 서비스에서 데이터를 보내고 받기 위한 페이로드로 JSON을 사용하여 REST 스타일 디자인을 사용하여 구축됩니다.

- 국제화의 주요 목표는 다양한 형식과 언어로 콘텐츠를 제공하는 응용 프로그램을 개발하는 것입니다.

- HATEOAS는 응용 프로그램 상태의 엔진인 Hypermedia를 나타냅니다. Spring HATEOAS는 주어진 리소스에 대한 관련 링크를 표시하는 HATEOAS 원칙을 따르는 API를 생성할 수 있는 작은 프로젝트입니다.

- DevOps 관점에서 마이크로서비스를 패키징, 배포 및 모니터링하는 방법은 매우 중요합니다.

- 기본적으로 Spring Boot를 사용하면 서비스를 단일 실행 가능한 JAR 파일로 전달할 수 있습니다. 생성된 JAR 파일에 포함된 Tomcat 서버가 서비스를 호스팅합니다.

- Spring Boot 프레임워크에 포함된 Spring Actuator는 서비스의 실행 시간에 대한 정보와 함께 서비스의 운영 상태에 대한 정보를 노출합니다.