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
package dev.nokee.platform.base.internal;

import com.google.common.collect.Streams;
import dev.nokee.model.HasName;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Namer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BinaryNamer implements Namer<BinaryIdentifier<?>> {
	public static final BinaryNamer INSTANCE = new BinaryNamer();

	@Override
	public String determineName(BinaryIdentifier<?> identifier) {
		return StringUtils.uncapitalize(Streams.stream(identifier).flatMap(it -> {
			if (it instanceof ComponentIdentifier) {
				return Stream.of((ComponentIdentifier) it).filter(t -> !t.isMainComponent()).map(t -> t.getName().get());
			} else if (it instanceof HasName) {
				return Stream.of(((HasName) it).getName().toString());
			} else {
				return Stream.empty();
			}
		}).map(StringUtils::capitalize).collect(Collectors.joining()));
	}
}
