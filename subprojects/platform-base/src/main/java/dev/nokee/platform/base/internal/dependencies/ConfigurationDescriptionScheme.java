package dev.nokee.platform.base.internal.dependencies;

import com.google.common.annotations.VisibleForTesting;
import dev.nokee.platform.base.internal.ComponentName;
import org.gradle.api.Project;

import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Owner.*;

public final class ConfigurationDescriptionScheme {
	private final ConfigurationDescription.Owner owner;

	private ConfigurationDescriptionScheme(ConfigurationDescription.Owner owner) {
		this.owner = owner;
	}

	public ConfigurationDescription description(ConfigurationDescription.Subject subject, ConfigurationDescription.Bucket bucket) {
		return new ConfigurationDescription(subject, bucket, owner);
	}

	@VisibleForTesting
	static ConfigurationDescriptionScheme forThisProject() {
		return new ConfigurationDescriptionScheme(ofThisProject());
	}

	public static ConfigurationDescriptionScheme forProject(Project project) {
		return new ConfigurationDescriptionScheme(ofProject(project));
	}

	public static ConfigurationDescriptionScheme forComponent(ComponentName componentName) {
		return new ConfigurationDescriptionScheme(ofComponent(componentName));
	}

	public static ConfigurationDescriptionScheme forVariant(ComponentName componentName, String variantName) {
		return new ConfigurationDescriptionScheme(ofVariant(componentName, variantName));
	}
}
