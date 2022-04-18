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
package dev.nokee.language.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.internal.ComponentName;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface LanguageSourceSetConventionSupplier {
	List<String> get(String sourceSetName);

	static LanguageSourceSetConventionSupplier defaultObjectiveCGradle(ComponentName componentName) {
		return name -> {
			assert name.equals("objectiveC") : "apply this convention only to source set named 'objectiveC'.";
			return ImmutableList.of("src/" + componentName + "/objc");
		};
	}

	static LanguageSourceSetConventionSupplier defaultObjectiveCppGradle(ComponentName componentName) {
		return name -> {
			assert name.equals("objectiveCpp") : "apply this convention only to source set named 'objectiveCpp'.";
			return ImmutableList.of("src/" + componentName + "/objcpp");
		};
	}

	static LanguageSourceSetConventionSupplier maven(ComponentName componentName) {
		return name -> ImmutableList.of("src/" + componentName + "/" + name);
	}

	static <T extends LanguageSourceSet> Consumer<T> withConventionOf(LanguageSourceSetConventionSupplier... suppliers) {
		return sourceSet -> {
			sourceSet.convention((Callable<List<String>>) () -> Arrays.stream(suppliers).flatMap(it -> it.get(((LanguageSourceSetIdentifier) ModelNodes.of(sourceSet).get(IdentifierComponent.class).get()).getName().get()).stream()).collect(Collectors.toList()));
		};
	}
}
