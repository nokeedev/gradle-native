package dev.nokee.model.internal;

public final class DomainObjectIdentities {
	public static Object root() {
		return RootIdentity.INSTANCE;
	}

	private enum RootIdentity {
		INSTANCE;

		@Override
		public String toString() {
			return "";
		}
	}
}
