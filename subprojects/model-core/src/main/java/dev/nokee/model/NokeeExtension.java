package dev.nokee.model;

import dev.nokee.model.dsl.ModelNode;
import dev.nokee.model.registry.ModelRegistry;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.PolymorphicDomainObjectContainer;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.util.ConfigureUtil;

public interface NokeeExtension extends ExtensionAware {
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

	ModelNode getModel();
	void model(Action<? super ModelNode> action);
	default void model(@ClosureParams(value = SimpleType.class, options = "dev.nokee.model.dsl.ModelNode") @DelegatesTo(value = ModelNode.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		model(ConfigureUtil.configureUsing(closure));
	}
}
