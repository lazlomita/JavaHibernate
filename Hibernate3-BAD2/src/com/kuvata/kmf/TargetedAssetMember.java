package com.kuvata.kmf;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.kuvata.kmf.usertype.SearchInterfaceType;

import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.HibernateUtil;
import com.kuvata.kmf.util.Reformat;

public class TargetedAssetMember extends PersistentEntity implements HistorizableCollectionMember
{
	private Long targetedAssetMemberId;
	private IAsset targetedAsset;	
	private IAsset asset;
	private Device device;
	private Grp deviceGrp;
	private AttrDefinition attrDefinition;
	private String attrValue;
	private String hql;
	private Integer seqNum;
	
	/**
	 * 
	 *
	 */
	public TargetedAssetMember()
	{		
	}
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getTargetedAssetMemberId();
	}
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getTargetedAsset().getAssetId();
	}
	/**
	 * 
	 */
	public String getEntityName()
	{
		String result = this.getAsset().getAssetName() +": ";
		if( this.getDevice() != null ){
			result += this.getDevice().getDeviceName();
		}else if( this.getDeviceGrp() != null ){
			result += this.getDeviceGrp().getGrpName() + " (DG)";
		}else if( this.getAttrDefinition() != null ){
			result += "Metadata: " + this.getAttrDefinition().getAttrDefinitionName() +": "+ this.getAttrValue();
		}else if( this.getHql() != null ){
			result += "HQL: " + this.getHql();
		}
		return result;
	}	
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof TargetedAssetMember) ) result = false;
		
		TargetedAssetMember de = (TargetedAssetMember) other;		
		if(this.hashCode() == de.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "TargetedAssetMember".hashCode();
		result = Reformat.getSafeHash(this.getTargetedAssetMemberId(), result, 3);
		result = Reformat.getSafeHash(this.getTargetedAsset().getAssetId(), result, 5);
		result = Reformat.getSafeHash(this.getAsset().getAssetId(), result, 7);		
		result = Reformat.getSafeHash(this.getDevice(), result, 9);
		result = Reformat.getSafeHash(this.getSeqNum(), result, 11);
		return result < 0 ? -result : result;
	}
	
	/**
	 * 
	 * @param deviceName
	 * @return
	 * @throws HibernateException
	 */	
	public static TargetedAssetMember getTargetedAssetMember(Long targetedAssetMemberId) throws HibernateException
	{
		return (TargetedAssetMember)PersistentEntity.load(TargetedAssetMember.class, targetedAssetMemberId);		
	}	
	
	public static TargetedAssetMember create(IAsset targetedAsset, IAsset asset, Device device, Integer seqNum){
		return create( targetedAsset, asset, device, null, null, null, null, seqNum );
	}
	
	public static TargetedAssetMember create(IAsset targetedAsset, IAsset asset, Grp deviceGrp, Integer seqNum){
		return create( targetedAsset, asset, null, deviceGrp, null, null, null, seqNum );
	}	
	
	public static TargetedAssetMember create(IAsset targetedAsset, IAsset asset, AttrDefinition attrDefinition, String attrValue, Integer seqNum){
		return create( targetedAsset, asset, null, null, attrDefinition, attrValue, null, seqNum );
	}		
	
	/**
	 * Creates a TargetedAssetMember object.
	 * @param targetedAsset
	 * @param asset
	 * @param device
	 * @param deviceGrp
	 * @param attrDefinition
	 * @param attrValue
	 * @param seqNum
	 * @return
	 */
	public static TargetedAssetMember create(IAsset targetedAsset, IAsset asset, Device device, Grp deviceGrp, AttrDefinition attrDefinition, String attrValue, String hql, Integer seqNum)
	{
		TargetedAssetMember tam = new TargetedAssetMember();
		tam.setTargetedAsset( targetedAsset );
		tam.setAsset( asset );
		
		if( device != null ){
			tam.setDevice(device);
		}
		else if( deviceGrp != null ){
			tam.setDeviceGrp( deviceGrp );
		}
		else if( attrDefinition != null && attrValue != null){
			tam.setAttrDefinition( attrDefinition );
			
			// If there are multiple multi-select items selected
			if(attrDefinition.getSearchInterface().equals(SearchInterfaceType.MULTI_SELECT) && attrValue.contains("~")){
				// Trim white-spaces from each value
				String[] valueParts = attrValue.split("~");
				attrValue = "";
				for(String s : valueParts){
					if(attrValue.length() > 0){
						attrValue += "~";
					}
					attrValue += s.trim();
				}
			}
			
			tam.setAttrValue( attrValue );
		}
		else if( hql != null ){
			tam.setHql(hql);
		}
		tam.setSeqNum(seqNum);
		tam.save();
		return tam;
	}
	
	public static List<TargetedAssetMember> getStaticTargetedAssetMembers(Device d){
		Session session = HibernateSession.currentSession();
		String hql = "Select tam from TargetedAssetMember as tam WHERE tam.device.deviceId = ?";
		return session.createQuery(hql).setParameter(0, d.getDeviceId()).list();
	}
	
	public static List<TargetedAssetMember> getTargetedAssetMembers(AttrDefinition ad){
		Session session = HibernateSession.currentSession();
		String hql = "Select tam from TargetedAssetMember as tam WHERE tam.attrDefinition.attrDefinitionId = ?";
		return session.createQuery(hql).setParameter(0, ad.getAttrDefinitionId()).list();
	}
	
	/**
	 * @return Returns the asset.
	 */
	public IAsset getAsset() {		
		return (IAsset)HibernateUtil.convert( asset );
	}
	
	/**
	 * @param asset The asset to set.
	 */
	public void setAsset(IAsset asset) {
		this.asset = asset;
	}
	
	/**
	 * @return Returns the seqNum.
	 */
	public Integer getSeqNum() {
		return seqNum;
	}
	
	/**
	 * @param seqNum The seqNum to set.
	 */
	public void setSeqNum(Integer seqNum) {
		this.seqNum = seqNum;
	}
	public Long getTargetedAssetMemberId() {
		return targetedAssetMemberId;
	}
	public void setTargetedAssetMemberId(Long targetedAssetMemberId) {
		this.targetedAssetMemberId = targetedAssetMemberId;
	}
	public IAsset getTargetedAsset() {
		return targetedAsset;
	}
	public void setTargetedAsset(IAsset targetedAsset) {
		this.targetedAsset = targetedAsset;
	}
	public Device getDevice() {
		return device;
	}
	public void setDevice(Device device) {
		this.device = device;
	}
	/**
	 * @return the deviceGrp
	 */
	public Grp getDeviceGrp() {
		return deviceGrp;
	}
	/**
	 * @param deviceGrp the deviceGrp to set
	 */
	public void setDeviceGrp(Grp deviceGrp) {
		this.deviceGrp = deviceGrp;
	}
	/**
	 * @return the attrDefinition
	 */
	public AttrDefinition getAttrDefinition() {
		return attrDefinition;
	}
	/**
	 * @param attrDefinition the attrDefinition to set
	 */
	public void setAttrDefinition(AttrDefinition attrDefinition) {
		this.attrDefinition = attrDefinition;
	}
	/**
	 * @return the attrValue
	 */
	public String getAttrValue() {
		return attrValue;
	}
	/**
	 * @param attrValue the attrValue to set
	 */
	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}
	public String getHql() {
		return hql;
	}
	public void setHql(String hql) {
		this.hql = hql;
	}
}
