import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.validator.ProblemReporter
import com.github.javaparser.ast.validator.VisitorValidator

class PacketVisitor : VisitorValidator() {

    val packetFields = HashMap<String, Type>()

    enum class Type {
        VarInt,
        VarLong,
        Byte,
        Short,
        Int,
        Long,
        Float,
        Double,
        Boolean,
        BlockPos,
        TextComponent,
        UUID,
        NBT,
        String,
        ResourceLocation,
        ItemStack,
        ByteArray,
        VarIntArray,
        LongArray,
        Unknown
    }

    override fun visit(node: MethodDeclaration, arg: ProblemReporter) {
        if(node.name.asString() == "writePacketData") {
            val body = node.body.get()
            body.accept(PacketMethodVisitor(), arg)
        }
    }

    inner class PacketMethodVisitor : VisitorValidator() {
        override fun visit(expr: MethodCallExpr, arg: ProblemReporter) {
            if(expr.arguments.size < 1) {
                return
            }

            val fieldType = when(expr.name.asString()) {
                "writeByteArray" -> Type.ByteArray
                "writeVarIntArray"-> Type.VarIntArray
                "writeLongArray" -> Type.LongArray
                "writeBlockPos" -> Type.BlockPos
                "writeTextComponent" -> Type.TextComponent
                "writeUniqueId" -> Type.UUID
                "writeVarInt" -> Type.VarInt
                "writeVarLong" -> Type.VarLong
                "writeCompoundTag" -> Type.NBT
                "writeItemStack" -> Type.ItemStack
                "writeString" -> Type.String
                "writeResourceLocation" -> Type.ResourceLocation
                "writeBoolean" -> Type.Boolean
                "writeByte" -> Type.Byte
                "writeShort" -> Type.Short
                "writeInt" -> Type.Int
                "writeLong" -> Type.Long
                "writeFloat" -> Type.Float
                "writeDouble" -> Type.Double
                else -> Type.Unknown
            }
            val name = expr.arguments[0].toString().replace("this.", "")
            packetFields[name] = fieldType
        }
    }
}