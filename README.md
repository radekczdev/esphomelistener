# ESPHome Native API connector for JAVA
Sample project to understand, how ESPHome uses it's NATIVE API to send messages.
ESPHome uses simple customized TCP protocol to send messages to the device.

## How to use it
### Protocol buffers 
Because it is using Protocol Buffers for the messages, in resources folder you can find the api.proto file (based on [ESPHOME doc](https://github.com/esphome/esphome/blob/dev/esphome/components/api/api.proto)).

In order to generate java class with proper messages, you need to use the protoc compiler:
```shell 
/esphome-listener > protoc --java_out=src/main/java/ src/main/resources/api.proto
```

### Environment variables
This app uses 3 environment variables, which needs to be configured:
- _ESPDEVICEHOSTNAME_ - hostname/IP address of the ESPHome device
- _ESPDEVICEPORT_ - Port of the ESPHome device
- _ESPDEVICEPASS_ - Password of the ESPHome device API

## TODOs:
- ~~replace _Socket_ with _SocketChannel_ to make it non-blocking~~ done
- ~~add code, which will ensure answer to message has came - currently it's needed to press '_y_' multiple time and reprocess the _while_ loop~~ fixed
