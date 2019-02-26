// Comparator Utilities
// Copyright (c) 2002 Alex Blewitt <Alex.Blewitt@ioshq.com>
// Copyright (c) 2002 JavaWorld
// First published by JavaWorld in December 2002
// http://www.javaworld.com/javaworld/jw-12-2002/jw-1227-sort.html
//
//    This library is free software; you can redistribute it and/or 
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  
//    Alternatively, it is downloadable at http://www.gnu.org/licenses/lgpl.txt
//
//  For more information, see the COPYRIGHT notice in the root directory 

package com.kuvata.kmf.comparator;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

// Note: There is a design decision here to capture any related exceptions
//       that may occur during the processing of this method, and wrap it
//       into an IllegalArgumentException.
//
//       Although it is generally not good practice to wrap all exceptions
//       and pass them back as a string message, the nested exceptions
//       aren't available until Java 1.4, and this should work with Java 1.1+
// 
//       Secondly, the method for accessing the property is supposed to be
//       used dymanically with an editor that knows the existence of the
//       properties given, so that if there is an error accessing it
//       it can be treated as a problem, and whether it is because the get
//       method is private (which would result in a invokation target exception)
//       or whether it does not exist (which would result in the exception)
//       is not particularly important.

/** Provides utility methods for accessing JavaBeans.
 *
 * @author Alex Blewitt &lt;<I><A href="mailto:Alex.Blewitt@ioshq.com">Alex.Blewitt@ioshq.com</A></I>&gt;
 * @version 1.0
 */
public class BeanPropertyUtil {
  /** Implements a static utility class; cannot be instantiated
   */
  private BeanPropertyUtil() {
  }
  /** Stores the list of class/property names with associated read methods
   */
  private static Map map = new WeakHashMap();
  /** Return the value of the JavaBean <CODE>property</CODE> from 
   * <CODE>instance</CODE>.
   *
   * Uses {@link java.beans.Introspector} to obtain the appropriate 
   * {@link java.beans.PropertyDescriptor}, and then the accessor method 
   * is invoked dynamically using {@link java.lang.reflect.Method}.
   *
   * @return Object value of the property.
   *   Primitive types are wrapped automatically by the reflection package.
   * @see java.beans.Introspector
   * @see java.beans.PropertyDescriptor
   * @see java.beans.PropertyDescriptor#getReadMethod
   * @see java.lang.reflect.Method#invoke
   * @param name the property name to look up
   * @param instance the bean instance to use
   * @throws IllegalArgumentException if the <I>name</I> does not exist 
   * in <I>instance</I> or there is an exception dynamically invoking the 
   * accessor method .
   */  
  public static Object getProperty(String name, Object instance)  
    throws IllegalArgumentException {
    try {
      Method read = getReadMethod(name,instance);
      if (read == null) 
    throw new IllegalArgumentException("Cannot find instance with property '" + name + "'");
      return read.invoke(instance); 
    } catch (Exception e) {
      throw new IllegalArgumentException("Problem accessing property '" + name + "': " + e.getMessage());
    }
  }
  /** Obtain the read method for property <em>name</em> associated with <em>instance</em>.
   * Once the method is located, it is stored in {@link #map} using a
   * format <code>fully.qualified.Class#name</code> so that it is
   * obtained quicker on subsequent requests. As the map is a 
   * {@link java.util.WeakHashMap} then memory requests may cause parts
   * of this cache to be lost.
   * @param name the property name
   * @param instance the instance to query
   */
  private static Method getReadMethod(String name, Object instance) 
    throws IllegalArgumentException, IntrospectionException {
    String id = instance.getClass() + "#" + name;
    Method read = (Method) map.get(id);
    if (read == null) {
      // Get the BeanInfo, either from BeanInfo class or reflection
      BeanInfo info = Introspector.getBeanInfo(
        instance.getClass(),Introspector.USE_ALL_BEANINFO);
      // Lookup all of this bean's properties
      PropertyDescriptor pds[] = info.getPropertyDescriptors();
      // Search through the list for the one with 'name'
      for(int i=0;i<pds.length;i++) {
        PropertyDescriptor pd = pds[i];
        if(name.equals(pd.getName())) {
          // and now that we've found it, invoke it with no arguments (null)
          read = pd.getReadMethod();
          map.put(id,read);
          break;
        }
      }
    }
    if (read == null) 
      throw new IllegalArgumentException("Cannot find instance with property '" + name + "'");
    return read;
  }
}
