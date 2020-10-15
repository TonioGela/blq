# blq
Filter, count and pretty print binlog files' events from the command line

![Build](https://github.com/TonioGela/blq/workflows/Build/badge.svg) ![GitHub last commit (branch)](https://img.shields.io/github/last-commit/TonioGela/blq/master) [![Latest Release](https://img.shields.io/github/v/release/toniogela/blq?include_prereleases)](https://github.com/TonioGela/blq/releases/latest)

![GitHub](https://img.shields.io/github/license/toniogela/blq) [![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit&logoColor=white)](https://github.com/pre-commit/pre-commit)

## Installation

Build the application using `sbt nativeImage` and you will find the executable under `./target/native-image/blq`

## Usage 

```
Usage: blq [--filter <type>]... [--head <integer> | --range <n:m>] [--count | --with-numbers] [--header] <binlogFile>

Prints binlog files content optionally applying filters. Filters are in AND.

Options and flags:
    --help
        Display this help text.
    --version, -v
        Prints version number and exits.
    --filter <type>
        Filter events according to event type. Can be repeated.
    --head <integer>
        Prints just the first n  matching events.
    --range <n:m>
        Prints just the matching events in the given inclusive range.
    --count
        Count events matching filters.
    --with-numbers
        Show event numbers
    --header
        Prints just the headers.
```