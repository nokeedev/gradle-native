package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.AbstractLanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.c.CHeaderSet;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.language.base.internal.UTTypeUtils.asFilenamePattern;

public final class CHeaderSetImpl extends AbstractLanguageSourceSet<CHeaderSet> implements CHeaderSet {
	public CHeaderSetImpl(LanguageSourceSetIdentifier<?> identifier, ObjectFactory objects) {
		super(identifier, CHeaderSet.class, objects);
		getFilter().include(asFilenamePattern(UTTypeCHeader.INSTANCE));
	}
}
