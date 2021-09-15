package dev.nokee.model.internal;

import dev.nokee.model.internal.TestDomainObjects.Bean;
import dev.nokee.model.internal.TestDomainObjects.DefaultBean;
import lombok.val;
import org.gradle.api.NamedDomainObjectContainer;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.SupportedTypes.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.isA;

class NamedContainerRegistryIntegrationTest extends AbstractDomainObjectContainerRegistryIntegrationTest<Bean> {
	@Override
	protected NamedDomainObjectContainerRegistry<Bean> createSubject() {
		val container = objectFactory().domainObjectContainer(Bean.class, DefaultBean::new);
		return new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(container);
	}

	@Override
	protected Class<Bean> getType() {
		return Bean.class;
	}

	@Test
	void canCreateOnlyBaseType() {
		assertThat(createSubject().getRegistrableTypes(), contains(instanceOf(Bean.class)));
	}

	@Test
	void canAccessContainerAssociatedToRegistry() {
		assertThat(createSubject().getContainer(), optionalWithValue(isA(NamedDomainObjectContainer.class)));
	}
}
