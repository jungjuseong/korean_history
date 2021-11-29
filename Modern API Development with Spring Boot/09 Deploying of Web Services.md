
# Chapter 9: Deployment of Web Services

In this chapter, you will learn about the fundamentals of containerization, Docker, and Kubernetes. You will then use these concepts to containerize a sample e-commerce app using Docker. This container will then be deployed as a Kubernetes cluster. You are going to use minikube for Kubernetes, which makes learning and Kubernetes-based development easier.

You'll explore the following topics in this chapter:

- Exploring the fundamentals of containerization
- Building a Docker image
- Deploying an application in Kubernetes

After completing this chapter, you will be able to perform containerization and container deployment in a Kubernetes cluster.

## Technical requirements
You will need the following for developing and executing the code in this chapter:

- Docker
- Kubernetes (Minikube)
- Any Java IDE, such as NetBeans, IntelliJ, or Eclipse
- JDK 15+
- An internet connection to clone the code (https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/tree/main/Chapter09) and download the dependencies and Gradle
- Postman/cURL (for API testing)

So, let's begin!

## Exploring the fundamentals of containerization

One problem that's encountered frequently by teams while developing large, complex systems is that the code that works on one machine doesn't work on another. One developer might say it works on their machine, but Quality Assurance (QA) claims that it is failing on "their machine" with animated face. The main reason behind these kinds of scenarios is a mismatch of dependencies (such as different versions of Java, a certain web server, or OS), configurations, or files.

Also, setting up a new environment for deploying new products sometimes takes a day or more. This is unacceptable in today's environment and slows down your development turnaround. These kinds of issues can be solved by containerizing the application.

In containerization, an application is bundled, configured, and wrapped with all the required dependencies and files. This bundle can then be run on any machine that support the containerization process. It provides the exact same behavior on all environments. It not only solves bugs related to misconfigurations or dependencies, but also reduces the deployment time to a few minutes or less.

This bundle, which sits on top of a physical machine and its operating system, is called a container. This container shares the kernel, as well as the libraries and binaries of its host operating system, in read-only mode. Therefore, these are lightweight. In this chapter, you are going to use Docker and Kubernetes for containerization and container deployment.

A related concept is virtualization – "the process of creating a virtual environment using the existing hardware system by splitting it into different parts. Each part acts as a separate, distinct, individual system." These distinct individual systems are called virtual machines (VMs). Each VM runs on its own unique operating system with its own binaries, libraries, and apps. VMs are heavy weighted and can be many gigabytes in size. A hardware system can have VMs with different operating systems such Unix, Linux, and Windows. The following diagram depicts the difference between virtual machines and containers:

![](https://learning.oreilly.com/api/v2/epubs/urn:orm:book:9781800562479/files/image/Figure_9.1_B16561.jpg)
Figure 9.1 – Virtual machines versus containers

Sometimes, people think that virtualization and containerization are the same thing. But they are not. VMs are created on top of the host system, which shares its hardware, whereas containers are executed as isolated processes on top of the hardware and its OS. Containers are lightweight and are only a few MBs, sometimes a GB, whereas VMs are heavyweight and are many GBs in size. Containers run faster than VMs, and they are also more portable.

We'll explore containers in more detail by building a Docker image in the next section.


## Building a Docker image

At this point, you know the benefit of containerization and why it is becoming popular: you create an application, product, or service, bundle it using containerization, and give it to the QA team, customer, or DevOps team to run it without any issues.

In this section, you'll learn how to use Docker as a containerization platform. Let's learn about it before creating a Docker image of a sample ecommerce app.

### Exploring Docker

Docker is a leading container platform and an open source project. Docker was launched in 2013. 10,000 developers tried it after its interactive tutorial was launched in August 2013. Docker was downloaded 2.75 million times by the time of its 1.0 release in June 2013. Many large corporations have signed a partnership agreement with Docker Inc., including Microsoft, Red Hat, HP, and OpenStack, as well as service providers such as AWS, IBM, and Google.

Docker makes use of Linux kernel features to ensure resource isolation and the packaging of the application, along with its dependencies, such as cgroups and namespaces. Everything in a Docker container executes natively on the host and uses the host kernel directly. 

Each container has its own user namespace – a process identifier (PID) for process isolation, a network (NET) for managing network interfaces, inter-process communication (IPC) for managing access to IPC resources, a mount point (MNT) for managing filesystem mount points, and Unix Time Sharing (UTS) namespaces for isolating kernel and version identifiers. This packaging of dependencies enables an application to run as expected across different Linux operating systems and distributions by supporting a level of portability.

Furthermore, this portability allows developers to develop an application in any language and then easily deploy it from any computer, such as a laptop, to different environments, such as test, stage, or production. Docker runs natively on Linux. However, you can also run Docker on Windows and macOS.

Containers are comprised of just the application and its dependencies, including the basic OS. This makes the application lightweight and efficient in terms of resource utilization. Developers and system administrators are interested in a container's portability and efficient resource utilization.

We'll explore Docker's architecture in the next subsection.

### Docker's architecture
As specified in the Docker documentation, Docker uses a client-server architecture. The Docker client (Docker) is basically a command-line interface (CLI) that is used by an end user; clients communicate back and forth with the Docker server (read as a Docker daemon). The Docker daemon does the heavy lifting in that it builds, runs, and distributes your Docker containers. The Docker client and the daemon can run on the same system or on different machines.

The Docker client and daemon communicate via sockets or through a RESTful API. Docker registers are public or private Docker image repositories that you can upload or download images from; for example, Docker Hub (hub.docker.com) is a public Docker registry.

The primary components of Docker are as follows:

- **Docker image**: A Docker image is a read-only template. For example, an image could contain an Ubuntu OS with an Apache web server and your web application installed on it. Docker images are build components of Docker, and images are used to create Docker containers. Docker provides a simple way to build new images or update existing images. You can also use images created by others and/or extend them.

- **Docker container**: A Docker container is created from a Docker image. Docker works so that the container can only see its own processes and has its own filesystem layered on a host filesystem and a networking stack, which pipes to the host-networking stack. Docker containers can be run, started, stopped, moved, or deleted. Docker also provides commands such as docker stats and docker events for container usage statics, such as CPU and memory usage, and for activities that are performed by the Docker daemons, respectively. These commands help you monitor Docker in a deployed environment.

You also need to be aware of Docker's container life cycle, which is as follows:

1. 컨테이너 생성: Docker creates a container from the Docker image using the `docker create` command.

2. 컨테이너 실행: Docker runs the container that was created in step 1 using the `docker run` command.

3. 실행 정지 (optional): Docker pauses the process running inside the container using the `docker pause` command.

4. 다시 실행 the container (optional): Docker un pauses the processes running inside the container using the `docker unpause` command.

5. 컨테이너 시작: Docker starts the container using the `docker start` command.

6. 컨테이너 정지: Docker stops the container and processes running inside the container using the `docker stop` command.

7. Restarts the container: Docker restarts the container and processes running inside it using the `docker restart` command.

8. Kills the container: Docker kills the running container using the `docker kill` command.

9. Destroys the container: Docker removes the stopped containers using the `docker rm` command. Therefore, this should only be performed for stopped containers.

At this point, you might be eager to use the Docker container life cycle, but first, you'll need to install Docker by going to https://docs.docker.com/get-docker/.

Once you've installed Docker, go to https://docs.docker.com/get-started/#start-the-tutorial to execute the first Docker command. You can refer to https://docs.docker.com/engine/reference/commandline/docker/ to learn more about Docker commands.

For more information, you can look at the overview of Docker that is provided by Docker (https://docs.docker.com/get-started/overview/).

Let's make the necessary code changes so that we can create a Docker image for a sample ecommerce app.

## Configuring code to build an image

8장의 테스팅 API 코드를 이 장의 기반으로 한다. 도커 이미지를 만드는데 라이브러리를 추가할 필요는 없지만 Spring Boot Actuator 의존성을 추가해서 상용 준비가 된 앱 기능을 제공하기로 한다.

We'll add the Actuator dependency and configure it in the next subsection.

### Adding Actuator

Actuator의 기능은 HttP Rest API와 JMX로 앱을 모니터링하고 관리하는데 도움을 준다. 이러한 엔드포인트는 각자의 문서에서 찾을 수 있다(https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-endpoints).

In this chapter, however, we are only going to use the `/actuator/health` endpoint, which tells us about the application's health status.

You can add `Actuator` by performing the following steps:

1. Add the Actuator dependency to build.gradle (https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter09/build.gradle):
```gradle
  runtimeOnly 'org.springframework.boot:spring-boot-starter-actuator'
```

2. Next, you need to remove all security from the `/actuator` endpoints. Let's add a constant to `Constants.java` (https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter09/src/main/java/com/packt/modern/api/security/Constants.java) for the Actuator URL, as shown here:
```java
  public static final String ACTUATOR_URL_PREFIX = "/actuator/**";
```
3. Now, you can update the security configuration in SecurityConfig.java, as shown here:
```java
  // rest of the code
  .antMatchers(H2_URL_PREFIX).permitAll()
  .antMatchers(ACTUATOR_URL_PREFIX).permitAll()
  .mvcMatchers(HttpMethod.POST, "/api/v1/addresses/**")
  // rest of the code
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter09/src/main/java/com/packt/modern/api/security/SecurityConfig.java

With that, you have added an `antMatcher` with Actuator endpoints. This allows all Actuator endpoints, both with and without authentication and authorization.

Now, you can configure the Spring Boot plugin's task, called `bootBuildImage`, to customize the name of the Docker image. We will do this in the next subsection.

### Configuring the Spring Boot plugin task

The Spring Boot Gradle plugin already provides a command (`bootBuildImage`) for building Docker images. It becomes available when the java plugin is applied in the plugins section. However, it is not available when you build the .`war` file. Therefore, you don't need to add any additional plugins as you are going to build a `.jar` file. However, there are certain plugins that are available that you can use if you want.

You can customize the image's name by adding the following code block to the `build.gradle` file:

```gradle
bootBuildImage {
    imageName = "192.168.80.1:5000/${project.name}:${project.version}"
}
```
Here, change the IP address and port of the local Docker registry. A Docker image will be built based on your project's name and version. The project version is already defined in the build.gradle file's top section. The project name, on the other hand, is picked from the settings.gradle file (https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter09/settings.gradle). 

Let's rename it, as shown in the following code snippet:
```
rootProject.name = 'ecomm-api-development-chapter09'
```
In Chapter 8, Testing APIs, the value of `rootProject.name` contains a capital letter, so the Docker image build failed. This is because the plugin has a validation check for capital letters. 도커 이미지 이름은 소문자가 가능하다

For more information and customization options, please refer to the plugin documentation (https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image).

Now that you have configured the code, you can use this to build an image after configure the Docker registry. You will do this in the next subsection.

### Configuring the Docker registry

By default, when you build an image (gradlew bootBuildImage), it will build an image called `docker.io/library/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT`. You may be wondering why, even though you have given only name:version, how come it is prefixing it with docker.io/library/. This is because if you don't specify the Docker registry, it takes the docker.io registry by default. 도커 이미지를 푸시/풀 할 수 있는 도커 레지스트리가 필요하다.

이미지가 빌드되면 도커 허브에 푸시할 수 있다. 그런 다음 쿠버네티스 환경에서 배포하기 위해 도커 허브에서 이미지를 가져올 수 있다. 하지만 개발 목적에서는 이상적인 시나리오가 아니다. 최선의 옵션은 로컬 도커 레지스트리를 설정하여 쿠버네티스 환경에서 사용하는 것이다.

> USING GIT BASH ON WINDOWS

You can use Git Bash on Windows to run these commands, which emulates Linux commands.

Let's execute the following commands to check if Docker is up and running:

```
$ docker version
Client:
  Version:           18.06.1-ce
  API version:       1.38
  Go version:        go1.10.3
  Git commit:        e68fc7a
  Built:             Tue Aug 21 17:21:34 2018
  OS/Arch:           windows/amd64
  Experimental:      false
Server: Docker Engine - Community
  Engine:
  Version:          20.10.5
// truncated output for brevity
```
Docker is now up and running. Now, we can pull and start the local Docker registry by using the following command:

```sh
$ docker run -d -p 5000:5000 -e REGISTRY_STORAGE_DELETE_ENABLED=true --restart=always --name registry registry:2
```

This command downloads the `register:2` image and then executes the container that was created by it, called registry, on port 5000. There are two port entries: one internal container port and another exposed external port. Both are set to 5000. The `--restart=always` flag tells Docker to start the registry container every time Docker is restarted. The `REGISTRY_STORAGE_DELETE_ENABLED` flag is used to remove any images from registry as it is set to true. The default value of this flag is false.

Now, let's check the Docker containers:
```sh
$ docker ps

CONTAINER ID  IMAGE COMMAND CREATED STATUS PORTS NAMES

10cd2f36af1e  registry:2   "/entrypoint.sh /etc…"  6 days ago Up 5 seconds  0.0.0.0:5000->5000/tcp   registry
```

This shows that the Docker container registry is up and running and was created using the `registry:2` image.

The host name is import when you're using the containers. Therefore, we'll use the IP number instead of the local hostname for the registry host. This is because the container will refer to its localhost when you use localhost, rather than the localhost of your system. In a Kubernetes environment, you need to provide a registry host, so you need to use the IP or proper `hostname` in place of localhost.

Let's find out what IP we can use by running the following command:

```sh
$ ipconfig

Windows IP Configuration
Ethernet adapter Ethernet:
   Media State . . . . . . . . . . . : Media disconnected
   Connection-specific DNS Suffix  . :

Ethernet adapter vEthernet (Default Switch):
   Connection-specific DNS Suffix  . :
   Link-local IPv6 Address . . . . . : ef80::2099:f848:8903:f996%81
   IPv4 Address. . . . . . . . . . . : 192.168.80.1
   Subnet Mask . . . . . . . . . . . : 255.255.240.0
   Default Gateway . . . . . . . . . :
```
You can find your system's IP address in the row highlighted in the preceding output. You can use similar commands on macOS and Linux to find out the IP address of your system.

You haven't configured the Transport Layer Security (TLS) for your system host, so this registry is an insecure registry. Docker only supports secure registries by default. You must configure Docker so that it can use insecure registries. Please refer to the Docker documentation to learn how to configure an insecure registry (https://docs.docker.com/registry/insecure/#deploy-a-plain-http-registry).

Please note that to build and publish the image successfully, the Docker configuration must be performed with a local registry, as explained previously.

> NOTE

Don't use an insecure registry on any environment other than a local or development environment.

Now, let's create a Docker image for a sample ecommerce app.

### Executing a Gradle task to build an image

You need to make a change to the bootBuildImage task so that the image's name contains the local Docker registry's prefix. Spring Boot bootBuildImage uses Paketo buildpacks to build the docker image. It supports Long-term Support (LTS) Java releases and only current non-LTS Java release. It means, for non-LTS if Java 16 is released then it would remove support of Java 15. Similarly, when Java 17 gets released, it would remove the Java 16 support. However, it won't remove the Java 17 support when Java 18 would release because Java 17 is LTS release. We can make this change like so:

```gradle
bootBuildImage {
imageName = "192.168.80.1:5000/${project.name}:${project.version}"
}
// Paketo removes 6 monthly Java release,

// therefore better to use Java 17, which is a LTS
environment = ["BP_JVM_VERSION" : "16"]
```

Here, you have customized the name of the Docker image according to local Docker registry. You should change the IP address and port as per your system and configuration. You have also used the environment property to set the Paketo buildpacks variables. You have set the JVM version to 16, as at the time of writing this chapter, Paketo buildpacks has removed the support of Java 15. It is recommend to use Java 17 (or any future LTS release), which should be release in September 2021. You can find all the supported Paketo buildpacks environment variables at https://github.com/paketo-buildpacks/bellsoft-liberica#configuration. Now, you can build the image by executing the following command from your project's home directory:

```sh
$ gradlew clean build

  build the jar file of app after running the tests

$ gradlew bootBuildImage
> Task :bootBuildImage

Building image '192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT'

> Pulling builder image 'docker.io/paketobuildpacks/builder:base' ..................................................

> Pulled builder image 'paketobuildpacks/builder@sha256:e4 6e13f550df3b1fd694000e417d6bed534772716090f11a9876501ddeecb521'

> Pulling run image 'docker.io/paketobuildpacks/run:base-cnb' ..................................................

> Pulled run image 'paketobuildpacks/run@sha256:367a43536f60c21190cea5c06d040d01d29f4102840d6b3e1dcd72 ed2eb71721'

> Executing lifecycle version v0.10.2

> Using build cache volume 'pack-cache-8020b69fd072.build'

// continue…
```

The Spring Boot Gradle plugin uses the Paketo BellSoft Liberica Buildpack (docker.io/paketobuildpacks) to build an application image. First, it pulls the image from Docker Hub and then runs its container, as shown here:
```
> Running creator

    [creator]     ===> DETECTING

    [creator]     5 of 18 buildpacks participating

    [creator]     paketo-buildpacks/ca-certificates   2.1.0

    [creator]     paketo-buildpacks/bellsoft-liberica 7.0.1

    [creator]     paketo-buildpacks/executable-jar    5.0.0

    [creator]     paketo-buildpacks/dist-zip          4.0.0

    [creator]     paketo-buildpacks/spring-boot       4.1.0

    [creator]     ===> ANALYZING

    [creator]     Previous image with name "192.168.80.1:5000/                  packt-modern-api-development-chapter09:0.0.1-                  SNAPSHOT" not found

    [creator]     // Truncated the output for brevity

    [creator]

    [creator]     Paketo BellSoft Liberica Buildpack 7.0.1

    [creator] https://github.com/paketo-buildpacks/bellsoft-              liberica

    [creator]       // Truncated the output for brevity

    [creator] BellSoft Liberica JRE 15.0.2: Contributing to               layer

    [creator]         Downloading from https://github.com/bell-                      sw/Liberica/releases/download/15.0.2+10/                      bellsoft-jre15.0.2+10-linux-amd64.tar.gz

    [creator]         Verifying checksum

    [creator] Expanding to /layers/paketo-buildpacks_bellsoft-              liberica/jre

    [creator]       // Truncated the output for brevity
```
The Spring Boot plugin uses Bellsoft's JRE 15.0.2 with Linux as a base image for building images. It uses the finely grained filesystem layers inside the container to do so:

```
    [creator] Launch Helper: Contributing to layer

    [creator] Creating /layers/paketo-buildpacks_                      bellsoft-liberica/helper/exec.d/active-                      processor-count

    [creator] Creating /layers/paketo-buildpacks_                      bellsoft-liberica/helper/exec.d/java-opts

    [creator] // Truncated the output for brevity  

    [creator] JVMKill Agent 1.16.0: Contributing to layer

    [creator] Downloading from https://github.com/                      cloudfoundry/jvmkill/releases/download/v1.16.0.RELEASE/jvmkill-1.16.0-RELEASE.so

    [creator] Verifying checksum

    [creator] Paketo Executable JAR Buildpack 5.0.0

    [creator]   https://github.com/paketo-buildpacks/                executable-jar

    [creator] Class Path: Contributing to layer

    [creator] // Truncated the output for brevity
``` 
It continues to add the layers and then the labels. At the end, it creates the Docker image:
```
    [creator] Paketo Spring Boot Buildpack 4.1.0

    [creator] // Truncated the output for brevity

    [creator] Adding layer 'paketo-buildpacks/executable-jar:classpath'

    [creator] Adding layer 'paketo-buildpacks/spring-boot:helper'

    [creator] Adding layer 'paketo-buildpacks/spring-boot:spring-cloud-bindings'

    [creator] Adding layer 'paketo-buildpacks/spring-                  boot:web-application-type'

    [creator] Adding 5/5 app layer(s)
    [creator] Adding layer 'launcher'
    [creator] // Truncated the output for brevity
    [creator] Adding label 'org.springframework.boot.version'
    [creator] Setting default process type 'web'
    [creator] *** Images (672dd1e3fdb4):
    [creator] 192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT

Successfully built image '192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT'

BUILD SUCCESSFUL in 58m 19s
```
You can learn more about Spring Boot, Docker, and Kubernetes and their configuration at https://github.com/dsyer/kubernetes-intro.

Now that the Docker image has been built, you can use this image to run the sample ecommerce app locally using the following command:
```sh
$ docker run -p 8080:8080 192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT
```
This command will run the application on port 8080 inside the container. Because it has been exposed on port 8080, you can access the sample ecommerce app on 8080 outside the container too, once the app is up and running. You can test the application by running the following command in a separate Terminal tab/window once the application container is up and running:

```sh
$ curl localhost:8080/actuator/health

{"status":"UP"}

$ curl localhost:8080/actuator | jq .

{

  "_links": {

    "self": {

      "href": "http://localhost:8080/actuator",

      "templated": false },

    "health-path": {

      "href": "http://localhost:8080/actuator/

               health/{*path}",

      "templated": true  },

    "health": {

      "href": "http://localhost:8080/actuator/health",

      "templated": false },

    "info": {

      "href": "http://localhost:8080/actuator/info",

      "templated": false  }

  }

}
```
The curl localhost:8080/actuator command returns the available Actuator endpoints.

You can also list the containers and their statuses by using the following command:
```sh
$ docker ps

CONTAINER ID  IMAGE   COMMAND               CREATED            

STATUS              PORTS                   NAMES

075ee30f733f  192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT   "/cnb/process/web"       3 minutes ago       Up 3 minutes        0.0.0.0:8080->8080/tcp  sharp_dijkstra

10cd2f36af1e  registry:2   "/entrypoint.sh /etc…"   6 days ago          Up 3 hours          0.0.0.0:5000->5000/tcp  registry
```
To find out what the available Docker images are, use the following command:
```sh
$ docker images

REPOSITORY                                                 TAG                 IMAGE ID            CREATED             SIZE

paketobuildpacks/run                                       base-cnb            6281947a9e8d        7 days ago          87.7MB

registry                                                   2                   5c4008a25e05        2 weeks ago         26.2MB

paketobuildpacks/builder                                   <none>              56c025ed0e91        41 years ago        665MB

paketobuildpacks/builder                                   base                a4ec710f3cd8        41 years ago        663MB

192.168.80.1:5000/packt-modern-api-development-chapter09   0.0.1-SNAPSHOT 672dd1e3fdb4 41 years ago      307MB
```
Now, you can tag and push the application image using the following commands:

```sh
$ docker tag 192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT 192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT

$ docker push 192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT
```
Similarly, you can also query the local Docker registry container. First, let's run the following command to find all the published images in the registry (the default value is 100):
```sh
$ curl -X GET http://192.168.80.1:5000/v2/_catalog

{"repositories":["packt-modern-api-development-chapter09"]}
```
Similarly, you can find out what all the available tags are for any specific image by using the following command:
```sh
>curl -X GET http://192.168.80.1:5000/v2/packt-modern-api-development-chapter09/tags/list

{"name":"packt-modern-api-development-chapter09","tags":["0.0.1-SNAPSHOT"]}
```

For these commands, you can also use localhost instead of the IP, if you are running a local registry container.

We'll deploy this image on Kubernetes in the next section.


## Deploying an application in Kubernetes

Docker containers are run in isolation. You need a platform that can execute multiple Docker containers and manage or scale them. Docker Compose does this for us. However, this is where Kubernetes helps. It not only manages the container, but also helps you scale the deployed containers dynamically.

You are going to use Minikube to run Kubernetes locally. You can use it on Linux, macOS, and Windows. It runs a single-node Kubernetes cluster, which is used for learning or development purposes. You can install it by referring to the respective guide (https://minikube.sigs.k8s.io/docs/start/).

Once Minikube has been installed, you need to update the local insecure registry in its configuration since, by default, it uses Docker Hub. Adding an image to Docker Hub and then fetching it for local usage is cumbersome for development. You can add a local insecure registry to your Minikube environment by adding your host IP and local Docker registry port to Minikube's config at HostOptions > EngineOptions > InsecureRegistry in ~/.minikube/machines/minikube/config.json (note: this file is only generated after Minikube has been started once; therefore, start Minikube before modifying config.json):
```
$ vi ~/.minikube/machines/minikube/config.json

     "HostOptions": {
35         "Driver": "",
36         "Memory": 0,
37         "Disk": 0,
38         "EngineOptions": {
39             "ArbitraryFlags": null,
40             "Dns": null,
41             "GraphDir": "",
42             "Env": [],
43             "Ipv6": false,
44             "InsecureRegistry": [
45                 "10.96.0.0/12",
46                 "192.168.80.1:5000"
47             ],
```
Once the insecure registry has been updated, you can start Minikube using the following command:
```sh
$ minikube start --insecure-registry="192.168.80.1:5000"
minikube v1.18.1 on Microsoft Windows 10 Pro 10.0.19041 Build 19041
Automatically selected the docker driver. Other choices: virtualbox, none

Starting control plane node minikube in cluster minikube
Pulling base image ...
Downloading Kubernetes v1.20.0 preload ...
   > preloaded-images-k8s-v9-v1....: 491.22 MiB / 491.22 MiB  100.00% 25.00 MiB

Creating docker container (CPUs=2, Memory=4000MB) ...
  Preparing Kubernetes v1.20.2 on Docker 20.10.3 ...

    ▪ Generating certificates and keys ...
    ▪ Booting up control plane ...
    ▪ Configuring RBAC rules ...

Verifying Kubernetes components...

    ▪ Using image gcr.io/k8s-minikube/storage-provisioner:v4
    ▪ Using image kubernetesui/dashboard:v2.1.0
    ▪ Using image kubernetesui/metrics-scraper:v1.0.4

Enabled addons: storage-provisioner, dashboard, default-storageclass
```

Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default

Here, we have used the --insecure-registry flag while starting Minikube. This is important as it makes the insecure registry work. The Kubernetes cluster uses the default namespace by default.

A namespace is a Kubernetes special object that allows you to divide the Kubernetes cluster resources among users or projects. However, you can't have nested namespaces. Kubernetes resources can only belong to single namespaces.

You can check whether Kubernetes is working or not by executing the following command once Minikube is up and running:

```sh
$ kubectl get po -A

NAMESPACE   NAME                            READY STATUS    RESTARTS   AGE

kube-system coredns-74ff55c5b-6nlbn         1/1   Running   0          4m10s

kube-system etcd-minikube                   1/1   Running   0          4m32s

kube-system kube-apiserver-minikube         1/1   Running   0          4m32s

kube-system kube-controller-manager-minikube 1/1  Running   1          4m44s

kube-system kube-proxy-2lsz9                1/1   Running   0          4m10s

kube-system kube-scheduler-minikube         1/1   Running   0          4m31s

kube-system storage-provisioner             1/1   Running   1          4m12s
```

The `kubectl` command is a command-line tool that's used to control a Kubernetes cluster, similar to the docker command for Docker. It is a Kubernetes client that uses Kubernetes REST APIs to perform various Kubernetes operations, such as deploying applications, viewing logs, and inspecting and managing cluster resources.

The get po and get pod parameters allow you to retrieve the pods from your Kubernetes cluster. The -A flag instructs kubectl to retrieve objects from across namespaces. Here, you can see that all the pods are from the kube-system namespace.

These pods are created by Kubernetes and are part of its internal system.

Minikube bundles the Kubernetes dashboard as a user interface for additional insight into your cluster's state. You can start it by using the following command:
```sh
$ minikube dashboard

  Verifying dashboard health ...
  Launching proxy ...
  Verifying proxy health ...
```

Opening http://127.0.0.1:12587/api/v1/namespaces/kubernetes-dashboard/services/http:kubernetes-dashboard:/proxy/ in your default browser...

Running the dashboard will look as follows and allows you to manage the Kubernetes cluster from the UI:

Figure 9.2 – Kubernetes dashboard
Figure 9.2 – Kubernetes dashboard

Kubernetes uses YAML configuration to create objects. For example, you need a deployment and service object to deploy and access the sample ecommerce application. Deployment will create a pod in the Kubernetes cluster that will run the application container, and the service will allow it to access it. You can create these YAML files either manually or generate them using kubectl. You should typically use kubectl, which generates the files for you. If you need to, you can modify the content of the file.

Let's create a new directory (k8s) in the project's home directory so that we can store the Kubernetes deployment configuration. We can generate the deployment Kubernetes configuration file by using the following commands from newly created k8s directory:

```sh
$ kubectl create deployment chapter09 --image=192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT --dry-run=client -o=yaml > deployment.yaml

$ echo --- >> deployment.yaml

$ kubectl create service clusterip chapter09 --tcp=8080:8080 --dry-run=client -o=yaml >> deployment.yaml
```
-The first command generates the deployment configuration in the deployment.yaml file using the create deployment command. A Kubernetes Deployment defines the scale at which you want to run your application. You can see that the replica is defined as 1. Therefore, Kubernetes will run a single replica of this deployment. Here, you pass the name (chapter09) of the deployment, the image name of the application to deploy, the --dry-run=client flag to preview the object that will be sent to the cluster, and the -o=yaml flag to generate the YAML output.

The second command appends --- to the end of the deployment.yaml file.

Finally, the third command creates the service configuration in deployment.yaml with a value of 8080 for both internal and external ports.

Here, you have used the same file for both deployment and service objects. However, you can create two separate files for these – deployment.yaml and service.yaml. In this case, you need to apply these objects separately in your Kubernetes cluster.

Let's have a look at the content of the deployment.yaml file, which was generated by the previous code block:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  creationTimestamp: null
  labels:
    app: chapter09
  name: chapter09
spec:
  replicas: 1
  selector:
    matchLabels:
      app: chapter09
  strategy: {}
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: chapter09
    spec:
      containers:
      - image: 192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT
        name: packt-modern-api-development-chapter09
        resources: {}
status: {}
---
apiVersion: v1
kind: Service
metadata:
  creationTimestamp: null
  labels:
    app: chapter09
  name: chapter09
spec:
  ports:
  - name: 8080-8080
    port: 8080
    protocol: TCP
    targetPort: 8080

  selector:
    app: chapter09
  type: ClusterIP
status:

  loadBalancer: {}
```

https://github.com/PacktPublishing/Modern-API-Development-with-Spring-and-Spring-Boot/blob/main/Chapter09/k8s/deployment.yaml

Now, you can deploy the sample ecommerce application using the deployment.yaml file, as shown in the following code block:
```
$ kubectl apply -f deployment.yaml

deployment.apps/chapter09 created

service/chapter09 created
```
Alternatively, you can perform the following steps to publish the Docker image to Minikube. Start a new Terminal and execute the following commands (the same Terminal window should be used here since the eval commands are only valid in an active Terminal):

- Execute eval $(minikube docker-env) to align the Minikube environment with your Docker configuration.

- Execute gradle bootBuildImage to generate an image based on the Minikube environment.

- Execute the following commands:

  i. docker tag 192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT 192.168.80.1:5000/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT

  ii. docker push 172.26.208.1192.168.80.1:5000/library/packt-modern-api-development-chapter09:0.0.1-SNAPSHOT

- Execute minikube stop and minikube start to ensure that the new configuration is applied.

You can start the Minikube logs by using the following commands:

i. minikube -p minikube docker-env

ii. eval $(minikube -p minikube docker-env)

Afterward, deploying with the kubectl apply -f deploymentTest.yaml command should work.

This will initiate the application deployment of chapter09. You can then either use the Kubernetes dashboard or the kubectl get all command to check the status of your pod and service. Pods are Kubernetes' smallest and most deployable objects. They contain one or more containers and represent a single instance of a running process in a Kubernetes cluster. A pod's IP address and other configuration details may change because Kubernetes keep track of these and may replace them if a pod goes down. Therefore, Kubernetes Service adds an abstraction layer over the pods it exposes the IP addresses of and manages mapping to internal pods.

Let's run the following command to find out the status of the pod and service:
```sh
$ kubectl get all

NAME READY   STATUS    RESTARTS   AGE
pod/chapter09-7788955cf7-rqrn6   1/1     Running   0          73s

NAME TYPE CLUSTER-IP EXTERNAL-IP PORT(S)    AGE
service/chapter09    ClusterIP   10.110.239.183   <none> 8080/TCP   75
service/kubernetes   ClusterIP   10.96.0.1 <none> 443/TCP    167m

NAME READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/chapter09   1/1     1            1           79s

NAME DESIRED    CURRENT   READY   AGE
replicaset.apps/chapter09-7788955cf7   1         1         1       77s
```
This returns all the Kubernetes resources in the default namespace. Here, you can see that it returns a running pod, a service, a deployment resource, and a Replica Set for chapter09. You need to run this command multiple times until you find a successful or erroneous response (such as "image is not pullable").

You can't access the application running inside Kubernetes directly. You must either use some kind of proxy or SSH tunneling. Let's quickly create a SSH tunnel using the following command:
```sh
$ kubectl port-forward service/chapter09 8080:8080

Forwarding from 127.0.0.1:8080 -> 8080
Forwarding from [::1]:8080 -> 8080
```
The application is running on port 8080 internally. It is mapped to the local machine's port, which is 8080. Now, the application can be accessed on port 8080 outside the Kubernetes cluster.

We can access it by using the following command after opening a new Terminal window:

```sh
$ curl localhost:8080/actuator/health
{"status":"UP","groups":["liveness","readiness"]}
```

With that, the application has been successfully deployed on our Kubernetes cluster. Now, you can use the Postman collection and run all the available REST endpoints.

## Summary

In this chapter, you learned about containerization and how it is different from virtualization. You also learned about the Docker containerization platform and how to use the Spring Boot plugin to generate a Docker image for a sample ecommerce app.

Then, you learned about the Docker registry and how to configure a local insecure registry so that you can use it to push and pull images.

You also learned about Kubernetes and its cluster operations by using Minikube. You configured it so that you can pull Docker images from insecure local Docker registries.

Now, you have the necessary skills to build a Docker image of a Spring Boot application and deploy it on a Kubernetes cluster.

In the next chapter, you'll learn about the fundamentals of the gRPC APIs.

## Questions

1. What is the difference between virtualization and containerization?

2. What is Kubernetes used for?

3. What is kubectl?

## Further reading

- Kubernetes and Docker – an Enterprise Guide:
https://www.packtpub.com/product/kubernetes-and-docker-an-enterprise-guide/9781839213403

- Docker documentation:
https://docs.docker.com/get-started/overview/

- Minikube documentation:
https://minikube.sigs.k8s.io/docs/start/
