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
import dev.nokee.gradle.ArtifactRepositoryGeneratorListener;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ComponentMetadataSupplierDetails;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.RepositoryContentDescriptor;
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
import java.util.Map;

public final class DefaultAdhocArtifactRepository implements AdhocArtifactRepository, ResolutionAwareRepository, ContentFilteringRepository {
	private final ArtifactRepositoryGeneratorListener listener;
	private final MavenArtifactRepository delegate;

	public DefaultAdhocArtifactRepository(MavenArtifactRepository delegate, ArtifactRepositoryGeneratorListener listener) {
		this.delegate = delegate;
		this.listener = listener;
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
			listener.onListModuleVersions(dependency.getSelector());
			delegate.listModuleVersions(dependency, result);
		}

		@Override
		public void resolveComponentMetaData(ModuleComponentIdentifier moduleComponentIdentifier, ComponentOverrideMetadata requestMetaData, BuildableModuleComponentMetaDataResolveResult result) {
			listener.onResolveComponentMetaData(moduleComponentIdentifier);
			delegate.resolveComponentMetaData(moduleComponentIdentifier, requestMetaData, result);
		}

		@Override // removed in 7.5
		public void resolveArtifacts(ComponentResolveMetadata component, ConfigurationMetadata variant, BuildableComponentArtifactsResolveResult result) {
			System.out.println("RESOL ARTIFACTS " + component + " -- " + variant);
			delegate.resolveArtifacts(component, variant, result);
		}

		@Override
		public void resolveArtifactsWithType(ComponentResolveMetadata component, ArtifactType artifactType, BuildableArtifactSetResolveResult result) {
			System.out.println("RESOL ARTIFACTS withtype " + component + " -- " + artifactType);
			delegate.resolveArtifactsWithType(component, artifactType, result);
		}

		@Override
		public void resolveArtifact(ComponentArtifactMetadata artifact, ModuleSources moduleSources, BuildableArtifactResolveResult result) {
			System.out.println("RESOL ARTIFACT " + artifact + " -- " + moduleSources);
			delegate.resolveArtifact(artifact, moduleSources, result);
		}

		@Override
		public MetadataFetchingCost estimateMetadataFetchingCost(ModuleComponentIdentifier moduleComponentIdentifier) {
			return delegate.estimateMetadataFetchingCost(moduleComponentIdentifier);
		}
	}
}
