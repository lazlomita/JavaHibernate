package com.kuvata.kmf.asset;

import java.util.Set;
import com.kuvata.kmf.usertype.DefaultAssetAffinityType;
import com.kuvata.kmf.IAsset;
import com.kuvata.kmf.TargetedAssetMember;

public interface ITargetedAsset extends IAsset
{
	public DefaultAssetAffinityType getDefaultAssetAffinityType();
	public Set<TargetedAssetMember> getTargetedAssetMembers();	
}
