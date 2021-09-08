package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelProperty;
import dev.nokee.model.streams.ModelStream;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

final class DefaultModelProperty<T> implements ModelProperty<T>, Callable<Object> {
	final ModelObject<T> delegate;

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
	public boolean instanceOf(Class<?> type) {
		return delegate.instanceOf(type);
	}

	@Override
	public Optional<ModelObject<?>> getParent() {
		return delegate.getParent();
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
	public ModelStream<ModelProperty<?>> getProperties() {
		return delegate.getProperties();
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

	@Override
	public <S> ModelProperty<S> as(Class<S> type) {
		return new DefaultModelProperty<>(delegate.as(type));
	}

	@Override
	public Object call() throws Exception {
		return ((DefaultModelObject<T>) delegate).asProvider();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof DefaultModelObject) {
			return delegate.equals(o);
		} else if (o instanceof DefaultModelProperty) {
			DefaultModelProperty<?> that = (DefaultModelProperty<?>) o;
			return Objects.equals(delegate, that.delegate);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(delegate);
	}
}
