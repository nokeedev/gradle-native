package dev.nokee.language.objectivec.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

class ObjectiveCSourceSetImpl extends AbstractLanguageSourceSet<ObjectiveCSourceSet> implements ObjectiveCSourceSet {
	@Inject
	public ObjectiveCSourceSetImpl(DomainObjectIdentifier identifier, ObjectFactory objects) {
		super(identifier, ObjectiveCSourceSet.class, objects);
	}

	@Override
	protected String getLanguageName() {
		return "Objective-C";
	}
}
