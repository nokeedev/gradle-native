package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.specs.Spec;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.Set;

public abstract class DefaultBinaryView<T extends Binary> extends AbstractView<T> implements BinaryView<T> {
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
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		delegate.configureEach(element -> {
			if (spec.isSatisfiedBy(element)) {
				action.execute(element);
			}
		});
	}

	@Override
	public <S extends T> BinaryView<S> withType(Class<S> type) {
		return Cast.uncheckedCast(getObjects().newInstance(DefaultBinaryView.class, delegate.withType(type), variants));
	}

	@Override
	public Set<? extends T> get() {
		variants.realize();
		return ImmutableSet.copyOf(delegate);
	}

	@Inject
	protected abstract ObjectFactory getObjects();
}
