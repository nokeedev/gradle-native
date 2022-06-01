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

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesEmptyImpl.EMPTY_ENVIRONMENT_VARIABLES;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.asMap;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.load;

/**
 * Represents the environment variable of a command line tool invocation.
 *
 * @since 0.5
 */
public interface CommandLineToolInvocationEnvironmentVariables {
	/**
	 * Returns the environment variables as a (key, value)-pair map.
	 *
	 * @return a map representing the environment variables, never null.
	 */
	Map<String, String> getAsMap();

	/**
	 * Returns the environment variables as a (key=value)-pair list.
	 *
	 * @return a list representing the environment variables, never null.
	 */
	List<String> getAsList();

	// TODO: allow more manipulation of the environment variables such as getting, putting (creates another instance with the result), etc.

	/**
	 * Merges the current environment variables with the specified environment variables.
	 *
	 * @param environmentVariables the environment variables to merge with this instance.
	 * @return a new instance representing the merged environment variables, never null.
	 */
	CommandLineToolInvocationEnvironmentVariables plus(CommandLineToolInvocationEnvironmentVariables environmentVariables);

	/**
	 * Creates the invocation environment variables from the specified list.
	 *
	 * @param environmentVariables the environment variables to use
	 * @return a instance representing the environment variables to use, never null.
	 */
	static CommandLineToolInvocationEnvironmentVariables from(@Nullable List<?> environmentVariables) {
		if (environmentVariables == null) {
			return inherit();
		} else if (environmentVariables.isEmpty()) {
			return EMPTY_ENVIRONMENT_VARIABLES;
		}
		return new CommandLineToolInvocationEnvironmentVariablesMapImpl(asMap(environmentVariables));
	}

	/**
	 * Creates the invocation environment variables from the specified map.
	 *
	 * @param environmentVariables the environment variables to use
	 * @return a instance representing the environment variables to use, never null.
	 */
	static CommandLineToolInvocationEnvironmentVariables from(Map<String, ?> environmentVariables) {
		if (environmentVariables.isEmpty()) {
			return EMPTY_ENVIRONMENT_VARIABLES;
		}
		return new CommandLineToolInvocationEnvironmentVariablesMapImpl(environmentVariables);
	}

	/**
	 * Creates the invocation environment variables from the current process.
	 *
	 * @return a instance representing the environment variables to use, never null.
	 */
	static CommandLineToolInvocationEnvironmentVariables inherit() {
		return new CommandLineToolInvocationEnvironmentVariablesInheritImpl();
	}

	/**
	 * Creates the empty invocation environment variables.
	 *
	 * @return a instance representing the environment variables to use, never null.
	 */
	static CommandLineToolInvocationEnvironmentVariables empty() {
		return EMPTY_ENVIRONMENT_VARIABLES;
	}

	/**
	 * Creates the invocation environment variables from a .properties file.
	 *
	 * @param propertiesFile a file from which to load the environment variables
	 * @return a instance representing the environment variables to use, never null.
	 */
	static CommandLineToolInvocationEnvironmentVariables from(File propertiesFile) {
		return from(load(propertiesFile));
	}

	/**
	 * Creates the invocation environment variables from a {@link Properties} instance.
	 *
	 * @param properties a {@link Properties} instance of environment variables to use
	 * @return a instance representing the environment variables to use, never null.
	 */
	static CommandLineToolInvocationEnvironmentVariables from(Properties properties) {
		return from(asMap(properties));
	}
}
