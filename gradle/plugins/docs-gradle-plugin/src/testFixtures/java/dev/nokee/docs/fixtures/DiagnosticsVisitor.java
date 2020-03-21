package dev.nokee.docs.fixtures;

public interface DiagnosticsVisitor {
    DiagnosticsVisitor node(String message);

    DiagnosticsVisitor startChildren();

    DiagnosticsVisitor endChildren();
}
