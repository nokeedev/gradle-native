package dev.nokee.model.streams;

abstract class Pipeline<P_OUT> {
	abstract <P_IN> Sink<P_IN> wrapSink(Sink<P_OUT> sink);

	abstract <P_IN, S extends Sink<P_OUT>> S wrapAndCopyInto(S sink, Source<P_IN> spliterator);

	abstract <P_IN> void copyInto(Sink<P_IN> wrappedSink, Source<P_IN> spliterator);
}
