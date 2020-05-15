package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.DomainObjectSet;
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

	@Inject
	protected abstract ProviderFactory getProviders();

//	public static <T extends Variant> DefaultVariantView<T> of(DomainObjectSet<T> delegate) {
//		return new DefaultVariantView<>(delegate);
//	}
}
