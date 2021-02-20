package dev.nokee.internal.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import groovy.lang.Closure;
import lombok.val;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.internal.Cast;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.google.common.base.Suppliers.ofInstance;
import static com.google.common.collect.Streams.zip;
import static dev.nokee.internal.testing.utils.ClosureTestUtils.adaptToClosure;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

public final class ExecuteWith {
	private ExecuteWith() {}

	public static <T> ExecutionResult<T> executeWith(ExecutionStrategy<T> strategy) {
		try {
			return strategy.execute();
		} catch (Throwable throwable) {
			return ExceptionUtils.rethrow(throwable);
		}
	}

	public interface ExecutionResult<T> {
		T getLastArgument();
		int getCalledCount();
	}

	public interface ExecutionStrategy<T> {
		ExecutionResult<T> execute() throws Throwable;
	}

	public static <T> ExecutionStrategy<T> action(ThrowingConsumer<? super Action<? super T>> execution) {
		return new ExecutionStrategy<T>() {
			@Override
			public ExecutionResult<T> execute() throws Throwable {
				Action<T> action = Cast.uncheckedCast(Mockito.mock(Action.class));
				ArgumentCaptor<T> captor = Cast.uncheckedCast(ArgumentCaptor.forClass(Object.class));
				Mockito.doNothing().when(action).execute(captor.capture());
				execution.accept(action);
				return new ActionExecutionResult<>(captor.getAllValues());
			}
		};
	}

	public interface ConsumerExecutionStrategy<T> extends ExecutionStrategy<T> {
		ConsumerExecutionStrategy<T> thenAnswer(Answer<Void> answer);
		ConsumerExecutionStrategy<T> thenThrow(Throwable throwable);
		ConsumerExecutionStrategy<T> captureUsing(ContextualCaptor<?> captor);
	}

	public static <T> ConsumerExecutionStrategy<T> consumer(ThrowingConsumer<? super Consumer<? super T>> execution) {
		return new ConsumerExecutionStrategy<T>() {
			private final List<ContextualCaptorAnswer<?>> contextCaptors = new ArrayList<>();
			private Answer<Void> answer = t -> null;

			@Override
			public ConsumerExecutionStrategy<T> thenAnswer(Answer<Void> answer) {
				this.answer = answer;
				return this;
			}

			@Override
			public ConsumerExecutionStrategy<T> thenThrow(Throwable throwable) {
				answer = t -> { throw throwable; };
				return this;
			}

			@Override
			public ConsumerExecutionStrategy<T> captureUsing(ContextualCaptor<?> captor) {
				contextCaptors.add((ContextualCaptorAnswer<?>) captor);
				return this;
			}

			@Override
			public ExecutionResult<T> execute() throws Throwable {
				Consumer<T> action = Cast.uncheckedCast(Mockito.mock(Consumer.class));
				ArgumentCaptor<T> captor = Cast.uncheckedCast(ArgumentCaptor.forClass(Object.class));
				Mockito.doAnswer(t -> {
					for (ContextualCaptorAnswer<?> contextCaptor : contextCaptors) {
						contextCaptor.answer(t);
					}
					return answer.answer(t);
				}).when(action).accept(captor.capture());
				execution.accept(action);
				return new ActionExecutionResult<>(captor.getAllValues());
			}
		};
	}

	public static final class BiArguments<T, U> {
		private final T first;
		private final U second;

		private BiArguments(T first, U second) {
			this.first = first;
			this.second = second;
		}

		public T getFirst() {
			return first;
		}

		public U getSecond() {
			return second;
		}
	}

	public interface BiConsumerExecutionStrategy<T, U> extends ExecutionStrategy<BiArguments<T, U>> {
		BiConsumerExecutionStrategy<T, U> thenAnswer(Answer<Void> answer);
		BiConsumerExecutionStrategy<T, U> thenThrow(Throwable throwable);
		BiConsumerExecutionStrategy<T, U> captureUsing(ContextualCaptor<?> captor);
	}

	public static <T, U> BiConsumerExecutionStrategy<T, U> biConsumer(ThrowingConsumer<? super BiConsumer<? super T, ? super U>> execution) {
		return new BiConsumerExecutionStrategy<T, U>() {
			private final List<ContextualCaptorAnswer<?>> contextCaptors = new ArrayList<>();
			private Answer<Void> answer = t -> null;

			@Override
			public BiConsumerExecutionStrategy<T, U> thenAnswer(Answer<Void> answer) {
				this.answer = answer;
				return this;
			}

			@Override
			public BiConsumerExecutionStrategy<T, U> thenThrow(Throwable throwable) {
				answer = t -> { throw throwable; };
				return this;
			}

			@Override
			public BiConsumerExecutionStrategy<T, U> captureUsing(ContextualCaptor<?> captor) {
				contextCaptors.add((ContextualCaptorAnswer<?>) captor);
				return this;
			}

			@Override
			public ExecutionResult<BiArguments<T, U>> execute() throws Throwable {
				BiConsumer<T, U> action = Cast.uncheckedCast(Mockito.mock(BiConsumer.class));
				ArgumentCaptor<T> captorT = Cast.uncheckedCast(ArgumentCaptor.forClass(Object.class));
				ArgumentCaptor<U> captorU = Cast.uncheckedCast(ArgumentCaptor.forClass(Object.class));
				Mockito.doAnswer(t -> {
					for (ContextualCaptorAnswer<?> contextCaptor : contextCaptors) {
						contextCaptor.answer(t);
					}
					return answer.answer(t);
				}).when(action).accept(captorT.capture(), captorU.capture());
				execution.accept(action);
				return new ActionExecutionResult<>(zip(captorT.getAllValues().stream(), captorU.getAllValues().stream(), BiArguments::new).collect(toList()));
			}
		};
	}

	public interface SupplierExecutionStrategy<R> extends ExecutionStrategy<Void> {
		SupplierExecutionStrategy<R> thenReturn(R value);
		SupplierExecutionStrategy<R> thenAnswer(Answer<R> answer);
		SupplierExecutionStrategy<R> thenThrow(Throwable throwable);
		SupplierExecutionStrategy<R> captureUsing(ContextualCaptor<?> captor);
	}

	public static <T, R> SupplierExecutionStrategy<R> supplier(ThrowingConsumer<? super Supplier<? extends R>> execution) {
		return new SupplierExecutionStrategy<R>() {
			private final List<ContextualCaptorAnswer<?>> contextCaptors = new ArrayList<>();
			private Answer<R> answer = t -> null;

			@Override
			public SupplierExecutionStrategy<R> thenReturn(R value) {
				answer = t -> value;
				return this;
			}

			@Override
			public SupplierExecutionStrategy<R> thenAnswer(Answer<R> answer) {
				this.answer = answer;
				return this;
			}

			@Override
			public SupplierExecutionStrategy<R> thenThrow(Throwable throwable) {
				answer = t -> { throw throwable; };
				return this;
			}

			@Override
			public SupplierExecutionStrategy<R> captureUsing(ContextualCaptor<?> captor) {
				contextCaptors.add((ContextualCaptorAnswer<?>) captor);
				return this;
			}

			@Override
			public ExecutionResult<Void> execute() throws Throwable {
				Supplier<R> action = Cast.uncheckedCast(Mockito.mock(Supplier.class));
				MutableInt calledCount = new MutableInt(0);
				Mockito.when(action.get()).thenAnswer(t -> {
					calledCount.increment();
					for (ContextualCaptorAnswer<?> contextCaptor : contextCaptors) {
						contextCaptor.answer(t);
					}
					return answer.answer(t);
				});
				execution.accept(action);
				return new ActionExecutionResult<>(calledCount.intValue());
			}
		};
	}

	public interface FunctionExecutionStrategy<T, R> extends ExecutionStrategy<T> {
		FunctionExecutionStrategy<T, R> thenReturn(R value);
		FunctionExecutionStrategy<T, R> thenAnswer(Answer<R> answer);
		FunctionExecutionStrategy<T, R> thenThrow(Throwable throwable);
		FunctionExecutionStrategy<T, R> captureUsing(ContextualCaptor<?> captor);
	}

	public static <T, R> FunctionExecutionStrategy<T, R> function(ThrowingConsumer<? super Function<? super T, ? extends R>> execution) {
		return new FunctionExecutionStrategy<T, R>() {
			private final List<ContextualCaptorAnswer<?>> contextCaptors = new ArrayList<>();
			private Answer<R> answer = t -> null;

			@Override
			public FunctionExecutionStrategy<T, R> thenReturn(R value) {
				answer = t -> value;
				return this;
			}

			@Override
			public FunctionExecutionStrategy<T, R> thenAnswer(Answer<R> answer) {
				this.answer = answer;
				return this;
			}

			@Override
			public FunctionExecutionStrategy<T, R> thenThrow(Throwable throwable) {
				answer = t -> { throw throwable; };
				return this;
			}

			@Override
			public FunctionExecutionStrategy<T, R> captureUsing(ContextualCaptor<?> captor) {
				contextCaptors.add((ContextualCaptorAnswer<?>) captor);
				return this;
			}

			@Override
			public ExecutionResult<T> execute() throws Throwable {
				Function<T, R> action = Cast.uncheckedCast(Mockito.mock(Function.class));
				ArgumentCaptor<T> captor = Cast.uncheckedCast(ArgumentCaptor.forClass(Object.class));
				Mockito.when(action.apply(captor.capture())).thenAnswer(t -> {
					for (ContextualCaptorAnswer<?> contextCaptor : contextCaptors) {
						contextCaptor.answer(t);
					}
					return answer.answer(t);
				});
				execution.accept(action);
				return new ActionExecutionResult<>(captor.getAllValues());
			}
		};
	}

	public interface TransformerExecutionStrategy<T, R> extends ExecutionStrategy<T> {
		TransformerExecutionStrategy<T, R> thenReturn(R value);
		TransformerExecutionStrategy<T, R> thenAnswer(Answer<R> answer);
		TransformerExecutionStrategy<T, R> thenThrow(Throwable throwable);
		TransformerExecutionStrategy<T, R> captureUsing(ContextualCaptor<?> captor);
	}

	public static <T, R> TransformerExecutionStrategy<T, R> transformer(ThrowingConsumer<? super Transformer<? extends R, ? super T>> execution) {
		return new TransformerExecutionStrategy<T, R>() {
			private final List<ContextualCaptorAnswer<?>> contextCaptors = new ArrayList<>();
			private Answer<R> answer = t -> null;

			@Override
			public TransformerExecutionStrategy<T, R> thenReturn(R value) {
				answer = t -> value;
				return this;
			}

			@Override
			public TransformerExecutionStrategy<T, R> thenAnswer(Answer<R> answer) {
				this.answer = answer;
				return this;
			}

			@Override
			public TransformerExecutionStrategy<T, R> thenThrow(Throwable throwable) {
				answer = t -> { throw throwable; };
				return this;
			}

			@Override
			public TransformerExecutionStrategy<T, R> captureUsing(ContextualCaptor<?> captor) {
				contextCaptors.add((ContextualCaptorAnswer<?>) captor);
				return this;
			}

			@Override
			public ExecutionResult<T> execute() throws Throwable {
				Transformer<R, T> action = Cast.uncheckedCast(Mockito.mock(Transformer.class));
				ArgumentCaptor<T> captor = Cast.uncheckedCast(ArgumentCaptor.forClass(Object.class));
				Mockito.when(action.transform(captor.capture())).thenAnswer(t -> {
					for (ContextualCaptorAnswer<?> contextCaptor : contextCaptors) {
						contextCaptor.answer(t);
					}
					return answer.answer(t);
				});
				execution.accept(action);
				return new ActionExecutionResult<>(captor.getAllValues());
			}
		};
	}

	public interface RunnableExecutionStrategy extends ExecutionStrategy<Void> {
		RunnableExecutionStrategy thenAnswer(Answer<Void> answer);
		RunnableExecutionStrategy thenThrow(Throwable throwable);
		RunnableExecutionStrategy captureUsing(ContextualCaptor<?> captor);
	}

	public static RunnableExecutionStrategy runnable(ThrowingConsumer<? super Runnable> execution) {
		return new RunnableExecutionStrategy() {
			private final List<ContextualCaptorAnswer<?>> contextCaptors = new ArrayList<>();
			private Answer<Void> answer = t -> null;

			@Override
			public RunnableExecutionStrategy thenAnswer(Answer<Void> answer) {
				this.answer = answer;
				return this;
			}

			@Override
			public RunnableExecutionStrategy thenThrow(Throwable throwable) {
				answer = t -> { throw throwable; };
				return this;
			}

			@Override
			public RunnableExecutionStrategy captureUsing(ContextualCaptor<?> captor) {
				contextCaptors.add((ContextualCaptorAnswer<?>) captor);
				return this;
			}

			@Override
			public ExecutionResult<Void> execute() throws Throwable {
				val action = Mockito.mock(Runnable.class);
				val callCount = new MutableInt();
				Mockito.doAnswer(t -> {
					callCount.increment();
					for (ContextualCaptorAnswer<?> contextCaptor : contextCaptors) {
						contextCaptor.answer(t);
					}
					return answer.answer(t);
				}).when(action).run();
				execution.accept(action);
				return new ActionExecutionResult<>(Stream.<Void>generate(ofInstance(null)).limit(callCount.intValue()).collect(toList()));
			}
		};
	}

	private static class ActionExecutionResult<T> implements ExecutionResult<T> {
		private final List<T> arguments;
		private final int calledCount;

		private ActionExecutionResult(List<T> arguments) {
			this.arguments = arguments;
			this.calledCount = arguments.size();
		}

		private ActionExecutionResult(int calledCount) {
			this.arguments = ImmutableList.of();
			this.calledCount = calledCount;
		}

		public T getLastArgument() {
			return Iterables.getLast(arguments);
		}

		public int getCalledCount() {
			return calledCount;
		}
	}

	public static <T> ExecutionStrategy<T> closure(ThrowingConsumer<? super Closure<Void>> execution) {
		return new ExecutionStrategy<T>() {
			@Override
			public ExecutionResult<T> execute() throws Throwable {
				Action<T> action = Cast.uncheckedCast(Mockito.mock(Action.class));
				ArgumentCaptor<T> captor = Cast.uncheckedCast(ArgumentCaptor.forClass(Object.class));
				Mockito.doNothing().when(action).execute(captor.capture());
				execution.accept(adaptToClosure(action::execute));
				return new ActionExecutionResult<>(captor.getAllValues());
			}
		};
	}

	public static <T> Matcher<ExecutionResult<T>> calledOnceWith(Matcher<T> matcher) {
		return allOf(called(equalTo(1)), lastArgument(matcher));
	}

	public static <T, U> Matcher<BiArguments<T, U>> firstArgumentOf(Matcher<T> matcher) {
		return new FeatureMatcher<BiArguments<T, U>, T>(matcher, "", "") {
			@Override
			protected T featureValueOf(BiArguments<T, U> actual) {
				return actual.getFirst();
			}
		};
	}

	public static <T, U> Matcher<BiArguments<T, U>> secondArgumentOf(Matcher<U> matcher) {
		return new FeatureMatcher<BiArguments<T, U>, U>(matcher, "", "") {
			@Override
			protected U featureValueOf(BiArguments<T, U> actual) {
				return actual.getSecond();
			}
		};
	}

	public static <T> Matcher<ExecutionResult<T>> called(Matcher<Integer> matcher) {
		return new FeatureMatcher<ExecutionResult<T>, Integer>(matcher, "", "") {
			@Override
			protected Integer featureValueOf(ExecutionResult<T> actual) {
				return actual.getCalledCount();
			}
		};
	}

	public static <T> Matcher<ExecutionResult<T>> calledOnce() {
		return called(equalTo(1));
	}

	public static <T> Matcher<ExecutionResult<T>> neverCalled() {
		return called(equalTo(0));
	}

	public static <T> Matcher<ExecutionResult<T>> lastArgument(Matcher<T> matcher) {
		return new FeatureMatcher<ExecutionResult<T>, T>(matcher, "", "") {
			@Override
			protected T featureValueOf(ExecutionResult<T> actual) {
				return actual.getLastArgument();
			}
		};
	}

	public static <T> ContextualCaptor<T> contextualCapture(Supplier<T> captor) {
		return new ContextualCaptorAnswer<>(captor);
	}

	public interface ContextualCaptor<T> {
		T getLastValue();
	}

	private static final class ContextualCaptorAnswer<T> implements ContextualCaptor<T>, Answer<Void> {
		private final Supplier<T> captor;
		private final List<T> values = new ArrayList<>();

		private ContextualCaptorAnswer(Supplier<T> captor) {
			this.captor = captor;
		}

		@Override
		public T getLastValue() {
			return Iterables.getLast(values);
		}

		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			values.add(captor.get());
			return null;
		}
	}
}
