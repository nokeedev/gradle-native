package dev.nokee.language.base;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;

public interface SelfAwareLanguageSourceSet<SELF extends LanguageSourceSet> extends LanguageSourceSet {
	@Override
	default SELF from(Object... paths) {
		LanguageSourceSet.super.from(paths);
		return (SELF) this;
	}

	@Override
	default SELF filter(Action<? super PatternFilterable> action) {
		LanguageSourceSet.super.filter(action);
		return (SELF) this;
	}

	@Override
	default SELF filter(Closure<Void> closure) {
		LanguageSourceSet.super.filter(closure);
		return (SELF) this;
	}

	@Override
	default SELF convention(Object... paths) {
		LanguageSourceSet.super.convention(paths);
		return (SELF) this;
	}
}
