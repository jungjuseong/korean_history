# WEMEETPLACE Map Cloud Platform API

## 개요

위밋플레이스 클라우드 플랫폼에서 제공하는 라우팅/맵 솔루션 상품을 이용할 수 있도록 지원하는 응용 프로그램 인터페이스(API)를 제공하고 있습니다.

이 페이지는 위밋플레이스 클라우드 플랫폼 API 대한 간략한 설명 및 API 호출 방법을 제공합니다.

API는 RESTful API 방식으로 제공되며, XML와 JSON 형식으로 응답합니다. 액션에 따라 파라미터 값을 입력하고 등록, 수정, 삭제, 조회할 수 있으며, 서비스 및 운영 도구 자동화에 활용할 수 있습니다. HTTP 방식의 GET/POST 메서드 호출을 통해서 사용됩니다.

만일 호출이 잘못되었을 경우는 오류 코드와 메시지를 리턴합니다.

## 위밋플레이스 맵 클라우드 플랫폼 API 호출 절차

위밋플레이스 맵 클라우드 플랫폼 API 호출은 다음과 같은 단계로 진행되어야 합니다.

## 인증키 생성하기

위밋플레이스 맵 클라우드 플랫폼 계정이 생성되면 기본적으로 맵 플랫폼 API 인증키가 한개 발급됩니다. 발급된 인증키는 클라우드 플랫폼 홈페이지의 [마이페이지] > [계정관리] > [인증키관리] 에서 확인할 수 있습니다. 

> **참고**
>
> 인증키는 계정 생성 시 자동으로 발급되는 것 외에 사용자가 하나 더 생성할 수 있어서 두 개까지 발급받을 수 있습니다.
>
> 인증키를 '사용 중지'로 설정하거나 삭제하면 유효하지 않은 키로 인식됩니다.

API 인증키는 Access 키와 Secret 키 한 쌍으로 구성되어 있습니다. 한 쌍의 API 인증키는 API를 인증할 때 파라미터로 직접 전달합니다.

1. 클라우드 플랫폼 홈페이지에서 로그인을 합니다.

2. [마이페이지] > [계정관리] > [인증키관리] 메뉴로 접속하고 "신규 API 인증키 생성" 버튼을 클릭합니다.

   기존에 생성하신 인증 키가 있는 경우에는 해당 인증 키를 사용할 수 있습니다.

3. API 인증키 관리에서 발급받은 자신의 Access Key ID와 Secret Key를 확인합니다.

## API 호출하기

**AUTHPARAMS**

|Header	|Description|
|-----|----|
|`x-ncp-apigw-timestamp` | - 1970년 1월 1일 00:00:00 협정 세계시(UTC)부터의 경과 시간을 밀리초(Millisecond)로 나타낸 것. <br> - API Gateway 서버와 시간 차가 5분 이상 나는 경우 유효하지 않은 요청으로 간주|
|`x-ncp-iam-access-key`| - 클라우드 플랫폼에서 발급받은 Access Key ID|
|`x-ncp-apigw-signature-v2`|- 위 예제의 Body를 Access Key ID와 맵핑되는 Secret Key로 암호화한 서명(HMAC 암호화 알고리즘은 HmacSHA256 사용) |

### AUTHPARAMS 요청 예시

```sh
$ curl -i -X GET \
-H "x-ncp-apigw-timestamp:1505290625682" \
-H "x-ncp-iam-access-key:D78BB444D6D3C84CA38D" \
-H "x-ncp-apigw-signature-v2:WTPItrmMIfLUk/UyUIyoQbA/z5hq9o3G8eQMolUzTEa=" \
'https://example.apigw.ntruss.com/photos/puppy.jpg?query1=&query2'
```

### Signature 생성하기

- 개행문자는 \n을 사용합니다.

- 요청에 맞게 StringToSign을 생성하고 SecretKey로 HmacSHA256 알고리즘으로 암호화한 후 Base64로 인코딩합니다.
 
- 이 값을 x-ncp-apigw-signature-v2로 사용합니다.

> 주의
> 요청 헤더의 x-ncp-apigw-timestamp 값과 StringToSign의 timestamp는 반드시 같은 값이여야 합니다.

#### 요청	
```
GET /photos/puppy.jpg?query1=&query2 <br> x-ncp-apigw-timestamp={timestamp} <br> x-ncp-iam-access-key={accesskey}<br>x-ncp-apigw-signature-v2={signature}
```

#### StringToSign
```sh
GET /photos/puppy.jpg?query1=&query2 {timeStamp} {accessKey}|
```

## 요청 예시

> Java
```java
public String makeSignature() {
	String space = " ";					// one space
	String newLine = "\n";					// new line
	String method = "GET";					// method
	String url = "/photos/puppy.jpg?query1=&query2";	// url (include query string)
	String timestamp = "{timestamp}";			// current timestamp (epoch)
	String accessKey = "{accessKey}";			// access key id (from portal or Sub Account)
	String secretKey = "{secretKey}";

	String message = new StringBuilder()
		.append(method)
		.append(space)
		.append(url)
		.append(newLine)
		.append(timestamp)
		.append(newLine)
		.append(accessKey)
		.toString();

	SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
	Mac mac = Mac.getInstance("HmacSHA256");
	mac.init(signingKey);

	byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
	String encodeBase64String = Base64.encodeBase64String(rawHmac);

  return encodeBase64String;
}
```

> JavaScript

```js
/*
https://code.google.com/archive/p/crypto-js/
https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/crypto-js/CryptoJS%20v3.1.2.zip
*/

/*
CryptoJS v3.1.2
code.google.com/p/crypto-js
(c) 2009-2013 by Jeff Mott. All rights reserved.
code.google.com/p/crypto-js/wiki/License
*/
<script type="text/javascript" src="./CryptoJS/rollups/hmac-sha256.js"></script>
<script type="text/javascript" src="./CryptoJS/components/enc-base64.js"></script>

function makeSignature() {
	var space = " ";				// one space
	var newLine = "\n";				// new line
	var method = "GET";				// method
	var url = "/photos/puppy.jpg?query1=&query2";	// url (include query string)
	var timestamp = "{timestamp}";			// current timestamp (epoch)
	var accessKey = "{accessKey}";			// access key id (from portal or Sub Account)
	var secretKey = "{secretKey}";			// secret key (from portal or Sub Account)

	var hmac = CryptoJS.algo.HMAC.create(CryptoJS.algo.SHA256, secretKey);
	hmac.update(method);
	hmac.update(space);
	hmac.update(url);
	hmac.update(newLine);
	hmac.update(timestamp);
	hmac.update(newLine);
	hmac.update(accessKey);

	var hash = hmac.finalize();

	return hash.toString(CryptoJS.enc.Base64);
}
```
