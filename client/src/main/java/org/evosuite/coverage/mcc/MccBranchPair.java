package org.evosuite.coverage.mcc;

import java.io.Serializable;

import org.evosuite.coverage.branch.Branch;

public class MccBranchPair implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient Branch branch;
	private String branchName;
	private int conditionStatus; // 0 - false, 1 - true, 3 - end
	
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public int getConditionStatus() {
		return conditionStatus;
	}
	public void setConditionStatus(int conditionStatus) {
		this.conditionStatus = conditionStatus;
	}
	public Branch getBranch() {
		return branch;
	}
	public void setBranch(Branch branch) {
		this.branch = branch;
	}
	

}
