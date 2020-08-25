package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetFactory;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class AbstractLanguageSourceSetFactory<T extends LanguageSourceSet> implements LanguageSourceSetFactory<T> {
	private final ObjectFactory objectFactory;
	private final Class<? extends T> implementationType;

	protected AbstractLanguageSourceSetFactory(ObjectFactory objectFactory, Class<? extends T> implementationType) {
		this.objectFactory = objectFactory;
		this.implementationType = implementationType;
	}

	@Override
	public T create(DomainObjectIdentifier identifier) {
		return objectFactory.newInstance(implementationType, identifier);
	}
}
