package org.evosuite.coverage.mcc;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * BranchCoverageFactory class.
 * </p>
 * 
 * @author Gordon Fraser, Andre Mis
 */
public class MccCoverageFactory extends
		AbstractFitnessFactory<MccCoverageTestFitness> {

	private static final Logger logger = LoggerFactory.getLogger(MccCoverageFactory.class);
	
	public static HashMap<String, ArrayList<String>> mccInsts = new HashMap<String, ArrayList<String>>();
	
	public static HashMap<String, Branch> mccInstruction = new HashMap<String, Branch>();

	public static HashMap<String, ArrayList<MccBranchInfo>> mccBranchInfoMap = new HashMap<String, ArrayList<MccBranchInfo>>();
	
	public static HashMap<String, CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>>> mccTestObligations = new HashMap<String, CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>>>();
	
	
	/**
	 * return coverage goals of the target class or of all the contextual branches, depending on the limitToCUT paramether
	 * @param limitToCUT
	 * @return
	 */

	private List<MccCoverageTestFitness> computeCoverageGoals(boolean limitToCUT){
		long start = System.currentTimeMillis();
		List<MccCoverageTestFitness> goals = new ArrayList<MccCoverageTestFitness>();
		
		for (String className : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
			//when limitToCUT== true, if not the class under test of a inner/anonymous class, continue
			if(limitToCUT && !isCUT(className)) continue;
			//when limitToCUT==false, consider all classes, but excludes libraries ones according the INSTRUMENT_LIBRARIES property
			if(!limitToCUT && (!Properties.INSTRUMENT_LIBRARIES && !DependencyAnalysis.isTargetProject(className))) continue;
			final MethodNameMatcher matcher = new MethodNameMatcher();
			// Branchless methods
			for (String method : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchlessMethods(className)) {
				if (matcher.fullyQualifiedMethodMatches(method)) {
					
					goals.add(createRootMccTestFitness(className, method));
				}
			}

			// Branches
			for (String methodName : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownMethods(className)) {
				if (!matcher.methodMatches(methodName)) {
					logger.info("Method " + methodName + " does not match criteria. ");
					continue;
				}

				for (Branch b : BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).retrieveBranchesInMethod(className,
						methodName)) {
					
                    if(!b.isInstrumented()) {
          
                		for(String methodName1 : mccTestObligations.keySet()) {
                			CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> obligationsForMethod = mccTestObligations.get(methodName1);
                			for(CopyOnWriteArrayList<MccBranchPair> obligation : obligationsForMethod) {
                				
                				for(MccBranchPair bp : obligation) {
                					boolean status = false;
                					if(bp.getConditionStatus() == 0) 
                						status = false;
                					else
                						status = true;
                				
                					if(!goals.contains(createMccCoverageTestFitness(b, bp, status))){
                					goals.add(createMccCoverageTestFitness(b, bp,status));}
                					
                				}
                				
                			}
                		}
                    
                    }
				}
			}
		}
			return goals;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<MccCoverageTestFitness> getCoverageGoals() {
		return computeCoverageGoals(true);
	}

	public List<MccCoverageTestFitness> getCoverageGoalsForAllKnownClasses() {
		return computeCoverageGoals(false); 
	}


	/**
	 * Create a fitness function for branch coverage aimed at executing the
	 * given ControlDependency.
	 * 
	 * @param cd
	 *            a {@link org.evosuite.graphs.cfg.ControlDependency} object.
	 * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
	 *         object.
	 */
/*
	public static MccCoverageTestFitness createMccCoverageTestFitness(
			ControlDependency cd) {
		return createMccCoverageTestFitness(cd.getBranch(),
				cd.getBranchExpressionValue());
	}
 */
	/**
	 * Create a fitness function for branch coverage aimed at executing the
	 * Branch identified by b as defined by branchExpressionValue.
	 * 
	 * @param b
	 *            a {@link org.evosuite.coverage.branch.Branch} object.
	 * @param branchExpressionValue
	 *            a boolean.
	 * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
	 *         object.
	 */
	public static MccCoverageTestFitness createMccCoverageTestFitness(
			Branch b, MccBranchPair bp, boolean branchExpressionValue ) {

		return new MccCoverageTestFitness(new MccCoverageGoal(b, bp,
				branchExpressionValue, b.getClassName(), b.getMethodName()));
	}

	/**
	 * Create a fitness function for branch coverage aimed at covering the root
	 * branch of the given method in the given class. Covering a root branch
	 * means entering the method.
	 * 
	 * @param className
	 *            a {@link java.lang.String} object.
	 * @param method
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
	 *         object.
	 */
	public static MccCoverageTestFitness createRootMccTestFitness(
			String className, String method) {

		return new MccCoverageTestFitness(new MccCoverageGoal(className,
				method.substring(method.lastIndexOf(".") + 1)));
	}

	/**
	 * Convenience method calling createRootBranchTestFitness(class,method) with
	 * the respective class and method of the given BytecodeInstruction.
	 * 
	 * @param instruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
	 *         object.
	 */
	public static MccCoverageTestFitness createRootBranchTestFitness(
			BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");

		return createRootMccTestFitness(instruction.getClassName(),
				instruction.getMethodName());
	}
	
	public static void storeInstrcutionForMCC(String methodName, BytecodeInstruction instruction, String inst, ClassLoader classLoader) {
		synchronized (instruction) {
			
//			Branch b = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchForInstruction(instruction);
		//	System.out.println(">>>>>> -----------------"+TestGenerationContext.getInstance().getClassLoaderForSUT());
		//	System.out.println(">>>>>> ------Size------"+ TestGenerationContext.getInstance().getClassLoaderForSUT().getLoadedClasses().size());
		//	for(String s: TestGenerationContext.getInstance().getClassLoaderForSUT().getLoadedClasses()){
		//		System.out.println("loaded classes: "+s);
		//	}
//			System.out.println(" >>>> Test output -------------------");
//			System.out.println(b.getActualBranchId());
//			System.out.println(b.getInstruction());
			

				if(MccCoverageFactory.mccInsts.containsKey(methodName)) {
					MccCoverageFactory.mccInsts.get(methodName).add(inst);
					if(BranchPool.getInstance(classLoader).isKnownAsBranch(instruction)){
						Branch b = BranchPool.getInstance(classLoader).getBranchForInstruction(instruction);
						System.out.println("Printing actual id ......... "+b.getActualBranchId());
						MccCoverageFactory.mccInstruction.put(inst, b);
					}
					
				}
				else {
					ArrayList<String> temp = new ArrayList<String>();
					temp.add(inst);
					MccCoverageFactory.mccInsts.put(methodName, temp);
					if(BranchPool.getInstance(classLoader).isKnownAsBranch(instruction)){
						Branch b = BranchPool.getInstance(classLoader).getBranchForInstruction(instruction);
						System.out.println("Printing actual id >>>>>>>>>>>> "+b.getActualBranchId());
						MccCoverageFactory.mccInstruction.put(inst, b);
					}
				}
		}
		
	}
	private static int counter = 0;
	
	public static void processMccInstrcution() {
			System.out.println("----processMccInstrcution:::"+counter++);
		// Get the method level branch info for MCC: MccBranchInfo
		for(String methodName : mccInsts.keySet()) {
			ArrayList<String> instsForMethod = mccInsts.get(methodName);
			//System.out.println("----Mcc Method Name:::"+methodName);
			ArrayList<MccBranchInfo> list =  getMccBranchInfoList(instsForMethod);
			mccBranchInfoMap.put(methodName, list);
			
			// Get the Test obligations for MCC: MccTruthTable
			ArrayList<MccBranch> mccBranchList = getMccBranchList(list);
			if(mccBranchList != null & mccBranchList.size() > 1) {
				
				MccBranchPair firstTrueBranch = new MccBranchPair();
				firstTrueBranch.setBranchName(mccBranchList.get(0).getBranchName());
				firstTrueBranch.setBranch(mccBranchList.get(0).getBranch());
				firstTrueBranch.setConditionStatus(1); // 1 == true
				
				// start with (first branch - true) &  (first branch - false) obligation
				MccBranchPair firstFalseBranch = new MccBranchPair();
				firstFalseBranch.setBranchName(mccBranchList.get(0).getBranchName());
				firstFalseBranch.setBranch(mccBranchList.get(0).getBranch());
				firstFalseBranch.setConditionStatus(0); // 0 == false
				
				CopyOnWriteArrayList<MccBranchPair> trueBranchPairList = new CopyOnWriteArrayList<MccBranchPair>();
				trueBranchPairList.add(firstTrueBranch);
				CopyOnWriteArrayList<MccBranchPair> falseBranchPairList = new CopyOnWriteArrayList<MccBranchPair>();
				falseBranchPairList.add(firstFalseBranch);
				
				// 
				// List of obligations stored in firstBranchPairList.
				// Each obligation contains list of (branch - true/false/NA) nodes.
				//
				CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> firstBranchPairList = new CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>>();
				firstBranchPairList.add(trueBranchPairList);
				firstBranchPairList.add(falseBranchPairList);
				
				CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> obligations = getObligations(firstBranchPairList, mccBranchList);
				
				//
				// TODO: Need to handle if a method has more conditions/branches in diff lines
				//
				// Get all the branches which don't exist in any obligations.
				//
				while(isAnyBranchDoesNotExistsInAnyObligation(mccBranchList, obligations)) {
					ArrayList<MccBranch> mccBranchListForNextSetOfObligations = new ArrayList<MccBranch>();
					for(MccBranch b: mccBranchList) {
						
						if(isBranchExistsInAnyObligation(b, obligations)) {
							continue;
						}
						else {
							mccBranchListForNextSetOfObligations.add(b);
						}
						//System.out.println("Branch(True, False):"+b.getBranchName()+" : "+ b.getTrueBranch()+ " : "+b.getFalseBranch());
					}
					
					// prepare first branch pair list
					MccBranchPair fstTrueBranch = new MccBranchPair();
					fstTrueBranch.setBranchName(mccBranchListForNextSetOfObligations.get(0).getBranchName());
					fstTrueBranch.setBranch(mccBranchListForNextSetOfObligations.get(0).getBranch());
					fstTrueBranch.setConditionStatus(1); // 1 == true
					
					// start with (first branch - true) &  (first branch - false) obligation
					MccBranchPair fstFalseBranch = new MccBranchPair();
					fstFalseBranch.setBranchName(mccBranchListForNextSetOfObligations.get(0).getBranchName());
					fstTrueBranch.setBranch(mccBranchListForNextSetOfObligations.get(0).getBranch());
					fstFalseBranch.setConditionStatus(0); // 0 == false
					
					CopyOnWriteArrayList<MccBranchPair> trBranchPairList = new CopyOnWriteArrayList<MccBranchPair>();
					trBranchPairList.add(fstTrueBranch);
					CopyOnWriteArrayList<MccBranchPair> faBranchPairList = new CopyOnWriteArrayList<MccBranchPair>();
					faBranchPairList.add(fstFalseBranch);
					
					// 
					// List of obligations stored in firstBranchPairList.
					// Each obligation contains list of (branch - true/false/NA) nodes.
					//
					CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> fstBranchPairList = new CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>>();
					fstBranchPairList.add(trBranchPairList);
					fstBranchPairList.add(faBranchPairList);
					
					CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> temp_obligations = getObligations(fstBranchPairList, mccBranchListForNextSetOfObligations);
					
					obligations.addAll(temp_obligations);
				}
				if(!MccCoverageFactory.mccTestObligations.containsValue(obligations)){
				MccCoverageFactory.mccTestObligations.put(methodName, obligations);
				printObligations();
				}

			}
		}
		

	}
	
	
	
	
	private static ArrayList<MccBranch> getMccBranchList(ArrayList<MccBranchInfo> mccBranchInfoList) {
		ArrayList<MccBranch> result = new ArrayList<MccBranch>();
		
		for(MccBranchInfo bInfo : mccBranchInfoList) {
			Branch b = bInfo.getBranch();
			String branchName = bInfo.getBranchName();
			String whereLabel = bInfo.getBranchName();
			String trueLabel = bInfo.getLabelForTrue();
			String falseLabel = bInfo.getLabelForFalse();
			
			MccBranch mccBranch = new MccBranch();
			mccBranch.setBranchName(branchName);
			mccBranch.setBranch(b);
			
			for(MccBranchInfo temp : mccBranchInfoList) {
				if(temp.getBranchName() != branchName) {
					if(trueLabel!=null && trueLabel.equals(temp.getLabelForWhere())) {
						mccBranch.setTrueBranch(temp.getBranchName());
						break;
					}
					else {
						continue;
					}
				}
			}
			
			for(MccBranchInfo temp : mccBranchInfoList) {
				if(temp.getBranchName() != branchName) {
					if(falseLabel.equals(temp.getLabelForWhere())) {
						mccBranch.setFalseBranch(temp.getBranchName());
						break;
					}
					else {
						continue;
					}
				}
			}
			result.add(mccBranch);
		}
		
		return result;
	}
	
	private static ArrayList<MccBranchInfo> getMccBranchInfoList(ArrayList<String> instsForMethod) {
		ArrayList<MccBranchInfo> result = new ArrayList<MccBranchInfo>();
		
		String prevLabel;
		String nextLabel;
		
		for(String inst : instsForMethod) {
			// check for the branch inst first
			if(isBranchInst(inst)) {
				int index = instsForMethod.indexOf(inst);
				MccBranchInfo mccBranchInfo = new MccBranchInfo();
				// get the branch name
				String branchName = getBranchName(inst);
				mccBranchInfo.setBranchName(branchName);
				
				Branch b = mccInstruction.get(inst);
				mccBranchInfo.setBranch(b);
				
				
				//System.out.println("----branchName:::"+branchName);
				
				// get the label for where
				String labelForWhere = getLabelforWhere(instsForMethod, index);
				mccBranchInfo.setLabelForWhere(labelForWhere);
				
				// get the label for true
				String labelForTrue = getlabelForTrue(inst);
				mccBranchInfo.setLabelForTrue(labelForTrue);
				//System.out.println("----labelForTrue:::"+labelForTrue);
				
				// get the lable for false 
				String labelForFalse = getLabelForFalse(instsForMethod, index);
				mccBranchInfo.setLabelForFalse(labelForFalse);
				if(!isMccBranchInfoExists(result, mccBranchInfo.getBranchName()))
					result.add(mccBranchInfo);
			}
			else {
				// label (labelForWhere)
				prevLabel = inst;
			}
		}
		return result;
	}
	
	private static String getBranchName(String inst) {
		String result = "";
		
		if(inst.contains("Branch") && inst.substring(inst.indexOf("Branch")).contains("IF_")) {
			
			result = inst.substring(inst.indexOf("Branch"), inst.indexOf("IF_"));
		}
		else if(inst.contains("Branch") && inst.substring(inst.indexOf("Branch")).contains("IFGT")) {
			result = inst.substring(inst.indexOf("Branch"), inst.indexOf("IFGT"));
		}
		else if(inst.contains("Branch") && inst.substring(inst.indexOf("Branch")).contains("IFGE")) {
			result = inst.substring(inst.indexOf("Branch"), inst.indexOf("IFGE"));
		}
		else if(inst.contains("Branch") && inst.substring(inst.indexOf("Branch")).contains("IFLE")) {
			result = inst.substring(inst.indexOf("Branch"), inst.indexOf("IFLE"));
		}
		else if(inst.contains("Branch") && inst.substring(inst.indexOf("Branch")).contains("ICMPGT")){
			result = inst.substring(inst.indexOf("Branch"), inst.indexOf("ICMPGT"));
		}
		else if(inst.contains("Branch") && inst.substring(inst.indexOf("Branch")).contains("ICMPLT")){
			result = inst.substring(inst.indexOf("Branch"), inst.indexOf("ICMPLT"));
		}
		else {
			System.out.println("In MCC coverage: Wrong instruction for branchName...:"+inst);
		}
		return result.trim();
	}
	
	private static String getlabelForTrue(String inst) {
		String result = null;
		
		if(inst.contains("jump to")) {
			result = inst.substring(inst.indexOf("jump to")+7);
		
		}
		else {
			System.out.println("In MCC coverage: Wrong instruction for branch...:"+inst);
		}
		return result==null?result:result.trim();
	}
	
	private static String getLabelforWhere(ArrayList<String> insts, int index) {
		String result = "";
		//System.out.println("Index------>"+index);
		for(int i = index; i >= 0; i--) {
			String temp = insts.get(i);
			//System.out.println("For where inst("+i+"):::"+temp);
			if(!isBranchInst(temp)) {
				String s = temp.substring(temp.indexOf("LABEL")+5);
				result = s.trim();
				return result;
			}
			else
				continue;
		}		
		return result.trim();
	}
	
	private static String getLabelForFalse(ArrayList<String> insts, int index) {
		String result = "";
		//System.out.println("Index------>"+index);
		
		if(!isBranchInst(insts.get(index+1))) {
			String temp = insts.get(index+1);
			temp = temp.substring(temp.indexOf("LABEL")+5);
			result = temp.trim();
			return result;
		}
		else {	
			for(int i = index; i >= 0; i--) {
				String temp = insts.get(i);
				//System.out.println("For false inst("+i+"):::"+temp);
				if(!isBranchInst(temp)) {
					String s = temp.substring(temp.indexOf("LABEL")+5);
					result = s.trim();
					return result;
				}
				else
					continue;
			}
		}
		return result.trim();
	}
	
	private static boolean isBranchInst(String inst){
		boolean result = false;
		if(inst.contains("Branch"))
			result = true;
		return result;
	}
	
	private static boolean isMccBranchInfoExists(ArrayList<MccBranchInfo> list, String branchName) {
		boolean result = false;
		for(MccBranchInfo info: list) {
			if(info.getBranchName().equals(branchName)) {
				result = true;
				return result;
			}
			else 
				continue;
		}
		return result;
	}
	
	private static CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> getObligations(
			CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> obligationsList,
			ArrayList<MccBranch> mccBranchList ) {
		
		boolean isEnd = true;
		for(CopyOnWriteArrayList<MccBranchPair> obligation : obligationsList) {
			// get the last BP in the obligation list and check for status
			// If status is true(1) or false(0) then set isEnd flag as flase and iterate.
			
			MccBranchPair pair = obligation.get(obligation.size() - 1);
			String nextBranchName = getNextBranchName(pair, mccBranchList);
			
			if(nextBranchName != null ) {
				isEnd = false;
				break;
			}
			else 
				continue;					
		}
		
		// if all obligations are having end node then return the list
		if(isEnd) {
			return obligationsList;
		}
		boolean iterateFlag = true;
		
		while(iterateFlag) {
			iterateFlag = false;
			int index = 0;
			//CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> temp = obligationsList; 
			for(CopyOnWriteArrayList<MccBranchPair> obligation : obligationsList) {
				
				// get the last BP in the obligation list and check for status
				// If status is true(1) or false(0) then set isEnd flag as flase and iterate.
				MccBranchPair pair = obligation.get(obligation.size() - 1);
				String nextBranchName = getNextBranchName(pair, mccBranchList);
				
				if(nextBranchName != null ) {
					// then add true & false BPs to the obligationList.
					
					MccBranchPair falseBp = new MccBranchPair();
					falseBp.setBranchName(nextBranchName);
					falseBp.setConditionStatus(0); // 0 == false
					
					CopyOnWriteArrayList<MccBranchPair> falseBranchPairList = new CopyOnWriteArrayList<MccBranchPair>();
					falseBranchPairList.addAll(obligation);
					falseBranchPairList.add(falseBp);
					obligationsList.add(falseBranchPairList);	
					
					MccBranchPair trueBp = new MccBranchPair();
					trueBp.setBranchName(nextBranchName);
					trueBp.setConditionStatus(1); // for true
					obligation.add(trueBp);
					
					iterateFlag = true;
					
				}
				else if(nextBranchName == null ) {
					//continue;
					// do nothing
				}
				index++;
			}
		}
		return obligationsList;
	}
	
	private static String getNextBranchName(MccBranchPair pair, ArrayList<MccBranch> mccBranchList) {
		if(pair == null || pair.getBranchName() == null || mccBranchList == null || mccBranchList.size() <= 0) {
			return null;
		}
		
		String branchName = pair.getBranchName();		
		int status = pair.getConditionStatus();
		
		for(MccBranch b: mccBranchList) {	
			if(b.getBranchName().equals(branchName)) {
				if(status == 0) { //false  = so look for false branch in mccBranchList
					if(!b.getFalseBranch().equals("NA"))
						return b.getFalseBranch();
					else 
						return null;
				}
				else if(status == 1) { //true  = so look for true branch in mccBranchList
					if(!b.getTrueBranch().equals("NA"))
						return b.getTrueBranch();
					else
						return null;
				}
				else if(status == 2) {
					return null;
				}
			}
			else
				continue;
		}
		return null;		
	}
	
	private static boolean isAnyBranchDoesNotExistsInAnyObligation(ArrayList<MccBranch> mccBranchList, CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> obligations) {
		boolean result = false;
		for(MccBranch b: mccBranchList) {
			String bName = b.getBranchName();
			boolean isBranchExists = false;
			
			for(CopyOnWriteArrayList<MccBranchPair> obligation : obligations) {
				
				for(MccBranchPair bp : obligation) {
					if(bName.equals(bp.getBranchName())) {
						isBranchExists = true;
						break;
					}
					else {
						continue;
					}
				}
				if(isBranchExists)
					break;
				else
					continue;
			}
			if(!isBranchExists) {
				result = true;
				break;
			}
			else {
				continue;
			}			
		}
		return result;
	}
	
	private static boolean isBranchExistsInAnyObligation(MccBranch mccBranch, CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> obligations) {
		boolean result = false;
		String bName = mccBranch.getBranchName();
	
		for(CopyOnWriteArrayList<MccBranchPair> obligation : obligations) {
			for(MccBranchPair bp : obligation) {
				if(bName.equals(bp.getBranchName())) {
					result = true;
					break;
				}
				else {
					continue;
				}
			}
			if(result)
				break;
			else
				continue;
		}
		return result;
	}
	
	private static void printObligations(){		
		for(String methodName : mccTestObligations.keySet()) {
			CopyOnWriteArrayList<CopyOnWriteArrayList<MccBranchPair>> obligationsForMethod = mccTestObligations.get(methodName);
			System.out.println("------"+methodName);
			int noOfObligations = obligationsForMethod.size();
			System.out.println(" No of obligations for MCC:"+noOfObligations);
			int cnt=1;
			for(CopyOnWriteArrayList<MccBranchPair> obligation : obligationsForMethod) {
				System.out.println(" Obligation:"+cnt++);
				for(MccBranchPair bp : obligation) {
					String status = "NA";
					if(bp.getConditionStatus() == 0) 
						status = "false";
					else
						status = "true";
					System.out.print(" :: "+bp.getBranchName()+"-"+status);
					
				}
				System.out.println("\n------------------------------------");
			}
		}
	}

	
}
