package dev.nokee.runtime.base.internal.tools;

import java.util.Set;

public interface CommandLineToolLocator {
	Set<CommandLineToolDescriptor> findAll(String toolName);

	Set<String> getKnownTools();
}
