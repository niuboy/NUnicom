REM java -jar �ļ���.jar ����
REM ����˵�� �ɶ��� ��� ���� 1+4=5 �������� ����1������ǰ
REM  ����			����
REM  1-�´�½������֤��¼����	�û��� ����
REM  2-�´�½������֤ע������
REM  4-VPN����			VPN������ VPN�û��� VPN���� [�绰������]
REM  8-VPN�Ͽ�			[VPN������]

set NUNICOM_JAVA_HOME="C:\Program Files\RedHat\java-11-openjdk-11.0.5-2"
set NUNICOM_OPER="5"
set NUNICOM_NEWLAND_USER="newland"
set NUNICOM_NEWLAND_PASS="newland"
set NUNICOM_VPN_ENTRY="vpn"
set NUNICOM_VPN_USER="user"
set NUNICOM_VPN_PASS="pass"

%NUNICOM_JAVA_HOME%\bin\java -Dfile.encoding=UTF-8 -jar NUnicom-1.0-SNAPSHOT.jar %NUNICOM_OPER% %NUNICOM_NEWLAND_USER% %NUNICOM_NEWLAND_PASS% %NUNICOM_VPN_ENTRY% %NUNICOM_VPN_USER% %NUNICOM_VPN_PASS%
