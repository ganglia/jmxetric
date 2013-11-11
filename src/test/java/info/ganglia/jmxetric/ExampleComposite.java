package info.ganglia.jmxetric;

import java.beans.ConstructorProperties;
import java.util.Date;

/**
 * Class used to test composite attribute of an MXBean
 * @author humphrej
 *
 */
public class ExampleComposite {
	
	public static final Date DATE_VALUE=new Date(1000);
	public static final int INT_VALUE=29111974;
	public static final String STRING_VALUE="WIBBLE";
	
    private final Date date; 
    private final int integer; 
    private final String name; 
     
    @ConstructorProperties({"date", "integer", "name"}) 
    public ExampleComposite(Date date, int integer, String name) { 
        this.date = date; 
        this.integer = integer; 
        this.name = name; 
    } 
     
    public Date getDate() { 
        return date; 
    } 
     
    public int getInteger() { 
        return integer; 
    } 
     
    public String getName() { 
        return name; 
    }
}	




