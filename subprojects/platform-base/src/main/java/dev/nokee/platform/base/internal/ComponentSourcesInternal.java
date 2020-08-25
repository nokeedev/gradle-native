package dev.nokee.platform.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.SourceView;
import org.gradle.api.Action;

public interface ComponentSourcesInternal extends ComponentSources {
	<U extends LanguageSourceSet> U register(String name, Class<U> type);
	<U extends LanguageSourceSet> U register(String name, Class<U> type, Action<? super U> action);
	SourceView<LanguageSourceSet> getAsView();
	ComponentSourcesInternal disallowChanges();
}
