package dev.nokee.platform.ios.internal.rules;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundle;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import static dev.nokee.platform.base.internal.DevelopmentBinaryUtils.selectSingleBinaryByType;

public enum IosDevelopmentBinaryConvention implements Transformer<Provider<? extends Binary>, Iterable<? extends Binary>> {
	INSTANCE;

	@Override
	public Provider<? extends Binary> transform(Iterable<? extends Binary> binaries) {
		return selectSingleBinaryByType(SignedIosApplicationBundle.class, binaries);
	}
}
