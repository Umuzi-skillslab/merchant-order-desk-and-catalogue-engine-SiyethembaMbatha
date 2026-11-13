package com.paynestsystem.util;

import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Test helper: keeps the checkout narrative out of the build log for tests
 * that only care about return values. Tests that assert on console output
 * use {@link ConsoleCapture} instead (the two nest safely).
 */
public class QuietConsole implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(QuietConsole.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        context.getStore(NS).put("original", System.out);
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        System.setOut((PrintStream) context.getStore(NS).remove("original"));
    }
}
