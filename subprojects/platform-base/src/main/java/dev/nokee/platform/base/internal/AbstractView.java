package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

public abstract class AbstractView<T> {
	@Inject
	protected abstract ProviderFactory getProviders();

	protected abstract Set<? extends T> get();

	public Provider<Set<? extends T>> getElements() {
		return getProviders().provider(this::get);
	}

	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		Preconditions.checkArgument(mapper != null, "map mapper for %s must not be null", getDisplayName());
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

	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		Preconditions.checkArgument(mapper != null, "flatMap mapper for %s must not be null", getDisplayName());
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

	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		Preconditions.checkArgument(spec != null, "filter spec for %s must not be null", getDisplayName());
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

	protected abstract String getDisplayName();
}
