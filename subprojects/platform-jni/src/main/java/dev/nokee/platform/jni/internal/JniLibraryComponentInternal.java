package dev.nokee.platform.jni.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.nokee.platform.nativebase.internal.BaseNativeComponent.one;

public abstract class JniLibraryComponentInternal extends BaseComponent<JniLibraryInternal> implements DependencyAwareComponent<JniLibraryDependencies>, BinaryAwareComponent {
	private final JniLibraryDependenciesInternal dependencies;
	private final GroupId groupId;
	private final DomainObjectSet<LanguageSourceSetInternal> sources;

	@Inject
	public JniLibraryComponentInternal(NamingScheme names, GroupId groupId) {
		super(names, JniLibraryInternal.class);
		this.dependencies = getObjects().newInstance(JniLibraryDependenciesInternal.class, names, getObjects().newInstance(DefaultNativeComponentDependencies.class, names.withConfigurationNamePrefix("native").withComponentDisplayName("JNI shared library")));
		this.groupId = groupId;
		this.sources = getObjects().domainObjectSet(LanguageSourceSetInternal.class);

		getDimensions().convention(ImmutableSet.of(DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
		getDimensions().disallowChanges(); // Let's disallow changing them for now.

		getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		getDevelopmentVariant().convention(getDefaultVariant());
		getDevelopmentVariant().disallowChanges();
	}

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public JniLibraryDependenciesInternal getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super JniLibraryDependencies> action) {
		action.execute(dependencies);
	}

	//region Variant-awareness
	protected Provider<JniLibraryInternal> getDefaultVariant() {
		return getProviders().provider(() -> {
			List<JniLibraryInternal> variants = getVariantCollection().get().stream().filter(it -> {
				DefaultOperatingSystemFamily osFamily = it.getBuildVariant().getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE);
				DefaultMachineArchitecture architecture = it.getBuildVariant().getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE);
				if (DefaultOperatingSystemFamily.HOST.equals(osFamily) && DefaultMachineArchitecture.HOST.equals(architecture)) {
					return true;
				}
				return false;
			}).collect(Collectors.toList());

			if (variants.isEmpty()) {
				return null;
			}
			return one(variants);
		});
	}

	public VariantView<JniLibrary> getVariants() {
		return getVariantCollection().getAsView(JniLibrary.class);
	}

	public JniLibraryInternal createVariant(String name, BuildVariant buildVariant, JniLibraryNativeDependenciesInternal variantDependencies) {
		Preconditions.checkArgument(buildVariant.getDimensions().size() == 2);
		Preconditions.checkArgument(buildVariant.getDimensions().get(0) instanceof OperatingSystemFamily);
		Preconditions.checkArgument(buildVariant.getDimensions().get(1) instanceof MachineArchitecture);
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());

		JniLibraryInternal result = getObjects().newInstance(JniLibraryInternal.class, name, names, sources, buildVariant, groupId, getBinaryCollection(), variantDependencies);
		return result;
	}

	private List<BuildVariant> createBuildVariants() {
		Set<TargetMachine> targetMachines = getTargetMachines().get();
		return targetMachines.stream().map(it -> (DefaultTargetMachine)it).map(it -> DefaultBuildVariant.of(it.getOperatingSystemFamily(), it.getArchitecture())).collect(Collectors.toList());
	}
	//endregion

	public Configuration getJvmImplementationDependencies() {
		return dependencies.getJvmImplementationDependencies();
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
		return sources;
	}

	public abstract SetProperty<TargetMachine> getTargetMachines();
}
