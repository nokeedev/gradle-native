package dev.nokee.platform.nativebase.internal

import dev.nokee.runtime.nativebase.MachineArchitecture
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

	private MachineArchitecture getHost() {
		return MachineArchitecture.forName(System.getProperty('os.arch'))
	}

	private MachineArchitecture getNotHost() {
		assert MachineArchitecture.X86 != MachineArchitecture.forName(System.getProperty('os.arch')).canonicalName
		return MachineArchitecture.forName(MachineArchitecture.X86);
	}

	private MachineArchitecture getSomeOtherArchitecture() {
		return MachineArchitecture.forName('some-other-architecture')
	}
}
