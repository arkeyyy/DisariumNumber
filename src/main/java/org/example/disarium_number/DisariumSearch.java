package org.example.disarium_number;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

final class DisariumSearch {
    private static final int MAX_THREADS = 64;
    private static final long[][] DIGIT_POWERS = digitPowers();

    private DisariumSearch() {
    }

    record Range(int start, int end) {
        int size() {
            return end - start + 1;
        }
    }

    static int[] parseInputs(String num, String cnt) {
        int limit = positiveInteger(num, "Upper limit");
        int threads = positiveInteger(cnt, "Thread count");
        checkThreadCount(limit, threads);
        return new int[]{limit, threads};
    }

    private static int positiveInteger(String text, String label) {
        try {
            int value = Integer.parseInt(text.strip());
            if (value > 0) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // Values outside the integer range share the same validation message.
        }
        throw new IllegalArgumentException(label + " must be a whole number between 1 and 2147483647.");
    }

    private static void checkThreadCount(int limit, int threads) {
        int maximum = Math.min(limit, MAX_THREADS);
        if (threads < 1 || threads > maximum) {
            throw new IllegalArgumentException("Thread count must be between 1 and " + maximum + ".");
        }
    }

    static List<Range> ranges(int num, int cnt) {
        if (num < 1) {
            throw new IllegalArgumentException("Upper limit must be positive.");
        }
        checkThreadCount(num, cnt);

        int totalWeight = cnt * (cnt + 1) / 2;
        // Near the minimum total for distinct sizes, weights alone guarantee sizes of at least 1 through cnt.
        int baseSize = num >= totalWeight && num < totalWeight + cnt ? 0 : 1;
        int remaining = num - baseSize * cnt;
        int[] sizes = new int[cnt];
        long[] remainders = new long[cnt];
        Integer[] byRemainder = new Integer[cnt];
        int unassigned = remaining;
        for (int i = 0; i < cnt; i++) {
            long share = (long) remaining * (i + 1);
            int whole = (int) (share / totalWeight);
            sizes[i] = whole + baseSize;
            remainders[i] = share % totalWeight;
            byRemainder[i] = i;
            unassigned -= whole;
        }
        Arrays.sort(byRemainder, Comparator.<Integer>comparingLong(i -> remainders[i]).reversed()
                .thenComparing(Comparator.reverseOrder()));
        for (int i = 0; i < unassigned; i++) {
            sizes[byRemainder[i]]++;
        }
        if (cnt > 1 && sizes[0] > 1 && sizes[0] == sizes[cnt - 1]) {
            sizes[0]--;
            sizes[cnt - 1]++;
        }

        List<Range> ranges = new ArrayList<>(cnt);
        int end = 0;
        for (int size : sizes) {
            int start = end + 1;
            end += size;
            ranges.add(new Range(start, end));
        }
        return ranges;
    }

    static boolean isDisarium(int number) {
        if (number < 1) {
            return false;
        }
        int position = 1;
        for (int highDigits = number; highDigits >= 10; highDigits /= 10) {
            position++;
        }
        long sum = 0;
        for (int digits = number; digits > 0; digits /= 10) {
            sum += DIGIT_POWERS[position--][digits % 10];
            if (sum > number) {
                return false;
            }
        }
        return sum == number;
    }

    private static long[][] digitPowers() {
        long[][] powers = new long[11][10];
        Arrays.fill(powers[0], 1L);
        for (int position = 1; position < powers.length; position++) {
            for (int digit = 0; digit <= 9; digit++) {
                powers[position][digit] = powers[position - 1][digit] * digit;
            }
        }
        return powers;
    }
}
