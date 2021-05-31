package dev.nokee.model.internal;

import dev.nokee.model.internal.TestDomainObjects.Bean;
import dev.nokee.model.internal.TestDomainObjects.BeanSub1;
import dev.nokee.model.internal.TestDomainObjects.BeanSub2;
import dev.nokee.model.internal.TestDomainObjects.DefaultBean;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.internal.SupportedTypes.instanceOf;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

class PolymorphicContainerRegistryIntegrationTest extends AbstractDomainObjectContainerRegistryIntegrationTest<Bean> {
	@Override
	protected NamedDomainObjectContainerRegistry<Bean> createSubject() {
		val container = objectFactory().polymorphicDomainObjectContainer(Bean.class);
		container.registerFactory(Bean.class, DefaultBean::new);
		container.registerFactory(BeanSub1.class, BeanSub1::new);
		container.registerFactory(BeanSub2.class, BeanSub2::new);
		return new NamedDomainObjectContainerRegistry.PolymorphicContainerRegistry<>(container);
	}

	@Override
	protected Class<Bean> getType() {
		return Bean.class;
	}

	@Test
	void canCreateRegisteredFactory() {
		assertThat(createSubject().getRegistrableTypes(), containsInAnyOrder(instanceOf(Bean.class), instanceOf(BeanSub1.class), instanceOf(BeanSub2.class)));
	}

	@Test
	void canRegisterDifferentObjectTypes() {
		assertAll(
			assertRegisteredType(BeanSub1.class, () -> createSubject().register("a", BeanSub1.class)),
			assertRegisteredType(BeanSub2.class, () -> createSubject().register("b", BeanSub2.class, doSomething())),
			assertRegisteredType(BeanSub2.class, () -> createSubject().registerIfAbsent("c", BeanSub2.class)),
			assertRegisteredType(BeanSub1.class, () -> createSubject().registerIfAbsent("d", BeanSub1.class, doSomething()))
		);
	}
	private Executable assertRegisteredType(Class<? extends Bean> expectedType, ThrowingSupplier<NamedDomainObjectProvider<? extends Bean>> executable) {
		return () -> assertThat(executable.get(), providerOf(isA(expectedType)));
	}

	@Test
	void canAccessContainerAssociatedToRegistry() {
		assertThat(createSubject().getContainer(), isA(PolymorphicDomainObjectContainer.class));
	}
}
