package com.lyft.kronos;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ClockFactoryTest {

    @Mock
    private Clock localClock;

    @Mock
    private SyncResponseCache syncResponseCache;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateKronosClockFromJavakReturnsNonNull() {
        Assert.assertNotNull(ClockFactory.createKronosClock(localClock, syncResponseCache));
    }
}
