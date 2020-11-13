package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

enum CommandLineToolInvocationEnvironmentVariablesEmptyImpl implements CommandLineToolInvocationEnvironmentVariables {
	EMPTY_ENVIRONMENT_VARIABLES;

	@Override
	public Map<String, String> getAsMap() {
		return ImmutableMap.of();
	}

	@Override
	public List<String> getAsList() {
		return ImmutableList.of();
	}

	@Override
	public CommandLineToolInvocationEnvironmentVariables plus(CommandLineToolInvocationEnvironmentVariables environmentVariables) {
		return requireNonNull(environmentVariables);
	}
}
