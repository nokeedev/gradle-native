package dev.nokee.platform.nativebase

import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires
import spock.lang.Specification
import spock.util.environment.OperatingSystem

import static dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.*

class DefaultOperatingSystemFamilyTest extends Specification {
	def "can compare operating system family instance"() {
		expect:
		dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.WINDOWS == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.WINDOWS
		dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.WINDOWS != dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.LINUX
		dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.MACOS == new DefaultOperatingSystemFamily(dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.MACOS.name)
		dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.MACOS != new DefaultOperatingSystemFamily(dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.WINDOWS.name)
	}

	@Requires({ OperatingSystem.current.linux })
	def "defaults to the right pre-made instances on Linux"() {
		expect:
		dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.HOST == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.LINUX
	}

	@Requires({ OperatingSystem.current.windows })
	def "defaults to the right pre-made instances on Windows"() {
		expect:
		dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.HOST == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.WINDOWS
	}

	@Requires({ OperatingSystem.current.macOs })
	def "defaults to the right pre-made instances on macOS"() {
		expect:
		dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.HOST == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.MACOS
	}

	@Requires({ SystemUtils.IS_OS_FREE_BSD })
	def "defaults to the right pre-made instances on FreeBSD"() {
		expect:
		dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.HOST == dev.nokee.platform.nativebase.internal.DefaultOperatingSystemFamily.FREE_BSD
	}
}
