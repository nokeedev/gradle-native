package dev.nokee.language.cpp.internal;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import dev.nokee.language.base.internal.LanguageSourceSetFactory;
import dev.nokee.language.base.internal.LanguageSourceSetFactoryImpl;
import dev.nokee.language.cpp.CppSourceSet;
import org.gradle.api.model.ObjectFactory;

@Module
public interface CppSourceSetModule {
	@Provides
	@IntoMap
	@ClassKey(CppSourceSet.class)
	static LanguageSourceSetFactory<?> aCppSourceSetFactory(ObjectFactory objectFactory) {
		return new LanguageSourceSetFactoryImpl<CppSourceSet>(objectFactory, CppSourceSetImpl.class);
	}
}
