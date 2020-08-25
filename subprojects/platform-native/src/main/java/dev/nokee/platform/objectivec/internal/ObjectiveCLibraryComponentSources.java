package dev.nokee.platform.objectivec.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.internal.ComponentSourcesInternal;
import lombok.Getter;

import javax.inject.Inject;

public class ObjectiveCLibraryComponentSources implements ComponentSources {
	private final ComponentSourcesInternal delegate;
	@Getter private final ObjectiveCSourceSet objectiveCSources;
	@Getter private final CHeaderSet privateHeaders;
	@Getter private final CHeaderSet publicHeaders;

	@Inject
	public ObjectiveCLibraryComponentSources(ComponentSourcesInternal delegate) {
		this.delegate = delegate;
		this.objectiveCSources = delegate.register("objc", ObjectiveCSourceSet.class).from("src/main/objc");
		this.privateHeaders = delegate.register("headers", CHeaderSet.class).from("src/main/headers");
		this.publicHeaders = delegate.register("public", CHeaderSet.class).from("src/main/public");
		delegate.disallowChanges();
	}

	public SourceView<LanguageSourceSet> getAsView() {
		return delegate.getAsView();
	}
}
