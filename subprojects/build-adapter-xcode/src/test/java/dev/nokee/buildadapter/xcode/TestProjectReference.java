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

import dev.nokee.xcode.XCProject;
import dev.nokee.xcode.XCProjectReference;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;

@EqualsAndHashCode
public final class TestProjectReference implements XCProjectReference {
	public static XCProjectReference project(String name) {
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

	@Override
	public XCProject load() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "project '" + name + "'";
	}
}
