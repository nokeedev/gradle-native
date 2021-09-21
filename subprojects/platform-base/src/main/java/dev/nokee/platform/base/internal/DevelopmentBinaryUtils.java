/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.base.internal;

import com.google.common.collect.Iterables;
import dev.nokee.platform.base.Binary;
import dev.nokee.utils.ProviderUtils;
import org.gradle.api.provider.Provider;

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DevelopmentBinaryUtils {
	public static Provider<? extends Binary> selectSingleBinaryByType(Class<? extends Binary> binaryTypeToSelect, Iterable<? extends Binary> binaries) {
		return StreamSupport.stream(binaries.spliterator(), false)
			.filter(binaryTypeToSelect::isInstance)
			.collect(toAtMostOneElement())
			.map(ProviderUtils::fixed)
			.orElseGet(ProviderUtils::notDefined);
	}

	private static <T> Collector<T, ?, Optional<T>> toAtMostOneElement() {
		return Collectors.collectingAndThen(
			Collectors.toList(),
			list -> Optional.of(list).map(it -> Iterables.getOnlyElement(it, null))
		);
	}
}
