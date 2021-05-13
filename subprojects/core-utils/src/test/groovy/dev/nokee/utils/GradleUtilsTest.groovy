package dev.nokee.utils

import org.gradle.api.initialization.IncludedBuild
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

	def "can detect included builds"() {
		given:
		def buildWithIncludedBuilds = Mock(Gradle) {
			getIncludedBuilds() >> [Mock(IncludedBuild), Mock(IncludedBuild)]
		}
		def buildWithoutIncludedBuilds = Mock(Gradle) {
			getIncludedBuilds() >> []
		}

		expect:
		GradleUtils.hasIncludedBuilds(buildWithIncludedBuilds)
		!GradleUtils.hasIncludedBuilds(buildWithoutIncludedBuilds)
	}

	def "can detect when a build is included inside another one"() {
		given:
		def root = Mock(Gradle) {
			getParent() >> null
		}
		def child = Mock(Gradle) {
			getParent() >> root
		}

		expect:
		!GradleUtils.isIncludedBuild(root)
		GradleUtils.isIncludedBuild(child)
	}

	def "can detect when a build is a composite build"() {
		given:
		def root = Mock(Gradle) {
			getParent() >> null
		}
		def child = Mock(Gradle) {
			getParent() >> root
		}

		and:
		root.getIncludedBuilds() >> [child]
		child.getIncludedBuilds() >> []

		expect:
		GradleUtils.isCompositeBuild(root)
		GradleUtils.isCompositeBuild(child)
	}

	def "can detect when a build is a not a composite build"() {
		given:
		def build = Mock(Gradle) {
			getParent() >> null
			getIncludedBuilds() >> []
		}

		expect:
		!GradleUtils.isCompositeBuild(build)
	}
}
