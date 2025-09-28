# syntax=docker/dockerfile:1
FROM mcr.microsoft.com/playwright:v1.50.0-jammy

ENV TZ=Asia/Tokyo

RUN apt-get update -y
RUN apt-get install -y --no-install-recommends less git

# ls系のエイリアスを予め使えるように
RUN echo "alias ll='ls -alF'" >> /etc/bash.bashrc && \
    echo "alias la='ls -A'" >> /etc/bash.bashrc && \
    echo "alias l='ls -CF'" >> /etc/bash.bashrc
