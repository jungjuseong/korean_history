## Part 1. First Steps

788 / 5000
번역 결과
보안은 소프트웨어 시스템의 필수적인 비기능적 특성 중 하나입니다. 이 책에서 배우는 가장 중요한 측면 중 하나는 애플리케이션 개발의 초기 단계부터 보안을 고려해야 한다는 것입니다. 1장에서는 애플리케이션 개발 프로세스에서 보안의 위치에 대해 논의하는 것으로 시작합니다. 그런 다음 2장에서 몇 가지 간단한 프로젝트를 구현하여 Spring Security의 백본 아키텍처의 기본 구성 요소를 소개합니다.
이 부분의 목적은 특히 이 프레임워크를 막 배우기 시작하는 경우 Spring Security를 시작하는 것입니다. 그러나 애플리케이션 수준 보안의 일부 측면과 Spring Security의 기본 아키텍처를 이미 알고 있더라도 이 부분을 복습으로 읽는 것이 좋습니다. 

# 1 Security today

This chapter covers
- What Spring Security is and what you can solve by using it
- What security is for a software application
- Why software security is essential and why you should care
- Common vulnerabilities that you’ll encounter at the application level
  
Figure 1.1 A user mainly thinks about functional requirements. Sometimes, you might see them aware of performance, which is nonfunctional, but unfortunately, it’s quite unusual that a user cares about security. Nonfunctional requirements tend to be more transparent than functional ones.
There are multiple nonfunctional aspects to consider when working on a software system. In practice, all of these are important and need to be treated responsibly in the process of software development. In this book, we focus on one of these: security. You’ll learn how to protect your application, step by step, using Spring Security.
But before starting, I’d like to make you aware of the following: depending on how much experience you have, you might find this chapter cumbersome. Don’t worry too much if you don’t understand absolutely all the aspects for now. In this chapter, I want to show you the big picture of security-related concepts. Throughout the book, we work on practical examples, and where appropriate, I’ll refer back to the description I give in this chapter. Where applicable, I’ll also provide you with more details. Here and there, you’ll find references to other materials (books, articles, documentation) on specific subjects that are useful for further reading.


## 1.1 Spring Security: The what and the why

In this section, we discuss the relationship between Spring Security and Spring. It is important, first of all, to understand the link between the two before starting to use those. If we go to the official website, https://spring.io/projects/spring-security, we see Spring Security described as a powerful and highly customizable framework for authentication and access control. I would simply say it is a framework that enormously simplifies applying (or “baking”) security for Spring applications.
Spring Security is the primary choice for implementing application-level security in Spring applications. Generally, its purpose is to offer you a highly customizable way of implementing authentication, authorization, and protection against common attacks. Spring Security is an open source software released under the Apache 2.0 license. You can access its source code on GitHub at https://github.com/spring-projects/ spring-security/. I highly recommend that you contribute to the project as well.

> NOTE 
> You can use Spring Security for both standard web servlets and reactive applications. To use it, you need at least Java 8, although the examples in this book use Java 11, which is the latest long-term supported version.

I can guess that if you opened this book, you work on Spring applications, and you are interested in securing those. Spring Security is most likely the best choice for you. It’s the de facto solution for implementing application-level security for Spring applications. Spring Security, however, doesn’t automatically secure your application. It’s not some kind of magic panacea that guarantees a vulnerability-free app. Developers need to understand how to configure and customize Spring Security around the needs of their applications. How to do this depends on many factors, from the functional requirements to the architecture.
Technically, applying security with Spring Security in Spring applications is simple. You’ve already implemented Spring applications, so you know that the framework’s philosophy starts with the management of the Spring context. You define beans in the Spring context to allow the framework to manage these based on the configurations you specify. And you use only annotations to make these configurations and leave behind the old-fashioned XML configuration style!
You use annotations to tell Spring what to do: expose endpoints, wrap methods in transactions, intercept methods in aspects, and so on. The same is true with Spring Security configurations, which is where Spring Security comes into play. What you want is to use annotations, beans, and in general, a Spring-fashioned configuration style comfortably when defining your application-level security. In a Spring application, the behavior that you need to protect is defined by methods.
To think about application-level security, you can consider your home and the way you allow access to it. Do you place the key under the entrance rug? Do you even have a key for your front door? The same concept applies to applications, and Spring Security helps you develop this functionality. It’s a puzzle that offers plenty of choices for building the exact image that describes your system. You can choose to leave your house completely unsecured, or you can decide not to allow everyone to enter your home.
The way you configure security can be straightforward like hiding your key under the rug, or it can be more complicated like choosing a variety of alarm systems, video cameras, and locks. In your applications, you have the same options, but as in real life, the more complexity you add, the more expensive it gets. In an application, this cost refers to the way security affects maintainability and performance.
But how do you use Spring Security with Spring applications? Generally, at the application level, one of the most encountered use cases is when you’re deciding whether someone is allowed to perform an action or use some piece of data. Based on configurations, you write Spring Security components that intercept the requests and that ensure whoever makes the requests has permission to access protected resources. The developer configures components to do precisely what’s desired. If you mount an alarm system, it’s you who should make sure it’s also set up for the windows as well as for the doors. If you forget to set it up for the windows, it’s not the fault of the alarm system that it doesn’t trigger when someone forces a window.
Other responsibilities of Spring Security components relate to data storage as well as data transit between different parts of the systems. By intercepting calls to these different parts, the components can act on the data. For example, when data is stored, these components can apply encryption or hashing algorithms. The data encodings keep the data accessible only to privileged entities. In a Spring application, the developer has to add and configure a component to do this part of the job wherever it’s needed. Spring Security provides us with a contract through which we know what the framework requires to be implemented, and we write the implementation according to the design of the application. We can say the same thing about transiting data.
In real-world implementations, you’ll find cases in which two communicating components don’t trust each other. How can the first know that the second one sent a specific message and it wasn’t someone else? Imagine you have a phone call with somebody to whom you have to give private information. How do you make sure that on the other end is indeed a valid individual with the right to get that data, and not somebody else? For your application, this situation applies as well. Spring Security provides components that allow you to solve these issues in several ways, but you have to know which part to configure and then set it up in your system. This way, Spring Security intercepts messages and makes sure to validate communication before the application uses any kind of data sent or received.
Like any framework, one of the primary purposes of Spring is to allow you to write less code to implement the desired functionality. And this is also what Spring Security does. It completes Spring as a framework by helping you write less code to perform one of the most critical aspects of an application--security. Spring Security provides predefined functionality to help you avoid writing boilerplate code or repeatedly writing the same logic from app to app. But it also allows you to configure any of its components, thus providing great flexibility. To briefly recap this discussion:
- You use Spring Security to bake application-level security into your applications in the “Spring” way. By this, I mean, you use annotations, beans, the Spring Expression Language (SpEL), and so on.
- Spring Security is a framework that lets you build application-level security. However, it is up to you, the developer, to understand and use Spring Security properly. Spring Security, by itself, does not secure an application or sensitive data at rest or in flight.
- This book provides you with the information you need to effectively use Spring Security.

Alternatives to Spring Security

This book is about Spring Security, but as with any solution, I always prefer to have a broad overview. Never forget to learn the alternatives that you have for any option. One of the things I’ve learned over time is that there’s no general right or wrong. “Everything is relative” also applies here!
You won’t find a lot of alternatives to Spring Security when it comes to securing a Spring application. One alternative you could consider is Apache Shiro (https://shiro.apache.org). It offers flexibility in configuration and is easy to integrate with Spring and Spring Boot applications. Apache Shiro sometimes makes a good alternative to the Spring Security approach.
If you’ve already worked with Spring Security, you’ll find using Apache Shiro easy and comfortable to learn. It offers its own annotations and design for web applications based on HTTP filters, which greatly simplify working with web applications. Also, you can secure more than just web applications with Shiro, from smaller command-line and mobile applications to large-scale enterprise applications. And even if simple, it’s powerful enough to use for a wide range of things from authentication and authorization to cryptography and session management.
However, Apache Shiro could be too “light” for the needs of your application. Spring Security is not just a hammer, but an entire set of tools. It offers a larger scale of possibilities and is designed specifically for Spring applications. Moreover, it benefits from a larger community of active developers, and it is continuously enhanced.
1.2 What is software security?
Software systems today manage large amounts of data, of which a significant part can be considered sensitive, especially given the current General Data Protection Regulations (GDPR) requirements. Any information that you, as a user, consider private is sensitive for your software application. Sensitive data can include harmless information like a phone number, email address, or identification number; although, we generally think more about data that is riskier to lose, like your credit card details. The application should ensure that there’s no chance for that information to be accessed, changed, or intercepted. No parties other than the users to whom this data is intended should be able to interact in any way with it. Broadly expressed, this is the meaning of security.
NOTE GDPR created a lot of buzz globally after its introduction in 2018. It generally represents a set of European laws that refer to data protection and gives people more control over their private data. GDPR applies to the owners of systems having users in Europe. The owners of such applications risk significant penalties if they don’t respect the regulations imposed.
We apply security in layers, with each layer requiring a different approach. Compare these layers to a protected castle (figure 1.2). A hacker needs to bypass several obstacles to obtain the resources managed by the app. The better you secure each layer, the lower the chance an individual with bad intentions manages to access data or perform unauthorized operations.
 
Figure 1.2 The Dark Wizard (a hacker) has to bypass multiple obstacles (security layers) to steal the Magic Sword (user resources) from the Princess (your application).
Security is a complex subject. In the case of a software system, security doesn’t apply only at the application level. For example, for networking, there are issues to be taken into consideration and specific practices to be used, while for storage, it’s another discussion altogether. Similarly, there’s a different philosophy in terms of deployment, and so on. Spring Security is a framework that belongs to application-level security. In this section, you’ll get a general picture of this security level and its implications.
Application-level security (figure 1.3) refers to everything that an application should do to protect the environment it executes in, as well as the data it processes and stores. Mind that this isn’t only about the data affected and used by the application. An application might contain vulnerabilities that allow a malicious individual to affect the entire system!
 
Figure 1.3 We apply security in layers, and each layer depends on those below it. In this book, we discuss Spring Security, which is a framework used to implement application-level security at the top-most level.
To be more explicit, let’s discuss using some practical cases. We’ll consider a situation in which we deploy a system as in figure 1.4. This situation is common for a system designed using a microservices architecture, especially if you deploy it in multiple availability zones in the cloud.
 
Figure 1.4 If a malicious user manages to get access to the virtual machine (VM) and there’s no applied application-level security, a hacker can gain control of the other applications in the system. If communication is done between two different availability zones (AZ), a malicious individual will find it easier to intercept the messages. This vulnerability allows them to steal data or to impersonate users.
With such microservice architectures, we can encounter various vulnerabilities, so you should exercise caution. As mentioned earlier, security is a cross-cutting concern that we design on multiple layers. It’s a best practice when addressing the security concerns of one of the layers to assume as much as possible that the above layer doesn’t exist. Think about the analogy with the castle in figure 1.2. If you manage the “layer” with 30 soldiers, you want to prepare them to be as strong as possible. And you do this even knowing that before reaching them, one would need to cross the fiery bridge.
With this in mind, let’s consider that an individual driven by bad intentions would be able to log in to the virtual machine (VM) that’s hosting the first application. Let’s also assume that the second application doesn’t validate the requests sent by the first application. The attacker can then exploit this vulnerability and control the second application by impersonating the first one.
Also, consider that we deploy the two services to two different locations. Then the attacker doesn’t need to log in to one of the VMs as they can directly act in the middle of communications between the two applications.
NOTE An availability zone (AZ in figure 1.4) in terms of cloud deployment is a separate data center. This data center is situated far enough geographically (and has other dependencies) from other data centers of the same region that, if one availability zone fails, the probability that others are failing too is minimal. In terms of security, an important aspect is that traffic between two different data centers generally goes across a public network.
Monolithic and microservices
The discussion on monolithic and microservices architectural styles is a whole different tome. I refer to these in multiple places in this book, so you should at least be aware of the terminology. For an excellent discussion of the two architectural styles, I recommend that you read Chris Richardson’s Microservices Patterns (Manning, 2018).
By monolithic architecture, we refer to an application in which we implement all the responsibilities in the same executable artifact. Consider this as one application that fulfills all use cases. The responsibilities can sometimes be implemented within different modules to make the application more comfortable to maintain. But you can’t separate the logic of one from the logic of others at runtime. Generally, monolithic architectures offer less flexibility for scaling and deployment management.
With a microservices system, we implement the responsibilities within different executable artifacts. You can see the system as being formed of multiple applications that execute at the same time and communicate between themselves when needed via the network. While this offers more flexibility for scaling, it introduces other difficulties. We can enumerate here latencies, security concerns, network reliability, distributed persistence, and deployment management.
I referred earlier to authentication and authorization. And, indeed, these are often present in most applications. Through authentication, an application identifies a user (a person or another application). The purpose of identifying these is to be able to decide afterward what they should be allowed to do--that’s authorization. I provide quite a lot of details on authentication and authorization, starting with chapter 3 and continuing throughout the book.
In an application, you often find the need to implement authorization in different scenarios. Consider another situation: most applications have restrictions regarding the user for obtaining access certain functionality. Achieving this implies first the need to identify who creates an access to request for a specific feature--that’s authentication. As well, we need to know their privileges to allow the user to use that part of the system. As the system becomes more complex, you’ll find different situations that require a specific implementation related to authentication and authorization.
For example, what if you’d like to authorize a particular component of the system against a subset of data or operations on behalf of the user? Let’s say the printer needs access to read the user’s documents. Should you simply share the credentials of the user with the printer? But that allows the printer more rights than needed! And it also exposes the credentials of the user. Is there a proper way to do this without impersonating the user? These are essential questions, and the kind of questions you encounter when developing applications: questions that we not only want to answer, but for which you’ll see applications with Spring Security in this book.
Depending on the chosen architecture for the system, you’ll find authentication and authorization at the level of the entire system, as well as for any of the components. And as you’ll see further along in this book, with Spring Security, you’ll sometimes prefer to use authorization even for different tiers of the same component. In chapter 16, we’ll discuss more on global method security, which refers to this aspect. The design gets even more complicated when you have a predefined set of roles and authorities.
I would also like to bring to your attention data storage. Data at rest adds to the responsibility of the application. Your app shouldn’t store all its data in a readable format. The application sometimes needs to keep the data either encrypted with a private key or hashed. Secrets like credentials and private keys can also be considered data at rest. These should be carefully stored, usually in a secrets vault.
NOTE We classify data as “at rest” or “in transition.” In this context, data at rest refers to data in computer storage or, in other words, persisted data. Data in transition applies to all the data that’s exchanged from one point to another. Different security measures should, therefore, be enforced, depending on the type of data.
Finally, an executing application must manage its internal memory as well. It may sound strange, but data stored in the heap of the application can also present vulnerabilities. Sometimes the class design allows the app to store sensitive data like credentials or private keys for a long time. In such cases, someone who has the privilege to make a heap dump could find these details and then use them maliciously.
With a short description of these cases, I hope I’ve managed to provide you with an overview of what we mean by application security, as well as the complexity of this subject. Software security is a tangled subject. One who is willing to become an expert in this field would need to understand (as well as to apply) and then test solutions for all the layers that collaborate within a system. In this book, however, we’ll focus only on presenting all the details of what you specifically need to understand in terms of Spring Security. You’ll find out where this framework applies and where it doesn’t, how it helps, and why you should use it. Of course, we’ll do this with practical examples that you should be able to adapt to your own unique use cases.

## 1.3 Why is security important?
The best way to start thinking about why security is important is from your point of view as a user. Like anyone else, you use applications, and these have access to your data. These can change your data, use it, or expose it. Think about all the apps you use, from your email to your online banking service accounts. How would you evaluate the sensitivity of the data that is managed by all these systems? How about the actions that you can perform using these systems? Similarly to data, some actions are more important than others. You don’t care very much about some of those, while others are more significant. Maybe for you, it’s not that important if someone would somehow manage to read some of your emails. But I bet you’d care if someone else could empty your bank accounts.
Once you’ve thought about security from your point of view, try to see a more objective picture. The same data or actions might have another degree of sensitivity to other people. Some might care a lot more than you if their email is accessed and someone could read their messages. Your application should make sure to protect everything to the desired degree of access. Any leak that allows the use of data and functionalities, as well as the application, to affect other systems is considered a vulnerability, and you need to solve it.
Not respecting security comes with a price that I’m sure you aren’t willing to pay. In general, it’s about money. But the cost can differ, and there are multiple ways through which you can lose profitability. It isn’t only about losing money from a bank account or using a service without paying for it. These things indeed imply cost. The image of a brand or a company is also valuable, and losing a good image can be expensive--sometimes even more costly than the expenses directly resulting from the exploitation of a vulnerability in the system! The trust that users have in your application is one of its most valuable assets, and it can make the difference between success or failure.

Here are a few fictitious examples. Think about how you would see these as a user. How can these affect the organization responsible for the software?

- A back-office application should manage the internal data of an organization but, somehow, some information leaks out.
- Users of a ride-sharing application observe that money is debited from their accounts on behalf of trips that aren’t theirs.
- After an update, users of a mobile banking application are presented with transactions that belong to other users.

In the first situation, the organization using the software, as well as its employees, can be affected. In some instances, the company could be liable and could lose a significant amount of money. In this situation, users don’t have the choice to change the application, but the organization can decide to change the provider of their software.

In the second case, users will probably choose to change the service provider. The image of the company developing the application would be dramatically affected. The cost lost in terms of money in this case is much less than the cost in terms of image. Even if payments are returned to the affected users, the application will still lose some customers. This affects profitability and can even lead to bankruptcy. And in the third case, the bank could see dramatic consequences in terms of trust, as well as legal repercussions.

In most of these scenarios, investing in security is safer than what happens if someone exploits a vulnerability in your system. For all of the examples, only a small weakness could cause each outcome. For the first example, it could be a broken authentication or a cross-site request forgery (CSRF). For the second and third examples, it could be a lack of method access control. And for all of these examples, it could be a combination of vulnerabilities.
Of course, from here we can go even further and discuss the security in defense-related systems. If you consider money important, add human lives to the cost! Can you even imagine what could be the result if a health care system was affected? What about systems that control nuclear power? You can reduce any risk by investing early in the security of your application and by allocating enough time for security professionals to develop and test your security mechanisms.
NOTE The lessons learned from those who failed before you are that the cost of an attack is usually higher than the investment cost of avoiding the vulnerability.
In the rest of this book, you’ll see examples of ways to apply Spring Security to avoid situations like the ones presented. I guess there will never be enough word written about how important security is. When you have to make a compromise on the security of your system, try to estimate your risks correctly.

## 1.4 웹에서 일반적인 보안 취약점

Before we discuss how to apply security in your applications, you should first know what you’re protecting the application from. To do something malicious, an attacker identifies and exploits the vulnerabilities of your application. We often describe vulnerability as a weakness that could allow the execution of actions that are unwanted, usually done with malicious intentions.
An excellent start to understanding vulnerabilities is being aware of the Open Web Application Security Project, also known as OWASP (https://www.owasp.org). At OWASP, you’ll find descriptions of the most common vulnerabilities that you should avoid in your applications. Let’s take a few minutes and discuss these theoretically before diving into the next chapters, where you’ll start to apply concepts from Spring Security. Among the common vulnerabilities that you should be aware of, you’ll find these:

- 깨진 인증
- 세션 고정
- Cross-site scripting (XSS)
- Cross-site request forgery (CSRF)
- 인젝션
- 중요 데이터 노출
- 액세스 제어 방법이 미흡
- 알려진 취약점이 있는 의존성 사용

These items are related to application-level security, and most of these are also directly related to using Spring Security. We’ll discuss their relationship with Spring Security and how to protect your application from these in detail in this book, but first, an overview.

### 1.4.1 인증과 권한부여에서 취약점

인증은 누군가 애플리케이션을 사용하려고 하는지를 식별하는 과정이다. 누구 또는 무엇인가가 앱을 사용할 때는 그들을 식별하여 권한을 허용할지 여부를 결정하고자 한다. 실제 앱에서는 익명의 사용자를 허용하기도 하지만 대부분의 경우 사용자를 식별한 후에만 서비스 또는 데이터를 사용할 수 있다. 사용자를 파악했다면 권한 부여를 처리할 수 있다.
권한 부여는 인증된 요청자가 특정 기능과 데이터를 사용할 권한을 부여하는 과정이다. 예를 들어 모바일 뱅킹 앱에서는 인증된 사용자만 자신의 계좌에서 이체를 할 수 있다.
나쁜 의도를 가진 누군가가 그에게 없는 기능과 데이터의 액세스를 획득한다면 인증이 깨진 것이라고 말할 수 있다. 스프링 시큐리티와 같은 프레임워크는 이와 같은 침해 가능성을 낮춰주지만 정확하게 사용하지 않으면 그런 일이 생길 수 있다. 예를 들어 특정한 역할을 가진 승인된 사용자만 사용하도록 특정 엔드포인트 액세스를 정의 할 수 있다. 데이터 레벨에서 제약이 없다면 누군가가 다른 사용자의 데이터를 사용할 방법을 찾아낼지도 모른다.
아래 그림을 보면 인증된 사용자는 /products/{name}를 액세스 할 수 있다. 브라우저로부터 웹 앱은 이 엔드포인트를 불러 시용자의 제품 정보를 표시한다. 하지만 앱이 정보를 리턴할 때 제품이 누군것인지를 검증하지 않으면 어떻게 될까? 어떤 사용자는 다른 사용자의 정보를 얻는 방법을 알아낼 것이다. 이 상황은 애플리케이션 설계 시작부터 고려할 사항 중에 하나이다.
 
Figure 1.5 A user that is logged in can see their products. If the application server only checks if the user is logged in, then the user can call the same endpoint to retrieve the products of some other user. In this way, John is able to see data that belongs to Bill. The issue that causes this problem is that the application doesn’t authenticate the user for data retrieval as well.
Throughout the book, we’ll refer to vulnerabilities. We’ll discuss vulnerabilities starting with the basic configuration of authentication and authorization in chapter 3. Then, we’ll discuss how vulnerabilities relate to the integration of Spring Security and Spring Data and how to design an application to avoid those, with OAuth 2.

### 1.4.2 세션 고정이란?

세션 고정 침해는 더 특수하고 고위험의 약점이다. 이것이 생기면 공격자는 앞서 생성된 세션 ID를 재사용하여 적합한 사용자로 위장하는 것을 허용한다. 이러한 침해는 인증 과정에서 생길 수 있는데 웹 애플리케이션이 고유한 세션 ID를 할당하지 않는 경우이다. 이것은 잠재적으로 기존의 세션 ID를 재사용으로 이어질 수 있다. 이 취약점을 악용하려면 적합한 세션 ID를 얻어 희생자의 브라우저에서 사용하도록 하면 된다.
웹 애플리케이션을 구현하는 방법에 따라 누군가가 이러한 취약점을 사용할 수 있는 다양한 방식이 있다. 예를 들어 애플리케이션이 URL에 세션 ID를 보여주면 희생자는 악성 링크를 클릭하도록 속임을 당할 수 있다. 애플리케이션이 히든 속성을 사용하면 공격자는 희생자를 속여서 외부 폼을 사용하도록 하여 서버에게 전송하게 할 수 있다. 애플리케이션이 세션 값을 쿠키에 저장하면 공격자는 스크립트를 주입하여 희생자의 브라우저가 실행하게 할 수 있다.

### 1.4.3 WHAT IS CROSS-SITE SCRIPTING (XSS)?

XSS라고 하는 크로스 사이트 스크립트는 클라이언트 측 스크립트를 서버에 노출된 웹 서버스에 주입을 허용하여 다른 사용자들이 이를 실행하게 한다. 사용하거나 보관하기 전에 요청을 알맞게 “소독”해서 원하지 않은 외부 스크립트의 실행을 막을 수 있다. 잠재적 영향으로 계정 사취에 연관되거나 DDOS와 같은 분산 공격에 참가하게 된다.
예를 들면, 사용자가 메시지 또는 답글을 포스팅한다. 메시지를 포스팅한 후에 사이트는 메시지를 표시하여 페이지 방문자가 볼 수 있도록 한다. 매일 수백명의 사용자가 이 사이트를 방문한다고 가정하자. 만일 이 사용자가 브라우저가 실행할 스크립트를 포스팅하면 어떻게 될까? 
 
Figure 1.6 A user posts a comment containing a script, on a web forum. The user defines the script such that it makes requests that try to post or get massive amounts of data from another application (App X), which represents the victim of the attack. If the web forum app allows cross-site scripting (XSS), all the users who display the page with the malicious comment receive it as it is.
 
Figure 1.7 Users access a page that displays a malicious script. Their browsers execute the script and then try to post or get substantial amounts of data from App X.

### 1.4.4 WHAT IS CROSS-SITE REQUEST FORGERY (CSRF)?
크로스 사이트 요청 위조(CSRF) 취약점도 일반적이다. CSRF 공격은 특정 서버의 액션을 호출하는 URL을 추출하여 애플리케이션 외부에서 사용되는 것을 가정한다. 서버가 요청의 오리진을 체크하지 않고 실행을 신뢰하면 다른 곳에서 온 것을 실행할 수 도 있다. CSRF를 통해 공격자는 원하지 않는 액션을 사용자가 실행하도록 할 수 있다. 이런 취약점으로 공격자는 시스템의 데이터를 수정하는 액션을 목표로 한다.
 
Figure 1.8 Steps of a cross-site request forgery (CSRF). After logging into their account, the user accesses a page that contains forgery code. The malicious code then executes actions on behalf of the unsuspecting user.
이러한 취약점을 완화하는 방법 중에 하나는 요청자를 식별하는 토큰 또는 크로스 오리진 리소스 공유(CORS)를 사용하는 것이다. 즉 요청의 오리진을 검증한다.

### 1.4.5 UNDERSTANDING INJECTION VULNERABILITIES IN WEB APPLICATIONS

시스템 상의 인젝션 공격은 널리 퍼져있다. 인젝션 공격에서 공격자가 채택한 취약점은 시스템에 특정한 데이터를 넣어 주는 것이다. 목적이 시스템에 위해를 가하는 것으로서 원치 않는 방식으로 데이터를 변경하거나 공격자가 접근하면 안되는 데이터를 조회하는 것이다.
여러가지 타입의 인젝션 공격이 있다. 앞서 언급한 XSS 조차도 인젝션 취약점으로 볼 수 있다. 결국 인젝션 공격은 시스템에 위해를 가하는 클라이언트 측 스크립트를 주입하는 것이다. 또 다른 타입으로는 SQL 인젝션, OS 명령어 인젝션, LDAP 인젝션 등이 있다.
Injection types of vulnerabilities are important, and the results of exploiting these can be change, deletion, or access to data in the systems being compromised. For example, if your application is somehow vulnerable to LDAP injection, an attacker could benefit from bypassing the authentication and from there control essential parts of the system. The same can happen for XPath or OS command injections.
One of the oldest and perhaps well-known types of injection vulnerability is SQL injection. If your application has an SQL injection vulnerability, an attacker can try to change or run different SQL queries to alter, delete, or extract data from your system. In the most advanced SQL injection attacks, an individual can run OS commands on the system, leading to a full system compromise.

### 1.4.6 DEALING WITH THE EXPOSURE OF SENSITIVE DATA

복잡성 측면에서 기밀 데이터의 공개가 가장 이해하기 쉽고 취약성 중 가장 덜 복잡한 것처럼 보이지만 가장 일반적인 실수 중 하나이다. 아마도 대부분의 튜토리얼과 예제들이 단순함을 이유로 설정 파일에 직접 자격 증명을 정의하기 때문이다. 다른 뭔가에 초점을 맞춘 가상의 예의 경우에는 의미가 있다.

> NOTE Most of the time, developers learn continuously from theoretical examples. Generally, examples are simplified to allow the reader to focus on a specific topic. But a downside of this simplification is that developers get used to wrong approaches. Developers might mistakenly think that everything they read is a good practice.

이 측면은 스프링 시큐리티와 어떻게 연관이 있을까? 여기서는 자격 증명과 프라이빗 키를 예제에서 다룰 것이다. 비밀 값을 설정 파일에서 사용하지만 이런 데이터는 금고에 저장해야 한다. 개발 시스템에서 개발자가 이런 데이터를 볼 수 없도록 해야 한다. 생산 시스템에도 최소한의 사람만 사설 데이터를 액세스할 수 있어야 한다.
그러한 값들을 설정 파일에 두면 소스 코드를 볼 수 있는 누구나 사설 값에 접근할 수 있다. 더구나 이러한 사설 값들의 변경 이력이 소스코드 저장소에서 볼 수가 있다. 또한 민감한 데이터 노출과 관련된 것은 컨솔이나 데이터베이스에 저장된 로그 정보이다. 개발자가 놓친 민감 데이터를 로그에서 발견하기도 한다.

> NOTE Never log something that isn’t public information. By public, I mean that anyone can see or access the info. Things like private keys or certificates aren’t public and shouldn’t be logged together with your error, warning, or info messages.
Next time you log something from your application, make sure what you log doesn’t look like one of these messages:
[error] The signature of the request is not correct. The correct key to be used should have been X.
[warning] Login failed for username X and password Y. User with username X has password Z.
[info] A login was performed with success by user X with password Y.

Be careful of what your server returns to the client, especially, but not limited to, cases where the application encounters exceptions. Often due to lack of time or experience, developers forget to implement all such cases. This way (and usually happening after a wrong request), the application returns too many details that expose the implementations.

This application behavior is also a vulnerability through data exposure. If your app encounters a NullPointerException because the request is wrong (part of it is missing, for example), then the exception shouldn’t appear in the body of the response. At the same time, the HTTP status should be 400 rather than 500. HTTP status codes of type 4XX are designed to represent problems on the client side. A wrong request is, in the end, a client issue, so the application should represent it accordingly. HTTP status codes of type 5XX are designed to inform you that there is a problem on the server. Do you see something wrong in the response presented by the next snippet?

```
{
    "status": 500,
    "error": "Internal Server Error",
    "message": "Connection not found for IP Address 10.2.5.8/8080",
    "path": "/product/add"
}
```
The message of the exception seems to be disclosing an IP address. An attacker can use this address to understand the network configuration and, eventually, find a way to control the VMs in your infrastructure. Of course, with only this piece of data, one can’t do any harm. But collecting different disclosed pieces of information and putting these together could provide everything that’s needed to adversely affect a system. Having exception stacks in the response is not a good choice either, for example:
```
➥ .runWorker(ThreadPoolExecutor.java:1128) ~[na:na]
at java.base/java.util.concurrent.ThreadPoolExecutor$Worker
➥ .run(ThreadPoolExecutor.java:628) ~[na:na]
at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable
➥ .run(TaskThread.java:61) ~[tomcat-embed-core-9.0.26.jar:9.0.26]
at java.base/java.lang.Thread.run(Thread.java:830) ~[na:na]
```

This approach also discloses the application’s internal structure. From the stack of an exception, you can see the naming notations as well as objects used for specific actions and the relationships among these. But even worse than that, logs sometimes can disclose versions of dependencies that your application uses. (Did you spot that Tomcat core version in the preceding exception stack?)

We should avoid using vulnerable dependencies. However, if we find ourselves using a vulnerable dependency by mistake, at least we don’t want to point this mistake out. Even if the dependency isn’t known as a vulnerable one, this can be because nobody has found the vulnerability yet. Exposures as in the previous snippet can motivate an attacker to find vulnerabilities in that specific version because they now know that’s what your system uses. It’s inviting them to harm your system. And an attacker often uses even the smallest detail against a system, for example:
```
Response A:
{
    "status": 401,
    "error": "Unauthorized",
    "message": "Username is not correct",
    "path": "/login "
}
Response B:
{
    "status": 401,
    "error": " Unauthorized",
    "message": "Password is not correct",
    "path": "/login "
}
```
In this example, the responses A and B are different results of calling the same authentication endpoint. They don’t seem to expose any information related to the class design or system infrastructure, but these hide another problem. If the messages disclose context information, then these can as well hide vulnerabilities. The different messages based on different inputs provided to the endpoint can be used to understand the context of execution. In this case, these could be used to know when a username is correct but the password is wrong. And this can make the system more liable to a brute force attack. The response provided back to the client shouldn’t help in identifying a possible guess of a specific input. In this case, it should have provided in both situations the same message:
```
{
    "status": 401,
    "error": " Unauthorized",
    "message": "Username or password is not correct",
    "path": "/login "
}
```
This precaution looks small, but if not taken, in some contexts, exposing sensitive data can become an excellent tool to be used against your system.

### 1.4.7 WHAT IS THE LACK OF METHOD ACCESS CONTROL?

애플리케이션 레벨에서도 계층 중 하나에만 권한 부여를 적용하지 않는다. 때로는 특정 유스케이스를 호출할 수 없도록 해야 한다.(예를 들어 현재 인증된 사용자의 권한으로는 허용하지 않는다면)
간단한 웹 애플리케이션이 있다고 하자. 앱에 엔드포인트를 노출하는 콘트롤러가 있고 컨트롤러는 일부 로직을 구현하면서 저장소를 통해 관리되는 데이터 서비스를 직접 호출한다. 엔드포인트 레벨에서만 권한 부여가 되는 상황을 상상해보자(REST 엔드포인트를 통해서 메소드를 액세스헐 수 있음). 개발자는 콘트롤러 계층에서만 권한 부여 규칙을 적용하고 싶을 것이다.
 
Figure 1.9 개발자는 권한 부여 규칙을 콘트롤러 레이어에 적용한다. 하지만 저장소는 사용자를 모르며 데이터 조회를 제약하지 않는다. 만일 서비스가 현재 승인된 사용자에 속하지 않은 계정을 요청받아도 이를 처리한다.
위의 그림의 경우가 정확히 작동하지만 콘트롤러 레벨에서 권한 부여 규칙의 적용은 에러의 소지를 남긴다. 이 경우에 앞으로의 구현에서 테스트 없이 노출이 될 수 있다. 그림 1.10은 같은 저장소에 의존하는 다른 기능을 추가하면 어떤 일이 생길 수 있는지를 확인할 수 있다.
이런 상황이 나타날 수 있으며 저장소 뿐만 아니라 어떤 계층에서도 처리할 수 있다. 
 
Figure 1.10 새로 추가한 TransactionController가 의존성 체인에서  AccountRepository를 사용한다. 개발자는 이 콘트롤러에도 권한 부여 규칙을 다시 적용해야 한다. 하지만 저장소 자체에서 인증된 사용자가 아니면 데이터를 노출하지 않도록 하는 것이 더 낫다.

### 1.4.8 USING DEPENDENCIES WITH KNOWN VULNERABILITIES

애플리케이션 레벨의 보안의 필수 측면에서 우리가 사용하는 의존성에 주의를 기울여야 한다. 취약점은 애플리케이션 자체가 아니라 빌드에 사용하는 라이브러리와 같은 종속성이다. 취약점이 있다고 알려진 라이브러리의 해당 버전은 제거해야 한다.
다행히 메이븐 또는 gradle 설정에 플러그인을 추가하여 빠르게 수행할 수 있는 정적 분석에 대한 여러가지 가능성이 있다. 오픈소스를 사용하는 개발 방법이 효과적이고 빠른 진화가 가능하지만 오류 발생 가능성이 높아질 수도 있다.

When developing any piece of software, we have to take all the needed measures to avoid the use of any dependency that has known vulnerabilities. If we discover that we’ve used such a dependency, then we not only have to correct this fast, we also have to investigate if the vulnerability was already exploited in our applications and then take the needed measures.

## 1.5 Security applied in various architectures

In this section, we discuss applying security practices depending on the design of your system. It’s important to understand that different software architectures imply different possible leaks and vulnerabilities. In this first chapter, I want to make you aware of the philosophy to which I’ll refer to throughout the book.

Architecture strongly influences choices in configuring Spring Security for your applications; so do functional and nonfunctional requirements. When you think of a tangible situation, to protect something, depending on what you want to protect, you use a metal door, bulletproof glass, or a barrier. You couldn’t just use a metal door in all the situations. If what you protect is an expensive painting in a museum, you still want people to be able to see it. You don’t, however, want them to be able to touch it, damage it, or even take it with them. In this case, functional requirements affect the solution we take for secure systems.
It could be that you need to make a good compromise with other quality attributes like, for example, performance. It’s like using a heavy metal door instead of a lightweight barrier at the parking entrance. You could do that, and for sure, the metal door would be more secure, but it takes much more time to open and close it. The time and cost of opening and closing the heavy door aren’t worth it; of course, assuming that this isn’t some kind of special parking for expensive cars.
Because the security approach is different depending on the solution we implement, the configuration in Spring Security is also different. In this section, we discuss some examples based on different architectural styles, that take into consideration various requirements that affect the approach to security. These aspects are linked to all the configurations that we’ll work on with Spring Security in the following chapters.
In this section, I present some of the practical scenarios you might have to deal with and those we’ll work through in the rest of the book. For a more detailed discussion on techniques for securing apps in microservices systems, I recommend you also read Microservices Security in Action by Prabath Siriwardena and Nuwan Dias (Manning, 2019).

### 1.5.1 DESIGNING A ONE-PIECE WEB APPLICATION

Let’s start with the case where you develop a component of a system that represents a web application. In this application, there’s no direct separation in development between the backend and the frontend. The way we usually see these kinds of applications is through the general servlet flow: the application receives an HTTP request and sends back an HTTP response to a client. Sometimes, we might have a server-side session for each client to store specific details over more HTTP requests. In the examples provided in the book, we use Spring MVC (figure 1.11).
You’ll find a great discussion about developing web applications and REST services with Spring in chapters 2 and 6 of Craig Walls’s Spring In Action, 6th ed. (Manning, 2020):

https://livebook.manning.com/book/spring-in-action-sixth-edition/chapter-2/
https://livebook.manning.com/book/spring-in-action-sixth-edition/chapter-6/
 
Figure 1.11 스프링 MVC 플로우. DispatcherServlet (1) 요청 경로의 콘트롤러 메소드 매핑을 찾는다 (2) 콘트롤러 메소드 실행 (3) 렌더 뷰를 얻음. 요청자에게 HTTP 응답을 전달하면 브라우저가 이를 표시한다.

일단 세션을 얻으면 CSRF는 물론 세션 고정 취약점의 가능성을 의심할 필요가 있다. 또한 HTTP 세션 자체에 저장된 것이 무엇인지도 확인해야 한다.
서버측 세션은 반영구적이다. 세션은 상태 저장 데이터이므로 수명이 더 길다. 메모리에 오래 상주할수록 이들이 조회 당할 확률이 더 많아진다. 예를 들어 힙 덤프를 접근할 수 있는 사람이 앱의 내부 메모리 정보를 읽을 수도 있다. 힙 덤프를 얻기 힘들다고 생각하지 말자. 특히 스프링 부트로 애플리케이션을 개발할 때 Actuator 역시 애플리케이션의 일부라는 것을 알수 있다. Actuator는 설정하기에 따라 엔드포인트 호출만 힙 덤프를 리턴할 수 있다. 즉 덤프를 얻기 위해 VM에 대한 루트 권한이 반드시 필요한 것은 아니다.

이 경우 CSRF 취약점으로 돌아가 보면 가장 완화할 수 있는 가장 쉬원 방법이 anti-CSRF 토큰을 사용하는 것이다. 다행히 스프링에서는 이 기능을 제공한다. CSRF 보호는 CORS 오리진 검증처럼 기본적으로 활성화되어 있다. 사용자 인증과 권한 부여를 위해 암묵적인 로긴 폼을 사용할지를 선택할 수 있다. 이를 통해 로그인 및 로그아웃의 룩앤필은 무시하더라도 인증 및 권한 부여 구성과의 기본 통합하는 이점을 얻을 수 있다. 또한 세션 고정 취약점도 완화할 수 있다.
인증과 권한부여를 구현한다면 이는 유효한 자격이 있는 사용자가 있다는 것을 의미한다. 애플리케이션이 사용자 자격 증명을 관리하거나 다른 시스템이 이를 하도록 선택할 수 있다(예를 들어 Facebook, Google의 자격 증명). 어떤 경우든 스프링 시큐리티는 사용자 관리 설정을 비교적 쉬운 방법으로 도와준다. 사용자 정보를 데이터베이스에 저장하거나 웹 서비스를 사용하거나 또는 다른 플랫폼에 연결하는 것을 선택할 수 있다.

### 1.5.2 DESIGNING SECURITY FOR A BACKEND/FRONTEND SEPARATION

요즘에는 웹 애플리케이션 개발에서 프론트엔드와 백엔드를 분리되어 있는 것을 자주 본다. 프론트 엔드는 REST 엔드포인트로 백엔드와 통신한다. 우리는 가능하면 서버측 세션은 피하고 클라이언트측 세션으로 대치할 것이다. 이러한 방식의 시스템 설계는 모바일 개발과 유사하다. 
 
Figure 1.12 The browser executes a frontend application. This application calls REST endpoints exposed by the backend to perform some operations requested by the user.

보안 측면에서는 또다른 고려 사항이 있다. 첫째로 CSRF와 CORS 설정이 좀 더 복잡해진다. 시스템을 수평으로 확장하기 원하지만 반드시 프론트엔드와 백엔드가 같은 오리진이 아닐 수 있다.

가장 단순하지만 실용적 솔루션으로서 최소한의 바람직한 방법은 엔드포인트 인증에 HTTP Basic을 사용하는 것이다. 이 방식이 이해하기 쉽고 일반적으로 인증에 관한 첫번째 이론적 예제로 사용되지만 피하고 싶은 누출이 있다. 예를 들어 HTTP Basic은 각 호출에 자격 증명을 보낸다는 의미이다. 2장에서 보면 자격 증명은 암호화되어 있지 않다. 브라우저가 username과 password를 Base64 인코딩으로 보낸다. 이 방식에서 자격 증명는 각 엔드포인트 호출의 헤더에서 네트워크에 남아 있다. 또한 자격 증명이 로그인 한 사용자를 나타낸다면 여러분은 사용자가 모든 요청마다 자격 증명을 체출하라고 원하지는 않는다. 또한 자격 증명을 클라이언트 측에 저장하길 원하지도 않는다. 이러한 관행은 권장하지 않는다.

Having the stated reasons in mind, chapter 12 gives you an alternative for authentication and authorization that offers a better approach, the OAuth 2 flow, and the following section provides an overview of this approach.

> A short reminder of application scalability

Scalability refers to the quality of a software application in which it can serve more or fewer requests while adapting the resources used, without the need to change the application or its architecture. Mainly, we classify scalability into two types: vertical and horizontal.
When a system is scaled vertically, the resources of the system on which it executes are adapted to the need of the application (for example, when there are more requests, more memory and processing power are added to the system).
We accomplish horizontal scalability by changing the number of instances of the same application that are in execution (for example, if there are more requests, another instance is started to serve the increased need). Of course, I assume the newly spun-up application instances consume resources offered by additional hardware, sometimes even in multiple data centers. If the demand decreases, we can reduce the instance numbers.

### 1.5.3 UNDERSTANDING THE OAUTH 2 FLOW

이 절은 Oauth2 플로우 전반을 고수준으로 설명한다. Oauth2를 적용하는 이유와 취약점에 어떻게 관련되는지에 초점을 둔다. 각 요청마다 백엔드에게 자격 증명을 다시 보내지 않으며 클라이언트 측에 저장하지 않는 방법을 다룬다. Oauth2 플로우는 이 경우에 인증과 권한 부여를 구현하는데 더 나은 방법을 제공한다.
Oauth2 프레임워크는 인증 서버와 리소스 서버를 정의한다. 인증 서버의 목적은 사용자를 인증하고 그 사용자가 사용할 수 있는 특권을 명시한 토큰을 제공한다. 이 기능을 구현한 백엔드 부분을 리소스 서버라고 한다. 호출이 가능한 엔드포인트를 보호된 리소스라고 여긴다. 획득한 토큰을 토대로 인증을 수행한 후에는 리소스에 대한 호출이 허용되거나 거부된다. 그림 1.13은 표준적인 Oauth2 인증 플로우를 나타낸다. 단계별로 다음과 같이 진행한다.

1.	사용자가 애플리케이션의 유스케이스를 액세스한다(also known as the client). 애플리케이션은 백엔드의 리소스를 호출해야 한다.
2.	리소스를 호출할 수 있으려면 먼저 액세스 토큰을 획득해야 하므로 인증 서버에게 요청한다. 요청에서 사용자 자격증명 또는 상황에 따라 리프레시 토큰을 보낸다.
3.	자격 증명 또는 리프레시 토큰이 맞으면 인증 서버는 액세스 토큰을 리턴한다.
4.	리소스 서버에 대한 요청의 헤더에 액세스 토큰을 사용한다.
 
Figure 1.13 암호 그랜트 타입의 OAuth2 인증 플로우. (1) 사용자가 요청한 액션을 실행 (2) 인증 서버에게 액세스 토큰을 요청. (3) 토큰을 받음 (4) 액세스 토큰으로 리소스 서버로부터 리소스를 액세스.

토큰은 사무실 빌딩 내부에서 사용하는 출입 카드와 비슷한다. 당신이 방문자라면 먼저 프론트에서 신분 증명을 한 후 출입카드를 받는다. 출입 카드로 어떤 문은 열 수 있지만 모든 문을 열 수 있을 필요는 없다. 액세스 토큰도 마찬가지이다. 인증 후에 호출자에게 토튼을 발급하며 이를 근거로 열람 권한이 있는 리소스를 액세스할 수 있다.  
토큰은 일정한 수명이 있으며 대개는 짧다. 토큰의 유효기간이 끝나면 앱은 새것을 얻어야 한다. 그런 경우 서버는 이전의 토큰은 폐기해야 한다. 이런 방식의 장점은 다음과 같다.

- 클라이언트는 사용자 자격 증명을 저장할 필요가 없다. 액세스 토큰이 유일하게 저장할 필요가 있는 액세스 정보이다.
- 애플리케이션은 사용자 자격 증명을 네트워크 상에 노출하지 않는다.
- 누군가 토큰을 탈취하면 사용자 자격증명 대신 토큰을 폐기하면 된다.
- 토큰은 사용자 대신 리소스를 액세스하는 제3의 엔티티로 사용할 수 있다. 물론 공격자가 토큰을 탈취할 수 있다. 하지만 토큰의 수명은 정해져 있으므로 이 
- 취약점을 사용할 수 있는 기간이 제한적이다.

> NOTE To make it simple and only give you an overview, I’ve described the OAuth 2 flow called the password grant type. OAuth 2 defines multiple grant types and, as you’ll see in chapters 12 through 15, the client application does not always have the credentials. If we use the authorization code grant, the application redirects the authentication in the browser directly to a login implemented by the authorization server. But more on this later in the book.

물론 Oauth2 플로우 조차도 모든 것이 완벽하것은 아니며 애플리케이션 설계에 알맞게 조정할 필요가 있다. 나올 수 있는 질문 중에는 이 토큰의 관리하는 최상의 방법은 무엇인가? 이다. 우리는 여러가지 가능성을 다룬다.

- 토큰을 앱의 메모리에 저장
- 토큰을 데이터베이스에 저장
- JWT로 암호화된 시그니처를 사용

### 1.5.4 USING API KEYS, CRYPTOGRAPHIC SIGNATURES, AND IP VALIDATION TO SECURE REQUESTS

어떤 경우에서는 호출자의 인증과 권한 부여를 위해 사용자 이름과 암호가 필요 없지만 아무도 교환된 메시지를 변경하지 않았음을 확신하길 원한다. 이 방식은 요청이 두 백엔드 컴포넌트 간에 이루어질 때 필요하다. 때로는 이들 사이의 메시지가 어떻게 든 유효성이 검증되었는지를 확인하길 원한다. 이를 위한 몇가지 지침에는

- 요청과 응답 헤더에 정적 키를 사용
- 암호화된 시그니처로 응답과 요청에 사인하기
- IP 주소에 대한 검증을 적용
- 
정적 키를 사용하는 것이 가장 약한 방법이다. 요청과 응답 헤더에서 키를 사용한다. 요청과 응답의 헤더 값이 부정확하면 수락하지 않는다. 물론 이것은 키 값을 네트워크에서 교환한다는 것을 가정한다. 트래픽이 데이터 센터 외부로 나가면 탈취되기 쉽다. 키 값을 획득한 누군가는 엔트포인트 호출이 가능해진다. 이 방식을 사용하려면 허용하는 IP 주소와 병행한다.
통신의 신뢰성을 테스트하는 더 나은 방법은 암호화된 시그니처를 사용하는 것이다. 이 방식에서 키는 요청과 응답을 사인하는데 사용한다. 키를 네트워크 상에 전송할 필요가 없다는 점은 정적 인증 값 방식보다 장점이다. 각자는 그 들의 키로 시그니처를 검증할 수 있다. 두 개의 비대칭 키 쌍으로 구현할 수 있다. 이 방식은 우리가 비밀 키를 절대로 교환하지 않음을 가정한다. 더 간단한 버전은 대칭키를 사용하는 것이며 설정에서 한번만 교환하면 된다. 단점으로는 시그니처의 계산에 리소스를 더 소비한다는 점이다. 
 
Figure 1.14 To make a successful call to another backend, the request should havethe correct signature or shared key.
If you know an address or range of addresses from where a request should come, then together with one of the solutions mentioned previously, IP address validation can be applied. This method implies that the application rejects requests if coming from IP addresses other than the ones that you configure to be accepted. However, in most cases, IP validation is not done at the application level but much earlier, in the networking layer.

## 1.6 What will you learn in this book?

This book offers a practical approach to learning Spring Security. Throughout the rest of the book, we’ll deep dive into Spring Security, step by step, proving concepts with simple to more complex examples. To get the most out of this book, you should be comfortable with Java programming, as well as with the basics of the Spring Framework. If you haven’t used the Spring Framework or you don’t feel comfortable yet using its basics, I recommend you first read Spring In Action, 6th ed., by Craig Walls (Manning, 2020). Another great resource is Spring Boot In Action by Craig Walls (Manning, 2015). But in this book, you’ll learn

- 스프링 시큐리티의 아키텍처와 기본 컴포넌트와 애플리케이션 보호를 위한 사용 방법
- 스프링 시큐리티로 인증과 권한 부여. Oauth2와 OpenID Connect 플로우 그리고 제품에 적용하는 방법
- 애플리케이션의 다른 레이어에 시큐리티를 구현하는 방법
- Different configuration styles and best practices for using those in your project
- 리액티브 애플리케이션에서 Spring Security
- 시큐리티 테스팅

To make the learning process smooth for each described concept, we’ll work on multiple simple examples. At the end of each significant subject, we’ll review the essential concepts you’ve learned with a more complex application in chapters whose titles begin with “Hands-On.”
When we finish, you’ll know how to apply Spring Security for the most practical scenarios and understand where to use it and its best practices. I also strongly recommend that you work on all the examples that accompany the explanations.

## Summary

- Spring Security is the leading choice for securing Spring applications. It offers a significant number of alternatives that apply to different styles and architectures.
- You should apply security in layers for your system, and for each layer, you need to use different practices.
- Security is a cross-cutting concern you should consider from the beginning of a software project.
- Usually, the cost of an attack is higher than the cost of investment in avoiding vulnerabilities to begin with.
- The Open Web Application Security Project is an excellent place to refer to when it comes to vulnerabilities and security concerns.
- Sometimes the smallest mistakes can cause significant harm. For example, exposing sensitive data through logs or error messages is a common way to introduce vulnerabilities in your application.

