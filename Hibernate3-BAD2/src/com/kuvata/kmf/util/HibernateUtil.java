package com.kuvata.kmf.util;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public class HibernateUtil 
{	
	public static Object convert(Object object) 
	{ 
		if (object instanceof HibernateProxy)  { 
			HibernateProxy proxy = (HibernateProxy) object; 
	        LazyInitializer li = proxy.getHibernateLazyInitializer(); 
	        return li.getImplementation(); 
		}  else { 
			return object; 
		}       
	} 
}
