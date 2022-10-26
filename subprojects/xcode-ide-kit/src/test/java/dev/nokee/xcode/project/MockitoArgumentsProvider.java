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
package dev.nokee.xcode.project;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.platform.commons.util.AnnotationUtils;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.stream.Stream;

public final class MockitoArgumentsProvider implements ArgumentsProvider {
	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Stream.of(AnnotationUtils.findAnnotation(context.getElement(), MockitoSource.class).map(it -> {
			if (it.value().equals(Null.class)) {
				return Arrays.stream(it.listOf()).map(Mockito::mock).collect(ImmutableList.toImmutableList());
			} else {
				return Mockito.mock(it.value());
			}
		}).map(Arguments::arguments).orElseThrow(RuntimeException::new));
	}
}
