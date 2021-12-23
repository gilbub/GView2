<?xml version="1.0" encoding="UTF-8"?>
  <Syslog defaultMask="WARNING">
    <Logger name="PrintWriterLog.err"
      class="com.protomatter.syslog.PrintWriterLog">  

      <Format class="com.protomatter.syslog.SimpleSyslogTextFormatter">  
        <showChannel>false</showChannel>
        <showThreadName>false</showThreadName>
        <showHostName>false</showHostName>
        <dateFormat>MM/dd/yyyy HH:mm:ss</dateFormat>
        <dateFormatCacheTime>1000</dateFormatCacheTime>
        <dateFormatTimeZone>America/Denver</dateFormatTimeZone>
      </Format>

      <Policy class="com.protomatter.syslog.SimpleLogPolicy">
        <channels>ALL_CHANNEL</channels>
        <logMask>INHERIT_MASK</logMask>
      </Policy>

      <stream>System.err</stream>
    </Logger>
  </Syslog>
