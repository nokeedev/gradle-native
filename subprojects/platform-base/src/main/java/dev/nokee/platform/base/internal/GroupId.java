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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Value(staticConstructor = "of")
public class GroupId {
	@Getter(AccessLevel.NONE) Supplier<Object> value;

	public Optional<String> get() {
		return Optional.ofNullable(value.get()).map(Object::toString);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GroupId)) return false;
		GroupId groupId = (GroupId) o;
		return Objects.equals(get(), groupId.get());
	}

	@Override
	public int hashCode() {
		return Objects.hash(get());
	}

	@Override
	public String toString() {
		return "GroupId{" +
			"value=" + value.get() +
			'}';
	}
}
