
13. User-Managed Access

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

OAuth 2.0 introduced an authorization framework for access delegation. It lets Bob delegate read access to his Facebook wall to a third-party application, without sharing Facebook credentials. User-Managed Access (UMA, pronounced “OOH-mah”) extends this model to another level, where Bob can not only delegate access to a third-party application but also to Peter who uses the same third-party application.

UMA is an OAuth 2.0 profile. OAuth 2.0 decouples the resource server from the authorization server. UMA takes one step further: it lets you control a distributed set of resource servers from a centralized authorization server. Also the resource owner can define a set of policies at the authorization server, which can be evaluated at the time a client is granted access to a protected resource. This eliminates the need of having the presence of the resource owner to approve access requests from arbitrary clients or requesting parties. The authorization server can make the decision based on the policies defined by the resource owner.

The latest version of UMA, which we discuss in this chapter, is UMA 2.0. If you are interested in learning more about UMA evolution, please check Appendix D: UMA Evolution.

Use Cases

Let’s say you have multiple bank accounts with Chase Bank, Bank of America, and Wells Fargo. You have hired a financial manager called Peter, who manages all your bank accounts through a personal financial management (PFM) application, which helps to budget better and understand the overall financial position, by often pulling information from multiple bank accounts. Here, you need to give limited access to Peter, to use the PFM to access your bank accounts. We assume all the banks expose their functionality over APIs and PFM uses banking APIs to retrieve data.

At a very high level, let’s see how UMA solves this problem (see Figure 13-1). First you need to define an access control policy at the authorization server, which all your banks trust. This authorization policy would say Peter should be given read access via the PFM app to Wells Fargo, Chase, and Bank of America bank accounts. Then you also need to introduce each bank to the authorization server, so whenever Peter tries to access your bank accounts, each bank talks to the authorization server and asks whether Peter is allowed to do that. For Peter to access a bank account via PFM app, the PFM app first needs to talk to the authorization server and gets a token on behalf of Peter. During this process, before issuing the token, the authorization server evaluates the access control policy you defined.

 

Figure 13-1

An account owner delegates the administration of his/her accounts to a Financial Manager via a Personal Financial Management App

Let’s take another example. Say you have a Google Doc. You do not want to share this with everyone, but with anyone from the management team of foo and bar companies (see Figure 13-2). Let’s see how this works with UMA.

First you have an authorization server, which Google trusts, so whenever someone wants to access your Google Doc, Google talks to the authorization server to see whether that person has the rights to do so. You also define a policy at the authorization server, which says only the managers from foo and bar companies can access your Google Doc.

 

Figure 13-2

A Google Doc owner delegates access to a Google Doc to a third party from a different company with specific roles

When a person (say Peter) tries to access your Google Doc, Google will redirect you to the authorization server. Then the authorization server will redirect Peter to Foo identity provider (or the home identity provider of Peter). Foo identity provider will authenticate Peter and send back Peter’s role as a claim to the authorization server. Now, since authorization server knows Peter’s role, and also the company Peter belongs to, if Peter belongs to a manager role, it will issue a token to Google Docs app, which it can use to retrieve the corresponding Google Doc via the Google Docs API.

UMA 2.0 Roles

UMA introduces one more role in addition to the four roles (resource owner, resource server, client, and authorization server) we discussed under OAuth 2.0, in Chapter 4. The following lists out all five roles involved in UMA:

1. 1.

Resource owner: In the preceding two use cases, you are the resource owner. In the first case, you owned the bank account, and in the second use case, you owned the Google Doc.

 

2. 2.

Resource server: This is the place which hosts protected resources. In the preceding first use case, each bank is a resource server—and in the second use case, the server, which hosts Google Docs API, is the resource server.

 

3. 3.

Client: This is the application, which wants to access a resource on behalf of the resource owner. In the preceding first use case, the personal financial management (PFM) application is the client, and in the second use case, it is the Google Docs web application.

 

4. 4.

Authorization server: This is the entity, which acts as the security token service (STS) to issue OAuth 2.0 access tokens to client applications.

 

5. 5.

Requesting party: This is something new in UMA. In the preceding first use case, Peter, the financial manager, is the requesting party, and in the second use case, Peter who is a manager at Foo company is the requesting party. The requesting party accesses a resource via a client application, on behalf of the resource owner.

 

UMA Protocol

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