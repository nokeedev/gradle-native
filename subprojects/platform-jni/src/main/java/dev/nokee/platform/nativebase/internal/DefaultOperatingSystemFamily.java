package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.OperatingSystemFamily;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.gradle.api.Named;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE) /** Use {@link DefaultOperatingSystemFamily#forName(String)} instead */
public class DefaultOperatingSystemFamily implements OperatingSystemFamily, Named {
	@NonNull String name;

	public static final DefaultOperatingSystemFamily WINDOWS = new DefaultOperatingSystemFamily("windows");
	public static final DefaultOperatingSystemFamily LINUX = new DefaultOperatingSystemFamily("linux");
	public static final DefaultOperatingSystemFamily MACOS = new DefaultOperatingSystemFamily("macos");
	public static final DefaultOperatingSystemFamily FREE_BSD = new DefaultOperatingSystemFamily("freebsd");
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
		} else {
			throw new UnsupportedOperationException("Unsupported operating system family of name '" + osName + "'");
		}
	}
}
