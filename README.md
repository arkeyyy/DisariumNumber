# Disarium Number

This JavaFX desktop app finds every Disarium number from 1 through an inclusive upper limit. A number is Disarium when the sum of its digits raised to their positions, counted from the left starting at 1, equals the number itself. For example, 89 qualifies because 8¹ + 9² = 89.

The search defaults to an upper limit of 10,000 and four worker threads. Both inputs are positive integers. The thread count is limited to the smaller of 64 and the upper limit. Workers search contiguous ranges that can differ in size and report their progress. An active search supports cancellation.

Completed searches list the matching numbers in ascending order and report the counts of Disarium numbers, non-Disarium numbers, and all numbers checked. With an upper limit of 100 and four threads, the matches are 1, 2, 3, 4, 5, 6, 7, 8, 9, and 89. That gives 10 matches and 90 non-matches across 100 numbers checked.

The project uses Java 17 or newer, JavaFX 21, and Maven.
