package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.val;
import org.gradle.api.GradleException;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;

public abstract class BaseNativeExtension<T extends BaseNativeComponent<?>> {
	private final T component;

	public BaseNativeExtension(T component) {
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

	@Inject
	protected abstract ProjectLayout getLayout();

	protected Transformer<Iterable<FileSystemLocation>, Set<FileSystemLocation>> toIfEmpty(String path) {
		return sources -> {
			if (sources.isEmpty()) {
				return ImmutableList.of(getLayout().getProjectDirectory().file(path));
			}
			return sources;
		};
	}

	protected Iterable<BuildVariant> createBuildVariants() {
		if (this instanceof TargetMachineAwareComponent) {
			Set<TargetMachine> targetMachines = ((TargetMachineAwareComponent) this).getTargetMachines().get();

			ImmutableList.Builder<BuildVariant> buildVariantBuilder = ImmutableList.builder();
			for (TargetMachine targetMachine : targetMachines) {
				DefaultTargetMachine targetMachineInternal = (DefaultTargetMachine) targetMachine;
				ImmutableList.Builder<Dimension> dimensionBuilder = ImmutableList.builder();
				dimensionBuilder.add(targetMachineInternal.getOperatingSystemFamily(), targetMachineInternal.getArchitecture());

				if (component instanceof DefaultNativeApplicationComponent) {
					dimensionBuilder.add(DefaultBinaryLinkage.EXECUTABLE);
					buildVariantBuilder.add(DefaultBuildVariant.of(dimensionBuilder.build()));
				} else if (component instanceof DefaultNativeLibraryComponent) {
					if (this instanceof TargetLinkageAwareComponent) {
						Set<TargetLinkage> targetLinkages = ((TargetLinkageAwareComponent) this).getTargetLinkages().get();
						for (TargetLinkage targetLinkage : targetLinkages) {
							DefaultBinaryLinkage linkageInternal = (DefaultBinaryLinkage) targetLinkage;
							val libraryDimensionBuilder = ImmutableList.<Dimension>builder().addAll(dimensionBuilder.build());
							libraryDimensionBuilder.add(linkageInternal);
							buildVariantBuilder.add(DefaultBuildVariant.of(libraryDimensionBuilder.build()));
						}
					} else {
						dimensionBuilder.add(DefaultBinaryLinkage.SHARED);
						buildVariantBuilder.add(DefaultBuildVariant.of(dimensionBuilder.build()));
					}
				} else {
					buildVariantBuilder.add(DefaultBuildVariant.of(dimensionBuilder.build()));
				}
			}

			return buildVariantBuilder.build();
		}
		throw new GradleException("Not able to create the default build variants");
	}

	protected T getComponent() {
		return component;
	}

	public DefaultTargetMachineFactory getMachines() {
		return DefaultTargetMachineFactory.INSTANCE;
	}

	public DefaultTargetLinkageFactory getLinkages() {
		return DefaultTargetLinkageFactory.INSTANCE;
	}

	public BinaryView<Binary> getBinaries() {
		return component.getBinaries();
	}
}
