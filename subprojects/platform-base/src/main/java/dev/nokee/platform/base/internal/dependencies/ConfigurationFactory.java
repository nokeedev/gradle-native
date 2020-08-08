package dev.nokee.platform.base.internal.dependencies;

import org.gradle.api.artifacts.Configuration;

public interface ConfigurationFactory {
	Configuration create(String name);
}
