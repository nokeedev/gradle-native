package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.NokeeCollection;
import dev.nokee.model.internal.NokeeMap;
import dev.nokee.model.internal.Value;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.utils.ProviderUtils;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

import static dev.nokee.utils.ActionUtils.onlyIf;
import static dev.nokee.utils.TransformerUtils.configureInPlace;
import static java.util.Objects.requireNonNull;

public final class VariantViewImpl<T extends Variant> implements VariantViewInternal<T> {
	private final NokeeCollection<NokeeMap.Entry<VariantIdentifier<T>, T>> store;

	public VariantViewImpl(NokeeCollection<NokeeMap.Entry<VariantIdentifier<T>, T>> store) {
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
	public <S extends T> VariantView<S> withType(Class<S> type) {
		return new VariantViewImpl(store.filter(byType(type)));
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return ProviderUtils.supplied(this::get);
	}

	@Override
	public Set<? extends T> get() {
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

	@Override
	public void whenElementKnown(Action<? super KnownVariant<T>> action) {
		store.whenElementAdded(asKnownVariant(action));
	}

	public static <T extends Variant> Action<Value<NokeeMap.Entry<VariantIdentifier<T>, T>>> asKnownVariant(Action<? super KnownVariant<T>> action) {
		return new AsKnownVariantAction<>(action);
	}

	@EqualsAndHashCode
	private static final class AsKnownVariantAction<T extends Variant> implements Action<Value<NokeeMap.Entry<VariantIdentifier<T>, T>>> {
		private final Action<? super KnownVariant<T>> action;

		public AsKnownVariantAction(Action<? super KnownVariant<T>> action) {
			this.action = action;
		}

		@Override
		public void execute(Value<NokeeMap.Entry<VariantIdentifier<T>, T>> t) {
			action.execute(new KnownVariant<>(t.get().getKey(), t.get().getValue()));
		}

		@Override
		public String toString() {
			return "VariantViewImpl.asKnownVariant(" + action + ")";
		}
	}

	public static <T extends Variant> Action<NokeeMap.Entry<VariantIdentifier<T>, T>> configureValue(Action<? super T> action) {
		return new ConfigureValueAction<>(action);
	}

	@EqualsAndHashCode
	private static final class ConfigureValueAction<T extends Variant> implements Action<NokeeMap.Entry<VariantIdentifier<T>, T>> {
		private final Action<? super T> action;

		public ConfigureValueAction(Action<? super T> action) {
			this.action = action;
		}

		@Override
		public void execute(NokeeMap.Entry<VariantIdentifier<T>, T> t) {
			t.getValue().mapInPlace(configureInPlace(action));
		}

		@Override
		public String toString() {
			return "VariantViewImpl.configureValue(" + action + ")";
		}
	}

	public static <T extends Variant> Spec<NokeeMap.Entry<VariantIdentifier<T>, T>> byType(Class<? extends T> type) {
		return new ByTypeSpec<>(type);
	}

	@EqualsAndHashCode
	private static final class ByTypeSpec<T extends Variant> implements Spec<NokeeMap.Entry<VariantIdentifier<T>, T>> {
		private final Class<? extends T> type;

		public ByTypeSpec(Class<? extends T> type) {
			this.type = type;
		}

		@Override
		public boolean isSatisfiedBy(NokeeMap.Entry<VariantIdentifier<T>, T> t) {
			return type.isAssignableFrom(t.getValue().getType());
		}

		@Override
		public String toString() {
			return "VariantViewImpl.byType(" + type.getSimpleName() + ")";
		}
	}
}
