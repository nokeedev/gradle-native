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
package dev.nokee.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TextCaseUtils {
	public static String toCamelCase(CharSequence string) {
		return camel().convert(string);
	}

	public static CaseConverter camel() {
		return string -> Stream.of(string).flatMap(new ToCamelCaseSeparator()).map(StringUtils::capitalize).collect(Collectors.joining());
	}

	public static String toLowerCamelCase(CharSequence string) {
		return lowerCamel().convert(string);
	}

	public static CaseConverter lowerCamel() {
		return string -> Stream.of(string).flatMap(new ToCamelCaseSeparator()).map(new LowerCapitalize()).collect(Collectors.joining());
	}

	public static String toWords(CharSequence string) {
		return words().convert(string);
	}

	public static CaseConverter words() {
		return string -> Stream.of(string).flatMap(new ToWordSeparator()).collect(Collectors.joining(" "));
	}

	public static String toSnakeCase(CharSequence string) {
		return snake().convert(string);
	}

	public static CaseConverter snake() {
		return string -> Stream.of(string).flatMap(new ToWordSeparator()).collect(Collectors.joining("_"));
	}

	public static String toConstant(CharSequence string) {
		return constant().convert(string);
	}

	public static CaseConverter constant() {
		return string -> Stream.of(string).flatMap(new ToWordSeparator()).map(it -> it.toUpperCase(Locale.US)).collect(Collectors.joining("_"));
	}

	public static String toKebabCase(CharSequence string) {
		return kebab().convert(string);
	}

	public static CaseConverter kebab() {
		return string -> Stream.of(string).flatMap(new ToWordSeparator()).collect(Collectors.joining("-"));
	}

	// There are three things that are important:
	//   - word separator (how words are split from each other)
	//   - each word manipulator (title case, all upper case, all lower case, only first word is not capitalized...)
	//   - word separator char (how words are joined together)
	public interface CaseConverter {
		String convert(CharSequence string);
	}

	private static final class ToCamelCaseSeparator implements Function<CharSequence, Stream<String>> {
		private static final Pattern WORD_SEPARATOR = Pattern.compile("\\W+");

		@Override
		public Stream<String> apply(CharSequence string) {
			final List<String> result = new ArrayList<>();
			final Matcher matcher = WORD_SEPARATOR.matcher(string);
			int pos = 0;
			while (matcher.find()) {
				final String chunk = string.subSequence(pos, matcher.start()).toString();
				pos = matcher.end();
				if (!chunk.isEmpty()) {
					result.add(chunk);
				}
			}
			final String rest = string.subSequence(pos, string.length()).toString();
			result.add(rest);
			return result.stream();
		}
	}

	private static final class ToWordSeparator implements Function<CharSequence, Stream<String>> {
		private static final Pattern UPPER_LOWER = Pattern.compile("(?m)([A-Z]*)([a-z0-9]*)");

		@Override
		public Stream<String> apply(CharSequence string) {
			final List<String> result = new ArrayList<>();
			final Matcher matcher = UPPER_LOWER.matcher(string);
			int pos = 0;
			StringBuilder builder = new StringBuilder();
			while (pos < string.length()) {
				matcher.find(pos);
				if (matcher.end() == pos) {
					// Not looking at a match
					pos++;
					continue;
				}
				if (builder.length() > 0) {
					result.add(builder.toString());
					builder = new StringBuilder();
				}

				String group1 = matcher.group(1).toLowerCase();
				String group2 = matcher.group(2);
				if (group2.length() == 0) {
					builder.append(group1);
				} else {
					if (group1.length() > 1) {
						builder.append(group1.substring(0, group1.length() - 1));
						result.add(builder.toString());
						builder = new StringBuilder();

						builder.append(group1.substring(group1.length() - 1));
					} else {
						builder.append(group1);
					}
					builder.append(group2);
				}
				pos = matcher.end();
			}

			if (builder.length() > 0) {
				result.add(builder.toString());
			}
			return result.stream();
		}
	}

	private static final class LowerCapitalize implements UnaryOperator<String> {
		private boolean first = true;

		@Override
		public String apply(String s) {
			if (first) {
				first = false;
				return StringUtils.uncapitalize(s);
			} else {
				return StringUtils.capitalize(s);
			}
		}
	}
}
