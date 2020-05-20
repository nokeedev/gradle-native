package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.nativebase.TargetMachine;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachineFactory;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.internal.Cast;

import javax.inject.Inject;

public abstract class JniLibraryExtensionInternal implements JniLibraryExtension {
	private final DomainObjectSet<LanguageSourceSetInternal> sources;
	private final JniLibraryDependenciesInternal dependencies;
	private final GroupId groupId;
	private final DomainObjectSet<BinaryInternal> binaryCollection;
	private final VariantCollection<JniLibraryInternal> variantCollection = getObjects().newInstance(VariantCollection.class, JniLibraryInternal.class, (VariantFactory<JniLibraryInternal>)this::create);
	private final DefaultTargetMachineFactory targetMachineFactory;

	@Inject
	public JniLibraryExtensionInternal(JniLibraryDependenciesInternal dependencies, GroupId groupId, DefaultTargetMachineFactory targetMachineFactory) {
		this.targetMachineFactory = targetMachineFactory;
		binaryCollection = getObjects().domainObjectSet(BinaryInternal.class);
		sources = getObjects().domainObjectSet(LanguageSourceSetInternal.class);
		this.dependencies = dependencies;
		this.groupId = groupId;
	}

	private JniLibraryInternal create(String name, NamingScheme names, Object targetMachineObject) {
		TargetMachine targetMachine = (TargetMachine)targetMachineObject;
		JniLibraryNativeDependenciesInternal variantDependencies = dependencies;
		if (getTargetMachines().get().size() > 1) {
			variantDependencies = getObjects().newInstance(JniLibraryNativeDependenciesInternal.class, names);
			variantDependencies.extendsFrom(dependencies);
		}

		JniLibraryInternal result = getObjects().newInstance(JniLibraryInternal.class, name, names, sources, dependencies.getNativeDependencies(), targetMachine, groupId, binaryCollection, variantDependencies);
		return result;
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	public VariantCollection<JniLibraryInternal> getVariantCollection() {
		return variantCollection;
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return Cast.uncheckedCast(getObjects().newInstance(VariantResolvingBinaryView.class, binaryCollection, variantCollection));
	}

	@Override
	public VariantView<JniLibrary> getVariants() {
		return variantCollection.getAsView(JniLibrary.class);
	}

	public DomainObjectSet<LanguageSourceSetInternal> getSources() {
		return sources;
	}

	public Configuration getNativeImplementationDependencies() {
		return dependencies.getNativeDependencies();
	}

	public Configuration getJvmImplementationDependencies() {
		return dependencies.getJvmDependencies();
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
		return targetMachineFactory;
	}

	public static abstract class VariantResolvingBinaryView extends DefaultBinaryView<Binary> {
		private final Realizable variants;

		@Inject
		public VariantResolvingBinaryView(DomainObjectSet<Binary> delegate, Realizable variants) {
			super(delegate);
			this.variants = variants;
		}

		@Override
		protected void doResolve() {
			variants.realize();
		}

		@Override
		protected <S extends Binary> DefaultBinaryView<S> newInstance(DomainObjectSet<S> elements) {
			return Cast.uncheckedCast(getObjects().newInstance(VariantResolvingBinaryView.class, elements, variants));
		}
	}
}
