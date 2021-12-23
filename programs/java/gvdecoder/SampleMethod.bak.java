import java.lang.reflect.*;
          import java.awt.*;

          class SampleMethod {

             public static void main(String[] args) {
                Double p = new Double();
                showMethods(p);
             }

             static void showMethods(Object o) {
                Class c = o.getClass();
                Method[] theMethods = c.getMethods();
                for (int i = 0; i < theMethods.length; i++) {
                   String methodString = theMethods[i].getName();
                   System.out.println("Name: " + methodString);
                   String returnString =
                     theMethods[i].getReturnType().getName();
                   System.out.println("   Return Type: " + returnString);
                   Class[] parameterTypes = theMethods[i].getParameterTypes();
                   System.out.print("   Parameter Types:");
                   for (int k = 0; k < parameterTypes.length; k ++) {
                      String parameterString = parameterTypes[k].getName();
                      System.out.print(" " + parameterString);
                   }
                   System.out.println();
                }
             }
          }