package ganglia.gmetric;

import static org.junit.Assert.assertEquals;
import ganglia.gmetric.GMetric.UDPAddressingMode;

import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class GMetricIT {

    private GMetric instance = null;

    @Before
    public void setUp() {
        instance = new GMetric("localhost", 8649, UDPAddressingMode.MULTICAST, 300, true);
    }

    /**
     * Test of announce method, type String
     */
    @Test
    public void announceString() throws Exception {
        System.out.println("announceString");
        String name = "TEST1";
        String value = "BOING";
        GMetricType type = GMetricType.STRING;
        String units = "UNITS";
        GMetricSlope slope = GMetricSlope.BOTH;
        int tmax = 60;
        int dmax = 0;
        instance.announce(name, value, type, units, slope, tmax, dmax, "STRINGGROUP");
        GMetricResult.GMetricDetail readValue = GMetricResult.getGMetric(name);
        assertEquals(value, readValue.value);
        assertEquals(type.getGangliaType(), readValue.type);
        assertEquals(units, readValue.units);
        assertEquals( slope.name().toLowerCase(), readValue.slope);
    }
    /**
     * Test of announce method, type int
     */
    @Test
    public void announceInt() throws Exception {
        System.out.println("announceInt");
        String name = "TESTINT";
        int value = 334567 ;
        instance.announce(name, value, "INTGROUP" );
        GMetricResult.GMetricDetail readValue = GMetricResult.getGMetric(name);
        assertEquals(value, Integer.valueOf(readValue.value));
        assertEquals(GMetricType.INT32.getGangliaType(), readValue.type);
    }
    /**
     * Test of announce method, type long
     */
    @Test
    public void announceLong() throws Exception {
        System.out.println("announceLong");
        String name = "TESTLONG";
        long value = 334567 ;
        instance.announce(name, value, "LONGGROUP" );
        GMetricResult.GMetricDetail readValue = GMetricResult.getGMetric(name);
        assertEquals(value, Long.valueOf(readValue.value));
        assertEquals(GMetricType.DOUBLE.getGangliaType(), readValue.type);
    }
    /**
     * Test of announce method, type float
     */
    @Test
    public void announceFloat() throws Exception {
        System.out.println("announceFloat");
        String name = "TESTFLOAT";
        float value = 334567.543f ;
        instance.announce(name, value, "FLOATGROUP" );
        GMetricResult.GMetricDetail readValue = GMetricResult.getGMetric(name);
        assertEquals(value, Float.valueOf(readValue.value));
        assertEquals(GMetricType.FLOAT.getGangliaType(), readValue.type);
    }
    /**
     * Test of announce method, type double
     */
    @Test
    public void announceDouble() throws Exception {
        System.out.println("announceDouble");
        String name = "TESTDOUBLE";
        double value = 334567.54355555 ;
        instance.announce(name, value, "DOUBLEGROUP" );
        GMetricResult.GMetricDetail readValue = GMetricResult.getGMetric(name);
        assertEquals(value, Double.valueOf(readValue.value));
        assertEquals(GMetricType.DOUBLE.getGangliaType(), readValue.type);
    }
}
