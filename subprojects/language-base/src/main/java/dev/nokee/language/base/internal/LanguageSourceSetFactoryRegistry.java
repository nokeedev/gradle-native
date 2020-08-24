package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;

public interface LanguageSourceSetFactoryRegistry {
	<U extends LanguageSourceSet> void registerFactory(Class<U> type, LanguageSourceSetFactory<? extends U> factory);
}
