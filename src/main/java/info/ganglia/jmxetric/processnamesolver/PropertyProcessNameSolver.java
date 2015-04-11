package info.ganglia.jmxetric.processnamesolver;

/**
 * Process name will be the system property with the sent name.
 */
public class PropertyProcessNameSolver implements ProcessNameSolver {
    private final String param;

    public PropertyProcessNameSolver(String param) {
        this.param = param;
    }

    public String solve() {
        return System.getProperty(param);
    }
}
