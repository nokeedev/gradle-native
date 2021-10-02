/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.base.internal;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.model.internal.core.ModelNodeUtils;
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
				return ModelNodeUtils.getDescendant(node, "sources").get(FunctionalSourceSet.class);
			}
		}
		throw new UnsupportedOperationException("Target '" + target + "' is not source aware or a language source set view itself.");
	}
}
