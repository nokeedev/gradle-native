package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Transformer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

public abstract class DefaultBinaryView<T extends Binary> implements BinaryView<T> {
	private final DomainObjectSet<T> delegate;
	private final Realizable variants;

	@Inject
	public DefaultBinaryView(DomainObjectSet<T> delegate, Realizable variants) {
		this.delegate = delegate;
		this.variants = variants;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		delegate.configureEach(action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		delegate.withType(type).configureEach(action);
	}

	@Override
	public <S extends T> BinaryView<S> withType(Class<S> type) {
		return Cast.uncheckedCast(getObjects().newInstance(DefaultBinaryView.class, delegate.withType(type), variants));
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return getProviders().provider(() -> {
			variants.realize();
			return ImmutableSet.copyOf(delegate);
		});
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

	@Inject
	protected abstract ObjectFactory getObjects();
}
