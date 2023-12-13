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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMap;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.language.nativebase.internal.ConfigurationUtilsEx;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import java.util.function.Function;

public final class AttachAttributesToConfigurationRule implements Action<Variant> {
	private final Class<?> type;
	private final Function<Object, ResolvableDependencyBucketSpec> mapper;
	private final ModelMap<Artifact> artifacts;
	private final ObjectFactory objects;

	@SuppressWarnings("unchecked")
	public <T> AttachAttributesToConfigurationRule(Class<T> type, Function<T, ResolvableDependencyBucketSpec> mapper, ObjectFactory objects, ModelMap<Artifact> artifacts) {
		this.type = type;
		this.mapper = (Function<Object, ResolvableDependencyBucketSpec>) mapper;
		this.objects = objects;
		this.artifacts = artifacts;
	}

	@Override
	public void execute(Variant variant) {
		ModelElementSupport.safeAsModelElement(variant).map(ModelElement::getIdentifier).ifPresent(variantIdentifier -> {
			artifacts.configureEach(type, artifact -> {
				ModelElementSupport.safeAsModelElement(artifact).map(ModelElement::getIdentifier).ifPresent(artifactIdentifier -> {
					if (ModelObjectIdentifiers.descendantOf(artifactIdentifier, variantIdentifier)) {
						final ResolvableDependencyBucketSpec bucket = mapper.apply(artifact);
						ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) variant.getBuildVariant(), objects).execute(bucket.getAsConfiguration());
						ConfigurationUtilsEx.configureAsGradleDebugCompatible(bucket.getAsConfiguration());
					}
				});
			});
		});
	}
}
