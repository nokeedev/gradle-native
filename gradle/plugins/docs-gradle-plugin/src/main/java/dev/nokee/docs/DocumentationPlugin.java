package dev.nokee.docs;

import dev.gradleplugins.internal.GroovySpockFrameworkTestSuite;
import dev.gradleplugins.internal.plugins.SpockFrameworkTestSuiteBasePlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DocumentationPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SpockFrameworkTestSuiteBasePlugin.class);

		project.getComponents().add(project.getObjects().newInstance(GroovySpockFrameworkTestSuite.class, "docsTest"));
	}
}
