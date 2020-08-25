package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.internal.ComponentSourcesInternal;
import lombok.Getter;

import javax.inject.Inject;

public class ObjectiveCppLibraryComponentSources implements ComponentSources {
	private final ComponentSourcesInternal delegate;
	@Getter private final ObjectiveCppSourceSet objectiveCppSources;
	@Getter private final CppHeaderSet privateHeaders;
	@Getter private final CppHeaderSet publicHeaders;

	@Inject
	public ObjectiveCppLibraryComponentSources(ComponentSourcesInternal delegate) {
		this.delegate = delegate;
		this.objectiveCppSources = delegate.register("objc", ObjectiveCppSourceSet.class).from("src/main/objc");
		this.privateHeaders = delegate.register("headers", CppHeaderSet.class).from("src/main/headers");
		this.publicHeaders = delegate.register("public", CppHeaderSet.class).from("src/main/public");
		delegate.disallowChanges();
	}

	public SourceView<LanguageSourceSet> getAsView() {
		return delegate.getAsView();
	}
}
