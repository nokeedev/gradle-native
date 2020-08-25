package dev.nokee.language.swift;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface SwiftSourceSet extends LanguageSourceSet {
	@Override
	SwiftSourceSet from(Object... paths);

	@Override
	SwiftSourceSet filter(Action<? super PatternFilterable> action);
}
