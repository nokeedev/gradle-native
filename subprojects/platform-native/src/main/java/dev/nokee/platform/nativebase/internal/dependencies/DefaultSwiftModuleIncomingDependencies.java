package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
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

public abstract class DefaultSwiftModuleIncomingDependencies implements SwiftModuleIncomingDependencies {
	private final Configuration importSwiftModulesConfiguration;
	@Inject
	public DefaultSwiftModuleIncomingDependencies(NamingScheme names, AbstractNativeComponentDependencies buckets) {
		ConfigurationUtils configurationUtils = getObjects().newInstance(ConfigurationUtils.class);

		String name = names.getConfigurationNameWithoutPrefix("importSwiftModules");
		importSwiftModulesConfiguration = getConfigurations().create(name, configurationUtils.asIncomingSwiftModuleFrom(buckets.getImplementationDependencies(), buckets.getCompileOnlyDependencies()).withDescription(names.getConfigurationDescription("Import Swift modules for %s.")));

		configureSwiftCompilerInputs();
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public FileCollection getSwiftModules() {
		return getObjects().fileCollection().from(getSwiftCompilerInputs().map(this::toSwiftModules)).builtBy(importSwiftModulesConfiguration);
	}

	@Override
	public FileCollection getFrameworkSearchPaths() {
		return getObjects().fileCollection().from(getSwiftCompilerInputs().map(this::toFrameworkSearchPaths)).builtBy(importSwiftModulesConfiguration);
	}

	private List<File> toSwiftModules(List<CompilerInput> inputs) {
		return inputs.stream().filter(it -> !it.isFramework()).map(CompilerInput::getFile).collect(Collectors.toList());
	}

	private List<File> toFrameworkSearchPaths(List<CompilerInput> inputs) {
		return inputs.stream().filter(it -> it.isFramework()).map(it -> it.getFile().getParentFile()).collect(Collectors.toList());
	}

	private void configureSwiftCompilerInputs() {
		getSwiftCompilerInputs().set(fromSwiftCompileConfiguration());
		getSwiftCompilerInputs().finalizeValueOnRead();
		getSwiftCompilerInputs().disallowChanges();
	}

	protected abstract ListProperty<CompilerInput> getSwiftCompilerInputs();

	private Provider<List<CompilerInput>> fromSwiftCompileConfiguration() {
		return getProviders().provider(() -> importSwiftModulesConfiguration.getIncoming().getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
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
