package dev.nokee.language.jvm;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface KotlinSourceSet extends LanguageSourceSet {
	@Override
    KotlinSourceSet from(Object... paths);

	@Override
    KotlinSourceSet filter(Action<? super PatternFilterable> action);
}
