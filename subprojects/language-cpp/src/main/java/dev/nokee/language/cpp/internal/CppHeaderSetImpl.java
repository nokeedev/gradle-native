package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

class CppHeaderSetImpl extends AbstractLanguageSourceSet<CppHeaderSet> implements CppHeaderSet {
	@Inject
	public CppHeaderSetImpl(DomainObjectIdentifier identifier, ObjectFactory objects) {
		super(identifier, CppHeaderSet.class, objects);
	}

	@Override
	protected String getLanguageName() {
		return "C++ headers";
	}
}
