package dev.nokee.language.swift.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.swift.SwiftSourceSet;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.language.base.internal.UTTypeUtils.asFilenamePattern;

public final class SwiftSourceSetImpl extends AbstractLanguageSourceSet<SwiftSourceSet> implements SwiftSourceSet {
	public SwiftSourceSetImpl(LanguageSourceSetIdentifier<?> identifier, ObjectFactory objects) {
		super(identifier, SwiftSourceSet.class, objects);
		getFilter().include(asFilenamePattern(UTTypeSwiftSource.INSTANCE));
	}
}
