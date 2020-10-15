package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.c.CSourceSet;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.language.base.internal.UTTypeUtils.asFilenamePattern;

public final class CSourceSetImpl extends AbstractLanguageSourceSet<CSourceSet> implements CSourceSet {
	public CSourceSetImpl(ObjectFactory objects) {
		super(CSourceSet.class, objects);
		getFilter().include(asFilenamePattern(UTTypeCSource.INSTANCE));
	}
}
