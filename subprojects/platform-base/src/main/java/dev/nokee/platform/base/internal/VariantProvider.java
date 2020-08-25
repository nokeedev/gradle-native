package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.platform.base.Variant;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public class VariantProvider<T extends Variant> {
	@Getter private final BuildVariantInternal buildVariant;
	@Getter private final Class<? extends T> type;
	@Getter private final DomainObjectProvider<T> delegate; // TODO: Do not expose this field in public API

	@Inject
	public VariantProvider(BuildVariantInternal buildVariant, DomainObjectProvider<T> delegate) {
		this.buildVariant = buildVariant;
		this.type = delegate.getType();
		this.delegate = delegate;
	}

	public T get() {
		return delegate.get();
	}

	public void configure(Action<? super T> action) {
		delegate.configure(action);
	}

	public <S> Provider<S> map(Transformer<? extends S, ? super T> mapper) {
		return delegate.map(mapper);
	}

	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> mapper) {
		return delegate.flatMap(mapper);
	}
}
