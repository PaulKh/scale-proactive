@echo off
echo. 

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

set CLASSPATHEXT=%JAVA_HOME%\lib\tools.jar;%PROACTIVE%\compile\lib\ant.jar;%PROACTIVE%\compile\lib\ant-launcher.jar;%PROACTIVE%\lib\ws\xml-apis.jar;%PROACTIVE%\lib\xercesImpl.jar

call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD%  -Xmx256000000 org.apache.tools.ant.Main -buildfile "%PROACTIVE%\src\org\objectweb\proactive\examples\pi\scripts\build.xml" %*
pause
ENDLOCAL