/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.leadpony.pandora;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.LogManager;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * The top-level command.
 *
 * @author leadpony
 */
@Command(
        name = "pandora",
        description = "Pandora",
        mixinStandardHelpOptions = true,
        version = {
                "Pandora ${project.version}",
                "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                "OS: ${os.name} ${os.version} ${os.arch}"
        }
)
class PandoraCommand {

    private static final String BUNDLE_BASE_NAME =
            PandoraCommand.class.getPackageName() + ".messages";

    private final PrintWriter out;
    private final PrintWriter err;

    PandoraCommand() {
        this(System.out, System.err);
    }

    PandoraCommand(PrintStream out, PrintStream err) {
        this(new PrintWriter(out), new PrintWriter(err));
    }

    PandoraCommand(PrintWriter out, PrintWriter err) {
        this.out = out;
        this.err = err;
    }

    public int run(String... args) {
        configureLoggers();

        CommandLine commandLine = new CommandLine(this)
                .addSubcommand(new CommandLine.HelpCommand())
                .addSubcommand(new CropCommand())
                .setResourceBundle(getResourceBundle())
                .setOut(out)
                .setErr(err);

        commandLine.registerConverter(Margin.class, Margin::valueOf)
                   .registerConverter(Pages.class, Pages::valueOf)
                   .registerConverter(Aspect.class, Aspect::valueOf);

        if (args.length > 0) {
            return commandLine.execute(args);
        } else {
            commandLine.usage(out);
            return 0;
        }
    }

    private static ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle(
                BUNDLE_BASE_NAME,
                Locale.getDefault(),
                PandoraCommand.class.getClassLoader());
    }

    private void configureLoggers() {
        LogManager manager = LogManager.getLogManager();
        try (InputStream in = getClass().getResourceAsStream("logging.properties")) {
            manager.readConfiguration(in);
        } catch (IOException e) {
        }
    }
}
