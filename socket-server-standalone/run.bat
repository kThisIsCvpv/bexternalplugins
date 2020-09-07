@ECHO OFF
java -jar socket-server.jar ^
	--port 26388 ^
	--room-limit 20 ^
	--ip-limit 10
pause 