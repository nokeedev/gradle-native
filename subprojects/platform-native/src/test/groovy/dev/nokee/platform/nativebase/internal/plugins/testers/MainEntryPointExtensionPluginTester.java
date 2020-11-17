package dev.nokee.platform.nativebase.internal.plugins.testers;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionsSchema;
import org.gradle.api.reflect.TypeOf;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class MainEntryPointExtensionPluginTester {
	protected abstract String getExtensionName();
	protected abstract Class<?> getExtensionType();
	protected abstract String getQualifiedPluginId();

	private final Project project = ProjectBuilder.builder().build();

	@BeforeEach
	void applyPlugin() {
		project.apply(Collections.singletonMap("plugin", getQualifiedPluginId()));
	}

	@Test
	void canGetExtensionByName() {
		assertNotNull(project.getExtensions().findByName(getExtensionName()), () -> String.format("Extension named '%s' not found. Known extension names are: %s", getExtensionName(), knownExtensionNames(project)));
	}

	private String knownExtensionNames(Project project) {
		return StreamSupport.stream(project.getExtensions().getExtensionsSchema().getElements().spliterator(), false).map(ExtensionsSchema.ExtensionSchema::getName).collect(Collectors.joining(", "));
	}

	@Test
	void rightType() {
		assertThat(project.getExtensions().getByName(getExtensionName()), isA(getExtensionType()));
	}

	@Test
	void canGetExtensionByType() {
		assertNotNull(project.getExtensions().findByType(getExtensionType()), () -> String.format("Extension typed '%s', not found. Known extension types are: %s", getExtensionType().getSimpleName(), knownExtensionTypes(project)));
	}

	private String knownExtensionTypes(Project project) {
		return StreamSupport.stream(project.getExtensions().getExtensionsSchema().getElements().spliterator(), false).map(ExtensionsSchema.ExtensionSchema::getPublicType).map(TypeOf::getConcreteClass).map(Class::getSimpleName).collect(Collectors.joining(", "));
	}
}
