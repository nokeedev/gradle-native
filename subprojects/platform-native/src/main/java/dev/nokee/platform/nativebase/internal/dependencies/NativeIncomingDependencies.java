package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.Value;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.nokee.runtime.nativebase.internal.DependencyUtils.isFrameworkDependency;

public abstract class NativeIncomingDependencies {
	private final SwiftModuleIncomingDependencies incomingSwiftModules;
	private final HeaderIncomingDependencies incomingHeaders;
	private final Configuration runtimeLibrariesConfiguration;
	private final Configuration linkLibrariesConfiguration;

	@Inject
	public NativeIncomingDependencies(NamingScheme names, BuildVariant buildVariant, AbstractNativeComponentDependencies buckets, SwiftModuleIncomingDependencies incomingSwiftModules, HeaderIncomingDependencies incomingHeaders) {
		DefaultTargetMachine targetMachine = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));
		this.incomingSwiftModules = incomingSwiftModules;
		this.incomingHeaders = incomingHeaders;

		ConfigurationUtils configurationUtils = getObjects().newInstance(ConfigurationUtils.class);
		this.linkLibrariesConfiguration = getConfigurations().create(names.getConfigurationName("linkLibraries"), configurationUtils.asIncomingLinkLibrariesFrom(buckets.getImplementationDependencies(), buckets.getLinkOnlyDependencies()).forTargetMachine(targetMachine).asDebug().withDescription(names.getConfigurationDescription("Link libraries for %s.")));
		this.runtimeLibrariesConfiguration = getConfigurations().create(names.getConfigurationName("runtimeLibraries"), configurationUtils.asIncomingRuntimeLibrariesFrom(buckets.getImplementationDependencies(), buckets.getRuntimeOnlyDependencies()).forTargetMachine(targetMachine).asDebug().withDescription(names.getConfigurationDescription("Runtime libraries for %s.")));

		configureLinkerInputs();
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ProviderFactory getProviders();

	public FileCollection getSwiftModules() {
		return incomingSwiftModules.getSwiftModules();
	}

	public FileCollection getHeaderSearchPaths() {
		return incomingHeaders.getHeaderSearchPaths();
	}

	public FileCollection getFrameworkSearchPaths() {
		return getObjects().fileCollection().from(incomingHeaders.getFrameworkSearchPaths()).from(incomingSwiftModules.getFrameworkSearchPaths());
	}

	//region Linker inputs
	public FileCollection getLinkLibraries() {
		return getObjects().fileCollection().from(getLinkerInputs().map(this::toLinkLibraries)).builtBy(linkLibrariesConfiguration);
	}

	public FileCollection getLinkFrameworks() {
		return getObjects().fileCollection().from(getLinkerInputs().map(this::toLinkFrameworks)).builtBy(linkLibrariesConfiguration);
	}

	private void configureLinkerInputs() {
		getLinkerInputs().set(fromLinkConfiguration());
		getLinkerInputs().finalizeValueOnRead();
		getLinkerInputs().disallowChanges();
	}

	private Provider<List<LinkerInput>> fromLinkConfiguration() {
		return getProviders().provider(() -> linkLibrariesConfiguration.getIncoming().getArtifacts().getArtifacts().stream().map(LinkerInput::of).collect(Collectors.toList()));
	}

	public abstract ListProperty<LinkerInput> getLinkerInputs();

	private List<File> toLinkLibraries(List<LinkerInput> inputs) {
		return inputs.stream().filter(it -> !it.isFramework()).map(LinkerInput::getFile).collect(Collectors.toList());
	}

	private List<File> toLinkFrameworks(List<LinkerInput> inputs) {
		return inputs.stream().filter(it -> it.isFramework()).map(LinkerInput::getFile).collect(Collectors.toList());
	}

	@Value
	static class LinkerInput {
		boolean framework;
		File file;

		public static LinkerInput of(ResolvedArtifactResult result) {
			return new LinkerInput(isFrameworkDependency(result), result.getFile());
		}
	}
	//endregion

	public FileCollection getRuntimeLibraries() {
		return getObjects().fileCollection().from(runtimeLibrariesConfiguration);
	}

	private boolean hasConfiguration(String name) {
		return StreamSupport.stream(getConfigurations().getCollectionSchema().getElements().spliterator(), false).anyMatch(it -> it.getName().equals(name));
	}
}
