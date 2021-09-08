package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelProperty;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.function.Consumer;

@EqualsAndHashCode
final class DefaultModelProperty<T> implements ModelProperty<T> {
	@EqualsAndHashCode.Include private final ModelObject<T> delegate;

	public DefaultModelProperty(ModelObject<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
		return delegate.getIdentifier();
	}

	@Override
	public Class<T> getType() {
		return delegate.getType();
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		return delegate.map(transformer);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return delegate.flatMap(transformer);
	}

	@Override
	public <S> ModelProperty<S> newProperty(Object identity, Class<S> type) {
		return delegate.newProperty(identity, type);
	}

	@Override
	public <S> ModelProperty<S> property(String name, Class<S> type) {
		return delegate.property(name, type);
	}

	@Override
	public ModelProperty<T> configure(Action<? super T> action) {
		delegate.configure(action);
		return this;
	}

	@Override
	public ModelProperty<T> configure(Consumer<? super ModelObject<? extends T>> action) {
		action.accept(this);
		return this;
	}
}
