
package org.evosuite.coverage.mcc;

import java.io.IOException;

import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.coverage.branch.ControlFlowDistanceCalculator;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * A single branch coverage goal Either true/false evaluation of a jump
 * condition, or a method entry
 * 
 * @author Srujana Bollina
 */
public class MccCoverageGoal implements Serializable, Comparable<MccCoverageGoal> {

	//private static final long serialVersionUID = 2962922303111452419L;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MccBranchPair mccbranch;
	
	private final boolean value;
	private final String className;
	private final String methodName;
	
	
	/**
	 * The line number in the source code. This information is stored in the bytecode if the
	 * code was compiled in debug mode. If no info, we would get a negative value (e.g., -1) here.
	 */
	private final int lineNumber;

	
	public MccCoverageGoal(MccBranchPair mccbranch, boolean value, String className,
	        String methodName) {
		if (className == null || methodName == null)
			throw new IllegalArgumentException("null given");
		if (mccbranch == null && !value)
			throw new IllegalArgumentException(
			        "expect goals for a root branch to always have value set to true");
		
		this.value = value;

		this.className = className;
		this.methodName = methodName;

		this.mccbranch = mccbranch;
		if (mccbranch != null && mccbranch.getBranch() != null) {
			lineNumber = mccbranch.getBranch().getInstruction().getLineNumber();
			if (!mccbranch.getBranch().getMethodName().equals(methodName)
			        || !mccbranch.getBranch().getClassName().equals(className))
				throw new IllegalArgumentException(
				        "expect explicitly given information about a branch to coincide with the information given by that branch");
		} else {
			lineNumber = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
					.getFirstLineNumberOfMethod(className,methodName);
		}
	}

	
	public MccCoverageGoal(ControlDependency cd, MccBranchPair mccbranch, String className, String methodName) {
		
		this(mccbranch, cd.getBranchExpressionValue(), className, methodName);
	}

	/**
	 * Methods that have no branches don't need a cfg, so we just set the cfg to
	 * null
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param methodName
	 *            a {@link java.lang.String} object.
	 */
	public MccCoverageGoal(String className, String methodName) {
		this.mccbranch = null;
		this.value = true;

		this.className = className;
		this.methodName = methodName;
		lineNumber = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
				.getFirstLineNumberOfMethod(className,  methodName);		                                                                                                                  
	}


	/**
	 * @return the value
	 */
	public boolean getValue() {
		return value;
	}

	
	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	public MccBranchPair getMccbranch() {
		return mccbranch;
	}

	
	/**
	 * Determines whether this goals is connected to the given goal
	 * 
	 * This is the case when this goals target branch is control dependent on
	 * the target branch of the given goal or visa versa
	 * 
	 * This is used in the ChromosomeRecycler to determine if tests produced to
	 * cover one goal should be used initially when trying to cover the other
	 * goal
	 * 
	 * @param goal
	 *            a {@link org.evosuite.coverage.branch.BranchCoverageGoal}
	 *            object.
	 * @return a boolean.
	 */
	public boolean isConnectedTo(MccCoverageGoal goal) {
		if (mccbranch == null || goal.getMccbranch() == null) {
			// one of the goals targets a root branch
			return goal.methodName.equals(methodName) && goal.className.equals(className);
		}

		// TODO map this to new CDG !

		return mccbranch.getBranch().getInstruction().isDirectlyControlDependentOn(goal.getMccbranch().getBranch())
		        || goal.getMccbranch().getBranch().getInstruction().isDirectlyControlDependentOn(mccbranch.getBranch());
	}

	/**
	 * <p>
	 * getDistance
	 * </p>
	 * 
	 * @param result
	 *            a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 * @return a {@link org.evosuite.coverage.ControlFlowDistance} object.
	 */
	public ControlFlowDistance getDistance(ExecutionResult result) {

		ControlFlowDistance r = ControlFlowDistanceCalculator.getDistance(result, mccbranch.getBranch(), value,
				className, methodName);
		return r;
	}



	/**
	 * {@inheritDoc}
	 * 
	 * Readable representation
	 */
	@Override
	public String toString() {
		String name = className + "." + methodName + ":";
		if (mccbranch != null && mccbranch.getBranch()!=null) {
			name += " " + mccbranch.getBranch().toString();
			if (value)
				name += " - true";
			else
				name += " - false";
		} else
			name += " root-Branch";

		return name;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (mccbranch == null || mccbranch.getBranch() ==null? 0 : mccbranch.getBranch().getActualBranchId());
		result = prime * result
		        + (mccbranch == null || mccbranch.getBranch() ==null? 0 : mccbranch.getBranch().getInstruction().getInstructionId());
		// TODO sure you want to call hashCode() on the cfg? doesn't that take
		// long?
		// Seems redundant -- GF
		/*
		result = prime
		        * result
		        + ((branch == null) ? 0
		                : branch.getInstruction().getActualCFG().hashCode());
		                */
		result = prime * result + className.hashCode();
		result = prime * result + methodName.hashCode();
		result = prime * result + (value ? 1231 : 1237);
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

		MccCoverageGoal other = (MccCoverageGoal) obj;
		// are we both root goals?
		if (this.mccbranch == null|| this.mccbranch.getBranch() == null) {
			if (other.mccbranch != null || other.mccbranch.getBranch() !=null)
				return false;
			else
				// i don't have to check for value at this point, because if
				// branch is null we are talking about the root branch here
				return this.methodName.equals(other.methodName)
				        && this.className.equals(other.className);
		}
		// well i am not, if you are we are different
		if (other.mccbranch == null || other.mccbranch.getBranch() ==null)
			return false;

		// so we both have a branch to cover, let's look at that branch and the
		// way we want it to be evaluated
		if (!this.mccbranch.getBranch().equals(other.mccbranch.getBranch()))
			return false;
		else {
			return this.value == other.value;
		}
	}

	@Override
	public int compareTo(MccCoverageGoal o) {
		int diff = lineNumber - o.lineNumber;
		if(diff == 0) {
			return 0;
			// TODO: this code in some cases leads to the violation of the compare
			// contract. I still have to figure out why - mattia
//			// Branch can only be null if this is a branchless method
//			if(branch == null || o.getBranch() == null)
//				return 0;
//			
//			// If on the same line, order by appearance in bytecode
//			return branch.getActualBranchId() - o.getBranch().getActualBranchId();
		} else {
			return diff;
		}
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		// Write/save additional fields
		if (mccbranch != null)
			oos.writeInt(mccbranch.getBranch().getActualBranchId());
		else
			oos.writeInt(-1);
	}

	// assumes "static java.util.Date aDate;" declared
/*
 *  by srujana temp cometing the code to test
 * 	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();

		int branchId = ois.readInt();
		if (branchId >= 0)
			this.mccbranch = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranch(branchId);
		else
			this.branch = null;
	}*/
}
