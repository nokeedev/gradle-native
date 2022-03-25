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
package dev.gradleplugins.buildscript.syntax;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static dev.gradleplugins.buildscript.syntax.Syntax.gradleProperty;
import static dev.gradleplugins.buildscript.syntax.Syntax.invoke;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.mapOf;
import static dev.gradleplugins.buildscript.syntax.Syntax.property;
import static dev.gradleplugins.buildscript.syntax.Syntax.setOf;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;
import static dev.gradleplugins.buildscript.syntax.Syntax.val;
import static dev.gradleplugins.buildscript.syntax.Syntax.var;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KotlinDslSyntaxTest {
	private final KotlinSyntax syntax = new KotlinSyntax();

	@Nested
	class VariableTest {
		@Test
		void rendersValVariableWithValue() {
			assertThat(syntax.render(val("myProp").assign(string("value"))),
				equalTo("val myProp = \"value\""));
		}

		@Test
		void rendersValVariableWithTypeAndValue() {
			assertThat(syntax.render(val("myProp").ofType("String").assign(string("myString"))),
				equalTo("val myProp: String = 'myString'"));
		}

		@Test
		void throwsExceptionWhenRenderValVariableWithoutValue() {
			assertThrows(UnsupportedOperationException.class, () -> syntax.render(val("myProp")));
		}

		@Test
		void rendersVarVariableWithValue() {
			assertThat(syntax.render(var("myProp").assign(string("value"))),
				equalTo("var myProp = 'value'"));
		}

		@Test
		void rendersVarVariableWithoutValue() {
			assertThat(syntax.render(var("myProp")),
				equalTo("var myProp"));
		}

		@Test
		void rendersVarVariableWithSpecificTypeWithoutValue() {
			assertThat(syntax.render(var("myProp").ofType("String")),
				equalTo("var myProp: String"));
		}

		@Test
		void rendersVarVariableWithSpecificTypeWithValue() {
			assertThat(syntax.render(var("myProp").ofType("CharSequence").assign(string("myString"))),
				equalTo("var myProp: CharSequence = 'myString'"));
		}
	}

	@Nested
	class PropertyAssignmentTest {
		@Test
		void rendersPlainPropertyAssignment() {
			assertThat(syntax.render(property(literal("myProp")).assign(string("some-value"))),
				equalTo("myProp = \"some-value\""));
		}

		@Test
		void rendersBooleanPropertyAssignment() {
			assertThat(syntax.render(property(literal("myProp")).ofBooleanType().assign(literal("true"))),
				equalTo("isMyProp = true"));
		}

		@Test
		void rendersChainedPropertyAssignment() {
			assertThat(syntax.render(property(literal("some.path")).property("myProp").ofBooleanType().assign(literal("true"))),
				equalTo("some.path.isMyProp = true"));
		}

		@Test
		void rendersGradlePropertyAssignment() {
			assertThat(syntax.render(gradleProperty(literal("myProp")).assign(string("some-value"))),
				equalTo("myProp.set(\"some-value\")"));
		}

		@Test
		void rendersUndecoratedGradlePropertyAssignment() {
			assertThat(syntax.render(gradleProperty(literal("myProp")).undecorated().assign(string("some-value"))),
				equalTo("myProp.set(\"some-value\")"));
		}
	}

	@Nested
	class MethodInvocationTest {
		@Test
		void rendersNoArgsMethodInvocation() {
			assertThat(syntax.render(invoke(literal("myMethod"))),
				equalTo("myMethod()"));
		}

		@Test
		void rendersSingleStringArgMethodInvocation() {
			assertThat(syntax.render(invoke(literal("myMethod"), string("foo"))),
				equalTo("myMethod(\"foo\")"));
		}

		@Test
		void rendersSingleMapArgMethodInvocation() {
			assertThat(syntax.render(invoke(literal("apply"), mapOf("plugin", string("com.example")))),
				equalTo("apply(plugin = \"com.example\")"));
		}

		@Test
		void rendersSingleArgMethodInvocationWithParenthesis() {
			assertThat(syntax.render(invoke(literal("Class.forName"), string("com.example.TestPlugin")).alwaysUseParenthesis()),
				equalTo("Class.forName(\"com.example.TestPlugin\")"));
		}

		@Test
		void rendersMultipleStringArgsMethodInvocation() {
			assertThat(syntax.render(invoke(literal("myMethod"), string("foo"), string("bar"))),
				equalTo("myMethod(\"foo\", \"bar\")"));
		}
	}

	@Nested
	class GradleDslLiteralTest {
		@Test
		void rendersKotlinDslLiteralVariant() {
			assertThat(syntax.render(new GradleDslLiteralValue(
					new GroovyDslLiteralValue("def myString = \"${prop}\""),
					new KotlinDslLiteralValue("val myString = \"$prop\""))
				), startsWith("val myString = "));
		}
	}

	@Nested
	class GroovyDslLiteralTest {
		@Test
		void throwsExceptionWhenRenderingGroovyDslLiteralInKotlin() {
			assertThrows(UnsupportedOperationException.class,
				() -> syntax.render(new GroovyDslLiteralValue("def myString = \"${prop}\"")));
		}
	}

	@Nested
	class KotlinDslLiteralKotlinDslTest {
		@Test
		void rendersKotlinDslAsIs() {
			assertThat(syntax.render(new KotlinDslLiteralValue("val myString = \"$prop\"")),
				equalTo("val myString = \"$prop\""));
		}
	}

	@Nested
	class LiteralTest {
		@Test
		void rendersEmptyLiteralAsEmptyString() {
			assertThat(syntax.render(literal("")), equalTo(""));
		}

		@Test
		void rendersLiteralAsIs() {
			assertThat(syntax.render(literal("some value")), equalTo("some value"));
		}
	}

	@Nested
	class MapLiteralTest {
		@Test
		void rendersEmptyMapLiteral() {
			assertThat(syntax.render(mapOf(emptyMap())), equalTo("emptyMap()"));
		}

		@Test
		void rendersMapLiteralWithOnlyOneEntry() {
			assertThat(syntax.render(mapOf(singletonMap("key", string("value")))), equalTo("mapOf(key to \"value\")"));
		}

		@Test
		void rendersMapLiteralWithMultipleEntries() {
			assertThat(syntax.render(mapOf(new LinkedHashMap<String, Expression>() {{
				put("k1", string("v1"));
				put("k2", string("v2"));
				put("k3", string("v3"));
			}})), equalTo("mapOf(k1 to \"v1\", k2 to \"v2\", k3 to \"v3\")"));
		}
	}

	@Nested
	class SetLiteralTest {
		@Test
		void rendersEmptySetLiteral() {
			assertThat(syntax.render(setOf()), equalTo("emptySet()"));
		}

		@Test
		void rendersSetLiteralWithOnlyOneEntry() {
			assertThat(syntax.render(setOf(string("value"))), equalTo("setOf(\"value\")"));
		}

		@Test
		void rendersSetLiteralWithMultipleEntries() {
			assertThat(syntax.render(setOf(string("v1"), string("v2"), string("v3"))),
				equalTo("setOf(\"v1\", \"v2\", \"v3\")"));
		}
	}

	@Nested
	class StringLiteralTest {
		@Test
		void rendersEmptyStringLiteral() {
			assertThat(syntax.render(string("")), equalTo("\"\""));
		}

		@Test
		void rendersSingleWordStringLiteral() {
			assertThat(syntax.render(string("word")), equalTo("\"word\""));
		}

		@Test
		void rendersMultiWordStringLiteral() {
			assertThat(syntax.render(string("multi word")), equalTo("\"multi word\""));
		}

		@Test
		void doesNotEscapeSingleQuoteInsideStringLiteral() {
			assertThat(syntax.render(string("string with 'single' quote")),
				equalTo("\"string with 'single' quote\""));
		}

		@Test
		void escapesDoubleQuoteInsideStringLiteral() {
			assertThat(syntax.render(string("string with \"double\" quote")),
				equalTo("\"string with \\\"double\\\" quote\""));
		}
	}
}
