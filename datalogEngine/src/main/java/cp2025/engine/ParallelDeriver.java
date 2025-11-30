package cp2025.engine;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import cp2025.engine.Datalog.Atom;
import cp2025.engine.Datalog.Constant;
import cp2025.engine.Datalog.Predicate;
import cp2025.engine.Datalog.Program;
import cp2025.engine.Datalog.Rule;
import cp2025.engine.Datalog.Variable;
import java.util.Collections;

import static java.util.Collections.synchronizedList;


/**
 * A straightforward single-threaded, deterministic implementation of
 * {@link AbstractDeriver} that evaluates queries in a Datalog program by
 * recursively attempting to derive each goal statement using the program's rules.
 *
 * <p>{@code ParallelDeriver} performs derivation in a naive, depth-first manner.</p>
 *
 * <p>This implementation is thread-interruptible: long-running derivations
 * may throw {@link InterruptedException} if the current thread is interrupted.</p>
 */
public class ParallelDeriver implements AbstractDeriver {
    private final int numWorkers;
    private final Semaphore mutex = new Semaphore(1);
    private Queue<Atom> tasks = new LinkedList<>();
    private ParallelGlobalDeriverState state;
    private boolean globalInterrupt = false;

    public ParallelDeriver(int numWorkers) { this.numWorkers = numWorkers; }


    /**
     * Derives all queries in the given Datalog program using the provided
     * oracle.
     *
     * <p>The method constructs an internal {@link ParallelGlobalDeriverState} object to manage
     * derivation state (known results, recursion tracking, etc.), then processes each
     * query atom individually TODO change desc.</p>
     *
     * @param input the {@link Datalog.Program} containing facts, rules, and
     *              queries to evaluate.
     * @param oracle the {@link AbstractOracle} used to directly
     *                   evaluate calculatable predicates.
     * @return a map from each {@link Datalog.Atom} query to a {@code boolean}
     *         indicating whether it is derivable.
     * @throws InterruptedException if the thread running this method is interrupted.
     */
    @Override
    public Map<Atom, Boolean> derive(Program input, AbstractOracle oracle)
            throws InterruptedException {
        state = new ParallelGlobalDeriverState(input, oracle);

        Map<Atom, Boolean> results = new HashMap<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numWorkers; i++) {
            threads.add(new Thread(new Worker()));
        }
        tasks.addAll(input.queries());
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                thread.join();
            } catch (InterruptedException e) {
                globalInterrupt = true;
                for (Thread thread1 : threads) {
                    thread1.interrupt();
                }
                throw new InterruptedException();
            }
        }
        for (Atom query : input.queries()) {
            results.put(query, state.knownStatements.get(query).get());
        }
        return results;
    }


    private class Worker implements Runnable {

        @Override
        public void run() {
            while (true) {
                Atom task;
                try {
                    mutex.acquire();
                    if (!tasks.isEmpty()) {
                        task = tasks.poll();
                    } else {
                        mutex.release();
                        //this thread is no longer needed
                        break;
                    }
                    mutex.release();
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    ParallelThreadDeriverState ownState = new ParallelThreadDeriverState();
                    ownState.deriveStatement(task);
                } catch (InterruptedException e) {
                    if (globalInterrupt) {
                        break;
                    }
                    //else this atom already has been calculated so continue
                }
            }
        }
    }


    /**
     * Represents the result of attempting to derive a statement.
     *
     * @param derivable whether the statement was successfully derived.
     *  If true, the statement is derivable.
     *  If false, the statement is not derivable without using
     *  in-progress statements in the derivation.
     * @param failedStatements a set of statements that are known to be non-derivable
     *                         if all in-progress statements are non-derivable.
     */
    private record DerivationResult(boolean derivable, Set<Atom> failedStatements) {}

    private class ParallelThreadDeriverState {

        /**
         * Set of statements currently being processed (to detect cycles).
         */
        private final List<Atom> inProgressStatements;

        public ParallelThreadDeriverState() {
            inProgressStatements = synchronizedList(new ArrayList<>());
            state.inProgressStatementsThread.put(Thread.currentThread(), inProgressStatements);
        }

        private DerivationResult basicInterruptionReact(Atom goal, InterruptedException e) throws InterruptedException {
            if (globalInterrupt) {
                throw new InterruptedException();
            }
            if (state.knownStatements.containsKey(goal)) {
                return new DerivationResult(state.knownStatements.get(goal).get(), Set.of());
            }
            throw e;
        }

        /**
         * Determines whether the given goal statement can be derived (from the
         * current program and using the current oracle) in a way that avoids
         * using statements that are in-progress at the moment of calling.
         *
         * @param goal the statement to derive.
         * @return a {@link DerivationResult}, see {@link DerivationResult}.
         * @throws InterruptedException if the derivation process is
         *         interrupted.
         */
        public DerivationResult deriveStatement(Atom goal) throws InterruptedException {
            // Check if we already know the result for this statement.
            if (state.knownStatements.containsKey(goal))
                return new DerivationResult(state.knownStatements.get(goal).get(), Set.of());

            // Check if we got a cancellation request.
            if (Thread.interrupted()) {
                return basicInterruptionReact(goal, new InterruptedException());
            }

            // Check if the statement is calculatable.
            if (state.oracle.isCalculatable(goal.predicate())) {
                boolean result;
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    state.inProgressByThread.putIfAbsent(goal, new ConcurrentLinkedQueue<>());
                    state.inProgressByThread.get(goal).add(Thread.currentThread());
                    result = state.oracle.calculate(goal);
                    state.inProgressByThread.get(goal).remove(Thread.currentThread());
                } catch (InterruptedException e) {
                    if (globalInterrupt) throw new InterruptedException();
                    if (!state.knownStatements.containsKey(goal)) {
                        throw new InterruptedException();
                    }
                    result = state.knownStatements.get(goal).get();
                    state.inProgressByThread.get(goal).remove(Thread.currentThread());
                    return new DerivationResult(result, Set.of());
                }
                state.knownStatements.put(goal, new AtomicBoolean(result));
                //we know the result first, so we gotta wake everyone wanting this up
                for (Thread thread : state.inProgressByThread.get(goal)) {
                    thread.interrupt();
                }
                return new DerivationResult(result, Set.of());
            }

            // Check for cycles, to avoid infinite loops.
            if (inProgressStatements.contains(goal)) {
                // Return false but do not store the result (we may find a different derivation later).
                return new DerivationResult(false, Set.of(goal));
            }
            inProgressStatements.add(goal);
            state.inProgressByThread.putIfAbsent(goal, new ConcurrentLinkedQueue<>());
            state.inProgressByThread.get(goal).add(Thread.currentThread());

            // Try to actually derive the statement using rules.
            DerivationResult result;
            try {
                result = deriveNewStatement(goal);
                state.inProgressByThread.get(goal).remove(Thread.currentThread());
                inProgressStatements.remove(goal);
            } catch (InterruptedException e) {
                state.inProgressByThread.get(goal).remove(Thread.currentThread());
                inProgressStatements.remove(goal);
                return basicInterruptionReact(goal, e);
            }

            if (result.derivable) {
                state.knownStatements.put(goal, new AtomicBoolean(true));
            } else {
                // We can only deduce non-derivability when there are no in-progress statements
                // (at the top of the recursion).
                if (inProgressStatements.isEmpty())
                    for (Atom s : result.failedStatements)
                        state.knownStatements.put(s, new AtomicBoolean(false));
            }
            return result;
        }

        /**
         * Attempts to derive a "new" statement.
         * Here "new" means that:
         * <ul>
         * <li>the derivability of the statement is not yet known;</li>
         * <li>the statement is not calculatable by the oracle; and</li>
         * <li>the statement was not in-progress when originally requested in deriveStatement()
         *     (but has been added to inProgressStatements since then).</li>
         *
         * @param goal the statement to derive.
         * @return a {@link DerivationResult} representing whether the goal is derivable.
         * @throws InterruptedException if the process is interrupted.
         */
        private DerivationResult deriveNewStatement(Atom goal) throws InterruptedException {
            List<Rule> rules = state.predicateToRules.get(goal.predicate());
            if (rules == null)
                return new DerivationResult(false, Set.of(goal));

            Set<Atom> failedStatements = new HashSet<>();

            for (Rule rule : rules) {
                Optional<List<Atom>> partiallyAssignedBody = Unifier.unify(rule, goal);
                if (partiallyAssignedBody.isEmpty())
                    continue;

                List<Variable> variables = Datalog.getVariables(partiallyAssignedBody.get());
                FunctionGenerator<Variable, Constant> iterator = new FunctionGenerator<>(variables,
                        state.input.constants());
                for (Map<Variable, Constant> assignment : iterator) {
                    List<Atom> assignedBody = Unifier.applyAssignment(partiallyAssignedBody.get(),
                            assignment);
                    DerivationResult result;
                    try {
                        result = deriveBody(assignedBody);
                    } catch (InterruptedException e) {
                        return basicInterruptionReact(goal, e);
                    }
                    if (result.derivable)
                        return new DerivationResult(true, Set.of());
                    failedStatements.addAll(result.failedStatements);
                }
            }

            failedStatements.add(goal);
            return new DerivationResult(false, failedStatements);
        }

        /**
         * Derive all statements in an assigned body.
         * Returns failure as soon as any statement in the body fails (i.e. is
         * found to not be derivable without using inProgressStatements in the
         * derivation). Returns success otherwise.
         *
         * @param body a list of statements forming the body of a rule.
         * @return a {@link DerivationResult} indicating success or failure.
         * @throws InterruptedException if the derivation process is interrupted.
         */
        private DerivationResult deriveBody(List<Atom> body) throws InterruptedException {
            for (Atom statement : body) {
                DerivationResult result = deriveStatement(statement);
                if (!result.derivable)
                    return new DerivationResult(false, result.failedStatements);
            }
            return new DerivationResult(true, Set.of());
        }
    }

    private static class ParallelGlobalDeriverState {
        private final Program input;
        private final AbstractOracle oracle;
        private final ConcurrentMap<Atom, ConcurrentLinkedQueue<Thread>> inProgressByThread = new ConcurrentHashMap<>();

        /**
         * Set of statements currently being processed (to detect cycles).
         */
        private final ConcurrentMap<Thread, List<Atom>> inProgressStatementsThread = new ConcurrentHashMap<>();

        /**
         * An immutable map for fast access to all rules with a given head predicate.
         */
        private final Map<Predicate, List<Rule>> predicateToRules;

        /**
         * A map of statements for which derivability is determined (derivable
         * or not derivable).
         */
        private final ConcurrentMap<Atom, AtomicBoolean> knownStatements = new ConcurrentHashMap<>();

        /**
         * Creates a new state for the derivation of a specific Datalog program.
         *
         * @param input the Datalog program being processed.
         * @param oracle the oracle used for calculatable predicates.
         */
        public ParallelGlobalDeriverState(Program input, AbstractOracle oracle) {
            this.input = input;
            this.oracle = oracle;

            // Build the predicateToRules map.
            this.predicateToRules = input.rules().stream().collect(
                    java.util.stream.Collectors.groupingBy(rule -> rule.head().predicate()));
        }
    }

}
