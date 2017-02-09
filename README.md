# YouTube information gatherer REST application

Available entry points:

GET /youtube/video/{video_id}  
GET /youtube/channel/{channel_id}  
GET /youtube/channel/{channel_id}/videos[?size=(int)]  

You can run the application instantly in an embedded tomcat by running a command
```
mvnw spring-boot:run
```

To build an executable for an application server, please use:
```
mvnw clean package
```

Built package would be under target directory named:
```
youtube-task.war
```


At this point you just need to put it under deployment directory of your application server.  
For tomcat the hot deploy directory is
```
$TOMCAT_HOME/webapps
```

The application path would be:
```
http://localhost:8080/youtube-task
```

Examples:
```
http://localhost:8080/youtube-task/youtube/channel/UC7yfnfvEUlXUIfm8rGLwZdA
http://localhost:8080/youtube-task/youtube/channel/UC7yfnfvEUlXUIfm8rGLwZdA/videos?size=40
http://localhost:8080/youtube-task/youtube/video/l8V6PkVV1Ec
```
