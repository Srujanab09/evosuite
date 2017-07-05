package org.evosuite.coverage.mcc;

public class MccBranchInfo {
	private String branchName;
	private String labelForWhere;
	private String labelForTrue;
	private String labelForFalse;
	
	public String getBranchName() {
		return branchName;
	}
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	public String getLabelForWhere() {
		return labelForWhere;
	}
	public void setLabelForWhere(String labelForWhere) {
		this.labelForWhere = labelForWhere;
	}
	public String getLabelForTrue() {
		return labelForTrue;
	}
	public void setLabelForTrue(String labelForTrue) {
		this.labelForTrue = labelForTrue;
	}
	public String getLabelForFalse() {
		return labelForFalse;
	}
	public void setLabelForFalse(String labelForFalse) {
		this.labelForFalse = labelForFalse;
	}
}
