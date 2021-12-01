
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

Workflow and tooling for GraphQL
As a per-data graph way of thinking in GraphQL, data is exposed using an API consisting of graphs of objects. These objects are connected using relations. GraphQL only exposes a single API endpoint. Clients query this endpoint that uses a single data graph. On top of that, the data graph may resolve data from a single source, or multiple sources, by following the OneGraph principle of GraphQL. These sources can be a database, legacy system, or services that expose data using REST/gRPC/SOAP.

The GraphQL server can be implemented in the following two ways:

Standalone GraphQL service: A standalone GraphQL service contains a single data graph. It could be a monolithic app or microservice architecture that fetches the data from single or multiple sources (having no GraphQL API).
Federated GraphQL services: It's very easy to query a single data graph for comprehensive data fetching. However, enterprise applications are made using multiple services and hence you can't have a single data graph unless you build a monolithic system. If you don't build a monolithic system, then you would have multiple service-specific data graphs.
This is where you make use of federated GraphQL services. A federated GraphQL service contains a single distributed graph exposed using a gateway. Clients would call the gateway, which is an entry point to the system. The data graph would be distributed among multiple services and each service can maintain its own development and release cycle independently. Having said that, federated GraphQL services would still follow the OneGraph principle. Therefore, the client would query the single endpoint for fetching any part of the graph.

Let's assume that a sample e-commerce app is developed using GraphQL federated services. It would have products, orders, shipping, inventory, customers, and other services that would expose their domain-specific data graphs using the GraphQL API.

Let's draw a high-level diagram of GraphQL federated e-commerce services as follows:

Figure 14.1 – Federated GraphQL services
Figure 14.1 – Federated GraphQL services

Let's say the GraphQL client queries for a list of most ordered products with the least inventory by calling the Gateway endpoint. This query may have fields from Orders, Products, and Inventory. Each service is responsible for resolving only the respective part of a data graph. Orders would resolve order-related data, Products would resolve product-related data, Inventory would resolve inventory-related data, and so on. Gateway then consolidates the graph data and sends it back to the clients.

The graphql-java library (https://www.graphql-java.com/) provides the Java implementation of the GraphQL specification. Its source code is available at https://github.com/graphql-java/graphql-java.

There are many Spring Boot Starter projects for GraphQL, for example, at https://github.com/graphql-java-kickstart/graphql-spring-boot. However, we are going to use Netflix's Domain Graph Service (DGS) framework (https://netflix.github.io/dgs/). Netflix's DGS provides not only the GraphQL Spring Boot Starter but also the full set of tools and libraries that you need to develop production-ready GraphQL services. It is built on top of Spring Boot and uses the graphql-java library.

Netflix open-sourced the DGS framework after using it in production in February 2021. It is continuously being enhanced and supported by the community. Netflix uses the same open-sourced code based on their production, which gives the assurance of the code's quality and future maintenance.

It provides the following features:

Provides a Spring Boot Starter and integration with Spring Security
Gradle plugin for code generation from a GraphQL schema
Support interfaces and union types, plus provides custom scalar types
Supports GraphQL subscriptions using WebSocket and server-sent events
Error handling
Pluggable instrumentation
GraphQL federated services by easy integration with GraphQL Federation
File upload
GraphQL Java client
GraphQL test framework
Full WebFlux support could be available in the future. The release candidate build was available at the time of writing this chapter.

Let's write a GraphQL server using Netflix's DGS framework in the next section.

Implementation of the GraphQL server
You are going to develop a standalone GraphQL server in this chapter. The knowledge you acquire while developing the standalone GraphQL server can be used to implement federated GraphQL services.

Let's create the Gradle project first in the next subsection.

Creating the gRPC server project
Either you can use the Chapter 14 code from a cloned Git repository (https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot), or you can start by creating a new Spring project from scratch using Spring Initializr (https://start.spring.io/) for the server and client with the following options (you will create the gRPC api library project separately):

Project: Gradle Project
Language: Java
Spring Boot: 2.4.4 (the preferred version is 2.4+; if not available, you can later modify it manually in the build.gradle file)
Project Metadata:
Group: com.packt.modern.api

Artifact: chapter14

Name: chapter14

Description: Chapter 14 code of book Modern API Development with Spring and Spring Boot

Package name: com.packt.modern.api

Packaging: JAR
Java: 11 (you can change it to another version such as 15/16/17 in the build.gradle file later as shown in the following code block):
// update following build.gradle file

sourceCompatibility = JavaVersion.VERSION_15

// or for Java 16

// sourceCompatibility = JavaVersion.VERSION_16

// or for Java 17

// sourceCompatibility = JavaVersion.VERSION_17

Dependencies: org.springframework.boot:spring-boot-starter-web
Then, you can click on the GENERATE button and download the project. The downloaded project will be used for creating the GraphQL server.

Next, let's add the GraphQL DGS dependencies to the newly created project.

Adding the GraphQL DGS dependencies
Once the Gradle project is available, you can modify the build.gradle file to include the GDS dependencies and plugin as shown in the following code:

plugins {

  id 'org.springframework.boot' version '2.4.4'

  id 'io.spring.dependency-management' version

    '1.0.11.RELEASE'

  id 'java'

  id 'com.netflix.dgs.codegen' version '4.6.4'

}

// other part removed from brevity

def dgsVersion = '3.12.1'

dependencies {

  implementation platform("com.netflix.graphql.dgs:

      graphql-dgs-platform-dependencies:${dgsVersion}")

  implementation 'com.netflix.graphql.dgs:graphql-dgs-

      spring-boot-starter'

  implementation 'com.netflix.graphql.dgs:graphql-dgs-

      extended-scalars'

  implementation 'org.springframework.boot:spring-boot-

      starter-web'

  testImplementation 'org.springframework.boot:spring-boot-

      starter-test'

  implementation 'com.github.javafaker:javafaker:1.0.2'

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/build.gradle

Here, the DGS Codegen plugin is added, which will generate the code from the GraphQL schema file. Next, the following three dependencies have been added:

graphql-dgs- platform-dependencies: The DGS platform dependencies for the DGS bill of material (BOM)
graphql-dgs-spring-boot-starter: The DGS Spring Boot Starter library for DGS Spring support
graphql-dgs-extended-scalars: The DGS extended scalars library for custom scalar types
Please note that the javafaker library is being used here to generate the domain seed data.

Next, let's configure the DGS Codegen plugin in the build.gradle file as shown in the next code block:

generateJava {

    packageName = "com.packt.modern.api.generated"

    generateClient = true

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/build.gradle

You have configured the following two properties of DGS Codegen using the generateJava task, which uses the com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask class:

packageName: The Java package name of the generated Java classes
generateClient: Whether you would like to generate the client or not
The DGS Codegen plugin picks GraphQL schema files from the src/main/resources/schema folder directory by default. However, you can modify it using the schemaPaths property, which accepts an array. You can add this property in the previous code of generateTask along with packageName and generateClient if you want to change the default schema location, as shown next:

schemaPaths = ["${projectDir}/src/main/resources/schema"]

You can also configure type mappings as you did for the org.hidetake.swagger.generator Gradle plugin while generating the Java code from OpenAPI specs in step 4 of the Convert OpenAPI spec to Spring code section in Chapter 3, API Specifications and Implementation. For adding a custom type mapping, you can add the typeMapping property to the plugin task as shown next:

typeMapping = ["GraphQLType": "mypackage.JavaType"]

This property accepts an array; you can add one or more type mappings here. You can refer to the plugin documentation at https://netflix.github.io/dgs/generating-code-from-schema/ for more information.

Let's add the GraphQL schema next.

Adding the GraphQL schema
Netflix's DGS supports both the code-first and design-first approaches. However, you are going to use the design-first approach in this chapter as we have done throughout this book. Therefore, first you'll design the schema using the GraphQL schema language and then use the generated code to implement the GraphQL APIs.

We are going to keep the domain objects minimal to reduce the complexity of business logic and keep the focus on the GraphQL server implementation. Therefore, you'll have just two domain objects – Product and Tag. The GraphQL schema allows the following operation using its endpoint as shown in the following schema file:

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/resources/schema/schema.graphqls

You need to add the schema.graphqls GraphQL schema file at the src/main/resources/schema location. You can have multiple schema files there to create the schema module-wise.

Here, the following root types have been exposed:

Query: The product and products queries for fetching a product by its ID, and a collection of products matched by the given criteria.
Mutation: The addTag mutation would add a tag to the product matched by the given ID. Another mutation, addQuantity, would increase the product quantities. The addQuantity mutation would also be used as an event that would trigger the subscription publication.
Subscription: The quantityChanged subscription would publish the product where the quantity has been updated. The event quantity change would be captured through the addQuantity mutation.
Let's add the object types and input types being used in these root types as shown in the next code block:

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/resources/schema/schema.graphqls

These are straightforward object and input types. All fields of the ProductCriteria input type have been kept optional.

You have also used a BigDecimal custom scalar type. Therefore, we need to first declare it in the schema. You can do that by adding BigDecimal to the end of the schema file, as shown next:

scalar BigDecimal

Next, you also need to map it to java.math.BigDecimal in the code generator plugin. Let's add it to the build.gradle file as shown next (check the highlighted line):

generateJava {

  generateClient = true

  packageName = "com.packt.modern.api.generated"

  typeMapping = ["BigDecimal": "java.math.BigDecimal"]

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/build.gradle

After these changes, your project is ready to generate the GraphQL objects and client. You can run the following command from the project root directory to build the project:

gradlew clean build

This command would generate the Java classes in the build/generated directory.

Before you start implementing the GraphQL root types, let's discuss the custom scalar types in the next subsection.

Adding custom scalar types
You are going to use BigDecimal for capturing the monetary values. This is a custom scalar type, therefore you need to add this custom scalar to the code so that the DGS framework can pick it for serialization and deserialization. (This has to be done apart from adding a mapping in the Gradle code generator plugin.)

Create a new Java file called BigDecimalScaler.java and add the following code to it:

@DgsScalar(name = "BigDecimal")

public class BigDecimalScalar extends

    GraphqlBigDecimalCoercing {

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/scalar/BigDecimalScalar.java

Here, class is marked with the @DgsScalar annotation, which registers this class as a custom scalar with the DGS framework. Ideally, you should implement the graphql.schema.Coercing interface (part of the graphql-java library). This interface lets you implement serialization and parsing for custom scalar types.

However, since the BigDecimal default implementation (GraphqlBigDecimalCoercing) is already there in graphql-java, we'll simply extend it.

The DGS framework also provides custom scalars such as DateTime. These custom scalars can also be added to the DGS framework. The DateTime custom scalar implementation is available at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/scalar/DateTimeScalar.java, which can be used as a reference for adding other DGS custom scalar types.

Next, let's start implementing the GraphQL root types. First, you are going to implement the GraphQL queries.

Implementing GraphQL queries
Both the queries are straightforward. You pass a product ID to find a product identified by that ID – that's the product query for you. Next, you pass the optional product criteria to find the products based on the given criteria, else products are returned, based on the default values of the fields of product criteria.

In REST, you have done that. You create a controller, pass the call to the service, and the service calls the repository to fetch the data from the database. You are going to use the same design. However, you are going to use ConcurrentHashMap in place of the database to simplify the code. This can also be used in your automated tests.

Let's create a repository class for that, as shown in the next code block:

public interface Repository {

  Product getProduct(String id);

  List<Product> getProducts();

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/repository/Repository.java

These are straightforward signatures for fetching the product and collection of products.

Let's implement this interface using ConcurrentHashMap as shown in the next code block:

@org.springframework.stereotype.Repository

public class InMemRepository implements Repository {

  private final Logger LOG = LoggerFactory.getLogger(

                             getClass());

  private static final Map<String, Product>

    productEntities = new ConcurrentHashMap<>();

  private static final Map<String, Tag> tagEntities =

    new ConcurrentHashMap<>();

  // rest of the code is truncated

Here, you have created two instances of ConcurrentHashMap to store the products and tags. Let's add the seed data to these maps using the constructor:

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

       .filter(t -> t.getKey().startsWith(

         faker.book().genre().substring(0, 1)))

       .map(Entry::getValue).collect(toList());

     if (tags.isEmpty()) {               

       tags.add(tagEntities.entrySet().stream()

         .findAny().get().getValue());

     }

     Product product = Product.newBuilder().id(id).name(

           title)          

       .description(faker.lorem().sentence())

       .count(faker.number().numberBetween(10, 100))

       .price(BigDecimal.valueOf(faker.number()

             .randomDigitNotZero()))

       .imageUrl(String.format("/images/%s.jpeg",

          title.replace(" ", "")))

       .tags(tags).build();

      productEntities.put(id, product);

  });

}

This code first generates the tags and then products and stores them in respective maps. This has been done for development purposes only. You should use the database in production applications.

Now, the getProduct and getProducts methods are straightforward, as shown in the next code block:

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

  return productEntities.entrySet().stream().map(e ->

                       e.getValue()).collect(toList());

}

The getProduct method performs the basic validations and returns the product. The getProducts method simply returns the collection of products converted from the map.

Now, you can add the service and its implementation. Let's add the service interface as shown in the next block:

public interface ProductService {

  Product getProduct(String id);

  List<Product> getProducts(ProductCriteria criteria);

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/services/ProductService.java

These interfaces simply call the repository to fetch the data. Let's add the implementation as shown in the next code block:

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/services/ProductServiceImpl.java

Here, the repository is injected using constructor injection.

Let's add the getProducts() method also, which also performs filtering based on given filtering criteria, as shown in the next code block:

@Override

public List<Product> getProducts(ProductCriteria criteria) {

  List<Predicate<Product>> predicates = new ArrayList<>(2);

  if (!Objects.isNull(criteria)) {

    if (Strings.isNotBlank(criteria.getName())) {

      Predicate<Product> namePredicate = p ->

          p.getName().contains(criteria.getName());

      predicates.add(namePredicate);

    }

    if (!Objects.isNull(criteria.getTags()) &&

        !criteria.getTags().isEmpty()) {

      List<String> tags = criteria.getTags().stream().map(

          ti -> ti.getName()).collect(toList());

      Predicate<Product> tagsPredicate = p ->

         p.getTags().stream().filter(t ->

                   tags.contains(t.getName())).count() > 0;

      predicates.add(tagsPredicate);

    }

  }

  if (predicates.isEmpty()) {

    return repository.getProducts();

  }

  return repository.getProducts().stream()

        .filter(p -> predicates.stream().allMatch(

                   pre -> pre.test(p))).collect(toList());

}

This method first checks whether criteria are given or not. If criteria are not given, then it calls the repository and returns all the products.

If criteria are given, then it creates the predicates list. These predicates are then used to filter out the matching products and return back to the calling function.

Now comes the most critical piece of GraphQL query implementation: writing the data fetchers. First, let's write the data fetcher for the product query next.

Writing the data fetcher for product
The data fetcher is a critical DSG component to serve the GraphQL requests that fetches the data and DSG internally resolves each of the fields. You mark them with the special @DgsComponent DGS annotation. These are types of Spring components that the DGS framework scans and uses for serving requests.

Let's create a new file called ProductDatafetcher.java in the datafetchers package for representing a DGS data fetcher component. It will have a data fetcher method for serving the product query. You can add the following code to it:

DgsComponent

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductDatafetcher.java

Here, you create a product service bean injection using the constructor. This service bean helps you to find the product based on the given product ID.

Two other important DGS framework annotations have been used in the getProduct method. Let's understand what it does:

@DgsData: This is a data fetcher annotation that marks the method as the data fetcher. The parentType property represents the type, and the field property represents the type's (parentType) field. Therefore, you can say that method would fetch the field of the given type.
You have set "Query" as parentType. The field property is set as a "product" query. Therefore, this method works as an entry point for the GraphQL query product call. The @DsgData annotation properties are set using the DgsConstants constants class.

DgsConstants is generated by the DGS Gradle plugin, which contains all the constant parts of the schema.

@InputArgument: This annotation allows you to capture the arguments passed by the GraphQL requests. Here, the value of the id parameter is captured and assigned to the id string variable.
You can find the test cases related to this data fetcher method in the Test automation section.

Similarly, you can write the data fetcher method for the products query. Let's code it in the next subsection.

Writing the data fetcher for a collection of products
Let's create a new file called ProductsDatafetcher.java in the datafetchers package for representing a DGS data fetcher component. It will have a data fetcher method for serving the products query. You can add the following code to it:

@DgsComponent

public class ProductsDatafetcher {

  private final Logger LOG = LoggerFactory.getLogger(

      getClass());

  private ProductService service;

  public ProductsDatafetcher(ProductService service) {

    this.service = service;

  }

  @DgsData(

      parentType = DgsConstants.QUERY_TYPE,

      field = QUERY.Products

  )

  public List<Product> getProducts(

              @InputArgument("filter") ProductCriteria

                  criteria) {

    return service.getProducts(criteria);

  }

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductsDatafetcher.java

This getProducts() method does not look different from the data fetcher method returned for getProduct() in the second-to-last code block. Here, the parentType and field properties of @DsgData indicate that this method would be used to fetch the collection of products for the "products" query (extra s at the end).

You are done with the GraphQL query implementation. You can now test your changes. You need to build the application before running the test. Let's build the application using the following command:

$ gradlew clean build

Once the build is done successfully, you can run the following command to run the application:

$ java –jar build/libs/chapter14-0.0.1-SNAPSHOT.jar

The application should be running on default port 8080 if you have not made any changes in the port settings.

Now, you can open a browser window and open GraphiQL using the following URL: http://localhost:8080/graphiql (part of the DGS framework). Change the host/port accordingly if required.

You can use the following query to fetch the collection of products:

{

  products(filter: {name: "His Dark Materials", tags: [{name:

                   "Fantasy"}, {name: "Legend"}]}

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

This would work great. However, what if you have to fetch the tags separately? You might have relations (such as orders having billing information) in objects that may be fetched from separate databases or services or from two separate tables. In that case, you might want to add a field resolver using the data fetcher method.

Let's add a field resolver using the data fetcher method in the next subsection.

Writing the field resolver using the data fetcher method
So far, you don't have a separate data fetcher for fetching the tags. You fetch the products and it also fetches the tags for you because we are using a concurrent map that stores both data together. Therefore, first you need to write a new data fetcher method for fetching the tags for a given product.

Let's add the tags() method to the ProductsDatafetcher class to fetch the tags, as shown in the next code block:

@DgsData(

     parentType = PRODUCT.TYPE_NAME,

     field = PRODUCT.Tags

)

public List<Tags>  tags(String productId) {

   return tagService.fetch(productId);

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductsDatafetcher.java

Here, the tags() method has a different set of values for the @DsgData properties. The parentType property is not set to a root type like earlier data fetcher methods (set to Query). Instead, it is set to an object type – "Product". The field property is set to "tags".

This method would be called for fetching the tags for each individual product because it is a field resolver for the tags field of the Product object. Therefore, if you have 20 products, this method would be called 20 times to fetch the tags for each of the 20 products. This is an N+1 problem, which we learned about in the last chapter (Chapter 13, GraphQL Fundamentals).

In the N+1 problem, extra database calls are made for fetching the data for relations. Therefore, given a collection of products, it may hit a database for fetching the tags for each product separately.

You know that you have to use data loaders to avoid the N+1 problem. Data loaders cache all the IDs of products before fetching their corresponding tags in a single query.

Next, let's learn how to implement a data loader for fixing the N+1 problem in this case.

Writing a data loader for solving the N+1 problem
You are going to make use of the DataFetchingEnvironment class as an argument in the data fetcher methods. It is injected by the graphql-java library in the data fetcher methods to provide the execution context. This execution context contains information about the resolver, such as the object and its fields. You can also use them in special use cases such as loading the data loader classes.

Let's modify the tags() method in the ProductsDatafetcher class mentioned in the previous code block to fetch the tags without the N+1 problem, as shown in the next code block:

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductsDatafetcher.java

Here, the modified tags() data fetcher method performs the fetch method using a data loader and returns the collection of tags wrapped inside CompletableFuture. And it would be called only once even if the number of products are more than 1.

WHAT IS COMPLETABLEFUTURE?

CompletableFuture is a Java concurrency class that represents the result of asynchronous computation, which is marked as completed explicitly. It can chain multiple dependent tasks asynchronously where the next task would be triggered when the current task's result is available.

You are using DsgDataFetchingEnvironment as an argument. It implements the DataFetchingEnvironment interface and provides ways to load the data loader class by both its class and name. Here, you are using the data loader class to load the data loader.

The getSource() method of DsgDataFetchingEnvironment returns the value from the parentType property of @DsgData. Therefore, getSource() returns Product.

This modified data fetcher method would fetch the tags for a given list of products. List of products? You are just passing a single product ID. This is correct, the data loader class implements MappedBatchLoader, which performs the operation using batches.

The data loader class fetches the tags of the given product (by ID) using the data loader in batches. The magic lies in returning CompletableFuture. Therefore, though you are passing a single product ID as an argument, the data loader processes it in bunches. Let's implement this data loader class (TagsDataloaderWithContext) next to dig into it more.

You can create a data loader class in two ways – with context or without context. Data loaders without context implement MappedBatchLoader, which has the following method signature:

CompletionStage<Map<K, V>> load(Set<K> keys);

On the other hand, data loaders with context implement the MappedBatchLoaderWithContext interface, which has the following method signature:

CompletionStage<Map<K, V>> load(Set<K> keys,

    BatchLoaderEnvironment environment);

Both are the same as far as data loading is concerned. However, the data loader with context provides you with extra information (through BatchLoaderEnvironment) that can be used for various additional features, such as authentication, authorization, or passing the database details.

Create a new Java file called TagsDataloaderWithContext.java in the dataloaders package with the following code:

@DgsDataLoader(name = "tagsWithContext")

public class TagsDataloaderWithContext implements                    MappedBatchLoaderWithContext<String,                  List<Tag>> {

  private final TagService tagService;

  public TagsDataloaderWithContext(TagService tagService) {

    this.tagService = tagService;

  }

  @Override

  public CompletionStage<Map<String, List<Tag>>>       load(Set<String> keys,       BatchLoaderEnvironment environment) {

    return CompletableFuture.supplyAsync(() ->                     tagService.getTags(new ArrayList<>(keys)));

  }

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/dataloaders/TagsDataloaderWithContext.java

Here, it implements the load() method from the MappedBatchLoaderWithContext interface. The BatchLoaderEnvironment argument exists, which provides the context, but we are not using it as we don't have to pass any additional information to the repository or underlying data access layer. You can find the data loader without context at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/dataloaders/TagDataloader.java. It is similar to what we have written for the data loader with context as we are not using the context.

You could see that it makes use of the tag's service to fetch the tags. Then, it simply returns the completion stage by supplying tags received from the tag service. This operation is performed in batch by the data loader.

You can create a new tag service and its implementation as follows:

public interface TagService {

  Map<String, List<Tag>> getTags(List<String> productIds);

}

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/services/TagService.java

This is the signature of the getTags method, which returns the map of product IDs with corresponding tags.

Let's implement this interface as shown in the next code block:

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/services/TagServiceImpl.java

Here, the implemented method is straightforward. It passes the call to the repository that fetches the tags based on the passed collection of product IDs.

You can add getProductTagMappings to the Repository interface as shown in the next line:

Map<String, List<Tag>> getProductTagMappings(List<String>     productIds);

Then you can implement this method in the InMemRepository class as shown in the next code block:

@Override

public Map<String, List<Tag>> getProductTagMappings(    List<String> productIds) {

  return productEntities.entrySet().stream()

            .filter(e -> productIds.contains(e.getKey()))

            .collect(toMap(e -> e.getKey(),               e -> e.getValue().getTags()));

}

Here, it first creates the stream of the product map's entry set, then filters the products that match the product passed in this method. At the end, it converts filtered products to map with the product ID as Key and Tags as the value, and then returns it.

Now, if you call the "product" GraphQL query, and even if products are fetched with a proper normalized database, it loads the product tags in batches without the N+1 problem.

You are done with GraphQL query implementation and should be comfortable with implementing queries on your own.

Next, you are going to implement GraphQL mutations.

Implementing GraphQL mutations
As per the GraphQL schema, you are going to implement two mutations – addTag and addQuantity.

The addTag mutation takes productId and a collection of tags as arguments and returns the Product object. The addQuantity mutation takes productId and quantity to add and returns Product.

Let's add this implementation to the existing ProductDatafetcher class as shown in the following code block:

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductDatafetcher.java

Here, these signatures follow the respective mutations written in the GraphQL schema. You are using another DGS framework @DgsMutation annotation, which is a type of @DgsData annotation that is marked on methods to denote them as a data fetcher method. The @DgsMutation annotation by default has the "Mutation" value set to the parentType property. You just have to set the field property in this annotation. Both of these methods have their respective values set to the field property in the @DgsMutation annotation.

If you notice, you will find that the @InputArgument annotation for tags is using another collectionType property that is used for setting the type of input. It is required when the input type is not scalar. If you don't use it, you'll get an error. Therefore, make sure to use the collectionType property whenever you have a non-scalar type input.

These methods use the tag and product services to perform the requested operations. So far, you have not added the tag service in the ProductDatafetcher class. Therefore, you need to add TagService first as shown in the next code block:

// rest of the ProductDatafetcher class code

private final TagService tagService;

public ProductDatafetcher(ProductService productService,

                                  TagService tagService) {

  this.productService = productService;

  this.tagService = tagService;

}

// rest of the ProductDatafetcher class code

Here, the TagService bean has been injected using the constructor.

Now, you need to implement the addTag() method in the TagService and addQuantity methods in ProductService. Both the interfaces and their implementations are straightforward and pass the call to the repository to perform the operations. You can have a look at the source code in the GitHub code repository (https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter14) to look into these implementations.

Let's add these two methods to the Repository interface as shown in the next code block:

Product addTags(String productId, List<TagInput> tags);

Product addQuantity(String productId, int qty);

These signatures in the Repository interface also follow the respective mutations written in the GraphQL schema.

Let's implement the addTags() method first in the InMemRepository class as shown in the next code block:

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/repository/InMemRepository.java

This implementation is straightforward. It performs a couple of validations for the passed product ID. Then it compares the new and existing tags, and adds the new tags to the passed product only if existing tags don't exist. At the end, it updates the concurrent map and returns the updated product.

Let's add the implementation of the addQuantity() method to the InMemRepository class next, as shown in the following code block:

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/repository/InMemRepository.java

Here, you first perform the validation for the productId and qty arguments. If everything goes fine, then you increase the quantity of the product, update the concurrent map, and return the updated product.

You are done with the implementation of GraphQL mutations. You can now test your changes. You need to build the application before running the test. Let's build the application using the following command:

$ gradlew clean build

Once the build is done successfully, you can run the following command to run the application:

$ java –jar build/libs/chapter14-0.0.1-SNAPSHOT.jar

The application should be running on default port 8080 if you have not made any changes to the port settings.

Now, you can open a browser window and open GraphiQL using the following URL: http://localhost:8080/graphiql (part of the DGS framework). Change the host/port accordingly if required.

You can use the following GraphQL request to perform the addTag mutation:

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

Here, you pass productId and tags as arguments. You can use the following GraphQL request to perform the addQuantity mutation:

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

Here, you pass productId and quantity as arguments. You have learned how to implement GraphQL mutations in the GraphQL server. Let's implement GraphQL subscriptions in the next subsection.

Implementing GraphQL subscriptions
Subscription is another GraphQL root type that sends the object to the subscriber (client) when a particular event occurs.

Let's assume an online shop offers a discount on products when the product's inventory reaches a certain level. You cannot track each and every product's quantity manually and then perform the computation and trigger the discount. This is where you can make use of the subscription.

Each change in the product's inventory (quantity) through the addQuantity() mutation should trigger the event and the subscriber should receive the updated product and hence the quantity. Then, the subscriber can place the logic and automate this process.

Let's write the subscription that would send the updated product object to the subscriber. You are going to use Reactive Streams and WebSocket to implement this functionality.

Let's add additional dependencies in build.gradle to take care of the auto-configuration of WebSocket and the playground tool to test the subscription functionality. (By default DGS provides the GraphiQL app to explore the documentation and schema and play with queries. However, the bundled GraphiQL tool doesn't work properly for testing the subscription presently. Once it starts working, you don't need to add the playground tool.)

Let's add these dependencies to build.gradle as shown in the following code block:

dependencies {

  // other dependencies …

  runtimeOnly 'com.netflix.graphql.dgs:graphql-dgs-

    subscriptions-websockets-autoconfigure'

  implementation 'com.graphql-java-kickstart:playground-

    spring-boot-starter:11.0.0'

  // other dependencies …

}

Now, you can add the following subscription data fetcher to the ProductDatafetcher class as shown in the following code:

// rest of the ProductDatafetcher class code

@DgsSubscription(field = SUBSCRIPTION.QuantityChanged)

public Publisher<Product> quantityChanged(

                     @InputArgument("productId") String

                     productId) {

  return productService.gerProductPublisher();

}

// rest of the ProductDatafetcher class code

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/datafetchers/ProductDatafetcher.java

Here, you are using another DGS framework annotation, @DgsSubscription, which is a type of @DgsData annotation that is marked on a method to denote it as a data fetcher method. The @DgsSubscription annotation by default has the Subscription value set to the parentType property. You just have to set the field property in this annotation. By setting the field to quantityChanged, you are indicating to the DGS framework to use this method when the subscription request for quantityChanged is called.

The Subscription method returns the Publisher instance, which can be sent an unbound number of objects (in this case, Product instances) to multiple subscribers. Therefore, the client just needs to subscribe to the product publisher.

You need to add a new method to the ProductService interface and its implementation in the ProductServiceImpl class. The method signature in the ProductService interface and its implementation are straightforward. It passes the call to the repository to perform the operation. You can have a look at the source code in the GitHub code repository at https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter14.

Actual work is being performed by the repository. Therefore, you need to make certain changes in the repository as shown in the following steps:

First add the following method signature to the Repository interface:
      Publisher<Product> getProductPublisher();

Next, you have to implement the getProductPublisher() method in the InMemRepository class. This method returns the product publisher as shown in the following code:
      public Publisher<Product> getProductPublisher() {

        return productPublisher;

      }

Now, we need all the magic to be performed by Reactive Streams. First, let's declare the FluxSink<Product> and ConnectableFlux<Product> (which is returned by the repository) variables:
      private FluxSink<Product> productsStream;

      private ConnectableFlux<Product> productPublisher;

Now, we need to initialize these declared instances. Let's do so in InMemRepository's constructor as shown in the following code:
      Flux<Product> publisher = Flux.create(emitter -> {

        productsStream = emitter;

      });

      productPublisher = publisher.publish();

      productPublisher.connect();

Flux<Product> is a product stream publisher that passes the baton to productsStream (FluxSink) for emitting the next signals followed by onError() or onComplete() events. This means productsStream should emit the signal when the product quantity gets changed. When Flux<Product> calls the publish() method, it returns an instance of connectableFlux, which is assigned to productPublisher (the one that is returned by the subscription).
You are almost done with the setup. You just need to emit the signal (product) when the product gets changed. Let's add the following highlighted line to the addQuantity() method before it returns the product, as shown in the following code:
      product.setCount(product.getCount() + qty);

      productEntities.put(product.getId(), product);

      productsStream.next(product);

      return product;

You have completed the subscription quantityChanged implementation. You can test it next.

You need to build the application before running the test. Let's build the application using the following command:

$ gradlew clean build

Once the build is done successfully, you can run the following command to run the application:

$ java –jar build/libs/chapter14-0.0.1-SNAPSHOT.jar

The application should be running on default port 8080 if you have not made any changes in the port settings.

The playground tool should be available at http://localhost:8080/playground if the application is running on localhost, else make the appropriate changes in the hostname.

Once the playground app is up, run the following query in it:

subscription {

  quantityChanged(productId: "a1s2d3f4-0") {

    id

    name

    description

    price

    count

  }

}

This should trigger the application in listening mode. The app will wait for object publications.

Now, you can open another browser window and open GraphiQL using the following URL: http://localhost:8080/graphiql. Change the host/port accordingly if required.

Here, you can fire the addQuantity mutation by running the following:

mutation {

addQuantity(productId: "a1s2d3f4-0", quantity: 10) {

    id

    name

    price

    count

  }

}

Each successful change would publish the updated product to the playground app.

You should know about the instrumentation that helps to implement the tracing, logging, and metrics collection. Let's discuss this in the next subsection.

Instrumenting the GraphQL API
The GraphQL Java library supports the instrumentation of the GraphQL API. This can be used to support metrics, tracing, and logging. The DGS framework also uses it. You just have to mark the instrumentation class with the Spring @Component annotation.

The instrumentation bean should implement the graphql.execution.instrumentation.Instumentation interface, an easier way to extend the SimpleInstumentation class.

Let's add instrumentation that would record the time taken by the data fetcher and complete GraphQL request processing. This metric may help you to fine-tune the performance and identify the fields that take more time to resolve.

Let's create the TracingInstrumentation.java file in the instrumentation package and add the following code:

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

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter14/src/main/java/com/packt/modern/api/instrumentation/TracingInstrumentation.java

This class extends SimpleInstrumentation and is created as a Spring bean by marking it as @Component. First of all, you need to create the instrumentation state by overriding the createState() method. Since you are implementing the time metric, you choose startTime as the state. A static inner class is added for declaring the startTime state.

As a next activity, you would like to initialize the instrumentation state. For that purpose you can override the beginExecution() method as shown in the following code:

@Override

public InstrumentationContext<ExecutionResult>

    beginExecution(

      InstrumentationExecutionParameters parameters) {

  TracingState tracingState =

      parameters.getInstrumentationState();

  tracingState.startTime = System.currentTimeMillis();

  return super.beginExecution(parameters);

}

This method allows you to set the instrumentation parameters.

The startTime state is set. Next, you'll override the instrumentExecutionResult() method. This helps you to instrument the execution result such as calculating the total execution time. Let's add the following code to calculate the total execution time:

@Override

public CompletableFuture<ExecutionResult>     instrumentExecutionResult(    ExecutionResult executionResult,    InstrumentationExecutionParameters parameters) {

  TracingState tracingState =     parameters.getInstrumentationState();

  long timeTaken = System.currentTimeMillis() -     tracingState.startTime;

  LOG.info("Request processing took: {} ms", timeTaken);

  return super.instrumentExecutionResult(

                                      executionResult,

                                      parameters);

}

It is a straightforward implementation to calculate the total execution time. It extracts the startTime state from the parameters and then uses it to calculate the timeTaken value.

So far you have overridden three methods – the initial method (createState()), the beginning method (beginExecution()) for state initialization, and the end method (instrumentExecutionResult()) for final calculations or state recording.

One intermediate method (instrumentDataFetcher()) that falls between beginExecution() and instrumentExecutionResult() is yet to be overridden. It is complex compared to other methods. Therefore, you'll override it after other methods.

Let's add the following code to override the instrumentDataFetcher() method:

@Override

public DataFetcher<?> instrumentDataFetcher(DataFetcher<?>   

      dataFetcher, InstrumentationFieldFetchParameters

      parameters) {

  if (parameters.isTrivialDataFetcher()) {

    return dataFetcher;

  }

  return environment -> {

    long initTime = System.currentTimeMillis();

    Object result = dataFetcher.get(environment);

    String msg = "Instrumentation of datafetcher {} took {}

                  ms";

    if (result instanceof CompletableFuture) {

      ((CompletableFuture<?>) result).whenComplete((r, ex)

          -> {

        long timeTaken = System.currentTimeMillis() –

            initTime;

        LOG.info(msg, findDatafetcherTag(parameters),

            timeTaken);

      });

    } else {

      long timeTaken = System.currentTimeMillis() –

          initTime;

      LOG.info(msg, findDatafetcherTag(parameters),

          timeTaken);

    }

    return result;

  };

}

This method is used for instrumenting the data fetchers. You have added two separate blocks to calculate the data fetching time because values can be returned in two ways by data fetcher methods – a blocking call or an asynchronous call (CompletableFuture). This method would be called for each data fetching call whether it is for the root type or for a field of the object type.

The final piece of instrumentation implementation is the findDatafetcherTag() method. This private method is added to find out the data fetching type of the field/root type.

Let's add it as shown in the following code:

private String findDatafetcherTag(

                 InstrumentationFieldFetchParameters parameters) {

  GraphQLOutputType type =

    parameters.getExecutionStepInfo()

                                        .getParent().getType();

  GraphQLObjectType parent;

  if (type instanceof GraphQLNonNull) {

    parent = (GraphQLObjectType)

                    ((GraphQLNonNull)

                      type).getWrappedType();

  } else {

    parent = (GraphQLObjectType) type;

  }

  return parent.getName() + "." +

      parameters.getExecutionStepInfo().getPath()

          .getSegmentName();

}

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
@DisplayName("Verify the JSON attrs returned from query
             'product'")
public void product() {
  String name = dgsQueryExecutor.executeAndExtractJsonPath(
         "{ product(id: \"any\") { name }}",
         "data.product.name");
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
  ExecutionResult result = dgsQueryExecutor.execute(
                              " { product (id: \"any\") {
                                 name }}");
  verify(productService, times(1)).getProduct("any");
  assertThat(result.getErrors()).isNotEmpty();
  assertThat(result.getErrors().get(0).getMessage())
     .isEqualTo("java.lang.RuntimeException: Invalid
                Product ID.");
}
```
Here, the product service method is stubbed for throwing the exception. When DgsQueryExecutor runs, the Spring-injected mock bean uses the stubbed method to throw the exception that is being asserted here.

Next, let's query product again, this time to explore GraphQLQueryRequest, which allows you to form the GraphQL query in a fluent way. The GraphQLQueryRequest construction takes two arguments – first the instance of GraphQLQuery, which can be a query/mutation or subscription, and second the projection root type of BaseProjectionNode, which allows you to select the fields.

Let's add the following code to test the product query using GraphQLQueryRequest:
```java
@Test
@DisplayName("Verify JSON attrs using GraphQLQueryRequest")
void productsWithQueryApi() {
  GraphQLQueryRequest graphQLQueryRequest = new
      GraphQLQueryRequest(
           ProductGraphQLQuery.newRequest().id(
               "any").build(),
           new ProductProjectionRoot().id().name());
  String name = dgsQueryExecutor.executeAndExtractJsonPath(
           graphQLQueryRequest.serialize(),
           "data.product.name");
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

