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

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.utils.ActionUtils;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;

final class DependencyBucketUtils {
	public static ActionUtils.Action<ModuleDependency> defaultAction(ModelNode entity) {
		assert entity.hasComponent(ModelTags.typeOf(IsDependencyBucket.class));
		return entity.find(DependencyDefaultActionComponent.class).map(DependencyDefaultActionComponent::get)
			.map(ActionUtils.Action::of).orElse(ActionUtils.doNothing());
	}

	public static Action<Dependency> asDependency(Action<? super ModuleDependency> action) {
		return new ExecuteAsModuleDependencyAction(action);
	}

	public static Transformer<Dependency, Dependency> transformUsing(Action<? super Dependency> action) {
		return value -> {
			final Dependency result = value.copy();
			action.execute(result);
			return result;
		};
	}
}
