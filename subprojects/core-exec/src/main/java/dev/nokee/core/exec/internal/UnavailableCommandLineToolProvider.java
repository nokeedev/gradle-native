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
package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolProvider;

public class UnavailableCommandLineToolProvider implements CommandLineToolProvider {
	@Override
	public CommandLineTool get() {
		throw new IllegalArgumentException("Don't know how to provide this tool.");
	}

	@Override
	public boolean isAvailable() {
		return false;
	}
}
