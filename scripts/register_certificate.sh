#!/bin/bash

truststore=cacerts
storepassword=changeit
wget https://s3.amazonaws.com/rds-downloads/rds-combined-ca-bundle.pem
awk 'split_after == 1 {n++;split_after=0} /-----END CERTIFICATE-----/ {split_after=1}{print > "rds-ca-" n ".pem"}' < rds-combined-ca-bundle.pem

for CERT in rds-ca-*; do
        alias=$(openssl x509 -noout -text -in $CERT | perl -ne 'next unless /Subject:/; s/.*(CN=|CN = )//; print')
        echo "Importing $alias"
        keytool -import -file ${CERT} -alias "${alias}" -storepass ${storepassword} -keystore ${truststore} -noprompt
        rm $CERT
done