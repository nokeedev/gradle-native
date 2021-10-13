/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.dependencies.*;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.runtime.darwin.internal.DarwinLibraryElements;
import dev.nokee.utils.ActionUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.ARTIFACT_COMPRESSION_STATE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.UNCOMPRESSED;

public class DefaultNativeIncomingDependencies implements NativeIncomingDependencies {
	private static final Logger LOGGER = Logger.getLogger(DefaultNativeIncomingDependencies.class.getCanonicalName());
	private final IncomingHeaders headers;
	private final IncomingSwiftModules swiftModules;
	@Getter private final DependencyBucket linkLibrariesBucket;
	@Getter private final DependencyBucket runtimeLibrariesBucket;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;

	@Inject
	@Deprecated // Use {@link #builder()} instead.
	public DefaultNativeIncomingDependencies(IncomingHeaders headers, IncomingSwiftModules swiftModules, DependencyBucket linkLibrariesBucket, DependencyBucket runtimeLibrariesBucket, ObjectFactory objects, ProviderFactory providers) {
		this.headers = headers;
		this.swiftModules = swiftModules;
		this.linkLibrariesBucket = linkLibrariesBucket;
		this.runtimeLibrariesBucket = runtimeLibrariesBucket;
		this.objects = objects;
		this.providers = providers;

		this.linkerInputs = objects.listProperty(LinkerInput.class);

		configureLinkerInputs();
	}

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
		return getProviders().provider(() -> linkLibrariesBucket.getAsConfiguration().getIncoming().artifactView(it -> it.getAttributes().attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, UNCOMPRESSED)).getArtifacts().getArtifacts().stream().map(LinkerInput::of).collect(Collectors.toList()));
	}

	@Getter private final ListProperty<LinkerInput> linkerInputs;

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
		private final NativeComponentDependencies componentDependencies;
		private boolean hasIncomingHeaders = false;
		private boolean hasIncomingSwiftModules = false;
		private VariantIdentifier<?> ownerIdentifier = null;
		private DependencyBucketFactory bucketFactory = null;
		private BuildVariantInternal buildVariant;

		private Builder(NativeComponentDependencies dependencies) {
			this.componentDependencies = dependencies;
		}

		public Builder withIncomingHeaders() {
			hasIncomingHeaders = true;
			return this;
		}

		public Builder withIncomingSwiftModules() {
			hasIncomingSwiftModules = true;
			return this;
		}

		public Builder withOwnerIdentifier(VariantIdentifier<?> owner) {
			this.ownerIdentifier = owner;
			return this;
		}

		public Builder withBucketFactory(DependencyBucketFactory factory) {
			this.bucketFactory = factory;
			return this;
		}

		public Builder withVariant(BuildVariantInternal buildVariant) {
			this.buildVariant = buildVariant;
			return this;
		}

		public DefaultNativeIncomingDependencies buildUsing(ObjectFactory objects) {
			ComponentDependenciesInternal dependenciesInternal = (ComponentDependenciesInternal) componentDependencies;
			ExtensionContainer dependencies = ((ExtensionAware) componentDependencies).getExtensions();

			Function<String, String> withPrefix = Function.identity();
			if (componentDependencies.getClass().getSimpleName().contains("JavaNativeInterface")) {
				withPrefix = (String it) -> "native" + StringUtils.capitalize(it);
			}

			val compileOnlyBucket = Optional.ofNullable((DependencyBucket) dependencies.findByName("compileOnly")); // As we reuse this code in JNI
			IncomingHeaders incomingHeaders = null;
			if (hasIncomingHeaders) {
				val identifier = DependencyBucketIdentifier.of(DependencyBucketName.of(withPrefix.apply("headerSearchPaths")), ResolvableDependencyBucket.class, ownerIdentifier);
				val bucket = bucketFactory.create(identifier);
				ActionUtils.Action.of(ConfigurationUtilsEx.asIncomingHeaderSearchPathFrom(componentDependencies.getImplementation()))
					.andThen(compileOnlyBucket.map(this::extendsFrom).orElse(ActionUtils.doNothing()))
					.andThen(ConfigurationUtilsEx.configureIncomingAttributes(buildVariant, objects))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible)
					.andThen(it -> it.setDescription(identifier.getDisplayName()))
					.execute(bucket.getAsConfiguration());
				incomingHeaders = objects.newInstance(DefaultIncomingHeaders.class, dependenciesInternal.add(bucket));
			} else {
				incomingHeaders = new AbsentIncomingHeaders(objects);
			}

			IncomingSwiftModules incomingSwiftModules = null;
			if (hasIncomingSwiftModules) {
				val identifier = DependencyBucketIdentifier.of(DependencyBucketName.of(withPrefix.apply("importSwiftModules")), ResolvableDependencyBucket.class, ownerIdentifier);
				val bucket = bucketFactory.create(identifier);
				ActionUtils.Action.of(ConfigurationUtilsEx.asIncomingSwiftModuleFrom(componentDependencies.getImplementation()))
					.andThen(compileOnlyBucket.map(this::extendsFrom).orElse(ActionUtils.doNothing()))
					.andThen(ConfigurationUtilsEx.configureIncomingAttributes(buildVariant, objects))
					.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible)
					.andThen(it -> it.setDescription(identifier.getDisplayName()))
					.execute(bucket.getAsConfiguration());
				incomingSwiftModules = objects.newInstance(DefaultIncomingSwiftModules.class, dependenciesInternal.add(bucket));
			} else {
				incomingSwiftModules = new AbsentIncomingSwiftModules(objects);
			}

			val linkLibrariesBucketIdentifier = DependencyBucketIdentifier.of(DependencyBucketName.of(withPrefix.apply("linkLibraries")), ResolvableDependencyBucket.class, ownerIdentifier);
			val linkLibrariesBucket = bucketFactory.create(linkLibrariesBucketIdentifier);
			ActionUtils.Action.of(ConfigurationUtilsEx.asIncomingLinkLibrariesFrom(componentDependencies.getImplementation(), componentDependencies.getLinkOnly()))
				.andThen(ConfigurationUtilsEx.configureIncomingAttributes(buildVariant, objects))
				.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible)
				.andThen(it -> it.setDescription(linkLibrariesBucketIdentifier.getDisplayName()))
				.execute(linkLibrariesBucket.getAsConfiguration());
			val runtimeLibrariesBucketIdentifier = DependencyBucketIdentifier.of(DependencyBucketName.of(withPrefix.apply("runtimeLibraries")), ResolvableDependencyBucket.class, ownerIdentifier);
			val runtimeLibrariesBucket = bucketFactory.create(runtimeLibrariesBucketIdentifier);
			ActionUtils.Action.of(ConfigurationUtilsEx.asIncomingRuntimeLibrariesFrom(componentDependencies.getImplementation(), componentDependencies.getRuntimeOnly()))
				.andThen(ConfigurationUtilsEx.configureIncomingAttributes(buildVariant, objects))
				.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible)
				.andThen(it -> it.setDescription(runtimeLibrariesBucketIdentifier.getDisplayName()))
				.execute(runtimeLibrariesBucket.getAsConfiguration());

			return objects.newInstance(DefaultNativeIncomingDependencies.class, incomingHeaders, incomingSwiftModules, dependenciesInternal.add(linkLibrariesBucket), dependenciesInternal.add(runtimeLibrariesBucket));
		}

		private ActionUtils.Action<Configuration> extendsFrom(DependencyBucket bucket) {
			return configuration -> configuration.extendsFrom(bucket.getAsConfiguration());
		}
	}
	//endregion

	public static boolean isFrameworkDependency(ResolvedArtifactResult result) {
		Optional<Attribute<?>> attribute = result.getVariant().getAttributes().keySet().stream().filter(it -> it.getName().equals(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE.getName())).findFirst();
		if (attribute.isPresent()) {
			String v = result.getVariant().getAttributes().getAttribute(attribute.get()).toString();
			if (v.equals(DarwinLibraryElements.FRAMEWORK_BUNDLE)) {
				return true;
			}
			return false;
		}
		LOGGER.finest(() -> "No library elements on dependency\n" + result.getVariant().getAttributes().keySet().stream().map(Attribute::getName).collect(Collectors.joining(", ")));
		return false;
	}

	//region incoming headers
	public interface IncomingHeaders {
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

	static class DefaultIncomingHeaders implements IncomingHeaders {
		private final DependencyBucket headerSearchPathsBucket;
		@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
		@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;

		@Inject
		public DefaultIncomingHeaders(DependencyBucket headerSearchPathsBucket, ObjectFactory objects, ProviderFactory providers) {
			this.headerSearchPathsBucket = headerSearchPathsBucket;
			this.objects = objects;
			this.providers = providers;

			this.nativeCompilerInputs = objects.listProperty(CompilerInput.class);
			configureNativeCompilerInputs();
		}

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

		@Getter(AccessLevel.PROTECTED) private final ListProperty<CompilerInput> nativeCompilerInputs;

		private Provider<List<CompilerInput>> fromNativeCompileConfiguration() {
			return getProviders().provider(() -> headerSearchPathsBucket.getAsConfiguration().getIncoming().artifactView(it -> it.getAttributes().attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, UNCOMPRESSED)).getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
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
	public interface IncomingSwiftModules {
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

	static class DefaultIncomingSwiftModules implements IncomingSwiftModules {
		private final DependencyBucket importSwiftModulesBucket;
		@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
		@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;

		@Inject
		public DefaultIncomingSwiftModules(DependencyBucket importSwiftModulesBucket, ObjectFactory objects, ProviderFactory providers) {
			this.importSwiftModulesBucket = importSwiftModulesBucket;
			this.swiftCompilerInputs = objects.listProperty(CompilerInput.class);
			this.objects = objects;
			this.providers = providers;
			configureSwiftCompilerInputs();
		}

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

		@Getter(AccessLevel.PROTECTED) private final ListProperty<CompilerInput> swiftCompilerInputs;

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
