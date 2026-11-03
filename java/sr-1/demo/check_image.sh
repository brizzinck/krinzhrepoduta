#!/bin/sh
# Етап 1: запитує POST /snapshot і перевіряє поле image з відповіді:
# довжину рядка Base64, розмір після декодування і перші байти (у JPEG це ffd8ff).
URL=${1:-http://localhost:8080/api/v1/camera/snapshot}

# Форма слова після числа: 1 символ, 2 символи, 5 символів.
plural() {
  n=$1; d=$((n % 10)); h=$((n % 100))
  if [ "$d" -eq 1 ] && [ "$h" -ne 11 ]; then w=$2
  elif [ "$d" -ge 2 ] && [ "$d" -le 4 ] && { [ "$h" -lt 12 ] || [ "$h" -gt 14 ]; }; then w=$3
  else w=$4
  fi
  echo "$n $w"
}

IMAGE=$(curl -s -X POST "$URL" | sed 's/^{"image":"//; s/"}$//')
CHARS=$(printf '%s' "$IMAGE" | wc -c)
BYTES=$(printf '%s' "$IMAGE" | base64 -d | wc -c)
START=$(printf '%s' "$IMAGE" | base64 -d | head -c 3 | od -An -tx1 | tr -d ' \n')

echo "довжина поля image: $(plural "$CHARS" символ символи символів)"
echo "після декодування: $(plural "$BYTES" байт байти байтів), початок: $START"
