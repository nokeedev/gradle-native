/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.internal.ancestors;

import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;

import java.util.Iterator;
import java.util.Set;

@EqualsAndHashCode
public final class Ancestors implements Iterable<AncestorRef> {
	private final Set<AncestorRef> ancestors;

	Ancestors(Set<AncestorRef> ancestors) {
		this.ancestors = ancestors;
	}

	public boolean contains(AncestorRef ancestorRef) {
		return ancestors.contains(ancestorRef);
	}

	@Override
	public Iterator<AncestorRef> iterator() {
		return ancestors.iterator();
	}

	public static Ancestors of(AncestorRef firstRef, AncestorRef... otherRefs) {
		return new Ancestors(ImmutableSet.<AncestorRef>builder().add(firstRef).add(otherRefs).build());
	}
}
