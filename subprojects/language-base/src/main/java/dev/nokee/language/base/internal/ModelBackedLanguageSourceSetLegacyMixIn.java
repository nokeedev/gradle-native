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

import com.google.common.collect.Streams;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SelfAwareLanguageSourceSet;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.util.ConfigureUtil;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public interface ModelBackedLanguageSourceSetLegacyMixIn<SELF extends LanguageSourceSet> extends SelfAwareLanguageSourceSet<SELF> {
	default String getName() {
		return StringUtils.uncapitalize(Streams.stream(ModelNodes.of(this).getComponent(ModelComponentType.componentOf(LanguageSourceSetIdentifier.class))).flatMap(it -> {
			if (it instanceof HasName) {
				return Stream.of(((HasName) it).getName().toString()).filter(name -> !name.equals("main"));
			} else {
				return Stream.empty();
			}
		}).map(StringUtils::capitalize).collect(Collectors.joining()));
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
	default SELF convention(Object... path) {
		ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get().convention(path);
		return (SELF) this;
	}
}
