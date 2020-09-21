package dev.nokee.platform.nativebase.internal.tasks;

import org.gradle.api.DefaultTask;

import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public class StaticLibraryLifecycleTask extends DefaultTask {
	@Override
	public String getGroup() {
		return BUILD_GROUP;
	}

	@Override
	public void setGroup(String group) {
		// ignores
	}

	@Override
	public String getDescription() {
		// TODO: The description should be derived from the owner (missing concept, but coming soon)
		return "Assembles a static library binary containing the main objects.";
	}

	@Override
	public void setDescription(String description) {
		// ignores
	}
}
