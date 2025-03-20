plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("java-library")
	id("io.spring.dependency-management") version "1.1.7"
	`maven-publish`
}

group = "no.fintlabs"
version = "0.0.1-SNAPSHOT"
val reposolite = "https://repo.fintlabs.no/releases"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	maven(reposolite)
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("no.fintlabs:fint-kafka:3.2.0-rc-1")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

publishing {
	repositories {
		maven {
			url = uri(reposolite)
			credentials {
				username = System.getenv("REPOSILITE_USERNAME")
				password = System.getenv("REPOSILITE_PASSWORD")
			}
			authentication {
				create<BasicAuthentication>("basic")
			}
		}
	}
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])
		}
	}
}
