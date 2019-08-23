ARG APP_INSIGHTS_AGENT_VERSION=2.3.1-SNAPSHOT
FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.0

ENV APP am-lib-testing-service.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 41
ENV JAVA_OPTS ""

COPY build/libs/$APP /opt/app/
COPY lib/applicationinsights-agent-2.3.1.jar lib/AI-Agent.xml /opt/app/

EXPOSE 3703

CMD ["am-lib-testing-service.jar"]
