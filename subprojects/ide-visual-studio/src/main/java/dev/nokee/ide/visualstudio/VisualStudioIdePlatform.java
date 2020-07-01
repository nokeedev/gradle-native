package dev.nokee.ide.visualstudio;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Represent a platform supported by Visual Studio IDE.
 * Each configuration items can be associated to a platform.
 *
 * @since 0.5
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VisualStudioIdePlatform {
	String identifier;

	/**
	 * Returns the platform identifier.
	 *
	 * @return a {@link String} instance of the identifier, never null.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Creates a platform of the specified identifier.
	 *
	 * @param identifier a identifier for the platform to create.
	 * @return a {@link VisualStudioIdePlatform} instance, never null.
	 */
	public static VisualStudioIdePlatform of(String identifier) {
		return new VisualStudioIdePlatform(identifier);
	}

	@Override
	public String toString() {
		return identifier;
	}
}
