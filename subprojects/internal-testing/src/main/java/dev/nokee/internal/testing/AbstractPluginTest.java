package dev.nokee.internal.testing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;

public class AbstractPluginTest {
	protected final Project project = rootProject();

	public Project project() {
		return project;
	}

	protected final void applyPlugin(Class<? extends Plugin<? extends Project>> pluginType) {
		project.getPluginManager().apply(pluginType);
	}

	public final void applyPlugin(String pluginId) {
		project.getPluginManager().apply(pluginId);
	}
}
