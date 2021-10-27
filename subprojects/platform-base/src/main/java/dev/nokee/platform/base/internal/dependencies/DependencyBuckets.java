/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.base.internal.dependencies;

import com.google.common.collect.Iterables;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.util.GUtil;

import java.util.function.Function;

public final class DependencyBuckets {
	public static String toDescription(DependencyBucketIdentifier identifier) {
		val builder = new StringBuilder();
		val identities = (Iterable<Object>) identifier;
		builder.append(StringUtils.capitalize(GUtil.toWords(identifier.getName().get()).replace("api", "API")));
		if (!ConsumableDependencyBucket.class.isAssignableFrom(identifier.getType()) && !ResolvableDependencyBucket.class.isAssignableFrom(identifier.getType())) {
			builder.append(" dependencies");
		}
		builder.append(" for ");
		if (Iterables.size(identities) == 1) {
			builder.append("<unknown>");
		} else {
			val ownerIdentifier = Iterables.get(identities, Iterables.size(identities) - 2);
			if (ownerIdentifier instanceof DomainObjectIdentifierInternal) {
				builder.append(((DomainObjectIdentifierInternal) ownerIdentifier).getDisplayName());
			} else if (ownerIdentifier instanceof ComponentIdentifier) {
				builder.append(ownerIdentifier);
			} else {
				builder.append("<unknown>");
			}
		}
		builder.append(".");
		return builder.toString();
	}
}
