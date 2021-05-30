package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.KnownDomainObject;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

@EqualsAndHashCode
final class DefaultKnownDomainObject<T> implements KnownDomainObject<T> {
	private final Class<T> type;

	DefaultKnownDomainObject(Class<T> type) {
		this.type = type;
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public void configure(Action<? super T> action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		throw new UnsupportedOperationException();
	}
}
