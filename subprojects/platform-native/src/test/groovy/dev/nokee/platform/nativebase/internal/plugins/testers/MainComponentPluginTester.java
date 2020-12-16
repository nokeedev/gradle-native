package dev.nokee.platform.nativebase.internal.plugins.testers;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

public abstract class MainComponentPluginTester {
	protected abstract Class<? extends Component> getComponentType();
	protected abstract String getQualifiedPluginId();

	private final Project project = TestUtils.rootProject();

	@BeforeEach
	void applyPlugin() {
		project.apply(Collections.singletonMap("plugin", getQualifiedPluginId()));
	}

	@Test
	void createMainComponent() {
		// TODO: Use the ComponentContainer instead of entity repository
		ComponentIdentifier<?> mainIdentifier = ComponentIdentifier.ofMain(getComponentType(), ProjectIdentifier.of(project));
		Component mainComponent = project.getExtensions().getByType(ComponentContainer.class).get("main", (Class<Component>)getComponentType()).get();

		assertThat(mainComponent, isA(getComponentType()));
	}
}
