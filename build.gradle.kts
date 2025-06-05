import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask

plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    id("org.springframework.boot") version "3.5.0"
	id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "6.0.1.5171"
	id("jacoco")
    id("com.epages.restdocs-api-spec") version "0.19.4"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

group = "com.scr.project"
version = "0.0.1-SNAPSHOT"
private val jakartaValidationVersion: String by project
private val kMongoVersion: String by project
private val mockkVersion: String by project
private val commonsCinemaVersion: String by project
private val testcontainersKeycloackVersion: String by project

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
    maven("https://gitlab.com/api/v4/projects/67204824/packages/maven")
    maven("https://gitlab.com/api/v4/projects/69406479/packages/maven")
    maven("https://packages.confluent.io/maven/")
}

configurations {
    create("avroSchemas")
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.kafka:spring-kafka")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("io.projectreactor.kafka:reactor-kafka")
    implementation("org.apache.avro:avro:1.12.0")
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationVersion")
    implementation("com.scr.project.commons.cinema:commons-cinema:$commonsCinemaVersion")
    implementation("com.scr.project.commons.cinema:commons-cinema-outbox:${commonsCinemaVersion}")
    implementation("com.scr.project.commons.cinema:commons-cinema-kafka:${commonsCinemaVersion}")
    implementation("io.confluent:kafka-avro-serializer:7.9.0")
    add("avroSchemas", "org.scr.project:service-rewarded-management:0.1.1:schemas")
    testImplementation("com.scr.project.commons.cinema.test:commons-cinema-test:$commonsCinemaVersion")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.testcontainers:testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.litote.kmongo:kmongo:$kMongoVersion")
    testImplementation("com.github.dasniko:testcontainers-keycloak:$testcontainersKeycloackVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")
    testImplementation("org.springframework.restdocs:spring-restdocs-asciidoctor")
    testImplementation("com.epages:restdocs-api-spec:0.19.4") {
        exclude(
            group = "org.springframework.boot",
            module = "spring-boot-starter-web"
        )
    }
    testImplementation("com.epages:restdocs-api-spec-webtestclient:0.19.4") {
        exclude(
            group = "org.springframework.boot",
            module = "spring-boot-starter-web"
        )
    }
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
val extractAvroSchemas by tasks.registering(Copy::class) {
    from(configurations["avroSchemas"].map { zipTree(it) })
    include("**/*.avsc")
    into(layout.buildDirectory.dir("schemas-libs"))
}

tasks.named<GenerateAvroJavaTask>("generateAvroJava") {
    dependsOn(extractAvroSchemas)
    source(layout.buildDirectory.dir("schemas-libs"))
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}



tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.register("printCoverage") {
    group = "verification"
    description = "Prints the code coverage of the project"
    dependsOn(tasks.jacocoTestReport)
    doLast {
        val reportFile = layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile
        if (reportFile.exists()) {
            val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(reportFile)
            val counters = document.getElementsByTagName("counter")
            var covered = 0
            var missed = 0
            for (i in 0 until counters.length) {
                val counter = counters.item(i) as org.w3c.dom.Element
                covered += counter.getAttribute("covered").toInt()
                missed += counter.getAttribute("missed").toInt()
            }
            val totalCoverage = (covered * 100.0) / (covered + missed)
            println("Total Code Coverage: %.2f%%".format(totalCoverage))
        } else {
            println("JaCoCo report file not found!")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport", tasks.named("printCoverage"))
}

sonar {
    properties {
        property("sonar.projectKey", "cinema7590904_service-actor-management")
        property("sonar.organization", "cinema7590904")
    }
}

openapi3 {
    title = "service-actor-management"
    description = "This application aims to manage the actors and their main characteristics"
    format = "yaml"
}

afterEvaluate {
    tasks.findByName("openapi3")?.finalizedBy(tasks.register<Copy>("copyApiSpecToDocs") {
        from("build/api-spec")
        into("docs")
    })
}
