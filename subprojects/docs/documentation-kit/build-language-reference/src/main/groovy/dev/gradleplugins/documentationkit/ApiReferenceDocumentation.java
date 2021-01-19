package dev.gradleplugins.documentationkit;

import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucket;

public interface ApiReferenceDocumentation extends Component, DependencyAwareComponent<ApiReferenceDocumentation.Dependencies> {
	ApiReferenceManifest getManifest();

	interface Dependencies extends ComponentDependencies {
		DeclarableDependencyBucket getApi();

		ConsumableDependencyBucket getManifestElements();

		ResolvableDependencyBucket getManifest();
	}
}
