plugins {
	id("dev.gradleplugins.java-gradle-plugin") version("1.4.2")
	id("groovy-base") // for Spock testing
}

gradlePlugin {
	compatibility {
		minimumGradleVersion.set("5.1")
	}

	testSourceSets(sourceSets.test.get())
	plugins {
		create("helloWorld") {
			id = "com.example.hello"
			implementationClass = "com.example.BasicPlugin"
		}
	}
}

repositories {
	mavenCentral()
}

dependencies { // <1>
	testImplementation(platform("org.spockframework:spock-bom:2.0-groovy-2.5"))
	testImplementation("org.spockframework:spock-core")
	testImplementation(gradleTestKit())
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
