FROM openjdk:8-jdk-alpine
ADD target/*.jar app.jar
ENV JAVA_OPTS="-server -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
ENTRYPOINT [ "sh", "-c", "exec java -Dspring.profiles.active=sample $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
