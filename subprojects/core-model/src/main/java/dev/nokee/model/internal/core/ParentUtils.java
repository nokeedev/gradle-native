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
package dev.nokee.model.internal.core;

import lombok.val;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class ParentUtils {
	public static Stream<ModelNode> stream(ParentComponent self) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new ParentIterator(self.get()), 0), false);
	}

	private static final class ParentIterator implements Iterator<ModelNode> {
		@Nullable private ModelNode nextParent;

		private ParentIterator(ModelNode self) {
			nextParent = self;
		}

		@Override
		public boolean hasNext() {
			return nextParent != null;
		}

		@Override
		public ModelNode next() {
			val result = nextParent;
			nextParent = nextParent.find(ParentComponent.class).map(ParentComponent::get).orElse(null);
			return result;
		}
	}
}
