package dev.nokee.platform.nativebase.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;

import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public class SharedLibraryLifecycleTask extends DefaultTask {
	@Override
	@Internal
	public String getGroup() {
		return BUILD_GROUP;
	}

	@Override
	public void setGroup(String group) {
		// ignores
	}

	@Override
	@Internal
	public String getDescription() {
		// TODO: The description should be derived from the owner (missing concept, but coming soon)
		return "Assembles a shared library binary containing the main objects.";
	}

	@Override
	public void setDescription(String description) {
		// ignores
	}
}
