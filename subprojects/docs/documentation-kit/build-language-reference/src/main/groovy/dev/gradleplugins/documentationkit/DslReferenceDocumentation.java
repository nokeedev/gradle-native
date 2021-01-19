package dev.gradleplugins.documentationkit;

import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucket;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public interface DslReferenceDocumentation extends Component, DependencyAwareComponent<DslReferenceDocumentation.Dependencies> {
	DslMetaData getDslMetaData();

	DslContent getDslContent();

	Property<String> getPermalink();
	DirectoryProperty getClassDocbookDirectory();

	abstract class Dependencies implements ComponentDependencies {
		public Dependencies(ObjectFactory objects) {
			getDslMetaData().getAsConfiguration().getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "dsl-meta-data"));
			getDslMetaDataElements().getAsConfiguration().getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "dsl-meta-data"));
		}

		public abstract ResolvableDependencyBucket getDslMetaData();

		public abstract ConsumableDependencyBucket getDslMetaDataElements();
	}
}
