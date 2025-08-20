package utils;

import java.io.FileWriter;
import java.io.IOException;

public class CSVUtils {
    public static void writeRow(FileWriter fw, String... cols) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(',');
            // naive escaping
            String c = cols[i] == null ? "" : cols[i];
            if (c.contains(",") || c.contains("\"") || c.contains("\n")) {
                c = "\"" + c.replace("\"", "\"\"") + "\"";
            }
            sb.append(c);
        }
        sb.append('\n');
        fw.append(sb.toString());
    }

    public static void closeQuietly(FileWriter fw) {
        if (fw != null) {
            try { fw.flush(); fw.close(); } catch (Exception ignored) {}
        }
    }
}
