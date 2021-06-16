package dev.nokee.model.streams;

/**
 * Represent a terminal operation on the stream pipeline.
 *
 * @param <E_IN>  type of elements to be accepted
 * @param <R>  type of the result
 */
interface TerminalOp<E_IN, R> {
	<P_IN> R evaluate(Pipeline<E_IN> h, Topic<P_IN> supplier);
}
