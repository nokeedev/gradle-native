package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.internal.DefaultDimensionType;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import lombok.NonNull;
import lombok.Value;
import org.gradle.api.Named;

@Value
public class DefaultOperatingSystemFamily implements OperatingSystemFamily, Named, Dimension {
	@NonNull String name;

	public static final DimensionType<DefaultOperatingSystemFamily> DIMENSION_TYPE = new DefaultDimensionType<>(DefaultOperatingSystemFamily.class);
	public static final DefaultOperatingSystemFamily WINDOWS = new DefaultOperatingSystemFamily("windows");
	public static final DefaultOperatingSystemFamily LINUX = new DefaultOperatingSystemFamily("linux");
	public static final DefaultOperatingSystemFamily MACOS = new DefaultOperatingSystemFamily("macos");
	public static final DefaultOperatingSystemFamily FREE_BSD = new DefaultOperatingSystemFamily("freebsd");
	public static final DefaultOperatingSystemFamily IOS = new DefaultOperatingSystemFamily("ios");
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
		} else {
			throw new UnsupportedOperationException("Unsupported operating system family of name '" + osName + "'");
		}
	}

	@Override
	public DimensionType getType() {
		return DIMENSION_TYPE;
	}
}
