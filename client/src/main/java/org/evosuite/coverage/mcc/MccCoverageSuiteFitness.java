package org.evosuite.coverage.mcc;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.branch.OnlyBranchCoverageFactory;
import org.evosuite.coverage.branch.OnlyBranchCoverageTestFitness;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fitness function for a whole test suite for all branches
 * 
 * @author Srujana Bollina
 */
public class MccCoverageSuiteFitness extends TestSuiteFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4895102521269459548L;

	private final static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);
	// Coverage targets
	// Coverage targets
	public int totalMccbranchpairs;
	public int totalGoals;
	protected final HashSet<Integer> branchesId;
	
	// Some stuff for debug output
	public int maxCoveredBranches = 0;
	public double bestFitness = Double.MAX_VALUE;

	// Each test gets a set of distinct covered goals, these are mapped by branch id
	private final Map<Integer, TestFitnessFunction> branchCoverageTrueMap = new HashMap<Integer, TestFitnessFunction>();
	private final Map<Integer, TestFitnessFunction> branchCoverageFalseMap = new HashMap<Integer, TestFitnessFunction>();

	private final Set<Integer> toRemoveBranchesT = new HashSet<>();
	private final Set<Integer> toRemoveBranchesF = new HashSet<>();
	
	private final Set<Integer> removedBranchesT = new HashSet<>();
	private final Set<Integer> removedBranchesF = new HashSet<>();
	
	
	
	/**
	 * <p>
	 * Constructor for MccCoverageSuiteFitness.
	 * </p>
	 */
	
	public MccCoverageSuiteFitness() {

		this(TestGenerationContext.getInstance().getClassLoaderForSUT());
	}

	
	
	public MccCoverageSuiteFitness(ClassLoader classLoader) {

		String prefix = Properties.TARGET_CLASS_PREFIX;
		
	/*	if (prefix.isEmpty()) {
			prefix = Properties.TARGET_CLASS;
			totalBranches = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCountForPrefix(prefix);
		} else {
			totalBranches = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCountForPrefix(prefix);
		}*/
		branchesId = new HashSet<>();

		for(String methodName : MccCoverageFactory.mccTestObligations.keySet()) {
			CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> obligationsForMethod = MccCoverageFactory.mccTestObligations.get(methodName);
		
			int noOfObligations = obligationsForMethod.size();
		
			totalGoals = totalGoals + noOfObligations  ;
			
			for(CopyOnWriteArrayList<MccBranchPair> obligation : obligationsForMethod) {
				
				for(MccBranchPair bp : obligation) {
					
					totalMccbranchpairs = totalMccbranchpairs + 1;
				}
				
			}
		}

		//System.out.println("printing number of goals:"+ totalGoals);
		//System.out.println("printing number of branches:"+ totalMccbranchpairs);

		logger.info("Total branch coverage goals: " + totalGoals);
	//	logger.info("Total branches: " + totalBranches);

		determineCoverageGoals();
	}


	/**
	 * Initialize the set of known coverage goals
	 */
	protected void determineCoverageGoals() {
		List<MccCoverageTestFitness> goals = new MccCoverageFactory().getCoverageGoals();
		for (MccCoverageTestFitness goal : goals) {
			for(MccBranchPair bp : goal.getGoal()){
			
				if(Properties.TEST_ARCHIVE)
					TestsArchive.instance.addGoalToCover(this, goal);

				branchesId.add(bp.getBranch().getActualBranchId());
			//	System.out.println("get branches id  :  "+bp.getBranch().getActualBranchId());
				
				int status = bp.getConditionStatus();
				boolean branchExpValue =false;
				if(status == 1){
					branchExpValue = true;
				}
				if (branchExpValue)
					branchCoverageTrueMap.put(bp.getBranch().getActualBranchId(), goal);
				else
					branchCoverageFalseMap.put(bp.getBranch().getActualBranchId(), goal);

			}
		}
		
		System.out.println("size of the branches id set :  "+branchesId.size());

	}

	
	/**
	 * Iterate over all execution results and summarize statistics
	 * 
	 * @param results
	 * @param predicateCount
	 * @param callCount
	 * @param trueDistance
	 * @param falseDistance
	 * @return
	 */
	private boolean analyzeTraces( AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, List<ExecutionResult> results,
	        Map<Integer, Integer> predicateCount, 
	        Map<Integer, Double> trueDistance, Map<Integer, Double> falseDistance) {
		
		boolean hasTimeoutOrTestException = false;
		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {			
				hasTimeoutOrTestException = true;
				continue;
			}
			
			List<MccCoverageTestFitness> goals = new MccCoverageFactory().getCoverageGoals();
			for (MccCoverageTestFitness goal : goals) {

		//		HashMap<Integer, ControlFlowDistance> obligationDist = goal.getDistance(result);
			}
			
			
			for (Entry<Integer, Integer> entry : result.getTrace().getPredicateExecutionCount().entrySet()) {
				if (!branchesId.contains(entry.getKey())
						|| (removedBranchesT.contains(entry.getKey())
						&& removedBranchesF.contains(entry.getKey())))
					continue;
				if (!predicateCount.containsKey(entry.getKey()))
					predicateCount.put(entry.getKey(), entry.getValue());
				else {
					predicateCount.put(entry.getKey(),
							predicateCount.get(entry.getKey())
							+ entry.getValue());
				}
			}
			for (Entry<Integer, Double> entry : result.getTrace().getTrueDistances().entrySet()) {
				if(!branchesId.contains(entry.getKey())||removedBranchesT.contains(entry.getKey())) continue;
				if (!trueDistance.containsKey(entry.getKey()))
					trueDistance.put(entry.getKey(), entry.getValue());
				else {
					trueDistance.put(entry.getKey(),
							Math.min(trueDistance.get(entry.getKey()),
									entry.getValue()));
				}
				if ((Double.compare(entry.getValue(), 0.0) ==0)) {
					result.test.addCoveredGoal(branchCoverageTrueMap.get(entry.getKey()));
					if(Properties.TEST_ARCHIVE) {
						TestsArchive.instance.putTest(this, branchCoverageTrueMap.get(entry.getKey()), result);
						toRemoveBranchesT.add(entry.getKey());
						suite.isToBeUpdated(true);
					}
				}
			}
			for (Entry<Integer, Double> entry : result.getTrace().getFalseDistances().entrySet()) {
				if(!branchesId.contains(entry.getKey())||removedBranchesF.contains(entry.getKey())) continue;
				if (!falseDistance.containsKey(entry.getKey()))
					falseDistance.put(entry.getKey(), entry.getValue());
				else {
					falseDistance.put(entry.getKey(),
							Math.min(falseDistance.get(entry.getKey()),
									entry.getValue()));
				}
				if ((Double.compare(entry.getValue(), 0.0) ==0)) {
					result.test.addCoveredGoal(branchCoverageFalseMap.get(entry.getKey()));
					if(Properties.TEST_ARCHIVE) {
						TestsArchive.instance.putTest(this, branchCoverageFalseMap.get(entry.getKey()), result);
						toRemoveBranchesF.add(entry.getKey());
						suite.isToBeUpdated(true);
					}
				}
			}
		}
		return hasTimeoutOrTestException;
	}
	
/*	@Override
	public boolean updateCoveredGoals() {
		
		if(!Properties.TEST_ARCHIVE)
			return false;
		
		for (Integer branch : toRemoveBranchesT) {
			TestFitnessFunction f = branchCoverageTrueMap.remove(branch);
			if (f != null) {
				removedBranchesT.add(branch);
				if (removedBranchesF.contains(branch)) {
					totalMccbranchpairs--;
					//if(isFullyCovered(f.getTargetClass(), f.getTargetMethod())) {
					//	removeTestCall(f.getTargetClass(), f.getTargetMethod());
					//}
				}
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}
		for (Integer branch : toRemoveBranchesF) {
			TestFitnessFunction f = branchCoverageFalseMap.remove(branch);
			if (f != null) {
				removedBranchesF.add(branch);
				if (removedBranchesT.contains(branch)) {
					totalMccbranchpairs--;
					//if(isFullyCovered(f.getTargetClass(), f.getTargetMethod())) {
					//	removeTestCall(f.getTargetClass(), f.getTargetMethod());
					//}
				}
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}
		
		toRemoveBranchesF.clear();
		toRemoveBranchesT.clear();
		logger.info("Current state of archive: "+TestsArchive.instance.toString());
		
		return true;
	}*/
	
	/**
	 * {@inheritDoc}
	 * 
	 * Execute all tests and count covered branches
	 */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		logger.trace("Calculating Mcc fitness");
		double fitness = 0.0;

		List<ExecutionResult> results = runTestSuite(suite);

		Map<Integer, Double> trueDistance = new HashMap<Integer, Double>();
		Map<Integer, Double> falseDistance = new HashMap<Integer, Double>();
		Map<Integer, Integer> predicateCount = new HashMap<Integer, Integer>();

		// Collect stats in the traces 
		boolean hasTimeoutOrTestException = analyzeTraces(suite, results, predicateCount,
		                                                  trueDistance,
		                                                  falseDistance);
		

		// Collect stats in the traces 

		int n = results.size();
		
		for (int i =0 ; i< n; i++) {
						
			if (results.get(i).hasTimeout() || results.get(i).hasTestException()) {			
				hasTimeoutOrTestException = true;
				continue;
			}
			
			List<MccCoverageTestFitness> goals = new MccCoverageFactory().getCoverageGoals();
			for (MccCoverageTestFitness goal : goals) {
				double s = goal.getFitness((TestChromosome) suite.getTestChromosome(i), results.get(i));
				if(s < bestFitness){
						bestFitness = s;
				}
			}
			
		}
		
		// Collect branch distances of covered branches
		int numCoveredBranches = 0;

		for (Integer key : predicateCount.keySet()) {
			
			double df = 0.0;
			double dt = 0.0;
			int numExecuted = predicateCount.get(key);
			
			if(removedBranchesT.contains(key))
				numExecuted++;
			if(removedBranchesF.contains(key))
				numExecuted++;
			
			if (trueDistance.containsKey(key)) {
				dt =  trueDistance.get(key);
			}
			if(falseDistance.containsKey(key)){
				df = falseDistance.get(key);
			}
			// If the branch predicate was only executed once, then add 1 
			if (numExecuted == 1) {
				fitness += 1.0;
			} else {
				fitness += normalize(df) + normalize(dt);
			}

			if (falseDistance.containsKey(key)&&(Double.compare(df, 0.0) == 0))
				numCoveredBranches++;

			if (trueDistance.containsKey(key)&&(Double.compare(dt, 0.0) == 0))
				numCoveredBranches++;
		}
		
		// +1 for every branch that was not executed
		fitness += (totalMccbranchpairs - 2* predicateCount.size());
		
		// +1 for every branch that was not executed
//		fitness +=  (totalMccbranchpairs - 2 * predicateCount.size());

		printStatusMessages(suite, numCoveredBranches, fitness);

		// Calculate coverage
		int coverage = numCoveredBranches;

		coverage +=removedBranchesF.size();
		coverage +=removedBranchesT.size();	
 		
		if (totalGoals > 0)
			suite.setCoverage(this, (double) coverage / (double) totalGoals);
        else
            suite.setCoverage(this, 1);

		suite.setNumOfCoveredGoals(this, coverage);
		suite.setNumOfNotCoveredGoals(this, totalGoals-coverage);
		
		if (hasTimeoutOrTestException) {
			logger.info("Test suite has timed out, setting fitness to max value "
			        + (totalMccbranchpairs));
			fitness = totalMccbranchpairs;
			//suite.setCoverage(0.0);
		}

		updateIndividual(this, suite, fitness);

		assert (coverage <= totalGoals) : "Covered " + coverage + " vs total goals "
		        + totalGoals;
		assert (fitness >= 0.0);
		assert (fitness != 0.0 || coverage == totalGoals) : "Fitness: " + fitness + ", "
		        + "coverage: " + coverage + "/" + totalGoals;
		assert (suite.getCoverage(this) <= 1.0) && (suite.getCoverage(this) >= 0.0) : "Wrong coverage value "
		        + suite.getCoverage(this); 
		return fitness;
	}

	/**
	 * Some useful debug information
	 * 
	 * @param coveredBranches
	 * @param coveredMethods
	 * @param fitness
	 */
	private void printStatusMessages(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite,
	        int coveredBranches, double fitness) {
		if (coveredBranches > maxCoveredBranches) {
			maxCoveredBranches = coveredBranches;
			logger.info("(Branches) Best individual covers " + coveredBranches + "/"
			        + (totalMccbranchpairs) + " branches");
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}
		if (fitness < bestFitness) {
			logger.info("(Fitness) Best individual covers " + coveredBranches + "/"
			        + (totalMccbranchpairs) + " branches");
			bestFitness = fitness;
			logger.info("Fitness: " + fitness + ", size: " + suite.size() + ", length: "
			        + suite.totalLengthOfTestCases());
		}
	}

}