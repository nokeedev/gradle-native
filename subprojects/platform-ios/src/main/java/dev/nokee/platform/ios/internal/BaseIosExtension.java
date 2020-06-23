package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.nativebase.internal.BaseNativeComponent;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

public abstract class BaseIosExtension<T extends BaseNativeComponent<?>> {
	private final T component;

	public BaseIosExtension(T component) {
		this.component = component;

		component.getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		component.getBuildVariants().finalizeValueOnRead();
		component.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		component.getDimensions().disallowChanges(); // Let's disallow changing them for now.
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	protected Iterable<BuildVariant> createBuildVariants() {
		return ImmutableList.of(DefaultBuildVariant.of(DefaultOperatingSystemFamily.forName("ios"), DefaultMachineArchitecture.X86_64, DefaultBinaryLinkage.EXECUTABLE));
	}

	public T getComponent() {
		return component;
	}

	public BinaryView<Binary> getBinaries() {
		return component.getBinaries();
	}
}
