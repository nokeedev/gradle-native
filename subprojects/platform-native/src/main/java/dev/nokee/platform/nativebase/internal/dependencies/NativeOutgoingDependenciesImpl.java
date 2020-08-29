package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.DependencyBucketName;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import lombok.Getter;
import lombok.val;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public final class NativeOutgoingDependenciesImpl implements NativeOutgoingDependencies {
	@Getter private final DirectoryProperty exportedHeaders;
	@Getter private final RegularFileProperty exportedSwiftModule;
	@Getter private final Property<Binary> exportedBinary;

	private NativeOutgoingDependenciesImpl(ObjectFactory objectFactory) {
		this.exportedHeaders = objectFactory.directoryProperty();
		this.exportedSwiftModule = objectFactory.fileProperty();
		this.exportedBinary = objectFactory.property(Binary.class);
	}

	public static Builder builder(NativeComponentDependencies dependencies) {
		return new Builder(dependencies);
	}

	public static class Builder {
		private final NativeComponentDependencies dependencies;
		private boolean hasOutgoingNativeHeaders = false;
		private boolean hasOutgoingSwiftModules = false;
		private boolean hasExportedApi = false;
		private BuildVariantInternal buildVariant = null;

		public Builder(NativeComponentDependencies dependencies) {
			this.dependencies = dependencies;
		}

		public Builder withOutgoingHeaders() {
			hasOutgoingNativeHeaders = true;
			return this;
		}

		public Builder withOutgoingSwiftModules() {
			hasOutgoingSwiftModules = true;
			return this;
		}

		public Builder withExportedApi() {
			hasExportedApi = true;
			return this;
		}

		public Builder withVariant(BuildVariantInternal buildVariant) {
			assert buildVariant != null;
			this.buildVariant = buildVariant;
			return this;
		}

		public NativeOutgoingDependenciesImpl buildUsing(ObjectFactory objectFactory) {
			assert !(hasOutgoingNativeHeaders && hasOutgoingSwiftModules);
			assert objectFactory != null;

			val outgoingDependencies = new NativeOutgoingDependenciesImpl(objectFactory);
			val dependencyContainer = (ComponentDependenciesContainer) dependencies;

			if (hasExportedApi) {
				if (hasOutgoingNativeHeaders) {
					dependencyContainer.register(DependencyBucketName.of("apiElements"), ConsumableNativeHeaders.class, it -> {
						it.extendsFrom(asLibraryDependencies(dependencies).getApi(), dependencies.getCompileOnly());
						it.variant(buildVariant);
						it.headerSearchPath(outgoingDependencies.getExportedHeaders());
					});
				}
				if (hasOutgoingSwiftModules) {
					dependencyContainer.register(DependencyBucketName.of("apiElements"), ConsumableSwiftModules.class, it -> {
						it.extendsFrom(asLibraryDependencies(dependencies).getApi(), dependencies.getCompileOnly());
						it.variant(buildVariant);
						it.swiftModule(outgoingDependencies.getExportedSwiftModule());
					});
				}

				dependencyContainer.register(DependencyBucketName.of("linkElements"), ConsumableLinkLibraries.class, it -> {
					it.extendsFrom(asLibraryDependencies(dependencies).getApi(), dependencies.getLinkOnly());
					it.variant(buildVariant);
					it.binary(outgoingDependencies.getExportedBinary());
				});
				dependencyContainer.register(DependencyBucketName.of("runtimeElements"), ConsumableRuntimeLibraries.class, it -> {
					// FIXME: I'm pretty sure we should be using api instead of implementation
					it.extendsFrom(dependencies.getImplementation(), dependencies.getRuntimeOnly());
					it.variant(buildVariant);
					it.binary(outgoingDependencies.getExportedBinary());
				});
			} else {
				// TODO: Technically, they may be an application executable here
				dependencyContainer.register(DependencyBucketName.of("runtimeElements"), ConsumableRuntimeLibraries.class, it -> {
					it.extendsFrom(dependencies.getImplementation(), dependencies.getRuntimeOnly());
					it.variant(buildVariant);
					it.binary(outgoingDependencies.getExportedBinary());
				});
			}

			return outgoingDependencies;
		}

		private static NativeLibraryComponentDependencies asLibraryDependencies(NativeComponentDependencies dependencies) {
			return (NativeLibraryComponentDependencies) dependencies;
		}
	}
}
