package dev.nokee.ide.xcode;

import lombok.Value;

/**
 * Represent a product type supported by Xcode IDE.
 * Each target needs to be configured with the product type it produce.
 *
 * @since 0.3
 */
@Value(staticConstructor = "of")
public class XcodeIdeProductType {
	String identifier;

	@Override
	public String toString() {
		return identifier;
	}
}
