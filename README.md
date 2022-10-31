# Fake
Simple build system with an elementary support of incremental task execution.

Build file `fake.yaml`  should contain the declarative description of the Fake-tasks:
- unique name of the task (prepare, compile, build, …);
- _dependencies_ — a list of task's inputs — the names of other tasks which must be successfully executed before running this particular task, and/or files used by this task;
- _run_ — a bash command to execute when running a task;
- _target_ — the artifact of the bash command execution.

If there are no arguments, the first task is executed.

Example:
```
compile:
    dependencies:
        - main.c
    target: main.o
    run: gcc -c main.c -o main.o

build:
    dependencies:
        - compile
    target: main
    run: gcc main.o -o main

```
## Build and run tests
```
./gradlew build
```  

## Install
To create executable `./build/install/fake/bin/fake`:
```
./gradlew installDist
``` 

To make it executable everywhere add it to PATH variable
```
export PATH="$(pwd)/build/install/fake/bin:$PATH"
```

[How to change PATH variable on different platforms](https://gist.github.com/nex3/c395b2f8fd4b02068be37c961301caa7)
## Run

```
fake [tasks]
```






