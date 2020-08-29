package dev.nokee.platform.nativebase.internal.dependencies;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.attributes.Usage;
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

public final class ResolvableSwiftModules extends BaseNativeResolvableDependencyBucket implements SwiftModules {
	private final ProviderFactory providerFactory;
	private final ObjectFactory objectFactory;

	@Inject
	public ResolvableSwiftModules(ProviderFactory providerFactory, ObjectFactory objectFactory) {
		super(objectFactory);
		this.swiftCompilerInputs = objectFactory.listProperty(CompilerInput.class);
		this.providerFactory = providerFactory;
		this.objectFactory = objectFactory;
		getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.SWIFT_API));
		configureSwiftCompilerInputs();
	}

	@Override
	public FileCollection getSwiftModules() {
		return objectFactory.fileCollection().from(getSwiftCompilerInputs().map(this::toSwiftModules)).builtBy(this);
	}

	@Override
	public FileCollection getFrameworkSearchPaths() {
		return objectFactory.fileCollection().from(getSwiftCompilerInputs().map(this::toFrameworkSearchPaths)).builtBy(this);
	}

	private List<File> toSwiftModules(List<CompilerInput> inputs) {
		return inputs.stream().filter(it -> !it.isFramework()).map(CompilerInput::getFile).collect(Collectors.toList());
	}

	private List<File> toFrameworkSearchPaths(List<CompilerInput> inputs) {
		return inputs.stream().filter(CompilerInput::isFramework).map(it -> it.getFile().getParentFile()).collect(Collectors.toList());
	}

	private void configureSwiftCompilerInputs() {
		getSwiftCompilerInputs().set(fromSwiftCompileConfiguration());
		getSwiftCompilerInputs().finalizeValueOnRead();
		getSwiftCompilerInputs().disallowChanges();
	}

	@Getter(AccessLevel.PROTECTED) private final ListProperty<CompilerInput> swiftCompilerInputs;

	private Provider<List<CompilerInput>> fromSwiftCompileConfiguration() {
		return providerFactory.provider(() -> getIncoming().getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
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
