package dev.nokee.language.swift.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

class SwiftSourceSetImpl extends AbstractLanguageSourceSet<SwiftSourceSet> implements SwiftSourceSet {
	@Inject
	public SwiftSourceSetImpl(DomainObjectIdentifier identifier, ObjectFactory objects) {
		super(identifier, SwiftSourceSet.class, objects);
	}
}
