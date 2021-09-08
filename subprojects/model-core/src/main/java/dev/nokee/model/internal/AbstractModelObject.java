package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.TypeAwareModelProjection;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import static java.util.Objects.requireNonNull;

abstract class AbstractModelObject<T> implements ModelObject<T> {
	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		requireNonNull(transformer);
		return asProvider().map(transformer);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		requireNonNull(transformer);
		return asProvider().flatMap(transformer);
	}

	public TypeAwareModelProjection<T> getProjection() {
		throw new UnsupportedOperationException("No projection for this object.");
	}

	public Provider<T> asProvider() {
		return getProjection().as(getType());
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
		return new DefaultDomainObjectIdentifier(getProjection());
	}

	@Override
	public Class<T> getType() {
		return getProjection().getType();
	}

	@Override
	public ModelObject<T> configure(Action<? super T> action) {
		getProjection().whenRealized(action);
		return this;
	}
}
