package dev.nokee.language.base;

// TODO: generalize to DomainObjectFactoryRegistry
public interface LanguageSourceSetFactoryRegistry {
	<U extends LanguageSourceSet> void registerFactory(Class<U> type, LanguageSourceSetFactory<? extends U> factory);
}
