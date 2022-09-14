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

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.val;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.asList;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.asMap;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.load;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.merge;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.toStringOnEachEntry;
import static dev.nokee.core.exec.UnpackStrategies.FLAT_UNPACK_TO_STRING;

/**
 * Represents the environment variable of a command line tool invocation.
 *
 * @since 0.5
 */
@EqualsAndHashCode
public final class CommandLineToolInvocationEnvironmentVariables implements Serializable {
	private static final CommandLineToolInvocationEnvironmentVariables EMPTY_ENVIRONMENT_VARIABLES = new CommandLineToolInvocationEnvironmentVariables();
	private final Map<String, String> environmentVariables;

	public CommandLineToolInvocationEnvironmentVariables() {
		this(ImmutableMap.of());
	}

	public CommandLineToolInvocationEnvironmentVariables(Map<String, String> environmentVariables) {
		this.environmentVariables = ImmutableMap.copyOf(environmentVariables);
	}

	/**
	 * Returns the environment variables as a (key, value)-pair map.
	 *
	 * @return a map representing the environment variables, never null.
	 */
	public Map<String, String> getAsMap() {
		return environmentVariables;
	}

	/**
	 * Returns the environment variables as a (key=value)-pair list.
	 *
	 * @return a list representing the environment variables, never null.
	 */
	public List<String> getAsList() {
		return asList(environmentVariables);
	}

	/**
	 * Returns the environment variable value for the specified key.
	 *
	 * @param key  the environment variable name, must not be null
	 * @return the value of the environment variable if present, never null
	 */
	public Optional<String> get(String key) {
		return Optional.ofNullable(environmentVariables.get(key));
	}

	// TODO: allow more manipulation of the environment variables such as getting, putting (creates another instance with the result), etc.

	/**
	 * Merges the current environment variables with the specified environment variables.
	 *
	 * @param environmentVariables the environment variables to merge with this instance.
	 * @return a new instance representing the merged environment variables, never null.
	 */
	public CommandLineToolInvocationEnvironmentVariables plus(CommandLineToolInvocationEnvironmentVariables environmentVariables) {
		return from(merge(this.environmentVariables, environmentVariables.getAsMap()));
	}

	@Override
	public String toString() {
		return String.format("environment variables:\n%s", environmentVariables.entrySet().stream().map(it -> "  " + it.getKey() + " => " + it.getValue()).collect(Collectors.joining("\n")));
	}

	/**
	 * Creates the invocation environment variables from the specified list.
	 *
	 * @param environmentVariables the environment variables to use
	 * @return a instance representing the environment variables to use, never null.
	 */
	public static CommandLineToolInvocationEnvironmentVariables from(@Nullable List<?> environmentVariables) {
		if (environmentVariables == null) {
			return inherit();
		} else if (environmentVariables.isEmpty()) {
			return EMPTY_ENVIRONMENT_VARIABLES;
		} else {
			return new CommandLineToolInvocationEnvironmentVariables(asMap(FLAT_UNPACK_TO_STRING.unpack(environmentVariables)));
		}
	}

	/**
	 * Creates the invocation environment variables from the specified map.
	 *
	 * @param environmentVariables the environment variables to use
	 * @return a instance representing the environment variables to use, never null.
	 */
	public static CommandLineToolInvocationEnvironmentVariables from(Map<String, ?> environmentVariables) {
		Objects.requireNonNull(environmentVariables, "'environmentVariables' must not be null");
		if (environmentVariables.isEmpty()) {
			return EMPTY_ENVIRONMENT_VARIABLES;
		} else {
			val builder = new CommandLineToolInvocationEnvironmentVariables.Builder();
			environmentVariables.forEach(builder::env);
			return builder.build();
		}
	}

	/**
	 * Creates the invocation environment variables from the current process.
	 *
	 * @return a instance representing the environment variables to use, never null.
	 */
	public static CommandLineToolInvocationEnvironmentVariables inherit() {
		return new CommandLineToolInvocationEnvironmentVariables(System.getenv());
	}

	/**
	 * Creates the empty invocation environment variables.
	 *
	 * @return a instance representing the environment variables to use, never null.
	 */
	public static CommandLineToolInvocationEnvironmentVariables empty() {
		return EMPTY_ENVIRONMENT_VARIABLES;
	}

	/**
	 * Creates the invocation environment variables from a .properties file.
	 *
	 * @param propertiesFile a file from which to load the environment variables
	 * @return a instance representing the environment variables to use, never null.
	 */
	public static CommandLineToolInvocationEnvironmentVariables from(File propertiesFile) {
		return from(load(propertiesFile));
	}

	/**
	 * Creates the invocation environment variables from a {@link Properties} instance.
	 *
	 * @param properties a {@link Properties} instance of environment variables to use
	 * @return a instance representing the environment variables to use, never null.
	 */
	public static CommandLineToolInvocationEnvironmentVariables from(Properties properties) {
		Objects.requireNonNull(properties, "'properties' must not be null");
		return from(toStringOnEachEntry(properties));
	}

	public static final class Builder {
		private final Map<String, String> envVars = new HashMap<>();
		private final UnpackStrategy unpackStrategy;

		public Builder() {
			this(UnpackStrategies.UNPACK_TO_STRING);
		}

		public Builder(UnpackStrategy unpackStrategy) {
			this.unpackStrategy = unpackStrategy;
		}

		public Builder env(String key, Object value) {
			Objects.requireNonNull(key, "'key' must not be null");
			Objects.requireNonNull(value, "'value' must not be null");
			envVars.put(key, unpackStrategy.unpack(value));
			return this;
		}

		public CommandLineToolInvocationEnvironmentVariables build() {
			return new CommandLineToolInvocationEnvironmentVariables(ImmutableMap.copyOf(envVars));
		}
	}
}
