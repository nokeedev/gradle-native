package dev.gradleplugins.exemplarkit;

import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class Step {
	private final String executable;
	private final List<String> arguments;
	@Nullable
	private final String output;
	private final Map<String, Object> attributes;

	private Step(String executable, List<String> arguments, @Nullable String output, Map<String, Object> attributes) {
		this.executable = executable;
		this.arguments = arguments;
		this.output = output;
		this.attributes = attributes;
	}

	/**
	 * Returns the executable of this step.
	 *
	 * @return the executable of this step.
	 */
	public String getExecutable() {
		return executable;
	}

	/**
	 * Returns the argument for the executable of this step.
	 *
	 * @return a list of argument for the executable of this step, never null.
	 */
	public List<String> getArguments() {
		return Collections.unmodifiableList(arguments);
	}

	/**
	 * Returns the exemplar output of this step.
	 * The absence of output doesn't necessarily means this step won't produce any output during execution.
	 *
	 * @return the exemplar output of this step if present
	 */
	public Optional<String> getOutput() {
		return Optional.ofNullable(output);
	}

	/**
	 * Returns key-value pairs to use during execution and assertion.
	 * The key-value pairs has not particular meanings.
	 * It's up to the user to provide meanings.
	 *
	 * @return a key-value pairs of attributes, never null
	 */
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String executable;
		private final List<String> arguments = new ArrayList<>();
		private String output;
		private final Map<String, Object> attributes = new HashMap<>();

		public Builder execute(String executable, String... args) {
			List<String> arguments = new ArrayList<>();
			Stream.of(requireNonNull(args, "No arguments can be null.")).forEach(arguments::add);
			return execute(executable, arguments);
		}

		public Builder execute(String executable, Iterable<String> arguments) {
			this.executable = Objects.requireNonNull(executable, "Executable cannot be null.").trim();
			this.arguments.clear();
			for (String argument : requireNonNull(arguments, "No arguments can be null.")) {
				this.arguments.add(requireNonNull(argument, "No arguments can be null.").trim());
			}
			return this;
		}

		public Builder output(@Nullable String content) {
			this.output = content;
			return this;
		}

		public Builder output(String l1, String l2, String... ln) {
			List<String> lines = new ArrayList<>();
			lines.add(requireNonNullOutputLine(l1));
			lines.add(requireNonNullOutputLine(l2));
			for (String line : requireNonNullOutputLine(ln)) {
				lines.add(requireNonNullOutputLine(line));
			}
			this.output = String.join(System.lineSeparator(), lines);
			return this;
		}

		private static <T> T requireNonNullOutputLine(T v) {
			return requireNonNull(v, "No output lines can be null.");
		}

		public Builder attributes(Map<String, Object> attributes) {
			this.attributes.clear();
			this.attributes.putAll(attributes);
			return this;
		}

		public Builder attribute(String key, Object value) {
			attributes.put(key, value);
			return this;
		}

		public Step build() {
			if (executable == null) {
				throw new IllegalStateException("Please specify an executable for this step.");
			}
			return new Step(executable, arguments, output, attributes);
		}
	}
}
