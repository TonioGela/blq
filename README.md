# blq
Filter, count and pretty print binlog files' events from the command line

![Build](https://github.com/TonioGela/blq/workflows/Build/badge.svg) ![GitHub last commit (branch)](https://img.shields.io/github/last-commit/TonioGela/blq/master) [![Latest Release](https://img.shields.io/github/v/release/toniogela/blq?include_prereleases)](https://github.com/TonioGela/blq/releases/latest)

![GitHub](https://img.shields.io/github/license/toniogela/blq) [![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit&logoColor=white)](https://github.com/pre-commit/pre-commit) [![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

## Installation

Build the application using `sbt nativeImage` and you will find the executable under `./target/native-image/blq`

## BLQ Usage

```
Usage:
    blq [--filter <type>]... [--head <integer> | --range <n:m>] [--header] [--count | --with-numbers] <binlogFile>
    blq tables
    blq stats

Prints binlog files content optionally applying filters. Filters are in AND.

Options and flags:
    --help
        Display this help text.
    --filter <type>
        Filter events according to event type. Can be repeated.
    --head <integer>
        Prints just the first n  matching events.
    --range <n:m>
        Prints just the matching events in the given inclusive range.
    --header
        Prints just the headers.
    --count
        Count events matching filters.
    --with-numbers
        Show event numbers
    --version, -v
        Prints version number and exits.

Subcommands:
    tables
        Prints a summary of the table_map informations in the binlog file
    stats
        Prints the statistics of the passed binlog files
```

## Examples

The `stats` subcommand generates an output like

```
┌──────────────────────────────────────────────────────────┐
│ SUMMARY                                                  │
├──────────────────────────────────────────────────────────┤
│ From: 2021-04-28 19:25:55 GMT+02:00 [Europe/Rome]        │
│ To:   2021-04-28 19:06:34 GMT+02:00 [Europe/Rome]        │
│ Number of DDLs: 0                                        │
├──────────────────────────────────────────────────────────┤
│ database │ table        │ # insert │ # delete │ # update │
├──────────────────────────────────────────────────────────┤
│ foo      │ FOO_BAR_PROD │ 1        │ 0        │ 0        │
│ bar      │ FOO_BAR_QA   │ 102      │ 5        │ 0        │
│ baz      │ FOO_BAR_DEV  │ 34       │ 0        │ 3        │
└──────────────────────────────────────────────────────────┘
```
