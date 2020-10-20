package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.model.ObjectFactory;

public final class LanguageSourceSetImpl extends AbstractLanguageSourceSet<LanguageSourceSet> implements LanguageSourceSet {
	public LanguageSourceSetImpl(LanguageSourceSetIdentifier<?> identifier, ObjectFactory objects) {
		super(identifier, LanguageSourceSet.class, objects);
	}
}
