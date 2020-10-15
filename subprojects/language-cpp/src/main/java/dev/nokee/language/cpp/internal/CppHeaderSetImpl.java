package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.cpp.CppHeaderSet;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.language.base.internal.UTTypeUtils.asFilenamePattern;

public final class CppHeaderSetImpl extends AbstractLanguageSourceSet<CppHeaderSet> implements CppHeaderSet {
	public CppHeaderSetImpl(LanguageSourceSetIdentifier<?> identifier, ObjectFactory objects) {
		super(identifier, CppHeaderSet.class, objects);
		getFilter().include(asFilenamePattern(UTTypeCppHeader.INSTANCE));
	}
}
