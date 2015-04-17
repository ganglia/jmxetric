package info.ganglia.jmxetric.processnamesolver;

/**
 * Process name will be the param sent.
 */
public class StringProcessNameSolver implements ProcessNameSolver {
    private final String param;

    public StringProcessNameSolver(String param) {
        this.param = param;
    }

    public String solve() {
        return param;
    }
}
