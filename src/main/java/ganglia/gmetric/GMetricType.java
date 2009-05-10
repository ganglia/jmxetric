/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ganglia.gmetric;

/**
 *
 * @author humphrej
 */
public enum GMetricType {
    //string|int8|uint8|int16|uint16|int32|uint32|float|double
    STRING("string"),
    INT8("int8"),
    UINT8("uint8"),
    INT16("int16"),
    UINT16("uint16"),
    INT32("int32"),
    UINT32("uint32"),
    FLOAT("float"),
    DOUBLE("double");
            
    private String gangliaType ;
    
    GMetricType( String gangliaType ) {
        this.gangliaType = gangliaType ;
    }
    
    public String getGangliaType() {
        return gangliaType ;
    }
    
}
