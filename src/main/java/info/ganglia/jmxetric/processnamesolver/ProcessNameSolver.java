package info.ganglia.jmxetric.processnamesolver;

/**
 * Each process must have a name to report stats to ganglia. The name could
 * be calculated using several strategies. All those strategies implement
 * this interface.
 */
public interface ProcessNameSolver {

    /**
     * Solve the process name.
     *
     * @return a String or null of the solver cannot calculate a name with the
     * implemented strategy.
     */
    String solve();
}
