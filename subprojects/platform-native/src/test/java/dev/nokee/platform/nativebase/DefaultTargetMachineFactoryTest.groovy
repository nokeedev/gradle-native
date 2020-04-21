package dev.nokee.platform.nativebase


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
		factory.windows.architecture == dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.X86_64
		factory.linux.architecture == dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.X86_64
		factory.macOS.architecture == dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.X86_64
	}

	def "configure the right operating system family"() {
		expect:
		factory.windows.operatingSystemFamily == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.WINDOWS
		factory.linux.operatingSystemFamily == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.LINUX
		factory.macOS.operatingSystemFamily == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.MACOS
	}

	def "can compare instances"() {
		expect:
		factory.windows == factory.windows
		factory.linux != factory.macOS
		factory.macOS == new DefaultTargetMachine(dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.MACOS, dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.HOST)
	}

	@Requires({ OperatingSystem.current.linux })
	def "defaults to the right pre-made instances on Linux"() {
		expect:
		factory.host().operatingSystemFamily == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.LINUX
	}

	@Requires({ OperatingSystem.current.windows })
	def "defaults to the right pre-made instances on Windows"() {
		expect:
		factory.host().operatingSystemFamily == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.WINDOWS
	}

	@Requires({ OperatingSystem.current.macOs })
	def "defaults to the right pre-made instances on macOS"() {
		expect:
		factory.host().operatingSystemFamily == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.MACOS
	}

	def "defaults to the right pre-made instances"() {
		expect:
		// TODO Improve when we test on other architectures
		factory.host().architecture == dev.nokee.platform.nativebase.internal.DefaultMachineArchitecture.X86_64
	}
}
