package dev.nokee.model.dsl;

import dev.nokee.model.KnownDomainObject;
import groovy.lang.Closure;

public interface NodeMethods {
	interface Identity {
		ModelNode invoke(ModelNode self, Object identity);
	}

	interface IdentityClosure {
		ModelNode invoke(ModelNode self, Object identity, Closure<?> closure);
	}

	interface IdentityProjection {
		<T> KnownDomainObject<T> invoke(ModelNode self, Object identity, Class<T> type);
	}

	interface IdentityProjectionClosure {
		<T> KnownDomainObject<T> invoke(ModelNode self, Object identity, Class<T> type, Closure<?> closure);
	}
}
