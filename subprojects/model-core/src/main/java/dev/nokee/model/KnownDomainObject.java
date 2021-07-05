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

public interface KnownDomainObject<T> {
	DomainObjectIdentifier getIdentifier();

	Class<T> getType();

	KnownDomainObject<T> configure(Action<? super T> action);

	/** @see #configure(Action) */
	default KnownDomainObject<T> configure(@ClosureParams(FirstParam.FirstGenericType.class) @DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		configure(ConfigureUtil.configureUsing(requireNonNull(closure)));
		return this;
	}

	<S> Provider<S> map(Transformer<? extends S, ? super T> transformer);
	<S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer);
}
