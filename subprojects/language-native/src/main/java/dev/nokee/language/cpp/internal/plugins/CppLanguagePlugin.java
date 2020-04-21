package dev.nokee.language.cpp.internal.plugins;

import dev.nokee.platform.nativebase.internal.plugins.DomainKnowledgeToolchainsRules;
import dev.nokee.platform.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CppLanguagePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("cpp");
		project.getPluginManager().apply(NativePlatformCapabilitiesMarkerPlugin.class);
		project.getPluginManager().apply(DomainKnowledgeToolchainsRules.class);
	}
}
