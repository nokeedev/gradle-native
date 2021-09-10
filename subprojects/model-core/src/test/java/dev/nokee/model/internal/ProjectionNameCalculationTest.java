package dev.nokee.model.internal;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.core.ModelNode;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.model.NokeeExtension.nokee;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(id = "dev.nokee.model-base")
class ProjectionNameCalculationTest {
	private final Project project = rootProject();

	private ModelNode newChildNode(Object... paths) {
		ModelNode node = nokee(project).getModelRegistry().getRoot();
		for (Object path : paths) {
			node = node.newChildNode(path);
		}
		return node;
	}

	@Nested
	class TaskCalculationTest {
		@Test
		void multiNamePath() {
			val result = newChildNode("foo", "bar", "compile")
				.newProjection(builder -> builder.type(Task.class));
			assertThat(result.get(), named("compileFooBar"));
		}

		@Test
		void firstParentWithMainName() {
			val result = newChildNode(new MainName("foo"), "bar", "compile")
				.newProjection(builder -> builder.type(Task.class));
			assertThat(result.get(), named("compileBar"));
		}

		@Test
		void middleParentWithMainName() {
			val result = newChildNode("foo", new MainName("bar"), "compile")
				.newProjection(builder -> builder.type(Task.class));
			assertThat(result.get(), named("compileFoo"));
		}

		@Test
		void parentPathsWithOnlyMainName() {
			val result = newChildNode(new MainName("foo"), new MainName("bar"), "compile")
				.newProjection(builder -> builder.type(Task.class));
			assertThat(result.get(), named("compile"));
		}

		@Test
		void pathWithEmptyIdentityString() {
			val result = newChildNode("", "compile")
				.newProjection(builder -> builder.type(Configuration.class));
			assertThat(result.get(), named("compile"));
		}
	}

	@Nested
	class ConfigurationCalculationTest {
		@Test
		void multiNamePath() {
			val result = newChildNode("foo", "bar", "compileOnly")
				.newProjection(builder -> builder.type(Configuration.class));
			assertThat(result.get(), named("fooBarCompileOnly"));
		}

		@Test
		void firstParentWithMainName() {
			val result = newChildNode(new MainName("foo"), "bar", "compileOnly")
				.newProjection(builder -> builder.type(Configuration.class));
			assertThat(result.get(), named("barCompileOnly"));
		}

		@Test
		void middleParentWithMainName() {
			val result = newChildNode("foo", new MainName("bar"), "compileOnly")
				.newProjection(builder -> builder.type(Configuration.class));
			assertThat(result.get(), named("fooCompileOnly"));
		}

		@Test
		void parentPathsWithOnlyMainName() {
			val result = newChildNode(new MainName("foo"), new MainName("bar"), "compileOnly")
				.newProjection(builder -> builder.type(Configuration.class));
			assertThat(result.get(), named("compileOnly"));
		}

		@Test
		void pathWithEmptyIdentityString() {
			val result = newChildNode("", "compileOnly")
				.newProjection(builder -> builder.type(Configuration.class));
			assertThat(result.get(), named("compileOnly"));
		}
	}


	static class MainName implements NameProvider {
		private final String name;

		MainName(String name) {
			this.name = name;
		}

		@Override
		public Optional<String> getProvidedName() {
			return Optional.empty();
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
