package dev.nokee.core.exec;

import com.google.common.collect.ImmutableMap;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		return Stream.concat(left.entrySet().stream(), right.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
