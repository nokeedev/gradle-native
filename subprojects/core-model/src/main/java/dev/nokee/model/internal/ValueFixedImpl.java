package dev.nokee.model.internal;

import dev.nokee.utils.ProviderUtils;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

final class ValueFixedImpl<T> implements Value<T> {
	private final Provider<T> provider;
	private T value;

	public ValueFixedImpl(T value) {
		this.value = value;
		this.provider = ProviderUtils.fixed(value);
	}

	@Override
	public T get() {
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
		value = mapper.transform(value);
		return this;
	}

	@Override
	public Provider<T> toProvider() {
		return provider;
	}
}
