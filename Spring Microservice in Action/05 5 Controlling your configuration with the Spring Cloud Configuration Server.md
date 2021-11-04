# 5 스프링 클라우드 구성 서버로 구성 제어하기

이 장에서는 다음을 다룹니다.

- 서비스 구성과 서비스 코드 분리
- 스프링 클라우드 구성 서버 구성
- Spring Boot 마이크로서비스를 구성 서버와 통합
- 민감한 속성 암호화
- Spring Cloud Configuration Server와 HashiCorp Vault 통합

소프트웨어 개발자는 애플리케이션 구성을 코드와 분리하여 유지하는 것의 중요성에 대해 항상 듣습니다. 대부분의 시나리오에서 이는 코드에 하드코딩된 값을 사용하지 않는다는 것을 의미합니다. 이 원칙을 잊어버리면 구성이 변경될 때마다 응용 프로그램을 다시 컴파일 및/또는 재배포해야 하므로 응용 프로그램 변경이 더 복잡해질 수 있습니다.

응용 프로그램 코드에서 구성 정보를 완전히 분리하면 개발자와 작업이 재컴파일 프로세스를 거치지 않고 구성을 변경할 수 있습니다. 그러나 이제 개발자는 응용 프로그램과 함께 관리하고 배포할 또 다른 아티팩트가 있으므로 복잡성도 발생합니다.

많은 개발자가 속성 파일(YAML, JSON 또는 XML)을 사용하여 구성 정보를 저장합니다. 이러한 파일에서 애플리케이션을 구성하는 것은 간단한 작업이 되므로 대부분의 개발자는 구성 파일을 소스 제어(해당하는 경우)에 배치하고 파일을 애플리케이션의 일부로 배포하는 것 이상을 하지 않습니다. 이 접근 방식은 소수의 애플리케이션에서 작동할 수 있지만 수백 개의 마이크로서비스를 포함할 수 있는 클라우드 기반 애플리케이션을 처리할 때는 빠르게 무너집니다. 여기서 각 마이크로서비스는 차례로 여러 서비스 인스턴스를 실행할 수 있습니다. 갑자기 쉽고 간단한 프로세스가 큰 문제가 되고 전체 팀이 모든 구성 파일과 씨름해야 합니다.

예를 들어 수백 개의 마이크로 서비스가 있고 각 마이크로 서비스에는 세 가지 환경에 대한 서로 다른 구성이 포함되어 있다고 가정해 보겠습니다. 애플리케이션 외부에서 이러한 파일을 관리하지 않으면 변경 사항이 있을 때마다 코드 저장소에서 파일을 검색하고 통합 프로세스(있는 경우)를 따라 애플리케이션을 다시 시작해야 합니다. 이 재앙적인 시나리오를 피하려면 클라우드 기반 마이크로서비스 개발을 위한 모범 사례로 다음을 고려해야 합니다.

- 배포되는 실제 코드와 애플리케이션 구성을 완전히 분리합니다.

- 환경을 통해 승격될 때 절대 변경할 수 없는 애플리케이션 이미지를 빌드하십시오.

- 시작 시 환경 변수 또는 마이크로 서비스가 읽는 중앙 저장소를 통해 애플리케이션 구성 정보를 주입합니다.

이 장에서는 클라우드 기반 마이크로서비스 애플리케이션에서 애플리케이션 구성 데이터를 관리하는 데 필요한 핵심 원칙과 패턴을 소개합니다. 그런 다음 구성 서버를 구축하고 서버를 Spring 및 Spring Boot 클라이언트와 통합한 다음 보다 민감한 구성을 보호하는 방법을 배웁니다.

## 5.1 구성(및 복잡성) 관리

마이크로서비스 인스턴스는 최소한의 개입으로 신속하게 시작되어야 하기 때문에 클라우드에서 실행되는 마이크로서비스에는 애플리케이션 구성을 관리하는 것이 중요합니다. 사람이 서비스를 배포하기 위해 수동으로 구성하거나 터치해야 하는 경우 구성 드리프트, 예기치 않은 중단 및 애플리케이션 내 확장성 문제에 응답하는 데 지연 시간이 발생할 수 있습니다. 따라야 할 4가지 원칙을 설정하여 애플리케이션 구성 관리에 대한 논의를 시작하겠습니다.

- **분리** - 서비스의 실제 물리적 배포에서 서비스 구성 정보를 완전히 분리해야 합니다. 사실, 애플리케이션 구성은 서비스 인스턴스와 함께 배포되어서는 안됩니다. 대신 구성 정보는 환경 변수로 시작 서비스에 전달되거나 서비스가 시작될 때 중앙 저장소에서 읽어야 합니다.

- **추상** - 또한 서비스 인터페이스 뒤의 구성 데이터에 대한 액세스를 추상화해야 합니다. 파일 기반이든 JDBC 데이터베이스이든 서비스 저장소를 직접 읽는 코드를 작성하는 대신 REST 기반 JSON 서비스를 사용하여 애플리케이션의 구성 데이터를 검색해야 합니다.

- **중앙 집중화** - 클라우드 기반 애플리케이션에는 말 그대로 수백 개의 서비스가 있을 수 있으므로 구성 데이터를 보관하는 데 사용되는 다양한 리포지토리의 수를 최소화하는 것이 중요합니다. 애플리케이션 구성을 가능한 한 적은 수의 리포지토리로 중앙 집중화하십시오.

- **강화** - 애플리케이션 구성 정보가 배포된 서비스와 완전히 분리되고 중앙 집중화되기 때문에 활용하고 구현하는 솔루션은 가용성이 높고 중복되어야 합니다.

기억해야 할 핵심 사항 중 하나는 실제 코드 외부에서 구성 정보를 분리할 때 관리 및 버전 제어가 필요한 외부 종속성을 생성한다는 것입니다. 애플리케이션 구성 데이터를 추적하고 버전을 제어해야 한다는 점을 아무리 강조해도 지나치지 않습니다. 관리되지 않는 애플리케이션 구성은 감지하기 어려운 버그와 계획되지 않은 가동 중단의 온상이 되기 때문입니다.

### 5.1.1 구성 관리 아키텍처

이전 장에서 기억할 수 있듯이 마이크로 서비스에 대한 구성 관리 로드는 마이크로 서비스의 부트스트랩 단계에서 발생합니다. 참고로 그림 5.1은 마이크로서비스 수명 주기를 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F01_Huaylupo.png)

그림 5.1 마이크로 서비스가 시작되면 수명 주기의 여러 단계를 거칩니다. 애플리케이션 구성 데이터는 서비스의 부트스트래핑 단계에서 읽습니다.

5.1에서 설명한 네 가지 원칙(분리, 추상, 중앙 집중화 및 강화)을 사용하여 서비스가 부트스트랩될 때 이러한 원칙이 어떻게 적용되는지 살펴보겠습니다. 그림 5.2는 부트스트랩 프로세스를 보다 자세히 보여주고 구성 서비스가 이 단계에서 어떻게 중요한 역할을 하는지 보여줍니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F02_Huaylupo.png)

그림 5.2 구성 관리 개념 아키텍처

그림 5.2에서는 여러 활동이 진행되고 있습니다. 다음은 그림의 각 단계에 대한 요약입니다.

1. 마이크로 서비스 인스턴스가 나타나면 서비스 엔드포인트를 호출하여 운영 중인 환경에 특정한 구성 정보를 읽습니다. 그런 다음 구성 관리를 위한 연결 정보(연결 자격 증명, 서비스 엔드포인트 등)는 마이크로서비스에게 전달됩니다. 

2. 실제 구성은 저장소에 있습니다. 구성 리포지토리의 구현에 따라 구성 데이터를 보관하는 다양한 방법을 선택할 수 있습니다. 여기에는 소스 제어, 관계형 데이터베이스 또는 키-값 데이터 저장소에 있는 파일이 포함될 수 있습니다.

3. 응용 프로그램 구성 데이터의 실제 관리는 응용 프로그램 배포 방법과 독립적으로 발생합니다. 구성 관리에 대한 변경 사항은 일반적으로 빌드 및 배포 파이프라인을 통해 처리되며, 여기서 수정 사항은 버전 정보로 태그 지정되고 다양한 환경(개발, 스테이징, 프로덕션 등)을 통해 배포될 수 있습니다.

4. 구성 관리가 변경되면 해당 애플리케이션 구성 데이터를 사용하는 서비스에 변경 사항을 통지하고 애플리케이션 데이터 사본을 새로 고쳐야 합니다.

이 시점에서 우리는 구성 관리 패턴의 다양한 부분과 이러한 부분이 서로 어떻게 맞물리는지를 보여주는 개념적 아키텍처를 통해 작업했습니다. 이제 구성 관리를 달성하기 위한 다양한 솔루션을 살펴본 다음 구체적인 구현을 살펴보겠습니다.

### 5.1.2 구현 선택

다행히도 많은 테스트를 거친 오픈 소스 프로젝트 중에서 선택하여 구성 관리 솔루션을 구현할 수 있습니다. 사용 가능한 여러 가지 선택 사항을 살펴보고 비교해 보겠습니다. 표 5.1에 옵션이 나와 있습니다.

표 5.1 구성 관리 시스템 구현을 위한 오픈 소스 프로젝트

**etcd**

- Go로 작성되었습니다. 서비스에 사용 발견 및 키-값 관리. 분산 컴퓨팅 모델에 raft 프로토콜(https://raft.github.io/)을 사용합니다.

- 매우 빠르고 확장 가능
- 분산 가능
- 명령줄 기반
- 간편한 사용 및 설정

**Eureka**

- 넷플릭스에서 작성했습니다. 극도로 실전 테스트를 거쳤습니다. 서비스 검색 및 키-값 관리 모두에 사용됩니다.

- 분산 키-값 저장소
- 유연하지만 설정하는 데 약간의 노력이 필요합니다.
- 즉시 사용 가능한 동적 클라이언트 리프레시 제공

**Consul**

- HashiCorp 작성. etcd 및 Eureka와 유사하지만 분산 컴퓨팅 모델에 대해 다른 알고리즘을 사용합니다.

- 빠름
- DNS와 직접 통합할 수 있는 옵션으로 기본 서비스 검색 제공
- 클라이언트 동적 새로 고침을 즉시 제공하지 않음

**Zookeeper**

- 아파치 프로젝트. 분산 잠금 기능을 제공합니다. 키-값 데이터에 액세스하기 위한 구성 관리 솔루션으로 자주 사용됩니다.

- 가장 오래되고 실전 테스트를 거친 솔루션
- 사용하기 가장 복잡함
- 구성 관리에 사용할 수 있지만 다른 부분에서 이미 사용하고 있는 경우에만 고려하십시오.

**스프링 클라우드 구성서버**

- 오픈 소스 프로젝트. 백엔드가 다른 일반 구성 관리 솔루션을 제공합니다.

- 비분산 키-값 저장소
- Spring 및 비 Spring 서비스에 대한 긴밀한 통합 제공
- 공유 파일 시스템, Eureka, Consul 또는 Git을 포함하여 구성 데이터를 저장하기 위해 여러 백엔드를 사용할 수 있습니다.

표 5.1의 모든 솔루션은 구성 관리 솔루션을 구축하는 데 쉽게 사용할 수 있습니다. 이 장의 예제와 이 책의 나머지 부분에서 우리는 Spring 마이크로서비스 아키텍처에 완벽하게 적응하는 Spring Cloud Configuration Server(종종 Spring Cloud Config 서버 또는 간단히 Config 서버라고 함)를 사용할 것입니다. 이 솔루션을 선택한 이유는

- 설정 및 사용이 쉽습니다.
- Spring Boot와 긴밀하게 통합됩니다. 사용하기 쉬운 몇 가지 주석으로 애플리케이션의 모든 구성 데이터를 문자 그대로 읽을 수 있습니다.
- 구성 데이터를 저장하기 위한 여러 백엔드를 제공합니다.
- Git 플랫폼 및 HashiCorp Vault와 직접 통합할 수 있습니다. 이 장의 뒷부분에서 이러한 항목에 대해 설명합니다.

이 장의 나머지 부분에서는 다음을 수행합니다.

1. 스프링 클라우드 구성 서버를 설정합니다. 애플리케이션 구성 데이터를 제공하는 세 가지 다른 메커니즘을 시연할 것입니다. 하나는 파일 시스템을 사용하고, 다른 하나는 Git 리포지토리를 사용하고, 다른 하나는 HashiCorp Vault를 사용합니다.

2. 데이터베이스에서 데이터를 검색하는 라이선스 서비스를 계속 구축합니다.

3. Spring Cloud Config 서비스를 라이선스 서비스에 연결하여 애플리케이션의 구성 데이터를 제공합니다.



## 5.2 스프링 클라우드 구성 서버 구축

Spring Cloud Configuration Server는 Spring Boot를 기반으로 구축된 REST 기반 애플리케이션입니다. 구성 서버는 독립 실행형 서버로 제공되지 않습니다. 대신 기존 Spring Boot 애플리케이션에 이를 포함하거나 서버에 포함된 새 Spring Boot 프로젝트를 시작하도록 선택할 수 있습니다. 가장 좋은 방법은 사물을 분리하는 것입니다.

구성 서버를 구축하기 위해 가장 먼저 해야 할 일은 Spring Initializr(https://start.spring.io/)로 Spring Boot 프로젝트를 생성하는 것입니다. 이를 달성하기 위해 Initializr 형식으로 다음 단계를 구현합니다. 입력이 완료되면 Spring Initializr 양식은 그림 5.3과 5.4와 같이 보일 것입니다. 
폼에서

1. 프로젝트 유형으로 Maven을 선택합니다.

2. 언어로 Java를 선택하십시오.

3. 최신 또는 더 안정적인 Spring 버전을 선택하십시오.

4. com.optimagrowth를 그룹으로, configserver를 아티팩트로 작성합니다.

5. 옵션 목록을 확장하고 작성하십시오.

- 구성 서버를 이름으로 사용합니다.
- 설명으로 구성 서버입니다.
- com.optimagrowth.configserver를 패키지 이름으로 사용합니다.
- JAR 패키징을 선택하십시오.

6. Java 버전으로 Java 11을 선택하십시오.

7. 구성 서버 및 Spring Boot Actuator 종속성을 추가하십시오.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F03_Huaylupo.png)

그림 5.3 Spring Configuration Server의 Spring Initializr 설정

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F04_Huaylupo.png)

그림 5.4 Spring Initializr Config 서버와 Spring Boot Actuator 종속성

양식을 만들고 선호하는 IDE에 Maven 프로젝트로 가져오면 다음 목록의 코드와 같은 구성 서버 프로젝트 디렉토리의 루트에 pom.xml 파일이 있어야 합니다.

Listing 5.1 Maven pom file for the Spring Configuration Server
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
        <version>2.2.4.RELEASE</version> ❶
        <relativePath/> <!-- lookup parent from repository -->
     </parent>
     <groupId>com.optimagrowth</groupId>
     <artifactId>configserver</artifactId>
     <version>0.0.1-SNAPSHOT</version>
     <name>Configuration Server</name>
     <description>Configuration Server</description>
     <properties>
        <java.version>11</java.version>
        <spring-cloud.version>
             Hoxton.SR1
        </spring-cloud.version> ❷
     </properties>

     <dependencies> ❸
        <dependency>
             <groupId>org.springframework.cloud</groupId>
             <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-test</artifactId>
             <scope>test</scope>
             <exclusions>
                 <exclusion>
                       <groupId>org.junit.vintage</groupId>
                       <artifactId>
                           junit-vintage-engine
                       </artifactId>
                 </exclusion>
             </exclusions>
        </dependency>
     </dependencies>
     <dependencyManagement> ❹
        <dependencies>
             <dependency>
                 <groupId>org.springframework.cloud</groupId>
                 <artifactId>spring-cloud-dependencies</artifactId>
                 <version>${spring-cloud.version}</version>
                 <type>pom</type>
                 <scope>import</scope>
             </dependency>
        </dependencies>
     </dependencyManagement>

     <build>
        <plugins>
             <plugin>
                 <groupId>org.springframework.boot</groupId>
                 <artifactId>spring-boot-maven-plugin</artifactId>
             </plugin>
        </plugins>
     </build>     
</project>
```
❶ Spring Boot version

❷ Spring Cloud version to use

❸ Spring Cloud projects and other dependencies needed to run the ConfigServer

❹ Spring Cloud BOM (Bill of Materials) definition

> **참고** 모든 pom.xml 파일에는 Docker 종속성 및 구성이 포함되어야 하지만 공간을 절약하기 위해 해당 행을 코드 목록에 추가하지 않습니다. 구성 서버의 Docker 구성을 살펴보려면 GitHub 리포지토리의 챕터 5 폴더(https://github.com/ihuaylupo/manning-smia/tree/master/chapter5)를 방문하세요.

전체 pom 파일을 자세히 살펴보진 않겠지만 먼저 몇 가지 핵심 영역에 주목하겠습니다. 목록 5.1의 Maven 파일에서 네 가지 중요한 부분을 볼 수 있습니다. 첫 번째는 Spring Boot 버전이고 다음은 우리가 사용할 Spring Cloud 버전입니다. 이 예제에서는 Spring Cloud의 Hoxton.SR1 버전을 사용합니다. 목록에서 강조 표시된 세 번째 요점은 서비스에서 사용할 특정 종속성이며 마지막 요점은 우리가 사용할 Spring Cloud Config 상위 BOM(Bill of Materials)입니다.

이 상위 BOM에는 클라우드 프로젝트에서 사용되는 모든 타사 라이브러리 및 종속성과 해당 버전을 구성하는 개별 프로젝트의 버전 번호가 포함됩니다. 이 예에서는 이전에 pom 파일의 <properties> 섹션에 정의된 버전을 사용합니다. BOM 정의를 사용하여 Spring Cloud에서 호환되는 하위 프로젝트 버전을 사용하도록 보장할 수 있습니다. 또한 하위 종속성에 대한 버전 번호를 선언할 필요가 없음을 의미합니다.

train을 타라, 해방 기차를 타라

Spring Cloud는 Maven 프로젝트에 레이블을 지정하기 위해 비전통적인 메커니즘을 사용합니다. Spring Cloud는 독립적인 하위 프로젝트의 모음이기 때문에 Spring Cloud 팀은 "릴리스 트레인"이라고 부르는 것을 통해 프로젝트에 대한 업데이트를 릴리스합니다. Spring Cloud를 구성하는 모든 하위 프로젝트는 하나의 Maven BOM으로 패키징되어 전체적으로 릴리스됩니다.

Spring Cloud 팀은 런던 지하철역 이름을 릴리스 이름으로 사용하며, 각 주요 릴리스에는 다음으로 높은 문자가 있는 런던 지하철역이 지정됩니다. Angel, Brixton, Camden, Dalston, Edgware, Finchley 및 Greenwich에서 Hoxton에 이르기까지 여러 릴리스가 있었습니다. Hoxton은 최신 릴리스이지만 여전히 하위 프로젝트에 대한 여러 릴리스 후보 분기가 있습니다.

한 가지 주목해야 할 점은 Spring Boot는 Spring Cloud 릴리스 트레인과 독립적으로 릴리스된다는 것입니다. 따라서 Spring Boot의 다른 버전은 Spring Cloud의 다른 릴리스와 호환되지 않습니다. Spring Cloud 웹사이트(http://projects.spring.io/spring-cloud/)를 참조하면 릴리스 트레인에 포함된 다양한 하위 프로젝트 버전과 함께 Spring Boot와 Spring Cloud 간의 버전 종속성을 확인할 수 있습니다.

Spring Cloud Config Server를 생성하는 다음 단계는 실행될 수 있도록 서버의 핵심 구성을 정의하는 파일을 하나 더 설정하는 것입니다. 이것은 application.properties, application.yml, bootstrap.properties 또는 bootstrap.yml 중 하나일 수 있습니다.

The bootstrap file is a specific Spring Cloud file type and is loaded before the application.properties or application.yml files. The bootstrap file is used for specifying the Spring application name, the Spring Cloud Configuration Git location, encryption/ decryption information, and so forth. Specifically, the bootstrap file is loaded by a parent Spring ApplicationContext, and that parent is loaded before the one that uses the application properties or YAML files.

As for the file extensions, .yml and .properties are just different data formats. You can choose the one you prefer. In this book, you’ll see that we will use bootstrap.yml to define the configuration of the Config Server and the microservices.

Now, to continue, let’s create our bootstrap.yml file in the /src/main/resources folder. This file tells the Spring Cloud Config service what port to listen in on, the application name, the application profiles, and the location where we will store the configuration data. Your bootstrap file should look like that shown in the following listing.

Listing 5.2 Creating our bootstrap.yml file
```yml
spring:
   application:
      name: config-server   ❶
server:
   port: 8071 ❷
```

❶ The Config Server application name (in this case, config-server)

❷ The server port

There are two important parts to highlight in listing 5.2. The first one is the application name. It is vital to name all of the services that we are going to create in our architecture for service discovery, which we will describe in the next chapter. The second point to note is the port the Spring Configuration Server is going to listen in on, in order to provide the requested configuration data.

### 5.2.1 Setting up the Spring Cloud Config bootstrap class

The next step in creating our Spring Cloud Config service is to set up the Spring Cloud Config bootstrap class. Every Spring Cloud service needs a bootstrap class that we can use to launch the service, as we explained in chapters 2 and 3 (where we created the licensing service).

Remember, this class contains several important parts: a Java main() method that acts as the entry point for the service to start in and a set of Spring annotations that tells the starting service what kind of behaviors Spring is going to launch for the service. The following listing shows the bootstrap class for our Spring Cloud Config Server.

Listing 5.3 Setting up the bootstrap class
```java
@SpringBootApplication ❶

@EnableConfigServer ❷

public class ConfigurationServerApplication {

   public static void main(String[] args) { ❸
    SpringApplication.run(ConfigurationServerApplication.class, args);
   }
}
```

❶ Our Config service is a Spring Boot application, so we must mark it with the @SpringBootApplication annotation.

❷ Enables the service as a Spring Cloud Config service

❸ The main method launches the service and starts the Spring container.

The next step is to define the search location for our configuration data. Let’s start with the simplest example: the filesystem.

### 5.2.2 Using the Spring Cloud Config Server with a filesystem

The Spring Cloud Configuration Server uses an entry in the bootstrap.yml file to point to the repository that holds the application’s configuration data. Setting up a filesystem-based repository is the easiest way to accomplish this. To do this, let’s update our bootstrap file. The following listing shows the required contents for this file in order to set up a filesystem repository.

Listing 5.4 Our bootstrap.yml with a filesystem repository

```yml
spring:
   application:
      name: config-server
   profiles:
      active: native ❶
   
   cloud:
      config:
        server:
        #Local configuration: This locations can either of 
        classpath or locations in the filesystem.
           native:
           #Reads from a specific filesystem folder
               search-locations: 
                    file:///{FILE_PATH} ❷
server:
   port: 8071

```
❶ Sets the Spring profile associated with the backend repository (filesystem)

❷ Sets the search location where the configuration files are stored

Because we will use the filesystem for storing application configuration information, we must tell the Spring Cloud Config Server to run with the native profile. Remember, a Spring profile is a core feature that the Spring framework offers. It allows us to map our beans to different environments, such as dev, test, staging, production, native, and others.

> NOTE Remember, native is just a profile created for the Spring Cloud Configuration Server, which indicates that the configuration files are going to be retrieved or read from the classpath or filesystem.

When we use a filesystem-based repository, we’ll also use the native profile because it is a profile in the Config Server that doesn’t use any Git or Vault configurations. Instead, it loads the configuration data directly from a local classpath or a filesystem. Finally, the last part of the bootstrap.yml shown in listing 5.4 provides the Spring Cloud configuration within the directory where the application data resides. For example:

```yml
server:
  native:
    search-locations: file:///Users/illary.huaylupo
```
The important parameter in the configuration entry is search-locations. This parameter provides a comma-separated list of the directories for each application that will have properties managed by the Config Server. In the previous example, we used a filesystem location (file:///Users/illary.huaylupo), but we can also indicate a specific classpath to look for our configuration data. This is set with the following code:
```yml
server:
  native:
    search-locations: classpath:/config
```

> **NOTE** The classpath attribute causes Spring Cloud Config Server to look in the src/main/resources/config folder.

Now that we have set up our Spring Configuration Server, let’s create our licensing service properties files. In order to make this example simple, we’ll use the classpath search location set in the preceding code snippet. Then, like the previous example, we’ll create the licensing properties files in a /config folder.

### 5.2.3 Setting up the configuration files for a service

In this section, we will use the licensing service example that we began in the initial chapters of this book. It will serve as an example of how to use Spring Cloud Config.

NOTE In case you didn’t follow along in the previous chapter’s code listings, you can download the code created in chapter 4 from the following link: https://github.com/ihuaylupo/manning-smia/tree/master/chapter4.

Again, to keep this example simple, we will set up application configuration data for three environments: a default environment for when we run the service locally, a dev environment, and a production environment.

With Spring Cloud Config, everything works off a hierarchy. Your application configuration is represented by the name of the application and then a property file for each environment you want to configure. In each of these environments, we’ll set up the following configuration properties:

- An example property that will be used directly by our licensing service

- A Spring Actuator configuration that we will use in the licensing service

- A database configuration for the licensing service

Figure 5.5 illustrates how we will set up and use the Spring Cloud Config service. One important point to mention is that as you build your Config service, it will be another microservice running in your environment. Once it’s set up, the contents of the service can be accessed via an HTTP-based REST endpoint.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F05_Huaylupo.png)

Figure 5.5 Spring Cloud Config exposes environment-specific properties as HTTP-based endpoints.

The naming convention for the application configuration files are appname-env.properties or appname-env.yml. As you can see from figure 5.5, the environment names translate directly into the URLs that will be accessed to browse the configuration information. Later, when we start the licensing microservice example, the environment we want to run the service against is specified by the Spring Boot profile that you pass in on the command line at service startup. If a profile isn’t passed in on the command line, Spring Boot defaults to the configuration data contained in the application.properties file packaged with the application.

Here is an example of some of the application configuration data we will serve up for the licensing service. This is the data that will be contained within the configserver/src/main/ resources/config/ licensing-service.properties file that was referred to in figure 5.5. Here is part of the contents of that file:
```
...
example.property= I AM THE DEFAULT
spring.jpa.hibernate.ddl-auto=none
spring.jpa.database=POSTGRESQL
spring.datasource.platform=postgres
spring.jpa.show-sql = true
spring.jpa.hibernate.naming-strategy = org.hibernate.cfg.ImprovedNamingStrategy
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
spring.database.driverClassName= org.postgresql.Driver
spring.datasource.testWhileIdle = true
spring.datasource.validationQuery = SELECT 1
management.endpoints.web.exposure.include=*
management.endpoints.enabled-by-default=true
```

Think before you implement

We advise against using a filesystem-based solution for medium to large cloud applications. Using the filesystem approach means that you need to implement a shared file mount point for all Configuration Servers that want to access the application’s configuration data. Setting up shared filesystem servers in the cloud is doable, but it puts the onus of maintaining this environment on you.

We’re showing the filesystem approach as the easiest example to use when getting your feet wet with the Spring Cloud Configuration Server. In a later section, we’ll show you how to configure the Config Server to use GitHub and HashiCorp Vault to store your application configuration.

To continue, let’s create a licensing-service-dev.properties file that contains only the development data. The dev properties file should contain the following parameters:
```
example.property= I AM DEV
spring.datasource.url = jdbc:postgresql://localhost:5432/ostock_dev
spring.datasource.username = postgres
spring.datasource.password = postgres
```
Now that we have enough work done to start the Configuration Server, let’s go ahead and do that by using the `mvn spring-boot:run` command or the `docker-compose up` command.

> NOTE From this point on, you will find a README file in the folder repository for each chapter. This file contains a section called “How to Use.” This section describes how to run all the services together using the docker-compose command.

The server should now come up with the Spring Boot splash screen on the command line. If you point your browser to http://localhost:8071/licensing-service/ default, you’ll see a JSON payload returned with all of the properties contained within the licensing-service.properties file. Figure 5.6 shows the results of calling this endpoint.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F06_Huaylupo.png)

Figure 5.6 Spring Cloud Config exposes environment-specific properties as HTTP-based endpoints.

If you want to see the configuration information for the dev-based licensing service environment, select the GET http://localhost:8071/licensing-service/dev endpoint. Figure 5.7 shows the result of calling this endpoint.

> NOTE The port is the one we set previously on the bootstrap.yml file.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F07_Huaylupo.png)

Figure 5.7 Retrieving configuration information for the licensing service using the dev profile

If we look closely, we will see that when we selected the dev endpoint, the Spring Cloud Configuration Server returned both the default configuration properties and the dev licensing service configuration. The reason why Spring Cloud Config returns both sets of configuration information is that the Spring framework implements a hierarchical mechanism for resolving problems. When the Spring framework does this, it looks for the property defined in the default properties file first and then overrides the default with an environment-specific value if one is present. In concrete terms, if you define a property in the licensing-service.properties file and don’t define it in any of the other environment configurations (for example, licensing-service-dev.properties), the Spring framework will use the default value.

> NOTE This isn’t the behavior you’ll see by directly calling the Spring Cloud Config REST endpoint. The REST endpoint returns all configuration values for both the default and environment-specific value that was called.

Now that we finished configuring everything in our Spring Cloud Config service, let’s move on to integrating Spring Cloud Config with our licensing microservice.

## 5.3 Integrating Spring Cloud Config with a Spring Boot client

In the previous chapters, we built a simple skeleton of our licensing service that did nothing more than to return a hardcoded Java object representing a single licensing record. In this section, we will build the licensing service with a PostgreSQL database to hold the licensing data.

Why use PostgreSQL?

PostgreSQL, also known as Postgres, is considered an enterprise system and one of the most interesting and advanced options for open source relational database management systems (RDBMSs). PostgreSQL has many advantages compared to other relational databases, but the main ones are that it offers a single license that is entirely free and open for anyone to use. The second advantage is that, in terms of its ability and functionality, it allows us to work with more significant amounts of data without increasing the complexity of the queries. Here are some of the main features of Postgres:

Postgres uses a multiversion concurrency control that adds an image of the database status to each transaction, which produces consistent transactions with better performance advantages.
Postgres doesn't use reading locks when it executes a transaction.
Postgres has something called hot standby, which allows the client to search in the servers while the server is in recovery or standby mode. In other words, Postgres performs maintenance without completely locking down the database.

Some of the main characteristics of PostgreSQL are as follows:

- It is supported by languages such as C, C++, Java, PHP, Python, and more.
- It is capable of serving many clients while delivering the same information from its tables without blockages.
- It supports working with views so users can query the data differently from how it is stored.
- It is an object-relational database, allowing us to work with data as if it were objects, thus offering object-oriented mechanisms.
- It allows us to store and query JSON as a data type.

We are going to communicate with the database using Spring Data and map our data from the licensing table to a Plain Old Java Object (POJO) holding the data. We’ll use the Spring Cloud Configuration Server to read the database connection and a simple property. Figure 5.8 shows what’s going to happen between the licensing service and the Spring Cloud Config service.

When the licensing service is first started, we’ll pass it three pieces of information: the Spring profile, the application name, and the endpoint the licensing service should use to communicate with the Spring Cloud Config service. The Spring profile maps to the environment of the properties being retrieved for the Spring service.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F08_Huaylupo.png)

Figure 5.8 Retrieving configuration information using the dev profile

When the licensing service boots up, it will contact the Spring Cloud Config service via an endpoint built from the Spring profile passed into it. The Spring Cloud Config service will then use the configured backend repository (filesystem, Git, or Vault) to retrieve the configuration information specific to the Spring profile value passed in on the URI. The appropriate property values are then passed back to the licensing service. The Spring Boot framework will then inject these values into the appropriate parts of the application.

### 5.3.1 Setting up the licensing service Spring Cloud Config Service dependencies

Let’s change our focus from the Spring Cloud Configuration Server to the licensing service. The first thing you need to do is to add a couple more entries to the Maven file in your licensing service. The following listing gives the entries that we need to add.

Listing 5.5 Adding Maven dependencies to the licensing service
```xml
//Parts of pom.xml omitted for conciseness
<dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>
         spring-cloud-starter-config
     </artifactId> ❶
</dependency>
<dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>
         spring-boot-starter-data-jpa
     </artifactId> ❷
</dependency>
<dependency>
     <groupId>org.postgresql</groupId> ❸
     <artifactId>postgresql</artifactId>
</dependency>
```
❶ Tells Spring Boot to pull down the dependencies needed for Spring Cloud Config

❷ Tells Spring Boot to use the Java Persistence API (JPA) in your service

❸ Tells Spring Boot to pull down the Postgres drivers

The first dependency’s artifact ID, spring-cloud-starter-config, contains all the classes needed to interact with the Spring Cloud Config Server. The second and third dependencies, spring-boot-starter-data-jpa and postgresql, import the Spring Data Java Persistence API (JPA) and the Postgres JDBC drivers.

### 5.3.2 Configuring the licensing service to use Spring Cloud Config

After the Maven dependencies have been defined, we need to tell the licensing service where to contact the Spring Cloud Configuration Server. In a Spring Boot service that uses Spring Cloud Config, configuration information can be set in one of these files: bootstrap.yml, bootstrap.properties, application.yml, or application.properties.

As we mentioned previously, the bootstrap.yml file reads the application properties before any other configuration information. In general, the bootstrap.yml file contains the application name for the service, the application profile, and the URI to connect to a Configuration Server. Any other configuration information that you want to keep local to the service (and not stored in Spring Cloud Config) can be set in the services in the local application.yml file.

Usually, the information you store in the application.yml file is configuration data that you might want to have available to a service even if the Spring Cloud Config service is unavailable. Both the bootstrap.yml and application.yml files are stored in a project’s src/main/resources directory.

To have the licensing service communicate with your Spring Cloud Config service, these parameters can be defined in the bootstrap.yml file, in the docker-compose.yml file of the licensing service, or via JVM arguments when you start the service. The following listing shows how bootstrap.yml should look in your application if you choose this option.

Listing 5.6 Configuring the licensing service’s bootstrap.yml
```yml
spring:
  application:
      name: licensing-service ❶
  profiles:
      active: dev ❷
  cloud:
      config: 
          uri: http://localhost:8071   ❸
```
❶ Specifies the name of the licensing service so that the Spring Cloud Config client knows which service is being looked up

❷ Specifies the default profile the service should run. The profile maps to an environment.

❸ Specifies the location of the Spring Cloud Config Server

> **NOTE** The Spring Boot application supports two mechanisms to define a property: YAML (YAML Ain’t Markup Language) and a dot-separated property name. We will choose YAML as the means for configuring our application. The hierarchical format of YAML property values maps directly to these names: spring.application.name, spring.profiles.active, and spring .cloud.config.uri.

The spring.application.name is the name of your application (for example, licensing service) and must map directly to the name of the config directory within your Spring Cloud Configuration Server. The second property, spring.profiles.active, tells Spring Boot which profile the application should run as. Remember, a profile is a mechanism to differentiate the configuration data consumed by the Spring Boot application. For the licensing service’s profile, you’ll support the environment the service is going to map directly to in your cloud configuration environment. For instance, by passing in dev as your profile, the Config Server will use the dev properties. If you don’t set a profile, the licensing service will use the default profile.

The third and last property, spring.cloud.config.uri, is the location where the licensing service will look for the Config Server endpoint. In this example, the licensing service looks for the configuration server at http://localhost:8071.

Later in this chapter, you’ll see how to override the different properties defined in the bootstrap.yml and application.yml files on application startup. This allows you to tell the licensing microservice which environment it should be running in. Now, if you bring up the Spring Cloud Config service with the corresponding Postgres database running on your local machine, you can launch the licensing service using its dev profile. This is done by changing to the licensing service’s directory and issuing the following command:
```
mvn spring-boot:run
```

> **NOTE** You first need to launch the Configuration Server to retrieve the configuration data for the licensing service.

By running this command without any properties set, the licensing server automatically attempts to connect to the Spring Cloud Configuration Server using the endpoint (in this case, our endpoint is http://localhost:8071) and the active profile (dev) defined previously in the bootstrap.yml file of the licensing service.

If you want to override these default values and point to another environment, you can do so by compiling the licensing service project down to a JAR file, and then run the JAR with a D system property override. The following command-line call demonstrates how to launch the licensing service, passing all the commands via JVM arguments:

```sh
java  -Dspring.cloud.config.uri=http://localhost:8071 \
      -Dspring.profiles.active=dev \
      -jar target/licensing-service-0.0.1-SNAPSHOT.jar
```

This example demonstrates how to override Spring properties via the command line. With this command line, we override these two parameters:
```
spring.cloud.config.uri
spring.profiles.active
```

> **NOTE** If you try to run the licensing service that you downloaded from the GitHub repository (https://github.com/ihuaylupo/manning-smia/tree/master/chapter5) from your desktop using the previous Java command, it will fail because of two reasons. The first one is that you don’t have a desktop Postgres server running, and the second is because the source code in the GitHub repository uses encryption on the Config Server. We’ll cover using encryption later in this chapter.

In the examples, we hardcoded the values to pass in to the -D parameter values. In the cloud, most of the application configuration data you’ll need will be in your configuration server.

All the code examples for each chapter can be completely run from within Docker containers. With Docker, you simulate different environments through environment-specific Docker Compose files, which orchestrate the startup of all of your services. Environment-specific values needed by the containers are passed in as environment variables to the container. For example, to start your licensing service in a dev environment, the dev/docker-compose.yml file contains the entry displayed in the next listing for the licensing service.

Listing 5.7 Dev docker-compose.yml
```yml
licensingservice:
    image: ostock/licensing-service:0.0.1-SNAPSHOT
    ports:
      - "8080:8080"

    environment:                       ❶
     SPRING_PROFILES_ACTIVE: "dev"     ❷

     SPRING_CLOUD_CONFIG_URI: 
         http://configserver:8071      ❸
```

❶ Specifies the start of the environment variables for the licensing service container

❷ Passes the SPRING_PROFILES_ACTIVE environment variable to the Spring Boot service command line and tells Spring Boot what profile should be run

❸ The endpoint of the Config service

The environment entry in the YML file contains the values of two variables: SPRING_ PROFILES_ACTIVE, which is the Spring Boot profile the licensing service will run under, and SPRING_CLOUD_CONFIG_URI, which is passed to your licensing service and defines the Spring Cloud Configuration Server instance where the service will read its configuration data. Once you have Docker Compose file set up, you can run the services just by executing the following command where the Docker Compose file is located:

`docker-compose up`

Because you enhance all your services with introspection capabilities via Spring Boot Actuator, you can confirm the environment you are running in by selecting the following endpoint: http://localhost:8080/actuator/env. The /env endpoint provides a complete list of the configuration information about the service, including the properties and endpoints the service is booted with (figure 5.9).

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F09_Huaylupo.png)

Figure 5.9 You can check the licensing configuration service by calling the /actuator/env endpoint. In the code, you can see how both the licensing-service.properties and the licensing-service-dev.properties are displayed.

On exposing too much information

Every organization is going to have different rules about how to implement security around their services. Many organizations believe services shouldn’t broadcast any information about themselves and won’t allow things like a /env endpoint to be active on a service. Their belief (rightfully so) is that this provides too much information for a potential hacker.

Spring Boot provides a wealth of capabilities on how to configure what information is returned by the Spring Actuator endpoints. That’s outside the scope of this book, however. Craig Walls’ excellent book, Spring Boot in Action (Manning, 2016), covers this subject in detail. We highly recommend that you review your corporate security policies and Walls’ book to provide the right level of information you want to expose through Spring Actuator.

### 5.3.3 Wiring in a data source using Spring Cloud Config Server

At this point, the database configuration information is directly injected into your microservice. With the database configuration set, configuring your licensing microservice becomes an exercise in using standard Spring components to build and retrieve the data from the Postgres database. In order to continue with the example, we need to refactor the licensing into different classes, where each class has separate responsibilities. These classes are shown in table 5.2.

Table 5.2 Licensing service classes and locations

Class name

Location

License

com.optimagrowth.license.model

LicenseRepository

com.optimagrowth.license.repository

LicenseService

com.optimagrowth.license.service

The License class is the model class that will hold the data retrieved from your licensing database. The following listing shows the code for this class.

Listing 5.8 The JPA model code for a single license record
```java

@Getter @Setter @ToString
@Entity ❶
@Table(name="licenses") ❷
public class License {
   @Id ❸
   @Column(name = "license_id", nullable = false) ❹
   private String licenseId;
   private String description;
   @Column(name = "organization_id", nullable = false)
   private String organizationId;
   @Column(name = "product_name", nullable = false)
   private String productName;
   @Column(name = "license_type", nullable = false)
   private String licenseType;
   @Column(name="comment")
   private String comment;

   public License withComment(String comment){
       this.setComment(comment);
       return this;
   }
}
```
❶ Tells Spring that this is a JPA class

❷ Maps to the database table

❸ Marks this field as a primary key

❹ Maps the field to a specific database column

In the listing, the License class uses several JPA annotations that will help the Spring Data framework map the data from the licenses table in the Postgres database to a Java object. The @Entity annotation lets Spring know that this Java POJO is going to be mapping objects that will hold data. The @Table annotation tells Spring/JPA what database table to map. The @Id annotation identifies the primary key for the database. Finally, each one of the columns from the database that will be mapped to individual properties is marked with a @Column attribute.

> **TIP** If your attribute has the same name as the database column, you don’t need to add the @Column annotation.

The Spring Data and JPA framework provides your basic CRUD methods (Create, Replace, Update, Delete) for accessing a database. Table 5.3 shows some of these.

Table 5.3 CRUD methods for Spring Data and the JPA framework

Method

Description

count()

Returns the number of entities available

delete(entity)

Deletes a given entity

deleteAll()

Deletes all entities managed by the repository

deleteAll(entities)

Deletes the given entities

deleteById(id)

Deletes the entity with the given ID

existsById(id)

Returns whether an entity with the given ID exists

findAll()

Returns all instances of the type

findAllById(ids)

Returns all instances of a given type T with the given IDs

findById(ID id)

Retrieves an entity by its ID

save(entity)

Saves a given entity

saveAll(entities)

Saves all given entities

If you want to build methods beyond this, you can use a Spring Data Repository interface and basic naming conventions to build those methods. At startup, Spring will parse the name of the methods from the Repository interface, convert them to an SQL statement based on the names, and then generate a dynamic proxy class (under the cover) to do the work. The repository for the licensing service is shown in the following listing.

Listing 5.9 The LicenseRepository interface defining the query methods

```java
@Repository ❶

public interface LicenseRepository 
      extends CrudRepository<License,String>  { ❷
   public List<License> findByOrganizationId
                       (String organizationId); ❸
   public License findByOrganizationIdAndLicenseId
                       (String organizationId,
                        String licenseId);
}
```
❶ Tells Spring Boot that this is a JPA repository class. Annotation is optional when we extend from a CrudRepository.

❷ Extends the Spring CrudRepository

❸ Parses query methods into a SELECT...FROM query

The repository interface, LicenseRepository, is marked with @Repository, which tells Spring that it should treat this interface as a repository and generate a dynamic proxy for it. The dynamic proxy, in this case, provides a set of fully featured, ready-to-use objects.

Spring offers different types of repositories for data access. In this example, we use the Spring CrudRepository base class to extend our LicenseRepository class. The CrudRepository base class contains basic CRUD methods. In addition to the CRUD methods extended from CrudRepository, we added two custom query methods to the LicenseRepository interface for retrieving data from the licensing table. The Spring Data framework pulls apart the name of the methods to build a query to access the underlying data.

NOTE The Spring Data framework provides an abstraction layer over various database platforms and isn’t limited to relational databases. NoSQL databases, such as MongoDB and Cassandra, are also supported.

Unlike the previous incarnation of the licensing service in chapter 3, you’ve now separated the business and data access logic for the licensing service out of the LicenseController and into a standalone Service class called LicenseService. Listing 5.10 shows the license service. Between this LicenseService class and the versions seen in the previous chapters, there are a lot of changes because we added the database connection. Feel free to download the file from the following link:

https://github.com/ihuaylupo/manning-smia/tree/master/chapter5/licensing-service/src/main/java/com/optimagrowth/license/service/LicenseService.java

Listing 5.10 A LicenseService class to execute database commands
```java
@Service
public class LicenseService {

   @Autowired
   MessageSource messages;
   @Autowired
   private LicenseRepository licenseRepository;
   @Autowired
   ServiceConfig config;

   public License getLicense(String licenseId, String organizationId){
       License license = licenseRepository
         .findByOrganizationIdAndLicenseId(organizationId, licenseId);
       if (null == license) {
           throw new IllegalArgumentException(
               String.format(messages.getMessage(
                 "license.search.error.message", null, null),
                  licenseId, organizationId));  
       }
       return license.withComment(config.getProperty());
   }

   public License createLicense(License license){
       license.setLicenseId(UUID.randomUUID().toString());
       licenseRepository.save(license);
       return license.withComment(config.getProperty());
   }

   public License updateLicense(License license){
       licenseRepository.save(license);
       return license.withComment(config.getProperty());
   }

   public String deleteLicense(String licenseId){
       String responseMessage = null;
       License license = new License();
       license.setLicenseId(licenseId);
       licenseRepository.delete(license);
       responseMessage = String.format(messages.getMessage(
              "license.delete.message", null, null),licenseId);
       return responseMessage;
   }
}
```
The controller, service, and repository classes are wired together using the standard Spring @Autowired annotation. Next, let’s look at reading the configuration properties in the LicenseService class.

### 5.3.4 Directly reading properties using @ConfigurationProperties

In the LicenseService class, you might have noticed that we set the license .withComment() value in the getLicense() method with a value from the config .getProperty() class. The code referred to is shown here:
```java
return license.withComment(config.getProperty());
```
If you look at the com.optimagrowth.license.config.ServiceConfig.java class, you’ll see that the class is annotated with the following. (Listing 5.11 shows the @ConfigurationProperties annotation that we’ll use.)
```java
@ConfigurationProperties(prefix= "example")
```

Listing 5.11 Using ServiceConfig to centralize application properties

```java
@ConfigurationProperties(prefix = "example")
public class ServiceConfig{

  private String property;

  public String getProperty(){
    return property;
  }
}
```
While Spring Data “auto-magically” injects the configuration data for the database into a database connection object, all other custom properties can be injected using the @ConfigurationProperties annotation. With the previous example,

`@ConfigurationProperties(prefix= "example")`
pulls all the example properties from the Spring Cloud Configuration Server and injects these into the property attribute on the ServiceConfig class.

TIP While it’s possible to directly inject configuration values into properties in individual classes, we’ve found it useful to centralize all of the configuration information into a single configuration class and then inject the configuration class into where it’s needed.

### 5.3.5 Refreshing your properties using Spring Cloud Config Server

One of the first questions that comes up from development teams when they want to use the Spring Cloud Configuration Server is how can they dynamically refresh their applications when a property changes. Rest assured. The Config Server always serves the latest version of a property. Changes made to a property via its underlying repository will be up to date!

Spring Boot applications, however, only read their properties at startup, so property changes made in the Config Server won’t be automatically picked up by the Spring Boot application. But Spring Boot Actuator offers a @RefreshScope annotation that allows a development team to access a /refresh endpoint that will force the Spring Boot application to reread its application configuration. The following listing shows this annotation in action.

Listing 5.12 The @RefreshScope annotation
```java

@SpringBootApplication
@RefreshScope
public class LicenseServiceApplication {

   public static void main(String[] args) {
      SpringApplication.run(LicenseServiceApplication.class, args);
   }

}
```
Note a couple of things about the @RefreshScope annotation. This annotation only reloads the custom Spring properties you have in your application configuration. Items like your database configuration used by Spring Data won’t be reloaded by this annotation.

On refreshing microservices

When using Spring Cloud Config service with microservices, one thing you need to consider before you dynamically change properties is that you might have multiple instances of the same service running. You’ll need to refresh all of those services with their new application configurations. There are several ways you can approach this problem.

Spring Cloud Config service offers a push-based mechanism called Spring Cloud Bus that allows the Spring Cloud Configuration Server to publish to all the clients using the service where a change occurs. Spring Cloud Bus requires an extra piece of running middleware: RabbitMQ. This is an extremely useful means of detecting changes, but not all Spring Cloud Config backends support the push mechanism (the Consul server, for example). In the next chapter, you’ll use Spring Cloud service discovery and Eureka to register all instances of a service.

One technique that we’ve used to handle application configuration refresh events is to refresh the application properties in Spring Cloud Config. Then we write a simple script to query the service discovery engine to find all instances of a service and call the /refresh endpoint directly.

You can also restart all the servers or containers to pick up the new property. This is a trivial exercise, especially if you’re running your services in a container service such as Docker. Restarting Docker containers literally takes seconds and will force a reread of the application configuration.

Remember that cloud-based servers are ephemeral. Don’t be afraid to start new instances of a service with new configurations, then direct traffic to the new services and tear down the old ones.

### 5.3.6 Using Spring Cloud Configuration Server with Git

As mentioned earlier, using a filesystem as the backend repository for the Spring Cloud Configuration Server can be impractical for a cloud-based application. That’s because the development team has to set up and manage a shared filesystem that’s mounted on all instances of the Config Server, and the Config Server integrates with different backend repositories that can be used to host the application configuration properties.

One approach that we’ve used successfully is to employ a Spring Cloud Configuration Server with a Git source control repository. By using Git, you can get all the benefits of putting your configuration management properties under source control and provide an easy mechanism to integrate the deployment of your property configuration files in your build and deployment pipeline. To use Git, we need to add the configuration to the Spring Cloud Config service bootstrap.yml file. The next listing shows how.

Listing 5.13 Adding Git support to the Spring Cloud bootstrap.yml
```yml
spring:
    application:
      name: config-server
    profiles:
      active:
       - native, git ❶
    cloud:
      config:
         server:
            native:
               search-locations: classpath:/config         
                uri: https://github.com/ihuaylupo/
                       config.git ❸
               searchPaths: licensingservice ❹
server:
    port: 8071
```
❶ Maps all the profiles (this is a comma-separated list)

❷ Tells Spring Cloud Config to use Git as a backend repository

❸ Tells Spring Cloud Config the URL to the Git server and repo

❹ Tells Spring Cloud Config what path in Git to use to look for the config files

The four key pieces of configuration properties in the previous listing include the following:
```
spring.profiles.active
spring.cloud.config.server.git
spring.cloud.config.server.git.uri
spring.cloud.config.server.git.searchPaths
```
The spring.profiles.active property sets all the active profiles for the Spring Config service. This comma-separated list uses the same precedence rules as a Spring Boot application: active profiles have precedence over default profiles, and the last profile is the winner. The spring.cloud.config.server.git property tells the Spring Cloud Config Server to use a non-filesystem-based backend repository. In the previous listing, we connected to the cloud-based Git repository, GitHub.

> **NOTE** If you’re authorized to use GitHub, you need to set the username and password (personal token or SSH configuration) in the Git configuration on the bootstrap.yml of the configuration server.

The spring.cloud.config.server.git.uri property provides the URL of the repository you’re connecting to. And finally, the spring.cloud.config.server.git .searchPaths property tells the Config Server the relative path on the Git repository that will be searched when the Cloud Config Server boots up. Like the filesystem version of the configuration, the value in the spring.cloud.config.server.git.searchPaths attribute will be a comma-separated list for each service hosted by the configuration service.

> **NOTE** The default implementation of an environment repository in Spring Cloud Config is the Git backend.

### 5.3.7 Integrating Vault with the Spring Cloud Config service

As mentioned earlier, there is another backend repository that we will use: the HashiCorp Vault. Vault is a tool that allows us to securely access secrets. We can define secrets as any piece of information we want to restrict or control access to, such as passwords, certificates, API keys, and so forth.

To configure Vault in our Spring Config service, we must add a Vault profile. This profile enables integration with Vault and allows us to securely store the application properties of our microservices. To achieve this integration, we will use Docker to create a Vault container with the following command:
```sh
docker run -d -p 8200:8200 --name vault -e 'VAULT_DEV_ROOT_TOKEN_ID=myroot' -e 'VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200' vault
```
The docker run command contains these parameters:

- VAULT_DEV_ROOT_TOKEN_ID: This parameter sets the ID of the generated root token. The root token is the initial access token to start configuring Vault. This sets the ID of the initial generated root token to a given value.

- VAULT_DEV_LISTEN_ADDRESS: This parameter sets the IP address and port of the development server listener; the default value is 0.0.0.0:8200.

> NOTE In this example, we will run Vault locally. If you need additional info on how to run Vault in server mode, we highly recommend that you visit the official Vault Docker image information at https://hub.docker.com/_/vault.

Once the latest Vault image is pulled into Docker, we can start creating our secrets. To make this example more straightforward, we will use the Vault UI, but if you prefer to move on with the CLI commands, go for it.

### 5.3.8 Vault UI

Vault offers a unified interface that facilitates the process of creating secrets. To access this UI, we need to enter the following URL: http://0.0.0.0:8200/ui/vault/auth. This URL was defined by the VAULT_DEV_LISTEN_ADDRESS parameter set with the docker run command. Figure 5.10 shows the login page for the Vault UI.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F10_Huaylupo.png)

Figure 5.10 Login page in the Vault UI. Enter the following URL in the Token field to sign in: http://0.0.0.0:8200/ui/vault/auth.

The next step is to create a secret. To create the secret after you’ve logged in, click the Secrets tab in the Vault UI dashboard. For this example, we will create a secret called secret/licensingservice with a property called license.vault.property and set its value to Welcome to vault. Remember, access to this piece of information will be restricted and will be encrypted. To achieve this, first we need to create a new secret engine and then add the specific secret to that engine. Figure 5.11 shows how to create this with the Vault UI.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F11_Huaylupo.png)

Figure 5.11 Creating a new secret engine in the Vault UI

Now, that we have our new secret engine, let’s create our secret. Figure 5.12 shows you how.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F12_Huaylupo.png)

Figure 5.12 Creating a new secret in the Vault UI

Now that we have configured the Vault and a secret, let’s configure our Spring Cloud Config Server to communicate with Vault. To do that, we’ll add the Vault profile to our bootstrap.yml file for the Config Server. The next listing shows how your bootstrap file should look.

Listing 5.14 Adding Vault to the Spring Cloud bootstrap.yml
```yml
spring:
    application:
       name: config-server
    profiles:
       active:
       - vault
    cloud:
       config:
         server:
            vault: ❶
               port: 8200 ❷
               host: 127.0.0.1 ❸
               kvVersion: 2 ❹
server:
  port: 8071
```

❶ Tells Spring Cloud Config to use Vault as the backend repository

❷ Tells Spring Cloud Config the Vault port

❸ Tells Spring Cloud Config the Vault host

❹ Sets the kv secrets engine version.

NOTE An important point here is the kv secrets engine version. The default value for spring.cloud.config.server.kv-version is 1. But it is recommended to use version 2 when we use Vault 0.10.0 or later.

Now that we have everything set, let’s test our Config Server via an HTTP request. Here you can use a cURL command or some REST client such as Postman:

```sh
$ curl -X "GET" "http://localhost:8071/licensing-service/default" -H "X-Config-Token: myroot"
```
If everything was configured successfully, the command should return a response like the following:
```json
{
    "name": "licensing-service",
    "profiles": [
        "default"
    ],
    "label": null,
    "version": null,
    "state": null,
    "propertySources": [
        {
            "name": "vault:licensing-service",
            "source": {
                "license.vault.property": "Welcome to vault"
            }
        }
    ]
}
```

## 5.4 Protecting sensitive configuration information

By default, the Spring Cloud Configuration Server stores all properties in plain text within the application’s configuration files. This includes sensitive information such as database credentials and so forth. It’s extremely poor practice to keep sensitive credentials stored as plain text in your source code repository. Unfortunately, it happens far more often than you might think.

Spring Cloud Config gives you the ability to encrypt your sensitive properties easily. Spring Cloud Config supports using both symmetric (shared secret) and asymmetric encryption (public/private) keys. Asymmetric encryption is more secure than symmetric encryption because it uses modern and more complex algorithms. But sometimes it is more convenient to use the symmetric key because we only need to define a single property value in the bootstrap.yml file in the Config Server.

### 5.4.1 Setting up a symmetric encryption key

The symmetric encryption key is nothing more than a shared secret that’s used by the encrypter to encrypt a value and by the decrypter to decrypt a value. With the Spring Cloud Configuration Server, the symmetric encryption key is a string of characters you select that can be either set in the bootstrap.yml file of your Config Server or passed to the service via an OS environment variable, ENCRYPT_KEY. You can select the option that best suits your needs.

> **NOTE** Your symmetric key should be 12 or more characters long and, ideally, should be a random set of characters.

Let’s start by looking at an example of how to configure the symmetric key on the bootstrap file for the Spring Cloud Configuration Server. The following listing shows how to do this.

Listing 5.15 Setting a symmetric key in the boostrap.yml file
```yml
cloud:
    config:
      server:
        native:
          search-locations: classpath:/config
        git:
            uri: https://github.com/ihuaylupo/config.git
            searchPaths: licensingservice
    
server:
  port: 8071
  
encrypt:
  key: secretkey ❶
```

❶ Tells the Config Server to use this value as the symmetric key

For the purposes of this book, we will always set the ENCRYPT_KEY environment variable to export ENCRYPT_KEY=IMSYMMETRIC. Feel free to use the bootstrap.yml file property if you need to locally test without using Docker.

Managing encryption keys

In this book, we did two things that we wouldn’t normally recommend in a production deployment:

We set the encryption key to a phrase. We wanted to keep the key simple so that we could remember it and so that it would fit nicely when reading the text. In a real-world deployment, we use a separate encryption key for each environment we deploy to, and we use random characters as a key.

We’ve hardcoded the ENCRYPT_KEY environment variable directly in the Docker files used for the book. We did this so that you as the reader can download the files and start them without having to remember to set an environment variable.
In a real run-time environment, we would reference the ENCRYPT_KEY as an OS environment variable inside a Dockerfile. Be aware of this and don’t hardcode your encryption key inside your Dockerfiles. Remember, your Dockerfiles are supposed to be kept under source control.

### 5.4.2 Encrypting and decrypting a property

We are now ready to begin encrypting properties for use with Spring Cloud Config. We will encrypt the licensing service Postgres database password you use to access the O-stock data. This property, called spring.datasource.password, is currently set as plain text with postgres as its value.

When you fire up your Spring Cloud Config instance, Spring Cloud Config detects that the ENCRYPT_KEY environment variable or the bootstrap file property is set, and automatically adds two new endpoints, /encrypt and /decrypt to the Spring Cloud Config service. We will use the /encrypt endpoint to encrypt the postgres value. Figure 5.13 shows how to encrypt the postgres value using the /encrypt endpoint and Postman.


![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781617296956/files/OEBPS/Images/CH05_F13_Huaylupo.png)

그림 5.13 /encrypt 엔드포인트를 사용하여 Spring 데이터 소스 비밀번호 암호화

> 참고 /encrypt 또는 /decrypt 끝점을 호출할 때 이러한 끝점에 대해 POST를 수행해야 합니다.

값을 해독하려면 /decrypt 끝점을 사용하여 암호화된 문자열을 전달해야 합니다. 이제 다음 목록에 표시된 구문을 사용하여 라이선스 서비스에 대한 GitHub 또는 파일 시스템 기반 구성 파일에 암호화된 속성을 추가할 수 있습니다.

Listing 5.16 Adding an encrypted value to the licensing service properties file

```yml
spring.datasource.url = jdbc:postgresql://localhost:5432/ostock_dev
spring.datasource.username = postgres
spring.datasource.password = {cipher} 559ac661a1c93d52b9e093d3833a238a142de7772961d94751883b17c41746a6
```

Configuration 서버는 모든 암호화된 속성 앞에 {cipher} 값을 추가해야 합니다. 이 값은 암호화된 값을 처리하고 있음을 Config Server에 알려줍니다.

## 5.5 마무리 생각

애플리케이션 구성 관리는 평범한 주제처럼 보일 수 있지만 클라우드 기반 환경에서는 매우 중요합니다. 이후 장에서 더 자세히 논의하겠지만, 애플리케이션과 이러한 서버가 실행되는 서버는 변경할 수 없고 승격되는 전체 서버가 환경 간에 수동으로 구성되지 않는 것이 중요합니다. 이는 속성 파일과 함께 애플리케이션 아티팩트(예: JAR 또는 WAR 파일)를 "고정" 환경에 배포하는 기존 배포 모델에 직면해 있습니다.

그러나 클라우드 기반 모델에서는 애플리케이션 구성 데이터를 애플리케이션과 완전히 분리해야 합니다. 그런 다음 런타임에 적절한 구성 데이터 요구 사항이 주입되어 모든 환경에서 동일한 서버/애플리케이션 아티팩트가 일관되게 승격됩니다.

## 요약

- Spring Cloud Configuration Server(Config Server라고도 함)를 사용하면 환경별 값으로 애플리케이션 속성을 설정할 수 있습니다.

- Spring은 프로파일을 사용하여 서비스를 시작하여 Spring Cloud Config 서비스에서 검색할 환경 속성을 결정합니다.

- Spring Cloud Config 서비스는 파일 기반, Git 기반 또는 Vault 기반 애플리케이션 구성 저장소를 사용하여 애플리케이션 속성을 저장할 수 있습니다.

- Spring Cloud Config 서비스를 사용하면 대칭 및 비대칭 암호화를 사용하여 민감한 속성 파일을 암호화할 수 있습니다.