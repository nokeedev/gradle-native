package dev.nokee.platform.cpp.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.internal.ComponentSourcesInternal;
import lombok.Getter;

import javax.inject.Inject;

public class CppLibraryComponentSources implements ComponentSources {
	private final ComponentSourcesInternal delegate;
	@Getter private final CppSourceSet cppSources;
	@Getter private final CppHeaderSet privateHeaders;
	@Getter private final CppHeaderSet publicHeaders;

	@Inject
	public CppLibraryComponentSources(ComponentSourcesInternal delegate) {
		this.delegate = delegate;
		this.cppSources = delegate.register("cpp", CppSourceSet.class).from("src/main/cpp");
		this.privateHeaders = delegate.register("headers", CppHeaderSet.class).from("src/main/headers");
		this.publicHeaders = delegate.register("public", CppHeaderSet.class).from("src/main/public");
		delegate.disallowChanges();
	}

	public SourceView<LanguageSourceSet> getAsView() {
		return delegate.getAsView();
	}
}
