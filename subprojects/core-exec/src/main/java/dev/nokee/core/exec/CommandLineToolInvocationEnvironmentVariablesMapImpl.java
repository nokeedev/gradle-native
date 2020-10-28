package dev.nokee.core.exec;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariablesUtils.*;

@EqualsAndHashCode
final class CommandLineToolInvocationEnvironmentVariablesMapImpl implements CommandLineToolInvocationEnvironmentVariables, Serializable {
	private final Map<String, ?> environmentVariables;

	CommandLineToolInvocationEnvironmentVariablesMapImpl(Map<String, ?> environmentVariables) {
		this.environmentVariables = ImmutableMap.copyOf(environmentVariables);
	}

	@Override
	public Map<String, String> getAsMap() {
		return toStringOnEachEntry(environmentVariables);
	}

	@Override
	public List<String> getAsList() {
		return asList(environmentVariables);
	}

	@Override
	public CommandLineToolInvocationEnvironmentVariables plus(CommandLineToolInvocationEnvironmentVariables environmentVariables) {
		return from(merge(this.environmentVariables, environmentVariables.getAsMap()));
	}
}
