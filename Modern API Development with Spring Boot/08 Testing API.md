# Chapter 8: Testing APIs

이 장은 수동적으로 자동적으로 API를 테스트하는 방법을 다룹니다. 먼저 단위 테스트와 통합 테스트를 자동화 합니다. 이러한 자동화 형태를 다룬 다음에는 어떤 빌드의 통합 부분을 테스트할 수 있을 것입니다. 또한 다른 코드 커버리지 척도를 계산하는 코드 커버리지 도구를 설정하는 방법을 다룹니다.  

In this chapter, we will cover the following topics:

- 수동 테스트
- 통합 테스팅 자동화

This chapter will help you learn about test automation by showing you how to implement unit and integration test automation. You will also learn how to set up code coverage using the Java Code Coverage (JaCoCo) tool.

## Technical requirements

You will need the following for developing and executing the code in this chapter:

- Any Java IDE, such as NetBeans, IntelliJ, or Eclipse
- JDK 15+
- An internet connection to clone the code and download the dependencies and Gradle
- Postman/cURL (for API testing)

The code present in this chapter can be found on GitHub at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter08


## API 및 코드 수동 테스트

테스트는 소프트웨어 개발 및 유지 관리 주기의 지속적인 프로세스입니다. 가능한 모든 사용 사례와 각 변경 사항에 대한 해당 코드를 포괄하는 전체 테스트를 수행해야 합니다. 다음을 포함하여 API에 대해 다양한 유형의 테스트를 수행할 수 있습니다.

- **단위 테스트**: 코드의 가장 작은 단위(예: 클래스 메서드)를 테스트

- **통합 테스트**: 개발자가 수행하여 다양한 구성 요소 계층의 통합을 테스트

- **계약 테스트**: 계약 테스트는 API에 대한 변경 사항이 소비자 코드를 손상시키지 않는지 확인하기 위해 개발자가 수행합니다. 소비자 코드는 항상 생산자 계약(API)을 준수해야 합니다. 주로 마이크로서비스 기반 개발에 필요합니다.

- **엔드 투 엔드 테스트**: QA 팀에서 UI(소비자)에서 백엔드까지 엔드 투 엔드 시나리오를 테스트

- **사용자 승인 테스팅**: 비즈니스 관점에서 비즈니스 사용자가 수행

이 책의 앞부분에서 cURL 및 Postman 도구를 사용하여 수동 API 테스트를 수행했습니다. 모든 변경은 영향을 받는 API뿐만 아니라 API를 완전히 테스트해야 합니다. 여기에는 이유가 있습니다. 특정 API에만 영향을 미친다고 가정할 수 있지만 기본 가정이 잘못된 경우에는 어떻게 합니까? 건너뛴 다른 API에 영향을 주어 프로덕션 문제가 발생할 수 있습니다. 이로 인해 패닉이 발생할 수 있으며 릴리스를 롤오버하거나 패치를 수정하여 릴리스해야 할 수 있습니다.

이러한 상황에 처하는 것을 원하지 않으므로 제품에는 릴리스가 가능한 최고의 품질로 전달되도록 보장하는 별도의 QA 팀이 있습니다. QA 팀은 개발 팀에서 수행하는 테스트와 별도로 별도의 종단 간 및 승인 테스트(비즈니스/도메인 사용자와 함께)를 수행합니다.

고품질 결과물에 대한 이러한 추가 보증에는 더 많은 시간과 노력이 필요합니다. 따라서 오늘날에 비해 소프트웨어 개발 주기가 엄청났습니다. Time to Market은 오늘날의 경쟁이 치열한 소프트웨어 산업에서 중요한 요소입니다. 오늘날에는 더 빠른 릴리스 주기가 필요합니다. 또한 테스트라고도 하는 품질 검사는 릴리스 주기의 중요하고 중요한 부분입니다.

테스트 프로세스를 자동화하고 CI/CD 파이프라인의 필수 부분으로 만들어 테스트 시간을 줄일 수 있습니다. CI는 지속적 통합을 의미하며 코드 저장소에서 빌드 > 테스트 > 병합을 의미합니다. CD는 지속적 전달 및/또는 지속적 배포를 의미하며 둘 다 서로 바꿔서 사용할 수 있습니다. 

지속적 딜리버리는 코드가 자동으로 테스트되고 아티팩트 리포지토리 또는 컨테이너 레지스트리에 릴리스되는 프로세스입니다. 그런 다음 이를 선택하여 프로덕션 환경에 배포할 수 있습니다. 지속적 배포는 파이프라인에서 지속적 배포보다 한 단계 앞서 있으며 이전 단계가 성공하면 코드가 프로덕션 환경에 배포됩니다. 공개 액세스를 위해 코드를 공개하지 않는 제품은 Facebook 및 Twitter와 같이 이 접근 방식을 사용합니다. 반면에 Spring Framework, Java와 같이 공개적으로 제공되는 제품/서비스는 지속적 딜리버리 파이프라인을 사용합니다.

다음 섹션에서 지금까지 수행한 수동 테스트를 자동화할 것입니다.

## 테스트 자동화

수동으로 수행하는 모든 테스트는 자동화되어 빌드의 일부가 될 수 있습니다. 이는 모든 변경 또는 코드 커밋이 테스트 스위트 부분을 빌드의 일부로 실행한다는 것을 의미합니다. 빌드는 모든 테스트를 통과한 경우에만 성공합니다.

모든 API에 대해 자동화된 통합 테스트를 추가할 수 있습니다. 따라서 cURL 또는 Postman을 사용하여 각 API를 수동으로 실행하는 대신 빌드에서 API를 실행하고 빌드가 끝날 때 테스트 결과를 사용할 수 있습니다.

이 섹션에서는 REST 클라이언트 호출을 복제하고 컨트롤러에서 시작하여 데이터베이스(H2)를 포함한 퍼시스턴스 계층까지 모든 애플리케이션 계층을 테스트하는 통합 테스트를 작성할 것입니다.

그러나 그 전에 필요한 단위 테스트를 추가합니다. 이상적으로 이러한 단위 테스트는 개발 프로세스와 함께 추가되거나 TDD(테스트 주도 개발)의 경우 개발 프로세스 전에 추가되어야 합니다.

단위 테스트는 클래스의 메서드와 같은 작은 코드 단위의 예상 결과를 검증하는 테스트입니다. 좋은 코드(90% 이상)와 분기 적용 범위(80% 이상)로 적절한 테스트를 수행하면 대부분의 버그를 피할 수 있습니다. **코드 커버리지** 범위는 테스트가 실행될 때 유효성이 검사되는 줄 및 분기(예: if-else)의 수와 같은 메트릭을 나타냅니다.

일부 클래스 또는 메서드는 다른 클래스 또는 인프라 서비스에 종속됩니다. 예를 들어 컨트롤러 클래스에는 서비스 및 어셈블러 클래스에 대한 의존성이 있는 반면 저장소 클래스에는 Hibernate API에 대한 의존성이 있습니다. 의존성 동작을 복제하기 위해 Mock을 생성하고 이것이 예상대로 작동하거나 정의된 테스트에 따라 동작한다고 가정할 수 있습니다. 이를 통해 실제 코드 단위(예: 메서드)를 테스트하고 해당 동작을 확인할 수 있습니다.

다음 섹션에서는 통합 테스트를 작성하기 전에 단위 테스트를 추가하는 방법을 살펴보겠습니다.

## Unit testing

I advise you to go back to Chapter 6, Security (Authorization and Authentication), as a base for this chapter's code. You don't have to add any additional dependencies for unit tests. 
You already have the following dependency in build.gradle (https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/build.gradle):

```gradle
testImplementation('org.springframework.boot:spring-boot-starter-test')
```
`spring-boot-starter-test` adds all the required test dependencies, not only for the unit tests, but also for the integration tests. You are going to primarily use the following libraries for testing:

- **JUnit 5**: JUnit 5는 모듈 번들이다 – the JUnit Platform, JUnit Jupiter, and JUnit Vintage:

   a. **JUnit Platform** allows you to launch tests on JVM and its engine provides APIs for writing testing frameworks that run on the platform. The JUnit Platform consists of junit-platform-engine and junit-platform-commons.

   b. **JUnit Jupiter** provides the programming and extension models for writing tests and extensions. It has a separate library called junit-jupiter-engine that allows you to run Jupiter-based tests on JUnit Platform. It also provides the junit-jupiter, junit-jupitor-api, and junit-jupiter-params libraries.

   c. **JUnit Vintage** supports older versions of JUnit, such as versions 3 and 4. You are going to use latest version, which is 5, so you don't need it.

   You can find out more about JUnit at https://junit.org/.

- **AssertJ**: AssertJ is a test assertion library that simplifies assertion writing by providing fluent APIs. It is also extendable. You can write custom assertions for your domain objects. You can find more about it at https://assertj.github.io/doc/.

- **Hamcrest**: Hamcrest는 매처를 기반으로 하는 assertion을 제공하는 또 다른 assertion 라이브러리입니다. 또한 사용자 지정 매처를 작성할 수 있습니다. 이 장에서 이 두 가지의 예를 찾을 수 있습니다. AssertJ가 선호되지만 사용 사례와 선호도에 따라 둘 중 하나 또는 둘 다를 선택할 수 있습니다. 자세한 내용은 http://hamcrest.org/에서 확인할 수 있습니다.

- **Mockito**: Mockito는 객체를 모형(의존성 읽기) 및 스텁 메서드 호출을 허용하는 모형 프레임워크입니다. 자세한 내용은 https://site.mockito.org/에서 확인할 수 있습니다.

단위 테스트는 테스트 가능한 가장 작은 코드 단위를 테스트한다는 것을 이미 알고 있습니다. 그러나 컨트롤러 메서드에 대한 단위 테스트를 어떻게 작성할 수 있습니까? 컨트롤러는 웹 서버에서 실행되며 Spring 웹 애플리케이션 컨텍스트가 있습니다. WebApplicationContext를 사용하고 웹 서버 위에서 실행되는 테스트를 작성하는 경우 단위 테스트가 아닌 통합 테스트라고 부를 수 있습니다.

단위 테스트는 가벼워야 하고 빠르게 실행되어야 합니다. 따라서 Spring 테스트 라이브러리에서 제공하는 MockMvc를 사용하여 컨트롤러를 테스트해야 합니다. 단위 테스트를 위해 MockMvc에 대한 독립 실행형 설정을 사용할 수 있습니다. 또한 MockitoExtension을 사용하여 객체 모형 및 메서드 스터빙을 지원하는 JUnit 플랫폼(JUnit 5는 주자용 확장을 제공함)에서 단위 테스트를 실행할 수 있습니다. 또한 Mockito 라이브러리를 사용하여 필요한 의존성을 모형합니다. 이러한 테스트는 정말 빠르며 개발자가 더 빠르게 빌드하는 데 도움이 됩니다.


## AssertJ assertion으로 테스트

Let's write our first unit test for ShipmentController. The following code can be found in `src/test/java/com/packt/modern/api/controller/ShipmentControllerTest.java`:

```java
@ExtendWith(MockitoExtension.class)
public class ShipmentControllerTest {
  private static final String id = "a1b9b31d-e73c-4112-af7c-b68530f38222";
  private MockMvc mockMvc;

  @Mock
  private ShipmentService service;

  @Mock
  private ShipmentRepresentationModelAssembler modelAssembler;

  @Mock
  private MessageSource messageSource;

  @InjectMocks
  private ShipmentController controller;

  private ShipmentEntity entity;
  private Shipment model = new Shipment();
  private JacksonTester<List<Shipment>> shipmentTester;

  // continue…
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/src/test/java/com/packt/modern/api/controller/ShipmentControllerTest.java

이 테스트는 Mockito 기반 모형 및 stubbing을 지원하기 위해 확장을 등록하는 Jupiter 기반 주석(ExtendWith)을 사용합니다.

여기에서 Spring 테스트 라이브러리는 Spring MVC를 흉내낼 수 있는 MockMvc 클래스를 제공합니다. 결과적으로 연결된 API 끝점의 URI를 호출하여 컨트롤러 메서드를 실행할 수 있습니다. 서비스 및 어셈블러와 같은 ShipmentController 클래스의 의존성은 해당 의존성의 모형 인스턴스를 생성하기 위해 @Mock 주석으로 표시됩니다. `Mockito.mock(classOrInterface)`를 사용하여 모형 객체를 생성할 수도 있습니다.

`@InjectMocks`은 테스트 클래스에 필요한 모든 선언된 모형 객체를 찾아 자동으로 주입합니다. ShipmentController는 생성자를 사용하여 주입되는 ShipmentService 및 ShipmentRepresentationModelAssembler 인스턴스를 사용합니다. 

@InjectMocks은 ShipmentController 클래스(서비스 및 어셈블러)에서 의존성을 찾습니다. 그런 다음 테스트 클래스에서 서비스 및 어셈블러의 모형을 찾습니다. 일단 그것들을 찾으면, 이 모형 객체를 ShipmentController 클래스에 주입합니다. 필요한 경우 다음과 같이 @InjectsMocks를 사용하는 대신 생성자를 사용하여 테스트 클래스의 인스턴스를 만들 수도 있습니다.
```java
controller = new ShipmentController(service, assembler);
```

설정 메소드에서 사용되는 RestApiHandler에 대해 MessageSource의 모형이 생성됩니다. 다음 코드 블록에서 더 자세히 살펴보겠습니다.

선언의 마지막 부분인 Spring 테스트 라이브러리의 일부인 JacksonTester는 AssertJ 및 Jackson 라이브러리를 사용하여 생성된 사용자 지정 JSON 어설션 클래스입니다.

JUnit Jupiter API는 전제 조건을 설정하는 데 사용할 수 있는 @BeforeAll 및 @BeforeEach 메서드 주석을 제공합니다. 이름에서 알 수 있듯이 @BeforeAll은 테스트 클래스 당 한 번 실행되는 반면 @BeforeEach는 각 테스트 실행 전에 실행됩니다. @BeforeEach는 public non-static 메서드에 배치할 수 있지만 @BeforeAll은 public static 메서드에 주석을 추가하는 데 사용해야 합니다.

마찬가지로 JUnit은 각 테스트가 실행된 후와 각 테스트가 실행된 후 관련 메서드를 각각 실행하는 @AfterAll 및 @AfterEach를 제공합니다.

@BeforeEach 주석을 사용하여 다음과 같이 ShipmentControllerTest 클래스에 대한 전제 조건을 설정해 보겠습니다.

```java
  @BeforeEach
  public void setup() {
    ObjectMapper mapper = new AppConfig().objectMapper();
    JacksonTester.initFields(this, mapper);
    MappingJackson2HttpMessageConverter mappingConverter =
        new MappingJackson2HttpMessageConverter();

    mappingConverter.setObjectMapper(mapper);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new RestApiErrorHandler(msgSource))
        .setMessageConverters(mappingConverter)
        .build();

    final Instant now = Instant.now();
    entity = BeanUtils.copyProperties(entity, model); // entity initialization code
    
    // extra model property initialization
  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/src/test/java/com/packt/modern/api/controller/ShipmentControllerTest.java

1. AppConfig에서 받은 객체 매퍼 인스턴스로 JacksonTester 필드를 초기화합니다. 이렇게 하면 사용자 지정 메시지 변환기 인스턴스(MappingJackson2HttpMessageConverter)가 생성됩니다.

2. standalone 설정을 사용하여 mockMvc 인스턴스를 생성하고 setter 메소드를 사용하여 컨트롤러 어드바이스를 초기화할 수 있습니다. RestApiErrorHandler 인스턴스는 MessageResource 클래스의 모형 객체를 사용합니다. 빌드하기 전에 메시지 변환기를 mockMvc로 설정할 수도 있습니다.

3. ShipmentEntity 및 Shipment(모델)의 인스턴스를 초기화합니다.

ShipmentController 클래스의 getShipmentByOrderId() 메서드를 사용하는 `GET /api/v1/shipping/{id}` 호출에 대한 테스트를 작성합니다. 테스트는 @Test로 표시됩니다. @DisplayName을 사용하여 테스트 보고서에서 테스트 이름을 사용자 정의할 수도 있습니다.

```java
  @Test
  @DisplayName("returns shipments by given order ID")
  public void testGetShipmentByOrderId() throws Exception {
    // given
    given(service.getShipmentByOrderId(id))
        .willReturn(List.of(entity));

    given(assembler.toListModel(List.of(entity)))
        .willReturn(List.of(model));

    // when
    MockHttpServletResponse response = mockMvc.perform(
        get("/api/v1/shipping/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andReturn().getResponse();

    // then      
    assertThat(response.getStatus())
        .isEqualTo(HttpStatus.OK.value());

    assertThat(response.getContentAsString())
        .isEqualTo(shipmentTester.write(
            List.of(model)).getJson());

  }
```
여기서는 행동 주도 개발 테스트 스타일을 사용한다. BDD 테스트는 Gherkin Given > When > Then 언어로 작성한다.

- Given: 테스트 컨텍스트
- When: 테스트 액션
- Then: 검증 후의 테스트 결과

Let's read this test from a BDD perspective:

- **Given**: 서비스가 가용이고 주문 ID와 엔티티 목록을 모델 목록으로 변환한 어셈블러에 기반한 배송 목록을 리턴한다. 또한 HATEOAS 링크를 추가한다.

- **When**: `GET /api/shipping/a1b9b31d-e73c-4112-af7c-b68530f38222`으로 API를 호출.

- **Then**: 주어진 주문 ID와 연관된 수신 배송 목록을 검증한다.

MockitoBDD 클래스는 모형 객체 메서드를 스텁하기 위해 주어진() 유창한 API를 제공합니다. mockMvc.perform()이 호출되면 내부적으로 각각의 서비스 및 어셈블러 모형를 호출하고, 차례로 스텁된 메서드를 호출하고 스텁에 정의된 값을 반환합니다.

andDo(MockMvcResultHandlers.print()) 메서드는 페이로드 및 응답 본문을 포함하여 요청 및 응답 추적을 기록합니다. 테스트 클래스 내에서 모든 mockMvc 로그를 추적하려면 여기에 표시된 것처럼 mockMvc.perform() 호출에서 개별적으로 정의하는 대신 mockMvc를 초기화하는 동안 직접 구성할 수 있습니다.

```java
mockMvc = MockMvcBuilders
      .standaloneSetup(controller)
      .setControllerAdvice(new RestApiErrorHandler(msgSource))
      .setMessageConverters(mappingJackson2HttpMessageConverter)
      .alwaysDo(print())
      .build();
```
마지막으로 AssertJ fluent API를 사용하여 어설션(상태가 200 OK인지 여부 및 예상 객체와 일치하는 JSON 객체 반환 여부)을 수행합니다. 먼저 실제 객체를 가져와 isEqualTo() 메서드를 사용하여 예상 객체와 비교하는 Asserts.assertThat() 함수를 사용합니다.

지금까지 AssertJ 어설션을 사용했습니다. 마찬가지로 Spring 및 Hamcrest 어설션을 사용할 수도 있습니다.

## Testing using Spring and Hamcrest assertions

At this point, you know how to write JUnit 5 tests using MockitoExtension. You'll use the same approach to write a unit test, except with assertions. This time, you will write an assertion using Hamcrest assertions, as shown here:

```java
@Test
@DisplayName("returns address by given existing ID")
public void getAddressByOrderIdWhenExists() throws Exception {
  given(service.getAddressesById(id))
      .willReturn(Optional.of(entity));

  // when
  ResultActions result = mockMvc.perform(
      get("/api/v1/addresses/a1b9b31d-e73c-4112-af7c-b68530f38222")
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON));

  // then
  result.andExpect(status().isOk());
  verifyJson(result);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/src/test/java/com/packt/modern/api/controller/AddressControllerTest.java

You have captured the MockHttpResponse instance from the mockMvc.perform() call in the previous test example; that is, testGetShipmentByOrderId(). This time, you will directly use the returned value of the mockMvc.perform() call rather than calling an extra andReturn().getResponse() on it.

The ResultAction class provides the andExpect() assertion method, which takes ResultMatcher as an argument. The StatusResultMatchers.status().isOk() result matcher evaluates the HTTP status returned by the perform() call. The VerifyJson() method evaluates the JSON response object, as shown in the following code:
```java
private void verifyJson(final ResultActions result) throws Exception {
  final String BASE_PATH = "http://localhost";
  result
      .andExpect(jsonPath("id", is(entity.getId().toString())))
      .andExpect(jsonPath("number", is(entity.getNumber())))
      .andExpect(jsonPath("residency", is(entity.getResidency())))
      .andExpect(jsonPath("street", is(entity.getStreet())))
      .andExpect(jsonPath("city", is(entity.getCity())))
      .andExpect(jsonPath("state", is(entity.getState())))
      .andExpect(jsonPath("country", is(entity.getCountry())))
      .andExpect(jsonPath("pincode", is(entity.getPincode())))
      .andExpect(jsonPath("links[0].rel", is("self")))
      .andExpect(jsonPath("links[0].href",is(BASE_PATH + "/" + entity.getId())))

      .andExpect(jsonPath("links[1].rel", is("self")))
      .andExpect(jsonPath("links[1].href", is(BASE_PATH + URI + "/" + entity.getId())));
}
```
Here, the MockMvcResultMatchers.jsonPath() result matcher takes two arguments – a JSON path expression and a matcher. Therefore, first, you must pass the JSON field name and then the Hamcrest matcher known as Is.is(), which is a shortcut for Is.is(equalsTo(entity.getCity())).

Writing the unit test for a service is much easier compared to writing one for the controller because you don't have to deal with MockMvc.

You will learn how to test private methods in the next subsection.

### Testing private methods

Unit testing a private method is a challenge. The Spring test library provides the ReflectionTestUtils class, which provides a method called invokeMethod. This method allows to you invoke private methods. The invokeMethod method takes three argument – the target class, the method's name, and the method's arguments (using variable arguments). Let's use it to test the AddressServiceImpl.toEntity() private method, as shown in the following code block:
```java
@Test
@DisplayName("returns an AddressEntity when private method
 toEntity() is called with Address model")
public void convertModelToEntity() {
  // given
  AddressServiceImpl srvc = new AddressServiceImpl(repository);

  // when
  AddressEntity e = ReflectionTestUtils.invokeMethod(
     srvc, "toEntity",addAddressReq);

  // then
  then(e)
    .as("Check address entity is returned and not null").isNotNull();

  then(e.getNumber())
    .as("Check house/flat no is set").isEqualTo(entity.getNumber());

  then(e.getResidency())
    .as("Check residency is set").isEqualTo(entity.getResidency());

  then(e.getStreet())
    .as("Check street is set").isEqualTo(entity.getStreet());

  then(e.getCity())
    .as("Check city is set").isEqualTo(entity.getCity());

  then(e.getState())
    .as("Check state is set").isEqualTo(entity.getState());

  then(e.getCountry())
    .as("Check country is set").isEqualTo(entity.getCountry());

  then(e.getPincode())
    .as("Check pincode is set").isEqualTo(entity.getPincode());
}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/src/test/java/com/packt/modern/api/service/AddressServiceTest.java

Here, you can see that when you call ReflectionTestUtils.invokeMethod() with the given arguments, it returns the AddressEntity instance, which has been converted using the passed argument's AddAddressReq model instance.

Here, you are using a third kind of assertion using AssertJ's BDDAssertions class. The BDDAssertions class provides methods that resonate with the BDD style. BDDAssertions.then() takes the actual value that you want to verify. The as() method describes the assertion and should be added before you perform the assertion. Finally, you perform verification using AssertJ's assertion methods, such as isEqualTo().

You will learn how to test void methods in the next subsection.

### Testing void methods
A method that returns a value can easily be stubbed, but how can we stub a method that returns nothing? Mockito provides the doNothing() method for this. It has a wrapper willDoNothing() method in the BDDMockito class that internally uses doNothing().

This is very handy, especially when you want such methods to do nothing while you're spying, as shown here:
```java
List linkedList = new LinkedList();
List spyLinkedList = spy(linkedList);
doNothing().when(spyLinkedList).clear();
```

Here, linkedList is a real object and not a mock. However, if you want to stub a specific method, then you can use spy(). Here, when the clear() method is called on spyLinkedList, it will do nothing.

Let's use willDoNothing to stub the void method and see how it helps test void methods:

```java
@Test
@DisplayName("delete address by given existing id")
public void deleteAddressesByIdWhenExists() {
  given(repository.findById(UUID.fromString(nonExistId)))
      .willReturn(Optional.of(entity));

  willDoNothing().given(repository)
      .deleteById(UUID.fromString(nonExistId));

  // when
  service.deleteAddressesById(nonExistId);


  // then
  verify(repository, times(1))
      .findById(UUID.fromString(nonExistId));

  verify(repository, times(1))
      .deleteById(UUID.fromString(nonExistId));
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/src/test/java/com/packt/modern/api/service/AddressServiceTest.java

Here, AddresRepository.deleteById() is being stubbed using Mockito's willDoNothing() method. Now, you can use the verify() method of Mockito, which takes two arguments – the mock object and its verification mode. Here, the times() verification mode is being used, which determines how many times a method is invoked.

We'll learn how to unit test exceptional scenarios in the next subsection.

### Testing exceptions
Mockito provides thenThrow() for stubbing methods with exceptions. BDDMockito's willThrow() is a wrapper that uses it internally. You can pass the Throwable argument and test it like so:
```java
@Test
@DisplayName("delete address by given non-existing id,
                            should throw                          ResourceNotFoundException")

public void deleteAddressesByNonExistId() throws Exception {
  given(repository.findById(UUID.fromString(nonExistId)))
    .willReturn(Optional.empty())
    .willThrow(new ResourceNotFoundException(
        String.format("No Address found with id %s.",
            nonExistId)));

  // when
  try {
    service.deleteAddressesById(nonExistId);
  } catch (Exception ex) {

  // then
    assertThat(ex)
       .isInstanceOf(ResourceNotFoundException.class);

    assertThat(ex.getMessage())
       .contains("No Address found with id " + nonExistId);
  }

  // then
  verify(repository, times(1))
    .findById(UUID.fromString(nonExistId));

  verify(repository, times(0))
    .deleteById(UUID.fromString(nonExistId));
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/src/test/java/com/packt/modern/api/service/AddressServiceTest.java

Here, you basically catch the exception and perform assertions on it.

With that, you have explored the unit tests that you can perform for both controllers and services. You can make use of these examples and write unit tests for the rest of the classes.

### Executing unit tests
You can run the gradlew clean test command to execute our tests. This will generate the unit test reports at Chapter08/build/reports/tests/test/index.html.

A generated test report will look like this:

Figure 8.1 – Unit tests report
Figure 8.1 – Unit tests report

You can click on the links to drill down further. If the test fails, it also shows the cause of the error.

Let's move on to the next section to learn how to configure code coverage for unit tests.


## Code coverage

Code coverage provides important metrics, including line and branch coverage. You are going to use the JaCoCo tool to perform and report your code coverage.

First, you need to add the jacoco Gradle plugin to the build.gradle file, as shown in the following code:

```gradle
plugins {    
  id 'org.springframework.boot' version '2.4.3'
  id 'io.spring.dependency-management' version '1.0.10.RELEASE'
  id 'java'
  id 'org.hidetake.swagger.generator' version '2.18.2'
  id 'jacoco'
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/build.gradle

Next, configure the jacoco plugin by providing its version and reports directory:

jacoco {

  toolVersion = "0.8.6"

  reportsDir = file("$buildDir/jacoco")

}

Next, create a new task called jacocoTestReport that depends on the test task. You don't want to calculate coverage for auto-generated code, so add the exclude block. Exclusion can be added by configuring afterEvaluate, as shown in the following code block:

jacocoTestReport {

  dependsOn test // tests should be run before generating

                 // report

  afterEvaluate {        

    classDirectories.setFrom(

        files(classDirectories.files.collect {

      fileTree(

        dir: it,

        exclude: [

          'com/packt/modern/api/model/*',

          'com/packt/modern/api/*Api.*',

          'com/packt/modern/api/security/UNUSED/*',

      ])

    }))

  }

}

Next, you need to configure jacocoTestCoverageVerification, which defines the violation rules. We have added instructions to cover the ratio rule in the following code block. This will set the expected ratio to a minimum of 90%. If the ratio is below 0.9, then it will fail the build. You can find out more about such rules at https://docs.gradle.org/current/userguide/jacoco_plugin.html#sec:jacoco_report_violation_rules:

jacocoTestCoverageVerification {

  violationRules {

    rule {

      limit {

        minimum = 0.9

      }

    }

  }

}

Next, add finalizedBy(jacocoTestReport) to the test task, which ensures that the jacocoTestReport task will execute after performing the tests:

test {

  jvmArgs '--enable-preview'

  useJUnitPlatform()

  finalizedBy(jacocoTestReport)

}

Once you've run gradlew clean build, it will not only run the test but also generate the code coverage report, along with the test reports. The code coverage report will be available at Chapter08/build/jacoco/test/html and look as follows:

Figure 8.2 – Code coverage report
Figure 8.2 – Code coverage report

Here, you can see that our instruction coverage is only at 29%, while our branch coverage is only at 3%. You can add more tests and increase these percentages.

You will learn about integration testing in the next section.

Integration testing
Once you have the automated integration tests in place, you can ensure that any changes you make won't produce bugs, provided you cover all the testing scenarios. You don't have to add any additional plugins or libraries to support integration testing in this chapter. The Spring test library provides all the libraries required to write and perform integration testing.

Let’s add the configuration for integration testing in next subsection.

Configuring the Integration testing
First, you need a separate location for your integration tests. This can be configured in build.gradle, as shown in the following code block:

sourceSets {

    integrationTest {

        java {

            compileClasspath += main.output + test.output

            runtimeClasspath += main.output + test.output

            srcDir file('src/integration/java')

        }

        resources.srcDir file('src/integration/resources')

    }

}

Next, you can configure the integration test's implementation and runtime so that that it's extended from the test's implementation and runtime, as shown in the following code block:

configurations {

    integrationTestImplementation.extendsFrom

        testImplementation

    integrationTestRuntime.extendsFrom testRuntime

}

Finally, create a task called integrationTest that will not only use the JUnit Platform, but also use our classpath and test classpath from sourceSets.integrationTest.

Finally, configure the check task so that it depends on the integrationTest task and run integrationTest after the test task. You can remove the last line in the following code block if you want to run integrationTest separately:

task integrationTest(type: Test) {

    useJUnitPlatform()

    description = 'Runs the integration tests.'

    group = 'verification'

    testClassesDirs = sourceSets.integrationTest.

        output.classesDirs

    classpath = sourceSets.integrationTest.runtimeClasspath

}

check.dependsOn integrationTest

integrationTest.mustRunAfter test

Now, we can start writing the integration tests. Before writing integration tests, first let's write the supporting Java classes in next subsection. First, let's create the TestUtils class. This will contain a method that returns an instance of ObjectMapper. It will contain a method to check whether JWT has expired.

Writing supporting classes for Integration test
The ObjectMapper instance was retrieved from the AppConfig class and added an extra configuration so that we can accept a single value as an array. For example, a JSON string field value might be {[{…}, {…}]}. If you take a closer look at it, you will see that it is an array wrapped as a single value. When you convert this value into an object, ObjectMapper treats it as an array. The complete code for this class is as follows:

public class TestUtils {

  private static ObjectMapper objectMapper;

  public static ObjectMapper objectMapper() {

    if (Objects.isNull(objectMapper)) {

      objectMapper = new AppConfig().objectMapper();

      objectMapper.configure(

         DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,            true);

    }

    return objectMapper;

  }

  public static boolean isTokenExpired(String jwt)                             throws JsonProcessingException {

    var encodedPayload = jwt.split("\\.")[1];

    var payload = new String(Base64.getDecoder()                                  .decode(encodedPayload));

    JsonNode parent = new ObjectMapper().readTree(payload);

    String expiration = parent.path("exp").asText();

    Instant expTime = Instant.ofEpochMilli(                          Long.valueOf(expiration) * 1000);

    return Instant.now().compareTo(expTime) < 0;

  }

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/src/integration/java/com/packt/modern/api/TestUtils.java

Next, you need a client that lets you log in so that you can retrieve the JWT. RestTemplate is an HTTP client in Spring that provides support for making HTTP calls. The AuthClient class makes use of TestRestTemplate, which is a replica of RestTemplate from a testing perspective.

Let's write this AuthClient, as follows:

public class AuthClient {

  private TestRestTemplate restTemplate;

  private ObjectMapper objectMapper;

  public AuthClient(TestRestTemplate restTemplate,                                ObjectMapper objectMapper) {

    this.restTemplate = restTemplate;

    this.objectMapper = objectMapper;

  }

  public SignedInUser login(String username, String

        password) {

    SignInReq signInReq = new SignInReq()                  .username(username).password(password);

    return restTemplate

      .execute("/api/v1/auth/token", HttpMethod.POST,

         request -> {

            objectMapper.writeValue(request.getBody(),                                     signInReq);

            request.getHeaders()

                .add(HttpHeaders.CONTENT_TYPE,

                     MediaType.APPLICATION_JSON_VALUE);

            request.getHeaders().add(HttpHeaders.ACCEPT,

                     MediaType.APPLICATION_JSON_VALUE);

         },

         response -> objectMapper.readValue(

                        response.getBody(),

                        SignedInUser.class));

  }

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/src/integration/java/com/packt/modern/api/AuthClient.java

The Sprint test library provides MockMvc, WebTestClient, and TestRestTemplate for performing integration testing. You have already used MockMvc in unit testing. The same approach can be used for integration testing as well. However, instead of using mocks, you can use the actual objects by adding the @SpringBootTest annotation to the test class. @SpringBootTest, along with SpringExtension, provides all the necessary Spring context, such as the actual application.

WebTestClient is used to test the reactive applications. However, to test REST services, you must use TestRestTemplate, which is a replica of RestTemplate.

The integration test you are going to write is fully fleshed out test that doesn't contain any mocks. It will use flyway scripts, similar to the actual application, which we added to src/integration/resources/db/migration. The integration test will also have its own application.properties located in src/integration/resources.

Therefore, the integration test will be as good as long as you are hitting the REST endpoints from REST clients such as cURL or Postman. These flyway scripts create the tables and data required in the H2 memory database. This data will then be used by the RESTful web service. You can also use other databases, such as Postgres or MySQL, using their test containers.

Let's create a new integration test called AddressControllerIT in src/integration/java in an appropriate package and add the following code:

@ExtendWith(SpringExtension.class)

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)

@TestMethodOrder(OrderAnnotation.class)

public class AddressControllerIT {

  private static ObjectMapper objectMapper;

  private static AuthClient authClient;

  private static SignedInUser signedInUser;

  private static Address address;

  private static String idOfAddressToBeRemoved;

  @Autowired

  private AddressRepository repository;

  @Autowired

  private TestRestTemplate restTemplate;

  @BeforeAll

  public static void init() {

    objectMapper = TestUtils.objectMapper();

    address = new Address().id(

           "a731fda1-aaad-42ea-bdbc-

               a27eeebe2cc0").number("9I-999")

        .residency("Fraser Suites Le Claridge")

        .street("Champs-Elysees").city("Paris")

        .state("Île-de-France").country("France")

            .pincode("75008");

  }

  @BeforeEach

  public void setup() throws JsonProcessingException {

    if (Objects.isNull(signedInUser)

        || Strings.isNullOrEmpty(

            signedInUser.getAccessToken())

        || isTokenExpired(signedInUser.getAccessToken())) {

      authClient = new AuthClient(restTemplate,

                   objectMapper);

      signedInUser = authClient.login("scott", "tiger");

    }

  }

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/src/integration/java/com/packt/modern/api/controller/AddressControllerIT.java

Here, SpringExtension is now being used to run the unit test on the JUnit Platform. The SpringBootTest annotation provides all the dependencies and context for the test class. A random port is being used to run the test server. You are also using @TestMethodOrder, along with the @Order annotation, to run the test in a particular order. You are going to execute the test in a particular order so that the POST HTTP method on the addresses resource is only called before the DELETE HTTP method on the addresses resource. This is because you are passing the newly created address ID in the DELETE call. Normally, tests run in a random order. If the DELETE call is made before the POST call, then the build will fail, without testing the proper scenarios.

The static init() method is annotated with @BeforeAll and will be run before all the tests. You are setting up objectMapper and the address model in this method.

The method's setup would be run before each test is executed because it is marked with the @BeforeEach annotation. Here, you are making sure that the login call will only be made if signedInUser is null or the token has expired.

Let's add an integration test that will verify the GET /api/v1/addresses REST endpoint, as shown in the following code:

@Test

@DisplayName("returns all addresses")

@Order(6)

public void getAllAddress() throws IOException {

  // given

  MultiValueMap<String, String> headers = new                                LinkedMultiValueMap<>();

  headers.add(HttpHeaders.CONTENT_TYPE,       MediaType.APPLICATION_JSON_VALUE);

  headers.add(HttpHeaders.ACCEPT,       MediaType.APPLICATION_JSON_VALUE);

  headers.add("Authorization", "Bearer " +               signedInUser.getAccessToken());

  // when

  ResponseEntity<JsonNode> addressResponseEntity =

      restTemplate

      .exchange("/api/v1/addresses", HttpMethod.GET,          new HttpEntity<>(headers), JsonNode.class);

  // then  

  assertThat(addressResponseEntity.getStatusCode())

     .isEqualTo(HttpStatus.OK);

  JsonNode node = addressResponseEntity.getBody();

  List<Address> addressFromResponse = objectMapper

     .convertValue(node, new

         TypeReference<ArrayList<Address>>(){});

  assertThat(addressFromResponse).hasSizeGreaterThan(0);

  assertThat(addressFromResponse.get(0))

     .hasFieldOrProperty("links");

  assertThat(addressFromResponse.get(0))

     .isInstanceOf(Address.class);

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter08/src/integration/java/com/packt/modern/api/controller/AddressControllerIT.java

First, you must set the headers in the given section. Here, you are using the signedInUser instance to set the bearer token. Next, you must call the exchange method of TestRestTemplate, which takes four arguments – the URI, the HTTP method, HttpEntity (that contains the headers and payload if required), and the type of the returned value. You can also use the fifth argument if the template is being used to set urlVariables, which expands the template.

Then, you must use the assertions to perform the verification process. Here, you can see that it replicates the actual calls.

Once the tests are run either by running gradlew clean integrationTest or gradlew clean build, you can find the test report at Chapter08/build/reports/tests/integrationTest. The test report should look like this:

Figure 8.3 – Integration test report
Figure 8.3 – Integration test report

You can find all the test address resources in AddressControllerIT.java, which contains tests for errors, authentication and authorization, and the create, read, and delete operations.

Similarly, you can add integration tests for other REST resources.

## Summary

이 장에서는 수동 및 자동 테스트를 모두 살펴보았습니다. JUnit, Spring 테스트 라이브러리, AssertJ 및 Hamcrest를 사용하여 단위 및 통합 테스트를 작성하는 방법을 배웠습니다. 또한 Gherkin Given > When > Then 언어를 사용하여 테스트를 더 읽기 쉽게 만드는 방법을 배웠습니다. 그런 다음 단위 테스트와 통합 테스트를 분리하는 방법을 배웠습니다.

마지막으로 단위 및 통합 테스트를 자동화하여 다양한 테스트 자동화 기술에 대해 배웠습니다. 이렇게 하면 코드를 품질 분석이나 고객에게 제공하기 전에 테스트를 자동화하고 버그와 격차를 파악하는 데 도움이 됩니다.

다음 장에서는 애플리케이션을 컨테이너화하고 Kubernetes에 배포하는 방법을 배웁니다.

## Questions

1. What is the difference between unit and integration testing?
2. What is the advantage of having separate unit and integration tests?
3. What is the difference between mocking and spying on an object?

## Further reading

JUnit: https://junit.org/

AssertJ: https://assertj.github.io/doc/

Hamcrest: http://hamcrest.org/

Mockito: https://site.mockito.org/

API Testing with Postman: https://www.packtpub.com/product/api-testing-with-postman-video/9781789616569