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

class GroovyDslSyntaxTest {
	private final GroovySyntax syntax = new GroovySyntax();

	@Nested
	class VariableTest {
		@Test
		void rendersValVariableWithValue() {
			assertThat(syntax.render(val("myProp").assign(string("value"))),
				equalTo("final def myProp = 'value'"));
		}

		@Test
		void rendersValVariableWithTypeAndValue() {
			assertThat(syntax.render(val("myProp").ofType("String").assign(string("myString"))),
				equalTo("final String myProp = 'myString'"));
		}

		@Test
		void throwsExceptionWhenRenderValVariableWithoutValue() {
			assertThrows(UnsupportedOperationException.class, () -> syntax.render(val("myProp")));
		}

		@Test
		void rendersVarVariableWithValue() {
			assertThat(syntax.render(var("myProp").assign(string("value"))),
				equalTo("def myProp = 'value'"));
		}

		@Test
		void rendersVarVariableWithoutValue() {
			assertThat(syntax.render(var("myProp")),
				equalTo("def myProp"));
		}

		@Test
		void rendersVarVariableWithSpecificTypeWithoutValue() {
			assertThat(syntax.render(var("myProp").ofType("String")),
				equalTo("String myProp"));
		}

		@Test
		void rendersVarVariableWithSpecificTypeWithValue() {
			assertThat(syntax.render(var("myProp").ofType("CharSequence").assign(string("myString"))),
				equalTo("CharSequence myProp = 'myString'"));
		}
	}

	@Nested
	class PropertyAssignmentTest {
		@Test
		void rendersPlainPropertyAssignment() {
			assertThat(syntax.render(property(literal("myProp")).assign(string("some-value"))),
				equalTo("myProp = 'some-value'"));
		}

		@Test
		void rendersBooleanPropertyAssignment() {
			assertThat(syntax.render(property(literal("myProp")).ofBooleanType().assign(literal("true"))),
				equalTo("myProp = true"));
		}

		@Test
		void rendersChainedPropertyAssignment() {
			assertThat(syntax.render(property(literal("some.path")).property("myProp").ofBooleanType().assign(literal("true"))),
				equalTo("some.path.myProp = true"));
		}

		@Test
		void rendersGradlePropertyAssignment() {
			assertThat(syntax.render(gradleProperty(literal("myProp")).assign(string("some-value"))),
				equalTo("myProp = 'some-value'"));
		}

		@Test
		void rendersUndecoratedGradlePropertyAssignment() {
			assertThat(syntax.render(gradleProperty(literal("myProp")).undecorated().assign(string("some-value"))),
				equalTo("myProp.set('some-value')"));
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
				equalTo("myMethod 'foo'"));
		}

		@Test
		void rendersSingleMapArgMethodInvocationWithoutParenthesis() {
			assertThat(syntax.render(invoke(literal("apply"), mapOf("plugin", string("com.example")))),
				equalTo("apply plugin: 'com.example'"));
		}

		@Test
		void rendersSingleArgMethodInvocationWithParenthesis() {
			assertThat(syntax.render(invoke(literal("Class.forName"), string("com.example.TestPlugin")).alwaysUseParenthesis()),
				equalTo("Class.forName('com.example.TestPlugin')"));
		}

		@Test
		void rendersMultipleStringArgsMethodInvocation() {
			assertThat(syntax.render(invoke(literal("myMethod"), string("foo"), string("bar"))),
				equalTo("myMethod('foo', 'bar')"));
		}
	}

	@Nested
	class GradleDslLiteralTest {
		@Test
		void rendersGroovyDslLiteralVariant() {
			assertThat(syntax.render(new GradleDslLiteralValue(
					new GroovyDslLiteralValue("def myString = \"${prop}\""),
					new KotlinDslLiteralValue("val myString = \"$prop\""))
				), startsWith("def myString = "));
		}
	}

	@Nested
	class GroovyDslLiteralTest {
		@Test
		void rendersGroovyDslAsIs() {
			assertThat(syntax.render(new GroovyDslLiteralValue("def myString = \"${prop}\"")),
				equalTo("def myString = \"${prop}\""));
		}
	}

	@Nested
	class KotlinDslLiteralTest {
		@Test
		void throwsExceptionWhenRenderingKotlinDslLiteralInGroovy() {
			assertThrows(UnsupportedOperationException.class,
				() -> syntax.render(new KotlinDslLiteralValue("val myString = \"$prop\"")));
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
			assertThat(syntax.render(mapOf(emptyMap())), equalTo("[:]"));
		}

		@Test
		void rendersMapLiteralWithOnlyOneEntry() {
			assertThat(syntax.render(mapOf(singletonMap("key", string("value")))), equalTo("[key: 'value']"));
		}

		@Test
		void rendersMapLiteralWithMultipleEntries() {
			assertThat(syntax.render(mapOf(new LinkedHashMap<String, Expression>() {{
				put("k1", string("v1"));
				put("k2", string("v2"));
				put("k3", string("v3"));
			}})), equalTo("[k1: 'v1', k2: 'v2', k3: 'v3']"));
		}
	}

	@Nested
	class SetLiteralTest {
		@Test
		void rendersEmptySetLiteral() {
			assertThat(syntax.render(setOf()), equalTo("[]"));
		}

		@Test
		void rendersSetLiteralWithOnlyOneEntry() {
			assertThat(syntax.render(setOf(string("value"))), equalTo("['value']"));
		}

		@Test
		void rendersSetLiteralWithMultipleEntries() {
			assertThat(syntax.render(setOf(string("v1"), string("v2"), string("v3"))),
				equalTo("['v1', 'v2', 'v3']"));
		}
	}

	@Nested
	class StringLiteralTest {
		@Test
		void rendersEmptyStringLiteral() {
			assertThat(syntax.render(string("")), equalTo("''"));
		}

		@Test
		void rendersSingleWordStringLiteral() {
			assertThat(syntax.render(string("word")), equalTo("'word'"));
		}

		@Test
		void rendersMultiWordStringLiteral() {
			assertThat(syntax.render(string("multi word")), equalTo("'multi word'"));
		}

		@Test
		void escapesSingleQuoteInsideStringLiteral() {
			assertThat(syntax.render(string("string with 'single' quote")),
				equalTo("'string with \\'single\\' quote'"));
		}

		@Test
		void doesNotEscapeDoubleQuoteInsideStringLiteral() {
			assertThat(syntax.render(string("string with \"double\" quote")),
				equalTo("'string with \"double\" quote'"));
		}
	}
}
