package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.Set;

public abstract class DefaultVariantView<T extends Variant> extends AbstractView<T> implements VariantView<T> {
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
	public Set<? extends T> get() {
		variants.realize();
		return ImmutableSet.copyOf(delegate);
	}
}
