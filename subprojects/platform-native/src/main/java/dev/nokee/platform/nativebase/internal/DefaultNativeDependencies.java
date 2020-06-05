package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.NamingScheme;
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

public abstract class DefaultNativeDependencies implements NativeDependencies {
	private final ConfigurationUtils configurationUtils = getObjects().newInstance(ConfigurationUtils.class);
	private final NamingScheme names;
	private final DefaultTargetMachine targetMachine;

	@Inject
	public DefaultNativeDependencies(NamingScheme names, DefaultTargetMachine targetMachine) {
		this.names = names;
		this.targetMachine = targetMachine;

		configureNativeCompilerInputs();
		configureSwiftCompilerInputs();
		configureLinkerInputs();
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ProviderFactory getProviders();

	//region Compiler inputs
	//region C/C++/Objective-C/Objective-C compiler inputs
	@Override
	public FileCollection getHeaderSearchPaths() {
		return getObjects().fileCollection().from(getNativeCompilerInputs().map(this::toHeaderSearchPaths)).builtBy((Callable)() -> getHeaderSearchPathsConfiguration());
	}

	private void configureNativeCompilerInputs() {
		getNativeCompilerInputs().set(fromNativeCompileConfiguration());
		getNativeCompilerInputs().finalizeValueOnRead();
		getNativeCompilerInputs().disallowChanges();
	}

	protected abstract ListProperty<CompilerInput> getNativeCompilerInputs();

	private Provider<List<CompilerInput>> fromNativeCompileConfiguration() {
		return getProviders().provider(() -> getHeaderSearchPathsConfiguration().getIncoming().getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
	}

	public  Configuration getHeaderSearchPathsConfiguration() {
		String name = names.getConfigurationNameWithoutPrefix("headerSearchPaths");
		if (hasConfiguration(name)) {
			return getConfigurations().getByName(name);
		}
		return getConfigurations().create(name, configurationUtils.asIncomingHeaderSearchPathFrom(getConfigurations().getByName(names.getConfigurationName("implementation")), getConfigurations().getByName(names.getConfigurationName("compileOnly"))));
	}
	//endregion

	//region Swift compiler inputs
	@Override
	public FileCollection getImportSearchPaths() {
		return getObjects().fileCollection().from(getSwiftCompilerInputs().map(this::toHeaderSearchPaths)).builtBy((Callable)() -> getImportSearchPathsConfiguration());
	}

	private void configureSwiftCompilerInputs() {
		getSwiftCompilerInputs().set(fromSwiftCompileConfiguration());
		getSwiftCompilerInputs().finalizeValueOnRead();
		getSwiftCompilerInputs().disallowChanges();
	}

	protected abstract ListProperty<CompilerInput> getSwiftCompilerInputs();

	private Provider<List<CompilerInput>> fromSwiftCompileConfiguration() {
		return getProviders().provider(() -> getHeaderSearchPathsConfiguration().getIncoming().getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
	}

	private Configuration getImportSearchPathsConfiguration() {
		String name = names.getConfigurationNameWithoutPrefix("importSearchPaths");
		if (hasConfiguration(name)) {
			return getConfigurations().getByName(name);
		}
		return getConfigurations().create(name, configurationUtils.asIncomingHeaderSearchPathFrom(getConfigurations().getByName(names.getConfigurationName("implementation")), getConfigurations().getByName(names.getConfigurationName("compileOnly"))).withDescription("TODO"));
	}
	//endregion

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
	//endregion

	@Override
	public FileCollection getFrameworkSearchPaths() {
		// TODO: It should aggregate inputs from Swift configuration as well
		return getObjects().fileCollection().from(getNativeCompilerInputs().map(this::toFrameworkSearchPaths)).builtBy(getHeaderSearchPathsConfiguration());
	}

	//region Linker inputs
	@Override
	public FileCollection getLinkLibraries() {
		return getObjects().fileCollection().from(getLinkerInputs().map(this::toLinkLibraries)).builtBy(getLinkLibrariesConfiguration());
	}

	@Override
	public FileCollection getLinkFrameworks() {
		return getObjects().fileCollection().from(getLinkerInputs().map(this::toLinkFrameworks)).builtBy(getLinkLibrariesConfiguration());
	}

	private void configureLinkerInputs() {
		getLinkerInputs().set(fromLinkConfiguration());
		getLinkerInputs().finalizeValueOnRead();
		getLinkerInputs().disallowChanges();
	}

	public Configuration getLinkLibrariesConfiguration() {
		String name = names.getConfigurationName("linkLibraries");
		if (hasConfiguration(name)) {
			return getConfigurations().getByName(name);
		}
		return getConfigurations().create(name, configurationUtils.asIncomingLinkLibrariesFrom(getConfigurations().getByName(names.getConfigurationName("implementation")), getConfigurations().getByName(names.getConfigurationName("linkOnly"))).forTargetMachine(targetMachine).asDebug().withDescription(names.getConfigurationDescription("Link libraries for %s.")));
	}

	private Provider<List<LinkerInput>> fromLinkConfiguration() {
		return getProviders().provider(() -> getLinkLibrariesConfiguration().getIncoming().getArtifacts().getArtifacts().stream().map(LinkerInput::of).collect(Collectors.toList()));
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

	public Configuration getRuntimeLibrariesConfiguration() {
		String name = names.getConfigurationName("runtimeLibraries");
		if (hasConfiguration(name)) {
			return getConfigurations().getByName(name);
		}
		return getConfigurations().create(name, configurationUtils.asIncomingRuntimeLibrariesFrom(getConfigurations().getByName(names.getConfigurationName("implementation")), getConfigurations().getByName(names.getConfigurationName("runtimeOnly"))).forTargetMachine(targetMachine).asDebug().withDescription(names.getConfigurationDescription("Runtime libraries for %s.")));
	}

	@Override
	public FileCollection getRuntimeLibraries() {
		return getObjects().fileCollection().from((Callable<FileCollection>) this::getRuntimeLibrariesConfiguration);
	}

	private boolean hasConfiguration(String name) {
		return StreamSupport.stream(getConfigurations().getCollectionSchema().getElements().spliterator(), false).anyMatch(it -> it.getName().equals(name));
	}
}
