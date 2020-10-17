package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetView;
import org.gradle.api.Action;

public interface LanguageSourceSetViewInternal<T extends LanguageSourceSet> extends LanguageSourceSetView<T> {
	void whenElementKnown(Action<? super KnownLanguageSourceSet<T>> action);
	<S extends T> void whenElementKnown(Class<S> type, Action<? super KnownLanguageSourceSet<S>> action);
}
