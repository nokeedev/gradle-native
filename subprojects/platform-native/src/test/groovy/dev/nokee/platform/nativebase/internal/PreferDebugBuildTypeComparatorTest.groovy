package dev.nokee.platform.nativebase.internal

import dev.nokee.runtime.nativebase.internal.BaseTargetBuildType
import dev.nokee.runtime.nativebase.internal.NamedTargetBuildType
import spock.lang.Specification
import spock.lang.Subject

@Subject(PreferDebugBuildTypeComparator)
class PreferDebugBuildTypeComparatorTest extends Specification {
	def subject = new PreferDebugBuildTypeComparator()

	def "always prefer debug build type"() {
		expect:
		subject.compare(debug, release) == -1
		subject.compare(release, debug) == 1
	}

	def "no opinion on different build types that is not debug"() {
		expect:
		subject.compare(release, notDebugOrRelease) == 0
	}

	def "no opinion on same build type"() {
		expect:
		subject.compare(debug, debug) == 0
		subject.compare(release, release) == 0
	}

	def "disregards the case of the debug build type"() {
		expect:
		subject.compare(buildTypeOf('debug'), buildTypeOf('Debug')) == 0
		subject.compare(buildTypeOf('dEbUg'), release) == -1
		subject.compare(release, buildTypeOf('DeBuG')) == 1
	}

	private BaseTargetBuildType getDebug() {
		return new NamedTargetBuildType('debug')
	}

	private BaseTargetBuildType getRelease() {
		return new NamedTargetBuildType('release')
	}

	private BaseTargetBuildType getNotDebugOrRelease() {
		return new NamedTargetBuildType('final')
	}

	private BaseTargetBuildType buildTypeOf(String name) {
		return new NamedTargetBuildType(name)
	}
}
