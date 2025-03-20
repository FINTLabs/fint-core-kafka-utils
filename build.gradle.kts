plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("java-library")
	id("io.spring.dependency-management") version "1.1.7"
	`maven-publish`
}

group = "no.fintlabs"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	maven("https://repo.fintlabs.no/releases")
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter:3.4.3")
	implementation("org.springframework.kafka:spring-kafka:3.4.3")
	implementation("no.fintlabs:fint-kafka:3.2.0-rc-1")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.mockito:mockito-core:5.5.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


kotlin {
	compilerOptions {
		freeCompilerArgs.add("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

publishing {
	repositories {
		maven {
			url = uri("https://repo.fintlabs.no/releases")
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
