package dev.nokee.model.internal;

import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

final class ValueSuppliedImpl<T> implements Value<T> {
	private final Provider<T> provider;
	private final Supplier<T> supplier;
	private final List<Transformer<? extends T, ? super T>> inPlaceMappers = new ArrayList<>();
	private T value = null;

	public ValueSuppliedImpl(Supplier<T> supplier) {
		this.supplier = supplier;
		this.provider = ProviderUtils.supplied(this::get);
	}

	@Override
	public T get() {
		if (value == null) {
			value = supplier.get();
			for (val inPlaceMapper : inPlaceMappers) {
				value = inPlaceMapper.transform(value);
			}
			inPlaceMappers.clear();
		}
		return value;
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> mapper) {
		return provider.map(mapper);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> mapper) {
		return provider.flatMap(mapper);
	}

	@Override
	public Value<T> mapInPlace(Transformer<? extends T, ? super T> mapper) {
		inPlaceMappers.add(mapper);
		return this;
	}

	@Override
	public Provider<T> toProvider() {
		return provider;
	}
}
