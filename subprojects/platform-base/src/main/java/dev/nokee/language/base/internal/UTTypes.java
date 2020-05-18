package dev.nokee.language.base.internal;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

/**
 * Factory for creating adhoc uniform type identifier.
 *
 * @since 0.4
 */
public final class UTTypes {
	/**
	 * Creates a uniform type of the specified identifier.
	 *
	 * @param identifier a identifier for the uniform type to create.
	 * @param filenameExtensions an array of filename extensions for the uniform type.
	 * @return a {@link UTType} instance, never null.
	 */
	public static UTType of(String identifier, String[] filenameExtensions) {
		return new DefaultUTType(identifier, filenameExtensions);
	}

	@Value
	@EqualsAndHashCode(of = {"identifier"})
	private static class DefaultUTType implements UTType {
		@NonNull String identifier;
		@NonNull String[] filenameExtensions;

		@Override
		public String getDisplayName() {
			return String.format("Uniform type '%s'", identifier);
		}
	}
}
