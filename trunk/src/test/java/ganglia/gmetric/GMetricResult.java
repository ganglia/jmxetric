package ganglia.gmetric;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GMetricResult {

    
    final private static int buffSize = 1024 * 50;

    public static  GMetricDetail getGMetric(String metricName) throws Exception {
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
        in.close();
        gangliaXMLSocket.close();
        
        return handler.getDetail() ;
    }

    private static class MyXMLHandler extends DefaultHandler {

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
    
    public static class GMetricDetail {
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
