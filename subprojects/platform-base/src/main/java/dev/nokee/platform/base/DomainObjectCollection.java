package dev.nokee.platform.base;

import dev.nokee.platform.base.internal.DomainObjectIdentifier;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.Collection;
import java.util.List;

public interface DomainObjectCollection<T> extends DomainObjectElementObserver<T>, DomainObjectElementConfigurer<T> {
	boolean add(DomainObjectElement<T> element);

	DomainObjectProvider<T> get(DomainObjectIdentifier identity);

	int size();

	View<T> getAsView();


	Provider<Collection<? extends T>> getElements();


	<S extends T> View<S> withType(Class<S> type);

	/**
	 * Returns a list containing the results of applying the given mapper function to each element in the view.
	 *
	 * <p>The returned {@link Provider} is live, and tracks changes of the view.</p>
	 *
	 * @param mapper a transform function to apply on each element of this view
	 * @param <S> the type of the mapped elements
	 * @return a provider containing the transformed elements included in this view.
	 */
	<S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper);

	/**
	 * Returns a single list containing all elements yielded from results of mapper function being invoked on each element of this view.
	 *
	 * <p>The returned {@link Provider} is live, and tracks changes of the view.</p>
	 *
	 * @param mapper a transform function to apply on each element of this view
	 * @param <S> the type of the mapped elements
	 * @return a provider containing the mapped elements of this view.
	 */
	<S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper);

	/**
	 * Returns a single list containing all elements matching the given specification.
	 *
	 * <p>The returned {@link Provider} is live, and tracks changes of the view.</p>
	 *
	 * @param spec a specification to satisfy
	 * @return a provider containing the filtered elements of this view.
	 */
	Provider<List<? extends T>> filter(Spec<? super T> spec);

	DomainObjectCollection<T> disallowChanges();
}
