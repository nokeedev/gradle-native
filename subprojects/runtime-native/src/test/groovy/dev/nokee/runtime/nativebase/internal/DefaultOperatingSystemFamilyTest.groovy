package dev.nokee.runtime.nativebase.internal

import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily
import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires
import spock.lang.Specification
import spock.util.environment.OperatingSystem

class DefaultOperatingSystemFamilyTest extends Specification {
	def "can compare operating system family instance"() {
		expect:
		DefaultOperatingSystemFamily.WINDOWS == DefaultOperatingSystemFamily.WINDOWS
		DefaultOperatingSystemFamily.WINDOWS != DefaultOperatingSystemFamily.LINUX
		DefaultOperatingSystemFamily.MACOS == new DefaultOperatingSystemFamily(DefaultOperatingSystemFamily.MACOS.name)
		DefaultOperatingSystemFamily.MACOS != new DefaultOperatingSystemFamily(DefaultOperatingSystemFamily.WINDOWS.name)
	}

	@Requires({ OperatingSystem.current.linux })
	def "defaults to the right pre-made instances on Linux"() {
		expect:
		DefaultOperatingSystemFamily.HOST == DefaultOperatingSystemFamily.LINUX
	}

	@Requires({ OperatingSystem.current.windows })
	def "defaults to the right pre-made instances on Windows"() {
		expect:
		DefaultOperatingSystemFamily.HOST == DefaultOperatingSystemFamily.WINDOWS
	}

	@Requires({ OperatingSystem.current.macOs })
	def "defaults to the right pre-made instances on macOS"() {
		expect:
		DefaultOperatingSystemFamily.HOST == DefaultOperatingSystemFamily.MACOS
	}

	@Requires({ SystemUtils.IS_OS_FREE_BSD })
	def "defaults to the right pre-made instances on FreeBSD"() {
		expect:
		DefaultOperatingSystemFamily.HOST == DefaultOperatingSystemFamily.FREE_BSD
	}
}
