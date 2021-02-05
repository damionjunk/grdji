#!/bin/bash

session_name="grdji"

tmux new-session -d -s $session_name
tmux rename-window -t 0 "api"
tmux send-keys -t "api" "lein run" C-m
tmux split-window -v -t "api" "lein test-refresh"
tmux split-window -h -t "api" "$SHELL"

tmux attach-session -t $session_name:0
