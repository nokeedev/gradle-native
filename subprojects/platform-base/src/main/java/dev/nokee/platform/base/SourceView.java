package dev.nokee.platform.base;

import dev.nokee.language.base.LanguageSourceSet;

public interface SourceView<T extends LanguageSourceSet> extends View<T> {
	/**
	 * Returns a source view containing the objects in this view of the given type.
	 * The returned collection is live, so that when matching objects are later added to this view, they are also visible in the filtered source view.
	 *
	 * @param type The type of source to find.
	 * @param <S> The base type of the new source view.
	 * @return the matching element as a {@link SourceView}, never null.
	 */
	<S extends T> SourceView<S> withType(Class<S> type);
}
