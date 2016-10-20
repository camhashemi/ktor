package org.jetbrains.ktor.transform

import java.util.*

internal inline fun <reified T : Any> dfs(): List<Class<*>> = dfs(T::class.java)

internal fun dfs(type: Class<*>): List<Class<*>> = dfs(type, ::supertypes)

internal fun <T> dfs(root: T, parent: (T) -> MutableList<T>): List<T> {
    val result = LinkedHashSet<T>()
    dfs(mutableListOf(Pair(root, parent(root))), parent, mutableSetOf(root), result)

    return result.toList()
}

tailrec
internal fun <T> dfs(nodes: MutableList<Pair<T, MutableList<T>>>, parent: (T) -> MutableList<T>, path: MutableSet<T>, visited: MutableSet<T>) {
    if (nodes.isEmpty()) return

    val (current, children) = nodes[nodes.lastIndex]
    if (children.isEmpty()) {
        visited.add(current)
        path.remove(current)
        nodes.removeLast()
    } else {
        val next = children.removeLast()
        if (path.add(next)) {
            nodes.add(Pair(next, parent(next)))
        }
    }

    dfs(nodes, parent, path, visited)
}

private fun supertypes(clazz: Class<*>): MutableList<Class<*>> = when {
    clazz.superclass == null -> clazz.interfaces?.toMutableList() ?: mutableListOf<Class<*>>()
    clazz.interfaces == null || clazz.interfaces.isEmpty() -> mutableListOf(clazz.superclass)
    else -> ArrayList<Class<*>>(clazz.interfaces.size + 1).apply {
        clazz.interfaces.toCollection(this)
        add(clazz.superclass)
    }
}

internal fun <T> MutableList<T>.removeLast(): T = removeAt(lastIndex)