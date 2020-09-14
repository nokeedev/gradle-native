package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.DomainObjectProvider;
import lombok.Getter;
import lombok.ToString;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

@ToString
public class DefaultDomainObjectProvider<I> implements DomainObjectProvider<I> {
	@Getter private final DomainObjectIdentifier identity;
	@Getter private final Class<I> type;
	private final NamedDomainObjectProvider<DomainObjectElement<I>> delegate;

	@Inject
	public DefaultDomainObjectProvider(DomainObjectIdentifier identity, Class<I> type, NamedDomainObjectProvider<DomainObjectElement<I>> delegate) {
		this.identity = identity;
		this.type = type;
		this.delegate = delegate;
	}

	@Override
	public I get() {
		return delegate.get().get();
	}

	@Override
	public void configure(Action<? super I> action) {
		delegate.configure(it -> action.execute(it.get()));
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super I> transformer) {
		return delegate.map(it -> transformer.transform(it.get()));
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super I> transformer) {
		return delegate.flatMap(it -> transformer.transform(it.get()));
	}
}
