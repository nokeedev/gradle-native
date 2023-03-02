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

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;

import java.util.List;

@EqualsAndHashCode
public final class AntlrMacroExpansionParser implements XCStringParser {
	@Override
	public XCString parse(String s) {
		dev.nokee.xcode.MacroExpansionLexer lexer = new dev.nokee.xcode.MacroExpansionLexer(CharStreams.fromString(s));
		lexer.removeErrorListeners();

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		dev.nokee.xcode.MacroExpansionParser parser = new dev.nokee.xcode.MacroExpansionParser(tokens);
		parser.removeErrorListeners();

		return new BaseVisitor().visitParse(parser.parse());
	}

	private static final class BaseVisitor extends dev.nokee.xcode.MacroExpansionParserBaseVisitor<XCString> {
		@Override
		public XCString visitMacroList(dev.nokee.xcode.MacroExpansionParser.MacroListContext ctx) {
			return newInstance(new MacroListVisitor().visitChildren(ctx));
		}

		private static XCString newInstance(List<XCString> macroList) {
			if (macroList.isEmpty()) {
				return new XCStringEmpty();
			} else {
				return new XCStringList(macroList);
			}
		}

		private static XCString newInstance(String literal) {
			if (literal.isEmpty()) {
				return new XCStringEmpty();
			} else {
				return new XCStringLiteral(literal);
			}
		}

		@Override
		public XCString visitErrorNode(ErrorNode node) {
			return new XCStringEmpty();
		}

		private final class MacroListVisitor extends dev.nokee.xcode.MacroExpansionParserBaseVisitor<List<XCString>> {
			@Override
			public List<XCString> visitLiteral(dev.nokee.xcode.MacroExpansionParser.LiteralContext ctx) {
				return ImmutableList.of(newInstance(ctx.getText().replace("$$", "$")));
			}

			@Override
			public List<XCString> visitMacroName(dev.nokee.xcode.MacroExpansionParser.MacroNameContext ctx) {
				return ImmutableList.of(new XCStringVariable(ctx.getText()));
			}

			@Override
			public List<XCString> visitMacroRef(dev.nokee.xcode.MacroExpansionParser.MacroRefContext ctx) {
				return ImmutableList.of(new XCStringVariableOperator(BaseVisitor.this.visitMacroList(ctx.macroList()), new OperationListVisitor().visitChildren(ctx)));
			}

			@Override
			protected List<XCString> defaultResult() {
				return ImmutableList.of();
			}

			@Override
			protected List<XCString> aggregateResult(List<XCString> aggregate, List<XCString> nextResult) {
				return ImmutableList.<XCString>builder().addAll(aggregate).addAll(nextResult).build();
			}
		}

		private static final class OperationListVisitor extends dev.nokee.xcode.MacroExpansionParserBaseVisitor<List<XCStringVariableOperator.Operator>> {
			@Override
			public List<XCStringVariableOperator.Operator> visitRetrievalOperation(dev.nokee.xcode.MacroExpansionParser.RetrievalOperationContext ctx) {
				return ImmutableList.of(XCString.operator(ctx.getText()));
			}

			@Override
			public List<XCStringVariableOperator.Operator> visitReplacementOperation(dev.nokee.xcode.MacroExpansionParser.ReplacementOperationContext ctx) {
				return ImmutableList.of(XCString.operator(ctx.operator().getText(), ctx.argument().getText()));
			}

			@Override
			protected List<XCStringVariableOperator.Operator> defaultResult() {
				return ImmutableList.of();
			}

			@Override
			protected List<XCStringVariableOperator.Operator> aggregateResult(List<XCStringVariableOperator.Operator> aggregate, List<XCStringVariableOperator.Operator> nextResult) {
				return ImmutableList.<XCStringVariableOperator.Operator>builder().addAll(aggregate).addAll(nextResult).build();
			}
		}
	}
}
