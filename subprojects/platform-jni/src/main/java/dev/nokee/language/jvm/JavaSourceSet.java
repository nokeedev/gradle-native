package dev.nokee.language.jvm;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface JavaSourceSet extends LanguageSourceSet {
	@Override
	JavaSourceSet from(Object... paths);

	@Override
	JavaSourceSet filter(Action<? super PatternFilterable> action);
}
