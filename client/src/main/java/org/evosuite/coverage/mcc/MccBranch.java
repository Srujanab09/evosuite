package org.evosuite.coverage.mcc;

public class MccBranch {
	private String branchName;
	private String trueBranch = "NA";  // NA = Not Available
	private String falseBranch = "NA"; // NA = Not Available
	
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public String getTrueBranch() {
		return trueBranch;
	}
	public void setTrueBranch(String trueBranch) {
		this.trueBranch = trueBranch;
	}
	public String getFalseBranch() {
		return falseBranch;
	}
	public void setFalseBranch(String falseBranch) {
		this.falseBranch = falseBranch;
	}
}
