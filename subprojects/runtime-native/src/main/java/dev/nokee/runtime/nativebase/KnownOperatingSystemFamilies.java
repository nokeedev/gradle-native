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
package dev.nokee.runtime.nativebase;

import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

final class KnownOperatingSystemFamilies {
	private KnownOperatingSystemFamilies() {}

	private static final List<KnownOperatingSystem> KNOWN_OPERATING_SYSTEMS = asList(
		new KnownOperatingSystem("windows"),
		new KnownOperatingSystem("macos", "mac os x", "darwin", "osx"),
		new KnownOperatingSystem("linux"),
		new KnownOperatingSystem("freebsd"),
		new KnownOperatingSystem("ios"),
		new KnownOperatingSystem("solaris", "sunos"),
		new KnownOperatingSystem("hpux", "hp-ux")
	);

	public static OperatingSystemFamily forName(String name) {
		for (KnownOperatingSystem knownOperatingSystem : KNOWN_OPERATING_SYSTEMS) {
			if (knownOperatingSystem.isAlias(name)) {
				return knownOperatingSystem;
			}
		}
		return new UnknownOperatingSystem(name); // unknown operating system, use as-is
	}

	public static String canonical(String name) {
		for (KnownOperatingSystem knownOperatingSystem : KNOWN_OPERATING_SYSTEMS) {
			if (knownOperatingSystem.isAlias(name)) {
				return knownOperatingSystem.getCanonicalName();
			}
		}
		return name.toLowerCase(Locale.CANADA);
	}

	private static final class KnownOperatingSystem extends OperatingSystemFamily implements Serializable {
		private final String canonicalName;
		private final List<String> aliases;

		private KnownOperatingSystem(String canonicalName, String... aliases) {
			this.canonicalName = canonicalName;
			this.aliases = ImmutableList.copyOf(aliases);
		}

		@Override
		public String getName() {
			return canonicalName;
		}

		@Override
		public String getCanonicalName() {
			return canonicalName;
		}

		public boolean isAlias(String input) {
			input = input.toLowerCase(Locale.CANADA);
			return input.contains(canonicalName) || aliases.stream().anyMatch(input::contains);
		}
	}

	private static final class UnknownOperatingSystem extends OperatingSystemFamily implements Serializable {
		private final String name;

		private UnknownOperatingSystem(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getCanonicalName() {
			return name.toLowerCase(Locale.CANADA);
		}

		@Override
		public boolean isWindows() {
			return false;
		}

		@Override
		public boolean isLinux() {
			return false;
		}

		@Override
		public boolean isMacOs() {
			return false;
		}

		@Override
		public boolean isFreeBSD() {
			return false;
		}

		@Override
		public boolean isIos() {
			return false;
		}

		@Override
		public boolean isSolaris() {
			return false;
		}

		@Override
		public boolean isHewlettPackardUnix() {
			return false;
		}
	}
}
