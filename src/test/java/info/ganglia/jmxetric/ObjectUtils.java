package info.ganglia.jmxetric;

public class ObjectUtils {
	
	public static Object subtractValues(Object minuend, Object subtrahend) {
		if(minuend instanceof Integer && subtrahend instanceof Integer) {
			return (Integer)minuend - (Integer) subtrahend;
		}
		if(minuend instanceof Long && subtrahend instanceof Long) {
			return (Long)minuend - (Long) subtrahend;
		}
		if(minuend instanceof Float && subtrahend instanceof Float) {
			return (Float)minuend - (Float) subtrahend;
		}
		if(minuend instanceof Double && subtrahend instanceof Double) {
			return (Double)minuend - (Double) subtrahend;
		}
		return minuend;
	}

	public static Object divideValueByTime(Object dividend, Long time) {
		Object quotient = dividend;
		if(dividend instanceof Float) {
			quotient = (Float)dividend/time;
		}
		if(dividend instanceof Double) {
			quotient = (Double)dividend/time;
		}
		if(dividend instanceof Long) {
			quotient = (Long)dividend/time;
		}
		if (dividend instanceof Integer) {
			quotient = (Integer)dividend/time;
		}
		return quotient;
	}

}
