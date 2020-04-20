# MC Packet Analyzer

A tool to try to extract a protocol spec from decompiled Minecraft classes.

In order to use, a compiled copy of the [Fernflower Decompiler](https://github.com/fesh0r/fernflower) must be placed in the working directory named fernflower.jar. The tool will emit a directory of compiled class files, and a seperate directory of decompiled class files, which are parsed to emit a rough specification to stdout.
