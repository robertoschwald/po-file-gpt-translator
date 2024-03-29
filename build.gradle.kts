import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  war
  id("org.springframework.boot") version "3.0.5"
  id("io.spring.dependency-management") version "1.1.0"
  kotlin("jvm") version "1.7.22"
  kotlin("plugin.spring") version "1.7.22"
}

group = "de.oschwald"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
  testImplementation("org.springframework.boot:spring-boot-starter-test")

  // Retry
  implementation("org.springframework.retry:spring-retry")
  implementation("org.springframework.boot:spring-boot-starter-aop")

  // HTTP
  implementation("com.squareup.okhttp3:okhttp:4.9.1")
  implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
  implementation("org.json:json:20230227")
  implementation("org.fedorahosted.tennera:jgettext:0.15.1")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "17"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
