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
import java.util.stream.Collectors;

import static dev.nokee.runtime.nativebase.internal.DependencyUtils.isFrameworkDependency;

public abstract class DefaultHeaderIncomingDependencies implements HeaderIncomingDependencies {
	private final Configuration headerSearchPathsConfiguration;

	@Inject
	public DefaultHeaderIncomingDependencies(NamingScheme names, AbstractNativeComponentDependencies buckets, BuildVariant buildVariant) {
		ConfigurationUtils configurationUtils = getObjects().newInstance(ConfigurationUtils.class);
		DefaultTargetMachine targetMachine = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));

		String name = names.getConfigurationNameWithoutPrefix("headerSearchPaths");
		if (buckets.getCompileOnlyDependencies() == null) {
			headerSearchPathsConfiguration = getConfigurations().create(name, configurationUtils.asIncomingHeaderSearchPathFrom(buckets.getImplementationDependencies()).forTargetMachine(targetMachine).withDescription(names.getConfigurationDescription("Header search paths for %s.")));
		} else {
			headerSearchPathsConfiguration = getConfigurations().create(name, configurationUtils.asIncomingHeaderSearchPathFrom(buckets.getImplementationDependencies(), buckets.getCompileOnlyDependencies()).forTargetMachine(targetMachine).withDescription(names.getConfigurationDescription("Header search paths for %s.")));
		}

		configureNativeCompilerInputs();
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public FileCollection getHeaderSearchPaths() {
		return getObjects().fileCollection().from(getNativeCompilerInputs().map(this::toHeaderSearchPaths)).builtBy(headerSearchPathsConfiguration);
	}

	@Override
	public FileCollection getFrameworkSearchPaths() {
		return getObjects().fileCollection().from(getNativeCompilerInputs().map(this::toFrameworkSearchPaths)).builtBy(headerSearchPathsConfiguration);
	}

	private void configureNativeCompilerInputs() {
		getNativeCompilerInputs().set(fromNativeCompileConfiguration());
		getNativeCompilerInputs().finalizeValueOnRead();
		getNativeCompilerInputs().disallowChanges();
	}

	protected abstract ListProperty<CompilerInput> getNativeCompilerInputs();

	private Provider<List<CompilerInput>> fromNativeCompileConfiguration() {
		return getProviders().provider(() -> headerSearchPathsConfiguration.getIncoming().getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
	}

	private List<File> toHeaderSearchPaths(List<CompilerInput> inputs) {
		return inputs.stream().filter(it -> !it.isFramework()).map(CompilerInput::getFile).collect(Collectors.toList());
	}

	private List<File> toFrameworkSearchPaths(List<CompilerInput> inputs) {
		return inputs.stream().filter(it -> it.isFramework()).map(it -> it.getFile().getParentFile()).collect(Collectors.toList());
	}

	@Value
	public static class CompilerInput {
		boolean framework;
		File file;

		public static CompilerInput of(ResolvedArtifactResult result) {
			return new CompilerInput(isFrameworkDependency(result), result.getFile());
		}
	}
}
