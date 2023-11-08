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
package dev.nokee.language.objectivecpp.internal;

import dev.nokee.language.objectivecpp.HasObjectiveCppSources;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;

public interface ObjectiveCppSourcesMixIn extends HasObjectiveCppSources {
	@Override
	default void objectiveCppSources(Action<? super ConfigurableFileCollection> action) {
		action.execute(getObjectiveCppSources());
	}

	@Override
	default void objectiveCppSources(@SuppressWarnings("rawtypes") Closure closure) {
		objectiveCppSources(new ClosureWrappedConfigureAction<>(closure));
	}
}
