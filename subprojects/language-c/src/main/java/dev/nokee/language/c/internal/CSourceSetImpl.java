package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.language.base.internal.UTTypeUtils.asFilenamePattern;

class CSourceSetImpl extends AbstractLanguageSourceSet<CSourceSet> implements CSourceSet {
	@Inject
	public CSourceSetImpl(DomainObjectIdentifier identifier, ObjectFactory objectFactory) {
		super(identifier, CSourceSet.class, objectFactory);
		getFilter().include(asFilenamePattern(UTTypeCSource.INSTANCE));
	}
}
