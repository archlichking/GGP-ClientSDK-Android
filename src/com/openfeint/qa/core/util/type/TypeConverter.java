
package com.openfeint.qa.core.util.type;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;

public class TypeConverter {

    /**
     * Helper class for type aliases
     */
    private final static TypeAliases typeAliases = new TypeAliases();

    /**
     * Converts a string value into an object of the given type.
     * 
     * @param value value string
     * @param type type string
     * @return the constructed object
     * @throws TypeConversionException in case of any errors
     */
    public static Object convertSimpleType(Object value, Type type) throws TypeConversionException {
        try {
            return createSimpleTypeInstance(value, type);
        } catch (Exception e) {
            throw new TypeConversionException(e);
        }
    }

    /**
     * Converts a complex type in an assert or param instance into an object of
     * the given type.
     * 
     * @param typeInstance assert or param instance
     * @param type type string
     * @return the constructed object
     * @throws TypeConversionException in case of any errors
     */
    protected Object _convertType(Object value, Type type) throws TypeConversionException {
        try {
            return objectMap(value, type);
        } catch (Exception e) {
            throw new TypeConversionException(e);
        }
    }

    /**
     * Converts a complex type in an assert or param instance into an object of
     * the given type.
     * 
     * @param abstractType assert or param instance
     * @param type type string
     * @return the constructed object
     * @throws TypeConversionException in case of any errors
     */
    private Object objectMap(Object val, Type type) throws TypeConversionException {

        if (!typeAliases.isSimpleType(type))
            return null;

        try {
            return createSimpleTypeInstance(val, type);
        } catch (ParseException e) {
            throw new TypeConversionException(e);
        }
    }

    /**
     * Converts a string value into an object of the given type.
     * 
     * @param val value string
     * @param type type string
     * @return the constructed object
     * @throws ParseException in case of errors in parsing the string value
     * @throws TypeConversionException in case of any errors
     */
    private static Object createSimpleTypeInstance(Object val, Type type) throws ParseException,
            TypeConversionException {
        Object newVal = null;

        DateFormat lDateFormat = null;
        DateFormat lTimeFormat = null;

        lDateFormat = DateFormat.getDateInstance();
        lTimeFormat = DateFormat.getDateTimeInstance();

        String concreteType;
        try {
            concreteType = typeAliases.getType(type.toString());
        } catch (TypeConversionException e) {
            throw new TypeConversionException("Not type alias for " + type);
        }

        if (concreteType.equals(TypeAliases.INTEGER)) {
            try {
                newVal = new Integer(val.toString());
            } catch (NumberFormatException e) {
                throw new TypeConversionException("Error conveting to integer: " + type);
            }
        } else if (concreteType.equals(TypeAliases.SHORT)) {
            try {
                newVal = new Short(val.toString());
            } catch (NumberFormatException e) {
                throw new TypeConversionException("Error converting to short: " + type);
            }
        } else if (concreteType.equals(TypeAliases.LONG)) {
            try {
                newVal = new Long(val.toString());
            } catch (NumberFormatException e) {
                throw new TypeConversionException("Error converting to long: " + type);
            }
        } else if (concreteType.equals(TypeAliases.CHAR)) {
            newVal = new Character(val.toString().charAt(0));
        } else if (concreteType.equals(TypeAliases.BYTE)) {
            try {
                newVal = new Byte(val.toString());
            } catch (NumberFormatException e) {
                throw new TypeConversionException("Error converting to byte: " + type);
            }
        } else if (concreteType.equals(TypeAliases.DOUBLE)) {
            try {
                newVal = new Double(val.toString());
            } catch (NumberFormatException e) {
                throw new TypeConversionException("Error converting to double: " + type);
            }
        } else if (concreteType.equals(TypeAliases.FLOAT)) {
            try {
                newVal = new Float(val.toString());
            } catch (NumberFormatException e) {
                throw new TypeConversionException("Error converting to float : " + type);
            }
        } else if (concreteType.equals(TypeAliases.BOOLEAN)) {
            admittedValuesForBoolean(val.toString());
            newVal = new Boolean(val.toString());
        } else if (concreteType.equals(TypeAliases.DATE)) {
            try {
                newVal = lDateFormat.parse(val.toString());
            } catch (ParseException e) {
                throw new TypeConversionException("Error parsing date: " + type);
            }
        } else if (concreteType.equals(TypeAliases.TIME)) {
            try {
                newVal = lTimeFormat.parse(val.toString());
            } catch (ParseException e) {
                throw new TypeConversionException("Error parsing timedate : " + type);
            }
        } else if (concreteType.equals(TypeAliases.STRING)) {
            if (val.equals(TypeAliases.STRING_EMPTY)) {
                newVal = "";
            } else if (val.equals(TypeAliases.STRING_SPACE)) {
                newVal = " ";
            } else {
                // default string type to literal value
                newVal = val;
            }
        }
        return newVal;

    }

    private static void admittedValuesForBoolean(String booleanValue)
            throws TypeConversionException
    // admitted value for boolean types are false,true (ignoring case)
    // all else throws exception
    {
        if ("false".equalsIgnoreCase(booleanValue) || "true".equalsIgnoreCase(booleanValue))
            return;
        else
            throw new TypeConversionException("Error converting to boolean : " + booleanValue);
    }

}
