package dev.nokee.language.c.internal;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import dev.nokee.language.base.internal.LanguageSourceSetFactory;
import dev.nokee.language.base.internal.LanguageSourceSetFactoryImpl;
import dev.nokee.language.c.CHeaderSet;
import org.gradle.api.model.ObjectFactory;

@Module
public interface CHeaderSetModule {
	@Provides
	@IntoMap
	@ClassKey(CHeaderSet.class)
	static LanguageSourceSetFactory<?> aCHeaderSetFactory(ObjectFactory objectFactory) {
		return new LanguageSourceSetFactoryImpl<CHeaderSet>(objectFactory, CHeaderSetImpl.class);
	}
}
