package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.ChainingAction;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.runtime.nativebase.internal.LibraryElements;
import dev.nokee.utils.ActionUtils;
import lombok.Value;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class DefaultNativeIncomingDependencies implements NativeIncomingDependencies {
	private static final Logger LOGGER = Logger.getLogger(DefaultNativeIncomingDependencies.class.getCanonicalName());
	private final IncomingHeaders headers;
	private final IncomingSwiftModules swiftModules;
	private final DependencyBucket linkLibrariesBucket;
	private final DependencyBucket runtimeLibrariesBucket;

	@Inject
	@Deprecated // Use {@link #builder()} instead.
	public DefaultNativeIncomingDependencies(IncomingHeaders headers, IncomingSwiftModules swiftModules, DependencyBucket linkLibrariesBucket, DependencyBucket runtimeLibrariesBucket) {
		this.headers = headers;
		this.swiftModules = swiftModules;
		this.linkLibrariesBucket = linkLibrariesBucket;
		this.runtimeLibrariesBucket = runtimeLibrariesBucket;

		configureLinkerInputs();
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public FileCollection getSwiftModules() {
		return swiftModules.getSwiftModules();
	}

	@Override
	public FileCollection getHeaderSearchPaths() {
		return headers.getHeaderSearchPaths();
	}

	@Override
	public FileCollection getFrameworkSearchPaths() {
		return getObjects().fileCollection().from(headers.getFrameworkSearchPaths()).from(swiftModules.getFrameworkSearchPaths());
	}

	//region Linker inputs
	public FileCollection getLinkLibraries() {
		return getObjects().fileCollection().from(getLinkerInputs().map(this::toLinkLibraries)).builtBy(linkLibrariesBucket.getAsConfiguration());
	}

	public FileCollection getLinkFrameworks() {
		return getObjects().fileCollection().from(getLinkerInputs().map(this::toLinkFrameworks)).builtBy(linkLibrariesBucket.getAsConfiguration());
	}

	private void configureLinkerInputs() {
		getLinkerInputs().set(fromLinkConfiguration());
		getLinkerInputs().finalizeValueOnRead();
		getLinkerInputs().disallowChanges();
	}

	private Provider<List<LinkerInput>> fromLinkConfiguration() {
		return getProviders().provider(() -> linkLibrariesBucket.getAsConfiguration().getIncoming().getArtifacts().getArtifacts().stream().map(LinkerInput::of).collect(Collectors.toList()));
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

	@Override
	public FileCollection getRuntimeLibraries() {
		return getObjects().fileCollection().from(runtimeLibrariesBucket.getAsConfiguration());
	}



	//region builder implementation
	public static Builder builder(NativeComponentDependencies dependencies) {
		return new Builder(dependencies);
	}

	public static final class Builder {
		private final NativeComponentDependencies dependencies;
		private boolean hasIncomingHeaders = false;
		private boolean hasIncomingSwiftModules = false;
		private BuildVariant buildVariant;

		private Builder(NativeComponentDependencies dependencies) {
			this.dependencies = dependencies;
		}

		public Builder withIncomingHeaders() {
			hasIncomingHeaders = true;
			return this;
		}

		public Builder withIncomingSwiftModules() {
			hasIncomingSwiftModules = true;
			return this;
		}

		public Builder withVariant(BuildVariant buildVariant) {
			this.buildVariant = buildVariant;
			return this;
		}

		public DefaultNativeIncomingDependencies buildUsing(ObjectFactory objects) {
			ComponentDependenciesInternal dependenciesInternal = (ComponentDependenciesInternal) dependencies;
			val compileOnlyBucket = dependenciesInternal.findByName("compileOnly"); // As we reuse this code in JNI
			IncomingHeaders incomingHeaders = null;
			if (hasIncomingHeaders) {
				val bucket = dependenciesInternal.create("headerSearchPaths", ChainingAction.of(ConfigurationUtilsEx.asIncomingHeaderSearchPathFrom(dependencies.getImplementation())).andThen(compileOnlyBucket.map(this::extendsFrom).orElse(ActionUtils.doNothing())).andThen(ConfigurationUtilsEx.withAttributes(buildVariant, objects)).andThen(it -> it.setDescription(String.format("Header search paths for %s.", dependenciesInternal.getComponentDisplayName()))));
				incomingHeaders = objects.newInstance(DefaultIncomingHeaders.class, bucket);
			} else {
				incomingHeaders = new AbsentIncomingHeaders(objects);
			}

			IncomingSwiftModules incomingSwiftModules = null;
			if (hasIncomingSwiftModules) {
				val bucket = dependenciesInternal.create("importSwiftModules", ChainingAction.of(ConfigurationUtilsEx.asIncomingSwiftModuleFrom(dependencies.getImplementation())).andThen(compileOnlyBucket.map(this::extendsFrom).orElse(ActionUtils.doNothing())).andThen(ConfigurationUtilsEx.withAttributes(buildVariant, objects)).andThen(it -> it.setDescription(String.format("Import Swift modules for %s.", dependenciesInternal.getComponentDisplayName()))));
				incomingSwiftModules = objects.newInstance(DefaultIncomingSwiftModules.class, bucket);
			} else {
				incomingSwiftModules = new AbsentIncomingSwiftModules(objects);
			}

			val linkLibrariesBucket = dependenciesInternal.create("linkLibraries", ChainingAction.of(ConfigurationUtilsEx.asIncomingLinkLibrariesFrom(dependencies.getImplementation(), dependencies.getLinkOnly())).andThen(ConfigurationUtilsEx.withAttributes(buildVariant, objects)).andThen(it -> it.setDescription(String.format("Link libraries for %s.", dependenciesInternal.getComponentDisplayName()))));
			val runtimeLibrariesBucket = dependenciesInternal.create("runtimeLibraries", ChainingAction.of(ConfigurationUtilsEx.asIncomingRuntimeLibrariesFrom(dependencies.getImplementation(), dependencies.getRuntimeOnly())).andThen(ConfigurationUtilsEx.withAttributes(buildVariant, objects)).andThen(it -> it.setDescription(String.format("Runtime libraries for %s.", dependenciesInternal.getComponentDisplayName()))));

			return objects.newInstance(DefaultNativeIncomingDependencies.class, incomingHeaders, incomingSwiftModules, linkLibrariesBucket, runtimeLibrariesBucket);
		}

		private Action<Configuration> extendsFrom(DependencyBucket bucket) {
			return configuration -> configuration.extendsFrom(bucket.getAsConfiguration());
		}
	}
	//endregion

	public static boolean isFrameworkDependency(ResolvedArtifactResult result) {
		Optional<Attribute<?>> attribute = result.getVariant().getAttributes().keySet().stream().filter(it -> it.getName().equals(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE.getName())).findFirst();
		if (attribute.isPresent()) {
			String v = result.getVariant().getAttributes().getAttribute(attribute.get()).toString();
			if (v.equals(LibraryElements.FRAMEWORK_BUNDLE)) {
				return true;
			}
			return false;
		}
		LOGGER.finest(() -> "No library elements on dependency\n" + result.getVariant().getAttributes().keySet().stream().map(Attribute::getName).collect(Collectors.joining(", ")));
		return false;
	}

	//region incoming headers
	private interface IncomingHeaders {
		FileCollection getHeaderSearchPaths();
		FileCollection getFrameworkSearchPaths();
	}

	private static class AbsentIncomingHeaders implements IncomingHeaders {
		private final ObjectFactory objects;

		private AbsentIncomingHeaders(ObjectFactory objects) {
			this.objects = objects;
		}

		@Override
		public FileCollection getHeaderSearchPaths() {
			return objects.fileCollection();
		}

		@Override
		public FileCollection getFrameworkSearchPaths() {
			return objects.fileCollection();
		}
	}

	static abstract class DefaultIncomingHeaders implements IncomingHeaders {
		private final DependencyBucket headerSearchPathsBucket;

		@Inject
		public DefaultIncomingHeaders(DependencyBucket headerSearchPathsBucket) {
			this.headerSearchPathsBucket = headerSearchPathsBucket;
			configureNativeCompilerInputs();
		}

		@Inject
		protected abstract ObjectFactory getObjects();

		@Inject
		protected abstract ProviderFactory getProviders();

		@Override
		public FileCollection getHeaderSearchPaths() {
			return getObjects().fileCollection().from(getNativeCompilerInputs().map(this::toHeaderSearchPaths)).builtBy(headerSearchPathsBucket.getAsConfiguration());
		}

		@Override
		public FileCollection getFrameworkSearchPaths() {
			return getObjects().fileCollection().from(getNativeCompilerInputs().map(this::toFrameworkSearchPaths)).builtBy(headerSearchPathsBucket.getAsConfiguration());
		}

		private void configureNativeCompilerInputs() {
			getNativeCompilerInputs().set(fromNativeCompileConfiguration());
			getNativeCompilerInputs().finalizeValueOnRead();
			getNativeCompilerInputs().disallowChanges();
		}

		protected abstract ListProperty<CompilerInput> getNativeCompilerInputs();

		private Provider<List<CompilerInput>> fromNativeCompileConfiguration() {
			return getProviders().provider(() -> headerSearchPathsBucket.getAsConfiguration().getIncoming().getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
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
	//endregion

	//region incoming Swift modules
	private interface IncomingSwiftModules {
		FileCollection getSwiftModules();
		FileCollection getFrameworkSearchPaths();
	}

	private static class AbsentIncomingSwiftModules implements IncomingSwiftModules {
		private final ObjectFactory objects;

		private AbsentIncomingSwiftModules(ObjectFactory objects) {
			this.objects = objects;
		}

		@Override
		public FileCollection getSwiftModules() {
			return objects.fileCollection();
		}

		@Override
		public FileCollection getFrameworkSearchPaths() {
			return objects.fileCollection();
		}
	}

	static abstract class DefaultIncomingSwiftModules implements IncomingSwiftModules {
		private final DependencyBucket importSwiftModulesBucket;

		@Inject
		public DefaultIncomingSwiftModules(DependencyBucket importSwiftModulesBucket) {
			this.importSwiftModulesBucket = importSwiftModulesBucket;
			configureSwiftCompilerInputs();
		}

		@Inject
		protected abstract ObjectFactory getObjects();

		@Inject
		protected abstract ProviderFactory getProviders();

		@Override
		public FileCollection getSwiftModules() {
			return getObjects().fileCollection().from(getSwiftCompilerInputs().map(this::toSwiftModules)).builtBy(importSwiftModulesBucket.getAsConfiguration());
		}

		@Override
		public FileCollection getFrameworkSearchPaths() {
			return getObjects().fileCollection().from(getSwiftCompilerInputs().map(this::toFrameworkSearchPaths)).builtBy(importSwiftModulesBucket.getAsConfiguration());
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
			return getProviders().provider(() -> importSwiftModulesBucket.getAsConfiguration().getIncoming().getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
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
	//endregion
}
