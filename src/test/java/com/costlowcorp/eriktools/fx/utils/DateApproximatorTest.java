/*
 * Copyright Erik Costlow.
 * Not authorized for use or view by others.
 */
package com.costlowcorp.eriktools.fx.utils;

import com.costlowcorp.fx.utils.DateApproximator;
import java.time.LocalDateTime;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Erik Costlow
 */
public class DateApproximatorTest {
    
    @Test
    public void testApprox(){
        final LocalDateTime a = LocalDateTime.of(2000, 12, 1, 0, 0);
        final LocalDateTime b = LocalDateTime.of(2016, 12, 21, 0, 0);
        
        final String between = DateApproximator.between(a, b);
        System.out.println(between);
        assertEquals("16 years, and 2 weeks", between);
    }
}
