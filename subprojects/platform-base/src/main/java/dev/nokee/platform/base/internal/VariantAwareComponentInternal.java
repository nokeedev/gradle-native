package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantAwareComponent;
import org.gradle.api.provider.Provider;

public interface VariantAwareComponentInternal<T extends Variant> extends VariantAwareComponent<T> {
	Provider<T> getDevelopmentVariant();
	ComponentIdentifier<?> getIdentifier();
}
