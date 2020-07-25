package dev.nokee.utils

import org.gradle.api.invocation.Gradle
import spock.lang.Specification
import spock.lang.Subject

@Subject(GradleUtils)
class GradleUtilsTest extends Specification {
	def "can detect root project"() {
		given:
		def hostBuild = Mock(Gradle) {
			getParent() >> null
		}
		def includedBuild = Mock(Gradle) {
			getParent() >> hostBuild
		}

		expect:
		GradleUtils.isHostBuild(hostBuild)
		!GradleUtils.isHostBuild(includedBuild)
	}
}
