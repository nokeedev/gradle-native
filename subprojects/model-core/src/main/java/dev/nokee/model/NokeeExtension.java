package dev.nokee.model;

import dev.nokee.model.registry.ModelRegistry;
import dev.nokee.model.util.Configurable;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.ExtensionAware;

public interface NokeeExtension extends ExtensionAware, Configurable<NokeeExtension> {
	ModelRegistry getModelRegistry();

	/**
	 * Bridges the specified {@link NamedDomainObjectContainer} into the Nokee model.
	 * Any elements created in the container will automatically bridge into the model.
	 *
	 * <pre>
	 * def myContainer = objects.domainObjectContainer(MyType)
	 * def e = myContainer.foo
	 * nokee.bridgeContainer(myContainer)
	 * assert nokee.model.foo(MyType).map { it }.get() == e
	 * </pre>
	 *
	 * The model can also create projection for the creatable type of the container.
	 *
	 * <pre>
	 * def myContainer = objects.domainObjectContainer(MyType)
	 * nokee.bridgeContainer(myContainer)
	 * assert nokee.model.foo(MyType).map { it }.get() == myContainer.foo
	 * </pre>
	 *
	 * @param container  the container to bridge into the model, must not be null
	 * @param <T>  the base type of the container
	 * @return this extension instance, never null
	 */
	<T> NokeeExtension bridgeContainer(NamedDomainObjectContainer<T> container);

	/**
	 * Bridges the specified {@link PolymorphicDomainObjectContainer} into the Nokee model.
	 * Any elements created in the container will automatically bridge into the model.
	 *
	 * <pre>
	 * def myContainer = objects.polymorphicObjectContainer(MyType)
	 * // assuming the container can create MyType elements
	 * def e = myContainer.foo(MyType)
	 * nokee.bridgeContainer(myContainer)
	 * assert nokee.model.foo(MyType).map { it }.get() == e
	 * </pre>
	 *
	 * The model can also create projection for the creatable type of the container.
	 *
	 * <pre>
	 * def myContainer = objects.polymorphicObjectContainer(MyType)
	 * // assuming the container can create MyType elements
	 * nokee.bridgeContainer(myContainer)
	 * assert nokee.model.foo(MyType).map { it }.get() == myContainer.foo
	 * </pre>
	 *
	 * @param container  the container to bridge into the model, must not be null
	 * @param <T>  the base type of the container
	 * @return this extension instance, never null
	 */
	<T> NokeeExtension bridgeContainer(PolymorphicDomainObjectContainer<T> container);

	// Note: We duplicate the methods here for Project and Settings to provide compile-time validation.

	/**
	 * Safe accessor for {@literal NokeeExtension} on {@literal Project} instances.
	 *
	 * @param project  a project instance with a Nokee extension, must not be null
	 * @return the Nokee extension register on the {@literal Project} instance, never null
	 */
	static NokeeExtension nokee(Project project) {
		return NokeeExtensionUtils.nokee(project);
	}

	/**
	 * Safe accessor for {@literal NokeeExtension} on {@literal Settings} instances.
	 *
	 * @param settings  a settings instance with a Nokee extension, must not be null
	 * @return the Nokee extension register on the {@literal Settings} instance, never null
	 */
	static NokeeExtension nokee(Settings settings) {
		return NokeeExtensionUtils.nokee(settings);
	}
}
