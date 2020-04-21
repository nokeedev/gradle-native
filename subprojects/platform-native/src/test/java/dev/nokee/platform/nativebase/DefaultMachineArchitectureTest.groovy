package dev.nokee.platform.nativebase

import spock.lang.Specification

import static dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.*

class DefaultMachineArchitectureTest extends Specification {
	def "can compare machine architecture instance"() {
		expect:
		dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.X86 == dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.X86
		dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.X86 != dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.X86_64
	}

	def "defaults to the right pre-made instances"() {
		expect:
		// TODO Improve when we test on other architectures
		dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.HOST == dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.X86_64
	}
}
