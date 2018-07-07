# Time Align Mobile

## Dev Env Start
On `Docker for Mac` copy the `docker-compose.override.yml.template` so that the correct IP address is used in expo.
Add *your IP address* to that copy.
```
cp docker-compose.override.yml.template docker-compose.override.yml
```

First bring up the containers with the UID mapped.  
```
export UID
docker-compose up
```

Make sure the device and dev env host are on same network.  
Run the next two commands in separate shells (they are both interactive).  
You will need an expo login to start the node container.  

Yarn install the first time before compiling with figwheel
```
export UID
docker-compose exec node /bin/bash
yarn install
exit
```

Compile with Figwheel
```
export UID
docker-compose exec clj /bin/bash
lein figwheel
```

Start expo (no dev is to not have node and figwheel both doing live/hot reloading) (idk the difference between live vs hot)
```
export UID
docker-compose exec node /bin/bash
exp start --lan --no-dev
```

## Add library
When containers are running
```
export UID
docker-compose exec node yarn add {library} 
```
## Errors that should have messages
- calling a function by a symbol that doesn't resolve
- styles taking wrong type (string instead of number for padding and margins)
- forgetting to deref subscription in component
- when destructured args don't match and those functions are called
