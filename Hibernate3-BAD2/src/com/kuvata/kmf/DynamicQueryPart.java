package com.kuvata.kmf;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;

import com.kuvata.kmf.usertype.AttrType;
import com.kuvata.kmf.usertype.DateType;
import com.kuvata.kmf.usertype.SearchInterfaceType;

import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.logging.HistorizableCollectionMember;

public class DynamicQueryPart extends Entity implements HistorizableCollectionMember{
	
	private Long dynamicQueryPartId;
	private Playlist playlist;
	private ContentRotation contentRotation;
	private AttrDefinition attrDefinition;
	private String operator;
	private String value;
	private Date selectedDate;
	private Integer numDaysAgo;
	private Boolean includeNull;
	private Integer seqNum;
	
	public static DynamicQueryPart create(Playlist playlist, ContentRotation contentRotation, AttrDefinition attrDefinition, String operator, String value, Date selectedDate, Integer numDaysAgo, Boolean includeNull, int seqNum){
		DynamicQueryPart dqp = new DynamicQueryPart();
		dqp.playlist = playlist;
		dqp.contentRotation = contentRotation;
		dqp.attrDefinition = attrDefinition;
		dqp.operator = operator;
		dqp.value = value;
		dqp.selectedDate = selectedDate;
		dqp.numDaysAgo = numDaysAgo;
		dqp.includeNull = includeNull;
		dqp.seqNum = seqNum;
		dqp.save();
		return dqp;
	}
	
	public static DynamicQueryPart getDynamicQueryPart(Long dynamicQueryPartId){
		Session session = HibernateSession.currentSession();				
		return (DynamicQueryPart)session.createCriteria(DynamicQueryPart.class)
				.add( Expression.eq("dynamicQueryPartId", dynamicQueryPartId) )		
				.uniqueResult();
	}
	
	public static List<DynamicQueryPart> getDynamicQueryParts(Playlist playlist){
		Session session = HibernateSession.currentSession();				
		return session.createCriteria(DynamicQueryPart.class)
				.add( Expression.eq("playlist", playlist) )		
				.addOrder(Order.asc("seqNum")).list();
	}
	
	public static List<DynamicQueryPart> getDynamicQueryParts(ContentRotation contentRotation){
		Session session = HibernateSession.currentSession();				
		return session.createCriteria(DynamicQueryPart.class)
				.add( Expression.eq("contentRotation", contentRotation) )		
				.addOrder(Order.asc("seqNum")).list();
	}
	
	public static List<DynamicQueryPart> getDynamicQueryParts(AttrDefinition ad){
		Session session = HibernateSession.currentSession();				
		return session.createCriteria(DynamicQueryPart.class)
				.add( Expression.eq("attrDefinition.attrDefinitionId", ad.getAttrDefinitionId()) ).list();
	}
	
	public static String generateHql(List<DynamicQueryPart> queryParts, Date csStartDate){
		String hql = "";
		String hqlHead = "";
		String hqlTail = "";
		if(queryParts.size() > 0){

			int i = 0;
			hqlHead = "SELECT asset FROM Asset as asset WHERE asset.assetId IN \n (SELECT DISTINCT a.assetId FROM Asset as a";
			for(DynamicQueryPart dqp : queryParts){
				
				if(i > 0){
					hqlTail += "\nAND ";
				}
				
				hqlTail += "(";
				
				// If a String attr was selected
				if( dqp.getAttrDefinition().getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
				{
					boolean doEscape = false;
					
					if(dqp.getIncludeNull() == null || dqp.getIncludeNull() == false){
						hqlHead += ", StringAttr as s" + i;
					}
					
					// If this is a multi-select attrDefinition
					if( dqp.getAttrDefinition().getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) ){
						String operator = dqp.getOperator().equals("=") ? "IN" : "NOT IN";
						
						String values = "";
						if(dqp.getValue() != null){
							String[] parts = dqp.getValue().split("~");
							for(String s : parts){
								if(values.length() > 0){
									values += ", ";
								}
								
								// Escape '
								if(s.contains("'")){
									s = s.replaceAll("'", "''");
								}
								
								values += "'" + s + "'";
							}
						}else{
							values = "''";
						}
						
						if(dqp.getIncludeNull() != null && dqp.getIncludeNull()){
							hqlTail += "a.assetId IN (SELECT sa.ownerId FROM StringAttr as sa WHERE sa.attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND sa.value " + operator + " (" + values + ") ) " +
									" OR a.assetId NOT IN (SELECT sa.ownerId FROM StringAttr as sa WHERE sa.attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND sa.ownerId IS NOT NULL) ";
						}else{
							hqlTail += "a.assetId = s" + i + ".ownerId AND s" + i + ".attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND s" + i + ".value " + operator + " (" + values + ") ";
						}
					}
					// If this is a memo attrDefinition
					else if(dqp.getAttrDefinition().getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MEMO.getPersistentValue())){
						String operator = dqp.getOperator().equals("=") ? "LIKE" : "NOT LIKE";
						
						// Escape _ and %
						String s = dqp.getValue() != null ? dqp.getValue() : "";
						if(s.contains("_") || s.contains("%")){
							doEscape = true;
							s = s.replaceAll("_", "~_");
							s = s.replaceAll("%", "~%");
						}
						
						if(dqp.getIncludeNull() != null && dqp.getIncludeNull()){
							hqlTail += "a.assetId IN (SELECT sa.ownerId FROM StringAttr as sa WHERE sa.attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND sa.value " + operator + " '" + s.replaceAll("'", "''").replaceAll("\\*", "%") + "' ) " +
									" OR a.assetId NOT IN (SELECT sa.ownerId FROM StringAttr as sa WHERE sa.attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + ") ";
						}else{
							hqlTail += "a.assetId = s" + i + ".ownerId AND s" + i + ".attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND s" + i + ".value " + operator + " '" + s.replaceAll("'", "''").replaceAll("\\*", "%") + "' ";
						}
					}
					// If this is an input box
					else{						
						String operator = dqp.getOperator().equals("=") ? "LIKE" : "NOT LIKE";
						
						// Escape _ and %
						String s = dqp.getValue() != null ? dqp.getValue() : "";
						if(s.contains("_") || s.contains("%")){
							doEscape = true;
							s = s.replaceAll("_", "~_");
							s = s.replaceAll("%", "~%");
						}
						
						if(dqp.getIncludeNull() != null && dqp.getIncludeNull()){
							hqlTail += "a.assetId IN (SELECT sa.ownerId FROM StringAttr as sa WHERE sa.attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND sa.value " + operator + " '" + s.replaceAll("'", "''").replaceAll("\\*", "%") + "' ) " +
									" OR a.assetId NOT IN (SELECT sa.ownerId FROM StringAttr as sa WHERE sa.attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + ") ";
						}else{
							hqlTail += "a.assetId = s" + i + ".ownerId AND s" + i + ".attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND s" + i + ".value " + operator + " '" + s.replaceAll("'", "''").replaceAll("\\*", "%") + "' ";
						}
					}
					
					if(doEscape){
						hqlTail += "ESCAPE '~' ";
					}
				}
				// If a Number attr was selected
				else if( dqp.getAttrDefinition().getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) )
				{
					if(dqp.getIncludeNull() == null || dqp.getIncludeNull() == false){
						hqlHead += ", NumberAttr as n" + i;
					}
					
					if(dqp.getIncludeNull() != null && dqp.getIncludeNull()){
						hqlTail += "a.assetId IN (SELECT na.ownerId FROM NumberAttr as na WHERE na.attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND na.value " + dqp.getOperator() + " " + dqp.getValue() + ") " +
								" OR a.assetId NOT IN (SELECT na.ownerId FROM NumberAttr as na WHERE na.attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + ") ";
					}else{
						hqlTail += "a.assetId = n" + i + ".ownerId AND n" + i + ".attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND n" + i + ".value " + dqp.getOperator() + " " + dqp.getValue() + " ";
					}
				}
				// If a Date attr was selected
				else if( dqp.getAttrDefinition().getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) )
				{
					// Use today if no date was passed in
					Calendar dateToUse = Calendar.getInstance();
					if(csStartDate != null){
						dateToUse.setTime(csStartDate);
					}else{
						dateToUse.set(Calendar.HOUR, 0);
						dateToUse.set(Calendar.MINUTE, 0);
						dateToUse.set(Calendar.SECOND, 0);
						dateToUse.set(Calendar.MILLISECOND, 0);
					}
					
					if(dqp.getIncludeNull() == null || dqp.getIncludeNull() == false){
						hqlHead += ", DateAttr as d" + i;
					}
					
					SimpleDateFormat outputFormat = new SimpleDateFormat("yyyyMMdd");
					String operator = dqp.getOperator();
					String value = "";
					if(dqp.getValue().equals(DateType.CURRENT_DAY.getPersistentValue())){
						value = outputFormat.format(dateToUse.getTime());
					}else if(dqp.getValue().equals(DateType.DAYS_AGO.getPersistentValue())){
						dateToUse.add(Calendar.DATE, -dqp.getNumDaysAgo());
						value = outputFormat.format(dateToUse.getTime());
					}else if(dqp.getValue().equals(DateType.MONTH_TO_DATE.getPersistentValue())){
						// Month start
						Calendar monthStart = (Calendar)dateToUse.clone();
						monthStart.set(Calendar.DATE, 1);
						
						if(operator.equals("=")){
							value = " >= '" + outputFormat.format(monthStart.getTime()) + "' AND to_char(d" + i + ".value, 'YYYYMMDD') <= '" + outputFormat.format(dateToUse.getTime()) + "' ";
						}else if(operator.equals("!=")){
							value = " < '" + outputFormat.format(monthStart.getTime()) + "' OR to_char(d" + i + ".value, 'YYYYMMDD') > '" + outputFormat.format(dateToUse.getTime()) + "' ";
						}
						operator = "";
					}else if(dqp.getValue().equals(DateType.PREVIOUS_MONTH.getPersistentValue())){
						// Previous month start
						Calendar start = (Calendar)dateToUse.clone();
						start.add(Calendar.MONTH, -1);
						start.set(Calendar.DATE, 1);
						
						// Previous month end
						Calendar end = (Calendar)start.clone();
						end.set(Calendar.DATE, end.getActualMaximum(Calendar.DAY_OF_MONTH));
						
						if(operator.equals("=")){
							value = " >= '" + outputFormat.format(start.getTime()) + "' AND to_char(d" + i + ".value, 'YYYYMMDD') <= '" + outputFormat.format(end.getTime()) + "' ";
						}else if(operator.equals("!=")){
							value = " < '" + outputFormat.format(start.getTime()) + "' OR to_char(d" + i + ".value, 'YYYYMMDD') > '" + outputFormat.format(end.getTime()) + "' ";
						}
						operator = "";
					}else if(dqp.getValue().equals(DateType.NEXT_MONTH.getPersistentValue())){
						// Next month start
						Calendar start = (Calendar)dateToUse.clone();
						start.add(Calendar.MONTH, 1);
						start.set(Calendar.DATE, 1);
						
						// Next month end
						Calendar end = (Calendar)start.clone();
						end.set(Calendar.DATE, end.getActualMaximum(Calendar.DAY_OF_MONTH));
						
						if(operator.equals("=")){
							value = " >= '" + outputFormat.format(start.getTime()) + "' AND to_char(d" + i + ".value, 'YYYYMMDD') <= '" + outputFormat.format(end.getTime()) + "' ";
						}else if(operator.equals("!=")){
							value = " < '" + outputFormat.format(start.getTime()) + "' OR to_char(d" + i + ".value, 'YYYYMMDD') > '" + outputFormat.format(end.getTime()) + "' ";
						}
						operator = "";
					}else if(dqp.getValue().equals(DateType.SELECTED_DATE.getPersistentValue())){
						value = "'" + outputFormat.format(dqp.getSelectedDate()) + "'";
					}
					
					if(dqp.getIncludeNull() != null && dqp.getIncludeNull()){
						hqlTail += "a.assetId IN (SELECT d" + i + ".ownerId FROM DateAttr as d" + i + " WHERE d" + i + ".attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND ( to_char(d" + i + ".value, 'YYYYMMDD') " + operator + " " + value + " ) ) " +
								" OR a.assetId NOT IN (SELECT d" + i + ".ownerId FROM DateAttr as d" + i + " WHERE d" + i + ".attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + ") ";
					}else{
						hqlTail += "a.assetId = d" + i + ".ownerId AND d" + i + ".attrDefinition.attrDefinitionId = " + dqp.getAttrDefinition().getAttrDefinitionId() + " AND ( to_char(d" + i + ".value, 'YYYYMMDD') " + operator + " " + value + " ) ";
					}
				}
				
				hqlTail += ")";
				i++;
			}
			
			hql = hqlHead + " WHERE \n" + hqlTail + ")";
		}
		
		return hql;
	}
	
	public String getEntityName(){
		return playlist != null ? playlist.getPlaylistName() : contentRotation.getContentRotationName();
	}
	public Long getHistoryEntityId(){
		return playlist != null ? playlist.getPlaylistId() : contentRotation.getContentRotationId();
	}
	public Long getEntityId(){
		return dynamicQueryPartId;
	}

	public Long getDynamicQueryPartId() {
		return dynamicQueryPartId;
	}

	public void setDynamicQueryPartId(Long dynamicQueryPartId) {
		this.dynamicQueryPartId = dynamicQueryPartId;
	}

	public Playlist getPlaylist() {
		return playlist;
	}

	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}

	public ContentRotation getContentRotation() {
		return contentRotation;
	}

	public void setContentRotation(ContentRotation contentRotation) {
		this.contentRotation = contentRotation;
	}

	public AttrDefinition getAttrDefinition() {
		return attrDefinition;
	}

	public void setAttrDefinition(AttrDefinition attrDefinition) {
		this.attrDefinition = attrDefinition;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(Date selectedDate) {
		this.selectedDate = selectedDate;
	}

	public Integer getNumDaysAgo() {
		return numDaysAgo;
	}

	public void setNumDaysAgo(Integer numDaysAgo) {
		this.numDaysAgo = numDaysAgo;
	}

	public Boolean getIncludeNull() {
		return includeNull;
	}

	public void setIncludeNull(Boolean includeNull) {
		this.includeNull = includeNull;
	}

	public Integer getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(Integer seqNum) {
		this.seqNum = seqNum;
	}
}