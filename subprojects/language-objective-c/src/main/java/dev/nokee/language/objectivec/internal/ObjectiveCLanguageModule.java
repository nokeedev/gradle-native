package dev.nokee.language.objectivec.internal;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import dev.nokee.language.base.internal.LanguageSourceSetFactory;
import dev.nokee.language.base.internal.LanguageSourceSetFactoryImpl;
import dev.nokee.language.c.internal.CHeaderSetModule;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import org.gradle.api.model.ObjectFactory;

@Module(includes = {CHeaderSetModule.class})
public interface ObjectiveCLanguageModule {
	@Provides
	@IntoMap
	@ClassKey(ObjectiveCSourceSet.class)
	static LanguageSourceSetFactory<?> aObjectiveCSourceSetFactory(ObjectFactory objectFactory) {
		return new LanguageSourceSetFactoryImpl<ObjectiveCSourceSet>(objectFactory, ObjectiveCSourceSetImpl.class);
	}
}
