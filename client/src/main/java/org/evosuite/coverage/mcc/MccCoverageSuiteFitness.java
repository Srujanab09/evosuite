package org.evosuite.coverage.mcc;


import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

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

	public int maxCoveredBranches = 0;
	public double bestFitness = Double.MAX_VALUE;

	private final List<MccCoverageTestFitness> listOfTestFitnessValues = new ArrayList<>(); 
	
	/**
	 * <p>
	 * Constructor for MccCoverageSuiteFitness.
	 * </p>
	 */
	
	public MccCoverageSuiteFitness() {

		this(TestGenerationContext.getInstance().getClassLoaderForSUT());
	}

	
	
	public MccCoverageSuiteFitness(ClassLoader classLoader) {

		
		listOfTestFitnessValues.addAll(new MccCoverageFactory().getCoverageGoals());

	}


	
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

		Set<MccCoverageTestFitness> coveredObligations = new HashSet<>();
		
		for(MccCoverageTestFitness goal: listOfTestFitnessValues){
			for(ExecutionResult result: results){
				if(goal.isCovered(result)){
					coveredObligations.add(goal);
					break;
				}
				else {
					
					TestChromosome chromosome = new TestChromosome();
					chromosome.setTestCase(result.test);
					chromosome.setLastExecutionResult(result);
					chromosome.setChanged(false);
					
					fitness = fitness + goal.getFitness(chromosome, result);
				}
					
			}
		}
		
		//fitness = listOfTestFitnessValues.size() - coveredObligations.size();

		for (ExecutionResult result : results) {
			if (result.hasTimeout() || result.hasTestException()) {
				fitness = listOfTestFitnessValues.size();
				break;
			}
		}
		
		
		updateIndividual(this, suite, fitness);

		suite.setNumOfCoveredGoals(this, coveredObligations.size());
		
		if(!listOfTestFitnessValues.isEmpty()){
			suite.setCoverage(this, (double) coveredObligations.size() / (double) listOfTestFitnessValues.size());
		}else{
			suite.setCoverage(this, 1.0);
		}
		
		return fitness;

	}

}
