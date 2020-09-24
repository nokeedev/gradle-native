package dev.nokee.platform.base.internal.dependencies;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;

public interface ConfigurationBucketRegistry {
	Configuration createIfAbsent(String name, ConfigurationBucketType type);
	Configuration createIfAbsent(String name, ConfigurationBucketType type, Action<? super Configuration> action);
}
