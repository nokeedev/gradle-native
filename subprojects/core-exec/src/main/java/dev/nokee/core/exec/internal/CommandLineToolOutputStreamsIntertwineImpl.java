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

import dev.nokee.core.exec.CommandLineToolLogContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static dev.nokee.core.exec.CommandLineToolLogContent.of;
import static java.util.stream.Collectors.joining;

public final class CommandLineToolOutputStreamsIntertwineImpl implements CommandLineToolOutputStreams {
	private final List<OutputSegment> outputSegments = new ArrayList<>();
	private final Object lock = new Object();
	private final SegmentingOutputStream out = new SegmentingOutputStream(OutputType.OUT);
	private final SegmentingOutputStream err = new SegmentingOutputStream(OutputType.ERR);

	public CommandLineToolLogContent getStandardOutputContent() {
		return of(outputSegments.stream().filter(OutputType.OUT).map(OutputSegment::getAsString).collect(joining()));
	}

	public CommandLineToolLogContent getErrorOutputContent() {
		return of(outputSegments.stream().filter(OutputType.ERR).map(OutputSegment::getAsString).collect(joining()));
	}

	public CommandLineToolLogContent getOutputContent() {
		return of(outputSegments.stream().map(OutputSegment::getAsString).collect(joining()));
	}

	@Override
	public OutputStream getStandardOutput() {
		return out;
	}

	@Override
	public OutputStream getErrorOutput() {
		return err;
	}

	private class SegmentingOutputStream extends OutputStream {
		private final OutputType type;
		private OutputSegment currentSegment;

		SegmentingOutputStream(OutputType type) {
			this.type = type;
			this.currentSegment = new OutputSegment(type);
		}

		@Override
		public void write(int b) throws IOException {
			currentSegment.append(b);
		}

		@Override
		public void flush() throws IOException {
			synchronized (lock) {
				outputSegments.add(currentSegment);
				currentSegment = new OutputSegment(type);
			}
		}
	}

	private enum OutputType implements Predicate<OutputSegment> {
		OUT, ERR;

		@Override
		public boolean test(OutputSegment outputSegment) {
			return outputSegment.type == this;
		}
	}

	private static final class OutputSegment {
		private final OutputType type;
		private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		OutputSegment(OutputType type) {
			this.type = type;
		}

		void append(int b) {
			buffer.write(b);
		}

		String getAsString() {
			return buffer.toString();
		}
	}
}
