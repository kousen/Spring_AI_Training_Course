plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.oreilly"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "1.0.0-M8"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring AI models
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springframework.ai:spring-ai-starter-model-anthropic")

    // Vector Stores
    implementation("org.springframework.ai:spring-ai-advisors-vector-store")
    implementation("org.springframework.ai:spring-ai-starter-vector-store-redis")

    // Document Readers
    implementation("org.springframework.ai:spring-ai-jsoup-document-reader")
    implementation("org.springframework.ai:spring-ai-pdf-document-reader")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs = listOf("-Xshare:off", "-XX:+EnableDynamicAgentLoading")
}
