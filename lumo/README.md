
# balcony

Port of the `clj` script in this repo to `ClojureScript` and `lumo` on `nodejs`.

## Setup

Make sure [clj](https://clojure.org/guides/getting_started),
[nodejs](https://nodejs.org/en/), [yarn](https://yarnpkg.com/en/) and
[lumo](http://lumo-cljs.org/) are installed. Then:

    yarn install

## Run

Make sure the environment variables described in [deploy](#deploy) are set
correctly. Then:

    scripts/balcony

## Build (optional)

To create a standalone file that contains all the code including dependencies:

    scripts/build

## Deploy

Set the variables `MAIL_USER`, `MAIL_PASS`, `MAIL_TO` (comma seperated if you
want multiple addresses) and `WEATHER_API_KEY` in e.g. `.profile`.

### Running directly with lumo without building

On the server:

    git clone https://github.com/borkdude/balcony.git
    cd balcony/lumo
    yarn install
    crontab -e

Add:

    30 19 * * * /usr/bin/env bash -c '. $HOME/.profile ; cd $HOME/balcony/lumo/; scripts/balcony -m'

### Single file deploy

Build first. Then `scp` `bin/balcony.js` to the server. Then hook the script up
in cron:

    crontab -e
    30 19 * * * /usr/bin/env bash -c '. $HOME/.profile; $HOME/balcony.js -m'

## Options

- `--mail` or `-m`: send an email if today's temperature exceeded threshold.

## Speed comparison with clj

Compared to the standalone `clj` in this repo on my VPS:

``` shell
# with the build step

$ time ./balcony.js -m
real	0m2.034s
user	0m0.330s
sys	0m0.046s

# JVM

$ time ./balcony.clj -m
real    0m30.508s
user    0m12.479s
sys    0m0.341s

# Directly with lumo, no build step

$ cd lumo
$ time scripts/balcony -m
real	0m2.672s
user	0m1.257s
sys	0m0.092s
```

## Credits

Huge thanks to @anmonteiro and @richiardiandrea from the #lumo channel on
[Clojurians Slack](http://clojurians.net/).

Inspiration taken from

- https://gist.github.com/yogthos/d9d2324016f62d151c9843bdac3c0f23#file-gallery-cljs-L11
- https://github.com/paullucas/les-clj/tree/master/scripts

## TODO

- Dockerize?

See https://github.com/nodejs/docker-node#how-to-use-this-image

        docker run -v$(PWD)/:/home/node/app -w/home/node/app node yarn install
        docker run -v$(PWD)/:/home/node/app -w/home/node/app node yarn add lumo-cljs --dev
        docker run -v$(PWD)/:/home/node/app -w/home/node/app -it node yarn lumo

Different location for node_modules:

        docker run -v$(PWD)/:/home/node/app -v$(PWD)/../node_modules/:/tmp/node_modules/ -w/home/node/app -it node yarn install --modules-folder /tmp/node_modules

- Look at https://github.com/Cumulo/cumulo-workflow for inspiration
