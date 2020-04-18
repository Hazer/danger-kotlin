#!/bin/sh

sudo -v && sudo="true" || sudo=""

if ! [[ -x "$(command -v danger)" ]]; then
	if ! [[ -x "$(command -v npm)" ]]; then
		echo "Please install node js"
		exit 1
	fi

	echo "Installing danger"
	npm install -g danger
fi

#if [[ -n "$sudo" && "$OSTYPE" != "darwin"* ]]; then
#	sudo chmod -R a+rwx /usr/local/
#fi

if ! [[ -x "$(command -v kotlinc)" ]]; then
    echo "Installing kotlin compiler 1.3.72"
    curl -o kotlin-compiler.zip -L https://github.com/JetBrains/kotlin/releases/download/v1.3.72/kotlin-compiler-1.3.72.zip
    mkdir -p /opt/ktc
    unzip -d /opt/ktc/ kotlin-compiler.zip
    echo 'PATH=/opt/ktc/kotlinc/bin:$PATH' >> ~/.bash_profile
    rm -rf kotlin-compiler.zip
fi

if ! [[ -x "$(command -v gradle)" ]]; then
    echo "Installing gradle 6.2.2"
    curl -o gradle.zip -L https://downloads.gradle-dn.com/distributions/gradle-6.2.2-bin.zip
    mkdir /opt/gradle
    unzip -d /opt/gradle gradle.zip
    echo 'export PATH=/opt/gradle/gradle-6.2.2/bin:$PATH' >> ~/.bash_profile
    rm -rf gradle.zip
fi

source ~/.bash_profile

make install_local
