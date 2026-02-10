#!/bin/sh

envsubst '$NGINX_SERVER_NAME' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

while :; do sleep 6h & wait ${!}; nginx -s reload; done &
nginx -g "daemon off;"
