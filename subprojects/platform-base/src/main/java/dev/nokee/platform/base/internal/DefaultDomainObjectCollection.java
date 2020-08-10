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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultDomainObjectCollection<T> implements DomainObjectCollection<T> {
	private final Map<String, DomainObjectElement<T>> nameToElements = new HashMap<>();
	private final DomainObjectSet<KnownDomainObject<T>> knownElements;
	private final ProviderFactory providers;
	private final NamedDomainObjectContainer<DomainObjectElement<T>> elements;
	private final DomainObjectElementObserver<T> elementObserver;
	private final DomainObjectElementConfigurer<T> elementConfigurer;
	private final View<T> baseView;
	private final Map<DomainObjectIdentity, DomainObjectProvider<T>> identityToProviders = new HashMap<>();
	private boolean disallowChanges = false;

	public DefaultDomainObjectCollection(Class<T> type, ObjectFactory objects, ProviderFactory providers) {
		this.knownElements = Cast.uncheckedCastBecauseOfTypeErasure(objects.domainObjectSet(KnownDomainObject.class));
		this.providers = providers;
		this.elements = Cast.uncheckedCastBecauseOfTypeErasure(objects.domainObjectContainer(DomainObjectElement.class, this::createElement));
		elementObserver = new DefaultDomainObjectElementObserver<>(knownElements);
		elementConfigurer = new DefaultDomainObjectElementConfigurer<>(elements, DomainObjectElementFilter.none());
		baseView = new DefaultView<>(elements, DomainObjectElementFilter.none(), providers);
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
		elementConfigurer.configureEach(action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		elementConfigurer.configureEach(type, action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Spec<? super S> spec, Action<? super S> action) {
		elementConfigurer.configureEach(type, spec, action);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		elementConfigurer.configureEach(spec, action);
	}

	@Override
	public int size() {
		return knownElements.size();
	}

	@Override
	public Provider<Collection<? extends T>> getElements() {
		return providers.provider(() -> elements.stream().map(DomainObjectElement::get).collect(ImmutableList.toImmutableList()));
	}

	@Override
	public <S extends T> View<S> withType(Class<S> type) {
		return baseView.withType(type);
	}

	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return baseView.map(mapper);
	}

	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return baseView.flatMap(mapper);
	}

	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return baseView.filter(spec);
	}

	@Override
	public DomainObjectCollection<T> disallowChanges() {
		disallowChanges = true;
		return this;
	}
}
