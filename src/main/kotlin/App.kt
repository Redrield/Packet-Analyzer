import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.util.zip.ZipFile

class App : CliktCommand() {

    val analyzeFile: File by option().file(mustExist = true, canBeDir = false).required()
    val version: String by option().choice("1.13.2", "1.14.4").required()

    override fun run() {
        val output = File("decompile")
        if(output.exists()) {
            println("Flushing existing decompiled classes...")
            output.delete()
        }
        output.deleteOnExit()
        output.mkdirs()

        val compiledClasses = File("minecraft-classes")
        if(compiledClasses.exists()) {
            println("Flushing existing extracted classes...")
            compiledClasses.delete()
        }
        compiledClasses.deleteOnExit()
        compiledClasses.mkdirs()

        val jar = ZipFile(analyzeFile)
        val iter = jar.entries()
        while(iter.hasMoreElements()) {
            val entry = iter.nextElement()
            if(entry.name.endsWith(".class") && entry.name.contains("net/minecraft/network")) {
                val child = File(compiledClasses, entry.name)
                val input = jar.getInputStream(entry)
                if(!child.parentFile.exists()) {
                    child.parentFile.mkdirs()
                }
                child.createNewFile()
                child.writeBytes(input.readBytes())
                input.close()
            }
        }
        jar.close()

        if(version == "1.13.2") {
            val decompiler = File("fernflower.jar")

            runProcess("java", "-jar", decompiler.absolutePath, "-dgs=1", "-hdc=0", "-rbr=0", "-asc=1", "-udv=0", compiledClasses.absolutePath, output.absolutePath)

            analyzeSource(output)
        }
    }


    fun runProcess(vararg command: String) {
        val commandList = command.toMutableList()
        val proc = ProcessBuilder(commandList).inheritIO()
        proc.start().waitFor()
    }
}

fun main(args: Array<String>) = App().main(args)

