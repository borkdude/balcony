
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

## Build

    scripts/build

## Deploy

`scp` `bin/balcony.js` to a server that runs `nodejs`. Set the variables
`MAIL_USER`, `MAIL_PASS`, `MAIL_TO` (comma seperated if you want multiple
addresses) and `WEATHER_API_KEY` in e.g. `.profile`.  Then hook the script up in
cron:

    crontab -e
    30 19 * * * /usr/bin/env bash -c '. $HOME/.profile && $HOME/balcony.js -m'

## Options

- `--mail` or `-m`: send an email if today's temperature exceeded threshold.

## Speed comparison with clj

Compared to the standalone `clj` in this repo on my VPS:

``` shell
$ time ./balcony.js -m
real	0m2.034s
user	0m0.330s
sys	0m0.046s

$ time ./balcony.clj -m
real    0m30.508s
user    0m12.479s
sys    0m0.341s
```

## Credits

Inspiration taken from

- https://gist.github.com/yogthos/d9d2324016f62d151c9843bdac3c0f23#file-gallery-cljs-L11
- https://github.com/paullucas/les-clj/tree/master/scripts
