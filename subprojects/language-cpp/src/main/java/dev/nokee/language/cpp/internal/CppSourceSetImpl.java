package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

class CppSourceSetImpl extends AbstractLanguageSourceSet<CppSourceSet> implements CppSourceSet {
	@Inject
	public CppSourceSetImpl(DomainObjectIdentifier identifier, ObjectFactory objects) {
		super(identifier, CppSourceSet.class, objects);
	}

	@Override
	protected String getLanguageName() {
		return "C++";
	}
}
