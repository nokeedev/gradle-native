package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.Value;
import dev.nokee.platform.base.KnownDomainObject;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import static dev.nokee.utils.TransformerUtils.configureInPlace;

final class KnownDomainObjectValueAdapter<T> implements KnownDomainObject<T> {
	private final DomainObjectIdentifier identifier;
	private final Value<? extends T> value;

	public KnownDomainObjectValueAdapter(DomainObjectIdentifier identifier, Value<? extends T> value) {
		this.identifier = identifier;
		this.value = value;
	}

	@Override
	public void configure(Action<? super T> action) {
		value.mapInPlace(configureInPlace(action));
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
