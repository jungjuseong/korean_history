# 2. Designing Security for APIs

Just a few days after everyone celebrated Thanksgiving Day in 2013, someone who fooled the Target defense system installed a malware in its security and payment system. It was the peak time in business for any retailer in the United States. While the customers were busy in getting ready for Christmas, the malware which was sitting in the Target payment system silently captured all the credit card information from the cashier’s terminal and stored them in a server, which was under the control of the attacker. Forty million credit card numbers were stolen in this way from 1797 Target stores around the country.1 It was a huge breach of trust and credibility from the retailer, and in March 2015 a federal judge in St. Paul, Minnesota, approved a $10 million offer by Target to settle the lawsuit against the data breach.2

Not just Target or the retail industry but as a whole, the cybercrime has gained a lot of momentum in the last few years. Figure 2-1 shows the annual number of data breaches and exposed records in the United States from 2005 to 2018. The attack on Dyn DNS in 2016 was one of the largest DDoS (distributed denial of service) attacks that took many large Internet services down for several hours. Then in February 2018, the largest recorded DDoS attack happened against GitHub. More than 1.35 terabits per second of traffic hit the developer platform GitHub all at once.3

Figure 2-1 Annual number of data breaches and exposed records in the United States from 2005 to 2018 (in millions), Statistica, 2019

Identity Theft Resource Center4 defines a data breach as the loss of information from computers or storage media that could potentially lead to identity theft, including social security numbers, bank account details, driving license numbers, and medical information. The most worrisome fact is that, according to an article5 by The Economist magazine, the average time between an attacker breaching a network and its owner noticing the intrusion is 205 days.

## Trinity of Trouble

Connectivity, extensibility, and complexity are the three trends behind the rise of data breaches around the globe in the last few years. Gary McGraw in his book, Software Security,6 identifies these three trends as the trinity of trouble.

APIs play a major role in connectivity. As we discussed in detail, in Chapter 1, we live in a world today where almost everything is connected with each other. Connectivity exposes many paths of exploitation for attackers, which never existed before. Login to Yelp, Foursquare, Instagram, and many more via Facebook means an attacker only needs to worry about compromising one’s Facebook account to get access to his/her all other connected accounts.

> Facebook Data Breach ~ September 2018
>
> In September 2018, Facebook team figured out an attack,7 which put the personal information of more than 50 million Facebook users at risk. The attackers exploited multiple issues on Facebook code base around the View As feature and got hold of OAuth 2.0 access tokens that belong to more than 50 million users. Access token is some kind of a temporary token or a key, which one can use to access a resource on behalf of someone else. Say, for example, if I want to share my photos uploaded to Instagram on my Facebook wall, I would give an access token corresponding to my Facebook wall, which I obtained from Facebook, to Instagram. Now, at each time when I upload a photo to Instagram, it can use the access token to access my Facebook account and publish the same on my Facebook wall using the Facebook API. Even though Instagram can post photos on my Facebook wall using the provided access token, it cannot do anything else other than that. For example, it cannot see my friend list, cannot delete my wall posts, or read my messages. Also, this is usually what happens when you log in to a third-party application via Facebook; you simply share an access token corresponding to your Facebook account with the third-party web application, so the third-party web application can use the access token to access the Facebook API to know more about you.

In a connected enterprise, not just the applications developed with modern, bleeding edge technology get connected but also the legacy systems. These legacy systems may not support latest security protocols, even Transport Layer Security (TLS) for securing data in transit. Also, the libraries used in those systems could have many well-known security vulnerabilities, which are not fixed due to the complexities in upgrading to the latest versions. All in all, a connected system, not planned/designed quite well, could easily become a security graveyard.

Most of the enterprise software are developed today with great extensibility. Extensibility over modification is a well-known design philosophy in the software industry. It talks about building software to evolve with new requirements, without changing or modifying the current source code, but having the ability to plug in new software components to the current system. Google Chrome extensions and Firefox add-ons all follow this concept. The Firefox add-on, Modify Headers, lets you add, modify, and filter the HTTP request headers sent to web servers. Another Firefox add-on, SSO Tracer, lets you track all the message flows between identity providers and service providers (web applications), via the browser. None of these are harmful—but, then again, if an attacker can fool you to install a malware as a browser plugin, it could easily bypass all your browser-level security protections, even the TLS, to get hold of your Facebook, Google, Amazon, or any other web site credentials. It’s not just about an attacker installing a plugin into the user’s browser, but also when there are many extensions installed in your browser, each one of them expands the attack surface. Attackers need not write new plugins; rather they can exploit security vulnerability in an already installed plugin.

> The Story of Mat Honan
>
> It was a day in August 2012. Mat Honan, a reporter for Wired magazine, San Francisco, returned home and was playing with his little daughter.8 He had no clue what was going to happen next. Suddenly his iPhone was powered down. He was expecting a call—so he plugged it into a wall power socket and rebooted back. What he witnessed next blew him away. Instead of the iPhone home screen with all the apps, it asked for him to set up a new phone with a big Apple logo and a welcome screen. Honan thought his iPhone was misbehaving—but was not that worried since he backed up daily to the iCloud. Restoring everything from iCloud could simply fix this, he thought. Honan tried to log in to iCloud. Tried once—failed. Tried again—failed. Again—failed. Thought he was excited. Tried once again for the last time, and failed. Now he knew something weird has happened. His last hope was his MacBook. Thought at least he could restore everything from the local backup. Booted up the MacBook and found nothing in it—and it prompted him to enter a four-digit passcode that he has never set up before.
>
> Honan called Apple tech support to reclaim his iCloud account. Then he learned he has called Apple, 30 minutes before, to reset his iCloud password. The only information required at that time to reset an iCloud account was the billing address and the last four digits of the credit card. The billing address was readily available under the whois Internet domain record Honan had for his personal web site. The attacker was good enough to get the last four digits of Honan’s credit card by talking to Amazon helpdesk; he already had Honan’s email address and the full mailing address—those were more than enough for a social engineering attack.
>
> Honan lost almost everything. The attacker was still desperate—next he broke into Honan’s Gmail account. Then from there to his Twitter account. One by one—Honan’s connected identity falls into the hands of the attacker.

The complexity of the source code or the system design is another well-known source of security vulnerabilities. According to a research, after some point, the number of defects in an application goes up as the square of the number of the lines of code.9 At the time of this writing, the complete Google codebase to run all its Internet services was around 2 billion lines of code, while Microsoft Windows operating system had around 50 million lines of code.10 As the number of lines of code goes high, the number of tests around the code should grow as well, to make sure that none of the existing functionalities are broken and the new code works in the expected way. At Nike, 1.5 million lines of test code is run against 400,000 lines of code.11

## Design Challenges

Security isn’t an afterthought. It has to be an integral part of any development project and also for APIs. It starts with requirements gathering and proceeds through the design, development, testing, deployment, and monitoring phases. Security brings a plethora of challenges into the system design. It’s hard to build a 100% secured system. The only thing you can do is to make the attacker’s job harder. This is in fact the philosophy followed while designing cryptographic algorithms. The following discusses some of the key challenges in a security design.

> **MD5**
>
> MD512 algorithm, which was designed in 1992, was accepted to be a strong hashing algorithm. One of key attributes of a hashing algorithm is, given the text, the hash corresponding to that text can be generated, but, given a hash, the text corresponding to the hash cannot be derived. In other words, hashes are not reversible. If the text can be derived from a given hash, then that hashing algorithm is broken.
>
> The other key attribute of a hashing algorithm is that it should be collision-free. In other words, any two distinct text messages must not result in the same hash. The MD5 design preserved both of these two properties at the time of its design. With the available computational power, it was hard to break MD5 in the early 1990s. As the computational power increased and it was made available to many people via cloud-based infrastructure as a service (IaaS) providers, like Amazon, MD5 was proven to be insecure. On March 1, 2005, Arjen Lenstra, Xiaoyun Wang, and Benne de Weger demonstrated that MD5 is susceptible to hash collisions.13

### User Experience

The most challenging thing in any security design is to find and maintain the right balance between security and the user comfort. Say you have the most complex password policy ever, which can never be broken by any brute-force attack. A password has to have more than 20 characters, with mandatory uppercase and lowercase letters, numbers, and special characters. Who on Earth is going to remember their passwords? Either you’ll write it on a piece of paper and keep it in your wallet, or you’ll add it as a note in your mobile device. Either way, you lose the ultimate objective of the strong password policy. Why would someone carry out a brute-force attack when the password is written down and kept in a wallet? The principle of psychological acceptability, discussed later in this chapter, states that security mechanisms should not make the resource more difficult to access than if the security mechanisms were not present. We have few good examples from the recent past, where user experience drastically improved while keeping security intact. Today, with the latest Apple Watch, you can unlock your MacBook, without retyping the password. Also the face recognition technology introduced in the latest iPhones lets you unlock the phone, just by looking at it. You never even notice that the phone was locked.

> It is essential that the human interface be designed for ease of use, so that users routinely and automatically apply the protection mechanisms correctly. Also, to the extent that the user's mental image of his protection goals matches the mechanisms he must use, mistakes will be minimized. If he must translate his image of his protection needs into a radically different specification language, he will make errors.
>
> —Jerome Saltzer and Michael Schroeder

### Performance

Performance is another key criterion. What is the cost of the overhead you add to your business operations to protect them from intruders? Say you have an API secured with a key, and each API call must be digitally signed. If the key is compromised, an attacker can use it to access the API. How do you minimize the impact? You can make the key valid only for a very short period; so, whatever the attacker can do with the stolen key is limited to its lifetime. What kind of impact will this have on legitimate day-to-day business operations? Each client application should first check the validity period of the key (before doing the API call) and, if it has expired, make a call to the authorization server (the issuer of the key) to generate a new key. If you make the lifetime too short, then almost for each API call, there will be a call to the authorization server to generate a new key. That kills performance—but drastically reduces the impact of an intruder getting access to the API key.

The use of TLS for transport-level security is another good example. We will be discussing TLS in Appendix C, in detail. TLS provides protection for data in transit. When you pass your login credentials to Amazon or eBay, those are passed over a secured communication channel, or HTTP over TLS, which is in fact the HTTPS. No one in the middle will be able to see the data passed from your browser to the web server (assuming there is no room for a man-in-the-middle attack). But this comes at a cost. TLS adds more overhead over the plain HTTP communication channel, which would simply slow down things a bit. For the exact same reason, some enterprises follow the strategy where all of the communication channels open to the public are over HTTPS, while the communication between internal servers are over plain HTTP. They make sure no one can intercept any of those internal channels by enforcing strong network-level security. The other option is to use optimized hardware to carry out the encryption/decryption process in the TLS communication. Doing encryption/decryption process at the dedicated hardware level is far more cost-effective than doing the same at the application level, in terms of performance.

Even with TLS, the message is only protected while it is in transit. As soon as the message leaves the transport channel, it’s in cleartext. In other words, the protection provided by TLS is point to point. When you log in to your banking web site from the browser, your credentials are only secured from your browser to the web server at your bank. If the web server talks to a Lightweight Directory Access Protocol (LDAP) server to validate the credentials, once again if this channel is not explicitly protected, then the credentials will be passed in cleartext. If anyone logs all the in and out messages to and from the bank’s web server, then your credentials will be logged in plaintext. In a highly secured environment, this may not be acceptable. Using message-level security over transport-level security is the solution. With message-level security, as its name implies, the message is protected by itself and does not rely on the underlying transport for security. Since this has no dependency on the transport channel, the message will be still protected, even after it leaves the transport. This once again comes at a high performance cost. Using message-level protection is much costlier than simply using TLS. There is no clear-cut definition on making a choice between the security and the performance. Always there is a compromise, and the decision has to be taken based on the context.

### Weakest Link

A proper security design should care about all the communication links in the system. Any system is no stronger than its weakest link. In 2010, it was discovered that since 2006, a gang of robbers equipped with a powerful vacuum cleaner had stolen more than 600,000 euros from the Monoprix supermarket chain in France.14 The most interesting thing was the way they did it. They found out the weakest link in the system and attacked it. To transfer money directly into the store’s cash coffers, cashiers slid tubes filled with money through pneumatic suction pipes. The robbers realized that it was sufficient to drill a hole in the pipe near the trunk and then connect a vacuum cleaner to capture the money. They didn’t have to deal with the coffer shield.

Not always, the weakest link in a system is either a communication channel or an application. There are many examples which show the humans have turned out to be the weakest link. The humans are the most underestimated or the overlooked entity in a security design. Most of the social engineering attacks target humans. In the famous Mat Honan’s attack, calling to an Amazon helpdesk representative, the attacker was able to reset Mat Honan’s Amazon credentials. The October 2015 attack on CIA Director John Brennan’s private email account is another prime example of social engineering.15 The teen who executed the attack said, he was able to fool a Verizon worker to get Brennan’s personal information and duping AOL into resetting his password. The worst side of the story is that Brennan has used his private email account to hold officially sensitive information—which is again a prime example of a human being the weakest link of the CIA defense system. Threat modeling is one of the techniques to identify the weakest links in a security design.

### Defense in Depth

A layered approach is preferred for any system being tightened for security. This is also known as defense in depth. Most international airports, which are at a high risk of terrorist attacks, follow a layered approach in their security design. On November 1, 2013, a man dressed in black walked into the Los Angeles International Airport, pulled a semi-automatic rifle out of his bag, and shot his way through a security checkpoint, killing a TSA screener and wounding at least two other officers.16 This was the first layer of defense. In case someone got through it, there has to be another to prevent the gunman from entering a flight and taking control. If there had been a security layer before the TSA, maybe just to scan everyone who entered the airport, it would have detected the weapon and probably saved the life of the TSA officer.

NSA of the United States identifies defense in depth as a practical strategy for achieving information assurance in today’s highly networked environments.17 It further explains layered defense under five classes of attack: passive monitoring of communication channels, active network attacks, exploitation of insiders, close-in attacks, and attacks through various distribution channels. The link and network layer encryption and traffic flow security is proposed as the first line of defense for passive attacks, and the second line of defense is the security-enabled applications. For active attacks, the first line of defense is the enclave boundaries, while the second line of defense is the computing environment. The insider attacks are prevented by having physical and personnel security as the first line of defense and having authentication, authorization, and audits as the second line of defense. The close-in attacks are prevented by physical and personnel security as the first layer and having technical surveillance countermeasures as the second line of defense. Adhering to trusted software development and distribution practices and via runtime integrity controls prevents the attacks via multiple distributed channels.

The number of layers and the strength of each layer depend on which assets you want to protect and the threat level associated with them. Why would someone hire a security officer and also use a burglar alarm system to secure an empty garage?

### Insider Attacks

Insider attacks are less complicated, but highly effective. From the confidential US diplomatic cables leaked by WikiLeaks to Edward Snowden’s disclosure about the National Security Agency’s secret operations, all are insider attacks. Both Snowden and Bradley Manning were insiders who had legitimate access to the information they disclosed. Most organizations spend the majority of their security budget to protect their systems from external intruders; but approximately 60% to 80% of network misuse incidents originate from inside the network, according to the Computer Security Institute (CSI) in San Francisco.

There are many prominent insider attacks listed down in the computer security literature. One of them was reported in March 2002 against the UBS Wealth Management firm in the United States. UBS is a global leader in wealth management having branches over 50 countries. Roger Duronio, one of the system administrators at UBS, found guilty of computer sabotage and securities fraud for writing, planting, and disseminating malicious code that took down up to 2000 servers. The US District Court in Newark, New Jersey, sentenced him for 97 months in jail.18 The Target data breach that we discussed at the beginning of the chapter is another prime example for an insider attack. In that case, even the attackers were not insiders, they gained access to the Target internal system using the credentials of an insider, who is one of the company’s refrigeration vendors.

According to an article by Harvard Business Review (HBR),19 at least 80 million insider attacks take place in the United States each year. HBR further identifies three causes for the growth of insider attacks over the years:

- One is the dramatic increase in the size and the complexity of IT. As companies grow in size and business, a lot of isolated silos are being created inside. One department does not know what the other does. In 2005 call center staffers based in Pune, India, defrauded four Citibank account holders in New York of nearly $350,000, and later it was found those call center staffers are outsourced employees of Citibank itself and had legitimate access to customers’ PINs and account numbers.

- The employees who use their own personal devices for work are another cause for the growing insider threats. According to a report released by Alcatel-Lucent in 2014, 11.6 million mobile devices worldwide are infected at any time.20 An attacker can easily exploit an infected device of an insider to carry out an attack against the company.

- The third cause for the growth of insider threats, according to the HBR, is the social media explosion. Social media allow all sorts of information to leak from a company and spread worldwide, often without the company’s knowledge.

Undoubtedly, insider attacks are one of the hardest problems to solve in a security design. These can be prevented to some extent by adopting robust insider policies, raising awareness, doing employee background checks at the point of hiring them, enforcing strict processes and policies on subcontractors, and continuous monitoring of employees. In addition to these, SANS Institute also published a set of guidelines in 2009 to protect organizations from insider attacks.21

> Note
>
> Insider attacks are identified as a growing threat in the military. To address this concern, the US Defense Advanced Research Projects Agency (DARPA) launched a project called Cyber Insider Threat (CINDER) in 2010. The objective of this project was to develop new ways to identify and mitigate insider threats as soon as possible.

### Security by Obscurity

Kerckhoffs’ principle22 emphasizes that a system should be secured by its design, not because the design is unknown to an adversary. One common example of security by obscurity is how we share door keys between family members, when there is only a single key. Everyone locks the door and hides the key somewhere, which is known to all the other family members. The hiding place is a secret, and it is assumed only family members know about it. In case if someone can find the hiding place, the house is no more secured.

Another example for security by obscurity is Microsoft’s NTLM (an authentication protocol) design. It was kept secret for some time, but at the point (to support interoperability between Unix and Windows) Samba engineers reverse-engineered it, they discovered security vulnerabilities caused by the protocol design itself. Security by obscurity is widely accepted as a bad practice in computer security industry. However, one can argue it as another layer of security before someone hits the real security layer. This can be further explained by extending our first example. Let’s say instead of just hiding the door key somewhere, we put it to a lock box and hide it. Only the family members know the place where the lock box is hidden and also the key combination to open the lock box. The first layer of defense is the location of the box, and the second layer is the key combination to open the lock box. In fact in this case, we do not mind anyone finding the lock box, because finding the lock box itself is not sufficient to open the door. But, anyone who finds the lock box can break it to get the key out, rather than trying out the key combination. In that case, security by obscurity adds some value as a layer of protection—but it’s never good by its own.

## Design Principles

Jerome Saltzer와 Michael Schroeder는 정보 보안 영역에서 가장 널리 인용되는 연구 논문 중 하나를 작성했습니다. 이 논문에 따르면 제공된 기능 수준에 관계없이 보호 메커니즘 세트의 효과는 시스템이 다음을 수행할 수 있는지 여부에 달려 있습니다. 보안 위반을 방지합니다. 대부분의 경우 모든 무단 작업을 방지하는 기능 수준에서 시스템을 구축하는 것은 매우 어려운 것으로 판명되었습니다. 고급 사용자의 경우 시스템을 충돌시켜 다른 인증된 사용자가 시스템에 액세스하는 것을 방지하는 방법을 하나 이상 찾는 것은 어렵지 않습니다. 다수의 다양한 범용 시스템과 관련된 침투 테스트는 사용자가 내부에 저장된 정보에 대한 무단 액세스를 얻기 위해 프로그램을 구축할 수 있음을 보여주었습니다. 보안을 최우선으로 설계하고 구현한 시스템에서도 설계 및 구현 결함으로 인해 의도한 액세스 제한을 우회할 수 있습니다. 결함을 체계적으로 배제할 수 있는 설계 및 구축 기술이 많은 연구 활동의 주제이지만 Jerome과 Michael에 따르면 1970년대 초반에는 대규모 범용 시스템 구축에 적용할 수 있는 완전한 방법이 없었습니다. 이 백서에서 Jerome Saltzer와 Michael Schroeder는 다음 섹션에서 설명하는 것처럼 컴퓨터 시스템의 정보 보안을 위한 8가지 설계 원칙을 더욱 강조합니다.

### 최소 권한의 원칙

최소 권한의 원칙은 엔터티가 권한이 부여된 작업을 수행하는 데 필요한 권한 집합만 가져야 하며 더 이상은 없어야 한다는 것입니다. 필요에 따라 권한을 추가할 수 있으며 더 이상 사용하지 않을 때는 권한을 취소해야 합니다. 이는 사고나 오류로 인해 발생할 수 있는 손상을 제한합니다. 최소 특권 철학을 따르는 알아야 할 필요성은 군사 보안 분야에서 인기가 있습니다. 이는 누군가가 정보에 액세스하는 데 필요한 모든 보안 인가 수준을 가지고 있더라도 실제/입증된 필요가 없는 한 액세스 권한을 부여해서는 안 된다는 의미입니다.

불행히도, 이 원칙은 Edward Snowden의 경우에 적용되지 않았거나 그는 이를 해결할 만큼 충분히 영리했습니다. NSA(미국 국가안보국)에서 하와이의 계약자로 근무한 Edward Snowden은 약 170만 개의 기밀 NSA 파일에 액세스하고 복사하는 데 정교하지 않은 기술을 사용했습니다. 그는 NSA의 직원이었고 다운로드한 모든 정보에 합법적으로 액세스할 수 있었습니다. Snowden은 Google의 `Googlebot`(웹에서 문서를 수집하여 Google 검색 엔진에 대한 검색 가능한 색인을 구축함)과 유사한 간단한 웹 크롤러를 사용하여 NSA의 내부 위키 페이지에서 모든 데이터를 크롤링하고 긁었습니다. 시스템 관리자로서 Snowden의 역할은 컴퓨터 시스템을 백업하고 정보를 로컬 서버로 옮기는 것이었습니다. 그는 데이터의 내용을 알 필요가 없었습니다.

`ISO 27002`는 또한 최소 권한 원칙을 강조합니다. ISO 27002 표준은 정보 보안 영역에서 널리 사용되는 잘 알려진 표준입니다. 원래 영국 표준 기관에서 개발했으며 BS7799라고 불렀고 이후 ISO에서 승인하여 2000년 12월 제목으로 출판했습니다. ISO 27002에 따르면 권한은 필요한 경우 개인에게 할당되어야 합니다. - 사용 기반 및 이벤트별로, 즉 필요할 때만 기능적 역할에 대한 최소 요구 사항. 또한 시작에 대한 "제로 액세스"의 개념을 식별합니다. 이는 액세스가 없거나 사실상 액세스가 없음을 기본으로 하여 모든 후속 액세스 및 최종 누적이 승인 프로세스를 통해 역추적될 수 있음을 나타냅니다.25

### 페일 세이프 기본 원칙

페일 세이프 기본 원칙은 기본적으로 시스템을 안전하게 만드는 것의 중요성을 강조합니다. 시스템의 모든 리소스에 대한 사용자의 기본 액세스 수준은 명시적으로 "허용"이 부여되지 않은 한 `거부`되어야 합니다. 페일 세이프 설계는 시스템이 실패해도 시스템을 위험에 빠뜨리지 않습니다. `Java Security Manager` 구현은 이 원칙을 따릅니다. 일단 연결되면 명시적으로 허용되지 않는 한 시스템의 어떤 구성 요소도 권한 있는 작업을 수행할 수 없습니다. 방화벽 규칙은 또 다른 예입니다. 데이터 패킷은 명시적으로 허용된 경우에만 방화벽을 통해 허용됩니다. 그렇지 않으면 기본적으로 모든 것이 거부됩니다.

모든 복잡한 시스템에는 실패 모드가 있습니다. 실패는 피할 수 없으며 시스템 실패의 일부로 보안 위험이 잠식되지 않도록 계획해야 합니다. 실패 가능성은 보안 설계 철학인 심층 방어에 따른 가정입니다. 장애가 예상되지 않으면 여러 방어 계층을 가질 필요가 없습니다. 우리 모두가 가장 잘 알고 있는 신용 카드 인증의 예를 살펴보겠습니다. 소매점에서 신용카드를 긁으면 거기에 있는 신용카드 기계가 해당 신용카드 서비스에 연결하여 카드 세부정보를 확인합니다. 신용 카드 인증 서비스는 카드의 가용 금액, 카드가 분실 또는 블랙리스트에 등록되었는지 여부, 거래가 시작된 위치, 하루 중 시간, 및 기타 많은 요인.

신용카드 기기가 인증 서비스에 연결되지 않으면 어떻게 되나요? 이러한 경우 판매자는 카드를 수동으로 인쇄할 수 있는 기계를 받게 됩니다. 어떤 확인 작업도 수행하지 않기 때문에 카드의 임프린트를 얻는 것만으로는 충분하지 않습니다. 판매자는 또한 전화로 은행과 통화하고 판매자 번호를 제공하여 인증한 다음 거래를 확인해야 합니다. 신용 카드 거래 기계의 오류로 인해 보안 위험이 발생하지 않기 때문에 신용 카드 인증을 위한 안전 장치 프로세스입니다. 판매자의 전화선도 완전히 끊긴 경우, 안전 불이행 원칙에 따라 판매자는 신용 카드 결제를 수락하지 않아야 합니다.

페일 세이프 기본값을 준수하지 않아 많은 TLS/SSL 취약점이 발생했습니다. 대부분의 TLS/SSL 취약점은 TLS/SSL 다운그레이드 공격을 기반으로 합니다. 이 공격에서 공격자는 서버가 암호학적으로 약한 암호 제품군을 사용하도록 합니다(TLS에 대해서는 부록 C에서 자세히 설명합니다). 2015년 5월 INRIA, Microsoft Research, Johns Hopkins, University of Michigan 및 University of Pennsylvania의 그룹은 TLS 및 기타 프로토콜에 사용되는 Diffie-Hellman 알고리즘에 대한 심층 분석26을 발표했습니다. 이 분석에는 내보내기 암호화를 악용하는 Logjam이라는 TLS 프로토콜 자체에 대한 새로운 다운그레이드 공격이 포함되었습니다. 수출 암호는 1990년대에 미국 정부가 시행한 특정 법적 요구 사항을 충족하기 위해 의도적으로 약하게 설계된 약한 암호입니다. 더 약한 암호만이 합법적으로 미국 이외의 다른 국가로 수출할 수 있었습니다. 이 법적 요구 사항이 나중에 해제되었지만 대부분의 인기 있는 응용 프로그램 서버는 여전히 내보내기 암호를 지원합니다. Logjam 공격은 TLS 핸드셰이크를 변경하고 서버가 나중에 손상될 수 있는 더 약한 암호 제품군을 사용하도록 하여 내보내기 암호를 지원하는 서버를 악용했습니다. 오류 방지 기본 원칙에 따르면 이 시나리오에서 서버는 클라이언트가 암호화 방식으로 더 약한 알고리즘을 제안하고 수락하고 진행하는 대신 TLS 핸드셰이크를 중단해야 합니다.

### 메커니즘의 경제

메커니즘 원리의 경제는 단순함의 가치를 강조합니다. 디자인은 가능한 한 단순해야 합니다. 모든 구성 요소 인터페이스와 이들 간의 상호 작용은 이해할 수 있을 만큼 간단해야 합니다. 설계와 구현이 단순했다면 버그가 발생할 가능성이 낮고 동시에 테스트에 드는 노력도 덜했을 것이다. 간단하고 이해하기 쉬운 디자인과 구현은 버그를 기하급수적으로 도입하지 않고도 수정 및 유지 관리를 쉽게 만들 것입니다. 이 장의 앞부분에서 논의한 바와 같이 `Gary McGraw`는 자신의 저서인 소프트웨어 보안에서 코드와 시스템 설계의 복잡성을 높은 비율의 데이터 침해에 대한 책임이 있는 한 가지 속성으로 강조합니다.
1960년 미 해군이 도입한 KISS(Keep it Simple, 바보같은) 원칙은 메커니즘의 경제에서 Jerome Saltzer와 Michael Schroeder가 설명한 것과 매우 유사합니다. 대부분의 시스템은 복잡하게 만들지 않고 단순하게 유지해야 가장 잘 작동한다고 합니다.27 실제로는 운영 체제에서 응용 프로그램 코드에 이르기까지 KISS 원칙을 고수하고 싶지만 모든 것이 점점 더 복잡해지고 있습니다. 1990년에 Microsoft Windows 3.1은 3백만 라인이 약간 넘는 코드베이스로 시작되었습니다. 시간이 지남에 따라 요구 사항이 복잡해졌으며 2001년 Windows XP 코드베이스는 4천만 라인의 코드를 넘었습니다. 이 장에서 이전에 논의한 것처럼 이 글을 쓰는 시점에 모든 인터넷 서비스를 실행하기 위한 전체 Google 코드베이스는 약 20억 줄의 코드였습니다. 증가된 코드 행 수가 코드 복잡성을 직접적으로 반영하지 않는다고 쉽게 주장할 수 있지만 대부분의 경우 슬프게도 그렇습니다.

### 완전한 중재

완전한 중재 원칙에 따라 시스템은 모든 리소스에 대한 액세스 권한을 확인하여 액세스가 허용되는지 여부를 확인해야 합니다. 대부분의 시스템은 캐시된 권한 매트릭스를 구축하기 위해 진입점에서 한 번 액세스 유효성 검사를 수행합니다. 각 후속 작업은 캐시된 권한 매트릭스에 대해 검증됩니다. 이 패턴은 대부분 정책 평가에 소요되는 시간을 줄임으로써 성능 문제를 해결하기 위해 따르지만 공격자가 시스템을 악용하도록 매우 쉽게 초대할 수 있습니다. 실제로 대부분의 시스템은 사용자 권한과 역할을 캐시하지만 권한이나 역할 업데이트가 발생한 경우 캐시를 지우는 메커니즘을 사용합니다.

예제를 살펴보겠습니다. UNIX 운영 체제에서 실행 중인 프로세스가 파일을 읽으려고 하면 운영 체제 자체에서 해당 프로세스에 파일을 읽을 수 있는 적절한 권한이 있는지 확인합니다. 이 경우 프로세스는 허용된 액세스 수준으로 인코딩된 파일 설명자를 수신합니다. 프로세스가 파일을 읽을 때마다 파일 설명자를 커널에 제공합니다. 커널은 파일 디스크립터를 검사한 다음 액세스를 허용합니다. 파일 디스크립터가 발행된 후 파일 소유자가 프로세스에서 읽기 권한을 취소하는 경우 커널은 여전히 ​​액세스를 허용하여 완전한 중재 원칙을 위반합니다. 완전한 중재 원칙에 따라 모든 권한 업데이트는 앱 런타임(캐시된 경우 캐시)에 즉시 반영되어야 합니다

### 열린 설계 원칙

개방형 설계 원칙은 비밀이 없고 기밀 알고리즘이 없는 개방형 방식으로 시스템을 구축하는 것의 중요성을 강조합니다. 이것은 앞부분의 "설계 과제" 섹션에서 논의한 은폐에 의한 보안의 반대입니다. 오늘날 사용되는 대부분의 강력한 암호화 알고리즘은 공개적으로 설계되고 구현됩니다. 한 가지 좋은 예는 `AES` 대칭 키 알고리즘입니다. NIST는 1997년부터 2000년까지 확장된 공개 프로세스를 따라 AES를 위한 최고의 암호학적으로 강력한 알고리즘을 선택하고 당시에는 무차별 공격에 취약했던 `DES`를 대체했습니다. 1997년 1월 2일 `NIST`는 `DES`를 대체할 알고리즘을 구축하기 위한 경쟁에 대해 처음 발표했습니다. 대회가 시작된 후 처음 9개월 동안 여러 국가에서 15개의 서로 다른 제안이 있었습니다.

모든 디자인이 공개되었고 하나하나 철저한 암호해독을 거쳤습니다. `NIST`는 또한 1998년 8월과 1999년 3월에 두 번의 공개 회의를 열어 제안을 논의한 후 15개의 제안을 모두 5개로 압축했습니다. 그들은 `AES` 알고리즘으로 `Rijndael`을 선택했습니다. 최종 결과 이상으로 모든 사람(심지어 경쟁에서 패한 사람까지 포함)은 `AES` 선택 단계 전반에 걸쳐 진행한 공개 프로세스에 대해 `NIST`를 높이 평가했습니다.

개방형 디자인 원칙은 특정 앱의 설계자 또는 개발자가 앱을 안전하게 만들기 위해 앱의 설계 또는 코딩 비밀에 의존해서는 안 된다는 점을 더욱 강조합니다. 오픈 소스 소프트웨어에 의존한다면 이것은 전혀 불가능합니다. 오픈 소스 개발에는 비밀이 없습니다. 디자인 결정에서 기능 개발에 이르기까지 오픈 소스 철학에 따라 모든 것이 공개적으로 진행됩니다. 똑같은 이유로 오픈 소스 소프트웨어가 보안에 취약하다고 쉽게 주장할 수 있습니다.

이것은 오픈 소스 소프트웨어에 대한 매우 인기 있는 주장이지만 사실은 그렇지 않다는 것을 증명합니다. 2015년 1월에 발행된 Netcraft의 보고서28에 따르면 인터넷의 모든 활성 사이트 중 거의 51%가 오픈 소스 Apache 웹 서버로 구동되는 웹 서버에서 호스팅됩니다. SSL 및 TLS 프로토콜을 구현하는 또 다른 오픈 소스 프로젝트인 OpenSSL 라이브러리는 2015년 11월까지 인터넷의 550만 개 이상의 웹 사이트에서 사용됩니다.29 오픈 소스의 보안 측면에 대해 심각하게 걱정하는 사람이 있다면 그에게 적극 권장됩니다. 또는 그녀는 기업 요구 사항에 대한 오픈 소스 소프트웨어 사용의 보안 문제라는 주제로 SANS Institute에서 발행한 백서를 읽습니다.

> Note
>
> Gartner predicts, by 2020, 98% of IT organizations will leverage open source software technology in their mission-critical IT portfolios, including many cases where they will be unaware of it.31

### 권한 분리의 원칙

권한 분리의 원칙은 시스템이 단일 조건에 따라 권한을 부여해서는 안 된다는 것입니다. 동일한 원칙을 직무 분담이라고도 하며 여러 측면에서 살펴볼 수 있습니다. 예를 들어, 상환 청구는 모든 직원이 제출할 수 있지만 관리자만 승인할 수 있다고 가정해 보겠습니다. 관리자가 환급을 제출하기를 원하면 어떻게 합니까? `권한 분리의 원칙`에 따라 관리자는 자신의 상환 청구를 승인할 수 있는 권한을 부여받아서는 안 됩니다.

Amazon이 AWS 인프라를 보호할 때 `권한 분리 원칙`을 어떻게 따르는지 보는 것은 흥미롭습니다. Amazon에서 발행한 보안 백서에 따르면 `AWS 프로덕션 네트워크`는 복잡한 네트워크 보안/분리 장치 세트를 통해 `Amazon Corporate 네트워크`에서 분리됩니다. AWS 클라우드 구성 요소를 유지 관리하기 위해 액세스해야 하는 기업 네트워크의 AWS 개발자 및 관리자는 `AWS 티켓팅 시스템`을 통해 액세스를 명시적으로 요청해야 합니다. 모든 요청은 해당 서비스 소유자가 검토하고 승인합니다. 그런 다음 승인된 AWS 직원이 네트워크 장치 및 기타 클라우드 구성 요소에 대한 액세스를 제한하는 배스천 호스트를 통해 AWS 네트워크에 연결하여 보안 검토를 위해 모든 활동을 기록합니다. 배스천 호스트에 액세스하려면 호스트의 모든 사용자 계정에 대한 `SSH 공개 키 인증`이 필요합니다.

`NSA`도 유사한 전략을 따릅니다. NSA에서 발행한 팩트 시트에서는 네트워크 수준에서 권한 분리 원칙을 구현하는 것의 중요성을 강조합니다. 네트워크는 다양한 기능, 목적 및 민감도 수준을 가진 상호 연결된 장치로 구성됩니다. 네트워크는 웹 서버, 데이터베이스 서버, 개발 환경 및 이들을 함께 묶는 인프라를 포함할 수 있는 여러 세그먼트로 구성될 수 있습니다. 이러한 세그먼트는 목적과 보안 문제가 다르기 때문에 네트워크를 악용 및 악의적인 의도로부터 보호하려면 이를 적절하게 분리하는 것이 가장 중요합니다.

### 최소 공통 메커니즘

최소 공통 메커니즘의 원칙은 서로 다른 구성 요소 간에 상태 정보를 공유할 위험에 관한 것입니다. 즉, 리소스 접근에 사용되는 메커니즘을 공유해서는 안 된다는 것입니다. 이 원칙은 여러 각도에서 해석될 수 있습니다. 한 가지 좋은 예는 AWS가 IaaS 공급자로 작동하는 방식을 보는 것입니다. EC2는 AWS에서 제공하는 핵심 서비스 중 하나입니다. Netflix, Reddit, Newsweek 및 기타 많은 회사에서 EC2에서 서비스를 실행합니다. EC2는 부하에 따라 선택한 서버 인스턴스를 스핀업 및 다운할 수 있는 클라우드 환경을 제공합니다. 이 접근 방식을 사용하면 예상되는 가장 높은 로드를 미리 계획할 필요가 없으며 로드가 낮을 때 대부분의 시간 동안 리소스를 유휴 상태로 만들 수 있습니다. 이 경우 각 EC2 사용자는 자체 게스트 운영 체제를 실행하는 자체 격리된 서버 인스턴스를 갖게 되지만 궁극적으로 모든 서버는 AWS에서 유지 관리하는 공유 플랫폼 위에서 실행됩니다.

이 공유 플랫폼에는 네트워킹 인프라, 하드웨어 인프라 및 스토리지가 포함됩니다. 인프라 위에 하이퍼바이저라는 특수 소프트웨어가 실행됩니다. 모든 게스트 운영 체제는 하이퍼바이저 위에서 실행됩니다. 하이퍼바이저는 하드웨어 인프라를 통해 가상화된 환경을 제공합니다. Xen과 KVM은 널리 사용되는 두 가지 하이퍼바이저이며 AWS는 내부적으로 Xen을 사용하고 있습니다. 한 고객을 위해 실행되는 특정 가상 서버 인스턴스가 다른 고객을 위해 실행 중인 다른 가상 서버 인스턴스에 액세스할 수 없더라도 누군가가 하이퍼바이저에서 보안 허점을 찾을 수 있다면 실행 중인 모든 가상 서버 인스턴스를 제어할 수 있습니다. EC2. 이것이 거의 불가능해 보이지만 과거에는 Xen 하이퍼바이저에 대해 보고된 많은 보안 취약점이 있었습니다.34

최소 공통 메커니즘의 원칙은 리소스의 공통 공유 사용을 최소화하도록 권장합니다. 공용 인프라의 사용을 완전히 제거할 수는 없지만 비즈니스 요구 사항에 따라 사용량을 최소화할 수 있습니다. AWS VPC는 각 사용자에게 논리적으로 격리된 인프라를 제공합니다. 선택적으로 추가 격리를 위해 각 고객 전용 하드웨어에서 실행되는 전용 인스턴스를 시작하도록 선택할 수도 있습니다.

최소 공통 메커니즘의 원칙은 공유 다중 테넌트 환경에서 데이터를 저장하고 관리하는 시나리오에도 적용될 수 있습니다. 모든 것을 공유하는 전략을 따르면 다른 고객의 데이터를 동일한 데이터베이스의 동일한 테이블에 저장하여 각 고객 데이터를 고객 ID로 격리할 수 있습니다. 데이터베이스에 액세스하는 앱은 주어진 고객이 자신의 데이터에만 액세스할 수 있도록 합니다. 이 접근 방식을 사용하면 누군가 앱 로직에서 보안 허점을 발견하면 모든 고객 데이터에 액세스할 수 있습니다. 다른 접근 방식은 각 고객에 대해 격리된 데이터베이스를 갖는 것입니다. 이것은 더 비싸지 만 훨씬 안전한 옵션입니다. 이를 통해 고객 간에 공유되는 내용을 최소화할 수 있습니다.

### 심리적 수용성

심리적 수용 가능성의 원칙은 보안 메커니즘이 보안 메커니즘이 없는 경우보다 리소스 접근을 더 어렵게 만들어서는 안 된다는 것입니다. 보안 메커니즘이 리소스의 유용성 또는 액세스 가능성을 죽이면 사용자는 이러한 메커니즘을 끌내는 방법을 찾을 수 있습니다. 가능하면 보안 메커니즘은 시스템 사용자에게 투명해야 하며 기껏해야 방해를 최소화해야 합니다. 보안 메커니즘은 사용자가 더 자주 사용하도록 권장하기 위해 사용자 친화적이어야 합니다.

`Microsoft`는 2005년에 정보 카드를 피싱에 대항하기 위한 인증의 새로운 패러다임으로 도입했습니다. 그러나 사용자 이름/비밀번호 기반 인증에 익숙한 사람들에게는 높은 설정 비용과 함께 사용자 경험이 좋지 않았습니다. 그것은 `Microsoft`의 또 다른 실패한 계획으로 역사에 기록되었습니다.

대부분의 웹 사이트는 자동화된 스크립트에서 인간을 구별하는 방법으로 CAPTCHA를 사용합니다. CAPTCHA는 실제로 컴퓨터와 인간을 구분하는 완전 자동화된 공개 튜링 테스트의 약자입니다. CAPTCHA는 챌린지 응답 모델을 기반으로 하며 자동 무차별 대입 공격을 피하기 위해 대부분 사용자 등록 및 비밀번호 복구 기능과 함께 사용됩니다. 이렇게 하면 보안이 강화되지만 사용자 경험이 쉽게 중단될 수도 있습니다. 특정 CAPTCHA 구현이 제공하는 일부 문제는 사람이 읽을 수조차 없습니다. Google은 Google reCAPTCHA를 사용하여 이 문제를 해결하려고 합니다. reCAPTCHA를 사용하면 사용자는 보안 문자를 풀지 않고도 자신이 사람임을 증명할 수 있습니다. 대신 클릭 한 번으로 로봇이 아님을 확인할 수 있습니다. 이것은 CAPTCHA reCAPTCHA 경험 없음이라고도 합니다.

## 보안 삼각대

정보 보안의 3요소로 널리 알려진 CIA(기밀성, 무결성 및 가용성)는 정보 시스템 보안을 벤치마킹하는 데 사용되는 세 가지 핵심 요소입니다. 이것은 CIA 트라이어드 또는 AIC 트라이어드라고도 합니다. CIA 트라이어드는 보안 모델을 설계하고 기존 보안 모델의 강점을 평가하는 데 도움이 됩니다. 다음 섹션에서는 CIA 트라이어드의 세 가지 주요 속성에 대해 자세히 설명합니다.

### 기밀성

CIA 트라이어드의 기밀성 속성은 저장 중이거나 전송 중인 의도하지 않은 수신자로부터 데이터를 보호하는 방법에 대해 걱정합니다. 암호화로 전송 채널과 스토리지를 보호하여 기밀성을 확보합니다. 전송 채널이 대부분 HTTP인 API의 경우 실제로 `HTTPS`로 알려진 `TLS`를 사용할 수 있습니다. 스토리지의 경우 디스크 수준 암호화 또는 응용 프로그램 수준 암호화를 사용할 수 있습니다. 채널 암호화 또는 전송 수준 암호화는 전송 중인 메시지만 보호합니다.

메시지가 전송 채널을 떠나는 즉시 더 이상 안전하지 않습니다. 즉, 전송 수준 암호화는 지점 간 보호만 제공하고 연결이 끝나는 곳에서 자릅니다. 이와 대조적으로 앱 수준에서 발생하고 전송 채널에 종속되지 않는 메시지 수준 암호화가 있습니다. 즉, 메시지 수준 암호화를 사용하면 앱 자체에서 유선을 통해 메시지를 보내기 전에 메시지를 암호화하는 방법에 대해 걱정해야 하며, 이를 `종단 간 암호화`라고도 합니다. 메시지 수준 암호화로 데이터를 보호하는 경우 보안되지 않은 `HTTP`을 사용하여 메시지를 전송할 수도 있습니다.

프록시를 통과할 때 클라이언트에서 서버로의 TLS 연결은 TLS 브리징 또는 TLS 터널링의 두 가지 방법으로 설정할 수 있습니다. 거의 모든 프록시 서버는 두 모드를 모두 지원합니다. 보안 수준이 높은 배포의 경우 `TLS` 터널링을 사용하는 것이 좋습니다. `TLS` 브리징에서 초기 연결은 프록시 서버에서 잘리고 거기에서 게이트웨이(또는 서버)에 대한 새 연결이 설정됩니다. 이는 데이터가 프록시 서버 내부에 있는 동안 일반 텍스트임을 의미합니다. 프록시 서버에 `맬웨어`를 심을 수 있는 모든 침입자는 통과하는 트래픽을 가로챌 수 있습니다. `TLS 터널링`을 사용하면 프록시 서버가 클라이언트 시스템과 게이트웨이(또는 서버) 간에 직접 채널을 쉽게 생성할 수 있습니다. 이 채널을 통한 데이터 흐름은 프록시 서버에 표시되지 않습니다.

반면에 메시지 수준 암호화는 기본 전송과 독립적입니다. 메시지를 암호화하고 해독하는 것은 앱 개발자의 책임입니다. 이것은 앱에 따라 다르기 때문에 상호 운용성을 해치고 발신자와 수신자 사이에 긴밀한 결합을 구축합니다. 각각은 데이터를 사전에 암호화/복호화하는 방법을 알아야 합니다. 이는 대규모로 분산된 시스템에서 잘 확장되지 않습니다. 이 문제를 극복하기 위해 메시지 수준 보안에 대한 표준을 구축하려는 집중적인 노력이 있었습니다. XML 암호화는 W3C가 주도하는 그러한 노력 중 하나입니다. XML 페이로드를 암호화하는 방법을 표준화합니다. 마찬가지로 `IETF JOSE 작업 그룹`은 JSON 페이로드에 대한 표준 세트를 구축했습니다. 7장과 8장에서는 각각 JSON 메시지 보안의 두 가지 주요 표준인 JSON 웹 서명과 JSON 웹 암호화에 대해 설명합니다.

> 참고
>
> SSL과 TLS는 종종 같은 의미로 사용되지만 순수한 기술 용어로는 동일하지 않습니다. TLS는 SSL 3.0의 후속 제품입니다. `IETF RFC 2246`에 정의된 TLS 1.0은 Netscape에서 발행한 SSL 3.0 프로토콜 사양을 기반으로 합니다. TLS 1.0과 SSL 3.0의 차이점은 극적인 것은 아니지만 TLS 1.0과 SSL 3.0이 상호 운용되지 않을 만큼 충분히 중요합니다.

이전에 논의된 것 외에도 전송 수준 보안과 메시지 수준 보안 사이에는 몇 가지 주요 차이점이 있습니다.

- 전송 수준 보안은 지점 간 보안이므로 전송 중 전체 메시지를 암호화합니다.

- 전송 수준은 보호를 위해 기본 채널에 의존하기 때문에 앱 개발자는 암호화할 데이터 부분과 암호화하지 않을 부분을 제어할 수 없습니다.

- 부분 암호화는 전송 수준 보안에서 지원되지 않지만 메시지 수준 보안에서는 지원됩니다.

- 성능은 메시지 수준 보안과 전송 수준 보안을 구분하는 핵심 요소입니다. 메시지 수준 암호화는 리소스 소비 측면에서 전송 수준 암호화보다 훨씬 비쌉니다.

- 메시지 레벨 암호화는 앱 계층에서 이루어지며, 암호화 과정을 수행하기 위해서는 메시지의 종류와 구조를 고려해야 한다. XML 메시지인 경우 XML 암호화 표준에 정의된 프로세스를 따라야 합니다.


### 무결성 원칙

무결성은 데이터의 정확성과 신뢰성을 보장하고 무단 수정을 감지하는 능력입니다. 데이터가 무단 또는 의도하지 않은 변경, 수정 또는 삭제로부터 보호되도록 합니다. 무결성을 달성하는 방법은 예방 조치와 탐지 조치의 두 가지입니다. 두 측정 모두 전송 데이터와 저장 데이터를 모두 처리해야 합니다.

전송 중에 데이터가 변경되는 것을 방지하려면 의도된 당사자만 메시지 수준 암호화를 읽거나 수행할 수 있는 보안 채널을 사용해야 합니다. TLS는 전송 수준 암호화에 권장되는 접근 방식입니다. TLS 자체에는 데이터 수정을 감지하는 방법이 있습니다. 첫 번째 핸드셰이크에서 각 메시지의 메시지 인증 코드를 전송합니다. 이 코드는 전송 중에 데이터가 수정되지 않았는지 확인하기 위해 수신 당사자가 확인할 수 있습니다.

데이터 변경을 방지하기 위해 메시지 수준 암호화를 사용하는 경우 받는 사람에서 메시지의 수정 사항을 감지하려면 보낸 사람이 메시지에 서명해야 하고 보낸 사람의 공개 키로 받는 사람이 서명을 확인할 수 있습니다. 이전 섹션에서 논의한 것과 유사하게 서명 프로세스를 정의하는 메시지 유형 및 구조를 기반으로 하는 표준이 있습니다. XML 메시지인 경우 W3C의 XML 서명 표준이 프로세스를 정의합니다.

미사용 데이터의 경우 메시지 다이제스트를 주기적으로 계산하여 안전한 장소에 보관할 수 있습니다. 의심스러운 활동을 숨기기 위해 침입자가 변경할 수 있는 감사 로그는 무결성을 위해 보호되어야 합니다. 또한 네트워크 스토리지의 출현과 스토리지에 대한 새로운 장애 모드를 초래한 새로운 기술 동향으로 인해 데이터 무결성을 보장하는 데 흥미로운 문제가 발생합니다. 보안과 별도로 스토리지 무결성 검사의 여러 흥미로운 응용 프로그램에 대해 설명하고 이러한 기술과 관련된 구현 문제에 대해 설명합니다.

> Note
>
> HTTP Digest authentication with the quality of protection (qop) value set to auth-int can be used to protect messages for integrity. Appendix F discusses HTTP Digest authentication in depth.

### 가용성

합법적인 사용자가 항상 액세스할 수 있는 시스템을 만드는 것은 모든 시스템 설계의 궁극적인 목표입니다. 보안은 조사해야 할 유일한 측면이 아니지만 시스템을 계속 가동하고 실행하는 데 중요한 역할을 합니다. 보안 설계의 목표는 불법적인 액세스 시도로부터 시스템을 보호하여 시스템을 고가용성으로 만드는 것이어야 합니다. 그렇게 하는 것은 매우 어려운 일입니다. 특히 공개 API에 대한 공격은 공격자가 시스템에 맬웨어를 심는 것부터 고도로 조직화된 DDoS 공격에 이르기까지 다양합니다.

DDoS 공격은 완전히 제거하기 어렵지만 신중하게 설계하면 영향을 줄이기 위해 최소화할 수 있습니다. 대부분의 경우 DDoS 공격은 네트워크 경계 수준에서 감지되어야 하므로 앱 코드는 너무 걱정할 필요가 없습니다. 그러나 앱 코드의 취약점을 악용하여 시스템을 다운시킬 수 있습니다. Christian Mainka, Juraj Somorovsky, Jorg Schwenk 및 Andreas Falkenberg가 발행한 논문은 XML 페이로드가 있는 SOAP 기반 API에 대해 수행할 수 있는 8가지 유형의 DoS 공격에 대해 설명합니다.

- 강압적 구문 분석 공격: 공격자가 깊이 중첩된 XML 구조를 가진 XML 문서를 보낸다. DOM 기반 파서가 XML 문서를 처리할 때 메모리 부족 예외 또는 높은 CPU 로드가 발생할 수 있습니다.

- SOAP 배열 공격: 공격을 받은 웹 서비스가 매우 큰 SOAP 배열을 선언하도록 합니다. 이것은 웹 서비스의 메모리를 고갈시킬 수 있습니다.

- XML ​​요소 개수 공격: 비중첩 요소가 많은 SOAP 메시지를 전송하여 서버를 공격합니다.

- XML ​​속성 카운트 공격: 속성 카운트가 높은 SOAP 메시지를 전송하여 서버를 공격합니다.

- XML ​​엔터티 확장 공격: 서버가 DTD(문서 유형 정의)에 정의된 엔터티를 재귀적으로 확인하도록 하여 시스템 장애를 일으킵니다. 이 공격은 XML 폭탄 또는 10억 웃음 공격이라고도 합니다.

- XML ​​외부 엔터티 DoS 공격: 서버가 DTD에 정의된 대규모 외부 엔터티를 강제로 해결하여 시스템 장애를 일으킵니다. 공격자가 외부 엔티티 공격을 실행할 수 있는 경우 추가 공격 표면이 나타날 수 있습니다.

- XML ​​overlong name 공격: XML 문서에 지나치게 긴 XML 노드를 삽입합니다. 너무 긴 노드는 너무 긴 요소 이름, 속성 이름, 속성 값 또는 네임스페이스 정의일 수 있습니다.

- 해시 충돌 공격(HashDoS): 다른 키를 사용하면 동일한 버킷 할당이 발생하여 충돌이 발생합니다. 충돌은 버킷에서 리소스 집약적인 계산으로 이어집니다. 약한 해시 함수를 사용하면 공격자가 의도적으로 해시 충돌을 만들어 시스템 오류를 일으킬 수 있습니다.

이러한 공격의 대부분은 앱 수준에서 방지할 수 있습니다. CPU 또는 메모리 집약적 작업의 경우 임계값을 유지할 수 있습니다. 예를 들어, 강제 구문 분석 공격을 방지하기 위해 XML 파서는 요소 수에 제한을 적용할 수 있습니다. 유사하게, 앱이 더 오랜 시간 동안 스레드를 실행하는 경우 임계값을 설정하고 종료할 수 있습니다. 합법적이지 않은 것으로 확인되는 즉시 메시지의 추가 처리를 중단하는 것이 DoS 공격에 맞서 싸우는 가장 좋은 방법입니다. 이는 또한 흐름의 진입점에 가장 가까운 인증/권한 부여 확인의 중요성을 강조합니다.

> 참고
>
> eSecurity Planet에 따르면 가장 큰 DDoS 공격 중 하나가 2013년 3월 인터넷을 강타했으며 120Gbps의 Cloudflare 네트워크를 표적으로 삼았습니다. 업스트림 공급자는 공격이 절정에 달했을 때 300Gbps DDoS의 공격을 받았습니다.

JSON 취약점에 대한 DoS 공격도 있습니다. CVE-2013-026938은 신중하게 제작된 JSON 메시지를 사용하여 임의의 Ruby 기호 또는 특정 내부 개체 생성을 트리거하여 DoS 공격을 일으킬 수 있는 시나리오를 설명합니다.

## 보안 통제

기밀성, 무결성 및 가용성은 정보 보안의 핵심 원칙 중 하나입니다. 이를 달성하기 위해 인증, 권한 부여, 부인 방지 및 감사가 중요한 역할을 하는 4가지 주요 제어 기능입니다. 다음 섹션에서는 이러한 네 가지 보안 제어에 대해 자세히 설명합니다.

### 인증

인증은 사용자, 시스템 또는 사물을 고유한 방식으로 식별하여 그것이 주장하는 사람임을 증명하는 프로세스입니다. 인증은 인증 어설션을 가져오는 방법에 따라 직접 또는 중개될 수 있습니다. 사용자 이름과 비밀번호만 제공하면 시스템에 직접 로그인하는 경우 직접 인증에 해당합니다. 즉, 직접 인증에서 자신을 인증하려는 엔터티는 액세스하려는 서비스에 인증 주장을 제시합니다.

중개 인증에는 제3자가 관련되어 있습니다. 이 제3자는 일반적으로 ID 제공자로 알려져 있습니다. Facebook을 통해 Yelp 계정에 로그인하면 중개 인증에 해당하며 Facebook은 ID 제공자입니다. 중개 인증을 사용하면 서비스 제공자(또는 로그인하려는 웹 사이트 또는 액세스하려는 API)가 사용자를 직접 신뢰하지 않습니다. ID 공급자만 신뢰합니다. 신뢰할 수 있는 ID 제공자(서비스 제공자에 의한)가 서비스 제공자에게 긍정적인 주장을 전달하는 경우에만 서비스에 액세스할 수 있습니다.

인증은 단일 요소 또는 다중 요소(다중 요소 인증이라고도 함)에서 수행할 수 있습니다. 당신이 알고 있는 것, 당신 자신, 그리고 당신이 가진 것이 인증의 잘 알려진 세 가지 요소입니다. 다단계 인증의 경우 시스템은 최소한 두 가지 요소를 조합하여 사용해야 합니다. 동일한 범주에 속하는 두 가지 기술을 결합하는 것은 다단계 인증으로 간주되지 않습니다. 예를 들어 사용자 이름과 비밀번호를 입력한 다음 PIN 번호를 입력하는 것은 다단계 인증으로 간주되지 않습니다. 둘 다 알고 있는 범주에 속하기 때문입니다

> **참고**
>
> Google 2단계 인증은 다단계 인증에 해당합니다. 먼저 사용자 이름과 비밀번호(귀하가 알고 있는 것)를 제공해야 하며, 그런 다음 PIN이 휴대 전화로 전송됩니다. PIN을 알면 등록된 휴대 전화가 귀하의 소유 하에 있음을 확인할 수 있습니다. 그런 다음 다시 한 번 이것은 다단계 인증이 아니라고 주장할 수 있습니다. PIN만 알면 되며 PIN을 받기 위해 전화를 가지고 있어야 하는 것은 필수가 아니기 때문입니다. 다소 이상하게 들리지만 Grant Blakeman의 사건은 정확히 동일한 것으로 판명되었습니다. 공격자는 Grant의 휴대전화에 착신 전환 번호를 설정할 수 있었고 Google 비밀번호 재설정 정보를 새 번호로 수신할 수 있었습니다(착신 전환을 통해).

### 당신이 알고 있는 것

암호, 암호 및 PIN 번호는 알고 있는 범주에 속합니다. 이것은 수십 년 동안뿐만 아니라 수세기 동안 가장 인기 있는 인증 형식이었습니다. 18세기로 거슬러 올라갑니다. 아라비아 민담 Ali Baba와 천일야화 사십도에서 Ali Baba는 "열려라 참깨"라는 암호를 사용하여 숨겨진 동굴의 문을 엽니다. 그 이후로 이것은 가장 인기 있는 인증 형식이 되었습니다. 불행히도 가장 약한 인증 방식이기도 합니다. 암호로 보호된 시스템은 여러 가지 방법으로 손상될 수 있습니다. Ali Baba의 이야기로 돌아가서, 그의 처남은 비밀번호도 모른 채 같은 동굴에 갇히고 그가 아는 ​​모든 단어를 외치려고 했습니다. 이것은 현대에는 무차별 대입 공격으로 알려져 있습니다. 알려진 최초의 무차별 대입 공격은 18세기에 발생했습니다. 그 이후로 암호 보안 시스템을 깨는 인기 있는 방법이 되었습니다.

> **참고**
>
> 2013년 4월 WordPress는 대규모 무차별 대입 공격을 받았습니다. 4월의 하루 평균 스캔 수는 100,000건을 넘었습니다. 무차별 대입 공격에는 다양한 형태가 있습니다. 사전 공격은 그 중 하나이며, 일반적으로 사용되는 단어 사전을 기반으로 제한된 입력 집합으로 무차별 대입 공격을 수행합니다. 이것이 사전에서 찾을 수 없는 혼합 영숫자 문자로 강력한 암호를 적용해야 하는 회사 암호 정책이 있어야 하는 이유입니다. 대부분의 공개 웹 사이트는 로그인 시도에 몇 번 실패하면 보안문자를 적용합니다. 이는 자동화/도구 기반 무차별 대입 공격을 실행하기 어렵게 만듭니다.

### 보유하고 있는 것

인증서 및 스마트 카드 기반 인증은 보유하고 있는 범주에 속합니다. 이것은 당신이 알고 있는 것보다 훨씬 더 강력한 인증 형식입니다. TLS 상호 인증은 클라이언트 인증서로 API를 보호하는 가장 널리 사용되는 방법입니다. 이것은 3장에서 자세히 다룹니다.

FIDO(Fast IDentity Online) 인증도 카테고리에 속합니다. FIDO Alliance41는 강력한 인증의 특정 문제를 해결하기 위해 FIDO U2F(FIDO Universal Second Factor), FIDO UAF(FIDO Universal Authentication Framework) 및 CTAP(Client to Authenticator Protocol)의 세 가지 공개 사양을 발표했습니다. FIDO U2F 프로토콜을 사용하면 온라인 서비스에서 사용자 로그인에 강력한 두 번째 요소를 추가하여 기존 암호 인프라의 보안을 강화할 수 있습니다. FIDO U2F 기반 인증의 가장 큰 배포는 Google입니다. Google은 내부 서비스를 보호하기 위해 한동안 내부적으로 FIDO U2F를 사용해 왔으며 2014년 10월에 모든 사용자가 FIDO U2F를 공개적으로 사용할 수 있도록 설정했습니다.42

### 당신 자신

지문, 눈 망막, 안면 인식 및 기타 모든 생체 인식 기반 인증 기술은 사용자의 범주에 속합니다. 이것은 가장 강력한 인증 형식입니다. 대부분의 경우 생체 인증은 자체적으로 수행되지 않고 보안을 더욱 강화하기 위해 다른 요소와 함께 사용됩니다.

모바일 장치의 광범위한 채택으로 대부분의 소매업체, 금융 기관 및 기타 많은 기업이 모바일 앱에 지문 기반 인증을 선택했습니다. iOS 플랫폼에서 이러한 모든 앱은 사용자 이름 및 암호 기반 인증을 Apple Touch ID(또는 얼굴 인식)와 연결합니다. 초기 연결이 완료되면 사용자는 지문을 스캔하는 것만으로 연결된 모든 앱에 로그인할 수 있습니다. 또한 iPhone은 Touch ID를 App Store 로그인과 연결하고 Apple Pay 거래를 승인합니다

## 권한 부여 Authorization

권한 부여는 인증된 사용자, 시스템 또는 사물이 잘 정의된 시스템 경계 내에서 수행할 수 있는 작업을 확인하는 프로세스입니다. 권한 부여는 사용자가 이미 인증되었다는 가정 하에 이루어집니다. 임의 액세스 제어(DAC) 및 필수 액세스 제어(MAC)는 시스템에서 액세스를 제어하는 ​​두 가지 주요 모델입니다.

임의 접근 제어(DAC)를 사용하면 데이터를 소유한 사용자가 재량에 따라 다른 사용자에게 권한을 양도할 수 있습니다. Unix, Linux 및 Windows를 포함한 대부분의 운영 체제는 DAC를 지원합니다. Linux에서 파일을 생성할 때 누가 파일을 읽고, 쓰고, 실행할 수 있는지 결정할 수 있습니다. 어떤 사용자 또는 사용자 그룹과도 공유할 수 없습니다. 시스템에 보안 결함을 쉽게 가져올 수 있는 중앙 집중식 제어가 없습니다.

MAC(Mandatory Access Control)을 사용하면 지정된 사용자만 권한을 부여할 수 있습니다. 권한이 부여되면 사용자는 이를 양도할 수 없습니다. SELinux, Trusted Solaris 및 TrustedBSD는 MAC을 지원하는 운영 체제 중 일부입니다.

> 참고
>
> `SELinux`는 Linux 커널에 MAC(Mandatory Access Control) 아키텍처를 추가한 NSA 연구 프로젝트로, 이후 2003년 8월 주류 버전의 Linux에 병합되었습니다. `Linux Security Modules(LSM)`라는 Linux 2.6 커널 기능을 활용합니다. ) 상호 작용.

DAC와 MAC의 차이점은 누가 위임할 수 있는지에 있습니다. 두 경우 모두 액세스 제어 규칙이나 액세스 매트릭스를 나타내는 방법이 필요합니다. 권한 부여 테이블, 액세스 제어 목록(그림 2-2 참조) 및 기능은 액세스 제어 규칙을 나타내는 세 가지 방법입니다. 권한 부여 테이블은 제목, 작업 및 리소스가 있는 3열 테이블입니다. 주체는 개별 사용자 또는 그룹이 될 수 있습니다. 액세스 제어 목록을 사용하면 각 리소스가 목록과 연결되어 각 주체에 대해 주체가 리소스에 대해 실행할 수 있는 작업을 나타냅니다. 기능이 있는 경우 각 주제에는 기능 목록이라는 관련 목록이 있으며, 이는 각 리소스에 대해 사용자가 해당 리소스에 대해 실행할 수 있는 작업을 나타냅니다. 은행 사물함 키는 기능으로 간주될 수 있습니다. 사물함은 리소스이고 사용자는 리소스에 대한 키를 보유합니다. 사용자가 로커를 열쇠로 열려고 할 때, 당신은 열쇠 소유자의 능력이 아니라 열쇠의 능력에 대해서만 걱정하면 됩니다. 액세스 제어 목록은 리소스 기반이지만 기능은 주체 기반입니다.

권한 부여 테이블, 액세스 제어 목록 및 기능은 매우 세분화되어 있습니다. 한 가지 대안은 정책 기반 액세스 제어를 사용하는 것입니다. 정책 기반 액세스 제어를 통해 세분화된 권한 부여 정책을 가질 수 있습니다. 또한 기능 및 액세스 제어 목록은 정책에서 동적으로 파생될 수 있습니다. XACML은 정책 기반 액세스 제어를 위한 OASIS 표준 중 하나입니다.

Figure 2-2 Access control list

> Note
>
> XACML is an XML-based open standard for policy-based access control developed under the OASIS XACML Technical Committee. XACML 3.0, the latest XACML specification, was standardized in January 2013.43 Then again XACML is little too complex in defining access control policies, irrespective of how powerful it is. You can also check the Open Policy Agent (OPA) project, which has become quite popular recently in building fine-grained access control policies.

### 부인 방지

API를 통해 신원을 증명하여 비즈니스 거래를 할 때마다 나중에 이를 거부하거나 거부할 수 없어야 합니다. 부인할 수 없음을 보장하는 속성을 부인 방지라고 합니다. 한 번만 하면 영원히 소유할 수 있습니다. 부인 방지는 제3자가 언제든지 확인할 수 있는 위조 불가능한 방식으로 데이터의 출처와 무결성에 대한 증거를 제공해야 합니다. 트랜잭션이 시작되면 트랜잭션 무결성을 유지하고 향후 검증을 허용하기 위해 사용자 ID, 날짜 및 시간, 트랜잭션 세부 정보를 포함한 그 어떤 내용도 변경되어서는 안 됩니다. 트랜잭션이 커밋되고 확인된 후 변경되지 않고 기록되었는지 확인해야 합니다. 무단 수정을 방지하려면 로그를 보관하고 적절하게 보호해야 합니다. 거부 분쟁이 있을 때마다 다른 로그 또는 데이터와 함께 트랜잭션 로그를 검색하여 개시자, 날짜 및 시간, 트랜잭션 내역 등을 확인할 수 있습니다.

> 참고
>
> TLS는 인증(인증서 확인), 기밀성(비밀 키로 데이터 암호화) 및 무결성(데이터 다이제스트)을 보장하지만 부인 방지는 보장하지 않습니다. TLS에서 전송된 데이터의 MAC(메시지 인증 코드) 값은 클라이언트와 서버 모두에 알려진 공유 비밀 키로 계산됩니다. 공유 키는 부인 방지에 사용할 수 없습니다.

디지털 서명은 사용자(트랜잭션을 시작한 사용자)와 사용자가 수행하는 트랜잭션 사이에 강력한 바인딩을 제공합니다. 사용자만 알고 있는 키로 전체 트랜잭션에 서명해야 하며 서버(또는 서비스)는 사용자 키의 적법성을 보증하는 신뢰할 수 있는 브로커를 통해 서명을 확인할 수 있어야 합니다. 이 신뢰할 수 있는 브로커는 인증 기관(CA)일 수 있습니다. 서명이 확인되면 서버는 사용자의 신원을 알고 데이터의 무결성을 보장할 수 있습니다. 부인 방지를 위해 데이터는 향후 확인을 위해 안전하게 저장되어야 합니다.

> 참고
>
> Citigroup의 Chii-Ren Tsai가 작성한 Non-Repudiation in Practice 백서에서는 시도 응답 1회용 암호 토큰과 디지털 서명을 사용하는 금융 거래에 대한 두 가지 잠재적인 부인 방지 아키텍처에 대해 논의합니다.


## 감사

감사에는 두 가지 측면이 있습니다. 부인 방지를 용이하게 하기 위해 모든 합법적인 액세스 시도를 추적하고 가능한 위협을 식별하기 위해 모든 불법적인 액세스 시도를 추적합니다. 리소스에 대한 액세스가 허용되는 경우가 있을 수 있지만 유효한 목적이어야 합니다. 예를 들어 이동통신사는 사용자의 통화 내역에 액세스할 수 있지만 해당 사용자의 요청 없이 액세스해서는 안 됩니다. 누군가 사용자의 통화 기록에 자주 액세스하는 경우 적절한 감사 추적으로 이를 감지할 수 있습니다. 감사 추적은 사기 탐지에서도 중요한 역할을 합니다. 관리자는 사기 탐지 패턴을 정의할 수 있으며 감사 로그는 거의 실시간으로 평가되어 일치하는 항목을 찾을 수 있습니다.

## 요약

- 보안은 나중에 생각하는 것이 아닙니다. 이는 모든 개발 프로젝트와 API의 필수적인 부분이어야 합니다. 요구 사항 수집으로 시작하여 설계, 개발, 테스트, 배포 및 모니터링 단계를 진행합니다.

- 연결성, 확장성 및 복잡성은 지난 몇 년 동안 전 세계적으로 증가하는 데이터 침해의 세 가지 추세입니다.
- 모든 보안 설계에서 가장 어려운 것은 보안과 사용자 편의 사이에서 적절한 균형을 찾고 유지하는 것입니다.

- 적절한 보안 설계는 시스템의 모든 통신 링크에 주의해야 합니다. 어떤 시스템도 가장 약한 연결보다 강하지 않습니다.

- 보안을 위해 강화되는 모든 시스템에는 계층화된 접근 방식이 선호됩니다. 이를 심층 방어라고도 합니다.

- 내부자 공격은 덜 복잡하지만 매우 효과적입니다.

- Kerckhoffs의 원칙은 설계가 적에게 알려지지 않았기 때문에가 아니라 설계에 의해 시스템을 보호해야 함을 강조합니다.

- 최소 권한의 원칙은 엔터티가 권한이 부여된 작업을 수행하는 데 필요한 권한 집합만 가져야 하며 더 이상은 없어야 한다는 것입니다.

- 페일 세이프 기본 원칙은 기본적으로 시스템을 안전하게 만드는 것의 중요성을 강조합니다.

- 메커니즘 원리의 경제는 단순함의 가치를 강조합니다. 디자인은 가능한 한 단순해야 합니다.

- 완전한 중재 원칙에 따라 시스템은 모든 리소스에 대한 액세스 권한을 확인하여 액세스가 허용되는지 여부를 확인해야 합니다.

- 개방형 설계 원칙은 비밀이 없고 기밀 알고리즘이 없는 개방형 방식으로 시스템을 구축하는 것의 중요성을 강조합니다.

- 권한 분리의 원칙은 시스템이 단일 조건으로 권한을 부여해서는 안 된다는 것입니다.

- 최소 공통 메커니즘의 원칙은 서로 다른 구성 요소 간에 상태 정보를 공유할 위험에 관한 것입니다.

- 심리적 수용의 원칙은 보안 메커니즘이 보안 메커니즘이 없는 경우보다 리소스에 대한 액세스를 더 어렵게 만들어서는 안 된다는 것입니다.

- 정보 보안의 3대 요소로 널리 알려진 CIA(Confidentiality, Integrity, and Availability)는 정보 시스템 보안을 벤치마킹하는 데 사용되는 세 가지 핵심 요소입니다.

>>>