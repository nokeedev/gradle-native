package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import lombok.NonNull;
import lombok.Value;
import org.gradle.api.Named;

@Value
public class DefaultOperatingSystemFamily implements OperatingSystemFamily, Named, Coordinate<OperatingSystemFamily> {
	@NonNull String name;

	public static final CoordinateAxis<OperatingSystemFamily> OPERATING_SYSTEM_FAMILY_COORDINATE_AXIS = CoordinateAxis.of(OperatingSystemFamily.class);
	public static final DefaultOperatingSystemFamily WINDOWS = new DefaultOperatingSystemFamily("windows");
	public static final DefaultOperatingSystemFamily LINUX = new DefaultOperatingSystemFamily("linux");
	public static final DefaultOperatingSystemFamily MACOS = new DefaultOperatingSystemFamily("macos");
	public static final DefaultOperatingSystemFamily FREE_BSD = new DefaultOperatingSystemFamily("freebsd");
	public static final DefaultOperatingSystemFamily IOS = new DefaultOperatingSystemFamily("ios");
	public static final DefaultOperatingSystemFamily SOLARIS = new DefaultOperatingSystemFamily("solaris");
	public static final DefaultOperatingSystemFamily HP_UX = new DefaultOperatingSystemFamily("hpux");
	public static final DefaultOperatingSystemFamily HOST = forName(System.getProperty("os.name"));

	@Override
	public boolean isWindows() {
		return equals(WINDOWS);
	}

	@Override
	public boolean isLinux() {
		return equals(LINUX);
	}

	@Override
	public boolean isMacOs() {
		return equals(MACOS);
	}

	@Override
	public boolean isFreeBSD() {
		return equals(FREE_BSD);
	}

	@Override
	public boolean isIos() {
		return equals(IOS);
	}

	@Override
	public boolean isSolaris() {
		return equals(SOLARIS);
	}

	@Override
	public boolean isHewlettPackardUnix() {
		return equals(HP_UX);
	}

	public static DefaultOperatingSystemFamily forName(String name) {
		String osName = name.toLowerCase();
		if (osName.contains("windows")) {
			return WINDOWS;
		} else if (osName.contains("mac os x") || osName.contains("darwin") || osName.contains("osx") || osName.contains("macos")) {
			return MACOS;
		} else if (osName.contains("linux")) {
			return LINUX;
		} else if (osName.contains("freebsd")) {
			return FREE_BSD;
		} else if (osName.contains("ios")) {
			return IOS;
		} else if (osName.contains("sunos") || osName.contains("solaris")) {
			return SOLARIS;
		} else if (osName.contains("hp-ux") || osName.contains("hpux")) {
			return HP_UX;
		} else {
			return new DefaultOperatingSystemFamily(osName); // unknown OS family, use as-is
		}
	}

	@Override
	public CoordinateAxis<OperatingSystemFamily> getAxis() {
		return OPERATING_SYSTEM_FAMILY_COORDINATE_AXIS;
	}
}
