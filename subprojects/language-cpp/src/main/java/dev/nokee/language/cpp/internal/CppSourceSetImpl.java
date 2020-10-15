package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.language.base.internal.UTTypeUtils.asFilenamePattern;

public final class CppSourceSetImpl extends AbstractLanguageSourceSet<CppSourceSet> implements CppSourceSet {
	public CppSourceSetImpl(ObjectFactory objects) {
		super(CppSourceSet.class, objects);
		getFilter().include(asFilenamePattern(UTTypeCppSource.INSTANCE));
	}
}
