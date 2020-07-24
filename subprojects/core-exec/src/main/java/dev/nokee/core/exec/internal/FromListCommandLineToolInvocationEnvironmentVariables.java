package dev.nokee.core.exec.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
public class FromListCommandLineToolInvocationEnvironmentVariables implements CommandLineToolInvocationEnvironmentVariables, Serializable {
	List<String> environmentVariables;

	public FromListCommandLineToolInvocationEnvironmentVariables(List<String> environmentVariables) {
		this.environmentVariables = ImmutableList.copyOf(environmentVariables);
	}

	@Override
	public Map<String, String> getAsMap() {
		return environmentVariables.stream().map(this::toEntry).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map.Entry<String, String> toEntry(Object o) {
		String[] tokens = StringUtils.split(o.toString(), "=", 2);
		if (tokens.length == 1) {
			return new HashMap.SimpleEntry<>(tokens[0], "");
		}
		return new HashMap.SimpleEntry<>(tokens[0], tokens[1]);
	}

	@Override
	public List<String> getAsList() {
		return environmentVariables;
	}
}
