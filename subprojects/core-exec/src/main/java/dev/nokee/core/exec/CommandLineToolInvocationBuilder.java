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

import java.io.File;

/**
 * A builder for a command line invocation.
 *
 * @since 0.4
 */
public interface CommandLineToolInvocationBuilder {
	// TODO: Support Provider
	// TODO: Support Directory
	CommandLineToolInvocationBuilder workingDirectory(File workingDirectory);

	CommandLineToolInvocationBuilder withEnvironmentVariables(CommandLineToolInvocationEnvironmentVariables environmentVariables);

	CommandLineToolInvocation build();

	<T extends CommandLineToolExecutionHandle> T buildAndSubmit(CommandLineToolExecutionEngine<T> engine);

	CommandLineToolInvocationBuilder appendStandardStreamToFile(File file);

	CommandLineToolInvocationBuilder redirectStandardOutput(CommandLineToolInvocationStandardOutputRedirect redirect);

	CommandLineToolInvocationBuilder redirectErrorOutput(CommandLineToolInvocationErrorOutputRedirect redirect);
}
