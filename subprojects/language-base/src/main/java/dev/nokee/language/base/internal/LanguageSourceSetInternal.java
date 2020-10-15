package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;

public interface LanguageSourceSetInternal extends LanguageSourceSet {
	LanguageSourceSetIdentifier<?> getIdentifier();
}
