package com.paynestsystem.architecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the one rule of the architecture: dependencies point inward.
 *
 *   app (composition root)  -> may know everything
 *   service                 -> domain, payment contract
 *   domain                  -> payment contract (PaymentMethod / PaymentProcessor)
 *   payment                 -> util only (it knows amounts, never orders)
 *   util                    -> nothing
 *
 * Only {@code app} may name a concrete rail (CardPayment, EftPayment, ...).
 * That is what lets a new rail arrive as one new class plus wiring.
 * The checks read the source files, so a bad import fails the build.
 */
class DependencyRuleTest {

    private static final String ROOT = "com.paynestsystem.";
    private static final Path MAIN = Path.of(System.getProperty("basedir", "."), "src", "main", "java",
            "com", "paynestsystem");

    private static final Pattern IMPORT = Pattern.compile("^\\s*import\\s+(static\\s+)?([\\w.*]+)\\s*;", Pattern.MULTILINE);
    private static final Pattern IMPLEMENTS_RAIL = Pattern.compile("class\\s+(\\w+)\\s+implements\\s+PaymentMethod\\b");

    // ---- helpers -------------------------------------------------------

    private static List<Path> sourcesIn(String layer) throws IOException {
        Path dir = MAIN.resolve(layer);
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        }
    }

    static List<String> projectImports(String source) {
        List<String> found = new ArrayList<>();
        Matcher m = IMPORT.matcher(source);
        while (m.find()) {
            if (m.group(2).startsWith(ROOT)) {
                found.add(m.group(2));
            }
        }
        return found;
    }

    /** Returns "File: import" for every import of a forbidden layer. */
    private static List<String> violations(String layer, List<String> forbiddenLayers) throws IOException {
        List<String> bad = new ArrayList<>();
        for (Path file : sourcesIn(layer)) {
            for (String imp : projectImports(Files.readString(file))) {
                for (String forbidden : forbiddenLayers) {
                    if (imp.startsWith(ROOT + forbidden + ".")) {
                        bad.add(file.getFileName() + " imports " + imp);
                    }
                }
            }
        }
        return bad;
    }

    private static List<String> concreteRails() throws IOException {
        List<String> rails = new ArrayList<>();
        for (Path file : sourcesIn("payment")) {
            Matcher m = IMPLEMENTS_RAIL.matcher(Files.readString(file));
            while (m.find()) {
                rails.add(m.group(1));
            }
        }
        return rails;
    }

    // ---- the rules -----------------------------------------------------

    @Test
    void util_dependsOnNothingInTheProject() throws IOException {
        assertEquals(List.of(), violations("util", List.of("domain", "payment", "service", "app")));
    }

    @Test
    void payment_neverKnowsOrdersServicesOrTheApp() throws IOException {
        assertEquals(List.of(), violations("payment", List.of("domain", "service", "app")));
    }

    @Test
    void domain_neverKnowsServicesOrTheApp() throws IOException {
        assertEquals(List.of(), violations("domain", List.of("service", "app")));
    }

    @Test
    void service_neverKnowsTheApp() throws IOException {
        assertEquals(List.of(), violations("service", List.of("app")));
    }

    @Test
    void onlyTheCompositionRootNamesAConcreteRail() throws IOException {
        List<String> rails = concreteRails();
        assertTrue(rails.size() >= 3, "expected at least three concrete rails, found " + rails);

        List<String> bad = new ArrayList<>();
        for (String layer : List.of("domain", "service", "util")) {
            bad.addAll(mentions(layer, rails));
        }
        // Inside the payment package, only the rails themselves may use their own names.
        for (Path file : sourcesIn("payment")) {
            String name = file.getFileName().toString().replace(".java", "");
            if (!rails.contains(name)) {
                for (String rail : rails) {
                    if (Pattern.compile("\\b" + rail + "\\b").matcher(Files.readString(file)).find()) {
                        bad.add(file.getFileName() + " mentions " + rail);
                    }
                }
            }
        }
        assertEquals(List.of(), bad);
    }

    @Test
    void noWildcardImportsOfProjectPackages() throws IOException {
        List<String> bad = new ArrayList<>();
        for (String layer : List.of("domain", "payment", "service", "util", "app")) {
            for (Path file : sourcesIn(layer)) {
                for (String imp : projectImports(Files.readString(file))) {
                    if (imp.endsWith(".*")) {
                        bad.add(file.getFileName() + " imports " + imp);
                    }
                }
            }
        }
        assertEquals(List.of(), bad);
    }

    private static List<String> mentions(String layer, List<String> rails) throws IOException {
        List<String> bad = new ArrayList<>();
        for (Path file : sourcesIn(layer)) {
            String source = Files.readString(file);
            for (String rail : rails) {
                if (Pattern.compile("\\b" + rail + "\\b").matcher(source).find()) {
                    bad.add(file.getFileName() + " mentions " + rail);
                }
            }
        }
        return bad;
    }

    // ---- make sure the checks themselves are not vacuous ---------------

    @Test
    void everyLayerActuallyHasSources() throws IOException {
        for (String layer : List.of("domain", "payment", "service", "util", "app")) {
            assertFalse(sourcesIn(layer).isEmpty(), layer + " has no sources - is the path right?");
        }
    }

    @Test
    void importScanner_detectsProjectImportsAndIgnoresJdkOnes() {
        String source = """
                package com.paynestsystem.domain;
                import java.util.List;
                import com.paynestsystem.app.PayNestApplication;
                import static com.paynestsystem.util.Money.formatRand;
                import com.paynestsystem.payment.*;
                """;

        assertEquals(List.of("com.paynestsystem.app.PayNestApplication",
                        "com.paynestsystem.util.Money.formatRand",
                        "com.paynestsystem.payment.*"),
                projectImports(source));
    }
}
