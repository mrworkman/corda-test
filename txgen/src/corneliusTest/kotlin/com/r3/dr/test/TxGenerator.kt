package com.r3.dr.test

import com.r3.corda.lib.tokens.contracts.utilities.heldBy
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.contracts.utilities.of
import com.r3.corda.lib.tokens.money.GBP
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.r3.cornelius.junit.runners.CorneliusTestRunner
import com.r3.cornelius.test.Node
import com.r3.cornelius.test.NodeProvider
import com.r3.cornelius.test.extensions.legalIdentity
import com.r3.dr.test.gen.flows.GenerateIssuances
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import org.apache.logging.log4j.LogManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.Instant

@RunWith(CorneliusTestRunner::class)
class TxGenerator(
    private val nodeProvider: NodeProvider
) {

    private val logger = LogManager.getLogger()

    private lateinit var nodeA: Node
    private lateinit var nodeB: Node

    private lateinit var nodeAId: Party
    private lateinit var nodeBId: Party

    private inline fun <R> Node.rpc(block: CordaRPCOps.() -> R): R = openRpcConnection().use { conn ->
        conn.proxy.run {
            return block(this)
        }
    }

    @Before
    fun setup() {
        nodeA = nodeProvider.getNodeByName("NodeA")
        nodeB = nodeProvider.getNodeByName("NodeB")

        nodeAId = nodeA.legalIdentity
        nodeBId = nodeB.legalIdentity
    }

    @Test
    fun generateButtLoadsOfTransactions() {
        val numTokens = System.getProperty("numTokens")?.toInt() ?: 100

        logger.info("Issuing $numTokens tokens...")

        val startTime = Instant.now()

        (0..numTokens).forEach {
            if (it != 0 && it % 100 == 0) {
                logger.info("Created $it transactions after ${Duration.between(startTime, Instant.now())}.")
            }

            val tx1 = nodeA.rpc {
                startFlow(
                    ::IssueTokens, listOf(30 of GBP issuedBy nodeAId heldBy nodeAId), emptyList()
                ).returnValue.getOrThrow()
            }
        }

        logger.info("Finished after ${Duration.between(startTime, Instant.now())}.")
    }

    @Test
    fun `run GenerateIssuances`() {
        nodeA.rpc {
            startFlow(::GenerateIssuances, 11)
        }
    }
}