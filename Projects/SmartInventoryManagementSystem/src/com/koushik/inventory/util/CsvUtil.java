package com.koushik.inventory.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Utility functions for RFC 4180 compliant CSV formatting and escaping.
 */
public final class CsvUtil {

    private CsvUtil() {
    }

    /**
     * Escapes a CSV field according to RFC 4180.
     * If the field contains comma, quote, or line breaks, it is enclosed in double quotes
     * and any internal quotes are doubled.
     *
     * @param field the string value to escape
     * @return the escaped CSV field
     */
    public static String escape(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains("\"") || field.contains(",") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Formats a row of fields into a comma-separated CSV line ending with CRLF.
     *
     * @param fields the list of fields in the row
     * @return CSV formatted row string
     */
    public static String toCsvRow(List<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return "\r\n";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(fields.get(i)));
        }
        sb.append("\r\n");
        return sb.toString();
    }

    /**
     * Formats an array of fields into a comma-separated CSV line ending with CRLF.
     *
     * @param fields the varargs fields in the row
     * @return CSV formatted row string
     */
    public static String toCsvRow(String... fields) {
        return toCsvRow(List.of(fields));
    }

    /**
     * Formats a monetary BigDecimal value to 2 decimal places.
     *
     * @param amount the monetary amount
     * @return 2-decimal plain string representation
     */
    public static String formatMonetary(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Formats a double price to 2 decimal places using BigDecimal.
     *
     * @param amount the price as double
     * @return 2-decimal plain string representation
     */
    public static String formatMonetary(double amount) {
        if (!Double.isFinite(amount)) {
            return "0.00";
        }
        return formatMonetary(BigDecimal.valueOf(amount));
    }
}
