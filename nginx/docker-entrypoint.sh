#!/bin/sh

envsubst '$NGINX_SERVER_NAME' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

while :; do sleep 6h & wait ${!}; nginx -s reload; done &
nginx -g "daemon off;"
