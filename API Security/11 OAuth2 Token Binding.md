11. OAuth 2.0 Token Binding

Prabath Siriwardena1 

(1)

San Jose, CA, USA

 

Most of the OAuth 2.0 deployments do rely upon bearer tokens. A bearer token is like “cash.” If I steal 10 bucks from you, I can use it at a Starbucks to buy a cup of coffee—no questions asked. I do not need to prove that I own the ten-dollar note. Unlike cash, if I use my credit card, I need to prove the possession. I need to prove I own it. I need to sign to authorize the transaction, and it’s validated against the signature on the card. The bearer tokens are like cash—once stolen, an attacker can use it to impersonate the original owner. Credit cards are like proof of possession (PoP) tokens.

OAuth 2.0 recommends using Transport Layer Security (TLS) for all the interactions between the client, authorization server, and resource server. This makes the OAuth 2.0 model quite simple with no complex cryptography involved—but at the same time, it carries all the risks associated with a bearer token. There is no second level of defense. Also not everyone is fully bought into the idea of using OAuth 2.0 bearer tokens—just trusting the underlying TLS communication. I’ve met several people—mostly from the financial domain—who are reluctant to use OAuth 2.0, just because of the bearer tokens.

An attacker may attempt to eavesdrop authorization code/access token/refresh token (see Chapter 4 for details) in transit from the authorization server to the client, using any of the following means:

- Malware installed in the browser (public clients).

- Browser history (public clients/URI fragments).

- Intercept the TLS communication between the client and the authorization server or the resource server (exploiting the vulnerabilities in the TLS layer like Heartbleed and Logjam).

- TLS is point to point (not end to end)—an attacker having access to a proxy server could simply log all the tokens. Also, in many production deployments, the TLS connection is terminated at the edge, and from there onward, it’s either a new TLS connection or a plain HTTP connection. In either case, as soon as a token leaves the channel, it’s no more secure.

Understanding Token Binding

OAuth 2.0 token binding proposal cryptographically binds security tokens to the TLS layer, preventing token export and replay attacks. It relies on TLS—and since it binds the tokens to the TLS connection itself, anyone who steals a token cannot use it over a different channel.

We can break down the token binding protocol into three main phases (see Figure 11-1).

 

Figure 11-1

Three main phases in the token binding protocol

Token Binding Negotiation

During the negotiation phase, the client and the server negotiate a set of parameters to use for token binding between them. This is independent of the application layer protocols—as it happens during the TLS handshake (see Appendix C). We discuss more about this in the next section. The token binding negotiation is defined in the RFC 8472. Keep in mind we do not negotiate any keys in this phase, only the metadata.

Key Generation

During the key generation phase, the client generates a key pair according to the parameters negotiated in the negotiation phase. The client will have a key pair for each host it talks to (in most of the cases).

Proof of Possession

During the proof of possession phase, the client uses the keys generated in the key generation phase to prove the possession. Once the keys are agreed upon, in the key generation phase, the client proves the possession of the key by signing the exported keying material (EKM) from the TLS connection. The RFC 5705 allows an application to get additional application-specific keying material derived from the TLS master secret (see Appendix C). The RFC 8471 defines the structure of the token binding message, which includes the signature and other key materials, but it does not define how to carry the token binding message from the client to the server. It’s up to the higher-level protocols to define it. The RFC 8473 defines how to carry the token binding message over an HTTP connection (see Figure 11-2).

 

Figure 11-2

The responsibilities of each layer in a token binding flow

TLS Extension for Token Binding Protocol Negotiation

To bind security tokens to the TLS connection, the client and the server need to first agree upon the token binding protocol (we’ll discuss about this later) version and the parameters (signature algorithm, length) related to the token binding key. This is accomplished by a new TLS extension without introducing additional network roundtrips in TLS 1.2 and earlier versions.

The token binding protocol version reflects the protocol version defined by the Token Binding Protocol (RFC 8471)—and the key parameters are defined by the same specification itself.

The client uses the Token Binding TLS extension to indicate the highest supported token binding protocol version and key parameters. This happens with the Client Hello message in the TLS handshake. To support the token binding specification, both the client and the server should support the token binding protocol negotiation extension.

The server uses the Token Binding TLS extension to indicate the support for the token binding protocol and to select the protocol version and key parameters. The server that supports token binding and receives a Client Hello message containing the Token Binding extension will include the Token Binding extension in the Server Hello if the required conditions are satisfied.

If the Token Binding extension is included in the Server Hello and the client supports the token binding protocol version selected by the server, it means that the version and key parameters have been negotiated between the client and the server and shall be definitive for the TLS connection. If the client does not support the token binding protocol version selected by the server, then the connection proceeds without token binding.

Every time a new TLS connection is negotiated (TLS handshake) between the client and the server, a token binding negotiation happens too. Even though the negotiation happens repeatedly by the TLS connection, the token bindings (you will learn more about this later) are long-lived; they encompass multiple TLS connections and TLS sessions between a given client and server.

In practice, Nginx (https://github.com/google/ngx_token_binding) and Apache (https://github.com/zmartzone/mod_token_binding) have support for token binding. An implementation of Token Binding Protocol Negotiation TLS Extension in Java is available here: https://github.com/pingidentity/java10-token-binding-negotiation.

Key Generation

The Token Binding Protocol specification (RFC 8471) defines the parameters related to key generation. These are the ones agreed upon during the negotiation phase.

- If rsa2048_pkcs1.5 key parameter is used during the negotiation phase, then the signature is generated using the RSASSA-PKCS1-v1_5 signature scheme as defined in RFC 3447 with SHA256 as the hash function.

- If rsa2048_pss key parameter is used during the negotiation phase, then the signature is generated using the RSASSA-PSS signature scheme as defined in RFC 3447 with SHA256 as the hash function.

- If ecdsap256 key parameter is used during the negotiation phase, the signature is generated with ECDSA using Curve P-256 and SHA256 as defined in ANSI.X9–62.2005 and FIPS.186–4.2013.

In case a browser acts as the client, then the browser itself has to generate the keys and maintain them against the hostname of the server. You can find the status of this feature development for Chrome from here (www.chromestatus.com/feature/5097603234529280). Then again the token binding is not only for a browser, it’s useful in all the interactions between a client and a server—irrespective of the client being thin or thick.

Proof of Possession

A token binding is established by a user agent (or the client) generating a private/public key pair (possibly, within a secure hardware module, such as trusted platform module (TPM)) per target server, providing the public key to the server, and proving the possession of the corresponding private key, on every TLS connection to the server. The generated public key is reflected in the token binding ID between the client and the server. At the server end, the verification happens in two steps.

First, the server receiving the token binding message needs to verify that the key parameters in the message match with the token binding parameters negotiated and then validate the signature contained in the token binding message. All the key parameters and the signature are embedded into the token binding message.

The structure of the token binding message is defined in the Token Binding Protocol specification (RFC 8471). A token binding message can have multiple token bindings (see Figure 11-3). A given token binding includes the token binding ID, the type of the token binding (provided or referred—we’ll talk about this later), extensions, and the signature over the concatenation of exported keying material (EKM) from the TLS layer, token binding type, and key parameters. The token binding ID reflects the derived public key along with the key parameters agreed upon the token binding negotiation.

Once the TLS connection is established between a client and a server, the EKM will be the same—both at the client end and at the server end. So, to verify the signature, the server can extract the EKM from the underneath TLS connection and use the token binding type and key parameters embedded into the token binding message itself. The signature is validated against the embedded public key (see Figure 11-3).

 

Figure 11-3

The structure of the token binding message

How to carry the token binding message from the client to the server is not defined in the Token Binding Protocol specification, but in the Token Binding for HTTP specification or the RFC 8473. In other words, the core token binding specification lets the higher-level protocols make the decision on that. The Token Binding for HTTP specification introduces a new HTTP header called Sec-Token-Binding —and it carries the base64url-encoded value of the token binding message. The Sec-Token-Binding header field MUST NOT be included in HTTP responses—MUST include only once in an HTTP request.

Once the token binding message is accepted as valid, the next step is to make sure that the security tokens carried in the corresponding HTTP connection are bound to it. Different security tokens can be transported over HTTP—for example, cookies and OAuth 2.0 tokens. In the case of OAuth 2.0, how the authorization code, access token, and refresh token are bound to the HTTP connection is defined in the OAuth 2.0 Token Binding specification (https://tools.ietf.org/html/draft-ietf-oauth-token-binding-08).

Token Binding for OAuth 2.0 Refresh Token

Let’s see how the token binding works for OAuth 2.0 refresh tokens . A refresh token, unlike authorization code and access token, is only used between the client and the authorization server. Under the OAuth 2.0 authorization code grant type, the client first gets the authorization code and then exchanges it to an access token and a refresh token by talking to the token endpoint of the OAuth 2.0 authorization server (see Chapter 4 for details). The following flow assumes the client has already got the authorization code (see Figure 11-4).

 

Figure 11-4

OAuth 2.0 refresh grant type

1. 1.

The connection between the client and the authorization server must be on TLS.

 

2. 2.

The client which supports OAuth 2.0 token binding, during the TLS handshake itself, negotiates the required parameters with the authorization server, which too supports OAuth 2.0 token binding.

 

3. 3.

Once the TLS handshake is completed, the OAuth 2.0 client will generate a private key and a public key and will sign the exported keying material (EKM) from the underlying TLS connection with the private key—and builds the token binding message. (To be precise, the client will sign EKM + token binding type + key parameters.)

 

4. 4.

The base64url-encoded token binding message will be added as the value to the Sec-Token-Binding HTTP header to the connection between the client and the OAuth 2.0 authorization server.

 

5. 5.

The client will send a standard OAuth request to the token endpoint along with the Sec-Token-Binding HTTP header.

 

6. 6.

The authorization server validates the value of Sec-Token-Binding header, including the signature, and records the token binding ID (which is also included in the token binding message) against the issued refresh token. To make the process stateless, the authorization server can include the hash of the token binding ID into the refresh token itself—so it does not need to remember/store it separately.

 

7. 7.

Later, the OAuth 2.0 client tries to use the refresh token against the same token endpoint to refresh the access token. Now, the client has to use the same private key and public key pair used before to generate the token binding message and, once again, includes the base64url-encoded value of it to the Sec-Token-Binding HTTP header. The token binding message has to carry the same token binding ID as in the case where the refresh token was originally issued.

 

8. 8.

The OAuth 2.0 authorization server now must validate the Sec-Token-Binding HTTP header and then needs to make sure that the token binding ID in the binding message is the same as the original token binding ID attached to the refresh token in the same request. This check will make sure that the refresh token cannot be used outside the original token binding. In case the authorization server decides to embed the hashed value of the token binding ID to the refresh token itself, now it has to calculate the hash of the token binding ID in the Sec-Token-Binding HTTP header and compare it with what is embedded into the refresh token.

 

9. 9.

If someone steals the refresh token and is desperate to use it outside the original token binding, then he/she also has to steal the private/public key pair corresponding to the connection between the client and the server.

 

There are two types of token bindings—and what we discussed with respect to the refresh token is known as provided token binding. This is used when the token exchange happens directly between the client and the server. The other type is known as referred token binding, which is used when requesting tokens, which are intended to present to a different server—for example, the access token. The access token is issued in a connection between the client and the authorization server—but used in a connection between the client and the resource server.

Token Binding for OAuth 2.0 Authorization Code/Access Token

Let’s see how the token binding works for access tokens, under the authorization code grant type. Under the OAuth 2.0 authorization code grant type, the client first gets the authorization code via the browser (user agent) and then exchanges it to an access token and a refresh token by talking to the token endpoint of the OAuth 2.0 authorization server (see Figure 11-5).

 

Figure 11-5

OAuth 2.0 authorization code flow

1. 1.

When the end user clicks the login link on the OAuth 2.0 client application on the browser, the browser has to do an HTTP GET to the client application (which is running on a web server), and the browser has to establish a TLS connection with the OAuth 2.0 client first. The browser, which supports OAuth 2.0 token binding, during the TLS handshake itself, negotiates the required parameters with the client application, which too supports OAuth 2.0 token binding. Once the TLS handshake is completed, the browser will generate a private key and public key (for the client domain) and will sign the exported keying material (EKM) from the underlying TLS connection with the private key—and builds the token binding message. The base64url-encoded token binding message will be added as the value to the Sec-Token-Binding HTTP header to the connection between the browser and the OAuth 2.0 client—which is the HTTP GET.

 

2. 2.

In response to step 1 (assuming all the token binding validations are done), the client will send a 302 response to the browser, asking to redirect the user to the OAuth 2.0 authorization server. Also in the response, the client will include the HTTP header Include-Referred-Token-Binding-ID, which is set to true. This instructs the browser to include the token binding ID established between the browser and the client in the request to the authorization server. Also, the client application will include two additional parameters in the request: code_challenge and code_challenge_method. These parameters are defined in the Proof Key for Code Exchange (PKCE) or RFC 7636 for OAuth 2.0. Under token binding, these two parameters will carry static values, code_challenge=referred_tb and code_challenge_method=referred_tb.

 

3. 3.

The browser, during the TLS handshake itself, negotiates the required parameters with the authorization server. Once the TLS handshake is completed, the browser will generate a private key and public key (for the authorization server domain) and will sign the exported keying material (EKM) from the underlying TLS connection with the private key—and builds the token binding message. The client will send the standard OAuth request to the authorization endpoint along with the Sec-Token-Binding HTTP header. This Sec-Token-Binding HTTP header now includes two token bindings (in one token binding message—see Figure 11-3), one for the connection between the browser and the authorization server, and the other one is for the browser and the client application (referred binding).

 

4. 4.

The authorization server redirects the user back to the OAuth client application via browser—along with the authorization code. The authorization code is issued against the token binding ID in the referred token binding.

 

5. 5.

The browser will do a POST to the client application, which also includes the authorization code from the authorization server. The browser will use the same token binding ID established between itself and the client application—and adds the Sec-Token-Binding HTTP header.

 

6. 6.

Once the client application gets the authorization code (and given that the Sec-Token-Binding validation is successful), it will now talk to the authorization server’s token endpoint. Prior to that, the client has to establish a token binding with the authorization server. The token request will also include the code_verifier parameter (defined in the PKCE RFC), which will carry the provided token binding ID between the client and the browser—which is also the token binding ID attached to the authorization code. Since the access token, which will be issued by the authorization server, is going to be used against a protected resource, the client has to include the token binding between itself and the resource server into this token binding message as a referred binding. Upon receiving the token request, the OAuth 2.0 authorization server now must validate the Sec-Token-Binding HTTP header and then needs to make sure that the token binding ID in the code_verifier parameter is the same as the original token binding ID attached to the authorization code at the point of issuing it. This check will make sure that the code cannot be used outside the original token binding. Then the authorization server will issue an access token, which is bound to the referred token binding, and a refresh token, which is bound to the connection between the client and the authorization server.

 

7. 7.

The client application now invokes an API in the resource server passing the access token. This will carry the token binding between the client and the resource server.

 

8. 8.

The resource server will now talk to the introspection endpoint of the authorization server—and it will return back the binding ID attached to the access token, so the resource server can check whether it’s the same binding ID used between itself and the client application.

 

TLS Termination

Many production deployments do include a reverse proxy—which terminates the TLS connection. This can be at an Apache or Nginx server sitting between the client and the server. Once the connection is terminated at the reverse proxy, the server has no clue what happened at the TLS layer. To make sure the security tokens are bound to the incoming TLS connection, the server has to know the token binding ID. The HTTPS Token Binding with TLS Terminating Reverse Proxies, the draft specification (https://tools.ietf.org/html/draft-ietf-tokbind-ttrp-09), standardizes how the binding IDs are passed from the reverse proxy to the back-end server, as HTTP headers. The Provided-Token-Binding-ID and Referred-Token-Binding-ID HTTP headers are introduced by this specification (see Figure 11-6).

 

Figure 11-6

The reverse proxy passes the Provided-Token-Binding-ID and Referred-Token-Binding-ID HTTP headers to the backend server

## Summary

- OAuth 2.0 token binding proposal cryptographically binds security tokens to the TLS layer, preventing token export and replay attacks.

- Token binding relies on TLS—and since it binds the tokens to the TLS connection itself, anyone who steals a token cannot use it over a different channel.

- We can break down the token binding protocol into three main phases: negotiation phase, key generation phase, and proof of possession phase.

- During the negotiation phase, the client and the server negotiate a set of parameters to use for token binding between them.

- During the key generation phase, the client generates a key pair according to the parameters negotiated in the negotiation phase.

- During the proof of possession phase, the client uses the keys generated in the key generation phase to prove the possession.

 

