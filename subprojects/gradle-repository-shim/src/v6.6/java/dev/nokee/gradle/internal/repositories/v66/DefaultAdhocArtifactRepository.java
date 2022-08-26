/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.gradle.internal.repositories.v66;

import dev.nokee.gradle.AdhocArtifactRepository;
import dev.nokee.gradle.AdhocComponentLister;
import dev.nokee.gradle.AdhocComponentListerDetails;
import dev.nokee.gradle.AdhocComponentSupplier;
import dev.nokee.gradle.AdhocComponentSupplierDetails;
import dev.nokee.publishing.internal.metadata.GradleModuleMetadata;
import dev.nokee.publishing.internal.metadata.GradleModuleMetadataWriter;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.ComponentMetadataSupplierDetails;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ComponentResolvers;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ConfiguredModuleComponentRepository;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ModuleComponentRepositoryAccess;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvableArtifact;
import org.gradle.api.internal.artifacts.repositories.ArtifactResolutionDetails;
import org.gradle.api.internal.artifacts.repositories.ContentFilteringRepository;
import org.gradle.api.internal.artifacts.repositories.ResolutionAwareRepository;
import org.gradle.api.internal.artifacts.repositories.descriptor.RepositoryDescriptor;
import org.gradle.api.internal.artifacts.repositories.resolver.MetadataFetchingCost;
import org.gradle.api.internal.component.ArtifactType;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.internal.action.InstantiatingAction;
import org.gradle.internal.component.external.model.ModuleDependencyMetadata;
import org.gradle.internal.component.model.ComponentArtifactMetadata;
import org.gradle.internal.component.model.ComponentOverrideMetadata;
import org.gradle.internal.component.model.ComponentResolveMetadata;
import org.gradle.internal.component.model.ConfigurationMetadata;
import org.gradle.internal.component.model.ModuleSources;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.resolve.result.BuildableArtifactResolveResult;
import org.gradle.internal.resolve.result.BuildableArtifactSetResolveResult;
import org.gradle.internal.resolve.result.BuildableComponentArtifactsResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleVersionListingResolveResult;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.newWriter;
import static java.nio.file.Files.createDirectories;

public final class DefaultAdhocArtifactRepository implements AdhocArtifactRepository, ResolutionAwareRepository, ContentFilteringRepository {
	private final MavenArtifactRepository delegate;
	private final Property<AdhocComponentSupplier> supplierRule;
	private final Property<AdhocComponentLister> listerRule;
	private final Set<ModuleIdentifier> listedModules = new HashSet<>();
	private final Set<ModuleComponentIdentifier> resolvedComponents = new HashSet<>();
	private final DirectoryProperty cacheDirectory;

	public DefaultAdhocArtifactRepository(MavenArtifactRepository delegate, ObjectFactory objects) {
		this.delegate = delegate;
		this.cacheDirectory = objects.directoryProperty();
		this.cacheDirectory.finalizeValueOnRead();
		this.supplierRule = objects.property(AdhocComponentSupplier.class);
		this.listerRule = objects.property(AdhocComponentLister.class);
		delegate.setUrl(cacheDirectory.map(new Transformer<Object, Directory>() {
			private boolean realized = false;
			@Override
			public Object transform(Directory it) {
				if (!realized) {
					try {
						FileUtils.deleteDirectory(it.getAsFile());
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
					supplierRule.disallowChanges();
					listerRule.disallowChanges();
					realized = true;
				}
				return it;
			}
		}));
	}

	@Override
	public DirectoryProperty getCacheDirectory() {
		return cacheDirectory;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public void setName(String name) {
		delegate.setName(name);
	}

	@Override
	public void content(Action<? super RepositoryContentDescriptor> configureAction) {
		delegate.content(configureAction);
	}

	@Override
	public ConfiguredModuleComponentRepository createResolver() {
		return new ConfiguredModuleComponentRepositoryDelegate(((ResolutionAwareRepository) delegate).createResolver());
	}

	@Override
	public RepositoryDescriptor getDescriptor() {
		return ((ResolutionAwareRepository) delegate).getDescriptor();
	}

	@Override
	public Action<? super ArtifactResolutionDetails> getContentFilter() {
		return ((ContentFilteringRepository) delegate).getContentFilter();
	}

	@Override
	public void setComponentSupplier(AdhocComponentSupplier rule) {
		try {
			this.supplierRule.set(rule);
		} catch (IllegalStateException e) {
			throw new IllegalStateException("The component supplier cannot be changed because repository was already queried.", e);
		}
	}

	@Override
	public void setComponentVersionLister(AdhocComponentLister rule) {
		try {
			this.listerRule.set(rule);
		} catch (IllegalStateException e) {
			throw new IllegalStateException("The component lister cannot be changed because repository was already queried.", e);
		}
	}

	private final class ConfiguredModuleComponentRepositoryDelegate implements ConfiguredModuleComponentRepository {
		private final ConfiguredModuleComponentRepository delegate;

		private ConfiguredModuleComponentRepositoryDelegate(ConfiguredModuleComponentRepository delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean isDynamicResolveMode() {
			return delegate.isDynamicResolveMode();
		}

		@Override
		public boolean isLocal() {
			return delegate.isLocal();
		}

		@Override
		public void setComponentResolvers(ComponentResolvers resolver) {
			delegate.setComponentResolvers(resolver);
		}

		@Override
		public Instantiator getComponentMetadataInstantiator() {
			return delegate.getComponentMetadataInstantiator();
		}

		@Override
		public String getId() {
			return delegate.getId();
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public ModuleComponentRepositoryAccess getLocalAccess() {
			return new ModuleComponentRepositoryAccessDelegate(delegate.getLocalAccess());
		}

		@Override
		public ModuleComponentRepositoryAccess getRemoteAccess() {
			return delegate.getRemoteAccess();
		}

		@Override
		public Map<ComponentArtifactIdentifier, ResolvableArtifact> getArtifactCache() {
			return delegate.getArtifactCache();
		}

		@Nullable
		@Override
		public InstantiatingAction<ComponentMetadataSupplierDetails> getComponentMetadataSupplier() {
			return delegate.getComponentMetadataSupplier();
		}
	}

	private final class ModuleComponentRepositoryAccessDelegate implements ModuleComponentRepositoryAccess {
		private final ModuleComponentRepositoryAccess delegate;

		private ModuleComponentRepositoryAccessDelegate(ModuleComponentRepositoryAccess delegate) {
			this.delegate = delegate;
		}

		@Override
		public void listModuleVersions(ModuleDependencyMetadata dependency, BuildableModuleVersionListingResolveResult result) {
			System.out.println(dependency.getSelector().getModuleIdentifier().getClass());
			if (listedModules.add(dependency.getSelector().getModuleIdentifier())) {
				listerRule.get().execute(new AdhocComponentListerDetails() {
					@Override
					public ModuleIdentifier getModuleIdentifier() {
						return dependency.getSelector().getModuleIdentifier();
					}

					@Override
					public void listed(Iterable<String> versions) {
						final Path modulePath = new File(DefaultAdhocArtifactRepository.this.delegate.getUrl()).toPath().resolve(modulePath(getModuleIdentifier()));
						versions.forEach(version -> {
							try {
								createDirectories(modulePath.resolve(version));
							} catch (
								IOException e) {
								throw new UncheckedIOException(e);
							}
						});
					}
				});
			}
			delegate.listModuleVersions(dependency, result);
		}

		@Override
		public void resolveComponentMetaData(ModuleComponentIdentifier id, ComponentOverrideMetadata requestMetaData, BuildableModuleComponentMetaDataResolveResult result) {
			if (resolvedComponents.add(id)) {
				final Path componentPath = new File(DefaultAdhocArtifactRepository.this.delegate.getUrl()).toPath().resolve(componentPath(id));
				supplierRule.get().execute(new AdhocComponentSupplierDetails() {
					@Override
					public ModuleComponentIdentifier getId() {
						return id;
					}

					@Override
					public void metadata(Action<? super GradleModuleMetadata.Builder> action) {
						try (final GradleModuleMetadataWriter writer = newWriter(createDirectories(componentPath).resolve(getId().getModule() + "-" + getId().getVersion() + ".module").toFile())) {
							final GradleModuleMetadata.Builder builder = GradleModuleMetadata.builder();
							builder.formatVersion("1.1"); // default to 1.1, users can still override the value
							action.execute(builder);
							writer.write(builder.build());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}

					@Override
					public void file(String filename, Action<? super OutputStream> action) {
						// TODO: should make sure filename doesn't point to a file outside the componentPath
						try (final OutputStream outStream = Files.newOutputStream(createDirectories(componentPath).resolve(filename))) {
							action.execute(outStream);
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
				});
			}
			delegate.resolveComponentMetaData(id, requestMetaData, result);
		}

		private /*static*/ String componentPath(ModuleComponentIdentifier id) {
			return id.getGroup().replace('.', '/') + "/" + id.getModule() + "/" + id.getVersion();
		}

		private /*static*/ String modulePath(ModuleIdentifier id) {
			return id.getGroup().replace('.', '/') + "/" + id.getName();
		}

		@Override // removed in 7.5
		public void resolveArtifacts(ComponentResolveMetadata component, ConfigurationMetadata variant, BuildableComponentArtifactsResolveResult result) {
			delegate.resolveArtifacts(component, variant, result);
		}

		@Override
		public void resolveArtifactsWithType(ComponentResolveMetadata component, ArtifactType artifactType, BuildableArtifactSetResolveResult result) {
			delegate.resolveArtifactsWithType(component, artifactType, result);
		}

		@Override
		public void resolveArtifact(ComponentArtifactMetadata artifact, ModuleSources moduleSources, BuildableArtifactResolveResult result) {
			delegate.resolveArtifact(artifact, moduleSources, result);
		}

		@Override
		public MetadataFetchingCost estimateMetadataFetchingCost(ModuleComponentIdentifier moduleComponentIdentifier) {
			return delegate.estimateMetadataFetchingCost(moduleComponentIdentifier);
		}
	}
}
