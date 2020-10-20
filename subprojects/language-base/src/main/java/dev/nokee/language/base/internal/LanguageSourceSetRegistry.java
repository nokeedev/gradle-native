package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.DomainObjectCreated;
import dev.nokee.model.internal.DomainObjectDiscovered;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import org.gradle.api.Action;

// FIXME: Test
public final class LanguageSourceSetRegistry {
	private final DomainObjectEventPublisher eventPublisher;
	private final LanguageSourceSetInstantiator languageSourceSetInstantiator;

	public LanguageSourceSetRegistry(DomainObjectEventPublisher eventPublisher, LanguageSourceSetInstantiator languageSourceSetInstantiator) {
		this.eventPublisher = eventPublisher;
		this.languageSourceSetInstantiator = languageSourceSetInstantiator;
	}

	public <S extends LanguageSourceSet> S create(LanguageSourceSetIdentifier<S> identifier) {
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier));
		S result = languageSourceSetInstantiator.newInstance(identifier, identifier.getType());
		eventPublisher.publish(new DomainObjectCreated<>(identifier, result));
		return result;
	}

	public <S extends LanguageSourceSet> S create(LanguageSourceSetIdentifier<S> identifier, Action<? super S> action) {
		S result = create(identifier);
		action.execute(result);
		return result;
	}
}
