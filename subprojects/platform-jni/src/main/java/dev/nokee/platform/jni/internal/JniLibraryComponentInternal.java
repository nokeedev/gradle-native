package dev.nokee.platform.jni.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class JniLibraryComponentInternal extends BaseComponent<JniLibraryInternal> implements DependencyAwareComponent<JniLibraryDependencies>, BinaryAwareComponent {
	private final JniLibraryDependenciesInternal dependencies;
	private final GroupId groupId;
	private final DomainObjectSet<LanguageSourceSetInternal> sources;

	@Inject
	public JniLibraryComponentInternal(NamingScheme names, GroupId groupId) {
		super(names, JniLibraryInternal.class);
		this.dependencies = getObjects().newInstance(JniLibraryDependenciesInternal.class, names);
		this.groupId = groupId;
		this.sources = getObjects().domainObjectSet(LanguageSourceSetInternal.class);

		getDimensions().convention(ImmutableSet.of(DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
		getDimensions().disallowChanges(); // Let's disallow changing them for now.

		getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.
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
	public VariantView<JniLibrary> getVariants() {
		return getVariantCollection().getAsView(JniLibrary.class);
	}

	@Override
	protected JniLibraryInternal createVariant(String name, BuildVariant buildVariant) {
		Preconditions.checkArgument(buildVariant.getDimensions().size() == 2);
		Preconditions.checkArgument(buildVariant.getDimensions().get(0) instanceof OperatingSystemFamily);
		Preconditions.checkArgument(buildVariant.getDimensions().get(1) instanceof MachineArchitecture);
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());
		JniLibraryNativeDependenciesInternal variantDependencies = dependencies;
		if (getTargetMachines().get().size() > 1) {
			variantDependencies = getObjects().newInstance(JniLibraryNativeDependenciesInternal.class, names);
			variantDependencies.extendsFrom(dependencies);
		}

		JniLibraryInternal result = getObjects().newInstance(JniLibraryInternal.class, name, names, sources, buildVariant, groupId, getBinaryCollection(), variantDependencies);
		return result;
	}

	private List<BuildVariant> createBuildVariants() {
		Set<TargetMachine> targetMachines = getTargetMachines().get();
		return targetMachines.stream().map(it -> (DefaultTargetMachine)it).map(it -> DefaultBuildVariant.of(it.getOperatingSystemFamily(), it.getArchitecture())).collect(Collectors.toList());
	}
	//endregion

	public Configuration getNativeImplementationDependencies() {
		return dependencies.getNativeImplementationDependencies();
	}

	public Configuration getJvmImplementationDependencies() {
		return dependencies.getJvmImplementationDependencies();
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
		return sources;
	}

	public abstract SetProperty<TargetMachine> getTargetMachines();
}
