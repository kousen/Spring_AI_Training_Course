plugins {
    java
    id("org.springframework.boot") version "4.1.0"
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

extra["springAiVersion"] = "2.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // Spring AI models
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")
    implementation("org.springframework.ai:spring-ai-starter-model-elevenlabs")

    // Advisors
    implementation("org.springframework.ai:spring-ai-vector-store-advisor")
    // Not managed by the Spring AI BOM; version pinned explicitly
    implementation("org.springframework.ai:spring-ai-starter-tool-search-advisor:2.0.0")
    implementation("org.springframework.ai:spring-ai-starter-vector-store-redis")

    // Document Readers
    implementation("org.springframework.ai:spring-ai-jsoup-document-reader")
    implementation("org.springframework.ai:spring-ai-pdf-document-reader")

    // MCP (Model Context Protocol) Support
    implementation("org.springframework.ai:spring-ai-starter-mcp-client")
    implementation("org.springframework.ai:spring-ai-starter-mcp-server")
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // Exclude the outdated android-json to avoid conflict with the newer org.json:json
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation("io.projectreactor:reactor-test")
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
