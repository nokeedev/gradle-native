package dev.nokee.model.dsl;

import dev.nokee.model.KnownDomainObject;
import groovy.lang.Closure;
import org.gradle.api.Action;

import java.util.function.BiConsumer;

public interface NodeMethods {
	interface Identity {
		ModelNode invoke(ModelNode self, Object identity);
	}

	interface IdentityAction {
		ModelNode invoke(ModelNode self, Object identity, Action<? super ModelNode> action);
	}

	interface IdentityClosure {
		ModelNode invoke(ModelNode self, Object identity, Closure<?> closure);
	}

	interface IdentityProjection {
		<T> KnownDomainObject<T> invoke(ModelNode self, Object identity, Class<T> type);
	}

	interface IdentityProjectionAction {
		<T> KnownDomainObject<T> invoke(ModelNode self, Object identity, Class<T> type, BiConsumer<? super ModelNode, ? super KnownDomainObject<T>> closure);
	}

	interface IdentityProjectionClosure {
		<T> KnownDomainObject<T> invoke(ModelNode self, Object identity, Class<T> type, Closure<?> closure);
	}
}
