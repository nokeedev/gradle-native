/*
 * Copyright 2024 the original author or authors.
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

package dev.nokee.model.internal.discover;

import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.ModelType;

import java.util.List;
import java.util.stream.Collectors;

public final class GroupRule implements DisRule {
	private final ModelType<?> targetType;
	private final List<Entry> entries;

	public GroupRule(ModelType<?> targetType, List<Entry> entries) {
		this.targetType = targetType;
		this.entries = entries;
	}

	public List<Entry> getEntries() {
		return entries;
	}

	@Override
	public void execute(Details details) {
		if (details.getCandidate().getType().isSubtypeOf(targetType)) {
			for (Entry entry : entries) {
				details.newCandidate(entry.elementName, entry.produceType);
			}
		}
	}

	@Override
	public String toString() {
		return "any subtype of " + targetType.getConcreteType().getSimpleName() + " will produce the following children: " + entries.stream().map(it -> it.elementName + " (" + it.produceType.getConcreteType().getSimpleName() + ")").collect(Collectors.joining(", "));
	}

	public static final class Entry {
		private final ElementName elementName;
		private final ModelType<?> produceType;

		public Entry(ElementName elementName, ModelType<?> produceType) {
			this.elementName = elementName;
			this.produceType = produceType;
		}

		public ModelType<?> getProduceType() {
			return produceType;
		}
	}
}
