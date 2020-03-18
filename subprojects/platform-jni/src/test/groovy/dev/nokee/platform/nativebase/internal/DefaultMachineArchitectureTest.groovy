package dev.nokee.platform.nativebase.internal

import spock.lang.Specification

import static dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.*

class DefaultMachineArchitectureTest extends Specification {
	def "can compare machine architecture instance"() {
		expect:
		X86 == X86
		X86 != X86_64
	}

	def "defaults to the right pre-made instances"() {
		expect:
		// TODO Improve when we test on other architectures
		HOST == X86_64
	}
}
