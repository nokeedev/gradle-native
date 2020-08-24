package dev.nokee.language.c.internal;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import dev.nokee.language.base.internal.LanguageSourceSetFactory;
import dev.nokee.language.base.internal.LanguageSourceSetFactoryImpl;
import dev.nokee.language.c.CSourceSet;
import org.gradle.api.model.ObjectFactory;

@Module
public interface CSourceSetModule {
	@Provides
	@IntoMap
	@ClassKey(CSourceSet.class)
	static LanguageSourceSetFactory<?> aCSourceSetFactory(ObjectFactory objectFactory) {
		return new LanguageSourceSetFactoryImpl<CSourceSet>(objectFactory, CSourceSetImpl.class);
	}
}
