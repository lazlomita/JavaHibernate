/*
 * Created on Aug 20, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf;

import java.lang.reflect.Method;

/**
 * Comment here
 * 
 * @author Jeff Randesi
 */
public interface TreeViewable {
	/**
	 * 
	 * @param includeLeaves
	 * @param includeHref
	 * @param includeDoubleClick
	 * @param doubleClickLeavesOnly
	 * @param treeNodeCssClass
	 * @param allBranchMethod
	 * @return
	 */
	public String treeViewFormat(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod);
	
}
