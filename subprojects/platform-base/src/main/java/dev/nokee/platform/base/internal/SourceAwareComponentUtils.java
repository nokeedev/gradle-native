package dev.nokee.platform.base.internal;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.SourceAwareComponent;
import lombok.val;

public class SourceAwareComponentUtils {
	public static FunctionalSourceSet sourceViewOf(Object target) {
		if (target instanceof SourceAwareComponent) {
			return (FunctionalSourceSet) ((SourceAwareComponent<?>) target).getSources();
		} else if (target instanceof FunctionalSourceSet) {
			return (FunctionalSourceSet) target;
		} else {
			val node = ModelNodes.of(target);
			if (node.hasDescendant("sources")) {
				return node.getDescendant("sources").get(FunctionalSourceSet.class);
			}
		}
		throw new UnsupportedOperationException("Target '" + target + "' is not source aware or a language source set view itself.");
	}
}
