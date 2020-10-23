package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.NamedTargetBuildType;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
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

		component.getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		component.getBuildVariants().finalizeValueOnRead();
		component.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		component.getDimensions().disallowChanges(); // Let's disallow changing them for now.
	}

	protected Iterable<BuildVariantInternal> createBuildVariants() {
		return ImmutableList.of(DefaultBuildVariant.of(DefaultOperatingSystemFamily.forName("ios"), DefaultMachineArchitecture.X86_64, DefaultBinaryLinkage.EXECUTABLE, new NamedTargetBuildType("Default")));
	}

	public T getComponent() {
		return component;
	}

	public BinaryView<Binary> getBinaries() {
		return component.getBinaries();
	}
}
