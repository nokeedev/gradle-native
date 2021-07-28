package dev.nokee.model;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.util.ConfigureUtil;

import static java.util.Objects.requireNonNull;

/**
 * A container object that knows about a value of a specific type.
 * The value can be transformed using one of the mapping methods such as {@link #map(Transformer)} or {@link #flatMap(Transformer)}.
 * Unlike its counterpart, the {@link Provider}, a known object does not give access to the value directly.
 *
 * @param <T>  the type of value known by this object
 */
public interface KnownDomainObject<T> {
	/**
	 * Returns an unique identifier for this known object.
	 *
	 * @return an identifier for this known object, never null
	 */
	DomainObjectIdentifier getIdentifier();

	/**
	 * Returns the known object type.
	 *
	 * @return the known object type, never null
	 */
	Class<T> getType();

	/**
	 * Configures the known object with the given action.
	 * Actions are run in the order added.
	 *
	 * @param action  a {@link Action} that can configure the known object when required, must not be null
	 * @return this known object, never null
	 */
	KnownDomainObject<T> configure(Action<? super T> action);

	/** @see #configure(Action) */
	default KnownDomainObject<T> configure(@ClosureParams(FirstParam.FirstGenericType.class) @DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		configure(ConfigureUtil.configureUsing(requireNonNull(closure)));
		return this;
	}

	/**
	 * Returns a {@link Provider} whose value is the value of this known object transformed using the given function.
	 * <p>
	 * The provider will be live, so that each time it is queried, it queries this object value and applies the transformation to the result.
	 *
	 * @param transformer  the transformer to apply to values, must not be null. May return {@code null}, in which case the provider will have no value.
	 * @param <S>  the mapped value type
	 * @return a {@link Provider} of the value of this known object mapped using the specified transformer, never null
	 */
	<S> Provider<S> map(Transformer<? extends S, ? super T> transformer);

	/**
	 * Returns a {@link Provider} from the value of this known object transformed using the given function.
	 * <p>
	 * The provider will be live, so that each time it is queried, it queries this object value and applies the transformation to the result.
	 *
	 * <p>Any task details associated with this known object are ignored.
	 * The new provider will use whatever task details are associated with the return value of the function.
	 *
	 * @param transformer  the transformer to apply to values, must not be null. May return {@code null}, in which case the provider will have no value.
	 * @param <S>  the mapped value type
	 * @return a {@link Provider} of the value of this known object mapped using the specified transformer, never null
	 */
	<S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer);
}
