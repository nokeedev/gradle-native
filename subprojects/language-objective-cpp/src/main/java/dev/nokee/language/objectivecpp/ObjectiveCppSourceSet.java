package dev.nokee.language.objectivecpp;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface ObjectiveCppSourceSet extends LanguageSourceSet {
	@Override
	ObjectiveCppSourceSet from(Object... paths);

	@Override
	ObjectiveCppSourceSet filter(Action<? super PatternFilterable> action);
}
