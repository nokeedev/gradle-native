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
	private OutputSegment currentSegment;
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

		SegmentingOutputStream(OutputType type) {
			this.type = type;
		}

		@Override
		public void write(int b) throws IOException {
			synchronized (lock) {
				getCurrentSegment().append(b);
			}
		}

		private OutputSegment getCurrentSegment() {
			if (currentSegment == null) {
				return newSegment();
			} else if (currentSegment.type != type) {
				return newSegment();
			}
			return currentSegment;
		}

		private OutputSegment newSegment() {
			currentSegment = new OutputSegment(type);
			outputSegments.add(currentSegment);
			return currentSegment;
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
