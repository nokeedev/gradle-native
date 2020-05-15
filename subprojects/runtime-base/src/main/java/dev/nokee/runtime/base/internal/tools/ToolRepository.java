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
