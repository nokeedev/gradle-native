package dev.nokee.platform.nativebase.internal.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class NativePlatformCapabilitiesMarkerPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		// Only serve as marking the project as native building capabilities
	}
}
