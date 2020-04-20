import com.github.javaparser.ParseProblemException
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.validator.ProblemReporter
import com.github.javaparser.ast.visitor.VoidVisitor
import com.github.javaparser.utils.SourceRoot
import java.io.File

data class Triple<A, B, C>(val first: A, val second: B, val third: C)

fun analyzeSource(root: File) {
    val sourceRoot = SourceRoot(root.toPath())

    val connectionEnum = sourceRoot.parse("net.minecraft.network", "EnumConnectionState.java")

    val reporter = ProblemReporter { problem ->
        println("ERR: VISITOR REPORTED PROBLEM: ${problem.message}")
    }
    val visitor = ConnectionVisitor()
    connectionEnum.accept(visitor, reporter)
    val packetData = HashMap<Triple<Int, ConnectionVisitor.Direction, ConnectionVisitor.Stage>, HashMap<String, PacketVisitor.Type>>()
    val packetNames = HashMap<Triple<Int, ConnectionVisitor.Direction, ConnectionVisitor.Stage>, String>()
    for((key, packets) in visitor.map) {
        val (direction, stage) = key
        for((name, id) in packets) {
            packetNames[Triple(id, direction, stage)] = name
            try {
                if (name.contains(".")) {
                    val outerClass = name.substring(0, name.lastIndexOf("."))

                    val packet = sourceRoot.parse(
                        "net.minecraft.network.${stage.name.toLowerCase()}.${direction.toPackageString()}",
                        "$outerClass.java"
                    )
                    val visitor = PacketVisitor()
                    val inner = packet.getClassByName(outerClass).get()
                        .findAll(ClassOrInterfaceDeclaration::class.java)
                        .find { it.name.asString() == name.substring(name.lastIndexOf(".") + 1, name.length) }!!
                    inner
                        .accept(visitor, reporter)
                    packetData[Triple(id, direction, stage)] = visitor.packetFields
                } else {
                    val packet = sourceRoot.parse(
                        "net.minecraft.network.${stage.name.toLowerCase()}.${direction.toPackageString()}",
                        "$name.java"
                    )
                    val visitor = PacketVisitor()
                    packet.accept(visitor, reporter)
                    packetData[Triple(id, direction, stage)] = visitor.packetFields
                }
            }catch(e: ParseProblemException) {
                println("Skipping $name. Unable to parse")
                continue
            }
        }
    }

    for((key, fields) in packetData) {
        val (id, direction, stage) = key
        println("0x${id.toString(16)}; $direction; $stage :: ${packetNames[key]}")
        for((name, type) in fields) {
            println("\t$name => $type")
        }
    }
}
