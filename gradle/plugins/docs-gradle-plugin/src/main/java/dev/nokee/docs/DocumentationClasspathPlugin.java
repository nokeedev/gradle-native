package dev.nokee.docs;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DocumentationClasspathPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		throw new GradleException("It's a classpath plugin, make sure you use 'apply false'");
	}
}
