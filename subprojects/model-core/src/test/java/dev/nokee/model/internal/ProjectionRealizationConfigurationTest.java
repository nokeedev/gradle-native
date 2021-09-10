package dev.nokee.model.internal;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.TestProjection;
import dev.nokee.model.core.TypeAwareModelProjection;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.model.NokeeExtension.nokee;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;

@PluginRequirement.Require(id = "dev.nokee.model-base")
class ProjectionRealizationConfigurationTest {
	private final Project project = rootProject();
	private final Action<TestProjection> a1 = Mockito.mock(Action.class);
	private final Action<Configuration> a2 = Mockito.mock(Action.class);
	private final InOrder inOrder = Mockito.inOrder(a1, a2);
	private TypeAwareModelProjection<Configuration> compileOnlyProjection;

	@BeforeEach
	void setup() {
		val container = project.getObjects().domainObjectContainer(TestProjection.class);
		nokee(project).bridgeContainer(container);

		val mainNode = nokee(project).getModelRegistry().getRoot().newChildNode("main");
		val mainProjection = mainNode.newProjection(builder -> builder.type(TestProjection.class));
		val compileOnlyNode = mainNode.newChildNode("compileOnly");
		compileOnlyProjection = compileOnlyNode.newProjection(builder -> builder.type(Configuration.class));

		mainProjection.whenRealized(a1);
		compileOnlyProjection.whenRealized(a2);

		Mockito.verify(a1, never()).execute(any());
		Mockito.verify(a2, never()).execute(any());
	}

	@Test
	void realizesConfigurationAndItsOwnersWhenProjectionRealized() {
		compileOnlyProjection.realize();
		inOrder.verify(a1).execute(isA(TestProjection.class));
		inOrder.verify(a2).execute(isA(Configuration.class));
	}

	@Test
	void doesNotRealizeConfigurationAndItsOwnersWhenConfigurationProviderRealized() {
		compileOnlyProjection.get();
		Mockito.verify(a1, never()).execute(any());
		Mockito.verify(a2, never()).execute(any());
	}

	@Test
	void realizesConfigurationAndItsOwnersWhenConfigurationResolved() {
		compileOnlyProjection.get().resolve();
		inOrder.verify(a1).execute(isA(TestProjection.class));
		inOrder.verify(a2).execute(isA(Configuration.class));
	}
}
