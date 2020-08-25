package dev.nokee.platform.base.internal;

import dagger.Binds;
import dagger.Module;
import dev.nokee.language.base.internal.LanguageModule;

@Module(includes = {LanguageModule.class})
public interface ComponentModule {
	@Binds
	SourceViewFactory aSourceViewFactory(SourceViewFactoryImpl impl);

	@Binds
	ComponentSourcesInternal aComponentSources(ComponentSourcesInternalImpl impl);

	@Binds
	DomainObjectCollectionFactory aDomainObjectCollectionFactory(DomainObjectCollectionFactoryImpl impl);
}
