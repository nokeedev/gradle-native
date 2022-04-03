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
package dev.nokee.xcode;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

final class AntlrParserIterator implements Iterator<AntlrParserIterator.AntlrEvent> {
	private final Deque<AntlrChildIterator> contexts = new ArrayDeque<>();
	private AntlrEvent next = null;

	public AntlrParserIterator(ParseTree t) {
		next = AntlrEvent.enter(t);
		contexts.push(new AntlrChildIterator(t));
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public AntlrEvent next() {
		final AntlrEvent result = next;
		next = findNext();
		return result;
	}

	@Nullable
	private AntlrEvent findNext() {
		ParseTree result = null;
		AntlrChildIterator iterator = contexts.peek();
		while (iterator != null) {
			while (iterator.hasNext()) {
				result = iterator.next();
				contexts.push(new AntlrChildIterator(result));
				return AntlrEvent.enter(result);
			}
			contexts.pop();
			return AntlrEvent.exit(iterator.parent);
//			iterator = contexts.peek();
		}
		return null;
	}

	private static final class AntlrChildIterator implements Iterator<ParseTree> {
		private int i = 0;
		private final ParseTree parent;

		private AntlrChildIterator(ParseTree parent) {
			this.parent = parent;
		}

		@Override
		public boolean hasNext() {
			return i < parent.getChildCount();
		}

		@Override
		public ParseTree next() {
			return parent.getChild(i++);
		}
	}

	public static final class AntlrEvent {
		private final boolean entering;
		private final ParseTree node;

		private AntlrEvent(boolean entering, ParseTree node) {
			this.entering = entering;
			this.node = node;
		}

		public static AntlrEvent enter(ParseTree node) {
			return new AntlrEvent(true, node);
		}

		public static AntlrEvent exit(ParseTree node) {
			return new AntlrEvent(false, node);
		}

		public boolean isRuleNode() {
			return node instanceof RuleNode;
		}

		public boolean isTerminalNode() {
			return node instanceof TerminalNode;
		}

		public ParseTree getNode() {
			return node;
		}

		public boolean isEntering() {
			return entering;
		}
	}
}
