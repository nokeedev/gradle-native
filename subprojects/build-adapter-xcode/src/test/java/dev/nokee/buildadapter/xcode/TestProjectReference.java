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

import dev.nokee.xcode.DefaultXCProjectReference;
import dev.nokee.xcode.XCProject;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.nio.file.Path;

@EqualsAndHashCode
public final class TestProjectReference implements XCProjectReference, Serializable {
	public static TestProjectReference project(String name) {
		assert !name.endsWith(".xcodeproj") : "do not include .xcodeproj extension";
		return new TestProjectReference(name);
	}

	private final String name;

	private TestProjectReference(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Path getLocation() {
		throw new UnsupportedOperationException();
	}

	public XCProjectReference inDirectory(Path location) {
		return new DefaultXCProjectReference(location.resolve(name + ".xcodeproj"));
	}

	@Override
	public XCTargetReference ofTarget(String name) {
		return new TestTargetReference(name, this);
	}

	@Override
	public XCProject load() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "project '" + name + ".xcodeproj'";
	}
}
