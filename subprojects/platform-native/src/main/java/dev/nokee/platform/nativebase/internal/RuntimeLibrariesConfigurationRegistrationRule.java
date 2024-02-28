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
import dev.nokee.model.internal.ModelObjects;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.linking.HasLinkLibrariesDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.utils.ConfigurationUtils.configureAttributes;

public final class RuntimeLibrariesConfigurationRegistrationRule implements Action<Artifact> {
	private final ModelObjects objs;
	private final ObjectFactory objects;

	public RuntimeLibrariesConfigurationRegistrationRule(ModelObjects objs, ObjectFactory objects) {
		this.objs = objs;
		this.objects = objects;
	}

	@Override
	public void execute(Artifact target) {
		if (target instanceof HasRuntimeLibrariesDependencyBucket) {
			final Configuration runtimeLibraries = ((HasRuntimeLibrariesDependencyBucket) target).getRuntimeLibraries().getAsConfiguration();
			forNativeRuntimeUsage().execute(runtimeLibraries);

			// TODO: Try to replace LinkLibrariesExtendsFromParentDependencyBucketAction when binaries are created by variant
//			ModelElementSupport.safeAsModelElement(target).map(ModelElement::getIdentifier).ifPresent(identifier -> {
//				objs.parentsOf(identifier).filter(it -> {
//					return it.instanceOf(DependencyAwareComponent.class) && it.asModelObject(DependencyAwareComponent.class).get().getDependencies() instanceof NativeComponentDependencies;
//				}).map(it -> (NativeComponentDependencies) it.asModelObject(DependencyAwareComponent.class).get()).findFirst().ifPresent(dependencies -> {
//					((HasLinkLibrariesDependencyBucket) target).getLinkLibraries().extendsFrom(dependencies.getImplementation(), dependencies.getRuntimeOnly());
//				});
//			});
		}
	}

	private Action<Configuration> forNativeRuntimeUsage() {
		return configureAttributes(builder -> builder.usage(objects.named(Usage.class, Usage.NATIVE_RUNTIME)));
	}
}
