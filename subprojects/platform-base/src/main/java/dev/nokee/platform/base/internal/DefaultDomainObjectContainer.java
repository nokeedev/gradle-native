package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.*;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import java.util.HashMap;
import java.util.Map;

public class DefaultDomainObjectContainer<T> implements DomainObjectContainer<T> {
	private final DomainObjectCollection<T> collection;
	private final Map<Class<?>, DomainObjectFactory<?>> typeToFactories = new HashMap<>();

	public DefaultDomainObjectContainer(Class<T> type, ObjectFactory objects, ProviderFactory providers) {
		this.collection = new DefaultDomainObjectCollection<>(type, objects, providers);
	}

	@Override
	public <U extends T> void registerFactory(Class<U> type, DomainObjectFactory<U> factory) {
		typeToFactories.put(type, factory);
	}

	@Override
	public <U extends T> DomainObjectProvider<U> register(DomainObjectIdentity identity, Class<U> type) {
		collection.add(new DomainObjectElement<T>() {
			@Override
			public T get() {
				return type.cast(typeToFactories.get(type).create());
			}

			@Override
			public Class<T> getType() {
				return (Class<T>)typeToFactories.get(type).getImplementationType();
			}

			@Override
			public DomainObjectIdentity getIdentity() {
				return identity;
			}
		});
		return collection.get(identity, type);
	}

	@Override
	public <U extends T> DomainObjectProvider<U> register(DomainObjectIdentity identity, Class<U> type, Action<? super U> action) {
		val provider = register(identity, type);
		provider.configure(action);
		return provider;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		collection.configureEach(action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		collection.configureEach(type, action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Spec<? super S> spec, Action<? super S> action) {
		collection.configureEach(type, spec, action);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		collection.configureEach(spec, action);
	}

	@Override
	public void whenElementKnown(Action<KnownDomainObject<? extends T>> action) {
		collection.whenElementKnown(action);
	}

	@Override
	public <U> void whenElementKnown(Class<U> type, Action<KnownDomainObject<? extends U>> action) {
		collection.whenElementKnown(type, action);
	}
}
