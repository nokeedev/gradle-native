package dev.nokee.platform.base.testers;

import com.google.common.reflect.TypeToken;
import dev.nokee.internal.testing.testers.ConfigureMethodTester;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

public interface DependencyAwareComponentTester<T extends ComponentDependencies> {
	DependencyAwareComponent<? extends T> subject();

	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	default Class<? extends ComponentDependencies> getComponentDependenciesType() {
		return (Class<? extends ComponentDependencies>) new TypeToken<T>(getClass()) {}.getRawType();
	}

	@Test
	default void hasComponentDependencies() {
		assertThat("component dependencies should be of the correct type",
			subject().getDependencies(), isA(getComponentDependenciesType()));
	}

	@Test
	default void canConfigureComponentDependencies() {
		ConfigureMethodTester.of(subject(), DependencyAwareComponent::getDependencies)
			.testAction(DependencyAwareComponent::dependencies)
			.testClosure(DependencyAwareComponent::dependencies);
	}

//	@Test
//	default void hasExtensibleComponentDependencies() {
//		assertThat(subject().getDependencies(), isA(ExtensionAware.class));
//	}
}
