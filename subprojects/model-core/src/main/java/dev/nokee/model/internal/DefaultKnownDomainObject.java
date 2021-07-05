package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.core.ModelProjection;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.concurrent.Callable;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class DefaultKnownDomainObject<T> implements KnownDomainObject<T>, Callable<Object> {
	private final Class<T> type;
	private final ModelProjection projection;

	DefaultKnownDomainObject(Class<T> type, ModelProjection projection) {
		this.type = type;
		this.projection = projection;
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
		return new DomainObjectIdentifier() {};
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public KnownDomainObject<T> configure(Action<? super T> action) {
		projection.whenRealized(type, action);
		return this;
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		requireNonNull(transformer);
		return projection.as(type).map(transformer);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		requireNonNull(transformer);
		return projection.as(type).flatMap(transformer);
	}

	@Override
	public Object call() throws Exception {
		return projection.as(type);
	}
}
