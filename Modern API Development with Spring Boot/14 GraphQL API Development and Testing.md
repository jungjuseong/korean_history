
# Chapter 14: GraphQL API Development and Testing

You will learn about GraphQL-based API development and its testing in this chapter. You will implement GraphQL-based APIs for a sample application in this chapter. GraphQL server implementation will be developed based on a design-first approach, the way you defined the OpenAPI specification in Chapter 3, API Specifications and Implementation, and designed the schema in Chapter 11, gRPC-based API Development and Testing.

The following topics will be covered in this chapter:

- Workflow and tooling
- Implementing the GraphQL server
- Documenting APIs
- Test automation


After completing this chapter, you will have learned how to practically implement the GraphQL concepts learned in the previous chapter and the implementation of the GraphQL server using Java and Spring and its testing.

## Technical requirements

You need the following for developing and testing the GraphQL-based service code presented in this chapter:

- Any Java IDE such as NetBeans, IntelliJ, or Eclipse
- Java Development Kit (JDK) 15
- An internet connection to clone the code and download the dependencies and Gradle (version 7+)

So, let's begin!

Please visit the following link to check the code files: https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter14

## GraphQL을 위한 워크플로 및 도구

GraphQL의 데이터별 그래프 사고 방식으로 데이터는 객체의 그래프로 구성된 API를 사용하여 노출됩니다. 
이러한 개체는 관계를 사용하여 연결됩니다. GraphQL은 단일 API 엔드포인트만 노출합니다. 
클라이언트는 단일 데이터 그래프를 사용하는 이 끝점을 쿼리합니다. 게다가, 데이터 그래프는 GraphQL의 OneGraph 원칙에 따라 단일 소스 또는 여러 소스의 데이터를 해결할 수 있습니다. 이러한 소스는 데이터베이스, 레거시 시스템 또는 REST/gRPC/SOAP를 사용하여 데이터를 노출하는 서비스일 수 있습니다.

GraphQL 서버는 다음 두 가지 방법으로 구현할 수 있습니다.

- **독립형 서비스**: 독립형 서비스에는 단일 데이터 그래프가 포함됩니다. 단일 또는 여러 소스(GraphQL API 없음)에서 데이터를 가져오는 단일 앱 또는 마이크로서비스 아키텍처일 수 있습니다.

- **연합 서비스**: 포괄적인 데이터 가져오기를 위해 단일 데이터 그래프를 쿼리하는 것은 매우 쉽습니다. 그러나 엔터프라이즈 애플리케이션은 여러 서비스를 사용하여 만들어지므로 단일 시스템을 구축하지 않는 한 단일 데이터 그래프를 가질 수 없습니다. 단일 시스템을 구축하지 않으면 여러 서비스별 데이터 그래프가 생깁니다.

여기에서 연합 서비스를 사용합니다. 연합 서비스에는 게이트웨이로 노출한 단일 분산 그래프를 포함합니다. 
클라이언트는 시스템의 진입점인 게이트웨이를 호출합니다. 데이터 그래프는 여러 서비스에 분산되며 각 서비스는 자체 개발 및 릴리스 주기를 독립적으로 유지할 수 있습니다. 그렇지만 연합된 서비스는 여전히 OneGraph 원칙을 따를 것입니다. 
따라서 클라이언트는 그래프의 어떤 부분을 가져오기 위해 단일 끝점을 쿼리합니다.

전자 상거래 앱이 연합 서비스를 사용하여 개발되었다고 가정해 보겠습니다. 
GraphQL API를 사용하여 도메인별 데이터 그래프를 노출하는 제품, 주문, 배송, 재고, 고객 및 기타 서비스가 있습니다.

다음과 같이 GraphQL 통합 전자 상거래 서비스의 상위 수준 다이어그램을 그려 보겠습니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_14.1_B16561-CV111.jpg)

Figure 14.1 – 연합 GraphQL 서비스

클라이언트가 게이트웨이 엔드포인트를 호출하여 가장 적은 재고로 가장 많이 주문된 제품 목록을 쿼리한다고 가정해 보겠습니다. 
이 쿼리에는 Orders, Products 및 Inventory 필드가 있을 수 있습니다. 각 서비스는 데이터 그래프의 해당 부분만 분석할 책임이 있습니다. 주문은 주문 관련 데이터를 확인하고, 제품은 제품 관련 데이터를 확인하고, 재고는 재고 관련 데이터를 확인하는 식입니다. 그런 다음 게이트웨이는 그래프 데이터를 통합하고 클라이언트로 다시 보냅니다.

The `graphql-java` library (https://www.graphql-java.com/) provides the Java implementation of the GraphQL specification. Its source code is available at https://github.com/graphql-java/graphql-java.

There are many Spring Boot Starter projects for GraphQL, for example, at https://github.com/graphql-java-kickstart/graphql-spring-boot. However, we are going to use Netflix's Domain Graph Service (DGS) framework (https://netflix.github.io/dgs/). Netflix's DGS provides not only the GraphQL Spring Boot Starter but also the full set of tools and libraries that you need to develop production-ready GraphQL services. It is built on top of Spring Boot and uses the `graphql-java` library.

Netflix open-sourced the DGS framework after using it in production in February 2021. It is continuously being enhanced and supported by the community. Netflix uses the same open-sourced code based on their production, which gives the assurance of the code's quality and future maintenance.

It provides the following features:

- Provides a Spring Boot Starter and integration with Spring Security
- Gradle plugin for code generation from a GraphQL schema
- Support interfaces and union types, plus provides custom scalar types
- Supports GraphQL subscriptions using WebSocket and server-sent events
- Error handling
- Pluggable instrumentation
- GraphQL federated services by easy integration with GraphQL Federation
- File upload
- GraphQL Java client
- GraphQL test framework

Full WebFlux support could be available in the future. The release candidate build was available at the time of writing this chapter.

Let's write a GraphQL server using Netflix's DGS framework in the next section.


## Implementation of the GraphQL server

이 장에서는 단독 GraphQL 서버를 개발 합니다. 단독 서버를 개발하면서 얻은 지식으로 연합 서버 구현에 사용할 수 있습니다.

### Creating the gRPC server project

Either you can use the Chapter 14 code from a cloned Git repository (https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot), or you can start by creating a new Spring project from scratch using Spring Initializr (https://start.spring.io/) for the server and client with the following options (you will create the gRPC api library project separately):

- Gradle Project
- Java
- Spring Boot: 2.4.4
- Project Metadata:
```
Group: com.packt.modern.api
Artifact: chapter14
Name: chapter14
Description: Chapter 14 code of book Modern API Development with Spring and Spring Boot
Package name: com.packt.modern.api
```
- Packaging: JAR
- Java: 15

```gradle
// update following build.gradle file
sourceCompatibility = JavaVersion.VERSION_15
Dependencies: org.springframework.boot:spring-boot-starter-web
```

Then, you can click on the GENERATE button and download the project. The downloaded project will be used for creating the GraphQL server.

Next, let's add the GraphQL DGS dependencies to the newly created project.

### Adding the GraphQL DGS dependencies

Gradle 프로젝트가 준비되면 `build.gradle` 파일에 다음과 같이 GDS 의존성과 플러그인을 넣어 수정한다:

```gradle
plugins {
  id 'org.springframework.boot' version '2.4.4'
  id 'io.spring.dependency-management' version '1.0.11.RELEASE'
  id 'java'
  id 'com.netflix.dgs.codegen' version '4.6.4'
}
// other part removed from brevity
def dgsVersion = '3.12.1'
dependencies {
  implementation platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${dgsVersion}")
  implementation 'com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter'
  implementation 'com.netflix.graphql.dgs:graphql-dgs-extended-scalars'
  implementation 'org.springframework.boot:spring-boot-starter-web'
  testImplementation 'org.springframework.boot:spring-boot-starter-test'
  implementation 'com.github.javafaker:javafaker:1.0.2'
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/build.gradle

Here, the DGS Codegen plugin is added, which will generate the code from the GraphQL schema file. 
Next, the following three dependencies have been added:

- `graphql-dgs-platform-dependencies`: The DGS platform dependencies for the DGS bill of material (BOM)
- `graphql-dgs-spring-boot-starter`: The DGS Spring Boot Starter library for DGS Spring support
- `graphql-dgs-extended-scalars`: The DGS extended scalars library for custom scalar types

Please note that the `javafaker` library is being used here to generate the domain seed data.

Next, let's configure the DGS Codegen plugin in the `build.gradle` file as shown in the next code block:

```gradle
generateJava {
    packageName = "com.packt.modern.api.generated"
    generateClient = true
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/build.gradle

You have configured the following two properties of DGS Codegen using the `generateJava` task, which uses the `com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask` class:

- packageName: The Java package name of the generated Java classes
- generateClient: Whether you would like to generate the client or not

The DGS Codegen plugin picks GraphQL schema files from the `src/main/resources/schema` folder directory by default. However, you can modify it using the schemaPaths property, which accepts an array. You can add this property in the previous code of `generateTask` along with packageName and generateClient if you want to change the default schema location, as shown next:
```
schemaPaths = ["${projectDir}/src/main/resources/schema"]
```
You can also configure type mappings as you did for the org.hidetake.swagger.generator Gradle plugin while generating the Java code from OpenAPI specs in step 4 of the Convert OpenAPI spec to Spring code section in Chapter 3, API Specifications and Implementation. For adding a custom type mapping, you can add the typeMapping property to the plugin task as shown next:

```gradle
typeMapping = ["GraphQLType": "mypackage.JavaType"]
```

This property accepts an array; you can add one or more type mappings here. You can refer to the plugin documentation at https://netflix.github.io/dgs/generating-code-from-schema/ for more information.

Let's add the GraphQL schema next.

### Adding the GraphQL schema

Netflix's DGS supports both the code-first and design-first approaches. However, you are going to use the design-first approach in this chapter as we have done throughout this book. Therefore, first you'll design the schema using the GraphQL schema language and then use the generated code to implement the GraphQL APIs.

We are going to keep the domain objects minimal to reduce the complexity of business logic and keep the focus on the GraphQL server implementation. Therefore, you'll have just two domain objects – Product and Tag. The GraphQL schema allows the following operation using its endpoint as shown in the following schema file:

```graphql
  type Query {
    products(filter: ProductCriteria): [Product]!
    product(id: ID!): Product
  }
  type Mutation {
    addTag(productId: ID!, tags: [TagInput!]!): Product
    addQuantity(productId: ID!, quantity: Int!): Product
  }
  type Subscription {
    quantityChanged(productId: ID!): Product
  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/resources/schema/schema.graphqls

You need to add the `schema.graphqls` schema file at the `src/main/resources/schema` location. You can have multiple schema files there to create the schema module-wise.

Here, the following root types have been exposed:

- **Query**: The product and products queries for fetching a product by its ID, and a collection of products matched by the given criteria.

- **Mutation**: The addTag mutation would add a tag to the product matched by the given ID. Another mutation, addQuantity, would increase the product quantities. The addQuantity mutation would also be used as an event that would trigger the subscription publication.

- **Subscription**: The `quantityChanged` subscription would publish the product where the quantity has been updated. The event quantity change would be captured through the `addQuantity` mutation.

Let's add the object types and input types being used in these root types as shown in the next code block:

```graphql
  type Product {
    id: String
    name: String
    description: String
    imageUrl: String
    price: BigDecimal
    count: Int
    tags: [Tag]
  }
  type Tag {
    id: String
    name: String
  }
  input ProductCriteria {
    tags: [TagInput] = []
    name: String = ""
    page: Int = 1
    size: Int = 10
  }
  input TagInput {
    name: String
  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/resources/schema/schema.graphqls

These are straightforward object and input types. All fields of the ProductCriteria input type have been kept optional.

You have also used a BigDecimal custom scalar type. Therefore, we need to first declare it in the schema. You can do that by adding BigDecimal to the end of the schema file, as shown next:
```graphql
scalar BigDecimal
```
Next, you also need to map it to java.math.BigDecimal in the code generator plugin. Let's add it to the build.gradle file as shown next (check the highlighted line):

```gradle
generateJava {
  generateClient = true
  packageName = "com.packt.modern.api.generated"
  typeMapping = ["BigDecimal": "java.math.BigDecimal"]
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/build.gradle

After these changes, your project is ready to generate the GraphQL objects and client. You can run the following command from the project root directory to build the project:
```sh
gradlew clean build
```
This command would generate the Java classes in the `build/generated` directory.

### 커스텀 스칼라 타입 추가
금액을 나타내기 위해 BigDecimal을 사용할 것입니다. 이것은 사용자 정의 스칼라 유형이므로 DGS 프레임워크가 직렬화 및 역직렬화를 위해 선택할 수 있도록 이 사용자 정의 스칼라를 코드에 추가해야 합니다. (이것은 Gradle 코드 생성기 플러그인에서 매핑을 추가하는 것과 별도로 수행되어야 합니다.)

`BigDecimalScaler.java`라는 파일을 만들고 여기에 다음 코드를 추가합니다.
```java
@DgsScalar(name = "BigDecimal")
public class BigDecimalScalar extends GraphqlBigDecimalCoercing {
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/scalar/BigDecimalScalar.java

여기에서 클래스는 '@DgsScalar' 주석으로 표시되며, 이 주석은 이 클래스를 DGS 프레임워크에 사용자 지정 스칼라로 등록합니다. 이상적으로는 `graphql.schema.Coercing` 인터페이스(graphql-java 라이브러리의 일부)를 구현해야 합니다. 이 인터페이스를 사용하면 사용자 지정 스칼라 유형에 대한 직렬화 및 구문 분석을 구현할 수 있습니다.

그러나 BigDecimal 기본 구현(`GraphqlBigDecimalCoercing`)이 이미 graphql-java에 있으므로 간단히 확장합니다.

DGS 프레임워크는 'DateTime'과 같은 사용자 지정 스칼라도 제공합니다. 이러한 사용자 지정 스칼라는 DGS 프레임워크에 추가할 수도 있습니다. 'DateTime' 사용자 지정 스칼라 구현은 https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/에서 사용할 수 있습니다. packt/modern/api/scalar/DateTimeScalar.java, 다른 DGS 사용자 정의 스칼라 유형을 추가하기 위한 참조로 사용할 수 있습니다.

다음으로 GraphQL 루트 유형 구현을 시작하겠습니다. 먼저 GraphQL 쿼리를 구현합니다.

### GraphQL 쿼리 구현

두 쿼리 모두 단순하다. 제품 ID로 제품을 찾는 제품 쿼리이다. 다음에는 제품 기준으로 제품을 찾거나 아니면 디폴트 값으로 제품을 리턴한다. 

REST에서는 콘트롤러를 만들고 서비스에게 호출을 넘기면 서비스가 레포지토리에게 데이터베이스로부터 데이터를 조회하도록 요청한다. 
여러분도 같은 설계를 사용하려고 한다. 그러나 데이터베이스 대신 `ConcurrentHashMap`을 사용하여 단순화한다. 
이것은 테스트 자동화에서도 사용할 수 있다.

Let's create a repository class for that, as shown in the next code block:

```java
public interface Repository {
  Product getProduct(String id);
  List<Product> getProducts();
}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/repository/Repository.java

These are straightforward signatures for fetching the product and collection of products.
Let's implement this interface using ConcurrentHashMap as shown in the next code block:

```java
@Repository
public class InMemRepository implements Repository {
  private final Logger LOG = LoggerFactory.getLogger(getClass());
  private static final Map<String, Product>productEntities = new ConcurrentHashMap<>();
  private static final Map<String, Tag> tagEntities = new ConcurrentHashMap<>();
  // rest of the code is truncated
```
Here, you have created two instances of `ConcurrentHashMap` to store the products and tags. 
Let's add the seed data to these maps using the constructor:

```java
@Repository
public InMemRepository() {
  Faker faker = new Faker();

  IntStream.range(0, faker.number().numberBetween(20, 50))
   .forEach(number -> {
     String tag = faker.book().genre();
     tagEntities.putIfAbsent(tag,
        Tag.newBuilder().id(UUID.randomUUID().toString())
        .name(tag).build());
  });
  IntStream.range(0, faker.number().numberBetween(4, 20))
   .forEach(number -> {
     String id = String.format("a1s2d3f4-%d", number);
     String title = faker.book().title();
     List<Tag> tags = tagEntities.entrySet().stream()
       .filter(t -> t.getKey().startsWith(faker.book().genre().substring(0, 1)))
       .map(Entry::getValue).collect(toList());
     if (tags.isEmpty()) {               
       tags.add(tagEntities.entrySet().stream()
         .findAny().get().getValue());
     }
     Product product = Product.newBuilder().id(id).name(title)          
       .description(faker.lorem().sentence())
       .count(faker.number().numberBetween(10, 100))
       .price(BigDecimal.valueOf(faker.number().randomDigitNotZero()))
       .imageUrl(String.format("/images/%s.jpeg",title.replace(" ", "")))
       .tags(tags).build();
      productEntities.put(id, product);
  });
}
```
This code first generates the tags and then products and stores them in respective maps. This has been done for development purposes only. You should use the database in production applications.

Now, the getProduct and getProducts methods are straightforward, as shown in the next code block:

```java
@Override
public Product getProduct(String id) {
  if (Strings.isBlank(id)) {
    throw new RuntimeException("Invalid Product ID.");
  }
  Product product = productEntities.get(id);
  if (Objects.isNull(product)) {
    throw new RuntimeException("Product not found.");
  }
  return product;
}
@Override
public List<Product> getProducts() {
  return productEntities.entrySet(
      .stream()
      .map(e -> e.getValue())
      .collect(toList());
}
```
The `getProduct` method performs the basic validations and returns the product. 
The `getProducts` method simply returns the collection of products converted from the map.

Let's add the service interface as shown in the next block:
```java
public interface ProductService {
  Product getProduct(String id);
  List<Product> getProducts(ProductCriteria criteria);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/services/ProductService.java

These interfaces simply call the repository to fetch the data. Let's add the implementation as shown in the next code block:

```java
@Service
public class ProductServiceImpl implements ProductService {
  private final Repository repository;
  public ProductServiceImpl(Repository repository) {
    this.repository = repository;
  }

  @Override
  public Product getProduct(String id) {
    return repository.getProduct(id);
  }
  // continue…
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/services/ProductServiceImpl.java

Here, the repository is injected using constructor injection.

Let's add the getProducts() method also, which also performs filtering based on given filtering criteria, as shown in the next code block:

```java
@Override
public List<Product> getProducts(ProductCriteria criteria) {
  List<Predicate<Product>> predicates = new ArrayList<>(2);
  if (!Objects.isNull(criteria)) {
    if (Strings.isNotBlank(criteria.getName())) {
      Predicate<Product> namePredicate = p -> p.getName().contains(criteria.getName());
      predicates.add(namePredicate);
    }
    if (!Objects.isNull(criteria.getTags()) &&
        !criteria.getTags().isEmpty()) {
      List<String> tags = criteria.getTags().stream().map(
          ti -> ti.getName()).collect(toList());
      Predicate<Product> tagsPredicate = p -> p.getTags().stream()
        .filter(t -> tags.contains(t.getName())).count() > 0;
      predicates.add(tagsPredicate);
    }
  }
  if (predicates.isEmpty()) {
    return repository.getProducts();
  }
  return repository.getProducts().stream()
        .filter(p -> predicates.stream().allMatch(pre -> pre.test(p)))
        .collect(toList());
}
```

이 방법은 먼저 기준이 주어졌는지 여부를 확인합니다. 기준이 제공되지 않으면 저장소를 호출하고 모든 제품을 리턴합니다.
기준이 주어지면 predicates 목록을 생성합니다. 그런 다음 이것으로 일치하는 제품을 필터링하고 호출 함수로 다시 반환합니다.

이제 GraphQL 쿼리 구현에서 가장 중요한 부분인 데이터 가져오기 도구를 작성합니다. 
먼저 제품 쿼리에 대한 데이터 가져오기를 작성해 보겠습니다.

### 제품에 대한 데이터 가져오기 작성
데이터 가져오기는 GraphQL 요청을 처리하는 중요한 DSG 구성 요소이며 DSG는 내부적으로 각 필드를 해결합니다. 
`@DgsComponent` 주석으로 표시합니다. 이것은 DGS 프레임워크가 요청을 제공하기 위해 스캔하고 사용하는 Spring 구성 요소의 유형입니다.

DGS 데이터 가져오기를 위한 `datafetchers.ProductDatafetcher`라는 클래스를 만들어 보겠습니다. 
제품 쿼리를 제공하기 위한 데이터 가져오기 메서드가 있습니다. 
다음 코드를 추가할 수 있습니다.

```java
@DgsComponent
public class ProductDatafetcher {
  private final ProductService productService;
  public ProductDatafetcher(ProductService productService) {
    this.productService = productService;
  }
  @DgsData(
     parentType = DgsConstants.QUERY_TYPE,
     field = QUERY.Product
  )
  public Product getProduct(@InputArgument("id") String id) {
    if (Strings.isBlank(id)) {
      new RuntimeException("Invalid Product ID.");
    }
    return productService.getProduct(id);
  }
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductDatafetcher.java

여기서는 생성자를 사용하여 제품 서비스 Bean 주입을 작성합니다. 이 서비스 빈은 주어진 제품 ID를 기반으로 제품을 찾는 데 도움이 됩니다.

두 개의 다른 중요한 DGS 프레임워크 주석이 getProduct 메소드에서 사용되었습니다. 그것이하는 일을 이해합시다.

- @DgsData: 메소드를 데이터 fetcher로 표시하는 데이터 기져오기 주석입니다. parentType 속성은 유형을 나타내고 field 속성은 유형의 (parentType) 필드를 나타냅니다. 따라서 메소드가 주어진 유형의 필드를 가져올 것이라고 말할 수 있습니다.

  "Query"를 parentType으로 설정했습니다. 필드 속성은 "제품" 쿼리로 설정됩니다. 따라서 이 메서드는 GraphQL 쿼리 제품 호출의 진입점으로 작동합니다. @DsgData 주석 속성은 DgsConstants 상수 클래스를 사용하여 설정됩니다.

  DgsConstants는 스키마의 모든 상수 부분을 포함하는 DGS Gradle 플러그인에 의해 생성됩니다.

- @InputArgument: 이 주석을 사용하면 GraphQL 요청에 의해 전달된 인수를 캡처할 수 있습니다. 여기에서 id 매개변수의 값이 캡처되어 id 문자열 변수에 할당됩니다. 테스트 자동화 섹션에서 이 데이터 가져오기 방법과 관련된 테스트 케이스를 찾을 수 있습니다.

마찬가지로 Products 쿼리에 대한 데이터 가져오기 메서드를 작성할 수 있습니다. 다음 하위 섹션에서 코딩해 보겠습니다.

### Writing the data fetcher for a collection of products
Let's create a new file called ProductsDatafetcher.java in the datafetchers package for representing a DGS data fetcher component. It will have a data fetcher method for serving the products query. You can add the following code to it:
```java
@DgsComponent
public class ProductsDatafetcher {
  private final Logger LOG = LoggerFactory.getLogger(getClass());
  private ProductService service;
  public ProductsDatafetcher(ProductService service) {
    this.service = service;
  }
  @DgsData(
      parentType = DgsConstants.QUERY_TYPE,
      field = QUERY.Products
  )
  public List<Product> getProducts(
              @InputArgument("filter") ProductCriteria criteria) {
    return service.getProducts(criteria);
  }
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductsDatafetcher.java

이 getProducts() 메서드는 마지막에서 두 번째 코드 블록의 getProduct()에 대해 반환된 데이터 가져오기 메서드와 다르게 보이지 않습니다. 여기에서 @DsgData의 parentType 및 필드 속성은 이 메서드가 "products" 쿼리(마지막에 추가)에 대한 제품 컬렉션을 가져오는 데 사용됨을 나타냅니다.

GraphQL 쿼리 구현이 완료되었습니다. 이제 변경 사항을 테스트할 수 있습니다. 
다음 명령을 사용하여 애플리케이션을 빌드해 보겠습니다.

```sh
$ gradlew clean build
```
애플리케이션을 실행합니다:
```sh
$ java –jar build/libs/chapter14-0.0.1-SNAPSHOT.jar
```
응용 프로그램은 기본 포트 8080에서 실행되어야 합니다.

이제 브라우저 창을 열고 다음 URL을 사용하여 GraphiQL을 열 수 있습니다. 
`http://localhost:8080/graphiql`(DGS 프레임워크의 일부).

다음 쿼리를 사용하여 제품 컬렉션을 가져올 수 있습니다.
```graphql
{
  products(filter: {name: "His Dark Materials", tags: [{name:"Fantasy"}, {name: "Legend"}]}
  ) {
    id
    name
    price
    description
    tags {
      id
      name
    }
  }
}
```
이것은 잘 작동합니다. 그러나 태그를 별도로 가져와야 하는 경우에는 어떻게 해야 합니까? 별도의 데이터베이스나 서비스 또는 두 개의 개별 테이블에서 가져올 수 있는 개체에 관계(예: 청구 정보가 있는 주문)가 있을 수 있습니다. 이 경우 데이터 가져오기 방법을 사용하여 필드 해석기를 추가할 수 있습니다.

다음 하위 섹션에서 데이터 가져오기 메서드를 사용하여 필드 해석기를 추가해 보겠습니다.

### 데이터 가져오기 메서드를 사용하여 필드 리졸버 작성

지금까지는 태그를 가져오기 위한 별도의 데이터 가져오기 도구가 없습니다. 우리는 두 데이터를 함께 저장하는 동시 맵을 사용하고 있기 때문에 제품을 가져오고 태그도 가져옵니다. 따라서 먼저 주어진 제품에 대한 태그를 가져오기 위한 새로운 데이터 가져오기 메서드를 작성해야 합니다.

다음 코드 블록과 같이 태그를 가져오기 위해 ProductsDatafetcher 클래스에 tags() 메서드를 추가해 보겠습니다.
```java
@DgsData(
     parentType = PRODUCT.TYPE_NAME,
     field = PRODUCT.Tags
)
public List<Tags>  tags(String productId) {
   return tagService.fetch(productId);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductsDatafetcher.java

여기에서 tags() 메서드는 @DsgData 속성에 대해 다른 값 집합을 갖습니다. parentType 속성은 이전 데이터 가져오기 메서드(Query로 설정)와 같이 루트 유형으로 설정되지 않습니다. 대신 "제품"이라는 개체 유형으로 설정됩니다. 필드 속성은 "태그"로 설정됩니다.

이 메서드는 제품 개체의 태그 필드에 대한 필드 확인자이므로 각 개별 제품에 대한 태그를 가져오기 위해 호출됩니다. 따라서 20개의 제품이 있는 경우 이 메서드를 20번 호출하여 20개의 제품 각각에 대한 태그를 가져옵니다. 이것은 우리가 지난 장(13장, GraphQL 기초)에서 배웠던 N+1 문제입니다.

N+1 문제에서 관계에 대한 데이터를 가져오기 위해 추가 데이터베이스 호출이 만들어집니다. 따라서 제품 컬렉션이 주어지면 각 제품에 대한 태그를 개별적으로 가져오기 위해 데이터베이스에 도달할 수 있습니다.

N+1 문제를 피하려면 데이터 로더를 사용해야 한다는 것을 알고 있습니다. 데이터 로더는 단일 쿼리에서 해당 태그를 가져오기 전에 제품의 모든 ID를 캐시합니다.

다음으로 이 경우 N+1 문제를 해결하기 위해 데이터 로더를 구현하는 방법을 알아보겠습니다.

N+1 문제 해결을 위한 데이터 로더 작성
DataFetchingEnvironment 클래스를 데이터 가져오기 메서드의 인수로 사용할 것입니다. 실행 컨텍스트를 제공하기 위해 data fetcher 메소드의 graphql-java 라이브러리에 의해 주입됩니다. 이 실행 컨텍스트에는 개체 및 해당 필드와 같은 확인자에 대한 정보가 포함됩니다. 데이터 로더 클래스를 로드하는 것과 같은 특별한 사용 사례에서도 사용할 수 있습니다.

다음 코드 블록과 같이 N+1 문제 없이 태그를 가져오도록 이전 코드 블록에서 언급한 ProductsDatafetcher 클래스의 tags() 메서드를 수정해 보겠습니다.
```java
@DgsData(
     parentType = PRODUCT.TYPE_NAME,
     field = PRODUCT.Tags
)
public CompletableFuture<List<Tags>>  
            tags(DgsDataFetchingEnvironment env) {
   DataLoader<String, List<Tags>> tagsDataLoader =
          env.getDataLoader(
              TagsDataloaderWithContext.class);
   Product product = env.getSource();
   return tagsDataLoader.load(product.getId());
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductsDatafetcher.java

Here, the modified tags() data fetcher method performs the fetch method using a data loader and returns the collection of tags wrapped inside CompletableFuture. And it would be called only once even if the number of products are more than 1.

> **WHAT IS COMPLETABLEFUTURE?**

`CompletableFuture`는 명시적으로 완료된 것으로 표시된 비동기 계산의 결과를 나타내는 Java 동시성 클래스입니다. 현재 작업의 결과를 사용할 수 있을 때 다음 작업이 트리거되는 여러 종속 작업을 비동기적으로 연결할 수 있습니다.

`DsgDataFetchingEnvironment`를 인수로 사용하고 있습니다. `DataFetchingEnvironment` 인터페이스를 구현하고 데이터 로더 클래스를 해당 클래스와 이름으로 로드하는 방법을 제공합니다. 여기에서 데이터 로더 클래스를 사용하여 데이터 로더를 로드합니다.

`DsgDataFetchingEnvironment`의 `getSource()` 메서드는 @DsgData의 parentType 속성에서 값을 반환합니다. 따라서 `getSource()`는 Product를 반환합니다.

이 수정된 데이터 가져오기 메서드는 주어진 제품 목록에 대한 태그를 가져옵니다. 제품 목록? 단일 제품 ID를 전달하고 있습니다. 맞습니다. 데이터 로더 클래스는 배치를 사용하여 작업을 수행하는 MappedBatchLoader를 구현합니다.

데이터 로더 클래스는 데이터 로더를 사용하여 지정된 제품의 태그(ID별)를 일괄적으로 가져옵니다. 마법은 CompletableFuture를 반환하는 데 있습니다. 따라서 단일 제품 ID를 인수로 전달하더라도 데이터 로더는 일괄 처리합니다. 더 자세히 알아보기 위해 이 데이터 로더 클래스(TagsDataloaderWithContext)를 구현해 보겠습니다.

컨텍스트가 있거나 컨텍스트가 없는 두 가지 방법으로 데이터 로더 클래스를 생성할 수 있습니다. 컨텍스트가 없는 데이터 로더는 다음 메서드 서명이 있는 MappedBatchLoader를 구현합니다.

```java
CompletionStage<Map<K, V>> load(Set<K> keys);
```

On the other hand, data loaders with context implement the MappedBatchLoaderWithContext interface, which has the following method signature:
```java
CompletionStage<Map<K, V>> load(Set<K> keys,
    BatchLoaderEnvironment environment);
```
Both are the same as far as data loading is concerned. However, the data loader with context provides you with extra information (through BatchLoaderEnvironment) that can be used for various additional features, such as authentication, authorization, or passing the database details.

Create a new Java file called TagsDataloaderWithContext.java in the dataloaders package with the following code:
```java
@DgsDataLoader(name = "tagsWithContext")
public class TagsDataloaderWithContext implements MappedBatchLoaderWithContext<String, List<Tag>> {
  private final TagService tagService;
  public TagsDataloaderWithContext(TagService tagService) {
    this.tagService = tagService;
  }
  @Override
  public CompletionStage<Map<String, List<Tag>>>load(Set<String> keys, BatchLoaderEnvironment environment) {
    return CompletableFuture.supplyAsync(() -> tagService.getTags(new ArrayList<>(keys)));
  }
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/dataloaders/TagsDataloaderWithContext.java

Here, it implements the load() method from the MappedBatchLoaderWithContext interface. The BatchLoaderEnvironment argument exists, which provides the context, but we are not using it as we don't have to pass any additional information to the repository or underlying data access layer. You can find the data loader without context at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/dataloaders/TagDataloader.java. It is similar to what we have written for the data loader with context as we are not using the context.

You could see that it makes use of the tag's service to fetch the tags. Then, it simply returns the completion stage by supplying tags received from the tag service. This operation is performed in batch by the data loader.

You can create a new tag service and its implementation as follows:
```java
public interface TagService {
  Map<String, List<Tag>> getTags(List<String> productIds);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/services/TagService.java

This is the signature of the getTags method, which returns the map of product IDs with corresponding tags.

Let's implement this interface as shown in the next code block:
```java
@Service
public class TagServiceImpl implements TagService {
  private final Repository repository;
  public TagServiceImpl(Repository repository) {
    this.repository = repository;
  }
  @Override
  public Map<String, List<Tag>> getTags(List<String>
      productIds) {
    return repository.getProductTagMappings(productIds);
  }
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/services/TagServiceImpl.java

Here, the implemented method is straightforward. It passes the call to the repository that fetches the tags based on the passed collection of product IDs.

You can add getProductTagMappings to the Repository interface as shown in the next line:
```java
Map<String, List<Tag>> getProductTagMappings(List<String>     productIds);
```
Then you can implement this method in the InMemRepository class as shown in the next code block:

```java
@Override
public Map<String, List<Tag>> getProductTagMappings(    List<String> productIds) {
  return productEntities.entrySet().stream()
            .filter(e -> productIds.contains(e.getKey()))
            .collect(toMap(e -> e.getKey(),               e -> e.getValue().getTags()));
}
```

Here, it first creates the stream of the product map's entry set, then filters the products that match the product passed in this method. At the end, it converts filtered products to map with the product ID as Key and Tags as the value, and then returns it.

Now, if you call the "product" GraphQL query, and even if products are fetched with a proper normalized database, it loads the product tags in batches without the N+1 problem.

You are done with GraphQL query implementation and should be comfortable with implementing queries on your own.

Next, you are going to implement GraphQL mutations.

## Implementing GraphQL mutations

As per the GraphQL schema, you are going to implement two mutations – addTag and addQuantity.

The addTag mutation takes productId and a collection of tags as arguments and returns the Product object. The addQuantity mutation takes productId and quantity to add and returns Product.

Let's add this implementation to the existing ProductDatafetcher class as shown in the following code block:

```java
// rest of the ProductDatafetcher class code
@DgsMutation(field = MUTATION.AddTag)
public Product addTags(@InputArgument("productId") String
      productId,
    @InputArgument(value = "tags", collectionType =
        TagInput.class) List<TagInput> tags) {
  return tagService.addTags(productId, tags);
}
@DgsMutation(field = MUTATION.AddQuantity)
public Product addQuantity(
    @InputArgument("productId") String productId,
    @InputArgument(value = "quantity") int qty) {
  return productService.addQuantity(productId, qty);
}

// rest of the ProductDatafetcher class code
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductDatafetcher.java

Here, these signatures follow the respective mutations written in the GraphQL schema. You are using another DGS framework @DgsMutation annotation, which is a type of @DgsData annotation that is marked on methods to denote them as a data fetcher method. The @DgsMutation annotation by default has the "Mutation" value set to the parentType property. You just have to set the field property in this annotation. Both of these methods have their respective values set to the field property in the @DgsMutation annotation.

If you notice, you will find that the @InputArgument annotation for tags is using another collectionType property that is used for setting the type of input. It is required when the input type is not scalar. If you don't use it, you'll get an error. Therefore, make sure to use the collectionType property whenever you have a non-scalar type input.

These methods use the tag and product services to perform the requested operations. So far, you have not added the tag service in the ProductDatafetcher class. Therefore, you need to add TagService first as shown in the next code block:
```java
// rest of the ProductDatafetcher class code
private final TagService tagService;
public ProductDatafetcher(ProductService productService, TagService tagService) {
  this.productService = productService;
  this.tagService = tagService;
}
// rest of the ProductDatafetcher class code
```

Here, the TagService bean has been injected using the constructor.

Now, you need to implement the addTag() method in the TagService and addQuantity methods in ProductService. Both the interfaces and their implementations are straightforward and pass the call to the repository to perform the operations. You can have a look at the source code in the GitHub code repository (https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter14) to look into these implementations.

Let's add these two methods to the Repository interface as shown in the next code block:
```java
Product addTags(String productId, List<TagInput> tags);
Product addQuantity(String productId, int qty);
```
These signatures in the Repository interface also follow the respective mutations written in the GraphQL schema.
Let's implement the addTags() method first in the InMemRepository class as shown in the next code block:
```java
@Override
public Product addTags(String productId, List<TagInput> tags) {
  if (Strings.isBlank(productId)) {
    throw new RuntimeException("Invalid Product ID.");
  }
  Product product = productEntities.get(productId);
  if (Objects.isNull(product)) {
    throw new RuntimeException("Product not found.");
  }
  if (tags != null && !tags.isEmpty()) {
    List<String> newTags = tags.stream()
         .map(t -> t.getName()).collect(toList());
    List<String> existingTags = product.getTags().stream()
         .map(t -> t.getName()).collect(toList());
    newTags.stream().forEach(nt -> {
      if (!existingTags.contains(nt)) {
        product.getTags().add(Tag.newBuilder()
           .id(UUID.randomUUID().toString()).
                name(nt).build());
      }
    });
    productEntities.put(product.getId(), product);
  }
  return product;
}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/repository/InMemRepository.java

This implementation is straightforward. It performs a couple of validations for the passed product ID. Then it compares the new and existing tags, and adds the new tags to the passed product only if existing tags don't exist. At the end, it updates the concurrent map and returns the updated product.

Let's add the implementation of the addQuantity() method to the InMemRepository class next, as shown in the following code block:
```java
@Override
public Product addQuantity(String productId, int qty) {
  if (Strings.isBlank(productId)) {
    throw new RuntimeException("Invalid Product ID.");
  }
  if (qty < 1) {
    throw new RuntimeException("Quantity arg can't be less
                               than 1");
  }
  Product product = productEntities.get(productId);
  if (Objects.isNull(product)) {
    throw new RuntimeException("Product not found.");
  }
  product.setCount(product.getCount() + qty);
  productEntities.put(product.getId(), product);
  return product;
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/repository/InMemRepository.java

Here, you first perform the validation for the productId and qty arguments. If everything goes fine, then you increase the quantity of the product, update the concurrent map, and return the updated product.

You are done with the implementation of GraphQL mutations. You can now test your changes. You need to build the application before running the test. Let's build the application using the following command:
```sh
$ gradlew clean build
```
Once the build is done successfully, you can run the following command to run the application:
```sh
$ java –jar build/libs/chapter14-0.0.1-SNAPSHOT.jar
```
The application should be running on default port 8080 if you have not made any changes to the port settings.

Now, you can open a browser window and open GraphiQL using the following URL: http://localhost:8080/graphiql (part of the DGS framework). Change the host/port accordingly if required.

You can use the following GraphQL request to perform the addTag mutation:
```graphql
mutation {
  addTag(productId: "a1s2d3f4-0", tags: [
    {
      name:"new Tags..."
    }
  ]) {
    id
    name
    price
    description
    tags {
      id
      name
    }
  }
}
```
Here, you pass productId and tags as arguments. You can use the following GraphQL request to perform the addQuantity mutation:
```graphql
mutation {
addQuantity(productId: "a1s2d3f4-0", quantity: 10) {
    id
    name
    description
    price
    count
    tags {
      id
      name
    }
  }
}
```
Here, you pass productId and quantity as arguments. You have learned how to implement GraphQL mutations in the GraphQL server. Let's implement GraphQL subscriptions in the next subsection.


## Implementing GraphQL subscriptions

Subscription is another GraphQL root type that sends the object to the subscriber (client) when a particular event occurs.

Let's assume an online shop offers a discount on products when the product's inventory reaches a certain level. You cannot track each and every product's quantity manually and then perform the computation and trigger the discount. This is where you can make use of the subscription.

Each change in the product's inventory (quantity) through the addQuantity() mutation should trigger the event and the subscriber should receive the updated product and hence the quantity. Then, the subscriber can place the logic and automate this process.

Let's write the subscription that would send the updated product object to the subscriber. You are going to use Reactive Streams and WebSocket to implement this functionality.

Let's add additional dependencies in build.gradle to take care of the auto-configuration of WebSocket and the playground tool to test the subscription functionality. (By default DGS provides the GraphiQL app to explore the documentation and schema and play with queries. However, the bundled GraphiQL tool doesn't work properly for testing the subscription presently. Once it starts working, you don't need to add the playground tool.)

Let's add these dependencies to build.gradle as shown in the following code block:
```gradle
dependencies {
  // other dependencies …
  runtimeOnly 'com.netflix.graphql.dgs:graphql-dgs-subscriptions-websockets-autoconfigure'
  implementation 'com.graphql-java-kickstart:playground-spring-boot-starter:11.0.0'
  // other dependencies …
}
```
Now, you can add the following subscription data fetcher to the ProductDatafetcher class as shown in the following code:
```java
// rest of the ProductDatafetcher class code
@DgsSubscription(field = SUBSCRIPTION.QuantityChanged)
public Publisher<Product> quantityChanged(
                     @InputArgument("productId") String productId) {
  return productService.gerProductPublisher();
}
// rest of the ProductDatafetcher class code
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductDatafetcher.java

Here, you are using another DGS framework annotation, @DgsSubscription, which is a type of @DgsData annotation that is marked on a method to denote it as a data fetcher method. The @DgsSubscription annotation by default has the Subscription value set to the parentType property. You just have to set the field property in this annotation. By setting the field to quantityChanged, you are indicating to the DGS framework to use this method when the subscription request for quantityChanged is called.

The Subscription method returns the Publisher instance, which can be sent an unbound number of objects (in this case, Product instances) to multiple subscribers. Therefore, the client just needs to subscribe to the product publisher.

You need to add a new method to the ProductService interface and its implementation in the ProductServiceImpl class. The method signature in the ProductService interface and its implementation are straightforward. It passes the call to the repository to perform the operation. You can have a look at the source code in the GitHub code repository at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter14.

Actual work is being performed by the repository. Therefore, you need to make certain changes in the repository as shown in the following steps:

First add the following method signature to the Repository interface:
```java
      Publisher<Product> getProductPublisher();
```
Next, you have to implement the getProductPublisher() method in the InMemRepository class. This method returns the product publisher as shown in the following code:
```java
      public Publisher<Product> getProductPublisher() {
        return productPublisher;
      }
```
Now, we need all the magic to be performed by Reactive Streams. First, let's declare the FluxSink<Product> and ConnectableFlux<Product> (which is returned by the repository) variables:
```java
      private FluxSink<Product> productsStream;
      private ConnectableFlux<Product> productPublisher;
```
Now, we need to initialize these declared instances. Let's do so in InMemRepository's constructor as shown in the following code:
```java
      Flux<Product> publisher = Flux.create(emitter -> {
        productsStream = emitter;
      });
      productPublisher = publisher.publish();
      productPublisher.connect();
```
Flux<Product> is a product stream publisher that passes the baton to productsStream (FluxSink) for emitting the next signals followed by onError() or onComplete() events. This means productsStream should emit the signal when the product quantity gets changed. When Flux<Product> calls the publish() method, it returns an instance of connectableFlux, which is assigned to productPublisher (the one that is returned by the subscription).
You are almost done with the setup. You just need to emit the signal (product) when the product gets changed. Let's add the following highlighted line to the addQuantity() method before it returns the product, as shown in the following code:
```java
      product.setCount(product.getCount() + qty);
      productEntities.put(product.getId(), product);
      productsStream.next(product);
      return product;
```
You have completed the subscription quantityChanged implementation. You can test it next.

You need to build the application before running the test. Let's build the application using the following command:
```sh
$ gradlew clean build
```
Once the build is done successfully, you can run the following command to run the application:
```sh
$ java –jar build/libs/chapter14-0.0.1-SNAPSHOT.jar
```
The application should be running on default port 8080 if you have not made any changes in the port settings.

The playground tool should be available at http://localhost:8080/playground if the application is running on localhost, else make the appropriate changes in the hostname.

Once the playground app is up, run the following query in it:
```graphql
subscription {
  quantityChanged(productId: "a1s2d3f4-0") {
    id
    name
    description
    price
    count
  }
}
```
This should trigger the application in listening mode. The app will wait for object publications.

Now, you can open another browser window and open GraphiQL using the following URL: http://localhost:8080/graphiql. Change the host/port accordingly if required.

Here, you can fire the addQuantity mutation by running the following:
```graphql
mutation {
addQuantity(productId: "a1s2d3f4-0", quantity: 10) {
    id
    name
    price
    count
  }
}
```
Each successful change would publish the updated product to the playground app.

You should know about the instrumentation that helps to implement the tracing, logging, and metrics collection. Let's discuss this in the next subsection.

Instrumenting the GraphQL API
The GraphQL Java library supports the instrumentation of the GraphQL API. This can be used to support metrics, tracing, and logging. The DGS framework also uses it. You just have to mark the instrumentation class with the Spring @Component annotation.

The instrumentation bean should implement the graphql.execution.instrumentation.Instumentation interface, an easier way to extend the SimpleInstumentation class.

Let's add instrumentation that would record the time taken by the data fetcher and complete GraphQL request processing. This metric may help you to fine-tune the performance and identify the fields that take more time to resolve.

Let's create the TracingInstrumentation.java file in the instrumentation package and add the following code:
```java
@Component
public class TracingInstrumentation extends
      SimpleInstrumentation {
  private final Logger LOG = LoggerFactory.getLogger(
      getClass());
  @Override
  public InstrumentationState createState() {
    return new TracingState();
  }
  static class TracingState implements InstrumentationState {
    long startTime;
  }
// continue…
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/instrumentation/TracingInstrumentation.java

This class extends SimpleInstrumentation and is created as a Spring bean by marking it as @Component. First of all, you need to create the instrumentation state by overriding the createState() method. Since you are implementing the time metric, you choose startTime as the state. A static inner class is added for declaring the startTime state.

As a next activity, you would like to initialize the instrumentation state. For that purpose you can override the beginExecution() method as shown in the following code:
```java
@Override
public InstrumentationContext<ExecutionResult>
    beginExecution(
      InstrumentationExecutionParameters parameters) {
  TracingState tracingState =
      parameters.getInstrumentationState();
  tracingState.startTime = System.currentTimeMillis();
  return super.beginExecution(parameters);
}
```
This method allows you to set the instrumentation parameters.

The startTime state is set. Next, you'll override the instrumentExecutionResult() method. This helps you to instrument the execution result such as calculating the total execution time. Let's add the following code to calculate the total execution time:
```java
@Override
public CompletableFuture<ExecutionResult>instrumentExecutionResult(ExecutionResult executionResult,    InstrumentationExecutionParameters parameters) {
  TracingState tracingState = parameters.getInstrumentationState();
  long timeTaken = System.currentTimeMillis() - tracingState.startTime;
  LOG.info("Request processing took: {} ms", timeTaken);
  return super.instrumentExecutionResult(executionResult, parameters);
}
```
It is a straightforward implementation to calculate the total execution time. It extracts the startTime state from the parameters and then uses it to calculate the timeTaken value.

So far you have overridden three methods – the initial method (createState()), the beginning method (beginExecution()) for state initialization, and the end method (instrumentExecutionResult()) for final calculations or state recording.

One intermediate method (instrumentDataFetcher()) that falls between beginExecution() and instrumentExecutionResult() is yet to be overridden. It is complex compared to other methods. Therefore, you'll override it after other methods.

Let's add the following code to override the instrumentDataFetcher() method:

```java
@Override
public DataFetcher<?> instrumentDataFetcher(DataFetcher<?>   
      dataFetcher, InstrumentationFieldFetchParameters parameters) {
  if (parameters.isTrivialDataFetcher()) {
    return dataFetcher;
  }
  return environment -> {
    long initTime = System.currentTimeMillis();
    Object result = dataFetcher.get(environment);
    String msg = "Instrumentation of datafetcher {} took {} ms";
    if (result instanceof CompletableFuture) {
      ((CompletableFuture<?>) result).whenComplete((r, ex)
          -> {
        long timeTaken = System.currentTimeMillis() – initTime;
        LOG.info(msg, findDatafetcherTag(parameters),
            timeTaken);
      });
    } else {
      long timeTaken = System.currentTimeMillis() – initTime;
      LOG.info(msg, findDatafetcherTag(parameters),timeTaken);
    }
    return result;
  };
}
```
This method is used for instrumenting the data fetchers. You have added two separate blocks to calculate the data fetching time because values can be returned in two ways by data fetcher methods – a blocking call or an asynchronous call (CompletableFuture). This method would be called for each data fetching call whether it is for the root type or for a field of the object type.

The final piece of instrumentation implementation is the findDatafetcherTag() method. This private method is added to find out the data fetching type of the field/root type.

Let's add it as shown in the following code:

```java
private String findDatafetcherTag(InstrumentationFieldFetchParameters parameters) {
  GraphQLOutputType type = parameters.getExecutionStepInfo().getParent().getType();
  GraphQLObjectType parent;
  if (type instanceof GraphQLNonNull) {
    parent = (GraphQLObjectType) ((GraphQLNonNull) type).getWrappedType();
  } else {
    parent = (GraphQLObjectType) type;
  }
  return parent.getName() + "." +
      parameters.getExecutionStepInfo().getPath()
          .getSegmentName();
}
```
Here, GraphQLNonNull tells us whether the type is a wrapped type or not. Next, let's find out what tool you can use for documenting APIs.

## Documenting APIs
You can use GraphQL or a playground tool that provides a graphical interface to explore the GraphQL schema and documentation.

However, if you are looking for a static page, then you can use tools such as graphdoc (https://github.com/2fd/graphdoc) for generating the static documentation of GraphQL APIs.

Next, let's learn about GraphQL API testing using the DGS framework.

### Test automation
The DGS framework provides you with classes and utilities that you can use to test GraphQL APIs.

Create a new file called ProductDatafetcherTest.java inside the datafetchers package in the test directory and add the following code:

```java
@SpringBootTest(classes = {DgsAutoConfiguration.class,
                  ProductDatafetcher.class,
                  BigDecimalScalar.class})
public class ProductDatafetcherTest {
  private final InMemRepository repo = new
    InMemRepository();
  private final int TEN = 10;
  @Autowired
  private DgsQueryExecutor dgsQueryExecutor;
  @MockBean
  private ProductService productService;
  @MockBean
  private TagService tagService;
// continue…
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/test/java/com/packt/modern/api/datafetchers/ProductDatafetcherTest.java

Here, you are using the @SpringBootTest annotation to execute the test. By providing limited classes such as DgsAutoConfiguration, ProductDatafetcher, and BigDecimalScalar, you are limiting the Spring context. You should add the classes here that are going to take part in testing.

First of all, you are auto-wiring the DgsQueryExecutor class that performs the query execution. After that, you add two Spring-injected mock beans for the Product and Tag services.

You are ready with the configuration and instances you need to run the tests.

Let's add the setup method that is required before running the tests. You can add the following method for this purpose:
```java
@BeforeEach
public void beforeEach() {
  List<Tag> tags = new ArrayList<>();
  tags.add(Tag.newBuilder().id("tag1").name("Tag 1").build());
  Product product = Product.newBuilder().id("any")
    .name("mock title").description("mock description")
    .price(BigDecimal.valueOf(20.20)).count(100)
    .tags(tags).build();
  given(productService.getProduct("any")).willReturn(product);
  tags.add(Tag.newBuilder().id("tag2").name("addTags").build());
  product.setTags(tags);
  given(tagService.addTags("any", List.of(TagInput.newBuilder().name("addTags").build())))
      .willAnswer(invocation -> product);
}
```
In this method, we used Mockito for stubbing the service methods.

You are done with the setup. Let's run our first test that would fetch the JSON object after running the GraphQL product query next.

### Testing GraphQL queries

Let's add the following code for testing the product query:
```java
@Test
@DisplayName("Verify the JSON attrs returned from query 'product'")
public void product() {
  String name = dgsQueryExecutor.executeAndExtractJsonPath(
         "{ product(id: \"any\") { name }}", "data.product.name");
  assertThat(name).contains("mock title");
}
```
Here, you are using the DgsQueryExecutor instance to execute the product query and extract the JSON property.

Next, you'll test the product query again, but this time for testing the exception.

You can add the following code to test the exception thrown by the product query:
```java
@Test
@DisplayName("Verify exception for incorrect ID in query
             'product'")
public void productWithException() {
  given(productService.getProduct("any"))
     .willThrow(new RuntimeException("Invalid Product
                ID."));
  ExecutionResult result = dgsQueryExecutor.execute(" { product (id: \"any\") {name }}");
  verify(productService, times(1)).getProduct("any");
  assertThat(result.getErrors()).isNotEmpty();
  assertThat(result.getErrors().get(0).getMessage())
     .isEqualTo("java.lang.RuntimeException: Invalid Product ID.");
}
```
Here, the product service method is stubbed for throwing the exception. When DgsQueryExecutor runs, the Spring-injected mock bean uses the stubbed method to throw the exception that is being asserted here.

Next, let's query product again, this time to explore GraphQLQueryRequest, which allows you to form the GraphQL query in a fluent way. The GraphQLQueryRequest construction takes two arguments – first the instance of GraphQLQuery, which can be a query/mutation or subscription, and second the projection root type of BaseProjectionNode, which allows you to select the fields.

Let's add the following code to test the product query using GraphQLQueryRequest:
```java
@Test
@DisplayName("Verify JSON attrs using GraphQLQueryRequest")
void productsWithQueryApi() {
  GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
           ProductGraphQLQuery.newRequest().id("any").build(),
           new ProductProjectionRoot().id().name());
  String name = dgsQueryExecutor.executeAndExtractJsonPath(
           graphQLQueryRequest.serialize(), "data.product.name");
  assertThat(name).contains("mock title");
}
```
Here, the ProductGraphQLQuery class is part of the auto-generated code by the DGS GraphQL Gradle plugin.

One thing we have not yet tested in previous tests is verifying the sub-fields in the tags field of product.

Let's verify it in the next test case. Add the following code to verify the tags:

```java
@Test
@DisplayName("Verify the Tags returned from the query
    'product'")
void productsWithTags() {
  GraphQLQueryRequest graphQLQueryRequest = new
    GraphQLQueryRequest(
        ProductGraphQLQuery.newRequest().id("any").build(),
        new ProductProjectionRoot().id().name()
            .tags().id().name());
  Product p = dgsQueryExecutor.
    executeAndExtractJsonPathAsObject(
        graphQLQueryRequest.serialize(),
        "data.product", new TypeRef<>() {});
  assertThat(p.getId()).isEqualTo("any");
  assertThat(p.getName()).isEqualTo("mock title");
  assertThat(p.getTags().size()).isEqualTo(2);
  assertThat(p.getTags().get(0).getName()).isEqualTo("Tag 1");
}
```
Here, you can see that you have to use a third argument (TypeRef) in the executeAndExtractJsonPathAsObject() method if you want to query the sub-fields. If you don't use it, you will get an error.

You are done with GraphQL query testing. Let's move on to testing the mutations in the next subsection.

Testing GraphQL mutations
Testing a GraphQL mutation is no different than testing GraphQL queries.

Let's test the addTag mutation as shown in the following code:
```java
@Test
@DisplayName("Verify the mutation 'addTags'")
void addTagsMutation() {
  GraphQLQueryRequest graphQLQueryRequest = new
    GraphQLQueryRequest(
      AddTagGraphQLQuery.newRequest().productId("any")
       .tags(List.of(TagInput.newBuilder().name(
             "addTags").build()))
            .build(),new AddTagProjectionRoot().name().
              count());
  ExecutionResult executionResult =
     dgsQueryExecutor.execute(
         graphQLQueryRequest.serialize());
  assertThat(executionResult.getErrors()).isEmpty();
  verify(tagService).addTags("any",
         List.of(
           TagInput.newBuilder().name("addTags").build()));
}
```
Here, the AddTagGraphQLQuery class is part of the auto-generated code by the DGS GraphQL Gradle plugin. You fire the request and then validate the results based on the existing configuration and setup.

Similarly, you can test the addQuantity mutation. Only the arguments and assertions would change; the core logic and classes would remain the same.

You can add the test as shown in the next code block to test the addQuantity mutation:

```java
@Test
@DisplayName("Verify the mutation 'addQuantity'")
void addQuantityMutation() {
  given(productService.addQuantity("a1s2d3f4-1", TEN))
      .willReturn(repo.addQuantity("a1s2d3f4-1", TEN));
  GraphQLQueryRequest graphQLQueryRequest = new
    GraphQLQueryRequest(
      AddQuantityGraphQLQuery.newRequest().productId(
          "a1s2d3f4-1")
          .quantity(TEN).build(),
      new AddQuantityProjectionRoot().name().count());
  ExecutionResult executionResult = dgsQueryExecutor.execute(        graphQLQueryRequest.serialize());
  assertThat(executionResult.getErrors()).isEmpty();
  Object obj = executionResult.getData();
  assertThat(obj).isNotNull();
  Map<String, Object> data = (Map)((Map)
    executionResult.getData())
                    .get(MUTATION.AddQuantity);
  org.hamcrest.MatcherAssert
       .assertThat((Integer) data.get("count"),
         greaterThan(TEN));
}
```
You are done with GraphQL mutation testing. Let's move on to testing subscriptions in the next subsection.

## Testing GraphQL subscriptions

Testing the subscription needs extra effort and care as you can see in the following code, which performs the test for the quantityChanged subscription. It uses the existing addQuantity mutation to trigger the subscription publisher that sends a product object on each call. You capture the product of the first call and store the value of the count field. Then, use it to perform the assertion as shown in the following code:

```java
@Test
@DisplayName("Verify the subscription 'quantityChanged'")
void reviewSubscription() {
  given(productService.gerProductPublisher()).willReturn(repo.getProductPublisher());
  ExecutionResult executionResult =
    dgsQueryExecutor.execute(
      "subscription { quantityChanged(productId:
         \"a1s2d3f4-0\") { id name price count } }");
  Publisher<ExecutionResult> publisher =
    executionResult.getData();
  List<Product> product = new CopyOnWriteArrayList<>();
  publisher.subscribe(new Subscriber<>() {
    @Override
    public void onSubscribe(Subscription s) { s.request(2); }
    @Override
    public void onNext(ExecutionResult result) {
      if (result.getErrors().size() > 0) {
        System.out.println(result.getErrors());
      }
      Map<String, Object> data = result.getData();
      product.add(new ObjectMapper().convertValue(
          data.get(SUBSCRIPTION.QuantityChanged),
                   Product.class));
    }
    @Override
    public void onError(Throwable t) {}
    @Override
    public void onComplete() {}
  });
  addQuantityMutation();
  Integer count = product.get(0).getCount();
  addQuantityMutation();
  assertThat(product.get(0).getId())
            .isEqualTo(product.get(1).getId());
  assertThat(product.get(1).getCount())
            .isEqualTo(count.intValue() + TEN);
}
```
Here, the core logic lies in the subscription that is done by calling the publisher.subscribe() method (check highlighted line). You know that the GraphQL quantityChanged subscription returns the publisher. This publisher is received from the data field of the execution result.

The publisher subscribes to the stream by passing an object of Subscriber, which is created on the fly. The subscriber's onNext() method is used to receive the product sent by the GraphQL server. These objects are pushed into the list. Then, you use this list to perform the assertion.


## Summary

In this chapter, you have learned about the different ways of implementing the GraphQL server including federated GraphQL services. You have also explored the complete standalone GraphQL server implementation that performs the following operations:

- Writing the GraphQL schema
- Implementing the GraphQL query APIs
- Implementing the GraphQL mutation APIs
- Implementing the GraphQL subscription APIs
- Writing the data loaders for solving the N+1 problem
- Adding custom scalar types
- Adding the GraphQL API's instrumentation
- Writing the GraphQL API's test automation using Netflix's DGS framework

- You learned about the GraphQL API implementation using Spring and Spring Boot skills that will help you implement GraphQL APIs for your work assignments and personal projects.

## Questions

Why should you prefer frameworks such as Netflix's DGS in place of the graphql-java library to implement GraphQL APIs?

What are federated GraphQL services?

## Further reading

GraphQL Java implementation: https://www.graphql-java.com/ and https://github.com/graphql-java/graphql-java
Netflix DGS documentation: https://netflix.github.io/dgs/getting-started/

GraphQL and Apollo with Android from Novice to Expert (video): https://www.packtpub.com/product/graphql-and-apollo-with-android-from-novice-to-expert-video/9781800564626

