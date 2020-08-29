package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.ConsumableDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.AttributeContainer;

import javax.inject.Inject;

public final class ConsumableDependencies extends BaseConsumableDependencyBucket implements ConsumableDependencyBucket {
//	ConsumableDependencies(DependencyIdentifier<ConsumableDependencies> identifier, Configuration configuration) {
//		super(identifier, configuration);
//	}
	@Inject
	ConsumableDependencies() {}

	public void artifact(Object notation) {
		configuration.getOutgoing().artifact(notation);
	}

	public void directory(Object notation) {
		configuration.getOutgoing().artifact(notation, it -> it.setType(ArtifactTypeDefinition.DIRECTORY_TYPE));
	}

	public ConfigurationPublications getOutgoing() {
		return configuration.getOutgoing();
	}

	public void outgoing(Action<? super ConfigurationPublications> action) {
		configuration.outgoing(action);
	}

	public AttributeContainer getAttributes() {
		return configuration.getAttributes();
	}

	public void attributes(Action<? super AttributeContainer> action) {
		configuration.attributes(action);
	}
}
