package dev.nokee.platform.nativebase.internal

import dev.nokee.runtime.nativebase.OperatingSystemFamily
import spock.lang.Specification
import spock.lang.Subject

@Subject(PreferHostOperatingSystemFamilyComparator)
class PreferHostOperatingSystemFamilyComparatorTest extends Specification {
	def subject = new PreferHostOperatingSystemFamilyComparator()

	def "always prefer host operating system family"() {
		expect:
		subject.compare(host, notHost) == -1
		subject.compare(notHost, host) == 1
	}

	def "no opinion on different operating system that is not host"() {
		expect:
		subject.compare(notHost, someOtherFamily) == 0
	}

	def "no opinion on same operating system family"() {
		expect:
		subject.compare(host, host) == 0
		subject.compare(someOtherFamily, someOtherFamily) == 0
	}

	private OperatingSystemFamily getHost() {
		return OperatingSystemFamily.forName(System.getProperty("os.name"))
	}

	private OperatingSystemFamily getNotHost() {
		return OperatingSystemFamily.forName('not-host')
	}

	private OperatingSystemFamily getSomeOtherFamily() {
		return OperatingSystemFamily.forName('some-other-family')
	}
}
