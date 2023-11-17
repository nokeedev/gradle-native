/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.model.internal;

import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.names.FullyQualifiedName;
import dev.nokee.model.internal.names.MainName;
import dev.nokee.model.internal.names.QualifyingName;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ModelObjectIdentifiers {
	public static FullyQualifiedName asFullyQualifiedName(ModelObjectIdentifier identifier) {
		return FullyQualifiedName.of(identifier.getName().toQualifiedName(asQualifyingName(identifier.getParent())));
	}

	public static QualifyingName asQualifyingName(@Nullable ModelObjectIdentifier identifier) {
		if (identifier == null) {
			return QualifyingName.empty();
		}

		if (identifier instanceof ProjectIdentifier) {
			return asQualifyingName(identifier.getParent());
		}

		if (identifier.getName() instanceof MainName) {
			return asQualifyingName(identifier.getParent());
		} else {
			return QualifyingName.of(identifier.getName().toQualifiedName(asQualifyingName(identifier.getParent())));
		}
	}

	public static ModelPath asPath(ModelObjectIdentifier identifier) {
		final List<String> result = new ArrayList<>();

		do {
			if (!(identifier instanceof ProjectIdentifier)) {
				result.add(0, identifier.getName().toString());
			}
		} while ((identifier = identifier.getParent()) != null);

		return ModelPath.path(result);
	}

	public static boolean descendantOf(ModelObjectIdentifier thiz, ModelObjectIdentifier parentCandidate) {
		while ((thiz = thiz.getParent()) != null && !thiz.equals(parentCandidate)) {
			// continue
		}

		return Objects.equals(thiz, parentCandidate);
	}
}
