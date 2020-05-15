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
import dev.nokee.platform.nativebase.internal.DefaultTargetMachine;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class JniLibraryExtensionInternal implements JniLibraryExtension {
	private final DomainObjectSet<LanguageSourceSetInternal> sources;
	private final JniLibraryDependenciesInternal dependencies;
	private final GroupId groupId;
	private final DomainObjectSet<BinaryInternal> binaryCollection;
	private final NamedDomainObjectContainer<JniLibraryInternal> variantCollection;
	private final Map<String, JniLibraryCreationArguments> variantCreationArguments = new HashMap<>();

	@Value
	private static class JniLibraryCreationArguments {
		NamingScheme names;
		TargetMachine targetMachine;
		Action<? super JniLibraryInternal> action;
	}

	@Inject
	public JniLibraryExtensionInternal(JniLibraryDependenciesInternal dependencies, GroupId groupId) {
		binaryCollection = getObjects().domainObjectSet(BinaryInternal.class);
		sources = getObjects().domainObjectSet(LanguageSourceSetInternal.class);
		variantCollection = getObjects().domainObjectContainer(JniLibraryInternal.class, name -> {
			JniLibraryCreationArguments args = variantCreationArguments.remove(name);
			JniLibraryInternal result = getObjects().newInstance(JniLibraryInternal.class, name, args.names, sources, dependencies.getNativeDependencies(), args.targetMachine, groupId, binaryCollection);
			args.action.execute(result);
			return result;
		});
		this.dependencies = dependencies;
		this.groupId = groupId;
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	public NamedDomainObjectContainer<JniLibraryInternal> getVariantCollection() {
		return variantCollection;
	}

	public NamedDomainObjectProvider<JniLibraryInternal> registerVariant(NamingScheme names, DefaultTargetMachine targetMachine, Action<? super JniLibraryInternal> action) {
		String variantName = targetMachine.getOperatingSystemFamily().getName() + StringUtils.capitalize(targetMachine.getArchitecture().getName());
		variantCreationArguments.put(variantName, new JniLibraryCreationArguments(names, targetMachine, action));
		return getVariantCollection().register(variantName);
	}

	public BinaryView<Binary> getBinaries() {
		return Cast.uncheckedCast(getObjects().newInstance(DefaultBinaryView.class, binaryCollection));
	}

	public VariantView<JniLibrary> getVariants() {
		return Cast.uncheckedCast(getObjects().newInstance(DefaultVariantView.class, variantCollection));
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
	public abstract SetProperty<TargetMachine> getTargetMachines();
}
