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

import com.google.common.base.Preconditions;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolProvider;

import java.util.function.Supplier;

public class SupplierCommandLineToolProvider implements CommandLineToolProvider {
	private Supplier<CommandLineTool> toolSupplier;
	private CommandLineTool suppliedTool;

	public SupplierCommandLineToolProvider(Supplier<CommandLineTool> toolSupplier) {
		this.toolSupplier = Preconditions.checkNotNull(toolSupplier);
	}

	@Override
	public CommandLineTool get() {
		try {
			if (isAvailableAndPassAlongExceptions()) {
				return suppliedTool;
			}
			throw new IllegalArgumentException("Don't know how to provide this tool.");
		} catch (Throwable ex) {
			throw new IllegalArgumentException("Don't know how to provide this tool.", ex);
		}
	}

	private boolean isAvailableAndPassAlongExceptions() {
		resolveTool();
		return suppliedTool != null;
	}

	@Override
	public boolean isAvailable() {
		try {
			return isAvailableAndPassAlongExceptions();
		} catch (Throwable ex) {
			return false;
		}
	}

	private void resolveTool() {
		if (toolSupplier != null) {
			suppliedTool = toolSupplier.get();
			toolSupplier = null;
		}
	}
}
