package info.ganglia.jmxetric.processnamesolver;

/**
 * Create instances of ProcessNameSolver according to the given
 * parameters.
 */
public class ProcessNameSolverFactory {

    private ProcessNameSolverFactory() {
        // empty
    }

    public static ProcessNameSolver create(String type, String param) {
        ProcessNameSolver processNameSolver;
        if ("string".equals(type)) {
            processNameSolver = new StringProcessNameSolver(param);
        } else if ("environment-variable".equals(type)) {
            processNameSolver = new EnvironmentProcessNameSolver(param);
        } else if ("property-variable".equals(type)) {
            processNameSolver = new PropertyProcessNameSolver(param);
        } else {
            processNameSolver = new StringProcessNameSolver(null);
        }
        return processNameSolver;

    }
}
