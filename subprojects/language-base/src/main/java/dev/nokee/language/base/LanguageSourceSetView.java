package dev.nokee.language.base;

import dev.nokee.model.DomainObjectView;

/**
 * A view of the language source set that are created and configured as they are required.
 *
 * @param <T> type of the elements in this view
 */
public interface LanguageSourceSetView<T extends LanguageSourceSet> extends DomainObjectView<T> {
	/**
	 * Returns a language source set view containing the objects in this view of the given type.
	 * The returned collection is live, so that when matching objects are later added to this view, they are also visible in the filtered variant view.
	 *
	 * @param type The type of language source set to find.
	 * @param <S> The base type of the new language source set view.
	 * @return the matching element as a {@link LanguageSourceSetView}, never null.
	 */
	@Override
	<S extends T> LanguageSourceSetView<S> withType(Class<S> type);
}
