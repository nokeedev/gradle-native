package dev.nokee.model.internal;

import dev.nokee.model.registry.ModelRegistry;
import dev.nokee.utils.NamedDomainObjectCollectionUtils;
import lombok.val;
import org.gradle.api.Action;

final class RegisterModelProjection<T> implements Action<NamedDomainObjectCollectionUtils.KnownElement<T>> {
	private final ModelRegistry modelRegistry;

	RegisterModelProjection(ModelRegistry modelRegistry) {
		this.modelRegistry = modelRegistry;
	}

	@Override
	public void execute(NamedDomainObjectCollectionUtils.KnownElement<T> element) {
		val node = modelRegistry.getRoot().find(element.getName()).orElseGet(() -> modelRegistry.getRoot().newChildNode(element.getName()));
		node.newProjection(builder -> builder.type(element.getType()).forProvider(element.getAsProvider()));
	}
}
