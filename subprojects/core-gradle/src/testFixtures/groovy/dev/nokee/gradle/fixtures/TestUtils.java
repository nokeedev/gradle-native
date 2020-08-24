package dev.nokee.gradle.fixtures;

import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.testfixtures.ProjectBuilder;

public class TestUtils {
	private static final Project PROJECT = ProjectBuilder.builder().build();

	public static ObjectFactory getObjectFactory() {
		return PROJECT.getObjects();
	}
}
