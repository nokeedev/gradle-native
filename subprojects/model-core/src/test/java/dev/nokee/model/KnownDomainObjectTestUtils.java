package dev.nokee.model;

import static dev.nokee.utils.TransformerUtils.noOpTransformer;

final class KnownDomainObjectTestUtils {
	public static void realize(KnownDomainObject<?> knownObject) {
		knownObject.map(noOpTransformer()).get();
	}
}
