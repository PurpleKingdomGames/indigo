if !(hash fswatch 2>/dev/null;) then
    echo "Please install the fswatch command from homebrew (`brew install fswatch`)"
    exit 1
fi

if !(hash http-server 2>/dev/null;) then
    echo "Please install the http-server module from npm (`npm install -g http-server`)"
    exit 1
fi

_term() {
  kill -TERM "$child" 2>/dev/null
}
trap _term SIGTERM

./build.sh $1
http-server ./ -o &

child=$!

fswatch -o  -r ./src ./static | fswatch -0 -l 0.25 -r ./src ./static | xargs -0 -n 1 -I {} ./build.sh $1

wait "$child"