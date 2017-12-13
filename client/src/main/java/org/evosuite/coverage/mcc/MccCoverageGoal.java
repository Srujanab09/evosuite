
package org.evosuite.coverage.mcc;


import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;


import org.evosuite.coverage.ControlFlowDistance;
import org.evosuite.coverage.branch.ControlFlowDistanceCalculator;
import org.evosuite.testcase.execution.ExecutionResult;


/**
 * A single MCC coverage goal is a list of branches being either true/false evaluation of a jump
 * condition.
 * 
 * @author Srujana Bollina
 */
public class MccCoverageGoal implements Serializable, Comparable<MccCoverageGoal> {

	//private static final long serialVersionUID = 2962922303111452419L;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CopyOnWriteArrayList<MccBranchPair> obligation;
	
	private final String className;
	private final String methodName;
	
	HashMap<Integer, ControlFlowDistance> obligationdistance = new HashMap<Integer, ControlFlowDistance>();
	
	/**
	 * The line number in the source code. This information is stored in the bytecode if the
	 * code was compiled in debug mode. If no info, we would get a negative value (e.g., -1) here.
	 */
	private int lineNumber;

	
	
	public int getLineNumber() {
		return lineNumber;
	}


	public MccCoverageGoal(CopyOnWriteArrayList<MccBranchPair> obligation, String className, String methodName) {

		this.setObligation(obligation);
		this.className = className;
		this.methodName = methodName;
		
		for(MccBranchPair bp : obligation) {

			lineNumber = bp.getBranch().getInstruction().getLineNumber();
			
			//System.out.println(" 1. "+ methodName +" "+ "2. "+bp.getBranch().getMethodName());
			
			if (!bp.getBranch().getMethodName().equals(methodName)
			        || !bp.getBranch().getClassName().equals(className))
				throw new IllegalArgumentException(
				        "expect explicitly given information about a branch to coincide with the information given by that branch");
		}
		
	}




	public CopyOnWriteArrayList<MccBranchPair> getObligation() {
		return obligation;
	}



	public void setObligation(CopyOnWriteArrayList<MccBranchPair> obligation) {
		this.obligation = obligation;
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
	 * <p>
	 * getDistance
	 * </p>
	 * 
	 * @param result
	 *            a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 * @return a {@link org.evosuite.coverage.ControlFlowDistance} object.
	 */
	public HashMap<Integer, ControlFlowDistance> getDistance(ExecutionResult result) {
		

		for(MccBranchPair bp : obligation) {
			boolean status = false;
			if(bp.getConditionStatus() == 0) 
				status = false;
			else
				status = true;

			ControlFlowDistance r = ControlFlowDistanceCalculator.getDistance(result, bp.getBranch(), status,
					className, methodName);
			
			obligationdistance.put(bp.getBranch().getActualBranchId(), r);
			
		}
		
		return obligationdistance;
	}



	/**
	 * {@inheritDoc}
	 * 
	 * Readable representation
	 */
	@Override
	public String toString() {
		String name = className + "." + methodName + ":";
		if (obligation != null) {
			
		for(MccBranchPair bp : obligation) {
				boolean status = false;
				if(bp.getConditionStatus() == 0) 
					status = false;
				else
					status = true;

				name += " " + bp.getBranch().toString();
				
			if (status)
				name += " - true";
			else
				name += " - false";
			} 

		}
		else
			return null;
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		
		for(MccBranchPair bp : obligation) {
			result = prime * result + (bp == null || bp.getBranch() ==null? 0 : bp.getBranch().getActualBranchId());
			result = prime * result
			        + (bp == null || bp.getBranch() ==null? 0 : bp.getBranch().getInstruction().getInstructionId());
			result = prime * result + className.hashCode();
			result = prime * result + methodName.hashCode();

		} 

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

		CopyOnWriteArrayList<MccBranchPair> otherobligation = other.getObligation();
		
		
		
		// are we both root goals?
		if (this.obligation == null) {
			if (otherobligation != null)
				return false;
			else
				// i don't have to check for value at this point, because if
				// branch is null we are talking about the root branch here
				return this.methodName.equals(other.methodName)
				        && this.className.equals(other.className);
		}
		// well i am not, if you are we are different
		if (otherobligation == null)
			return false;

		if(obligation.size() != otherobligation.size())
			return false;
		
		int count =0;
		for(MccBranchPair bp : obligation){
			for(MccBranchPair bp2: otherobligation){
				if(bp.getBranch() !=null && bp2.getBranch() !=null){
					if(bp.getBranch().equals(bp2.getBranch()) && bp.getConditionStatus() == bp2.getConditionStatus()){
						count++;
					}
				}
				else
					return false;
			}
		}
		
		return obligation.size() == count? true: false;
		
	}

	@Override
	public int compareTo(MccCoverageGoal o) {
		int diff = lineNumber - o.lineNumber;
		if(diff == 0) {
			return 0;

		} else {
			return diff;
		}
	}


}
