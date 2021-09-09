package dev.nokee.model;

import dev.nokee.model.registry.ModelRegistry;
import lombok.val;
import org.gradle.api.plugins.ExtensionAware;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface NokeeExtensionTester extends ConfigurableTester<NokeeExtension> {
	NokeeExtension createSubject();

	@Test
	default void canGetModelRegistry() {
		assertThat(createSubject().getModelRegistry(), isA(ModelRegistry.class));
	}

	@Test
	default void isExtensionAware() {
		assertAll(
			() -> assertThat(ExtensionAware.class.isAssignableFrom(NokeeExtension.class), is(true)),
			() -> assertThat(createSubject(), isA(ExtensionAware.class))
		);
	}

	@Test
	default void returnsExtensionOnContainerBridging() {
		val extension = createSubject();
		assertAll(
			() -> assertThat(extension.bridgeContainer(objectFactory().domainObjectContainer(TestProjection.class)),
				is(extension)),
			() -> assertThat(extension.bridgeContainer(objectFactory().polymorphicDomainObjectContainer(TestProjection.class)),
				is(extension))
		);
	}

	// TODO: Move to integration test
	@Test
	default void canBridgeNamedContainer() {
		val container = objectFactory().domainObjectContainer(TestProjection.class);
		val extension = createSubject().bridgeContainer(container);
		val modelRegistry = extension.getModelRegistry();
		container.register("foo");
		container.create("bar");
		modelRegistry.getRoot().newChildNode("far").newProjection(builder -> builder.type(TestProjection.class));
		assertAll(
			() -> assertThat(modelRegistry.getRoot().get("foo").canBeViewedAs(TestProjection.class), is(true)),
			() -> assertThat(modelRegistry.getRoot().get("bar").canBeViewedAs(TestProjection.class), is(true)),
			() -> assertThat(container.findByName("far"), notNullValue(TestProjection.class))
		);
	}

	@Test
	default void canBridgePolymorphicContainer() {
		val container = objectFactory().polymorphicDomainObjectContainer(TestProjection.class);
		container.registerFactory(TestProjection.class, TestProjection::new);
		val extension = createSubject().bridgeContainer(container);
		val modelRegistry = extension.getModelRegistry();
		container.register("foo", TestProjection.class);
		container.create("bar", TestProjection.class);
		modelRegistry.getRoot().newChildNode("far").newProjection(builder -> builder.type(TestProjection.class));
		assertAll(
			() -> assertThat(modelRegistry.getRoot().get("foo").canBeViewedAs(TestProjection.class), is(true)),
			() -> assertThat(modelRegistry.getRoot().get("bar").canBeViewedAs(TestProjection.class), is(true)),
			() -> assertThat(container.findByName("far"), notNullValue(TestProjection.class))
		);
	}

	@Test
	default void deduplicateAutomaticModelRegistrationFromNamedContainer() {
		val container = objectFactory().domainObjectContainer(TestProjection.class);
		val extension = createSubject().bridgeContainer(container);
		val modelRegistry = extension.getModelRegistry();
		modelRegistry.getRoot().newChildNode("far").newChildNode("bar").newProjection(builder -> builder.type(TestProjection.class));
		assertThat(modelRegistry.getRoot().find("farBar"), emptyOptional());
	}

	@Test
	default void deduplicateAutomaticModelRegistrationFromNPolymorphicContainer() {
		val container = objectFactory().polymorphicDomainObjectContainer(TestProjection.class);
		container.registerFactory(TestProjection.class, TestProjection::new);
		val extension = createSubject().bridgeContainer(container);
		val modelRegistry = extension.getModelRegistry();
		modelRegistry.getRoot().newChildNode("far").newChildNode("bar").newProjection(builder -> builder.type(TestProjection.class));
		assertThat(modelRegistry.getRoot().find("farBar"), emptyOptional());
	}
}
