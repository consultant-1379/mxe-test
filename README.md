# Machine Learning Model Execution Environment Test

Main repo which contains all the MXE tests

## How to use the repo

After clone initialize the submodule as following:

    git submodule update --init --recursive

## Use Bob for building

    bob/bob build-test

## Use Bob for testing with released MXE JCat
Set test environment (localLinux/remoteLinux/remoteLinuxSoi)
    TESTENVIRONMENT=localLinux

    bob/bob run-released-test

## Use Bob for testing with local built MXE JCat
Set test environment (localLinux/remoteLinux/remoteLinuxSoi)
    TESTENVIRONMENT=localLinux

    bob/bob run-test

## Build and run with local built MXE JCat
    bob/bob build-and-run-test



