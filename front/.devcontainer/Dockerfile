# devcontainer用のベースイメージ
FROM mcr.microsoft.com/vscode/devcontainers/base:ubuntu

# AWS CLIをインストール
RUN apt-get update
RUN apt-get install -y --no-install-recommends unzip curl groff less ca-certificates
RUN update-ca-certificates
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
RUN unzip awscliv2.zip
RUN ./aws/install
RUN rm -rf awscliv2.zip aws

# ls系のエイリアスを予め使えるようにコメントを外す
RUN sed -i 's/^#alias ll=/alias ll=/; s/^#alias la=/alias la=/; s/^#alias l=/alias l=/' /home/vscode/.bashrc
