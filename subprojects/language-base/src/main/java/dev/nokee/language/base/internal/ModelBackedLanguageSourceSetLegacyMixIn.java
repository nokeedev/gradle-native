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

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SelfAwareLanguageSourceSet;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.utils.ConfigureUtils;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.util.PatternFilterable;

@SuppressWarnings("unchecked")
public interface ModelBackedLanguageSourceSetLegacyMixIn<SELF extends LanguageSourceSet> extends SelfAwareLanguageSourceSet<SELF> {
	default String getName() {
		return ModelNodes.of(this).get(FullyQualifiedNameComponent.class).get().toString();
	}

	default SELF from(Object... paths) {
		throw new UnsupportedOperationException();
	}

	@Override
	default void setFrom(Object... paths) {
		throw new UnsupportedOperationException();
	}

	default FileCollection getSourceDirectories() {
		throw new UnsupportedOperationException();
	}

	default SELF filter(Action<? super PatternFilterable> action) {
		action.execute(getFilter());
		return (SELF) this;
	}

	default SELF filter(@DelegatesTo(value = PatternFilterable.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		return filter(ConfigureUtils.configureUsing(closure));
	}

	default PatternFilterable getFilter() {
		throw new UnsupportedOperationException();
	}

	default FileTree getAsFileTree() {
		throw new UnsupportedOperationException();
	}

	@Override
	default SELF convention(Object... path) {
		throw new UnsupportedOperationException();
	}
}
