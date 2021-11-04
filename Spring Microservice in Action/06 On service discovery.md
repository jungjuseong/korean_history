# 6 On service discovery

이 장에서는 다음을 다룹니다.

- 클라우드 기반 애플리케이션에서 서비스 검색이 중요한 이유
- 서비스 검색과 로드 밸런서의 장단점
- Spring Netflix Eureka 서버 설정
- Eureka에 Spring Boot 마이크로 서비스 등록
- 클라이언트 측 부하 분산을 위해 Spring Cloud Load Balancer 라이브러리 사용

모든 분산 아키텍처에서 시스템이 위치한 호스트 이름 또는 IP 주소를 찾아야 합니다. 이 개념은 분산 컴퓨팅이 시작될 때부터 존재했으며 공식적으로는 "서비스 검색"으로 알려져 있습니다. 서비스 검색은 애플리케이션에서 사용하는 모든 원격 서비스의 주소가 포함된 속성 파일을 유지 관리하는 것처럼 간단하거나 UDDI 리포지토리와 같이 형식화된 것일 수 있습니다. 서비스 검색은 두 가지 주요 이유로 마이크로서비스, 클라우드 기반 애플리케이션에 중요합니다.

- 수평적 확장 또는 확장: 이 패턴은 일반적으로 클라우드 서비스 및 더 많은 컨테이너 내부에 더 많은 서비스 인스턴스를 추가하는 것과 같이 애플리케이션 아키텍처에서 조정이 필요합니다.

- 탄력성: 이 패턴은 비즈니스에 영향을 주지 않고 아키텍처 또는 서비스 내에서 문제의 영향을 흡수하는 능력을 나타냅니다. 마이크로서비스 아키텍처는 단일 서비스(또는 서비스 인스턴스)의 문제가 서비스 소비자에게 연쇄적으로 발생하는 것을 방지하는 데 극도로 민감해야 합니다.

첫번째는, 서비스 검색을 통해 애플리케이션 팀은 환경에서 실행되는 서비스 인스턴스 수를 수평으로 빠르게 확장할 수 있습니다. 서비스 소비자는 서비스의 물리적 위치에서 추상화됩니다. 서비스 소비자는 실제 서비스 인스턴스의 물리적 위치를 모르기 때문에 사용 가능한 서비스 풀에서 새 서비스 인스턴스를 추가하거나 제거할 수 있습니다.

서비스 소비자를 방해하지 않고 서비스를 신속하게 확장할 수 있는 이러한 기능은 매력적인 개념입니다. 이는 모놀리식 단일 테넌트(예: 한 고객) 애플리케이션을 구축하는 데 익숙한 개발 팀을 더 크고 더 나은 하드웨어(수직적 확장)를 추가하는 관점에서 확장에 대한 보다 강력한 접근 방식에 대한 생각에서 멀어지게 할 수 있습니다. 더 많은 서비스로 더 많은 서버 추가(수평 확장).

모놀리식 접근 방식은 일반적으로 개발 팀이 용량 요구 사항을 과도하게 구매하도록 유도합니다. 용량 증가는 덩어리와 급증으로 나타나며 매끄럽고 꾸준한 프로세스는 드뭅니다. 예를 들어, 일부 휴일 전에 전자 상거래 사이트에 대한 요청의 증가 수를 고려하십시오. 마이크로서비스를 통해 새로운 서비스 인스턴스를 온디맨드로 확장할 수 있습니다. 서비스 검색은 이러한 배포를 추상화하여 서비스 소비자로부터 멀리 떨어지도록 합니다.

서비스 디스커버리의 두 번째 이점은 애플리케이션 복원력을 높이는 데 도움이 된다는 것입니다. 마이크로 서비스 인스턴스가 비정상이거나 사용할 수 없게 되면 대부분의 서비스 디스커버리 엔진은 사용 가능한 서비스의 내부 목록에서 해당 인스턴스를 제거합니다. 서비스 디스커버리 엔진이 사용할 수 없는 서비스를 중심으로 서비스를 라우팅하므로 다운 서비스로 인한 피해가 최소화됩니다.

이 모든 것이 다소 복잡하게 들릴 수 있으며 DNS 또는 로드 밸런서와 같은 검증된 방법을 사용하여 서비스 검색을 용이하게 할 수 없는 이유가 궁금할 것입니다. 마이크로서비스 기반 애플리케이션, 특히 클라우드에서 실행되는 애플리케이션에서 이것이 작동하지 않는 이유를 살펴보겠습니다. 그런 다음 아키텍처에서 Eureka Discovery를 구현하는 방법을 배웁니다.

## 6.1 Where’s my service?

여러 서버에 분산된 리소스를 호출하는 애플리케이션이 있는 경우 해당 리소스의 물리적 위치를 찾아야 합니다. 클라우드가 아닌 세계에서 서비스 위치 확인은 종종 DNS와 네트워크 로드 밸런서의 조합을 통해 해결되었습니다(그림 6.1). 이 전통적인 시나리오에서는 애플리케이션이 조직의 다른 부분에 있는 서비스를 호출해야 할 때 애플리케이션이 호출하려는 서비스를 고유하게 나타내는 경로와 함께 일반 DNS 이름을 사용하여 서비스 호출을 시도했습니다. DNS 이름은 널리 사용되는 F5 로드 밸런서(http://f5.com)와 같은 상용 로드 밸런서 또는 HAProxy(http://haproxy.org)와 같은 오픈 소스 로드 밸런서로 확인됩니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH06_F01_Huaylupo.png)

그림 6.1 기존 서비스 위치 확인 모델은 DNS와 로드 밸런서를 사용합니다.

기존 시나리오에서 로드 밸런서는 서비스 소비자로부터 요청을 받으면 사용자가 액세스하려는 경로를 기반으로 라우팅 테이블에서 물리적 주소 항목을 찾았습니다. 이 라우팅 테이블 항목에는 서비스를 호스팅하는 하나 이상의 서버 목록이 포함되어 있습니다. 그런 다음 로드 밸런서는 목록에서 서버 중 하나를 선택하고 해당 서버로 요청을 전달했습니다.

이 레거시 모델에서는 서비스의 각 인스턴스가 하나 이상의 애플리케이션 서버에 배포되었습니다. 이러한 응용 프로그램 서버의 수는 종종 정적(서비스를 호스팅하는 응용 프로그램 서버의 수는 증가하거나 감소하지 않음)이고 지속적(응용 프로그램 서버를 실행하는 서버가 충돌하는 경우 동일한 상태로 복원됩니다. 이전과 동일한 IP 주소 및 구성으로 충돌이 발생한 시간). 고가용성 형태를 달성하기 위해 보조 유휴 로드 밸런서는 기본 로드 밸런서에 ping을 실행하여 활성 상태인지 확인했습니다. 활성 상태가 아니면 보조 로드 밸런서가 활성화되어 기본 로드 밸런서의 IP 주소를 인수하고 요청을 처리하기 시작했습니다.

이러한 유형의 모델은 기업 데이터 센터의 네 벽 내부에서 실행되는 애플리케이션과 정적 서버 그룹에서 실행되는 비교적 적은 수의 서비스에서 잘 작동하지만 클라우드 기반 마이크로서비스 애플리케이션에서는 잘 작동하지 않습니다. 그 이유는 다음과 같습니다.

로드 밸런서는 고가용성으로 만들 수 있지만 전체 인프라에 대한 단일 장애 지점입니다. 로드 밸런서가 다운되면 이에 의존하는 모든 애플리케이션도 다운됩니다. 로드 밸런서를 고가용성으로 만들 수 있지만 로드 밸런서는 애플리케이션 인프라 내에서 중앙 집중식 요충지인 경향이 있습니다.

서비스를 로드 밸런서의 단일 클러스터로 중앙 집중화하면 여러 서버에 걸쳐 로드 밸런싱 인프라를 수평으로 확장하는 능력이 제한됩니다. 많은 상용 로드 밸런서는 이중화 모델과 라이선스 비용이라는 두 가지 제약을 받습니다.

대부분의 상용 로드 밸런서는 중복성을 위해 핫 스왑 모델을 사용하므로 로드를 처리할 단일 서버만 있는 반면 보조 로드 밸런서는 기본 로드 밸런서가 다운되는 경우 장애 조치용으로만 존재합니다. 본질적으로 하드웨어의 제약을 받습니다. 상용 로드 밸런서에는 더 가변적인 모델이 아닌 고정 용량에 맞춰진 제한적인 라이선스 모델도 있습니다.

대부분의 기존 로드 밸런서는 정적으로 관리됩니다. 서비스의 빠른 등록 및 등록 취소를 위해 설계되지 않았습니다. 기존 로드 밸런서는 중앙 집중식 데이터베이스를 사용하여 규칙에 대한 경로를 저장하며, 새 경로를 추가하는 유일한 방법은 공급업체의 독점 API를 통하는 경우가 많습니다.

로드 밸런서는 서비스에 대한 프록시 역할을 하기 때문에 서비스 소비자 요청은 이를 물리적 서비스에 매핑해야 합니다. 이 변환 계층은 서비스에 대한 매핑 규칙을 수동으로 정의하고 배포해야 하기 때문에 서비스 인프라에 또 다른 복잡성 계층을 추가하는 경우가 많습니다. 또한 기존 로드 밸런서 시나리오에서는 새 서비스 인스턴스가 시작될 때 새 서비스 인스턴스 등록이 수행되지 않습니다.

이 네 가지 이유는 로드 밸런서에 대한 일반적인 비난이 아닙니다. 로드 밸런서는 중앙 집중식 네트워크 인프라를 통해 대부분의 애플리케이션의 크기와 규모를 처리할 수 있는 기업 환경에서 잘 작동합니다. 그러나 로드 밸런서는 여전히 SSL 종료를 중앙 집중화하고 서비스 포트 보안을 관리하는 역할을 합니다. 로드 밸런서는 뒤에 있는 모든 서버에 대한 인바운드(ingress) 및 아웃바운드(egress) 포트 액세스를 잠글 수 있습니다. 이러한 "최소 네트워크 액세스" 개념은 PCI(Payment Card Industry) 규정 준수와 같은 업계 표준 인증 요구 사항을 충족하려고 할 때 종종 중요한 구성 요소입니다.

그러나 엄청난 양의 트랜잭션과 중복성을 처리해야 하는 클라우드에서는 중앙 집중식 네트워크 인프라가 궁극적으로 제대로 작동하지 않습니다. 이는 효율적으로 확장되지 않고 비용 효율적이지 않기 때문입니다. 이제 강력한 서비스 검색 메커니즘을 구현하는 방법을 살펴보겠습니다.

## 6.2 Service discovery in the cloud

클라우드 기반 마이크로서비스 환경을 위한 솔루션은 다음과 같은 서비스 검색 메커니즘을 사용하는 것입니다.

- 고가용성 - 서비스 검색은 서비스 검색 클러스터의 여러 노드에서 서비스 조회를 공유할 수 있는 "핫" 클러스터링 환경을 지원할 수 있어야 합니다. 노드를 사용할 수 없게 되면 클러스터의 다른 노드가 인계할 수 있어야 합니다.

클러스터는 여러 서버 인스턴스의 그룹으로 정의할 수 있습니다. 이 환경의 모든 인스턴스는 동일한 구성을 가지며 함께 작동하여 고가용성, 안정성 및 확장성을 제공합니다. 로드 밸런서와 결합된 클러스터는 장애 조치를 제공하여 서비스 중단을 방지하고 세션 데이터를 저장하기 위한 세션 복제를 제공할 수 있습니다.

- 피어 투 피어 - 서비스 검색 클러스터의 각 노드는 서비스 인스턴스의 상태를 공유합니다.

- 로드 밸런싱 - 서비스 검색은 모든 서비스 인스턴스에서 요청을 동적으로 로드 밸런싱해야 합니다. 이렇게 하면 서비스 호출이 해당 서비스에서 관리하는 모든 서비스 인스턴스에 분산됩니다. 여러 면에서 서비스 검색은 많은 초기 웹 애플리케이션 구현에서 발견되는 보다 정적인 수동 관리 로드 밸런서를 대체합니다.

- 복원력 - 서비스 검색의 클라이언트는 서비스 정보를 로컬로 캐시해야 합니다. 로컬 캐싱은 서비스 검색 기능의 점진적인 저하를 허용하므로 서비스 검색 서비스를 사용할 수 없게 되더라도 애플리케이션은 여전히 ​​작동하고 로컬 캐시에 유지 관리되는 정보를 기반으로 서비스를 찾을 수 있습니다.

- 내결함성 - 서비스 검색은 서비스 인스턴스가 정상이 아닐 때 감지하고 클라이언트 요청을 받을 수 있는 사용 가능한 서비스 목록에서 해당 인스턴스를 제거해야 합니다. 서비스를 통해 이러한 결함을 감지하고 사람의 개입 없이 조치를 취해야 합니다.

다음 섹션에서는

- 클라우드 기반 서비스 검색 에이전트의 작동 방식에 대한 개념적 아키텍처 안내

- 클라이언트 측 캐싱 및 로드 밸런싱을 통해 서비스 검색 에이전트를 사용할 수 없는 경우에도 서비스가 계속 작동하는 방법을 보여줍니다.

- Spring Cloud 및 Netflix의 Eureka 서비스 검색 에이전트를 사용하여 서비스 검색을 구현하는 방법을 보여줍니다.

### 6.2.1 서비스 발견의 아키텍처

서비스 발견에 대한 논의를 시작하려면 네 가지 개념을 이해해야 합니다. 이러한 일반 개념은 종종 모든 서비스 검색 구현에서 공유됩니다.

- 서비스 등록 - 서비스가 서비스 검색 에이전트에 등록하는 방법

- 서비스 주소의 클라이언트 조회 - 서비스 클라이언트가 서비스 정보를 조회하는 방법

- 정보 공유 - 노드가 서비스 정보를 공유하는 방법

- 상태 모니터링 - 서비스가 서비스 검색 에이전트에 상태를 다시 전달하는 방법

서비스 검색의 주요 목표는 위치를 수동으로 구성할 필요 없이 서비스가 물리적 위치를 나타내는 아키텍처를 갖는 것입니다. 그림 6.2는 서비스 인스턴스가 추가 및 제거되는 방법과 서비스 검색 에이전트를 업데이트하고 사용자 요청을 처리하는 데 사용할 수 있게 되는 방법을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH06_F02_Huaylupo.png)

그림 6.2 서비스 인스턴스가 추가되거나 제거되면 서비스 검색 노드가 업데이트되고 사용자 요청을 처리하는 데 사용할 수 있습니다.

그림 6.2는 앞의 네 가지(서비스 등록, 서비스 검색 조회, 정보 공유, 상태 모니터링)의 흐름과 서비스 검색 패턴을 구현할 때 일반적으로 발생하는 일을 보여줍니다. 그림에서 하나 이상의 서비스 검색 노드가 시작되었습니다. 이러한 서비스 검색 인스턴스에는 일반적으로 앞에 로드 밸런서가 없습니다.

서비스 인스턴스가 시작되면 하나 이상의 서비스 검색 인스턴스가 인스턴스에 액세스하는 데 사용할 수 있는 물리적 위치, 경로 및 포트를 등록합니다. 서비스의 각 인스턴스에는 고유한 IP 주소와 포트가 있지만 나타나는 각 서비스 인스턴스는 동일한 서비스 ID로 등록됩니다. 서비스 ID는 동일한 서비스 인스턴스 그룹을 고유하게 식별하는 키에 불과합니다.

서비스는 일반적으로 하나의 서비스 검색 서비스 인스턴스에만 등록됩니다. 대부분의 서비스 검색 구현은 데이터 전파의 피어-투-피어 모델을 사용합니다. 여기서 각 서비스 인스턴스 주변의 데이터는 클러스터의 다른 모든 노드에 전달됩니다. 서비스 검색 구현에 따라 전파 메커니즘은 하드코딩된 서비스 목록을 사용하여 전파하거나 가십 또는 감염 스타일 프로토콜과 같은 멀티캐스팅 프로토콜을 사용하여 다른 노드가 클러스터의 변경 사항을 "검색"할 수 있도록 할 수 있습니다.

> **참고** 가십 또는 감염 스타일 프로토콜에 대해 자세히 알고 싶다면 다음을 검토하는 것이 좋습니다. Consul의 "가십 프로토콜"(https://www.consul.io/docs/internals/gossip.html) 또는 Brian Storti의 게시물, "SWIM: 확장 가능한 멤버십 프로토콜"(https://www.brianstorti.com/swim/).

마지막으로 각 서비스 인스턴스는 서비스 검색 서비스에 의해 상태를 푸시하거나 풀합니다. 양호한 상태 확인을 반환하지 못한 모든 서비스는 사용 가능한 서비스 인스턴스 풀에서 제거됩니다. 서비스가 서비스 검색 서비스에 등록되면 해당 기능을 사용해야 하는 애플리케이션이나 서비스에서 사용할 준비가 된 것입니다. 클라이언트가 서비스를 검색하기 위한 다양한 모델이 있습니다.

첫 번째 접근 방식으로 클라이언트는 서비스가 호출될 때마다 서비스 위치를 확인하기 위해 서비스 검색 엔진에만 의존합니다. 이 접근 방식을 사용하면 등록된 마이크로서비스 인스턴스를 호출할 때마다 서비스 검색 엔진이 호출됩니다. 불행히도 이 접근 방식은 서비스 클라이언트가 서비스를 찾고 호출하는 서비스 검색 엔진에 완전히 의존하기 때문에 취약합니다.

보다 강력한 접근 방식은 클라이언트 측 로드 밸런싱이라고 하는 것을 사용합니다. 이 메커니즘은 영역별 또는 라운드 로빈과 같은 알고리즘을 사용하여 호출 서비스의 인스턴스를 호출합니다. "라운드 로빈 알고리즘 로드 밸런싱"이라고 할 때 클라이언트 요청을 여러 서버에 분산시키는 방법을 의미합니다. 이는 클라이언트 요청을 각 서버에 차례로 전달하는 것으로 구성됩니다. Eureka와 함께 클라이언트 측 로드 밸런서를 사용하는 이점은 서비스 인스턴스가 다운될 때 레지스트리에서 제거된다는 것입니다. 완료되면 클라이언트 측 로드 밸런서는 레지스트리 서비스와의 지속적인 통신을 설정하여 수동 개입 없이 자체 업데이트합니다. 그림 6.3은 이 접근 방식을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH06_F03_Huaylupo.png)

그림 6.3 클라이언트 측 로드 밸런싱은 서비스 위치를 캐시하여 서비스 클라이언트가 모든 호출에서 서비스 검색에 연결할 필요가 없도록 합니다.

이 모델에서 소비 클라이언트가 서비스를 호출해야 할 때

- 서비스 소비자(클라이언트)가 요청하는 모든 인스턴스에 대해 검색 서비스에 접속한 다음 서비스 소비자의 시스템에서 로컬로 데이터를 캐시합니다.

- 클라이언트가 서비스를 호출하려고 할 때마다 서비스 소비자는 캐시에서 서비스에 대한 위치 정보를 찾습니다. 일반적으로 클라이언트 측 캐싱은 라운드 로빈 로드 밸런싱 알고리즘과 같은 간단한 로드 밸런싱 알고리즘을 사용하여 서비스 호출이 여러 서비스 인스턴스에 분산되도록 합니다.

그런 다음 클라이언트는 주기적으로 검색 서비스에 접속하고 서비스 인스턴스의 캐시를 새로 고칩니다. 클라이언트 캐시는 최종적으로 일관성이 있지만 클라이언트가 새로 고침을 위해 서비스 검색 인스턴스에 접속하고 호출이 이루어지면 호출이 정상적이지 않은 서비스 인스턴스로 전달될 위험이 항상 있습니다.
서비스를 호출하는 동안 서비스 호출이 실패하면 로컬 서비스 검색 캐시가 무효화되고 서비스 검색 클라이언트는 서비스 검색 에이전트에서 항목을 새로 고치려고 시도합니다. 이제 일반적인 서비스 검색 패턴을 가져와 O-stock 문제 영역에 적용해 보겠습니다.

### 6.2.2 Spring과 Netflix Eureka를 사용한 서비스 검색

이 섹션에서는 서비스 검색 에이전트를 설정하여 서비스 검색을 구현한 다음 에이전트에 두 개의 서비스를 등록합니다. 이 구현에서는 서비스 검색으로 검색된 정보를 사용하여 다른 서비스에서 서비스를 호출합니다. Spring Cloud는 서비스 검색 에이전트에서 정보를 조회하는 여러 방법을 제공합니다. 각 접근 방식의 장단점을 살펴보겠습니다.

다시 말하지만, Spring Cloud는 이러한 유형의 설정을 수행하기 쉽게 만듭니다. Spring Cloud와 Netflix의 Eureka Service Discovery 엔진을 사용하여 서비스 검색 패턴을 구현합니다. 클라이언트 측 부하 분산을 위해 Spring Cloud Load Balancer를 사용합니다.

> **참고** 이 장에서는 리본을 사용하지 않습니다. Ribbon은 Spring Cloud를 사용하는 애플리케이션 간의 REST 기반 통신을 위한 사실상의 클라이언트 측 로드 밸런서였습니다. Netflix Ribbon 클라이언트 측 로드 밸런싱은 안정적인 솔루션이었지만 이제 유지 관리 모드에 들어갔으므로 불행히도 더 이상 개발되지 않습니다.

이번 절에서는 Ribbon을 대체할 Spring Cloud Load Balancer 사용법을 설명한다. 현재 Spring Cloud Load Balancer는 아직 활발히 개발 중이므로 곧 새로운 기능이 제공될 예정입니다. 이전 두 장에서는 라이선스 서비스를 단순하게 유지하고 라이선스 데이터에 라이선스에 대한 조직 이름을 포함했습니다. 이 장에서는 조직 정보를 자체 서비스로 나눕니다. 그림 6.4는 O-stock 마이크로서비스를 위해 Eureka를 사용한 클라이언트 측 캐싱의 구현을 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH04_F01_Huaylupo.png)

그림 6.4 O-stock의 라이선스 및 조직 서비스와 함께 클라이언트 측 캐싱 및 Eureka를 구현하면 Eureka 서버의 부하를 줄이고 Eureka를 사용할 수 없게 되는 경우 클라이언트 안정성을 향상시킬 수 있습니다.

라이선스 서비스가 호출되면 지정된 조직 ID와 연결된 조직 정보를 검색하기 위해 조직 서비스를 호출합니다. 조직 서비스 위치의 실제 확인은 서비스 검색 레지스트리에 보관됩니다. 이 예에서는 조직 서비스의 두 인스턴스를 서비스 검색 레지스트리에 등록한 다음 클라이언트 측 로드 밸런싱을 사용하여 각 서비스 인스턴스에서 레지스트리를 조회하고 캐시합니다. 그림 6.4는 이 배열을 보여줍니다.

서비스가 부트스트랩되면 라이선스 및 조직 서비스가 Eureka 서비스에 등록됩니다. 이 등록 프로세스는 시작되는 서비스의 서비스 ID와 함께 각 서비스 인스턴스의 물리적 위치 및 포트 번호를 Eureka에 알려줍니다.

라이선스 서비스가 조직 서비스를 호출할 때 Spring Cloud Load Balancer를 사용하여 클라이언트 측 로드 밸런싱을 제공합니다. 로드 밸런서는 Eureka 서비스에 접속하여 서비스 위치 정보를 검색한 다음 이를 로컬로 캐시합니다.

주기적으로 Spring Cloud Load Balancer는 Eureka 서비스를 ping하고 서비스 위치의 로컬 캐시를 새로 고칩니다.

이제 모든 새 조직 서비스 인스턴스가 로컬 라이선스 서비스에 표시되고 비정상 인스턴스는 로컬 캐시에서 제거됩니다. Spring Cloud Eureka 서비스를 설정하여 이 디자인을 구현할 것입니다.

## 6.3 Spring Eureka 서비스 구축

이 섹션에서는 Spring Boot를 사용하여 Eureka 서비스를 설정합니다. Spring Cloud Config 서비스와 마찬가지로 Spring Cloud Eureka 서비스 설정은 새 Spring Boot 프로젝트를 빌드하고 주석 및 구성을 적용하는 것으로 시작됩니다. Spring Initializr(https://start.spring.io/)로 이 프로젝트를 생성하는 것부터 시작합시다. 이를 달성하기 위해 Spring Initializr에서 다음 단계를 수행합니다.

1. 프로젝트 유형으로 Maven을 선택합니다.

2. 언어로 Java를 선택하십시오.

3. 2.2.x 최신 버전이나 더 안정적인 Spring 버전을 선택하세요.

4. com.optimagrowth를 그룹으로, eurekaserver를 아티팩트로 작성합니다.

5. 옵션 목록을 확장하고 Eureka Server를 이름으로, Eureka server를 설명으로, com.optimagrowth.eureka를 패키지 이름으로 작성하십시오.

6. JAR 패키징을 선택하십시오.

J7. ava 버전으로 Java 11을 선택하십시오.

그림 6.5와 같이 Eureka Server, Config Client 및 Spring Boot Actuator 종속성을 추가합니다. Listing 6.1은 Eureka Server pom.xml 파일을 보여준다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH06_F05_Huaylupo.png)

Figure 6.5 Eureka Server dependencies in Spring Initializr

Listing 6.1 Maven pom file for the Eureka Server
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"     
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0   
  https://maven.apache.org/xsd/maven-4.0.0.xsd">
     <modelVersion>4.0.0</modelVersion>
     <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.5.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
     </parent>
     <groupId>com.optimagrowth</groupId>
     <artifactId>eurekaserver</artifactId>
     <version>0.0.1-SNAPSHOT</version>
     <name>Eureka Server</name>
     <description>Eureka Server</description>

     <properties>
        <java.version>11</java.version>
        <spring-cloud.version>Hoxton.SR1</spring-cloud.version>
     </properties>

     <dependencies>
        <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
           <groupId>org.springframework.cloud
           </groupId> ❶
           <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-starter-netflix-eureka-server
           </artifactId> ❷
           <exclusions> ❸
              <exclusion>
                 <groupId>org.springframework.cloud</groupId>
                 <artifactId>spring-cloud-starter-ribbon</artifactId>
              </exclusion>
              <exclusion>
                 <groupId>com.netflix.ribbon</groupId>
                 <artifactId>ribbon-eureka</artifactId>
              </exclusion>
           </exclusions>
        </dependency>
        <dependency>
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-starter-loadbalancer
           </artifactId> ❹
        </dependency>
        <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-test</artifactId>
           <scope>test</scope>
           <exclusions>
              <exclusion>
                 <groupId>org.junit.vintage</groupId>
                 <artifactId>junit-vintage-engine</artifactId>
              </exclusion>
           </exclusions>
        </dependency>
     </dependencies>

<!--Rest of pom.xml omitted for conciseness
</project>
```
❶ Tells Maven to include the client that connects to a Spring ConfigServer to retrieve the application’s configuration

❷ Tells Maven to include the Eureka libraries

❸ Excludes the Netflix Ribbon libraries

❹ Tells Maven to include the Spring Cloud Load Balancer libraries

The next step is to set up the src/main/resources/bootstrap.yml file with the settings we need to retrieve the configuration from the Spring Config Server previously created in chapter 5. We also need to add the configuration to disable Ribbon as our default client-side load balancer. The following listing shows how your bootstrap.yml file should look.

Listing 6.2 Setting up the Eureka bootstrap.yml file
```yml
spring:
   application:
      name: eureka-server ❶
   cloud:
      config: 
         uri: http://localhost:8071 ❷

      loadbalancer: ❸
         ribbon:
            enabled: false
```
❶ Names the Eureka service so the Spring Cloud Config client knows which service it’s looking up

❷ Specifies the location of the Spring Cloud Config Server

❸ Because Ribbon is still the default client-side load balancer, we disable it using the loadbalancer.ribbon.enabled configuration.

Once we add the Spring Configuration Server information in the bootstrap file on the Eureka Server and we disable Ribbon as our load balancer, we can continue with the next step. That step adds the configuration needed to set up the Eureka service running in standalone mode (no other nodes in the cluster) in the Spring Configuration Server.

In order to achieve this, we must create the Eureka Server configuration file in the repository we set up in the Spring Config service. (Remember, we can specify a repository as a classpath, filesystem, GIT, or Vault.) The configuration file should be named as the spring.application.name property previously defined in the Eureka bootstrap.yml file of the Eureka service. For purposes of this example, we will create the eureka-server.yml file in classpath/configserver/src/main/resources/config/eureka-server.yml. Listing 6.3 shows the contents of this file.

> **NOTE** If you didn’t follow the code listings in chapter 5, you can download the code from this link: https://github.com/ihuaylupo/manning-smia/tree/master/chapter5.

Listing 6.3 Setting up the Eureka configuration in the Spring Config Server

```yml
server:
   port: 8070                                                              ❶
eureka:
   instance:
      hostname: localhost ❷
   client:
      registerWithEureka: false ❸
      fetchRegistry: false ❹
      serviceUrl:
         defaultZone: ❺
            http://${eureka.instance.hostname}:${server.port}/eureka/
   server:
      waitTimeInMsWhenSyncEmpty: 5 ❻
```
❶ Eureka Server의 수신 포트를 설정합니다.

❷ Eureka 인스턴스 호스트 이름 설정

❸ Config Server에 Eureka 서비스에 등록하지 말라고 ...

❹ ... 그리고 레지스트리 정보를 로컬로 캐시하지 않으려면

❺ 서비스 URL 제공

❻ 서버가 요청을 받기 전에 대기할 초기 시간을 설정합니다.

목록 6.3에 설정된 주요 속성은 다음과 같습니다.

- `server.port` — 기본 포트를 설정.
-
- `eureka.instance.hostname` — Eureka 서비스에 대한 Eureka 인스턴스 호스트 이름을 설정합니다.

- `eureka.client.registerWithEureka` — Spring Boot Eureka 애플리케이션이 시작될 때 Eureka에 등록하지 않도록 Config Server에 지시합니다.

- `eureka.client.fetchRegistry` — false로 설정하면 Eureka 서비스가 시작될 때 레지스트리 정보를 로컬로 캐시할 필요가 없다고 알려줍니다. Eureka 클라이언트를 실행할 때 Eureka에 등록할 Spring Boot 서비스에 대해 이 값을 변경하고 싶을 것입니다.

- `eureka.client.serviceUrl.defaultZone` — 모든 클라이언트에 대한 서비스 URL을 제공. eureka.instance.hostname과 server.port 속성의 조합입니다.

- `eureka.server.waitTimeInMsWhenSyncEmpty — `서버가 요청을 받기 전에 대기할 시간

목록 6.3의 마지막 속성인 `eureka.server.waitTimeInMsWhenSyncEmpty`는 시작하기 전에 대기할 시간(밀리초)을 나타냅니다. 서비스를 로컬에서 테스트할 때 Eureka가 등록된 서비스를 즉시 광고하지 않기 때문에 이 줄을 사용해야 합니다. 기본적으로 모든 서비스가 광고하기 전에 등록할 수 있는 기회를 주기 위해 5분을 기다립니다. 로컬 테스트에 이 라인을 사용하면 Eureka 서비스가 시작되고 여기에 등록된 서비스를 표시하는 데 걸리는 시간을 단축하는 데 도움이 됩니다.

> **참고** Eureka에 등록된 개별 서비스는 Eureka 서비스에 표시되는 데 최대 30초가 소요됩니다. Eureka는 서비스를 사용할 준비가 되었다고 알리기 전에 10초 간격으로 서비스에서 세 번의 연속적인 하트비트 핑이 필요하기 때문입니다. 자체 서비스를 배포하고 테스트할 때 이 점을 염두에 두십시오.

Eureka 서비스에 대해 수행해야 하는 설정 작업의 마지막 부분은 Eureka 서비스를 시작하는 데 사용하는 애플리케이션 부트스트랩 클래스에 주석을 추가하는 것입니다. Eureka 서비스의 경우 src/main/java/com/optimagrowth/eureka/EurekaServerApplication.java 클래스 파일에서 애플리케이션 부트스트랩 클래스인 EurekaServerApplication을 찾을 수 있습니다. 다음 목록은 주석을 추가할 위치를 보여줍니다.

Listing 6.4 Annotating the bootstrap class to enable the Eureka Server

```java
@SpringBootApplication
@EnableEurekaServer ❶
public class EurekaServerApplication {

    public static void main(String[] args) {
      SpringApplication.run(EurekaServerApplication.class, args);
    }

}
```
❶ Spring 서비스에서 Eureka Server 활성화

이 시점에서 새로운 주석 @EnableEurekaServer만 사용하여 서비스를 Eureka 서비스로 활성화합니다. 이제 `mvn spring-boot:run` 또는 `docker-compose` 명령을 실행하여 Eureka 서비스를 시작할 수 있습니다. 시작 명령이 실행되면 등록된 서비스 없이 실행 중인 Eureka 서비스가 있어야 합니다. Eureka 애플리케이션 구성이 포함되어 있으므로 먼저 Spring Config 서비스를 실행해야 합니다. 구성 서비스를 먼저 실행하지 않으면 다음 오류가 발생합니다.
```
Connect Timeout Exception on Url - http://localhost:8071. 
Will be trying the next url if available.      
     com.sun.jersey.api.client.ClientHandlerException: java.net.ConnectException: 
     Connection refused (Connection refused)
```
이전 문제를 방지하려면 Docker Compose로 서비스를 실행해 보십시오. GitHub의 챕터 리포지토리에서 업데이트된 docker-compose.yml 파일을 찾을 수 있음을 기억하십시오. 이제 조직 서비스를 구축해 보겠습니다. 그런 다음 라이선스 및 조직 서비스를 Eureka 서비스에 등록합니다.

## 6.4 Spring Eureka에 서비스 등록하기

이 시점에서 Spring 기반 Eureka Server가 실행 중입니다. 이 섹션에서는 조직 및 라이선스 서비스가 Eureka Server에 등록되도록 구성합니다. 이 작업은 서비스 클라이언트가 Eureka 레지스트리에서 서비스를 조회하도록 하기 위해 수행됩니다. 이 섹션을 마치면 Eureka에 Spring Boot 마이크로서비스를 등록하는 방법을 확실히 이해하게 될 것입니다.

Eureka에 Spring Boot 기반 마이크로서비스를 등록하는 것은 간단한 연습입니다. 이 장의 목적을 위해 서비스 작성과 관련된 모든 Java 코드를 살펴보지는 않겠지만(우리는 의도적으로 그 양의 코드를 작게 유지했습니다), 대신 Eureka 서비스 레지스트리에 서비스를 등록하는 데 중점을 둡니다. 이전 섹션에서 생성되었습니다.

이 섹션에서는 조직 서비스라고 하는 새로운 서비스를 소개합니다. 이 서비스에는 CRUD 엔드포인트가 포함됩니다. 다음 링크에서 라이선스 및 조직 서비스에 대한 코드를 다운로드할 수 있습니다.

https://github.com/ihuaylupo/manning-smia/tree/master/chapter6/Initial

> 참고 이 시점에서 보유하고 있는 다른 마이크로서비스를 사용할 수 있습니다. 서비스 검색에 등록할 때 서비스 ID 이름에 주의하십시오.

우리가 해야 할 첫 번째 일은 우리 조직과 라이선싱 서비스의 pom.xml 파일에 Spring Eureka 의존성을 추가하는 것입니다. 다음 목록은 방법을 보여줍니다.

Listing 6.5 조직의 서비스 pom.xml에 Spring Eureka 종속성 추가하기
```xml
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId> ❶
      spring-cloud-starter-netflix-eureka-client
   </artifactId>
</dependency>
```
❶ Includes the Eureka libraries so that the service can register with Eureka

The spring-cloud-starter-netflix-eureka-client artifact holds the JAR files that Spring Cloud uses to interact with your Eureka service. After we’ve set up the pom.xml file, we need to make sure we have set the spring.application.name in the bootstrap.yml file of the service we want to register. The following listings, 6.6 and 6.7, indicate how to do this.

Listing 6.6 Adding the spring.application.name to the organization service
```yml
spring:
   application:
       name: organization-service ❶
       profiles:
          active: dev
   cloud:
       config: 
          uri: http://localhost:8071
```
❶ Logical name of the service that will be registered with Eureka

Listing 6.7 Adding the spring.application.name to the licensing service
```yml
spring:
   application:
       name: licensing-service ❶
       profiles:
          active: dev
   cloud:
       config: 
          uri: http://localhost:8071
```
❶ Logical name of the service that will be registered with Eureka

Every service registered with Eureka will have two components associated with it: the application ID and the instance ID. The application ID represents a group service instance. In a Spring Boot microservice, the application ID is always the value set by the spring.application.name property. For our organization service, this property is creatively named organization-service, and for our licensing service, it’s named licensing-service. The instance ID will be a randomly autogenerated number to represent a single service instance.

Next, we need to tell Spring Boot to register the organization and licensing services with Eureka. This registration is done via additional configuration in the service’s configuration files managed by the Spring Config service. For this example, these files are located in the following two files for the Spring Configuration Server project. Listing 6.8 then shows how to register the services with Eureka.
```
src/main/resources/config/organization-service.properties

src/main/resources/config/licensing-service.properties
```
NOTE Remember, the configuration file can be either a YAML or a properties file and can be located in the classpath, filesystem, Git repository, or Vault. It depends on the configuration you’ve set in the Spring Config Server. For this example, we selected the classpath and properties file, but feel free to make the changes that best suit your needs.

Listing 6.8 Modifying the service application.properties files for Eureka
```
eureka.instance.preferIpAddress = true ❶

eureka.client.registerWithEureka = true ❷

eureka.client.fetchRegistry = true ❸

eureka.client.serviceUrl.defaultZone = 
       http://localhost:8070/eureka/       ❹
```
❶ Registers the IP address of the service rather than the server name

❷ Registers the service with Eureka

❸ Pulls down a local copy of the registry

❹ Sets the location of the Eureka service

If you have an application.yml file, your file should look like that shown in the following code to register the services with Eureka. The eureka.instance.preferIpAddress property tells Eureka that you want to register the service’s IP address with Eureka rather than its hostname.
```yml
eureka:
   instance:
      preferIpAddress: true
   client:
      registerWithEureka: true
      fetchRegistry: true
      serviceUrl: defaultZone: http://localhost:8070/eureka/
```
IP 주소를 선호하는 이유는 무엇입니까?

기본적으로 Eureka는 호스트 이름으로 연락하는 서비스를 등록합니다. 이는 서비스에 DNS 지원 호스트 이름이 할당되는 서버 기반 환경에서 잘 작동합니다. 그러나 컨테이너 기반 배포(예: Docker)에서 컨테이너는 임의로 생성된 호스트 이름으로 시작되고 컨테이너에 대한 DNS 항목이 없습니다. eureka.instance.preferIpAddress를 true로 설정하지 않으면 클라이언트 애플리케이션이 호스트 이름의 위치를 ​​올바르게 확인하지 못합니다. 해당 컨테이너에 대한 DNS 항목이 없습니다. preferredIpAddress 속성을 설정하면 Eureka 서비스에 클라이언트가 IP 주소로 광고되기를 원한다는 것을 알립니다.

개인적으로 우리는 항상 이 속성을 true로 설정합니다. 클라우드 기반 마이크로서비스는 임시적이고 상태 비저장이어야 합니다. 이들은 마음대로 시작하고 종료할 수 있으므로 이러한 유형의 서비스에는 IP 주소가 더 적합합니다.

eureka.client.registerWithEureka 속성은 조직 및 라이선스 서비스에 Eureka에 등록하도록 알리는 트리거입니다. eureka.client .fetchRegistry 속성은 레지스트리의 로컬 복사본을 가져오도록 Spring Eureka 클라이언트에 지시합니다. 이 속성을 true로 설정하면 조회할 때마다 Eureka 서비스를 호출하는 대신 레지스트리를 로컬로 캐시합니다. 30초마다 클라이언트 소프트웨어는 레지스트리 변경 사항에 대해 Eureka 서비스에 다시 연결합니다.

> 참고 이 두 속성은 기본적으로 true로 설정되어 있지만 설명 목적으로만 응용 프로그램 구성 파일에 속성을 포함했습니다. 코드는 해당 속성을 true로 설정하지 않고도 작동합니다.

마지막 속성인 eureka.serviceUrl.defaultZone은 클라이언트가 서비스 위치를 확인하는 데 사용하는 Eureka 서비스의 쉼표로 구분된 목록을 포함합니다. 우리의 목적을 위해 우리는 하나의 유레카 서비스만 가질 것입니다. 각 서비스의 부트스트랩 파일에서 이전에 정의된 모든 키-값 속성을 선언할 수도 있습니다. 그러나 아이디어는 구성을 Spring Config 서비스에 위임하는 것입니다. 이것이 우리가 Spring Config 서비스 저장소의 서비스 구성 파일에 모든 구성을 등록하는 이유입니다. 지금까지 이러한 서비스의 부트스트랩 파일에는 애플리케이션 이름, 프로필(필요한 경우) 및 Spring Cloud 구성 URI만 포함되어야 합니다.

Eureka and high availability

Setting up multiple URL services isn’t enough for high availability. The eureka .service-Url.defaultZone attribute only provides a list of Eureka services for the client to communicate with. You also need to set up the Eureka services to replicate the contents of their registries with each other. A group of Eureka registries communicate with each other using a peer-to-peer communication model, where each Eureka service must be configured so that it knows about the other nodes in the cluster.

Setting up a Eureka cluster is outside of the scope of this book. If you’re interested in setting up a Eureka cluster, visit the Spring Cloud project’s website for further information:

https://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-eureka-server

At this point, we have two services registered with our Eureka service. We can use Eureka’s REST API or the Eureka dashboard to see the contents of the registry. We’ll explain each in the following sections.

### 6.4.1 Eureka’s REST API

To see all the instances of a service in the REST API, select the following GET endpoint:
```
http://<eureka service>:8070/eureka/apps/<APPID>
```
For instance, to see the organization service in the registry, you would call the following endpoint: http://localhost:8070/eureka/apps/organization-service. Figure 6.6 shows the response.



Figure 6.6 The Eureka REST API showing the organization service. The response shows the IP address of the service instances registered in Eureka, along with the service status.

The default format returned by the Eureka service is XML. Eureka can also return the data in figure 6.6 as a JSON payload, but you’ll have to set the Accept HTTP header to application/json. An example of the JSON payload is shown in figure 6.7.



그림 6.7 JSON 결과가 포함된 Eureka REST API

### 6.4.2 유레카 대시보드

Eureka 서비스가 시작되면 브라우저에서 http://localhost:8070으로 이동하여 Eureka 대시보드를 볼 수 있습니다. Eureka 대시보드를 통해 서비스의 등록 상태를 볼 수 있습니다. 그림 6.8은 Eureka 대시보드의 예를 보여줍니다.

이제 조직 및 라이선스 서비스를 등록했으므로 서비스 검색을 사용하여 서비스를 조회하는 방법을 살펴보겠습니다.



그림 6.8 등록된 조직 및 라이선스 인스턴스가 있는 Eureka 대시보드

유레카와 서비스 스타트업: 조급해하지 마세요

서비스가 Eureka에 등록되면 Eureka는 서비스를 사용할 수 있게 되기 전에 30초 동안 3번의 연속적인 상태 확인을 기다립니다. 이 워밍업 기간은 일부 개발자를 실망시킵니다. 서비스가 시작된 직후 Eureka에 전화를 걸면 Eureka가 서비스를 등록하지 않은 것으로 가정합니다.

이것은 Eureka 서비스와 애플리케이션 서비스(라이선스 및 조직)가 모두 동시에 시작되기 때문에 Docker 환경에서 실행되는 코드 예제에서 분명합니다. 응용 프로그램을 시작한 후 서비스 자체가 시작되었음에도 불구하고 서비스를 찾을 수 없다는 404 오류를 수신할 수 있습니다. 이 경우 서비스에 전화를 걸기 전에 30초를 기다리십시오.

프로덕션 환경에서 Eureka 서비스는 이미 실행 중입니다. 기존 서비스를 배포하는 경우 이전 서비스는 여전히 요청을 받을 수 있습니다.

## 6.5 서비스 검색을 사용하여 서비스 조회

이 섹션에서는 조직 서비스의 위치에 대한 직접적인 지식 없이 라이선스 서비스가 조직 서비스를 호출하도록 하는 방법을 설명합니다. 라이선스 서비스는 Eureka를 사용하여 조직의 물리적 위치를 조회합니다.

우리의 목적을 위해 서비스 소비자가 Spring Cloud Load Balancer와 상호 작용할 수 있는 세 가지 다른 Spring/Netflix 클라이언트 라이브러리를 살펴보겠습니다. 이러한 라이브러리는 Load Balancer와 상호 작용하기 위한 가장 낮은 추상화 수준에서 가장 높은 수준으로 이동합니다. 우리가 탐색할 라이브러리는 다음과 같습니다.

- 스프링 디스커버리 클라이언트

- Spring Discovery Client 지원 REST 템플릿

- Netflix Feign 클라이언트

이러한 각 클라이언트를 살펴보고 라이선스 서비스의 맥락에서 사용을 살펴보겠습니다. 클라이언트의 세부 사항을 시작하기 전에 코드에 몇 가지 편의 클래스와 메서드를 작성했습니다. 동일한 서비스 엔드포인트를 사용하여 다양한 클라이언트 유형으로 플레이할 수 있습니다.

먼저 라이선스 서비스를 위한 새로운 경로를 포함하도록 src/main/java/com/optimagrowth/license/controller /LicenseController.java 클래스를 수정했습니다. 이 새 경로를 사용하면 서비스를 호출하는 데 사용할 클라이언트 유형을 지정할 수 있습니다. 이것은 Load Balancer를 통해 조직의 서비스를 호출하는 다양한 방법을 탐색할 때 단일 경로를 통해 각 메커니즘을 시도할 수 있도록 하는 도우미 경로입니다. 다음 목록은 LicenseController 클래스의 새 경로에 대한 코드를 보여줍니다.

Listing 6.9 Calling the licensing service with different REST clients
```java
@RequestMapping(value="/{licenseId}/{clientType}",
                method = RequestMethod.GET)             ❶
public License getLicensesWithClient( 
           @PathVariable("organizationId") String organizationId,
           @PathVariable("licenseId") String licenseId,
           @PathVariable("clientType") String clientType) {
              return licenseService.getLicense(organizationId,
                     licenseId, clientType);
}
```
❶ clientType 매개변수는 사용할 Spring REST 클라이언트의 유형을 결정합니다.

목록 6.9에서 경로에 전달된 clientType 매개변수는 코드 예제에서 사용할 클라이언트 유형을 결정합니다. 이 경로에서 전달할 수 있는 특정 유형은 다음과 같습니다.

- 디스커버리 - 디스커버리 클라이언트와 표준 Spring RestTemplate 클래스를 사용하여 조직 서비스를 호출합니다.

- Rest - 향상된 Spring RestTemplate을 사용하여 로드 밸런서 서비스를 호출합니다.

- Feign - Netflix의 Feign 클라이언트 라이브러리를 사용하여 로드 밸런서를 통해 서비스를 호출합니다.

> **참고** 세 가지 유형의 클라이언트 모두에 대해 동일한 코드를 사용하고 있기 때문에 필요하지 않은 것처럼 보이더라도 특정 클라이언트에 대한 주석이 표시되는 상황을 볼 수 있습니다. 예를 들어, 텍스트가 클라이언트 유형 중 하나만 설명하는 경우에도 코드에서 @EnableDiscoveryClient 및 @EnableFeignClients 주석을 모두 볼 수 있습니다. 이는 예제에 하나의 코드베이스를 사용할 수 있도록 하기 위한 것입니다. 이러한 중복 및 코드가 발생하면 호출할 것입니다. 아이디어는 항상 그렇듯이 필요에 가장 적합한 것을 선택한다는 것입니다.

src/main/java/com/optimagrowth/license/service/LicenseService .java 클래스에서 경로에 전달된 clientType을 기반으로 확인되는 간단한 retrieveOrganizationInfo() 메소드를 추가했습니다. 이 클라이언트 유형은 조직 서비스 인스턴스를 조회하는 데 사용됩니다. LicenseService 클래스의 getLicense() 메서드는 retrieveOrganizationInfo() 메서드를 사용하여 Postgres 데이터베이스에서 조직 데이터를 검색합니다. 다음 목록은 LicenseService 클래스의 getLicense() 서비스에 대한 코드를 보여줍니다.

목록 6.10 REST 호출에 여러 메서드를 사용하는 getLicense() 함수
```java
public License getLicense(String licenseId, String organizationId, String clientType){
   License license = licenseRepository.findByOrganizationIdAndLicenseId
                    (organizationId, licenseId);
   if (null == license) {
      throw new IllegalArgumentException(String.format(
         messages.getMessage("license.search.error.message", null, null),
         licenseId, organizationId));
   }
   Organization organization = retrieveOrganizationInfo(organizationId,
                               clientType);
   if (null != organization) {
      license.setOrganizationName(organization.getName());
      license.setContactName(organization.getContactName());
      license.setContactEmail(organization.getContactEmail());
      license.setContactPhone(organization.getContactPhone());
   }
   return license.withComment(config.getExampleProperty());
}
```

Spring Discovery Client, Spring RestTemplate 클래스 또는 라이선싱 서비스의 service/client 패키지에서 Feign 라이브러리를 사용하여 구축한 각 클라이언트를 찾을 수 있습니다. 다른 클라이언트로 getLicense() 서비스를 호출하려면 다음 GET 엔드포인트를 호출해야 합니다.

http://<licensing service Hostname/IP>:<licensing service Port>/v1/ organization/<organizationID>/license/<licenseID>/<client type( feign, discovery, rest)>

### 6.5.1 Spring Discovery Client로 서비스 인스턴스 찾기

Spring Discovery Client는 로드 밸런서와 그 안에 등록된 서비스에 대한 가장 낮은 수준의 액세스를 제공합니다. Discovery Client를 사용하여 Spring Cloud Load Balancer 클라이언트에 등록된 모든 서비스와 해당 URL을 쿼리할 수 있습니다.

다음으로 Discovery Client를 사용하여 Load Balancer에서 조직 서비스 URL 중 하나를 검색한 다음 표준 RestTemplate 클래스를 사용하여 서비스를 호출하는 간단한 예제를 빌드합니다. Discovery Client를 사용하려면 먼저 다음 목록과 같이 LicenseServiceApplication 클래스에 @EnableDiscoveryClient 주석을 추가해야 합니다.

Listing 6.11 Eureka Discovery Client를 사용하도록 부트스트랩 클래스 설정하기
```java
package com.optimagrowth.license;
@SpringBootApplication
@RefreshScope
@EnableDiscoveryClient ❶
public class LicenseServiceApplication {

    public static void main(String[] args) {
       SpringApplication.run(LicenseServiceApplication.class, args);
    }
}
```
❶ Eureka 디스커버리 클라이언트 활성화

@EnableDiscoveryClient는 애플리케이션이 Discovery Client 및 Spring Cloud Load Balancer 라이브러리를 사용할 수 있도록 하는 Spring Cloud의 트리거입니다. 이제 Spring Discovery Client를 통해 조직 서비스를 호출하는 코드 구현을 살펴보겠습니다. 다음 목록은 이 구현을 보여줍니다. 이 코드는 src/main/java/com/optimagrowth/license/service/client/OrganizationDiscoveryClient.java 파일에서 찾을 수 있습니다.

목록 6.12 디스커버리 클라이언트를 사용하여 정보 조회
```java
@Component
public class OrganizationDiscoveryClient {

   @Autowired
   private DiscoveryClient discoveryClient; ❶

   public Organization getOrganization(String organizationId) {
       RestTemplate restTemplate = new RestTemplate();
       List<ServiceInstance> instances = ❷
           discoveryClient.getInstances("organization-service");

       if (instances.size()==0) return null;
       String serviceUri = String.format      
              ("%s/v1/organization/%s",instances.get(0)
              .getUri().toString(),
              organizationId); ❸

       ResponseEntity<Organization> restExchange = ❹
              restTemplate.exchange(
              serviceUri, HttpMethod.GET,
              null, Organization.class, organizationId);

       return restExchange.getBody();
   }
}
```
❶ Discovery Client를 클래스에 주입

❷ 조직 서비스의 모든 인스턴스 목록을 가져옵니다.

❸ 서비스 엔드포인트 검색

❹ 표준 Spring RestTemplate 클래스를 사용하여 서비스 호출

코드에서 첫 번째 관심 항목은 DiscoveryClient 클래스입니다. 이 클래스를 사용하여 Spring Cloud Load Balancer와 상호 작용합니다. 그런 다음 Eureka에 등록된 조직 서비스의 모든 인스턴스를 검색하려면 getInstances() 메서드를 사용하고 ServiceInstance 개체 목록을 검색하기 위해 찾고 있는 서비스 키를 전달합니다. ServiceInstance 클래스는 호스트 이름, 포트 및 URI를 포함하여 서비스의 특정 인스턴스에 대한 정보를 보유합니다.

목록 6.12에서는 목록의 첫 번째 ServiceInstance 클래스를 사용하여 서비스를 호출하는 데 사용할 수 있는 대상 URL을 빌드합니다. 대상 URL이 있으면 표준 Spring RestTemplate을 사용하여 조직 서비스를 호출하고 데이터를 검색할 수 있습니다.

디스커버리 클라이언트와 실생활

어떤 서비스와 서비스 인스턴스가 등록되어 있는지 이해하기 위해 서비스가 로드 밸런서를 쿼리해야 하는 경우에만 Discovery Client를 사용해야 합니다. 다음을 포함하여 목록 6.12의 코드에는 몇 가지 문제가 있습니다.

Spring Cloud 클라이언트 측 로드 밸런서를 활용하고 있지 않습니다. Discovery Client를 직접 호출하면 서비스 목록을 얻을 수 있지만 호출할 반환된 서비스 인스턴스를 선택하는 것은 사용자의 책임이 됩니다.
너무 많은 일을 하고 있습니다. 코드에서 서비스를 호출하는 데 사용할 URL을 작성해야 합니다. 작은 일이지만 작성을 피할 수 있는 모든 코드 조각은 디버그해야 하는 코드 조각이 하나 줄어듭니다.
관찰력이 있는 Spring 개발자는 코드에서 RestTemplate 클래스를 직접 인스턴스화했음을 알아차렸을 수도 있습니다. 이것은 일반적으로 Spring 프레임워크가 @Autowired 주석을 통해 RestTemplate 클래스를 주입하도록 하기 때문에 일반적인 Spring REST 호출과 반대입니다.

목록 6.12에서 RestTemplate 클래스를 인스턴스화했습니다. @EnableDiscoveryClient를 통해 애플리케이션 클래스에서 Spring Discovery Client를 활성화하면 Spring 프레임워크에서 관리하는 모든 REST 템플릿에는 로드 밸런서가 활성화된 인터셉터가 해당 인스턴스에 주입됩니다. 이렇게 하면 RestTemplate 클래스로 URL이 생성되는 방식이 변경됩니다. RestTemplate을 직접 인스턴스화하면 이 동작을 피할 수 있습니다.

### 6.5.2 로드 밸런서 인식 Spring REST 템플릿으로 서비스 호출

다음으로 Load Balancer를 인식하는 REST 템플릿을 사용하는 방법의 예를 살펴보겠습니다. 이것은 Spring을 통해 로드 밸런서와 상호 작용하기 위한 보다 일반적인 메커니즘 중 하나입니다. 로드 밸런서 인식 RestTemplate 클래스를 사용하려면 Spring Cloud @LoadBalanced 주석으로 RestTemplate 빈을 정의해야 합니다.

라이선스 서비스의 경우 RestTemplate 빈을 생성하는 데 사용할 메서드는 src/main/java/com/optimagrowth/license/LicenseServiceApplication.java의 LicenseServiceApplication 클래스에서 찾을 수 있습니다. 다음 목록은 Load Balancer 지원 Spring RestTemplate 빈을 생성할 getRestTemplate() 메서드를 보여줍니다.

Listing 6.13 RestTemplate 생성 방법에 주석 달기 및 정의하기
```java

@SpringBootApplication
@RefreshScope
public class LicenseServiceApplication {
     public static void main(String[] args) {
        SpringApplication.run(LicenseServiceApplication.class, args);
     }

     @LoadBalanced ❶
     @Bean
     public RestTemplate getRestTemplate(){
        return new RestTemplate();
     }
}
```
❶ 조직 서비스에 대한 모든 인스턴스 목록을 가져옵니다.

이제 지원되는 RestTemplate 클래스에 대한 빈 정의가 정의되었으므로 RestTemplate 빈을 사용하여 서비스를 호출할 때마다 이를 사용하여 클래스에 자동 연결하기만 하면 됩니다.

지원되는 RestTemplate 클래스를 사용하면 대상 서비스에 대한 URL이 정의되는 방식에서 한 가지 작은 차이점을 제외하고는 표준 Spring RestTemplate 클래스와 거의 비슷하게 동작합니다. RestTemplate 호출에서 서비스의 물리적 위치를 사용하는 대신 호출하려는 서비스의 Eureka 서비스 ID를 사용하여 대상 URL을 빌드해야 합니다. 다음 목록을 통해 이 호출을 볼 수 있습니다. 이 목록의 코드는 service/client/OrganizationRestTemplateClient.java 클래스 파일에서 찾을 수 있습니다.

목록 6.14 로드 밸런서 지원 RestTemplate을 사용하여 서비스 호출
```java
@Component
public class OrganizationRestTemplateClient {
   @Autowired
   RestTemplate restTemplate;

   public Organization getOrganization(String organizationId){
      ResponseEntity<Organization> restExchange =
         restTemplate.exchange(
            "http://organization-service/v1/
               organization/{organizationId}",             ❶
             HttpMethod.GET, null, 
             Organization.class, organizationId);

      return restExchange.getBody();
   }
}
```
❶ When using a Load Balancer–backed RestTemplate, builds the target URL with the Eureka service ID

This code should look somewhat similar to the previous example except for two key differences. First, the Spring Cloud Discovery Client is nowhere in sight, and second, the URL used in the restTemplate.exchange() call should look odd to you. Here’s that call:
```
restTemplate.exchange(
   "http://organization-service/v1/organization/{organizationId}",
   HttpMethod.GET, null, Organization.class, organizationId);
```
The server name in the URL matches the application ID of the organization service key that you used to register the organization service with Eureka:
```
http://{applicationid}/v1/organization/{organizationId} 

```
로드 밸런서 사용 RestTemplate 클래스는 전달된 URL을 구문 분석하고 전달된 모든 것을 서버 이름으로 사용하여 서비스 인스턴스에 대한 로드 밸런서를 쿼리합니다. 실제 서비스 위치와 포트는 개발자로부터 완전히 추상화됩니다. 또한 RestTemplate 클래스를 사용하여 Spring Cloud Load Balancer는 모든 서비스 인스턴스 간에 모든 요청을 라운드 로빈으로 로드 밸런싱합니다.

### 6.5.3 Netflix Feign 클라이언트로 서비스 호출
Spring Load Balancer 지원 RestTemplate 클래스의 대안은 Netflix의 Feign 클라이언트 라이브러리입니다. Feign 라이브러리는 REST 서비스를 호출하기 위해 다른 접근 방식을 취합니다. 이 접근 방식을 사용하면 개발자는 먼저 Java 인터페이스를 정의한 다음 Spring Cloud Load Balancer가 호출할 Eureka 기반 서비스를 매핑하기 위해 Spring Cloud 주석을 추가합니다. Spring Cloud 프레임워크는 대상 REST 서비스를 호출하기 위해 프록시 클래스를 동적으로 생성합니다. 인터페이스 정의 외에 서비스를 호출하기 위해 작성된 코드는 없습니다.

라이선싱 서비스에서 Feign 클라이언트를 사용하려면 라이선싱 서비스의 src/main/java/com/optimagrowth/license/LicenseServiceApplication.java 클래스 파일에 @EnableFeignClients라는 새 주석을 추가해야 합니다. 다음 목록은 이 코드를 보여줍니다.

Listing 6.15 라이선싱 서비스에서 Spring Cloud/Netflix Feign 클라이언트 활성화
```java
@SpringBootApplication
@EnableFeignClients ❶
public class LicenseServiceApplication {

   public static void main(String[] args) {
      SpringApplication.run(LicenseServiceApplication.class, args);
   }
}
```
❶ 이 주석은 코드에서 Feign 클라이언트를 사용하는 데 필요합니다.

이제 라이선싱 서비스에서 Feign 클라이언트를 사용하도록 설정했으므로 조직 서비스의 끝점을 호출하는 데 사용할 수 있는 Feign 클라이언트 인터페이스 정의를 살펴보겠습니다. 다음 목록은 예를 보여줍니다. 이 목록의 코드는 src/main/java/com/optimagrowth/license/service/client/OrganizationFeignClient .java 클래스 파일에서 찾을 수 있습니다.

Listing 6.16 조직 서비스를 호출하기 위한 Feign 인터페이스 정의하기
```java
//Package and import left out for conciseness
@FeignClient("organization-service") ❶
public interface OrganizationFeignClient {
   @RequestMapping( ❷
      method= RequestMethod.GET,
      value="/v1/organization/{organizationId}",
      consumes="application/json")
   Organization getOrganization
      (@PathVariable("organizationId") ❸
         String organizationId);
}
```
❶ Identifies your service to Feign

❷ Defines the path and action to your endpoint

❸ Defines the parameters passed into the endpoint

목록 6.16에서 @FeignClient 주석을 사용하여 인터페이스가 나타내려는 서비스의 응용 프로그램 ID를 전달했습니다. 그런 다음 클라이언트가 조직 서비스를 호출하기 위해 호출할 수 있는 getOrganization() 메서드를 인터페이스에 정의했습니다.

getOrganization() 메소드를 정의하는 방법은 Spring 컨트롤러 클래스에서 엔드포인트를 노출하는 방법과 정확히 같습니다. 먼저 조직 서비스의 호출에 노출될 HTTP 동사와 끝점을 매핑하는 getOrganization() 메서드에 대한 @RequestMapping 주석을 정의합니다. 둘째, URL에 전달된 조직 ID를 @PathVariable을 사용하여 메서드 호출의 organizationId 매개 변수에 매핑합니다. 조직 서비스에 대한 호출의 반환 값은 getOrganization() 메서드의 반환 값으로 정의된 Organization 클래스에 자동으로 매핑됩니다. OrganizationFeignClient 클래스를 사용하려면 자동 연결하고 사용하기만 하면 됩니다. Feign 클라이언트 코드는 우리를 위해 모든 코딩을 처리합니다.

오류 처리에 대해

표준 Spring RestTemplate 클래스를 사용할 때 모든 서비스 호출 HTTP 상태 코드는 ResponseEntity 클래스의 getStatusCode() 메서드를 통해 반환됩니다. Feign 클라이언트를 사용하면 호출되는 서비스에서 반환된 모든 HTTP 4xx–5xx 상태 코드가 FeignException에 매핑됩니다. FeignException에는 특정 오류 메시지에 대해 구문 분석할 수 있는 JSON 본문이 포함되어 있습니다.

Feign은 오류를 사용자 정의 예외 클래스에 다시 매핑하는 오류 디코더 클래스를 작성하는 기능을 제공합니다. 이 디코더를 작성하는 것은 이 책의 범위를 벗어나지만 여기 Feign GitHub 저장소에서 이에 대한 예를 찾을 수 있습니다.

https://github.com/Netflix/feign/wiki/Custom-error-handling

## 요약

우리는 서비스 발견 패턴을 사용하여 서비스의 물리적 위치를 추상화합니다.

Eureka와 같은 서비스 검색 엔진은 서비스 클라이언트에 영향을 주지 않고 환경에서 서비스 인스턴스를 원활하게 추가 및 제거할 수 있습니다.

클라이언트 측 로드 밸런싱은 서비스를 호출하는 클라이언트에서 서비스의 물리적 위치를 캐싱하여 추가 수준의 성능과 탄력성을 제공할 수 있습니다.

Eureka는 Spring Cloud와 함께 사용할 때 설정 및 구성이 쉬운 Netflix 프로젝트입니다.

Spring Cloud 및 Netflix Eureka에서 Spring Cloud Discovery Client, Spring Cloud Load Balancer 지원 RestTemplate 및 Netflix의 Feign 클라이언트와 같은 세 가지 메커니즘을 사용하여 서비스를 호출할 수 있습니다.
