package com.rentmaster.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class FormatUtils {
    // Australian locale for consistent formatting
    private static final Locale AU_LOCALE = Locale.forLanguageTag("en-AU");

    // Date formatters
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", AU_LOCALE);
    private static final SimpleDateFormat DATE_WITH_MONTH_FORMAT = new SimpleDateFormat("dd MMM yyyy", AU_LOCALE);

    // Australian phone number patterns
    // Mobile: 04XXXXXXXX
    // Landline: (0X) XXXX XXXX or 0X XXXX XXXX
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^04\\d{2}\\s?\\d{3}\\s?\\d{3}$");
    private static final Pattern LANDLINE_PATTERN = Pattern.compile("^\\(?0[2-8]\\)?\\s?\\d{4}\\s?\\d{4}$");
    private static final Pattern SIMPLE_PHONE_PATTERN = Pattern.compile("^\\d{10}$");

    /**
     * Format a currency amount in AUD
     * 
     * @param amount The amount to format
     * @return Formatted string like "$123.45"
     */
    public static String formatCurrency(double amount) {
        return String.format(AU_LOCALE, "$%.2f", amount);
    }

    /**
     * Format a timestamp as DD/MM/YYYY
     * 
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string
     */
    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format a timestamp as DD MMM YYYY (e.g., "22 Jan 2026")
     * 
     * @param timestamp Unix timestamp in milliseconds
     * @return Formatted date string
     */
    public static String formatDateWithMonth(long timestamp) {
        return DATE_WITH_MONTH_FORMAT.format(new Date(timestamp));
    }

    /**
     * Validate phone number (International Support)
     * Accepts any number with 8 to 15 digits to support both Australian and
     * International (e.g. Indian) numbers.
     * 
     * @param phone The phone number to validate
     * @return true if valid phone number
     */
    public static boolean isValidAustralianPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        // Remove spaces, dashes, parentheses, and plus sign
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)\\+]", "");

        // Basic validation: Check if it has between 8 and 15 digits
        // This covers Australian mobile (04...), landline (02...), and
        // International/Indian (98..., 91...)
        return cleanPhone.matches("^\\d{8,15}$");
    }

    /**
     * Get a user-friendly error message for invalid phone numbers
     * 
     * @return Error message string
     */
    public static String getPhoneValidationError() {
        return "Please enter a valid phone number (8-15 digits).";
    }
}
