# devcontainer用のベースイメージ
FROM mcr.microsoft.com/vscode/devcontainers/python:3-bookworm

# tfenv (Terraformのバージョン管理ツール) をgitからインストールし、最新のTerraformをインストール
RUN git clone --depth=1 https://github.com/tfutils/tfenv.git /opt/tfenv
ENV PATH=$PATH:/opt/tfenv/bin
RUN tfenv install latest && tfenv use latest

# AWS CLIをインストール
RUN apt-get update && \
    apt-get install -y --no-install-recommends unzip curl groff less && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install && \
    rm -rf awscliv2.zip aws

# ls系のエイリアスを予め使えるようにコメントを外す
RUN sed -i 's/^#alias ll=/alias ll=/; s/^#alias la=/alias la=/; s/^#alias l=/alias l=/' /home/vscode/.bashrc
