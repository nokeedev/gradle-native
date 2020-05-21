package dev.nokee.platform.nativebase

import dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture
import spock.lang.Specification

class DefaultMachineArchitectureTest extends Specification {
	def "can compare machine architecture instance"() {
		expect:
		DefaultMachineArchitecture.X86 == DefaultMachineArchitecture.X86
		DefaultMachineArchitecture.X86 != DefaultMachineArchitecture.X86_64
	}

	def "defaults to the right pre-made instances"() {
		expect:
		// TODO Improve when we test on other architectures
		DefaultMachineArchitecture.HOST == DefaultMachineArchitecture.X86_64
	}
}
