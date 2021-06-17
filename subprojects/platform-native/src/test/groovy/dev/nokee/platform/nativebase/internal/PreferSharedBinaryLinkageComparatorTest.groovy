package dev.nokee.platform.nativebase.internal

import dev.nokee.runtime.nativebase.internal.DefaultBinaryLinkage
import spock.lang.Specification
import spock.lang.Subject

@Subject(PreferSharedBinaryLinkageComparator)
class PreferSharedBinaryLinkageComparatorTest extends Specification {
	def subject = new PreferSharedBinaryLinkageComparator()

	def "always prefer shared binary linkage"() {
		expect:
		subject.compare(shared, getStatic()) == -1
		subject.compare(getStatic(), shared) == 1
	}

	def "no opinion on different binary linkage that is not shared"() {
		expect:
		subject.compare(getStatic(), notSharedAndStatic) == 0
	}

	def "no opinion same build type"() {
		expect:
		subject.compare(shared, shared) == 0
		subject.compare(getStatic(), getStatic()) == 0
	}

	private DefaultBinaryLinkage getShared() {
		return DefaultBinaryLinkage.SHARED
	}

	private DefaultBinaryLinkage getStatic() {
		return DefaultBinaryLinkage.STATIC
	}

	private DefaultBinaryLinkage getNotSharedAndStatic() {
		return new DefaultBinaryLinkage('some-linkage')
	}
}
