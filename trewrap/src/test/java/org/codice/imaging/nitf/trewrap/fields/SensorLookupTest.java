/*
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 */
package org.codice.imaging.nitf.trewrap.fields;

import java.util.Arrays;
import static org.hamcrest.core.Is.is;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

/**
 * Tests for sensor-dependent field lookup
 */
public class SensorLookupTest {

    TestLogger LOGGER = TestLoggerFactory.getTestLogger(SensorLookup.class);

    public SensorLookupTest() {
    }

    @Test
    public void checkBadResourcePath() {
        assertThat(LOGGER.getLoggingEvents().isEmpty(), is(true));
        SensorLookup lookup = new SensorLookup(SensorLookup.class.getResourceAsStream("/bad path"));
        assertThat(LOGGER.getLoggingEvents(), is(Arrays.asList(LoggingEvent.warn("Problem parsing XML for null:null. javax.xml.stream.XMLStreamException: java.net.MalformedURLException"))));
    }

    @Test
    public void checkLookupJSTARS() {
        SensorLookup lookup = new SensorLookup(SensorLookup.class.getResourceAsStream("/ACFTB_SCENE_SOURCE_sensor.xml"));
        assertEquals("Manual", lookup.lookupDescription("JSE8CA", "1"));
        assertEquals("Manual", lookup.lookupDescription("JSE8CA", "1", "Pre-planned"));
    }

    @Before
    public void clearLoggers() {
        TestLoggerFactory.clear();
    }
}
