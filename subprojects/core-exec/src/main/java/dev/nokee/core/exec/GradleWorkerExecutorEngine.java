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

import org.gradle.api.provider.Property;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

public final class GradleWorkerExecutorEngine implements CommandLineToolExecutionEngine<GradleWorkerExecutorEngine.Handle> {
	private final WorkerExecutor workerExecutor;

	public GradleWorkerExecutorEngine(WorkerExecutor workerExecutor) {
		this.workerExecutor = workerExecutor;
	}

	@Override
	public Handle submit(CommandLineToolInvocation invocation) {
		WorkQueue workQueue = workerExecutor.noIsolation();
		workQueue.submit(GradleWorkerExecutorEngineWorkAction.class, it -> {
			it.getInvocation().set(invocation);
		});
		return new Handle();
	}

	public static class Handle implements CommandLineToolExecutionHandle {}

	public interface GradleWorkerExecutorEngineWorkParameters extends WorkParameters {
		Property<CommandLineToolInvocation> getInvocation();
	}

	public static abstract class GradleWorkerExecutorEngineWorkAction implements WorkAction<GradleWorkerExecutorEngineWorkParameters> {
		@Inject
		protected abstract ExecOperations getExecOperations();

		@Override
		public void execute() {
			new ExecOperationsExecutionEngine(getExecOperations()).submit(getParameters().getInvocation().get()).result().assertNormalExitValue();
		}
	}
}
