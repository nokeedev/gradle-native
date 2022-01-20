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
package nokeebuild.testing.strategies;

public final class OperatingSystemFamilyTestingStrategies {
	private OperatingSystemFamilyTestingStrategies() {}

	public static final OperatingSystemFamilyTestingStrategy WINDOWS = new AbstractOperatingSystemFamilyTestingStrategy() {
		@Override
		public String getName() {
			return "windows";
		}

		@Override
		public String toString() {
			return "coverage for Windows family";
		}
	};
	public static final OperatingSystemFamilyTestingStrategy MACOS = new AbstractOperatingSystemFamilyTestingStrategy() {
		@Override
		public String getName() {
			return "macos";
		}

		@Override
		public String toString() {
			return "coverage for macOS family";
		}
	};
	public static final OperatingSystemFamilyTestingStrategy LINUX = new AbstractOperatingSystemFamilyTestingStrategy() {
		@Override
		public String getName() {
			return "linux";
		}

		@Override
		public String toString() {
			return "coverage for Linux family";
		}
	};
	public static final OperatingSystemFamilyTestingStrategy FREEBSD = new AbstractOperatingSystemFamilyTestingStrategy() {
		@Override
		public String getName() {
			return "freebsd";
		}

		@Override
		public String toString() {
			return "coverage for FreeBSD family";
		}
	};
	private static final OperatingSystemFamilyTestingStrategy[] VALUES = new OperatingSystemFamilyTestingStrategy[] {WINDOWS, MACOS, LINUX, FREEBSD};

	public static OperatingSystemFamilyTestingStrategy[] values() {
		return VALUES;
	}

	private static abstract class AbstractOperatingSystemFamilyTestingStrategy implements OperatingSystemFamilyTestingStrategy {}
}
