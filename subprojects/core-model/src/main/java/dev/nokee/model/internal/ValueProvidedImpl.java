package dev.nokee.model.internal;

import dev.nokee.utils.ProviderUtils;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

final class ValueProvidedImpl<T> implements Value<T> {
	private final NamedDomainObjectProvider<T> provider;

	public ValueProvidedImpl(NamedDomainObjectProvider<T> provider) {
		this.provider = provider;
	}

	@Override
	public T get() {
		return provider.get();
	}

	@Override
	public Class<T> getType() {
		return ProviderUtils.getType(provider).orElse(null);
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
		provider.configure(mapper::transform);
		return this;
	}

	@Override
	public Provider<T> toProvider() {
		return provider;
	}
}
