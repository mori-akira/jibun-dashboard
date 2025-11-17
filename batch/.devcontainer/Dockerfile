FROM python:3.12

RUN apt-get update
RUN apt-get install -y --no-install-recommends apt-utils curl
RUN pip install -U pip
RUN pip install --no-cache-dir autopep8 flake8 pytest

# ls系のエイリアスを予め使えるように
RUN echo "alias ll='ls -alF'" >> /etc/bash.bashrc && \
    echo "alias la='ls -A'" >> /etc/bash.bashrc && \
    echo "alias l='ls -CF'" >> /etc/bash.bashrc
