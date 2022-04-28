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
import lombok.val;
import org.apache.commons.lang3.StringUtils;

public final class DependencyBuckets {
	public static String toDescription(DependencyBucketIdentifier identifier) {
		val builder = new StringBuilder();
		val identities = (Iterable<Object>) identifier;
		val identity = Iterables.getLast(identifier);
		builder.append(StringUtils.capitalize(identity.toString()));
		builder.append(" for ");
		if (Iterables.size(identities) == 1) {
			builder.append("<unknown>");
		} else {
			builder.append(identifier.getOwnerIdentifier());
		}
		builder.append(".");
		return builder.toString();
	}
}
