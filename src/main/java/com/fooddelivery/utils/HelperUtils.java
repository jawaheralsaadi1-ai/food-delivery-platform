package com.fooddelivery.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;
import java.util.Random;

public class HelperUtils {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static String generateCode(String prefix) {
        int number = 1000 + RANDOM.nextInt(8080);
        return prefix + "-" + number;
    }

    public static String generateCode(String prefix, int length) {
        if (length <= 0) throw new IllegalArgumentException("length must be greater than 0");
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        int number = min + RANDOM.nextInt(max - min + 1);
        return prefix + "-" + number;
    }

    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS_KM = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static double calculateTotal(double subtotal, double fee) {
        return subtotal + fee;
    }

    public static double calculateTotal(double subtotal, double fee, double discount) {
        return Math.max(subtotal + fee - discount, 0.0);
    }

    public static String formatCurrency(double amount) {
        return String.format("%.2f", amount);
    }

    public static String formatCurrency(double amount, String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode.toUpperCase(Locale.ROOT));
            return currency.getSymbol() + " " + String.format("%.2f", amount);
        } catch (IllegalArgumentException e) {
            return currencyCode + " " + String.format("%.2f", amount);
        }
    }

    public static boolean isBusinessOpen(String openTime, String closeTime) {
        LocalTime now   = LocalTime.now();
        LocalTime open  = LocalTime.parse(openTime,  TIME_FORMATTER);
        LocalTime close = LocalTime.parse(closeTime, TIME_FORMATTER);
        if (!open.isAfter(close)) {
            return !now.isBefore(open) && !now.isAfter(close);
        } else {
            return !now.isBefore(open) || !now.isAfter(close);
        }
    }
}