package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.tasks.TaskDependency;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.function.Consumer;

import static dev.nokee.internal.testing.ConfigurationMatchers.*;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.utils.TestUtils.objectFactory;
import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Subject(ConfigurationMatchers.class)
class ConfigurationMatchersTest {
	@Test
	void canCheckDependenciesOfConfigurationUsingStringCoordinate() {
		assertThat(configuration(addDependency("com.example:foo:4.2")).getDependencies(),
			hasItem(forCoordinate("com.example:foo:4.2")));
	}

	@Test
	void canCheckDependenciesOfConfigurationUsingCoordinateTokens() {
		assertThat(configuration(addDependency("com.example:foo:4.2")).getDependencies(),
			hasItem(forCoordinate("com.example", "foo", "4.2")));
	}

	@Test
	void canCheckDependencyDirectlyFromConfiguration() {
		assertThat(configuration(addDependency("com.example:foo:4.2")),
			hasDependency(forCoordinate("com.example:foo:4.2")));
	}

	@Test
	void givesSensibleErrorMessageWhenCheckingDependency() {
		assertThat(description(it -> forCoordinate("com.example:foo:4.2").describeTo(it)),
			hasToString("(a dependency with group \"com.example\" and a dependency with name \"foo\" and a dependency with version \"4.2\")"));
		assertThat(description(it -> forCoordinate("wrong.group:foo:4.2").describeMismatch(dependency("com.example:foo:4.2"), it)),
			hasToString("a dependency with group \"wrong.group\" but dependency's group was \"com.example\""));
		assertThat(description(it -> forCoordinate("com.example:wrong-name:4.2").describeMismatch(dependency("com.example:foo:4.2"), it)),
			hasToString("a dependency with name \"wrong-name\" but dependency's name was \"foo\""));
		assertThat(description(it -> forCoordinate("com.example:foo:wrong.version").describeMismatch(dependency("com.example:foo:4.2"), it)),
			hasToString("a dependency with version \"wrong.version\" but dependency's version was \"4.2\""));
	}

	private static Configuration configuration(Consumer<? super Configuration> action) {
		return rootProject().getConfigurations().create("test", action::accept);
	}

	private static Dependency dependency(Object notation) {
		return rootProject().getDependencies().create(notation);
	}

	private static Description description(Consumer<? super Description> action) {
		val description = new StringDescription();
		action.accept(description);
		return description;
	}

	@Test
	void onlyCheckFirstLevelDependenciesOfConfiguration() {
		assertThat(configuration(addDependency("com.google.guava:guava:28.0-jre")).getDependencies(),
			contains(forCoordinate("com.google.guava:guava:28.0-jre")));
	}

	private static Consumer<Configuration> addDependency(Object notation) {
		return configuration -> configuration.getDependencies().add(dependency(notation));
	}

	@Test
	void canCheckAttributesByValueDirectlyFromConfiguration() {
		val value = objectFactory().named(Usage.class, "foo");
		assertThat(configuration(addAttribute(Usage.USAGE_ATTRIBUTE, value)),
			hasAttribute(Usage.USAGE_ATTRIBUTE, value));
	}

	@Test
	void canCheckAttributesUsingMatcherDirectlyFromConfiguration() {
		assertThat(configuration(addAttribute(Usage.USAGE_ATTRIBUTE, objectFactory().named(Usage.class, "foo"))),
			hasAttribute(Usage.USAGE_ATTRIBUTE, named("foo")));
	}

	@Test
	void givesSensibleErrorMessageWhenCheckingAttributes() {
		assertThat(description(it -> hasAttribute(Usage.USAGE_ATTRIBUTE, objectFactory().named(Usage.class, "foo")).describeTo(it)),
			hasToString("a configuration with attribute map containing [<org.gradle.usage>-><foo>]"));
		assertThat(description(it -> hasAttribute(Usage.USAGE_ATTRIBUTE, objectFactory().named(Usage.class, "foo"))
				.describeMismatch(configuration(addAttribute(Usage.USAGE_ATTRIBUTE, objectFactory().named(Usage.class, "wrong-name"))), it)),
			hasToString("attributes map was [<org.gradle.usage=wrong-name>]"));
	}

	private static <T> Consumer<Configuration> addAttribute(Attribute<T> attribute, T value) {
		return configuration -> configuration.getAttributes().attribute(attribute, value);
	}

	@Test
	void canCheckConfigurationFromProject() {
		val project = rootProject();
		project.getConfigurations().create("test");
		assertThat(project, hasConfiguration(named("test")));
	}

	@Test
	void canCheckPublishArtifactOfConfiguration() throws IOException {
		val file = File.createTempFile("test", "artifact");
		assertThat(configuration(addArtifact(file)), hasPublishArtifact(ofFile(file)));
	}

	@Test
	void givesSensibleErrorMessageWhenCheckingPublishArtifact() throws IOException {
		val file = File.createTempFile("test", "artifact");
		val wrongFile = File.createTempFile("test", "artifact");
		assertThat(description(it -> hasPublishArtifact(ofFile(file)).describeTo(it)),
			hasToString("a configuration with a collection containing a publish artifact with file <" + file + ">"));
		assertThat(description(it -> hasPublishArtifact(ofFile(file)).describeMismatch(publishArtifact(wrongFile), it)),
			hasToString("was  <" + wrongFile + ">"));
	}

	private static PublishArtifact publishArtifact(File file) {
		return new PublishArtifact() {
			@Override
			public TaskDependency getBuildDependencies() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getName() {
				return file.getName();
			}

			@Override
			public String getExtension() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getType() {
				throw new UnsupportedOperationException();
			}

			@Nullable
			@Override
			public String getClassifier() {
				return null;
			}

			@Override
			public File getFile() {
				return file;
			}

			@Nullable
			@Override
			public Date getDate() {
				return null;
			}

			@Override
			public String toString() {
				return file.getAbsolutePath();
			}
		};
	}

	private static Consumer<Configuration> addArtifact(Object notation) {
		return configuration -> configuration.getOutgoing().artifact(notation);
	}
}
