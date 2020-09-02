REM java -jar 文件名.jar 参数
REM 参数说明 可多项 相加 例如 1+4=5 参数叠加 操作1参数在前
REM  操作			参数
REM  1-新大陆网络认证登录操作	用户名 密码
REM  2-新大陆网络认证注销操作
REM  4-VPN拨号			VPN连接名 VPN用户名 VPN密码 [电话簿名称]
REM  8-VPN断开			[VPN连接名]

set NUNICOM_JAVA_HOME="C:\Program Files\RedHat\java-11-openjdk-11.0.5-2"
set NUNICOM_OPER="5"
set NUNICOM_NEWLAND_USER="newland"
set NUNICOM_NEWLAND_PASS="newland"
set NUNICOM_VPN_ENTRY="vpn"
set NUNICOM_VPN_USER="user"
set NUNICOM_VPN_PASS="pass"

%NUNICOM_JAVA_HOME%\bin\java -Dfile.encoding=UTF-8 -jar NUnicom-1.0-SNAPSHOT.jar %NUNICOM_OPER% %NUNICOM_NEWLAND_USER% %NUNICOM_NEWLAND_PASS% %NUNICOM_VPN_ENTRY% %NUNICOM_VPN_USER% %NUNICOM_VPN_PASS%
