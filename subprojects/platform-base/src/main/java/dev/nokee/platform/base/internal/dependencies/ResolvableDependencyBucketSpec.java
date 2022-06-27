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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.DomainObjectEntities;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.file.FileCollection;

import javax.inject.Inject;
import java.util.function.Supplier;

import static dev.nokee.model.internal.buffers.ModelBuffers.typeOf;

@DomainObjectEntities.Tag({ResolvableDependencyBucketSpec.Tag.class, ConfigurableTag.class, IsDependencyBucket.class})
public /*final*/ class ResolvableDependencyBucketSpec implements ResolvableDependencyBucket, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();
	private final IncomingArtifacts incoming = new IncomingArtifacts(this::getAsConfiguration);

	@Inject
	public ResolvableDependencyBucketSpec() {}

	@Override
	public String getName() {
		return node.get(FullyQualifiedNameComponent.class).get().toString();
	}

	@Override
	public void addDependency(Object notation) {
		node.setComponent(node.getComponent(typeOf(DependencyElement.class)).appended(new DependencyElement(notation)));
	}

	@Override
	public void addDependency(Object notation, Action<? super ModuleDependency> action) {
		node.setComponent(node.getComponent(typeOf(DependencyElement.class)).appended(new DependencyElement(notation, action)));
	}

	@Override
	public Configuration getAsConfiguration() {
		return ModelNodeUtils.get(node, Configuration.class);
	}

	@Override
	public FileCollection getAsLenientFileCollection() {
		return incoming.getAsLenient();
	}

	@Override
	public FileCollection getAsFileCollection() {
		return incoming.get();
	}

	@Override
	public ModelNode getNode() {
		return node;
	}

	public interface Tag extends ModelTag {}

	static final class IncomingArtifacts {
		private final Supplier<Configuration> delegate;

		public IncomingArtifacts(Supplier<Configuration> delegate) {
			this.delegate = delegate;
		}

		public FileCollection get() {
			return delegate.get().getIncoming().getFiles();
		}

		public FileCollection getAsLenient() {
			return delegate.get().getIncoming().artifactView(this::asLenient).getFiles();
		}

		private void asLenient(ArtifactView.ViewConfiguration view) {
			view.setLenient(true);
		}
	}
}
