package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.Set;

public abstract class DefaultBinaryView<T extends Binary> implements BinaryView<T> {
	private final DomainObjectSet<T> delegate;

	@Inject
	public DefaultBinaryView(DomainObjectSet<T> delegate) {
		this.delegate = delegate;
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
		return Cast.uncheckedCast(getObjects().newInstance(DefaultBinaryView.class, delegate.withType(type)));
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return getProviders().provider(() -> ImmutableSet.copyOf(delegate));
	}

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract ObjectFactory getObjects();
}
