package dev.nokee.language.objectivecpp.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

class ObjectiveCppSourceSetImpl extends AbstractLanguageSourceSet<ObjectiveCppSourceSet> implements ObjectiveCppSourceSet {
	@Inject
	public ObjectiveCppSourceSetImpl(DomainObjectIdentifier identifier, ObjectFactory objects) {
		super(identifier, ObjectiveCppSourceSet.class, objects);
	}

	@Override
	protected String getLanguageName() {
		return "Objective-C++";
	}
}
