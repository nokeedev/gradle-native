package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.Value;
import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.utils.TransformerUtils;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

public final class DomainObjectProviderValueAdapter<T> implements DomainObjectProvider<T> {
	private final DomainObjectIdentifier identifier;
	private final Value<? extends T> value;

	public DomainObjectProviderValueAdapter(DomainObjectIdentifier identifier, Value<? extends T> value) {
		this.identifier = identifier;
		this.value = value;
	}

	@Override
	public void configure(Action<? super T> action) {
		value.mapInPlace(TransformerUtils.configureInPlace(action));
	}

	@Override
	public T get() {
		return value.get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		return (Class<T>) value.getType();
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
		return identifier;
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		return value.map(transformer);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return value.flatMap(transformer);
	}
}
