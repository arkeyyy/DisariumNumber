package org.example.disarium_number;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DisariumSearchTest {
    @Test
    void sampleHasTenMatchesAndNinetyNonMatches() {
        List<Integer> matches = IntStream.rangeClosed(1, 100)
                .filter(DisariumSearch::isDisarium).boxed().toList();
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 89), matches);
        assertEquals(90, 100 - matches.size());
    }

    @Test
    void digitPositionsStartAtOneOnTheLeft() {
        assertTrue(DisariumSearch.isDisarium(135));
        assertTrue(DisariumSearch.isDisarium(518));
        assertTrue(DisariumSearch.isDisarium(2427));
        assertFalse(DisariumSearch.isDisarium(0));
        assertFalse(DisariumSearch.isDisarium(10));
        assertFalse(DisariumSearch.isDisarium(Integer.MAX_VALUE));
        assertFalse(DisariumSearch.isDisarium(1_999_999_999));
    }

    @Test
    void partitionsMatchTheFourThreadSample() {
        assertEquals(List.of(new DisariumSearch.Range(1, 11), new DisariumSearch.Range(12, 31),
                        new DisariumSearch.Range(32, 61), new DisariumSearch.Range(62, 100)),
                DisariumSearch.ranges(100, 4));
    }

    @Test
    void partitionsCoverTheWholeIntervalAndDifferInSizeWheneverPossible() {
        for (int num : new int[]{1, 2, 4, 10, 11, 13, 14, 16, 63, 64, 65, 127, 1_000, 2_080, Integer.MAX_VALUE}) {
            for (int cnt = 1; cnt <= Math.min(num, 64); cnt++) {
                List<DisariumSearch.Range> ranges = DisariumSearch.ranges(num, cnt);
                assertEquals(cnt, ranges.size());
                long next = 1;
                for (DisariumSearch.Range range : ranges) {
                    assertEquals(next, range.start());
                    assertTrue(range.size() >= 1);
                    assertEquals((long) range.end() - range.start() + 1, range.size());
                    next = (long) range.end() + 1;
                }
                assertEquals((long) num + 1, next);
                long distinctSizes = ranges.stream().map(DisariumSearch.Range::size).distinct().count();
                if (num >= cnt * (cnt + 1) / 2) {
                    assertEquals(cnt, distinctSizes);
                } else if (num > cnt) {
                    assertTrue(distinctSizes > 1);
                }
            }
        }
    }

    @Test
    void inputLimitsIncludeTheLargestIntegerAndSixtyFourWorkers() {
        assertArrayEquals(new int[]{Integer.MAX_VALUE, 64}, DisariumSearch.parseInputs("2147483647", "64"));
        assertArrayEquals(new int[]{1, 1}, DisariumSearch.parseInputs(" 1 ", "1"));
        for (String invalid : List.of("", "0", "-1", "1.5", "abc", "2147483648")) {
            assertThrows(IllegalArgumentException.class, () -> DisariumSearch.parseInputs(invalid, "1"));
            assertThrows(IllegalArgumentException.class, () -> DisariumSearch.parseInputs("100", invalid));
        }
        assertThrows(IllegalArgumentException.class, () -> DisariumSearch.parseInputs("100", "65"));
        assertThrows(IllegalArgumentException.class, () -> DisariumSearch.parseInputs("3", "4"));
    }
}
