package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Variant;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

enum ToDevelopmentBinaryTransformer implements Transformer<Provider<Binary>, Variant> {
	TO_DEVELOPMENT_BINARY;

	@Override
	public Provider<Binary> transform(Variant variant) {
		return variant.getDevelopmentBinary();
	}
}
