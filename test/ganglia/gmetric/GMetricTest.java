package ganglia.gmetric;

import static org.junit.Assert.assertEquals;
import ganglia.gmetric.GMetric.UDPAddressingMode;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 */
public class GMetricTest {

    GMetric instance = null;

    public GMetricTest() {
    }

    @Before
    public void setUp() {
        instance = new GMetric("localhost", 8649, UDPAddressingMode.MULTICAST, true);
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
        GMetricDetail readValue = getGMetric(name);
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
        GMetricDetail readValue = getGMetric(name);
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
        GMetricDetail readValue = getGMetric(name);
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
        GMetricDetail readValue = getGMetric(name);
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
        GMetricDetail readValue = getGMetric(name);
        assertEquals(value, Double.valueOf(readValue.value));
        assertEquals(GMetricType.DOUBLE.getGangliaType(), readValue.type);
    }
    
    final private static int buffSize = 1024 * 50;

    private  GMetricDetail getGMetric(String metricName) throws Exception {
        // Open the socket, get the XML
        Socket gangliaXMLSocket = new Socket("localhost", 8649);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                gangliaXMLSocket.getInputStream()));
        char[] charBuff = new char[buffSize];
        int in_buff = in.read(charBuff, 0, buffSize);

        System.out.println("in_buff = " + in_buff);
        System.out.println("charBuff length: " + charBuff.length);

        if (in_buff != -1) {
            System.out.println("End of file");
        }

        //System.out.println(charBuff);

        // Parse XML
        CharArrayReader car = new CharArrayReader(charBuff, 0, in_buff); // these two lines have to be here.
        BufferedReader br_car = new BufferedReader(car);

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(true);

        XMLReader xmlReader = null;
        SAXParser saxParser = spf.newSAXParser();
        xmlReader = saxParser.getXMLReader();

        // Set the ContentHandler of the XMLReader
        MyXMLHandler handler = new MyXMLHandler( metricName );
        xmlReader.setContentHandler(handler);

        // Set an ErrorHandler before parsing
        // xmlReader.setErrorHandler(new MyErrorHandler(System.err));

        xmlReader.parse(new InputSource(br_car));
        return handler.getDetail() ;
    }

    class MyXMLHandler extends DefaultHandler {

        public void startElement(String uri,
                String localName,
                String qname,
                Attributes attributes) {
            //System.out.println(qname);
            if ( !qname.equals("METRIC"))
                return ;
            String name = attributes.getValue("","NAME");
            if ( name.equals(metricName)) {
                gmetricDetail = new GMetricDetail() ;
                gmetricDetail.metricName = metricName ;
                gmetricDetail.value = attributes.getValue("","VAL");
                gmetricDetail.type = attributes.getValue("","TYPE");
                gmetricDetail.units = attributes.getValue("","UNITS");
                gmetricDetail.dmax = attributes.getValue("","DMAX");
                gmetricDetail.tmax = attributes.getValue("","TMAX");
                gmetricDetail.slope = attributes.getValue("","SLOPE");
                System.out.println(gmetricDetail);
           }

        // process start of element
        }

        public void endElement(String uri,
                String localName,
                String qname) {
        // process end of element
        }

        public void characters(char[] ch,
                int start,
                int length) {
        // process characters
        }

        private String metricName = null ;
        private GMetricDetail gmetricDetail = null ;

        public MyXMLHandler( String metricName )
                throws org.xml.sax.SAXException {
            super();
            this.metricName = metricName ;
        }

        public GMetricDetail getDetail() {
            return gmetricDetail ;
        }
    }
    
    class GMetricDetail {
        public String metricName = null ;
        public String type = null ;
        public String units = null ;
        public String value = null ;
        public String dmax = null ;
        public String tmax = null ;
        public String slope = null ;
        
        public String toString() {
            StringBuilder b = new StringBuilder() ;
            b.append( metricName );
            b.append("/");
            b.append( type );
            b.append("/");
            b.append( value );
            b.append("/");
            b.append( units );
            b.append("/");
            b.append( dmax );
            b.append("/");
            b.append( tmax );
            b.append("/");
            b.append( slope );
            b.append("/");
            return b.toString() ;
        }
    }
}
