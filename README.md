# Time Align Mobile

## Dev Env Start
On `Docker for Mac` copy the `docker-compose.override.yml.template` so that the correct IP address is used in expo.
Add *your IP address* to that copy.
```
cp docker-compose.override.yml.template docker-compose.override.yml
```

First bring up the containers with the UID mapped.  
```
UID=$UID docker-compose up
```

Make sure the device and dev env host are on same network.  
Run the next two commands in separate shells (they are both interactive).  
You will need an expo login to start the node container.
```
docker-compose exec clj /bin/bash
lein figwheel
```

```
docker-compose exec node
exp start --lan
```

## Add library
```
docker-compose run --service-ports node yarn add {library}
```
