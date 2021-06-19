package dev.nokee.runtime.nativebase.internal;

import com.google.common.testing.EqualsTester;
import dev.nokee.runtime.nativebase.KnownOperatingSystemFamilyTester;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.UnknownOperatingSystemFamilyTester;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DefaultOperatingSystemFamilyTest implements KnownOperatingSystemFamilyTester, UnknownOperatingSystemFamilyTester {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void canCompareOperatingSystemFamilyInstance() {
		new EqualsTester()
			.addEqualityGroup(DefaultOperatingSystemFamily.WINDOWS, DefaultOperatingSystemFamily.WINDOWS, new DefaultOperatingSystemFamily(DefaultOperatingSystemFamily.WINDOWS.getName()))
			.addEqualityGroup(DefaultOperatingSystemFamily.LINUX)
			.addEqualityGroup(DefaultOperatingSystemFamily.MACOS, new DefaultOperatingSystemFamily(DefaultOperatingSystemFamily.MACOS.getName()))
			.testEquals();
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void defaultsToTheRightPreMadeInstancesOnLinux() {
		assertThat(DefaultOperatingSystemFamily.HOST, equalTo(DefaultOperatingSystemFamily.LINUX));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void defaultsToTheRightPreMadeInstancesOnWindows() {
		assertThat(DefaultOperatingSystemFamily.HOST, equalTo(DefaultOperatingSystemFamily.WINDOWS));
	}

	@Test
	@EnabledOnOs(OS.MAC)
	void defaultsToTheRightPreMadeInstancesOnMacOS() {
		assertThat(DefaultOperatingSystemFamily.HOST, equalTo(DefaultOperatingSystemFamily.MACOS));
	}

	@Test
	@EnabledIf("isOsFreeBsd")
	void defaultsToTheRightPreMadeInstancesOnFreeBSD() {
		assertThat(DefaultOperatingSystemFamily.HOST, equalTo(DefaultOperatingSystemFamily.FREE_BSD));
	}

	static boolean isOsFreeBsd() {
		return SystemUtils.IS_OS_FREE_BSD;
	}

	@Override
	public OperatingSystemFamily createSubject(String name) {
		return DefaultOperatingSystemFamily.forName(name);
	}
}
