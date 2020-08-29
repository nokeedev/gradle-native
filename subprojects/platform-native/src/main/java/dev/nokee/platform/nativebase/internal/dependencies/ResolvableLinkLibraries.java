package dev.nokee.platform.nativebase.internal.dependencies;

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

public final class ResolvableLinkLibraries extends BaseNativeResolvableDependencyBucket {
	private final ProviderFactory providerFactory;
	private final ObjectFactory objectFactory;
	private final ListProperty<LinkerInput> linkerInputs;

	@Inject
	public ResolvableLinkLibraries(ProviderFactory providerFactory, ObjectFactory objectFactory) {
		super(objectFactory);
		this.providerFactory = providerFactory;
		this.objectFactory = objectFactory;
		getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.NATIVE_LINK));
		this.linkerInputs = objectFactory.listProperty(LinkerInput.class);
		configureLinkerInputs();
	}

	public FileCollection getLinkLibraries() {
		return objectFactory.fileCollection().from(linkerInputs.map(this::toLinkLibraries)).builtBy(this);
	}

	public FileCollection getLinkFrameworks() {
		return objectFactory.fileCollection().from(linkerInputs.map(this::toLinkFrameworks)).builtBy(this);
	}

	private void configureLinkerInputs() {
		linkerInputs.set(fromLinkConfiguration());
		linkerInputs.finalizeValueOnRead();
		linkerInputs.disallowChanges();
	}

	private Provider<List<LinkerInput>> fromLinkConfiguration() {
		return providerFactory.provider(() -> getIncoming().getArtifacts().getArtifacts().stream().map(LinkerInput::of).collect(Collectors.toList()));
	}

	private List<File> toLinkLibraries(List<LinkerInput> inputs) {
		return inputs.stream().filter(it -> !it.isFramework()).map(LinkerInput::getFile).collect(Collectors.toList());
	}

	private List<File> toLinkFrameworks(List<LinkerInput> inputs) {
		return inputs.stream().filter(LinkerInput::isFramework).map(LinkerInput::getFile).collect(Collectors.toList());
	}

	@Value
	static class LinkerInput {
		boolean framework;
		File file;

		public static LinkerInput of(ResolvedArtifactResult result) {
			return new LinkerInput(isFrameworkDependency(result), result.getFile());
		}
	}
}
