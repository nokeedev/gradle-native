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

import lombok.EqualsAndHashCode;
import org.apache.commons.io.output.CloseShieldOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static dev.nokee.core.exec.CommandLineUtils.resolve;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;

public final class CommandLineToolInvocationOutputRedirection {
	/**
	 * Creates a redirection that discard a process output.
	 *
	 * <p>Typically, this is the same as {@code a.out > /dev/null} or {@code a.out > 2> /dev/null}.
	 *
	 * @return a redirection instance that discard a process standard output, never null
	 */
	public static ToNullStreamRedirection toNullStream() {
		return new ToNullStreamRedirection();
	}

	@EqualsAndHashCode
	public static final class ToNullStreamRedirection implements CommandLineToolInvocationStandardOutputRedirect, CommandLineToolInvocationErrorOutputRedirect, OutputRedirectInternal, Serializable {
		@Override
		public OutputStream redirect(Context context) {
			return NULL_OUTPUT_STREAM;
		}

		@Override
		public String toString() {
			return "toNullStream()";
		}
	}

	/**
	 * Creates a redirection that duplicate a process output to the specified file.
	 *
	 * @param path  the file to write a process output, can be anything that represent a file and must not be null
	 * @return a redirection instance that redirect process standard output to {@link System#out}, never null
	 */
	public static ToFileRedirection toFile(Object path) {
		return new ToFileRedirection(resolve(path));
	}

	@EqualsAndHashCode
	public static final class ToFileRedirection implements CommandLineToolInvocationStandardOutputRedirect, CommandLineToolInvocationErrorOutputRedirect, OutputRedirectInternal, Serializable {
		private final String path;

		public ToFileRedirection(Path path) {
			this.path = Objects.requireNonNull(path, "'path' must not be null").toString();
		}

		@Override
		public OutputStream redirect(Context context) {
			try {
				return Files.newOutputStream(Paths.get(path), WRITE, CREATE, TRUNCATE_EXISTING);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public String toString() {
			return "toFile(" + path + ")";
		}

		public File getOutputFile() {
			return Paths.get(path).toFile();
		}
	}

	/**
	 * Creates a redirection that duplicate a process output to the {@code System#err}.
	 *
	 * @return a redirection instance that redirect a process output to {@link System#out}, never null
	 */
	public static ToSystemErrorRedirection toSystemError() {
		return new ToSystemErrorRedirection();
	}

	@EqualsAndHashCode
	public static final class ToSystemErrorRedirection implements CommandLineToolInvocationStandardOutputRedirect, CommandLineToolInvocationErrorOutputRedirect, OutputRedirectInternal, Serializable {
		@Override
		public OutputStream redirect(Context context) {
			return CloseShieldOutputStream.wrap(System.err);
		}

		@Override
		public String toString() {
			return "toSystemErr()";
		}
	}

	/**
	 * Creates a redirection that duplicate a process output to the {@code System#out}.
	 *
	 * @return a redirection instance that redirect a process output to {@link System#out}, never null
	 */
	public static ToSystemOutputRedirection toSystemOutput() {
		return new ToSystemOutputRedirection();
	}

	@EqualsAndHashCode
	public static final class ToSystemOutputRedirection implements CommandLineToolInvocationStandardOutputRedirect, CommandLineToolInvocationErrorOutputRedirect, OutputRedirectInternal, Serializable {
		@Override
		public OutputStream redirect(Context context) {
			return CloseShieldOutputStream.wrap(System.out);
		}

		@Override
		public String toString() {
			return "toSystemOut()";
		}
	}

	/**
	 * Creates a redirection that duplicate the process error output to the standard stream.
	 *
	 * <p>Typically, this is the same as {@code a.out 2&>1}.
	 *
	 * @return a redirection instance that redirect the process error output to standard stream, never null.
	 */
	public static CommandLineToolInvocationErrorOutputRedirect toStandardStream() {
		return new ToStandardStreamRedirection();
	}

	@EqualsAndHashCode
	static final class ToStandardStreamRedirection implements CommandLineToolInvocationErrorOutputRedirect, OutputRedirectInternal, Serializable {
		@Override
		public OutputStream redirect(Context context) {
			return context.getStandardOutput();
		}

		@Override
		public String toString() {
			return "toStandardStream()";
		}
	}

	interface Context {
		OutputStream getStandardOutput();
		OutputStream getErrorOutput();
	}

	interface OutputRedirectInternal {
		OutputStream redirect(Context context);
	}
}
