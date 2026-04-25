package com.backend.core.language;

/**
 * Step 05: Text Blocks & Modern String Handling (L7 Mastery)
 *
 * CONCEPT:
 * Text blocks (""") are multi-line string literals with smart indentation handling.
 * Combined with String.formatted(), they replace verbose concatenation and StringBuilder.
 *
 * FOR C/C++ ENGINEERS:
 * C has no multi-line strings. C++ has R"(...)" raw strings but no indentation control.
 * Python has triple-quoted strings. Java text blocks add automatic stripIndent() and
 * trailing whitespace control via '\s' escape.
 *
 * L7 AWARENESS:
 * 1. INCIDENTAL WHITESPACE: The compiler strips common leading whitespace.
 * 2. TRAILING NEWLINE: A text block always ends with a newline unless you add \
 * 3. STRING TEMPLATES: Java 21 preview — interpolation without format strings.
 * 4. PERFORMANCE: Text blocks compile to the same constant pool entries as regular strings.
 */
public class Step05_TextBlocks {

    // ─── 1. Basic Text Block vs Old Style ───────────────────────────────────
    public static void compareStyles() {
        // OLD (Java 8): Painful string concatenation
        String oldJson = "{\n" +
                "  \"name\": \"L7 Engineer\",\n" +
                "  \"level\": 7,\n" +
                "  \"skills\": [\"Java\", \"C++\", \"Systems\"]\n" +
                "}";

        // NEW (Java 15+): Text blocks
        String newJson = """
                {
                  "name": "L7 Engineer",
                  "level": 7,
                  "skills": ["Java", "C++", "Systems"]
                }""";

        System.out.println("Old style JSON:\n" + oldJson);
        System.out.println("Text block JSON:\n" + newJson);
        System.out.println("Identical content: " + oldJson.equals(newJson));
    }

    // ─── 2. Indentation Control ─────────────────────────────────────────────
    public static void indentationRules() {
        // The closing """ position determines the "margin".
        // Everything to the right of it is preserved.

        // Closing """ at column 0 → no stripping
        String noStrip = """
name: L7
level: 7
""";

        // Closing """ indented → strips that much from each line
        String stripped = """
                name: L7
                level: 7
                """;

        System.out.println("No strip (leading spaces preserved):");
        System.out.println("'" + noStrip + "'");
        System.out.println("Stripped (clean output):");
        System.out.println("'" + stripped + "'");
    }

    // ─── 3. SQL Queries (real-world use case) ───────────────────────────────
    public static String buildQuery(String table, int limit) {
        return """
                SELECT id, name, created_at
                FROM %s
                WHERE active = true
                ORDER BY created_at DESC
                LIMIT %d
                """.formatted(table, limit);
    }

    // ─── 4. HTML/Template Generation ────────────────────────────────────────
    public static String generateHtml(String title, String content) {
        return """
                <!DOCTYPE html>
                <html>
                <head><title>%s</title></head>
                <body>
                  <h1>%s</h1>
                  <p>%s</p>
                </body>
                </html>
                """.formatted(title, title, content);
    }

    // ─── 5. Escape sequences in text blocks ─────────────────────────────────
    public static void escapeDemo() {
        // \s = explicit space (prevents trailing whitespace stripping)
        // \  = line continuation (no newline)
        String poem = """
                Roses are red,\s\s\s
                Violets are blue,\
                 Java 21 is great,\
                 and so are you.""";
        System.out.println("Escape demo:\n" + poem);
    }

    public static void main(String[] args) {
        System.out.println("=== Step 05: Text Blocks & Modern String Handling ===");

        compareStyles();
        System.out.println();

        indentationRules();

        System.out.println("SQL Query:\n" + buildQuery("users", 10));

        System.out.println("HTML:\n" + generateHtml("L7 Dashboard", "Welcome to Java 21"));

        escapeDemo();
    }
}
