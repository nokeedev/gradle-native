package dev.nokee.platform.nativebase.internal


import spock.lang.Requires
import spock.lang.Specification
import spock.util.environment.OperatingSystem

import static dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.*
import static dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.*

class DefaultTargetMachineFactoryTest extends Specification {
	def factory = new DefaultTargetMachineFactory()

	def "defaults to the current architecture"() {
		expect:
		// TODO Improve when we test on other architectures
		factory.windows.architecture == X86_64
		factory.linux.architecture == X86_64
		factory.macOS.architecture == X86_64
	}

	def "configure the right operating system family"() {
		expect:
		factory.windows.operatingSystemFamily == WINDOWS
		factory.linux.operatingSystemFamily == LINUX
		factory.macOS.operatingSystemFamily == MACOS
	}

	def "can compare instances"() {
		expect:
		factory.windows == factory.windows
		factory.linux != factory.macOS
		factory.macOS == new DefaultTargetMachine(MACOS, HOST)
	}

	@Requires({ OperatingSystem.current.linux })
	def "defaults to the right pre-made instances on Linux"() {
		expect:
		factory.host().operatingSystemFamily == LINUX
	}

	@Requires({ OperatingSystem.current.windows })
	def "defaults to the right pre-made instances on Windows"() {
		expect:
		factory.host().operatingSystemFamily == WINDOWS
	}

	@Requires({ OperatingSystem.current.macOs })
	def "defaults to the right pre-made instances on macOS"() {
		expect:
		factory.host().operatingSystemFamily == MACOS
	}

	def "defaults to the right pre-made instances"() {
		expect:
		// TODO Improve when we test on other architectures
		factory.host().architecture == X86_64
	}
}
