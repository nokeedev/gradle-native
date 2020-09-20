package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.NokeeMap;
import dev.nokee.model.internal.NokeeMapImpl;
import dev.nokee.model.internal.Value;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

import static dev.nokee.platform.base.internal.VariantViewImpl.byType;

public final class VariantCollection<T extends Variant> implements Realizable {
	private final NokeeMap<VariantIdentifier<T>, T> store;
	private final Class<T> elementType;

	// TODO: Make the distinction between public and implementation type
	public VariantCollection(Class<T> elementType, ObjectFactory objects) {
		this.store = new NokeeMapImpl<>(elementType, objects);
		this.elementType = elementType;
	}

	public VariantProvider<T> registerVariant(VariantIdentifier<T> identifier, VariantFactory<T> factory) {
		val value = Value.supplied(elementType, () -> factory.create(identifier.getFullName(), (BuildVariantInternal) identifier.getBuildVariant()));
		store.put(identifier, value);
		return new VariantProvider<>(identifier, value);
	}

	// TODO: I don't like that we have to pass in the viewElementType
	public <S extends Variant> VariantView<S> getAsView(Class<S> viewElementType) {
		Preconditions.checkArgument(viewElementType.isAssignableFrom(elementType), "element type of the view needs to be the same type or a supertype of the element of this collection");
		@SuppressWarnings("unchecked")
		Class<? extends T> type = (Class<? extends T>) viewElementType;
		return new VariantViewImpl(store.entrySet().filter(byType(type)));
	}

	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return store.values().getElements().map(ProviderUtils.filter(spec));
	}

	public void realize() {
		store.values().get(); // crappy way to realize
	}

	public VariantCollection<T> disallowChanges() {
		store.disallowChanges();
		return this;
	}

	// TODO: This may not be needed, the only place it's used should probably use public API
	public Set<T> get() {
		return ImmutableSet.copyOf(store.values().get());
	}

	public void whenElementKnown(Action<? super KnownVariant<T>> action) {
		store.forEach((k, v) -> {
			action.execute(new KnownVariant<>(k, v));
		});
	}
}
