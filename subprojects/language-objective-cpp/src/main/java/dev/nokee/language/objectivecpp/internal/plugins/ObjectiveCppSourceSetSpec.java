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
package dev.nokee.language.objectivecpp.internal.plugins;

import dev.nokee.language.base.internal.HasConfigurableSourceMixIn;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.nativebase.internal.DefaultCompilableNativeComponentDependencies;
import dev.nokee.language.nativebase.internal.HasConfigurableHeadersMixIn;
import dev.nokee.language.nativebase.internal.HasHeaderSearchPaths;
import dev.nokee.language.nativebase.internal.HasNativeCompileTaskMixIn;
import dev.nokee.language.nativebase.internal.NativeHeaderSetTag;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetTag;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.tasks.TaskDependency;

import javax.inject.Inject;

@DomainObjectEntities.Tag({ObjectiveCppSourceSetTag.class, ObjectiveCppSourceSetSpec.Tag.class, ConfigurableTag.class, IsLanguageSourceSet.class, NativeHeaderSetTag.class})
public /*final*/ abstract class ObjectiveCppSourceSetSpec extends ModelElementSupport implements ObjectiveCppSourceSet
	, ModelBackedLanguageSourceSetLegacyMixIn<ObjectiveCppSourceSet>
	, HasConfigurableSourceMixIn
	, HasConfigurableHeadersMixIn
	, HasNativeCompileTaskMixIn<ObjectiveCppCompileTask>
	, DependencyAwareComponentMixIn<DefaultCompilableNativeComponentDependencies>
	, HasHeaderSearchPaths
{
	@Inject
	public ObjectiveCppSourceSetSpec(ModelObjectRegistry<DependencyBucket> bucketRegistry) {
		getExtensions().create("dependencies", DefaultCompilableNativeComponentDependencies.class, getIdentifier(), bucketRegistry);
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.composite(getSource().getBuildDependencies(), getHeaders().getBuildDependencies(), TaskDependencyUtils.of(getCompileTask()));
	}

	@Override
	public ResolvableDependencyBucketSpec getHeaderSearchPaths() {
		return getDependencies().getHeaderSearchPaths();
	}

	@Override
	public String toString() {
		return "Objective-C++ sources '" + getName() + "'";
	}

	public interface Tag extends ModelTag {}
}
