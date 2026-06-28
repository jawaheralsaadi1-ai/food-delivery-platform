package com.fooddelivery.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class HelperUtils {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // Code generation (overloaded)

    public static String generateCode(String prefix) {
        return generateCode(prefix, 6);
    }

    public static String generateCode(String prefix, int length) {
        StringBuilder sb = new StringBuilder(prefix).append("-");
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    //  Distance (Haversine formula — km)

    public static double calculateDistance(double lat1, double lng1,
                                           double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    //  Total calculation (overloaded)

    public static double calculateTotal(double subtotal, double fee) {
        return subtotal + fee;
    }

    public static double calculateTotal(double subtotal, double fee, double discount) {
        return Math.max(0, subtotal + fee - discount);
    }

    //  Currency formatting (overloaded)

    public static String formatCurrency(double amount) {
        return formatCurrency(amount, "USD");
    }

    public static String formatCurrency(double amount, String currencyCode) {
        return String.format("%s %.2f", currencyCode, amount);
    }

    //  Business hours check

    public static boolean isBusinessOpen(String openTime, String closeTime) {
        try {
            LocalTime now   = LocalTime.now();
            LocalTime open  = LocalTime.parse(openTime,  TIME_FMT);
            LocalTime close = LocalTime.parse(closeTime, TIME_FMT);
            if (close.isAfter(open)) {
                return !now.isBefore(open) && !now.isAfter(close);
            } else {
                // overnight: e.g. 22:00 → 02:00
                return !now.isBefore(open) || !now.isAfter(close);
            }
        } catch (Exception e) {
            return false;
        }
    }
}