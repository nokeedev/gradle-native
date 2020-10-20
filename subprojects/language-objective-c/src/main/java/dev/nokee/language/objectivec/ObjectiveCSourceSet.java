package dev.nokee.language.objectivec;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface ObjectiveCSourceSet extends LanguageSourceSet {
	@Override
	ObjectiveCSourceSet from(Object... paths);

	@Override
	ObjectiveCSourceSet filter(Action<? super PatternFilterable> action);
}
