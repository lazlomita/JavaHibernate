package com.kuvata.kmf.usertype;

import org.hibernate.Hibernate;
import org.hibernate.type.NullableType;

/**
 * @author Lazlo Mita
 * Created on July 26, 2018
 * Copyright 2018, Inception Signage, Inc.
 */
public abstract class PersistentStringEnum extends PersistentEnum {
	
	private String persistentValue;
	
    /**
     * Default constructor.  Hibernate need the default constructor
     * to retrieve an instance of the enum from a JDBC resultset.
     * The instance will be resolved to the correct enum instance
     * in {@link #nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)}.
     */
    protected PersistentStringEnum() {
        // no-op -- instance will be tossed away once the equivalent enum is found.
    }


    /**
     * Constructs an enum with name as the persistent representation.
     *
     * @param name name of the enum.
     */
    protected PersistentStringEnum(String name) {
        super(name, name);
        this.persistentValue = name;
    }


    /**
     * Constructs an enum with the given name and persistent representation.
     *
     * @param name name of enum.
     * @param persistentString persistent representation of the enum.
     */
    protected PersistentStringEnum(String name, String persistentString) {
        super(name, persistentString);
        this.persistentValue = persistentString;
    }


    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object other) {
        if (other == this) {
            return 0;
        }
        return ((String) getEnumCode()).compareTo((String)((PersistentEnum) other).getEnumCode());
    }


    /**
     * @see PersistentEnum#getNullableType()
     */
    protected NullableType getNullableType() {
        return Hibernate.STRING;
    }


	public String getPersistentValue() {
		return persistentValue;
	}
}

