package dev.nokee.model.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.DisplayName;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import lombok.val;
import org.gradle.util.GUtil;

import java.util.Optional;

final class DefaultDomainObjectIdentifier implements DomainObjectIdentifier {
	private final ModelProjection projection;

	DefaultDomainObjectIdentifier(ModelProjection projection) {
		this.projection = projection;
	}

	public String getPath() {
		val result = ImmutableList.<String>builder();
		ModelNode node = projection.getOwner();
		do {
			val name = node.getIdentity().toString();
			if (!name.isEmpty()) {
				result.add(node.getIdentity().toString());
			}
			node = node.getParent().orElse(null);
		} while (node != null);
		return String.join(":", result.build().reverse());
	}

	private String getTypeName() {
		return projection.getType().getSimpleName();
	}

	private String getDefaultDisplayName() {
		return GUtil.toWords(getTypeName());
	}

	@Override
	public String toString() {
		return Optional.ofNullable(projection.getType().getAnnotation(DisplayName.class)).map(DisplayName::value).orElseGet(this::getDefaultDisplayName) + " '" + getPath() + "'";
	}
}
