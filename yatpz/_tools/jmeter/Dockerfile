# Apache JMeter 5.6.3 на JRE 21: запуск без GUI та GUI під Xvfb для знімків екрана
FROM eclipse-temurin:21-jre
ENV JMETER_VERSION=5.6.3
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl xvfb xdotool imagemagick openbox fontconfig fonts-dejavu libxtst6 libxi6 libxrender1 \
 && rm -rf /var/lib/apt/lists/* \
 && curl -fsSL "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz" | tar -xz -C /opt \
 && ln -s "/opt/apache-jmeter-${JMETER_VERSION}/bin/jmeter" /usr/local/bin/jmeter
# Тека резервних копій плану, у яку GUI пише під час збереження
RUN install -d -m 1777 "/opt/apache-jmeter-${JMETER_VERSION}/backups"
WORKDIR /test
