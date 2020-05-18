package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Set;

public abstract class DefaultVariantView<T extends Variant> implements VariantView<T> {
	private final DomainObjectCollection<T> delegate;

	@Inject
	public DefaultVariantView(DomainObjectCollection<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		delegate.configureEach(t -> action.execute(t));
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return getProviders().provider(() -> ImmutableSet.copyOf(delegate));
	}

	@Override
	public <S> Provider<Set<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return getElements().map(new Transformer<Set<? extends S>, Set<? extends T>>() {
			@Override
			public Set<? extends S> transform(Set<? extends T> elements) {
				ImmutableSet.Builder<S> result = ImmutableSet.builder();
				for (T element : elements) {
					result.add(mapper.transform(element));
				}
				return result.build();
			}
		});
	}

	@Override
	public <S> Provider<Set<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return getElements().map(new Transformer<Set<? extends S>, Set<? extends T>>() {
			@Override
			public Set<? extends S> transform(Set<? extends T> elements) {
				ImmutableSet.Builder<S> result = ImmutableSet.builder();
				for (T element : elements) {
					result.addAll(mapper.transform(element));
				}
				return result.build();
			}
		});
	}

	@Inject
	protected abstract ProviderFactory getProviders();
}
