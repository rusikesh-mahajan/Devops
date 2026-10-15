FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DevOps Team <devops@gatepass.local>"
LABEL description="Production image for Automated Residential Visitor Gate-Pass System"

WORKDIR /app

ARG JAR_FILE=target/visitor-gate-pass-1.0.0.jar
COPY ${JAR_FILE} app.jar

# Run as non-root user for security best practice
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=25s --retries=5 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
