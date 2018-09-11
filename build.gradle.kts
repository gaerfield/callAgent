import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.2.51"
    id("org.springframework.boot") version "2.0.3.RELEASE"
    id ("org.jetbrains.kotlin.kapt") version "1.2.51"
    java
}

group = "de.kramhal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    compile("org.jetbrains.kotlin:kotlin-reflect:1.2.51")
    compile("io.github.microutils:kotlin-logging:1.5.4")

    compile("org.springframework.boot:spring-boot-starter-web:2.0.3.RELEASE")
    compile("org.springframework.boot:spring-boot-devtools:2.0.3.RELEASE")
    compile("org.springframework.boot:spring-boot-starter-thymeleaf:2.0.3.RELEASE")
    compile("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.3.0")
    // compile("org.springframework.boot:spring-boot-starter-security:2.0.3.RELEASE")
    //compile("org.springframework:spring-web:5.0.6.RELEASE")

    compile("com.squareup.moshi:moshi-kotlin:1.6.0")
    // kotlin annotation processing
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.6.0")

    compile("com.squareup.retrofit2:retrofit:2.4.0")
    compile("com.squareup.retrofit2:converter-moshi:2.4.0")
    compile("com.squareup.okhttp3:logging-interceptor:3.8.0")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.6")

    compile("org.webjars:webjars-locator:0.34")
    compile("org.webjars:bootstrap:4.1.3")
    compile("org.webjars:bootstrap-glyphicons:bdd2cbfba0")

    runtime("javax.xml.bind:jaxb-api:2.3.0")
    //    runtime("com.sun.xml.bind:jaxb-core:2.2.11")
    //    runtime("com.sun.xml.bind:jaxb-impl:2.2.11")
    //    runtime("javax.activation:activation:1.1.1")

    // unit-tests
//    testImplementation("org.springframework.boot:spring-boot-starter-test:2.0.3.RELEASE") {
//        exclude(module = "junit")
//    }
    testCompile("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testCompile("org.junit.jupiter:junit-jupiter-params:5.2.0")
    testCompile("io.mockk:mockk:1.8.5")
    //    testRuntime("org.junit.jupiter:junit-platform-launcher:1.2.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

springBoot {
    mainClassName = "de.kramhal.callagent.CallAgentKt"
}

tasks {
    getByName<BootJar>("bootJar") {
        archiveName = "callagent.jar"
        version = "0.0.1"
        launchScript() {
            properties["inlinedConfScript"] = "src/main/resources/javaOpts.conf"
        }
    }
    getByName<Test>("test") {
        useJUnitPlatform()
    }
//    getByName<JavaExec>("run") {
//        standardInput = System.`in`
//    }
}
