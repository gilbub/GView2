"""
System and class properties:
  
  GView uses system properties (contained in a java.util.properties object) 
  and 'class properties' (contained in static data structures for a few 
  classes) to hold user changeable parameters. Only the system properties can
  be saved and are automatically loaded the next time the program is run. (The
  user can write scripts that set parameters for all other program settings as 
  a general workaround).
  
  System properties mostly contain information on directories. The data is saved in
  a file called 'viewer.cfg' in the base directory ('decoder') of the program. It is 
  automatically loaded each time the program is run.
  
  'Class' properties are mostly toggles for various program behaviours. These are held
  in  trace_properties,viewer_properties,navigator_properties.
    viewer_properties holds variables dealing with operation of the movie viewer.
    navigator_properties holds variables dealing with the navigator graph object.
    trace_properties holds variables that deal with individual traces in the navigator graph.
   
  All public variables (including but not limited to the ones discussed above) are directly 
  accessible by Jython, and can be set by user scripts.

 Usage:
   Assuming that the package was imported as 'prop':
    
   To see all the properties listed, with their values, type 'prop.property_show_all()'
 
   For system properties, type help 'topic name':
     prop.property_system_show()
     prop.property_system_get()
     prop.property_system_set()
     prop.property_system_save()
   System properties must be set indirectly using a quoted string for the variable name and
   a value. ie prop.property_system_set('framerate',15). Note that if the value is a directory name
   it must be a string (enclosed in quotes).
   

   For other properties, properties can be viewed by typing 'prop.property_show(prop.classname), 
   where classname is either viewer_properties, navigator_properties, or trace_properties. Do
   not use quotes as these are classes not strings.
   Properties can be set directly for these (ie prop.trace_properties.FlipData=1)
   
"""

import ViewerParams
import trace.TraceParams
import trace
import NavigatorGraphParams
from property import *




