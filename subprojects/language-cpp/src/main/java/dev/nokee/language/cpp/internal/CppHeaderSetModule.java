package dev.nokee.language.cpp.internal;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import dev.nokee.language.base.internal.LanguageSourceSetFactory;
import dev.nokee.language.base.internal.LanguageSourceSetFactoryImpl;
import dev.nokee.language.cpp.CppHeaderSet;
import org.gradle.api.model.ObjectFactory;

@Module
public interface CppHeaderSetModule {
	@Provides
	@IntoMap
	@ClassKey(CppHeaderSet.class)
	static LanguageSourceSetFactory<?> aCppHeaderSetFactory(ObjectFactory objectFactory) {
		return new LanguageSourceSetFactoryImpl<CppHeaderSet>(objectFactory, CppHeaderSetImpl.class);
	}
}
