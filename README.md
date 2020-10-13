# blq

Filter, count events from, and print binlog files' content from the command line

## Installation

Build the application using `sbt nativeImage` and you will find the executable under `./target/native-image/blq`

## Usage

```
Usage: blq [--head <n>] [--filter <type>]... [--count] <binlogFile>

Prints binlog files content optionally applying filter. Filters do stack and apply to count too.

Options and flags:
    --help
        Display this help text.
    --head <n>
        Reads just the first n events.
    --filter <type>
        Filter events according to event type. Can be repeated.
    --count
        Count events matching filters.
```