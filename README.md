# chat-service

## Build

Build server
```shell
./gradlew server:shadowJar
```

Build client
```shell
./gradlew client:shadowJar
```

## Quick Start

Run server
```shell
java -jar server/build/libs/server-1.0-SNAPSHOT-all.jar <configuration>
```

Run client
```shell
java -jar client/build/libs/client-1.0-SNAPSHOT-all.jar <configuration>
```

## Configuration

Configuration specifies **port** for server to run at, in JSON format:
```json
{
  "port": 8999
}
```
Path to configuration file is passed as a parameter in **run** commands.
File **conf.json** contains default configuration.

## Server

### Commands
```quit``` - stop server and quit

## Client

### Commands

```connect <name>``` - connect to server under specified **name**

```text <text>``` - send **text**

```file <file name> <relative path>``` - send file from **relative path** under specified **file name**

```quit``` - disconnect and quit

### Examples

1. On connection ```connect Bob``` you will receive a welcome message:
    ```shell
    [Welcome to chat, Bob!]
    ```
   Others will receive:
    ```shell
    [Bob joined chat]
    ```
2. On text message ```text hello jim``` others will receive your message:
    ```shell
    [Bob]: hello jim
    ```
3. On file message ```file hello.txt test.txt```
others will receive your file **test.txt** under name **hello.txt** and a message:
    ```shell
    [Bob sent file hello.txt]
    ```
4. On quit ```quit``` others will receive message:
    ```shell
    [Jim left chat]
    ```
