# 7. Message-Level Security with JSON Web Signature

JavaScript Object Notation (JSON) provides a way of exchanging data in a language-neutral, text-based, and lightweight manner. It was originally derived from the ECMAScript programming language. JSON and XML are the most commonly used data exchange formats for APIs. Observing the trend over the last few years, it’s quite obvious that JSON is replacing XML. Most of the APIs out there have support for JSON, and some support both JSON and XML. XML-only APIs are quite rare.

## Understanding JSON Web Token (JWT)

JSON Web Token (JWT) defines a container to transport data between interested parties in JSON. It became an IETF standard in May 2015 with the RFC 7519. The OpenID Connect specification, which we discussed in Chapter 6, uses a JWT to represent the ID token. Let’s examine an OpenID Connect ID token returned from the Google API, as an example (to understand JWT, you do not need to know about OpenID Connect):

eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc4YjRjZjIzNjU2ZGMzOTUzNjRmMWI2YzAyOTA3

NjkxZjJjZGZmZTEifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMT

EwNTAyMjUxMTU4OTIwMTQ3NzMyIiwiYXpwIjoiODI1MjQ5ODM1NjU5LXRlOHF

nbDcwMWtnb25ub21ucDRzcXY3ZXJodTEyMTFzLmFwcHMuZ29vZ2xldXNlcmNvb

nRlbnQuY29tIiwiZW1haWwiOiJwcmFiYXRoQHdzbzIuY29tIiwiYXRfaGFzaCI6InpmO

DZ2TnVsc0xCOGdGYXFSd2R6WWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkI

joiODI1MjQ5ODM1NjU5LXRlOHFnbDcwMWtnb25ub21ucDRzcXY3ZXJodTEyMTFz

LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiaGQiOiJ3c28yLmNvbSIsImlhdCI6

MTQwMTkwODI3MSwiZXhwIjoxNDAxOTEyMTcxfQ.TVKv-pdyvk2gW8sGsCbsnkq

srS0T-H00xnY6ETkIfgIxfotvFn5IwKm3xyBMpy0FFe0Rb5Ht8AEJV6PdWyxz8rMgX

2HROWqSo_RfEfUpBb4iOsq4W28KftW5H0IA44VmNZ6zU4YTqPSt4TPhyFC9fP2D

_Hg7JQozpQRUfbWTJI

> **Note**
>


Way before JWT, in 2009, Microsoft introduced Simple Web Token (SWT).1 It is neither JSON nor XML. It defined its own token format to carry out a set of HTML form–encoded name/value pairs. Both JWTs and SWTs define a way to carry claims between applications. In SWT, both the claim names and claim values are strings, while in JWT claim names are strings, but claim values can be any JSON type. Both of these token types offer cryptographic protection for their content: SWTs with HMAC SHA256 and JWTs with a choice of algorithms, including signature, MAC, and encryption algorithms. Even though SWT was developed as a proposal for IETF, it never became an IETF proposed standard. Dick Hardt was the editor of the SWT specification, who also played a major role later in building the OAuth WRAP specification, which we discuss in Appendix B.

JOSE Header

The preceding JWT has three main elements. Each element is base64url-encoded and separated by a period (.). Appendix E explains how base64url encoding works in detail. Let’s identify each individual element in the JWT. The first element of the JWT is called the JavaScript Object Signing and Encryption (JOSE) header. The JOSE header lists out the properties related to the cryptographic operations applied on the JWT claims set (which we explain later in this chapter). The following is the base64url-encoded JOSE header of the preceding JWT:

eyJhbGciOiJSUzI1NiIsImtpZCI6Ijc4YjRjZjIzNjU2ZGMzOTUzNjRmMWI2YzAyOTA3

NjkxZjJjZGZmZTEifQ

To make the JOSE header readable, we need to base64url-decode it. The following shows the base64url-decoded JOSE header, which defines two attributes, the algorithm (alg) and key identifier (kid).

{"alg":"RS256","kid":"78b4cf23656dc395364f1b6c02907691f2cdffe1"}

Both the alg and kid parameters are not defined in the JWT specification, but in the JSON Web Signature (JWS) specification. Let’s briefly identify here what these parameters mean and will discuss in detail when we explain JWS. The JWT specification is not bound to any specific algorithm. All applicable algorithms are defined under the JSON Web Algorithms (JWA) specification, which is the RFC 7518. Section 3.1 of RFC 7518 defines all possible alg parameter values for a JWS token. The value of the kid parameter provides an indication or a hint about the key, which is used to sign the message. Looking at the kid, the recipient of the message should know where to look up for the key and find it. The JWT specification only defines two parameters in the JOSE header; the following lists out those:

- typ (type): The typ parameter is used to define the media type of the complete JWT. A media type is an identifier, which defines the format of the content, transmitted over the Internet. There are two types of components that process a JWT: the JWT implementations and JWT applications. Nimbus2 is a JWT implementation in Java. The Nimbus library knows how to build and parse a JWT. A JWT application can be anything, which uses JWTs internally. A JWT application uses a JWT implementation to build or parse a JWT. The typ parameter is just another parameter for the JWT implementation. It will not try to interpret the value of it, but the JWT application would. The typ parameter helps JWT applications to differentiate the content of the JWT when the values that are not JWTs could also be present in an application data structure along with a JWT object. This is an optional parameter, and if present for a JWT, it is recommended to use JWT as the media type.

- cty (content type): The cty parameter is used to define the structural information about the JWT. It is only recommended to use this parameter in the case of a nested JWT. The nested JWTs are discussed in Chapter 8, and the definition of the cty parameter is further explained there.

JWT Claims Set

The second element of the JWT is known as either the JWT payload or the JWT claims set. It is a JSON object, which carries the business data. The following is the base64url-encoded JWT claims set of the preceding JWT (which is returned from the Google API); it includes information about the authenticated user:

eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTEwNTAyMjUxMTU4OT

IwMTQ3NzMyIiwiYXpwIjoiODI1MjQ5ODM1NjU5LXRlOHFnbDcwMWtnb25ub21uc

DRzcXY3ZXJodTEyMTFzLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1ha

WwiOiJwcmFiYXRoQHdzbzIuY29tIiwiYXRfaGFzaCI6InpmODZ2TnVsc0xCOGdGYX

FSd2R6WWciLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXVkIjoiODI1MjQ5ODM1NjU

5LXRlOHFnbDcwMWtnb25ub21ucDRzcXY3ZXJodTEyMTFzLmFwcHMuZ29vZ2xld

XNlcmNvbnRlbnQuY29tIiwiaGQiOiJ3c28yLmNvbSIsImlhdCI6MTQwMTkwODI3MS

wiZXhwIjoxNDAxOTEyMTcxfQ

To make the JWT claims set readable, we need to base64url-decode it. The following shows the base64url-decoded JWT claims set. Whitespaces can be explicitly retained while building the JWT claims set—no canonicalization is required before base64url-encoding. Canonicalization is the process of converting different forms of a message into a single standard form. This is used mostly before signing XML messages. In XML, the same message can be represented in different forms to carry the same meaning. For example, <vehicles><car></car></vehicles> and <vehicles><car/></vehicles> are equivalent in meaning, but have two different canonical forms. Before signing an XML message, you should follow a canonicalization algorithm to build a standard form.

{

     "iss":"accounts.google.com",

     "sub":"110502251158920147732",

     "azp":"825249835659-te8qgl701kgonnomnp4sqv7erhu1211s.apps.googleusercontent.com",

     "email":"prabath@wso2.com",

     "at_hash":"zf86vNulsLB8gFaqRwdzYg",

     "email_verified":true,

     "aud":"825249835659-te8qgl701kgonnomnp4sqv7erhu1211s.apps.googleusercontent.com",

     "hd":"wso2.com",

     "iat":1401908271,

     "exp":1401912171

}

The JWT claims set represents a JSON object whose members are the claims asserted by the JWT issuer. Each claim name within a JWT must be unique. If there are duplicate claim names, then the JWT parser could either return a parsing error or just return back the claims set with the very last duplicate claim. JWT specification does not explicitly define what claims are mandatory and what are optional. It’s up to each application of JWT to define mandatory and optional claims. For example, the OpenID Connect specification, which we discussed in detail in Chapter 6, defines the mandatory and optional claims.

The JWT specification defines three classes of claims: registered claims, public claims, and private claims. The registered claims are registered in the Internet Assigned Numbers Authority (IANA) JSON Web Token Claims registry. Even though these claims are treated as registered claims, the JWT specification doesn’t mandate their usage. It’s totally up to the other specifications which are built on top of JWT to decide which are mandatory and which aren’t. For example, in OpenID Connect specification, iss is a mandatory claim. The following lists out the registered claims set as defined by the JWT specification:

- iss (issuer): The issuer of the JWT. This is treated as a case-sensitive string value. Ideally, this represents the asserting party of the claims set. If Google issues the JWT, then the value of iss would be accounts.google.com. This is an indication to the receiving party who the issuer of the JWT is.

- sub (subject): The token issuer or the asserting party issues the JWT for a particular entity, and the claims set embedded into the JWT normally represents this entity, which is identified by the sub parameter. The value of the sub parameter is a case-sensitive string value.

- aud (audience): The token issuer issues the JWT to an intended recipient or a list of recipients, which is represented by the aud parameter. The recipient or the recipient list should know how to parse the JWT and validate it. Prior to any validation check, it must first see whether the particular JWT is issued for its use and if not should reject immediately. The value of the aud parameter can be a case-sensitive string value or an array of strings. The token issuer should know, prior to issuing the token, who the intended recipient (or the recipients) of the token is, and the value of the aud parameter must be a pre-agreed value between the token issuer and the recipient. In practice, one can also use a regular expression to validate the audience of the token. For example, the value of the aud in the token can be *.apress.com, while each recipient under the apress.com domain can have its own aud values: foo.apress.com, bar.apress.com likewise. Instead of finding an exact match for the aud value, each recipient can just check whether the aud value matches the regular expression: (?:[a-zA-Z0-9]*|\*).apress.com. This will make sure that any recipient can use a JWT, which is having any subdomain of apress.com.

- exp (expiration time): Each JWT carries an expiration time. The recipient of the JWT token must reject it, if that token has expired. The issuer can decide the value of the expiration time. The JWT specification does not recommend or provide any guidelines on how to decide the best token expiration time. It’s a responsibility of the other specifications, which use JWT internally to provide such recommendations. The value of the exp parameter is calculated by adding the expiration time (from the token issued time) in seconds to the time elapsed from 1970-01-01T00:00:00Z UTC to the current time. If the token issuer’s clock is out of sync with the recipient’s clock (irrespective of their time zone), then the expiration time validation could fail. To fix that, each recipient can add a couple of minutes as the clock skew during the validation process.

- nbf (not before): The recipient of the token should reject it, if the value of the nbf parameter is greater than the current time. The JWT is not good enough to use prior to the value indicated in the nbf parameter. The value of the nbf parameter is the number of seconds elapsed from 1970-01-01T00:00:00Z UTC to the not before time.

- iat (issued at): The iat parameter in the JWT indicates the issued time of the JWT as calculated by the token issuer. The value of the iat parameter is the number of seconds elapsed from 1970-01-01T00:00:00Z UTC to the current time, when the token is issued.

- jti (JWT ID): The jti parameter in the JWT is a unique token identifier generated by the token issuer. If the token recipient accepts JWTs from multiple token issuers, then this value may not be unique across all the issuers. In that case, the token recipient can maintain the token uniqueness by maintaining the tokens under the token issuer. The combination of the token issuer identifier + the jti should produce a unique token identifier.

The public claims are defined by the other specifications, which are built on top of JWT. To avoid any collisions in such cases, names should either be registered in the IANA JSON Web Token Claims registry or defined in a collision-resistant manner with a proper namespace. For example, the OpenID Connect specification defines its own set of claims, which are included inside the ID token (the ID token itself is a JWT), and those claims are registered in the IANA JSON Web Token Claims registry.

The private claims should indeed be private and shared only between a given token issuer and a selected set of recipients. These claims should be used with caution, because there is a chance for collision. If a given recipient accepts tokens from multiple token issuers, then the semantics of the same claim may be different from one issuer to another, if it is a private claim.

JWT Signature

The third part of the JWT is the signature, which is also base64url-encoded. The cryptographic parameters related to the signature are defined in the JOSE header. In this particular example, Google uses RSASSA-PKCS1-V1_53 with the SHA256 hashing algorithm, which is expressed by value of the alg parameter in the JOSE header: RS256. The following shows the signature element of the JWT returned back from Google. The signature itself is not human readable—so there is no point of trying to base64url-decode the following:

TVKv-pdyvk2gW8sGsCbsnkqsrS0TH00xnY6ETkIfgIxfotvFn5IwKm3xyBMpy0

FFe0Rb5Ht8AEJV6PdWyxz8rMgX2HROWqSo_RfEfUpBb4iOsq4W28KftW5

H0IA44VmNZ6zU4YTqPSt4TPhyFC-9fP2D_Hg7JQozpQRUfbWTJI

Generating a Plaintext JWT

The plaintext JWT doesn’t have a signature. It has only two parts. The value of the alg parameter in the JOSE header must be set to none. The following Java code generates a plaintext JWT. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample01.

public static String buildPlainJWT() {

// build audience restriction list.

List<String> aud = new ArrayList<String>();

aud.add("https://app1.foo.com");

aud.add("https://app2.foo.com");

Date currentTime = new Date();

// create a claims set.

JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().

                                // set the value of the issuer.

                                issuer("https://apress.com").

                                // set the subject value - JWT belongs to // this subject.

                                subject("john").

                                // set values for audience restriction.

                                audience(aud).

                                // expiration time set to 10 minutes.

                                expirationTime(new Date(new Date().getTime() + 1000 - 60 - 10)).

                                // set the valid from time to current time.

                                notBeforeTime(currentTime).

                                // set issued time to current time.

                                issueTime(currentTime).

                                // set a generated UUID as the JWT // identifier.

                                jwtID(UUID.randomUUID().toString()).

                                build();

// create plaintext JWT with the JWT claims.

PlainJWT plainJwt = new PlainJWT(jwtClaims);

// serialize into string.

String jwtInText = plainJwt.serialize();

// print the value of the JWT.

System.out.println(jwtInText);

return jwtInText;

}

To build and run the program, execute the following Maven command from the ch07/sample01 directory.

\> mvn test -Psample01

The preceding code produces the following output, which is a JWT. If you run the code again and again, you may not get the same output as the value of the currentTime variable changes every time you run the program:

eyJhbGciOiJub25lIn0.eyJleHAiOjE0MDIwMzcxNDEsInN1YiI6ImpvaG4iLCJuYm

YiOjE0MDIwMzY1NDEsImF1ZCI6WyJodHRwczpcL1wvYXBwMS5mb28uY29tIi

wiaHR0cHM6XC9cL2FwcDIuZm9vLmNvbSJdLCJpc3MiOiJodHRwczpcL1wvYX

ByZXNzLmNvbSIsImp0aSI6IjVmMmQzM2RmLTEyNDktNGIwMS04MmYxLWJl

MjliM2NhOTY4OSIsImlhdCI6MTQwMjAzNjU0MX0.

The following Java code shows how to parse a base64url-encoded JWT. This code would ideally run at the JWT recipient end:

public static PlainJWT parsePlainJWT() throws ParseException {

        // get JWT in base64url-encoded text.

        String jwtInText = buildPlainJWT();

        // build a plain JWT from the bade64url-encoded text.

        PlainJWT plainJwt  = PlainJWT.parse(jwtInText);

        // print the JOSE header in JSON.

        System.out.println(plainJwt.getHeader().toString());

        // print JWT body in JSON.

        System.out.println(plainJwt.getPayload().toString());

        return plainJwt;

}

This code produces the following output, which includes the parsed JOSE header and the payload:

{"alg":"none"}

{

   "exp":1402038339,

   "sub":"john",

   "nbf":1402037739,

   "aud":["https:\/\/app1.foo.com","https:\/\/app2.foo.com"],

   "iss":"https:\/\/apress.com",

   "jti":"1e41881f-7472-4030-8132-856ccf4cbb25",

   "iat":1402037739

}

Jose Working Group

Many working groups within the IETF work directly with JSON, including the OAuth working group and the System for Cross-domain Identity Management (SCIM) working group. The SCIM working group is building a provisioning standard based on JSON. Outside the IETF, the OASIS XACML working group is working on building a JSON profile for XACML 3.0.

The OpenID Connect specification, which is developed under the OpenID Foundation, is also heavily based on JSON. Due to the rise of standards built around JSON and the heavy usage of JSON for data exchange in APIs, it has become absolutely necessary to define how to secure JSON messages at the message level. The use of Transport Layer Security (TLS) only provides confidentiality and integrity at the transport layer. The JOSE working group, formed under the IETF, has the goal of standardizing integrity protection and confidentiality as well as the format for keys and algorithm identifiers to support interoperability of security services for protocols that use JSON. JSON Web Signature (RFC 7515), JSON Web Encryption (RFC 7516), JSON Web Key (RFC 7517), and JSON Web Algorithms (RFC 7518) are four IETF proposed standards, which were developed under the JOSE working group.

JSON Web Signature (JWS)

The JSON Web Signature (JWS) specification, developed under the IETF JOSE working group, represents a message or a payload, which is digitally signed or MACed (when a hashing algorithm is used with HMAC). A signed message can be serialized in two ways by following the JWS specification: the JWS compact serialization and the JWS JSON serialization. The Google OpenID Connect example discussed at the beginning of this chapter uses JWS compact serialization. In fact, the OpenID Connect specification mandates to use JWS compact serialization and JWE compact serialization whenever necessary (we discuss JWE in Chapter 8). The term JWS token is used to refer to the serialized form of a payload, following any of the serialization techniques defined in the JWS specification.

> **Note**
>


JSON Web Tokens (JWTs) are always serialized with the JWS compact serialization or the JWE compact serialization. We discuss JWE (JSON Web Encryption) in Chapter 8.

JWS Compact Serialization

JWS compact serialization represents a signed JSON payload as a compact URL-safe string. This compact string has three main elements separated by periods (.): the JOSE header, the JWS payload, and the JWS signature (see Figure 7-1). If you use compact serialization against a JSON payload, then you can have only a single signature, which is computed over the complete JOSE header and JWS payload.

 

Figure 7-1

A JWS token with compact serialization

JOSE Header

The JWS specification introduces 11 parameters to the JOSE header. The following lists out the parameters carried in a JOSE header, which are related to the message signature. Out of all those parameters, the JWT specification only defines the typ and cty parameters (as we discussed before); the rest is defined by the JWS specification. The JOSE header in a JWS token carries all the parameters required by the JWS token recipient to properly validate its signature:

- alg (algorithm): The name of the algorithm, which is used to sign the JSON payload. This is a required attribute in the JOSE header. Failure to include this in the header will result in a token parsing error. The value of the alg parameter is a string, which is picked from the JSON Web Signature and Encryption Algorithms registry defined by the JSON Web Algorithms (JWA) specification. If the value of the alg parameter is not picked from the preceding registry, then it should be defined in a collision-resistant manner, but that won’t give any guarantee that the particular algorithm is identified by all JWS implementations. It’s always better to stick to the algorithms defined in the JWA specification.

- jku: The jku parameter in the JOSE header carries a URL, which points to a JSON Web Key (JWK) set. This JWK set represents a collection of JSON-encoded public keys, where one of the keys is used to sign the JSON payload. Whatever the protocol used to retrieve the key set should provide the integrity protection. If keys are retrieved over HTTP, then instead of plain HTTP, HTTPS (or HTTP over TLS) should be used. We discuss Transport Layer Security (TLS) in detail in Appendix C. The jku is an optional parameter.

- jwk: The jwk parameter in JOSE header represents the public key corresponding to the key that is used to sign the JSON payload. The key is encoded as per the JSON Web Key (JWK) specification. The jku parameter, which we discussed before, points to a link that holds a set of JWKs, while the jwk parameter embeds the key into the JOSE header itself. The jwk is an optional parameter.

- kid: The kid parameter of the JOSE header represents an identifier for the key that is used to sign the JSON payload. Using this identifier, the recipient of the JWS should be able locate the key. If the token issuer uses the kid parameter in the JOSE header to let the recipient know about the signing key, then the corresponding key should be exchanged “somehow” between the token issuer and the recipient beforehand. How this key exchange happens is out of the scope of the JWS specification. If the value of the kid parameter refers to a JWK, then the value of this parameter should match the value of the kid parameter in the JWK. The kid is an optional parameter in the JOSE header.

- x5u: The x5u parameter in the JOSE header is very much similar to the jku parameter, which we discussed before. Instead of pointing to a JWK set, the URL here points to an X.509 certificate or a chain of X.509 certificates. The resource pointed by the URL must hold the certificate or the chain of certificates in the PEM-encoded form. Each certificate in the chain must appear between the delimiters4: -----BEGIN CERTIFICATE----- and -----END CERTIFICATE-----. The public key corresponding to the key used to sign the JSON payload should be the very first entry in the certificate chain, and the rest is the certificates of intermediate CAs (certificate authority) and the root CA. The x5u is an optional parameter in the JOSE header.

- x5c: The x5c parameter in the JOSE header represents the X.509 certificate (or the certificate chain), which corresponds to the private key, which is used to sign the JSON payload. This is similar to the jwk parameter we discussed before, but in this case, instead of a JWK, it’s an X.509 certificate (or a chain of certificates). The certificate or the certificate chain is represented in a JSON array of certificate value strings. Each element in the array should be a base64-encoded DER PKIX certificate value. The public key corresponding to the key used to sign the JSON payload should be the very first entry in the JSON array, and the rest is the certificates of intermediate CAs (certificate authority) and the root CA. The x5c is an optional parameter in the JOSE header.

- x5t: The x5t parameter in the JOSE header represents the base64url-encoded SHA-1 thumbprint of the X.509 certificate corresponding to the key used to sign the JSON payload. This is similar to the kid parameter we discussed before. Both these parameters are used to locate the key. If the token issuer uses the x5t parameter in the JOSE header to let the recipient know about the signing key, then the corresponding key should be exchanged “somehow” between the token issuer and the recipient beforehand. How this key exchange happens is out of the scope of the JWS specification. The x5t is an optional parameter in the JOSE header.

- x5t#s256: The x5t#s256 parameter in the JOSE header represents the base64url-encoded SHA256 thumbprint of the X.509 certificate corresponding to the key used to sign the JSON payload. The only difference between x5t#s256 and the x5t is the hashing algorithm. The x5t#s256 is an optional parameter in the JOSE header.

- typ: The typ parameter in the JOSE header is used to define the media type of the complete JWS. There are two types of components that process a JWS: JWS implementations and JWS applications. Nimbus5 is a JWS implementation in Java. The Nimbus library knows how to build and parse a JWS. A JWS application can be anything, which uses JWS internally. A JWS application uses a JWS implementation to build or parse a JWS. In this case, the typ parameter is just another parameter for the JWS implementation. It will not try to interpret the value of it, but the JWS application would. The typ parameter will help JWS applications to differentiate the content when multiple types of objects are present. For a JWS token using JWS compact serialization and for a JWE token using JWE compact serialization, the value of the typ parameter is JOSE, and for a JWS token using JWS JSON serialization and for a JWE token using JWE JSON serialization, the value is JOSE+JSON. (JWS serialization is discussed later in this chapter, and JWE serialization is discussed in Chapter 8). The typ is an optional parameter in the JOSE header.

- cty: The cty parameter in the JOSE header is used to represent the media type of the secured content in the JWS. It is only recommended to use this parameter in the case of a nested JWT. The nested JWT is discussed later in Chapter 8, and the definition of the cty parameter is further explained there. The cty is an optional parameter in the JOSE header.

- crit: The crit parameter in the JOSE header is used to indicate the recipient of the JWS that the presence of custom parameters, which neither defined by the JWS or JWA specifications, in the JOSE header. If these custom parameters are not understood by the recipient, then the JWS token will be treated as invalid. The value of the crit parameter is a JSON array of names, where each entry represents a custom parameter. The crit is an optional parameter in the JOSE header.

Out of all the 11 parameters defined earlier, 7 talk about how to reference the public key corresponding to the key, which is used to sign the JSON payload. There are three ways of referencing a key: external reference, embedded, and key identifier. The jku and x5u parameters fall under the external reference category. Both of them reference the key through a URI. The jwk and x5c parameters fall under embedded reference category. Each one of them defines how to embed the key to the JOSE header itself. The kid, x5t, and x5t#s256 parameters fall under the key identifier reference category. All three of them define how to locate the key using an identifier. Then again all the seven parameters can further divide into two categories based on the representation of the key: JSON Web Key (JWK) and X.509. The jku, jwk, and kid fall under the JWK category, while x5u, x5c, x5t, and x5t#s256 fall under the X.509 category. In the JOSE header of a given JWS token, at a given time, we only need to have one from the preceding parameters.

> **Note**
>


If any of the jku, jwk, kid, x5u, x5c, x5t, and x5t#s256 are present in the JOSE header, those must be integrity protected. Failure to do so will let an attacker modify the key used to sign the message and change the content of the message payload. After validating the signature of a JWS token, the recipient application must check whether the key associated with the signature is trusted. Checking whether the recipient knows the corresponding key can do the trust validation.

The JWS specification does not restrict applications only to use 11 header parameters defined earlier. There are two ways to introduce new header parameters: public header names and private header names. Any header parameter that is intended to use in the public space should be introduced in a collision-resistant manner. It is recommended to register such public header parameters in the IANA JSON Web Signature and Encryption Header Parameters registry. The private header parameters are mostly used in a restricted environment, where both the token issuer and the recipients are well aware of each other. These parameters should be used with caution, because there is a chance for collision. If a given recipient accepts tokens from multiple token issuers, then the semantics of the same parameter may be different from one issuer to another, if it is a private header. In either case, whether it’s a public or a private header parameter, if it is not defined in the JWS or the JWA specification, the header name should be included in the crit header parameter, which we discussed before.

JWS Payload

The JWS payload is the message that needs to be signed. The message can be anything—need not be a JSON payload. If it is a JSON payload, then it could contain whitespaces and/or line breaks before or after any JSON value. The second element of the serialized JWS token carries the base64url-encoded value of the JWS payload.

JWS Signature

The JWS signature is the digital signature or the MAC, which is calculated over the JWS payload and the JOSE header. The third element of the serialized JWS token carries the base64url-encoded value of the JWS signature.

The Process of Signing (Compact Serialization)

We discussed about all the ingredients that are required to build a JWS token under compact serialization. The following discusses the steps involved in building a JWS token. There are three elements in a JWS token; the first element is produced by step 2, the second element is produced by step 4, and the third element is produced by step 7.

1. 1.

Build a JSON object including all the header parameters, which express the cryptographic properties of the JWS token—this is known as the JOSE header. As discussed before in this chapter, under the section “JOSE Header,” the token issuer should advertise in the JOSE header the public key corresponding to the key used to sign the message. This can be expressed via any of these header parameters: jku, jwk, kid, x5u, x5c, x5t, and x5t#s256.

 

2. 2.

Compute the base64url-encoded value against the UTF-8 encoded JOSE header from step 1 to produce the first element of the JWS token.

 

3. 3.

Construct the payload or the content to be signed—this is known as the JWS payload. The payload is not necessarily JSON—it can be any content.

 

4. 4.

Compute the base64url-encoded value of the JWS payload from step 3 to produce the second element of the JWS token.

 

5. 5.

Build the message to compute the digital signature or the MAC. The message is constructed as ASCII(BASE64URL-ENCODE(UTF8(JOSE Header)) . BASE64URL-ENCODE(JWS Payload)).

 

6. 6.

Compute the signature over the message constructed in step 5, following the signature algorithm defined by the JOSE header parameter alg. The message is signed using the private key corresponding to the public key advertised in the JOSE header.

 

7. 7.

Compute the base64url-encoded value of the JWS signature produced in step 6, which is the third element of the serialized JWS token.

 

8. 8.

Now we have all the elements to build the JWS token in the following manner. The line breaks are introduced only for clarity.

BASE64URL(UTF8(JWS Protected Header)).

BASE64URL(JWS Payload).

BASE64URL(JWS Signature)

 

JWS JSON Serialization

In contrast to the JWS compact serialization , the JWS JSON serialization can produce multiple signatures over the same JWS payload along with different JOSE header parameters. The ultimate serialized form under JWS JSON serialization wraps the signed payload in a JSON object, with all related metadata. This JSON object includes two top-level elements, payload and signatures, and three subelements under the signatures element: protected, header, and signature. The following is an example of a JWS token, which is serialized with JWS JSON serialization. This is neither URL safe nor optimized for compactness. It carries two signatures over the same payload, and each signature and the metadata around it are stored as an element in the JSON array, under the signatures top-level element. Each signature uses a different key to sign, represented by the corresponding kid header parameter. The JSON serialization is also useful in selectively signing JOSE header parameters. In contrast, JWS compact serialization signs the complete JOSE header:

{

"payload":"eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzOD",

"signatures":[

               {

                  "protected":"eyJhbGciOiJSUzI1NiJ9",

                  "header":{"kid":"2014-06-29"},

                  "signature":"cC4hiUPoj9Eetdgtv3hF80EGrhuB"

               },

               {

                  "protected":"eyJhbGciOiJFUzI1NiJ9",

                  "header":{"kid":"e909097a-ce81-4036-9562-d21d2992db0d"},

                  "signature":"DtEhU3ljbEg8L38VWAfUAqOyKAM"

               }

             ]

}

JWS Payload

The payload top-level element of the JSON object includes the base64url-encoded value of the complete JWS payload. The JWS payload necessarily need not be a JSON payload, it can be of any content type. The payload is a required element in the serialized JWS token.

JWS Protected Header

The JWS Protected Header is a JSON object that includes the header parameters that have to be integrity protected by the signing or MAC algorithm. The protected parameter in the serialized JSON form represents the base64url-encoded value of the JWS Protected Header. The protected is not a top-level element of the serialized JWS token. It is used to define elements in the signatures JSON array and includes the base64url-encoded header elements, which should be signed. If you base64url-decode the value of the first protected element in the preceding code snippet, you will see {"alg":"RS256"}. The protected parameter must be present, if there are any protected header parameters. There is one protected element for each entry of the signatures JSON array.

JWS Unprotected Header

The JWS Unprotected Header is a JSON object that includes the header parameters that are not integrity protected by the signing or MAC algorithm. The header parameter in the serialized JSON form represents the base64url-encoded value of the JWS Unprotected Header. The header is not a top-level parameter of the JSON object. It is used to define elements in the signatures JSON array. The header parameter includes unprotected header elements related to the corresponding signature, and these elements are not signed. Combining both the protected headers and unprotected headers ultimately derives the JOSE header corresponding to the signature. In the preceding code snippet, the complete JOSE header corresponding to the first entry in the signatures JSON array would be {"alg":"RS256", "kid":"2010-12-29"}. The header element is represented as a JSON object and must be present if there are any unprotected header parameters. There is one header element for each entry of the signatures JSON array.

JWS Signature

The signatures parameter of the JSON object includes an array of JSON objects, where each element includes a signature or MAC (over the JWS payload and JWS protected header) and the associated metadata. This is a required parameter. The signature subelement, which is inside each entry of the signatures array, carries the base64url-encoded value of the signature computed over the protected header elements (represented by the protected parameter) and the JWS payload. Both the signatures and signature are required parameters.

> **Note**
>


Even though JSON serialization provides a way to selectively sign JOSE header parameters, it does not provide a direct way to selectively sign the parameters in the JWS payload. Both forms of serialization mentioned in the JWS specification sign the complete JWS payload. There is a workaround for this using JSON serialization. You can replicate the payload parameters that need to be signed selectively in the JOSE header. Then with JSON serialization, header parameters can be selectively signed.

The Process of Signing (JSON Serialization)

We discussed about all the ingredients that are required to build a JWS token under JSON serialization. The following discusses the steps involved in building the JWS token.

1. 1.

Construct the payload or the content to be signed—this is known as the JWS payload. The payload is not necessarily JSON—it can be any content. The payload element in the serialized JWS token carries the base64url-encoded value of the content.

 

2. 2.

Decide how many signatures you would need against the payload and for each case which header parameters must be signed and which are not.

 

3. 3.

Build a JSON object including all the header parameters that are to be integrity protected or to be signed. In other words, construct the JWS Protected Header for each signature. The base64url-encoded value of the UTF-8 encoded JWS Protected Header will produce the value of the protected subelement inside the signatures top-level element of the serialized JWS token.

 

4. 4.

Build a JSON object including all the header parameters that need not be integrity protected or not be signed. In other words, construct the JWS Unprotected Header for each signature. This will produce the header subelement inside the signatures top-level element of the serialized JWS token.

 

5. 5.

Both the JWS Protected Header and the JWS Unprotected Header express the cryptographic properties of the corresponding signature (there can be more than one signature element)—this is known as the JOSE header. As discussed before in this chapter, under the section “JOSE Header,” the token issuer should advertise in the JOSE header the public key corresponding to the key used to sign the message. This can be expressed via any of these header parameters: jku, jwk, kid, x5u, x5c, x5t, and x5t#s256.

 

6. 6.

Build the message to compute the digital signature or the MAC against each entry in the signatures JSON array of the serialized JWS token. The message is constructed as ASCII(BASE64URL-ENCODE(UTF8(JWS Protected Header)). BASE64URL-ENCODE(JWS Payload)).

 

7. 7.

Compute the signature over the message constructed in step 6, following the signature algorithm defined by the header parameter alg. This parameter can be either inside the JWS Protected Header or the JWS Unprotected Header. The message is signed using the private key corresponding to the public key advertised in the header.

 

8. 8.

Compute the base64url-encoded value of the JWS signature produced in step 7, which will produce the value of the signature subelement inside the signatures top-level element of the serialized JWS token.

 

9. 9.

Once all the signatures are computed, the signatures top-level element can be constructed and will complete the JWS JSON serialization.

 

Signature Types

The XML Signature specification, which was developed under W3C, proposes three types of signatures: enveloping, enveloped, and detached. These three kinds of signatures are only discussed under the context of XML.

With the enveloping signature, the XML content to be signed is inside the signature itself. That is, inside the <ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#"> element.

With the enveloped signature, the signature is inside the XML content to be signed. In other words, the <ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#"> element is inside the parent element of the XML payload to be signed.

With the detached signature, there is no parent-child relationship between the XML content to be signed and the corresponding signature. They are detached from each other.

For anyone who is familiar with XML Signature, all the signatures defined in the JWS specification can be treated as detached signatures.

> **Note**
>


The XML Signature specification by W3C only talks about signing an XML payload. If you have to sign any content, then first you need to embed that within an XML payload and then sign. In contrast, the JWS specification is not just limited to JSON. You can sign any content with JWS without wrapping it inside a JSON payload.

Generating a JWS Token with HMAC-SHA256 with a JSON Payload

The following Java code generates a JWS token with HMAC-SHA256. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample02.

The method buildHmacSha256SignedJWT() in the code should be invoked by passing a secret value that is used as the shared key to sign. The length of the secret value must be at least 256 bits:

public static String buildHmacSha256SignedJSON(String sharedSecretString) throws JOSEException {

// build audience restriction list.

List<String> aud = new ArrayList<String>();

aud.add("https://app1.foo.com");

aud.add("https://app2.foo.com");

Date currentTime = new Date();

// create a claims set.

JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().

                                // set the value of the issuer.

                                issuer("https://apress.com").

                                // set the subject value - JWT belongs to // this subject.

                                subject("john").

                                // set values for audience restriction.

                                audience(aud).

                                // expiration time set to 10 minutes.

                                expirationTime(new Date(new Date().getTime() + 1000 - 60 - 10)).

                                // set the valid from time to current time.

                                notBeforeTime(currentTime).

                                // set issued time to current time.

                                issueTime(currentTime).

                                // set a generated UUID as the JWT // identifier.

                                jwtID(UUID.randomUUID().toString()).

                                build();

// create JWS header with HMAC-SHA256 algorithm.

JWSHeader jswHeader = new JWSHeader(JWSAlgorithm.HS256);

// create signer with the provider shared secret.

JWSSigner signer = new MACSigner(sharedSecretString);

// create the signed JWT with the JWS header and the JWT body.

SignedJWT signedJWT = new SignedJWT(jswHeader, jwtClaims);

// sign the JWT with HMAC-SHA256.

signedJWT.sign(signer);

// serialize into base64url-encoded text.

String jwtInText = signedJWT.serialize();

// print the value of the JWT.

System.out.println(jwtInText);

return jwtInText;

}

To build and run the program, execute the following Maven command from the ch07/sample02 directory.

\> mvn test -Psample02

The preceding code produces the following output, which is a signed JSON payload (a JWS). If you run the code again and again, you may not get the same output as the value of the currentTime variable changes every time you run the program:

eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0MDIwMzkyOTIsInN1YiI6ImpvaG4iLCJuYm

YiOjE0MDIwMzg2OTIsImF1ZCI6WyJodHRwczpcL1wvYXBwMS5mb28uY29tIiw

iaHR0cHM6XC9cL2FwcDIuZm9vLmNvbSJdLCJpc3MiOiJodHRwczpcL1wvYXBy

ZXNzLmNvbSIsImp0aSI6ImVkNjkwN2YwLWRlOGEtNDMyNi1hZDU2LWE5ZmE

5NjA2YTVhOCIsImlhdCI6MTQwMjAzODY5Mn0.3v_pa-QFCRwoKU0RaP7pLOox

T57okVuZMe_A0UcqQ8

The following Java code shows how to validate the signature of a signed JSON message with HMAC-SHA256. To do that, you need to know the shared secret used to sign the JSON payload:

public static boolean isValidHmacSha256Signature()

                                       throws JOSEException, ParseException {

        String sharedSecretString = "ea9566bd-590d-4fe2-a441-d5f240050dbc";

        // get signed JWT in base64url-encoded text.

        String jwtInText = buildHmacSha256SignedJWT(sharedSecretString);

        // create verifier with the provider shared secret.

        JWSVerifier verifier = new MACVerifier(sharedSecretString);

        // create the signed JWS token with the base64url-encoded text.

        SignedJWT signedJWT = SignedJWT.parse(jwtInText);

        // verify the signature of the JWS token.

        boolean isValid = signedJWT.verify(verifier);

        if (isValid) {

            System.out.println("valid JWT signature");

        } else {

            System.out.println("invalid JWT signature");

        }

        return isValid;

}

Generating a JWS Token with RSA-SHA256 with a JSON Payload

The following Java code generates a JWS token with RSA-SHA256. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample03. First you need to invoke the method generateKeyPair() and pass the PrivateKey(generateKeyPair().getPrivateKey()) into the method buildRsaSha256SignedJSON():

public static KeyPair generateKeyPair()

                                  throws NoSuchAlgorithmException {

        // instantiate KeyPairGenerate with RSA algorithm.

        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

        // set the key size to 1024 bits.

        keyGenerator.initialize(1024);

        // generate and return private/public key pair.

        return keyGenerator.genKeyPair();

 }

 public static String buildRsaSha256SignedJSON(PrivateKey privateKey)

                                                throws JOSEException {

    // build audience restriction list.

    List<String> aud = new ArrayList<String>();

    aud.add("https://app1.foo.com");

    aud.add("https://app2.foo.com");

    Date currentTime = new Date();

    // create a claims set.

    JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().

                                // set the value of the issuer.

                                issuer("https://apress.com").

                                // set the subject value - JWT belongs to // this subject.

                                subject("john").

                                // set values for audience restriction.

                                audience(aud).

                                // expiration time set to 10 minutes.

                                expirationTime(new Date(new Date().getTime() + 1000 - 60 - 10)).

                                // set the valid from time to current time.

                                notBeforeTime(currentTime).

                                // set issued time to current time.

                                issueTime(currentTime).

                                // set a generated UUID as the JWT identifier.

                                jwtID(UUID.randomUUID().toString()).

                                build();

        // create JWS header with RSA-SHA256 algorithm.

        JWSHeader jswHeader = new JWSHeader(JWSAlgorithm.RS256);

        // create signer with the RSA private key..

        JWSSigner signer = new RSASSASigner((RSAPrivateKey)privateKey);

        // create the signed JWT with the JWS header and the JWT body.

        SignedJWT signedJWT = new SignedJWT(jswHeader, jwtClaims);

        // sign the JWT with HMAC-SHA256.

        signedJWT.sign(signer);

        // serialize into base64-encoded text.

        String jwtInText = signedJWT.serialize();

        // print the value of the JWT.

        System.out.println(jwtInText);

        return jwtInText;

}

The following Java code shows how to invoke the previous two methods:

KeyPair keyPair = generateKeyPair();

buildRsaSha256SignedJSON(keyPair.getPrivate());

To build and run the program, execute the following Maven command from the ch07/sample03 directory .

\> mvn test -Psample03

Let’s examine how to validate a JWS token signed by RSA-SHA256. You need to know the PublicKey corresponding to the PrivateKey used to sign the message:

public static boolean isValidRsaSha256Signature()

                                           throws NoSuchAlgorithmException,

                                                       JOSEException, ParseException {

        // generate private/public key pair.

        KeyPair keyPair = generateKeyPair();

        // get the private key - used to sign the message.

        PrivateKey privateKey = keyPair.getPrivate();

        // get public key - used to verify the message signature.

        PublicKey publicKey = keyPair.getPublic();

        // get signed JWT in base64url-encoded text.

        String jwtInText = buildRsaSha256SignedJWT(privateKey);

        // create verifier with the provider shared secret.

        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);

        // create the signed JWT with the base64url-encoded text.

        SignedJWT signedJWT = SignedJWT.parse(jwtInText);

        // verify the signature of the JWT.

        boolean isValid = signedJWT.verify(verifier);

        if (isValid) {

            System.out.println("valid JWT signature");

        } else {

            System.out.println("invalid JWT signature");

        }

        return isValid;

}

Generating a JWS Token with HMAC-SHA256 with a Non-JSON Payload

The following Java code generates a JWS token with HMAC-SHA256. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch07/sample04. The method buildHmacSha256SignedNonJSON() in the code should be invoked by passing a secret value that is used as the shared key to sign. The length of the secret value must be at least 256 bits:

public static String buildHmacSha256SignedJWT(String sharedSecretString)

                                                    throws JOSEException {

// create an HMAC-protected JWS object with a non-JSON payload

JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256),

                                    new Payload("Hello world!"));

// create JWS header with HMAC-SHA256 algorithm.

jwsObject.sign(new MACSigner(sharedSecretString));

// serialize into base64-encoded text.

String jwtInText = jwsObject.serialize();

// print the value of the serialzied JWS token.

System.out.println(jwtInText);

return jwtInText;

}

To build and run the program, execute the following Maven command from the ch07/sample04 directory.

\> mvn test -Psample04

The preceding code uses the JWS compact serialization and will produce the following output:

eyJhbGciOiJIUzI1NiJ9.SGVsbG8gd29ybGQh.zub7JG0FOh7EIKAgWMzx95w-nFpJdRMvUh_pMwd6wnA

## Summary

- JSON has already become the de facto message exchange format for APIs.

- Understanding JSON security plays a key role in securing APIs.

- JSON Web Token (JWT) defines a container to transport data between interested parties in a cryptographically safe manner. It became an IETF standard in May 2015 with the RFC 7519.

- Both JWS (JSON Web Signature) and JWE (JSON Web Encryption) standards are built on top of JWT.

- There are two types of serialization techniques defined by the JWS specification: compact serialization and JSON serialization.

- The JWS specification is not just limited to JSON. You can sign any content with JWS without wrapping it inside a JSON payload.

 



 



