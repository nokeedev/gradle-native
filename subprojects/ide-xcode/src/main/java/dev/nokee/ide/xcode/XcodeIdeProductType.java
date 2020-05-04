package dev.nokee.ide.xcode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Represent a product type supported by Xcode IDE.
 * Each target needs to be configured with the product type it produce.
 *
 * @since 0.3
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class XcodeIdeProductType {
	String identifier;

	/**
	 * Returns the product type identifier.
	 *
	 * @return a {@link String} instance of the identifier, never null.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Creates a product type of the specified identifier.
	 *
	 * @param identifier a identifier for the product type to create.
	 * @return a {@link XcodeIdeProductType} instance, never null.
	 */
	public static XcodeIdeProductType of(String identifier) {
		return new XcodeIdeProductType(identifier);
	}

	@Override
	public String toString() {
		return identifier;
	}
}
