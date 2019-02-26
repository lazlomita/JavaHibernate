/*
 * Created on Sep 20, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf.usertype;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.type.NullableType;
import org.hibernate.usertype.UserType;


/**
 * Provides a base class for implementations of persistable, type-safe,
 * comparable and serializable enums with a custom persisted representation.
 *
 * <p>The subclass must provide a compareTo(Object) and getNullableType()
 * implementation.</p>
 *
 * <p><code>
 * $Id: PersistentEnum.java,v 1.1 2004/05/31 06:52:28 austvold Exp $
 * </pre></p>
 *
 * @version $Revision: 1.1 $
 * @author &Oslash;rjan Nygaard Austvold
 */
abstract class PersistentEnum implements Comparable, Serializable, UserType {
    /**
     * <code>Map</code> where key is of class name, value is of <code>Map</code>.
     * where key is of enumCode and value is of enum instance.
     */
    private static final Map enumClasses = new HashMap();
    /**
     * The identifying enum code.
     */
    protected Serializable enumCode;
    /**
     * The name of the enumeration. Used as toString result.
     * Public so it can be serialized
     *     */
    protected String name;
    /**
     * The hashcode representation of the enum.
     */
    protected transient int hashCode;


    /**
     * Default constructor.  Hibernate need the default constructor
     * to retrieve an instance of the enum from a JDBC resultset.
     * The instance will be converted to the correct enum instance
     * in {@link #nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)}.
     */
    public PersistentEnum() {
        // no-op -- instance will be tossed away once the equivalent enum is found.
    }


    /**
     * Constructs a new enumeration instance with the given name and persisted
     * representation of enumCode.
     *
     * @param name name of the enum instance.
     * @param enumCode persisted enum representation.
     */
    protected PersistentEnum(String name, Serializable enumCode) {
        this.name = name;
        this.enumCode = enumCode;
        hashCode = 7 + returnedClass().hashCode() + 3 * enumCode.hashCode();

        Class enumClass = returnedClass();
        Map entries = (Map) enumClasses.get(enumClass);
        if (entries == null) {
            entries = new HashMap();
            enumClasses.put(enumClass, entries);
        }
        if (entries.containsKey(enumCode)) {
            throw new IllegalArgumentException("The enum code must be unique, '"
                    + enumCode + "' has already been added");
        }
        entries.put(enumCode, this);
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    public final int hashCode() {
        return hashCode;
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public final boolean equals(Object other) {
        
        //@author J.Brown 31.08.2004
        //An instanceof check is probably wise..
 
        if (!(other instanceof PersistentEnum)) {
            return false;
        }

        if (other == this) {
            return true;

        } else if (other == null) {
            return false;

        } else if (((PersistentEnum) other).returnedClass().getName().equals(returnedClass().getName())) {
            // different classloaders
            try {
                // try to avoid reflection
                return enumCode.equals(((PersistentEnum) other).enumCode);

            } catch (ClassCastException ex) {
                // use reflection
                try {
                    Method mth = other.getClass().getMethod("getEnumCode", (Class[])null);
                    Serializable enumCode = (Serializable) mth.invoke(other, (Object[])null);
                    return this.enumCode.equals(enumCode);
                } catch (Exception ignore) { // NoSuchMethod-, IllegalAccess-, InvocationTargetException
                }
                return false;
            }
        } else {
            return false;
        }
    }


    /**
     * Gets the persistable enum code of this enum.
     *
     * @return the enum code.
     */
    public final Serializable getEnumCode() {
        return enumCode;
    }


    /**
     * Resolves this enumeration into a already staticly instantiated enum.
     *
     * @return the type-safe enum equivalent to this enumeration.
     */
    protected Object readResolve() {
        Map entries = (Map) enumClasses.get(returnedClass());
        return (entries != null) ? entries.get(enumCode) : null;
    }


    /**
     * Gets the collection of enumeration instances of a given
     * enumeration class.
     *
     * @param enumClass enumeration class type.
     * @return collection of enumerations of the given class.
     */
    protected static Collection getEnumCollection(Class enumClass) {
        Map entries = (Map) enumClasses.get(enumClass);
        return (entries != null)
                ? Collections.unmodifiableCollection(entries.values())
                : Collections.EMPTY_LIST;
    }


    /**
     * @see Comparable#compareTo(Object)
     */
    public abstract int compareTo(Object other);


    /**
     * Gets the Hibernate type of the persisted representation of the enum.
     *
     * @return the Nullable Hibernate type.
     */
    protected abstract NullableType getNullableType();


    /**
     *  @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return new int[]{getNullableType().sqlType()};
    }


    /**
     * Simply return the enums name.
     *
     * @return the string representation of this enum.
     */
    public String toString() {
        return name;
    }


    /**
     *  @see net.sf.hibernate.UserType#deepCopy(java.lang.Object)
     */
    public Object deepCopy(Object value) throws HibernateException {
        // Enums are immutable - nothing to be done to deeply clone it
        return value;
    }


    /**
     *  @see net.sf.hibernate.UserType#isMutable()
     */
    public boolean isMutable() {
        // Enums are immutable
        return false;
    }


    /**
     * @see net.sf.hibernate.UserType#returnedClass()
     * @return
     */
    public Class returnedClass() {
        return this.getClass();
    }

    public boolean equals(Object x, Object y) throws HibernateException { 
        if (x == y) 
            return true; 
        if (null == x || null == y) 
            return false; 
        return x.equals(y); 
    } 

    /**
     *  @see net.sf.hibernate.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        Serializable enumCode = (Serializable) getNullableType().nullSafeGet(rs, names[0]);
        Map entries = (Map) enumClasses.get(returnedClass());
        return (PersistentEnum) ((entries != null)
                ? entries.get(enumCode)
                : null);
    }


    /**
     * @see net.sf.hibernate.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        
    	Serializable enumCode = null;
    	
    	// make sure the received value is of the right type
        if ((value != null) && !returnedClass().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Received value is not a [" +
                    returnedClass().getName() + "] but [" + value.getClass() + "]");
        }

        // JSR 21/16/04 -- allow null values
        if(value != null)
        {
        	// convert the enum into its persistence format
        	enumCode = ((PersistentEnum) value).getEnumCode();
        }

        // set the value into the resultset
        st.setObject(index, enumCode, getNullableType().sqlType());
    }
	
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }
	
    public int hashCode(Object x) throws HibernateException { 
        return x.hashCode(); 
    } 
	
    public Object replace(Object original, Object target, Object owner) throws HibernateException { 
        return original; 
    } 
    public Serializable disassemble(Object value) throws HibernateException { 
        return (Serializable)value; 
    } 
}

