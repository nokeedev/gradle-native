package dev.nokee.model.internal.core;

// TODO: See ModelPredicate for improvements around this interface
public interface ModelSpec extends ModelPredicate {
	boolean isSatisfiedBy(ModelNode node);
}
