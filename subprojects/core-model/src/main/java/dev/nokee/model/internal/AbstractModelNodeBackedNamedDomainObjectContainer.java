package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectContainer;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.type.ModelType;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.internal.metaobject.DynamicObject;

import javax.annotation.Nullable;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.gradle.util.ConfigureUtil.configureUsing;

abstract class AbstractModelNodeBackedNamedDomainObjectContainer<T> extends AbstractModelNodeBackedNamedDomainObjectView<T> implements DomainObjectContainer<T> {
	private final Projection projection;

	AbstractModelNodeBackedNamedDomainObjectContainer(@Nullable ModelType<T> elementType, ModelNode node) {
		this(elementType, node, null);
		elementsDynamicObject = new ModelNodeBackedNamedDomainObjectCollectionDynamicObject(this.elementType, node) {
			@Override
			protected boolean canRegister() {
				return true;
			}
		};
	}

	AbstractModelNodeBackedNamedDomainObjectContainer(@Nullable ModelType<T> elementType, ModelNode node, DynamicObject elementsDynamicObject) {
		super(elementType, node, elementsDynamicObject);
		this.projection = node.get(Projection.class);
	}

	@Override
	public <U extends T> DomainObjectProvider<U> register(String name, Class<U> type) {
		return projection.register(name, of(type));
	}

	@Override
	public <U extends T> DomainObjectProvider<U> register(String name, Class<U> type, Action<? super U> action) {
		return projection.register(name, of(type), action);
	}

	@Override
	public final <U extends T> DomainObjectProvider<U> register(String name, Class<U> type, Closure<Void> closure) {
		return register(name, type, configureUsing(closure));
	}

	@Override
	public <U extends T> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory) {
		projection.registerFactory(type, factory);
	}

	@Override
	public <U extends T> void registerBinding(Class<U> type, Class<? extends U> implementationType) {
		projection.registerBinding(type, implementationType);
	}

	interface Projection {
		<U> DomainObjectProvider<U> register(String name, ModelType<U> type);
		<U> DomainObjectProvider<U> register(String name, ModelType<U> type, Action<? super U> action);
		<U> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory);
		<U> void registerBinding(Class<U> type, Class<? extends U> implementationType);
	}
}
