package org.evosuite.coverage.mcc;

import org.evosuite.coverage.branch.Branch;

public class MccBranch {

	private transient Branch branch;
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
	public Branch getBranch() {
		return branch;
	}
	public void setBranch(Branch branch) {
		this.branch = branch;
	}
	
	
}
