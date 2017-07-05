package org.evosuite.coverage.mcc;

public class MccBranchPair {
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
	
	

}
