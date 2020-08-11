package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.*;
import dev.nokee.utils.Cast;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import java.util.*;

public class DefaultDomainObjectCollection<T> implements DomainObjectCollection<T> {
	private final Map<String, DomainObjectElement<T>> nameToElements = new HashMap<>();
	private final DomainObjectSet<KnownDomainObject<T>> knownElements;
	private final ProviderFactory providers;
	private final NamedDomainObjectContainer<DomainObjectElement<T>> elements;
	private final DomainObjectElementObserver<T> elementObserver;
	private final View<T> baseView;
	private final Map<DomainObjectIdentity, DomainObjectProvider<T>> identityToProviders = new HashMap<>();
	private boolean disallowChanges = false;

	public DefaultDomainObjectCollection(Class<T> type, ObjectFactory objects, ProviderFactory providers) {
		this.knownElements = Cast.uncheckedCastBecauseOfTypeErasure(objects.domainObjectSet(KnownDomainObject.class));
		this.providers = providers;
		this.elements = Cast.uncheckedCastBecauseOfTypeErasure(objects.domainObjectContainer(DomainObjectElement.class, this::createElement));
		elementObserver = new DefaultDomainObjectElementObserver<>(knownElements);
		baseView = new DefaultView<>(this);
	}

	private DomainObjectElement<T> createElement(String name) {
		val elementSupplier = nameToElements.remove(name);
		val element = elementSupplier.get();
		return newElement(elementSupplier.getIdentity(), element.getClass(), element);
	}

	private DomainObjectElement<T> newElement(DomainObjectIdentity identity, Class<?> type, T element) {
		return new DomainObjectElements.Naming<>(identity, Cast.uncheckedCastBecauseOfTypeErasure(type), element);
	}

	@Override
	public boolean add(DomainObjectElement<T> element) {
		if (disallowChanges) {
			throw new IllegalStateException("The value cannot be changed any further.");
		}

		val elementName = ((NamedDomainObjectIdentity)element.getIdentity()).getName();
		if (element instanceof DomainObjectElements.Existing) {
			knownElements.add(new KnownDomainObjects.Existing<>(element.getIdentity(), element.getType(), element.get()));
			elements.add(newElement(element.getIdentity(), element.getType(), element.get()));
			identityToProviders.put(element.getIdentity(), new DefaultDomainObjectProvider<>(element.getIdentity(), element.getType(), elements.named(elementName)));
			return true;
		}

		nameToElements.put(elementName, element);
		val provider = elements.register(elementName);
		knownElements.add(new KnownDomainObjects.Providing<>(element.getIdentity(), element.getType(), provider));
		identityToProviders.put(element.getIdentity(), new DefaultDomainObjectProvider<>(element.getIdentity(), element.getType(), provider));
		return true;
	}

	@Override
	public DomainObjectProvider<T> get(DomainObjectIdentity identity) {
		return identityToProviders.get(identity);
	}

	@Override
	public void whenElementKnown(Action<KnownDomainObject<? extends T>> action) {
		elementObserver.whenElementKnown(action);
	}

	@Override
	public <U> void whenElementKnown(Class<U> type, Action<KnownDomainObject<? extends U>> action) {
		elementObserver.whenElementKnown(type, action);
	}

	@Override
	public View<T> getAsView() {
		return baseView;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		elements.configureEach(element -> action.execute(element.get()));
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		elements.configureEach(element -> {
			if (type.isAssignableFrom(element.getType())) {
				action.execute(type.cast(element.get()));
			}
		});
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Spec<? super S> spec, Action<? super S> action) {
		elements.configureEach(element -> {
			if (type.isAssignableFrom(element.getType())) {
				if (spec.isSatisfiedBy(type.cast(element.get()))) {
					action.execute(type.cast(element.get()));
				}
			}
		});
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		elements.configureEach(element -> {
			if (spec.isSatisfiedBy(element.get())) {
				action.execute(element.get());
			}
		});
	}

	@Override
	public int size() {
		return knownElements.size();
	}

	@Override
	public Provider<Collection<? extends T>> getElements() {
		return providers.provider(() -> {
			if (!disallowChanges) {
				throw new IllegalStateException("Please disallow changes before realizing this collection.");
			}
			return elements.stream().map(DomainObjectElement::get).collect(ImmutableList.toImmutableList());
		});
	}

	@Override
	public <S extends T> View<S> withType(Class<S> type) {
		return new DefaultFilteringView<>(Cast.uncheckedCast("", baseView), it -> type.isAssignableFrom(it.getClass()));
	}

	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return getElements().map(new Transformer<List<? extends S>, Collection<? extends T>>() {
			@Override
			public List<? extends S> transform(Collection<? extends T> elements) {
				ImmutableList.Builder<S> result = ImmutableList.builder();
				for (T element : elements) {
					result.add(mapper.transform(element));
				}
				return result.build();
			}
		});
	}

	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return getElements().map(new Transformer<List<? extends S>, Collection<? extends T>>() {
			@Override
			public List<? extends S> transform(Collection<? extends T> elements) {
				ImmutableList.Builder<S> result = ImmutableList.builder();
				for (T element : elements) {
					result.addAll(mapper.transform(element));
				}
				return result.build();
			}
		});
	}

	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return flatMap(new Transformer<Iterable<? extends T>, T>() {
			@Override
			public Iterable<? extends T> transform(T t) {
				if (spec.isSatisfiedBy(t)) {
					return ImmutableList.of(t);
				}
				return ImmutableList.of();
			}
		});
	}

	@Override
	public DomainObjectCollection<T> disallowChanges() {
		disallowChanges = true;
		return this;
	}
}
