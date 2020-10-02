package dev.nokee.platform.base.internal;

public final class BaseNameUtils {
	private BaseNameUtils() {}

	public static BaseName from(ComponentIdentifier<?> identifier) {
		if (identifier.isMainComponent()) {
			return BaseName.of(identifier.getProjectIdentifier().getName());
		}
		return BaseName.of(identifier.getProjectIdentifier().getName() + "-" + identifier.getName().get());
	}

	public static BaseName from(VariantIdentifier<?> identifier) {
		return from(identifier.getComponentIdentifier());
	}
}
