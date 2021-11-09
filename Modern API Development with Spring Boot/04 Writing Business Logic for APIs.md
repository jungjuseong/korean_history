# Chapter 4: Writing Business Logic for APIs

3장에서 OpenAPI를 사용하는 API에 대해 논의했습니다. API 인터페이스 및 모델은 Swagger Codegen에서 생성되었습니다. 이 장에서는 비즈니스 로직과 데이터 영속성 측면에서 API 코드를 구현합니다. 서비스 및 리포지토리를 작성하고 API 응답에 하이퍼미디어 및 eTag도 추가합니다. 

이 장에는 다음 항목이 포함되어 있습니다.

- 서비스 디자인 개요
- 저장소 구성 요소 추가
- 서비스 구성 요소 추가
- 하이퍼미디어 구현
- 서비스와 HATEOAS로 컨트롤러 강화
- API 응답에 ETag 추가


## Technical requirements

You need the following to execute instructions in this chapter:

- The Postman tool (https://learning.postman.com/docs/getting-started/sending-the-first-request/)

You can find the code files for this chapter on GitHub at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter04.

## Overview of the service design

프레젠테이션 계층, 응용 프로그램 계층, 도메인 계층 및 인프라 계층의 4개 계층으로 구성된 다계층 아키텍처를 구현할 것입니다. 다계층 아키텍처는 Domain-Driven Design로 알려진 아키텍처 스타일의 기본 빌딩 블록입니다. 각 레이어에 대해 간략히 살펴보겠습니다.

- **프레젠테이션**: 이 계층은 사용자 인터페이스(UI)를 나타냅니다. "7장 사용자 인터페이스 디자인하기"에서는 전자 상거래 앱용 UI를 개발할 것입니다.

- **애플리케이션**: 애플리케이션 로직을 포함하고 애플리케이션의 전반적인 흐름을 유지하고 조정합니다. 참고로 여기에는 비즈니스 로직이 아닌 애플리케이션 로직만 포함되어 있습니다. 

  RESTful 웹 서비스, 비동기 API, gRPC API 및 GraphQL API는 이 계층의 일부입니다.
애플리케이션 계층의 일부인 "3장 API 사양 및 구현"에서 REST API 인터페이스 및 컨트롤러(REST API 인터페이스 구현)를 이미 다뤘습니다. 이전 장에서 데모 목적으로 컨트롤러를 구현했습니다. 이 장에서는 실제 데이터를 제공하기 위해 컨트롤러를 광범위하게 구현합니다.

- **도메인**: 비즈니스 로직 및 도메인 정보를 포함하는 계층입니다. 여기에는 주문, 제품 등과 같은 비즈니스 개체의 상태가 포함됩니다. 인프라 계층에서 이러한 개체를 읽고 유지하는 역할을 합니다. 도메인 계층도 서비스와 저장소로 구성됩니다. 이 장에서도 이에 대해 다룰 것입니다.

- **인프라**: 데이터베이스, 메시지 브로커, 파일 시스템 등과의 상호 작용과 같은 통신을 담당합니다. Spring Boot는 인프라 계층으로 작동하며 데이터베이스, 메시지 브로커 등과 같은 외부 및 내부 시스템과의 통신 및 상호 작용을 지원합니다.

@Repository 컴포넌트로 도메인 레이어 구현을 시작해보자.


## Repository 컴포넌트 추가

먼저, @Repository 컴포넌트를 사용하여 도메인 계층을 구현합니다. 그 다음에는 서비스를 구현하고 컨트롤러를 개선할 것입니다. @Repository 컴포넌트를 만든 다음 생성자 주입을 사용하여 @Service 구성 요소에서 사용합니다. @Controller 컴포넌트는 @Service 컴포넌트를 사용하여 향상되며 생성자 주입을 사용하여 컨트롤러에도 주입됩니다.

### @Repository
Repository 컴포넌트는 @Repository 주석이 달린 클래스입니다. 이것은 데이터베이스와 상호 작용하는 데 사용되는 특별한 Spring 컴포넌트입니다.

@Repository는 DDD의 Repository와 Java EE 패턴인 DAO를 모두 나타내는 범용 스테레오타입입니다. 개발자와 팀은 기본 접근 방식을 기반으로 Repository 객체를 처리해야 합니다. DDD에서 Repository는 모든 개체에 대한 참조를 전달하고 요청된 개체의 참조를 반환해야 하는 중심 개체입니다. @Repository로 표시된 클래스를 작성하기 전에 필요한 모든 의존성과 구성을 준비해야 합니다.

다음 라이브러리를 데이터베이스 의존성으로 사용합니다.

- H2: H2의 메모리 인스턴스를 사용하지만 파일 기반 인스턴스를 사용할 수도 있습니다.
- Hibernate ORM: DB 객체 매핑용.
- Flyway: DB를 유지 관리하고 롤백, 버전 업그레이드 등을 허용하는 DB 변경 기록을 유지 관리하는 데 도움이 됩니다.

### DB와 JPA 설정

We also need to modify the` application.properties` file with the following configuration:

1. Data source 설정

```yaml
spring.datasource.name=ecomm
spring.datasource.url=jdbc:h2:mem:ecomm;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE;DATABASE_TO_UPPER=false

spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```
We need to add H2-specific properties to the data source. The URL value suggests that a memory-based H2 database instance will be used.

2. H2 database 설정

```
spring.h2.console.enabled=true
spring.h2.console.settings.web-allow-others=false
```

The H2 console is enabled for local access only; it means you can access the H2 console only on localhost. Also, remote access is disabled by setting web-allow-others to false.

3. JPA 설정

```yaml
spring.jpa.properties.hibernate.default_schema=ecomm
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=true
spring.jpa.format_sql=true
spring.jpa.generate-ddl=false
spring.jpa.hibernate.ddl-auto=none
```

We don't want to generate the DDL or to process the SQL file, because we want to use Flyway for database migrations. 

Therefore, generate-ddl is marked with false and ddl-auto is set to none.

4. Flyway 설정

```
spring.flyway.url=jdbc:h2:mem:ecomm
spring.flyway.schemas=ecomm
spring.flyway.user=sa
spring.flyway.password=
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/resources/application.properties

*ACCESSING THE H2 DATABASE*

You can access the H2 database console using /h2-console. For example, if your server is running on localhost and on port 8080 then you can access it using http://localhost:8080/h2-console/.

### DB와 초기 데이터 입력

이제 코드 작성을 시작할 수 있습니다. 먼저 Flyway DB 마이그레이션 스크립트를 추가합니다. 이 스크립트는 SQL로만 작성할 수 있습니다. 이 파일을 src/main/resources 디렉토리 내의 db/migration 디렉토리에 배치합니다. Flyway 명명 규칙(V<version>.<name>.sql)을 따르고 db/migration 디렉토리 안에 V1.0.0.Init.sql 파일을 생성합니다. 

그런 다음 이 파일에 다음 스크립트를 추가할 수 있습니다.

```sql
create schema if not exists ecomm;
-- Other script tags

create TABLE IF NOT EXISTS `ecomm`.`product` (
	id uuid NOT NULL,
	name varchar(56) NOT NULL,
	description varchar(200),
	price numeric(16, 4) DEFAULT 0 NOT NULL,
	count numeric(8, 0),
	image_url varchar(40),
	PRIMARY KEY(id)
);

create TABLE IF NOT EXISTS `ecomm`.`cart` (
   id uuid NOT NULL,
   user_id uuid NOT NULL,
   FOREIGN KEY (user_id) REFERENCES `ecomm`.`user`(id),
   PRIMARY KEY(id)
);

create TABLE IF NOT EXISTS `ecomm`.`item` (
	id uuid NOT NULL,
	product_id uuid NOT NULL,
	quantity numeric(8, 0),
	unit_price numeric(16, 4) NOT NULL,
  FOREIGN KEY(product_id) REFERENCES `ecomm`.`product`(id),
	PRIMARY KEY(id)
);

create TABLE IF NOT EXISTS `ecomm`.`cart_item` (
   cart_id uuid NOT NULL,
   item_id uuid NOT NULL,
   FOREIGN KEY (cart_id) REFERENCES `ecomm`.`cart`(id),
   FOREIGN KEY(item_id) REFERENCES `ecomm`.`item`(id)
);
-- other SQL scripts
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/resources/db/migration/V1.0.0__Init.sql

This script creates the ecomm schema and adds all the tables required for our sample e-commerce app. It also adds insert statements for the seed data.

### 엔티티 추가

이제 엔티티를 추가할 수 있습니다. 엔티티는 Hibernate와 같은 ORM 구현을 사용하여 테이블에 직접 매핑되는 @Entity 주석으로 표시된 특수 객체입니다.

CartEntity.java 파일을 생성해 보겠습니다.

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

여기서 @Entity 주석은 Entity이고 javax.persistence 패키지의 일부입니다. 기본적으로 엔티티 이름을 사용하지만 @Table 주석을 사용하여 데이터베이스 테이블에 매핑합니다.

Cart 엔티티를 User 및 Item에 각각 매핑하기 위해 일대일 및 다대다 주석을 사용하고 있습니다. ItemEntity 목록은 `@JoinTable`과도 연관되어 있습니다. CART_ITEM 조인 테이블을 사용하여 해당 테이블의 CART_ID 및 ITEM_ID 열을 기반으로 장바구니 및 제품 항목을 매핑하기 때문입니다.

UserEntity에는 다음 코드 같이 관계를 유지하기 위해 Cart 엔터티도 추가되었습니다. FetchType은 `LAZY`로 표시됩니다. 즉, 명시적으로 요청할 때만 사용자의 cart가 로드됩니다. 또한 `orphanRemoval=true`로 구성하여 사용자가 참조하지 않는 장바구니를 제거하려고 합니다.

```java
@Entity
@Table(name = "user")
public class UserEntity {
  // other code
  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
  private CartEntity cart;
  // other code…
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/entity/UserEntity.java

All other entities are being added to the entity package located at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/entity.

Now, we can add the repository.

## 리포지토리 추가

All the repository have been added to https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/repository.

리포지토리는 Spring Data JPA 덕분에 CRUD 작업에 가장 간단하게 추가할 수 있습니다. save, saveAll, findById, findAll, findAllById, delete 및 deleteById와 같은 모든 CRUD 작업 구현을 제공하는 CrudRepository와 같은 기본 구현으로 인터페이스를 확장하기만 하면 됩니다.` save(Entity e)` 메소드는 엔티티 생성 및 업데이트 작업 모두에 사용됩니다.

```java
public interface CartRepository extends CrudRepository<CartEntity, UUID> {
    @Query("select c from CartEntity c join c.user u where u.id = :customerId")
    public Optional<CartEntity> findByCustomerId(@Param("customerId") UUID customerId);
}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/repository/CartRepository.java

CartRepository 인터페이스는 CrudRepository 부분을 확장합니다. @Query로 표시된 JPQL에서 지원하는 메서드를 추가할 수도 있습니다. @Query 주석 내부의 쿼리는 JPQL로 작성됩니다. JPQL은 SQL과 매우 유사하지만 여기서는 실제 테이블 이름 대신 데이터베이스 테이블에 매핑된 Java 클래스 이름을 사용했습니다. 따라서 Cart 대신 CartEntity를 테이블 이름으로 사용했습니다.

> **참고**

마찬가지로 속성의 경우 테이블의 필드가 아니라 클래스의 필드 변수 이름을 사용해야 합니다. 어쨌든 테이블 이름이나 필드 이름을 사용하고 실제 테이블에 매핑된 클래스 및 클래스 멤버와 일치하지 않으면 오류가 발생합니다.

JPQL 또는 기본 SQL을 사용하여 나만의 사용자 지정 메서드를 추가하려면 어떻게 해야 합니까?. 먼저 CartRepository와 매우 유사한 OrderRepository를 살펴보겠습니다.

```java
@Repository
public interface OrderRepository extends CrudRepository<OrderEntity, UUID>, OrderRepositoryExt {
    @Query("select o from OrderEntity o join o.userEntity u where u.id = :customerId")
    public Iterable<OrderEntity> findByCustomerId(@Param("customerId") UUID customerId);
}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/repository/OrderRepository.java

If you look closely, we have extended an extra interface – OrderRepositoryExt. This is our extra interface for the Order repository and consists of the following code:
```java
public interface OrderRepositoryExt {
  Optional<OrderEntity> insert(NewOrder m);
}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/repository/OrderRepositoryExt.java

이미 CrudRepository에 `save()` 메소드가 있지만 다른 구현을 사용하려고 합니다. 이를 위해 그리고 고유한 저장소 메서드 구현을 만드는 방법을 보여주기 위해 이 추가 저장소 인터페이스를 추가합니다.

다음과 같이 OrderRepositoryExt 인터페이스 구현을 생성해 보겠습니다.

```java
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
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/repository/OrderRepositoryImpl.java

이런 식으로 JPQL/HQL 또는 기본 SQL에서 자체 구현을 가질 수도 있습니다. 여기에서 @Repository 주석은 이 특수 컴포넌트가 저장소이고 기본 JPA를 사용하여 데이터베이스와 상호 작용하는 데 사용해야 함을 Spring 컨테이너에 알려줍니다.

또한 @Transactional은 이 클래스의 메소드에 의해 수행되는 트랜잭션이 Spring에 의해 관리됨을 의미하는 특수 주석입니다. 커밋 및 롤백을 추가하는 모든 수동 작업을 제거합니다. 이 주석을 클래스 내의 특정 메서드에 추가할 수도 있습니다.

또한 EntityManager 클래스에 @PersistenceContext를 사용하여 다음 코드와 같이 수동으로 쿼리를 만들고 실행할 수 있습니다.

```java
@Override
public Optional<OrderEntity> insert(NewOrder m) {
  // Items are already in cart and saved in db when user places
  // order
  // Here you can also populate other Order details like address
  // etc.

  Iterable<ItemEntity> dbItems = itemRepo.findByCustomerId(m.getCustomerId());
  List<ItemEntity> items = StreamSupport.stream(dbItems.spliterator(), false).collect(toList());

  if (items.size() < 1) {
    throw new ResourceNotFoundException(String.format("There is no item found in customer's (ID: %s) cart.", m.getCustomerId()));
  }

  BigDecimal total = BigDecimal.ZERO;
  for (ItemEntity i : items) {
    total = (BigDecimal.valueOf(i.getQuantity()).multiply(i.getPrice())).add(total);
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

  Optional<CartEntity> oCart = cRepo.findByCustomerId(UUID.fromString(m.getCustomerId()));

  CartEntity cart = oCart.orElseThrow(() -> new ResourceNotFoundException(String.format("Cart not found for given customer (ID: %s)", m.getCustomerId())));

  itemRepo.deleteCartItemJoinById(cart.getItems().stream().map(i -> i.getId()).collect(toList()), cart.getId());

  OrderEntity entity = (OrderEntity) em.createNativeQuery("""
    SELECT o.* FROM ecomm.orders o WHERE o.customer_id = ? AND
    o.order_date >= ?
    """, OrderEntity.class)
    .setParameter(1, m.getCustomerId())
    .setParameter(2, OffsetDateTime.ofInstant(orderDate.toInstant(),
       ZoneId.of("Z")).truncatedTo(ChronoUnit.MICROS))
    .getSingleResult();

  oiRepo.saveAll(cart.getItems().stream()
       .map(i -> new OrderItemEntity().setOrderId(entity.getId())
         .setItemId(i.getId())).collect(toList()));

  return Optional.of(entity);
}
```
이 방법은 기본적으로 고객의 장바구니에 있는 항목을 먼저 가져옵니다. 그런 다음 주문 합계를 계산하고 새 주문을 생성하여 데이터베이스에 저장합니다. 그런 다음 카트 항목이 이제 주문의 일부이므로 매핑을 제거하여 카트에서 항목을 제거합니다. 다음으로 주문 및 장바구니 항목의 매핑을 저장합니다.

주문 생성은 준비된 명령문과 함께 기본 SQL 쿼리를 사용하여 수행됩니다.

If you look closely, you'll also find that we have used the official Java 15 feature, text blocks (https://docs.oracle.com/en/java/javase/15/text-blocks/index.html), in it.

Similarly, you can create a repository for all other entities. All entities are available at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter04/src/main/java/com/packt/modern/api/repository.

Now that we have created the repositories, we can move on to adding services.

## 서비스 컴포넌트 추가

서비스 컴포넌트는 컨트롤러와 리포지토리 사이에서 작동하는 인터페이스이며 여기에서 비즈니스 로직을 추가할 것입니다. 컨트롤러에서 리포지토리를 직접 호출할 수 있지만 리포지토리는 데이터 검색 및 지속성 기능의 일부여야 하므로 좋은 방법이 아닙니다. 서비스 구성 요소는 또한 데이터베이스 및 기타 외부 응용 프로그램과 같은 다양한 소스에서 데이터를 소싱하는 데 도움이 됩니다.

서비스 구성 요소는 구현된 클래스가 클래스 경로 스캐닝을 사용하여 자동 감지되도록 하는  @Service 주석으로 표시합니다. 서비스 클래스는 비즈니스 로직을 추가하는 데 사용됩니다. Repository와 마찬가지로 Service 객체도 DDD의 Service와 Java EE의 Business Service Façade 패턴을 모두 나타냅니다. Repository와 마찬가지로 범용 고정 관념이며 기본 접근 방식에 따라 사용할 수 있습니다.

먼저 원하는 모든 메소드 서명이 있는 일반 Java 인터페이스인 서비스 인터페이스를 작성합니다. 이 인터페이스는 CartService에서 수행할 수 있는 모든 작업을 표시합니다.

```java
public interface CartService {

  public List<Item> addCartItemsByCustomerId(String customerId, @Valid Item item);

  public List<Item> addOrReplaceItemsByCustomerId(String customerId, @Valid Item item);

  public void deleteCart(String customerId);

  public void deleteItemFromCart(String customerId, String itemId);

  public CartEntity getCartByCustomerId(String customerId);

  public List<Item> getCartItemsByCustomerId(String customerId);

  public Item getCartItemsByItemId(String customerId, String itemId);

}
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/service/CartService.java

CartService에 추가된 메소드는 CartController 클래스에 정의된 각 API를 제공하기 위해 직접 매핑됩니다. 이제 CartService 인터페이스의 구현인 CartServiceImpl 클래스에서 각 메서드를 구현할 수 있습니다. CartServiceImpl의 각 메소드는 특정 Repository 객체를 사용하여 작업을 수행합니다.

```java
@Service
public class CartServiceImpl implements CartService {
  private CartRepository repository;
  private UserRepository userRepo;
  private ItemService itemService;

  public CartServiceImpl(CartRepository repository, UserRepository userRepo, ItemService itemService) {
      this.repository = repository;
      this.userRepo = userRepo;
      this.itemService = itemService;
  }

  @Override
  public List<Item> addCartItemsByCustomerId(String customerId, @Valid Item item) {

    CartEntity entity = getCartByCustomerId(customerId);
    long count = entity.getItems().stream().filter(i ->i.getProduct().getId().equals(UUID.fromString(item.getId()))).count();

    if (count > 0) {
      throw new GenericAlreadyExistsException(
          String.format("Item with Id (%s) already exists. You can update it.", item.getId()));
    }
    entity.getItems().add(itemService.toEntity(item));
    return itemService.toModelList(repository.save(entity).getItems());
  }

  // rest of the code trimmed for brevity
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/service/CartServiceImpl.java

CartServiceImpl 클래스는 @Service로 주석 처리되므로 자동 감지되어 주입에 사용할 수 있습니다. CartRepository, UserRepository 및 ItemService 클래스 의존성은 생성자 주입을 사용하여 주입됩니다.

CartService 인터페이스의 메소드 구현을 하나 더 살펴보겠습니다. 다음 코드를 확인하십시오. 항목을 추가하거나 항목이 이미 있는 경우 가격과 수량을 업데이트합니다.

```java
@Override

public List<Item> addOrReplaceItemsByCustomerId(String customerId, @Valid Item item) {
  // 1  
  CartEntity entity = getCartByCustomerId(customerId);
  List<ItemEntity> items =Objects.nonNull(entity.getItems()) ? entity.getItems() : Collections.emptyList();

  AtomicBoolean itemExists = new AtomicBoolean(false);

  // 2
  items.forEach(i -> {
    if (i.getProduct().getId().equals(UUID.fromString(item.getId()))) {
      i.setQuantity(item.getQuantity()).setPrice(i.getPrice());
      itemExists.set(true);
    }
  });
  if (!itemExists.get()) {
      items.add(itemService.toEntity(item));
  }
  // 3
  return itemService.toModelList(repository.save(entity).getItems());
}
```

앞의 코드에서 우리는 애플리케이션 상태를 관리하지 않고 대신 DB를 쿼리하고 엔터티 개체를 설정하고 개체를 유지한 다음 모델 클래스를 반환하는 일종의 비즈니스 로직을 작성하고 있습니다. 문장을 하나씩 살펴보겠습니다.

1. 이 메소드는 매개변수로 customerId만 갖고 cart 매개변수는 없습니다. 따라서 먼저 주어진 customerId를 기반으로 DB에서 CartEntity를 가져옵니다.

2. 프로그램 컨트롤은 CartEntity 개체에서 검색된 항목을 반복합니다. 주어진 항목이 이미 존재하는 경우 수량과 가격이 변경됩니다. 그렇지 않으면 지정된 항목 모델에서 새 항목 엔터티를 만든 다음 CartEntity 개체에 저장합니다. itemExists 플래그는 기존 항목을 업데이트하거나 새 항목을 추가해야 하는지 여부를 확인하는 데 사용됩니다.

3. 마지막으로 업데이트된 CartEntity 개체가 DB에 저장됩니다. 최신 항목 엔터티는 DB에서 검색된 다음 모델 컬렉션으로 변환되어 호출 프로그램으로 다시 반환됩니다.

마찬가지로 장바구니에 구현한 방식으로 다른 사용자를 위한 서비스 구성 요소를 작성할 수 있습니다. 컨트롤러 클래스 향상을 시작하기 전에 전체 기능에 최종 경계를 추가해야 합니다.



## 하이퍼미디어 구현

1장 RESTful 웹 서비스 기본 사항에서 하이퍼미디어와 HATEOAS(Hypermedia As Engine Of Application State)에 대해 배웠습니다. Spring은 org.springframework.boot:spring-boot-starter-hateoas 의존성을 사용하여 HATEOAS에 대한 최첨단 지원을 제공합니다.

먼저 API 응답의 일부로 반환된 모든 모델에 링크 필드가 포함되어 있는지 확인해야 합니다. 수동으로 또는 자동 생성을 통해 링크(즉, org.springframework.hateoas.Link 클래스)를 모델과 연결하는 다양한 방법이 있습니다. Spring HATEOAS의 링크 및 속성은 RFC 8288(https://tools.ietf.org/html/rfc8288)에 따라 구현됩니다. 예를 들어 다음과 같이 수동으로 자체 링크를 만들 수 있습니다.

```java
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

// other code blocks…

responseModel.setSelf(linkTo(methodOn(CartController.class)
    .getItemsByUserId(userId,item)).withSelfRel())
```
여기서 responseModel은 API에서 반환하는 모델 객체입니다. linkTo 및 methodOn 정적 메서드를 사용하여 설정되는` _self`라는 필드가 있습니다. `linkTo` 및 `methodOn` 메소드는 Spring HATEOAS 라이브러리에 의해 제공되며 주어진 컨트롤러 메소드에 대한 자체 링크를 생성할 수 있도록 합니다.

이것은 Spring HATEOAS의 `RepresentationModelAssembler` 인터페이스를 사용하여 자동으로 수행될 수도 있습니다. 이 인터페이스는 주로 주어진 엔티티를 Model 및 CollectionModel로 변환하는 toModel(T 모델) 및 toCollectionModel(Iterable<? extends T> 엔티티)의 두 가지 메소드를 노출합니다.

Spring HATEOAS는 하이퍼미디어로 사용자 정의 모델을 강화하기 위해 다음 클래스를 제공합니다. 기본적으로 모델에 추가하는 링크와 메서드가 포함된 클래스를 제공합니다.

- RepresentationModel: 모델/DTO는 이를 확장하여 링크를 수집할 수 있습니다.

- EntityModel: RepresentationModel을 확장하고 그 안에 있는 도메인 개체(즉, 모델)를 콘텐츠 개인 필드로 래핑합니다. 따라서 도메인 모델/DTO 및 링크가 포함됩니다.

- CollectionModel: CollectionModel은 RepresentationModel도 확장합니다. 모델 컬렉션을 래핑하고 링크를 유지 관리하고 저장하는 방법을 제공합니다.

- PageModel: PageModel은 CollectionModel을 확장하고 getNextLink() 및 getPreviousLink()와 같은 페이지와 getTotalPages()를 사용하여 페이지 메타데이터를 통해 반복하는 방법을 제공합니다.

Spring HATEOAS로 작업하는 기본 방법은 다음과 같이 RepresentationModel을 도메인 모델로 확장하는 것입니다.

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
RepresentationModel 확장은 getLink(), hasLink() 및 add()를 포함한 추가 메서드로 모델을 향상시킵니다.

이 모든 모델이 Swagger Codegen에 의해 생성된다는 것을 알고 있으므로 하이퍼미디어를 지원하는 새 모델을 생성하도록 Swagger Codegen을 구성해야 합니다. 이것은 다음 config.json 파일을 사용하여 Swagger Codegen을 구성하여 수행할 수 있습니다.

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

hatoas 속성을 추가하고 true로 설정하면 RepresentationModel 클래스를 확장하는 모델이 자동으로 생성됩니다.

API 비즈니스 로직을 구현하기 위해 절반 정도 남았습니다. 이제 링크가 적절한 URL로 자동으로 채워지도록 해야 합니다. 이를 위해 RepresentationModelAssembler를 내부적으로 구현하는 RepresentationModelAssemblerSupport 추상 클래스를 확장합니다. 다음 코드 블록과 같이 Cart용 어셈블러를 작성해 보겠습니다.

```java
@Component

public class CartRepresentationModelAssembler extends     RepresentationModelAssemblerSupport<CartEntity, Cart> {
  private ItemService itemService;
  public CartRepresentationModelAssembler(ItemService itemService) {
    super(CartsController.class, Cart.class);
    this.itemService = itemService;
  }

  @Override
  public Cart toModel(CartEntity entity) {
    String uid = Objects.nonNull(entity.getUser()) ? entity.getUser().getId().toString() : null;
    String cid = Objects.nonNull(entity.getId()) ? entity.getId().toString() : null;

    Cart resource = new Cart();
    BeanUtils.copyProperties(entity, resource);
    resource.id(cid).customerId(uid).items(itemService.toModelList(entity.getItems()));

    resource.add(linkTo(methodOn(CartsController.class).getCartByCustomerId(uid)).withSelfRel());

    resource.add(linkTo(methodOn(CartsController.class)
      .getCartItemsByCustomerId(uid.toString()))
      .withRel("cart-items"));

    return resource;
  }

  public List<Cart> toListModel(Iterable<CartEntity>entities) {
    if (Objects.isNull(entities)) return Collections.emptyList();

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

### 서비스 및 HATEOAS로 컨트롤러 향상

3장, API 사양 및 구현에서는 Swagger Codegen 생성 API 사양 인터페이스인 CartApi를 구현하는 Cart API용 Controller 클래스인 CartController를 만들었습니다. 비즈니스 로직이나 데이터 지속성 호출이 없는 단순한 코드 블록이었습니다.

이제 리포지토리, 서비스 및 HATEOAS 어셈블러를 작성했으므로 다음과 같이 API 컨트롤러 클래스를 향상할 수 있습니다.

```java
@RestController
public class CartsController implements CartApi {
  private static final Logger log = LoggerFactory.getLogger(CartsController.class);
  private CartService service;
  private final CartRepresentationModelAssembler assembler;

  public CartsController(CartService service, CartRepresentationModelAssembler assembler) {
    this.service = service;
    this.assembler = assembler;
  }
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/controller/CartsController.java

You could see that CartService and CartRepresentationModelAssembler are injected using the constructor. The Spring container injects these dependencies at runtime. Then, these can be used as shown in the following code block:

```java
@Override
public ResponseEntity<Cart> getCartByCustomerId(String customerId) {

 return ok(
     assembler.toModel(service.getCartByCustomerId(customerId)));
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/controller/CartsController.java

앞의 코드에서 서비스가 customerId(리포지토리에서 내부적으로 검색)를 기반으로 Cart 엔터티를 검색하는 것을 볼 수 있습니다. 그런 다음 이 Cart 엔터티는 Spring HATEOAS의 RepresentationModelAssemblerSupport 클래스에서 사용할 수 있는 하이퍼미디어 링크도 포함하는 모델로 변환됩니다.

ResponseEntity의 ok() 정적 메서드는 상태 200 OK도 포함하는 반환된 모델을 래핑하는 데 사용됩니다. 이 방법으로 다른 컨트롤러도 향상하고 구현할 수 있습니다. 이제 API 응답에 ETag를 추가할 수도 있습니다.

### API 응답에 ETag 추가
엔터티 태그(ETag)는 응답 엔터티의 계산된 해시 또는 이에 상응하는 값을 포함하는 HTTP 응답 헤더이며 엔터티의 사소한 변경은 해당 값을 변경해야 합니다. HTTP 요청 객체는 조건부 응답을 수신하기 위한 If-None-Match 및 If-Match 헤더를 포함할 수 있습니다.

다음과 같이 ETag를 사용하여 응답을 검색하기 위한 API를 호출해 보겠습니다.

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
    "description": "Antifragile - Things that gains from disorder. By Nassim Nicholas Taleb",
    "imageUrl": "/images/Antifragile.jpg",
    "price": 17.1500,
    "count": 33,
    "tag": [
        "psychology",
        "book"
    ]
}
```
Then, you can copy the value from the ETag header to the `If-None-Match` header and send the same request again with the `If-None-Match` header:

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

You can see that since there is no change to the entity in the database, and it contains the same entity, it sends a 304 response instead of sending the proper response with 200 OK.

The easiest and simplest way to implement ETags is using Spring's ShallowEtagHeaderFilter as shown here:

```java
@Bean
public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
 return new ShallowEtagHeaderFilter();
```
https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/src/main/java/com/packt/modern/api/AppConfig.java

이 구현을 위해 Spring은 응답에 기록된 캐시된 콘텐츠에서 MD5 해시를 계산합니다. 다음에 If-None-Match 헤더가 있는 요청을 수신하면 응답에 기록된 캐시된 콘텐츠에서 MD5 해시를 다시 생성한 다음 이 두 해시를 비교합니다. 둘 다 같으면 304 NOT MODIFIED 응답을 보냅니다. 이렇게하면 대역폭이 절약되지만 동일한 CPU 계산을 사용하여 계산이 수행됩니다.

HTTP 캐시 제어(org.springframework.http.CacheControl) 클래스를 사용하고, 사용 가능한 경우 각 변경 사항에 대해 업데이트되는 버전 또는 유사한 속성을 사용하여 다음과 같이 불필요한 CPU 계산을 피하고 ETag 처리를 개선할 수 있습니다.

```java
Return ResponseEntity.ok()
       .cacheControl(CacheControl.maxAge(5, TimeUnit.DAYS))
       .eTag(prodcut.getModifiedDateInEpoch())
       .body(product);
```

응답에 ETag를 추가하면 UI 앱이 페이지/객체 새로 고침이 필요한지 또는 이벤트를 트리거해야 하는지 여부를 결정할 수 있습니다. 특히 라이브 점수 또는 주식 시세 제공과 같이 애플리케이션에서 데이터가 자주 변경되는 경우에 그렇습니다.

## Testing the APIs

Now, you must be looking forward to testing. You can find the Postman (API client) collection at the following location, which is based on Postman Collection version 2.1. You can import it and then test the APIs:

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter04/Chapter04.postman_collection.json

*BUILDING AND RUNNING THE SERVICE*

You can build the code by running gradlew clean build from the root of the project, and run the service using java -jar build/libs/Chapter04-0.0.1-SNAPSHOT.jar. Make sure to use Java 15 in the path.

## 요약

이 장에서는 Flyway를 사용한 데이터베이스 마이그레이션, 리포지토리를 사용한 데이터 유지 및 유지, 서비스에 비즈니스 로직 작성에 대해 배웠습니다. 또한 Spring HATEOAS 어셈블러를 사용하여 API 응답에 하이퍼미디어를 자동으로 추가하는 방법을 배웠습니다. 이제 RESTful API 개발과 관련된 일상적인 작업에서 이러한 기술을 사용할 수 있는 완전한 RESTful API 개발 사례에 대해 배웠습니다.

지금까지 동기 API를 작성했습니다. 다음 장에서는 비동기 API와 이를 Spring을 사용하여 구현하는 방법에 대해 학습합니다.

## 질문

- @Repository 클래스를 사용하는 이유는 무엇입니까?
- Swagger 생성 클래스 또는 모델에 추가 가져오기 또는 주석을 추가할 수 있습니까?
- ETag는 어떻게 유용합니까?

## Further reading

Spring HATEOAS: https://docs.spring.io/spring-hateoas/docs/current/reference/html/

RFC-8288: https://tools.ietf.org/html/rfc8288

A video on Spring HATEOAS: https://subscription.packtpub.com/video/programming/9781788993241/p3/video3_6/using-spring-hateoas

The Postman tool:https://learning.postman.com/docs/getting-started/sending-the-first-request/
