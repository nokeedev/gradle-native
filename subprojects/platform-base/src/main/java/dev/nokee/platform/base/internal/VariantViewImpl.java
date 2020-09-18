package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.NokeeCollection;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.utils.ProviderUtils;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

import static dev.nokee.utils.ActionUtils.onlyIf;
import static dev.nokee.utils.SpecUtils.byType;
import static java.util.Objects.requireNonNull;

public final class VariantViewImpl<T extends Variant> implements VariantView<T> {
	private final NokeeCollection<T> store;

	public VariantViewImpl(NokeeCollection<T> store) {
		this.store = requireNonNull(store);
	}

	@Override
	public void configureEach(Action<? super T> action) {
		store.forEach(action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		store.forEach(onlyIf(type, action));
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		store.forEach(onlyIf(spec, action));
	}

	@Override
	public <S extends T> VariantView<S> withType(Class<S> type) {
		return new VariantViewImpl<>(store.filter(byType(type)));
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return ProviderUtils.supplied(this::get);
	}

	@Override
	public Set<? extends T> get() {
		return ImmutableSet.copyOf(store.get());
	}

	@Override
	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return getElements().map(ProviderUtils.map(mapper));
	}

	@Override
	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return getElements().map(ProviderUtils.flatMap(mapper));
	}

	@Override
	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return getElements().map(ProviderUtils.filter(spec));
	}
}
