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
package dev.nokee.ide.base.internal;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdeRequestAction {
	public static final IdeRequestAction BUILD = new IdeRequestAction("build");
	public static final IdeRequestAction CLEAN = new IdeRequestAction("clean");

	String identifier;

	public static IdeRequestAction valueOf(String name) {
		return ImmutableSet.of(BUILD, CLEAN)
			.stream()
			.filter(it -> it.getIdentifier().equals(name.toLowerCase()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Unrecognized bridge action '%s'.", name)));
	}
}
