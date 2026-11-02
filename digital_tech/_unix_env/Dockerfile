# Середовище для практичних робіт 2.1-2.4 (UNIX): Ubuntu 24.04, користувач lavrishko.
FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive LANG=C.UTF-8 LC_ALL=C.UTF-8

# Повертаємо man-сторінки та документацію, які вирізані в мінімальному образі.
RUN rm -f /etc/dpkg/dpkg.cfg.d/excludes \
 && dpkg-divert --remove --rename /usr/bin/man \
 && apt-get update \
 && apt-get install -y --no-install-recommends \
      tree psmisc procps man-db manpages less nano vim-tiny sudo at bc \
      bsdextrautils file e2fsprogs util-linux passwd \
 && apt-get install -y --reinstall --no-install-recommends coreutils procps passwd util-linux findutils grep sed \
 && rm -rf /var/lib/apt/lists/*

# Користувач lavrishko замість стандартного ubuntu.
RUN userdel -r ubuntu 2>/dev/null || true \
 && useradd -m -u 1000 -s /bin/bash -G sudo -c "Lavrishko Vladyslav" lavrishko \
 && echo 'lavrishko ALL=(ALL) NOPASSWD:ALL' > /etc/sudoers.d/lavrishko \
 && chmod 0440 /etc/sudoers.d/lavrishko

USER lavrishko
WORKDIR /home/lavrishko
CMD ["bash", "-l"]
