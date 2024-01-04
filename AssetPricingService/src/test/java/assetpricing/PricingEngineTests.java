package assetpricing;

import assetpricingregistry.*;
import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class PricingEngineTests {
    private final Random random = new Random();
    private final long thirtyDays = 1000L * 60 * 60 * 24 * 30;
    private AssetPricingRepository repository;
    private Time time;
    private Session session;
    private PricingEngine engine;
    private String assetId;
    private String newPricingCode;
    private long newStart;
    private long newEnd;
    private HashSet<PriceScheduleRecord> previousSchedules;
    private boolean accepted;
    private long now;

    @BeforeEach
    void setUp() {
        repository = mock(AssetPricingRepository.class);
        time = mock(Time.class);
        engine = new PricingEngine(repository, time);

        session = mock(Session.class);
        assetId = UUID.randomUUID().toString();
        newPricingCode = UUID.randomUUID().toString();
        newStart = random.nextLong();
        newEnd = newStart + 1000 + random.nextInt(10);
        previousSchedules = new HashSet<>();
        when(repository.getPriceTable(session, assetId)).thenReturn(previousSchedules);
        now = random.nextLong();
        when(time.getCurrentTimeInMilliseconds()).thenReturn(now);
    }

    @Test
    public void pricingCodeAppliedWhenNoneAlreadyExistsNoLock() {
        processRequest(newPricingCode, "DO_NOT_LOCK", newStart, newEnd);

        thePricingSchedulesShouldBe(
                schedule(newPricingCode, false, newStart, newEnd));
    }

    @Test
    public void noStartTimeDefaultsToNowPlusThirtyDays() {
        processRequest(newPricingCode, "DO_NOT_LOCK", null, null);

        thePricingSchedulesShouldBe(
                schedule(newPricingCode, false, now, now + thirtyDays));
    }

    @Test
    public void noStartTimeIgnoresEndTime() {
        processRequest(newPricingCode, "DO_NOT_LOCK", null, newEnd);

        thePricingSchedulesShouldBe(
                schedule(newPricingCode, false, now, now + thirtyDays));
    }

    @Test
    public void noEndTimeDefaultsToNowPlusThirtyDays() {
        processRequest(newPricingCode, "DO_NOT_LOCK", newStart, null);

        thePricingSchedulesShouldBe(
                schedule(newPricingCode, false, newStart, newStart + thirtyDays));
    }

    @Test
    public void pricingCodeAppliedWhenNoneAlreadyExistsLock() {
        processRequest(newPricingCode, "LOCK", newStart, newEnd);

        thePricingSchedulesShouldBe(
                schedule(newPricingCode, true, newStart, newEnd));
    }

    @Test
    public void doesNotImpactNonOverlappingScheduleAfterEnd() {
        final var preExistingSchedule = schedule(UUID.randomUUID().toString(), false, newEnd, newEnd + 1);
        preExisting(preExistingSchedule);
        processRequest(newPricingCode, "LOCK", newStart, newEnd);

        thePricingSchedulesShouldBe(
                preExistingSchedule,
                schedule(newPricingCode, true, newStart, newEnd));
    }

    @Test
    public void currentlyActiveScheduleIsLockedByDefault() {
        lockingDefaultScenario(now - 1, 2, true);
    }

    @Test
    public void defaultLockingWindowStartsExactlyAtCurrentTime() {
        lockingDefaultScenario(now, 1, true);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void scheduleInThePastIsNotLockedByDefault(int width) {
        lockingDefaultScenario(now - 1, width, false);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    public void scheduleInTheFutureIsNotLockedByDefault(int width) {
        lockingDefaultScenario(now + 1, width, false);
    }

    private void lockingDefaultScenario(long start, int windowWidth, boolean shouldBeLocked) {
        processRequest(newPricingCode, null, start, start + windowWidth);

        thePricingSchedulesShouldBe(schedule(newPricingCode, shouldBeLocked, start, start + windowWidth));
    }

    @Test
    public void doesNotImpactNonOverlappingScheduleBeforeStart() {
        final var preExistingSchedule = schedule(UUID.randomUUID().toString(), false, newStart - 1, newStart);
        preExisting(preExistingSchedule);
        processRequest(newPricingCode, "LOCK", newStart, newEnd);

        thePricingSchedulesShouldBe(
                preExistingSchedule,
                schedule(newPricingCode, true, newStart, newEnd));
    }

    @ParameterizedTest
    @MethodSource("offsets")
    public void wipesOutExistingPriceSchedulesWithOverlappingTimeframes(int startOffset, int endOffset) {
        preExisting(schedule(UUID.randomUUID().toString(), false, newStart + startOffset, newEnd + endOffset));

        processRequest(newPricingCode, "UNLOCK", newStart, newEnd);

        thePricingSchedulesShouldBe(schedule(newPricingCode, false, newStart, newEnd));
        theRequestShouldHaveBeenAccepted();
    }

    @ParameterizedTest
    @MethodSource("offsets")
    public void rejectsChangesThatWouldImpactLockedSchedules(int startOffset, int endOffset) {
        preExisting(schedule(UUID.randomUUID().toString(), true, newStart + startOffset, newEnd + endOffset));

        processRequest(newPricingCode, "UNLOCK", newStart, newEnd);

        noChangesShouldBeMade();
        theRequestShouldHaveBeenRejected();
    }

    private void theRequestShouldHaveBeenRejected() {
        assertThat(accepted, equalTo(false));
    }

    private void theRequestShouldHaveBeenAccepted() {
        assertThat(accepted, equalTo(true));
    }

    private void noChangesShouldBeMade() {
        verify(repository, never()).setPriceSchedules(any(), any(), any());
    }

    private void preExisting(PriceScheduleRecord schedule) {
        previousSchedules.add(schedule);
    }

    private void processRequest(String pricingCode, String lockPolicy, Long start, Long end) {
        final var pricingRequest = new AssetPricingUpdateDescriptor();
        pricingRequest.setAssetId(assetId);
        pricingRequest.setPricingCode(pricingCode);
        pricingRequest.setLockPolicy(lockPolicy);
        pricingRequest.setStart(start);
        pricingRequest.setEnd(end);

        accepted = engine.process(session, pricingRequest);
    }

    private void thePricingSchedulesShouldBe(PriceScheduleRecord... expected) {
        final ArgumentCaptor<Collection<PriceScheduleRecord>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(repository).setPriceSchedules(same(session), eq(assetId), captor.capture());
        final var actual = captor.getValue().toArray(new PriceScheduleRecord[0]);
        assertThat(actual.length, equalTo(expected.length));

        for (var i = 0; i < expected.length; ++i) {
            final var expectedSchedule = expected[i];
            final var actualSchedule = actual[i];

            assertThat(actualSchedule.getPricingCode(), equalTo(expectedSchedule.getPricingCode()));
            assertThat(actualSchedule.getIsLocked(), equalTo(expectedSchedule.getIsLocked()));
            assertThat(actualSchedule.getStartDate(), equalTo(expectedSchedule.getStartDate()));
            assertThat(actualSchedule.getEndDate(), equalTo(expectedSchedule.getEndDate()));
        }
    }

    private PriceScheduleRecord schedule(String pricingCode, boolean locked, long start, long end) {
        final var result = new PriceScheduleRecord();
        result.setPricingCode(pricingCode);
        result.setIsLocked(locked);
        result.setStartDate(start);
        result.setEndDate(end);
        return result;
    }

    public static Object[][] offsets() {
        return new Object[][]{
                {-1, -1},
                {-1, 0},
                {-1, 1},
                {0, -1},
                {0, 0},
                {0, 1},
                {1, -1},
                {1, 0},
                {1, 1},
        };
    }
}
