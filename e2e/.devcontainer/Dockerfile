# syntax=docker/dockerfile:1
FROM mcr.microsoft.com/playwright:v1.47.0-jammy

USER root

ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Asia/Tokyo

RUN apt-get update -y
RUN apt-get install -y --no-install-recommends less git
RUN apt-get install -y --no-install-recommends tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    dpkg-reconfigure -f noninteractive tzdata

# ls系のエイリアスを予め使えるようにコメントを外す
RUN sed -i 's/^#alias ll=/alias ll=/; s/^#alias la=/alias la=/; s/^#alias l=/alias l=/' /home/vscode/.bashrc

USER pwuser
