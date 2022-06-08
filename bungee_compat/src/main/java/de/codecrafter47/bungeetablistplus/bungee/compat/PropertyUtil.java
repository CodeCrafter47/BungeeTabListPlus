package de.codecrafter47.bungeetablistplus.bungee.compat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public class PropertyUtil {

    /**
     * Used to safely get the property arrays from objects that used to return String[][] properties and now do Property[]
     * @param object object to get properties from, should implement a getProperties() method
     * @return String[][] array of properties, as seen in >=1.18
     */
    public static String[][] getProperties(Object object) {
        try {
            Method getProperties = object.getClass().getMethod("getProperties");
            Object properties = getProperties.invoke(object); //Get using reflection to not lock return type
            if(properties instanceof String[][]) {
                //Pre 1.19, just return value
                return (String[][]) properties;
            } else if(bungeeProtocolPropertyClass().isPresent()) {
                //Post 1.19, convert property class using reflection to keep backwards compat
                Object[] propertiesObjects = (Object[]) properties;
                String[][] newProperties = new String[propertiesObjects.length][3];
                for (int i = 0; i < propertiesObjects.length; i++) {
                    Object p = propertiesObjects[i];
                    newProperties[i] = VersionSafeProperty.makeFrom(p, bungeeProtocolPropertyClass().get()).toArray();
                }
                return newProperties;
            } else {
                throw new RuntimeException("Unsupported Version!");
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used to convert the LoginResult.Property or protocol.Property to a version safe equivalent
     * @param propertyHolder object implementing getProperties(), returning either LoginResult.Property[] or protocol.Property[]
     * @return Version safe equivalent
     */
    public static VersionSafeProperty[] convertLoginProperties(Object propertyHolder) {
        try {
            Method getProperties = propertyHolder.getClass().getMethod("getProperties");
            Object propertiesOrNull = getProperties.invoke(propertyHolder); //Get using reflection to not lock return type
            if(propertiesOrNull == null) return null;
            Object[] properties = (Object[]) propertiesOrNull;
            VersionSafeProperty[] vsProps = new VersionSafeProperty[properties.length];
            for (int i = 0; i < properties.length; i++) {
                vsProps[i] = VersionSafeProperty.makeFrom(properties[i], getProperties.getReturnType().getComponentType());
            }
            return vsProps;
        } catch(ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the property of an object with either a setProperties(String[][]) or setProperties(Property[]) method
     * @param properties 1.18-style property array
     * @param object object implementing either setProperties(String[][]) or setProperties(Property[])
     */
    public static void safelySetProperties(String[][] properties, Object object) {
        try {
            if (bungeeProtocolPropertyClass().isPresent()) {
                Class<?> bungeeProtocolPropertyClass = bungeeProtocolPropertyClass().get();
                Object[] newPropArray = (Object[]) Array.newInstance(bungeeProtocolPropertyClass, properties.length);
                for (int i = 0; i < properties.length; i++) {
                    newPropArray[i] = new VersionSafeProperty(properties[i]).convertTo(bungeeProtocolPropertyClass);
                }

                Method setPropertyMethod = object.getClass().getMethod("setProperties", newPropArray.getClass());
                setPropertyMethod.invoke(object, (Object) newPropArray);
            } else {
                Method setPropertyMethod = object.getClass().getMethod("setProperties", String[][].class);
                setPropertyMethod.invoke(object, (Object) properties);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<Class<?>> bungeeProtocolPropertyClass() {
        try {
            return Optional.of(Class.forName("net.md_5.bungee.protocol.Property"));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    @Data
    @AllArgsConstructor
    public static class VersionSafeProperty
    {
        private String name;
        private String value;
        private String signature;

        private static VersionSafeProperty makeFrom(Object property, Class<?> propertyClass) throws ReflectiveOperationException {
            Method nameMethod = propertyClass.getDeclaredMethod("getName");
            Method valueMethod = propertyClass.getDeclaredMethod("getValue");
            Method sigMethod = propertyClass.getDeclaredMethod("getSignature");

            return new VersionSafeProperty((String) nameMethod.invoke(property), (String) valueMethod.invoke(property), (String) sigMethod.invoke(property));
        }

        private VersionSafeProperty (String[] properties) {
            this(properties[0], properties[1], properties.length >= 3 ? properties[2] : null);
        }

        public String[] toArray() {
            return new String[]{name, value, signature};
        }

        private Object convertTo(Class<?> clazz) throws ReflectiveOperationException{
            Constructor<?> constructor = clazz.getDeclaredConstructor(String.class, String.class, String.class);
            return constructor.newInstance(name, value, signature);
        }
    }
}
