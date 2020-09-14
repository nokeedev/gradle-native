package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.platform.base.KnownDomainObject;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

@ToString
@EqualsAndHashCode
public class DefaultKnownDomainObject<T> implements KnownDomainObject<T> {
	private final DomainObjectProvider<T> provider;

	@Inject
	public DefaultKnownDomainObject(DomainObjectProvider<T> provider) {
		this.provider = provider;
	}

	@Override
	public void configure(Action<? super T> action) {
		provider.configure(action);
	}

	@Override
	public Class<T> getType() {
		return provider.getType();
	}

	@Override
	public DomainObjectIdentifier getIdentity() {
		return provider.getIdentity();
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		return provider.map(transformer);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return provider.flatMap(transformer);
	}
}
