package dev.nokee.model.testers;

public final class SampleProviders<T> {
	private final SampleProvider<T> p0;
	private final SampleProvider<T> p1;
	private final SampleProvider<T> p2;


	public SampleProviders(SampleProvider<T> p0, SampleProvider<T> p1, SampleProvider<T> p2) {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
	}

	public SampleProvider<T> p0() {
		return p0;
	}

	public SampleProvider<T> p1() {
		return p1;
	}

	public SampleProvider<T> p2() {
		return p2;
	}
}
