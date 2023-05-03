package com.tap.skynet.topology

/**
 * Creates a balanced tree where each node has at most maxChildrenPerNode children
 */
fun clusterToTree(
    maxChildrenPerNode: Int,
    cluster: Set<String>,
): Map<String, Set<String>> {

    val tierRanges = generateSequence(0) { it * maxChildrenPerNode + 2 }
        .takeWhile { it - cluster.size < cluster.size }
        .fold(mutableListOf<IntRange>()) { acc, bound ->
            acc.apply {
                if (acc.isEmpty()) {
                    add(1..1)
                } else {
                    val lower = acc.last().last + 1
                    val upper = lower + bound
                    add(lower..upper)
                }
            }
        }

    return cluster.foldIndexed(mutableMapOf()) { idx, acc, node ->
        val index = idx + 1
        val tierIndex = tierRanges.indexOfFirst { range -> range.contains(index) }
        val parentTierIndex = tierIndex - 1
        val childTierIndex = tierIndex + 1

        val parent = tierRanges.getOrNull(parentTierIndex)?.let { parentTier ->
            val parentNodes = parentTier.map { cluster.elementAt(it - 1) }
            val parent = parentNodes.first{ acc[it]?.contains(node) ?: false}
            setOf(parent)
        } ?: emptySet()

        val children = tierRanges.getOrNull(childTierIndex)?.let { childrenTier ->
            val firstInTier = tierRanges.getOrNull(tierIndex)?.first ?: throw IllegalStateException("here")
            val firstChildIndex = (index - firstInTier) * maxChildrenPerNode + childrenTier.first
            (firstChildIndex until firstChildIndex + maxChildrenPerNode)
                .filter { it <= childrenTier.last }
                .mapNotNull { cluster.elementAtOrNull(it - 1) }
                .toSet()
        } ?: emptySet()

        acc.apply {
            put(node, parent + children)
        }
    }
}
