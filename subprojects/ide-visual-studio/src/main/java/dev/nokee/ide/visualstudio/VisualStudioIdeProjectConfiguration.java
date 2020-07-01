package dev.nokee.ide.visualstudio;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * The project configuration each item configuration in Visual Studio projects.
 * It associate a single configuration, i.e. Default, Debug, Release, with a single platform, i.e. x64, Win32, ARM.
 *
 * @since 0.5
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VisualStudioIdeProjectConfiguration {
	VisualStudioIdeConfiguration configuration;
	VisualStudioIdePlatform platform;

	public VisualStudioIdeConfiguration getConfiguration() {
		return configuration;
	}

	public VisualStudioIdePlatform getPlatform() {
		return platform;
	}

	public static VisualStudioIdeProjectConfiguration of(VisualStudioIdeConfiguration configuration, VisualStudioIdePlatform platform) {
		return new VisualStudioIdeProjectConfiguration(configuration, platform);
	}
}
