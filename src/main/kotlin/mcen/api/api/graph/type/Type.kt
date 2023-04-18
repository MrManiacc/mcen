package mcen.api.api.graph.type


enum class Type(val typeName: String, val typeClass: Class<*>) {
    BOOLEAN("Boolean", Boolean::class.javaObjectType),
    INT("Int", Int::class.javaObjectType),
    FLOAT("Float", Float::class.javaObjectType),
    STRING("String", String::class.javaObjectType),
    ANY("Any", Object::class.javaObjectType);
}