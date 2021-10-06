8. Message-Level Security with JSON Web Encryption

In Chapter 7, we discussed in detail the JWT (JSON Web Token) and JWS (JSON Web Signature) specifications. Both of these specifications are developed under the IETF JOSE working group. This chapter focuses on another prominent standard developed by the same IETF working group for encrypting messages (not necessarily JSON payloads): JSON Web Encryption (JWE). Like in JWS, JWT is the foundation for JWE. The JWE specification standardizes the way to represent an encrypted content in a JSON-based data structure. The JWE1 specification defines two serialized forms to represent the encrypted payload: the JWE compact serialization and JWE JSON serialization. Both of these two serialization techniques are discussed in detail in the sections to follow. Like in JWS, the message to be encrypted using JWE standard need not be a JSON payload, it can be any content. The term JWE token is used to refer to the serialized form of an encrypted message (any message, not just JSON), following any of the serialization techniques defined in the JWE specification.

JWE Compact Serialization

With the JWE compact serialization, a JWE token is built with five key components, each separated by periods (.): JOSE header, JWE Encrypted Key, JWE Initialization Vector, JWE Ciphertext, and JWE Authentication Tag. Figure 8-1 shows the structure of a JWE token formed by JWE compact serialization.

 

Figure 8-1

A JWE token with compact serialization

JOSE Header

The JOSE header is the very first element of the JWE token produced under compact serialization. The structure of the JOSE header is the same, as we discussed in Chapter 7, other than few exceptions. The JWE specification introduces two new parameters (enc and zip), which are included in the JOSE header of a JWE token, in addition to those introduced by the JSON Web Signature (JWS) specification. The following lists out all the JOSE header parameters, which are defined by the JWE specification:

- alg (algorithm): The name of the algorithm, which is used to encrypt the Content Encryption Key (CEK). The CEK is a symmetric key, which encrypts the plaintext JSON payload. Once the plaintext is encrypted with the CEK, the CEK itself will be encrypted with another key following the algorithm identified by the value of the alg parameter. The encrypted CEK will then be included in the JWE Encrypted Key section of the JWE token. This is a required attribute in the JOSE header. Failure to include this in the header will result in a token parsing error. The value of the alg parameter is a string, which is picked from the JSON Web Signature and Encryption Algorithms registry defined by the JSON Web Algorithms2 (JWA) specification. If the value of the alg parameter is not picked from the preceding registry, then it should be defined in a collision-resistant manner, but that won’t give any guarantee that the particular algorithm is identified by all JWE implementations. It’s always better to stick to the algorithms defined in the JWA specification.

- enc: The enc parameter in the JOSE header represents the name of the algorithm, which is used for content encryption. This algorithm should be a symmetric Authenticated Encryption with Associated Data (AEAD) algorithm. This is a required attribute in the JOSE header. Failure to include this in the header will result in a token parsing error. The value of the enc parameter is a string, which is picked from the JSON Web Signature and Encryption Algorithms registry defined by the JSON Web Algorithms (JWA) specification. If the value of the enc parameter is not picked from the preceding registry, then it should be defined in a collision-resistant manner, but that won’t give any guarantee that the particular algorithm is identified by all JWE implementations. It’s always better to stick to the algorithms defined in the JWA specification.

- zip: The zip parameter in the JOSE header defines the name of the compression algorithm. The plaintext JSON payload gets compressed before the encryption, if the token issuer decides to use compression. The compression is not a must. The JWE specification defines DEF as the compression algorithm, but it’s not a must to use it. The token issuers can define their own compression algorithms. The default value of the compression algorithm is defined in the JSON Web Encryption Compression Algorithms registry under the JSON Web Algorithms (JWA) specification. This is an optional parameter.

- jku: The jku parameter in the JOSE header carries a URL, which points to a JSON Web Key (JWK)3 set. This JWK set represents a collection of JSON-encoded public keys, where one of the keys is used to encrypt the Content Encryption Key (CEK). Whatever the protocol used to retrieve the key set should provide the integrity protection. If keys are retrieved over HTTP, then instead of plain HTTP, HTTPS (or HTTP over TLS) should be used. We discuss Transport Layer Security (TLS) in detail in Appendix C. The jku is an optional parameter.

- jwk: The jwk parameter in JOSE header represents the public key corresponding to the key that is used to encrypt the Content Encryption Key (CEK). The key is encoded as per the JSON Web Key (JWK) specification.3 The jku parameter, which we discussed before, points to a link that holds a set of JWKs, while the jwk parameter embeds the key into the JOSE header itself. The jwk is an optional parameter.

- kid: The kid parameter of the JOSE header represents an identifier for the key that is used to encrypt the Content Encryption Key (CEK). Using this identifier, the recipient of the JWE should be able to locate the key. If the token issuer uses the kid parameter in the JOSE header to let the recipient know about the signing key, then the corresponding key should be exchanged “somehow” between the token issuer and the recipient beforehand. How this key exchange happens is out of the scope of the JWE specification. If the value of the kid parameter refers to a JWK, then the value of this parameter should match the value of the kid parameter in the JWK. The kid is an optional parameter in the JOSE header.

- x5u: The x5u parameter in the JOSE header is very much similar to the jku parameter, which we discussed before. Instead of pointing to a JWK set, the URL here points to an X.509 certificate or a chain of X.509 certificates. The resource pointed by the URL must hold the certificate or the chain of certificates in the PEM-encoded form. Each certificate in the chain must appear between the delimiters4: -----BEGIN CERTIFICATE----- and -----END CERTIFICATE-----. The public key corresponding to the key used to encrypt the Content Encryption Key (CEK) should be the very first entry in the certificate chain, and the rest is the certificates of intermediate CAs (certificate authority) and the root CA. The x5u is an optional parameter in the JOSE header.

- x5c: The x5c parameter in the JOSE header represents the X.509 certificate (or the certificate chain), which corresponds to the public key, which is used to encrypt the Content Encryption Key (CEK). This is similar to the jwk parameter we discussed before, but in this case instead of a JWK, it’s an X.509 certificate (or a chain of certificates). The certificate or the certificate chain is represented in a JSON array of certificate value strings. Each element in the array should be a base64-encoded DER PKIX certificate value. The public key corresponding to the key used to encrypt the Content Encryption Key (CEK) should be the very first entry in the JSON array, and the rest is the certificates of intermediate CAs (certificate authority) and the root CA. The x5c is an optional parameter in the JOSE header.

- x5t: The x5t parameter in the JOSE header represents the base64url-encoded SHA-1 thumbprint of the X.509 certificate corresponding to the key used to encrypt the Content Encryption Key (CEK). This is similar to the kid parameter we discussed before. Both these parameters are used to locate the key. If the token issuer uses the x5t parameter in the JOSE header to let the recipient know about the signing key, then the corresponding key should be exchanged “somehow” between the token issuer and the recipient beforehand. How this key exchange happens is out of the scope of the JWE specification. The x5t is an optional parameter in the JOSE header.

- x5t#s256: The x5t#s256 parameter in the JOSE header represents the base64url-encoded SHA256 thumbprint of the X.509 certificate corresponding to the key used to encrypt the Content Encryption Key (CEK). The only difference between x5t#s256 and the x5t is the hashing algorithm. The x5t#s256 is an optional parameter in the JOSE header.

- typ: The typ parameter in the JOSE header is used to define the media type of the complete JWE. There are two types of components that process a JWE: JWE implementations and JWE applications. Nimbus5 is a JWE implementation in Java. The Nimbus library knows how to build and parse a JWE. A JWE application can be anything, which uses JWE internally. A JWE application uses a JWE implementation to build or parse a JWE. In this case, the typ parameter is just another parameter for the JWE implementation. It will not try to interpret the value of it, but the JWE application would. The typ parameter will help JWE applications to differentiate the content when multiple types of objects are present. For a JWS token using JWS compact serialization and for a JWE token using JWE compact serialization, the value of the typ parameter is JOSE, and for a JWS token using JWS JSON serialization and for a JWE token using JWE JSON serialization, the value is JOSE+JSON. (JWS serialization was discussed in Chapter 7 and JWE serialization is discussed later in this chapter). The typ is an optional parameter in the JOSE header.

- cty: The cty parameter in the JOSE header is used to represent the media type of the secured content in the JWE. It is only recommended to use this parameter in the case of a nested JWT. The nested JWT is discussed later in this chapter, and the definition of the cty parameter is further explained there. The cty is an optional parameter in the JOSE header.

- crit: The crit parameter in the JOSE header is used to indicate to the recipient of the JWE that the presence of custom parameters, which neither defined by the JWE or JWA specifications, in the JOSE header. If these custom parameters are not understood by the recipient, then the JWE token will be treated as invalid. The value of the crit parameter is a JSON array of names, where each entry represents a custom parameter. The crit is an optional parameter in the JOSE header.

Out of all the 13 parameters defined earlier, 7 talk about how to reference the public key, which is used to encrypt the Content Encryption Key (CEK). There are three ways of referencing a key: external reference, embedded, and key identifier. The jku and x5u parameters fall under the external reference category. Both of them reference the key through a URI. The jwk and x5c parameters fall under embedded reference category. Each one of them defines how to embed the key to the JOSE header itself. The kid, x5t, and x5t#s256 parameters fall under the key identifier reference category. All three of them define how to locate the key using an identifier. Then again all the seven parameters can further divide into two categories based on the representation of the key: JSON Web Key (JWK) and X.509. The jku, jwk, and kid fall under the JWK category, while x5u, x5c, x5t, and x5t#s256 fall under the X.509 category. In the JOSE header of a given JWE token, at a given time, we only need to have one from the preceding parameters.

> **Note**
>


The JSON payload, which is subject to encryption, could contain whitespaces and/or line breaks before or after any JSON value.

The JWE specification does not restrict applications only to use 13 header parameters defined earlier. There are two ways to introduce new header parameters: public header names and private header names. Any header parameter that is intended to use in the public space should be introduced in a collision-resistant manner. It is recommended to register such public header parameters in the IANA JSON Web Signature and Encryption Header Parameters registry. The private header parameters are mostly used in a restricted environment, where both the token issuer and the recipients are well aware of each other. These parameters should be used with caution, because there is a chance for collision. If a given recipient accepts tokens from multiple token issuers, then the semantics of the same parameter may be different from one issuer to another, if it is a private header. In either case, whether it’s a public or a private header parameter, if it is not defined in the JWE or the JWA specification, the header name should be included in the crit header parameter, which we discussed before.

JWE Encrypted Key

To understand JWE Encrypted Key section of the JWE, we first need to understand how a JSON payload gets encrypted. The enc parameter of the JOSE header defines the content encryption algorithm, and it should be a symmetric Authenticated Encryption with Associated Data (AEAD) algorithm. The alg parameter of the JOSE header defines the encryption algorithm to encrypt the Content Encryption Key (CEK). We can also call this algorithm a key wrapping algorithm, as it wraps the CEK.

Authenticated Encryption

Encryption alone only provides the data confidentiality. Only the intended recipient can decrypt and view the encrypted data. Even though data is not visible to everyone, anyone having access to the encrypted data can change the bit stream of it to reflect a different message. For example, if Alice transfers US $100 from her bank account to Bob’s account and if that message is encrypted, then Eve in the middle can’t see what’s inside it. But, Eve can modify the bit stream of the encrypted data to change the message, let’s say from US $100 to US $150. The bank which controls the transaction would not detect this change done by Eve in the middle and will treat it as a legitimate transaction. This is why encryption itself is not always safe, and in the 1970s, this was identified as an issue in the banking industry.

Unlike just encryption, the Authenticated Encryption simultaneously provides a confidentiality, integrity, and authenticity guarantee for data. ISO/IEC 19772:2009 has standardized six different authenticated encryption modes: GCM, OCB 2.0, CCM, Key Wrap, EAX, and Encrypt-then-MAC. Authenticated Encryption with Associated Data (AEAD) extends this model to add the ability to preserve the integrity and authenticity of Additional Authenticated Data (AAD) that isn’t encrypted. AAD is also known as Associated Data (AD). AEAD algorithms take two inputs, plaintext to be encrypted and the Additional Authentication Data (AAD), and result in two outputs: the ciphertext and the authentication tag. The AAD represents the data to be authenticated, but not encrypted. The authentication tag ensures the integrity of the ciphertext and the AAD.

Let’s look at the following JOSE header. For content encryption, it uses A256GCM algorithm, and for key wrapping, RSA-OAEP:

{"alg":"RSA-OAEP","enc":"A256GCM"}

A256GCM is defined in the JWA specification. It uses the Advanced Encryption Standard (AES) in Galois/Counter Mode (GCM) algorithm with a 256-bit long key, and it’s a symmetric key algorithm used for AEAD. Symmetric keys are mostly used for content encryption. Symmetric key encryption is much faster than asymmetric key encryption. At the same time, asymmetric key encryption can’t be used to encrypt large messages. RSA-OAEP is too defined in the JWA specification. During the encryption process, the token issuer generates a random key, which is 256 bits in size, and encrypts the message using that key following the AES GCM algorithm. Next, the key used to encrypt the message is encrypted using RSA-OAEP,6 which is an asymmetric encryption scheme. The RSA-OAEP encryption scheme uses RSA algorithm with the Optimal Asymmetric Encryption Padding (OAEP) method. Finally, the encrypted symmetric key is placed in the JWE Encrypted Header section of the JWE.

Key Management Modes

The key management mode defines the method to derive or compute a value to the Content Encryption Key (CEK). The JWE specification employs five key management modes, as listed in the following, and the appropriate key management mode is decided based on the alg parameter, which is defined in the JOSE header:

1. 1.

Key encryption: With the key encryption mode, the value of the CEK is encrypted using an asymmetric encryption algorithm. For example, if the value of the alg parameter in the JOSE header is RSA-OAEP, then the corresponding key management algorithm is the RSAES OAEP using the default parameters. This relationship between the alg parameter and the key management algorithm is defined in the JWA specification. The RSAES OAEP algorithm occupies the key encryption as the key management mode to derive the value of the CEK.

 

2. 2.

Key wrapping: With the key wrapping mode, the value of the CEK is encrypted using a symmetric key wrapping algorithm. For example, if the value of the alg parameter in the JOSE header is A128KW, then the corresponding key management algorithm is the AES Key Wrap with the default initial value, which uses a 128-bit key. The AES Key Wrap algorithm occupies the key wrapping as the key management mode to derive the value of the CEK.

 

3. 3.

Direct key agreement: With the direct key agreement mode, the value of the CEK is decided based upon a key agreement algorithm. For example, if the value of the alg parameter in the JOSE header is ECDH-ES, then the corresponding key management algorithm is the Elliptic Curve Diffie-Hellman Ephemeral Static key agreement using Concat KDF. This algorithm occupies the direct key agreement as the key management mode to derive the value of the CEK.

 

4. 4.

Key agreement with key wrapping: With the direct key agreement with key wrapping mode, the value of the CEK is decided based upon a key agreement algorithm, and it is encrypted using a symmetric key wrapping algorithm. For example, if the value of the alg parameter in the JOSE header is ECDH-ES+A128KW, then the corresponding key management algorithm is the ECDH-ES using Concat KDF and CEK rapped with A128KW. This algorithm occupies the direct key agreement with key wrapping as the key management mode to derive the value of the CEK.

 

5. 5.

Direct encryption: With the direct encryption mode, the value of the CEK is the same as the symmetric key value, which is already shared between the token issuer and the recipient. For example, if the value of the alg parameter in the JOSE header is dir, then the direct encryption is occupied as the key management mode to derive the value of the CEK.

 

JWE Initialization Vector

Some encryption algorithms, which are used for content encryption, require an initialization vector, during the encryption process. Initialization vector is a randomly generated number, which is used along with a secret key to encrypt data. This will add randomness to the encrypted data, which will prevent repetition even if the same data gets encrypted using the same secret key again and again. To decrypt the message at the token recipient end, it has to know the initialization vector, hence included in the JWE token, under the JWE Initialization Vector element. If the content encryption algorithm does not require an initialization vector, then the value of this element should be kept empty.

JWE Ciphertext

The fourth element of the JWE token is the base64url-encoded value of the JWE ciphertext. The JWE ciphertext is computed by encrypting the plaintext JSON payload using the CEK, the JWE Initialization Vector, and the Additional Authentication Data (AAD) value, with the encryption algorithm defined by the header parameter enc. The algorithm defined by the enc header parameter should be a symmetric Authenticated Encryption with Associated Data (AEAD) algorithm. The AEAD algorithm, which is used to encrypt the plaintext payload, also allows specifying Additional Authenticated Data (AAD).

JWE Authentication Tag

The base64url-encoded value of the JWE Authentication Tag is the final element of the JWE token. The value of the authentication tag is produced during the AEAD encryption process, along with the ciphertext. The authentication tag ensures the integrity of the ciphertext and the Additional Authenticated Data (AAD).

The Process of Encryption (Compact Serialization)

We have discussed about all the ingredients that are required to build a JWE token under compact serialization. The following discusses the steps involved in building the JWE token. There are five elements in a JWE token; the first element is produced by step 6, the second element is produced by step 3, the third element is produced by step 4, the fourth element is produced by step 10, and the fifth element is produced by step 11.

1. 1.

Figure out the key management mode by the algorithm used to determine the Content Encryption Key (CEK) value. This algorithm is defined by the alg parameter in the JOSE header. There is only one alg parameter per JWE token.

 

2. 2.

Compute the CEK and calculate the JWE Encrypted Key based on the key management mode, picked in step 1. The CEK is later used to encrypt the JSON payload. There is only one JWE Encrypted Key element in the JWE token.

 

3. 3.

Compute the base64url-encoded value of the JWE Encrypted Key, which is produced by step 2. This is the second element of the JWE token.

 

4. 4.

Generate a random value for the JWE Initialization Vector. Irrespective of the serialization technique, the JWE token carries the value of the base64url-encoded value of the JWE Initialization Vector. This is the third element of the JWE token.

 

5. 5.

If token compression is needed, the JSON payload in plaintext must be compressed following the compression algorithm defined under the zip header parameter.

 

6. 6.

Construct the JSON representation of the JOSE header and find the base64url-encoded value of the JOSE header with UTF-8 encoding. This is the first element of the JWE token.

 

7. 7.

To encrypt the JSON payload, we need the CEK (which we already have), the JWE Initialization Vector (which we already have), and the Additional Authenticated Data (AAD). Compute ASCII value of the encoded JOSE header (step 6) and use it as the AAD.

 

8. 8.

Encrypt the compressed JSON payload (from step 5) using the CEK, the JWE Initialization Vector, and the Additional Authenticated Data (AAD), following the content encryption algorithm defined by the enc header parameter.

 

9. 9.

The algorithm defined by the enc header parameter is an AEAD algorithm, and after the encryption process, it produces the ciphertext and the Authentication Tag.

 

10. 10.

Compute the base64url-encoded value of the ciphertext, which is produced by step 9. This is the fourth element of the JWE token.

 

11. 11.

Compute the base64url-encoded value of the Authentication Tag, which is produced by step 9. This is the fifth element of the JWE token.

 

12. 12.

Now we have all the elements to build the JWE token in the following manner. The line breaks are introduced only for clarity.

BASE64URL-ENCODE(UTF8(JWE Protected Header)).

BASE64URL-ENCODE(JWE Encrypted Key).

BASE64URL-ENCODE(JWE Initialization Vector).

BASE64URL-ENCODE(JWE Ciphertext).

BASE64URL-ENCODE(JWE Authentication Tag)

 

JWE JSON Serialization

Unlike the JWE compact serialization, the JWE JSON serialization can produce encrypted data targeting at multiple recipients over the same JSON payload. The ultimate serialized form under JWE JSON serialization represents an encrypted JSON payload as a JSON object. This JSON object includes six top-level elements: protected, unprotected, recipients, iv, ciphertext, and tag. The following is an example of a JWE token, which is serialized with JWE JSON serialization:

{

    "protected":"eyJlbmMiOiJBMTI4Q0JDLUhTMjU2In0",

    "unprotected":{"jku":"https://server.example.com/keys.jwks"},

    "recipients":[

       {

        "header":{"alg":"RSA1_5","kid":"2011-04-29"},

        "encrypted_key":"UGhIOguC7IuEvf_NPVaXsGMoLOmwvc1GyqlIK..."

       },

       {

        "header":{"alg":"A128KW","kid":"7"},

        "encrypted_key":"6KB707dM9YTIgHtLvtgWQ8mKwb..."

       }

    ],

    "iv":"AxY8DCtDaGlsbGljb3RoZQ",

    "ciphertext":"KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY",

    "tag":"Mz-VPPyU4RlcuYv1IwIvzw"

}

JWE Protected Header

The JWE Protected Header is a JSON object that includes the header parameters that have to be integrity protected by the AEAD algorithm. The parameters inside the JWE Protected Header are applicable to all the recipients of the JWE token. The protected parameter in the serialized JSON form represents the base64url-encoded value of the JWE Protected Header. There is only one protected element in a JWE token at the root level, and any header parameter that we discussed before under the JOSE header can also be used under the JWE Protected Header.

JWE Shared Unprotected Header

The JWE Shared Unprotected Header is a JSON object that includes the header parameters that are not integrity protected. The unprotected parameter in the serialized JSON form represents the JWE Shared Unprotected Header. There is only one unprotected element in a JWE token at the root level, and any header parameter that we discussed before under the JOSE header can also be used under the JWE Shared Unprotected Header.

JWE Per-Recipient Unprotected Header

The JWE Per-Recipient Unprotected Header is a JSON object that includes the header parameters that are not integrity protected. The parameters inside the JWE Per-Recipient Unprotected Header are applicable only to a particular recipient of the JWE token. In the JWE token, these header parameters are grouped under the parameter recipients. The recipients parameter represents an array of recipients of the JWE token. Each member consists of a header parameter and an encryptedkey parameter .

- header: The header parameter, which is inside the recipients parameter, represents the value of the JWE header elements that aren’t protected for integrity by authenticated encryption for each recipient.

- encryptedkey: The encryptedkey parameter represents the base64url-encoded value of the encrypted key. This is the key used to encrypt the message payload. The key can be encrypted in different ways for each recipient.

Any header parameter that we discussed before under the JOSE header can also be used under the JWE Per-Recipient Unprotected Header.

JWE Initialization Vector

This carries the same meaning as explained under JWE compact serialization previously in this chapter. The iv parameter in the JWE token represents the value of the initialization vector used for encryption.

JWE Ciphertext

This carries the same meaning as explained under JWE compact serialization previously in this chapter. The ciphertext parameter in the JWE token carries the base64url-encoded value of the JWE ciphertext.

JWE Authentication Tag

This carries the same meaning as explained under JWE compact serialization previously in this chapter. The tag parameter in the JWE token carries the base64url-encoded value of the JWE Authentication Tag, which is an outcome of the encryption process using an AEAD algorithm.

The Process of Encryption (JSON Serialization)

We have discussed about all the ingredients that are required to build a JWE token under JSON serialization. The following discusses the steps involved in building the JWE token.

1. 1.

Figure out the key management mode by the algorithm used to determine the Content Encryption Key (CEK) value. This algorithm is defined by the alg parameter in the JOSE header. Under JWE JSON serialization, the JOSE header is built by the union of all the parameters defined under the JWE Protected Header, JWE Shared Unprotected Header, and Per-Recipient Unprotected Header. Once included in the Per-Recipient Unprotected Header, the alg parameter can be defined per recipient.

 

2. 2.

Compute the CEK and calculate the JWE Encrypted Key based on the key management mode, picked in step 1. The CEK is later used to encrypt the JSON payload.

 

3. 3.

Compute the base64url-encoded value of the JWE Encrypted Key, which is produced by step 2. Once again, this is computed per recipient, and the resultant value is included in the Per-Recipient Unprotected Header parameter, encryptedkey.

 

4. 4.

Perform steps 1–3 for each recipient of the JWE token. Each iteration will produce an element in the recipients JSON array of the JWE token.

 

5. 5.

Generate a random value for the JWE Initialization Vector. Irrespective of the serialization technique, the JWE token carries the value of the base64url-encoded value of the JWE Initialization Vector.

 

6. 6.

If token compression is needed, the JSON payload in plaintext must be compressed following the compression algorithm defined under the zip header parameter. The value of the zip header parameter can be defined either in the JWE Protected Header or JWE Shared Unprotected Header.

 

7. 7.

Construct the JSON representation of the JWE Protected Header, JWE Shared Unprotected Header, and Per-Recipient Unprotected Headers.

 

8. 8.

Compute the base64url-encoded value of the JWE Protected Header with UTF-8 encoding. This value is represented by the protected element in the serialized JWE token. The JWE Protected Header is optional, and if present there can be only one header. If no JWE header is present, then the value of the protected element will be empty.

 

9. 9.

Generate a value for the Additional Authenticated Data (AAD) and compute the base64url-encoded value of it. This is an optional step, and if it’s there, then the base64url-encoded AAD value will be used as an input parameter to encrypt the JSON payload, as in step 10.

 

10. 10.

To encrypt the JSON payload, we need the CEK (which we already have), the JWE Initialization Vector (which we already have), and the Additional Authenticated Data (AAD). Compute ASCII value of the encoded JWE Protected Header (step 8) and use it as the AAD. In case step 9 is done and then the value of AAD is computed as ASCII(encoded JWE Protected Header. BASE64URL-ENCODE(AAD)).

 

11. 11.

Encrypt the compressed JSON payload (from step 6) using the CEK, the JWE Initialization Vector, and the Additional Authenticated Data (AAD from step 10), following the content encryption algorithm defined by the enc header parameter.

 

12. 12.

The algorithm defined by the enc header parameter is an AEAD algorithm, and after the encryption process, it produces the ciphertext and the Authentication Tag.

 

13. 13.

Compute the base64url-encoded value of the ciphertext, which is produced by step 12.

 

14. 14.

Compute the base64url-encoded value of the Authentication Tag, which is produced by step 12.

 

Now we have all the elements to build the JWE token under JSON serialization.

> **Note**
>


The XML Encryption specification by W3C only talks about encrypting an XML payload. If you have to encrypt any content, then first you need to embed that within an XML payload and then encrypt. In contrast, the JWE specification is not just limited to JSON. You can encrypt any content with JWE without wrapping it inside a JSON payload.

Nested JWTs

Both in a JWS token and a JWE token, the payload can be of any content. It can be JSON, XML, or anything. In a Nested JWT, the payload must be a JWT itself. In other words, a JWT, which is enclosed in another JWS or JWE token, builds a Nested JWT. A Nested JWT is used to perform nested signing and encryption. The cty header parameter must be present and set to the value JWT, in the case of a Nested JWT. The following lists out the steps in building a Nested JWT, which signs a payload first using JWS and then encrypts the JWS token using JWE:

1. 1.

Build the JWS token with the payload or the content of your choice.

 

2. 2.

Based on the JWS serialization technique you use, step 1 will produce either a JSON object with JSON serialization or a three-element string where each element is separated out by a period (.)—with compact serialization.

 

3. 3.

Base64url-encode the output from step 2 and use it as the payload to be encrypted for the JWE token.

 

4. 4.

Set the value of the cty header parameter of the JWE JOSE header to JWT.

 

5. 5.

Build the JWE following any of the two serialization techniques defined in the JWE specification.

 

> **Note**
>


Sign first and then encrypt is the preferred approach in building a nested JWT, instead of sign and then encrypt. The signature binds the ownership of the content to the signer or the token issuer. It is an industry accepted best practice to sign the original content, rather than the encrypted content. Also, when sign first and encrypt the signed payload, the signature itself gets encrypted too, preventing an attacker in the middle stripping off the signature. Since the signature and all its related metadata are encrypted, an attacker cannot derive any details about the token issuer looking at the message. When encrypt first and sign the encrypted payload, then the signature is visible to anyone and also an attacker can strip it off from the message.

JWE vs. JWS

From an application developer’s point of view, it may be quite important to identify whether a given message is a JWE token or a JWS token and start processing based on that. The following lists out a few techniques that can be used to differentiate a JWS token from a JWE token:

1. 1.

When compact serialization is used, a JWS token has three base64url-encoded elements separated by periods (.), while a JWE token has five base64url-encoded elements separated by periods (.).

 

2. 2.

When JSON serialization is used, the elements of the JSON object produced are different in JWS token and JWE token. For example, the JWS token has a top-level element called payload, which is not in the JWE token, and the JWE token has a top-level element called ciphertext, which is not in the JWS token.

 

3. 3.

The JOSE header of a JWE token has the enc header parameter, while it is not present in the JOSE header of a JWS token.

 

4. 4.

The value of the alg parameter in the JOSE header of a JWS token carries a digital signature or a MAC algorithm or none, while the same parameter in the JOSE header of a JWE token carries a key encryption, key wrapping, direct key agreement, key agreement with key wrapping, or direct encryption algorithm.

 

Generating a JWE Token with RSA-OAEP and AES with a JSON Payload

The following Java code generates a JWE token with RSA-OAEP and AES. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch08/sample01—and it runs on Java 8+. First you need to invoke the method generateKeyPair() and pass the PublicKey(generateKeyPair().getPublicKey()) into the method buildEncryptedJWT():

// this method generates a key pair and the corresponding public key is used // to encrypt the message.

public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {

    // instantiate KeyPairGenerate with RSA algorithm.

    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

    // set the key size to 1024 bits.

    keyGenerator.initialize(1024);

    // generate and return private/public key pair.

    return keyGenerator.genKeyPair();

}

// this method is used to encrypt a JWT claims set using the provided public // key.

public static String buildEncryptedJWT(PublicKey publicKey) throws JOSEException {

    // build audience restriction list.

    List<String> aud = new ArrayList<String>();

    aud.add("https://app1.foo.com");

    aud.add("https://app2.foo.com");

    Date currentTime = new Date();

    // create a claims set.

    JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().

                  // set the value of the issuer.

                  issuer("https://apress.com").

                  // set the subject value - JWT belongs to this subject.

                  subject("john").

                  // set values for audience restriction.

                  audience(aud).

                  // expiration time set to 10 minutes.

                  expirationTime(new Date(new Date().getTime() + 1000 ∗ 60 ∗ 10)).

                  // set the valid from time to current time.

                  notBeforeTime(currentTime).

                  // set issued time to current time.

                  issueTime(currentTime).

                  // set a generated UUID as the JWT identifier.

                  jwtID(UUID.randomUUID().toString()).build();

    // create JWE header with RSA-OAEP and AES/GCM.

    JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

    // create encrypter with the RSA public key.

    JWEEncrypter encrypter = new RSAEncrypter((RSAPublicKey) publicKey);

    // create the encrypted JWT with the JWE header and the JWT payload.

    EncryptedJWT encryptedJWT = new EncryptedJWT(jweHeader, jwtClaims);

    // encrypt the JWT.

    encryptedJWT.encrypt(encrypter);

    // serialize into base64-encoded text.

    String jwtInText = encryptedJWT.serialize();

    // print the value of the JWT.

    System.out.println(jwtInText);

    return jwtInText;

}

The following Java code shows how to invoke the previous two methods:

KeyPair keyPair = generateKeyPair();

buildEncryptedJWT(keyPair.getPublic());

To build and run the program, execute the following Maven command from the ch08/sample01 directory.

\> mvn test -Psample01

Let’s see how to decrypt a JWT encrypted by RSA-OAEP. You need to know the PrivateKey corresponding to the PublicKey used to encrypt the message:

    public static void decryptJWT() throws NoSuchAlgorithmException,

                                JOSEException, ParseException {

    // generate private/public key pair.

    KeyPair keyPair = generateKeyPair();

    // get the private key - used to decrypt the message.

    PrivateKey privateKey = keyPair.getPrivate();

    // get the public key - used to encrypt the message.

    PublicKey publicKey = keyPair.getPublic();

    // get encrypted JWT in base64-encoded text.

    String jwtInText = buildEncryptedJWT(publicKey);

    // create a decrypter.

    JWEDecrypter decrypter = new RSADecrypter((RSAPrivateKey) privateKey);

    // create the encrypted JWT with the base64-encoded text.

    EncryptedJWT encryptedJWT = EncryptedJWT.parse(jwtInText);

    // decrypt the JWT.

    encryptedJWT.decrypt(decrypter);

    // print the value of JOSE header.

    System.out.println("JWE Header:" + encryptedJWT.getHeader());

    // JWE content encryption key.

    System.out.println("JWE Content Encryption Key: " + encryptedJWT.getEncryptedKey());

    // initialization vector.

    System.out.println("Initialization Vector: " + encryptedJWT.getIV());

    // ciphertext.

    System.out.println("Ciphertext : " + encryptedJWT.getCipherText());

    // authentication tag.

    System.out.println("Authentication Tag: " + encryptedJWT.getAuthTag());

    // print the value of JWT body

    System.out.println("Decrypted Payload: " + encryptedJWT.getPayload());

}

The preceding code produces something similar to the following output:

JWE Header: {"alg":"RSA-OAEP","enc":"A128GCM"}

JWE Content Encryption Key: NbIuAjnNBwmwlbKiIpEzffU1duaQfxJpJaodkxDj

SC2s3tO76ZdUZ6YfPrwSZ6DU8F51pbEw2f2MK_C7kLpgWUl8hMHP7g2_Eh3y

Th5iK6Agx72o8IPwpD4woY7CVvIB_iJqz-cngZgNAikHjHzOC6JF748MwtgSiiyrI

9BsmU

Initialization Vector: JPPFsk6yimrkohJf

Ciphertext: XF2kAcBrAX_4LSOGejsegoxEfb8kV58yFJSQ0_WOONP5wQ07HG

mMLTyR713ufXwannitR6d2eTDMFe1xkTFfF9ZskYj5qJ36rOvhGGhNqNdGEpsB

YK5wmPiRlk3tbUtd_DulQWEUKHqPc_VszWKFOlLQW5UgMeHndVi3JOZgiwN

gy9bvzacWazK8lTpxSQVf-NrD_zu_qPYJRisvbKI8dudv7ayKoE4mnQW_fUY-U10

AMy-7Bg4WQE4j6dfxMlQGoPOo

Authentication Tag: pZWfYyt2kO-VpHSW7btznA

Decrypted Payload:

{

   "exp":1402116034,

   "sub":"john",

   "nbf":1402115434,

   "aud":["https:\/\/app1.foo.com "," https:\/\/app2.foo.com"],

   "iss":"https:\/\/apress.com",

   "jti":"a1b41dd4-ba4a-4584-b06d-8988e8f995bf",

   "iat":1402115434

}

Generating a JWE Token with RSA-OAEP and AES with a Non-JSON Payload

The following Java code generates a JWE token with RSA-OAEP and AES for a non-JSON payload. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch08/sample02—and it runs on Java 8+. First you need to invoke the method generateKeyPair() and pass the PublicKey(generateKeyPair().getPublicKey()) into the method buildEncryptedJWT():

// this method generates a key pair and the corresponding public key is used // to encrypt the message.

public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, JOSEException {

    // instantiate KeyPairGenerate with RSA algorithm.

    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

    // set the key size to 1024 bits.

    keyGenerator.initialize(1024);

    // generate and return private/public key pair.

    return keyGenerator.genKeyPair();

}

// this method is used to encrypt a non-JSON payload using the provided // public key.

public static String buildEncryptedJWT(PublicKey publicKey) throws JOSEException {

    // create JWE header with RSA-OAEP and AES/GCM.

    JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

    // create encrypter with the RSA public key.

    JWEEncrypter encrypter = new RSAEncrypter((RSAPublicKey) publicKey);

    // create a JWE object with a non-JSON payload

    JWEObject jweObject = new JWEObject(jweHeader, new Payload("Hello world!"));

    // encrypt the JWT.

    jweObject.encrypt(encrypter);

    // serialize into base64-encoded text.

    String jwtInText = jweObject.serialize();

    // print the value of the JWT.

    System.out.println(jwtInText);

    return jwtInText;

}

To build and run the program, execute the following Maven command from the ch08/sample02 directory.

\> mvn test -Psample02

Generating a Nested JWT

The following Java code generates a nested JWT with RSA-OAEP and AES for encryption and HMAC-SHA256 for signing. The nested JWT is constructed by encrypting the signed JWT. You can download the complete Java sample as a Maven project from https://github.com/apisecurity/samples/tree/master/ch08/sample03—and it runs on Java 8+. First you need to invoke the method buildHmacSha256SignedJWT() with a shared secret and pass its output along with the generateKeyPair().getPublicKey() into the method buildNestedJWT():

// this method generates a key pair and the corresponding public key is used // to encrypt the message.

public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {

    // instantiate KeyPairGenerate with RSA algorithm.

    KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");

    // set the key size to 1024 bits.

    keyGenerator.initialize(1024);

    // generate and return private/public key pair.

    return keyGenerator.genKeyPair();

}

// this method is used to sign a JWT claims set using the provided shared // secret.

public static SignedJWT buildHmacSha256SignedJWT(String sharedSecretString) throws JOSEException {

    // build audience restriction list.

    List<String> aud = new ArrayList<String>();

    aud.add("https://app1.foo.com");

    aud.add("https://app2.foo.com");

    Date currentTime = new Date();

    // create a claims set.

    JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().

    // set the value of the issuer.

    issuer("https://apress.com").

    // set the subject value - JWT belongs to this subject.

    subject("john").

    // set values for audience restriction.

    audience(aud).

    // expiration time set to 10 minutes.

    expirationTime(new Date(new Date().getTime() + 1000 ∗ 60 ∗ 10)).

    // set the valid from time to current time.

    notBeforeTime(currentTime).

    // set issued time to current time.

    issueTime(currentTime).

    // set a generated UUID as the JWT identifier.

    jwtID(UUID.randomUUID().toString()).build();

    // create JWS header with HMAC-SHA256 algorithm.

    JWSHeader jswHeader = new JWSHeader(JWSAlgorithm.HS256);

    // create signer with the provider shared secret.

    JWSSigner signer = new MACSigner(sharedSecretString);

    // create the signed JWT with the JWS header and the JWT body.

    SignedJWT signedJWT = new SignedJWT(jswHeader, jwtClaims);

    // sign the JWT with HMAC-SHA256.

    signedJWT.sign(signer);

    // serialize into base64-encoded text.

    String jwtInText = signedJWT.serialize();

    // print the value of the JWT.

    System.out.println(jwtInText);

    return signedJWT;

}

// this method is used to encrypt the provided signed JWT or the JWS using // the provided public key.

public static String buildNestedJWT(PublicKey publicKey, SignedJWT signedJwt) throws JOSEException {

    // create JWE header with RSA-OAEP and AES/GCM.

    JWEHeader jweHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

    // create encrypter with the RSA public key.

    JWEEncrypter encrypter = new RSAEncrypter((RSAPublicKey) publicKey);

    // create a JWE object with the passed SignedJWT as the payload.

    JWEObject jweObject = new JWEObject(jweHeader, new Payload(signedJwt));

    // encrypt the JWT.

    jweObject.encrypt(encrypter);

    // serialize into base64-encoded text.

    String jwtInText = jweObject.serialize();

    // print the value of the JWT.

    System.out.println(jwtInText);

    return jwtInText;

}

To build and run the program, execute the following Maven command from the ch08/sample03 directory.

\> mvn test -Psample03

## Summary

- The JWE specification standardizes the way to represent encrypted content in a cryptographically safe manner.

- JWE defines two serialized forms to represent the encrypted payload: the JWE compact serialization and JWE JSON serialization.

- In the JWE compact serialization, a JWE token is built with five components, each separated by a period (.): JOSE header, JWE Encrypted Key, JWE Initialization Vector, JWE Ciphertext, and JWE Authentication Tag.

- The JWE JSON serialization can produce encrypted data targeting at multiple recipients over the same payload.

- In a Nested JWT, the payload must be a JWT itself. In other words, a JWT, which is enclosed in another JWS or JWE token, builds a Nested JWT.

- A Nested JWT is used to perform nested signing and encryption.