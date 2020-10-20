package dev.nokee.language.c;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface CSourceSet extends LanguageSourceSet {
	@Override
	CSourceSet from(Object... paths);

	@Override
	CSourceSet filter(Action<? super PatternFilterable> action);
}
