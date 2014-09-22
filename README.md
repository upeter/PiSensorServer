Look at the provided run-shell to see what's all configurable:
```
#!/bin/bash
#custom
export REST_HOST=localhost
export REST_PORT=8001

export NOTIFICATION_SEND_SAME_AFTER_MINUTES=5

export MAIL_RECIPIENT=urs_peter@gmx.ch
export MAIL_FROM=urs_peter@gmx.ch
export MAIL_SSL=true
export MAIL_HOST=mail.gmx.net
export MAIL_USER=urs_peter@gmx.ch
export MAIL_PASSWORD=takeyourstake

export SENSOR_SERIAL_PORT=/dev/ttyACM0
export SENSOR_UPDATE_INTERVAL=3s
export SENSOR_SAMPLE_STALE_AFTER_HOURS=5
export SENSOR_SAMPLE_MANDATORY_DEVIATION=45
export SENSOR_MAX_SAMPLES_MOVING_AVERAGE=10




java -cp target/scala-2.10/pi-server-assembly-0.1-SNAPSHOT.jar org.up.pi.Main
```                                                                                             
