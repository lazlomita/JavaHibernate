package com.kuvata.kmf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;

import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.logging.HistorizableSet;

/**
 * 
 * 
 * Persistent class for table GRP.
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class Grp extends Entity implements Historizable {

	private Long grpId;
	private String grpName;
	private Set<GrpMember> grpMembers = new HistorizableSet<GrpMember>();
	private Set<GrpGrpMember> grpGrpMembers = new HashSet<GrpGrpMember>();
		
	/**
	 * Constructor
	 */
	public Grp()
	{		
	}
	/**
	 * 
	 * @param grpId
	 * @return
	 * @throws HibernateException
	 */
	public static Grp getGrp(Long grpId) throws HibernateException
	{
		return (Grp)Entity.load(Grp.class, grpId);		
	}	
	
	/**
	 * Implementation of the inherited abstract method Entity.getEntityId().
	 * Returns the grpId.
	 */
	public Long getEntityId()
	{
		return this.getGrpId();
	}
		
	/**
	 * This static method retrieves a persistent Grp object with the given grpName,
	 * and invokes the given formatter method of that Grp object. The result is
	 * a String of the given Grp's data and all of it's children so as to build
	 * a tree structure in the given formatter format. 
	 * 
	 * @param grpName				Name of the root group in which to build the tree from
	 * @param includeLeaves			Flag to include the leaves (bottom-most level) of the tree
	 * @param formatter				Method to invoke when formatting the tree				
	 * @param allBranchMethod		Method to invoke if including the "All" branch of the tree
	 * @return
	 * @throws HibernateException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */
	public static String getTree(String grpName, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method formatter, Method allBranchMethod) 
		throws HibernateException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException
	{
		Grp root = Grp.getUniqueGrp( grpName );

		// If we did not find a group with the given grpName -- exit
		if(root == null)
			return null;
				
		// Invoke the given formatter method on the root grp object
		StringBuffer result = new StringBuffer();
		Object[] formatterParams = { Integer.valueOf(0), Boolean.valueOf(includeLeaves), Boolean.valueOf(includeAllLeaves), Boolean.valueOf(includeHref), Boolean.valueOf(includeDoubleClick), Boolean.valueOf(doubleClickLeavesOnly), treeNodeCssClass, allBranchMethod };
		result.append( (String)formatter.invoke(root, formatterParams) );		
		return result.toString();
	}

	/**
	 * Returns a persistent Grp object for the given grpName.
	 * This method should only be called when you are guaranteed to
	 * only return one grp object. 
	 * For example, Constants.DEVICE_GROUPS, Constants.PLAYLIST_GROUPS, etc.
	 * 
	 * @param grpName
	 * @return
	 * @throws HibernateException
	 */
	public static Grp getUniqueGrp(String grpName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		Grp g = (Grp)session.createCriteria(Grp.class)				
				.add( Expression.eq("grpName", grpName).ignoreCase() )
				.uniqueResult();				
		return g;
	}	
	
	/**
	 * Returns a persistent Grp object for the given grpName
	 * 
	 * @param grpName
	 * @return
	 * @throws HibernateException
	 */
	public static List<Grp> getGrps(String grpName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		List<Grp> l = session.createCriteria(Grp.class)				
				.add( Restrictions.eq("grpName", grpName).ignoreCase() )
				.list();				
		return l;
	}	
	
	/**
	 * Returns a persistent Grp object for the given grpName
	 * 
	 * @param grpName
	 * @return
	 * @throws HibernateException
	 */
	public static List getGrps(String rootGroupName, String childGrpName) throws HibernateException
	{
		Session session = HibernateSession.currentSession();	
		String hql = "SELECT childGrp FROM GrpGrpMember ggm "
			+ "INNER JOIN ggm.childGrp as childGrp "
			+ "INNER JOIN ggm.grp as grp "
			+ "WHERE grp.grpName = :rootGrpName "
			+ "AND childGrp.grpName = :childGrpName";			
		return session.createQuery( hql )
			.setParameter("rootGrpName", rootGroupName )
			.setParameter("childGrpName", childGrpName )
			.list();
	}		
	
	/**
	 * Recursive method used to build the String of data needed for the 
	 * com.kuvata.kmf.TreeNodes tag class. The resulting String is formatted
	 * so that the TreeNodes tag class can render a javascript TreeView
	 * control correctly in the browser.  
	 * 
	 * @param includeLeaves		Flag to include the bottom-most level of the tree
	 * @param allBranchMethod	Method to invoke if including the "All" branch of the tree
	 */
	public String treeViewFormat(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod)
	{
		StringBuffer result = new StringBuffer();
		String onClick = "null";
		String onDoubleClick = "";
		String grpTypeSuffix = "";
		
		try
		{				
			// Since this is a generic method, we have to convert the treeNodeCssClass to the appropriate suffix
			if( treeNodeCssClass.equalsIgnoreCase( Constants.TYPE_PLAYLIST_GROUP ) ){
				grpTypeSuffix = Constants.PLAYLIST_GROUP_SUFFIX;
			}
			
			if( treeNodeCssClass.equalsIgnoreCase( Constants.TYPE_DEVICE_GROUP ) ){
				grpTypeSuffix = Constants.DEVICE_GROUP_SUFFIX;
			}
			
			if( ( includeHref ) && (doubleClickLeavesOnly == false) )
			{
				onClick = "\'javascript:grpOnClick("+ this.getGrpId() +")\'";
			}	
			if( ( includeDoubleClick ) && (doubleClickLeavesOnly == false) )
			{
				onDoubleClick = " onDblClick=\\\"grpOnDoubleClick("+ this.getGrpId() +", '"+ grpTypeSuffix +"')\\\"";	
			}				


			result.append("[");
			result.append("{id:"+ this.getGrpId() +"}, \"<span class=\\\"treeNodeGrp\\\""+ onDoubleClick +">"+ this.getGrpName() + "</span>\", "+ onClick +", null, "+ treeNodeCssClass +",\n");		
		
			if(allBranchMethod != null)
			{
				// Invoke the given allBranchMethod to build the "All" branch of the tree				
				Object[] params = { Boolean.valueOf(includeHref), Boolean.valueOf(includeDoubleClick), Boolean.valueOf(doubleClickLeavesOnly), Boolean.valueOf(includeAllLeaves) };
				result.append( (String)allBranchMethod.invoke(null, params) );
			}

			if (recursionLevel == 0) {
				List children = this.getGrpMembersSorted();
				int numChildren = children.size();			
				for(int i=0; i < numChildren; i++)
				{
					// Get the child of the current GrpMember object
					GrpMember thisChild = (GrpMember)children.get(i);	
					Entity child = thisChild.getChild();
					
					// Invoke the treeViewFormat method of the child object
					Class[] methodParamTypes = { int.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, String.class, Method.class };
					Method m = Class.forName(child.getClass().getName()).getDeclaredMethod("treeViewFormat", methodParamTypes);				
					Object[] formatterParams = { Integer.valueOf(++recursionLevel), Boolean.valueOf(includeLeaves), Boolean.valueOf(includeAllLeaves), Boolean.valueOf(includeHref), Boolean.valueOf(includeDoubleClick), Boolean.valueOf(doubleClickLeavesOnly), treeNodeCssClass, null };				
					result.append( (String)m.invoke(child, formatterParams) );
	
					// Limit the number of child nodes
					/*if( includeLeaves && includeAllLeaves && i >= Constants.MAX_CHILD_NODES - 1 ){
						
						// Append a "more" child node and break
						result.append("[");					
						result.append("{id:0}, \"<span class=\\\"treeNode\\\">...more</span>\", null, null, "+ treeNodeCssClass +",");
						result.append("],\n");
						break;
					}*/
				}	
			}
			result.append("],");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
		return result.toString();
	}
	
	/**
	 * Recursive method used to build the String of data needed for the 
	 * com.kuvata.kmf.TreeNodes tag class. The resulting String is formatted
	 * so that the TreeNodes tag class can render a javascript TreeView
	 * control correctly in the browser.  
	 * 
	 * @param includeLeaves		Flag to include the bottom-most level of the tree
	 * @param allBranchMethod	Method to invoke if including the "All" branch of the tree
	 */
	public String treeViewFormatSchedule(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod)
	{
		StringBuffer result = new StringBuffer();
	
		try
		{		
			String onClick = "null";	
			String onDoubleClick = "";
			if(( includeHref ) && (doubleClickLeavesOnly == false) )
			{
				onClick="\'javascript:grpOnClick("+ this.getGrpId() +")\'";	
			}		
			if(( includeDoubleClick ) && (doubleClickLeavesOnly == false) )
			{
				onDoubleClick = " onDblClick=\\\"grpOnDoubleClick("+ this.getGrpId() +")\\\"";	
			}	
			
			result.append("[");		
			result.append("{id:"+ this.getGrpId() +"}, \"<span class=\\\"treeNodeGrp\\\""+ onDoubleClick +">"+ this.getGrpName() + "</span>\", "+ onClick +", null, type_device_group,\n");		
		
			if(allBranchMethod != null)
			{
				// Invoke the given allBranchMethod to build the "All" branch of the tree				
				Object[] params = { Boolean.valueOf(includeHref), Boolean.valueOf(includeDoubleClick), Boolean.valueOf(doubleClickLeavesOnly), Boolean.valueOf(includeAllLeaves) };
				result.append( (String)allBranchMethod.invoke(null, params) );
			}
			
			if (recursionLevel == 0) {
				List children = this.getGrpMembersSorted();
				for(int i=0; i < children.size(); i++)
				{
					// Get the child of the current GrpMember object
					GrpMember thisChild = (GrpMember)children.get(i);	
					Entity child = thisChild.getChild();	
							
					// Invoke the treeViewFormat method of the child object
					Class[] methodParamTypes = { int.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, String.class, Method.class };
					Method m = Class.forName(child.getClass().getName()).getDeclaredMethod("treeViewFormatSchedule", methodParamTypes);
					Object[] formatterParams = { Integer.valueOf(++recursionLevel), Boolean.valueOf(includeLeaves), Boolean.valueOf(includeAllLeaves), Boolean.valueOf(includeHref), Boolean.valueOf(includeDoubleClick), Boolean.valueOf(doubleClickLeavesOnly), treeNodeCssClass, null };				
					result.append( (String)m.invoke(child, formatterParams) );
					
					// Limit the number of child nodes
					/*if( i >= Constants.MAX_CHILD_NODES - 1 ){
						
						// Append a "more" child node and break
						result.append("[");					
						result.append("{id:0}, \"<span class=\\\"treeNode\\\">...more</span>\", null, null, "+ treeNodeCssClass +",");
						result.append("],\n");
						break;
					}*/						
				}
			}
			result.append("],\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
		return result.toString();
	}	
	
	/**
	 * Recursive method used to build the String of data needed for the 
	 * com.kuvata.kmf.TreeNodes tag class. The resulting String is formatted
	 * so that the TreeNodes tag class can render a javascript TreeView
	 * control correctly in the browser.  
	 * 
	 * @param includeLeaves		Flag to include the bottom-most level of the tree
	 * @param allBranchMethod	Method to invoke if including the "All" branch of the tree
	 */
	public String treeViewFormatMasterDevicesOnly(int recursionLevel, boolean includeLeaves, boolean includeAllLeaves, boolean includeHref, boolean includeDoubleClick, boolean doubleClickLeavesOnly, String treeNodeCssClass, Method allBranchMethod)
	{
		StringBuffer result = new StringBuffer();
	
		try
		{		
			String onClick = "";	
			String onDoubleClick = "";
			if(( includeHref ) && (doubleClickLeavesOnly == false) )
			{
				onClick = "\'javascript:grpOnClick("+ this.getGrpId() +")\'";					
			}		
			if(( includeDoubleClick ) && (doubleClickLeavesOnly == false) )
			{
				onDoubleClick = " onDblClick=\\\"grpOnDoubleClick("+ this.getGrpId() +", '"+ Constants.DEVICE_GROUP_SUFFIX +"')\\\"";	
			}	
			result.append("[");		
			result.append("{id:"+ this.getGrpId() +"}, \"<span class=\\\"treeNodeGrp\\\""+ onDoubleClick +">"+ this.getGrpName() + "</span>\", "+ onClick +", null, type_device_group,\n");		
		
			if(allBranchMethod != null)
			{
				// Invoke the given allBranchMethod to build the "All" branch of the tree				
				Object[] params = { Boolean.valueOf(includeHref), Boolean.valueOf(includeDoubleClick), Boolean.valueOf(doubleClickLeavesOnly), Boolean.valueOf(includeAllLeaves) };
				result.append( (String)allBranchMethod.invoke(null, params) );
			}
			
			if (recursionLevel == 0) {
				List children = this.getGrpMembersSorted();		
				for(int i=0; i < children.size(); i++)
				{
					// Get the child of the current GrpMember object
					GrpMember thisChild = (GrpMember)children.get(i);	
					Entity child = thisChild.getChild();	
							
					// Invoke the treeViewFormat method of the child object
					Class[] methodParamTypes = { int.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, String.class, Method.class };
					Method m = Class.forName(child.getClass().getName()).getDeclaredMethod("treeViewFormatMasterDevicesOnly", methodParamTypes);
					Object[] formatterParams = { Integer.valueOf(++recursionLevel), Boolean.valueOf(includeLeaves), Boolean.valueOf(includeAllLeaves), Boolean.valueOf(includeHref), Boolean.valueOf(includeDoubleClick), Boolean.valueOf(doubleClickLeavesOnly), treeNodeCssClass, null };				
					result.append( (String)m.invoke(child, formatterParams) );
					
					// Limit the number of child nodes
					/*if( i >= Constants.MAX_CHILD_NODES - 1 ){
						
						// Append a "more" child node and break
						result.append("[");					
						result.append("{id:0}, \"<span class=\\\"treeNode\\\">...more</span>\", null, null, "+ treeNodeCssClass +",");
						result.append("],\n");
						break;
					}*/		
				}
			}
			result.append("],\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	
		return result.toString();
	}		
	
	/**
	 * Returns true if a grp with the given name already exists "under" the root grp
	 * 
	 * @param rootGrp
	 * @param grpName
	 * @return
	 */
	public static boolean grpExists(Grp rootGrp, String grpName)
	{
		boolean result = false;
		
		// Get the GrpMembers of the root group
		Iterator i = rootGrp.getGrpMembers().iterator();
		while(i.hasNext())
		{
			// If this GrpMember is of type GrpGrpMember
			GrpMember gm = (GrpMember)i.next();
			if(gm instanceof GrpGrpMember)
			{
				// If this GrpGrpMember's child group has the same group name -- this group already exists
				GrpGrpMember ggm = (GrpGrpMember)gm;					
				if(ggm.getChildGrp().getGrpName().equalsIgnoreCase( grpName ))
						result = true;
			}		
		}		
		return result;
	}
	
	public static HashMap<Long, Long> getNumGrpMembers(List<Long> grpIds){
		HashMap<Long, Long> result = new HashMap();
		if(grpIds.size() > 0){
			Session session = HibernateSession.currentSession();
			String countHql = "SELECT grpMember.grp.grpId, COUNT(grpMember) FROM GrpMember as grpMember WHERE grpMember.grp.grpId IN (:grpIds) GROUP BY grpMember.grp.grpId";
			List<Object[]> l = session.createQuery( countHql ).setParameterList("grpIds", grpIds).list();
			for(Object[] o : l){
				result.put((Long)o[0], (Long)o[1]);
			}
		}
		return result;
	}
	
	public static HashMap<Long, Long> getDeviceNumGrpMembers(List<Long> grpIds){
		HashMap<Long, Long> result = new HashMap();
		if(grpIds.size() > 0){
			Session session = HibernateSession.currentSession();
			String countHql = "SELECT grp.grpId, COUNT(grpMember) FROM GrpMember as grpMember "
					+ "INNER JOIN grpMember.device as node "
					+ "INNER JOIN grpMember.grp as grp "
					+ "WHERE grp.grpId IN (:grpIds) and (node.deleted is null OR node.deleted = 0) GROUP BY grp.grpId";
			
			List<Object[]> l = session.createQuery( countHql ).setParameterList("grpIds", grpIds).list();
			for(Object[] o : l){
				result.put((Long)o[0], (Long)o[1]);
			}
		}
		return result;
	}
	
	public static int getNumGrpMembers(Long grpId){
		Session session = HibernateSession.currentSession();
		String countHql = "SELECT COUNT(grpMember) FROM GrpMember as grpMember WHERE grpMember.grp.grpId = :grpId";
		Object o = session.createQuery( countHql ).setParameter("grpId", grpId).uniqueResult();
		return ((Long)o).intValue();
	}
	
	public static int getNumDeviceGrpMembers(Long grpId){
		Session session = HibernateSession.currentSession();
		String countHql = "SELECT COUNT(node) FROM DeviceGrpMember gm "
				+ "INNER JOIN gm.device as node "
				+ "INNER JOIN gm.grp as grp "
				+ "WHERE grp.grpId=:grpId and (node.deleted is null OR node.deleted = 0) ";
		
		Object o = session.createQuery( countHql ).setParameter("grpId", grpId).uniqueResult();
		return ((Long)o).intValue();
	}
	
	/**
	 * Returns true of false depending upon whether or not the given device
	 * is already a member of this group. 
	 * NOTE: This is currently DeviceGrpMember specific. We could change the
	 * implementation to take an Entity and test for all types of GrpMembers if needed.
	 * @param device
	 * @return
	 */
	public boolean grpMemberExists(Device device)
	{
		boolean result = false;
		
		for( Iterator<GrpMember> i=this.getGrpMembers().iterator(); i.hasNext(); )
		{
			// NOTE: Only checking for DeviceGrpMember
			GrpMember grpMember = i.next();
			if( grpMember instanceof DeviceGrpMember ){
				DeviceGrpMember deviceGrpMember = (DeviceGrpMember)grpMember;
				if( deviceGrpMember.getDevice().getDeviceId().equals( device.getDeviceId() ) ){
					result = true;
					break;
				}
			}
		}
		return result;
	}	
		
	/**
	 * 
	 * @throws HibernateException
	 */
	public void removeGrpMembers() throws HibernateException
	{
		this.grpMembers.clear();
		this.update();
	}	
	/**
	 * 
	 * @throws HibernateException
	 */
	public void removeChildren() throws HibernateException
	{
		Session session = HibernateSession.currentSession();
		HibernateSession.beginTransaction();
		List l = session.createCriteria(GrpMember.class)
				.add( Expression.eq("grp", this) )
				.list();
		for(int i=0; i<l.size(); i++)
		{
			GrpMember gm = (GrpMember)l.get(i);
			session.delete( gm );
		}
		session.flush();	
		HibernateSession.commitTransaction();
	}
	
	/**
	 * Returns a list of grpMembers sorted by name
	 * @return
	 */
	public List getGrpMembersSorted()
	{
		List result = new LinkedList();
		result.addAll( this.grpMembers );
		
		// Sort the children according to their name property
		BeanPropertyComparator comparator = new BeanPropertyComparator("name");
		Collections.sort(result, comparator);
		return result;
	}
	
	public void delete(){
		// Delete any targeted asset members
		HibernateSession.currentSession().createQuery("UPDATE TargetedAssetMember SET deviceGrp.grpId = NULL WHERE deviceGrp.grpId = :grpId").setParameter("grpId", this.grpId).executeUpdate();
		
		super.delete();
	}

	/**
	 * @return Returns the grpId.
	 */
	public Long getGrpId() {
		return grpId;
	}

	/**
	 * @param grpId The grpId to set.
	 */
	public void setGrpId(Long grpId) {
		this.grpId = grpId;
	}

	/**
	 * @return Returns the grpName.
	 */
	public String getGrpName() {
		return grpName;
	}

	/**
	 * @param grpName The grpName to set.
	 */
	public void setGrpName(String grpName) {
		this.grpName = grpName;
	}
	/**
	 * @return the grpMembers
	 */
	public Set<GrpMember> getGrpMembers() {
		return grpMembers;
	}
	/**
	 * @param grpMembers the grpMembers to set
	 */
	public void setGrpMembers(Set<GrpMember> grpMembers) {
		this.grpMembers = grpMembers;
	}
	/**
	 * @return the grpGrpMembers
	 */
	public Set<GrpGrpMember> getGrpGrpMembers() {
		return grpGrpMembers;
	}
	/**
	 * @param grpGrpMembers the grpGrpMembers to set
	 */
	public void setGrpGrpMembers(Set<GrpGrpMember> grpGrpMembers) {
		this.grpGrpMembers = grpGrpMembers;
	}



}
