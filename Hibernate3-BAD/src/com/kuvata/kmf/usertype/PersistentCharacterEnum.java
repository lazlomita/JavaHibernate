/*
 * Created on Sep 20, 2004
 * Copyright 2004, Kuvata, Inc.
 */
package com.kuvata.kmf.usertype;

import org.hibernate.Hibernate;
import org.hibernate.type.NullableType;

/**
 * Provides a base class for persistable, type-safe, comparable,
 * and serializable enums persisted as characters.
 *
 * <p>Create a subclass of this class implementing the enumeration:
 * <pre>package com.foo;
 *
 * public final class Gender extends PersistentCharacterEnum {
 *  public static final Gender MALE = new Gender("male", 'M');
 *  public static final Gender FEMALE = new Gender("female", 'F');
 *  public static final Gender UNDETERMINED = new Gender("undetermined", 'U');
 *
 *  public Gender() {}
 *
 *  private Gender(String name, char persistentValue) {
 *   super(name, persistentValue);
 *  }
 * }
 * </pre>
 * Note that a no-op default constructor must be provided.</p>
 *
 * <p>Use this enumeration in your mapping file as:
 * <pre>&lt;property name="gender" type="com.foo.Gender"&gt;</pre></p>
 *
 * <p><code>
 * $Id: PersistentCharacterEnum.java,v 1.1 2004/05/31 06:52:28 austvold Exp $
 * </pre></p>
 *
 * @version $Revision: 1.1 $
 * @author &Oslash;rjan Nygaard Austvold
 */
public abstract class PersistentCharacterEnum extends PersistentEnum {
    /**
     * Default constructor.  Hibernate need the default constructor
     * to retrieve an instance of the enum from a JDBC resultset.
     * The instance will be converted to the correct enum instance
     * in {@link #nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)}.
     */
    protected PersistentCharacterEnum() {
        // no-op -- instance will be tossed away once the equivalent enum is found.
    }


    /**
     * Constructs an enum with the given name and persistent representation.
     *
     * @param name name of enum.
     * @param persistentCharacter persistent representation of the enum.
     */
    protected PersistentCharacterEnum(String name, char persistentCharacter) {
        super(name, new Character(persistentCharacter));
    }


    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object other) {
        if (other == this) {
            return 0;
        }
        return ((Character) getEnumCode()).compareTo((Character)((PersistentEnum) other).getEnumCode());
    }


    /**
     * @see PersistentEnum#getNullableType()
     */
    protected NullableType getNullableType() {
        return Hibernate.CHARACTER;
    }
}
