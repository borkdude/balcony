
# balcony

A self-contained Clojure script that lets me know if I should water my
balcony at night.

## Credits

The idea to use `exec` came from [Planck](https://github.com/planck-repl/planck).

## Installation

Make sure [clj](https://clojure.org/guides/getting_started) is installed.
SCP the script to a server or run from your own machine.
Set the variables `MAIL_USER`, `MAIL_PASS` and `WEATHER_API_KEY` in e.g. `.profile`.
Then hook the script up in cron:

    crontab -e
    30 19 * * * /usr/bin/env bash -c '. $HOME/.profile && $HOME/balcony.clj -m'

## Options

- `--develop` or `-d`: development mode. Will start CIDER-nREPL.
- `--mail` or `-m`: send an email if today's temperature exceeded threshold.
