# Chapter 4: Writing Business Logic for APIs

이전 장에서 OpenAPI를 사용하는 API에 대해 논의했습니다. API 인터페이스 및 모델은 Swagger Codegen에서 생성되었습니다. 이 장에서는 비즈니스 로직과 데이터 지속성 측면에서 API 코드를 구현합니다. 구현을 위한 서비스 및 리포지토리를 작성하고 API 응답에 하이퍼미디어 및 ETag도 추가합니다. 제공된 코드는 간결함을 위해 전체 파일이 아닌 중요한 줄로만 구성되어 있다는 점에 유의할 필요가 있습니다. 코드 아래에 제공된 링크에 항상 액세스하여 전체 파일을 볼 수 있습니다.

이 장에는 다음 항목이 포함되어 있습니다.

- 서비스 디자인 개요
- 저장소 구성 요소 추가
- 서비스 구성 요소 추가
- 하이퍼미디어 구현
- 서비스와 HATEOAS로 컨트롤러 강화
- API 응답에 ETag 추가


## Technical requirements

You need the following to execute instructions in this chapter:

- Any Java IDE such as NetBeans, IntelliJ, or Eclipse

- The Java Development Kit (JDK) 15+

- An internet connection to download the dependencies and Gradle

- The Postman tool (https://learning.postman.com/docs/getting-started/sending-the-first-request/)

You can find the code files for this chapter on GitHub at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter04.

## Overview of the service design

프레젠테이션 계층, 응용 프로그램 계층, 도메인 계층 및 인프라 계층의 4개 계층으로 구성된 다계층 아키텍처를 구현할 것입니다. 다계층 아키텍처는 DDD(Domain-Driven Design)로 알려진 아키텍처 스타일의 기본 빌딩 블록입니다. 각 레이어에 대해 간략히 살펴보겠습니다.

- 프레젠테이션 계층: 이 계층은 사용자 인터페이스(UI)를 나타냅니다. 다가오는 7장, 사용자 인터페이스 디자인하기에서는 전자 상거래 앱용 UI를 개발할 것입니다.

- 애플리케이션 계층: 애플리케이션 계층은 애플리케이션 로직을 포함하고 애플리케이션의 전반적인 흐름을 유지하고 조정합니다. 참고로 여기에는 비즈니스 로직이 아닌 애플리케이션 로직만 포함되어 있습니다. RESTful 웹 서비스, 비동기 API, gRPC API 및 GraphQL API는 이 계층의 일부입니다.
애플리케이션 계층의 일부인 3장, API 사양 및 구현에서 REST API 인터페이스 및 컨트롤러(REST API 인터페이스 구현)를 이미 다뤘습니다. 이전 장에서 데모 목적으로 컨트롤러를 구현했습니다. 이 장에서는 실제 데이터를 제공하기 위해 컨트롤러를 광범위하게 구현합니다.

- 도메인 계층: 비즈니스 로직 및 도메인 정보를 포함하는 계층입니다. 여기에는 주문, 제품 등과 같은 비즈니스 개체의 상태가 포함됩니다. 인프라 계층에서 이러한 개체를 읽고 유지하는 역할을 합니다. 도메인 계층도 서비스와 저장소로 구성됩니다. 이 장에서도 이에 대해 다룰 것입니다.

- 인프라 계층: 인프라 계층은 다른 모든 계층에 대한 지원을 제공합니다. 데이터베이스, 메시지 브로커, 파일 시스템 등과의 상호 작용과 같은 통신을 담당합니다. Spring Boot는 인프라 계층으로 작동하며 데이터베이스, 메시지 브로커 등과 같은 외부 및 내부 시스템과의 통신 및 상호 작용을 지원합니다.
우리는 아래에서 위로 접근 방식을 사용할 것입니다. @Repository 컴포넌트로 도메인 레이어 구현을 시작해보자.

## 저장소 구성 요소 추가

@Repository 구성 요소를 추가하기 위해 하향식 접근 방식을 사용할 것입니다. @Repository 구성 요소를 사용하여 도메인 계층 구현을 시작하겠습니다. 이에 따라 후속 섹션에서 서비스를 구현하고 Controller 구성 요소를 개선할 것입니다. 먼저 @Repository 구성 요소를 코딩한 다음 생성자 주입을 사용하여 @Service 구성 요소에서 사용합니다. @Controller 구성 요소는 @Service 구성 요소를 사용하여 향상되며 생성자 주입을 사용하여 컨트롤러에도 주입됩니다.

### @Repository annotation
Repository 구성 요소는 @Repository 주석으로 표시된 Java 클래스입니다. 이것은 데이터베이스와 상호 작용하는 데 사용되는 특별한 Spring 구성 요소입니다.

@Repository는 DDD의 Repository와 Java EE 패턴인 DAO(Data Access Object)를 모두 나타내는 범용 스테레오타입입니다. 개발자와 팀은 기본 접근 방식을 기반으로 Repository 객체를 처리해야 합니다. DDD에서 Repository는 모든 개체에 대한 참조를 전달하고 요청된 개체의 참조를 반환해야 하는 중심 개체입니다. @Repository로 표시된 클래스를 작성하기 전에 필요한 모든 종속성과 구성을 준비해야 합니다.

다음 라이브러리를 데이터베이스 종속성으로 사용합니다.

- H2: H2의 메모리 인스턴스를 사용하지만 파일 기반 인스턴스를 사용할 수도 있습니다.
- Hibernate ORM: 데이터베이스 객체 매핑용.
- Flyway: 데이터베이스 마이그레이션. 데이터베이스를 유지 관리하고 롤백, 버전 업그레이드 등을 허용하는 데이터베이스 변경 기록을 유지 관리하는 데 도움이 됩니다.

Let's add these dependencies to the build.gradle file. org.springframework.boot:spring-boot-starter-data-jpa adds all the required JPA dependencies including Hibernate:
```
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

implementation 'org.flywaydb:flyway-core'
runtimeOnly 'com.h2database:h2'
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/build.gradle

After adding the dependencies, we can add the configuration related to the database.

### Database and JPA configuration

We also need to modify the application.properties file with the following configuration:

1. Data source configuration
The following is the Spring data source configuration:
```yaml
spring.datasource.name=ecomm
spring.datasource.url=jdbc:h2:mem:ecomm;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE;DATABASE_TO_UPPER=false

spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

We need to add H2-specific properties to the data source. The URL value suggests that a memory-based H2 database instance will be used.

2. H2 database configuration

The following are the two H2 database configurations:
```
spring.h2.console.enabled=true
spring.h2.console.settings.web-allow-others=false
```

The H2 console is enabled for local access only; it means you can access the H2 console only on localhost. Also, remote access is disabled by setting web-allow-others to false.

3. JPA configuration
The following are the JPA/Hibernate configurations:

```yaml
spring.jpa.properties.hibernate.default_schema=ecomm
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=true
spring.jpa.format_sql=true
spring.jpa.generate-ddl=false
spring.jpa.hibernate.ddl-auto=none
```

We don't want to generate the DDL or to process the SQL file, because we want to use Flyway for database migrations. Therefore, generate-ddl is marked with false and ddl-auto is set to none.

4. Flyway configuration

The following are the Flyway configurations:
```
spring.flyway.url=jdbc:h2:mem:ecomm
spring.flyway.schemas=ecomm
spring.flyway.user=sa
spring.flyway.password=
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/resources/application.properties

*ACCESSING THE H2 DATABASE*

You can access the H2 database console using /h2-console. For example, if your server is running on localhost and on port 8080 then you can access it using http://localhost:8080/h2-console/.

### The database and seed data script
이제 build.gradle 및 application.properties 파일 구성을 완료했으며 이제 코드 작성을 시작할 수 있습니다. 먼저 Flyway 데이터베이스 마이그레이션 스크립트를 추가합니다. 이 스크립트는 SQL로만 작성할 수 있습니다. 이 파일을 src/main/resources 디렉토리 내의 db/migration 디렉토리에 배치할 수 있습니다. Flyway 명명 규칙(V<version>.<name>.sql)을 따르고 db/migration 디렉토리 안에 V1.0.0.Init.sql 파일을 생성합니다. 그런 다음 이 파일에 다음 스크립트를 추가할 수 있습니다.

```sql
create schema if not exists ecomm;
-- Other script tags
create TABLE IF NOT EXISTS ecomm.cart (
   id uuid NOT NULL,
   user_id uuid NOT NULL,
   FOREIGN KEY (user_id)
   REFERENCES ecomm.user(id),
   PRIMARY KEY(id)
);

create TABLE IF NOT EXISTS ecomm.cart_item (
   cart_id uuid NOT NULL,
   item_id uuid NOT NULL,
   FOREIGN KEY (cart_id)
   REFERENCES ecomm.cart(id),
   FOREIGN KEY(item_id)
   REFERENCES ecomm.item(id)
);
-- other SQL scripts
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/resources/db/migration/V1.0.0__Init.sql

This script creates the ecomm schema and adds all the tables required for our sample e-commerce app. It also adds insert statements for the seed data.

### Adding entities
이제 엔티티를 추가할 수 있습니다. 엔티티는 Hibernate와 같은 ORM 구현을 사용하여 데이터베이스 테이블에 직접 매핑되는 @Entity 주석으로 표시된 특수 객체입니다. 또 다른 인기 있는 ORM은 EclipseLink입니다. com.packt.modern.api.entity 패키지에 모든 엔터티 개체를 배치할 수 있습니다. CartEntity.java 파일을 생성해 보겠습니다.

```java
@Entity
@Table(name = "cart")
public class CartEntity {
  @Id
  @GeneratedValue
  @Column(name = "ID", updatable = false, nullable = false)
  private UUID id;

  @OneToOne
  @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
  private UserEntity user;
  @ManyToMany(
    cascade = CascadeType.ALL
  )
  @JoinTable(
    name = "CART_ITEM",
    joinColumns = @JoinColumn(name = "CART_ID"),
    inverseJoinColumns = @JoinColumn(name = "ITEM_ID")
  )
  private List<ItemEntity> items = Collections.emptyList();

// Getters/Setter and other codes are removed for brevity
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/entity/CartEntity.java

여기서 @Entity 주석은 Entity이고 데이터베이스 테이블에 매핑되어야 함을 나타내는 javax.persistence 패키지의 일부입니다. 기본적으로 엔티티 이름을 사용하지만 @Table 주석을 사용하여 데이터베이스 테이블에 매핑합니다.

또한 Cart 엔티티를 User Entity 및 Item Entity에 각각 매핑하기 위해 일대일 및 다대다 주석을 사용하고 있습니다. ItemEntity 목록은 @JoinTable과도 연관되어 있습니다. CART_ITEM 조인 테이블을 사용하여 해당 테이블의 CART_ID 및 ITEM_ID 열을 기반으로 장바구니 및 제품 항목을 매핑하기 때문입니다.

UserEntity에는 다음 코드 블록과 같이 관계를 유지하기 위해 Cart 엔터티도 추가되었습니다. FetchType은 LAZY로 표시됩니다. 즉, 명시적으로 요청할 때만 사용자의 장바구니가 로드됩니다. 또한 orphanRemoval을 true로 구성하여 수행할 수 있는 사용자가 참조하지 않는 장바구니를 제거하려고 합니다.

```java
@Entity
@Table(name = "user")
public class UserEntity {
  // other code
  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY,
                                 orphanRemoval = true)
  private CartEntity cart;
  // other code…
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/entity/UserEntity.java

All other entities are being added to the entity package located at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/entity.

Now, we can add the repository.

Adding repositories
All the repository have been added to https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/repository.

Repositories are simplest to add for CRUD operations, thanks to Spring Data JPA. You just have to extend the interfaces with default implementations, such as CrudRepository, which provides all the CRUD operation implementation such as save, saveAll, findById, findAll, findAllById, delete, and deleteById. The Save(Entity e) method is used for both create and update entity operations.

Let's create CartRepository:

public interface CartRepository extends       CrudRepository<CartEntity, UUID> {

  @Query("select c from CartEntity c join c.user u where u.id =          :customerId")

  public Optional<CartEntity> findByCustomerId(@      Param("customerId") UUID customerId);

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/repository/CartRepository.java

The CartRepository interface extends the CrudRepository part of the org.springframework.data.repository package. You can also add methods supported by the JPA Query Language marked with the @Query annotation (part of the org.springframework.data.jpa.repository package). The query inside the @Query annotation is written in Java Persistence Query Language (JPQL). JPQL is very similar to SQL, however, here you used the Java class name mapped to a database table instead of using the actual table name. Therefore, we have used CartEntity as the table name instead of Cart.

NOTE

Similarly, for attributes, you should use the variable names given in the class for the fields, instead of using the database table fields. In any case, if you use the database table name or field name and it does not match with the class and class members mapped to the actual table, you will get an error.

You must be wondering, "What if I want to add my own custom method with JPQL or native SQL?" Well let me tell you, you can do that too. For orders, we have added a custom interface for this very purpose. First, let's have a look at OrderRepository, which is very similar to CartRepository:

@Repository

public interface OrderRepository extends       CrudRepository<OrderEntity, UUID>, OrderRepositoryExt {

  @Query("select o from OrderEntity o join o.userEntity u where          u.id = :customerId")

  public Iterable<OrderEntity> findByCustomerId(@      Param("customerId") UUID customerId);

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/repository/OrderRepository.java

If you look closely, we have extended an extra interface – OrderRepositoryExt. This is our extra interface for the Order repository and consists of the following code:

public interface OrderRepositoryExt {

  Optional<OrderEntity> insert(NewOrder m);

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/repository/OrderRepositoryExt.java

We already have a save() method for this purpose in CrudRepository, however, we want to use a different implementation. For this purpose, and to demonstrate how you can create your own repository method implementation, we are adding this extra repository interface.

Now, let's create the OrderRepositoryExt interface implementation as shown here:

@Repository

@Transactional

public class OrderRepositoryImpl implements OrderRepositoryExt {

  @PersistenceContext

  private EntityManager em;

  private ItemRepository itemRepo;

  private ItemService itemService;

  public OrderRepositoryImpl(EntityManager em, ItemRepository itemRepo, ItemService itemService) {

    this.em = em;

    this.itemRepo = itemRepo;

    this.itemService = itemService;

  }

  // other code

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/repository/OrderRepositoryImpl.java

This way we can also have our own implementation in JPQL/Hibernate Query Language (HQL), or in native SQL. Here, the @Repository annotation tells the Spring container that this special component is a Repository and should be used for interacting with the database using the underlying JPA.

It is also marked as @Transactional, which is a special annotation that means that transactions performed by methods in this class will be managed by Spring. It removes all the manual work of adding commits and rollbacks. You can also add this annotation to a specific method inside the class.

We are also using @PersistenceContext for the EntityManager class, which allows us to create and execute the query manually as shown in the following code:

@Override

public Optional<OrderEntity> insert(NewOrder m) {

// Items are already in cart and saved in db when user places

// order

// Here you can also populate other Order details like address

// etc.

  Iterable<ItemEntity> dbItems =                              

                     itemRepo.findByCustomerId(m.getCustomerId());

  List<ItemEntity> items =

                 StreamSupport.stream(dbItems.spliterator(),                                       false)

                .collect(toList());

  if (items.size() < 1) {

    throw new ResourceNotFoundException(String.format(           "There is no item found in customer's (ID: %s)           cart.", m.getCustomerId()));

  }

  BigDecimal total = BigDecimal.ZERO;

  for (ItemEntity i : items) {

    total = (BigDecimal.valueOf(i.getQuantity()).multiply(

                                i.getPrice())).add(total);

  }

  Timestamp orderDate = Timestamp.from(Instant.now());

  em.createNativeQuery("""

    INSERT INTO ecomm.orders (address_id, card_id, customer_id

    order_date, total, status) VALUES(?, ?, ?, ?, ?, ?)

    """)

    .setParameter(1, m.getAddress().getId())

    .setParameter(2, m.getCard().getId())

    .setParameter(3, m.getCustomerId())

    .setParameter(4, orderDate)

    .setParameter(5, total)

    .setParameter(6, StatusEnum.CREATED.getValue())

    .executeUpdate();

  Optional<CartEntity> oCart =       cRepo.findByCustomerId(UUID.fromString(m.                              getCustomerId()));

  CartEntity cart = oCart.orElseThrow(() -> new               ResourceNotFoundException(String.format("Cart not               found for given customer (ID: %s)",               m.getCustomerId())));

  itemRepo.deleteCartItemJoinById(cart.getItems().stream()             .map(i -> i.getId()).collect(toList()), cart.                  getId());

  OrderEntity entity = (OrderEntity) em.createNativeQuery("""

    SELECT o.* FROM ecomm.orders o WHERE o.customer_id = ? AND

    o.order_date >= ?

    """, OrderEntity.class)

    .setParameter(1, m.getCustomerId())

    .setParameter(2, OffsetDateTime.ofInstant(orderDate.                  toInstant(),

       ZoneId.of("Z")).truncatedTo(ChronoUnit.MICROS))

    .getSingleResult();

  oiRepo.saveAll(cart.getItems().stream()

       .map(i -> new OrderItemEntity().setOrderId(entity.            getId())

         .setItemId(i.getId())).collect(toList()));

  return Optional.of(entity);

}

This method basically first fetches the items in the customer's cart. Then, it calculates the order total, creates a new order, and saves it in the database. Next, it removes the items from the cart by removing the mapping because cart items are now part of the order. Next, it saves the mapping of the order and cart items.

Order creation is done using the native SQL query with the prepared statement.

If you look closely, you'll also find that we have used the official Java 15 feature, text blocks (https://docs.oracle.com/en/java/javase/15/text-blocks/index.html), in it.

Similarly, you can create a repository for all other entities. All entities are available at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter04/src/main/java/com/packt/modern/api/repository.

Now that we have created the repositories, we can move on to adding services.

Adding a Service component
The Service component is an interface that works between controllers and repositories and is where we'll add the business logic. Though you can directly call repositories from controllers, it is not a good practice as repositories should only be part of the data retrieval and persistence functionalities. Service components also help in sourcing data from various sources, such as databases and other external applications.

Service components are marked with the @Service annotation, which is a specialized Spring @Component that allows implemented classes to be auto-detected using class-path scanning. Service classes are used for adding business logic. Like Repository, the Service object also represents both DDD's Service and Java EE's Business Service Façade pattern. Like Repository, it is also a general-purpose stereotype and can be used according to the underlying approach.

First we'll create the service interface, which is a normal Java interface with all the desired method signatures. This interface will expose all the operations that can be performed by CartService:

public interface CartService {

  public List<Item> addCartItemsByCustomerId(String customerId,      @Valid Item item);

  public List<Item> addOrReplaceItemsByCustomerId(String       customerId, @Valid Item item);

  public void deleteCart(String customerId);

  public void deleteItemFromCart(String customerId, String       itemId);

  public CartEntity getCartByCustomerId(String customerId);

  public List<Item> getCartItemsByCustomerId(String       customerId);

  public Item getCartItemsByItemId(String customerId, String       itemId);

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/service/CartService.java

The methods added to CartService are directly mapped to serve each of the APIs defined in the CartController class. Now, we can implement each of the methods in the CartServiceImpl class, which is an implementation of the CartService interface. Each method in CartServiceImpl makes use of a specific Repository object to carry out the operation:
```java
@Service

public class CartServiceImpl implements CartService {

  private CartRepository repository;

  private UserRepository userRepo;

  private ItemService itemService;

  public CartServiceImpl(CartRepository repository,       UserRepository userRepo, ItemService itemService) {

    this.repository = repository;

    this.userRepo = userRepo;

    this.itemService = itemService;

  }

  @Override

  public List<Item> addCartItemsByCustomerId(

                          String customerId, @Valid Item item) {

    CartEntity entity = getCartByCustomerId(customerId);

    long count = entity.getItems().stream().filter(i ->                          i.getProduct().getId().equals(                     UUID.fromString(item.getId()))).count();

    if (count > 0) {

      throw new GenericAlreadyExistsException(

          String.format("Item with Id (%s) already exists.

             You can update it.", item.getId()));

    }

    entity.getItems().add(itemService.toEntity(item));

    return itemService.toModelList(

          repository.save(entity).getItems());

  }

  // rest of the code trimmed for brevity
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/service/CartServiceImpl.java

The CartServiceImpl class is annotated with @Service, therefore would be auto-detected and available for injection. The CartRepository, UserRepository, and ItemService class dependencies are injected using constructor injection.

Let's have a look at one more method implementation of the CartService interface. Check the following code. It adds an item, or updates the price and quantity if the item already exists:
```java
@Override

public List<Item> addOrReplaceItemsByCustomerId(

                         String customerId, @Valid Item item) {

  // 1  

  CartEntity entity = getCartByCustomerId(customerId);

  List<ItemEntity> items =Objects.nonNull(entity.getItems()) ?

                      entity.getItems() : Collections.emptyList();

  AtomicBoolean itemExists = new AtomicBoolean(false);

  // 2

  items.forEach(i -> {

    if (i.getProduct().getId()

            .equals(UUID.fromString(item.getId()))) {

      i.setQuantity(item.getQuantity()).setPrice(i.getPrice());

      itemExists.set(true);

    }

  });

  if (!itemExists.get()) {

      items.add(itemService.toEntity(item));

  }

  // 3

  return itemService.

           toModelList(repository.save(entity).getItems());

}
```
In the preceding code, we are not managing the application state, but are instead writing the sort of business logic that queries the database, sets the entity object, persists the object, and then returns the model class. Let's have a look at the statements one by one:

The method only has customerId as a parameter and there is no Cart parameter. Therefore, first we get CartEntity from the database based on the given customerId.
The program control iterates through the items retrieved from the CartEntity object. If the given item already exists then the quantity and price are changed. Else, it creates a new Item entity from the given Item model and then saves it to the CartEntity object. The itemExists flag is used to find out whether we need to update the existing Item or add a new one.
Finally, the updated CartEntity object is saved in the database. The latest Item entity is retrieved from the database, and then gets converted to a model collection and returned back to the calling program.
Similarly, you can write Service components for others the way you have implemented it for Cart. Before we start enhancing the Controller classes, we need to add a final frontier to our overall feature.

## Implementing hypermedia

We have learned about hypermedia and Hypermedia As The Engine Of Application State (HATEOAS) in Chapter 1, RESTful Web Service Fundamentals. Spring provides state-of-the-art support to HATEOAS using the org.springframework.boot:spring-boot-starter-hateoas dependency.

First of all, we need to make sure that all models returned as part of the API response contain the link field. There are different ways to associate links (that is, the org.springframework.hateoas.Link class) with models, either manually or via auto-generation. Spring HATEOAS's links and its attributes are implemented according to RFC 8288 (https://tools.ietf.org/html/rfc8288). For example, you can create a self-link manually as follows:
 
```java
import static org.springframework.hateoas.server.mvc.      WebMvcLinkBuilder.linkTo;

import static org.springframework.hateoas.server.mvc.      WebMvcLinkBuilder.methodOn;

// other code blocks…

responseModel.setSelf(linkTo(methodOn(CartController.class)

    .getItemsByUserId(userId,item)).withSelfRel())
```
Here, responseModel is a model object that is returned by the API. It has a field called _self that is set using the linkTo and methodOn static methods. The linkTo and methodOn methods are provided by the Spring HATEOAS library and allow us to generate a self-link for a given controller method.

This can also be done automatically by using Spring HATEOAS's RepresentationModelAssembler interface. This interface mainly exposes two methods – toModel(T model) and toCollectionModel(Iterable<? extends T> entities) – that convert the given entity/entities to Model and CollectionModel respectively.

Spring HATEOAS provides the following classes to enrich the user-defined models with hypermedia. It basically provides a class that contains links and methods to add those to the model:

RepresentationModel: Models/DTOs can extend this to collect the links.
EntityModel: This extends RepresentationModel and wraps the domain object (that is, the model) inside it with the content private field. Therefore, it contains the domain model/DTO and the links.
CollectionModel: CollectionModel also extends RepresentationModel. It wraps the collection of models and provides a way to maintain and store the links.
PageModel: PageModel extends CollectionModel and provides ways to iterate through the pages, such as getNextLink() and getPreviousLink(), and through page metadata with getTotalPages(), among others.
The default way to work with Spring HATEOAS is to extend RepresentationModel with domain models as shown in the following snippet:

```java
public class Cart extends RepresentationModel<Cart>  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("customerId")

  @JacksonXmlProperty(localName = "customerId")

  private String customerId;

  @JsonProperty("items")

  @JacksonXmlProperty(localName = "items")

  @Valid

  private List<Item> items = null;
```
Extending RepresentationModel enhances the model with additional methods including getLink(), hasLink(), and add().

You know that all these models are being generated by the Swagger Codegen, therefore we need to configure the Swagger Codegen to generate new models that support hypermedia. This can be done by configuring the Swagger Codegen using the following config.json file:

```json
{

  // …

  "apiPackage": "com.packt.modern.api",

  "invokerPackage": "com.packt.modern.api",

  "serializableModel": true,

  "useTags": true,

  "useGzipFeature" : true,

  "hateoas": true,

  "withXml": true,

  // …

}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/resources/api/config.json

Adding the hateoas property and setting it to true would automatically generate models that would extend the RepresentationModel class.

We are halfway there to implement the API business logic. Now, we need to make sure that links will be populated with the appropriate URL automatically. For that purpose, we'll extend the RepresentationModelAssemblerSupport abstract class that internally implements RepresentationModelAssembler. Let's write the assembler for Cart as shown in the following code block:

```java
@Component

public class CartRepresentationModelAssembler extends     RepresentationModelAssemblerSupport<CartEntity, Cart> {

  private ItemService itemService;

  public CartRepresentationModelAssembler(ItemService       itemService) {

    super(CartsController.class, Cart.class);

    this.itemService = itemService;

  }

  @Override

  public Cart toModel(CartEntity entity) {

    String uid = Objects.nonNull(entity.getUser()) ?                      entity.getUser().getId().toString() :                       null;

    String cid = Objects.nonNull(entity.getId()) ?                       entity.getId().toString() : null;

    Cart resource = new Cart();

    BeanUtils.copyProperties(entity, resource);

    resource.id(cid).customerId(uid)

               .items(itemService.toModelList(entity.                      getItems()));

    resource.add(linkTo(methodOn(CartsController.class)

               .getCartByCustomerId(uid)).withSelfRel());

    resource.add(linkTo(methodOn(CartsController.class)

              .getCartItemsByCustomerId(uid.toString()))

             .withRel("cart-items"));

    return resource;

  }

  public List<Cart> toListModel(Iterable<CartEntity>      entities) {

    if (Objects.isNull(entities)) return Collections.      emptyList();

    return StreamSupport.stream(entities.spliterator(), false)

              .map(e -> toModel(e)).collect(toList());

  }
}
```
Cart 어셈블러에서 중요한 부분은 RepresentationModelAssemblerSupport를 확장하고 toModel() 메서드를 재정의하는 것입니다. 자세히 살펴보면 Cart 모델과 함께 CartController.class도 super() 호출을 사용하여 Rep에 전달되는 것을 볼 수 있습니다. 이를 통해 어셈블러는 앞에서 공유한 methodOn 메서드에 필요한 링크를 적절하게 생성할 수 있습니다. 이런 식으로 링크를 자동으로 생성할 수 있습니다.

다른 리소스 컨트롤러에 대한 추가 링크를 추가해야 할 수도 있습니다. 이것은 RepresentationModelProcessor를 구현하는 빈을 작성하고 다음과 같이 process() 메서드를 재정의하여 달성할 수 있습니다.

```java
@Override
public Order process(Order model) { 
   model.add(Link.of("/payments/{orderId}")
      .withRel(LinkRelation.of("payments"))
      .expand(model.getOrderId()));

  return model;

}
```
You can always refer to https://docs.spring.io/spring-hateoas/docs/current/reference/html/ for more information.

Enhancing the controller with a service and HATEOAS
In Chapter 3, API Specifications and Implementation, we created the Controller class for the Cart API – CartController, which just implements the Swagger Codegen-generated API specification interface – CartApi. It was just a mere block of code without any business logic or data persistence calls.

Now, since we have written the repositories, services, and HATEOAS assemblers, we can enhance the API controller class as shown here:

```java
@RestController
public class CartsController implements CartApi {

  private static final Logger log = LoggerFactory.getLogger(CartsController.class);

  private CartService service;
  private final CartRepresentationModelAssembler assembler;

  public CartsController(CartService service,            CartRepresentationModelAssembler assembler) {
    this.service = service;
    this.assembler = assembler;
  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/controller/CartsController.java

You could see that CartService and CartRepresentationModelAssembler are injected using the constructor. The Spring container injects these dependencies at runtime. Then, these can be used as shown in the following code block:

```java
@Override
public ResponseEntity<Cart> getCartByCustomerId(String     customerId) {

 return ok(
     assembler.toModel(service.getCartByCustomerId        (customerId)));
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/controller/CartsController.java

In the preceding code, you can see that the service retrieves the Cart entity based on customerId (which internally retrieves it from the repository). This Cart entity then gets converted into a model that also contains the hypermedia links made available by Spring HATEOAS's RepresentationModelAssemblerSupport class.

The ok() static method of ResponseEntity is used for wrapping the returned model that also contains the status 200 OK.

This way you can also enhance and implement the other controllers. Now, we can also add an ETag to our API responses.

Adding ETags to API responses
An Entity Tag (ETag) is an HTTP response header that contains a computed hash or equivalent value of the response entity and a minor change in the entity must change its value. HTTP request objects can then contain the If-None-Match and If-Match headers for receiving the conditional responses.

Let's call an API for retrieving the response with an ETag as shown next:
```sh
$ curl -v --location --request GET 'http://localhost:8080/api/v1/products/6d62d909-f957-430e-8689-b5129c0bb75e' –header 'Content-Type: application/json' --header 'Accept: application/json'
```

> Note: Unnecessary use of -X or --request, GET is already inferred.
```
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /api/v1/products/6d62d909-f957-430e-8689-b5129c0bb75e   HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.55.1
> Content-Type: application/json
> Accept: application/json

>

< HTTP/1.1 200

< ETag: "098e97de3b61db55286f5f2812785116f"

< Content-Type: application/json

< Content-Length: 339

<

{

    "_links": {

        "self": {

            "href": "http://localhost:8080/6d62d909-f957-430e-                     8689-b5129c0bb75e"

        }

    },

    "id": "6d62d909-f957-430e-8689-b5129c0bb75e",

    "name": "Antifragile",

    "description": "Antifragile - Things that gains from                     disorder. By Nassim Nicholas Taleb",

    "imageUrl": "/images/Antifragile.jpg",

    "price": 17.1500,

    "count": 33,

    "tag": [

        "psychology",

        "book"

    ]

}
```
Then, you can copy the value from the ETag header to the If-None-Match header and send the same request again with the If-None-Match header:

```sh
$ curl -v --location --request GET 'http://localhost:8080/api/v1/products/6d62d909-f957-430e-8689-b5129c0bb75e' --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'If-None-Match: "098e97de3b61db55286f5f2812785116f"'
```

Note: Unnecessary use of -X or --request, GET is already inferred.

```
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /api/v1/products/6d62d909-f957-430e-8689-b5129c0bb75e HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.55.1
> Content-Type: application/json
> Accept: application/json
> If-None-Match: "098e97de3b61db55286f5f2812785116f"
>
< HTTP/1.1 304
< ETag: "098e97de3b61db55286f5f2812785116f"
```
```
You can see that since there is no change to the entity in the database, and it contains the same entity, it sends a 304 response instead of sending the proper response with 200 OK.

The easiest and simplest way to implement ETags is using Spring's ShallowEtagHeaderFilter as shown here:
```java
@Bean
public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
 return new ShallowEtagHeaderFilter();
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/AppConfig.java

For this implementation, Spring calculates the MD5 hash from the cached content written to the response. Next time, when it receives a request with the If-None-Match header, it again creates the MD5 hash from the cached content written to the response and then compares these two hashes. If both are the same, it sends the 304 NOT MODIFIED response. This way it will save bandwidth but computation will be performed there using the same CPU computation.

We can use the HTTP cache control (org.springframework.http.CacheControl) class and use the version or similar attribute that gets updated for each change, if available, to avoid unecessary CPU computation and for better ETag handling as shown next:

```java
Return ResponseEntity.ok()
       .cacheControl(CacheControl.maxAge(5, TimeUnit.DAYS))
       .eTag(prodcut.getModifiedDateInEpoch())
       .body(product);
```

Adding an ETag to the response also allows UI apps to determine whether a page/object refresh is required, or an event needs to be triggered, especially where data changes frequently in applications such as providing live scores or stock quotes.

## Testing the APIs

Now, you must be looking forward to testing. You can find the Postman (API client) collection at the following location, which is based on Postman Collection version 2.1. You can import it and then test the APIs:

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/Chapter04.postman_collection.json

BUILDING AND RUNNING THE SERVICE

You can build the code by running gradlew clean build from the root of the project, and run the service using java -jar build/libs/Chapter04-0.0.1-SNAPSHOT.jar. Make sure to use Java 15 in the path.

## Summary

In this chapter, we have learned about database migration using Flyway, maintaining and persisting data using repositories, and writing business logic to services. You have also learned how hypermedia can automatically be added to API responses using Spring HATEOAS assemblers. You have now learned about the complete RESTful API development practices, which allows you to use these skill in your day-to-day work involving RESTful API development.

So far we have written synchronous APIs. In the next chapter, you will learn about async APIs and how to implement them using Spring.

## Questions

- Why is the @Repository class used?

- Is it possible to add extra imports or annotations to Swagger-generated classes or models?

- How is ETag useful?

## Further reading

Spring HATEOAS: https://docs.spring.io/spring-hateoas/docs/current/reference/html/

RFC-8288: https://tools.ietf.org/html/rfc8288

A video on Spring HATEOAS: https://subscription.packtpub.com/video/programming/9781788993241/p3/video3_6/using-spring-hateoas

The Postman tool:https://learning.postman.com/docs/getting-started/sending-the-first-request/
