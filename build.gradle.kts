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

val reposolite = "https://repo.fintlabs.no/releases"
val springVersion = "3.4.3"

repositories {
	maven(reposolite)
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux:$springVersion")
	implementation("org.springframework.boot:spring-boot-starter:$springVersion")
	implementation("org.springframework.kafka:spring-kafka:3.3.4")
	implementation("no.fintlabs:fint-kafka:3.2.0-rc-1")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
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
