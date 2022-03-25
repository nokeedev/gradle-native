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
package dev.gradleplugins.buildscript;

import dev.gradleplugins.buildscript.statements.EmptyAwareSection;
import dev.gradleplugins.buildscript.statements.Statement;

import java.io.IOException;

public enum TestStatement implements Statement, EmptyAwareSection {
	empty {
		@Override
		public boolean isEmpty() {
			return true;
		}
	},
	nonEmpty {
		@Override
		public boolean isEmpty() {
			return false;
		}
	};

	@Override
	public void writeTo(PrettyPrinter out) throws IOException {
		// do nothing
	}

	@Override
	public void accept(Visitor visitor) {
		// do nothing
	}
}
