package dev.nokee.platform.nativebase.internal

import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires
import spock.lang.Specification
import spock.util.environment.OperatingSystem

import static dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.*

class DefaultOperatingSystemFamilyTest extends Specification {
	def "can compare operating system family instance"() {
		expect:
		WINDOWS == WINDOWS
		WINDOWS != LINUX
		MACOS == new DefaultOperatingSystemFamily(MACOS.name)
		MACOS != new DefaultOperatingSystemFamily(WINDOWS.name)
	}

	@Requires({ OperatingSystem.current.linux })
	def "defaults to the right pre-made instances on Linux"() {
		expect:
		HOST == LINUX
	}

	@Requires({ OperatingSystem.current.windows })
	def "defaults to the right pre-made instances on Windows"() {
		expect:
		HOST == WINDOWS
	}

	@Requires({ OperatingSystem.current.macOs })
	def "defaults to the right pre-made instances on macOS"() {
		expect:
		HOST == MACOS
	}

	@Requires({ SystemUtils.IS_OS_FREE_BSD })
	def "defaults to the right pre-made instances on FreeBSD"() {
		expect:
		HOST == FREE_BSD
	}
}
