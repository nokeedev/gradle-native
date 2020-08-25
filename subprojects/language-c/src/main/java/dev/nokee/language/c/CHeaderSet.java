package dev.nokee.language.c;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface CHeaderSet extends LanguageSourceSet {
	@Override
	CHeaderSet from(Object... paths);

	@Override
	CHeaderSet filter(Action<? super PatternFilterable> action);
}
