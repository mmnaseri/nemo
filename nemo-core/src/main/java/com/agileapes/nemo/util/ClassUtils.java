package com.agileapes.nemo.util;

import com.agileapes.nemo.event.Event;
import com.agileapes.nemo.event.EventListener;

import java.lang.reflect.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This is a port of Spring 3.2's ClassUtils class. This is mainly here for the {@link #forName(String, ClassLoader)}
 * utility method.
 *
 * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
 * @since 1.0 (2013/6/10, 19:32)
 */
public abstract class ClassUtils {

    private static final Map<String, Class> primitiveTypes = new HashMap<String, Class>();
    private static final String ARRAY_SUFFIX = "[]";
    private static final String INTERNAL_ARRAY_PREFIX = "[";
    private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

    /** Cache from Class to TypeVariable Map */
    private static final Map<Class, Map<TypeVariable, Type>> typeVariableCache =
            Collections.synchronizedMap(new WeakHashMap<Class, Map<TypeVariable, Type>>());

    static {
        primitiveTypes.put("char", char.class);
        primitiveTypes.put("int", int.class);
        primitiveTypes.put("short", short.class);
        primitiveTypes.put("long", long.class);
        primitiveTypes.put("boolean", boolean.class);
        primitiveTypes.put("float", float.class);
        primitiveTypes.put("double", double.class);
    }

    public static Class forName(String name, ClassLoader classLoader) throws ClassNotFoundException {
        if (name == null) {
            throw new NullPointerException();
        }
        Class<?> clazz = resolvePrimitiveClassName(name);
        // "java.lang.String[]" style arrays
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }
        // "[Ljava.lang.String;" style arrays
        if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
            String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }
        // "[[I" or "[[Ljava.lang.String;" style arrays
        if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
            String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
            Class<?> elementClass = forName(elementName, classLoader);
            return Array.newInstance(elementClass, 0).getClass();
        }
        ClassLoader classLoaderToUse = classLoader;
        if (classLoaderToUse == null) {
            classLoaderToUse = getDefaultClassLoader();
        }
        try {
            return classLoaderToUse.loadClass(name);
        }
        catch (ClassNotFoundException ex) {
            int lastDotIndex = name.lastIndexOf('.');
            if (lastDotIndex != -1) {
                String innerClassName = name.substring(0, lastDotIndex) + '$' + name.substring(lastDotIndex + 1);
                try {
                    return classLoaderToUse.loadClass(innerClassName);
                }
                catch (ClassNotFoundException ex2) {
                    // swallow - let original exception get through
                }
            }
            throw ex;
        }
    }

    private static Class<?> resolvePrimitiveClassName(String name) {
        return primitiveTypes.get(name);
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to system class loader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
        }
        return cl;
    }

    /**
     * Resolve the single type argument of the given generic interface against
     * the given target class which is assumed to implement the generic interface
     * and possibly declare a concrete type for its type variable.
     * @param clazz the target class to check against
     * @param genericIfc the generic interface or superclass to resolve the type argument from
     * @return the resolved type of the argument, or {@code null} if not resolvable
     */
    public static Class<?> resolveTypeArgument(Class<?> clazz, Class<?> genericIfc) {
        Class[] typeArgs = resolveTypeArguments(clazz, genericIfc);
        if (typeArgs == null) {
            return null;
        }
        if (typeArgs.length != 1) {
            throw new IllegalArgumentException("Expected 1 type argument on generic interface [" +
                    genericIfc.getName() + "] but found " + typeArgs.length);
        }
        return typeArgs[0];
    }

    /**
     * Resolve the type arguments of the given generic interface against the given
     * target class which is assumed to implement the generic interface and possibly
     * declare concrete types for its type variables.
     * @param clazz the target class to check against
     * @param genericIfc the generic interface or superclass to resolve the type argument from
     * @return the resolved type of each argument, with the array size matching the
     * number of actual type arguments, or {@code null} if not resolvable
     */
    public static Class[] resolveTypeArguments(Class<?> clazz, Class<?> genericIfc) {
        return doResolveTypeArguments(clazz, clazz, genericIfc);
    }

    private static Class[] doResolveTypeArguments(Class<?> ownerClass, Class<?> classToIntrospect, Class<?> genericIfc) {
        while (classToIntrospect != null) {
            if (genericIfc.isInterface()) {
                Type[] ifcs = classToIntrospect.getGenericInterfaces();
                for (Type ifc : ifcs) {
                    Class[] result = doResolveTypeArguments(ownerClass, ifc, genericIfc);
                    if (result != null) {
                        return result;
                    }
                }
            }
            else {
                try {
                    Class[] result = doResolveTypeArguments(ownerClass, classToIntrospect.getGenericSuperclass(), genericIfc);
                    if (result != null) {
                        return result;
                    }
                }
                catch (MalformedParameterizedTypeException ex) {
                    // from getGenericSuperclass() - return null to skip further superclass traversal
                    return null;
                }
            }
            classToIntrospect = classToIntrospect.getSuperclass();
        }
        return null;
    }

    private static Class[] doResolveTypeArguments(Class<?> ownerClass, Type ifc, Class<?> genericIfc) {
        if (ifc instanceof ParameterizedType) {
            ParameterizedType paramIfc = (ParameterizedType) ifc;
            Type rawType = paramIfc.getRawType();
            if (genericIfc.equals(rawType)) {
                Type[] typeArgs = paramIfc.getActualTypeArguments();
                Class[] result = new Class[typeArgs.length];
                for (int i = 0; i < typeArgs.length; i++) {
                    Type arg = typeArgs[i];
                    result[i] = extractClass(ownerClass, arg);
                }
                return result;
            }
            else if (genericIfc.isAssignableFrom((Class) rawType)) {
                return doResolveTypeArguments(ownerClass, (Class) rawType, genericIfc);
            }
        }
        else if (ifc != null && genericIfc.isAssignableFrom((Class) ifc)) {
            return doResolveTypeArguments(ownerClass, (Class) ifc, genericIfc);
        }
        return null;
    }

    /**
     * Extract a class instance from given Type.
     */
    private static Class<?> extractClass(Class<?> ownerClass, Type arg) {
        if (arg instanceof ParameterizedType) {
            return extractClass(ownerClass, ((ParameterizedType) arg).getRawType());
        }
        else if (arg instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) arg;
            Type gt = gat.getGenericComponentType();
            Class<?> componentClass = extractClass(ownerClass, gt);
            return Array.newInstance(componentClass, 0).getClass();
        }
        else if (arg instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) arg;
            arg = getTypeVariableMap(ownerClass).get(tv);
            if (arg == null) {
                arg = extractBoundForTypeVariable(tv);
            }
            else {
                arg = extractClass(ownerClass, arg);
            }
        }
        return (arg instanceof Class ? (Class) arg : Object.class);
    }

    /**
     * Build a mapping of {@link TypeVariable#getName TypeVariable names} to
     * {@link Class concrete classes} for the specified {@link Class}. Searches
     * all super types, enclosing types and interfaces.
     */
    public static Map<TypeVariable, Type> getTypeVariableMap(Class<?> clazz) {
        Map<TypeVariable, Type> typeVariableMap = typeVariableCache.get(clazz);

        if (typeVariableMap == null) {
            typeVariableMap = new HashMap<TypeVariable, Type>();

            // interfaces
            extractTypeVariablesFromGenericInterfaces(clazz.getGenericInterfaces(), typeVariableMap);

            try {
                // super class
                Class<?> type = clazz;
                while (type.getSuperclass() != null && !Object.class.equals(type.getSuperclass())) {
                    Type genericType = type.getGenericSuperclass();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) genericType;
                        populateTypeMapFromParameterizedType(pt, typeVariableMap);
                    }
                    extractTypeVariablesFromGenericInterfaces(type.getSuperclass().getGenericInterfaces(), typeVariableMap);
                    type = type.getSuperclass();
                }
            }
            catch (MalformedParameterizedTypeException ex) {
                // from getGenericSuperclass() - ignore and continue with member class check
            }

            try {
                // enclosing class
                Class<?> type = clazz;
                while (type.isMemberClass()) {
                    Type genericType = type.getGenericSuperclass();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) genericType;
                        populateTypeMapFromParameterizedType(pt, typeVariableMap);
                    }
                    type = type.getEnclosingClass();
                }
            }
            catch (MalformedParameterizedTypeException ex) {
                // from getGenericSuperclass() - ignore and preserve previously accumulated type variables
            }

            typeVariableCache.put(clazz, typeVariableMap);
        }

        return typeVariableMap;
    }

    /**
     * Extracts the bound {@code Type} for a given {@link TypeVariable}.
     */
    static Type extractBoundForTypeVariable(TypeVariable typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return Object.class;
        }
        Type bound = bounds[0];
        if (bound instanceof TypeVariable) {
            bound = extractBoundForTypeVariable((TypeVariable) bound);
        }
        return bound;
    }

    private static void extractTypeVariablesFromGenericInterfaces(Type[] genericInterfaces, Map<TypeVariable, Type> typeVariableMap) {
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                populateTypeMapFromParameterizedType(pt, typeVariableMap);
                if (pt.getRawType() instanceof Class) {
                    extractTypeVariablesFromGenericInterfaces(
                            ((Class) pt.getRawType()).getGenericInterfaces(), typeVariableMap);
                }
            }
            else if (genericInterface instanceof Class) {
                extractTypeVariablesFromGenericInterfaces(
                        ((Class) genericInterface).getGenericInterfaces(), typeVariableMap);
            }
        }
    }


    /**
     * Read the {@link TypeVariable TypeVariables} from the supplied {@link ParameterizedType}
     * and add mappings corresponding to the {@link TypeVariable#getName TypeVariable name} ->
     * concrete type to the supplied {@link Map}.
     * <p>Consider this case:
     * <pre class="code>
     * public interface Foo<S, T> {
     *  ..
     * }
     *
     * public class FooImpl implements Foo<String, Integer> {
     *  ..
     * }</pre>
     * For '{@code FooImpl}' the following mappings would be added to the {@link Map}:
     * {S=java.lang.String, T=java.lang.Integer}.
     */
    private static void populateTypeMapFromParameterizedType(ParameterizedType type, Map<TypeVariable, Type> typeVariableMap) {
        if (type.getRawType() instanceof Class) {
            Type[] actualTypeArguments = type.getActualTypeArguments();
            TypeVariable[] typeVariables = ((Class) type.getRawType()).getTypeParameters();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type actualTypeArgument = actualTypeArguments[i];
                TypeVariable variable = typeVariables[i];
                if (actualTypeArgument instanceof Class) {
                    typeVariableMap.put(variable, actualTypeArgument);
                }
                else if (actualTypeArgument instanceof GenericArrayType) {
                    typeVariableMap.put(variable, actualTypeArgument);
                }
                else if (actualTypeArgument instanceof ParameterizedType) {
                    typeVariableMap.put(variable, actualTypeArgument);
                }
                else if (actualTypeArgument instanceof TypeVariable) {
                    // We have a type that is parameterized at instantiation time
                    // the nearest match on the bridge method will be the bounded type.
                    TypeVariable typeVariableArgument = (TypeVariable) actualTypeArgument;
                    Type resolvedType = typeVariableMap.get(typeVariableArgument);
                    if (resolvedType == null) {
                        resolvedType = extractBoundForTypeVariable(typeVariableArgument);
                    }
                    typeVariableMap.put(variable, resolvedType);
                }
            }
        }
    }

}
