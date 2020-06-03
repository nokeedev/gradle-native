package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.nokee.internal.Cast;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.View;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.Set;

public abstract class DefaultVariantView<T extends Variant> extends AbstractView<T> implements VariantView<T> {
	private final Class<T> elementType;
	private final DomainObjectCollection<T> delegate;
	private final Realizable variants;

	@Inject
	public DefaultVariantView(Class<T> elementType, DomainObjectCollection<T> delegate, Realizable variants) {
		this.elementType = elementType;
		this.delegate = delegate;
		this.variants = variants;
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void configureEach(Action<? super T> action) {
		Preconditions.checkArgument(action != null, "configure each action for variant view must not be null");
		delegate.configureEach(t -> action.execute(t));
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		Preconditions.checkArgument(action != null, "configure each action for variant view must not be null");
		delegate.withType(type).configureEach(action);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		Preconditions.checkArgument(action != null, "configure each action for variant view must not be null");
		delegate.configureEach(element -> {
			if (spec.isSatisfiedBy(element)) {
				action.execute(element);
			}
		});
	}

	@Override
	public <S extends T> VariantView<S> withType(Class<S> type) {
		Preconditions.checkArgument(type != null, "variant view subview type must not be null");
		if (elementType.equals(type)) {
			return Cast.uncheckedCast("view types are the same", this);
		}
		return Cast.uncheckedCast("of type erasure", getObjects().newInstance(DefaultVariantView.class, type, delegate.withType(type), variants));
	}

	@Override
	public Set<? extends T> get() {
		variants.realize();
		return ImmutableSet.copyOf(delegate);
	}

	@Override
	protected String getDisplayName() {
		return "variant view";
	}
}
