package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public abstract class AbstractKnownDomainObject<TYPE, T extends TYPE> implements KnownDomainObject<T> {
	private final TypeAwareDomainObjectIdentifier<T> identifier;
	@EqualsAndHashCode.Exclude private final Provider<T> provider;
	@EqualsAndHashCode.Exclude private final DomainObjectConfigurer<TYPE> configurer;

	protected AbstractKnownDomainObject(TypeAwareDomainObjectIdentifier<T> identifier, Provider<T> provider, DomainObjectConfigurer<TYPE> configurer) {
		this.identifier = requireNonNull(identifier);
		this.provider = requireNonNull(provider);
		this.configurer = configurer;
	}

	public DomainObjectIdentifier getIdentifier() {
		return identifier;
	}

	public Class<T> getType() {
		return identifier.getType();
	}

	public void configure(Action<? super T> action) {
		configurer.configure(identifier, action);
	}

	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		return provider.map(transformer);
	}

	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return provider.flatMap(transformer);
	}
}
