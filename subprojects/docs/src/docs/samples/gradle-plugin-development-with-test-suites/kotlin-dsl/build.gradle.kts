plugins {
	id("dev.gradleplugins.java-gradle-plugin") version("1.1")
	id("dev.gradleplugins.gradle-plugin-unit-test") version("1.1")
	id("dev.gradleplugins.gradle-plugin-functional-test") version("1.1")
}

gradlePlugin {
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

test {
	dependencies {
		implementation(spockFramework())
		implementation(groovy())
	}
}

functionalTest {
	dependencies {
		implementation(spockFramework())
		implementation(groovy())
		implementation(gradleTestKit())
	}
}
