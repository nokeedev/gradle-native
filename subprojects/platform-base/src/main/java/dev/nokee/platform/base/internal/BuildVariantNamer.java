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
package dev.nokee.platform.base.internal;

import dev.nokee.runtime.core.Coordinate;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;
import org.gradle.api.Namer;

import java.util.stream.Collectors;

public class BuildVariantNamer implements Namer<BuildVariantInternal> {
	public static final BuildVariantNamer INSTANCE = new BuildVariantNamer();

	@Override
	public String determineName(BuildVariantInternal buildVariant) {
		return StringUtils.uncapitalize(buildVariant.getDimensions().stream().map(this::determineName).map(StringUtils::capitalize).collect(Collectors.joining()));
	}

	private String determineName(Coordinate<?> dimension) {
		if (dimension instanceof Named) {
			return ((Named) dimension).getName();
		}
		throw new IllegalArgumentException("Can't determine name");
	}
}
