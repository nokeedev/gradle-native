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

	<T> void bridgeContainer(NamedDomainObjectContainer<T> container);
	<T> void bridgeContainer(PolymorphicDomainObjectContainer<T> container);

	ModelNode getModel();
	void model(Action<? super ModelNode> action);
	default void model(@ClosureParams(value = SimpleType.class, options = "dev.nokee.model.dsl.ModelNode") @DelegatesTo(value = ModelNode.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		model(ConfigureUtil.configureUsing(closure));
	}
}
