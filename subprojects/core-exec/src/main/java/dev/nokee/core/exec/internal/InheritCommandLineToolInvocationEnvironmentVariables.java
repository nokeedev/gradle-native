package dev.nokee.core.exec.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class InheritCommandLineToolInvocationEnvironmentVariables implements CommandLineToolInvocationEnvironmentVariables, Serializable {
	@Override
	public Map<String, String> getAsMap() {
		return System.getenv();
	}

	@Override
	public List<String> getAsList() {
		return System.getenv().entrySet().stream().map(e -> String.format("%s=%s", e.getKey(), e.getValue())).collect(ImmutableList.toImmutableList());
	}
}
