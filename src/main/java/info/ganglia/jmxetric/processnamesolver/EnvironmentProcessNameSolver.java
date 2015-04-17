package info.ganglia.jmxetric.processnamesolver;

/**
 * Process name will be the environment variable value with the sent name.
 */
public class EnvironmentProcessNameSolver implements ProcessNameSolver {
    private final String param;

    public EnvironmentProcessNameSolver(String param) {
        this.param = param;
    }

    public String solve() {
        return System.getenv(param);
    }
}
