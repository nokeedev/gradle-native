package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.NamedDomainObjectView;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.type.ModelType;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.internal.metaobject.DynamicObject;

import javax.annotation.Nullable;
import java.util.Map;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.gradle.util.ConfigureUtil.configureUsing;

abstract class AbstractModelNodeBackedNamedDomainObjectView<T> extends AbstractModelNodeBackedDomainObjectView<T> implements NamedDomainObjectView<T> {
	private final Projection projection;

	AbstractModelNodeBackedNamedDomainObjectView(@Nullable ModelType<T> elementType, ModelNode node) {
		this(elementType, node, new ModelNodeBackedNamedDomainObjectCollectionDynamicObject(elementType, node) {
			@Override
			protected boolean canRegister() {
				return false;
			}
		});
	}

	AbstractModelNodeBackedNamedDomainObjectView(@Nullable ModelType<T> elementType, ModelNode node, DynamicObject elementsDynamicObject) {
		super(elementType, node, elementsDynamicObject);
		this.projection = node.get(Projection.class);
	}

	@Override
	public final void configure(String name, Action<? super T> action) {
		projection.configure(name, elementType, action);
	}

	@Override
	public final void configure(String name, Closure<Void> closure) {
		projection.configure(name, elementType, configureUsing(closure));
	}

	@Override
	public <S extends T> void configure(String name, Class<S> type, Action<? super S> action) {
		projection.configure(name, of(type), action);
	}

	@Override
	public final <S extends T> void configure(String name, Class<S> type, Closure<Void> closure) {
		configure(name, type, configureUsing(closure));
	}

	@Override
	public final DomainObjectProvider<T> get(String name) {
		return projection.get(name, elementType);
	}

	@Override
	public final <S extends T> DomainObjectProvider<S> get(String name, Class<S> type) {
		return projection.get(name, of(type));
	}

	interface Projection {
		<T> DomainObjectProvider<T> get(String name, ModelType<T> type);
		<T> void configure(String name, ModelType<T> type, Action<? super T> action);
		<T> Map<String, DomainObjectProvider<T>> getAsMap(ModelType<T> type);
		<T> NamedDomainObjectView<T> createSubView(ModelType<T> type);
	}
}
