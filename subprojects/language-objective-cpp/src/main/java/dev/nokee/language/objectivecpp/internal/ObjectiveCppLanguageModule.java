package dev.nokee.language.objectivecpp.internal;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import dev.nokee.language.base.LanguageSourceSetFactory;
import dev.nokee.language.base.internal.LanguageSourceSetFactoryImpl;
import dev.nokee.language.cpp.internal.CppHeaderSetModule;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import org.gradle.api.model.ObjectFactory;

@Module(includes = {CppHeaderSetModule.class})
public interface ObjectiveCppLanguageModule {
	@Provides
	@IntoMap
	@ClassKey(ObjectiveCppSourceSet.class)
	static LanguageSourceSetFactory<?> aObjectiveCppSourceSetFactory(ObjectFactory objectFactory) {
		return new LanguageSourceSetFactoryImpl<ObjectiveCppSourceSet>(objectFactory, ObjectiveCppSourceSetImpl.class);
	}
}
