FROM public.ecr.aws/docker/library/amazoncorretto:21-alpine-jdk

COPY . /app
WORKDIR /app

RUN apk update  \
    && apk add maven  \
    && apk add bash \
    && apk cache clean \
    && mvn package -DskipTests \
    && mv $(find target -name "*.jar" ! -name "*.original" | head -n 1) ./videostreamingpoc.jar \
    && chmod 777 ./videostreamingpoc.jar

EXPOSE 8080

COPY command/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]