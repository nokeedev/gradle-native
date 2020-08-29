package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.DependencyBucketName;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import lombok.val;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;

public final class NativeIncomingDependenciesImpl implements NativeIncomingDependencies {
	private final NativeHeaders headers;
	private final SwiftModules swiftModules;
	private final ResolvableLinkLibraries linkLibrariesBucket;
	private final ResolvableRuntimeLibraries runtimeLibrariesBucket;
	private final ObjectFactory objectFactory;

	private NativeIncomingDependenciesImpl(NativeHeaders headers, SwiftModules swiftModules, ResolvableLinkLibraries linkLibrariesBucket, ResolvableRuntimeLibraries runtimeLibrariesBucket, ObjectFactory objectFactory) {
		this.headers = headers;
		this.swiftModules = swiftModules;
		this.linkLibrariesBucket = linkLibrariesBucket;
		this.runtimeLibrariesBucket = runtimeLibrariesBucket;
		this.objectFactory = objectFactory;
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
		return objectFactory.fileCollection().from(headers.getFrameworkSearchPaths()).from(swiftModules.getFrameworkSearchPaths());
	}

	@Override
	public FileCollection getLinkLibraries() {
		return linkLibrariesBucket.getLinkLibraries();
	}

	@Override
	public FileCollection getLinkFrameworks() {
		return linkLibrariesBucket.getLinkFrameworks();
	}

	@Override
	public FileCollection getRuntimeLibraries() {
		return runtimeLibrariesBucket.getAsFiles();
	}

	//region builder implementation
	public static Builder builder(NativeComponentDependencies dependencies) {
		return new Builder(dependencies);
	}

	public static final class Builder {
		private final NativeComponentDependencies dependencies;
		private boolean hasIncomingHeaders = false;
		private boolean hasIncomingSwiftModules = false;
		private BuildVariantInternal buildVariant;

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

		public Builder withVariant(BuildVariantInternal buildVariant) {
			this.buildVariant = buildVariant;
			return this;
		}

		public NativeIncomingDependenciesImpl buildUsing(ObjectFactory objects) {
			ComponentDependenciesContainer dependenciesInternal = (ComponentDependenciesContainer) dependencies;
			val compileOnlyBucket = dependenciesInternal.findByName(DependencyBucketName.of("compileOnly")); // As we reuse this code in JNI
			NativeHeaders incomingHeaders = null;
			if (hasIncomingHeaders) {
				incomingHeaders = dependenciesInternal.register(DependencyBucketName.of("headerSearchPaths"), ResolvableNativeHeaders.class, it -> {
					it.extendsFrom(dependencies.getImplementation());
					compileOnlyBucket.ifPresent(it::extendsFrom);
					it.variant(buildVariant);
				});
			} else {
				incomingHeaders = new AbsentIncomingHeaders(objects);
			}

			SwiftModules incomingSwiftModules = null;
			if (hasIncomingSwiftModules) {
				incomingSwiftModules = dependenciesInternal.register(DependencyBucketName.of("importSwiftModules"), ResolvableSwiftModules.class, it -> {
					it.extendsFrom(dependencies.getImplementation());
					compileOnlyBucket.ifPresent(it::extendsFrom);
					it.variant(buildVariant);
				});
			} else {
				incomingSwiftModules = new AbsentIncomingSwiftModules(objects);
			}

			val linkLibrariesBucket = dependenciesInternal.register(DependencyBucketName.of("linkLibraries"), ResolvableLinkLibraries.class, it -> {
				it.extendsFrom(dependencies.getImplementation(), dependencies.getLinkOnly());
				it.variant(buildVariant);
			});
			val runtimeLibrariesBucket = dependenciesInternal.register(DependencyBucketName.of("runtimeLibraries"), ResolvableRuntimeLibraries.class, it -> {
				it.extendsFrom(dependencies.getImplementation(), dependencies.getRuntimeOnly());
				it.variant(buildVariant);
			});

			return new NativeIncomingDependenciesImpl(incomingHeaders, incomingSwiftModules, linkLibrariesBucket, runtimeLibrariesBucket, objects);
		}
	}
	//endregion

	//region incoming headers
	private static class AbsentIncomingHeaders implements NativeHeaders {
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
	//endregion

	//region incoming Swift modules
	private static class AbsentIncomingSwiftModules implements SwiftModules {
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
	//endregion
}
