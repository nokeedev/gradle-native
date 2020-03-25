package dev.nokee.language.objectivec.internal.plugins;

import dev.nokee.platform.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ObjectiveCLanguagePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("objective-c");
		project.getPluginManager().apply(NativePlatformCapabilitiesMarkerPlugin.class);
	}
}
