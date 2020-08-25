package dev.nokee.platform.c.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.internal.ComponentSourcesInternal;
import lombok.Getter;

import javax.inject.Inject;

public class CLibraryComponentSources implements ComponentSources {
	private final ComponentSourcesInternal delegate;
	@Getter private final CSourceSet cSources;
	@Getter private final CHeaderSet privateHeaders;
	@Getter private final CHeaderSet publicHeaders;

	@Inject
	public CLibraryComponentSources(ComponentSourcesInternal delegate) {
		this.delegate = delegate;
		this.cSources = delegate.register("c", CSourceSet.class).from("src/main/c");
		this.privateHeaders = delegate.register("headers", CHeaderSet.class).from("src/main/headers");
		this.publicHeaders = delegate.register("public", CHeaderSet.class).from("src/main/public");
	}

	public SourceView<LanguageSourceSet> getAsView() {
		return delegate.getAsView();
	}
}
