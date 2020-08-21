package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.DomainObjectCollection;
import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.platform.base.KnownDomainObject;
import dev.nokee.utils.Cast;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

public abstract class DefaultDomainObjectStore implements DomainObjectStore {
	private final DomainObjectCollection<Object> store = new DefaultDomainObjectCollection<>(Object.class, getObjects(), getProviders());

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public <U> DomainObjectProvider<U> register(DomainObjectFactory<U> factory) {
		store.add(new DomainObjectElement<Object>() {
			@Override
			public Object get() {
				return factory.create();
			}

			@Override
			public Class<Object> getType() {
				return (Class<Object>)factory.getImplementationType();
			}

			@Override
			public DomainObjectIdentity getIdentity() {
				return factory.getIdentity();
			}
		});
		return Cast.uncheckedCastBecauseOfTypeErasure(store.get(factory.getIdentity()));
	}

	@Override
	public void whenElementKnown(Action<KnownDomainObject<?>> action) {
		store.whenElementKnown(action);
	}

	@Override
	public <U> void whenElementKnown(Class<U> type, Action<KnownDomainObject<? extends U>> action) {
		store.whenElementKnown(type, action);
	}

	@Override
	public void configureEach(Action<? super Object> action) {
		store.configureEach(action);
	}

	@Override
	public <U> void configureEach(Class<U> type, Action<? super U> action) {
		store.configureEach(type, action);
	}

	@Override
	public <U> void configureEach(Class<U> type, Spec<? super U> spec, Action<? super U> action) {
		store.configureEach(type, spec, action);
	}

	@Override
	public void configureEach(Spec<? super Object> spec, Action<? super Object> action) {
		store.configureEach(spec, action);
	}

	public Provider<Collection<? extends Object>> getElements() {
		return store.getElements();
	}

	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super Object> mapper) {
		return store.flatMap(mapper);
	}

	@Override
	public <S> Provider<List<? extends S>> select(Spec<? super S> spec) {
		return getElements().map(new Transformer<List<? extends S>, Collection<? extends Object>>() {
			@Override
			public List<? extends S> transform(Collection<?> elements) {
				ImmutableList.Builder<S> result = ImmutableList.builder();
				for (Object element : elements) {
					if (spec.isSatisfiedBy((S)element)) {
						result.add((S)element);
					}
				}
				return result.build();
			}
		});
	}

	public void disallowChanges() {
		this.store.disallowChanges();
	}

	@Override
	public <T> void forceRealize(Class<T> publicType) {
		this.store.getElements().get();
	}
}
