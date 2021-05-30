package dev.nokee.model.dsl

import dev.nokee.model.KnownDomainObject

import java.util.stream.Stream

class NodeParams {
	static Stream<NodeMethods.Identity> string() {
		return Stream.of(
			new NodeMethods.Identity() {
				@Override
				ModelNode invoke(ModelNode self, Object identity) {
					return self.node(identity)
				}

				@Override
				String toString() {
					return "ModelNode#node(<identity>)";
				}
			},
			new NodeMethods.Identity() {
				@Override
				ModelNode invoke(ModelNode self, Object identity) {
					return self."${identity}"
				}

				@Override
				String toString() {
					return "ModelNode#<identity>";
				}
			},
			new NodeMethods.Identity() {
				@Override
				ModelNode invoke(ModelNode self, Object identity) {
					return self."${identity}"()
				}

				@Override
				String toString() {
					return "ModelNode#<identity>()";
				}
			})
	}

	static Stream<NodeMethods.IdentityClosure> stringClosure() {
		return Stream.of(
			new NodeMethods.IdentityClosure() {
				@Override
				ModelNode invoke(ModelNode self, Object identity, Closure<?> closure) {
					return self.node(identity, closure)
				}

				@Override
				String toString() {
					return "ModelNode#node(<identity>) { }";
				}
			},
			new NodeMethods.IdentityClosure() {
				@Override
				ModelNode invoke(ModelNode self, Object identity, Closure<?> closure) {
					return self."${identity}"(closure)
				}

				@Override
				String toString() {
					return "ModelNode#<identity> { }";
				}
			})
	}

	static Stream<NodeMethods.IdentityProjection> stringProjection() {
		return Stream.of(
			new NodeMethods.IdentityProjection() {
				@Override
				<T> KnownDomainObject<T> invoke(ModelNode self, Object identity, Class<T> type) {
					return self.node(identity, type)
				}

				@Override
				String toString() {
					return "ModelNode#node(<identity>, <projection>)";
				}
			},
			new NodeMethods.IdentityProjection() {
				@Override
				<T> KnownDomainObject<T> invoke(ModelNode self, Object identity, Class<T> type) {
					return self."${identity}"(type)
				}

				@Override
				String toString() {
					return "ModelNode#<identity>(<projection>)";
				}
			})
	}

	static Stream<NodeMethods.IdentityProjectionClosure> stringProjectionClosure() {
		return Stream.of(
			new NodeMethods.IdentityProjectionClosure() {
				@Override
				<T> KnownDomainObject<T> invoke(ModelNode self, Object identity, Class<T> type, Closure<?> closure) {
					return self.node(identity, type, closure)
				}

				@Override
				String toString() {
					return "ModelNode#node(<identity>, <projection>) { }";
				}
			},
			new NodeMethods.IdentityProjectionClosure() {
				@Override
				<T> KnownDomainObject<T> invoke(ModelNode self, Object identity, Class<T> type, Closure<?> closure) {
					return self."${identity}"(type, closure)
				}

				@Override
				String toString() {
					return "ModelNode#<identity>(<projection>) { }";
				}
			})
	}
}
