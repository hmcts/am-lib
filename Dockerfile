FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.0

ENV APP am-lib-testing-service.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 41
ENV JAVA_OPTS ""

COPY build/libs/$APP /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q --spider http://localhost:3703/health || exit 1

EXPOSE 3703

CMD ["am-lib-testing-service.jar"]
