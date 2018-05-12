# Time Align Mobile

## Dev Env Start
First bring up the containers to do the initial build  
```
docker-compose up
```
Then stop the containers with `Ctrl-c`  

Make sure the device and dev env host are on same wifi  
Run the next two commands in separate shells (they are both interactive)  
You will need an expo login to start the node container
```
docker-compose run --service-ports clj lein figwheel
docker-compose run --service-ports node exp start --lan
```
Don't use the generated link to connect device use the ip address of the host machine
Should be able to send a link to an email using the prompts as a guide, then change the ip address in that link to match the dev host
