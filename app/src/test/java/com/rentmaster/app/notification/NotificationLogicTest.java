package com.rentmaster.app.notification;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.Calendar;

public class NotificationLogicTest {

    @Test
    public void testGetAdjustedTime() {
        // Base time: 2026-01-01 12:00:00
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JANUARY, 1, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long baseTime = cal.getTimeInMillis();

        // Test 1: 2 days before (Offset -2)
        long resultMinus2 = NotificationHelper.getAdjustedTime(baseTime, -2, 9);
        Calendar expectedMinus2 = Calendar.getInstance();
        expectedMinus2.setTimeInMillis(baseTime);
        expectedMinus2.add(Calendar.DAY_OF_YEAR, -2);
        expectedMinus2.set(Calendar.HOUR_OF_DAY, 9);
        expectedMinus2.set(Calendar.MINUTE, 0);
        expectedMinus2.set(Calendar.SECOND, 0);
        expectedMinus2.set(Calendar.MILLISECOND, 0);
        
        assertEquals("Should be 2 days before at 9 AM", expectedMinus2.getTimeInMillis(), resultMinus2);

        // Test 2: On due date (Offset 0)
        long result0 = NotificationHelper.getAdjustedTime(baseTime, 0, 9);
        Calendar expected0 = Calendar.getInstance();
        expected0.setTimeInMillis(baseTime);
        expected0.set(Calendar.HOUR_OF_DAY, 9);
        expected0.set(Calendar.MINUTE, 0);
        expected0.set(Calendar.SECOND, 0);
        expected0.set(Calendar.MILLISECOND, 0);
        
        assertEquals("Should be same day at 9 AM", expected0.getTimeInMillis(), result0);

        // Test 3: 2 days after (Offset 2) - This is the key change
        long resultPlus2 = NotificationHelper.getAdjustedTime(baseTime, 2, 9);
        Calendar expectedPlus2 = Calendar.getInstance();
        expectedPlus2.setTimeInMillis(baseTime);
        expectedPlus2.add(Calendar.DAY_OF_YEAR, 2);
        expectedPlus2.set(Calendar.HOUR_OF_DAY, 9);
        expectedPlus2.set(Calendar.MINUTE, 0);
        expectedPlus2.set(Calendar.SECOND, 0);
        expectedPlus2.set(Calendar.MILLISECOND, 0);
        
        assertEquals("Should be 2 days after at 9 AM", expectedPlus2.getTimeInMillis(), resultPlus2);
    }
}
