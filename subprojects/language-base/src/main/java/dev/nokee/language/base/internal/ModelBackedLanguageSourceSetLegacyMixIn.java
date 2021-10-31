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
package dev.nokee.language.base.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SelfAwareLanguageSourceSet;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.utils.TaskDependencyUtils;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.util.ConfigureUtil;

@SuppressWarnings("unchecked")
public interface ModelBackedLanguageSourceSetLegacyMixIn<SELF extends LanguageSourceSet> extends SelfAwareLanguageSourceSet<SELF>, HasPublicType {
	default String getName() {
		return ModelNodeUtils.getPath(ModelNodes.of(this)).getName();
	}

	default SELF from(Object... paths) {
		ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get().from(paths);
		return (SELF) this;
	}

	default FileCollection getSourceDirectories() {
		return ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get().getSourceDirectories();
	}

	default SELF filter(Action<? super PatternFilterable> action) {
		action.execute(getFilter());
		return (SELF) this;
	}

	default SELF filter(@DelegatesTo(value = PatternFilterable.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		return filter(ConfigureUtil.configureUsing(closure));
	}

	default PatternFilterable getFilter() {
		return ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get().getFilter();
	}

	default FileTree getAsFileTree() {
		return ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get().getAsFileTree();
	}

	@Override
	default TaskDependency getBuildDependencies() {
		val builder = ImmutableList.<TaskDependency>builder();
		builder.add(ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get().getBuildDependencies());
		ModelProperties.findProperty(this, "compileTask")
			.ifPresent(it -> builder.add(TaskDependencyUtils.of(it.as(TaskProvider.class).get())));
		ModelProperties.findProperty(this, "headers")
			.ifPresent(it -> builder.add(it.as(ConfigurableSourceSet.class).get().getBuildDependencies()));
		return TaskDependencyUtils.composite(builder.build().toArray(new TaskDependency[0]));
	}

	@Override
	default SELF convention(Object... path) {
		ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get().convention(path);
		return (SELF) this;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	default TypeOf<?> getPublicType() {
		return TypeOf.typeOf(new TypeToken<SELF>(getClass()) {}.getType());
	}
}
