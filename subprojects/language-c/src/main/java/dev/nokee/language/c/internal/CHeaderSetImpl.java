package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

class CHeaderSetImpl extends AbstractLanguageSourceSet<CHeaderSet> implements CHeaderSet {
	@Inject
	public CHeaderSetImpl(DomainObjectIdentifier identifier, ObjectFactory objectFactory) {
		super(identifier, CHeaderSet.class, objectFactory);
	}

	@Override
	protected String getLanguageName() {
		return "C headers";
	}
}
