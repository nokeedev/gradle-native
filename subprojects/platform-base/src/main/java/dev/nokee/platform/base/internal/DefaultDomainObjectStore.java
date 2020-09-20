package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.NokeeMap;
import dev.nokee.model.internal.NokeeMapImpl;
import dev.nokee.model.internal.Value;
import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.platform.base.KnownDomainObject;
import dev.nokee.utils.ActionUtils;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.utils.ActionUtils.onlyIf;
import static dev.nokee.utils.TransformerUtils.configureInPlace;

public class DefaultDomainObjectStore implements DomainObjectStore {
	private final NokeeMap<DomainObjectIdentifier, Object> store;

	@Inject
	public DefaultDomainObjectStore(ObjectFactory objects) {
		this.store = new NokeeMapImpl<>(Object.class, objects);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> DomainObjectProvider<U> register(DomainObjectIdentifier identifier, Class<U> type, DomainObjectFactory<U> factory) {
		val value = Value.supplied(type, () -> factory.create(identifier));
		store.put(identifier, (Value<Object>) value);
		return new DomainObjectProviderValueAdapter<>(identifier, value);
	}

	@Override
	public void whenElementKnown(Action<KnownDomainObject<?>> action) {
		store.forEach((k, v) -> action.execute(new KnownDomainObjectValueAdapter<>(k, v)));
	}

	@Override
	public <U> void whenElementKnown(Class<U> type, Action<KnownDomainObject<? extends U>> action) {
		store.forEach((k, v) -> {
			if (type.isAssignableFrom(v.getType())) {
				@SuppressWarnings("unchecked")
				val castedValue = (Value<U>) v;
				action.execute(new KnownDomainObjectValueAdapter<>(k, castedValue));
			}
		});
	}

	@Override
	public void configureEach(Action<? super Object> action) {
		store.forEach((k, v) -> v.mapInPlace(configureInPlace(action)));
	}

	@Override
	public <U> void configureEach(Class<U> type, Action<? super U> action) {
		store.forEach((k, v) -> {
			if (type.isAssignableFrom(v.getType())) {
				v.mapInPlace(configureInPlace(ActionUtils.map(type::cast, action)));
			}
		});
	}

	@Override
	public <U> void configureEach(Class<U> type, Spec<? super U> spec, Action<? super U> action) {
		store.forEach((k, v) -> {
			if (type.isAssignableFrom(v.getType())) {
				v.mapInPlace(configureInPlace(ActionUtils.map(type::cast, onlyIf(spec, action))));
			}
		});
	}

	@Override
	public void configureEach(Spec<? super Object> spec, Action<? super Object> action) {
		store.forEach((k, v) -> v.mapInPlace(configureInPlace(onlyIf(spec, action))));
	}

	public Provider<? extends Collection<? extends Object>> getElements() {
		return store.values().getElements();
	}

	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super Object> mapper) {
		return getElements().map(ProviderUtils.flatMap(mapper));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S> Provider<List<? extends S>> select(Spec<? super S> spec) {
		return getElements().map(it -> it.stream().map(a -> (S)a).collect(Collectors.toList())).map(ProviderUtils.filter(spec));
	}

	public void disallowChanges() {
		store.disallowChanges();
	}

	@Override
	public <T> void forceRealize(Class<T> publicType) {
		store.values().get();
	}
}
