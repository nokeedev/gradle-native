package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import org.gradle.util.GradleVersion

trait JavaApplicationFixture {
	String configureJavaApplicationMainClassName(String mainClassName) {
		def gradleVersionValue = System.getProperty(AbstractGradleSpecification.DEFAULT_GRADLE_VERSION_SYSPROP_NAME)
		if (gradleVersionValue != null && GradleVersion.version(gradleVersionValue) > GradleVersion.version("6.6")) {
			return """
				application {
					mainClass = '${mainClassName}'
				}
			"""
		}
		return """
			application {
				mainClassName = '${mainClassName}'
			}
		"""
	}
}
