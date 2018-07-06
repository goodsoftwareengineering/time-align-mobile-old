#!/bin/bash

iparr=($(hostname -I))
echo 'IP addr: ' ${iparr[0]}
read -r -p "Did you set docker-override? [y/N] " response
case "$response" in
    [yY][eE][sS]|[yY])
        tmux new -s time-align-mobile -d
        tmux rename-window -t time-align-mobile docker
        tmux send-keys -t time-align-mobile 'export UID' C-m
        tmux send-keys -t time-align-mobile 'docker-compose up' C-m

        sleep 5

        tmux new-window -t time-align-mobile
        tmux rename-window -t time-align-mobile clj-node
        tmux send-keys -t time-align-mobile 'export UID' C-m
        tmux send-keys -t time-align-mobile 'docker-compose exec clj lein figwheel' C-m
        tmux split-window -v -t time-align-mobile
        tmux send-keys -t time-align-mobile 'export UID' C-m
        tmux send-keys -t time-align-mobile 'docker-compose exec node exp start --lan --no-dev' C-m

        tmux attach -t time-align-mobile
        ;;
    *)
      echo 'Go set that then!'
        ;;
esac
