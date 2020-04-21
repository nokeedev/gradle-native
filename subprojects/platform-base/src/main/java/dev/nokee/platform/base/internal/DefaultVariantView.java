package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;

import javax.inject.Inject;

public class DefaultVariantView<T extends Variant> implements VariantView<T> {
	private final DomainObjectSet<T> delegate;

	@Inject
	public DefaultVariantView(DomainObjectSet<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		delegate.configureEach(t -> action.execute(t));
	}

//	public static <T extends Variant> DefaultVariantView<T> of(DomainObjectSet<T> delegate) {
//		return new DefaultVariantView<>(delegate);
//	}
}
