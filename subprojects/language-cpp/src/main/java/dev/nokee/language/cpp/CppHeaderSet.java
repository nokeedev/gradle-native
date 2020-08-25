package dev.nokee.language.cpp;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface CppHeaderSet extends LanguageSourceSet {
	@Override
	CppHeaderSet from(Object... paths);

	@Override
	CppHeaderSet filter(Action<? super PatternFilterable> action);
}
