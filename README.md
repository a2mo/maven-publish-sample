<div id="top"></div>
<br />
<div align="center">

<h3 align="center">Maven Publish Sample</h3>

  <p align="center">
    sample for publishing artifact to maven repository
    <br />
  </p>
</div>


<!-- ABOUT THE PROJECT -->

## About The Project

This project try to publish sample artifact to maven repository.
<br/>

## Getting Started

* For start, you should create jira account, follow [this link](https://issues.sonatype.org/secure/Signup!default.jspa) and fill out the form if you don't already own a sonatype user.
* Then use your sonatype user and create a Jira issue with this [link](https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134).
After some time you will get feedback through the issue, so remember to enable notifications to your email.

 ```xml
  <distributionManagement>
       <snapshotRepository>
           <id>ossrh</id>
           <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
       </snapshotRepository>
       <repository>
           <id>ossrh</id>
           <name>maven central</name>
           <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
       </repository>
  </distributionManagement>
  ```
  
## Add necessary plugins
You will need to attach sources and javadoc with your jars. In order to do so, include the following plugin configurations in the pom. This will reduce the rejection causes when trying to release.

 ```xml
<!-- Attach source jars-->
  <plugin>
    <artifactId>maven-source-plugin</artifactId>
    <version>${version.plugin.source}</version>
    <executions>
      <execution>
        <id>attach-source</id>
        <phase>compile</phase>
        <goals>
          <goal>jar-no-fork</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
  ```
  
  ```xml
<!-- Attach javadocs jar -->
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>${version.plugin.javadoc}</version>
    <executions>
      <execution>
        <id>attach-javadocs</id>
        <goals>
          <goal>jar</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
  ```
  
  ```xml
  <plugin>
      <groupId>org.sonatype.plugins</groupId>
      <artifactId>nexus-staging-maven-plugin</artifactId>
      <version>1.6.12</version>
      <extensions>true</extensions>
      <configuration>
          <serverId>ossrh</serverId>
          <nexusUrl>https://s01.oss.sonatype.org/</nexusUrl>
          <autoReleaseAfterClose>true</autoReleaseAfterClose>
      </configuration>
  </plugin>
```

## Sign your artifacts

```xml
 <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-gpg-plugin</artifactId>
    <version>3.0.1</version>
    <executions>
        <execution>
            <id>sign-artifacts</id>
            <phase>verify</phase>
            <goals>
                <goal>sign</goal>
            </goals>
            <configuration>
                <gpgArguments>
                    <arg>--pinentry-mode</arg>
                    <arg>loopback</arg>
                </gpgArguments>
            </configuration>
        </execution>
    </executions>
 </plugin>
```

### Generate gpg key
If you don't already have it, you'll need to install gpg and generate a key to make sure your sonatype user is the only one allowed to upload artifacts with your groupId.
Follow [this page](https://central.sonatype.org/pages/working-with-pgp-signatures.html) to generate your key.
<br/>
These are the important command:

* To generate key pair.

```console
gpg --full-generate-key
```

* To get list of key pairs.

```console
gpg --list-keys
```

* To get private key of key pair.

```console
gpg --export-secret-keys --armor {key_id}
```

Download gpg4win with [this link](https://gpg4win.org/download.html).

## Create github action
You only need a GitHub repository to create and run a GitHub Actions workflow. In this guide, you'll add a workflow that demonstrates some of the essential features of GitHub Actions.Follow [this page](https://docs.github.com/en/actions/quickstart) to do it.

`build.xml`
```
# This workflow will build a Java project with Maven, and cache/restore any dependencies to improve the workflow execution time
# For more information see: https://help.github.com/actions/language-and-framework-guides/building-and-testing-java-with-maven

name: Build and deploy per push

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:

    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: 11
        distribution: temurin
        cache: maven
        server-id: ossrh
        server-username: MAVEN_USERNAME
        server-password: MAVEN_PASSWORD
        gpg-private-key: ${{ secrets.OSSRH_GPG_SECRET_KEY }}
        gpg-passphrase: MAVEN_GPG_PASSPHRASE
    - name: Configure Git user
      run: |
          git config user.email "actions@github.com"
          git config user.name "GitHub Actions"
    - name: Build with Maven
      run: mvn -B package --file pom.xml
    - name: Publish to the Maven Central Repository
      run: |
          mvn \
            --no-transfer-progress \
            --batch-mode \
            deploy -Dgpg.passphrase=${{ secrets.OSSRH_GPG_SECRET_KEY_PASSWORD }} -Pbuild
      env:
          MAVEN_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          MAVEN_PASSWORD: ${{ secrets.OSSRH_TOKEN }}
          MAVEN_GPG_PASSPHRASE: ${{ secrets.OSSRH_GPG_SECRET_KEY_PASSWORD }}
```

`release.xml`
```
name: Release project to Maven central

on:
  workflow_dispatch:
    inputs:
      releaseVersion:
        description: "Default version to use when preparing a release."
        required: true
        default: "X.Y.Z"
      developmentVersion:
        description: "Default version to use for new local working copy."
        required: true
        default: "X.Y.Z-SNAPSHOT"

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout project
        uses: actions/checkout@v3
      - name: Setup Java JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: 11
          distribution: temurin
          cache: maven
          server-id: github
      - name: Configure Git user
        run: |
          git config user.email "actions@github.com"
          git config user.name "GitHub Actions"
      - name: Publish JAR
        run: mvn -B release:clean release:prepare release:perform -DreleaseVersion=${{ github.event.inputs.releaseVersion }} -DdevelopmentVersion=${{ github.event.inputs.developmentVersion }}
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  deploy:
    needs: release
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
      with:
        ref: v${{ github.event.inputs.releaseVersion }}
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: 11
        distribution: temurin
        cache: maven
        server-id: ossrh
        server-username: MAVEN_USERNAME
        server-password: MAVEN_PASSWORD
        gpg-private-key: ${{ secrets.OSSRH_GPG_SECRET_KEY }}
        gpg-passphrase: ${{ secrets.OSSRH_GPG_SECRET_KEY_PASSWORD }}
    - name: Configure Git user
      run: |
          git config user.email "actions@github.com"
          git config user.name "GitHub Actions"
    - name: Release and publish to the Maven Central Repository
      run: |
          mvn \
            --no-transfer-progress \
            --batch-mode \
            deploy -Dgpg.passphrase=${{ secrets.OSSRH_GPG_SECRET_KEY_PASSWORD }} -Pbuild
      env:
          MAVEN_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          MAVEN_PASSWORD: ${{ secrets.OSSRH_TOKEN }}
          MAVEN_GPG_PASSPHRASE: ${{ secrets.OSSRH_GPG_SECRET_KEY_PASSWORD }}
```

## Set secret in repository setting
Secrets are encrypted environment variables that you create in an organization, repository, or repository environment. The secrets that you create are available to use in GitHub Actions workflows.Follow [this page](https://docs.github.com/en/actions/security-guides/encrypted-secrets) to do it.
<br/>
Set follow secret key:
`OSSRH_GPG_SECRET_KEY`,`OSSRH_GPG_SECRET_KEY_PASSWORD`,`OSSRH_TOKEN`,`OSSRH_USERNAME`
<br/>
This keys be used in `build.xml` and `release.xml` file for github action. 

<br/>
<p align="right">(<a href="#top">back to top</a>)</p>

### Prerequisites

This Library requires java version 11 or above.
