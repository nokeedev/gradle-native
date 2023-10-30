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

import dev.nokee.model.internal.names.FullyQualifiedName;
import dev.nokee.model.internal.names.MainName;
import dev.nokee.model.internal.names.QualifyingName;

import javax.annotation.Nullable;

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
}