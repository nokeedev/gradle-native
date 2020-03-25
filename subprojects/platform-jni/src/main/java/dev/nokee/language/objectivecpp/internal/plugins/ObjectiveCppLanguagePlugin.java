package dev.nokee.language.objectivecpp.internal.plugins;

import dev.nokee.platform.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ObjectiveCppLanguagePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("objective-cpp");
		project.getPluginManager().apply(NativePlatformCapabilitiesMarkerPlugin.class);
	}
}
