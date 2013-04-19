package ica.exam;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
 
public class Ksoap2ResultParser {
 
    /**
    * Parses a single business object containing primitive types from the response
    * @param input soap message, one element at a time
    * @param theClass your class object, that contains the same member names and types for the response soap object
    * @return the values parsed
    * @throws NumberFormatException
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws InstantiationException
    */
    public static void parseBusinessObject(String input, Object output) throws NumberFormatException, IllegalArgumentException, IllegalAccessException, InstantiationException{
 
        Class theClass = output.getClass();
        Field[] fields = theClass.getDeclaredFields();
       
        for (int i = 0; i < fields.length; i++) {
            Type type=fields[i].getType();
            fields[i].setAccessible(true);
 
            //detect String
            if (fields[i].getType().equals(String.class)) {
                String tag = "s" + fields[i].getName() + "=";   //"s" is for String in the above soap response example + field name for example Name = "sName"
                if(input.contains(tag)){
                    String strValue = input.substring(input.indexOf(tag)+tag.length(), input.indexOf(";", input.indexOf(tag)));
                    if(strValue.length()!=0){
                        fields[i].set(output, strValue);
                    }
                }
            }
 
            //detect int or Integer
            if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
                String tag = "i" + fields[i].getName() + "=";  //"i" is for Integer or int in the above soap response example+ field name for example Goals = "iGoals"
                if(input.contains(tag)){
                    String strValue = input.substring(input.indexOf(tag)+tag.length(), input.indexOf(";", input.indexOf(tag)));
                    if(strValue.length()!=0){
                        fields[i].setInt(output, Integer.valueOf(strValue));
                    }
                }
            }
 
            //detect float or Float
            if (type.equals(Float.TYPE) || type.equals(Float.class)) {
                String tag = "f" + fields[i].getName() + "=";
                if(input.contains(tag)){
                    String strValue = input.substring(input.indexOf(tag)+tag.length(), input.indexOf(";", input.indexOf(tag)));
                    if(strValue.length()!=0){
                        fields[i].setFloat(output, Float.valueOf(strValue));
                    }
                }
            }
        }
       
    }
}