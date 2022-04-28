/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.jni.internal;

import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.provider.Provider;

// TODO: We could set the classes directory as secondary variant.
// TODO: We could maybe set the shared library directory as secondary variant.
//  However, the shared library would requires the resource path to be taken into consideration...
public final class RuntimeElementsConfiguration implements ModelComponent {
	private final ModelNode entity;

	public RuntimeElementsConfiguration(ModelNode entity) {
		this.entity = entity;
	}

	public void add(Object notation) {
		ModelNodeUtils.get(entity, ModelType.of(new TypeOf<NamedDomainObjectProvider<Configuration>>() {})).configure(configuration -> configuration.getOutgoing().artifact(notation));
	}

	public void addAll(Provider<? extends Iterable<PublishArtifact>> values) {
		ModelNodeUtils.get(entity, ModelType.of(new TypeOf<NamedDomainObjectProvider<Configuration>>() {})).configure(configuration -> configuration.getOutgoing().getArtifacts().addAllLater(values));
	}

	public ModelNode get() {
		return entity;
	}
}
