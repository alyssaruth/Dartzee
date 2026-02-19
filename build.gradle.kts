import java.net.URI
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.0"
    id("java-library")
    id("com.github.ben-manes.versions") version "0.44.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jetbrains.kotlinx.kover") version "0.9.7"
    id("com.ncorti.ktfmt.gradle") version "0.25.0"
}

repositories {
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
}

apply(plugin = "kotlin")

apply(plugin = "application")

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/detekt-config.yml")
}

ktfmt { kotlinLangStyle() }

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.miglayout:miglayout-swing:11.0")
    implementation("org.jfree:jfreechart:1.5.4")
    implementation("com.konghq:unirest-java:3.14.2")
    implementation("com.github.lgooddatepicker:LGoodDatePicker:11.2.1")
    implementation("org.apache.derby:derby:10.17.1.0")
    implementation("org.apache.derby:derbytools:10.17.1.0")
    implementation("com.amazonaws:aws-java-sdk-elasticsearch:1.12.396")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.396")
    implementation("com.github.awslabs:aws-request-signing-apache-interceptor:b3772780da")
    implementation("org.elasticsearch.client:elasticsearch-rest-client:8.6.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("net.lingala.zip4j:zip4j:2.11.3")

    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("io.kotest:kotest-assertions-core:5.5.4")
    testImplementation("com.github.alexburlton:swing-test:4.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)

        java {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }
}

tasks.register("runDev", JavaExec::class) {
    configure(
        closureOf<JavaExec> {
            group = "run"
            classpath = project.the<SourceSetContainer>()["main"].runtimeClasspath
            args = listOf("devMode", "trueLaunch")
            mainClass.set("dartzee.main.DartsMainKt")
        }
    )
}

kover {
    currentProject {
        instrumentation {
            disabledForTestTasks.add("test")
            disabledForTestTasks.add("updateScreenshots")
            disabledForTestTasks.add("integrationAndE2E")
        }
    }

    reports { filters { excludes { classes("dartzee.screen.TestWindow") } } }
}

val test by testing.suites.existing(JvmTestSuite::class)
val derviedTestClassesDirs = files(test.map { it.sources.output.classesDirs })
val derivedTestClasspath = files(test.map { it.sources.runtimeClasspath })

tasks.register("unitTest", Test::class) {
    group = "verification"
    useJUnitPlatform { excludeTags = setOf("integration", "e2e") }

    testClassesDirs = derviedTestClassesDirs
    classpath = derivedTestClasspath
}

tasks.register("updateScreenshots", Test::class) {
    group = "verification"
    useJUnitPlatform { includeTags = setOf("screenshot") }

    jvmArgs = listOf("-DupdateSnapshots=true")

    testClassesDirs = derviedTestClassesDirs
    classpath = derivedTestClasspath
}

tasks.register("integrationAndE2E", Test::class) {
    group = "verification"
    useJUnitPlatform { includeTags = setOf("integration", "e2e") }

    testClassesDirs = derviedTestClassesDirs
    classpath = derivedTestClasspath
}

tasks { named<Test>("test") { useJUnitPlatform() } }

tasks.withType<Test> {
    minHeapSize = "1024m"
    maxHeapSize = "1024m"

    jvmArgs(
        "-Dcom.sun.management.jmxremote",
        "-Dcom.sun.management.jmxremote.port=9010",
        "-Dcom.sun.management.jmxremote.authenticate=false",
        "-Dcom.sun.management.jmxremote.ssl=false",
        "-Djava.rmi.server.hostname=localhost",
        "--add-opens",
        "java.desktop/sun.awt=ALL-UNNAMED",
        "--add-opens",
        "java.desktop/java.awt=ALL-UNNAMED",
    )

    testLogging {
        events = mutableSetOf(TestLogEvent.STARTED, TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<Jar> {
    manifest { attributes["Main-Class"] = "dartzee.main.DartsMainKt" }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    dependsOn(configurations.runtimeClasspath)
    from(
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    )
}
