package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import org.gradle.api.specs.Spec;

import java.util.Optional;

public final class SpecUtils {
	private SpecUtils() {}

	public static <T> Spec<T> byType(Class<? extends T> type) {
		return new ByTypeSpec<>(type);
	}

	public static <T> Optional<Class<? extends T>> getTypeFiltered(Spec<T> spec) {
		if (spec instanceof ByTypeSpec) {
			return Optional.of(((ByTypeSpec<T>) spec).getType());
		}
		return Optional.empty();
	}

	@EqualsAndHashCode
	private static class ByTypeSpec<T> implements Spec<T> {
		private final Class<? extends T> type;

		public ByTypeSpec(Class<? extends T> type) {
			this.type = type;
		}

		public Class<? extends T> getType() {
			return type;
		}

		@Override
		public boolean isSatisfiedBy(T t) {
			return type.isInstance(t);
		}

		@Override
		public String toString() {
			return "SpecUtils.byType(" + type.getCanonicalName() + ")";
		}
	}
}
