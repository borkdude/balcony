
# balcony

A self-contained Clojure script that lets me know if I should water my
balcony at night.

## Credits

The idea to use `exec` came from [Planck](https://github.com/planck-repl/planck).

## Installation

Make sure [clj](https://clojure.org/guides/getting_started) is installed.  `scp`
the script to a server or run from your own machine.  Set the variables
`MAIL_USER`, `MAIL_PASS`, `MAIL_TO` (comma seperated if you want multiple
addresses) and `WEATHER_API_KEY` in e.g. `.profile`.  Then hook the script up in
cron:

    crontab -e
    30 19 * * * /usr/bin/env bash -c '. $HOME/.profile && $HOME/balcony.clj -m'

## Options

- `--develop` or `-d`: development mode. Will start CIDER-nREPL.
- `--mail` or `-m`: send an email if today's temperature exceeded threshold.

## What's with the weird first few lines?

The first few lines are relevant to bash, but not to the Clojure program. Still
they have to be valid Clojure, because (using exec) clj executes the entire
file. Also, it's convenient to be able to evaluate your entire file inside your
editor. By making the Bash expressions readable by Clojure, you get no errors.
