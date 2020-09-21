package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.NokeeCollection;
import dev.nokee.model.internal.NokeeMap;
import dev.nokee.model.internal.Value;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.utils.ProviderUtils;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static dev.nokee.model.internal.NokeeMapImpl.byType;
import static dev.nokee.model.internal.NokeeMapImpl.configureValue;
import static dev.nokee.utils.ActionUtils.onlyIf;
import static java.util.Objects.requireNonNull;

public final class BinaryViewImpl<T extends Binary> implements BinaryView<T> {
	private final NokeeCollection<NokeeMap.Entry<BinaryIdentifier<? extends Binary>, T>> store;
	private final List<Runnable> realizingListener = new ArrayList<>();

	public BinaryViewImpl(NokeeCollection<NokeeMap.Entry<BinaryIdentifier<? extends Binary>, T>> store) {
		this.store = requireNonNull(store);
	}

	@Override
	public void configureEach(Action<? super T> action) {
		store.forEach(configureValue(action));
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		store.forEach(configureValue(onlyIf(type, action)));
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		store.forEach(configureValue(onlyIf(spec, action)));
	}

	@Override
	public <S extends T> BinaryView<S> withType(Class<S> type) {
		return new BinaryViewImpl(store.filter(byType(type)));
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return ProviderUtils.supplied(this::get);
	}

	@Override
	public Set<? extends T> get() {
		realizingListener.forEach(Runnable::run);
		return store.get().stream().map(NokeeMap.Entry::getValue).map(Value::get).collect(ImmutableSet.toImmutableSet());
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

	public void onRealize(Runnable runnable) {
		realizingListener.add(runnable);
	}
}
