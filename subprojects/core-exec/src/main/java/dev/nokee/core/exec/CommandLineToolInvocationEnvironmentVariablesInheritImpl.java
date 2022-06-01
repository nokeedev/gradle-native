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
package dev.nokee.core.exec;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.asList;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.merge;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.toStringOnEachEntry;

@EqualsAndHashCode
final class CommandLineToolInvocationEnvironmentVariablesInheritImpl implements CommandLineToolInvocationEnvironmentVariables, Serializable {
	@Override
	public Map<String, String> getAsMap() {
		return toStringOnEachEntry(System.getenv());
	}

	@Override
	public List<String> getAsList() {
		return asList(System.getenv());
	}

	@Override
	public CommandLineToolInvocationEnvironmentVariables plus(CommandLineToolInvocationEnvironmentVariables environmentVariables) {
		return from(merge(System.getenv(), environmentVariables.getAsMap()));
	}
}
