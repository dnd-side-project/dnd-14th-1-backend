#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# .env 파일에서 설정 로드
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/../.env"

if [ ! -f "$ENV_FILE" ]; then
  echo -e "${RED}오류: .env 파일을 찾을 수 없습니다. ($ENV_FILE)${NC}"
  exit 1
fi

source "$ENV_FILE"

domains=("$NGINX_SERVER_NAME")
email="$CERTBOT_EMAIL"
staging=0

data_path="./certbot/conf"
www_path="./data/certbot/www"
ssl_path="./nginx/ssl"

echo -e "${GREEN}=== Let's Encrypt 인증서 초기화 스크립트 ===${NC}\n"
echo -e "${YELLOW}서버: ${domains[0]} / 이메일: $email${NC}\n"

if [ -z "$NGINX_SERVER_NAME" ] || [ -z "$CERTBOT_EMAIL" ]; then
  echo -e "${RED}오류: .env 파일에 NGINX_SERVER_NAME과 CERTBOT_EMAIL을 설정해주세요.${NC}"
  exit 1
fi

if [ -d "$data_path" ] && [ "$(ls -A $data_path)" ]; then
  read -p "기존 인증서 데이터가 존재합니다. 삭제하고 다시 시작하시겠습니까? (y/N) " decision
  if [ "$decision" = "Y" ] || [ "$decision" = "y" ]; then
    echo -e "${YELLOW}기존 데이터 삭제 중...${NC}"
    sudo rm -rf "$data_path"
  else
    echo -e "${YELLOW}스크립트를 종료합니다.${NC}"
    exit
  fi
fi

echo -e "${GREEN}필요한 디렉토리 생성 중...${NC}"
sudo mkdir -p "$data_path"
sudo mkdir -p "$www_path"
sudo mkdir -p "$ssl_path"
sudo chown -R $USER:$USER "$data_path" "$www_path" "$ssl_path"

if [ ! -f "$ssl_path/ssl-dhparams.pem" ]; then
  echo -e "${GREEN}DH Parameters 생성 중 (시간이 걸릴 수 있습니다)...${NC}"
  openssl dhparam -out "$ssl_path/ssl-dhparams.pem" 2048
  echo -e "${GREEN}DH Parameters 생성 완료${NC}"
fi

echo -e "${GREEN}각 도메인에 대한 임시 인증서 생성 중...${NC}"
for domain in "${domains[@]}"; do
  domain_path="$data_path/live/$domain"
  mkdir -p "$domain_path"

  echo -e "${YELLOW}  - $domain${NC}"
  openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
    -keyout "$domain_path/privkey.pem" \
    -out "$domain_path/fullchain.pem" \
    -subj "/CN=$domain" > /dev/null 2>&1
done

echo -e "${GREEN}nginx 컨테이너 시작 중...${NC}"
docker compose up -d nginx

echo -e "${YELLOW}nginx 시작 대기 중...${NC}"
sleep 5

if ! docker compose ps nginx | grep -q "Up"; then
  echo -e "${RED}오류: nginx 컨테이너가 시작되지 않았습니다.${NC}"
  echo -e "${YELLOW}로그 확인: docker compose logs nginx${NC}"
  exit 1
fi

echo -e "\n${GREEN}Let's Encrypt 인증서 발급 중...${NC}"

for domain in "${domains[@]}"; do
  echo -e "\n${YELLOW}===========================================${NC}"
  echo -e "${YELLOW}도메인: $domain${NC}"
  echo -e "${YELLOW}===========================================${NC}"

  rm -rf "$data_path/live/$domain"

  if [ $staging != "0" ]; then
    staging_arg="--staging"
    echo -e "${YELLOW}테스트 모드 (staging)${NC}"
  else
    staging_arg=""
  fi

  docker compose run --rm --entrypoint "\
    certbot certonly --webroot -w /var/www/certbot \
      $staging_arg \
      --email $email \
      --agree-tos \
      --no-eff-email \
      --force-renewal \
      -d $domain" certbot

  if [ $? -eq 0 ]; then
    echo -e "${GREEN}$domain 인증서 발급 성공${NC}"
  else
    echo -e "${RED}$domain 인증서 발급 실패${NC}"
    echo -e "${YELLOW}DNS 설정 및 방화벽(80/443 포트)을 확인해주세요.${NC}"
  fi
done

echo -e "\n${GREEN}nginx 재시작 중...${NC}"
docker compose restart nginx

echo -e "\n${GREEN}===========================================${NC}"
echo -e "${GREEN}=== 완료! ===${NC}"
echo -e "${GREEN}===========================================${NC}"
echo -e "모든 도메인의 인증서 발급 프로세스가 완료되었습니다."
echo -e "\n${YELLOW}다음 명령어로 상태 확인:${NC}"
echo -e "  docker compose ps"
echo -e "  docker compose logs nginx"
echo -e "\n${YELLOW}인증서 확인:${NC}"
echo -e "  docker compose exec nginx ls -la /etc/letsencrypt/live/"
echo -e "\n${GREEN}서비스가 정상적으로 실행 중입니다!${NC}"
