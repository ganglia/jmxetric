/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ganglia.gmetric;

/**
 *
 * @author humphrej
 */
public enum GMetricSlope {
    //zero|positive|negative|both
    ZERO(0),
    POSITIVE(0),
    NEGATIVE(0),
    BOTH(3);
    
    private int gangliaSlope ;
    
    GMetricSlope( int gangliaSlope ) {
        this.gangliaSlope = gangliaSlope ;
    }

    public int getGangliaSlope() {
        return gangliaSlope ;
    }
}
