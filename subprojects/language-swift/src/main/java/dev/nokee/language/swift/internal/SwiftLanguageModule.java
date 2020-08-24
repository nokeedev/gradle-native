package dev.nokee.language.swift.internal;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import dev.nokee.language.base.internal.LanguageSourceSetFactory;
import dev.nokee.language.base.internal.LanguageSourceSetFactoryImpl;
import dev.nokee.language.swift.SwiftSourceSet;
import org.gradle.api.model.ObjectFactory;

@Module
public interface SwiftLanguageModule {
	@Provides
	@IntoMap
	@ClassKey(SwiftSourceSet.class)
	static LanguageSourceSetFactory<?> aSwiftSourceSetFactory(ObjectFactory objectFactory) {
		return new LanguageSourceSetFactoryImpl<SwiftSourceSet>(objectFactory, SwiftSourceSetImpl.class);
	}
}
