package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.internal.rules.BuildVariantConvention;
import dev.nokee.runtime.base.internal.DefaultDimensionType;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class BaseNativeExtension<T extends BaseNativeComponent<?>> {
	@Getter private final T component;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;

	public BaseNativeExtension(T component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout) {
		this.component = component;
		this.objects = objects;
		this.providers = providers;
		this.layout = layout;

		component.getBuildVariants().convention(providers.provider(new BuildVariantConvention(this, component, component.getDimensions()::get)));
		component.getBuildVariants().finalizeValueOnRead();
		component.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		component.getDimensions().disallowChanges(); // Let's disallow changing them for now.
	}

	protected Transformer<Iterable<FileSystemLocation>, Set<FileSystemLocation>> toIfEmpty(String path) {
		return sources -> {
			if (sources.isEmpty()) {
				return ImmutableList.of(getLayout().getProjectDirectory().file(path));
			}
			return sources;
		};
	}

	public BinaryView<Binary> getBinaries() {
		return component.getBinaries();
	}
}
