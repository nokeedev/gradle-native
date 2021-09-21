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
package dev.nokee.core.exec;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachingProcessBuilderEngine implements CommandLineToolExecutionEngine<CachingProcessBuilderEngine.Handle> {
	private final CommandLineToolExecutionEngine<ProcessBuilderEngine.Handle> delegate;
	private final LoadingCache<CommandLineToolInvocation, CommandLineToolExecutionResult> cache = CacheBuilder.newBuilder()
		.concurrencyLevel(4)
		.build(new CacheLoader<CommandLineToolInvocation, CommandLineToolExecutionResult>() {
		@Override
		public CommandLineToolExecutionResult load(CommandLineToolInvocation key) throws Exception {
			return delegate.submit(key).waitFor().assertNormalExitValue();
		}
	});

	public CachingProcessBuilderEngine(CommandLineToolExecutionEngine<ProcessBuilderEngine.Handle> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Handle submit(CommandLineToolInvocation invocation) {
		return () -> cache.getUnchecked(invocation);
	}

	public interface Handle extends CommandLineToolExecutionHandle {
		CommandLineToolExecutionResult getResult();
	}
}
