package org.evosuite.coverage.mcc;



import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;


/**
 * Fitness function for a single test on a single branch
 * 
 * @author Srujana Bollina
 */
public class MccCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = -6310967747257242580L;

	/** Target branch */
	private final MccCoverageGoal goal;

	/**
	 * Constructor - fitness is specific to a branch
	 * 
	 * @param goal
	 *            a {@link org.evosuite.coverage.branch.BranchCoverageGoal}
	 *            object.
	 */

/*
 *  passing coverage goal as bytecode instruction
 */
	public MccCoverageTestFitness(MccCoverageGoal goal)  throws IllegalArgumentException{
		
		
		if(goal == null){
			throw new IllegalArgumentException("goal cannot be null");
		}
		this.goal = goal;
		
		
	}
	
	/**
	 * <p>
	 * getBranch
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.branch.Branch} object.
	 */
	public MccBranchPair getMccBranch() {
		return goal.getMccbranch();
	}

	public boolean getValue() {
		return goal.getValue();
	}

	public MccCoverageGoal getMccGoal() {
		return goal;
	}

	/**
	 * <p>
	 * getClassName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getClassName() {
		return goal.getClassName();
	}

	/**
	 * <p>
	 * getMethod
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getMethod() {
		return goal.getMethodName();
	}

	/**
	 * <p>
	 * getBranchExpressionValue
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean getBranchExpressionValue() {
		return goal.getValue();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Calculate approach level + branch distance
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		ControlFlowDistance distance = goal.getDistance(result);

		double fitness = distance.getResultingBranchFitness();
		


		if(logger.isDebugEnabled()) {
			logger.debug("Goal at line "+goal.getLineNumber()+": approach level = " + distance.getApproachLevel()
					+ " / branch distance = " + distance.getBranchDistance() + ", fitness = " + fitness);
		}
		updateIndividual(this, individual, fitness);
		
	//	System.out.println("printing fitness "+ fitness );
	//	System.out.println(" for goal :" +goal.getLineNumber());
		
		return fitness;
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return goal.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((goal == null) ? 0 : goal.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MccCoverageTestFitness other = (MccCoverageTestFitness) obj;
		if (goal == null) {
			if (other.goal != null)
				return false;
		} else if (!goal.equals(other.goal))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof MccCoverageTestFitness) {
			MccCoverageTestFitness otherBranchFitness = (MccCoverageTestFitness) other;
			return goal.compareTo(otherBranchFitness.goal);
		}
		return compareClassName(other);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
	 */
	@Override
	public String getTargetClass() {
		return getClassName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
	 */
	@Override
	public String getTargetMethod() {
		return getMethod();
	}

}
