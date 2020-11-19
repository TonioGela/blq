/* Copyright 2020 Antonio Gelameris
 *
 * SPDX-License-Identifier: MIT */

package dev.toniogela.blq

import com.monovore.decline._
import dev.toniogela.blq.cli.Options.version
import dev.toniogela.blq.commands.{MainCommand, Subcommands}

object Main
    extends CommandApp(
      name = "blq",
      header = "Prints binlog files content optionally applying filters. Filters are in AND.",
      main = MainCommand.command.orElse(Subcommands.tableMaps).orElse(Subcommands.stats).orElse(version)
    )
