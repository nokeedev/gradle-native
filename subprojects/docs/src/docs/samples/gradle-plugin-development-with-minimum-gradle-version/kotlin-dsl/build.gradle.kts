plugins {
	id("dev.gradleplugins.java-gradle-plugin") version("0.0.82")
	id("groovy-base")
}

gradlePlugin {
	compatibility {
		minimumGradleVersion.set("4.9")
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
	testImplementation(platform("org.spockframework:spock-bom:1.2-groovy-2.5"))
	testImplementation("org.spockframework:spock-core")
	testImplementation(gradleTestKit())
}
