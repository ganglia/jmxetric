package jmxetric;

/**
 * MXBean definition used for testing
 * @author humphrej
 *
 */
public interface TestExampleMXBean {
	
	final int INT_VALUE=1234 ;
	final long LONG_VALUE=43210L ;
	final double DOUBLE_VALUE=123.45D ;
	final float FLOAT_VALUE=543.21F ;
	final String STRING_VALUE="STRING" ;

	int getInt() ;
	long getLong() ;
	String getString() ;
	double getDouble() ;
	float getFloat() ;
	TestComposite getComposite();
}

