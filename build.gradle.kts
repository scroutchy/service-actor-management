plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "6.0.1.5171"
    id("jacoco")
    id("com.epages.restdocs-api-spec") version "0.19.4"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    `maven-publish`
}

group = "com.scr.project"
fun getGitTag(): String {
    return try {
        val tag = ProcessBuilder("git", "describe", "--tags", "--abbrev=0").start().run {
            inputStream.bufferedReader().readText().trim().takeIf { waitFor() == 0 && it.isNotEmpty() }
        }
        tag ?: System.getenv("CI_COMMIT_REF_SLUG") ?: "0.0.1-SNAPSHOT"
    } catch (e: Exception) {
        println("Error getting git information: ${e.message}")
        "0.0.1-SNAPSHOT"
    }
}
version = getGitTag()
private val jakartaValidationVersion = "3.0.2"
private val kMongoVersion = "4.10.0"
private val mockkVersion = "1.12.0"
private val commonsCinemaVersion = "2.1.2"
private val testcontainersKeycloackVersion = "3.6.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    maven("https://gitlab.com/api/v4/projects/67204824/packages/maven")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("io.projectreactor.kafka:reactor-kafka")
    implementation("org.apache.avro:avro:1.12.0")
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationVersion")
    implementation("com.scr.project.commons.cinema:commons-cinema:$commonsCinemaVersion")
    implementation("io.confluent:kafka-avro-serializer:7.9.0")
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

tasks.register<Jar>("copyAvroSchemas") {
    group = "build"
    description = "Packages Avro schema files into a publishable JAR with 'schemas' classifier"
//    from("src/main/avro")
//    into("build/schema-libs")
//    include("**/*.avsc")
    archiveClassifier.set("schemas") // Définit le classifier 'schemas'
    archiveBaseName.set("service-actor-management") // Nom de base (souvent nom du projet)
//    archiveVersion.set(project.version) // Version (définie par getGitTag())
    archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-${archiveClassifier.get()}.jar") // Nom complet du fichier
    destinationDirectory.set(layout.buildDirectory.dir("libs")) // build/libs
    from("src/main/avro") { /* ... */ }
    include("**/*.avsc")
}
//artifacts {
//    archives(tasks["copyAvroSchemas"]) {
//        classifier = "schemas"
//    }
//}


publishing {
    publications {
        create<MavenPublication>("avroSchemas") {
//            from(components["java"])
//            artifact(tasks["copyAvroSchemas"]) {
//                classifier = "schemas"
//            }
            artifact(tasks.named<Jar>("bootJar"))
            artifact(tasks.named<Jar>("copyAvroSchemas"))
        }
    }

    repositories {
        maven {
            url = uri("${System.getenv("CI_API_V4_URL")}/projects/${System.getenv("CI_PROJECT_ID")}/packages/maven")
            credentials(HttpHeaderCredentials::class.java) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }
            authentication { create("header", HttpHeaderAuthentication::class.java) }
        }
    }
}

tasks.register("publishToGitLab") {
    group = "publishing"
    description = "Publish the project to GitLab Maven repository"
//    dependsOn("generateAvroJava") // Générer les classes avant publication
//    dependsOn("copyAvroSchemas")  // Copier les schémas avant publication
//    dependsOn("publish")          // Lancer la publication
    dependsOn("publishMavenJavaPublicationToMavenRepository")
}


afterEvaluate {
    tasks.findByName("openapi3")?.finalizedBy(tasks.register<Copy>("copyApiSpecToDocs") {
        from("build/api-spec")
        into("docs")
    })
}
