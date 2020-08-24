package dev.nokee.platform.ios.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.ios.internal.rules.IosBuildVariantConvention;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

public class BaseIosExtension<T extends BaseNativeComponent<?>> {
	private final T component;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;

	public BaseIosExtension(T component, ObjectFactory objects, ProviderFactory providers) {
		this.component = component;
		this.objects = objects;
		this.providers = providers;

		component.getBuildVariants().convention(getProviders().provider(new IosBuildVariantConvention()));
		component.getBuildVariants().finalizeValueOnRead();
		component.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		component.getDimensions().disallowChanges(); // Let's disallow changing them for now.
	}

	public T getComponent() {
		return component;
	}

	public BinaryView<Binary> getBinaries() {
		return component.getBinaries();
	}
}
