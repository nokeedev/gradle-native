package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.nokee.internal.Cast;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.Set;

public abstract class DefaultBinaryView<T extends Binary> extends AbstractView<T> implements BinaryView<T> {
	private final Class<T> elementType;
	private final DomainObjectSet<T> delegate;
	private final Realizable variants;

	@Inject
	public DefaultBinaryView(Class<T> elementType, DomainObjectSet<T> delegate, Realizable variants) {
		this.elementType = elementType;
		this.delegate = delegate;
		this.variants = variants;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		Preconditions.checkArgument(action != null, "configure each action for binary view must not be null");
		delegate.configureEach(action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		Preconditions.checkArgument(type != null, "configure each type for binary view must not be null");
		Preconditions.checkArgument(action != null, "configure each action for binary view must not be null");
		delegate.withType(type).configureEach(action);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		Preconditions.checkArgument(spec != null, "configure each spec for binary view must not be null");
		Preconditions.checkArgument(action != null, "configure each action for binary view must not be null");
		delegate.configureEach(element -> {
			if (spec.isSatisfiedBy(element)) {
				action.execute(element);
			}
		});
	}

	@Override
	public <S extends T> BinaryView<S> withType(Class<S> type) {
		Preconditions.checkArgument(type != null, "binary view subview type must not be null");
		if (elementType.equals(type)) {
			return Cast.uncheckedCast("view types are the same", this);
		}
		return Cast.uncheckedCast("of type erasure", getObjects().newInstance(DefaultBinaryView.class, type, delegate.withType(type), variants));
	}

	@Override
	public Set<? extends T> get() {
		variants.realize();
		return ImmutableSet.copyOf(delegate);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	protected String getDisplayName() {
		return "binary view";
	}
}
