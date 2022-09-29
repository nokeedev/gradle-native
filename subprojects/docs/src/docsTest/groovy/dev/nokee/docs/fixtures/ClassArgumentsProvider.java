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
package dev.nokee.docs.fixtures;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ClassArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<ClassSource> {
		private Class<? extends Supplier<?>> supplierClass;

		@Override
		public void accept(ClassSource annotation) {
			this.supplierClass = annotation.value();
		}

		@Override
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			Object testInstance = context.getTestInstance().orElse(null);
			// @formatter:off
			Supplier<?> argumentSupplier = ReflectionUtils.newInstance(supplierClass);
			return CollectionUtils.toStream(argumentSupplier.get()).map(ClassArgumentsProvider::toArguments);
			// @formatter:on
		}

		// From MethodArgumentsProvider
		private static Arguments toArguments(Object item) {

			// Nothing to do except cast.
			if (item instanceof Arguments) {
				return (Arguments) item;
			}

			// Pass all multidimensional arrays "as is", in contrast to Object[].
			// See https://github.com/junit-team/junit5/issues/1665
			if (ReflectionUtils.isMultidimensionalArray(item)) {
				return arguments(item);
			}

			// Special treatment for one-dimensional reference arrays.
			// See https://github.com/junit-team/junit5/issues/1665
			if (item instanceof Object[]) {
				return arguments((Object[]) item);
			}

			// Pass everything else "as is".
			return arguments(item);
		}
}
