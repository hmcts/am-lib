ARG APP_INSIGHTS_AGENT_VERSION=2.3.1
FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.0

COPY build/libs/am-accessmgmt-api.jar /opt/app/
COPY lib/applicationinsights-agent-2.3.1.jar lib/AI-Agent.xml /opt/app/

EXPOSE 3703

CMD ["am-accessmgmt-api.jar"]
