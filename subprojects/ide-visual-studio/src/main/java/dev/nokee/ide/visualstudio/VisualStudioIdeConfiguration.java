package dev.nokee.ide.visualstudio;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VisualStudioIdeConfiguration {
	String identifier;

	/**
	 * Returns the configuration identifier.
	 *
	 * @return a {@link String} instance of the identifier, never null.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Creates a configuration of the specified identifier.
	 *
	 * @param identifier a identifier for the configuration to create.
	 * @return a {@link VisualStudioIdeConfiguration} instance, never null.
	 */
	public static VisualStudioIdeConfiguration of(String identifier) {
		return new VisualStudioIdeConfiguration(identifier);
	}

	@Override
	public String toString() {
		return identifier;
	}
}
