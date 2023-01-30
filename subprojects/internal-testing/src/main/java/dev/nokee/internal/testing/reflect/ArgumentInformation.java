/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.internal.testing.reflect;

public interface ArgumentInformation {
	int count();

	interface None extends ArgumentInformation {
		@Override
		default int count() {
			return 0;
		}
	}

	interface Arg1<A0> extends ArgumentInformation {
		@Override
		default int count() {
			return 1;
		}
	}
	interface Arg2<A0, A1> extends ArgumentInformation {
		@Override
		default int count() {
			return 2;
		}
	}
	interface Arg3<A0, A1, A2> extends ArgumentInformation {
		@Override
		default int count() {
			return 3;
		}
	}
}
