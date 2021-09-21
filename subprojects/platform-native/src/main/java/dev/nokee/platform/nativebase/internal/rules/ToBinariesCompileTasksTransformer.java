/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.rules;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.nativebase.NativeBinary;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.Set;

import static dev.nokee.utils.TransformerUtils.toSetTransformer;

enum ToBinariesCompileTasksTransformer implements Transformer<Provider<Set<? extends SourceCompile>>, Variant> {
	TO_DEVELOPMENT_BINARY_COMPILE_TASKS;

	@Override
	public Provider<Set<? extends SourceCompile>> transform(Variant variant) {
		return variant.getBinaries().flatMap(FlatMapBinaryToCompileTasks.INSTANCE).map(toSetTransformer());
	}

	enum FlatMapBinaryToCompileTasks implements Transformer<Iterable<SourceCompile>, Binary> {
		INSTANCE;

		@Override
		public Iterable<SourceCompile> transform(Binary binary) {
			if (binary instanceof NativeBinary) {
				return ((NativeBinary) binary).getCompileTasks().get();
			}
			return ImmutableSet.of();
		}
	}
}
