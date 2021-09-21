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
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

final class CommandLineToolInvocationEnvironmentVariablesUtils {
	private CommandLineToolInvocationEnvironmentVariablesUtils() {}

	static List<String> asList(Map<String, ?> environmentVariables) {
		return environmentVariables
			.entrySet()
			.stream()
			.map(CommandLineToolInvocationEnvironmentVariablesUtils::toListEntry)
			.collect(Collectors.toList());
	}

	private static String toListEntry(Map.Entry<String, ?> entry) {
		return entry.getKey() + "=" + entry.getValue().toString();
	}

	static Map<String, String> asMap(List<?> environmentVariables) {
		return environmentVariables.stream().map(CommandLineToolInvocationEnvironmentVariablesUtils::toEntry).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private static Map.Entry<String, String> toEntry(Object o) {
		String[] tokens = StringUtils.split(o.toString(), "=", 2);
		if (tokens.length == 1) {
			return new HashMap.SimpleEntry<>(tokens[0], "");
		}
		return new HashMap.SimpleEntry<>(tokens[0], tokens[1]);
	}

	static Map<String, String> toStringOnEachEntry(Map<?, ?> environmentVariables) {
		return environmentVariables.entrySet().stream().map(CommandLineToolInvocationEnvironmentVariablesUtils::toStringEntry).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private static Map.Entry<String, String> toStringEntry(Map.Entry<?, ?> entry) {
		return new AbstractMap.SimpleImmutableEntry<>(entry.getKey().toString(), entry.getValue().toString());
	}

	static Map<String, ?> merge(Map<String, ?> left, Map<String, ?> right) {
		val result = new HashMap<String, Object>();
		result.putAll(left);
		result.putAll(right);
		return result;
	}

	static Map<String, ?> load(File file) {
		val result = new Properties();
		try (val inStream = new FileInputStream(file)) {
			result.load(inStream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return asMap(result);
	}

	static Map<String, String> asMap(Properties properties) {
		return toStringOnEachEntry(properties);
	}
}
