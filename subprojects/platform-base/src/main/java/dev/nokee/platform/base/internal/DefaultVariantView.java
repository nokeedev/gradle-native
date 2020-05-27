package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

public abstract class DefaultVariantView<T extends Variant> implements VariantView<T> {
	private final DomainObjectCollection<T> delegate;
	private final Realizable variants;

	@Inject
	public DefaultVariantView(DomainObjectCollection<T> delegate, Realizable variants) {
		this.delegate = delegate;
		this.variants = variants;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		delegate.configureEach(t -> action.execute(t));
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		delegate.configureEach(element -> {
			if (spec.isSatisfiedBy(element)) {
				action.execute(element);
			}
		});
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return getProviders().provider(() -> {
			variants.realize();
			return ImmutableSet.copyOf(delegate);
		});
	}

	@Override
	public Set<? extends T> get() {
		return getElements().get();
	}

	@Override
	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return getElements().map(new Transformer<List<? extends S>, Set<? extends T>>() {
			@Override
			public List<? extends S> transform(Set<? extends T> elements) {
				ImmutableList.Builder<S> result = ImmutableList.builder();
				for (T element : elements) {
					result.add(mapper.transform(element));
				}
				return result.build();
			}
		});
	}

	@Override
	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return getElements().map(new Transformer<List<? extends S>, Set<? extends T>>() {
			@Override
			public List<? extends S> transform(Set<? extends T> elements) {
				ImmutableList.Builder<S> result = ImmutableList.builder();
				for (T element : elements) {
					result.addAll(mapper.transform(element));
				}
				return result.build();
			}
		});
	}

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return flatMap(new Transformer<Iterable<? extends T>, T>() {
			@Override
			public Iterable<? extends T> transform(T t) {
				if (spec.isSatisfiedBy(t)) {
					return ImmutableList.of(t);
				}
				return ImmutableList.of();
			}
		});
	}
}
