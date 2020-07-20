package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.platform.base.KnownDomainObject;
import dev.nokee.utils.Cast;
import lombok.*;
import org.gradle.api.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public abstract class DefaultDomainObjectStore implements DomainObjectStore {
	private final Map<DomainObjectIdentity, DomainObjectFactory<?>> elementFactories = new HashMap<>();
	private final NamedDomainObjectContainer<Element<?>> elements = Cast.uncheckedCastBecauseOfTypeErasure(getObjects().domainObjectContainer(Element.class, this::createElement));
	private final DomainObjectSet<KnownDomainObject<?>> knownElements = Cast.uncheckedCastBecauseOfTypeErasure(getObjects().domainObjectSet(KnownDomainObject.class));

	private Element<?> createElement(String name) {
		val factory = elementFactories.remove(DomainObjectIdentity.named(name));
		return new Element<Object>(factory.getIdentity(), Cast.uncheckedCast("", factory.getImplementationType()), factory.create());
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public <U> DomainObjectProvider<U> register(DomainObjectFactory<U> factory) {
		elementFactories.put(factory.getIdentity(), factory);

		NamedDomainObjectProvider<Element<?>> elementProvider = elements.register(((NamedDomainObjectIdentity) factory.getIdentity()).getName());
		DomainObjectProvider<U> provider = Cast.uncheckedCast("of type erasure", getObjects().newInstance(DomainObjectElementProvider.class, factory.getIdentity(), factory.getImplementationType(), elementProvider));
		KnownDomainObject<U> knownElement = Cast.uncheckedCast("of type erasure", getObjects().newInstance(DefaultKnownDomainObject.class, provider));
		knownElements.add(knownElement);

		return provider;
	}

	@Override
	public void whenElementKnown(Action<KnownDomainObject<?>> action) {
		knownElements.all(action);
	}

	@Override
	public <U> void whenElementKnown(Class<U> type, Action<KnownDomainObject<? extends U>> action) {
		knownElements.all(knownElement -> {
			if (type.isAssignableFrom(knownElement.getType())) {
				action.execute(Cast.uncheckedCast("of type erasure", knownElement));
			}
		});
	}

	@Override
	public void configureEach(Action<? super Object> action) {
		elements.configureEach(it -> action.execute(it.get()));
	}

	@Override
	public <U> void configureEach(Class<U> type, Action<? super U> action) {
		elements.configureEach(it -> {
			if (type.isAssignableFrom(it.getType())) {
				action.execute(type.cast(it.get()));
			}
		});
	}

	@Override
	public <U> void configureEach(Class<U> type, Spec<? super U> spec, Action<? super U> action) {
		elements.configureEach(it -> {
			if (type.isAssignableFrom(it.getType())) {
				if (spec.isSatisfiedBy(type.cast(it.get()))) {
					action.execute(type.cast(it.get()));
				}
			}
		});
	}

	@Override
	public void configureEach(Spec<? super Object> spec, Action<? super Object> action) {
		elements.configureEach(it -> {
			if (spec.isSatisfiedBy(it.get())) {
				action.execute(it.get());
			}
		});
	}

	public Provider<Set<? extends Object>> getElements() {
		return getProviders().provider(() -> elements.stream().map(Element::get).collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super Object> mapper) {
//		Preconditions.checkArgument(mapper != null, "flatMap mapper for %s must not be null", getDisplayName());
		return getElements().map(new Transformer<List<? extends S>, Set<? extends Object>>() {
			@Override
			public List<? extends S> transform(Set<? extends Object> elements) {
				ImmutableList.Builder<S> result = ImmutableList.builder();
				for (Object element : elements) {
					result.addAll(mapper.transform(element));
				}
				return result.build();
			}
		});
	}

	@Override
	public <S> Provider<List<? extends S>> select(Spec<? super S> spec) {
		return getElements().map(new Transformer<List<? extends S>, Set<? extends Object>>() {
			@Override
			public List<? extends S> transform(Set<?> elements) {
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

	@RequiredArgsConstructor
	private static class Element<I> implements Named {
		private final DomainObjectIdentity identity;
		@Getter private final Class<I> type;
		private final I value;

		@Override
		public String getName() {
			return ((NamedDomainObjectIdentity)identity).getName();
		}

		public I get() {
			return value;
		}
	}

	@ToString
	public static class DomainObjectElementProvider<I> implements DomainObjectProvider<I> {
		@Getter private final DomainObjectIdentity identity;
		@Getter private final Class<I> type;
		private final NamedDomainObjectProvider<Element<I>> delegate;

		@Inject
		public DomainObjectElementProvider(DomainObjectIdentity identity, Class<I> type, NamedDomainObjectProvider<Element<I>> delegate) {
			this.identity = identity;
			this.type = type;
			this.delegate = delegate;
		}

		@Override
		public I get() {
			return delegate.get().get();
		}

		@Override
		public void configure(Action<? super I> action) {
			delegate.configure(it -> action.execute(it.get()));
		}

		@Override
		public <S> Provider<S> map(Transformer<? extends S, ? super I> transformer) {
			return delegate.map(it -> transformer.transform(it.get()));
		}

		@Override
		public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super I> transformer) {
			return delegate.flatMap(it -> transformer.transform(it.get()));
		}
	}
}
