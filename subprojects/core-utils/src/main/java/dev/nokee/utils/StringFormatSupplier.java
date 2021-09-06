package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.Arrays;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class StringFormatSupplier implements Supplier<String> {
	private final String format;
	private final Object[] args;

	StringFormatSupplier(String format, Object[] args) {
		this.format = requireNonNull(format);
		this.args = requireNonNull(args);
	}

	public Object[] getUnpackedArgs() {
		return Arrays.stream(args).map(DeferredUtils::unpack).toArray();
	}

	@Override
	public String get() {
		return String.format(format, getUnpackedArgs());
	}

	@Override
	public String toString() {
		val builder = new StringBuilder();
		builder.append("String.format(").append(format);
		val args = getUnpackedArgs();
		if (args.length > 0) {
			for (Object arg : args) {
				builder.append(", ").append(arg);
			}
		}
		builder.append(")");
		return builder.toString();
	}
}
