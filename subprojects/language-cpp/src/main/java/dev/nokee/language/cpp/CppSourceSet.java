package dev.nokee.language.cpp;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface CppSourceSet extends LanguageSourceSet {
	@Override
	CppSourceSet from(Object... paths);

	@Override
	CppSourceSet filter(Action<? super PatternFilterable> action);
}
