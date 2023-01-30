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

import org.checkerframework.checker.units.qual.A;

final class ArgumentInformationUtils {
	private static final Arg0 ARG0 = new Arg0();
	private static final Arg1 ARG1 = new Arg1();
	private static final Arg2 ARG2 = new Arg2();
	private static final Arg3 ARG3 = new Arg3();

	public static ArgumentInformation.None arg0() {
		return ARG0;
	}

	public static <A0> ArgumentInformation.Arg1<A0> arg1() {
		return ARG1.withNarrowType();
	}

	public static <A0, A1> ArgumentInformation.Arg2<A0, A1> arg2() {
		return ARG2.withNarrowType();
	}

	public static <A0, A1, A3> ArgumentInformation.Arg3<A0, A1, A3> arg3() {
		return ARG3.withNarrowType();
	}

	private static final class Arg0 implements ArgumentInformation.None {}

	private static final class Arg1 implements ArgumentInformation.Arg1<Object> {
		@SuppressWarnings("unchecked")
		public <A0> ArgumentInformation.Arg1<A0> withNarrowType() {
			return (ArgumentInformation.Arg1<A0>) this;
		}
	}

	private static final class Arg2 implements ArgumentInformation.Arg2<Object, Object> {
		@SuppressWarnings("unchecked")
		public <A0, A1> ArgumentInformation.Arg2<A0, A1> withNarrowType() {
			return (ArgumentInformation.Arg2<A0, A1>) this;
		}
	}

	private static final class Arg3 implements ArgumentInformation.Arg3<Object, Object, Object> {
		@SuppressWarnings("unchecked")
		public <A0, A1, A2> ArgumentInformation.Arg3<A0, A1, A2> withNarrowType() {
			return (ArgumentInformation.Arg3<A0, A1, A2>) this;
		}
	}
}
