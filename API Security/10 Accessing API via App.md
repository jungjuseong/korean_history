10. Accessing APIs via Native Mobile Apps

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

The adoption of native mobile apps has increased heavily in the last few years. Within the first decade of the 21st century, the Internet users worldwide increased from 350 million to more than 2 billion and mobile phone subscribers from 750 million to 5 billion—and today it hits 6 billion, where the world population is around 7 billion. Most of the mobile devices out there–even the cheapest ones—could be used to access the Internet.

We treat a native mobile application as an untrusted or a public client. A client application, which is not capable of protecting its own keys or credentials, is identified as a public client under OAuth terminology. Since the native mobile apps run on a device owned by the user, the user who is having complete access to the mobile device can figure out any keys the application hides. This is a hard challenge we face in accessing secured APIs from a native mobile application.

In this chapter, we discuss the best practices in using OAuth 2.0 for native apps, Proof Key for Code Exchange (PKCE), which is an approach for protecting native apps from code interception attack and protecting native apps in a browser-less environment.

Mobile Single Sign-On (SSO)

It takes an average of 20 seconds for a user to log in to an application. Not having to enter a password each time a user needs to access a resource saves time and makes users more productive and also reduces the frustration of multiple login events and forgotten passwords. When we have single sign-on, the users will only have one password to remember and update and only one set of password rules to remember. Their initial login provides them with access to all the resources, typically for the entire day or the week.

If you provide multiple mobile applications for your corporate employees to access from their mobile devices, it’s a pain to ask them to re-login to each application independently. Possibly all of them may be sharing the same credential store. This is analogous to a case where Facebook users log in to multiple third-party mobile applications with their Facebook credentials. With Facebook login, you only login once to Facebook and will automatically log into the other applications rely on Facebook login.

In mobile world, login to native apps is done in three different ways: directly asking for user credentials, using a WebView, and using the system browser.

Login with Direct Credentials

With this approach, the user directly provides the credentials to the native app itself (see Figure 10-1). And the app will use an API (or OAuth 2.0 password grant type) to authenticate the user. This approach assumes the native app is trusted. In case your native app uses a third-party identity provider for login, we must not use this. Even this approach may not be possible, unless the third-party identity provider provides a login API or supports OAuth 2.0 password grant type. Also this approach can make the users vulnerable for phishing attacks. An attacker can plant a phishing attack by fooling the user to install a native app with the same look and feel as the original app and then mislead the user to share his or her credentials with it. In addition to this risk, login with direct credentials does not help in building a single sign-on experience, when you have multiple native apps. You need to use your credentials to log in to each individual application.

 

Figure 10-1

The Chase bank’s mobile app, which users directly provide credentials for login

Login with WebView

The native app developers use a WebView in a native app to embed the browser, so that the app can use web technologies such as HTML, JavaScript, and CSS. During the login flow, the native app loads the system browser into a WebView and uses HTTP redirects to get the user to the corresponding identity provider. For example, if you want to authenticate users with Facebook, to your native app, you load the system browser into a WebView first and then redirect the user to Facebook. What’s happening in the browser loaded into the WebView is no different from the flow you see when you log in to a web app via Facebook using a browser.

The WebView-based approach was popular in building hybrid native apps, because it provides better user experience. The users won’t notice the browser being loaded into the WebView. It looks like everything happens in the same native app.

It also has some major disadvantages. The web session under the browser loaded into a WebView of a native app is not shared between multiple native apps. For example, if you do login with Facebook to one native app, by redirecting the user to facebook.com via a browser loaded into a WebView, the user has to log in to Facebook again and again, when multiple native apps follow the same approach. That is because the web session created under facebook.com in one WebView is not shared with another WebView of a different native app. So the single sign-on (SSO) between native apps will not work with the WebView approach.

WebView-based native apps also make the users more vulnerable to phishing attacks. In the same example we discussed before, when a user gets redirected to facebook.com via the system browser loaded into a WebView, he or she won’t be able to figure out whether they are visiting something outside the native app. So, the native app developer can trick the user by presenting something very similar to facebook.com and steal user’s Facebook credentials. Due to this reason, most of the developers are now moving away from using a WebView for login.

Login with a System Browser

This approach for login into a native app is similar to what we discussed in the previous section, but instead of the WebView, the native app spins up the system browser (see Figure 10-2). System browser itself is another native app. User experience in this approach is not as smooth as with the WebView approach, as the user has to switch between two native apps during the login process, but in terms of security, this is the best approach. Also, this is the only approach we can have single sign-on experience in a mobile environment. Unlike WebView approach, when you use the system browser, it manages a single web session for the user. Say, for example, when there are multiple native apps using Facebook login via the same system browser, the users only need to log in to Facebook once. Once a web session is created under facebook.com domain with the system browser, for the subsequent login requests from other native apps, users will be logged in automatically. In the next section, we see how we can use OAuth 2.0 securely to build this use case.

 

Figure 10-2

Login to Foursquare native app using Facebook

Using OAuth 2.0 in Native Mobile Apps

OAuth 2.0 has become the de facto standard for mobile application authentication. In our security design, we need to treat a native app a dumb application. It is very much similar to a single-page application. The following lists out the sequence of events that happen in using OAuth 2.0 to log in to a native mobile app.

 

Figure 10-3

A typical login flow for a native mobile app with OAuth 2.0

1. 1.

Mobile app developer has to register the application with the corresponding identity provider or the OAuth 2.0 authorization server and obtain a client_id. The recommendation is to use OAuth 2.0 authorization code grant type, without a client secret. Since the native app is an untrusted client, there is no point of having a client secret. Some were using implicit grant type for native apps, but it has its own inherent security issues and not recommended any more.

 

2. 2.

Instead of WebView, use SFSafariViewController with iOS9+ or Chrome Custom Tabs for Android. This web controller provides all the benefits of the native system browser in a control that can be placed within an application. Then you can embed the client_id obtained from step 1 into the application. When you embed a client_id into an app, it will be the same for all the instances of that native app. If you want to differentiate each instance of the app (installed in different devices), then we can dynamically generate a client_id for each instance at the start of the app, following the protocol defined in OAuth 2.0 Dynamic Client Registration profile, which we explained in detail in Chapter 9.

 

3. 3.

During the installation of the app, we need to register an app-specific custom URL scheme with the mobile operating system. This URL scheme must match the callback URL or redirect URI you used in step 1, at the time of app registration. A custom URL scheme lets the mobile operating system to pass back the control to your app from another external application, for example from the system browser. If you send some parameters to the app-specific custom URI scheme on the browser, the mobile operating system will track that and invoke the corresponding native app with those parameters.

 

4. 4.

Once the user clicks login, on the native app, we need to spin up the system browser and follow the protocol defined in OAuth 2.0 authorization code grant type (see Figure 10-3), which we discussed in detail in Chapter 4.

 

5. 5.

After the user authenticates to the identity provider, the browser redirects the user back to the registered redirect URI, which is in fact a custom URL scheme registered with the mobile operating system.

 

6. 6.

Upon receiving the authorization code to the custom URL scheme on the system browser, the mobile operating system spins up the corresponding native app and passes over the control.

 

7. 7.

The native app will talk to the token endpoint of the authorization server and exchange the authorization code to an access token.

 

8. 8.

The native app uses the access token to access APIs.

 

Inter-app Communication

The system browser itself is another native app. We used a custom URL scheme as a way of inter-app communication to receive the authorization code from the authorization server. There are multiple ways for inter-app communication available in a mobile environment: private-use URI scheme (also known as custom URL scheme), claimed HTTPS URL scheme, and loopback URI scheme.

Private URI Schemes

In the previous section, we already discussed how a private URI scheme works. When the browser hits with a private URI scheme, it invokes the corresponding native app, registered for that URI scheme, and hands over the control. The RFC 75951 defines guidelines and registration procedures for URI schemes, and according to that, it is recommended to use a domain name that is under your control, in its reverse order as the private URI scheme. For example, if you own app.foo.com, then the private URI scheme should be com.foo.app. The complete private URI scheme may look like com.foo.app:/oauth2/redirect, and there is only one slash that appears right after the scheme component.

In the same mobile environment, the private URI schemes can collide with each other. For example, there can be two apps registered for the same URI scheme. Ideally, this should not happen if you follow the convention we discussed before while choosing an identifier. But still there is an opportunity that an attacker can use this technique to carry out a code interception attack. To prevent such attacks, we must use Proof Key for Code Exchange (PKCE) along with private URI schemes. We discuss PKCE in a later section.

Claimed HTTPS URI Scheme

Just like the private URI scheme, which we discussed in the previous section, when a browser sees a claimed HTTPS URI scheme, instead of loading the corresponding page, it hands over the control to the corresponding native app. In supported mobile operating systems, you can claim an HTTPS domain, which you have control. The complete claimed HTTPS URI scheme may look like https://app.foo.com/oauth2/redirect. Unlike in private URI scheme, the browser verifies the identity of the claimed HTTPS URI before redirection, and for the same reason, it is recommended to use claimed HTTPS URI scheme over others where possible.

Loopback Interface

With this approach, your native app will listen on a given port in the device itself. In other words, your native app acts as a simple web server. For example, your redirect URI will look like http://127.0.0.1:5000/oauth2/redirect. Since we are using the loopback interface (127.0.0.1), when the browser sees this URL, it will hand over the control to the service listening on the mobile device on port 5000. The challenge with this approach is that your app may not be able to run on the same port on all the devices, if there are any other apps on the mobile device already using the same port.

Proof Key for Code Exchange (PKCE)

Proof Key for Code Exchange (PKCE) is defined in the RFC 7636 as a way to mitigate code interception attack (more details in Chapter 14) in a mobile environment. As we discussed in the previous section, when you use a custom URL scheme to retrieve the authorization code from the OAuth authorization server, there can be a case where it goes to a different app, which is also registered with the mobile device for the same custom URL scheme as the original app. An attacker can possibly do this with the intention of stealing the code.

When the authorization code gets to the wrong app, it can exchange it to an access token and then gets access to the corresponding APIs. Since we use authorization code with no client secret in mobile environments, and the client id of the original app is public, the attacker has no issue in exchanging the code to an access token by talking to the token endpoint of the authorization server.

 

Figure 10-4

A typical login flow for a native mobile app with OAuth 2.0 and PKCE

Let’s see how PKCE solves the code interception attack (see Figure 10-4):

1. 1.

The native mobile app, before redirecting the user to the authorization server, generates a random value, which is called the code_verifier. The value of the code_verifier must have a minimum length of 43 characters and a maximum of 128 characters.

 

2. 2.

Next the app has to calculate the SHA256 of the code_verifier and find its base64-url-encoded (see Appendix E) representation, with no padding. Since SHA256 hashing algorithm always results in a hash of 256 bits, when you base64-url-encode it, there will be a padding all the time, which is represented by the = sign. According to the PKCE RFC, we need to remove that padding—and that value, which is the SHA256-hashed, base64-url-encoded, unpadded code_verifier, is known as the code_challenge.

 

3. 3.

Now, when the native app initiates the authorization code request and redirects the user to the authorization server, it has to construct the request URL in the following manner, along with the code_challenge and the code_challenge_method query parameters. The code_challenge_method carries the name of the hashing algorithm.

https://idp.foo.com/authorization?client_id=FFGFGOIPI7898778&scopeopenid&redirect_uri=com.foo.app:/oauth2/redirect&response_type=code&code_challenge=YzfcdAoRg7rAfj9_Fllh7XZ6BBl4PIHC-xoMrfqvWUc&code_challenge_method=S256"

 

4. 4.

At the time of issuing the authorization code, the authorization server must record the provided code_challenge against the issued authorization code. Some authorization servers may embed the code_challenge into the code itself.

 

5. 5.

Once the native app gets the authorization code, it can exchange the code to an access token by talking to the authorization server’s token endpoint. But, when you follow PKCE, you must send the code_verifier (which is corresponding to the code_challenge) along with the token request.

curl -k --user "XDFHKKJURJSHJD" -d "code=XDFHKKJURJSHJD&grant_type=authorization_code&client_id=FFGFGOIPI7898778 &redirect_uri=com.foo.app:/oauth2/redirect&code_verifier=ewewewoiuojslkdjsd9sadoidjalskdjsdsdewewewoiuojslkdjsd9sadoidjalskdjsdsd" https://idp.foo.com/token

 

6. 6.

If the attacker’s app gets the authorization code, it still cannot exchange it to an access token, because only the original app knows the code_verifier.

 

7. 7.

Once the authorization server receives the code_verifier along with the token request, it will find the SHA256-hashed, base64-url-encoded, unpadded value of it and compare it with the recorded code_challenge. If those two match, then it will issue the access token.

 

Browser-less Apps

So far in this chapter, we only discussed about mobile devices, which are capable of spinning up a web browser. There is another growing requirement to use OAuth secured APIs from applications running on devices with input constraints and no web browser, such as smart TVs, smart speakers, printers, and so on. In this section, we discuss how to access OAuth 2.0 protected APIs from browser-less apps using the OAuth 2.0 device authorization grant. In any case, the device authorization grant does not replace any of the approaches we discussed earlier with respect to native apps running on capable mobile devices.

OAuth 2.0 Device Authorization Grant

The OAuth 2.0 device authorization grant2 is the RFC 8628, which is published by the IETF OAuth working group. According to this RFC, a device to use the device authorization grant type must satisfy the following requirements:

- The device is already connected to the Internet or to the network, which has access to the authorization server.

- The device is able to make outbound HTTPS requests.

- The device is able to display or otherwise communicate a URI and code sequence to the user.

- The user has a secondary device (e.g., personal computer or smartphone) from which they can process a request.

Let’s see how device authorization grant works, with an example. Say we have a YouTube app running on a smart TV, and we need the smart TV to access our YouTube account on behalf of us. In this case, YouTube acts as both the OAuth authorization server and the resource server, and the YouTube app running on the smart TV is the OAuth client application.

 

Figure 10-5

A typical login flow for a browser-less app with OAuth 2.0

1. 1.

The user takes the TV remote and clicks the YouTube app to associate his/her YouTube account with the app.

 

2. 2.

The YouTube app running on the smart TV has an embedded client ID and sends a direct HTTP request over HTTPS to the authorization server.

POST /device_authorization HTTP/1.1

Host: idp.youtube.com

Content-Type: application/x-www-form-urlencoded

client_id=XDFHKKJURJSHJD

 

3. 3.

In response to the preceding request, the authorization server returns back a device_code, a user_code, and a verification URI. Both the device_code and the user_code have an expiration time associated with them, which is communicated to the client app via expires_in parameter (in seconds).

HTTP/1.1 200 OK

Content-Type: application/json

Cache-Control: no-store

{

  "device_code": "GmRhmhcxhwAzkoEqiMEg_DnyEysNkuNhszIySk9eS",

  "user_code": "WDJB-MJHT",

  "verification_uri": "https://youtube.com/device",

  "verification_uri_complete":

            "https://youtube.com/device?user_code=WDJB-MJHT",

  "expires_in": 1800,

  "interval": 5

}

 

4. 4.

The YouTube client app instructs the user to visit the provided verification URI (from the preceding response) and confirm the authorization request with the provided user code (from the preceding response).

 

5. 5.

Now the user has to use a secondary device (a laptop or mobile phone) to visit the verification URI. While that action is in progress, the YouTube app will keep polling the authorization server to see whether the user has confirmed the authorization request. The minimum amount of time the client should wait before polling or the time between polling is specified by the authorization server in the preceding response under the interval parameter. The poll request to the token endpoint of the authorization server includes three parameters. The grant_type parameter must carry the value urn:ietf:params:oauth:grant-type:device_code, so the authorization server knows how to process this request. The device_code parameter carries the device code issued by the authorization server in its first response, and the client_id parameter carries the client identifier of the YouTube app.

POST /token HTTP/1.1

Host: idp.youtube.com

Content-Type: application/x-www-form-urlencoded

grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code

&device_code=GmRhmhcxhwAzkoEqiMEg_DnyEysNkuNhszIySk9eS

&client_id=459691054427

 

6. 6.

The user visits the provided verification URI, enters the user code, and confirms the authorization request.

 

7. 7.

Once the user confirms the authorization request, the authorization server issues the following response to the request in step 5. This is the standard response from an OAuth 2.0 authorization server token endpoint.

HTTP/1.1 200 OK

Content-Type: application/json;charset=UTF-8

Cache-Control: no-store

Pragma: no-cache

{

       "access_token":"2YotnFZFEjr1zCsicMWpAA",

       "token_type":"Bearer",

       "expires_in":3600,

       "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",

 }

 

8. 8.

Now the YouTube app can use this access token to access the YouTube API on behalf of the user.

 

## Summary

- There are multiple grant types in OAuth 2.0; however, while using OAuth 2.0 to access APIs from a native mobile app, it is recommended to use authorization code grant type, along with Proof Key for Code Exchange (PKCE).

- PKCE protects the native apps from code interception attack.

- The use of browser-less devices such as smart TVs, smart speakers, printers, and so on is gaining popularity.

- The OAuth 2.0 device authorization grant defines a standard flow to use OAuth 2.0 from a browser-less device and gain access to APIs.

Footnotes

 

