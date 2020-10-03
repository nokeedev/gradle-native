package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

@EqualsAndHashCode
public abstract class AbstractDomainObjectProvider<TYPE, T extends TYPE> implements DomainObjectProvider<T> {
	private final TypeAwareDomainObjectIdentifier<T> identifier;
	@EqualsAndHashCode.Exclude private final Provider<T> provider;
	@EqualsAndHashCode.Exclude private final DomainObjectConfigurer<TYPE> configurer;

	protected AbstractDomainObjectProvider(TypeAwareDomainObjectIdentifier<T> identifier, Provider<T> provider, DomainObjectConfigurer<TYPE> configurer) {
		this.identifier = identifier;
		this.provider = provider;
		this.configurer = configurer;
	}

	@Override
	public DomainObjectIdentifier getIdentifier() {
		return identifier;
	}

	@Override
	public Class<T> getType() {
		return identifier.getType();
	}

	@Override
	public void configure(Action<? super T> action) {
		configurer.configure(identifier, action);
	}

	@Override
	public T get() {
		return provider.get();
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
