package dev.nokee.platform.nativebase.internal

import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture
import spock.lang.Specification
import spock.lang.Subject

@Subject(PreferHostMachineArchitectureComparator)
class PreferHostMachineArchitectureComparatorTest extends Specification {
	def subject = new PreferHostMachineArchitectureComparator()

	def "always prefer host machine architecture"() {
		expect:
		subject.compare(host, notHost) == -1
		subject.compare(notHost, host) == 1
	}

	def "no opinion on different machine architecture that is not host"() {
		expect:
		subject.compare(notHost, someOtherArchitecture) == 0
	}

	def "no opinion on same machine architecture"() {
		expect:
		subject.compare(host, host) == 0
		subject.compare(someOtherArchitecture, someOtherArchitecture) == 0
	}

	private DefaultMachineArchitecture getHost() {
		return DefaultMachineArchitecture.HOST
	}

	private DefaultMachineArchitecture getNotHost() {
		assert DefaultMachineArchitecture.X86 != DefaultMachineArchitecture.HOST
		return DefaultMachineArchitecture.X86
	}

	private DefaultMachineArchitecture getSomeOtherArchitecture() {
		return new DefaultMachineArchitecture.UnknownMachineArchitecture('some-other-architecture')
	}
}
