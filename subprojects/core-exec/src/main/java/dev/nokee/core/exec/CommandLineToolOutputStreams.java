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
package dev.nokee.core.exec;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.io.output.TeeOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.function.BiFunction;

final class CommandLineToolOutputStreams {
	public static <R> Result<R> execute(CommandLineToolInvocation invocation, BiFunction<? super OutputStream, ? super OutputStream, ? extends R> action) {
		val fullOutput = new ByteArrayOutputStream();
		val capturedStdOut = new ByteArrayOutputStream();
		val capturedStdErr = new ByteArrayOutputStream();
		val outStream = new TeeOutputStream(capturedStdOut, fullOutput);
		val errStream = new TeeOutputStream(capturedStdErr, fullOutput);

		OutputStream stdout = CloseShieldOutputStream.wrap(System.out);
		OutputStream stderr = CloseShieldOutputStream.wrap(System.err);

		val closable = new ArrayList<Closeable>();

		val redirectedStdOut = ((CommandLineToolInvocationOutputRedirection.OutputRedirectInternal) invocation.getStandardOutputRedirect()).redirect(new CommandLineToolInvocationOutputRedirection.Context() {
			@Override
			public OutputStream getStandardOutput() {
				return stdout;
			}

			@Override
			public OutputStream getErrorOutput() {
				return stderr;
			}
		});

		closable.add(redirectedStdOut);

		val redirectedStdErr = ((CommandLineToolInvocationOutputRedirection.OutputRedirectInternal) invocation.getStandardOutputRedirect()).redirect(new CommandLineToolInvocationOutputRedirection.Context() {
			@Override
			public OutputStream getStandardOutput() {
				return redirectedStdOut;
			}

			@Override
			public OutputStream getErrorOutput() {
				return stderr;
			}
		});

		closable.add(redirectedStdErr);

		val finalStdOut = new TeeOutputStream(redirectedStdOut, outStream);
		val finalStdErr = new TeeOutputStream(redirectedStdErr, errStream);

		try {
			val result = action.apply(finalStdOut, finalStdErr);
			return new Result<R>() {
				@Override
				public void close() throws IOException {
					IOUtils.close(closable.toArray(new Closeable[0]));
				}

				@Override
				public R getResult() {
					return result;
				}

				@Override
				public CommandLineToolLogContent getErrorOutput() {
					return CommandLineToolLogContent.of(capturedStdErr.toString());
				}

				@Override
				public CommandLineToolLogContent getStandardOutput() {
					return CommandLineToolLogContent.of(capturedStdOut.toString());
				}

				@Override
				public CommandLineToolLogContent getOutput() {
					return CommandLineToolLogContent.of(fullOutput.toString());
				}
			};
		} catch (Throwable ex) {
			IOUtils.closeQuietly(closable.toArray(new Closeable[0]));
			throw ex;
		}
	}

	interface Result<T> extends Closeable {
		T getResult();
		CommandLineToolLogContent getErrorOutput();
		CommandLineToolLogContent getStandardOutput();
		CommandLineToolLogContent getOutput();
	}
}
