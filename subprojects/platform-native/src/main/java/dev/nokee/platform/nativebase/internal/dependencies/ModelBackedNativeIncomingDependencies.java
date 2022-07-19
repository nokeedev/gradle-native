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

import com.google.common.base.Suppliers;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.runtime.darwin.internal.DarwinLibraryElements;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.ARTIFACT_COMPRESSION_STATE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.UNCOMPRESSED;

public class ModelBackedNativeIncomingDependencies implements NativeIncomingDependencies, ModelComponent {
	private static final Logger LOGGER = Logger.getLogger(ModelBackedNativeIncomingDependencies.class.getCanonicalName());
	private final Supplier<IncomingHeaders> headers;
	private final Supplier<IncomingSwiftModules> swiftModules;
	private final ObjectFactory objects;
	private final ProviderFactory providers;

	public ModelBackedNativeIncomingDependencies(ModelPath owner, ObjectFactory objects, ProviderFactory providers, ModelLookup lookup) {
		this(owner, objects, providers, lookup, UnaryOperator.identity());
	}

	public ModelBackedNativeIncomingDependencies(ModelPath owner, ObjectFactory objects, ProviderFactory providers, ModelLookup lookup, UnaryOperator<String> prefix) {
		this.objects = objects;
		this.providers = providers;
		this.headers = Suppliers.memoize(() -> {
			lookup.find(owner.child(prefix.apply("headerSearchPaths"))).ifPresent(ModelStates::realize);
			return lookup.find(owner.child(prefix.apply("headerSearchPaths"))).<IncomingHeaders>map(entity -> new DefaultIncomingHeaders(ModelNodeUtils.get(entity, DependencyBucket.class))).orElseGet(AbsentIncomingHeaders::new);
		});
		this.swiftModules = Suppliers.memoize(() -> {
			lookup.find(owner.child(prefix.apply("importSwiftModules"))).ifPresent(ModelStates::realize);
			return lookup.find(owner.child(prefix.apply("importSwiftModules"))).<IncomingSwiftModules>map(entity -> new DefaultIncomingSwiftModules(ModelNodeUtils.get(entity, DependencyBucket.class))).orElseGet(AbsentIncomingSwiftModules::new);
		});
	}

	@Override
	public FileCollection getSwiftModules() {
		return objects.fileCollection().from((Callable<Object>) () -> swiftModules.get().getSwiftModules());
	}

	@Override
	public FileCollection getHeaderSearchPaths() {
		return objects.fileCollection().from((Callable<Object>) () -> headers.get().getHeaderSearchPaths());
	}

	@Override
	public FileCollection getFrameworkSearchPaths() {
		return objects.fileCollection().from((Callable<Object>) () -> headers.get().getFrameworkSearchPaths()).from((Callable<Object>) () -> swiftModules.get().getFrameworkSearchPaths());
	}


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

	private final class AbsentIncomingHeaders implements IncomingHeaders {
		@Override
		public FileCollection getHeaderSearchPaths() {
			return objects.fileCollection();
		}

		@Override
		public FileCollection getFrameworkSearchPaths() {
			return objects.fileCollection();
		}
	}

	private final class DefaultIncomingHeaders implements IncomingHeaders {
		private final DependencyBucket headerSearchPathsBucket;

		@Inject
		public DefaultIncomingHeaders(DependencyBucket headerSearchPathsBucket) {
			this.headerSearchPathsBucket = headerSearchPathsBucket;

			this.nativeCompilerInputs = objects.listProperty(CompilerInput.class);
			configureNativeCompilerInputs();
		}

		@Override
		public FileCollection getHeaderSearchPaths() {
			return objects.fileCollection().from(getNativeCompilerInputs().map(this::toHeaderSearchPaths)).builtBy(headerSearchPathsBucket.getAsConfiguration());
		}

		@Override
		public FileCollection getFrameworkSearchPaths() {
			return objects.fileCollection().from(getNativeCompilerInputs().map(this::toFrameworkSearchPaths)).builtBy(headerSearchPathsBucket.getAsConfiguration());
		}

		private void configureNativeCompilerInputs() {
			getNativeCompilerInputs().set(fromNativeCompileConfiguration());
			getNativeCompilerInputs().finalizeValueOnRead();
			getNativeCompilerInputs().disallowChanges();
		}

		@Getter(AccessLevel.PROTECTED) private final ListProperty<CompilerInput> nativeCompilerInputs;

		private Provider<List<CompilerInput>> fromNativeCompileConfiguration() {
			return providers.provider(() -> headerSearchPathsBucket.getAsConfiguration().getIncoming().artifactView(it -> it.getAttributes().attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, UNCOMPRESSED)).getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
		}

		private List<File> toHeaderSearchPaths(List<CompilerInput> inputs) {
			return inputs.stream().filter(it -> !it.isFramework()).map(CompilerInput::getFile).collect(Collectors.toList());
		}

		private List<File> toFrameworkSearchPaths(List<CompilerInput> inputs) {
			return inputs.stream().filter(it -> it.isFramework()).map(it -> it.getFile().getParentFile()).collect(Collectors.toList());
		}
	}
	//endregion

	//region incoming Swift modules
	public interface IncomingSwiftModules {
		FileCollection getSwiftModules();
		FileCollection getFrameworkSearchPaths();
	}

	private final class AbsentIncomingSwiftModules implements IncomingSwiftModules {
		@Override
		public FileCollection getSwiftModules() {
			return objects.fileCollection();
		}

		@Override
		public FileCollection getFrameworkSearchPaths() {
			return objects.fileCollection();
		}
	}

	private final class DefaultIncomingSwiftModules implements IncomingSwiftModules {
		private final DependencyBucket importSwiftModulesBucket;

		@Inject
		public DefaultIncomingSwiftModules(DependencyBucket importSwiftModulesBucket) {
			this.importSwiftModulesBucket = importSwiftModulesBucket;
			this.swiftCompilerInputs = objects.listProperty(CompilerInput.class);
			configureSwiftCompilerInputs();
		}

		@Override
		public FileCollection getSwiftModules() {
			return objects.fileCollection().from(getSwiftCompilerInputs().map(this::toSwiftModules)).builtBy(importSwiftModulesBucket.getAsConfiguration());
		}

		@Override
		public FileCollection getFrameworkSearchPaths() {
			return objects.fileCollection().from(getSwiftCompilerInputs().map(this::toFrameworkSearchPaths)).builtBy(importSwiftModulesBucket.getAsConfiguration());
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
			return providers.provider(() -> importSwiftModulesBucket.getAsConfiguration().getIncoming().getArtifacts().getArtifacts().stream().map(CompilerInput::of).collect(Collectors.toList()));
		}
	}
	//endregion

	@Value
	public static class CompilerInput {
		boolean framework;
		File file;

		public static CompilerInput of(ResolvedArtifactResult result) {
			return new CompilerInput(isFrameworkDependency(result), result.getFile());
		}
	}
}
