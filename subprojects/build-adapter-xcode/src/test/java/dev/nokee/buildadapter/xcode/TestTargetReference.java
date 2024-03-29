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
package dev.nokee.buildadapter.xcode;

import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTarget;
import dev.nokee.xcode.XCTargetReference;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Optional;

@EqualsAndHashCode(doNotUseGetters = true)
public final class TestTargetReference implements XCTargetReference {
	public static XCTargetReference target(String name) {
		return new TestTargetReference(name, null);
	}

	public static XCTargetReference target(String name, XCProjectReference project) {
		return new TestTargetReference(name, project);
	}

	private final String name;
	@Nullable private final XCProjectReference project;

	public TestTargetReference(String name, @Nullable XCProjectReference project) {
		this.name = name;
		this.project = project;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public XCProjectReference getProject() {
		return Optional.ofNullable(project).orElseThrow(UnsupportedOperationException::new);
	}

	@Override
	public XCTarget load() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "target '" + name + "'";
	}
}
