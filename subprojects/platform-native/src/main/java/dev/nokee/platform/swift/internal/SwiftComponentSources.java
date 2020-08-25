package dev.nokee.platform.swift.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.internal.ComponentSourcesInternal;
import lombok.Getter;

import javax.inject.Inject;

public class SwiftComponentSources implements ComponentSources {
	private final ComponentSourcesInternal delegate;
	@Getter private final SwiftSourceSet swiftSources;

	@Inject
	public SwiftComponentSources(ComponentSourcesInternal delegate) {
		this.delegate = delegate;
		this.swiftSources = delegate.register("swift", SwiftSourceSet.class).from("src/main/swift");
		delegate.disallowChanges();
	}

	public SourceView<LanguageSourceSet> getAsView() {
		return delegate.getAsView();
	}
}
