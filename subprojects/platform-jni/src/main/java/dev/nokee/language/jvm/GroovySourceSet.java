package dev.nokee.language.jvm;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface GroovySourceSet extends LanguageSourceSet {
	@Override
    GroovySourceSet from(Object... paths);

	@Override
    GroovySourceSet filter(Action<? super PatternFilterable> action);
}
