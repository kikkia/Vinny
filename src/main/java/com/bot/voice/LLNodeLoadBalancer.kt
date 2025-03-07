package com.bot.voice

import dev.arbjerg.lavalink.client.LavalinkClient
import dev.arbjerg.lavalink.client.LavalinkNode
import dev.arbjerg.lavalink.client.loadbalancing.ILoadBalancer
import dev.arbjerg.lavalink.client.loadbalancing.MAX_ERROR
import dev.arbjerg.lavalink.client.loadbalancing.VoiceRegion
import dev.arbjerg.lavalink.client.loadbalancing.builtin.IPenaltyProvider
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider
import kotlin.math.pow

class LLNodeLoadBalancer(private val client: LavalinkClient, val nodeHealth: HashMap<String, LLNodeHealthMonitor>) : ILoadBalancer {
    private val regionPenaltyProvider = VoiceRegionPenaltyProvider()
    private val penaltyProviders = mutableListOf<IPenaltyProvider>()
    private val ALLOWED_DEVIATION = 300


    override fun addPenaltyProvider(penaltyProvider: IPenaltyProvider) {
        penaltyProviders.add(penaltyProvider)
    }

    override fun removePenaltyProvider(penaltyProvider: IPenaltyProvider) {
        // Not used but need to override to be compatible
    }

    override fun selectNode(region: VoiceRegion?, guildId: Long?): LavalinkNode {
        val nodes = client.nodes

        // If one only just short circuit
        if (nodes.size == 1) {
            val node = nodes.first()

            if (!node.available) {
                throw IllegalStateException("Node ${nodes[0].name} is unavailable!")
            }

            return node
        }

        val nodeScores = ArrayList<Pair<LavalinkNode, Int>>()
        // Calculate healthy nodes
        for (node in nodes) {
            // as of current impl, 0 for same, 1000 for other
            val regionScore = regionPenaltyProvider.getPenalty(node, region)
            // We treat each player as 2 points
            val players = node.stats?.playingPlayers
            val frames = node.stats?.frameStats
            var deficitFramePenalty = 0
            var nullFramePenalty = 0

            // frame stats are per minute.
            // -1 or null means we don't have any frame stats. This is normal for very young nodes
            if (frames != null && frames.deficit != -1) {

                deficitFramePenalty = ( 1.03f.pow(100f * (frames.deficit / 3000f)) * 600 - 600 ).toInt()
                nullFramePenalty = ( 1.03f.pow(100f * (frames.nulled / 3000f)) * 600 - 600 ).toInt()
                nullFramePenalty *= 2
            }

            var penaltyProviderScore = 0
            for (penalty in penaltyProviders) {
                penaltyProviderScore += penalty.getPenalty(node, region)
            }

            val health = nodeHealth[node.name]?.getHealth()
            val healthScore = when (health) {
                NodeHealth.HEALTHY -> {
                    0
                }

                NodeHealth.DEGRADED -> {
                    600
                }

                NodeHealth.UNHEALTHY -> {
                    2000
                }

                null -> {
                    10000 // Something is very wrong here
                }

                else -> {
                    // Unknown could be lack of data so treat it similar to a healthy
                    100
                }
            }

            val score = if (node.available && players != null)
                regionScore + (players * 2) + deficitFramePenalty + nullFramePenalty + healthScore + penaltyProviderScore
            else
                MAX_ERROR

            nodeScores.add(Pair(node, score))
        }

        // Sort all nodes by score in ascending
        val sortedPairs = nodeScores.sortedBy { it.second }
        println(sortedPairs.map { "${it.first.name} - ${it.second}" })
        // Find the lowest score node
        val bestScore = sortedPairs.first().second
        // Find all possible nodes (The allowed deviation helps spread load across all seen as healthy)
        val bestChoices = sortedPairs.filter { it.second <= bestScore + ALLOWED_DEVIATION }


        return bestChoices.random().first
    }
}