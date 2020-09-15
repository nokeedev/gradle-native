package dev.nokee.platform.base;

import dev.nokee.platform.base.internal.DomainObjectElements;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.utils.Cast;

import java.util.function.Supplier;

/**
 * Represent a domain object element of a view, collection or container.
 * The element can be eagerly or lazily created.
 * It contains the important information about the object, that is the element itself, the type of the element and the identifier of the element.
 *
 * @param <T> the type of the element held by this class.
 * @since 0.5
 */
public interface DomainObjectElement<T> {
	/**
	 * Returns the instance of this element.
	 * The same object should always be returned by this method.
	 *
	 * @return the instance of this element, never null.
	 */
	T get();

	/**
	 * Returns the type of this element.
	 *
	 * @return a {@link Class} instance representing the type of this element, never null.
	 */
	Class<T> getType();

	/**
	 * Returns the identifier of this element.
	 *
	 * @return a {@link DomainObjectIdentifier} instance representing this element, never null.
	 */
	DomainObjectIdentifier getIdentifier();

	static <T> DomainObjectElement<T> of(T element) {
		return new DomainObjectElements.Existing<>(Cast.uncheckedCastBecauseOfTypeErasure(element.getClass()), element);
	}

	static <T, U extends T> DomainObjectElement<T> of(Class<T> type, U element) {
		return new DomainObjectElements.Existing<>(type, element);
	}

	static <T> DomainObjectElement<T> of(Class<T> type, Supplier<T> elementSupplier) {
		return new DomainObjectElements.Memoizing<>(new DomainObjectElements.Supplying<>(type, elementSupplier));
	}
}
