package dev.nokee.model.internal;

import lombok.EqualsAndHashCode;

final class SupportedTypes {
	private SupportedTypes() {}

	public static NamedDomainObjectRegistry.SupportedType instanceOf(Class<?> type) {
		return new InstanceOfSupportedType(type);
	}

	@EqualsAndHashCode
	private static final class InstanceOfSupportedType implements NamedDomainObjectRegistry.SupportedType {
		private final Class<?> type;

		private InstanceOfSupportedType(Class<?> type) {
			this.type = type;
		}

		@Override
		public boolean supports(Class<?> type) {
			return this.type.equals(type);
		}

		@Override
		public String toString() {
			return type.getSimpleName();
		}
	}

	public static NamedDomainObjectRegistry.SupportedType subtypeOf(Class<?> type) {
		return new SubtypeOfSupportedType(type);
	}

	@EqualsAndHashCode
	private static final class SubtypeOfSupportedType implements NamedDomainObjectRegistry.SupportedType {
		private final Class<?> type;

		private SubtypeOfSupportedType(Class<?> type) {
			this.type = type;
		}

		@Override
		public boolean supports(Class<?> type) {
			return this.type.isAssignableFrom(type);
		}

		@Override
		public String toString() {
			return "subtypes of '" + type.getSimpleName() + "'";
		}
	}
}
