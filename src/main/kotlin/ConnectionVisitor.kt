import com.github.javaparser.ast.*
import com.github.javaparser.ast.body.*
import com.github.javaparser.ast.comments.BlockComment
import com.github.javaparser.ast.comments.JavadocComment
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.*
import com.github.javaparser.ast.modules.*
import com.github.javaparser.ast.stmt.*
import com.github.javaparser.ast.type.*
import com.github.javaparser.ast.validator.ProblemReporter
import com.github.javaparser.ast.validator.VisitorValidator
import com.github.javaparser.ast.visitor.VoidVisitor
import java.util.*

class ConnectionVisitor : VisitorValidator() {
    enum class Direction {
        CLIENTBOUND,
        SERVERBOUND;

        fun toPackageString() = when(this) {
            CLIENTBOUND -> "server"
            SERVERBOUND -> "client"
        }

        companion object {
            fun fromExprString(s: String): Direction {
                return when {
                    s.contains("CLIENTBOUND") -> CLIENTBOUND
                    s.contains("SERVERBOUND") -> SERVERBOUND
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    enum class Stage {
        HANDSHAKING,
        PLAY,
        STATUS,
        LOGIN,
    }


    var acc = 0
    val map = HashMap<Pair<Direction, Stage>, HashMap<String, Int>>()

    override fun visit(expr: MethodCallExpr, arg: ProblemReporter) {
        if(expr.name.asString().contains("registerPacket")) {
            acc++
            val args = expr.arguments
            val direction = Direction.fromExprString(args[0].toString())
            val className = args[1] as ClassExpr
            val stage = when(acc) {
                1 -> Stage.HANDSHAKING
                in 2..130 -> Stage.PLAY
                in 131..134 -> Stage.STATUS
                else -> Stage.LOGIN
            }
            val inner = map.computeIfAbsent(direction to stage) { HashMap() }

            // Same thing Minecraft does internally to generate packet id mappings
            inner[className.type.asString()] = inner.size
        }
    }

}
