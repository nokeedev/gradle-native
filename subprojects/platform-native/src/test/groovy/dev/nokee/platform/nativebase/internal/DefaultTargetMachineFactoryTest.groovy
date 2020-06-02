package dev.nokee.platform.nativebase.internal

import dev.nokee.platform.nativebase.internal.DefaultTargetMachineFactory
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine
import spock.lang.Requires
import spock.lang.Specification
import spock.util.environment.OperatingSystem

class DefaultTargetMachineFactoryTest extends Specification {
	def factory = new DefaultTargetMachineFactory()

	def "defaults to the current architecture"() {
		expect:
		// TODO Improve when we test on other architectures
		factory.windows.architecture == DefaultMachineArchitecture.X86_64
		factory.linux.architecture == DefaultMachineArchitecture.X86_64
		factory.macOS.architecture == DefaultMachineArchitecture.X86_64
	}

	def "configure the right operating system family"() {
		expect:
		factory.windows.operatingSystemFamily == DefaultOperatingSystemFamily.WINDOWS
		factory.linux.operatingSystemFamily == DefaultOperatingSystemFamily.LINUX
		factory.macOS.operatingSystemFamily == DefaultOperatingSystemFamily.MACOS
	}

	def "can compare instances"() {
		expect:
		factory.windows == factory.windows
		factory.linux != factory.macOS
		factory.macOS == new DefaultTargetMachine(DefaultOperatingSystemFamily.MACOS, DefaultMachineArchitecture.HOST)
	}

	@Requires({ OperatingSystem.current.linux })
	def "defaults to the right pre-made instances on Linux"() {
		expect:
		factory.host().operatingSystemFamily == DefaultOperatingSystemFamily.LINUX
	}

	@Requires({ OperatingSystem.current.windows })
	def "defaults to the right pre-made instances on Windows"() {
		expect:
		factory.host().operatingSystemFamily == DefaultOperatingSystemFamily.WINDOWS
	}

	@Requires({ OperatingSystem.current.macOs })
	def "defaults to the right pre-made instances on macOS"() {
		expect:
		factory.host().operatingSystemFamily == DefaultOperatingSystemFamily.MACOS
	}

	def "defaults to the right pre-made instances"() {
		expect:
		// TODO Improve when we test on other architectures
		factory.host().architecture == DefaultMachineArchitecture.X86_64
	}
}
