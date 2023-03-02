/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.xcode;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static dev.nokee.xcode.MacroExpansionIntegrationTester.XCMacro.Simple;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class MacroExpansionIntegrationTester {
	interface BaseIntegrationTester {
		Map<String, String> buildSettings();
		String var(String name);
		XCMacro macro();
		String expand(String s);
	}

	interface MacroExpansionIntegrationTests extends BaseIntegrationTester {
		void __do_not_implement_this_interface_instead_choose_between_AllowedTests_or_DisallowedTests();

		interface AllowedTests extends BaseIntegrationTester {
			@BeforeEach
			default void givenNestedBuildSettings() {
				buildSettings().put("FOO__MY_BAR", "foo and my-bar value");
				buildSettings().put("FOO__MY_FAR__BAR", "foo, my-far and bar value");
				buildSettings().put("MY_FOO__BAR", "my-foo and bar value");
				buildSettings().put("MyFoo", "MY_FOO");
				buildSettings().put("MyBar", "MY_BAR");
				buildSettings().put("MyFar", "MY_FAR");
			}

			@Test
			default void allowsVariableAtTheEndOfVariableNameReference() {
				assertThat(expand(var("FOO__" + var("MyBar"))), into("foo and my-bar value"));
			}

			@Test
			default void allowsVariableInMiddleOfVariableNameReference() {
				assertThat(expand(var("FOO__" + var("MyFar") + "__BAR")), into("foo, my-far and bar value"));
			}

			@Test
			default void allowsVariableAtTheBeginningOfVariableNameReference() {
				assertThat(expand(var(var("MyFoo") + "__BAR")), into("my-foo and bar value"));
			}

			@Test
			default void bob() {
				buildSettings().put("FOO_bar", "my-result");
				buildSettings().put("BAR", "bar");
				assertThat(expand(var("FOO_" + var("BAR"))), into("my-result"));
			}

			@Test
			default void allowsEmptyVariableName() {
				assertThat(expand("something-before " + var("") + " something-after"), into("something-before  something-after"));
			}

			@Test
			default void escape() {
				buildSettings().put("FOO", "should-not-appears");
				assertThat(expand("$" + var("FOO")), into(var("FOO")));
			}
		}

		interface DisallowedTests extends BaseIntegrationTester {
			@Test
			default void bob() {
				buildSettings().put("FOO_bar", "my-result");
				buildSettings().put("BAR", "bar");
				assertThat(expand(var("FOO_" + var("BAR"))), into(var("FOO_bar")));
			}
		}
	}

	interface SingleCharacterTester extends BaseIntegrationTester {
		TestCharacter ch();
	}

	static final class CharExTester {
		public static final class AtBeginningTester {

			private interface GivenCharAtBeginning extends SingleCharacterTester {
				@BeforeEach
				default void givenCharAtBeginning() {
					buildSettings().put(nameThatStartsWith(ch()), illegalCharPayload(ch()));
				}
			}

			public interface Literal extends SingleCharacterTester, GivenCharAtBeginning {
				@Test
				default void treatsVariableAsLiteralOnWhenAtBeginning() {
					assertThat(expand(var(nameThatStartsWith(ch()))), into(var(nameThatStartsWith(ch()))));
				}
			}

			public interface Allows extends SingleCharacterTester, GivenCharAtBeginning {
				@Test
				default void alwaysAllowedCharactersAtBeginningOfVariableNameReference() {
					assertThat(expand(var(nameThatStartsWith(ch()))), into(isExpandable(ch())));
				}
			}

			public interface Disallows extends SingleCharacterTester, GivenCharAtBeginning {
				@Test
				default void alwaysDisallowedCharactersAtBeginningOfVariableNameReference() {
					assertThat(expand(var(nameThatStartsWith(ch()))), into(nullValue()));
				}
			}

			public interface EarlyCloses extends SingleCharacterTester, GivenCharAtBeginning {
				@Test
				default void earlyCloseVariableCharactersAtBeginningOfVariableNameReference() {
					assertThat(expand(var(nameThatStartsWith(ch()))), into("VariableName" + ch()));
				}
			}

			public interface Crash extends SingleCharacterTester, GivenCharAtBeginning {
				@Test
				default void throwsExceptionWhenCharactersAtBeginningOfVariableNameReference() {
					assertThrows(RuntimeException.class, () -> expand(var(nameThatStartsWith(ch()))));
				}
			}
		}

		public static final class AtEndingTester {

			private interface GivenCharAtEnd extends SingleCharacterTester {
				@BeforeEach
				default void givenCharAtEnding() {
					buildSettings().put(nameThatEndsWith(ch()), illegalCharPayload(ch()));
				}
			}

			public interface Allows extends SingleCharacterTester, GivenCharAtEnd {
				@Test
				default void alwaysAllowedCharactersAtEndOfVariableNameReference() {
					assertThat(expand(var(nameThatEndsWith(ch()))), into(isExpandable(ch())));
				}
			}

			public interface Disallows extends SingleCharacterTester, GivenCharAtEnd {
				@Test
				default void alwaysDisallowedCharactersAtEndOfVariableNameReference() {
					buildSettings().put("VariableName", "illegal");
					assertThat(expand(var(nameThatEndsWith(ch()))), into(nullValue()));
				}
			}

			public interface EarlyCloses extends SingleCharacterTester, GivenCharAtEnd {
				@Test
				default void earlyClosesCharactersAtEndOfVariableNameReference() {
					buildSettings().put("VariableName", "variable-name");
					assertThat(expand(var(nameThatEndsWith(ch()))), into("variable-name" + ch()));
				}
			}

			public interface Literal extends SingleCharacterTester, GivenCharAtEnd {
				@Test
				default void treatsAsLiteralWhenCharactersAtEndOfVariableNameReference() {
					assertThat(expand(var(nameThatEndsWith(ch()))), into(var(nameThatEndsWith(ch()))));
				}
			}

			public interface Crash extends SingleCharacterTester, GivenCharAtEnd {
				@Test
				default void throwsExceptionWhenCharactersAtEndOfVariableNameReference() {
					assertThrows(RuntimeException.class, () -> expand(var(nameThatEndsWith(ch()))));
				}
			}
		}

		public static final class AtMiddleTester {
			private interface GivenCharAtMiddle extends SingleCharacterTester {
				@BeforeEach
				default void givenCharAtMiddle() {
					buildSettings().put(nameThatEmbed(ch()), illegalCharPayload(ch()));
				}
			}

			public interface Allows extends SingleCharacterTester, GivenCharAtMiddle {
				@Test
				default void alwaysAllowedCharactersInMiddleOfVariableNameReference() {
					assertThat(expand(var(nameThatEmbed(ch()))), into(isExpandable(ch())));
				}
			}

			public interface Disallows extends SingleCharacterTester, GivenCharAtMiddle {
				@Test
				default void alwaysDisallowedCharactersInMiddleOfVariableNameReference() {
					buildSettings().put("Variable", "illegal");
					assertThat(expand(var(nameThatEmbed(ch()))), into(nullValue()));
				}
			}

			public interface EarlyCloses extends SingleCharacterTester, GivenCharAtMiddle {
				@Test
				default void earlyCloseCharactersInMiddleOfVariableNameReference() {
					buildSettings().put("Variable", "var");
					assertThat(expand(var(nameThatEmbed(ch()))), into("varName" + ch()));
				}
			}

			public interface Literal extends SingleCharacterTester, GivenCharAtMiddle {
				@Test
				default void treatsAsLiteralWhenCharactersInMiddleOfVariableNameReference() {
					assertThat(expand(var(nameThatEmbed(ch()))), into(var(nameThatEmbed(ch()))));
				}
			}

			public interface Crash extends SingleCharacterTester, GivenCharAtMiddle {
				@Test
				default void throwsExceptionWhenCharactersInMiddleOfVariableNameReference() {
					assertThrows(RuntimeException.class, () -> expand(var(nameThatEmbed(ch()))));
				}
			}
		}

		public interface LiteralTester extends AtBeginningTester.Literal, AtMiddleTester.Literal, AtEndingTester.Literal {}

		public interface AllowedTester extends AtBeginningTester.Allows, AtMiddleTester.Allows, AtEndingTester.Allows {}

		public interface DisallowedTester extends AtBeginningTester.Disallows, AtMiddleTester.Disallows, AtEndingTester.Disallows {}

		public interface CrashedTester extends AtBeginningTester.Crash, AtMiddleTester.Crash, AtEndingTester.Crash {}
		public interface EarlyCloses extends AtBeginningTester.EarlyCloses, AtMiddleTester.EarlyCloses, AtEndingTester.EarlyCloses {}
	}

	abstract class AbstractCharacterIntegrationTester extends AbstractIntegrationTester {
		protected abstract class CharTester extends AbstractIntegrationTester {
			@BeforeEach
			void givenBuildSettings() {
				buildSettings().putAll(buildSettingsWithCharacter(ch()));
			}

			public TestCharacter ch() {
				// Imply TestCharacter from class name
				return TestCharacter.valueOf(getClass().getSimpleName().replace("Character", ""));
			}

			@Override
			public String var(String name) {
				return AbstractCharacterIntegrationTester.this.var(name);
			}
		}
	}

	enum TestCharacter {
		Space(' '), ExclamationMark('!'), QuotationMark('"'), NumberSign('#'), DollarSign('$'), PercentSign('%'), Ampersand('&'),
		Apostrophe('\''), LeftParenthesis('('), RightParenthesis(')'), Asterisk('*'), PlusSign('+'), Comma(','), HyphenMinus('-'), FullStop('.'),
		Slash('/'), Colon(':'), Semicolon(';'), LessThanSign('<'), EqualsSign('='), GreaterThanSign('>'), QuestionMark('?'), AtSign('@'),
		Backtick('`'), LeftSquareBracket('['), Backslash('\\'), Tilde('~'), RightSquareBracket(']'), Caret('^'), Underscore('_'),
		LeftCurlyBracket('{'), VerticalBar('|'), RightCurlyBracket('}'),
		Zero('0'), One('1'), Two('2'), Three('3'), Four('4'), Five('5'), Six('6'), Seven('7'), Eight('8'), Nine('9')
		;

		private final char c;

		TestCharacter(char c) {
			this.c = c;
		}

		@Override
		public String toString() {
			return String.valueOf(c);
		}
	}

	Map<String, String> buildSettings = new LinkedHashMap<>();

	private abstract class AbstractIntegrationTester implements BaseIntegrationTester {
		@Override
		public Map<String, String> buildSettings() {
			return buildSettings;
		}

		@Override
		public String expand(String s) {
			return MacroExpansionIntegrationTester.this.expand(s);
		}

		@Override
		public XCMacro macro() {
			return Arrays.stream(getClass().getCanonicalName().split("\\.")).sequential().filter(it -> it.endsWith("Macro")).findFirst().map(it -> it.replace("Macro", "")).map(XCMacro::valueOf).orElseThrow(RuntimeException::new);
		}

		@Override
		public String var(String name) {
			return macro().var(name);
		}
	}

	interface ExpandNonExistingMacroTester {
		interface ToLiteral extends BaseIntegrationTester {
			@Test
			default void expandsNonExistingMacroToLiteral() {
				assertThat(expand("before " + var("NON_EXISTANT") + " after"), into("before " + var("NON_EXISTANT") + " after"));
			}

			@Test
			default void expandsMacroWithNoNameToLiteral() {
				assertThat(expand("before " + var("") + " after"), into("before " + var("") + " after"));
			}
		}

		interface ToNothing extends BaseIntegrationTester {
			@Test
			default void expandNonExistingMacroToNothing() {
				assertThat(expand("before " + var("NON_EXISTANT") + " after"), into("before  after"));
			}

			@Test
			default void expandsMacroWithNoNameToNothing() {
				assertThat(expand("before " + var("") + " after"), into("before  after"));
			}
		}
	}

	private static String nameThatStartsWith(TestCharacter ch) {
		return ch + "VariableName";
	}

	private static String nameThatEndsWith(TestCharacter ch) {
		return "VariableName" + ch;
	}

	private static String nameThatEmbed(TestCharacter ch) {
		return "Variable" + ch + "Name";
	}

	private static String illegalCharPayload(TestCharacter ch) {
		return "illegal-but-may-be-legal ==> '" + ch + "'";
	}

	private static Matcher<String> isExpandable(TestCharacter ch) {
		return equalTo(illegalCharPayload(ch));
	}

	@SuppressWarnings("unchecked")
	private static Matcher<String> into(Matcher<? super String> matcher) {
		return (Matcher<String>) matcher;
	}

	private static Matcher<String> into(String s) {
		return into(equalTo(s));
	}






	@Nested
	class ParenthesisMacro extends AbstractIntegrationTester implements MacroExpansionIntegrationTests.AllowedTests, EscapeMacroTester, WrapMacroTester, ExpandNonExistingMacroTester.ToNothing {
		@Nested
		class CharacterTests extends MacroRefCharacterTester {
			@Override
			public String var(String name) {
				return ParenthesisMacro.this.var(name);
			}

			@Nested class RightParenthesisCharacter extends CharTester implements CharExTester.EarlyCloses {}
			@Nested class RightCurlyBracketCharacter extends CharTester implements CharExTester.AllowedTester {}
			@Nested class RightSquareBracketCharacter extends CharTester implements CharExTester.AllowedTester {}
		}
	}

	@Nested
	class CurlyBracketMacro extends AbstractIntegrationTester implements MacroExpansionIntegrationTests.AllowedTests, EscapeMacroTester, WrapMacroTester, ExpandNonExistingMacroTester.ToNothing {
		@Nested
		class CharacterTests extends MacroRefCharacterTester {
			@Override
			public String var(String name) {
				return CurlyBracketMacro.this.var(name);
			}

			@Nested class RightParenthesisCharacter extends CharTester implements CharExTester.AllowedTester {}
			@Nested class RightCurlyBracketCharacter extends CharTester implements CharExTester.EarlyCloses {}
			@Nested class RightSquareBracketCharacter extends CharTester implements CharExTester.AllowedTester {}
		}
	}

	abstract class MacroRefCharacterTester extends AbstractCharacterIntegrationTester {
		@Override
		public abstract String var(String name);

		@Nested class DollarSignCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class PercentSignCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class NumberSignCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class AtSignCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class ExclamationMarkCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class CaretCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class AmpersandCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class AsteriskCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class LeftParenthesisCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class LessThanSignCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class GreaterThanSignCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class CommaCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class ApostropheCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class QuotationMarkCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class LeftCurlyBracketCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class SlashCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class VerticalBarCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class BackslashCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class SemicolonCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class EqualsSignCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class PlusSignCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class FullStopCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class TildeCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class QuestionMarkCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class BacktickCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class UnderscoreCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class HyphenMinusCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class ZeroCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class OneCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class TwoCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class ThreeCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class FourCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class FiveCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class SixCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class SevenCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class EightCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class NineCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class SpaceCharacter extends CharTester implements CharExTester.AllowedTester {}
		@Nested class ColonCharacter extends CharTester implements CharExTester.AtBeginningTester.Disallows {
			@Test
			void ignoresUnknownRetrievalOperation() {
				buildSettings().put("Variable", "var");
				assertThat(expand(var(nameThatEmbed(ch()))), into("var"));
			}

			@Test
			void ignoresEmptyOperationList() {
				buildSettings().put("VariableName", "variable-name");
				assertThat(expand(var(nameThatEndsWith(ch()))), into("variable-name"));
			}
		}
	}

	@Nested
	class SquareBracketMacro extends AbstractIntegrationTester implements MacroExpansionIntegrationTests.AllowedTests, EscapeMacroTester, WrapMacroTester, ExpandNonExistingMacroTester.ToNothing {
		@Nested
		class CharacterTests extends MacroRefCharacterTester {
			@Override
			public String var(String name) {
				return SquareBracketMacro.this.var(name);
			}

			@Nested class RightParenthesisCharacter extends CharTester implements CharExTester.AllowedTester {}
			@Nested class RightCurlyBracketCharacter extends CharTester implements CharExTester.AllowedTester {}
			@Nested class RightSquareBracketCharacter extends CharTester implements CharExTester.EarlyCloses {}
		}
	}

	interface WrapMacroTester extends BaseIntegrationTester {
		@Test
		default void treatsNoDollarSignMacroAsLiteral() {
			assertThat(expand(wrap("FOO")), into(wrap("FOO")));
		}

		@Test
		default void expandsNestedMacroInNoDollarSignMacro() {
			buildSettings().put("BAR", "bar");
			assertThat(expand(wrap("FOO_" + var("BAR") + "_FAR")), into(wrap("FOO_bar_FAR")));
		}

		default String wrap(String s) {
			return macro().wrap(s);
		}
	}

	interface EscapeMacroTester extends BaseIntegrationTester {
		@Test
		default void canEscapeEntireMacro() {
			assertThat(expand(escape(var("MY_MACRO"))), into(var("MY_MACRO")));
		}

		@Test
		default void canEscapeResolveNestedEscapedMacro() {
			buildSettings().put("MACRO", "macro");
			buildSettings().put("MY_macro", "illegal");
			buildSettings().put("MY_", "illegal");
			assertThat(expand(escape(var("MY_" + Simple.var("MACRO")))), into(var("MY_macro")));
		}

		@Test
		default void canEscapeNestedMacro() {
			buildSettings().put("MACRO", "macro");
			buildSettings().put("MY_macro", "illegal");
			buildSettings().put("MY_", "illegal");
			assertThat(expand(escape(var("MY_" + escape(Simple.var("MACRO"))))),
				into(var("MY_" + Simple.var("MACRO"))));
		}

		static String escape(String s) {
			assert s.startsWith("$");
			return "$" + s;
		}
	}

	enum XCMacro {
		Parenthesis {
			@Override
			public String wrap(String name) {
				return "(" + name + ")";
			}
		},
		SquareBracket {
			@Override
			public String wrap(String name) {
				return "[" + name + "]";
			}
		},
		CurlyBracket {
			@Override
			public String wrap(String name) {
				return "{" + name + "}";
			}
		},
		Simple {
			@Override
			public String wrap(String name) {
				return name;
			}
		}
		;

		public final String var(String name) {
			return "$" + wrap(name);
		}

		public abstract String wrap(String name);
	}

	@Nested
	class SimpleMacro extends AbstractIntegrationTester implements MacroExpansionIntegrationTests.DisallowedTests, EscapeMacroTester, ExpandNonExistingMacroTester.ToLiteral {
		@Nested
		class CharacterTests extends AbstractCharacterIntegrationTester {
			@Nested class NineCharacter extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class EightCharacter extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class SevenCharacter extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class SixCharacter extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class FiveCharacter extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class FourCharacter extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class ThreeCharacter extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class TwoCharacter extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class OneCharacter extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class ZeroCharacter extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class RightCurlyBracketCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class RightParenthesisCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class RightSquareBracketCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class SpaceCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class ColonCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class HyphenMinusCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class UnderscoreCharacter  extends CharTester implements CharExTester.AllowedTester {}
			@Nested class BacktickCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class QuestionMarkCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class TildeCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class FullStopCharacter  extends CharTester implements CharExTester.AtBeginningTester.Literal, CharExTester.AtMiddleTester.Allows, CharExTester.AtEndingTester.Allows {}
			@Nested class PlusSignCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class EqualsSignCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class SemicolonCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class BackslashCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class VerticalBarCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class SlashCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class LeftCurlyBracketCharacter  extends CharTester implements CharExTester.AtBeginningTester.Disallows, CharExTester.AtMiddleTester.Literal, CharExTester.AtEndingTester.Literal {}
			// This case is hard to pass on Nokee, so we will ignore for now, users should use this character because it cause Xcode to crash anyway
			@Disabled @Nested class LeftSquareBracketCharacter  extends CharTester implements CharExTester.CrashedTester {}
			@Nested class QuotationMarkCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class ApostropheCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class CommaCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class GreaterThanSignCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class LessThanSignCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class LeftParenthesisCharacter  extends CharTester implements CharExTester.AtBeginningTester.Disallows, CharExTester.AtMiddleTester.Literal, CharExTester.AtEndingTester.Literal {}
			@Nested class AsteriskCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class AmpersandCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class CaretCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class ExclamationMarkCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class AtSignCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class NumberSignCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class PercentSignCharacter  extends CharTester implements CharExTester.LiteralTester {}
			@Nested class DollarSignCharacter  extends CharTester implements CharExTester.AtMiddleTester.Literal, CharExTester.AtEndingTester.Literal, CharExTester.AtBeginningTester.GivenCharAtBeginning {
				@Test
				void undoubleCharacterAndTreatsAsLiteral() {
					assertThat(expand(var(nameThatStartsWith(ch()))), into(nameThatStartsWith(ch())));
				}
			}
		}
	}

	@Test
	void stopsParsingUponUnmatchedMacroWrapping() {
		assertAll(
			() -> assertThat(expand("before $(FOO_${BAR)} after"), into("before ")),
			() -> assertThat(expand("before $(FOO_$[BAR)] after"), into("before ")),
			() -> assertThat(expand("before ${FOO_$(BAR}) after"), into("before ")),
			() -> assertThat(expand("before ${FOO_$[BAR}] after"), into("before ")),
			() -> assertThat(expand("before $[FOO_$(BAR]) after"), into("before ")),
			() -> assertThat(expand("before $[FOO_${BAR]} after"), into("before "))
		);
	}

	public Map<String, String> buildSettings() {
		return buildSettings;
	}

	private static Map<String, String> buildSettingsWithCharacter(TestCharacter ch) {
		return ImmutableMap.of(nameThatEmbed(ch), illegalCharPayload(ch), nameThatStartsWith(ch), illegalCharPayload(ch), nameThatEndsWith(ch), illegalCharPayload(ch));
	}

	public abstract String expand(String str);
}
