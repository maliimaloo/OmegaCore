package net.omegagames.core.persistanceapi.utils;

import org.mineacademy.fo.command.annotation.Permission;
import org.mineacademy.fo.exception.FoException;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Transcoder
{
    // Remove dash to insert in the database
    public static String encode(String uuid) {
        uuid = uuid.replace("-","");
        return uuid;
    }

    // Put lower case and add dash for uuid
    public static String decode(String uuid) {
        // Regexp to format uuid
        uuid = uuid.toLowerCase();
        uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        return uuid;
    }

    // Get the permissions to a HashMap
    public static Map<String, Boolean> getHashMapPerm(Object permissions)
    {
        Map<String, Boolean> result = new HashMap<>();
        try {
            //Iterate class fields
            for (Field field : permissions.getClass().getDeclaredFields()) {
                //Check if annotations present
                if (field.isAnnotationPresent(Permission.class)) {
                    // Make the private field accessible
                    field.setAccessible(true);
                    // Add to HashMap with correct value
                    result.put(field.getAnnotation(Permission.class).value(), field.getBoolean(permissions));
                }
            }
        }
        catch (Exception exception) {
            throw new FoException(exception);
        }

        return result;
    }

    // Set the annotations values
    public static void setAnnotationValue(Object permissions, String key, Boolean value) {
        try {
            //Iterate class fields
            for (Field field : permissions.getClass().getDeclaredFields()) {
                // Make the private field accessible
                field.setAccessible(true);
                // Check if annotations present and equal the key
                if (field.isAnnotationPresent(Permission.class) && field.getAnnotation(Permission.class).value().equals(key)) {
                    field.setBoolean(permissions, value);
                    break;
                }
            }
        }
        catch (Exception exception) {
            throw new FoException(exception);
        }
    }
}
