
# 13. User-Managed Access

OAuth 2.0은 액세스 위임을 위한 인증 프레임워크를 도입했습니다. 이를 통해 Bob은 Facebook 자격 증명을 공유하지 않고도 Facebook 담벼락에 대한 읽기 액세스 권한을 타사 애플리케이션에 위임할 수 있습니다. 사용자 관리 액세스(UMA)는 이 모델을 다른 수준으로 확장합니다. 여기서 Bob은 타사 응용 프로그램에 대한 액세스 권한을 위임할 수 있을 뿐만 아니라 동일한 타사 응용 프로그램을 사용하는 Peter에게도 권한을 위임할 수 있습니다.

UMA는 OAuth 2.0 프로필입니다. OAuth 2.0은 권한 서버에서 리소스 서버를 분리합니다. UMA는 한 단계 더 나아가 중앙 권한 부여 서버에서 분산된 리소스 서버 집합을 제어할 수 있습니다. 또한 자원 소유자는 권한 부여 서버에서 정책 세트를 정의할 수 있으며, 이는 클라이언트가 보호 자원에 대한 액세스 권한을 부여받을 때 평가될 수 있습니다. 따라서 임의의 클라이언트 또는 요청 당사자의 액세스 요청을 승인하기 위해 리소스 소유자가 있어야 할 필요가 없습니다. 권한 부여 서버는 리소스 소유자가 정의한 정책을 기반으로 결정을 내릴 수 있습니다.

이 장에서 논의할 UMA의 최신 버전은 UMA 2.0입니다. UMA 진화에 대해 더 알고 싶다면 부록 D: UMA 진화를 확인하십시오.

## Use Cases

Chase Bank, Bank of America 및 Wells Fargo에 여러 은행 계좌가 있다고 가정해 보겠습니다. 개인 재무 관리(PFM) 애플리케이션을 통해 모든 은행 계좌를 관리하는 `Peter`라는 재무 관리자를 고용했습니다. 이 애플리케이션은 종종 여러 은행 계좌에서 정보를 가져옴으로써 예산을 더 잘 책정하고 전반적인 재무 상태를 이해하는 데 도움이 됩니다. 여기에서 PFM을 사용하여 은행 계좌에 액세스하려면 `Peter`에게 제한된 액세스 권한을 부여해야 합니다. 우리는 모든 은행이 API를 통해 기능을 노출하고 PFM이 뱅킹 API를 사용하여 데이터를 검색한다고 가정합니다.

매우 높은 수준에서 UMA가 이 문제를 해결하는 방법을 살펴보겠습니다(그림 13-1 참조). 먼저 모든 은행이 신뢰하는 인증 서버에서 액세스 제어 정책을 정의해야 합니다. 이 권한 부여 정책은 Peter에게 PFM 앱을 통해 Wells Fargo, Chase 및 Bank of America 은행 계좌에 대한 읽기 액세스 권한을 부여해야 한다고 말합니다. 그런 다음 각 은행을 승인 서버에 소개해야 하므로 Peter가 귀하의 은행 계좌에 액세스하려고 할 때마다 각 은행은 승인 서버와 통신하여 Peter가 그렇게 할 수 있는지 묻습니다. Peter가 PFM 앱을 통해 은행 계좌에 액세스하려면 PFM 앱이 먼저 인증 서버와 통신해야 하며 Peter를 대신하여 토큰을 받아야 합니다. 이 과정에서 토큰을 발행하기 전에 권한 부여 서버는 사용자가 정의한 액세스 제어 정책을 평가합니다.

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_13_Fig1_HTML.jpg)

Figure 13-1 계정 소유자는 개인 재무 관리 앱을 통해 자신의 계정 관리를 재무 관리자에게 위임합니다.

다른 예를 들어보겠습니다. Google 문서가 있다고 가정해 보겠습니다. 이것을 모든 사람과 공유하는 것이 아니라 foo 및 bar 회사 관리 팀의 모든 사람과 공유하고 싶습니다(그림 13-2 참조). 이것이 UMA와 어떻게 작동하는지 봅시다.

먼저 Google이 신뢰하는 인증 서버가 있으므로 누군가가 Google 문서에 액세스하려고 할 때마다 Google은 인증 서버와 통신하여 해당 사용자에게 액세스 권한이 있는지 확인합니다. 또한 foo 및 bar 회사의 관리자만 Google 문서에 액세스할 수 있다는 정책을 인증 서버에서 정의합니다

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781484220504/files/A323855_2_En_13_Fig2_HTML.jpg)
Figure 13-2 Google 문서 소유자는 특정 역할을 가진 다른 회사의 제3자에게 Google 문서에 대한 액세스 권한을 위임합니다.

사람(예: Peter)이 Google 문서에 액세스하려고 하면 Google이 승인 서버로 리디렉션합니다. 그런 다음 인증 서버는 Peter를 Foo ID 공급자(또는 Peter의 홈 ID 공급자)로 리디렉션합니다. Foo ID 공급자는 Peter를 인증하고 권한 부여 서버에 대한 클레임으로서 Peter의 역할을 다시 보냅니다. 이제 권한 부여 서버는 Peter의 역할과 Peter가 속한 회사를 알고 있기 때문에 Peter가 관리자 역할에 속하는 경우 Google Docs 앱에 토큰을 발급하고 Google Docs API를 통해 해당 Google 문서를 검색하는 데 사용할 수 있습니다.

## UMA 2.0 Roles

UMA는 4장의 OAuth 2.0에서 논의한 네 가지 역할(리소스 소유자, 리소스 서버, 클라이언트 및 권한 부여 서버) 외에 하나의 역할을 더 도입합니다. 다음은 UMA와 관련된 다섯 가지 역할을 모두 나열합니다.

1. 리소스 소유자: 앞의 두 사용 사례에서 귀하는 리소스 소유자입니다. 첫 번째 경우에는 은행 계좌를 소유하고 두 번째 사용 사례에서는 Google 문서를 소유했습니다.

2. 리소스 서버: 보호된 리소스를 호스팅하는 곳입니다. 앞의 첫 번째 사용 사례에서 각 은행은 리소스 서버이고 두 번째 사용 사례에서는 Google Docs API를 호스팅하는 서버가 리소스 서버입니다.

3. 클라이언트: 리소스 소유자를 대신하여 리소스에 액세스하려는 애플리케이션입니다. 앞의 첫 번째 사용 사례에서 개인 재무 관리(PFM) 애플리케이션은 클라이언트이고 두 번째 사용 사례에서는 Google 문서도구 웹 애플리케이션입니다.

4. 권한 부여 서버: 클라이언트 애플리케이션에 OAuth 2.0 액세스 토큰을 발급하는 보안 토큰 서비스(STS) 역할을 하는 엔터티입니다.

5. 요청 당사자: 이것은 UMA의 새로운 기능입니다. 앞의 첫 번째 사용 사례에서는 재무 관리자인 Peter가 요청 당사자이고 두 번째 사용 사례에서는 Foo 회사의 관리자인 Peter가 요청 당사자입니다. 요청 당사자는 리소스 소유자를 대신하여 클라이언트 애플리케이션을 통해 리소스에 액세스합니다.

## UMA Protocol

There are two specifications developed under Kantara Initiative, which define UMA protocol. The core specification is called UMA 2.0 Grant for OAuth 2.0 Authorization. The other one is the Federated Authorization for UMA 2.0, which is optional.

A grant type is an extension point in OAuth 2.0 architecture. UMA 2.0 grant type extends the OAuth 2.0 to support the requesting party role and defines the flow the client application should follow to obtain an access token on behalf of the requesting party from the authorization server.

Let’s see in step by step how UMA 2.0 works, with the first use case we discussed earlier:

1. 1.

First, the account owner has to introduce each of his banks to the UMA authorization server. Here we possibly follow OAuth 2.0 authorization code grant type and provision an access token to the Chase Bank. UMA gives a special name to this token: Protection API Access Token (PAT).

 

2. 2.

The Chase Bank uses the provisioned access token or the PAT to register its resources with the authorization server. Following is a sample cURL command for resource registration. $PAT in the following command is a placeholder for the Protection API Access Token. Here we register the account of the account owner as a resource.

\> curl -v -X POST -H "Authorization:Bearer $PAT" -H "Content-Type: application/json" -d '{"resource_scopes":["view"], "description":"bank account details", "name":"accounts/1112019209", "type":"/accounts"}' https://as.uma.example.com/uma/resourceregistration

 

3. 3.

Peter via the personal financial management (PFM) application tries to access the Chase Bank account with no token.

\> curl –X GET https://chase.com/apis/accounts/1112019209

 

4. 4.

Since there is no token in the request from PFM, the bank API responds back with a 401 HTTP error code, along with the endpoint of the authorization server and a permission ticket. This permission ticket represents the level of permissions PFM needs to do a GET to /accounts API of the Chase Bank. In other words, PFM should get an access token from the provided authorization server, with the provided permissions in the given permission ticket.

 

5. 5.

To generate the permission ticket, the Chase Bank has to talk to the authorization server. As per the following cURL command, Chase Bank also passes resource_id and the resource_scope. The permission API is protected via OAuth 2.0, so the Chase Bank has to pass a valid access token to access it. UMA gives a special name to this token: Protection API Access Token (PAT), which we provisioned to Chase Bank in step 1.

\> curl -v -X POST -H "Authorization:Bearer $PAT" -H "Content-Type: application/json" -d '[{"resource_id":" accounts/1112019209","resource_scopes":["view"]}]' https://as.uma.example.com/uma/permission

{"ticket":"1qw32s-2q1e2s-1rt32g-r4wf2e"}

 

6. 6.

Now the Chase Bank will send the following 401 response to the PFM application.

HTTP/1.1 401 Unauthorized

WWW-Authenticate: UMA realm="chase" as_uri="https://as.uma.example.com" ticket="1qw32s-2q1e2s-1rt32g-r4wf2e "

 

7. 7.

The client application or the PFM now has to talk to the authorization server. By this time, we can assume that Peter, or the requesting party, has already logged in to the client app. If that login happens over OpenID Connect, then PFM has an ID token, which represents Peter. PFM passes both the ID token (as claim_token) and the permission ticket (as ticket) it got from Chase Bank to the authorization server, in the following cURL command. The claim_token is an optional parameter in the request, and if it is present, then there must be claim_token_format parameter as well, which defines the format of the claim_token. In the following cURL command, use a claim_token of the ID token format, and it can be even a SAML token. Here the $APP_CLIENTID and $APP_CLIENTSECRET are the OAuth 2.0 client id and client secret, respectively, you get at the time you register your application (PFM) with the OAuth 2.0 authorization server. The $IDTOKEN is a placeholder for the OpenID Connect ID token, while $TICKET is a placeholder for the permission ticket. The value of the grant_type parameter must be set to urn:ietf:params:oauth:grant-type:uma-ticket. The following cURL command is only an example, and it does not carry all the optional parameters.

\> curl -v -X POST --basic -u $APP_CLIENTID:$APP_CLIENTSECRET

   -H "Content-Type: application/x-www-form-urlencoded;

   charset=UTF-8" -k -d

   "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket&

    claim_token=$IDTOKEN&

    claim_token_format=http://openid.net/specs/openid-connect-core-1_0.html#IDToken&

    ticket=$TICKET"

    https://as.uma.example.com/uma/token

 

8. 8.

As the response to the preceding request, the client application gets an access token, which UMA calls a requesting party token (RPT) , and before authorization server returns back the access token, it internally evaluates any authorization policies defined by the account owner (or the resource owner) to see whether Peter has access to the corresponding bank account.

{

  "token_type":"bearer",

  "expires_in":3600,

  "refresh_token":"22b157546b26c2d6c0165c4ef6b3f736",

  "access_token":"cac93e1d29e45bf6d84073dbfb460"

}

 

9. 9.

Now the application (PFM) tries to access the Chase Bank account with the RPT from the preceding step.

\> curl –X GET –H "Authorization: Bearer cac93e1d29e45bf6d84073dbfb460" https://chase.com/apis/accounts/1112019209

 

10. 10.

The Chase Bank API will now talk to the introspection (see Chapter 9) endpoint to validate the provided RPT and, if the token is valid, will respond back with the corresponding data. If the introspection endpoint is secured, then the Chase Bank API has to pass the PAT in the HTTP authorization header to authenticate.

\> curl -H "Authorization:Bearer $PAT" -H 'Content-Type: application/x-www-form-urlencoded' -X POST --data "token=cac93e1d29e45bf6d84073dbfb460" https://as.uma.example.com/uma/introspection

HTTP/1.1 200 OK

Content-Type: application/json

Cache-Control: no-store

{

  "active": true,

  "client_id":"s6BhdRkqt3",

  "scope": "view",

  "sub": "peter",

  "aud": "accounts/1112019209"

 }

 

11. 11.

Once the Chase Bank finds the token is valid and carries all required scopes, it will respond back to the client application (PFM) with the requested data.

 

> **Note**
>


A recording of a UMA 2.0 demo done by the author of the book to the UMA working group with the open source WSO2 Identity Server is available here: www.youtube.com/watch?v=66aGc5AV7P4.

Interactive Claims Gathering

In the previous section, in step 7, we assumed that the requesting party is already logged in to the client application and the client application knows about the requesting party’s claims, say, for example, in the format of an ID token or a SAML token. The client application passes these claims in the claim_token parameter along with the permission ticket to the token endpoint of the authorization server. This request from the client application to the authorization server is a direct request. In case the client application finds that it does not have enough claims that are required by the authorization server to make an authorization decision based on its policies, the client application can decide to use interactive claim gathering. During the interactive claim gathering, the client application redirects the requesting party to the UMA authorization server. This is what we discussed under the second use case at the very beginning of the chapter, with respect to sharing Google Docs with external companies. The following is a sample request the client application generates to redirect the requesting party to the authorization server.

Host: as.uma.example.com

GET /uma/rqp_claims?client_id=$APP_CLIENTID

&ticket=$TICKET

&claims_redirect_uri=https://client.example.com/redirect_claims

&state=abc

The preceding sample request is an HTTP redirect, which flows through the browser. Here the $APP_CLIENTID is the OAuth 2.0 client id you get at the time you register your application with the UMA authorization server, and $TICKET is a placeholder for the permission ticket the client application gets from the resource server (see step 6 in the previous section). The value of claim_redirect_uri indicates the authorization server, where to send the response back, which points to an endpoint hosted in the client application.

How the authorization server does claim gathering is out of the scope of the UMA specification. Ideally, it can be by redirecting the requesting party again to his/her own home identity provider and getting back the requested claims (see Figure 13-2). Once the claim gathering is completed, the authorization server redirects the user back to the claim_redirect_uri endpoint with a permission ticket, as shown in the following. The authorization server tracks all the claims it gathered against this permission ticket.

HTTP/1.1 302 Found

Location: https://client.example.com/redirect_claims?

ticket=cHJpdmFjeSBpcyBjb250ZXh0LCBjb250cm9s&state=abc

The client application will now talk to the token endpoint of the authorization server with the preceding permission ticket to get a requesting party token (RPT) . This is similar to what we discussed under step 7 in the previous section, but here we do not send a claim_token.

\> curl -v -X POST --basic -u $APP_CLIENTID:$APP_CLIENTSECRET

   -H "Content-Type: application/x-www-form-urlencoded;

   charset=UTF-8" -k -d

   "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket&

    ticket=$TICKET"

    https://as.uma.example.com/uma/token

As the response to the preceding request, the client application gets an access token, which UMA calls a requesting party token (RPT), and before authorization server returns back the access token, it internally evaluates any authorization policies defined by the account owner (or the resource owner) to see whether Peter has access to the corresponding bank account.

{

  "token_type":"bearer",

  "expires_in":3600,

  "refresh_token":"22b157546b26c2d6c0165c4ef6b3f736",

  "access_token":"cac93e1d29e45bf6d84073dbfb460"

}

## Summary

- User-Managed Access (UMA) is an emerging standard built on top of the OAuth 2.0 core specification as a profile.

- UMA still has very few vendor implementations, but it promises to be a highly recognized standard in the near future.

- There are two specifications developed under Kantara Initiative, which define the UMA protocol. The core specification is called the UMA 2.0 Grant for OAuth 2.0 Authorization. The other one is the Federated Authorization for UMA 2.0, which is optional.

- UMA introduces a new role called, requesting party, in addition to the four roles used in OAuth 2.0: the authorization server, the resource server, the resource owner and the client application.