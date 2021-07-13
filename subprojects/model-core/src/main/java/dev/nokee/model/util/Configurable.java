package dev.nokee.model.util;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

/**
 * Represent a object that can be configured using an {@link Action}, {@link Closure}, or a {@link Action} class.
 *
 * @param <T> the configurable object type
 */
public interface Configurable<T> extends org.gradle.util.Configurable<T> {
	T configure(Action<? super T> action);
	T configure(Class<? extends Action<? super T>> actionClass);
	T configure(@SuppressWarnings("rawtype") @DelegatesTo(target = "T", strategy = Closure.DELEGATE_FIRST) Closure closure);
}
