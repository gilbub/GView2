import ViewerParams
import trace.TraceParams
import trace
import NavigatorGraphParams

def property_system_show():
  en=gv.pv.prp.propertyNames()
  while en.hasMoreElements():
    key=en.next()
    w.print("%s = %s"%(key,gv.pv.prp.get(key)))

def property_system_get(key):
  """ Gets the value of a known system property.
      Usage: property_system_get('key') (note that 'key' is a string in quotes).
      Type 'help properties' for a general overview.
  """
  return gv.pv.prp.get(key)

def property_system_set(key,value):
  """ Sets the value of a known system property.
        Usage: property_system_set('key',value) (note that 'key' is a string in quotes).
        Notes: All boolean values are represented in Jython as a 0(False) or 1(True).
               You must save the properties (property_system_save()) in order to have
               them take effect next load time.   
      Type 'help properties' for a general overview.
  """
  gv.pv.prp.setProperty(key,value)

def property_system_save():
  """ Saves system properties (which are automatically loaded next time the program is run.)
      Note that routine specific properties are not saved.
      Type help properties for a general overview on properties.
  """
  file=open("properties.cfg","w")
  gv.pv.prp.store(file,"GView config file")

trace_properties=trace.TraceParams.getInstance()
viewer_properties=ViewerParams.getInstance()
navigator_properties=NavigatorGraphParams.getInstance()

def property_show(tp):
  f=tp.getClass().getFields()
  for i in range(len(f)):
    w.print("%s = %s"%(f[i].name,eval("tp.%s"%f[i].name)))

def property_show_all():
  """ Prints all system (directories etc) and routine specific (viewer,navigator,etc)
      parameters, with minimal instructions on how to set them.
      See property_system_save(), property_system_set(), property_system_get()
      
      For a general overview, type: 'help properties' 
 """
  w.print("System properties : set using property_system_set(\"key\",value)")
  w.print("                    ie 'property_system_set(\"framerate\",15)'")
  property_system_show()
  w.print("")
  w.print("Viewer properties : set by typing viewer_properties.'key'=value")
  w.print("                    ie viewer_properties.AutoLoadLastRois=1")
  property_show(viewer_properties)
  w.print("")
  w.print("navigator properties : set by typing navigator_properties.'key'=value")
  w.print("                    ie navigator_properties.AutoLoadLastChartRange=1")
  property_show(navigator_properties)
  w.print("")
  w.print("trace properties : set by typing trace_properties.'key'=value")
  w.print("                    ie trace_properties.FlipData=1")
  property_show(trace_properties)


