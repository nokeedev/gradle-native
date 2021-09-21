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
package dev.nokee.runtime.base.internal.tools;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.Set;
import java.util.stream.Collectors;

public class ToolRepository {
	private final Multimap<String, CommandLineToolLocator> toolLocators = MultimapBuilder.hashKeys().arrayListValues().build();

	public void register(String toolName, CommandLineToolLocator locator) {
		toolLocators.put(toolName, locator);
	}

	public Set<CommandLineToolDescriptor> findAll(String toolName) {
		return toolLocators.get(toolName).stream().flatMap(it -> it.findAll(toolName).stream()).collect(Collectors.toSet());
	}

	public Set<String> getKnownTools() {
		return toolLocators.keySet();
	}
}
