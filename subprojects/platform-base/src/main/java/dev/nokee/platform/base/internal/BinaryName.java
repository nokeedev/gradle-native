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

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class BinaryName {
	private static final String VALID_PART_CHARACTERS = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "-_";
	private final String name;

	private BinaryName(String name) {
		requireNonNull(name);
		checkArgument(!name.isEmpty());
		checkArgument(CharUtils.isAsciiAlphaLower(name.charAt(0)));
		checkArgument(StringUtils.containsOnly(StringUtils.substring(name, 1), VALID_PART_CHARACTERS));
		this.name = name;
	}

	public String get() {
		return name;
	}

	public static BinaryName of(String name) {
		return new BinaryName(name);
	}

	@Override
	public String toString() {
		return name;
	}
}
