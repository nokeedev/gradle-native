package dev.nokee.platform.jni.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachineFactory;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class JniLibraryExtensionInternal implements JniLibraryExtension {
	private final DomainObjectSet<LanguageSourceSetInternal> sources;
	private final JniLibraryDependenciesInternal dependencies;
	private final GroupId groupId;
	private final DomainObjectSet<BinaryInternal> binaryCollection;
	private final NamingScheme names;

	@Inject
	public JniLibraryExtensionInternal(JniLibraryDependenciesInternal dependencies, GroupId groupId, NamingScheme names) {
		this.names = names;
		binaryCollection = getObjects().domainObjectSet(BinaryInternal.class);
		sources = getObjects().domainObjectSet(LanguageSourceSetInternal.class);
		this.dependencies = dependencies;
		this.groupId = groupId;

		getDimensions().convention(ImmutableSet.of(DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE));
		getDimensions().finalizeValueOnRead();
		getDimensions().disallowChanges(); // Let's disallow changing them for now.

		getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	//region Variant-awareness
	// TODO: We may be able to fold all the variant concept inside the VariantCollection or a derived type, let's see how it goes with the other plugins
	private final VariantCollection<JniLibraryInternal> variantCollection = getObjects().newInstance(VariantCollection.class, JniLibraryInternal.class, (VariantFactory<JniLibraryInternal>)this::create);

	private JniLibraryInternal create(String name, BuildVariant buildVariant) {
		Preconditions.checkArgument(buildVariant.getDimensions().size() == 2);
		Preconditions.checkArgument(buildVariant.getDimensions().get(0) instanceof OperatingSystemFamily);
		Preconditions.checkArgument(buildVariant.getDimensions().get(1) instanceof MachineArchitecture);
		NamingScheme names = this.names.forBuildVariant(buildVariant, getBuildVariants().get());
		JniLibraryNativeDependenciesInternal variantDependencies = dependencies;
		if (getTargetMachines().get().size() > 1) {
			variantDependencies = getObjects().newInstance(JniLibraryNativeDependenciesInternal.class, names);
			variantDependencies.extendsFrom(dependencies);
		}

		JniLibraryInternal result = getObjects().newInstance(JniLibraryInternal.class, name, names, sources, dependencies.getNativeDependencies(), buildVariant, groupId, binaryCollection, variantDependencies);
		return result;
	}

	private List<BuildVariant> createBuildVariants() {
		Set<TargetMachine> targetMachines = getTargetMachines().get();
		return targetMachines.stream().map(it -> (DefaultTargetMachine)it).map(it -> DefaultBuildVariant.of(it.getOperatingSystemFamily(), it.getArchitecture())).collect(Collectors.toList());
	}

	public VariantCollection<JniLibraryInternal> getVariantCollection() {
		return variantCollection;
	}

	@Override
	public VariantView<JniLibrary> getVariants() {
		return variantCollection.getAsView(JniLibrary.class);
	}

	// TODO: We may want to model this as a DimensionRegistry for more richness than a plain set
	public abstract SetProperty<DimensionType> getDimensions();

	// TODO: We may want to model this as a BuildVariantRegistry for more richness than a plain set
	public abstract SetProperty<BuildVariant> getBuildVariants();
	//endregion

	@Override
	public BinaryView<Binary> getBinaries() {
		return Cast.uncheckedCast(getObjects().newInstance(DefaultBinaryView.class, binaryCollection, variantCollection));
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
		return sources;
	}

	public Configuration getNativeImplementationDependencies() {
		return dependencies.getNativeImplementationDependencies();
	}

	public Configuration getJvmImplementationDependencies() {
		return dependencies.getJvmImplementationDependencies();
	}

	@Override
	public JniLibraryDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super JniLibraryDependencies> action) {
		action.execute(dependencies);
	}

	@Override
	public TargetMachineFactory getMachines() {
		return DefaultTargetMachineFactory.INSTANCE;
	}
}
