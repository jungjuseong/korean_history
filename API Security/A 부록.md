The Evolution of Identity Delegation

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

Identity delegation plays a key role in securing APIs. Most of the resources on the Web today are exposed over APIs. The Facebook API exposes your Facebook wall, the Twitter API exposes your Twitter feed, Flickr API exposes your Flickr photos, Google Calendar API exposes your Google Calendar, and so on. You could be the owner of a certain resource (Facebook wall, Twitter feed, etc.) but not the direct consumer of an API. There may be a third party who wants to access an API on your behalf. For example, a Facebook app may want to import your Flickr photos on behalf of you. Sharing credentials with a third party who wants to access a resource you own on your behalf is an antipattern. Most web-based applications and APIs developed prior to 2006 utilized credential sharing to facilitate identity delegation. Post 2006, many vendors started developing their own proprietary ways to address this concern without credential sharing. Yahoo! BBAuth, Google AuthSub, and Flickr Authentication are some of the implementations that became popular.

A typical identity delegation model has three main roles: delegator, delegate, and service provider. The delegator owns the resource and is also known as the resource owner. The delegate wants to access a service on behalf of the delegator. The delegator delegates a limited set of privileges to the delegate to access the service. The service provider hosts the protected service and validates the legitimacy of the delegate. The service provider is also known as the resource server.

Direct Delegation vs. Brokered Delegation

Let’s take a step back and look at a real-world example (see Figure A-1). Flickr is a popular cloud-based service for storing and sharing photos. Photos stored in Flickr are the resources, and Flickr is the resource server or the service provider. Say you have a Flickr account: you’re the resource owner (or the delegator) of the photos under your account. You also have a Snapfish account. Snapfish is a web-based photo-sharing and photo-printing service that is owned by Hewlett-Packard. How can you print your Flickr photos from Snapfish? To do so, Snapfish has to first import those photos from Flickr and should have the privilege to do so, which should be delegated to Snapfish by you. You’re the delegator, and Snapfish is the delegate. Other than the privilege to import photos, Snapfish won’t be able to do any of the following with your Flickr photos:

- Access your Flickr account (including private content)

- Upload, edit, and replace photos and videos in the account

- Interact with other members’ photos and videos (comment, add notes, favorite)

 

Figure A-1

Direct delegation. The resource owner delegates privileges to the client application

Snapfish can now access your Flickr account on your behalf with the delegated privileges. This model is called direct delegation : the delegator directly delegates a subset of his or her privileges to a delegate. The other model is called indirect delegation : the delegator first delegates to an intermediate delegate, and that delegate delegates to another delegate. This is also known as brokered delegation (see Figure A-2).

 

Figure A-2

Brokered delegation. The resource owner delegates privileges to an intermediate application and that application delegates privileges to another application

Let’s say you have a Lucidchart account. Lucidchart is a cloud-based design tool that you can use to draw a wide variety of diagrams. It also integrates with Google Drive. From your Lucidchart account, you have the option to publish completed diagrams to your Google Drive. To do that, Lucidchart needs privileges to access the Google Drive API on your behalf, and you need to delegate the relevant permissions to Lucidchart. If you want to print something from Lucidchart, it invokes the Snapfish printing API. Snapfish needs to access the diagrams stored in your Google Drive. Lucidchart has to delegate a subset of the permissions you delegated to it to Snapfish. Even though you granted read/write permissions to Lucidchart, it only has to delegate read permission to Snapfish to access your Google Drive and print the selected drawings.

The Evolution

The modern history of identity delegation can be divided into two eras: pre-2006 and post-2006. Credential sharing mostly drove identity delegation prior to 2006. Twitter, SlideShare, and almost all the web applications used credential sharing to access third-party APIs. As shown in Figure A-3, when you created a Twitter account prior to 2006, Twitter asked for your email account credentials so it could access your email address book and invite your friends to join Twitter. Interestingly, it displayed the message “We don’t store your login, your password is submitted securely, and we don’t email without your permission” to win user confidence. But who knows—if Twitter wanted to read all your emails or do whatever it wanted to your email account, it could have done so quite easily.

 

Figure A-3

Twitter, pre-2006

SlideShare did the same thing. SlideShare is a cloud-based service for hosting and sharing slides. Prior to 2006, if you wanted to publish a slide deck from SlideShare to a Blogger blog, you had to give your Blogger username and password to SlideShare, as shown in Figure A-4. SlideShare used Blogger credentials to access its API to post the selected slide deck to your blog. If SlideShare had wanted to, it could have modified published blog posts, removed them, and so on.

 

Figure A-4

SlideShare, pre-2006

These are just two examples. The pre-2006 era was full of such applications. Google Calendar, introduced in April 2006, followed a similar approach. Any third-party application that wanted to create an event in your Google Calendar first had to request your Google credentials and use them to access the Google Calendar API. This wasn’t tolerable in the Internet community, and Google was pushed to invent a new and, of course, better way of securing its APIs. Google AuthSub was introduced toward the end of 2006 as a result. This was the start of the post-2006 era of identity delegation.

Google ClientLogin

In the very early stages of its deployment, the Google Data API was secured with two nonstandard security protocols: ClientLogin and AuthSub. ClientLogin was intended to be used by installed applications. An installed application can vary from a simple desktop application to a mobile application—but it can’t be a web application. For web applications, the recommended way was to use AuthSub.

> **Note**
>


The complete Google ClientLogin documentation is available at https://developers.google.com/accounts/docs/AuthForInstalledApps. The ClientLogin API was deprecated as of April 20, 2012. According to the Google deprecation policy, it operated the same until April 20, 2015.

As shown in Figure A-5, Google ClientLogin uses identity delegation with password sharing. The user has to share his Google credentials with the installed application in the first step. Then the installed application creates a request token out of the credentials, and it calls the Google Accounts Authorization service. After the validation, a CAPTCHA challenge is sent back as the response. The user must respond to the CAPTCHA and is validated again against the Google Accounts Authorization service. Once the user is validated successfully, a token is issued to the application. Then the application can use the token to access Google services.

 

Figure A-5

Google ClientLogin

Google AuthSub

Google AuthSub was the recommended authentication protocol to access Google APIs via web applications in the post-2006 era. Unlike ClientLogin, AuthSub doesn’t require credential sharing. Users don’t need to provide credentials for a third-party web application—instead, they provide credentials directly to Google, and Google shares a temporary token with a limited set of privileges with the third-party web application. The third-party application uses the temporary token to access Google APIs. Figure A-6 explains the protocol flow in detail.

 

Figure A-6

Google AuthSub

The end user initiates the protocol flow by visiting the web application. The web application redirects the user to the Google Accounts Authorization service with an AuthSub request. Google notifies the user of the access rights (or the privileges) requested by the application, and the user can approve the request by login. Once approved by the user, Google Accounts Authorization service provides a temporary token to the web application. Now the web application can use that temporary token to access Google APIs.

> **Note**
>


The complete Google AuthSub documentation is available at https://developers.google.com/accounts/docs/AuthSub. How to use AuthSub with the Google Data API is explained at https://developers.google.com/gdata/docs/auth/authsub. The AuthSub API was deprecated as of April 20, 2012. According to the Google deprecation policy, it operated the same until April 20, 2015.

Flickr Authentication API

Flickr is a popular image/video hosting service owned by Yahoo!. Flickr was launched in 2004 (before the acquisition by Yahoo! in 2005), and toward 2005 it exposed its services via a public API. It was one of the very few companies at that time that had a public API; this was even before the Google Calendar API. Flickr was one of the very few applications that followed an identity delegation model without credential sharing prior to 2006. Most of the implementations that came after that were highly influenced by the Flickr Authentication API. Unlike in Google AuthSub or ClientLogin, the Flickr model was signature based. Each request should be signed by the application from its application secret.

Yahoo! Browser–Based Authentication (BBAuth)

Yahoo! BBAuth was launched in September 2006 as a generic way of granting third-party applications access to Yahoo! data with a limited set of privileges. Yahoo! Photos and Yahoo! Mail were the first two services to support BBAuth. BBAuth, like Google AuthSub, borrowed the same concept used in Flickr (see Figure A-7).

 

Figure A-7

Yahoo! BBAuth

The user first initiates the flow by visiting the third-party web application. The web application redirects the user to Yahoo!, where the user has to log in and approve the access request from the third-party application. Once approved by the user, Yahoo! redirects the user to the web application with a temporary token. Now the third-party web application can use the temporary token to access user’s data in Yahoo! with limited privileges.

> **Note**
>


The complete guide to Yahoo! BBAuth is available at http://developer.yahoo.com/bbauth/.

OAuth

Google AuthSub, Yahoo! BBAuth, and Flickr Authentication all made considerable contributions to initiate a dialog to build a common standardized delegation model. OAuth 1.0 was the first step toward identity delegation standardization. The roots of OAuth go back to November 2006, when Blaine Cook started developing an OpenID implementation for Twitter. In parallel, Larry Halff of Magnolia (a social bookmarking site) was thinking about integrating an authorization model with OpenID (around this time, OpenID began gaining more traction in the Web 2.0 community). Larry started discussing the use of OpenID for Magnolia with Twitter and found out there is no way to delegate access to Twitter APIs through OpenID. Blaine and Larry, together with Chris Messina, DeWitt Clinton, and Eran Hammer, started a discussion group in April 2007 to build a standardized access delegation protocol—which later became OAuth. The access delegation model proposed in OAuth 1.0 wasn’t drastically different from what Google, Yahoo!, and Flickr already had.

> **Note**
>


OpenID is a standard developed by the OpenID Foundation for decentralized single sign-on. The OpenID 2.0 final specification is available at http://openid.net/specs/openid-authentication-2_0.html.

The OAuth 1.0 core specification was released in December 2007. Later, in 2008, during the 73rd Internet Engineering Task Force (IETF) meeting, a decision was made to develop OAuth under the IETF. It took some time to be established in the IETF, and OAuth 1.0a was released as a community specification in June 2009 to fix a security issue related to a session fixation attack.1 In April 2010, OAuth 1.0 was released as RFC 5849 under the IETF.

> **Note**
>
> The OAuth 1.0 community specification is available at http://oauth.net/core/1.0/, and OAuth 1.0a is at http://oauth.net/core/1.0a/. Appendix B explains OAuth 1.0 in detail.

In November 2009, during the Internet Identity Workshop (IIW), Dick Hardt of Microsoft, Brian Eaton of Google, and Allen Tom of Yahoo! presented a new draft specification for access delegation. It was called Web Resource Authorization Profiles (WRAP), and it was built on top of the OAuth 1.0 model to address some of its limitations. In December 2009, WRAP was deprecated in favor of OAuth 2.0.

> **Note**
>


The WRAP specification contributed to the IETF OAuth working group is available at http://tools.ietf.org/html/draft-hardt-oauth-01.

While OAuth was being developed under the OAuth community and the IETF working group, the OpenID community also began to discuss a model to integrate OAuth with OpenID. This effort, initiated in 2009, was called OpenID/OAuth hybrid extension (see Figure A-8). This extension describes how to embed an OAuth approval request into an OpenID authentication request to allow combined user approval. For security reasons, the OAuth access token isn’t returned in the OpenID authentication response. Instead, a mechanism to obtain the access token is provided.

> **Note**
>
> The finalized specification for OpenID/OAuth extension is available at http://step2.googlecode.com/svn/spec/openid_oauth_extension/latest/openid_oauth_extension.html.


Figure A-8 The evolution of identity protocols from OpenID to OpenID Connect

OAuth 1.0 provided a good foundation for access delegation. However, criticism arose against OAuth 1.0, mainly targeting its usability and extensibility. As a result, OAuth 2.0 was developed as an authorization framework, rather than a standard protocol. OAuth 2.0 became the RFC 6749 in October 2012 under the IETF.